package io.github.routis.cbor

/**
 * [Data items][DataItem] that can be used as keys in a [CBOR map][MapDataItem]
 * @param DI The type of the data item
 */
sealed interface Key<out DI : DataItem> {
    /**
     * The data item of the key
     */
    val item: DI
}

data class BoolKey(override val item: BooleanDataItem) : Key<BooleanDataItem>

data class ByteStringKey(override val item: ByteStringDataItem) : Key<ByteStringDataItem>

data class IntegerKey(override val item: IntegerDataItem) : Key<IntegerDataItem>

data class TextStringKey(override val item: TextStringDataItem) : Key<TextStringDataItem>

/**
 * Creates a [Key] for the given [dataItem]
 * In case the [dataItem] cannot be used as a kay a `null` is being returned.
 */
fun keyOf(dataItem: DataItem): Key<DataItem>? =
    when (dataItem) {
        is BooleanDataItem -> BoolKey(dataItem)
        is ByteStringDataItem -> ByteStringKey(dataItem)
        is UnsignedIntegerDataItem -> IntegerKey(dataItem)
        is NegativeIntegerDataItem -> IntegerKey(dataItem)
        is TextStringDataItem -> TextStringKey(dataItem)
        else -> null
    }
