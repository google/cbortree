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

final class CborFloatImpl extends CborFloat {
    private final double mValue;
    private final int mTag;
    private int mType;

    @Override
    public int getTag() {
        return mTag;
    }

    public static CborFloat createHalf(float value, int tag) {
        CborFloatImpl ret = new CborFloatImpl(value, tag);
        ret.mType = TYPE_HALF;
        return ret;
    }

    CborFloatImpl(float value, int tag) {
        if (!CborTag.isValid(tag)) {
            throw new IllegalArgumentException("Invalid tag value " + tag);
        }

        mTag = tag;
        mValue = value;
        mType = TYPE_FLOAT;
    }

    CborFloatImpl(double value, int tag) {
        if (!CborTag.isValid(tag)) {
            throw new IllegalArgumentException("Invalid tag value " + tag);
        }

        mTag = tag;
        mValue = value;
        mType = TYPE_DOUBLE;
    }

    @Override
    public int getAdditionalInformation() {
        return mType;
    }

    @Override
    public float floatValue() {
        return (float) mValue;
    }

    @Override
    public double doubleValue() {
        return mValue;
    }
}
