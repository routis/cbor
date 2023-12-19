package io.github.routis.cbor

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
    fun decodeTests() = readTestVectors().map { decodeTest(it) }

    @TestFactory
    fun encodeTests() =
        readTestVectors()
            .filter { it.roundTrip }
            .map { encodeTest(it) }

    private fun encodeTest(tv: TestVector): DynamicTest {
        val dataItem = decode(tv.bytes).also { println(it) }

        return dynamicTest("given a cbor \"${dataItem}\", when  encoded, then it should produce ${tv.hex}") {
            val bytes = encode(dataItem)
            val expected = tv.hex
            val actual = bytes.toHexString()
            assertEquals(expected, actual)
        }
    }

    private fun decodeTest(tv: TestVector): DynamicTest {
        fun DataItem.assertJsonIs(
            expected: JsonElement,
            dataItemJson: JsonElement?,
        ) {
            when (this) {
                is DataItem.SinglePrecisionFloat ->
                    assertEquals(
                        expected.jsonPrimitive.floatOrNull,
                        dataItemJson?.jsonPrimitive?.floatOrNull,
                    )

                is DataItem.DoublePrecisionFloat ->
                    assertEquals(
                        expected.jsonPrimitive.doubleOrNull,
                        dataItemJson?.jsonPrimitive?.doubleOrNull,
                    )

                is DataItem.HalfPrecisionFloat ->
                    assertEquals(
                        expected.jsonPrimitive.floatOrNull,
                        dataItemJson?.jsonPrimitive?.floatOrNull,
                    )

                else -> assertEquals(expected, dataItemJson)
            }
        }

        return dynamicTest("given a \"${tv.hex}\", decode() should produce ${tv.decoded ?: tv.diagnostic}}") {
            val dataItem =
                assertDoesNotThrow("Failed decode") {
                    decode(tv.bytes)
                }.also { println(it) }

            val dataItemJson = assertDoesNotThrow("Failed toJson for ${dataItem.javaClass}") { dataItem.toJson() }
            tv.decoded?.let { elem -> dataItem.assertJsonIs(elem, dataItemJson) }
        }
    }
}

fun readTestVectors(): List<TestVector> =
    FileSystem.RESOURCES.read("appendix_a.json".toPath()) {
        jsonSupport.decodeFromStream<List<TestVector>>(this.inputStream())
    }
