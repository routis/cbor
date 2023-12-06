package io.github.routis.cbor.internal

import io.github.routis.cbor.DataItem
import kotlinx.io.*

/**
 *  @receiver The sink to write to
 */
@Throws(IOException::class)
internal fun Sink.writeDataItem(item: DataItem) {
    when (item) {
        is DataItem.Integer.Unsigned -> writeInitialByteAndUnsignedInteger(MajorType.Zero, item.value)
        is DataItem.Integer.Negative -> writeInitialByteAndUnsignedInteger(MajorType.One, item.value)
        is DataItem.ByteString -> {
            val bytes = item.bytes
            val byteCount = item.bytes.size
            writeInitialByteAndUnsignedInteger(MajorType.Two, byteCount.toULong())
            write(bytes)
        }

        is DataItem.TextString -> {
            val bytes = item.text.encodeToByteArray()
            val byteCount = bytes.size
            writeInitialByteAndUnsignedInteger(MajorType.Three, byteCount.toULong())
            write(bytes)
        }

        is DataItem.Array -> {
            val elementsNo = item.size
            writeInitialByteAndUnsignedInteger(MajorType.Four, elementsNo.toULong())
            item.forEach { element -> writeDataItem(element) }
        }

        is DataItem.CborMap -> {
            val entriesNo = item.size
            writeInitialByteAndUnsignedInteger(MajorType.Five, entriesNo.toULong())
            item.forEach { (key, value) ->
                writeDataItem(key.item)
                writeDataItem(value)
            }
        }

        is DataItem.Tagged<*> -> {
            val tagValue = item.tagValue()
            writeInitialByteAndUnsignedInteger(MajorType.Six, tagValue)
            writeDataItem(item.content)
        }

        is DataItem.Bool -> {
            if (item.value) {
                writeMajorSevenInitialByte(AdditionalInfo.BOOLEAN_TRUE)
            } else {
                writeMajorSevenInitialByte(AdditionalInfo.BOOLEAN_FALSE)
            }
        }

        DataItem.Null -> writeMajorSevenInitialByte(AdditionalInfo.NULL)
        DataItem.Undefined -> writeMajorSevenInitialByte(AdditionalInfo.UNDEFINED)
        is DataItem.Reserved -> {
            writeMajorSevenInitialByte(AdditionalInfo.RESERVED_OR_UNASSIGNED)
            writeUByte(item.value)
        }

        is DataItem.HalfPrecisionFloat -> {
            writeMajorSevenInitialByte(AdditionalInfo.HALF_PRECISION_FLOAT)
            writeShort(halfBitsFromFloat(item.value))
        }

        is DataItem.SinglePrecisionFloat -> {
            writeMajorSevenInitialByte(AdditionalInfo.SINGLE_PRECISION_FLOAT)
            writeFloat(item.value)
        }

        is DataItem.DoublePrecisionFloat -> {
            writeMajorSevenInitialByte(AdditionalInfo.DOUBLE_PRECISION_FLOAT)
            writeDouble(item.value)
        }

        is DataItem.Unassigned -> {
            val (additionalInfo, next) =
                when (item.value) {
                    in 0u..19u -> item.value to null
                    in 28u..30u -> item.value to null
                    else -> AdditionalInfo.RESERVED_OR_UNASSIGNED to item.value.toByte()
                }
            writeMajorSevenInitialByte(additionalInfo)
            next?.let { writeByte(it) }
        }
    }
}

/**
 * Calculates the closest [AdditionalInfo] of the given [value], then writes to the [Sink] the
 * initial byte followed, if needed, by an 1, 2, 4 or 8 byte long value
 *
 * @receiver The sink to write to
 */
private fun Sink.writeInitialByteAndUnsignedInteger(
    majorType: MajorType,
    value: ULong,
) {
    require(majorType != MajorType.Seven) { "Not applicable for Major type 7" }
    require(value >= 0u)
    val additionalInfo = AdditionalInfo.forUnsignedInt(value)
    val initialByte = initialByte(majorType, additionalInfo)
    writeByte(initialByte.toByte())
    when (additionalInfo.value) {
        in AdditionalInfo.ZeroToTwentyThreeRange -> Unit // Do nothing. Value is included in initial byte
        AdditionalInfo.SINGLE_BYTE_UINT -> writeUByte(value.toUByte())
        AdditionalInfo.DOUBLE_BYTE_UINT -> writeUShort(value.toUShort())
        AdditionalInfo.FOUR_BYTE_UINT -> writeUInt(value.toUInt())
        AdditionalInfo.EIGHT_BYTE_UINT -> writeULong(value)
        else -> error("Oops")
    }
}

private fun Sink.writeMajorSevenInitialByte(additionalInfo: UByte) {
    val initialByte = initialByte(MajorType.Seven, AdditionalInfo(additionalInfo))
    writeByte(initialByte.toByte())
}
