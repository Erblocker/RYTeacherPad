package com.netspace.library.threads;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.netspace.library.consts.Const;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import org.apache.http.protocol.HTTP;

public class MessageWaitThread extends Thread {
    private static final String TAG = "MessageWaitThread";
    protected OnMessageArrivedListener mCallBack;
    protected Context mContext;
    protected Handler mHandler;
    protected InputStream mInputStream;
    protected String mWaitURL = "";
    protected boolean mbConnected = false;
    protected boolean mbStop = false;

    public interface OnMessageArrivedListener {
        void OnConnected();

        void OnMessageArrived(String str);

        void OnMessageError();
    }

    public MessageWaitThread(Context context, String szWaitURL, OnMessageArrivedListener CallBack) {
        this.mWaitURL = szWaitURL;
        this.mCallBack = CallBack;
        this.mContext = context;
        this.mHandler = new Handler(Looper.getMainLooper());
    }

    public void stopThread() {
        Log.d(TAG, "request MessageWaitThread to close.");
        this.mbStop = true;
        if (this.mInputStream != null) {
            try {
                this.mInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void run() {
        setName(TAG);
        try {
            URL url = new URL(this.mWaitURL);
            Log.d(TAG, "Connecting.");
            HttpURLConnection connection = null;
            URL url2;
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(1000);
                connection.setReadTimeout(1000);
                connection.connect();
                connection.setConnectTimeout(1000);
                connection.setReadTimeout(1000);
                Log.d(TAG, "After Connect. Before url.openStream");
                if (this.mbStop) {
                    url2 = url;
                    return;
                }
                this.mInputStream = new BufferedInputStream(url.openStream());
                byte[] data = new byte[4096];
                if (this.mbStop) {
                    this.mInputStream.close();
                    this.mInputStream = null;
                    url2 = url;
                    return;
                }
                this.mbConnected = true;
                connection.setReadTimeout(Const.IM_DEFAULT_TIMEOUT);
                Log.d(TAG, "Connected.");
                this.mHandler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(MessageWaitThread.this.mContext, "消息系统已连接", 0).show();
                    }
                });
                do {
                    int count = this.mInputStream.read(data);
                    if (count == -1) {
                        break;
                    }
                    String szData = new String(data, 0, count, HTTP.UTF_8);
                    Log.d(TAG, "Receive data form server: " + szData);
                    String[] arrData = szData.split("\r\n");
                    for (String szData2 : arrData) {
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
                    url2 = url;
                    return;
                }
                this.mHandler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(MessageWaitThread.this.mContext, "消息系统连接出现异常，将自动重连", 0).show();
                    }
                });
                if (this.mCallBack != null) {
                    this.mCallBack.OnMessageError();
                }
                this.mInputStream.close();
                this.mInputStream = null;
                if (connection != null) {
                    connection.disconnect();
                }
                url2 = url;
            } catch (SocketTimeoutException e) {
                Log.d(TAG, "SocketTimeoutException");
                this.mbConnected = false;
                e.printStackTrace();
                if (connection != null) {
                    connection.disconnect();
                }
                if (this.mbStop) {
                    url2 = url;
                    return;
                }
                this.mHandler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(MessageWaitThread.this.mContext, "消息系统连接超时，继续重连中", 0).show();
                    }
                });
                if (this.mCallBack != null) {
                    this.mCallBack.OnMessageError();
                }
            } catch (IOException e2) {
                Log.d(TAG, "IOException");
                this.mbConnected = false;
                e2.printStackTrace();
                if (connection != null) {
                    connection.disconnect();
                }
                if (this.mbStop) {
                    url2 = url;
                    return;
                }
                this.mHandler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(MessageWaitThread.this.mContext, "消息系统连接出现异常，将自动重连", 0).show();
                    }
                });
                if (this.mCallBack != null) {
                    this.mCallBack.OnMessageError();
                }
            }
        } catch (MalformedURLException e3) {
            e3.printStackTrace();
        }
    }

    public boolean isConnected() {
        return this.mbConnected;
    }
}
