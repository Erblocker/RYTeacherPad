package com.netspace.library.servers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.wrapper.ScreenRecordPermissionActivity;
import com.netspace.library.wrapper.ScreenRecordPermissionActivity.ScreenRecordPermissionCallBack;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import net.sqlcipher.database.SQLiteDatabase;
import org.apache.http.HttpStatus;

public class MJpegServerH264 extends MJpegServer {
    private static final String MIME_TYPE = "video/avc";
    private static String TAG = "MJpegServerH264";
    private static final boolean VERBOSE = false;
    private final int TIMEOUT_USEC = 10000;
    private String boundary = "---this is a boundary----";
    private int[] mARGBData = null;
    private int mBitRate = 20000000;
    private int mColorFormat = 0;
    private MediaCodec mEncoder = null;
    private ByteBuffer[] mEncoderInputBuffers = null;
    private ByteBuffer[] mEncoderOutputBuffers = null;
    private int mFPS = 15;
    private byte[] mFrameData = null;
    private byte[] mFrameSPSPPSInfo = null;
    private int mGenerateIndex = 0;
    private int mHeight = -1;
    private int mIFrameInterval = HttpStatus.SC_MULTIPLE_CHOICES;
    private Surface mInputSurface;
    private MediaProjection mMediaProjection;
    private int mPendingSendData = 0;
    private ScreenRecordPermissionCallBack mPermissionCallBack = new ScreenRecordPermissionCallBack() {
        public void onSuccess(MediaProjection MediaProjection) {
            Log.d(MJpegServerH264.TAG, "ScreenRecordPermissionCallBack onSuccess");
            ScreenRecordPermissionActivity.setCallBack(null);
            MJpegServerH264.this.mbProjectionPermissionAsked = false;
            MJpegServerH264.this.mMediaProjection = MediaProjection;
            MJpegServerH264.this.prepareEncoder();
        }

        public void onFailure() {
            Log.d(MJpegServerH264.TAG, "ScreenRecordPermissionCallBack onFailure");
            ScreenRecordPermissionActivity.setCallBack(null);
            MJpegServerH264.this.mbProjectionPermissionAsked = false;
            MJpegServerH264.this.mbProjectionPermissionFailed = true;
            Utilities.runOnUIThread(MJpegServerH264.this.m_Context, new Runnable() {
                public void run() {
                    Toast.makeText(MJpegServerH264.this.m_Context, "无法获得屏幕捕获权限，请在出现提示时选择“允许”", 0).show();
                }
            });
        }
    };
    private String mServerAddress = "";
    private int mServerPort = 8003;
    private String mServerURI = "/2bfb365bfa744dd7b7ef50d9f0fe83ca/Master/Display";
    private Date mStartRecordTime;
    private VirtualDisplay mVirtualDisplay;
    private int mWidth = -1;
    private ByteArrayOutputStream m_LastImageOutputStream = new ByteArrayOutputStream();
    private ServerSocket m_Server;
    private Bitmap m_TempBitmap;
    private ContinuesConvertH264Thread m_TempH264Thread;
    private ConvertH264Thread m_TempThread;
    private ArrayList<WorkThread> m_arrWorkThreads = new ArrayList();
    private volatile boolean m_bStop = false;
    private int m_nClientCount = 0;
    private long m_nCurrentImageFrameIndex = 0;
    private long m_nCurrentImageSize = 0;
    private boolean mbEncoderReady = false;
    private boolean mbProjectionPermissionAsked = false;
    private boolean mbProjectionPermissionFailed = false;
    private boolean mbSendToServerMode = false;
    private boolean mbUseMediaProjection = true;

    private class ContinuesConvertH264Thread extends Thread {
        private boolean mbStopConvertThread;

        private ContinuesConvertH264Thread() {
            this.mbStopConvertThread = false;
        }

        private void stopEncoding() {
            this.mbStopConvertThread = true;
        }

        public void run() {
            setName("ContinuesConvertH264Thread");
            BufferInfo info = new BufferInfo();
            byte[] framedata = new byte[65536];
            while (!this.mbStopConvertThread && !MJpegServerH264.this.m_bStop) {
                if (MJpegServerH264.this.mbEncoderReady) {
                    boolean bOutputSPSPPS = false;
                    int encoderStatus = MJpegServerH264.this.mEncoder.dequeueOutputBuffer(info, 10000);
                    if (encoderStatus == -1) {
                        continue;
                    } else if (encoderStatus == -3) {
                        MJpegServerH264.this.mEncoderOutputBuffers = MJpegServerH264.this.mEncoder.getOutputBuffers();
                    } else if (encoderStatus == -2) {
                        MediaFormat newFormat = MJpegServerH264.this.mEncoder.getOutputFormat();
                    } else if (encoderStatus < 0) {
                        Log.i(MJpegServerH264.TAG, "unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
                    } else {
                        ByteBuffer encodedData = MJpegServerH264.this.mEncoderOutputBuffers[encoderStatus];
                        if (encodedData == null) {
                            Log.i(MJpegServerH264.TAG, "encoderOutputBuffer " + encoderStatus + " was null");
                        } else {
                            if (MJpegServerH264.this.mFrameSPSPPSInfo == null) {
                                byte[] outData = new byte[info.size];
                                encodedData.get(outData);
                                if (ByteBuffer.wrap(outData).getInt() == 1) {
                                    MJpegServerH264.this.mFrameSPSPPSInfo = new byte[outData.length];
                                    System.arraycopy(outData, 0, MJpegServerH264.this.mFrameSPSPPSInfo, 0, outData.length);
                                }
                            } else if ((info.flags & 1) == 1) {
                                bOutputSPSPPS = true;
                                Log.i(MJpegServerH264.TAG, "-----------KeyFrame-----------");
                            }
                            encodedData.position(info.offset);
                            encodedData.limit(info.offset + info.size);
                            if (framedata.length < info.size) {
                                framedata = new byte[info.size];
                                Log.i(MJpegServerH264.TAG, "-----Realloc buffer " + info.size + "-----------");
                            }
                            encodedData.get(framedata, 0, info.size);
                            encodedData.position(info.offset);
                            try {
                                synchronized (MJpegServerH264.this.m_arrWorkThreads) {
                                    Iterator it = MJpegServerH264.this.m_arrWorkThreads.iterator();
                                    while (it.hasNext()) {
                                        WorkThread oneThread = (WorkThread) it.next();
                                        if (bOutputSPSPPS) {
                                            oneThread.writeData(MJpegServerH264.this.mFrameSPSPPSInfo, framedata, 0, info.size);
                                        } else {
                                            oneThread.writeData(null, framedata, 0, info.size);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                MJpegServerH264 mJpegServerH264 = MJpegServerH264.this;
                                mJpegServerH264.m_nCurrentImageFrameIndex = mJpegServerH264.m_nCurrentImageFrameIndex + 1;
                                MJpegServerH264.this.m_nCurrentImageSize = (long) MJpegServerH264.this.m_LastImageOutputStream.size();
                            } catch (IllegalStateException e2) {
                                e2.printStackTrace();
                                throw e2;
                            }
                        }
                        MJpegServerH264.this.mEncoder.releaseOutputBuffer(encoderStatus, false);
                    }
                } else {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e3) {
                        e3.printStackTrace();
                    }
                }
            }
            Log.d(MJpegServerH264.TAG, "ContinuesConvertH264Thread Terminated");
            MJpegServerH264.this.m_TempH264Thread = null;
        }
    }

    private class ConvertH264Thread extends Thread {
        private ConvertH264Thread() {
        }

        public void run() {
            setName("ConvertH264Thread");
            MJpegServerH264.this.putFrame(MJpegServerH264.this.m_TempBitmap, false);
            MJpegServerH264.this.m_TempThread = null;
        }
    }

    private class SendTimerOutCheckThread extends Thread {
        private SendTimerOutCheckThread() {
        }

        public void run() {
            setName("SendTimerOutCheckThread");
            while (!MJpegServerH264.this.m_bStop) {
                ArrayList<WorkThread> arrStopThreads = new ArrayList();
                synchronized (MJpegServerH264.this.m_arrWorkThreads) {
                    int i = 0;
                    while (i < MJpegServerH264.this.m_arrWorkThreads.size()) {
                        WorkThread OneWorkThread = (WorkThread) MJpegServerH264.this.m_arrWorkThreads.get(i);
                        if (OneWorkThread.IsClosed()) {
                            Log.d("MJpegServer", "Found closed thread. Remove it. ");
                            MJpegServerH264.this.m_arrWorkThreads.remove(i);
                            i--;
                            Log.d("MJpegServer", "Active threads count " + MJpegServerH264.this.m_arrWorkThreads.size());
                        } else if (OneWorkThread.IsSending() && System.currentTimeMillis() - OneWorkThread.GetSendStartTime() > 3000) {
                            arrStopThreads.add(OneWorkThread);
                        }
                        i++;
                    }
                }
                for (i = 0; i < arrStopThreads.size(); i++) {
                    OneWorkThread = (WorkThread) arrStopThreads.get(i);
                    Log.d(MJpegServerH264.TAG, "Send timeout. Close connection " + i);
                    OneWorkThread.Stop();
                }
                MJpegServerH264.this.m_nClientCount = MJpegServerH264.this.m_arrWorkThreads.size();
                if (MJpegServerH264.this.m_nClientCount == 0) {
                    MJpegServerH264.this.shutdownEncoder();
                }
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class WorkThread extends Thread {
        private ByteArrayOutputStream mDataSendCache = new ByteArrayOutputStream();
        private int mFrameCount = 0;
        private int mIdleCount = 0;
        private int mPort;
        private String mServerAddress;
        private long m_SendImageFrameIndex = 0;
        private Socket m_Socket;
        private DataOutputStream m_Stream;
        private boolean m_bClose = false;
        private boolean m_bSending = false;
        private long m_nSendStartTime = 0;
        private boolean mbConnectMode = false;
        private boolean mbHasDataToSend = false;

        public WorkThread(Socket socket) {
            this.m_Socket = socket;
        }

        public WorkThread(String szServerAddress, int nPort, boolean bConnectMode) {
            this.mServerAddress = szServerAddress;
            this.mPort = nPort;
            this.mbConnectMode = bConnectMode;
        }

        public void writeData(byte[] SPSPPSInfo, byte[] data, int nOffset, int nSize) {
            int nTotalSize = nSize;
            if (SPSPPSInfo != null) {
                nTotalSize += SPSPPSInfo.length;
            }
            synchronized (this.mDataSendCache) {
                try {
                    this.mDataSendCache.write(("Content-type: video/h264\r\nContent-Length: " + nTotalSize + "\r\n" + "ScreenSize: " + String.valueOf(MJpegServerH264.this.mWidth) + "," + String.valueOf(MJpegServerH264.this.mHeight) + "\r\n" + "\r\n").getBytes());
                    if (SPSPPSInfo != null) {
                        this.mDataSendCache.write(SPSPPSInfo);
                    }
                    this.mDataSendCache.write(data, nOffset, nSize);
                    this.mDataSendCache.write(("\r\n--" + MJpegServerH264.this.boundary + "\r\n").getBytes());
                    this.mFrameCount++;
                    this.mbHasDataToSend = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void Stop() {
            this.m_bClose = true;
            Log.d(MJpegServerH264.TAG, "WorkThread Stop.");
            try {
                if (this.m_Socket != null) {
                    this.m_Socket.close();
                }
                join();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
        }

        public long GetSendStartTime() {
            return this.m_nSendStartTime;
        }

        public boolean IsSending() {
            return this.m_bSending;
        }

        public boolean IsClosed() {
            return this.m_bClose;
        }

        public void run() {
            setName("MJpegServerH264 WorkThread");
            try {
                if (this.mbConnectMode) {
                    this.m_Socket = new Socket(this.mServerAddress, this.mPort);
                }
                this.m_Stream = new DataOutputStream(this.m_Socket.getOutputStream());
                if (this.m_Stream != null) {
                    if (this.mbConnectMode) {
                        this.m_Stream.write(("PUT " + MJpegServerH264.this.mServerURI + " HTTP/1.1\r\n" + "Server: MJpegServer\r\n" + "Connection: close\r\n" + "Max-Age: 0\r\n" + "Expires: 0\r\n" + "Cache-Control: no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0\r\n" + "Pragma: no-cache\r\n" + "ScreenSize: " + String.valueOf(MJpegServerH264.this.mWidth) + "," + String.valueOf(MJpegServerH264.this.mHeight) + "\r\n" + "Content-Type: video/h264; multipart/x-mixed-replace; " + "boundary=" + MJpegServerH264.this.boundary + "\r\n" + "\r\n" + "--" + MJpegServerH264.this.boundary + "\r\n").getBytes());
                    } else {
                        this.m_Stream.write(("HTTP/1.0 200 OK\r\nServer: MJpegServer\r\nConnection: close\r\nMax-Age: 0\r\nExpires: 0\r\nCache-Control: no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0\r\nPragma: no-cache\r\nScreenSize: " + String.valueOf(MJpegServerH264.this.mWidth) + "," + String.valueOf(MJpegServerH264.this.mHeight) + "\r\n" + "Content-Type: video/h264; multipart/x-mixed-replace; " + "boundary=" + MJpegServerH264.this.boundary + "\r\n" + "\r\n" + "--" + MJpegServerH264.this.boundary + "\r\n").getBytes());
                    }
                    this.m_Stream.flush();
                    while (!this.m_bClose) {
                        boolean bWrite;
                        if (this.mIdleCount > 50) {
                            this.mIdleCount = 0;
                        }
                        if (this.mbHasDataToSend) {
                            this.m_SendImageFrameIndex = MJpegServerH264.this.m_nCurrentImageFrameIndex;
                            this.m_bSending = true;
                            this.m_nSendStartTime = System.currentTimeMillis();
                            synchronized (this.mDataSendCache) {
                                this.mDataSendCache.writeTo(this.m_Stream);
                                Log.d("MJpegServer", "sending " + this.mFrameCount + " frames to pc. Local FrameCount " + this.m_SendImageFrameIndex + " to client. Size " + this.mDataSendCache.size() + " bytes...");
                                bWrite = true;
                                this.mDataSendCache.reset();
                                this.mbHasDataToSend = false;
                                this.mFrameCount = 0;
                            }
                        } else {
                            this.m_Stream.write("Content-type: video/h264\r\nContent-Length: 0\r\n\r\n".getBytes());
                            this.m_Stream.write(("\r\n--" + MJpegServerH264.this.boundary + "\r\n").getBytes());
                            bWrite = true;
                            this.mIdleCount++;
                        }
                        if (bWrite) {
                            this.m_bSending = false;
                            this.m_nSendStartTime = 0;
                        }
                        if (!this.m_Socket.isConnected() || this.m_Socket.isClosed()) {
                            break;
                        }
                        sleep(25);
                    }
                    this.m_Stream.close();
                    this.m_Socket.close();
                }
            } catch (IOException e) {
                Log.d(MJpegServerH264.TAG, "IOException Error.");
                e.printStackTrace();
            } catch (InterruptedException e2) {
                Log.d(MJpegServerH264.TAG, "InterruptedException Error.");
                e2.printStackTrace();
            }
            if (this.mbConnectMode && !MJpegServerH264.this.m_bStop) {
                Log.e(MJpegServerH264.TAG, "Data send failed. Start another thread to connect to relay server.");
                WorkThread WorkThread = new WorkThread(this.mServerAddress, this.mPort, true);
                synchronized (MJpegServerH264.this.m_arrWorkThreads) {
                    MJpegServerH264.this.m_arrWorkThreads.add(WorkThread);
                    MJpegServerH264.this.m_nClientCount = MJpegServerH264.this.m_arrWorkThreads.size();
                }
                WorkThread.start();
            }
            this.m_bClose = true;
            Log.d(MJpegServerH264.TAG, "Work thread terminated.");
        }
    }

    public MJpegServerH264(Context Context, int nPort) {
        super(Context, nPort);
    }

    public int getRefreshTime() {
        return 50;
    }

    public boolean isUseMediaProjection() {
        return this.mbUseMediaProjection;
    }

    public void setUseMediaProjection(boolean bEnable) {
        this.mbUseMediaProjection = bEnable;
    }

    public boolean needFeedImage() {
        if (this.mbUseMediaProjection) {
            return false;
        }
        return true;
    }

    public void setRelayServerMode(String szAddress, int nPort, String szURI) {
        this.mServerAddress = szAddress;
        this.mServerPort = nPort;
        this.mServerURI = szURI;
        this.mbSendToServerMode = true;
    }

    public void PostNewImageData(Bitmap bitmap) {
        if (HasClients() && bitmap != null && !this.mbUseMediaProjection && !this.mbPause) {
            if (this.m_TempThread != null) {
                Log.d(TAG, "Convert Bitmap Thread working. Skip a frame.");
                return;
            }
            Log.d(TAG, "get data.");
            if (this.m_TempBitmap != null) {
                this.m_TempBitmap.recycle();
                this.m_TempBitmap = null;
            }
            this.m_TempBitmap = Bitmap.createBitmap(bitmap);
            this.m_TempThread = new ConvertH264Thread();
            this.m_TempThread.start();
        }
    }

    public void CleanImageData() {
        synchronized (this.m_LastImageOutputStream) {
            Log.d("MJpegServer", "CleanImageData");
            this.m_LastImageOutputStream.reset();
        }
    }

    public void PostNewImageData(ByteArrayOutputStream data) {
        if (HasClients()) {
            synchronized (this.m_LastImageOutputStream) {
                Log.d(TAG, "get data.");
                this.m_LastImageOutputStream.reset();
                try {
                    data.writeTo(this.m_LastImageOutputStream);
                    this.m_nCurrentImageFrameIndex = this.m_nCurrentImageSize;
                    this.m_nCurrentImageSize = (long) data.size();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean HasClients() {
        return this.m_nClientCount > 0;
    }

    public void SetSendAlways(boolean bSendAlways) {
    }

    public void SetSendOnlyDiff(boolean bSendDiff) {
    }

    public boolean Stop() {
        this.m_bStop = true;
        for (int i = 0; i < this.m_arrWorkThreads.size(); i++) {
            ((WorkThread) this.m_arrWorkThreads.get(i)).Stop();
        }
        try {
            if (this.m_Server != null) {
                this.m_Server.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean prepareEncoder() {
        this.mWidth = Utilities.getScreenWidth(this.m_Context);
        this.mHeight = Utilities.getScreenHeight(this.m_Context);
        MediaCodecInfo codecInfo = selectCodec(MIME_TYPE);
        if (codecInfo == null) {
            Log.e(TAG, "Unable to find an appropriate codec for video/avc");
            return false;
        }
        this.mColorFormat = selectColorFormat(codecInfo, MIME_TYPE);
        if (this.mColorFormat == 0) {
            Log.e(TAG, "Unable to find an appropriate colorformat for video/avc");
            return false;
        }
        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, this.mWidth, this.mHeight);
        if (this.mbUseMediaProjection) {
            format.setInteger(io.vov.vitamio.MediaFormat.KEY_COLOR_FORMAT, 2130708361);
            format.setInteger("bitrate", this.mBitRate);
            format.setInteger(io.vov.vitamio.MediaFormat.KEY_FRAME_RATE, this.mFPS);
            format.setInteger(io.vov.vitamio.MediaFormat.KEY_I_FRAME_INTERVAL, this.mIFrameInterval);
            format.setLong(io.vov.vitamio.MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 80);
            format.setInteger("profile", 2);
            format.setInteger("level", 1024);
        } else {
            format.setInteger(io.vov.vitamio.MediaFormat.KEY_COLOR_FORMAT, this.mColorFormat);
            format.setInteger("bitrate", this.mBitRate);
            format.setInteger(io.vov.vitamio.MediaFormat.KEY_FRAME_RATE, this.mFPS);
            format.setInteger(io.vov.vitamio.MediaFormat.KEY_I_FRAME_INTERVAL, this.mIFrameInterval);
        }
        try {
            this.mEncoder = MediaCodec.createByCodecName(codecInfo.getName());
            this.mEncoder.configure(format, null, null, 1);
            Bundle bitrate = new Bundle();
            bitrate.putInt("video-bitrate", this.mBitRate);
            this.mEncoder.setParameters(bitrate);
            if (this.mbUseMediaProjection) {
                this.mInputSurface = this.mEncoder.createInputSurface();
            }
            this.mEncoder.start();
            if (this.mbUseMediaProjection) {
                Display display = ((WindowManager) this.m_Context.getSystemService("window")).getDefaultDisplay();
                DisplayMetrics metrics = new DisplayMetrics();
                display.getMetrics(metrics);
                this.mVirtualDisplay = this.mMediaProjection.createVirtualDisplay(getClass().getSimpleName(), this.mWidth, this.mHeight, metrics.densityDpi, 16, this.mInputSurface, null, null);
            }
            this.mbEncoderReady = true;
            this.mEncoderInputBuffers = this.mEncoder.getInputBuffers();
            this.mEncoderOutputBuffers = this.mEncoder.getOutputBuffers();
            this.mFrameData = new byte[(((this.mWidth * this.mHeight) * 3) / 2)];
            this.mStartRecordTime = new Date();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "MediaCodec.createByCodecName failed.");
            e.printStackTrace();
            return false;
        } catch (SecurityException e2) {
            Log.e(TAG, "MediaCodec.createByCodecName failed with SecurityException.");
            e2.printStackTrace();
            return false;
        }
    }

    private void shutdownEncoder() {
        if (this.m_TempH264Thread != null) {
            this.m_TempH264Thread.stopEncoding();
        }
        while (this.m_TempH264Thread != null) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (this.mEncoder != null) {
            Log.d(TAG, "Shutdown H264 Encoder.");
            while (this.m_TempThread != null) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
            }
            this.mEncoder.stop();
            this.mEncoder.release();
            this.mEncoder = null;
            this.mbEncoderReady = false;
            this.mEncoderInputBuffers = null;
            this.mEncoderOutputBuffers = null;
            this.mFrameData = null;
            if (this.mVirtualDisplay != null) {
                this.mVirtualDisplay.release();
                this.mVirtualDisplay = null;
            }
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
            if (colorFormat == 21 || colorFormat == 19) {
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
                return true;
            default:
                return false;
        }
    }

    protected void putFrame(Bitmap b, boolean bLastFrame) {
        boolean bReallocCache = false;
        if (this.mWidth != b.getWidth()) {
            this.mWidth = b.getWidth();
            bReallocCache = true;
        }
        if (this.mHeight != b.getHeight()) {
            this.mHeight = b.getHeight();
            bReallocCache = true;
        }
        if (!(this.mWidth % 16 == 0 && this.mHeight % 16 == 0)) {
            Log.w(TAG, "WARNING: width or height not multiple of 16");
        }
        if (bReallocCache || this.mARGBData == null) {
            this.mARGBData = new int[(this.mWidth * this.mHeight)];
        }
        b.getPixels(this.mARGBData, 0, this.mWidth, 0, 0, this.mWidth, this.mHeight);
        BufferInfo info = new BufferInfo();
        Date currentDate = new Date();
        try {
            int inputBufIndex = this.mEncoder.dequeueInputBuffer(10000);
            if (inputBufIndex >= 0) {
                long ptsUsec = (currentDate.getTime() - this.mStartRecordTime.getTime()) * 1000;
                if (bLastFrame) {
                    this.mEncoder.queueInputBuffer(inputBufIndex, 0, 0, ptsUsec, 4);
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
                }
                this.mGenerateIndex++;
            }
            boolean bOutputSPSPPS = false;
            try {
                int encoderStatus = this.mEncoder.dequeueOutputBuffer(info, 10000);
                if (encoderStatus != -1) {
                    if (encoderStatus == -3) {
                        this.mEncoderOutputBuffers = this.mEncoder.getOutputBuffers();
                    } else if (encoderStatus == -2) {
                        MediaFormat newFormat = this.mEncoder.getOutputFormat();
                    } else if (encoderStatus < 0) {
                        Log.i(TAG, "unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
                    } else {
                        ByteBuffer encodedData = this.mEncoderOutputBuffers[encoderStatus];
                        if (encodedData == null) {
                            Log.i(TAG, "encoderOutputBuffer " + encoderStatus + " was null");
                        } else {
                            if (this.mFrameSPSPPSInfo == null) {
                                Object outData = new byte[info.size];
                                encodedData.get(outData);
                                if (ByteBuffer.wrap(outData).getInt() == 1) {
                                    this.mFrameSPSPPSInfo = new byte[outData.length];
                                    System.arraycopy(outData, 0, this.mFrameSPSPPSInfo, 0, outData.length);
                                }
                            } else if ((info.flags & 1) == 1) {
                                bOutputSPSPPS = true;
                                Log.i(TAG, "-----------KeyFrame-----------");
                            }
                            encodedData.position(info.offset);
                            encodedData.limit(info.offset + info.size);
                            byte[] data = new byte[info.size];
                            encodedData.get(data);
                            encodedData.position(info.offset);
                            synchronized (this.m_LastImageOutputStream) {
                                System.currentTimeMillis();
                                this.m_LastImageOutputStream.reset();
                                if (bOutputSPSPPS) {
                                    try {
                                        this.m_LastImageOutputStream.write(this.mFrameSPSPPSInfo);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                this.m_LastImageOutputStream.write(data);
                                this.m_nCurrentImageFrameIndex = (long) this.mGenerateIndex;
                                this.m_nCurrentImageSize = (long) this.m_LastImageOutputStream.size();
                            }
                        }
                        this.mEncoder.releaseOutputBuffer(encoderStatus, false);
                    }
                }
            } catch (IllegalStateException e2) {
                Toast.makeText(this.m_Context, "dequeueOutputBuffer 出现错误", 0).show();
                e2.printStackTrace();
                throw e2;
            }
        } catch (IllegalStateException e22) {
            Toast.makeText(this.m_Context, "putFrame dequeue 出现错误", 0).show();
            e22.printStackTrace();
            throw e22;
        }
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        WorkThread WorkThread;
        setName("MJpegServerH264 Master Thread");
        this.m_Server = new ServerSocket(this.m_nPort);
        new SendTimerOutCheckThread().start();
        if (this.mbSendToServerMode) {
            WorkThread = new WorkThread(this.mServerAddress, this.mServerPort, true);
            synchronized (this.m_arrWorkThreads) {
                this.m_arrWorkThreads.add(WorkThread);
                this.m_nClientCount = this.m_arrWorkThreads.size();
            }
            WorkThread.start();
        }
        while (!this.m_bStop) {
            Socket socket = this.m_Server.accept();
            if (this.mEncoder == null) {
                this.mbProjectionPermissionFailed = false;
                if (this.mbUseMediaProjection && this.mMediaProjection == null && !this.mbProjectionPermissionAsked) {
                    Intent intent2 = new Intent(this.m_Context, ScreenRecordPermissionActivity.class);
                    ScreenRecordPermissionActivity.setCallBack(this.mPermissionCallBack);
                    Activity activity = UI.getCurrentActivity();
                    if (activity == null) {
                        intent2.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
                        this.m_Context.startActivity(intent2);
                    } else {
                        intent2.setFlags(67108864);
                        activity.startActivity(intent2);
                    }
                    Log.d(TAG, "Permission Asked.");
                    this.mbProjectionPermissionAsked = true;
                }
                if (this.mbUseMediaProjection) {
                    if (this.mMediaProjection != null && this.mEncoder == null) {
                        prepareEncoder();
                    }
                    long nAskStartTime = System.currentTimeMillis();
                    while (!this.m_bStop && (this.mMediaProjection == null || this.mEncoder == null)) {
                        try {
                            Thread.sleep(50, 0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (this.mbProjectionPermissionFailed) {
                            break;
                        } else if (System.currentTimeMillis() - nAskStartTime > 10000) {
                            this.mbProjectionPermissionFailed = true;
                            this.mbProjectionPermissionAsked = false;
                        }
                    }
                    if (this.mbProjectionPermissionFailed) {
                        try {
                            socket.close();
                        } catch (IOException e2) {
                        }
                    } else if (!this.m_bStop && this.m_TempH264Thread == null) {
                        this.m_TempH264Thread = new ContinuesConvertH264Thread();
                        this.m_TempH264Thread.start();
                    }
                } else if (this.mEncoder == null) {
                    prepareEncoder();
                }
            }
            Log.i(TAG, "New connection to :" + socket.getInetAddress());
            WorkThread = new WorkThread(socket);
            synchronized (this.m_arrWorkThreads) {
                this.m_arrWorkThreads.add(WorkThread);
                this.m_nClientCount = this.m_arrWorkThreads.size();
            }
            WorkThread.start();
        }
        this.m_Server.close();
        if (this.mEncoder != null) {
            shutdownEncoder();
        }
    }
}
