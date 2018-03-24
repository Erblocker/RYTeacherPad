package net.sourceforge.zbar;

public class Symbol {
    public static final int CODABAR = 38;
    public static final int CODE128 = 128;
    public static final int CODE39 = 39;
    public static final int CODE93 = 93;
    public static final int DATABAR = 34;
    public static final int DATABAR_EXP = 35;
    public static final int EAN13 = 13;
    public static final int EAN8 = 8;
    public static final int I25 = 25;
    public static final int ISBN10 = 10;
    public static final int ISBN13 = 14;
    public static final int NONE = 0;
    public static final int PARTIAL = 1;
    public static final int PDF417 = 57;
    public static final int QRCODE = 64;
    public static final int UPCA = 12;
    public static final int UPCE = 9;
    private long peer;
    private int type;

    static {
        System.loadLibrary("zbarjni");
        init();
    }

    Symbol(long j) {
        this.peer = j;
    }

    private native void destroy(long j);

    private native long getComponents(long j);

    private native int getLocationSize(long j);

    private native int getLocationX(long j, int i);

    private native int getLocationY(long j, int i);

    private native int getType(long j);

    private static native void init();

    public synchronized void destroy() {
        if (this.peer != 0) {
            destroy(this.peer);
            this.peer = 0;
        }
    }

    protected void finalize() {
        destroy();
    }

    public int[] getBounds() {
        int i = Integer.MAX_VALUE;
        int i2 = Integer.MIN_VALUE;
        int locationSize = getLocationSize(this.peer);
        if (locationSize <= 0) {
            return null;
        }
        int[] iArr = new int[4];
        int i3 = 0;
        int i4 = Integer.MIN_VALUE;
        int i5 = Integer.MAX_VALUE;
        while (i3 < locationSize) {
            int locationX = getLocationX(this.peer, i3);
            if (i5 > locationX) {
                i5 = locationX;
            }
            if (i4 >= locationX) {
                locationX = i4;
            }
            i4 = getLocationY(this.peer, i3);
            if (i > i4) {
                i = i4;
            }
            if (i2 >= i4) {
                i4 = i2;
            }
            i3++;
            i2 = i4;
            i4 = locationX;
        }
        iArr[0] = i5;
        iArr[1] = i;
        iArr[2] = i4 - i5;
        iArr[3] = i2 - i;
        return iArr;
    }

    public SymbolSet getComponents() {
        return new SymbolSet(getComponents(this.peer));
    }

    public native int getConfigMask();

    public native int getCount();

    public native String getData();

    public native byte[] getDataBytes();

    public int[] getLocationPoint(int i) {
        return new int[]{getLocationX(this.peer, i), getLocationY(this.peer, i)};
    }

    public native int getModifierMask();

    public native int getOrientation();

    public native int getQuality();

    public int getType() {
        if (this.type == 0) {
            this.type = getType(this.peer);
        }
        return this.type;
    }

    native long next();
}
