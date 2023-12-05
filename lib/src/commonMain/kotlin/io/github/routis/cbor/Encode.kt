@file:JvmName("Encoder")

package io.github.routis.cbor

import io.github.routis.cbor.internal.writeDataItem
import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlinx.io.readByteArray

/**
 * Encodes the given [dataItem] into a [ByteArray] according to CBOR
 *
 * The implementation doesn't support indefinite size (aka streaming)
 * for lists, maps or other types.
 *
 * @param dataItem the item to encode
 * @return the byte representation of the item in CBOR
 *
 * @throws IOException if something goes wrong with IO
 */
@Throws(IOException::class)
fun encode(dataItem: DataItem): ByteArray {
    return Buffer().use { buffer ->
        buffer.writeDataItem(dataItem)
        buffer.readByteArray()
    }
}