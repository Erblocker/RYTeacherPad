package org.kxml2.kdom;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class Document extends Node {
    String encoding;
    protected int rootIndex = -1;
    Boolean standalone;

    public String getEncoding() {
        return this.encoding;
    }

    public void setEncoding(String enc) {
        this.encoding = enc;
    }

    public void setStandalone(Boolean standalone) {
        this.standalone = standalone;
    }

    public Boolean getStandalone() {
        return this.standalone;
    }

    public String getName() {
        return "#document";
    }

    public void addChild(int index, int type, Object child) {
        if (type == 2) {
            this.rootIndex = index;
        } else if (this.rootIndex >= index) {
            this.rootIndex++;
        }
        super.addChild(index, type, child);
    }

    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(0, null, null);
        parser.nextToken();
        this.encoding = parser.getInputEncoding();
        this.standalone = (Boolean) parser.getProperty("http://xmlpull.org/v1/doc/properties.html#xmldecl-standalone");
        super.parse(parser);
        if (parser.getEventType() != 1) {
            throw new RuntimeException("Document end expected!");
        }
    }

    public void removeChild(int index) {
        if (index == this.rootIndex) {
            this.rootIndex = -1;
        } else if (index < this.rootIndex) {
            this.rootIndex--;
        }
        super.removeChild(index);
    }

    public Element getRootElement() {
        if (this.rootIndex != -1) {
            return (Element) getChild(this.rootIndex);
        }
        throw new RuntimeException("Document has no root element!");
    }

    public void write(XmlSerializer writer) throws IOException {
        writer.startDocument(this.encoding, this.standalone);
        writeChildren(writer);
        writer.endDocument();
    }
}
