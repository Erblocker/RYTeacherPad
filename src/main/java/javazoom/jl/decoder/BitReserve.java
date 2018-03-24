package javazoom.jl.decoder;

final class BitReserve {
    private static final int BUFSIZE = 32768;
    private static final int BUFSIZE_MASK = 32767;
    private final int[] buf = new int[32768];
    private int buf_byte_idx = 0;
    private int offset = 0;
    private int totbit = 0;

    BitReserve() {
    }

    public int hsstell() {
        return this.totbit;
    }

    public int hgetbits(int N) {
        this.totbit += N;
        int val = 0;
        int pos = this.buf_byte_idx;
        int N2;
        if (pos + N < 32768) {
            int pos2 = pos;
            N2 = N;
            while (true) {
                N = N2 - 1;
                if (N2 <= 0) {
                    break;
                }
                val = (val << 1) | (this.buf[pos2] != 0 ? 1 : 0);
                pos2++;
                N2 = N;
            }
            pos = pos2;
        } else {
            N2 = N;
            while (true) {
                N = N2 - 1;
                if (N2 <= 0) {
                    break;
                }
                int i;
                val <<= 1;
                if (this.buf[pos] != 0) {
                    i = 1;
                } else {
                    i = 0;
                }
                val |= i;
                pos = (pos + 1) & BUFSIZE_MASK;
                N2 = N;
            }
        }
        this.buf_byte_idx = pos;
        return val;
    }

    public int hget1bit() {
        this.totbit++;
        int val = this.buf[this.buf_byte_idx];
        this.buf_byte_idx = (this.buf_byte_idx + 1) & BUFSIZE_MASK;
        return val;
    }

    public void hputbuf(int val) {
        int i = this.offset;
        int i2 = i + 1;
        this.buf[i] = val & 128;
        i = i2 + 1;
        this.buf[i2] = val & 64;
        i2 = i + 1;
        this.buf[i] = val & 32;
        i = i2 + 1;
        this.buf[i2] = val & 16;
        i2 = i + 1;
        this.buf[i] = val & 8;
        i = i2 + 1;
        this.buf[i2] = val & 4;
        i2 = i + 1;
        this.buf[i] = val & 2;
        i = i2 + 1;
        this.buf[i2] = val & 1;
        if (i == 32768) {
            this.offset = 0;
        } else {
            this.offset = i;
        }
    }

    public void rewindNbits(int N) {
        this.totbit -= N;
        this.buf_byte_idx -= N;
        if (this.buf_byte_idx < 0) {
            this.buf_byte_idx += 32768;
        }
    }

    public void rewindNbytes(int N) {
        int bits = N << 3;
        this.totbit -= bits;
        this.buf_byte_idx -= bits;
        if (this.buf_byte_idx < 0) {
            this.buf_byte_idx += 32768;
        }
    }
}
