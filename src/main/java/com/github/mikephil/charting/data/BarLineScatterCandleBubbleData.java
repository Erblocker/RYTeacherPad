package com.github.mikephil.charting.data;

import java.util.List;

public abstract class BarLineScatterCandleBubbleData<T extends BarLineScatterCandleBubbleDataSet<? extends Entry>> extends ChartData<T> {
    public BarLineScatterCandleBubbleData(List<String> xVals) {
        super((List) xVals);
    }

    public BarLineScatterCandleBubbleData(String[] xVals) {
        super(xVals);
    }

    public BarLineScatterCandleBubbleData(List<String> xVals, List<T> sets) {
        super((List) xVals, (List) sets);
    }

    public BarLineScatterCandleBubbleData(String[] xVals, List<T> sets) {
        super(xVals, (List) sets);
    }
}
