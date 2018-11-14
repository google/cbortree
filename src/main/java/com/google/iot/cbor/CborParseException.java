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

/**
 * Checked CBOR data parsing exception. Thrown when an error is encountered parsing CBOR data, such
 * as the data being corrupt the fundamental CBOR data type doesn't match what was excepted(such as
 * for {@link CborMap#createFromCborByteArray(byte[])}).
 */
public class CborParseException extends CborException {
    public CborParseException() {}

    public CborParseException(String explain) {
        super(explain);
    }

    public CborParseException(String explain, Throwable t) {
        super(explain, t);
    }

    public CborParseException(Throwable t) {
        super(t);
    }
}
