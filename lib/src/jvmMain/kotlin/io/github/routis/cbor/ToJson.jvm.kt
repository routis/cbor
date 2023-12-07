package io.github.routis.cbor

import java.math.BigInteger

actual fun bigIntSupport(): BigIntSupport<Number> = object : BigIntSupport<BigInteger> {

    override fun unsignedFrom(hex: String): BigInteger = BigInteger(hex, 16)

    override fun negativeFrom(hex: String): BigInteger = - BigInteger.ONE - unsignedFrom(hex)
}