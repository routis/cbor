package io.github.routis.cbor

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

val jsonSupport =
    Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

// https://github.com/cbor/test-vectors/blob/master/appendix_a.json

@Serializable
data class TestVector(
    val cbor: String,
    val hex: String,
    @SerialName("roundtrip") val roundTrip: Boolean,
    val decoded: JsonElement? = null,
    val diagnostic: String? = null,
) {
    val bytes: ByteArray by lazy {
        hex.hexToByteArray(HexFormat.Default)
    }
}

