package com.netspace.teacherpad.modules.webbrowser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.utilities.Utilities;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.WebBrowserActivity;
import com.netspace.teacherpad.modules.TeacherModuleBase;

public class WebBrowserModule extends TeacherModuleBase {
    private View mContentView;
    private Context mContext;
    private Toolbar mToolbar;
    private WebView mWebView;
    private ProgressBar m_Progressbar;
    private String mszURL = "about:blank";

    public WebBrowserModule(Activity Activity, ViewGroup RootLayout) {
        super(Activity, RootLayout);
        this.mContext = Activity;
        this.mModuleName = "上今天的课";
        this.mCategoryName = "上课";
        this.mIconID = R.drawable.ic_geography_light;
        this.mbPutInScrollView = true;
    }

    public void setModuleName(String szName) {
        this.mModuleName = szName;
    }

    public void setModuleCategory(String szName) {
        this.mCategoryName = szName;
    }

    public void startModule() {
        super.startModule();
        if (this.mContentView == null) {
            this.mContentView = this.mLayoutInflater.inflate(R.layout.layout_webbrowsermodule, null);
            this.mRootLayout.addView(this.mContentView);
            this.mToolbar = (Toolbar) this.mContentView.findViewById(R.id.toolbar);
            this.mWebView = (WebView) this.mContentView.findViewById(R.id.webView1);
            this.m_Progressbar = (ProgressBar) this.mContentView.findViewById(R.id.progressBar1);
            this.m_Progressbar.setMax(100);
            this.m_Progressbar.setProgress(0);
            this.mToolbar.setTitle(this.mModuleName);
            this.mToolbar.setNavigationIcon(new IconDrawable(this.mContext, FontAwesomeIcons.fa_angle_left).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
            this.mToolbar.setNavigationOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (WebBrowserModule.this.mWebView.canGoBack()) {
                        WebBrowserModule.this.mWebView.goBack();
                    }
                }
            });
            this.mToolbar.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem arg0) {
                    if (arg0.getItemId() != R.id.action_max) {
                        return false;
                    }
                    Bundle outState = new Bundle();
                    WebBrowserModule.this.mWebView.saveState(outState);
                    Intent intent = new Intent(WebBrowserModule.this.mContext, WebBrowserActivity.class);
                    intent.putExtra("bundle", outState);
                    WebBrowserModule.this.mContext.startActivity(intent);
                    return true;
                }
            });
            this.mToolbar.inflateMenu(R.menu.menu_webbrowsermodule);
            this.mWebView.getSettings().setJavaScriptEnabled(true);
            this.mWebView.getSettings().setPluginState(PluginState.ON);
            this.mWebView.getSettings().setDefaultTextEncodingName("gb2312");
            this.mWebView.getSettings().setBuiltInZoomControls(true);
            this.mWebView.getSettings().setDisplayZoomControls(false);
            this.mWebView.getSettings().setSupportZoom(true);
            this.mWebView.getSettings().setSaveFormData(false);
            this.mWebView.getSettings().setDomStorageEnabled(true);
            if (VERSION.SDK_INT < 19) {
                this.mWebView.getSettings().setDatabasePath("/data/data/" + this.mContext.getPackageName() + "/databases/");
            }
            this.mWebView.getSettings().setSavePassword(false);
            this.mWebView.getSettings().setCacheMode(2);
            this.mWebView.getSettings().setLoadWithOverviewMode(true);
            this.mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            this.mWebView.getSettings().setSupportMultipleWindows(true);
            this.mWebView.setWebViewClient(new WebViewClient() {
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    handler.proceed();
                }

                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    return false;
                }
            });
            this.mWebView.setWebChromeClient(new WebChromeClient() {
                public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
                    Intent intent = new Intent(WebBrowserModule.this.mContext, WebBrowserActivity.class);
                    WebBrowserActivity.setResultMsg(resultMsg);
                    WebBrowserModule.this.mContext.startActivity(intent);
                    return true;
                }

                public void onProgressChanged(WebView view, int progress) {
                    WebBrowserModule.this.m_Progressbar.setProgress(progress);
                    if (progress == 100) {
                        WebBrowserModule.this.m_Progressbar.setVisibility(4);
                    }
                }
            });
            this.mWebView.loadUrl(this.mszURL);
            return;
        }
        this.mRootLayout.addView(this.mContentView);
    }

    public void stopModule() {
        super.stopModule();
        if (this.mContentView != null) {
            this.mRootLayout.removeView(this.mContentView);
        }
    }

    public void setParam(String szParam) {
        this.mszURL = szParam;
        if (this.mWebView != null) {
            this.mWebView.loadUrl(this.mszURL);
        }
    }
}
