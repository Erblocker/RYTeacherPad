package com.eclipsesource.v8.debug.mirror;

import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.V8Value;

public class Scope extends Mirror {
    private static final String SCOPE_OBJECT = "scopeObject";
    private static final String SCOPE_TYPE = "scopeType";
    private static final String SET_VARIABLE_VALUE = "setVariableValue";

    public enum ScopeType {
        Global(0),
        Local(1),
        With(2),
        Closure(3),
        Catch(4),
        Block(5),
        Script(6);
        
        int index;

        private ScopeType(int index) {
            this.index = index;
        }
    }

    Scope(V8Object v8Object) {
        super(v8Object);
    }

    public ScopeType getType() {
        return ScopeType.values()[this.v8Object.executeIntegerFunction(SCOPE_TYPE, null)];
    }

    public void setVariableValue(String name, int value) {
        V8Array parameters = new V8Array(this.v8Object.getRuntime());
        parameters.push(name);
        parameters.push(value);
        try {
            this.v8Object.executeVoidFunction(SET_VARIABLE_VALUE, parameters);
        } finally {
            parameters.release();
        }
    }

    public void setVariableValue(String name, V8Value value) {
        V8Array parameters = new V8Array(this.v8Object.getRuntime());
        parameters.push(name);
        parameters.push(value);
        try {
            this.v8Object.executeVoidFunction(SET_VARIABLE_VALUE, parameters);
        } finally {
            parameters.release();
        }
    }

    public void setVariableValue(String name, boolean value) {
        V8Array parameters = new V8Array(this.v8Object.getRuntime());
        parameters.push(name);
        parameters.push(value);
        try {
            this.v8Object.executeVoidFunction(SET_VARIABLE_VALUE, parameters);
        } finally {
            parameters.release();
        }
    }

    public void setVariableValue(String name, String value) {
        V8Array parameters = new V8Array(this.v8Object.getRuntime());
        parameters.push(name);
        parameters.push(value);
        try {
            this.v8Object.executeVoidFunction(SET_VARIABLE_VALUE, parameters);
        } finally {
            parameters.release();
        }
    }

    public void setVariableValue(String name, double value) {
        V8Array parameters = new V8Array(this.v8Object.getRuntime());
        parameters.push(name);
        parameters.push(value);
        try {
            this.v8Object.executeVoidFunction(SET_VARIABLE_VALUE, parameters);
        } finally {
            parameters.release();
        }
    }

    public ObjectMirror getScopeObject() {
        V8Object mirror = null;
        try {
            mirror = this.v8Object.executeObjectFunction(SCOPE_OBJECT, null);
            ObjectMirror objectMirror = (ObjectMirror) Mirror.createMirror(mirror);
            return objectMirror;
        } finally {
            if (mirror != null) {
                mirror.release();
            }
        }
    }
}
