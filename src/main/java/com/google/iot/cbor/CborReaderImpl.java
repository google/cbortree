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

import java.io.*;
import java.nio.BufferUnderflowException;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

class CborReaderImpl implements CborReader {
    private static final boolean DEBUG = true;
    private static final Logger LOGGER = Logger.getLogger(CborReader.class.getCanonicalName());

    static final int UNSPECIFIED = -1;
    private static final byte BREAK = (byte) 0xFF;

    private final DecoderStream mDecoderStream;
    private int mRemainingObjects;
    private int mLastTag = CborTag.UNTAGGED;

    private CborReaderImpl(DecoderStream decoderStream, int objectCount) {
        mDecoderStream = decoderStream;
        mRemainingObjects = objectCount;
    }

    CborReaderImpl(InputStream inputStream, int objectCount) {
        this(DecoderStream.create(inputStream), objectCount);
    }

    CborReaderImpl(byte[] bytes, int offset, int objectCount) {
        this(new ByteArrayInputStream(bytes, offset, bytes.length - offset), objectCount);
        if (offset >= bytes.length) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public boolean hasRemainingDataItems() {
        try {
            if (mRemainingObjects < 0) {
                return mDecoderStream.hasRemaining() && (mDecoderStream.peek() != BREAK);
            }
            return mRemainingObjects != 0;
        } catch (IOException x) {
            x.printStackTrace();
            // We say true here so that we will call readDataItem() and get the exception
            return true;
        }
    }

    @Override
    public long bytesParsed() {
        return mDecoderStream.bytesParsed();
    }

    @Override
    public CborObject readDataItem() throws CborParseException, IOException {
        if (!hasRemainingDataItems()) {
            throw new NoSuchElementException();
        }

        int tag = mLastTag;
        mLastTag = CborTag.UNTAGGED;

        try {
            byte firstByte = mDecoderStream.get();
            int majorType = ((firstByte & 0xFF) >> 5);
            byte additionalInfo = (byte) (firstByte & 0x1F);
            long additionalData;

            if (additionalInfo < CborObject.ADDITIONAL_INFO_EXTRA_1B) {
                additionalData = additionalInfo;

            } else if (additionalInfo == CborObject.ADDITIONAL_INFO_EXTRA_1B) {
                additionalData = (mDecoderStream.get() & 0xFF);

            } else if (additionalInfo == CborObject.ADDITIONAL_INFO_EXTRA_2B) {
                additionalData = mDecoderStream.getShort() & 0xFFFF;

            } else if (additionalInfo == CborObject.ADDITIONAL_INFO_EXTRA_4B) {
                additionalData = mDecoderStream.getInt() & 0xFFFFFFFFL;

            } else if (additionalInfo == CborObject.ADDITIONAL_INFO_EXTRA_8B) {
                additionalData = mDecoderStream.getLong();

                // Overflow check
                if (additionalData < 0) {
                    final String explanation =
                            String.format(
                                    Locale.ENGLISH,
                                    "Additional data value was too large: 0x%X",
                                    additionalData);

                    if (majorType == CborMajorType.TAG) {
                        // If this was a tag, then we can simply ignore it.
                        LOGGER.warning(explanation + ", ignoring tag");
                        additionalData = CborTag.UNTAGGED;

                    } else {
                        // If this was a tag, then we can simply ignore it.
                        LOGGER.warning(explanation + ", stopping");
                        throw new CborParseException(explanation);
                    }
                }

            } else if (additionalInfo == CborObject.ADDITIONAL_INFO_EXTRA_INDEF) {
                additionalData = UNSPECIFIED;

            } else {
                throw new CborParseException(
                        "Undefined additional info value "
                                + additionalInfo
                                + " for major type "
                                + majorType);
            }

            switch (majorType) {
                case CborMajorType.TAG:
                    if (CborTag.isValid(additionalData)) {
                        mLastTag = (int) additionalData;

                    } else {
                        LOGGER.warning("Ignoring invalid tag: " + additionalData);
                    }

                    return readDataItem();

                case CborMajorType.POS_INTEGER:
                    if (additionalData < 0) {
                        throw new CborParseException();
                    } else {
                        if (mRemainingObjects != UNSPECIFIED) mRemainingObjects--;
                        return CborInteger.create(additionalData, tag);
                    }

                case CborMajorType.NEG_INTEGER:
                    if (additionalData < 0) {
                        throw new CborParseException();
                    } else {
                        if (mRemainingObjects != UNSPECIFIED) mRemainingObjects--;
                        return CborInteger.create(-1 - additionalData, tag);
                    }

                case CborMajorType.BYTE_STRING:
                    if (additionalData < 0) {
                        ByteArrayOutputStream aggregator = new ByteArrayOutputStream();
                        CborReaderImpl subparser =
                                new CborReaderImpl(mDecoderStream, (int) additionalData);
                        while (subparser.hasRemainingDataItems()) {
                            CborObject obj = subparser.readDataItem();
                            if (obj instanceof CborByteString
                                    && obj.getMajorType() == CborMajorType.BYTE_STRING) {
                                aggregator.write(((CborByteString) obj).byteArrayValue());
                            } else {
                                throw new CborParseException(
                                        "Unexpected major type in byte string stream");
                            }
                        }
                        if (mRemainingObjects != UNSPECIFIED) mRemainingObjects--;

                        if (mDecoderStream.get() != BREAK) {
                            throw new CborParseException("Missing break");
                        }

                        return CborByteString.create(
                                aggregator.toByteArray(), 0, aggregator.size(), tag);
                    } else {
                        byte[] bytes = new byte[(int) additionalData];
                        mDecoderStream.get(bytes);
                        if (mRemainingObjects != UNSPECIFIED) mRemainingObjects--;
                        return CborByteString.create(bytes, 0, bytes.length, tag);
                    }

                case CborMajorType.TEXT_STRING:
                    if (additionalData < 0) {
                        ByteArrayOutputStream aggregator = new ByteArrayOutputStream();
                        CborReaderImpl subparser =
                                new CborReaderImpl(mDecoderStream, (int) additionalData);
                        while (subparser.hasRemainingDataItems()) {
                            CborObject obj = subparser.readDataItem();
                            if (obj instanceof CborTextString) {
                                aggregator.write(((CborTextString) obj).byteArrayValue());
                            } else {
                                throw new CborParseException(
                                        "Unexpected major type in text string stream");
                            }
                        }
                        if (mRemainingObjects != UNSPECIFIED) mRemainingObjects--;

                        if (mDecoderStream.get() != BREAK) {
                            throw new CborParseException("Missing break");
                        }

                        return CborTextString.create(
                                aggregator.toByteArray(), 0, aggregator.size(), tag);
                    } else {
                        byte[] bytes = new byte[(int) additionalData];
                        mDecoderStream.get(bytes);
                        if (mRemainingObjects != UNSPECIFIED) mRemainingObjects--;
                        return CborTextString.create(bytes, 0, bytes.length, tag);
                    }

                case CborMajorType.ARRAY:
                    {
                        CborArray ret = CborArray.create(tag);
                        CborReaderImpl subparser =
                                new CborReaderImpl(mDecoderStream, (int) additionalData);
                        while (subparser.hasRemainingDataItems()) {
                            ret.add(subparser.readDataItem());
                        }
                        if (mRemainingObjects != UNSPECIFIED) mRemainingObjects--;

                        if ((additionalData == UNSPECIFIED && mDecoderStream.get() != BREAK)) {
                            throw new CborParseException("Missing break");
                        }
                        return ret;
                    }

                case CborMajorType.MAP:
                    {
                        CborMap ret = CborMap.create(tag);
                        if (additionalData != UNSPECIFIED) {
                            additionalData *= 2;
                        }
                        CborReaderImpl subparser =
                                new CborReaderImpl(mDecoderStream, (int) additionalData);

                        while (subparser.hasRemainingDataItems()) {
                            CborObject key = subparser.readDataItem();
                            CborObject value = subparser.readDataItem();
                            ret.mapValue().put(key, value);
                        }

                        if ((additionalData == UNSPECIFIED) && mDecoderStream.get() != BREAK) {
                            throw new CborParseException("Missing break");
                        }

                        if (mRemainingObjects != UNSPECIFIED) mRemainingObjects--;
                        return ret;
                    }

                case CborMajorType.OTHER:
                    if (additionalInfo == CborFloat.TYPE_HALF) {
                        // Half-precision float
                        if (mRemainingObjects != UNSPECIFIED) mRemainingObjects--;
                        return CborFloat.createHalf(
                                Half.shortBitsToFloat((short) additionalData), tag);

                    } else if (additionalInfo == CborFloat.TYPE_FLOAT) {
                        // Full-precision float
                        if (mRemainingObjects != UNSPECIFIED) mRemainingObjects--;
                        return CborFloat.create(Float.intBitsToFloat((int) additionalData), tag);

                    } else if (additionalInfo == CborFloat.TYPE_DOUBLE) {
                        // Double-precision float
                        if (mRemainingObjects != UNSPECIFIED) mRemainingObjects--;
                        return CborFloat.create(Double.longBitsToDouble(additionalData), tag);

                    } else {
                        if (mRemainingObjects != UNSPECIFIED) mRemainingObjects--;
                        return CborSimple.create((int) additionalData, tag);
                    }

                default:
                    throw new CborParseException("Invalid major type value " + majorType);
            }

        } catch (EOFException
                | BufferUnderflowException
                | NoSuchElementException
                | IllegalArgumentException x) {
            throw new CborParseException("CBOR data is truncated or corrupt", x);
        }
    }
}
