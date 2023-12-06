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
    ULong.MAX_VALUE
    val isZeroOrMore = i >= BigInteger.ZERO
    val normalized = if (isZeroOrMore) i else - (BigInteger.ONE + i)
    val bytes = normalized.toByteArray()
    return integerFrom(isZeroOrMore, bytes)
}

/**
 * Creates a [DataItem.Integer]
 *
 * @param isZeroOrMore indicates whether the [DataItem.Integer] would be
 * [positive][DataItem.Integer.Unsigned] or [negative][DataItem.Integer.Negative]
 * @param bytes an array of 1, 2, 4, or 8 bytes representing [UByte], [UShort], [UInt]
 * or [ULong] respectively
 * @return the [DataItem.Integer] representing the given value
 * @throws IllegalArgumentException in case the [bytes] represent
 * a number outside the range 0..2^64-1 inclusive
 */
private fun integerFrom(isZeroOrMore: Boolean, bytes: ByteArray) : DataItem.Integer {
    val value = Buffer().use { buffer ->
        buffer.write(bytes)
        when (bytes.size) {
            1 -> buffer.readUByte().toULong()
            2 -> buffer.readUShort().toULong()
            4 -> buffer.readUInt().toULong()
            8 -> buffer.readULong()
            else -> error("Array should contain 1, 2, 4 or 8 bytes but was ${bytes.size}")
        }
    }
    return if (isZeroOrMore) DataItem.Integer.Unsigned(value) else DataItem.Integer.Negative(value)
}