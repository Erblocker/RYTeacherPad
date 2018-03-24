package net.sourceforge.opencamera;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

public class CanvasView extends View {
    private static final String TAG = "CanvasView";
    private int[] measure_spec = new int[2];
    private Preview preview = null;

    CanvasView(Context context, Bundle savedInstanceState, Preview preview) {
        super(context);
        this.preview = preview;
        final Handler handler = new Handler();
        new Runnable() {
            public void run() {
                CanvasView.this.invalidate();
                handler.postDelayed(this, 100);
            }
        }.run();
    }

    public void onDraw(Canvas canvas) {
        this.preview.draw(canvas);
    }

    protected void onMeasure(int widthSpec, int heightSpec) {
        this.preview.getMeasureSpec(this.measure_spec, widthSpec, heightSpec);
        super.onMeasure(this.measure_spec[0], this.measure_spec[1]);
    }
}
