package com.github.mikephil.charting.data;

import com.github.mikephil.charting.utils.Utils;
import java.util.ArrayList;
import java.util.List;

public class PieDataSet extends DataSet<Entry> {
    private float mShift = 18.0f;
    private float mSliceSpace = 0.0f;

    public PieDataSet(List<Entry> yVals, String label) {
        super(yVals, label);
    }

    public DataSet<Entry> copy() {
        List<Entry> yVals = new ArrayList();
        for (int i = 0; i < this.mYVals.size(); i++) {
            yVals.add(((Entry) this.mYVals.get(i)).copy());
        }
        PieDataSet copied = new PieDataSet(yVals, getLabel());
        copied.mColors = this.mColors;
        copied.mSliceSpace = this.mSliceSpace;
        copied.mShift = this.mShift;
        return copied;
    }

    public void setSliceSpace(float degrees) {
        if (degrees > 45.0f) {
            degrees = 45.0f;
        }
        if (degrees < 0.0f) {
            degrees = 0.0f;
        }
        this.mSliceSpace = degrees;
    }

    public float getSliceSpace() {
        return this.mSliceSpace;
    }

    public void setSelectionShift(float shift) {
        this.mShift = Utils.convertDpToPixel(shift);
    }

    public float getSelectionShift() {
        return this.mShift;
    }
}
