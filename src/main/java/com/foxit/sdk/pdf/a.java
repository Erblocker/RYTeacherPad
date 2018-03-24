package com.foxit.sdk.pdf;

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

    protected static void a(Object obj, String str) throws PDFException {
        if (obj == null) {
            throw new PDFException(8);
        }
        try {
            Method declaredMethod = obj.getClass().getDeclaredMethod(str, new Class[0]);
            declaredMethod.setAccessible(true);
            declaredMethod.invoke(obj, new Object[0]);
            declaredMethod.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static Object a(Class<?> cls, String str, Class<?>[] clsArr, Object[] objArr) throws PDFException {
        Exception e;
        if (cls == null) {
            throw new PDFException(8);
        }
        Object invoke;
        try {
            Method declaredMethod = cls.getDeclaredMethod(str, clsArr);
            declaredMethod.setAccessible(true);
            invoke = declaredMethod.invoke(null, objArr);
            try {
                declaredMethod.setAccessible(false);
            } catch (Exception e2) {
                e = e2;
                e.printStackTrace();
                return invoke;
            }
        } catch (Exception e3) {
            Exception exception = e3;
            invoke = null;
            e = exception;
            e.printStackTrace();
            return invoke;
        }
        return invoke;
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
