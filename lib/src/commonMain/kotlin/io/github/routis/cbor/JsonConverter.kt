@file:JvmName("JsonConverter")

package io.github.routis.cbor

import io.github.routis.cbor.internal.dataItemToJson
import kotlinx.serialization.json.*
import kotlin.io.encoding.Base64
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

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

/**
 * Converts the a [DataItem] into JSON using the provided [options]
 * @receiver the item to convert
 */
@JvmOverloads
fun DataItem.toJson(options: JsonOptions = JsonOptions.Default): JsonElement = dataItemToJson(this, options)
