package org.ksoap2.serialization;

import java.io.IOException;
import java.math.BigDecimal;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class MarshalFloat implements Marshal {
    static Class class$java$lang$Double;
    static Class class$java$lang$Float;
    static Class class$java$math$BigDecimal;

    public Object readInstance(XmlPullParser parser, String namespace, String name, PropertyInfo propertyInfo) throws IOException, XmlPullParserException {
        String stringValue = parser.nextText();
        if (name.equals("float")) {
            return new Float(stringValue);
        }
        if (name.equals("double")) {
            return new Double(stringValue);
        }
        if (name.equals("decimal")) {
            return new BigDecimal(stringValue);
        }
        throw new RuntimeException("float, double, or decimal expected");
    }

    public void writeInstance(XmlSerializer writer, Object instance) throws IOException {
        writer.text(instance.toString());
    }

    static Class class$(String x0) {
        try {
            return Class.forName(x0);
        } catch (ClassNotFoundException x1) {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }

    public void register(SoapSerializationEnvelope cm) {
        Class class$;
        String str = cm.xsd;
        String str2 = "float";
        if (class$java$lang$Float == null) {
            class$ = class$("java.lang.Float");
            class$java$lang$Float = class$;
        } else {
            class$ = class$java$lang$Float;
        }
        cm.addMapping(str, str2, class$, this);
        str = cm.xsd;
        str2 = "double";
        if (class$java$lang$Double == null) {
            class$ = class$("java.lang.Double");
            class$java$lang$Double = class$;
        } else {
            class$ = class$java$lang$Double;
        }
        cm.addMapping(str, str2, class$, this);
        str = cm.xsd;
        str2 = "decimal";
        if (class$java$math$BigDecimal == null) {
            class$ = class$("java.math.BigDecimal");
            class$java$math$BigDecimal = class$;
        } else {
            class$ = class$java$math$BigDecimal;
        }
        cm.addMapping(str, str2, class$, this);
    }
}
