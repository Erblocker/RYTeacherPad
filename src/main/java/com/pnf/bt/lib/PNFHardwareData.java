package com.pnf.bt.lib;

import android.content.Context;
import android.content.SharedPreferences.Editor;

public class PNFHardwareData {
    final String PNFHardwareSharedPre = "PNFHardwareData";
    byte audioMode = (byte) 0;
    byte audioVolum = (byte) 0;
    int defaultModelCode = 0;
    int hwVersion = 0;
    boolean isFirmwareUpdate = false;
    int lastModelCode = 0;
    public Context mContext;
    int mcu1Code = 0;
    int mcu2Code = 0;
    int modelCode = 0;
    int oldStationPosition = 2;
    int stationPosition = 2;

    public PNFHardwareData(Context con) {
        this.mContext = con;
        loadHardwareData();
    }

    void setLastModelCode(int _lastModelCode) {
        this.lastModelCode = _lastModelCode;
        saveHardwareData();
    }

    void setFirmwareUpdate(boolean _isUpdate) {
        this.isFirmwareUpdate = _isUpdate;
    }

    void loadHardwareData() {
        this.lastModelCode = this.mContext.getSharedPreferences("PNFHardwareData", 0).getInt("lastModelCode", this.defaultModelCode);
    }

    void saveHardwareData() {
        Editor ePref = this.mContext.getSharedPreferences("PNFHardwareData", 0).edit();
        ePref.putInt("lastModelCode", this.lastModelCode);
        ePref.commit();
    }
}
