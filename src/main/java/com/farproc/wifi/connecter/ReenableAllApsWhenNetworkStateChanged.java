package com.farproc.wifi.connecter;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import java.util.List;

public class ReenableAllApsWhenNetworkStateChanged {

    public static class BackgroundService extends Service {
        private IntentFilter mIntentFilter;
        private BroadcastReceiver mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                    DetailedState detailed = ((NetworkInfo) intent.getParcelableExtra("networkInfo")).getDetailedState();
                    if (detailed != DetailedState.DISCONNECTED && detailed != DetailedState.DISCONNECTING && detailed != DetailedState.SCANNING && !BackgroundService.this.mReenabled) {
                        BackgroundService.this.mReenabled = true;
                        ReenableAllApsWhenNetworkStateChanged.reenableAllAps(context);
                        BackgroundService.this.stopSelf();
                    }
                }
            }
        };
        private boolean mReenabled;

        public IBinder onBind(Intent intent) {
            return null;
        }

        public void onCreate() {
            super.onCreate();
            this.mReenabled = false;
            this.mIntentFilter = new IntentFilter("android.net.wifi.STATE_CHANGE");
            registerReceiver(this.mReceiver, this.mIntentFilter);
        }

        public void onDestroy() {
            super.onDestroy();
            unregisterReceiver(this.mReceiver);
        }
    }

    public static void schedule(Context ctx) {
        ctx.startService(new Intent(ctx, BackgroundService.class));
    }

    private static void reenableAllAps(Context ctx) {
        WifiManager wifiMgr = (WifiManager) ctx.getSystemService("wifi");
        List<WifiConfiguration> configurations = wifiMgr.getConfiguredNetworks();
        if (configurations != null) {
            for (WifiConfiguration config : configurations) {
                wifiMgr.enableNetwork(config.networkId, false);
            }
        }
    }
}
