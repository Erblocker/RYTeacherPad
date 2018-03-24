package net.sourceforge.opencamera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import java.io.IOException;

public class MySurfaceView extends SurfaceView implements CameraSurface {
    private static final String TAG = "MySurfaceView";
    private int[] measure_spec = new int[2];
    private Preview preview = null;

    MySurfaceView(Context context, Bundle savedInstanceState, Preview preview) {
        super(context);
        this.preview = preview;
        getHolder().addCallback(preview);
        getHolder().setType(3);
    }

    public View getView() {
        return this;
    }

    public void setPreviewDisplay(CameraController camera_controller) {
        try {
            camera_controller.setPreviewDisplay(getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setVideoRecorder(MediaRecorder video_recorder) {
        video_recorder.setPreviewDisplay(getHolder().getSurface());
    }

    @SuppressLint({"ClickableViewAccessibility"})
    public boolean onTouchEvent(MotionEvent event) {
        return this.preview.touchEvent(event);
    }

    public void onDraw(Canvas canvas) {
        this.preview.draw(canvas);
    }

    protected void onMeasure(int widthSpec, int heightSpec) {
        this.preview.getMeasureSpec(this.measure_spec, widthSpec, heightSpec);
        super.onMeasure(this.measure_spec[0], this.measure_spec[1]);
    }

    public void setTransform(Matrix matrix) {
        throw new RuntimeException();
    }
}
