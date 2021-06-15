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

import javax.annotation.Nullable;

/** CBOR integer object interface. */
public abstract class CborInteger extends CborObject implements CborNumber {
    // Prohibit users from subclassing for now.
    CborInteger() {}

    public static CborInteger create(long value) {
        return create(value, CborTag.UNTAGGED);
    }

    public static CborInteger create(long value, int tag) {
        return create(value, tag, null);
    }

    public static CborInteger create(long value, int tag, @Nullable Integer majorType) {
        return new CborIntegerImpl(value, tag, majorType);
    }

    static int calcAdditionalInformation(long val) {
        if (val < 0) {
            val = -val - 1;
        }

        if (val < ADDITIONAL_INFO_EXTRA_1B) {
            return (byte) val;
        }

        if (val <= 0xFFL) {
            return ADDITIONAL_INFO_EXTRA_1B;
        }

        if (val <= 0xFFFFL) {
            return ADDITIONAL_INFO_EXTRA_2B;
        }

        if (val <= 0xFFFFFFFFL) {
            return ADDITIONAL_INFO_EXTRA_4B;
        }

        return ADDITIONAL_INFO_EXTRA_8B;
    }

    @Override
    public final int getAdditionalInformation() {
        return calcAdditionalInformation(longValue());
    }

    @Override
    public int getMajorType() {
        return (longValue() < 0) ? CborMajorType.NEG_INTEGER : CborMajorType.POS_INTEGER;
    }

    /**
     * Returns the value of the integer as a {@link long}.
     *
     * @return The {@link long} value of this object.
     */
    @Override
    public abstract long longValue();

    @Override
    public final float floatValue() {
        return (float) longValue();
    }

    @Override
    public final double doubleValue() {
        return (double) longValue();
    }

    @Override
    public final CborInteger copy() {
        // CborInteger objects are immutable, thus we can copy by returning this.
        return this;
    }

    @Override
    public final boolean isValidJson() {
        // CborIntegers are always valid in JSON.
        return true;
    }

    @Override
    public final String toJsonString() {
        return Long.toString(longValue());
    }

    @Override
    public Number toJavaObject() {
        long lval = longValue();
        if ((lval > Integer.MAX_VALUE) || (lval < Integer.MIN_VALUE)) {
            return lval;
        }
        return (int) lval;
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
                String.format("%s is not assignable from %s", clazz, Long.class));
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

        if (obj instanceof CborInteger) {
            CborInteger rhs = (CborInteger) obj;
            return longValue() == rhs.longValue();
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
        String ret;
        if(getMajorType() == CborMajorType.POS_INTEGER) {
            // handle the case of a 64bit unsigned positive integer value
            ret = Long.toUnsignedString(longValue());
        } else {
            ret = Long.toString(longValue());
        }

        int tag = getTag();

        return tag == CborTag.UNTAGGED ? ret : tag + "(" + ret + ")";
    }
}
