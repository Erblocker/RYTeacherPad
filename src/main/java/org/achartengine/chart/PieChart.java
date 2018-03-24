package org.achartengine.chart;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import java.util.ArrayList;
import java.util.List;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.Point;
import org.achartengine.model.SeriesSelection;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

public class PieChart extends RoundChart {
    private PieMapper mPieMapper = new PieMapper();

    public PieChart(CategorySeries dataset, DefaultRenderer renderer) {
        super(dataset, renderer);
    }

    public void draw(Canvas canvas, int x, int y, int width, int height, Paint paint) {
        int i;
        paint.setAntiAlias(this.mRenderer.isAntialiasing());
        paint.setStyle(Style.FILL);
        paint.setTextSize(this.mRenderer.getLabelsTextSize());
        int legendSize = getLegendSize(this.mRenderer, height / 5, 0.0f);
        int left = x;
        int top = y;
        int right = x + width;
        int sLength = this.mDataset.getItemCount();
        double total = 0.0d;
        String[] titles = new String[sLength];
        for (i = 0; i < sLength; i++) {
            total += this.mDataset.getValue(i);
            titles[i] = this.mDataset.getCategory(i);
        }
        if (this.mRenderer.isFitLegend()) {
            legendSize = drawLegend(canvas, this.mRenderer, titles, left, right, y, width, height, legendSize, paint, true);
        }
        int bottom = (y + height) - legendSize;
        drawBackground(this.mRenderer, canvas, x, y, width, height, paint, false, 0);
        float currentAngle = this.mRenderer.getStartAngle();
        int radius = (int) ((((double) Math.min(Math.abs(right - left), Math.abs(bottom - top))) * 0.35d) * ((double) this.mRenderer.getScale()));
        if (this.mCenterX == Integer.MAX_VALUE) {
            this.mCenterX = (left + right) / 2;
        }
        if (this.mCenterY == Integer.MAX_VALUE) {
            this.mCenterY = (bottom + top) / 2;
        }
        this.mPieMapper.setDimensions(radius, this.mCenterX, this.mCenterY);
        boolean loadPieCfg = !this.mPieMapper.areAllSegmentPresent(sLength);
        if (loadPieCfg) {
            this.mPieMapper.clearPieSegments();
        }
        float shortRadius = ((float) radius) * 0.9f;
        float longRadius = ((float) radius) * 1.1f;
        RectF rectF = new RectF((float) (this.mCenterX - radius), (float) (this.mCenterY - radius), (float) (this.mCenterX + radius), (float) (this.mCenterY + radius));
        List<RectF> prevLabelsBounds = new ArrayList();
        for (i = 0; i < sLength; i++) {
            SimpleSeriesRenderer seriesRenderer = this.mRenderer.getSeriesRendererAt(i);
            if (seriesRenderer.isGradientEnabled()) {
                paint.setShader(new RadialGradient((float) this.mCenterX, (float) this.mCenterY, longRadius, seriesRenderer.getGradientStartColor(), seriesRenderer.getGradientStopColor(), TileMode.MIRROR));
            } else {
                paint.setColor(seriesRenderer.getColor());
            }
            float value = (float) this.mDataset.getValue(i);
            float angle = (float) ((((double) value) / total) * 360.0d);
            if (seriesRenderer.isHighlighted()) {
                double rAngle = Math.toRadians((double) (90.0f - ((angle / 2.0f) + currentAngle)));
                float translateX = (float) ((((double) radius) * 0.1d) * Math.sin(rAngle));
                float translateY = (float) ((((double) radius) * 0.1d) * Math.cos(rAngle));
                rectF.offset(translateX, translateY);
                canvas.drawArc(rectF, currentAngle, angle, true, paint);
                rectF.offset(-translateX, -translateY);
            } else {
                canvas.drawArc(rectF, currentAngle, angle, true, paint);
            }
            paint.setColor(seriesRenderer.getColor());
            paint.setShader(null);
            drawLabel(canvas, this.mDataset.getCategory(i), this.mRenderer, prevLabelsBounds, this.mCenterX, this.mCenterY, shortRadius, longRadius, currentAngle, angle, left, right, this.mRenderer.getLabelsColor(), paint, true, false);
            if (this.mRenderer.isDisplayValues()) {
                drawLabel(canvas, getLabel(this.mRenderer.getSeriesRendererAt(i).getChartValuesFormat(), this.mDataset.getValue(i)), this.mRenderer, prevLabelsBounds, this.mCenterX, this.mCenterY, shortRadius / 2.0f, longRadius / 2.0f, currentAngle, angle, left, right, this.mRenderer.getLabelsColor(), paint, false, true);
            }
            if (loadPieCfg) {
                this.mPieMapper.addPieSegment(i, value, currentAngle, angle);
            }
            currentAngle += angle;
        }
        prevLabelsBounds.clear();
        drawLegend(canvas, this.mRenderer, titles, left, right, y, width, height, legendSize, paint, false);
        drawTitle(canvas, x, y, width, paint);
    }

    public SeriesSelection getSeriesAndPointForScreenCoordinate(Point screenPoint) {
        return this.mPieMapper.getSeriesAndPointForScreenCoordinate(screenPoint);
    }
}
