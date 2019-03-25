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
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Class for representing CBOR map data items.
 *
 * <p>This class is used to represent data items of major type 5 ({@link CborMajorType#MAP}).
 */
public abstract class CborMap extends CborObject {
    // Prohibit users from subclassing for now.
    CborMap() {}

    /**
     * Parses the given CBOR byte array into a {@link CborMap} object. If the data in the byte array
     * is corrupted or if the type of object represented in the byte array isn't a CBOR Map, {@link
     * CborParseException} is thrown. This method is similar to {@link
     * CborObject#createFromCborByteArray(byte[], int, int)}, except that it is {@link
     * CborMap}-specific.
     *
     * @param input CBOR-encoded byte array. Must be a CBOR Map.
     * @param offset Offset into <code>input</code> to start parsing.
     * @param length the number of bytes to parse
     * @return the created {@link CborMap} object
     * @throws CborParseException if the input data is corrupt or if the input data doesn't
     *     represent a CborMap.
     * @throws IndexOutOfBoundsException if {@code offset} ois out of bounds
     * @see #createFromCborByteArray(byte[])
     * @see CborObject#createFromCborByteArray(byte[], int, int)
     */
    public static CborMap createFromCborByteArray(byte[] input, int offset, int length)
            throws CborParseException {

        CborObject obj = CborObject.createFromCborByteArray(input, offset, length);

        if (obj instanceof CborMap) {
            return (CborMap) obj;
        }

        throw new CborParseException("Not a map");
    }

    /**
     * Parses the given CBOR byte array into a {@link CborMap} object. If the data in the byte array
     * is corrupted or if the type of object represented in the byte array isn't a CBOR Map, {@link
     * CborParseException} is thrown. This method is similar to {@link
     * CborObject#createFromCborByteArray(byte[])}, except that it is {@link CborMap}-specific.
     *
     * @param input CBOR-encoded byte array. Must be a CBOR Map.
     * @return the created {@link CborMap} object
     * @throws CborParseException if the input data is corrupt or if the input data doesn't
     *     represent a CborMap.
     * @see #createFromCborByteArray(byte[], int, int)
     * @see CborObject#createFromCborByteArray(byte[])
     */
    public static CborMap createFromCborByteArray(byte[] input) throws CborParseException {
        return createFromCborByteArray(input, 0, input.length);
    }

    /**
     * Converts the given Java {@link Map} into a {@link CborMap} object. If the map contains keys
     * or values that cannot be converted to {@link CborObject} instances, then {@link
     * CborConversionException} is thrown. The returned {@link CborMap} will be untagged.
     *
     * @param obj the map to convert to a {@link CborMap}.
     * @return the created {@link CborMap} object
     * @throws CborConversionException if any of the map's keys or values cannot be represented as a
     *     {@link CborObject}.
     */
    public static CborMap createFromJavaObject(Map<?, ?> obj) throws CborConversionException {
        CborMap map = CborMap.create();
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet()) {
            map.mapValue()
                    .put(
                            CborObject.createFromJavaObject(entry.getKey()),
                            CborObject.createFromJavaObject(entry.getValue()));
        }
        return map;
    }

    /**
     * Creates an empty, tagged {@link CborMap} object.
     *
     * @param tag the tag to use on the new {@link CborMap} object
     * @return the created {@link CborMap} object
     */
    public static CborMap create(int tag) {
        return new CborMapImpl(tag);
    }

    /**
     * Creates a tagged {@link CborMap} object populated with the keys and values from <code>map
     * </code>. The keys and values from <code>map</code> are used directly (they are not
     * deep-copied).
     *
     * @param map the {@link Map} to use to pre-populate the entries in the new {@link CborMap}.
     * @param tag the tag to use on the new {@link CborMap} object
     * @return the created {@link CborMap} object
     */
    public static CborMap create(Map<CborObject, CborObject> map, int tag) {
        return new CborMapImpl(map, tag);
    }

    /**
     * Creates a {@link CborMap} object populated with the keys and values from <code>map</code>.
     * The keys and values from <code>map</code> are used directly (they are not deep-copied).
     *
     * @param map the {@link Map} to use to pre-populate the entries in the new {@link CborMap}.
     * @return the created {@link CborMap} object
     */
    public static CborMap create(Map<CborObject, CborObject> map) {
        return create(map, CborTag.UNTAGGED);
    }

    /**
     * Creates a untagged {@link CborMap} object from the given {@link JSONObject}.
     *
     * @param obj the {@link JSONObject} used to pre-populate the entries in the new {@link
     *     CborMap}.
     * @return the created {@link CborMap} object
     */
    public static CborMap createFromJSONObject(JSONObject obj) {
        CborMap map = create();

        for (String key : obj.keySet()) {
            final CborObject value;

            if (obj.isNull(key)) {
                value = CborSimple.NULL;

            } else {
                Object rawValue = obj.get(key);

                if (rawValue instanceof JSONArray) {
                    value = CborArray.createFromJSONArray((JSONArray) rawValue);

                } else if (rawValue instanceof JSONObject) {
                    value = CborMap.createFromJSONObject((JSONObject) rawValue);

                } else {
                    try {
                        value = CborObject.createFromJavaObject(rawValue);
                    } catch (CborConversionException x) {
                        // Should not happen, but rethrow as a runtime exception if it does.
                        throw new CborRuntimeException(x);
                    }
                }
            }

            map.put(key, value);
        }

        return map;
    }

    /**
     * Creates an empty, untagged {@link CborMap} object.
     *
     * @return the created {@link CborMap} object
     */
    public static CborMap create() {
        return create(CborTag.UNTAGGED);
    }

    public abstract Map<CborObject, CborObject> mapValue();

    public int size() {
        return mapValue().size();
    }

    public boolean isEmpty() {
        return mapValue().isEmpty();
    }

    @CanIgnoreReturnValue
    public CborObject remove(CborObject key) {
        return mapValue().remove(key);
    }

    public void clear() {
        mapValue().clear();
    }

    public Set<Map.Entry<CborObject, CborObject>> entrySet() {
        return mapValue().entrySet();
    }

    public Set<CborObject> keySet() {
        return mapValue().keySet();
    }

    public CborObject get(CborObject key) {
        return mapValue().get(key);
    }

    /**
     * Convenience getter method to allow the use of standard {@link String} objects when the keys
     * in the map are all {@link CborTextString}. The given key WILL NOT match tagged {@link
     * CborTextString} objects, only untagged objects will match.
     *
     * @param key the string value to use as a key. i * @return The {@link CborObject} value, if
     *     found. {@code null} otherwise.
     * @see #put(String, CborObject)
     * @see #containsKey(String)
     * @see #remove(String)
     * @see #keySetAsStrings()
     */
    public final CborObject get(String key) {
        return mapValue().get(CborTextString.create(key));
    }

    /**
     * Convenience test method to allow the use of standard {@link String} objects when the keys in
     * the map are all {@link CborTextString}. The given key WILL NOT match tagged {@link
     * CborTextString} objects, only untagged objects will match.
     *
     * @param key the string value to use as a key.
     * @return true if there is a corresponding {@link CborTextString} object, false otherwise.
     * @see #put(String, CborObject)
     * @see #get(String)
     * @see #remove(String)
     * @see #keySetAsStrings()
     */
    public final boolean containsKey(String key) {
        return mapValue().containsKey(CborTextString.create(key));
    }

    /**
     * Convenience setter method to allow the use of standard {@link String} objects when the keys
     * in the map are all {@link CborTextString}. The given key WILL NOT match tagged {@link
     * CborTextString} objects, only untagged objects will match.
     *
     * @param key the string value to use as a key.
     * @param value the CborObject to associate with this key.
     * @return the previous value associated with key, or null if there was no mapping for key.
     * @see #get(String)
     * @see #containsKey(String)
     * @see #remove(String)
     * @see #keySetAsStrings()
     */
    @CanIgnoreReturnValue
    public final CborObject put(String key, CborObject value) {
        return mapValue().put(CborTextString.create(key), value);
    }

    /**
     * Convenience removal method to allow the use of standard {@link String} objects when the keys
     * in the map are all {@link CborTextString}. The given key WILL NOT match tagged {@link
     * CborTextString} objects, only untagged objects will match.
     *
     * @param key the string value to use as a key.
     * @return the previous value associated with key, or null if there was no mapping for key.
     * @see #get(String)
     * @see #containsKey(String)
     * @see #keySetAsStrings()
     */
    @CanIgnoreReturnValue
    public final CborObject remove(String key) {
        return mapValue().remove(CborTextString.create(key));
    }

    /**
     * Returns the key set as standard {@link String} objects instead of {@link CborObject}
     * instances. {@link #areAllKeysStrings()} must return true in order for this method to be
     * usable.
     *
     * @throws CborConversionException if there are keys in this map which are not {@link
     *     CborTextString} instances.
     * @see #put(String, CborObject)
     * @see #get(String)
     * @see #containsKey(String)
     */
    public final Set<String> keySetAsStrings() throws CborConversionException {
        Set<String> ret = new HashSet<>();
        for (CborObject key : mapValue().keySet()) {
            if (!(key instanceof CborTextString)) {
                throw new CborConversionException("Key is not a string");
            }
            ret.add(((CborTextString) key).stringValue());
        }
        return ret;
    }

    /**
     * Determines if all of the keys in this map are {@link CborTextString} instances.
     *
     * @return true if all of the keys in this map are {@link CborTextString} instances or if there
     *     are no entries in this map; false otherwise.
     * @see #keySetAsStrings()
     */
    public final boolean areAllKeysStrings() {
        for (CborObject key : mapValue().keySet()) {
            if (!(key instanceof CborTextString)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Converts the {@link CborMap} into a standard Java {@link Map}{@code <String,Object>} that is
     * guaranteed to use {@link String} objects for keys. This method differs from {@link
     * CborObject#toJavaObject()} in that this method is guaranteed to return a map that is keyed
     * only with {@link String} objects.
     *
     * @throws CborConversionException if not all of the keys are {@link CborTextString} objects.
     */
    @SuppressWarnings("unchecked")
    public final Map<String, Object> toNormalMap() throws CborConversionException {
        if (!areAllKeysStrings()) {
            throw new CborConversionException("Not all keys are strings");
        }
        return (Map) toJavaObject();
    }

    @Override
    public final int getMajorType() {
        return CborMajorType.MAP;
    }

    @Override
    public final int getAdditionalInformation() {
        return CborInteger.calcAdditionalInformation(mapValue().size());
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Map<Object, Object> toJavaObject() {
        final Map<Object, Object> ret = new LinkedHashMap<>();

        for (Map.Entry<CborObject, CborObject> entry : mapValue().entrySet()) {
            ret.put(entry.getKey().toJavaObject(), entry.getValue().toJavaObject());
        }

        return ret;
    }

    @Override
    public <T> T toJavaObject(Class<T> clazz) throws CborConversionException {
        if (clazz.isAssignableFrom(Map.class)) {
            final Map<Object, Object> ret = new LinkedHashMap<>();

            for (Map.Entry<CborObject, CborObject> entry : mapValue().entrySet()) {
                ret.put(
                        entry.getKey().toJavaObject(Object.class),
                        entry.getValue().toJavaObject(Object.class));
            }

            return clazz.cast(ret);
        }

        throw new CborConversionException(clazz + " is not assignable from map");
    }

    /**
     * Creates an independent, deep copy of this {@link CborMap}.
     *
     * @return the new {@link CborMap} object.
     */
    @Override
    public final CborMap copy() {
        CborMap ret = create(getTag());
        for (Map.Entry<CborObject, CborObject> entry : mapValue().entrySet()) {
            ret.mapValue().put(entry.getKey().copy(), entry.getValue().copy());
        }
        return ret;
    }

    @Override
    public final boolean isValidJson() {
        for (Map.Entry<CborObject, CborObject> entry : mapValue().entrySet()) {
            if (!(entry.getKey() instanceof CborTextString)) {
                return false;
            }

            if (!entry.getValue().isValidJson()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public final String toJsonString() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<CborObject, CborObject> entry : mapValue().entrySet()) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            if (entry.getKey() instanceof CborTextString) {
                sb.append(entry.getKey().toJsonString());
            } else {
                sb.append(JSONObject.quote(entry.getKey().toJsonString()));
            }
            sb.append(":");
            sb.append(entry.getValue().toJsonString());
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getTag()) * 1337 + mapValue().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof CborMap)) {
            return false;
        }

        CborMap rhs = (CborMap) obj;

        return rhs.getTag() == getTag() && mapValue().equals(rhs.mapValue());
    }

    @Override
    public String toString(int indentLevel) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;

        if (indentLevel >= 0) {
            indentLevel++;
        }

        for (Map.Entry<CborObject, CborObject> entry : mapValue().entrySet()) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            if (indentLevel >= 0) {
                sb.append("\n");
                for (int i = 0; i < indentLevel; i++) {
                    sb.append("\t");
                }
            }

            sb.append(entry.getKey().toString(indentLevel));
            sb.append(":");
            sb.append(entry.getValue().toString(indentLevel));
        }

        if (!isEmpty() && indentLevel > 0) {
            indentLevel--;
            sb.append("\n");
            for (int i = 0; i < indentLevel; i++) {
                sb.append("\t");
            }
        }

        sb.append("}");

        int tag = getTag();

        return tag == CborTag.UNTAGGED ? sb.toString() : tag + "(" + sb.toString() + ")";
    }
}
