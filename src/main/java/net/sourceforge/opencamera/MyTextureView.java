package net.sourceforge.opencamera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import java.io.IOException;

class MyTextureView extends TextureView implements CameraSurface {
    private static final String TAG = "MyTextureView";
    private int[] measure_spec = new int[2];
    private Preview preview = null;

    MyTextureView(Context context, Bundle savedInstanceState, Preview preview) {
        super(context);
        this.preview = preview;
        setSurfaceTextureListener(preview);
    }

    public View getView() {
        return this;
    }

    public void setPreviewDisplay(CameraController camera_controller) {
        try {
            camera_controller.setPreviewTexture(getSurfaceTexture());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setVideoRecorder(MediaRecorder video_recorder) {
    }

    @SuppressLint({"ClickableViewAccessibility"})
    public boolean onTouchEvent(MotionEvent event) {
        return this.preview.touchEvent(event);
    }

    protected void onMeasure(int widthSpec, int heightSpec) {
        this.preview.getMeasureSpec(this.measure_spec, widthSpec, heightSpec);
        super.onMeasure(this.measure_spec[0], this.measure_spec[1]);
    }

    public void setTransform(Matrix matrix) {
        super.setTransform(matrix);
    }
}
