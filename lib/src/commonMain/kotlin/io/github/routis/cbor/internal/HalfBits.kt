package io.github.routis.cbor.internal

private const val HALF_PRECISION_EXPONENT_BIAS = 15
private const val HALF_PRECISION_MAX_EXPONENT = 0x1f
private const val HALF_PRECISION_MAX_MANTISSA = 0x3ff
private const val SINGLE_PRECISION_EXPONENT_BIAS = 127
private const val SINGLE_PRECISION_MAX_EXPONENT = 0xFF
private const val SINGLE_PRECISION_NORMALIZE_BASE = 0.5f
private val normalizeBaseBits = SINGLE_PRECISION_NORMALIZE_BASE.toBits()

/**
 * Copied this from kotlinx serialization
 *
 * https://github.com/Kotlin/kotlinx.serialization/blob/master/formats/cbor/commonMain/src/kotlinx/serialization/cbor/internal/Encoding.kt#L665
 * For details about half-precision floating-point numbers see https://tools.ietf.org/html/rfc7049#appendix-D
 */
internal fun floatFromHalfBits(bits: Short): Float {
    val intBits = bits.toInt()

    val negative = (intBits and 0x8000) != 0
    val halfExp = intBits shr 10 and HALF_PRECISION_MAX_EXPONENT
    val halfMant = intBits and HALF_PRECISION_MAX_MANTISSA

    val exp: Int
    val mant: Int

    when (halfExp) {
        HALF_PRECISION_MAX_EXPONENT -> {
            // if exponent maximal - value is NaN or Infinity
            exp = SINGLE_PRECISION_MAX_EXPONENT
            mant = halfMant
        }

        0 -> {
            if (halfMant == 0) {
                // if exponent and mantissa are zero - value is zero
                mant = 0
                exp = 0
            } else {
                // if exponent is zero and mantissa non-zero - value denormalized. normalize it
                var res = Float.fromBits(normalizeBaseBits + halfMant)
                res -= SINGLE_PRECISION_NORMALIZE_BASE
                return if (negative) -res else res
            }
        }

        else -> {
            // normalized value
            exp = (halfExp + (SINGLE_PRECISION_EXPONENT_BIAS - HALF_PRECISION_EXPONENT_BIAS))
            mant = halfMant
        }
    }

    val res = Float.fromBits((exp shl 23) or (mant shl 13))
    return if (negative) -res else res
}

// https://stackoverflow.com/questions/6162651/half-precision-floating-point-in-java
internal fun halfBitsFromFloat(number: Float): Short {
    val bits = number.toRawBits()
    val sign = bits ushr 16 and 0x8000
    val value = (bits and 0x7fffffff) + 0x1000
    val result =
        when {
            value >= 0x47800000 -> {
                if (bits and 0x7fffffff >= 0x47800000) {
                    if (value < 0x7f800000) {
                        sign or 0x7c00
                    } else {
                        sign or 0x7c00 or (bits and 0x007fffff ushr 13)
                    }
                } else {
                    sign or 0x7bff
                }
            }
            value >= 0x38800000 -> sign or (value - 0x38000000 ushr 13)
            value < 0x33000000 -> sign
            else -> {
                val otherValue = bits and 0x7fffffff ushr 23
                sign or (
                    (
                        (bits and 0x7fffff or 0x800000) +
                            (0x800000 ushr otherValue - 102)
                    ) ushr 126 - otherValue
                )
            }
        }

    return result.toShort()
}
