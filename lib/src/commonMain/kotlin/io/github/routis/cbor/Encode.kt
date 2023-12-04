package io.github.routis.cbor

import io.github.routis.cbor.internal.writeDataItem
import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlinx.io.readByteArray

@Throws(IllegalStateException::class, IOException::class)
fun encode(dataItem: DataItem): ByteArray {
    return Buffer().use { buffer ->
        buffer.writeDataItem(dataItem)
        buffer.readByteArray()
    }
}