package io.github.routis.cbor.internal

import io.github.routis.cbor.DataItem

sealed interface Tag {


    data object StandardDateTimeString : Tag
    data object EpochBasedDateTime : Tag
    data object BigNumUnsigned : Tag
    data object BigNumNegative : Tag
    data object DecimalFraction : Tag
    data object BigFloat : Tag
    data object ToBase64Url : Tag
    data object ToBase64 : Tag
    data object ToBase16 : Tag
    data object CborDataItem : Tag
    data object Uri : Tag
    data object Base64Url : Tag
    data object Base64 : Tag
    data object Regex : Tag
    data object Mime : Tag
    data object SelfDescribedCbor : Tag
    data object FullDate : Tag

    companion object {
        fun of(x: ULong): Tag? = when (x) {
            0uL -> StandardDateTimeString
            1uL -> EpochBasedDateTime
            2uL -> BigNumUnsigned
            3uL -> BigNumNegative
            4uL -> DecimalFraction
            5uL -> BigFloat
            21uL -> ToBase64Url
            22uL -> ToBase64
            23uL -> ToBase16
            24uL -> CborDataItem
            32uL -> Uri
            33uL -> Base64Url
            34uL -> Base64
            35uL -> Regex
            36uL -> Mime
            55799uL -> SelfDescribedCbor
            1004uL -> FullDate
            else -> null
        }
    }
}

internal fun Tag.value(): ULong = when (this) {
    Tag.StandardDateTimeString -> 0uL
    Tag.EpochBasedDateTime -> 1uL
    Tag.BigNumUnsigned -> 2uL
    Tag.BigNumNegative -> 3uL
    Tag.DecimalFraction -> 4uL
    Tag.BigFloat -> 5uL
    Tag.ToBase64Url -> 21uL
    Tag.ToBase64 -> 22uL
    Tag.ToBase16 -> 23uL
    Tag.CborDataItem -> 24uL
    Tag.Uri -> 32uL
    Tag.Base64Url -> 33uL
    Tag.Base64 -> 34uL
    Tag.Regex -> 35uL
    Tag.Mime -> 36uL
    Tag.SelfDescribedCbor -> 55799uL
    Tag.FullDate -> 1004.toULong()
}


internal fun Tag.withItem(dataItem: DataItem): DataItem.Tagged<DataItem> = when (this) {
    Tag.StandardDateTimeString -> DataItem.Tagged.StandardDateTimeString(expected(dataItem))
    Tag.EpochBasedDateTime -> when (dataItem) {
        is DataItem.UnsignedInteger -> DataItem.Tagged.EpochBasedDateTime.Unsigned(dataItem)
        is DataItem.NegativeInteger -> DataItem.Tagged.EpochBasedDateTime.Negative(dataItem)
        is DataItem.HalfPrecisionFloat -> DataItem.Tagged.EpochBasedDateTime.HalfFloat(dataItem)
        is DataItem.SinglePrecisionFloat -> DataItem.Tagged.EpochBasedDateTime.SingleFloat(dataItem)
        is DataItem.DoublePrecisionFloat -> DataItem.Tagged.EpochBasedDateTime.DoubleFloat(dataItem)
        else -> error("$dataItem is not valid for ${this}. Expecting integer or float")
    }

    Tag.BigNumUnsigned -> DataItem.Tagged.BigNumUnsigned(expected(dataItem))
    Tag.BigNumNegative -> DataItem.Tagged.BigNumNegative(expected(dataItem))
    Tag.DecimalFraction -> exponentAndMantissa(dataItem) { e, m -> DataItem.Tagged.DecimalFraction(e, m) }
    Tag.BigFloat -> exponentAndMantissa(dataItem) { e, m -> DataItem.Tagged.BigFloat(e, m) }
    Tag.ToBase16 -> DataItem.Tagged.Unassigned(value(), dataItem) // TODO
    Tag.ToBase64 -> DataItem.Tagged.Unassigned(value(), dataItem)// TODO
    Tag.ToBase64Url -> DataItem.Tagged.Unassigned(value(), dataItem)// TODO
    Tag.CborDataItem -> DataItem.Tagged.DborDataItem(expected(dataItem))
    Tag.SelfDescribedCbor -> DataItem.Tagged.SelfDescribedCbor(dataItem)
    Tag.Uri -> DataItem.Tagged.EncodedText.Uri(expected(dataItem))
    Tag.Base64Url -> DataItem.Tagged.EncodedText.Base64Url(expected(dataItem))
    Tag.Base64 -> DataItem.Tagged.EncodedText.Base64(expected(dataItem))
    Tag.Regex -> DataItem.Tagged.EncodedText.Regex(expected(dataItem))
    Tag.Mime -> DataItem.Tagged.StandardDateTimeString(expected(dataItem))
    Tag.FullDate -> DataItem.Tagged.FullDateTime(expected(dataItem))
}

private inline fun <reified DI : DataItem> Tag.expected(di: DataItem): DI {
    require(di is DI) { "$di is not valid for $this. Expecting ${DI::class.java}" }
    return di
}

private fun <DI> Tag.exponentAndMantissa(dataItem: DataItem, cons: (DataItem.Integer, DataItem.Integer) -> DI): DI {
    val array = expected<DataItem.Array>(dataItem)
    require(array.size == 2) { "Array must have 2 elements" }
    return cons(expected(array[0]), expected(array[1]))

}