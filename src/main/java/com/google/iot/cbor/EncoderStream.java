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
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

/** Internal helper class for {@link CborWriterImpl}. */
@SuppressWarnings("UnusedReturnValue")
interface EncoderStream {

    int length();

    @CanIgnoreReturnValue
    EncoderStream put(byte x) throws IOException;

    @CanIgnoreReturnValue
    EncoderStream putShort(short x) throws IOException;

    @CanIgnoreReturnValue
    EncoderStream putInt(int x) throws IOException;

    @CanIgnoreReturnValue
    EncoderStream putLong(long x) throws IOException;

    @CanIgnoreReturnValue
    default EncoderStream put(byte[] x) throws IOException {
        for (byte b : x) {
            put(b);
        }
        return this;
    }

    @CanIgnoreReturnValue
    default EncoderStream putHalf(float x) throws IOException {
        return putShort(Half.floatToRawShortBits(x));
    }

    @CanIgnoreReturnValue
    default EncoderStream putFloat(float x) throws IOException {
        return putInt(Float.floatToRawIntBits(x));
    }

    @CanIgnoreReturnValue
    default EncoderStream putDouble(double x) throws IOException {
        return putLong(Double.doubleToRawLongBits(x));
    }

    static EncoderStream create(OutputStream outputStream) {
        return new EncoderStream_OutputStream(outputStream);
    }

    static EncoderStream create(ByteBuffer byteBuffer) {
        return new EncoderStream_ByteBuffer(byteBuffer);
    }

    static EncoderStream create() {
        return new EncoderStream_Empty();
    }
}

class EncoderStream_Empty implements EncoderStream {
    private int mLength = 0;

    @Override
    public int length() {
        return mLength;
    }

    @Override
    public EncoderStream put(byte x) {
        mLength++;
        return this;
    }

    @Override
    public EncoderStream putShort(short x) {
        mLength += 2;
        return this;
    }

    @Override
    public EncoderStream putInt(int x) {
        mLength += 4;
        return this;
    }

    @Override
    public EncoderStream putLong(long x) {
        mLength += 8;
        return this;
    }
}

class EncoderStream_ByteBuffer implements EncoderStream {
    private final ByteBuffer mByteBuffer;
    private int mLength = 0;

    EncoderStream_ByteBuffer(ByteBuffer byteBuffer) {
        mByteBuffer = byteBuffer;
    }

    @Override
    public int length() {
        return mLength;
    }

    @Override
    public EncoderStream put(byte x) throws IOException {
        try {
            mByteBuffer.put(x);
        } catch (IndexOutOfBoundsException | ReadOnlyBufferException e) {
            throw new IOException(e);
        }
        mLength++;
        return this;
    }

    @Override
    public EncoderStream putShort(short x) throws IOException {
        try {
            mByteBuffer.putShort(x);
        } catch (IndexOutOfBoundsException | ReadOnlyBufferException e) {
            throw new IOException(e);
        }
        mLength += 2;
        return this;
    }

    @Override
    public EncoderStream putInt(int x) throws IOException {
        try {
            mByteBuffer.putInt(x);
        } catch (IndexOutOfBoundsException | ReadOnlyBufferException e) {
            throw new IOException(e);
        }
        mLength += 4;
        return this;
    }

    @Override
    public EncoderStream putLong(long x) throws IOException {
        try {
            mByteBuffer.putLong(x);
        } catch (IndexOutOfBoundsException | ReadOnlyBufferException e) {
            throw new IOException(e);
        }
        mLength += 8;
        return this;
    }
}

class EncoderStream_OutputStream implements EncoderStream {
    private final OutputStream mOutputStream;
    private int mLength = 0;

    EncoderStream_OutputStream(OutputStream outputStream) {
        mOutputStream = outputStream;
    }

    @Override
    public int length() {
        return mLength;
    }

    public EncoderStream put(byte x) throws IOException {
        mOutputStream.write(x & 0xFF);
        mLength++;
        return this;
    }

    public EncoderStream putShort(short x) throws IOException {
        mOutputStream.write((x >> 8) & 0xFF);
        mOutputStream.write(x & 0xFF);
        mLength += 2;
        return this;
    }

    public EncoderStream putInt(int x) throws IOException {
        mOutputStream.write((x >> 24) & 0xFF);
        mOutputStream.write((x >> 16) & 0xFF);
        mOutputStream.write((x >> 8) & 0xFF);
        mOutputStream.write(x & 0xFF);
        mLength += 4;
        return this;
    }

    public EncoderStream putLong(long x) throws IOException {
        mOutputStream.write((int) (x >> 56) & 0xFF);
        mOutputStream.write((int) (x >> 48) & 0xFF);
        mOutputStream.write((int) (x >> 40) & 0xFF);
        mOutputStream.write((int) (x >> 32) & 0xFF);
        mOutputStream.write((int) (x >> 24) & 0xFF);
        mOutputStream.write((int) (x >> 16) & 0xFF);
        mOutputStream.write((int) (x >> 8) & 0xFF);
        mOutputStream.write((int) x & 0xFF);
        mLength += 8;
        return this;
    }
}
