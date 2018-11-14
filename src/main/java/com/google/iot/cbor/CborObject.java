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

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Base class for CBOR data items.
 *
 * @see CborArray
 * @see CborFloat
 * @see CborInteger
 * @see CborNumber
 * @see CborMap
 * @see CborByteString
 * @see CborSimple
 * @see CborTextString
 * @see <a href="https://tools.ietf.org/html/rfc7049#section">RFC7049</a>
 */
public abstract class CborObject {
    // Prohibit users from subclassing for now.
    CborObject() {}

    /** Additional info value for when the subsequent value/size encoding is one byte long. */
    static final int ADDITIONAL_INFO_EXTRA_1B = 24;

    /** Additional info value for when the subsequent value/size encoding is two bytes long. */
    static final int ADDITIONAL_INFO_EXTRA_2B = 25;

    /** Additional info value for when the subsequent value/size encoding is four bytes long. */
    static final int ADDITIONAL_INFO_EXTRA_4B = 26;

    /** Additional info value for when the subsequent value/size encoding is eight bytes long. */
    static final int ADDITIONAL_INFO_EXTRA_8B = 27;

    /** Additional info value for when the additional data size is indefinite. */
    static final int ADDITIONAL_INFO_EXTRA_INDEF = 31;

    /**
     * Creates a new {@link CborObject} from a CBOR-encoded byte array starting at a given offset.
     *
     * <p>Provided as convenient alternative to using {@link CborReader}.
     *
     * @param input byte array to parse
     * @param offset index of first byte to start parsing
     * @param length the number of bytes to parse
     * @return finished CborObject.
     * @throws CborParseException if the input data could not be parsed correctly or if there was
     *     extra data present at the end of {@code input}
     * @throws IndexOutOfBoundsException if {@code offset} is out of bounds
     * @see #createFromCborByteArray(byte[])
     * @see CborReader
     */
    public static CborObject createFromCborByteArray(byte[] input, int offset, int length)
            throws CborParseException {
        if (offset < 0) {
            throw new IllegalArgumentException("offset cannot be negative");
        }

        if (length < 1) {
            throw new IllegalArgumentException("length must be greater than 1");
        }

        if (input.length < offset) {
            throw new IndexOutOfBoundsException("offset is larger than byte array");
        }

        if (input.length < offset + length) {
            throw new IndexOutOfBoundsException("offset+length is larger than byte array");
        }

        try {
            CborReader reader = CborReader.createFromByteArray(input, offset, 1);
            CborObject ret = reader.readDataItem();

            if (reader.bytesParsed() > length) {
                throw new CborParseException("data item is truncated");
            }

            if (reader.bytesParsed() < length) {
                throw new CborParseException(
                        "extra data at end of data item "
                                + "(parsed only "
                                + reader.bytesParsed()
                                + " of "
                                + length
                                + " bytes)");
            }

            return ret;

        } catch (IOException x) {
            // Should never happen.
            throw new AssertionError(x);
        }
    }

    /**
     * Creates a new {@link CborObject} from a CBOR-encoded byte array.
     *
     * <p>Provided as convenient alternative to using {@link CborReader}.
     *
     * @param input byte array to parse
     * @return finished CborObject.
     * @throws CborParseException if the input data could not be parsed correctly
     * @see #createFromCborByteArray(byte[], int, int)
     * @see CborReader
     */
    public static CborObject createFromCborByteArray(byte[] input) throws CborParseException {
        return createFromCborByteArray(input, 0, input.length);
    }

    /**
     * Creates a new {@link CborObject} based on the given Java object. Supported Java class types
     * include:
     *
     * <ul>
     *   <li>{@code null} → {@link CborSimple#NULL}
     *   <li>{@link Float}/{@link Double} → {@link CborFloat}
     *   <li>{@link Integer}/{@link Long}/{@link Short} → {@link CborInteger}
     *   <li>{@link String} → {@link CborTextString}
     *   <li>{@link Boolean} → {@link CborSimple#TRUE}/{@link CborSimple#FALSE}
     *   <li>{@link Map} → {@link CborMap}
     *   <li>{@link Iterable} → {@link CborArray}
     *   <li>{@link URI} → {@link CborTag#URI} + {@link CborTextString}
     *   <li>{@link BigInteger} → {@link CborTag#BIGNUM_POS}/{@link CborTag#BIGNUM_NEG} + {@link
     *       CborByteString}
     *   <li>{@code byte[]} → {@link CborByteString}
     *   <li>{@code int[]}/{@code short[]}/{@code long[]} → {@link CborArray}
     *   <li>{@code float[]}/{@code double[]} → {@link CborArray}
     *   <li>{@code String[]} → {@link CborArray}
     * </ul>
     *
     * @param obj the Java object to convert. May be {@code null}.
     * @return the constructed {@link CborObject} representing obj
     * @throws CborConversionException if the object cannot be converted
     * @see #toJavaObject()
     */
    public static CborObject createFromJavaObject(@Nullable Object obj)
            throws CborConversionException {
        if (obj == null) return CborSimple.NULL;
        if (obj instanceof CborObject) {
            byte[] cborBytes = ((CborObject) obj).toCborByteArray();
            return CborByteString.create(cborBytes, 0, cborBytes.length, CborTag.CBOR_DATA_ITEM);
        }
        if (obj instanceof Float) return CborFloat.create((Float) obj);
        if (obj instanceof Double) return CborFloat.create((Double) obj);
        if (obj instanceof Integer) return CborInteger.create((Integer) obj);
        if (obj instanceof Long) return CborInteger.create((Long) obj);
        if (obj instanceof Short) return CborInteger.create((Short) obj);
        if (obj instanceof String) return CborTextString.create((String) obj);
        if (obj instanceof byte[]) return CborByteString.create((byte[]) obj);

        if (obj instanceof Boolean) {
            if ((Boolean) obj) {
                return CborSimple.TRUE;
            } else {
                return CborSimple.FALSE;
            }
        }

        if (obj instanceof Map) {
            return CborMap.createFromJavaObject((Map<?, ?>) obj);
        }

        if (obj instanceof Iterable) {
            return CborArray.createFromJavaObject((Iterable<?>) obj);
        }

        if (obj instanceof URI) {
            URI uri = (URI) obj;
            return CborTextString.create(uri.toASCIIString(), CborTag.URI);
        }

        if (obj instanceof BigInteger) {
            BigInteger bint = (BigInteger) obj;

            final int tag;

            if (bint.signum() < 0) {
                tag = CborTag.BIGNUM_NEG;
                bint = bint.negate();
            } else {
                tag = CborTag.BIGNUM_POS;
            }

            final byte[] byteArray = bint.toByteArray();

            if (byteArray.length > 0 && (byteArray[0] == 0)) {
                // Sometimes a zero byte will be prepended, because toByteArray()
                // returns a signed representation. Since we already know the sign of
                // this value, we can omit the first byte if (and only if) it is zero.
                return CborByteString.create(byteArray, 1, byteArray.length - 1, tag);
            } else {
                return CborByteString.create(byteArray, 0, byteArray.length, tag);
            }
        }

        if (obj instanceof int[]) {
            return CborArray.createFromJavaObject((int[]) obj);
        }

        if (obj instanceof short[]) {
            return CborArray.createFromJavaObject((short[]) obj);
        }

        if (obj instanceof long[]) {
            return CborArray.createFromJavaObject((long[]) obj);
        }

        if (obj instanceof boolean[]) {
            return CborArray.createFromJavaObject((boolean[]) obj);
        }

        if (obj instanceof float[]) {
            return CborArray.createFromJavaObject((float[]) obj);
        }

        if (obj instanceof double[]) {
            return CborArray.createFromJavaObject((double[]) obj);
        }

        if (obj instanceof Object[]) {
            return CborArray.createFromJavaObject((Object[]) obj);
        }

        throw new CborConversionException(
                "Unable to convert java type \""
                        + obj.getClass().getCanonicalName()
                        + "\" to CborObject");
    }

    /**
     * Returns the tag for this object.
     *
     * <p>If the object is not tagged, returns {@link CborTag#UNTAGGED}.
     *
     * @return a number identifying the tag for this object, or {@link CborTag#UNTAGGED} if
     *     untagged.
     * @see CborTag
     */
    public int getTag() {
        return CborTag.UNTAGGED;
    }

    /**
     * Returns the object's "Major Type", as defined by RFC7049. See RFC7049 section 2 for more
     * information.
     *
     * @see CborMajorType
     * @see #getAdditionalInformation()
     */
    public abstract int getMajorType();

    /**
     * Returns the object's "additional information" value. The meaning of this field is dependent
     * on the major type. See RFC7049 section 2 for more information.
     *
     * @see #getMajorType()
     */
    public abstract int getAdditionalInformation();

    /**
     * Indicates if this data item can be losslessly converted to a JSON fragment.
     *
     * <p>Note that, with {@link CborTag#EXPECTED_BASE16 few} {@link CborTag#EXPECTED_BASE64
     * exceptions}, tags are not considered in this determination, since {@link #toJsonString()}
     * will strip out tags anyway.
     *
     * @return {@code true} if this object could be rendered as JSON, {@code false} otherwise.
     */
    public abstract boolean isValidJson();

    /**
     * Returns a representation of this data item as <em>JSON text</em>.
     *
     * <p>The result is guaranteed to be syntactically-valid <a
     * href="https://tools.ietf.org/html/rfc8259">RFC8259</a> JSON text. In the event that there is
     * no JSON text representation for the underlying data item, its value will be replaced with a
     * placeholder JSON value (usually "{@code null}"). This condition can be detected beforehand
     * using {@link #isValidJson()}.
     *
     * <p>Note that, in some pre-RFC8259 contexts, this method could be described as returning a
     * "JSON fragment" unless the data item was of the types {@link CborMap} (for a <em>JSON
     * object</em>) or {@link CborArray} (for a <em>JSON array</em>).
     *
     * @return a {@link String} containing representation of this data item as <em>JSON text</em>
     * @see #isValidJson()
     * @see <a href="https://tools.ietf.org/html/rfc8259">RFC8259</a>
     */
    public abstract String toJsonString();

    /**
     * Converts the given data item into a standard java object representation.
     *
     * <p>Tags are largely ignored, except in cases where the tag itself has defined behavior when
     * converting to JSON, like {@link CborTag#EXPECTED_BASE16} and {@link CborTag#EXPECTED_BASE64}.
     *
     * <p>The resulting object type is dependent on the data item:
     *
     * <ul>
     *   <li>{@link CborMap} → {@link java.util.Map}{@code <}{@link Object}{@code ,}{@link
     *       Object}{@code >}
     *   <li>{@link CborArray} → {@link java.util.Collection}{@code <}{@link Object}{@code >}
     *   <li>{@link CborFloat} → {@link Float}/{@link Double}
     *   <li>{@link CborInteger} → {@link Integer}/{@link Long}
     *   <li>{@link CborTextString} → {@link String}
     *   <li>{@link CborByteString} → {@code byte[]}
     *   <li>{@link CborSimple#TRUE}/{@link CborSimple#FALSE} → {@link Boolean}
     *   <li>Other {@link CborSimple} values → {@code null}
     * </ul>
     *
     * The returned value is a wholly independent copy.
     *
     * @see #createFromJavaObject(Object)
     */
    @Nullable
    public abstract Object toJavaObject();

    /**
     * Attempts to convert this data item to a java object with a specific class.
     *
     * <p>Unlike {@link #toJavaObject()}, this method will introspect into tags to figure out
     * conversions to {@link BigInteger} and {@link URI}. Unknown tags are ignored.
     *
     * <p>This method will return the most expressive result that satisfies the {@code clazz}
     * parameter. For example, if the data item is a {@link CborByteString} tagged {@link
     * CborTag#BIGNUM_POS}, calling this method with different classes will lead to different
     * results:
     *
     * <ul>
     *   <li>{@code toJavaObject(byte[].class} would return a {@code byte[]}
     *   <li>{@code toJavaObject(Object.class} would return a {@link BigInteger}
     *   <li>{@code toJavaObject(BigInteger.class} would return a {@link BigInteger}
     *   <li>{@code toJavaObject(URI.class} would throw {@link CborConversionException}
     * </ul>
     *
     * Whereas this method called an untagged {@link CborByteString} would behave like this:
     *
     * <ul>
     *   <li>{@code toJavaObject(byte[].class} would return a {@code byte[]}
     *   <li>{@code toJavaObject(Object.class} would return a {@code byte[]}
     *   <li>{@code toJavaObject(BigInteger.class} would throw {@link CborConversionException}
     *   <li>{@code toJavaObject(URI.class} would throw {@link CborConversionException}
     * </ul>
     *
     * @param clazz The desired class of the returned object
     * @return a reference to an object of class {@code clazz} or {@code null} if this data item was
     *     {@link CborSimple#NULL} or {@link CborSimple#UNDEFINED}.
     * @throws CborConversionException if the underlying data item could not be represented as an
     *     instance of {@code clazz}.
     */
    public abstract <T> T toJavaObject(Class<T> clazz) throws CborConversionException;

    /**
     * Creates an independent, deep copy of this data item. If the object that is being copied is
     * immutable, this method may simple return a reference to the object.
     *
     * @return new {@link CborObject}
     */
    public abstract CborObject copy();

    /**
     * Convenience method to quickly obtaining a CBOR byte-encoding of this data item.
     *
     * <p>Depending on your needs it may be more appropriate to directly use {@link CborWriter},
     * which allows for the use of {@link OutputStream} and {@link ByteBuffer} objects.
     *
     * @see CborWriter
     */
    public final byte[] toCborByteArray() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(CborWriter.length(this));
        try {
            CborWriter.createFromByteBuffer(byteBuffer).writeDataItem(this);
        } catch (IOException x) {
            // This should not happen.
            throw new CborRuntimeException(x);
        }
        return byteBuffer.array();
    }

    /**
     * Describes this data item using CBOR diagnostic notation.
     *
     * <p>The output is similar to that of {@link #toJsonString()} except that it also includes tag
     * and encoding information that is CBOR-specific.
     *
     * <p>The returned string is intended as a diagnostic aid and not intended to be
     * machine-readable.
     *
     * @return a {@link String} describing the data item.
     * @see <a href="https://tools.ietf.org/html/rfc7049#section-6">CBOR Diagnostic Notation</a>
     */
    @Override
    public abstract String toString();
}
