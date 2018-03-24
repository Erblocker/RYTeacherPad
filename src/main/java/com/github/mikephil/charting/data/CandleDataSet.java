package com.github.mikephil.charting.data;

import android.graphics.Paint.Style;
import com.github.mikephil.charting.utils.Utils;
import java.util.ArrayList;
import java.util.List;

public class CandleDataSet extends LineScatterCandleRadarDataSet<CandleEntry> {
    private float mBodySpace = 0.1f;
    protected int mDecreasingColor = -1;
    protected Style mDecreasingPaintStyle = Style.STROKE;
    protected int mIncreasingColor = -1;
    protected Style mIncreasingPaintStyle = Style.FILL;
    protected int mShadowColor = -1;
    private boolean mShadowColorSameAsCandle = false;
    private float mShadowWidth = 3.0f;

    public CandleDataSet(List<CandleEntry> yVals, String label) {
        super(yVals, label);
    }

    public DataSet<CandleEntry> copy() {
        List<CandleEntry> yVals = new ArrayList();
        for (int i = 0; i < this.mYVals.size(); i++) {
            yVals.add(((CandleEntry) this.mYVals.get(i)).copy());
        }
        CandleDataSet copied = new CandleDataSet(yVals, getLabel());
        copied.mColors = this.mColors;
        copied.mShadowWidth = this.mShadowWidth;
        copied.mBodySpace = this.mBodySpace;
        copied.mHighLightColor = this.mHighLightColor;
        copied.mIncreasingPaintStyle = this.mIncreasingPaintStyle;
        copied.mDecreasingPaintStyle = this.mDecreasingPaintStyle;
        copied.mShadowColor = this.mShadowColor;
        return copied;
    }

    protected void calcMinMax(int start, int end) {
        if (this.mYVals.size() != 0) {
            int endValue;
            List<CandleEntry> entries = this.mYVals;
            if (end == 0 || end >= this.mYVals.size()) {
                endValue = this.mYVals.size() - 1;
            } else {
                endValue = end;
            }
            this.mLastStart = start;
            this.mLastEnd = endValue;
            this.mYMin = Float.MAX_VALUE;
            this.mYMax = -3.4028235E38f;
            for (int i = start; i <= endValue; i++) {
                CandleEntry e = (CandleEntry) entries.get(i);
                if (e.getLow() < this.mYMin) {
                    this.mYMin = e.getLow();
                }
                if (e.getHigh() > this.mYMax) {
                    this.mYMax = e.getHigh();
                }
            }
        }
    }

    public void setBodySpace(float space) {
        if (space < 0.0f) {
            space = 0.0f;
        }
        if (space > 0.45f) {
            space = 0.45f;
        }
        this.mBodySpace = space;
    }

    public float getBodySpace() {
        return this.mBodySpace;
    }

    public void setShadowWidth(float width) {
        this.mShadowWidth = Utils.convertDpToPixel(width);
    }

    public float getShadowWidth() {
        return this.mShadowWidth;
    }

    public void setDecreasingColor(int color) {
        this.mDecreasingColor = color;
    }

    public int getDecreasingColor() {
        return this.mDecreasingColor;
    }

    public void setIncreasingColor(int color) {
        this.mIncreasingColor = color;
    }

    public int getIncreasingColor() {
        return this.mIncreasingColor;
    }

    public Style getDecreasingPaintStyle() {
        return this.mDecreasingPaintStyle;
    }

    public void setDecreasingPaintStyle(Style decreasingPaintStyle) {
        this.mDecreasingPaintStyle = decreasingPaintStyle;
    }

    public Style getIncreasingPaintStyle() {
        return this.mIncreasingPaintStyle;
    }

    public void setIncreasingPaintStyle(Style paintStyle) {
        this.mIncreasingPaintStyle = paintStyle;
    }

    public int getShadowColor() {
        return this.mShadowColor;
    }

    public void setShadowColor(int shadowColor) {
        this.mShadowColor = shadowColor;
    }

    public boolean getShadowColorSameAsCandle() {
        return this.mShadowColorSameAsCandle;
    }

    public void setShadowColorSameAsCandle(boolean shadowColorSameAsCandle) {
        this.mShadowColorSameAsCandle = shadowColorSameAsCandle;
    }
}
