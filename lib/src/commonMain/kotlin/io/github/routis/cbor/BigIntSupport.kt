package io.github.routis.cbor

internal interface BigIntSupport<out T> {
    fun unsignedFrom(hex: String): T

    fun negativeFrom(hex: String): T
}

internal fun <T> BigIntSupport<T>.unsignedFrom(bytes: ByteArray): T = unsignedFrom(bytes.toHexString())

internal fun <T> BigIntSupport<T>.negativeFrom(bytes: ByteArray): T = negativeFrom(bytes.toHexString())

internal fun <T> BigIntSupport<T>.negativeFrom(value: ULong): T = negativeFrom(value.toHexString())

internal expect fun bigIntSupport(): BigIntSupport<Number>
