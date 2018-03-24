package com.eclipsesource.v8.debug.mirror;

import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;

public class Frame extends Mirror {
    private static final String ARGUMENT_COUNT = "argumentCount";
    private static final String ARGUMENT_NAME = "argumentName";
    private static final String ARGUMENT_VALUE = "argumentValue";
    private static final String COLUMN = "column";
    private static final String FUNC = "func";
    private static final String LINE = "line";
    private static final String LOCAL_COUNT = "localCount";
    private static final String LOCAL_NAME = "localName";
    private static final String LOCAL_VALUE = "localValue";
    private static final String NAME = "name";
    private static final String POSITION = "position";
    private static final String SCOPE = "scope";
    private static final String SCOPE_COUNT = "scopeCount";
    private static final String SCRIPT = "script";
    private static final String SOURCE_LOCATION = "sourceLocation";
    private static final String SOURCE_TEXT = "sourceText";

    public Frame(V8Object v8Object) {
        super(v8Object);
    }

    public int getScopeCount() {
        return this.v8Object.executeIntegerFunction(SCOPE_COUNT, null);
    }

    public SourceLocation getSourceLocation() {
        V8Object sourceLocation = this.v8Object.executeObjectFunction(SOURCE_LOCATION, null);
        FunctionMirror function = getFunction();
        String functionScriptName = function.getScriptName();
        String scriptName = null;
        try {
            V8Object scriptObject = (V8Object) sourceLocation.get(SCRIPT);
            if (scriptObject != null) {
                scriptName = scriptObject.getString("name");
                scriptObject.release();
            }
            if (scriptName != null || functionScriptName == null) {
                scriptName = "undefined";
            } else {
                scriptName = functionScriptName;
            }
            SourceLocation sourceLocation2 = new SourceLocation(scriptName, sourceLocation.getInteger(POSITION), sourceLocation.getInteger(LINE), sourceLocation.getInteger(COLUMN), sourceLocation.getString(SOURCE_TEXT));
            return sourceLocation2;
        } finally {
            function.release();
            sourceLocation.release();
        }
    }

    public int getArgumentCount() {
        return this.v8Object.executeIntegerFunction(ARGUMENT_COUNT, null);
    }

    public String getArgumentName(int index) {
        V8Array parameters = new V8Array(this.v8Object.getRuntime());
        parameters.push(index);
        try {
            String executeStringFunction = this.v8Object.executeStringFunction(ARGUMENT_NAME, parameters);
            return executeStringFunction;
        } finally {
            parameters.release();
        }
    }

    public ValueMirror getArgumentValue(int index) {
        V8Array parameters = new V8Array(this.v8Object.getRuntime());
        parameters.push(index);
        V8Object result = null;
        try {
            result = this.v8Object.executeObjectFunction(ARGUMENT_VALUE, parameters);
            if (Mirror.isValue(result)) {
                ValueMirror valueMirror = new ValueMirror(result);
                return valueMirror;
            }
            throw new IllegalStateException("Argument value is not a ValueMirror");
        } finally {
            parameters.release();
            if (result != null) {
                result.release();
            }
        }
    }

    public ValueMirror getLocalValue(int index) {
        V8Array parameters = new V8Array(this.v8Object.getRuntime());
        parameters.push(index);
        V8Object result = null;
        try {
            result = this.v8Object.executeObjectFunction(LOCAL_VALUE, parameters);
            if (Mirror.isValue(result)) {
                ValueMirror createMirror = Mirror.createMirror(result);
                return createMirror;
            }
            throw new IllegalStateException("Local value is not a ValueMirror");
        } finally {
            parameters.release();
            if (result != null) {
                result.release();
            }
        }
    }

    public int getLocalCount() {
        return this.v8Object.executeIntegerFunction(LOCAL_COUNT, null);
    }

    public String getLocalName(int index) {
        V8Array parameters = new V8Array(this.v8Object.getRuntime());
        parameters.push(index);
        try {
            String executeStringFunction = this.v8Object.executeStringFunction(LOCAL_NAME, parameters);
            return executeStringFunction;
        } finally {
            parameters.release();
        }
    }

    public Scope getScope(int index) {
        V8Array parameters = new V8Array(this.v8Object.getRuntime());
        parameters.push(index);
        V8Object scope = null;
        try {
            scope = this.v8Object.executeObjectFunction(SCOPE, parameters);
            Scope scope2 = new Scope(scope);
            return scope2;
        } finally {
            parameters.release();
            if (scope != null) {
                scope.release();
            }
        }
    }

    public FunctionMirror getFunction() {
        V8Object function = null;
        try {
            function = this.v8Object.executeObjectFunction(FUNC, null);
            FunctionMirror functionMirror = new FunctionMirror(function);
            return functionMirror;
        } finally {
            if (function != null) {
                function.release();
            }
        }
    }

    public boolean isFrame() {
        return true;
    }
}
