package com.github.mikephil.charting.data;

import java.util.ArrayList;
import java.util.List;

public class CandleData extends BarLineScatterCandleBubbleData<CandleDataSet> {
    public CandleData(List<String> xVals) {
        super((List) xVals);
    }

    public CandleData(String[] xVals) {
        super(xVals);
    }

    public CandleData(List<String> xVals, List<CandleDataSet> dataSets) {
        super((List) xVals, (List) dataSets);
    }

    public CandleData(String[] xVals, List<CandleDataSet> dataSets) {
        super(xVals, (List) dataSets);
    }

    public CandleData(List<String> xVals, CandleDataSet dataSet) {
        super((List) xVals, toList(dataSet));
    }

    public CandleData(String[] xVals, CandleDataSet dataSet) {
        super(xVals, toList(dataSet));
    }

    private static List<CandleDataSet> toList(CandleDataSet dataSet) {
        List<CandleDataSet> sets = new ArrayList();
        sets.add(dataSet);
        return sets;
    }
}
