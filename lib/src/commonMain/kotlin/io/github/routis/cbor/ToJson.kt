@file:JvmName("JsonConverter")

package io.github.routis.cbor

import kotlinx.serialization.json.*
import kotlin.io.encoding.Base64

typealias KeyMapper<K> = (K) -> String?

/**
 * Options on if and how to serialize a [Key] other than [Key.TextStringKey],
 * into a Json attribute name.
 * One may choose to not map into Json other keys (besides [Key.TextStringKey])
 * to make sure that there is no key collision in JSON.
 *
 * @param boolKeyMapper how to include [Key.BoolKey]
 * @param byteStringKeyMapper how to include [Key.ByteStringKey]
 * @param integerKeyMapper how to include [Key.IntegerKey]
 */
data class KeyOptions
    @JvmOverloads
    constructor(
        val boolKeyMapper: KeyMapper<Key.BoolKey>? = null,
        val byteStringKeyMapper: KeyMapper<Key.ByteStringKey>? = null,
        val integerKeyMapper: KeyMapper<Key.IntegerKey>? = null,
    )

/**
 * A function that serializes a [DataItem.ByteString] into a [JsonElement]
 */
typealias ByteStringOption = (DataItem.ByteString) -> JsonElement

/**
 * Options for converting a [DataItem] into Json
 *
 * @param keyOptions options about [keys][Key] of [maps][DataItem.CborMap]
 * @param byteStringOption how to serialize into JSON a [DataItem.ByteString]
 */
data class JsonOptions(
    val keyOptions: KeyOptions,
    val byteStringOption: ByteStringOption,
) {
    companion object {
        /**
         * Serializes a [DataItem.ByteString] into a base64 url encoded string
         */
        @JvmStatic
        val Base64UrlEncodedBytes: ByteStringOption =
            { byteString -> JsonPrimitive(Base64.UrlSafe.encode(byteString.bytes)) }

        /**
         * Choice to use only [text keys][Key.TextStringKey]
         */
        @JvmStatic
        val UseOnlyTextKey: KeyOptions =
            KeyOptions(boolKeyMapper = null, byteStringKeyMapper = null, integerKeyMapper = null)

        /**
         * Default convention options.
         * Uses [UseOnlyTextKey] and [Base64UrlEncodedBytes]
         */
        @JvmStatic
        val Default = JsonOptions(keyOptions = UseOnlyTextKey, byteStringOption = Base64UrlEncodedBytes)
    }
}

@JvmOverloads
fun DataItem.toJson(options: JsonOptions = JsonOptions.Default): JsonElement {
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
            is DataItem.Integer.Negative -> JsonPrimitive(item.asNumber())
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
                    is DataItem.Tagged.BigNumUnsigned -> JsonPrimitive(item.asNumber())
                    is DataItem.Tagged.BigNumNegative -> JsonPrimitive(item.asNumber())
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

    return convert(this)
}
