package com.eclipsesource.v8;

import java.lang.reflect.Method;

public class V8Object extends V8Value {

    static class Undefined extends V8Object {
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

        public V8Object add(String key, boolean value) {
            throw new UnsupportedOperationException();
        }

        public V8 getRuntime() {
            throw new UnsupportedOperationException();
        }

        public V8Object add(String key, double value) {
            throw new UnsupportedOperationException();
        }

        public V8Object add(String key, int value) {
            throw new UnsupportedOperationException();
        }

        public Object executeJSFunction(String name, Object... parameters) {
            throw new UnsupportedOperationException();
        }

        public Object executeFunction(String name, V8Array parameters) {
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
    }

    public V8Object(V8 v8) {
        this(v8, null);
    }

    protected V8Object(V8 v8, Object data) {
        super(v8);
        if (v8 != null) {
            this.v8.checkThread();
            initialize(this.v8.getV8RuntimePtr(), data);
        }
    }

    protected V8Object() {
    }

    protected V8Value createTwin() {
        return new V8Object(this.v8);
    }

    public V8Object twin() {
        return (V8Object) super.twin();
    }

    public boolean contains(String key) {
        this.v8.checkThread();
        checkReleased();
        checkKey(key);
        return this.v8.contains(this.v8.getV8RuntimePtr(), this.objectHandle, key);
    }

    public String[] getKeys() {
        this.v8.checkThread();
        checkReleased();
        return this.v8.getKeys(this.v8.getV8RuntimePtr(), this.objectHandle);
    }

    public int getType(String key) {
        this.v8.checkThread();
        checkReleased();
        checkKey(key);
        return this.v8.getType(this.v8.getV8RuntimePtr(), this.objectHandle, key);
    }

    public Object get(String key) {
        this.v8.checkThread();
        checkReleased();
        checkKey(key);
        return this.v8.get(this.v8.getV8RuntimePtr(), 6, this.objectHandle, key);
    }

    public int getInteger(String key) {
        this.v8.checkThread();
        checkReleased();
        checkKey(key);
        return this.v8.getInteger(this.v8.getV8RuntimePtr(), this.objectHandle, key);
    }

    public boolean getBoolean(String key) {
        this.v8.checkThread();
        checkReleased();
        checkKey(key);
        return this.v8.getBoolean(this.v8.getV8RuntimePtr(), this.objectHandle, key);
    }

    public double getDouble(String key) {
        this.v8.checkThread();
        checkReleased();
        checkKey(key);
        return this.v8.getDouble(this.v8.getV8RuntimePtr(), this.objectHandle, key);
    }

    public String getString(String key) {
        this.v8.checkThread();
        checkReleased();
        checkKey(key);
        return this.v8.getString(this.v8.getV8RuntimePtr(), this.objectHandle, key);
    }

    public V8Array getArray(String key) {
        this.v8.checkThread();
        checkReleased();
        checkKey(key);
        Object result = this.v8.get(this.v8.getV8RuntimePtr(), 5, this.objectHandle, key);
        if (result == null || (result instanceof V8Array)) {
            return (V8Array) result;
        }
        throw new V8ResultUndefined();
    }

    public V8Object getObject(String key) {
        this.v8.checkThread();
        checkReleased();
        checkKey(key);
        Object result = this.v8.get(this.v8.getV8RuntimePtr(), 6, this.objectHandle, key);
        if (result == null || (result instanceof V8Object)) {
            return (V8Object) result;
        }
        throw new V8ResultUndefined();
    }

    public int executeIntegerFunction(String name, V8Array parameters) {
        this.v8.checkThread();
        checkReleased();
        this.v8.checkRuntime(parameters);
        return this.v8.executeIntegerFunction(this.v8.getV8RuntimePtr(), getHandle(), name, parameters == null ? 0 : parameters.getHandle());
    }

    public double executeDoubleFunction(String name, V8Array parameters) {
        this.v8.checkThread();
        checkReleased();
        this.v8.checkRuntime(parameters);
        return this.v8.executeDoubleFunction(this.v8.getV8RuntimePtr(), getHandle(), name, parameters == null ? 0 : parameters.getHandle());
    }

    public String executeStringFunction(String name, V8Array parameters) {
        this.v8.checkThread();
        checkReleased();
        this.v8.checkRuntime(parameters);
        return this.v8.executeStringFunction(this.v8.getV8RuntimePtr(), getHandle(), name, parameters == null ? 0 : parameters.getHandle());
    }

    public boolean executeBooleanFunction(String name, V8Array parameters) {
        this.v8.checkThread();
        checkReleased();
        this.v8.checkRuntime(parameters);
        return this.v8.executeBooleanFunction(this.v8.getV8RuntimePtr(), getHandle(), name, parameters == null ? 0 : parameters.getHandle());
    }

    public V8Array executeArrayFunction(String name, V8Array parameters) {
        this.v8.checkThread();
        checkReleased();
        this.v8.checkRuntime(parameters);
        Object result = this.v8.executeFunction(this.v8.getV8RuntimePtr(), 5, this.objectHandle, name, parameters == null ? 0 : parameters.getHandle());
        if (result instanceof V8Array) {
            return (V8Array) result;
        }
        throw new V8ResultUndefined();
    }

    public V8Object executeObjectFunction(String name, V8Array parameters) {
        this.v8.checkThread();
        checkReleased();
        this.v8.checkRuntime(parameters);
        Object result = this.v8.executeFunction(this.v8.getV8RuntimePtr(), 6, this.objectHandle, name, parameters == null ? 0 : parameters.getHandle());
        if (result instanceof V8Object) {
            return (V8Object) result;
        }
        throw new V8ResultUndefined();
    }

    public Object executeFunction(String name, V8Array parameters) {
        this.v8.checkThread();
        checkReleased();
        this.v8.checkRuntime(parameters);
        return this.v8.executeFunction(this.v8.getV8RuntimePtr(), 0, this.objectHandle, name, parameters == null ? 0 : parameters.getHandle());
    }

    public Object executeJSFunction(String name) {
        return executeFunction(name, null);
    }

    public Object executeJSFunction(String name, Object... parameters) {
        if (parameters == null) {
            return executeFunction(name, null);
        }
        V8Array parameterArray = new V8Array(this.v8.getRuntime());
        try {
            for (Object object : parameters) {
                if (object == null) {
                    parameterArray.pushNull();
                } else if (object instanceof V8Value) {
                    parameterArray.push((V8Value) object);
                } else if (object instanceof Integer) {
                    parameterArray.push((Integer) object);
                } else if (object instanceof Double) {
                    parameterArray.push((Double) object);
                } else if (object instanceof Long) {
                    parameterArray.push(((Long) object).doubleValue());
                } else if (object instanceof Float) {
                    parameterArray.push((double) ((Float) object).floatValue());
                } else if (object instanceof Boolean) {
                    parameterArray.push((Boolean) object);
                } else if (object instanceof String) {
                    parameterArray.push((String) object);
                } else {
                    throw new IllegalArgumentException("Unsupported Object of type: " + object.getClass());
                }
            }
            Object executeFunction = executeFunction(name, parameterArray);
            return executeFunction;
        } finally {
            parameterArray.release();
        }
    }

    public void executeVoidFunction(String name, V8Array parameters) {
        this.v8.checkThread();
        checkReleased();
        this.v8.checkRuntime(parameters);
        this.v8.executeVoidFunction(this.v8.getV8RuntimePtr(), this.objectHandle, name, parameters == null ? 0 : parameters.getHandle());
    }

    public V8Object add(String key, int value) {
        this.v8.checkThread();
        checkReleased();
        this.v8.add(this.v8.getV8RuntimePtr(), this.objectHandle, key, value);
        return this;
    }

    public V8Object add(String key, boolean value) {
        this.v8.checkThread();
        checkReleased();
        this.v8.add(this.v8.getV8RuntimePtr(), this.objectHandle, key, value);
        return this;
    }

    public V8Object add(String key, double value) {
        this.v8.checkThread();
        checkReleased();
        this.v8.add(this.v8.getV8RuntimePtr(), this.objectHandle, key, value);
        return this;
    }

    public V8Object add(String key, String value) {
        this.v8.checkThread();
        checkReleased();
        if (value == null) {
            this.v8.addNull(this.v8.getV8RuntimePtr(), this.objectHandle, key);
        } else if (value.equals(V8.getUndefined())) {
            this.v8.addUndefined(this.v8.getV8RuntimePtr(), this.objectHandle, key);
        } else {
            this.v8.add(this.v8.getV8RuntimePtr(), this.objectHandle, key, value);
        }
        return this;
    }

    public V8Object add(String key, V8Value value) {
        this.v8.checkThread();
        checkReleased();
        this.v8.checkRuntime(value);
        if (value == null) {
            this.v8.addNull(this.v8.getV8RuntimePtr(), this.objectHandle, key);
        } else if (value.equals(V8.getUndefined())) {
            this.v8.addUndefined(this.v8.getV8RuntimePtr(), this.objectHandle, key);
        } else {
            this.v8.addObject(this.v8.getV8RuntimePtr(), this.objectHandle, key, value.getHandle());
        }
        return this;
    }

    public V8Object addUndefined(String key) {
        this.v8.checkThread();
        checkReleased();
        this.v8.addUndefined(this.v8.getV8RuntimePtr(), this.objectHandle, key);
        return this;
    }

    public V8Object addNull(String key) {
        this.v8.checkThread();
        checkReleased();
        this.v8.addNull(this.v8.getV8RuntimePtr(), this.objectHandle, key);
        return this;
    }

    public V8Object setPrototype(V8Object value) {
        this.v8.checkThread();
        checkReleased();
        this.v8.setPrototype(this.v8.getV8RuntimePtr(), this.objectHandle, value.getHandle());
        return this;
    }

    public V8Object registerJavaMethod(JavaCallback callback, String jsFunctionName) {
        this.v8.checkThread();
        checkReleased();
        this.v8.registerCallback(callback, getHandle(), jsFunctionName);
        return this;
    }

    public V8Object registerJavaMethod(JavaVoidCallback callback, String jsFunctionName) {
        this.v8.checkThread();
        checkReleased();
        this.v8.registerVoidCallback(callback, getHandle(), jsFunctionName);
        return this;
    }

    public V8Object registerJavaMethod(Object object, String methodName, String jsFunctionName, Class<?>[] parameterTypes) {
        return registerJavaMethod(object, methodName, jsFunctionName, parameterTypes, false);
    }

    public V8Object registerJavaMethod(Object object, String methodName, String jsFunctionName, Class<?>[] parameterTypes, boolean includeReceiver) {
        this.v8.checkThread();
        checkReleased();
        try {
            Method method = object.getClass().getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            this.v8.registerCallback(object, method, getHandle(), jsFunctionName, includeReceiver);
            return this;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        } catch (SecurityException e2) {
            throw new IllegalStateException(e2);
        }
    }

    public String toString() {
        if (isReleased() || this.v8.isReleased()) {
            return "[Object released]";
        }
        this.v8.checkThread();
        return this.v8.toString(this.v8.getV8RuntimePtr(), getHandle());
    }

    private void checkKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
    }
}
