package com.foxit.sdk.pdf.graphics;

import android.graphics.PointF;
import com.foxit.sdk.common.Font;

public class PDFTextState {
    private long a = 1;
    private Font b = null;
    private float c = 0.0f;
    private float d = 0.0f;
    private float e = 0.0f;
    private int f = 0;
    private PointF g = new PointF();
    private float[] h = new float[]{1.0f, 0.0f, 0.0f, 1.0f};

    public void set(long j, Font font, float f, float f2, float f3, int i, PointF pointF, float[] fArr) {
        this.a = j;
        this.b = font;
        this.c = f;
        this.d = f2;
        this.e = f3;
        this.f = i;
        this.g = pointF;
        this.h = fArr;
    }

    public void setVersion(long j) {
        this.a = j;
    }

    public long getVersion() {
        return this.a;
    }

    public void setFont(Font font) {
        this.b = font;
    }

    public Font getFont() {
        return this.b;
    }

    public void setFontSize(float f) {
        this.c = f;
    }

    public float getFontSize() {
        return this.c;
    }

    public void setCharSpace(float f) {
        this.d = f;
    }

    public float getCharSpace() {
        return this.d;
    }

    public void setWordSpace(float f) {
        this.e = f;
    }

    public float getWordSpace() {
        return this.e;
    }

    public void setTextMode(int i) {
        this.f = i;
    }

    public int getTextMode() {
        return this.f;
    }

    public void setOriginPosition(PointF pointF) {
        this.g = pointF;
    }

    public PointF getOriginPosition() {
        return this.g;
    }

    public void setTextMatrix(float[] fArr) {
        this.h = fArr;
    }

    public float[] getTextMatrix() {
        return this.h;
    }
}
