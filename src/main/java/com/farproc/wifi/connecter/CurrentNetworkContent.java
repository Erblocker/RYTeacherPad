package com.farproc.wifi.connecter;

import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.view.MotionEventCompat;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import com.netspace.pad.library.R;

public class CurrentNetworkContent extends BaseContent {
    private OnClickListener mForgetOnClick = new OnClickListener() {
        public void onClick(View v) {
            WifiConfiguration config = Wifi.getWifiConfiguration(CurrentNetworkContent.this.mWifiManager, CurrentNetworkContent.this.mScanResult, CurrentNetworkContent.this.mScanResultSecurity);
            boolean result = false;
            if (config != null) {
                result = CurrentNetworkContent.this.mWifiManager.removeNetwork(config.networkId) && CurrentNetworkContent.this.mWifiManager.saveConfiguration();
            }
            if (!result) {
                Toast.makeText(CurrentNetworkContent.this.mFloating, R.string.toastFailed, 1).show();
            }
            CurrentNetworkContent.this.mFloating.finish();
        }
    };
    private OnClickListener[] mOnClickListeners = new OnClickListener[]{this.mForgetOnClick, this.mChangePasswordOnClick, this.mCancelOnClick};

    public CurrentNetworkContent(Floating floating, WifiManager wifiManager, ScanResult scanResult) {
        super(floating, wifiManager, scanResult);
        this.mView.findViewById(R.id.Status).setVisibility(8);
        this.mView.findViewById(R.id.Speed).setVisibility(8);
        this.mView.findViewById(R.id.IPAddress).setVisibility(8);
        this.mView.findViewById(R.id.Password).setVisibility(8);
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            Toast.makeText(this.mFloating, R.string.toastFailed, 1).show();
            return;
        }
        DetailedState detailedState = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
        if (detailedState == DetailedState.CONNECTED || (detailedState == DetailedState.OBTAINING_IPADDR && wifiInfo.getIpAddress() != 0)) {
            this.mView.findViewById(R.id.Status).setVisibility(0);
            this.mView.findViewById(R.id.Speed).setVisibility(0);
            this.mView.findViewById(R.id.IPAddress).setVisibility(0);
            ((TextView) this.mView.findViewById(R.id.Status_TextView)).setText(R.string.status_connected);
            ((TextView) this.mView.findViewById(R.id.LinkSpeed_TextView)).setText(wifiInfo.getLinkSpeed() + " " + "Mbps");
            ((TextView) this.mView.findViewById(R.id.IPAddress_TextView)).setText(getIPAddress(wifiInfo.getIpAddress()));
        } else if (detailedState == DetailedState.AUTHENTICATING || detailedState == DetailedState.CONNECTING || detailedState == DetailedState.OBTAINING_IPADDR) {
            this.mView.findViewById(R.id.Status).setVisibility(0);
            ((TextView) this.mView.findViewById(R.id.Status_TextView)).setText(R.string.status_connecting);
        }
    }

    public int getButtonCount() {
        return this.mIsOpenNetwork ? 2 : 3;
    }

    public OnClickListener getButtonOnClickListener(int index) {
        if (this.mIsOpenNetwork && index == 1) {
            return this.mOnClickListeners[2];
        }
        return this.mOnClickListeners[index];
    }

    public CharSequence getButtonText(int index) {
        switch (index) {
            case 0:
                return this.mFloating.getString(R.string.forget_network);
            case 1:
                if (this.mIsOpenNetwork) {
                    return getCancelString();
                }
                return this.mFloating.getString(R.string.button_change_password);
            case 2:
                return getCancelString();
            default:
                return null;
        }
    }

    public CharSequence getTitle() {
        return this.mScanResult.SSID;
    }

    private String getIPAddress(int address) {
        StringBuilder sb = new StringBuilder();
        sb.append(address & 255).append(".").append((MotionEventCompat.ACTION_POINTER_INDEX_MASK & address) >> 8).append(".").append((16711680 & address) >> 16).append(".").append((((long) address) & 4278190080L) >> 24);
        return sb.toString();
    }

    public boolean onContextItemSelected(MenuItem item) {
        return false;
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    }
}
