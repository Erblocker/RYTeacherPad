package javazoom.jl.decoder;

import android.support.v4.internal.view.SupportMenu;
import android.support.v4.media.TransportMediator;
import android.support.v4.view.MotionEventCompat;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import org.kxml2.wap.Wbxml;

public final class Bitstream implements BitstreamErrors {
    private static final int BUFFER_INT_SIZE = 433;
    static byte INITIAL_SYNC = (byte) 0;
    static byte STRICT_SYNC = (byte) 1;
    private int bitindex;
    private final int[] bitmask;
    private Crc16[] crc;
    private boolean firstframe;
    private byte[] frame_bytes = new byte[1732];
    private final int[] framebuffer = new int[BUFFER_INT_SIZE];
    private int framesize;
    private final Header header;
    private int header_pos = 0;
    private byte[] rawid3v2;
    private boolean single_ch_mode;
    public final PushbackInputStream source;
    private final byte[] syncbuf;
    private int syncword;
    private int wordpointer;

    public Bitstream(InputStream in) {
        int[] iArr = new int[18];
        iArr[1] = 1;
        iArr[2] = 3;
        iArr[3] = 7;
        iArr[4] = 15;
        iArr[5] = 31;
        iArr[6] = 63;
        iArr[7] = TransportMediator.KEYCODE_MEDIA_PAUSE;
        iArr[8] = 255;
        iArr[9] = BitstreamErrors.BITSTREAM_LAST;
        iArr[10] = 1023;
        iArr[11] = 2047;
        iArr[12] = 4095;
        iArr[13] = 8191;
        iArr[14] = 16383;
        iArr[15] = 32767;
        iArr[16] = SupportMenu.USER_MASK;
        iArr[17] = 131071;
        this.bitmask = iArr;
        this.header = new Header();
        this.syncbuf = new byte[4];
        this.crc = new Crc16[1];
        this.rawid3v2 = null;
        this.firstframe = true;
        if (in == null) {
            throw new NullPointerException("in");
        }
        InputStream in2 = new BufferedInputStream(in);
        loadID3v2(in2);
        this.firstframe = true;
        this.source = new PushbackInputStream(in2, 17320);
        closeFrame();
    }

    public int header_pos() {
        return this.header_pos;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadID3v2(InputStream in) {
        int size = -1;
        try {
            in.mark(10);
            size = readID3v2Header(in);
            this.header_pos = size;
            try {
                in.reset();
            } catch (IOException e) {
            }
        } catch (IOException e2) {
        } catch (Throwable th) {
            try {
                in.reset();
            } catch (IOException e3) {
            }
        }
        if (size > 0) {
            try {
                this.rawid3v2 = new byte[size];
                in.read(this.rawid3v2, 0, this.rawid3v2.length);
            } catch (IOException e4) {
            }
        }
    }

    private int readID3v2Header(InputStream in) throws IOException {
        byte[] id3header = new byte[4];
        int size = -10;
        in.read(id3header, 0, 3);
        if (id3header[0] == (byte) 73 && id3header[1] == (byte) 68 && id3header[2] == (byte) 51) {
            in.read(id3header, 0, 3);
            int majorVersion = id3header[0];
            int revision = id3header[1];
            in.read(id3header, 0, 4);
            size = (((majorVersion << 21) + (revision << 14)) + (id3header[2] << 7)) + id3header[3];
        }
        return size + 10;
    }

    public InputStream getRawID3v2() {
        if (this.rawid3v2 == null) {
            return null;
        }
        return new ByteArrayInputStream(this.rawid3v2);
    }

    public void close() throws BitstreamException {
        try {
            this.source.close();
        } catch (IOException ex) {
            throw newBitstreamException(258, ex);
        }
    }

    public Header readFrame() throws BitstreamException {
        Header result = null;
        try {
            result = readNextFrame();
            if (this.firstframe) {
                result.parseVBR(this.frame_bytes);
                this.firstframe = false;
            }
        } catch (BitstreamException ex) {
            if (ex.getErrorCode() == 261) {
                try {
                    closeFrame();
                    result = readNextFrame();
                } catch (BitstreamException e) {
                    if (e.getErrorCode() != 260) {
                        throw newBitstreamException(e.getErrorCode(), e);
                    }
                }
            } else if (ex.getErrorCode() != 260) {
                throw newBitstreamException(ex.getErrorCode(), ex);
            }
        }
        return result;
    }

    private Header readNextFrame() throws BitstreamException {
        if (this.framesize == -1) {
            nextFrame();
        }
        return this.header;
    }

    private void nextFrame() throws BitstreamException {
        this.header.read_header(this, this.crc);
    }

    public void unreadFrame() throws BitstreamException {
        if (this.wordpointer == -1 && this.bitindex == -1 && this.framesize > 0) {
            try {
                this.source.unread(this.frame_bytes, 0, this.framesize);
            } catch (IOException e) {
                throw newBitstreamException(258);
            }
        }
    }

    public void closeFrame() {
        this.framesize = -1;
        this.wordpointer = -1;
        this.bitindex = -1;
    }

    public boolean isSyncCurrentPosition(int syncmode) throws BitstreamException {
        int read = readBytes(this.syncbuf, 0, 4);
        int headerstring = ((((this.syncbuf[0] << 24) & -16777216) | ((this.syncbuf[1] << 16) & 16711680)) | ((this.syncbuf[2] << 8) & MotionEventCompat.ACTION_POINTER_INDEX_MASK)) | ((this.syncbuf[3] << 0) & 255);
        try {
            this.source.unread(this.syncbuf, 0, read);
        } catch (IOException e) {
        }
        switch (read) {
            case 0:
                return true;
            case 4:
                return isSyncMark(headerstring, syncmode, this.syncword);
            default:
                return false;
        }
    }

    public int readBits(int n) {
        return get_bits(n);
    }

    public int readCheckedBits(int n) {
        return get_bits(n);
    }

    protected BitstreamException newBitstreamException(int errorcode) {
        return new BitstreamException(errorcode, null);
    }

    protected BitstreamException newBitstreamException(int errorcode, Throwable throwable) {
        return new BitstreamException(errorcode, throwable);
    }

    int syncHeader(byte syncmode) throws BitstreamException {
        if (readBytes(this.syncbuf, 0, 3) != 3) {
            throw newBitstreamException(260, null);
        }
        int headerstring = (((this.syncbuf[0] << 16) & 16711680) | ((this.syncbuf[1] << 8) & MotionEventCompat.ACTION_POINTER_INDEX_MASK)) | ((this.syncbuf[2] << 0) & 255);
        do {
            headerstring <<= 8;
            if (readBytes(this.syncbuf, 3, 1) != 1) {
                throw newBitstreamException(260, null);
            }
            headerstring |= this.syncbuf[3] & 255;
        } while (!isSyncMark(headerstring, syncmode, this.syncword));
        return headerstring;
    }

    public boolean isSyncMark(int headerstring, int syncmode, int word) {
        boolean sync;
        if (syncmode == INITIAL_SYNC) {
            sync = (headerstring & -2097152) == -2097152;
        } else {
            if ((-521216 & headerstring) == word) {
                boolean z;
                if ((headerstring & Wbxml.EXT_0) == Wbxml.EXT_0) {
                    z = true;
                } else {
                    z = false;
                }
                if (z == this.single_ch_mode) {
                    sync = true;
                }
            }
            sync = false;
        }
        if (sync) {
            if (((headerstring >>> 10) & 3) != 3) {
                sync = true;
            } else {
                sync = false;
            }
        }
        if (sync) {
            if (((headerstring >>> 17) & 3) != 0) {
                sync = true;
            } else {
                sync = false;
            }
        }
        if (!sync) {
            return sync;
        }
        if (((headerstring >>> 19) & 3) != 1) {
            return true;
        }
        return false;
    }

    int read_frame_data(int bytesize) throws BitstreamException {
        int numread = readFully(this.frame_bytes, 0, bytesize);
        this.framesize = bytesize;
        this.wordpointer = -1;
        this.bitindex = -1;
        return numread;
    }

    void parse_frame() throws BitstreamException {
        byte[] byteread = this.frame_bytes;
        int bytesize = this.framesize;
        int k = 0;
        int b = 0;
        while (k < bytesize) {
            byte b1 = (byte) 0;
            byte b2 = (byte) 0;
            byte b3 = (byte) 0;
            byte b0 = byteread[k];
            if (k + 1 < bytesize) {
                b1 = byteread[k + 1];
            }
            if (k + 2 < bytesize) {
                b2 = byteread[k + 2];
            }
            if (k + 3 < bytesize) {
                b3 = byteread[k + 3];
            }
            int b4 = b + 1;
            this.framebuffer[b] = ((((b0 << 24) & -16777216) | ((b1 << 16) & 16711680)) | ((b2 << 8) & MotionEventCompat.ACTION_POINTER_INDEX_MASK)) | (b3 & 255);
            k += 4;
            b = b4;
        }
        this.wordpointer = 0;
        this.bitindex = 0;
    }

    public int get_bits(int number_of_bits) {
        int sum = this.bitindex + number_of_bits;
        if (this.wordpointer < 0) {
            this.wordpointer = 0;
        }
        if (sum <= 32) {
            int returnvalue = (this.framebuffer[this.wordpointer] >>> (32 - sum)) & this.bitmask[number_of_bits];
            int i = this.bitindex + number_of_bits;
            this.bitindex = i;
            if (i == 32) {
                this.bitindex = 0;
                this.wordpointer++;
            }
            return returnvalue;
        }
        int Right = this.framebuffer[this.wordpointer] & SupportMenu.USER_MASK;
        this.wordpointer++;
        returnvalue = ((((Right << 16) & SupportMenu.CATEGORY_MASK) | (((this.framebuffer[this.wordpointer] & SupportMenu.CATEGORY_MASK) >>> 16) & SupportMenu.USER_MASK)) >>> (48 - sum)) & this.bitmask[number_of_bits];
        this.bitindex = sum - 32;
        return returnvalue;
    }

    void set_syncword(int syncword0) {
        this.syncword = syncword0 & -193;
        this.single_ch_mode = (syncword0 & Wbxml.EXT_0) == Wbxml.EXT_0;
    }

    private int readFully(byte[] b, int offs, int len) throws BitstreamException {
        int nRead = 0;
        while (len > 0) {
            try {
                int bytesread = this.source.read(b, offs, len);
                if (bytesread == -1) {
                    int len2 = len;
                    int offs2 = offs;
                    while (true) {
                        len = len2 - 1;
                        if (len2 <= 0) {
                            break;
                        }
                        offs = offs2 + 1;
                        b[offs2] = (byte) 0;
                        len2 = len;
                        offs2 = offs;
                    }
                    offs = offs2;
                    return nRead;
                }
                nRead += bytesread;
                offs += bytesread;
                len -= bytesread;
            } catch (IOException ex) {
                throw newBitstreamException(258, ex);
            }
        }
        return nRead;
    }

    private int readBytes(byte[] b, int offs, int len) throws BitstreamException {
        int totalBytesRead = 0;
        while (len > 0) {
            try {
                int bytesread = this.source.read(b, offs, len);
                if (bytesread == -1) {
                    break;
                }
                totalBytesRead += bytesread;
                offs += bytesread;
                len -= bytesread;
            } catch (IOException ex) {
                throw newBitstreamException(258, ex);
            }
        }
        return totalBytesRead;
    }
}
