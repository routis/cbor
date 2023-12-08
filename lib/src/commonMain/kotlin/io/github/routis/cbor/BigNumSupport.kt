package io.github.routis.cbor

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign

fun DataItem.Integer.asBigInteger(): BigInteger =
    when (this) {
        is DataItem.Integer.Unsigned -> BigInteger.fromULong(value)
        is DataItem.Integer.Negative -> BigInteger.fromULong(value).toCborNegative()
    }

fun DataItem.Tagged.BigNumUnsigned.asBigInteger(): BigInteger = BigInteger.fromByteString(content)

fun DataItem.Tagged.BigNumNegative.asBigInteger(): BigInteger = BigInteger.fromByteString(content).toCborNegative()

private fun BigInteger.Companion.fromByteString(byteString: DataItem.ByteString) = fromByteArray(byteString.bytes, Sign.POSITIVE)

private fun BigInteger.toCborNegative(): BigInteger {
    check(this >= BigInteger.ZERO) { "Applicable to integers greater equal to zero" }
    return -(BigInteger.ONE + this)
}
