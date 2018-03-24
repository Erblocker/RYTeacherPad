package com.foxit.sdk.pdf.signature;

import com.foxit.sdk.common.PDFException;
import java.lang.reflect.Constructor;

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
}
