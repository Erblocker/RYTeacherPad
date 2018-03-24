package org.kobjects.pim;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public abstract class PimItem {
    public static final int TYPE_STRING = 0;
    public static final int TYPE_STRING_ARRAY = 1;
    Hashtable fields = new Hashtable();

    public abstract int getArraySize(String str);

    public abstract String getType();

    public PimItem(PimItem orig) {
        Enumeration e = orig.fields();
        while (e.hasMoreElements()) {
            addField(new PimField((PimField) e.nextElement()));
        }
    }

    public Enumeration fieldNames() {
        return this.fields.keys();
    }

    public void addField(PimField field) {
        Vector v = (Vector) this.fields.get(field.name);
        if (v == null) {
            v = new Vector();
            this.fields.put(field.name, v);
        }
        v.addElement(field);
    }

    public Enumeration fields() {
        Vector v = new Vector();
        Enumeration e = fieldNames();
        while (e.hasMoreElements()) {
            Enumeration f = fields((String) e.nextElement());
            while (f.hasMoreElements()) {
                v.addElement(f.nextElement());
            }
        }
        return v.elements();
    }

    public Enumeration fields(String name) {
        Vector v = (Vector) this.fields.get(name);
        if (v == null) {
            v = new Vector();
        }
        return v.elements();
    }

    public PimField getField(String name, int index) {
        return (PimField) ((Vector) this.fields.get(name)).elementAt(index);
    }

    public int getFieldCount(String name) {
        Vector v = (Vector) this.fields.get(name);
        return v == null ? 0 : v.size();
    }

    public int getType(String name) {
        return getArraySize(name) == -1 ? 0 : 1;
    }

    public void removeField(String name, int index) {
        ((Vector) this.fields.get(name)).removeElementAt(index);
    }

    public String toString() {
        return getType() + ":" + this.fields.toString();
    }
}
