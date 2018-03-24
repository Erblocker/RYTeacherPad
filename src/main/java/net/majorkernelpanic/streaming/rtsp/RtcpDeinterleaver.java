package net.majorkernelpanic.streaming.rtsp;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

class RtcpDeinterleaver extends InputStream implements Runnable {
    public static final String TAG = "RtcpDeinterleaver";
    private byte[] mBuffer;
    private IOException mIOException;
    private InputStream mInputStream;
    private PipedInputStream mPipedInputStream = new PipedInputStream(4096);
    private PipedOutputStream mPipedOutputStream;

    public RtcpDeinterleaver(InputStream inputStream) {
        this.mInputStream = inputStream;
        try {
            this.mPipedOutputStream = new PipedOutputStream(this.mPipedInputStream);
        } catch (IOException e) {
        }
        this.mBuffer = new byte[1024];
        new Thread(this).start();
    }

    public void run() {
        while (true) {
            try {
                this.mPipedOutputStream.write(this.mBuffer, 0, this.mInputStream.read(this.mBuffer, 0, 1024));
            } catch (IOException e) {
                try {
                    this.mPipedInputStream.close();
                } catch (IOException e2) {
                }
                this.mIOException = e;
                return;
            }
        }
    }

    public int read(byte[] buffer) throws IOException {
        if (this.mIOException == null) {
            return this.mPipedInputStream.read(buffer);
        }
        throw this.mIOException;
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
        if (this.mIOException == null) {
            return this.mPipedInputStream.read(buffer, offset, length);
        }
        throw this.mIOException;
    }

    public int read() throws IOException {
        if (this.mIOException == null) {
            return this.mPipedInputStream.read();
        }
        throw this.mIOException;
    }

    public void close() throws IOException {
        this.mInputStream.close();
    }
}
