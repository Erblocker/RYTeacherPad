package com.foxit.sdk.common;

public class DefaultAppearance {
    public static final int e_defaultAPFont = 1;
    public static final int e_defaultAPFontSize = 4;
    public static final int e_defaultAPTextColor = 2;
    private long a = 0;
    private Font b = null;
    private float c = 0.0f;
    private long d = 0;

    public void release() throws PDFException {
        this.a = 0;
        this.c = 0.0f;
        this.d = 0;
        this.b = null;
    }

    public void set(long j, Font font, float f, long j2) throws PDFException {
        if (j < 0) {
            throw new PDFException(8);
        }
        this.a = j;
        this.b = font;
        this.c = f;
        this.d = j2;
    }

    public void setFlags(long j) throws PDFException {
        if (j < 0) {
            throw new PDFException(8);
        }
        this.a = j;
    }

    public long getFlags() throws PDFException {
        return this.a;
    }

    public void setFont(Font font) throws PDFException {
        this.b = font;
    }

    public Font getFont() throws PDFException {
        return this.b;
    }

    public void setFontSize(float f) throws PDFException {
        this.c = f;
    }

    public float getFontSize() throws PDFException {
        return this.c;
    }

    public void setTextColor(long j) throws PDFException {
        this.d = j;
    }

    public long getTextColor() throws PDFException {
        return this.d;
    }
}
