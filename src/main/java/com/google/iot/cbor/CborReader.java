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
import java.io.InputStream;
import java.util.NoSuchElementException;

/**
 * Interface for parsing CBOR data into {@link CborObject} instances.
 *
 * @see CborWriter
 * @see CborObject#createFromCborByteArray(byte[])
 * @see CborObject#createFromCborByteArray(byte[], int, int)
 */
public interface CborReader {
    /**
     * Creates a {@link CborReader} instance from an {@link InputStream} instance with a total
     * object count.
     *
     * @param inputStream the {@link InputStream} to parse
     * @param objectCount the number of top-level data items that will be generated
     * @return a new {@link CborReader} instance
     */
    static CborReader createFromInputStream(InputStream inputStream, int objectCount) {
        return new CborReaderImpl(inputStream, objectCount);
    }

    /**
     * Creates a {@link CborReader} instance from an {@link InputStream} instance.
     *
     * @param inputStream the {@link InputStream} to parse
     * @return a new {@link CborReader} instance
     */
    static CborReader createFromInputStream(InputStream inputStream) {
        return createFromInputStream(inputStream, CborReaderImpl.UNSPECIFIED);
    }

    /**
     * Creates a {@link CborReader} instance from a byte array with starting offset and a total
     * object count. The resulting {@link CborReader} will be configured to emit {@code objectCount}
     * data items. If the input data does not contain {@code objectCount} objects, a {@link
     * CborParseException} will be thrown from {@link #readDataItem()}}.
     *
     * @param bytes the byte array to parse
     * @param offset where in {@code bytes} to start parsing
     * @param objectCount the number of top-level data items that will be generated
     * @return a new {@link CborReader} instance
     */
    static CborReader createFromByteArray(byte[] bytes, int offset, int objectCount) {
        return new CborReaderImpl(bytes, offset, objectCount);
    }

    /**
     * Creates a {@link CborReader} instance from a byte array with starting offset.
     *
     * @param bytes the byte array to parse
     * @param offset where in {@code bytes} to start parsing
     * @return a new {@link CborReader} instance
     */
    static CborReader createFromByteArray(byte[] bytes, int offset) {
        return createFromByteArray(bytes, offset, CborReaderImpl.UNSPECIFIED);
    }

    /**
     * Creates a {@link CborReader} instance from a byte array.
     *
     * @param bytes the byte array to parse
     * @return a new {@link CborReader} instance
     */
    static CborReader createFromByteArray(byte[] bytes) {
        return createFromByteArray(bytes, 0);
    }

    /**
     * Determines if there are additional data items to parse.
     *
     * @return true if there are additional data items which can be retrieved via {@link
     *     #readDataItem()}, false otherwise.
     */
    boolean hasRemainingDataItems();

    /**
     * Reads the next available data item as a {@link CborObject}. This method will block until an
     * entire data item is read.
     *
     * @return the read data item
     * @throws CborParseException if the data item was corrupted
     * @throws IOException if there was a problem with the underlying input mechanism
     * @throws NoSuchElementException if there are no more data items to be read
     */
    CborObject readDataItem() throws CborParseException, IOException;

    /**
     * Gets the number of bytes that have been parsed by this instance.
     *
     * @return the number of parsed bytes
     */
    long bytesParsed();
}
