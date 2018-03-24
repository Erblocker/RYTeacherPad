package com.netspace.library.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.netspace.library.application.MyiBaseApplication;

public class ShutdownReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Log.d("ShutdownReceiver", intent.getAction() + " received.");
        Log.d("ShutdownReceiver", "Shutdown received.");
        MyiBaseApplication.PowerOff = true;
    }
}
