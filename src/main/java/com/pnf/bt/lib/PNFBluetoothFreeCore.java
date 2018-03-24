package com.pnf.bt.lib;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Handler;
import android.support.v4.internal.view.SupportMenu;
import android.support.v4.media.TransportMediator;
import android.support.v4.view.InputDeviceCompat;
import android.util.Log;
import com.netspace.library.consts.Const;
import com.netspace.library.restful.provider.device.DeviceOperationRESTServiceProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import net.sqlcipher.database.SQLiteDatabase;
import org.kxml2.wap.Wbxml;

public class PNFBluetoothFreeCore {
    final int ADJUST_L = 2255;
    final int ADJUST_R = 2255;
    TimerTask BTCloseCheckTask = null;
    Timer BTCloseCheckTimer = null;
    TimerTask BTConnectCheckTask = null;
    Timer BTConnectCheckTimer = null;
    String DeleteFileName = "";
    ArrayList<byte[]> DeleteList = new ArrayList();
    int InsertCnt = 0;
    short Len_L = (short) 0;
    short Len_Lb = (short) 0;
    short Len_R = (short) 0;
    short Len_Rb = (short) 0;
    int MV_threshold = 5;
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    int MakerPenStatus = 0;
    PenDataClass OldpenData = new PenDataClass();
    byte[] OpenFileByte = null;
    final int PACKET_DIV_CNT = 2;
    int PEN_DAGERZONE_WIDTH = 1800;
    final String PNFBluetoothDeviceSharedPre = "PNFBluetoothDevic";
    int PenAliveSec = 0;
    short PenIr = (short) 0;
    int PenOldStatus = 0;
    int PenStatus = 0;
    short PenUs = (short) 0;
    private final int READ_MODE_MESSAGE = 1;
    private final int READ_MODE_QUE = 2;
    final int SENSOR_DISTANCE = 785;
    private final int SMARTMARKER_MCU2VERSION = 5;
    public int SwichCheckCount = 0;
    int X_MAX_RANGE_IGNORE = (this.X_MAX_RANGE_WARN + this.X_OFFSET_IGNORE);
    int X_MAX_RANGE_WARN = 20740;
    int X_MIN_RANGE_IGNORE = (this.X_MIN_RANGE_WARN - this.X_OFFSET_IGNORE);
    int X_MIN_RANGE_WARN = 1580;
    int X_OFFSET_IGNORE = 1000;
    int Y_MAX_RANGE_IGNORE = (this.Y_MAX_RANGE_WARN + this.Y_OFFSET_IGNORE);
    int Y_MAX_RANGE_WARN = 22250;
    int Y_MIN_RANGE_IGNORE = (this.Y_MIN_RANGE_WARN - this.Y_OFFSET_IGNORE);
    int Y_MIN_RANGE_WARN = 10280;
    int Y_OFFSET_IGNORE = 1000;
    final float Y_RANGE = 6500.0f;
    final float Y_RANGE2 = 66535.0f;
    byte[] _readData;
    int _totalBytesRead;
    byte[] _writeData;
    float adjust_val_l;
    float adjust_val_r;
    float angle0 = 0.0f;
    float angle1 = 0.0f;
    boolean bCanDi = false;
    boolean bClicked = false;
    boolean bDeleteDIFile = false;
    boolean bDeviceDataCheck;
    boolean bErrFile = false;
    boolean bFirstData = true;
    boolean bLiveChangePage = false;
    boolean bReCommand = false;
    private boolean bStopped = false;
    public boolean bSwitchedReceiver = false;
    public boolean bUseAcc = true;
    ByteArrayInputStream bais = null;
    ByteArrayOutputStream baos = null;
    int btCloseCount = 0;
    final int btCloseLimit = 30;
    int clickCnt = 0;
    Timer dbCkTimer = null;
    double dbuf = 0.0d;
    int delta_move = 0;
    int delta_move_b = 0;
    boolean di_bFirst = true;
    boolean di_bOn;
    int di_battery_pen;
    int di_battery_station;
    int di_choice_file;
    int di_choice_folder;
    ArrayList<String> di_debug_str;
    boolean di_disk_err_check;
    int di_down_file_size;
    int di_err_cnt;
    int di_figure_count;
    byte[] di_file_data;
    ArrayList<String> di_file_dates;
    int di_freespace;
    int di_paper_size;
    PointF di_point_max;
    PointF di_point_min;
    int di_readbytes;
    byte[] di_showdate;
    boolean di_start_ask;
    int di_state;
    int errCnt = 0;
    boolean forceUp = false;
    public PNFHardwareData hardwareData = null;
    long iNowWakeTime = 0;
    long iPreWakeTime = 0;
    boolean isAlive = true;
    boolean isAppBackground = false;
    public boolean isCloudConnect = false;
    public boolean isForceConnect = false;
    boolean isGesture = false;
    public boolean isLetterPaper = false;
    boolean isPenUPData = true;
    boolean isSetDiStart = false;
    boolean isT2Mode = false;
    double l_sqr = 0.0d;
    int lastQueMakerPenStatus = 0;
    int lastQuePenPressure = 0;
    PointF lastQuePenPt = new PointF();
    boolean lastQuePenRight = true;
    int lastQuePenState = 3;
    ArrayList<PenDataClass> livepenlist = new ArrayList();
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice = null;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    public Context mContext = null;
    int mDiState = 0;
    public Handler mMessageHandler = null;
    int mMode;
    public Handler mPenDIHandler = null;
    public Handler mPenEnvHandler = null;
    public Handler mPenFuncHandler = null;
    public byte mSMPenFlag = (byte) 0;
    public byte mSMPenFlagOld = (byte) -1;
    public int mSMPenState = 0;
    public int mSMPenStateOld = 0;
    public int mSMPenSwitch = -1;
    public int mState;
    public List<PenDataClass> m_ReadQue = null;
    ArrayList<Point> m_ptGestureList = new ArrayList();
    int pairedCount = 0;
    Stack<BluetoothDeviceData> pairedDevices = new Stack();
    TimerTask penUPTask = null;
    Timer penUPTimer = null;
    int pen_cnt = 0;
    Point ptRaw = new Point();
    public int readMode = 1;
    int rmd = 0;
    int rmdCount = 0;
    int rmd_const = 600;
    long saveSessionTime = 0;
    float sd2 = 1570.0f;
    float sd_sqr = 616225.0f;
    float sensor_dist;
    int sessionStartCnt = 0;
    boolean socketConnected = false;
    boolean specialKeyDown = false;
    int temperature = 0;
    int up_cnt = 0;
    double xbuf = 0.0d;
    int xsqr = 0;
    int xxx = 0;
    int xxx0 = 0;
    int xxx1 = 0;
    double ybuf = 0.0d;
    int ysqr = 0;
    int yyy = 0;
    int yyy0 = 0;
    int yyy1 = 0;

    public class ConnectThread extends Thread {
        private BluetoothDevice mmDevice = null;
        private BluetoothSocket mmSocket = null;

        public ConnectThread(BluetoothDevice device) {
            this.mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                tmp = this.mmDevice.createInsecureRfcommSocketToServiceRecord(PNFBluetoothFreeCore.this.MY_UUID);
            } catch (IOException e) {
                Log.e("error", "PNFBluetoothFreeCore ConnectThread Exception : " + e);
            }
            this.mmSocket = tmp;
        }

        public void run() {
            setName("ConnectThread");
            try {
                if (this.mmSocket == null) {
                    PNFBluetoothFreeCore.this.connectionFailed();
                    return;
                }
                PNFBluetoothFreeCore.this.mBluetoothAdapter.cancelDiscovery();
                this.mmSocket.connect();
                PNFBluetoothFreeCore.this.connected(this.mmSocket, this.mmDevice);
                PNFBluetoothFreeCore.this.socketConnected = this.mmSocket.isConnected();
            } catch (IOException e) {
                PNFBluetoothFreeCore.this.connectionFailed();
            }
        }

        public void cancel() {
            try {
                if (this.mmSocket != null) {
                    this.mmSocket.close();
                }
                PNFBluetoothFreeCore.this.socketConnected = false;
            } catch (Exception e) {
                Log.e("error", "PNFBluetoothFreeCore cancel Exception : " + e);
            }
        }
    }

    public class ConnectedThread extends Thread {
        int ErrCnt = 0;
        boolean bReciveFilePacket = false;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final BluetoothSocket mmSocket;

        public ConnectedThread(BluetoothSocket socket) {
            this.mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = this.mmSocket.getInputStream();
                tmpOut = this.mmSocket.getOutputStream();
            } catch (IOException e) {
                Log.e("error", "PNFBluetoothFreeCore ConnectedThread Exception : " + e);
            }
            this.mmInStream = tmpIn;
            this.mmOutStream = tmpOut;
        }

        public void run() {
            byte[] readByte = new byte[16];
            PNFBluetoothFreeCore.this.baos = new ByteArrayOutputStream();
            while (true) {
                try {
                    if (this.mmInStream != null && this.mmInStream.available() > 0) {
                        byte[] buffer = new byte[this.mmInStream.available()];
                        this.mmInStream.read(buffer, 0, buffer.length);
                        PNFBluetoothFreeCore.this.baos.write(buffer);
                    }
                    PNFBluetoothFreeCore.this.bais = new ByteArrayInputStream(PNFBluetoothFreeCore.this.baos.toByteArray());
                    if (PNFBluetoothFreeCore.this.bais.available() > 0) {
                        PNFBluetoothFreeCore.this.btCloseCount = 0;
                    }
                    if (PNFBluetoothFreeCore.this.bais.available() >= 16) {
                        if (PNFBluetoothFreeCore.this.bStopped) {
                            continue;
                        } else {
                            int i;
                            if (PNFBluetoothFreeCore.this.bFirstData) {
                                int baisCnt = PNFBluetoothFreeCore.this.bais.available();
                                PNFBluetoothFreeCore.this.bDeviceDataCheck = false;
                                i = 0;
                                while (i < baisCnt) {
                                    byte[] tempbyte = new byte[1];
                                    PNFBluetoothFreeCore.this.bais.read(tempbyte, 0, 1);
                                    readByte[0] = readByte[1];
                                    readByte[1] = readByte[2];
                                    readByte[2] = readByte[3];
                                    readByte[3] = readByte[4];
                                    readByte[4] = readByte[5];
                                    readByte[5] = readByte[6];
                                    readByte[6] = readByte[7];
                                    readByte[7] = readByte[8];
                                    readByte[8] = readByte[9];
                                    readByte[9] = readByte[10];
                                    readByte[10] = readByte[11];
                                    readByte[11] = readByte[12];
                                    readByte[12] = readByte[13];
                                    readByte[13] = readByte[14];
                                    readByte[14] = readByte[15];
                                    readByte[15] = tempbyte[0];
                                    if ((readByte[14] & 255) != 255 || (readByte[15] & 255) != 255) {
                                        if (readByte[0] != (byte) 0 || readByte[0] != (byte) 0 || readByte[2] != (byte) 0 || readByte[3] != (byte) 0 || readByte[4] != (byte) 0 || readByte[5] != (byte) 0 || readByte[6] != (byte) 0 || readByte[7] != (byte) 0 || readByte[8] != (byte) 0 || readByte[9] != (byte) 0 || readByte[10] != (byte) 0 || readByte[11] != (byte) 0 || readByte[12] != (byte) 0 || readByte[13] != (byte) 0 || (readByte[14] & 255) != 255 || (readByte[15] & 255) != 255) {
                                            i++;
                                        } else if (0 + 1 > 10) {
                                            PNFBluetoothFreeCore.this.PNFMessageHandler(8);
                                        }
                                    }
                                }
                            } else {
                                readByte = new byte[16];
                                PNFBluetoothFreeCore.this.bais.read(readByte, 0, 16);
                            }
                            byte[] tempByte;
                            if ((readByte[14] & 255) == 255 && (readByte[15] & 255) == 255) {
                                if (!PNFBluetoothFreeCore.this.setDIData(readByte)) {
                                    PNFBluetoothFreeCore pNFBluetoothFreeCore;
                                    if ((PNFBluetoothFreeCore.this.hardwareData.modelCode < 4 && (readByte[2] & Wbxml.EXT_0) == 192) || (PNFBluetoothFreeCore.this.hardwareData.modelCode == 4 && (readByte[2] & Wbxml.EXT_0) == 192 && readByte[11] == (byte) 0 && readByte[12] == (byte) 0)) {
                                        PNFBluetoothFreeCore.this.PenIr = PNFBluetoothFreeCore.this.byteToShort(readByte[1], readByte[2] & 63);
                                        PNFBluetoothFreeCore.this.PenUs = PNFBluetoothFreeCore.this.byteToShort(readByte[3], readByte[4] & 63);
                                        int batSt;
                                        switch (PNFBluetoothFreeCore.this.hardwareData.modelCode) {
                                            case 3:
                                                if ((readByte[6] & 128) > 0) {
                                                    PNFBluetoothFreeCore.this.PenAliveSec = PNFBluetoothFreeCore.this.byteToShort(readByte[5], readByte[6] & 7);
                                                }
                                                batSt = (int) ((((float) (PNFBluetoothFreeCore.this.byteToShort(readByte[7], readByte[8]) - 1192)) / 254.0f) * 100.0f);
                                                if (batSt > 0) {
                                                    pNFBluetoothFreeCore = PNFBluetoothFreeCore.this;
                                                    if (batSt > 100) {
                                                        batSt = 100;
                                                    }
                                                    pNFBluetoothFreeCore.di_battery_station = batSt;
                                                }
                                                int batPt = (int) ((((float) (PNFBluetoothFreeCore.this.byteToShort(readByte[9], readByte[10]) - 1032)) / 227.0f) * 100.0f);
                                                if (batPt > 0) {
                                                    pNFBluetoothFreeCore = PNFBluetoothFreeCore.this;
                                                    if (batPt > 100) {
                                                        batPt = 100;
                                                    }
                                                    pNFBluetoothFreeCore.di_battery_pen = batPt;
                                                }
                                                PNFBluetoothFreeCore.this.PNFFuncHandler(1, PNFBluetoothFreeCore.this.di_battery_station, PNFBluetoothFreeCore.this.di_battery_pen);
                                                if (PNFBluetoothFreeCore.this.di_bFirst) {
                                                    PNFBluetoothFreeCore.this.di_bFirst = false;
                                                    PNFBluetoothFreeCore.this.setDIState(7);
                                                    break;
                                                }
                                                break;
                                            case 4:
                                                PNFBluetoothFreeCore.this.PenAliveSec = 600;
                                                batSt = (int) ((((float) (PNFBluetoothFreeCore.this.byteToShort(readByte[9], readByte[10]) - 1216)) / 250.0f) * 100.0f);
                                                if (batSt > 0) {
                                                    pNFBluetoothFreeCore = PNFBluetoothFreeCore.this;
                                                    if (batSt > 100) {
                                                        batSt = 100;
                                                    }
                                                    pNFBluetoothFreeCore.di_battery_station = batSt;
                                                }
                                                PNFBluetoothFreeCore.this.di_battery_pen = readByte[11];
                                                int x = PNFBluetoothFreeCore.this.byteToShort(readByte[3], readByte[4]);
                                                int y = PNFBluetoothFreeCore.this.byteToShort(readByte[5], readByte[6]);
                                                int z = PNFBluetoothFreeCore.this.byteToShort(readByte[7], readByte[8]);
                                                PNFBluetoothFreeCore.this.hardwareData.stationPosition = PNFBluetoothFreeCore.this.GetSmartMakerStationPosition(x, y, z);
                                                PNFBluetoothFreeCore.this.PNFFuncHandler(1, PNFBluetoothFreeCore.this.di_battery_station, PNFBluetoothFreeCore.this.di_battery_pen);
                                                if (PNFBluetoothFreeCore.this.hardwareData.oldStationPosition == -1) {
                                                    PNFBluetoothFreeCore.this.PNFFuncHandler(5);
                                                } else {
                                                    if (PNFBluetoothFreeCore.this.hardwareData.oldStationPosition != PNFBluetoothFreeCore.this.hardwareData.stationPosition) {
                                                        PNFBluetoothFreeCore.this.PNFFuncHandler(4);
                                                    }
                                                }
                                                PNFBluetoothFreeCore.this.hardwareData.oldStationPosition = PNFBluetoothFreeCore.this.hardwareData.stationPosition;
                                                if (PNFBluetoothFreeCore.this.di_bFirst) {
                                                    PNFBluetoothFreeCore.this.di_bFirst = false;
                                                    PNFBluetoothFreeCore.this.setDIState(7);
                                                    break;
                                                }
                                                break;
                                            default:
                                                PNFBluetoothFreeCore.this.PenIr = PNFBluetoothFreeCore.this.byteToShort(readByte[1], readByte[2] & 63);
                                                PNFBluetoothFreeCore.this.PenUs = PNFBluetoothFreeCore.this.byteToShort(readByte[3], readByte[4] & 63);
                                                if ((readByte[6] & 128) > 0) {
                                                    PNFBluetoothFreeCore.this.PenAliveSec = PNFBluetoothFreeCore.this.byteToShort(readByte[5], readByte[6] & 7);
                                                }
                                                PNFBluetoothFreeCore.this.PNFEnvHandler(PNFBluetoothFreeCore.this.PenIr, PNFBluetoothFreeCore.this.PenUs, PNFBluetoothFreeCore.this.PenAliveSec);
                                                break;
                                        }
                                    } else if (readByte[2] == Byte.MAX_VALUE && readByte[3] == (byte) -1) {
                                        PNFBluetoothFreeCore.this.isAlive = false;
                                    } else if (readByte[2] == Byte.MAX_VALUE && readByte[3] == (byte) -49) {
                                        PNFBluetoothFreeCore.this.PNFFuncHandler(2);
                                    } else if (readByte[2] == Byte.MAX_VALUE && readByte[3] == (byte) -33) {
                                        PNFBluetoothFreeCore.this.PNFFuncHandler(3);
                                    } else if (readByte[2] != Byte.MAX_VALUE || readByte[3] < (byte) 2) {
                                        PNFBluetoothFreeCore.this._readData(readByte);
                                        if (PNFBluetoothFreeCore.this.di_bFirst && PNFBluetoothFreeCore.this.hardwareData.modelCode >= 3 && PNFBluetoothFreeCore.this.bCanDi) {
                                            PNFBluetoothFreeCore.this.di_bFirst = false;
                                            PNFBluetoothFreeCore.this.setDIState(7);
                                        }
                                    } else {
                                        PNFBluetoothFreeCore.this.hardwareData.modelCode = readByte[3];
                                        PNFBluetoothFreeCore.this.sensor_dist = (float) PNFBluetoothFreeCore.this.byteToShort(readByte[5], readByte[6]);
                                        PNFBluetoothFreeCore.this.sd_sqr = PNFBluetoothFreeCore.this.sensor_dist * PNFBluetoothFreeCore.this.sensor_dist;
                                        PNFBluetoothFreeCore.this.sd2 = PNFBluetoothFreeCore.this.sensor_dist * 2.0f;
                                        PNFBluetoothFreeCore.this.adjust_val_l = (float) PNFBluetoothFreeCore.this.byteToShort(readByte[7], readByte[8]);
                                        PNFBluetoothFreeCore.this.adjust_val_r = PNFBluetoothFreeCore.this.adjust_val_l;
                                        if (readByte[4] == (byte) 16 || readByte[4] == (byte) 32) {
                                            PNFBluetoothFreeCore.this.bCanDi = false;
                                        } else {
                                            PNFBluetoothFreeCore.this.bCanDi = true;
                                        }
                                        PNFBluetoothFreeCore.this.hardwareData.mcu1Code = readByte[10];
                                        PNFBluetoothFreeCore.this.hardwareData.mcu2Code = readByte[11];
                                        PNFBluetoothFreeCore.this.hardwareData.hwVersion = readByte[12];
                                        PNFBluetoothFreeCore.this.temperature = readByte[13] & 63;
                                        if (PNFBluetoothFreeCore.this.hardwareData.modelCode == 4) {
                                            PNFBluetoothFreeCore.this.hardwareData.audioMode = readByte[7];
                                            PNFBluetoothFreeCore.this.hardwareData.audioVolum = readByte[8];
                                        }
                                        pNFBluetoothFreeCore = PNFBluetoothFreeCore.this;
                                        int i2 = pNFBluetoothFreeCore.sessionStartCnt + 1;
                                        pNFBluetoothFreeCore.sessionStartCnt = i2;
                                        if (i2 > 3) {
                                            PNFBluetoothFreeCore.this.sessionStartCnt = 0;
                                            PNFBluetoothFreeCore.this.appSessionStart();
                                            PNFBluetoothFreeCore.this.bais.close();
                                            PNFBluetoothFreeCore.this.bais = null;
                                            PNFBluetoothFreeCore.this.settingMSGFirstData();
                                            PNFBluetoothFreeCore.this.PNFMessageHandler(7);
                                            PNFBluetoothFreeCore.this.initDI();
                                            PNFBluetoothFreeCore.this.bFirstData = false;
                                        }
                                    }
                                }
                                PNFBluetoothFreeCore.this.baos.reset();
                                if (PNFBluetoothFreeCore.this.bais != null) {
                                    if (PNFBluetoothFreeCore.this.bais.available() > 0) {
                                        tempByte = new byte[PNFBluetoothFreeCore.this.bais.available()];
                                        PNFBluetoothFreeCore.this.bais.read(tempByte, 0, tempByte.length);
                                        PNFBluetoothFreeCore.this.baos.write(tempByte);
                                        PNFBluetoothFreeCore.this.bais.skip((long) PNFBluetoothFreeCore.this.bais.available());
                                    }
                                    PNFBluetoothFreeCore.this.bais.close();
                                    PNFBluetoothFreeCore.this.bais = null;
                                }
                            } else {
                                PNFBluetoothFreeCore.this.bais = new ByteArrayInputStream(PNFBluetoothFreeCore.this.baos.toByteArray());
                                if (PNFBluetoothFreeCore.this.bais.available() + PNFBluetoothFreeCore.this.InsertCnt >= 4098) {
                                    this.ErrCnt = 0;
                                    readByte = new byte[(4098 - PNFBluetoothFreeCore.this.InsertCnt)];
                                    PNFBluetoothFreeCore.this.bais.read(readByte, 0, 4098 - PNFBluetoothFreeCore.this.InsertCnt);
                                    if (!(readByte[readByte.length - 2] == (byte) -1 || readByte[readByte.length - 1] == (byte) -1)) {
                                        this.bReciveFilePacket = true;
                                    }
                                    PNFBluetoothFreeCore.this.setDIData(readByte);
                                    PNFBluetoothFreeCore.this.baos.reset();
                                    if (PNFBluetoothFreeCore.this.bais != null) {
                                        if (PNFBluetoothFreeCore.this.bais.available() > 0) {
                                            tempByte = new byte[PNFBluetoothFreeCore.this.bais.available()];
                                            PNFBluetoothFreeCore.this.bais.read(tempByte, 0, tempByte.length);
                                            PNFBluetoothFreeCore.this.baos.write(tempByte);
                                            PNFBluetoothFreeCore.this.bais.skip((long) PNFBluetoothFreeCore.this.bais.available());
                                        }
                                        PNFBluetoothFreeCore.this.bais.close();
                                        PNFBluetoothFreeCore.this.bais = null;
                                    }
                                }
                                if (this.bReciveFilePacket && PNFBluetoothFreeCore.this.bais != null) {
                                    int i3 = this.ErrCnt + 1;
                                    this.ErrCnt = i3;
                                    if (i3 > 3) {
                                        Object tempByte2;
                                        this.ErrCnt = 0;
                                        this.bReciveFilePacket = false;
                                        Object readByte2 = new byte[PNFBluetoothFreeCore.this.bais.available()];
                                        PNFBluetoothFreeCore.this.bais.read(readByte2, 0, PNFBluetoothFreeCore.this.bais.available());
                                        int nskipCnt = 0;
                                        i = 0;
                                        while (i < readByte2.length - 2) {
                                            boolean bEOF = true;
                                            int j = 0;
                                            while (j < 2) {
                                                if (readByte2[i + j] != (byte) -1) {
                                                    bEOF = false;
                                                    if (bEOF) {
                                                        i++;
                                                    } else {
                                                        nskipCnt = i + 2;
                                                        tempByte2 = new byte[(readByte2.length - nskipCnt)];
                                                        System.arraycopy(tempByte2, 0, readByte2, nskipCnt, readByte2.length - nskipCnt);
                                                        System.arraycopy(new byte[nskipCnt], 0, readByte2, 0, nskipCnt);
                                                        PNFBluetoothFreeCore.this.baos.reset();
                                                        if (PNFBluetoothFreeCore.this.bais != null) {
                                                            if (PNFBluetoothFreeCore.this.bais.available() > 0) {
                                                                PNFBluetoothFreeCore.this.baos.write(tempByte2);
                                                                PNFBluetoothFreeCore.this.bais.skip((long) PNFBluetoothFreeCore.this.bais.available());
                                                            }
                                                            PNFBluetoothFreeCore.this.bais.close();
                                                            PNFBluetoothFreeCore.this.bais = null;
                                                        }
                                                    }
                                                } else {
                                                    j++;
                                                }
                                            }
                                            if (bEOF) {
                                                i++;
                                            } else {
                                                nskipCnt = i + 2;
                                                tempByte2 = new byte[(readByte2.length - nskipCnt)];
                                                System.arraycopy(tempByte2, 0, readByte2, nskipCnt, readByte2.length - nskipCnt);
                                                System.arraycopy(new byte[nskipCnt], 0, readByte2, 0, nskipCnt);
                                                PNFBluetoothFreeCore.this.baos.reset();
                                                if (PNFBluetoothFreeCore.this.bais != null) {
                                                    if (PNFBluetoothFreeCore.this.bais.available() > 0) {
                                                        PNFBluetoothFreeCore.this.baos.write(tempByte2);
                                                        PNFBluetoothFreeCore.this.bais.skip((long) PNFBluetoothFreeCore.this.bais.available());
                                                    }
                                                    PNFBluetoothFreeCore.this.bais.close();
                                                    PNFBluetoothFreeCore.this.bais = null;
                                                }
                                            }
                                        }
                                        tempByte2 = new byte[(readByte2.length - nskipCnt)];
                                        System.arraycopy(tempByte2, 0, readByte2, nskipCnt, readByte2.length - nskipCnt);
                                        System.arraycopy(new byte[nskipCnt], 0, readByte2, 0, nskipCnt);
                                        PNFBluetoothFreeCore.this.baos.reset();
                                        if (PNFBluetoothFreeCore.this.bais != null) {
                                            if (PNFBluetoothFreeCore.this.bais.available() > 0) {
                                                PNFBluetoothFreeCore.this.baos.write(tempByte2);
                                                PNFBluetoothFreeCore.this.bais.skip((long) PNFBluetoothFreeCore.this.bais.available());
                                            }
                                            PNFBluetoothFreeCore.this.bais.close();
                                            PNFBluetoothFreeCore.this.bais = null;
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("error", "ConnectedThread run Exception : " + e);
                    PNFBluetoothFreeCore.this.isAlive = false;
                }
                if (!PNFBluetoothFreeCore.this.isAlive) {
                    PNFBluetoothFreeCore.this.isT2Mode = true;
                    PNFBluetoothFreeCore.this.connectionLost();
                    return;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                if (this.mmOutStream != null) {
                    this.mmOutStream.write(buffer);
                    this.mmOutStream.flush();
                }
            } catch (IOException e) {
                Log.e("error", "PNFBluetoothFreeCore ConnectedThread write Exception : " + e);
            }
        }

        public void cancel() {
            try {
                if (this.mmSocket != null) {
                    this.mmSocket.close();
                }
            } catch (IOException e) {
                Log.e("error", "PNFBluetoothFreeCore ConnectedThread cancel Exception : " + e);
            }
        }
    }

    public PNFBluetoothFreeCore(Context con) {
        this.mContext = con;
        this.hardwareData = new PNFHardwareData(this.mContext);
        this.m_ReadQue = Collections.synchronizedList(new ArrayList());
    }

    public void setLetterPaper(boolean isLetter) {
        this.isLetterPaper = isLetter;
    }

    public void startPen() {
        this.bStopped = false;
        this.adjust_val_l = 2255.0f;
        this.adjust_val_r = 2255.0f;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        setBTConnectCheckCDT();
        setState(1);
        setMode(11);
    }

    public void stopPen() {
        this.bStopped = true;
    }

    public void restartPen() {
        this.bStopped = false;
    }

    public void StartReadQ() {
        this.readMode = 2;
    }

    public void EndReadQ() {
        this.readMode = 1;
        ClearQ();
    }

    public void removeQ() {
        this.m_ReadQue.remove(0);
    }

    public void ClearQ() {
        this.m_ReadQue.clear();
    }

    PenDataClass ReadQ() {
        if (this.m_ReadQue == null || this.m_ReadQue.size() <= 0) {
            return null;
        }
        return (PenDataClass) this.m_ReadQue.get(0);
    }

    void initPenUp() {
        this.forceUp = true;
    }

    public boolean isBluetoothEnabled() {
        return this.mBluetoothAdapter.isEnabled();
    }

    public void setDevice(BluetoothDevice device) {
        this.mBluetoothDevice = device;
    }

    private synchronized void setState(int state) {
        this.mState = state;
    }

    private synchronized void setMode(int mode) {
        this.mMode = mode;
    }

    public synchronized int getState() {
        return this.mState;
    }

    void startBTCloseTimer() {
        stopBTCloseTimer();
        this.btCloseCount = 0;
        this.BTCloseCheckTimer = new Timer();
        this.BTCloseCheckTask = new TimerTask() {
            public void run() {
                PNFBluetoothFreeCore pNFBluetoothFreeCore = PNFBluetoothFreeCore.this;
                pNFBluetoothFreeCore.btCloseCount++;
                if (PNFBluetoothFreeCore.this.isBluetoothConnected() && PNFBluetoothFreeCore.this.isAlive && PNFBluetoothFreeCore.this.btCloseCount > 30) {
                    PNFBluetoothFreeCore.this.isAlive = false;
                    PNFBluetoothFreeCore.this.stopBTCloseTimer();
                }
            }
        };
        this.BTCloseCheckTimer.schedule(this.BTCloseCheckTask, 5000, 1000);
    }

    void stopBTCloseTimer() {
        if (this.pairedDevices != null) {
            this.pairedDevices.clear();
        }
        if (this.BTCloseCheckTask != null) {
            this.BTCloseCheckTask.cancel();
            this.BTCloseCheckTask = null;
        }
        if (this.BTCloseCheckTimer != null) {
            this.BTCloseCheckTimer.cancel();
            this.BTCloseCheckTimer = null;
        }
    }

    void stopConnectTimer() {
        if (this.BTConnectCheckTask != null) {
            this.BTConnectCheckTask.cancel();
            this.BTConnectCheckTask = null;
        }
        if (this.BTConnectCheckTimer != null) {
            this.BTConnectCheckTimer.cancel();
            this.BTConnectCheckTimer = null;
        }
    }

    int setForceBondedDevices() {
        if (this.mBluetoothAdapter == null) {
            return 0;
        }
        if (this.pairedDevices == null) {
            return 0;
        }
        this.isForceConnect = true;
        this.pairedDevices.clear();
        ArrayList<BluetoothDeviceData> bondedDevices = new ArrayList();
        for (BluetoothDevice device : this.mBluetoothAdapter.getBondedDevices()) {
            if (device != null) {
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();
                if (!(deviceName == null || deviceName.isEmpty() || deviceName.length() <= 0 || deviceAddress == null || deviceAddress.isEmpty() || deviceAddress.length() <= 0 || !deviceName.contains("Equil"))) {
                    bondedDevices.add(new BluetoothDeviceData(deviceName, deviceAddress));
                }
            }
        }
        removeBluetoothDevice(bondedDevices);
        this.isForceConnect = false;
        return this.pairedDevices.size();
    }

    void setBTConnectCheckCDT() {
        if (this.mBluetoothAdapter != null && this.pairedDevices != null) {
            this.pairedDevices.clear();
            this.mBluetoothAdapter.cancelDiscovery();
            stopConnectTimer();
            this.BTConnectCheckTimer = new Timer();
            this.BTConnectCheckTask = new TimerTask() {
                public void run() {
                    if (!PNFBluetoothFreeCore.this.isBluetoothConnected() && !PNFBluetoothFreeCore.this.isForceConnect && !PNFBluetoothFreeCore.this.isAppBackground) {
                        if (PNFBluetoothFreeCore.this.isCloudConnect) {
                            PNFBluetoothFreeCore.this.BTConnect();
                            return;
                        }
                        if (PNFBluetoothFreeCore.this.pairedDevices.empty()) {
                            ArrayList<BluetoothDeviceData> bondedDevices = new ArrayList();
                            for (BluetoothDevice device : PNFBluetoothFreeCore.this.mBluetoothAdapter.getBondedDevices()) {
                                if (device != null) {
                                    String deviceName = device.getName();
                                    String deviceAddress = device.getAddress();
                                    if (!(deviceName == null || deviceName.isEmpty() || deviceName.length() <= 0 || deviceAddress == null || deviceAddress.isEmpty() || deviceAddress.length() <= 0 || !deviceName.contains("Equil"))) {
                                        bondedDevices.add(new BluetoothDeviceData(deviceName, deviceAddress));
                                    }
                                }
                            }
                            PNFBluetoothFreeCore.this.removeBluetoothDevice(bondedDevices);
                        }
                        PNFBluetoothFreeCore.this.BTConnect();
                    }
                }
            };
            this.BTConnectCheckTimer.schedule(this.BTConnectCheckTask, 1000, 5000);
        }
    }

    void BTConnect() {
        if (this.mConnectThread == null && this.pairedDevices.size() > 0) {
            BluetoothDevice device = this.mBluetoothAdapter.getRemoteDevice(((BluetoothDeviceData) this.pairedDevices.pop()).deviceAddress);
            if (device.getBondState() == 12) {
                setDevice(device);
                connect();
            }
        }
    }

    public boolean isBluetoothConnected() {
        if (this.mState == 4 || this.mState == 5) {
            return true;
        }
        return false;
    }

    public synchronized void connect() {
        this.isAlive = true;
        this.mConnectThread = new ConnectThread(this.mBluetoothDevice);
        this.mConnectThread.start();
        setState(4);
        setMode(9);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        this.bStopped = false;
        PNFDefine.isImportMsg = true;
        this.pairedDevices.clear();
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        this.mConnectedThread = new ConnectedThread(socket);
        this.mConnectedThread.start();
        PNFMessageHandler(5);
        setState(5);
        setMode(9);
        saveBluetoothDevice(device.getName(), device.getAddress());
    }

    public synchronized void stop() {
        this.socketConnected = false;
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
    }

    void saveBluetoothDevice(String deviceName, String deviceAddress) {
        if (deviceName.contains("Equil")) {
            Editor ePref = this.mContext.getSharedPreferences("PNFBluetoothDevic", 0).edit();
            ePref.putString("deviceName", deviceName);
            ePref.putString("deviceAddress", deviceAddress);
            ePref.commit();
        }
    }

    void loadBluetoothDevice() {
        if (this.pairedDevices.empty()) {
            SharedPreferences pref = this.mContext.getSharedPreferences("PNFBluetoothDevic", 0);
            String deviceName = pref.getString("deviceName", "");
            String deviceAddress = pref.getString("deviceAddress", "");
            if (deviceName != null && deviceName.contains("Equil")) {
                this.pairedDevices.push(new BluetoothDeviceData(deviceName, deviceAddress));
            }
        }
    }

    void removeBluetoothDevice(ArrayList<BluetoothDeviceData> bondedDevices) {
        BluetoothDeviceData deviceData;
        SharedPreferences pref = this.mContext.getSharedPreferences("PNFBluetoothDevic", 0);
        boolean isAddDevice = false;
        String name = pref.getString("deviceName", "");
        String address = pref.getString("deviceAddress", "");
        String deviceName = "";
        String deviceAddress = "";
        if (name != null && name.contains("Equil")) {
            for (int d = 0; d < bondedDevices.size(); d++) {
                deviceData = (BluetoothDeviceData) bondedDevices.get(d);
                if (address.equals(deviceData.deviceAddress)) {
                    deviceName = deviceData.deviceName;
                    deviceAddress = deviceData.deviceAddress;
                    bondedDevices.remove(d);
                    isAddDevice = true;
                    break;
                }
            }
        }
        for (int i = 0; i < bondedDevices.size(); i++) {
            deviceData = (BluetoothDeviceData) bondedDevices.get(i);
            this.pairedDevices.push(new BluetoothDeviceData(deviceData.deviceName, deviceData.deviceAddress));
        }
        if (isAddDevice) {
            this.pairedDevices.push(new BluetoothDeviceData(deviceName, deviceAddress));
        }
    }

    public void write(byte[] out) {
        synchronized (this) {
            if (this.mState != 5) {
                return;
            }
            ConnectedThread r = this.mConnectedThread;
            r.write(out);
        }
    }

    private void connectionFailed() {
        stop();
        setState(3);
        setMode(11);
    }

    private void connectionLost() {
        this.isAlive = false;
        PNFDefine.isImportMsg = false;
        stopBTCloseTimer();
        initDI();
        this.hardwareData.modelCode = 0;
        this.hardwareData.mcu1Code = 0;
        this.hardwareData.mcu2Code = 0;
        this.hardwareData.hwVersion = 0;
        this.hardwareData.audioMode = (byte) 1;
        this.hardwareData.audioVolum = (byte) -1;
        this.livepenlist = new ArrayList();
        this.bLiveChangePage = false;
        this.bCanDi = false;
        this.isGesture = false;
        this.specialKeyDown = false;
        this.m_ptGestureList = new ArrayList();
        this.clickCnt = 0;
        this.hardwareData.stationPosition = 2;
        this.hardwareData.oldStationPosition = -1;
        this.bClicked = false;
        this.PenStatus = 0;
        this.PenOldStatus = 0;
        this.MakerPenStatus = 0;
        this.temperature = 0;
        this.xsqr = 0;
        this.ysqr = 0;
        this.xxx = 0;
        this.yyy = 0;
        this.xxx0 = 0;
        this.yyy0 = 0;
        this.pen_cnt = 0;
        this.xbuf = 0.0d;
        this.ybuf = 0.0d;
        this.dbuf = 0.0d;
        this.l_sqr = 0.0d;
        this.Len_L = (short) 0;
        this.Len_R = (short) 0;
        this.Len_Lb = (short) 0;
        this.Len_Rb = (short) 0;
        this.delta_move = 0;
        this.rmd = 0;
        this.delta_move_b = 0;
        this.ptRaw = new Point();
        this.OldpenData = new PenDataClass();
        stop();
        PNFMessageHandler(2);
        setState(2);
        setMode(11);
    }

    public void disConnection() {
        setDIState(13);
        appSessionClose();
        this.bFirstData = true;
    }

    void initDI() {
        this.di_bFirst = true;
        this._writeData = null;
        this._readData = null;
        this.di_disk_err_check = false;
        this.bDeviceDataCheck = false;
        this.di_start_ask = false;
        this.di_bOn = false;
        this._totalBytesRead = 0;
        this.di_readbytes = 0;
        this.di_state = 0;
        this.di_choice_folder = 0;
        this.di_choice_file = 0;
        this.di_down_file_size = 0;
        this.di_battery_station = 0;
        this.di_battery_pen = 0;
        this.di_freespace = 0;
        this.di_file_data = null;
        this.di_file_dates = null;
        this.di_showdate = null;
        this.di_err_cnt = 0;
        this.di_figure_count = 0;
        this.di_debug_str = new ArrayList();
    }

    void appSessionClose() {
        stopBTCloseTimer();
        setMode(11);
        byte[] start = new byte[7];
        start[0] = (byte) 112;
        start[1] = (byte) 112;
        start[5] = (byte) -1;
        start[6] = (byte) -1;
        switch (this.hardwareData.modelCode) {
            case 3:
            case 4:
                start = new byte[11];
                start[0] = (byte) 112;
                start[1] = (byte) 112;
                start[9] = (byte) -1;
                start[10] = (byte) -1;
                break;
            default:
                start = new byte[7];
                start[0] = (byte) 112;
                start[1] = (byte) 112;
                start[5] = (byte) -1;
                start[6] = (byte) -1;
                break;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.write(start);
        }
        new Handler().postDelayed(new Runnable() {
            public void run() {
                PNFBluetoothFreeCore.this.isAlive = false;
            }
        }, 1000);
    }

    void ChangeT2Mode() {
        this.bDeviceDataCheck = true;
        byte[] start = new byte[7];
        start[0] = (byte) 112;
        start[1] = (byte) 112;
        start[5] = (byte) -1;
        start[6] = (byte) -1;
        switch (this.hardwareData.modelCode) {
            case 3:
            case 4:
                start = new byte[11];
                start[0] = (byte) 112;
                start[1] = (byte) 112;
                start[9] = (byte) -1;
                start[10] = (byte) -1;
                break;
            default:
                start = new byte[7];
                start[0] = (byte) 112;
                start[1] = (byte) 112;
                start[5] = (byte) -1;
                start[6] = (byte) -1;
                break;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.write(start);
        }
    }

    void appSessionStart() {
        byte[] start;
        startBTCloseTimer();
        setMode(10);
        switch (this.hardwareData.modelCode) {
            case 3:
            case 4:
                start = new byte[11];
                start[0] = (byte) 113;
                start[1] = (byte) 112;
                start[3] = (byte) 1;
                start[5] = (byte) -1;
                start[6] = (byte) -1;
                start[7] = (byte) -1;
                start[8] = (byte) -1;
                start[9] = (byte) -1;
                start[10] = (byte) -1;
                start[0] = (byte) (((((((start[1] + start[2]) + start[3]) + start[4]) + start[5]) + start[6]) + start[7]) + start[8]);
                break;
            default:
                start = new byte[7];
                start[0] = (byte) 113;
                start[1] = (byte) 112;
                start[3] = (byte) 1;
                start[5] = (byte) -1;
                start[6] = (byte) -1;
                break;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.write(start);
        }
        this.saveSessionTime = (long) GetCurrentSec();
    }

    public int GetCurrentSec() {
        return (int) (new Date().getTime() / 1000);
    }

    boolean NOMOVE_RANGE(int iAccPoint) {
        return iAccPoint > Const.IM_DEFAULT_TIMEOUT || iAccPoint < DeviceOperationRESTServiceProvider.TIMEOUT;
    }

    int GetSmartMakerStationPosition(int x, int y, int z) {
        int SMAccelX = x;
        int SMAccelY = y;
        int SMAccelZ = z;
        if (SMAccelX < 0) {
            SMAccelX += SupportMenu.USER_MASK;
        }
        if (SMAccelY < 0) {
            SMAccelY += SupportMenu.USER_MASK;
        }
        if (SMAccelZ < 0) {
            SMAccelZ += SupportMenu.USER_MASK;
        }
        if (NOMOVE_RANGE(SMAccelX) && SMAccelY > 15000 && SMAccelY < 17000) {
            return 2;
        }
        if (NOMOVE_RANGE(SMAccelY) && SMAccelX > 48000 && SMAccelX < SQLiteDatabase.SQLITE_MAX_LIKE_PATTERN_LENGTH) {
            return 1;
        }
        if (NOMOVE_RANGE(SMAccelY) && SMAccelX > 15000 && SMAccelX < 17000) {
            return 4;
        }
        if (!NOMOVE_RANGE(SMAccelX) || SMAccelY <= 48000 || SMAccelY >= SQLiteDatabase.SQLITE_MAX_LIKE_PATTERN_LENGTH) {
            return 2;
        }
        return 3;
    }

    void OpenFileTobyte(byte[] action) {
        byte[] fullaction = new byte[]{(byte) -1, (byte) -112, (byte) 2, action[0], action[1], action[2], action[3], action[4], action[5], (byte) -1, (byte) -1};
        fullaction[0] = (byte) (((((((fullaction[1] + fullaction[2]) + fullaction[3]) + fullaction[4]) + fullaction[5]) + fullaction[6]) + fullaction[7]) + fullaction[8]);
        writeData(fullaction);
        this.di_state = 2;
        this.bDeviceDataCheck = true;
        this.OpenFileByte = action;
    }

    void ReopenFileTobyte() {
        OpenFileTobyte(this.OpenFileByte);
    }

    void DeleteFileTobyte(byte[] action) {
        if (action != null && action.length != 0) {
            byte[] fullaction = new byte[]{(byte) -1, (byte) -112, (byte) 4, action[0], action[1], action[2], action[3], action[4], action[5], (byte) -1, (byte) -1};
            fullaction[0] = (byte) (((((((fullaction[1] + fullaction[2]) + fullaction[3]) + fullaction[4]) + fullaction[5]) + fullaction[6]) + fullaction[7]) + fullaction[8]);
            this.di_state = 4;
            this.bDeleteDIFile = true;
            this.bDeviceDataCheck = true;
            this.DeleteFileName = String.format("%02d%02d%02d%02d%02d%02d", new Object[]{Byte.valueOf(action[0]), Byte.valueOf(action[1]), Byte.valueOf(action[2]), Byte.valueOf(action[3]), Byte.valueOf(action[4]), Byte.valueOf(action[5])});
            writeData(fullaction);
        }
    }

    void AddDeletelist(byte[] action) {
        byte[] fullaction = new byte[]{(byte) -1, (byte) -112, (byte) 4, action[0], action[1], action[2], action[3], action[4], action[5], (byte) -1, (byte) -1};
        fullaction[0] = (byte) (((((((fullaction[1] + fullaction[2]) + fullaction[3]) + fullaction[4]) + fullaction[5]) + fullaction[6]) + fullaction[7]) + fullaction[8]);
        this.DeleteList.add(fullaction);
    }

    void DeleteFolderTobyte(byte[] action) {
        byte[] fullaction = new byte[]{(byte) -1, (byte) -112, (byte) 4, action[0], action[1], action[2], action[3], action[4], action[5], (byte) -1, (byte) -1};
        fullaction[0] = (byte) (((((((fullaction[1] + fullaction[2]) + fullaction[3]) + fullaction[4]) + fullaction[5]) + fullaction[6]) + fullaction[7]) + fullaction[8]);
        this.di_state = 5;
        this.bDeviceDataCheck = true;
        this.DeleteFileName = String.format("%02d%02d%02d%02d%02d%02d", new Object[]{Byte.valueOf(action[0]), Byte.valueOf(action[1]), Byte.valueOf(action[2]), Byte.valueOf(action[3]), Byte.valueOf(action[4]), Byte.valueOf(action[5])});
        writeData(fullaction);
    }

    void setDIState(int state) {
        if (this.bCanDi) {
            this.mDiState = state;
            this._readData = null;
            action = new byte[11];
            temp = new byte[6];
            command_01 = new char[8];
            char[] command_02 = new char[]{'\u0001', '\u0002', '\u0003', '\u0004', '\u0005', '\u0006', '\u0007', '\b', '\t', '\n', '\u000b', '\f', '\r', '\u000e', '\u000f', '\u001f', '/', '?', 'ï', '@', (byte) command_01[0]};
            action[1] = (byte) command_01[1];
            action[2] = (byte) command_02[state];
            this.di_state = state;
            this.bDeviceDataCheck = true;
            if (state == 1) {
                this.di_choice_file = 0;
                this.di_choice_folder = 0;
                this.di_readbytes = 0;
                this._totalBytesRead = 0;
                this.di_down_file_size = 0;
                this.di_file_dates = null;
                this.di_file_data = null;
            }
            int i;
            switch (state) {
                case 0:
                    this.bDeviceDataCheck = false;
                    break;
                case 1:
                case 6:
                case 8:
                case 9:
                case 10:
                case 11:
                case 14:
                case 19:
                    for (i = 0; i < 6; i++) {
                        action[i + 3] = (byte) 0;
                    }
                    break;
                case 7:
                    temp = setDateByte();
                    action = new byte[]{(byte) -1, (byte) -112, (byte) 7, temp[0], temp[1], temp[2], temp[3], temp[4], temp[5], (byte) -1, (byte) -1};
                    break;
                case 12:
                case 13:
                    this.bDeviceDataCheck = false;
                    for (i = 0; i < 6; i++) {
                        action[i + 3] = (byte) 0;
                    }
                    break;
                case 20:
                    action[3] = this.hardwareData.audioMode;
                    action[4] = this.hardwareData.audioVolum;
                    for (i = 2; i < 6; i++) {
                        action[i + 3] = (byte) 0;
                    }
                    break;
            }
            action[0] = (byte) (((((((action[1] + action[2]) + action[3]) + action[4]) + action[5]) + action[6]) + action[7]) + action[8]);
            action[10] = (byte) -1;
            action[9] = (byte) -1;
            writeData(action);
        }
    }

    byte[] setDateByte() {
        byte[] data = new byte[6];
        Calendar cal = Calendar.getInstance();
        int year = cal.get(1) - 2000;
        int month = cal.get(2) + 1;
        int date = cal.get(5);
        int hour = cal.get(11);
        int min = cal.get(12);
        int sec = cal.get(13);
        data[0] = (byte) year;
        data[1] = (byte) month;
        data[2] = (byte) date;
        data[3] = (byte) hour;
        data[4] = (byte) min;
        data[5] = (byte) sec;
        return data;
    }

    public int hexToIntBug(byte data) {
        return Integer.valueOf(String.format("%02x", new Object[]{Byte.valueOf(data)})).intValue();
    }

    public int hexToInt(byte data) {
        return Integer.valueOf(String.format("%02x", new Object[]{Byte.valueOf(data)})).intValue();
    }

    public int hexToInt(byte[] data, int len, int idx) {
        String str = "";
        for (int i = 0; i <= len; i++) {
            str = new StringBuilder(String.valueOf(str)).append(String.format("%02X", new Object[]{Byte.valueOf(data[idx - i])})).toString();
        }
        return Integer.parseInt(str, 16);
    }

    void writeData(byte[] data) {
        if (this._writeData == null) {
            this._writeData = new byte[data.length];
        }
        this._writeData = data;
        if (this.mConnectedThread != null) {
            this.mConnectedThread.write(this._writeData);
        }
    }

    byte[] readData(int bytesToRead) {
        if (this._readData == null) {
            return null;
        }
        if (this._readData.length < bytesToRead) {
            return null;
        }
        byte[] data = new byte[this._readData.length];
        int ulen = this._readData.length / bytesToRead;
        for (int x = 0; x < ulen; x++) {
            System.arraycopy(this._readData, 0, data, x * bytesToRead, bytesToRead);
            byte[] temp = new byte[(this._readData.length - bytesToRead)];
            System.arraycopy(this._readData, bytesToRead, temp, 0, this._readData.length - bytesToRead);
            this._readData = null;
            if (temp.length != 0) {
                this._readData = temp;
            }
        }
        return data;
    }

    boolean setDIData(byte[] bais) {
        if (this.hardwareData.modelCode < 3) {
            return false;
        }
        if (!this.bDeviceDataCheck) {
            return false;
        }
        int nHeaderSize;
        Object deviceBuf = bais;
        if (this.di_state == 2 || this.di_state == 3 || this.di_state == 11) {
            int nPacketSize = InputDeviceCompat.SOURCE_TOUCHSCREEN;
            nHeaderSize = InputDeviceCompat.SOURCE_TOUCHSCREEN;
        } else {
            nHeaderSize = 16;
        }
        if (deviceBuf[0] == this.hardwareData.hwVersion && deviceBuf[2] == Byte.MAX_VALUE && deviceBuf[8] == (byte) 0 && deviceBuf[9] == (byte) 0 && deviceBuf[10] == (byte) 0 && deviceBuf[11] == (byte) 0 && (deviceBuf[14] & 255) == 255 && (deviceBuf[15] & 255) == 255) {
            int i;
            switch (deviceBuf[3]) {
                case (byte) -113:
                    this.di_start_ask = true;
                    PNFDIHandler(5);
                    break;
                case (byte) -97:
                    this.InsertCnt = 0;
                    this.di_start_ask = false;
                    this.di_err_cnt = 0;
                    this.bDeviceDataCheck = false;
                    PNFDIHandler(6);
                    if (this._readData != null) {
                        if (this.di_file_data != null) {
                            this.di_file_data = null;
                        }
                        this._totalBytesRead = this._readData.length;
                        Object data = readData(this._totalBytesRead);
                        this.di_file_data = new byte[data.length];
                        if (data != null) {
                            int len;
                            int range;
                            if (this._totalBytesRead == nHeaderSize) {
                                range = nHeaderSize - 2;
                                this.di_file_data = new byte[range];
                                System.arraycopy(data, 0, this.di_file_data, 0, range);
                            } else {
                                len = this._totalBytesRead / nHeaderSize;
                                this.di_file_data = new byte[(this._totalBytesRead - (len * 2))];
                                range = nHeaderSize - 2;
                                for (i = 0; i < len; i++) {
                                    System.arraycopy(data, nHeaderSize * i, this.di_file_data, i * range, range);
                                }
                            }
                            int j;
                            Object r_buf;
                            if (this.di_state != 1) {
                                byte[] r_buf2;
                                boolean bEOF;
                                if (this.di_state != 2 && this.di_state != 11) {
                                    if (this.di_state != 3) {
                                        if (this.di_state == 8 && this.di_showdate != null) {
                                            this.di_showdate = null;
                                            break;
                                        }
                                    }
                                    r_buf2 = this.di_file_data;
                                    int arr_idx = 0;
                                    int start = 0;
                                    len = this.di_file_data.length;
                                    for (i = 0; i < len; i++) {
                                        int cnt;
                                        int nx;
                                        Byte[] temp;
                                        bEOF = true;
                                        j = 0;
                                        while (j < 12) {
                                            if (r_buf2[i + j] != (byte) -1) {
                                                bEOF = false;
                                                if (bEOF) {
                                                    cnt = i + 12;
                                                    nx = 0;
                                                    do {
                                                        nx++;
                                                        if (r_buf2[cnt + nx] == (byte) 0) {
                                                        }
                                                        temp = new Byte[((i + 12) - start)];
                                                        for (j = 0; j < (i + 12) - (start * 2); j++) {
                                                            temp[j] = Byte.valueOf(this.di_file_data[start + j]);
                                                        }
                                                        start = (cnt + nx) - 1;
                                                        arr_idx++;
                                                    } while (arr_idx < getDIFileCount(this.di_choice_folder) - 1);
                                                    temp = new Byte[((i + 12) - start)];
                                                    for (j = 0; j < (i + 12) - (start * 2); j++) {
                                                        temp[j] = Byte.valueOf(this.di_file_data[start + j]);
                                                    }
                                                    start = (cnt + nx) - 1;
                                                    arr_idx++;
                                                }
                                            } else {
                                                j++;
                                            }
                                        }
                                        if (bEOF) {
                                            cnt = i + 12;
                                            nx = 0;
                                            do {
                                                nx++;
                                                if (r_buf2[cnt + nx] == (byte) 0) {
                                                }
                                                temp = new Byte[((i + 12) - start)];
                                                for (j = 0; j < (i + 12) - (start * 2); j++) {
                                                    temp[j] = Byte.valueOf(this.di_file_data[start + j]);
                                                }
                                                start = (cnt + nx) - 1;
                                                arr_idx++;
                                            } while (arr_idx < getDIFileCount(this.di_choice_folder) - 1);
                                            temp = new Byte[((i + 12) - start)];
                                            for (j = 0; j < (i + 12) - (start * 2); j++) {
                                                temp[j] = Byte.valueOf(this.di_file_data[start + j]);
                                            }
                                            start = (cnt + nx) - 1;
                                            arr_idx++;
                                        }
                                    }
                                    this.di_file_data = null;
                                    break;
                                }
                                Object d2 = this.di_file_data;
                                this.di_file_data = null;
                                len = d2.length;
                                r_buf = d2;
                                i = 0;
                                while (i < len) {
                                    int size;
                                    bEOF = true;
                                    j = 0;
                                    while (j < 12) {
                                        if (r_buf[i + j] != (byte) -1) {
                                            bEOF = false;
                                            if (bEOF) {
                                                size = i + 12;
                                                this.di_file_data = new byte[size];
                                                System.arraycopy(r_buf, 0, this.di_file_data, 0, size);
                                                this.di_down_file_size = size;
                                                r_buf2 = this.di_file_data;
                                                if (this.di_state != 2) {
                                                    convertData(true, r_buf2, false);
                                                } else if (this.di_state == 11) {
                                                    convertData(true, r_buf2, true);
                                                    PNFDIHandler(10);
                                                    return true;
                                                }
                                                this.OpenFileByte = null;
                                                break;
                                            }
                                            i++;
                                        } else {
                                            j++;
                                        }
                                    }
                                    if (bEOF) {
                                        size = i + 12;
                                        this.di_file_data = new byte[size];
                                        System.arraycopy(r_buf, 0, this.di_file_data, 0, size);
                                        this.di_down_file_size = size;
                                        r_buf2 = this.di_file_data;
                                        if (this.di_state != 2) {
                                            convertData(true, r_buf2, false);
                                        } else if (this.di_state == 11) {
                                            convertData(true, r_buf2, true);
                                            PNFDIHandler(10);
                                            return true;
                                        }
                                        this.OpenFileByte = null;
                                    } else {
                                        i++;
                                    }
                                }
                                r_buf2 = this.di_file_data;
                                if (this.di_state != 2) {
                                    convertData(true, r_buf2, false);
                                } else if (this.di_state == 11) {
                                    convertData(true, r_buf2, true);
                                    PNFDIHandler(10);
                                    return true;
                                }
                                this.OpenFileByte = null;
                            } else {
                                int[] dateVal = new int[6];
                                int ulen = this.di_file_data.length / 7;
                                int nEmptyFolder = 0;
                                for (i = 0; i < ulen; i++) {
                                    HashMap arrTest = new HashMap();
                                    r_buf = new byte[7];
                                    System.arraycopy(this.di_file_data, i * 7, r_buf, 0, 7);
                                    if (r_buf[0] != (byte) 0 || r_buf[1] != (byte) 0 || r_buf[2] != (byte) 0 || r_buf[3] != (byte) 0 || r_buf[4] != (byte) 0 || r_buf[5] != (byte) 0 || r_buf[6] != (byte) 0) {
                                        if (this.di_file_dates == null) {
                                            this.di_file_dates = new ArrayList();
                                        }
                                        if ((r_buf[0] & -128) != 0) {
                                            r_buf[0] = (byte) (r_buf[0] & TransportMediator.KEYCODE_MEDIA_PAUSE);
                                            for (j = 0; j < 3; j++) {
                                                dateVal[j] = hexToIntBug(r_buf[j]);
                                                if (j == 0) {
                                                    dateVal[j] = dateVal[j] + 2000;
                                                }
                                            }
                                            nEmptyFolder = i;
                                        } else {
                                            for (j = 0; j < 3; j++) {
                                                dateVal[j + 3] = hexToIntBug(r_buf[j]);
                                            }
                                            this.di_file_dates.add(String.format("%04d%02d%02d%02d%02d%02d%d", new Object[]{Integer.valueOf(dateVal[0]), Integer.valueOf(dateVal[1]), Integer.valueOf(dateVal[2]), Integer.valueOf(dateVal[3]), Integer.valueOf(dateVal[4]), Integer.valueOf(dateVal[5]), Integer.valueOf(hexToInt(r_buf, 3, 6))}));
                                        }
                                        if (i == ulen - 1) {
                                            this.di_bOn = true;
                                        }
                                    } else if (nEmptyFolder + 1 == i) {
                                        byte[] action = new byte[11];
                                        action[0] = (byte) -1;
                                        action[1] = (byte) -112;
                                        action[2] = (byte) 5;
                                        action[3] = (byte) dateVal[0];
                                        action[4] = (byte) dateVal[1];
                                        action[5] = (byte) dateVal[2];
                                        action[9] = (byte) -1;
                                        action[10] = (byte) -1;
                                        action[0] = (byte) (((((((action[1] + action[2]) + action[3]) + action[4]) + action[5]) + action[6]) + action[7]) + action[8]);
                                        writeData(action);
                                    } else if (i == ulen - 1) {
                                        this.di_bOn = true;
                                    }
                                }
                                Collections.sort(this.di_file_dates);
                                PNFDIHandler(11);
                                break;
                            }
                        }
                    }
                    if (this.di_state == 1) {
                        PNFDIHandler(11);
                    }
                    if (this.di_state == 11) {
                        PNFDIHandler(10);
                        setDIState(1);
                    }
                    return true;
                    break;
                case (byte) -81:
                    this.bDeviceDataCheck = false;
                    if (this.di_state == 7) {
                        this.di_bOn = true;
                        if (this.isT2Mode) {
                            setDIState(14);
                        } else {
                            setDIState(1);
                        }
                        this.isT2Mode = false;
                    } else if (this.di_state == 2) {
                        if (this.OpenFileByte != null) {
                            ReopenFileTobyte();
                        }
                    } else if (this.di_state == 13) {
                        this.isAlive = false;
                    } else if (this.di_state == 14) {
                        this.di_bOn = true;
                        if (hexToInt(deviceBuf, 3, 7) == 0) {
                            setDIState(1);
                        } else {
                            setDIState(11);
                        }
                    } else if (this.di_state != 18) {
                        if (this.di_state == 4) {
                            if (this.bDeleteDIFile) {
                                this.bDeleteDIFile = false;
                                PNFDIHandler(4);
                            }
                            for (i = this.di_file_dates.size() - 1; i >= 0; i--) {
                                if (((String) this.di_file_dates.get(i)).contains(this.DeleteFileName)) {
                                    this.di_file_dates.remove(i);
                                }
                            }
                        } else if (this.di_state == 5) {
                            if (this.bDeleteDIFile) {
                                this.bDeleteDIFile = false;
                                PNFDIHandler(4);
                            }
                            String DeleteFolderName = this.DeleteFileName.substring(0, 6);
                            for (i = this.di_file_dates.size() - 1; i >= 0; i--) {
                                if (((String) this.di_file_dates.get(i)).substring(0, 8).contains(DeleteFolderName)) {
                                    this.di_file_dates.remove(i);
                                }
                            }
                        } else if (this.di_state == 6 || this.di_state == 7 || this.di_state == 10) {
                            this.di_file_dates = new ArrayList();
                        } else if (this.di_state == 19) {
                            initDI();
                        }
                    }
                    PNFDIHandler(7);
                    break;
                case (byte) -65:
                    this.bDeviceDataCheck = false;
                    PNFDIHandler(8);
                    break;
                case (byte) -49:
                    if (this.OpenFileByte != null) {
                        ReopenFileTobyte();
                    }
                    PNFFuncHandler(2);
                    break;
                case (byte) -1:
                    this.isAlive = false;
                    break;
            }
        } else if (!this.di_start_ask) {
            return true;
        } else {
            int i2;
            if (deviceBuf.length <= 16 && ((this._readData == null || this._readData.length == 0) && (((this.hardwareData.modelCode < 4 && (deviceBuf[2] & Wbxml.EXT_0) == 192) || (this.hardwareData.modelCode == 4 && (deviceBuf[2] & Wbxml.EXT_0) == 192 && deviceBuf[11] == (byte) 0 && deviceBuf[12] == (byte) 0)) && (deviceBuf[14] & 255) == 255 && (deviceBuf[15] & 255) == 255))) {
                i2 = this.errCnt + 1;
                this.errCnt = i2;
                if (i2 > 3) {
                    this.errCnt = 0;
                    this._readData = new byte[0];
                    if (this.bReCommand) {
                        setDIState(19);
                    } else if (this.di_state == 2) {
                        if (this.OpenFileByte != null) {
                            ReopenFileTobyte();
                            this.bReCommand = true;
                        }
                    } else if (this.di_state == 11) {
                        setDIState(11);
                    } else {
                        setDIState(this.mDiState);
                    }
                }
                return true;
            } else if (deviceBuf.length > 16 || !((this._readData == null || this._readData.length == 0) && deviceBuf[0] == this.hardwareData.hwVersion && (deviceBuf[14] & 255) == 255 && (deviceBuf[15] & 255) == 255)) {
                if (deviceBuf.length < 4098 && (this.di_state == 2 || this.di_state == 11)) {
                    this.InsertCnt += deviceBuf.length;
                }
                this.errCnt = 0;
                if (this._readData == null) {
                    this._readData = new byte[0];
                }
                int byteLength = this._readData.length + deviceBuf.length;
                Object temp2 = new byte[byteLength];
                System.arraycopy(this._readData, 0, temp2, 0, this._readData.length);
                System.arraycopy(deviceBuf, 0, temp2, this._readData.length, deviceBuf.length);
                this._readData = new byte[byteLength];
                this._readData = temp2;
            } else {
                i2 = this.errCnt + 1;
                this.errCnt = i2;
                if (i2 > 3) {
                    this.errCnt = 0;
                    this._readData = new byte[0];
                    if (this.bReCommand) {
                        setDIState(19);
                    } else if (this.di_state == 2) {
                        if (this.OpenFileByte != null) {
                            ReopenFileTobyte();
                            this.bReCommand = true;
                        }
                    } else if (this.di_state == 11) {
                        setDIState(11);
                    } else {
                        setDIState(this.mDiState);
                    }
                }
                return true;
            }
        }
        return true;
    }

    void changeAudioMode(byte _mode) {
        this.hardwareData.audioMode = _mode;
        setDIState(20);
    }

    void changeVolume(byte _volume) {
        this.hardwareData.audioVolum = _volume;
        setDIState(20);
    }

    void setAudio(byte audiomode, byte volume) {
        this.hardwareData.audioMode = audiomode;
        this.hardwareData.audioVolum = volume;
        setDIState(20);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void convertData(boolean check, byte[] r_buf, boolean bTemp) {
        Exception e;
        Iterator it;
        PenDataClass _pen;
        Object f_buf = new byte[r_buf.length];
        System.arraycopy(r_buf, 0, f_buf, 0, r_buf.length);
        float m_sd_sqr = 0.0f;
        float m_sd2 = 0.0f;
        float m_adjust_val_l = 0.0f;
        float m_adjust_val_r = 0.0f;
        this.di_figure_count = 0;
        int len = (f_buf.length / 12) - 1;
        this.di_point_max = new PointF();
        this.di_point_min = new PointF();
        PointF pointF = this.di_point_max;
        this.di_point_max.y = 0.0f;
        pointF.x = 0.0f;
        pointF = this.di_point_min;
        this.di_point_min.y = 1.0E8f;
        pointF.x = 1.0E8f;
        int m_SMPenSwitch = -1;
        ArrayList<PenDataClass> sendArray = new ArrayList();
        ArrayList<PenDataClass> sendRightArray = new ArrayList();
        ArrayList<ArrayList<PenDataClass>> TempsendArray = new ArrayList();
        int iDupIndex = 0;
        int SMPenPositionDI = -1;
        int nTotalFgCount = 0;
        this.mSMPenFlagOld = (byte) -1;
        boolean bLastPageRight = false;
        int i = 0;
        while (i < len) {
            try {
                Object buf = new byte[12];
                System.arraycopy(f_buf, i * 12, buf, 0, 12);
                if (buf[0] == (byte) 0 && buf[1] == Byte.MAX_VALUE) {
                    float m_sensor_dist;
                    if (buf[2] == (byte) 4) {
                        m_sensor_dist = this.sensor_dist;
                        m_sd_sqr = this.sd_sqr;
                        m_sd2 = this.sd2;
                        m_adjust_val_l = this.adjust_val_l;
                        m_adjust_val_r = this.adjust_val_r;
                    } else {
                        m_sensor_dist = (float) byteToShort(buf[4], buf[5]);
                        m_sd_sqr = m_sensor_dist * m_sensor_dist;
                        m_sd2 = m_sensor_dist * 2.0f;
                        m_adjust_val_l = (float) byteToShort(buf[6], buf[7]);
                        m_adjust_val_r = m_adjust_val_l;
                    }
                    i++;
                } else if (buf[10] == (byte) -86 && buf[11] == (byte) 102) {
                    ArrayList<PenDataClass> sendArray2;
                    ArrayList<PenDataClass> sendRightArray2;
                    if (this.hardwareData.modelCode == 4 && SMPenPositionDI == -1) {
                        if (iDupIndex + 1 != i) {
                            sendAccDIPage(bLastPageRight, bTemp, sendArray, sendRightArray, TempsendArray);
                            sendArray2 = new ArrayList();
                            try {
                                sendRightArray2 = new ArrayList();
                                SMPenPositionDI = -1;
                                m_SMPenSwitch = -1;
                            } catch (Exception e2) {
                                e = e2;
                                sendArray = sendArray2;
                                Log.e("error", "convertData Exception e ==> " + e.getMessage());
                                i++;
                            }
                            try {
                                this.mSMPenFlagOld = (byte) -1;
                                this.bClicked = false;
                                sendRightArray = sendRightArray2;
                                sendArray = sendArray2;
                            } catch (Exception e3) {
                                e = e3;
                                sendRightArray = sendRightArray2;
                                sendArray = sendArray2;
                                Log.e("error", "convertData Exception e ==> " + e.getMessage());
                                i++;
                            }
                        }
                        SMPenPositionDI = GetSmartMakerStationPosition(byteToShort(buf[0], buf[1]), byteToShort(buf[2], buf[3]), byteToShort(buf[4], buf[8]));
                        sendRightArray2 = sendRightArray;
                        sendArray2 = sendArray;
                    } else {
                        sendRightArray2 = sendRightArray;
                        sendArray2 = sendArray;
                    }
                    if (this.hardwareData.modelCode <= 3) {
                        SMPenPositionDI = 2;
                    }
                    if (check) {
                        boolean bT1Page = false;
                        if (buf[0] == (byte) -1 && buf[1] == (byte) -1 && buf[2] == (byte) -1 && buf[3] == (byte) -1 && buf[4] == (byte) -1 && buf[5] == (byte) -1) {
                            bT1Page = true;
                        }
                        int zh = buf[5];
                        if (this.hardwareData.modelCode == 4) {
                            zh = 0;
                        }
                        if ((zh > 80 || bT1Page) && this.bUseAcc) {
                            sendAccDIPage(bLastPageRight, bTemp, sendArray2, sendRightArray2, TempsendArray);
                            sendArray = new ArrayList();
                            try {
                                sendRightArray = new ArrayList();
                                SMPenPositionDI = -1;
                                m_SMPenSwitch = -1;
                                this.mSMPenFlagOld = (byte) -1;
                                this.bClicked = false;
                            } catch (Exception e4) {
                                e = e4;
                                sendRightArray = sendRightArray2;
                                Log.e("error", "convertData Exception e ==> " + e.getMessage());
                                i++;
                            }
                            i++;
                        }
                    }
                    sendRightArray = sendRightArray2;
                    sendArray = sendArray2;
                    i++;
                } else if (buf[10] == (byte) -86 && buf[11] == (byte) 119) {
                    sendAccDIPage(bLastPageRight, bTemp, sendArray, sendRightArray, TempsendArray);
                    if (bLastPageRight) {
                        sendArray = new ArrayList();
                    } else {
                        sendRightArray = new ArrayList();
                    }
                    SMPenPositionDI = -1;
                    m_SMPenSwitch = -1;
                    this.mSMPenFlagOld = (byte) -1;
                    this.bClicked = false;
                    iDupIndex = i;
                    i++;
                } else {
                    for (int j = 0; j < 2; j++) {
                        int inc = j * 6;
                        PenDataClass penData = new PenDataClass();
                        PenEnvDataClass penEnvData = new PenEnvDataClass();
                        short pressure;
                        byte b;
                        int x_gap;
                        int y_gap;
                        if (this.hardwareData.modelCode == 4) {
                            boolean setBeforeData;
                            pressure = (short) 100;
                            this.Len_R = byteToShort(buf[0 + inc], buf[1 + inc]);
                            this.Len_L = byteToShort(buf[2 + inc], buf[3 + inc] & TransportMediator.KEYCODE_MEDIA_PAUSE);
                            this.mSMPenFlag = buf[4 + inc];
                            this.mSMPenState = buf[5 + inc];
                            switch (this.mSMPenState) {
                                case 15:
                                    if (this.hardwareData.modelCode != 4 || this.hardwareData.mcu2Code < 4) {
                                        b = (byte) 0;
                                        if (this.mSMPenStateOld == 15) {
                                            break;
                                        }
                                    }
                                    if (this.up_cnt >= 1 || this.mSMPenStateOld == 89 || this.mSMPenStateOld == 92) {
                                        b = (byte) 0;
                                        if (this.mSMPenStateOld != 92) {
                                        }
                                    } else {
                                        this.MakerPenStatus = this.mSMPenStateOld;
                                        b = (byte) 64;
                                    }
                                    this.up_cnt++;
                                    break;
                                case 81:
                                case 82:
                                case 83:
                                case 84:
                                case 86:
                                case 88:
                                case 89:
                                case 92:
                                    this.MakerPenStatus = this.mSMPenState;
                                    b = (byte) 64;
                                    this.up_cnt = 0;
                                    if (this.bClicked && this.mSMPenStateOld != this.mSMPenState) {
                                        this.bClicked = false;
                                        this.MakerPenStatus = this.mSMPenStateOld;
                                    }
                                    if (this.forceUp) {
                                        this.bClicked = false;
                                        this.forceUp = false;
                                    }
                                    this.mSMPenStateOld = this.mSMPenState;
                                    setBeforeData = false;
                                    if (this.Len_R < this.X_MIN_RANGE_WARN || this.Len_R > this.X_MAX_RANGE_WARN || this.Len_L < this.Y_MIN_RANGE_WARN || this.Len_L > this.Y_MAX_RANGE_WARN) {
                                        if (!this.bClicked) {
                                            break;
                                        }
                                        b = (byte) 0;
                                        setBeforeData = true;
                                    }
                                    switch (SMPenPositionDI) {
                                        case 1:
                                            this.xxx = this.Len_L;
                                            this.yyy = this.Len_R;
                                            penData.StationPosition = 1;
                                            break;
                                        case 2:
                                            this.xxx = this.Len_R;
                                            this.yyy = (int) (66535.0f - ((float) this.Len_L));
                                            penData.StationPosition = 2;
                                            break;
                                        case 3:
                                            this.yyy = this.Len_L;
                                            this.xxx = (int) (66535.0f - ((float) this.Len_R));
                                            penData.StationPosition = 3;
                                            break;
                                        case 4:
                                            this.xxx = (int) (66535.0f - ((float) this.Len_L));
                                            this.yyy = (int) (66535.0f - ((float) this.Len_R));
                                            penData.StationPosition = 4;
                                            break;
                                        default:
                                            this.xxx = this.Len_R;
                                            this.yyy = (int) (66535.0f - ((float) this.Len_L));
                                            penData.StationPosition = 2;
                                            break;
                                    }
                                    this.rmd_const = 600;
                                    if (setBeforeData) {
                                        this.xxx = this.xxx0;
                                        this.yyy = this.yyy0;
                                        break;
                                    }
                                    break;
                                default:
                                    break;
                            }
                            this.mSMPenStateOld = this.mSMPenState;
                            setBeforeData = false;
                            if (!this.bClicked) {
                                b = (byte) 0;
                                setBeforeData = true;
                                switch (SMPenPositionDI) {
                                    case 1:
                                        this.xxx = this.Len_L;
                                        this.yyy = this.Len_R;
                                        penData.StationPosition = 1;
                                        break;
                                    case 2:
                                        this.xxx = this.Len_R;
                                        this.yyy = (int) (66535.0f - ((float) this.Len_L));
                                        penData.StationPosition = 2;
                                        break;
                                    case 3:
                                        this.yyy = this.Len_L;
                                        this.xxx = (int) (66535.0f - ((float) this.Len_R));
                                        penData.StationPosition = 3;
                                        break;
                                    case 4:
                                        this.xxx = (int) (66535.0f - ((float) this.Len_L));
                                        this.yyy = (int) (66535.0f - ((float) this.Len_R));
                                        penData.StationPosition = 4;
                                        break;
                                    default:
                                        this.xxx = this.Len_R;
                                        this.yyy = (int) (66535.0f - ((float) this.Len_L));
                                        penData.StationPosition = 2;
                                        break;
                                }
                                this.rmd_const = 600;
                                if (setBeforeData) {
                                    this.xxx = this.xxx0;
                                    this.yyy = this.yyy0;
                                }
                                if ((this.hardwareData.modelCode == 2 || this.hardwareData.modelCode == 3) && b == (byte) 64 && pressure <= (short) 25) {
                                    b = (byte) 0;
                                }
                                if (b != (byte) 64) {
                                    if (this.bClicked) {
                                        INIT_PEN_DOWN();
                                    } else {
                                        this.PenStatus = 2;
                                    }
                                } else if (this.bClicked) {
                                    this.PenStatus = 4;
                                } else {
                                    this.bClicked = false;
                                    this.PenStatus = 3;
                                }
                                x_gap = Math.abs(this.xxx - this.xxx0);
                                y_gap = Math.abs(this.yyy - this.yyy0);
                                this.xsqr = x_gap * x_gap;
                                this.ysqr = y_gap * y_gap;
                                this.delta_move = (int) Math.sqrt((double) (this.xsqr + this.ysqr));
                                this.rmd = Math.abs(this.delta_move - this.delta_move_b);
                                if (!CHECK_RMD_WITH_SEND_NOTI(true)) {
                                    this.delta_move_b = this.delta_move;
                                    this.xxx = ((this.xxx + this.xxx) + this.xxx0) / 3;
                                    this.yyy = ((this.yyy + this.yyy) + this.yyy0) / 3;
                                    this.xxx1 = this.xxx0;
                                    this.yyy1 = this.yyy0;
                                    this.xxx0 = this.xxx;
                                    this.yyy0 = this.yyy;
                                    this.Len_Rb = this.Len_R;
                                    this.Len_Lb = this.Len_L;
                                    this.ptRaw.x = this.xxx;
                                    this.ptRaw.y = this.yyy;
                                    if (this.PenStatus != 4) {
                                        if (this.hardwareData.modelCode != 4) {
                                            if (!(this.mSMPenFlagOld == (byte) -1 || this.mSMPenFlagOld == this.mSMPenFlag)) {
                                                if (!this.bClicked) {
                                                    this.mSMPenFlag = this.mSMPenFlagOld;
                                                } else if (penData.ptRaw.x == ((float) this.xxx)) {
                                                    this.mSMPenFlag = this.mSMPenFlagOld;
                                                }
                                            }
                                            if ((this.mSMPenFlag & 1) != 1) {
                                                if (m_SMPenSwitch == 0) {
                                                    this.bClicked = false;
                                                    if (penData.PenStatus == 2 || penData.PenStatus == 1) {
                                                        penData.PenStatus = 3;
                                                        switch (SMPenPositionDI) {
                                                            case 2:
                                                            case 3:
                                                                sendArray.add(penData);
                                                                nTotalFgCount++;
                                                                break;
                                                            default:
                                                                sendArray.add(penData);
                                                                nTotalFgCount++;
                                                                break;
                                                        }
                                                    }
                                                    if (this.PenStatus == 2) {
                                                        this.PenStatus = 1;
                                                    }
                                                }
                                                m_SMPenSwitch = 1;
                                                penData.bRight = true;
                                                bLastPageRight = true;
                                            } else {
                                                if (m_SMPenSwitch == 1) {
                                                    this.bClicked = false;
                                                    if (penData.PenStatus == 2 || penData.PenStatus == 1) {
                                                        penData.PenStatus = 3;
                                                        sendRightArray.add(penData);
                                                        nTotalFgCount++;
                                                    }
                                                    if (this.PenStatus == 2) {
                                                        this.PenStatus = 1;
                                                    }
                                                }
                                                m_SMPenSwitch = 0;
                                                penData.bRight = false;
                                                bLastPageRight = false;
                                            }
                                            this.mSMPenFlagOld = this.mSMPenFlag;
                                            this.PenOldStatus = this.PenStatus;
                                        } else {
                                            penData.bRight = true;
                                        }
                                    }
                                    penData.Temperature = this.temperature;
                                    penData.pressure = pressure;
                                    penData.PenStatus = this.PenStatus;
                                    penData.MakerPenStatus = this.MakerPenStatus;
                                    penData.ptRaw.x = (float) this.ptRaw.x;
                                    penData.ptRaw.y = (float) this.ptRaw.y;
                                    if (this.PenStatus == 4) {
                                        if (this.hardwareData.modelCode != 4) {
                                            if (penData.bRight) {
                                                switch (SMPenPositionDI) {
                                                    case 2:
                                                    case 3:
                                                        sendArray.add(penData);
                                                        nTotalFgCount++;
                                                        break;
                                                    default:
                                                        sendArray.add(penData);
                                                        nTotalFgCount++;
                                                        break;
                                                }
                                            }
                                            sendRightArray.add(penData);
                                        } else {
                                            sendArray.add(penData);
                                            nTotalFgCount++;
                                        }
                                    }
                                }
                            }
                        } else {
                            pressure = byteToShort(buf[4 + inc], buf[5 + inc] & 247);
                            this.Len_R = byteToShort(buf[0 + inc], buf[1 + inc] & 63);
                            this.Len_L = byteToShort(buf[2 + inc], buf[3 + inc] & 63);
                            b = (byte) (buf[3 + inc] & 64);
                            this.dbuf = (double) (((float) this.Len_R) - m_adjust_val_r);
                            this.l_sqr = this.dbuf * this.dbuf;
                            this.dbuf = (double) (((float) this.Len_L) - m_adjust_val_l);
                            this.dbuf *= this.dbuf;
                            this.xbuf = ((((double) m_sd_sqr) + this.l_sqr) - this.dbuf) / ((double) m_sd2);
                            this.dbuf = this.xbuf * this.xbuf;
                            this.ybuf = Math.abs(this.l_sqr - this.dbuf);
                            this.ybuf = Math.sqrt(this.ybuf);
                            this.xxx = (int) (this.xbuf + 3000.0d);
                            this.yyy = (int) (this.ybuf + 200.0d);
                            this.rmd_const = 600;
                            b = (byte) 0;
                            if (b != (byte) 64) {
                                if (this.bClicked) {
                                    this.PenStatus = 4;
                                } else {
                                    this.bClicked = false;
                                    this.PenStatus = 3;
                                }
                            } else if (this.bClicked) {
                                INIT_PEN_DOWN();
                            } else {
                                this.PenStatus = 2;
                            }
                            x_gap = Math.abs(this.xxx - this.xxx0);
                            y_gap = Math.abs(this.yyy - this.yyy0);
                            this.xsqr = x_gap * x_gap;
                            this.ysqr = y_gap * y_gap;
                            this.delta_move = (int) Math.sqrt((double) (this.xsqr + this.ysqr));
                            this.rmd = Math.abs(this.delta_move - this.delta_move_b);
                            if (!CHECK_RMD_WITH_SEND_NOTI(true)) {
                                this.delta_move_b = this.delta_move;
                                this.xxx = ((this.xxx + this.xxx) + this.xxx0) / 3;
                                this.yyy = ((this.yyy + this.yyy) + this.yyy0) / 3;
                                this.xxx1 = this.xxx0;
                                this.yyy1 = this.yyy0;
                                this.xxx0 = this.xxx;
                                this.yyy0 = this.yyy;
                                this.Len_Rb = this.Len_R;
                                this.Len_Lb = this.Len_L;
                                this.ptRaw.x = this.xxx;
                                this.ptRaw.y = this.yyy;
                                if (this.PenStatus != 4) {
                                    if (this.hardwareData.modelCode != 4) {
                                        penData.bRight = true;
                                    } else {
                                        if (!this.bClicked) {
                                            this.mSMPenFlag = this.mSMPenFlagOld;
                                        } else if (penData.ptRaw.x == ((float) this.xxx)) {
                                            this.mSMPenFlag = this.mSMPenFlagOld;
                                        }
                                        if ((this.mSMPenFlag & 1) != 1) {
                                            if (m_SMPenSwitch == 1) {
                                                this.bClicked = false;
                                                penData.PenStatus = 3;
                                                sendRightArray.add(penData);
                                                nTotalFgCount++;
                                                if (this.PenStatus == 2) {
                                                    this.PenStatus = 1;
                                                }
                                            }
                                            m_SMPenSwitch = 0;
                                            penData.bRight = false;
                                            bLastPageRight = false;
                                        } else {
                                            if (m_SMPenSwitch == 0) {
                                                this.bClicked = false;
                                                penData.PenStatus = 3;
                                                switch (SMPenPositionDI) {
                                                    case 2:
                                                    case 3:
                                                        sendArray.add(penData);
                                                        nTotalFgCount++;
                                                        break;
                                                    default:
                                                        sendArray.add(penData);
                                                        nTotalFgCount++;
                                                        break;
                                                }
                                                if (this.PenStatus == 2) {
                                                    this.PenStatus = 1;
                                                }
                                            }
                                            m_SMPenSwitch = 1;
                                            penData.bRight = true;
                                            bLastPageRight = true;
                                        }
                                        this.mSMPenFlagOld = this.mSMPenFlag;
                                        this.PenOldStatus = this.PenStatus;
                                    }
                                }
                                penData.Temperature = this.temperature;
                                penData.pressure = pressure;
                                penData.PenStatus = this.PenStatus;
                                penData.MakerPenStatus = this.MakerPenStatus;
                                penData.ptRaw.x = (float) this.ptRaw.x;
                                penData.ptRaw.y = (float) this.ptRaw.y;
                                if (this.PenStatus == 4) {
                                    if (this.hardwareData.modelCode != 4) {
                                        sendArray.add(penData);
                                        nTotalFgCount++;
                                    } else if (penData.bRight) {
                                        switch (SMPenPositionDI) {
                                            case 2:
                                            case 3:
                                                sendArray.add(penData);
                                                nTotalFgCount++;
                                                break;
                                            default:
                                                sendArray.add(penData);
                                                nTotalFgCount++;
                                                break;
                                        }
                                    } else {
                                        sendRightArray.add(penData);
                                    }
                                }
                            }
                        }
                    }
                    i++;
                }
            } catch (Exception e5) {
                e = e5;
                Log.e("error", "convertData Exception e ==> " + e.getMessage());
                i++;
            }
        }
        if (sendArray.size() >= 3) {
            if (bTemp) {
                TempsendArray.add(sendArray);
            }
            it = sendArray.iterator();
            while (it.hasNext()) {
                _pen = (PenDataClass) it.next();
                if (this.di_point_max.x < _pen.ptRaw.x) {
                    this.di_point_max.x = _pen.ptRaw.x;
                }
                if (this.di_point_min.x > _pen.ptRaw.x) {
                    this.di_point_min.x = _pen.ptRaw.x;
                }
                if (this.di_point_max.y < _pen.ptRaw.y) {
                    this.di_point_max.y = _pen.ptRaw.y;
                }
                if (this.di_point_min.y > _pen.ptRaw.y) {
                    this.di_point_min.y = _pen.ptRaw.y;
                }
            }
            this.di_paper_size = setDIPaperSize(this.di_point_max, this.di_point_min, ((PenDataClass) sendArray.get(0)).StationPosition);
            this.di_point_max = new PointF();
            this.di_point_min = new PointF();
            pointF = this.di_point_max;
            this.di_point_max.y = 0.0f;
            pointF.x = 0.0f;
            pointF = this.di_point_min;
            this.di_point_min.y = 9999999.0f;
            pointF.x = 9999999.0f;
            if (!bTemp) {
                if (sendRightArray.size() < 3) {
                    PNFDIHandler(1, this.di_paper_size, sendArray);
                } else {
                    PNFDIHandler(3, this.di_paper_size, sendArray);
                }
            }
        }
        if (sendRightArray.size() >= 3) {
            if (bTemp) {
                TempsendArray.add(sendRightArray);
            }
            it = sendRightArray.iterator();
            while (it.hasNext()) {
                _pen = (PenDataClass) it.next();
                if (this.di_point_max.x < _pen.ptRaw.x) {
                    this.di_point_max.x = _pen.ptRaw.x;
                }
                if (this.di_point_min.x > _pen.ptRaw.x) {
                    this.di_point_min.x = _pen.ptRaw.x;
                }
                if (this.di_point_max.y < _pen.ptRaw.y) {
                    this.di_point_max.y = _pen.ptRaw.y;
                }
                if (this.di_point_min.y > _pen.ptRaw.y) {
                    this.di_point_min.y = _pen.ptRaw.y;
                }
            }
            this.di_paper_size = setDIPaperSize(this.di_point_max, this.di_point_min, ((PenDataClass) sendRightArray.get(0)).StationPosition);
            this.di_point_max = new PointF();
            this.di_point_min = new PointF();
            pointF = this.di_point_max;
            this.di_point_max.y = 0.0f;
            pointF.x = 0.0f;
            pointF = this.di_point_min;
            this.di_point_min.y = 9999999.0f;
            pointF.x = 9999999.0f;
            if (!bTemp) {
                PNFDIHandler(1, this.di_paper_size, sendRightArray);
            }
        }
        if (bTemp && TempsendArray.size() > 0) {
            PNFDIHandler(9, this.di_paper_size, TempsendArray);
            PNFDIHandler(2, this.di_paper_size, TempsendArray);
        }
        this.mSMPenFlagOld = (byte) -1;
        this.bClicked = false;
    }

    int setDIPaperSize(PointF max, PointF min, int _StationPosition) {
        if (this.hardwareData.modelCode == 4) {
            switch (_StationPosition) {
                case 1:
                    if (min.x >= 12790.0f && max.x <= 19966.0f && min.y >= 1547.0f && max.y <= 13248.0f) {
                        return 10;
                    }
                    if (min.x < 12790.0f || max.x > 19966.0f || min.y < 1532.0f || max.y > 15298.0f) {
                        return 12;
                    }
                    return 11;
                case 2:
                case 3:
                    if (min.x >= 1728.0f && max.x <= 15524.0f && min.y >= 45372.0f && max.y <= 54824.0f) {
                        return 6;
                    }
                    if (min.x >= 1830.0f && max.x <= 15506.0f && min.y >= 44156.0f && max.y <= 56034.0f) {
                        return 7;
                    }
                    if (min.x >= 1868.0f && max.x <= 20153.0f && min.y >= 45377.0f && max.y <= 54735.0f) {
                        return 8;
                    }
                    if (min.x < 1810.0f || max.x > 20164.0f || min.y < 44163.0f || max.y > 55938.0f) {
                        return 9;
                    }
                    return 9;
                case 4:
                    if (min.x >= 46612.0f && max.x <= 53788.0f && min.y >= 53961.0f && max.y <= 65662.0f) {
                        return 13;
                    }
                    if (min.x < 46612.0f || max.x > 53788.0f || min.y < 51800.0f || max.y > 65566.0f) {
                        return 15;
                    }
                    return 14;
                default:
                    return 0;
            }
        } else if (min.x >= 2500.0f && max.x <= 4704.0f && min.y >= 544.0f && max.y <= 3154.0f) {
            return 4;
        } else {
            if (min.x >= 2341.0f && max.x <= 4865.0f && min.y >= 542.0f && max.y <= 3631.0f) {
                return 2;
            }
            if (min.x >= 2027.0f && max.x <= 5183.0f && min.y >= 561.0f && max.y <= 4462.0f) {
                return 3;
            }
            if (min.x >= 1768.0f && max.x <= 5392.0f && min.y >= 563.0f && max.y <= 5160.0f) {
                return 1;
            }
            if (min.x >= 1737.0f && max.x <= 5445.0f && min.y >= 541.0f && max.y <= 4818.0f) {
                return 0;
            }
            if (this.isLetterPaper) {
                return 0;
            }
            return 1;
        }
    }

    void sendAccDIPage(boolean bLastPageRight, boolean bTemp, ArrayList<PenDataClass> sendArray, ArrayList<PenDataClass> sendRightArray, ArrayList<ArrayList<PenDataClass>> TempsendArray) {
        Iterator it;
        PenDataClass _pen;
        ArrayList<PenDataClass> sendTempArray = new ArrayList(sendArray);
        ArrayList<PenDataClass> sendTempRightArray = new ArrayList(sendRightArray);
        boolean bLeftErrFile = false;
        boolean bRightErrFile = false;
        if (sendTempArray.size() < 3) {
            bLeftErrFile = true;
        }
        if (bTemp && !bLeftErrFile) {
            TempsendArray.add(sendTempArray);
        } else if (!(bTemp || bLeftErrFile)) {
            it = sendTempArray.iterator();
            while (it.hasNext()) {
                _pen = (PenDataClass) it.next();
                if (this.di_point_max.x < _pen.ptRaw.x) {
                    this.di_point_max.x = _pen.ptRaw.x;
                }
                if (this.di_point_min.x > _pen.ptRaw.x) {
                    this.di_point_min.x = _pen.ptRaw.x;
                }
                if (this.di_point_max.y < _pen.ptRaw.y) {
                    this.di_point_max.y = _pen.ptRaw.y;
                }
                if (this.di_point_min.y > _pen.ptRaw.y) {
                    this.di_point_min.y = _pen.ptRaw.y;
                }
            }
            this.di_paper_size = setDIPaperSize(this.di_point_max, this.di_point_min, ((PenDataClass) sendTempArray.get(0)).StationPosition);
            this.di_point_max = new PointF();
            this.di_point_min = new PointF();
            PointF pointF = this.di_point_max;
            this.di_point_max.y = 0.0f;
            pointF.x = 0.0f;
            pointF = this.di_point_min;
            this.di_point_min.y = 9999999.0f;
            pointF.x = 9999999.0f;
            PNFDIHandler(3, this.di_paper_size, sendTempArray);
        }
        if (sendTempRightArray.size() < 3) {
            bRightErrFile = true;
        }
        if (bTemp && !bRightErrFile) {
            TempsendArray.add(sendTempRightArray);
        } else if (!bTemp && !bRightErrFile) {
            it = sendTempRightArray.iterator();
            while (it.hasNext()) {
                _pen = (PenDataClass) it.next();
                if (this.di_point_max.x < _pen.ptRaw.x) {
                    this.di_point_max.x = _pen.ptRaw.x;
                }
                if (this.di_point_min.x > _pen.ptRaw.x) {
                    this.di_point_min.x = _pen.ptRaw.x;
                }
                if (this.di_point_max.y < _pen.ptRaw.y) {
                    this.di_point_max.y = _pen.ptRaw.y;
                }
                if (this.di_point_min.y > _pen.ptRaw.y) {
                    this.di_point_min.y = _pen.ptRaw.y;
                }
            }
            this.di_paper_size = setDIPaperSize(this.di_point_max, this.di_point_min, ((PenDataClass) sendTempRightArray.get(0)).StationPosition);
            this.di_point_max = new PointF();
            this.di_point_min = new PointF();
            pointF = this.di_point_max;
            this.di_point_max.y = 0.0f;
            pointF.x = 0.0f;
            pointF = this.di_point_min;
            this.di_point_min.y = 9999999.0f;
            pointF.x = 9999999.0f;
            PNFDIHandler(3, this.di_paper_size, sendTempRightArray);
        }
    }

    int setDIPaperSizeMerge(int pointCnt, PointF _max, PointF _min) {
        PointF max = _max;
        PointF min = _min;
        if (pointCnt < 2) {
            return -1;
        }
        if (this.hardwareData.modelCode == 4) {
            if (min.x >= 1728.0f && max.x <= 15524.0f && min.y >= 45372.0f && max.y <= 54824.0f) {
                return 6;
            }
            if (min.x >= 1830.0f && max.x <= 15506.0f && min.y >= 44156.0f && max.y <= 56034.0f) {
                return 7;
            }
            if (min.x >= 1868.0f && max.x <= 20153.0f && min.y >= 45377.0f && max.y <= 54735.0f) {
                return 8;
            }
            if (min.x < 1810.0f || max.x > 20164.0f || min.y < 44163.0f || max.y > 55938.0f) {
                return 9;
            }
            return 9;
        } else if (min.x >= 2500.0f && max.x <= 4704.0f && min.y >= 544.0f && max.y <= 3154.0f) {
            return 4;
        } else {
            if (min.x >= 2341.0f && max.x <= 4865.0f && min.y >= 542.0f && max.y <= 3631.0f) {
                return 2;
            }
            if (min.x >= 2027.0f && max.x <= 5183.0f && min.y >= 561.0f && max.y <= 4462.0f) {
                return 3;
            }
            if (min.x >= 1768.0f && max.x <= 5392.0f && min.y >= 563.0f && max.y <= 5160.0f) {
                return 1;
            }
            if (min.x >= 1737.0f && max.x <= 5445.0f && min.y >= 541.0f && max.y <= 4818.0f) {
                return 0;
            }
            if (this.isLetterPaper) {
                return 0;
            }
            return 1;
        }
    }

    byte[] getDIShowData() {
        return this.di_showdate;
    }

    int getDIFreespace() {
        return this.di_freespace;
    }

    int getDIFileCount() {
        Log.e("0523", "di_file_dates==>" + this.di_file_dates);
        if (this.di_file_dates == null || this.di_file_dates.size() == 0) {
            return 0;
        }
        Log.e("0523", "di_file_dates.size()==>" + this.di_file_dates.size());
        return this.di_file_dates.size();
    }

    int getDIFileCount(int _folderIdx) {
        if (this.di_file_dates == null || this.di_file_dates.size() == 0) {
            return 0;
        }
        ArrayList<String> folderNameList = getDIFolderNameList();
        if (_folderIdx < folderNameList.size()) {
            return getDIFileNameList((String) folderNameList.get(_folderIdx)).size();
        }
        return 0;
    }

    ArrayList<String> getAllDIData() {
        return this.di_file_dates;
    }

    ArrayList<String> getDIFolderNameList() {
        if (this.di_file_dates == null || this.di_file_dates.size() == 0) {
            return null;
        }
        int len = this.di_file_dates.size();
        ArrayList<String> rtnList = new ArrayList();
        for (int i = 0; i < len; i++) {
            String diFileFullName = (String) this.di_file_dates.get(i);
            if (diFileFullName != null && diFileFullName.length() > 15) {
                String diDataFolder = diFileFullName.substring(0, 8);
                if (!rtnList.contains(diDataFolder)) {
                    rtnList.add(diDataFolder);
                }
            }
        }
        return rtnList;
    }

    ArrayList<String> getDIFileNameList(String _folderName) {
        if (this.di_file_dates == null || this.di_file_dates.size() == 0) {
            return null;
        }
        int len = this.di_file_dates.size();
        ArrayList<String> rtnList = new ArrayList();
        for (int i = 0; i < len; i++) {
            String diFileFullName = (String) this.di_file_dates.get(i);
            if (diFileFullName != null && diFileFullName.length() > 15 && diFileFullName.substring(0, 8).contains(_folderName)) {
                String diDataFile = diFileFullName.substring(8, 16);
                if (!rtnList.equals(diDataFile)) {
                    rtnList.add(diDataFile);
                }
            }
        }
        return rtnList;
    }

    byte[] getSendDiByte(int _index) {
        if (_index >= this.di_file_dates.size()) {
            return null;
        }
        int year = Integer.valueOf(((String) this.di_file_dates.get(_index)).substring(2, 4)).intValue();
        int month = Integer.valueOf(((String) this.di_file_dates.get(_index)).substring(2 + 2, 6)).intValue();
        int days = Integer.valueOf(((String) this.di_file_dates.get(_index)).substring(2 + 4, 8)).intValue();
        int hour = Integer.valueOf(((String) this.di_file_dates.get(_index)).substring(2 + 6, 10)).intValue();
        int minute = Integer.valueOf(((String) this.di_file_dates.get(_index)).substring(2 + 8, 12)).intValue();
        int second = Integer.valueOf(((String) this.di_file_dates.get(_index)).substring(2 + 10, 14)).intValue();
        int index = 2 + 12;
        return new byte[]{(byte) year, (byte) month, (byte) days, (byte) hour, (byte) minute, (byte) second};
    }

    void INIT_PEN_DOWN() {
        this.bClicked = true;
        this.PenStatus = 1;
        int i = this.xxx;
        this.xxx0 = i;
        this.xxx1 = i;
        i = this.yyy;
        this.yyy0 = i;
        this.yyy1 = i;
        this.Len_Lb = this.Len_L;
        this.Len_Rb = this.Len_R;
        this.angle1 = 0.0f;
        this.angle0 = 0.0f;
        this.delta_move_b = 0;
    }

    boolean CHECK_RMD_WITH_SEND_NOTI(boolean isSendNoti) {
        if (this.rmd <= this.rmd_const) {
            return false;
        }
        this.rmdCount++;
        this.xxx = this.xxx0;
        this.yyy = this.yyy0;
        this.Len_R = this.Len_Rb;
        this.Len_L = this.Len_Lb;
        this.delta_move = this.delta_move_b;
        if (this.rmdCount <= 5) {
            return true;
        }
        this.rmdCount = 0;
        if (isSendNoti) {
            PNFMessageHandler(6);
        }
        if (!this.bClicked) {
            return false;
        }
        this.bClicked = false;
        this.PenStatus = 3;
        return false;
    }

    public void _readData(byte[] recvBuf) {
        this.temperature = recvBuf[13] & 63;
        for (int i = 0; i < 2; i++) {
            PenDataClass penData = new PenDataClass();
            int pressure;
            int func;
            int b;
            float x_gap;
            float y_gap;
            Iterator it;
            PenDataClass pen;
            if (this.hardwareData.modelCode == 4) {
                pressure = 100;
                func = 0;
                if (i == 0) {
                    this.Len_R = byteToShort(recvBuf[1], recvBuf[2]);
                    this.Len_L = byteToShort(recvBuf[3], recvBuf[4] & TransportMediator.KEYCODE_MEDIA_PAUSE);
                    this.mSMPenFlag = recvBuf[5];
                    this.mSMPenState = recvBuf[6];
                } else {
                    this.Len_R = byteToShort(recvBuf[7], recvBuf[8]);
                    this.Len_L = byteToShort(recvBuf[9], recvBuf[10] & TransportMediator.KEYCODE_MEDIA_PAUSE);
                    this.mSMPenFlag = recvBuf[11];
                    this.mSMPenState = recvBuf[12];
                }
                switch (this.mSMPenState) {
                    case 15:
                        if (this.up_cnt >= 1 || this.mSMPenStateOld == 89 || this.mSMPenStateOld == 92) {
                            b = 0;
                        } else {
                            this.mSMPenState = this.mSMPenStateOld;
                            b = 64;
                        }
                        this.up_cnt++;
                        break;
                    case 81:
                    case 82:
                    case 83:
                    case 84:
                    case 86:
                    case 88:
                    case 89:
                    case 92:
                        b = 64;
                        this.up_cnt = 0;
                        if (this.bClicked && this.mSMPenStateOld != this.mSMPenState) {
                            this.bClicked = false;
                            if (this.OldpenData != null) {
                                if (this.OldpenData.PenStatus == 1 || this.OldpenData.PenStatus == 2) {
                                    this.mSMPenStateOld = this.OldpenData.MakerPenStatus;
                                    this.OldpenData.PenStatus = 3;
                                    PNFEventHandler(this.OldpenData.PenStatus, this.OldpenData.ptRaw.x, this.OldpenData.ptRaw.y, this.OldpenData);
                                }
                                this.OldpenData = null;
                            }
                            this.livepenlist = new ArrayList();
                            break;
                        }
                    default:
                        break;
                }
                penData.MakerPenStatus = this.mSMPenState;
                this.mSMPenStateOld = this.mSMPenState;
                boolean setBeforeData = false;
                if (this.Len_R < this.X_MIN_RANGE_WARN || this.Len_R > this.X_MAX_RANGE_WARN || this.Len_L < this.Y_MIN_RANGE_WARN || this.Len_L > this.Y_MAX_RANGE_WARN) {
                    if (this.Len_R < this.X_MIN_RANGE_IGNORE || this.Len_R > this.X_MAX_RANGE_IGNORE || this.Len_L < this.Y_MIN_RANGE_IGNORE || this.Len_L > this.Y_MAX_RANGE_IGNORE) {
                        if (this.bClicked) {
                            b = 0;
                            setBeforeData = true;
                            this.livepenlist = new ArrayList();
                        } else {
                            this.bClicked = false;
                            if (this.OldpenData != null) {
                                if (this.OldpenData.PenStatus == 1 || this.OldpenData.PenStatus == 2) {
                                    this.mSMPenStateOld = this.OldpenData.MakerPenStatus;
                                    this.OldpenData.PenStatus = 3;
                                    PNFEventHandler(this.OldpenData.PenStatus, this.OldpenData.ptRaw.x, this.OldpenData.ptRaw.y, this.OldpenData);
                                }
                                this.OldpenData = null;
                            }
                            this.livepenlist = new ArrayList();
                        }
                    } else if (this.bClicked) {
                        setBeforeData = true;
                        this.livepenlist = new ArrayList();
                        PNFMessageHandler(6);
                    } else {
                        this.bClicked = false;
                        if (this.OldpenData != null) {
                            if (this.OldpenData.PenStatus == 1 || this.OldpenData.PenStatus == 2) {
                                this.mSMPenStateOld = this.OldpenData.MakerPenStatus;
                                this.OldpenData.PenStatus = 3;
                                PNFEventHandler(this.OldpenData.PenStatus, this.OldpenData.ptRaw.x, this.OldpenData.ptRaw.y, this.OldpenData);
                            }
                            this.OldpenData = null;
                        }
                        this.livepenlist = new ArrayList();
                    }
                }
                switch (this.hardwareData.stationPosition) {
                    case 1:
                        this.xxx = this.Len_L;
                        this.yyy = this.Len_R;
                        penData.StationPosition = 1;
                        break;
                    case 2:
                        this.xxx = this.Len_R;
                        this.yyy = (int) (66535.0f - ((float) this.Len_L));
                        penData.StationPosition = 2;
                        break;
                    case 3:
                        this.yyy = this.Len_L;
                        this.xxx = (int) (66535.0f - ((float) this.Len_R));
                        penData.StationPosition = 3;
                        break;
                    case 4:
                        this.xxx = (int) (66535.0f - ((float) this.Len_L));
                        this.yyy = (int) (66535.0f - ((float) this.Len_R));
                        penData.StationPosition = 4;
                        break;
                    default:
                        this.xxx = this.Len_R;
                        this.yyy = (int) (66535.0f - ((float) this.Len_L));
                        penData.StationPosition = 2;
                        break;
                }
                this.rmd_const = 600;
                if (b == 64 && 100 <= 25) {
                    b = 0;
                }
                if (setBeforeData) {
                    this.xxx = this.xxx0;
                    this.yyy = this.yyy0;
                }
                if (this.hardwareData.modelCode == 4 || b != 64) {
                    if (b != 1) {
                        if (this.bClicked) {
                            if (this.OldpenData != null) {
                                if (this.OldpenData.PenStatus == 1 || this.OldpenData.PenStatus == 2) {
                                    this.mSMPenStateOld = this.OldpenData.MakerPenStatus;
                                    this.OldpenData.PenStatus = 3;
                                    PNFEventHandler(this.OldpenData.PenStatus, this.OldpenData.ptRaw.x, this.OldpenData.ptRaw.y, this.OldpenData);
                                }
                                this.OldpenData = null;
                            }
                            this.livepenlist = new ArrayList();
                            INIT_PEN_DOWN();
                        } else {
                            this.PenStatus = 2;
                        }
                    } else if (this.bClicked) {
                        if (this.OldpenData != null) {
                            if (this.OldpenData.PenStatus == 1 || this.OldpenData.PenStatus == 2) {
                                this.mSMPenStateOld = this.OldpenData.MakerPenStatus;
                                this.OldpenData.PenStatus = 3;
                                PNFEventHandler(this.OldpenData.PenStatus, this.OldpenData.ptRaw.x, this.OldpenData.ptRaw.y, this.OldpenData);
                            }
                            this.OldpenData = null;
                        }
                        this.livepenlist = new ArrayList();
                        this.PenStatus = 4;
                        if (func != 2) {
                            if (!this.specialKeyDown) {
                                if (!this.isGesture) {
                                    this.m_ptGestureList.add(new Point(this.xxx, this.yyy));
                                    if (this.m_ptGestureList.size() != 1) {
                                        this.PenStatus = 5;
                                    } else {
                                        this.PenStatus = 6;
                                    }
                                    if (this.m_ptGestureList.size() > 10) {
                                        switch (GetGesture()) {
                                            case 22:
                                                PNFMessageHandler(22);
                                                this.isGesture = true;
                                                if (this.dbCkTimer != null) {
                                                    this.dbCkTimer.cancel();
                                                    this.dbCkTimer = null;
                                                }
                                                this.m_ptGestureList.clear();
                                                this.clickCnt = 0;
                                                break;
                                            case 23:
                                                PNFMessageHandler(23);
                                                this.isGesture = true;
                                                if (this.dbCkTimer != null) {
                                                    this.dbCkTimer.cancel();
                                                    this.dbCkTimer = null;
                                                }
                                                this.m_ptGestureList.clear();
                                                this.clickCnt = 0;
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                }
                            }
                            this.isGesture = false;
                            this.specialKeyDown = true;
                            this.m_ptGestureList.clear();
                            if (this.dbCkTimer != null) {
                                this.clickCnt = 1;
                                DBCK_TIMER_RESTART();
                            } else if (this.clickCnt == 1) {
                                this.clickCnt = 2;
                            }
                        } else if (this.specialKeyDown) {
                            this.specialKeyDown = false;
                            if (this.dbCkTimer != null) {
                                this.clickCnt = 0;
                                this.m_ptGestureList.clear();
                            } else if (this.clickCnt != 2) {
                                if (this.dbCkTimer != null) {
                                    this.dbCkTimer.cancel();
                                    this.dbCkTimer = null;
                                }
                                this.clickCnt = 0;
                                PNFMessageHandler(26);
                            } else {
                                PNFMessageHandler(25);
                            }
                        }
                    } else {
                        this.bClicked = false;
                        this.PenStatus = 3;
                    }
                } else if (this.bClicked) {
                    this.PenStatus = 2;
                } else {
                    if (this.OldpenData != null) {
                        if (this.OldpenData.PenStatus == 1 || this.OldpenData.PenStatus == 2) {
                            this.mSMPenStateOld = this.OldpenData.MakerPenStatus;
                            this.OldpenData.PenStatus = 3;
                            PNFEventHandler(this.OldpenData.PenStatus, this.OldpenData.ptRaw.x, this.OldpenData.ptRaw.y, this.OldpenData);
                        }
                        this.OldpenData = null;
                    }
                    this.livepenlist = new ArrayList();
                    INIT_PEN_DOWN();
                }
                x_gap = (float) Math.abs(this.xxx - this.xxx0);
                y_gap = (float) Math.abs(this.yyy - this.yyy0);
                this.xsqr = (int) (x_gap * x_gap);
                this.ysqr = (int) (y_gap * y_gap);
                this.delta_move = (int) Math.sqrt((double) (this.xsqr + this.ysqr));
                this.rmd = Math.abs(this.delta_move - this.delta_move_b);
                if (!CHECK_RMD_WITH_SEND_NOTI(true)) {
                    this.delta_move_b = this.delta_move;
                    this.xxx = ((this.xxx + this.xxx) + this.xxx0) / 3;
                    this.yyy = ((this.yyy + this.yyy) + this.yyy0) / 3;
                    this.xxx1 = this.xxx0;
                    this.yyy1 = this.yyy0;
                    this.xxx0 = this.xxx;
                    this.yyy0 = this.yyy;
                    this.Len_Rb = this.Len_R;
                    this.Len_Lb = this.Len_L;
                    this.angle1 = this.angle0;
                    if (this.hardwareData.modelCode == 4) {
                        if (!(this.mSMPenFlagOld == (byte) -1 || (this.mSMPenFlagOld & 1) == (this.mSMPenFlag & 1))) {
                            if (!this.bClicked) {
                                this.mSMPenFlag = this.mSMPenFlagOld;
                            } else if (this.ptRaw.x == this.xxx) {
                                this.mSMPenFlag = this.mSMPenFlagOld;
                            }
                        }
                        if ((this.mSMPenFlag & 1) != 1) {
                            if (this.mSMPenSwitch == 0) {
                                this.bClicked = false;
                                penData.bRight = false;
                                penData.pressure = pressure;
                                penData.PenStatus = 3;
                                penData.Temperature = this.temperature;
                                this.livepenlist.add(penData);
                                this.bLiveChangePage = this.bLiveChangePage;
                                if (!this.bLiveChangePage) {
                                    this.livepenlist = new ArrayList();
                                }
                            }
                            this.mSMPenSwitch = 1;
                            penData.bRight = true;
                        } else {
                            if (this.mSMPenSwitch == 1) {
                                this.bClicked = false;
                                penData.bRight = true;
                                penData.pressure = pressure;
                                penData.PenStatus = 3;
                                penData.Temperature = this.temperature;
                                this.livepenlist.add(penData);
                                this.bLiveChangePage = this.bLiveChangePage;
                                if (!this.bLiveChangePage) {
                                    this.livepenlist = new ArrayList();
                                }
                            }
                            this.mSMPenSwitch = 0;
                            penData.bRight = false;
                        }
                        this.mSMPenFlagOld = this.mSMPenFlag;
                        this.PenOldStatus = this.PenStatus;
                    }
                    this.ptRaw.x = this.xxx;
                    this.ptRaw.y = this.yyy;
                    penData.ptRaw.x = (float) this.ptRaw.x;
                    penData.ptRaw.y = (float) this.ptRaw.y;
                    penData.PenStatus = this.PenStatus;
                    penData.Temperature = this.temperature;
                    if (this.hardwareData.modelCode >= 2) {
                        penData.pressure = 1;
                    } else {
                        penData.pressure = pressure;
                    }
                    if (this.hardwareData.modelCode != 4) {
                        if (penData.PenStatus != 4) {
                            this.livepenlist.add(penData);
                        }
                        if (!this.bLiveChangePage && this.livepenlist.size() >= 10) {
                            it = this.livepenlist.iterator();
                            while (it.hasNext()) {
                                pen = (PenDataClass) it.next();
                                PNFEventHandler(pen.PenStatus, pen.ptRaw.x, pen.ptRaw.y, pen);
                            }
                            this.livepenlist = new ArrayList();
                            this.bLiveChangePage = false;
                        } else if (!this.bLiveChangePage) {
                            it = this.livepenlist.iterator();
                            while (it.hasNext()) {
                                pen = (PenDataClass) it.next();
                                PNFEventHandler(pen.PenStatus, pen.ptRaw.x, pen.ptRaw.y, pen);
                            }
                            this.livepenlist = new ArrayList();
                        }
                    } else {
                        PNFEventHandler(this.PenStatus, (float) this.ptRaw.x, (float) this.ptRaw.y, penData);
                    }
                    this.OldpenData = new PenDataClass();
                    this.OldpenData = penData;
                }
            } else {
                if (i == 0) {
                    this.Len_R = byteToShort(recvBuf[1], recvBuf[2] & 63);
                    this.Len_L = byteToShort(recvBuf[3], recvBuf[4] & 63);
                    b = recvBuf[5] & 1;
                    func = recvBuf[5] & 2;
                    pressure = byteToShort(((recvBuf[5] & 224) >> 5) | ((recvBuf[6] & 31) << 3), (recvBuf[6] & 224) >> 5);
                } else {
                    this.Len_R = byteToShort(recvBuf[7], recvBuf[8] & 63);
                    this.Len_L = byteToShort(recvBuf[9], recvBuf[10] & 63);
                    b = recvBuf[11] & 1;
                    func = recvBuf[11] & 2;
                    pressure = byteToShort(((recvBuf[11] & 224) >> 5) | ((recvBuf[12] & 31) << 3), (recvBuf[12] & 224) >> 5);
                }
                this.dbuf = (double) (((float) this.Len_R) - this.adjust_val_r);
                this.l_sqr = this.dbuf * this.dbuf;
                this.dbuf = (double) (((float) this.Len_L) - this.adjust_val_l);
                this.dbuf *= this.dbuf;
                this.xbuf = ((((double) this.sd_sqr) + this.l_sqr) - this.dbuf) / ((double) this.sd2);
                this.dbuf = this.xbuf * this.xbuf;
                this.ybuf = Math.abs(this.l_sqr - this.dbuf);
                this.ybuf = Math.sqrt(this.ybuf);
                this.xxx = (int) (this.xbuf + 3000.0d);
                this.yyy = (int) (this.ybuf + 200.0d);
                this.rmd_const = 600;
                if (this.hardwareData.modelCode == 4) {
                }
                if (b != 1) {
                    if (this.bClicked) {
                        if (this.OldpenData != null) {
                            this.mSMPenStateOld = this.OldpenData.MakerPenStatus;
                            this.OldpenData.PenStatus = 3;
                            PNFEventHandler(this.OldpenData.PenStatus, this.OldpenData.ptRaw.x, this.OldpenData.ptRaw.y, this.OldpenData);
                            this.OldpenData = null;
                        }
                        this.livepenlist = new ArrayList();
                        this.PenStatus = 4;
                        if (func != 2) {
                            if (this.specialKeyDown) {
                                this.specialKeyDown = false;
                                if (this.dbCkTimer != null) {
                                    this.clickCnt = 0;
                                    this.m_ptGestureList.clear();
                                } else if (this.clickCnt != 2) {
                                    PNFMessageHandler(25);
                                } else {
                                    if (this.dbCkTimer != null) {
                                        this.dbCkTimer.cancel();
                                        this.dbCkTimer = null;
                                    }
                                    this.clickCnt = 0;
                                    PNFMessageHandler(26);
                                }
                            }
                        } else if (!this.specialKeyDown) {
                            if (this.isGesture) {
                                this.m_ptGestureList.add(new Point(this.xxx, this.yyy));
                                if (this.m_ptGestureList.size() != 1) {
                                    this.PenStatus = 6;
                                } else {
                                    this.PenStatus = 5;
                                }
                                if (this.m_ptGestureList.size() > 10) {
                                    switch (GetGesture()) {
                                        case 22:
                                            PNFMessageHandler(22);
                                            this.isGesture = true;
                                            if (this.dbCkTimer != null) {
                                                this.dbCkTimer.cancel();
                                                this.dbCkTimer = null;
                                            }
                                            this.m_ptGestureList.clear();
                                            this.clickCnt = 0;
                                            break;
                                        case 23:
                                            PNFMessageHandler(23);
                                            this.isGesture = true;
                                            if (this.dbCkTimer != null) {
                                                this.dbCkTimer.cancel();
                                                this.dbCkTimer = null;
                                            }
                                            this.m_ptGestureList.clear();
                                            this.clickCnt = 0;
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }
                        } else {
                            this.isGesture = false;
                            this.specialKeyDown = true;
                            this.m_ptGestureList.clear();
                            if (this.dbCkTimer != null) {
                                this.clickCnt = 1;
                                DBCK_TIMER_RESTART();
                            } else if (this.clickCnt == 1) {
                                this.clickCnt = 2;
                            }
                        }
                    } else {
                        this.bClicked = false;
                        this.PenStatus = 3;
                    }
                } else if (this.bClicked) {
                    if (this.OldpenData != null) {
                        this.mSMPenStateOld = this.OldpenData.MakerPenStatus;
                        this.OldpenData.PenStatus = 3;
                        PNFEventHandler(this.OldpenData.PenStatus, this.OldpenData.ptRaw.x, this.OldpenData.ptRaw.y, this.OldpenData);
                        this.OldpenData = null;
                    }
                    this.livepenlist = new ArrayList();
                    INIT_PEN_DOWN();
                } else {
                    this.PenStatus = 2;
                }
                x_gap = (float) Math.abs(this.xxx - this.xxx0);
                y_gap = (float) Math.abs(this.yyy - this.yyy0);
                this.xsqr = (int) (x_gap * x_gap);
                this.ysqr = (int) (y_gap * y_gap);
                this.delta_move = (int) Math.sqrt((double) (this.xsqr + this.ysqr));
                this.rmd = Math.abs(this.delta_move - this.delta_move_b);
                if (!CHECK_RMD_WITH_SEND_NOTI(true)) {
                    this.delta_move_b = this.delta_move;
                    this.xxx = ((this.xxx + this.xxx) + this.xxx0) / 3;
                    this.yyy = ((this.yyy + this.yyy) + this.yyy0) / 3;
                    this.xxx1 = this.xxx0;
                    this.yyy1 = this.yyy0;
                    this.xxx0 = this.xxx;
                    this.yyy0 = this.yyy;
                    this.Len_Rb = this.Len_R;
                    this.Len_Lb = this.Len_L;
                    this.angle1 = this.angle0;
                    if (this.hardwareData.modelCode == 4) {
                        if (!this.bClicked) {
                            this.mSMPenFlag = this.mSMPenFlagOld;
                        } else if (this.ptRaw.x == this.xxx) {
                            this.mSMPenFlag = this.mSMPenFlagOld;
                        }
                        if ((this.mSMPenFlag & 1) != 1) {
                            if (this.mSMPenSwitch == 1) {
                                this.bClicked = false;
                                penData.bRight = true;
                                penData.pressure = pressure;
                                penData.PenStatus = 3;
                                penData.Temperature = this.temperature;
                                this.livepenlist.add(penData);
                                if (this.bLiveChangePage) {
                                }
                                this.bLiveChangePage = this.bLiveChangePage;
                                if (this.bLiveChangePage) {
                                    this.livepenlist = new ArrayList();
                                }
                            }
                            this.mSMPenSwitch = 0;
                            penData.bRight = false;
                        } else {
                            if (this.mSMPenSwitch == 0) {
                                this.bClicked = false;
                                penData.bRight = false;
                                penData.pressure = pressure;
                                penData.PenStatus = 3;
                                penData.Temperature = this.temperature;
                                this.livepenlist.add(penData);
                                if (this.bLiveChangePage) {
                                }
                                this.bLiveChangePage = this.bLiveChangePage;
                                if (this.bLiveChangePage) {
                                    this.livepenlist = new ArrayList();
                                }
                            }
                            this.mSMPenSwitch = 1;
                            penData.bRight = true;
                        }
                        this.mSMPenFlagOld = this.mSMPenFlag;
                        this.PenOldStatus = this.PenStatus;
                    }
                    this.ptRaw.x = this.xxx;
                    this.ptRaw.y = this.yyy;
                    penData.ptRaw.x = (float) this.ptRaw.x;
                    penData.ptRaw.y = (float) this.ptRaw.y;
                    penData.PenStatus = this.PenStatus;
                    penData.Temperature = this.temperature;
                    if (this.hardwareData.modelCode >= 2) {
                        penData.pressure = pressure;
                    } else {
                        penData.pressure = 1;
                    }
                    if (this.hardwareData.modelCode != 4) {
                        PNFEventHandler(this.PenStatus, (float) this.ptRaw.x, (float) this.ptRaw.y, penData);
                    } else {
                        if (penData.PenStatus != 4) {
                            this.livepenlist.add(penData);
                        }
                        if (!this.bLiveChangePage) {
                        }
                        if (this.bLiveChangePage) {
                            it = this.livepenlist.iterator();
                            while (it.hasNext()) {
                                pen = (PenDataClass) it.next();
                                PNFEventHandler(pen.PenStatus, pen.ptRaw.x, pen.ptRaw.y, pen);
                            }
                            this.livepenlist = new ArrayList();
                        }
                    }
                    this.OldpenData = new PenDataClass();
                    this.OldpenData = penData;
                }
            }
        }
    }

    void DBCK_TIMER_RESTART() {
        if (this.dbCkTimer == null) {
            this.dbCkTimer = new Timer();
            this.dbCkTimer.schedule(new TimerTask() {
                public void run() {
                    PNFBluetoothFreeCore.this.dbCkTimer.cancel();
                    PNFBluetoothFreeCore.this.dbCkTimer = null;
                }
            }, 500);
        }
    }

    public int GetGesture() {
        int ptLen = this.m_ptGestureList.size();
        if (ptLen < 9) {
            return 25;
        }
        if (ptLen < 10) {
            return 0;
        }
        Point[] pts = new Point[ptLen];
        Point tPt = new Point();
        int k = 0;
        for (int i = 0; i < ptLen - 2; i++) {
            if (i >= 5) {
                pts[k] = (Point) this.m_ptGestureList.get(i);
                k++;
            }
        }
        return GetGesture(pts, k);
    }

    int GetGesture(Point[] pts, int ptLen) {
        int i;
        float angle;
        int[] VX = new int[(ptLen - 1)];
        int[] VY = new int[(ptLen - 1)];
        int maxX = 0;
        int maxY = 0;
        int minX = 99999;
        int minY = 99999;
        int VPXCnt = 0;
        int VPYCnt = 0;
        int tempVPX = 0;
        int tempVPY = 0;
        Point[] VPX = new Point[ptLen];
        for (i = 0; i < VPX.length; i++) {
            VPX[i] = new Point();
        }
        Point[] VPY = new Point[ptLen];
        for (i = 0; i < VPY.length; i++) {
            VPY[i] = new Point();
        }
        boolean VPXOK = false;
        boolean VPYOK = false;
        for (i = 0; i < ptLen; i++) {
            if (maxX < pts[i].x) {
                maxX = pts[i].x;
            }
            if (maxY < pts[i].y) {
                maxY = pts[i].y;
            }
            if (minX > pts[i].x) {
                minX = pts[i].x;
            }
            if (minY > pts[i].y) {
                minY = pts[i].y;
            }
            if (i != 0) {
                int ii = i - 1;
                VX[ii] = pts[i].x - pts[i - 1].x;
                VY[ii] = pts[i].y - pts[i - 1].y;
                if (tempVPX == 0) {
                    tempVPX = VX[ii];
                } else if (tempVPX > 0) {
                    if (VX[ii] > 0) {
                        if (VPXOK) {
                            VPX[VPXCnt].x = i;
                            VPX[VPXCnt].y = VX[ii] - tempVPX;
                            VPXCnt++;
                            VPXOK = false;
                        }
                        tempVPX = VX[ii];
                    } else if (VX[ii] != 0) {
                        VPXOK = true;
                        tempVPX = VX[ii];
                    }
                } else if (VX[ii] > 0) {
                    VPXOK = true;
                    tempVPX = VX[ii];
                } else if (VX[ii] != 0) {
                    if (VPXOK) {
                        VPX[VPXCnt].x = i;
                        VPX[VPXCnt].y = VX[ii] - tempVPX;
                        VPXCnt++;
                        VPXOK = false;
                    }
                    tempVPX = VX[ii];
                }
                if (tempVPY == 0) {
                    tempVPY = VY[ii];
                } else if (tempVPY > 0) {
                    if (VY[ii] > 0) {
                        if (VPYOK) {
                            VPY[VPYCnt].x = i;
                            VPY[VPYCnt].y = VY[ii] - tempVPY;
                            VPYCnt++;
                            VPYOK = false;
                        }
                        tempVPY = VY[ii];
                    } else if (VY[ii] != 0) {
                        VPYOK = true;
                        tempVPY = VY[ii];
                    }
                } else if (VY[ii] > 0) {
                    VPYOK = true;
                    tempVPY = VY[ii];
                } else if (VY[ii] != 0) {
                    if (VPYOK) {
                        VPY[VPYCnt].x = i;
                        VPY[VPYCnt].y = VY[ii] - tempVPY;
                        VPYCnt++;
                        VPYOK = false;
                    }
                    tempVPY = VY[ii];
                }
            }
        }
        float cX = ((float) (minX + maxX)) / 2.0f;
        float cY = ((float) (minY + maxY)) / 2.0f;
        boolean z = false;
        boolean z2 = false;
        boolean bDoubleCrossDown = false;
        boolean bDoubleCrossUp = false;
        boolean bDoubleCrossLeft = false;
        boolean bDoubleCrossRight = false;
        for (i = 0; i < ptLen; i++) {
            if (i < ptLen - 1) {
                float tan = 1.0E9f;
                if (VX[i] != 0) {
                    tan = ((float) VY[i]) / ((float) VX[i]);
                } else if (VY[i] == 0) {
                }
                if (((double) Math.abs(tan)) >= 3.0d) {
                }
                if (VX[i] > 0) {
                }
                if (VX[i] < 0) {
                }
                if (VY[i] > 0) {
                }
                if (VY[i] < 0) {
                }
            }
        }
        Point CP = new Point();
        CP.x = (int) cX;
        CP.y = (int) cY;
        float AngleOld = 0.0f;
        if (ptLen < 12) {
            z = false;
            z2 = false;
        } else {
            float AngleSum = 0.0f;
            for (i = 1; i < ptLen; i++) {
                float AngleNew = GetAngleOldPoints(pts[i - 1], pts[i], CP);
                AngleSum += AngleNew;
                if (i == 1) {
                    AngleOld = AngleNew;
                } else if (AngleOld == 0.0f) {
                    AngleOld = AngleNew;
                } else if (AngleNew == 0.0f) {
                    continue;
                } else if (AngleOld > 0.0f && AngleNew > 0.0f) {
                    z2 = true;
                } else if (AngleOld >= 0.0f || AngleNew >= 0.0f) {
                    z = false;
                    z2 = false;
                    break;
                } else {
                    z = true;
                }
            }
            if (Math.abs(AngleSum) < 5.0f) {
                z = false;
                z2 = false;
            }
            if (Math.abs(AngleSum) > 10.0f) {
                z = false;
                z2 = false;
            }
        }
        if (VPYCnt == 1 && VPXCnt == 0) {
            angle = GetAngleOldPoints(pts[0], pts[ptLen - 1], pts[VPY[0].x]);
            if (Math.abs(angle) <= 2.0f && angle != 0.0f) {
                if (VPY[0].y < 0) {
                }
            }
        }
        if (VPXCnt == 1 && VPYCnt == 0) {
            if (VPX[0].y < 0) {
            }
        }
        if (VPYCnt == 3 && VPXCnt < 2) {
            if (VPY[0].y > 0) {
                bDoubleCrossUp = true;
            } else if (VPY[0].y < 0) {
                bDoubleCrossDown = true;
            }
            if (bDoubleCrossDown || bDoubleCrossUp) {
                angle = GetAngleOldPoints(pts[0], pts[VPY[1].x], pts[VPY[0].x]);
                if (Math.abs(angle) <= 3.0f && angle != 0.0f) {
                    angle = GetAngleOldPoints(pts[VPY[1].x], pts[ptLen - 1], pts[VPY[2].x]);
                    if (Math.abs(angle) > 3.0f || angle == 0.0f) {
                    }
                }
            }
        }
        if (VPXCnt == 3 && VPYCnt < 2) {
            if (VPX[0].y > 0) {
                bDoubleCrossLeft = true;
            } else if (VPX[0].y < 0) {
                bDoubleCrossRight = true;
            }
            if (bDoubleCrossLeft || bDoubleCrossRight) {
                angle = GetAngleOldPoints(pts[0], pts[VPX[1].x], pts[VPX[0].x]);
                if (Math.abs(angle) <= 3.0f && angle != 0.0f) {
                    angle = GetAngleOldPoints(pts[VPX[1].x], pts[ptLen - 1], pts[VPX[2].x]);
                    if (Math.abs(angle) > 3.0f || angle == 0.0f) {
                    }
                }
            }
        }
        if (VPXCnt == 2) {
            if (VPX[0].y >= 0 || VPX[1].y <= 0) {
            }
        }
        if (z) {
            return 22;
        }
        if (z2) {
            return 23;
        }
        return 0;
    }

    PointF GetVector(Point pt1, Point pt2) {
        return new PointF((float) (pt2.x - pt1.x), (float) (pt2.y - pt1.y));
    }

    float GetLengthPoints(Point pt1, Point pt2) {
        return (float) Math.sqrt((double) (((pt1.x - pt2.x) * (pt1.x - pt2.x)) + ((pt1.y - pt2.y) * (pt1.y - pt2.y))));
    }

    float GetAngleOldPoints(Point pt1, Point pt2, Point ptCenter) {
        float d1 = GetLengthPoints(pt2, pt1);
        float d2 = GetLengthPoints(pt2, ptCenter);
        float d3 = GetLengthPoints(pt1, ptCenter);
        if (d1 <= 0.0f || d2 <= 0.0f || d3 <= 0.0f) {
            return 0.0f;
        }
        float ag = (float) Math.acos(((double) (((d3 * d3) + (d2 * d2)) - (d1 * d1))) / ((2.0d * ((double) d3)) * ((double) d2)));
        PointF vCE = GetVector(ptCenter, pt2);
        PointF vCS = GetVector(ptCenter, pt1);
        if ((vCE.x * vCS.y) - (vCE.y * vCS.x) < 0.0f) {
            ag *= -1.0f;
        }
        return ag;
    }

    public short byteToShort(int j, int i) {
        return (short) ((j & 255) + ((short) (((i & 255) << 8) + (short) 0)));
    }

    public int byteToInteger(byte b) {
        return b & 255;
    }

    public PointF CalcOperation(int Len_L, int Len_R) {
        double dbuf = (double) (((float) Len_R) - this.adjust_val_r);
        double l_sqr = dbuf * dbuf;
        dbuf = (double) (((float) Len_L) - this.adjust_val_l);
        double xbuf = ((((double) this.sd_sqr) + l_sqr) - (dbuf * dbuf)) / ((double) this.sd2);
        return new PointF((float) (xbuf + 3000.0d), (float) (Math.sqrt(Math.abs(l_sqr - (xbuf * xbuf))) + 200.0d));
    }

    int GetTransData(float xxx, float yyy, float xx, float yy) {
        return 1;
    }

    public static String toHexString(byte[] buf) {
        StringBuffer sb = new StringBuffer();
        for (byte b : buf) {
            sb.append(Integer.toHexString((b & 255) + 256).substring(1));
            sb.append(" ");
        }
        return sb.toString();
    }

    public void PNFEventHandler(int ev, float RawX, float RawY, PenDataClass penData) {
        PenDataClass sendPenData = new PenDataClass();
        sendPenData.PenStatus = ev;
        sendPenData.ptRaw.x = RawX;
        sendPenData.ptRaw.y = RawY;
        sendPenData.pressure = penData.pressure;
        sendPenData.MakerPenStatus = penData.MakerPenStatus;
        sendPenData.bRight = penData.bRight;
        if (this.readMode != 1 && this.m_ReadQue != null) {
            PenDataClass tempPenData;
            if (sendPenData.PenStatus == 1) {
                if (this.lastQuePenState == 1) {
                    sendPenData.PenStatus = 2;
                } else if (this.lastQuePenState == 2) {
                    tempPenData = new PenDataClass();
                    tempPenData.PenStatus = 3;
                    tempPenData.ptRaw.x = this.lastQuePenPt.x;
                    tempPenData.ptRaw.y = this.lastQuePenPt.y;
                    tempPenData.pressure = this.lastQuePenPressure;
                    tempPenData.MakerPenStatus = this.lastQueMakerPenStatus;
                    tempPenData.bRight = this.lastQuePenRight;
                    this.m_ReadQue.add(tempPenData);
                    sendPenData.PenStatus = 1;
                } else if (this.lastQuePenState == 3) {
                    sendPenData.PenStatus = 1;
                }
            } else if (sendPenData.PenStatus == 2) {
                if (this.lastQuePenState == 1) {
                    sendPenData.PenStatus = 2;
                } else if (this.lastQuePenState == 2) {
                    sendPenData.PenStatus = 2;
                } else if (this.lastQuePenState == 3) {
                    tempPenData = new PenDataClass();
                    tempPenData.PenStatus = 1;
                    tempPenData.ptRaw.x = this.lastQuePenPt.x;
                    tempPenData.ptRaw.y = this.lastQuePenPt.y;
                    tempPenData.pressure = this.lastQuePenPressure;
                    tempPenData.MakerPenStatus = this.lastQueMakerPenStatus;
                    tempPenData.bRight = this.lastQuePenRight;
                    this.m_ReadQue.add(tempPenData);
                    sendPenData.PenStatus = 2;
                }
            } else if (sendPenData.PenStatus == 3) {
                if (this.lastQuePenState == 1) {
                    tempPenData = new PenDataClass();
                    tempPenData.PenStatus = 2;
                    tempPenData.ptRaw.x = this.lastQuePenPt.x;
                    tempPenData.ptRaw.y = this.lastQuePenPt.y;
                    tempPenData.pressure = this.lastQuePenPressure;
                    tempPenData.MakerPenStatus = this.lastQueMakerPenStatus;
                    tempPenData.bRight = this.lastQuePenRight;
                    this.m_ReadQue.add(tempPenData);
                    sendPenData.PenStatus = 3;
                } else if (this.lastQuePenState == 2) {
                    sendPenData.PenStatus = 3;
                } else if (this.lastQuePenState == 3) {
                    sendPenData.PenStatus = 3;
                }
            }
            this.m_ReadQue.add(sendPenData);
            this.lastQuePenState = sendPenData.PenStatus;
            this.lastQuePenPt.x = RawX;
            this.lastQuePenPt.y = RawY;
            this.lastQuePenPressure = sendPenData.pressure;
            this.lastQueMakerPenStatus = sendPenData.MakerPenStatus;
            this.lastQuePenRight = sendPenData.bRight;
            if (this.lastQuePenState == 1) {
                startPenUPTimer();
            } else if (this.lastQuePenState == 2) {
                this.isPenUPData = true;
            } else if (this.lastQuePenState == 3) {
                stopPenUPTimer();
            }
        }
    }

    void stopPenUPTimer() {
        this.isPenUPData = false;
        if (this.penUPTask != null) {
            this.penUPTask.cancel();
            this.penUPTask = null;
        }
        if (this.penUPTimer != null) {
            this.penUPTimer.cancel();
            this.penUPTimer = null;
        }
    }

    void startPenUPTimer() {
        stopPenUPTimer();
        this.penUPTimer = new Timer();
        this.penUPTask = new TimerTask() {
            public void run() {
                if (PNFBluetoothFreeCore.this.isPenUPData) {
                    PNFBluetoothFreeCore.this.isPenUPData = false;
                    return;
                }
                PNFBluetoothFreeCore.this.stopPenUPTimer();
                PenDataClass tempPenData = new PenDataClass();
                tempPenData.PenStatus = 3;
                tempPenData.ptRaw.x = PNFBluetoothFreeCore.this.lastQuePenPt.x;
                tempPenData.ptRaw.y = PNFBluetoothFreeCore.this.lastQuePenPt.y;
                tempPenData.pressure = PNFBluetoothFreeCore.this.lastQuePenPressure;
                tempPenData.MakerPenStatus = PNFBluetoothFreeCore.this.lastQueMakerPenStatus;
                tempPenData.bRight = PNFBluetoothFreeCore.this.lastQuePenRight;
                PNFBluetoothFreeCore.this.m_ReadQue.add(tempPenData);
            }
        };
        this.penUPTimer.schedule(this.penUPTask, 400, 800);
    }

    void settingMSGFirstData() {
        int modelCode = this.hardwareData.modelCode;
        int mcu2Code = this.hardwareData.mcu2Code;
        if (modelCode < 4) {
            this.hardwareData.stationPosition = 1;
        } else {
            this.hardwareData.stationPosition = 2;
            if (mcu2Code < 5) {
                this.hardwareData.setFirmwareUpdate(true);
            } else {
                this.hardwareData.setFirmwareUpdate(false);
            }
        }
        this.hardwareData.setLastModelCode(modelCode);
    }

    public void PNFMessageHandler(int ev) {
        if (this.mMessageHandler != null) {
            this.mMessageHandler.obtainMessage(ev).sendToTarget();
        }
    }

    public void PNFEnvHandler(short _ir, short _us, int _alive) {
        if (this.mPenEnvHandler != null) {
            PenEnvDataClass penEnvData = new PenEnvDataClass();
            penEnvData.penAliveSec = _alive;
            penEnvData.Pen_Ir = _ir;
            penEnvData.Pen_Us = _us;
            this.mPenEnvHandler.obtainMessage(1, 0, 0, penEnvData).sendToTarget();
        }
    }

    public void PNFDIHandler(int ev) {
        PNFDIHandler(ev, 0, null);
    }

    public void PNFDIHandler(int ev, int di_Papersize, Object obj) {
        if (this.mPenDIHandler != null) {
            this.mPenDIHandler.obtainMessage(ev, di_Papersize, 0, obj).sendToTarget();
        }
    }

    public void PNFFuncHandler(int ev) {
        PNFFuncHandler(ev, 0, 0);
    }

    public void PNFFuncHandler(int ev, int _Station_Battery, int _Battery) {
        if (this.mPenFuncHandler != null) {
            this.mPenFuncHandler.obtainMessage(ev, _Station_Battery, _Battery).sendToTarget();
        }
    }
}
