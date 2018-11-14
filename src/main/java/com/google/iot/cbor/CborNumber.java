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

/**
 * Interface for CBOR data items representing numbers.
 *
 * <p>This interface can be used to identify (and provide a uniform set of accessors for) the two
 * data item types that represent numbers: {@link CborInteger} and {@link CborFloat}. This helps
 * make it easier to implement the behavior described in <a
 * href="https://tools.ietf.org/html/rfc7049#section-3.6">Section 3.6 of RFC7049</a>.
 *
 * <p>Additionally, the {@link #byteValueExact()}, {@link #shortValueExact()}, and {@link
 * #intValueExact()} methods automatically perform range checks and will throw an {@link
 * ArithmeticException} if the underlying value will not fit into the respective primitive type.
 *
 * @see CborFloat
 * @see CborInteger
 */
public interface CborNumber {
    /**
     * Returns the value of the integer as a {@code long}. If the underlying value is a floating
     * point number, then the result is the floor of that value.
     *
     * @return The {@code long} value of this object.
     */
    long longValue();

    /** Returns the value of this data item as a {@code float}. */
    float floatValue();

    /** Returns the value of this data item as a {@code double}. */
    double doubleValue();

    /**
     * Returns the value of the integer as a {@code byte}. If the contained value is too large or
     * too small to fit inside of a {@code byte}, a {@link ArithmeticException} will be thrown. If
     * the underlying value is a floating point number, then the result is the floor of that value.
     *
     * @return The {@code byte} value of this object.
     * @throws ArithmeticException if the contained value is too large or too small for a {@link
     *     byte}.
     */
    default byte byteValueExact() {
        long value = longValue();
        if ((value > Byte.MAX_VALUE) || (value < Byte.MIN_VALUE)) {
            throw new ArithmeticException("Value does not fit in a byte");
        }
        return (byte) value;
    }

    /**
     * Returns the value of the integer as a {@code short}. If the contained value is too large or
     * too small to fit inside of a {@code short}, a {@link ArithmeticException} will be thrown. If
     * the underlying value is a floating point number, then the result is the floor of that value.
     *
     * @return The {@code short} value of this object.
     * @throws ArithmeticException if the contained value is too large or too small for a {@link
     *     short}.
     */
    default short shortValueExact() {
        long value = longValue();
        if ((value > Short.MAX_VALUE) || (value < Short.MIN_VALUE)) {
            throw new ArithmeticException("Value does not fit in a short");
        }
        return (short) value;
    }

    /**
     * Returns the value of the integer as a {@code int}. If the contained value is too large or too
     * small to fit inside of a {@code int}, a If the underlying value is a floating point number,
     * then the result is the floor of that value. {@link ArithmeticException} will be thrown.
     *
     * @return The {@code int} value of this object.
     * @throws ArithmeticException if the contained value is too large or too small for a {@link
     *     int}.
     */
    default int intValueExact() {
        long value = longValue();
        if ((value > Integer.MAX_VALUE) || (value < Integer.MIN_VALUE)) {
            throw new ArithmeticException("Value does not fit in a int");
        }
        return (int) value;
    }
}
