package com.netspace.library.service;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.VirtualDisplay;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
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
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidParameterException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.sqlcipher.database.SQLiteDatabase;

@TargetApi(21)
public class ScreenRecorderService3 extends Service {
    private static final String AUDIO_MIME_TYPE = "audio/mp4a-latm";
    private static final int BIT_RATE = 64000;
    private static final boolean DEBUG = false;
    public static final int FRAMES_PER_BUFFER = 25;
    public static final int SAMPLES_PER_FRAME = 2048;
    private static final int SAMPLE_RATE = 44100;
    private static final String TAG = "ScreenRecorderService3";
    protected static final int TIMEOUT_USEC = 10000;
    private static final String VIDEO_MIME_TYPE = "video/avc";
    private static OnRecorderServiceListener mCallBack = null;
    private static ScreenRecorderService3 mService;
    private static boolean mServiceStarted = false;
    private final Runnable UpdateTimerRunnable = new Runnable() {
        public void run() {
            if (ScreenRecorderService3.this.mTextViewTimer != null) {
                if (!ScreenRecorderService3.this.mPauseRecord) {
                    long diffInSec = TimeUnit.MILLISECONDS.toSeconds((new Date().getTime() - ScreenRecorderService3.this.mRecordStartTimeForCountDisplay.getTime()) + ScreenRecorderService3.this.mnAlreadyRecordedTime);
                    int seconds = (int) (diffInSec % 60);
                    diffInSec /= 60;
                    int minutes = (int) (diffInSec % 60);
                    diffInSec /= 60;
                    int hours = (int) (diffInSec % 24);
                    diffInSec /= 24;
                    final String szResult = String.format("正在录制   %02d:%02d:%02d", new Object[]{Integer.valueOf(hours), Integer.valueOf(minutes), Integer.valueOf(seconds)});
                    final String szResult2 = String.format("%02d:%02d:%02d", new Object[]{Integer.valueOf(hours), Integer.valueOf(minutes), Integer.valueOf(seconds)});
                    ScreenRecorderService3.this.mTextViewTimer.post(new Runnable() {
                        public void run() {
                            ScreenRecorderService3.this.mTextViewTimer.setText(szResult);
                        }
                    });
                    if (ScreenRecorderService3.this.mTextViewTimeOnly != null) {
                        ScreenRecorderService3.this.mTextViewTimeOnly.post(new Runnable() {
                            public void run() {
                                ScreenRecorderService3.this.mTextViewTimeOnly.setText(szResult2);
                            }
                        });
                    }
                }
                ScreenRecorderService3.this.mUIHandler.postDelayed(ScreenRecorderService3.this.UpdateTimerRunnable, 500);
            }
        }
    };
    private BufferInfo mAudioBufferInfo;
    private MediaCodec mAudioEncoder;
    private MediaRecorder mAudioRecorder = null;
    private AudioThread mAudioThread;
    private int mAudioTrackIndex = -1;
    private int mBitRate = 0;
    private Runnable mDrainEncoderRunnable = new Runnable() {
        public void run() {
            ScreenRecorderService3.this.drainEncoder();
        }
    };
    private final Handler mDrainHandler = new Handler(Looper.getMainLooper());
    private int mFPS = 5;
    private int mHeight = -1;
    private Surface mInputSurface;
    private long mLastAudioTimestampus = 0;
    private MediaProjection mMediaProjection;
    private long mMinStartTime = 0;
    private MediaMuxer mMuxer;
    private boolean mMuxerStarted = false;
    private boolean mPauseRecord = false;
    private Date mRecordStartTimeForCountDisplay;
    private long mStartSystemTime = 0;
    private String mTargetAudioFileName = "";
    private String mTargetFileName = "";
    private String mTargetFolder = "";
    private TextView mTextViewTimeOnly;
    private TextView mTextViewTimer;
    private int mTrackIndex = -1;
    private Handler mUIHandler = null;
    private ServiceUIHelper mUIHelper;
    private BufferInfo mVideoBufferInfo;
    private MediaCodec mVideoEncoder;
    private VirtualDisplay mVirtualDisplay;
    private int mWidth = -1;
    private boolean mbRecordAudio = true;
    private boolean mbStopRecord = false;
    private long mnAlreadyRecordedTime = 0;
    private long mnAudioTimeAtPause = 0;
    private long mnAudioTimeOffset = 0;
    private long mnTimeAtPause = 0;
    private long mnTimeOffset = 0;

    private class AudioThread extends Thread {
        private AudioThread() {
        }

        public void run() {
            AudioRecord audioRecord;
            ByteBuffer buf;
            int i;
            long presentationTimeUs;
            int length;
            ByteBuffer[] inputBuffers;
            int inputBufferIndex;
            setName("AudioThread");
            Log.d(ScreenRecorderService3.TAG, "AudioThread started.");
            float gain = (float) Utilities.getIntSettings("AudioGain", 1);
            Process.setThreadPriority(-19);
            try {
                int readBytes;
                ByteBuffer inputBuffer;
                int min_buffer_size = AudioRecord.getMinBufferSize(ScreenRecorderService3.SAMPLE_RATE, 12, 2);
                int buffer_size = 51200;
                if (51200 < min_buffer_size) {
                    buffer_size = (((min_buffer_size / 2048) + 1) * 2048) * 2;
                }
                try {
                    audioRecord = new AudioRecord(1, ScreenRecorderService3.SAMPLE_RATE, 12, 2, buffer_size);
                    try {
                        if (audioRecord.getState() != 1) {
                            audioRecord = null;
                        }
                    } catch (Exception e) {
                        audioRecord = null;
                        if (audioRecord != null) {
                            try {
                                buf = ByteBuffer.allocateDirect(2048);
                                audioRecord.startRecording();
                                while (!ScreenRecorderService3.this.mbStopRecord) {
                                    buf.clear();
                                    readBytes = audioRecord.read(buf, 2048);
                                    if (readBytes <= 0) {
                                        buf.position(readBytes);
                                        buf.flip();
                                        if (((double) gain) != 1.0d) {
                                            buf.order(ByteOrder.LITTLE_ENDIAN);
                                            for (i = 0; i < readBytes - 2; i += 2) {
                                                buf.putShort(i, (short) ((int) (((float) buf.getShort(i)) * gain)));
                                            }
                                            buf.order(ByteOrder.BIG_ENDIAN);
                                        }
                                        presentationTimeUs = ((System.nanoTime() / 1000) - ScreenRecorderService3.this.mStartSystemTime) + ScreenRecorderService3.this.mMinStartTime;
                                        length = readBytes;
                                        inputBuffers = ScreenRecorderService3.this.mAudioEncoder.getInputBuffers();
                                        while (!ScreenRecorderService3.this.mbStopRecord) {
                                            inputBufferIndex = ScreenRecorderService3.this.mAudioEncoder.dequeueInputBuffer(10000);
                                            if (inputBufferIndex >= 0) {
                                                inputBuffer = inputBuffers[inputBufferIndex];
                                                inputBuffer.clear();
                                                if (buf != null) {
                                                    inputBuffer.put(buf);
                                                }
                                                if (length <= 0) {
                                                    ScreenRecorderService3.this.mAudioEncoder.queueInputBuffer(inputBufferIndex, 0, 0, presentationTimeUs, 4);
                                                } else {
                                                    ScreenRecorderService3.this.mAudioEncoder.queueInputBuffer(inputBufferIndex, 0, length, presentationTimeUs, 0);
                                                }
                                            }
                                        }
                                    }
                                }
                                audioRecord.stop();
                                audioRecord.release();
                                return;
                            } catch (Throwable th) {
                                audioRecord.release();
                            }
                        } else {
                            Log.e(ScreenRecorderService3.TAG, "failed to initialize AudioRecord");
                        }
                    }
                } catch (Exception e2) {
                    audioRecord = null;
                    audioRecord = null;
                    if (audioRecord != null) {
                        Log.e(ScreenRecorderService3.TAG, "failed to initialize AudioRecord");
                    }
                    buf = ByteBuffer.allocateDirect(2048);
                    audioRecord.startRecording();
                    while (!ScreenRecorderService3.this.mbStopRecord) {
                        buf.clear();
                        readBytes = audioRecord.read(buf, 2048);
                        if (readBytes <= 0) {
                            buf.position(readBytes);
                            buf.flip();
                            if (((double) gain) != 1.0d) {
                                buf.order(ByteOrder.LITTLE_ENDIAN);
                                for (i = 0; i < readBytes - 2; i += 2) {
                                    buf.putShort(i, (short) ((int) (((float) buf.getShort(i)) * gain)));
                                }
                                buf.order(ByteOrder.BIG_ENDIAN);
                            }
                            presentationTimeUs = ((System.nanoTime() / 1000) - ScreenRecorderService3.this.mStartSystemTime) + ScreenRecorderService3.this.mMinStartTime;
                            length = readBytes;
                            inputBuffers = ScreenRecorderService3.this.mAudioEncoder.getInputBuffers();
                            while (!ScreenRecorderService3.this.mbStopRecord) {
                                inputBufferIndex = ScreenRecorderService3.this.mAudioEncoder.dequeueInputBuffer(10000);
                                if (inputBufferIndex >= 0) {
                                    inputBuffer = inputBuffers[inputBufferIndex];
                                    inputBuffer.clear();
                                    if (buf != null) {
                                        inputBuffer.put(buf);
                                    }
                                    if (length <= 0) {
                                        ScreenRecorderService3.this.mAudioEncoder.queueInputBuffer(inputBufferIndex, 0, length, presentationTimeUs, 0);
                                    } else {
                                        ScreenRecorderService3.this.mAudioEncoder.queueInputBuffer(inputBufferIndex, 0, 0, presentationTimeUs, 4);
                                    }
                                }
                            }
                        }
                    }
                    audioRecord.stop();
                    audioRecord.release();
                    return;
                }
                if (audioRecord != null) {
                    buf = ByteBuffer.allocateDirect(2048);
                    audioRecord.startRecording();
                    while (!ScreenRecorderService3.this.mbStopRecord) {
                        buf.clear();
                        readBytes = audioRecord.read(buf, 2048);
                        if (readBytes <= 0) {
                            buf.position(readBytes);
                            buf.flip();
                            if (((double) gain) != 1.0d) {
                                buf.order(ByteOrder.LITTLE_ENDIAN);
                                for (i = 0; i < readBytes - 2; i += 2) {
                                    buf.putShort(i, (short) ((int) (((float) buf.getShort(i)) * gain)));
                                }
                                buf.order(ByteOrder.BIG_ENDIAN);
                            }
                            presentationTimeUs = ((System.nanoTime() / 1000) - ScreenRecorderService3.this.mStartSystemTime) + ScreenRecorderService3.this.mMinStartTime;
                            length = readBytes;
                            inputBuffers = ScreenRecorderService3.this.mAudioEncoder.getInputBuffers();
                            while (!ScreenRecorderService3.this.mbStopRecord) {
                                inputBufferIndex = ScreenRecorderService3.this.mAudioEncoder.dequeueInputBuffer(10000);
                                if (inputBufferIndex >= 0) {
                                    inputBuffer = inputBuffers[inputBufferIndex];
                                    inputBuffer.clear();
                                    if (buf != null) {
                                        inputBuffer.put(buf);
                                    }
                                    if (length <= 0) {
                                        ScreenRecorderService3.this.mAudioEncoder.queueInputBuffer(inputBufferIndex, 0, 0, presentationTimeUs, 4);
                                    } else {
                                        ScreenRecorderService3.this.mAudioEncoder.queueInputBuffer(inputBufferIndex, 0, length, presentationTimeUs, 0);
                                    }
                                }
                            }
                        }
                    }
                    audioRecord.stop();
                    audioRecord.release();
                    return;
                }
                Log.e(ScreenRecorderService3.TAG, "failed to initialize AudioRecord");
            } catch (Throwable e3) {
                Log.e(ScreenRecorderService3.TAG, "AudioThread#run", e3);
            }
        }
    }

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

    public static ScreenRecorderService3 getService() {
        if (mService == null) {
            mService = new ScreenRecorderService3();
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
        this.mTextViewTimeOnly = (TextView) this.mUIHelper.getView().findViewById(R.id.textViewTimeOnly);
        this.mUIHelper.getView().findViewById(R.id.buttonStop).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ScreenRecorderService3.this.mUIHandler.removeCallbacks(ScreenRecorderService3.this.UpdateTimerRunnable);
                ScreenRecorderService3.stopRecorder();
                v.setEnabled(false);
                v.setVisibility(4);
                ScreenRecorderService3.this.mUIHelper.getView().findViewById(R.id.buttonPause).setVisibility(4);
                ScreenRecorderService3.this.mTextViewTimer.setText("正在保存数据...");
            }
        });
        this.mUIHelper.getView().findViewById(R.id.buttonPause).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (ScreenRecorderService3.this.mPauseRecord) {
                    ScreenRecorderService3.this.mPauseRecord = false;
                    ScreenRecorderService3.this.mTextViewTimer.setText("录制已恢复");
                    ScreenRecorderService3.this.mRecordStartTimeForCountDisplay = new Date();
                    return;
                }
                ScreenRecorderService3.this.mPauseRecord = true;
                ScreenRecorderService3.this.mTextViewTimer.setText("录制已暂停");
                long nTimeDiff = new Date().getTime() - ScreenRecorderService3.this.mRecordStartTimeForCountDisplay.getTime();
                ScreenRecorderService3 screenRecorderService3 = ScreenRecorderService3.this;
                screenRecorderService3.mnAlreadyRecordedTime = screenRecorderService3.mnAlreadyRecordedTime + nTimeDiff;
            }
        });
        this.mRecordStartTimeForCountDisplay = new Date();
        if (this.mUIHandler == null) {
            this.mUIHandler = new Handler(Looper.getMainLooper());
        }
        this.mUIHandler.postDelayed(this.UpdateTimerRunnable, 500);
        startRecord();
    }

    public boolean startRecord() {
        final String message;
        if (this.mAudioRecorder != null) {
            Log.e(TAG, "Already called startRecord, Second call ignored.");
            return true;
        }
        this.mLastAudioTimestampus = 0;
        this.mnAlreadyRecordedTime = 0;
        this.mnAudioTimeAtPause = 0;
        this.mnAudioTimeOffset = 0;
        this.mnTimeOffset = 0;
        this.mnTimeAtPause = 0;
        this.mPauseRecord = false;
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
            format.setInteger(io.vov.vitamio.MediaFormat.KEY_I_FRAME_INTERVAL, 1);
            this.mVideoEncoder = MediaCodec.createEncoderByType(VIDEO_MIME_TYPE);
            this.mVideoEncoder.configure(format, null, null, 1);
            this.mInputSurface = this.mVideoEncoder.createInputSurface();
            this.mVideoEncoder.start();
            this.mMuxer = new MediaMuxer(this.mTargetFileName, 0);
            this.mAudioBufferInfo = new BufferInfo();
            MediaFormat audioFormat = MediaFormat.createAudioFormat(AUDIO_MIME_TYPE, SAMPLE_RATE, 2);
            audioFormat.setInteger(io.vov.vitamio.MediaFormat.KEY_AAC_PROFILE, 2);
            audioFormat.setInteger(io.vov.vitamio.MediaFormat.KEY_CHANNEL_MASK, 12);
            audioFormat.setInteger("bitrate", BIT_RATE);
            audioFormat.setInteger(io.vov.vitamio.MediaFormat.KEY_CHANNEL_COUNT, 2);
            this.mAudioEncoder = MediaCodec.createEncoderByType(AUDIO_MIME_TYPE);
            this.mAudioEncoder.configure(audioFormat, null, null, 1);
            this.mAudioEncoder.start();
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
                message = e.getMessage();
                Utilities.runOnUIThread(mService, new Runnable() {
                    public void run() {
                        ScreenRecorderService3.mCallBack.OnRecordError(ScreenRecorderService3.this.mTargetFileName, message);
                    }
                });
            }
            this.mAudioRecorder = null;
            e.printStackTrace();
            return false;
        } catch (IOException e2) {
            if (mCallBack != null) {
                message = e2.getMessage();
                Utilities.runOnUIThread(mService, new Runnable() {
                    public void run() {
                        ScreenRecorderService3.mCallBack.OnRecordError(ScreenRecorderService3.this.mTargetFileName, message);
                    }
                });
            }
            this.mAudioRecorder = null;
            e2.printStackTrace();
            return false;
        }
    }

    private boolean drainAudioEncoder() {
        while (true) {
            int bufferIndex = this.mAudioEncoder.dequeueOutputBuffer(this.mAudioBufferInfo, 0);
            if (bufferIndex == -1) {
                break;
            } else if (bufferIndex != -2 && bufferIndex >= 0) {
                ByteBuffer encodedData = this.mAudioEncoder.getOutputBuffer(bufferIndex);
                if (encodedData == null) {
                    throw new RuntimeException("couldn't fetch buffer at index " + bufferIndex);
                }
                if ((this.mAudioBufferInfo.flags & 2) != 0) {
                    this.mAudioBufferInfo.size = 0;
                }
                if (this.mAudioBufferInfo.size != 0) {
                    if (this.mPauseRecord) {
                        if (this.mnAudioTimeAtPause == 0) {
                            this.mnAudioTimeAtPause = this.mAudioBufferInfo.presentationTimeUs;
                        }
                    } else if (this.mnAudioTimeAtPause != 0) {
                        this.mnAudioTimeOffset += this.mAudioBufferInfo.presentationTimeUs - this.mnAudioTimeAtPause;
                        this.mnAudioTimeAtPause = 0;
                    }
                    if (this.mMuxerStarted && !this.mPauseRecord) {
                        encodedData.position(this.mAudioBufferInfo.offset);
                        encodedData.limit(this.mAudioBufferInfo.offset + this.mAudioBufferInfo.size);
                        if (this.mnAudioTimeOffset != 0) {
                            Log.i(TAG, "Audio presentTime change from " + this.mAudioBufferInfo.presentationTimeUs);
                            BufferInfo bufferInfo = this.mAudioBufferInfo;
                            bufferInfo.presentationTimeUs -= this.mnAudioTimeOffset;
                            Log.i(TAG, "Audio presentTime change to " + this.mAudioBufferInfo.presentationTimeUs);
                        }
                        if (this.mLastAudioTimestampus == 0) {
                            this.mLastAudioTimestampus = this.mAudioBufferInfo.presentationTimeUs;
                            this.mMuxer.writeSampleData(this.mAudioTrackIndex, encodedData, this.mAudioBufferInfo);
                        } else {
                            if (this.mAudioBufferInfo.presentationTimeUs < this.mLastAudioTimestampus) {
                                Log.e(TAG, "mLastAudioTimestampus > mAudioBufferInfo.presentationTimeUs !!!");
                                Log.e(TAG, "mLastAudioTimestampus is " + this.mLastAudioTimestampus);
                                Log.e(TAG, "mAudioBufferInfo.presentationTimeUs is " + this.mAudioBufferInfo.presentationTimeUs);
                            }
                            if (this.mAudioBufferInfo.presentationTimeUs - this.mLastAudioTimestampus > 9) {
                                this.mMuxer.writeSampleData(this.mAudioTrackIndex, encodedData, this.mAudioBufferInfo);
                                this.mLastAudioTimestampus = this.mAudioBufferInfo.presentationTimeUs;
                            } else {
                                Log.e(TAG, "Ignore audio info. Because presentationTimeUs < 11us.");
                            }
                        }
                    }
                }
                this.mAudioEncoder.releaseOutputBuffer(bufferIndex, false);
                if ((this.mAudioBufferInfo.flags & 4) != 0) {
                    break;
                }
            }
        }
        Log.d(TAG, "AudioBuffer end of stream.");
        return false;
    }

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
                    MediaFormat audioformat = MediaFormat.createAudioFormat(AUDIO_MIME_TYPE, SAMPLE_RATE, 2);
                    ByteBuffer CSDBuffer = ByteBuffer.allocate(2);
                    CSDBuffer.put(0, (byte) 18);
                    CSDBuffer.put(1, (byte) 16);
                    audioformat.setByteBuffer("csd-0", CSDBuffer);
                    this.mAudioTrackIndex = this.mMuxer.addTrack(audioformat);
                    Log.d(TAG, "Audio track id=" + this.mAudioTrackIndex);
                    if (this.mAudioThread == null) {
                        this.mAudioThread = new AudioThread();
                        this.mAudioThread.start();
                    }
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
                    if (this.mStartSystemTime == 0) {
                        this.mStartSystemTime = System.nanoTime() / 1000;
                    }
                    if (this.mPauseRecord) {
                        if (this.mnTimeAtPause == 0) {
                            this.mnTimeAtPause = this.mVideoBufferInfo.presentationTimeUs;
                            Log.d(TAG, "Time at pause: " + this.mnTimeAtPause);
                        }
                    } else if (this.mnTimeAtPause != 0) {
                        this.mnTimeOffset += this.mVideoBufferInfo.presentationTimeUs - this.mnTimeAtPause;
                        this.mnTimeAtPause = 0;
                        Log.d(TAG, "Time offset during pause: " + this.mnTimeOffset);
                    }
                    if (!this.mPauseRecord) {
                        if (this.mnTimeOffset != 0) {
                            Log.i(TAG, "Video presentTime change from " + this.mVideoBufferInfo.presentationTimeUs);
                            BufferInfo bufferInfo = this.mVideoBufferInfo;
                            bufferInfo.presentationTimeUs -= this.mnTimeOffset;
                            Log.i(TAG, "Video presentTime change to " + this.mVideoBufferInfo.presentationTimeUs);
                        }
                        this.mMuxer.writeSampleData(this.mTrackIndex, encodedData, this.mVideoBufferInfo);
                    }
                }
                this.mVideoEncoder.releaseOutputBuffer(bufferIndex, false);
                if ((this.mVideoBufferInfo.flags & 4) != 0) {
                    break;
                }
            } else {
                continue;
            }
        }
        Log.d(TAG, "VideoBuffer end of stream.");
        drainAudioEncoder();
        this.mDrainHandler.postDelayed(this.mDrainEncoderRunnable, 10);
        return false;
    }

    protected void endRecord() {
        this.mbStopRecord = true;
        try {
            this.mAudioThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.mAudioThread = null;
        this.mDrainHandler.removeCallbacks(this.mDrainEncoderRunnable);
        if (this.mAudioRecorder != null) {
            this.mAudioRecorder.stop();
            this.mAudioRecorder.release();
            this.mAudioRecorder = null;
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
        if (null == null) {
            Log.d(TAG, "record complete. FileName = " + this.mTargetFileName);
        }
        if (null == null) {
            if (mCallBack != null) {
                Utilities.runOnUIThread(mService, new Runnable() {
                    public void run() {
                        ScreenRecorderService3.mCallBack.OnRecordStop(ScreenRecorderService3.this.mTargetFileName, ScreenRecorderService3.this.getActivity());
                    }
                });
            }
        } else if (mCallBack != null) {
            Utilities.runOnUIThread(mService, new Runnable() {
                public void run() {
                    ScreenRecorderService3.mCallBack.OnRecordError(ScreenRecorderService3.this.mTargetFileName, "音视频混合出现错误");
                }
            });
        }
        mService.stopSelf();
    }
}
