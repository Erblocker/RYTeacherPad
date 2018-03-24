package net.sourceforge.opencamera;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraDevice.StateCallback;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.media.Image;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Range;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import org.apache.http.HttpStatus;

@TargetApi(21)
public class CameraController2 extends CameraController {
    private static final String TAG = "CameraController2";
    private AutoFocusCallback autofocus_cb;
    private CameraDevice camera;
    private String cameraIdS;
    CameraSettings camera_settings;
    private CameraCaptureSession captureSession;
    private CameraCharacteristics characteristics;
    private int current_zoom_value;
    private FaceDetectionListener face_detection_listener;
    Handler handler;
    private ImageReader imageReader;
    private PictureCallback jpeg_cb;
    private int picture_height;
    private int picture_width;
    private Builder previewBuilder;
    private CaptureCallback previewCaptureCallback;
    private int preview_height;
    private int preview_width;
    private Surface surface_texture;
    private SurfaceTexture texture;
    private HandlerThread thread;
    private List<Integer> zoom_ratios;

    class CameraSettings {
        private int ae_exposure_compensation = 0;
        private boolean ae_lock = false;
        private MeteringRectangle[] ae_regions = null;
        private int af_mode = 1;
        private MeteringRectangle[] af_regions = null;
        private int color_effect = 0;
        private int face_detect_mode = 0;
        private boolean has_ae_exposure_compensation = false;
        private boolean has_af_mode = false;
        private boolean has_face_detect_mode = false;
        private boolean has_iso = false;
        private int iso = 0;
        private byte jpeg_quality = (byte) 90;
        private Location location = null;
        private int rotation = 0;
        private Rect scalar_crop_region = null;
        private int scene_mode = 0;
        private int white_balance = 1;

        CameraSettings() {
        }

        private void setupBuilder(Builder builder, boolean is_still) {
            setSceneMode(builder);
            setColorEffect(builder);
            setWhiteBalance(builder);
            setISO(builder);
            setCropRegion(builder);
            setExposureCompensation(builder);
            setFocusMode(builder);
            setAutoExposureLock(builder);
            setAFRegions(builder);
            setAERegions(builder);
            setFaceDetectMode(builder);
            builder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(1));
            if (is_still) {
                builder.set(CaptureRequest.JPEG_ORIENTATION, Integer.valueOf(this.rotation));
                builder.set(CaptureRequest.JPEG_QUALITY, Byte.valueOf(this.jpeg_quality));
            }
        }

        private boolean setSceneMode(Builder builder) {
            if ((builder.get(CaptureRequest.CONTROL_SCENE_MODE) == null && this.scene_mode == 0) || (builder.get(CaptureRequest.CONTROL_SCENE_MODE) != null && ((Integer) builder.get(CaptureRequest.CONTROL_SCENE_MODE)).intValue() == this.scene_mode)) {
                return false;
            }
            if (this.scene_mode == 0) {
                builder.set(CaptureRequest.CONTROL_MODE, Integer.valueOf(1));
            } else {
                builder.set(CaptureRequest.CONTROL_MODE, Integer.valueOf(2));
            }
            builder.set(CaptureRequest.CONTROL_SCENE_MODE, Integer.valueOf(this.scene_mode));
            return true;
        }

        private boolean setColorEffect(Builder builder) {
            if ((builder.get(CaptureRequest.CONTROL_EFFECT_MODE) == null && this.color_effect == 0) || (builder.get(CaptureRequest.CONTROL_EFFECT_MODE) != null && ((Integer) builder.get(CaptureRequest.CONTROL_EFFECT_MODE)).intValue() == this.color_effect)) {
                return false;
            }
            builder.set(CaptureRequest.CONTROL_EFFECT_MODE, Integer.valueOf(this.color_effect));
            return true;
        }

        private boolean setWhiteBalance(Builder builder) {
            if ((builder.get(CaptureRequest.CONTROL_AWB_MODE) == null && this.white_balance == 1) || (builder.get(CaptureRequest.CONTROL_AWB_MODE) != null && ((Integer) builder.get(CaptureRequest.CONTROL_AWB_MODE)).intValue() == this.white_balance)) {
                return false;
            }
            builder.set(CaptureRequest.CONTROL_AWB_MODE, Integer.valueOf(this.white_balance));
            return true;
        }

        private boolean setISO(Builder builder) {
            if (this.has_iso) {
                builder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(0));
                builder.set(CaptureRequest.SENSOR_SENSITIVITY, Integer.valueOf(this.iso));
            } else {
                builder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(1));
            }
            return true;
        }

        private void setCropRegion(Builder builder) {
            if (this.scalar_crop_region != null) {
                builder.set(CaptureRequest.SCALER_CROP_REGION, this.scalar_crop_region);
            }
        }

        private boolean setExposureCompensation(Builder builder) {
            if (!this.has_ae_exposure_compensation) {
                return false;
            }
            if (builder.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION) != null && this.ae_exposure_compensation == ((Integer) builder.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION)).intValue()) {
                return false;
            }
            builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, Integer.valueOf(this.ae_exposure_compensation));
            return true;
        }

        private void setFocusMode(Builder builder) {
            if (this.has_af_mode) {
                builder.set(CaptureRequest.CONTROL_AF_MODE, Integer.valueOf(this.af_mode));
            }
        }

        private void setAutoExposureLock(Builder builder) {
            builder.set(CaptureRequest.CONTROL_AE_LOCK, Boolean.valueOf(this.ae_lock));
        }

        private void setAFRegions(Builder builder) {
            if (this.af_regions != null && ((Integer) CameraController2.this.characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF)).intValue() > 0) {
                builder.set(CaptureRequest.CONTROL_AF_REGIONS, this.af_regions);
            }
        }

        private void setAERegions(Builder builder) {
            if (this.ae_regions != null && ((Integer) CameraController2.this.characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE)).intValue() > 0) {
                builder.set(CaptureRequest.CONTROL_AE_REGIONS, this.ae_regions);
            }
        }

        private void setFaceDetectMode(Builder builder) {
            if (this.has_face_detect_mode) {
                builder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, Integer.valueOf(this.face_detect_mode));
            }
        }
    }

    public CameraController2(Context context, int cameraId) {
        this.camera = null;
        this.cameraIdS = null;
        this.characteristics = null;
        this.zoom_ratios = null;
        this.current_zoom_value = 0;
        this.captureSession = null;
        this.previewBuilder = null;
        this.autofocus_cb = null;
        this.face_detection_listener = null;
        this.imageReader = null;
        this.jpeg_cb = null;
        this.texture = null;
        this.surface_texture = null;
        this.thread = null;
        this.handler = null;
        this.preview_width = 0;
        this.preview_height = 0;
        this.picture_width = 0;
        this.picture_height = 0;
        this.camera_settings = new CameraSettings();
        this.previewCaptureCallback = new CaptureCallback() {
            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                if (CameraController2.this.autofocus_cb != null) {
                    int af_state = ((Integer) result.get(CaptureResult.CONTROL_AF_STATE)).intValue();
                    if (af_state == 4 || af_state == 5) {
                        boolean z;
                        CameraController2.this.previewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(2));
                        CameraController2.this.capture();
                        CameraController2.this.previewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(0));
                        AutoFocusCallback access$0 = CameraController2.this.autofocus_cb;
                        if (af_state == 4) {
                            z = true;
                        } else {
                            z = false;
                        }
                        access$0.onAutoFocus(z);
                        CameraController2.this.autofocus_cb = null;
                    }
                }
                if (CameraController2.this.face_detection_listener != null && CameraController2.this.previewBuilder.get(CaptureRequest.STATISTICS_FACE_DETECT_MODE) != null && ((Integer) CameraController2.this.previewBuilder.get(CaptureRequest.STATISTICS_FACE_DETECT_MODE)).intValue() == 1) {
                    Rect sensor_rect = (Rect) CameraController2.this.characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                    Face[] camera_faces = (Face[]) result.get(CaptureResult.STATISTICS_FACES);
                    Face[] faces = new Face[camera_faces.length];
                    for (int i = 0; i < camera_faces.length; i++) {
                        faces[i] = CameraController2.this.convertFromCameraFace(sensor_rect, camera_faces[i]);
                    }
                    CameraController2.this.face_detection_listener.onFaceDetection(faces);
                }
            }
        };
        this.thread = new HandlerThread("CameraBackground");
        this.thread.start();
        this.handler = new Handler(this.thread.getLooper());
        AnonymousClass1MyStateCallback myStateCallback = new StateCallback() {
            boolean callback_done = false;

            public void onOpened(CameraDevice cam) {
                CameraController2.this.camera = cam;
                CameraController2.this.createPreviewRequest();
                this.callback_done = true;
            }

            public void onClosed(CameraDevice cam) {
            }

            public void onDisconnected(CameraDevice cam) {
                CameraController2.this.camera = null;
                cam.close();
                this.callback_done = true;
            }

            public void onError(CameraDevice cam, int error) {
                CameraController2.this.camera = null;
                cam.close();
                this.callback_done = true;
            }
        };
        CameraManager manager = (CameraManager) context.getSystemService("camera");
        try {
            this.cameraIdS = manager.getCameraIdList()[cameraId];
            manager.openCamera(this.cameraIdS, myStateCallback, this.handler);
            this.characteristics = manager.getCameraCharacteristics(this.cameraIdS);
            do {
            } while (!myStateCallback.callback_done);
            if (this.camera == null) {
                throw new RuntimeException();
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    void release() {
        if (this.thread != null) {
            this.thread.quitSafely();
            try {
                this.thread.join();
                this.thread = null;
                this.handler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (this.captureSession != null) {
            this.captureSession.close();
            this.captureSession = null;
        }
        this.previewBuilder = null;
        if (this.camera != null) {
            this.camera.close();
            this.camera = null;
        }
        if (this.imageReader != null) {
            this.imageReader.close();
            this.imageReader = null;
        }
    }

    private List<String> convertFocusModesToValues(int[] supported_focus_modes_arr) {
        List<Integer> supported_focus_modes = new ArrayList();
        for (int valueOf : supported_focus_modes_arr) {
            supported_focus_modes.add(Integer.valueOf(valueOf));
        }
        List<String> output_modes = new Vector();
        if (supported_focus_modes != null) {
            if (supported_focus_modes.contains(Integer.valueOf(1))) {
                output_modes.add("focus_mode_auto");
            }
            if (supported_focus_modes.contains(Integer.valueOf(2))) {
                output_modes.add("focus_mode_macro");
            }
            if (supported_focus_modes.contains(Integer.valueOf(1))) {
                output_modes.add("focus_mode_manual");
            }
            if (supported_focus_modes.contains(Integer.valueOf(5))) {
                output_modes.add("focus_mode_edof");
            }
            if (supported_focus_modes.contains(Integer.valueOf(3))) {
                output_modes.add("focus_mode_continuous_video");
            }
        }
        return output_modes;
    }

    String getAPI() {
        return "Camera2 (Android L)";
    }

    CameraFeatures getCameraFeatures() {
        int i;
        CameraFeatures camera_features = new CameraFeatures();
        float max_zoom = ((Float) this.characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)).floatValue();
        camera_features.is_zoom_supported = max_zoom > 0.0f;
        if (camera_features.is_zoom_supported) {
            int n_steps = (int) ((20.0d * Math.log(((double) max_zoom) + 1.0E-11d)) / Math.log(2.0d));
            double scale_factor = Math.pow((double) max_zoom, 1.0d / ((double) n_steps));
            camera_features.zoom_ratios = new ArrayList();
            camera_features.zoom_ratios.add(Integer.valueOf(100));
            double zoom = 1.0d;
            for (i = 0; i < n_steps - 1; i++) {
                zoom *= scale_factor;
                camera_features.zoom_ratios.add(Integer.valueOf((int) (100.0d * zoom)));
            }
            camera_features.zoom_ratios.add(Integer.valueOf((int) (100.0f * max_zoom)));
            camera_features.max_zoom = camera_features.zoom_ratios.size() - 1;
            this.zoom_ratios = camera_features.zoom_ratios;
        } else {
            this.zoom_ratios = null;
        }
        int[] face_modes = (int[]) this.characteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES);
        camera_features.supports_face_detection = false;
        for (i = 0; i < face_modes.length && !camera_features.supports_face_detection; i++) {
            if (face_modes[i] == 1) {
                camera_features.supports_face_detection = true;
            }
        }
        if (camera_features.supports_face_detection && ((Integer) this.characteristics.get(CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT)).intValue() <= 0) {
            camera_features.supports_face_detection = false;
        }
        StreamConfigurationMap configs = (StreamConfigurationMap) this.characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] camera_picture_sizes = configs.getOutputSizes(256);
        camera_features.picture_sizes = new ArrayList();
        for (Size camera_size : camera_picture_sizes) {
            camera_features.picture_sizes.add(new CameraController.Size(camera_size.getWidth(), camera_size.getHeight()));
        }
        Size[] camera_video_sizes = configs.getOutputSizes(MediaRecorder.class);
        camera_features.video_sizes = new ArrayList();
        for (Size camera_size2 : camera_video_sizes) {
            if (camera_size2.getWidth() <= 3840 && camera_size2.getHeight() <= 2160) {
                camera_features.video_sizes.add(new CameraController.Size(camera_size2.getWidth(), camera_size2.getHeight()));
            }
        }
        Size[] camera_preview_sizes = configs.getOutputSizes(SurfaceTexture.class);
        camera_features.preview_sizes = new ArrayList();
        for (Size camera_size22 : camera_preview_sizes) {
            if (camera_size22.getWidth() <= 1920 && camera_size22.getHeight() <= 1440) {
                camera_features.preview_sizes.add(new CameraController.Size(camera_size22.getWidth(), camera_size22.getHeight()));
            }
        }
        ((Boolean) this.characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)).booleanValue();
        camera_features.supported_focus_values = convertFocusModesToValues((int[]) this.characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES));
        camera_features.max_num_focus_areas = ((Integer) this.characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF)).intValue();
        camera_features.is_exposure_lock_supported = true;
        Range<Integer> exposure_range = (Range) this.characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
        camera_features.min_exposure = ((Integer) exposure_range.getLower()).intValue();
        camera_features.max_exposure = ((Integer) exposure_range.getUpper()).intValue();
        camera_features.exposure_step = ((Rational) this.characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP)).floatValue();
        return camera_features;
    }

    private String convertSceneMode(int value2) {
        switch (value2) {
            case 0:
                return "auto";
            case 2:
                return TestHandler.ACTION;
            case 3:
                return "portrait";
            case 4:
                return "landscape";
            case 5:
                return "night";
            case 6:
                return "night-portrait";
            case 7:
                return "theatre";
            case 8:
                return "beach";
            case 9:
                return "snow";
            case 10:
                return "sunset";
            case 11:
                return "steadyphoto";
            case 12:
                return "fireworks";
            case 13:
                return "sports";
            case 14:
                return "party";
            case 15:
                return "candlelight";
            case 16:
                return "barcode";
            default:
                return null;
        }
    }

    SupportedValues setSceneMode(String value) {
        String default_value = getDefaultSceneMode();
        int[] values2 = (int[]) this.characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES);
        boolean has_disabled = false;
        List<String> values = new ArrayList();
        for (int i = 0; i < values2.length; i++) {
            if (values2[i] == 0) {
                has_disabled = true;
            }
            String this_value = convertSceneMode(values2[i]);
            if (this_value != null) {
                values.add(this_value);
            }
        }
        if (!has_disabled) {
            values.add(0, "auto");
        }
        SupportedValues supported_values = checkModeIsSupported(values, value, default_value);
        if (supported_values != null) {
            int selected_value2 = 0;
            if (supported_values.selected_value.equals(TestHandler.ACTION)) {
                selected_value2 = 2;
            } else if (supported_values.selected_value.equals("barcode")) {
                selected_value2 = 16;
            } else if (supported_values.selected_value.equals("beach")) {
                selected_value2 = 8;
            } else if (supported_values.selected_value.equals("candlelight")) {
                selected_value2 = 15;
            } else if (supported_values.selected_value.equals("auto")) {
                selected_value2 = 0;
            } else if (supported_values.selected_value.equals("fireworks")) {
                selected_value2 = 12;
            } else if (supported_values.selected_value.equals("landscape")) {
                selected_value2 = 4;
            } else if (supported_values.selected_value.equals("night")) {
                selected_value2 = 5;
            } else if (supported_values.selected_value.equals("night-portrait")) {
                selected_value2 = 6;
            } else if (supported_values.selected_value.equals("party")) {
                selected_value2 = 14;
            } else if (supported_values.selected_value.equals("portrait")) {
                selected_value2 = 3;
            } else if (supported_values.selected_value.equals("snow")) {
                selected_value2 = 9;
            } else if (supported_values.selected_value.equals("sports")) {
                selected_value2 = 13;
            } else if (supported_values.selected_value.equals("steadyphoto")) {
                selected_value2 = 11;
            } else if (supported_values.selected_value.equals("sunset")) {
                selected_value2 = 10;
            } else if (supported_values.selected_value.equals("theatre")) {
                selected_value2 = 7;
            }
            this.camera_settings.scene_mode = selected_value2;
            if (this.camera_settings.setSceneMode(this.previewBuilder)) {
                setRepeatingRequest();
            }
        }
        return supported_values;
    }

    public String getSceneMode() {
        if (this.previewBuilder.get(CaptureRequest.CONTROL_SCENE_MODE) == null) {
            return null;
        }
        return convertSceneMode(((Integer) this.previewBuilder.get(CaptureRequest.CONTROL_SCENE_MODE)).intValue());
    }

    private String convertColorEffect(int value2) {
        switch (value2) {
            case 0:
                return "none";
            case 1:
                return "mono";
            case 2:
                return "negative";
            case 3:
                return "solarize";
            case 4:
                return "sepia";
            case 5:
                return "posterize";
            case 6:
                return "whiteboard";
            case 7:
                return "blackboard";
            case 8:
                return "aqua";
            default:
                return null;
        }
    }

    SupportedValues setColorEffect(String value) {
        String default_value = getDefaultColorEffect();
        int[] values2 = (int[]) this.characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS);
        List<String> values = new ArrayList();
        for (int convertColorEffect : values2) {
            String this_value = convertColorEffect(convertColorEffect);
            if (this_value != null) {
                values.add(this_value);
            }
        }
        SupportedValues supported_values = checkModeIsSupported(values, value, default_value);
        if (supported_values != null) {
            int selected_value2 = 0;
            if (supported_values.selected_value.equals("aqua")) {
                selected_value2 = 8;
            } else if (supported_values.selected_value.equals("blackboard")) {
                selected_value2 = 7;
            } else if (supported_values.selected_value.equals("mono")) {
                selected_value2 = 1;
            } else if (supported_values.selected_value.equals("negative")) {
                selected_value2 = 2;
            } else if (supported_values.selected_value.equals("none")) {
                selected_value2 = 0;
            } else if (supported_values.selected_value.equals("posterize")) {
                selected_value2 = 5;
            } else if (supported_values.selected_value.equals("sepia")) {
                selected_value2 = 4;
            } else if (supported_values.selected_value.equals("solarize")) {
                selected_value2 = 3;
            } else if (supported_values.selected_value.equals("whiteboard")) {
                selected_value2 = 6;
            }
            this.camera_settings.color_effect = selected_value2;
            if (this.camera_settings.setColorEffect(this.previewBuilder)) {
                setRepeatingRequest();
            }
        }
        return supported_values;
    }

    public String getColorEffect() {
        if (this.previewBuilder.get(CaptureRequest.CONTROL_EFFECT_MODE) == null) {
            return null;
        }
        return convertColorEffect(((Integer) this.previewBuilder.get(CaptureRequest.CONTROL_EFFECT_MODE)).intValue());
    }

    private String convertWhiteBalance(int value2) {
        switch (value2) {
            case 1:
                return "auto";
            case 2:
                return "incandescent";
            case 3:
                return "fluorescent";
            case 4:
                return "warm-fluorescent";
            case 5:
                return "daylight";
            case 6:
                return "cloudy-daylight";
            case 7:
                return "twilight";
            case 8:
                return "shade";
            default:
                return null;
        }
    }

    SupportedValues setWhiteBalance(String value) {
        String default_value = getDefaultWhiteBalance();
        int[] values2 = (int[]) this.characteristics.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES);
        List<String> values = new ArrayList();
        for (int convertWhiteBalance : values2) {
            String this_value = convertWhiteBalance(convertWhiteBalance);
            if (this_value != null) {
                values.add(this_value);
            }
        }
        SupportedValues supported_values = checkModeIsSupported(values, value, default_value);
        if (supported_values != null) {
            int selected_value2 = 1;
            if (supported_values.selected_value.equals("auto")) {
                selected_value2 = 1;
            } else if (supported_values.selected_value.equals("cloudy-daylight")) {
                selected_value2 = 6;
            } else if (supported_values.selected_value.equals("daylight")) {
                selected_value2 = 5;
            } else if (supported_values.selected_value.equals("fluorescent")) {
                selected_value2 = 3;
            } else if (supported_values.selected_value.equals("incandescent")) {
                selected_value2 = 2;
            } else if (supported_values.selected_value.equals("shade")) {
                selected_value2 = 8;
            } else if (supported_values.selected_value.equals("twilight")) {
                selected_value2 = 7;
            } else if (supported_values.selected_value.equals("warm-fluorescent")) {
                selected_value2 = 4;
            }
            this.camera_settings.white_balance = selected_value2;
            if (this.camera_settings.setWhiteBalance(this.previewBuilder)) {
                setRepeatingRequest();
            }
        }
        return supported_values;
    }

    public String getWhiteBalance() {
        if (this.previewBuilder.get(CaptureRequest.CONTROL_AWB_MODE) == null) {
            return null;
        }
        return convertWhiteBalance(((Integer) this.previewBuilder.get(CaptureRequest.CONTROL_AWB_MODE)).intValue());
    }

    SupportedValues setISO(String value) {
        String default_value = getDefaultISO();
        Range<Integer> iso_range = (Range) this.characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
        if (iso_range == null) {
            return null;
        }
        List<String> values = new ArrayList();
        values.add("auto");
        int[] iso_values = new int[]{100, 200, HttpStatus.SC_BAD_REQUEST, 800, 1600};
        int i = 0;
        while (i < iso_values.length) {
            if (iso_values[i] >= ((Integer) iso_range.getLower()).intValue() && iso_values[i] <= ((Integer) iso_range.getUpper()).intValue()) {
                values.add(iso_values[i]);
            }
            i++;
        }
        SupportedValues supported_values = checkModeIsSupported(values, value, default_value);
        if (supported_values == null) {
            return supported_values;
        }
        if (supported_values.selected_value.equals("auto")) {
            this.camera_settings.has_iso = false;
            this.camera_settings.iso = 0;
            if (!this.camera_settings.setISO(this.previewBuilder)) {
                return supported_values;
            }
            setRepeatingRequest();
            return supported_values;
        }
        try {
            int selected_value2 = Integer.parseInt(supported_values.selected_value);
            this.camera_settings.has_iso = true;
            this.camera_settings.iso = selected_value2;
            if (!this.camera_settings.setISO(this.previewBuilder)) {
                return supported_values;
            }
            setRepeatingRequest();
            return supported_values;
        } catch (NumberFormatException e) {
            this.camera_settings.has_iso = false;
            this.camera_settings.iso = 0;
            if (!this.camera_settings.setISO(this.previewBuilder)) {
                return supported_values;
            }
            setRepeatingRequest();
            return supported_values;
        }
    }

    String getISOKey() {
        return "";
    }

    public CameraController.Size getPictureSize() {
        return new CameraController.Size(this.picture_width, this.picture_height);
    }

    void setPictureSize(int width, int height) {
        if (this.camera != null) {
            if (this.captureSession != null) {
                throw new RuntimeException();
            }
            this.picture_width = width;
            this.picture_height = height;
        }
    }

    private void createPictureImageReader() {
        if (this.captureSession != null) {
            throw new RuntimeException();
        }
        if (this.imageReader != null) {
            this.imageReader.close();
        }
        if (this.picture_width == 0 || this.picture_height == 0) {
            throw new RuntimeException();
        }
        this.imageReader = ImageReader.newInstance(this.picture_width, this.picture_height, 256, 2);
        this.imageReader.setOnImageAvailableListener(new OnImageAvailableListener() {
            public void onImageAvailable(ImageReader reader) {
                if (CameraController2.this.jpeg_cb != null) {
                    Image image = reader.acquireNextImage();
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    image.close();
                    CameraController2.this.jpeg_cb.onPictureTaken(bytes);
                    CameraController2.this.cancelAutoFocus();
                }
            }
        }, null);
    }

    public CameraController.Size getPreviewSize() {
        return new CameraController.Size(this.preview_width, this.preview_height);
    }

    void setPreviewSize(int width, int height) {
        this.preview_width = width;
        this.preview_height = height;
    }

    void setVideoStabilization(boolean enabled) {
    }

    public boolean getVideoStabilization() {
        return false;
    }

    public int getJpegQuality() {
        return this.camera_settings.jpeg_quality;
    }

    void setJpegQuality(int quality) {
        if (quality < 0 || quality > 100) {
            throw new RuntimeException();
        }
        this.camera_settings.jpeg_quality = (byte) quality;
    }

    public int getZoom() {
        return this.current_zoom_value;
    }

    void setZoom(int value) {
        if (this.zoom_ratios != null) {
            if (value < 0 || value > this.zoom_ratios.size()) {
                throw new RuntimeException();
            }
            float zoom = ((float) ((Integer) this.zoom_ratios.get(value)).intValue()) / 100.0f;
            Rect sensor_rect = (Rect) this.characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            int left = sensor_rect.centerX();
            int right = left;
            int top = sensor_rect.centerY();
            int bottom = top;
            int hwidth = (int) (((double) sensor_rect.width()) / (((double) zoom) * 2.0d));
            int hheight = (int) (((double) sensor_rect.height()) / (((double) zoom) * 2.0d));
            top -= hheight;
            this.camera_settings.scalar_crop_region = new Rect(left - hwidth, top, right + hwidth, bottom + hheight);
            this.camera_settings.setCropRegion(this.previewBuilder);
            setRepeatingRequest();
            this.current_zoom_value = value;
        }
    }

    int getExposureCompensation() {
        if (this.previewBuilder.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION) == null) {
            return 0;
        }
        return ((Integer) this.previewBuilder.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION)).intValue();
    }

    boolean setExposureCompensation(int new_exposure) {
        this.camera_settings.has_ae_exposure_compensation = true;
        this.camera_settings.ae_exposure_compensation = new_exposure;
        if (!this.camera_settings.setExposureCompensation(this.previewBuilder)) {
            return false;
        }
        setRepeatingRequest();
        return true;
    }

    void setPreviewFpsRange(int min, int max) {
    }

    List<int[]> getSupportedPreviewFpsRange() {
        return null;
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

    void setFocusValue(String focus_value) {
        int focus_mode;
        if (focus_value.equals("focus_mode_auto") || focus_value.equals("focus_mode_manual")) {
            focus_mode = 1;
        } else if (focus_value.equals("focus_mode_macro")) {
            focus_mode = 2;
        } else if (focus_value.equals("focus_mode_edof")) {
            focus_mode = 5;
        } else if (focus_value.equals("focus_mode_continuous_video")) {
            focus_mode = 3;
        } else {
            return;
        }
        this.camera_settings.has_af_mode = true;
        this.camera_settings.af_mode = focus_mode;
        this.camera_settings.setFocusMode(this.previewBuilder);
        setRepeatingRequest();
    }

    private String convertFocusModeToValue(int focus_mode) {
        String focus_value = "";
        if (focus_mode == 1) {
            return "focus_mode_auto";
        }
        if (focus_mode == 2) {
            return "focus_mode_macro";
        }
        if (focus_mode == 5) {
            return "focus_mode_edof";
        }
        if (focus_mode == 3) {
            return "focus_mode_continuous_video";
        }
        return focus_value;
    }

    public String getFocusValue() {
        return convertFocusModeToValue(this.previewBuilder.get(CaptureRequest.CONTROL_AF_MODE) != null ? ((Integer) this.previewBuilder.get(CaptureRequest.CONTROL_AF_MODE)).intValue() : 1);
    }

    void setFlashValue(String flash_value) {
    }

    public String getFlashValue() {
        return "";
    }

    void setRecordingHint(boolean hint) {
    }

    void setAutoExposureLock(boolean enabled) {
        this.camera_settings.ae_lock = enabled;
        this.camera_settings.setAutoExposureLock(this.previewBuilder);
        setRepeatingRequest();
    }

    public boolean getAutoExposureLock() {
        if (this.previewBuilder.get(CaptureRequest.CONTROL_AE_LOCK) == null) {
            return false;
        }
        return ((Boolean) this.previewBuilder.get(CaptureRequest.CONTROL_AE_LOCK)).booleanValue();
    }

    void setRotation(int rotation) {
        this.camera_settings.rotation = rotation;
    }

    void setLocationInfo(Location location) {
        this.camera_settings.location = location;
    }

    void removeLocationInfo() {
        this.camera_settings.location = null;
    }

    void enableShutterSound(boolean enabled) {
    }

    private Rect convertRectToCamera2(Rect sensor_rect, Rect rect) {
        int right = (int) (((double) (sensor_rect.width() - 1)) * (((double) (rect.right + 1000)) / 2000.0d));
        int top = (int) (((double) (sensor_rect.height() - 1)) * (((double) (rect.top + 1000)) / 2000.0d));
        int bottom = (int) (((double) (sensor_rect.height() - 1)) * (((double) (rect.bottom + 1000)) / 2000.0d));
        int left = Math.max((int) (((double) (sensor_rect.width() - 1)) * (((double) (rect.left + 1000)) / 2000.0d)), 0);
        right = Math.max(right, 0);
        top = Math.max(top, 0);
        bottom = Math.max(bottom, 0);
        return new Rect(Math.min(left, sensor_rect.width() - 1), Math.min(top, sensor_rect.height() - 1), Math.min(right, sensor_rect.width() - 1), Math.min(bottom, sensor_rect.height() - 1));
    }

    private MeteringRectangle convertAreaToMeteringRectangle(Rect sensor_rect, Area area) {
        return new MeteringRectangle(convertRectToCamera2(sensor_rect, area.rect), area.weight);
    }

    private Rect convertRectFromCamera2(Rect sensor_rect, Rect camera2_rect) {
        int right = ((int) (2000.0d * (((double) camera2_rect.right) / ((double) (sensor_rect.width() - 1))))) - 1000;
        int top = ((int) (2000.0d * (((double) camera2_rect.top) / ((double) (sensor_rect.height() - 1))))) - 1000;
        int bottom = ((int) (2000.0d * (((double) camera2_rect.bottom) / ((double) (sensor_rect.height() - 1))))) - 1000;
        int left = Math.max(((int) (2000.0d * (((double) camera2_rect.left) / ((double) (sensor_rect.width() - 1))))) - 1000, -1000);
        right = Math.max(right, -1000);
        top = Math.max(top, -1000);
        bottom = Math.max(bottom, -1000);
        return new Rect(Math.min(left, 1000), Math.min(top, 1000), Math.min(right, 1000), Math.min(bottom, 1000));
    }

    private Area convertMeteringRectangleToArea(Rect sensor_rect, MeteringRectangle metering_rectangle) {
        return new Area(convertRectFromCamera2(sensor_rect, metering_rectangle.getRect()), metering_rectangle.getMeteringWeight());
    }

    private Face convertFromCameraFace(Rect sensor_rect, Face camera2_face) {
        return new Face(camera2_face.getScore(), convertRectFromCamera2(sensor_rect, camera2_face.getBounds()));
    }

    boolean setFocusAndMeteringArea(List<Area> areas) {
        int i;
        Rect sensor_rect = (Rect) this.characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        boolean has_focus = false;
        boolean has_metering = false;
        if (((Integer) this.characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF)).intValue() > 0) {
            has_focus = true;
            this.camera_settings.af_regions = new MeteringRectangle[areas.size()];
            i = 0;
            for (Area area : areas) {
                int i2 = i + 1;
                this.camera_settings.af_regions[i] = convertAreaToMeteringRectangle(sensor_rect, area);
                i = i2;
            }
            this.camera_settings.setAFRegions(this.previewBuilder);
        } else {
            this.camera_settings.af_regions = null;
        }
        if (((Integer) this.characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE)).intValue() > 0) {
            has_metering = true;
            this.camera_settings.ae_regions = new MeteringRectangle[areas.size()];
            i = 0;
            for (Area area2 : areas) {
                i2 = i + 1;
                this.camera_settings.ae_regions[i] = convertAreaToMeteringRectangle(sensor_rect, area2);
                i = i2;
            }
            this.camera_settings.setAERegions(this.previewBuilder);
        } else {
            this.camera_settings.ae_regions = null;
        }
        if (has_focus || has_metering) {
            setRepeatingRequest();
        }
        return has_focus;
    }

    void clearFocusAndMetering() {
        Rect sensor_rect = (Rect) this.characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        boolean has_focus = false;
        boolean has_metering = false;
        if (((Integer) this.characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF)).intValue() > 0) {
            has_focus = true;
            this.camera_settings.af_regions = new MeteringRectangle[1];
            this.camera_settings.af_regions[0] = new MeteringRectangle(0, 0, sensor_rect.width() - 1, sensor_rect.height() - 1, 0);
            this.camera_settings.setAFRegions(this.previewBuilder);
        } else {
            this.camera_settings.af_regions = null;
        }
        if (((Integer) this.characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE)).intValue() > 0) {
            has_metering = true;
            this.camera_settings.ae_regions = new MeteringRectangle[1];
            this.camera_settings.ae_regions[0] = new MeteringRectangle(0, 0, sensor_rect.width() - 1, sensor_rect.height() - 1, 0);
            this.camera_settings.setAERegions(this.previewBuilder);
        } else {
            this.camera_settings.ae_regions = null;
        }
        if (has_focus || has_metering) {
            setRepeatingRequest();
        }
    }

    public List<Area> getFocusAreas() {
        List<Area> list = null;
        if (((Integer) this.characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF)).intValue() != 0) {
            MeteringRectangle[] metering_rectangles = (MeteringRectangle[]) this.previewBuilder.get(CaptureRequest.CONTROL_AF_REGIONS);
            if (metering_rectangles != null) {
                Rect sensor_rect = (Rect) this.characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                this.camera_settings.af_regions[0] = new MeteringRectangle(0, 0, sensor_rect.width() - 1, sensor_rect.height() - 1, 0);
                if (!(metering_rectangles.length == 1 && metering_rectangles[0].getRect().left == 0 && metering_rectangles[0].getRect().top == 0 && metering_rectangles[0].getRect().right == sensor_rect.width() - 1 && metering_rectangles[0].getRect().bottom == sensor_rect.height() - 1)) {
                    list = new ArrayList();
                    for (MeteringRectangle convertMeteringRectangleToArea : metering_rectangles) {
                        list.add(convertMeteringRectangleToArea(sensor_rect, convertMeteringRectangleToArea));
                    }
                }
            }
        }
        return list;
    }

    public List<Area> getMeteringAreas() {
        List<Area> list = null;
        if (((Integer) this.characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE)).intValue() != 0) {
            MeteringRectangle[] metering_rectangles = (MeteringRectangle[]) this.previewBuilder.get(CaptureRequest.CONTROL_AE_REGIONS);
            if (metering_rectangles != null) {
                Rect sensor_rect = (Rect) this.characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                if (!(metering_rectangles.length == 1 && metering_rectangles[0].getRect().left == 0 && metering_rectangles[0].getRect().top == 0 && metering_rectangles[0].getRect().right == sensor_rect.width() - 1 && metering_rectangles[0].getRect().bottom == sensor_rect.height() - 1)) {
                    list = new ArrayList();
                    for (MeteringRectangle convertMeteringRectangleToArea : metering_rectangles) {
                        list.add(convertMeteringRectangleToArea(sensor_rect, convertMeteringRectangleToArea));
                    }
                }
            }
        }
        return list;
    }

    boolean supportsAutoFocus() {
        if (this.previewBuilder.get(CaptureRequest.CONTROL_AF_MODE) == null) {
            return true;
        }
        int focus_mode = ((Integer) this.previewBuilder.get(CaptureRequest.CONTROL_AF_MODE)).intValue();
        if (focus_mode == 1 || focus_mode == 2) {
            return true;
        }
        return false;
    }

    boolean focusIsVideo() {
        if (this.previewBuilder.get(CaptureRequest.CONTROL_AF_MODE) == null) {
            return false;
        }
        return ((Integer) this.previewBuilder.get(CaptureRequest.CONTROL_AF_MODE)).intValue() == 3;
    }

    void setPreviewDisplay(SurfaceHolder holder) throws IOException {
        throw new RuntimeException();
    }

    void setPreviewTexture(SurfaceTexture texture) throws IOException {
        if (this.texture != null) {
            throw new RuntimeException();
        }
        this.texture = texture;
        this.surface_texture = new Surface(texture);
    }

    private void setRepeatingRequest() {
        if (this.camera != null && this.captureSession != null) {
            try {
                this.captureSession.setRepeatingRequest(this.previewBuilder.build(), this.previewCaptureCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
        }
    }

    private void capture() {
        if (this.camera != null && this.captureSession != null) {
            try {
                this.captureSession.capture(this.previewBuilder.build(), this.previewCaptureCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
        }
    }

    private void createPreviewRequest() {
        if (this.camera != null) {
            try {
                this.previewBuilder = this.camera.createCaptureRequest(1);
                this.camera_settings.setupBuilder(this.previewBuilder, false);
            } catch (CameraAccessException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
        }
    }

    private Surface getPreviewSurface() {
        return this.surface_texture;
    }

    private void createCaptureSession(MediaRecorder video_recorder) {
        if (this.captureSession != null) {
            this.captureSession.close();
            this.captureSession = null;
        }
        try {
            AnonymousClass2MyStateCallback myStateCallback;
            Surface preview_surface;
            Surface capture_surface;
            this.captureSession = null;
            if (video_recorder == null) {
                createPictureImageReader();
            } else if (this.imageReader != null) {
                this.imageReader.close();
                this.imageReader = null;
            }
            if (this.texture != null) {
                if (this.preview_width == 0 || this.preview_height == 0) {
                    throw new RuntimeException();
                }
                this.texture.setDefaultBufferSize(this.preview_width, this.preview_height);
            }
            if (video_recorder != null) {
                myStateCallback = new CameraCaptureSession.StateCallback() {
                    boolean callback_done = false;

                    public void onConfigured(CameraCaptureSession session) {
                        if (CameraController2.this.camera == null) {
                            this.callback_done = true;
                            return;
                        }
                        CameraController2.this.captureSession = session;
                        CameraController2.this.previewBuilder.addTarget(CameraController2.this.getPreviewSurface());
                        CameraController2.this.setRepeatingRequest();
                        this.callback_done = true;
                    }

                    public void onConfigureFailed(CameraCaptureSession session) {
                        this.callback_done = true;
                    }
                };
                preview_surface = getPreviewSurface();
            } else {
                myStateCallback = /* anonymous class already generated */;
                preview_surface = getPreviewSurface();
            }
            if (video_recorder != null) {
                capture_surface = video_recorder.getSurface();
            } else {
                capture_surface = this.imageReader.getSurface();
            }
            this.camera.createCaptureSession(Arrays.asList(new Surface[]{preview_surface, capture_surface}), myStateCallback, this.handler);
            do {
            } while (!myStateCallback.callback_done);
            if (this.captureSession == null) {
                throw new RuntimeException();
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    void startPreview() {
        if (this.captureSession != null) {
            setRepeatingRequest();
        } else {
            createCaptureSession(null);
        }
    }

    void stopPreview() {
        if (this.camera != null && this.captureSession != null) {
            try {
                this.captureSession.stopRepeating();
                this.captureSession.close();
                this.captureSession = null;
            } catch (CameraAccessException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
        }
    }

    public boolean startFaceDetection() {
        if (this.previewBuilder.get(CaptureRequest.STATISTICS_FACE_DETECT_MODE) != null && ((Integer) this.previewBuilder.get(CaptureRequest.STATISTICS_FACE_DETECT_MODE)).intValue() == 1) {
            return false;
        }
        this.camera_settings.has_face_detect_mode = true;
        this.camera_settings.face_detect_mode = 1;
        this.camera_settings.setFaceDetectMode(this.previewBuilder);
        setRepeatingRequest();
        return true;
    }

    void setFaceDetectionListener(FaceDetectionListener listener) {
        this.face_detection_listener = listener;
    }

    void autoFocus(AutoFocusCallback cb) {
        if (this.camera == null || this.captureSession == null) {
            cb.onAutoFocus(false);
            return;
        }
        this.previewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(1));
        capture();
        this.autofocus_cb = cb;
        this.previewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(0));
    }

    void cancelAutoFocus() {
        if (this.camera != null && this.captureSession != null) {
            this.previewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(2));
            capture();
            this.previewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(0));
            this.autofocus_cb = null;
        }
    }

    void takePicture(PictureCallback raw, PictureCallback jpeg) {
        if (this.camera == null || this.captureSession == null) {
            throw new RuntimeException();
        }
        try {
            Builder stillBuilder = this.camera.createCaptureRequest(2);
            this.camera_settings.setupBuilder(stillBuilder, true);
            stillBuilder.addTarget(this.imageReader.getSurface());
            CaptureCallback stillCaptureCallback = new CaptureCallback() {
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                }
            };
            this.jpeg_cb = jpeg;
            this.captureSession.capture(stillBuilder.build(), stillCaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    void setDisplayOrientation(int degrees) {
    }

    int getDisplayOrientation() {
        return 0;
    }

    int getCameraOrientation() {
        return ((Integer) this.characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)).intValue();
    }

    boolean isFrontFacing() {
        return ((Integer) this.characteristics.get(CameraCharacteristics.LENS_FACING)).intValue() == 0;
    }

    void unlock() {
    }

    void initVideoRecorderPrePrepare(MediaRecorder video_recorder) {
    }

    void initVideoRecorderPostPrepare(MediaRecorder video_recorder) {
        try {
            Surface surface = video_recorder.getSurface();
            this.previewBuilder = this.camera.createCaptureRequest(3);
            this.camera_settings.setupBuilder(this.previewBuilder, false);
            this.previewBuilder.addTarget(surface);
            createCaptureSession(video_recorder);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    void reconnect() throws IOException {
        createPreviewRequest();
        createCaptureSession(null);
    }

    String getParametersString() {
        return null;
    }
}
