package org.achartengine.chart;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import java.util.List;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

public class RangeBarChart extends BarChart {
    public static final String TYPE = "RangeBar";

    RangeBarChart() {
    }

    RangeBarChart(Type type) {
        super(type);
    }

    public RangeBarChart(XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer, Type type) {
        super(dataset, renderer, type);
    }

    public void drawSeries(Canvas canvas, Paint paint, List<Float> points, SimpleSeriesRenderer seriesRenderer, float yAxisValue, int seriesIndex, int startIndex) {
        int seriesNr = this.mDataset.getSeriesCount();
        int length = points.size();
        paint.setColor(seriesRenderer.getColor());
        paint.setStyle(Style.FILL);
        float halfDiffX = getHalfDiffX(points, length, seriesNr);
        int start = 0;
        if (startIndex > 0) {
            start = 2;
        }
        for (int i = start; i < length; i += 4) {
            if (points.size() > i + 3) {
                drawBar(canvas, ((Float) points.get(i)).floatValue(), ((Float) points.get(i + 1)).floatValue(), ((Float) points.get(i + 2)).floatValue(), ((Float) points.get(i + 3)).floatValue(), halfDiffX, seriesNr, seriesIndex, paint);
            }
        }
        paint.setColor(seriesRenderer.getColor());
    }

    protected void drawChartValuesText(Canvas canvas, XYSeries series, SimpleSeriesRenderer renderer, Paint paint, List<Float> points, int seriesIndex, int startIndex) {
        int seriesNr = this.mDataset.getSeriesCount();
        float halfDiffX = getHalfDiffX(points, points.size(), seriesNr);
        int start = 0;
        if (startIndex > 0) {
            start = 2;
        }
        int i = start;
        while (i < points.size()) {
            int index = startIndex + (i / 2);
            float x = ((Float) points.get(i)).floatValue();
            if (this.mType == Type.DEFAULT) {
                x += (((float) (seriesIndex * 2)) * halfDiffX) - ((((float) seriesNr) - 1.5f) * halfDiffX);
            }
            if (!isNullValue(series.getY(index + 1)) && points.size() > i + 3) {
                drawText(canvas, getLabel(renderer.getChartValuesFormat(), series.getY(index + 1)), x, ((Float) points.get(i + 3)).floatValue() - renderer.getChartValuesSpacing(), paint, 0.0f);
            }
            if (!isNullValue(series.getY(index)) && points.size() > i + 1) {
                drawText(canvas, getLabel(renderer.getChartValuesFormat(), series.getY(index)), x, ((((Float) points.get(i + 1)).floatValue() + renderer.getChartValuesTextSize()) + renderer.getChartValuesSpacing()) - 3.0f, paint, 0.0f);
            }
            i += 4;
        }
    }

    protected float getCoeficient() {
        return 0.5f;
    }

    public String getChartType() {
        return TYPE;
    }
}
