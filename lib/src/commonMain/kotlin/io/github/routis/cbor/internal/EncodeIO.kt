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
            item.forEach { writeDataItem(it) }
        }

        is DataItem.CborMap -> {
            val keyValuePairsNo = item.size.toULong()
            writeInitialByteAndSize(MajorType.Five, keyValuePairsNo)
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

        is DataItem.Bool ->
            if (!item.value) writeInitialByte(MajorType.Seven, AdditionalInfo(20u))
            else writeInitialByte(MajorType.Seven, AdditionalInfo(21u))

        DataItem.Null -> writeInitialByte(MajorType.Seven, AdditionalInfo(22u))
        DataItem.Undefined -> writeInitialByte(MajorType.Seven, AdditionalInfo(23u))
        is DataItem.Reserved -> {
            writeInitialByte(MajorType.Seven, AdditionalInfo(24u))
            writeByte(item.value.toByte())
        }

        is DataItem.HalfPrecisionFloat -> {
            writeInitialByte(MajorType.Seven, AdditionalInfo(25u))
            writeShort(fromFullPrecision(item.value))
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

private fun Sink.writeInitialByteAndSize(majorType: MajorType, size: ULong) {
    val additionalInfo = when (size) {
        in 0uL..<24uL -> AdditionalInfo(size.toUByte())
        in 24uL..UByte.MAX_VALUE.toULong() -> AdditionalInfo(24u)
        in (UByte.MAX_VALUE + 1u).toULong()..UShort.MAX_VALUE.toULong() -> AdditionalInfo(25u)
        in (UShort.MAX_VALUE + 1u).toULong()..UInt.MAX_VALUE.toULong() -> AdditionalInfo(26u)
        in (UInt.MAX_VALUE + 1u).toULong()..ULong.MAX_VALUE -> AdditionalInfo(27u)
        else -> error("Invalid value")
    }
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

private fun Sink.writeInitialByte(majorType: MajorType, additionalInfo: AdditionalInfo) {
    val initialByte = initialByte(majorType, additionalInfo)
    writeByte(initialByte)
}



