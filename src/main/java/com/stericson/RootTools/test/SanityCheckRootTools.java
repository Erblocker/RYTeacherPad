package com.stericson.RootTools.test;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ScrollView;
import android.widget.TextView;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.RootTools.Result;
import com.stericson.RootTools.containers.Permissions;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.exceptions.RootToolsException;
import com.stericson.RootTools.execution.Shell;
import io.vov.vitamio.MediaMetadataRetriever;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class SanityCheckRootTools extends Activity {
    private ProgressDialog mPDialog;
    private ScrollView mScrollView;
    private TextView mTextView;

    private class SanityCheckThread extends Thread {
        private Handler mHandler;

        public SanityCheckThread(Context context, Handler handler) {
            this.mHandler = handler;
        }

        public void run() {
            IOException e;
            RootToolsException e2;
            TimeoutException e3;
            visualUpdate(1, null);
            visualUpdate(4, "Testing Find Binary");
            boolean result = RootTools.isRootAvailable();
            visualUpdate(3, "[ Checking Root ]\n");
            visualUpdate(3, result + " k\n\n");
            visualUpdate(4, "Testing file exists");
            visualUpdate(3, "[ Checking Exists() ]\n");
            visualUpdate(3, RootTools.exists("/system/sbin/[") + " k\n\n");
            visualUpdate(4, "Testing Is Access Given");
            result = RootTools.isAccessGiven();
            visualUpdate(3, "[ Checking for Access to Root ]\n");
            visualUpdate(3, result + " k\n\n");
            visualUpdate(4, "Testing Remount");
            result = RootTools.remount("/system", "rw");
            visualUpdate(3, "[ Remounting System as RW ]\n");
            visualUpdate(3, result + " k\n\n");
            visualUpdate(4, "Testing CheckUtil");
            visualUpdate(3, "[ Checking busybox is setup ]\n");
            visualUpdate(3, RootTools.checkUtil("busybox") + " k\n\n");
            visualUpdate(4, "Testing getBusyBoxVersion");
            visualUpdate(3, "[ Checking busybox version ]\n");
            visualUpdate(3, RootTools.getBusyBoxVersion("/system/bin/") + " k\n\n");
            try {
                visualUpdate(4, "Testing fixUtils");
                visualUpdate(3, "[ Checking Utils ]\n");
                visualUpdate(3, RootTools.fixUtils(new String[]{"ls", "rm", "ln", "dd", "chmod", "mount"}) + " k\n\n");
            } catch (Exception e22) {
                e22.printStackTrace();
            }
            try {
                visualUpdate(4, "Testing getSymlink");
                visualUpdate(3, "[ Checking [[ for symlink ]\n");
                visualUpdate(3, RootTools.getSymlink("/system/bin/[[") + " k\n\n");
            } catch (Exception e222) {
                e222.printStackTrace();
            }
            visualUpdate(4, "Testing getInode");
            visualUpdate(3, "[ Checking Inodes ]\n");
            visualUpdate(3, RootTools.getInode("/system/bin/busybox") + " k\n\n");
            visualUpdate(4, "Testing GetBusyBoxapplets");
            try {
                visualUpdate(3, "[ Getting all available Busybox applets ]\n");
                for (String applet : RootTools.getBusyBoxApplets("/data/data/stericson.busybox.donate/files/bb")) {
                    visualUpdate(3, applet + " k\n\n");
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            visualUpdate(4, "Testing getFilePermissionsSymlinks");
            Permissions permissions = RootTools.getFilePermissionsSymlinks("/system/bin/busybox");
            visualUpdate(3, "[ Checking busybox permissions and symlink ]\n");
            if (permissions != null) {
                visualUpdate(3, "Symlink: " + permissions.getSymlink() + " k\n\n");
                visualUpdate(3, "Group Permissions: " + permissions.getGroupPermissions() + " k\n\n");
                visualUpdate(3, "Owner Permissions: " + permissions.getOtherPermissions() + " k\n\n");
                visualUpdate(3, "Permissions: " + permissions.getPermissions() + " k\n\n");
                visualUpdate(3, "Type: " + permissions.getType() + " k\n\n");
                visualUpdate(3, "User Permissions: " + permissions.getUserPermissions() + " k\n\n");
            } else {
                visualUpdate(3, "Permissions == null k\n\n");
            }
            visualUpdate(4, "Testing df");
            long spaceValue = RootTools.getSpace("/data");
            visualUpdate(3, "[ Checking /data partition size]\n");
            visualUpdate(3, spaceValue + "k\n\n");
            visualUpdate(4, "Testing sendShell() w/ return array");
            try {
                List<String> response = RootTools.sendShell("ls /", -1);
                visualUpdate(3, "[ Listing of / (passing a List)]\n");
                for (String line : response) {
                    visualUpdate(3, line + "\n");
                }
                visualUpdate(4, "Testing sendShell() w/ callbacks");
                try {
                    visualUpdate(3, "\n[ Listing of / (callback)]\n");
                    Result result2 = new Result() {
                        public void process(String line) throws Exception {
                            SanityCheckThread.this.visualUpdate(3, line + "\n");
                        }

                        public void onFailure(Exception ex) {
                            SanityCheckThread.this.visualUpdate(2, "ERROR: " + ex);
                            setError(1);
                        }

                        public void onComplete(int diag) {
                            SanityCheckThread.this.visualUpdate(3, "------\nDone.\n");
                        }

                        public void processError(String line) throws Exception {
                            SanityCheckThread.this.visualUpdate(3, line + "\n");
                        }
                    };
                    RootTools.sendShell("ls /", result2, -1);
                    if (result2.getError() == 0) {
                        visualUpdate(4, "Testing sendShell() for multiple commands");
                        try {
                            visualUpdate(3, "\n[ ps + ls + date / (callback)]\n");
                            Result result22 = new Result() {
                                public void process(String line) throws Exception {
                                    SanityCheckThread.this.visualUpdate(3, line + "\n");
                                }

                                public void onFailure(Exception ex) {
                                    SanityCheckThread.this.visualUpdate(2, "ERROR: " + ex);
                                    setError(1);
                                }

                                public void onComplete(int diag) {
                                    SanityCheckThread.this.visualUpdate(3, "------\nDone.\n");
                                }

                                public void processError(String line) throws Exception {
                                    SanityCheckThread.this.visualUpdate(3, line + "\n");
                                }
                            };
                            try {
                                RootTools.sendShell(new String[]{"echo \"* PS:\"", "ps", "echo \"* LS:\"", "ls", "echo \"* DATE:\"", MediaMetadataRetriever.METADATA_KEY_DATE}, 0, result22, -1);
                                if (result22.getError() != 0) {
                                    return;
                                }
                            } catch (IOException e4) {
                                e = e4;
                                result2 = result22;
                                visualUpdate(2, "ERROR: " + e);
                                visualUpdate(4, "All tests complete.");
                                visualUpdate(2, null);
                                RootTools.closeAllShells();
                            } catch (RootToolsException e5) {
                                e2 = e5;
                                result2 = result22;
                                visualUpdate(2, "DEV-DEFINED ERROR: " + e2);
                                visualUpdate(4, "All tests complete.");
                                visualUpdate(2, null);
                                RootTools.closeAllShells();
                            } catch (TimeoutException e6) {
                                e3 = e6;
                                result2 = result22;
                                visualUpdate(2, "Timeout.. " + e3);
                                return;
                            }
                        } catch (IOException e7) {
                            e = e7;
                            visualUpdate(2, "ERROR: " + e);
                            visualUpdate(4, "All tests complete.");
                            visualUpdate(2, null);
                            RootTools.closeAllShells();
                        } catch (RootToolsException e8) {
                            e2 = e8;
                            visualUpdate(2, "DEV-DEFINED ERROR: " + e2);
                            visualUpdate(4, "All tests complete.");
                            visualUpdate(2, null);
                            RootTools.closeAllShells();
                        } catch (TimeoutException e9) {
                            e3 = e9;
                            visualUpdate(2, "Timeout.. " + e3);
                            return;
                        }
                        visualUpdate(4, "All tests complete.");
                        visualUpdate(2, null);
                        try {
                            RootTools.closeAllShells();
                        } catch (IOException e10) {
                            e10.printStackTrace();
                        }
                    }
                } catch (IOException e102) {
                    visualUpdate(2, "ERROR: " + e102);
                } catch (RootToolsException e23) {
                    visualUpdate(2, "DEV-DEFINED ERROR: " + e23);
                } catch (TimeoutException e32) {
                    visualUpdate(2, "Timeout.. " + e32);
                }
            } catch (IOException e1022) {
                visualUpdate(2, "ERROR: " + e1022);
            } catch (RootToolsException e232) {
                visualUpdate(2, "DEV-DEFINED ERROR: " + e232);
            } catch (TimeoutException e322) {
                visualUpdate(2, "Timeout.. " + e322);
            }
        }

        private void visualUpdate(int action, String text) {
            Message msg = this.mHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putInt(TestHandler.ACTION, action);
            bundle.putString(TestHandler.TEXT, text);
            msg.setData(bundle);
            this.mHandler.sendMessage(msg);
        }
    }

    private class TestHandler extends Handler {
        public static final String ACTION = "action";
        public static final int ACTION_DISPLAY = 3;
        public static final int ACTION_HIDE = 2;
        public static final int ACTION_PDISPLAY = 4;
        public static final int ACTION_SHOW = 1;
        public static final String TEXT = "text";

        private TestHandler() {
        }

        public void handleMessage(Message msg) {
            int action = msg.getData().getInt(ACTION);
            String text = msg.getData().getString(TEXT);
            switch (action) {
                case 1:
                    SanityCheckRootTools.this.mPDialog.show();
                    SanityCheckRootTools.this.mPDialog.setMessage("Running Root Library Tests...");
                    return;
                case 2:
                    if (text != null) {
                        SanityCheckRootTools.this.print(text);
                    }
                    SanityCheckRootTools.this.mPDialog.hide();
                    return;
                case 3:
                    SanityCheckRootTools.this.print(text);
                    return;
                case 4:
                    SanityCheckRootTools.this.mPDialog.setMessage(text);
                    return;
                default:
                    return;
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RootTools.debugMode = true;
        this.mTextView = new TextView(this);
        this.mTextView.setText("");
        this.mScrollView = new ScrollView(this);
        this.mScrollView.addView(this.mTextView);
        setContentView(this.mScrollView);
        String version = "?";
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
        }
        print("SanityCheckRootTools v " + version + "\n\n");
        try {
            Shell.startRootShell();
        } catch (IOException e2) {
            e2.printStackTrace();
        } catch (TimeoutException e3) {
            print("[ TIMEOUT EXCEPTION! ]\n");
            e3.printStackTrace();
        } catch (RootDeniedException e4) {
            print("[ ROOT DENIED EXCEPTION! ]\n");
            e4.printStackTrace();
        }
        try {
            if (RootTools.isAccessGiven()) {
                this.mPDialog = new ProgressDialog(this);
                this.mPDialog.setCancelable(false);
                this.mPDialog.setProgressStyle(0);
                new SanityCheckThread(this, new TestHandler()).start();
                return;
            }
            print("ERROR: No root access to this device.\n");
        } catch (Exception e5) {
            print("ERROR: could not determine root access to this device.\n");
        }
    }

    protected void print(CharSequence text) {
        this.mTextView.append(text);
        this.mScrollView.post(new Runnable() {
            public void run() {
                SanityCheckRootTools.this.mScrollView.fullScroll(130);
            }
        });
    }
}
