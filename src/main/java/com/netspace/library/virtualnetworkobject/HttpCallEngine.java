package com.netspace.library.virtualnetworkobject;

import android.content.Context;
import android.util.Log;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.error.ErrorCode;
import com.netspace.library.utilities.Utilities;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import org.apache.http.auth.AUTH;

public class HttpCallEngine extends Engine {
    private static final String TAG = "HttpCallEngine";
    private final OkHttpClient mClient = new Builder().hostnameVerifier(new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }).retryOnConnectionFailure(true).build();
    protected String mEngineName = TAG;

    public HttpCallEngine(Context Context) {
        super(Context);
    }

    public String getEngineName() {
        return this.mEngineName;
    }

    public boolean handlePackageRead(ItemObject OneObject) {
        IOException e;
        setItemObjectActivityBusy(OneObject, true);
        HttpItemObject HttpItemObject = (HttpItemObject) OneObject;
        File outputFile = null;
        FileOutputStream OutputStream = null;
        int nBufferSize = HttpItemObject.getBufferSize();
        String szCharset = ((HttpItemObject) OneObject).getEncoding();
        try {
            HttpItemObject ItemObject = (HttpItemObject) OneObject;
            OkHttpClient client = this.mClient.newBuilder().readTimeout((long) (ItemObject.getTimeout() * 2), TimeUnit.MILLISECONDS).connectTimeout((long) ItemObject.getTimeout(), TimeUnit.MILLISECONDS).writeTimeout((long) (ItemObject.getTimeout() * 2), TimeUnit.MILLISECONDS).hostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            }).retryOnConnectionFailure(true).build();
            Request.Builder requestBuilder = new Request.Builder().url(ItemObject.mObjectURI);
            if (ItemObject.getNeedAuthenticate()) {
                requestBuilder.header(AUTH.WWW_AUTH_RESP, Credentials.basic(MyiBaseApplication.getCommonVariables().UserInfo.szUserName, MyiBaseApplication.getCommonVariables().Session.getPassword(), Charset.forName("UTF8")));
            }
            Response response = client.newCall(requestBuilder.build()).execute();
            int nResponseCode = response.code();
            if (!response.isSuccessful()) {
                setItemObjectActivityBusy(OneObject, false);
                OneObject.mReturnMessage = "服务器端返回" + response.message() + "(" + String.valueOf(nResponseCode) + "代码)";
                OneObject.callCallbacks(false, nResponseCode);
            } else if (HttpItemObject.getSaveToFile()) {
                int nTotalLength = (int) response.body().contentLength();
                InputStream inputStream = response.body().byteStream();
                boolean bLengthOK = true;
                if (nTotalLength == -1 || HttpItemObject.getSizeLimit() == -1) {
                    bLengthOK = true;
                } else if (nTotalLength > HttpItemObject.getSizeLimit()) {
                    bLengthOK = false;
                }
                if (bLengthOK) {
                    File file = new File(HttpItemObject.getTargetFileName());
                    try {
                        byte[] Buffer = new byte[nBufferSize];
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
                            OneObject.writeTextData(HttpItemObject.getTargetFileName());
                            setItemObjectActivityBusy(OneObject, false);
                            OneObject.callCallbacks(true, nResponseCode);
                            OutputStream = OutputStream2;
                            outputFile = file;
                        } else {
                            file.delete();
                            setItemObjectActivityBusy(OneObject, false);
                            OneObject.mReturnMessage = "文件获取不完整，完整长度" + String.valueOf(nTotalLength) + "，只获得了" + String.valueOf(nCurrentPos) + "字节。";
                            OneObject.callCallbacks(false, ErrorCode.ERROR_FILE_DOWNLOADINCOMPLETE);
                            OutputStream = OutputStream2;
                            outputFile = file;
                        }
                    } catch (IOException e3) {
                        e = e3;
                        outputFile = file;
                        Log.e(TAG, "IOException while process" + HttpItemObject.toString());
                        e.printStackTrace();
                        if (HttpItemObject.getSaveToFile()) {
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
                        }
                        setItemObjectActivityBusy(OneObject, false);
                        OneObject.mReturnMessage = e.getMessage();
                        OneObject.callCallbacks(false, -100);
                        return false;
                    }
                }
                inputStream.close();
                setItemObjectActivityBusy(OneObject, false);
                OneObject.mReturnMessage = "文件大小超过限制。";
                OneObject.callCallbacks(false, ErrorCode.ERROR_FILE_DOWNLOADSIZEEXCEED);
            } else {
                if (response.body().contentType() == null) {
                    OneObject.writeTextData(new String(response.body().bytes(), ItemObject.getEncoding()));
                } else if (response.body().contentType().charset() != null) {
                    OneObject.writeTextData(response.body().string());
                } else {
                    OneObject.writeTextData(new String(response.body().bytes(), ItemObject.getEncoding()));
                }
                setItemObjectActivityBusy(OneObject, false);
                OneObject.callCallbacks(true, nResponseCode);
                return true;
            }
        } catch (IOException e4) {
            e = e4;
            Log.e(TAG, "IOException while process" + HttpItemObject.toString());
            e.printStackTrace();
            if (HttpItemObject.getSaveToFile()) {
                if (OutputStream != null) {
                    OutputStream.close();
                }
                if (outputFile != null) {
                    outputFile.delete();
                }
            }
            setItemObjectActivityBusy(OneObject, false);
            OneObject.mReturnMessage = e.getMessage();
            OneObject.callCallbacks(false, -100);
            return false;
        }
        return false;
    }

    public boolean handlePackageWrite(ItemObject OneObject) {
        setItemObjectActivityBusy(OneObject, true);
        int nTryCount = OneObject.getRetryCount();
        boolean bResult = false;
        while (nTryCount > 0) {
            try {
                int nLength;
                final HttpItemObject ItemObject = (HttpItemObject) OneObject;
                OkHttpClient client = this.mClient.newBuilder().readTimeout((long) (ItemObject.getTimeout() * 2), TimeUnit.MILLISECONDS).connectTimeout((long) ItemObject.getTimeout(), TimeUnit.MILLISECONDS).writeTimeout((long) (ItemObject.getTimeout() * 2), TimeUnit.MILLISECONDS).hostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }).retryOnConnectionFailure(true).build();
                final int nBufferSize = ItemObject.getBufferSize();
                if (ItemObject.getSourceFileName() == null || ItemObject.getSourceFileName().isEmpty()) {
                    nLength = ItemObject.mTextContent.getBytes(ItemObject.getEncoding()).length;
                } else {
                    nLength = (int) new File(ItemObject.getSourceFileName()).length();
                }
                final int nTotalUploadLength = nLength;
                RequestBody requestBody = new RequestBody() {
                    public MediaType contentType() {
                        return MediaType.parse(ItemObject.getContentType());
                    }

                    public void writeTo(BufferedSink sink) throws IOException {
                        if (ItemObject.getSourceFileName() == null || ItemObject.getSourceFileName().isEmpty()) {
                            sink.write(ItemObject.mTextContent.getBytes(ItemObject.getEncoding()));
                            return;
                        }
                        InputStream in = new FileInputStream(ItemObject.getSourceFileName());
                        byte[] buf = new byte[nBufferSize];
                        while (true) {
                            int len = in.read(buf);
                            if (len <= 0) {
                                in.close();
                                return;
                            }
                            sink.write(buf, 0, len);
                        }
                    }

                    public long contentLength() throws IOException {
                        return (long) nTotalUploadLength;
                    }
                };
                if (ItemObject.getEnableRandomWait()) {
                    Utilities.randomWait(2000);
                }
                Request.Builder requestBuilder = new Request.Builder().url(ItemObject.mObjectURI).post(requestBody);
                if (ItemObject.getNeedAuthenticate()) {
                    requestBuilder.header(AUTH.WWW_AUTH_RESP, Credentials.basic(MyiBaseApplication.getCommonVariables().UserInfo.szUserName, MyiBaseApplication.getCommonVariables().Session.getPassword()));
                }
                Response response = client.newCall(requestBuilder.build()).execute();
                if (response.isSuccessful()) {
                    setItemObjectActivityBusy(OneObject, false);
                    if (response.body().contentType() == null) {
                        OneObject.writeTextData(new String(response.body().bytes(), ItemObject.getEncoding()));
                    } else if (response.body().contentType().charset() != null) {
                        OneObject.writeTextData(response.body().string());
                    } else {
                        OneObject.writeTextData(new String(response.body().bytes(), ItemObject.getEncoding()));
                    }
                    OneObject.callCallbacks(true, response.code());
                    return true;
                } else if (nTryCount <= 1) {
                    setItemObjectActivityBusy(OneObject, false);
                    OneObject.mReturnMessage = response.message();
                    OneObject.callCallbacks(false, response.code());
                    return false;
                } else {
                    continue;
                    nTryCount--;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                setItemObjectActivityBusy(OneObject, false);
                if (e.getMessage() != null) {
                    OneObject.mReturnMessage = e.getMessage();
                } else {
                    OneObject.mReturnMessage = "文件未找到";
                }
                OneObject.callCallbacks(false, 2);
                return false;
            } catch (IOException e2) {
                e2.printStackTrace();
                bResult = false;
                if (nTryCount <= 1) {
                    setItemObjectActivityBusy(OneObject, false);
                    if (e2.getMessage() != null) {
                        OneObject.mReturnMessage = e2.getMessage();
                    } else {
                        OneObject.mReturnMessage = "IO异常";
                    }
                    OneObject.callCallbacks(false, -100);
                }
            }
        }
        return bResult;
    }
}
