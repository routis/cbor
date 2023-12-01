package io.github.routis.cbor

import kotlinx.serialization.json.Json

val jsonSupport = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
}