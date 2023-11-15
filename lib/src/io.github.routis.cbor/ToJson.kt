package io.github.routis.cbor

import kotlinx.serialization.json.*
import kotlin.io.encoding.Base64


fun toJson(cbor: DataItem): JsonElement? {
    return when (cbor) {
        DataItem.Break -> null
        is DataItem.SimpleType.Reserved -> null
        is DataItem.SimpleType.Unassigned -> null
        DataItem.Undefined -> null
        DataItem.Null -> JsonNull

        is DataItem.Bool -> JsonPrimitive(cbor.value)
        is DataItem.Float32 -> JsonPrimitive(cbor.value)
        is DataItem.Float64 -> JsonPrimitive(cbor.value)
        is DataItem.NegativeInteger -> JsonPrimitive(cbor.value)
        is DataItem.PositiveInteger -> JsonPrimitive(cbor.value)
        is DataItem.Text -> JsonPrimitive(cbor.value)

        is DataItem.Bytes -> JsonPrimitive(Base64.UrlSafe.encode(cbor.value.toByteArray()))
        is DataItem.Tagged<*> -> when (cbor) {
            is DataItem.Tagged.Base64 -> toJson(cbor.value)
            is DataItem.Tagged.Base64Url -> toJson(cbor.value)
            is DataItem.Tagged.Cbor -> toJson(decode(cbor.value.value.toByteArray()))
            is DataItem.Tagged.DateTime -> toJson(cbor.value)
            is DataItem.Tagged.Mime -> toJson(cbor.value)
            is DataItem.Tagged.Unassigned -> null
            is DataItem.Tagged.Regex -> toJson(cbor.value)
            is DataItem.Tagged.Uri -> toJson(cbor.value)
            is DataItem.Tagged.FullDateTime -> toJson(cbor.value)
        }

        is DataItem.CborArray -> cbor.mapNotNull { toJson(it) }.let(::JsonArray)
        is DataItem.CborMap -> {
            return cbor.mapNotNull { (k, v) ->
                val jsonKey = when (k) {
                    is Key.BoolKey -> k.value.value.toString()
                    is Key.BytesKey -> Base64.UrlSafe.encode(k.value.value.toByteArray())
                    is Key.NegIntKey -> k.value.value.toString()
                    is Key.PosIntKey -> k.value.value.toString()
                    is Key.TextKey -> k.value.value
                }
                val jsonValue = toJson(v) ?: JsonNull
                jsonKey to jsonValue
            }.toMap().let(::JsonObject)
        }
    }
}