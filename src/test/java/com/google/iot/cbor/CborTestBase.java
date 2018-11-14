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

import static java.lang.Character.digit;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

class CborTestBase {
    private static final boolean DEBUG = false;
    private static final Logger LOGGER = Logger.getLogger(CborTestBase.class.getCanonicalName());

    byte[] decode(CharSequence cs) {
        final int numCh = cs.length();
        if ((numCh & 1) != 0) throw new IllegalArgumentException("cs must have an even length");
        byte[] array = new byte[numCh >> 1];
        for (int p = 0; p < numCh; p += 2) {
            int hi = digit(cs.charAt(p), 16), lo = digit(cs.charAt(p + 1), 16);
            if ((hi | lo) < 0)
                throw new IllegalArgumentException(cs + " contains non-hex characters");
            array[p >> 1] = (byte) (hi << 4 | lo);
        }
        return array;
    }

    CborObject assertParseToString(String expect, byte[] byteArray) {
        CborObject obj;
        try {
            obj = CborObject.createFromCborByteArray(byteArray);
        } catch (CborParseException e) {
            e.printStackTrace();
            throw new AssertionError(e);
        }

        if (DEBUG) LOGGER.info("Decoded " + obj);

        // Additional checks to exercise the parser.
        try {
            CborObject obj2;
            ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
            CborReader reader = CborReader.createFromInputStream(inputStream, 1);
            obj2 = reader.readDataItem();

            assertEquals(obj, obj2);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CborWriter writer = CborWriter.createFromOutputStream(outputStream);
            writer.writeDataItem(obj);

            assertArrayEquals(obj.toCborByteArray(), outputStream.toByteArray());

        } catch (CborParseException | IOException e) {
            throw new AssertionError("Input stream parsed differently with InputStream!");
        }

        assertEquals(expect, obj.toString());
        return obj;
    }

    void assertParseEquality(String hex1, String hex2) {
        try {
            byte[] array1 = decode(hex1);
            byte[] array2 = decode(hex2);

            CborObject obj1 = CborObject.createFromCborByteArray(array1);
            CborObject obj2 = CborObject.createFromCborByteArray(array2);

            assertEquals(obj1, obj2);
            assertEquals(obj2, obj1);
        } catch (Exception x) {
            throw new AssertionError(x);
        }
    }

    void assertParseInequality(String hex1, String hex2) {
        try {
            byte[] array1 = decode(hex1);
            byte[] array2 = decode(hex2);

            CborObject obj1 = CborObject.createFromCborByteArray(array1);
            CborObject obj2 = CborObject.createFromCborByteArray(array2);

            assertNotEquals(obj1, obj2);
            assertNotEquals(obj2, obj1);
        } catch (Exception x) {
            throw new AssertionError(x);
        }
    }
}
