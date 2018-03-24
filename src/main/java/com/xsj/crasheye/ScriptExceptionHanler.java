package com.xsj.crasheye;

import com.xsj.crasheye.util.Utils;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class ScriptExceptionHanler {
    private static HashMap<String, Long> exceptions = new HashMap();

    public static synchronized void logScriptException(String errorTitle, String stacktrace, String language) {
        synchronized (ScriptExceptionHanler.class) {
            if (isSendReoprt(new StringBuilder(String.valueOf(errorTitle)).append(stacktrace).toString())) {
                new ActionScriptError(errorTitle, stacktrace, language, 1, null).send(new NetSender(), true);
            }
        }
    }

    private static boolean isSendReoprt(String exceptionMessage) {
        if (exceptionMessage.equals("") || exceptionMessage == null) {
            return false;
        }
        try {
            String md5ExMessage = Utils.MD5(exceptionMessage);
            Iterator<Entry<String, Long>> it = exceptions.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, Long> entry = (Entry) it.next();
                if (((String) entry.getKey()).equals(md5ExMessage)) {
                    if (Utils.getTimeForLong() - ((Long) entry.getValue()).longValue() < 600000) {
                        return false;
                    }
                    entry.setValue(Long.valueOf(Utils.getTimeForLong()));
                    return true;
                } else if (Utils.getTimeForLong() - ((Long) entry.getValue()).longValue() >= 600000) {
                    it.remove();
                }
            }
            exceptions.put(md5ExMessage, Long.valueOf(Utils.getTimeForLong()));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
