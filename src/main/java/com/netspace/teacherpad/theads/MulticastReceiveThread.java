package com.netspace.teacherpad.theads;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.support.v4.internal.view.SupportMenu;
import android.util.Log;
import com.netspace.library.struct.UDPDataPackage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MulticastReceiveThread extends Thread {
    private Context mContext;
    private UDPDataPackage mDataPackage = new UDPDataPackage();
    private MulticastLock mcLock;
    private int mnPort = 0;
    private String mszAddress = "";

    public MulticastReceiveThread(Context context, String szAddress, int nPort) {
        this.mContext = context;
        WifiManager wifi = (WifiManager) context.getSystemService("wifi");
        if (wifi != null) {
            this.mcLock = wifi.createMulticastLock("multicastlockReceiveThread");
            this.mcLock.setReferenceCounted(true);
            this.mcLock.acquire();
        }
        this.mszAddress = szAddress;
        this.mnPort = nPort;
    }

    public void run() {
        UnknownHostException e;
        IOException e2;
        InetAddress ia = null;
        byte[] buffer = new byte[SupportMenu.USER_MASK];
        MulticastSocket multicastSocket = null;
        try {
            ia = InetAddress.getByName(this.mszAddress);
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length, ia, this.mnPort);
            MulticastSocket ms = new MulticastSocket(this.mnPort);
            try {
                ms.joinGroup(ia);
                for (int i = 0; i < 20; i++) {
                    byte[] test = "192.168.1.105".getBytes();
                    ms.send(new DatagramPacket(test, test.length, ia, this.mnPort));
                }
                while (true) {
                    ms.receive(dp);
                    if (dp.getLength() > 0) {
                        Log.e("Multicast", "Receive data. Length=" + String.valueOf(dp.getLength()));
                    }
                }
            } catch (UnknownHostException e3) {
                e = e3;
                multicastSocket = ms;
            } catch (IOException e4) {
                e2 = e4;
                multicastSocket = ms;
            }
        } catch (UnknownHostException e5) {
            e = e5;
            e.printStackTrace();
            try {
                multicastSocket.leaveGroup(ia);
            } catch (IOException e22) {
                e22.printStackTrace();
            }
        } catch (IOException e6) {
            e22 = e6;
            e22.printStackTrace();
            multicastSocket.leaveGroup(ia);
        }
    }
}
