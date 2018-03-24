package net.majorkernelpanic.streaming.rtp;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

@SuppressLint({"NewApi"})
public class MediaCodecInputStream extends InputStream {
    public final String TAG = "MediaCodecInputStream";
    private ByteBuffer mBuffer = null;
    private BufferInfo mBufferInfo = new BufferInfo();
    private ByteBuffer[] mBuffers = null;
    private boolean mClosed = false;
    private int mIndex = -1;
    private MediaCodec mMediaCodec = null;
    public MediaFormat mMediaFormat;

    public MediaCodecInputStream(MediaCodec mediaCodec) {
        this.mMediaCodec = mediaCodec;
        this.mBuffers = this.mMediaCodec.getOutputBuffers();
    }

    public void close() {
        this.mClosed = true;
    }

    public int read() throws IOException {
        return 0;
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
        int min = 0;
        try {
            if (this.mBuffer == null) {
                while (!Thread.interrupted() && !this.mClosed) {
                    this.mIndex = this.mMediaCodec.dequeueOutputBuffer(this.mBufferInfo, 500000);
                    if (this.mIndex >= 0) {
                        this.mBuffer = this.mBuffers[this.mIndex];
                        this.mBuffer.position(0);
                        break;
                    } else if (this.mIndex == -3) {
                        this.mBuffers = this.mMediaCodec.getOutputBuffers();
                    } else if (this.mIndex == -2) {
                        this.mMediaFormat = this.mMediaCodec.getOutputFormat();
                        Log.i("MediaCodecInputStream", this.mMediaFormat.toString());
                    } else if (this.mIndex == -1) {
                        Log.v("MediaCodecInputStream", "No buffer available...");
                    } else {
                        Log.e("MediaCodecInputStream", "Message: " + this.mIndex);
                    }
                }
            }
            if (this.mClosed) {
                throw new IOException("This InputStream was closed");
            }
            min = length < this.mBufferInfo.size - this.mBuffer.position() ? length : this.mBufferInfo.size - this.mBuffer.position();
            this.mBuffer.get(buffer, offset, min);
            if (this.mBuffer.position() >= this.mBufferInfo.size) {
                this.mMediaCodec.releaseOutputBuffer(this.mIndex, false);
                this.mBuffer = null;
            }
            return min;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public int available() {
        if (this.mBuffer != null) {
            return this.mBufferInfo.size - this.mBuffer.position();
        }
        return 0;
    }

    public BufferInfo getLastBufferInfo() {
        return this.mBufferInfo;
    }
}
