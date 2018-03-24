package com.netspace.library.utilities;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.view.View;

public class Shadow {
    private static final int END_COLOR = Color.parseColor("#00000000");
    private static final int SHADOW_LENGTH = ((int) (5.0f * Resources.getSystem().getDisplayMetrics().density));
    private static final int START_COLOR = Color.parseColor("#55000000");
    private static int[] colors = new int[]{START_COLOR, END_COLOR};
    private static GradientDrawable linearGradient = new GradientDrawable(Orientation.TOP_BOTTOM, colors);
    private static GradientDrawable radialGradient = new GradientDrawable();

    static {
        radialGradient.setGradientType(1);
        radialGradient.setColors(colors);
        radialGradient.setGradientRadius((float) SHADOW_LENGTH);
    }

    public static void draw(View view, Canvas canvas) {
        int height = view.getHeight();
        int width = view.getWidth();
        linearGradient.setBounds(new Rect(SHADOW_LENGTH, height, width, SHADOW_LENGTH + height));
        linearGradient.setOrientation(Orientation.TOP_BOTTOM);
        linearGradient.draw(canvas);
        linearGradient.setBounds(new Rect(width, SHADOW_LENGTH, SHADOW_LENGTH + width, height));
        linearGradient.setOrientation(Orientation.LEFT_RIGHT);
        linearGradient.draw(canvas);
        radialGradient.setBounds(new Rect(0, height, SHADOW_LENGTH, SHADOW_LENGTH + height));
        radialGradient.setGradientCenter(1.0f, 0.0f);
        radialGradient.draw(canvas);
        radialGradient.setBounds(new Rect(width, height, SHADOW_LENGTH + width, SHADOW_LENGTH + height));
        radialGradient.setGradientCenter(0.0f, 0.0f);
        radialGradient.draw(canvas);
        radialGradient.setBounds(new Rect(width, 0, SHADOW_LENGTH + width, SHADOW_LENGTH));
        radialGradient.setGradientCenter(0.0f, 1.0f);
        radialGradient.draw(canvas);
    }
}
