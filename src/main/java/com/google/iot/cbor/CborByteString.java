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

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Base64;

/** CBOR byte string object interface. */
public abstract class CborByteString extends CborObject {

    // Prohibit users from subclassing for now.
    CborByteString() {}

    public static CborByteString create(byte[] array, int offset, int length, int tag) {
        if (!CborTag.isValid(tag)) {
            throw new IllegalArgumentException("Invalid tag value " + tag);
        }

        return new CborByteStringImpl(array, offset, length, tag);
    }

    public static CborByteString create(byte[] array, int offset, int length) {
        return create(array, offset, length, CborTag.UNTAGGED);
    }

    public static CborByteString create(byte[] array) {
        return create(array, 0, array.length);
    }

    /**
     * Provides access to the underlying byte array value for this data item. Note that the caller
     * must avoid mutating the returned byte array value. If you might end up mutating the resulting
     * byte array, use {@link #toJavaObject()} instead: {@link #toJavaObject()} is guaranteed to
     * give you a fully independent object instance.
     *
     * @return the underlying byte array backing this data item.
     */
    public abstract byte[] byteArrayValue();

    @Override
    public int getMajorType() {
        return CborMajorType.BYTE_STRING;
    }

    @Override
    public int getAdditionalInformation() {
        return CborInteger.calcAdditionalInformation(byteArrayValue().length);
    }

    @Override
    public boolean isValidJson() {
        switch (getTag()) {
            case CborTag.EXPECTED_BASE64:
            case CborTag.EXPECTED_BASE16:
                // We are indeed valid JSON is we have either of these tags.
                return true;

            default:
                return false;
        }
    }

    private String toBase16String() {
        StringBuilder ret = new StringBuilder();
        for (byte b : byteArrayValue()) {
            ret.append(String.format("%02x", b));
        }
        return ret.toString();
    }

    @Override
    public String toJsonString() {
        if (getTag() == CborTag.EXPECTED_BASE16) {
            return "\"" + toBase16String() + "\"";

        } else {
            return "\"" + Base64.getEncoder().encodeToString(byteArrayValue()) + "\"";
        }
    }

    @Override
    public byte[] toJavaObject() {
        byte[] value = byteArrayValue();
        return Arrays.copyOf(value, value.length);
    }

    @Override
    public <T> T toJavaObject(Class<T> clazz) throws CborConversionException {
        CborConversionException lastException = null;

        switch (getTag()) {
            case CborTag.BIGNUM_POS:
                if (clazz.isAssignableFrom(BigInteger.class)) {
                    return clazz.cast(new BigInteger(1, byteArrayValue()));
                }
                break;

            case CborTag.BIGNUM_NEG:
                if (clazz.isAssignableFrom(BigInteger.class)) {
                    return clazz.cast(new BigInteger(-1, byteArrayValue()));
                }
                break;

            case CborTag.EXPECTED_BASE16:
                if (clazz.isAssignableFrom(String.class)) {
                    return clazz.cast(toBase16String());
                }
                break;

            case CborTag.EXPECTED_BASE64:
                if (clazz.isAssignableFrom(String.class)) {
                    clazz.cast(Base64.getEncoder().encodeToString(byteArrayValue()));
                }
                break;

            case CborTag.CBOR_DATA_ITEM:
                if (CborObject.class.isAssignableFrom(clazz)
                        || clazz.isAssignableFrom(String.class)) {
                    CborObject obj;

                    try {
                        obj = CborObject.createFromCborByteArray(byteArrayValue());

                        if (clazz.isAssignableFrom(obj.getClass())) {
                            return clazz.cast(obj);
                        } else {
                            lastException =
                                    new CborConversionException(
                                            clazz + " is not assignable from " + obj.getClass());
                        }
                    } catch (CborParseException e) {
                        lastException = new CborConversionException(e);
                    }
                }

            default:
                break;
        }

        if (clazz.isAssignableFrom(byte[].class)) {
            return clazz.cast(toJavaObject());
        }

        if (lastException != null) {
            throw lastException;
        }

        throw new CborConversionException(clazz + " is not assignable from byte string");
    }

    @Override
    public CborByteString copy() {
        // CborByteString objects are technically immutable, but as long
        // as we expose our underlying byte array via byteArrayValue()
        // then we stand the chance of it being mutated despite this.
        // Because of this we go ahead and make a real copy here, as if
        // we were mutable.
        final byte[] array = byteArrayValue();
        return create(array, 0, array.length, getTag());
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getTag()) * 1337 + Arrays.hashCode(byteArrayValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof CborByteString)) {
            return false;
        }

        final CborByteString rhs = (CborByteString) obj;

        return getTag() == rhs.getTag() && Arrays.equals(byteArrayValue(), rhs.byteArrayValue());
    }

    @Override
    public String toString(int ignore) {
        return toString();
    }

    @Override
    public String toString() {
        String ret = "h'" + toBase16String() + "'";

        int tag = getTag();

        return tag == CborTag.UNTAGGED ? ret : tag + "(" + ret + ")";
    }
}
