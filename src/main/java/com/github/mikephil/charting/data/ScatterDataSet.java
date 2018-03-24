package com.github.mikephil.charting.data;

import android.graphics.Path;
import com.github.mikephil.charting.charts.ScatterChart.ScatterShape;
import com.github.mikephil.charting.utils.Utils;
import java.util.ArrayList;
import java.util.List;

public class ScatterDataSet extends LineScatterCandleRadarDataSet<Entry> {
    private Path mCustomScatterPath = null;
    private ScatterShape mScatterShape = ScatterShape.SQUARE;
    private float mShapeSize = 15.0f;

    public ScatterDataSet(List<Entry> yVals, String label) {
        super(yVals, label);
    }

    public DataSet<Entry> copy() {
        List<Entry> yVals = new ArrayList();
        for (int i = 0; i < this.mYVals.size(); i++) {
            yVals.add(((Entry) this.mYVals.get(i)).copy());
        }
        ScatterDataSet copied = new ScatterDataSet(yVals, getLabel());
        copied.mColors = this.mColors;
        copied.mShapeSize = this.mShapeSize;
        copied.mScatterShape = this.mScatterShape;
        copied.mCustomScatterPath = this.mCustomScatterPath;
        copied.mHighLightColor = this.mHighLightColor;
        return copied;
    }

    public void setScatterShapeSize(float size) {
        this.mShapeSize = Utils.convertDpToPixel(size);
    }

    public float getScatterShapeSize() {
        return this.mShapeSize;
    }

    public void setScatterShape(ScatterShape shape) {
        this.mScatterShape = shape;
    }

    public ScatterShape getScatterShape() {
        return this.mScatterShape;
    }

    public void setCustomScatterShape(Path shape) {
        this.mCustomScatterPath = shape;
    }

    public Path getCustomScatterShape() {
        return this.mCustomScatterPath;
    }
}
