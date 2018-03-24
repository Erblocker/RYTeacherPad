package org.achartengine.renderer;

import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import java.io.Serializable;

public class BasicStroke implements Serializable {
    public static final BasicStroke DASHED = new BasicStroke(Cap.ROUND, Join.BEVEL, 10.0f, new float[]{10.0f, 10.0f}, 1.0f);
    public static final BasicStroke DOTTED = new BasicStroke(Cap.ROUND, Join.BEVEL, 5.0f, new float[]{2.0f, 10.0f}, 1.0f);
    public static final BasicStroke SOLID = new BasicStroke(Cap.BUTT, Join.MITER, 4.0f, null, 0.0f);
    private Cap mCap;
    private float[] mIntervals;
    private Join mJoin;
    private float mMiter;
    private float mPhase;

    public BasicStroke(Cap cap, Join join, float miter, float[] intervals, float phase) {
        this.mCap = cap;
        this.mJoin = join;
        this.mMiter = miter;
        this.mIntervals = intervals;
    }

    public Cap getCap() {
        return this.mCap;
    }

    public Join getJoin() {
        return this.mJoin;
    }

    public float getMiter() {
        return this.mMiter;
    }

    public float[] getIntervals() {
        return this.mIntervals;
    }

    public float getPhase() {
        return this.mPhase;
    }
}
