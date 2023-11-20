package io.github.routis.cbor.internal

import io.github.routis.cbor.DataItem
import io.github.routis.cbor.Key
import okio.BufferedSource
import okio.IOException
import kotlin.contracts.contract

/**
 * When reading a byte array for [DataItem] you can
 * get either a [DataItemOrBreak.Item] or a [DataItemOrBreak.Break]
 */
private sealed interface DataItemOrBreak {
    @JvmInline
    value class Item(val item: DataItem) : DataItemOrBreak
    data object Break : DataItemOrBreak
}

/**
 * Lifts a [DataItem] to the [DataItemOrBreak] hierarchy
 */
private fun DataItem.orBreak() = DataItemOrBreak.Item(this)

/**
 * Reads the next [DataItem] from the source.
 *
 * @throws IllegalStateException when instead of a [DataItem] we get break
 */
@Throws(IllegalStateException::class, IOException::class)
internal fun BufferedSource.readDataItem(): DataItem {
    val dataItemOrBreak = readDataItemOrBreak()
    check(dataItemOrBreak is DataItemOrBreak.Item) { "Unexpected break" }
    return dataItemOrBreak.item
}

/**
 * Reads the next [DataItem] or break from the source.
 */
@Throws(IOException::class)
private fun BufferedSource.readDataItemOrBreak(): DataItemOrBreak {

    val initialByte = readUByte()
    val majorType = MajorType(initialByte)
    val additionalInfo = AdditionalInfo(initialByte)

    fun readSizeThen(block: (Size) -> DataItem): DataItem =
            readSize(additionalInfo).let(block)

    return when (majorType) {
        MajorType.Zero -> readUnsigntInteger(additionalInfo).orBreak()
        MajorType.One -> readNegativeInteger(additionalInfo).orBreak()
        MajorType.Two -> readSizeThen(::readByteString).orBreak()
        MajorType.Three -> readSizeThen(::readTextString).orBreak()
        MajorType.Four -> readSizeThen(::readArray).orBreak()
        MajorType.Five -> readSizeThen(::readMap).orBreak()
        MajorType.Six -> readTagged(additionalInfo).orBreak()
        MajorType.Seven -> readMajorSeven(additionalInfo)
    }
}

@Throws(IOException::class)
private fun BufferedSource.readUnsigntInteger(additionalInfo: AdditionalInfo): DataItem.UnsignedInteger {
    val value = readUnsignedInt(additionalInfo)
    return DataItem.UnsignedInteger(value)
}

@Throws(IOException::class)
private fun BufferedSource.readNegativeInteger(additionalInfo: AdditionalInfo): DataItem.NegativeInteger {
    val value = readUnsignedInt(additionalInfo)
    return DataItem.NegativeInteger(-1 - value.toLong())
}

private const val BRAKE_BYTE: UByte = 0b111_11111u

@Throws(IOException::class)
private fun BufferedSource.readByteString(size: Size): DataItem.ByteString {
    val byteCount = when (size) {
        Size.Indefinite -> indexOf(BRAKE_BYTE.toByte())
        is Size.Definite -> size.value.toLong()
    }
    val bytes = readByteArray(byteCount).toUByteArray()
    return DataItem.ByteString(bytes)
}

@Throws(IOException::class)
private fun BufferedSource.readTextString(size: Size): DataItem.TextString {
    val text = when (size) {

        Size.Indefinite -> {
            var buffer = ""
            untilBreak { item ->
                require(item is DataItem.TextString)
                buffer += item.value
            }
            buffer
        }

        is Size.Definite -> {
            require(size.value <= Long.MAX_VALUE.toULong()) { "Too big byteCount to handle" }
            val byteCount = size.value.toLong()
            readUtf8(byteCount)
        }
    }

    return DataItem.TextString(text)
}

@Throws(IOException::class)
private fun BufferedSource.readArray(size: Size): DataItem.Array {
    val items = buildList {
        when (size) {
            Size.Indefinite -> untilBreak(::add)
            is Size.Definite -> repeat(size.value) { add(readDataItem()) }
        }
    }
    return DataItem.Array(items)
}

@Throws(IOException::class)
private fun BufferedSource.readMap(size: Size): DataItem.CborMap {

    val items = buildMap {

        fun put(item: DataItem) {
            val key = Key(item).getOrThrow()
            check(!contains(key)) { "Duplicate $key" }
            val value = readDataItem()
            put(key, value)
        }

        when (size) {
            Size.Indefinite -> untilBreak(::put)
            is Size.Definite -> repeat(size.value) { put(readDataItem()) }
        }
    }

    return DataItem.CborMap(items)
}

@Throws(IOException::class)
private fun BufferedSource.readTagged(additionalInfo: AdditionalInfo): DataItem.Tagged<*> {
    val tag = readUnsignedInt(additionalInfo)
    val dataItem = readDataItem()
    return when (val supportedTag = Tag.of(tag)) {
        null -> DataItem.Tagged.Unassigned(tag, dataItem)
        else -> supportedTag.withItem(dataItem)
    }
}

@Throws(IOException::class)
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

/**
 * Reads the next [DataItem] until it finds a break.
 * For each [DataItem] the callback [useDataItem] is being invoked
 */
@Throws(IOException::class)
private fun BufferedSource.untilBreak(useDataItem: (DataItem) -> Unit) {
    do {
        val dataItemOrBreak = readDataItemOrBreak()
        if (dataItemOrBreak is DataItemOrBreak.Item) {
            useDataItem(dataItemOrBreak.item)
        }
    } while (dataItemOrBreak !is DataItemOrBreak.Break)
}

private inline fun repeat(times: ULong, action: (ULong) -> Unit) {
    contract { callsInPlace(action) }
    for (index in 0uL until times) {
        action(index)
    }
}

private sealed interface Size {
    data object Indefinite : Size

    @JvmInline
    value class Definite(val value: ULong) : Size
}


private const val indefiniteLengthIndicator: UByte = 31u

@Throws(IOException::class)
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
@Throws(IOException::class)
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

@Throws(IOException::class)
private fun BufferedSource.readUByte(): UByte = readByte().toUByte()

@Throws(IOException::class)
private fun BufferedSource.readUShort(): UShort = readShort().toUShort()

@Throws(IOException::class)
private fun BufferedSource.readUInt(): UInt = readInt().toUInt()

@Throws(IOException::class)
private fun BufferedSource.readULong(): ULong = readLong().toULong()
