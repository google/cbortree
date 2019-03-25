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

/** CBOR floating-point number object interface. */
public abstract class CborFloat extends CborObject implements CborNumber {
    // Prohibit users from subclassing for now.
    CborFloat() {}

    /**
     * Value of the "additional information" field when contained value is a two-byte half-float.
     */
    public static final int TYPE_HALF = ADDITIONAL_INFO_EXTRA_2B;

    /** Value of the "additional information" field when contained value is a four-byte float. */
    public static final int TYPE_FLOAT = ADDITIONAL_INFO_EXTRA_4B;

    /**
     * Value of the "additional information" field when contained value is an eight-byte
     * double-float.
     */
    public static final int TYPE_DOUBLE = ADDITIONAL_INFO_EXTRA_8B;

    /**
     * Creates an untagged {@link CborFloat} instance that will be encoded as a two-byte half-float.
     */
    public static CborFloat createHalf(float value) {
        return createHalf(value, CborTag.UNTAGGED);
    }

    /**
     * Creates a tagged {@link CborFloat} instance that will be encoded as a two-byte half-float.
     */
    public static CborFloat createHalf(float value, int tag) {
        return CborFloatImpl.createHalf(value, tag);
    }

    /** Creates an untagged {@link CborFloat} instance that will be encoded as a four-byte float. */
    public static CborFloat create(float value) {
        return create(value, CborTag.UNTAGGED);
    }

    /** Creates a tagged {@link CborFloat} instance that will be encoded as a four-byte float. */
    public static CborFloat create(float value, int tag) {
        return new CborFloatImpl(value, tag);
    }

    /**
     * Creates an untagged {@link CborFloat} instance that will be encoded as an eight-byte
     * double-float.
     */
    public static CborFloat create(double value) {
        return create(value, CborTag.UNTAGGED);
    }

    /**
     * Creates a tagged {@link CborFloat} instance that will be encoded as a eight-byte
     * double-float.
     */
    public static CborFloat create(double value, int tag) {
        return new CborFloatImpl(value, tag);
    }

    @Override
    public abstract float floatValue();

    @Override
    public abstract double doubleValue();

    @Override
    public final long longValue() {
        return (long) doubleValue();
    }

    @Override
    public final int getMajorType() {
        return CborMajorType.OTHER;
    }

    @Override
    public final String toJsonString() {
        double value = doubleValue();

        if (isValidJson()) {
            switch (getAdditionalInformation()) {
                default:
                case TYPE_DOUBLE:
                    return Double.toString(value);
                case TYPE_HALF:
                case TYPE_FLOAT:
                    return Float.toString((float) value);
            }

        } else {
            // This floating-point number doesn't have a JSON representation.
            // Thus, according to RFC7049 Section 4.1, a substitute value
            // of `null` is used instead.

            return "null";
        }
    }

    @Override
    public final CborFloat copy() {
        // CborFloat objects are immutable, thus we can copy by returning this.
        return this;
    }

    @Override
    public final boolean isValidJson() {
        // JSON only officially supports finite floating point numbers.
        // NaN and Infinity aren't allowed. :(
        return Double.isFinite(doubleValue());
    }

    @Override
    public final int hashCode() {
        // Mixes hashes of both the double value and the long value in
        // order to preserve the semantic equivalence of numbers as
        // described in Section 3.6 of RFC7049.
        return (getTag() - CborTag.UNTAGGED) * 1337 + Double.hashCode(doubleValue())
                ^ Long.hashCode(longValue());
    }

    @Override
    public Number toJavaObject() {
        if (getAdditionalInformation() == CborFloat.TYPE_DOUBLE) {
            return doubleValue();
        }
        return floatValue();
    }

    @Override
    public <T> T toJavaObject(Class<T> clazz) throws CborConversionException {
        if (clazz.isAssignableFrom(Number.class) || Object.class.equals(clazz)) {
            return clazz.cast(toJavaObject());
        }

        if (clazz.isAssignableFrom(Float.class)) {
            return clazz.cast(floatValue());
        }

        if (clazz.isAssignableFrom(Double.class)) {
            return clazz.cast(doubleValue());
        }

        if (clazz.isAssignableFrom(Long.class)) {
            return clazz.cast(longValue());
        }

        try {
            if (clazz.isAssignableFrom(Integer.class)) {
                return clazz.cast(intValueExact());
            }

            if (clazz.isAssignableFrom(Short.class)) {
                return clazz.cast(shortValueExact());
            }

        } catch (ArithmeticException x) {
            throw new CborConversionException(x);
        }

        throw new CborConversionException(
                String.format("%s is not assignable from %s", clazz, Double.class));
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof CborObject)) {
            return false;
        }

        if (getTag() != ((CborObject) obj).getTag()) {
            return false;
        }

        if (obj instanceof CborFloat) {
            CborFloat rhs = (CborFloat) obj;
            return Double.doubleToRawLongBits(doubleValue())
                    == Double.doubleToRawLongBits(rhs.doubleValue());
        }

        if (!(obj instanceof CborNumber)) {
            return false;
        }

        CborNumber rhs = (CborNumber) obj;

        // Compares both long value and double value in
        // order to preserve the semantic equivalence of numbers as
        // described in Section 3.6 of RFC7049.
        return longValue() == rhs.longValue()
                && Double.doubleToRawLongBits(doubleValue())
                        == Double.doubleToRawLongBits(rhs.doubleValue());
    }

    @Override
    public String toString(int ignore) {
        return toString();
    }

    @Override
    public String toString() {
        int ai = getAdditionalInformation();
        String ret =
                ai == TYPE_DOUBLE ? Double.toString(doubleValue()) : Float.toString(floatValue());

        ret += "_" + (ai - 24);

        int tag = getTag();

        return tag == CborTag.UNTAGGED ? ret : tag + "(" + ret + ")";
    }
}
