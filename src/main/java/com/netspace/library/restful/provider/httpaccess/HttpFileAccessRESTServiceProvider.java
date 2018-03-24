package com.netspace.library.restful.provider.httpaccess;

import com.google.gson.annotations.Expose;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.restful.RESTEvent;
import com.netspace.library.restful.RESTRequest;
import com.netspace.library.restful.RESTRequest.RESTRequestCallBack;
import com.netspace.library.restful.RESTService;
import com.netspace.library.restful.RESTServiceProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.auth.AUTH;

public class HttpFileAccessRESTServiceProvider extends RESTServiceProvider {
    public static final String TARGETFILENAME = "targetfilename";
    public static final String TIMEOUT = "timeout";
    public static final String URI = "";

    public static class HttpFileAccessResultEvent extends RESTEvent {
        @Expose
        public String szTargetFileName;
    }

    public HttpFileAccessRESTServiceProvider initialize(RESTService instance) {
        super.initialize(instance);
        this.mszName = "HttpAccessService";
        this.mszURI = "";
        return this;
    }

    public boolean execute(RESTRequest request, String szURI, HashMap<String, String> arrParams, RESTRequestCallBack callBack) {
        super.execute(request, szURI, arrParams, callBack);
        String szFileName = optParam((HashMap) arrParams, "targetfilename", "");
        if (szFileName.isEmpty()) {
            try {
                szFileName = File.createTempFile("tmpfile", ".bin", this.mContext.getExternalCacheDir()).getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        handlePackageRead(szURI, true, optParam((HashMap) arrParams, TIMEOUT, 2000), szFileName, callBack, request);
        return true;
    }

    private boolean handlePackageRead(String szURL, boolean bNeedAuth, int nTimeoutMS, String szTargetFileName, RESTRequestCallBack callBack, RESTRequest restrequest) {
        IOException e;
        File outputFile = null;
        FileOutputStream OutputStream = null;
        RESTEvent result = new HttpFileAccessResultEvent();
        try {
            OkHttpClient client = new Builder().readTimeout((long) nTimeoutMS, TimeUnit.MILLISECONDS).connectTimeout((long) nTimeoutMS, TimeUnit.MILLISECONDS).writeTimeout((long) nTimeoutMS, TimeUnit.MILLISECONDS).hostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            }).retryOnConnectionFailure(true).build();
            Request.Builder requestBuilder = new Request.Builder().url(szURL);
            if (bNeedAuth) {
                requestBuilder.header(AUTH.WWW_AUTH_RESP, Credentials.basic(MyiBaseApplication.getCommonVariables().UserInfo.szUserName, MyiBaseApplication.getCommonVariables().Session.getPasswordMD5(), Charset.forName("UTF8")));
            }
            Response response = client.newCall(requestBuilder.build()).execute();
            int nResponseCode = response.code();
            if (response.isSuccessful()) {
                int nTotalLength = (int) response.body().contentLength();
                InputStream inputStream = response.body().byteStream();
                File file = new File(szTargetFileName);
                try {
                    byte[] Buffer = new byte[4096];
                    int nCurrentPos = 0;
                    FileOutputStream OutputStream2 = new FileOutputStream(file.getPath());
                    while (true) {
                        try {
                            int nReadCount = inputStream.read(Buffer);
                            if (nReadCount == -1) {
                                break;
                            }
                            OutputStream2.write(Buffer, 0, nReadCount);
                            nCurrentPos += nReadCount;
                        } catch (IOException e2) {
                            e = e2;
                            OutputStream = OutputStream2;
                            outputFile = file;
                        }
                    }
                    Buffer = null;
                    OutputStream2.close();
                    inputStream.close();
                    if (nCurrentPos >= nTotalLength || nTotalLength == -1) {
                        file.setReadable(true, false);
                        result.nResult = RESTEvent.RESULT_OK;
                        restrequest.param("targetfilename", szTargetFileName);
                        callBack.onRestSuccess(this, restrequest, result);
                        OutputStream = OutputStream2;
                        outputFile = file;
                    } else {
                        file.delete();
                        result.nResult = RESTEvent.RESULT_FAILURE;
                        result.szMessage = "文件获取不完整，完整长度" + String.valueOf(nTotalLength) + "，只获得了" + String.valueOf(nCurrentPos) + "字节。";
                        callBack.onRestFailure(this, restrequest, result);
                        OutputStream = OutputStream2;
                        outputFile = file;
                    }
                } catch (IOException e3) {
                    e = e3;
                    outputFile = file;
                    e.printStackTrace();
                    if (OutputStream != null) {
                        try {
                            OutputStream.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    if (outputFile != null) {
                        outputFile.delete();
                    }
                    result.nResult = RESTEvent.RESULT_FAILURE;
                    result.szMessage = e.getMessage();
                    callBack.onRestFailure(this, restrequest, result);
                    return false;
                }
            }
            result.nResult = RESTEvent.RESULT_FAILURE;
            result.szMessage = "服务器端返回" + response.message() + "(" + String.valueOf(nResponseCode) + "代码)";
            callBack.onRestFailure(this, restrequest, result);
        } catch (IOException e4) {
            e = e4;
            e.printStackTrace();
            if (OutputStream != null) {
                OutputStream.close();
            }
            if (outputFile != null) {
                outputFile.delete();
            }
            result.nResult = RESTEvent.RESULT_FAILURE;
            result.szMessage = e.getMessage();
            callBack.onRestFailure(this, restrequest, result);
            return false;
        }
        return false;
    }
}
