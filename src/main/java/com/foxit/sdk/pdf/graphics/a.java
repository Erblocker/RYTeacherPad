package com.foxit.sdk.pdf.graphics;

import com.foxit.sdk.common.PDFException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/* compiled from: ReflectUtils */
class a {
    protected static Object a(Class<?> cls, long j, boolean z) throws PDFException {
        if (cls == null) {
            throw new PDFException(8);
        }
        Object obj = null;
        try {
            Constructor declaredConstructor = cls.getDeclaredConstructor(new Class[]{Long.TYPE, Boolean.TYPE});
            declaredConstructor.setAccessible(true);
            obj = declaredConstructor.newInstance(new Object[]{Long.valueOf(j), Boolean.valueOf(z)});
            declaredConstructor.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (obj != null) {
            return obj;
        }
        throw new PDFException(8);
    }

    protected static Object a(Object obj, Class<?> cls, String str, Object obj2) throws PDFException {
        if (obj == null) {
            throw new PDFException(8);
        }
        Object obj3 = null;
        try {
            Method declaredMethod = cls.getDeclaredMethod(str, new Class[]{obj2.getClass()});
            declaredMethod.setAccessible(true);
            obj3 = declaredMethod.invoke(obj, new Object[]{obj2});
            declaredMethod.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (obj3 != null) {
            return obj3;
        }
        throw new PDFException(8);
    }

    protected static Object a(Class<?> cls, String str, Object obj) throws PDFException {
        Object obj2 = null;
        if (cls == null) {
            throw new PDFException(8);
        }
        try {
            Method declaredMethod = cls.getDeclaredMethod(str, new Class[]{obj.getClass()});
            declaredMethod.setAccessible(true);
            obj2 = declaredMethod.invoke(null, new Object[]{obj});
            declaredMethod.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (obj2 != null) {
            return obj2;
        }
        throw new PDFException(8);
    }
}
