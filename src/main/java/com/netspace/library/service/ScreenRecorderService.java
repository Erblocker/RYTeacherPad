package com.netspace.library.service;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import com.eclipsesource.v8.Platform;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.ServiceUIHelper;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ScreenRecorderService extends Service {
    private static final String MIME_TYPE = "video/avc";
    private static final String TAG = "ScreenRecorderService";
    private static final boolean VERBOSE = true;
    private static OnRecorderServiceListener mCallBack = null;
    private static Activity mCurrentActivity = null;
    private static ScreenRecorderService mService;
    private static boolean mServiceStarted = false;
    private static boolean mbAllowRecord = VERBOSE;
    private static boolean mbLastFrame = false;
    private final Runnable GetScreenCopyRunnable = new Runnable() {
        public void run() {
            long nNextTime = (long) (1000 / ScreenRecorderService.this.mFPS);
            long nStartTime = System.currentTimeMillis();
            if (ScreenRecorderService.mServiceStarted) {
                if (ScreenRecorderService.mCurrentActivity == null) {
                    ScreenRecorderService.mCurrentActivity = ScreenRecorderService.getActivity();
                }
                if (ScreenRecorderService.mCurrentActivity == null) {
                    ScreenRecorderService.this.mUIHandler.postDelayed(ScreenRecorderService.this.GetScreenCopyRunnable, nNextTime);
                    return;
                }
                synchronized (ScreenRecorderService.mCurrentActivity) {
                    View v = ScreenRecorderService.mCurrentActivity.getWindow().getDecorView();
                    boolean bLastCacheEnabled = v.isDrawingCacheEnabled();
                    v.setDrawingCacheEnabled(ScreenRecorderService.VERBOSE);
                    Bitmap b = v.getDrawingCache();
                    if (b != null) {
                        Canvas Canvas = new Canvas();
                        Paint Paint = new Paint();
                        Paint.setColor(-16777216);
                        Canvas.setBitmap(b);
                        Canvas.drawRect(0.0f, 0.0f, (float) b.getWidth(), (float) ScreenRecorderService.this.mStatusBarHeight, Paint);
                        ScreenRecorderService.mService.processFrame(b, ScreenRecorderService.mbLastFrame);
                    }
                    v.setDrawingCacheEnabled(bLastCacheEnabled);
                }
                Log.d("ScreenCopy", "time cost " + String.valueOf(System.currentTimeMillis() - nStartTime));
                nNextTime -= System.currentTimeMillis() - nStartTime;
                if (!ScreenRecorderService.mbLastFrame) {
                    ScreenRecorderService.this.mUIHandler.postDelayed(ScreenRecorderService.this.GetScreenCopyRunnable, nNextTime);
                    return;
                }
                return;
            }
            ScreenRecorderService.this.mUIHandler.postDelayed(ScreenRecorderService.this.GetScreenCopyRunnable, nNextTime);
        }
    };
    private final int TIMEOUT_USEC = 10000;
    private final Runnable UpdateTimerRunnable = new Runnable() {
        public void run() {
            if (ScreenRecorderService.this.mTextViewTimer != null) {
                long diffInSec = TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - ScreenRecorderService.this.mRecordStartTime.getTime());
                int seconds = (int) (diffInSec % 60);
                diffInSec /= 60;
                int minutes = (int) (diffInSec % 60);
                diffInSec /= 60;
                int hours = (int) (diffInSec % 24);
                diffInSec /= 24;
                final String szResult = String.format("正在录制   %02d:%02d:%02d", new Object[]{Integer.valueOf(hours), Integer.valueOf(minutes), Integer.valueOf(seconds)});
                ScreenRecorderService.this.mTextViewTimer.post(new Runnable() {
                    public void run() {
                        ScreenRecorderService.this.mTextViewTimer.setText(szResult);
                    }
                });
                ScreenRecorderService.this.mUIHandler.postDelayed(ScreenRecorderService.this.UpdateTimerRunnable, 500);
            }
        }
    };
    private int[] mARGBData = null;
    private MediaRecorder mAudioRecorder = null;
    private int mBitRate = 6000000;
    private int mColorFormat = 0;
    private MediaCodec mEncoder = null;
    private ByteBuffer[] mEncoderInputBuffers = null;
    private ByteBuffer[] mEncoderOutputBuffers = null;
    private int mFPS = 5;
    private byte[] mFrameData = null;
    private Runnable mFrameProcessRunnable = new Runnable() {
        public void run() {
            if (ScreenRecorderService.this.mGenerateIndex == 0) {
                try {
                    ScreenRecorderService.this.mStartRecordTime = new Date();
                    if (!ScreenRecorderService.this.startRecord()) {
                        Toast.makeText(ScreenRecorderService.this, "startRecord出现错误", 0).show();
                        ScreenRecorderService.this.mWorkThreadHandler.removeCallbacks(this);
                        ScreenRecorderService.this.mGenerateIndex = 0;
                        ScreenRecorderService.this.mWorkThreadHandler.post(new Runnable() {
                            public void run() {
                                ScreenRecorderService.this.mWorkThreadHandler = null;
                                ScreenRecorderService.mbLastFrame = false;
                                Looper.myLooper().quit();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (ScreenRecorderService.mbLastFrame) {
                ScreenRecorderService.this.putFrame(ScreenRecorderService.VERBOSE);
                ScreenRecorderService.this.mWorkThreadHandler.removeCallbacks(this);
                ScreenRecorderService.this.mGenerateIndex = 0;
                ScreenRecorderService.this.mWorkThreadHandler.post(new Runnable() {
                    public void run() {
                        ScreenRecorderService.this.mWorkThreadHandler = null;
                        ScreenRecorderService.mbLastFrame = false;
                        Looper.myLooper().quit();
                    }
                });
                return;
            }
            ScreenRecorderService.this.putFrame(false);
        }
    };
    private int mGenerateIndex = 0;
    private FileOutputStream mH264OutputStream = null;
    private int mHeight = -1;
    private int mIFrameInterval = 10;
    private ProgressDialog mProgressDialog;
    private Date mRecordStartTime;
    private Date mStartRecordTime;
    private int mStatusBarHeight = 0;
    private String mTargetFileName = "";
    private String mTargetFolder = "";
    private TextView mTextViewTimer;
    private Handler mUIHandler = null;
    private ServiceUIHelper mUIHelper;
    private int mWidth = -1;
    private WorkThread mWorkThread = null;
    private Handler mWorkThreadHandler = null;
    private boolean mbRecordAudio = VERBOSE;

    private class MeasureAudioThread extends Thread {
        private String mAudioFileName;
        private String mFinalFileName;
        private double mGenerateIndex = 0.0d;
        private Date mStartRecordTime;
        private String mVideoFileName;
        private boolean mbRecordAudio = ScreenRecorderService.VERBOSE;

        public MeasureAudioThread(String szVideoFileName, String szAudioFileName, String szFinalFileName, Date startRecordTime, double nGenerateIndex) {
            this.mVideoFileName = szVideoFileName;
            this.mAudioFileName = szAudioFileName;
            this.mFinalFileName = szFinalFileName;
            this.mStartRecordTime = startRecordTime;
            this.mGenerateIndex = nGenerateIndex;
        }

        public void run() {
        }
    }

    public interface OnRecorderServiceListener {
        void OnFrameProcessed(int i);

        void OnRecordError(String str, String str2);

        void OnRecordStart(String str);

        void OnRecordStop(String str, Activity activity);
    }

    protected class WorkThread extends Thread {
        protected WorkThread() {
        }

        public void run() {
            setName("ScreenRecorderService WorkThread");
            Process.setThreadPriority(-19);
            super.run();
            Looper.prepare();
            ScreenRecorderService.this.mWorkThreadHandler = new Handler();
            ScreenRecorderService.this.mWorkThreadHandler.post(ScreenRecorderService.this.mFrameProcessRunnable);
            Looper.loop();
            ScreenRecorderService.mService.stopSelf();
            ScreenRecorderService.mCurrentActivity = null;
        }
    }

    public static Activity getActivity() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread", new Class[0]).invoke(null, new Object[0]);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(VERBOSE);
            for (Object activityRecord : ((Map) activitiesField.get(activityThread)).values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(VERBOSE);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(VERBOSE);
                    return (Activity) activityField.get(activityRecord);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void registerActivity(Activity Activity) {
        if (this.mUIHandler == null) {
            this.mUIHandler = new Handler(Looper.getMainLooper());
            this.mUIHandler.post(this.GetScreenCopyRunnable);
        }
        if (mCurrentActivity != null) {
            synchronized (mCurrentActivity) {
                mCurrentActivity = Activity;
            }
            return;
        }
        mCurrentActivity = Activity;
    }

    public void unregisterActivity(Activity Activity) {
        if (mCurrentActivity != null) {
            synchronized (mCurrentActivity) {
                if (mCurrentActivity.equals(Activity)) {
                    mCurrentActivity = null;
                }
            }
        }
    }

    public static void setCallBack(OnRecorderServiceListener CallBack) {
        mCallBack = CallBack;
    }

    public static void stopRecorder() {
        mbLastFrame = VERBOSE;
    }

    public static void setAllowRecord(boolean bAllow) {
        mbAllowRecord = bAllow;
    }

    public static boolean getAllowRecord() {
        return mbAllowRecord;
    }

    public void processFrame(Bitmap b, boolean bLastFrame) {
        boolean bFirstStart = false;
        if (mServiceStarted) {
            boolean bReallocCache = false;
            if (this.mWidth != b.getWidth()) {
                this.mWidth = b.getWidth();
                bReallocCache = VERBOSE;
            }
            if (this.mHeight != b.getHeight()) {
                this.mHeight = b.getHeight();
                bReallocCache = VERBOSE;
            }
            if (!(this.mWidth % 16 == 0 && this.mHeight % 16 == 0)) {
                Log.w(TAG, "WARNING: width or height not multiple of 16");
            }
            if (bReallocCache || this.mARGBData == null) {
                this.mARGBData = new int[(this.mWidth * this.mHeight)];
            }
            b.getPixels(this.mARGBData, 0, this.mWidth, 0, 0, this.mWidth, this.mHeight);
            if (this.mWorkThread == null) {
                bFirstStart = VERBOSE;
                this.mWorkThread = new WorkThread();
                this.mWorkThread.start();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mbLastFrame = bLastFrame;
            if (!bFirstStart && this.mWorkThreadHandler != null) {
                this.mWorkThreadHandler.removeCallbacks(this.mFrameProcessRunnable);
                this.mWorkThreadHandler.post(this.mFrameProcessRunnable);
            }
        }
    }

    public void onCreate() {
        super.onCreate();
        this.mStatusBarHeight = getStatusBarHeight();
        mService = this;
    }

    public static ScreenRecorderService getService() {
        if (mService == null) {
            mService = new ScreenRecorderService();
        }
        return mService;
    }

    public static boolean isActive() {
        return mServiceStarted;
    }

    public int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", Platform.ANDROID);
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (!(mServiceStarted || intent == null)) {
            if (getAllowRecord()) {
                this.mBitRate = intent.getIntExtra("bitrate", 6000000);
                this.mFPS = intent.getIntExtra("fps", 5);
                this.mIFrameInterval = intent.getIntExtra("iframeinterval", 10);
                this.mbRecordAudio = intent.getBooleanExtra("recordaudio", VERBOSE);
                this.mTargetFolder = intent.getStringExtra("folder");
                if (this.mTargetFolder == null || this.mTargetFolder.isEmpty()) {
                    throw new InvalidParameterException("folder must be set.");
                }
                mService = this;
                mServiceStarted = VERBOSE;
                this.mUIHelper = new ServiceUIHelper(this, R.layout.layout_screenrecorder, (Utilities.getScreenWidth((Context) this) - 100) / 2, 0);
                this.mUIHelper.setMove(VERBOSE, false);
                this.mUIHelper.showWindow();
                this.mTextViewTimer = (TextView) this.mUIHelper.getView().findViewById(R.id.textViewStatus);
                this.mUIHelper.getView().findViewById(R.id.buttonPause).setVisibility(8);
                this.mUIHelper.getView().findViewById(R.id.buttonStop).setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        ScreenRecorderService.this.mUIHandler.removeCallbacks(ScreenRecorderService.this.UpdateTimerRunnable);
                        ScreenRecorderService.stopRecorder();
                        v.setEnabled(false);
                        v.setVisibility(4);
                        ScreenRecorderService.this.mTextViewTimer.setText("正在保存数据...");
                    }
                });
                this.mRecordStartTime = new Date();
                if (this.mUIHandler == null) {
                    this.mUIHandler = new Handler(Looper.getMainLooper());
                    this.mUIHandler.post(this.GetScreenCopyRunnable);
                }
                this.mUIHandler.postDelayed(this.UpdateTimerRunnable, 500);
                if (UI.getCurrentActivity() != null) {
                    registerActivity(UI.getCurrentActivity());
                }
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

    public static PendingIntent getScreenRecordIntent() {
        File Folder = new File(new StringBuilder(String.valueOf(MyiBaseApplication.getBaseAppContext().getExternalCacheDir().getAbsolutePath())).append("/upload").toString());
        Context context = MyiBaseApplication.getBaseAppContext();
        Folder.mkdir();
        if (VERSION.SDK_INT >= 21) {
            Intent RecordService = new Intent(context, ScreenRecorderService3.class);
            RecordService.putExtra("fps", 25);
            RecordService.putExtra("folder", Folder.getAbsolutePath() + "/");
            return PendingIntent.getService(context, 0, RecordService, 134217728);
        }
        RecordService = new Intent(context, ScreenRecorderService.class);
        RecordService.putExtra("fps", 5);
        RecordService.putExtra("folder", Folder.getAbsolutePath() + "/");
        return PendingIntent.getService(context, 0, RecordService, 134217728);
    }

    public boolean startRecord() {
        final String szErrorMessage;
        if (this.mAudioRecorder != null) {
            Log.e(TAG, "Already called startRecord, Second call ignored.");
            return VERBOSE;
        }
        this.mTargetFileName = this.mTargetFolder + Utilities.createGUID() + ".mp4";
        if (this.mbRecordAudio) {
            this.mAudioRecorder = new MediaRecorder();
            this.mAudioRecorder.setAudioSource(0);
            this.mAudioRecorder.setOutputFormat(6);
            this.mAudioRecorder.setAudioEncoder(3);
            this.mAudioRecorder.setAudioSamplingRate(44100);
            this.mAudioRecorder.setAudioEncodingBitRate(320000);
            this.mAudioRecorder.setOutputFile(this.mTargetFileName + ".aac");
            try {
                this.mAudioRecorder.prepare();
            } catch (IllegalStateException e) {
                if (!(mCallBack == null || mCallBack == null)) {
                    szErrorMessage = e.getMessage();
                    Utilities.runOnUIThread(mService, new Runnable() {
                        public void run() {
                            ScreenRecorderService.mCallBack.OnRecordError(ScreenRecorderService.this.mTargetFileName, szErrorMessage);
                        }
                    });
                }
                this.mAudioRecorder = null;
                e.printStackTrace();
            } catch (IOException e2) {
                if (mCallBack != null) {
                    szErrorMessage = e2.getMessage();
                    Utilities.runOnUIThread(mService, new Runnable() {
                        public void run() {
                            ScreenRecorderService.mCallBack.OnRecordError(ScreenRecorderService.this.mTargetFileName, szErrorMessage);
                        }
                    });
                }
                this.mAudioRecorder = null;
                e2.printStackTrace();
            }
            if (this.mAudioRecorder == null) {
                return false;
            }
            this.mAudioRecorder.start();
        }
        return prepareEncoder();
    }

    private boolean prepareEncoder() {
        MediaCodecInfo codecInfo = selectCodec(MIME_TYPE);
        if (codecInfo == null) {
            Log.e(TAG, "Unable to find an appropriate codec for video/avc");
            if (mCallBack == null) {
                return false;
            }
            mCallBack.OnRecordError(this.mTargetFileName, "Unable to find an appropriate codec for video/avc");
            return false;
        }
        Log.d(TAG, "found codec: " + codecInfo.getName());
        this.mColorFormat = selectColorFormat(codecInfo, MIME_TYPE);
        if (this.mColorFormat == 0) {
            Log.e(TAG, "Unable to find an appropriate colorformat for video/avc");
            if (mCallBack == null) {
                return false;
            }
            mCallBack.OnRecordError(this.mTargetFileName, "Unable to find an appropriate colorformat for video/avc");
            return false;
        }
        Log.d(TAG, "found colorFormat: " + this.mColorFormat);
        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, this.mWidth, this.mHeight);
        format.setInteger(io.vov.vitamio.MediaFormat.KEY_COLOR_FORMAT, this.mColorFormat);
        format.setInteger("bitrate", this.mBitRate);
        format.setInteger(io.vov.vitamio.MediaFormat.KEY_FRAME_RATE, this.mFPS);
        format.setInteger(io.vov.vitamio.MediaFormat.KEY_I_FRAME_INTERVAL, this.mIFrameInterval);
        Log.d(TAG, "format: " + format);
        try {
            this.mEncoder = MediaCodec.createByCodecName(codecInfo.getName());
            this.mEncoder.configure(format, null, null, 1);
            this.mEncoder.start();
            this.mEncoderInputBuffers = this.mEncoder.getInputBuffers();
            this.mEncoderOutputBuffers = this.mEncoder.getOutputBuffers();
            this.mFrameData = new byte[(((this.mWidth * this.mHeight) * 3) / 2)];
            String fileName = this.mTargetFileName + ".h264";
            try {
                this.mH264OutputStream = new FileOutputStream(fileName);
                if (mCallBack != null) {
                    mCallBack.OnRecordStart(this.mTargetFileName);
                }
                return VERBOSE;
            } catch (IOException ioe) {
                Log.w(TAG, "Unable to create debug output file " + fileName);
                if (mCallBack != null) {
                    mCallBack.OnRecordError(this.mTargetFileName, ioe.getMessage());
                }
                throw new RuntimeException(ioe);
            }
        } catch (IOException e) {
            Log.e(TAG, "MediaCodec.createByCodecName failed.");
            e.printStackTrace();
            if (mCallBack == null) {
                return false;
            }
            mCallBack.OnRecordError(this.mTargetFileName, e.getMessage());
            return false;
        }
    }

    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (codecInfo.isEncoder()) {
                String[] types = codecInfo.getSupportedTypes();
                for (String equalsIgnoreCase : types) {
                    if (equalsIgnoreCase.equalsIgnoreCase(mimeType)) {
                        return codecInfo;
                    }
                }
                continue;
            }
        }
        return null;
    }

    private static int selectColorFormat(MediaCodecInfo codecInfo, String mimeType) {
        CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
        for (int colorFormat : capabilities.colorFormats) {
            if (colorFormat == 19) {
                return colorFormat;
            }
        }
        for (int colorFormat2 : capabilities.colorFormats) {
            if (isRecognizedFormat(colorFormat2)) {
                return colorFormat2;
            }
        }
        Log.d(TAG, "couldn't find a good color format for " + codecInfo.getName() + " / " + mimeType);
        return 0;
    }

    private static boolean isRecognizedFormat(int colorFormat) {
        switch (colorFormat) {
            case 19:
            case 21:
                return VERBOSE;
            default:
                return false;
        }
    }

    protected void putFrame(boolean bLastFrame) {
        BufferInfo info = new BufferInfo();
        Date currentDate = new Date();
        try {
            int inputBufIndex = this.mEncoder.dequeueInputBuffer(10000);
            Log.d(TAG, "inputBufIndex=" + inputBufIndex);
            if (inputBufIndex >= 0) {
                long ptsUsec = (currentDate.getTime() - this.mStartRecordTime.getTime()) * 1000;
                Log.d(TAG, "ptsUsec=" + ptsUsec);
                if (bLastFrame) {
                    this.mEncoder.queueInputBuffer(inputBufIndex, 0, 0, ptsUsec, 4);
                    Log.d(TAG, "Last frame. sent input EOS (with zero-length frame)");
                } else {
                    if (this.mColorFormat == 19) {
                        encodeYUV420Planar(this.mFrameData, this.mARGBData, this.mWidth, this.mHeight);
                    } else if (this.mColorFormat == 21) {
                        encodeYUV420SP(this.mFrameData, this.mARGBData, this.mWidth, this.mHeight);
                    }
                    ByteBuffer inputBuf = this.mEncoderInputBuffers[inputBufIndex];
                    inputBuf.clear();
                    inputBuf.put(this.mFrameData);
                    this.mEncoder.queueInputBuffer(inputBufIndex, 0, this.mFrameData.length, ptsUsec, 0);
                    Log.d(TAG, "submitted frame " + this.mGenerateIndex + " to enc");
                }
                this.mGenerateIndex++;
                if (mCallBack != null) {
                    mCallBack.OnFrameProcessed(this.mGenerateIndex);
                }
            } else {
                Log.d(TAG, "input buffer not available. Dropping frame.");
            }
            try {
                int encoderStatus = this.mEncoder.dequeueOutputBuffer(info, 10000);
                if (encoderStatus == -1) {
                    Log.d(TAG, "no output from encoder available");
                } else if (encoderStatus == -3) {
                    this.mEncoderOutputBuffers = this.mEncoder.getOutputBuffers();
                    Log.d(TAG, "encoder output buffers changed");
                } else if (encoderStatus == -2) {
                    Log.d(TAG, "encoder output format changed: " + this.mEncoder.getOutputFormat());
                } else if (encoderStatus < 0) {
                    Log.i(TAG, "unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
                } else {
                    ByteBuffer encodedData = this.mEncoderOutputBuffers[encoderStatus];
                    if (encodedData == null) {
                        Log.i(TAG, "encoderOutputBuffer " + encoderStatus + " was null");
                    } else {
                        encodedData.position(info.offset);
                        encodedData.limit(info.offset + info.size);
                        if (this.mH264OutputStream != null) {
                            byte[] data = new byte[info.size];
                            encodedData.get(data);
                            encodedData.position(info.offset);
                            this.mH264OutputStream.write(data);
                        }
                    }
                    this.mEncoder.releaseOutputBuffer(encoderStatus, false);
                }
                if (this.mH264OutputStream != null && bLastFrame) {
                    try {
                        this.mH264OutputStream.close();
                        Log.d(TAG, "releasing codecs");
                        if (this.mEncoder != null) {
                            this.mEncoder.stop();
                            this.mEncoder.release();
                            this.mEncoder = null;
                        }
                        if (this.mAudioRecorder != null) {
                            this.mAudioRecorder.stop();
                            this.mAudioRecorder.release();
                            this.mAudioRecorder = null;
                        }
                        final MeasureAudioThread MeasureAudioThread = new MeasureAudioThread(this.mTargetFileName + ".h264", this.mTargetFileName + ".aac", this.mTargetFileName, this.mStartRecordTime, (double) this.mGenerateIndex);
                        Utilities.runOnUIThread(mService, new Runnable() {
                            public void run() {
                                Activity activity = UI.getCurrentActivity();
                                if (activity == null) {
                                    activity = ScreenRecorderService.getActivity();
                                }
                                if (activity != null) {
                                    ScreenRecorderService.this.mProgressDialog = new ProgressDialog(activity);
                                    ScreenRecorderService.this.mProgressDialog.setTitle("正在处理");
                                    ScreenRecorderService.this.mProgressDialog.setMessage("正在处理录课文件，预计需要" + String.valueOf(((int) ((new Date().getTime() - ScreenRecorderService.this.mStartRecordTime.getTime()) / 60000)) * 6) + "秒，请稍候...");
                                    ScreenRecorderService.this.mProgressDialog.setCancelable(false);
                                    ScreenRecorderService.this.mProgressDialog.setIndeterminate(ScreenRecorderService.VERBOSE);
                                    ScreenRecorderService.this.mProgressDialog.setProgressStyle(1);
                                    ScreenRecorderService.this.mProgressDialog.show();
                                }
                                MeasureAudioThread.start();
                            }
                        });
                    } catch (Throwable ioe) {
                        Log.w(TAG, "failed closing debug file");
                        throw new RuntimeException(ioe);
                    }
                }
            } catch (Throwable ioe2) {
                Log.w(TAG, "failed writing debug data to file");
                throw new RuntimeException(ioe2);
            } catch (IllegalStateException e) {
                Toast.makeText(this, "dequeueOutputBuffer 出现错误", 0).show();
                e.printStackTrace();
                throw e;
            }
        } catch (IllegalStateException e2) {
            Toast.makeText(this, "putFrame dequeue 出现错误", 0).show();
            e2.printStackTrace();
            throw e2;
        }
    }

    private long computePresentationTime(int frameIndex) {
        return (long) (((1000000 * frameIndex) / this.mFPS) + 312);
    }

    private void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        int yIndex = 0;
        int uvIndex = width * height;
        int index = 0;
        int j = 0;
        while (j < height) {
            int i = 0;
            int uvIndex2 = uvIndex;
            int yIndex2 = yIndex;
            while (i < width) {
                int a = (argb[index] & -16777216) >> 24;
                int R = (argb[index] & 16711680) >> 16;
                int G = (argb[index] & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >> 8;
                int B = (argb[index] & 255) >> 0;
                int Y = (((((R * 66) + (G * 129)) + (B * 25)) + 128) >> 8) + 16;
                int U = (((((R * -38) - (G * 74)) + (B * 112)) + 128) >> 8) + 128;
                int V = (((((R * 112) - (G * 94)) - (B * 18)) + 128) >> 8) + 128;
                yIndex = yIndex2 + 1;
                if (Y < 0) {
                    Y = 0;
                } else if (Y > 255) {
                    Y = 255;
                }
                yuv420sp[yIndex2] = (byte) Y;
                if (j % 2 == 0 && index % 2 == 0) {
                    uvIndex = uvIndex2 + 1;
                    if (U < 0) {
                        U = 0;
                    } else if (U > 255) {
                        U = 255;
                    }
                    yuv420sp[uvIndex2] = (byte) U;
                    uvIndex2 = uvIndex + 1;
                    if (V < 0) {
                        V = 0;
                    } else if (V > 255) {
                        V = 255;
                    }
                    yuv420sp[uvIndex] = (byte) V;
                }
                index++;
                i++;
                uvIndex2 = uvIndex2;
                yIndex2 = yIndex;
            }
            j++;
            uvIndex = uvIndex2;
            yIndex = yIndex2;
        }
    }

    private void encodeYUV420Planar(byte[] yuv420sp, int[] argb, int width, int height) {
        int frameSize = width * height;
        int yIndex = 0;
        int uIndex = frameSize;
        int vIndex = frameSize + ((yuv420sp.length - frameSize) / 2);
        int index = 0;
        int j = 0;
        while (j < height) {
            int i = 0;
            int vIndex2 = vIndex;
            int uIndex2 = uIndex;
            int yIndex2 = yIndex;
            while (i < width) {
                int a = (argb[index] & -16777216) >> 24;
                int R = (argb[index] & 16711680) >> 16;
                int G = (argb[index] & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >> 8;
                int B = (argb[index] & 255) >> 0;
                int Y = (((((R * 66) + (G * 129)) + (B * 25)) + 128) >> 8) + 16;
                int U = (((((R * -38) - (G * 74)) + (B * 112)) + 128) >> 8) + 128;
                int V = (((((R * 112) - (G * 94)) - (B * 18)) + 128) >> 8) + 128;
                yIndex = yIndex2 + 1;
                if (Y < 0) {
                    Y = 0;
                } else if (Y > 255) {
                    Y = 255;
                }
                yuv420sp[yIndex2] = (byte) Y;
                if (j % 2 == 0 && index % 2 == 0) {
                    uIndex = uIndex2 + 1;
                    if (U < 0) {
                        U = 0;
                    } else if (U > 255) {
                        U = 255;
                    }
                    yuv420sp[uIndex2] = (byte) U;
                    vIndex = vIndex2 + 1;
                    if (V < 0) {
                        V = 0;
                    } else if (V > 255) {
                        V = 255;
                    }
                    yuv420sp[vIndex2] = (byte) V;
                } else {
                    vIndex = vIndex2;
                    uIndex = uIndex2;
                }
                index++;
                i++;
                vIndex2 = vIndex;
                uIndex2 = uIndex;
                yIndex2 = yIndex;
            }
            j++;
            vIndex = vIndex2;
            uIndex = uIndex2;
            yIndex = yIndex2;
        }
    }
}
