package io.github.routis.cbor

import java.math.BigInteger

sealed interface DataItem {

    /**
     * Integer numbers in the range -2^64..2^64-1 inclusive
     */
    sealed interface Integer : DataItem {

        /**
         * Unsigned integer in the range 0..2^64-1 inclusive
         */
        @JvmInline
        value class Unsigned(val value: ULong) : Integer {
            init {
                require(value >= 0uL) { "Value should be in range 0..2^64-1" }
            }
        }

        /**
         * Negative integer in the range -2^64..-1 inclusive
         */
        @JvmInline
        value class Negative(val value: BigInteger) : Integer {
            init {
                require(value <= -BigInteger.ONE) { "Value should be in range -2^64..-1 inclusive" }
            }
        }
    }

    /**
     * A "string" of [bytes]
     */
    @JvmInline
    value class ByteString(val bytes: ByteArray) : DataItem

    /**
     * A [text] string  encoded as UTF-8.
     */
    @JvmInline
    value class TextString(val text: String) : DataItem

    /**
     * An array of data [items].
     * Items in an array do not need to all be of the same type
     */
    @JvmInline
    value class Array(private val items: List<DataItem>) : DataItem, List<DataItem> by items

    /**
     * A map (set of key value pairs)
     * Not all [DataItem] can be used as keys. Check [Key]
     */
    @JvmInline
    value class CborMap(private val items: Map<Key<*>, DataItem>) : DataItem, Map<Key<*>, DataItem> by items

    /**
     * A tagged [DataItem]
     * @param DI the type of the content
     */
    sealed interface Tagged<out DI : DataItem> : DataItem {
        val content: DI

        /**
         * Tag number 0 contains a text string in the standard format described by the
         * date-time production in RFC3339, as refined by Section 3.3 of RFC4287,
         * representing the point in time described there.
         */
        data class StandardDateTimeString(override val content: TextString) : Tagged<TextString>
        sealed interface EpochBasedDateTime<DI : DataItem> : Tagged<DI> {
            data class Unsigned(override val content: Integer.Unsigned) : EpochBasedDateTime<Integer.Unsigned>
            data class Negative(override val content: Integer.Negative) : EpochBasedDateTime<Integer.Negative>
            data class HalfFloat(override val content: HalfPrecisionFloat) : EpochBasedDateTime<HalfPrecisionFloat>
            data class SingleFloat(override val content: SinglePrecisionFloat) : EpochBasedDateTime<SinglePrecisionFloat>
            data class DoubleFloat(override val content: DoublePrecisionFloat) : EpochBasedDateTime<DoublePrecisionFloat>
        }

        data class BigNumUnsigned(override val content: ByteString) : Tagged<ByteString>
        data class BigNumNegative(override val content: ByteString) : Tagged<ByteString>
        data class DecimalFraction(val exponent: Integer, val mantissa: Integer) : Tagged<Array> {
            override val content: Array
                get() = Array(listOf(exponent, mantissa))
        }

        data class BigFloat(val exponent: Integer, val mantissa: Integer) : Tagged<Array> {
            override val content: Array
                get() = Array(listOf(exponent, mantissa))
        }

        data class DborDataItem(override val content: ByteString) : Tagged<ByteString>

        data class SelfDescribedCbor(override val content: DataItem) : Tagged<DataItem>

        sealed interface EncodedText : Tagged<TextString> {
            data class Base64(override val content: TextString) : EncodedText
            data class Base64Url(override val content: TextString) : EncodedText
            data class Regex(override val content: TextString) : EncodedText
            data class Mime(override val content: TextString) : EncodedText
            data class Uri(override val content: TextString) : EncodedText
        }

        data class CalendarDay(override val content: Integer): Tagged<Integer>
        data class CalendarDate(override val content: TextString) : Tagged<TextString>
        data class Unsupported(val tag: ULong, override val content: DataItem) : Tagged<DataItem>

    }

    @JvmInline
    value class Bool(val value: Boolean) : DataItem

    @JvmInline
    value class HalfPrecisionFloat(val value: Float) : DataItem

    @JvmInline
    value class SinglePrecisionFloat(val value: Float) : DataItem

    @JvmInline
    value class DoublePrecisionFloat(val value: Double) : DataItem

    data object Null : DataItem
    data object Undefined : DataItem

    @JvmInline
    value class Unassigned(val value: UByte) : DataItem

    @JvmInline
    value class Reserved(val value: UByte) : DataItem

}