package io.github.routis.cbor

sealed interface DataItem {

    @JvmInline
    value class PositiveInteger(val value: ULong) : DataItem

    @JvmInline
    value class NegativeInteger(val value: Long): DataItem{
        init {
            require(value <= -1) {"Should -1 or lower. Was $value"}
        }
    }

    @JvmInline
    value class Float32(val value: Float) : DataItem

    @JvmInline
    value class Float64(val value: Double) : DataItem

    @JvmInline
    value class Text(val value: String) : DataItem

    data object Break : DataItem
    data object Null : DataItem
    data object Undefined : DataItem

    @JvmInline
    value class Bool(val value: Boolean) : DataItem

    sealed interface SimpleType : DataItem {
        @JvmInline
        value class Unassigned(val value: UByte) : SimpleType

        @JvmInline
        value class Reserved(val value: UByte) : SimpleType
    }

    @JvmInline
    value class Bytes(val value: UByteArray) : DataItem

    sealed interface Tagged<out DI: DataItem> : DataItem{

        val value: DI
        data class Cbor(override val value: Bytes): Tagged<Bytes>
        data class DateTime(override val value: Text): Tagged<Text>
        data class FullDateTime(override val value: Text): Tagged<Text>
        data class Base64(override val value: Text): Tagged<Text>
        data class Base64Url(override val value: Text): Tagged<Text>
        data class Regex(override val value: Text): Tagged<Text>
        data class Mime(override val value: Text): Tagged<Text>
        data class Uri(override val value: Text): Tagged<Text>
        data class Unassigned(val tag: Tag, override val value: DataItem): Tagged<DataItem>
    }

    @JvmInline
    value class CborArray(private val items: List<DataItem>) : DataItem, List<DataItem> by items

    @JvmInline
    value class CborMap(private val items: Map<Key<*>, DataItem>) : DataItem, Map<Key<*>, DataItem> by items
}

fun <C: DataItem> tag(tag: Tag, value: C): DataItem.Tagged<DataItem> = when(tag){
    Tag.Base64 -> DataItem.Tagged.Base64(value as DataItem.Text)
    Tag.Base64Url -> DataItem.Tagged.Base64Url(value as DataItem.Text)
    Tag.BigFloat -> DataItem.Tagged.Unassigned(tag, value)
    Tag.BigNum -> DataItem.Tagged.Unassigned(tag, value)
    Tag.Cbor -> DataItem.Tagged.Cbor(value as DataItem.Bytes)
    Tag.CborSelf -> DataItem.Tagged.Unassigned(tag, value)
    Tag.DateTime -> DataItem.Tagged.DateTime(value as DataItem.Text)
    Tag.Decimal -> DataItem.Tagged.Unassigned(tag, value)
    Tag.Mime -> DataItem.Tagged.DateTime(value as DataItem.Text)
    Tag.NegativeBigNum -> TODO("NegativeBigNu,")
    Tag.Regex -> DataItem.Tagged.Regex(value as DataItem.Text)
    Tag.Timestamp -> DataItem.Tagged.Unassigned(tag, value)
    Tag.ToBase16 -> DataItem.Tagged.Unassigned(tag, value)
    Tag.ToBase64 -> DataItem.Tagged.Unassigned(tag, value)
    Tag.ToBase64Url -> DataItem.Tagged.Unassigned(tag, value)
    is Tag.Unassigned -> DataItem.Tagged.Unassigned(tag, value)
    Tag.Uri -> DataItem.Tagged.Uri(value as DataItem.Text)
    Tag.FullDate -> DataItem.Tagged.FullDateTime(value as DataItem.Text)
}
