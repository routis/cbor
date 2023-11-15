package io.github.routis.cbor

sealed interface Tag {
    
    data object FullDate: Tag
    data object DateTime : Tag
    data object Timestamp : Tag
    data object BigNum : Tag
    data object NegativeBigNum : Tag
    data object Decimal : Tag
    data object BigFloat : Tag
    data class Unassigned(val value: ULong) : Tag
    data object ToBase64Url : Tag
    data object ToBase64 : Tag
    data object ToBase16 : Tag
    data object Cbor : Tag
    data object Uri : Tag
    data object Base64Url : Tag
    data object Base64 : Tag
    data object Regex : Tag
    data object Mime : Tag
    data object CborSelf : Tag

    companion object {
        fun of(x: ULong): Tag = when (x) {
            0uL -> DateTime
            1uL -> Timestamp
            2uL -> BigNum
            3uL -> NegativeBigNum
            4uL -> Decimal
            5uL -> BigFloat
            21uL -> ToBase64Url
            22uL -> ToBase64
            23uL -> ToBase16
            24uL -> Cbor
            32uL -> Uri
            33uL -> Base64Url
            34uL -> Base64
            35uL -> Regex
            36uL -> Mime
            55799uL -> CborSelf
            1004uL-> FullDate
            else -> Unassigned(x)
        }
    }
}

fun Tag.value(): ULong = when (this) {
    Tag.DateTime -> 0uL
    Tag.Timestamp -> 1uL
    Tag.BigNum -> 2uL
    Tag.NegativeBigNum -> 3uL
    Tag.Decimal -> 4uL
    Tag.BigFloat -> 5uL
    Tag.ToBase64Url -> 21uL
    Tag.ToBase64 -> 22uL
    Tag.ToBase16 -> 23uL
    Tag.Cbor -> 24uL
    Tag.Uri -> 32uL
    Tag.Base64Url -> 33uL
    Tag.Base64 -> 34uL
    Tag.Regex -> 35uL
    Tag.Mime -> 36uL
    Tag.CborSelf -> 55799uL
    is Tag.Unassigned -> value
    Tag.FullDate -> 1004.toULong()
}