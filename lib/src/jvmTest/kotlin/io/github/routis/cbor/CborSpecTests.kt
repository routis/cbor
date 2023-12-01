package io.github.routis.cbor

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.jsonPrimitive
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
                is DataItem.SinglePrecisionFloat -> assertEquals(
                    expected.jsonPrimitive.floatOrNull,
                    dataItemJson?.jsonPrimitive?.floatOrNull
                )

                is DataItem.DoublePrecisionFloat -> assertEquals(
                    expected.jsonPrimitive.doubleOrNull,
                    dataItemJson?.jsonPrimitive?.doubleOrNull
                )

                is DataItem.HalfPrecisionFloat -> assertEquals(
                    expected.jsonPrimitive.floatOrNull,
                    dataItemJson?.jsonPrimitive?.floatOrNull
                )

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


