package org.kxml2.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.xmlpull.v1.XmlSerializer;

public class KXmlSerializer implements XmlSerializer {
    private int auto;
    private int depth;
    private String[] elementStack = new String[12];
    private String encoding;
    private boolean[] indent = new boolean[4];
    private int[] nspCounts = new int[4];
    private String[] nspStack = new String[8];
    private boolean pending;
    private boolean unicode;
    private Writer writer;

    private final void check(boolean close) throws IOException {
        if (this.pending) {
            this.depth++;
            this.pending = false;
            if (this.indent.length <= this.depth) {
                boolean[] hlp = new boolean[(this.depth + 4)];
                System.arraycopy(this.indent, 0, hlp, 0, this.depth);
                this.indent = hlp;
            }
            this.indent[this.depth] = this.indent[this.depth - 1];
            int i = this.nspCounts[this.depth - 1];
            while (i < this.nspCounts[this.depth]) {
                this.writer.write(32);
                this.writer.write("xmlns");
                if (!"".equals(this.nspStack[i * 2])) {
                    this.writer.write(58);
                    this.writer.write(this.nspStack[i * 2]);
                } else if ("".equals(getNamespace()) && !"".equals(this.nspStack[(i * 2) + 1])) {
                    throw new IllegalStateException("Cannot set default namespace for elements in no namespace");
                }
                this.writer.write("=\"");
                writeEscaped(this.nspStack[(i * 2) + 1], 34);
                this.writer.write(34);
                i++;
            }
            if (this.nspCounts.length <= this.depth + 1) {
                int[] hlp2 = new int[(this.depth + 8)];
                System.arraycopy(this.nspCounts, 0, hlp2, 0, this.depth + 1);
                this.nspCounts = hlp2;
            }
            this.nspCounts[this.depth + 1] = this.nspCounts[this.depth];
            this.writer.write(close ? " />" : ">");
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void writeEscaped(String s, int quot) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\t':
                case '\n':
                case '\r':
                    if (quot != -1) {
                        this.writer.write("&#" + c + ';');
                        break;
                    } else {
                        this.writer.write(c);
                        break;
                    }
                case '\"':
                case '\'':
                    if (c == quot) {
                        this.writer.write(c == '\"' ? "&quot;" : "&apos;");
                        break;
                    }
                case '&':
                    this.writer.write("&amp;");
                    break;
                case '<':
                    this.writer.write("&lt;");
                    break;
                case '>':
                    this.writer.write("&gt;");
                    break;
                default:
                    if (c >= ' ' && c != '@' && (c < '' || this.unicode)) {
                        this.writer.write(c);
                        break;
                    } else {
                        this.writer.write("&#" + c + ";");
                        break;
                    }
                    break;
            }
        }
    }

    public void docdecl(String dd) throws IOException {
        this.writer.write("<!DOCTYPE");
        this.writer.write(dd);
        this.writer.write(">");
    }

    public void endDocument() throws IOException {
        while (this.depth > 0) {
            endTag(this.elementStack[(this.depth * 3) - 3], this.elementStack[(this.depth * 3) - 1]);
        }
        flush();
    }

    public void entityRef(String name) throws IOException {
        check(false);
        this.writer.write(38);
        this.writer.write(name);
        this.writer.write(59);
    }

    public boolean getFeature(String name) {
        return "http://xmlpull.org/v1/doc/features.html#indent-output".equals(name) ? this.indent[this.depth] : false;
    }

    public String getPrefix(String namespace, boolean create) {
        try {
            return getPrefix(namespace, false, create);
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
    }

    private final String getPrefix(String namespace, boolean includeDefault, boolean create) throws IOException {
        int i = (this.nspCounts[this.depth + 1] * 2) - 2;
        while (i >= 0) {
            if (this.nspStack[i + 1].equals(namespace) && (includeDefault || !this.nspStack[i].equals(""))) {
                String cand = this.nspStack[i];
                for (int j = i + 2; j < this.nspCounts[this.depth + 1] * 2; j++) {
                    if (this.nspStack[j].equals(cand)) {
                        cand = null;
                        break;
                    }
                }
                if (cand != null) {
                    return cand;
                }
            }
            i -= 2;
        }
        if (!create) {
            return null;
        }
        String prefix;
        if ("".equals(namespace)) {
            prefix = "";
        } else {
            do {
                StringBuilder append = new StringBuilder().append("n");
                int i2 = this.auto;
                this.auto = i2 + 1;
                prefix = append.append(i2).toString();
                for (i = (this.nspCounts[this.depth + 1] * 2) - 2; i >= 0; i -= 2) {
                    if (prefix.equals(this.nspStack[i])) {
                        prefix = null;
                        continue;
                        break;
                    }
                }
            } while (prefix == null);
        }
        boolean p = this.pending;
        this.pending = false;
        setPrefix(prefix, namespace);
        this.pending = p;
        return prefix;
    }

    public Object getProperty(String name) {
        throw new RuntimeException("Unsupported property");
    }

    public void ignorableWhitespace(String s) throws IOException {
        text(s);
    }

    public void setFeature(String name, boolean value) {
        if ("http://xmlpull.org/v1/doc/features.html#indent-output".equals(name)) {
            this.indent[this.depth] = value;
            return;
        }
        throw new RuntimeException("Unsupported Feature");
    }

    public void setProperty(String name, Object value) {
        throw new RuntimeException("Unsupported Property:" + value);
    }

    public void setPrefix(String prefix, String namespace) throws IOException {
        check(false);
        if (prefix == null) {
            prefix = "";
        }
        if (namespace == null) {
            namespace = "";
        }
        if (!prefix.equals(getPrefix(namespace, true, false))) {
            int[] iArr = this.nspCounts;
            int i = this.depth + 1;
            int i2 = iArr[i];
            iArr[i] = i2 + 1;
            int pos = i2 << 1;
            if (this.nspStack.length < pos + 1) {
                String[] hlp = new String[(this.nspStack.length + 16)];
                System.arraycopy(this.nspStack, 0, hlp, 0, pos);
                this.nspStack = hlp;
            }
            int pos2 = pos + 1;
            this.nspStack[pos] = prefix;
            this.nspStack[pos2] = namespace;
        }
    }

    public void setOutput(Writer writer) {
        this.writer = writer;
        this.nspCounts[0] = 2;
        this.nspCounts[1] = 2;
        this.nspStack[0] = "";
        this.nspStack[1] = "";
        this.nspStack[2] = "xml";
        this.nspStack[3] = "http://www.w3.org/XML/1998/namespace";
        this.pending = false;
        this.auto = 0;
        this.depth = 0;
        this.unicode = false;
    }

    public void setOutput(OutputStream os, String encoding) throws IOException {
        if (os == null) {
            throw new IllegalArgumentException();
        }
        setOutput(encoding == null ? new OutputStreamWriter(os) : new OutputStreamWriter(os, encoding));
        this.encoding = encoding;
        if (encoding != null && encoding.toLowerCase().startsWith("utf")) {
            this.unicode = true;
        }
    }

    public void startDocument(String encoding, Boolean standalone) throws IOException {
        this.writer.write("<?xml version='1.0' ");
        if (encoding != null) {
            this.encoding = encoding;
            if (encoding.toLowerCase().startsWith("utf")) {
                this.unicode = true;
            }
        }
        if (this.encoding != null) {
            this.writer.write("encoding='");
            this.writer.write(this.encoding);
            this.writer.write("' ");
        }
        if (standalone != null) {
            this.writer.write("standalone='");
            this.writer.write(standalone.booleanValue() ? "yes" : "no");
            this.writer.write("' ");
        }
        this.writer.write("?>");
    }

    public XmlSerializer startTag(String namespace, String name) throws IOException {
        int i;
        check(false);
        if (this.indent[this.depth]) {
            this.writer.write("\r\n");
            for (i = 0; i < this.depth; i++) {
                this.writer.write("  ");
            }
        }
        int esp = this.depth * 3;
        if (this.elementStack.length < esp + 3) {
            String[] hlp = new String[(this.elementStack.length + 12)];
            System.arraycopy(this.elementStack, 0, hlp, 0, esp);
            this.elementStack = hlp;
        }
        String prefix = namespace == null ? "" : getPrefix(namespace, true, true);
        if ("".equals(namespace)) {
            i = this.nspCounts[this.depth];
            while (i < this.nspCounts[this.depth + 1]) {
                if (!"".equals(this.nspStack[i * 2]) || "".equals(this.nspStack[(i * 2) + 1])) {
                    i++;
                } else {
                    throw new IllegalStateException("Cannot set default namespace for elements in no namespace");
                }
            }
        }
        int esp2 = esp + 1;
        this.elementStack[esp] = namespace;
        esp = esp2 + 1;
        this.elementStack[esp2] = prefix;
        this.elementStack[esp] = name;
        this.writer.write(60);
        if (!"".equals(prefix)) {
            this.writer.write(prefix);
            this.writer.write(58);
        }
        this.writer.write(name);
        this.pending = true;
        return this;
    }

    public XmlSerializer attribute(String namespace, String name, String value) throws IOException {
        char q = '\"';
        if (this.pending) {
            String prefix;
            if (namespace == null) {
                namespace = "";
            }
            if ("".equals(namespace)) {
                prefix = "";
            } else {
                prefix = getPrefix(namespace, false, true);
            }
            this.writer.write(32);
            if (!"".equals(prefix)) {
                this.writer.write(prefix);
                this.writer.write(58);
            }
            this.writer.write(name);
            this.writer.write(61);
            if (value.indexOf(34) != -1) {
                q = '\'';
            }
            this.writer.write(q);
            writeEscaped(value, q);
            this.writer.write(q);
            return this;
        }
        throw new IllegalStateException("illegal position for attribute");
    }

    public void flush() throws IOException {
        check(false);
        this.writer.flush();
    }

    public XmlSerializer endTag(String namespace, String name) throws IOException {
        if (!this.pending) {
            this.depth--;
        }
        if ((namespace != null || this.elementStack[this.depth * 3] == null) && ((namespace == null || namespace.equals(this.elementStack[this.depth * 3])) && this.elementStack[(this.depth * 3) + 2].equals(name))) {
            if (this.pending) {
                check(true);
                this.depth--;
            } else {
                if (this.indent[this.depth + 1]) {
                    this.writer.write("\r\n");
                    for (int i = 0; i < this.depth; i++) {
                        this.writer.write("  ");
                    }
                }
                this.writer.write("</");
                String prefix = this.elementStack[(this.depth * 3) + 1];
                if (!"".equals(prefix)) {
                    this.writer.write(prefix);
                    this.writer.write(58);
                }
                this.writer.write(name);
                this.writer.write(62);
            }
            this.nspCounts[this.depth + 1] = this.nspCounts[this.depth];
            return this;
        }
        throw new IllegalArgumentException("</{" + namespace + "}" + name + "> does not match start");
    }

    public String getNamespace() {
        return getDepth() == 0 ? null : this.elementStack[(getDepth() * 3) - 3];
    }

    public String getName() {
        return getDepth() == 0 ? null : this.elementStack[(getDepth() * 3) - 1];
    }

    public int getDepth() {
        return this.pending ? this.depth + 1 : this.depth;
    }

    public XmlSerializer text(String text) throws IOException {
        check(false);
        this.indent[this.depth] = false;
        writeEscaped(text, -1);
        return this;
    }

    public XmlSerializer text(char[] text, int start, int len) throws IOException {
        text(new String(text, start, len));
        return this;
    }

    public void cdsect(String data) throws IOException {
        check(false);
        this.writer.write("<![CDATA[");
        this.writer.write(data);
        this.writer.write("]]>");
    }

    public void comment(String comment) throws IOException {
        check(false);
        this.writer.write("<!--");
        this.writer.write(comment);
        this.writer.write("-->");
    }

    public void processingInstruction(String pi) throws IOException {
        check(false);
        this.writer.write("<?");
        this.writer.write(pi);
        this.writer.write("?>");
    }
}
