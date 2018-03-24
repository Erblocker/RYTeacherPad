package com.foxit.sdk.pdf.annots;

import android.graphics.PointF;

public class QuadPoints {
    public PointF first;
    public PointF fourth;
    public PointF second;
    public PointF third;

    public QuadPoints(PointF pointF, PointF pointF2, PointF pointF3, PointF pointF4) {
        this.first = pointF;
        this.second = pointF2;
        this.third = pointF3;
        this.fourth = pointF4;
    }

    public void set(PointF pointF, PointF pointF2, PointF pointF3, PointF pointF4) {
        this.first = pointF;
        this.second = pointF2;
        this.third = pointF3;
        this.fourth = pointF4;
    }

    public void setFirst(PointF pointF) {
        this.first = pointF;
    }

    public PointF getFirst() {
        return this.first;
    }

    public void setSecond(PointF pointF) {
        this.second = pointF;
    }

    public PointF getSecond() {
        return this.second;
    }

    public void setThird(PointF pointF) {
        this.third = pointF;
    }

    public PointF getThird() {
        return this.third;
    }

    public void setFourth(PointF pointF) {
        this.fourth = pointF;
    }

    public PointF getFourth() {
        return this.fourth;
    }
}
