package org.kobjects.io;

import java.io.IOException;
import java.io.InputStream;

public class BoundInputStream extends InputStream {
    InputStream is;
    int remaining;

    public BoundInputStream(InputStream is, int length) {
        this.is = is;
        this.remaining = length;
    }

    public int available() throws IOException {
        int avail = this.is.available();
        return avail < this.remaining ? avail : this.remaining;
    }

    public int read() throws IOException {
        if (this.remaining <= 0) {
            return -1;
        }
        this.remaining--;
        return this.is.read();
    }

    public int read(byte[] data, int start, int max) throws IOException {
        if (max > this.remaining) {
            max = this.remaining;
        }
        int actual = this.is.read(data, start, max);
        if (actual > 0) {
            this.remaining -= actual;
        }
        return actual;
    }

    public void close() {
        try {
            this.is.close();
        } catch (IOException e) {
        }
    }
}
