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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Class for representing CBOR array data items.
 *
 * <p>This class is used to represent data items of major type 4 ({@link CborMajorType#ARRAY}).
 */
public abstract class CborArray extends CborObject implements Iterable<CborObject> {

    // Prohibit users from subclassing for now.
    CborArray() {}

    /**
     * Creates an untagged empty {@link CborArray}.
     *
     * @return new {@link CborArray} instance
     */
    public static CborArray create() {
        return new CborArrayImpl();
    }

    /**
     * Creates a tagged empty {@link CborArray}.
     *
     * @param tag the integer value of the tag
     * @return new {@link CborArray} instance
     */
    public static CborArray create(int tag) {
        if (!CborTag.isValid(tag)) {
            throw new IllegalArgumentException("Invalid tag value " + tag);
        }

        return new CborArrayImpl(null, tag);
    }

    /**
     * Creates an untagged {@link CborArray} populated with the given CborObjects. The given
     * CborObjects are used directly and not deep-copied.
     *
     * @param objs iterable object (like a {@link Collection})
     * @return new {@link CborArray} instance
     */
    public static CborArray create(Iterable<CborObject> objs) {
        return create(objs, CborTag.UNTAGGED);
    }

    /**
     * Creates a tagged {@link CborArray} populated with the given CborObjects. The given
     * CborObjects are used directly and not deep-copied.
     *
     * @param objs iterable object (like a {@link Collection})
     * @param tag the integer value of the tag
     * @return new {@link CborArray} instance
     */
    public static CborArray create(Iterable<CborObject> objs, int tag) {
        if (!CborTag.isValid(tag)) {
            throw new IllegalArgumentException("Invalid tag value " + tag);
        }

        return new CborArrayImpl(objs, tag);
    }

    /**
     * Converts an {@link Iterable} of Java objects to an untagged {@link CborArray}. Note that this
     * method differs from {@link #create(Iterable)} in that this method converts standard Java
     * objects (like String, Integer, etc) into CborObjects.
     *
     * @param obj the iterable of java objects to convert
     * @return new {@link CborArray} instance
     * @throws CborConversionException if one or more of the contained objects cannot be converted
     */
    public static CborArray createFromJavaObject(Iterable<?> obj) throws CborConversionException {
        CborArray array = CborArray.create();
        for (Object value : obj) {
            array.add(CborObject.createFromJavaObject(value));
        }
        return array;
    }

    /**
     * Converts an object array to an untagged {@link CborArray}.
     *
     * @param obj the object array to convert
     * @return new {@link CborArray} instance
     * @throws CborConversionException if one or more of the contained objects cannot be converted
     */
    public static CborArray createFromJavaObject(Object[] obj) throws CborConversionException {
        CborArray array = CborArray.create();
        for (Object value : obj) {
            array.add(CborObject.createFromJavaObject(value));
        }
        return array;
    }

    /**
     * Converts an integer array to a {@link CborArray} of {@link CborInteger} objects.
     *
     * @param obj the integer array to convert
     * @return new {@link CborArray} instance
     */
    public static CborArray createFromJavaObject(int[] obj) {
        CborArray array = CborArray.create();
        for (int value : (int[]) obj) {
            array.add(CborInteger.create(value));
        }
        return array;
    }

    /**
     * Converts a short array to a {@link CborArray} of {@link CborInteger} objects.
     *
     * @param obj the short array to convert
     * @return new {@link CborArray} instance
     */
    public static CborArray createFromJavaObject(short[] obj) {
        CborArray array = CborArray.create();
        for (short value : (short[]) obj) {
            array.add(CborInteger.create(value));
        }
        return array;
    }

    /**
     * Converts a long array to a {@link CborArray} of {@link CborInteger} objects.
     *
     * @param obj the long array to convert
     * @return new {@link CborArray} instance
     */
    public static CborArray createFromJavaObject(long[] obj) {
        CborArray array = CborArray.create();
        for (long value : (long[]) obj) {
            array.add(CborInteger.create(value));
        }
        return array;
    }

    /**
     * Converts a boolean array to a {@link CborArray} of {@link CborSimple} objects.
     *
     * @param obj the boolean array to convert
     * @return new {@link CborArray} instance
     */
    public static CborArray createFromJavaObject(boolean[] obj) {
        CborArray array = CborArray.create();
        for (boolean value : (boolean[]) obj) {
            array.add(value ? CborSimple.TRUE : CborSimple.FALSE);
        }
        return array;
    }

    /**
     * Converts a float array to a {@link CborArray} of {@link CborFloat} objects.
     *
     * @param obj the float array to convert
     * @return new {@link CborArray} instance
     */
    public static CborArray createFromJavaObject(float[] obj) {
        CborArray array = CborArray.create();
        for (float value : (float[]) obj) {
            array.add(CborFloat.create(value));
        }
        return array;
    }

    /**
     * Converts a double array to a {@link CborArray} of {@link CborFloat} objects.
     *
     * @param obj the double array to convert
     * @return new {@link CborArray} instance
     */
    public static CborArray createFromJavaObject(double[] obj) {
        CborArray array = CborArray.create();
        for (double value : (double[]) obj) {
            array.add(CborFloat.create(value));
        }
        return array;
    }

    /**
     * Creates a {@link CborArray} from a given {@link JSONArray}. The resulting {@link CborArray}
     * is wholly independent from the given {@link JSONArray}.
     *
     * @return new {@link CborArray} instance
     */
    public static CborArray createFromJSONArray(JSONArray jsonArray) {
        CborArray ret = create();
        for (int i = 0; i < jsonArray.length(); i++) {
            final CborObject value;

            if (jsonArray.isNull(i)) {
                value = CborSimple.NULL;

            } else {
                Object rawValue = jsonArray.get(i);

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

            ret.add(value);
        }
        return ret;
    }

    /**
     * Returns the underlying {@link List} object backing this {@link CborArray}.
     *
     * @return the backing {@link List} of {@link CborObject} instances.
     */
    public abstract List<CborObject> listValue();

    /**
     * Indicates the number of items in this array. This is functionally equivalent to calling
     * {@code listValue().size()}.
     *
     * @return the number of items in this array
     */
    public int size() {
        return listValue().size();
    }

    /**
     * Determines if this array is empty or not. This is functionally equivalent to calling {@code
     * listValue().isEmpty()}.
     *
     * @return true if this array is empty, false otherwise
     */
    public boolean isEmpty() {
        return listValue().isEmpty();
    }

    /**
     * Appends the given object to this {@link CborArray}. This is functionally equivalent to
     * calling {@code listValue().add(cborObject)}.
     *
     * @param cborObject the object to append
     */
    public void add(CborObject cborObject) {
        listValue().add(cborObject);
    }

    /**
     * Removes the given object from this {@link CborArray}. This is functionally equivalent to
     * calling {@code listValue().remove(o)}.
     *
     * @param cborObject the object to remove
     * @return true if the object was successfully removed, false otherwise
     */
    @CanIgnoreReturnValue
    public boolean remove(CborObject cborObject) {
        return listValue().remove(cborObject);
    }

    /**
     * Removes all items from this {@link CborArray}. This is functionally equivalent to calling
     * {@code listValue().clear()}.
     */
    public void clear() {
        listValue().clear();
    }

    /** Always returns {@link CborMajorType#ARRAY}. */
    @Override
    public final int getMajorType() {
        return CborMajorType.ARRAY;
    }

    @Override
    public final int getAdditionalInformation() {
        return CborInteger.calcAdditionalInformation(size());
    }

    @Override
    public boolean isValidJson() {
        for (CborObject item : this) {
            if (!item.isValidJson()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public final String toJsonString() {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (CborObject obj : this) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(obj.toJsonString());
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public List<Object> toJavaObject() {
        final ArrayList<Object> ret = new ArrayList<>();

        forEach((x) -> ret.add(x.toJavaObject()));

        return ret;
    }

    @Override
    public <T> T toJavaObject(Class<T> clazz) throws CborConversionException {
        if (clazz.isAssignableFrom(List.class)) {
            return clazz.cast(toJavaObject());
        }

        if (clazz.isAssignableFrom(float[].class)) {
            float[] array = new float[size()];
            Iterator<CborObject> iter = iterator();
            for (int i = 0; i < size(); i++) {
                Float v = iter.next().toJavaObject(Float.class);
                if (v == null) {
                    throw new CborConversionException("element is not assignable from null");
                }
                array[i] = v;
            }
            return clazz.cast(array);
        }

        if (clazz.isAssignableFrom(double[].class)) {
            double[] array = new double[size()];
            Iterator<CborObject> iter = iterator();
            for (int i = 0; i < size(); i++) {
                Double v = iter.next().toJavaObject(Double.class);
                if (v == null) {
                    throw new CborConversionException("element is not assignable from null");
                }
                array[i] = v;
            }
            return clazz.cast(array);
        }

        if (clazz.isAssignableFrom(int[].class)) {
            int[] array = new int[size()];
            Iterator<CborObject> iter = iterator();
            for (int i = 0; i < size(); i++) {
                Integer v = iter.next().toJavaObject(Integer.class);
                if (v == null) {
                    throw new CborConversionException("element is not assignable from null");
                }
                array[i] = v;
            }
            return clazz.cast(array);
        }

        if (clazz.isAssignableFrom(short[].class)) {
            short[] array = new short[size()];
            Iterator<CborObject> iter = iterator();
            for (int i = 0; i < size(); i++) {
                Short v = iter.next().toJavaObject(Short.class);
                if (v == null) {
                    throw new CborConversionException("element is not assignable from null");
                }
                array[i] = v;
            }
            return clazz.cast(array);
        }

        if (clazz.isAssignableFrom(long[].class)) {
            long[] array = new long[size()];
            Iterator<CborObject> iter = iterator();
            for (int i = 0; i < size(); i++) {
                Long v = iter.next().toJavaObject(Long.class);
                if (v == null) {
                    throw new CborConversionException("element is not assignable from null");
                }
                array[i] = v;
            }
            return clazz.cast(array);
        }

        if (clazz.isAssignableFrom(String[].class)) {
            String[] array = new String[size()];
            Iterator<CborObject> iter = iterator();
            for (int i = 0; i < size(); i++) {
                String v = iter.next().toJavaObject(String.class);
                if (v == null) {
                    throw new CborConversionException("element is not assignable from null");
                }
                array[i] = v;
            }
            return clazz.cast(array);
        }

        throw new CborConversionException(clazz + " is not assignable from array");
    }

    /** Creates an independent, deep copy of this object. */
    @Override
    public CborArray copy() {
        CborArray ret = create(getTag());
        for (CborObject obj : this) {
            ret.add(obj.copy());
        }
        return ret;
    }

    @NonNull
    @Override
    public Iterator<CborObject> iterator() {
        return listValue().iterator();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getTag()) * 1337 + listValue().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof CborArray)) {
            return false;
        }

        final CborArray rhs = (CborArray) obj;

        return rhs.getTag() == getTag() && listValue().equals(rhs.listValue());
    }

    @Override
    public final String toString(int indentLevel) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;

        if (indentLevel >= 0) {
            indentLevel++;
        }

        for (CborObject obj : this) {
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

            sb.append(obj.toString(indentLevel));
        }

        if (!isEmpty() && indentLevel > 0) {
            indentLevel--;
            sb.append("\n");
            for (int i = 0; i < indentLevel; i++) {
                sb.append("\t");
            }
        }

        sb.append("]");

        int tag = getTag();

        return tag == CborTag.UNTAGGED ? sb.toString() : tag + "(" + sb.toString() + ")";
    }
}
