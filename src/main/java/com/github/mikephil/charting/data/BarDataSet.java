package com.github.mikephil.charting.data;

import android.graphics.Color;
import java.util.ArrayList;
import java.util.List;
import org.ksoap2.SoapEnvelope;

public class BarDataSet extends BarLineScatterCandleBubbleDataSet<BarEntry> {
    private int mBarShadowColor;
    private float mBarSpace;
    private int mEntryCountStacks;
    private int mHighLightAlpha;
    private String[] mStackLabels;
    private int mStackSize;

    public BarDataSet(List<BarEntry> yVals, String label) {
        super(yVals, label);
        this.mBarSpace = 0.15f;
        this.mStackSize = 1;
        this.mBarShadowColor = Color.rgb(215, 215, 215);
        this.mHighLightAlpha = SoapEnvelope.VER12;
        this.mEntryCountStacks = 0;
        this.mStackLabels = new String[]{"Stack"};
        this.mHighLightColor = Color.rgb(0, 0, 0);
        calcStackSize(yVals);
        calcEntryCountIncludingStacks(yVals);
    }

    public DataSet<BarEntry> copy() {
        List<BarEntry> yVals = new ArrayList();
        for (int i = 0; i < this.mYVals.size(); i++) {
            yVals.add(((BarEntry) this.mYVals.get(i)).copy());
        }
        BarDataSet copied = new BarDataSet(yVals, getLabel());
        copied.mColors = this.mColors;
        copied.mStackSize = this.mStackSize;
        copied.mBarSpace = this.mBarSpace;
        copied.mBarShadowColor = this.mBarShadowColor;
        copied.mStackLabels = this.mStackLabels;
        copied.mHighLightColor = this.mHighLightColor;
        copied.mHighLightAlpha = this.mHighLightAlpha;
        return copied;
    }

    private void calcEntryCountIncludingStacks(List<BarEntry> yVals) {
        this.mEntryCountStacks = 0;
        for (int i = 0; i < yVals.size(); i++) {
            float[] vals = ((BarEntry) yVals.get(i)).getVals();
            if (vals == null) {
                this.mEntryCountStacks++;
            } else {
                this.mEntryCountStacks += vals.length;
            }
        }
    }

    private void calcStackSize(List<BarEntry> yVals) {
        for (int i = 0; i < yVals.size(); i++) {
            float[] vals = ((BarEntry) yVals.get(i)).getVals();
            if (vals != null && vals.length > this.mStackSize) {
                this.mStackSize = vals.length;
            }
        }
    }

    protected void calcMinMax(int start, int end) {
        int yValCount = this.mYVals.size();
        if (yValCount != 0) {
            int endValue;
            if (end == 0 || end >= yValCount) {
                endValue = yValCount - 1;
            } else {
                endValue = end;
            }
            this.mLastStart = start;
            this.mLastEnd = endValue;
            this.mYMin = Float.MAX_VALUE;
            this.mYMax = -3.4028235E38f;
            for (int i = start; i <= endValue; i++) {
                BarEntry e = (BarEntry) this.mYVals.get(i);
                if (!(e == null || Float.isNaN(e.getVal()))) {
                    if (e.getVals() == null) {
                        if (e.getVal() < this.mYMin) {
                            this.mYMin = e.getVal();
                        }
                        if (e.getVal() > this.mYMax) {
                            this.mYMax = e.getVal();
                        }
                    } else {
                        if ((-e.getNegativeSum()) < this.mYMin) {
                            this.mYMin = -e.getNegativeSum();
                        }
                        if (e.getPositiveSum() > this.mYMax) {
                            this.mYMax = e.getPositiveSum();
                        }
                    }
                }
            }
            if (this.mYMin == Float.MAX_VALUE) {
                this.mYMin = 0.0f;
                this.mYMax = 0.0f;
            }
        }
    }

    public int getStackSize() {
        return this.mStackSize;
    }

    public boolean isStacked() {
        return this.mStackSize > 1;
    }

    public int getEntryCountStacks() {
        return this.mEntryCountStacks;
    }

    public float getBarSpacePercent() {
        return this.mBarSpace * 100.0f;
    }

    public float getBarSpace() {
        return this.mBarSpace;
    }

    public void setBarSpacePercent(float percent) {
        this.mBarSpace = percent / 100.0f;
    }

    public void setBarShadowColor(int color) {
        this.mBarShadowColor = color;
    }

    public int getBarShadowColor() {
        return this.mBarShadowColor;
    }

    public void setHighLightAlpha(int alpha) {
        this.mHighLightAlpha = alpha;
    }

    public int getHighLightAlpha() {
        return this.mHighLightAlpha;
    }

    public void setStackLabels(String[] labels) {
        this.mStackLabels = labels;
    }

    public String[] getStackLabels() {
        return this.mStackLabels;
    }
}
