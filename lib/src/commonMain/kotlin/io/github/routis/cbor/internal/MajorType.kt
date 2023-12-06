package io.github.routis.cbor.internal

/**
 * The three higher bits of the initial byte.
 * By definition, there are eight major types from zero to seven.
 *
 * To obtain the [MajorType] from an initial byte use [fromInitialByte]
 */
internal enum class MajorType {
    Zero,
    One,
    Two,
    Three,
    Four,
    Five,
    Six,
    Seven,
    ;

    companion object {
        /**
         * Keeps the three high-order bytes of the [initialByte]
         */
        fun fromInitialByte(initialByte: UByte): MajorType {
            // Take the 3 bits and moved the 5 positions to the right
            // filling left positions with zeros
            val highOrder3Bits = initialByte.toInt() ushr 5
            return when (highOrder3Bits) {
                0 -> Zero
                1 -> One
                2 -> Two
                3 -> Three
                4 -> Four
                5 -> Five
                6 -> Six
                7 -> Seven
                else -> error("Cannot happen, since we use only 3bits")
            }
        }
    }
}

/**
 * The five lower bits of the initial byte.
 * By definition [value] is in the range 0..31 inclusive.
 *
 * To obtain additional info from initial byte use [fromInitialByte]
 *
 */
internal class AdditionalInfo(val value: UByte) : Comparable<AdditionalInfo> {
    init {
        require(value in ZERO..THIRTY_ONE) { "Value should be between $ZERO .. $THIRTY_ONE" }
    }

    override fun compareTo(other: AdditionalInfo): Int = this.value.compareTo(other.value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AdditionalInfo
        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = "AdditionalInfo(value=$value)"

    companion object {
        private const val ZERO: UByte = 0u
        private const val THIRTY_ONE: UByte = 31u
        const val SINGLE_BYTE_UINT: UByte = 24u
        const val DOUBLE_BYTE_UINT: UByte = 25u
        const val FOUR_BYTE_UINT: UByte = 26u
        const val EIGHT_BYTE_UINT: UByte = 27u
        const val INDEFINITE_LENGTH_INDICATOR: UByte = 31u
        const val BOOLEAN_FALSE: UByte = 20u
        const val BOOLEAN_TRUE: UByte = 21u
        const val NULL: UByte = 22u
        const val UNDEFINED: UByte = 23u
        const val RESERVED_OR_UNASSIGNED: UByte = 24u
        const val HALF_PRECISION_FLOAT: UByte = 25u
        const val SINGLE_PRECISION_FLOAT: UByte = 26u
        const val DOUBLE_PRECISION_FLOAT: UByte = 27u
        const val BREAK: UByte = 31u
        val ZeroToTwentyThreeRange: ULongRange = 0uL..<SINGLE_BYTE_UINT.toULong()
        private val SingleByteRange: ULongRange = SINGLE_BYTE_UINT.toULong()..UByte.MAX_VALUE.toULong()
        private val DoubleByteRange: ULongRange = UByte.MAX_VALUE.toULong().inc()..UShort.MAX_VALUE.toULong()
        private val FourByteRange: ULongRange = UShort.MAX_VALUE.toULong().inc()..UInt.MAX_VALUE.toULong()
        private val EightByteRange: ULongRange = UInt.MAX_VALUE.toULong().inc()..ULong.MAX_VALUE

        /**
         * Keeps the five lower bits of the given [initialByte]
         */
        fun fromInitialByte(initialByte: UByte): AdditionalInfo {
            val lowOrder5Bits = initialByte and 0b00011111u
            return AdditionalInfo(lowOrder5Bits)
        }

        /**
         * Calculates the closest [AdditionalInfo] for the given [value]
         */
        fun forUnsignedInt(value: ULong): AdditionalInfo =
            when (value) {
                in ZeroToTwentyThreeRange -> AdditionalInfo(value.toUByte())
                in SingleByteRange -> AdditionalInfo(SINGLE_BYTE_UINT)
                in DoubleByteRange -> AdditionalInfo(DOUBLE_BYTE_UINT)
                in FourByteRange -> AdditionalInfo(FOUR_BYTE_UINT)
                in EightByteRange -> AdditionalInfo(EIGHT_BYTE_UINT)
                else -> error("Invalid value $value")
            }
    }
}

/**
 * Creates the initial byte by combining the given [majorType] (three higher bits) with
 * the [additionalInfo] (lower five bits)
 */
internal fun initialByte(
    majorType: MajorType,
    additionalInfo: AdditionalInfo,
): UByte {
    val highOrder3Bits: UByte =
        when (majorType) {
            MajorType.Zero -> 0b000_00000u
            MajorType.One -> 0b001_00000u
            MajorType.Two -> 0b010_00000u
            MajorType.Three -> 0b011_00000u
            MajorType.Four -> 0b100_00000u
            MajorType.Five -> 0b101_00000u
            MajorType.Six -> 0b110_00000u
            MajorType.Seven -> 0b111_00000u
        }

    val lowOrder5Bits = additionalInfo.value
    return highOrder3Bits or lowOrder5Bits
}
