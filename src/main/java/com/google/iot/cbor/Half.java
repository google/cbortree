/*
 * Copyright (C) 2018 Google Inc.
 * Copyright (C) 2016 The Android Open Source Project
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

/**
 * Utility class for conversions to/from 16-bit "half" floating point representation. The code in
 * this class was adapted from <code>android.util.Half</code> in Android, which can be found <a
 * href="https://goo.gl/n4SeZG">here</a>. This class is intended to only be used from inside of this
 * CBOR library.
 */
final class Half {
    private Half() {}

    public static final int MAX_EXPONENT = 15;
    public static final int MIN_EXPONENT = -14;

    public static final float MAX_VALUE = 65504.0f;
    public static final float MIN_VALUE = 0.000061035156f;
    public static final float MIN_NORMAL = 0.000000059604645f;
    public static final float EPSILON = 0.0009765625f;

    public static int MAX_EXACT_INT_RANGE_VALUE = 2048;
    public static int MIN_EXACT_INT_RANGE_VALUE = -2048;

    public static float SIZE = 16;
    public static final float NaN = Float.NaN;
    public static final float POSITIVE_INFINITY = Float.POSITIVE_INFINITY;
    public static final float NEGATIVE_INFINITY = Float.NEGATIVE_INFINITY;

    private static final int FP16_SIGN_SHIFT = 15;
    private static final int FP16_SIGN_MASK = 0x8000;
    private static final int FP16_EXPONENT_SHIFT = 10;
    private static final int FP16_EXPONENT_MASK = 0x1f;
    private static final int FP16_SIGNIFICAND_MASK = 0x3ff;
    private static final int FP16_EXPONENT_BIAS = 15;
    private static final int FP32_SIGN_SHIFT = 31;
    private static final int FP32_EXPONENT_SHIFT = 23;
    private static final int FP32_EXPONENT_MASK = 0xff;
    private static final int FP32_SIGNIFICAND_MASK = 0x7fffff;
    private static final int FP32_EXPONENT_BIAS = 127;
    private static final int FP32_DENORMAL_MAGIC = 126 << 23;
    private static final float FP32_DENORMAL_FLOAT = Float.intBitsToFloat(FP32_DENORMAL_MAGIC);

    public static float shortBitsToFloat(short h) {
        int bits = h & 0xffff;
        int s = bits & FP16_SIGN_MASK;
        int e = (bits >>> FP16_EXPONENT_SHIFT) & FP16_EXPONENT_MASK;
        int m = (bits) & FP16_SIGNIFICAND_MASK;
        int outE = 0;
        int outM = 0;
        if (e == 0) { // Denormal or 0
            if (m != 0) {
                // Convert denorm fp16 into normalized fp32
                float o = Float.intBitsToFloat(FP32_DENORMAL_MAGIC + m);
                o -= FP32_DENORMAL_FLOAT;
                return s == 0 ? o : -o;
            }
        } else {
            outM = m << 13;
            if (e == 0x1f) { // Infinite or NaN
                outE = 0xff;
            } else {
                outE = e - FP16_EXPONENT_BIAS + FP32_EXPONENT_BIAS;
            }
        }
        int out = (s << 16) | (outE << FP32_EXPONENT_SHIFT) | outM;
        return Float.intBitsToFloat(out);
    }

    public static short floatToRawShortBits(float f) {
        int bits = Float.floatToRawIntBits(f);
        int s = (bits >>> FP32_SIGN_SHIFT);
        int e = (bits >>> FP32_EXPONENT_SHIFT) & FP32_EXPONENT_MASK;
        int m = (bits) & FP32_SIGNIFICAND_MASK;
        int outE = 0;
        int outM = 0;
        if (e == 0xff) { // Infinite or NaN
            outE = 0x1f;
            outM = m != 0 ? 0x200 : 0;
        } else {
            e = e - FP32_EXPONENT_BIAS + FP16_EXPONENT_BIAS;
            if (e >= 0x1f) { // Overflow
                outE = 0x31;
            } else if (e <= 0) { // Underflow
                //noinspection StatementWithEmptyBody
                if (e < -10) {
                    // The absolute fp32 value is less than MIN_VALUE, flush to +/-0
                } else {
                    // The fp32 value is a normalized float less than MIN_NORMAL,
                    // we convert to a denorm fp16
                    m = (m | 0x800000) >> (1 - e);
                    if ((m & 0x1000) != 0) m += 0x2000;
                    outM = m >> 13;
                }
            } else {
                outE = e;
                outM = m >> 13;
                if ((m & 0x1000) != 0) {
                    // Round to nearest "0.5" up
                    int out = (outE << FP16_EXPONENT_SHIFT) | outM;
                    out++;
                    return (short) (out | (s << FP16_SIGN_SHIFT));
                }
            }
        }
        return (short) ((s << FP16_SIGN_SHIFT) | (outE << FP16_EXPONENT_SHIFT) | outM);
    }
}
