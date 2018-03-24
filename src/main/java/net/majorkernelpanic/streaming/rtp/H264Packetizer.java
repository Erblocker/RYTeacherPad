package net.majorkernelpanic.streaming.rtp;

import android.annotation.SuppressLint;
import android.support.v4.media.TransportMediator;
import android.util.Log;
import java.io.IOException;

public class H264Packetizer extends AbstractPacketizer implements Runnable {
    public static final String TAG = "H264Packetizer";
    private int count = 0;
    private long delay = 0;
    byte[] header = new byte[5];
    private int naluLength = 0;
    private long oldtime = 0;
    private byte[] pps = null;
    private byte[] sps = null;
    private byte[] stapa = null;
    private Statistics stats = new Statistics();
    private int streamType = 1;
    private Thread t = null;

    public H264Packetizer() {
        this.socket.setClockFrequency(90000);
    }

    public void start() {
        if (this.t == null) {
            this.t = new Thread(this);
            this.t.start();
        }
    }

    public void stop() {
        if (this.t != null) {
            try {
                this.is.close();
            } catch (IOException e) {
            }
            this.t.interrupt();
            try {
                this.t.join();
            } catch (InterruptedException e2) {
            }
            this.t = null;
        }
    }

    public void setStreamParameters(byte[] pps, byte[] sps) {
        this.pps = pps;
        this.sps = sps;
        if (pps != null && sps != null) {
            this.stapa = new byte[((sps.length + pps.length) + 5)];
            this.stapa[0] = (byte) 24;
            this.stapa[1] = (byte) (sps.length >> 8);
            this.stapa[2] = (byte) (sps.length & 255);
            this.stapa[sps.length + 3] = (byte) (pps.length >> 8);
            this.stapa[sps.length + 4] = (byte) (pps.length & 255);
            System.arraycopy(sps, 0, this.stapa, 3, sps.length);
            System.arraycopy(pps, 0, this.stapa, sps.length + 5, pps.length);
        }
    }

    public void run() {
        Log.d(TAG, "H264 packetizer started !");
        this.stats.reset();
        this.count = 0;
        if (this.is instanceof MediaCodecInputStream) {
            this.streamType = 1;
            this.socket.setCacheSize(0);
        } else {
            this.streamType = 0;
            this.socket.setCacheSize(400);
        }
        while (!Thread.interrupted()) {
            try {
                this.oldtime = System.nanoTime();
                send();
                this.stats.push(System.nanoTime() - this.oldtime);
                this.delay = this.stats.average();
            } catch (IOException e) {
            } catch (InterruptedException e2) {
            }
        }
        Log.d(TAG, "H264 packetizer stopped !");
    }

    @SuppressLint({"NewApi"})
    private void send() throws IOException, InterruptedException {
        int sum = 1;
        if (this.streamType == 0) {
            fill(this.header, 0, 5);
            this.ts += this.delay;
            this.naluLength = (((this.header[3] & 255) | ((this.header[2] & 255) << 8)) | ((this.header[1] & 255) << 16)) | ((this.header[0] & 255) << 24);
            if (this.naluLength > 100000 || this.naluLength < 0) {
                resync();
            }
        } else if (this.streamType == 1) {
            fill(this.header, 0, 5);
            this.ts = ((MediaCodecInputStream) this.is).getLastBufferInfo().presentationTimeUs * 1000;
            this.naluLength = this.is.available() + 1;
            if (!(this.header[0] == (byte) 0 && this.header[1] == (byte) 0 && this.header[2] == (byte) 0)) {
                Log.e(TAG, "NAL units are not preceeded by 0x00000001");
                this.streamType = 2;
                return;
            }
        } else {
            fill(this.header, 0, 1);
            this.header[4] = this.header[0];
            this.ts = ((MediaCodecInputStream) this.is).getLastBufferInfo().presentationTimeUs * 1000;
            this.naluLength = this.is.available() + 1;
        }
        int type = this.header[4] & 31;
        if (type == 7 || type == 8) {
            Log.v(TAG, "SPS or PPS present in the stream.");
            this.count++;
            if (this.count > 4) {
                this.sps = null;
                this.pps = null;
            }
        }
        if (!(type != 5 || this.sps == null || this.pps == null)) {
            this.buffer = this.socket.requestBuffer();
            this.socket.markNextPacket();
            this.socket.updateTimestamp(this.ts);
            System.arraycopy(this.stapa, 0, this.buffer, 12, this.stapa.length);
            super.send(this.stapa.length + 12);
        }
        if (this.naluLength <= 1258) {
            this.buffer = this.socket.requestBuffer();
            this.buffer[12] = this.header[4];
            int len = fill(this.buffer, 13, this.naluLength - 1);
            this.socket.updateTimestamp(this.ts);
            this.socket.markNextPacket();
            super.send(this.naluLength + 12);
            return;
        }
        this.header[1] = (byte) (this.header[4] & 31);
        byte[] bArr = this.header;
        bArr[1] = (byte) (bArr[1] + 128);
        this.header[0] = (byte) ((this.header[4] & 96) & 255);
        bArr = this.header;
        bArr[0] = (byte) (bArr[0] + 28);
        while (sum < this.naluLength) {
            this.buffer = this.socket.requestBuffer();
            this.buffer[12] = this.header[0];
            this.buffer[13] = this.header[1];
            this.socket.updateTimestamp(this.ts);
            len = fill(this.buffer, 14, this.naluLength - sum > 1258 ? 1258 : this.naluLength - sum);
            if (len >= 0) {
                sum += len;
                if (sum >= this.naluLength) {
                    bArr = this.buffer;
                    bArr[13] = (byte) (bArr[13] + 64);
                    this.socket.markNextPacket();
                }
                super.send((len + 12) + 2);
                this.header[1] = (byte) (this.header[1] & TransportMediator.KEYCODE_MEDIA_PAUSE);
            } else {
                return;
            }
        }
    }

    private int fill(byte[] buffer, int offset, int length) throws IOException {
        int sum = 0;
        while (sum < length) {
            int len = this.is.read(buffer, offset + sum, length - sum);
            if (len < 0) {
                throw new IOException("End of stream");
            }
            sum += len;
        }
        return sum;
    }

    private void resync() throws IOException {
        Log.e(TAG, "Packetizer out of sync ! Let's try to fix that...(NAL length: " + this.naluLength + ")");
        while (true) {
            this.header[0] = this.header[1];
            this.header[1] = this.header[2];
            this.header[2] = this.header[3];
            this.header[3] = this.header[4];
            this.header[4] = (byte) this.is.read();
            int type = this.header[4] & 31;
            if (type == 5 || type == 1) {
                this.naluLength = (((this.header[3] & 255) | ((this.header[2] & 255) << 8)) | ((this.header[1] & 255) << 16)) | ((this.header[0] & 255) << 24);
                if (this.naluLength > 0 && this.naluLength < 100000) {
                    this.oldtime = System.nanoTime();
                    Log.e(TAG, "A NAL unit may have been found in the bit stream !");
                    return;
                } else if (this.naluLength == 0) {
                    Log.e(TAG, "NAL unit with NULL size found...");
                } else if (this.header[3] == (byte) -1 && this.header[2] == (byte) -1 && this.header[1] == (byte) -1 && this.header[0] == (byte) -1) {
                    Log.e(TAG, "NAL unit with 0xFFFFFFFF size found...");
                }
            }
        }
    }
}
