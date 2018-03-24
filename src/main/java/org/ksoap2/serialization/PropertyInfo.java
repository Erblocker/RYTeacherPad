package org.ksoap2.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Vector;

public class PropertyInfo implements Serializable {
    public static final Class BOOLEAN_CLASS = new Boolean(true).getClass();
    public static final Class INTEGER_CLASS = new Integer(0).getClass();
    public static final Class LONG_CLASS = new Long(0).getClass();
    public static final int MULTI_REF = 2;
    public static final Class OBJECT_CLASS = new Object().getClass();
    public static final PropertyInfo OBJECT_TYPE = new PropertyInfo();
    public static final int REF_ONLY = 4;
    public static final Class STRING_CLASS = "".getClass();
    public static final int TRANSIENT = 1;
    public static final Class VECTOR_CLASS = new Vector().getClass();
    public PropertyInfo elementType;
    public int flags;
    public boolean multiRef;
    public String name;
    public String namespace;
    public Object type = OBJECT_CLASS;
    protected Object value;

    public void clear() {
        this.type = OBJECT_CLASS;
        this.flags = 0;
        this.name = null;
        this.namespace = null;
    }

    public PropertyInfo getElementType() {
        return this.elementType;
    }

    public void setElementType(PropertyInfo elementType) {
        this.elementType = elementType;
    }

    public int getFlags() {
        return this.flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public boolean isMultiRef() {
        return this.multiRef;
    }

    public void setMultiRef(boolean multiRef) {
        this.multiRef = multiRef;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Object getType() {
        return this.type;
    }

    public void setType(Object type) {
        this.type = type;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.name);
        sb.append(" : ");
        if (this.value != null) {
            sb.append(this.value);
        } else {
            sb.append("(not set)");
        }
        return sb.toString();
    }

    public Object clone() {
        Object obj = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(this);
            out.flush();
            out.close();
            obj = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        } catch (NotSerializableException nse) {
            nse.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
