package com.github.mikephil.charting.formatter;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.ViewPortHandler;
import java.text.DecimalFormat;

public class PercentFormatter implements ValueFormatter, YAxisValueFormatter {
    protected DecimalFormat mFormat;

    public PercentFormatter() {
        this.mFormat = new DecimalFormat("###,###,##0.0");
    }

    public PercentFormatter(DecimalFormat format) {
        this.mFormat = format;
    }

    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return new StringBuilder(String.valueOf(this.mFormat.format((double) value))).append(" %").toString();
    }

    public String getFormattedValue(float value, YAxis yAxis) {
        return new StringBuilder(String.valueOf(this.mFormat.format((double) value))).append(" %").toString();
    }
}
