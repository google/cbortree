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

import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"ConstantConditions", "unused"})
public class CborIntegerTest extends CborTestBase {
    private static final boolean DEBUG = false;
    private static final Logger LOGGER = Logger.getLogger(CborIntegerTest.class.getCanonicalName());

    @Test
    void testEquality() {
        assertParseEquality("00", "00");
        assertParseEquality("00", "1800");
        assertParseEquality("1864", "1864");
        assertParseEquality("1864", "190064");
        assertParseEquality("1864", "1a00000064");
        assertParseEquality("1864", "1b0000000000000064");
        assertParseInequality("00", "01");
        assertParseInequality("00", "d9d9f700");
    }

    @Test
    void testTaggedInteger() {
        byte[] array = decode("d9d9f700");

        String output = "55799(0)";

        CborObject obj = assertParseToString(output, array);

        assertTrue(obj.isValidJson());

        byte[] encoded = obj.toCborByteArray();

        assertArrayEquals(array, encoded);

        assertEquals(obj, obj.copy());
    }

    @Test
    void testIntegerE0() throws Exception {
        byte[] array = decode("00");

        String output = "0";

        CborObject obj = assertParseToString(output, array);

        assertTrue(obj.isValidJson());

        byte[] encoded = obj.toCborByteArray();

        assertArrayEquals(array, encoded);

        assertEquals(obj, obj.copy());

        assertEquals(Integer.class, obj.toJavaObject(Object.class).getClass());

        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Double.class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Float.class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Long.class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Integer.class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Short.class)));
        assertThrows(
                CborConversionException.class,
                () -> CborObject.createFromJavaObject(obj.toJavaObject(String.class)));
    }

    @Test
    void testIntegerE1() throws Exception {
        byte[] array = decode("01");

        String output = "1";

        CborObject obj = assertParseToString(output, array);

        assertTrue(obj.isValidJson());

        byte[] encoded = obj.toCborByteArray();

        assertArrayEquals(array, encoded);

        assertEquals(obj, obj.copy());

        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Double.class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Float.class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Long.class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Integer.class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Short.class)));
    }

    @Test
    void testIntegerE3() throws Exception {
        byte[] array = decode("1864");

        String output = "100";

        CborObject obj = assertParseToString(output, array);

        assertTrue(obj.isValidJson());

        byte[] encoded = obj.toCborByteArray();

        assertArrayEquals(array, encoded);

        assertEquals(obj, obj.copy());

        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Double.class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Float.class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Long.class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Integer.class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Short.class)));
        assertThrows(
                CborConversionException.class,
                () -> CborObject.createFromJavaObject(obj.toJavaObject(String.class)));
    }

    @Test
    void testIntegerE4() throws Exception {
        byte[] array = decode("1903e8");

        String output = "1000";

        CborObject obj = assertParseToString(output, array);

        assertTrue(obj.isValidJson());

        byte[] encoded = obj.toCborByteArray();

        assertArrayEquals(array, encoded);

        assertEquals(obj, obj.copy());

        assertTrue(obj instanceof CborNumber);
        assertThrows(ArithmeticException.class, ((CborNumber) obj)::byteValueExact);
        assertEquals(1000, ((CborNumber) obj).intValueExact());
        assertEquals(1000, ((CborNumber) obj).shortValueExact());
        assertEquals(1000, ((CborNumber) obj).longValue());
        assertEquals(1000.0, ((CborNumber) obj).doubleValue());
        assertEquals(1000.0f, ((CborNumber) obj).floatValue());

        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Double.class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Float.class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Long.class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Integer.class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Short.class)));
    }

    @Test
    void testIntegerE7() throws Exception {
        byte[] array = decode("1a000f4240");

        String output = "1000000";

        CborObject obj = assertParseToString(output, array);

        assertTrue(obj.isValidJson());

        byte[] encoded = obj.toCborByteArray();

        CborObject obj2 = assertParseToString(output, encoded);

        assertEquals(obj, obj2);

        assertArrayEquals(array, encoded);

        assertEquals(obj, obj.copy());

        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Double.class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Float.class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Long.class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Integer.class)));
        assertThrows(
                CborConversionException.class,
                () -> CborObject.createFromJavaObject(obj.toJavaObject(Short.class)));
    }

    @Test
    void testIntegerE13() throws Exception {
        byte[] array = decode("1b000000e8d4a51000");

        String output = "1000000000000";

        CborObject obj = assertParseToString(output, array);

        assertTrue(obj.isValidJson());

        byte[] encoded = obj.toCborByteArray();

        CborObject obj2 = assertParseToString(output, encoded);

        assertEquals(obj, obj2);

        assertArrayEquals(array, encoded);

        assertEquals(obj, obj.copy());

        assertEquals(Long.class, obj.toJavaObject(Object.class).getClass());

        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Double.class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Long.class)));
        assertThrows(
                CborConversionException.class,
                () -> CborObject.createFromJavaObject(obj.toJavaObject(Integer.class)));
        assertThrows(
                CborConversionException.class,
                () -> CborObject.createFromJavaObject(obj.toJavaObject(Short.class)));
        assertThrows(
                CborConversionException.class,
                () -> CborObject.createFromJavaObject(obj.toJavaObject(String.class)));
    }

    @Test
    void testLongOverflow() {
        byte[] array = decode("1b8ac7230489e80000");

        String output = "10000000000000000000";

        CborObject obj = assertParseToString(output, array);
    }
}
