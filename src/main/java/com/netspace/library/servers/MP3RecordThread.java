package com.netspace.library.servers;

import android.content.Context;
import android.media.AudioRecord;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;
import com.netspace.library.restful.provider.device.DeviceOperationRESTServiceProvider;
import com.netspace.library.utilities.Utilities;
import com.uraroji.garage.android.lame.SimpleLame;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MP3RecordThread extends Thread {
    private static final String TAG = "MP3RecordThread";
    private static boolean mbMute = false;
    private boolean mAutoReconnect = true;
    private MP3RecordThreadCallBackInterface mCallBack;
    private int mChannelConfig = 16;
    private int mClientCount;
    private boolean mConnected = false;
    private Context mContext;
    private ByteArrayOutputStream mDataOutputStream = new ByteArrayOutputStream();
    private long mDataTick = 0;
    private volatile Boolean mIsWorking = Boolean.valueOf(true);
    private ListenThread mListenThread;
    private int mLocalPort = 8083;
    private int mSampleRate = 44100;
    private ServerSocket mServerSocket;
    private String mTargetAddress;
    private int mTargetPort;
    private String mTargetURI;
    private ArrayList<WorkThread> mWorkThreads;
    private boolean mbMainThreadStopped = false;

    private class ListenThread extends Thread {
        private ListenThread() {
        }

        public void run() {
            setName("MP3Record Master Thread");
            MP3RecordThread.this.mServerSocket = new ServerSocket(MP3RecordThread.this.mLocalPort);
            new SendTimerOutCheckThread().start();
            if (MP3RecordThread.this.mTargetPort != 0) {
                Log.i(MP3RecordThread.TAG, "Connect to " + MP3RecordThread.this.mTargetAddress + ":" + MP3RecordThread.this.mTargetPort);
                WorkThread WorkThread2 = new WorkThread(MP3RecordThread.this.mTargetAddress, MP3RecordThread.this.mTargetPort, true);
                synchronized (MP3RecordThread.this.mWorkThreads) {
                    MP3RecordThread.this.mWorkThreads.add(WorkThread2);
                }
                WorkThread2.start();
            }
            while (MP3RecordThread.this.mIsWorking.booleanValue()) {
                Socket socket = MP3RecordThread.this.mServerSocket.accept();
                Log.i(MP3RecordThread.TAG, "New connection to :" + socket.getInetAddress());
                WorkThread WorkThread = new WorkThread(socket);
                synchronized (MP3RecordThread.this.mWorkThreads) {
                    MP3RecordThread.this.mWorkThreads.add(WorkThread);
                    MP3RecordThread.this.mClientCount = MP3RecordThread.this.mWorkThreads.size();
                }
                try {
                    WorkThread.start();
                } catch (IOException e) {
                    Log.e(MP3RecordThread.TAG, e.getMessage());
                }
            }
            MP3RecordThread.this.mServerSocket.close();
            Log.i(MP3RecordThread.TAG, "MP3Record Master Thread terminate");
        }
    }

    public interface MP3RecordThreadCallBackInterface {
        void onNewMP3RecordThreadInstance(MP3RecordThread mP3RecordThread);

        void onRecordError();

        void onRecordStart();
    }

    private class SendTimerOutCheckThread extends Thread {
        private SendTimerOutCheckThread() {
        }

        public void run() {
            setName("MP3Record SendTimerOutCheckThread");
            while (MP3RecordThread.this.mIsWorking.booleanValue()) {
                ArrayList<WorkThread> arrStopThreads = new ArrayList();
                synchronized (MP3RecordThread.this.mWorkThreads) {
                    int i = 0;
                    while (i < MP3RecordThread.this.mWorkThreads.size()) {
                        WorkThread OneWorkThread = (WorkThread) MP3RecordThread.this.mWorkThreads.get(i);
                        if (OneWorkThread.IsClosed()) {
                            Log.d(MP3RecordThread.TAG, "Found closed thread. Remove it. ");
                            MP3RecordThread.this.mWorkThreads.remove(i);
                            i--;
                            Log.d(MP3RecordThread.TAG, "Active threads count " + MP3RecordThread.this.mWorkThreads.size());
                        } else if (OneWorkThread.IsSending() && System.currentTimeMillis() - OneWorkThread.GetSendStartTime() > 3000) {
                            arrStopThreads.add(OneWorkThread);
                        }
                        i++;
                    }
                }
                for (i = 0; i < arrStopThreads.size(); i++) {
                    OneWorkThread = (WorkThread) arrStopThreads.get(i);
                    Log.d("MJpegServer", "Send timeout. Close connection " + i);
                    OneWorkThread.Stop();
                }
                MP3RecordThread.this.mClientCount = MP3RecordThread.this.mWorkThreads.size();
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class WorkThread extends Thread {
        private int mPort;
        private String mServerAddress;
        private long m_LastDataTick = 0;
        private Socket m_Socket;
        private DataOutputStream m_Stream;
        private boolean m_bClose = false;
        private boolean m_bSending = false;
        private long m_nSendStartTime = 0;
        private boolean mbConnectMode = false;

        public WorkThread(Socket socket) {
            this.m_Socket = socket;
        }

        public WorkThread(String szTargetAddress, int nTargetPort, boolean bConnectMode) {
            this.mServerAddress = szTargetAddress;
            this.mPort = nTargetPort;
            this.mbConnectMode = bConnectMode;
        }

        public void Stop() {
            this.m_bClose = true;
            Log.d(MP3RecordThread.TAG, "WorkThread Stop.");
            try {
                if (MP3RecordThread.this.mDataOutputStream != null) {
                    MP3RecordThread.this.mDataOutputStream.close();
                }
                if (this.m_Socket != null) {
                    this.m_Socket.close();
                    join();
                }
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
            setName("MP3RecordThread WorkThread");
            boolean bWrite = false;
            try {
                if (this.mbConnectMode) {
                    this.m_Socket = new Socket(this.mServerAddress, this.mPort);
                }
                this.m_Socket.setSoTimeout(DeviceOperationRESTServiceProvider.TIMEOUT);
                this.m_Stream = new DataOutputStream(this.m_Socket.getOutputStream());
                if (this.m_Stream != null) {
                    if (this.mbConnectMode) {
                        this.m_Stream.write(("PUT " + MP3RecordThread.this.mTargetURI + " HTTP/1.1\r\n" + "UserAgent: RuiyiClass\r\n" + "Connection: keep-alive\r\n" + "Content-Type: application/x-www-form-urlencoded\r\n" + "Cache-Control: no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0\r\n" + "Pragma: no-cache\r\n\r\n").getBytes());
                    } else {
                        this.m_Stream.write("HTTP/1.0 200 OK\r\nServer: RuiyiClass\r\nConnection: close\r\nMax-Age: 0\r\nExpires: 0\r\nCache-Control: no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0\r\nPragma: no-cache\r\n\r\n".getBytes());
                    }
                    this.m_Stream.flush();
                    while (!this.m_bClose) {
                        synchronized (MP3RecordThread.this.mDataOutputStream) {
                            if (MP3RecordThread.this.mDataOutputStream.size() > 0 && MP3RecordThread.this.mDataTick != this.m_LastDataTick) {
                                MP3RecordThread.this.mDataOutputStream.writeTo(this.m_Stream);
                                this.m_LastDataTick = MP3RecordThread.this.mDataTick;
                                bWrite = true;
                            }
                        }
                        if (bWrite) {
                            this.m_Stream.flush();
                            this.m_bSending = false;
                            this.m_nSendStartTime = 0;
                        }
                        bWrite = false;
                        if (!this.m_Socket.isConnected() || this.m_Socket.isClosed()) {
                            break;
                        }
                        sleep(10);
                    }
                    this.m_Stream.close();
                    this.m_Socket.close();
                }
            } catch (IOException e) {
                Log.d(MP3RecordThread.TAG, "IOException Error.");
                e.printStackTrace();
            } catch (InterruptedException e2) {
                Log.d(MP3RecordThread.TAG, "InterruptedException Error.");
                e2.printStackTrace();
            }
            if (this.mbConnectMode && !this.m_bClose) {
                Log.e(MP3RecordThread.TAG, "Data send failed. Start another thread to connect to relay server.");
                WorkThread WorkThread = new WorkThread(this.mServerAddress, this.mPort, true);
                synchronized (MP3RecordThread.this.mWorkThreads) {
                    MP3RecordThread.this.mWorkThreads.add(WorkThread);
                }
                WorkThread.start();
            }
            this.m_bClose = true;
            Log.d(MP3RecordThread.TAG, "Work thread terminated.");
        }
    }

    static {
        System.loadLibrary("mp3lame");
    }

    public MP3RecordThread(Context Context, String szTargetAddress, int nPort, String szObjectURI, MP3RecordThreadCallBackInterface CallBack) {
        this.mContext = Context;
        this.mTargetAddress = szTargetAddress;
        this.mTargetPort = nPort;
        this.mTargetURI = szObjectURI;
        this.mIsWorking = Boolean.valueOf(true);
        this.mCallBack = CallBack;
        this.mWorkThreads = new ArrayList();
    }

    public void setLocalPort(int nLocalPort) {
        this.mLocalPort = nLocalPort;
    }

    public void setAutoReconnect(boolean bEnable) {
        this.mAutoReconnect = bEnable;
    }

    public static void setMute(boolean bMute) {
        mbMute = bMute;
    }

    public static boolean getMute() {
        return mbMute;
    }

    public void stopRecord() {
        this.mIsWorking = Boolean.valueOf(false);
        try {
            if (this.mServerSocket != null) {
                this.mServerSocket.close();
            }
            try {
                if (this.mWorkThreads != null) {
                    for (int i = 0; i < this.mWorkThreads.size(); i++) {
                        ((WorkThread) this.mWorkThreads.get(i)).Stop();
                    }
                }
                if (this.mListenThread != null) {
                    this.mListenThread.join();
                }
                if (!this.mbMainThreadStopped) {
                    join(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    private void reconnect() {
        if (this.mAutoReconnect && this.mIsWorking.booleanValue()) {
            Log.d(TAG, "Reconnecting to " + this.mTargetAddress + ". Using new MP3RecordThread instance.");
            MP3RecordThread NewThread = new MP3RecordThread(this.mContext, this.mTargetAddress, this.mTargetPort, this.mTargetURI, this.mCallBack);
            NewThread.start();
            if (this.mCallBack != null) {
                this.mCallBack.onNewMP3RecordThreadInstance(NewThread);
            }
        }
    }

    public void run() {
        setName(TAG);
        Process.setThreadPriority(-19);
        int minBufferSize = AudioRecord.getMinBufferSize(this.mSampleRate, this.mChannelConfig, 2);
        if (minBufferSize < 0) {
            Log.e(TAG, "Format not supported. getMinBufferSize return " + minBufferSize);
            Toast.makeText(this.mContext, "无法开始录音，设备不支持所需要的录音格式。", 0).show();
            if (this.mCallBack != null) {
                this.mCallBack.onRecordError();
                return;
            }
            return;
        }
        AudioRecord audioRecord = new AudioRecord(1, this.mSampleRate, this.mChannelConfig, 2, minBufferSize * 5);
        short[] rawBuffer = new short[(((this.mSampleRate * 2) * 1) * 1)];
        byte[] mp3Buffer = new byte[((int) (7200.0d + (((double) (rawBuffer.length * 2)) * 1.25d)))];
        try {
            int flushResult;
            audioRecord.startRecording();
            if (this.mCallBack != null) {
                this.mCallBack.onRecordStart();
            }
            MP3RecordThread mP3RecordThread = this;
            this.mListenThread = new ListenThread();
            this.mListenThread.start();
            SimpleLame.init(this.mSampleRate, 1, this.mSampleRate, 32);
            int nEncResult = 0;
            while (this.mIsWorking.booleanValue()) {
                int readSize = audioRecord.read(rawBuffer, 0, rawBuffer.length);
                if (readSize < 0) {
                    if (this.mCallBack != null) {
                        this.mCallBack.onRecordError();
                    }
                } else if (readSize != 0) {
                    if (mbMute) {
                        for (int i = 0; i < readSize; i++) {
                            rawBuffer[i] = (short) 0;
                        }
                    }
                    nEncResult = SimpleLame.encode(rawBuffer, rawBuffer, readSize, mp3Buffer);
                    if (nEncResult < 0) {
                        if (this.mCallBack != null) {
                            this.mCallBack.onRecordError();
                        }
                    } else if (nEncResult != 0) {
                        synchronized (this.mDataOutputStream) {
                            this.mDataOutputStream.reset();
                            this.mDataOutputStream.write(mp3Buffer, 0, nEncResult);
                            this.mDataTick = System.currentTimeMillis();
                        }
                    } else {
                        continue;
                    }
                }
                flushResult = SimpleLame.flush(mp3Buffer);
                if (flushResult >= 0 && flushResult != 0) {
                    synchronized (this.mDataOutputStream) {
                        this.mDataOutputStream.reset();
                        this.mDataOutputStream.write(mp3Buffer, 0, nEncResult);
                        this.mDataTick = System.currentTimeMillis();
                    }
                }
                SimpleLame.close();
                audioRecord.stop();
                audioRecord.release();
                this.mbMainThreadStopped = true;
            }
            flushResult = SimpleLame.flush(mp3Buffer);
            synchronized (this.mDataOutputStream) {
                this.mDataOutputStream.reset();
                this.mDataOutputStream.write(mp3Buffer, 0, nEncResult);
                this.mDataTick = System.currentTimeMillis();
            }
            SimpleLame.close();
            audioRecord.stop();
            audioRecord.release();
            this.mbMainThreadStopped = true;
        } catch (IllegalStateException e) {
            Utilities.showToastMessage("无法开始录音，创建录音对象失败。", 0);
            e.printStackTrace();
            Log.e(TAG, "startRecording failed.");
            if (this.mCallBack != null) {
                this.mCallBack.onRecordError();
            }
        }
    }
}
