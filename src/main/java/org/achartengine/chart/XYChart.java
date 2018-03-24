package org.achartengine.chart;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.achartengine.model.Point;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.BasicStroke;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;
import org.achartengine.util.MathHelper;
import wei.mark.standout.StandOutWindow.StandOutLayoutParams;

public abstract class XYChart extends AbstractChart {
    private Map<Integer, List<ClickableArea>> clickableAreas = new HashMap();
    private final Map<Integer, double[]> mCalcRange = new HashMap();
    private Point mCenter;
    protected XYMultipleSeriesDataset mDataset;
    protected XYMultipleSeriesRenderer mRenderer;
    private float mScale;
    private Rect mScreenR;
    private float mTranslate;

    protected abstract ClickableArea[] clickableAreasForPoints(List<Float> list, List<Double> list2, float f, int i, int i2);

    public abstract void drawSeries(Canvas canvas, Paint paint, List<Float> list, SimpleSeriesRenderer simpleSeriesRenderer, float f, int i, int i2);

    public abstract String getChartType();

    protected XYChart() {
    }

    public XYChart(XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer) {
        this.mDataset = dataset;
        this.mRenderer = renderer;
    }

    protected void setDatasetRenderer(XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer) {
        this.mDataset = dataset;
        this.mRenderer = renderer;
    }

    public void draw(Canvas canvas, int x, int y, int width, int height, Paint paint) {
        int i;
        paint.setAntiAlias(this.mRenderer.isAntialiasing());
        int legendSize = getLegendSize(this.mRenderer, height / 5, this.mRenderer.getAxisTitleTextSize());
        int[] margins = this.mRenderer.getMargins();
        int left = x + margins[1];
        int top = y + margins[0];
        int right = (x + width) - margins[3];
        int sLength = this.mDataset.getSeriesCount();
        String[] titles = new String[sLength];
        for (i = 0; i < sLength; i++) {
            titles[i] = this.mDataset.getSeriesAt(i).getTitle();
        }
        if (this.mRenderer.isFitLegend() && this.mRenderer.isShowLegend()) {
            legendSize = drawLegend(canvas, this.mRenderer, titles, left, right, y, width, height, legendSize, paint, true);
        }
        int bottom = ((y + height) - margins[2]) - legendSize;
        if (this.mScreenR == null) {
            this.mScreenR = new Rect();
        }
        this.mScreenR.set(left, top, right, bottom);
        drawBackground(this.mRenderer, canvas, x, y, width, height, paint, false, 0);
        if (paint.getTypeface() == null || !((this.mRenderer.getTextTypeface() == null || !paint.getTypeface().equals(this.mRenderer.getTextTypeface())) && paint.getTypeface().toString().equals(this.mRenderer.getTextTypefaceName()) && paint.getTypeface().getStyle() == this.mRenderer.getTextTypefaceStyle())) {
            if (this.mRenderer.getTextTypeface() != null) {
                paint.setTypeface(this.mRenderer.getTextTypeface());
            } else {
                paint.setTypeface(Typeface.create(this.mRenderer.getTextTypefaceName(), this.mRenderer.getTextTypefaceStyle()));
            }
        }
        Orientation or = this.mRenderer.getOrientation();
        if (or == Orientation.VERTICAL) {
            right -= legendSize;
            bottom += legendSize - 20;
        }
        int angle = or.getAngle();
        boolean rotate = angle == 90;
        this.mScale = ((float) height) / ((float) width);
        this.mTranslate = (float) (Math.abs(width - height) / 2);
        if (this.mScale < 1.0f) {
            this.mTranslate *= -1.0f;
        }
        this.mCenter = new Point((float) ((x + width) / 2), (float) ((y + height) / 2));
        if (rotate) {
            transform(canvas, (float) angle, false);
        }
        int maxScaleNumber = StandOutLayoutParams.AUTO_POSITION;
        for (i = 0; i < sLength; i++) {
            maxScaleNumber = Math.max(maxScaleNumber, this.mDataset.getSeriesAt(i).getScaleNumber());
        }
        maxScaleNumber++;
        if (maxScaleNumber >= 0) {
            XYSeries series;
            int scale;
            double[] minX = new double[maxScaleNumber];
            double[] maxX = new double[maxScaleNumber];
            double[] minY = new double[maxScaleNumber];
            double[] maxY = new double[maxScaleNumber];
            boolean[] isMinXSet = new boolean[maxScaleNumber];
            boolean[] isMaxXSet = new boolean[maxScaleNumber];
            boolean[] isMinYSet = new boolean[maxScaleNumber];
            boolean[] isMaxYSet = new boolean[maxScaleNumber];
            for (i = 0; i < maxScaleNumber; i++) {
                minX[i] = this.mRenderer.getXAxisMin(i);
                maxX[i] = this.mRenderer.getXAxisMax(i);
                minY[i] = this.mRenderer.getYAxisMin(i);
                maxY[i] = this.mRenderer.getYAxisMax(i);
                isMinXSet[i] = this.mRenderer.isMinXSet(i);
                isMaxXSet[i] = this.mRenderer.isMaxXSet(i);
                isMinYSet[i] = this.mRenderer.isMinYSet(i);
                isMaxYSet[i] = this.mRenderer.isMaxYSet(i);
                if (this.mCalcRange.get(Integer.valueOf(i)) == null) {
                    this.mCalcRange.put(Integer.valueOf(i), new double[4]);
                }
            }
            double[] xPixelsPerUnit = new double[maxScaleNumber];
            double[] yPixelsPerUnit = new double[maxScaleNumber];
            for (i = 0; i < sLength; i++) {
                series = this.mDataset.getSeriesAt(i);
                scale = series.getScaleNumber();
                if (series.getItemCount() != 0) {
                    if (!isMinXSet[scale]) {
                        minX[scale] = Math.min(minX[scale], series.getMinX());
                        ((double[]) this.mCalcRange.get(Integer.valueOf(scale)))[0] = minX[scale];
                    }
                    if (!isMaxXSet[scale]) {
                        maxX[scale] = Math.max(maxX[scale], series.getMaxX());
                        ((double[]) this.mCalcRange.get(Integer.valueOf(scale)))[1] = maxX[scale];
                    }
                    if (!isMinYSet[scale]) {
                        minY[scale] = Math.min(minY[scale], (double) ((float) series.getMinY()));
                        ((double[]) this.mCalcRange.get(Integer.valueOf(scale)))[2] = minY[scale];
                    }
                    if (!isMaxYSet[scale]) {
                        maxY[scale] = Math.max(maxY[scale], (double) ((float) series.getMaxY()));
                        ((double[]) this.mCalcRange.get(Integer.valueOf(scale)))[3] = maxY[scale];
                    }
                }
            }
            for (i = 0; i < maxScaleNumber; i++) {
                if (maxX[i] - minX[i] != 0.0d) {
                    xPixelsPerUnit[i] = ((double) (right - left)) / (maxX[i] - minX[i]);
                }
                if (maxY[i] - minY[i] != 0.0d) {
                    yPixelsPerUnit[i] = (double) ((float) (((double) (bottom - top)) / (maxY[i] - minY[i])));
                }
            }
            boolean hasValues = false;
            this.clickableAreas = new HashMap();
            for (i = 0; i < sLength; i++) {
                series = this.mDataset.getSeriesAt(i);
                scale = series.getScaleNumber();
                if (series.getItemCount() != 0) {
                    hasValues = true;
                    SimpleSeriesRenderer seriesRenderer = this.mRenderer.getSeriesRendererAt(i);
                    List<Float> points = new ArrayList();
                    List<Double> values = new ArrayList();
                    float yAxisValue = Math.min((float) bottom, (float) (((double) bottom) + (yPixelsPerUnit[scale] * minY[scale])));
                    LinkedList<ClickableArea> clickableArea = new LinkedList();
                    this.clickableAreas.put(Integer.valueOf(i), clickableArea);
                    synchronized (series) {
                        int startIndex = -1;
                        for (Entry<Double, Double> value : series.getRange(minX[scale], maxX[scale], seriesRenderer.isDisplayBoundingPoints()).entrySet()) {
                            double xValue = ((Double) value.getKey()).doubleValue();
                            double yValue = ((Double) value.getValue()).doubleValue();
                            if (startIndex < 0 && (!isNullValue(yValue) || isRenderNullValues())) {
                                startIndex = series.getIndexForKey(xValue);
                            }
                            values.add(value.getKey());
                            values.add(value.getValue());
                            if (!isNullValue(yValue)) {
                                points.add(Float.valueOf((float) (((double) left) + (xPixelsPerUnit[scale] * (xValue - minX[scale])))));
                                points.add(Float.valueOf((float) (((double) bottom) - (yPixelsPerUnit[scale] * (yValue - minY[scale])))));
                            } else if (isRenderNullValues()) {
                                points.add(Float.valueOf((float) (((double) left) + (xPixelsPerUnit[scale] * (xValue - minX[scale])))));
                                points.add(Float.valueOf((float) (((double) bottom) - (yPixelsPerUnit[scale] * (-minY[scale])))));
                            } else {
                                if (points.size() > 0) {
                                    drawSeries(series, canvas, paint, points, seriesRenderer, yAxisValue, i, or, startIndex);
                                    clickableArea.addAll(Arrays.asList(clickableAreasForPoints(points, values, yAxisValue, i, startIndex)));
                                    points.clear();
                                    values.clear();
                                    startIndex = -1;
                                }
                                clickableArea.add(null);
                            }
                        }
                        int count = series.getAnnotationCount();
                        if (count > 0) {
                            paint.setColor(this.mRenderer.getLabelsColor());
                            Rect bound = new Rect();
                            for (int j = 0; j < count; j++) {
                                float xS = (float) (((double) left) + (xPixelsPerUnit[scale] * (series.getAnnotationX(j) - minX[scale])));
                                float yS = (float) (((double) bottom) - (yPixelsPerUnit[scale] * (series.getAnnotationY(j) - minY[scale])));
                                paint.getTextBounds(series.getAnnotationAt(j), 0, series.getAnnotationAt(j).length(), bound);
                                if (xS < ((float) bound.width()) + xS && yS < ((float) canvas.getHeight())) {
                                    drawString(canvas, series.getAnnotationAt(j), xS, yS, paint);
                                }
                            }
                        }
                        if (points.size() > 0) {
                            drawSeries(series, canvas, paint, points, seriesRenderer, yAxisValue, i, or, startIndex);
                            clickableArea.addAll(Arrays.asList(clickableAreasForPoints(points, values, yAxisValue, i, startIndex)));
                        }
                    }
                }
            }
            drawBackground(this.mRenderer, canvas, x, bottom, width, height - bottom, paint, true, this.mRenderer.getMarginsColor());
            drawBackground(this.mRenderer, canvas, x, y, width, margins[0], paint, true, this.mRenderer.getMarginsColor());
            if (or == Orientation.HORIZONTAL) {
                drawBackground(this.mRenderer, canvas, x, y, left - x, height - y, paint, true, this.mRenderer.getMarginsColor());
                drawBackground(this.mRenderer, canvas, right, y, margins[3], height - y, paint, true, this.mRenderer.getMarginsColor());
            } else if (or == Orientation.VERTICAL) {
                drawBackground(this.mRenderer, canvas, right, y, width - right, height - y, paint, true, this.mRenderer.getMarginsColor());
                drawBackground(this.mRenderer, canvas, x, y, left - x, height - y, paint, true, this.mRenderer.getMarginsColor());
            }
            boolean showLabels = this.mRenderer.isShowLabels() && hasValues;
            boolean showGridX = this.mRenderer.isShowGridX();
            boolean showCustomTextGrid = this.mRenderer.isShowCustomTextGrid();
            if (showLabels || showGridX) {
                List<Double> xLabels = getValidLabels(getXLabels(minX[0], maxX[0], this.mRenderer.getXLabels()));
                Map<Integer, List<Double>> allYLabels = getYLabels(minY, maxY, maxScaleNumber);
                int xLabelsLeft = left;
                if (showLabels) {
                    paint.setColor(this.mRenderer.getXLabelsColor());
                    paint.setTextSize(this.mRenderer.getLabelsTextSize());
                    paint.setTextAlign(this.mRenderer.getXLabelsAlign());
                }
                drawXLabels(xLabels, this.mRenderer.getXTextLabelLocations(), canvas, paint, xLabelsLeft, top, bottom, xPixelsPerUnit[0], minX[0], maxX[0]);
                drawYLabels(allYLabels, canvas, paint, maxScaleNumber, left, right, bottom, yPixelsPerUnit, minY);
                if (showLabels) {
                    paint.setColor(this.mRenderer.getLabelsColor());
                    i = 0;
                    while (i < maxScaleNumber) {
                        Align axisAlign = this.mRenderer.getYAxisAlign(i);
                        for (Double location : this.mRenderer.getYTextLabelLocations(i)) {
                            if (minY[i] <= location.doubleValue() && location.doubleValue() <= maxY[i]) {
                                float yLabel = (float) (((double) bottom) - (yPixelsPerUnit[i] * (location.doubleValue() - minY[i])));
                                String label = this.mRenderer.getYTextLabel(location, i);
                                paint.setColor(this.mRenderer.getYLabelsColor(i));
                                paint.setTextAlign(this.mRenderer.getYLabelsAlign(i));
                                if (or == Orientation.HORIZONTAL) {
                                    if (axisAlign == Align.LEFT) {
                                        canvas.drawLine((float) (getLabelLinePos(axisAlign) + left), yLabel, (float) left, yLabel, paint);
                                        drawText(canvas, label, (float) left, yLabel - this.mRenderer.getYLabelsVerticalPadding(), paint, this.mRenderer.getYLabelsAngle());
                                    } else {
                                        canvas.drawLine((float) right, yLabel, (float) (getLabelLinePos(axisAlign) + right), yLabel, paint);
                                        drawText(canvas, label, (float) right, yLabel - this.mRenderer.getYLabelsVerticalPadding(), paint, this.mRenderer.getYLabelsAngle());
                                    }
                                    if (showCustomTextGrid) {
                                        paint.setColor(this.mRenderer.getGridColor());
                                        canvas.drawLine((float) left, yLabel, (float) right, yLabel, paint);
                                    }
                                } else {
                                    canvas.drawLine((float) (right - getLabelLinePos(axisAlign)), yLabel, (float) right, yLabel, paint);
                                    drawText(canvas, label, (float) (right + 10), yLabel - this.mRenderer.getYLabelsVerticalPadding(), paint, this.mRenderer.getYLabelsAngle());
                                    if (showCustomTextGrid) {
                                        paint.setColor(this.mRenderer.getGridColor());
                                        canvas.drawLine((float) right, yLabel, (float) left, yLabel, paint);
                                    }
                                }
                            }
                        }
                        i++;
                    }
                }
                if (showLabels) {
                    paint.setColor(this.mRenderer.getLabelsColor());
                    float size = this.mRenderer.getAxisTitleTextSize();
                    paint.setTextSize(size);
                    paint.setTextAlign(Align.CENTER);
                    if (or == Orientation.HORIZONTAL) {
                        drawText(canvas, this.mRenderer.getXTitle(), (float) ((width / 2) + x), ((((float) bottom) + ((this.mRenderer.getLabelsTextSize() * 4.0f) / 3.0f)) + this.mRenderer.getXLabelsPadding()) + size, paint, 0.0f);
                        for (i = 0; i < maxScaleNumber; i++) {
                            if (this.mRenderer.getYAxisAlign(i) == Align.LEFT) {
                                drawText(canvas, this.mRenderer.getYTitle(i), ((float) x) + size, (float) ((height / 2) + y), paint, -90.0f);
                            } else {
                                drawText(canvas, this.mRenderer.getYTitle(i), (float) (x + width), (float) ((height / 2) + y), paint, -90.0f);
                            }
                        }
                        paint.setTextSize(this.mRenderer.getChartTitleTextSize());
                        drawText(canvas, this.mRenderer.getChartTitle(), (float) ((width / 2) + x), ((float) y) + this.mRenderer.getChartTitleTextSize(), paint, 0.0f);
                    } else if (or == Orientation.VERTICAL) {
                        drawText(canvas, this.mRenderer.getXTitle(), (float) ((width / 2) + x), (((float) (y + height)) - size) + this.mRenderer.getXLabelsPadding(), paint, -90.0f);
                        drawText(canvas, this.mRenderer.getYTitle(), (float) (right + 20), (float) ((height / 2) + y), paint, 0.0f);
                        paint.setTextSize(this.mRenderer.getChartTitleTextSize());
                        drawText(canvas, this.mRenderer.getChartTitle(), ((float) x) + size, (float) ((height / 2) + top), paint, 0.0f);
                    }
                }
            }
            if (or == Orientation.HORIZONTAL) {
                drawLegend(canvas, this.mRenderer, titles, left, right, y + ((int) this.mRenderer.getXLabelsPadding()), width, height, legendSize, paint, false);
            } else if (or == Orientation.VERTICAL) {
                transform(canvas, (float) angle, true);
                drawLegend(canvas, this.mRenderer, titles, left, right, y + ((int) this.mRenderer.getXLabelsPadding()), width, height, legendSize, paint, false);
                transform(canvas, (float) angle, false);
            }
            if (this.mRenderer.isShowAxes()) {
                paint.setColor(this.mRenderer.getAxesColor());
                canvas.drawLine((float) left, (float) bottom, (float) right, (float) bottom, paint);
                boolean rightAxis = false;
                for (i = 0; i < maxScaleNumber && !rightAxis; i++) {
                    rightAxis = this.mRenderer.getYAxisAlign(i) == Align.RIGHT;
                }
                if (or == Orientation.HORIZONTAL) {
                    canvas.drawLine((float) left, (float) top, (float) left, (float) bottom, paint);
                    if (rightAxis) {
                        canvas.drawLine((float) right, (float) top, (float) right, (float) bottom, paint);
                    }
                } else if (or == Orientation.VERTICAL) {
                    canvas.drawLine((float) right, (float) top, (float) right, (float) bottom, paint);
                }
            }
            if (rotate) {
                transform(canvas, (float) angle, true);
            }
        }
    }

    protected List<Double> getXLabels(double min, double max, int count) {
        return MathHelper.getLabels(min, max, count);
    }

    protected Map<Integer, List<Double>> getYLabels(double[] minY, double[] maxY, int maxScaleNumber) {
        Map<Integer, List<Double>> allYLabels = new HashMap();
        for (int i = 0; i < maxScaleNumber; i++) {
            allYLabels.put(Integer.valueOf(i), getValidLabels(MathHelper.getLabels(minY[i], maxY[i], this.mRenderer.getYLabels())));
        }
        return allYLabels;
    }

    protected Rect getScreenR() {
        return this.mScreenR;
    }

    protected void setScreenR(Rect screenR) {
        this.mScreenR = screenR;
    }

    private List<Double> getValidLabels(List<Double> labels) {
        List<Double> result = new ArrayList(labels);
        for (Double label : labels) {
            if (label.isNaN()) {
                result.remove(label);
            }
        }
        return result;
    }

    protected void drawSeries(XYSeries series, Canvas canvas, Paint paint, List<Float> pointsList, SimpleSeriesRenderer seriesRenderer, float yAxisValue, int seriesIndex, Orientation or, int startIndex) {
        BasicStroke stroke = seriesRenderer.getStroke();
        Cap cap = paint.getStrokeCap();
        Join join = paint.getStrokeJoin();
        float miter = paint.getStrokeMiter();
        PathEffect pathEffect = paint.getPathEffect();
        Style style = paint.getStyle();
        if (stroke != null) {
            PathEffect effect = null;
            if (stroke.getIntervals() != null) {
                effect = new DashPathEffect(stroke.getIntervals(), stroke.getPhase());
            }
            setStroke(stroke.getCap(), stroke.getJoin(), stroke.getMiter(), Style.FILL_AND_STROKE, effect, paint);
        }
        drawSeries(canvas, paint, pointsList, seriesRenderer, yAxisValue, seriesIndex, startIndex);
        if (isRenderPoints(seriesRenderer)) {
            ScatterChart pointsChart = getPointsChart();
            if (pointsChart != null) {
                pointsChart.drawSeries(canvas, paint, pointsList, seriesRenderer, yAxisValue, seriesIndex, startIndex);
            }
        }
        paint.setTextSize(seriesRenderer.getChartValuesTextSize());
        if (or == Orientation.HORIZONTAL) {
            paint.setTextAlign(Align.CENTER);
        } else {
            paint.setTextAlign(Align.LEFT);
        }
        if (seriesRenderer.isDisplayChartValues()) {
            paint.setTextAlign(seriesRenderer.getChartValuesTextAlign());
            drawChartValuesText(canvas, series, seriesRenderer, paint, pointsList, seriesIndex, startIndex);
        }
        if (stroke != null) {
            setStroke(cap, join, miter, style, pathEffect, paint);
        }
    }

    private void setStroke(Cap cap, Join join, float miter, Style style, PathEffect pathEffect, Paint paint) {
        paint.setStrokeCap(cap);
        paint.setStrokeJoin(join);
        paint.setStrokeMiter(miter);
        paint.setPathEffect(pathEffect);
        paint.setStyle(style);
    }

    protected void drawChartValuesText(Canvas canvas, XYSeries series, SimpleSeriesRenderer renderer, Paint paint, List<Float> points, int seriesIndex, int startIndex) {
        int k;
        if (points.size() > 1) {
            float previousPointX = ((Float) points.get(0)).floatValue();
            float previousPointY = ((Float) points.get(1)).floatValue();
            for (k = 0; k < points.size(); k += 2) {
                if (k == 2) {
                    if (Math.abs(((Float) points.get(2)).floatValue() - ((Float) points.get(0)).floatValue()) > ((float) renderer.getDisplayChartValuesDistance()) || Math.abs(((Float) points.get(3)).floatValue() - ((Float) points.get(1)).floatValue()) > ((float) renderer.getDisplayChartValuesDistance())) {
                        drawText(canvas, getLabel(renderer.getChartValuesFormat(), series.getY(startIndex)), ((Float) points.get(0)).floatValue(), ((Float) points.get(1)).floatValue() - renderer.getChartValuesSpacing(), paint, 0.0f);
                        drawText(canvas, getLabel(renderer.getChartValuesFormat(), series.getY(startIndex + 1)), ((Float) points.get(2)).floatValue(), ((Float) points.get(3)).floatValue() - renderer.getChartValuesSpacing(), paint, 0.0f);
                        previousPointX = ((Float) points.get(2)).floatValue();
                        previousPointY = ((Float) points.get(3)).floatValue();
                    }
                } else if (k > 2) {
                    if (Math.abs(((Float) points.get(k)).floatValue() - previousPointX) <= ((float) renderer.getDisplayChartValuesDistance())) {
                        if (Math.abs(((Float) points.get(k + 1)).floatValue() - previousPointY) <= ((float) renderer.getDisplayChartValuesDistance())) {
                        }
                    }
                    drawText(canvas, getLabel(renderer.getChartValuesFormat(), series.getY((k / 2) + startIndex)), ((Float) points.get(k)).floatValue(), ((Float) points.get(k + 1)).floatValue() - renderer.getChartValuesSpacing(), paint, 0.0f);
                    previousPointX = ((Float) points.get(k)).floatValue();
                    previousPointY = ((Float) points.get(k + 1)).floatValue();
                }
            }
            return;
        }
        for (k = 0; k < points.size(); k += 2) {
            drawText(canvas, getLabel(renderer.getChartValuesFormat(), series.getY((k / 2) + startIndex)), ((Float) points.get(k)).floatValue(), ((Float) points.get(k + 1)).floatValue() - renderer.getChartValuesSpacing(), paint, 0.0f);
        }
    }

    protected void drawText(Canvas canvas, String text, float x, float y, Paint paint, float extraAngle) {
        float angle = ((float) (-this.mRenderer.getOrientation().getAngle())) + extraAngle;
        if (angle != 0.0f) {
            canvas.rotate(angle, x, y);
        }
        drawString(canvas, text, x, y, paint);
        if (angle != 0.0f) {
            canvas.rotate(-angle, x, y);
        }
    }

    private void transform(Canvas canvas, float angle, boolean inverse) {
        if (inverse) {
            canvas.scale(1.0f / this.mScale, this.mScale);
            canvas.translate(this.mTranslate, -this.mTranslate);
            canvas.rotate(-angle, this.mCenter.getX(), this.mCenter.getY());
            return;
        }
        canvas.rotate(angle, this.mCenter.getX(), this.mCenter.getY());
        canvas.translate(-this.mTranslate, this.mTranslate);
        canvas.scale(this.mScale, 1.0f / this.mScale);
    }

    protected void drawXLabels(List<Double> xLabels, Double[] xTextLabelLocations, Canvas canvas, Paint paint, int left, int top, int bottom, double xPixelsPerUnit, double minX, double maxX) {
        int length = xLabels.size();
        boolean showLabels = this.mRenderer.isShowLabels();
        boolean showGridY = this.mRenderer.isShowGridY();
        for (int i = 0; i < length; i++) {
            double label = ((Double) xLabels.get(i)).doubleValue();
            float xLabel = (float) (((double) left) + ((label - minX) * xPixelsPerUnit));
            if (showLabels) {
                paint.setColor(this.mRenderer.getXLabelsColor());
                canvas.drawLine(xLabel, (float) bottom, xLabel, ((float) bottom) + (this.mRenderer.getLabelsTextSize() / 3.0f), paint);
                drawText(canvas, getLabel(this.mRenderer.getLabelFormat(), label), xLabel, (((float) bottom) + ((this.mRenderer.getLabelsTextSize() * 4.0f) / 3.0f)) + this.mRenderer.getXLabelsPadding(), paint, this.mRenderer.getXLabelsAngle());
            }
            if (showGridY) {
                paint.setColor(this.mRenderer.getGridColor());
                canvas.drawLine(xLabel, (float) bottom, xLabel, (float) top, paint);
            }
        }
        drawXTextLabels(xTextLabelLocations, canvas, paint, showLabels, left, top, bottom, xPixelsPerUnit, minX, maxX);
    }

    protected void drawYLabels(Map<Integer, List<Double>> allYLabels, Canvas canvas, Paint paint, int maxScaleNumber, int left, int right, int bottom, double[] yPixelsPerUnit, double[] minY) {
        Orientation or = this.mRenderer.getOrientation();
        boolean showGridX = this.mRenderer.isShowGridX();
        boolean showLabels = this.mRenderer.isShowLabels();
        for (int i = 0; i < maxScaleNumber; i++) {
            paint.setTextAlign(this.mRenderer.getYLabelsAlign(i));
            List<Double> yLabels = (List) allYLabels.get(Integer.valueOf(i));
            int length = yLabels.size();
            for (int j = 0; j < length; j++) {
                double label = ((Double) yLabels.get(j)).doubleValue();
                Align axisAlign = this.mRenderer.getYAxisAlign(i);
                boolean textLabel = this.mRenderer.getYTextLabel(Double.valueOf(label), i) != null;
                float yLabel = (float) (((double) bottom) - (yPixelsPerUnit[i] * (label - minY[i])));
                if (or == Orientation.HORIZONTAL) {
                    if (showLabels && !textLabel) {
                        paint.setColor(this.mRenderer.getYLabelsColor(i));
                        if (axisAlign == Align.LEFT) {
                            canvas.drawLine((float) (getLabelLinePos(axisAlign) + left), yLabel, (float) left, yLabel, paint);
                            drawText(canvas, getLabel(this.mRenderer.getLabelFormat(), label), ((float) left) - this.mRenderer.getYLabelsPadding(), yLabel - this.mRenderer.getYLabelsVerticalPadding(), paint, this.mRenderer.getYLabelsAngle());
                        } else {
                            canvas.drawLine((float) right, yLabel, (float) (getLabelLinePos(axisAlign) + right), yLabel, paint);
                            drawText(canvas, getLabel(this.mRenderer.getLabelFormat(), label), ((float) right) + this.mRenderer.getYLabelsPadding(), yLabel - this.mRenderer.getYLabelsVerticalPadding(), paint, this.mRenderer.getYLabelsAngle());
                        }
                    }
                    if (showGridX) {
                        paint.setColor(this.mRenderer.getGridColor());
                        canvas.drawLine((float) left, yLabel, (float) right, yLabel, paint);
                    }
                } else if (or == Orientation.VERTICAL) {
                    if (showLabels && !textLabel) {
                        paint.setColor(this.mRenderer.getYLabelsColor(i));
                        canvas.drawLine((float) (right - getLabelLinePos(axisAlign)), yLabel, (float) right, yLabel, paint);
                        drawText(canvas, getLabel(this.mRenderer.getLabelFormat(), label), ((float) (right + 10)) + this.mRenderer.getYLabelsPadding(), yLabel - this.mRenderer.getYLabelsVerticalPadding(), paint, this.mRenderer.getYLabelsAngle());
                    }
                    if (showGridX) {
                        paint.setColor(this.mRenderer.getGridColor());
                        canvas.drawLine((float) right, yLabel, (float) left, yLabel, paint);
                    }
                }
            }
        }
    }

    protected void drawXTextLabels(Double[] xTextLabelLocations, Canvas canvas, Paint paint, boolean showLabels, int left, int top, int bottom, double xPixelsPerUnit, double minX, double maxX) {
        boolean showCustomTextGrid = this.mRenderer.isShowCustomTextGrid();
        if (showLabels) {
            paint.setColor(this.mRenderer.getXLabelsColor());
            for (Double location : xTextLabelLocations) {
                if (minX <= location.doubleValue() && location.doubleValue() <= maxX) {
                    float xLabel = (float) (((double) left) + ((location.doubleValue() - minX) * xPixelsPerUnit));
                    paint.setColor(this.mRenderer.getXLabelsColor());
                    canvas.drawLine(xLabel, (float) bottom, xLabel, ((float) bottom) + (this.mRenderer.getLabelsTextSize() / 3.0f), paint);
                    drawText(canvas, this.mRenderer.getXTextLabel(location), xLabel, ((float) bottom) + ((this.mRenderer.getLabelsTextSize() * 4.0f) / 3.0f), paint, this.mRenderer.getXLabelsAngle());
                    if (showCustomTextGrid) {
                        paint.setColor(this.mRenderer.getGridColor());
                        canvas.drawLine(xLabel, (float) bottom, xLabel, (float) top, paint);
                    }
                }
            }
        }
    }

    public XYMultipleSeriesRenderer getRenderer() {
        return this.mRenderer;
    }

    public XYMultipleSeriesDataset getDataset() {
        return this.mDataset;
    }

    public double[] getCalcRange(int scale) {
        return (double[]) this.mCalcRange.get(Integer.valueOf(scale));
    }

    public void setCalcRange(double[] range, int scale) {
        this.mCalcRange.put(Integer.valueOf(scale), range);
    }

    public double[] toRealPoint(float screenX, float screenY) {
        return toRealPoint(screenX, screenY, 0);
    }

    public double[] toScreenPoint(double[] realPoint) {
        return toScreenPoint(realPoint, 0);
    }

    private int getLabelLinePos(Align align) {
        if (align == Align.LEFT) {
            return -4;
        }
        return 4;
    }

    public double[] toRealPoint(float screenX, float screenY, int scale) {
        double realMinX = this.mRenderer.getXAxisMin(scale);
        double realMaxX = this.mRenderer.getXAxisMax(scale);
        double realMinY = this.mRenderer.getYAxisMin(scale);
        double realMaxY = this.mRenderer.getYAxisMax(scale);
        if (this.mScreenR != null) {
            return new double[]{((((double) (screenX - ((float) this.mScreenR.left))) * (realMaxX - realMinX)) / ((double) this.mScreenR.width())) + realMinX, ((((double) (((float) (this.mScreenR.top + this.mScreenR.height())) - screenY)) * (realMaxY - realMinY)) / ((double) this.mScreenR.height())) + realMinY};
        }
        return new double[]{(double) screenX, (double) screenY};
    }

    public double[] toScreenPoint(double[] realPoint, int scale) {
        double realMinX = this.mRenderer.getXAxisMin(scale);
        double realMaxX = this.mRenderer.getXAxisMax(scale);
        double realMinY = this.mRenderer.getYAxisMin(scale);
        double realMaxY = this.mRenderer.getYAxisMax(scale);
        if (!(this.mRenderer.isMinXSet(scale) && this.mRenderer.isMaxXSet(scale) && this.mRenderer.isMinXSet(scale) && this.mRenderer.isMaxYSet(scale))) {
            double[] calcRange = getCalcRange(scale);
            realMinX = calcRange[0];
            realMaxX = calcRange[1];
            realMinY = calcRange[2];
            realMaxY = calcRange[3];
        }
        if (this.mScreenR == null) {
            return realPoint;
        }
        return new double[]{(((realPoint[0] - realMinX) * ((double) this.mScreenR.width())) / (realMaxX - realMinX)) + ((double) this.mScreenR.left), (((realMaxY - realPoint[1]) * ((double) this.mScreenR.height())) / (realMaxY - realMinY)) + ((double) this.mScreenR.top)};
    }

    public SeriesSelection getSeriesAndPointForScreenCoordinate(Point screenPoint) {
        if (this.clickableAreas != null) {
            for (int seriesIndex = this.clickableAreas.size() - 1; seriesIndex >= 0; seriesIndex--) {
                int pointIndex = 0;
                if (this.clickableAreas.get(Integer.valueOf(seriesIndex)) != null) {
                    for (ClickableArea area : (List) this.clickableAreas.get(Integer.valueOf(seriesIndex))) {
                        if (area != null) {
                            RectF rectangle = area.getRect();
                            if (rectangle != null && rectangle.contains(screenPoint.getX(), screenPoint.getY())) {
                                return new SeriesSelection(seriesIndex, pointIndex, area.getX(), area.getY());
                            }
                        }
                        pointIndex++;
                    }
                    continue;
                }
            }
        }
        return super.getSeriesAndPointForScreenCoordinate(screenPoint);
    }

    protected boolean isRenderNullValues() {
        return false;
    }

    public boolean isRenderPoints(SimpleSeriesRenderer renderer) {
        return false;
    }

    public double getDefaultMinimum() {
        return Double.MAX_VALUE;
    }

    public ScatterChart getPointsChart() {
        return null;
    }
}
