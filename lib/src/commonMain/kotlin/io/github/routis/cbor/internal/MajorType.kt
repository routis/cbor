package io.github.routis.cbor.internal

/**
 * The three higher bits of the initial byte
 */
internal enum class MajorType {
    Zero, One, Two, Three, Four, Five, Six, Seven;

    companion object {
        operator fun invoke(initialByte: UByte): MajorType {
            val highOrder3Bits = (initialByte.toInt() and 0b11100000) shr 5
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
 * The 5 lower bits of the initial byte
 */
internal class AdditionalInfo private constructor(val value: UByte) : Comparable<AdditionalInfo> {
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

        operator fun invoke(initialByte: UByte): AdditionalInfo {
            val lowOrder5Bits = initialByte and 0b00011111.toUByte()
            return AdditionalInfo(lowOrder5Bits)
        }

        /**
         * Calculates the closest [AdditionalInfo] for the given [size]
         */
        fun forSize(size: ULong): AdditionalInfo = when (size) {
            in 0uL..<24uL -> AdditionalInfo(size.toUByte())
            in 24uL..UByte.MAX_VALUE.toULong() -> AdditionalInfo(24u)
            in (UByte.MAX_VALUE + 1u).toULong()..UShort.MAX_VALUE.toULong() -> AdditionalInfo(25u)
            in (UShort.MAX_VALUE + 1u).toULong()..UInt.MAX_VALUE.toULong() -> AdditionalInfo(26u)
            in (UInt.MAX_VALUE + 1u).toULong()..ULong.MAX_VALUE -> AdditionalInfo(27u)
            else -> error("Invalid value $size")
        }

    }
}

/**
 * Creates the initial byte by combining the given [majorType] (three higher bits) with
 * the [additionalInfo] (lower five bits)
 */
internal fun initialByte(majorType: MajorType, additionalInfo: AdditionalInfo): UByte {
    val highOrder3Bits: UByte = when (majorType) {
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