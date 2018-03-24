package net.sourceforge.opencamera;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.location.Location;
import android.media.MediaRecorder;
import android.view.SurfaceHolder;
import java.io.IOException;
import java.util.List;

public abstract class CameraController {
    private static final String TAG = "CameraController";
    public int count_camera_parameters_exception = 0;

    static class Area {
        public Rect rect = null;
        public int weight = 0;

        public Area(Rect rect, int weight) {
            this.rect = rect;
            this.weight = weight;
        }
    }

    interface AutoFocusCallback {
        void onAutoFocus(boolean z);
    }

    static class CameraFeatures {
        boolean can_disable_shutter_sound = false;
        float exposure_step = 0.0f;
        boolean is_exposure_lock_supported = false;
        boolean is_video_stabilization_supported = false;
        boolean is_zoom_supported = false;
        int max_exposure = 0;
        int max_num_focus_areas = 0;
        int max_zoom = 0;
        int min_exposure = 0;
        List<Size> picture_sizes = null;
        List<Size> preview_sizes = null;
        List<String> supported_flash_values = null;
        List<String> supported_focus_values = null;
        boolean supports_face_detection = false;
        List<Size> video_sizes = null;
        List<Integer> zoom_ratios = null;

        CameraFeatures() {
        }
    }

    static class Face {
        public Rect rect = null;
        public int score = 0;

        Face(int score, Rect rect) {
            this.score = score;
            this.rect = rect;
        }
    }

    interface FaceDetectionListener {
        void onFaceDetection(Face[] faceArr);
    }

    interface PictureCallback {
        void onPictureTaken(byte[] bArr);
    }

    public static class Size {
        public int height = 0;
        public int width = 0;

        public Size(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public boolean equals(Size that) {
            return this.width == that.width && this.height == that.height;
        }
    }

    class SupportedValues {
        String selected_value = null;
        List<String> values = null;

        SupportedValues(List<String> values, String selected_value) {
            this.values = values;
            this.selected_value = selected_value;
        }
    }

    abstract void autoFocus(AutoFocusCallback autoFocusCallback);

    abstract void cancelAutoFocus();

    abstract void clearFocusAndMetering();

    abstract void enableShutterSound(boolean z);

    abstract boolean focusIsVideo();

    abstract String getAPI();

    public abstract boolean getAutoExposureLock();

    abstract CameraFeatures getCameraFeatures();

    abstract int getCameraOrientation();

    public abstract String getColorEffect();

    public abstract String getDefaultColorEffect();

    public abstract String getDefaultISO();

    public abstract String getDefaultSceneMode();

    public abstract String getDefaultWhiteBalance();

    abstract int getDisplayOrientation();

    abstract int getExposureCompensation();

    public abstract String getFlashValue();

    public abstract List<Area> getFocusAreas();

    public abstract String getFocusValue();

    abstract String getISOKey();

    public abstract int getJpegQuality();

    public abstract List<Area> getMeteringAreas();

    abstract String getParametersString();

    public abstract Size getPictureSize();

    public abstract Size getPreviewSize();

    public abstract String getSceneMode();

    abstract List<int[]> getSupportedPreviewFpsRange();

    public abstract boolean getVideoStabilization();

    public abstract String getWhiteBalance();

    public abstract int getZoom();

    abstract void initVideoRecorderPostPrepare(MediaRecorder mediaRecorder);

    abstract void initVideoRecorderPrePrepare(MediaRecorder mediaRecorder);

    abstract boolean isFrontFacing();

    abstract void reconnect() throws IOException;

    abstract void release();

    abstract void removeLocationInfo();

    abstract void setAutoExposureLock(boolean z);

    abstract SupportedValues setColorEffect(String str);

    abstract void setDisplayOrientation(int i);

    abstract boolean setExposureCompensation(int i);

    abstract void setFaceDetectionListener(FaceDetectionListener faceDetectionListener);

    abstract void setFlashValue(String str);

    abstract boolean setFocusAndMeteringArea(List<Area> list);

    abstract void setFocusValue(String str);

    abstract SupportedValues setISO(String str);

    abstract void setJpegQuality(int i);

    abstract void setLocationInfo(Location location);

    abstract void setPictureSize(int i, int i2);

    abstract void setPreviewDisplay(SurfaceHolder surfaceHolder) throws IOException;

    abstract void setPreviewFpsRange(int i, int i2);

    abstract void setPreviewSize(int i, int i2);

    abstract void setPreviewTexture(SurfaceTexture surfaceTexture) throws IOException;

    abstract void setRecordingHint(boolean z);

    abstract void setRotation(int i);

    abstract SupportedValues setSceneMode(String str);

    abstract void setVideoStabilization(boolean z);

    abstract SupportedValues setWhiteBalance(String str);

    abstract void setZoom(int i);

    public abstract boolean startFaceDetection();

    abstract void startPreview();

    abstract void stopPreview();

    abstract boolean supportsAutoFocus();

    abstract void takePicture(PictureCallback pictureCallback, PictureCallback pictureCallback2);

    abstract void unlock();

    protected SupportedValues checkModeIsSupported(List<String> values, String value, String default_value) {
        if (values == null || values.size() <= 1) {
            return null;
        }
        if (!values.contains(value)) {
            if (values.contains(default_value)) {
                value = default_value;
            } else {
                value = (String) values.get(0);
            }
        }
        return new SupportedValues(values, value);
    }
}
