package io.github.routis.cbor

import io.github.routis.cbor.internal.AdditionalInfo
import kotlin.test.Test
import kotlin.test.assertEquals

class AdditionalInfoTests {
    @Test
    fun additionalInfoForUnsignedInt_for_number_0_to_23_UByte() {
        val start: UByte = 0u
        val end: UByte = 23u
        (start..end).forEach {
            assertEquals(AdditionalInfo(it.toUByte()), AdditionalInfo.forUnsignedInt(it.toULong()))
        }
    }

    @Test
    fun additionalInfoForUnsignedInt_for_number_24_to_2255_UByte() {
        val start: UByte = 24u
        val end: UByte = 255u
        (start..end).forEach {
            assertEquals(AdditionalInfo(AdditionalInfo.SINGLE_BYTE_UINT), AdditionalInfo.forUnsignedInt(it.toULong()))
        }
    }

    @Test
    fun additionalInfoForUnsignedInt_for_number_0_to_23_UShort() {
        val start: UShort = 0u
        val end: UShort = 23u
        (start..end).forEach {
            assertEquals(AdditionalInfo(it.toUByte()), AdditionalInfo.forUnsignedInt(it.toULong()))
        }
    }

    @Test
    fun additionalInfoForUnsignedInt_for_number_24_to_2255_UShort() {
        val start: UShort = 24u
        val end: UShort = 255u
        (start..end).forEach {
            assertEquals(AdditionalInfo(AdditionalInfo.SINGLE_BYTE_UINT), AdditionalInfo.forUnsignedInt(it.toULong()))
        }
    }

    @Test
    fun additionalInfoForUnsignedInt_for_number_2256_to_65535_UByte() {
        val start: UShort = 256u
        val end: UShort = 65535u
        (start..end).forEach {
            assertEquals(AdditionalInfo(AdditionalInfo.DOUBLE_BYTE_UINT), AdditionalInfo.forUnsignedInt(it.toULong()))
        }
    }

    @Test
    fun additionalInfoForUnsignedInt_for_number_0_to_23_UInt() {
        val start = 0u
        val end = 23u
        (start..end).forEach {
            assertEquals(AdditionalInfo(it.toUByte()), AdditionalInfo.forUnsignedInt(it.toULong()))
        }
    }

    @Test
    fun additionalInfoForUnsignedInt_for_number_24_to_2255_UInt() {
        val start = 24u
        val end = 255u
        (start..end).forEach {
            assertEquals(AdditionalInfo(AdditionalInfo.SINGLE_BYTE_UINT), AdditionalInfo.forUnsignedInt(it.toULong()))
        }
    }

    @Test
    fun additionalInfoForUnsignedInt_for_number_2256_to_65535_UInt() {
        val start = 256u
        val end = 65535u
        (start..end).forEach {
            assertEquals(AdditionalInfo(AdditionalInfo.DOUBLE_BYTE_UINT), AdditionalInfo.forUnsignedInt(it.toULong()))
        }
    }

    @Test
    fun additionalInfoForUnsignedInt_for_number_0_to_23_ULong() {
        val start: ULong = 0u
        val end: ULong = 23u
        (start..end).forEach {
            assertEquals(AdditionalInfo(it.toUByte()), AdditionalInfo.forUnsignedInt(it))
        }
    }

    @Test
    fun additionalInfoForUnsignedInt_for_number_24_to_2255_ULong() {
        val start: ULong = 24u
        val end: ULong = 255u
        (start..end).forEach {
            assertEquals(AdditionalInfo(AdditionalInfo.SINGLE_BYTE_UINT), AdditionalInfo.forUnsignedInt(it))
        }
    }

    @Test
    fun additionalInfoForUnsignedInt_for_number_2256_to_65535_ULong() {
        val start: ULong = 256u
        val end: ULong = 65535u
        (start..end).forEach {
            assertEquals(AdditionalInfo(AdditionalInfo.DOUBLE_BYTE_UINT), AdditionalInfo.forUnsignedInt(it))
        }
    }
}
