package com.netspace.library.struct;

import android.support.v4.internal.view.SupportMenu;
import com.netspace.library.consts.Const;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class UDPDataPackage {
    private final byte FOOTSIGN = (byte) -2;
    private final byte FOOTSIGN_COMPLETE = (byte) -1;
    private final int HEADFOOTSIZE = 20;
    private final byte[] HEADSIGN = new byte[2];
    private final int HEADSIZE = 18;
    private final byte[] RESENDSIGN = new byte[2];
    public boolean bResendPackage = false;
    public ByteBuffer dataContent;
    private byte[] mBuffer = new byte[SupportMenu.USER_MASK];
    private boolean mbLastPackage = false;
    public byte nCRC = (byte) 0;
    public int nDataIndex = 0;
    public int nDataSize = 0;
    public int nFrameIndex = 0;
    public int nPackageIndex = 0;
    public int nPackageTotalSize = 0;
    public byte[] szSign = new byte[2];

    public interface OnDataPackageReady {
        boolean onPackageReady(byte[] bArr, int i, int i2, int i3);
    }

    public UDPDataPackage() {
        this.HEADSIGN[0] = (byte) -1;
        this.HEADSIGN[1] = (byte) 1;
        this.RESENDSIGN[1] = (byte) 2;
    }

    public boolean isLastPackage() {
        return this.mbLastPackage;
    }

    public int fromBytes(byte[] data, int nSize) {
        if (nSize <= 20) {
            return -2;
        }
        this.szSign[0] = data[0];
        this.szSign[1] = data[1];
        if (data[0] != this.HEADSIGN[0]) {
            return -2;
        }
        if (data[1] != this.HEADSIGN[1] && data[1] != this.RESENDSIGN[1]) {
            return -2;
        }
        if (data[1] == this.RESENDSIGN[1]) {
            this.bResendPackage = true;
        } else {
            this.bResendPackage = false;
        }
        this.nFrameIndex = ByteBuffer.wrap(data, 2, 4).getInt();
        this.nDataIndex = ByteBuffer.wrap(data, 6, 4).getInt();
        this.nPackageIndex = ByteBuffer.wrap(data, 10, 4).getInt();
        this.nPackageTotalSize = ByteBuffer.wrap(data, 14, 4).getInt();
        this.nDataSize = ByteBuffer.wrap(data, 18, 4).getInt();
        if (this.nDataSize <= 0 || this.nDataSize >= Const.MULTICAST_PACKAGE_SIZE || (data.length - 22) - 2 < this.nDataSize) {
            return -2;
        }
        this.dataContent = ByteBuffer.wrap(data, 22, this.nDataSize);
        if (data[this.nDataSize + 22] != (byte) -2 && data[this.nDataSize + 22] != (byte) -1) {
            return -3;
        }
        this.nCRC = data[(this.nDataSize + 22) + 1];
        CRC32 crc = new CRC32();
        crc.update(data, 0, this.nDataSize + 22);
        if (this.nCRC != ((byte) ((int) crc.getValue()))) {
            return -1;
        }
        if (data[this.nDataSize + 22] == (byte) -1) {
            this.mbLastPackage = true;
        } else {
            this.mbLastPackage = false;
        }
        return this.nDataSize + 24;
    }

    public boolean toUDPPackage(byte[] data, int nSize, int nFrameIndex, int nUDPLength, OnDataPackageReady CallBack) {
        int nRemainSize = nSize;
        int nContentSize = nUDPLength - 20;
        int nDataStartPos = 0;
        boolean bLastPackage = false;
        this.nPackageIndex++;
        while (nRemainSize > 0) {
            int nCopyLength;
            this.mBuffer[0] = (byte) -1;
            this.mBuffer[1] = (byte) 1;
            intToBytes(nFrameIndex, this.mBuffer, 2);
            intToBytes(nDataStartPos, this.mBuffer, 6);
            intToBytes(this.nPackageIndex, this.mBuffer, 10);
            intToBytes(nSize, this.mBuffer, 14);
            if (nRemainSize > nContentSize) {
                nCopyLength = nContentSize;
            } else {
                nCopyLength = nRemainSize;
                bLastPackage = true;
            }
            intToBytes(nCopyLength, this.mBuffer, 18);
            System.arraycopy(data, nDataStartPos, this.mBuffer, 22, nCopyLength);
            if (bLastPackage) {
                this.mBuffer[nCopyLength + 22] = (byte) -1;
            } else {
                this.mBuffer[nCopyLength + 22] = (byte) -2;
            }
            CRC32 crc = new CRC32();
            crc.update(this.mBuffer, 0, nCopyLength + 22);
            this.mBuffer[(nCopyLength + 22) + 1] = (byte) ((int) crc.getValue());
            if (CallBack != null) {
                if (!CallBack.onPackageReady(this.mBuffer, (nCopyLength + 22) + 2, this.nPackageIndex, nDataStartPos)) {
                    break;
                }
            }
            nDataStartPos += nCopyLength;
            nRemainSize -= nCopyLength;
        }
        return false;
    }

    private void intToBytes(int i, byte[] buffer, int nStartPos) {
        buffer[nStartPos] = (byte) (i >> 24);
        buffer[nStartPos + 1] = (byte) (i >> 16);
        buffer[nStartPos + 2] = (byte) (i >> 8);
        buffer[nStartPos + 3] = (byte) i;
    }
}
