package com.uraroji.garage.android.lame;

public class SimpleLame {
    public static native void close();

    public static native int encode(short[] sArr, short[] sArr2, int i, byte[] bArr);

    public static native int flush(byte[] bArr);

    public static native void init(int i, int i2, int i3, int i4, int i5);

    public static void init(int inSamplerate, int outChannel, int outSamplerate, int outBitrate) {
        init(inSamplerate, outChannel, outSamplerate, outBitrate, 7);
    }
}
