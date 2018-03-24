package com.netspace.library.utilities;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.TransportMediator;
import android.view.View;
import android.view.View.OnClickListener;
import com.esotericsoftware.wildcard.Paths;
import com.netspace.library.activity.ExecuteCommandActivity;
import com.netspace.library.activity.ResourceDetailActivity;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.components.CommentComponent;
import com.netspace.library.dialog.AddResourceDialog;
import com.netspace.library.parser.ResourceParser;
import com.netspace.library.threads.LocalFileUploader;
import com.netspace.library.threads.LocalFileUploader.FileUploaderListener;
import com.netspace.library.ui.StatusBarDisplayer;
import com.netspace.library.ui.UI;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.pad.library.R;
import java.io.File;

public class ResourceUploadProcess implements FileUploaderListener {
    private static String mClientID;
    private static int mReuploadIntentTick = 0;
    private ResourceUploadInterface mCallBack;
    private Context mContext;
    private LocalFileUploader mFileUploader;
    private StatusBarDisplayer mRecordStatusBarDisplayer;
    private String mResourceGUID;
    private String mTitle;
    private String mUploadFileName;
    private String mUploadPath;
    private String mUploadURL;
    private String mXMLFileName;
    private String mszFileType;
    private String mszKPGUID;
    private String mszKPPath;
    private String mszRealName;

    public interface ResourceUploadInterface {
        void onBeginUpload();

        void onCancel();

        void onUploadComplete();
    }

    public ResourceUploadProcess(Context context, String szUploadPath, String szRealName, String szKPPath, String szKPGUID) {
        this.mContext = context;
        this.mUploadPath = szUploadPath;
        this.mszRealName = szRealName;
        this.mszKPPath = szKPPath;
        this.mszKPGUID = szKPGUID;
    }

    public void setType(String szFileType) {
        this.mszFileType = szFileType;
    }

    public static void setClientID(String szClientID) {
        mClientID = szClientID;
    }

    public void setCallBack(ResourceUploadInterface CallBack) {
        this.mCallBack = CallBack;
    }

    public void scanUnUploadFile(String szPath) {
        for (File file : new Paths(szPath, "*.xml").getFiles()) {
            String szOneFile = file.getAbsolutePath();
            String szMainFile = szOneFile.replace(".xml", "");
            if (new File(szMainFile).exists()) {
                this.mUploadFileName = szMainFile;
                this.mXMLFileName = szOneFile;
                startUpload();
                return;
            }
        }
    }

    public void startUpload(String szTargetFileName, String szTargetXMLFileName) {
        if (szTargetFileName != null && szTargetXMLFileName != null) {
            this.mUploadFileName = szTargetFileName;
            this.mXMLFileName = szTargetXMLFileName;
            startUpload();
        }
    }

    public void startUploadProcess(String szTargetFileName) {
        Context context = this.mContext;
        if (UI.getCurrentActivity() != null) {
            context = UI.getCurrentActivity();
        }
        this.mUploadFileName = szTargetFileName;
        this.mXMLFileName = new StringBuilder(String.valueOf(szTargetFileName)).append(".xml").toString();
        final AddResourceDialog AddResourceDialog = new AddResourceDialog(context);
        AddResourceDialog.setCancelable(false);
        AddResourceDialog.setCanceledOnTouchOutside(false);
        AddResourceDialog.setFileName(szTargetFileName, new StringBuilder(String.valueOf(szTargetFileName)).append(".xml").toString(), this.mszRealName, this.mszKPPath, this.mszKPGUID);
        AddResourceDialog.setFileType(this.mszFileType);
        AddResourceDialog.setOnOKClickListener(new OnClickListener() {
            public void onClick(View v) {
                ResourceUploadProcess.this.mTitle = AddResourceDialog.getTitle();
                ResourceUploadProcess.this.startUpload();
                if (ResourceUploadProcess.this.mCallBack != null) {
                    ResourceUploadProcess.this.mCallBack.onBeginUpload();
                }
            }
        });
        AddResourceDialog.setOnCancelClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (ResourceUploadProcess.this.mCallBack != null) {
                    ResourceUploadProcess.this.mCallBack.onCancel();
                }
            }
        });
        AddResourceDialog.show();
    }

    private void startUpload() {
        this.mUploadURL = MyiBaseApplication.getProtocol() + "://" + VirtualNetworkObject.getServerAddress() + "/PutFileChunk?filename=" + Utilities.getFileName(this.mUploadFileName);
        if (this.mRecordStatusBarDisplayer != null) {
            this.mRecordStatusBarDisplayer.hideMessage();
            this.mRecordStatusBarDisplayer.shutDown();
        }
        this.mResourceGUID = null;
        ResourceParser ResourceParser = new ResourceParser();
        if (ResourceParser.initialize(this.mContext, Utilities.readTextFile(this.mXMLFileName))) {
            this.mResourceGUID = ResourceParser.getGUID();
            this.mTitle = ResourceParser.getTitle();
        }
        this.mRecordStatusBarDisplayer = new StatusBarDisplayer(this.mContext);
        this.mRecordStatusBarDisplayer.setTitle("正在上传文件");
        this.mRecordStatusBarDisplayer.setText("正在上传文件到服务器端，请稍候...");
        this.mRecordStatusBarDisplayer.setIcon(R.drawable.ic_launcher);
        this.mRecordStatusBarDisplayer.showProgressBox(null);
        this.mFileUploader = new LocalFileUploader(this.mUploadURL, this.mUploadFileName, true, this);
        this.mFileUploader.start();
    }

    private static void setResourcePermission(String szResourceGUID) {
        WebServiceCallItemObject CallItem = new WebServiceCallItemObject("SetObjectAccessRights", null, new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
            }
        });
        CallItem.setParam("nObjectType", Integer.valueOf(4));
        CallItem.setParam("lpszSearchSQL", " where guid='" + szResourceGUID + "'");
        CallItem.setParam("nRights", Integer.valueOf(TransportMediator.KEYCODE_MEDIA_PAUSE));
        CallItem.setParam("szGroupGUID", MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
        CallItem.setParam("szOwnerGUID", MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
        CallItem.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(CallItem);
    }

    public void onFileUploadProgress(LocalFileUploader Uploader, int nCurrentPos, int nMaxPos) {
        this.mRecordStatusBarDisplayer.setProgressMax(nMaxPos);
        this.mRecordStatusBarDisplayer.setProgress(nCurrentPos);
    }

    public String getResourceGUID() {
        return this.mResourceGUID;
    }

    public void onFileUploadedSuccess(LocalFileUploader Uploader) {
        WebServiceCallItemObject WebServiceCallItem = new WebServiceCallItemObject("AddResourceByXML", null);
        WebServiceCallItem.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                new File(ResourceUploadProcess.this.mXMLFileName).delete();
                new File(ResourceUploadProcess.this.mUploadFileName).delete();
                ResourceUploadProcess.setResourcePermission(ResourceUploadProcess.this.mResourceGUID);
                ResourceUploadProcess.this.mRecordStatusBarDisplayer.hideProgressBox();
                ResourceUploadProcess.this.mRecordStatusBarDisplayer.setTitle("上传完成");
                ResourceUploadProcess.this.mRecordStatusBarDisplayer.setText("文件上传成功，点击这里打开");
                Intent Intent = new Intent(ResourceUploadProcess.this.mContext, ResourceDetailActivity.class);
                Intent.putExtra(CommentComponent.RESOURCEGUID, ResourceUploadProcess.this.mResourceGUID);
                Intent.putExtra("title", ResourceUploadProcess.this.mTitle);
                Intent.putExtra("isquestion", false);
                Intent.putExtra("resourcetype", 11);
                ResourceUploadProcess.this.mRecordStatusBarDisplayer.setPendingIntent(PendingIntent.getActivity(ResourceUploadProcess.this.mContext, 0, Intent, 134217728));
                ResourceUploadProcess.this.mRecordStatusBarDisplayer.showAlertBox();
                Utilities.runOnUIThread(ResourceUploadProcess.this.mContext, new Runnable() {
                    public void run() {
                        ResourceUploadProcess.this.scanUnUploadFile(ResourceUploadProcess.this.mUploadPath);
                        if (ResourceUploadProcess.this.mCallBack != null) {
                            ResourceUploadProcess.this.mCallBack.onUploadComplete();
                        }
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
        VirtualNetworkObject.addToQueue(WebServiceCallItem);
    }

    public void onFileUploadedFail(LocalFileUploader Uploader) {
        this.mRecordStatusBarDisplayer.hideProgressBox();
        this.mRecordStatusBarDisplayer.setText("文件上传失败，点击这里重试。");
        Intent reuploadIntent = new Intent(this.mContext, ExecuteCommandActivity.class);
        reuploadIntent.setAction("startscreenrecordupload");
        reuploadIntent.putExtra("realname", this.mszRealName);
        reuploadIntent.putExtra("kppath", this.mszKPPath);
        reuploadIntent.putExtra("kpguid", this.mszKPGUID);
        reuploadIntent.putExtra("uploadfilename", this.mUploadFileName);
        reuploadIntent.putExtra("xmlfilename", this.mXMLFileName);
        this.mRecordStatusBarDisplayer.setPendingIntent(PendingIntent.getActivity(this.mContext, mReuploadIntentTick, reuploadIntent, 0));
        mReuploadIntentTick++;
        this.mRecordStatusBarDisplayer.showAlertBox();
    }
}
