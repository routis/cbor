package io.github.routis.cbor.internal

import io.github.routis.cbor.DataItem
import io.github.routis.cbor.Key
import kotlinx.io.*
import kotlin.contracts.contract

/**
 * When reading a byte array for [DataItem] you can
 * get either a [DataItemOrBreak.Item] or a [DataItemOrBreak.Break]
 */
private sealed interface DataItemOrBreak {
    data class Item(val item: DataItem) : DataItemOrBreak

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
internal fun Source.readDataItem(): DataItem {
    val dataItemOrBreak = readDataItemOrBreak()
    check(dataItemOrBreak is DataItemOrBreak.Item) { "Unexpected break" }
    return dataItemOrBreak.item
}

/**
 * Reads the next [DataItem] or break from the source.
 */
private fun Source.readDataItemOrBreak(): DataItemOrBreak {
    val initialByte = readUByte()
    val majorType = MajorType.fromInitialByte(initialByte)
    val additionalInfo = AdditionalInfo.fromInitialByte(initialByte)

    fun readSizeThen(block: (Size) -> DataItem): DataItem = readSize(additionalInfo).let(block)

    return when (majorType) {
        MajorType.Zero -> readUnsignedInteger(additionalInfo).orBreak()
        MajorType.One -> readNegativeInteger(additionalInfo).orBreak()
        MajorType.Two -> readSizeThen(::readByteString).orBreak()
        MajorType.Three -> readSizeThen(::readTextString).orBreak()
        MajorType.Four -> readSizeThen(::readArray).orBreak()
        MajorType.Five -> readSizeThen(::readMap).orBreak()
        MajorType.Six -> readTagged(additionalInfo).orBreak()
        MajorType.Seven -> readMajorSeven(additionalInfo)
    }
}

private fun Source.readUnsignedInteger(additionalInfo: AdditionalInfo): DataItem.Integer.Unsigned {
    val value = readUnsignedInt(additionalInfo)
    return DataItem.Integer.Unsigned(value)
}

private fun Source.readNegativeInteger(additionalInfo: AdditionalInfo): DataItem.Integer.Negative {
    val value = readUnsignedInt(additionalInfo)
    return DataItem.Integer.Negative(value)
}

private const val BRAKE_BYTE: UByte = 0b111_11111u

private fun Source.readByteString(size: Size): DataItem.ByteString {
    val byteCount =
        when (size) {
            Size.Indefinite -> indexOf(BRAKE_BYTE.toByte())
            is Size.Definite -> size.value.toLong()
        }
    val bytes = readByteArray(byteCount.toInt())
    return DataItem.ByteString(bytes)
}

private fun Source.readTextString(size: Size): DataItem.TextString {
    val text =
        when (size) {
            Size.Indefinite -> {
                var buffer = ""
                untilBreak { item ->
                    require(item is DataItem.TextString)
                    buffer += item.text
                }
                buffer
            }

            is Size.Definite -> {
                require(size.value <= Long.MAX_VALUE.toULong()) { "Too big byteCount to handle" }
                val byteCount = size.value.toLong()
                readString(byteCount)
            }
        }

    return DataItem.TextString(text)
}

private fun Source.readArray(size: Size): DataItem.Array {
    val items =
        buildList {
            when (size) {
                Size.Indefinite -> untilBreak(::add)
                is Size.Definite -> repeat(size.value) { add(readDataItem()) }
            }
        }
    return DataItem.Array(items)
}

private fun Source.readMap(size: Size): DataItem.CborMap {
    val items =
        buildMap {
            fun put(item: DataItem) {
                val key = Key(item) ?: error("$item cannot be used as key")
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

private fun Source.readTagged(additionalInfo: AdditionalInfo): DataItem.Tagged<*> {
    val tag = readUnsignedInt(additionalInfo)
    val dataItem = readDataItem()
    return when (val supportedTag = Tag.of(tag)) {
        null -> DataItem.Tagged.Unsupported(tag, dataItem)
        else -> dataItem.tagged(supportedTag)
    }
}

private fun Source.readMajorSeven(additionalInfo: AdditionalInfo): DataItemOrBreak {
    return when (additionalInfo.value) {
        in 0u..19u -> DataItem.Unassigned(additionalInfo.value).orBreak()
        AdditionalInfo.BOOLEAN_FALSE -> DataItem.Bool(false).orBreak()
        AdditionalInfo.BOOLEAN_TRUE -> DataItem.Bool(true).orBreak()
        AdditionalInfo.NULL -> DataItem.Null.orBreak()
        AdditionalInfo.UNDEFINED -> DataItem.Undefined.orBreak()
        AdditionalInfo.RESERVED_OR_UNASSIGNED ->
            when (val next = readUByte()) {
                in 0.toUByte()..31.toUByte() -> DataItem.Reserved(next)
                else -> DataItem.Unassigned(next)
            }.orBreak()

        AdditionalInfo.HALF_PRECISION_FLOAT -> DataItem.HalfPrecisionFloat(floatFromHalfBits(readShort())).orBreak()
        AdditionalInfo.SINGLE_PRECISION_FLOAT -> DataItem.SinglePrecisionFloat(Float.fromBits(readInt())).orBreak()
        AdditionalInfo.DOUBLE_PRECISION_FLOAT -> DataItem.DoublePrecisionFloat(Double.fromBits(readLong())).orBreak()
        in 28u..30u -> DataItem.Unassigned(additionalInfo.value).orBreak()
        AdditionalInfo.BREAK -> DataItemOrBreak.Break
        else -> error("Not supported")
    }
}

/**
 * Reads the next [DataItem] until it finds a break.
 * For each [DataItem] the callback [useDataItem] is being invoked
 */
private fun Source.untilBreak(useDataItem: (DataItem) -> Unit) {
    do {
        val dataItemOrBreak = readDataItemOrBreak()
        if (dataItemOrBreak is DataItemOrBreak.Item) {
            useDataItem(dataItemOrBreak.item)
        }
    } while (dataItemOrBreak !is DataItemOrBreak.Break)
}

private inline fun repeat(
    times: ULong,
    action: (ULong) -> Unit,
) {
    contract { callsInPlace(action) }
    for (index in 0uL until times) {
        action(index)
    }
}

private sealed interface Size {
    data object Indefinite : Size

    data class Definite(val value: ULong) : Size
}

private fun Source.readSize(additionalInfo: AdditionalInfo): Size =
    when (additionalInfo.value) {
        AdditionalInfo.INDEFINITE_LENGTH_INDICATOR -> Size.Indefinite
        else -> Size.Definite(readUnsignedInt(additionalInfo))
    }

/**
 * Reads from the [Source] an unsigned integer, using the [additionalInfo] as follows:
 * - if [additionalInfo] is less than 24, the result is the additional info itself
 * - if [additionalInfo] is 24, 25, 26 or 27 The argument's value is held in the following 1, 2, 4, or 8 bytes,
 *   respectively, in network byte order
 *
 * @param additionalInfo the instruction on how to read the unsigned integer
 * @receiver The [Source] to read from
 * @return the unsigned integer. Regardless of the case this always a [ULong]
 */
private fun Source.readUnsignedInt(additionalInfo: AdditionalInfo): ULong {
    return when (additionalInfo.value) {
        in AdditionalInfo.ZeroToTwentyThreeRange -> additionalInfo.value.toULong()
        AdditionalInfo.SINGLE_BYTE_UINT -> readUByte().toULong()
        AdditionalInfo.DOUBLE_BYTE_UINT -> readUShort().toULong()
        AdditionalInfo.FOUR_BYTE_UINT -> readUInt().toULong()
        AdditionalInfo.EIGHT_BYTE_UINT -> readULong()
        else -> error("Cannot use ${additionalInfo.value} for reading unsigned. Valid values are in 0..27 inclusive")
    }
}
