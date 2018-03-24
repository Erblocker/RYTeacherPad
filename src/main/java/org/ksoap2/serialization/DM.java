package org.ksoap2.serialization;

import java.io.IOException;
import org.ksoap2.SoapEnvelope;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

class DM implements Marshal {
    DM() {
    }

    public Object readInstance(XmlPullParser parser, String namespace, String name, PropertyInfo expected) throws IOException, XmlPullParserException {
        String text = parser.nextText();
        switch (name.charAt(0)) {
            case 'b':
                return new Boolean(SoapEnvelope.stringToBoolean(text));
            case 'i':
                return new Integer(Integer.parseInt(text));
            case 'l':
                return new Long(Long.parseLong(text));
            case 's':
                return text;
            default:
                throw new RuntimeException();
        }
    }

    public void writeInstance(XmlSerializer writer, Object instance) throws IOException {
        if (instance instanceof AttributeContainer) {
            AttributeContainer attributeContainer = (AttributeContainer) instance;
            int cnt = attributeContainer.getAttributeCount();
            for (int counter = 0; counter < cnt; counter++) {
                AttributeInfo attributeInfo = new AttributeInfo();
                attributeContainer.getAttributeInfo(counter, attributeInfo);
                writer.attribute(attributeInfo.getNamespace(), attributeInfo.getName(), attributeInfo.getValue().toString());
            }
        }
        writer.text(instance.toString());
    }

    public void register(SoapSerializationEnvelope cm) {
        cm.addMapping(cm.xsd, "int", PropertyInfo.INTEGER_CLASS, this);
        cm.addMapping(cm.xsd, "long", PropertyInfo.LONG_CLASS, this);
        cm.addMapping(cm.xsd, "string", PropertyInfo.STRING_CLASS, this);
        cm.addMapping(cm.xsd, "boolean", PropertyInfo.BOOLEAN_CLASS, this);
    }
}
