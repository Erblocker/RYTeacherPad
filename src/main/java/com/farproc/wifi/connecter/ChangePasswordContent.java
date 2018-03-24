package com.farproc.wifi.connecter;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.netspace.pad.library.R;

public class ChangePasswordContent extends BaseContent {
    OnClickListener[] mOnClickListeners = new OnClickListener[]{this.mSaveOnClick, this.mCancelOnClick};
    private ChangingAwareEditText mPasswordEditText;
    private OnClickListener mSaveOnClick = new OnClickListener() {
        public void onClick(View v) {
            if (ChangePasswordContent.this.mPasswordEditText.getChanged()) {
                WifiConfiguration config = Wifi.getWifiConfiguration(ChangePasswordContent.this.mWifiManager, ChangePasswordContent.this.mScanResult, ChangePasswordContent.this.mScanResultSecurity);
                boolean saveResult = false;
                if (config != null) {
                    saveResult = Wifi.changePasswordAndConnect(ChangePasswordContent.this.mFloating, ChangePasswordContent.this.mWifiManager, config, ChangePasswordContent.this.mPasswordEditText.getText().toString(), ChangePasswordContent.this.mNumOpenNetworksKept);
                }
                if (!saveResult) {
                    Toast.makeText(ChangePasswordContent.this.mFloating, R.string.toastFailed, 1).show();
                }
            }
            ChangePasswordContent.this.mFloating.finish();
        }
    };

    public ChangePasswordContent(Floating floating, WifiManager wifiManager, ScanResult scanResult) {
        super(floating, wifiManager, scanResult);
        this.mView.findViewById(R.id.Status).setVisibility(8);
        this.mView.findViewById(R.id.Speed).setVisibility(8);
        this.mView.findViewById(R.id.IPAddress).setVisibility(8);
        this.mPasswordEditText = (ChangingAwareEditText) this.mView.findViewById(R.id.Password_EditText);
        ((TextView) this.mView.findViewById(R.id.Password_TextView)).setText(R.string.please_type_passphrase);
        ((EditText) this.mView.findViewById(R.id.Password_EditText)).setHint(R.string.wifi_password_unchanged);
    }

    public int getButtonCount() {
        return 2;
    }

    public OnClickListener getButtonOnClickListener(int index) {
        return this.mOnClickListeners[index];
    }

    public CharSequence getButtonText(int index) {
        switch (index) {
            case 0:
                return this.mFloating.getString(R.string.wifi_save_config);
            case 1:
                return getCancelString();
            default:
                return null;
        }
    }

    public CharSequence getTitle() {
        return this.mScanResult.SSID;
    }

    public boolean onContextItemSelected(MenuItem item) {
        return false;
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    }
}
