package io.github.routis.cbor

fun DataItem.Integer.Unsigned.negate() = unaryMinus()
expect operator fun DataItem.Integer.Unsigned.unaryMinus(): DataItem.Integer.Negative
expect fun DataItem.Integer.Negative.asNumber(): Number
expect fun DataItem.Tagged.BigNumUnsigned.asNumber(): Number
expect fun DataItem.Tagged.BigNumNegative.asNumber(): Number