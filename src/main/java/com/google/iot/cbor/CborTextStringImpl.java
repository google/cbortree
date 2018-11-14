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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

final class CborTextStringImpl extends CborTextString {
    private final String mValue;
    private final byte[] mByteValue;
    private final int mTag;

    @Override
    public int getTag() {
        return mTag;
    }

    CborTextStringImpl(String value, int tag) {
        if (!CborTag.isValid(tag)) {
            throw new IllegalArgumentException("Invalid tag value " + tag);
        }

        mTag = tag;
        mValue = value;
        mByteValue = mValue.getBytes(StandardCharsets.UTF_8);
    }

    CborTextStringImpl(byte[] array, int offset, int length, int tag) {
        if (!CborTag.isValid(tag)) {
            throw new IllegalArgumentException("Invalid tag value " + tag);
        }

        mTag = tag;
        mValue = new String(array, offset, length, StandardCharsets.UTF_8);
        mByteValue = Arrays.copyOfRange(array, offset, offset + length);
    }

    @Override
    public String stringValue() {
        return mValue;
    }

    @Override
    public byte[] byteArrayValue() {
        return mByteValue;
    }
}
