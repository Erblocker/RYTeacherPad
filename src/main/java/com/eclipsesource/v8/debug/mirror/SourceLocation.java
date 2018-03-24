package com.eclipsesource.v8.debug.mirror;

public class SourceLocation {
    private final int column;
    private final int line;
    private final int position;
    private final String scriptName;
    private String sourceText;

    public SourceLocation(String scriptName, int position, int line, int column, String sourceText) {
        this.scriptName = scriptName;
        this.position = position;
        this.line = line;
        this.column = column;
        this.sourceText = sourceText;
    }

    public String toString() {
        return this.scriptName + " : " + this.position + " : " + this.line + " : " + this.column + " : " + this.sourceText;
    }

    public String getScriptName() {
        return this.scriptName;
    }

    public int getPosition() {
        return this.position;
    }

    public int getLine() {
        return this.line;
    }

    public int getColumn() {
        return this.column;
    }

    public String getSourceText() {
        return this.sourceText;
    }
}
