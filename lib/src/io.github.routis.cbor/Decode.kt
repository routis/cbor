package io.github.routis.cbor

import io.github.routis.cbor.internal.readDataItem
import okio.Buffer
import kotlin.io.encoding.Base64


fun decodeBase64UrlSafe(source: String): DataItem {
    val bytes = Base64.UrlSafe.decode(source.toByteArray())
    return decode(bytes)
}

fun decode(bytes: ByteArray): DataItem {
    return Buffer().use { buffer ->
        buffer.write(bytes)
        buffer.readDataItem()
    }
}