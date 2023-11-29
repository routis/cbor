package io.github.routis.cbor

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import okio.FileSystem
import okio.Path.Companion.toPath
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals


class CborSpecTests {
    @TestFactory
    fun appendixATests() = readTestVectors().map { createTestFor(it) }


    private fun createTestFor(tv: TestVector): DynamicTest {

        fun DataItem.assertJsonIs(expected: JsonElement, dataItemJson: JsonElement?) {

            when (this) {
                is DataItem.SinglePrecisionFloat -> assertEquals(expected.jsonPrimitive.floatOrNull, dataItemJson?.jsonPrimitive?.floatOrNull)
                is DataItem.DoublePrecisionFloat -> assertEquals(expected.jsonPrimitive.doubleOrNull, dataItemJson?.jsonPrimitive?.doubleOrNull)
                is DataItem.HalfPrecisionFloat -> assertEquals(expected.jsonPrimitive.floatOrNull, dataItemJson?.jsonPrimitive?.floatOrNull)
                else -> assertEquals(expected, dataItemJson)
            }

        }

        return dynamicTest("given a cbor \"${tv.hex}\", when decoding, then it should produce a dataitem  ${tv.decoded ?: tv.diagnostic}}") {
            val dataItem = assertDoesNotThrow("Failed decode") {
                decode(tv.bytes)
            }.also { println(it) }

            val dataItemJson = assertDoesNotThrow("Failed toJson for ${dataItem.javaClass}") { toJson(dataItem) }
            tv.decoded?.let { elem -> dataItem.assertJsonIs(elem, dataItemJson) }

        }
    }

}
val jsonSupport = Json {
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
        val diagnostic: String? = null
) {
    val bytes: ByteArray by lazy {
        hex.hexToByteArray(HexFormat.Default)
    }
}

fun readTestVectors(): List<TestVector> = FileSystem.RESOURCES.read("appendix_a.json".toPath()) {
    jsonSupport.decodeFromStream<List<TestVector>>(this.inputStream())
}