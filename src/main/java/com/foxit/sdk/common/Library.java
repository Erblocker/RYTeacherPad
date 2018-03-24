package com.foxit.sdk.common;

import com.foxit.sdk.pdf.annots.AnnotIconProvider;
import com.foxit.sdk.pdf.security.a;

public class Library {
    private static Boolean a = new Boolean(false);

    public static void init(String str, String str2) throws PDFException {
        int Library_init = CommonJNI.Library_init(str, str2);
        if (Library_init != 0) {
            throw new PDFException(Library_init);
        }
        synchronized (a) {
            a = Boolean.valueOf(false);
        }
    }

    public static void reinit() throws PDFException {
        int Library_reinit = CommonJNI.Library_reinit();
        if (Library_reinit != 0) {
            throw new PDFException(Library_reinit);
        }
        synchronized (a) {
            a = Boolean.valueOf(false);
        }
    }

    public static void release() throws PDFException {
        CommonJNI.Library_release();
        synchronized (a) {
            a = Boolean.valueOf(false);
        }
    }

    public static int getModuleRight(int i) throws PDFException {
        return CommonJNI.Library_getModuleRight(i);
    }

    public static String getVersion() throws PDFException {
        return CommonJNI.Library_getVersion();
    }

    public static boolean setAnnotIconProvider(AnnotIconProvider annotIconProvider) throws PDFException {
        return CommonJNI.Library_setAnnotIconProvider(annotIconProvider);
    }

    public static boolean setNotifier(Notifier notifier) throws PDFException {
        if (notifier != null) {
            return CommonJNI.Library_setNotifier(notifier);
        }
        throw new PDFException(8);
    }

    public static boolean setActionHandler(ActionHandler actionHandler) throws PDFException {
        if (actionHandler != null) {
            return CommonJNI.Library_setActionHandler(actionHandler);
        }
        throw new PDFException(8);
    }

    public static boolean registerDefaultSignatureHandler() throws PDFException {
        boolean booleanValue;
        synchronized (a) {
            if (a.booleanValue()) {
                booleanValue = a.booleanValue();
            } else {
                a = Boolean.valueOf(CommonJNI.Library_registerDefaultSignatureHandler());
                booleanValue = a.booleanValue();
            }
        }
        return booleanValue;
    }

    public static boolean registerSecurityCallback(String str, a aVar) throws PDFException {
        return CommonJNI.Library_registerSecurityCallback(str, aVar);
    }

    public static boolean unregisterSecurityCallback(String str) throws PDFException {
        return CommonJNI.Library_unregisterSecurityCallback(str);
    }
}
