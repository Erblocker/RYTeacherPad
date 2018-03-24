package org.ksoap2;

import java.io.IOException;
import org.kxml2.kdom.Node;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class SoapFault extends IOException {
    private static final long serialVersionUID = 1011001;
    public Node detail;
    public String faultactor;
    public String faultcode;
    public String faultstring;
    public int version;

    public SoapFault() {
        this.version = SoapEnvelope.VER11;
    }

    public SoapFault(int version) {
        this.version = version;
    }

    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(2, SoapEnvelope.ENV, "Fault");
        while (parser.nextTag() == 2) {
            String name = parser.getName();
            if (name.equals("detail")) {
                this.detail = new Node();
                this.detail.parse(parser);
                if (parser.getNamespace().equals(SoapEnvelope.ENV) && parser.getName().equals("Fault")) {
                    break;
                }
            }
            if (name.equals("faultcode")) {
                this.faultcode = parser.nextText();
            } else if (name.equals("faultstring")) {
                this.faultstring = parser.nextText();
            } else if (name.equals("faultactor")) {
                this.faultactor = parser.nextText();
            } else {
                throw new RuntimeException(new StringBuffer().append("unexpected tag:").append(name).toString());
            }
            parser.require(3, null, name);
        }
        parser.require(3, SoapEnvelope.ENV, "Fault");
        parser.nextTag();
    }

    public void write(XmlSerializer xw) throws IOException {
        xw.startTag(SoapEnvelope.ENV, "Fault");
        xw.startTag(null, "faultcode");
        xw.text(new StringBuffer().append("").append(this.faultcode).toString());
        xw.endTag(null, "faultcode");
        xw.startTag(null, "faultstring");
        xw.text(new StringBuffer().append("").append(this.faultstring).toString());
        xw.endTag(null, "faultstring");
        xw.startTag(null, "detail");
        if (this.detail != null) {
            this.detail.write(xw);
        }
        xw.endTag(null, "detail");
        xw.endTag(SoapEnvelope.ENV, "Fault");
    }

    public String getMessage() {
        return this.faultstring;
    }

    public String toString() {
        return new StringBuffer().append("SoapFault - faultcode: '").append(this.faultcode).append("' faultstring: '").append(this.faultstring).append("' faultactor: '").append(this.faultactor).append("' detail: ").append(this.detail).toString();
    }
}
