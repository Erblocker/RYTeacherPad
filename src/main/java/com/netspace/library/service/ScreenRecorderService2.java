package com.netspace.library.service;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.netspace.library.service.ScreenRecorderService.OnRecorderServiceListener;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.ServiceUIHelper;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.wrapper.ScreenRecordPermissionActivity;
import com.netspace.pad.library.R;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.sqlcipher.database.SQLiteDatabase;

@TargetApi(21)
public class ScreenRecorderService2 extends Service {
    private static final String AUDIO_MIME_TYPE = "audio/mp4a-latm";
    private static final String TAG = "ScreenRecorderService2";
    private static final String VIDEO_MIME_TYPE = "video/avc";
    private static OnRecorderServiceListener mCallBack = null;
    private static ScreenRecorderService2 mService;
    private static boolean mServiceStarted = false;
    private final Runnable UpdateTimerRunnable = new Runnable() {
        public void run() {
            if (ScreenRecorderService2.this.mTextViewTimer != null) {
                long diffInSec = TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - ScreenRecorderService2.this.mRecordStartTime.getTime());
                int seconds = (int) (diffInSec % 60);
                diffInSec /= 60;
                int minutes = (int) (diffInSec % 60);
                diffInSec /= 60;
                int hours = (int) (diffInSec % 24);
                diffInSec /= 24;
                final String szResult = String.format("正在录制   %02d:%02d:%02d", new Object[]{Integer.valueOf(hours), Integer.valueOf(minutes), Integer.valueOf(seconds)});
                ScreenRecorderService2.this.mTextViewTimer.post(new Runnable() {
                    public void run() {
                        ScreenRecorderService2.this.mTextViewTimer.setText(szResult);
                    }
                });
                ScreenRecorderService2.this.mUIHandler.postDelayed(ScreenRecorderService2.this.UpdateTimerRunnable, 500);
            }
        }
    };
    private MediaRecorder mAudioRecorder = null;
    private int mAudioTrackIndex = -1;
    private int mBitRate = 0;
    private Runnable mDrainEncoderRunnable = new Runnable() {
        public void run() {
            ScreenRecorderService2.this.drainEncoder();
        }
    };
    private final Handler mDrainHandler = new Handler(Looper.getMainLooper());
    private int mFPS = 5;
    private int mHeight = -1;
    private Surface mInputSurface;
    private MediaProjection mMediaProjection;
    private long mMinStartTime = 0;
    private MediaMuxer mMuxer;
    private boolean mMuxerStarted = false;
    private Date mRecordStartTime;
    private String mTargetAudioFileName = "";
    private String mTargetFileName = "";
    private String mTargetFolder = "";
    private TextView mTextViewTimer;
    private int mTrackIndex = -1;
    private Handler mUIHandler = null;
    private ServiceUIHelper mUIHelper;
    private BufferInfo mVideoBufferInfo;
    private MediaCodec mVideoEncoder;
    private VirtualDisplay mVirtualDisplay;
    private int mWidth = -1;
    private boolean mbRecordAudio = true;

    public static void setCallBack(OnRecorderServiceListener CallBack) {
        mCallBack = CallBack;
    }

    public static void stopRecorder() {
        if (mService != null) {
            mService.endRecord();
        }
    }

    public static void setMediaProjection(MediaProjection MediaProjection) {
        if (mService != null) {
            mService.mMediaProjection = MediaProjection;
            mService.prepareRecordUI();
        }
    }

    public void onCreate() {
        super.onCreate();
        mService = this;
    }

    public static ScreenRecorderService2 getService() {
        if (mService == null) {
            mService = new ScreenRecorderService2();
        }
        return mService;
    }

    public static boolean isActive() {
        return mServiceStarted;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (!(mServiceStarted || intent == null)) {
            if (ScreenRecorderService.getAllowRecord()) {
                this.mBitRate = intent.getIntExtra("bitrate", 6000000);
                this.mFPS = intent.getIntExtra("fps", 25);
                this.mbRecordAudio = intent.getBooleanExtra("recordaudio", true);
                this.mTargetFolder = intent.getStringExtra("folder");
                this.mWidth = Utilities.getScreenWidth((Context) this);
                this.mHeight = Utilities.getScreenHeight((Context) this);
                this.mWidth = 1024;
                this.mHeight = 720;
                if (this.mTargetFolder == null || this.mTargetFolder.isEmpty()) {
                    throw new InvalidParameterException("folder must be set.");
                }
                mService = this;
                Intent intent2 = new Intent(this, ScreenRecordPermissionActivity.class);
                intent2.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
                startActivity(intent2);
            } else {
                Toast.makeText(this, "当前处于上课模式，不允许录屏", 0).show();
                stopSelf();
            }
        }
        return 2;
    }

    public void onDestroy() {
        if (this.mUIHelper != null) {
            this.mUIHelper.hideWindow();
        }
        mServiceStarted = false;
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public Activity getActivity() {
        if (UI.getCurrentActivity() != null) {
            return UI.getCurrentActivity();
        }
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread", new Class[0]).invoke(null, new Object[0]);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            for (Object activityRecord : ((Map) activitiesField.get(activityThread)).values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    return (Activity) activityField.get(activityRecord);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void prepareRecordUI() {
        mServiceStarted = true;
        this.mUIHelper = new ServiceUIHelper(this, R.layout.layout_screenrecorder, (Utilities.getScreenWidth((Context) this) - 100) / 2, 0);
        this.mUIHelper.setMove(true, false);
        this.mUIHelper.showWindow();
        this.mTextViewTimer = (TextView) this.mUIHelper.getView().findViewById(R.id.textViewStatus);
        this.mUIHelper.getView().findViewById(R.id.buttonStop).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ScreenRecorderService2.this.mUIHandler.removeCallbacks(ScreenRecorderService2.this.UpdateTimerRunnable);
                ScreenRecorderService2.stopRecorder();
                v.setEnabled(false);
                v.setVisibility(4);
                ScreenRecorderService2.this.mTextViewTimer.setText("正在保存数据...");
            }
        });
        this.mRecordStartTime = new Date();
        if (this.mUIHandler == null) {
            this.mUIHandler = new Handler(Looper.getMainLooper());
        }
        this.mUIHandler.postDelayed(this.UpdateTimerRunnable, 500);
        startRecord();
    }

    public boolean startRecord() {
        final String szErrorMessage;
        if (this.mAudioRecorder != null) {
            Log.e(TAG, "Already called startRecord, Second call ignored.");
            return true;
        }
        this.mTargetFileName = this.mTargetFolder + Utilities.createGUID() + ".mp4";
        this.mTargetAudioFileName = this.mTargetFileName + ".aac";
        try {
            this.mVideoBufferInfo = new BufferInfo();
            MediaFormat format = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, this.mWidth, this.mHeight);
            format.setInteger(io.vov.vitamio.MediaFormat.KEY_COLOR_FORMAT, 2130708361);
            format.setInteger("bitrate", this.mBitRate);
            format.setInteger(io.vov.vitamio.MediaFormat.KEY_FRAME_RATE, this.mFPS);
            format.setInteger("capture-rate", this.mFPS);
            format.setInteger(io.vov.vitamio.MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1000000 / this.mFPS);
            format.setInteger(io.vov.vitamio.MediaFormat.KEY_CHANNEL_COUNT, 1);
            format.setInteger(io.vov.vitamio.MediaFormat.KEY_I_FRAME_INTERVAL, 1);
            this.mVideoEncoder = MediaCodec.createEncoderByType(VIDEO_MIME_TYPE);
            this.mVideoEncoder.configure(format, null, null, 1);
            this.mInputSurface = this.mVideoEncoder.createInputSurface();
            this.mVideoEncoder.start();
            this.mMuxer = new MediaMuxer(this.mTargetFileName, 0);
            if (this.mbRecordAudio) {
                this.mAudioRecorder = new MediaRecorder();
                this.mAudioRecorder.setAudioSource(1);
                this.mAudioRecorder.setOutputFormat(2);
                this.mAudioRecorder.setAudioEncoder(3);
                this.mAudioRecorder.setAudioSamplingRate(44100);
                this.mAudioRecorder.setAudioChannels(2);
                this.mAudioRecorder.setAudioEncodingBitRate(320000);
                this.mAudioRecorder.setOutputFile(this.mTargetAudioFileName);
                this.mAudioRecorder.prepare();
                Log.d(TAG, "Local AudioRecorder prepared.");
            }
            Display display = ((WindowManager) getSystemService("window")).getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            this.mVirtualDisplay = this.mMediaProjection.createVirtualDisplay(getClass().getSimpleName(), this.mWidth, this.mHeight, metrics.densityDpi, 16, this.mInputSurface, null, null);
            if (this.mAudioRecorder != null) {
                this.mAudioRecorder.start();
                Log.d(TAG, "Local AudioRecorder started.");
            }
            drainEncoder();
            return true;
        } catch (IllegalStateException e) {
            if (!(mCallBack == null || mCallBack == null)) {
                szErrorMessage = e.getMessage();
                Utilities.runOnUIThread(mService, new Runnable() {
                    public void run() {
                        ScreenRecorderService2.mCallBack.OnRecordError(ScreenRecorderService2.this.mTargetFileName, szErrorMessage);
                    }
                });
            }
            this.mAudioRecorder = null;
            e.printStackTrace();
            return false;
        } catch (IOException e2) {
            if (mCallBack != null) {
                szErrorMessage = e2.getMessage();
                Utilities.runOnUIThread(mService, new Runnable() {
                    public void run() {
                        ScreenRecorderService2.mCallBack.OnRecordError(ScreenRecorderService2.this.mTargetFileName, szErrorMessage);
                    }
                });
            }
            this.mAudioRecorder = null;
            e2.printStackTrace();
            return false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean drainEncoder() {
        this.mDrainHandler.removeCallbacks(this.mDrainEncoderRunnable);
        while (true) {
            int bufferIndex = this.mVideoEncoder.dequeueOutputBuffer(this.mVideoBufferInfo, 0);
            if (bufferIndex == -1) {
                break;
            } else if (bufferIndex == -2) {
                if (this.mTrackIndex >= 0) {
                    throw new RuntimeException("format changed twice");
                }
                this.mTrackIndex = this.mMuxer.addTrack(this.mVideoEncoder.getOutputFormat());
                if (this.mbRecordAudio) {
                    MediaFormat audioformat = MediaFormat.createAudioFormat(AUDIO_MIME_TYPE, 44100, 2);
                    ByteBuffer CSDBuffer = ByteBuffer.allocate(2);
                    CSDBuffer.put(0, (byte) 18);
                    CSDBuffer.put(1, (byte) 16);
                    audioformat.setByteBuffer("csd-0", CSDBuffer);
                    this.mAudioTrackIndex = this.mMuxer.addTrack(audioformat);
                    Log.d(TAG, "Audio track id=" + this.mAudioTrackIndex);
                }
                if (!this.mMuxerStarted && this.mTrackIndex >= 0) {
                    Log.d(TAG, "Mutex started. VideoTrack=" + this.mTrackIndex);
                    this.mMuxer.start();
                    this.mMuxerStarted = true;
                }
            } else if (bufferIndex >= 0) {
                ByteBuffer encodedData = this.mVideoEncoder.getOutputBuffer(bufferIndex);
                if (encodedData == null) {
                    throw new RuntimeException("couldn't fetch buffer at index " + bufferIndex);
                }
                if ((this.mVideoBufferInfo.flags & 2) != 0) {
                    this.mVideoBufferInfo.size = 0;
                }
                if (this.mVideoBufferInfo.size != 0 && this.mMuxerStarted) {
                    encodedData.position(this.mVideoBufferInfo.offset);
                    encodedData.limit(this.mVideoBufferInfo.offset + this.mVideoBufferInfo.size);
                    if (this.mMinStartTime == 0) {
                        this.mMinStartTime = this.mVideoBufferInfo.presentationTimeUs;
                    }
                    this.mMuxer.writeSampleData(this.mTrackIndex, encodedData, this.mVideoBufferInfo);
                }
                this.mVideoEncoder.releaseOutputBuffer(bufferIndex, false);
                if ((this.mVideoBufferInfo.flags & 4) != 0) {
                    break;
                }
            } else {
                continue;
            }
        }
        this.mDrainHandler.postDelayed(this.mDrainEncoderRunnable, 10);
        return false;
    }

    private int getAudioTrackIndex(MediaExtractor paramMediaExtractor) {
        for (int i = 0; i < paramMediaExtractor.getTrackCount(); i++) {
            if (paramMediaExtractor.getTrackFormat(i).getString(io.vov.vitamio.MediaFormat.KEY_MIME).startsWith("audio")) {
                return i;
            }
        }
        return -1;
    }

    protected void endRecord() {
        boolean bError = false;
        this.mDrainHandler.removeCallbacks(this.mDrainEncoderRunnable);
        if (this.mAudioRecorder != null) {
            this.mAudioRecorder.stop();
            this.mAudioRecorder.release();
            this.mAudioRecorder = null;
        }
        try {
            if (new File(this.mTargetAudioFileName).exists()) {
                MediaExtractor audioExtractor = new MediaExtractor();
                audioExtractor.setDataSource(this.mTargetAudioFileName);
                audioExtractor.selectTrack(getAudioTrackIndex(audioExtractor));
                BufferInfo localBufferInfo = new BufferInfo();
                ByteBuffer localByteBuffer = ByteBuffer.allocate(44100);
                localByteBuffer.rewind();
                do {
                    int i = audioExtractor.readSampleData(localByteBuffer, 0);
                    if (i < 0) {
                        break;
                    }
                    localBufferInfo.presentationTimeUs = audioExtractor.getSampleTime() + this.mMinStartTime;
                    localBufferInfo.flags = audioExtractor.getSampleFlags();
                    localBufferInfo.offset = 0;
                    localBufferInfo.size = i;
                    this.mMuxer.writeSampleData(this.mAudioTrackIndex, localByteBuffer, localBufferInfo);
                } while (audioExtractor.advance());
            }
        } catch (IOException e) {
            e.printStackTrace();
            bError = true;
        }
        if (this.mMuxer != null) {
            if (this.mMuxerStarted) {
                this.mMuxer.stop();
            }
            this.mMuxer.release();
            this.mMuxer = null;
            this.mMuxerStarted = false;
        }
        if (this.mVideoEncoder != null) {
            this.mVideoEncoder.stop();
            this.mVideoEncoder.release();
            this.mVideoEncoder = null;
        }
        if (this.mVirtualDisplay != null) {
            this.mVirtualDisplay.release();
            this.mVirtualDisplay = null;
        }
        if (this.mMediaProjection != null) {
            this.mMediaProjection.stop();
        }
        if (!bError) {
            Log.d(TAG, "record complete. FileName = " + this.mTargetFileName);
        }
        if (bError) {
            if (mCallBack != null) {
                Utilities.runOnUIThread(mService, new Runnable() {
                    public void run() {
                        ScreenRecorderService2.mCallBack.OnRecordError(ScreenRecorderService2.this.mTargetFileName, "音视频混合出现错误");
                    }
                });
            }
        } else if (mCallBack != null) {
            Utilities.runOnUIThread(mService, new Runnable() {
                public void run() {
                    ScreenRecorderService2.mCallBack.OnRecordStop(ScreenRecorderService2.this.mTargetFileName, ScreenRecorderService2.this.getActivity());
                }
            });
        }
        mService.stopSelf();
    }
}
