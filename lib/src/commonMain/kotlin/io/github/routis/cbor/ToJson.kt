@file:JvmName("JsonConverter")

package io.github.routis.cbor

import kotlinx.serialization.json.*
import kotlin.io.encoding.Base64


typealias KeyMapper<K> = (K) -> String?

data class KeyOptions @JvmOverloads constructor(
        val booKeyMapper: KeyMapper<Key.BoolKey>? = null,
        val byteStringKeyMapper: KeyMapper<Key.ByteStringKey>? = null,
        val integerKeyMapper: KeyMapper<Key.IntegerKey>? = null,
)
typealias ByteStringOption = (ByteArray) -> JsonElement

data class JsonOptions(
        val keyOptions: KeyOptions,
        val byteStringOption: ByteStringOption
) {

    companion object {
        @JvmStatic
        val Base64UrlEncodedBytes: ByteStringOption = { bytes -> JsonPrimitive(Base64.UrlSafe.encode(bytes)) }

        @JvmStatic
        val UseOnlyTextKey: KeyOptions = KeyOptions(booKeyMapper = null, byteStringKeyMapper = null, integerKeyMapper = null)

        @JvmStatic
        val Default = JsonOptions(keyOptions = UseOnlyTextKey, byteStringOption = Base64UrlEncodedBytes)

    }
}
@JvmOverloads
fun DataItem.toJson(options: JsonOptions = JsonOptions.Default): JsonElement {

    fun keyAsString(k: Key<*>): String? {
        val keyOptions = options.keyOptions
        return when (k) {
            is Key.BoolKey -> keyOptions.booKeyMapper?.invoke(k)
            is Key.ByteStringKey -> keyOptions.byteStringKeyMapper?.invoke(k)
            is Key.IntegerKey -> keyOptions.integerKeyMapper?.invoke(k)
            is Key.TextStringKey -> k.item.text
        }
    }

    fun convert(item: DataItem): JsonElement = when (item) {
        is DataItem.Integer.Unsigned -> JsonPrimitive(item.value)
        is DataItem.Integer.Negative -> JsonPrimitive(item.asNumber())
        is DataItem.ByteString -> options.byteStringOption(item.bytes)
        is DataItem.TextString -> JsonPrimitive(item.text)
        is DataItem.Array -> item.map(::convert).let(::JsonArray)
        is DataItem.CborMap -> buildMap {
            item.forEach { (k, v) -> keyAsString(k)?.let { put(it, convert(v)) } }
        }.let(::JsonObject)

        is DataItem.Tagged<*> -> when (item) {
            is DataItem.Tagged.StandardDateTimeString -> convert(item.content)
            is DataItem.Tagged.EpochBasedDateTime.Unsigned -> convert(item.content)
            is DataItem.Tagged.EpochBasedDateTime.Negative -> convert(item.content)
            is DataItem.Tagged.EpochBasedDateTime.HalfFloat -> convert(item.content)
            is DataItem.Tagged.EpochBasedDateTime.SingleFloat -> convert(item.content)
            is DataItem.Tagged.EpochBasedDateTime.DoubleFloat -> convert(item.content)
            is DataItem.Tagged.BigNumUnsigned -> JsonPrimitive(item.asNumber())
            is DataItem.Tagged.BigNumNegative -> JsonPrimitive(item.asNumber())
            is DataItem.Tagged.DecimalFraction -> TODO("Implement toJson for DecimalFraction")
            is DataItem.Tagged.BigFloat -> TODO("Implement toJson for BigFloat")
            is DataItem.Tagged.EncodedText -> convert(item.content)
            is DataItem.Tagged.DborDataItem -> convert(decode(item.content.bytes))
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

    return convert(this)
}