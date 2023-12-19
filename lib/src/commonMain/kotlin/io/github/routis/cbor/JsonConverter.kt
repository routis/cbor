@file:JvmName("JsonConverter")

package io.github.routis.cbor

import io.github.routis.cbor.internal.dataItemToJson
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.io.encoding.Base64

typealias KeyMapper<K> = (K) -> String?

/**
 * Options on if and how to serialize a [Key] other than [TextStringKey],
 * into a Json attribute name.
 * One may choose to not map into Json other keys (besides [TextStringKey])
 * to make sure that there is no key collision in JSON.
 *
 * @param boolKeyMapper how to include [BoolKey]
 * @param byteStringKeyMapper how to include [ByteStringKey]
 * @param integerKeyMapper how to include [IntegerKey]
 */
data class KeyOptions
    @JvmOverloads
    constructor(
        val boolKeyMapper: KeyMapper<BoolKey>? = null,
        val byteStringKeyMapper: KeyMapper<ByteStringKey>? = null,
        val integerKeyMapper: KeyMapper<IntegerKey>? = null,
    )

/**
 * A function that serializes a [ByteStringDataItem] into a [JsonElement]
 */
typealias ByteStringOption = (ByteStringDataItem) -> JsonElement

/**
 * Options for converting a [DataItem] into Json
 *
 * @param keyOptions options about [keys][Key] of [maps][MapDataItem]
 * @param byteStringOption how to serialize into JSON a [ByteStringDataItem]
 */
data class JsonOptions(
    val keyOptions: KeyOptions,
    val byteStringOption: ByteStringOption,
) {
    companion object {
        /**
         * Serializes a [ByteStringDataItem] into a base64 url encoded string
         */
        @JvmStatic
        val Base64UrlEncodedBytes: ByteStringOption =
            { byteString -> JsonPrimitive(Base64.UrlSafe.encode(byteString.bytes)) }

        /**
         * Choice to use only [text keys][TextStringKey]
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
fun DataItem.toJson(options: JsonOptions = JsonOptions.Default): JsonElement = with(options) { dataItemToJson(this@toJson) }
