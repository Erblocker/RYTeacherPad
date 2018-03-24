package org.ksoap2.serialization;

import java.util.Hashtable;
import java.util.Vector;

public class SoapObject extends AttributeContainer implements KvmSerializable {
    private static final String EMPTY_STRING = "";
    static Class class$java$lang$String;
    static Class class$org$ksoap2$serialization$SoapObject;
    protected String name;
    protected String namespace;
    protected Vector properties;

    public SoapObject() {
        this("", "");
    }

    public SoapObject(String namespace, String name) {
        this.properties = new Vector();
        this.namespace = namespace;
        this.name = name;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof SoapObject)) {
            return false;
        }
        SoapObject otherSoapObject = (SoapObject) obj;
        if (!this.name.equals(otherSoapObject.name) || !this.namespace.equals(otherSoapObject.namespace)) {
            return false;
        }
        int numProperties = this.properties.size();
        if (numProperties != otherSoapObject.properties.size()) {
            return false;
        }
        for (int propIndex = 0; propIndex < numProperties; propIndex++) {
            if (!otherSoapObject.isPropertyEqual(this.properties.elementAt(propIndex), propIndex)) {
                return false;
            }
        }
        return attributesAreEqual(otherSoapObject);
    }

    public boolean isPropertyEqual(Object otherProp, int index) {
        if (index >= getPropertyCount()) {
            return false;
        }
        PropertyInfo thisProp = this.properties.elementAt(index);
        if ((otherProp instanceof PropertyInfo) && (thisProp instanceof PropertyInfo)) {
            PropertyInfo otherPropInfo = (PropertyInfo) otherProp;
            PropertyInfo thisPropInfo = thisProp;
            if (otherPropInfo.getName().equals(thisPropInfo.getName()) && otherPropInfo.getValue().equals(thisPropInfo.getValue())) {
                return true;
            }
            return false;
        } else if ((otherProp instanceof SoapObject) && (thisProp instanceof SoapObject)) {
            return ((SoapObject) otherProp).equals((SoapObject) thisProp);
        } else {
            return false;
        }
    }

    public String getName() {
        return this.name;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public Object getProperty(int index) {
        Object prop = this.properties.elementAt(index);
        if (prop instanceof PropertyInfo) {
            return ((PropertyInfo) prop).getValue();
        }
        return (SoapObject) prop;
    }

    public String getPropertyAsString(int index) {
        return ((PropertyInfo) this.properties.elementAt(index)).getValue().toString();
    }

    public Object getProperty(String name) {
        Integer index = propertyIndex(name);
        if (index != null) {
            return getProperty(index.intValue());
        }
        throw new RuntimeException(new StringBuffer().append("illegal property: ").append(name).toString());
    }

    public String getPropertyAsString(String name) {
        Integer index = propertyIndex(name);
        if (index != null) {
            return getProperty(index.intValue()).toString();
        }
        throw new RuntimeException(new StringBuffer().append("illegal property: ").append(name).toString());
    }

    public boolean hasProperty(String name) {
        if (propertyIndex(name) != null) {
            return true;
        }
        return false;
    }

    public Object getPropertySafely(String name) {
        Integer i = propertyIndex(name);
        if (i != null) {
            return getProperty(i.intValue());
        }
        return new NullSoapObject();
    }

    public String getPropertySafelyAsString(String name) {
        Integer i = propertyIndex(name);
        if (i == null) {
            return "";
        }
        Object foo = getProperty(i.intValue());
        if (foo == null) {
            return "";
        }
        return foo.toString();
    }

    public Object getPropertySafely(String name, Object defaultThing) {
        Integer i = propertyIndex(name);
        if (i != null) {
            return getProperty(i.intValue());
        }
        return defaultThing;
    }

    public String getPropertySafelyAsString(String name, Object defaultThing) {
        Integer i = propertyIndex(name);
        if (i != null) {
            Object property = getProperty(i.intValue());
            if (property != null) {
                return property.toString();
            }
            return "";
        } else if (defaultThing != null) {
            return defaultThing.toString();
        } else {
            return "";
        }
    }

    public Object getPrimitiveProperty(String name) {
        Integer index = propertyIndex(name);
        if (index != null) {
            Class class$;
            PropertyInfo propertyInfo = (PropertyInfo) this.properties.elementAt(index.intValue());
            Class type = propertyInfo.getType();
            if (class$org$ksoap2$serialization$SoapObject == null) {
                class$ = class$("org.ksoap2.serialization.SoapObject");
                class$org$ksoap2$serialization$SoapObject = class$;
            } else {
                class$ = class$org$ksoap2$serialization$SoapObject;
            }
            if (type != class$) {
                return propertyInfo.getValue();
            }
            Object class$2;
            propertyInfo = new PropertyInfo();
            if (class$java$lang$String == null) {
                class$2 = class$("java.lang.String");
                class$java$lang$String = class$2;
            } else {
                class$2 = class$java$lang$String;
            }
            propertyInfo.setType(class$2);
            propertyInfo.setValue("");
            propertyInfo.setName(name);
            return propertyInfo.getValue();
        }
        throw new RuntimeException(new StringBuffer().append("illegal property: ").append(name).toString());
    }

    static Class class$(String x0) {
        try {
            return Class.forName(x0);
        } catch (ClassNotFoundException x1) {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }

    public String getPrimitivePropertyAsString(String name) {
        Integer index = propertyIndex(name);
        if (index != null) {
            Class class$;
            PropertyInfo propertyInfo = (PropertyInfo) this.properties.elementAt(index.intValue());
            Class type = propertyInfo.getType();
            if (class$org$ksoap2$serialization$SoapObject == null) {
                class$ = class$("org.ksoap2.serialization.SoapObject");
                class$org$ksoap2$serialization$SoapObject = class$;
            } else {
                class$ = class$org$ksoap2$serialization$SoapObject;
            }
            if (type != class$) {
                return propertyInfo.getValue().toString();
            }
            return "";
        }
        throw new RuntimeException(new StringBuffer().append("illegal property: ").append(name).toString());
    }

    public Object getPrimitivePropertySafely(String name) {
        Integer index = propertyIndex(name);
        if (index == null) {
            return new NullSoapObject();
        }
        Class class$;
        PropertyInfo propertyInfo = (PropertyInfo) this.properties.elementAt(index.intValue());
        Class type = propertyInfo.getType();
        if (class$org$ksoap2$serialization$SoapObject == null) {
            class$ = class$("org.ksoap2.serialization.SoapObject");
            class$org$ksoap2$serialization$SoapObject = class$;
        } else {
            class$ = class$org$ksoap2$serialization$SoapObject;
        }
        if (type != class$) {
            return propertyInfo.getValue().toString();
        }
        Object class$2;
        propertyInfo = new PropertyInfo();
        if (class$java$lang$String == null) {
            class$2 = class$("java.lang.String");
            class$java$lang$String = class$2;
        } else {
            class$2 = class$java$lang$String;
        }
        propertyInfo.setType(class$2);
        propertyInfo.setValue("");
        propertyInfo.setName(name);
        return propertyInfo.getValue();
    }

    public String getPrimitivePropertySafelyAsString(String name) {
        Integer index = propertyIndex(name);
        if (index == null) {
            return "";
        }
        Class class$;
        PropertyInfo propertyInfo = (PropertyInfo) this.properties.elementAt(index.intValue());
        Class type = propertyInfo.getType();
        if (class$org$ksoap2$serialization$SoapObject == null) {
            class$ = class$("org.ksoap2.serialization.SoapObject");
            class$org$ksoap2$serialization$SoapObject = class$;
        } else {
            class$ = class$org$ksoap2$serialization$SoapObject;
        }
        if (type != class$) {
            return propertyInfo.getValue().toString();
        }
        return "";
    }

    private Integer propertyIndex(String name) {
        if (name != null) {
            for (int i = 0; i < this.properties.size(); i++) {
                if (name.equals(((PropertyInfo) this.properties.elementAt(i)).getName())) {
                    return new Integer(i);
                }
            }
        }
        return null;
    }

    public int getPropertyCount() {
        return this.properties.size();
    }

    public void getPropertyInfo(int index, Hashtable properties, PropertyInfo propertyInfo) {
        getPropertyInfo(index, propertyInfo);
    }

    public void getPropertyInfo(int index, PropertyInfo propertyInfo) {
        PropertyInfo element = this.properties.elementAt(index);
        if (element instanceof PropertyInfo) {
            PropertyInfo p = element;
            propertyInfo.name = p.name;
            propertyInfo.namespace = p.namespace;
            propertyInfo.flags = p.flags;
            propertyInfo.type = p.type;
            propertyInfo.elementType = p.elementType;
            propertyInfo.value = p.value;
            propertyInfo.multiRef = p.multiRef;
            return;
        }
        propertyInfo.name = null;
        propertyInfo.namespace = null;
        propertyInfo.flags = 0;
        propertyInfo.type = null;
        propertyInfo.elementType = null;
        propertyInfo.value = element;
        propertyInfo.multiRef = false;
    }

    public SoapObject newInstance() {
        SoapObject o = new SoapObject(this.namespace, this.name);
        for (int propIndex = 0; propIndex < this.properties.size(); propIndex++) {
            Object prop = this.properties.elementAt(propIndex);
            if (prop instanceof PropertyInfo) {
                o.addProperty((PropertyInfo) ((PropertyInfo) this.properties.elementAt(propIndex)).clone());
            } else if (prop instanceof SoapObject) {
                o.addSoapObject(((SoapObject) prop).newInstance());
            }
        }
        for (int attribIndex = 0; attribIndex < getAttributeCount(); attribIndex++) {
            AttributeInfo newAI = new AttributeInfo();
            getAttributeInfo(attribIndex, newAI);
            o.addAttribute(newAI);
        }
        return o;
    }

    public void setProperty(int index, Object value) {
        Object prop = this.properties.elementAt(index);
        if (prop instanceof PropertyInfo) {
            ((PropertyInfo) prop).setValue(value);
        }
    }

    public SoapObject addProperty(String name, Object value) {
        PropertyInfo propertyInfo = new PropertyInfo();
        propertyInfo.name = name;
        propertyInfo.type = value == null ? PropertyInfo.OBJECT_CLASS : value.getClass();
        propertyInfo.value = value;
        return addProperty(propertyInfo);
    }

    public SoapObject addPropertyIfValue(String name, Object value) {
        if (value != null) {
            return addProperty(name, value);
        }
        return this;
    }

    public SoapObject addPropertyIfValue(PropertyInfo propertyInfo, Object value) {
        if (value == null) {
            return this;
        }
        propertyInfo.setValue(value);
        return addProperty(propertyInfo);
    }

    public SoapObject addProperty(PropertyInfo propertyInfo) {
        this.properties.addElement(propertyInfo);
        return this;
    }

    public SoapObject addPropertyIfValue(PropertyInfo propertyInfo) {
        if (propertyInfo.value != null) {
            this.properties.addElement(propertyInfo);
        }
        return this;
    }

    public SoapObject addSoapObject(SoapObject soapObject) {
        this.properties.addElement(soapObject);
        return this;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer(new StringBuffer().append("").append(this.name).append("{").toString());
        for (int i = 0; i < getPropertyCount(); i++) {
            Object prop = this.properties.elementAt(i);
            if (prop instanceof PropertyInfo) {
                buf.append("").append(((PropertyInfo) prop).getName()).append("=").append(getProperty(i)).append("; ");
            } else {
                buf.append(((SoapObject) prop).toString());
            }
        }
        buf.append("}");
        return buf.toString();
    }
}
