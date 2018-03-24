package com.netspace.library.controls;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.foxit.read.RD_ReadActivity;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.threads.URLDownloader;
import com.netspace.library.threads.URLDownloader.OnURLDownloadEventListener;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.pad.library.R;
import java.io.File;

public class CustomDocumentView extends LinearLayout implements OnClickListener, OnURLDownloadEventListener, OnLongClickListener {
    private OnOpenDocumentListener m_CallBack;
    private Context m_Context;
    private ImageView m_ImageView;
    private CircularProgressBar m_ProgressBar;
    private URLDownloader m_URLDownloader;
    private boolean m_bDownLoadOriginalFileMode = false;
    private boolean m_bFlashMode = false;
    private boolean m_bLargeImageLoaded = false;
    private String m_szLargeImageURL;
    private String m_szLocalFilePath;
    private String m_szMainFileURL;
    private String m_szPreviewURL;

    public interface OnOpenDocumentListener {
        void OnOpenDocument(String str);
    }

    public CustomDocumentView(Context context) {
        super(context);
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.layout_customdocumentview, this);
        this.m_Context = context;
        this.m_ImageView = (ImageView) findViewById(R.id.ImageView);
        this.m_ProgressBar = (CircularProgressBar) findViewById(R.id.ProgressBarLoading);
        this.m_ProgressBar.setVisibility(8);
        this.m_ImageView.setOnClickListener(this);
        this.m_ImageView.setOnLongClickListener(this);
    }

    public void setMainFileURL(String szURL) {
        int nPos = szURL.lastIndexOf(".");
        this.m_szMainFileURL = szURL;
        this.m_szLargeImageURL = szURL.substring(0, nPos);
        this.m_szLargeImageURL += ".pdf";
    }

    public void setPreviewFileURL(String previewFileURL) {
        this.m_szPreviewURL = previewFileURL;
    }

    public void setFlashFileURL(String szURL) {
        this.m_szLargeImageURL = szURL;
        this.m_bFlashMode = true;
    }

    public void setLoadingProgress(boolean bShow) {
        if (bShow) {
            this.m_ProgressBar.setVisibility(0);
        } else {
            this.m_ProgressBar.setVisibility(8);
        }
    }

    public void setOpenListener(OnOpenDocumentListener OnOpenDocumentListener) {
        this.m_CallBack = OnOpenDocumentListener;
    }

    public void onClick(View v) {
        if (VirtualNetworkObject.getOfflineMode()) {
            String szLocalFileName = VirtualNetworkObject.getOfflineURL(this.m_szLargeImageURL);
            if (szLocalFileName == null) {
                return;
            }
            if (this.m_bFlashMode) {
                Utilities.logClick(this, szLocalFileName);
                if (this.m_CallBack != null) {
                    this.m_CallBack.OnOpenDocument(szLocalFileName);
                    return;
                }
                return;
            }
            Intent intent = new Intent("android.intent.action.VIEW", Uri.fromFile(new File(szLocalFileName)));
            intent.setClass(this.m_Context, RD_ReadActivity.class);
            Utilities.logClick(this, intent.toUri(0));
            this.m_Context.startActivity(intent);
        } else if (this.m_szLargeImageURL != null && !this.m_szLargeImageURL.isEmpty()) {
            if (this.m_bFlashMode) {
                Utilities.logClick(this, this.m_szLargeImageURL);
                if (this.m_CallBack != null) {
                    this.m_CallBack.OnOpenDocument(this.m_szLargeImageURL);
                    return;
                }
                return;
            }
            setLoadingProgress(true);
            this.m_URLDownloader = new URLDownloader(this.m_Context, this.m_szLargeImageURL, this);
            this.m_URLDownloader.setSkipIfLocalFileExsit(true);
            this.m_URLDownloader.start();
        }
    }

    private boolean checkFileCanOpen(String szURL) {
        PackageManager pm = this.m_Context.getPackageManager();
        Intent intent = new Intent("android.intent.action.VIEW");
        Uri uri = Uri.fromFile(new File(URLDownloader.GetLocalFileName(this.m_Context, szURL)));
        intent.setDataAndType(uri, Utilities.getMimeType(this.m_Context, uri));
        if (pm.resolveActivity(intent, 65536) != null) {
            return true;
        }
        return false;
    }

    public void onURLDownloadEvent(int nMsg, int nProgress, int nProgressMax, float fPercent, String szURL, String szLocalFileName, String szErrorMessage) {
        if (nMsg == 1) {
            File targetFile = new File(szLocalFileName);
            if (targetFile.length() > PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) {
                Uri FileURI = Uri.fromFile(new File(szLocalFileName));
                this.m_szLocalFilePath = szLocalFileName;
                setLoadingProgress(false);
                Uri Data = Uri.fromFile(new File(szLocalFileName));
                Intent intent;
                if (this.m_bDownLoadOriginalFileMode) {
                    intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    intent.setDataAndType(Data, Utilities.getMimeType(this.m_Context, Data));
                    this.m_Context.startActivity(intent);
                } else {
                    intent = new Intent("android.intent.action.VIEW", Data);
                    intent.setClass(this.m_Context, RD_ReadActivity.class);
                    Utilities.logClick(this, intent.toUri(0));
                    this.m_Context.startActivity(intent);
                }
            } else {
                targetFile.delete();
                onURLDownloadEvent(-1, nProgress, nProgressMax, fPercent, szURL, szLocalFileName, szErrorMessage);
            }
            this.m_bDownLoadOriginalFileMode = false;
        } else if (nMsg == -1) {
            Utilities.logClick(this, new StringBuilder(String.valueOf(szURL)).append(" download failed.").toString());
            setLoadingProgress(false);
            if (this.m_szPreviewURL.isEmpty()) {
                Utilities.showAlertMessage(this.m_Context, "资源下载失败", "该资源目前无法下载，服务器端可能正在进行处理，请稍候再试。");
            } else if (this.m_CallBack != null) {
                this.m_CallBack.OnOpenDocument(this.m_szPreviewURL);
            }
            this.m_bDownLoadOriginalFileMode = false;
        } else if (nMsg == 2) {
            this.m_ProgressBar.setProgress(fPercent);
        }
    }

    public boolean onLongClick(View v) {
        if (MyiBaseApplication.getCommonVariables().UserInfo.nUserType != 0) {
            if (checkFileCanOpen(this.m_szMainFileURL)) {
                this.m_bDownLoadOriginalFileMode = true;
                setLoadingProgress(true);
                this.m_URLDownloader = new URLDownloader(this.m_Context, this.m_szMainFileURL, this);
                this.m_URLDownloader.setSkipIfLocalFileExsit(true);
                this.m_URLDownloader.start();
            } else {
                Utilities.showAlertMessage(this.m_Context, "无法打开", "当前系统中没有安装可以打开这个原始文件的程序。");
            }
        }
        return false;
    }
}
