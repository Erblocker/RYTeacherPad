package com.netspace.library.restful;

import java.util.HashMap;

public class RESTRequestManager {
    private static HashMap<String, RESTRequest> mmapRequest = new HashMap();
    private static HashMap<String, RESTRequest> mmapUniqueRequest = new HashMap();

    public static void registerRequest(RESTRequest request) {
        synchronized (mmapRequest) {
            mmapRequest.put(request.getRequestGUID(), request);
        }
    }

    public static RESTRequest getRequest(String szGUID) {
        RESTRequest weakRequest;
        synchronized (mmapRequest) {
            weakRequest = (RESTRequest) mmapRequest.get(szGUID);
        }
        return weakRequest;
    }

    public static void removeRequest(String szGUID) {
        synchronized (mmapRequest) {
            mmapRequest.remove(szGUID);
        }
    }

    public static void removeRequest(String szGUID, String szOperationName) {
        synchronized (mmapRequest) {
            mmapRequest.remove(szGUID);
        }
        if (szOperationName != null) {
            synchronized (mmapUniqueRequest) {
                mmapUniqueRequest.remove(szOperationName);
            }
        }
    }

    public static void registerUnique(String szOperationName, RESTRequest restRequest) {
        synchronized (mmapUniqueRequest) {
            mmapUniqueRequest.put(szOperationName, restRequest);
        }
    }

    public static boolean isUniqueRequestFinished(String szOperationName) {
        synchronized (mmapUniqueRequest) {
            RESTRequest request = (RESTRequest) mmapUniqueRequest.get(szOperationName);
        }
        if (request == null) {
            return true;
        }
        return request.isAllFinish();
    }
}
