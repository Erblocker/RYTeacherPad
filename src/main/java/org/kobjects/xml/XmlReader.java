package org.kobjects.xml;

import java.io.IOException;
import java.io.Reader;
import java.util.Hashtable;

public class XmlReader {
    static final int CDSECT = 5;
    public static final int END_DOCUMENT = 1;
    public static final int END_TAG = 3;
    static final int ENTITY_REF = 6;
    private static final int LEGACY = 999;
    public static final int START_DOCUMENT = 0;
    public static final int START_TAG = 2;
    public static final int TEXT = 4;
    private static final String UNEXPECTED_EOF = "Unexpected EOF";
    private String[] TYPES;
    private int attributeCount;
    private String[] attributes;
    private int column;
    private boolean degenerated;
    private int depth;
    private String[] elementStack = new String[4];
    private Hashtable entityMap;
    private boolean eof;
    private boolean isWhitespace;
    private int line;
    private String name;
    private int peek0;
    private int peek1;
    private Reader reader;
    public boolean relaxed;
    private char[] srcBuf;
    private int srcCount;
    private int srcPos;
    private String text;
    private char[] txtBuf;
    private int txtPos;
    private int type;

    private final int read() throws IOException {
        int r = this.peek0;
        this.peek0 = this.peek1;
        if (this.peek0 == -1) {
            this.eof = true;
        } else {
            if (r == 10 || r == 13) {
                this.line++;
                this.column = 0;
                if (r == 13 && this.peek0 == 10) {
                    this.peek0 = 0;
                }
            }
            this.column++;
            if (this.srcPos >= this.srcCount) {
                this.srcCount = this.reader.read(this.srcBuf, 0, this.srcBuf.length);
                if (this.srcCount <= 0) {
                    this.peek1 = -1;
                } else {
                    this.srcPos = 0;
                }
            }
            char[] cArr = this.srcBuf;
            int i = this.srcPos;
            this.srcPos = i + 1;
            this.peek1 = cArr[i];
        }
        return r;
    }

    private final void exception(String desc) throws IOException {
        throw new IOException(desc + " pos: " + getPositionDescription());
    }

    private final void push(int c) {
        if (c != 0) {
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
    }

    private final void read(char c) throws IOException {
        if (read() == c) {
            return;
        }
        if (!this.relaxed) {
            exception("expected: '" + c + "'");
        } else if (c <= ' ') {
            skip();
            read();
        }
    }

    private final void skip() throws IOException {
        while (!this.eof && this.peek0 <= 32) {
            read();
        }
    }

    private final String pop(int pos) {
        String result = new String(this.txtBuf, pos, this.txtPos - pos);
        this.txtPos = pos;
        return result;
    }

    private final String readName() throws IOException {
        int pos = this.txtPos;
        int c = this.peek0;
        if ((c < 97 || c > 122) && !((c >= 65 && c <= 90) || c == 95 || c == 58 || this.relaxed)) {
            exception("name expected");
        }
        while (true) {
            push(read());
            c = this.peek0;
            if ((c < 97 || c > 122) && ((c < 65 || c > 90) && !((c >= 48 && c <= 57) || c == 95 || c == 45 || c == 58 || c == 46))) {
                return pop(pos);
            }
        }
    }

    private final void parseLegacy(boolean push) throws IOException {
        int term;
        String req = "";
        read();
        int c = read();
        if (c == 63) {
            term = 63;
        } else if (c != 33) {
            if (c != 91) {
                exception("cantreachme: " + c);
            }
            req = "CDATA[";
            term = 93;
        } else if (this.peek0 == 45) {
            req = "--";
            term = 45;
        } else {
            req = "DOCTYPE";
            term = -1;
        }
        for (int i = 0; i < req.length(); i++) {
            read(req.charAt(i));
        }
        if (term == -1) {
            parseDoctype();
            return;
        }
        while (true) {
            if (this.eof) {
                exception(UNEXPECTED_EOF);
            }
            c = read();
            if (push) {
                push(c);
            }
            if ((term == 63 || c == term) && this.peek0 == term && this.peek1 == 62) {
                break;
            }
        }
        read();
        read();
        if (push && term != 63) {
            pop(this.txtPos - 1);
        }
    }

    private final void parseDoctype() throws IOException {
        int nesting = 1;
        while (true) {
            switch (read()) {
                case -1:
                    exception(UNEXPECTED_EOF);
                    break;
                case 60:
                    break;
                case 62:
                    nesting--;
                    if (nesting != 0) {
                        continue;
                    } else {
                        return;
                    }
                default:
                    continue;
            }
            nesting++;
        }
    }

    private final void parseEndTag() throws IOException {
        read();
        read();
        this.name = readName();
        if (this.depth == 0 && !this.relaxed) {
            exception("element stack empty");
        }
        if (this.name.equals(this.elementStack[this.depth - 1])) {
            this.depth--;
        } else if (!this.relaxed) {
            exception("expected: " + this.elementStack[this.depth]);
        }
        skip();
        read('>');
    }

    private final int peekType() {
        switch (this.peek0) {
            case -1:
                return 1;
            case 38:
                return 6;
            case 60:
                switch (this.peek1) {
                    case 33:
                    case 63:
                        return LEGACY;
                    case 47:
                        return 3;
                    case 91:
                        return 5;
                    default:
                        return 2;
                }
            default:
                return 4;
        }
    }

    private static final String[] ensureCapacity(String[] arr, int required) {
        if (arr.length >= required) {
            return arr;
        }
        String[] bigger = new String[(required + 16)];
        System.arraycopy(arr, 0, bigger, 0, arr.length);
        return bigger;
    }

    private final void parseStartTag() throws IOException {
        read();
        this.name = readName();
        this.elementStack = ensureCapacity(this.elementStack, this.depth + 1);
        String[] strArr = this.elementStack;
        int i = this.depth;
        this.depth = i + 1;
        strArr[i] = this.name;
        while (true) {
            skip();
            int c = this.peek0;
            if (c == 47) {
                this.degenerated = true;
                read();
                skip();
                read('>');
                return;
            } else if (c == 62) {
                read();
                return;
            } else {
                if (c == -1) {
                    exception(UNEXPECTED_EOF);
                }
                String attrName = readName();
                if (attrName.length() == 0) {
                    exception("attr name expected");
                }
                skip();
                read('=');
                skip();
                int delimiter = read();
                if (!(delimiter == 39 || delimiter == 34)) {
                    if (!this.relaxed) {
                        exception("<" + this.name + ">: invalid delimiter: " + ((char) delimiter));
                    }
                    delimiter = 32;
                }
                int i2 = this.attributeCount;
                this.attributeCount = i2 + 1;
                int i3 = i2 << 1;
                this.attributes = ensureCapacity(this.attributes, i3 + 4);
                int i4 = i3 + 1;
                this.attributes[i3] = attrName;
                int p = this.txtPos;
                pushText(delimiter);
                this.attributes[i4] = pop(p);
                if (delimiter != 32) {
                    read();
                }
            }
        }
    }

    public final boolean pushEntity() throws IOException {
        read();
        int pos = this.txtPos;
        while (!this.eof && this.peek0 != 59) {
            push(read());
        }
        String code = pop(pos);
        read();
        if (code.length() <= 0 || code.charAt(0) != '#') {
            String result = (String) this.entityMap.get(code);
            boolean whitespace = true;
            if (result == null) {
                result = "&" + code + ";";
            }
            for (int i = 0; i < result.length(); i++) {
                char c = result.charAt(i);
                if (c > ' ') {
                    whitespace = false;
                }
                push(c);
            }
            return whitespace;
        }
        int c2 = code.charAt(1) == 'x' ? Integer.parseInt(code.substring(2), 16) : Integer.parseInt(code.substring(1));
        push(c2);
        if (c2 <= 32) {
            return true;
        }
        return false;
    }

    private final boolean pushText(int delimiter) throws IOException {
        boolean whitespace = true;
        int next = this.peek0;
        while (!this.eof && next != delimiter && (delimiter != 32 || (next > 32 && next != 62))) {
            if (next != 38) {
                if (next > 32) {
                    whitespace = false;
                }
                push(read());
            } else if (!pushEntity()) {
                whitespace = false;
            }
            next = this.peek0;
        }
        return whitespace;
    }

    public XmlReader(Reader reader) throws IOException {
        int i;
        if (Runtime.getRuntime().freeMemory() >= 1048576) {
            i = 8192;
        } else {
            i = 128;
        }
        this.srcBuf = new char[i];
        this.txtBuf = new char[128];
        this.attributes = new String[16];
        this.TYPES = new String[]{"Start Document", "End Document", "Start Tag", "End Tag", "Text"};
        this.reader = reader;
        this.peek0 = reader.read();
        this.peek1 = reader.read();
        this.eof = this.peek0 == -1;
        this.entityMap = new Hashtable();
        this.entityMap.put("amp", "&");
        this.entityMap.put("apos", "'");
        this.entityMap.put("gt", ">");
        this.entityMap.put("lt", "<");
        this.entityMap.put("quot", "\"");
        this.line = 1;
        this.column = 1;
    }

    public void defineCharacterEntity(String entity, String value) {
        this.entityMap.put(entity, value);
    }

    public int getDepth() {
        return this.depth;
    }

    public String getPositionDescription() {
        StringBuffer buf = new StringBuffer(this.type < this.TYPES.length ? this.TYPES[this.type] : "Other");
        buf.append(" @" + this.line + ":" + this.column + ": ");
        if (this.type == 2 || this.type == 3) {
            buf.append('<');
            if (this.type == 3) {
                buf.append('/');
            }
            buf.append(this.name);
            buf.append('>');
        } else if (this.isWhitespace) {
            buf.append("[whitespace]");
        } else {
            buf.append(getText());
        }
        return buf.toString();
    }

    public int getLineNumber() {
        return this.line;
    }

    public int getColumnNumber() {
        return this.column;
    }

    public boolean isWhitespace() {
        return this.isWhitespace;
    }

    public String getText() {
        if (this.text == null) {
            this.text = pop(0);
        }
        return this.text;
    }

    public String getName() {
        return this.name;
    }

    public boolean isEmptyElementTag() {
        return this.degenerated;
    }

    public int getAttributeCount() {
        return this.attributeCount;
    }

    public String getAttributeName(int index) {
        if (index < this.attributeCount) {
            return this.attributes[index << 1];
        }
        throw new IndexOutOfBoundsException();
    }

    public String getAttributeValue(int index) {
        if (index < this.attributeCount) {
            return this.attributes[(index << 1) + 1];
        }
        throw new IndexOutOfBoundsException();
    }

    public String getAttributeValue(String name) {
        for (int i = (this.attributeCount << 1) - 2; i >= 0; i -= 2) {
            if (this.attributes[i].equals(name)) {
                return this.attributes[i + 1];
            }
        }
        return null;
    }

    public int getType() {
        return this.type;
    }

    public int next() throws IOException {
        int i = 1;
        if (this.degenerated) {
            this.type = 3;
            this.degenerated = false;
            this.depth--;
            return this.type;
        }
        this.txtPos = 0;
        this.isWhitespace = true;
        while (true) {
            this.attributeCount = 0;
            this.name = null;
            this.text = null;
            this.type = peekType();
            switch (this.type) {
                case 1:
                    break;
                case 2:
                    parseStartTag();
                    break;
                case 3:
                    parseEndTag();
                    break;
                case 4:
                    this.isWhitespace &= pushText(60);
                    break;
                case 5:
                    parseLegacy(true);
                    this.isWhitespace = false;
                    this.type = 4;
                    break;
                case 6:
                    this.isWhitespace &= pushEntity();
                    this.type = 4;
                    break;
                default:
                    parseLegacy(false);
                    break;
            }
            if (this.type <= 4 && (this.type != 4 || peekType() < 4)) {
                boolean z = this.isWhitespace;
                if (this.type != 4) {
                    i = 0;
                }
                this.isWhitespace = i & z;
                return this.type;
            }
        }
    }

    public void require(int type, String name) throws IOException {
        if (this.type == 4 && type != 4 && isWhitespace()) {
            next();
        }
        if (type != this.type || (name != null && !name.equals(getName()))) {
            exception("expected: " + this.TYPES[type] + "/" + name);
        }
    }

    public String readText() throws IOException {
        if (this.type != 4) {
            return "";
        }
        String result = getText();
        next();
        return result;
    }
}
