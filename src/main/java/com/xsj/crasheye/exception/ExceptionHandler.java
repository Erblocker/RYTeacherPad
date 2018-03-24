package com.xsj.crasheye.exception;

import com.xsj.crasheye.ActionError;
import com.xsj.crasheye.ActionTransactionStop;
import com.xsj.crasheye.AsyncDataSaver;
import com.xsj.crasheye.Crasheye;
import com.xsj.crasheye.EnumActionType;
import com.xsj.crasheye.EnumExceptionType;
import com.xsj.crasheye.Properties;
import com.xsj.crasheye.TransactionsDatabase.Container;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Iterator;
import java.util.Map.Entry;

public class ExceptionHandler implements UncaughtExceptionHandler {
    private UncaughtExceptionHandler defaultExceptionHandler;

    public ExceptionHandler(UncaughtExceptionHandler pDefaultExceptionHandler) {
        this.defaultExceptionHandler = pDefaultExceptionHandler;
    }

    public void uncaughtException(Thread t, Throwable e) {
        Writer stacktrace = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stacktrace);
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            cause.printStackTrace(printWriter);
        }
        ActionError crashData = new ActionError(EnumActionType.error, stacktrace.toString(), EnumExceptionType.UNHANDLED, null);
        Iterator<Entry<String, Container>> iterator = Properties.transactionsDatabase.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Container> pair = (Entry) iterator.next();
            if (pair.getValue() != null) {
                ActionTransactionStop.createTransactionFail(((String) pair.getKey()).replace("TStart:name:", ""), crashData.getErrorHash(), null).save(new AsyncDataSaver());
            }
            iterator.remove();
        }
        crashData.save(null);
        if (Crasheye.crasheyeCallback != null) {
            Crasheye.crasheyeCallback.lastBreath(new Exception(e));
        }
        this.defaultExceptionHandler.uncaughtException(t, e);
    }
}
