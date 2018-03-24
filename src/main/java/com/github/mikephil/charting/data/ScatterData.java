package com.github.mikephil.charting.data;

import java.util.ArrayList;
import java.util.List;

public class ScatterData extends BarLineScatterCandleBubbleData<ScatterDataSet> {
    public ScatterData(List<String> xVals) {
        super((List) xVals);
    }

    public ScatterData(String[] xVals) {
        super(xVals);
    }

    public ScatterData(List<String> xVals, List<ScatterDataSet> dataSets) {
        super((List) xVals, (List) dataSets);
    }

    public ScatterData(String[] xVals, List<ScatterDataSet> dataSets) {
        super(xVals, (List) dataSets);
    }

    public ScatterData(List<String> xVals, ScatterDataSet dataSet) {
        super((List) xVals, toList(dataSet));
    }

    public ScatterData(String[] xVals, ScatterDataSet dataSet) {
        super(xVals, toList(dataSet));
    }

    private static List<ScatterDataSet> toList(ScatterDataSet dataSet) {
        List<ScatterDataSet> sets = new ArrayList();
        sets.add(dataSet);
        return sets;
    }

    public float getGreatestShapeSize() {
        float max = 0.0f;
        for (ScatterDataSet set : this.mDataSets) {
            float size = set.getScatterShapeSize();
            if (size > max) {
                max = size;
            }
        }
        return max;
    }
}
