package com.xsj.crasheye;

import com.xsj.crasheye.log.Logger;
import com.xsj.crasheye.util.Utils;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

class NetSender extends BaseExecutor implements InterfaceExecutor {
    NetSender() {
    }

    public synchronized void send(String data, boolean saveOnFail) {
        send(data, saveOnFail, false);
    }

    public synchronized void send(final String data, final boolean saveOnFail, final boolean isSession) {
        Thread t = new LowPriorityThreadFactory().newThread(new Runnable() {
            public void run() {
                NetSender.this.sendBlocking(data, saveOnFail, isSession);
            }
        });
        if (getExecutor() != null) {
            getExecutor().execute(t);
        }
    }

    public synchronized void send(String url, String data, boolean saveOnFail) {
        send(url, data, saveOnFail, false);
    }

    public synchronized void send(String url, String data, boolean saveOnFail, boolean isSession) {
        final String str = url;
        final String str2 = data;
        final boolean z = saveOnFail;
        final boolean z2 = isSession;
        Thread t = new LowPriorityThreadFactory().newThread(new Runnable() {
            public void run() {
                NetSender.this.sendBlocking(str, str2, z, z2);
            }
        });
        if (getExecutor() != null) {
            getExecutor().execute(t);
        }
    }

    public synchronized NetSenderResponse sendBlocking(String data, boolean saveOnFail, boolean isSession) {
        return sendBlocking(null, data, saveOnFail, isSession);
    }

    public synchronized NetSenderResponse sendBlocking(String data, boolean saveOnFail) {
        return sendBlocking(data, saveOnFail, false);
    }

    public synchronized NetSenderResponse sendBlocking(String url, String data, boolean saveOnFail) {
        return sendBlocking(url, data, saveOnFail, false);
    }

    public synchronized NetSenderResponse sendBlocking(String url, String data, boolean saveOnFail, boolean isSession) {
        NetSenderResponse mNetSenderResponse;
        HttpResponse response;
        mNetSenderResponse = new NetSenderResponse(url, data);
        if (data == null) {
            mNetSenderResponse.setException(new IllegalArgumentException("null data!"));
            if (Crasheye.crasheyeCallback != null) {
                Crasheye.crasheyeCallback.netSenderResponse(mNetSenderResponse);
            }
            Logger.logInfo(mNetSenderResponse.toString());
        } else {
            ByteArrayEntity byteArrayEntity;
            String sendData = Properties.actionTypeRegx.matcher(data).replaceAll("");
            byte[] gzipString = Utils.getGZipString(sendData);
            if (url == null) {
                EnumActionType action = Properties.findActionType(data);
                if (gzipString == null) {
                    url = CrasheyeUrls.getURL(action, sendData);
                } else {
                    url = CrasheyeUrls.getURL(action, gzipString);
                }
            }
            Logger.logInfo("NetSender: Sending data to url: " + url);
            Logger.logInfo("NetSender: Sending data value: " + data);
            HttpClient httpClient = new DefaultHttpClient();
            HttpParams params = httpClient.getParams();
            HttpProtocolParams.setUseExpectContinue(params, false);
            HttpConnectionParams.setConnectionTimeout(params, 20000);
            HttpConnectionParams.setSoTimeout(params, 20000);
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader(HTTP.CONTENT_TYPE, "application/x-gzip");
            httpPost.setHeader("gzip", "true");
            response = null;
            if (gzipString == null) {
                try {
                    byteArrayEntity = new ByteArrayEntity(data.getBytes());
                } catch (Error er) {
                    Logger.logError("NetSender: Transmitting Error " + er.getMessage());
                    if (Crasheye.DEBUG) {
                        er.printStackTrace();
                    }
                    if (response != null) {
                        mNetSenderResponse.setResponseCode(response.getStatusLine().getStatusCode());
                    }
                    mNetSenderResponse.setException(new Exception(er.getMessage()));
                    if (Crasheye.crasheyeCallback != null) {
                        Crasheye.crasheyeCallback.netSenderResponse(mNetSenderResponse);
                    }
                    if (saveOnFail) {
                        Logger.logWarning("NetSender: Couldn't send data, saving...");
                        new AsyncDataSaver().save(data, isSession ? CrasheyeFileFilter.createSessionNewFile() : CrasheyeFileFilter.createNewFile());
                    }
                } catch (Exception e) {
                    Logger.logError("NetSender: Transmitting Exception " + e.getMessage());
                    if (Crasheye.DEBUG) {
                        e.printStackTrace();
                    }
                    if (response != null) {
                        mNetSenderResponse.setResponseCode(response.getStatusLine().getStatusCode());
                    }
                    mNetSenderResponse.setException(e);
                    if (Crasheye.crasheyeCallback != null) {
                        Crasheye.crasheyeCallback.netSenderResponse(mNetSenderResponse);
                    }
                    if (saveOnFail) {
                        Logger.logWarning("NetSender: Couldn't send data, saving...");
                        new AsyncDataSaver().save(data, isSession ? CrasheyeFileFilter.createSessionNewFile() : CrasheyeFileFilter.createNewFile());
                    }
                }
            } else {
                byteArrayEntity = new ByteArrayEntity(gzipString);
            }
            httpPost.setEntity(byteArrayEntity);
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            int responseCode = response.getStatusLine().getStatusCode();
            Logger.logInfo("net send status code " + responseCode);
            mNetSenderResponse.setResponseCode(responseCode);
            if (entity != null || responseCode < 400) {
                if (entity != null) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent()));
                    String line = in.readLine();
                    Logger.logWarning("NetSender: Transmitting result " + line);
                    in.close();
                    mNetSenderResponse.setServerResponse(line);
                }
                mNetSenderResponse.setSentSuccessfully(Boolean.valueOf(true));
            } else {
                mNetSenderResponse.setException(new Exception(response.getStatusLine().getReasonPhrase()));
                if (Crasheye.crasheyeCallback != null) {
                    Crasheye.crasheyeCallback.netSenderResponse(mNetSenderResponse);
                }
            }
            if (Crasheye.crasheyeCallback != null) {
                if (Crasheye.crasheyeCallback != null) {
                    Crasheye.crasheyeCallback.netSenderResponse(mNetSenderResponse);
                }
            }
        }
        return mNetSenderResponse;
    }

    public ExecutorService getExecutor() {
        if (executor == null) {
            executor = Executors.newFixedThreadPool(2);
        }
        return executor;
    }
}
