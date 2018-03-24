package com.eclipsesource.v8.utils;

import com.eclipsesource.v8.Releasable;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8ArrayBuffer;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.V8TypedArray;
import com.eclipsesource.v8.V8Value;
import com.eclipsesource.v8.utils.typedarrays.ArrayBuffer;
import com.eclipsesource.v8.utils.typedarrays.Float32Array;
import com.eclipsesource.v8.utils.typedarrays.Float64Array;
import com.eclipsesource.v8.utils.typedarrays.Int16Array;
import com.eclipsesource.v8.utils.typedarrays.Int32Array;
import com.eclipsesource.v8.utils.typedarrays.Int8Array;
import com.eclipsesource.v8.utils.typedarrays.TypedArray;
import com.eclipsesource.v8.utils.typedarrays.UInt16Array;
import com.eclipsesource.v8.utils.typedarrays.UInt32Array;
import com.eclipsesource.v8.utils.typedarrays.UInt8Array;
import com.eclipsesource.v8.utils.typedarrays.UInt8ClampedArray;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class V8ObjectUtils {
    private static final TypeAdapter DEFAULT_TYPE_ADAPTER = new DefaultTypeAdapter();
    private static final Object IGNORE = new Object();

    static class ListWrapper {
        private List<? extends Object> list;

        public ListWrapper(List<? extends Object> list) {
            this.list = list;
        }

        public boolean equals(Object obj) {
            if ((obj instanceof ListWrapper) && ((ListWrapper) obj).list == this.list) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return System.identityHashCode(this.list);
        }
    }

    static class DefaultTypeAdapter implements TypeAdapter {
        DefaultTypeAdapter() {
        }

        public Object adapt(int type, Object value) {
            return TypeAdapter.DEFAULT;
        }
    }

    public static java.lang.Object getV8Result(com.eclipsesource.v8.V8 r6, java.lang.Object r7) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Incorrect nodes count for selectOther: B:19:0x0060 in [B:15:0x0056, B:19:0x0060, B:17:0x0003]
	at jadx.core.utils.BlockUtils.selectOther(BlockUtils.java:53)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:64)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:323)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:226)
*/
        /*
        if (r7 != 0) goto L_0x0004;
    L_0x0002:
        r1 = 0;
    L_0x0003:
        return r1;
    L_0x0004:
        r0 = new java.util.Hashtable;
        r0.<init>();
        r1 = getV8Result(r6, r7, r0);	 Catch:{ all -> 0x0047 }
        r3 = r1 instanceof com.eclipsesource.v8.V8Object;	 Catch:{ all -> 0x0047 }
        if (r3 == 0) goto L_0x002f;	 Catch:{ all -> 0x0047 }
    L_0x0011:
        r1 = (com.eclipsesource.v8.V8Object) r1;	 Catch:{ all -> 0x0047 }
        r1 = r1.twin();	 Catch:{ all -> 0x0047 }
        r3 = r0.values();
        r3 = r3.iterator();
    L_0x001f:
        r4 = r3.hasNext();
        if (r4 == 0) goto L_0x0003;
    L_0x0025:
        r2 = r3.next();
        r2 = (com.eclipsesource.v8.V8Value) r2;
        r2.release();
        goto L_0x001f;
    L_0x002f:
        r3 = r0.values();
        r3 = r3.iterator();
    L_0x0037:
        r4 = r3.hasNext();
        if (r4 == 0) goto L_0x0003;
    L_0x003d:
        r2 = r3.next();
        r2 = (com.eclipsesource.v8.V8Value) r2;
        r2.release();
        goto L_0x0037;
    L_0x0047:
        r3 = move-exception;
        r4 = r0.values();
        r4 = r4.iterator();
    L_0x0050:
        r5 = r4.hasNext();
        if (r5 == 0) goto L_0x0060;
    L_0x0056:
        r2 = r4.next();
        r2 = (com.eclipsesource.v8.V8Value) r2;
        r2.release();
        goto L_0x0050;
    L_0x0060:
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.eclipsesource.v8.utils.V8ObjectUtils.getV8Result(com.eclipsesource.v8.V8, java.lang.Object):java.lang.Object");
    }

    public static void pushValue(com.eclipsesource.v8.V8 r5, com.eclipsesource.v8.V8Array r6, java.lang.Object r7) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Incorrect nodes count for selectOther: B:11:0x0039 in [B:7:0x002f, B:11:0x0039, B:10:0x003a]
	at jadx.core.utils.BlockUtils.selectOther(BlockUtils.java:53)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:64)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:323)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:226)
*/
        /*
        r0 = new java.util.Hashtable;
        r0.<init>();
        pushValue(r5, r6, r7, r0);	 Catch:{ all -> 0x0020 }
        r2 = r0.values();
        r2 = r2.iterator();
    L_0x0010:
        r3 = r2.hasNext();
        if (r3 == 0) goto L_0x003a;
    L_0x0016:
        r1 = r2.next();
        r1 = (com.eclipsesource.v8.V8Value) r1;
        r1.release();
        goto L_0x0010;
    L_0x0020:
        r2 = move-exception;
        r3 = r0.values();
        r3 = r3.iterator();
    L_0x0029:
        r4 = r3.hasNext();
        if (r4 == 0) goto L_0x0039;
    L_0x002f:
        r1 = r3.next();
        r1 = (com.eclipsesource.v8.V8Value) r1;
        r1.release();
        goto L_0x0029;
    L_0x0039:
        throw r2;
    L_0x003a:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.eclipsesource.v8.utils.V8ObjectUtils.pushValue(com.eclipsesource.v8.V8, com.eclipsesource.v8.V8Array, java.lang.Object):void");
    }

    public static com.eclipsesource.v8.V8Array toV8Array(com.eclipsesource.v8.V8 r5, java.util.List<? extends java.lang.Object> r6) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Incorrect nodes count for selectOther: B:11:0x003e in [B:7:0x0034, B:11:0x003e, B:10:0x003f]
	at jadx.core.utils.BlockUtils.selectOther(BlockUtils.java:53)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:64)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:323)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:226)
*/
        /*
        r0 = new java.util.Hashtable;
        r0.<init>();
        r2 = toV8Array(r5, r6, r0);	 Catch:{ all -> 0x0025 }
        r2 = r2.twin();	 Catch:{ all -> 0x0025 }
        r3 = r0.values();
        r3 = r3.iterator();
    L_0x0015:
        r4 = r3.hasNext();
        if (r4 == 0) goto L_0x003f;
    L_0x001b:
        r1 = r3.next();
        r1 = (com.eclipsesource.v8.V8Value) r1;
        r1.release();
        goto L_0x0015;
    L_0x0025:
        r2 = move-exception;
        r3 = r0.values();
        r3 = r3.iterator();
    L_0x002e:
        r4 = r3.hasNext();
        if (r4 == 0) goto L_0x003e;
    L_0x0034:
        r1 = r3.next();
        r1 = (com.eclipsesource.v8.V8Value) r1;
        r1.release();
        goto L_0x002e;
    L_0x003e:
        throw r2;
    L_0x003f:
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.eclipsesource.v8.utils.V8ObjectUtils.toV8Array(com.eclipsesource.v8.V8, java.util.List):com.eclipsesource.v8.V8Array");
    }

    public static com.eclipsesource.v8.V8Object toV8Object(com.eclipsesource.v8.V8 r5, java.util.Map<java.lang.String, ? extends java.lang.Object> r6) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Incorrect nodes count for selectOther: B:11:0x003e in [B:7:0x0034, B:11:0x003e, B:10:0x003f]
	at jadx.core.utils.BlockUtils.selectOther(BlockUtils.java:53)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:64)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:323)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:226)
*/
        /*
        r0 = new java.util.Hashtable;
        r0.<init>();
        r2 = toV8Object(r5, r6, r0);	 Catch:{ all -> 0x0025 }
        r2 = r2.twin();	 Catch:{ all -> 0x0025 }
        r3 = r0.values();
        r3 = r3.iterator();
    L_0x0015:
        r4 = r3.hasNext();
        if (r4 == 0) goto L_0x003f;
    L_0x001b:
        r1 = r3.next();
        r1 = (com.eclipsesource.v8.V8Value) r1;
        r1.release();
        goto L_0x0015;
    L_0x0025:
        r2 = move-exception;
        r3 = r0.values();
        r3 = r3.iterator();
    L_0x002e:
        r4 = r3.hasNext();
        if (r4 == 0) goto L_0x003e;
    L_0x0034:
        r1 = r3.next();
        r1 = (com.eclipsesource.v8.V8Value) r1;
        r1.release();
        goto L_0x002e;
    L_0x003e:
        throw r2;
    L_0x003f:
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.eclipsesource.v8.utils.V8ObjectUtils.toV8Object(com.eclipsesource.v8.V8, java.util.Map):com.eclipsesource.v8.V8Object");
    }

    public static Object getValue(Object v8Object) {
        return getValue(v8Object, DEFAULT_TYPE_ADAPTER);
    }

    public static Object getValue(Object v8Object, TypeAdapter adapter) {
        V8Map<Object> cache = new V8Map();
        try {
            if (v8Object instanceof V8Value) {
                v8Object = getValue(v8Object, ((V8Value) v8Object).getV8Type(), cache, adapter);
            } else {
                cache.release();
            }
            return v8Object;
        } finally {
            cache.release();
        }
    }

    public static Map<String, ? super Object> toMap(V8Object object) {
        return toMap(object, DEFAULT_TYPE_ADAPTER);
    }

    public static Map<String, ? super Object> toMap(V8Object object, TypeAdapter adapter) {
        V8Map<Object> cache = new V8Map();
        try {
            Map<String, ? super Object> toMap = toMap(object, cache, adapter);
            return toMap;
        } finally {
            cache.release();
        }
    }

    public static List<? super Object> toList(V8Array array) {
        return toList(array, DEFAULT_TYPE_ADAPTER);
    }

    public static List<? super Object> toList(V8Array array, TypeAdapter adapter) {
        V8Map<Object> cache = new V8Map();
        try {
            List<? super Object> toList = toList(array, cache, adapter);
            return toList;
        } finally {
            cache.release();
        }
    }

    public static Object getTypedArray(V8Array array, int arrayType, Object result) {
        int length = array.length();
        if (arrayType == 1) {
            int[] intArray = (int[]) result;
            if (intArray == null || intArray.length < length) {
                intArray = new int[length];
            }
            array.getIntegers(0, length, intArray);
            return intArray;
        } else if (arrayType == 2) {
            Object doubleArray = (double[]) result;
            if (doubleArray == null || doubleArray.length < length) {
                doubleArray = new double[length];
            }
            array.getDoubles(0, length, doubleArray);
            return doubleArray;
        } else if (arrayType == 3) {
            Object booleanArray = (boolean[]) result;
            if (booleanArray == null || booleanArray.length < length) {
                booleanArray = new boolean[length];
            }
            array.getBooleans(0, length, booleanArray);
            return booleanArray;
        } else if (arrayType == 4) {
            String[] stringArray = (String[]) result;
            if (stringArray == null || stringArray.length < length) {
                stringArray = new String[length];
            }
            array.getStrings(0, length, stringArray);
            return stringArray;
        } else if (arrayType == 9) {
            Object byteArray = (byte[]) result;
            if (byteArray == null || byteArray.length < length) {
                byteArray = new byte[length];
            }
            array.getBytes(0, length, byteArray);
            return byteArray;
        } else {
            throw new RuntimeException("Unsupported bulk load type: " + arrayType);
        }
    }

    public static Object getTypedArray(V8Array array, int arrayType) {
        int length = array.length();
        if (arrayType == 1) {
            return array.getIntegers(0, length);
        }
        if (arrayType == 2) {
            return array.getDoubles(0, length);
        }
        if (arrayType == 3) {
            return array.getBooleans(0, length);
        }
        if (arrayType == 4) {
            return array.getStrings(0, length);
        }
        throw new RuntimeException("Unsupported bulk load type: " + arrayType);
    }

    public static Object getValue(V8Array array, int index) {
        V8Map<Object> cache = new V8Map();
        Object obj = null;
        try {
            obj = array.get(index);
            Object value = getValue(obj, array.getType(index), cache, DEFAULT_TYPE_ADAPTER);
            return value;
        } finally {
            if (obj instanceof Releasable) {
                ((Releasable) obj).release();
            }
            cache.release();
        }
    }

    public static Object getValue(V8Array array, int index, TypeAdapter adapter) {
        V8Map<Object> cache = new V8Map();
        Object obj = null;
        try {
            obj = array.get(index);
            Object value = getValue(obj, array.getType(index), cache, adapter);
            return value;
        } finally {
            if (obj instanceof Releasable) {
                ((Releasable) obj).release();
            }
            cache.release();
        }
    }

    public static Object getValue(V8Object object, String key) {
        return getValue(object, key, DEFAULT_TYPE_ADAPTER);
    }

    public static Object getValue(V8Object v8Object, String key, TypeAdapter adapter) {
        V8Map<Object> cache = new V8Map();
        Object obj = null;
        try {
            obj = v8Object.get(key);
            Object value = getValue(obj, v8Object.getType(key), cache, adapter);
            return value;
        } finally {
            if (obj instanceof Releasable) {
                ((Releasable) obj).release();
            }
            cache.release();
        }
    }

    private static Map<String, ? super Object> toMap(V8Object v8Object, V8Map<Object> cache, TypeAdapter adapter) {
        if (v8Object == null) {
            return Collections.emptyMap();
        }
        if (cache.containsKey(v8Object)) {
            return (Map) cache.get(v8Object);
        }
        Map<String, ? super Object> result = new V8PropertyMap();
        cache.put((V8Value) v8Object, (Object) result);
        String[] keys = v8Object.getKeys();
        int length = keys.length;
        int i = 0;
        while (i < length) {
            String key = keys[i];
            try {
                Object object = v8Object.get(key);
                Object value = getValue(object, v8Object.getType(key), cache, adapter);
                if (value != IGNORE) {
                    result.put(key, value);
                }
                if (object instanceof Releasable) {
                    ((Releasable) object).release();
                }
                i++;
            } catch (Throwable th) {
                if (null instanceof Releasable) {
                    ((Releasable) null).release();
                }
            }
        }
        return result;
    }

    private static List<? super Object> toList(V8Array array, V8Map<Object> cache, TypeAdapter adapter) {
        if (array == null) {
            return Collections.emptyList();
        }
        if (cache.containsKey(array)) {
            return (List) cache.get(array);
        }
        List<? super Object> result = new ArrayList();
        cache.put((V8Value) array, (Object) result);
        int i = 0;
        while (i < array.length()) {
            try {
                Object object = array.get(i);
                Object value = getValue(object, array.getType(i), cache, adapter);
                if (value != IGNORE) {
                    result.add(value);
                }
                if (object instanceof Releasable) {
                    ((Releasable) object).release();
                }
                i++;
            } catch (Throwable th) {
                if (null instanceof Releasable) {
                    ((Releasable) null).release();
                }
            }
        }
        return result;
    }

    private static V8Object toV8Object(V8 v8, Map<String, ? extends Object> map, Map<Object, V8Value> cache) {
        if (cache.containsKey(map)) {
            return (V8Object) cache.get(map);
        }
        V8Object result = new V8Object(v8);
        cache.put(map, result);
        try {
            for (Entry<String, ? extends Object> entry : map.entrySet()) {
                setValue(v8, result, (String) entry.getKey(), entry.getValue(), cache);
            }
            return result;
        } catch (IllegalStateException e) {
            result.release();
            throw e;
        }
    }

    private static V8Array toV8Array(V8 v8, List<? extends Object> list, Map<Object, V8Value> cache) {
        if (cache.containsKey(new ListWrapper(list))) {
            return (V8Array) cache.get(new ListWrapper(list));
        }
        V8Array result = new V8Array(v8);
        cache.put(new ListWrapper(list), result);
        int i = 0;
        while (i < list.size()) {
            try {
                pushValue(v8, result, list.get(i), cache);
                i++;
            } catch (IllegalStateException e) {
                result.release();
                throw e;
            }
        }
        return result;
    }

    private static V8ArrayBuffer toV8ArrayBuffer(V8 v8, ArrayBuffer arrayBuffer, Map<Object, V8Value> cache) {
        if (cache.containsKey(arrayBuffer)) {
            return (V8ArrayBuffer) cache.get(arrayBuffer);
        }
        V8ArrayBuffer result = new V8ArrayBuffer(v8, arrayBuffer.getByteBuffer());
        cache.put(arrayBuffer, result);
        return result;
    }

    private static V8TypedArray toV8TypedArray(V8 v8, TypedArray typedArray, Map<Object, V8Value> cache) {
        if (cache.containsKey(typedArray)) {
            return (V8TypedArray) cache.get(typedArray);
        }
        V8ArrayBuffer arrayBuffer = new V8ArrayBuffer(v8, typedArray.getByteBuffer());
        try {
            V8TypedArray result = new V8TypedArray(v8, arrayBuffer, typedArray.getType(), 0, typedArray.length());
            cache.put(typedArray, result);
            return result;
        } finally {
            arrayBuffer.release();
        }
    }

    private static Object getV8Result(V8 v8, Object value, Map<Object, V8Value> cache) {
        if (cache.containsKey(value)) {
            return cache.get(value);
        }
        if (value instanceof Map) {
            return toV8Object(v8, (Map) value, cache);
        }
        if (value instanceof List) {
            return toV8Array(v8, (List) value, cache);
        }
        if (value instanceof TypedArray) {
            return toV8TypedArray(v8, (TypedArray) value, cache);
        }
        if (value instanceof ArrayBuffer) {
            return toV8ArrayBuffer(v8, (ArrayBuffer) value, cache);
        }
        return value;
    }

    private static void pushValue(V8 v8, V8Array result, Object value, Map<Object, V8Value> cache) {
        if (value == null) {
            result.pushUndefined();
        } else if (value instanceof Integer) {
            result.push(value);
        } else if (value instanceof Long) {
            result.push(new Double((double) ((Long) value).longValue()));
        } else if (value instanceof Double) {
            result.push(value);
        } else if (value instanceof Float) {
            result.push(value);
        } else if (value instanceof String) {
            result.push((String) value);
        } else if (value instanceof Boolean) {
            result.push(value);
        } else if (value instanceof V8Object) {
            result.push((V8Object) value);
        } else if (value instanceof TypedArray) {
            result.push(toV8TypedArray(v8, (TypedArray) value, cache));
        } else if (value instanceof ArrayBuffer) {
            result.push(toV8ArrayBuffer(v8, (ArrayBuffer) value, cache));
        } else if (value instanceof Map) {
            result.push(toV8Object(v8, (Map) value, cache));
        } else if (value instanceof List) {
            result.push(toV8Array(v8, (List) value, cache));
        } else {
            throw new IllegalStateException("Unsupported Object of type: " + value.getClass());
        }
    }

    private static void setValue(V8 v8, V8Object result, String key, Object value, Map<Object, V8Value> cache) {
        if (value == null) {
            result.addUndefined(key);
        } else if (value instanceof Integer) {
            result.add(key, ((Integer) value).intValue());
        } else if (value instanceof Long) {
            result.add(key, (double) ((Long) value).longValue());
        } else if (value instanceof Double) {
            result.add(key, ((Double) value).doubleValue());
        } else if (value instanceof Float) {
            result.add(key, (double) ((Float) value).floatValue());
        } else if (value instanceof String) {
            result.add(key, (String) value);
        } else if (value instanceof Boolean) {
            result.add(key, ((Boolean) value).booleanValue());
        } else if (value instanceof V8Object) {
            result.add(key, (V8Object) value);
        } else if (value instanceof TypedArray) {
            result.add(key, toV8TypedArray(v8, (TypedArray) value, cache));
        } else if (value instanceof ArrayBuffer) {
            result.add(key, toV8ArrayBuffer(v8, (ArrayBuffer) value, cache));
        } else if (value instanceof Map) {
            result.add(key, toV8Object(v8, (Map) value, cache));
        } else if (value instanceof List) {
            result.add(key, toV8Array(v8, (List) value, cache));
        } else {
            throw new IllegalStateException("Unsupported Object of type: " + value.getClass());
        }
    }

    private static Object getValue(Object value, int valueType, V8Map<Object> cache, TypeAdapter adapter) {
        Object adapterResult = adapter.adapt(valueType, value);
        if (TypeAdapter.DEFAULT != adapterResult) {
            return adapterResult;
        }
        switch (valueType) {
            case 0:
                return null;
            case 1:
            case 2:
            case 3:
            case 4:
                return value;
            case 5:
                return toList((V8Array) value, cache, adapter);
            case 6:
                return toMap((V8Object) value, cache, adapter);
            case 7:
                return IGNORE;
            case 8:
                return toTypedArray((V8Array) value);
            case 10:
                return new ArrayBuffer(((V8ArrayBuffer) value).getBackingStore());
            case 99:
                return V8.getUndefined();
            default:
                throw new IllegalStateException("Cannot convert type " + V8Value.getStringRepresentation(valueType));
        }
    }

    private static Object toTypedArray(V8Array typedArray) {
        int arrayType = typedArray.getType();
        ByteBuffer buffer = ((V8TypedArray) typedArray).getByteBuffer();
        switch (arrayType) {
            case 1:
                return new Int32Array(buffer);
            case 2:
                return new Float64Array(buffer);
            case 9:
                return new Int8Array(buffer);
            case 11:
                return new UInt8Array(buffer);
            case 12:
                return new UInt8ClampedArray(buffer);
            case 13:
                return new Int16Array(buffer);
            case 14:
                return new UInt16Array(buffer);
            case 15:
                return new UInt32Array(buffer);
            case 16:
                return new Float32Array(buffer);
            default:
                throw new IllegalStateException("Known Typed Array type: " + V8Value.getStringRepresentation(arrayType));
        }
    }

    private V8ObjectUtils() {
    }
}
