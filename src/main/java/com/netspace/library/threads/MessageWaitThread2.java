package com.netspace.library.threads;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.im.IMService;
import com.netspace.library.threads.MessageWaitThread.OnMessageArrivedListener;
import com.netspace.library.utilities.MySSLSocketFactory;
import com.netspace.library.utilities.Utilities;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.protocol.HTTP;

public class MessageWaitThread2 extends MessageWaitThread {
    public static final String IMPASSWORD = "(SImP0s5Word!346";
    private static final String TAG = "MessageWaitThread2";
    private static Toast mErrorToast = null;
    private CloseSocketThread mCloseSocketThread = new CloseSocketThread();
    private int mLastMessageIndex = -1;
    private Socket mSocket;
    private DataOutputStream mStream;

    private class CloseSocketThread extends Thread {
        private CloseSocketThread() {
        }

        public void run() {
            if (MessageWaitThread2.this.mSocket != null) {
                try {
                    MessageWaitThread2.this.mSocket.close();
                    Log.d(MessageWaitThread2.TAG, "Socket closed.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public MessageWaitThread2(Context context, String szWaitURL, OnMessageArrivedListener CallBack) {
        super(context, szWaitURL, CallBack);
    }

    public void stopThread() {
        Log.d(TAG, "request MessageWaitThread to close.");
        this.mbStop = true;
        if (this.mInputStream != null) {
            try {
                this.mInputStream.close();
                if (this.mSocket != null) {
                    this.mCloseSocketThread.start();
                    this.mCloseSocketThread.join();
                }
                join();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
        }
    }

    public void run() {
        URL url;
        setName(TAG);
        try {
            URL url2 = new URL(new StringBuilder(String.valueOf(this.mWaitURL + "&ssid=" + Utilities.getWifiSSID(this.mContext))).append("&ip=").append(Utilities.getWifiIP(this.mContext)).toString());
            Log.d(TAG, "Connecting.");
            byte[] data = new byte[65536];
            try {
                int nFindPos;
                int count;
                String szData;
                if (MyiBaseApplication.isUseSSL()) {
                    try {
                        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                        trustStore.load(null, null);
                        MySSLSocketFactory factory = new MySSLSocketFactory(trustStore);
                        factory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                        this.mSocket = factory.createSocket();
                    } catch (KeyStoreException e) {
                        e.printStackTrace();
                    } catch (KeyManagementException e2) {
                        e2.printStackTrace();
                    } catch (UnrecoverableKeyException e3) {
                        e3.printStackTrace();
                    } catch (NoSuchAlgorithmException e4) {
                        e4.printStackTrace();
                    } catch (IOException e5) {
                        e5.printStackTrace();
                    }
                } else {
                    this.mSocket = new Socket();
                }
                this.mSocket.setReuseAddress(true);
                this.mSocket.setKeepAlive(true);
                this.mSocket.bind(null);
                this.mSocket.connect(new InetSocketAddress(url2.getHost(), url2.getPort()), 2000);
                this.mStream = new DataOutputStream(this.mSocket.getOutputStream());
                this.mInputStream = new DataInputStream(this.mSocket.getInputStream());
                if (this.mStream != null) {
                    this.mStream.write(("GET " + url2.getFile() + " HTTP/1.1\r\n" + "User-Agent: MessageWaitThread2\r\n" + "Host: " + url2.getHost() + ":" + String.valueOf(url2.getPort()) + "\r\n" + "Connection: Keep-Alive\r\n" + "\r\n").getBytes());
                }
                this.mStream.flush();
                String szHttpHead = "";
                String szRemainData = "";
                do {
                    count = this.mInputStream.read(data);
                    if (count == -1) {
                        break;
                    }
                    this.mbConnected = true;
                    szData = new String(data, 0, count, HTTP.UTF_8);
                    Log.d(TAG, "Receive data form server: " + szData);
                    szHttpHead = new StringBuilder(String.valueOf(szHttpHead)).append(szData).toString();
                    nFindPos = szHttpHead.indexOf("\r\n\r\n");
                } while (nFindPos == -1);
                szRemainData = szHttpHead.substring(nFindPos + 4);
                if (szHttpHead.indexOf("200 OK") == -1) {
                    this.mbConnected = false;
                    this.mHandler.post(new Runnable() {
                        public void run() {
                            if (MessageWaitThread2.mErrorToast != null) {
                                MessageWaitThread2.mErrorToast.cancel();
                                MessageWaitThread2.mErrorToast = null;
                            }
                            if (MessageWaitThread2.mErrorToast == null) {
                                MessageWaitThread2.mErrorToast = Toast.makeText(MessageWaitThread2.this.mContext, "消息系统连接出现异常，服务器返回的响应数据无效，将自动重连", 0);
                                MessageWaitThread2.mErrorToast.show();
                            }
                        }
                    });
                    if (this.mCallBack != null) {
                        this.mCallBack.OnMessageError();
                    }
                    url = url2;
                    return;
                }
                this.mHandler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(MessageWaitThread2.this.mContext, "消息系统已连接", 0).show();
                        if (MessageWaitThread2.this.mCallBack != null) {
                            MessageWaitThread2.this.mCallBack.OnConnected();
                        }
                        IMService.resendMissingMessage();
                    }
                });
                do {
                    count = this.mInputStream.read(data);
                    if (count == -1) {
                        break;
                    }
                    szData = new String(data, 0, count, HTTP.UTF_8);
                    if (!(szData.isEmpty() || szData.equalsIgnoreCase("\r\n") || szData.indexOf("System: ReportIPResult") != -1)) {
                        Log.d(TAG, "Receive data form server: \n---\n" + szData + "\n---");
                    }
                    szData = new StringBuilder(String.valueOf(szRemainData)).append(szData).toString();
                    nFindPos = szData.lastIndexOf("\r\n");
                    if (nFindPos != -1) {
                        nFindPos += 2;
                        szRemainData = szData.substring(nFindPos);
                        szData = szData.substring(0, nFindPos);
                    } else {
                        szRemainData = szData;
                        szData = "";
                    }
                    String[] arrData = szData.split("\r\n");
                    int nMessageID = -1;
                    for (String szData2 : arrData) {
                        if (szData2.startsWith("ID:")) {
                            String[] dataParts = szData2.split(",");
                            if (dataParts.length >= 2) {
                                nMessageID = Utilities.toInt(dataParts[0].substring(3));
                                szData2 = dataParts[1];
                            }
                        }
                        if (szData2.startsWith("ENC:")) {
                            szData2 = Utilities.decryptBase64StringZeroPadding(szData2.substring(4), Utilities.md5(new StringBuilder(IMPASSWORD).append(MyiBaseApplication.getCommonVariables().Session.getSessionID()).toString()));
                            if (szData2 == null) {
                                Log.e(TAG, "Message " + nMessageID + "+ decryption error. Ignore this message. ");
                            } else {
                                Log.i(TAG, "Receive encrypted message with id = " + nMessageID + " and successful decrypted.  Content is " + szData2);
                            }
                        }
                        if (nMessageID != -1) {
                            if (this.mLastMessageIndex == -1) {
                                this.mLastMessageIndex = nMessageID;
                            } else if (nMessageID > this.mLastMessageIndex) {
                                while (this.mLastMessageIndex + 1 != nMessageID) {
                                    Log.e(TAG, "Message missing, request resend message " + String.valueOf(this.mLastMessageIndex + 1));
                                    IMService.resendMissingMessage(this.mLastMessageIndex + 1);
                                    this.mLastMessageIndex++;
                                }
                                this.mLastMessageIndex = nMessageID;
                            } else {
                                Log.i(TAG, "Receive old missing message with id= " + nMessageID);
                            }
                        }
                        szData2 = szData2.replace("\r\n", "").replace("\\r", "\r").replace("\\n", "\n");
                        if (this.mCallBack != null) {
                            this.mCallBack.OnMessageArrived(szData2);
                        }
                    }
                    if (arrData.length == 0 && this.mCallBack != null) {
                        this.mCallBack.OnMessageArrived("");
                    }
                } while (!this.mbStop);
                Log.d(TAG, "mInputStream.read return -1");
                this.mbConnected = false;
                if (this.mbStop) {
                    url = url2;
                    return;
                }
                if (Utilities.isNetworkConnected(MyiBaseApplication.getBaseAppContext())) {
                    this.mHandler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(MessageWaitThread2.this.mContext, "消息系统连接出现异常，将自动重连", 0).show();
                        }
                    });
                }
                if (this.mCallBack != null) {
                    this.mCallBack.OnMessageError();
                }
                this.mInputStream.close();
                this.mInputStream = null;
                url = url2;
            } catch (SocketTimeoutException e6) {
                Log.d(TAG, "SocketTimeoutException");
                this.mbConnected = false;
                e6.printStackTrace();
                if (this.mbStop) {
                    url = url2;
                    return;
                }
                if (Utilities.isNetworkConnected(MyiBaseApplication.getBaseAppContext())) {
                    this.mHandler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(MessageWaitThread2.this.mContext, "消息系统连接超时，继续重连中", 0).show();
                        }
                    });
                }
                if (this.mCallBack != null) {
                    this.mCallBack.OnMessageError();
                }
            } catch (IOException e52) {
                Log.d(TAG, "IOException");
                this.mbConnected = false;
                e52.printStackTrace();
                if (this.mbStop) {
                    url = url2;
                    return;
                }
                if (Utilities.isNetworkConnected(MyiBaseApplication.getBaseAppContext())) {
                    this.mHandler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(MessageWaitThread2.this.mContext, "消息系统连接出现异常，将自动重连", 0).show();
                        }
                    });
                }
                if (this.mCallBack != null) {
                    this.mCallBack.OnMessageError();
                }
            }
        } catch (MalformedURLException e7) {
            e7.printStackTrace();
        }
    }

    public boolean isConnected() {
        return this.mbConnected;
    }
}
