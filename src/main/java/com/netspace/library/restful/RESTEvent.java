package com.netspace.library.restful;

import com.google.gson.annotations.Expose;

public class RESTEvent {
    public static int RESULT_CANCELED = 0;
    public static int RESULT_FAILURE = -2;
    public static int RESULT_OK = -1;
    public static int RESULT_TIMEOUT = -3;
    public static RESTEvent mCancelEvent;
    public static RESTEvent mOKEvent;
    @Expose
    public int nResult;
    @Expose
    public String szMessage;
    @Expose
    public String szRequestGUID;
    @Expose
    public String szResult;

    public static RESTEvent getEvent(int nResult) {
        return getEvent(nResult, null);
    }

    public static RESTEvent getEvent(int nResult, String szMessage) {
        RESTEvent event = new RESTEvent();
        event.nResult = nResult;
        event.szMessage = szMessage;
        return event;
    }

    public static RESTEvent getOKEvent() {
        if (mOKEvent == null) {
            mOKEvent = new RESTEvent();
            mOKEvent.nResult = RESULT_OK;
        }
        return mOKEvent;
    }

    public static RESTEvent getCancelEvent() {
        if (mCancelEvent == null) {
            mCancelEvent = new RESTEvent();
            mCancelEvent.nResult = RESULT_CANCELED;
        }
        return mCancelEvent;
    }
}
