package io.github.routis.cbor

import io.github.routis.cbor.internal.AdditionalInfo
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AdditionalInfoTests {
    @Test
    fun `additionalInfoForUnsignedInt for number 0 to 23 UByte`() {
        val start: UByte = 0u
        val end: UByte = 23u
        (start..end).forEach {
            assertEquals(AdditionalInfo(it.toUByte()), AdditionalInfo.forUnsignedInt(it.toULong()))
        }
    }

    @Test
    fun `additionalInfoForUnsignedInt for number 24 to 255 UByte`() {
        val start: UByte = 24u
        val end: UByte = 255u
        (start..end).forEach {
            assertEquals(AdditionalInfo(AdditionalInfo.SINGLE_BYTE_UINT), AdditionalInfo.forUnsignedInt(it.toULong()))
        }
    }

    @Test
    fun `additionalInfoForUnsignedInt for number 0 to 23 UShort`() {
        val start: UShort = 0u
        val end: UShort = 23u
        (start..end).forEach {
            assertEquals(AdditionalInfo(it.toUByte()), AdditionalInfo.forUnsignedInt(it.toULong()))
        }
    }

    @Test
    fun `additionalInfoForUnsignedInt for number 24 to 255 UShort`() {
        val start: UShort = 24u
        val end: UShort = 255u
        (start..end).forEach {
            assertEquals(AdditionalInfo(AdditionalInfo.SINGLE_BYTE_UINT), AdditionalInfo.forUnsignedInt(it.toULong()))
        }
    }

    @Test
    fun `additionalInfoForUnsignedInt for number 256 to 65535 UShort`() {
        val start: UShort = 256u
        val end: UShort = 65535u
        (start..end).forEach {
            assertEquals(AdditionalInfo(AdditionalInfo.DOUBLE_BYTE_UINT), AdditionalInfo.forUnsignedInt(it.toULong()))
        }
    }

    @Test
    fun `additionalInfoForUnsignedInt for number 0 to 23 UInt`() {
        val start: UInt = 0u
        val end: UInt = 23u
        (start..end).forEach {
            assertEquals(AdditionalInfo(it.toUByte()), AdditionalInfo.forUnsignedInt(it.toULong()))
        }
    }

    @Test
    fun `additionalInfoForUnsignedInt for number 24 to 255 UInt`() {
        val start: UInt = 24u
        val end: UInt = 255u
        (start..end).forEach {
            assertEquals(AdditionalInfo(AdditionalInfo.SINGLE_BYTE_UINT), AdditionalInfo.forUnsignedInt(it.toULong()))
        }
    }

    @Test
    fun `additionalInfoForUnsignedInt for number 256 to 65535 UInt`() {
        val start: UInt = 256u
        val end: UInt = 65535u
        (start..end).forEach {
            assertEquals(AdditionalInfo(AdditionalInfo.DOUBLE_BYTE_UINT), AdditionalInfo.forUnsignedInt(it.toULong()))
        }
    }

    @Test
    fun `additionalInfoForUnsignedInt for number 0 to 23 ULong`() {
        val start: ULong = 0u
        val end: ULong = 23u
        (start..end).forEach {
            assertEquals(AdditionalInfo(it.toUByte()), AdditionalInfo.forUnsignedInt(it))
        }
    }

    @Test
    fun `additionalInfoForUnsignedInt for number 24 to 255 ULong`() {
        val start: ULong = 24u
        val end: ULong = 255u
        (start..end).forEach {
            assertEquals(AdditionalInfo(AdditionalInfo.SINGLE_BYTE_UINT), AdditionalInfo.forUnsignedInt(it))
        }
    }

    @Test
    fun `additionalInfoForUnsignedInt for number 256 to 65535 ULong`() {
        val start: ULong = 256u
        val end: ULong = 65535u
        (start..end).forEach {
            assertEquals(AdditionalInfo(AdditionalInfo.DOUBLE_BYTE_UINT), AdditionalInfo.forUnsignedInt(it.toULong()))
        }
    }
}
