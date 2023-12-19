package io.github.routis.cbor.internal

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.routis.cbor.*
import kotlinx.serialization.json.*

internal fun JsonOptions.dataItemToJson(dataItem: DataItem): JsonElement {
    fun convert(item: DataItem): JsonElement =
        when (item) {
            is UnsignedIntegerDataItem -> JsonPrimitive(item.value)
            is NegativeIntegerDataItem -> item.asBigInteger().toJson()
            is ByteStringDataItem -> byteStringOption(item)
            is TextStringDataItem -> JsonPrimitive(item.text)
            is ArrayDataItem -> item.map(::convert).let(::JsonArray)
            is MapDataItem ->
                buildMap {
                    item.forEach { (k, v) -> keyAsString(k)?.let { put(it, convert(v)) } }
                }.let(::JsonObject)

            is TaggedDataItem<*> ->
                when (item) {
                    is StandardDateTimeDataItem -> convert(item.content)
                    is IntegerEpochDataItem -> convert(item.content)
                    is HalfFloatEpochDataItem -> convert(item.content)
                    is SingleFloatEpochDataItem -> convert(item.content)
                    is DoubleFloatEpochDataItem -> convert(item.content)
                    is BigNumUnsigned -> item.asBigInteger().toJson()
                    is BigNumNegative -> item.asBigInteger().toJson()
                    is DecimalFraction -> TODO("Implement toJson for DecimalFraction")
                    is BigFloat -> TODO("Implement toJson for BigFloat")
                    is EncodedText -> convert(item.content)
                    is CborDataItem -> convert(decode(item.content.bytes))
                    is SelfDescribedCbor -> convert(item.content)
                    is UnsupportedTagDataItem -> JsonNull
                    is CalendarDayDataItem -> convert(item.content)
                    is CalendarDateDataItem -> convert(item.content)
                }

            is BooleanDataItem -> JsonPrimitive(item.value)
            is HalfPrecisionFloatDataItem -> JsonPrimitive(item.value)
            is SinglePrecisionFloatDataItem -> JsonPrimitive(item.value)
            is DoublePrecisionFloatDataItem -> JsonPrimitive(item.value)
            is ReservedDataItem -> JsonNull
            is UnassignedDataItem -> JsonNull
            UndefinedDataItem -> JsonNull
            NullDataItem -> JsonNull
        }

    return convert(dataItem)
}

private fun JsonOptions.keyAsString(k: Key<*>): String? =
    when (k) {
        is BoolKey -> keyOptions.boolKeyMapper?.invoke(k)
        is ByteStringKey -> keyOptions.byteStringKeyMapper?.invoke(k)
        is IntegerKey -> keyOptions.integerKeyMapper?.invoke(k)
        is TextStringKey -> k.item.text
    }

private fun BigInteger.toJson() = JsonUnquotedLiteral(toString(10))
