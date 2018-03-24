package javazoom.jl.decoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;

public class JavaLayerUtils {
    private static JavaLayerHook hook = null;

    public static Object deserialize(InputStream in, Class cls) throws IOException {
        if (cls == null) {
            throw new NullPointerException("cls");
        }
        Object obj = deserialize(in, cls);
        if (cls.isInstance(obj)) {
            return obj;
        }
        throw new InvalidObjectException("type of deserialized instance not of required class.");
    }

    public static Object deserialize(InputStream in) throws IOException {
        if (in == null) {
            throw new NullPointerException("in");
        }
        try {
            return new ObjectInputStream(in).readObject();
        } catch (ClassNotFoundException ex) {
            throw new InvalidClassException(ex.toString());
        }
    }

    public static Object deserializeArray(InputStream in, Class elemType, int length) throws IOException {
        if (elemType == null) {
            throw new NullPointerException("elemType");
        } else if (length < -1) {
            throw new IllegalArgumentException("length");
        } else {
            Object obj = deserialize(in);
            Class cls = obj.getClass();
            if (!cls.isArray()) {
                throw new InvalidObjectException("object is not an array");
            } else if (cls.getComponentType() != elemType) {
                throw new InvalidObjectException("unexpected array component type");
            } else if (length == -1 || Array.getLength(obj) == length) {
                return obj;
            } else {
                throw new InvalidObjectException("array length mismatch");
            }
        }
    }

    public static Object deserializeArrayResource(String name, Class elemType, int length) throws IOException {
        InputStream str = getResourceAsStream(name);
        if (str != null) {
            return deserializeArray(str, elemType, length);
        }
        throw new IOException("unable to load resource '" + name + "'");
    }

    public static void serialize(OutputStream out, Object obj) throws IOException {
        if (out == null) {
            throw new NullPointerException("out");
        } else if (obj == null) {
            throw new NullPointerException("obj");
        } else {
            new ObjectOutputStream(out).writeObject(obj);
        }
    }

    public static synchronized void setHook(JavaLayerHook hook0) {
        synchronized (JavaLayerUtils.class) {
            hook = hook0;
        }
    }

    public static synchronized JavaLayerHook getHook() {
        JavaLayerHook javaLayerHook;
        synchronized (JavaLayerUtils.class) {
            javaLayerHook = hook;
        }
        return javaLayerHook;
    }

    public static synchronized InputStream getResourceAsStream(String name) {
        InputStream is;
        synchronized (JavaLayerUtils.class) {
            if (hook != null) {
                is = hook.getResourceAsStream(name);
            } else {
                is = JavaLayerUtils.class.getResourceAsStream(name);
            }
        }
        return is;
    }
}
