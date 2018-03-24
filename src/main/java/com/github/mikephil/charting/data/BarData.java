package com.github.mikephil.charting.data;

import java.util.ArrayList;
import java.util.List;

public class BarData extends BarLineScatterCandleBubbleData<BarDataSet> {
    private float mGroupSpace = 0.8f;

    public BarData(List<String> xVals) {
        super((List) xVals);
    }

    public BarData(String[] xVals) {
        super(xVals);
    }

    public BarData(List<String> xVals, List<BarDataSet> dataSets) {
        super((List) xVals, (List) dataSets);
    }

    public BarData(String[] xVals, List<BarDataSet> dataSets) {
        super(xVals, (List) dataSets);
    }

    public BarData(List<String> xVals, BarDataSet dataSet) {
        super((List) xVals, toList(dataSet));
    }

    public BarData(String[] xVals, BarDataSet dataSet) {
        super(xVals, toList(dataSet));
    }

    private static List<BarDataSet> toList(BarDataSet dataSet) {
        List<BarDataSet> sets = new ArrayList();
        sets.add(dataSet);
        return sets;
    }

    public float getGroupSpace() {
        if (this.mDataSets.size() <= 1) {
            return 0.0f;
        }
        return this.mGroupSpace;
    }

    public void setGroupSpace(float percent) {
        this.mGroupSpace = percent / 100.0f;
    }

    public boolean isGrouped() {
        return this.mDataSets.size() > 1;
    }
}
