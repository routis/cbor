package io.github.routis.cbor


expect fun DataItem.Integer.Negative.asNumber(): Number
expect fun DataItem.Tagged.BigNumUnsigned.asNumber(): Number
expect fun DataItem.Tagged.BigNumNegative.asNumber(): Number