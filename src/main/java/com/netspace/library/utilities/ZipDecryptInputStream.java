package com.netspace.library.utilities;

import java.io.IOException;
import java.io.InputStream;

public class ZipDecryptInputStream extends InputStream {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$netspace$library$utilities$ZipDecryptInputStream$State = null;
    private static final int[] CRC_TABLE = new int[256];
    private static final int DECRYPT_HEADER_SIZE = 12;
    private static final int[] LFH_SIGNATURE = new int[]{80, 75, 3, 4};
    private int compressedSize;
    private final InputStream delegate;
    private final int[] keys = new int[3];
    private final String password;
    private int skipBytes;
    private State state = State.SIGNATURE;
    private int value;
    private int valueInc;
    private int valuePos;

    private enum State {
        SIGNATURE,
        FLAGS,
        COMPRESSED_SIZE,
        FN_LENGTH,
        EF_LENGTH,
        HEADER,
        DATA,
        TAIL
    }

    static /* synthetic */ int[] $SWITCH_TABLE$com$netspace$library$utilities$ZipDecryptInputStream$State() {
        int[] iArr = $SWITCH_TABLE$com$netspace$library$utilities$ZipDecryptInputStream$State;
        if (iArr == null) {
            iArr = new int[State.values().length];
            try {
                iArr[State.COMPRESSED_SIZE.ordinal()] = 3;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[State.DATA.ordinal()] = 7;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[State.EF_LENGTH.ordinal()] = 5;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[State.FLAGS.ordinal()] = 2;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[State.FN_LENGTH.ordinal()] = 4;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[State.HEADER.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[State.SIGNATURE.ordinal()] = 1;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[State.TAIL.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            $SWITCH_TABLE$com$netspace$library$utilities$ZipDecryptInputStream$State = iArr;
        }
        return iArr;
    }

    static {
        for (int i = 0; i < 256; i++) {
            int r = i;
            for (int j = 0; j < 8; j++) {
                if ((r & 1) == 1) {
                    r = (r >>> 1) ^ -306674912;
                } else {
                    r >>>= 1;
                }
            }
            CRC_TABLE[i] = r;
        }
    }

    public ZipDecryptInputStream(InputStream stream, String password) {
        this.delegate = stream;
        this.password = password;
    }

    public int read() throws IOException {
        int result = this.delegate.read();
        if (this.skipBytes == 0) {
            switch ($SWITCH_TABLE$com$netspace$library$utilities$ZipDecryptInputStream$State()[this.state.ordinal()]) {
                case 1:
                    if (result != LFH_SIGNATURE[this.valuePos]) {
                        this.state = State.TAIL;
                        return result;
                    }
                    this.valuePos++;
                    if (this.valuePos < LFH_SIGNATURE.length) {
                        return result;
                    }
                    this.skipBytes = 2;
                    this.state = State.FLAGS;
                    return result;
                case 2:
                    if ((result & 1) == 0) {
                        throw new IllegalStateException("ZIP not password protected.");
                    } else if ((result & 64) == 64) {
                        throw new IllegalStateException("Strong encryption used.");
                    } else if ((result & 8) == 8) {
                        throw new IllegalStateException("Unsupported ZIP format.");
                    } else {
                        result--;
                        this.compressedSize = 0;
                        this.valuePos = 0;
                        this.valueInc = 12;
                        this.state = State.COMPRESSED_SIZE;
                        this.skipBytes = 11;
                        return result;
                    }
                case 3:
                    this.compressedSize += result << (this.valuePos * 8);
                    result -= this.valueInc;
                    if (result < 0) {
                        this.valueInc = 1;
                        result += 256;
                    } else {
                        this.valueInc = 0;
                    }
                    this.valuePos++;
                    if (this.valuePos <= 3) {
                        return result;
                    }
                    this.valuePos = 0;
                    this.value = 0;
                    this.state = State.FN_LENGTH;
                    this.skipBytes = 4;
                    return result;
                case 4:
                case 5:
                    this.value += result << (this.valuePos * 8);
                    if (this.valuePos == 1) {
                        this.valuePos = 0;
                        if (this.state == State.FN_LENGTH) {
                            this.state = State.EF_LENGTH;
                            return result;
                        }
                        this.state = State.HEADER;
                        this.skipBytes = this.value;
                        return result;
                    }
                    this.valuePos = 1;
                    return result;
                case 6:
                    initKeys(this.password);
                    for (int i = 0; i < 12; i++) {
                        updateKeys((byte) (decryptByte() ^ result));
                        result = this.delegate.read();
                    }
                    this.compressedSize -= 12;
                    this.state = State.DATA;
                    break;
                case 7:
                    break;
                default:
                    return result;
            }
            result = (decryptByte() ^ result) & 255;
            updateKeys((byte) result);
            this.compressedSize--;
            if (this.compressedSize != 0) {
                return result;
            }
            this.valuePos = 0;
            this.state = State.SIGNATURE;
            return result;
        }
        this.skipBytes--;
        return result;
    }

    public void close() throws IOException {
        this.delegate.close();
        super.close();
    }

    private void initKeys(String password) {
        this.keys[0] = 305419896;
        this.keys[1] = 591751049;
        this.keys[2] = 878082192;
        for (int i = 0; i < password.length(); i++) {
            updateKeys((byte) (password.charAt(i) & 255));
        }
    }

    private void updateKeys(byte charAt) {
        this.keys[0] = crc32(this.keys[0], charAt);
        int[] iArr = this.keys;
        iArr[1] = iArr[1] + (this.keys[0] & 255);
        this.keys[1] = (this.keys[1] * 134775813) + 1;
        this.keys[2] = crc32(this.keys[2], (byte) (this.keys[1] >> 24));
    }

    private byte decryptByte() {
        int temp = this.keys[2] | 2;
        return (byte) (((temp ^ 1) * temp) >>> 8);
    }

    private int crc32(int oldCrc, byte charAt) {
        return (oldCrc >>> 8) ^ CRC_TABLE[(oldCrc ^ charAt) & 255];
    }
}
