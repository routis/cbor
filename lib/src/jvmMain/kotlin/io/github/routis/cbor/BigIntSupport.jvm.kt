package io.github.routis.cbor

import java.math.BigInteger

internal actual fun bigIntSupport(): BigIntSupport<Number> = JvmBigIntSupport

private object JvmBigIntSupport: BigIntSupport<BigInteger> {
    private const val HEX_RADIX = 16
    override fun unsignedFrom(hex: String): BigInteger = BigInteger(hex, HEX_RADIX)
    override fun negativeFrom(hex: String): BigInteger = -(BigInteger.ONE + unsignedFrom(hex))
}
