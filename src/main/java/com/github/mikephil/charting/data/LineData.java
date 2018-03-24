package com.github.mikephil.charting.data;

import java.util.ArrayList;
import java.util.List;

public class LineData extends BarLineScatterCandleBubbleData<LineDataSet> {
    public LineData(List<String> xVals) {
        super((List) xVals);
    }

    public LineData(String[] xVals) {
        super(xVals);
    }

    public LineData(List<String> xVals, List<LineDataSet> dataSets) {
        super((List) xVals, (List) dataSets);
    }

    public LineData(String[] xVals, List<LineDataSet> dataSets) {
        super(xVals, (List) dataSets);
    }

    public LineData(List<String> xVals, LineDataSet dataSet) {
        super((List) xVals, toList(dataSet));
    }

    public LineData(String[] xVals, LineDataSet dataSet) {
        super(xVals, toList(dataSet));
    }

    private static List<LineDataSet> toList(LineDataSet dataSet) {
        List<LineDataSet> sets = new ArrayList();
        sets.add(dataSet);
        return sets;
    }
}
