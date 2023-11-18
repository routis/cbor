package io.github.routis.cbor

sealed interface DataItem {

    sealed interface Integer : DataItem

    /**
     * Major 0
     */
    @JvmInline
    value class UnsignedInteger(val value: ULong) : Integer

    /**
     * Major 1
     */
    @JvmInline
    value class NegativeInteger(val value: Long) : Integer {
        init {
            require(value <= -1) { "Should -1 or lower but was $value" }
        }
    }

    /**
     * Major 2
     */
    @JvmInline
    value class ByteString(val value: UByteArray) : DataItem

    /**
     * Major 3
     */
    
    @JvmInline
    value class TextString(val value: String) : DataItem

    /**
     * Major 4
     */
    @JvmInline
    value class Array(private val items: List<DataItem>) : DataItem, List<DataItem> by items

    /**
     * Major 5
     */
    @JvmInline
    value class CborMap(private val items: Map<Key<*>, DataItem>) : DataItem, Map<Key<*>, DataItem> by items

    /**
     * Major 6
     */
    sealed interface Tagged<out DI : DataItem> : DataItem {
        val item: DI

        /**
         * Tag number 0 contains a text string in the standard format described by the
         * date-time production in RFC3339, as refined by Section 3.3 of RFC4287,
         * representing the point in time described there.
         */
        data class StandardDateTimeString(override val item: TextString) : Tagged<TextString>
        sealed interface EpochBasedDateTime<DI : DataItem> : Tagged<DI> {
            data class Unsigned(override val item: UnsignedInteger) : EpochBasedDateTime<UnsignedInteger>
            data class Negative(override val item: NegativeInteger) : EpochBasedDateTime<NegativeInteger>
            data class HalfFloat(override val item: HalfPrecisionFloat) : EpochBasedDateTime<HalfPrecisionFloat>
            data class SingleFloat(override val item: SinglePrecisionFloat) : EpochBasedDateTime<SinglePrecisionFloat>
            data class DoubleFloat(override val item: DoublePrecisionFloat) : EpochBasedDateTime<DoublePrecisionFloat>
        }

        data class BigNumUnsigned(override val item: ByteString) : Tagged<ByteString>
        data class BigNumNegative(override val item: ByteString) : Tagged<ByteString>
        data class DecimalFraction(val exponent: Integer, val mantissa: Integer) : Tagged<Array> {
            override val item: Array
                get() = Array(listOf(exponent, mantissa))
        }
        data class BigFloat(val exponent: Integer, val mantissa: Integer) : Tagged<Array> {
            override val item: Array
                get() = Array(listOf(exponent, mantissa))
        }
        data class DborDataItem(override val item: ByteString) : Tagged<ByteString>

        data class SelfDescribedCbor(override val item: DataItem): Tagged<DataItem>

        sealed interface EncodedText : Tagged<TextString> {
            data class Base64(override val item: TextString) : EncodedText
            data class Base64Url(override val item: TextString) : EncodedText
            data class Regex(override val item: TextString) : EncodedText
            data class Mime(override val item: TextString) : EncodedText
            data class Uri(override val item: TextString) : EncodedText
        }
        data class FullDateTime(override val item: TextString) : Tagged<TextString>
        data class Unassigned(val tag: ULong, override val item: DataItem) : Tagged<DataItem>

    }

    @JvmInline
    value class HalfPrecisionFloat(val value: Float) : DataItem

    @JvmInline
    value class SinglePrecisionFloat(val value: Float) : DataItem

    @JvmInline
    value class DoublePrecisionFloat(val value: Double) : DataItem

    data object Null : DataItem
    data object Undefined : DataItem

    @JvmInline
    value class Bool(val value: Boolean) : DataItem

    sealed interface SimpleType : DataItem {
        @JvmInline
        value class Unassigned(val value: UByte) : SimpleType

        @JvmInline
        value class Reserved(val value: UByte) : SimpleType
    }

}




