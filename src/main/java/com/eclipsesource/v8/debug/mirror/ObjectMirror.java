package com.eclipsesource.v8.debug.mirror;

import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;

public class ObjectMirror extends ValueMirror {
    private static final String PROPERTIES = "properties";
    private static final String PROPERTY_NAMES = "propertyNames";

    public enum PropertyKind {
        Named(1),
        Indexed(2);
        
        int index;

        private PropertyKind(int index) {
            this.index = index;
        }
    }

    public String toString() {
        return this.v8Object.toString();
    }

    ObjectMirror(V8Object v8Object) {
        super(v8Object);
    }

    public boolean isObject() {
        return true;
    }

    public String[] getPropertyNames(PropertyKind kind, int limit) {
        V8Array parameters = new V8Array(this.v8Object.getRuntime());
        parameters.push(kind.index);
        parameters.push(limit);
        V8Array propertyNames = null;
        try {
            propertyNames = this.v8Object.executeArrayFunction(PROPERTY_NAMES, parameters);
            String[] result = new String[propertyNames.length()];
            for (int i = 0; i < result.length; i++) {
                result[i] = propertyNames.getString(i);
            }
            return result;
        } finally {
            parameters.release();
            if (propertyNames != null) {
                propertyNames.release();
            }
        }
    }

    public PropertiesArray getProperties(PropertyKind kind, int limit) {
        V8Array parameters = new V8Array(this.v8Object.getRuntime());
        parameters.push(kind.index);
        parameters.push(limit);
        V8Array result = null;
        try {
            result = this.v8Object.executeArrayFunction(PROPERTIES, parameters);
            PropertiesArray propertiesArray = new PropertiesArray(result);
            return propertiesArray;
        } finally {
            parameters.release();
            if (!(result == null || result.isReleased())) {
                result.release();
            }
        }
    }
}
