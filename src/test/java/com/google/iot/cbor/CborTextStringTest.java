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
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"ConstantConditions", "unused"})
public class CborTextStringTest extends CborTestBase {
    private static final boolean DEBUG = false;
    private static final Logger LOGGER =
            Logger.getLogger(CborTextStringTest.class.getCanonicalName());

    @Test
    void testTruncatedTextStream() {
        assertThrows(
                CborParseException.class, () -> CborObject.createFromCborByteArray(decode("61")));
        assertThrows(
                CborParseException.class, () -> CborObject.createFromCborByteArray(decode("7F")));
    }

    @Test
    void testInvalidIndefiniteTextStream() {
        assertThrows(
                CborParseException.class,
                () -> CborObject.createFromCborByteArray(decode("7f457374726561646d696e67ff")));
        assertThrows(
                CborParseException.class,
                () -> CborObject.createFromCborByteArray(decode("7f6573747265610001ff")));
    }

    @Test
    void testIndefiniteTextStream() throws Exception {
        byte[] array = decode("7f657374726561646d696e67ff");

        String output = "\"streaming\"";

        CborObject obj = assertParseToString(output, array);

        byte[] encoded = obj.toCborByteArray();

        CborObject obj2 = assertParseToString(output, encoded);

        assertEquals(obj, obj2);

        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Object.class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(String.class)));
    }

    @Test
    void testURI() throws Exception {
        URI uri = URI.create("http://google.com/#body");

        CborObject obj = CborObject.createFromJavaObject(uri);

        assertEquals(uri, obj.toJavaObject(URI.class));
        assertEquals(uri, obj.toJavaObject(Object.class));
        assertEquals(uri.toString(), obj.toJavaObject(String.class));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Object.class)));
        assertEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(URI.class)));
    }

    @Test
    void testBadURI() throws Exception {
        String badUri = "h ttp: //google.com/#body";

        CborObject obj = CborTextString.create(badUri, CborTag.URI);

        assertEquals(badUri, obj.toJavaObject(String.class));
        assertThrows(CborConversionException.class, () -> obj.toJavaObject(URI.class));
        assertEquals(badUri, obj.toJavaObject(Object.class));
        assertNotEquals(obj, CborObject.createFromJavaObject(obj.toJavaObject(Object.class)));
    }

    @Test
    void testSimpleUtf8() {
        byte[] array = decode("6324C2A2");

        String output = "\"$¢\"";

        CborObject obj = assertParseToString(output, array);

        byte[] encoded = obj.toCborByteArray();

        CborObject obj2 = assertParseToString(output, encoded);

        assertEquals(obj, obj2);
    }

    @Test
    void testComplexUtf8() {
        byte[] array = decode("63E282AC");

        // Note: this is the JSON escaped encoding of "€"...
        String output = "\"\\u20ac\"";

        CborObject obj = assertParseToString(output, array);

        byte[] encoded = obj.toCborByteArray();

        CborObject obj2 = assertParseToString(output, encoded);

        assertEquals(obj, obj2);
    }

    @Test
    void testIndefiniteTextStreamSplitUtf8() {
        byte[] array = decode("7f61C261A2ff");

        String output = "\"¢\"";

        CborObject obj = assertParseToString(output, array);

        byte[] encoded = obj.toCborByteArray();

        CborObject obj2 = assertParseToString(output, encoded);

        assertEquals(obj, obj2);
    }

    @Test
    void testBadUtf8TextStream() {
        byte[] array = decode("646df06e67");

        String output = "\"m�ng\"";

        CborObject obj = assertParseToString(output, array);

        byte[] encoded = obj.toCborByteArray();

        CborObject obj2 = assertParseToString(output, encoded);

        assertEquals(obj, obj2);
    }

    @Test
    void testTaggedTextStream() {
        byte[] array = decode("c074323031332d30332d32315432303a30343a30305a");

        String output = "0(\"2013-03-21T20:04:00Z\")";

        CborObject obj = assertParseToString(output, array);

        assertTrue(obj.isValidJson());

        assertEquals(CborTag.TIME_DATE_STRING, obj.getTag());

        byte[] encoded = obj.toCborByteArray();

        assertArrayEquals(array, encoded);

        assertEquals(obj, obj.copy());
    }
}
