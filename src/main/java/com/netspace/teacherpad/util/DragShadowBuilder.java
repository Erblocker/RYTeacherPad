package com.netspace.teacherpad.util;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import org.achartengine.renderer.DefaultRenderer;

public class DragShadowBuilder extends android.view.View.DragShadowBuilder {
    private static Drawable shadow;

    public DragShadowBuilder(View v) {
        super(v);
        shadow = new ColorDrawable(DefaultRenderer.TEXT_COLOR);
    }

    public void onProvideShadowMetrics(Point size, Point touch) {
        int width = getView().getWidth() / 2;
        int height = getView().getHeight() / 2;
        shadow.setBounds(0, 0, width, height);
        size.set(width, height);
        touch.set(width / 2, height / 2);
    }

    public void onDrawShadow(Canvas canvas) {
        shadow.draw(canvas);
    }
}
