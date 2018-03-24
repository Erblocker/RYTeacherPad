package com.stericson.RootTools.internal;

import android.content.Context;
import android.util.Log;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;
import java.io.IOException;

public class Runner extends Thread {
    private static final String LOG_TAG = "RootTools::Runner";
    String binaryName;
    Context context;
    String parameter;

    public Runner(Context context, String binaryName, String parameter) {
        this.context = context;
        this.binaryName = binaryName;
        this.parameter = parameter;
    }

    public void run() {
        String privateFilesPath = null;
        try {
            privateFilesPath = this.context.getFilesDir().getCanonicalPath();
        } catch (IOException e) {
            if (RootTools.debugMode) {
                Log.e(LOG_TAG, "Problem occured while trying to locate private files directory!");
            }
            e.printStackTrace();
        }
        if (privateFilesPath != null) {
            try {
                CommandCapture command = new CommandCapture(0, privateFilesPath + "/" + this.binaryName + " " + this.parameter);
                Shell.startRootShell().add(command);
                command.waitForFinish();
            } catch (Exception e2) {
            }
        }
    }
}
