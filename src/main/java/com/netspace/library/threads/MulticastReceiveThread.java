package com.netspace.library.threads;

import android.content.Context;
import android.media.MediaFormat;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.support.v4.internal.view.SupportMenu;
import android.util.Log;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.consts.Const;
import com.netspace.library.servers.SimpleTCPClient;
import com.netspace.library.servers.SimpleTCPClient.TCPClientCallBack;
import com.netspace.library.struct.UDPDataPackage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

public class MulticastReceiveThread extends Thread implements TCPClientCallBack {
    private final int BUFFERCOUNT = 30;
    private String TAG = "MulticastReceiveThread";
    private final boolean VERBOSE = false;
    private UDPDataPackage mDataPackage = new UDPDataPackage();
    private HashMap<Integer, Boolean> mFinishedPackages = new HashMap();
    private UDPDataBuffer[] mReceiveBuffer = new UDPDataBuffer[30];
    private ByteBuffer mRecorrectBuffer = ByteBuffer.allocate(262144);
    private byte[] mResendDataBuffer = new byte[20];
    private SimpleTCPClient mResendTCPClient;
    private boolean mbStop = false;
    private MulticastLock mcLock;
    private int mnLastBufferIndex = -1;
    private int mnPort = 0;
    private String mszAddress = "";

    public static class UDPDataBuffer {
        boolean bFinished = false;
        private boolean mbIsKeyFrame = false;
        private boolean mbKeyFrameChecked = false;
        int nDataSize = 0;
        int nPackageIndex = 0;
        long nPackageTime;
        int nPackageTotalLength = 0;
        int nPosition = 0;
        int nTotalLength = 0;
        int nValidDataLength = 0;
        ByteBuffer receiveBuffer;
        String szWriteIndex = "";

        public void reset() {
            this.nPackageIndex = 0;
            this.nTotalLength = 0;
            this.nPackageTotalLength = 0;
            this.nPosition = 0;
            this.nValidDataLength = 0;
            this.szWriteIndex = "";
            this.nPackageTime = 0;
            this.receiveBuffer.clear();
            this.bFinished = false;
            this.mbKeyFrameChecked = false;
            this.mbIsKeyFrame = false;
        }

        public boolean isKeyFrame() {
            if (this.mbKeyFrameChecked) {
                return this.mbIsKeyFrame;
            }
            this.mbKeyFrameChecked = true;
            this.mbIsKeyFrame = false;
            if (this.receiveBuffer.position() < 2048) {
                return false;
            }
            byte[] data = this.receiveBuffer.array();
            int i = 0;
            while (i < Math.min(2048, this.receiveBuffer.position() - 4)) {
                if (data[i] == (byte) 0 && data[i + 1] == (byte) 0 && data[i + 2] == (byte) 1 && data[i + 3] == (byte) 101) {
                    this.mbIsKeyFrame = true;
                    return true;
                }
                i++;
            }
            return false;
        }
    }

    public MulticastReceiveThread(Context context, String szAddress, String szTCPCorrectServerAddress, int nPort) {
        WifiManager wifi = (WifiManager) context.getSystemService("wifi");
        if (wifi != null) {
            this.mcLock = wifi.createMulticastLock("multicastlockReceiveThread");
            this.mcLock.setReferenceCounted(true);
            this.mcLock.acquire();
        }
        this.mszAddress = szAddress;
        this.mnPort = nPort;
        this.mResendTCPClient = new SimpleTCPClient(context, szTCPCorrectServerAddress, Const.MULTICAST_TCP_PORT);
        this.mResendTCPClient.setCallBack(this);
        for (int i = 0; i < this.mReceiveBuffer.length; i++) {
            this.mReceiveBuffer[i] = new UDPDataBuffer();
            this.mReceiveBuffer[i].receiveBuffer = ByteBuffer.allocate(524288);
        }
    }

    public void readData(ByteBuffer resultBuffer) {
        if (MyiBaseApplication.ReleaseBuild) {
            readData(resultBuffer, true);
        } else {
            readData(resultBuffer, false);
        }
    }

    public void readData(ByteBuffer resultBuffer, boolean bAllowIncomplete) {
        while (!this.mbStop) {
            int nSmallPackageIndex = -1;
            int nIndex = -1;
            while (!this.mbStop) {
                for (int i = 0; i < this.mReceiveBuffer.length; i++) {
                    if (this.mReceiveBuffer[i].nPackageIndex != 0) {
                        if (nSmallPackageIndex == -1) {
                            nSmallPackageIndex = this.mReceiveBuffer[i].nPackageIndex;
                            nIndex = i;
                        } else if (this.mReceiveBuffer[i].nPackageIndex < nSmallPackageIndex) {
                            nSmallPackageIndex = this.mReceiveBuffer[i].nPackageIndex;
                            nIndex = i;
                        }
                    }
                }
                if (nIndex != -1) {
                    break;
                }
            }
            if (nIndex != -1) {
                UDPDataBuffer currentBuffer = this.mReceiveBuffer[nIndex];
                boolean bPackageIsBad = false;
                while (!currentBuffer.bFinished) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (currentBuffer.nPackageIndex != nSmallPackageIndex) {
                        bPackageIsBad = true;
                        break;
                    } else if (this.mbStop) {
                        return;
                    }
                }
                if (!bPackageIsBad) {
                    int nTotalLength = currentBuffer.nTotalLength;
                    ByteBuffer SourceBuffer = currentBuffer.receiveBuffer;
                    if (bAllowIncomplete || currentBuffer.nValidDataLength == currentBuffer.nTotalLength) {
                        synchronized (SourceBuffer) {
                            SourceBuffer.position(0);
                            resultBuffer.put(SourceBuffer.array(), 0, nTotalLength);
                        }
                        currentBuffer.reset();
                        return;
                    }
                    currentBuffer.reset();
                }
            }
        }
    }

    public boolean readH264Info(MediaFormat format) {
        ByteBuffer buffer = ByteBuffer.allocate(524288);
        readData(buffer, true);
        byte[] frameData = buffer.array();
        byte[] SPSData = new byte[512];
        int nSPSCount = 0;
        byte[] PPSData = new byte[512];
        int nPPSCount = 0;
        boolean bSPSData = false;
        boolean bPPSData = false;
        int i = 0;
        while (i < buffer.position() - 3) {
            if (frameData[i] == (byte) 0 && frameData[i + 1] == (byte) 0 && frameData[i + 2] == (byte) 1 && frameData[i + 3] == (byte) 103) {
                bSPSData = true;
                bPPSData = false;
            }
            if (frameData[i] == (byte) 0 && frameData[i + 1] == (byte) 0 && frameData[i + 2] == (byte) 1 && frameData[i + 3] == (byte) 104) {
                bSPSData = false;
                bPPSData = true;
            }
            if (frameData[i] == (byte) 0 && frameData[i + 1] == (byte) 0 && frameData[i + 2] == (byte) 1 && frameData[i + 3] != (byte) 103 && frameData[i + 3] != (byte) 104) {
                break;
            }
            if (bSPSData) {
                SPSData[nSPSCount] = frameData[i];
                nSPSCount++;
            }
            if (bPPSData) {
                PPSData[nPPSCount] = frameData[i];
                nPPSCount++;
            }
            i++;
        }
        if (nSPSCount == 0) {
            if (nPPSCount != 0) {
                frameData = null;
            }
            if (nSPSCount > 0 || nPPSCount <= 0) {
                return false;
            }
            format.setByteBuffer("csd-0", ByteBuffer.wrap(SPSData, 0, nSPSCount));
            format.setByteBuffer("csd-1", ByteBuffer.wrap(PPSData, 0, nPPSCount));
            return true;
        }
        if (nPPSCount != 0) {
            frameData = null;
        }
        if (nSPSCount > 0) {
        }
        return false;
        frameData = null;
        if (nSPSCount > 0) {
        }
        return false;
    }

    public void stopThread() {
        if (this.mResendTCPClient != null) {
            this.mResendTCPClient.stopThread();
            this.mResendTCPClient = null;
        }
        if (!this.mbStop) {
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

    private void intToBytes(int i, byte[] buffer, int nStartPos) {
        buffer[nStartPos] = (byte) (i >> 24);
        buffer[nStartPos + 1] = (byte) (i >> 16);
        buffer[nStartPos + 2] = (byte) (i >> 8);
        buffer[nStartPos + 3] = (byte) i;
    }

    private void sendMissingPackageRequest(int nPackageIndex, int nStartPos) {
        intToBytes(nPackageIndex, this.mResendDataBuffer, 2);
        intToBytes(nStartPos, this.mResendDataBuffer, 6);
        if (this.mResendTCPClient != null) {
            this.mResendTCPClient.sendData(this.mResendDataBuffer, 0, 10);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        UnknownHostException e;
        IOException e2;
        setName("MulticastReceiveThread");
        InetAddress ia = null;
        byte[] buffer = new byte[SupportMenu.USER_MASK];
        MulticastSocket multicastSocket = null;
        this.mResendTCPClient.start();
        try {
            ia = InetAddress.getByName(this.mszAddress);
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length, ia, this.mnPort);
            MulticastSocket ms = new MulticastSocket(this.mnPort);
            try {
                ms.setReuseAddress(true);
                ms.joinGroup(ia);
                ms.setSoTimeout(200);
                this.mResendDataBuffer[0] = (byte) -1;
                this.mResendDataBuffer[1] = (byte) 1;
                while (!this.mbStop) {
                    try {
                        ms.receive(dp);
                        processReceivedPackage(dp.getData(), dp.getLength(), true);
                    } catch (IOException e3) {
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
        e.printStackTrace();
        multicastSocket.leaveGroup(ia);
        e22.printStackTrace();
        multicastSocket.leaveGroup(ia);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized int processReceivedPackage(byte[] buffer, int nLength, boolean bCheckLastBuffer) {
        int nResult;
        nResult = this.mDataPackage.fromBytes(buffer, nLength);
        if (nResult > 0 && !this.mFinishedPackages.containsKey(Integer.valueOf(this.mDataPackage.nPackageIndex))) {
            int nBufferIndex;
            boolean bFoundBuffer;
            int i;
            UDPDataBuffer currentBuffer;
            if (this.mDataPackage.bResendPackage) {
                nBufferIndex = 0;
                bFoundBuffer = false;
            } else {
                nBufferIndex = 0;
                bFoundBuffer = false;
            }
            for (i = 0; i < this.mReceiveBuffer.length; i++) {
                if (this.mReceiveBuffer[i].nPackageIndex == this.mDataPackage.nPackageIndex) {
                    nBufferIndex = i;
                    bFoundBuffer = true;
                    break;
                }
            }
            if (!bFoundBuffer) {
                for (i = 0; i < this.mReceiveBuffer.length; i++) {
                    if (this.mReceiveBuffer[i].nPackageIndex == 0) {
                        this.mReceiveBuffer[i].nPackageIndex = this.mDataPackage.nPackageIndex;
                        this.mReceiveBuffer[i].nPackageTotalLength = this.mDataPackage.nPackageTotalSize;
                        this.mReceiveBuffer[i].nDataSize = this.mDataPackage.nDataSize;
                        this.mReceiveBuffer[i].nPackageTime = System.currentTimeMillis();
                        nBufferIndex = i;
                        bFoundBuffer = true;
                        break;
                    }
                }
            }
            if (!bFoundBuffer) {
                long nMinPackageTime = -1;
                int nMinIndex = -1;
                for (i = 0; i < this.mReceiveBuffer.length; i++) {
                    currentBuffer = this.mReceiveBuffer[i];
                    if (nMinPackageTime == -1) {
                        nMinPackageTime = currentBuffer.nPackageTime;
                        nMinIndex = i;
                    } else if (currentBuffer.nPackageTime < nMinPackageTime) {
                        nMinPackageTime = currentBuffer.nPackageTime;
                        nMinIndex = i;
                    }
                }
                if (nMinIndex != -1) {
                    this.mReceiveBuffer[nMinIndex].reset();
                    this.mReceiveBuffer[nMinIndex].nPackageIndex = this.mDataPackage.nPackageIndex;
                    this.mReceiveBuffer[nMinIndex].nPackageTotalLength = this.mDataPackage.nPackageTotalSize;
                    this.mReceiveBuffer[nMinIndex].nDataSize = this.mDataPackage.nDataSize;
                    this.mReceiveBuffer[nMinIndex].nPackageTime = System.currentTimeMillis();
                    nBufferIndex = nMinIndex;
                    bFoundBuffer = true;
                }
            }
            if (bFoundBuffer) {
                ByteBuffer SourceBuffer;
                currentBuffer = this.mReceiveBuffer[nBufferIndex];
                if (currentBuffer.bFinished) {
                    SourceBuffer = currentBuffer.receiveBuffer;
                } else {
                    SourceBuffer = currentBuffer.receiveBuffer;
                }
                synchronized (SourceBuffer) {
                    if (currentBuffer.szWriteIndex.indexOf("," + String.valueOf(this.mDataPackage.nDataIndex) + ";") == -1) {
                        SourceBuffer.position(this.mDataPackage.nDataIndex);
                        SourceBuffer.put(this.mDataPackage.dataContent.array(), this.mDataPackage.dataContent.position(), this.mDataPackage.nDataSize);
                        currentBuffer.szWriteIndex += "," + String.valueOf(this.mDataPackage.nDataIndex) + ";(" + this.mDataPackage.nDataSize + ")" + ";";
                        currentBuffer.nValidDataLength += this.mDataPackage.nDataSize;
                        if (!(this.mDataPackage.bResendPackage || SourceBuffer.position() - currentBuffer.nPosition == this.mDataPackage.nDataSize)) {
                            int nCurrentPosition = currentBuffer.nPosition;
                            int nAfterWritePosition = SourceBuffer.position();
                            while (this.mDataPackage.nDataSize + nCurrentPosition < nAfterWritePosition) {
                                sendMissingPackageRequest(this.mDataPackage.nPackageIndex, nCurrentPosition);
                                nCurrentPosition += this.mDataPackage.nDataSize;
                            }
                        }
                        currentBuffer.nPosition = Math.max(currentBuffer.nPosition, SourceBuffer.position());
                        if (this.mDataPackage.isLastPackage()) {
                            currentBuffer.nTotalLength = currentBuffer.nPosition;
                        }
                        if (currentBuffer.nValidDataLength >= currentBuffer.nPackageTotalLength) {
                            this.mFinishedPackages.put(Integer.valueOf(currentBuffer.nPackageIndex), Boolean.TRUE);
                            currentBuffer.bFinished = true;
                            if (currentBuffer.isKeyFrame()) {
                                i = 0;
                                while (i < this.mReceiveBuffer.length) {
                                    UDPDataBuffer pendingBuffer = this.mReceiveBuffer[i];
                                    if (!(pendingBuffer.nPackageIndex == 0 || nBufferIndex == i || pendingBuffer.bFinished || !pendingBuffer.isKeyFrame())) {
                                        int nPendingBufferIndex = pendingBuffer.nPackageIndex;
                                        pendingBuffer.reset();
                                    }
                                    i++;
                                }
                            }
                        } else if (!bCheckLastBuffer) {
                        }
                    }
                }
            }
            if (bCheckLastBuffer && this.mnLastBufferIndex != -1) {
                UDPDataBuffer lastBuffer = this.mReceiveBuffer[this.mnLastBufferIndex];
                if (!lastBuffer.bFinished) {
                    for (int nLastBufferDataPosition = lastBuffer.nPosition; nLastBufferDataPosition < lastBuffer.nPackageTotalLength; nLastBufferDataPosition += lastBuffer.nDataSize) {
                        sendMissingPackageRequest(lastBuffer.nPackageIndex, lastBuffer.nPosition);
                    }
                }
                this.mnLastBufferIndex = nBufferIndex;
            }
        }
        return nResult;
    }

    public synchronized boolean onDataReceived(Socket socket, byte[] data, int nLength) {
        Log.i(this.TAG, "Receive correct package. size=" + nLength);
        int nBufferSize = this.mRecorrectBuffer.position();
        int nSourceSize = nLength;
        byte[] sourceBuffer = data;
        int nStartPos = 0;
        boolean bBufferHasData = false;
        boolean bFoundVaildHead = false;
        this.mRecorrectBuffer.put(data, 0, nLength);
        data = this.mRecorrectBuffer.array();
        nLength = this.mRecorrectBuffer.position() + 1;
        int i = 0;
        while (i < nLength - 1) {
            if (data[i] == (byte) -1 && (data[i + 1] == (byte) 1 || data[i + 1] == (byte) 2)) {
                nStartPos = i;
                bFoundVaildHead = true;
                break;
            }
            i++;
        }
        if (!bFoundVaildHead) {
            Log.e(this.TAG, "Correct Package can not found a vaild head.");
        }
        while (nStartPos < nLength - 1) {
            byte[] destin = Arrays.copyOfRange(data, nStartPos, nLength - 1);
            int nResult = processReceivedPackage(destin, destin.length, false);
            if (nResult <= 0) {
                if (nResult != -2) {
                    Log.e(this.TAG, "Correct Package process error. pos=" + nStartPos + ",length=" + nLength);
                    nStartPos++;
                    i = nStartPos;
                    while (i < nLength - 1) {
                        if (data[i] == (byte) -1 && (data[i + 1] == (byte) 1 || data[i + 1] == (byte) 2)) {
                            nStartPos = i;
                            break;
                        }
                        i++;
                    }
                } else {
                    this.mRecorrectBuffer.clear();
                    this.mRecorrectBuffer.put(destin);
                    if (destin.length > Const.MULTICAST_PACKAGE_SIZE) {
                        Log.e(this.TAG, "Recorrect package error. Has more data in the buffer.");
                    }
                    bBufferHasData = true;
                    if (!bBufferHasData) {
                        this.mRecorrectBuffer.clear();
                    }
                }
            } else {
                nStartPos += nResult;
            }
        }
        if (bBufferHasData) {
            this.mRecorrectBuffer.clear();
        }
        return true;
    }

    public boolean onNewInstance(SimpleTCPClient newThread) {
        this.mResendTCPClient = newThread;
        return false;
    }
}
