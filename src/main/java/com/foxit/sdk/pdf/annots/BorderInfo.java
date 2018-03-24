package com.foxit.sdk.pdf.annots;

import com.foxit.sdk.common.PDFException;

public class BorderInfo {
    private float a = 0.0f;
    private int b = 0;
    private float c = 0.0f;
    private float d = 0.0f;
    private float[] e = new float[]{-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f};

    public void set(float f, int i, float f2, float f3, float[] fArr) throws PDFException {
        int i2 = 0;
        if (f < 0.0f) {
            throw new PDFException(8);
        } else if (i < 0 || i > 5) {
            throw new PDFException(8);
        } else {
            for (int i3 = 0; i3 < this.e.length; i3++) {
                this.e[i3] = -1.0f;
            }
            if (fArr != null) {
                while (i2 < fArr.length && i2 < this.e.length) {
                    this.e[i2] = fArr[i2];
                    i2++;
                }
            }
            this.a = f;
            this.b = i;
            this.c = f2;
            this.d = f3;
            this.e = fArr;
        }
    }

    public void setWidth(float f) throws PDFException {
        if (f < 0.0f) {
            throw new PDFException(8);
        }
        this.a = f;
    }

    public float getWidth() throws PDFException {
        return this.a;
    }

    public void setStyle(int i) throws PDFException {
        if (i < 0 || i > 5) {
            throw new PDFException(8);
        }
        this.b = i;
    }

    public int getStyle() throws PDFException {
        return this.b;
    }

    public void setCloudIntensity(float f) throws PDFException {
        this.c = f;
    }

    public float getCloudIntensity() throws PDFException {
        return this.c;
    }

    public void setDashPhase(float f) throws PDFException {
        this.d = f;
    }

    public float getDashPhase() throws PDFException {
        return this.d;
    }

    public void setDashes(float[] fArr) throws PDFException {
        if (fArr == null) {
            throw new PDFException(8);
        }
        for (int i = 0; i < this.e.length; i++) {
            this.e[i] = -1.0f;
        }
        System.arraycopy(fArr, 0, this.e, 0, Math.min(fArr.length, this.e.length));
    }

    public float[] getDashes() throws PDFException {
        return this.e;
    }
}
