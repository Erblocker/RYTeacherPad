package org.kobjects.mime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import org.apache.http.protocol.HTTP;
import org.kobjects.base64.Base64;

public class Decoder {
    String boundary;
    char[] buf;
    String characterEncoding;
    boolean consumed;
    boolean eof;
    Hashtable header;
    InputStream is;

    private final String readLine() throws IOException {
        int cnt = 0;
        while (true) {
            int i = this.is.read();
            if (i == -1 && cnt == 0) {
                return null;
            }
            if (i != -1 && i != 10) {
                if (i != 13) {
                    if (cnt >= this.buf.length) {
                        char[] tmp = new char[((this.buf.length * 3) / 2)];
                        System.arraycopy(this.buf, 0, tmp, 0, this.buf.length);
                        this.buf = tmp;
                    }
                    int cnt2 = cnt + 1;
                    this.buf[cnt] = (char) i;
                    cnt = cnt2;
                }
            }
        }
        return new String(this.buf, 0, cnt);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Hashtable getHeaderElements(String header) {
        String key = "";
        int pos = 0;
        Hashtable result = new Hashtable();
        int len = header.length();
        while (true) {
            if (pos >= len || header.charAt(pos) > ' ') {
                if (pos >= len) {
                    break;
                }
                int cut;
                if (header.charAt(pos) == '\"') {
                    pos++;
                    cut = header.indexOf(34, pos);
                    if (cut == -1) {
                        throw new RuntimeException("End quote expected in " + header);
                    }
                    result.put(key, header.substring(pos, cut));
                    pos = cut + 2;
                    if (pos >= len) {
                        break;
                    } else if (header.charAt(pos - 1) != ';') {
                        throw new RuntimeException("; expected in " + header);
                    }
                }
                cut = header.indexOf(59, pos);
                if (cut == -1) {
                    break;
                }
                result.put(key, header.substring(pos, cut));
                pos = cut + 1;
                cut = header.indexOf(61, pos);
                if (cut == -1) {
                    break;
                }
                key = header.substring(pos, cut).toLowerCase().trim();
                pos = cut + 1;
            } else {
                pos++;
            }
        }
        return result;
    }

    public Decoder(InputStream is, String _bound) throws IOException {
        this(is, _bound, null);
    }

    public Decoder(InputStream is, String _bound, String characterEncoding) throws IOException {
        String line;
        this.buf = new char[256];
        this.characterEncoding = characterEncoding;
        this.is = is;
        this.boundary = "--" + _bound;
        do {
            line = readLine();
            if (line == null) {
                throw new IOException("Unexpected EOF");
            }
        } while (!line.startsWith(this.boundary));
        if (line.endsWith("--")) {
            this.eof = true;
            is.close();
        }
        this.consumed = true;
    }

    public Enumeration getHeaderNames() {
        return this.header.keys();
    }

    public String getHeader(String key) {
        return (String) this.header.get(key.toLowerCase());
    }

    public String readContent() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        readContent(bos);
        String result = this.characterEncoding == null ? new String(bos.toByteArray()) : new String(bos.toByteArray(), this.characterEncoding);
        System.out.println("Field content: '" + result + "'");
        return result;
    }

    public void readContent(OutputStream os) throws IOException {
        if (this.consumed) {
            throw new RuntimeException("Content already consumed!");
        }
        String line = "";
        String contentType = getHeader(HTTP.CONTENT_TYPE);
        if ("base64".equals(getHeader("Content-Transfer-Encoding"))) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while (true) {
                line = readLine();
                if (line == null) {
                    throw new IOException("Unexpected EOF");
                } else if (line.startsWith(this.boundary)) {
                    break;
                } else {
                    Base64.decode(line, os);
                }
            }
        } else {
            String deli = "\r\n" + this.boundary;
            int match = 0;
            while (true) {
                int i = this.is.read();
                if (i == -1) {
                    throw new RuntimeException("Unexpected EOF");
                } else if (((char) i) == deli.charAt(match)) {
                    match++;
                    if (match == deli.length()) {
                        break;
                    }
                } else {
                    if (match > 0) {
                        for (int j = 0; j < match; j++) {
                            os.write((byte) deli.charAt(j));
                        }
                        if (((char) i) == deli.charAt(0)) {
                            match = 1;
                        } else {
                            match = 0;
                        }
                    }
                    if (match == 0) {
                        os.write((byte) i);
                    }
                }
            }
            line = readLine();
        }
        if (line.endsWith("--")) {
            this.eof = true;
        }
        this.consumed = true;
    }

    public boolean next() throws IOException {
        if (!this.consumed) {
            readContent(null);
        }
        if (this.eof) {
            return false;
        }
        this.header = new Hashtable();
        while (true) {
            String line = readLine();
            if (line == null || line.equals("")) {
                this.consumed = false;
            } else {
                int cut = line.indexOf(58);
                if (cut == -1) {
                    throw new IOException("colon missing in multipart header line: " + line);
                }
                this.header.put(line.substring(0, cut).trim().toLowerCase(), line.substring(cut + 1).trim());
            }
        }
        this.consumed = false;
        return true;
    }
}
