package org.ksoap2.serialization;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.SoapFault12;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class SoapSerializationEnvelope extends SoapEnvelope {
    private static final String ANY_TYPE_LABEL = "anyType";
    private static final String ARRAY_MAPPING_NAME = "Array";
    private static final String ARRAY_TYPE_LABEL = "arrayType";
    static final Marshal DEFAULT_MARSHAL = new DM();
    private static final String HREF_LABEL = "href";
    private static final String ID_LABEL = "id";
    private static final String ITEM_LABEL = "item";
    private static final String NIL_LABEL = "nil";
    private static final String NULL_LABEL = "null";
    protected static final int QNAME_MARSHAL = 3;
    protected static final int QNAME_NAMESPACE = 0;
    protected static final int QNAME_TYPE = 1;
    private static final String ROOT_LABEL = "root";
    private static final String TYPE_LABEL = "type";
    static Class class$org$ksoap2$serialization$SoapObject;
    protected boolean addAdornments = true;
    public boolean avoidExceptionForUnknownProperty;
    protected Hashtable classToQName = new Hashtable();
    public boolean dotNet;
    Hashtable idMap = new Hashtable();
    public boolean implicitTypes;
    Vector multiRef;
    public Hashtable properties = new Hashtable();
    protected Hashtable qNameToClass = new Hashtable();

    public SoapSerializationEnvelope(int version) {
        super(version);
        addMapping(this.enc, ARRAY_MAPPING_NAME, PropertyInfo.VECTOR_CLASS);
        DEFAULT_MARSHAL.register(this);
    }

    public boolean isAddAdornments() {
        return this.addAdornments;
    }

    public void setAddAdornments(boolean addAdornments) {
        this.addAdornments = addAdornments;
    }

    public void setBodyOutEmpty(boolean emptyBody) {
        if (emptyBody) {
            this.bodyOut = null;
        }
    }

    public void parseBody(XmlPullParser parser) throws IOException, XmlPullParserException {
        this.bodyIn = null;
        parser.nextTag();
        if (parser.getEventType() == 2 && parser.getNamespace().equals(this.env) && parser.getName().equals("Fault")) {
            SoapFault fault;
            if (this.version < SoapEnvelope.VER12) {
                fault = new SoapFault(this.version);
            } else {
                fault = new SoapFault12(this.version);
            }
            fault.parse(parser);
            this.bodyIn = fault;
            return;
        }
        while (parser.getEventType() == 2) {
            String rootAttr = parser.getAttributeValue(this.enc, ROOT_LABEL);
            Object o = read(parser, null, -1, parser.getNamespace(), parser.getName(), PropertyInfo.OBJECT_TYPE);
            if ("1".equals(rootAttr) || this.bodyIn == null) {
                this.bodyIn = o;
            }
            parser.nextTag();
        }
    }

    protected void readSerializable(XmlPullParser parser, SoapObject obj) throws IOException, XmlPullParserException {
        for (int counter = 0; counter < parser.getAttributeCount(); counter++) {
            obj.addAttribute(parser.getAttributeName(counter), parser.getAttributeValue(counter));
        }
        readSerializable(parser, (KvmSerializable) obj);
    }

    protected void readSerializable(XmlPullParser parser, KvmSerializable obj) throws IOException, XmlPullParserException {
        while (parser.nextTag() != 3) {
            String name = parser.getName();
            if (this.implicitTypes && (obj instanceof SoapObject)) {
                ((SoapObject) obj).addProperty(parser.getName(), read(parser, obj, obj.getPropertyCount(), ((SoapObject) obj).getNamespace(), name, PropertyInfo.OBJECT_TYPE));
            } else {
                PropertyInfo info = new PropertyInfo();
                int propertyCount = obj.getPropertyCount();
                boolean propertyFound = false;
                for (int i = 0; i < propertyCount && !propertyFound; i++) {
                    info.clear();
                    obj.getPropertyInfo(i, this.properties, info);
                    if ((name.equals(info.name) && info.namespace == null) || (name.equals(info.name) && parser.getNamespace().equals(info.namespace))) {
                        propertyFound = true;
                        obj.setProperty(i, read(parser, obj, i, null, null, info));
                    }
                }
                if (propertyFound) {
                    continue;
                } else if (this.avoidExceptionForUnknownProperty) {
                    while (true) {
                        if (parser.next() == 3) {
                            if (name.equals(parser.getName())) {
                                break;
                            }
                        }
                    }
                } else {
                    throw new RuntimeException(new StringBuffer().append("Unknown Property: ").append(name).toString());
                }
            }
        }
        parser.require(3, null, null);
    }

    protected Object readUnknown(XmlPullParser parser, String typeNamespace, String typeName) throws IOException, XmlPullParserException {
        int i;
        SoapObject so;
        String name = parser.getName();
        String namespace = parser.getNamespace();
        Vector attributeInfoVector = new Vector();
        for (int attributeCount = 0; attributeCount < parser.getAttributeCount(); attributeCount++) {
            AttributeInfo attributeInfo = new AttributeInfo();
            attributeInfo.setName(parser.getAttributeName(attributeCount));
            attributeInfo.setValue(parser.getAttributeValue(attributeCount));
            attributeInfo.setNamespace(parser.getAttributeNamespace(attributeCount));
            attributeInfo.setType(parser.getAttributeType(attributeCount));
            attributeInfoVector.addElement(attributeInfo);
        }
        parser.next();
        Object obj = null;
        String text = null;
        if (parser.getEventType() == 4) {
            text = parser.getText();
            SoapPrimitive soapPrimitive = new SoapPrimitive(typeNamespace, typeName, text);
            obj = soapPrimitive;
            for (i = 0; i < attributeInfoVector.size(); i++) {
                soapPrimitive.addAttribute((AttributeInfo) attributeInfoVector.elementAt(i));
            }
            parser.next();
        } else if (parser.getEventType() == 3) {
            so = new SoapObject(typeNamespace, typeName);
            for (i = 0; i < attributeInfoVector.size(); i++) {
                so.addAttribute((AttributeInfo) attributeInfoVector.elementAt(i));
            }
            SoapObject result = so;
        }
        if (parser.getEventType() == 2) {
            if (text == null || text.trim().length() == 0) {
                so = new SoapObject(typeNamespace, typeName);
                for (i = 0; i < attributeInfoVector.size(); i++) {
                    so.addAttribute((AttributeInfo) attributeInfoVector.elementAt(i));
                }
                while (parser.getEventType() != 3) {
                    String name2 = parser.getName();
                    so.addProperty(name2, read(parser, so, so.getPropertyCount(), null, null, PropertyInfo.OBJECT_TYPE));
                    parser.nextTag();
                }
                obj = so;
            } else {
                throw new RuntimeException("Malformed input: Mixed content");
            }
        }
        parser.require(3, namespace, name);
        return obj;
    }

    private int getIndex(String value, int start, int dflt) {
        return (value != null && value.length() - start >= 3) ? Integer.parseInt(value.substring(start + 1, value.length() - 1)) : dflt;
    }

    protected void readVector(XmlPullParser parser, Vector v, PropertyInfo elementType) throws IOException, XmlPullParserException {
        String namespace = null;
        String name = null;
        int size = v.size();
        boolean dynamic = true;
        String type = parser.getAttributeValue(this.enc, ARRAY_TYPE_LABEL);
        if (type != null) {
            String prefix;
            int cut0 = type.indexOf(58);
            int cut1 = type.indexOf("[", cut0);
            name = type.substring(cut0 + 1, cut1);
            if (cut0 == -1) {
                prefix = "";
            } else {
                prefix = type.substring(0, cut0);
            }
            namespace = parser.getNamespace(prefix);
            size = getIndex(type, cut1, -1);
            if (size != -1) {
                v.setSize(size);
                dynamic = false;
            }
        }
        if (elementType == null) {
            elementType = PropertyInfo.OBJECT_TYPE;
        }
        parser.nextTag();
        int position = getIndex(parser.getAttributeValue(this.enc, "offset"), 0, 0);
        while (parser.getEventType() != 3) {
            position = getIndex(parser.getAttributeValue(this.enc, "position"), 0, position);
            if (dynamic && position >= size) {
                size = position + 1;
                v.setSize(size);
            }
            v.setElementAt(read(parser, v, position, namespace, name, elementType), position);
            position++;
            parser.nextTag();
        }
        parser.require(3, null, null);
    }

    public Object read(XmlPullParser parser, Object owner, int index, String namespace, String name, PropertyInfo expected) throws IOException, XmlPullParserException {
        Object obj;
        String elementName = parser.getName();
        String href = parser.getAttributeValue(null, HREF_LABEL);
        FwdRef f;
        if (href == null) {
            String nullAttr = parser.getAttributeValue(this.xsi, NIL_LABEL);
            String id = parser.getAttributeValue(null, ID_LABEL);
            if (nullAttr == null) {
                nullAttr = parser.getAttributeValue(this.xsi, NULL_LABEL);
            }
            if (nullAttr == null || !SoapEnvelope.stringToBoolean(nullAttr)) {
                String type = parser.getAttributeValue(this.xsi, TYPE_LABEL);
                if (type != null) {
                    int cut = type.indexOf(58);
                    name = type.substring(cut + 1);
                    namespace = parser.getNamespace(cut == -1 ? "" : type.substring(0, cut));
                } else if (name == null && namespace == null) {
                    if (parser.getAttributeValue(this.enc, ARRAY_TYPE_LABEL) != null) {
                        namespace = this.enc;
                        name = ARRAY_MAPPING_NAME;
                    } else {
                        Object[] names = getInfo(expected.type, null);
                        namespace = names[0];
                        name = names[1];
                    }
                }
                if (type == null) {
                    this.implicitTypes = true;
                }
                obj = readInstance(parser, namespace, name, expected);
                if (obj == null) {
                    obj = readUnknown(parser, namespace, name);
                }
            } else {
                obj = null;
                parser.nextTag();
                parser.require(3, null, elementName);
            }
            if (id != null) {
                Object hlp = this.idMap.get(id);
                if (hlp instanceof FwdRef) {
                    f = (FwdRef) hlp;
                    do {
                        if (f.obj instanceof KvmSerializable) {
                            ((KvmSerializable) f.obj).setProperty(f.index, obj);
                        } else {
                            ((Vector) f.obj).setElementAt(obj, f.index);
                        }
                        f = f.next;
                    } while (f != null);
                } else if (hlp != null) {
                    throw new RuntimeException("double ID");
                }
                this.idMap.put(id, obj);
            }
        } else if (owner == null) {
            throw new RuntimeException("href at root level?!?");
        } else {
            href = href.substring(1);
            obj = this.idMap.get(href);
            if (obj == null || (obj instanceof FwdRef)) {
                f = new FwdRef();
                f.next = (FwdRef) obj;
                f.obj = owner;
                f.index = index;
                this.idMap.put(href, f);
                obj = null;
            }
            parser.nextTag();
            parser.require(3, null, elementName);
        }
        parser.require(3, null, elementName);
        return obj;
    }

    public Object readInstance(XmlPullParser parser, String namespace, String name, PropertyInfo expected) throws IOException, XmlPullParserException {
        Class obj = this.qNameToClass.get(new SoapPrimitive(namespace, name, null));
        if (obj == null) {
            return null;
        }
        if (obj instanceof Marshal) {
            return ((Marshal) obj).readInstance(parser, namespace, name, expected);
        }
        Object obj2;
        if (obj instanceof SoapObject) {
            obj2 = ((SoapObject) obj).newInstance();
        } else {
            Class class$;
            if (class$org$ksoap2$serialization$SoapObject == null) {
                class$ = class$("org.ksoap2.serialization.SoapObject");
                class$org$ksoap2$serialization$SoapObject = class$;
            } else {
                class$ = class$org$ksoap2$serialization$SoapObject;
            }
            if (obj == class$) {
                obj2 = new SoapObject(namespace, name);
            } else {
                try {
                    obj2 = obj.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e.toString());
                }
            }
        }
        if (obj2 instanceof SoapObject) {
            readSerializable(parser, (SoapObject) obj2);
            return obj2;
        } else if (obj2 instanceof KvmSerializable) {
            readSerializable(parser, (KvmSerializable) obj2);
            return obj2;
        } else if (obj2 instanceof Vector) {
            readVector(parser, (Vector) obj2, expected.elementType);
            return obj2;
        } else {
            throw new RuntimeException(new StringBuffer().append("no deserializer for ").append(obj2.getClass()).toString());
        }
    }

    static Class class$(String x0) {
        try {
            return Class.forName(x0);
        } catch (ClassNotFoundException x1) {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }

    public Object[] getInfo(Object type, Object instance) {
        AttributeContainer type2;
        if (type == null) {
            if ((instance instanceof SoapObject) || (instance instanceof SoapPrimitive)) {
                type2 = instance;
            } else {
                type2 = instance.getClass();
            }
        }
        if (type2 instanceof SoapObject) {
            SoapObject so = (SoapObject) type2;
            return new Object[]{so.getNamespace(), so.getName(), null, null};
        } else if (type2 instanceof SoapPrimitive) {
            SoapPrimitive sp = (SoapPrimitive) type2;
            return new Object[]{sp.getNamespace(), sp.getName(), null, DEFAULT_MARSHAL};
        } else {
            if ((type2 instanceof Class) && type2 != PropertyInfo.OBJECT_CLASS) {
                Object[] tmp = (Object[]) this.classToQName.get(((Class) type2).getName());
                if (tmp != null) {
                    return tmp;
                }
            }
            return new Object[]{this.xsd, ANY_TYPE_LABEL, null, null};
        }
    }

    public void addMapping(String namespace, String name, Class clazz, Marshal marshal) {
        Object obj;
        Hashtable hashtable = this.qNameToClass;
        SoapPrimitive soapPrimitive = new SoapPrimitive(namespace, name, null);
        if (marshal == null) {
            obj = clazz;
        } else {
            Marshal marshal2 = marshal;
        }
        hashtable.put(soapPrimitive, obj);
        this.classToQName.put(clazz.getName(), new Object[]{namespace, name, null, marshal});
    }

    public void addMapping(String namespace, String name, Class clazz) {
        addMapping(namespace, name, clazz, null);
    }

    public void addTemplate(SoapObject so) {
        this.qNameToClass.put(new SoapPrimitive(so.namespace, so.name, null), so);
    }

    public Object getResponse() throws SoapFault {
        if (this.bodyIn instanceof SoapFault) {
            throw ((SoapFault) this.bodyIn);
        }
        KvmSerializable ks = this.bodyIn;
        if (ks.getPropertyCount() == 0) {
            return null;
        }
        if (ks.getPropertyCount() == 1) {
            return ks.getProperty(0);
        }
        Object ret = new Vector();
        for (int i = 0; i < ks.getPropertyCount(); i++) {
            ret.add(ks.getProperty(i));
        }
        return ret;
    }

    public void writeBody(XmlSerializer writer) throws IOException {
        if (this.bodyOut != null) {
            this.multiRef = new Vector();
            this.multiRef.addElement(this.bodyOut);
            Object[] qName = getInfo(null, this.bodyOut);
            writer.startTag(this.dotNet ? "" : (String) qName[0], (String) qName[1]);
            if (this.dotNet) {
                writer.attribute(null, "xmlns", (String) qName[0]);
            }
            if (this.addAdornments) {
                writer.attribute(null, ID_LABEL, qName[2] == null ? "o0" : (String) qName[2]);
                writer.attribute(this.enc, ROOT_LABEL, "1");
            }
            writeElement(writer, this.bodyOut, null, qName[3]);
            writer.endTag(this.dotNet ? "" : (String) qName[0], (String) qName[1]);
        }
    }

    public void writeObjectBody(XmlSerializer writer, SoapObject obj) throws IOException {
        SoapObject soapObject = obj;
        int cnt = soapObject.getAttributeCount();
        for (int counter = 0; counter < cnt; counter++) {
            AttributeInfo attributeInfo = new AttributeInfo();
            soapObject.getAttributeInfo(counter, attributeInfo);
            writer.attribute(attributeInfo.getNamespace(), attributeInfo.getName(), attributeInfo.getValue().toString());
        }
        writeObjectBody(writer, (KvmSerializable) obj);
    }

    public void writeObjectBody(XmlSerializer writer, KvmSerializable obj) throws IOException {
        int cnt = obj.getPropertyCount();
        PropertyInfo propertyInfo = new PropertyInfo();
        for (int i = 0; i < cnt; i++) {
            SoapObject prop = obj.getProperty(i);
            obj.getPropertyInfo(i, this.properties, propertyInfo);
            if (prop instanceof SoapObject) {
                String name;
                SoapObject nestedSoap = prop;
                Object[] qName = getInfo(null, nestedSoap);
                String namespace = qName[0];
                String type = qName[1];
                if (propertyInfo.name == null || propertyInfo.name.length() <= 0) {
                    name = (String) qName[1];
                } else {
                    name = propertyInfo.name;
                }
                if (propertyInfo.namespace == null || propertyInfo.namespace.length() <= 0) {
                    namespace = qName[0];
                } else {
                    namespace = propertyInfo.namespace;
                }
                writer.startTag(namespace, name);
                if (!this.implicitTypes) {
                    XmlSerializer xmlSerializer = writer;
                    xmlSerializer.attribute(this.xsi, TYPE_LABEL, new StringBuffer().append(writer.getPrefix(namespace, true)).append(":").append(type).toString());
                }
                writeObjectBody(writer, nestedSoap);
                writer.endTag(namespace, name);
            } else if ((propertyInfo.flags & 1) == 0) {
                writer.startTag(propertyInfo.namespace, propertyInfo.name);
                writeProperty(writer, obj.getProperty(i), propertyInfo);
                writer.endTag(propertyInfo.namespace, propertyInfo.name);
            }
        }
    }

    protected void writeProperty(XmlSerializer writer, Object obj, PropertyInfo type) throws IOException {
        if (obj == null) {
            String str;
            String str2 = this.xsi;
            if (this.version >= SoapEnvelope.VER12) {
                str = NIL_LABEL;
            } else {
                str = NULL_LABEL;
            }
            writer.attribute(str2, str, "true");
            return;
        }
        Object[] qName = getInfo(null, obj);
        if (type.multiRef || qName[2] != null) {
            int i = this.multiRef.indexOf(obj);
            if (i == -1) {
                i = this.multiRef.size();
                this.multiRef.addElement(obj);
            }
            writer.attribute(null, HREF_LABEL, qName[2] == null ? new StringBuffer().append("#o").append(i).toString() : new StringBuffer().append("#").append(qName[2]).toString());
            return;
        }
        if (!(this.implicitTypes && obj.getClass() == type.type)) {
            writer.attribute(this.xsi, TYPE_LABEL, new StringBuffer().append(writer.getPrefix((String) qName[0], true)).append(":").append(qName[1]).toString());
        }
        writeElement(writer, obj, type, qName[3]);
    }

    private void writeElement(XmlSerializer writer, Object element, PropertyInfo type, Object marshal) throws IOException {
        if (marshal != null) {
            ((Marshal) marshal).writeInstance(writer, element);
        } else if (element instanceof SoapObject) {
            writeObjectBody(writer, (SoapObject) element);
        } else if (element instanceof KvmSerializable) {
            writeObjectBody(writer, (KvmSerializable) element);
        } else if (element instanceof Vector) {
            writeVectorBody(writer, (Vector) element, type.elementType);
        } else {
            throw new RuntimeException(new StringBuffer().append("Cannot serialize: ").append(element).toString());
        }
    }

    protected void writeVectorBody(XmlSerializer writer, Vector vector, PropertyInfo elementType) throws IOException {
        String itemsTagName = ITEM_LABEL;
        String itemsNamespace = null;
        if (elementType == null) {
            elementType = PropertyInfo.OBJECT_TYPE;
        } else if ((elementType instanceof PropertyInfo) && elementType.name != null) {
            itemsTagName = elementType.name;
            itemsNamespace = elementType.namespace;
        }
        int cnt = vector.size();
        Object[] arrType = getInfo(elementType.type, null);
        if (!this.implicitTypes) {
            writer.attribute(this.enc, ARRAY_TYPE_LABEL, new StringBuffer().append(writer.getPrefix((String) arrType[0], false)).append(":").append(arrType[1]).append("[").append(cnt).append("]").toString());
        } else if (itemsNamespace == null) {
            itemsNamespace = arrType[0];
        }
        boolean skipped = false;
        for (int i = 0; i < cnt; i++) {
            if (vector.elementAt(i) == null) {
                skipped = true;
            } else {
                writer.startTag(itemsNamespace, itemsTagName);
                if (skipped) {
                    writer.attribute(this.enc, "position", new StringBuffer().append("[").append(i).append("]").toString());
                    skipped = false;
                }
                writeProperty(writer, vector.elementAt(i), elementType);
                writer.endTag(itemsNamespace, itemsTagName);
            }
        }
    }
}
