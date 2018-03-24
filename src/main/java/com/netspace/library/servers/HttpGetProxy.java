package com.netspace.library.servers;

import android.util.Log;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

public class HttpGetProxy {
    private String TAG = "HttpGetProxy";
    private boolean mCanReadDataBlock = true;
    private int mContentLength = 0;
    private DataInputStream mLocalInputStream;
    private DataOutputStream mLocalOutputStream;
    private Socket mLocalSocket;
    private boolean mOneBlockReadComplete = false;
    private DataInputStream mRemoteInputStream;
    private DataOutputStream mRemoteOutputStream;
    private Socket mRemoteSocket;
    private int mRequireReadSize = 0;
    private ServerSocket mServerSocket;
    private URL mSourceURL;
    private int mTotalReadSize = 0;
    private String mURL;
    private WorkThread mWorkThread;
    private ArrayList<HttpGetProxy> marrClientProxy = new ArrayList();
    private boolean mbMaster = false;
    private volatile boolean mbWorking = true;

    private class WorkThread extends Thread {
        private WorkThread() {
        }

        public void run() {
            HttpGetProxy httpGetProxy;
            if (HttpGetProxy.this.marrClientProxy.size() > 0) {
                setName("HttpGetProxy WorkThread Master");
                httpGetProxy = HttpGetProxy.this;
                httpGetProxy.TAG = httpGetProxy.TAG + "Master";
                HttpGetProxy.this.mbMaster = true;
            } else {
                setName("HttpGetProxy WorkThread Client");
                httpGetProxy = HttpGetProxy.this;
                httpGetProxy.TAG = httpGetProxy.TAG + "Client";
                HttpGetProxy.this.mbMaster = false;
            }
            while (HttpGetProxy.this.mbWorking) {
                try {
                    int nFindPos;
                    HttpGetProxy.this.mLocalSocket = HttpGetProxy.this.mServerSocket.accept();
                    HttpGetProxy.this.mTotalReadSize = 0;
                    HttpGetProxy.this.mContentLength = 0;
                    HttpGetProxy.this.mLocalOutputStream = new DataOutputStream(HttpGetProxy.this.mLocalSocket.getOutputStream());
                    HttpGetProxy.this.mLocalInputStream = new DataInputStream(HttpGetProxy.this.mLocalSocket.getInputStream());
                    HttpGetProxy.this.mRemoteSocket = new Socket(HttpGetProxy.this.mSourceURL.getHost(), HttpGetProxy.this.mSourceURL.getPort());
                    HttpGetProxy.this.mRemoteOutputStream = new DataOutputStream(HttpGetProxy.this.mRemoteSocket.getOutputStream());
                    HttpGetProxy.this.mRemoteInputStream = new DataInputStream(HttpGetProxy.this.mRemoteSocket.getInputStream());
                    String szHeadData = "";
                    boolean bHeadFinish = false;
                    byte[] buffer = new byte[4096];
                    while (!bHeadFinish) {
                        Arrays.fill(buffer, (byte) 0);
                        if (HttpGetProxy.this.mLocalInputStream.read(buffer) == -1) {
                            break;
                        }
                        szHeadData = new StringBuilder(String.valueOf(szHeadData)).append(new String(buffer, "GB2312")).toString();
                        nFindPos = szHeadData.indexOf("\r\n\r\n");
                        if (nFindPos != -1) {
                            bHeadFinish = true;
                            szHeadData = szHeadData.substring(0, nFindPos + 4);
                        }
                    }
                    nFindPos = szHeadData.indexOf("Host: ");
                    String szMidData = "";
                    if (nFindPos != -1) {
                        szHeadData = szHeadData.replace(szHeadData.substring(nFindPos, szHeadData.indexOf("\r\n", nFindPos)), "Host: " + HttpGetProxy.this.mSourceURL.getHost() + ":" + String.valueOf(HttpGetProxy.this.mSourceURL.getPort()));
                    }
                    Log.d(HttpGetProxy.this.TAG, "HeadData:\n" + szHeadData + "\nComplete.");
                    HttpGetProxy.this.mRemoteOutputStream.write(szHeadData.getBytes("GB2312"));
                    byte[] Databuffer = new byte[40960];
                    int nBufferSize = Databuffer.length;
                    while (HttpGetProxy.this.mbWorking) {
                        if (HttpGetProxy.this.mCanReadDataBlock) {
                            int nReadSize = HttpGetProxy.this.mRemoteInputStream.read(Databuffer, 0, nBufferSize);
                            if (nReadSize == -1) {
                                break;
                            }
                            if (HttpGetProxy.this.mContentLength == 0) {
                                szHeadData = new String(Databuffer, "GBK");
                                nFindPos = szHeadData.indexOf("Content-Length: ");
                                if (nFindPos != -1) {
                                    HttpGetProxy.this.mContentLength = Integer.valueOf(szHeadData.substring(nFindPos + 16, szHeadData.indexOf("\r\n", nFindPos))).intValue();
                                    Log.d(HttpGetProxy.this.TAG, "Content-Length " + HttpGetProxy.this.mContentLength);
                                }
                            }
                            httpGetProxy = HttpGetProxy.this;
                            httpGetProxy.mTotalReadSize = httpGetProxy.mTotalReadSize + nReadSize;
                            try {
                                HttpGetProxy.this.mLocalOutputStream.write(Databuffer, 0, nReadSize);
                            } catch (IOException e) {
                            } catch (UnknownHostException e2) {
                                e2.printStackTrace();
                            }
                        } else {
                            try {
                                sleep(10);
                            } catch (InterruptedException e3) {
                                e3.printStackTrace();
                            }
                        }
                    }
                    HttpGetProxy.this.mRemoteOutputStream.close();
                    HttpGetProxy.this.mRemoteInputStream.close();
                    HttpGetProxy.this.mRemoteSocket.close();
                    HttpGetProxy.this.mLocalOutputStream.close();
                    HttpGetProxy.this.mLocalInputStream.close();
                    HttpGetProxy.this.mLocalSocket.close();
                } catch (UnknownHostException e22) {
                    e22.printStackTrace();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
        }
    }

    public boolean initProxy(String szURL) {
        this.mURL = szURL;
        try {
            this.mSourceURL = new URL(szURL);
            try {
                this.mServerSocket = new ServerSocket(0);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } catch (MalformedURLException e2) {
            e2.printStackTrace();
            return false;
        }
    }

    public int getTotalReadSize() {
        return this.mTotalReadSize;
    }

    public boolean getDataBlockRead() {
        boolean bResult = this.mOneBlockReadComplete;
        this.mOneBlockReadComplete = false;
        return bResult;
    }

    public void setPause(boolean bPause) {
        this.mCanReadDataBlock = !bPause;
    }

    public void setCanReadDataBlock(boolean bCan, int nRequireDataSize) {
        this.mCanReadDataBlock = bCan;
        this.mRequireReadSize = nRequireDataSize;
    }

    public int getContentLength() {
        return this.mContentLength;
    }

    public void addNeighbour(HttpGetProxy Proxy) {
        this.marrClientProxy.add(Proxy);
    }

    public String getLocalAddress() {
        return this.mURL;
    }

    public void start() {
        this.mWorkThread = new WorkThread();
        this.mWorkThread.start();
    }

    public void stop() {
        this.mbWorking = false;
        try {
            this.mServerSocket.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            this.mWorkThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
