package com.foxit.sdk.common;

public class DateTime {
    private int a = 0;
    private int b = 0;
    private int c = 0;
    private int d = 0;
    private int e = 0;
    private int f = 0;
    private int g = 0;
    private short h = (short) 0;
    private int i = 0;

    public void set(int i, int i2, int i3, int i4, int i5, int i6, int i7, short s, int i8) throws PDFException {
        this.a = i;
        this.b = i2;
        this.c = i3;
        this.d = i4;
        this.e = i5;
        this.f = i6;
        this.g = i7;
        this.h = s;
        this.i = i8;
    }

    public void setYear(int i) throws PDFException {
        this.a = i;
    }

    public int getYear() throws PDFException {
        return this.a;
    }

    public void setMonth(int i) throws PDFException {
        if (i < 1 || i > 12) {
            throw new PDFException(8);
        }
        this.b = i;
    }

    public int getMonth() throws PDFException {
        return this.b;
    }

    public void setDay(int i) throws PDFException {
        if (i < 1 || i > 31) {
            throw new PDFException(8);
        }
        this.c = i;
    }

    public int getDay() throws PDFException {
        return this.c;
    }

    public void setHour(int i) throws PDFException {
        if (i < 0 || i > 23) {
            throw new PDFException(8);
        }
        this.d = i;
    }

    public int getHour() throws PDFException {
        return this.d;
    }

    public void setMinute(int i) throws PDFException {
        if (i < 0 || i > 59) {
            throw new PDFException(8);
        }
        this.e = i;
    }

    public int getMinute() throws PDFException {
        return this.e;
    }

    public void setSecond(int i) throws PDFException {
        if (i < 0 || i > 59) {
            throw new PDFException(8);
        }
        this.f = i;
    }

    public int getSecond() throws PDFException {
        return this.f;
    }

    public void setMillisecond(int i) throws PDFException {
        if (i < 0 || i > 999) {
            throw new PDFException(8);
        }
        this.g = i;
    }

    public int getMillisecond() throws PDFException {
        return this.g;
    }

    public void setUTHourOffset(short s) throws PDFException {
        if (s < (short) -12 || s > (short) 12) {
            throw new PDFException(8);
        }
        this.h = s;
    }

    public short getUTHourOffset() throws PDFException {
        return this.h;
    }

    public void setUTMinuteOffset(int i) throws PDFException {
        if (i < 0 || i > 59) {
            throw new PDFException(8);
        }
        this.i = i;
    }

    public int getUTMinuteOffset() throws PDFException {
        return this.i;
    }
}
