package javazoom.jl.decoder;

public final class Crc16 {
    private static short polynomial = (short) -32763;
    private short crc = (short) -1;

    public void add_bits(int bitstring, int length) {
        int bitmask = 1 << (length - 1);
        do {
            if ((((bitstring & bitmask) == 0 ? 1 : 0) ^ ((this.crc & 32768) == 0 ? 1 : 0)) != 0) {
                this.crc = (short) (this.crc << 1);
                this.crc = (short) (this.crc ^ polynomial);
            } else {
                this.crc = (short) (this.crc << 1);
            }
            bitmask >>>= 1;
        } while (bitmask != 0);
    }

    public short checksum() {
        short sum = this.crc;
        this.crc = (short) -1;
        return sum;
    }
}
