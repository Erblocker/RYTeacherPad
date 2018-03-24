package org.bitlet.weupnp;

import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class NameValueHandler extends DefaultHandler {
    private String currentElement;
    private Map<String, String> nameValue;

    public NameValueHandler(Map<String, String> nameValue) {
        this.nameValue = nameValue;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.currentElement = localName;
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        this.currentElement = null;
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.currentElement != null) {
            String value = new String(ch, start, length);
            String old = (String) this.nameValue.put(this.currentElement, value);
            if (old != null) {
                this.nameValue.put(this.currentElement, old + value);
            }
        }
    }
}
