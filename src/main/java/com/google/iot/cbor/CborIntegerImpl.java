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

final class CborIntegerImpl extends CborInteger {
    private final long mValue;
    private final int mTag;
    private final Integer mMajorType;

    @Override
    public int getTag() {
        return mTag;
    }

    CborIntegerImpl(long value, int tag, @Nullable Integer majorType) {
        if (!CborTag.isValid(tag)) {
            throw new IllegalArgumentException("Invalid tag value " + tag);
        }

        mTag = tag;
        mValue = value;
        mMajorType = majorType;
    }

    @Override
    public int getMajorType() {
        if(mMajorType != null) return mMajorType;
        return super.getMajorType();
    }

    @Override
    public long longValue() {
        return mValue;
    }
}
