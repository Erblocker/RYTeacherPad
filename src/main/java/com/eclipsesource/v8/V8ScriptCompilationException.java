package com.eclipsesource.v8;

public class V8ScriptCompilationException extends V8ScriptException {
    V8ScriptCompilationException(String fileName, int lineNumber, String message, String sourceLine, int startColumn, int endColumn) {
        super(fileName, lineNumber, message, sourceLine, startColumn, endColumn, null, null);
    }
}
