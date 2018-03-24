package com.github.mikephil.charting.data;

import java.util.ArrayList;
import java.util.List;

public class PieData extends ChartData<PieDataSet> {
    public PieData(List<String> xVals) {
        super((List) xVals);
    }

    public PieData(String[] xVals) {
        super(xVals);
    }

    public PieData(List<String> xVals, PieDataSet dataSet) {
        super((List) xVals, toList(dataSet));
    }

    public PieData(String[] xVals, PieDataSet dataSet) {
        super(xVals, toList(dataSet));
    }

    private static List<PieDataSet> toList(PieDataSet dataSet) {
        List<PieDataSet> sets = new ArrayList();
        sets.add(dataSet);
        return sets;
    }

    public void setDataSet(PieDataSet dataSet) {
        this.mDataSets.clear();
        this.mDataSets.add(dataSet);
        init();
    }

    public PieDataSet getDataSet() {
        return (PieDataSet) this.mDataSets.get(0);
    }

    public PieDataSet getDataSetByIndex(int index) {
        return index == 0 ? getDataSet() : null;
    }

    public PieDataSet getDataSetByLabel(String label, boolean ignorecase) {
        return ignorecase ? label.equalsIgnoreCase(((PieDataSet) this.mDataSets.get(0)).getLabel()) ? (PieDataSet) this.mDataSets.get(0) : null : label.equals(((PieDataSet) this.mDataSets.get(0)).getLabel()) ? (PieDataSet) this.mDataSets.get(0) : null;
    }
}
