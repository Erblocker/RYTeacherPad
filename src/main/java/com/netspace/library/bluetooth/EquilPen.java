package com.netspace.library.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.bluetooth.BlueToothPen.PenDeviceInterface;
import com.pnf.bt.lib.PNFPenController;
import com.pnf.bt.lib.PenDataClass;

public class EquilPen implements PenDeviceInterface {
    public static final int A4 = 1;
    public static final int A5 = 2;
    public static final int Auto = 5;
    public static final int B5 = 3;
    public static final int B6 = 4;
    public static final int BFT3x5 = 13;
    public static final int BFT3x6 = 14;
    public static final int BFT4x6 = 15;
    public static final int CALIBRATION_MODE_CUSTOM = 0;
    public static final int CALIBRATION_MODE_SM_BOTTOM = 6;
    public static final int CALIBRATION_MODE_SM_LEFT = 4;
    public static final int CALIBRATION_MODE_SM_RIGHT = 5;
    public static final int CALIBRATION_MODE_SM_TOP = 3;
    public static final int CALIBRATION_MODE_SP = 1;
    public static final int CALIBRATION_MODE_SP2 = 2;
    public static final int Custom = 5;
    public static final int FT6x4 = 6;
    public static final int FT6x5 = 7;
    public static final int FT8x4 = 8;
    public static final int FT8x5 = 9;
    public static final int Letter = 0;
    public static final String TAG = "EquilPen";
    public static final int TFT3x5 = 10;
    public static final int TFT3x6 = 11;
    public static final int TFT4x6 = 12;
    public static final RectF caliSM_BOTTOM_3X5 = new RectF(46612.0f, 53961.0f, 53788.0f, 65662.0f);
    public static final RectF caliSM_BOTTOM_3X6 = new RectF(46612.0f, 51800.0f, 53788.0f, 65566.0f);
    public static final RectF caliSM_BOTTOM_4X6 = new RectF(45338.0f, 51900.0f, 55089.0f, 65666.0f);
    public static final RectF caliSM_LEFT_6X4 = new RectF(1728.0f, 45372.0f, 15524.0f, 54824.0f);
    public static final RectF caliSM_LEFT_6X5 = new RectF(1830.0f, 44156.0f, 15506.0f, 56034.0f);
    public static final RectF caliSM_LEFT_8X4 = new RectF(1868.0f, 45377.0f, 20153.0f, 54735.0f);
    public static final RectF caliSM_LEFT_8X5 = new RectF(1810.0f, 44163.0f, 20164.0f, 55938.0f);
    public static final RectF caliSM_TOP_3X5 = new RectF(12790.0f, 1547.0f, 19966.0f, 13248.0f);
    public static final RectF caliSM_TOP_3X6 = new RectF(12790.0f, 1532.0f, 19966.0f, 15298.0f);
    public static final RectF caliSM_TOP_4X6 = new RectF(11551.0f, 1532.0f, 21303.0f, 15298.0f);
    public static final RectF caliSP_A4 = new RectF(1768.0f, 563.0f, 5392.0f, 5160.0f);
    public static final RectF caliSP_A5 = new RectF(2341.0f, 542.0f, 4865.0f, 3631.0f);
    public static final RectF caliSP_B5 = new RectF(2027.0f, 561.0f, 5183.0f, 4462.0f);
    public static final RectF caliSP_B6 = new RectF(2500.0f, 544.0f, 4704.0f, 3154.0f);
    public static final RectF caliSP_LETTER = new RectF(1737.0f, 541.0f, 5445.0f, 4818.0f);
    public static int calibrationMode = 0;
    public static int iDisGetHeight = 0;
    public static int iDisGetWidth = 0;
    public static final Point sizeSM_3X5 = new Point(914, 1524);
    public static final Point sizeSM_3X6 = new Point(914, 1828);
    public static final Point sizeSM_4X6 = new Point(1219, 1828);
    public static final Point sizeSM_6X4 = new Point(1828, 1219);
    public static final Point sizeSM_6X5 = new Point(1828, 1524);
    public static final Point sizeSM_8X4 = new Point(2438, 1219);
    public static final Point sizeSM_8X5 = new Point(2438, 1524);
    public static final Point sizeSP_A4 = new Point(210, 297);
    public static final Point sizeSP_A5 = new Point(148, 210);
    public static final Point sizeSP_B5 = new Point(176, 250);
    public static final Point sizeSP_B6 = new Point(125, 175);
    public static final Point sizeSP_LETTER = new Point(216, 279);
    private PenDataThread mDataThread = null;
    private Runnable mPenConnectCheckRunnable = new Runnable() {
        public void run() {
            if (!EquilPen.this.mbConnected) {
                Log.i(EquilPen.TAG, "Pen is not really connect. stopPen.");
                EquilPen.this.mStopPenRunnable.run();
            }
        }
    };
    private PNFPenController mPenController = null;
    private Handler mPenHandlerFunc = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                int batteryStation = msg.arg1;
                int batteryPen = msg.arg2;
                Log.d(EquilPen.TAG, "Battery: station: " + batteryStation + ", pen: " + batteryPen);
                if (!EquilPen.this.mbPenBatteryPrompted && batteryStation != 100 && batteryPen != 100) {
                    Toast.makeText(MyiBaseApplication.getBaseAppContext(), "基站电量：" + batteryStation + "%，手写笔电量：" + batteryPen + "%", 0).show();
                    EquilPen.this.mbPenBatteryPrompted = true;
                }
            } else if (msg.what == 2) {
                BlueToothPen.reportDataToCall("newpage", 0, 0, 0.0f);
            }
        }
    };
    private Handler mPenMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 5) {
                EquilPen.this.mbConnected = true;
                EquilPen.this.mbPenStarted = true;
                Toast.makeText(MyiBaseApplication.getBaseAppContext(), "已连接到Equil手写笔", 0).show();
            } else if (msg.what == 3) {
                if (EquilPen.this.mbConnected) {
                    EquilPen.this.mbConnected = false;
                    Toast.makeText(MyiBaseApplication.getBaseAppContext(), "Equil手写笔连接失败", 0).show();
                }
            } else if (msg.what == 2) {
                if (EquilPen.this.mDataThread != null) {
                    EquilPen.this.mDataThread.stopThread();
                }
                if (EquilPen.this.mPenController != null) {
                    EquilPen.this.mPenController.EndReadQ();
                }
                if (EquilPen.this.mbConnected) {
                    Toast.makeText(MyiBaseApplication.getBaseAppContext(), "Equil手写笔连接已中断", 0).show();
                }
                EquilPen.this.mbPenBatteryPrompted = false;
            } else if (msg.what == 7) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        EquilPen.this.lazyCheckCalibration();
                        if (EquilPen.this.mDataThread == null) {
                            EquilPen.this.mDataThread = new PenDataThread();
                            EquilPen.this.mDataThread.start();
                            EquilPen.this.mPenController.StartReadQ();
                        }
                    }
                }, 1500);
            }
        }
    };
    private Runnable mStopPenRunnable = new Runnable() {
        public void run() {
            if (EquilPen.this.mDataThread != null) {
                EquilPen.this.mDataThread.stopThread();
            }
            if (EquilPen.this.mPenController != null) {
                EquilPen.this.mPenController.EndReadQ();
            }
            if (EquilPen.this.mbPenStarted) {
                Log.d(EquilPen.TAG, "stopPen.");
                EquilPen.this.mPenController.stopPen();
                EquilPen.this.mbPenStarted = false;
                EquilPen.this.mPenController.disconnectPen();
                EquilPen.this.mbConnected = false;
                Log.d(EquilPen.TAG, "Pen stopped.");
                EquilPen.this.mbPenBatteryPrompted = false;
                if (EquilPen.this.mbConnected) {
                    Toast.makeText(MyiBaseApplication.getBaseAppContext(), "已关闭Equil手写笔连接", 0).show();
                }
            }
        }
    };
    private boolean mbConnected = false;
    private boolean mbPenBatteryPrompted = false;
    private boolean mbPenStarted = false;
    private boolean mbStart = false;
    BroadcastReceiver mkeyGuardReceiver = new BroadcastReceiver() {
        private boolean mbDisconnectByOurSelf = false;

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.SCREEN_ON")) {
                if (EquilPen.this.mPenController != null) {
                    Log.i(EquilPen.TAG, "ScreenOn, mbDisconnectByOurSelf=" + this.mbDisconnectByOurSelf);
                    if (this.mbDisconnectByOurSelf) {
                        EquilPen.this.mPenController.SetRetObjForFunc(EquilPen.this.mPenHandlerFunc);
                        this.mbDisconnectByOurSelf = false;
                    }
                }
            } else if (action.equals("android.intent.action.SCREEN_OFF") && EquilPen.this.mPenController != null) {
                Log.i(EquilPen.TAG, "ScreenOff, mbPenStarted=" + EquilPen.this.mbPenStarted);
                if (EquilPen.this.mbPenStarted) {
                    EquilPen.this.mPenController.disconnectPen();
                    EquilPen.this.mbPenStarted = false;
                    this.mbDisconnectByOurSelf = true;
                    EquilPen.this.mbPenBatteryPrompted = false;
                }
            }
        }
    };

    private class PenDataThread extends Thread {
        private boolean mbStop;

        private PenDataThread() {
            this.mbStop = false;
        }

        public void stopThread() {
            this.mbStop = true;
            try {
                join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            while (!this.mbStop) {
                try {
                    synchronized (EquilPen.this.mPenController) {
                        PenDataClass penData = EquilPen.this.mPenController.ReadQ();
                        if (penData != null) {
                            PointF penConvPos = EquilPen.this.mPenController.getCoordinatePostionXY(penData.ptRaw.x, penData.ptRaw.y, penData.bRight);
                            float fPressure = ((float) penData.pressure) / 450.0f;
                            if (EquilPen.this.mbStart) {
                                switch (penData.PenStatus) {
                                    case 1:
                                        BlueToothPen.reportDataToCall("write", (int) penConvPos.x, (int) penConvPos.y, fPressure);
                                        break;
                                    case 2:
                                        BlueToothPen.reportDataToCall("write", (int) penConvPos.x, (int) penConvPos.y, fPressure);
                                        break;
                                    case 3:
                                        BlueToothPen.reportDataToCall("reset", (int) penConvPos.x, (int) penConvPos.y, fPressure);
                                        break;
                                    case 4:
                                        BlueToothPen.reportDataToCall("move", (int) penConvPos.x, (int) penConvPos.y, fPressure);
                                        break;
                                    case 5:
                                        BlueToothPen.reportDataToCall("move", (int) penConvPos.x, (int) penConvPos.y, fPressure);
                                        break;
                                    case 6:
                                        BlueToothPen.reportDataToCall("move", (int) penConvPos.x, (int) penConvPos.y, fPressure);
                                        break;
                                }
                            }
                            EquilPen.this.mPenController.removeQ();
                        }
                        Thread.sleep(10);
                    }
                } catch (Exception e) {
                }
            }
            EquilPen.this.mDataThread = null;
        }
    }

    public void init(BlueToothPen BlueToochPen) {
        IntentFilter filter = new IntentFilter("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        MyiBaseApplication.getBaseAppContext().registerReceiver(this.mkeyGuardReceiver, filter);
        Point LCDSize = new Point();
        ((WindowManager) MyiBaseApplication.getBaseAppContext().getSystemService("window")).getDefaultDisplay().getSize(LCDSize);
        if (LCDSize.x > LCDSize.y) {
            iDisGetWidth = LCDSize.x;
            iDisGetHeight = LCDSize.y;
            return;
        }
        iDisGetWidth = LCDSize.y;
        iDisGetHeight = LCDSize.x;
    }

    public boolean isConnected() {
        return true;
    }

    public boolean isStarted() {
        return this.mbStart;
    }

    public boolean start() {
        this.mbStart = true;
        if (this.mPenController == null) {
            this.mPenController = new PNFPenController(MyiBaseApplication.getBaseAppContext());
            this.mPenController.setConnectDelay(false);
            this.mPenController.setCalibration(MyiBaseApplication.getBaseAppContext());
            this.mPenController.SetRetObjForMsg(this.mPenMessageHandler);
        }
        this.mPenMessageHandler.removeCallbacks(this.mStopPenRunnable);
        if (!this.mbPenStarted) {
            this.mPenController.startPen();
            this.mPenController.SetRetObjForFunc(this.mPenHandlerFunc);
            this.mbPenStarted = true;
            this.mPenMessageHandler.postDelayed(this.mPenConnectCheckRunnable, 4000);
        }
        return true;
    }

    public void stop() {
        this.mPenMessageHandler.removeCallbacks(this.mStopPenRunnable);
        if (this.mbPenStarted) {
            this.mPenMessageHandler.postDelayed(this.mStopPenRunnable, 3000);
        }
        this.mbStart = false;
    }

    void lazyCheckCalibration() {
        if (this.mPenController != null) {
            Log.d(TAG, "lazyCheckCalibration");
            PointF[] calScreenPoint = new PointF[4];
            PointF[] calResultPoint = new PointF[4];
            if (this.mPenController.getModelCode() < 4) {
                calResultPoint[0] = new PointF(caliSP_A4.left, caliSP_A4.top);
                calResultPoint[1] = new PointF(caliSP_A4.right, caliSP_A4.top);
                calResultPoint[2] = new PointF(caliSP_A4.right, caliSP_A4.bottom);
                calResultPoint[3] = new PointF(caliSP_A4.left, caliSP_A4.bottom);
            } else {
                int stationPostion = this.mPenController.getStationPostion();
                if (stationPostion == 1) {
                    calResultPoint[0] = new PointF(caliSM_TOP_4X6.left, caliSM_TOP_4X6.top);
                    calResultPoint[1] = new PointF(caliSM_TOP_4X6.right, caliSM_TOP_4X6.top);
                    calResultPoint[2] = new PointF(caliSM_TOP_4X6.right, caliSM_TOP_4X6.bottom);
                    calResultPoint[3] = new PointF(caliSM_TOP_4X6.left, caliSM_TOP_4X6.bottom);
                } else if (stationPostion == 4) {
                    calResultPoint[0] = new PointF(caliSM_BOTTOM_4X6.left, caliSM_BOTTOM_4X6.top);
                    calResultPoint[1] = new PointF(caliSM_BOTTOM_4X6.right, caliSM_BOTTOM_4X6.top);
                    calResultPoint[2] = new PointF(caliSM_BOTTOM_4X6.right, caliSM_BOTTOM_4X6.bottom);
                    calResultPoint[3] = new PointF(caliSM_BOTTOM_4X6.left, caliSM_BOTTOM_4X6.bottom);
                } else {
                    calResultPoint[0] = new PointF(caliSM_LEFT_8X5.left, caliSM_LEFT_8X5.top);
                    calResultPoint[1] = new PointF(caliSM_LEFT_8X5.right, caliSM_LEFT_8X5.top);
                    calResultPoint[2] = new PointF(caliSM_LEFT_8X5.right, caliSM_LEFT_8X5.bottom);
                    calResultPoint[3] = new PointF(caliSM_LEFT_8X5.left, caliSM_LEFT_8X5.bottom);
                }
            }
            calScreenPoint[0] = new PointF(0.0f, 0.0f);
            calScreenPoint[1] = new PointF((float) iDisGetWidth, 0.0f);
            calScreenPoint[2] = new PointF((float) iDisGetWidth, (float) (iDisGetHeight * 2));
            calScreenPoint[3] = new PointF(0.0f, (float) (iDisGetHeight * 2));
            this.mPenController.setCalibrationData(calScreenPoint, 0, calResultPoint);
        }
    }
}
