package com.pnf.bt.lib;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Handler;
import android.view.WindowManager;
import java.util.ArrayList;

public class PNFPenController {
    int ProjectiveLevel = 4;
    boolean delayFlag = false;
    PNFBluetoothFreeCore g_BtFreeCore = null;
    int m_iPenPosition = 0;
    PNF4CalData pnf4CalData;
    PNF9CalData pnf9CalData;

    public PNFPenController(Context con) {
        this.g_BtFreeCore = new PNFBluetoothFreeCore(con);
        this.pnf4CalData = new PNF4CalData(con);
        this.pnf9CalData = new PNF9CalData(con);
        if (con.getResources().getConfiguration().locale.getCountry().equals("US") || con.getResources().getConfiguration().locale.getCountry().equals("CA")) {
            this.pnf4CalData.isLetterPaper = true;
            this.pnf9CalData.isLetterPaper = true;
            if (this.g_BtFreeCore != null) {
                this.g_BtFreeCore.setLetterPaper(true);
                return;
            }
            return;
        }
        this.pnf4CalData.isLetterPaper = false;
        this.pnf4CalData.isLetterPaper = false;
        if (this.g_BtFreeCore != null) {
            this.g_BtFreeCore.setLetterPaper(false);
        }
    }

    public int getLastModelCode() {
        if (this.g_BtFreeCore == null) {
            return 0;
        }
        return this.g_BtFreeCore.hardwareData.modelCode;
    }

    public boolean getFirmwareUpdate() {
        if (this.g_BtFreeCore == null) {
            return false;
        }
        return this.g_BtFreeCore.hardwareData.isFirmwareUpdate;
    }

    public void setConnectDelay(boolean _delayFlag) {
        this.delayFlag = _delayFlag;
    }

    public int getProjectiveLevel() {
        return this.ProjectiveLevel;
    }

    public void fixStationPosition(int nStationPosition) {
        if (this.g_BtFreeCore != null) {
            this.g_BtFreeCore.hardwareData.stationPosition = nStationPosition;
        }
    }

    public boolean getExistCaliData() {
        if (this.ProjectiveLevel == 4) {
            return this.pnf4CalData.isExistCaliData;
        }
        return this.pnf9CalData.isExistCaliData;
    }

    public void startPen() {
        this.g_BtFreeCore.startPen();
    }

    public void stopPen() {
        this.g_BtFreeCore.stopPen();
    }

    public void restartPen() {
        this.g_BtFreeCore.restartPen();
    }

    public void StartReadQ() {
        this.g_BtFreeCore.StartReadQ();
    }

    public void EndReadQ() {
        this.g_BtFreeCore.EndReadQ();
    }

    public void removeQ() {
        this.g_BtFreeCore.removeQ();
    }

    public void ClearQ() {
        this.g_BtFreeCore.ClearQ();
    }

    public PenDataClass ReadQ() {
        return this.g_BtFreeCore.ReadQ();
    }

    public void initPenUp() {
        this.g_BtFreeCore.initPenUp();
    }

    public void changeAudioMode() {
    }

    public void changeVolume() {
    }

    public void setCalibration(Context context) {
        Point actSize = new Point();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getSize(actSize);
        int modeCode = 0;
        if (this.g_BtFreeCore != null) {
            modeCode = this.g_BtFreeCore.hardwareData.lastModelCode;
        }
        if (this.ProjectiveLevel == 4) {
            this.pnf4CalData.loadCaliData(actSize.x, actSize.y, modeCode);
        } else {
            this.pnf9CalData.loadCaliData(actSize.x, actSize.y, modeCode);
        }
    }

    public int getModelCode() {
        return this.g_BtFreeCore.hardwareData.modelCode;
    }

    public int getMCU1() {
        return this.g_BtFreeCore.hardwareData.mcu1Code;
    }

    public int getMCU2() {
        return this.g_BtFreeCore.hardwareData.mcu2Code;
    }

    public int getHWVersion() {
        return this.g_BtFreeCore.hardwareData.hwVersion;
    }

    public byte getAudioMode() {
        return this.g_BtFreeCore.hardwareData.audioMode;
    }

    public byte getAudioVolum() {
        return this.g_BtFreeCore.hardwareData.audioVolum;
    }

    public int getStationPostion() {
        return this.g_BtFreeCore.hardwareData.stationPosition;
    }

    public void disconnectPen() {
        this.g_BtFreeCore.disConnection();
        this.g_BtFreeCore.isAppBackground = true;
    }

    public void SetRetObjForMsg(Handler mHandler) {
        if (this.g_BtFreeCore != null) {
            this.g_BtFreeCore.mMessageHandler = mHandler;
            this.g_BtFreeCore.isAppBackground = false;
        }
    }

    public void SetRetObjForEnv(Handler mHandler) {
        if (this.g_BtFreeCore != null) {
            if (!isPenMode()) {
                this.g_BtFreeCore.loadBluetoothDevice();
            }
            this.g_BtFreeCore.mPenEnvHandler = mHandler;
            this.g_BtFreeCore.isAppBackground = false;
        }
    }

    public void SetRetObjForDI(Handler mHandler) {
        if (this.g_BtFreeCore != null) {
            this.g_BtFreeCore.mPenDIHandler = mHandler;
            this.g_BtFreeCore.isAppBackground = false;
        }
    }

    public void SetRetObjForFunc(Handler mHandler) {
        if (this.g_BtFreeCore != null) {
            this.g_BtFreeCore.mPenFuncHandler = mHandler;
            this.g_BtFreeCore.isAppBackground = false;
        }
    }

    public void sendAlertMsg() {
        if (this.g_BtFreeCore != null) {
            this.g_BtFreeCore.PNFDIHandler(11);
        }
    }

    public void sendAlertTempMsg() {
        if (this.g_BtFreeCore != null) {
            this.g_BtFreeCore.PNFDIHandler(10);
        }
    }

    public boolean isPenMode() {
        return this.g_BtFreeCore.mState == 5;
    }

    public boolean isBluetoothEnabled() {
        return this.g_BtFreeCore.isBluetoothEnabled();
    }

    public void setCloudConnect(boolean isCloud) {
        this.g_BtFreeCore.isCloudConnect = isCloud;
    }

    public boolean getCloudConnect() {
        return this.g_BtFreeCore.isCloudConnect;
    }

    public int setForceBondedDevices() {
        return this.g_BtFreeCore.setForceBondedDevices();
    }

    public void BTConnectForce() {
        this.g_BtFreeCore.BTConnect();
    }

    public void setCalibrationData(PointF[] calScreen, int margin, PointF[] calPen) {
        if (this.ProjectiveLevel == 4) {
            this.pnf4CalData.setCalibrationData(calScreen, margin, calPen);
        } else {
            this.pnf9CalData.setCalibrationData(calScreen, margin, calPen);
        }
    }

    public PointF getCoordinatePostionXY(float x, float y, boolean bRight) {
        PointF rtnPt = new PointF();
        if (this.ProjectiveLevel != 4) {
            return this.pnf9CalData.getPNFPerspective(x, y);
        }
        if (getModelCode() == 4) {
            return this.pnf4CalData.getPNFPerspective(x, y, this.g_BtFreeCore.hardwareData.stationPosition, bRight);
        }
        if (this.g_BtFreeCore.hardwareData.stationPosition == 4) {
            return this.pnf4CalData.getPNFPerspective(x, y, true);
        }
        return this.pnf4CalData.getPNFPerspective(x, y, false);
    }

    public PointF getCoordinatePostionXY(float x, float y, int StationPosition, boolean bRight) {
        PointF rtnPt = new PointF();
        if (this.ProjectiveLevel != 4) {
            return this.pnf9CalData.getPNFPerspective(x, y);
        }
        if (getModelCode() == 4) {
            return this.pnf4CalData.getPNFPerspective(x, y, StationPosition, bRight);
        }
        if (StationPosition == 4) {
            return this.pnf4CalData.getPNFPerspective(x, y, true);
        }
        return this.pnf4CalData.getPNFPerspective(x, y, false);
    }

    public void SetCalibrationAngle(int iTurnNumber) {
        if (this.ProjectiveLevel == 4) {
            switch (iTurnNumber) {
                case 1:
                    this.pnf4CalData.setCalibrationTurnLeft90Angle();
                    return;
                case 2:
                    this.pnf4CalData.setCalibrationTurnRight90Angle();
                    return;
                default:
                    return;
            }
        }
    }

    public void setCoordinateScale(double x, double y) {
        if (!(x == 1.0d && y == 1.0d) && this.ProjectiveLevel == 4) {
            this.pnf4CalData.mPerspectiveTransform.scale(x, y);
            this.pnf4CalData.saveCaliData();
        }
    }

    public void setCoordinateTranslate(double x, double y) {
        if (!(x == 0.0d && y == 0.0d) && this.ProjectiveLevel == 4) {
            this.pnf4CalData.mPerspectiveTransform.translate(x, y);
            this.pnf4CalData.saveCaliData();
        }
    }

    public void setCoordinateRotate(double theta, double x, double y) {
        if (this.ProjectiveLevel == 4) {
            this.pnf4CalData.mPerspectiveTransform.rotate(theta, x, y);
            this.pnf4CalData.saveCaliData();
        }
    }

    public void setDIState(int state) {
        this.g_BtFreeCore.setDIState(state);
    }

    public void ChangeRealMode() {
        this.g_BtFreeCore.appSessionStart();
    }

    public void ChangeT2Mode() {
        this.g_BtFreeCore.setDIState(13);
        this.g_BtFreeCore.ChangeT2Mode();
    }

    public int getDIFileCount() {
        return this.g_BtFreeCore.getDIFileCount();
    }

    public int getDIFileCount(int _folderIdx) {
        return this.g_BtFreeCore.getDIFileCount(_folderIdx);
    }

    public ArrayList<String> getAllDIData() {
        return this.g_BtFreeCore.getAllDIData();
    }

    public ArrayList<String> getDIFolderNameList() {
        return this.g_BtFreeCore.getDIFolderNameList();
    }

    public ArrayList<String> getDIFileNameList(String _folderName) {
        return this.g_BtFreeCore.getDIFileNameList(_folderName);
    }

    public byte[] getSendDiByte(int _index) {
        if (this.g_BtFreeCore != null) {
            return this.g_BtFreeCore.getSendDiByte(_index);
        }
        return null;
    }

    public void OpenFileTobyte(int _index) {
        this.g_BtFreeCore.OpenFileTobyte(getSendDiByte(_index));
    }

    public void OpenFileTobyte(String _folderName, String _fileName) {
        int year = Integer.valueOf(_folderName.substring(2, 4)).intValue();
        int index = 2 + 2;
        int month = Integer.valueOf(_folderName.substring(index, 6)).intValue();
        index += 2;
        int days = Integer.valueOf(_folderName.substring(index, 8)).intValue();
        index += 2;
        int hour = Integer.valueOf(_fileName.substring(0, 2)).intValue();
        index = 0 + 2;
        int minute = Integer.valueOf(_fileName.substring(index, 4)).intValue();
        index += 2;
        int second = Integer.valueOf(_fileName.substring(index, 6)).intValue();
        index += 2;
        this.g_BtFreeCore.OpenFileTobyte(new byte[]{(byte) year, (byte) month, (byte) days, (byte) hour, (byte) minute, (byte) second});
    }

    public void OpenFileTobyte(byte[] action) {
        this.g_BtFreeCore.OpenFileTobyte(action);
    }

    public void DeleteFileTobyte(String _folderName, String _fileName) {
        int year = Integer.valueOf(_folderName.substring(2, 4)).intValue();
        int index = 2 + 2;
        int month = Integer.valueOf(_folderName.substring(index, 6)).intValue();
        index += 2;
        int days = Integer.valueOf(_folderName.substring(index, 8)).intValue();
        index += 2;
        int hour = Integer.valueOf(_fileName.substring(0, 2)).intValue();
        index = 0 + 2;
        int minute = Integer.valueOf(_fileName.substring(index, 4)).intValue();
        index += 2;
        int second = Integer.valueOf(_fileName.substring(index, 6)).intValue();
        index += 2;
        this.g_BtFreeCore.DeleteFileTobyte(new byte[]{(byte) year, (byte) month, (byte) days, (byte) hour, (byte) minute, (byte) second});
    }

    public void DeleteFileTobyte(int _index) {
        this.g_BtFreeCore.DeleteFileTobyte(getSendDiByte(_index));
    }

    public void DeleteFileTobyte(byte[] action) {
        this.g_BtFreeCore.DeleteFileTobyte(action);
    }

    public void setbUseAcc(boolean bUseAcc) {
        this.g_BtFreeCore.bUseAcc = bUseAcc;
    }

    public void changeAudioMode(byte _mode) {
        this.g_BtFreeCore.changeAudioMode(_mode);
    }

    public void changeVolume(byte _volume) {
        this.g_BtFreeCore.changeVolume(_volume);
    }

    public void setAudio(byte audiomode, byte volume) {
        this.g_BtFreeCore.setAudio(audiomode, volume);
    }
}
