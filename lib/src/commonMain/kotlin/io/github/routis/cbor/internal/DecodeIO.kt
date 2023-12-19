package io.github.routis.cbor.internal

import io.github.routis.cbor.*
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

private fun Source.readUnsignedInteger(additionalInfo: AdditionalInfo): UnsignedIntegerDataItem {
    val value = readUnsignedInt(additionalInfo)
    return UnsignedIntegerDataItem(value)
}

private fun Source.readNegativeInteger(additionalInfo: AdditionalInfo): NegativeIntegerDataItem {
    val value = readUnsignedInt(additionalInfo)
    return NegativeIntegerDataItem(value)
}

private const val BRAKE_BYTE: UByte = 0b111_11111u

private fun Source.readByteString(size: Size): ByteStringDataItem {
    val byteCount =
        when (size) {
            Size.Indefinite -> indexOf(BRAKE_BYTE.toByte())
            is Size.Definite -> size.value.toLong()
        }
    val bytes = readByteArray(byteCount.toInt())
    return ByteStringDataItem(bytes)
}

private fun Source.readTextString(size: Size): TextStringDataItem {
    val text =
        when (size) {
            Size.Indefinite -> {
                var buffer = ""
                untilBreak { item ->
                    require(item is TextStringDataItem)
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

    return TextStringDataItem(text)
}

private fun Source.readArray(size: Size): ArrayDataItem {
    val items =
        buildList {
            when (size) {
                Size.Indefinite -> untilBreak(::add)
                is Size.Definite -> repeat(size.value) { add(readDataItem()) }
            }
        }
    return ArrayDataItem(items)
}

private fun Source.readMap(size: Size): MapDataItem {
    val items =
        buildMap {
            fun put(item: DataItem) {
                val key = keyOf(item) ?: error("$item cannot be used as key")
                check(!contains(key)) { "Duplicate $key" }
                val value = readDataItem()
                put(key, value)
            }

            when (size) {
                Size.Indefinite -> untilBreak(::put)
                is Size.Definite -> repeat(size.value) { put(readDataItem()) }
            }
        }

    return MapDataItem(items)
}

private fun Source.readTagged(additionalInfo: AdditionalInfo): TaggedDataItem<*> {
    val tag = readUnsignedInt(additionalInfo)
    val dataItem = readDataItem()
    return when (val supportedTag = Tag.of(tag)) {
        null -> UnsupportedTagDataItem(tag, dataItem)
        else -> dataItem.tagged(supportedTag)
    }
}

private fun Source.readMajorSeven(additionalInfo: AdditionalInfo): DataItemOrBreak {
    return when (additionalInfo.value) {
        in 0u..19u -> UnassignedDataItem(additionalInfo.value).orBreak()
        AdditionalInfo.BOOLEAN_FALSE -> BooleanDataItem(false).orBreak()
        AdditionalInfo.BOOLEAN_TRUE -> BooleanDataItem(true).orBreak()
        AdditionalInfo.NULL -> NullDataItem.orBreak()
        AdditionalInfo.UNDEFINED -> UndefinedDataItem.orBreak()
        AdditionalInfo.RESERVED_OR_UNASSIGNED ->
            when (val next = readUByte()) {
                in 0.toUByte()..31.toUByte() -> ReservedDataItem(next)
                else -> UnassignedDataItem(next)
            }.orBreak()

        AdditionalInfo.HALF_PRECISION_FLOAT -> HalfPrecisionFloatDataItem(floatFromHalfBits(readShort())).orBreak()
        AdditionalInfo.SINGLE_PRECISION_FLOAT -> SinglePrecisionFloatDataItem(Float.fromBits(readInt())).orBreak()
        AdditionalInfo.DOUBLE_PRECISION_FLOAT -> DoublePrecisionFloatDataItem(Double.fromBits(readLong())).orBreak()
        in 28u..30u -> UnassignedDataItem(additionalInfo.value).orBreak()
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
