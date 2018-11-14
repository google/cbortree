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

import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

public class CborFloatTest extends CborTestBase {
    private static final boolean DEBUG = false;
    private static final Logger LOGGER = Logger.getLogger(CborFloatTest.class.getCanonicalName());

    @Test
    void testEquality() {
        assertParseEquality("f93c00", "f93c00");
        assertParseEquality("f93c00", "fa3f800000");
        assertParseEquality("f93c00", "01");
        assertParseEquality("f9c400", "fac0800000");
        assertParseEquality("f9c400", "23");
        assertParseInequality("f9c400", "24");
        assertParseInequality("f93c00", "d9d9f7f93c00");
    }

    @Test
    void testHalfUnity() throws Exception {
        byte[] array = decode("f93c00");

        String output = "1.0_1";

        CborObject obj = assertParseToString(output, array);

        assertTrue(obj.isValidJson());

        byte[] encoded = obj.toCborByteArray();

        CborObject obj2 = assertParseToString(output, encoded);

        assertEquals(obj, obj2);

        assertArrayEquals(array, encoded);

        assertEquals(obj, obj.copy());

        assertTrue(obj instanceof CborNumber);
        assertEquals(1, ((CborNumber) obj).intValueExact());
        assertEquals(1, ((CborNumber) obj).byteValueExact());
        assertEquals(1, ((CborNumber) obj).shortValueExact());
        assertEquals(1, ((CborNumber) obj).longValue());
        assertEquals(1.0, ((CborNumber) obj).doubleValue());
        assertEquals(1.0f, ((CborNumber) obj).floatValue());

        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Object.class)));
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
    void testHalfNegativeFour() throws Exception {
        byte[] array = decode("f9c400");

        String output = "-4.0_1";

        CborObject obj = assertParseToString(output, array);

        assertTrue(obj.isValidJson());

        byte[] encoded = obj.toCborByteArray();

        CborObject obj2 = assertParseToString(output, encoded);

        assertEquals(obj, obj2);

        assertArrayEquals(array, encoded);

        assertEquals(obj, obj.copy());

        assertTrue(obj instanceof CborNumber);
        assertEquals(-4, ((CborNumber) obj).intValueExact());
        assertEquals(-4, ((CborNumber) obj).byteValueExact());
        assertEquals(-4, ((CborNumber) obj).shortValueExact());
        assertEquals(-4, ((CborNumber) obj).longValue());
        assertEquals(-4.0, ((CborNumber) obj).doubleValue());
        assertEquals(-4.0f, ((CborNumber) obj).floatValue());

        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Object.class)));
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
    void testHalfPosInfinity() {
        byte[] array = decode("f97c00");

        String output = "Infinity_1";

        CborObject obj = assertParseToString(output, array);

        assertFalse(obj.isValidJson());

        byte[] encoded = obj.toCborByteArray();

        CborObject obj2 = assertParseToString(output, encoded);

        assertEquals(obj, obj2);

        assertArrayEquals(array, encoded);

        assertEquals("null", obj.toJsonString());
        assertEquals(output, obj.toString());

        assertEquals(obj, obj.copy());
    }

    @Test
    void testHalfNaN() {
        byte[] array = decode("f97e00");

        String output = "NaN_1";

        CborObject obj = assertParseToString(output, array);

        byte[] encoded = obj.toCborByteArray();

        CborObject obj2 = assertParseToString(output, encoded);

        assertEquals(obj, obj2);

        assertArrayEquals(array, encoded);

        assertEquals("null", obj.toJsonString());
        assertEquals(output, obj.toString());

        assertEquals(obj, obj.copy());
    }

    @Test
    void testHalfNegInfinity() {
        byte[] array = decode("f9fc00");

        String output = "-Infinity_1";

        CborObject obj = assertParseToString(output, array);

        assertFalse(obj.isValidJson());

        byte[] encoded = obj.toCborByteArray();

        CborObject obj2 = assertParseToString(output, encoded);

        assertEquals(obj, obj2);

        assertArrayEquals(array, encoded);

        assertEquals(obj, obj.copy());

        assertEquals("null", obj.toJsonString());
    }
}
