package com.farproc.wifi.connecter;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.provider.Settings.Secure;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import com.farproc.wifi.connecter.Floating.Content;
import com.netspace.pad.library.R;

public abstract class BaseContent implements Content, OnCheckedChangeListener {
    private static final int[] SIGNAL_LEVEL = new int[]{R.string.wifi_signal_0, R.string.wifi_signal_1, R.string.wifi_signal_2, R.string.wifi_signal_3};
    protected OnClickListener mCancelOnClick = new OnClickListener() {
        public void onClick(View v) {
            BaseContent.this.mFloating.finish();
        }
    };
    public OnClickListener mChangePasswordOnClick = new OnClickListener() {
        public void onClick(View v) {
            BaseContent.this.changePassword();
        }
    };
    protected final Floating mFloating;
    protected final boolean mIsOpenNetwork;
    protected int mNumOpenNetworksKept;
    protected final ScanResult mScanResult;
    protected final String mScanResultSecurity;
    protected View mView;
    protected final WifiManager mWifiManager;

    protected String getCancelString() {
        return this.mFloating.getString(17039360);
    }

    public BaseContent(Floating floating, WifiManager wifiManager, ScanResult scanResult) {
        String readableSecurity;
        this.mWifiManager = wifiManager;
        this.mFloating = floating;
        this.mScanResult = scanResult;
        this.mScanResultSecurity = Wifi.ConfigSec.getScanResultSecurity(this.mScanResult);
        this.mIsOpenNetwork = Wifi.ConfigSec.isOpenNetwork(this.mScanResultSecurity);
        this.mView = View.inflate(this.mFloating, R.layout.base_content, null);
        ((TextView) this.mView.findViewById(R.id.SignalStrength_TextView)).setText(SIGNAL_LEVEL[WifiManager.calculateSignalLevel(this.mScanResult.level, SIGNAL_LEVEL.length)]);
        String rawSecurity = Wifi.ConfigSec.getDisplaySecirityString(this.mScanResult);
        if (Wifi.ConfigSec.isOpenNetwork(rawSecurity)) {
            readableSecurity = this.mFloating.getString(R.string.wifi_security_open);
        } else {
            readableSecurity = rawSecurity;
        }
        ((TextView) this.mView.findViewById(R.id.Security_TextView)).setText(readableSecurity);
        ((CheckBox) this.mView.findViewById(R.id.ShowPassword_CheckBox)).setOnCheckedChangeListener(this);
        this.mNumOpenNetworksKept = Secure.getInt(floating.getContentResolver(), "wifi_num_open_networks_kept", 10);
    }

    public View getView() {
        return this.mView;
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int i;
        EditText editText = (EditText) this.mView.findViewById(R.id.Password_EditText);
        if (isChecked) {
            i = 144;
        } else {
            i = 128;
        }
        editText.setInputType(i | 1);
    }

    public void changePassword() {
        this.mFloating.setContent(new ChangePasswordContent(this.mFloating, this.mWifiManager, this.mScanResult));
    }
}
