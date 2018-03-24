package com.netspace.library.servers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MJpegServer extends Thread {
    private String TAG = "MJpegServer";
    private String boundary = "---this is a boundary----";
    private String mServerAddress = "";
    private int mServerPort = 8003;
    private String mServerURI = "/2bfb365bfa744dd7b7ef50d9f0fe83ca/Master/Display";
    protected Context m_Context;
    private Bitmap m_LastBitmap;
    private ByteArrayOutputStream m_LastImageDiffOutputStream = new ByteArrayOutputStream();
    private ByteArrayOutputStream m_LastImageOutputStream = new ByteArrayOutputStream();
    private ServerSocket m_Server;
    private Bitmap m_TempBitmap;
    private ConvertBmpThread m_TempThread;
    private ArrayList<WorkThread> m_arrWorkThreads;
    private boolean m_bSendAlways = false;
    private boolean m_bSendOnlyDiffArea = false;
    private boolean m_bShiftLastImage = false;
    private volatile boolean m_bStop = false;
    private int m_nClientCount = 0;
    private long m_nCurrentImageSize = 0;
    private long m_nLastImageSize = 0;
    protected int m_nPort = 8080;
    private int m_nWriteCount = 0;
    private Rect m_rectDiff;
    protected boolean mbPause = false;
    private boolean mbSendToServerMode = false;

    private class ConvertBmpThread extends Thread {
        private ConvertBmpThread() {
        }

        public void run() {
            setName("ConvertBmpThread");
            if (MJpegServer.this.m_TempBitmap != null) {
            }
            if (MJpegServer.this.m_LastBitmap != null) {
                Rect rectDiff = MJpegServer.this.ImageCompare(MJpegServer.this.m_TempBitmap, MJpegServer.this.m_LastBitmap);
                if (rectDiff != null && rectDiff.width() > 0 && rectDiff.height() > 0) {
                    MJpegServer.this.m_rectDiff = rectDiff;
                    Bitmap result = Bitmap.createBitmap(MJpegServer.this.m_rectDiff.width(), MJpegServer.this.m_rectDiff.height(), Config.ARGB_8888);
                    new Canvas(result).drawBitmap(MJpegServer.this.m_TempBitmap, MJpegServer.this.m_rectDiff, new Rect(0, 0, MJpegServer.this.m_rectDiff.width(), MJpegServer.this.m_rectDiff.height()), new Paint());
                    MJpegServer.this.m_LastImageDiffOutputStream.reset();
                    result.compress(CompressFormat.JPEG, 75, MJpegServer.this.m_LastImageDiffOutputStream);
                    result.recycle();
                }
            }
            synchronized (MJpegServer.this.m_LastImageOutputStream) {
                long nStartTime = System.currentTimeMillis();
                MJpegServer.this.m_nWriteCount = 0;
                MJpegServer.this.m_LastImageOutputStream.reset();
                MJpegServer.this.m_TempBitmap.compress(CompressFormat.JPEG, 75, MJpegServer.this.m_LastImageOutputStream);
                if (!MJpegServer.this.m_bSendOnlyDiffArea) {
                    MJpegServer.this.m_TempBitmap.recycle();
                    MJpegServer.this.m_TempBitmap = null;
                }
                MJpegServer.this.m_nLastImageSize = MJpegServer.this.m_nCurrentImageSize;
                MJpegServer.this.m_nCurrentImageSize = (long) MJpegServer.this.m_LastImageOutputStream.size();
                Log.d("MJpegServer", "compress time " + String.valueOf(System.currentTimeMillis() - nStartTime));
            }
            MJpegServer.this.m_TempThread = null;
        }
    }

    private class SendTimerOutCheckThread extends Thread {
        private SendTimerOutCheckThread() {
        }

        public void run() {
            setName("SendTimerOutCheckThread");
            while (!MJpegServer.this.m_bStop) {
                ArrayList<WorkThread> arrStopThreads = new ArrayList();
                synchronized (MJpegServer.this.m_arrWorkThreads) {
                    int i = 0;
                    while (i < MJpegServer.this.m_arrWorkThreads.size()) {
                        WorkThread OneWorkThread = (WorkThread) MJpegServer.this.m_arrWorkThreads.get(i);
                        if (OneWorkThread.IsClosed()) {
                            Log.d("MJpegServer", "Found closed thread. Remove it. ");
                            MJpegServer.this.m_arrWorkThreads.remove(i);
                            i--;
                            Log.d("MJpegServer", "Active threads count " + MJpegServer.this.m_arrWorkThreads.size());
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
                MJpegServer.this.m_nClientCount = MJpegServer.this.m_arrWorkThreads.size();
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class WorkThread extends Thread {
        private int mIdleCount = 0;
        private int mPort;
        private String mServerAddress;
        private long m_SendImageSize = 0;
        private Socket m_Socket;
        private DataOutputStream m_Stream;
        private boolean m_bClose = false;
        private boolean m_bFirstFrame = true;
        private boolean m_bSending = false;
        private long m_nSendStartTime = 0;
        private boolean mbConnectMode = false;

        public WorkThread(Socket socket) {
            this.m_Socket = socket;
        }

        public WorkThread(String szServerAddress, int nPort, boolean bConnectMode) {
            this.mServerAddress = szServerAddress;
            this.mPort = nPort;
            this.mbConnectMode = bConnectMode;
        }

        public void Stop() {
            this.m_bClose = true;
            Log.d("MJpegServer", "WorkThread Stop.");
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
            setName("MJpegServer WorkThread");
            try {
                if (this.mbConnectMode) {
                    this.m_Socket = new Socket(this.mServerAddress, this.mPort);
                }
                this.m_Stream = new DataOutputStream(this.m_Socket.getOutputStream());
                if (this.m_Stream != null) {
                    if (this.mbConnectMode) {
                        this.m_Stream.write(("PUT " + MJpegServer.this.mServerURI + " HTTP/1.1\r\n" + "Server: MJpegServer\r\n" + "Connection: close\r\n" + "Max-Age: 0\r\n" + "Expires: 0\r\n" + "Cache-Control: no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0\r\n" + "Pragma: no-cache\r\n" + "Content-Type: multipart/x-mixed-replace; " + "boundary=" + MJpegServer.this.boundary + "\r\n" + "\r\n" + "--" + MJpegServer.this.boundary + "\r\n").getBytes());
                    } else {
                        this.m_Stream.write(("HTTP/1.0 200 OK\r\nServer: MJpegServer\r\nConnection: close\r\nMax-Age: 0\r\nExpires: 0\r\nCache-Control: no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0\r\nPragma: no-cache\r\nContent-Type: multipart/x-mixed-replace; boundary=" + MJpegServer.this.boundary + "\r\n" + "\r\n" + "--" + MJpegServer.this.boundary + "\r\n").getBytes());
                    }
                    this.m_Stream.flush();
                    while (!this.m_bClose) {
                        boolean bWrite;
                        synchronized (MJpegServer.this.m_LastImageOutputStream) {
                            if (this.mIdleCount > 50) {
                                this.mIdleCount = 0;
                                this.m_bFirstFrame = true;
                            }
                            if ((MJpegServer.this.m_LastImageOutputStream.size() <= 0 || (this.m_SendImageSize == MJpegServer.this.m_nLastImageSize && !this.m_bFirstFrame)) && !MJpegServer.this.m_bSendAlways) {
                                this.m_Stream.write("Content-type: image/jpeg\r\nContent-Length: 0\r\n\r\n".getBytes());
                                this.m_Stream.write(("\r\n--" + MJpegServer.this.boundary + "\r\n").getBytes());
                                bWrite = true;
                                this.mIdleCount++;
                            } else {
                                this.m_bSending = true;
                                this.m_nSendStartTime = System.currentTimeMillis();
                                boolean bSendFullFrame = false;
                                if (this.m_bFirstFrame) {
                                    bSendFullFrame = true;
                                }
                                if (!MJpegServer.this.m_bSendOnlyDiffArea) {
                                    bSendFullFrame = true;
                                }
                                if (bSendFullFrame) {
                                    this.m_Stream.write(("Content-type: image/jpeg\r\nContent-Length: " + MJpegServer.this.m_LastImageOutputStream.size() + "\r\n" + "\r\n").getBytes());
                                    MJpegServer.this.m_LastImageOutputStream.writeTo(this.m_Stream);
                                    this.m_Stream.write(("\r\n--" + MJpegServer.this.boundary + "\r\n").getBytes());
                                    Log.d("MJpegServer", "send full data.");
                                    bWrite = true;
                                    this.m_SendImageSize = MJpegServer.this.m_nLastImageSize;
                                    this.m_bFirstFrame = false;
                                    MJpegServer.this.m_bShiftLastImage = true;
                                } else if (MJpegServer.this.m_LastImageDiffOutputStream.size() > 0) {
                                    this.m_Stream.write(("Content-type: image/jpeg\r\nContent-Length: " + MJpegServer.this.m_LastImageDiffOutputStream.size() + "\r\n" + "Position: " + MJpegServer.this.m_rectDiff.left + "," + MJpegServer.this.m_rectDiff.top + "\r\n" + "\r\n").getBytes());
                                    MJpegServer.this.m_LastImageDiffOutputStream.writeTo(this.m_Stream);
                                    this.m_Stream.write(("\r\n--" + MJpegServer.this.boundary + "\r\n").getBytes());
                                    Log.d("MJpegServer", "send part data.");
                                    bWrite = true;
                                    this.m_SendImageSize = MJpegServer.this.m_nLastImageSize;
                                    MJpegServer.this.m_bShiftLastImage = true;
                                    this.mIdleCount = 0;
                                } else {
                                    this.m_Stream.write("Content-type: image/jpeg\r\nContent-Length: 0\r\n\r\n".getBytes());
                                    this.m_Stream.write(("\r\n--" + MJpegServer.this.boundary + "\r\n").getBytes());
                                    bWrite = true;
                                    this.mIdleCount++;
                                }
                            }
                        }
                        if (bWrite) {
                            this.m_Stream.flush();
                            this.m_bSending = false;
                            this.m_nSendStartTime = 0;
                        }
                        if (!this.m_Socket.isConnected() || this.m_Socket.isClosed()) {
                            break;
                        }
                        sleep(50);
                    }
                    this.m_Stream.close();
                    this.m_Socket.close();
                }
            } catch (IOException e) {
                Log.d("MJpegServer", "IOException Error.");
                e.printStackTrace();
            } catch (InterruptedException e2) {
                Log.d("MJpegServer", "InterruptedException Error.");
                e2.printStackTrace();
            }
            if (this.mbConnectMode && !MJpegServer.this.m_bStop) {
                Log.e("MJpegServer", "Data send failed. Start another thread to connect to relay server.");
                WorkThread WorkThread = new WorkThread(this.mServerAddress, this.mPort, true);
                synchronized (MJpegServer.this.m_arrWorkThreads) {
                    MJpegServer.this.m_arrWorkThreads.add(WorkThread);
                    MJpegServer.this.m_nClientCount = MJpegServer.this.m_arrWorkThreads.size();
                }
                WorkThread.start();
            }
            this.m_bClose = true;
            Log.d("MJpegServer", "Work thread terminated.");
        }
    }

    public MJpegServer(Context Context, int nPort) {
        this.m_Context = Context;
        this.m_arrWorkThreads = new ArrayList();
        this.m_nPort = nPort;
    }

    public int getRefreshTime() {
        return 500;
    }

    public void setPause(boolean bPause) {
        this.mbPause = bPause;
    }

    public void setRelayServerMode(String szAddress, int nPort, String szURI) {
        this.mServerAddress = szAddress;
        this.mServerPort = nPort;
        this.mServerURI = szURI;
        this.mbSendToServerMode = true;
    }

    public boolean needFeedImage() {
        return true;
    }

    public void PostNewImageData(Bitmap bitmap) {
        if (HasClients() && bitmap != null && !this.mbPause) {
            if (this.m_TempThread != null) {
                Log.d("MJpegServer", "Convert Bitmap Thread working. Skip a frame.");
                return;
            }
            Log.d("MJpegServer", "get data.");
            if (this.m_bSendOnlyDiffArea) {
                if (this.m_bShiftLastImage) {
                    if (this.m_LastBitmap != null) {
                        this.m_LastBitmap.recycle();
                        this.m_LastBitmap = null;
                    }
                    this.m_LastBitmap = this.m_TempBitmap;
                    this.m_TempBitmap = null;
                    this.m_bShiftLastImage = false;
                }
            } else if (this.m_TempBitmap != null) {
                this.m_TempBitmap.recycle();
                this.m_TempBitmap = null;
            }
            this.m_TempBitmap = Bitmap.createBitmap(bitmap);
            this.m_TempThread = new ConvertBmpThread();
            this.m_TempThread.start();
        }
    }

    public void CleanImageData() {
        synchronized (this.m_LastImageOutputStream) {
            Log.d("MJpegServer", "CleanImageData");
            this.m_nWriteCount = 0;
            this.m_LastImageOutputStream.reset();
        }
    }

    public void PostNewImageData(ByteArrayOutputStream data) {
        if (HasClients() && !this.mbPause) {
            synchronized (this.m_LastImageOutputStream) {
                Log.d("MJpegServer", "get data.");
                this.m_nWriteCount = 0;
                this.m_LastImageOutputStream.reset();
                try {
                    data.writeTo(this.m_LastImageOutputStream);
                    this.m_nLastImageSize = this.m_nCurrentImageSize;
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
        this.m_bSendAlways = bSendAlways;
    }

    public void SetSendOnlyDiff(boolean bSendDiff) {
        this.m_bSendOnlyDiffArea = bSendDiff;
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

    public void run() {
        WorkThread WorkThread;
        setName("MJpegServer Master Thread");
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
            try {
                Socket socket = this.m_Server.accept();
                Log.i(this.TAG, "New connection to :" + socket.getInetAddress());
                WorkThread = new WorkThread(socket);
                synchronized (this.m_arrWorkThreads) {
                    this.m_arrWorkThreads.add(WorkThread);
                    this.m_nClientCount = this.m_arrWorkThreads.size();
                }
                WorkThread.start();
            } catch (IOException e) {
                Log.e(this.TAG, e.getMessage());
                return;
            }
        }
        this.m_Server.close();
    }

    public Rect ImageCompare(Bitmap SourceBitmap, Bitmap DestinBitmap) {
        long nStartTime = System.currentTimeMillis();
        int width = SourceBitmap.getWidth();
        int height = SourceBitmap.getHeight();
        if (DestinBitmap == null || DestinBitmap.getWidth() != width || DestinBitmap.getHeight() != height) {
            return null;
        }
        boolean bFoundDiff = false;
        int[] pixelSource = new int[width];
        int[] pixelDestin = new int[width];
        int nTop = height;
        int nBottom = 0;
        int nLeft = width;
        int nRight = 0;
        for (int i = 0; i < height; i++) {
            SourceBitmap.getPixels(pixelSource, 0, width, 0, i, width, 1);
            DestinBitmap.getPixels(pixelDestin, 0, width, 0, i, width, 1);
            for (int j = 0; j < width; j++) {
                if (pixelSource[j] != pixelDestin[j]) {
                    if (nTop > i) {
                        nTop = i;
                    }
                    if (nBottom < i) {
                        nBottom = i;
                    }
                    if (nLeft > j) {
                        nLeft = j;
                    }
                    if (nRight < j) {
                        nRight = j;
                    }
                    bFoundDiff = true;
                }
            }
        }
        Log.d("ImageCompare", "TimeCost:" + (System.currentTimeMillis() - nStartTime) + "ms.");
        if (bFoundDiff) {
            Log.d("ImageCompare", "Rect:  " + nLeft + "," + nTop + "," + nRight + "," + nBottom);
        } else {
            Log.d("ImageCompare", "No Diff found.");
            nLeft = 0;
            nTop = 0;
            nRight = 0;
            nBottom = 0;
        }
        pixelSource = null;
        pixelDestin = null;
        return new Rect(nLeft, nTop, nRight, nBottom);
    }
}
