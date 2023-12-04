@file:JvmName("DataItemUtils")

package io.github.routis.cbor

import kotlinx.io.*
import java.math.BigInteger

fun BigInteger.asIntegerDataItem(): DataItem.Integer =  integerFrom(this)

/**
 * Creates a [DataItem.Integer] from a [big integer]i]
 * @return either a [DataItem.Integer.Unsigned] in case the given [i] is zero or more
 * or [DataItem.Integer.Negative] otherwise
 * @throws IllegalArgumentException in case the input is outside the range -2^64..2^64-1 inclusive
 */
@Throws(IllegalArgumentException::class)
fun integerFrom(i: BigInteger): DataItem.Integer {
    val isZeroOrMore = i >= BigInteger.ZERO
    val normalized = if (isZeroOrMore) i else - (BigInteger.ONE + i)
    val bytes = normalized.toByteArray()
    return DataItem.Integer.integerFrom(isZeroOrMore, bytes)
}