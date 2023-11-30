package io.github.routis.cbor

import java.math.BigInteger


actual fun DataItem.Integer.Negative.asNumber(): Number = -BigInteger.ONE - BigInteger(value.toString())
actual fun DataItem.Tagged.BigNumUnsigned.asNumber(): Number = content.asBigInteger() // TODO this is java specific
actual fun DataItem.Tagged.BigNumNegative.asNumber(): Number = -BigInteger.ONE - content.asBigInteger() // TODO this is java specific
private fun DataItem.ByteString.asBigInteger(): BigInteger = BigInteger(bytes) // TODO this is java specific