package io.github.routis.cbor

sealed interface Key<out DI : DataItem> {
    val item: DI

    @JvmInline
    value class BoolKey(override val item: DataItem.Bool) : Key<DataItem.Bool>

    @JvmInline
    value class ByteStringKey(override val item: DataItem.ByteString) : Key<DataItem.ByteString>

    @JvmInline
    value class UnsignedIntegerKey(override val item: DataItem.UnsignedInteger) : Key<DataItem.UnsignedInteger>

    @JvmInline
    value class NegativeIntegerKey(override val item: DataItem.NegativeInteger) : Key<DataItem.NegativeInteger>

    @JvmInline
    value class TextStringKey(override val item: DataItem.TextString) : Key<DataItem.TextString>

    companion object {
        operator fun invoke(di: DataItem): Result<Key<DataItem>> = runCatching {
            when (di) {
                is DataItem.Bool -> BoolKey(di)
                is DataItem.ByteString -> ByteStringKey(di)
                is DataItem.UnsignedInteger -> UnsignedIntegerKey(di)
                is DataItem.NegativeInteger -> NegativeIntegerKey(di)
                is DataItem.TextString -> TextStringKey(di)
                else -> error("$di cannot be used as key")
            }
        }

    }
}

