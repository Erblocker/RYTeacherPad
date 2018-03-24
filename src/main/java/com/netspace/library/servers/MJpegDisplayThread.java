package com.netspace.library.servers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.media.MediaCodec;
import android.media.MediaCodec.CodecException;
import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build.VERSION;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.Surface;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.restful.provider.device.DeviceOperationRESTServiceProvider;
import com.netspace.library.struct.Size;
import com.netspace.library.threads.MulticastReceiveThread;
import com.netspace.library.threads.MulticastSendThread;
import com.netspace.library.utilities.Utilities;
import com.xsj.crasheye.Crasheye;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Date;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;

public class MJpegDisplayThread extends Thread {
    private static final String MIME_TYPE = "video/avc";
    private static final String TAG = "MJpegDisplayThread";
    private static final boolean VERBOSE = true;
    private static MJpegDisplayThread mInstance = null;
    private static Boolean mInstanceLock = Boolean.TRUE;
    private static volatile boolean mbDecodeStarted = false;
    private final int TIMEOUT_USEC = DeviceOperationRESTServiceProvider.TIMEOUT;
    private int[] mARGBData = null;
    private Context mContext;
    private H264DataToBmpThread mConvertThread;
    private FileOutputStream mDebugFile;
    private boolean mDecodePartImage = false;
    private byte[] mDecodedFrameData = null;
    private MediaCodec mDecoder = null;
    private ByteBuffer[] mDecoderInputBuffers = null;
    private ByteBuffer[] mDecoderOutputBuffers = null;
    private MJpegFrameData mFrameData = new MJpegFrameData();
    private int mGenerateIndex = 0;
    private Bitmap mH264FrameBitmap;
    private Bitmap mLastFrameBitmap;
    private Handler mMonitorHandler = new Handler();
    private MulticastReceiveThread mMulticastReceiveThread;
    private MulticastSendThread mMulticastSendThread;
    final Runnable mNewThreadRunnable = new Runnable() {
        public void run() {
            if (Utilities.isNetworkConnected(MyiBaseApplication.getBaseAppContext())) {
                MJpegDisplayThread NewThread = new MJpegDisplayThread(MJpegDisplayThread.this.mContext, (Handler) MJpegDisplayThread.this.m_Handler.get(), MJpegDisplayThread.this.m_nFrameMsgID, MJpegDisplayThread.this.m_szTargetIP, MJpegDisplayThread.this.m_CallBack);
                if (MJpegDisplayThread.this.m_CallBack != null) {
                    MJpegDisplayThread.this.m_CallBack.OnNewMJpegInstance(NewThread);
                }
                NewThread.setSurface(MJpegDisplayThread.this.mSurface);
                NewThread.setNoReallocBitmap(MJpegDisplayThread.this.m_bNoReallocBitmap);
                NewThread.setDecodePartImage(MJpegDisplayThread.this.mDecodePartImage);
                NewThread.mMulticastReceiveThread = MJpegDisplayThread.this.mMulticastReceiveThread;
                NewThread.mMulticastSendThread = MJpegDisplayThread.this.mMulticastSendThread;
                if (MJpegDisplayThread.this.mMulticastReceiveThread != null) {
                    NewThread.mSourceWidth = MJpegDisplayThread.this.mSourceWidth;
                    NewThread.mSourceHeight = MJpegDisplayThread.this.mSourceHeight;
                }
                NewThread.start();
                return;
            }
            Utilities.runOnUIThreadDelay(MyiBaseApplication.getBaseAppContext(), this, 1000);
        }
    };
    private int mOutputHeight = 0;
    private int mOutputWidth = 0;
    private int mScreenHeight = -1;
    private int mScreenWidth = -1;
    private int mSourceHeight = -1;
    private int mSourceWidth = -1;
    private Date mStartRecordTime;
    private Surface mSurface;
    private MJpegCallInterface m_CallBack = null;
    private WeakReference<Handler> m_Handler;
    private HttpClient m_HttpClient;
    private boolean m_bNoReallocBitmap = false;
    private Boolean m_bPauseDisplay = Boolean.FALSE;
    private boolean m_bPauseReceive = false;
    private boolean m_bShortTimeWait = VERBOSE;
    private volatile Boolean m_bWorking = Boolean.valueOf(VERBOSE);
    private int m_nFrameMsgID = 0;
    private String m_szTargetIP = "";
    private boolean mbConnected = false;
    private volatile boolean mbDecodeStateSet = false;
    private boolean mbH264Frame = false;
    private boolean mbHasNewFrame = false;
    private boolean mbServerResponseH264 = false;
    private int mnWaitDecodeCount = 0;
    private int nConvertFPS = 0;
    private long nConvertLastSecond = 0;

    private class H264DataToBmpThread extends Thread {
        private H264DataToBmpThread() {
        }

        public void run() {
            setName("H264DataToBmpThread");
            while (!Thread.currentThread().isInterrupted()) {
                if (MJpegDisplayThread.this.mbHasNewFrame) {
                    if (MJpegDisplayThread.this.decodeYUV420SP(MJpegDisplayThread.this.mARGBData, MJpegDisplayThread.this.mDecodedFrameData, MJpegDisplayThread.this.mSourceWidth, MJpegDisplayThread.this.mSourceHeight)) {
                        if (MJpegDisplayThread.this.mH264FrameBitmap == null) {
                            MJpegDisplayThread.this.mH264FrameBitmap = Bitmap.createBitmap(MJpegDisplayThread.this.mARGBData, MJpegDisplayThread.this.mSourceWidth, MJpegDisplayThread.this.mSourceHeight, Config.ARGB_8888).copy(Config.ARGB_8888, MJpegDisplayThread.VERBOSE);
                        } else {
                            int nWidth = MJpegDisplayThread.this.mH264FrameBitmap.getWidth();
                            MJpegDisplayThread.this.mH264FrameBitmap.setPixels(MJpegDisplayThread.this.mARGBData, 0, nWidth, 0, 0, nWidth, MJpegDisplayThread.this.mH264FrameBitmap.getHeight());
                        }
                        MJpegDisplayThread.this.mbH264Frame = MJpegDisplayThread.VERBOSE;
                        synchronized (MJpegDisplayThread.this.m_bPauseDisplay) {
                            if (!MJpegDisplayThread.this.m_bPauseDisplay.booleanValue()) {
                                Handler Handler = (Handler) MJpegDisplayThread.this.m_Handler.get();
                                if (Handler != null) {
                                    MJpegDisplayThread.this.mFrameData.bm = MJpegDisplayThread.this.mH264FrameBitmap;
                                    Handler.obtainMessage(MJpegDisplayThread.this.m_nFrameMsgID, MJpegDisplayThread.this.mFrameData).sendToTarget();
                                }
                            }
                            MJpegDisplayThread.this.m_bPauseDisplay = Boolean.TRUE;
                        }
                        MJpegDisplayThread mJpegDisplayThread = MJpegDisplayThread.this;
                        mJpegDisplayThread.nConvertFPS = mJpegDisplayThread.nConvertFPS + 1;
                        if (System.currentTimeMillis() - MJpegDisplayThread.this.nConvertLastSecond > 1000) {
                            Log.i(MJpegDisplayThread.TAG, "Convert fps = " + MJpegDisplayThread.this.nConvertFPS);
                            MJpegDisplayThread.this.nConvertFPS = 0;
                            MJpegDisplayThread.this.nConvertLastSecond = System.currentTimeMillis();
                        }
                    }
                    MJpegDisplayThread.this.mbHasNewFrame = false;
                }
            }
        }
    }

    public interface MJpegCallInterface {
        void OnMJpegError(String str);

        void OnMJpegMessage(String str);

        void OnNewMJpegInstance(MJpegDisplayThread mJpegDisplayThread);
    }

    public class MJpegFrameData {
        public MJpegDisplayThread DisplayObject;
        public Bitmap bm;
    }

    public MJpegDisplayThread(Context context, Handler handler, int nFrameMsgID, String szTargetIP, MJpegCallInterface CallBack) {
        synchronized (mInstanceLock) {
            if (!(mInstance == null || mInstance.equals(this))) {
                mInstance.stopDisplay(VERBOSE);
                Log.d(TAG, "Stop previous thread.");
                if (MyiBaseApplication.isDebugOn()) {
                    Utilities.logInfo("MJpegDisplayThread Stop previous thread.");
                }
            }
            mInstance = this;
        }
        this.m_szTargetIP = szTargetIP;
        this.m_Handler = new WeakReference(handler);
        this.m_nFrameMsgID = nFrameMsgID;
        this.m_CallBack = CallBack;
        this.mContext = context;
        this.mFrameData.DisplayObject = this;
    }

    public MJpegDisplayThread(Context context, Handler handler, int nFrameMsgID, String szTargetMulticastAddress, String szMulticastTCPAddress, int nTargetPort, int nSourceWidth, int nSourceHeight, MJpegCallInterface CallBack) {
        synchronized (mInstanceLock) {
            if (!(mInstance == null || mInstance.equals(this))) {
                mInstance.stopDisplay(VERBOSE);
                Log.d(TAG, "Stop previous thread.");
                if (MyiBaseApplication.isDebugOn()) {
                    Utilities.logInfo("MJpegDisplayThread Stop previous thread.");
                }
            }
            mInstance = this;
        }
        this.mMulticastReceiveThread = new MulticastReceiveThread(context, szTargetMulticastAddress, szMulticastTCPAddress, nTargetPort);
        this.mSourceWidth = nSourceWidth;
        this.mSourceHeight = nSourceHeight;
        this.m_Handler = new WeakReference(handler);
        this.m_nFrameMsgID = nFrameMsgID;
        this.m_CallBack = CallBack;
        this.mContext = context;
        this.mFrameData.DisplayObject = this;
    }

    public boolean isH264() {
        return this.mbServerResponseH264;
    }

    public boolean isConnected() {
        return this.mbConnected;
    }

    public void setFrameHandled() {
        synchronized (this.m_bPauseDisplay) {
            this.m_bPauseDisplay = Boolean.FALSE;
            if (!(this.mbH264Frame || this.m_bNoReallocBitmap)) {
                if (this.mLastFrameBitmap != null) {
                    if (!this.mLastFrameBitmap.isRecycled()) {
                        this.mLastFrameBitmap.recycle();
                    }
                    this.mLastFrameBitmap = null;
                }
                this.mLastFrameBitmap = this.mFrameData.bm;
            }
        }
    }

    public void setPauseReceive(boolean bPause) {
        this.m_bPauseReceive = bPause;
    }

    public void setNoReallocBitmap(boolean bEnable) {
        this.m_bNoReallocBitmap = bEnable;
    }

    public void setOutputSize(int nWidth, int nHeight) {
        this.mOutputWidth = nWidth;
        this.mOutputHeight = nHeight;
    }

    public void setMulticastSendThread(MulticastSendThread SendThread) {
        this.mMulticastSendThread = SendThread;
    }

    public MulticastSendThread getMulticastSendThread() {
        return this.mMulticastSendThread;
    }

    public void stopDisplay() {
        stopDisplay(false);
    }

    public void stopDisplay(boolean bNoWait) {
        this.m_bWorking = Boolean.valueOf(false);
        Log.i(TAG, "MJpegDisplayThread stopDisplay.");
        if (MyiBaseApplication.isDebugOn()) {
            Utilities.logInfo("MJpegDisplayThread stopDisplay");
        }
        Utilities.clearRunnable(this.mContext, this.mNewThreadRunnable);
        if (this.mMulticastReceiveThread != null) {
            this.mMulticastReceiveThread.stopThread();
            this.mMulticastReceiveThread = null;
        }
        if (!(this.m_HttpClient == null || this.m_HttpClient.getConnectionManager() == null)) {
            this.m_HttpClient.getConnectionManager().shutdown();
            Log.i(TAG, "Network shutdown.");
            if (MyiBaseApplication.isDebugOn()) {
                Utilities.logInfo("MJpegDisplayThread network shutdown");
            }
        }
        if (!bNoWait) {
            try {
                join(5000);
                Log.i(TAG, "Thread finished within 5000 ms.");
                if (MyiBaseApplication.isDebugOn()) {
                    Utilities.logInfo("MJpegDisplayThread thread finished in 5000ms");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.i(TAG, "Thread finished exceed 5000 ms.");
                if (MyiBaseApplication.isDebugOn()) {
                    Utilities.logInfo("MJpegDisplayThread thread finished exceed 5000ms");
                }
            }
        }
        this.m_CallBack = null;
    }

    public void SetShortTimeWait(boolean bWait) {
        this.m_bShortTimeWait = bWait;
    }

    public void setDecodePartImage(boolean bEnable) {
        this.mDecodePartImage = bEnable;
    }

    private void shutdownDecoder() {
        Log.i(TAG, "Shutdown H264 Encoder.");
        if (MyiBaseApplication.isDebugOn()) {
            Utilities.logInfo("MJpegDisplayThread shutdownDecoder. mbDecodeStateSet=" + this.mbDecodeStateSet + ",mbDecodeStarted=" + mbDecodeStarted);
        }
        if (this.mDecoder != null) {
            if (MyiBaseApplication.isDebugOn()) {
                Utilities.logInfo("MJpegDisplayThread Before mDecoder.stop.");
            }
            try {
                this.mDecoder.stop();
            } catch (IllegalStateException e) {
                Utilities.logException(e);
                e.printStackTrace();
            }
            if (MyiBaseApplication.isDebugOn()) {
                Utilities.logInfo("MJpegDisplayThread After mDecoder.stop");
            }
            this.mDecoder.release();
            this.mDecoder = null;
            if (MyiBaseApplication.isDebugOn()) {
                Utilities.logInfo("MJpegDisplayThread Decoder released.");
            }
        }
        if (this.mbDecodeStateSet && mbDecodeStarted) {
            mbDecodeStarted = false;
            if (MyiBaseApplication.isDebugOn()) {
                Utilities.logInfo("MJpegDisplayThread mbDecodeStarted set to false.");
            }
        }
        this.mDecoderInputBuffers = null;
        this.mDecoderOutputBuffers = null;
        this.mDecodedFrameData = null;
        this.mFrameData = null;
    }

    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
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

    private boolean decodeYUV420SP(int[] argb, byte[] yuv420sp, int width, int height) {
        int frameSize = ((width % 16) + width) * ((height % 16) + height);
        if (yuv420sp.length < width * height) {
            Log.e(TAG, "Wrong size of yuv420sp. Can not convert to ARGB. ");
            return false;
        }
        int yp = 0;
        for (int j = 0; j < height; j++) {
            int u = 0;
            int v = 0;
            int i = 0;
            int uvp = frameSize + ((j >> 1) * width);
            while (i < width) {
                int uvp2;
                int y = (yuv420sp[yp] & 255) - 16;
                if (y < 0) {
                    y = 0;
                }
                if ((i & 1) == 0) {
                    uvp2 = uvp + 1;
                    v = (yuv420sp[uvp] & 255) - 128;
                    u = (yuv420sp[uvp2] & 255) - 128;
                    uvp2++;
                } else {
                    uvp2 = uvp;
                }
                int y1192 = y * 1192;
                int r = y1192 + (v * 1634);
                int g = (y1192 - (v * 833)) - (u * HttpStatus.SC_BAD_REQUEST);
                int b = y1192 + (u * 2066);
                if (r < 0) {
                    r = 0;
                } else if (r > 262143) {
                    r = 262143;
                }
                if (g < 0) {
                    g = 0;
                } else if (g > 262143) {
                    g = 262143;
                }
                if (b < 0) {
                    b = 0;
                } else if (b > 262143) {
                    b = 262143;
                }
                argb[yp] = ((-16777216 | (((b >> 10) << 16) & 16711680)) | (((g >> 10) << 8) & MotionEventCompat.ACTION_POINTER_INDEX_MASK)) | ((r >> 10) & 255);
                i++;
                yp++;
                uvp = uvp2;
            }
        }
        return VERBOSE;
    }

    public void setSurface(Surface surface) {
        this.mSurface = surface;
    }

    private boolean configDecode(MjpegInputStream InputStream) throws Exception {
        if (MyiBaseApplication.isDebugOn()) {
            Utilities.logInfo("MJpegDisplayThread configDecode");
        }
        if (mbDecodeStarted && this.mDecoder == null) {
            try {
                throw new IllegalStateException("Previous decode is not released. Wait for another thread to stop. ");
            } catch (IllegalStateException e) {
                Utilities.logException(e);
                Crasheye.logException(e);
                Log.e(TAG, "Previous decode is not release. Wait for another thread to stop. ");
                if (MyiBaseApplication.isDebugOn()) {
                    this.mnWaitDecodeCount++;
                    if (this.mnWaitDecodeCount > 5) {
                        Utilities.beep(100, -1);
                    }
                }
                if (this.m_CallBack != null) {
                    this.m_CallBack.OnMJpegError("等待编码器释放");
                }
                try {
                    Thread.sleep(1000);
                    return VERBOSE;
                } catch (InterruptedException e2) {
                    return VERBOSE;
                }
            }
        }
        MediaCodecInfo codecInfo = selectCodec(MIME_TYPE);
        if (codecInfo == null) {
            Log.e(TAG, "Unable to find an appropriate codec for video/avc");
            return false;
        }
        Log.i(TAG, "found codec: " + codecInfo.getName());
        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, this.mSourceWidth, this.mSourceHeight);
        try {
            if (InputStream.readH264Info(format, this.mDebugFile)) {
                if (MyiBaseApplication.isDebugOn()) {
                    Utilities.logInfo("MJpegDisplayThread readH264Info success");
                }
                format.setInteger(io.vov.vitamio.MediaFormat.KEY_SLICE_HEIGHT, this.mSourceHeight);
                format.setInteger(io.vov.vitamio.MediaFormat.KEY_STRIDE, this.mSourceWidth);
                format.setInteger("width", this.mSourceWidth);
                format.setInteger("height", this.mSourceHeight);
                if (VERSION.SDK_INT >= 23) {
                    format.setInteger(io.vov.vitamio.MediaFormat.KEY_COLOR_FORMAT, 2135033992);
                } else {
                    format.setInteger(io.vov.vitamio.MediaFormat.KEY_COLOR_FORMAT, 21);
                }
                CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(MIME_TYPE);
                for (int colorFormat : capabilities.colorFormats) {
                    Log.i(TAG, "Support color format=" + colorFormat);
                }
                try {
                    this.mDecoder = MediaCodec.createByCodecName(codecInfo.getName());
                    if (MyiBaseApplication.isDebugOn()) {
                        Utilities.logInfo("MJpegDisplayThread MediaCodec.createByCodecName success");
                    }
                    this.mDecoder.configure(format, this.mSurface, null, 0);
                    if (MyiBaseApplication.isDebugOn()) {
                        Utilities.logInfo("MJpegDisplayThread mDecoder.configure success");
                    }
                    this.mDecoder.start();
                    if (MyiBaseApplication.isDebugOn()) {
                        Utilities.logInfo("MJpegDisplayThread mDecoder.start() success");
                    }
                    mbDecodeStarted = VERBOSE;
                    this.mbDecodeStateSet = VERBOSE;
                    if (MyiBaseApplication.isDebugOn()) {
                        Utilities.logInfo("MJpegDisplayThread mbDecodeStarted and mbDecodeStateSet set");
                    }
                    if (this.m_CallBack != null) {
                        this.m_CallBack.OnMJpegMessage("解码器已配置");
                    }
                    this.mDecoderInputBuffers = this.mDecoder.getInputBuffers();
                    this.mDecoderOutputBuffers = this.mDecoder.getOutputBuffers();
                    this.mARGBData = new int[(this.mSourceWidth * this.mSourceHeight)];
                    if (this.mSurface != null) {
                        Handler Handler = (Handler) this.m_Handler.get();
                        if (Handler != null) {
                            Handler.obtainMessage(this.m_nFrameMsgID, new Size(this.mScreenWidth, this.mScreenHeight)).sendToTarget();
                            if (this.m_CallBack != null) {
                                this.m_CallBack.OnMJpegMessage("图像参数已获得");
                            }
                        }
                    }
                } catch (CodecException e3) {
                    Utilities.logException(e3);
                    Crasheye.logException(e3);
                    if (e3.isRecoverable()) {
                        this.mDecoder.stop();
                        this.mDecoder = null;
                        if (this.mbDecodeStateSet) {
                            mbDecodeStarted = false;
                            this.mbDecodeStateSet = false;
                        }
                    }
                    if (!(e3.isRecoverable() || e3.isTransient())) {
                        this.mDecoder.reset();
                        this.mDecoder = null;
                        if (this.mbDecodeStateSet) {
                            mbDecodeStarted = false;
                            this.mbDecodeStateSet = false;
                        }
                    }
                    handleExceptionNoThrow(e3, "MediaCodec.createByCodecName");
                } catch (Exception e4) {
                    Utilities.logException(e4);
                    Crasheye.logException(e4);
                    handleException(e4, "MediaCodec.createByCodecName");
                }
                return VERBOSE;
            }
            if (this.m_CallBack != null) {
                this.m_CallBack.OnMJpegMessage("无法获得解码器参数，将自动重试");
            }
            return VERBOSE;
        } catch (EOFException e5) {
            Utilities.logException(e5);
            throw e5;
        } catch (IOException e6) {
            Utilities.logException(e6);
            return VERBOSE;
        }
    }

    private void cleanLastSession() {
        synchronized (mInstanceLock) {
            if (mInstance != null && mInstance.equals(this)) {
                mInstance = null;
            }
        }
    }

    public void run() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxOverflowException: Regions stack size limit reached
	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:36)
	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:60)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:323)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:226)
*/
        /*
        r50 = this;
        r4 = "MJpegDisplayThread";
        r0 = r50;
        r0.setName(r4);
        r4 = -8;
        android.os.Process.setThreadPriority(r4);
    L_0x000c:
        r4 = com.netspace.library.utilities.Utilities.isScreenOn();
        if (r4 == 0) goto L_0x012a;
    L_0x0012:
        r45 = 0;
        r25 = new org.apache.http.params.BasicHttpParams;
        r25.<init>();
        r20 = 0;
        r21 = 0;
        r43 = 0;
        r13 = 0;
        r19 = 0;
        r36 = 0;
        r33 = 0;
        r32 = 0;
        r38 = 0;
        r4 = "MJpegDisplayThread";
        r6 = new java.lang.StringBuilder;
        r7 = "MJpegDisplayThread start. target url=";
        r6.<init>(r7);
        r0 = r50;
        r7 = r0.m_szTargetIP;
        r6 = r6.append(r7);
        r6 = r6.toString();
        android.util.Log.e(r4, r6);
        r0 = r50;
        r4 = r0.m_bShortTimeWait;
        if (r4 == 0) goto L_0x013f;
    L_0x004a:
        r4 = 5000; // 0x1388 float:7.006E-42 double:2.4703E-320;
        r0 = r25;
        org.apache.http.params.HttpConnectionParams.setConnectionTimeout(r0, r4);
        r4 = 5000; // 0x1388 float:7.006E-42 double:2.4703E-320;
        r0 = r25;
        org.apache.http.params.HttpConnectionParams.setSoTimeout(r0, r4);
    L_0x0058:
        r4 = new org.apache.http.impl.client.DefaultHttpClient;
        r0 = r25;
        r4.<init>(r0);
        r0 = r50;
        r0.m_HttpClient = r4;
        r0 = r50;
        r4 = r0.m_HttpClient;
        r24 = r4.getParams();
        r0 = r50;
        r4 = r0.m_bShortTimeWait;
        if (r4 == 0) goto L_0x0148;
    L_0x0071:
        r4 = 5000; // 0x1388 float:7.006E-42 double:2.4703E-320;
        r0 = r24;
        org.apache.http.params.HttpConnectionParams.setConnectionTimeout(r0, r4);
        r4 = 5000; // 0x1388 float:7.006E-42 double:2.4703E-320;
        r0 = r24;
        org.apache.http.params.HttpConnectionParams.setSoTimeout(r0, r4);
    L_0x007f:
        r4 = "MJpegDisplayThread";
        r6 = "1. Sending http request";
        android.util.Log.i(r4, r6);
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r0.m_CallBack;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        if (r4 == 0) goto L_0x00a9;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
    L_0x008e:
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r0.m_CallBack;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r6 = new java.lang.StringBuilder;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r7 = "准备连接到服务器";	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r6.<init>(r7);	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r7 = r0.m_szTargetIP;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r6 = r6.append(r7);	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r6 = r6.toString();	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4.OnMJpegMessage(r6);	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
    L_0x00a9:
        r23 = new org.apache.http.client.methods.HttpGet;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r0.m_szTargetIP;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = java.net.URI.create(r4);	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0 = r23;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0.<init>(r4);	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = "PartImage";	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r6 = "support";	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0 = r23;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0.addHeader(r4, r6);	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = android.os.Build.VERSION.SDK_INT;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r6 = 21;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        if (r4 < r6) goto L_0x00f2;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
    L_0x00c9:
        r4 = com.netspace.library.application.MyiBaseApplication.getCommonVariables();	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r4.UserInfo;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r6 = "enableH264";	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r4.checkPermission(r6);	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        if (r4 != 0) goto L_0x00e7;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
    L_0x00d8:
        r4 = com.netspace.library.application.MyiBaseApplication.getCommonVariables();	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r4.UserInfo;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r6 = "enableH264_receiveonly";	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r4.checkPermission(r6);	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        if (r4 == 0) goto L_0x00f2;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
    L_0x00e7:
        r4 = "Accept";	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r6 = "video/h264";	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0 = r23;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0.addHeader(r4, r6);	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
    L_0x00f2:
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r0.m_HttpClient;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0 = r23;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r45 = r4.execute(r0);	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = "MJpegDisplayThread";	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r6 = new java.lang.StringBuilder;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r7 = "2. Request finished, status = ";	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r6.<init>(r7);	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r7 = r45.getStatusLine();	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r7 = r7.getStatusCode();	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r6 = r6.append(r7);	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r6 = r6.toString();	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        android.util.Log.i(r4, r6);	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r45.getStatusLine();	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r4.getStatusCode();	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r6 = 401; // 0x191 float:5.62E-43 double:1.98E-321;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        if (r4 != r6) goto L_0x0151;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
    L_0x0126:
        r50.cleanLastSession();	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
    L_0x0129:
        return;
    L_0x012a:
        r4 = "MJpegDisplayThread";
        r6 = "Screen is not on. Wait until screen is truly on";
        android.util.Log.e(r4, r6);
        r6 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        java.lang.Thread.sleep(r6);	 Catch:{ InterruptedException -> 0x013a }
        goto L_0x000c;
    L_0x013a:
        r16 = move-exception;
        r50.cleanLastSession();
        goto L_0x0129;
    L_0x013f:
        r4 = 5000; // 0x1388 float:7.006E-42 double:2.4703E-320;
        r0 = r25;
        org.apache.http.params.HttpConnectionParams.setConnectionTimeout(r0, r4);
        goto L_0x0058;
    L_0x0148:
        r4 = 5000; // 0x1388 float:7.006E-42 double:2.4703E-320;
        r0 = r24;
        org.apache.http.params.HttpConnectionParams.setConnectionTimeout(r0, r4);
        goto L_0x007f;
    L_0x0151:
        r4 = 1;
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0.mbConnected = r4;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r0.m_CallBack;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        if (r4 == 0) goto L_0x0166;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
    L_0x015c:
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r0.m_CallBack;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r6 = "已连接到服务器";	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4.OnMJpegMessage(r6);	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
    L_0x0166:
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r0.m_bWorking;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r4.booleanValue();	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        if (r4 != 0) goto L_0x0234;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
    L_0x0170:
        r50.cleanLastSession();	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        goto L_0x0129;
    L_0x0174:
        r16 = move-exception;
    L_0x0175:
        com.netspace.library.utilities.Utilities.logException(r16);
        r4 = 0;
        r0 = r50;
        r0.mbConnected = r4;
        r16.printStackTrace();
        r4 = "MJpegDisplayThread";
        r6 = "Request failed-ClientProtocolException";
        r0 = r16;
        android.util.Log.i(r4, r6, r0);
        r0 = r50;
        r4 = r0.m_CallBack;
        if (r4 == 0) goto L_0x01a9;
    L_0x0191:
        r46 = r16.getMessage();
        if (r46 == 0) goto L_0x019d;
    L_0x0197:
        r4 = r46.isEmpty();
        if (r4 == 0) goto L_0x01a0;
    L_0x019d:
        r46 = "客户端协议异常";
    L_0x01a0:
        r0 = r50;
        r4 = r0.m_CallBack;
        r0 = r46;
        r4.OnMJpegError(r0);
    L_0x01a9:
        r0 = r50;
        r4 = r0.m_bWorking;
        r4 = r4.booleanValue();
        if (r4 == 0) goto L_0x01b9;
    L_0x01b3:
        r50.shutdownDecoder();
        r50.createNewThread();
    L_0x01b9:
        r4 = "MJpegDisplayThread";
        r6 = "Main decode loop prepare to stop";
        android.util.Log.i(r4, r6);
        r4 = com.netspace.library.application.MyiBaseApplication.isDebugOn();
        if (r4 == 0) goto L_0x01ce;
    L_0x01c8:
        r4 = "MJpegDisplayThread Main decode loop prepare to stop";
        com.netspace.library.utilities.Utilities.logInfo(r4);
    L_0x01ce:
        if (r20 == 0) goto L_0x01d5;
    L_0x01d0:
        r20.recycle();
        r20 = 0;
    L_0x01d5:
        r0 = r50;
        r4 = r0.mFrameData;
        if (r4 == 0) goto L_0x01e2;
    L_0x01db:
        r0 = r50;
        r4 = r0.mFrameData;
        r6 = 0;
        r4.bm = r6;
    L_0x01e2:
        r0 = r50;
        r4 = r0.mDebugFile;
        if (r4 == 0) goto L_0x0208;
    L_0x01e8:
        r4 = "MJpegDisplayThread";
        r6 = "Dump file about to close.";
        android.util.Log.i(r4, r6);
        r0 = r50;	 Catch:{ IOException -> 0x0811 }
        r4 = r0.mDebugFile;	 Catch:{ IOException -> 0x0811 }
        r4.flush();	 Catch:{ IOException -> 0x0811 }
        r0 = r50;	 Catch:{ IOException -> 0x0811 }
        r4 = r0.mDebugFile;	 Catch:{ IOException -> 0x0811 }
        r4.close();	 Catch:{ IOException -> 0x0811 }
    L_0x01ff:
        r4 = "MJpegDisplayThread";
        r6 = "Dump file closed.";
        android.util.Log.i(r4, r6);
    L_0x0208:
        r4 = "MJpegDisplayThread";
        r6 = "Main decode about to shutdown decoder.";
        android.util.Log.i(r4, r6);
        r4 = com.netspace.library.application.MyiBaseApplication.isDebugOn();
        if (r4 == 0) goto L_0x021d;
    L_0x0217:
        r4 = "MJpegDisplayThread Main decode about to shutdown decoder.";
        com.netspace.library.utilities.Utilities.logInfo(r4);
    L_0x021d:
        r50.shutdownDecoder();
        r4 = com.netspace.library.application.MyiBaseApplication.isDebugOn();
        if (r4 == 0) goto L_0x022c;
    L_0x0226:
        r4 = "MJpegDisplayThread Main decode complete shutdown. Thread finish.";
        com.netspace.library.utilities.Utilities.logInfo(r4);
    L_0x022c:
        r50.cleanLastSession();
        super.run();
        goto L_0x0129;
    L_0x0234:
        r12 = new com.netspace.library.servers.MjpegInputStream;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r45.getEntity();	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r4.getContent();	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r12.<init>(r4);	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r0.mDecodePartImage;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r12.setDecodePartImage(r4);	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r0.mMulticastSendThread;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r12.setMulticastSendThread(r4);	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r13 = r12.isH264();	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r0.m_bWorking;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r4.booleanValue();	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        if (r4 != 0) goto L_0x02a9;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
    L_0x025d:
        r50.cleanLastSession();	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        goto L_0x0129;
    L_0x0262:
        r16 = move-exception;
    L_0x0263:
        com.netspace.library.utilities.Utilities.logException(r16);
        r4 = 0;
        r0 = r50;
        r0.mbConnected = r4;
        r16.printStackTrace();
        r0 = r50;
        r4 = r0.m_CallBack;
        if (r4 == 0) goto L_0x028c;
    L_0x0274:
        r46 = r16.getMessage();
        if (r46 == 0) goto L_0x0280;
    L_0x027a:
        r4 = r46.isEmpty();
        if (r4 == 0) goto L_0x0283;
    L_0x0280:
        r46 = "IO异常";
    L_0x0283:
        r0 = r50;
        r4 = r0.m_CallBack;
        r0 = r46;
        r4.OnMJpegError(r0);
    L_0x028c:
        r4 = "MJpegDisplayThread";
        r6 = "Request failed-IOException";
        r0 = r16;
        android.util.Log.i(r4, r6, r0);
        r0 = r50;
        r4 = r0.m_bWorking;
        r4 = r4.booleanValue();
        if (r4 == 0) goto L_0x01b9;
    L_0x02a1:
        r50.shutdownDecoder();
        r50.createNewThread();
        goto L_0x01b9;
    L_0x02a9:
        if (r13 == 0) goto L_0x034b;
    L_0x02ab:
        r4 = r12.getWidth();	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0.mSourceWidth = r4;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r12.getHeight();	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0.mSourceHeight = r4;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r12.getRealScreenWidth();	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0.mScreenWidth = r4;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r12.getRealScreenHeight();	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0.mScreenHeight = r4;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = 1;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0.mbServerResponseH264 = r4;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r0.mSourceWidth;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        if (r4 <= 0) goto L_0x02dc;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
    L_0x02d6:
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r0.mSourceHeight;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        if (r4 > 0) goto L_0x02e5;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
    L_0x02dc:
        r4 = "MJpegDisplayThread";	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r6 = "Can not found a vaild height and width.";	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        android.util.Log.e(r4, r6);	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
    L_0x02e5:
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r0.mConvertThread;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        if (r4 != 0) goto L_0x0304;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
    L_0x02eb:
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r0.mSurface;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        if (r4 != 0) goto L_0x0304;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
    L_0x02f1:
        r4 = new com.netspace.library.servers.MJpegDisplayThread$H264DataToBmpThread;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r6 = 0;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4.<init>();	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0.mConvertThread = r4;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r0.mConvertThread;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4.start();	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
    L_0x0304:
        r4 = new java.util.Date;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4.<init>();	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0.mStartRecordTime = r4;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = com.netspace.library.application.MyiBaseApplication.isDebugOn();	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        if (r4 == 0) goto L_0x081b;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
    L_0x0313:
        r4 = "MJpegDisplayThread ready to begin main loop";	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        com.netspace.library.utilities.Utilities.logInfo(r4);	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r22 = r21;
    L_0x031b:
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = r0.m_bWorking;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = r4.booleanValue();	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        if (r4 != 0) goto L_0x038b;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x0325:
        r4 = "MJpegDisplayThread";	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r6 = "Main decode loop exit.";	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        android.util.Log.i(r4, r6);	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = com.netspace.library.application.MyiBaseApplication.isDebugOn();	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        if (r4 == 0) goto L_0x033a;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x0334:
        r4 = "MJpegDisplayThread main decode loop exit";	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        com.netspace.library.utilities.Utilities.logInfo(r4);	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x033a:
        if (r20 == 0) goto L_0x0817;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x033c:
        r4 = 0;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r0 = r22;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r0.setBitmap(r4);	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r21 = 0;
        r20.recycle();	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r20 = 0;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        goto L_0x01b9;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
    L_0x034b:
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r0.m_CallBack;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        if (r4 == 0) goto L_0x0304;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
    L_0x0351:
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r0.m_CallBack;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r6 = "";	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4.OnMJpegMessage(r6);	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        goto L_0x0304;
    L_0x035c:
        r16 = move-exception;
    L_0x035d:
        com.netspace.library.utilities.Utilities.logException(r16);
        r4 = 0;
        r0 = r50;
        r0.mbConnected = r4;
        r0 = r50;
        r4 = r0.m_CallBack;
        if (r4 == 0) goto L_0x0376;
    L_0x036b:
        r0 = r50;
        r4 = r0.m_CallBack;
        r6 = r16.getMessage();
        r4.OnMJpegError(r6);
    L_0x0376:
        r16.printStackTrace();
        r0 = r50;
        r4 = r0.m_bWorking;
        r4 = r4.booleanValue();
        if (r4 == 0) goto L_0x01b9;
    L_0x0383:
        r50.shutdownDecoder();
        r50.createNewThread();
        goto L_0x01b9;
    L_0x038b:
        if (r13 == 0) goto L_0x06bb;
    L_0x038d:
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = r0.mSurface;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        if (r4 == 0) goto L_0x03c1;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x0393:
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = r0.mSurface;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = r4.isValid();	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        if (r4 != 0) goto L_0x03c1;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x039d:
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = r0.m_CallBack;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        if (r4 == 0) goto L_0x03ad;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x03a3:
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = r0.m_CallBack;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r6 = "目标显示区域无效";	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4.OnMJpegError(r6);	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x03ad:
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = r0.m_bWorking;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = r4.booleanValue();	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        if (r4 == 0) goto L_0x0325;
    L_0x03b7:
        r6 = 100;
        java.lang.Thread.sleep(r6);	 Catch:{ InterruptedException -> 0x03be }
        goto L_0x031b;
    L_0x03be:
        r4 = move-exception;
        goto L_0x031b;
    L_0x03c1:
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = r0.m_bWorking;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = r4.booleanValue();	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        if (r4 == 0) goto L_0x0325;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x03cb:
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = r0.mDecoder;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        if (r4 != 0) goto L_0x03d9;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x03d1:
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = r0.configDecode(r12);	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        if (r4 == 0) goto L_0x0325;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x03d9:
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = r0.mDecoder;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        if (r4 == 0) goto L_0x031b;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x03df:
        r4 = com.netspace.library.application.MyiBaseApplication.isDebugOn();	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        if (r4 == 0) goto L_0x03fd;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x03e5:
        r6 = 0;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = (r38 > r6 ? 1 : (r38 == r6 ? 0 : -1));	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        if (r4 <= 0) goto L_0x03fd;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x03eb:
        r6 = java.lang.System.currentTimeMillis();	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r6 = r6 - r38;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r48 = 10000; // 0x2710 float:1.4013E-41 double:4.9407E-320;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = (r6 > r48 ? 1 : (r6 == r48 ? 0 : -1));	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        if (r4 <= 0) goto L_0x03fd;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x03f7:
        r4 = 100;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r6 = -1;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        com.netspace.library.utilities.Utilities.beep(r4, r6);	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x03fd:
        r29 = new android.media.MediaCodec$BufferInfo;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r29.<init>();	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r5 = -1;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r15 = new java.util.Date;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r15.<init>();	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r8 = java.lang.System.nanoTime();	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r42 = 0;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x040e:
        r4 = -1;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        if (r5 != r4) goto L_0x0417;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x0411:
        r4 = 30;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r0 = r42;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        if (r0 < r4) goto L_0x045b;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x0417:
        if (r5 < 0) goto L_0x04e5;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x0419:
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = r0.mDecoderInputBuffers;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r30 = r4[r5];	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r30.clear();	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r14 = 0;
        r0 = r50;	 Catch:{ IOException -> 0x0447, ClientProtocolException -> 0x0456, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = r0.mDebugFile;	 Catch:{ IOException -> 0x0447, ClientProtocolException -> 0x0456, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r0 = r30;	 Catch:{ IOException -> 0x0447, ClientProtocolException -> 0x0456, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r14 = r12.readH264Frame2(r0, r4);	 Catch:{ IOException -> 0x0447, ClientProtocolException -> 0x0456, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        if (r14 != 0) goto L_0x0486;	 Catch:{ IOException -> 0x0447, ClientProtocolException -> 0x0456, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x042f:
        r4 = "MJpegDisplayThread";	 Catch:{ IOException -> 0x0447, ClientProtocolException -> 0x0456, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r6 = "InputStream.readH264Frame failed. Try again.";	 Catch:{ IOException -> 0x0447, ClientProtocolException -> 0x0456, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        android.util.Log.e(r4, r6);	 Catch:{ IOException -> 0x0447, ClientProtocolException -> 0x0456, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r0 = r50;	 Catch:{ IOException -> 0x0447, ClientProtocolException -> 0x0456, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = r0.mDecoder;	 Catch:{ IOException -> 0x0447, ClientProtocolException -> 0x0456, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r6 = 0;	 Catch:{ IOException -> 0x0447, ClientProtocolException -> 0x0456, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r7 = r30.position();	 Catch:{ IOException -> 0x0447, ClientProtocolException -> 0x0456, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r10 = 0;	 Catch:{ IOException -> 0x0447, ClientProtocolException -> 0x0456, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4.queueInputBuffer(r5, r6, r7, r8, r10);	 Catch:{ IOException -> 0x0447, ClientProtocolException -> 0x0456, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        goto L_0x031b;
    L_0x0447:
        r16 = move-exception;
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = r0.mDecoder;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r6 = 0;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r7 = r30.position();	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r10 = 0;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4.queueInputBuffer(r5, r6, r7, r8, r10);	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        throw r16;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x0456:
        r16 = move-exception;
        r21 = r22;
        goto L_0x0175;
    L_0x045b:
        r0 = r50;	 Catch:{ IllegalStateException -> 0x0472, ClientProtocolException -> 0x0456, IOException -> 0x0481, Exception -> 0x04ef }
        r4 = r0.mDecoder;	 Catch:{ IllegalStateException -> 0x0472, ClientProtocolException -> 0x0456, IOException -> 0x0481, Exception -> 0x04ef }
        r6 = 5000; // 0x1388 float:7.006E-42 double:2.4703E-320;	 Catch:{ IllegalStateException -> 0x0472, ClientProtocolException -> 0x0456, IOException -> 0x0481, Exception -> 0x04ef }
        r5 = r4.dequeueInputBuffer(r6);	 Catch:{ IllegalStateException -> 0x0472, ClientProtocolException -> 0x0456, IOException -> 0x0481, Exception -> 0x04ef }
        r0 = r50;	 Catch:{ IllegalStateException -> 0x0472, ClientProtocolException -> 0x0456, IOException -> 0x0481, Exception -> 0x04ef }
        r4 = r0.m_bWorking;	 Catch:{ IllegalStateException -> 0x0472, ClientProtocolException -> 0x0456, IOException -> 0x0481, Exception -> 0x04ef }
        r4 = r4.booleanValue();	 Catch:{ IllegalStateException -> 0x0472, ClientProtocolException -> 0x0456, IOException -> 0x0481, Exception -> 0x04ef }
        if (r4 == 0) goto L_0x0417;
    L_0x046f:
        r42 = r42 + 1;
        goto L_0x040e;
    L_0x0472:
        r16 = move-exception;
        com.netspace.library.utilities.Utilities.logException(r16);	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = "mDecoder\t.dequeueInputBuffer";	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r1 = r16;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r0.handleException(r1, r4);	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        goto L_0x0417;
    L_0x0481:
        r16 = move-exception;
        r21 = r22;
        goto L_0x0263;
    L_0x0486:
        r4 = r30.position();	 Catch:{ IOException -> 0x0447, ClientProtocolException -> 0x0456, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r32 = r32 + r4;
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = r0.m_bWorking;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = r4.booleanValue();	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        if (r4 != 0) goto L_0x04a3;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x0496:
        r4 = "MJpegDisplayThread m_bWorking = false. ready to break loop.";	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        com.netspace.library.utilities.Utilities.logInfo(r4);	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        goto L_0x0325;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x049e:
        r16 = move-exception;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r21 = r22;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        goto L_0x035d;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x04a3:
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = r0.mDecoder;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r6 = 0;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r7 = r30.position();	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r10 = 0;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4.queueInputBuffer(r5, r6, r7, r8, r10);	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x04b0:
        r18 = -1;
        r0 = r50;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4 = r0.mDecoder;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = 5000; // 0x1388 float:7.006E-42 double:2.4703E-320;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r0 = r29;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r18 = r4.dequeueOutputBuffer(r0, r6);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4 = -1;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r0 = r18;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        if (r0 != r4) goto L_0x050f;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
    L_0x04c3:
        r4 = "MJpegDisplayThread";	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = "no output from encoder available";	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        android.util.Log.i(r4, r6);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r21 = r22;
    L_0x04ce:
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r0.m_bPauseReceive;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        if (r4 == 0) goto L_0x07c1;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
    L_0x04d4:
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r4 = r0.m_bNoReallocBitmap;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        if (r4 != 0) goto L_0x081b;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
    L_0x04da:
        if (r19 == 0) goto L_0x081b;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
    L_0x04dc:
        r19.recycle();	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r19 = 0;
        r22 = r21;
        goto L_0x031b;
    L_0x04e5:
        r4 = "MJpegDisplayThread";	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r6 = "input buffer not available. Dropping frame.";	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        android.util.Log.i(r4, r6);	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        goto L_0x04b0;
    L_0x04ef:
        r16 = move-exception;
        r21 = r22;
    L_0x04f2:
        com.netspace.library.utilities.Utilities.logException(r16);
        r4 = 0;
        r0 = r50;
        r0.mbConnected = r4;
        r16.printStackTrace();
        r0 = r50;
        r4 = r0.m_bWorking;
        r4 = r4.booleanValue();
        if (r4 == 0) goto L_0x01b9;
    L_0x0507:
        r50.shutdownDecoder();
        r50.createNewThread();
        goto L_0x01b9;
    L_0x050f:
        r4 = -3;
        r0 = r18;
        if (r0 != r4) goto L_0x052c;
    L_0x0514:
        r0 = r50;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4 = r0.mDecoder;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4 = r4.getOutputBuffers();	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r0 = r50;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r0.mDecoderOutputBuffers = r4;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4 = "MJpegDisplayThread";	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = "encoder output buffers changed";	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        android.util.Log.i(r4, r6);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r21 = r22;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        goto L_0x04ce;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
    L_0x052c:
        r4 = -2;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r0 = r18;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        if (r0 != r4) goto L_0x0555;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
    L_0x0531:
        r0 = r50;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4 = r0.mDecoder;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r44 = r4.getOutputFormat();	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4 = "MJpegDisplayThread";	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r7 = "encoder output format changed: ";	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6.<init>(r7);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r0 = r44;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = r6.append(r0);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = r6.toString();	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        android.util.Log.i(r4, r6);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r21 = r22;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        goto L_0x04ce;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
    L_0x0555:
        if (r18 >= 0) goto L_0x0573;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
    L_0x0557:
        r4 = "MJpegDisplayThread";	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r7 = "unexpected result from encoder.dequeueOutputBuffer: ";	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6.<init>(r7);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r0 = r18;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = r6.append(r0);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = r6.toString();	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        android.util.Log.i(r4, r6);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r21 = r22;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        goto L_0x04ce;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
    L_0x0573:
        r0 = r50;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4 = r0.mDecoderOutputBuffers;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r17 = r4[r18];	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        if (r17 != 0) goto L_0x0662;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
    L_0x057b:
        r33 = r33 + 1;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4 = "MJpegDisplayThread";	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r7 = "encoderOutputBuffer ";	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6.<init>(r7);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r0 = r18;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = r6.append(r0);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r7 = " was null";	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = r6.append(r7);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = r6.toString();	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        android.util.Log.i(r4, r6);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
    L_0x059c:
        r6 = java.lang.System.currentTimeMillis();	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = r6 - r38;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r48 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4 = (r6 > r48 ? 1 : (r6 == r48 ? 0 : -1));	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        if (r4 <= 0) goto L_0x064f;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
    L_0x05a8:
        r0 = r50;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4 = r0.m_CallBack;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        if (r4 == 0) goto L_0x05fd;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
    L_0x05ae:
        r0 = r32;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r0 = r0 / 1024;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r32 = r0;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r0 = r50;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4 = r0.m_CallBack;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r7 = java.lang.String.valueOf(r32);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r7 = java.lang.String.valueOf(r7);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6.<init>(r7);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r7 = "KB/s, ";	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = r6.append(r7);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r7 = java.lang.String.valueOf(r33);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = r6.append(r7);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r7 = "帧每秒";	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = r6.append(r7);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = r6.toString();	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4.OnMJpegMessage(r6);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4 = com.netspace.library.application.MyiBaseApplication.isDebugOn();	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        if (r4 == 0) goto L_0x05fd;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
    L_0x05e8:
        r4 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = "MJpegDisplayThread FPS=";	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4.<init>(r6);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r0 = r33;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4 = r4.append(r0);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4 = r4.toString();	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        com.netspace.library.utilities.Utilities.logInfo(r4);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
    L_0x05fd:
        r4 = "MJpegDisplayThread";	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r7 = "Decode fps = ";	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6.<init>(r7);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r0 = r33;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = r6.append(r0);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = r6.toString();	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        android.util.Log.w(r4, r6);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4 = "MJpegDisplayThread";	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r7 = "-------- Data in queue = ";	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6.<init>(r7);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r7 = r12.available();	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = r6.append(r7);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = r6.toString();	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        android.util.Log.e(r4, r6);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r33 = 0;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r32 = 0;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r38 = java.lang.System.currentTimeMillis();	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4 = com.netspace.library.application.MyiBaseApplication.isDebugOn();	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        if (r4 == 0) goto L_0x064f;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
    L_0x063d:
        r0 = r50;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4 = r0.mDebugFile;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        if (r4 != 0) goto L_0x064f;
    L_0x0643:
        r4 = new java.io.FileOutputStream;	 Catch:{ Exception -> 0x06a8, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = "/sdcard/MJpegDisplayThread.bin";	 Catch:{ Exception -> 0x06a8, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4.<init>(r6);	 Catch:{ Exception -> 0x06a8, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r0 = r50;	 Catch:{ Exception -> 0x06a8, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r0.mDebugFile = r4;	 Catch:{ Exception -> 0x06a8, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
    L_0x064f:
        r0 = r50;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4 = r0.mSurface;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        if (r4 == 0) goto L_0x06ad;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
    L_0x0655:
        r0 = r50;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4 = r0.mDecoder;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r0 = r18;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4.releaseOutputBuffer(r0, r8);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r21 = r22;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        goto L_0x04ce;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
    L_0x0662:
        r33 = r33 + 1;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r0 = r50;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4 = r0.mDecodedFrameData;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        if (r4 != 0) goto L_0x067a;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
    L_0x066a:
        r0 = r50;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4 = r0.mSurface;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        if (r4 != 0) goto L_0x067a;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
    L_0x0670:
        r4 = r17.limit();	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4 = new byte[r4];	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r0 = r50;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r0.mDecodedFrameData = r4;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
    L_0x067a:
        r0 = r50;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4 = r0.mbHasNewFrame;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        if (r4 != 0) goto L_0x059c;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
    L_0x0680:
        r0 = r50;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4 = r0.mSurface;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        if (r4 != 0) goto L_0x059c;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
    L_0x0686:
        r4 = 1;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r0 = r50;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r0.mbHasNewFrame = r4;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r0 = r50;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4 = r0.mDecodedFrameData;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r0 = r17;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r0.get(r4);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        goto L_0x059c;
    L_0x0696:
        r16 = move-exception;
        com.netspace.library.utilities.Utilities.logException(r16);	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = "mDecoder.dequeueOutputBuffer";	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r1 = r16;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r0.handleException(r1, r4);	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r21 = r22;
        goto L_0x04ce;
    L_0x06a8:
        r16 = move-exception;
        r16.printStackTrace();	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        goto L_0x064f;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
    L_0x06ad:
        r0 = r50;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4 = r0.mDecoder;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r6 = 0;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r0 = r18;	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r4.releaseOutputBuffer(r0, r6);	 Catch:{ Exception -> 0x0696, ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e }
        r21 = r22;
        goto L_0x04ce;
    L_0x06bb:
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = r0.mDecodePartImage;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        if (r4 != 0) goto L_0x06fe;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x06c1:
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = r0.m_bPauseDisplay;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = r4.booleanValue();	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r19 = r12.readMjpegFrame(r4);	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x06cd:
        if (r19 == 0) goto L_0x081f;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x06cf:
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = r0.mDecodePartImage;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        if (r4 == 0) goto L_0x081f;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x06d5:
        r4 = r12.getIsPartImage();	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        if (r4 != 0) goto L_0x0712;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x06db:
        if (r20 != 0) goto L_0x0704;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x06dd:
        r4 = 0;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r6 = 0;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r7 = r19.getWidth();	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r10 = r19.getHeight();	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r0 = r19;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r20 = com.netspace.library.utilities.Utilities.cloneBitmap(r0, r4, r6, r7, r10);	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r21 = new android.graphics.Canvas;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r21.<init>();	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r0 = r21;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r1 = r20;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0.setBitmap(r1);	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        goto L_0x04ce;
    L_0x06fb:
        r16 = move-exception;
        goto L_0x04f2;
    L_0x06fe:
        r4 = 0;
        r19 = r12.readMjpegFrame(r4);	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        goto L_0x06cd;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x0704:
        r4 = 0;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r6 = 0;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r7 = 0;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r0 = r22;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r1 = r19;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r0.drawBitmap(r1, r4, r6, r7);	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r21 = r22;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        goto L_0x04ce;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x0712:
        if (r20 == 0) goto L_0x081f;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x0714:
        r4 = r20.isRecycled();	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        if (r4 == 0) goto L_0x0723;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x071a:
        r4 = "MJpegDisplayThread";	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r6 = "fullImageBitmap is recycled.";	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        android.util.Log.e(r4, r6);	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x0723:
        r4 = r12.getLeftPos();	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r4 = (float) r4;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r6 = r12.getTopPos();	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r6 = (float) r6;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r7 = 0;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r0 = r22;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r1 = r19;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r0.drawBitmap(r1, r4, r6, r7);	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r19.recycle();	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r19 = 0;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r6 = r0.m_bPauseDisplay;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        monitor-enter(r6);	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
        r0 = r50;	 Catch:{ all -> 0x07be }
        r4 = r0.m_bPauseDisplay;	 Catch:{ all -> 0x07be }
        r4 = r4.booleanValue();	 Catch:{ all -> 0x07be }
        if (r4 != 0) goto L_0x07b1;	 Catch:{ all -> 0x07be }
    L_0x0749:
        r35 = r20.getWidth();	 Catch:{ all -> 0x07be }
        r34 = r20.getHeight();	 Catch:{ all -> 0x07be }
        r0 = r50;	 Catch:{ all -> 0x07be }
        r4 = r0.mOutputWidth;	 Catch:{ all -> 0x07be }
        if (r4 <= 0) goto L_0x077b;	 Catch:{ all -> 0x07be }
    L_0x0757:
        r0 = r50;	 Catch:{ all -> 0x07be }
        r4 = r0.mOutputHeight;	 Catch:{ all -> 0x07be }
        if (r4 <= 0) goto L_0x077b;	 Catch:{ all -> 0x07be }
    L_0x075d:
        r0 = r50;	 Catch:{ all -> 0x07be }
        r0 = r0.mOutputHeight;	 Catch:{ all -> 0x07be }
        r28 = r0;	 Catch:{ all -> 0x07be }
        r0 = r50;	 Catch:{ all -> 0x07be }
        r0 = r0.mOutputWidth;	 Catch:{ all -> 0x07be }
        r31 = r0;	 Catch:{ all -> 0x07be }
        r26 = r34;	 Catch:{ all -> 0x07be }
        r27 = r35;	 Catch:{ all -> 0x07be }
        r4 = r28 / r26;	 Catch:{ all -> 0x07be }
        r7 = r31 / r27;	 Catch:{ all -> 0x07be }
        if (r4 > r7) goto L_0x07b6;	 Catch:{ all -> 0x07be }
    L_0x0773:
        r4 = r27 * r28;	 Catch:{ all -> 0x07be }
        r31 = r4 / r26;	 Catch:{ all -> 0x07be }
    L_0x0777:
        r35 = r31;	 Catch:{ all -> 0x07be }
        r34 = r28;	 Catch:{ all -> 0x07be }
    L_0x077b:
        r0 = r50;	 Catch:{ all -> 0x07be }
        r4 = r0.m_bNoReallocBitmap;	 Catch:{ all -> 0x07be }
        if (r4 != 0) goto L_0x07bb;	 Catch:{ all -> 0x07be }
    L_0x0781:
        r40 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x07be }
        r4 = 0;	 Catch:{ all -> 0x07be }
        r7 = 0;	 Catch:{ all -> 0x07be }
        r0 = r20;	 Catch:{ all -> 0x07be }
        r1 = r35;	 Catch:{ all -> 0x07be }
        r2 = r34;	 Catch:{ all -> 0x07be }
        r19 = com.netspace.library.utilities.Utilities.cloneBitmap(r0, r4, r7, r1, r2);	 Catch:{ all -> 0x07be }
        r4 = "MJpegDisplayThread";	 Catch:{ all -> 0x07be }
        r7 = new java.lang.StringBuilder;	 Catch:{ all -> 0x07be }
        r10 = "Clone time cost=";	 Catch:{ all -> 0x07be }
        r7.<init>(r10);	 Catch:{ all -> 0x07be }
        r48 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x07be }
        r48 = r48 - r40;	 Catch:{ all -> 0x07be }
        r10 = java.lang.String.valueOf(r48);	 Catch:{ all -> 0x07be }
        r7 = r7.append(r10);	 Catch:{ all -> 0x07be }
        r7 = r7.toString();	 Catch:{ all -> 0x07be }
        android.util.Log.i(r4, r7);	 Catch:{ all -> 0x07be }
    L_0x07b1:
        monitor-exit(r6);	 Catch:{ all -> 0x07be }
        r21 = r22;	 Catch:{ all -> 0x07be }
        goto L_0x04ce;	 Catch:{ all -> 0x07be }
    L_0x07b6:
        r4 = r26 * r31;	 Catch:{ all -> 0x07be }
        r28 = r4 / r27;	 Catch:{ all -> 0x07be }
        goto L_0x0777;	 Catch:{ all -> 0x07be }
    L_0x07bb:
        r19 = r20;	 Catch:{ all -> 0x07be }
        goto L_0x07b1;	 Catch:{ all -> 0x07be }
    L_0x07be:
        r4 = move-exception;	 Catch:{ all -> 0x07be }
        monitor-exit(r6);	 Catch:{ all -> 0x07be }
        throw r4;	 Catch:{ ClientProtocolException -> 0x0456, IOException -> 0x0481, IllegalStateException -> 0x049e, Exception -> 0x04ef }
    L_0x07c1:
        if (r19 == 0) goto L_0x081b;
    L_0x07c3:
        r0 = r50;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r6 = r0.m_bPauseDisplay;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        monitor-enter(r6);	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
        r0 = r50;	 Catch:{ all -> 0x080e }
        r4 = r0.m_bPauseDisplay;	 Catch:{ all -> 0x080e }
        r4 = r4.booleanValue();	 Catch:{ all -> 0x080e }
        if (r4 != 0) goto L_0x0800;	 Catch:{ all -> 0x080e }
    L_0x07d2:
        r0 = r50;	 Catch:{ all -> 0x080e }
        r4 = r0.m_Handler;	 Catch:{ all -> 0x080e }
        r11 = r4.get();	 Catch:{ all -> 0x080e }
        r11 = (android.os.Handler) r11;	 Catch:{ all -> 0x080e }
        if (r11 == 0) goto L_0x07f5;	 Catch:{ all -> 0x080e }
    L_0x07de:
        r0 = r50;	 Catch:{ all -> 0x080e }
        r4 = r0.mFrameData;	 Catch:{ all -> 0x080e }
        r0 = r19;	 Catch:{ all -> 0x080e }
        r4.bm = r0;	 Catch:{ all -> 0x080e }
        r0 = r50;	 Catch:{ all -> 0x080e }
        r4 = r0.m_nFrameMsgID;	 Catch:{ all -> 0x080e }
        r0 = r50;	 Catch:{ all -> 0x080e }
        r7 = r0.mFrameData;	 Catch:{ all -> 0x080e }
        r4 = r11.obtainMessage(r4, r7);	 Catch:{ all -> 0x080e }
        r4.sendToTarget();	 Catch:{ all -> 0x080e }
    L_0x07f5:
        r4 = java.lang.Boolean.TRUE;	 Catch:{ all -> 0x080e }
        r0 = r50;	 Catch:{ all -> 0x080e }
        r0.m_bPauseDisplay = r4;	 Catch:{ all -> 0x080e }
        monitor-exit(r6);	 Catch:{ all -> 0x080e }
        r22 = r21;	 Catch:{ all -> 0x080e }
        goto L_0x031b;	 Catch:{ all -> 0x080e }
    L_0x0800:
        r0 = r50;	 Catch:{ all -> 0x080e }
        r4 = r0.m_bNoReallocBitmap;	 Catch:{ all -> 0x080e }
        if (r4 != 0) goto L_0x07f5;	 Catch:{ all -> 0x080e }
    L_0x0806:
        if (r19 == 0) goto L_0x07f5;	 Catch:{ all -> 0x080e }
    L_0x0808:
        r19.recycle();	 Catch:{ all -> 0x080e }
        r19 = 0;	 Catch:{ all -> 0x080e }
        goto L_0x07f5;	 Catch:{ all -> 0x080e }
    L_0x080e:
        r4 = move-exception;	 Catch:{ all -> 0x080e }
        monitor-exit(r6);	 Catch:{ all -> 0x080e }
        throw r4;	 Catch:{ ClientProtocolException -> 0x0174, IOException -> 0x0262, IllegalStateException -> 0x035c, Exception -> 0x06fb }
    L_0x0811:
        r16 = move-exception;
        r16.printStackTrace();
        goto L_0x01ff;
    L_0x0817:
        r21 = r22;
        goto L_0x01b9;
    L_0x081b:
        r22 = r21;
        goto L_0x031b;
    L_0x081f:
        r21 = r22;
        goto L_0x04ce;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.netspace.library.servers.MJpegDisplayThread.run():void");
    }

    private void handleExceptionNoThrow(Exception e, String szField) {
        try {
            handleException(e, szField);
        } catch (Exception e2) {
        }
    }

    private void handleException(Exception e, String szField) throws Exception {
        if (e instanceof IOException) {
            IOException IOException = (IOException) e;
            Log.e(TAG, new StringBuilder(String.valueOf(szField)).append(" failed with IOException.").toString());
            e.printStackTrace();
            if (this.m_CallBack != null) {
                this.m_CallBack.OnMJpegError(new StringBuilder(String.valueOf(szField)).append(e.getMessage()).append("(IOException)").toString());
            }
        } else if (e instanceof CodecException) {
            CodecException CodecException = (CodecException) e;
            Log.e(TAG, new StringBuilder(String.valueOf(szField)).append(" failed with CodecException.").toString());
            e.printStackTrace();
            if (this.m_CallBack != null) {
                String szPrompt = new StringBuilder(String.valueOf(szField)).append(e.getMessage()).append("(CodecException)").toString();
                if (CodecException.isRecoverable()) {
                    szPrompt = new StringBuilder(String.valueOf(szPrompt)).append("(Recoverable)").toString();
                }
                if (CodecException.isTransient()) {
                    szPrompt = new StringBuilder(String.valueOf(szPrompt)).append("(isTransient)").toString();
                }
                this.m_CallBack.OnMJpegError(new StringBuilder(String.valueOf(szPrompt)).append("(").append(CodecException.getDiagnosticInfo()).append(")").toString());
            }
        } else if (e instanceof IllegalArgumentException) {
            Log.e(TAG, new StringBuilder(String.valueOf(szField)).append(" failed with IllegalArgumentException.").toString());
            e.printStackTrace();
            if (this.m_CallBack != null) {
                this.m_CallBack.OnMJpegError(new StringBuilder(String.valueOf(szField)).append(e.getMessage()).append("(IllegalArgumentException)").toString());
            }
        } else if (e instanceof IllegalStateException) {
            IllegalStateException IllegalStateException = (IllegalStateException) e;
            Log.e(TAG, new StringBuilder(String.valueOf(szField)).append(" failed with IllegalStateException.").toString());
            e.printStackTrace();
            if (this.m_CallBack != null) {
                this.m_CallBack.OnMJpegError(new StringBuilder(String.valueOf(szField)).append(e.getMessage()).append("(IllegalStateException)").toString());
            }
        }
        throw e;
    }

    private void createNewThread() {
        if (this.mSurface == null || this.mSurface.isValid()) {
            while (this.m_bWorking.booleanValue()) {
                if (Utilities.isNetworkConnected(MyiBaseApplication.getBaseAppContext())) {
                    Utilities.runOnUIThread(this.mContext, this.mNewThreadRunnable);
                    return;
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
}
