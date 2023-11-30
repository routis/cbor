package io.github.routis.cbor.internal

/**
 * - [MajorType.Zero]: An unsigned integer in the range 0..264-1 inclusive
 * - [MajorType.One]
 *
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


internal class AdditionalInfo private constructor(val value: UByte): Comparable<AdditionalInfo> {
    init{
        require(value  in ZERO .. THIRTY_ONE) {"Value should be betweem $ZERO .. $THIRTY_ONE"}
    }
    override fun compareTo(other: AdditionalInfo): Int = this.value.compareTo(other.value)

    companion object {
        private const val ZERO : UByte = 0u
        private const val THIRTY_ONE : UByte = 31u

        operator fun invoke(initialByte: UByte): AdditionalInfo {
            val lowOrder5Bits = initialByte and 0b00011111.toUByte()
            return AdditionalInfo(lowOrder5Bits)
        }
    }
}