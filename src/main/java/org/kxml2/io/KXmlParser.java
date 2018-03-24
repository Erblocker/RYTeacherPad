package org.kxml2.io;

import android.net.http.Headers;
import android.support.v4.internal.view.SupportMenu;
import android.support.v4.view.InputDeviceCompat;
import com.netspace.library.error.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Hashtable;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.protocol.HTTP;
import org.ksoap2.SoapEnvelope;
import org.kxml2.wap.Wbxml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class KXmlParser implements XmlPullParser {
    private static final String ILLEGAL_TYPE = "Wrong event type";
    private static final int LEGACY = 999;
    private static final String UNEXPECTED_EOF = "Unexpected EOF";
    private static final int XML_DECL = 998;
    private int attributeCount;
    private String[] attributes = new String[16];
    private int column;
    private boolean degenerated;
    private int depth;
    private String[] elementStack = new String[16];
    private String encoding;
    private Hashtable entityMap;
    private String error;
    private boolean isWhitespace;
    private int line;
    private Object location;
    private String name;
    private String namespace;
    private int[] nspCounts = new int[4];
    private String[] nspStack = new String[8];
    private int[] peek = new int[2];
    private int peekCount;
    private String prefix;
    private boolean processNsp;
    private Reader reader;
    private boolean relaxed;
    private char[] srcBuf;
    private int srcCount;
    private int srcPos;
    private Boolean standalone;
    private boolean token;
    private char[] txtBuf = new char[128];
    private int txtPos;
    private int type;
    private boolean unresolved;
    private String version;
    private boolean wasCR;

    public KXmlParser() {
        int i = 128;
        if (Runtime.getRuntime().freeMemory() >= 1048576) {
            i = 8192;
        }
        this.srcBuf = new char[i];
    }

    private final boolean isProp(String n1, boolean prop, String n2) {
        if (!n1.startsWith("http://xmlpull.org/v1/doc/")) {
            return false;
        }
        if (prop) {
            return n1.substring(42).equals(n2);
        }
        return n1.substring(40).equals(n2);
    }

    private final boolean adjustNsp() throws XmlPullParserException {
        int cut;
        boolean any = false;
        int i = 0;
        while (i < (this.attributeCount << 2)) {
            String prefix;
            String attrName = this.attributes[i + 2];
            cut = attrName.indexOf(58);
            if (cut != -1) {
                prefix = attrName.substring(0, cut);
                attrName = attrName.substring(cut + 1);
            } else if (attrName.equals("xmlns")) {
                prefix = attrName;
                attrName = null;
            } else {
                i += 4;
            }
            if (prefix.equals("xmlns")) {
                int[] iArr = this.nspCounts;
                int i2 = this.depth;
                int i3 = iArr[i2];
                iArr[i2] = i3 + 1;
                int j = i3 << 1;
                this.nspStack = ensureCapacity(this.nspStack, j + 2);
                this.nspStack[j] = attrName;
                this.nspStack[j + 1] = this.attributes[i + 3];
                if (attrName != null && this.attributes[i + 3].equals("")) {
                    error("illegal empty namespace");
                }
                Object obj = this.attributes;
                i2 = i + 4;
                Object obj2 = this.attributes;
                int i4 = this.attributeCount - 1;
                this.attributeCount = i4;
                System.arraycopy(obj, i2, obj2, i, (i4 << 2) - i);
                i -= 4;
            } else {
                any = true;
            }
            i += 4;
        }
        if (any) {
            i = (this.attributeCount << 2) - 4;
            while (i >= 0) {
                attrName = this.attributes[i + 2];
                cut = attrName.indexOf(58);
                if (cut != 0 || this.relaxed) {
                    if (cut != -1) {
                        String attrPrefix = attrName.substring(0, cut);
                        attrName = attrName.substring(cut + 1);
                        String attrNs = getNamespace(attrPrefix);
                        if (attrNs != null || this.relaxed) {
                            this.attributes[i] = attrNs;
                            this.attributes[i + 1] = attrPrefix;
                            this.attributes[i + 2] = attrName;
                        } else {
                            throw new RuntimeException("Undefined Prefix: " + attrPrefix + " in " + this);
                        }
                    }
                    i -= 4;
                } else {
                    throw new RuntimeException("illegal attribute name: " + attrName + " at " + this);
                }
            }
        }
        cut = this.name.indexOf(58);
        if (cut == 0) {
            error("illegal tag name: " + this.name);
        }
        if (cut != -1) {
            this.prefix = this.name.substring(0, cut);
            this.name = this.name.substring(cut + 1);
        }
        this.namespace = getNamespace(this.prefix);
        if (this.namespace == null) {
            if (this.prefix != null) {
                error("undefined prefix: " + this.prefix);
            }
            this.namespace = "";
        }
        return any;
    }

    private final String[] ensureCapacity(String[] arr, int required) {
        if (arr.length >= required) {
            return arr;
        }
        String[] bigger = new String[(required + 16)];
        System.arraycopy(arr, 0, bigger, 0, arr.length);
        return bigger;
    }

    private final void error(String desc) throws XmlPullParserException {
        if (!this.relaxed) {
            exception(desc);
        } else if (this.error == null) {
            this.error = "ERR: " + desc;
        }
    }

    private final void exception(String desc) throws XmlPullParserException {
        if (desc.length() >= 100) {
            desc = desc.substring(0, 100) + "\n";
        }
        throw new XmlPullParserException(desc, this, null);
    }

    private final void nextImpl() throws IOException, XmlPullParserException {
        boolean z = false;
        if (this.reader == null) {
            exception("No Input specified");
        }
        if (this.type == 3) {
            this.depth--;
        }
        do {
            this.attributeCount = -1;
            if (!this.degenerated) {
                if (this.error == null) {
                    this.prefix = null;
                    this.name = null;
                    this.namespace = null;
                    this.type = peekType();
                    switch (this.type) {
                        case 1:
                            return;
                        case 2:
                            parseStartTag(false);
                            return;
                        case 3:
                            parseEndTag();
                            return;
                        case 4:
                            if (!this.token) {
                                z = true;
                            }
                            pushText(60, z);
                            if (this.depth == 0 && this.isWhitespace) {
                                this.type = 7;
                                return;
                            }
                            return;
                        case 6:
                            pushEntity();
                            return;
                        default:
                            this.type = parseLegacy(this.token);
                            break;
                    }
                }
                for (int i = 0; i < this.error.length(); i++) {
                    push(this.error.charAt(i));
                }
                this.error = null;
                this.type = 9;
                return;
            }
            this.degenerated = false;
            this.type = 3;
            return;
        } while (this.type == XML_DECL);
    }

    private final int parseLegacy(boolean push) throws IOException, XmlPullParserException {
        int term;
        int result;
        String req = "";
        int prev = 0;
        read();
        int c = read();
        if (c == 63) {
            if ((peek(0) == SoapEnvelope.VER12 || peek(0) == 88) && (peek(1) == 109 || peek(1) == 77)) {
                if (push) {
                    push(peek(0));
                    push(peek(1));
                }
                read();
                read();
                if ((peek(0) == 108 || peek(0) == 76) && peek(1) <= 32) {
                    if (this.line != 1 || this.column > 4) {
                        error("PI must not start with xml");
                    }
                    parseStartTag(true);
                    if (this.attributeCount < 1 || !ClientCookie.VERSION_ATTR.equals(this.attributes[2])) {
                        error("version expected");
                    }
                    this.version = this.attributes[3];
                    int pos = 1;
                    if (1 < this.attributeCount && "encoding".equals(this.attributes[6])) {
                        this.encoding = this.attributes[7];
                        pos = 1 + 1;
                    }
                    if (pos < this.attributeCount && "standalone".equals(this.attributes[(pos * 4) + 2])) {
                        String st = this.attributes[(pos * 4) + 3];
                        if ("yes".equals(st)) {
                            this.standalone = new Boolean(true);
                        } else if ("no".equals(st)) {
                            this.standalone = new Boolean(false);
                        } else {
                            error("illegal standalone value: " + st);
                        }
                        pos++;
                    }
                    if (pos != this.attributeCount) {
                        error("illegal xmldecl");
                    }
                    this.isWhitespace = true;
                    this.txtPos = 0;
                    return XML_DECL;
                }
            }
            term = 63;
            result = 8;
        } else if (c != 33) {
            error("illegal: <" + c);
            return 9;
        } else if (peek(0) == 45) {
            result = 9;
            req = "--";
            term = 45;
        } else if (peek(0) == 91) {
            result = 5;
            req = "[CDATA[";
            term = 93;
            push = true;
        } else {
            result = 10;
            req = "DOCTYPE";
            term = -1;
        }
        for (int i = 0; i < req.length(); i++) {
            read(req.charAt(i));
        }
        if (result == 10) {
            parseDoctype(push);
            return result;
        }
        while (true) {
            c = read();
            if (c == -1) {
                error(UNEXPECTED_EOF);
                return 9;
            }
            if (push) {
                push(c);
            }
            if ((term == 63 || c == term) && peek(0) == term && peek(1) == 62) {
                break;
            }
            prev = c;
        }
        if (term == 45 && prev == 45 && !this.relaxed) {
            error("illegal comment delimiter: --->");
        }
        read();
        read();
        if (!push || term == 63) {
            return result;
        }
        this.txtPos--;
        return result;
    }

    private final void parseDoctype(boolean push) throws IOException, XmlPullParserException {
        int nesting = 1;
        boolean quoted = false;
        while (true) {
            int i = read();
            switch (i) {
                case -1:
                    error(UNEXPECTED_EOF);
                    return;
                case 39:
                    quoted = !quoted;
                    break;
                case 60:
                    if (!quoted) {
                        nesting++;
                        break;
                    }
                    break;
                case 62:
                    if (!quoted) {
                        nesting--;
                        if (nesting == 0) {
                            return;
                        }
                    }
                    break;
            }
            if (push) {
                push(i);
            }
        }
    }

    private final void parseEndTag() throws IOException, XmlPullParserException {
        read();
        read();
        this.name = readName();
        skip();
        read('>');
        int sp = (this.depth - 1) << 2;
        if (this.depth == 0) {
            error("element stack empty");
            this.type = 9;
        } else if (!this.relaxed) {
            if (!this.name.equals(this.elementStack[sp + 3])) {
                error("expected: /" + this.elementStack[sp + 3] + " read: " + this.name);
            }
            this.namespace = this.elementStack[sp];
            this.prefix = this.elementStack[sp + 1];
            this.name = this.elementStack[sp + 2];
        }
    }

    private final int peekType() throws IOException {
        switch (peek(0)) {
            case -1:
                return 1;
            case 38:
                return 6;
            case 60:
                switch (peek(1)) {
                    case 33:
                    case 63:
                        return LEGACY;
                    case 47:
                        return 3;
                    default:
                        return 2;
                }
            default:
                return 4;
        }
    }

    private final String get(int pos) {
        return new String(this.txtBuf, pos, this.txtPos - pos);
    }

    private final void push(int c) {
        this.isWhitespace = (c <= 32 ? 1 : 0) & this.isWhitespace;
        if (this.txtPos == this.txtBuf.length) {
            char[] bigger = new char[(((this.txtPos * 4) / 3) + 4)];
            System.arraycopy(this.txtBuf, 0, bigger, 0, this.txtPos);
            this.txtBuf = bigger;
        }
        char[] cArr = this.txtBuf;
        int i = this.txtPos;
        this.txtPos = i + 1;
        cArr[i] = (char) c;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void parseStartTag(boolean xmldecl) throws IOException, XmlPullParserException {
        int i;
        int sp;
        if (!xmldecl) {
            read();
        }
        this.name = readName();
        this.attributeCount = 0;
        while (true) {
            skip();
            int c = peek(0);
            String attrName;
            int i2;
            int i3;
            int delimiter;
            int p;
            if (xmldecl) {
                if (c == 63) {
                    read();
                    read('>');
                    return;
                }
                if (c != -1) {
                    error(UNEXPECTED_EOF);
                    return;
                }
                attrName = readName();
                if (attrName.length() == 0) {
                    break;
                }
                i = this.attributeCount;
                this.attributeCount = i + 1;
                i2 = i << 2;
                this.attributes = ensureCapacity(this.attributes, i2 + 4);
                i3 = i2 + 1;
                this.attributes[i2] = "";
                i2 = i3 + 1;
                this.attributes[i3] = null;
                i3 = i2 + 1;
                this.attributes[i2] = attrName;
                skip();
                if (peek(0) == 61) {
                    if (!this.relaxed) {
                        error("Attr.value missing f. " + attrName);
                    }
                    this.attributes[i3] = attrName;
                } else {
                    read('=');
                    skip();
                    delimiter = peek(0);
                    if (delimiter != 39 || delimiter == 34) {
                        read();
                    } else {
                        if (!this.relaxed) {
                            error("attr value delimiter missing!");
                        }
                        delimiter = 32;
                    }
                    p = this.txtPos;
                    pushText(delimiter, true);
                    this.attributes[i3] = get(p);
                    this.txtPos = p;
                    if (delimiter != 32) {
                        read();
                    }
                }
            } else if (c == 47) {
                break;
            } else {
                if (c == 62 && !xmldecl) {
                    break;
                }
                if (c != -1) {
                    attrName = readName();
                    if (attrName.length() == 0) {
                        break;
                    }
                    i = this.attributeCount;
                    this.attributeCount = i + 1;
                    i2 = i << 2;
                    this.attributes = ensureCapacity(this.attributes, i2 + 4);
                    i3 = i2 + 1;
                    this.attributes[i2] = "";
                    i2 = i3 + 1;
                    this.attributes[i3] = null;
                    i3 = i2 + 1;
                    this.attributes[i2] = attrName;
                    skip();
                    if (peek(0) == 61) {
                        read('=');
                        skip();
                        delimiter = peek(0);
                        if (delimiter != 39) {
                        }
                        read();
                        p = this.txtPos;
                        pushText(delimiter, true);
                        this.attributes[i3] = get(p);
                        this.txtPos = p;
                        if (delimiter != 32) {
                            read();
                        }
                    } else {
                        if (this.relaxed) {
                            error("Attr.value missing f. " + attrName);
                        }
                        this.attributes[i3] = attrName;
                    }
                } else {
                    error(UNEXPECTED_EOF);
                    return;
                }
            }
            i = this.depth;
            this.depth = i + 1;
            sp = i << 2;
            this.elementStack = ensureCapacity(this.elementStack, sp + 4);
            this.elementStack[sp + 3] = this.name;
            if (this.depth >= this.nspCounts.length) {
                int[] bigger = new int[(this.depth + 4)];
                System.arraycopy(this.nspCounts, 0, bigger, 0, this.nspCounts.length);
                this.nspCounts = bigger;
            }
            this.nspCounts[this.depth] = this.nspCounts[this.depth - 1];
            if (this.processNsp) {
                this.namespace = "";
            } else {
                adjustNsp();
            }
            this.elementStack[sp] = this.namespace;
            this.elementStack[sp + 1] = this.prefix;
            this.elementStack[sp + 2] = this.name;
        }
        error("attr name expected");
        i = this.depth;
        this.depth = i + 1;
        sp = i << 2;
        this.elementStack = ensureCapacity(this.elementStack, sp + 4);
        this.elementStack[sp + 3] = this.name;
        if (this.depth >= this.nspCounts.length) {
            int[] bigger2 = new int[(this.depth + 4)];
            System.arraycopy(this.nspCounts, 0, bigger2, 0, this.nspCounts.length);
            this.nspCounts = bigger2;
        }
        this.nspCounts[this.depth] = this.nspCounts[this.depth - 1];
        if (this.processNsp) {
            this.namespace = "";
        } else {
            adjustNsp();
        }
        this.elementStack[sp] = this.namespace;
        this.elementStack[sp + 1] = this.prefix;
        this.elementStack[sp + 2] = this.name;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void pushEntity() throws IOException, XmlPullParserException {
        boolean z = true;
        push(read());
        int pos = this.txtPos;
        while (true) {
            int c = peek(0);
            if (c != 59) {
                if (c < 128 && ((c < 48 || c > 57) && ((c < 97 || c > 122) && ((c < 65 || c > 90) && c != 95 && c != 45 && c != 35)))) {
                    break;
                }
                push(read());
            } else {
                break;
            }
        }
        if (!this.relaxed) {
            error("unterminated entity ref");
        }
        System.out.println("broken entitiy: " + get(pos - 1));
    }

    private final void pushText(int delimiter, boolean resolveEntities) throws IOException, XmlPullParserException {
        int next = peek(0);
        int cbrCount = 0;
        while (next != -1 && next != delimiter) {
            if (delimiter != 32 || (next > 32 && next != 62)) {
                if (next == 38) {
                    if (resolveEntities) {
                        pushEntity();
                    } else {
                        return;
                    }
                } else if (next == 10 && this.type == 2) {
                    read();
                    push(32);
                } else {
                    push(read());
                }
                if (next == 62 && cbrCount >= 2 && delimiter != 93) {
                    error("Illegal: ]]>");
                }
                if (next == 93) {
                    cbrCount++;
                } else {
                    cbrCount = 0;
                }
                next = peek(0);
            } else {
                return;
            }
        }
    }

    private final void read(char c) throws IOException, XmlPullParserException {
        char a = read();
        if (a != c) {
            error("expected: '" + c + "' actual: '" + ((char) a) + "'");
        }
    }

    private final int read() throws IOException {
        int result;
        if (this.peekCount == 0) {
            result = peek(0);
        } else {
            result = this.peek[0];
            this.peek[0] = this.peek[1];
        }
        this.peekCount--;
        this.column++;
        if (result == 10) {
            this.line++;
            this.column = 1;
        }
        return result;
    }

    private final int peek(int pos) throws IOException {
        while (pos >= this.peekCount) {
            int nw;
            if (this.srcBuf.length <= 1) {
                nw = this.reader.read();
            } else if (this.srcPos < this.srcCount) {
                char[] cArr = this.srcBuf;
                int i = this.srcPos;
                this.srcPos = i + 1;
                nw = cArr[i];
            } else {
                this.srcCount = this.reader.read(this.srcBuf, 0, this.srcBuf.length);
                if (this.srcCount <= 0) {
                    nw = -1;
                } else {
                    nw = this.srcBuf[0];
                }
                this.srcPos = 1;
            }
            int[] iArr;
            if (nw == 13) {
                this.wasCR = true;
                iArr = this.peek;
                i = this.peekCount;
                this.peekCount = i + 1;
                iArr[i] = 10;
            } else {
                if (nw != 10) {
                    iArr = this.peek;
                    i = this.peekCount;
                    this.peekCount = i + 1;
                    iArr[i] = nw;
                } else if (!this.wasCR) {
                    iArr = this.peek;
                    i = this.peekCount;
                    this.peekCount = i + 1;
                    iArr[i] = 10;
                }
                this.wasCR = false;
            }
        }
        return this.peek[pos];
    }

    private final String readName() throws IOException, XmlPullParserException {
        int pos = this.txtPos;
        int c = peek(0);
        if ((c < 97 || c > 122) && !((c >= 65 && c <= 90) || c == 95 || c == 58 || c >= Wbxml.EXT_0 || this.relaxed)) {
            error("name expected");
        }
        while (true) {
            push(read());
            c = peek(0);
            if ((c < 97 || c > 122) && ((c < 65 || c > 90) && !((c >= 48 && c <= 57) || c == 95 || c == 45 || c == 58 || c == 46 || c >= ErrorCode.ERROR_ALREADY_EXISTS))) {
                String result = get(pos);
                this.txtPos = pos;
                return result;
            }
        }
    }

    private final void skip() throws IOException {
        while (true) {
            int c = peek(0);
            if (c <= 32 && c != -1) {
                read();
            } else {
                return;
            }
        }
    }

    public void setInput(Reader reader) throws XmlPullParserException {
        this.reader = reader;
        this.line = 1;
        this.column = 0;
        this.type = 0;
        this.name = null;
        this.namespace = null;
        this.degenerated = false;
        this.attributeCount = -1;
        this.encoding = null;
        this.version = null;
        this.standalone = null;
        if (reader != null) {
            this.srcPos = 0;
            this.srcCount = 0;
            this.peekCount = 0;
            this.depth = 0;
            this.entityMap = new Hashtable();
            this.entityMap.put("amp", "&");
            this.entityMap.put("apos", "'");
            this.entityMap.put("gt", ">");
            this.entityMap.put("lt", "<");
            this.entityMap.put("quot", "\"");
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setInput(InputStream is, String _enc) throws XmlPullParserException {
        this.srcPos = 0;
        this.srcCount = 0;
        String enc = _enc;
        if (is == null) {
            throw new IllegalArgumentException();
        }
        if (enc == null) {
            int i;
            int chk = 0;
            while (this.srcCount < 4) {
                try {
                    i = is.read();
                    if (i == -1) {
                        break;
                    }
                    chk = (chk << 8) | i;
                    char[] cArr = this.srcBuf;
                    int i2 = this.srcCount;
                    this.srcCount = i2 + 1;
                    cArr[i2] = (char) i;
                } catch (Exception e) {
                    throw new XmlPullParserException("Invalid stream or encoding: " + e.toString(), this, e);
                }
            }
            if (this.srcCount == 4) {
                switch (chk) {
                    case -131072:
                        enc = "UTF-32LE";
                        this.srcCount = 0;
                        break;
                    case 60:
                        enc = "UTF-32BE";
                        this.srcBuf[0] = '<';
                        this.srcCount = 1;
                        break;
                    case 65279:
                        enc = "UTF-32BE";
                        this.srcCount = 0;
                        break;
                    case 3932223:
                        enc = "UTF-16BE";
                        this.srcBuf[0] = '<';
                        this.srcBuf[1] = '?';
                        this.srcCount = 2;
                        break;
                    case 1006632960:
                        enc = "UTF-32LE";
                        this.srcBuf[0] = '<';
                        this.srcCount = 1;
                        break;
                    case 1006649088:
                        enc = "UTF-16LE";
                        this.srcBuf[0] = '<';
                        this.srcBuf[1] = '?';
                        this.srcCount = 2;
                        break;
                    case 1010792557:
                        do {
                            i = is.read();
                            break;
                        } while (i != 62);
                        break;
                }
                if ((SupportMenu.CATEGORY_MASK & chk) == -16842752) {
                    enc = "UTF-16BE";
                    this.srcBuf[0] = (char) ((this.srcBuf[2] << 8) | this.srcBuf[3]);
                    this.srcCount = 1;
                } else if ((SupportMenu.CATEGORY_MASK & chk) == -131072) {
                    enc = "UTF-16LE";
                    this.srcBuf[0] = (char) ((this.srcBuf[3] << 8) | this.srcBuf[2]);
                    this.srcCount = 1;
                } else if ((chk & InputDeviceCompat.SOURCE_ANY) == -272908544) {
                    enc = HTTP.UTF_8;
                    this.srcBuf[0] = this.srcBuf[3];
                    this.srcCount = 1;
                }
            }
        }
        if (enc == null) {
            enc = HTTP.UTF_8;
        }
        int sc = this.srcCount;
        setInput(new InputStreamReader(is, enc));
        this.encoding = _enc;
        this.srcCount = sc;
    }

    public boolean getFeature(String feature) {
        if (XmlPullParser.FEATURE_PROCESS_NAMESPACES.equals(feature)) {
            return this.processNsp;
        }
        if (isProp(feature, false, "relaxed")) {
            return this.relaxed;
        }
        return false;
    }

    public String getInputEncoding() {
        return this.encoding;
    }

    public void defineEntityReplacementText(String entity, String value) throws XmlPullParserException {
        if (this.entityMap == null) {
            throw new RuntimeException("entity replacement text must be defined after setInput!");
        }
        this.entityMap.put(entity, value);
    }

    public Object getProperty(String property) {
        if (isProp(property, true, "xmldecl-version")) {
            return this.version;
        }
        if (isProp(property, true, "xmldecl-standalone")) {
            return this.standalone;
        }
        if (isProp(property, true, Headers.LOCATION)) {
            return this.location != null ? this.location : this.reader.toString();
        } else {
            return null;
        }
    }

    public int getNamespaceCount(int depth) {
        if (depth <= this.depth) {
            return this.nspCounts[depth];
        }
        throw new IndexOutOfBoundsException();
    }

    public String getNamespacePrefix(int pos) {
        return this.nspStack[pos << 1];
    }

    public String getNamespaceUri(int pos) {
        return this.nspStack[(pos << 1) + 1];
    }

    public String getNamespace(String prefix) {
        if ("xml".equals(prefix)) {
            return "http://www.w3.org/XML/1998/namespace";
        }
        if ("xmlns".equals(prefix)) {
            return "http://www.w3.org/2000/xmlns/";
        }
        for (int i = (getNamespaceCount(this.depth) << 1) - 2; i >= 0; i -= 2) {
            if (prefix == null) {
                if (this.nspStack[i] == null) {
                    return this.nspStack[i + 1];
                }
            } else if (prefix.equals(this.nspStack[i])) {
                return this.nspStack[i + 1];
            }
        }
        return null;
    }

    public int getDepth() {
        return this.depth;
    }

    public String getPositionDescription() {
        StringBuffer buf = new StringBuffer(this.type < TYPES.length ? TYPES[this.type] : "unknown");
        buf.append(' ');
        if (this.type == 2 || this.type == 3) {
            if (this.degenerated) {
                buf.append("(empty) ");
            }
            buf.append('<');
            if (this.type == 3) {
                buf.append('/');
            }
            if (this.prefix != null) {
                buf.append("{" + this.namespace + "}" + this.prefix + ":");
            }
            buf.append(this.name);
            int cnt = this.attributeCount << 2;
            for (int i = 0; i < cnt; i += 4) {
                buf.append(' ');
                if (this.attributes[i + 1] != null) {
                    buf.append("{" + this.attributes[i] + "}" + this.attributes[i + 1] + ":");
                }
                buf.append(this.attributes[i + 2] + "='" + this.attributes[i + 3] + "'");
            }
            buf.append('>');
        } else if (this.type != 7) {
            if (this.type != 4) {
                buf.append(getText());
            } else if (this.isWhitespace) {
                buf.append("(whitespace)");
            } else {
                String text = getText();
                if (text.length() > 16) {
                    text = text.substring(0, 16) + "...";
                }
                buf.append(text);
            }
        }
        buf.append("@" + this.line + ":" + this.column);
        if (this.location != null) {
            buf.append(" in ");
            buf.append(this.location);
        } else if (this.reader != null) {
            buf.append(" in ");
            buf.append(this.reader.toString());
        }
        return buf.toString();
    }

    public int getLineNumber() {
        return this.line;
    }

    public int getColumnNumber() {
        return this.column;
    }

    public boolean isWhitespace() throws XmlPullParserException {
        if (!(this.type == 4 || this.type == 7 || this.type == 5)) {
            exception(ILLEGAL_TYPE);
        }
        return this.isWhitespace;
    }

    public String getText() {
        return (this.type < 4 || (this.type == 6 && this.unresolved)) ? null : get(0);
    }

    public char[] getTextCharacters(int[] poslen) {
        if (this.type < 4) {
            poslen[0] = -1;
            poslen[1] = -1;
            return null;
        } else if (this.type == 6) {
            poslen[0] = 0;
            poslen[1] = this.name.length();
            return this.name.toCharArray();
        } else {
            poslen[0] = 0;
            poslen[1] = this.txtPos;
            return this.txtBuf;
        }
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getName() {
        return this.name;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public boolean isEmptyElementTag() throws XmlPullParserException {
        if (this.type != 2) {
            exception(ILLEGAL_TYPE);
        }
        return this.degenerated;
    }

    public int getAttributeCount() {
        return this.attributeCount;
    }

    public String getAttributeType(int index) {
        return "CDATA";
    }

    public boolean isAttributeDefault(int index) {
        return false;
    }

    public String getAttributeNamespace(int index) {
        if (index < this.attributeCount) {
            return this.attributes[index << 2];
        }
        throw new IndexOutOfBoundsException();
    }

    public String getAttributeName(int index) {
        if (index < this.attributeCount) {
            return this.attributes[(index << 2) + 2];
        }
        throw new IndexOutOfBoundsException();
    }

    public String getAttributePrefix(int index) {
        if (index < this.attributeCount) {
            return this.attributes[(index << 2) + 1];
        }
        throw new IndexOutOfBoundsException();
    }

    public String getAttributeValue(int index) {
        if (index < this.attributeCount) {
            return this.attributes[(index << 2) + 3];
        }
        throw new IndexOutOfBoundsException();
    }

    public String getAttributeValue(String namespace, String name) {
        int i = (this.attributeCount << 2) - 4;
        while (i >= 0) {
            if (this.attributes[i + 2].equals(name) && (namespace == null || this.attributes[i].equals(namespace))) {
                return this.attributes[i + 3];
            }
            i -= 4;
        }
        return null;
    }

    public int getEventType() throws XmlPullParserException {
        return this.type;
    }

    public int next() throws XmlPullParserException, IOException {
        this.txtPos = 0;
        this.isWhitespace = true;
        int minType = 9999;
        this.token = false;
        while (true) {
            nextImpl();
            if (this.type < minType) {
                minType = this.type;
            }
            if (minType > 6 || (minType >= 4 && peekType() >= 4)) {
            }
        }
        this.type = minType;
        if (this.type > 4) {
            this.type = 4;
        }
        return this.type;
    }

    public int nextToken() throws XmlPullParserException, IOException {
        this.isWhitespace = true;
        this.txtPos = 0;
        this.token = true;
        nextImpl();
        return this.type;
    }

    public int nextTag() throws XmlPullParserException, IOException {
        next();
        if (this.type == 4 && this.isWhitespace) {
            next();
        }
        if (!(this.type == 3 || this.type == 2)) {
            exception("unexpected type");
        }
        return this.type;
    }

    public void require(int type, String namespace, String name) throws XmlPullParserException, IOException {
        if (type != this.type || ((namespace != null && !namespace.equals(getNamespace())) || (name != null && !name.equals(getName())))) {
            exception("expected: " + TYPES[type] + " {" + namespace + "}" + name);
        }
    }

    public String nextText() throws XmlPullParserException, IOException {
        String result;
        if (this.type != 2) {
            exception("precondition: START_TAG");
        }
        next();
        if (this.type == 4) {
            result = getText();
            next();
        } else {
            result = "";
        }
        if (this.type != 3) {
            exception("END_TAG expected");
        }
        return result;
    }

    public void setFeature(String feature, boolean value) throws XmlPullParserException {
        if (XmlPullParser.FEATURE_PROCESS_NAMESPACES.equals(feature)) {
            this.processNsp = value;
        } else if (isProp(feature, false, "relaxed")) {
            this.relaxed = value;
        } else {
            exception("unsupported feature: " + feature);
        }
    }

    public void setProperty(String property, Object value) throws XmlPullParserException {
        if (isProp(property, true, Headers.LOCATION)) {
            this.location = value;
            return;
        }
        throw new XmlPullParserException("unsupported property: " + property);
    }

    public void skipSubTree() throws XmlPullParserException, IOException {
        require(2, null, null);
        int level = 1;
        while (level > 0) {
            int eventType = next();
            if (eventType == 3) {
                level--;
            } else if (eventType == 2) {
                level++;
            }
        }
    }
}
