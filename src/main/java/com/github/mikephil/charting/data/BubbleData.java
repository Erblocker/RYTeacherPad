package com.github.mikephil.charting.data;

import java.util.ArrayList;
import java.util.List;

public class BubbleData extends BarLineScatterCandleBubbleData<BubbleDataSet> {
    public BubbleData(List<String> xVals) {
        super((List) xVals);
    }

    public BubbleData(String[] xVals) {
        super(xVals);
    }

    public BubbleData(List<String> xVals, List<BubbleDataSet> dataSets) {
        super((List) xVals, (List) dataSets);
    }

    public BubbleData(String[] xVals, List<BubbleDataSet> dataSets) {
        super(xVals, (List) dataSets);
    }

    public BubbleData(List<String> xVals, BubbleDataSet dataSet) {
        super((List) xVals, toList(dataSet));
    }

    public BubbleData(String[] xVals, BubbleDataSet dataSet) {
        super(xVals, toList(dataSet));
    }

    private static List<BubbleDataSet> toList(BubbleDataSet dataSet) {
        List<BubbleDataSet> sets = new ArrayList();
        sets.add(dataSet);
        return sets;
    }

    public void setHighlightCircleWidth(float width) {
        for (BubbleDataSet set : this.mDataSets) {
            set.setHighlightCircleWidth(width);
        }
    }
}
