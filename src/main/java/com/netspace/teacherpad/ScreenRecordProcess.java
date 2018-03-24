package com.netspace.teacherpad;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import com.esotericsoftware.wildcard.Paths;
import com.netspace.library.activity.ExecuteCommandActivity;
import com.netspace.library.activity.FingerDrawActivity;
import com.netspace.library.activity.ResourceDetailActivity;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.components.CommentComponent;
import com.netspace.library.parser.ResourceParser;
import com.netspace.library.service.ScreenRecorderService.OnRecorderServiceListener;
import com.netspace.library.threads.LocalFileUploader;
import com.netspace.library.threads.LocalFileUploader.FileUploaderListener;
import com.netspace.library.ui.StatusBarDisplayer;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.library.wrapper.InVisibleActivity;
import com.netspace.library.wrapper.InVisibleActivity.OnActivityReadyCallBack;
import com.netspace.teacherpad.dialog.AddScreenRecordDialog;
import java.io.File;

public class ScreenRecordProcess implements OnRecorderServiceListener, FileUploaderListener {
    private static int mReuploadIntentTick = 0;
    private Context mContext;
    private LocalFileUploader mFileUploader;
    private StatusBarDisplayer mRecordStatusBarDisplayer;
    private String mResourceGUID;
    private String mTitle;
    private String mUploadFileName;
    private String mUploadPath;
    private String mUploadURL;
    private String mXMLFileName;

    public ScreenRecordProcess(Context context, String szUploadPath) {
        this.mContext = context;
        this.mUploadPath = szUploadPath;
    }

    public void OnRecordStart(String szTargetFileName) {
    }

    public void OnFrameProcessed(int nIndex) {
    }

    public void scanUnUploadFile(String szPath) {
        for (File file : new Paths(szPath, "*.mp4").getFiles()) {
            String szOneFile = file.getAbsolutePath();
            if (new File(new StringBuilder(String.valueOf(szOneFile)).append(".xml").toString()).exists()) {
                this.mUploadFileName = szOneFile;
                this.mXMLFileName = this.mUploadFileName + ".xml";
                startUpload();
                return;
            }
        }
    }

    public void OnRecordStop(final String szTargetFileName, Activity Activity) {
        Log.d("ScreenRecordProcess", "OnRecordStop");
        Context context = this.mContext;
        if (UI.getCurrentActivity() != null) {
            context = UI.getCurrentActivity();
        }
        if (Activity != null) {
            context = Activity;
        }
        FingerDrawActivity.clearPageContents();
        this.mUploadFileName = szTargetFileName;
        this.mXMLFileName = new StringBuilder(String.valueOf(szTargetFileName)).append(".xml").toString();
        InVisibleActivity.setCallBack(new OnActivityReadyCallBack() {
            public void onActivityReady(Context activity) {
                final AddScreenRecordDialog AddScreenRecordDialog = new AddScreenRecordDialog(activity);
                AddScreenRecordDialog.setCancelable(false);
                AddScreenRecordDialog.setCanceledOnTouchOutside(false);
                AddScreenRecordDialog.setFileName(szTargetFileName, szTargetFileName + ".xml");
                AddScreenRecordDialog.setOnOKClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        ScreenRecordProcess.this.mResourceGUID = AddScreenRecordDialog.getGUID();
                        ScreenRecordProcess.this.mTitle = AddScreenRecordDialog.getTitle();
                        ScreenRecordProcess.this.startUpload();
                    }
                });
                AddScreenRecordDialog.setOnDismissListener(new OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        InVisibleActivity.finishLastActivity();
                    }
                });
                AddScreenRecordDialog.show();
            }
        });
        Utilities.launchIntent(new Intent(this.mContext, InVisibleActivity.class));
    }

    private void startUpload() {
        this.mUploadURL = MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/PutFileChunk?filename=" + Utilities.getFileName(this.mUploadFileName);
        if (this.mRecordStatusBarDisplayer != null) {
            this.mRecordStatusBarDisplayer.hideMessage();
            this.mRecordStatusBarDisplayer.shutDown();
        }
        this.mRecordStatusBarDisplayer = new StatusBarDisplayer(this.mContext);
        this.mRecordStatusBarDisplayer.setTitle("正在上传文件");
        this.mRecordStatusBarDisplayer.setText("正在上传录课到服务器端，请稍候...");
        this.mRecordStatusBarDisplayer.setIcon(R.drawable.ic_launcher);
        this.mRecordStatusBarDisplayer.showProgressBox(null);
        this.mFileUploader = new LocalFileUploader(this.mUploadURL, this.mUploadFileName, true, this);
        this.mFileUploader.start();
    }

    public void OnRecordError(String szTargetFileName, String szErrorText) {
        Utilities.showAlertMessage(this.mContext, "屏幕录制出现错误", "屏幕录制出现错误，错误原因：" + szErrorText);
    }

    public void onFileUploadProgress(LocalFileUploader Uploader, int nCurrentPos, int nMaxPos) {
        this.mRecordStatusBarDisplayer.setProgressMax(nMaxPos);
        this.mRecordStatusBarDisplayer.setProgress(nCurrentPos);
    }

    public void onFileUploadedSuccess(LocalFileUploader Uploader) {
        WebServiceCallItemObject WebServiceCallItem = new WebServiceCallItemObject("AddResourceByXML", null);
        WebServiceCallItem.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                new File(ScreenRecordProcess.this.mXMLFileName).delete();
                new File(ScreenRecordProcess.this.mUploadFileName).delete();
                ScreenRecordProcess.this.mRecordStatusBarDisplayer.hideProgressBox();
                ScreenRecordProcess.this.mRecordStatusBarDisplayer.setTitle("上传完成");
                ScreenRecordProcess.this.mRecordStatusBarDisplayer.setText("文件上传成功，点击这里打开");
                Intent Intent = new Intent(ScreenRecordProcess.this.mContext, ResourceDetailActivity.class);
                Intent.putExtra(CommentComponent.RESOURCEGUID, ScreenRecordProcess.this.mResourceGUID);
                Intent.putExtra("title", ScreenRecordProcess.this.mTitle);
                Intent.putExtra("isquestion", false);
                Intent.putExtra("resourcetype", 11);
                ScreenRecordProcess.this.mRecordStatusBarDisplayer.setPendingIntent(PendingIntent.getActivity(ScreenRecordProcess.this.mContext, 0, Intent, 134217728));
                ScreenRecordProcess.this.mRecordStatusBarDisplayer.showAlertBox();
                Utilities.runOnUIThread(ScreenRecordProcess.this.mContext, new Runnable() {
                    public void run() {
                        ScreenRecordProcess.this.scanUnUploadFile(ScreenRecordProcess.this.mUploadPath);
                    }
                });
            }
        });
        WebServiceCallItem.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
            }
        });
        String szXMLFileContent = Utilities.readTextFile(this.mXMLFileName);
        if (this.mResourceGUID == null || this.mResourceGUID.isEmpty()) {
            ResourceParser parser = new ResourceParser();
            if (parser.initialize(this.mContext, szXMLFileContent)) {
                this.mTitle = parser.getTitle();
                this.mResourceGUID = parser.getGUID();
            }
        }
        WebServiceCallItem.setParam("lpszResourceXML", szXMLFileContent);
        WebServiceCallItem.setAlwaysActiveCallbacks(true);
        WebServiceCallItem.setUserGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
        VirtualNetworkObject.addToQueue(WebServiceCallItem);
    }

    public void onFileUploadedFail(LocalFileUploader Uploader) {
        this.mRecordStatusBarDisplayer.hideProgressBox();
        this.mRecordStatusBarDisplayer.setText("文件上传失败，点击这里重试。");
        Intent reuploadIntent = new Intent(this.mContext, ExecuteCommandActivity.class);
        reuploadIntent.setAction("startscreenrecordupload");
        this.mRecordStatusBarDisplayer.setPendingIntent(PendingIntent.getActivity(this.mContext, mReuploadIntentTick, reuploadIntent, 0));
        mReuploadIntentTick++;
        this.mRecordStatusBarDisplayer.showAlertBox();
    }
}
