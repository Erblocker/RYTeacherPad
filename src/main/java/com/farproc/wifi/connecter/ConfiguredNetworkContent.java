package com.farproc.wifi.connecter;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.netspace.pad.library.R;

public class ConfiguredNetworkContent extends BaseContent {
    private static final int MENU_CHANGE_PASSWORD = 1;
    private static final int MENU_FORGET = 0;
    private OnClickListener mConnectOnClick = new OnClickListener() {
        public void onClick(View v) {
            WifiConfiguration config = Wifi.getWifiConfiguration(ConfiguredNetworkContent.this.mWifiManager, ConfiguredNetworkContent.this.mScanResult, ConfiguredNetworkContent.this.mScanResultSecurity);
            boolean connResult = false;
            if (config != null) {
                connResult = Wifi.connectToConfiguredNetwork(ConfiguredNetworkContent.this.mFloating, ConfiguredNetworkContent.this.mWifiManager, config, false);
            }
            if (!connResult) {
                Toast.makeText(ConfiguredNetworkContent.this.mFloating, R.string.toastFailed, 1).show();
            }
            ConfiguredNetworkContent.this.mFloating.finish();
        }
    };
    private OnClickListener mForgetOnClick = new OnClickListener() {
        public void onClick(View v) {
            ConfiguredNetworkContent.this.forget();
        }
    };
    private OnClickListener mOpOnClick = new OnClickListener() {
        public void onClick(View v) {
            ConfiguredNetworkContent.this.mFloating.registerForContextMenu(v);
            ConfiguredNetworkContent.this.mFloating.openContextMenu(v);
            ConfiguredNetworkContent.this.mFloating.unregisterForContextMenu(v);
        }
    };

    public ConfiguredNetworkContent(Floating floating, WifiManager wifiManager, ScanResult scanResult) {
        super(floating, wifiManager, scanResult);
        this.mView.findViewById(R.id.Status).setVisibility(8);
        this.mView.findViewById(R.id.Speed).setVisibility(8);
        this.mView.findViewById(R.id.IPAddress).setVisibility(8);
        this.mView.findViewById(R.id.Password).setVisibility(8);
    }

    public int getButtonCount() {
        return 3;
    }

    public OnClickListener getButtonOnClickListener(int index) {
        switch (index) {
            case 0:
                return this.mConnectOnClick;
            case 1:
                if (this.mIsOpenNetwork) {
                    return this.mForgetOnClick;
                }
                return this.mOpOnClick;
            case 2:
                return this.mCancelOnClick;
            default:
                return null;
        }
    }

    public CharSequence getButtonText(int index) {
        switch (index) {
            case 0:
                return this.mFloating.getString(R.string.connect);
            case 1:
                if (this.mIsOpenNetwork) {
                    return this.mFloating.getString(R.string.forget_network);
                }
                return this.mFloating.getString(R.string.buttonOp);
            case 2:
                return getCancelString();
            default:
                return null;
        }
    }

    public CharSequence getTitle() {
        return this.mFloating.getString(R.string.wifi_connect_to, new Object[]{this.mScanResult.SSID});
    }

    private void forget() {
        WifiConfiguration config = Wifi.getWifiConfiguration(this.mWifiManager, this.mScanResult, this.mScanResultSecurity);
        boolean result = false;
        if (config != null) {
            result = this.mWifiManager.removeNetwork(config.networkId) && this.mWifiManager.saveConfiguration();
        }
        if (!result) {
            Toast.makeText(this.mFloating, R.string.toastFailed, 1).show();
        }
        this.mFloating.finish();
    }

    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                forget();
                break;
            case 1:
                changePassword();
                break;
        }
        return false;
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        menu.add(0, 0, 0, R.string.forget_network);
        menu.add(0, 1, 0, R.string.wifi_change_password);
    }
}
