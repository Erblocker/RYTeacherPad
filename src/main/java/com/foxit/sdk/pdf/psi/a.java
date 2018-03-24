package com.foxit.sdk.pdf.psi;

import com.foxit.sdk.common.PDFException;
import java.lang.reflect.Method;

/* compiled from: ReflectUtils */
class a {
    protected static Object a(Object obj, String str, Class<?>[] clsArr, Object[] objArr) throws PDFException {
        if (obj == null) {
            throw new PDFException(8);
        }
        Object obj2 = null;
        try {
            Method declaredMethod = obj.getClass().getDeclaredMethod(str, clsArr);
            declaredMethod.setAccessible(true);
            obj2 = declaredMethod.invoke(obj, objArr);
            declaredMethod.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (obj2 != null) {
            return obj2;
        }
        throw new PDFException(8);
    }

    protected static Object a(Class<?> cls, String str, Class<?>[] clsArr, Object[] objArr) throws PDFException {
        Object invoke;
        Exception e;
        if (cls == null) {
            throw new PDFException(8);
        }
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
