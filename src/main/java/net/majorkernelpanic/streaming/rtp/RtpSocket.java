package net.majorkernelpanic.streaming.rtp;

import android.os.SystemClock;
import android.support.v4.media.TransportMediator;
import android.util.Log;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import net.majorkernelpanic.streaming.rtcp.SenderReport;
import org.apache.http.HttpStatus;

public class RtpSocket implements Runnable {
    public static final int MTU = 1300;
    public static final int RTP_HEADER_LENGTH = 12;
    public static final String TAG = "RtpSocket";
    public static final int TRANSPORT_TCP = 1;
    public static final int TRANSPORT_UDP = 0;
    private AverageBitrate mAverageBitrate = new AverageBitrate();
    private Semaphore mBufferCommitted;
    private int mBufferCount = HttpStatus.SC_MULTIPLE_CHOICES;
    private int mBufferIn;
    private int mBufferOut;
    private Semaphore mBufferRequested;
    private byte[][] mBuffers = new byte[this.mBufferCount][];
    private long mCacheSize = 0;
    private long mClock = 0;
    private int mCount = 0;
    private long mOldTimestamp = 0;
    protected OutputStream mOutputStream = null;
    private DatagramPacket[] mPackets = new DatagramPacket[this.mBufferCount];
    private int mPort = -1;
    private SenderReport mReport = new SenderReport();
    private int mSeq = 0;
    private MulticastSocket mSocket;
    private int mSsrc;
    private byte[] mTcpHeader;
    private Thread mThread;
    private long[] mTimestamps;
    private int mTransport = 0;

    protected static class AverageBitrate {
        private static final long RESOLUTION = 200;
        private int mCount;
        private long mDelta;
        private long[] mElapsed;
        private int mIndex;
        private long mNow;
        private long mOldNow;
        private int mSize;
        private long[] mSum;
        private int mTotal;

        public AverageBitrate() {
            this.mSize = 25;
            reset();
        }

        public AverageBitrate(int delay) {
            this.mSize = delay / 200;
            reset();
        }

        public void reset() {
            this.mSum = new long[this.mSize];
            this.mElapsed = new long[this.mSize];
            this.mNow = SystemClock.elapsedRealtime();
            this.mOldNow = this.mNow;
            this.mCount = 0;
            this.mDelta = 0;
            this.mTotal = 0;
            this.mIndex = 0;
        }

        public void push(int length) {
            this.mNow = SystemClock.elapsedRealtime();
            if (this.mCount > 0) {
                this.mDelta += this.mNow - this.mOldNow;
                this.mTotal += length;
                if (this.mDelta > RESOLUTION) {
                    this.mSum[this.mIndex] = (long) this.mTotal;
                    this.mTotal = 0;
                    this.mElapsed[this.mIndex] = this.mDelta;
                    this.mDelta = 0;
                    this.mIndex++;
                    if (this.mIndex >= this.mSize) {
                        this.mIndex = 0;
                    }
                }
            }
            this.mOldNow = this.mNow;
            this.mCount++;
        }

        public int average() {
            long j = 0;
            long delta = 0;
            long sum = 0;
            for (int i = 0; i < this.mSize; i++) {
                sum += this.mSum[i];
                delta += this.mElapsed[i];
            }
            if (delta > 0) {
                j = (8000 * sum) / delta;
            }
            return (int) j;
        }
    }

    protected static class Statistics {
        public static final String TAG = "Statistics";
        private int c = 0;
        private int count = 500;
        private long duration = 0;
        private long elapsed = 0;
        private boolean initoffset = false;
        private float m = 0.0f;
        private long period = 6000000000L;
        private float q = 0.0f;
        private long start = 0;

        public Statistics(int count, long period) {
            this.count = count;
            this.period = 1000000 * period;
        }

        public void push(long value) {
            this.duration += value;
            this.elapsed += value;
            if (this.elapsed > this.period) {
                this.elapsed = 0;
                long now = System.nanoTime();
                if (!this.initoffset || now - this.start < 0) {
                    this.start = now;
                    this.duration = 0;
                    this.initoffset = true;
                }
                value -= (now - this.start) - this.duration;
            }
            if (this.c < 40) {
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
            long l = ((long) this.m) - 2000000;
            return l > 0 ? l : 0;
        }
    }

    public RtpSocket() {
        byte[] bArr = new byte[4];
        bArr[0] = (byte) 36;
        this.mTcpHeader = bArr;
        resetFifo();
        for (int i = 0; i < this.mBufferCount; i++) {
            this.mBuffers[i] = new byte[MTU];
            this.mPackets[i] = new DatagramPacket(this.mBuffers[i], 1);
            this.mBuffers[i][0] = (byte) Integer.parseInt("10000000", 2);
            this.mBuffers[i][1] = (byte) 96;
        }
        try {
            this.mSocket = new MulticastSocket();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void resetFifo() {
        this.mCount = 0;
        this.mBufferIn = 0;
        this.mBufferOut = 0;
        this.mTimestamps = new long[this.mBufferCount];
        this.mBufferRequested = new Semaphore(this.mBufferCount);
        this.mBufferCommitted = new Semaphore(0);
        this.mReport.reset();
        this.mAverageBitrate.reset();
    }

    public void close() {
        this.mSocket.close();
    }

    public void setSSRC(int ssrc) {
        this.mSsrc = ssrc;
        for (int i = 0; i < this.mBufferCount; i++) {
            setLong(this.mBuffers[i], (long) ssrc, 8, 12);
        }
        this.mReport.setSSRC(this.mSsrc);
    }

    public int getSSRC() {
        return this.mSsrc;
    }

    public void setClockFrequency(long clock) {
        this.mClock = clock;
    }

    public void setCacheSize(long cacheSize) {
        this.mCacheSize = cacheSize;
    }

    public void setTimeToLive(int ttl) throws IOException {
        this.mSocket.setTimeToLive(ttl);
    }

    public void setDestination(InetAddress dest, int dport, int rtcpPort) {
        if (dport != 0 && rtcpPort != 0) {
            this.mTransport = 0;
            this.mPort = dport;
            for (int i = 0; i < this.mBufferCount; i++) {
                this.mPackets[i].setPort(dport);
                this.mPackets[i].setAddress(dest);
            }
            this.mReport.setDestination(dest, rtcpPort);
        }
    }

    public void setOutputStream(OutputStream outputStream, byte channelIdentifier) {
        if (outputStream != null) {
            this.mTransport = 1;
            this.mOutputStream = outputStream;
            this.mTcpHeader[1] = channelIdentifier;
            this.mReport.setOutputStream(outputStream, (byte) (channelIdentifier + 1));
        }
    }

    public int getPort() {
        return this.mPort;
    }

    public int[] getLocalPorts() {
        return new int[]{this.mSocket.getLocalPort(), this.mReport.getLocalPort()};
    }

    public byte[] requestBuffer() throws InterruptedException {
        this.mBufferRequested.acquire();
        byte[] bArr = this.mBuffers[this.mBufferIn];
        bArr[1] = (byte) (bArr[1] & TransportMediator.KEYCODE_MEDIA_PAUSE);
        return this.mBuffers[this.mBufferIn];
    }

    public void commitBuffer() throws IOException {
        if (this.mThread == null) {
            this.mThread = new Thread(this);
            this.mThread.start();
        }
        int i = this.mBufferIn + 1;
        this.mBufferIn = i;
        if (i >= this.mBufferCount) {
            this.mBufferIn = 0;
        }
        this.mBufferCommitted.release();
    }

    public void commitBuffer(int length) throws IOException {
        updateSequence();
        this.mPackets[this.mBufferIn].setLength(length);
        this.mAverageBitrate.push(length);
        int i = this.mBufferIn + 1;
        this.mBufferIn = i;
        if (i >= this.mBufferCount) {
            this.mBufferIn = 0;
        }
        this.mBufferCommitted.release();
        if (this.mThread == null) {
            this.mThread = new Thread(this);
            this.mThread.start();
        }
    }

    public long getBitrate() {
        return (long) this.mAverageBitrate.average();
    }

    private void updateSequence() {
        byte[] bArr = this.mBuffers[this.mBufferIn];
        int i = this.mSeq + 1;
        this.mSeq = i;
        setLong(bArr, (long) i, 2, 4);
    }

    public void updateTimestamp(long timestamp) {
        this.mTimestamps[this.mBufferIn] = timestamp;
        setLong(this.mBuffers[this.mBufferIn], ((timestamp / 100) * (this.mClock / 1000)) / 10000, 4, 8);
    }

    public void markNextPacket() {
        byte[] bArr = this.mBuffers[this.mBufferIn];
        bArr[1] = (byte) (bArr[1] | 128);
    }

    public void run() {
        Statistics stats = new Statistics(50, 3000);
        Thread.sleep(this.mCacheSize);
        long delta = 0;
        while (this.mBufferCommitted.tryAcquire(4, TimeUnit.SECONDS)) {
            try {
                if (this.mOldTimestamp != 0) {
                    if (this.mTimestamps[this.mBufferOut] - this.mOldTimestamp > 0) {
                        stats.push(this.mTimestamps[this.mBufferOut] - this.mOldTimestamp);
                        long d = stats.average() / 1000000;
                        if (this.mCacheSize > 0) {
                            Thread.sleep(d);
                        }
                    } else if (this.mTimestamps[this.mBufferOut] - this.mOldTimestamp < 0) {
                        Log.e(TAG, "TS: " + this.mTimestamps[this.mBufferOut] + " OLD: " + this.mOldTimestamp);
                    }
                    delta += this.mTimestamps[this.mBufferOut] - this.mOldTimestamp;
                    if (delta > 500000000 || delta < 0) {
                        delta = 0;
                    }
                }
                this.mReport.update(this.mPackets[this.mBufferOut].getLength(), ((this.mTimestamps[this.mBufferOut] / 100) * (this.mClock / 1000)) / 10000);
                this.mOldTimestamp = this.mTimestamps[this.mBufferOut];
                int i = this.mCount;
                this.mCount = i + 1;
                if (i > 30) {
                    if (this.mTransport == 0) {
                        this.mSocket.send(this.mPackets[this.mBufferOut]);
                    } else {
                        sendTCP();
                    }
                }
                i = this.mBufferOut + 1;
                this.mBufferOut = i;
                if (i >= this.mBufferCount) {
                    this.mBufferOut = 0;
                }
                this.mBufferRequested.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.mThread = null;
        resetFifo();
    }

    private void sendTCP() {
        synchronized (this.mOutputStream) {
            int len = this.mPackets[this.mBufferOut].getLength();
            Log.d(TAG, "sent " + len);
            this.mTcpHeader[2] = (byte) (len >> 8);
            this.mTcpHeader[3] = (byte) (len & 255);
            try {
                this.mOutputStream.write(this.mTcpHeader);
                this.mOutputStream.write(this.mBuffers[this.mBufferOut], 0, len);
            } catch (Exception e) {
            }
        }
    }

    private void setLong(byte[] buffer, long n, int begin, int end) {
        for (end--; end >= begin; end--) {
            buffer[end] = (byte) ((int) (n % 256));
            n >>= 8;
        }
    }
}
