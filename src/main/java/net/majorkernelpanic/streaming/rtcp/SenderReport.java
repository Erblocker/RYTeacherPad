package net.majorkernelpanic.streaming.rtcp;

import android.os.SystemClock;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class SenderReport {
    public static final int MTU = 1500;
    private static final int PACKET_LENGTH = 28;
    private long delta;
    private long interval;
    private byte[] mBuffer;
    private int mOctetCount;
    private OutputStream mOutputStream;
    private int mPacketCount;
    private int mPort;
    private int mSSRC;
    private byte[] mTcpHeader;
    private int mTransport;
    private long now;
    private long oldnow;
    private DatagramPacket upack;
    private MulticastSocket usock;

    public SenderReport(int ssrc) throws IOException {
        this.mOutputStream = null;
        this.mBuffer = new byte[MTU];
        this.mPort = -1;
        this.mOctetCount = 0;
        this.mPacketCount = 0;
        this.mSSRC = ssrc;
    }

    public SenderReport() {
        this.mOutputStream = null;
        this.mBuffer = new byte[MTU];
        this.mPort = -1;
        this.mOctetCount = 0;
        this.mPacketCount = 0;
        this.mTransport = 0;
        r1 = new byte[4];
        this.mTcpHeader = r1;
        this.mBuffer[0] = (byte) Integer.parseInt("10000000", 2);
        this.mBuffer[1] = (byte) -56;
        setLong(6, 2, 4);
        try {
            this.usock = new MulticastSocket();
            this.upack = new DatagramPacket(this.mBuffer, 1);
            this.interval = 3000;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void close() {
        this.usock.close();
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public void update(int length, long rtpts) throws IOException {
        this.mPacketCount++;
        this.mOctetCount += length;
        setLong((long) this.mPacketCount, 20, 24);
        setLong((long) this.mOctetCount, 24, 28);
        this.now = SystemClock.elapsedRealtime();
        this.delta = (this.oldnow != 0 ? this.now - this.oldnow : 0) + this.delta;
        this.oldnow = this.now;
        if (this.interval > 0 && this.delta >= this.interval) {
            send(System.nanoTime(), rtpts);
            this.delta = 0;
        }
    }

    public void setSSRC(int ssrc) {
        this.mSSRC = ssrc;
        setLong((long) ssrc, 4, 8);
        this.mPacketCount = 0;
        this.mOctetCount = 0;
        setLong((long) this.mPacketCount, 20, 24);
        setLong((long) this.mOctetCount, 24, 28);
    }

    public void setDestination(InetAddress dest, int dport) {
        this.mTransport = 0;
        this.mPort = dport;
        this.upack.setPort(dport);
        this.upack.setAddress(dest);
    }

    public void setOutputStream(OutputStream os, byte channelIdentifier) {
        this.mTransport = 1;
        this.mOutputStream = os;
        this.mTcpHeader[1] = channelIdentifier;
    }

    public int getPort() {
        return this.mPort;
    }

    public int getLocalPort() {
        return this.usock.getLocalPort();
    }

    public int getSSRC() {
        return this.mSSRC;
    }

    public void reset() {
        this.mPacketCount = 0;
        this.mOctetCount = 0;
        setLong((long) this.mPacketCount, 20, 24);
        setLong((long) this.mOctetCount, 24, 28);
        this.oldnow = 0;
        this.now = 0;
        this.delta = 0;
    }

    private void setLong(long n, int begin, int end) {
        for (end--; end >= begin; end--) {
            this.mBuffer[end] = (byte) ((int) (n % 256));
            n >>= 8;
        }
    }

    private void send(long ntpts, long rtpts) throws IOException {
        long hb = ntpts / 1000000000;
        long lb = ((ntpts - (1000000000 * hb)) * 4294967296L) / 1000000000;
        setLong(hb, 8, 12);
        setLong(lb, 12, 16);
        setLong(rtpts, 16, 20);
        if (this.mTransport == 0) {
            this.upack.setLength(28);
            this.usock.send(this.upack);
            return;
        }
        synchronized (this.mOutputStream) {
            try {
                this.mOutputStream.write(this.mTcpHeader);
                this.mOutputStream.write(this.mBuffer, 0, 28);
            } catch (Exception e) {
            }
        }
    }
}
