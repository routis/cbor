package io.github.routis.cbor

import io.github.routis.cbor.DataItem.Integer.Negative

sealed interface DataItem {
    /**
     * Integer numbers in the range -2^64..2^64-1 inclusive
     */
    sealed interface Integer : DataItem {
        /**
         * Unsigned integer in the range 0..2^64-1 inclusive
         *
         * @param value The numeric value
         */
        data class Unsigned(val value: ULong) : Integer {
            init {
                require(value >= 0uL) { "Value should be in range 0..2^64-1" }
            }
        }

        /**
         * Negative integer in the range -2^64..-1 inclusive
         *
         * Please note that the [value] is the unsigned (zero or positive) representation
         * of the number.
         *
         * To get the actual numeric value, please use [Negative.asBigInteger]
         *
         * @param value the unsigned representation of the Negative Integer.
         */
        data class Negative(val value: ULong) : Integer {
            init {
                require(value >= 0uL) { "Value should be in range 0..2^64-1" }
            }
        }
    }

    /**
     * A "string" of [bytes]
     */
    data class ByteString(val bytes: ByteArray) : DataItem {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as ByteString

            return bytes.contentEquals(other.bytes)
        }

        override fun hashCode(): Int = bytes.contentHashCode()
    }

    /**
     * A [text] string encoded as UTF-8.
     */
    data class TextString(val text: String) : DataItem

    /**
     * An array of data [items].
     * Items in an array do not need to all be of the same type
     */
    data class Array(private val items: List<DataItem>) : DataItem, List<DataItem> by items

    /**
     * A map (set of key value pairs)
     * Not all [DataItem] can be used as keys. Check [Key]
     */
    data class CborMap(private val items: Map<Key<*>, DataItem>) : DataItem, Map<Key<*>, DataItem> by items

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
            data class Integer(override val content: DataItem.Integer) : EpochBasedDateTime<DataItem.Integer>

            data class HalfFloat(override val content: HalfPrecisionFloat) : EpochBasedDateTime<HalfPrecisionFloat>

            data class SingleFloat(override val content: SinglePrecisionFloat) :
                EpochBasedDateTime<SinglePrecisionFloat>

            data class DoubleFloat(override val content: DoublePrecisionFloat) :
                EpochBasedDateTime<DoublePrecisionFloat>
        }

        /**
         *  To get the actual numeric value, please use [BigNumUnsigned.asBigInteger]
         */
        data class BigNumUnsigned(override val content: ByteString) : Tagged<ByteString>

        /**
         *  To get the actual numeric value, please use [BigNumNegative.asBigInteger]
         */
        data class BigNumNegative(override val content: ByteString) : Tagged<ByteString>

        data class DecimalFraction(val exponent: Integer, val mantissa: Integer) : Tagged<Array> {
            override val content: Array
                get() = Array(listOf(exponent, mantissa))
        }

        data class BigFloat(val exponent: Integer, val mantissa: Integer) : Tagged<Array> {
            override val content: Array
                get() = Array(listOf(exponent, mantissa))
        }

        data class CborDataItem(override val content: ByteString) : Tagged<ByteString>

        data class SelfDescribedCbor(override val content: DataItem) : Tagged<DataItem>

        sealed interface EncodedText : Tagged<TextString> {
            data class Base64(override val content: TextString) : EncodedText

            data class Base64Url(override val content: TextString) : EncodedText

            data class Regex(override val content: TextString) : EncodedText

            data class Mime(override val content: TextString) : EncodedText

            data class Uri(override val content: TextString) : EncodedText
        }

        data class CalendarDay(override val content: Integer) : Tagged<Integer>

        data class CalendarDate(override val content: TextString) : Tagged<TextString>

        data class Unsupported(val tag: ULong, override val content: DataItem) : Tagged<DataItem>
    }

    data class Bool(val value: Boolean) : DataItem

    data class HalfPrecisionFloat(val value: Float) : DataItem

    data class SinglePrecisionFloat(val value: Float) : DataItem

    data class DoublePrecisionFloat(val value: Double) : DataItem

    data object Null : DataItem

    data object Undefined : DataItem

    data class Unassigned(val value: UByte) : DataItem

    data class Reserved(val value: UByte) : DataItem
}
