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

import java.util.*;
import org.checkerframework.checker.nullness.qual.Nullable;

final class CborArrayImpl extends CborArray {
    private final LinkedList<CborObject> mList = new LinkedList<>();
    private int mTag = CborTag.UNTAGGED;

    CborArrayImpl(@Nullable Iterable<CborObject> list, int tag) {
        mTag = tag;
        if (list != null) {
            for (CborObject obj : list) {
                add(obj.copy());
            }
        }
    }

    CborArrayImpl() {}

    @Override
    public int getTag() {
        return mTag;
    }

    @Override
    public List<CborObject> listValue() {
        return mList;
    }
}
