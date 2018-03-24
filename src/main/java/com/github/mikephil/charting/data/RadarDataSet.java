package com.github.mikephil.charting.data;

import java.util.ArrayList;
import java.util.List;

public class RadarDataSet extends LineRadarDataSet<Entry> {
    public RadarDataSet(List<Entry> yVals, String label) {
        super(yVals, label);
    }

    public DataSet<Entry> copy() {
        List<Entry> yVals = new ArrayList();
        for (int i = 0; i < this.mYVals.size(); i++) {
            yVals.add(((Entry) this.mYVals.get(i)).copy());
        }
        RadarDataSet copied = new RadarDataSet(yVals, getLabel());
        copied.mColors = this.mColors;
        copied.mHighLightColor = this.mHighLightColor;
        return copied;
    }
}
