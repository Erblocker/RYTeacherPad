package com.netspace.library.threads;

import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.im.IMService;
import com.netspace.library.restful.provider.device.DeviceOperationRESTServiceProvider;
import com.netspace.library.utilities.Timeout;
import com.netspace.library.utilities.Timeout.OnTimeoutCallBack;
import com.netspace.library.utilities.Utilities;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import org.apache.http.protocol.HTTP;

public class DirectIMClientThread extends Thread {
    private static final int PORT = 50008;
    private String TAG = "DirectIMClientThread";
    private DirectIMDataReceiveCallBack mCallBack;
    private long mConnectTime = 0;
    private long mLastSendDataTick = 0;
    private OutputStream mOutputStream;
    private String mSendHead;
    private Socket mSocket;
    private String mTargetClientID;
    private String mTargetIP;
    private boolean mbAutoReconnect = false;
    private boolean mbConnected = false;
    private boolean mbConnecting = false;
    private volatile boolean mbShutdown = false;
    private int mnPort = PORT;

    public interface DirectIMDataReceiveCallBack {
        void onConnectError();

        void onDataAvailable(String str);
    }

    public void setCallBack(DirectIMDataReceiveCallBack CallBack) {
        this.mCallBack = CallBack;
    }

    public void setAutoReconnect(boolean bEnable) {
        this.mbAutoReconnect = bEnable;
    }

    public String getClientID() {
        return this.mTargetClientID;
    }

    public String getIP() {
        return this.mTargetIP;
    }

    public boolean isReady() {
        return this.mbConnected && !this.mbShutdown;
    }

    public void shutdown() {
        this.mbShutdown = true;
        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public long getActiveTime() {
        return System.currentTimeMillis() - this.mConnectTime;
    }

    public void connect(String szTargetIP, String szClientID) {
        this.mTargetClientID = szClientID;
        this.mTargetIP = szTargetIP;
    }

    public void connect(String szTargetIP, int nPort, String szClientID) {
        this.mTargetClientID = szClientID;
        this.mTargetIP = szTargetIP;
        this.mnPort = nPort;
    }

    public void setConnectHead(String szHead) {
        this.mSendHead = szHead;
    }

    public void run() {
        while (!this.mbShutdown) {
            try {
                setName("DirectIMClientThread for " + this.mTargetClientID);
                this.mbConnecting = true;
                this.mConnectTime = System.currentTimeMillis();
                Log.i(this.TAG, "Connecting to " + this.mTargetIP + " port " + this.mnPort + "...");
                this.mSocket = new Socket();
                this.mSocket.connect(new InetSocketAddress(this.mTargetIP, this.mnPort), DeviceOperationRESTServiceProvider.TIMEOUT);
                this.mSocket.setSoTimeout(DeviceOperationRESTServiceProvider.TIMEOUT);
                this.mSocket.setKeepAlive(true);
                this.mbConnecting = false;
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];
                InputStream inputStream = this.mSocket.getInputStream();
                this.mOutputStream = this.mSocket.getOutputStream();
                if (this.mSendHead != null) {
                    this.mOutputStream.write(this.mSendHead.getBytes("UTF8"));
                }
                do {
                    int bytesRead = inputStream.read(buffer);
                    if (bytesRead == -1) {
                        break;
                    }
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                } while (byteArrayOutputStream.toString(HTTP.UTF_8).indexOf("Hello") == -1);
                this.mbConnected = true;
                int nResetCount = 0;
                ByteArrayOutputStream tempDataBuffer = new ByteArrayOutputStream(1024);
                while (!this.mbShutdown) {
                    boolean bHasRealDataReceived = false;
                    for (int nDataAvailable = inputStream.available(); nDataAvailable > 0; nDataAvailable = inputStream.available()) {
                        nResetCount = 0;
                        int nReadByte = inputStream.read();
                        tempDataBuffer.write(nReadByte);
                        if (nReadByte == 10) {
                            String szOneLine = tempDataBuffer.toString("utf-8");
                            String szOriginalData = szOneLine;
                            if (szOneLine.indexOf("\r\n") != -1) {
                                szOneLine = szOneLine.substring(0, szOneLine.indexOf("\r\n"));
                                Log.d(this.TAG, "Receive " + szOneLine + " from " + this.mTargetIP);
                                if (!szOneLine.equalsIgnoreCase("OK")) {
                                    bHasRealDataReceived = true;
                                    if (this.mCallBack != null) {
                                        this.mCallBack.onDataAvailable(szOneLine.replace("\\r", "\r").replace("\\n", "\n"));
                                    }
                                }
                                tempDataBuffer.close();
                                tempDataBuffer = new ByteArrayOutputStream(1024);
                            }
                        }
                    }
                    if (bHasRealDataReceived) {
                        sendData("OK");
                    }
                    try {
                        Thread.sleep(50);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    nResetCount++;
                    if (nResetCount > 100) {
                        Log.e("DirectIMClient", "Wait for response from DirectIM server timeout. Reconnect.");
                        Utilities.runOnUIThread(MyiBaseApplication.getBaseAppContext(), new Runnable() {
                            public void run() {
                                Toast.makeText(MyiBaseApplication.getBaseAppContext(), "和" + DirectIMClientThread.this.mTargetIP + "的直接连接出现问题，正在尝试重新建立连接...", 0).show();
                            }
                        });
                        if (!this.mbAutoReconnect) {
                            this.mbShutdown = true;
                            if (this.mCallBack != null) {
                                this.mCallBack.onConnectError();
                            } else if (this.mSendHead != null) {
                                IMService.addDirectIMClient(this.mTargetIP, this.mnPort, this.mTargetClientID, this.mSendHead);
                            } else {
                                IMService.addDirectIMClient(this.mTargetIP, this.mTargetClientID);
                            }
                        }
                        this.mbConnected = false;
                        this.mbConnecting = false;
                        if (this.mSocket != null) {
                            this.mSocket.close();
                        }
                        if (!this.mbShutdown && this.mbAutoReconnect) {
                            Thread.sleep(1500);
                        }
                    } else if (System.currentTimeMillis() - this.mLastSendDataTick > 1000) {
                        sendData("Ping");
                    }
                }
            } catch (UnknownHostException e2) {
                this.mbConnecting = false;
                this.mbConnected = false;
                e2.printStackTrace();
                if (this.mCallBack != null) {
                    this.mCallBack.onConnectError();
                }
            } catch (IOException e3) {
                this.mbConnecting = false;
                this.mbConnected = false;
                Log.e("DirectIMClient", "Wait for response from DirectIM meet IOException. Reconnect.");
                if (!this.mbAutoReconnect) {
                    this.mbShutdown = true;
                    if (this.mCallBack != null) {
                        this.mCallBack.onConnectError();
                    } else if (this.mSendHead != null) {
                        IMService.addDirectIMClient(this.mTargetIP, this.mnPort, this.mTargetClientID, this.mSendHead);
                    } else {
                        IMService.addDirectIMClient(this.mTargetIP, this.mTargetClientID);
                    }
                }
            }
            this.mbConnected = false;
            this.mbConnecting = false;
            try {
                if (this.mSocket != null) {
                    this.mSocket.close();
                }
            } catch (IOException e4) {
                e4.printStackTrace();
            }
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e5) {
                e5.printStackTrace();
            }
        }
    }

    public boolean sendData(String szData) {
        try {
            Log.d(this.TAG, "Send " + szData + " to " + this.mTargetIP);
            szData = new StringBuilder(String.valueOf(szData)).append("\r\n").toString();
            final Timeout timeOut = new Timeout(3000);
            timeOut.setCallBack(new OnTimeoutCallBack() {
                public void onTimeout() {
                    Log.e("DirectIMClient", "DirectIM Senddata timeout.");
                    if (!DirectIMClientThread.this.mbAutoReconnect) {
                        DirectIMClientThread.this.mbShutdown = true;
                    }
                    try {
                        if (DirectIMClientThread.this.mSocket != null) {
                            DirectIMClientThread.this.mSocket.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Utilities.runOnUIThread(MyiBaseApplication.getBaseAppContext(), new Runnable() {
                        public void run() {
                            Toast.makeText(MyiBaseApplication.getBaseAppContext(), "发送数据到" + DirectIMClientThread.this.mTargetIP + "时超时，正在尝试重新建立连接...", 0).show();
                        }
                    });
                    if (DirectIMClientThread.this.mCallBack != null) {
                        DirectIMClientThread.this.mCallBack.onConnectError();
                    }
                }
            });
            if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                final String szSendData = szData;
                new Thread() {
                    public void run() {
                        timeOut.start();
                        try {
                            DirectIMClientThread.this.mOutputStream.write(szSendData.getBytes("UTF8"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                        timeOut.stopTimer();
                        DirectIMClientThread.this.mLastSendDataTick = System.currentTimeMillis();
                    }
                }.start();
                return true;
            }
            timeOut.start();
            this.mOutputStream.write(szData.getBytes("UTF8"));
            timeOut.stopTimer();
            this.mLastSendDataTick = System.currentTimeMillis();
            return true;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e2) {
            Log.e("DirectIMClient", "DirectIM Senddata error.");
            if (this.mbAutoReconnect) {
                try {
                    if (this.mSocket != null) {
                        this.mSocket.close();
                    }
                } catch (IOException e3) {
                }
            } else {
                this.mbShutdown = true;
                e2.printStackTrace();
                if (this.mCallBack != null) {
                    this.mCallBack.onConnectError();
                }
            }
            return false;
        }
    }

    public boolean isConnecting() {
        return this.mbConnecting;
    }
}
