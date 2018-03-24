package com.netspace.library.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.dm.zbar.android.scanner.ZBarConstants;
import com.farproc.wifi.connecter.TestWifiScan;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.parser.ServerConfigurationParser;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;

public class WifiConfigActivity extends Activity implements OnClickListener {
    private final Runnable WifiCheckRunnable = new Runnable() {
        public void run() {
            boolean bWifiOK;
            NetworkInfo activeNetwork = ((ConnectivityManager) WifiConfigActivity.this.getSystemService("connectivity")).getActiveNetworkInfo();
            boolean isConnected = false;
            if (activeNetwork != null) {
                isConnected = activeNetwork.isConnectedOrConnecting();
            }
            if (!isConnected) {
                bWifiOK = false;
            } else if (activeNetwork.isConnected()) {
                bWifiOK = true;
            } else {
                bWifiOK = false;
            }
            TextView TextViewWifiFound = (TextView) WifiConfigActivity.this.m_Activity.findViewById(R.id.textViewWifiFound);
            if (bWifiOK) {
                WifiConfigActivity.this.m_Activity.findViewById(R.id.buttonNext).setEnabled(true);
                TextViewWifiFound.setText("检测到Wifi已成功连接");
            } else {
                WifiConfigActivity.this.m_Activity.findViewById(R.id.buttonNext).setEnabled(true);
                TextViewWifiFound.setText("没有检测到Wifi连接");
            }
            WifiConfigActivity.this.m_Handler.postDelayed(WifiConfigActivity.this.WifiCheckRunnable, 1000);
        }
    };
    private Activity m_Activity;
    private final Handler m_Handler = new Handler();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wificonfig);
        findViewById(R.id.buttonWifiConfig).setOnClickListener(this);
        findViewById(R.id.buttonNext).setOnClickListener(this);
        this.m_Activity = this;
        findViewById(R.id.buttonNext).setEnabled(false);
        if (MyiBaseApplication.canExecuteIPTableScript()) {
            ServerConfigurationParser ServerConfigurationParser = new ServerConfigurationParser();
            ServerConfigurationParser.generateHostLimitScript();
            ServerConfigurationParser.executeScripts();
        }
        this.m_Handler.postDelayed(this.WifiCheckRunnable, 1000);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.buttonWifiConfig) {
            startActivity(new Intent(this, TestWifiScan.class));
        } else if (v.getId() != R.id.buttonNext) {
        } else {
            if (Utilities.isCurrentUserOwner()) {
                Intent intent = new Intent(this, AccountConfigActivity.class);
                intent.putExtra(ZBarConstants.SCAN_MODES, new int[]{64});
                startActivity(intent);
                finish();
                return;
            }
            Utilities.showAlertMessage(this, "登录错误", "当前安装的用户不是这个平板的所有者，请重新在所有者账户下安装。");
        }
    }
}
