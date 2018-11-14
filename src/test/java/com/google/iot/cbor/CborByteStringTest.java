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

import java.math.BigInteger;
import java.util.Random;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

public class CborByteStringTest extends CborTestBase {
    private static final boolean DEBUG = false;
    private static final Logger LOGGER =
            Logger.getLogger(CborByteStringTest.class.getCanonicalName());

    @Test
    void testTruncatedByteStream() {
        assertThrows(
                CborParseException.class, () -> CborObject.createFromCborByteArray(decode("41")));
        assertThrows(
                CborParseException.class,
                () -> CborObject.createFromCborByteArray(decode("5F0001")));
    }

    @Test
    void testInvalidIndefiniteByteStream() {
        assertThrows(
                CborParseException.class,
                () -> CborObject.createFromCborByteArray(decode("5f6573747265610001ff")));
        assertThrows(
                CborParseException.class,
                () -> CborObject.createFromCborByteArray(decode("5f657374726561646d696e67ff")));
    }

    @Test
    void testEquality() {
        assertParseEquality("d9d9f74401020304", "d9d9f74401020304");
        assertParseEquality("4401020304", "4401020304");
        assertParseInequality("d9d9f74401020304", "4401020304");
        assertParseInequality("4401020304", "4401040304");
        assertParseInequality("4401020304", "00");
        assertParseInequality("d9d9f74401020304", "d9d0f74401020304");
    }

    @Test
    void testIndefiniteByteStream() {
        byte[] array = decode("5f457374726561446d696e67ff");

        String output = "h'73747265616d696e67'";

        CborObject obj = assertParseToString(output, array);

        byte[] encoded = obj.toCborByteArray();

        CborObject obj2 = assertParseToString(output, encoded);

        assertEquals(obj, obj2);
    }

    @Test
    void testTaggedByteStream() {
        byte[] array = decode("d9d9f74401020304");

        String output = "55799(h'01020304')";

        CborObject obj = assertParseToString(output, array);

        assertFalse(obj.isValidJson());

        byte[] encoded = obj.toCborByteArray();

        CborObject obj2 = assertParseToString(output, encoded);

        assertEquals(obj, obj2);

        assertArrayEquals(array, encoded);

        assertEquals(obj, obj.copy());

        assertEquals(CborTag.SELF_DESCRIBE_CBOR, obj.getTag());
    }

    @Test
    void testByteStream4B() throws Exception {
        byte[] array = decode("4401020304");

        String output = "h'01020304'";

        CborObject obj = assertParseToString(output, array);

        assertFalse(obj.isValidJson());

        byte[] encoded = obj.toCborByteArray();

        CborObject obj2 = assertParseToString(output, encoded);

        assertEquals(obj, obj2);

        assertArrayEquals(array, encoded);

        assertEquals(obj, obj.copy());

        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Object.class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(byte[].class)));
        assertThrows(
                CborConversionException.class,
                () -> CborObject.createFromJavaObject(obj.toJavaObject(String.class)));
    }

    @Test
    void testEXPECTED_BASE16() throws Exception {
        byte[] array = decode("01234567");

        CborByteString obj = CborByteString.create(array);

        assertFalse(obj.isValidJson());

        assertNotEquals("\"01234567\"", obj.toJsonString());

        CborByteString obj2 =
                CborByteString.create(array, 0, array.length, CborTag.EXPECTED_BASE16);

        assertTrue(obj2.isValidJson());

        assertEquals("\"01234567\"", obj2.toJsonString());

        assertEquals(obj2, obj2.copy());

        assertNotEquals(obj, CborObject.createFromJavaObject(obj2.toJavaObject(Object.class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj2.toJavaObject(byte[].class)));
        assertArrayEquals(array, obj2.toJavaObject(byte[].class));
        assertEquals("01234567", obj2.toJavaObject(String.class));
        assertEquals("01234567", obj2.toJavaObject(Object.class));
    }

    @Test
    void testBigInteger() throws Exception {

        BigInteger bi = new BigInteger(200, 1, new Random());

        CborObject obj = CborObject.createFromJavaObject(bi);

        assertEquals(CborTag.BIGNUM_POS, obj.getTag());
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(BigInteger.class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Number.class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Object.class)));

        CborObject obj2 = CborObject.createFromJavaObject(bi.negate());
        assertEquals(CborTag.BIGNUM_NEG, obj2.getTag());
        assertEquals(obj2, CborObject.createFromJavaObject(obj2.toJavaObject(BigInteger.class)));
        assertEquals(obj2, CborObject.createFromJavaObject(obj2.toJavaObject(Number.class)));
        assertEquals(obj2, CborObject.createFromJavaObject(obj2.toJavaObject(Object.class)));

        assertArrayEquals(obj.toJavaObject(byte[].class), obj2.toJavaObject(byte[].class));
    }

    @Test
    void testEmbeddedCbor() throws Exception {
        byte[] array = decode("4401020304");

        CborObject innerObj = CborObject.createFromCborByteArray(array);

        CborByteString obj = CborByteString.create(array, 0, array.length, CborTag.CBOR_DATA_ITEM);

        assertEquals(innerObj, obj.toJavaObject(CborObject.class));

        assertEquals(innerObj, obj.toJavaObject(CborByteString.class));

        assertThrows(CborConversionException.class, () -> obj.toJavaObject(CborMap.class));

        assertEquals(innerObj, obj.toJavaObject(Object.class));

        assertArrayEquals(array, obj.toJavaObject(byte[].class));

        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(CborObject.class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Object.class)));
    }

    @Test
    void testBadEmbeddedCbor() throws Exception {
        byte[] array = decode("ab3c424909f3828d82cc93abc4dffd26");

        CborByteString obj = CborByteString.create(array, 0, array.length, CborTag.CBOR_DATA_ITEM);

        assertThrows(CborConversionException.class, () -> obj.toJavaObject(CborObject.class));

        assertArrayEquals(array, (byte[]) obj.toJavaObject(Object.class));

        assertArrayEquals(array, obj.toJavaObject(byte[].class));

        assertNotEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Object.class)));
    }
}
