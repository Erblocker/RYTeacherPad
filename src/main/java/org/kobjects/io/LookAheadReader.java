package org.kobjects.io;

import android.support.v4.media.TransportMediator;
import java.io.IOException;
import java.io.Reader;

public class LookAheadReader extends Reader {
    char[] buf;
    int bufPos;
    int bufValid;
    Reader reader;

    public LookAheadReader(Reader r) {
        this.buf = new char[(Runtime.getRuntime().freeMemory() > 1000000 ? 16384 : 128)];
        this.bufPos = 0;
        this.bufValid = 0;
        this.reader = r;
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        if (this.bufValid == 0 && peek(0) == -1) {
            return -1;
        }
        if (len > this.bufValid) {
            len = this.bufValid;
        }
        if (len > this.buf.length - this.bufPos) {
            len = this.buf.length - this.bufPos;
        }
        System.arraycopy(this.buf, this.bufPos, cbuf, off, len);
        this.bufValid -= len;
        this.bufPos += len;
        if (this.bufPos > this.buf.length) {
            this.bufPos -= this.buf.length;
        }
        return len;
    }

    public String readTo(String chars) throws IOException {
        StringBuffer buf = new StringBuffer();
        while (peek(0) != -1 && chars.indexOf((char) peek(0)) == -1) {
            buf.append((char) read());
        }
        return buf.toString();
    }

    public String readTo(char c) throws IOException {
        StringBuffer buf = new StringBuffer();
        while (peek(0) != -1 && peek(0) != c) {
            buf.append((char) read());
        }
        return buf.toString();
    }

    public void close() throws IOException {
        this.reader.close();
    }

    public int read() throws IOException {
        int result = peek(0);
        if (result != -1) {
            int i = this.bufPos + 1;
            this.bufPos = i;
            if (i == this.buf.length) {
                this.bufPos = 0;
            }
            this.bufValid--;
        }
        return result;
    }

    public int peek(int delta) throws IOException {
        if (delta > TransportMediator.KEYCODE_MEDIA_PAUSE) {
            throw new RuntimeException("peek > 127 not supported!");
        }
        while (delta >= this.bufValid) {
            int startPos = (this.bufPos + this.bufValid) % this.buf.length;
            int count = this.reader.read(this.buf, startPos, Math.min(this.buf.length - startPos, this.buf.length - this.bufValid));
            if (count == -1) {
                return -1;
            }
            this.bufValid += count;
        }
        return this.buf[this.bufPos + (delta % this.buf.length)];
    }

    public String readLine() throws IOException {
        if (peek(0) == -1) {
            return null;
        }
        String s = readTo("\r\n");
        if (read() != 13 || peek(0) != 10) {
            return s;
        }
        read();
        return s;
    }

    public String readWhile(String chars) throws IOException {
        StringBuffer buf = new StringBuffer();
        while (peek(0) != -1 && chars.indexOf((char) peek(0)) != -1) {
            buf.append((char) read());
        }
        return buf.toString();
    }

    public void skip(String chars) throws IOException {
        StringBuffer buf = new StringBuffer();
        while (peek(0) != -1 && chars.indexOf((char) peek(0)) != -1) {
            read();
        }
    }
}
