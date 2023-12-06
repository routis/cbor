@file:JvmName("Decoder")

package io.github.routis.cbor

import io.github.routis.cbor.internal.readDataItem
import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlin.io.encoding.Base64

/**
 * Decodes the given [source] into [DataItem]
 *
 * @param source the CBOR bytes Base64, URL safe, encoded.
 *
 * @throws IOException if something goes wrong with IO
 * @throws IllegalStateException if the given input is invalid
 */
@Throws(IllegalStateException::class, IOException::class)
fun decodeBase64UrlSafe(source: String): DataItem {
    val bytes = Base64.UrlSafe.decode(source)
    return decode(bytes)
}

/**
 * Decodes the given [bytes] into [DataItem]
 *
 * @param bytes input to decode.
 *
 * @throws IOException if something goes wrong with IO
 * @throws IllegalStateException if the given input is invalid
 */
@Throws(IllegalStateException::class, IOException::class)
fun decode(bytes: ByteArray): DataItem {
    return Buffer().use { buffer ->
        buffer.write(bytes)
        buffer.readDataItem()
    }
}
