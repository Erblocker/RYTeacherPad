package javazoom.jl.decoder;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamSource implements Source {
    private final InputStream in;

    public InputStreamSource(InputStream in) {
        if (in == null) {
            throw new NullPointerException("in");
        }
        this.in = in;
    }

    public int read(byte[] b, int offs, int len) throws IOException {
        return this.in.read(b, offs, len);
    }

    public boolean willReadBlock() {
        return true;
    }

    public boolean isSeekable() {
        return false;
    }

    public long tell() {
        return -1;
    }

    public long seek(long to) {
        return -1;
    }

    public long length() {
        return -1;
    }
}
