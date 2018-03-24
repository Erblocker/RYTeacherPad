package com.foxit.sdk.pdf.annots;

public class ShadingColor {
    public long firstColor;
    public long secondColor;

    public void set(long j, long j2) {
        this.firstColor = j;
        this.secondColor = j2;
    }

    public void setFirstColor(long j) {
        this.firstColor = j;
    }

    public long getFirstColor() {
        return this.firstColor;
    }

    public void setSecondColor(long j) {
        this.secondColor = j;
    }

    public long getSecondColor() {
        return this.secondColor;
    }

    public ShadingColor(long j, long j2) {
        this.firstColor = j;
        this.secondColor = j2;
    }
}
