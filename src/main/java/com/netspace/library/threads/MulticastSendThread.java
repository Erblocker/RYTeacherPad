package com.netspace.library.threads;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.util.Log;
import com.netspace.library.consts.Const;
import com.netspace.library.struct.UDPDataPackage;
import com.netspace.library.struct.UDPDataPackage.OnDataPackageReady;
import com.netspace.library.struct.UDPDataSendCache;
import com.netspace.library.struct.UDPDataSendCache.UDPCacheFoundCallBack;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class MulticastSendThread extends Thread {
    private static final String TAG = "MulticastSendThread";
    private Context mContext;
    private UDPDataPackage mDataPackage = new UDPDataPackage();
    private MulticastResendThread2 mResendThread;
    private ByteBuffer[] mSendBuffer = new ByteBuffer[3];
    private UDPDataSendCache mSendCache;
    private boolean mbHasDataToSend = false;
    private boolean mbStop = false;
    private MulticastLock mcLock;
    private int mnPort = 0;
    private int mnSendBufferIndex = 0;
    private String mszAddress = "";

    public MulticastSendThread(Context context, String szAddress, int nPort, int nResendMessagePort) {
        this.mContext = context;
        WifiManager wifi = (WifiManager) context.getSystemService("wifi");
        if (wifi != null) {
            this.mcLock = wifi.createMulticastLock("multicastlockSendThread");
            this.mcLock.setReferenceCounted(true);
            this.mcLock.acquire();
        }
        this.mSendCache = new UDPDataSendCache();
        this.mszAddress = szAddress;
        this.mnPort = nPort;
        this.mSendBuffer[0] = ByteBuffer.allocate(524288);
        this.mSendBuffer[1] = ByteBuffer.allocate(524288);
        this.mSendBuffer[2] = ByteBuffer.allocate(524288);
        this.mResendThread = new MulticastResendThread2(context, nResendMessagePort);
        this.mResendThread.setDataCache(this.mSendCache);
        this.mResendThread.start();
    }

    public void stopThread() {
        this.mResendThread.stopThread();
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

    public void sendData(byte[] data, int nStartPos, int nLength) {
        ByteBuffer buffer = this.mSendBuffer[this.mnSendBufferIndex];
        buffer.clear();
        buffer.put(data, nStartPos, nLength);
        this.mnSendBufferIndex++;
        if (this.mnSendBufferIndex >= 3) {
            this.mnSendBufferIndex = 0;
        }
        this.mbHasDataToSend = true;
    }

    public void run() {
        while (!this.mbStop) {
            try {
                final InetAddress ia = InetAddress.getByName(this.mszAddress);
                final MulticastSocket ms = new MulticastSocket(this.mnPort);
                int nMTUSize = Const.MULTICAST_PACKAGE_SIZE;
                try {
                    NetworkInterface onenterface = NetworkInterface.getByInetAddress(ia);
                    if (onenterface != null) {
                        nMTUSize = onenterface.getMTU();
                        Log.i(TAG, "MTUSize=" + nMTUSize);
                    }
                } catch (Exception e) {
                }
                ms.joinGroup(ia);
                this.mResendThread.setCallBack(new UDPCacheFoundCallBack() {
                    public void onCacheFound(byte[] content, int nLength, int nPackageIndex, int nStartPos) {
                        Log.e("MulticastResend", "onCacheFound. packageIndex=" + nPackageIndex + ", datapos=" + nStartPos);
                        try {
                            ms.send(new DatagramPacket(content, nLength, ia, MulticastSendThread.this.mnPort));
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                });
                do {
                    if (this.mbHasDataToSend) {
                        for (int i = 0; i < 3; i++) {
                            ByteBuffer sendBuffer = this.mSendBuffer[i];
                            if (sendBuffer.position() > 0) {
                                this.mDataPackage.toUDPPackage(sendBuffer.array(), sendBuffer.position(), i, nMTUSize - 2, new OnDataPackageReady() {
                                    public boolean onPackageReady(byte[] buffer, int nLength, int nPackageIndex, int nPos) {
                                        MulticastSendThread.this.mSendCache.writeToCache(buffer, nLength, nPackageIndex, nPos);
                                        try {
                                            ms.send(new DatagramPacket(buffer, nLength, ia, MulticastSendThread.this.mnPort));
                                        } catch (UnknownHostException e) {
                                            e.printStackTrace();
                                        } catch (IOException e2) {
                                            e2.printStackTrace();
                                        }
                                        return true;
                                    }
                                });
                                sendBuffer.clear();
                            }
                            if (this.mbStop) {
                                break;
                            }
                        }
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    }
                } while (!this.mbStop);
                ms.leaveGroup(ia);
            } catch (UnknownHostException e3) {
                e3.printStackTrace();
            } catch (IOException e4) {
                e4.printStackTrace();
            }
        }
    }
}
