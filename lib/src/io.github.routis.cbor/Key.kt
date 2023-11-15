package io.github.routis.cbor

sealed interface Key<out DI: DataItem> {
    val value: DI
    @JvmInline
    value class BoolKey(override val value: DataItem.Bool) : Key<DataItem.Bool>
    @JvmInline
    value class BytesKey(override val value: DataItem.Bytes) : Key<DataItem.Bytes>
    @JvmInline
    value class PosIntKey(override val value: DataItem.PositiveInteger) : Key<DataItem.PositiveInteger>
    @JvmInline
    value class NegIntKey(override val value: DataItem.NegativeInteger) : Key<DataItem.NegativeInteger>
    @JvmInline
    value class TextKey(override val value: DataItem.Text) : Key<DataItem.Text>

    companion object{
        internal operator fun <DI : DataItem>invoke(di: DI): Result<Key<DataItem>> = runCatching {
            when(di) {
                is DataItem.Bool -> BoolKey(di)
                is DataItem.Bytes -> BytesKey(di)
                is DataItem.PositiveInteger -> PosIntKey(di)
                is DataItem.NegativeInteger -> NegIntKey(di)
                is DataItem.Text -> TextKey(di)
                else -> error("$di cannot be used as key")
            }
        }

    }
}

