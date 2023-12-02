package io.github.routis.cbor;

import kotlinx.serialization.json.JsonPrimitive;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;


public class DecoderTests {

    @DisplayName("Single byte Unsigned Integer")
    @Test
    void happyPath() {
        var value = BigInteger.valueOf(10);
        byte[] bytes = value.toByteArray();
        var dataItem = assertDoesNotThrow(() -> Decoder.decode(bytes));
        var expectedDataItem = DataItemUtils.integerFrom(value);
        assertEquals(expectedDataItem, dataItem);
        var json = JsonConverter.toJson(dataItem);
        assertInstanceOf(JsonPrimitive.class, json);
        var jsonPrimitive = (JsonPrimitive) json;
        assertEquals("" + value, jsonPrimitive.getContent());
    }

    @DisplayName("Single byte Negative Integer")
    @Test
    void happyPath2() {

        var dataItem = DataItemUtils.integerFrom(BigInteger.valueOf(-500));
        var json = JsonConverter.toJson(dataItem);
        assertInstanceOf(JsonPrimitive.class, json);
        var jsonPrimitive = (JsonPrimitive) json;
        assertEquals("-500", jsonPrimitive.getContent());

    }

}
