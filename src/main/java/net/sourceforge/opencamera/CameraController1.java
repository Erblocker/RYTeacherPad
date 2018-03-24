package net.sourceforge.opencamera;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.location.Location;
import android.media.MediaRecorder;
import android.os.Build.VERSION;
import android.view.SurfaceHolder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

class CameraController1 extends CameraController {
    private static final String TAG = "CameraController1";
    private Camera camera = null;
    private CameraInfo camera_info = new CameraInfo();
    private int display_orientation = 0;
    private String iso_key = null;

    CameraController1(int cameraId) {
        this.camera = Camera.open(cameraId);
        Camera.getCameraInfo(cameraId, this.camera_info);
    }

    void release() {
        this.camera.release();
        this.camera = null;
    }

    public Camera getCamera() {
        return this.camera;
    }

    private Parameters getParameters() {
        return this.camera.getParameters();
    }

    private void setCameraParameters(Parameters parameters) {
        try {
            this.camera.setParameters(parameters);
        } catch (RuntimeException e) {
            e.printStackTrace();
            this.count_camera_parameters_exception++;
        }
    }

    private List<String> convertFlashModesToValues(List<String> supported_flash_modes) {
        List<String> output_modes = new Vector();
        if (supported_flash_modes != null) {
            if (supported_flash_modes.contains("off")) {
                output_modes.add("flash_off");
            }
            if (supported_flash_modes.contains("auto")) {
                output_modes.add("flash_auto");
            }
            if (supported_flash_modes.contains("on")) {
                output_modes.add("flash_on");
            }
            if (supported_flash_modes.contains("torch")) {
                output_modes.add("flash_torch");
            }
            if (supported_flash_modes.contains("red-eye")) {
                output_modes.add("flash_red_eye");
            }
        }
        return output_modes;
    }

    private List<String> convertFocusModesToValues(List<String> supported_focus_modes) {
        List<String> output_modes = new Vector();
        if (supported_focus_modes != null) {
            if (supported_focus_modes.contains("auto")) {
                output_modes.add("focus_mode_auto");
            }
            if (supported_focus_modes.contains("infinity")) {
                output_modes.add("focus_mode_infinity");
            }
            if (supported_focus_modes.contains("macro")) {
                output_modes.add("focus_mode_macro");
            }
            if (supported_focus_modes.contains("auto")) {
                output_modes.add("focus_mode_manual");
            }
            if (supported_focus_modes.contains("fixed")) {
                output_modes.add("focus_mode_fixed");
            }
            if (supported_focus_modes.contains("edof")) {
                output_modes.add("focus_mode_edof");
            }
            if (supported_focus_modes.contains("continuous-video")) {
                output_modes.add("focus_mode_continuous_video");
            }
        }
        return output_modes;
    }

    String getAPI() {
        return "Camera";
    }

    @TargetApi(17)
    CameraFeatures getCameraFeatures() {
        boolean z;
        Parameters parameters = getParameters();
        CameraFeatures camera_features = new CameraFeatures();
        camera_features.is_zoom_supported = parameters.isZoomSupported();
        if (camera_features.is_zoom_supported) {
            camera_features.max_zoom = parameters.getMaxZoom();
            try {
                camera_features.zoom_ratios = parameters.getZoomRatios();
            } catch (NumberFormatException e) {
                e.printStackTrace();
                camera_features.is_zoom_supported = false;
                camera_features.max_zoom = 0;
                camera_features.zoom_ratios = null;
            }
        }
        if (parameters.getMaxNumDetectedFaces() > 0) {
            z = true;
        } else {
            z = false;
        }
        camera_features.supports_face_detection = z;
        List<Size> camera_picture_sizes = parameters.getSupportedPictureSizes();
        camera_features.picture_sizes = new ArrayList();
        for (Size camera_size : camera_picture_sizes) {
            camera_features.picture_sizes.add(new CameraController.Size(camera_size.width, camera_size.height));
        }
        camera_features.supported_flash_values = convertFlashModesToValues(parameters.getSupportedFlashModes());
        camera_features.supported_focus_values = convertFocusModesToValues(parameters.getSupportedFocusModes());
        camera_features.max_num_focus_areas = parameters.getMaxNumFocusAreas();
        camera_features.is_exposure_lock_supported = parameters.isAutoExposureLockSupported();
        camera_features.is_video_stabilization_supported = parameters.isVideoStabilizationSupported();
        camera_features.min_exposure = parameters.getMinExposureCompensation();
        camera_features.max_exposure = parameters.getMaxExposureCompensation();
        camera_features.exposure_step = parameters.getExposureCompensationStep();
        List<Size> camera_video_sizes = parameters.getSupportedVideoSizes();
        if (camera_video_sizes == null) {
            camera_video_sizes = parameters.getSupportedPreviewSizes();
        }
        camera_features.video_sizes = new ArrayList();
        for (Size camera_size2 : camera_video_sizes) {
            camera_features.video_sizes.add(new CameraController.Size(camera_size2.width, camera_size2.height));
        }
        List<Size> camera_preview_sizes = parameters.getSupportedPreviewSizes();
        camera_features.preview_sizes = new ArrayList();
        for (Size camera_size22 : camera_preview_sizes) {
            camera_features.preview_sizes.add(new CameraController.Size(camera_size22.width, camera_size22.height));
        }
        if (VERSION.SDK_INT >= 17) {
            camera_features.can_disable_shutter_sound = this.camera_info.canDisableShutterSound;
        } else {
            camera_features.can_disable_shutter_sound = false;
        }
        return camera_features;
    }

    public String getDefaultSceneMode() {
        return "auto";
    }

    public String getDefaultColorEffect() {
        return "none";
    }

    public String getDefaultWhiteBalance() {
        return "auto";
    }

    public String getDefaultISO() {
        return "auto";
    }

    SupportedValues setSceneMode(String value) {
        String default_value = getDefaultSceneMode();
        Parameters parameters = getParameters();
        SupportedValues supported_values = checkModeIsSupported(parameters.getSupportedSceneModes(), value, default_value);
        if (!(supported_values == null || parameters.getSceneMode().equals(supported_values.selected_value))) {
            parameters.setSceneMode(supported_values.selected_value);
            setCameraParameters(parameters);
        }
        return supported_values;
    }

    public String getSceneMode() {
        return getParameters().getSceneMode();
    }

    SupportedValues setColorEffect(String value) {
        String default_value = getDefaultColorEffect();
        Parameters parameters = getParameters();
        SupportedValues supported_values = checkModeIsSupported(parameters.getSupportedColorEffects(), value, default_value);
        if (!(supported_values == null || parameters.getColorEffect().equals(supported_values.selected_value))) {
            parameters.setColorEffect(supported_values.selected_value);
            setCameraParameters(parameters);
        }
        return supported_values;
    }

    public String getColorEffect() {
        return getParameters().getColorEffect();
    }

    SupportedValues setWhiteBalance(String value) {
        String default_value = getDefaultWhiteBalance();
        Parameters parameters = getParameters();
        SupportedValues supported_values = checkModeIsSupported(parameters.getSupportedWhiteBalance(), value, default_value);
        if (!(supported_values == null || parameters.getWhiteBalance().equals(supported_values.selected_value))) {
            parameters.setWhiteBalance(supported_values.selected_value);
            setCameraParameters(parameters);
        }
        return supported_values;
    }

    public String getWhiteBalance() {
        return getParameters().getWhiteBalance();
    }

    SupportedValues setISO(String value) {
        SupportedValues supported_values = null;
        String default_value = getDefaultISO();
        Parameters parameters = getParameters();
        String iso_values = parameters.get("iso-values");
        if (iso_values == null) {
            iso_values = parameters.get("iso-mode-values");
            if (iso_values == null) {
                iso_values = parameters.get("iso-speed-values");
                if (iso_values == null) {
                    iso_values = parameters.get("nv-picture-iso-values");
                }
            }
        }
        List<String> list = null;
        if (iso_values != null && iso_values.length() > 0) {
            String[] isos_array = iso_values.split(",");
            if (isos_array != null && isos_array.length > 0) {
                list = new ArrayList();
                for (Object add : isos_array) {
                    list.add(add);
                }
            }
        }
        this.iso_key = "iso";
        if (parameters.get(this.iso_key) == null) {
            this.iso_key = "iso-speed";
            if (parameters.get(this.iso_key) == null) {
                this.iso_key = "nv-picture-iso";
                if (parameters.get(this.iso_key) == null) {
                    this.iso_key = null;
                }
            }
        }
        if (this.iso_key != null) {
            if (list == null) {
                list = new ArrayList();
                list.add("auto");
                list.add("100");
                list.add("200");
                list.add("400");
                list.add("800");
                list.add("1600");
            }
            supported_values = checkModeIsSupported(list, value, default_value);
            if (supported_values != null) {
                parameters.set(this.iso_key, supported_values.selected_value);
                setCameraParameters(parameters);
            }
        }
        return supported_values;
    }

    String getISOKey() {
        return this.iso_key;
    }

    public CameraController.Size getPictureSize() {
        Size camera_size = getParameters().getPictureSize();
        return new CameraController.Size(camera_size.width, camera_size.height);
    }

    void setPictureSize(int width, int height) {
        Parameters parameters = getParameters();
        parameters.setPictureSize(width, height);
        setCameraParameters(parameters);
    }

    public CameraController.Size getPreviewSize() {
        Size camera_size = getParameters().getPreviewSize();
        return new CameraController.Size(camera_size.width, camera_size.height);
    }

    void setPreviewSize(int width, int height) {
        Parameters parameters = getParameters();
        parameters.setPreviewSize(width, height);
        setCameraParameters(parameters);
    }

    void setVideoStabilization(boolean enabled) {
        Parameters parameters = getParameters();
        parameters.setVideoStabilization(enabled);
        setCameraParameters(parameters);
    }

    public boolean getVideoStabilization() {
        return getParameters().getVideoStabilization();
    }

    public int getJpegQuality() {
        return getParameters().getJpegQuality();
    }

    void setJpegQuality(int quality) {
        Parameters parameters = getParameters();
        parameters.setJpegQuality(quality);
        setCameraParameters(parameters);
    }

    public int getZoom() {
        return getParameters().getZoom();
    }

    void setZoom(int value) {
        Parameters parameters = getParameters();
        parameters.setZoom(value);
        setCameraParameters(parameters);
    }

    int getExposureCompensation() {
        return getParameters().getExposureCompensation();
    }

    boolean setExposureCompensation(int new_exposure) {
        Parameters parameters = getParameters();
        if (new_exposure == parameters.getExposureCompensation()) {
            return false;
        }
        parameters.setExposureCompensation(new_exposure);
        setCameraParameters(parameters);
        return true;
    }

    void setPreviewFpsRange(int min, int max) {
        Parameters parameters = getParameters();
        parameters.setPreviewFpsRange(min, max);
        setCameraParameters(parameters);
    }

    List<int[]> getSupportedPreviewFpsRange() {
        try {
            return getParameters().getSupportedPreviewFpsRange();
        } catch (StringIndexOutOfBoundsException e) {
            e.printStackTrace();
            return null;
        }
    }

    void setFocusValue(String focus_value) {
        Parameters parameters = getParameters();
        if (focus_value.equals("focus_mode_auto") || focus_value.equals("focus_mode_manual")) {
            parameters.setFocusMode("auto");
        } else if (focus_value.equals("focus_mode_infinity")) {
            parameters.setFocusMode("infinity");
        } else if (focus_value.equals("focus_mode_macro")) {
            parameters.setFocusMode("macro");
        } else if (focus_value.equals("focus_mode_fixed")) {
            parameters.setFocusMode("fixed");
        } else if (focus_value.equals("focus_mode_edof")) {
            parameters.setFocusMode("edof");
        } else if (focus_value.equals("focus_mode_continuous_video")) {
            parameters.setFocusMode("continuous-video");
        }
        setCameraParameters(parameters);
    }

    private String convertFocusModeToValue(String focus_mode) {
        String focus_value = "";
        if (focus_mode == null) {
            return focus_value;
        }
        if (focus_mode.equals("auto")) {
            return "focus_mode_auto";
        }
        if (focus_mode.equals("infinity")) {
            return "focus_mode_infinity";
        }
        if (focus_mode.equals("macro")) {
            return "focus_mode_macro";
        }
        if (focus_mode.equals("fixed")) {
            return "focus_mode_fixed";
        }
        if (focus_mode.equals("edof")) {
            return "focus_mode_edof";
        }
        if (focus_mode.equals("continuous-video")) {
            return "focus_mode_continuous_video";
        }
        return focus_value;
    }

    public String getFocusValue() {
        return convertFocusModeToValue(getParameters().getFocusMode());
    }

    private String convertFlashValueToMode(String flash_value) {
        String flash_mode = "";
        if (flash_value.equals("flash_off")) {
            return "off";
        }
        if (flash_value.equals("flash_auto")) {
            return "auto";
        }
        if (flash_value.equals("flash_on")) {
            return "on";
        }
        if (flash_value.equals("flash_torch")) {
            return "torch";
        }
        if (flash_value.equals("flash_red_eye")) {
            return "red-eye";
        }
        return flash_mode;
    }

    void setFlashValue(String flash_value) {
        Parameters parameters = getParameters();
        if (parameters.getFlashMode() != null) {
            String flash_mode = convertFlashValueToMode(flash_value);
            if (flash_mode.length() > 0 && !flash_mode.equals(parameters.getFlashMode())) {
                if (parameters.getFlashMode().equals("torch") && !flash_mode.equals("off")) {
                    parameters.setFlashMode("off");
                    setCameraParameters(parameters);
                    parameters = getParameters();
                }
                parameters.setFlashMode(flash_mode);
                setCameraParameters(parameters);
            }
        }
    }

    private String convertFlashModeToValue(String flash_mode) {
        String flash_value = "";
        if (flash_mode == null) {
            return flash_value;
        }
        if (flash_mode.equals("off")) {
            return "flash_off";
        }
        if (flash_mode.equals("auto")) {
            return "flash_auto";
        }
        if (flash_mode.equals("on")) {
            return "flash_on";
        }
        if (flash_mode.equals("torch")) {
            return "flash_torch";
        }
        if (flash_mode.equals("red-eye")) {
            return "flash_red_eye";
        }
        return flash_value;
    }

    public String getFlashValue() {
        return convertFlashModeToValue(getParameters().getFlashMode());
    }

    void setRecordingHint(boolean hint) {
        Parameters parameters = getParameters();
        String focus_mode = parameters.getFocusMode();
        if (focus_mode != null && !focus_mode.equals("continuous-video")) {
            parameters.setRecordingHint(hint);
            setCameraParameters(parameters);
        }
    }

    void setAutoExposureLock(boolean enabled) {
        Parameters parameters = getParameters();
        parameters.setAutoExposureLock(enabled);
        setCameraParameters(parameters);
    }

    public boolean getAutoExposureLock() {
        Parameters parameters = getParameters();
        if (parameters.isAutoExposureLockSupported()) {
            return parameters.getAutoExposureLock();
        }
        return false;
    }

    void setRotation(int rotation) {
        Parameters parameters = getParameters();
        parameters.setRotation(rotation);
        setCameraParameters(parameters);
    }

    void setLocationInfo(Location location) {
        Parameters parameters = getParameters();
        parameters.removeGpsData();
        parameters.setGpsTimestamp(System.currentTimeMillis() / 1000);
        parameters.setGpsLatitude(location.getLatitude());
        parameters.setGpsLongitude(location.getLongitude());
        parameters.setGpsProcessingMethod(location.getProvider());
        if (location.hasAltitude()) {
            parameters.setGpsAltitude(location.getAltitude());
        } else {
            parameters.setGpsAltitude(0.0d);
        }
        if (location.getTime() != 0) {
            parameters.setGpsTimestamp(location.getTime() / 1000);
        }
        setCameraParameters(parameters);
    }

    void removeLocationInfo() {
        Parameters parameters = getParameters();
        parameters.removeGpsData();
        setCameraParameters(parameters);
    }

    @TargetApi(17)
    void enableShutterSound(boolean enabled) {
        if (VERSION.SDK_INT >= 17) {
            this.camera.enableShutterSound(enabled);
        }
    }

    boolean setFocusAndMeteringArea(List<Area> areas) {
        List<Area> camera_areas = new ArrayList();
        for (Area area : areas) {
            camera_areas.add(new Area(area.rect, area.weight));
        }
        Parameters parameters = getParameters();
        String focus_mode = parameters.getFocusMode();
        if (parameters.getMaxNumFocusAreas() == 0 || focus_mode == null || !(focus_mode.equals("auto") || focus_mode.equals("macro") || focus_mode.equals("continuous-picture") || focus_mode.equals("continuous-video"))) {
            if (parameters.getMaxNumMeteringAreas() != 0) {
                parameters.setMeteringAreas(camera_areas);
                setCameraParameters(parameters);
            }
            return false;
        }
        parameters.setFocusAreas(camera_areas);
        if (parameters.getMaxNumMeteringAreas() != 0) {
            parameters.setMeteringAreas(camera_areas);
        }
        setCameraParameters(parameters);
        return true;
    }

    void clearFocusAndMetering() {
        Parameters parameters = getParameters();
        boolean update_parameters = false;
        if (parameters.getMaxNumFocusAreas() > 0) {
            parameters.setFocusAreas(null);
            update_parameters = true;
        }
        if (parameters.getMaxNumMeteringAreas() > 0) {
            parameters.setMeteringAreas(null);
            update_parameters = true;
        }
        if (update_parameters) {
            setCameraParameters(parameters);
        }
    }

    public List<Area> getFocusAreas() {
        List<Area> camera_areas = getParameters().getFocusAreas();
        if (camera_areas == null) {
            return null;
        }
        List<Area> areas = new ArrayList();
        for (Area camera_area : camera_areas) {
            areas.add(new Area(camera_area.rect, camera_area.weight));
        }
        return areas;
    }

    public List<Area> getMeteringAreas() {
        List<Area> camera_areas = getParameters().getMeteringAreas();
        if (camera_areas == null) {
            return null;
        }
        List<Area> areas = new ArrayList();
        for (Area camera_area : camera_areas) {
            areas.add(new Area(camera_area.rect, camera_area.weight));
        }
        return areas;
    }

    boolean supportsAutoFocus() {
        String focus_mode = getParameters().getFocusMode();
        if (focus_mode == null || (!focus_mode.equals("auto") && !focus_mode.equals("macro"))) {
            return false;
        }
        return true;
    }

    boolean focusIsVideo() {
        String current_focus_mode = getParameters().getFocusMode();
        return current_focus_mode != null && current_focus_mode.equals("continuous-video");
    }

    void reconnect() throws IOException {
        this.camera.reconnect();
    }

    void setPreviewDisplay(SurfaceHolder holder) throws IOException {
        this.camera.setPreviewDisplay(holder);
    }

    void setPreviewTexture(SurfaceTexture texture) throws IOException {
        this.camera.setPreviewTexture(texture);
    }

    void startPreview() {
        this.camera.startPreview();
    }

    void stopPreview() {
        this.camera.stopPreview();
    }

    public boolean startFaceDetection() {
        try {
            this.camera.startFaceDetection();
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    void setFaceDetectionListener(final FaceDetectionListener listener) {
        this.camera.setFaceDetectionListener(new FaceDetectionListener() {
            public void onFaceDetection(Face[] camera_faces, Camera camera) {
                Face[] faces = new Face[camera_faces.length];
                for (int i = 0; i < camera_faces.length; i++) {
                    faces[i] = new Face(camera_faces[i].score, camera_faces[i].rect);
                }
                listener.onFaceDetection(faces);
            }
        });
    }

    void autoFocus(final AutoFocusCallback cb) {
        this.camera.autoFocus(new AutoFocusCallback() {
            public void onAutoFocus(boolean success, Camera camera) {
                cb.onAutoFocus(success);
            }
        });
    }

    void cancelAutoFocus() {
        this.camera.cancelAutoFocus();
    }

    void takePicture(final PictureCallback raw, final PictureCallback jpeg) {
        PictureCallback camera_jpeg = null;
        ShutterCallback shutter = new ShutterCallback() {
            public void onShutter() {
            }
        };
        PictureCallback camera_raw = raw == null ? null : new PictureCallback() {
            public void onPictureTaken(byte[] data, Camera cam) {
                raw.onPictureTaken(data);
            }
        };
        if (jpeg != null) {
            camera_jpeg = new PictureCallback() {
                public void onPictureTaken(byte[] data, Camera cam) {
                    jpeg.onPictureTaken(data);
                }
            };
        }
        this.camera.takePicture(shutter, camera_raw, camera_jpeg);
    }

    void setDisplayOrientation(int degrees) {
        int result;
        if (this.camera_info.facing == 1) {
            result = (360 - ((this.camera_info.orientation + degrees) % 360)) % 360;
        } else {
            result = ((this.camera_info.orientation - degrees) + 360) % 360;
        }
        this.camera.setDisplayOrientation(result);
        this.display_orientation = result;
    }

    int getDisplayOrientation() {
        return this.display_orientation;
    }

    int getCameraOrientation() {
        return this.camera_info.orientation;
    }

    boolean isFrontFacing() {
        return this.camera_info.facing == 1;
    }

    void unlock() {
        stopPreview();
        this.camera.unlock();
    }

    void initVideoRecorderPrePrepare(MediaRecorder video_recorder) {
        video_recorder.setCamera(this.camera);
    }

    void initVideoRecorderPostPrepare(MediaRecorder video_recorder) {
    }

    String getParametersString() {
        return getParameters().flatten();
    }
}
