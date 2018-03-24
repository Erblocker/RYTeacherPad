package com.github.mikephil.charting.data;

import java.util.ArrayList;
import java.util.List;

public class RadarData extends ChartData<RadarDataSet> {
    public RadarData(List<String> xVals) {
        super((List) xVals);
    }

    public RadarData(String[] xVals) {
        super(xVals);
    }

    public RadarData(List<String> xVals, List<RadarDataSet> dataSets) {
        super((List) xVals, (List) dataSets);
    }

    public RadarData(String[] xVals, List<RadarDataSet> dataSets) {
        super(xVals, (List) dataSets);
    }

    public RadarData(List<String> xVals, RadarDataSet dataSet) {
        super((List) xVals, toList(dataSet));
    }

    public RadarData(String[] xVals, RadarDataSet dataSet) {
        super(xVals, toList(dataSet));
    }

    private static List<RadarDataSet> toList(RadarDataSet dataSet) {
        List<RadarDataSet> sets = new ArrayList();
        sets.add(dataSet);
        return sets;
    }
}
