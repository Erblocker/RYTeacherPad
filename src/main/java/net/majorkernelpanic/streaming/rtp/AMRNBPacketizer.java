package net.majorkernelpanic.streaming.rtp;

import android.util.Log;
import com.foxit.sdk.common.Font;
import java.io.IOException;

public class AMRNBPacketizer extends AbstractPacketizer implements Runnable {
    private static final int AMR_FRAME_HEADER_LENGTH = 1;
    public static final String TAG = "AMRNBPacketizer";
    private static final int[] sFrameBits = new int[]{95, 103, 118, Font.e_fontCharsetGB2312, 148, 159, 204, 244};
    private final int AMR_HEADER_LENGTH = 6;
    private int samplingRate = 8000;
    private Thread t;

    public AMRNBPacketizer() {
        this.socket.setClockFrequency((long) this.samplingRate);
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

    public void run() {
        long oldtime = System.nanoTime();
        byte[] header = new byte[6];
        try {
            fill(header, 0, 6);
            if (header[5] != (byte) 10) {
                Log.e(TAG, "Bad header ! AMR not correcty supported by the phone !");
                return;
            }
            while (!Thread.interrupted()) {
                this.buffer = this.socket.requestBuffer();
                this.buffer[12] = (byte) -16;
                fill(this.buffer, 13, 1);
                int frameLength = (sFrameBits[(Math.abs(this.buffer[13]) >> 3) & 15] + 7) / 8;
                fill(this.buffer, 14, frameLength);
                this.ts += 160000000000L / ((long) this.samplingRate);
                this.socket.updateTimestamp(this.ts);
                this.socket.markNextPacket();
                send(frameLength + 14);
            }
            Log.d(TAG, "AMR packetizer stopped !");
        } catch (IOException e) {
        } catch (InterruptedException e2) {
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
}
