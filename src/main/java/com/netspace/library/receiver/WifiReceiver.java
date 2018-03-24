package com.netspace.library.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.util.Log;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import org.greenrobot.eventbus.EventBus;

public class WifiReceiver extends BroadcastReceiver {

    public static class WifiConnect {
    }

    public static class WifiDisconnect {
    }

    public void onReceive(Context context, Intent intent) {
        Log.d("WifiReceiver", "onReceived");
        if (intent.getAction().equals("android.net.wifi.supplicant.CONNECTION_CHANGE")) {
            if (intent.getBooleanExtra("connected", false)) {
                EventBus.getDefault().post(new WifiConnect());
            } else {
                EventBus.getDefault().post(new WifiDisconnect());
            }
        } else if (!intent.getAction().equals("android.net.wifi.STATE_CHANGE")) {
        } else {
            if (((NetworkInfo) intent.getParcelableExtra("networkInfo")).isConnected()) {
                networkConnected();
                EventBus.getDefault().post(new WifiConnect());
                return;
            }
            EventBus.getDefault().post(new WifiDisconnect());
        }
    }

    private void networkConnected() {
        if (MyiBaseApplication.getCommonVariables().Session.isLoggedIn() && VirtualNetworkObject.getOfflineMode()) {
            VirtualNetworkObject.setOfflineMode(false);
            MyiBaseApplication.getCommonVariables().Session.relogin();
            Utilities.showToastMessage("离线模式下检测到网络连接，自动切换为在线模式", 0);
        }
    }
}
