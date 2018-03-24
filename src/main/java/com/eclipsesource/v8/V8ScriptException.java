package com.eclipsesource.v8;

public abstract class V8ScriptException extends V8RuntimeException {
    private final int endColumn;
    private final String fileName;
    private final String jsMessage;
    private final String jsStackTrace;
    private final int lineNumber;
    private final String sourceLine;
    private final int startColumn;

    V8ScriptException(String fileName, int lineNumber, String jsMessage, String sourceLine, int startColumn, int endColumn, String jsStackTrace, Throwable cause) {
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.jsMessage = jsMessage;
        this.sourceLine = sourceLine;
        this.startColumn = startColumn;
        this.endColumn = endColumn;
        this.jsStackTrace = jsStackTrace;
        if (cause != null) {
            initCause(cause);
        }
    }

    public String getJSStackTrace() {
        return this.jsStackTrace;
    }

    public String getFileName() {
        return this.fileName;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public int getStartColumn() {
        return this.startColumn;
    }

    public int getEndColumn() {
        return this.endColumn;
    }

    public String getSourceLine() {
        return this.sourceLine;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(createMessageLine());
        result.append(createMessageDetails());
        result.append(createJSStackDetails());
        result.append("\n");
        result.append(getClass().getName());
        return result.toString();
    }

    public String getMessage() {
        return createMessageLine();
    }

    public String getJSMessage() {
        return this.jsMessage;
    }

    private String createMessageLine() {
        return this.fileName + ":" + this.lineNumber + ": " + this.jsMessage;
    }

    private String createJSStackDetails() {
        if (this.jsStackTrace != null) {
            return "\n" + this.jsStackTrace;
        }
        return "";
    }

    private String createMessageDetails() {
        StringBuilder result = new StringBuilder();
        if (!(this.sourceLine == null || this.sourceLine.isEmpty())) {
            result.append('\n');
            result.append(this.sourceLine);
            result.append('\n');
            if (this.startColumn >= 0) {
                result.append(createCharSequence(this.startColumn, ' '));
                result.append(createCharSequence(this.endColumn - this.startColumn, '^'));
            }
        }
        return result.toString();
    }

    private char[] createCharSequence(int length, char c) {
        char[] result = new char[length];
        for (int i = 0; i < length; i++) {
            result[i] = c;
        }
        return result;
    }
}
