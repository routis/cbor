package io.github.routis.cbor.internal

import io.github.routis.cbor.*

internal sealed interface Tag {
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

    data object CalendarDay : Tag

    data object CalendarDate : Tag

    companion object {
        fun of(x: ULong): Tag? =
            when (x) {
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
                100uL -> CalendarDay
                1004uL -> CalendarDate
                else -> null
            }
    }
}

internal fun Tag.value(): ULong =
    when (this) {
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
        Tag.CalendarDay -> 100uL
        Tag.CalendarDate -> 1004uL
    }

internal fun TaggedDataItem<*>.tagValue(): ULong =
    when (this) {
        is StandardDateTimeDataItem -> Tag.StandardDateTimeString.value()
        is EpochBasedDateTime<*> -> Tag.EpochBasedDateTime.value()
        is BigNumUnsigned -> Tag.BigNumUnsigned.value()
        is BigNumNegative -> Tag.BigNumNegative.value()
        is DecimalFraction -> Tag.DecimalFraction.value()
        is BigFloat -> Tag.BigFloat.value()
        is Base64DataItem -> Tag.Base64.value()
        is Base64UrlDataItem -> Tag.Base64Url.value()
        is MimeDataItem -> Tag.Mime.value()
        is RegexDataItem -> Tag.Regex.value()
        is UriDataItem -> Tag.Uri.value()
        is CborDataItem -> Tag.CborDataItem.value()
        is SelfDescribedCbor -> Tag.SelfDescribedCbor.value()
        is UnsupportedTagDataItem -> tag
        is CalendarDayDataItem -> Tag.CalendarDay.value()
        is CalendarDateDataItem -> Tag.CalendarDate.value()
    }

internal fun DataItem.tagged(tag: Tag): TaggedDataItem<DataItem> {
    val dataItem = this
    return with(tag) {
        when (this) {
            Tag.StandardDateTimeString -> StandardDateTimeDataItem(expected(dataItem))
            Tag.EpochBasedDateTime ->
                when (dataItem) {
                    is IntegerDataItem -> IntegerEpochDataItem(dataItem)
                    is HalfPrecisionFloatDataItem -> HalfFloatEpochDataItem(dataItem)
                    is SinglePrecisionFloatDataItem -> SingleFloatEpochDataItem(dataItem)
                    is DoublePrecisionFloatDataItem -> DoubleFloatEpochDataItem(dataItem)
                    else -> error("$dataItem is not valid for $this. Expecting integer or float")
                }

            Tag.BigNumUnsigned -> BigNumUnsigned(expected(dataItem))
            Tag.BigNumNegative -> BigNumNegative(expected(dataItem))
            Tag.DecimalFraction -> exponentAndMantissa(dataItem) { e, m -> DecimalFraction(e, m) }
            Tag.BigFloat -> exponentAndMantissa(dataItem) { e, m -> BigFloat(e, m) }
            Tag.ToBase16 -> UnsupportedTagDataItem(value(), dataItem) // TODO
            Tag.ToBase64 -> UnsupportedTagDataItem(value(), dataItem) // TODO
            Tag.ToBase64Url -> UnsupportedTagDataItem(value(), dataItem) // TODO
            Tag.CborDataItem -> CborDataItem(expected(dataItem))
            Tag.SelfDescribedCbor -> SelfDescribedCbor(dataItem)
            Tag.Uri -> UriDataItem(expected(dataItem))
            Tag.Base64Url -> Base64UrlDataItem(expected(dataItem))
            Tag.Base64 -> Base64DataItem(expected(dataItem))
            Tag.Regex -> RegexDataItem(expected(dataItem))
            Tag.Mime -> MimeDataItem(expected(dataItem))
            Tag.CalendarDay -> CalendarDayDataItem(expected(dataItem))
            Tag.CalendarDate -> CalendarDateDataItem(expected(dataItem))
        }
    }
}

private inline fun <reified DI : DataItem> Tag.expected(dataItem: DataItem): DI {
    require(dataItem is DI) { "$dataItem is not valid for $this. Expecting ${DI::class}" }
    return dataItem
}

private fun <DI> Tag.exponentAndMantissa(
    dataItem: DataItem,
    cons: (IntegerDataItem, IntegerDataItem) -> DI,
): DI {
    val array = expected<ArrayDataItem>(dataItem)
    require(array.size == 2) { "Array must have 2 elements" }
    return cons(expected(array[0]), expected(array[1]))
}
