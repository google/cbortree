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

/** Contains CBOR tag constants. */
public final class CborTag {
    /** Indicates the absence of a CBOR tag. Note that this is not an actual CBOR tag value. */
    public static final int UNTAGGED = -1;

    /**
     * CBOR tag indicating that the tagged string is a RFC3339-formatted timestamp.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7049#section-2.4.1">RFC7049 section-2.4.1</a>
     */
    public static final int TIME_DATE_STRING = 0;

    /**
     * CBOR tag indicating that the tagged integer represents the number of seconds since <code>
     * 1970-01-01T00:00Z</code>.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7049#section-2.4.1">RFC7049 section-2.4.1</a>
     */
    public static final int TIMESTAMP_UNIX = 1;

    /**
     * CBOR tag indicating that the tagged byte string represents a positive bignum.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7049#section-2.4.2">RFC7049 section-2.4.2</a>
     */
    public static final int BIGNUM_POS = 2;

    /**
     * CBOR tag indicating that the tagged byte string represents a negative bignum.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7049#section-2.4.2">RFC7049 section-2.4.2</a>
     */
    public static final int BIGNUM_NEG = 3;

    /**
     * CBOR tag indicating that the tagged array is a decimal fraction. The array contains two
     * integers: a base-10 exponent followed by a mantissa. The value is defined as <code>m*(10^e)
     * </code>. The exponent MUST be a normal integer with a major type of zero or one. The mantissa
     * may be either a normal integer of a bignum.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7049#section-2.4.3">RFC7049 section-2.4.3</a>
     */
    public static final int FRACTION = 4;

    /**
     * CBOR tag indicating that the tagged array is a bigfloat. The array contains two integers: a
     * base-2 exponent followed by a mantissa. The value is defined as <code>m*(2^e)</code>. The
     * exponent MUST be a normal integer with a major type of zero or one. The mantissa may be
     * either a normal integer of a bignum.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7049#section-2.4.3">RFC7049 section-2.4.3</a>
     */
    public static final int BIGFLOAT = 5;

    /**
     * CBOR tag indicating that the tagged byte string is expected to be base64 when encoded as
     * JSON.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7049#section-2.4.4.2">RFC7049
     *     section-2.4.4.2</a>
     * @see <a href="https://tools.ietf.org/html/rfc4648">RFC4648</a>
     */
    public static final int EXPECTED_BASE64 = 22;

    /**
     * CBOR tag indicating that the tagged byte string is expected to be base16 when encoded as
     * JSON.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7049#section-2.4.4.2">RFC7049
     *     section-2.4.4.2</a>
     * @see <a href="https://tools.ietf.org/html/rfc4648">RFC4648</a>
     */
    public static final int EXPECTED_BASE16 = 23;

    /**
     * CBOR tag indicating that the tagged byte string contains a CBOR-encoded data item.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7049#section-2.4.4.2">RFC7049
     *     section-2.4.4.2</a>
     */
    public static final int CBOR_DATA_ITEM = 24;

    /**
     * CBOR tag indicating that the tagged text string is a URI.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7049#section-2.4.4.3">RFC7049
     *     section-2.4.4.3</a>
     */
    public static final int URI = 32;

    /**
     * CBOR tag indicating that the tagged text string is a base64url-encoded data.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7049#section-2.4.4.3">RFC7049
     *     section-2.4.4.3</a>
     * @see <a href="https://tools.ietf.org/html/rfc4648">RFC4648</a>
     */
    public static final int BASE64URL = 33;

    /**
     * CBOR tag indicating that the tagged text string is a base64-encoded data.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7049#section-2.4.4.3">RFC7049
     *     section-2.4.4.3</a>
     * @see <a href="https://tools.ietf.org/html/rfc4648">RFC4648</a>
     */
    public static final int BASE64 = 34;

    /**
     * CBOR tag indicating that the tagged text string is a regular expression in Perl Compatible
     * Regular Expressions (PCRE) / JavaScript syntax.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7049#section-2.4.4.3">RFC7049
     *     section-2.4.4.3</a>
     * @see <a
     *     href="http://www.ecma-international.org/publications/files/ecma-st/ECMA-262.pdf">ECMA-262</a>
     */
    public static final int REGEX = 35;

    /**
     * CBOR tag for facilitating self-describing CBOR encodings. This tag confers no inherent
     * meaning to the tagged CBOR object and is intended to be used as a encoding preamble to help
     * identify CBOR-formatted data.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7049#section-2.4.5">RFC7049 section-2.4.5</a>
     */
    public static final int SELF_DESCRIBE_CBOR = 55799;

    /** The largest tag value supported by this implementation. */
    public static final int MAX_VALUE = Integer.MAX_VALUE;

    /**
     * Indicates if the given tag value is considered valid.
     *
     * @param tag the tag value
     * @return true if the tag value is valid, false otherwise
     */
    public static boolean isValid(long tag) {
        return tag >= UNTAGGED && tag <= MAX_VALUE;
    }

    // Prevent instantiation
    private CborTag() {}
}
