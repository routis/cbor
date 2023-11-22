package io.github.routis.cbor

import kotlinx.serialization.json.*
import java.math.BigInteger
import kotlin.io.encoding.Base64


typealias KeyMapper<K> = (K) -> String?

data class KeyOptions(
        val booKeyMapper: KeyMapper<Key.BoolKey>? = null,
        val byteStringKeyMapper: KeyMapper<Key.ByteStringKey>? = null,
        val integerKeyMapper: KeyMapper<Key.IntegerKey>? = null,

) {
    companion object {
        val UseOnlyTextKey: KeyOptions = KeyOptions(
                booKeyMapper = null,
                byteStringKeyMapper = null,
                integerKeyMapper = null,

        )
    }
}

data class ToJsonOptions(
        val keyOptions: KeyOptions
) {
    companion object {
        val Default = ToJsonOptions(keyOptions = KeyOptions.UseOnlyTextKey)
    }
}

fun toJson(cbor: DataItem, options: ToJsonOptions = ToJsonOptions.Default): JsonElement {
    return when (cbor) {
        is DataItem.Integer.Unsigned -> JsonPrimitive(cbor.value)
        is DataItem.Integer.Negative -> JsonPrimitive(cbor.value)
        is DataItem.ByteString -> JsonPrimitive(Base64.UrlSafe.encode(cbor.bytes))
        is DataItem.TextString -> JsonPrimitive(cbor.text)
        is DataItem.Array -> cbor.mapNotNull { toJson(it, options) }.let(::JsonArray)
        is DataItem.CborMap -> {
            val keyOptions = options.keyOptions
            return cbor.mapNotNull { (k, v) ->

                val jsonKey = when (k) {
                    is Key.BoolKey -> keyOptions.booKeyMapper?.invoke(k)
                    is Key.ByteStringKey -> keyOptions.byteStringKeyMapper?.invoke(k)
                    is Key.IntegerKey -> keyOptions.integerKeyMapper?.invoke(k)
                    is Key.TextStringKey -> k.item.text
                }
                
                if (jsonKey != null) jsonKey to toJson(v, options)
                else null

            }.toMap().let(::JsonObject)
        }

        is DataItem.Tagged<*> -> when (cbor) {
            is DataItem.Tagged.StandardDateTimeString -> toJson(cbor.content, options)
            is DataItem.Tagged.EpochBasedDateTime.Unsigned -> toJson(cbor.content, options)
            is DataItem.Tagged.EpochBasedDateTime.Negative -> toJson(cbor.content, options)
            is DataItem.Tagged.EpochBasedDateTime.HalfFloat -> toJson(cbor.content, options)
            is DataItem.Tagged.EpochBasedDateTime.SingleFloat -> toJson(cbor.content, options)
            is DataItem.Tagged.EpochBasedDateTime.DoubleFloat -> toJson(cbor.content, options)
            is DataItem.Tagged.BigNumUnsigned -> JsonPrimitive(cbor.asNumber())
            is DataItem.Tagged.BigNumNegative -> JsonPrimitive(cbor.asNumber())
            is DataItem.Tagged.DecimalFraction -> TODO("Implement toJson for DecimalFraction")
            is DataItem.Tagged.BigFloat -> TODO("Implement toJson for BigFloat")
            is DataItem.Tagged.EncodedText -> toJson(cbor.content, options)
            is DataItem.Tagged.DborDataItem -> toJson(decode(cbor.content.bytes), options)
            is DataItem.Tagged.SelfDescribedCbor -> toJson(cbor.content, options)
            is DataItem.Tagged.Unsupported -> JsonNull
            is DataItem.Tagged.FullDateTime -> toJson(cbor.content, options)
        }

        is DataItem.Bool -> JsonPrimitive(cbor.value)
        is DataItem.HalfPrecisionFloat -> JsonPrimitive(cbor.value)
        is DataItem.SinglePrecisionFloat -> JsonPrimitive(cbor.value)
        is DataItem.DoublePrecisionFloat -> JsonPrimitive(cbor.value)
        is DataItem.Reserved -> JsonNull
        is DataItem.Unassigned -> JsonNull
        DataItem.Undefined -> JsonNull
        DataItem.Null -> JsonNull
    }
}

private fun DataItem.Tagged.BigNumUnsigned.asNumber(): Number = content.asBigInteger() // TODO this is java specific
private fun DataItem.Tagged.BigNumNegative.asNumber(): Number = -BigInteger.ONE - content.asBigInteger() // TODO this is java specific
private fun DataItem.ByteString.asBigInteger(): BigInteger = BigInteger(bytes) // TODO this is java specific