package com.github.mikephil.charting.data;

public class CandleEntry extends Entry {
    private float mClose = 0.0f;
    private float mOpen = 0.0f;
    private float mShadowHigh = 0.0f;
    private float mShadowLow = 0.0f;

    public CandleEntry(int xIndex, float shadowH, float shadowL, float open, float close) {
        super((shadowH + shadowL) / 2.0f, xIndex);
        this.mShadowHigh = shadowH;
        this.mShadowLow = shadowL;
        this.mOpen = open;
        this.mClose = close;
    }

    public CandleEntry(int xIndex, float shadowH, float shadowL, float open, float close, Object data) {
        super((shadowH + shadowL) / 2.0f, xIndex, data);
        this.mShadowHigh = shadowH;
        this.mShadowLow = shadowL;
        this.mOpen = open;
        this.mClose = close;
    }

    public float getShadowRange() {
        return Math.abs(this.mShadowHigh - this.mShadowLow);
    }

    public float getBodyRange() {
        return Math.abs(this.mOpen - this.mClose);
    }

    public float getVal() {
        return super.getVal();
    }

    public CandleEntry copy() {
        return new CandleEntry(getXIndex(), this.mShadowHigh, this.mShadowLow, this.mOpen, this.mClose, getData());
    }

    public float getHigh() {
        return this.mShadowHigh;
    }

    public void setHigh(float mShadowHigh) {
        this.mShadowHigh = mShadowHigh;
    }

    public float getLow() {
        return this.mShadowLow;
    }

    public void setLow(float mShadowLow) {
        this.mShadowLow = mShadowLow;
    }

    public float getClose() {
        return this.mClose;
    }

    public void setClose(float mClose) {
        this.mClose = mClose;
    }

    public float getOpen() {
        return this.mOpen;
    }

    public void setOpen(float mOpen) {
        this.mOpen = mOpen;
    }
}
