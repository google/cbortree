CborTree
========

CborTree is a Java library for encoding/decoding [CBOR](https://tools.ietf.org/html/rfc7049)
data items to/from a convenient object representation. It can also be used to convert
to/from JSON and standard Java object representations.

## Features ##

 * Full support for [RFC7049](https://tools.ietf.org/html/rfc7049)
 * Partial streaming support via `CoapReader` and `CoapWriter`
 * Supports easy conversions...
    * ...to/from standard Java objects/collections/maps via `toJavaObject()` method
    * ...to JSON representation via `toJsonString()` method
    * ...to/from `JSONObject`/`JSONArray` objects (from the common `org.json` API)
 * Supports `BigInteger`
 * Supports reading and writing half (16-bit) floating-point numbers
 * RFC7049 diagnostic notation available via `toString()`

## Current Limitations ##

 * Multiple tags on a single data item are not supported
 * Tags currently are limited to values no larger than `Integer.MAX_VALUE` (2,147,483,647).

## Planned Features ##

 * Canonicalization as described in [RFC7049 Section 3.9](https://tools.ietf.org/html/rfc7049#section-3.9)
 * Bytecode builder for directly generating CBOR bytecode without an intermediate object representation
 * Object builder for more easily generating `CborObject` trees than via manual creation.

## Documentation ##

 * [API JavaDoc](https://google.github.io/cbortree/releases/latest/apidocs/)
 * [Github Project](https://github.com/google/cbortree)

## Example ##

    byte[] cborBytes = new byte[] { (byte)0xd9, (byte)0xd9, (byte)0xf7,
                                    (byte)0xa2, 0x61, 0x61, 0x01, 0x61,
                                    0x62, (byte)0x82, 0x02, 0x03 };

    CborMap cborMap = CborMap.createFromCborByteArray(cborBytes);

    // Prints out the line `toString: 55799({"a":1,"b":[2,3]})`
    System.out.println("toString: " + cborMap);

    // Prints out the line `toJsonString: {"a":1,"b":[2,3]}`
    System.out.println("toJsonString: " + cborMap.toJsonString());

    CborArray cborArray = (CborArray)cborMap.get("b");

    float sum = 0;

    // Prints out `b: 2` and `b: 3`
    for (CborObject obj : cborArray) {
        System.out.println("b: " + obj);

        if (obj instanceof CborNumber) {
            sum += ((CborNumber)obj).floatValue();
        }
    }

    // Prints out `Sum: 5.0`
    System.out.println("Sum: " + sum);

## Building and Installing ##

This project uses Maven for building. Once Maven is installed, you
should be able to build and install the project by doing the
following:

    mvn verify
    mvn install

### Adding to Projects ###

Gradle:

    dependencies {
	  compile 'com.google.iot.cbor:cbor:0.01.00'
	}

Maven:

    <dependency>
	  <groupId>com.google.iot.cbor</groupId>
	  <artifactId>cbor</artifactId>
	  <version>0.01.00</version>
    </dependency>

## License ##

CborTree is released under the [Apache 2.0 license](LICENSE).

	Copyright 2018 Google Inc.

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.

## Disclaimer ##

This is not an officially supported Google product.

