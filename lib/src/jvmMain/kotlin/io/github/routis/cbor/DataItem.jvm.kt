package io.github.routis.cbor

import java.math.BigInteger

actual fun DataItem.Integer.Unsigned.asNumber(): Number = BigInteger(value.toString())
actual fun DataItem.Integer.Negative.asNumber(): Number = BigInteger(value.toString()).asNegative()
actual fun DataItem.Tagged.BigNumUnsigned.asNumber(): Number = content.asBigInteger()
actual fun DataItem.Tagged.BigNumNegative.asNumber(): Number = content.asBigInteger().asNegative()
private fun DataItem.ByteString.asBigInteger(): BigInteger = BigInteger(bytes)
private fun BigInteger.asNegative(): BigInteger = -BigInteger.ONE - this
