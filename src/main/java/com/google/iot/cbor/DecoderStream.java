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

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.*;

/** Internal helper class for {@link CborReaderImpl}. */
interface DecoderStream {
    static DecoderStream create(InputStream inputStream) {
        return new DecoderStream_InputStream(inputStream);
    }

    byte get() throws IOException;

    short getShort() throws IOException;

    int getInt() throws IOException;

    long getLong() throws IOException;

    boolean hasRemaining() throws IOException;

    int peek() throws IOException;

    long bytesParsed();

    default void get(byte[] x) throws IOException {
        for (int i = 0; i < x.length; ++i) {
            x[i] = get();
        }
    }
}

class DecoderStream_InputStream implements DecoderStream {
    private final InputStream mInputStream;
    private int mPeekedByte = -2;
    private boolean mDidPeek = false;
    private long mBytesParsed = 0;

    DecoderStream_InputStream(InputStream inputStream) {
        mInputStream = inputStream;
    }

    @Override
    public long bytesParsed() {
        return mBytesParsed;
    }

    @Override
    @CanIgnoreReturnValue
    public int peek() throws IOException {
        if (mDidPeek) {
            return (byte) mPeekedByte;
        }
        mPeekedByte = mInputStream.read();
        mDidPeek = true;
        return mPeekedByte;
    }

    @Override
    public byte get() throws IOException {
        int readByte;

        if (mDidPeek) {
            mDidPeek = false;
            readByte = mPeekedByte;
        } else {
            readByte = mInputStream.read();
        }

        if (readByte < 0) {
            throw new EOFException("No more bytes in input stream");
        }

        mBytesParsed++;

        return (byte) readByte;
    }

    @Override
    public short getShort() throws IOException {
        short ret;

        ret = (short) ((get() & 0xFF) << 8);
        ret |= (short) (get() & 0xFF);

        return ret;
    }

    @Override
    public int getInt() throws IOException {
        int ret;

        ret = ((get() & 0xFF) << 24);
        ret |= ((get() & 0xFF) << 16);
        ret |= ((get() & 0xFF) << 8);
        ret |= (get() & 0xFF);

        return ret;
    }

    @Override
    public long getLong() throws IOException {
        long ret;

        ret = ((long) (get() & 0xFF) << 56);
        ret |= ((long) (get() & 0xFF) << 48);
        ret |= ((long) (get() & 0xFF) << 40);
        ret |= ((long) (get() & 0xFF) << 32);
        ret |= ((long) (get() & 0xFF) << 24);
        ret |= ((long) (get() & 0xFF) << 16);
        ret |= ((long) (get() & 0xFF) << 8);
        ret |= ((long) get() & 0xFF);

        return ret;
    }

    @Override
    public boolean hasRemaining() throws IOException {
        if (!mDidPeek) {
            peek();
        }
        return mPeekedByte >= 0;
    }
}
