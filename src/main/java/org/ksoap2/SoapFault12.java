package org.ksoap2;

import java.io.IOException;
import org.kxml2.kdom.Node;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class SoapFault12 extends SoapFault {
    private static final long serialVersionUID = 1012001;
    public Node Code;
    public Node Detail;
    public Node Node;
    public Node Reason;
    public Node Role;

    public SoapFault12() {
        this.version = SoapEnvelope.VER12;
    }

    public SoapFault12(int version) {
        this.version = version;
    }

    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        parseSelf(parser);
        this.faultcode = this.Code.getElement(SoapEnvelope.ENV2003, "Value").getText(0);
        this.faultstring = this.Reason.getElement(SoapEnvelope.ENV2003, "Text").getText(0);
        this.detail = this.Detail;
        this.faultactor = null;
    }

    private void parseSelf(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(2, SoapEnvelope.ENV2003, "Fault");
        while (parser.nextTag() == 2) {
            String name = parser.getName();
            parser.nextTag();
            if (name.equals("Code")) {
                this.Code = new Node();
                this.Code.parse(parser);
            } else if (name.equals("Reason")) {
                this.Reason = new Node();
                this.Reason.parse(parser);
            } else if (name.equals("Node")) {
                this.Node = new Node();
                this.Node.parse(parser);
            } else if (name.equals("Role")) {
                this.Role = new Node();
                this.Role.parse(parser);
            } else if (name.equals("Detail")) {
                this.Detail = new Node();
                this.Detail.parse(parser);
            } else {
                throw new RuntimeException(new StringBuffer().append("unexpected tag:").append(name).toString());
            }
            parser.require(3, SoapEnvelope.ENV2003, name);
        }
        parser.require(3, SoapEnvelope.ENV2003, "Fault");
        parser.nextTag();
    }

    public void write(XmlSerializer xw) throws IOException {
        xw.startTag(SoapEnvelope.ENV2003, "Fault");
        xw.startTag(SoapEnvelope.ENV2003, "Code");
        this.Code.write(xw);
        xw.endTag(SoapEnvelope.ENV2003, "Code");
        xw.startTag(SoapEnvelope.ENV2003, "Reason");
        this.Reason.write(xw);
        xw.endTag(SoapEnvelope.ENV2003, "Reason");
        if (this.Node != null) {
            xw.startTag(SoapEnvelope.ENV2003, "Node");
            this.Node.write(xw);
            xw.endTag(SoapEnvelope.ENV2003, "Node");
        }
        if (this.Role != null) {
            xw.startTag(SoapEnvelope.ENV2003, "Role");
            this.Role.write(xw);
            xw.endTag(SoapEnvelope.ENV2003, "Role");
        }
        if (this.Detail != null) {
            xw.startTag(SoapEnvelope.ENV2003, "Detail");
            this.Detail.write(xw);
            xw.endTag(SoapEnvelope.ENV2003, "Detail");
        }
        xw.endTag(SoapEnvelope.ENV2003, "Fault");
    }

    public String getMessage() {
        return this.Reason.getElement(SoapEnvelope.ENV2003, "Text").getText(0);
    }

    public String toString() {
        return new StringBuffer().append("Code: ").append(this.Code.getElement(SoapEnvelope.ENV2003, "Value").getText(0)).append(", Reason: ").append(this.Reason.getElement(SoapEnvelope.ENV2003, "Text").getText(0)).toString();
    }
}
