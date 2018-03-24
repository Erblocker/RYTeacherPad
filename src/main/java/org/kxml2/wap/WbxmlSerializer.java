package org.kxml2.wap;

import android.support.v4.media.TransportMediator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.http.protocol.HTTP;
import org.xmlpull.v1.XmlSerializer;

public class WbxmlSerializer implements XmlSerializer {
    private int attrPage;
    Hashtable attrStartTable = new Hashtable();
    Hashtable attrValueTable = new Hashtable();
    Vector attributes = new Vector();
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    int depth;
    private String encoding;
    private boolean headerSent = false;
    String name;
    String namespace;
    OutputStream out;
    String pending;
    Hashtable stringTable = new Hashtable();
    ByteArrayOutputStream stringTableBuf = new ByteArrayOutputStream();
    private int tagPage;
    Hashtable tagTable = new Hashtable();

    public XmlSerializer attribute(String namespace, String name, String value) {
        this.attributes.addElement(name);
        this.attributes.addElement(value);
        return this;
    }

    public void cdsect(String cdsect) throws IOException {
        text(cdsect);
    }

    public void comment(String comment) {
    }

    public void docdecl(String docdecl) {
        throw new RuntimeException("Cannot write docdecl for WBXML");
    }

    public void entityRef(String er) {
        throw new RuntimeException("EntityReference not supported for WBXML");
    }

    public int getDepth() {
        return this.depth;
    }

    public boolean getFeature(String name) {
        return false;
    }

    public String getNamespace() {
        return null;
    }

    public String getName() {
        return this.pending;
    }

    public String getPrefix(String nsp, boolean create) {
        throw new RuntimeException("NYI");
    }

    public Object getProperty(String name) {
        return null;
    }

    public void ignorableWhitespace(String sp) {
    }

    public void endDocument() throws IOException {
        flush();
    }

    public void flush() throws IOException {
        checkPending(false);
        if (!this.headerSent) {
            writeInt(this.out, this.stringTableBuf.size());
            this.out.write(this.stringTableBuf.toByteArray());
            this.headerSent = true;
        }
        this.out.write(this.buf.toByteArray());
        this.buf.reset();
    }

    public void checkPending(boolean degenerated) throws IOException {
        if (this.pending != null) {
            int len = this.attributes.size();
            int[] idx = (int[]) this.tagTable.get(this.pending);
            ByteArrayOutputStream byteArrayOutputStream;
            int i;
            if (idx == null) {
                byteArrayOutputStream = this.buf;
                i = len == 0 ? degenerated ? 4 : 68 : degenerated ? Wbxml.LITERAL_A : Wbxml.LITERAL_AC;
                byteArrayOutputStream.write(i);
                writeStrT(this.pending, false);
            } else {
                if (idx[0] != this.tagPage) {
                    this.tagPage = idx[0];
                    this.buf.write(0);
                    this.buf.write(this.tagPage);
                }
                byteArrayOutputStream = this.buf;
                i = len == 0 ? degenerated ? idx[1] : idx[1] | 64 : degenerated ? idx[1] | 128 : idx[1] | Wbxml.EXT_0;
                byteArrayOutputStream.write(i);
            }
            int i2 = 0;
            while (i2 < len) {
                idx = (int[]) this.attrStartTable.get(this.attributes.elementAt(i2));
                if (idx == null) {
                    this.buf.write(4);
                    writeStrT((String) this.attributes.elementAt(i2), false);
                } else {
                    if (idx[0] != this.attrPage) {
                        this.attrPage = idx[0];
                        this.buf.write(0);
                        this.buf.write(this.attrPage);
                    }
                    this.buf.write(idx[1]);
                }
                i2++;
                idx = (int[]) this.attrValueTable.get(this.attributes.elementAt(i2));
                if (idx == null) {
                    writeStr((String) this.attributes.elementAt(i2));
                } else {
                    if (idx[0] != this.attrPage) {
                        this.attrPage = idx[0];
                        this.buf.write(0);
                        this.buf.write(this.attrPage);
                    }
                    this.buf.write(idx[1]);
                }
                i2++;
            }
            if (len > 0) {
                this.buf.write(1);
            }
            this.pending = null;
            this.attributes.removeAllElements();
        }
    }

    public void processingInstruction(String pi) {
        throw new RuntimeException("PI NYI");
    }

    public void setFeature(String name, boolean value) {
        throw new IllegalArgumentException("unknown feature " + name);
    }

    public void setOutput(Writer writer) {
        throw new RuntimeException("Wbxml requires an OutputStream!");
    }

    public void setOutput(OutputStream out, String encoding) throws IOException {
        if (encoding == null) {
            encoding = HTTP.UTF_8;
        }
        this.encoding = encoding;
        this.out = out;
        this.buf = new ByteArrayOutputStream();
        this.stringTableBuf = new ByteArrayOutputStream();
        this.headerSent = false;
    }

    public void setPrefix(String prefix, String nsp) {
        throw new RuntimeException("NYI");
    }

    public void setProperty(String property, Object value) {
        throw new IllegalArgumentException("unknown property " + property);
    }

    public void startDocument(String encoding, Boolean standalone) throws IOException {
        this.out.write(3);
        this.out.write(1);
        if (encoding != null) {
            this.encoding = encoding;
        }
        if (this.encoding.toUpperCase().equals(HTTP.UTF_8)) {
            this.out.write(106);
        } else if (this.encoding.toUpperCase().equals("ISO-8859-1")) {
            this.out.write(4);
        } else {
            throw new UnsupportedEncodingException(encoding);
        }
    }

    public XmlSerializer startTag(String namespace, String name) throws IOException {
        if (namespace == null || "".equals(namespace)) {
            checkPending(false);
            this.pending = name;
            this.depth++;
            return this;
        }
        throw new RuntimeException("NSP NYI");
    }

    public XmlSerializer text(char[] chars, int start, int len) throws IOException {
        checkPending(false);
        writeStr(new String(chars, start, len));
        return this;
    }

    public XmlSerializer text(String text) throws IOException {
        checkPending(false);
        writeStr(text);
        return this;
    }

    private void writeStr(String text) throws IOException {
        int p0 = 0;
        int lastCut = 0;
        int len = text.length();
        if (this.headerSent) {
            writeStrI(this.buf, text);
            return;
        }
        while (p0 < len) {
            while (p0 < len && text.charAt(p0) < 'A') {
                p0++;
            }
            int p1 = p0;
            while (p1 < len && text.charAt(p1) >= 'A') {
                p1++;
            }
            if (p1 - p0 > 10) {
                if (p0 > lastCut && text.charAt(p0 - 1) == ' ' && this.stringTable.get(text.substring(p0, p1)) == null) {
                    this.buf.write(Wbxml.STR_T);
                    writeStrT(text.substring(lastCut, p1), false);
                } else {
                    if (p0 > lastCut && text.charAt(p0 - 1) == ' ') {
                        p0--;
                    }
                    if (p0 > lastCut) {
                        this.buf.write(Wbxml.STR_T);
                        writeStrT(text.substring(lastCut, p0), false);
                    }
                    this.buf.write(Wbxml.STR_T);
                    writeStrT(text.substring(p0, p1), true);
                }
                lastCut = p1;
            }
            p0 = p1;
        }
        if (lastCut < len) {
            this.buf.write(Wbxml.STR_T);
            writeStrT(text.substring(lastCut, len), false);
        }
    }

    public XmlSerializer endTag(String namespace, String name) throws IOException {
        if (this.pending != null) {
            checkPending(true);
        } else {
            this.buf.write(1);
        }
        this.depth--;
        return this;
    }

    public void writeWapExtension(int type, Object data) throws IOException {
        checkPending(false);
        this.buf.write(type);
        switch (type) {
            case 64:
            case 65:
            case 66:
                writeStrI(this.buf, (String) data);
                return;
            case 128:
            case 129:
            case 130:
                writeStrT((String) data, false);
                return;
            case Wbxml.EXT_0 /*192*/:
            case Wbxml.EXT_1 /*193*/:
            case Wbxml.EXT_2 /*194*/:
                return;
            case Wbxml.OPAQUE /*195*/:
                byte[] bytes = (byte[]) data;
                writeInt(this.buf, bytes.length);
                this.buf.write(bytes);
                return;
            default:
                throw new IllegalArgumentException();
        }
    }

    static void writeInt(OutputStream out, int i) throws IOException {
        int idx;
        byte[] buf = new byte[5];
        int i2 = 0;
        while (true) {
            idx = i2 + 1;
            buf[i2] = (byte) (i & TransportMediator.KEYCODE_MEDIA_PAUSE);
            i >>= 7;
            if (i == 0) {
                break;
            }
            i2 = idx;
        }
        i2 = idx;
        while (i2 > 1) {
            i2--;
            out.write(buf[i2] | 128);
        }
        out.write(buf[0]);
    }

    void writeStrI(OutputStream out, String s) throws IOException {
        out.write(s.getBytes(this.encoding));
        out.write(0);
    }

    private final void writeStrT(String s, boolean mayPrependSpace) throws IOException {
        Integer idx = (Integer) this.stringTable.get(s);
        writeInt(this.buf, idx == null ? addToStringTable(s, mayPrependSpace) : idx.intValue());
    }

    public int addToStringTable(String s, boolean mayPrependSpace) throws IOException {
        if (this.headerSent) {
            throw new IOException("stringtable sent");
        }
        int i = this.stringTableBuf.size();
        int offset = i;
        if (s.charAt(0) >= '0' && mayPrependSpace) {
            s = ' ' + s;
            offset++;
        }
        this.stringTable.put(s, new Integer(i));
        if (s.charAt(0) == ' ') {
            this.stringTable.put(s.substring(1), new Integer(i + 1));
        }
        int j = s.lastIndexOf(32);
        if (j > 1) {
            this.stringTable.put(s.substring(j), new Integer(i + j));
            this.stringTable.put(s.substring(j + 1), new Integer((i + j) + 1));
        }
        writeStrI(this.stringTableBuf, s);
        this.stringTableBuf.flush();
        return offset;
    }

    public void setTagTable(int page, String[] tagTable) {
        for (int i = 0; i < tagTable.length; i++) {
            if (tagTable[i] != null) {
                this.tagTable.put(tagTable[i], new int[]{page, i + 5});
            }
        }
    }

    public void setAttrStartTable(int page, String[] attrStartTable) {
        for (int i = 0; i < attrStartTable.length; i++) {
            if (attrStartTable[i] != null) {
                this.attrStartTable.put(attrStartTable[i], new int[]{page, i + 5});
            }
        }
    }

    public void setAttrValueTable(int page, String[] attrValueTable) {
        for (int i = 0; i < attrValueTable.length; i++) {
            if (attrValueTable[i] != null) {
                this.attrValueTable.put(attrValueTable[i], new int[]{page, i + 133});
            }
        }
    }
}
