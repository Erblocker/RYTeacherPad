package com.foxit.sdk.common;

import java.lang.reflect.Method;

/* compiled from: ReflectUtils */
class a {
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

    protected static Object a(Object obj, String str) throws PDFException {
        Exception exception;
        if (obj == null) {
            throw new PDFException(8);
        }
        Object obj2 = null;
        try {
            Method declaredMethod = obj.getClass().getDeclaredMethod(str, new Class[0]);
            declaredMethod.setAccessible(true);
            Object invoke = declaredMethod.invoke(obj, new Object[0]);
            try {
                declaredMethod.setAccessible(false);
                return invoke;
            } catch (Exception e) {
                Exception exception2 = e;
                obj2 = invoke;
                exception = exception2;
            }
        } catch (Exception e2) {
            exception = e2;
            if (exception instanceof PDFException) {
                throw new PDFException(((PDFException) exception).getLastError());
            }
            exception.printStackTrace();
            return obj2;
        }
    }
}
