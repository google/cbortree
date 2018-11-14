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

/**
 * Interface for serializing {@link CborObject} instances into CBOR-encoded data.
 *
 * @see CborReader
 */
public interface CborWriter {
    /** Creates a {@link CborWriter} instance that writes to an {@link OutputStream}. */
    static CborWriter createFromOutputStream(OutputStream outputStream) {
        return new CborWriterImpl(outputStream);
    }

    /** Creates a {@link CborWriter} instance that writes to a {@link ByteBuffer}. */
    static CborWriter createFromByteBuffer(ByteBuffer byteBuffer) {
        return new CborWriterImpl(byteBuffer);
    }

    /**
     * Calculates the CBOR-encoded length of the given {@link CborObject}.
     *
     * @param obj the {@link CborObject} instance to determine the encoded length of
     * @return the number of bytes required to serialize {@code obj}.
     */
    static int length(CborObject obj) {
        return CborWriterImpl.length(obj);
    }

    /**
     * Serializes a {@link CborObject}.
     *
     * @param obj the {@link CborObject} to serialize
     * @return an instance of this interface
     * @throws IOException if there was a problem with the output mechanism
     */
    @CanIgnoreReturnValue
    CborWriter writeDataItem(CborObject obj) throws IOException;

    /**
     * Serializes a CBOR tag. This method is useful for prepending the tag {@link
     * CborTag#SELF_DESCRIBE_CBOR}. The tag will be associated with the next object serialized with
     * {@link #writeDataItem}.
     *
     * @param tag the value of the tag to serialize
     * @return an instance of this interface
     * @throws IOException if there was a problem with the output mechanism
     */
    @CanIgnoreReturnValue
    CborWriter writeTag(int tag) throws IOException;
}
