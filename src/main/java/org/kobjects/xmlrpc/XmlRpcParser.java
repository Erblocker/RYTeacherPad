package org.kobjects.xmlrpc;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import org.kobjects.base64.Base64;
import org.kobjects.isodate.IsoDate;
import org.kobjects.xml.XmlReader;

public class XmlRpcParser {
    private XmlReader parser = null;

    public XmlRpcParser(XmlReader parser) {
        this.parser = parser;
    }

    private final Hashtable parseStruct() throws IOException {
        Hashtable result = new Hashtable();
        int type = nextTag();
        while (type != 3) {
            nextTag();
            String name = nextText();
            nextTag();
            result.put(name, parseValue());
            type = nextTag();
        }
        nextTag();
        return result;
    }

    private final Object parseValue() throws IOException {
        Object obj = null;
        int event = this.parser.next();
        if (event == 4) {
            obj = this.parser.getText();
            event = this.parser.next();
        }
        if (event == 2) {
            String name = this.parser.getName();
            if (name.equals("array")) {
                obj = parseArray();
            } else if (name.equals("struct")) {
                obj = parseStruct();
            } else {
                if (name.equals("string")) {
                    obj = nextText();
                } else if (name.equals("i4") || name.equals("int")) {
                    obj = new Integer(Integer.parseInt(nextText().trim()));
                } else if (name.equals("boolean")) {
                    obj = new Boolean(nextText().trim().equals("1"));
                } else if (name.equals("dateTime.iso8601")) {
                    obj = IsoDate.stringToDate(nextText(), 3);
                } else if (name.equals("base64")) {
                    obj = Base64.decode(nextText());
                } else if (name.equals("double")) {
                    obj = nextText();
                }
                nextTag();
            }
        }
        nextTag();
        return obj;
    }

    private final Vector parseArray() throws IOException {
        nextTag();
        int type = nextTag();
        Vector vec = new Vector();
        while (type != 3) {
            vec.addElement(parseValue());
            type = this.parser.getType();
        }
        nextTag();
        nextTag();
        return vec;
    }

    private final Object parseFault() throws IOException {
        nextTag();
        Object value = parseValue();
        nextTag();
        return value;
    }

    private final Object parseParams() throws IOException {
        Vector params = new Vector();
        int type = nextTag();
        while (type != 3) {
            nextTag();
            params.addElement(parseValue());
            type = nextTag();
        }
        nextTag();
        return params;
    }

    public final Object parseResponse() throws IOException {
        nextTag();
        if (nextTag() != 2) {
            return null;
        }
        if ("fault".equals(this.parser.getName())) {
            return parseFault();
        }
        if ("params".equals(this.parser.getName())) {
            return parseParams();
        }
        return null;
    }

    private final int nextTag() throws IOException {
        int type = this.parser.getType();
        type = this.parser.next();
        if (type == 4 && this.parser.isWhitespace()) {
            type = this.parser.next();
        }
        if (type == 3 || type == 2) {
            return type;
        }
        throw new IOException("unexpected type: " + type);
    }

    private final String nextText() throws IOException {
        if (this.parser.getType() != 2) {
            throw new IOException("precondition: START_TAG");
        }
        String result;
        int type = this.parser.next();
        if (type == 4) {
            result = this.parser.getText();
            type = this.parser.next();
        } else {
            result = "";
        }
        if (type == 3) {
            return result;
        }
        throw new IOException("END_TAG expected");
    }
}
