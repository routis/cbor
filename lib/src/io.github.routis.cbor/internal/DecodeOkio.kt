package io.github.routis.cbor.internal

import io.github.routis.cbor.DataItem
import io.github.routis.cbor.Key
import okio.BufferedSource
import kotlin.contracts.contract


private sealed interface DataItemOrBreak {

    @JvmInline
    value class Item(val item: DataItem) : DataItemOrBreak
    data object Break : DataItemOrBreak

    companion object {
        private const val BRAKE_BYTE: UByte = 0b111_11111u
        fun isBreak(uByte: UByte): Boolean = BRAKE_BYTE == uByte
    }
}

private fun DataItem.orBreak() = DataItemOrBreak.Item(this)

internal fun BufferedSource.readDataItem(): DataItem {
    val dataItemOrBreak = readDataItemOrBreak()
    check(dataItemOrBreak is DataItemOrBreak.Item) { "Unexpected break" }
    return dataItemOrBreak.item
}

private fun BufferedSource.readDataItemOrBreak(): DataItemOrBreak {

    val initialByte = readUByte()
    val additionalInfo = AdditionalInfo(initialByte)

    fun readSizeThen(ifIndefinite: () -> DataItem, ifDefinite: (ULong) -> DataItem): DataItem {
        return when (val byteCount = readSize(additionalInfo)) {
            Size.Indefinite -> ifIndefinite()
            is Size.Definite -> ifDefinite(byteCount.size)
        }
    }
    return when (MajorType(initialByte)) {
        MajorType.Zero -> readUnsigntInteger(additionalInfo).orBreak()
        MajorType.One -> readNegativeInteger(additionalInfo).orBreak()
        MajorType.Two -> readSizeThen(ifIndefinite = ::readBytesStringUntilBreak, ifDefinite = ::readByteString).orBreak()
        MajorType.Three -> readSizeThen(ifIndefinite = ::readTextStringUntilBreak, ifDefinite = ::readTextString).orBreak()
        MajorType.Four -> readSizeThen(ifIndefinite = ::readArrayUntilBreak, ifDefinite = ::readArray).orBreak()
        MajorType.Five -> readSizeThen(ifIndefinite = ::readMapUntilBreak, ifDefinite = ::readMap).orBreak()
        MajorType.Six -> readTagged(additionalInfo).orBreak()
        MajorType.Seven -> readMajorSeven(additionalInfo)
    }
}


private fun BufferedSource.readUnsigntInteger(additionalInfo: AdditionalInfo): DataItem.UnsignedInteger {
    val value = readUnsignedInt(additionalInfo)
    return DataItem.UnsignedInteger(value)
}

private fun BufferedSource.readNegativeInteger(additionalInfo: AdditionalInfo): DataItem.NegativeInteger {
    val value = readUnsignedInt(additionalInfo)
    return DataItem.NegativeInteger(-1 - value.toLong())
}

private fun BufferedSource.readBytesStringUntilBreak(): DataItem.ByteString {
    val bytes = buildList {
        do {
            val byte = readUByte()
            val isBreak = DataItemOrBreak.isBreak(byte)
            if (!isBreak) add(byte)
        } while (!isBreak)
    }.toUByteArray()
    return DataItem.ByteString(bytes)
}

private fun BufferedSource.readByteString(byteCount: ULong): DataItem.ByteString {
    val bytes = buildList {
        repeat(byteCount) { add(readUByte()) }
    }.toUByteArray()
    return DataItem.ByteString(bytes)
}

private fun BufferedSource.readTextStringUntilBreak(): DataItem.TextString {

    val bytes = buildList {
        do {
            val byte = readByte()
            val isBreak = DataItemOrBreak.isBreak(byte.toUByte())
            byte.takeUnless { isBreak }?.let(::add)
        } while (!isBreak)
    }.toByteArray()

    return DataItem.TextString(String(bytes))
}

private fun BufferedSource.readTextString(byteCount: ULong): DataItem.TextString {
    require(byteCount <= Long.MAX_VALUE.toULong()) { "Too big" }
    return DataItem.TextString(readUtf8(byteCount.toLong()))
}


private fun BufferedSource.readArrayUntilBreak(): DataItem.Array {
    val items = buildList {
        do {
            val dataItemOrBreak = readDataItemOrBreak()
            if (dataItemOrBreak is DataItemOrBreak.Item) {
                add(dataItemOrBreak.item)
            }
        } while (dataItemOrBreak !is DataItemOrBreak.Break)
    }
    return DataItem.Array(items)
}

private fun BufferedSource.readArray(itemsNo: ULong): DataItem.Array {
    val items = buildList {
        repeat(itemsNo) { add(readDataItem()) }
    }
    return DataItem.Array(items)
}


private fun BufferedSource.readMapUntilBreak(): DataItem.CborMap {
    val items = buildMap {
        do {
            val keyOrBreak = readDataItemOrBreak()
            if (keyOrBreak is DataItemOrBreak.Item) {
                val key = Key(keyOrBreak.item).getOrThrow()
                check(!contains(key)) { "Duplicate $key" }
                val value = readDataItem()
                put(key, value)
            }
        } while (keyOrBreak !is DataItemOrBreak.Break)
    }
    return DataItem.CborMap(items)
}


private fun BufferedSource.readMap(itemsNo: ULong): DataItem.CborMap {
    val items = buildMap {
        repeat(itemsNo) {
            val key = Key(readDataItem()).getOrThrow()
            check(!contains(key)) { "Duplicate $key" }
            val value = readDataItem()
            put(key, value)
        }
    }
    return DataItem.CborMap(items)
}


private fun BufferedSource.readTagged(additionalInfo: AdditionalInfo): DataItem.Tagged<*> {
    val tag = readUnsignedInt(additionalInfo)
    val dataItem = readDataItem()
    return when (val supportedTag = Tag.of(tag)) {
        null -> DataItem.Tagged.Unassigned(tag, dataItem)
        else -> supportedTag.item(dataItem)
    }

}

private fun BufferedSource.readMajorSeven(additionalInfo: AdditionalInfo): DataItemOrBreak =
        when (additionalInfo.value.toInt()) {
            in 0..19 -> DataItem.SimpleType.Unassigned(additionalInfo.value).orBreak()
            20 -> DataItem.Bool(false).orBreak()
            21 -> DataItem.Bool(true).orBreak()
            22 -> DataItem.Null.orBreak()
            23 -> DataItem.Undefined.orBreak()
            24 -> when (val next = readUByte()) {
                in 0.toUByte()..31.toUByte() -> DataItem.SimpleType.Reserved(next)
                else -> DataItem.SimpleType.Unassigned(next)
            }.orBreak()

            25 -> DataItem.HalfPrecisionFloat(floatFromHalfBits(readShort())).orBreak()
            26 -> DataItem.SinglePrecisionFloat(Float.fromBits(readInt())).orBreak()
            27 -> DataItem.DoublePrecisionFloat(Double.fromBits(readLong())).orBreak()
            in 28..30 -> DataItem.SimpleType.Unassigned(additionalInfo.value).orBreak()
            31 -> DataItemOrBreak.Break
            else -> error("Not supported")
        }


private sealed interface Size {
    data object Indefinite : Size
    data class Definite(val size: ULong) : Size
}

private const val indefiniteLengthIndicator: UByte = 31u

private fun BufferedSource.readSize(additionalInfo: AdditionalInfo): Size =
        if (additionalInfo.value == indefiniteLengthIndicator) Size.Indefinite
        else Size.Definite(readUnsignedInt(additionalInfo))


/**
 * Reads from the [BufferedSource] an unsinged integer, using the [additionalInfo] as follows:
 * - if [additionalInfo] is less than 24, the result is the additional info itself
 * - if [additionalInfo] is 24, 25, 26 or 27 The argument's value is held in the following 1, 2, 4, or 8 bytes,
 *   respectively, in network byte order
 *
 * @param additionalInfo the instuction on how to read the unsigned integer
 * @receiver The [BufferedSource] to read from
 * @return the unsigned intger. Regardless of the case this always a [ULong]
 */
private fun BufferedSource.readUnsignedInt(additionalInfo: AdditionalInfo): ULong {

    return when (additionalInfo.value.toInt()) {
        in 0..<24 -> additionalInfo.value.toULong()
        24 -> readUByte().toULong()
        25 -> readUShort().toULong()
        26 -> readUInt().toULong()
        27 -> readULong()
        else -> error("Cannot use ${additionalInfo.value} for reading unsigned. Valid values are in 0..27 inclusive")
    }.toULong()
}

private fun BufferedSource.readUByte(): UByte = readByte().toUByte()
private fun BufferedSource.readUShort(): UShort = readShort().toUShort()
private fun BufferedSource.readUInt(): UInt = readInt().toUInt()
private fun BufferedSource.readULong(): ULong = readLong().toULong()
private inline fun repeat(times: ULong, action: (ULong) -> Unit) {
    contract { callsInPlace(action) }
    for (index in 0uL until times) {
        action(index)
    }
}