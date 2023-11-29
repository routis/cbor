package io.github.routis.cbor

/**
 * [Data items][DataItem] that can be used as keys in a [CBOR map][DataItem.CborMap]
 * @param DI The type of the data item
 */
sealed interface Key<out DI : DataItem> {
    /**
     * The data item of the key
     */
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

        /**
         * Creates a [Key] for the given [dataItem]
         * In case the [dataItem] cannot be used as a kay a `null` is being returned.
         */
        operator fun invoke(dataItem: DataItem): Key<DataItem>? =
                when (dataItem) {
                    is DataItem.Bool -> BoolKey(dataItem)
                    is DataItem.ByteString -> ByteStringKey(dataItem)
                    is DataItem.Integer.Unsigned -> IntegerKey(dataItem)
                    is DataItem.Integer.Negative -> IntegerKey(dataItem)
                    is DataItem.TextString -> TextStringKey(dataItem)
                    else -> null
                }

    }
}

