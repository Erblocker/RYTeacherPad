package com.stericson.RootTools.internal;

import android.content.Context;
import android.util.Log;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

class Installer {
    private static final String BOGUS_FILE_NAME = "bogus";
    private static final String LOG_TAG = "RootTools::Installer";
    private Context context;
    private String filesPath;

    public Installer(Context context) throws IOException {
        this.context = context;
        this.filesPath = context.getFilesDir().getCanonicalPath();
    }

    protected boolean installBinary(int sourceId, String destName, String mode) {
        FileNotFoundException ex;
        Throwable th;
        File mf = new File(this.filesPath + File.separator + destName);
        if (!mf.exists()) {
            try {
                this.context.openFileInput(BOGUS_FILE_NAME).close();
            } catch (FileNotFoundException e) {
                FileOutputStream fos = null;
                try {
                    fos = this.context.openFileOutput(BOGUS_FILE_NAME, 0);
                    fos.write("justcreatedfilesdirectory".getBytes());
                    if (fos != null) {
                        try {
                            fos.close();
                            this.context.deleteFile(BOGUS_FILE_NAME);
                        } catch (IOException e2) {
                        }
                    }
                } catch (Exception ex2) {
                    if (RootTools.debugMode) {
                        Log.e(LOG_TAG, ex2.toString());
                    }
                    if (fos == null) {
                        return false;
                    }
                    try {
                        fos.close();
                        this.context.deleteFile(BOGUS_FILE_NAME);
                        return false;
                    } catch (IOException e3) {
                        return false;
                    }
                } catch (Throwable th2) {
                    if (fos != null) {
                        try {
                            fos.close();
                            this.context.deleteFile(BOGUS_FILE_NAME);
                        } catch (IOException e4) {
                        }
                    }
                }
            } catch (IOException ex3) {
                if (RootTools.debugMode) {
                    Log.e(LOG_TAG, ex3.toString());
                }
                return false;
            }
            InputStream iss = this.context.getResources().openRawResource(sourceId);
            FileOutputStream oss = null;
            try {
                FileOutputStream oss2 = new FileOutputStream(mf);
                try {
                    byte[] buffer = new byte[4096];
                    while (true) {
                        try {
                            int len = iss.read(buffer);
                            if (-1 == len) {
                                break;
                            }
                            oss2.write(buffer, 0, len);
                        } catch (IOException ex32) {
                            if (RootTools.debugMode) {
                                Log.e(LOG_TAG, ex32.toString());
                            }
                            if (oss2 == null) {
                                return false;
                            }
                            try {
                                oss2.close();
                                return false;
                            } catch (IOException e5) {
                                return false;
                            }
                        }
                    }
                    if (oss2 != null) {
                        try {
                            oss2.close();
                        } catch (IOException e6) {
                        }
                    }
                    try {
                        iss.close();
                        try {
                            CommandCapture command = new CommandCapture(0, "chmod " + mode + " " + this.filesPath + File.separator + destName);
                            Shell.startRootShell().add(command);
                            command.waitForFinish();
                        } catch (Exception e7) {
                        }
                    } catch (IOException ex322) {
                        if (RootTools.debugMode) {
                            Log.e(LOG_TAG, ex322.toString());
                        }
                        return false;
                    }
                } catch (FileNotFoundException e8) {
                    ex = e8;
                    oss = oss2;
                    try {
                        if (RootTools.debugMode) {
                            Log.e(LOG_TAG, ex.toString());
                        }
                        if (oss != null) {
                            return false;
                        }
                        try {
                            oss.close();
                            return false;
                        } catch (IOException e9) {
                            return false;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        if (oss != null) {
                            try {
                                oss.close();
                            } catch (IOException e10) {
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    oss = oss2;
                    if (oss != null) {
                        oss.close();
                    }
                    throw th;
                }
            } catch (FileNotFoundException e11) {
                ex = e11;
                if (RootTools.debugMode) {
                    Log.e(LOG_TAG, ex.toString());
                }
                if (oss != null) {
                    return false;
                }
                oss.close();
                return false;
            }
        }
        return true;
    }

    protected boolean isBinaryInstalled(String destName) {
        if (new File(this.filesPath + File.separator + destName).exists()) {
            return true;
        }
        return false;
    }
}
