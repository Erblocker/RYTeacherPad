package com.touchmenotapps.widget.radialmenu.menu.v1;

import android.graphics.Path;
import android.graphics.RectF;

public class RadialMenuWedge extends Path {
    private float ArcWidth;
    private int InnerSize;
    private int OuterSize;
    private float StartArc;
    private int x;
    private int y;

    protected RadialMenuWedge(int x, int y, int InnerSize, int OuterSize, float StartArc, float ArcWidth) {
        if (StartArc >= 360.0f) {
            StartArc -= 360.0f;
        }
        this.x = x;
        this.y = y;
        this.InnerSize = InnerSize;
        this.OuterSize = OuterSize;
        this.StartArc = StartArc;
        this.ArcWidth = ArcWidth;
        buildPath();
    }

    public int getInnerSize() {
        return this.InnerSize;
    }

    public int getOuterSize() {
        return this.OuterSize;
    }

    public float getArcWidth() {
        return this.ArcWidth;
    }

    protected void buildPath() {
        RectF rect = new RectF();
        RectF rect2 = new RectF();
        rect.set((float) (this.x - this.InnerSize), (float) (this.y - this.InnerSize), (float) (this.x + this.InnerSize), (float) (this.y + this.InnerSize));
        rect2.set((float) (this.x - this.OuterSize), (float) (this.y - this.OuterSize), (float) (this.x + this.OuterSize), (float) (this.y + this.OuterSize));
        reset();
        arcTo(rect2, this.StartArc, this.ArcWidth);
        arcTo(rect, this.StartArc + this.ArcWidth, -this.ArcWidth);
        close();
    }
}
