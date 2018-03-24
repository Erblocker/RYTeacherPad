package net.sourceforge.opencamera;

import android.graphics.Matrix;
import android.media.MediaRecorder;
import android.view.View;

interface CameraSurface {
    View getView();

    void setPreviewDisplay(CameraController cameraController);

    void setTransform(Matrix matrix);

    void setVideoRecorder(MediaRecorder mediaRecorder);
}
