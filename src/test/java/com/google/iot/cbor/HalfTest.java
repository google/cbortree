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

import org.junit.jupiter.api.Test;

class HalfTest {

    private void assertBidirectionalConversion(float fvalue, int bits) {
        assertEquals(fvalue, Half.shortBitsToFloat((short) bits));
        assertEquals((short) bits, Half.floatToRawShortBits(fvalue));
    }

    @Test
    void testMaxValue() {
        assertBidirectionalConversion(Half.MAX_VALUE, 0x7BFF);

        for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
            float v = Half.shortBitsToFloat((short) i);
            if (v == Half.POSITIVE_INFINITY) {
                continue;
            }
            assertFalse(
                    v > Half.MAX_VALUE,
                    String.format("0x%04X: %f is larger than %f", i, v, Half.MAX_VALUE));
        }
    }

    @Test
    void testMinValue() {
        assertBidirectionalConversion(Half.MIN_VALUE, 0x0400);
    }

    @Test
    void testMinNormal() {
        assertBidirectionalConversion(Half.MIN_NORMAL, 0x0001);
        for (int i = 1; i <= Short.MAX_VALUE; i++) {
            float v = Half.shortBitsToFloat((short) i);
            assertFalse(
                    v < Half.MIN_NORMAL,
                    String.format("0x%04X: %f is smaller than %f", i, v, Half.MIN_NORMAL));
        }
    }

    @Test
    void testZero() {
        assertBidirectionalConversion(0.0f, 0x0000);
        assertBidirectionalConversion(-0.0f, 0x8000);
    }

    @Test
    void testUnity() {
        assertBidirectionalConversion(1.0f, 0x3C00);
    }

    @Test
    void testMaxExponent() {
        assertEquals(Half.MAX_EXPONENT, Math.getExponent(Half.MAX_VALUE));
    }

    @Test
    void testMinExponent() {
        assertEquals(Half.MIN_EXPONENT, Math.getExponent(Half.MIN_VALUE));
    }

    @Test
    void testEpsilon() {
        assertEquals(
                Half.EPSILON,
                Half.shortBitsToFloat((short) (Half.floatToRawShortBits(1.0f) + 1)) - 1.0f);
    }

    @Test
    void testNaN() {
        assertBidirectionalConversion(Half.NaN, 0x7E00);
        assertEquals(Half.NaN, Half.shortBitsToFloat((short) 0xFFFF));
        assertEquals(Half.NaN, Half.shortBitsToFloat((short) 0x7FFF));
    }

    @Test
    void testPosInfinity() {
        assertBidirectionalConversion(Half.POSITIVE_INFINITY, 0x7C00);
    }

    @Test
    void testNegInfinity() {
        assertBidirectionalConversion(Half.NEGATIVE_INFINITY, 0xFC00);
    }

    @Test
    void testOneThird() {
        assertBidirectionalConversion(0.3332519531f, 0x3555);
    }
}
