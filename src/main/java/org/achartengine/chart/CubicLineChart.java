package org.achartengine.chart;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import java.util.List;
import org.achartengine.model.Point;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

public class CubicLineChart extends LineChart {
    public static final String TYPE = "Cubic";
    private float firstMultiplier;
    private Point p1;
    private Point p2;
    private Point p3;
    private float secondMultiplier;

    public CubicLineChart() {
        this.p1 = new Point();
        this.p2 = new Point();
        this.p3 = new Point();
        this.firstMultiplier = 0.33f;
        this.secondMultiplier = 1.0f - this.firstMultiplier;
    }

    public CubicLineChart(XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer, float smoothness) {
        super(dataset, renderer);
        this.p1 = new Point();
        this.p2 = new Point();
        this.p3 = new Point();
        this.firstMultiplier = smoothness;
        this.secondMultiplier = 1.0f - this.firstMultiplier;
    }

    protected void drawPath(Canvas canvas, List<Float> points, Paint paint, boolean circular) {
        int i;
        Path p = new Path();
        p.moveTo(((Float) points.get(0)).floatValue(), ((Float) points.get(1)).floatValue());
        int length = points.size();
        if (circular) {
            length -= 4;
        }
        for (i = 0; i < length; i += 2) {
            int nextIndex;
            int nextNextIndex;
            if (i + 2 < length) {
                nextIndex = i + 2;
            } else {
                nextIndex = i;
            }
            if (i + 4 < length) {
                nextNextIndex = i + 4;
            } else {
                nextNextIndex = nextIndex;
            }
            calc(points, this.p1, i, nextIndex, this.secondMultiplier);
            this.p2.setX(((Float) points.get(nextIndex)).floatValue());
            this.p2.setY(((Float) points.get(nextIndex + 1)).floatValue());
            calc(points, this.p3, nextIndex, nextNextIndex, this.firstMultiplier);
            p.cubicTo(this.p1.getX(), this.p1.getY(), this.p2.getX(), this.p2.getY(), this.p3.getX(), this.p3.getY());
        }
        if (circular) {
            for (i = length; i < length + 4; i += 2) {
                Path path = p;
                path.lineTo(((Float) points.get(i)).floatValue(), ((Float) points.get(i + 1)).floatValue());
            }
            p.lineTo(((Float) points.get(0)).floatValue(), ((Float) points.get(1)).floatValue());
        }
        canvas.drawPath(p, paint);
    }

    private void calc(List<Float> points, Point result, int index1, int index2, float multiplier) {
        float p1x = ((Float) points.get(index1)).floatValue();
        float p1y = ((Float) points.get(index1 + 1)).floatValue();
        float diffY = ((Float) points.get(index2 + 1)).floatValue() - p1y;
        result.setX(((((Float) points.get(index2)).floatValue() - p1x) * multiplier) + p1x);
        result.setY((diffY * multiplier) + p1y);
    }

    public String getChartType() {
        return TYPE;
    }
}
