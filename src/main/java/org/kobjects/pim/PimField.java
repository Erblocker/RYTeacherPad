package org.kobjects.pim;

import java.util.Enumeration;
import java.util.Hashtable;

public class PimField {
    String name;
    Hashtable properties;
    Object value;

    public PimField(PimField orig) {
        this(orig.name);
        if (orig.value instanceof String[]) {
            String[] val = new String[((String[]) orig.value).length];
            System.arraycopy((String[]) orig.value, 0, val, 0, val.length);
            this.value = val;
        } else {
            this.value = orig.value;
        }
        if (orig.properties != null) {
            this.properties = new Hashtable();
            Enumeration e = orig.properties.keys();
            while (e.hasMoreElements()) {
                String name = (String) e.nextElement();
                this.properties.put(name, orig.properties.get(name));
            }
        }
    }

    public PimField(String name) {
        this.name = name;
    }

    public Enumeration propertyNames() {
        return this.properties.keys();
    }

    public void setProperty(String name, String value) {
        if (this.properties == null) {
            if (value != null) {
                this.properties = new Hashtable();
            } else {
                return;
            }
        }
        if (value == null) {
            this.properties.remove(name);
        } else {
            this.properties.put(name, value);
        }
    }

    public void setValue(Object object) {
        this.value = object;
    }

    public Object getValue() {
        return this.value;
    }

    public String toString() {
        return this.name + (this.properties != null ? ";" + this.properties : "") + ":" + this.value;
    }

    public String getProperty(String name) {
        return this.properties == null ? null : (String) this.properties.get(name);
    }

    public boolean getAttribute(String name) {
        String s = getProperty("type");
        if (s == null || s.indexOf(name) == -1) {
            return false;
        }
        return true;
    }

    public void setAttribute(String name, boolean value) {
        if (getAttribute(name) != value) {
            String s = getProperty("type");
            if (!value) {
                int i = s.indexOf(name);
                if (i > 0) {
                    i--;
                }
                if (i != -1) {
                    s = s.substring(0, i) + s.substring((name.length() + i) + 1);
                }
            } else if (s == null || s.length() == 0) {
                s = name;
            } else {
                s = s + name;
            }
            setProperty("type", s);
        }
    }
}
