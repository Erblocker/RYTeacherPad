package com.farproc.wifi.connecter;

import android.net.wifi.ScanResult;
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

public class NewNetworkContent extends BaseContent {
    private OnClickListener mConnectOnClick = new OnClickListener() {
        public void onClick(View v) {
            boolean connResult;
            if (NewNetworkContent.this.mIsOpenNetwork) {
                connResult = Wifi.connectToNewNetwork(NewNetworkContent.this.mFloating, NewNetworkContent.this.mWifiManager, NewNetworkContent.this.mScanResult, null, NewNetworkContent.this.mNumOpenNetworksKept);
            } else {
                connResult = Wifi.connectToNewNetwork(NewNetworkContent.this.mFloating, NewNetworkContent.this.mWifiManager, NewNetworkContent.this.mScanResult, ((EditText) NewNetworkContent.this.mView.findViewById(R.id.Password_EditText)).getText().toString(), NewNetworkContent.this.mNumOpenNetworksKept);
            }
            if (!connResult) {
                Toast.makeText(NewNetworkContent.this.mFloating, R.string.toastFailed, 1).show();
            }
            NewNetworkContent.this.mFloating.finish();
        }
    };
    private boolean mIsOpenNetwork = false;
    private OnClickListener[] mOnClickListeners = new OnClickListener[]{this.mConnectOnClick, this.mCancelOnClick};

    public NewNetworkContent(Floating floating, WifiManager wifiManager, ScanResult scanResult) {
        super(floating, wifiManager, scanResult);
        this.mView.findViewById(R.id.Status).setVisibility(8);
        this.mView.findViewById(R.id.Speed).setVisibility(8);
        this.mView.findViewById(R.id.IPAddress).setVisibility(8);
        if (Wifi.ConfigSec.isOpenNetwork(this.mScanResultSecurity)) {
            this.mIsOpenNetwork = true;
            this.mView.findViewById(R.id.Password).setVisibility(8);
            return;
        }
        ((TextView) this.mView.findViewById(R.id.Password_TextView)).setText(R.string.please_type_passphrase);
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
                return this.mFloating.getText(R.string.connect);
            case 1:
                return getCancelString();
            default:
                return null;
        }
    }

    public CharSequence getTitle() {
        return this.mFloating.getString(R.string.wifi_connect_to, new Object[]{this.mScanResult.SSID});
    }

    public boolean onContextItemSelected(MenuItem item) {
        return false;
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    }
}
