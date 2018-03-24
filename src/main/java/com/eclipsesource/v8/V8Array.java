package com.eclipsesource.v8;

public class V8Array extends V8Object {

    static class Undefined extends V8Array {
        public boolean isUndefined() {
            return true;
        }

        public boolean isReleased() {
            return false;
        }

        public void release() {
        }

        public Undefined twin() {
            return (Undefined) super.twin();
        }

        public String toString() {
            return "undefined";
        }

        public boolean equals(Object that) {
            if ((that instanceof V8Object) && ((V8Object) that).isUndefined()) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return 919;
        }

        public V8 getRuntime() {
            throw new UnsupportedOperationException();
        }

        public V8Object add(String key, boolean value) {
            throw new UnsupportedOperationException();
        }

        public V8Object add(String key, double value) {
            throw new UnsupportedOperationException();
        }

        public V8Object add(String key, int value) {
            throw new UnsupportedOperationException();
        }

        public V8Object add(String key, String value) {
            throw new UnsupportedOperationException();
        }

        public V8Object add(String key, V8Value value) {
            throw new UnsupportedOperationException();
        }

        public V8Object addUndefined(String key) {
            throw new UnsupportedOperationException();
        }

        public boolean contains(String key) {
            throw new UnsupportedOperationException();
        }

        public V8Array executeArrayFunction(String name, V8Array parameters) {
            throw new UnsupportedOperationException();
        }

        public boolean executeBooleanFunction(String name, V8Array parameters) {
            throw new UnsupportedOperationException();
        }

        public double executeDoubleFunction(String name, V8Array parameters) {
            throw new UnsupportedOperationException();
        }

        public int executeIntegerFunction(String name, V8Array parameters) {
            throw new UnsupportedOperationException();
        }

        public V8Object executeObjectFunction(String name, V8Array parameters) {
            throw new UnsupportedOperationException();
        }

        public String executeStringFunction(String name, V8Array parameters) {
            throw new UnsupportedOperationException();
        }

        public void executeVoidFunction(String name, V8Array parameters) {
            throw new UnsupportedOperationException();
        }

        public V8Array getArray(String key) {
            throw new UnsupportedOperationException();
        }

        public boolean getBoolean(String key) {
            throw new UnsupportedOperationException();
        }

        public double getDouble(String key) {
            throw new UnsupportedOperationException();
        }

        public int getInteger(String key) {
            throw new UnsupportedOperationException();
        }

        public String[] getKeys() {
            throw new UnsupportedOperationException();
        }

        public V8Object getObject(String key) throws V8ResultUndefined {
            throw new UnsupportedOperationException();
        }

        public String getString(String key) throws V8ResultUndefined {
            throw new UnsupportedOperationException();
        }

        public int getType(String key) throws V8ResultUndefined {
            throw new UnsupportedOperationException();
        }

        public V8Object registerJavaMethod(JavaCallback callback, String jsFunctionName) {
            throw new UnsupportedOperationException();
        }

        public V8Object registerJavaMethod(JavaVoidCallback callback, String jsFunctionName) {
            throw new UnsupportedOperationException();
        }

        public V8Object registerJavaMethod(Object object, String methodName, String jsFunctionName, Class<?>[] clsArr, boolean includeReceiver) {
            throw new UnsupportedOperationException();
        }

        public V8Object setPrototype(V8Object value) {
            throw new UnsupportedOperationException();
        }

        public Object get(int index) {
            throw new UnsupportedOperationException();
        }

        public V8Array getArray(int index) {
            throw new UnsupportedOperationException();
        }

        public boolean getBoolean(int index) {
            throw new UnsupportedOperationException();
        }

        public boolean[] getBooleans(int index, int length) {
            throw new UnsupportedOperationException();
        }

        public byte[] getBytes(int index, int length) {
            throw new UnsupportedOperationException();
        }

        public int getBytes(int index, int length, byte[] resultArray) {
            throw new UnsupportedOperationException();
        }

        public byte getByte(int index) {
            throw new UnsupportedOperationException();
        }

        public int getBooleans(int index, int length, boolean[] resultArray) {
            throw new UnsupportedOperationException();
        }

        public double getDouble(int index) {
            throw new UnsupportedOperationException();
        }

        public double[] getDoubles(int index, int length) {
            throw new UnsupportedOperationException();
        }

        public int getDoubles(int index, int length, double[] resultArray) {
            throw new UnsupportedOperationException();
        }

        public int getInteger(int index) {
            throw new UnsupportedOperationException();
        }

        public int[] getIntegers(int index, int length) {
            throw new UnsupportedOperationException();
        }

        public int getIntegers(int index, int length, int[] resultArray) {
            throw new UnsupportedOperationException();
        }

        public V8Object getObject(int index) {
            throw new UnsupportedOperationException();
        }

        public String getString(int index) {
            throw new UnsupportedOperationException();
        }

        public String[] getStrings(int index, int length) {
            throw new UnsupportedOperationException();
        }

        public int getStrings(int index, int length, String[] resultArray) {
            throw new UnsupportedOperationException();
        }

        public int getType() {
            throw new UnsupportedOperationException();
        }

        public int getType(int index) {
            throw new UnsupportedOperationException();
        }

        public int getType(int index, int length) {
            throw new UnsupportedOperationException();
        }

        public int length() {
            throw new UnsupportedOperationException();
        }

        public V8Array push(boolean value) {
            throw new UnsupportedOperationException();
        }

        public V8Array push(double value) {
            throw new UnsupportedOperationException();
        }

        public V8Array push(int value) {
            throw new UnsupportedOperationException();
        }

        public V8Array push(String value) {
            throw new UnsupportedOperationException();
        }

        public V8Array push(V8Value value) {
            throw new UnsupportedOperationException();
        }

        public V8Array pushUndefined() {
            throw new UnsupportedOperationException();
        }
    }

    protected V8Array() {
    }

    public V8Array(V8 v8) {
        super(v8);
        v8.checkThread();
    }

    protected V8Array(V8 v8, Object data) {
        super(v8, data);
    }

    protected V8Value createTwin() {
        return new V8Array(this.v8);
    }

    public V8Array twin() {
        return (V8Array) super.twin();
    }

    public String toString() {
        if (this.released || this.v8.isReleased()) {
            return "[Array released]";
        }
        return super.toString();
    }

    protected void initialize(long runtimePtr, Object data) {
        long handle = this.v8.initNewV8Array(runtimePtr);
        this.released = false;
        addObjectReference(handle);
    }

    public int length() {
        this.v8.checkThread();
        checkReleased();
        return this.v8.arrayGetSize(this.v8.getV8RuntimePtr(), getHandle());
    }

    public int getType(int index) {
        this.v8.checkThread();
        checkReleased();
        return this.v8.getType(this.v8.getV8RuntimePtr(), getHandle(), index);
    }

    public int getType() {
        this.v8.checkThread();
        checkReleased();
        return this.v8.getArrayType(this.v8.getV8RuntimePtr(), getHandle());
    }

    public int getType(int index, int length) {
        this.v8.checkThread();
        checkReleased();
        return this.v8.getType(this.v8.getV8RuntimePtr(), getHandle(), index, length);
    }

    public int getInteger(int index) {
        this.v8.checkThread();
        checkReleased();
        return this.v8.arrayGetInteger(this.v8.getV8RuntimePtr(), getHandle(), index);
    }

    public boolean getBoolean(int index) {
        this.v8.checkThread();
        checkReleased();
        return this.v8.arrayGetBoolean(this.v8.getV8RuntimePtr(), getHandle(), index);
    }

    public byte getByte(int index) {
        this.v8.checkThread();
        checkReleased();
        return this.v8.arrayGetByte(this.v8.getV8RuntimePtr(), getHandle(), index);
    }

    public double getDouble(int index) {
        this.v8.checkThread();
        checkReleased();
        return this.v8.arrayGetDouble(this.v8.getV8RuntimePtr(), getHandle(), index);
    }

    public String getString(int index) {
        this.v8.checkThread();
        checkReleased();
        return this.v8.arrayGetString(this.v8.getV8RuntimePtr(), getHandle(), index);
    }

    public int[] getIntegers(int index, int length) {
        this.v8.checkThread();
        checkReleased();
        return this.v8.arrayGetIntegers(this.v8.getV8RuntimePtr(), getHandle(), index, length);
    }

    public int getIntegers(int index, int length, int[] resultArray) {
        this.v8.checkThread();
        checkReleased();
        if (length <= resultArray.length) {
            return this.v8.arrayGetIntegers(this.v8.getV8RuntimePtr(), getHandle(), index, length, resultArray);
        }
        throw new IndexOutOfBoundsException();
    }

    public double[] getDoubles(int index, int length) {
        this.v8.checkThread();
        checkReleased();
        return this.v8.arrayGetDoubles(this.v8.getV8RuntimePtr(), getHandle(), index, length);
    }

    public int getDoubles(int index, int length, double[] resultArray) {
        this.v8.checkThread();
        checkReleased();
        if (length <= resultArray.length) {
            return this.v8.arrayGetDoubles(this.v8.getV8RuntimePtr(), getHandle(), index, length, resultArray);
        }
        throw new IndexOutOfBoundsException();
    }

    public boolean[] getBooleans(int index, int length) {
        this.v8.checkThread();
        checkReleased();
        return this.v8.arrayGetBooleans(this.v8.getV8RuntimePtr(), getHandle(), index, length);
    }

    public byte[] getBytes(int index, int length) {
        this.v8.checkThread();
        checkReleased();
        return this.v8.arrayGetBytes(this.v8.getV8RuntimePtr(), getHandle(), index, length);
    }

    public int getBooleans(int index, int length, boolean[] resultArray) {
        this.v8.checkThread();
        checkReleased();
        if (length <= resultArray.length) {
            return this.v8.arrayGetBooleans(this.v8.getV8RuntimePtr(), getHandle(), index, length, resultArray);
        }
        throw new IndexOutOfBoundsException();
    }

    public int getBytes(int index, int length, byte[] resultArray) {
        this.v8.checkThread();
        checkReleased();
        if (length <= resultArray.length) {
            return this.v8.arrayGetBytes(this.v8.getV8RuntimePtr(), getHandle(), index, length, resultArray);
        }
        throw new IndexOutOfBoundsException();
    }

    public String[] getStrings(int index, int length) {
        this.v8.checkThread();
        checkReleased();
        return this.v8.arrayGetStrings(this.v8.getV8RuntimePtr(), getHandle(), index, length);
    }

    public int getStrings(int index, int length, String[] resultArray) {
        this.v8.checkThread();
        checkReleased();
        if (length <= resultArray.length) {
            return this.v8.arrayGetStrings(this.v8.getV8RuntimePtr(), getHandle(), index, length, resultArray);
        }
        throw new IndexOutOfBoundsException();
    }

    public Object get(int index) {
        this.v8.checkThread();
        checkReleased();
        return this.v8.arrayGet(this.v8.getV8RuntimePtr(), 6, this.objectHandle, index);
    }

    public V8Array getArray(int index) {
        this.v8.checkThread();
        checkReleased();
        Object result = this.v8.arrayGet(this.v8.getV8RuntimePtr(), 5, this.objectHandle, index);
        if (result == null || (result instanceof V8Array)) {
            return (V8Array) result;
        }
        throw new V8ResultUndefined();
    }

    public V8Object getObject(int index) {
        this.v8.checkThread();
        checkReleased();
        Object result = this.v8.arrayGet(this.v8.getV8RuntimePtr(), 6, this.objectHandle, index);
        if (result == null || (result instanceof V8Object)) {
            return (V8Object) result;
        }
        throw new V8ResultUndefined();
    }

    public V8Array push(int value) {
        this.v8.checkThread();
        checkReleased();
        this.v8.addArrayIntItem(this.v8.getV8RuntimePtr(), getHandle(), value);
        return this;
    }

    public V8Array push(boolean value) {
        this.v8.checkThread();
        checkReleased();
        this.v8.addArrayBooleanItem(this.v8.getV8RuntimePtr(), getHandle(), value);
        return this;
    }

    public V8Array push(double value) {
        this.v8.checkThread();
        checkReleased();
        this.v8.addArrayDoubleItem(this.v8.getV8RuntimePtr(), getHandle(), value);
        return this;
    }

    public V8Array push(String value) {
        this.v8.checkThread();
        checkReleased();
        if (value == null) {
            this.v8.addArrayNullItem(this.v8.getV8RuntimePtr(), getHandle());
        } else if (value.equals(V8.getUndefined())) {
            this.v8.addArrayUndefinedItem(this.v8.getV8RuntimePtr(), getHandle());
        } else {
            this.v8.addArrayStringItem(this.v8.getV8RuntimePtr(), getHandle(), value);
        }
        return this;
    }

    public V8Array push(V8Value value) {
        this.v8.checkThread();
        checkReleased();
        this.v8.checkRuntime(value);
        if (value == null) {
            this.v8.addArrayNullItem(this.v8.getV8RuntimePtr(), getHandle());
        } else if (value.equals(V8.getUndefined())) {
            this.v8.addArrayUndefinedItem(this.v8.getV8RuntimePtr(), getHandle());
        } else {
            this.v8.addArrayObjectItem(this.v8.getV8RuntimePtr(), getHandle(), value.getHandle());
        }
        return this;
    }

    public V8Array push(Object value) {
        this.v8.checkThread();
        checkReleased();
        if (value instanceof V8Value) {
            this.v8.checkRuntime((V8Value) value);
        }
        if (value == null) {
            this.v8.addArrayNullItem(this.v8.getV8RuntimePtr(), getHandle());
        } else if (value.equals(V8.getUndefined())) {
            this.v8.addArrayUndefinedItem(this.v8.getV8RuntimePtr(), getHandle());
        } else if (value instanceof Double) {
            this.v8.addArrayDoubleItem(this.v8.getV8RuntimePtr(), getHandle(), ((Double) value).doubleValue());
        } else if (value instanceof Integer) {
            this.v8.addArrayIntItem(this.v8.getV8RuntimePtr(), getHandle(), ((Integer) value).intValue());
        } else if (value instanceof Float) {
            this.v8.addArrayDoubleItem(this.v8.getV8RuntimePtr(), getHandle(), ((Float) value).doubleValue());
        } else if (value instanceof Number) {
            this.v8.addArrayDoubleItem(this.v8.getV8RuntimePtr(), getHandle(), ((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            this.v8.addArrayBooleanItem(this.v8.getV8RuntimePtr(), getHandle(), ((Boolean) value).booleanValue());
        } else if (value instanceof String) {
            this.v8.addArrayStringItem(this.v8.getV8RuntimePtr(), getHandle(), (String) value);
        } else if (value instanceof V8Value) {
            this.v8.addArrayObjectItem(this.v8.getV8RuntimePtr(), getHandle(), ((V8Value) value).getHandle());
        } else {
            throw new IllegalArgumentException();
        }
        return this;
    }

    public V8Array pushNull() {
        this.v8.checkThread();
        checkReleased();
        this.v8.addArrayNullItem(this.v8.getV8RuntimePtr(), getHandle());
        return this;
    }

    public V8Array pushUndefined() {
        this.v8.checkThread();
        checkReleased();
        this.v8.addArrayUndefinedItem(this.v8.getV8RuntimePtr(), getHandle());
        return this;
    }
}
