package org.achartengine.chart;

import android.graphics.Canvas;
import android.graphics.Paint;
import java.util.List;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;

public class CombinedXYChart extends XYChart {
    private XYChart[] mCharts;
    private Class<?>[] xyChartTypes = new Class[]{TimeChart.class, LineChart.class, CubicLineChart.class, BarChart.class, BubbleChart.class, ScatterChart.class, RangeBarChart.class, RangeStackedBarChart.class};

    public CombinedXYChart(XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer, String[] types) {
        super(dataset, renderer);
        int length = types.length;
        this.mCharts = new XYChart[length];
        for (int i = 0; i < length; i++) {
            try {
                this.mCharts[i] = getXYChart(types[i]);
            } catch (Exception e) {
            }
            if (this.mCharts[i] == null) {
                throw new IllegalArgumentException("Unknown chart type " + types[i]);
            }
            XYMultipleSeriesDataset newDataset = new XYMultipleSeriesDataset();
            newDataset.addSeries(dataset.getSeriesAt(i));
            XYMultipleSeriesRenderer newRenderer = new XYMultipleSeriesRenderer();
            newRenderer.setBarSpacing(renderer.getBarSpacing());
            newRenderer.setPointSize(renderer.getPointSize());
            int scale = dataset.getSeriesAt(i).getScaleNumber();
            if (renderer.isMinXSet(scale)) {
                newRenderer.setXAxisMin(renderer.getXAxisMin(scale));
            }
            if (renderer.isMaxXSet(scale)) {
                newRenderer.setXAxisMax(renderer.getXAxisMax(scale));
            }
            if (renderer.isMinYSet(scale)) {
                newRenderer.setYAxisMin(renderer.getYAxisMin(scale));
            }
            if (renderer.isMaxYSet(scale)) {
                newRenderer.setYAxisMax(renderer.getYAxisMax(scale));
            }
            newRenderer.addSeriesRenderer(renderer.getSeriesRendererAt(i));
            this.mCharts[i].setDatasetRenderer(newDataset, newRenderer);
        }
    }

    private XYChart getXYChart(String type) throws IllegalAccessException, InstantiationException {
        XYChart chart = null;
        int length = this.xyChartTypes.length;
        for (int i = 0; i < length && chart == null; i++) {
            XYChart newChart = (XYChart) this.xyChartTypes[i].newInstance();
            if (type.equals(newChart.getChartType())) {
                chart = newChart;
            }
        }
        return chart;
    }

    public void drawSeries(Canvas canvas, Paint paint, List<Float> points, SimpleSeriesRenderer seriesRenderer, float yAxisValue, int seriesIndex, int startIndex) {
        this.mCharts[seriesIndex].setScreenR(getScreenR());
        this.mCharts[seriesIndex].setCalcRange(getCalcRange(this.mDataset.getSeriesAt(seriesIndex).getScaleNumber()), 0);
        this.mCharts[seriesIndex].drawSeries(canvas, paint, points, seriesRenderer, yAxisValue, 0, startIndex);
    }

    protected ClickableArea[] clickableAreasForPoints(List<Float> points, List<Double> values, float yAxisValue, int seriesIndex, int startIndex) {
        return this.mCharts[seriesIndex].clickableAreasForPoints(points, values, yAxisValue, 0, startIndex);
    }

    protected void drawSeries(XYSeries series, Canvas canvas, Paint paint, List<Float> pointsList, SimpleSeriesRenderer seriesRenderer, float yAxisValue, int seriesIndex, Orientation or, int startIndex) {
        this.mCharts[seriesIndex].setScreenR(getScreenR());
        this.mCharts[seriesIndex].setCalcRange(getCalcRange(this.mDataset.getSeriesAt(seriesIndex).getScaleNumber()), 0);
        this.mCharts[seriesIndex].drawSeries(series, canvas, paint, pointsList, seriesRenderer, yAxisValue, 0, or, startIndex);
    }

    public int getLegendShapeWidth(int seriesIndex) {
        return this.mCharts[seriesIndex].getLegendShapeWidth(0);
    }

    public void drawLegendShape(Canvas canvas, SimpleSeriesRenderer renderer, float x, float y, int seriesIndex, Paint paint) {
        this.mCharts[seriesIndex].drawLegendShape(canvas, renderer, x, y, 0, paint);
    }

    public String getChartType() {
        return "Combined";
    }
}
