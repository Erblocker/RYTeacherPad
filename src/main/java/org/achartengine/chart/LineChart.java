package org.achartengine.chart;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import java.util.ArrayList;
import java.util.List;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer.FillOutsideLine;
import org.achartengine.renderer.XYSeriesRenderer.FillOutsideLine.Type;

public class LineChart extends XYChart {
    private static final int SHAPE_WIDTH = 30;
    public static final String TYPE = "Line";
    private ScatterChart pointsChart;

    LineChart() {
    }

    public LineChart(XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer) {
        super(dataset, renderer);
        this.pointsChart = new ScatterChart(dataset, renderer);
    }

    protected void setDatasetRenderer(XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer) {
        super.setDatasetRenderer(dataset, renderer);
        this.pointsChart = new ScatterChart(dataset, renderer);
    }

    public void drawSeries(Canvas canvas, Paint paint, List<Float> points, SimpleSeriesRenderer seriesRenderer, float yAxisValue, int seriesIndex, int startIndex) {
        XYSeriesRenderer renderer = (XYSeriesRenderer) seriesRenderer;
        float lineWidth = paint.getStrokeWidth();
        paint.setStrokeWidth(renderer.getLineWidth());
        for (FillOutsideLine fill : renderer.getFillOutsideLine()) {
            if (fill.getType() != Type.NONE) {
                float referencePoint;
                int i;
                paint.setColor(fill.getColor());
                List<Float> fillPoints = new ArrayList();
                int[] range = fill.getFillRange();
                if (range == null) {
                    fillPoints.addAll(points);
                } else {
                    fillPoints.addAll(points.subList(range[0] * 2, range[1] * 2));
                }
                switch (fill.getType()) {
                    case BOUNDS_ALL:
                        referencePoint = yAxisValue;
                        break;
                    case BOUNDS_BELOW:
                        referencePoint = yAxisValue;
                        break;
                    case BOUNDS_ABOVE:
                        referencePoint = yAxisValue;
                        break;
                    case BELOW:
                        referencePoint = (float) canvas.getHeight();
                        break;
                    case ABOVE:
                        referencePoint = 0.0f;
                        break;
                    default:
                        throw new RuntimeException("You have added a new type of filling but have not implemented.");
                }
                if (fill.getType() == Type.BOUNDS_ABOVE || fill.getType() == Type.BOUNDS_BELOW) {
                    List<Float> boundsPoints = new ArrayList();
                    boolean add = false;
                    if ((fill.getType() == Type.BOUNDS_ABOVE && ((Float) fillPoints.get(1)).floatValue() < referencePoint) || (fill.getType() == Type.BOUNDS_BELOW && ((Float) fillPoints.get(1)).floatValue() > referencePoint)) {
                        boundsPoints.add(fillPoints.get(0));
                        boundsPoints.add(fillPoints.get(1));
                        add = true;
                    }
                    i = 3;
                    while (i < fillPoints.size()) {
                        float prevValue = ((Float) fillPoints.get(i - 2)).floatValue();
                        float value = ((Float) fillPoints.get(i)).floatValue();
                        if ((prevValue < referencePoint && value > referencePoint) || (prevValue > referencePoint && value < referencePoint)) {
                            float prevX = ((Float) fillPoints.get(i - 3)).floatValue();
                            float x = ((Float) fillPoints.get(i - 1)).floatValue();
                            boundsPoints.add(Float.valueOf((((x - prevX) * (referencePoint - prevValue)) / (value - prevValue)) + prevX));
                            boundsPoints.add(Float.valueOf(referencePoint));
                            if ((fill.getType() != Type.BOUNDS_ABOVE || value <= referencePoint) && (fill.getType() != Type.BOUNDS_BELOW || value >= referencePoint)) {
                                boundsPoints.add(Float.valueOf(x));
                                boundsPoints.add(Float.valueOf(value));
                                add = true;
                            } else {
                                i += 2;
                                add = false;
                            }
                        } else if (add || ((fill.getType() == Type.BOUNDS_ABOVE && value < referencePoint) || (fill.getType() == Type.BOUNDS_BELOW && value > referencePoint))) {
                            boundsPoints.add(fillPoints.get(i - 1));
                            boundsPoints.add(Float.valueOf(value));
                        }
                        i += 2;
                    }
                    fillPoints.clear();
                    fillPoints.addAll(boundsPoints);
                }
                int length = fillPoints.size();
                fillPoints.set(0, Float.valueOf(((Float) fillPoints.get(0)).floatValue() + 1.0f));
                fillPoints.add(fillPoints.get(length - 2));
                fillPoints.add(Float.valueOf(referencePoint));
                fillPoints.add(fillPoints.get(0));
                fillPoints.add(fillPoints.get(length + 1));
                for (i = 0; i < length + 4; i += 2) {
                    if (((Float) fillPoints.get(i + 1)).floatValue() < 0.0f) {
                        fillPoints.set(i + 1, Float.valueOf(0.0f));
                    }
                }
                paint.setStyle(Style.FILL);
                drawPath(canvas, (List) fillPoints, paint, true);
            }
        }
        paint.setColor(seriesRenderer.getColor());
        paint.setStyle(Style.STROKE);
        drawPath(canvas, (List) points, paint, false);
        paint.setStrokeWidth(lineWidth);
    }

    protected ClickableArea[] clickableAreasForPoints(List<Float> points, List<Double> values, float yAxisValue, int seriesIndex, int startIndex) {
        int length = points.size();
        ClickableArea[] ret = new ClickableArea[(length / 2)];
        for (int i = 0; i < length; i += 2) {
            int selectableBuffer = this.mRenderer.getSelectableBuffer();
            ret[i / 2] = new ClickableArea(new RectF(((Float) points.get(i)).floatValue() - ((float) selectableBuffer), ((Float) points.get(i + 1)).floatValue() - ((float) selectableBuffer), ((float) selectableBuffer) + ((Float) points.get(i)).floatValue(), ((Float) points.get(i + 1)).floatValue() + ((float) selectableBuffer)), ((Double) values.get(i)).doubleValue(), ((Double) values.get(i + 1)).doubleValue());
        }
        return ret;
    }

    public int getLegendShapeWidth(int seriesIndex) {
        return 30;
    }

    public void drawLegendShape(Canvas canvas, SimpleSeriesRenderer renderer, float x, float y, int seriesIndex, Paint paint) {
        canvas.drawLine(x, y, x + 30.0f, y, paint);
        if (isRenderPoints(renderer)) {
            this.pointsChart.drawLegendShape(canvas, renderer, x + 5.0f, y, seriesIndex, paint);
        }
    }

    public boolean isRenderPoints(SimpleSeriesRenderer renderer) {
        return ((XYSeriesRenderer) renderer).getPointStyle() != PointStyle.POINT;
    }

    public ScatterChart getPointsChart() {
        return this.pointsChart;
    }

    public String getChartType() {
        return "Line";
    }
}
