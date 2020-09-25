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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * CBOR simple value object class. This class is used for representing simple objects that have a
 * major type of {@link CborMajorType#OTHER} and contain no additional data.
 *
 * <p>Untagged instances are always singletons, so you may directly compare known-untagged instances
 * (like {@link #TRUE}, {@link #NULL}, etc,) to any other {@link CborSimple} instance using just the
 * {@code ==} operator. The {@link #equals(Object)} method, of course, will also work, too.
 *
 * <p>On the other hand, tagged instances are not singletons. If you need to compare two unknown
 * {@code CborSimple} objects then you must use {@link #equals(Object)}.
 */
public final class CborSimple extends CborObject {
    private static final Map<Integer, CborSimple> mSingletons = new ConcurrentHashMap<>();

    /**
     * Additional data value that represents "false" when used with {@link CborMajorType#OTHER}.
     *
     * @see #getValue()
     * @see #FALSE
     */
    private static final int TYPE_FALSE = 20;

    /**
     * Additional data value that represents "true" when used with {@link CborMajorType#OTHER}.
     *
     * @see #getValue()
     * @see #TRUE
     */
    private static final int TYPE_TRUE = 21;

    /**
     * Additional data value that represents "null" when used with {@link CborMajorType#OTHER}.
     *
     * @see #getValue()
     * @see #NULL
     */
    private static final int TYPE_NULL = 22;

    /**
     * Additional data value that represents "undefined" when used with {@link CborMajorType#OTHER}.
     *
     * @see #getValue()
     * @see #UNDEFINED
     */
    private static final int TYPE_UNDEFINED_VALUE = 23;

    /** CBOR simple value constant instance for "true". */
    public static final CborSimple TRUE = CborSimple.create(CborSimple.TYPE_TRUE);

    /** CBOR simple value constant instance for "false". */
    public static final CborSimple FALSE = CborSimple.create(CborSimple.TYPE_FALSE);

    /** CBOR simple value constant instance for "null". */
    public static final CborSimple NULL = CborSimple.create(CborSimple.TYPE_NULL);

    /** CBOR simple value constant instance for "undefined" values. */
    public static final CborSimple UNDEFINED = CborSimple.create(CborSimple.TYPE_UNDEFINED_VALUE);

    private int mValue;
    private int mTag;

    /**
     * Method for obtaining a tagged instance for "simple values" that contains no data. If the
     * specified {@code tag} is {@link CborTag#UNTAGGED}, then this method will return a singleton.
     * Otherwise it will return a newly allocated instance.
     *
     * @param value the "simple value" of the desired singleton {@link CborSimple} object. Must be
     *     in the range of 0 to 255, inclusive.
     * @param tag the value of the tag
     * @return a {@link CborSimple} object with the given value and tag.
     * @throws IllegalArgumentException if <code>value</code> is out of range or if <code>tag</code>
     *     is invalid.
     */
    public static CborSimple create(int value, int tag) {
        return tag == CborTag.UNTAGGED ? create(value) : new CborSimple(value, tag);
    }

    /**
     * Method for obtaining an untagged singleton instance for "simple values" that contains no
     * data.
     *
     * @param value the "simple value" of the desired singleton {@link CborSimple} object. Must be
     *     in the range of 0 to 255, inclusive. Note that values in the range of 24..31 are reserved
     *     and cannot be used.
     * @return a singleton {@link CborSimple} object with the given value.
     * @throws IllegalArgumentException if <code>value</code> is out of range or if it lies in the
     *     reserved range of 24..31.
     */
    public static CborSimple create(int value) {
        return mSingletons.computeIfAbsent(value, (k) -> new CborSimple(value, CborTag.UNTAGGED));
    }

    private CborSimple(int value, int tag) {
        if ((value < 0) || (value > 255)) {
            throw new IllegalArgumentException("Invalid simple value: out of range: " + value);
        }

        // These values from from RFC7049.
        if ((value >= 24) && (value <= 31)) {
            throw new IllegalArgumentException("Reserved simple value " + value);
        }

        if (!CborTag.isValid(tag)) {
            throw new IllegalArgumentException("Invalid tag value " + tag);
        }

        mValue = value;
        mTag = tag;
    }

    @Override
    public final int getMajorType() {
        return CborMajorType.OTHER;
    }

    @Override
    public final int getAdditionalInformation() {
        int x = getValue();
        return (x < ADDITIONAL_INFO_EXTRA_1B) ? x : ADDITIONAL_INFO_EXTRA_1B;
    }

    /** Returns the "simple value" associated with this object. */
    public int getValue() {
        return mValue;
    }

    @Override
    public int getTag() {
        return mTag;
    }

    @Override
    public CborSimple copy() {
        // All instances are immutable, so we can just return ourselves.
        return this;
    }

    @Override
    public final boolean isValidJson() {
        switch (getValue()) {
            case TYPE_TRUE:
            case TYPE_FALSE:
            case TYPE_NULL:
                return true;
            case TYPE_UNDEFINED_VALUE:
            default:
                return false;
        }
    }

    @Override
    public final String toJsonString() {
        int x = getValue();
        switch (x) {
            case TYPE_TRUE:
                return "true";
            case TYPE_FALSE:
                return "false";
            case TYPE_NULL:
                return "null";
            case TYPE_UNDEFINED_VALUE:
                // String-quoted to allow JSON encoding to still be parsable.
                return "\"undefined\"";
            default:
                // String-quoted to allow JSON encoding to still be parsable.
                return "\"simple(" + x + ")\"";
        }
    }

    @Override
    @Nullable
    public Object toJavaObject() {
        int x = getValue();
        switch (x) {
            case TYPE_TRUE:
                return Boolean.TRUE;
            case TYPE_FALSE:
                return Boolean.FALSE;
            default:
                return null;
        }
    }

    @Override
    public <T> T toJavaObject(Class<T> clazz) throws CborConversionException {
        if (this == NULL || this == UNDEFINED) {
            return null;
        }

        if (clazz.isAssignableFrom(Boolean.class)) {
            switch (getValue()) {
                case TYPE_TRUE:
                    return clazz.cast(Boolean.TRUE);
                case TYPE_FALSE:
                    return clazz.cast(Boolean.FALSE);
                default:
                    throw new CborConversionException(this + " is not a boolean");
            }
        }

        if (Object.class.equals(clazz)) {
            // We return null here despite us not having a good conversion because
            // we don't want to cause maps/arrays to throw CborConversionException
            // if they are embedded with unusual simple values. This is the same
            // rationale used for the non-typed toJavaObject(), above.
            //
            // Any attempt to call this method with a more specific class will end
            // up getting the CborConversionException below.
            return null;
        }

        throw new CborConversionException(
                String.format("%s cannot be converted to a %s", this, clazz));
    }

    @Override
    public int hashCode() {
        return mTag * 257 + mValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (mTag == CborTag.UNTAGGED) {
            return this == obj;
        }

        if (!(obj instanceof CborSimple)) {
            return false;
        }

        final CborSimple rhs = (CborSimple) obj;

        return mValue == rhs.mValue && mTag == rhs.mTag;
    }

    @Override
    public String toString(int ignore) {
        return toString();
    }

    @Override
    public final String toString() {
        String ret;
        switch (mValue) {
            case TYPE_TRUE:
                ret = "true";
                break;

            case TYPE_FALSE:
                ret = "false";
                break;

            case TYPE_NULL:
                ret = "null";
                break;

            case TYPE_UNDEFINED_VALUE:
                ret = "undefined";
                break;

            default:
                ret = "simple(" + mValue + ")";
                break;
        }

        return mTag == CborTag.UNTAGGED ? ret : mTag + "(" + ret + ")";
    }
}
