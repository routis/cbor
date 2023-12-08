package io.github.routis.cbor.internal

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.routis.cbor.*
import kotlinx.serialization.json.*

internal fun JsonOptions.dataItemToJson(dataItem: DataItem): JsonElement {
    fun convert(item: DataItem): JsonElement =
        when (item) {
            is DataItem.Integer.Unsigned -> JsonPrimitive(item.value)
            is DataItem.Integer.Negative -> item.asBigInteger().toJson()
            is DataItem.ByteString -> byteStringOption(item)
            is DataItem.TextString -> JsonPrimitive(item.text)
            is DataItem.Array -> item.map(::convert).let(::JsonArray)
            is DataItem.CborMap ->
                buildMap {
                    item.forEach { (k, v) -> keyAsString(k)?.let { put(it, convert(v)) } }
                }.let(::JsonObject)

            is DataItem.Tagged<*> ->
                when (item) {
                    is DataItem.Tagged.StandardDateTimeString -> convert(item.content)
                    is DataItem.Tagged.EpochBasedDateTime.Integer -> convert(item.content)
                    is DataItem.Tagged.EpochBasedDateTime.HalfFloat -> convert(item.content)
                    is DataItem.Tagged.EpochBasedDateTime.SingleFloat -> convert(item.content)
                    is DataItem.Tagged.EpochBasedDateTime.DoubleFloat -> convert(item.content)
                    is DataItem.Tagged.BigNumUnsigned -> item.asBigInteger().toJson()
                    is DataItem.Tagged.BigNumNegative -> item.asBigInteger().toJson()
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

private fun JsonOptions.keyAsString(k: Key<*>): String? =
    when (k) {
        is Key.BoolKey -> keyOptions.boolKeyMapper?.invoke(k)
        is Key.ByteStringKey -> keyOptions.byteStringKeyMapper?.invoke(k)
        is Key.IntegerKey -> keyOptions.integerKeyMapper?.invoke(k)
        is Key.TextStringKey -> k.item.text
    }

private fun BigInteger.toJson() = JsonUnquotedLiteral(toString(10))
