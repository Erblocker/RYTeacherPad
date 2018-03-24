package com.netspace.library.threads;

import android.content.Context;
import android.net.wifi.WifiManager.MulticastLock;
import android.util.Log;
import com.netspace.library.servers.SimpleTCPServer;
import com.netspace.library.servers.SimpleTCPServer.ClientServerThread;
import com.netspace.library.servers.SimpleTCPServer.TCPServerCallBack;
import com.netspace.library.struct.UDPDataSendCache;
import com.netspace.library.struct.UDPDataSendCache.UDPCacheFoundCallBack;
import java.nio.ByteBuffer;

public class MulticastResendThread2 extends Thread implements TCPServerCallBack {
    private String TAG = "MulticastResendThread";
    private UDPCacheFoundCallBack mCallBack;
    private Context mContext;
    private UDPDataSendCache mDataCache;
    private SimpleTCPServer mTCPServer;
    private boolean mbStop = false;
    private MulticastLock mcLock;
    private int mnPort = 0;
    private String mszAddress = "";

    public MulticastResendThread2(Context context, int nPort) {
        this.mContext = context;
        this.mTCPServer = new SimpleTCPServer(context, nPort);
        this.mTCPServer.setCallBack(this);
        this.mnPort = nPort;
    }

    public void setDataCache(UDPDataSendCache cache) {
        this.mDataCache = cache;
    }

    public void setCallBack(UDPCacheFoundCallBack CallBack) {
    }

    public void stopThread() {
        if (!this.mbStop) {
            this.mTCPServer.stopThread();
            this.mbStop = true;
            try {
                join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                this.mcLock.release();
            } catch (Exception e2) {
            }
        }
    }

    public void run() {
        setName("MulticastResendThread2");
        this.mTCPServer.start();
        while (!this.mbStop) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean onDataReceived(final ClientServerThread thread, byte[] data, int nLength) {
        byte[] receiveBuffer = data;
        int nIndex = 0;
        Log.d(this.TAG, "Request send missing frame. size=" + nLength);
        while (nIndex < nLength - 1) {
            if (receiveBuffer[nIndex] == (byte) -1 && receiveBuffer[nIndex + 1] == (byte) 1) {
                this.mDataCache.findInCache(ByteBuffer.wrap(receiveBuffer, nIndex + 2, 4).getInt(), ByteBuffer.wrap(receiveBuffer, nIndex + 6, 4).getInt(), new UDPCacheFoundCallBack() {
                    public void onCacheFound(byte[] content, int nLength, int nPackageIndex, int nStartPos) {
                        thread.sendData(content, 0, nLength);
                    }
                });
                nIndex += 10;
            } else {
                nIndex++;
            }
        }
        return true;
    }
}
