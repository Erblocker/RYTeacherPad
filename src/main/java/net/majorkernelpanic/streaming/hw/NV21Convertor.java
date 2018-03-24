package net.majorkernelpanic.streaming.hw;

import java.nio.ByteBuffer;

public class NV21Convertor {
    private byte[] mBuffer;
    ByteBuffer mCopy;
    private int mHeight;
    private boolean mPanesReversed = false;
    private boolean mPlanar;
    private int mSize;
    private int mSliceHeight;
    private int mStride;
    private int mWidth;
    private int mYPadding;

    public void setSize(int width, int height) {
        this.mHeight = height;
        this.mWidth = width;
        this.mSliceHeight = height;
        this.mStride = width;
        this.mSize = this.mWidth * this.mHeight;
    }

    public void setStride(int width) {
        this.mStride = width;
    }

    public void setSliceHeigth(int height) {
        this.mSliceHeight = height;
    }

    public void setPlanar(boolean planar) {
        this.mPlanar = planar;
    }

    public void setYPadding(int padding) {
        this.mYPadding = padding;
    }

    public int getBufferSize() {
        return (this.mSize * 3) / 2;
    }

    public void setEncoderColorFormat(int colorFormat) {
        switch (colorFormat) {
            case 19:
            case 20:
                setPlanar(true);
                return;
            case 21:
            case 39:
            case 2130706688:
                setPlanar(false);
                return;
            default:
                return;
        }
    }

    public void setColorPanesReversed(boolean b) {
        this.mPanesReversed = b;
    }

    public int getStride() {
        return this.mStride;
    }

    public int getSliceHeigth() {
        return this.mSliceHeight;
    }

    public int getYPadding() {
        return this.mYPadding;
    }

    public boolean getPlanar() {
        return this.mPlanar;
    }

    public boolean getUVPanesReversed() {
        return this.mPanesReversed;
    }

    public void convert(byte[] data, ByteBuffer buffer) {
        buffer.put(convert(data), 0, buffer.capacity() < data.length ? buffer.capacity() : data.length);
    }

    public byte[] convert(byte[] data) {
        if (this.mBuffer == null || this.mBuffer.length != (((this.mSliceHeight * 3) * this.mStride) / 2) + this.mYPadding) {
            this.mBuffer = new byte[((((this.mSliceHeight * 3) * this.mStride) / 2) + this.mYPadding)];
        }
        int i;
        if (this.mPlanar) {
            if (this.mSliceHeight != this.mHeight || this.mStride != this.mWidth) {
                return data;
            }
            if (this.mPanesReversed) {
                for (i = 0; i < this.mSize / 4; i++) {
                    this.mBuffer[i] = data[this.mSize + (i * 2)];
                    this.mBuffer[(this.mSize / 4) + i] = data[(this.mSize + (i * 2)) + 1];
                }
            } else {
                for (i = 0; i < this.mSize / 4; i++) {
                    this.mBuffer[i] = data[(this.mSize + (i * 2)) + 1];
                    this.mBuffer[(this.mSize / 4) + i] = data[this.mSize + (i * 2)];
                }
            }
            if (this.mYPadding == 0) {
                System.arraycopy(this.mBuffer, 0, data, this.mSize, this.mSize / 2);
                return data;
            }
            System.arraycopy(data, 0, this.mBuffer, 0, this.mSize);
            System.arraycopy(this.mBuffer, 0, this.mBuffer, this.mSize + this.mYPadding, this.mSize / 2);
            return this.mBuffer;
        } else if (this.mSliceHeight != this.mHeight || this.mStride != this.mWidth) {
            return data;
        } else {
            if (!this.mPanesReversed) {
                for (i = this.mSize; i < this.mSize + (this.mSize / 2); i += 2) {
                    this.mBuffer[0] = data[i + 1];
                    data[i + 1] = data[i];
                    data[i] = this.mBuffer[0];
                }
            }
            if (this.mYPadding <= 0) {
                return data;
            }
            System.arraycopy(data, 0, this.mBuffer, 0, this.mSize);
            System.arraycopy(data, this.mSize, this.mBuffer, this.mSize + this.mYPadding, this.mSize / 2);
            return this.mBuffer;
        }
    }
}
