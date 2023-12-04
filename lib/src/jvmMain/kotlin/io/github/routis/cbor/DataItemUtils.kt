@file:JvmName("DataItemUtils")

package io.github.routis.cbor

import kotlinx.io.*
import java.math.BigInteger

fun BigInteger.asIntegerDataItem(): DataItem.Integer =  integerFrom(this)

@Throws(IllegalArgumentException::class)
fun integerFrom(i: BigInteger): DataItem.Integer {
    val isZeroOrMore = i >= BigInteger.ZERO
    val normalized = if (isZeroOrMore) i else - (BigInteger.ONE + i)
    val bytes = normalized.toByteArray()
    return DataItem.Integer.integerFrom(isZeroOrMore, bytes)
}