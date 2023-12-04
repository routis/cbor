package io.github.routis.cbor.internal

import io.github.routis.cbor.DataItem
import kotlinx.io.Sink
import kotlinx.io.writeDouble
import kotlinx.io.writeFloat


internal fun Sink.writeDataItem(item: DataItem) {
    when (item) {
        is DataItem.Integer.Unsigned -> writeInitialByteAndSize(MajorType.Zero, item.value)
        is DataItem.Integer.Negative -> writeInitialByteAndSize(MajorType.One, item.value)
        is DataItem.ByteString -> {
            val bytes = item.bytes
            val byteCount = item.bytes.size.toULong()
            writeInitialByteAndSize(MajorType.Two, byteCount)
            write(bytes)
        }

        is DataItem.TextString -> {
            val bytes = item.text.encodeToByteArray()
            val byteCount = bytes.size.toULong()
            writeInitialByteAndSize(MajorType.Three, byteCount)
            write(bytes)
        }

        is DataItem.Array -> {
            val elementsNo = item.size.toULong()
            writeInitialByteAndSize(MajorType.Four, elementsNo)
            item.forEach { element -> writeDataItem(element) }
        }

        is DataItem.CborMap -> {
            val entriesNo = item.size.toULong()
            writeInitialByteAndSize(MajorType.Five, entriesNo)
            item.forEach { (key, value) ->
                writeDataItem(key.item)
                writeDataItem(value)
            }
        }

        is DataItem.Tagged<*> -> {
            val tagValue = item.tagValue()
            writeInitialByteAndSize(MajorType.Six, tagValue)
            writeDataItem(item.content)
        }

        is DataItem.Bool -> {
            val additionalInfo = when {
                !item.value -> AdditionalInfo(20u)
                else -> AdditionalInfo(21u)
            }
            writeInitialByte(MajorType.Seven, additionalInfo)
        }

        DataItem.Null -> writeInitialByte(MajorType.Seven, AdditionalInfo(22u))
        DataItem.Undefined -> writeInitialByte(MajorType.Seven, AdditionalInfo(23u))
        is DataItem.Reserved -> {
            writeInitialByte(MajorType.Seven, AdditionalInfo(24u))
            writeByte(item.value.toByte())
        }

        is DataItem.HalfPrecisionFloat -> {
            writeInitialByte(MajorType.Seven, AdditionalInfo(25u))
            writeShort(halfBitsFromFloat(item.value))
        }

        is DataItem.SinglePrecisionFloat -> {
            writeInitialByte(MajorType.Seven, AdditionalInfo(26u))
            writeFloat(item.value)
        }

        is DataItem.DoublePrecisionFloat -> {
            writeInitialByte(MajorType.Seven, AdditionalInfo(27u))
            writeDouble(item.value)
        }

        is DataItem.Unassigned -> {
            val (additionalInfo, next) = when (item.value) {
                in 0u..19u -> AdditionalInfo(item.value) to null
                in 28u..30u -> AdditionalInfo(item.value) to null
                else -> AdditionalInfo(24u) to item.value.toByte()
            }
            writeInitialByte(MajorType.Seven, additionalInfo)
            next?.let { writeByte(it) }
        }
    }
}

/**
 * Calculates the closest [AdditionalInfo] of the given [size]
 */
private fun Sink.writeInitialByteAndSize(majorType: MajorType, size: ULong) {
    val additionalInfo = AdditionalInfo.forSize(size)
    writeInitialByte(majorType, additionalInfo)
    when (additionalInfo.value.toInt()) {
        in 0..23 -> Unit
        24 -> writeByte(size.toUByte().toByte())
        25 -> writeShort(size.toUShort().toShort())
        26 -> writeInt(size.toUInt().toInt())
        27 -> writeLong(size.toLong())
        else -> error("Oops")
    }
}

/**
 * [Calculates][initialByte] the initial byte using the given [majorType] & [additionalInfo]
 * and writes it to the [Sink]
 * @receiver the sink to write to the initial bye
 */
private fun Sink.writeInitialByte(majorType: MajorType, additionalInfo: AdditionalInfo) {
    val initialByte = initialByte(majorType, additionalInfo)
    writeByte(initialByte.toByte())
}



