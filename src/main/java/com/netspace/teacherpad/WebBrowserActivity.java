package com.netspace.teacherpad;

import android.content.Context;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.netspace.library.service.StudentAnswerImageService;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.utilities.Utilities;

public class WebBrowserActivity extends BaseActivity {
    private static String mContent = null;
    public static Message m_resultMsg = null;
    private String mCurrentContent;
    private Context m_Context;
    private WebView m_WebView;

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(1);
        super.onCreate(savedInstanceState);
        this.m_Context = this;
        setDoublePressReturn(true, true);
        setContentView((int) R.layout.activity_webbrowser);
        this.m_WebView = (WebView) findViewById(R.id.webView1);
        this.m_WebView.getSettings().setJavaScriptEnabled(true);
        this.m_WebView.getSettings().setPluginState(PluginState.ON);
        this.m_WebView.getSettings().setDefaultTextEncodingName("gb2312");
        this.m_WebView.getSettings().setBuiltInZoomControls(true);
        this.m_WebView.getSettings().setDisplayZoomControls(false);
        this.m_WebView.getSettings().setSupportZoom(true);
        this.m_WebView.getSettings().setSaveFormData(false);
        this.m_WebView.getSettings().setDomStorageEnabled(true);
        if (VERSION.SDK_INT < 19) {
            this.m_WebView.getSettings().setDatabasePath("/data/data/" + getPackageName() + "/databases/");
        }
        this.m_WebView.getSettings().setSavePassword(false);
        this.m_WebView.getSettings().setCacheMode(2);
        this.m_WebView.getSettings().setLoadWithOverviewMode(true);
        this.m_WebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        this.m_WebView.getSettings().setSupportMultipleWindows(true);
        this.m_WebView.setWebViewClient(new WebViewClient() {
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
        this.m_WebView.setWebChromeClient(new WebChromeClient() {
            public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
                Intent intent = new Intent(WebBrowserActivity.this.m_Context, WebBrowserActivity.class);
                WebBrowserActivity.m_resultMsg = resultMsg;
                WebBrowserActivity.this.startActivity(intent);
                return true;
            }
        });
        if (mContent != null) {
            this.mCurrentContent = mContent;
            Utilities.showStaticTextInWebView(this.m_WebView, this.mCurrentContent);
            mContent = null;
            return;
        }
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey(StudentAnswerImageService.LISTURL)) {
                String szURL = getIntent().getExtras().getString(StudentAnswerImageService.LISTURL);
                if (szURL != null) {
                    this.m_WebView.loadUrl(szURL);
                }
            }
            if (getIntent().getExtras().containsKey("bundle")) {
                this.m_WebView.restoreState(getIntent().getExtras().getBundle("bundle"));
            }
        }
        if (m_resultMsg != null) {
            m_resultMsg.obj.setWebView(this.m_WebView);
            m_resultMsg.sendToTarget();
            m_resultMsg = null;
        }
    }

    public static void setContent(String szContent) {
        mContent = szContent;
    }

    public static void setResultMsg(Message resultMsg) {
        m_resultMsg = resultMsg;
    }

    public void onBackPressed() {
        super.onBackPressed();
        if (this.m_WebView.canGoBack()) {
            this.m_WebView.goBack();
            return;
        }
        this.m_WebView.destroy();
        finish();
    }
}
