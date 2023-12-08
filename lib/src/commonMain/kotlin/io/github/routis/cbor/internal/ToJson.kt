package io.github.routis.cbor.internal

import io.github.routis.cbor.*
import kotlinx.serialization.json.*

internal fun dataItemToJson(
    dataItem: DataItem,
    options: JsonOptions,
): JsonElement {
    val bigIntSupport = bigIntSupport()

    fun keyAsString(k: Key<*>): String? {
        val keyOptions = options.keyOptions
        return when (k) {
            is Key.BoolKey -> keyOptions.boolKeyMapper?.invoke(k)
            is Key.ByteStringKey -> keyOptions.byteStringKeyMapper?.invoke(k)
            is Key.IntegerKey -> keyOptions.integerKeyMapper?.invoke(k)
            is Key.TextStringKey -> k.item.text
        }
    }

    fun convert(item: DataItem): JsonElement =
        when (item) {
            is DataItem.Integer.Unsigned -> JsonPrimitive(item.value)
            is DataItem.Integer.Negative -> JsonPrimitive(bigIntSupport.negativeFrom(item.value))
            is DataItem.ByteString -> options.byteStringOption(item)
            is DataItem.TextString -> JsonPrimitive(item.text)
            is DataItem.Array -> item.map(::convert).let(::JsonArray)
            is DataItem.CborMap ->
                buildMap {
                    item.forEach { (k, v) -> keyAsString(k)?.let { put(it, convert(v)) } }
                }.let(::JsonObject)

            is DataItem.Tagged<*> ->
                when (item) {
                    is DataItem.Tagged.StandardDateTimeString -> convert(item.content)
                    is DataItem.Tagged.EpochBasedDateTime.Unsigned -> convert(item.content)
                    is DataItem.Tagged.EpochBasedDateTime.Negative -> convert(item.content)
                    is DataItem.Tagged.EpochBasedDateTime.HalfFloat -> convert(item.content)
                    is DataItem.Tagged.EpochBasedDateTime.SingleFloat -> convert(item.content)
                    is DataItem.Tagged.EpochBasedDateTime.DoubleFloat -> convert(item.content)
                    is DataItem.Tagged.BigNumUnsigned -> JsonPrimitive(bigIntSupport.unsignedFrom(item.content.bytes))
                    is DataItem.Tagged.BigNumNegative -> JsonPrimitive(bigIntSupport.negativeFrom(item.content.bytes))
                    is DataItem.Tagged.DecimalFraction -> TODO("Implement toJson for DecimalFraction")
                    is DataItem.Tagged.BigFloat -> TODO("Implement toJson for BigFloat")
                    is DataItem.Tagged.EncodedText -> convert(item.content)
                    is DataItem.Tagged.CborDataItem -> convert(decode(item.content.bytes))
                    is DataItem.Tagged.SelfDescribedCbor -> convert(item.content)
                    is DataItem.Tagged.Unsupported -> JsonNull
                    is DataItem.Tagged.CalendarDay -> convert(item.content)
                    is DataItem.Tagged.CalendarDate -> convert(item.content)
                }

            is DataItem.Bool -> JsonPrimitive(item.value)
            is DataItem.HalfPrecisionFloat -> JsonPrimitive(item.value)
            is DataItem.SinglePrecisionFloat -> JsonPrimitive(item.value)
            is DataItem.DoublePrecisionFloat -> JsonPrimitive(item.value)
            is DataItem.Reserved -> JsonNull
            is DataItem.Unassigned -> JsonNull
            DataItem.Undefined -> JsonNull
            DataItem.Null -> JsonNull
        }

    return convert(dataItem)
}
