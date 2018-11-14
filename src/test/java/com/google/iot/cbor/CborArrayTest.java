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

import java.util.List;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"ConstantConditions", "unused"})
public class CborArrayTest extends CborTestBase {
    private static final boolean DEBUG = false;
    private static final Logger LOGGER = Logger.getLogger(CborArrayTest.class.getCanonicalName());

    @Test
    void testEquality() {
        assertParseEquality("80", "80");
        assertParseEquality(
                "970102030405060708090a0b0c0d0e0f1011121314151617",
                "970102030405060708090a0b0c0d0e0f1011121314151617");
        //noinspection SpellCheckingInspection
        assertParseEquality("9ff4f5f6f7f0f118f8ff", "9ff4f5f6f7f0f118f8ff");
        assertParseEquality("9F1864FF", "81190064");
        assertParseInequality("80", "d9d9f780");
        assertParseInequality("9F1864FF", "81190065");
    }

    @Test
    void testTaggedArray() throws Exception {
        byte[] array = decode("d9d9f798190102030405060708090a0b0c0d0e0f101112131415161718181819");

        String output =
                "55799([1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25])";

        CborObject obj = assertParseToString(output, array);

        assertTrue(obj.isValidJson());

        assertEquals(CborTag.SELF_DESCRIBE_CBOR, obj.getTag());

        byte[] encoded = obj.toCborByteArray();

        assertArrayEquals(array, encoded);

        assertNotEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(int[].class)));
    }

    @Test
    void testCreate() throws Exception {
        byte[] array = decode("d9d9f798190102030405060708090a0b0c0d0e0f101112131415161718181819");

        String output = "[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25]";

        CborObject obj = CborObject.createFromCborByteArray(array);

        obj = CborArray.create((CborArray) obj);

        assertEquals(output, obj.toString());

        assertTrue(
                List.class.isInstance(obj.toJavaObject(Object.class)),
                "Weird type: " + obj.toJavaObject(Object.class).getClass());

        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(int[].class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(short[].class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(long[].class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(float[].class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(double[].class)));

        final CborObject obj2 = obj;

        assertThrows(
                CborConversionException.class,
                () -> CborObject.createFromJavaObject(obj2.toJavaObject(String[].class)));
    }

    @Test
    void testArrayStrings() throws Exception {
        String output = "[\"item1\",\"item2\"]";

        CborArray obj = CborArray.create();

        obj.add(CborTextString.create("item1"));
        obj.add(CborTextString.create("item2"));

        assertEquals(output, obj.toString());

        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(String[].class)));
        assertThrows(
                CborConversionException.class,
                () -> CborObject.createFromJavaObject(obj.toJavaObject(short[].class)));
    }

    @Test
    void testEmptyArray() throws Exception {
        byte[] array = decode("80");

        String output = "[]";

        CborObject obj = assertParseToString(output, array);

        assertTrue(obj.isValidJson());

        byte[] encoded = obj.toCborByteArray();

        assertArrayEquals(array, encoded);

        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(int[].class)));
    }

    @Test
    void testIndefiniteEmptyArray() {
        byte[] array = decode("9fff");

        String output = "[]";

        CborObject obj = assertParseToString(output, array);

        assertTrue(obj.isValidJson());
    }

    @Test
    void testTruncatedArray() {
        assertThrows(
                CborParseException.class, () -> CborObject.createFromCborByteArray(decode("90")));
        assertThrows(
                CborParseException.class,
                () -> CborObject.createFromCborByteArray(decode("9F0001")));
    }

    @Test
    void test23ElementArray() {
        byte[] array = decode("970102030405060708090a0b0c0d0e0f1011121314151617");

        String output = "[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23]";

        CborObject obj = assertParseToString(output, array);

        assertTrue(obj.isValidJson());

        byte[] encoded = obj.toCborByteArray();

        assertArrayEquals(array, encoded);
    }

    @Test
    void test24ElementArray() {
        byte[] array = decode("98180102030405060708090a0b0c0d0e0f10111213141516171818");

        String output = "[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24]";

        CborObject obj = assertParseToString(output, array);

        assertTrue(obj.isValidJson());

        byte[] encoded = obj.toCborByteArray();

        assertArrayEquals(array, encoded);
    }

    @Test
    void test25ElementArray() {
        byte[] array = decode("98190102030405060708090a0b0c0d0e0f101112131415161718181819");

        String output = "[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25]";

        CborObject obj = assertParseToString(output, array);

        assertTrue(obj.isValidJson());

        byte[] encoded = obj.toCborByteArray();

        assertArrayEquals(array, encoded);
    }

    @Test
    void testIndefinite25ElementArray() {
        byte[] array = decode("9F0102030405060708090a0b0c0d0e0f101112131415161718181819FF");

        String output = "[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25]";

        CborObject obj = assertParseToString(output, array);

        assertTrue(obj.isValidJson());
    }

    @Test
    void testCborArrayJavaObjectConversion() throws Exception {
        byte[] array = decode("9F0102030405060708090a0b0c0d0e0f101112131415161718181819FF");

        byte[] recodedArray = decode("98190102030405060708090a0b0c0d0e0f101112131415161718181819");

        String output = "[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25]";

        CborArray obj = (CborArray) assertParseToString(output, array);

        assertTrue(obj.isValidJson());

        assertEquals(25, obj.size());
        assertFalse(obj.isEmpty());

        byte[] encoded = obj.toCborByteArray();

        assertArrayEquals(recodedArray, encoded);

        CborObject obj2 = CborObject.createFromJavaObject(obj.toJavaObject());

        assertTrue(obj2.isValidJson());

        assertEquals(obj, obj2);

        assertEquals(obj, obj.copy());

        obj.clear();
        assertTrue(obj.isEmpty());
    }

    @Test
    void testIntArrayConvert() {
        short[] shortArray = {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
            25
        };
        int[] intArray = {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
            25
        };
        int[] longArray = {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
            25
        };
        float[] floatArray = {
            1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f, 11.0f, 12.0f, 13.0f, 14.0f,
            15.0f, 16.0f, 17.0f, 18.0f, 19.0f, 20.0f, 21.0f, 22.0f, 23.0f, 24.0f, 25.0f
        };
        String output = "[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25]";

        CborObject obj;

        obj = CborArray.createFromJavaObject(shortArray);
        assertEquals(output, obj.toJsonString());

        obj = CborArray.createFromJavaObject(intArray);
        assertEquals(output, obj.toJsonString());

        obj = CborArray.createFromJavaObject(longArray);
        assertEquals(output, obj.toJsonString());
    }

    @Test
    void testFloatArrayConvert() {
        float[] floatArray = {
            1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f, 11.0f, 12.0f, 13.0f, 14.0f,
            15.0f, 16.0f, 17.0f, 18.0f, 19.0f, 20.0f, 21.0f, 22.0f, 23.0f, 24.0f, 25.0f
        };
        double[] doubleArray = {
            1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f, 11.0f, 12.0f, 13.0f, 14.0f,
            15.0f, 16.0f, 17.0f, 18.0f, 19.0f, 20.0f, 21.0f, 22.0f, 23.0f, 24.0f, 25.0f
        };
        String output =
                "[1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,17.0,"
                        + "18.0,19.0,20.0,21.0,22.0,23.0,24.0,25.0]";

        CborObject obj;

        obj = CborArray.createFromJavaObject(floatArray);
        assertEquals(output, obj.toJsonString());

        obj = CborArray.createFromJavaObject(doubleArray);
        assertEquals(output, obj.toJsonString());
    }

    @Test
    void testSimpleValueArray() {
        byte[] array = decode("9ff4f5f6f7f0f8ffff");

        String output = "[true,false,null,undefined,simple(16),simple(255)]";

        CborObject obj = assertParseToString(output, array);

        assertFalse(obj.isValidJson());

        byte[] encoded = obj.toCborByteArray();

        assertEquals(obj, obj.copy());
    }
}
