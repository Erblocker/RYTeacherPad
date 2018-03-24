package net.sourceforge.opencamera;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.net.http.Headers;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video.Thumbnails;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ZoomControls;
import com.foxit.uiextensions.utils.AppSQLite;
import io.vov.vitamio.provider.MediaStore.Video.VideoColumns;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.sourceforge.opencamera.CameraController.Size;
import org.apache.http.HttpStatus;

public class MainActivity extends Activity {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final String TAG = "MainActivity";
    private SensorEventListener accelerometerListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            MainActivity.this.preview.onAccelerometerSensorChanged(event);
        }
    };
    private boolean camera_in_background = false;
    ToastBoxer changed_auto_stabilise_toast = new ToastBoxer();
    private int current_orientation = 0;
    public boolean failed_to_scan = false;
    public Bitmap gallery_bitmap = null;
    private GestureDetector gestureDetector;
    private Handler immersive_timer_handler = null;
    private Runnable immersive_timer_runnable = null;
    public boolean is_test = false;
    private Uri last_media_scanned = null;
    private MyLocationListener[] locationListeners = null;
    private LocationManager mLocationManager = null;
    private Sensor mSensorAccelerometer = null;
    private Sensor mSensorMagnetic = null;
    private SensorManager mSensorManager = null;
    private SensorEventListener magneticListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            MainActivity.this.preview.onMagneticSensorChanged(event);
        }
    };
    private OrientationEventListener orientationEventListener = null;
    private PopupView popup_view = null;
    private Map<Integer, Bitmap> preloaded_bitmap_resources = new Hashtable();
    private Preview preview = null;
    private ArrayList<String> save_location_history = new ArrayList();
    private boolean screen_is_locked = false;
    private ToastBoxer screen_locked_toast = new ToastBoxer();
    private boolean supports_auto_stabilise = false;
    private boolean supports_force_video_4k = false;

    class Media {
        public long date;
        public long id;
        public int orientation;
        public Uri uri;
        public boolean video;

        Media(long id, boolean video, Uri uri, long date, int orientation) {
            this.id = id;
            this.video = video;
            this.uri = uri;
            this.date = date;
            this.orientation = orientation;
        }
    }

    class MyGestureDetector extends SimpleOnGestureListener {
        MyGestureDetector() {
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                int swipeMinDistance = (int) ((160.0f * MainActivity.this.getResources().getDisplayMetrics().density) + 0.5f);
                int swipeThresholdVelocity = ViewConfiguration.get(MainActivity.this).getScaledMinimumFlingVelocity();
                float xdist = e1.getX() - e2.getX();
                float ydist = e1.getY() - e2.getY();
                float vel2 = (velocityX * velocityX) + (velocityY * velocityY);
                if ((xdist * xdist) + (ydist * ydist) > ((float) (swipeMinDistance * swipeMinDistance)) && vel2 > ((float) (swipeThresholdVelocity * swipeThresholdVelocity))) {
                    MainActivity.this.preview.showToast(MainActivity.this.screen_locked_toast, R.string.unlocked);
                    MainActivity.this.unlockScreen();
                }
            } catch (Exception e) {
            }
            return false;
        }

        public boolean onDown(MotionEvent e) {
            MainActivity.this.preview.showToast(MainActivity.this.screen_locked_toast, R.string.screen_is_locked);
            return true;
        }
    }

    private class MyLocationListener implements LocationListener {
        private Location location = null;
        public boolean test_has_received_location = false;

        private MyLocationListener() {
        }

        Location getLocation() {
            return this.location;
        }

        public void onLocationChanged(Location location) {
            this.test_has_received_location = true;
            if (location.getLatitude() != 0.0d || location.getLongitude() != 0.0d) {
                this.location = location;
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case 0:
                case 1:
                    this.location = null;
                    this.test_has_received_location = false;
                    return;
                default:
                    return;
            }
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
            this.location = null;
            this.test_has_received_location = false;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        long time_s = System.currentTimeMillis();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cameramain);
        PreferenceManager.setDefaultValues(this, R.xml.opencamera_preferences, false);
        if (!(getIntent() == null || getIntent().getExtras() == null)) {
            this.is_test = getIntent().getExtras().getBoolean("test_project");
        }
        if (!(getIntent() == null || getIntent().getExtras() == null)) {
            getIntent().getExtras().getBoolean(TakePhoto.TAKE_PHOTO);
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        ActivityManager activityManager = (ActivityManager) getSystemService("activity");
        if (activityManager.getLargeMemoryClass() >= 128) {
            this.supports_auto_stabilise = true;
        }
        if (activityManager.getMemoryClass() >= 128 || activityManager.getLargeMemoryClass() >= 512) {
            this.supports_force_video_4k = true;
        }
        setWindowFlagsForCamera();
        this.save_location_history.clear();
        int save_location_history_size = sharedPreferences.getInt("save_location_history_size", 0);
        for (int i = 0; i < save_location_history_size; i++) {
            String string = sharedPreferences.getString("save_location_history_" + i, null);
            if (string != null) {
                this.save_location_history.add(string);
            }
        }
        updateFolderHistory();
        this.mSensorManager = (SensorManager) getSystemService("sensor");
        if (this.mSensorManager.getDefaultSensor(1) != null) {
            this.mSensorAccelerometer = this.mSensorManager.getDefaultSensor(1);
        }
        if (this.mSensorManager.getDefaultSensor(2) != null) {
            this.mSensorMagnetic = this.mSensorManager.getDefaultSensor(2);
        }
        this.mLocationManager = (LocationManager) getSystemService(Headers.LOCATION);
        clearSeekBar();
        this.preview = new Preview(this, savedInstanceState);
        this.orientationEventListener = new OrientationEventListener(this) {
            public void onOrientationChanged(int orientation) {
                MainActivity.this.onOrientationChanged(orientation);
            }
        };
        findViewById(R.id.gallery).setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                MainActivity.this.longClickedGallery();
                return true;
            }
        });
        this.gestureDetector = new GestureDetector(this, new MyGestureDetector());
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
            public void onSystemUiVisibilityChange(int visibility) {
                if (!MainActivity.this.usingKitKatImmersiveMode()) {
                    return;
                }
                if ((visibility & 4) == 0) {
                    MainActivity.this.preview.setImmersiveMode(false);
                    MainActivity.this.setImmersiveTimer();
                    return;
                }
                MainActivity.this.preview.setImmersiveMode(true);
            }
        });
        preloadIcons(R.array.flash_icons);
        preloadIcons(R.array.focus_mode_icons);
        String action = getIntent().getAction();
        if ("android.media.action.VIDEO_CAPTURE".equalsIgnoreCase(action)) {
            Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putBoolean(getIsVideoPreferenceKey(), true);
            editor.apply();
        }
        if ("android.media.action.IMAGE_CAPTURE".equalsIgnoreCase(action)) {
            editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putBoolean(getIsVideoPreferenceKey(), false);
            editor.apply();
        }
    }

    private void preloadIcons(int icons_id) {
        long time_s = System.currentTimeMillis();
        String[] icons = getResources().getStringArray(icons_id);
        for (String identifier : icons) {
            int resource = getResources().getIdentifier(identifier, null, getApplicationContext().getPackageName());
            this.preloaded_bitmap_resources.put(Integer.valueOf(resource), BitmapFactory.decodeResource(getResources(), resource));
        }
    }

    protected void onDestroy() {
        for (Entry<Integer, Bitmap> entry : this.preloaded_bitmap_resources.entrySet()) {
            ((Bitmap) entry.getValue()).recycle();
        }
        this.preloaded_bitmap_resources.clear();
        super.onDestroy();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void setFirstTimeFlag() {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean(getFirstTimePreferenceKey(), true);
        editor.apply();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case 24:
            case 25:
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                String volume_keys = sharedPreferences.getString(getVolumeKeysPreferenceKey(), "volume_take_photo");
                if (volume_keys.equals("volume_take_photo")) {
                    takePicture();
                    return true;
                } else if (volume_keys.equals("volume_focus")) {
                    this.preview.requestAutoFocus();
                    return true;
                } else if (volume_keys.equals("volume_zoom")) {
                    if (keyCode == 24) {
                        this.preview.zoomIn();
                        return true;
                    }
                    this.preview.zoomOut();
                    return true;
                } else if (volume_keys.equals("volume_exposure")) {
                    if (keyCode == 24) {
                        this.preview.changeExposure(1, true);
                        return true;
                    }
                    this.preview.changeExposure(-1, true);
                    return true;
                } else if (volume_keys.equals("volume_auto_stabilise")) {
                    if (this.supports_auto_stabilise) {
                        boolean auto_stabilise;
                        if (sharedPreferences.getBoolean(getAutoStabilisePreferenceKey(), false)) {
                            auto_stabilise = false;
                        } else {
                            auto_stabilise = true;
                        }
                        Editor editor = sharedPreferences.edit();
                        editor.putBoolean(getAutoStabilisePreferenceKey(), auto_stabilise);
                        editor.apply();
                        this.preview.showToast(this.changed_auto_stabilise_toast, new StringBuilder(String.valueOf(getResources().getString(R.string.preference_auto_stabilise))).append(": ").append(getResources().getString(auto_stabilise ? R.string.on : R.string.off)).toString());
                        return true;
                    }
                    this.preview.showToast(this.changed_auto_stabilise_toast, R.string.auto_stabilise_not_supported);
                    return true;
                } else if (volume_keys.equals("volume_really_nothing")) {
                    return true;
                }
                break;
            case 27:
                if (event.getRepeatCount() == 0) {
                    takePicture();
                    return true;
                }
                break;
            case 80:
                break;
            case 82:
                openSettings();
                return true;
            case 168:
                this.preview.zoomIn();
                return true;
            case 169:
                this.preview.zoomOut();
                return true;
        }
    }

    public Location getLocation() {
        if (this.locationListeners == null) {
            return null;
        }
        for (MyLocationListener location : this.locationListeners) {
            Location location2 = location.getLocation();
            if (location2 != null) {
                return location2;
            }
        }
        return null;
    }

    public boolean testHasReceivedLocation() {
        if (this.locationListeners == null) {
            return false;
        }
        for (MyLocationListener myLocationListener : this.locationListeners) {
            if (myLocationListener.test_has_received_location) {
                return true;
            }
        }
        return false;
    }

    private void setupLocationListener() {
    }

    private void freeLocationListeners() {
        if (this.locationListeners != null) {
            for (int i = 0; i < this.locationListeners.length; i++) {
                this.mLocationManager.removeUpdates(this.locationListeners[i]);
                this.locationListeners[i] = null;
            }
            this.locationListeners = null;
        }
    }

    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().getRootView().setBackgroundColor(-16777216);
        this.mSensorManager.registerListener(this.accelerometerListener, this.mSensorAccelerometer, 3);
        this.mSensorManager.registerListener(this.magneticListener, this.mSensorMagnetic, 3);
        this.orientationEventListener.enable();
        setupLocationListener();
        layoutUI();
        updateGalleryIcon();
        this.preview.onResume();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!this.camera_in_background && hasFocus) {
            initImmersiveMode();
        }
    }

    protected void onPause() {
        super.onPause();
        closePopup();
        this.mSensorManager.unregisterListener(this.accelerometerListener);
        this.mSensorManager.unregisterListener(this.magneticListener);
        this.orientationEventListener.disable();
        freeLocationListeners();
        this.preview.onPause();
    }

    public void layoutUI() {
        int width_dp;
        this.preview.updateUIPlacement();
        boolean ui_placement_right = PreferenceManager.getDefaultSharedPreferences(this).getString(getUIPlacementPreferenceKey(), "ui_right").equals("ui_right");
        int degrees = 0;
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
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
        int ui_rotation = (360 - ((this.current_orientation + degrees) % 360)) % 360;
        this.preview.setUIRotation(ui_rotation);
        int above = 2;
        int below = 3;
        int align_parent_top = 10;
        int align_parent_bottom = 12;
        if (!ui_placement_right) {
            above = 3;
            below = 2;
            align_parent_top = 12;
            align_parent_bottom = 10;
        }
        String action = getIntent().getAction();
        View view = findViewById(R.id.gui_anchor);
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.addRule(9, 0);
        layoutParams.addRule(11, -1);
        layoutParams.addRule(align_parent_top, -1);
        layoutParams.addRule(align_parent_bottom, 0);
        layoutParams.addRule(0, 0);
        layoutParams.addRule(1, 0);
        view.setLayoutParams(layoutParams);
        view.setRotation((float) ui_rotation);
        view = findViewById(R.id.settings);
        layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.addRule(align_parent_top, -1);
        layoutParams.addRule(align_parent_bottom, 0);
        layoutParams.addRule(0, R.id.gui_anchor);
        layoutParams.addRule(1, 0);
        view.setLayoutParams(layoutParams);
        view.setRotation((float) ui_rotation);
        view = findViewById(R.id.gallery);
        layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.addRule(align_parent_top, -1);
        layoutParams.addRule(align_parent_bottom, 0);
        layoutParams.addRule(0, R.id.settings);
        layoutParams.addRule(1, 0);
        view.setLayoutParams(layoutParams);
        view.setRotation((float) ui_rotation);
        view.setVisibility(8);
        view = findViewById(R.id.popup);
        layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.addRule(align_parent_top, -1);
        layoutParams.addRule(align_parent_bottom, 0);
        layoutParams.addRule(0, R.id.gallery);
        layoutParams.addRule(1, 0);
        view.setLayoutParams(layoutParams);
        view.setRotation((float) ui_rotation);
        view = findViewById(R.id.exposure_lock);
        layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.addRule(align_parent_top, -1);
        layoutParams.addRule(align_parent_bottom, 0);
        layoutParams.addRule(0, R.id.popup);
        layoutParams.addRule(1, 0);
        view.setLayoutParams(layoutParams);
        view.setRotation((float) ui_rotation);
        view = findViewById(R.id.exposure);
        layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.addRule(align_parent_top, -1);
        layoutParams.addRule(align_parent_bottom, 0);
        layoutParams.addRule(0, R.id.exposure_lock);
        layoutParams.addRule(1, 0);
        view.setLayoutParams(layoutParams);
        view.setRotation((float) ui_rotation);
        view = findViewById(R.id.switch_video);
        layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.addRule(align_parent_top, -1);
        layoutParams.addRule(align_parent_bottom, 0);
        layoutParams.addRule(0, R.id.exposure);
        layoutParams.addRule(1, 0);
        view.setLayoutParams(layoutParams);
        view.setRotation((float) ui_rotation);
        if ("android.media.action.VIDEO_CAPTURE".equalsIgnoreCase(action) || "android.media.action.IMAGE_CAPTURE".equalsIgnoreCase(action)) {
            view.setEnabled(false);
        }
        view = findViewById(R.id.switch_camera);
        layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.addRule(9, 0);
        layoutParams.addRule(11, 0);
        layoutParams.addRule(align_parent_top, -1);
        layoutParams.addRule(align_parent_bottom, 0);
        layoutParams.addRule(0, R.id.switch_video);
        layoutParams.addRule(1, 0);
        view.setLayoutParams(layoutParams);
        view.setRotation((float) ui_rotation);
        if ("android.media.action.VIDEO_CAPTURE".equalsIgnoreCase(action) || "android.media.action.IMAGE_CAPTURE".equalsIgnoreCase(action)) {
            view.setEnabled(false);
        }
        view = findViewById(R.id.trash);
        layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.addRule(align_parent_top, -1);
        layoutParams.addRule(align_parent_bottom, 0);
        layoutParams.addRule(0, R.id.switch_camera);
        layoutParams.addRule(1, 0);
        view.setLayoutParams(layoutParams);
        view.setRotation((float) ui_rotation);
        view = findViewById(R.id.share);
        layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.addRule(align_parent_top, -1);
        layoutParams.addRule(align_parent_bottom, 0);
        layoutParams.addRule(0, R.id.trash);
        layoutParams.addRule(1, 0);
        view.setLayoutParams(layoutParams);
        view.setRotation((float) ui_rotation);
        view = findViewById(R.id.take_photo);
        layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.addRule(9, 0);
        layoutParams.addRule(11, -1);
        view.setLayoutParams(layoutParams);
        view.setRotation((float) ui_rotation);
        view = findViewById(R.id.zoom);
        layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.addRule(9, 0);
        layoutParams.addRule(11, -1);
        layoutParams.addRule(align_parent_top, 0);
        layoutParams.addRule(align_parent_bottom, -1);
        view.setLayoutParams(layoutParams);
        view.setRotation(180.0f);
        view = findViewById(R.id.zoom_seekbar);
        layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.addRule(5, 0);
        layoutParams.addRule(7, R.id.zoom);
        layoutParams.addRule(above, R.id.zoom);
        layoutParams.addRule(below, 0);
        view.setLayoutParams(layoutParams);
        view = findViewById(R.id.seekbar);
        view.setRotation((float) ui_rotation);
        if (ui_rotation == 0 || ui_rotation == 180) {
            width_dp = HttpStatus.SC_MULTIPLE_CHOICES;
        } else {
            width_dp = 200;
        }
        float scale = getResources().getDisplayMetrics().density;
        int height_pixels = (int) ((((float) 50) * scale) + 0.5f);
        ViewGroup.LayoutParams lp = (LayoutParams) view.getLayoutParams();
        lp.width = (int) ((((float) width_dp) * scale) + 0.5f);
        lp.height = height_pixels;
        view.setLayoutParams(lp);
        view = findViewById(R.id.seekbar_zoom);
        view.setRotation((float) ui_rotation);
        view.setAlpha(0.5f);
        if (ui_rotation == 0) {
            view.setTranslationX(0.0f);
            view.setTranslationY((float) height_pixels);
        } else if (ui_rotation == 90) {
            view.setTranslationX((float) (-height_pixels));
            view.setTranslationY(0.0f);
        } else if (ui_rotation == 180) {
            view.setTranslationX(0.0f);
            view.setTranslationY((float) (-height_pixels));
        } else if (ui_rotation == 270) {
            view.setTranslationX((float) height_pixels);
            view.setTranslationY(0.0f);
        }
        view = findViewById(R.id.popup_container);
        layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.addRule(7, R.id.popup);
        layoutParams.addRule(below, R.id.popup);
        layoutParams.addRule(align_parent_bottom, -1);
        layoutParams.addRule(above, 0);
        layoutParams.addRule(align_parent_top, 0);
        view.setLayoutParams(layoutParams);
        view.setRotation((float) ui_rotation);
        view.setTranslationX(0.0f);
        view.setTranslationY(0.0f);
        if (ui_rotation == 0 || ui_rotation == 180) {
            view.setPivotX(((float) view.getWidth()) / 2.0f);
            view.setPivotY(((float) view.getHeight()) / 2.0f);
        } else {
            view.setPivotX((float) view.getWidth());
            view.setPivotY(ui_placement_right ? 0.0f : (float) view.getHeight());
            if (ui_placement_right) {
                if (ui_rotation == 90) {
                    view.setTranslationY((float) view.getWidth());
                } else if (ui_rotation == 270) {
                    view.setTranslationX((float) (-view.getHeight()));
                }
            } else if (ui_rotation == 90) {
                view.setTranslationX((float) (-view.getHeight()));
            } else if (ui_rotation == 270) {
                view.setTranslationY((float) (-view.getWidth()));
            }
        }
        ImageButton view2 = (ImageButton) findViewById(R.id.take_photo);
        if (this.preview != null) {
            view2.setImageResource(this.preview.isVideo() ? R.drawable.take_video_selector : R.drawable.take_photo_selector);
        }
    }

    private void onOrientationChanged(int orientation) {
        if (orientation != -1) {
            int diff = Math.abs(orientation - this.current_orientation);
            if (diff > 180) {
                diff = 360 - diff;
            }
            if (diff > 60) {
                orientation = (((orientation + 45) / 90) * 90) % 360;
                if (orientation != this.current_orientation) {
                    this.current_orientation = orientation;
                    layoutUI();
                }
            }
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        this.preview.setCameraDisplayOrientation();
        super.onConfigurationChanged(newConfig);
    }

    public void clickedTakePhoto(View view) {
        takePicture();
    }

    public void clickedSwitchCamera(View view) {
        closePopup();
        this.preview.switchCamera();
    }

    public void clickedSwitchVideo(View view) {
        closePopup();
        this.preview.switchVideo(true, true);
    }

    public void clickedFlash(View view) {
        this.preview.cycleFlash();
    }

    public void clickedFocusMode(View view) {
        this.preview.cycleFocusMode();
    }

    void clearSeekBar() {
        findViewById(R.id.seekbar).setVisibility(8);
        findViewById(R.id.seekbar_zoom).setVisibility(8);
    }

    void setSeekBarExposure() {
        SeekBar seek_bar = (SeekBar) findViewById(R.id.seekbar);
        int min_exposure = this.preview.getMinimumExposure();
        seek_bar.setMax(this.preview.getMaximumExposure() - min_exposure);
        seek_bar.setProgress(this.preview.getCurrentExposure() - min_exposure);
    }

    public void clickedExposure(View view) {
        closePopup();
        SeekBar seek_bar = (SeekBar) findViewById(R.id.seekbar);
        int visibility = seek_bar.getVisibility();
        if (visibility == 8 && this.preview.getCameraController() != null && this.preview.supportsExposures()) {
            final int min_exposure = this.preview.getMinimumExposure();
            seek_bar.setVisibility(0);
            setSeekBarExposure();
            seek_bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    MainActivity.this.preview.setExposure(min_exposure + progress, false);
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            ZoomControls seek_bar_zoom = (ZoomControls) findViewById(R.id.seekbar_zoom);
            seek_bar_zoom.setVisibility(0);
            seek_bar_zoom.setOnZoomInClickListener(new OnClickListener() {
                public void onClick(View v) {
                    MainActivity.this.preview.changeExposure(1, true);
                }
            });
            seek_bar_zoom.setOnZoomOutClickListener(new OnClickListener() {
                public void onClick(View v) {
                    MainActivity.this.preview.changeExposure(-1, true);
                }
            });
        } else if (visibility == 0) {
            clearSeekBar();
        }
    }

    public void clickedExposureLock(View view) {
        this.preview.toggleExposureLock();
    }

    public void clickedSettings(View view) {
        openSettings();
    }

    public boolean popupIsOpen() {
        if (this.popup_view != null) {
            return true;
        }
        return false;
    }

    public View getPopupButton(String key) {
        return this.popup_view.getPopupButton(key);
    }

    void closePopup() {
        if (popupIsOpen()) {
            ((ViewGroup) findViewById(R.id.popup_container)).removeAllViews();
            this.popup_view.close();
            this.popup_view = null;
            initImmersiveMode();
        }
    }

    Bitmap getPreloadedBitmap(int resource) {
        return (Bitmap) this.preloaded_bitmap_resources.get(Integer.valueOf(resource));
    }

    public void clickedPopupSettings(View view) {
        final ViewGroup popup_container = (ViewGroup) findViewById(R.id.popup_container);
        if (popupIsOpen()) {
            closePopup();
        } else if (this.preview.getCameraController() != null) {
            clearSeekBar();
            this.preview.cancelTimer();
            final long time_s = System.currentTimeMillis();
            popup_container.setBackgroundColor(-16777216);
            popup_container.setAlpha(0.95f);
            this.popup_view = new PopupView(this);
            popup_container.addView(this.popup_view);
            popup_container.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                @SuppressLint({"NewApi"})
                public void onGlobalLayout() {
                    float f;
                    MainActivity.this.layoutUI();
                    if (VERSION.SDK_INT > 15) {
                        popup_container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        popup_container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                    if (PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString(MainActivity.getUIPlacementPreferenceKey(), "ui_right").equals("ui_right")) {
                        f = 0.0f;
                    } else {
                        f = 1.0f;
                    }
                    ScaleAnimation animation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, 1, 1.0f, 1, f);
                    animation.setDuration(100);
                    popup_container.setAnimation(animation);
                }
            });
        }
    }

    private void openSettings() {
        int[] widths;
        int[] heights;
        int i;
        closePopup();
        this.preview.cancelTimer();
        this.preview.stopVideo(false);
        Bundle bundle = new Bundle();
        bundle.putInt("cameraId", this.preview.getCameraId());
        bundle.putString("camera_api", this.preview.getCameraAPI());
        bundle.putBoolean("supports_auto_stabilise", this.supports_auto_stabilise);
        bundle.putBoolean("supports_force_video_4k", this.supports_force_video_4k);
        bundle.putBoolean("supports_face_detection", this.preview.supportsFaceDetection());
        bundle.putBoolean("supports_video_stabilization", this.preview.supportsVideoStabilization());
        bundle.putBoolean("can_disable_shutter_sound", this.preview.canDisableShutterSound());
        putBundleExtra(bundle, "color_effects", this.preview.getSupportedColorEffects());
        putBundleExtra(bundle, "scene_modes", this.preview.getSupportedSceneModes());
        putBundleExtra(bundle, "white_balances", this.preview.getSupportedWhiteBalances());
        putBundleExtra(bundle, "isos", this.preview.getSupportedISOs());
        bundle.putString("iso_key", this.preview.getISOKey());
        if (this.preview.getCameraController() != null) {
            bundle.putString("parameters_string", this.preview.getCameraController().getParametersString());
        }
        List<Size> preview_sizes = this.preview.getSupportedPreviewSizes();
        if (preview_sizes != null) {
            widths = new int[preview_sizes.size()];
            heights = new int[preview_sizes.size()];
            i = 0;
            for (Size size : preview_sizes) {
                widths[i] = size.width;
                heights[i] = size.height;
                i++;
            }
            bundle.putIntArray("preview_widths", widths);
            bundle.putIntArray("preview_heights", heights);
        }
        List<Size> sizes = this.preview.getSupportedPictureSizes();
        if (sizes != null) {
            widths = new int[sizes.size()];
            heights = new int[sizes.size()];
            i = 0;
            for (Size size2 : sizes) {
                widths[i] = size2.width;
                heights[i] = size2.height;
                i++;
            }
            bundle.putIntArray("resolution_widths", widths);
            bundle.putIntArray("resolution_heights", heights);
        }
        List<String> video_quality = this.preview.getSupportedVideoQuality();
        if (video_quality != null) {
            String[] video_quality_arr = new String[video_quality.size()];
            String[] video_quality_string_arr = new String[video_quality.size()];
            i = 0;
            for (String value : video_quality) {
                video_quality_arr[i] = value;
                video_quality_string_arr[i] = this.preview.getCamcorderProfileDescription(value);
                i++;
            }
            bundle.putStringArray("video_quality", video_quality_arr);
            bundle.putStringArray("video_quality_string", video_quality_string_arr);
        }
        List<Size> video_sizes = this.preview.getSupportedVideoSizes();
        if (video_sizes != null) {
            widths = new int[video_sizes.size()];
            heights = new int[video_sizes.size()];
            i = 0;
            for (Size size22 : video_sizes) {
                widths[i] = size22.width;
                heights[i] = size22.height;
                i++;
            }
            bundle.putIntArray("video_widths", widths);
            bundle.putIntArray("video_heights", heights);
        }
        putBundleExtra(bundle, "flash_values", this.preview.getSupportedFlashValues());
        putBundleExtra(bundle, "focus_values", this.preview.getSupportedFocusValues());
        setWindowFlagsForSettings();
        MyPreferenceFragment fragment = new MyPreferenceFragment();
        fragment.setArguments(bundle);
        getFragmentManager().beginTransaction().add(R.id.prefs_container, fragment, "PREFERENCE_FRAGMENT").addToBackStack(null).commit();
    }

    public void updateForSettings() {
        updateForSettings(null);
    }

    public void updateForSettings(String toast_message) {
        String saved_focus_value = null;
        if (!(this.preview.getCameraController() == null || !this.preview.isVideo() || this.preview.focusIsVideo())) {
            saved_focus_value = this.preview.getCurrentFocusValue();
            this.preview.updateFocusForVideo(false);
        }
        updateFolderHistory();
        boolean need_reopen = false;
        if (this.preview.getCameraController() != null) {
            if (!PreferenceManager.getDefaultSharedPreferences(this).getString(getSceneModePreferenceKey(), this.preview.getCameraController().getDefaultSceneMode()).equals(this.preview.getCameraController().getSceneMode())) {
                need_reopen = true;
            }
        }
        layoutUI();
        setupLocationListener();
        if (need_reopen || this.preview.getCameraController() == null) {
            this.preview.onPause();
            this.preview.onResume(toast_message);
        } else {
            this.preview.setCameraDisplayOrientation();
            this.preview.pausePreview();
            this.preview.setupCamera(toast_message, false);
        }
        if (saved_focus_value != null) {
            this.preview.updateFocus(saved_focus_value, true, false);
        }
    }

    boolean cameraInBackground() {
        return this.camera_in_background;
    }

    MyPreferenceFragment getPreferenceFragment() {
        return (MyPreferenceFragment) getFragmentManager().findFragmentByTag("PREFERENCE_FRAGMENT");
    }

    public void onBackPressed() {
        MyPreferenceFragment fragment = getPreferenceFragment();
        if (this.screen_is_locked) {
            this.preview.showToast(this.screen_locked_toast, R.string.screen_is_locked);
            return;
        }
        if (fragment != null) {
            setWindowFlagsForCamera();
            updateForSettings();
        } else if (popupIsOpen()) {
            closePopup();
            return;
        }
        super.onBackPressed();
    }

    boolean usingKitKatImmersiveMode() {
        if (VERSION.SDK_INT >= 19) {
            String immersive_mode = PreferenceManager.getDefaultSharedPreferences(this).getString(getImmersiveModePreferenceKey(), "immersive_mode_low_profile");
            if (immersive_mode.equals("immersive_mode_gui") || immersive_mode.equals("immersive_mode_everything")) {
                return true;
            }
        }
        return false;
    }

    private void setImmersiveTimer() {
        if (!(this.immersive_timer_handler == null || this.immersive_timer_runnable == null)) {
            this.immersive_timer_handler.removeCallbacks(this.immersive_timer_runnable);
        }
        this.immersive_timer_handler = new Handler();
        Handler handler = this.immersive_timer_handler;
        Runnable anonymousClass10 = new Runnable() {
            public void run() {
                if (!MainActivity.this.camera_in_background && !MainActivity.this.popupIsOpen() && MainActivity.this.usingKitKatImmersiveMode()) {
                    MainActivity.this.setImmersiveMode(true);
                }
            }
        };
        this.immersive_timer_runnable = anonymousClass10;
        handler.postDelayed(anonymousClass10, 5000);
    }

    void initImmersiveMode() {
        if (usingKitKatImmersiveMode()) {
            setImmersiveTimer();
        } else {
            setImmersiveMode(true);
        }
    }

    @TargetApi(19)
    void setImmersiveMode(boolean on) {
        if (!on) {
            getWindow().getDecorView().setSystemUiVisibility(0);
        } else if (VERSION.SDK_INT >= 19 && usingKitKatImmersiveMode()) {
            getWindow().getDecorView().setSystemUiVisibility(2310);
        } else if (PreferenceManager.getDefaultSharedPreferences(this).getString(getImmersiveModePreferenceKey(), "immersive_mode_low_profile").equals("immersive_mode_low_profile")) {
            getWindow().getDecorView().setSystemUiVisibility(1);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(0);
        }
    }

    private void setWindowFlagsForCamera() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setRequestedOrientation(0);
        if (sharedPreferences.getBoolean(getKeepDisplayOnPreferenceKey(), true)) {
            getWindow().addFlags(128);
        } else {
            getWindow().clearFlags(128);
        }
        if (sharedPreferences.getBoolean(getShowWhenLockedPreferenceKey(), true)) {
            getWindow().addFlags(524288);
        } else {
            getWindow().clearFlags(524288);
        }
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        if (sharedPreferences.getBoolean(getMaxBrightnessPreferenceKey(), true)) {
            layout.screenBrightness = 1.0f;
        } else {
            layout.screenBrightness = -1.0f;
        }
        getWindow().setAttributes(layout);
        initImmersiveMode();
        this.camera_in_background = false;
    }

    private void setWindowFlagsForSettings() {
        setRequestedOrientation(-1);
        getWindow().clearFlags(128);
        getWindow().clearFlags(524288);
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = -1.0f;
        getWindow().setAttributes(layout);
        setImmersiveMode(false);
        this.camera_in_background = true;
    }

    private void showPreview(boolean show) {
        ViewGroup container = (ViewGroup) findViewById(R.id.hide_container);
        container.setBackgroundColor(-16777216);
        container.setAlpha(show ? 0.0f : 1.0f);
    }

    private Media getLatestMedia(boolean video) {
        Media media = null;
        Uri baseUri = video ? android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI : android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(baseUri, video ? new String[]{AppSQLite.KEY_ID, VideoColumns.DATE_TAKEN, "_data"} : new String[]{AppSQLite.KEY_ID, VideoColumns.DATE_TAKEN, "_data", "orientation"}, video ? "" : "mime_type='image/jpeg'", null, video ? "datetaken DESC,_id DESC" : "datetaken DESC,_id DESC");
            if (cursor != null && cursor.moveToFirst()) {
                boolean found = false;
                String save_folder_string = new StringBuilder(String.valueOf(getImageFolder().getAbsolutePath())).append(File.separator).toString();
                do {
                    String path = cursor.getString(2);
                    if (path != null && path.contains(save_folder_string) && cursor.getLong(1) <= 172800000 + System.currentTimeMillis()) {
                        found = true;
                        break;
                    }
                } while (cursor.moveToNext());
                if (!found) {
                    cursor.moveToFirst();
                }
                long id = cursor.getLong(0);
                media = new Media(id, video, ContentUris.withAppendedId(baseUri, id), cursor.getLong(1), video ? 0 : cursor.getInt(3));
            }
            if (cursor != null) {
                cursor.close();
            }
            return media;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private Media getLatestMedia() {
        Media image_media = getLatestMedia(false);
        Media video_media = getLatestMedia(true);
        if (image_media != null && video_media == null) {
            return image_media;
        }
        if (image_media == null && video_media != null) {
            return video_media;
        }
        if (image_media == null || video_media == null) {
            return null;
        }
        if (image_media.date >= video_media.date) {
            return image_media;
        }
        return video_media;
    }

    public void updateGalleryIconToBlank() {
        ImageButton galleryButton = (ImageButton) findViewById(R.id.gallery);
        int bottom = galleryButton.getPaddingBottom();
        int top = galleryButton.getPaddingTop();
        int right = galleryButton.getPaddingRight();
        int left = galleryButton.getPaddingLeft();
        galleryButton.setImageBitmap(null);
        galleryButton.setImageResource(R.drawable.gallery);
        galleryButton.setPadding(left, top, right, bottom);
        this.gallery_bitmap = null;
    }

    public void updateGalleryIconToBitmap(Bitmap bitmap) {
        ((ImageButton) findViewById(R.id.gallery)).setImageBitmap(bitmap);
        this.gallery_bitmap = bitmap;
    }

    public void updateGalleryIcon() {
        long time_s = System.currentTimeMillis();
        Media media = getLatestMedia();
        Bitmap thumbnail = null;
        if (media != null) {
            if (media.video) {
                thumbnail = Thumbnails.getThumbnail(getContentResolver(), media.id, 1, null);
            } else {
                thumbnail = Images.Thumbnails.getThumbnail(getContentResolver(), media.id, 1, null);
            }
            if (!(thumbnail == null || media.orientation == 0)) {
                Matrix matrix = new Matrix();
                matrix.setRotate((float) media.orientation, ((float) thumbnail.getWidth()) * 0.5f, ((float) thumbnail.getHeight()) * 0.5f);
                try {
                    Bitmap rotated_thumbnail = Bitmap.createBitmap(thumbnail, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(), matrix, true);
                    if (rotated_thumbnail != thumbnail) {
                        thumbnail.recycle();
                        thumbnail = rotated_thumbnail;
                    }
                } catch (Throwable th) {
                }
            }
        }
        this.last_media_scanned = null;
        if (thumbnail != null) {
            updateGalleryIconToBitmap(thumbnail);
        } else {
            updateGalleryIconToBlank();
        }
    }

    public void clickedGallery(View view) {
        Uri uri = this.last_media_scanned;
        if (uri == null) {
            Media media = getLatestMedia();
            if (media != null) {
                uri = media.uri;
            }
        }
        if (uri != null) {
            try {
                ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "r");
                if (pfd == null) {
                    uri = null;
                }
                pfd.close();
            } catch (IOException e) {
                uri = null;
            }
        }
        if (uri == null) {
            uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        if (!this.is_test) {
            String REVIEW_ACTION = "com.android.camera.action.REVIEW";
            try {
                startActivity(new Intent("com.android.camera.action.REVIEW", uri));
            } catch (ActivityNotFoundException e2) {
                Intent intent = new Intent("android.intent.action.VIEW", uri);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    this.preview.showToast(null, R.string.no_gallery_app);
                }
            }
        }
    }

    private void updateFolderHistory() {
        updateFolderHistory(getSaveLocation());
        updateGalleryIcon();
    }

    private void updateFolderHistory(String folder_name) {
        do {
        } while (this.save_location_history.remove(folder_name));
        this.save_location_history.add(folder_name);
        while (this.save_location_history.size() > 6) {
            this.save_location_history.remove(0);
        }
        writeSaveLocations();
    }

    public void clearFolderHistory() {
        this.save_location_history.clear();
        updateFolderHistory();
    }

    private void writeSaveLocations() {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putInt("save_location_history_size", this.save_location_history.size());
        for (int i = 0; i < this.save_location_history.size(); i++) {
            editor.putString("save_location_history_" + i, (String) this.save_location_history.get(i));
        }
        editor.apply();
    }

    private void openFolderChooserDialog() {
        showPreview(false);
        setWindowFlagsForSettings();
        final String orig_save_location = getSaveLocation();
        new FolderChooserDialog() {
            public void onDismiss(DialogInterface dialog) {
                MainActivity.this.setWindowFlagsForCamera();
                MainActivity.this.showPreview(true);
                if (!orig_save_location.equals(MainActivity.this.getSaveLocation())) {
                    MainActivity.this.updateFolderHistory();
                    MainActivity.this.preview.showToast(null, new StringBuilder(String.valueOf(getResources().getString(R.string.changed_save_location))).append("\n").append(MainActivity.this.getSaveLocation()).toString());
                }
                super.onDismiss(dialog);
            }
        }.show(getFragmentManager(), "FOLDER_FRAGMENT");
    }

    private void longClickedGallery() {
        if (this.save_location_history.size() <= 1) {
            openFolderChooserDialog();
            return;
        }
        int index;
        showPreview(false);
        Builder alertDialog = new Builder(this);
        alertDialog.setTitle(R.string.choose_save_location);
        CharSequence[] items = new CharSequence[(this.save_location_history.size() + 2)];
        int index2 = 0;
        int i = 0;
        while (i < this.save_location_history.size()) {
            index = index2 + 1;
            items[index2] = (CharSequence) this.save_location_history.get((this.save_location_history.size() - 1) - i);
            i++;
            index2 = index;
        }
        final int clear_index = index2;
        index = index2 + 1;
        items[index2] = getResources().getString(R.string.clear_folder_history);
        final int new_index = index;
        index2 = index + 1;
        items[index] = getResources().getString(R.string.choose_another_folder);
        alertDialog.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == clear_index) {
                    new Builder(MainActivity.this).setIcon(17301543).setTitle(R.string.clear_folder_history).setMessage(R.string.clear_folder_history_question).setPositiveButton(R.string.answer_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.this.clearFolderHistory();
                            MainActivity.this.setWindowFlagsForCamera();
                            MainActivity.this.showPreview(true);
                        }
                    }).setNegativeButton(R.string.answer_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.this.setWindowFlagsForCamera();
                            MainActivity.this.showPreview(true);
                        }
                    }).setOnCancelListener(new OnCancelListener() {
                        public void onCancel(DialogInterface arg0) {
                            MainActivity.this.setWindowFlagsForCamera();
                            MainActivity.this.showPreview(true);
                        }
                    }).show();
                } else if (which == new_index) {
                    MainActivity.this.openFolderChooserDialog();
                } else {
                    if (which >= 0 && which < MainActivity.this.save_location_history.size()) {
                        String save_folder = (String) MainActivity.this.save_location_history.get((MainActivity.this.save_location_history.size() - 1) - which);
                        MainActivity.this.preview.showToast(null, new StringBuilder(String.valueOf(MainActivity.this.getResources().getString(R.string.changed_save_location))).append("\n").append(save_folder).toString());
                        Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                        editor.putString(MainActivity.getSaveLocationPreferenceKey(), save_folder);
                        editor.apply();
                        MainActivity.this.updateFolderHistory();
                    }
                    MainActivity.this.setWindowFlagsForCamera();
                    MainActivity.this.showPreview(true);
                }
            }
        });
        alertDialog.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface arg0) {
                MainActivity.this.setWindowFlagsForCamera();
                MainActivity.this.showPreview(true);
            }
        });
        alertDialog.show();
        setWindowFlagsForSettings();
    }

    private static void putBundleExtra(Bundle bundle, String key, List<String> values) {
        if (values != null) {
            String[] values_arr = new String[values.size()];
            int i = 0;
            for (String value : values) {
                values_arr[i] = value;
                i++;
            }
            bundle.putStringArray(key, values_arr);
        }
    }

    public void clickedShare(View view) {
        this.preview.clickedShare();
    }

    public void clickedTrash(View view) {
        this.preview.clickedTrash();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                MainActivity.this.updateGalleryIcon();
            }
        }, 500);
    }

    private void takePicture() {
        closePopup();
        this.preview.takePicturePressed();
    }

    void lockScreen() {
        ((ViewGroup) findViewById(R.id.locker)).setOnTouchListener(new OnTouchListener() {
            @SuppressLint({"ClickableViewAccessibility"})
            public boolean onTouch(View arg0, MotionEvent event) {
                return MainActivity.this.gestureDetector.onTouchEvent(event);
            }
        });
        this.screen_is_locked = true;
    }

    void unlockScreen() {
        ((ViewGroup) findViewById(R.id.locker)).setOnTouchListener(null);
        this.screen_is_locked = false;
    }

    boolean isScreenLocked() {
        return this.screen_is_locked;
    }

    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        if (this.preview != null) {
            this.preview.onSaveInstanceState(state);
        }
    }

    public void broadcastFile(File file, final boolean is_new_picture, final boolean is_new_video) {
        if (!file.isDirectory()) {
            this.failed_to_scan = true;
            MediaScannerConnection.scanFile(this, new String[]{file.getAbsolutePath()}, null, new OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                    MainActivity.this.last_media_scanned = uri;
                    if (is_new_picture) {
                        MainActivity.this.sendBroadcast(new Intent("android.hardware.action.NEW_PICTURE", uri));
                        MainActivity.this.sendBroadcast(new Intent("com.android.camera.NEW_PICTURE", uri));
                    } else if (is_new_video) {
                        MainActivity.this.sendBroadcast(new Intent("android.hardware.action.NEW_VIDEO", uri));
                    }
                    MainActivity.this.failed_to_scan = false;
                }
            });
        }
    }

    private String getSaveLocation() {
        return PreferenceManager.getDefaultSharedPreferences(this).getString(getSaveLocationPreferenceKey(), "OpenCamera");
    }

    static File getBaseFolder() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
    }

    static File getImageFolder(String folder_name) {
        if (folder_name.length() > 0 && folder_name.lastIndexOf(47) == folder_name.length() - 1) {
            folder_name = folder_name.substring(0, folder_name.length() - 1);
        }
        if (folder_name.startsWith("/")) {
            return new File(folder_name);
        }
        return new File(getBaseFolder(), folder_name);
    }

    public File getImageFolder() {
        return getImageFolder(getSaveLocation());
    }

    @SuppressLint({"SimpleDateFormat"})
    public File getOutputMediaFile(int type) {
        File mediaStorageDir = getImageFolder();
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
            broadcastFile(mediaStorageDir, false, false);
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String index = "";
        File mediaFile = null;
        for (int count = 1; count <= 100; count++) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            if (type == 1) {
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + sharedPreferences.getString(getSavePhotoPrefixPreferenceKey(), "IMG_") + timeStamp + index + ".jpg");
            } else if (type != 2) {
                return null;
            } else {
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + sharedPreferences.getString(getSaveVideoPrefixPreferenceKey(), "VID_") + timeStamp + index + ".mp4");
            }
            if (!mediaFile.exists()) {
                return mediaFile;
            }
            index = "_" + count;
        }
        return mediaFile;
    }

    public boolean supportsAutoStabilise() {
        return this.supports_auto_stabilise;
    }

    public boolean supportsForceVideo4K() {
        return this.supports_force_video_4k;
    }

    void disableForceVideo4K() {
        this.supports_force_video_4k = false;
    }

    public long freeMemory() {
        StatFs statFs;
        try {
            statFs = new StatFs(getImageFolder().getAbsolutePath());
            return (((long) statFs.getAvailableBlocks()) * ((long) statFs.getBlockSize())) / 1048576;
        } catch (IllegalArgumentException e) {
            try {
                if (!getSaveLocation().startsWith("/")) {
                    statFs = new StatFs(getBaseFolder().getAbsolutePath());
                    return (((long) statFs.getAvailableBlocks()) * ((long) statFs.getBlockSize())) / 1048576;
                }
            } catch (IllegalArgumentException e2) {
            }
            return -1;
        }
    }

    public static String getDonateLink() {
        return "https://play.google.com/store/apps/details?id=harman.mark.donation";
    }

    public Preview getPreview() {
        return this.preview;
    }

    public static String getFirstTimePreferenceKey() {
        return "done_first_time";
    }

    public static String getFlashPreferenceKey(int cameraId) {
        return "flash_value_" + cameraId;
    }

    public static String getFocusPreferenceKey(int cameraId) {
        return "focus_value_" + cameraId;
    }

    public static String getResolutionPreferenceKey(int cameraId) {
        return "camera_resolution_" + cameraId;
    }

    public static String getVideoQualityPreferenceKey(int cameraId) {
        return "video_quality_" + cameraId;
    }

    public static String getIsVideoPreferenceKey() {
        return "is_video";
    }

    public static String getExposurePreferenceKey() {
        return "preference_exposure";
    }

    public static String getColorEffectPreferenceKey() {
        return "preference_color_effect";
    }

    public static String getSceneModePreferenceKey() {
        return "preference_scene_mode";
    }

    public static String getWhiteBalancePreferenceKey() {
        return "preference_white_balance";
    }

    public static String getISOPreferenceKey() {
        return "preference_iso";
    }

    public static String getVolumeKeysPreferenceKey() {
        return "preference_volume_keys";
    }

    public static String getQualityPreferenceKey() {
        return "preference_quality";
    }

    public static String getAutoStabilisePreferenceKey() {
        return "preference_auto_stabilise";
    }

    public static String getLocationPreferenceKey() {
        return "preference_location";
    }

    public static String getGPSDirectionPreferenceKey() {
        return "preference_gps_direction";
    }

    public static String getRequireLocationPreferenceKey() {
        return "preference_require_location";
    }

    public static String getStampPreferenceKey() {
        return "preference_stamp";
    }

    public static String getTextStampPreferenceKey() {
        return "preference_textstamp";
    }

    public static String getStampFontSizePreferenceKey() {
        return "preference_stamp_fontsize";
    }

    public static String getUIPlacementPreferenceKey() {
        return "preference_ui_placement";
    }

    public static String getPausePreviewPreferenceKey() {
        return "preference_pause_preview";
    }

    public static String getThumbnailAnimationPreferenceKey() {
        return "preference_thumbnail_animation";
    }

    public static String getShowWhenLockedPreferenceKey() {
        return "preference_show_when_locked";
    }

    public static String getKeepDisplayOnPreferenceKey() {
        return "preference_keep_display_on";
    }

    public static String getMaxBrightnessPreferenceKey() {
        return "preference_max_brightness";
    }

    public static String getSaveLocationPreferenceKey() {
        return "preference_save_location";
    }

    public static String getSavePhotoPrefixPreferenceKey() {
        return "preference_save_photo_prefix";
    }

    public static String getSaveVideoPrefixPreferenceKey() {
        return "preference_save_video_prefix";
    }

    public static String getShowZoomControlsPreferenceKey() {
        return "preference_show_zoom_controls";
    }

    public static String getShowZoomSliderControlsPreferenceKey() {
        return "preference_show_zoom_slider_controls";
    }

    public static String getShowZoomPreferenceKey() {
        return "preference_show_zoom";
    }

    public static String getShowAnglePreferenceKey() {
        return "preference_show_angle";
    }

    public static String getShowAngleLinePreferenceKey() {
        return "preference_show_angle_line";
    }

    public static String getShowGeoDirectionPreferenceKey() {
        return "preference_show_geo_direction";
    }

    public static String getShowFreeMemoryPreferenceKey() {
        return "preference_free_memory";
    }

    public static String getShowTimePreferenceKey() {
        return "preference_show_time";
    }

    public static String getShowBatteryPreferenceKey() {
        return "preference_show_battery";
    }

    public static String getShowGridPreferenceKey() {
        return "preference_grid";
    }

    public static String getShowCropGuidePreferenceKey() {
        return "preference_crop_guide";
    }

    public static String getFaceDetectionPreferenceKey() {
        return "preference_face_detection";
    }

    public static String getVideoStabilizationPreferenceKey() {
        return "preference_video_stabilization";
    }

    public static String getForceVideo4KPreferenceKey() {
        return "preference_force_video_4k";
    }

    public static String getVideoBitratePreferenceKey() {
        return "preference_video_bitrate";
    }

    public static String getVideoFPSPreferenceKey() {
        return "preference_video_fps";
    }

    public static String getVideoMaxDurationPreferenceKey() {
        return "preference_video_max_duration";
    }

    public static String getVideoRestartPreferenceKey() {
        return "preference_video_restart";
    }

    public static String getVideoFlashPreferenceKey() {
        return "preference_video_flash";
    }

    public static String getLockVideoPreferenceKey() {
        return "preference_lock_video";
    }

    public static String getRecordAudioPreferenceKey() {
        return "preference_record_audio";
    }

    public static String getRecordAudioSourcePreferenceKey() {
        return "preference_record_audio_src";
    }

    public static String getPreviewSizePreferenceKey() {
        return "preference_preview_size";
    }

    public static String getRotatePreviewPreferenceKey() {
        return "preference_rotate_preview";
    }

    public static String getLockOrientationPreferenceKey() {
        return "preference_lock_orientation";
    }

    public static String getTimerPreferenceKey() {
        return "preference_timer";
    }

    public static String getTimerBeepPreferenceKey() {
        return "preference_timer_beep";
    }

    public static String getBurstModePreferenceKey() {
        return "preference_burst_mode";
    }

    public static String getBurstIntervalPreferenceKey() {
        return "preference_burst_interval";
    }

    public static String getShutterSoundPreferenceKey() {
        return "preference_shutter_sound";
    }

    public static String getImmersiveModePreferenceKey() {
        return "preference_immersive_mode";
    }

    public ArrayList<String> getSaveLocationHistory() {
        return this.save_location_history;
    }

    public void usedFolderPicker() {
        updateFolderHistory();
    }

    public boolean hasLocationListeners() {
        if (this.locationListeners == null || this.locationListeners.length != 2) {
            return false;
        }
        for (MyLocationListener myLocationListener : this.locationListeners) {
            if (myLocationListener == null) {
                return false;
            }
        }
        return true;
    }
}
