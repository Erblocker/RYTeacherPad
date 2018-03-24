package com.netspace.teacherpad.controls;

import android.content.Context;
import android.widget.TextView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.netspace.teacherpad.R;

public class ReportMarkerView extends MarkerView {
    private LineChart mChart;
    private TextView tvContent = ((TextView) findViewById(R.id.tvContent));

    public ReportMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
    }

    public void setChart(LineChart chart) {
        this.mChart = chart;
    }

    public void refreshContent(Entry e, Highlight highlight) {
        this.tvContent.setText(this.mChart.getXValue(e.getXIndex()));
    }

    public int getXOffset(float xpos) {
        return -(getWidth() / 2);
    }

    public int getYOffset(float ypos) {
        return -getHeight();
    }
}
