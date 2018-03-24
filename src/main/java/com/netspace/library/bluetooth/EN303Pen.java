package com.netspace.library.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.Toast;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.bluetooth.BlueToothPen.PenDeviceInterface;
import com.netspace.library.consts.Const;
import com.netspace.library.utilities.Utilities;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class EN303Pen implements PenDeviceInterface {
    private static final String TAG = "EN303Pen";
    private static DecodeDataThread mDecodeDataThread;
    private static BluetoothSocket mSocket = null;
    private static BluetoothDevice mTargetDevice;

    public class DecodeDataThread extends Thread {
        private BufferedInputStream mStream = null;
        private boolean mbStop = false;

        public DecodeDataThread(InputStream in) {
            this.mStream = new BufferedInputStream(in);
        }

        public void run() {
            boolean bError = false;
            while (true) {
                try {
                    int nIndex = this.mStream.read();
                    if (nIndex == -1) {
                        break;
                    } else if (this.mbStop) {
                        break;
                    } else if (nIndex == 128) {
                        int nextByte = this.mStream.read();
                        if (nextByte == -1) {
                            break;
                        } else if (nextByte == 128 || nextByte == 129 || nextByte == 136) {
                            String szAction = "";
                            if (nextByte == 128) {
                                szAction = "reset";
                            } else if (nextByte == 129) {
                                szAction = "write";
                            } else if (nextByte == 136) {
                                szAction = "move";
                            }
                            long[] nData = new long[4];
                            for (int i = 0; i < 4; i++) {
                                nData[i] = (long) this.mStream.read();
                            }
                            short x = (short) ((int) (nData[0] | (nData[1] << 8)));
                            short y = (short) ((int) (nData[2] | (nData[3] << 8)));
                            if (szAction.equalsIgnoreCase("reset")) {
                                BlueToothPen.reportDataToCall("reset", 0, 0, 1.0f);
                            } else {
                                BlueToothPen.reportDataToCall(szAction, (x + Const.BLUETOOTH_PEN_LEFT) / 6, (y + Const.BLUETOOTH_PEN_TOP) / 6, 1.0f);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(EN303Pen.TAG, "connect closed.");
                    if (!this.mbStop) {
                        bError = true;
                    }
                }
            }
            if (bError) {
                Utilities.runOnUIThread(MyiBaseApplication.getBaseAppContext(), new Runnable() {
                    public void run() {
                        Toast.makeText(MyiBaseApplication.getBaseAppContext(), "和蓝牙手写笔的连接异常断开", 0).show();
                    }
                });
            }
            EN303Pen.mDecodeDataThread = null;
        }

        public void stopThread() {
            this.mbStop = true;
        }
    }

    public void init(BlueToothPen BlueToochPen) {
    }

    public boolean isConnected() {
        if (mTargetDevice != null) {
            return true;
        }
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            return false;
        }
        if (!adapter.isEnabled()) {
            return false;
        }
        for (BluetoothDevice device : adapter.getBondedDevices()) {
            Log.d(TAG, device.getName() + "," + device.getType() + "," + device.getAddress());
            if (device.getName().equalsIgnoreCase("Mobile Pen")) {
                mTargetDevice = device;
                break;
            }
        }
        if (mTargetDevice == null) {
            return false;
        }
        return true;
    }

    public boolean start() {
        if (mDecodeDataThread != null) {
            return false;
        }
        try {
            mSocket = mTargetDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mSocket.connect();
            mDecodeDataThread = new DecodeDataThread(mSocket.getInputStream());
            mDecodeDataThread.start();
            Toast.makeText(MyiBaseApplication.getBaseAppContext(), "蓝牙手写笔已连接", 0).show();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        return true;
    }

    public void stop() {
        if (mDecodeDataThread != null) {
            mDecodeDataThread.stopThread();
            if (mSocket != null) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mSocket = null;
            }
            mDecodeDataThread = null;
            Toast.makeText(MyiBaseApplication.getBaseAppContext(), "已断开与蓝牙手写笔的连接", 0).show();
        }
    }

    public boolean isStarted() {
        if (mDecodeDataThread != null) {
            return true;
        }
        return false;
    }
}
