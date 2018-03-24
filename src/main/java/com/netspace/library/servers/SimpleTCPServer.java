package com.netspace.library.servers;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.netspace.library.restful.provider.device.DeviceOperationRESTServiceProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class SimpleTCPServer extends Thread {
    private static final String TAG = "SimpleTCPServer";
    private TCPServerCallBack mCallBack;
    private Runnable mCleanClosedClientsRunnable = new Runnable() {
        public void run() {
            synchronized (SimpleTCPServer.this.marrClients) {
                int i = 0;
                while (i < SimpleTCPServer.this.marrClients.size()) {
                    if (!((ClientServerThread) SimpleTCPServer.this.marrClients.get(i)).isAlive()) {
                        SimpleTCPServer.this.marrClients.remove(i);
                        i--;
                    }
                    i++;
                }
            }
            SimpleTCPServer.this.mHandler.postDelayed(this, 5000);
        }
    };
    private Context mContext;
    private Handler mHandler;
    private ServerSocket mServerSocket;
    private ArrayList<ClientServerThread> marrClients = new ArrayList();
    private volatile boolean mbStop = false;
    private int mnPort;

    public class ClientServerThread extends Thread {
        private InputStream mClientInputStream;
        private OutputStream mClientOutputStream;
        private Socket mClientSocket;
        private boolean mClosed = false;
        private boolean mbStopThread = false;
        private String mszAddr;

        public ClientServerThread(Socket socket) {
            try {
                this.mClientSocket = socket;
                this.mClientOutputStream = this.mClientSocket.getOutputStream();
                this.mClientInputStream = this.mClientSocket.getInputStream();
                this.mszAddr = this.mClientSocket.getInetAddress().toString();
            } catch (IOException e) {
                this.mClosed = true;
            }
        }

        public boolean sendData(byte[] data, int nPos, int nLength) {
            if (this.mClientOutputStream != null) {
                try {
                    this.mClientOutputStream.write(data, nPos, nLength);
                    return true;
                } catch (IOException e) {
                }
            }
            return false;
        }

        public void stopThread() {
            this.mbStopThread = true;
            if (!this.mClosed) {
                try {
                    this.mClientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                join();
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
        }

        public void run() {
            setName("SimpleTCPServer - ClientThread for " + this.mszAddr);
            byte[] buffer = new byte[4096];
            byte[] tickBuffer = new byte[1];
            byte[] tickResponseBuffer = new byte[]{(byte) -1};
            tickResponseBuffer[0] = (byte) -2;
            try {
                this.mClientSocket.setSoTimeout(DeviceOperationRESTServiceProvider.TIMEOUT);
                this.mClientSocket.setKeepAlive(true);
                while (!SimpleTCPServer.this.mbStop && !this.mbStopThread) {
                    try {
                        int nBytesRead = this.mClientInputStream.read(buffer);
                        if (nBytesRead > 0) {
                            boolean bFoundVaildData = true;
                            if (nBytesRead == 1) {
                                if (buffer[0] == (byte) -1) {
                                    this.mClientOutputStream.write(tickResponseBuffer);
                                    bFoundVaildData = false;
                                } else if (buffer[0] == (byte) -2) {
                                    bFoundVaildData = false;
                                }
                            }
                            if (!(!bFoundVaildData || SimpleTCPServer.this.mCallBack == null || SimpleTCPServer.this.mCallBack.onDataReceived(this, buffer, nBytesRead))) {
                                break;
                            }
                        } else if (nBytesRead == -1) {
                            break;
                        }
                    } catch (SocketTimeoutException e) {
                        this.mClientOutputStream.write(tickBuffer);
                    }
                }
                this.mClientSocket.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            Log.i(SimpleTCPServer.TAG, "Close connection for " + this.mszAddr);
            this.mClosed = true;
        }
    }

    public interface TCPServerCallBack {
        boolean onDataReceived(ClientServerThread clientServerThread, byte[] bArr, int i);
    }

    public SimpleTCPServer(Context context, int nPort) {
        this.mContext = context;
        this.mnPort = nPort;
        this.mHandler = new Handler(context.getMainLooper());
    }

    public void setCallBack(TCPServerCallBack callback) {
        this.mCallBack = callback;
    }

    public void stopThread() {
        if (!this.mbStop) {
            this.mbStop = true;
            this.mHandler.removeCallbacks(this.mCleanClosedClientsRunnable);
            try {
                this.mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            synchronized (this.marrClients) {
                for (int i = 0; i < this.marrClients.size(); i++) {
                    ((ClientServerThread) this.marrClients.get(i)).stopThread();
                }
                this.marrClients.clear();
            }
        }
    }

    public void run() {
        setName("SimpleTCPServer - MasterListenThread");
        this.mHandler.postDelayed(this.mCleanClosedClientsRunnable, 5000);
        try {
            this.mServerSocket = new ServerSocket(this.mnPort);
            while (!this.mbStop) {
                Socket socket = this.mServerSocket.accept();
                Log.i(TAG, "New connection to :" + socket.getInetAddress());
                synchronized (this.marrClients) {
                    ClientServerThread newThread = new ClientServerThread(socket);
                    newThread.start();
                    this.marrClients.add(newThread);
                }
            }
            this.mServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
