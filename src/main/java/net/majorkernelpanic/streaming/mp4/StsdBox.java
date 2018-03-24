package net.majorkernelpanic.streaming.mp4;

import android.util.Base64;
import java.io.IOException;
import java.io.RandomAccessFile;

/* compiled from: MP4Parser */
class StsdBox {
    private byte[] buffer = new byte[4];
    private RandomAccessFile fis;
    private long pos = 0;
    private byte[] pps;
    private int ppsLength;
    private byte[] sps;
    private int spsLength;

    public StsdBox(RandomAccessFile fis, long pos) {
        this.fis = fis;
        this.pos = pos;
        findBoxAvcc();
        findSPSandPPS();
    }

    public String getProfileLevel() {
        return MP4Parser.toHexString(this.sps, 1, 3);
    }

    public String getB64PPS() {
        return Base64.encodeToString(this.pps, 0, this.ppsLength, 2);
    }

    public String getB64SPS() {
        return Base64.encodeToString(this.sps, 0, this.spsLength, 2);
    }

    private boolean findSPSandPPS() {
        try {
            this.fis.skipBytes(7);
            this.spsLength = this.fis.readByte() & 255;
            this.sps = new byte[this.spsLength];
            this.fis.read(this.sps, 0, this.spsLength);
            this.fis.skipBytes(2);
            this.ppsLength = this.fis.readByte() & 255;
            this.pps = new byte[this.ppsLength];
            this.fis.read(this.pps, 0, this.ppsLength);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean findBoxAvcc() {
        try {
            this.fis.seek(this.pos + 8);
            while (true) {
                if (this.fis.read() == 97) {
                    this.fis.read(this.buffer, 0, 3);
                    if (this.buffer[0] == (byte) 118 && this.buffer[1] == (byte) 99 && this.buffer[2] == (byte) 67) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }
    }
}
