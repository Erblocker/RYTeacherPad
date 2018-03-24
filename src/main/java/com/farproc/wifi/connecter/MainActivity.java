package com.farproc.wifi.connecter;

import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import com.farproc.wifi.connecter.Floating.Content;
import com.netspace.pad.library.R;

public class MainActivity extends Floating {
    public static final String EXTRA_HOTSPOT = "com.farproc.wifi.connecter.extra.HOTSPOT";
    private Content mContent;
    private ScanResult mScanResult;
    private WifiManager mWifiManager;

    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        doNewIntent(intent);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mWifiManager = (WifiManager) getSystemService("wifi");
        doNewIntent(getIntent());
    }

    private boolean isAdHoc(ScanResult scanResule) {
        return scanResule.capabilities.indexOf("IBSS") != -1;
    }

    private void doNewIntent(Intent intent) {
        this.mScanResult = (ScanResult) intent.getParcelableExtra(EXTRA_HOTSPOT);
        if (this.mScanResult == null) {
            Toast.makeText(this, "No data in Intent!", 1).show();
            finish();
        } else if (isAdHoc(this.mScanResult)) {
            Toast.makeText(this, R.string.adhoc_not_supported_yet, 1).show();
            finish();
        } else {
            WifiConfiguration config = Wifi.getWifiConfiguration(this.mWifiManager, this.mScanResult, Wifi.ConfigSec.getScanResultSecurity(this.mScanResult));
            if (config == null) {
                this.mContent = new NewNetworkContent(this, this.mWifiManager, this.mScanResult);
            } else {
                boolean isCurrentNetwork_ConfigurationStatus;
                if (config.status == 0) {
                    isCurrentNetwork_ConfigurationStatus = true;
                } else {
                    isCurrentNetwork_ConfigurationStatus = false;
                }
                WifiInfo info = this.mWifiManager.getConnectionInfo();
                boolean isCurrentNetwork_WifiInfo;
                if (info != null && TextUtils.equals(info.getSSID(), this.mScanResult.SSID) && TextUtils.equals(info.getBSSID(), this.mScanResult.BSSID)) {
                    isCurrentNetwork_WifiInfo = true;
                } else {
                    isCurrentNetwork_WifiInfo = false;
                }
                if (isCurrentNetwork_ConfigurationStatus || isCurrentNetwork_WifiInfo) {
                    this.mContent = new CurrentNetworkContent(this, this.mWifiManager, this.mScanResult);
                } else {
                    this.mContent = new ConfiguredNetworkContent(this, this.mWifiManager, this.mScanResult);
                }
            }
            setContent(this.mContent);
        }
    }
}
