package com.netspace.library.utilities;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.consts.Features;
import com.netspace.library.interfaces.IDownloadStatus;
import com.netspace.library.service.StudentAnswerImageService;
import com.netspace.library.ui.StatusBarDisplayer;
import com.netspace.pad.library.R;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import net.sqlcipher.database.SQLiteDatabase;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.protocol.HTTP;

public class MyiUpdate extends Thread {
    private static int mStatusBarID = 5;
    private final int PROGRESSDIALOG_CLOSE = 3;
    private final int PROGRESSDIALOG_SETPROGRESS = 1;
    private final int PROGRESSDIALOG_SETTEXT = 2;
    private MyiUpdateCallBack mCallBack;
    private Context mContext;
    private IDownloadStatus mDownloadStatus = new IDownloadStatus() {
        private boolean mbFileContent = false;
        private int nLastPercent = 0;
        private String szNewVersion;

        public void onBeginDownload() {
            if (this.mbFileContent) {
                MyiUpdate.this.setDisplayText("开始下载新版本...", false);
            } else {
                MyiUpdate.this.setDisplayText("正在检查更新文件...", false);
            }
        }

        public void onDownloadProgress(long nCurrentPos, long nMaxLength) {
            float fPercent = (((float) nCurrentPos) / ((float) nMaxLength)) * 100.0f;
            int nPercent = (int) fPercent;
            MyiUpdate.this.setDisplayText(fPercent);
            if (MyiUpdate.this.mStatusBarDisplayer != null) {
                if (this.nLastPercent != nPercent) {
                    MyiUpdate.this.mStatusBarDisplayer.setProgressMax((int) nMaxLength);
                    MyiUpdate.this.mStatusBarDisplayer.setProgress((int) nCurrentPos);
                }
                this.nLastPercent = nPercent;
            }
        }

        public void onProgressLineContent(String szLineContent) {
            int nSepPos = szLineContent.indexOf("=");
            String szFieldName = "";
            String szFieldValue = "";
            if (nSepPos != -1) {
                szFieldName = szLineContent.substring(0, nSepPos);
                szFieldValue = szLineContent.substring(nSepPos + 1).trim();
                if (szFieldName.compareToIgnoreCase(StudentAnswerImageService.LISTURL) == 0) {
                    MyiUpdate.this.mszNewVersionURL = szFieldValue;
                }
                if (szFieldName.compareToIgnoreCase(ClientCookie.VERSION_ATTR) == 0) {
                    this.szNewVersion = szFieldValue;
                }
            }
        }

        public void onDownloadComplete(File outputFile) {
            MalformedURLException e;
            File outputDir;
            IOException e2;
            if (this.mbFileContent) {
                if (MyiUpdate.this.checkApkFileSign(outputFile.getAbsolutePath())) {
                    if (MyiUpdate.this.mProgressDialog != null) {
                        MyiUpdate.this.mProgressDialogMessageHandler.obtainMessage(3).sendToTarget();
                        Utilities.showAlertMessage(MyiUpdate.this.mContext, "软件更新", "更新下载完毕并开始安装。");
                    } else {
                        MyiUpdate.this.setDisplayText("更新下载完毕，准备开始安装。", true);
                        if (MyiUpdate.this.mStatusBarDisplayer != null) {
                            MyiUpdate.this.mStatusBarDisplayer.hideProgressBox();
                            MyiUpdate.this.mStatusBarDisplayer.setTitle("软件更新");
                            MyiUpdate.this.mStatusBarDisplayer.setText("下载完毕，点击这里开始安装。");
                            MyiUpdate.this.mStatusBarDisplayer.showAlertBox();
                            MyiUpdate.this.mStatusBarDisplayer.setPendingIntent(PendingIntent.getActivity(MyiUpdate.this.mContext, 0, MyiUpdate.this.getInstallAPKIntent(outputFile.getAbsolutePath()), 134217728));
                        }
                    }
                    outputFile.setExecutable(true, false);
                    outputFile.setReadable(true, false);
                    MyiUpdate.this.InstallAPK(outputFile.getAbsolutePath());
                } else if (MyiUpdate.this.mProgressDialog != null) {
                    MyiUpdate.this.mProgressDialogMessageHandler.obtainMessage(3).sendToTarget();
                    Utilities.showAlertMessage(MyiUpdate.this.mContext, "软件更新", "安装包下载完毕但签名无效，不能安装。");
                } else {
                    MyiUpdate.this.setDisplayText("安装包下载完毕但签名无效，不能安装。", true);
                    if (MyiUpdate.this.mStatusBarDisplayer != null) {
                        MyiUpdate.this.mStatusBarDisplayer.hideProgressBox();
                        MyiUpdate.this.mStatusBarDisplayer.setTitle("软件更新");
                        MyiUpdate.this.mStatusBarDisplayer.setText("安装包下载完毕但签名无效，不能安装。");
                        MyiUpdate.this.mStatusBarDisplayer.showAlertBox();
                    }
                }
            } else if (MyiUpdate.this.mszNewVersionURL != null && this.szNewVersion != null) {
                if (MyiUpdate.this.CompareVersion(this.szNewVersion, HardwareInfo.getVersionName(MyiUpdate.this.mContext)) > 0) {
                    this.mbFileContent = true;
                    String szBaseAddress = PreferenceManager.getDefaultSharedPreferences(MyiUpdate.this.mContext).getString("BaseAddress", "");
                    boolean bUseOnlyFileName = false;
                    if (!szBaseAddress.isEmpty() && szBaseAddress.indexOf("webservice.myi.cn") == -1) {
                        if (!szBaseAddress.endsWith("/")) {
                            szBaseAddress = new StringBuilder(String.valueOf(szBaseAddress)).append("/").toString();
                        }
                        bUseOnlyFileName = true;
                    }
                    URL TargetFileURL = null;
                    URL UpdateFileURL = null;
                    try {
                        URL TargetFileURL2 = new URL(MyiUpdate.this.mszNewVersionURL);
                        try {
                            UpdateFileURL = new URL(MyiUpdate.this.mFileURL);
                            TargetFileURL = TargetFileURL2;
                        } catch (MalformedURLException e3) {
                            e = e3;
                            TargetFileURL = TargetFileURL2;
                            e.printStackTrace();
                            if (bUseOnlyFileName) {
                                MyiUpdate.this.mszNewVersionURL = TargetFileURL.getProtocol() + "://" + UpdateFileURL.getAuthority() + "/updates" + TargetFileURL.getFile();
                            }
                            if (MyiUpdate.this.mProgressDialog == null) {
                                MyiUpdate.this.mProgressDialogMessageHandler.obtainMessage(2, "正在下载更新...").sendToTarget();
                            } else if (!MyiUpdate.this.mbSilence) {
                                if (MyiUpdate.this.mStatusBarDisplayer == null) {
                                    MyiUpdate.this.mStatusBarDisplayer = new StatusBarDisplayer(MyiUpdate.this.mContext);
                                    MyiUpdate.this.mStatusBarDisplayer.setNotifyID(MyiUpdate.mStatusBarID * 1000);
                                } else {
                                    MyiUpdate.this.mStatusBarDisplayer.hideMessage();
                                }
                                MyiUpdate.this.mStatusBarDisplayer.setTitle("软件更新");
                                MyiUpdate.this.mStatusBarDisplayer.setText("正在下载更新...");
                                MyiUpdate.this.mStatusBarDisplayer.setIcon(R.drawable.ic_cloud_download);
                                MyiUpdate.this.mStatusBarDisplayer.showProgressBox(null);
                            }
                            outputDir = new File(new StringBuilder(String.valueOf(MyiUpdate.this.mContext.getCacheDir().getAbsolutePath())).append("/update").toString());
                            try {
                                outputDir.mkdir();
                                outputDir.setExecutable(true, false);
                                outputDir.setReadable(true, false);
                                Utilities.deleteDir(outputDir, outputDir);
                                outputFile = File.createTempFile("upgrade", ".apk", outputDir);
                                MyiUpdate.this.mWorkFileName = outputFile.getAbsolutePath();
                                MyiUpdate.this.mbIncreaseMode = true;
                                if (!MyiUpdate.this.mbIncreaseMode) {
                                    MyiUpdate.this.setDisplayText("正在分析当前版本...", false);
                                    MyiUpdate.this.analysisCurrentPackage();
                                    Utilities.downloadFileToLocalFile(MyiBaseApplication.getProtocol() + "://" + MyiUpdate.this.mszServerAddress + ("/getcontent?apkfilename=" + Utilities.getURLFileName(MyiUpdate.this.mszNewVersionURL) + "&contentfilename=META-INF/MANIFEST.MF"), null, MyiUpdate.this.mMETAINFAnalysis);
                                } else if (MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_DISABLE_NORMAL_UPGRADE)) {
                                    MyiUpdate.this.setDisplayText("服务器禁用标准更新，请使用增量更新", true);
                                    if (MyiUpdate.this.mStatusBarDisplayer == null) {
                                        MyiUpdate.this.mStatusBarDisplayer.hideProgressBox();
                                        MyiUpdate.this.mStatusBarDisplayer.setTitle("软件更新");
                                        MyiUpdate.this.mStatusBarDisplayer.setText("服务器禁用标准更新，请使用增量更新");
                                        MyiUpdate.this.mStatusBarDisplayer.showAlertBox();
                                    }
                                } else {
                                    MyiUpdate.this.setDisplayText("正在下载新版本...", false);
                                    Utilities.downloadFileToLocalFile(MyiUpdate.this.mszNewVersionURL, outputFile, this);
                                }
                            } catch (IOException e4) {
                                e2 = e4;
                                File file = outputDir;
                                e2.printStackTrace();
                                MyiUpdate.this.setDisplayText("检查更新遇到错误，" + e2.getMessage(), true);
                                if (MyiUpdate.this.mStatusBarDisplayer != null) {
                                    MyiUpdate.this.mStatusBarDisplayer.hideProgressBox();
                                    MyiUpdate.this.mStatusBarDisplayer.setTitle("软件更新");
                                    MyiUpdate.this.mStatusBarDisplayer.setText("检查更新遇到错误，" + e2.getMessage());
                                    MyiUpdate.this.mStatusBarDisplayer.showAlertBox();
                                }
                            }
                        }
                    } catch (MalformedURLException e5) {
                        e = e5;
                        e.printStackTrace();
                        if (bUseOnlyFileName) {
                            MyiUpdate.this.mszNewVersionURL = TargetFileURL.getProtocol() + "://" + UpdateFileURL.getAuthority() + "/updates" + TargetFileURL.getFile();
                        }
                        if (MyiUpdate.this.mProgressDialog == null) {
                            MyiUpdate.this.mProgressDialogMessageHandler.obtainMessage(2, "正在下载更新...").sendToTarget();
                        } else if (MyiUpdate.this.mbSilence) {
                            if (MyiUpdate.this.mStatusBarDisplayer == null) {
                                MyiUpdate.this.mStatusBarDisplayer.hideMessage();
                            } else {
                                MyiUpdate.this.mStatusBarDisplayer = new StatusBarDisplayer(MyiUpdate.this.mContext);
                                MyiUpdate.this.mStatusBarDisplayer.setNotifyID(MyiUpdate.mStatusBarID * 1000);
                            }
                            MyiUpdate.this.mStatusBarDisplayer.setTitle("软件更新");
                            MyiUpdate.this.mStatusBarDisplayer.setText("正在下载更新...");
                            MyiUpdate.this.mStatusBarDisplayer.setIcon(R.drawable.ic_cloud_download);
                            MyiUpdate.this.mStatusBarDisplayer.showProgressBox(null);
                        }
                        outputDir = new File(new StringBuilder(String.valueOf(MyiUpdate.this.mContext.getCacheDir().getAbsolutePath())).append("/update").toString());
                        outputDir.mkdir();
                        outputDir.setExecutable(true, false);
                        outputDir.setReadable(true, false);
                        Utilities.deleteDir(outputDir, outputDir);
                        outputFile = File.createTempFile("upgrade", ".apk", outputDir);
                        MyiUpdate.this.mWorkFileName = outputFile.getAbsolutePath();
                        MyiUpdate.this.mbIncreaseMode = true;
                        if (!MyiUpdate.this.mbIncreaseMode) {
                            MyiUpdate.this.setDisplayText("正在分析当前版本...", false);
                            MyiUpdate.this.analysisCurrentPackage();
                            Utilities.downloadFileToLocalFile(MyiBaseApplication.getProtocol() + "://" + MyiUpdate.this.mszServerAddress + ("/getcontent?apkfilename=" + Utilities.getURLFileName(MyiUpdate.this.mszNewVersionURL) + "&contentfilename=META-INF/MANIFEST.MF"), null, MyiUpdate.this.mMETAINFAnalysis);
                        } else if (MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_DISABLE_NORMAL_UPGRADE)) {
                            MyiUpdate.this.setDisplayText("正在下载新版本...", false);
                            Utilities.downloadFileToLocalFile(MyiUpdate.this.mszNewVersionURL, outputFile, this);
                        } else {
                            MyiUpdate.this.setDisplayText("服务器禁用标准更新，请使用增量更新", true);
                            if (MyiUpdate.this.mStatusBarDisplayer == null) {
                                MyiUpdate.this.mStatusBarDisplayer.hideProgressBox();
                                MyiUpdate.this.mStatusBarDisplayer.setTitle("软件更新");
                                MyiUpdate.this.mStatusBarDisplayer.setText("服务器禁用标准更新，请使用增量更新");
                                MyiUpdate.this.mStatusBarDisplayer.showAlertBox();
                            }
                        }
                    }
                    if (bUseOnlyFileName) {
                        MyiUpdate.this.mszNewVersionURL = TargetFileURL.getProtocol() + "://" + UpdateFileURL.getAuthority() + "/updates" + TargetFileURL.getFile();
                    }
                    if (MyiUpdate.this.mProgressDialog == null) {
                        MyiUpdate.this.mProgressDialogMessageHandler.obtainMessage(2, "正在下载更新...").sendToTarget();
                    } else if (MyiUpdate.this.mbSilence) {
                        if (MyiUpdate.this.mStatusBarDisplayer == null) {
                            MyiUpdate.this.mStatusBarDisplayer = new StatusBarDisplayer(MyiUpdate.this.mContext);
                            MyiUpdate.this.mStatusBarDisplayer.setNotifyID(MyiUpdate.mStatusBarID * 1000);
                        } else {
                            MyiUpdate.this.mStatusBarDisplayer.hideMessage();
                        }
                        MyiUpdate.this.mStatusBarDisplayer.setTitle("软件更新");
                        MyiUpdate.this.mStatusBarDisplayer.setText("正在下载更新...");
                        MyiUpdate.this.mStatusBarDisplayer.setIcon(R.drawable.ic_cloud_download);
                        MyiUpdate.this.mStatusBarDisplayer.showProgressBox(null);
                    }
                    try {
                        outputDir = new File(new StringBuilder(String.valueOf(MyiUpdate.this.mContext.getCacheDir().getAbsolutePath())).append("/update").toString());
                        outputDir.mkdir();
                        outputDir.setExecutable(true, false);
                        outputDir.setReadable(true, false);
                        Utilities.deleteDir(outputDir, outputDir);
                        outputFile = File.createTempFile("upgrade", ".apk", outputDir);
                        MyiUpdate.this.mWorkFileName = outputFile.getAbsolutePath();
                        if (!(MyiUpdate.this.mszNewVersionURL.indexOf("enc") == -1 && MyiUpdate.this.mszNewVersionURL.indexOf(".zip") == -1)) {
                            MyiUpdate.this.mbIncreaseMode = true;
                        }
                        if (!MyiUpdate.this.mbIncreaseMode) {
                            MyiUpdate.this.setDisplayText("正在分析当前版本...", false);
                            MyiUpdate.this.analysisCurrentPackage();
                            Utilities.downloadFileToLocalFile(MyiBaseApplication.getProtocol() + "://" + MyiUpdate.this.mszServerAddress + ("/getcontent?apkfilename=" + Utilities.getURLFileName(MyiUpdate.this.mszNewVersionURL) + "&contentfilename=META-INF/MANIFEST.MF"), null, MyiUpdate.this.mMETAINFAnalysis);
                        } else if (MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_DISABLE_NORMAL_UPGRADE)) {
                            MyiUpdate.this.setDisplayText("服务器禁用标准更新，请使用增量更新", true);
                            if (MyiUpdate.this.mStatusBarDisplayer == null) {
                                MyiUpdate.this.mStatusBarDisplayer.hideProgressBox();
                                MyiUpdate.this.mStatusBarDisplayer.setTitle("软件更新");
                                MyiUpdate.this.mStatusBarDisplayer.setText("服务器禁用标准更新，请使用增量更新");
                                MyiUpdate.this.mStatusBarDisplayer.showAlertBox();
                            }
                        } else {
                            MyiUpdate.this.setDisplayText("正在下载新版本...", false);
                            Utilities.downloadFileToLocalFile(MyiUpdate.this.mszNewVersionURL, outputFile, this);
                        }
                    } catch (IOException e6) {
                        e2 = e6;
                        e2.printStackTrace();
                        MyiUpdate.this.setDisplayText("检查更新遇到错误，" + e2.getMessage(), true);
                        if (MyiUpdate.this.mStatusBarDisplayer != null) {
                            MyiUpdate.this.mStatusBarDisplayer.hideProgressBox();
                            MyiUpdate.this.mStatusBarDisplayer.setTitle("软件更新");
                            MyiUpdate.this.mStatusBarDisplayer.setText("检查更新遇到错误，" + e2.getMessage());
                            MyiUpdate.this.mStatusBarDisplayer.showAlertBox();
                        }
                    }
                } else if (MyiUpdate.this.mProgressDialog != null) {
                    MyiUpdate.this.mProgressDialogMessageHandler.obtainMessage(3).sendToTarget();
                    Utilities.showAlertMessage(MyiUpdate.this.mContext, "软件更新", "当前使用的是最新版，无需更新。");
                } else {
                    MyiUpdate.this.setDisplayText("当前使用的是最新版，无需更新。", false);
                }
            }
        }

        public void onDownloadError(int nErrorCode, String szErrorText) {
            if (MyiUpdate.this.mProgressDialog != null) {
                MyiUpdate.this.mProgressDialogMessageHandler.obtainMessage(3).sendToTarget();
                Utilities.showAlertMessage(MyiUpdate.this.mContext, "软件更新", "检查更新遇到错误，" + szErrorText);
                return;
            }
            MyiUpdate.this.setDisplayText("检查更新遇到错误，" + szErrorText, true);
            if (MyiUpdate.this.mStatusBarDisplayer != null) {
                MyiUpdate.this.mStatusBarDisplayer.hideMessage();
                MyiUpdate.this.mStatusBarDisplayer.hideProgressBox();
                MyiUpdate.this.mStatusBarDisplayer.setTitle("软件更新");
                MyiUpdate.this.mStatusBarDisplayer.setText("检查更新遇到错误，" + szErrorText);
                MyiUpdate.this.mStatusBarDisplayer.showAlertBox();
            }
        }

        public void onProgressFileBlock(byte[] fileContent, long nLength) {
        }

        public boolean isCancelled() {
            return MyiUpdate.this.mbCancel;
        }
    };
    private String mFileURL;
    private IDownloadStatus mMETAINFAnalysis = new IDownloadStatus() {
        private StringBuilder mStringBuilder = new StringBuilder();
        private HashMap<String, String> mmapNewFile = new HashMap();

        public void onBeginDownload() {
        }

        public void onDownloadProgress(long nCurrentPos, long nMaxLength) {
        }

        public void onProgressLineContent(String szLineContent) {
            this.mStringBuilder.append(new StringBuilder(String.valueOf(szLineContent)).append("\r\n").toString());
        }

        public void onDownloadComplete(File outputFile) {
            MyiUpdate.this.parserMetaINF(this.mStringBuilder.toString(), this.mmapNewFile);
            MyiUpdate.this.setDisplayText("正在准备新版本安装包...", false);
            MyiUpdate.this.buildNewPackage(this.mmapNewFile, MyiUpdate.this.mOriginalFileHash);
        }

        public void onDownloadError(int nErrorCode, String szErrorText) {
            if (MyiUpdate.this.mProgressDialog != null) {
                MyiUpdate.this.mProgressDialogMessageHandler.obtainMessage(3).sendToTarget();
                Utilities.showAlertMessage(MyiUpdate.this.mContext, "软件更新", "检查更新遇到错误，" + szErrorText);
                return;
            }
            MyiUpdate.this.setDisplayText("检查更新遇到错误，" + szErrorText, true);
            if (MyiUpdate.this.mStatusBarDisplayer != null) {
                MyiUpdate.this.mStatusBarDisplayer.hideProgressBox();
                MyiUpdate.this.mStatusBarDisplayer.setTitle("软件更新");
                MyiUpdate.this.mStatusBarDisplayer.setText("检查更新遇到错误，" + szErrorText);
                MyiUpdate.this.mStatusBarDisplayer.showAlertBox();
            }
        }

        public void onProgressFileBlock(byte[] fileContent, long nLength) {
        }

        public boolean isCancelled() {
            return MyiUpdate.this.mbCancel;
        }
    };
    private HashMap<String, String> mOriginalFileHash = new HashMap();
    private ProgressDialog mProgressDialog;
    private Handler mProgressDialogMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    float fPercent = ((Float) msg.obj).floatValue();
                    MyiUpdate.this.mProgressDialog.setIndeterminate(false);
                    MyiUpdate.this.mProgressDialog.setMax(100);
                    MyiUpdate.this.mProgressDialog.setProgress(Integer.valueOf((int) fPercent).intValue());
                    return;
                case 2:
                    MyiUpdate.this.mProgressDialog.setMessage((String) msg.obj);
                    return;
                case 3:
                    MyiUpdate.this.mProgressDialog.dismiss();
                    return;
                default:
                    return;
            }
        }
    };
    private StatusBarDisplayer mStatusBarDisplayer;
    private WeakReference<TextView> mTextView;
    private String mWorkFileName;
    private ZipOutputStream mZipOutputStream;
    private ArrayList<String> marrFilesToDownload = new ArrayList();
    private boolean mbCancel = false;
    private boolean mbIncreaseMode = false;
    private boolean mbSilence;
    private String mszNewVersionURL;
    private String mszServerAddress;

    public interface MyiUpdateCallBack {
        boolean onBeforeInstall(String str);
    }

    private class DownloadToPackage implements IDownloadStatus {
        private String mFileName;

        public DownloadToPackage(String szFileName) {
            this.mFileName = szFileName;
        }

        public void onBeginDownload() {
            Log.d("MyiUpdate", "measure file " + this.mFileName + " into new package.");
            try {
                MyiUpdate.this.mZipOutputStream.putNextEntry(new ZipEntry(this.mFileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void onDownloadProgress(long nCurrentPos, long nMaxLength) {
        }

        public void onProgressLineContent(String szLineContent) {
        }

        public void onProgressFileBlock(byte[] fileContent, long nLength) {
            try {
                MyiUpdate.this.mZipOutputStream.write(fileContent, 0, (int) nLength);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void onDownloadComplete(File outputFile) {
            try {
                if (this.mFileName != null) {
                    MyiUpdate.this.mZipOutputStream.closeEntry();
                    Log.d("MyiUpdate", "measure file " + this.mFileName + " complete.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void onDownloadError(int nErrorCode, String szErrorText) {
            if (MyiUpdate.this.mProgressDialog != null) {
                MyiUpdate.this.mProgressDialogMessageHandler.obtainMessage(3).sendToTarget();
                Utilities.showAlertMessage(MyiUpdate.this.mContext, "软件更新", "下载更新遇到错误，" + szErrorText);
                return;
            }
            MyiUpdate.this.setDisplayText("下载更新遇到错误，" + szErrorText, true);
            if (MyiUpdate.this.mStatusBarDisplayer != null) {
                MyiUpdate.this.mStatusBarDisplayer.hideProgressBox();
                MyiUpdate.this.mStatusBarDisplayer.setTitle("软件更新");
                MyiUpdate.this.mStatusBarDisplayer.setText("下载更新遇到错误，" + szErrorText);
                MyiUpdate.this.mStatusBarDisplayer.showAlertBox();
            }
        }

        public boolean isCancelled() {
            return MyiUpdate.this.mbCancel;
        }
    }

    public void setServerAddress(String szServerAddress) {
        this.mszServerAddress = szServerAddress;
    }

    public void setSilence(boolean bEnable) {
        this.mbSilence = bEnable;
    }

    protected boolean checkApkFileSign(String szApkFilePath) {
        PackageManager pm = this.mContext.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(szApkFilePath, 64);
        if (info == null) {
            return false;
        }
        try {
            PackageInfo current = pm.getPackageInfo(this.mContext.getPackageName(), 64);
            if (info.signatures == null || current.signatures == null || info.signatures.length <= 0 || info.signatures.length != current.signatures.length || !info.signatures[0].toString().equalsIgnoreCase(current.signatures[0].toString())) {
                return false;
            }
            return true;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public MyiUpdate(Context context, String szUpdateFileName) {
        this.mContext = context;
        mStatusBarID++;
        SharedPreferences Settings = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        String szVersionCheckURL = "http://updates.myi.cn/release/updates/" + szUpdateFileName;
        String szBaseAddress = Settings.getString("BaseAddress", "");
        if (!szBaseAddress.isEmpty() && szBaseAddress.indexOf("webservice.myi.cn") == -1) {
            if (!szBaseAddress.endsWith("/")) {
                szBaseAddress = new StringBuilder(String.valueOf(szBaseAddress)).append("/").toString();
            }
            szVersionCheckURL = new StringBuilder(String.valueOf(szBaseAddress)).append("updates/release/updates/").append(szUpdateFileName).toString();
        }
        this.mFileURL = szVersionCheckURL;
    }

    private void analysisCurrentPackage() {
        try {
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(HardwareInfo.getFilePath(this.mContext))));
            ZipEntry ze;
            do {
                ze = zis.getNextEntry();
                if (ze == null) {
                    break;
                }
            } while (!ze.getName().equalsIgnoreCase("META-INF/MANIFEST.MF"));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            while (true) {
                int count = zis.read(buffer);
                if (count == -1) {
                    break;
                }
                baos.write(buffer, 0, count);
            }
            parserMetaINF(new String(baos.toByteArray(), HTTP.UTF_8), this.mOriginalFileHash);
            zis.close();
        } catch (IOException ioe) {
            System.out.println("Error opening zip file" + ioe);
        }
    }

    private void buildNewPackage(HashMap<String, String> mapNewFileContent, HashMap<String, String> mapOldFileContent) {
        String szSourceApkFileName = HardwareInfo.getFilePath(this.mContext);
        try {
            this.mZipOutputStream = new ZipOutputStream(new FileOutputStream(this.mWorkFileName));
            this.mZipOutputStream.setMethod(8);
            this.mZipOutputStream.setLevel(9);
            InputStream is = new FileInputStream(szSourceApkFileName);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
            while (true) {
                ZipEntry ze = zis.getNextEntry();
                if (ze == null) {
                    break;
                }
                String filename = ze.getName();
                if (filename.indexOf("META-INF/") == -1) {
                    if (mapNewFileContent.containsKey(filename) && ((String) mapNewFileContent.get(filename)).equalsIgnoreCase((String) mapOldFileContent.get(filename))) {
                        this.mZipOutputStream.putNextEntry(new ZipEntry(filename));
                        byte[] buffer = new byte[1024];
                        while (true) {
                            int count = zis.read(buffer);
                            if (count == -1) {
                                break;
                            }
                            this.mZipOutputStream.write(buffer, 0, count);
                        }
                        this.mZipOutputStream.closeEntry();
                        mapNewFileContent.remove(filename);
                    }
                    if (this.mbCancel) {
                        return;
                    }
                }
            }
            zis.close();
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        for (String szKey : mapNewFileContent.keySet()) {
            this.marrFilesToDownload.add(szKey);
            Log.d("MyiUpdate", "new file, " + szKey);
        }
        if (mapNewFileContent.size() > 0) {
            this.marrFilesToDownload.add("META-INF/MYI.RSA");
            this.marrFilesToDownload.add("META-INF/MYI.SF");
            this.marrFilesToDownload.add("META-INF/MANIFEST.MF");
            startDownload();
            return;
        }
        setDisplayText("版本完全一致，无需更新。", false);
    }

    private void startDownload() {
        if (this.mProgressDialog != null) {
            setDisplayText("正在下载所需文件...", false);
        } else if (!this.mbSilence) {
            if (this.mStatusBarDisplayer == null) {
                this.mStatusBarDisplayer = new StatusBarDisplayer(this.mContext);
                this.mStatusBarDisplayer.setNotifyID(mStatusBarID * 1000);
            }
            this.mStatusBarDisplayer.setTitle("软件更新");
            this.mStatusBarDisplayer.setText("正在下载更新...");
            this.mStatusBarDisplayer.setIcon(R.drawable.ic_cloud_download);
            this.mStatusBarDisplayer.showProgressBox(null);
        }
        int i = 0;
        while (i < this.marrFilesToDownload.size()) {
            setDisplayText((((float) (i + 1)) / ((float) this.marrFilesToDownload.size())) * 100.0f);
            if (this.mStatusBarDisplayer != null) {
                this.mStatusBarDisplayer.setProgressMax(this.marrFilesToDownload.size());
                this.mStatusBarDisplayer.setProgress(i + 1);
            }
            String szFileName = (String) this.marrFilesToDownload.get(i);
            String szURL = "/getcontent?apkfilename=" + Utilities.getURLFileName(this.mszNewVersionURL) + "&contentfilename=" + szFileName;
            File nullFile = new File("/dev/null");
            szURL = MyiBaseApplication.getProtocol() + "://" + this.mszServerAddress + szURL;
            if (Utilities.downloadFileToLocalFile(szURL, nullFile, new DownloadToPackage(szFileName)) == null) {
                Log.d("MyiUpdate", "Download " + szURL + " failed. skip rest.");
                return;
            } else if (!this.mbCancel) {
                i++;
            } else {
                return;
            }
        }
        try {
            this.mZipOutputStream.close();
            File outputFile = new File(this.mWorkFileName);
            if (this.mProgressDialog != null) {
                this.mProgressDialogMessageHandler.obtainMessage(3).sendToTarget();
                Utilities.showAlertMessage(this.mContext, "软件更新", "更新下载完毕并开始安装。");
            } else {
                setDisplayText("更新下载完毕，准备开始安装。", true);
                if (this.mStatusBarDisplayer != null) {
                    this.mStatusBarDisplayer.hideProgressBox();
                    this.mStatusBarDisplayer.setTitle("软件更新");
                    this.mStatusBarDisplayer.setText("下载完毕，点击这里开始安装。");
                    this.mStatusBarDisplayer.setPendingIntent(PendingIntent.getActivity(this.mContext, 0, getInstallAPKIntent(outputFile.getAbsolutePath()), 134217728));
                    this.mStatusBarDisplayer.showAlertBox();
                }
            }
            outputFile.setExecutable(true, false);
            outputFile.setReadable(true, false);
            InstallAPK(outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCallBack(MyiUpdateCallBack callBack) {
        this.mCallBack = callBack;
    }

    private void parserMetaINF(String szInfContent, HashMap<String, String> mapFileHash) {
        String[] arrContents = szInfContent.split("\r\n\r\n");
        for (int i = 1; i < arrContents.length; i++) {
            String szOneLine = arrContents[i];
            if (szOneLine.indexOf("\r\n ") != -1) {
                szOneLine = szOneLine.replace("\r\n ", "");
            }
            int nPos = szOneLine.indexOf("\n");
            int nStartPos = szOneLine.indexOf(":");
            mapFileHash.put(szOneLine.substring(nStartPos + 1, nPos).trim().replace("\r", "").replace("\n", ""), szOneLine.substring(szOneLine.indexOf(":", nStartPos + 1) + 2, szOneLine.length()));
        }
    }

    public void setTextView(TextView textView) {
        if (textView != null) {
            this.mTextView = new WeakReference(textView);
        }
    }

    public void showProgressDialog() {
        this.mProgressDialog = new ProgressDialog(this.mContext);
        this.mProgressDialog.setTitle("版本更新检测");
        this.mProgressDialog.setMessage("正在检查服务器上是否有新版本，请稍候...");
        this.mProgressDialog.setCancelable(true);
        this.mProgressDialog.setCanceledOnTouchOutside(false);
        this.mProgressDialog.setIndeterminate(true);
        this.mProgressDialog.setProgressStyle(1);
        this.mProgressDialog.setButton(-2, "取消", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                MyiUpdate.this.cancel();
                dialog.dismiss();
            }
        });
        this.mProgressDialog.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                MyiUpdate.this.cancel();
                dialog.dismiss();
            }
        });
        this.mProgressDialog.show();
    }

    public void cancel() {
        this.mbCancel = true;
    }

    private void setDisplayText(final String szText, boolean bSkipIfNoTextView) {
        if (this.mProgressDialog != null) {
            this.mProgressDialogMessageHandler.obtainMessage(2, szText).sendToTarget();
        }
        if (this.mTextView != null) {
            TextView textView = (TextView) this.mTextView.get();
            if (textView != null) {
                textView.post(new Runnable() {
                    public void run() {
                        TextView textView = (TextView) MyiUpdate.this.mTextView.get();
                        if (textView != null) {
                            textView.setText(szText);
                        }
                    }
                });
            }
        } else if (!bSkipIfNoTextView && this.mProgressDialog == null && !this.mbSilence) {
            if (this.mStatusBarDisplayer == null) {
                this.mStatusBarDisplayer = new StatusBarDisplayer(this.mContext);
                this.mStatusBarDisplayer.setNotifyID(mStatusBarID * 1000);
                this.mStatusBarDisplayer.setIcon(R.drawable.ic_cloud_download);
                this.mStatusBarDisplayer.setTitle("软件更新");
            } else {
                this.mStatusBarDisplayer.hideMessage();
            }
            this.mStatusBarDisplayer.setTitle("软件更新");
            this.mStatusBarDisplayer.setText(szText);
            this.mStatusBarDisplayer.showAlertBox();
        }
    }

    private void setDisplayText(final float fProgress) {
        if (this.mProgressDialog != null) {
            this.mProgressDialogMessageHandler.obtainMessage(1, Float.valueOf(fProgress)).sendToTarget();
        }
        if (this.mTextView != null) {
            TextView textView = (TextView) this.mTextView.get();
            if (textView != null) {
                textView.post(new Runnable() {
                    public void run() {
                        TextView textView = (TextView) MyiUpdate.this.mTextView.get();
                        if (textView != null) {
                            textView.setText("正在下载更新(" + String.format("%.0f", new Object[]{Float.valueOf(fProgress)}) + "%已完成)");
                        }
                    }
                });
            }
        }
    }

    private int CompareVersion(String szVersion1, String szVersion2) {
        String[] arrVersion1 = szVersion1.split("\\.");
        String[] arrVersion2 = szVersion2.split("\\.");
        if (arrVersion1.length == arrVersion2.length) {
            for (int i = 0; i < arrVersion1.length; i++) {
                int a = Integer.parseInt(arrVersion1[i]);
                int b = Integer.parseInt(arrVersion2[i]);
                if (a > b) {
                    return 1;
                }
                if (a < b) {
                    return -1;
                }
            }
        }
        return 0;
    }

    private void InstallAPK(String szFileName) {
        if (this.mCallBack == null || !this.mCallBack.onBeforeInstall(szFileName)) {
            File file = new File(szFileName);
            Intent intent = new Intent();
            intent.addFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
            intent.setAction("android.intent.action.VIEW");
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            this.mContext.startActivity(intent);
        }
    }

    private Intent getInstallAPKIntent(String szFileName) {
        File file = new File(szFileName);
        Intent intent = new Intent();
        intent.addFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
        intent.setAction("android.intent.action.VIEW");
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        return intent;
    }

    public void run() {
        currentThread().setName("MyiUpdate Work Thread");
        ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = false;
        if (activeNetwork != null) {
            isConnected = activeNetwork.isConnected();
        }
        if (!isConnected) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            setDisplayText("正在等待网络就绪...", false);
        }
        while (!isConnected) {
            if (activeNetwork != null) {
                isConnected = activeNetwork.isConnected();
            }
            activeNetwork = cm.getActiveNetworkInfo();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
        }
        setDisplayText("开始检查更新...", false);
        Utilities.downloadFileToLocalFile(this.mFileURL, null, this.mDownloadStatus);
    }
}
