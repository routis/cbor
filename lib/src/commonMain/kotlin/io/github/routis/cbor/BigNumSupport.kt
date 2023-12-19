package io.github.routis.cbor

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign

fun IntegerDataItem.asBigInteger(): BigInteger =
    when (this) {
        is UnsignedIntegerDataItem -> BigInteger.fromULong(value)
        is NegativeIntegerDataItem -> BigInteger.fromULong(value).toCborNegative()
    }

fun BigNumUnsigned.asBigInteger(): BigInteger = BigInteger.fromByteString(content)

fun BigNumNegative.asBigInteger(): BigInteger = BigInteger.fromByteString(content).toCborNegative()

private fun BigInteger.Companion.fromByteString(byteString: ByteStringDataItem) = fromByteArray(byteString.bytes, Sign.POSITIVE)

private fun BigInteger.toCborNegative(): BigInteger {
    check(this >= BigInteger.ZERO) { "Applicable to integers greater equal to zero" }
    return -(BigInteger.ONE + this)
}
