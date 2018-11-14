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

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class CborObjectTest extends CborTestBase {
    private static final boolean DEBUG = false;
    private static final Logger LOGGER = Logger.getLogger(CborObjectTest.class.getCanonicalName());

    @Test
    void testCreateFromCborByteArray() throws Exception {
        byte[] array = decode("9f00ff");
        CborObject obj;

        obj = CborObject.createFromCborByteArray(array);
        assertEquals("[0]", obj.toString());

        obj = CborObject.createFromCborByteArray(array, 1, array.length - 2);
        assertEquals("0", obj.toString());

        assertThrows(
                IndexOutOfBoundsException.class,
                () -> CborObject.createFromCborByteArray(array, 30, 1));
        assertThrows(
                CborParseException.class,
                () -> CborObject.createFromCborByteArray(array, 1, array.length - 1));
        assertThrows(
                CborParseException.class,
                () -> CborObject.createFromCborByteArray(array, 2, array.length - 2));
    }

    @Test
    void testJavaToCborConversion() throws Exception {
        assertEquals("\"hello\"", CborObject.createFromJavaObject("hello").toString());
        assertEquals(
                "32(\"http://google.com/\")",
                CborObject.createFromJavaObject(URI.create("http://google.com/")).toString());
        assertEquals("false", CborObject.createFromJavaObject(false).toString());
        assertEquals("null", CborObject.createFromJavaObject(null).toString());
        assertEquals("true", CborObject.createFromJavaObject(true).toString());
        assertEquals("12345", CborObject.createFromJavaObject(12345).toString());
        assertEquals("12345", CborObject.createFromJavaObject((short) 12345).toString());
        assertEquals("-12345", CborObject.createFromJavaObject(-12345L).toString());
        assertEquals("12345.0_2", CborObject.createFromJavaObject(12345.0f).toString());
        assertEquals("12345.0_3", CborObject.createFromJavaObject(12345.0).toString());
        assertEquals("[]", CborObject.createFromJavaObject(new LinkedList<>()).toString());
        assertEquals("{}", CborObject.createFromJavaObject(new HashMap<>()).toString());
        assertEquals(
                "h'01020304'",
                CborObject.createFromJavaObject(new byte[] {0x01, 0x02, 0x03, 0x04}).toString());
        assertEquals(
                "[1,2,3,4]",
                CborObject.createFromJavaObject(new int[] {0x01, 0x02, 0x03, 0x04}).toString());
        assertEquals(
                "[1,2,3,4]",
                CborObject.createFromJavaObject(new long[] {0x01, 0x02, 0x03, 0x04}).toString());
        assertEquals(
                "[1,2,3,4]",
                CborObject.createFromJavaObject(new short[] {0x01, 0x02, 0x03, 0x04}).toString());
        assertEquals(
                "[false,true,false,true]",
                CborObject.createFromJavaObject(new boolean[] {false, true, false, true})
                        .toString());
        assertEquals(
                "[1.0_2,2.0_2,3.0_2,4.0_2]",
                CborObject.createFromJavaObject(new float[] {1.0f, 2.0f, 3.0f, 4.0f}).toString());
        assertEquals(
                "[1.0_3,2.0_3,3.0_3,4.0_3]",
                CborObject.createFromJavaObject(new double[] {1.0f, 2.0f, 3.0f, 4.0f}).toString());
        assertEquals(
                "[1,2,null,4]",
                CborObject.createFromJavaObject(new Object[] {1, 2, null, 4}).toString());
    }

    @Test
    void testCborToJavaConversion() throws Exception {
        assertEquals(12345, CborInteger.create(12345).toJavaObject());
        assertEquals("hello", CborTextString.create("hello").toJavaObject());
        assertEquals(true, CborObject.createFromJavaObject(true).toJavaObject());
        assertEquals(false, CborObject.createFromJavaObject(false).toJavaObject());
        assertNull(CborObject.createFromJavaObject(null).toJavaObject());
        assertEquals(
                "http://google.com/",
                CborObject.createFromJavaObject(URI.create("http://google.com/")).toJavaObject());
    }

    @Test
    void testJavaToCborConversionFailures() {
        assertThrows(
                CborConversionException.class, () -> CborObject.createFromJavaObject(Object.class));
        assertThrows(
                CborConversionException.class,
                () -> CborObject.createFromJavaObject(new Object[] {1, 2, null, Object.class}));
    }

    @Test
    void testInvalidTags() {
        final int INVALID_TAG = CborTag.UNTAGGED - 1;
        assertThrows(IllegalArgumentException.class, () -> CborSimple.create(0, INVALID_TAG));
        assertThrows(IllegalArgumentException.class, () -> CborInteger.create(0, INVALID_TAG));
        assertThrows(IllegalArgumentException.class, () -> CborFloat.create(0, INVALID_TAG));
        assertThrows(IllegalArgumentException.class, () -> CborArray.create(INVALID_TAG));
        assertThrows(
                IllegalArgumentException.class,
                () -> CborArray.create(new LinkedList<>(), INVALID_TAG));
        assertThrows(IllegalArgumentException.class, () -> CborMap.create(INVALID_TAG));
        assertThrows(
                IllegalArgumentException.class, () -> CborMap.create(new HashMap<>(), INVALID_TAG));
        assertThrows(IllegalArgumentException.class, () -> CborTextString.create("", INVALID_TAG));
        assertThrows(
                IllegalArgumentException.class,
                () -> CborByteString.create(new byte[0], 0, 0, INVALID_TAG));
    }

    @Test
    void testTags() {
        final int TAG = CborTag.SELF_DESCRIBE_CBOR;
        assertEquals(TAG, CborSimple.create(0, TAG).getTag());
        assertEquals(TAG, CborInteger.create(0, TAG).getTag());
        assertEquals(TAG, CborFloat.create(0, TAG).getTag());
        assertEquals(TAG, CborTextString.create("", TAG).getTag());
        assertEquals(TAG, CborByteString.create(new byte[0], 0, 0, TAG).getTag());

        assertEquals(TAG, CborMap.create(TAG).getTag());
        CborMap map = CborMap.create();
        assertEquals(CborTag.UNTAGGED, map.getTag());

        assertEquals(TAG, CborArray.create(TAG).getTag());
        CborArray array = CborArray.create();
        assertEquals(CborTag.UNTAGGED, array.getTag());
    }

    @Test
    void testJSONObjectConversion1() throws Exception {
        String jsonString =
                "{\"modeOfOperation\":\"cbc\",\"encrypted\":[[]],\"f\":0.2,\"segmentSize\":null,\"plaintext\":[[]],\"iv\":[239,223,181,236,61,118,254,53,251,59,113,4,2,58,69,100],\"key\":[58,69,185,228,20,175,122,156,194,103,53,209,61,20,147,253,222,177,125,54,3,84,13,168]}";
        JSONObject jsonObject = new JSONObject(jsonString);

        CborMap cborObject = CborMap.createFromJSONObject(jsonObject);

        if (DEBUG) LOGGER.info("cborObject = " + cborObject);

        assertTrue(cborObject.areAllKeysStrings());

        assertEquals(jsonObject.toString(), cborObject.toJsonString());
        assertEquals(jsonString, cborObject.toJsonString());

        assertEquals(cborObject.toNormalMap(), jsonObject.toMap());
        assertEquals(cborObject, CborMap.createFromJavaObject(jsonObject.toMap()));

        assertEquals(cborObject, cborObject.copy());

        assertEquals(
                cborObject, CborObject.createFromJavaObject(cborObject.toJavaObject(Object.class)));
        assertEquals(
                cborObject, CborObject.createFromJavaObject(cborObject.toJavaObject(Map.class)));
        assertThrows(
                CborConversionException.class,
                () -> CborObject.createFromJavaObject(cborObject.toJavaObject(String[].class)));
    }

    @Test
    void testJSONObjectConversion2() throws Exception {
        String jsonString =
                "{\"glossary\":{\"title\":\"example glossary\",\"GlossDiv\":{\"GlossList\":{\"GlossEntry\":{\"GlossTerm\":\"Standard Generalized Markup Language\",\"GlossSee\":\"markup\",\"SortAs\":\"SGML\",\"GlossDef\":{\"para\":\"A meta-markup language, used to create markup languages such as DocBook.\",\"GlossSeeAlso\":[\"GML\",\"XML\"]},\"ID\":\"SGML\",\"Acronym\":\"SGML\",\"Abbrev\":\"ISO 8879:1986\"}},\"title\":\"S\"}},\"float\":1.5}";
        JSONObject jsonObject = new JSONObject(jsonString);

        CborMap cborObject = CborMap.createFromJSONObject(jsonObject);

        if (DEBUG) LOGGER.info("cborObject = " + cborObject);

        assertTrue(cborObject.areAllKeysStrings());

        assertEquals(jsonObject.toString(), cborObject.toJsonString());
        assertEquals(jsonString, cborObject.toJsonString());

        assertEquals(cborObject.toNormalMap(), jsonObject.toMap());
        assertEquals(cborObject, CborMap.createFromJavaObject(jsonObject.toMap()));

        assertEquals(cborObject, cborObject.copy());

        assertEquals(
                cborObject, CborObject.createFromJavaObject(cborObject.toJavaObject(Object.class)));
        assertEquals(
                cborObject, CborObject.createFromJavaObject(cborObject.toJavaObject(Map.class)));
        assertThrows(
                CborConversionException.class,
                () -> CborObject.createFromJavaObject(cborObject.toJavaObject(String[].class)));
    }
}
