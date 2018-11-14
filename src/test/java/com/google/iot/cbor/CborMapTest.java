/*
 * Copyright (C) 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.iot.cbor;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
public class CborMapTest extends CborTestBase {
    private static final boolean DEBUG = false;
    private static final Logger LOGGER = Logger.getLogger(CborMapTest.class.getCanonicalName());

    @Test
    void testCreateFromCborByteArray() throws Exception {
        byte[] array = decode("bf00a0ff");
        CborObject obj;

        obj = CborMap.createFromCborByteArray(array);
        assertEquals("{0:{}}", obj.toString());

        obj = CborMap.createFromCborByteArray(array, 2, array.length - 3);
        assertEquals("{}", obj.toString());

        assertThrows(
                CborParseException.class,
                () -> CborMap.createFromCborByteArray(array, 2, array.length - 2));
        assertThrows(
                CborParseException.class,
                () -> CborMap.createFromCborByteArray(array, 1, array.length - 1));
        assertThrows(
                CborParseException.class,
                () -> CborMap.createFromCborByteArray(array, 3, array.length - 3));

        assertThrows(
                IndexOutOfBoundsException.class,
                () -> CborMap.createFromCborByteArray(array, 40, 1));
    }

    @Test
    void testCorruptedMap() {
        assertThrows(
                CborParseException.class,
                () -> CborObject.createFromCborByteArray(decode("bf6346756ef563416d742100c0ff")));
    }

    @Test
    void testParser2() {
        byte[] array = decode("a26161016162820203");

        String output = "{\"a\":1,\"b\":[2,3]}";

        CborMap obj = (CborMap) assertParseToString(output, array);

        byte[] encoded = obj.toCborByteArray();

        assertArrayEquals(array, encoded);

        assertEquals(2, obj.size());
        assertFalse(obj.isEmpty());
        assertTrue(obj.isValidJson());
    }

    @Test
    void testTaggedMap() {
        byte[] array = decode("d9d9f7a0");

        String output = "55799({})";

        CborMap obj = (CborMap) assertParseToString(output, array);

        byte[] encoded = obj.toCborByteArray();

        assertArrayEquals(array, encoded);

        assertEquals(0, obj.size());
        assertEquals(CborTag.SELF_DESCRIBE_CBOR, obj.getTag());
        assertTrue(obj.isEmpty());
        assertTrue(obj.isValidJson());
    }

    @Test
    void testParser3() {
        byte[] array = decode("a0");

        String output = "{}";

        CborMap obj = (CborMap) assertParseToString(output, array);

        byte[] encoded = obj.toCborByteArray();

        assertArrayEquals(array, encoded);

        assertEquals(0, obj.size());
        assertTrue(obj.isEmpty());
        assertTrue(obj.isValidJson());
    }

    @Test
    void testParser4() {
        byte[] array = decode("a201020304");

        String output = "{1:2,3:4}";

        CborMap obj = (CborMap) assertParseToString(output, array);

        byte[] encoded = obj.toCborByteArray();

        assertArrayEquals(array, encoded);
    }

    @Test
    void testParser8() throws Exception {
        byte[] array = decode("bf6346756ef563416d7421ff");

        String output = "{\"Fun\":false,\"Amt\":-2}";

        CborMap obj = (CborMap) assertParseToString(output, array);

        assertTrue(obj.isValidJson());

        byte[] encoded = obj.toCborByteArray();

        assertEquals(obj, obj.copy());

        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Map.class)));
        assertThrows(
                CborConversionException.class,
                () -> CborObject.createFromJavaObject(obj.toJavaObject(String[].class)));
    }

    @Test
    void testParser9() throws Exception {
        byte[] array = decode("bf6346756ef563416d7421a0f7ff");

        String output = "{\"Fun\":false,\"Amt\":-2,{}:undefined}";

        CborMap obj = (CborMap) assertParseToString(output, array);

        assertEquals(3, obj.size());
        assertFalse(obj.isEmpty());
        assertFalse(obj.isValidJson());

        byte[] encoded = obj.toCborByteArray();

        assertEquals(obj, obj.copy());

        assertEquals("{\"Fun\":false,\"Amt\":-2,\"{}\":\"undefined\"}", obj.toJsonString());

        // This is not equals because CborSimple.UNDEFINED gets converted to null...
        assertNotEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Map.class)));

        assertThrows(
                CborConversionException.class,
                () -> CborObject.createFromJavaObject(obj.toJavaObject(String[].class)));
    }

    @Test
    void testParser10() throws Exception {
        byte[] array = decode("bf6346756ef563416d7421a001ff");

        String output = "{\"Fun\":false,\"Amt\":-2,{}:1}";

        CborMap obj = (CborMap) assertParseToString(output, array);

        assertEquals(3, obj.size());
        assertFalse(obj.isEmpty());
        assertFalse(obj.isValidJson());

        byte[] encoded = obj.toCborByteArray();

        assertEquals(obj, obj.copy());

        assertEquals("{\"Fun\":false,\"Amt\":-2,\"{}\":1}", obj.toJsonString());

        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Map.class)));
        assertThrows(
                CborConversionException.class,
                () -> CborObject.createFromJavaObject(obj.toJavaObject(String[].class)));
    }
}
