package com.netspace.library.threads;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.protocol.HTTP;

public class LocalFileUploader extends Thread {
    private byte[] mBuffer;
    private int mBufferSize = 524288;
    private FileUploaderListener mCallBack;
    private boolean mbProgressUpload = false;
    private boolean mbResult = false;
    private String mszLocalFileName;
    private String mszResponse = "";
    private String mszTargetURL;

    public interface FileUploaderListener {
        void onFileUploadProgress(LocalFileUploader localFileUploader, int i, int i2);

        void onFileUploadedFail(LocalFileUploader localFileUploader);

        void onFileUploadedSuccess(LocalFileUploader localFileUploader);
    }

    public LocalFileUploader(String szURL, String szLocalFileName, boolean bProgressUpload, FileUploaderListener CallBack) {
        this.mszTargetURL = szURL;
        this.mszLocalFileName = szLocalFileName;
        this.mbProgressUpload = bProgressUpload;
        this.mCallBack = CallBack;
        this.mBuffer = new byte[this.mBufferSize];
    }

    public boolean getResult() {
        return this.mbResult;
    }

    public String getResponse() {
        return this.mszResponse;
    }

    public String getLocalFileName() {
        return this.mszLocalFileName;
    }

    public void run() {
        setName("LocalFileUploader");
        String szURL = this.mszTargetURL;
        int nFilePos = 0;
        boolean bHasDataToUpload = true;
        while (bHasDataToUpload) {
            try {
                szURL = this.mszTargetURL;
                if (this.mbProgressUpload) {
                    szURL = new StringBuilder(String.valueOf(szURL)).append("&offset=").append(nFilePos).toString();
                    if (nFilePos != 0) {
                        szURL = new StringBuilder(String.valueOf(szURL)).append("&append=1").toString();
                    } else {
                        szURL = new StringBuilder(String.valueOf(szURL)).append("&append=0").toString();
                    }
                }
                HttpURLConnection connection = (HttpURLConnection) new URL(szURL).openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod(HttpPost.METHOD_NAME);
                connection.setRequestProperty(HTTP.CONTENT_TYPE, "application/octet-stream");
                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                int len;
                if (this.mbProgressUpload) {
                    RandomAccessFile in = new RandomAccessFile(this.mszLocalFileName, "r");
                    in.seek((long) nFilePos);
                    if (this.mCallBack != null) {
                        this.mCallBack.onFileUploadProgress(this, nFilePos, (int) in.length());
                    }
                    len = in.read(this.mBuffer);
                    if (len > 0) {
                        outputStream.write(this.mBuffer, 0, len);
                        if (len < this.mBuffer.length) {
                            bHasDataToUpload = false;
                        }
                    }
                    nFilePos += len;
                    in.close();
                } else {
                    InputStream in2 = new FileInputStream(this.mszLocalFileName);
                    while (true) {
                        len = in2.read(this.mBuffer);
                        if (len <= 0) {
                            break;
                        }
                        outputStream.write(this.mBuffer, 0, len);
                    }
                    in2.close();
                    bHasDataToUpload = false;
                }
                outputStream.flush();
                outputStream.close();
                int serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();
                InputStream input = new BufferedInputStream(connection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(input, "GBK"));
                StringBuilder str = new StringBuilder();
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    str.append(new StringBuilder(String.valueOf(line)).append("\r\n").toString());
                }
                input.close();
                this.mszResponse = str.toString();
                connection.disconnect();
                if (serverResponseCode < 200 || serverResponseCode > 299) {
                    this.mbResult = false;
                    this.mszResponse = "服务器端返回" + serverResponseMessage;
                    if (this.mCallBack != null) {
                        this.mCallBack.onFileUploadedFail(this);
                        break;
                    }
                } else {
                    this.mbResult = true;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                this.mbResult = false;
                this.mszResponse = e.getMessage();
                if (this.mCallBack != null) {
                    this.mCallBack.onFileUploadedFail(this);
                }
            } catch (IOException e2) {
                e2.printStackTrace();
                this.mbResult = false;
                this.mszResponse = e2.getMessage();
                if (this.mCallBack != null) {
                    this.mCallBack.onFileUploadedFail(this);
                }
            }
        }
        if (this.mbResult && this.mCallBack != null) {
            this.mCallBack.onFileUploadedSuccess(this);
        }
    }

    public String getErrorText() {
        return this.mszResponse;
    }
}
