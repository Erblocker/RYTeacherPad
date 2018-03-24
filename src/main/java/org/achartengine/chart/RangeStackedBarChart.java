package org.achartengine.chart;

import org.achartengine.chart.BarChart.Type;

public class RangeStackedBarChart extends RangeBarChart {
    public static final String TYPE = "RangeStackedBar";

    RangeStackedBarChart() {
        super(Type.STACKED);
    }

    public String getChartType() {
        return TYPE;
    }
}
