package net.majorkernelpanic.streaming.rtp;

import io.vov.vitamio.MediaPlayer;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Random;

public abstract class AbstractPacketizer {
    protected static final int MAXPACKETSIZE = 1272;
    protected static final int rtphl = 12;
    protected byte[] buffer;
    protected InputStream is = null;
    protected RtpSocket socket = null;
    protected long ts = 0;

    protected static class Statistics {
        public static final String TAG = "Statistics";
        private int c = 0;
        private int count = MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING;
        private long duration = 0;
        private long elapsed = 0;
        private boolean initoffset = false;
        private float m = 0.0f;
        private long period = 10000000000L;
        private float q = 0.0f;
        private long start = 0;

        public Statistics(int count, int period) {
            this.count = count;
            this.period = (long) period;
        }

        public void reset() {
            this.initoffset = false;
            this.q = 0.0f;
            this.m = 0.0f;
            this.c = 0;
            this.elapsed = 0;
            this.start = 0;
            this.duration = 0;
        }

        public void push(long value) {
            this.elapsed += value;
            if (this.elapsed > this.period) {
                this.elapsed = 0;
                long now = System.nanoTime();
                if (!this.initoffset || now - this.start < 0) {
                    this.start = now;
                    this.duration = 0;
                    this.initoffset = true;
                }
                value += (now - this.start) - this.duration;
            }
            if (this.c < 5) {
                this.c++;
                this.m = (float) value;
                return;
            }
            this.m = ((this.m * this.q) + ((float) value)) / (this.q + 1.0f);
            if (this.q < ((float) this.count)) {
                this.q += 1.0f;
            }
        }

        public long average() {
            long l = (long) this.m;
            this.duration += l;
            return l;
        }
    }

    public abstract void start();

    public abstract void stop();

    public AbstractPacketizer() {
        int ssrc = new Random().nextInt();
        this.ts = (long) new Random().nextInt();
        this.socket = new RtpSocket();
        this.socket.setSSRC(ssrc);
    }

    public RtpSocket getRtpSocket() {
        return this.socket;
    }

    public void setSSRC(int ssrc) {
        this.socket.setSSRC(ssrc);
    }

    public int getSSRC() {
        return this.socket.getSSRC();
    }

    public void setInputStream(InputStream is) {
        this.is = is;
    }

    public void setTimeToLive(int ttl) throws IOException {
        this.socket.setTimeToLive(ttl);
    }

    public void setDestination(InetAddress dest, int rtpPort, int rtcpPort) {
        this.socket.setDestination(dest, rtpPort, rtcpPort);
    }

    protected void send(int length) throws IOException {
        this.socket.commitBuffer(length);
    }

    protected static String printBuffer(byte[] buffer, int start, int end) {
        String str = "";
        for (int i = start; i < end; i++) {
            str = new StringBuilder(String.valueOf(str)).append(",").append(Integer.toHexString(buffer[i] & 255)).toString();
        }
        return str;
    }
}
