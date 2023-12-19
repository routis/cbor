package io.github.routis.cbor

sealed interface DataItem

/**
 * Integer numbers in the range -2^64..2^64-1 inclusive
 */
sealed interface IntegerDataItem : DataItem

/**
 * Unsigned integer in the range 0 to 2^64-1 inclusive
 *
 * @param value The numeric value
 */
data class UnsignedIntegerDataItem(val value: ULong) : IntegerDataItem {
    init {
        require(value >= 0uL) { "Value should be in range 0..2^64-1" }
    }
}

/**
 * Negative integer in the range -2^64 to -1 inclusive
 *
 * Please note that the [value] is the unsigned (zero or positive) representation
 * of the number.
 *
 * To get the actual numeric value, please use [NegativeIntegerDataItem.asBigInteger]
 *
 * @param value the unsigned representation of the Negative Integer.
 */
data class NegativeIntegerDataItem(val value: ULong) : IntegerDataItem {
    init {
        require(value >= 0uL) { "Value should be in range 0..2^64-1" }
    }
}

/**
 * A "string" of [bytes]
 */
data class ByteStringDataItem(val bytes: ByteArray) : DataItem {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ByteStringDataItem

        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = bytes.contentHashCode()
}

/**
 * A [text] string encoded as UTF-8.
 */
data class TextStringDataItem(val text: String) : DataItem

/**
 * An array of data [items].
 * Items in an array do not need to all be of the same type
 */
data class ArrayDataItem(private val items: List<DataItem>) : DataItem, List<DataItem> by items

/**
 * A map (set of key value pairs)
 * Not all [DataItem] can be used as keys.
 * Check [Key]
 */
data class MapDataItem(private val items: Map<Key<*>, DataItem>) : DataItem, Map<Key<*>, DataItem> by items

/**
 * A tagged [DataItem]
 * @param DI the type of the content
 */
sealed interface TaggedDataItem<out DI : DataItem> : DataItem {
    val content: DI
}

/**
 * Tag number 0 contains a text string in the standard format described by the
 * date-time production in RFC3339, as refined by Section 3.3 of RFC4287,
 * representing the point in time described there.
 */
data class StandardDateTimeDataItem(override val content: TextStringDataItem) : TaggedDataItem<TextStringDataItem>

sealed interface EpochBasedDateTime<DI : DataItem> : TaggedDataItem<DI>

data class IntegerEpochDataItem(override val content: IntegerDataItem) : EpochBasedDateTime<IntegerDataItem>

data class HalfFloatEpochDataItem(override val content: HalfPrecisionFloatDataItem) :
    EpochBasedDateTime<HalfPrecisionFloatDataItem>

data class SingleFloatEpochDataItem(override val content: SinglePrecisionFloatDataItem) :
    EpochBasedDateTime<SinglePrecisionFloatDataItem>

data class DoubleFloatEpochDataItem(override val content: DoublePrecisionFloatDataItem) :
    EpochBasedDateTime<DoublePrecisionFloatDataItem>

/**
 *  To get the actual numeric value, please use [BigNumUnsigned.asBigInteger]
 */
data class BigNumUnsigned(override val content: ByteStringDataItem) : TaggedDataItem<ByteStringDataItem>

/**
 *  To get the actual numeric value, please use [BigNumNegative.asBigInteger]
 */
data class BigNumNegative(override val content: ByteStringDataItem) : TaggedDataItem<ByteStringDataItem>

data class DecimalFraction(
    val exponent: IntegerDataItem,
    val mantissa: IntegerDataItem,
) : TaggedDataItem<ArrayDataItem> {
    override val content: ArrayDataItem
        get() = ArrayDataItem(listOf(exponent, mantissa))
}

data class BigFloat(
    val exponent: IntegerDataItem,
    val mantissa: IntegerDataItem,
) : TaggedDataItem<ArrayDataItem> {
    override val content: ArrayDataItem
        get() = ArrayDataItem(listOf(exponent, mantissa))
}

data class CborDataItem(override val content: ByteStringDataItem) : TaggedDataItem<ByteStringDataItem> {
    constructor(content: ByteArray) : this(ByteStringDataItem(content))
}

data class SelfDescribedCbor(override val content: DataItem) : TaggedDataItem<DataItem>

sealed interface EncodedText : TaggedDataItem<TextStringDataItem>

data class Base64DataItem(override val content: TextStringDataItem) : EncodedText {
    constructor(content: String) : this(TextStringDataItem(content))
}

data class Base64UrlDataItem(override val content: TextStringDataItem) : EncodedText {
    constructor(content: String) : this(TextStringDataItem(content))
}

data class RegexDataItem(override val content: TextStringDataItem) : EncodedText {
    constructor(content: String) : this(TextStringDataItem(content))
}

data class MimeDataItem(override val content: TextStringDataItem) : EncodedText {
    constructor(content: String) : this(TextStringDataItem(content))
}

data class UriDataItem(override val content: TextStringDataItem) : EncodedText {
    constructor(content: String) : this(TextStringDataItem(content))
}

data class CalendarDayDataItem(override val content: IntegerDataItem) : TaggedDataItem<IntegerDataItem>

data class CalendarDateDataItem(override val content: TextStringDataItem) : TaggedDataItem<TextStringDataItem> {
    constructor(content: String) : this(TextStringDataItem(content))
}

data class UnsupportedTagDataItem(val tag: ULong, override val content: DataItem) : TaggedDataItem<DataItem>

data class BooleanDataItem(val value: Boolean) : DataItem

data class HalfPrecisionFloatDataItem(val value: Float) : DataItem

data class SinglePrecisionFloatDataItem(val value: Float) : DataItem

data class DoublePrecisionFloatDataItem(val value: Double) : DataItem

data object NullDataItem : DataItem

data object UndefinedDataItem : DataItem

data class UnassignedDataItem(val value: UByte) : DataItem

data class ReservedDataItem(val value: UByte) : DataItem
