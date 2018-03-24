package com.foxit.sdk.common;

public class GraphState {
    private int a = 0;
    private float b = 0.0f;
    private int c = 0;
    private float d = 0.0f;
    private int e = 0;
    private float f = 0.0f;
    private float[] g = new float[]{-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f};

    public void set(int i, float f, int i2, float f2, int i3, float f3, float[] fArr) throws PDFException {
        this.a = i;
        this.b = f;
        this.c = i2;
        this.d = f2;
        this.e = i3;
        this.f = f3;
        a(fArr);
    }

    public void setBlendMode(int i) throws PDFException {
        this.a = i;
    }

    public int getBlendMode() {
        return this.a;
    }

    public void setLineWidth(float f) throws PDFException {
        this.b = f;
    }

    public float getLineWidth() {
        return this.b;
    }

    public void setLineJoin(int i) throws PDFException {
        this.c = i;
    }

    public int getLineJoin() {
        return this.c;
    }

    public void setMiterLimit(float f) throws PDFException {
        this.d = f;
    }

    public float getMiterLimit() {
        return this.d;
    }

    public void setLineCap(int i) throws PDFException {
        this.e = i;
    }

    public int getLineCap() {
        return this.e;
    }

    public void setDashPhase(float f) throws PDFException {
        this.f = f;
    }

    public float getDashPhase() {
        return this.f;
    }

    public void setDashes(float[] fArr) throws PDFException {
        if (fArr == null) {
            throw new PDFException(8);
        }
        a(fArr);
    }

    public float[] getDashes() {
        return this.g;
    }

    private void a(float[] fArr) {
        if (fArr != null) {
            for (int i = 0; i < this.g.length; i++) {
                this.g[i] = -1.0f;
            }
            System.arraycopy(fArr, 0, this.g, 0, Math.min(fArr.length, this.g.length));
        }
    }
}
