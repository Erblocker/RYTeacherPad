package com.netspace.library.threads;

import android.util.Log;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

public class MessagePostThread2 extends Thread {
    private static final String TAG = "MessagePostThread2";
    private static String mPostURL = "";
    private static ArrayList<String> marrDataToSend = new ArrayList();
    private static ArrayList<String> marrUrlSend = new ArrayList();
    private OnMessagePostedListener mCallBack;
    private InputStream mInputStream;
    private Socket mSocket;
    private DataOutputStream mStream;
    private boolean mbStop = false;

    public interface OnMessagePostedListener {
        void OnMessagePosted(String str);
    }

    private void uploadData(java.lang.String r12, java.lang.String r13) {
        /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.base/java.util.HashMap$HashIterator.nextNode(HashMap.java:1496)
	at java.base/java.util.HashMap$KeyIterator.next(HashMap.java:1517)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:537)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:176)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:81)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:52)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:323)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:226)
*/
        /*
        r11 = this;
        r1 = 0;
        r7 = new java.net.URL;	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r7.<init>(r13);	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r9 = r7.openConnection();	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r0 = r9;	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r0 = (java.net.HttpURLConnection) r0;	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r1 = r0;	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r9 = 3000; // 0xbb8 float:4.204E-42 double:1.482E-320;	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r1.setReadTimeout(r9);	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r9 = "POST";	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r1.setRequestMethod(r9);	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r9 = "Content-Type";	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r10 = "application/x-www-form-urlencoded";	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r1.setRequestProperty(r9, r10);	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r9 = "Content-Encoding";	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r10 = "UTF-8";	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r1.setRequestProperty(r9, r10);	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r9 = "Content-Length";	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r10 = "utf-8";	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r10 = r12.getBytes(r10);	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r10 = r10.length;	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r10 = java.lang.Integer.toString(r10);	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r1.setRequestProperty(r9, r10);	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r9 = 0;	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r1.setUseCaches(r9);	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r9 = 1;	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r1.setDoInput(r9);	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r9 = 1;	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r1.setDoOutput(r9);	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r8 = new java.io.DataOutputStream;	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r9 = r1.getOutputStream();	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r8.<init>(r9);	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r9 = "utf-8";	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r9 = r12.getBytes(r9);	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r8.write(r9);	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r8.flush();	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r8.close();	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r3 = r1.getInputStream();	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r5 = new java.io.BufferedReader;	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r9 = new java.io.InputStreamReader;	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r9.<init>(r3);	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r5.<init>(r9);	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r6 = new java.lang.StringBuffer;	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r6.<init>();	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
    L_0x0075:
        r4 = r5.readLine();	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        if (r4 != 0) goto L_0x008d;	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
    L_0x007b:
        r5.close();	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r9 = r11.mCallBack;	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        if (r9 == 0) goto L_0x0087;	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
    L_0x0082:
        r9 = r11.mCallBack;	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r9.OnMessagePosted(r12);	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
    L_0x0087:
        if (r1 == 0) goto L_0x008c;
    L_0x0089:
        r1.disconnect();
    L_0x008c:
        return;
    L_0x008d:
        r6.append(r4);	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r9 = 13;	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        r6.append(r9);	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        goto L_0x0075;
    L_0x0096:
        r2 = move-exception;
        r2.printStackTrace();	 Catch:{ Exception -> 0x0096, all -> 0x00a0 }
        if (r1 == 0) goto L_0x008c;
    L_0x009c:
        r1.disconnect();
        goto L_0x008c;
    L_0x00a0:
        r9 = move-exception;
        if (r1 == 0) goto L_0x00a6;
    L_0x00a3:
        r1.disconnect();
    L_0x00a6:
        throw r9;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.netspace.library.threads.MessagePostThread2.uploadData(java.lang.String, java.lang.String):void");
    }

    public MessagePostThread2(String szPostURL, OnMessagePostedListener CallBack) {
        mPostURL = szPostURL;
        this.mCallBack = CallBack;
    }

    public static void setPostData(String szData, String szURL) {
        synchronized (marrDataToSend) {
            for (int i = 0; i < marrUrlSend.size(); i++) {
                if (((String) marrUrlSend.get(i)).contentEquals(szURL)) {
                    marrDataToSend.set(i, new StringBuilder(String.valueOf((String) marrDataToSend.get(i))).append("\r\n").append(szData).toString());
                    return;
                }
            }
            marrDataToSend.add(szData);
            marrUrlSend.add(szURL);
        }
    }

    public void stopThread() {
        Log.d(TAG, "request MessagePostThread2 to close.");
        this.mbStop = true;
        if (this.mInputStream != null) {
            try {
                this.mInputStream.close();
                if (this.mSocket != null) {
                    this.mSocket.close();
                }
                join();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
        }
    }

    public void run() {
        setName(TAG);
        while (!this.mbStop) {
            String szData = "";
            String szTargetURL = "";
            boolean bHasDataToSend = false;
            synchronized (marrDataToSend) {
                if (marrDataToSend.size() > 0) {
                    szData = (String) marrDataToSend.get(0);
                    szTargetURL = (String) marrUrlSend.get(0);
                    marrDataToSend.remove(0);
                    marrUrlSend.remove(0);
                    bHasDataToSend = true;
                }
            }
            if (bHasDataToSend) {
                String[] arrData = szData.split("\r\n");
                String szOldURL = szTargetURL;
                String szNonJsonData = "";
                for (String szData2 : arrData) {
                    if (szData2.startsWith("{")) {
                        try {
                            JSONObject JSON = new JSONObject(szData2);
                            szTargetURL = szOldURL;
                            JSONObject jSONObject;
                            try {
                                szTargetURL = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(szTargetURL)).append("&guid=").append(JSON.getString("guid")).toString())).append("&from=").append(JSON.getString("from")).toString())).append("&expire=").append(JSON.getString("expire")).toString();
                                jSONObject = JSON;
                            } catch (JSONException e) {
                                jSONObject = JSON;
                            }
                        } catch (JSONException e2) {
                        }
                        if (!szNonJsonData.isEmpty()) {
                            uploadData(szNonJsonData, szOldURL);
                            szNonJsonData = "";
                        }
                        uploadData(szData2, szTargetURL);
                    } else {
                        szNonJsonData = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(szNonJsonData)).append(szData2).toString())).append("\r\n").toString();
                    }
                }
                if (!szNonJsonData.isEmpty()) {
                    uploadData(szNonJsonData, szOldURL);
                    String str = "";
                }
            }
            if (!this.mbStop) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e3) {
                    e3.printStackTrace();
                }
            } else {
                return;
            }
        }
    }
}
