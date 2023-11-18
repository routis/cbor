package io.github.routis.cbor

import kotlinx.serialization.json.*
import java.math.BigInteger
import kotlin.io.encoding.Base64


fun toJson(cbor: DataItem): JsonElement {
    return when (cbor) {
        is DataItem.UnsignedInteger -> JsonPrimitive(cbor.value)
        is DataItem.NegativeInteger -> JsonPrimitive(cbor.value)
        is DataItem.ByteString -> JsonPrimitive(Base64.UrlSafe.encode(cbor.value.toByteArray()))
        is DataItem.TextString -> JsonPrimitive(cbor.value)
        is DataItem.Array -> cbor.mapNotNull { toJson(it) }.let(::JsonArray)
        is DataItem.CborMap -> {
            return cbor.mapNotNull { (k, v) ->
                val jsonKey = when (k) {
                    is Key.BoolKey -> k.value.value.toString()
                    is Key.ByteStringKey -> Base64.UrlSafe.encode(k.value.value.toByteArray())
                    is Key.NegativeIntegerKey -> k.value.value.toString()
                    is Key.UnsignedIntegerKey -> k.value.value.toString()
                    is Key.TextStringKey -> k.value.value
                }
                val jsonValue = toJson(v) ?: JsonNull
                jsonKey to jsonValue
            }.toMap().let(::JsonObject)
        }

        is DataItem.Tagged<*> -> when (cbor) {
            is DataItem.Tagged.StandardDateTimeString -> toJson(cbor.item)
            is DataItem.Tagged.EpochBasedDateTime.Unsigned -> toJson(cbor.item)
            is DataItem.Tagged.EpochBasedDateTime.Negative -> toJson(cbor.item)
            is DataItem.Tagged.EpochBasedDateTime.HalfFloat -> toJson(cbor.item)
            is DataItem.Tagged.EpochBasedDateTime.SingleFloat -> toJson(cbor.item)
            is DataItem.Tagged.EpochBasedDateTime.DoubleFloat -> toJson(cbor.item)
            is DataItem.Tagged.BigNumUnsigned -> JsonPrimitive(cbor.asNumber())
            is DataItem.Tagged.BigNumNegative -> JsonPrimitive(cbor.asNumber())
            is DataItem.Tagged.DecimalFraction -> TODO("Implement toJson for DecimalFraction")
            is DataItem.Tagged.BigFloat -> TODO("Implement toJson for BigFloat")
            is DataItem.Tagged.EncodedText -> toJson(cbor.item)
            is DataItem.Tagged.DborDataItem -> toJson(decode(cbor.item.value.toByteArray()))
            is DataItem.Tagged.SelfDescribedCbor -> toJson(cbor.item)
            is DataItem.Tagged.Unassigned -> JsonNull
            is DataItem.Tagged.FullDateTime -> toJson(cbor.item)
        }
        is DataItem.Bool -> JsonPrimitive(cbor.value)
        is DataItem.HalfPrecisionFloat -> JsonPrimitive(cbor.value)
        is DataItem.SinglePrecisionFloat -> JsonPrimitive(cbor.value)
        is DataItem.DoublePrecisionFloat -> JsonPrimitive(cbor.value)
        is DataItem.SimpleType.Reserved -> JsonNull
        is DataItem.SimpleType.Unassigned -> JsonNull
        DataItem.Undefined -> JsonNull
        DataItem.Null -> JsonNull
    }
}

private fun DataItem.Tagged.BigNumUnsigned.asNumber(): Number = item.asBigInteger() // TODO this is java specific
private fun DataItem.Tagged.BigNumNegative.asNumber(): Number = - BigInteger.ONE - item.asBigInteger() // TODO this is java specific
private fun DataItem.ByteString.asBigInteger() : BigInteger = BigInteger(value.toByteArray()) // TODO this is java specific