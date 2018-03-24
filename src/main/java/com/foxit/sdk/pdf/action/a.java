package com.foxit.sdk.pdf.action;

import com.foxit.sdk.common.PDFException;
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
}
