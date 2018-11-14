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

/** Contains constants for CBOR major types. */
public final class CborMajorType {
    /** CBOR major type for positive integers. */
    public static final int POS_INTEGER = 0;

    /** CBOR major type for negative integers. */
    public static final int NEG_INTEGER = 1;

    /** CBOR major type for byte strings (raw data). */
    public static final int BYTE_STRING = 2;

    /** CBOR major type for text strings. */
    public static final int TEXT_STRING = 3;

    /** CBOR major type for arrays of CBOR objects. */
    public static final int ARRAY = 4;

    /** CBOR major type for associative maps of CBOR objects. */
    public static final int MAP = 5;

    /** CBOR major type for CBOR tags. */
    public static final int TAG = 6;

    /** CBOR major type for CBOR floating-point numbers and CBOR simple values. */
    public static final int OTHER = 7;

    // Prevent instantiation
    private CborMajorType() {}
}
