package org.xmlpull.v1;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class XmlPullParserFactory {
    public static final String PROPERTY_NAME = "org.xmlpull.v1.XmlPullParserFactory";
    private static final String RESOURCE_NAME = "/META-INF/services/org.xmlpull.v1.XmlPullParserFactory";
    static final Class referenceContextClass = new XmlPullParserFactory().getClass();
    protected String classNamesLocation;
    protected Hashtable features = new Hashtable();
    protected Vector parserClasses;
    protected Vector serializerClasses;

    protected XmlPullParserFactory() {
    }

    public void setFeature(String name, boolean state) throws XmlPullParserException {
        this.features.put(name, new Boolean(state));
    }

    public boolean getFeature(String name) {
        Boolean value = (Boolean) this.features.get(name);
        return value != null ? value.booleanValue() : false;
    }

    public void setNamespaceAware(boolean awareness) {
        this.features.put(XmlPullParser.FEATURE_PROCESS_NAMESPACES, new Boolean(awareness));
    }

    public boolean isNamespaceAware() {
        return getFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES);
    }

    public void setValidating(boolean validating) {
        this.features.put(XmlPullParser.FEATURE_VALIDATION, new Boolean(validating));
    }

    public boolean isValidating() {
        return getFeature(XmlPullParser.FEATURE_VALIDATION);
    }

    public XmlPullParser newPullParser() throws XmlPullParserException {
        if (this.parserClasses == null) {
            throw new XmlPullParserException("Factory initialization was incomplete - has not tried " + this.classNamesLocation);
        } else if (this.parserClasses.size() == 0) {
            throw new XmlPullParserException("No valid parser classes found in " + this.classNamesLocation);
        } else {
            StringBuffer issues = new StringBuffer();
            int i = 0;
            while (i < this.parserClasses.size()) {
                Class ppClass = (Class) this.parserClasses.elementAt(i);
                try {
                    XmlPullParser pp = (XmlPullParser) ppClass.newInstance();
                    Enumeration e = this.features.keys();
                    while (e.hasMoreElements()) {
                        String key = (String) e.nextElement();
                        Boolean value = (Boolean) this.features.get(key);
                        if (value != null && value.booleanValue()) {
                            pp.setFeature(key, true);
                        }
                    }
                    return pp;
                } catch (Exception ex) {
                    issues.append(ppClass.getName() + ": " + ex.toString() + "; ");
                    i++;
                }
            }
            throw new XmlPullParserException("could not create parser: " + issues);
        }
    }

    public XmlSerializer newSerializer() throws XmlPullParserException {
        if (this.serializerClasses == null) {
            throw new XmlPullParserException("Factory initialization incomplete - has not tried " + this.classNamesLocation);
        } else if (this.serializerClasses.size() == 0) {
            throw new XmlPullParserException("No valid serializer classes found in " + this.classNamesLocation);
        } else {
            StringBuffer issues = new StringBuffer();
            int i = 0;
            while (i < this.serializerClasses.size()) {
                Class ppClass = (Class) this.serializerClasses.elementAt(i);
                try {
                    return (XmlSerializer) ppClass.newInstance();
                } catch (Exception ex) {
                    issues.append(ppClass.getName() + ": " + ex.toString() + "; ");
                    i++;
                }
            }
            throw new XmlPullParserException("could not create serializer: " + issues);
        }
    }

    public static XmlPullParserFactory newInstance() throws XmlPullParserException {
        return newInstance(null, null);
    }

    public static XmlPullParserFactory newInstance(String classNames, Class context) throws XmlPullParserException {
        String classNamesLocation;
        if (context == null) {
            context = referenceContextClass;
        }
        if (classNames == null || classNames.length() == 0 || "DEFAULT".equals(classNames)) {
            try {
                InputStream is = context.getResourceAsStream(RESOURCE_NAME);
                if (is == null) {
                    throw new XmlPullParserException("resource not found: /META-INF/services/org.xmlpull.v1.XmlPullParserFactory make sure that parser implementing XmlPull API is available");
                }
                StringBuffer sb = new StringBuffer();
                while (true) {
                    int ch = is.read();
                    if (ch < 0) {
                        break;
                    } else if (ch > 32) {
                        sb.append((char) ch);
                    }
                }
                is.close();
                classNames = sb.toString();
                classNamesLocation = "resource /META-INF/services/org.xmlpull.v1.XmlPullParserFactory that contained '" + classNames + "'";
            } catch (Exception e) {
                throw new XmlPullParserException(null, null, e);
            }
        }
        classNamesLocation = "parameter classNames to newInstance() that contained '" + classNames + "'";
        XmlPullParserFactory factory = null;
        Vector parserClasses = new Vector();
        Vector serializerClasses = new Vector();
        int pos = 0;
        while (pos < classNames.length()) {
            int cut = classNames.indexOf(44, pos);
            if (cut == -1) {
                cut = classNames.length();
            }
            String name = classNames.substring(pos, cut);
            Class cls = null;
            XmlPullParserFactory instance = null;
            try {
                cls = Class.forName(name);
                instance = cls.newInstance();
            } catch (Exception e2) {
            }
            if (cls != null) {
                boolean recognized = false;
                if (instance instanceof XmlPullParser) {
                    parserClasses.addElement(cls);
                    recognized = true;
                }
                if (instance instanceof XmlSerializer) {
                    serializerClasses.addElement(cls);
                    recognized = true;
                }
                if (instance instanceof XmlPullParserFactory) {
                    if (factory == null) {
                        factory = instance;
                    }
                    recognized = true;
                }
                if (!recognized) {
                    throw new XmlPullParserException("incompatible class: " + name);
                }
            }
            pos = cut + 1;
        }
        if (factory == null) {
            factory = new XmlPullParserFactory();
        }
        factory.parserClasses = parserClasses;
        factory.serializerClasses = serializerClasses;
        factory.classNamesLocation = classNamesLocation;
        return factory;
    }
}
