package io.github.routis.cbor

sealed interface Key<out DI : DataItem> {
    val item: DI

    @JvmInline
    value class BoolKey(override val item: DataItem.Bool) : Key<DataItem.Bool>

    @JvmInline
    value class ByteStringKey(override val item: DataItem.ByteString) : Key<DataItem.ByteString>

    @JvmInline
    value class IntegerKey(override val item: DataItem.Integer) : Key<DataItem.Integer>


    @JvmInline
    value class TextStringKey(override val item: DataItem.TextString) : Key<DataItem.TextString>

    companion object {
        operator fun invoke(di: DataItem): Result<Key<DataItem>> = runCatching {
            when (di) {
                is DataItem.Bool -> BoolKey(di)
                is DataItem.ByteString -> ByteStringKey(di)
                is DataItem.Integer.Unsigned -> IntegerKey(di)
                is DataItem.Integer.Negative -> IntegerKey(di)
                is DataItem.TextString -> TextStringKey(di)
                else -> error("$di cannot be used as key")
            }
        }

    }
}

