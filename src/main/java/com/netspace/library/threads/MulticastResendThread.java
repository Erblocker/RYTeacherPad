package com.netspace.library.threads;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.support.v4.internal.view.SupportMenu;
import com.netspace.library.struct.UDPDataSendCache;
import com.netspace.library.struct.UDPDataSendCache.UDPCacheFoundCallBack;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class MulticastResendThread extends Thread {
    private String TAG = "MulticastResendThread";
    private UDPCacheFoundCallBack mCallBack;
    private Context mContext;
    private UDPDataSendCache mDataCache;
    private boolean mbStop = false;
    private MulticastLock mcLock;
    private int mnPort = 0;
    private String mszAddress = "";

    public MulticastResendThread(Context context, String szAddress, int nPort) {
        this.mContext = context;
        WifiManager wifi = (WifiManager) context.getSystemService("wifi");
        if (wifi != null) {
            this.mcLock = wifi.createMulticastLock("multicastlockResendThread");
            this.mcLock.setReferenceCounted(true);
            this.mcLock.acquire();
        }
        this.mszAddress = szAddress;
        this.mnPort = nPort;
    }

    public void setDataCache(UDPDataSendCache cache) {
        this.mDataCache = cache;
    }

    public void setCallBack(UDPCacheFoundCallBack CallBack) {
        this.mCallBack = CallBack;
    }

    public void stopThread() {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        UnknownHostException e;
        IOException e2;
        setName("MulticastResendThread");
        InetAddress ia = null;
        byte[] buffer = new byte[SupportMenu.USER_MASK];
        MulticastSocket multicastSocket = null;
        try {
            ia = InetAddress.getByName(this.mszAddress);
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length, ia, this.mnPort);
            MulticastSocket ms = new MulticastSocket(this.mnPort);
            try {
                ms.joinGroup(ia);
                ms.setSoTimeout(200);
                while (!this.mbStop) {
                    try {
                        ms.receive(dp);
                        byte[] receiveBuffer = dp.getData();
                        if (receiveBuffer[0] == (byte) -1 && receiveBuffer[1] == (byte) 1) {
                            this.mDataCache.findInCache(ByteBuffer.wrap(receiveBuffer, 2, 4).getInt(), ByteBuffer.wrap(receiveBuffer, 6, 4).getInt(), this.mCallBack);
                        }
                    } catch (IOException e3) {
                        ms.leaveGroup(ia);
                        ms.close();
                        multicastSocket = new MulticastSocket(this.mnPort);
                        multicastSocket.joinGroup(ia);
                        multicastSocket.setSoTimeout(200);
                        ms = multicastSocket;
                    } catch (UnknownHostException e4) {
                        e = e4;
                        multicastSocket = ms;
                    }
                }
                multicastSocket = ms;
            } catch (UnknownHostException e42) {
                e = e42;
                multicastSocket = ms;
            } catch (IOException e5) {
                e2 = e5;
                multicastSocket = ms;
            }
        } catch (UnknownHostException e6) {
            e = e6;
        } catch (IOException e7) {
            e2 = e7;
        }
        try {
            multicastSocket.leaveGroup(ia);
        } catch (IOException e22) {
            e22.printStackTrace();
            return;
        }
        e22.printStackTrace();
        multicastSocket.leaveGroup(ia);
        e.printStackTrace();
        multicastSocket.leaveGroup(ia);
    }
}
