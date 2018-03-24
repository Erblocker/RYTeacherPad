package net.majorkernelpanic.streaming.rtp;

import android.util.Log;
import java.io.IOException;

public class H263Packetizer extends AbstractPacketizer implements Runnable {
    public static final String TAG = "H263Packetizer";
    private Statistics stats = new Statistics();
    private Thread t;

    public H263Packetizer() {
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

    public void run() {
        long duration = 0;
        int j = 0;
        boolean firstFragment = true;
        this.stats.reset();
        while (!Thread.interrupted()) {
            try {
                if (j == 0) {
                    this.buffer = this.socket.requestBuffer();
                }
                this.socket.updateTimestamp(this.ts);
                this.buffer[12] = (byte) 0;
                this.buffer[13] = (byte) 0;
                long time = System.nanoTime();
                if (fill((j + 12) + 2, (1260 - j) - 2) >= 0) {
                    duration += System.nanoTime() - time;
                    j = 0;
                    int i = 14;
                    while (i < 1271) {
                        if (this.buffer[i] == (byte) 0 && this.buffer[i + 1] == (byte) 0 && (this.buffer[i + 2] & 252) == 128) {
                            j = i;
                            break;
                        }
                        i++;
                    }
                    int tr = ((this.buffer[i + 2] & 3) << 6) | ((this.buffer[i + 3] & 255) >> 2);
                    if (firstFragment) {
                        this.buffer[12] = (byte) 4;
                        firstFragment = false;
                    } else {
                        this.buffer[12] = (byte) 0;
                    }
                    if (j > 0) {
                        this.stats.push(duration);
                        this.ts += this.stats.average();
                        duration = 0;
                        this.socket.markNextPacket();
                        send(j);
                        byte[] nextBuffer = this.socket.requestBuffer();
                        System.arraycopy(this.buffer, j + 2, nextBuffer, 14, (1272 - j) - 2);
                        this.buffer = nextBuffer;
                        j = (1272 - j) - 2;
                        firstFragment = true;
                    } else {
                        send(1272);
                    }
                } else {
                    return;
                }
            } catch (IOException e) {
            } catch (InterruptedException e2) {
            }
        }
        Log.d(TAG, "H263 Packetizer stopped !");
    }

    private int fill(int offset, int length) throws IOException {
        int sum = 0;
        while (sum < length) {
            int len = this.is.read(this.buffer, offset + sum, length - sum);
            if (len < 0) {
                throw new IOException("End of stream");
            }
            sum += len;
        }
        return sum;
    }
}
