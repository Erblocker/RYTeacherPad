package org.achartengine.chart;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import java.util.ArrayList;
import java.util.List;
import org.achartengine.model.MultipleCategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

public class DoughnutChart extends RoundChart {
    private MultipleCategorySeries mDataset;
    private int mStep;

    public DoughnutChart(MultipleCategorySeries dataset, DefaultRenderer renderer) {
        super(null, renderer);
        this.mDataset = dataset;
    }

    public void draw(Canvas canvas, int x, int y, int width, int height, Paint paint) {
        int category;
        paint.setAntiAlias(this.mRenderer.isAntialiasing());
        paint.setStyle(Style.FILL);
        paint.setTextSize(this.mRenderer.getLabelsTextSize());
        int legendSize = getLegendSize(this.mRenderer, height / 5, 0.0f);
        int left = x;
        int top = y;
        int right = x + width;
        int cLength = this.mDataset.getCategoriesCount();
        String[] categories = new String[cLength];
        for (category = 0; category < cLength; category++) {
            categories[category] = this.mDataset.getCategory(category);
        }
        if (this.mRenderer.isFitLegend()) {
            legendSize = drawLegend(canvas, this.mRenderer, categories, left, right, y, width, height, legendSize, paint, true);
        }
        int bottom = (y + height) - legendSize;
        drawBackground(this.mRenderer, canvas, x, y, width, height, paint, false, 0);
        this.mStep = 7;
        int mRadius = Math.min(Math.abs(right - left), Math.abs(bottom - top));
        double decCoef = 0.2d / ((double) cLength);
        int radius = (int) (((double) mRadius) * (0.35d * ((double) this.mRenderer.getScale())));
        if (this.mCenterX == Integer.MAX_VALUE) {
            this.mCenterX = (left + right) / 2;
        }
        if (this.mCenterY == Integer.MAX_VALUE) {
            this.mCenterY = (bottom + top) / 2;
        }
        float shortRadius = ((float) radius) * 0.9f;
        float longRadius = ((float) radius) * 1.1f;
        List<RectF> prevLabelsBounds = new ArrayList();
        for (category = 0; category < cLength; category++) {
            int i;
            int sLength = this.mDataset.getItemCount(category);
            double total = 0.0d;
            String[] titles = new String[sLength];
            for (i = 0; i < sLength; i++) {
                total += this.mDataset.getValues(category)[i];
                titles[i] = this.mDataset.getTitles(category)[i];
            }
            float currentAngle = this.mRenderer.getStartAngle();
            RectF oval = new RectF((float) (this.mCenterX - radius), (float) (this.mCenterY - radius), (float) (this.mCenterX + radius), (float) (this.mCenterY + radius));
            for (i = 0; i < sLength; i++) {
                paint.setColor(this.mRenderer.getSeriesRendererAt(i).getColor());
                float angle = (float) ((((double) ((float) this.mDataset.getValues(category)[i])) / total) * 360.0d);
                canvas.drawArc(oval, currentAngle, angle, true, paint);
                drawLabel(canvas, this.mDataset.getTitles(category)[i], this.mRenderer, prevLabelsBounds, this.mCenterX, this.mCenterY, shortRadius, longRadius, currentAngle, angle, left, right, this.mRenderer.getLabelsColor(), paint, true, false);
                currentAngle += angle;
            }
            radius = (int) (((double) radius) - (((double) mRadius) * decCoef));
            shortRadius = (float) (((double) shortRadius) - ((((double) mRadius) * decCoef) - 2.0d));
            if (this.mRenderer.getBackgroundColor() != 0) {
                paint.setColor(this.mRenderer.getBackgroundColor());
            } else {
                paint.setColor(-1);
            }
            paint.setStyle(Style.FILL);
            canvas.drawArc(new RectF((float) (this.mCenterX - radius), (float) (this.mCenterY - radius), (float) (this.mCenterX + radius), (float) (this.mCenterY + radius)), 0.0f, 360.0f, true, paint);
            radius--;
        }
        prevLabelsBounds.clear();
        drawLegend(canvas, this.mRenderer, categories, left, right, y, width, height, legendSize, paint, false);
        drawTitle(canvas, x, y, width, paint);
    }

    public int getLegendShapeWidth(int seriesIndex) {
        return 10;
    }

    public void drawLegendShape(Canvas canvas, SimpleSeriesRenderer renderer, float x, float y, int seriesIndex, Paint paint) {
        this.mStep--;
        canvas.drawCircle((10.0f + x) - ((float) this.mStep), y, (float) this.mStep, paint);
    }
}
