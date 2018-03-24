package com.eclipsesource.v8;

public class V8ScriptExecutionException extends V8ScriptException {
    V8ScriptExecutionException(String fileName, int lineNumber, String message, String sourceLine, int startColumn, int endColumn, String jsStackTrace) {
        this(fileName, lineNumber, message, sourceLine, startColumn, endColumn, jsStackTrace, null);
    }

    V8ScriptExecutionException(String fileName, int lineNumber, String message, String sourceLine, int startColumn, int endColumn, String jsStackTrace, Throwable cause) {
        super(fileName, lineNumber, message, sourceLine, startColumn, endColumn, jsStackTrace, cause);
    }
}
