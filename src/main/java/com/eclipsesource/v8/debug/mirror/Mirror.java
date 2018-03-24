package com.eclipsesource.v8.debug.mirror;

import com.eclipsesource.v8.Releasable;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.V8ResultUndefined;

public class Mirror implements Releasable {
    private static final String IS_ARRAY = "isArray";
    private static final String IS_BOOLEAN = "isBoolean";
    private static final String IS_FUNCTION = "isFunction";
    private static final String IS_NULL = "isNull";
    private static final String IS_NUMBER = "isNumber";
    private static final String IS_OBJECT = "isObject";
    private static final String IS_STRING = "isString";
    private static final String IS_UNDEFINED = "isUndefined";
    private static final String IS_VALUE = "isValue";
    protected V8Object v8Object;

    Mirror(V8Object v8Object) {
        this.v8Object = v8Object.twin();
    }

    public boolean isUndefined() {
        return this.v8Object.executeBooleanFunction(IS_UNDEFINED, null);
    }

    public boolean isValue() {
        return false;
    }

    public boolean isNull() {
        return false;
    }

    public boolean isBoolean() {
        return false;
    }

    public boolean isNumber() {
        return false;
    }

    public boolean isString() {
        return false;
    }

    public boolean isObject() {
        return false;
    }

    public boolean isFunction() {
        return false;
    }

    public boolean isArray() {
        return false;
    }

    public boolean isFrame() {
        return false;
    }

    public boolean isProperty() {
        return false;
    }

    public void release() {
        if (this.v8Object != null && !this.v8Object.isReleased()) {
            this.v8Object.release();
            this.v8Object = null;
        }
    }

    protected static boolean isValue(V8Object mirror) {
        try {
            return mirror.executeBooleanFunction(IS_VALUE, null);
        } catch (V8ResultUndefined e) {
            return false;
        }
    }

    private static boolean isObject(V8Object mirror) {
        try {
            return mirror.executeBooleanFunction(IS_OBJECT, null);
        } catch (V8ResultUndefined e) {
            return false;
        }
    }

    private static boolean isNumber(V8Object mirror) {
        try {
            return mirror.executeBooleanFunction(IS_NUMBER, null);
        } catch (V8ResultUndefined e) {
            return false;
        }
    }

    private static boolean isBoolean(V8Object mirror) {
        try {
            return mirror.executeBooleanFunction(IS_BOOLEAN, null);
        } catch (V8ResultUndefined e) {
            return false;
        }
    }

    private static boolean isFunction(V8Object mirror) {
        try {
            return mirror.executeBooleanFunction(IS_FUNCTION, null);
        } catch (V8ResultUndefined e) {
            return false;
        }
    }

    private static boolean isArray(V8Object mirror) {
        try {
            return mirror.executeBooleanFunction(IS_ARRAY, null);
        } catch (V8ResultUndefined e) {
            return false;
        }
    }

    private static boolean isString(V8Object mirror) {
        try {
            return mirror.executeBooleanFunction(IS_STRING, null);
        } catch (V8ResultUndefined e) {
            return false;
        }
    }

    private static boolean isUndefined(V8Object mirror) {
        try {
            return mirror.executeBooleanFunction(IS_UNDEFINED, null);
        } catch (V8ResultUndefined e) {
            return false;
        }
    }

    private static boolean isNull(V8Object mirror) {
        try {
            return mirror.executeBooleanFunction(IS_NULL, null);
        } catch (V8ResultUndefined e) {
            return false;
        }
    }

    protected static ValueMirror createMirror(V8Object mirror) {
        if (isNull(mirror)) {
            return new NullMirror(mirror);
        }
        if (isUndefined(mirror)) {
            return new UndefinedMirror(mirror);
        }
        if (isFunction(mirror)) {
            return new FunctionMirror(mirror);
        }
        if (isArray(mirror)) {
            return new ArrayMirror(mirror);
        }
        if (isObject(mirror)) {
            return new ObjectMirror(mirror);
        }
        if (isString(mirror)) {
            return new StringMirror(mirror);
        }
        if (isNumber(mirror)) {
            return new NumberMirror(mirror);
        }
        if (isBoolean(mirror)) {
            return new BooleanMirror(mirror);
        }
        return new ValueMirror(mirror);
    }

    public String toString() {
        return this.v8Object.toString();
    }

    public boolean equals(Object obj) {
        if (obj != null && (obj instanceof Mirror)) {
            return this.v8Object.equals(((Mirror) obj).v8Object);
        }
        return false;
    }

    public int hashCode() {
        return this.v8Object.hashCode();
    }
}
