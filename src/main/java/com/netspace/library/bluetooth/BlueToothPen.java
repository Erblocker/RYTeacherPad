package com.netspace.library.bluetooth;

import java.util.ArrayList;
import java.util.Iterator;

public class BlueToothPen {
    private static final String TAG = "BlueToothPen";
    private static ArrayList<PenActionInterface> marrCallbacks = new ArrayList();
    private static ArrayList<PenDeviceInterface> marrPenDevices = new ArrayList();
    private PenActionInterface mCallBack;

    public interface PenActionInterface {
        void onPenAction(String str, int i, int i2, float f);

        void onPenConnected();

        void onPenDisconnected();
    }

    public interface PenDeviceInterface {
        void init(BlueToothPen blueToothPen);

        boolean isConnected();

        boolean isStarted();

        boolean start();

        void stop();
    }

    public BlueToothPen() {
        if (marrPenDevices.size() == 0) {
            marrPenDevices.add(new EquilPen());
            Iterator it = marrPenDevices.iterator();
            while (it.hasNext()) {
                ((PenDeviceInterface) it.next()).init(this);
            }
        }
    }

    public void setCallBack(PenActionInterface callBack) {
        this.mCallBack = callBack;
    }

    public boolean isBlueToothPenConnected() {
        boolean bConnected = false;
        synchronized (marrPenDevices) {
            Iterator it = marrPenDevices.iterator();
            while (it.hasNext()) {
                bConnected |= ((PenDeviceInterface) it.next()).isConnected();
            }
        }
        return bConnected;
    }

    public boolean start() {
        if (this.mCallBack == null) {
            throw new NullPointerException("Callback must be set before start.");
        }
        synchronized (marrCallbacks) {
            int i;
            boolean bFound = false;
            for (i = 0; i < marrCallbacks.size(); i++) {
                if (((PenActionInterface) marrCallbacks.get(i)).equals(this.mCallBack)) {
                    bFound = true;
                    break;
                }
            }
            if (!bFound) {
                marrCallbacks.add(this.mCallBack);
            }
        }
        boolean bConnected = false;
        synchronized (marrPenDevices) {
            Iterator it = marrPenDevices.iterator();
            while (it.hasNext()) {
                PenDeviceInterface onePen = (PenDeviceInterface) it.next();
                if (onePen.isConnected()) {
                    bConnected |= onePen.start();
                }
            }
        }
        if (bConnected) {
            synchronized (marrCallbacks) {
                for (i = 0; i < marrCallbacks.size(); i++) {
                    ((PenActionInterface) marrCallbacks.get(i)).onPenConnected();
                }
            }
        }
        return bConnected;
    }

    public void stop() {
        synchronized (marrCallbacks) {
            int i;
            for (i = 0; i < marrCallbacks.size(); i++) {
                if (((PenActionInterface) marrCallbacks.get(i)).equals(this.mCallBack)) {
                    marrCallbacks.remove(i);
                }
            }
            int nCount = marrCallbacks.size();
        }
        if (nCount == 0) {
            synchronized (marrPenDevices) {
                Iterator it = marrPenDevices.iterator();
                while (it.hasNext()) {
                    PenDeviceInterface onePen = (PenDeviceInterface) it.next();
                    if (onePen.isConnected()) {
                        onePen.stop();
                    }
                }
            }
            synchronized (marrCallbacks) {
                for (i = 0; i < marrCallbacks.size(); i++) {
                    ((PenActionInterface) marrCallbacks.get(i)).onPenDisconnected();
                }
            }
        }
    }

    protected static void reportDataToCall(String szAction, int X, int Y, float fPressure) {
        synchronized (marrCallbacks) {
            Iterator it = marrCallbacks.iterator();
            while (it.hasNext()) {
                ((PenActionInterface) it.next()).onPenAction(szAction, X, Y, fPressure);
            }
        }
    }
}
