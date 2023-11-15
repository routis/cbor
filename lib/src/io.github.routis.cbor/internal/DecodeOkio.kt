package io.github.routis.cbor.internal

import io.github.routis.cbor.*
import okio.BufferedSource
import kotlin.contracts.contract


internal fun BufferedSource.readCbor(): DataItem {

    val ubyte = readUByte()
    val (majorType, additionalInfo) = majorTypeAndAdditionalInfo(ubyte)
    fun readSizeAndIfDefinite(block: (ULong) -> DataItem): DataItem {
        return when (val byteCount = readSize(additionalInfo)) {
            Size.Indefinite -> error("Streaming not supported")
            is Size.Definite -> block(byteCount.size)
        }
    }
    return when (majorType) {
        MajorType.Zero -> readCborPosInt(additionalInfo)
        MajorType.One -> readCborNegInt(additionalInfo)
        MajorType.Two -> readSizeAndIfDefinite { byteCount -> readCborBytes(byteCount) }
        MajorType.Three -> readSizeAndIfDefinite { byteCount -> readCborText(byteCount) }
        MajorType.Four -> readSizeAndIfDefinite { itemsNo -> readCborArray(itemsNo) }
        MajorType.Five -> readSizeAndIfDefinite { entriesNo -> readCborMap(entriesNo) }
        MajorType.Six -> readCborTag(additionalInfo)
        MajorType.Seven -> readMajorSeven(additionalInfo)
    }
}


private fun BufferedSource.readCborPosInt(additionalInfo: AdditionalInfo): DataItem.PositiveInteger {
    val value = readUnsignedInt(additionalInfo)
    return DataItem.PositiveInteger(value)
}

private fun BufferedSource.readCborNegInt(additionalInfo: AdditionalInfo): DataItem.NegativeInteger {
    val value = readUnsignedInt(additionalInfo)
    return DataItem.NegativeInteger(-1 - value.toLong())
}
private fun BufferedSource.readCborBytes(byteCount: ULong): DataItem.Bytes {
    val bytes = buildList {
        repeat(byteCount) { add(readUByte()) }
    }.toUByteArray()
    return DataItem.Bytes(bytes)
}
private fun BufferedSource.readCborText(byteCount: ULong): DataItem.Text {
    require(byteCount <= Long.MAX_VALUE.toULong()) { "Too big" }
    return DataItem.Text(readUtf8(byteCount.toLong()))
}
private fun BufferedSource.readCborArray(itemsNo: ULong): DataItem.CborArray {
    val items = buildList {
        repeat(itemsNo) { add(readCbor()) }
    }
    return DataItem.CborArray(items)
}
private fun BufferedSource.readCborMap(itemsNo: ULong): DataItem.CborMap {
    val items = buildMap {
        repeat(itemsNo) {
            val k = Key(readCbor()).getOrThrow()
            check(!contains(k)) { "Duplicate $k" }
            val value = readCbor()
            put(k, value)
        }
    }
    return DataItem.CborMap(items)
}
private fun BufferedSource.readCborTag(additionalInfo: AdditionalInfo): DataItem.Tagged<*> {
    val tag = readUnsignedInt(additionalInfo).let(Tag::of)
    val value = readCbor()
    return tag(tag, value)
}
private fun BufferedSource.readMajorSeven(additionalInfo: AdditionalInfo): DataItem = when (additionalInfo.value.toInt()) {
    in 0..19 -> DataItem.SimpleType.Unassigned(additionalInfo.value)
    20 -> DataItem.Bool(false)
    21 -> DataItem.Bool(true)
    22 -> DataItem.Null
    23 -> DataItem.Undefined
    24 -> when (val next = readUByte()) {
        in 0.toUByte()..31.toUByte() -> DataItem.SimpleType.Reserved(next)
        else -> DataItem.SimpleType.Unassigned(next)
    }

    25 -> TODO("F16???")
    26 -> DataItem.Float32(TODO("F32???"))
    27 -> DataItem.Float64(TODO("F64???"))
    in 28..30 -> DataItem.SimpleType.Unassigned(additionalInfo.value)
    31 -> DataItem.Break
    else -> error("Not supported")
}

private sealed interface Size {
    data object Indefinite : Size
    data class Definite(val size: ULong) : Size
}

private const val IndefiniteLengthIndicator: UByte = 31u

private fun BufferedSource.readSize(additionalInfo: AdditionalInfo): Size =
        (if (additionalInfo.value == IndefiniteLengthIndicator) Size.Indefinite
        else Size.Definite(readUnsignedInt(additionalInfo)))


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

/**
 * Splits the [initialByte] into a  [MajorType] and  [AdditionalInfo]
 *
 * @param initialByte The initial byte of each encoded data item contains
 * both information about the major type (the high-order 3 bits) and
 * additional information (the low-order 5 bits)
 * @return the [MajorType] and the [AdditionalInfo]
 */
private fun majorTypeAndAdditionalInfo(initialByte: UByte): Pair<MajorType, AdditionalInfo> {

    val highOrder3Bits = (initialByte.toInt() and 0b11100000) shr 5
    val lowOrder5Bits = initialByte and 0b00011111.toUByte()
    val majorType = when (highOrder3Bits) {
        0 -> MajorType.Zero
        1 -> MajorType.One
        2 -> MajorType.Two
        3 -> MajorType.Three
        4 -> MajorType.Four
        5 -> MajorType.Five
        6 -> MajorType.Six
        7 -> MajorType.Seven
        else -> error("Cannot happen, since we use only 3bits")
    }
    val additionalInfo = AdditionalInfo(lowOrder5Bits)
    return majorType to additionalInfo
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