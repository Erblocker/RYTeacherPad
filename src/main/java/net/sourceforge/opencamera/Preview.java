package net.sourceforge.opencamera;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.CamcorderProfile;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import android.widget.ZoomControls;
import com.foxit.sdk.pdf.annots.Markup;
import eu.janmuller.android.simplecropimage.CropImage;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.ThumbnailUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import net.sourceforge.opencamera.CameraController.Size;
import org.ksoap2.SoapEnvelope;

public class Preview implements Callback, SurfaceTextureListener {
    private static final int FOCUS_DONE = 3;
    private static final int FOCUS_FAILED = 2;
    private static final int FOCUS_SUCCESS = 1;
    private static final int FOCUS_WAITING = 0;
    private static final String TAG = "Preview";
    private static final String TAG_GPS_IMG_DIRECTION = "GPSImgDirection";
    private static final String TAG_GPS_IMG_DIRECTION_REF = "GPSImgDirectionRef";
    private final int PHASE_NORMAL = 0;
    private final int PHASE_PREVIEW_PAUSED = 3;
    private final int PHASE_TAKING_PHOTO = 2;
    private final int PHASE_TIMER = 1;
    private boolean app_is_paused = true;
    private double aspect_ratio = 0.0d;
    private float battery_frac = 0.0f;
    private IntentFilter battery_ifilter = new IntentFilter("android.intent.action.BATTERY_CHANGED");
    private Timer beepTimer = new Timer();
    private TimerTask beepTimerTask = null;
    private int cameraId = 0;
    private float[] cameraRotation = new float[9];
    private CameraSurface cameraSurface = null;
    private CameraController camera_controller = null;
    private CameraControllerManager camera_controller_manager = null;
    private Matrix camera_to_preview_matrix = new Matrix();
    private boolean can_disable_shutter_sound = false;
    private CanvasView canvasView = null;
    private ToastBoxer change_exposure_toast = new ToastBoxer();
    private List<String> color_effects = null;
    public int count_cameraAutoFocus = 0;
    public int count_cameraStartPreview = 0;
    public int count_cameraTakePicture = 0;
    private int current_flash_index = -1;
    private int current_focus_index = -1;
    private int current_orientation = 0;
    private int current_rotation = 0;
    private int current_size_index = -1;
    private int current_video_quality = -1;
    private DecimalFormat decimalFormat = new DecimalFormat("#0.0");
    private float[] deviceInclination = new float[9];
    private float[] deviceRotation = new float[9];
    private ToastBoxer exposure_lock_toast = new ToastBoxer();
    private float exposure_step = 0.0f;
    private List<String> exposures = null;
    private RectF face_rect = new RectF();
    private Face[] faces_detected = null;
    private Timer flashVideoTimer = new Timer();
    private TimerTask flashVideoTimerTask = null;
    private ToastBoxer flash_toast = new ToastBoxer();
    private long focus_complete_time = -1;
    private int focus_screen_x = 0;
    private int focus_screen_y = 0;
    private int focus_success = 3;
    private ToastBoxer focus_toast = new ToastBoxer();
    private float free_memory_gb = -1.0f;
    private float[] geo_direction = new float[3];
    private float[] geomagnetic = new float[3];
    private float[] gravity = new float[3];
    private int[] gui_location = new int[2];
    private boolean has_aspect_ratio = false;
    private boolean has_battery_frac = false;
    private boolean has_focus_area = false;
    private boolean has_geo_direction = false;
    private boolean has_geomagnetic = false;
    private boolean has_gravity = false;
    private boolean has_level_angle = false;
    private boolean has_surface = false;
    private boolean has_zoom = false;
    private boolean immersive_mode = false;
    private boolean is_exposure_lock_supported = false;
    private boolean is_exposure_locked = false;
    private boolean is_preview_started = false;
    private boolean is_video = false;
    private List<String> isos = null;
    private long last_battery_time = 0;
    private long last_free_memory_time = 0;
    private double level_angle = 0.0d;
    private Bitmap location_bitmap = null;
    private Rect location_dest = new Rect();
    private Bitmap location_off_bitmap = null;
    private int max_exposure = 0;
    private int max_num_focus_areas = 0;
    private int max_zoom_factor = 0;
    private int min_exposure = 0;
    private double orig_level_angle = 0.0d;
    private Paint p = new Paint();
    private int phase = 0;
    private int preview_h = 0;
    private String preview_image_name = null;
    private double preview_targetRatio = 0.0d;
    private Matrix preview_to_camera_matrix = new Matrix();
    private int preview_w = 0;
    private int remaining_burst_photos = 0;
    private int remaining_restart_video = 0;
    private Timer restartVideoTimer = new Timer();
    private TimerTask restartVideoTimerTask = null;
    private ScaleGestureDetector scaleGestureDetector;
    private List<String> scene_modes = null;
    private final float sensor_alpha = 0.8f;
    private String set_flash_value_after_autofocus = "";
    private boolean set_preview_size = false;
    private boolean set_textureview_size = false;
    private boolean show_gui = true;
    private List<Size> sizes = null;
    private ToastBoxer stopstart_video_toast = new ToastBoxer();
    private boolean successfully_focused = false;
    private long successfully_focused_time = -1;
    private List<String> supported_flash_values = null;
    private List<String> supported_focus_values = null;
    private List<Size> supported_preview_sizes = null;
    private boolean supports_face_detection = false;
    private boolean supports_video_stabilization = false;
    private ToastBoxer switch_camera_toast = new ToastBoxer();
    private ToastBoxer switch_video_toast = new ToastBoxer();
    private Timer takePictureTimer = new Timer();
    private TimerTask takePictureTimerTask = null;
    private long take_photo_time = 0;
    private ToastBoxer take_photo_toast = new ToastBoxer();
    public float test_angle = 0.0f;
    public boolean test_fail_open_camera = false;
    public boolean test_have_angle = false;
    public String test_last_saved_image = null;
    public boolean test_low_memory = false;
    private Rect text_bounds = new Rect();
    private int textureview_h = 0;
    private int textureview_w = 0;
    private Bitmap thumbnail = null;
    private boolean thumbnail_anim = false;
    private RectF thumbnail_anim_dst_rect = new RectF();
    private Matrix thumbnail_anim_matrix = new Matrix();
    private RectF thumbnail_anim_src_rect = new RectF();
    private long thumbnail_anim_start_ms = -1;
    private boolean touch_was_multitouch = false;
    private boolean ui_placement_right = true;
    private int ui_rotation = 0;
    private boolean using_android_l = false;
    private boolean using_face_detection = false;
    private boolean using_texture_view = false;
    private String video_name = null;
    private List<String> video_quality = null;
    private MediaRecorder video_recorder = null;
    private List<Size> video_sizes = null;
    private long video_start_time = 0;
    private boolean video_start_time_set = false;
    private List<String> white_balances = null;
    private int zoom_factor = 0;
    private List<Integer> zoom_ratios = null;

    /* renamed from: net.sourceforge.opencamera.Preview$1RotatedTextView */
    class AnonymousClass1RotatedTextView extends View {
        private Rect bounds = new Rect();
        private String[] lines = null;
        private Paint paint = new Paint();
        private RectF rect = new RectF();
        private Rect sub_bounds = new Rect();

        public AnonymousClass1RotatedTextView(String text, Context context) {
            super(context);
            this.lines = text.split("\n");
        }

        protected void onDraw(Canvas canvas) {
            float scale = getResources().getDisplayMetrics().density;
            this.paint.setTextSize((14.0f * scale) + 0.5f);
            this.paint.setShadowLayer(1.0f, 0.0f, 1.0f, -16777216);
            boolean first_line = true;
            for (String line : this.lines) {
                this.paint.getTextBounds(line, 0, line.length(), this.sub_bounds);
                if (first_line) {
                    this.bounds.set(this.sub_bounds);
                    first_line = false;
                } else {
                    this.bounds.top = Math.min(this.sub_bounds.top, this.bounds.top);
                    this.bounds.bottom = Math.max(this.sub_bounds.bottom, this.bounds.bottom);
                    this.bounds.left = Math.min(this.sub_bounds.left, this.bounds.left);
                    this.bounds.right = Math.max(this.sub_bounds.right, this.bounds.right);
                }
            }
            int height = (this.bounds.bottom - this.bounds.top) + 2;
            Rect rect = this.bounds;
            rect.bottom += ((this.lines.length - 1) * height) / 2;
            rect = this.bounds;
            rect.top -= ((this.lines.length - 1) * height) / 2;
            int padding = (int) ((14.0f * scale) + 0.5f);
            int offset_y = (int) ((32.0f * scale) + 0.5f);
            canvas.save();
            canvas.rotate((float) Preview.this.ui_rotation, (float) (canvas.getWidth() / 2), (float) (canvas.getHeight() / 2));
            this.rect.left = (float) ((((canvas.getWidth() / 2) - (this.bounds.width() / 2)) + this.bounds.left) - padding);
            this.rect.top = (float) ((((canvas.getHeight() / 2) + this.bounds.top) - padding) + offset_y);
            this.rect.right = (float) ((((canvas.getWidth() / 2) - (this.bounds.width() / 2)) + this.bounds.right) + padding);
            this.rect.bottom = (float) ((((canvas.getHeight() / 2) + this.bounds.bottom) + padding) + offset_y);
            this.paint.setStyle(Style.FILL);
            this.paint.setColor(Color.rgb(50, 50, 50));
            float radius = (24.0f * scale) + 0.5f;
            canvas.drawRoundRect(this.rect, radius, radius, this.paint);
            this.paint.setColor(-1);
            int ypos = ((canvas.getHeight() / 2) + offset_y) - (((this.lines.length - 1) * height) / 2);
            for (String line2 : this.lines) {
                canvas.drawText(line2, (float) ((canvas.getWidth() / 2) - (this.bounds.width() / 2)), (float) ypos, this.paint);
                ypos += height;
            }
            canvas.restore();
        }
    }

    private class ScaleListener extends SimpleOnScaleGestureListener {
        private ScaleListener() {
        }

        public boolean onScale(ScaleGestureDetector detector) {
            if (Preview.this.camera_controller != null && Preview.this.has_zoom) {
                Preview.this.scaleZoom(detector.getScaleFactor());
            }
            return true;
        }
    }

    Preview(Context context, Bundle savedInstanceState) {
        int i = VERSION.SDK_INT;
        if (this.using_android_l) {
            this.using_texture_view = true;
        }
        if (this.using_texture_view) {
            this.cameraSurface = new MyTextureView(context, savedInstanceState, this);
            this.canvasView = new CanvasView(context, savedInstanceState, this);
            this.camera_controller_manager = new CameraControllerManager2(context);
        } else {
            this.cameraSurface = new MySurfaceView(context, savedInstanceState, this);
            this.camera_controller_manager = new CameraControllerManager1();
        }
        this.scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        if (savedInstanceState != null) {
            this.cameraId = savedInstanceState.getInt("cameraId", 0);
            if (this.cameraId < 0 || this.cameraId >= this.camera_controller_manager.getNumberOfCameras()) {
                this.cameraId = 0;
            }
            this.zoom_factor = savedInstanceState.getInt("zoom_factor", 0);
        }
        this.location_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.earth);
        this.location_off_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.earth_off);
        Activity activity = (Activity) context;
        ((ViewGroup) activity.findViewById(R.id.preview)).addView(this.cameraSurface.getView());
        if (this.canvasView != null) {
            ((ViewGroup) activity.findViewById(R.id.preview)).addView(this.canvasView);
        }
    }

    private Resources getResources() {
        return this.cameraSurface.getView().getResources();
    }

    public View getView() {
        return this.cameraSurface.getView();
    }

    private void calculateCameraToPreviewMatrix() {
        if (this.camera_controller != null) {
            this.camera_to_preview_matrix.reset();
            this.camera_to_preview_matrix.setScale((float) (this.camera_controller.isFrontFacing() ? -1 : 1), 1.0f);
            if (!this.using_texture_view) {
                this.camera_to_preview_matrix.postRotate((float) this.camera_controller.getDisplayOrientation());
            }
            this.camera_to_preview_matrix.postScale(((float) this.cameraSurface.getView().getWidth()) / 2000.0f, ((float) this.cameraSurface.getView().getHeight()) / 2000.0f);
            this.camera_to_preview_matrix.postTranslate(((float) this.cameraSurface.getView().getWidth()) / 2.0f, ((float) this.cameraSurface.getView().getHeight()) / 2.0f);
        }
    }

    private void calculatePreviewToCameraMatrix() {
        if (this.camera_controller != null) {
            calculateCameraToPreviewMatrix();
            if (!this.camera_to_preview_matrix.invert(this.preview_to_camera_matrix)) {
            }
        }
    }

    private ArrayList<Area> getAreas(float x, float y) {
        float[] coords = new float[]{x, y};
        calculatePreviewToCameraMatrix();
        this.preview_to_camera_matrix.mapPoints(coords);
        float focus_x = coords[0];
        float focus_y = coords[1];
        Rect rect = new Rect();
        rect.left = ((int) focus_x) - 50;
        rect.right = ((int) focus_x) + 50;
        rect.top = ((int) focus_y) - 50;
        rect.bottom = ((int) focus_y) + 50;
        if (rect.left < -1000) {
            rect.left = -1000;
            rect.right = rect.left + 100;
        } else if (rect.right > 1000) {
            rect.right = 1000;
            rect.left = rect.right - 100;
        }
        if (rect.top < -1000) {
            rect.top = -1000;
            rect.bottom = rect.top + 100;
        } else if (rect.bottom > 1000) {
            rect.bottom = 1000;
            rect.top = rect.bottom - 100;
        }
        ArrayList<Area> areas = new ArrayList();
        areas.add(new Area(rect, 1000));
        return areas;
    }

    boolean touchEvent(MotionEvent event) {
        this.scaleGestureDetector.onTouchEvent(event);
        if (this.camera_controller == null) {
            openCamera();
        } else {
            MainActivity main_activity = (MainActivity) getContext();
            main_activity.clearSeekBar();
            main_activity.closePopup();
            if (main_activity.usingKitKatImmersiveMode()) {
                main_activity.setImmersiveMode(false);
            }
            if (event.getPointerCount() != 1) {
                this.touch_was_multitouch = true;
            } else if (event.getAction() != 1) {
                if (event.getAction() == 0 && event.getPointerCount() == 1) {
                    this.touch_was_multitouch = false;
                }
            } else if (!this.touch_was_multitouch && (this.is_video || !isTakingPhotoOrOnTimer())) {
                if (!this.is_video) {
                    startCameraPreview();
                }
                cancelAutoFocus();
                if (!(this.camera_controller == null || this.using_face_detection)) {
                    this.has_focus_area = false;
                    if (this.camera_controller.setFocusAndMeteringArea(getAreas(event.getX(), event.getY()))) {
                        this.has_focus_area = true;
                        this.focus_screen_x = (int) event.getX();
                        this.focus_screen_y = (int) event.getY();
                    }
                }
                tryAutoFocus(false, true);
            }
        }
        return true;
    }

    public void clearFocusAreas() {
        if (this.camera_controller != null) {
            cancelAutoFocus();
            this.camera_controller.clearFocusAndMetering();
            this.has_focus_area = false;
            this.focus_success = 3;
            this.successfully_focused = false;
        }
    }

    protected void getMeasureSpec(int[] spec, int widthSpec, int heightSpec) {
        if (hasAspectRatio()) {
            int longSide;
            int shortSide;
            double aspect_ratio = getAspectRatio();
            int hPadding = this.cameraSurface.getView().getPaddingLeft() + this.cameraSurface.getView().getPaddingRight();
            int vPadding = this.cameraSurface.getView().getPaddingTop() + this.cameraSurface.getView().getPaddingBottom();
            int previewWidth = MeasureSpec.getSize(widthSpec) - hPadding;
            int previewHeight = MeasureSpec.getSize(heightSpec) - vPadding;
            boolean widthLonger = previewWidth > previewHeight;
            if (widthLonger) {
                longSide = previewWidth;
            } else {
                longSide = previewHeight;
            }
            if (widthLonger) {
                shortSide = previewHeight;
            } else {
                shortSide = previewWidth;
            }
            if (((double) longSide) > ((double) shortSide) * aspect_ratio) {
                longSide = (int) (((double) shortSide) * aspect_ratio);
            } else {
                shortSide = (int) (((double) longSide) / aspect_ratio);
            }
            if (widthLonger) {
                previewWidth = longSide;
                previewHeight = shortSide;
            } else {
                previewWidth = shortSide;
                previewHeight = longSide;
            }
            previewHeight += vPadding;
            spec[0] = MeasureSpec.makeMeasureSpec(previewWidth + hPadding, 1073741824);
            spec[1] = MeasureSpec.makeMeasureSpec(previewHeight, 1073741824);
            return;
        }
        spec[0] = widthSpec;
        spec[1] = heightSpec;
    }

    private void mySurfaceCreated() {
        this.has_surface = true;
        openCamera();
    }

    private void mySurfaceDestroyed() {
        this.has_surface = false;
        closeCamera();
    }

    private void mySurfaceChanged() {
        if (this.camera_controller != null) {
            ((MainActivity) getContext()).layoutUI();
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        mySurfaceCreated();
        this.cameraSurface.getView().setWillNotDraw(false);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mySurfaceDestroyed();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (holder.getSurface() != null) {
            mySurfaceChanged();
        }
    }

    public void onSurfaceTextureAvailable(SurfaceTexture arg0, int width, int height) {
        this.set_textureview_size = true;
        this.textureview_w = width;
        this.textureview_h = height;
        mySurfaceCreated();
        configureTransform();
    }

    public boolean onSurfaceTextureDestroyed(SurfaceTexture arg0) {
        this.set_textureview_size = false;
        this.textureview_w = 0;
        this.textureview_h = 0;
        mySurfaceDestroyed();
        return true;
    }

    public void onSurfaceTextureSizeChanged(SurfaceTexture arg0, int width, int height) {
        this.set_textureview_size = true;
        this.textureview_w = width;
        this.textureview_h = height;
        mySurfaceChanged();
        configureTransform();
    }

    public void onSurfaceTextureUpdated(SurfaceTexture arg0) {
    }

    private void configureTransform() {
        if (this.camera_controller != null && this.set_preview_size && this.set_textureview_size) {
            int rotation = getDisplayRotation();
            Matrix matrix = new Matrix();
            RectF viewRect = new RectF(0.0f, 0.0f, (float) this.textureview_w, (float) this.textureview_h);
            RectF bufferRect = new RectF(0.0f, 0.0f, (float) this.preview_h, (float) this.preview_w);
            float centerX = viewRect.centerX();
            float centerY = viewRect.centerY();
            if (1 == rotation || 3 == rotation) {
                bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
                matrix.setRectToRect(viewRect, bufferRect, ScaleToFit.FILL);
                float scale = Math.max(((float) this.textureview_h) / ((float) this.preview_h), ((float) this.textureview_w) / ((float) this.preview_w));
                matrix.postScale(scale, scale, centerX, centerY);
                matrix.postRotate((float) ((rotation - 2) * 90), centerX, centerY);
            }
            this.cameraSurface.setTransform(matrix);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void stopVideo(boolean from_restart) {
        final MainActivity main_activity = (MainActivity) getContext();
        main_activity.unlockScreen();
        if (this.restartVideoTimerTask != null) {
            this.restartVideoTimerTask.cancel();
            this.restartVideoTimerTask = null;
        }
        if (this.flashVideoTimerTask != null) {
            this.flashVideoTimerTask.cancel();
            this.flashVideoTimerTask = null;
        }
        if (!from_restart) {
            this.remaining_restart_video = 0;
        }
        if (this.video_recorder != null) {
            File file;
            String toast = getResources().getString(R.string.stopped_recording_video);
            if (this.remaining_restart_video > 0) {
                toast = new StringBuilder(String.valueOf(toast)).append(" (").append(this.remaining_restart_video).append(" ").append(getResources().getString(R.string.repeats_to_go)).append(")").toString();
            }
            showToast(this.stopstart_video_toast, toast);
            this.phase = 0;
            try {
                this.video_recorder.setOnErrorListener(null);
                this.video_recorder.setOnInfoListener(null);
                this.video_recorder.stop();
            } catch (RuntimeException e) {
                if (this.video_name != null) {
                    file = new File(this.video_name);
                    if (file != null) {
                        file.delete();
                    }
                    this.video_name = null;
                }
                if (!this.video_start_time_set || System.currentTimeMillis() - this.video_start_time > 2000) {
                    showToast(null, R.string.failed_to_record_video);
                }
            }
            this.video_recorder.reset();
            this.video_recorder.release();
            this.video_recorder = null;
            reconnectCamera(false);
            if (this.video_name != null) {
                file = new File(this.video_name);
                if (file != null) {
                    main_activity.broadcastFile(file, false, true);
                    if (main_activity.getIntent() != null && main_activity.getIntent().getAction().equalsIgnoreCase("android.media.action.VIDEO_CAPTURE")) {
                        main_activity.setResult(-1, new Intent().setData(Uri.fromFile(file)));
                        main_activity.finish();
                    }
                }
                long time_s = System.currentTimeMillis();
                Bitmap old_thumbnail = this.thumbnail;
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                try {
                    retriever.setDataSource(this.video_name);
                    this.thumbnail = retriever.getFrameAtTime(-1);
                    try {
                        retriever.release();
                    } catch (RuntimeException e2) {
                    }
                } catch (IllegalArgumentException e3) {
                } catch (RuntimeException e4) {
                    try {
                        retriever.release();
                    } catch (RuntimeException e5) {
                    }
                } catch (Throwable th) {
                    try {
                        retriever.release();
                    } catch (RuntimeException e6) {
                    }
                }
                if (!(this.thumbnail == null || this.thumbnail == old_thumbnail)) {
                    ImageButton galleryButton = (ImageButton) main_activity.findViewById(R.id.gallery);
                    if (galleryButton.getVisibility() == 0) {
                        int width = this.thumbnail.getWidth();
                        int height = this.thumbnail.getHeight();
                        if (width > galleryButton.getWidth()) {
                            float scale = ((float) galleryButton.getWidth()) / ((float) width);
                            Bitmap scaled_thumbnail = Bitmap.createScaledBitmap(this.thumbnail, Math.round(((float) width) * scale), Math.round(((float) height) * scale), true);
                            if (scaled_thumbnail != this.thumbnail) {
                                this.thumbnail.recycle();
                                this.thumbnail = scaled_thumbnail;
                            }
                        }
                        main_activity.runOnUiThread(new Runnable() {
                            public void run() {
                                main_activity.updateGalleryIconToBitmap(Preview.this.thumbnail);
                            }
                        });
                        if (old_thumbnail != null) {
                            old_thumbnail.recycle();
                        }
                    }
                }
                this.video_name = null;
            }
        }
    }

    private Context getContext() {
        return this.cameraSurface.getView().getContext();
    }

    private void restartVideo() {
        if (this.video_recorder != null) {
            stopVideo(true);
            if (this.remaining_restart_video <= 0) {
                return;
            }
            if (this.is_video) {
                takePicture();
                this.remaining_restart_video--;
                return;
            }
            this.remaining_restart_video = 0;
        }
    }

    private void reconnectCamera(boolean quiet) {
        if (this.camera_controller != null) {
            try {
                this.camera_controller.reconnect();
                setPreviewPaused(false);
            } catch (IOException e) {
                e.printStackTrace();
                showToast(null, R.string.failed_to_reconnect_camera);
                closeCamera();
            }
            try {
                tryAutoFocus(false, false);
            } catch (RuntimeException e2) {
                e2.printStackTrace();
                this.is_preview_started = false;
                this.camera_controller.release();
                this.camera_controller = null;
                if (!quiet) {
                    String features = getErrorFeatures(getCamcorderProfile());
                    String error_message = getResources().getString(R.string.video_may_be_corrupted);
                    if (features.length() > 0) {
                        error_message = new StringBuilder(String.valueOf(error_message)).append(", ").append(features).append(" ").append(getResources().getString(R.string.not_supported)).toString();
                    }
                    showToast(null, error_message);
                }
                openCamera();
            }
        }
    }

    private void closeCamera() {
        this.has_focus_area = false;
        this.focus_success = 3;
        this.set_flash_value_after_autofocus = "";
        this.successfully_focused = false;
        this.preview_targetRatio = 0.0d;
        ((MainActivity) getContext()).clearSeekBar();
        cancelTimer();
        if (this.camera_controller != null) {
            if (this.video_recorder != null) {
                stopVideo(false);
            }
            if (this.is_video) {
                updateFocusForVideo(false);
            }
            if (this.camera_controller != null) {
                pausePreview();
                this.camera_controller.release();
                this.camera_controller = null;
            }
        }
    }

    void cancelTimer() {
        if (isOnTimer()) {
            this.takePictureTimerTask.cancel();
            this.takePictureTimerTask = null;
            if (this.beepTimerTask != null) {
                this.beepTimerTask.cancel();
                this.beepTimerTask = null;
            }
            this.phase = 0;
        }
    }

    void pausePreview() {
        if (this.camera_controller != null) {
            if (this.is_video) {
                updateFocusForVideo(false);
            }
            setPreviewPaused(false);
            this.camera_controller.stopPreview();
            this.phase = 0;
            this.is_preview_started = false;
            showGUI(true);
        }
    }

    private void openCamera() {
        openCamera(null);
    }

    private void openCamera(String toast_message) {
        int i = 0;
        this.is_preview_started = false;
        this.set_preview_size = false;
        this.preview_w = 0;
        this.preview_h = 0;
        this.has_focus_area = false;
        this.focus_success = 3;
        this.set_flash_value_after_autofocus = "";
        this.successfully_focused = false;
        this.preview_targetRatio = 0.0d;
        this.scene_modes = null;
        this.has_zoom = false;
        this.max_zoom_factor = 0;
        this.zoom_ratios = null;
        this.faces_detected = null;
        this.supports_face_detection = false;
        this.using_face_detection = false;
        this.supports_video_stabilization = false;
        this.can_disable_shutter_sound = false;
        this.color_effects = null;
        this.white_balances = null;
        this.isos = null;
        this.exposures = null;
        this.min_exposure = 0;
        this.max_exposure = 0;
        this.exposure_step = 0.0f;
        this.sizes = null;
        this.current_size_index = -1;
        this.video_quality = null;
        this.current_video_quality = -1;
        this.supported_flash_values = null;
        this.current_flash_index = -1;
        this.supported_focus_values = null;
        this.current_focus_index = -1;
        this.max_num_focus_areas = 0;
        showGUI(true);
        if (this.has_surface && !this.app_is_paused) {
            try {
                if (this.test_fail_open_camera) {
                    throw new RuntimeException();
                }
                if (this.using_android_l) {
                    this.camera_controller = new CameraController2(getContext(), this.cameraId);
                } else {
                    this.camera_controller = new CameraController1(this.cameraId);
                }
                boolean take_photo = false;
                if (this.camera_controller != null) {
                    Activity activity = (Activity) getContext();
                    if (!(activity.getIntent() == null || activity.getIntent().getExtras() == null)) {
                        take_photo = activity.getIntent().getExtras().getBoolean(TakePhoto.TAKE_PHOTO);
                        activity.getIntent().removeExtra(TakePhoto.TAKE_PHOTO);
                    }
                    setCameraDisplayOrientation();
                    new OrientationEventListener(activity) {
                        public void onOrientationChanged(int orientation) {
                            Preview.this.onOrientationChanged(orientation);
                        }
                    }.enable();
                    this.cameraSurface.setPreviewDisplay(this.camera_controller);
                    View switchCameraButton = activity.findViewById(R.id.switch_camera);
                    if (this.camera_controller_manager.getNumberOfCameras() <= 1 || this.immersive_mode) {
                        i = 8;
                    }
                    switchCameraButton.setVisibility(i);
                    setupCamera(toast_message, take_photo);
                }
                setPopupIcon();
            } catch (RuntimeException e) {
                e.printStackTrace();
                this.camera_controller = null;
            }
        }
    }

    void setupCamera(String toast_message, boolean take_photo) {
        if (this.camera_controller != null) {
            if (this.is_video) {
                updateFocusForVideo(false);
            }
            setupCameraParameters();
            if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(MainActivity.getIsVideoPreferenceKey(), false) != this.is_video) {
                switchVideo(false, false);
            } else if (toast_message == null) {
                showPhotoVideoToast();
            } else if (toast_message.length() > 0) {
                showToast(null, toast_message);
            }
            setPreviewSize();
            startCameraPreview();
            if (this.has_zoom && this.zoom_factor != 0) {
                int new_zoom_factor = this.zoom_factor;
                this.zoom_factor = 0;
                zoomTo(new_zoom_factor, true);
            }
            if (take_photo) {
                if (this.is_video) {
                    switchVideo(true, true);
                }
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        Preview.this.takePicture();
                    }
                }, 500);
                return;
            }
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    Preview.this.tryAutoFocus(true, false);
                }
            }, 500);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setupCameraParameters() {
        Size size;
        int i;
        int i2;
        Size current_size;
        Activity activity = (Activity) getContext();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SupportedValues supported_values = this.camera_controller.setSceneMode(sharedPreferences.getString(MainActivity.getSceneModePreferenceKey(), this.camera_controller.getDefaultSceneMode()));
        if (supported_values != null) {
            this.scene_modes = supported_values.values;
            Editor editor = sharedPreferences.edit();
            editor.putString(MainActivity.getSceneModePreferenceKey(), supported_values.selected_value);
            editor.apply();
        }
        CameraFeatures camera_features = this.camera_controller.getCameraFeatures();
        this.has_zoom = camera_features.is_zoom_supported;
        if (this.has_zoom) {
            this.max_zoom_factor = camera_features.max_zoom;
            this.zoom_ratios = camera_features.zoom_ratios;
        }
        this.supports_face_detection = camera_features.supports_face_detection;
        this.sizes = camera_features.picture_sizes;
        this.supported_flash_values = camera_features.supported_flash_values;
        this.supported_focus_values = camera_features.supported_focus_values;
        this.max_num_focus_areas = camera_features.max_num_focus_areas;
        this.is_exposure_lock_supported = camera_features.is_exposure_lock_supported;
        this.supports_video_stabilization = camera_features.is_video_stabilization_supported;
        this.can_disable_shutter_sound = camera_features.can_disable_shutter_sound;
        this.min_exposure = camera_features.min_exposure;
        this.max_exposure = camera_features.max_exposure;
        this.exposure_step = camera_features.exposure_step;
        this.video_sizes = camera_features.video_sizes;
        this.supported_preview_sizes = camera_features.preview_sizes;
        MainActivity main_activity = (MainActivity) getContext();
        if (main_activity.supportsForceVideo4K() && this.video_sizes != null) {
            for (Size size2 : this.video_sizes) {
                if (size2.width >= 3840 && size2.height >= 2160) {
                    main_activity.disableForceVideo4K();
                }
            }
        }
        ZoomControls zoomControls = (ZoomControls) activity.findViewById(R.id.zoom);
        SeekBar zoomSeekBar = (SeekBar) activity.findViewById(R.id.zoom_seekbar);
        if (this.has_zoom) {
            if (sharedPreferences.getBoolean(MainActivity.getShowZoomControlsPreferenceKey(), false)) {
                zoomControls.setIsZoomInEnabled(true);
                zoomControls.setIsZoomOutEnabled(true);
                zoomControls.setZoomSpeed(20);
                zoomControls.setOnZoomInClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        Preview.this.zoomIn();
                    }
                });
                zoomControls.setOnZoomOutClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        Preview.this.zoomOut();
                    }
                });
                if (!this.immersive_mode) {
                    zoomControls.setVisibility(0);
                }
            } else {
                zoomControls.setVisibility(4);
            }
            zoomSeekBar.setMax(this.max_zoom_factor);
            zoomSeekBar.setProgress(this.max_zoom_factor - this.zoom_factor);
            zoomSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    Preview.this.zoomTo(Preview.this.max_zoom_factor - progress, false);
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            if (!sharedPreferences.getBoolean(MainActivity.getShowZoomSliderControlsPreferenceKey(), true)) {
                zoomSeekBar.setVisibility(4);
            } else if (!this.immersive_mode) {
                zoomSeekBar.setVisibility(0);
            }
        } else {
            zoomControls.setVisibility(8);
            zoomSeekBar.setVisibility(8);
        }
        this.faces_detected = null;
        if (this.supports_face_detection) {
            this.using_face_detection = sharedPreferences.getBoolean(MainActivity.getFaceDetectionPreferenceKey(), false);
        } else {
            this.using_face_detection = false;
        }
        if (this.using_face_detection) {
            this.camera_controller.setFaceDetectionListener(new FaceDetectionListener() {
                public void onFaceDetection(Face[] faces) {
                    Preview.this.faces_detected = new Face[faces.length];
                    System.arraycopy(faces, 0, Preview.this.faces_detected, 0, faces.length);
                }
            });
        }
        if (this.supports_video_stabilization) {
            this.camera_controller.setVideoStabilization(sharedPreferences.getBoolean(MainActivity.getVideoStabilizationPreferenceKey(), false));
        }
        supported_values = this.camera_controller.setColorEffect(sharedPreferences.getString(MainActivity.getColorEffectPreferenceKey(), this.camera_controller.getDefaultColorEffect()));
        if (supported_values != null) {
            this.color_effects = supported_values.values;
            editor = sharedPreferences.edit();
            editor.putString(MainActivity.getColorEffectPreferenceKey(), supported_values.selected_value);
            editor.apply();
        }
        supported_values = this.camera_controller.setWhiteBalance(sharedPreferences.getString(MainActivity.getWhiteBalancePreferenceKey(), this.camera_controller.getDefaultWhiteBalance()));
        if (supported_values != null) {
            this.white_balances = supported_values.values;
            editor = sharedPreferences.edit();
            editor.putString(MainActivity.getWhiteBalancePreferenceKey(), supported_values.selected_value);
            editor.apply();
        }
        supported_values = this.camera_controller.setISO(sharedPreferences.getString(MainActivity.getISOPreferenceKey(), this.camera_controller.getDefaultISO()));
        if (supported_values != null) {
            this.isos = supported_values.values;
            editor = sharedPreferences.edit();
            editor.putString(MainActivity.getISOPreferenceKey(), supported_values.selected_value);
            editor.apply();
        }
        this.exposures = null;
        if (!(this.min_exposure == 0 && this.max_exposure == 0)) {
            this.exposures = new Vector();
            for (i = this.min_exposure; i <= this.max_exposure; i++) {
                this.exposures.add(i);
            }
            int exposure = 0;
            try {
                exposure = Integer.parseInt(sharedPreferences.getString(MainActivity.getExposurePreferenceKey(), "0"));
            } catch (NumberFormatException e) {
            }
            if (exposure < this.min_exposure || exposure > this.max_exposure) {
                exposure = 0;
                if (0 < this.min_exposure || 0 > this.max_exposure) {
                    exposure = this.min_exposure;
                }
            }
            this.camera_controller.setExposureCompensation(exposure);
            editor = sharedPreferences.edit();
            editor.putString(MainActivity.getExposurePreferenceKey(), exposure);
            editor.apply();
        }
        View exposureButton = activity.findViewById(R.id.exposure);
        if (this.exposures == null || this.immersive_mode) {
            i2 = 8;
        } else {
            i2 = 0;
        }
        exposureButton.setVisibility(i2);
        this.current_size_index = -1;
        String resolution_value = sharedPreferences.getString(MainActivity.getResolutionPreferenceKey(this.cameraId), "");
        if (resolution_value.length() > 0) {
            int index = resolution_value.indexOf(32);
            if (index != -1) {
                String resolution_w_s = resolution_value.substring(0, index);
                String resolution_h_s = resolution_value.substring(index + 1);
                try {
                    int resolution_w = Integer.parseInt(resolution_w_s);
                    int resolution_h = Integer.parseInt(resolution_h_s);
                    for (i = 0; i < this.sizes.size() && this.current_size_index == -1; i++) {
                        size2 = (Size) this.sizes.get(i);
                        if (size2.width == resolution_w && size2.height == resolution_h) {
                            this.current_size_index = i;
                        }
                    }
                    if (this.current_size_index == -1) {
                    }
                } catch (NumberFormatException e2) {
                }
            }
        }
        if (this.current_size_index == -1) {
            current_size = null;
            for (i = 0; i < this.sizes.size(); i++) {
                size2 = (Size) this.sizes.get(i);
                if (current_size == null || size2.width * size2.height > current_size.width * current_size.height) {
                    this.current_size_index = i;
                    current_size = size2;
                }
            }
        }
        if (this.current_size_index != -1) {
            current_size = (Size) this.sizes.get(this.current_size_index);
            resolution_value = current_size.width + " " + current_size.height;
            editor = sharedPreferences.edit();
            editor.putString(MainActivity.getResolutionPreferenceKey(this.cameraId), resolution_value);
            editor.apply();
        }
        this.camera_controller.setJpegQuality(getImageQuality());
        initialiseVideoSizes();
        initialiseVideoQuality();
        this.current_video_quality = -1;
        String video_quality_value_s = sharedPreferences.getString(MainActivity.getVideoQualityPreferenceKey(this.cameraId), "");
        if (video_quality_value_s.length() > 0) {
            for (i = 0; i < this.video_quality.size() && this.current_video_quality == -1; i++) {
                if (((String) this.video_quality.get(i)).equals(video_quality_value_s)) {
                    this.current_video_quality = i;
                }
            }
        }
        if (this.current_video_quality == -1 && this.video_quality.size() > 0) {
            this.current_video_quality = 0;
        }
        if (this.current_video_quality != -1) {
            editor = sharedPreferences.edit();
            editor.putString(MainActivity.getVideoQualityPreferenceKey(this.cameraId), (String) this.video_quality.get(this.current_video_quality));
            editor.apply();
        }
        this.current_flash_index = -1;
        if (this.supported_flash_values == null || this.supported_flash_values.size() <= 1) {
            this.supported_flash_values = null;
        } else {
            String flash_value = sharedPreferences.getString(MainActivity.getFlashPreferenceKey(this.cameraId), "");
            if (flash_value.length() <= 0) {
                updateFlash("flash_auto", true);
            } else if (!updateFlash(flash_value, false)) {
                updateFlash(0, true);
            }
        }
        this.current_focus_index = -1;
        if (this.supported_focus_values == null || this.supported_focus_values.size() <= 1) {
            this.supported_focus_values = null;
        } else {
            String focus_value = sharedPreferences.getString(MainActivity.getFocusPreferenceKey(this.cameraId), "");
            if (focus_value.length() <= 0) {
                updateFocus("focus_mode_auto", false, true, true);
            } else if (!updateFocus(focus_value, false, false, true)) {
                updateFocus(0, false, true, true);
            }
        }
        ImageButton exposureLockButton = (ImageButton) activity.findViewById(R.id.exposure_lock);
        i2 = (!this.is_exposure_lock_supported || this.immersive_mode) ? 8 : 0;
        exposureLockButton.setVisibility(i2);
        this.is_exposure_locked = false;
        if (this.is_exposure_lock_supported) {
            exposureLockButton.setImageResource(this.is_exposure_locked ? R.drawable.exposure_locked : R.drawable.exposure_unlocked);
        }
    }

    private void setPreviewSize() {
        if (this.camera_controller != null) {
            if (this.is_preview_started) {
                throw new RuntimeException();
            }
            cancelAutoFocus();
            Size new_size = null;
            if (this.is_video) {
                CamcorderProfile profile = getCamcorderProfile();
                new_size = getOptimalVideoPictureSize(this.sizes, ((double) profile.videoFrameWidth) / ((double) profile.videoFrameHeight));
            } else if (this.current_size_index != -1) {
                new_size = (Size) this.sizes.get(this.current_size_index);
            }
            if (new_size != null) {
                this.camera_controller.setPictureSize(new_size.width, new_size.height);
            }
            if (this.supported_preview_sizes != null && this.supported_preview_sizes.size() > 0) {
                Size best_size = getOptimalPreviewSize(this.supported_preview_sizes);
                this.camera_controller.setPreviewSize(best_size.width, best_size.height);
                this.set_preview_size = true;
                this.preview_w = best_size.width;
                this.preview_h = best_size.height;
                setAspectRatio(((double) best_size.width) / ((double) best_size.height));
            }
        }
    }

    private void sortVideoSizes() {
        Collections.sort(this.video_sizes, new Comparator<Size>() {
            public int compare(Size a, Size b) {
                return (b.width * b.height) - (a.width * a.height);
            }
        });
    }

    public void setVideoSizes(List<Size> video_sizes) {
        this.video_sizes = video_sizes;
        sortVideoSizes();
    }

    private void initialiseVideoSizes() {
        if (this.camera_controller != null) {
            sortVideoSizes();
        }
    }

    private void initialiseVideoQuality() {
        SparseArray<Pair<Integer, Integer>> profiles = new SparseArray();
        if (CamcorderProfile.hasProfile(this.cameraId, 1)) {
            CamcorderProfile profile = CamcorderProfile.get(this.cameraId, 1);
            profiles.put(1, new Pair(Integer.valueOf(profile.videoFrameWidth), Integer.valueOf(profile.videoFrameHeight)));
        }
        if (CamcorderProfile.hasProfile(this.cameraId, 6)) {
            profile = CamcorderProfile.get(this.cameraId, 6);
            profiles.put(6, new Pair(Integer.valueOf(profile.videoFrameWidth), Integer.valueOf(profile.videoFrameHeight)));
        }
        if (CamcorderProfile.hasProfile(this.cameraId, 5)) {
            profile = CamcorderProfile.get(this.cameraId, 5);
            profiles.put(5, new Pair(Integer.valueOf(profile.videoFrameWidth), Integer.valueOf(profile.videoFrameHeight)));
        }
        if (CamcorderProfile.hasProfile(this.cameraId, 4)) {
            profile = CamcorderProfile.get(this.cameraId, 4);
            profiles.put(4, new Pair(Integer.valueOf(profile.videoFrameWidth), Integer.valueOf(profile.videoFrameHeight)));
        }
        if (CamcorderProfile.hasProfile(this.cameraId, 3)) {
            profile = CamcorderProfile.get(this.cameraId, 3);
            profiles.put(3, new Pair(Integer.valueOf(profile.videoFrameWidth), Integer.valueOf(profile.videoFrameHeight)));
        }
        if (CamcorderProfile.hasProfile(this.cameraId, 7)) {
            profile = CamcorderProfile.get(this.cameraId, 7);
            profiles.put(7, new Pair(Integer.valueOf(profile.videoFrameWidth), Integer.valueOf(profile.videoFrameHeight)));
        }
        if (CamcorderProfile.hasProfile(this.cameraId, 2)) {
            profile = CamcorderProfile.get(this.cameraId, 2);
            profiles.put(2, new Pair(Integer.valueOf(profile.videoFrameWidth), Integer.valueOf(profile.videoFrameHeight)));
        }
        if (CamcorderProfile.hasProfile(this.cameraId, 0)) {
            profile = CamcorderProfile.get(this.cameraId, 0);
            profiles.put(0, new Pair(Integer.valueOf(profile.videoFrameWidth), Integer.valueOf(profile.videoFrameHeight)));
        }
        initialiseVideoQualityFromProfiles(profiles);
    }

    private void addVideoResolutions(boolean[] done_video_size, int base_profile, int min_resolution_w, int min_resolution_h) {
        if (this.video_sizes != null) {
            for (int i = 0; i < this.video_sizes.size(); i++) {
                if (!done_video_size[i]) {
                    Size size = (Size) this.video_sizes.get(i);
                    if (size.width == min_resolution_w && size.height == min_resolution_h) {
                        this.video_quality.add(base_profile);
                        done_video_size[i] = true;
                    } else if (base_profile == 0 || size.width * size.height >= min_resolution_w * min_resolution_h) {
                        this.video_quality.add(base_profile + "_r" + size.width + "x" + size.height);
                        done_video_size[i] = true;
                    }
                }
            }
        }
    }

    public void initialiseVideoQualityFromProfiles(SparseArray<Pair<Integer, Integer>> profiles) {
        this.video_quality = new Vector();
        boolean[] done_video_size = null;
        if (this.video_sizes != null) {
            done_video_size = new boolean[this.video_sizes.size()];
            for (int i = 0; i < this.video_sizes.size(); i++) {
                done_video_size[i] = false;
            }
        }
        if (profiles.get(1) != null) {
            Pair<Integer, Integer> pair = (Pair) profiles.get(1);
            addVideoResolutions(done_video_size, 1, ((Integer) pair.first).intValue(), ((Integer) pair.second).intValue());
        }
        if (profiles.get(6) != null) {
            pair = (Pair) profiles.get(6);
            addVideoResolutions(done_video_size, 6, ((Integer) pair.first).intValue(), ((Integer) pair.second).intValue());
        }
        if (profiles.get(5) != null) {
            pair = (Pair) profiles.get(5);
            addVideoResolutions(done_video_size, 5, ((Integer) pair.first).intValue(), ((Integer) pair.second).intValue());
        }
        if (profiles.get(4) != null) {
            pair = (Pair) profiles.get(4);
            addVideoResolutions(done_video_size, 4, ((Integer) pair.first).intValue(), ((Integer) pair.second).intValue());
        }
        if (profiles.get(3) != null) {
            pair = (Pair) profiles.get(3);
            addVideoResolutions(done_video_size, 3, ((Integer) pair.first).intValue(), ((Integer) pair.second).intValue());
        }
        if (profiles.get(7) != null) {
            pair = (Pair) profiles.get(7);
            addVideoResolutions(done_video_size, 7, ((Integer) pair.first).intValue(), ((Integer) pair.second).intValue());
        }
        if (profiles.get(2) != null) {
            pair = (Pair) profiles.get(2);
            addVideoResolutions(done_video_size, 2, ((Integer) pair.first).intValue(), ((Integer) pair.second).intValue());
        }
        if (profiles.get(0) != null) {
            pair = (Pair) profiles.get(0);
            addVideoResolutions(done_video_size, 0, ((Integer) pair.first).intValue(), ((Integer) pair.second).intValue());
        }
    }

    private CamcorderProfile getCamcorderProfile(String quality) {
        CamcorderProfile camcorder_profile = CamcorderProfile.get(this.cameraId, 1);
        String profile_string = quality;
        try {
            int index = profile_string.indexOf(95);
            if (index != -1) {
                profile_string = quality.substring(0, index);
            }
            camcorder_profile = CamcorderProfile.get(this.cameraId, Integer.parseInt(profile_string));
            if (index != -1 && index + 1 < quality.length()) {
                String override_string = quality.substring(index + 1);
                if (override_string.charAt(0) == 'r' && override_string.length() >= 4) {
                    index = override_string.indexOf(SoapEnvelope.VER12);
                    if (index != -1) {
                        String resolution_w_s = override_string.substring(1, index);
                        String resolution_h_s = override_string.substring(index + 1);
                        int resolution_w = Integer.parseInt(resolution_w_s);
                        int resolution_h = Integer.parseInt(resolution_h_s);
                        camcorder_profile.videoFrameWidth = resolution_w;
                        camcorder_profile.videoFrameHeight = resolution_h;
                    }
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return camcorder_profile;
    }

    public CamcorderProfile getCamcorderProfile() {
        CamcorderProfile profile;
        MainActivity main_activity = (MainActivity) getContext();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
        if (this.cameraId == 0 && sharedPreferences.getBoolean(MainActivity.getForceVideo4KPreferenceKey(), false) && main_activity.supportsForceVideo4K()) {
            profile = CamcorderProfile.get(this.cameraId, 1);
            profile.videoFrameWidth = 3840;
            profile.videoFrameHeight = 2160;
            profile.videoBitRate = (int) (((double) profile.videoBitRate) * 2.8d);
        } else if (this.current_video_quality != -1) {
            profile = getCamcorderProfile((String) this.video_quality.get(this.current_video_quality));
        } else {
            profile = CamcorderProfile.get(this.cameraId, 1);
        }
        String bitrate_value = sharedPreferences.getString(MainActivity.getVideoBitratePreferenceKey(), "default");
        if (!bitrate_value.equals("default")) {
            try {
                profile.videoBitRate = Integer.parseInt(bitrate_value);
            } catch (NumberFormatException e) {
            }
        }
        String fps_value = sharedPreferences.getString(MainActivity.getVideoFPSPreferenceKey(), "default");
        if (!fps_value.equals("default")) {
            try {
                profile.videoFrameRate = Integer.parseInt(fps_value);
            } catch (NumberFormatException e2) {
            }
        }
        return profile;
    }

    private static String formatFloatToString(float f) {
        int i = (int) f;
        if (f == ((float) i)) {
            return Integer.toString(i);
        }
        return String.format(Locale.getDefault(), "%.2f", new Object[]{Float.valueOf(f)});
    }

    private static int greatestCommonFactor(int a, int b) {
        while (b > 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    private static String getAspectRatio(int width, int height) {
        int gcf = greatestCommonFactor(width, height);
        return (width / gcf) + ":" + (height / gcf);
    }

    static String getMPString(int width, int height) {
        return new StringBuilder(String.valueOf(formatFloatToString(((float) (width * height)) / 1000000.0f))).append("MP").toString();
    }

    static String getAspectRatioMPString(int width, int height) {
        return "(" + getAspectRatio(width, height) + ", " + getMPString(width, height) + ")";
    }

    String getCamcorderProfileDescriptionShort(String quality) {
        CamcorderProfile profile = getCamcorderProfile(quality);
        return profile.videoFrameWidth + "x" + profile.videoFrameHeight + " " + getMPString(profile.videoFrameWidth, profile.videoFrameHeight);
    }

    String getCamcorderProfileDescription(String quality) {
        CamcorderProfile profile = getCamcorderProfile(quality);
        String highest = "";
        if (profile.quality == 1) {
            highest = "Highest: ";
        }
        String type = "";
        if (profile.videoFrameWidth == 3840 && profile.videoFrameHeight == 2160) {
            type = "4K Ultra HD ";
        } else if (profile.videoFrameWidth == 1920 && profile.videoFrameHeight == 1080) {
            type = "Full HD ";
        } else if (profile.videoFrameWidth == 1280 && profile.videoFrameHeight == 720) {
            type = "HD ";
        } else if (profile.videoFrameWidth == 720 && profile.videoFrameHeight == 480) {
            type = "SD ";
        } else if (profile.videoFrameWidth == 640 && profile.videoFrameHeight == 480) {
            type = "VGA ";
        } else if (profile.videoFrameWidth == 352 && profile.videoFrameHeight == 288) {
            type = "CIF ";
        } else if (profile.videoFrameWidth == ThumbnailUtils.TARGET_SIZE_MINI_THUMBNAIL_HEIGHT && profile.videoFrameHeight == 240) {
            type = "QVGA ";
        } else if (profile.videoFrameWidth == 176 && profile.videoFrameHeight == 144) {
            type = "QCIF ";
        }
        return new StringBuilder(String.valueOf(highest)).append(type).append(profile.videoFrameWidth).append("x").append(profile.videoFrameHeight).append(" ").append(getAspectRatioMPString(profile.videoFrameWidth, profile.videoFrameHeight)).toString();
    }

    public double getTargetRatioForPreview(Point display_size) {
        double targetRatio;
        if (!PreferenceManager.getDefaultSharedPreferences((Activity) getContext()).getString(MainActivity.getPreviewSizePreferenceKey(), "preference_preview_size_wysiwyg").equals("preference_preview_size_wysiwyg") && !this.is_video) {
            targetRatio = ((double) display_size.x) / ((double) display_size.y);
        } else if (this.is_video) {
            CamcorderProfile profile = getCamcorderProfile();
            targetRatio = ((double) profile.videoFrameWidth) / ((double) profile.videoFrameHeight);
        } else {
            Size picture_size = this.camera_controller.getPictureSize();
            targetRatio = ((double) picture_size.width) / ((double) picture_size.height);
        }
        this.preview_targetRatio = targetRatio;
        return targetRatio;
    }

    public Size getClosestSize(List<Size> sizes, double targetRatio) {
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        for (Size size : sizes) {
            double ratio = ((double) size.width) / ((double) size.height);
            if (Math.abs(ratio - targetRatio) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(ratio - targetRatio);
            }
        }
        return optimalSize;
    }

    public Size getOptimalPreviewSize(List<Size> sizes) {
        if (sizes == null) {
            return null;
        }
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        Point display_size = new Point();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getSize(display_size);
        double targetRatio = getTargetRatioForPreview(display_size);
        int targetHeight = Math.min(display_size.y, display_size.x);
        if (targetHeight <= 0) {
            targetHeight = display_size.y;
        }
        for (Size size : sizes) {
            if (Math.abs((((double) size.width) / ((double) size.height)) - targetRatio) <= 0.05d && ((double) Math.abs(size.height - targetHeight)) < minDiff) {
                optimalSize = size;
                minDiff = (double) Math.abs(size.height - targetHeight);
            }
        }
        if (optimalSize == null) {
            return getClosestSize(sizes, targetRatio);
        }
        return optimalSize;
    }

    public Size getOptimalVideoPictureSize(List<Size> sizes, double targetRatio) {
        if (sizes == null) {
            return null;
        }
        Size optimalSize = null;
        for (Size size : sizes) {
            if (Math.abs((((double) size.width) / ((double) size.height)) - targetRatio) <= 0.05d && (optimalSize == null || size.width > optimalSize.width)) {
                optimalSize = size;
            }
        }
        if (optimalSize == null) {
            return getClosestSize(sizes, targetRatio);
        }
        return optimalSize;
    }

    private void setAspectRatio(double ratio) {
        if (ratio <= 0.0d) {
            throw new IllegalArgumentException();
        }
        this.has_aspect_ratio = true;
        if (this.aspect_ratio != ratio) {
            this.aspect_ratio = ratio;
            this.cameraSurface.getView().requestLayout();
        }
    }

    private boolean hasAspectRatio() {
        return this.has_aspect_ratio;
    }

    private double getAspectRatio() {
        return this.aspect_ratio;
    }

    public int getDisplayRotation() {
        int rotation = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getRotation();
        if (!PreferenceManager.getDefaultSharedPreferences(getContext()).getString(MainActivity.getRotatePreviewPreferenceKey(), "0").equals("180")) {
            return rotation;
        }
        switch (rotation) {
            case 0:
                return 2;
            case 1:
                return 3;
            case 2:
                return 0;
            case 3:
                return 1;
            default:
                return rotation;
        }
    }

    void setCameraDisplayOrientation() {
        if (this.camera_controller != null) {
            int degrees = 0;
            switch (getDisplayRotation()) {
                case 0:
                    degrees = 0;
                    break;
                case 1:
                    degrees = 90;
                    break;
                case 2:
                    degrees = 180;
                    break;
                case 3:
                    degrees = 270;
                    break;
            }
            if (this.using_texture_view) {
                configureTransform();
            } else {
                this.camera_controller.setDisplayOrientation(degrees);
            }
        }
    }

    private void onOrientationChanged(int orientation) {
        if (orientation != -1 && this.camera_controller != null) {
            int new_rotation;
            orientation = ((orientation + 45) / 90) * 90;
            this.current_orientation = orientation % 360;
            int camera_orientation = this.camera_controller.getCameraOrientation();
            if (this.camera_controller.isFrontFacing()) {
                new_rotation = ((camera_orientation - orientation) + 360) % 360;
            } else {
                new_rotation = (camera_orientation + orientation) % 360;
            }
            if (new_rotation != this.current_rotation) {
                this.current_rotation = new_rotation;
            }
        }
    }

    private int getDeviceDefaultOrientation() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService("window");
        Configuration config = getResources().getConfiguration();
        int rotation = windowManager.getDefaultDisplay().getRotation();
        if ((rotation == 0 || rotation == 2) && config.orientation == 2) {
            return 2;
        }
        if ((rotation == 1 || rotation == 3) && config.orientation == 1) {
            return 2;
        }
        return 1;
    }

    private int getImageVideoRotation() {
        String lock_orientation = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(MainActivity.getLockOrientationPreferenceKey(), "none");
        int camera_orientation;
        if (lock_orientation.equals("landscape")) {
            camera_orientation = this.camera_controller.getCameraOrientation();
            if (getDeviceDefaultOrientation() != 1) {
                return camera_orientation;
            }
            if (this.camera_controller.isFrontFacing()) {
                return (camera_orientation + 90) % 360;
            }
            return (camera_orientation + 270) % 360;
        } else if (!lock_orientation.equals("portrait")) {
            return this.current_rotation;
        } else {
            camera_orientation = this.camera_controller.getCameraOrientation();
            if (getDeviceDefaultOrientation() == 1) {
                return camera_orientation;
            }
            if (this.camera_controller.isFrontFacing()) {
                return (camera_orientation + 270) % 360;
            }
            return (camera_orientation + 90) % 360;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void draw(Canvas canvas) {
        if (!this.app_is_paused) {
            int pixels_offset_y;
            int diff;
            int rgb;
            int location_x;
            int location_y;
            MainActivity main_activity = (MainActivity) getContext();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            if (this.immersive_mode) {
                if (sharedPreferences.getString(MainActivity.getImmersiveModePreferenceKey(), "immersive_mode_low_profile").equals("immersive_mode_everything")) {
                    return;
                }
            }
            float scale = getResources().getDisplayMetrics().density;
            String preference_grid = sharedPreferences.getString(MainActivity.getShowGridPreferenceKey(), "preference_grid_none");
            if (this.camera_controller != null) {
                if (preference_grid.equals("preference_grid_3x3")) {
                    this.p.setColor(-1);
                    canvas.drawLine(((float) canvas.getWidth()) / 3.0f, 0.0f, ((float) canvas.getWidth()) / 3.0f, ((float) canvas.getHeight()) - 1.0f, this.p);
                    canvas.drawLine((2.0f * ((float) canvas.getWidth())) / 3.0f, 0.0f, (2.0f * ((float) canvas.getWidth())) / 3.0f, ((float) canvas.getHeight()) - 1.0f, this.p);
                    canvas.drawLine(0.0f, ((float) canvas.getHeight()) / 3.0f, ((float) canvas.getWidth()) - 1.0f, ((float) canvas.getHeight()) / 3.0f, this.p);
                    canvas.drawLine(0.0f, (2.0f * ((float) canvas.getHeight())) / 3.0f, ((float) canvas.getWidth()) - 1.0f, (2.0f * ((float) canvas.getHeight())) / 3.0f, this.p);
                }
            }
            if (this.camera_controller != null) {
                if (preference_grid.equals("preference_grid_4x2")) {
                    this.p.setColor(-7829368);
                    canvas.drawLine(((float) canvas.getWidth()) / 4.0f, 0.0f, ((float) canvas.getWidth()) / 4.0f, ((float) canvas.getHeight()) - 1.0f, this.p);
                    canvas.drawLine(((float) canvas.getWidth()) / 2.0f, 0.0f, ((float) canvas.getWidth()) / 2.0f, ((float) canvas.getHeight()) - 1.0f, this.p);
                    canvas.drawLine((3.0f * ((float) canvas.getWidth())) / 4.0f, 0.0f, (3.0f * ((float) canvas.getWidth())) / 4.0f, ((float) canvas.getHeight()) - 1.0f, this.p);
                    canvas.drawLine(0.0f, ((float) canvas.getHeight()) / 2.0f, ((float) canvas.getWidth()) - 1.0f, ((float) canvas.getHeight()) / 2.0f, this.p);
                    this.p.setColor(-1);
                    int crosshairs_radius = (int) ((20.0f * scale) + 0.5f);
                    canvas.drawLine(((float) canvas.getWidth()) / 2.0f, (((float) canvas.getHeight()) / 2.0f) - ((float) crosshairs_radius), ((float) canvas.getWidth()) / 2.0f, (((float) canvas.getHeight()) / 2.0f) + ((float) crosshairs_radius), this.p);
                    canvas.drawLine((((float) canvas.getWidth()) / 2.0f) - ((float) crosshairs_radius), ((float) canvas.getHeight()) / 2.0f, (((float) canvas.getWidth()) / 2.0f) + ((float) crosshairs_radius), ((float) canvas.getHeight()) / 2.0f, this.p);
                }
            }
            if (!this.is_video) {
            }
            String preference_crop_guide = sharedPreferences.getString(MainActivity.getShowCropGuidePreferenceKey(), "crop_guide_none");
            if (this.camera_controller != null && this.preview_targetRatio > 0.0d) {
                if (!preference_crop_guide.equals("crop_guide_none")) {
                    this.p.setStyle(Style.STROKE);
                    this.p.setColor(Color.rgb(255, 235, 59));
                    double crop_ratio = -1.0d;
                    if (preference_crop_guide.equals("crop_guide_1.33")) {
                        crop_ratio = 1.33333333d;
                    } else {
                        if (preference_crop_guide.equals("crop_guide_1.5")) {
                            crop_ratio = 1.5d;
                        } else {
                            if (preference_crop_guide.equals("crop_guide_1.78")) {
                                crop_ratio = 1.77777778d;
                            } else {
                                if (preference_crop_guide.equals("crop_guide_1.85")) {
                                    crop_ratio = 1.85d;
                                } else {
                                    if (preference_crop_guide.equals("crop_guide_2.33")) {
                                        crop_ratio = 2.33333333d;
                                    } else {
                                        if (preference_crop_guide.equals("crop_guide_2.35")) {
                                            crop_ratio = 2.3500612d;
                                        } else {
                                            if (preference_crop_guide.equals("crop_guide_2.4")) {
                                                crop_ratio = 2.4d;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (crop_ratio > 0.0d && Math.abs(this.preview_targetRatio - crop_ratio) > 1.0E-5d) {
                        int left = 1;
                        int top = 1;
                        int right = canvas.getWidth() - 1;
                        int bottom = canvas.getHeight() - 1;
                        if (crop_ratio > this.preview_targetRatio) {
                            double new_hheight = ((double) canvas.getWidth()) / (2.0d * crop_ratio);
                            top = (int) (((double) (canvas.getHeight() / 2)) - new_hheight);
                            bottom = (int) (((double) (canvas.getHeight() / 2)) + new_hheight);
                        } else {
                            double new_hwidth = (((double) canvas.getHeight()) * crop_ratio) / 2.0d;
                            left = (int) (((double) (canvas.getWidth() / 2)) - new_hwidth);
                            right = (int) (((double) (canvas.getWidth() / 2)) + new_hwidth);
                        }
                        canvas.drawRect((float) left, (float) top, (float) right, (float) bottom, this.p);
                    }
                }
            }
            if (!(this.camera_controller == null || !this.thumbnail_anim || this.thumbnail == null)) {
                long time = System.currentTimeMillis() - this.thumbnail_anim_start_ms;
                if (time > 500) {
                    this.thumbnail_anim = false;
                } else {
                    this.thumbnail_anim_src_rect.left = 0.0f;
                    this.thumbnail_anim_src_rect.top = 0.0f;
                    this.thumbnail_anim_src_rect.right = (float) this.thumbnail.getWidth();
                    this.thumbnail_anim_src_rect.bottom = (float) this.thumbnail.getHeight();
                    View galleryButton = main_activity.findViewById(R.id.gallery);
                    float alpha = ((float) time) / 500.0f;
                    int thumbnail_x = (int) (((1.0f - alpha) * ((float) (canvas.getWidth() / 2))) + (((float) (galleryButton.getLeft() + (galleryButton.getWidth() / 2))) * alpha));
                    int thumbnail_y = (int) (((1.0f - alpha) * ((float) (canvas.getHeight() / 2))) + (((float) (galleryButton.getTop() + (galleryButton.getHeight() / 2))) * alpha));
                    float st_w = (float) canvas.getWidth();
                    float st_h = (float) canvas.getHeight();
                    int thumbnail_w = (int) (st_w / (1.0f + (alpha * ((st_w / ((float) galleryButton.getWidth())) - 1.0f))));
                    int thumbnail_h = (int) (st_h / (1.0f + (alpha * ((st_h / ((float) galleryButton.getHeight())) - 1.0f))));
                    this.thumbnail_anim_dst_rect.left = (float) (thumbnail_x - (thumbnail_w / 2));
                    this.thumbnail_anim_dst_rect.top = (float) (thumbnail_y - (thumbnail_h / 2));
                    this.thumbnail_anim_dst_rect.right = (float) ((thumbnail_w / 2) + thumbnail_x);
                    this.thumbnail_anim_dst_rect.bottom = (float) ((thumbnail_h / 2) + thumbnail_y);
                    this.thumbnail_anim_matrix.setRectToRect(this.thumbnail_anim_src_rect, this.thumbnail_anim_dst_rect, ScaleToFit.FILL);
                    if (this.ui_rotation == 90 || this.ui_rotation == 270) {
                        float ratio = ((float) this.thumbnail.getWidth()) / ((float) this.thumbnail.getHeight());
                        this.thumbnail_anim_matrix.preScale(ratio, 1.0f / ratio, (float) (this.thumbnail.getWidth() / 2), (float) (this.thumbnail.getHeight() / 2));
                    }
                    this.thumbnail_anim_matrix.preRotate((float) this.ui_rotation, (float) (this.thumbnail.getWidth() / 2), (float) (this.thumbnail.getHeight() / 2));
                    canvas.drawBitmap(this.thumbnail, this.thumbnail_anim_matrix, this.p);
                }
            }
            canvas.save();
            canvas.rotate((float) this.ui_rotation, (float) (canvas.getWidth() / 2), (float) (canvas.getHeight() / 2));
            int text_y = (int) ((20.0f * scale) + 0.5f);
            int text_base_y = 0;
            if (this.ui_rotation == (this.ui_placement_right ? 0 : 180)) {
                text_base_y = canvas.getHeight() - ((int) (0.5d * ((double) text_y)));
            } else {
                if (this.ui_rotation == (this.ui_placement_right ? 180 : 0)) {
                    text_base_y = canvas.getHeight() - ((int) (2.5d * ((double) text_y)));
                } else if (this.ui_rotation == 90 || this.ui_rotation == 270) {
                    ((ImageButton) main_activity.findViewById(R.id.take_photo)).getLocationOnScreen(this.gui_location);
                    int view_left = this.gui_location[0];
                    this.cameraSurface.getView().getLocationOnScreen(this.gui_location);
                    int diff_x = view_left - ((canvas.getWidth() / 2) + this.gui_location[0]);
                    int max_x = canvas.getWidth();
                    if (this.ui_rotation == 90) {
                        max_x -= (int) (1.5d * ((double) text_y));
                    }
                    if ((canvas.getWidth() / 2) + diff_x > max_x) {
                        diff_x = max_x - (canvas.getWidth() / 2);
                    }
                    text_base_y = ((canvas.getHeight() / 2) + diff_x) - ((int) (0.5d * ((double) text_y)));
                }
            }
            int top_y = (int) ((5.0f * scale) + 0.5f);
            String ybounds_text = new StringBuilder(String.valueOf(getResources().getString(R.string.zoom))).append(getResources().getString(R.string.free_memory)).append(getResources().getString(R.string.angle)).append(getResources().getString(R.string.direction)).toString();
            if (this.camera_controller != null && this.phase != 3) {
                boolean draw_angle;
                boolean draw_geo_direction;
                int color;
                int pixels_offset_x;
                float geo_angle;
                long remaining_time;
                long video_time;
                int secs;
                int mins;
                String time_s;
                if (this.has_level_angle) {
                    if (sharedPreferences.getBoolean(MainActivity.getShowAnglePreferenceKey(), true)) {
                        draw_angle = true;
                        if (this.has_geo_direction) {
                            if (sharedPreferences.getBoolean(MainActivity.getShowGeoDirectionPreferenceKey(), true)) {
                                draw_geo_direction = true;
                                if (draw_angle) {
                                    color = -1;
                                    this.p.setTextSize((14.0f * scale) + 0.5f);
                                    pixels_offset_x = 0;
                                    if (draw_geo_direction) {
                                        this.p.setTextAlign(Align.CENTER);
                                    } else {
                                        pixels_offset_x = -((int) ((82.0f * scale) + 0.5f));
                                        this.p.setTextAlign(Align.LEFT);
                                    }
                                    if (Math.abs(this.level_angle) <= 1.0d) {
                                        color = Color.rgb(20, 231, 21);
                                    }
                                    drawTextWithBackground(canvas, this.p, new StringBuilder(String.valueOf(getResources().getString(R.string.angle))).append(": ").append(this.decimalFormat.format(this.level_angle)).append('°').toString(), color, -16777216, (canvas.getWidth() / 2) + pixels_offset_x, text_base_y, false, ybounds_text);
                                }
                                if (draw_geo_direction) {
                                    this.p.setTextSize((14.0f * scale) + 0.5f);
                                    if (draw_angle) {
                                        this.p.setTextAlign(Align.CENTER);
                                    } else {
                                        this.p.setTextAlign(Align.LEFT);
                                    }
                                    geo_angle = (float) Math.toDegrees((double) this.geo_direction[0]);
                                    if (geo_angle < 0.0f) {
                                        geo_angle += 360.0f;
                                    }
                                    drawTextWithBackground(canvas, this.p, " " + getResources().getString(R.string.direction) + ": " + Math.round(geo_angle) + '°', -1, -16777216, canvas.getWidth() / 2, text_base_y, false, ybounds_text);
                                }
                                if (isOnTimer()) {
                                    remaining_time = ((this.take_photo_time - System.currentTimeMillis()) + 999) / 1000;
                                    if (remaining_time >= 0) {
                                        this.p.setTextSize((42.0f * scale) + 0.5f);
                                        this.p.setTextAlign(Align.CENTER);
                                        drawTextWithBackground(canvas, this.p, remaining_time, Color.rgb(229, 28, 35), -16777216, canvas.getWidth() / 2, canvas.getHeight() / 2);
                                    }
                                } else if (this.video_recorder != null && this.video_start_time_set) {
                                    video_time = (System.currentTimeMillis() - this.video_start_time) / 1000;
                                    secs = (int) (video_time % 60);
                                    video_time /= 60;
                                    mins = (int) (video_time % 60);
                                    time_s = (video_time / 60) + ":" + String.format("%02d", new Object[]{Integer.valueOf(mins)}) + ":" + String.format("%02d", new Object[]{Integer.valueOf(secs)});
                                    this.p.setTextSize((14.0f * scale) + 0.5f);
                                    this.p.setTextAlign(Align.CENTER);
                                    pixels_offset_y = text_y * 3;
                                    color = Color.rgb(229, 28, 35);
                                    if (main_activity.isScreenLocked()) {
                                        drawTextWithBackground(canvas, this.p, getResources().getString(R.string.screen_lock_message_2), color, -16777216, canvas.getWidth() / 2, text_base_y - pixels_offset_y);
                                        pixels_offset_y += text_y;
                                        drawTextWithBackground(canvas, this.p, getResources().getString(R.string.screen_lock_message_1), color, -16777216, canvas.getWidth() / 2, text_base_y - pixels_offset_y);
                                        pixels_offset_y += text_y;
                                    }
                                    drawTextWithBackground(canvas, this.p, time_s, color, -16777216, canvas.getWidth() / 2, text_base_y - pixels_offset_y);
                                }
                            }
                        }
                        draw_geo_direction = false;
                        if (draw_angle) {
                            color = -1;
                            this.p.setTextSize((14.0f * scale) + 0.5f);
                            pixels_offset_x = 0;
                            if (draw_geo_direction) {
                                this.p.setTextAlign(Align.CENTER);
                            } else {
                                pixels_offset_x = -((int) ((82.0f * scale) + 0.5f));
                                this.p.setTextAlign(Align.LEFT);
                            }
                            if (Math.abs(this.level_angle) <= 1.0d) {
                                color = Color.rgb(20, 231, 21);
                            }
                            drawTextWithBackground(canvas, this.p, new StringBuilder(String.valueOf(getResources().getString(R.string.angle))).append(": ").append(this.decimalFormat.format(this.level_angle)).append('°').toString(), color, -16777216, (canvas.getWidth() / 2) + pixels_offset_x, text_base_y, false, ybounds_text);
                        }
                        if (draw_geo_direction) {
                            this.p.setTextSize((14.0f * scale) + 0.5f);
                            if (draw_angle) {
                                this.p.setTextAlign(Align.CENTER);
                            } else {
                                this.p.setTextAlign(Align.LEFT);
                            }
                            geo_angle = (float) Math.toDegrees((double) this.geo_direction[0]);
                            if (geo_angle < 0.0f) {
                                geo_angle += 360.0f;
                            }
                            drawTextWithBackground(canvas, this.p, " " + getResources().getString(R.string.direction) + ": " + Math.round(geo_angle) + '°', -1, -16777216, canvas.getWidth() / 2, text_base_y, false, ybounds_text);
                        }
                        if (isOnTimer()) {
                            remaining_time = ((this.take_photo_time - System.currentTimeMillis()) + 999) / 1000;
                            if (remaining_time >= 0) {
                                this.p.setTextSize((42.0f * scale) + 0.5f);
                                this.p.setTextAlign(Align.CENTER);
                                drawTextWithBackground(canvas, this.p, remaining_time, Color.rgb(229, 28, 35), -16777216, canvas.getWidth() / 2, canvas.getHeight() / 2);
                            }
                        } else {
                            video_time = (System.currentTimeMillis() - this.video_start_time) / 1000;
                            secs = (int) (video_time % 60);
                            video_time /= 60;
                            mins = (int) (video_time % 60);
                            time_s = (video_time / 60) + ":" + String.format("%02d", new Object[]{Integer.valueOf(mins)}) + ":" + String.format("%02d", new Object[]{Integer.valueOf(secs)});
                            this.p.setTextSize((14.0f * scale) + 0.5f);
                            this.p.setTextAlign(Align.CENTER);
                            pixels_offset_y = text_y * 3;
                            color = Color.rgb(229, 28, 35);
                            if (main_activity.isScreenLocked()) {
                                drawTextWithBackground(canvas, this.p, getResources().getString(R.string.screen_lock_message_2), color, -16777216, canvas.getWidth() / 2, text_base_y - pixels_offset_y);
                                pixels_offset_y += text_y;
                                drawTextWithBackground(canvas, this.p, getResources().getString(R.string.screen_lock_message_1), color, -16777216, canvas.getWidth() / 2, text_base_y - pixels_offset_y);
                                pixels_offset_y += text_y;
                            }
                            drawTextWithBackground(canvas, this.p, time_s, color, -16777216, canvas.getWidth() / 2, text_base_y - pixels_offset_y);
                        }
                    }
                }
                draw_angle = false;
                if (this.has_geo_direction) {
                    if (sharedPreferences.getBoolean(MainActivity.getShowGeoDirectionPreferenceKey(), true)) {
                        draw_geo_direction = true;
                        if (draw_angle) {
                            color = -1;
                            this.p.setTextSize((14.0f * scale) + 0.5f);
                            pixels_offset_x = 0;
                            if (draw_geo_direction) {
                                pixels_offset_x = -((int) ((82.0f * scale) + 0.5f));
                                this.p.setTextAlign(Align.LEFT);
                            } else {
                                this.p.setTextAlign(Align.CENTER);
                            }
                            if (Math.abs(this.level_angle) <= 1.0d) {
                                color = Color.rgb(20, 231, 21);
                            }
                            drawTextWithBackground(canvas, this.p, new StringBuilder(String.valueOf(getResources().getString(R.string.angle))).append(": ").append(this.decimalFormat.format(this.level_angle)).append('°').toString(), color, -16777216, (canvas.getWidth() / 2) + pixels_offset_x, text_base_y, false, ybounds_text);
                        }
                        if (draw_geo_direction) {
                            this.p.setTextSize((14.0f * scale) + 0.5f);
                            if (draw_angle) {
                                this.p.setTextAlign(Align.LEFT);
                            } else {
                                this.p.setTextAlign(Align.CENTER);
                            }
                            geo_angle = (float) Math.toDegrees((double) this.geo_direction[0]);
                            if (geo_angle < 0.0f) {
                                geo_angle += 360.0f;
                            }
                            drawTextWithBackground(canvas, this.p, " " + getResources().getString(R.string.direction) + ": " + Math.round(geo_angle) + '°', -1, -16777216, canvas.getWidth() / 2, text_base_y, false, ybounds_text);
                        }
                        if (isOnTimer()) {
                            remaining_time = ((this.take_photo_time - System.currentTimeMillis()) + 999) / 1000;
                            if (remaining_time >= 0) {
                                this.p.setTextSize((42.0f * scale) + 0.5f);
                                this.p.setTextAlign(Align.CENTER);
                                drawTextWithBackground(canvas, this.p, remaining_time, Color.rgb(229, 28, 35), -16777216, canvas.getWidth() / 2, canvas.getHeight() / 2);
                            }
                        } else {
                            video_time = (System.currentTimeMillis() - this.video_start_time) / 1000;
                            secs = (int) (video_time % 60);
                            video_time /= 60;
                            mins = (int) (video_time % 60);
                            time_s = (video_time / 60) + ":" + String.format("%02d", new Object[]{Integer.valueOf(mins)}) + ":" + String.format("%02d", new Object[]{Integer.valueOf(secs)});
                            this.p.setTextSize((14.0f * scale) + 0.5f);
                            this.p.setTextAlign(Align.CENTER);
                            pixels_offset_y = text_y * 3;
                            color = Color.rgb(229, 28, 35);
                            if (main_activity.isScreenLocked()) {
                                drawTextWithBackground(canvas, this.p, getResources().getString(R.string.screen_lock_message_2), color, -16777216, canvas.getWidth() / 2, text_base_y - pixels_offset_y);
                                pixels_offset_y += text_y;
                                drawTextWithBackground(canvas, this.p, getResources().getString(R.string.screen_lock_message_1), color, -16777216, canvas.getWidth() / 2, text_base_y - pixels_offset_y);
                                pixels_offset_y += text_y;
                            }
                            drawTextWithBackground(canvas, this.p, time_s, color, -16777216, canvas.getWidth() / 2, text_base_y - pixels_offset_y);
                        }
                    }
                }
                draw_geo_direction = false;
                if (draw_angle) {
                    color = -1;
                    this.p.setTextSize((14.0f * scale) + 0.5f);
                    pixels_offset_x = 0;
                    if (draw_geo_direction) {
                        this.p.setTextAlign(Align.CENTER);
                    } else {
                        pixels_offset_x = -((int) ((82.0f * scale) + 0.5f));
                        this.p.setTextAlign(Align.LEFT);
                    }
                    if (Math.abs(this.level_angle) <= 1.0d) {
                        color = Color.rgb(20, 231, 21);
                    }
                    drawTextWithBackground(canvas, this.p, new StringBuilder(String.valueOf(getResources().getString(R.string.angle))).append(": ").append(this.decimalFormat.format(this.level_angle)).append('°').toString(), color, -16777216, (canvas.getWidth() / 2) + pixels_offset_x, text_base_y, false, ybounds_text);
                }
                if (draw_geo_direction) {
                    this.p.setTextSize((14.0f * scale) + 0.5f);
                    if (draw_angle) {
                        this.p.setTextAlign(Align.CENTER);
                    } else {
                        this.p.setTextAlign(Align.LEFT);
                    }
                    geo_angle = (float) Math.toDegrees((double) this.geo_direction[0]);
                    if (geo_angle < 0.0f) {
                        geo_angle += 360.0f;
                    }
                    drawTextWithBackground(canvas, this.p, " " + getResources().getString(R.string.direction) + ": " + Math.round(geo_angle) + '°', -1, -16777216, canvas.getWidth() / 2, text_base_y, false, ybounds_text);
                }
                if (isOnTimer()) {
                    video_time = (System.currentTimeMillis() - this.video_start_time) / 1000;
                    secs = (int) (video_time % 60);
                    video_time /= 60;
                    mins = (int) (video_time % 60);
                    time_s = (video_time / 60) + ":" + String.format("%02d", new Object[]{Integer.valueOf(mins)}) + ":" + String.format("%02d", new Object[]{Integer.valueOf(secs)});
                    this.p.setTextSize((14.0f * scale) + 0.5f);
                    this.p.setTextAlign(Align.CENTER);
                    pixels_offset_y = text_y * 3;
                    color = Color.rgb(229, 28, 35);
                    if (main_activity.isScreenLocked()) {
                        drawTextWithBackground(canvas, this.p, getResources().getString(R.string.screen_lock_message_2), color, -16777216, canvas.getWidth() / 2, text_base_y - pixels_offset_y);
                        pixels_offset_y += text_y;
                        drawTextWithBackground(canvas, this.p, getResources().getString(R.string.screen_lock_message_1), color, -16777216, canvas.getWidth() / 2, text_base_y - pixels_offset_y);
                        pixels_offset_y += text_y;
                    }
                    drawTextWithBackground(canvas, this.p, time_s, color, -16777216, canvas.getWidth() / 2, text_base_y - pixels_offset_y);
                } else {
                    remaining_time = ((this.take_photo_time - System.currentTimeMillis()) + 999) / 1000;
                    if (remaining_time >= 0) {
                        this.p.setTextSize((42.0f * scale) + 0.5f);
                        this.p.setTextAlign(Align.CENTER);
                        drawTextWithBackground(canvas, this.p, remaining_time, Color.rgb(229, 28, 35), -16777216, canvas.getWidth() / 2, canvas.getHeight() / 2);
                    }
                }
            } else if (this.camera_controller == null) {
                this.p.setColor(-1);
                this.p.setTextSize((14.0f * scale) + 0.5f);
                this.p.setTextAlign(Align.CENTER);
                int pixels_offset = (int) ((20.0f * scale) + 0.5f);
                canvas.drawText(getResources().getString(R.string.failed_to_open_camera_1), (float) (canvas.getWidth() / 2), (float) (canvas.getHeight() / 2), this.p);
                canvas.drawText(getResources().getString(R.string.failed_to_open_camera_2), (float) (canvas.getWidth() / 2), (float) ((canvas.getHeight() / 2) + pixels_offset), this.p);
                canvas.drawText(getResources().getString(R.string.failed_to_open_camera_3), (float) (canvas.getWidth() / 2), (float) ((canvas.getHeight() / 2) + (pixels_offset * 2)), this.p);
            }
            if (this.has_zoom && this.camera_controller != null) {
                if (sharedPreferences.getBoolean(MainActivity.getShowZoomPreferenceKey(), true)) {
                    float zoom_ratio = ((float) ((Integer) this.zoom_ratios.get(this.zoom_factor)).intValue()) / 100.0f;
                    if (zoom_ratio > 1.00001f) {
                        pixels_offset_y = text_y * 2;
                        this.p.setTextSize((14.0f * scale) + 0.5f);
                        this.p.setTextAlign(Align.CENTER);
                        drawTextWithBackground(canvas, this.p, new StringBuilder(String.valueOf(getResources().getString(R.string.zoom))).append(": ").append(zoom_ratio).append("x").toString(), -1, -16777216, canvas.getWidth() / 2, text_base_y - pixels_offset_y, false, ybounds_text);
                    }
                }
            }
            if (this.camera_controller != null) {
                if (sharedPreferences.getBoolean(MainActivity.getShowFreeMemoryPreferenceKey(), true)) {
                    pixels_offset_y = text_y * 1;
                    this.p.setTextSize((14.0f * scale) + 0.5f);
                    this.p.setTextAlign(Align.CENTER);
                    long time_now = System.currentTimeMillis();
                    if (this.free_memory_gb < 0.0f || time_now > this.last_free_memory_time + 1000) {
                        long free_mb = main_activity.freeMemory();
                        if (free_mb >= 0) {
                            this.free_memory_gb = ((float) free_mb) / 1024.0f;
                            this.last_free_memory_time = time_now;
                        }
                    }
                    if (this.free_memory_gb >= 0.0f) {
                        drawTextWithBackground(canvas, this.p, new StringBuilder(String.valueOf(getResources().getString(R.string.free_memory))).append(": ").append(this.decimalFormat.format((double) this.free_memory_gb)).append("GB").toString(), -1, -16777216, canvas.getWidth() / 2, text_base_y - pixels_offset_y, false, ybounds_text);
                    }
                }
            }
            if (sharedPreferences.getBoolean(MainActivity.getShowBatteryPreferenceKey(), true)) {
                if (!this.has_battery_frac || System.currentTimeMillis() > this.last_battery_time + 60000) {
                    Intent batteryStatus = main_activity.registerReceiver(null, this.battery_ifilter);
                    int battery_level = batteryStatus.getIntExtra("level", -1);
                    int battery_scale = batteryStatus.getIntExtra(CropImage.SCALE, -1);
                    this.has_battery_frac = true;
                    this.battery_frac = ((float) battery_level) / ((float) battery_scale);
                    this.last_battery_time = System.currentTimeMillis();
                }
                int battery_x = (int) ((5.0f * scale) + 0.5f);
                int battery_y = top_y;
                int battery_width = (int) ((5.0f * scale) + 0.5f);
                int battery_height = battery_width * 4;
                if (this.ui_rotation == 90 || this.ui_rotation == 270) {
                    diff = canvas.getWidth() - canvas.getHeight();
                    battery_x += diff / 2;
                    battery_y -= diff / 2;
                }
                if (this.ui_rotation == 90) {
                    battery_y = (canvas.getHeight() - battery_y) - battery_height;
                }
                if (this.ui_rotation == 180) {
                    battery_x = (canvas.getWidth() - battery_x) - battery_width;
                }
                this.p.setColor(-1);
                this.p.setStyle(Style.STROKE);
                canvas.drawRect((float) battery_x, (float) battery_y, (float) (battery_x + battery_width), (float) (battery_y + battery_height), this.p);
                Paint paint = this.p;
                if (this.battery_frac >= 0.3f) {
                    rgb = Color.rgb(37, 155, 36);
                } else {
                    rgb = Color.rgb(229, 28, 35);
                }
                paint.setColor(rgb);
                this.p.setStyle(Style.FILL);
                canvas.drawRect((float) (battery_x + 1), ((float) (battery_y + 1)) + ((1.0f - this.battery_frac) * ((float) (battery_height - 2))), (float) ((battery_x + battery_width) - 1), (float) ((battery_y + battery_height) - 1), this.p);
            }
            int location_size = (int) ((20.0f * scale) + 0.5f);
            if (sharedPreferences.getBoolean(MainActivity.getLocationPreferenceKey(), false)) {
                location_x = (int) ((20.0f * scale) + 0.5f);
                location_y = top_y;
                if (this.ui_rotation == 90 || this.ui_rotation == 270) {
                    diff = canvas.getWidth() - canvas.getHeight();
                    location_x += diff / 2;
                    location_y -= diff / 2;
                }
                if (this.ui_rotation == 90) {
                    location_y = (canvas.getHeight() - location_y) - location_size;
                }
                if (this.ui_rotation == 180) {
                    location_x = (canvas.getWidth() - location_x) - location_size;
                }
                this.location_dest.set(location_x, location_y, location_x + location_size, location_y + location_size);
                if (main_activity.getLocation() != null) {
                    canvas.drawBitmap(this.location_bitmap, null, this.location_dest, this.p);
                    int location_radius = location_size / 10;
                    int indicator_x = location_x + location_size;
                    int indicator_y = ((location_radius / 2) + location_y) + 1;
                    this.p.setStyle(Style.FILL_AND_STROKE);
                    this.p.setColor(main_activity.getLocation().getAccuracy() < 25.01f ? Color.rgb(37, 155, 36) : Color.rgb(255, 235, 59));
                    canvas.drawCircle((float) indicator_x, (float) indicator_y, (float) location_radius, this.p);
                } else {
                    canvas.drawBitmap(this.location_off_bitmap, null, this.location_dest, this.p);
                }
            }
            if (sharedPreferences.getBoolean(MainActivity.getShowTimePreferenceKey(), true)) {
                this.p.setTextSize((14.0f * scale) + 0.5f);
                this.p.setTextAlign(Align.LEFT);
                location_x = (int) ((50.0f * scale) + 0.5f);
                location_y = top_y;
                if (this.ui_rotation == 90 || this.ui_rotation == 270) {
                    diff = canvas.getWidth() - canvas.getHeight();
                    location_x += diff / 2;
                    location_y -= diff / 2;
                }
                if (this.ui_rotation == 90) {
                    location_y = (canvas.getHeight() - location_y) - location_size;
                }
                if (this.ui_rotation == 180) {
                    location_x = canvas.getWidth() - location_x;
                    this.p.setTextAlign(Align.RIGHT);
                }
                drawTextWithBackground(canvas, this.p, DateFormat.getTimeInstance().format(Calendar.getInstance().getTime()), -1, -16777216, location_x, location_y, true);
            }
            canvas.restore();
            if (!(this.camera_controller == null || this.phase == 3 || !this.has_level_angle)) {
                if (sharedPreferences.getBoolean(MainActivity.getShowAngleLinePreferenceKey(), false)) {
                    int radius_dps = (this.ui_rotation == 90 || this.ui_rotation == 270) ? 60 : 80;
                    int radius = (int) ((((float) radius_dps) * scale) + 0.5f);
                    double angle = -this.orig_level_angle;
                    switch (main_activity.getWindowManager().getDefaultDisplay().getRotation()) {
                        case 1:
                        case 3:
                            angle += 90.0d;
                            break;
                    }
                    int off_x = (int) (((double) radius) * Math.cos(Math.toRadians(angle)));
                    int off_y = (int) (((double) radius) * Math.sin(Math.toRadians(angle)));
                    int cx = canvas.getWidth() / 2;
                    int cy = canvas.getHeight() / 2;
                    if (Math.abs(this.level_angle) <= 1.0d) {
                        this.p.setColor(Color.rgb(20, 231, 21));
                    } else {
                        this.p.setColor(-1);
                    }
                    canvas.drawLine((float) (cx - off_x), (float) (cy - off_y), (float) (cx + off_x), (float) (cy + off_y), this.p);
                }
            }
            if (this.focus_success != 3) {
                int pos_x;
                int pos_y;
                int size = (int) ((50.0f * scale) + 0.5f);
                if (this.focus_success == 1) {
                    this.p.setColor(Color.rgb(20, 231, 21));
                } else if (this.focus_success == 2) {
                    this.p.setColor(Color.rgb(229, 28, 35));
                } else {
                    this.p.setColor(-1);
                }
                this.p.setStyle(Style.STROKE);
                if (this.has_focus_area) {
                    pos_x = this.focus_screen_x;
                    pos_y = this.focus_screen_y;
                } else {
                    pos_x = canvas.getWidth() / 2;
                    pos_y = canvas.getHeight() / 2;
                }
                canvas.drawRect((float) (pos_x - size), (float) (pos_y - size), (float) (pos_x + size), (float) (pos_y + size), this.p);
                if (this.focus_complete_time != -1 && System.currentTimeMillis() > this.focus_complete_time + 1000) {
                    this.focus_success = 3;
                }
                this.p.setStyle(Style.FILL);
            }
            if (this.using_face_detection && this.faces_detected != null) {
                this.p.setColor(Color.rgb(255, 235, 59));
                this.p.setStyle(Style.STROKE);
                for (Face face : this.faces_detected) {
                    if (face.score >= 50) {
                        calculateCameraToPreviewMatrix();
                        this.face_rect.set(face.rect);
                        this.camera_to_preview_matrix.mapRect(this.face_rect);
                        canvas.drawRect(this.face_rect, this.p);
                    }
                }
                this.p.setStyle(Style.FILL);
            }
        }
    }

    private void drawTextWithBackground(Canvas canvas, Paint paint, String text, int foreground, int background, int location_x, int location_y) {
        drawTextWithBackground(canvas, paint, text, foreground, background, location_x, location_y, false);
    }

    private void drawTextWithBackground(Canvas canvas, Paint paint, String text, int foreground, int background, int location_x, int location_y, boolean align_top) {
        drawTextWithBackground(canvas, paint, text, foreground, background, location_x, location_y, align_top, null);
    }

    private void drawTextWithBackground(Canvas canvas, Paint paint, String text, int foreground, int background, int location_x, int location_y, boolean align_top, String ybounds_text) {
        Rect rect;
        float scale = getResources().getDisplayMetrics().density;
        this.p.setStyle(Style.FILL);
        paint.setColor(background);
        paint.setAlpha(64);
        int alt_height = 0;
        if (ybounds_text != null) {
            paint.getTextBounds(ybounds_text, 0, ybounds_text.length(), this.text_bounds);
            alt_height = this.text_bounds.bottom - this.text_bounds.top;
        }
        paint.getTextBounds(text, 0, text.length(), this.text_bounds);
        if (ybounds_text != null) {
            this.text_bounds.bottom = this.text_bounds.top + alt_height;
        }
        int padding = (int) ((2.0f * scale) + 0.5f);
        if (paint.getTextAlign() == Align.RIGHT || paint.getTextAlign() == Align.CENTER) {
            float width = paint.measureText(text);
            if (paint.getTextAlign() == Align.CENTER) {
                width /= 2.0f;
            }
            rect = this.text_bounds;
            rect.left = (int) (((float) rect.left) - width);
            rect = this.text_bounds;
            rect.right = (int) (((float) rect.right) - width);
        }
        rect = this.text_bounds;
        rect.left += location_x - padding;
        rect = this.text_bounds;
        rect.right += location_x + padding;
        if (align_top) {
            int height = (this.text_bounds.bottom - this.text_bounds.top) + (padding * 2);
            int y_diff = ((-this.text_bounds.top) + padding) - 1;
            this.text_bounds.top = location_y - 1;
            this.text_bounds.bottom = this.text_bounds.top + height;
            location_y += y_diff;
        } else {
            rect = this.text_bounds;
            rect.top += location_y - padding;
            rect = this.text_bounds;
            rect.bottom += location_y + padding;
        }
        canvas.drawRect(this.text_bounds, paint);
        paint.setColor(foreground);
        canvas.drawText(text, (float) location_x, (float) location_y, paint);
    }

    public void scaleZoom(float scale_factor) {
        if (this.camera_controller != null && this.has_zoom) {
            float zoom_ratio = (((float) ((Integer) this.zoom_ratios.get(this.zoom_factor)).intValue()) / 100.0f) * scale_factor;
            int new_zoom_factor = this.zoom_factor;
            if (zoom_ratio <= 1.0f) {
                new_zoom_factor = 0;
            } else if (zoom_ratio >= ((float) ((Integer) this.zoom_ratios.get(this.max_zoom_factor)).intValue()) / 100.0f) {
                new_zoom_factor = this.max_zoom_factor;
            } else if (scale_factor > 1.0f) {
                for (i = this.zoom_factor; i < this.zoom_ratios.size(); i++) {
                    if (((float) ((Integer) this.zoom_ratios.get(i)).intValue()) / 100.0f >= zoom_ratio) {
                        new_zoom_factor = i;
                        break;
                    }
                }
            } else {
                for (i = this.zoom_factor; i >= 0; i--) {
                    if (((float) ((Integer) this.zoom_ratios.get(i)).intValue()) / 100.0f <= zoom_ratio) {
                        new_zoom_factor = i;
                        break;
                    }
                }
            }
            zoomTo(new_zoom_factor, true);
        }
    }

    public void zoomIn() {
        if (this.zoom_factor < this.max_zoom_factor) {
            zoomTo(this.zoom_factor + 1, true);
        }
    }

    public void zoomOut() {
        if (this.zoom_factor > 0) {
            zoomTo(this.zoom_factor - 1, true);
        }
    }

    public void zoomTo(int new_zoom_factor, boolean update_seek_bar) {
        if (new_zoom_factor < 0) {
            new_zoom_factor = 0;
        }
        if (new_zoom_factor > this.max_zoom_factor) {
            new_zoom_factor = this.max_zoom_factor;
        }
        if (new_zoom_factor != this.zoom_factor && this.camera_controller != null && this.has_zoom) {
            this.camera_controller.setZoom(new_zoom_factor);
            this.zoom_factor = new_zoom_factor;
            if (update_seek_bar) {
                ((SeekBar) ((Activity) getContext()).findViewById(R.id.zoom_seekbar)).setProgress(this.max_zoom_factor - this.zoom_factor);
            }
            clearFocusAreas();
        }
    }

    public void changeExposure(int change, boolean update_seek_bar) {
        if (change != 0 && this.camera_controller != null) {
            if (this.min_exposure != 0 || this.max_exposure != 0) {
                setExposure(this.camera_controller.getExposureCompensation() + change, update_seek_bar);
            }
        }
    }

    public void setExposure(int new_exposure, boolean update_seek_bar) {
        if (this.camera_controller == null) {
            return;
        }
        if (this.min_exposure != 0 || this.max_exposure != 0) {
            cancelAutoFocus();
            if (new_exposure < this.min_exposure) {
                new_exposure = this.min_exposure;
            }
            if (new_exposure > this.max_exposure) {
                new_exposure = this.max_exposure;
            }
            if (this.camera_controller.setExposureCompensation(new_exposure)) {
                Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                editor.putString(MainActivity.getExposurePreferenceKey(), new_exposure);
                editor.apply();
                showToast(this.change_exposure_toast, new StringBuilder(String.valueOf(getResources().getString(R.string.exposure_compensation))).append(" ").append(new_exposure > 0 ? "+" : "").append(new DecimalFormat("#.##").format((double) (((float) new_exposure) * this.exposure_step))).append(" EV").toString());
                if (update_seek_bar) {
                    ((MainActivity) getContext()).setSeekBarExposure();
                }
            }
        }
    }

    void switchCamera() {
        if (this.phase != 2) {
            int n_cameras = this.camera_controller_manager.getNumberOfCameras();
            if (n_cameras > 1) {
                closeCamera();
                this.cameraId = (this.cameraId + 1) % n_cameras;
                if (this.camera_controller_manager.isFrontFacing(this.cameraId)) {
                    showToast(this.switch_camera_toast, R.string.front_camera);
                } else {
                    showToast(this.switch_camera_toast, R.string.back_camera);
                }
                openCamera();
                updateFocusForVideo(true);
            }
        }
    }

    private void showPhotoVideoToast() {
        MainActivity main_activity = (MainActivity) getContext();
        if (this.camera_controller != null && !main_activity.cameraInBackground()) {
            String[] entries_array;
            int index;
            String toast_string = "";
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            if (this.is_video) {
                CamcorderProfile profile = getCamcorderProfile();
                String bitrate_string = "";
                if (profile.videoBitRate >= 10000000) {
                    bitrate_string = new StringBuilder(String.valueOf(profile.videoBitRate / 1000000)).append("Mbps").toString();
                } else if (profile.videoBitRate >= 10000) {
                    bitrate_string = new StringBuilder(String.valueOf(profile.videoBitRate / 1000)).append("Kbps").toString();
                } else {
                    bitrate_string = profile.videoBitRate + "bps";
                }
                String timer_value = sharedPreferences.getString(MainActivity.getVideoMaxDurationPreferenceKey(), "0");
                toast_string = new StringBuilder(String.valueOf(getResources().getString(R.string.video))).append(": ").append(profile.videoFrameWidth).append("x").append(profile.videoFrameHeight).append(", ").append(profile.videoFrameRate).append("fps, ").append(bitrate_string).toString();
                if (!sharedPreferences.getBoolean(MainActivity.getRecordAudioPreferenceKey(), true)) {
                    toast_string = new StringBuilder(String.valueOf(toast_string)).append("\n").append(getResources().getString(R.string.audio_disabled)).toString();
                }
                if (timer_value.length() > 0 && !timer_value.equals("0")) {
                    entries_array = getResources().getStringArray(R.array.preference_video_max_duration_entries);
                    index = Arrays.asList(getResources().getStringArray(R.array.preference_video_max_duration_values)).indexOf(timer_value);
                    if (index != -1) {
                        toast_string = new StringBuilder(String.valueOf(toast_string)).append("\n").append(getResources().getString(R.string.max_duration)).append(": ").append(entries_array[index]).toString();
                    }
                }
                if (sharedPreferences.getBoolean(MainActivity.getVideoFlashPreferenceKey(), false) && supportsFlash()) {
                    toast_string = new StringBuilder(String.valueOf(toast_string)).append("\n").append(getResources().getString(R.string.preference_video_flash)).toString();
                }
            } else {
                toast_string = getResources().getString(R.string.photo);
                if (!(this.current_size_index == -1 || this.sizes == null)) {
                    Size current_size = (Size) this.sizes.get(this.current_size_index);
                    toast_string = new StringBuilder(String.valueOf(toast_string)).append(" ").append(current_size.width).append("x").append(current_size.height).toString();
                }
                if (!(this.supported_focus_values == null || this.supported_focus_values.size() <= 1 || this.current_focus_index == -1)) {
                    String focus_value = (String) this.supported_focus_values.get(this.current_focus_index);
                    if (!focus_value.equals("focus_mode_auto")) {
                        String focus_entry = findFocusEntryForValue(focus_value);
                        if (focus_entry != null) {
                            toast_string = new StringBuilder(String.valueOf(toast_string)).append("\n").append(focus_entry).toString();
                        }
                    }
                }
            }
            String iso_value = sharedPreferences.getString(MainActivity.getISOPreferenceKey(), this.camera_controller.getDefaultISO());
            if (!iso_value.equals(this.camera_controller.getDefaultISO())) {
                toast_string = new StringBuilder(String.valueOf(toast_string)).append("\nISO: ").append(iso_value).toString();
            }
            int current_exposure = this.camera_controller.getExposureCompensation();
            if (current_exposure != 0) {
                toast_string = new StringBuilder(String.valueOf(toast_string)).append("\n").append(getResources().getString(R.string.exposure)).append(": ").append(current_exposure > 0 ? "+" : "").append(new DecimalFormat("#.##").format((double) (((float) current_exposure) * this.exposure_step))).append(" EV").toString();
            }
            String scene_mode = this.camera_controller.getSceneMode();
            if (!(scene_mode == null || scene_mode.equals(this.camera_controller.getDefaultSceneMode()))) {
                toast_string = new StringBuilder(String.valueOf(toast_string)).append("\n").append(getResources().getString(R.string.scene_mode)).append(": ").append(scene_mode).toString();
            }
            String white_balance = this.camera_controller.getWhiteBalance();
            if (!(white_balance == null || white_balance.equals(this.camera_controller.getDefaultWhiteBalance()))) {
                toast_string = new StringBuilder(String.valueOf(toast_string)).append("\n").append(getResources().getString(R.string.white_balance)).append(": ").append(white_balance).toString();
            }
            String color_effect = this.camera_controller.getColorEffect();
            if (!(color_effect == null || color_effect.equals(this.camera_controller.getDefaultColorEffect()))) {
                toast_string = new StringBuilder(String.valueOf(toast_string)).append("\n").append(getResources().getString(R.string.color_effect)).append(": ").append(color_effect).toString();
            }
            String lock_orientation = sharedPreferences.getString(MainActivity.getLockOrientationPreferenceKey(), "none");
            if (!lock_orientation.equals("none")) {
                entries_array = getResources().getStringArray(R.array.preference_lock_orientation_entries);
                index = Arrays.asList(getResources().getStringArray(R.array.preference_lock_orientation_values)).indexOf(lock_orientation);
                if (index != -1) {
                    toast_string = new StringBuilder(String.valueOf(toast_string)).append("\n").append(entries_array[index]).toString();
                }
            }
            showToast(this.switch_video_toast, toast_string, 1);
        }
    }

    public int[] matchPreviewFpsToVideo(List<int[]> fps_ranges, int video_frame_rate) {
        int selected_min_fps = -1;
        int selected_max_fps = -1;
        int selected_diff = -1;
        for (int[] fps_range : fps_ranges) {
            int diff;
            int min_fps = fps_range[0];
            int max_fps = fps_range[1];
            if (min_fps <= video_frame_rate && max_fps >= video_frame_rate) {
                diff = max_fps - min_fps;
                if (selected_diff == -1 || diff < selected_diff) {
                    selected_min_fps = min_fps;
                    selected_max_fps = max_fps;
                    selected_diff = diff;
                }
            }
        }
        if (selected_min_fps == -1) {
            selected_diff = -1;
            int selected_dist = -1;
            for (int[] fps_range2 : fps_ranges) {
                int dist;
                min_fps = fps_range2[0];
                max_fps = fps_range2[1];
                diff = max_fps - min_fps;
                if (max_fps < video_frame_rate) {
                    dist = video_frame_rate - max_fps;
                } else {
                    dist = min_fps - video_frame_rate;
                }
                if (selected_dist == -1 || dist < selected_dist || (dist == selected_dist && diff < selected_diff)) {
                    selected_min_fps = min_fps;
                    selected_max_fps = max_fps;
                    selected_dist = dist;
                    selected_diff = diff;
                }
            }
        }
        return new int[]{selected_min_fps, selected_max_fps};
    }

    public int[] chooseBestPreviewFps(List<int[]> fps_ranges) {
        int selected_min_fps = -1;
        int selected_max_fps = -1;
        for (int[] fps_range : fps_ranges) {
            int min_fps = fps_range[0];
            int max_fps = fps_range[1];
            if (max_fps >= 30000) {
                if (selected_min_fps == -1 || min_fps < selected_min_fps) {
                    selected_min_fps = min_fps;
                    selected_max_fps = max_fps;
                } else if (min_fps == selected_min_fps && max_fps > selected_max_fps) {
                    selected_min_fps = min_fps;
                    selected_max_fps = max_fps;
                }
            }
        }
        if (selected_min_fps == -1) {
            int selected_diff = -1;
            for (int[] fps_range2 : fps_ranges) {
                min_fps = fps_range2[0];
                max_fps = fps_range2[1];
                int diff = max_fps - min_fps;
                if (selected_diff == -1 || diff > selected_diff) {
                    selected_min_fps = min_fps;
                    selected_max_fps = max_fps;
                    selected_diff = diff;
                } else if (diff == selected_diff && max_fps > selected_max_fps) {
                    selected_min_fps = min_fps;
                    selected_max_fps = max_fps;
                    selected_diff = diff;
                }
            }
        }
        return new int[]{selected_min_fps, selected_max_fps};
    }

    private void setPreviewFps() {
        CamcorderProfile profile = getCamcorderProfile();
        List<int[]> fps_ranges = this.camera_controller.getSupportedPreviewFpsRange();
        if (fps_ranges != null && fps_ranges.size() != 0) {
            int[] selected_fps = null;
            if (this.is_video) {
                boolean preview_too_dark = Build.MODEL.equals("Nexus 5") || Build.MODEL.equals("Nexus 6");
                if (PreferenceManager.getDefaultSharedPreferences(getContext()).getString(MainActivity.getVideoFPSPreferenceKey(), "default").equals("default") && preview_too_dark) {
                    selected_fps = chooseBestPreviewFps(fps_ranges);
                } else {
                    selected_fps = matchPreviewFpsToVideo(fps_ranges, profile.videoFrameRate * 1000);
                }
            } else {
                selected_fps = chooseBestPreviewFps(fps_ranges);
            }
            this.camera_controller.setPreviewFpsRange(selected_fps[0], selected_fps[1]);
        }
    }

    void switchVideo(boolean save, boolean update_preview_size) {
        if (this.camera_controller != null) {
            boolean old_is_video = this.is_video;
            if (this.is_video) {
                if (this.video_recorder != null) {
                    stopVideo(false);
                }
                this.is_video = false;
            } else if (isOnTimer()) {
                cancelTimer();
                this.is_video = true;
            } else if (this.phase != 2) {
                this.is_video = true;
            }
            if (this.is_video != old_is_video) {
                updateFocusForVideo(false);
                showPhotoVideoToast();
                ((ImageButton) ((Activity) getContext()).findViewById(R.id.take_photo)).setImageResource(this.is_video ? R.drawable.take_video_selector : R.drawable.take_photo_selector);
                if (save) {
                    Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                    editor.putBoolean(MainActivity.getIsVideoPreferenceKey(), this.is_video);
                    editor.apply();
                }
                if (update_preview_size) {
                    if (this.is_preview_started) {
                        this.camera_controller.stopPreview();
                        this.is_preview_started = false;
                    }
                    setPreviewSize();
                    startCameraPreview();
                }
            }
        }
    }

    boolean focusIsVideo() {
        if (this.camera_controller != null) {
            return this.camera_controller.focusIsVideo();
        }
        return false;
    }

    void updateFocusForVideo(boolean auto_focus) {
        if (this.supported_focus_values != null && this.camera_controller != null && focusIsVideo() != this.is_video) {
            updateFocus(this.is_video ? "focus_mode_continuous_video" : "focus_mode_auto", true, true, auto_focus);
        }
    }

    private String getErrorFeatures(CamcorderProfile profile) {
        boolean was_4k = false;
        boolean was_bitrate = false;
        boolean was_fps = false;
        if (profile.videoFrameWidth == 3840 && profile.videoFrameHeight == 2160) {
            was_4k = true;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (!sharedPreferences.getString(MainActivity.getVideoBitratePreferenceKey(), "default").equals("default")) {
            was_bitrate = true;
        }
        if (!sharedPreferences.getString(MainActivity.getVideoFPSPreferenceKey(), "default").equals("default")) {
            was_fps = true;
        }
        String features = "";
        if (!was_4k && !was_bitrate && !was_fps) {
            return features;
        }
        if (was_4k) {
            features = "4K UHD";
        }
        if (was_bitrate) {
            if (features.length() == 0) {
                features = "Bitrate";
            } else {
                features = new StringBuilder(String.valueOf(features)).append("/Bitrate").toString();
            }
        }
        if (!was_fps) {
            return features;
        }
        if (features.length() == 0) {
            return "Frame rate";
        }
        return new StringBuilder(String.valueOf(features)).append("/Frame rate").toString();
    }

    void cycleFlash() {
        if ((this.phase != 2 || this.is_video) && this.supported_flash_values != null && this.supported_flash_values.size() > 1) {
            updateFlash((this.current_flash_index + 1) % this.supported_flash_values.size(), true);
        }
    }

    void updateFlash(String focus_value) {
        if (this.phase != 2 || this.is_video) {
            updateFlash(focus_value, true);
        }
    }

    private boolean updateFlash(String flash_value, boolean save) {
        if (this.supported_flash_values != null) {
            int new_flash_index = this.supported_flash_values.indexOf(flash_value);
            if (new_flash_index != -1) {
                updateFlash(new_flash_index, save);
                return true;
            }
        }
        return false;
    }

    private void updateFlash(int new_flash_index, boolean save) {
        if (this.supported_flash_values != null && new_flash_index != this.current_flash_index) {
            Editor editor;
            boolean initial = this.current_flash_index == -1;
            this.current_flash_index = new_flash_index;
            String[] flash_entries = getResources().getStringArray(R.array.flash_entries);
            String flash_value = (String) this.supported_flash_values.get(this.current_flash_index);
            String[] flash_values = getResources().getStringArray(R.array.flash_values);
            for (int i = 0; i < flash_values.length; i++) {
                if (flash_value.equals(flash_values[i])) {
                    if (!initial) {
                        showToast(this.flash_toast, flash_entries[i]);
                    }
                    setPopupIcon();
                    setFlash(flash_value);
                    if (save) {
                        editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                        editor.putString(MainActivity.getFlashPreferenceKey(this.cameraId), flash_value);
                        editor.apply();
                    }
                }
            }
            setPopupIcon();
            setFlash(flash_value);
            if (save) {
                editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                editor.putString(MainActivity.getFlashPreferenceKey(this.cameraId), flash_value);
                editor.apply();
            }
        }
    }

    private void setFlash(String flash_value) {
        this.set_flash_value_after_autofocus = "";
        if (this.camera_controller != null) {
            cancelAutoFocus();
            this.camera_controller.setFlashValue(flash_value);
        }
    }

    public String getCurrentFlashValue() {
        if (this.current_flash_index == -1) {
            return null;
        }
        return (String) this.supported_flash_values.get(this.current_flash_index);
    }

    void cycleFocusMode() {
        if (this.phase != 2 && this.supported_focus_values != null && this.supported_focus_values.size() > 1) {
            updateFocus((this.current_focus_index + 1) % this.supported_focus_values.size(), false, true, true);
        }
    }

    void updateFocus(String focus_value, boolean quiet, boolean auto_focus) {
        if (this.phase != 2) {
            updateFocus(focus_value, quiet, true, auto_focus);
        }
    }

    private boolean updateFocus(String focus_value, boolean quiet, boolean save, boolean auto_focus) {
        if (this.supported_focus_values != null) {
            int new_focus_index = this.supported_focus_values.indexOf(focus_value);
            if (new_focus_index != -1) {
                updateFocus(new_focus_index, quiet, save, auto_focus);
                return true;
            }
        }
        return false;
    }

    private String findEntryForValue(String value, int entries_id, int values_id) {
        String[] entries = getResources().getStringArray(entries_id);
        String[] values = getResources().getStringArray(values_id);
        for (int i = 0; i < values.length; i++) {
            if (value.equals(values[i])) {
                return entries[i];
            }
        }
        return null;
    }

    private String findFocusEntryForValue(String focus_value) {
        return findEntryForValue(focus_value, R.array.focus_mode_entries, R.array.focus_mode_values);
    }

    private void updateFocus(int new_focus_index, boolean quiet, boolean save, boolean auto_focus) {
        if (this.supported_focus_values != null && new_focus_index != this.current_focus_index) {
            boolean initial = this.current_focus_index == -1;
            this.current_focus_index = new_focus_index;
            String focus_value = (String) this.supported_focus_values.get(this.current_focus_index);
            if (!(initial || quiet)) {
                String focus_entry = findFocusEntryForValue(focus_value);
                if (focus_entry != null) {
                    showToast(this.focus_toast, focus_entry);
                }
            }
            setFocusValue(focus_value, auto_focus);
            if (save) {
                Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                editor.putString(MainActivity.getFocusPreferenceKey(this.cameraId), focus_value);
                editor.apply();
            }
        }
    }

    public String getCurrentFocusValue() {
        if (this.camera_controller == null || this.supported_focus_values == null || this.current_focus_index == -1) {
            return null;
        }
        return (String) this.supported_focus_values.get(this.current_focus_index);
    }

    private void setFocusValue(String focus_value, boolean auto_focus) {
        if (this.camera_controller != null) {
            cancelAutoFocus();
            this.camera_controller.setFocusValue(focus_value);
            clearFocusAreas();
            if (auto_focus) {
                tryAutoFocus(false, false);
            }
        }
    }

    void toggleExposureLock() {
        if (this.camera_controller != null && this.is_exposure_lock_supported) {
            this.is_exposure_locked = !this.is_exposure_locked;
            setExposureLocked();
            showToast(this.exposure_lock_toast, this.is_exposure_locked ? R.string.exposure_locked : R.string.exposure_unlocked);
        }
    }

    private void setExposureLocked() {
        if (this.camera_controller != null && this.is_exposure_lock_supported) {
            cancelAutoFocus();
            this.camera_controller.setAutoExposureLock(this.is_exposure_locked);
            ((ImageButton) ((Activity) getContext()).findViewById(R.id.exposure_lock)).setImageResource(this.is_exposure_locked ? R.drawable.exposure_locked : R.drawable.exposure_unlocked);
        }
    }

    void takePicturePressed() {
        if (this.camera_controller == null) {
            this.phase = 0;
        } else if (!this.has_surface) {
            this.phase = 0;
        } else if (isOnTimer()) {
            cancelTimer();
            showToast(this.take_photo_toast, R.string.cancelled_timer);
        } else if (this.phase != 2) {
            long timer_delay;
            startCameraPreview();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            try {
                timer_delay = (long) (Integer.parseInt(sharedPreferences.getString(MainActivity.getTimerPreferenceKey(), "0")) * 1000);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                timer_delay = 0;
            }
            String burst_mode_value = sharedPreferences.getString(MainActivity.getBurstModePreferenceKey(), "1");
            if (burst_mode_value.equals("unlimited")) {
                this.remaining_burst_photos = -1;
            } else {
                int n_burst;
                try {
                    n_burst = Integer.parseInt(burst_mode_value);
                } catch (NumberFormatException e2) {
                    e2.printStackTrace();
                    n_burst = 1;
                }
                this.remaining_burst_photos = n_burst - 1;
            }
            if (timer_delay == 0) {
                takePicture();
            } else {
                takePictureOnTimer(timer_delay, false);
            }
        } else if (this.is_video) {
            if (this.video_start_time_set && System.currentTimeMillis() - this.video_start_time >= 500) {
                stopVideo(false);
            }
        } else if (this.remaining_burst_photos != 0) {
            this.remaining_burst_photos = 0;
            showToast(this.take_photo_toast, R.string.cancelled_burst_mode);
        }
    }

    private void takePictureOnTimer(long timer_delay, boolean repeated) {
        this.phase = 1;
        this.take_photo_time = System.currentTimeMillis() + timer_delay;
        Timer timer = this.takePictureTimer;
        TimerTask anonymousClass1TakePictureTimerTask = new TimerTask() {
            public void run() {
                if (Preview.this.beepTimerTask != null) {
                    Preview.this.beepTimerTask.cancel();
                    Preview.this.beepTimerTask = null;
                }
                ((MainActivity) Preview.this.getContext()).runOnUiThread(new Runnable() {
                    public void run() {
                        if (Preview.this.camera_controller != null && Preview.this.takePictureTimerTask != null) {
                            Preview.this.takePicture();
                        }
                    }
                });
            }
        };
        this.takePictureTimerTask = anonymousClass1TakePictureTimerTask;
        timer.schedule(anonymousClass1TakePictureTimerTask, timer_delay);
        if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(MainActivity.getTimerBeepPreferenceKey(), true)) {
            timer = this.beepTimer;
            anonymousClass1TakePictureTimerTask = new TimerTask() {
                public void run() {
                    try {
                        RingtoneManager.getRingtone(((Activity) Preview.this.getContext()).getApplicationContext(), RingtoneManager.getDefaultUri(2)).play();
                    } catch (Exception e) {
                    }
                }
            };
            this.beepTimerTask = anonymousClass1TakePictureTimerTask;
            timer.schedule(anonymousClass1TakePictureTimerTask, 0, 1000);
        }
    }

    private void flashVideo() {
        String flash_value = this.camera_controller.getFlashValue();
        if (flash_value.length() != 0) {
            String flash_value_ui = getCurrentFlashValue();
            if (flash_value_ui != null && !flash_value_ui.equals("flash_torch")) {
                if (flash_value.equals("flash_torch")) {
                    cancelAutoFocus();
                    this.camera_controller.setFlashValue(flash_value_ui);
                    return;
                }
                cancelAutoFocus();
                this.camera_controller.setFlashValue("flash_torch");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                cancelAutoFocus();
                this.camera_controller.setFlashValue(flash_value_ui);
            }
        }
    }

    private void onVideoError(int message_id, int what, int extra, String debug_value) {
        if (message_id != 0) {
            showToast(null, message_id);
        }
        Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.putString("last_video_error", debug_value);
        editor.apply();
        stopVideo(false);
    }

    @TargetApi(21)
    private void takePicture() {
        this.thumbnail_anim = false;
        this.phase = 2;
        if (this.camera_controller == null) {
            this.phase = 0;
            showGUI(true);
        } else if (this.has_surface) {
            updateParametersFromLocation();
            MainActivity main_activity = (MainActivity) getContext();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean store_location = sharedPreferences.getBoolean(MainActivity.getLocationPreferenceKey(), false);
            if (store_location) {
                if (sharedPreferences.getBoolean(MainActivity.getRequireLocationPreferenceKey(), false) && main_activity.getLocation() == null) {
                    showToast(null, R.string.location_not_available);
                    this.phase = 0;
                    showGUI(true);
                    return;
                }
            }
            if (this.is_video) {
                this.focus_success = 3;
                File videoFile = main_activity.getOutputMediaFile(2);
                if (videoFile == null) {
                    Log.e(TAG, "Couldn't create media video file; check storage permissions?");
                    showToast(null, R.string.failed_to_save_video);
                    this.phase = 0;
                    showGUI(true);
                    return;
                }
                this.video_name = videoFile.getAbsolutePath();
                CamcorderProfile profile = getCamcorderProfile();
                this.video_recorder = new MediaRecorder();
                this.camera_controller.unlock();
                this.video_recorder.setOnInfoListener(new OnInfoListener() {
                    public void onInfo(MediaRecorder mr, int what, int extra) {
                        if (what == 800 || what == MediaPlayer.MEDIA_INFO_NOT_SEEKABLE) {
                            int message_id = 0;
                            if (what == 800) {
                                message_id = R.string.video_max_duration;
                            } else if (what == MediaPlayer.MEDIA_INFO_NOT_SEEKABLE) {
                                message_id = R.string.video_max_filesize;
                            }
                            final int final_message_id = message_id;
                            final int final_what = what;
                            final int final_extra = extra;
                            ((MainActivity) Preview.this.getContext()).runOnUiThread(new Runnable() {
                                public void run() {
                                    Preview.this.onVideoError(final_message_id, final_what, final_extra, "info_" + final_what + "_" + final_extra);
                                }
                            });
                        }
                    }
                });
                this.video_recorder.setOnErrorListener(new OnErrorListener() {
                    public void onError(MediaRecorder mr, int what, int extra) {
                        int message_id = R.string.video_error_unknown;
                        if (what == 100) {
                            message_id = R.string.video_error_server_died;
                        }
                        final int final_message_id = message_id;
                        final int final_what = what;
                        final int final_extra = extra;
                        ((MainActivity) Preview.this.getContext()).runOnUiThread(new Runnable() {
                            public void run() {
                                Preview.this.onVideoError(final_message_id, final_what, final_extra, "error_" + final_what + "_" + final_extra);
                            }
                        });
                    }
                });
                this.camera_controller.initVideoRecorderPrePrepare(this.video_recorder);
                boolean record_audio = sharedPreferences.getBoolean(MainActivity.getRecordAudioPreferenceKey(), true);
                if (record_audio) {
                    String pref_audio_src = sharedPreferences.getString(MainActivity.getRecordAudioSourcePreferenceKey(), "audio_src_camcorder");
                    int audio_source = 5;
                    if (pref_audio_src.equals("audio_src_mic")) {
                        audio_source = 1;
                    } else {
                        if (pref_audio_src.equals("audio_src_default")) {
                            audio_source = 0;
                        } else {
                            if (pref_audio_src.equals("audio_src_voice_communication")) {
                                audio_source = 7;
                            }
                        }
                    }
                    this.video_recorder.setAudioSource(audio_source);
                }
                this.video_recorder.setVideoSource(this.using_android_l ? 2 : 1);
                if (store_location && main_activity.getLocation() != null) {
                    Location location = main_activity.getLocation();
                    this.video_recorder.setLocation((float) location.getLatitude(), (float) location.getLongitude());
                }
                if (record_audio) {
                    this.video_recorder.setProfile(profile);
                } else {
                    this.video_recorder.setOutputFormat(profile.fileFormat);
                    this.video_recorder.setVideoFrameRate(profile.videoFrameRate);
                    this.video_recorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
                    this.video_recorder.setVideoEncodingBitRate(profile.videoBitRate);
                    this.video_recorder.setVideoEncoder(profile.videoCodec);
                }
                this.video_recorder.setOutputFile(this.video_name);
                try {
                    long timer_delay;
                    Timer timer;
                    TimerTask anonymousClass1RestartVideoTimerTask;
                    showGUI(false);
                    if (sharedPreferences.getBoolean(MainActivity.getLockVideoPreferenceKey(), false)) {
                        main_activity.lockScreen();
                    }
                    this.cameraSurface.setVideoRecorder(this.video_recorder);
                    this.video_recorder.setOrientationHint(getImageVideoRotation());
                    this.video_recorder.prepare();
                    this.camera_controller.initVideoRecorderPostPrepare(this.video_recorder);
                    this.video_recorder.start();
                    this.video_start_time = System.currentTimeMillis();
                    this.video_start_time_set = true;
                    String timer_value = sharedPreferences.getString(MainActivity.getVideoMaxDurationPreferenceKey(), "0");
                    if (main_activity.getIntent() != null && main_activity.getIntent().hasExtra("android.intent.extra.durationLimit")) {
                        timer_value = String.valueOf(main_activity.getIntent().getIntExtra("android.intent.extra.durationLimit", 30));
                        this.remaining_restart_video = 0;
                    }
                    try {
                        timer_delay = (long) (Integer.parseInt(timer_value) * 1000);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        timer_delay = 0;
                    }
                    if (timer_delay > 0) {
                        if (this.remaining_restart_video == 0) {
                            try {
                                this.remaining_restart_video = Integer.parseInt(sharedPreferences.getString(MainActivity.getVideoRestartPreferenceKey(), "0"));
                            } catch (NumberFormatException e2) {
                                e2.printStackTrace();
                                this.remaining_restart_video = 0;
                            }
                        }
                        timer = this.restartVideoTimer;
                        anonymousClass1RestartVideoTimerTask = new TimerTask() {
                            public void run() {
                                ((MainActivity) Preview.this.getContext()).runOnUiThread(new Runnable() {
                                    public void run() {
                                        if (Preview.this.camera_controller != null && Preview.this.restartVideoTimerTask != null) {
                                            Preview.this.restartVideo();
                                        }
                                    }
                                });
                            }
                        };
                        this.restartVideoTimerTask = anonymousClass1RestartVideoTimerTask;
                        timer.schedule(anonymousClass1RestartVideoTimerTask, timer_delay);
                    }
                    if (sharedPreferences.getBoolean(MainActivity.getVideoFlashPreferenceKey(), false) && supportsFlash()) {
                        timer = this.flashVideoTimer;
                        anonymousClass1RestartVideoTimerTask = new TimerTask() {
                            public void run() {
                                ((MainActivity) Preview.this.getContext()).runOnUiThread(new Runnable() {
                                    public void run() {
                                        if (Preview.this.camera_controller != null && Preview.this.flashVideoTimerTask != null) {
                                            Preview.this.flashVideo();
                                        }
                                    }
                                });
                            }
                        };
                        this.flashVideoTimerTask = anonymousClass1RestartVideoTimerTask;
                        timer.schedule(anonymousClass1RestartVideoTimerTask, 0, 1000);
                        return;
                    }
                    return;
                } catch (IOException e3) {
                    e3.printStackTrace();
                    showToast(null, R.string.failed_to_save_video);
                    this.video_recorder.reset();
                    this.video_recorder.release();
                    this.video_recorder = null;
                    this.phase = 0;
                    showGUI(true);
                    reconnectCamera(true);
                    return;
                } catch (RuntimeException e4) {
                    e4.printStackTrace();
                    String error_message = "";
                    String features = getErrorFeatures(profile);
                    if (features.length() > 0) {
                        error_message = new StringBuilder(String.valueOf(getResources().getString(R.string.sorry))).append(", ").append(features).append(" ").append(getResources().getString(R.string.not_supported)).toString();
                    } else {
                        error_message = getResources().getString(R.string.failed_to_record_video);
                    }
                    showToast(null, error_message);
                    this.video_recorder.reset();
                    this.video_recorder.release();
                    this.video_recorder = null;
                    this.phase = 0;
                    showGUI(true);
                    reconnectCamera(true);
                    return;
                }
            }
            showGUI(false);
            String focus_value = this.current_focus_index != -1 ? (String) this.supported_focus_values.get(this.current_focus_index) : null;
            if (this.successfully_focused && System.currentTimeMillis() < this.successfully_focused_time + 5000) {
                takePictureWhenFocused();
            } else if (focus_value == null || !(focus_value.equals("focus_mode_auto") || focus_value.equals("focus_mode_macro"))) {
                takePictureWhenFocused();
            } else {
                this.focus_success = 3;
                AutoFocusCallback autoFocusCallback = new AutoFocusCallback() {
                    public void onAutoFocus(boolean success) {
                        Preview.this.takePictureWhenFocused();
                    }
                };
                try {
                    this.camera_controller.autoFocus(autoFocusCallback);
                    this.count_cameraAutoFocus++;
                } catch (RuntimeException e42) {
                    autoFocusCallback.onAutoFocus(false);
                    e42.printStackTrace();
                }
            }
        } else {
            this.phase = 0;
            showGUI(true);
        }
    }

    private void takePictureWhenFocused() {
        if (this.camera_controller == null) {
            this.phase = 0;
            showGUI(true);
        } else if (this.has_surface) {
            String focus_value;
            if (this.current_focus_index != -1) {
                focus_value = (String) this.supported_focus_values.get(this.current_focus_index);
            } else {
                focus_value = null;
            }
            if (focus_value != null && focus_value.equals("focus_mode_manual") && this.focus_success == 0) {
                cancelAutoFocus();
            }
            this.focus_success = 3;
            this.successfully_focused = false;
            PictureCallback jpegPictureCallback = new PictureCallback() {
                /* JADX WARNING: inconsistent code. */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public void onPictureTaken(byte[] data) {
                    Preview preview;
                    Options options;
                    Matrix matrix;
                    float scale;
                    Bitmap new_bitmap;
                    OutputStream outputStream;
                    OutputStream fileOutputStream;
                    IOException e;
                    File tempFile;
                    ExifInterface exifInterface;
                    String exif_aperture;
                    String exif_datetime;
                    String exif_exposure_time;
                    String exif_flash;
                    String exif_focal_length;
                    String exif_gps_altitude;
                    String exif_gps_altitude_ref;
                    String exif_gps_datestamp;
                    String exif_gps_latitude;
                    String exif_gps_latitude_ref;
                    String exif_gps_longitude;
                    String exif_gps_longitude_ref;
                    String exif_gps_processing_method;
                    String exif_gps_timestamp;
                    String exif_iso;
                    String exif_make;
                    String exif_model;
                    String exif_orientation;
                    String exif_white_balance;
                    int sample_size;
                    Bitmap old_thumbnail;
                    int thumbnail_rotation;
                    int exif_orientation2;
                    Matrix m;
                    Bitmap rotated_thumbnail;
                    long timer_delay;
                    FileNotFoundException e2;
                    OutputStream outputStream2;
                    System.gc();
                    MainActivity main_activity = (MainActivity) Preview.this.getContext();
                    boolean image_capture_intent = false;
                    Uri image_capture_intent_uri = null;
                    if ("android.media.action.IMAGE_CAPTURE".equals(main_activity.getIntent().getAction())) {
                        image_capture_intent = true;
                        Bundle myExtras = main_activity.getIntent().getExtras();
                        if (myExtras != null) {
                            image_capture_intent_uri = (Uri) myExtras.getParcelable("output");
                        }
                    }
                    boolean success = false;
                    Bitmap bitmap = null;
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Preview.this.getContext());
                    if (sharedPreferences.getBoolean(MainActivity.getAutoStabilisePreferenceKey(), false) && Preview.this.has_level_angle && main_activity.supportsAutoStabilise()) {
                        if (Preview.this.test_have_angle) {
                            Preview.this.level_angle = (double) Preview.this.test_angle;
                        }
                        while (Preview.this.level_angle < -90.0d) {
                            preview = Preview.this;
                            preview.level_angle = preview.level_angle + 180.0d;
                        }
                        while (Preview.this.level_angle > 90.0d) {
                            preview = Preview.this;
                            preview.level_angle = preview.level_angle - 180.0d;
                        }
                        options = new Options();
                        if (VERSION.SDK_INT <= 19) {
                            options.inPurgeable = true;
                        }
                        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                        if (bitmap == null) {
                            Preview.this.showToast(null, R.string.failed_to_auto_stabilise);
                            System.gc();
                        } else {
                            int width = bitmap.getWidth();
                            int height = bitmap.getHeight();
                            if (Preview.this.test_low_memory) {
                                Preview.this.level_angle = 45.0d;
                            }
                            matrix = new Matrix();
                            double level_angle_rad_abs = Math.abs(Math.toRadians(Preview.this.level_angle));
                            int w1 = width;
                            int h1 = height;
                            double w0 = (((double) w1) * Math.cos(level_angle_rad_abs)) + (((double) h1) * Math.sin(level_angle_rad_abs));
                            double h0 = (((double) w1) * Math.sin(level_angle_rad_abs)) + (((double) h1) * Math.cos(level_angle_rad_abs));
                            scale = (float) Math.sqrt((double) (((float) (w1 * h1)) / ((float) (w0 * h0))));
                            if (Preview.this.test_low_memory) {
                                scale *= 2.0f;
                            }
                            matrix.postScale(scale, scale);
                            w0 *= (double) scale;
                            h0 *= (double) scale;
                            w1 = (int) (((float) w1) * scale);
                            h1 = (int) (((float) h1) * scale);
                            if (Preview.this.camera_controller == null || !Preview.this.camera_controller.isFrontFacing()) {
                                matrix.postRotate((float) Preview.this.level_angle);
                            } else {
                                matrix.postRotate((float) (-Preview.this.level_angle));
                            }
                            new_bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                            if (new_bitmap != bitmap) {
                                bitmap.recycle();
                                bitmap = new_bitmap;
                            }
                            System.gc();
                            double tan_theta = Math.tan(level_angle_rad_abs);
                            double sin_theta = Math.sin(level_angle_rad_abs);
                            double denom = (h0 / w0) + tan_theta;
                            double alt_denom = (w0 / h0) + tan_theta;
                            if (denom != 0.0d && denom >= 1.0E-14d && alt_denom != 0.0d && alt_denom >= 1.0E-14d) {
                                int w2 = (int) ((((((2.0d * ((double) h1)) * sin_theta) * tan_theta) + h0) - (w0 * tan_theta)) / denom);
                                int h2 = (int) ((((double) w2) * h0) / w0);
                                int alt_h2 = (int) ((((((2.0d * ((double) w1)) * sin_theta) * tan_theta) + w0) - (h0 * tan_theta)) / alt_denom);
                                int alt_w2 = (int) ((((double) alt_h2) * w0) / h0);
                                if (alt_w2 < w2) {
                                    w2 = alt_w2;
                                    h2 = alt_h2;
                                }
                                if (w2 <= 0) {
                                    w2 = 1;
                                } else if (w2 >= bitmap.getWidth()) {
                                    w2 = bitmap.getWidth() - 1;
                                }
                                if (h2 <= 0) {
                                    h2 = 1;
                                } else if (h2 >= bitmap.getHeight()) {
                                    h2 = bitmap.getHeight() - 1;
                                }
                                new_bitmap = Bitmap.createBitmap(bitmap, (bitmap.getWidth() - w2) / 2, (bitmap.getHeight() - h2) / 2, w2, h2);
                                if (new_bitmap != bitmap) {
                                    bitmap.recycle();
                                    bitmap = new_bitmap;
                                }
                                System.gc();
                            }
                        }
                    }
                    String preference_stamp = sharedPreferences.getString(MainActivity.getStampPreferenceKey(), "preference_stamp_no");
                    String preference_textstamp = sharedPreferences.getString(MainActivity.getTextStampPreferenceKey(), "");
                    boolean dategeo_stamp = preference_stamp.equals("preference_stamp_yes");
                    if (preference_textstamp.length() > 0) {
                    }
                    if (!(null == null && null == null)) {
                        if (bitmap == null) {
                            options = new Options();
                            options.inMutable = true;
                            if (VERSION.SDK_INT <= 19) {
                                options.inPurgeable = true;
                            }
                            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                            if (bitmap == null) {
                                Preview.this.showToast(null, R.string.failed_to_stamp);
                                System.gc();
                            }
                        }
                        if (bitmap != null) {
                            int smallest_size;
                            width = bitmap.getWidth();
                            height = bitmap.getHeight();
                            Canvas canvas = new Canvas(bitmap);
                            Preview.this.p.setColor(-1);
                            int font_size = 20;
                            try {
                                font_size = Integer.parseInt(sharedPreferences.getString(MainActivity.getStampFontSizePreferenceKey(), "12"));
                            } catch (NumberFormatException e3) {
                            }
                            if (width < height) {
                                smallest_size = width;
                            } else {
                                smallest_size = height;
                            }
                            scale = ((float) smallest_size) / 288.0f;
                            Preview.this.p.setTextSize((float) ((int) ((((float) font_size) * scale) + 0.5f)));
                            int offset_x = (int) ((8.0f * scale) + 0.5f);
                            int diff_y = (int) ((((float) (font_size + 4)) * scale) + 0.5f);
                            int ypos = height - ((int) ((8.0f * scale) + 0.5f));
                            Preview.this.p.setTextAlign(Align.RIGHT);
                            if (null != null) {
                                Preview.this.drawTextWithBackground(canvas, Preview.this.p, DateFormat.getDateTimeInstance().format(new Date()), -1, -16777216, width - offset_x, ypos);
                                ypos -= diff_y;
                                String location_string = "";
                                if (sharedPreferences.getBoolean(MainActivity.getLocationPreferenceKey(), false) && main_activity.getLocation() != null) {
                                    Location location = main_activity.getLocation();
                                    location_string = new StringBuilder(String.valueOf(location_string)).append(Location.convert(location.getLatitude(), 0)).append(", ").append(Location.convert(location.getLongitude(), 0)).toString();
                                    if (location.hasAltitude()) {
                                        location_string = new StringBuilder(String.valueOf(location_string)).append(", ").append(Preview.this.decimalFormat.format(location.getAltitude())).append(Preview.this.getResources().getString(R.string.metres_abbreviation)).toString();
                                    }
                                }
                                if (Preview.this.has_geo_direction) {
                                    if (sharedPreferences.getBoolean(MainActivity.getGPSDirectionPreferenceKey(), false)) {
                                        float geo_angle = (float) Math.toDegrees((double) Preview.this.geo_direction[0]);
                                        if (geo_angle < 0.0f) {
                                            geo_angle += 360.0f;
                                        }
                                        if (location_string.length() > 0) {
                                            location_string = new StringBuilder(String.valueOf(location_string)).append(", ").toString();
                                        }
                                        location_string = new StringBuilder(String.valueOf(location_string)).append(Math.round(geo_angle)).append('°').toString();
                                    }
                                }
                                if (location_string.length() > 0) {
                                    Preview.this.drawTextWithBackground(canvas, Preview.this.p, location_string, -1, -16777216, width - offset_x, ypos);
                                    ypos -= diff_y;
                                }
                            }
                            if (null != null) {
                                Preview.this.drawTextWithBackground(canvas, Preview.this.p, preference_textstamp, -1, -16777216, width - offset_x, ypos);
                                int i = ypos - diff_y;
                            }
                        }
                    }
                    String exif_orientation_s = null;
                    String str = null;
                    File picFile = null;
                    if (!image_capture_intent) {
                        picFile = main_activity.getOutputMediaFile(1);
                        if (picFile == null) {
                            Log.e(Preview.TAG, "Couldn't create media image file; check storage permissions?");
                            Preview.this.showToast(null, R.string.failed_to_save_image);
                            outputStream = null;
                        } else {
                            str = picFile.getAbsolutePath();
                            fileOutputStream = new FileOutputStream(picFile);
                        }
                    } else if (image_capture_intent_uri != null) {
                        try {
                            outputStream = main_activity.getContentResolver().openOutputStream(image_capture_intent_uri);
                        } catch (IOException e4) {
                            e = e4;
                            e.printStackTrace();
                            success = true;
                            if (picFile != null) {
                                if (bitmap == null) {
                                    tempFile = File.createTempFile("opencamera_exif", "");
                                    fileOutputStream = new FileOutputStream(tempFile);
                                    fileOutputStream.write(data);
                                    fileOutputStream.close();
                                    exifInterface = new ExifInterface(tempFile.getAbsolutePath());
                                    exif_aperture = exifInterface.getAttribute("FNumber");
                                    exif_datetime = exifInterface.getAttribute("DateTime");
                                    exif_exposure_time = exifInterface.getAttribute("ExposureTime");
                                    exif_flash = exifInterface.getAttribute("Flash");
                                    exif_focal_length = exifInterface.getAttribute("FocalLength");
                                    exif_gps_altitude = exifInterface.getAttribute("GPSAltitude");
                                    exif_gps_altitude_ref = exifInterface.getAttribute("GPSAltitudeRef");
                                    exif_gps_datestamp = exifInterface.getAttribute("GPSDateStamp");
                                    exif_gps_latitude = exifInterface.getAttribute("GPSLatitude");
                                    exif_gps_latitude_ref = exifInterface.getAttribute("GPSLatitudeRef");
                                    exif_gps_longitude = exifInterface.getAttribute("GPSLongitude");
                                    exif_gps_longitude_ref = exifInterface.getAttribute("GPSLongitudeRef");
                                    exif_gps_processing_method = exifInterface.getAttribute("GPSProcessingMethod");
                                    exif_gps_timestamp = exifInterface.getAttribute("GPSTimeStamp");
                                    exif_iso = exifInterface.getAttribute("ISOSpeedRatings");
                                    exif_make = exifInterface.getAttribute("Make");
                                    exif_model = exifInterface.getAttribute("Model");
                                    exif_orientation = exifInterface.getAttribute("Orientation");
                                    exif_orientation_s = exif_orientation;
                                    exif_white_balance = exifInterface.getAttribute("WhiteBalance");
                                    if (tempFile.delete()) {
                                        exifInterface = new ExifInterface(picFile.getAbsolutePath());
                                    } else {
                                        exifInterface = new ExifInterface(picFile.getAbsolutePath());
                                    }
                                    if (exif_aperture != null) {
                                        exifInterface.setAttribute("FNumber", exif_aperture);
                                    }
                                    if (exif_datetime != null) {
                                        exifInterface.setAttribute("DateTime", exif_datetime);
                                    }
                                    if (exif_exposure_time != null) {
                                        exifInterface.setAttribute("ExposureTime", exif_exposure_time);
                                    }
                                    if (exif_flash != null) {
                                        exifInterface.setAttribute("Flash", exif_flash);
                                    }
                                    if (exif_focal_length != null) {
                                        exifInterface.setAttribute("FocalLength", exif_focal_length);
                                    }
                                    if (exif_gps_altitude != null) {
                                        exifInterface.setAttribute("GPSAltitude", exif_gps_altitude);
                                    }
                                    if (exif_gps_altitude_ref != null) {
                                        exifInterface.setAttribute("GPSAltitudeRef", exif_gps_altitude_ref);
                                    }
                                    if (exif_gps_datestamp != null) {
                                        exifInterface.setAttribute("GPSDateStamp", exif_gps_datestamp);
                                    }
                                    if (exif_gps_latitude != null) {
                                        exifInterface.setAttribute("GPSLatitude", exif_gps_latitude);
                                    }
                                    if (exif_gps_latitude_ref != null) {
                                        exifInterface.setAttribute("GPSLatitudeRef", exif_gps_latitude_ref);
                                    }
                                    if (exif_gps_longitude != null) {
                                        exifInterface.setAttribute("GPSLongitude", exif_gps_longitude);
                                    }
                                    if (exif_gps_longitude_ref != null) {
                                        exifInterface.setAttribute("GPSLongitudeRef", exif_gps_longitude_ref);
                                    }
                                    if (exif_gps_processing_method != null) {
                                        exifInterface.setAttribute("GPSProcessingMethod", exif_gps_processing_method);
                                    }
                                    if (exif_gps_timestamp != null) {
                                        exifInterface.setAttribute("GPSTimeStamp", exif_gps_timestamp);
                                    }
                                    if (exif_iso != null) {
                                        exifInterface.setAttribute("ISOSpeedRatings", exif_iso);
                                    }
                                    if (exif_make != null) {
                                        exifInterface.setAttribute("Make", exif_make);
                                    }
                                    if (exif_model != null) {
                                        exifInterface.setAttribute("Model", exif_model);
                                    }
                                    if (exif_orientation != null) {
                                        exifInterface.setAttribute("Orientation", exif_orientation);
                                    }
                                    if (exif_white_balance != null) {
                                        exifInterface.setAttribute("WhiteBalance", exif_white_balance);
                                    }
                                    Preview.this.setGPSDirectionExif(exifInterface);
                                    Preview.this.setDateTimeExif(exifInterface);
                                    exifInterface.saveAttributes();
                                } else if (Preview.this.has_geo_direction) {
                                    if (sharedPreferences.getBoolean(MainActivity.getGPSDirectionPreferenceKey(), false)) {
                                        long currentTimeMillis = System.currentTimeMillis();
                                        exifInterface = new ExifInterface(picFile.getAbsolutePath());
                                        Preview.this.setGPSDirectionExif(exifInterface);
                                        Preview.this.setDateTimeExif(exifInterface);
                                        exifInterface.saveAttributes();
                                    }
                                }
                                if (!image_capture_intent) {
                                    main_activity.broadcastFile(picFile, true, false);
                                    Preview.this.test_last_saved_image = str;
                                }
                            }
                            if (image_capture_intent) {
                                main_activity.setResult(-1);
                                main_activity.finish();
                            }
                            if (!Preview.this.using_android_l) {
                                Preview.this.is_preview_started = false;
                            }
                            Preview.this.phase = 0;
                            if (Preview.this.remaining_burst_photos == -1) {
                            }
                            if (!Preview.this.is_preview_started) {
                                Preview.this.startCameraPreview();
                            }
                            currentTimeMillis = System.currentTimeMillis();
                            sample_size = Integer.highestOneBit((int) Math.ceil(((double) Preview.this.camera_controller.getPictureSize().width) / ((double) Preview.this.cameraSurface.getView().getWidth()))) * 4;
                            if (!sharedPreferences.getBoolean(MainActivity.getThumbnailAnimationPreferenceKey(), true)) {
                                sample_size *= 4;
                            }
                            old_thumbnail = Preview.this.thumbnail;
                            if (bitmap == null) {
                                width = bitmap.getWidth();
                                height = bitmap.getHeight();
                                matrix = new Matrix();
                                scale = 1.0f / ((float) sample_size);
                                matrix.postScale(scale, scale);
                                Preview.this.thumbnail = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                            } else {
                                options = new Options();
                                options.inMutable = false;
                                if (VERSION.SDK_INT <= 19) {
                                    options.inPurgeable = true;
                                }
                                options.inSampleSize = sample_size;
                                Preview.this.thumbnail = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                            }
                            thumbnail_rotation = 0;
                            if (exif_orientation_s == null) {
                                try {
                                    exif_orientation_s = new ExifInterface(picFile.getAbsolutePath()).getAttribute("Orientation");
                                } catch (IOException exception) {
                                    exception.printStackTrace();
                                }
                            }
                            exif_orientation2 = 0;
                            if (!exif_orientation_s.equals("0")) {
                                if (!exif_orientation_s.equals("1")) {
                                    if (exif_orientation_s.equals("3")) {
                                        if (exif_orientation_s.equals("6")) {
                                            if (exif_orientation_s.equals("8")) {
                                                exif_orientation2 = 270;
                                            }
                                        } else {
                                            exif_orientation2 = 90;
                                        }
                                    } else {
                                        exif_orientation2 = 180;
                                    }
                                }
                            }
                            thumbnail_rotation = (0 + exif_orientation2) % 360;
                            if (thumbnail_rotation != 0) {
                                m = new Matrix();
                                m.setRotate((float) thumbnail_rotation, ((float) Preview.this.thumbnail.getWidth()) * 0.5f, ((float) Preview.this.thumbnail.getHeight()) * 0.5f);
                                rotated_thumbnail = Bitmap.createBitmap(Preview.this.thumbnail, 0, 0, Preview.this.thumbnail.getWidth(), Preview.this.thumbnail.getHeight(), m, true);
                                if (rotated_thumbnail != Preview.this.thumbnail) {
                                    Preview.this.thumbnail.recycle();
                                    Preview.this.thumbnail = rotated_thumbnail;
                                }
                            }
                            if (sharedPreferences.getBoolean(MainActivity.getThumbnailAnimationPreferenceKey(), true)) {
                                Preview.this.thumbnail_anim = true;
                                Preview.this.thumbnail_anim_start_ms = System.currentTimeMillis();
                            }
                            main_activity.updateGalleryIconToBitmap(Preview.this.thumbnail);
                            if (old_thumbnail != null) {
                                old_thumbnail.recycle();
                            }
                            if (bitmap != null) {
                                bitmap.recycle();
                            }
                            System.gc();
                            if (Preview.this.remaining_burst_photos != -1) {
                            }
                            if (Preview.this.remaining_burst_photos > 0) {
                                preview = Preview.this;
                                preview.remaining_burst_photos = preview.remaining_burst_photos - 1;
                            }
                            try {
                                timer_delay = (long) (Integer.parseInt(sharedPreferences.getString(MainActivity.getBurstIntervalPreferenceKey(), "0")) * 1000);
                            } catch (NumberFormatException e5) {
                                e5.printStackTrace();
                                timer_delay = 0;
                            }
                            if (timer_delay == 0) {
                                Preview.this.takePictureOnTimer(timer_delay, true);
                            }
                            Preview.this.phase = 2;
                            Preview.this.showGUI(false);
                            Preview.this.takePictureWhenFocused();
                            return;
                        } catch (FileNotFoundException e6) {
                            e2 = e6;
                            e2.getStackTrace();
                            Preview.this.showToast(null, R.string.failed_to_save_photo);
                            if (Preview.this.using_android_l) {
                                Preview.this.is_preview_started = false;
                            }
                            Preview.this.phase = 0;
                            if (Preview.this.remaining_burst_photos == -1) {
                            }
                            if (Preview.this.is_preview_started) {
                                Preview.this.startCameraPreview();
                            }
                            currentTimeMillis = System.currentTimeMillis();
                            sample_size = Integer.highestOneBit((int) Math.ceil(((double) Preview.this.camera_controller.getPictureSize().width) / ((double) Preview.this.cameraSurface.getView().getWidth()))) * 4;
                            if (sharedPreferences.getBoolean(MainActivity.getThumbnailAnimationPreferenceKey(), true)) {
                                sample_size *= 4;
                            }
                            old_thumbnail = Preview.this.thumbnail;
                            if (bitmap == null) {
                                options = new Options();
                                options.inMutable = false;
                                if (VERSION.SDK_INT <= 19) {
                                    options.inPurgeable = true;
                                }
                                options.inSampleSize = sample_size;
                                Preview.this.thumbnail = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                            } else {
                                width = bitmap.getWidth();
                                height = bitmap.getHeight();
                                matrix = new Matrix();
                                scale = 1.0f / ((float) sample_size);
                                matrix.postScale(scale, scale);
                                Preview.this.thumbnail = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                            }
                            thumbnail_rotation = 0;
                            if (exif_orientation_s == null) {
                                exif_orientation_s = new ExifInterface(picFile.getAbsolutePath()).getAttribute("Orientation");
                            }
                            exif_orientation2 = 0;
                            if (exif_orientation_s.equals("0")) {
                                if (exif_orientation_s.equals("1")) {
                                    if (exif_orientation_s.equals("3")) {
                                        exif_orientation2 = 180;
                                    } else {
                                        if (exif_orientation_s.equals("6")) {
                                            exif_orientation2 = 90;
                                        } else {
                                            if (exif_orientation_s.equals("8")) {
                                                exif_orientation2 = 270;
                                            }
                                        }
                                    }
                                }
                            }
                            thumbnail_rotation = (0 + exif_orientation2) % 360;
                            if (thumbnail_rotation != 0) {
                                m = new Matrix();
                                m.setRotate((float) thumbnail_rotation, ((float) Preview.this.thumbnail.getWidth()) * 0.5f, ((float) Preview.this.thumbnail.getHeight()) * 0.5f);
                                rotated_thumbnail = Bitmap.createBitmap(Preview.this.thumbnail, 0, 0, Preview.this.thumbnail.getWidth(), Preview.this.thumbnail.getHeight(), m, true);
                                if (rotated_thumbnail != Preview.this.thumbnail) {
                                    Preview.this.thumbnail.recycle();
                                    Preview.this.thumbnail = rotated_thumbnail;
                                }
                            }
                            if (sharedPreferences.getBoolean(MainActivity.getThumbnailAnimationPreferenceKey(), true)) {
                                Preview.this.thumbnail_anim = true;
                                Preview.this.thumbnail_anim_start_ms = System.currentTimeMillis();
                            }
                            main_activity.updateGalleryIconToBitmap(Preview.this.thumbnail);
                            if (old_thumbnail != null) {
                                old_thumbnail.recycle();
                            }
                            if (bitmap != null) {
                                bitmap.recycle();
                            }
                            System.gc();
                            if (Preview.this.remaining_burst_photos != -1) {
                            }
                            if (Preview.this.remaining_burst_photos > 0) {
                                preview = Preview.this;
                                preview.remaining_burst_photos = preview.remaining_burst_photos - 1;
                            }
                            timer_delay = (long) (Integer.parseInt(sharedPreferences.getString(MainActivity.getBurstIntervalPreferenceKey(), "0")) * 1000);
                            if (timer_delay == 0) {
                                Preview.this.phase = 2;
                                Preview.this.showGUI(false);
                                Preview.this.takePictureWhenFocused();
                                return;
                            }
                            Preview.this.takePictureOnTimer(timer_delay, true);
                        }
                    } else {
                        if (bitmap == null) {
                            options = new Options();
                            if (VERSION.SDK_INT <= 19) {
                                options.inPurgeable = true;
                            }
                            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                        }
                        if (bitmap != null) {
                            width = bitmap.getWidth();
                            height = bitmap.getHeight();
                            if (width > 128) {
                                scale = 128.0f / ((float) width);
                                matrix = new Matrix();
                                matrix.postScale(scale, scale);
                                new_bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                                if (new_bitmap != bitmap) {
                                    bitmap.recycle();
                                    bitmap = new_bitmap;
                                }
                            }
                        }
                        main_activity.setResult(-1, new Intent(CropImage.ACTION_INLINE_DATA).putExtra("data", bitmap));
                        main_activity.finish();
                        outputStream = null;
                    }
                    if (outputStream != null) {
                        if (bitmap != null) {
                            try {
                                bitmap.compress(CompressFormat.JPEG, Preview.this.getImageQuality(), outputStream);
                            } catch (IOException e7) {
                                e = e7;
                                outputStream2 = outputStream;
                                e.printStackTrace();
                                success = true;
                                if (picFile != null) {
                                    if (bitmap == null) {
                                        tempFile = File.createTempFile("opencamera_exif", "");
                                        fileOutputStream = new FileOutputStream(tempFile);
                                        fileOutputStream.write(data);
                                        fileOutputStream.close();
                                        exifInterface = new ExifInterface(tempFile.getAbsolutePath());
                                        exif_aperture = exifInterface.getAttribute("FNumber");
                                        exif_datetime = exifInterface.getAttribute("DateTime");
                                        exif_exposure_time = exifInterface.getAttribute("ExposureTime");
                                        exif_flash = exifInterface.getAttribute("Flash");
                                        exif_focal_length = exifInterface.getAttribute("FocalLength");
                                        exif_gps_altitude = exifInterface.getAttribute("GPSAltitude");
                                        exif_gps_altitude_ref = exifInterface.getAttribute("GPSAltitudeRef");
                                        exif_gps_datestamp = exifInterface.getAttribute("GPSDateStamp");
                                        exif_gps_latitude = exifInterface.getAttribute("GPSLatitude");
                                        exif_gps_latitude_ref = exifInterface.getAttribute("GPSLatitudeRef");
                                        exif_gps_longitude = exifInterface.getAttribute("GPSLongitude");
                                        exif_gps_longitude_ref = exifInterface.getAttribute("GPSLongitudeRef");
                                        exif_gps_processing_method = exifInterface.getAttribute("GPSProcessingMethod");
                                        exif_gps_timestamp = exifInterface.getAttribute("GPSTimeStamp");
                                        exif_iso = exifInterface.getAttribute("ISOSpeedRatings");
                                        exif_make = exifInterface.getAttribute("Make");
                                        exif_model = exifInterface.getAttribute("Model");
                                        exif_orientation = exifInterface.getAttribute("Orientation");
                                        exif_orientation_s = exif_orientation;
                                        exif_white_balance = exifInterface.getAttribute("WhiteBalance");
                                        if (tempFile.delete()) {
                                            exifInterface = new ExifInterface(picFile.getAbsolutePath());
                                        } else {
                                            exifInterface = new ExifInterface(picFile.getAbsolutePath());
                                        }
                                        if (exif_aperture != null) {
                                            exifInterface.setAttribute("FNumber", exif_aperture);
                                        }
                                        if (exif_datetime != null) {
                                            exifInterface.setAttribute("DateTime", exif_datetime);
                                        }
                                        if (exif_exposure_time != null) {
                                            exifInterface.setAttribute("ExposureTime", exif_exposure_time);
                                        }
                                        if (exif_flash != null) {
                                            exifInterface.setAttribute("Flash", exif_flash);
                                        }
                                        if (exif_focal_length != null) {
                                            exifInterface.setAttribute("FocalLength", exif_focal_length);
                                        }
                                        if (exif_gps_altitude != null) {
                                            exifInterface.setAttribute("GPSAltitude", exif_gps_altitude);
                                        }
                                        if (exif_gps_altitude_ref != null) {
                                            exifInterface.setAttribute("GPSAltitudeRef", exif_gps_altitude_ref);
                                        }
                                        if (exif_gps_datestamp != null) {
                                            exifInterface.setAttribute("GPSDateStamp", exif_gps_datestamp);
                                        }
                                        if (exif_gps_latitude != null) {
                                            exifInterface.setAttribute("GPSLatitude", exif_gps_latitude);
                                        }
                                        if (exif_gps_latitude_ref != null) {
                                            exifInterface.setAttribute("GPSLatitudeRef", exif_gps_latitude_ref);
                                        }
                                        if (exif_gps_longitude != null) {
                                            exifInterface.setAttribute("GPSLongitude", exif_gps_longitude);
                                        }
                                        if (exif_gps_longitude_ref != null) {
                                            exifInterface.setAttribute("GPSLongitudeRef", exif_gps_longitude_ref);
                                        }
                                        if (exif_gps_processing_method != null) {
                                            exifInterface.setAttribute("GPSProcessingMethod", exif_gps_processing_method);
                                        }
                                        if (exif_gps_timestamp != null) {
                                            exifInterface.setAttribute("GPSTimeStamp", exif_gps_timestamp);
                                        }
                                        if (exif_iso != null) {
                                            exifInterface.setAttribute("ISOSpeedRatings", exif_iso);
                                        }
                                        if (exif_make != null) {
                                            exifInterface.setAttribute("Make", exif_make);
                                        }
                                        if (exif_model != null) {
                                            exifInterface.setAttribute("Model", exif_model);
                                        }
                                        if (exif_orientation != null) {
                                            exifInterface.setAttribute("Orientation", exif_orientation);
                                        }
                                        if (exif_white_balance != null) {
                                            exifInterface.setAttribute("WhiteBalance", exif_white_balance);
                                        }
                                        Preview.this.setGPSDirectionExif(exifInterface);
                                        Preview.this.setDateTimeExif(exifInterface);
                                        exifInterface.saveAttributes();
                                    } else if (Preview.this.has_geo_direction) {
                                        if (sharedPreferences.getBoolean(MainActivity.getGPSDirectionPreferenceKey(), false)) {
                                            long currentTimeMillis2 = System.currentTimeMillis();
                                            exifInterface = new ExifInterface(picFile.getAbsolutePath());
                                            Preview.this.setGPSDirectionExif(exifInterface);
                                            Preview.this.setDateTimeExif(exifInterface);
                                            exifInterface.saveAttributes();
                                        }
                                    }
                                    if (image_capture_intent) {
                                        main_activity.broadcastFile(picFile, true, false);
                                        Preview.this.test_last_saved_image = str;
                                    }
                                }
                                if (image_capture_intent) {
                                    main_activity.setResult(-1);
                                    main_activity.finish();
                                }
                                if (Preview.this.using_android_l) {
                                    Preview.this.is_preview_started = false;
                                }
                                Preview.this.phase = 0;
                                if (Preview.this.remaining_burst_photos == -1) {
                                }
                                if (Preview.this.is_preview_started) {
                                    Preview.this.startCameraPreview();
                                }
                                currentTimeMillis2 = System.currentTimeMillis();
                                sample_size = Integer.highestOneBit((int) Math.ceil(((double) Preview.this.camera_controller.getPictureSize().width) / ((double) Preview.this.cameraSurface.getView().getWidth()))) * 4;
                                if (sharedPreferences.getBoolean(MainActivity.getThumbnailAnimationPreferenceKey(), true)) {
                                    sample_size *= 4;
                                }
                                old_thumbnail = Preview.this.thumbnail;
                                if (bitmap == null) {
                                    width = bitmap.getWidth();
                                    height = bitmap.getHeight();
                                    matrix = new Matrix();
                                    scale = 1.0f / ((float) sample_size);
                                    matrix.postScale(scale, scale);
                                    Preview.this.thumbnail = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                                } else {
                                    options = new Options();
                                    options.inMutable = false;
                                    if (VERSION.SDK_INT <= 19) {
                                        options.inPurgeable = true;
                                    }
                                    options.inSampleSize = sample_size;
                                    Preview.this.thumbnail = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                                }
                                thumbnail_rotation = 0;
                                if (exif_orientation_s == null) {
                                    exif_orientation_s = new ExifInterface(picFile.getAbsolutePath()).getAttribute("Orientation");
                                }
                                exif_orientation2 = 0;
                                if (exif_orientation_s.equals("0")) {
                                    if (exif_orientation_s.equals("1")) {
                                        if (exif_orientation_s.equals("3")) {
                                            if (exif_orientation_s.equals("6")) {
                                                if (exif_orientation_s.equals("8")) {
                                                    exif_orientation2 = 270;
                                                }
                                            } else {
                                                exif_orientation2 = 90;
                                            }
                                        } else {
                                            exif_orientation2 = 180;
                                        }
                                    }
                                }
                                thumbnail_rotation = (0 + exif_orientation2) % 360;
                                if (thumbnail_rotation != 0) {
                                    m = new Matrix();
                                    m.setRotate((float) thumbnail_rotation, ((float) Preview.this.thumbnail.getWidth()) * 0.5f, ((float) Preview.this.thumbnail.getHeight()) * 0.5f);
                                    rotated_thumbnail = Bitmap.createBitmap(Preview.this.thumbnail, 0, 0, Preview.this.thumbnail.getWidth(), Preview.this.thumbnail.getHeight(), m, true);
                                    if (rotated_thumbnail != Preview.this.thumbnail) {
                                        Preview.this.thumbnail.recycle();
                                        Preview.this.thumbnail = rotated_thumbnail;
                                    }
                                }
                                if (sharedPreferences.getBoolean(MainActivity.getThumbnailAnimationPreferenceKey(), true)) {
                                    Preview.this.thumbnail_anim = true;
                                    Preview.this.thumbnail_anim_start_ms = System.currentTimeMillis();
                                }
                                main_activity.updateGalleryIconToBitmap(Preview.this.thumbnail);
                                if (old_thumbnail != null) {
                                    old_thumbnail.recycle();
                                }
                                if (bitmap != null) {
                                    bitmap.recycle();
                                }
                                System.gc();
                                if (Preview.this.remaining_burst_photos != -1) {
                                }
                                if (Preview.this.remaining_burst_photos > 0) {
                                    preview = Preview.this;
                                    preview.remaining_burst_photos = preview.remaining_burst_photos - 1;
                                }
                                timer_delay = (long) (Integer.parseInt(sharedPreferences.getString(MainActivity.getBurstIntervalPreferenceKey(), "0")) * 1000);
                                if (timer_delay == 0) {
                                    Preview.this.takePictureOnTimer(timer_delay, true);
                                }
                                Preview.this.phase = 2;
                                Preview.this.showGUI(false);
                                Preview.this.takePictureWhenFocused();
                                return;
                            } catch (FileNotFoundException e8) {
                                e2 = e8;
                                outputStream2 = outputStream;
                                e2.getStackTrace();
                                Preview.this.showToast(null, R.string.failed_to_save_photo);
                                if (Preview.this.using_android_l) {
                                    Preview.this.is_preview_started = false;
                                }
                                Preview.this.phase = 0;
                                if (Preview.this.remaining_burst_photos == -1) {
                                }
                                if (Preview.this.is_preview_started) {
                                    Preview.this.startCameraPreview();
                                }
                                currentTimeMillis2 = System.currentTimeMillis();
                                sample_size = Integer.highestOneBit((int) Math.ceil(((double) Preview.this.camera_controller.getPictureSize().width) / ((double) Preview.this.cameraSurface.getView().getWidth()))) * 4;
                                if (sharedPreferences.getBoolean(MainActivity.getThumbnailAnimationPreferenceKey(), true)) {
                                    sample_size *= 4;
                                }
                                old_thumbnail = Preview.this.thumbnail;
                                if (bitmap == null) {
                                    options = new Options();
                                    options.inMutable = false;
                                    if (VERSION.SDK_INT <= 19) {
                                        options.inPurgeable = true;
                                    }
                                    options.inSampleSize = sample_size;
                                    Preview.this.thumbnail = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                                } else {
                                    width = bitmap.getWidth();
                                    height = bitmap.getHeight();
                                    matrix = new Matrix();
                                    scale = 1.0f / ((float) sample_size);
                                    matrix.postScale(scale, scale);
                                    Preview.this.thumbnail = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                                }
                                thumbnail_rotation = 0;
                                if (exif_orientation_s == null) {
                                    exif_orientation_s = new ExifInterface(picFile.getAbsolutePath()).getAttribute("Orientation");
                                }
                                exif_orientation2 = 0;
                                if (exif_orientation_s.equals("0")) {
                                    if (exif_orientation_s.equals("1")) {
                                        if (exif_orientation_s.equals("3")) {
                                            exif_orientation2 = 180;
                                        } else {
                                            if (exif_orientation_s.equals("6")) {
                                                exif_orientation2 = 90;
                                            } else {
                                                if (exif_orientation_s.equals("8")) {
                                                    exif_orientation2 = 270;
                                                }
                                            }
                                        }
                                    }
                                }
                                thumbnail_rotation = (0 + exif_orientation2) % 360;
                                if (thumbnail_rotation != 0) {
                                    m = new Matrix();
                                    m.setRotate((float) thumbnail_rotation, ((float) Preview.this.thumbnail.getWidth()) * 0.5f, ((float) Preview.this.thumbnail.getHeight()) * 0.5f);
                                    rotated_thumbnail = Bitmap.createBitmap(Preview.this.thumbnail, 0, 0, Preview.this.thumbnail.getWidth(), Preview.this.thumbnail.getHeight(), m, true);
                                    if (rotated_thumbnail != Preview.this.thumbnail) {
                                        Preview.this.thumbnail.recycle();
                                        Preview.this.thumbnail = rotated_thumbnail;
                                    }
                                }
                                if (sharedPreferences.getBoolean(MainActivity.getThumbnailAnimationPreferenceKey(), true)) {
                                    Preview.this.thumbnail_anim = true;
                                    Preview.this.thumbnail_anim_start_ms = System.currentTimeMillis();
                                }
                                main_activity.updateGalleryIconToBitmap(Preview.this.thumbnail);
                                if (old_thumbnail != null) {
                                    old_thumbnail.recycle();
                                }
                                if (bitmap != null) {
                                    bitmap.recycle();
                                }
                                System.gc();
                                if (Preview.this.remaining_burst_photos != -1) {
                                }
                                if (Preview.this.remaining_burst_photos > 0) {
                                    preview = Preview.this;
                                    preview.remaining_burst_photos = preview.remaining_burst_photos - 1;
                                }
                                timer_delay = (long) (Integer.parseInt(sharedPreferences.getString(MainActivity.getBurstIntervalPreferenceKey(), "0")) * 1000);
                                if (timer_delay == 0) {
                                    Preview.this.phase = 2;
                                    Preview.this.showGUI(false);
                                    Preview.this.takePictureWhenFocused();
                                    return;
                                }
                                Preview.this.takePictureOnTimer(timer_delay, true);
                            }
                        }
                        outputStream.write(data);
                        outputStream.close();
                        String szFileName = null;
                        if (picFile != null) {
                            szFileName = picFile.getAbsolutePath();
                        } else if (image_capture_intent && image_capture_intent_uri != null) {
                            szFileName = new File(image_capture_intent_uri.getPath()).getAbsolutePath();
                        }
                        if (szFileName != null) {
                            tempFile = File.createTempFile("opencamera_exif", "");
                            fileOutputStream = new FileOutputStream(tempFile);
                            fileOutputStream.write(data);
                            fileOutputStream.close();
                            int orientation = new ExifInterface(tempFile.getAbsolutePath()).getAttributeInt("Orientation", 1);
                            matrix = new Matrix();
                            boolean bResaveNeeded = false;
                            tempFile.delete();
                            if (orientation == 6) {
                                matrix.postRotate(90.0f);
                                bResaveNeeded = true;
                            } else if (orientation == 3) {
                                matrix.postRotate(180.0f);
                                bResaveNeeded = true;
                            } else if (orientation == 8) {
                                matrix.postRotate(270.0f);
                                bResaveNeeded = true;
                            }
                            if (bResaveNeeded) {
                                options = new Options();
                                options.inMutable = true;
                                if (VERSION.SDK_INT <= 19) {
                                    options.inPurgeable = true;
                                }
                                Bitmap rotateBitmap = BitmapFactory.decodeFile(szFileName, options);
                                if (rotateBitmap == null) {
                                    Preview.this.showToast(null, R.string.failed_to_stamp);
                                    System.gc();
                                    outputStream2 = outputStream;
                                } else {
                                    new_bitmap = Bitmap.createBitmap(rotateBitmap, 0, 0, rotateBitmap.getWidth(), rotateBitmap.getHeight(), matrix, true);
                                    if (new_bitmap != rotateBitmap) {
                                        fileOutputStream = new FileOutputStream(szFileName);
                                        Bitmap bitmap2 = new_bitmap;
                                        bitmap2.compress(CompressFormat.JPEG, Preview.this.getImageQuality(), fileOutputStream);
                                        fileOutputStream.close();
                                        new_bitmap.recycle();
                                    }
                                    rotateBitmap.recycle();
                                }
                                success = true;
                                if (picFile != null) {
                                    if (bitmap == null) {
                                        tempFile = File.createTempFile("opencamera_exif", "");
                                        fileOutputStream = new FileOutputStream(tempFile);
                                        fileOutputStream.write(data);
                                        fileOutputStream.close();
                                        exifInterface = new ExifInterface(tempFile.getAbsolutePath());
                                        exif_aperture = exifInterface.getAttribute("FNumber");
                                        exif_datetime = exifInterface.getAttribute("DateTime");
                                        exif_exposure_time = exifInterface.getAttribute("ExposureTime");
                                        exif_flash = exifInterface.getAttribute("Flash");
                                        exif_focal_length = exifInterface.getAttribute("FocalLength");
                                        exif_gps_altitude = exifInterface.getAttribute("GPSAltitude");
                                        exif_gps_altitude_ref = exifInterface.getAttribute("GPSAltitudeRef");
                                        exif_gps_datestamp = exifInterface.getAttribute("GPSDateStamp");
                                        exif_gps_latitude = exifInterface.getAttribute("GPSLatitude");
                                        exif_gps_latitude_ref = exifInterface.getAttribute("GPSLatitudeRef");
                                        exif_gps_longitude = exifInterface.getAttribute("GPSLongitude");
                                        exif_gps_longitude_ref = exifInterface.getAttribute("GPSLongitudeRef");
                                        exif_gps_processing_method = exifInterface.getAttribute("GPSProcessingMethod");
                                        exif_gps_timestamp = exifInterface.getAttribute("GPSTimeStamp");
                                        exif_iso = exifInterface.getAttribute("ISOSpeedRatings");
                                        exif_make = exifInterface.getAttribute("Make");
                                        exif_model = exifInterface.getAttribute("Model");
                                        exif_orientation = exifInterface.getAttribute("Orientation");
                                        exif_orientation_s = exif_orientation;
                                        exif_white_balance = exifInterface.getAttribute("WhiteBalance");
                                        if (tempFile.delete()) {
                                            exifInterface = new ExifInterface(picFile.getAbsolutePath());
                                        } else {
                                            exifInterface = new ExifInterface(picFile.getAbsolutePath());
                                        }
                                        if (exif_aperture != null) {
                                            exifInterface.setAttribute("FNumber", exif_aperture);
                                        }
                                        if (exif_datetime != null) {
                                            exifInterface.setAttribute("DateTime", exif_datetime);
                                        }
                                        if (exif_exposure_time != null) {
                                            exifInterface.setAttribute("ExposureTime", exif_exposure_time);
                                        }
                                        if (exif_flash != null) {
                                            exifInterface.setAttribute("Flash", exif_flash);
                                        }
                                        if (exif_focal_length != null) {
                                            exifInterface.setAttribute("FocalLength", exif_focal_length);
                                        }
                                        if (exif_gps_altitude != null) {
                                            exifInterface.setAttribute("GPSAltitude", exif_gps_altitude);
                                        }
                                        if (exif_gps_altitude_ref != null) {
                                            exifInterface.setAttribute("GPSAltitudeRef", exif_gps_altitude_ref);
                                        }
                                        if (exif_gps_datestamp != null) {
                                            exifInterface.setAttribute("GPSDateStamp", exif_gps_datestamp);
                                        }
                                        if (exif_gps_latitude != null) {
                                            exifInterface.setAttribute("GPSLatitude", exif_gps_latitude);
                                        }
                                        if (exif_gps_latitude_ref != null) {
                                            exifInterface.setAttribute("GPSLatitudeRef", exif_gps_latitude_ref);
                                        }
                                        if (exif_gps_longitude != null) {
                                            exifInterface.setAttribute("GPSLongitude", exif_gps_longitude);
                                        }
                                        if (exif_gps_longitude_ref != null) {
                                            exifInterface.setAttribute("GPSLongitudeRef", exif_gps_longitude_ref);
                                        }
                                        if (exif_gps_processing_method != null) {
                                            exifInterface.setAttribute("GPSProcessingMethod", exif_gps_processing_method);
                                        }
                                        if (exif_gps_timestamp != null) {
                                            exifInterface.setAttribute("GPSTimeStamp", exif_gps_timestamp);
                                        }
                                        if (exif_iso != null) {
                                            exifInterface.setAttribute("ISOSpeedRatings", exif_iso);
                                        }
                                        if (exif_make != null) {
                                            exifInterface.setAttribute("Make", exif_make);
                                        }
                                        if (exif_model != null) {
                                            exifInterface.setAttribute("Model", exif_model);
                                        }
                                        if (exif_orientation != null) {
                                            exifInterface.setAttribute("Orientation", exif_orientation);
                                        }
                                        if (exif_white_balance != null) {
                                            exifInterface.setAttribute("WhiteBalance", exif_white_balance);
                                        }
                                        Preview.this.setGPSDirectionExif(exifInterface);
                                        Preview.this.setDateTimeExif(exifInterface);
                                        exifInterface.saveAttributes();
                                    } else if (Preview.this.has_geo_direction) {
                                        if (sharedPreferences.getBoolean(MainActivity.getGPSDirectionPreferenceKey(), false)) {
                                            long currentTimeMillis22 = System.currentTimeMillis();
                                            exifInterface = new ExifInterface(picFile.getAbsolutePath());
                                            Preview.this.setGPSDirectionExif(exifInterface);
                                            Preview.this.setDateTimeExif(exifInterface);
                                            exifInterface.saveAttributes();
                                        }
                                    }
                                    if (image_capture_intent) {
                                        main_activity.broadcastFile(picFile, true, false);
                                        Preview.this.test_last_saved_image = str;
                                    }
                                }
                                if (image_capture_intent) {
                                    main_activity.setResult(-1);
                                    main_activity.finish();
                                }
                            }
                        }
                        success = true;
                        if (picFile != null) {
                            if (bitmap == null) {
                                tempFile = File.createTempFile("opencamera_exif", "");
                                fileOutputStream = new FileOutputStream(tempFile);
                                fileOutputStream.write(data);
                                fileOutputStream.close();
                                exifInterface = new ExifInterface(tempFile.getAbsolutePath());
                                exif_aperture = exifInterface.getAttribute("FNumber");
                                exif_datetime = exifInterface.getAttribute("DateTime");
                                exif_exposure_time = exifInterface.getAttribute("ExposureTime");
                                exif_flash = exifInterface.getAttribute("Flash");
                                exif_focal_length = exifInterface.getAttribute("FocalLength");
                                exif_gps_altitude = exifInterface.getAttribute("GPSAltitude");
                                exif_gps_altitude_ref = exifInterface.getAttribute("GPSAltitudeRef");
                                exif_gps_datestamp = exifInterface.getAttribute("GPSDateStamp");
                                exif_gps_latitude = exifInterface.getAttribute("GPSLatitude");
                                exif_gps_latitude_ref = exifInterface.getAttribute("GPSLatitudeRef");
                                exif_gps_longitude = exifInterface.getAttribute("GPSLongitude");
                                exif_gps_longitude_ref = exifInterface.getAttribute("GPSLongitudeRef");
                                exif_gps_processing_method = exifInterface.getAttribute("GPSProcessingMethod");
                                exif_gps_timestamp = exifInterface.getAttribute("GPSTimeStamp");
                                exif_iso = exifInterface.getAttribute("ISOSpeedRatings");
                                exif_make = exifInterface.getAttribute("Make");
                                exif_model = exifInterface.getAttribute("Model");
                                exif_orientation = exifInterface.getAttribute("Orientation");
                                exif_orientation_s = exif_orientation;
                                exif_white_balance = exifInterface.getAttribute("WhiteBalance");
                                if (tempFile.delete()) {
                                    exifInterface = new ExifInterface(picFile.getAbsolutePath());
                                } else {
                                    exifInterface = new ExifInterface(picFile.getAbsolutePath());
                                }
                                if (exif_aperture != null) {
                                    exifInterface.setAttribute("FNumber", exif_aperture);
                                }
                                if (exif_datetime != null) {
                                    exifInterface.setAttribute("DateTime", exif_datetime);
                                }
                                if (exif_exposure_time != null) {
                                    exifInterface.setAttribute("ExposureTime", exif_exposure_time);
                                }
                                if (exif_flash != null) {
                                    exifInterface.setAttribute("Flash", exif_flash);
                                }
                                if (exif_focal_length != null) {
                                    exifInterface.setAttribute("FocalLength", exif_focal_length);
                                }
                                if (exif_gps_altitude != null) {
                                    exifInterface.setAttribute("GPSAltitude", exif_gps_altitude);
                                }
                                if (exif_gps_altitude_ref != null) {
                                    exifInterface.setAttribute("GPSAltitudeRef", exif_gps_altitude_ref);
                                }
                                if (exif_gps_datestamp != null) {
                                    exifInterface.setAttribute("GPSDateStamp", exif_gps_datestamp);
                                }
                                if (exif_gps_latitude != null) {
                                    exifInterface.setAttribute("GPSLatitude", exif_gps_latitude);
                                }
                                if (exif_gps_latitude_ref != null) {
                                    exifInterface.setAttribute("GPSLatitudeRef", exif_gps_latitude_ref);
                                }
                                if (exif_gps_longitude != null) {
                                    exifInterface.setAttribute("GPSLongitude", exif_gps_longitude);
                                }
                                if (exif_gps_longitude_ref != null) {
                                    exifInterface.setAttribute("GPSLongitudeRef", exif_gps_longitude_ref);
                                }
                                if (exif_gps_processing_method != null) {
                                    exifInterface.setAttribute("GPSProcessingMethod", exif_gps_processing_method);
                                }
                                if (exif_gps_timestamp != null) {
                                    exifInterface.setAttribute("GPSTimeStamp", exif_gps_timestamp);
                                }
                                if (exif_iso != null) {
                                    exifInterface.setAttribute("ISOSpeedRatings", exif_iso);
                                }
                                if (exif_make != null) {
                                    exifInterface.setAttribute("Make", exif_make);
                                }
                                if (exif_model != null) {
                                    exifInterface.setAttribute("Model", exif_model);
                                }
                                if (exif_orientation != null) {
                                    exifInterface.setAttribute("Orientation", exif_orientation);
                                }
                                if (exif_white_balance != null) {
                                    exifInterface.setAttribute("WhiteBalance", exif_white_balance);
                                }
                                Preview.this.setGPSDirectionExif(exifInterface);
                                Preview.this.setDateTimeExif(exifInterface);
                                exifInterface.saveAttributes();
                            } else if (Preview.this.has_geo_direction) {
                                if (sharedPreferences.getBoolean(MainActivity.getGPSDirectionPreferenceKey(), false)) {
                                    long currentTimeMillis222 = System.currentTimeMillis();
                                    exifInterface = new ExifInterface(picFile.getAbsolutePath());
                                    Preview.this.setGPSDirectionExif(exifInterface);
                                    Preview.this.setDateTimeExif(exifInterface);
                                    exifInterface.saveAttributes();
                                }
                            }
                            if (image_capture_intent) {
                                main_activity.broadcastFile(picFile, true, false);
                                Preview.this.test_last_saved_image = str;
                            }
                        }
                        if (image_capture_intent) {
                            main_activity.setResult(-1);
                            main_activity.finish();
                        }
                    }
                    if (Preview.this.using_android_l) {
                        Preview.this.is_preview_started = false;
                    }
                    Preview.this.phase = 0;
                    if (Preview.this.remaining_burst_photos == -1 && Preview.this.remaining_burst_photos <= 0) {
                        Preview.this.phase = 0;
                        if (sharedPreferences.getBoolean(MainActivity.getPausePreviewPreferenceKey(), false) && success) {
                            if (Preview.this.is_preview_started) {
                                Preview.this.camera_controller.stopPreview();
                                Preview.this.is_preview_started = false;
                            }
                            Preview.this.setPreviewPaused(true);
                            Preview.this.preview_image_name = str;
                        } else {
                            if (!Preview.this.is_preview_started) {
                                Preview.this.startCameraPreview();
                            }
                            Preview.this.showGUI(true);
                        }
                    } else if (Preview.this.is_preview_started) {
                        Preview.this.startCameraPreview();
                    }
                    if (!(!success || picFile == null || Preview.this.camera_controller == null)) {
                        currentTimeMillis222 = System.currentTimeMillis();
                        sample_size = Integer.highestOneBit((int) Math.ceil(((double) Preview.this.camera_controller.getPictureSize().width) / ((double) Preview.this.cameraSurface.getView().getWidth()))) * 4;
                        if (sharedPreferences.getBoolean(MainActivity.getThumbnailAnimationPreferenceKey(), true)) {
                            sample_size *= 4;
                        }
                        old_thumbnail = Preview.this.thumbnail;
                        if (bitmap == null) {
                            options = new Options();
                            options.inMutable = false;
                            if (VERSION.SDK_INT <= 19) {
                                options.inPurgeable = true;
                            }
                            options.inSampleSize = sample_size;
                            Preview.this.thumbnail = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                        } else {
                            width = bitmap.getWidth();
                            height = bitmap.getHeight();
                            matrix = new Matrix();
                            scale = 1.0f / ((float) sample_size);
                            matrix.postScale(scale, scale);
                            Preview.this.thumbnail = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                        }
                        thumbnail_rotation = 0;
                        if (exif_orientation_s == null) {
                            exif_orientation_s = new ExifInterface(picFile.getAbsolutePath()).getAttribute("Orientation");
                        }
                        exif_orientation2 = 0;
                        if (exif_orientation_s.equals("0")) {
                            if (exif_orientation_s.equals("1")) {
                                if (exif_orientation_s.equals("3")) {
                                    exif_orientation2 = 180;
                                } else {
                                    if (exif_orientation_s.equals("6")) {
                                        exif_orientation2 = 90;
                                    } else {
                                        if (exif_orientation_s.equals("8")) {
                                            exif_orientation2 = 270;
                                        }
                                    }
                                }
                            }
                        }
                        thumbnail_rotation = (0 + exif_orientation2) % 360;
                        if (thumbnail_rotation != 0) {
                            m = new Matrix();
                            m.setRotate((float) thumbnail_rotation, ((float) Preview.this.thumbnail.getWidth()) * 0.5f, ((float) Preview.this.thumbnail.getHeight()) * 0.5f);
                            rotated_thumbnail = Bitmap.createBitmap(Preview.this.thumbnail, 0, 0, Preview.this.thumbnail.getWidth(), Preview.this.thumbnail.getHeight(), m, true);
                            if (rotated_thumbnail != Preview.this.thumbnail) {
                                Preview.this.thumbnail.recycle();
                                Preview.this.thumbnail = rotated_thumbnail;
                            }
                        }
                        if (sharedPreferences.getBoolean(MainActivity.getThumbnailAnimationPreferenceKey(), true)) {
                            Preview.this.thumbnail_anim = true;
                            Preview.this.thumbnail_anim_start_ms = System.currentTimeMillis();
                        }
                        main_activity.updateGalleryIconToBitmap(Preview.this.thumbnail);
                        if (old_thumbnail != null) {
                            old_thumbnail.recycle();
                        }
                    }
                    if (bitmap != null) {
                        bitmap.recycle();
                    }
                    System.gc();
                    if (Preview.this.remaining_burst_photos != -1 || Preview.this.remaining_burst_photos > 0) {
                        if (Preview.this.remaining_burst_photos > 0) {
                            preview = Preview.this;
                            preview.remaining_burst_photos = preview.remaining_burst_photos - 1;
                        }
                        timer_delay = (long) (Integer.parseInt(sharedPreferences.getString(MainActivity.getBurstIntervalPreferenceKey(), "0")) * 1000);
                        if (timer_delay == 0) {
                            Preview.this.phase = 2;
                            Preview.this.showGUI(false);
                            Preview.this.takePictureWhenFocused();
                            return;
                        }
                        Preview.this.takePictureOnTimer(timer_delay, true);
                    }
                }
            };
            this.camera_controller.setRotation(getImageVideoRotation());
            this.camera_controller.enableShutterSound(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(MainActivity.getShutterSoundPreferenceKey(), true));
            try {
                this.camera_controller.takePicture(null, jpegPictureCallback);
                this.count_cameraTakePicture++;
            } catch (RuntimeException e) {
                e.printStackTrace();
                showToast(null, R.string.failed_to_take_picture);
                this.phase = 0;
                startCameraPreview();
                showGUI(true);
            }
        } else {
            this.phase = 0;
            showGUI(true);
        }
    }

    private void setGPSDirectionExif(ExifInterface exif) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (this.has_geo_direction && sharedPreferences.getBoolean(MainActivity.getGPSDirectionPreferenceKey(), false)) {
            float geo_angle = (float) Math.toDegrees((double) this.geo_direction[0]);
            if (geo_angle < 0.0f) {
                geo_angle += 360.0f;
            }
            exif.setAttribute(TAG_GPS_IMG_DIRECTION, new StringBuilder(String.valueOf(Math.round(100.0f * geo_angle))).append("/100").toString());
            exif.setAttribute(TAG_GPS_IMG_DIRECTION_REF, "M");
        }
    }

    private void setDateTimeExif(ExifInterface exif) {
        String exif_datetime = exif.getAttribute("DateTime");
        if (exif_datetime != null) {
            exif.setAttribute("DateTimeOriginal", exif_datetime);
            exif.setAttribute("DateTimeDigitized", exif_datetime);
        }
    }

    void clickedShare() {
        if (this.phase == 3) {
            if (this.preview_image_name != null) {
                Intent intent = new Intent("android.intent.action.SEND");
                intent.setType("image/jpeg");
                intent.putExtra("android.intent.extra.STREAM", Uri.parse("file://" + this.preview_image_name));
                ((Activity) getContext()).startActivity(Intent.createChooser(intent, "Photo"));
            }
            startCameraPreview();
            tryAutoFocus(false, false);
        }
    }

    void clickedTrash() {
        if (this.phase == 3) {
            if (this.preview_image_name != null) {
                File file = new File(this.preview_image_name);
                if (file.delete()) {
                    showToast(null, R.string.photo_deleted);
                    ((MainActivity) getContext()).broadcastFile(file, false, false);
                }
            }
            startCameraPreview();
            tryAutoFocus(false, false);
        }
    }

    void requestAutoFocus() {
        cancelAutoFocus();
        tryAutoFocus(false, true);
    }

    private void tryAutoFocus(boolean startup, final boolean manual) {
        if (this.camera_controller == null || !this.has_surface || !this.is_preview_started) {
            return;
        }
        if ((manual && this.is_video) || !isTakingPhotoOrOnTimer()) {
            if (this.camera_controller.supportsAutoFocus()) {
                this.set_flash_value_after_autofocus = "";
                String old_flash_value = this.camera_controller.getFlashValue();
                if (startup && old_flash_value.length() > 0 && !old_flash_value.equals("flash_off")) {
                    this.set_flash_value_after_autofocus = old_flash_value;
                    this.camera_controller.setFlashValue("flash_off");
                }
                AutoFocusCallback autoFocusCallback = new AutoFocusCallback() {
                    public void onAutoFocus(boolean success) {
                        Preview.this.autoFocusCompleted(manual, success, false);
                    }
                };
                this.focus_success = 0;
                this.focus_complete_time = -1;
                this.successfully_focused = false;
                try {
                    this.camera_controller.autoFocus(autoFocusCallback);
                    this.count_cameraAutoFocus++;
                } catch (RuntimeException e) {
                    autoFocusCallback.onAutoFocus(false);
                    e.printStackTrace();
                }
            } else if (this.has_focus_area) {
                this.focus_success = 1;
                this.focus_complete_time = System.currentTimeMillis();
            }
        }
    }

    private void cancelAutoFocus() {
        if (this.camera_controller != null) {
            try {
                this.camera_controller.cancelAutoFocus();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            autoFocusCompleted(false, false, true);
        }
    }

    private void autoFocusCompleted(boolean manual, boolean success, boolean cancelled) {
        if (cancelled) {
            this.focus_success = 3;
        } else {
            this.focus_success = success ? 1 : 2;
            this.focus_complete_time = System.currentTimeMillis();
        }
        MainActivity main_activity = (MainActivity) getContext();
        if (manual && !cancelled && (success || main_activity.is_test)) {
            this.successfully_focused = true;
            this.successfully_focused_time = this.focus_complete_time;
        }
        if (this.set_flash_value_after_autofocus.length() > 0 && this.camera_controller != null) {
            this.camera_controller.setFlashValue(this.set_flash_value_after_autofocus);
            this.set_flash_value_after_autofocus = "";
        }
        if (this.using_face_detection && !cancelled && this.camera_controller != null) {
            try {
                this.camera_controller.cancelAutoFocus();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    private void startCameraPreview() {
        if (!(this.camera_controller == null || isTakingPhotoOrOnTimer() || this.is_preview_started)) {
            this.camera_controller.setRecordingHint(this.is_video);
            setPreviewFps();
            try {
                this.camera_controller.startPreview();
                this.count_cameraStartPreview++;
                this.is_preview_started = true;
                if (this.using_face_detection) {
                    try {
                        this.camera_controller.startFaceDetection();
                    } catch (RuntimeException e) {
                    }
                    this.faces_detected = null;
                }
            } catch (RuntimeException e2) {
                e2.printStackTrace();
                showToast(null, R.string.failed_to_start_camera_preview);
                return;
            }
        }
        setPreviewPaused(false);
    }

    private void setPreviewPaused(boolean paused) {
        MainActivity main_activity = (MainActivity) getContext();
        View shareButton = main_activity.findViewById(R.id.share);
        View trashButton = main_activity.findViewById(R.id.trash);
        if (paused) {
            this.phase = 3;
            shareButton.setVisibility(0);
            trashButton.setVisibility(0);
            return;
        }
        this.phase = 0;
        shareButton.setVisibility(8);
        trashButton.setVisibility(8);
        this.preview_image_name = null;
        showGUI(true);
    }

    void setImmersiveMode(final boolean immersive_mode) {
        this.immersive_mode = immersive_mode;
        final MainActivity main_activity = (MainActivity) getContext();
        main_activity.runOnUiThread(new Runnable() {
            public void run() {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
                int visibility = immersive_mode ? 8 : 0;
                View switchCameraButton = main_activity.findViewById(R.id.switch_camera);
                View switchVideoButton = main_activity.findViewById(R.id.switch_video);
                View exposureButton = main_activity.findViewById(R.id.exposure);
                View exposureLockButton = main_activity.findViewById(R.id.exposure_lock);
                View popupButton = main_activity.findViewById(R.id.popup);
                View galleryButton = main_activity.findViewById(R.id.gallery);
                View settingsButton = main_activity.findViewById(R.id.settings);
                View zoomControls = main_activity.findViewById(R.id.zoom);
                View zoomSeekBar = main_activity.findViewById(R.id.zoom_seekbar);
                if (Preview.this.camera_controller_manager.getNumberOfCameras() > 1) {
                    switchCameraButton.setVisibility(visibility);
                }
                switchVideoButton.setVisibility(visibility);
                if (Preview.this.exposures != null) {
                    exposureButton.setVisibility(visibility);
                }
                if (Preview.this.is_exposure_lock_supported) {
                    exposureLockButton.setVisibility(visibility);
                }
                popupButton.setVisibility(visibility);
                galleryButton.setVisibility(visibility);
                settingsButton.setVisibility(visibility);
                if (Preview.this.has_zoom && sharedPreferences.getBoolean(MainActivity.getShowZoomControlsPreferenceKey(), false)) {
                    zoomControls.setVisibility(visibility);
                }
                if (Preview.this.has_zoom && sharedPreferences.getBoolean(MainActivity.getShowZoomSliderControlsPreferenceKey(), true)) {
                    zoomSeekBar.setVisibility(visibility);
                }
                if (sharedPreferences.getString(MainActivity.getImmersiveModePreferenceKey(), "immersive_mode_low_profile").equals("immersive_mode_everything")) {
                    main_activity.findViewById(R.id.take_photo).setVisibility(visibility);
                }
                if (!immersive_mode) {
                    Preview.this.showGUI(Preview.this.show_gui);
                }
            }
        });
    }

    private void showGUI(final boolean show) {
        this.show_gui = show;
        if (!this.immersive_mode) {
            final MainActivity main_activity = (MainActivity) getContext();
            if (show && main_activity.usingKitKatImmersiveMode()) {
                main_activity.initImmersiveMode();
            }
            main_activity.runOnUiThread(new Runnable() {
                public void run() {
                    int visibility = show ? 0 : 8;
                    View switchCameraButton = main_activity.findViewById(R.id.switch_camera);
                    View switchVideoButton = main_activity.findViewById(R.id.switch_video);
                    View exposureButton = main_activity.findViewById(R.id.exposure);
                    View exposureLockButton = main_activity.findViewById(R.id.exposure_lock);
                    View popupButton = main_activity.findViewById(R.id.popup);
                    if (Preview.this.camera_controller_manager.getNumberOfCameras() > 1) {
                        switchCameraButton.setVisibility(visibility);
                    }
                    if (!Preview.this.is_video) {
                        switchVideoButton.setVisibility(visibility);
                    }
                    if (!(Preview.this.exposures == null || Preview.this.is_video)) {
                        exposureButton.setVisibility(visibility);
                    }
                    if (Preview.this.is_exposure_lock_supported && !Preview.this.is_video) {
                        exposureLockButton.setVisibility(visibility);
                    }
                    if (!show) {
                        main_activity.closePopup();
                    }
                    if (!Preview.this.is_video || Preview.this.supported_flash_values == null) {
                        popupButton.setVisibility(visibility);
                    }
                }
            });
        }
    }

    private void setPopupIcon() {
        ImageButton popup = (ImageButton) ((MainActivity) getContext()).findViewById(R.id.popup);
        String flash_value = getCurrentFlashValue();
        if (flash_value != null && flash_value.equals("flash_torch")) {
            popup.setImageResource(R.drawable.popup_flash_torch);
        } else if (flash_value != null && flash_value.equals("flash_auto")) {
            popup.setImageResource(R.drawable.popup_flash_auto);
        } else if (flash_value == null || !flash_value.equals("flash_on")) {
            popup.setImageResource(R.drawable.popup);
        } else {
            popup.setImageResource(R.drawable.popup_flash_on);
        }
    }

    void onAccelerometerSensorChanged(SensorEvent event) {
        this.has_gravity = true;
        for (int i = 0; i < 3; i++) {
            this.gravity[i] = (0.8f * this.gravity[i]) + (0.19999999f * event.values[i]);
        }
        calculateGeoDirection();
        double x = (double) this.gravity[0];
        double y = (double) this.gravity[1];
        this.has_level_angle = true;
        this.level_angle = (Math.atan2(-x, y) * 180.0d) / 3.141592653589793d;
        if (this.level_angle < -0.0d) {
            this.level_angle += 360.0d;
        }
        this.orig_level_angle = this.level_angle;
        this.level_angle -= (double) ((float) this.current_orientation);
        if (this.level_angle < -180.0d) {
            this.level_angle += 360.0d;
        } else if (this.level_angle > 180.0d) {
            this.level_angle -= 360.0d;
        }
        this.cameraSurface.getView().invalidate();
    }

    void onMagneticSensorChanged(SensorEvent event) {
        this.has_geomagnetic = true;
        for (int i = 0; i < 3; i++) {
            this.geomagnetic[i] = (0.8f * this.geomagnetic[i]) + (0.19999999f * event.values[i]);
        }
        calculateGeoDirection();
    }

    private void calculateGeoDirection() {
        if (this.has_gravity && this.has_geomagnetic && SensorManager.getRotationMatrix(this.deviceRotation, this.deviceInclination, this.gravity, this.geomagnetic)) {
            SensorManager.remapCoordinateSystem(this.deviceRotation, 1, 3, this.cameraRotation);
            this.has_geo_direction = true;
            SensorManager.getOrientation(this.cameraRotation, this.geo_direction);
        }
    }

    public boolean supportsFaceDetection() {
        return this.supports_face_detection;
    }

    public boolean supportsVideoStabilization() {
        return this.supports_video_stabilization;
    }

    boolean canDisableShutterSound() {
        return this.can_disable_shutter_sound;
    }

    public List<String> getSupportedColorEffects() {
        return this.color_effects;
    }

    public List<String> getSupportedSceneModes() {
        return this.scene_modes;
    }

    public List<String> getSupportedWhiteBalances() {
        return this.white_balances;
    }

    String getISOKey() {
        return this.camera_controller == null ? "" : this.camera_controller.getISOKey();
    }

    public List<String> getSupportedISOs() {
        return this.isos;
    }

    public boolean supportsExposures() {
        return this.exposures != null;
    }

    public int getMinimumExposure() {
        return this.min_exposure;
    }

    public int getMaximumExposure() {
        return this.max_exposure;
    }

    public int getCurrentExposure() {
        if (this.camera_controller == null) {
            return 0;
        }
        return this.camera_controller.getExposureCompensation();
    }

    List<String> getSupportedExposures() {
        return this.exposures;
    }

    public List<Size> getSupportedPreviewSizes() {
        return this.supported_preview_sizes;
    }

    public List<Size> getSupportedPictureSizes() {
        return this.sizes;
    }

    int getCurrentPictureSizeIndex() {
        return this.current_size_index;
    }

    public List<String> getSupportedVideoQuality() {
        return this.video_quality;
    }

    int getCurrentVideoQualityIndex() {
        return this.current_video_quality;
    }

    List<Size> getSupportedVideoSizes() {
        return this.video_sizes;
    }

    public List<String> getSupportedFlashValues() {
        return this.supported_flash_values;
    }

    public List<String> getSupportedFocusValues() {
        return this.supported_focus_values;
    }

    public int getCameraId() {
        return this.cameraId;
    }

    String getCameraAPI() {
        if (this.camera_controller == null) {
            return Markup.LINEENDINGSTYLE_NONE;
        }
        return this.camera_controller.getAPI();
    }

    private int getImageQuality() {
        try {
            return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(MainActivity.getQualityPreferenceKey(), "90"));
        } catch (NumberFormatException e) {
            return 90;
        }
    }

    void onResume() {
        onResume(null);
    }

    void onResume(String toast_message) {
        this.app_is_paused = false;
        openCamera(toast_message);
    }

    void onPause() {
        this.app_is_paused = true;
        closeCamera();
    }

    void updateUIPlacement() {
        this.ui_placement_right = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(MainActivity.getUIPlacementPreferenceKey(), "ui_right").equals("ui_right");
    }

    void onSaveInstanceState(Bundle state) {
        state.putInt("cameraId", this.cameraId);
        state.putInt("zoom_factor", this.zoom_factor);
    }

    public void showToast(ToastBoxer clear_toast, int message_id) {
        showToast(clear_toast, getResources().getString(message_id));
    }

    public void showToast(ToastBoxer clear_toast, String message) {
        showToast(clear_toast, message, 0);
    }

    public void showToast(ToastBoxer clear_toast, String message, int duration) {
        final Activity activity = (Activity) getContext();
        final ToastBoxer toastBoxer = clear_toast;
        final String str = message;
        final int i = duration;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                if (!(toastBoxer == null || toastBoxer.toast == null)) {
                    toastBoxer.toast.cancel();
                }
                Toast toast = new Toast(activity);
                if (toastBoxer != null) {
                    toastBoxer.toast = toast;
                }
                toast.setView(new AnonymousClass1RotatedTextView(str, activity));
                toast.setDuration(i);
                toast.show();
            }
        });
    }

    void setUIRotation(int ui_rotation) {
        this.ui_rotation = ui_rotation;
    }

    private void updateParametersFromLocation() {
        if (this.camera_controller != null) {
            MainActivity main_activity = (MainActivity) getContext();
            if (!PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(MainActivity.getLocationPreferenceKey(), false) || main_activity.getLocation() == null) {
                this.camera_controller.removeLocationInfo();
                return;
            }
            this.camera_controller.setLocationInfo(main_activity.getLocation());
        }
    }

    public boolean isVideo() {
        return this.is_video;
    }

    public boolean isTakingPhoto() {
        return this.phase == 2;
    }

    public CameraController getCameraController() {
        return this.camera_controller;
    }

    public CameraControllerManager getCameraControllerManager() {
        return this.camera_controller_manager;
    }

    public boolean supportsFocus() {
        return this.supported_focus_values != null;
    }

    public boolean supportsFlash() {
        return this.supported_flash_values != null;
    }

    public boolean supportsExposureLock() {
        return this.is_exposure_lock_supported;
    }

    public boolean supportsZoom() {
        return this.has_zoom;
    }

    public int getMaxZoom() {
        return this.max_zoom_factor;
    }

    public boolean hasFocusArea() {
        return this.has_focus_area;
    }

    public int getMaxNumFocusAreas() {
        return this.max_num_focus_areas;
    }

    public boolean isTakingPhotoOrOnTimer() {
        return this.phase == 2 || this.phase == 1;
    }

    public boolean isOnTimer() {
        return this.phase == 1;
    }

    public boolean isPreviewStarted() {
        return this.is_preview_started;
    }

    public boolean isFocusWaiting() {
        return this.focus_success == 0;
    }
}
