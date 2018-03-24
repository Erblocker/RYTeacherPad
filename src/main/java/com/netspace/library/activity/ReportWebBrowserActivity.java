package com.netspace.library.activity;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.im.IMService;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.PutTemporaryStorageItemObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.pad.library.R;

public class ReportWebBrowserActivity extends BaseActivity {
    private static String mContent = null;
    public static Message m_resultMsg = null;
    private String mCurrentContent = "";
    private ProgressBar m_Progressbar;
    private WebView m_WebView;
    private boolean mbAllLoaded = false;
    private String mszLastReportTempKey = "";
    private String mszTitle;

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        if (getIntent().hasExtra("title")) {
            setTitle(getIntent().getStringExtra("title"));
        } else {
            setTitle("作答情况报表");
        }
        this.mszTitle = getTitle().toString();
        setContentView(R.layout.activity_reportwebbrowser);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ReportWebBrowserActivity.this.finish();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.m_Progressbar = (ProgressBar) findViewById(R.id.progressBar1);
        this.m_Progressbar.setMax(100);
        this.m_Progressbar.setProgress(0);
        this.m_WebView = (WebView) findViewById(R.id.webView1);
        this.m_WebView.getSettings().setJavaScriptEnabled(true);
        this.m_WebView.getSettings().setPluginState(PluginState.ON);
        this.m_WebView.getSettings().setDefaultTextEncodingName("gb2312");
        this.m_WebView.getSettings().setBuiltInZoomControls(true);
        this.m_WebView.getSettings().setDisplayZoomControls(false);
        this.m_WebView.getSettings().setSupportZoom(true);
        this.m_WebView.getSettings().setSaveFormData(false);
        this.m_WebView.getSettings().setSavePassword(false);
        this.m_WebView.getSettings().setLoadWithOverviewMode(true);
        this.m_WebView.getSettings().setCacheMode(2);
        this.m_WebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        this.m_WebView.getSettings().setSupportMultipleWindows(true);
        this.m_WebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true;
            }
        });
        this.m_WebView.setWebChromeClient(new WebChromeClient() {
            public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
                Intent intent = new Intent(ReportWebBrowserActivity.this, ReportWebBrowserActivity.class);
                ReportWebBrowserActivity.m_resultMsg = resultMsg;
                ReportWebBrowserActivity.this.startActivity(intent);
                return true;
            }

            public void onProgressChanged(WebView view, int progress) {
                ReportWebBrowserActivity.this.m_Progressbar.setProgress(progress);
                if (progress == 100) {
                    ReportWebBrowserActivity.this.m_Progressbar.setVisibility(4);
                    ReportWebBrowserActivity.this.mbAllLoaded = true;
                }
            }
        });
        if (mContent != null) {
            this.mCurrentContent = mContent;
            Utilities.showStaticTextInWebView(this.m_WebView, this.mCurrentContent);
            mContent = null;
        } else if (m_resultMsg != null) {
            m_resultMsg.obj.setWebView(this.m_WebView);
            m_resultMsg.sendToTarget();
            m_resultMsg = null;
        }
    }

    public static void setContent(String szContent) {
        mContent = szContent;
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
    }

    public void onBackPressed() {
        if (this.m_WebView.canGoBack()) {
            this.m_WebView.goBack();
            return;
        }
        this.m_WebView.destroy();
        finish();
    }

    public void onIMMessage(String szFrom, String szMessage) {
        if (szMessage.indexOf("SaveUTF8TextFileResult") != -1) {
            final String[] szResult = szMessage.split(" ");
            if (szResult.length > 1 && szResult[2].equalsIgnoreCase(this.mszLastReportTempKey)) {
                if (szResult[1].equalsIgnoreCase("0")) {
                    new Builder(this).setTitle("数据保存成功").setMessage("报表数据已成功发送到睿易通端保存").setPositiveButton("确定", null).setNegativeButton("睿易通端打开目标文件", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (szResult.length >= 5) {
                                String szFolderName = szResult[3];
                                IMService.getIMService().sendMessage(Utilities.getNow() + " " + MyiBaseApplication.getCommonVariables().UserInfo.szUserJID + ": OpenFile: " + szFolderName + " " + szResult[4], MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
                            }
                        }
                    }).setNeutralButton("打开目标文件夹", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (szResult.length >= 5) {
                                String szFolderName = szResult[3];
                                IMService.getIMService().sendMessage(Utilities.getNow() + " " + MyiBaseApplication.getCommonVariables().UserInfo.szUserJID + ": OpenFolder: " + szFolderName + " " + szResult[4], MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
                            }
                        }
                    }).show();
                } else {
                    Utilities.showAlertMessage(this, "保存失败", "睿易通数据保存失败，错误代码：" + szResult[1]);
                }
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if (this.mCurrentContent.isEmpty()) {
            return false;
        }
        getMenuInflater().inflate(R.menu.menu_reportwebbrowser, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Utilities.logMenuClick(item);
        if (item.getItemId() != R.id.action_save) {
            return false;
        }
        final String szKey = "~TMP_" + Utilities.createGUID() + ".html";
        final String szFileName = (this.mszTitle + ".html").replaceAll(" ", "");
        String szContent = this.mCurrentContent.replace("gb2312", "utf-8").replace("file:///android_asset/", "https://webservice.myi.cn:8090/resource/js/");
        this.mszLastReportTempKey = szKey;
        PutTemporaryStorageItemObject ItemObject = new PutTemporaryStorageItemObject(szKey, UI.getCurrentActivity(), new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                IMService.getIMService().sendMessage(Utilities.getNow() + " " + MyiBaseApplication.getCommonVariables().UserInfo.szUserJID + ": SaveUTF8TextFile: " + szKey + " " + szFileName, MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
            }
        });
        ItemObject.setReadOperation(false);
        ItemObject.setEncoding("UTF8");
        ItemObject.writeTextData(szContent);
        VirtualNetworkObject.addToQueue(ItemObject);
        return true;
    }
}
