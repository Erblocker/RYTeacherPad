package org.ksoap2.serialization;

public class SoapPrimitive extends AttributeContainer {
    String name;
    String namespace;
    String value;

    public SoapPrimitive(String namespace, String name, String value) {
        this.namespace = namespace;
        this.name = name;
        this.value = value;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean equals(Object o) {
        boolean z = true;
        if (!(o instanceof SoapPrimitive)) {
            return false;
        }
        boolean varsEqual;
        SoapPrimitive p = (SoapPrimitive) o;
        if (this.name.equals(p.name) && (this.namespace != null ? this.namespace.equals(p.namespace) : p.namespace == null)) {
            if (this.value != null) {
                if (this.value.equals(p.value)) {
                }
            }
            varsEqual = true;
            if (!(varsEqual && attributesAreEqual(p))) {
                z = false;
            }
            return z;
        }
        varsEqual = false;
        z = false;
        return z;
    }

    public int hashCode() {
        return (this.namespace == null ? 0 : this.namespace.hashCode()) ^ this.name.hashCode();
    }

    public String toString() {
        return this.value;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getName() {
        return this.name;
    }
}
