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

import java.net.URI;
import java.net.URISyntaxException;
import org.json.JSONObject;

/** CBOR text string object interface. */
public abstract class CborTextString extends CborObject {
    // Prohibit users from subclassing for now.
    CborTextString() {}

    public static CborTextString create(byte[] array, int offset, int length, int tag) {
        return new CborTextStringImpl(array, offset, length, tag);
    }

    public static CborTextString create(byte[] array, int offset, int length) {
        return new CborTextStringImpl(array, offset, length, CborTag.UNTAGGED);
    }

    public static CborTextString create(byte[] array) {
        return create(array, 0, array.length);
    }

    public static CborTextString create(String string, int tag) {
        return new CborTextStringImpl(string, tag);
    }

    public static CborTextString create(String string) {
        return new CborTextStringImpl(string, CborTag.UNTAGGED);
    }

    public abstract String stringValue();

    public abstract byte[] byteArrayValue();

    @Override
    public final int getMajorType() {
        return CborMajorType.TEXT_STRING;
    }

    @Override
    public int getAdditionalInformation() {
        return CborInteger.calcAdditionalInformation(byteArrayValue().length);
    }

    @Override
    public final boolean isValidJson() {
        // CborTextStrings are always valid JSON.
        return true;
    }

    @Override
    public CborTextString copy() {
        // CborTextString instances are immutable, so we can simply return this.
        return this;
    }

    @Override
    public final String toJsonString() {
        return JSONObject.quote(stringValue())
                /* Remove redundant escaping */
                .replaceAll("\\\\/", "/");
    }

    @Override
    public String toJavaObject() {
        return stringValue();
    }

    @Override
    public <T> T toJavaObject(Class<T> clazz) throws CborConversionException {
        CborConversionException lastException = null;

        switch (getTag()) {
            case CborTag.URI:
                if (clazz.isAssignableFrom(URI.class)) {
                    try {
                        return clazz.cast(new URI(stringValue()));
                    } catch (URISyntaxException e) {
                        lastException = new CborConversionException(e);
                    }
                }
                break;

            default:
                break;
        }

        if (clazz.isAssignableFrom(String.class)) {
            return clazz.cast(stringValue());
        }

        if (lastException != null) {
            throw lastException;
        }

        throw new CborConversionException(clazz + " is not assignable from a text string");
    }

    @Override
    public int hashCode() {
        return (getTag() - CborTag.UNTAGGED) * 1337 + stringValue().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof CborTextString)) {
            return false;
        }

        CborTextString rhs = (CborTextString) obj;

        return getTag() == rhs.getTag() && stringValue().equals(rhs.stringValue());
    }

    @Override
    public String toString(int ignore) {
        return toString();
    }

    @Override
    public String toString() {
        String ret = toJsonString();

        int tag = getTag();

        return tag == CborTag.UNTAGGED ? ret : tag + "(" + ret + ")";
    }
}
