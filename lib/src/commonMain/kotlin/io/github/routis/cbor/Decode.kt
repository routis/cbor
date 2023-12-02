@file:JvmName("Decoder")

package io.github.routis.cbor

import io.github.routis.cbor.internal.readDataItem
import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlin.io.encoding.Base64


@Throws(IllegalStateException::class, IOException::class)
fun decodeBase64UrlSafe(source: String): DataItem {
    val bytes = Base64.UrlSafe.decode(source)
    return decode(bytes)
}


@Throws(IllegalStateException::class, IOException::class)
fun decode(bytes: ByteArray): DataItem {
    return Buffer().use { buffer ->
        buffer.write(bytes)
        buffer.readDataItem()
    }
}