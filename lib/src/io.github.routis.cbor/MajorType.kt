package io.github.routis.cbor

/**
 * - [MajorType.Zero]: An unsigned integer in the range 0..264-1 inclusive
 */
internal enum class MajorType {
    Zero, One, Two, Three, Four, Five, Six, Seven
}

@JvmInline
internal value class AdditionalInfo(val value: UByte): Comparable<AdditionalInfo> {
    init{
        require(value  in ZERO .. THIRTY_ONE) {"Value should be betweem $ZERO .. $THIRTY_ONE"}
    }
    override fun compareTo(other: AdditionalInfo): Int = this.value.compareTo(other.value)

    companion object {
        private const val ZERO : UByte = 0u
        private const val THIRTY_ONE : UByte = 31u
    }


}

