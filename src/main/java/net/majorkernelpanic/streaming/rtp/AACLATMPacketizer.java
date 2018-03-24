package net.majorkernelpanic.streaming.rtp;

import android.annotation.SuppressLint;
import android.media.MediaCodec.BufferInfo;
import android.util.Log;
import java.io.IOException;

@SuppressLint({"NewApi"})
public class AACLATMPacketizer extends AbstractPacketizer implements Runnable {
    private static final String TAG = "AACLATMPacketizer";
    private Thread t;

    public AACLATMPacketizer() {
        this.socket.setCacheSize(0);
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

    public void setSamplingRate(int samplingRate) {
        this.socket.setClockFrequency((long) samplingRate);
    }

    @SuppressLint({"NewApi"})
    public void run() {
        Log.d(TAG, "AAC LATM packetizer started !");
        while (!Thread.interrupted()) {
            try {
                this.buffer = this.socket.requestBuffer();
                int length = this.is.read(this.buffer, 16, 1256);
                if (length > 0) {
                    BufferInfo bufferInfo = ((MediaCodecInputStream) this.is).getLastBufferInfo();
                    long oldts = this.ts;
                    this.ts = bufferInfo.presentationTimeUs * 1000;
                    if (oldts > this.ts) {
                        this.socket.commitBuffer();
                    } else {
                        this.socket.markNextPacket();
                        this.socket.updateTimestamp(this.ts);
                        this.buffer[12] = (byte) 0;
                        this.buffer[13] = (byte) 16;
                        this.buffer[14] = (byte) (length >> 5);
                        this.buffer[15] = (byte) (length << 3);
                        byte[] bArr = this.buffer;
                        bArr[15] = (byte) (bArr[15] & 248);
                        bArr = this.buffer;
                        bArr[15] = (byte) (bArr[15] | 0);
                        send((length + 12) + 4);
                    }
                } else {
                    this.socket.commitBuffer();
                }
            } catch (IOException e) {
            } catch (ArrayIndexOutOfBoundsException e2) {
                Log.e(TAG, "ArrayIndexOutOfBoundsException: " + (e2.getMessage() != null ? e2.getMessage() : "unknown error"));
                e2.printStackTrace();
            } catch (InterruptedException e3) {
            }
        }
        Log.d(TAG, "AAC LATM packetizer stopped !");
    }
}
