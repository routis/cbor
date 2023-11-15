package io.github.routis.cbor

import io.github.routis.cbor.internal.readCbor
import okio.Buffer
import kotlin.io.encoding.Base64


fun decodeBase64UrlSafe(source: String): DataItem {
    val bytes = Base64.UrlSafe.decode(source.toByteArray())
    return decode(bytes)
}

fun decode(bytes: ByteArray): DataItem =
        Buffer().use { buffer ->
            buffer.write(bytes)
            buffer.readCbor()
        }

