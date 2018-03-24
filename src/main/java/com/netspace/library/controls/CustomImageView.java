package com.netspace.library.controls;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.netspace.library.activity.PictureActivity2;
import com.netspace.library.service.StudentAnswerImageService;
import com.netspace.library.threads.URLDownloader;
import com.netspace.library.threads.URLDownloader.OnURLDownloadEventListener;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.pad.library.R;
import java.io.File;

public class CustomImageView extends LinearLayout implements OnClickListener, OnURLDownloadEventListener {
    private Context m_Context;
    private ImageView m_ImageView;
    private ProgressBar m_ProgressBar;
    private URLDownloader m_URLDownloader;
    private boolean m_bLargeImageLoaded = false;
    private String m_szLargeImageURL;
    private String m_szLocalFilePath;

    public CustomImageView(Context context) {
        super(context);
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.layout_customimageview, this);
        this.m_Context = context;
        this.m_ImageView = (ImageView) findViewById(R.id.ImageView);
        this.m_ProgressBar = (ProgressBar) findViewById(R.id.ProgressBarLoading);
        this.m_ProgressBar.setVisibility(8);
        this.m_ImageView.setOnClickListener(this);
    }

    public void setImage(String szBase64) {
        Bitmap Bitmap = Utilities.getBase64Bitmap(szBase64);
        if (Bitmap != null) {
            this.m_ImageView.setImageBitmap(Bitmap);
        }
    }

    public void setMainFileURL(String szURL) {
        this.m_szLargeImageURL = szURL;
    }

    public void setLoadingProgress(boolean bShow) {
        if (bShow) {
            this.m_ImageView.setAlpha(50);
            this.m_ProgressBar.setVisibility(0);
            return;
        }
        this.m_ImageView.setAlpha(255);
        this.m_ProgressBar.setVisibility(8);
    }

    public void onClick(View v) {
        Intent intent;
        if (VirtualNetworkObject.getOfflineMode()) {
            String szLocalFilePath = VirtualNetworkObject.getOfflineURL(this.m_szLargeImageURL);
            if (szLocalFilePath != null) {
                intent = new Intent(this.m_Context, PictureActivity2.class);
                intent.putExtra(StudentAnswerImageService.LISTURL, szLocalFilePath);
                Utilities.logClick(this, intent.toUri(0));
                this.m_Context.startActivity(intent);
            }
        } else if (this.m_bLargeImageLoaded) {
            intent = new Intent(this.m_Context, PictureActivity2.class);
            intent.putExtra(StudentAnswerImageService.LISTURL, this.m_szLocalFilePath);
            Utilities.logClick(this, intent.toUri(0));
            this.m_Context.startActivity(intent);
        } else if (this.m_szLargeImageURL != null && !this.m_szLargeImageURL.isEmpty()) {
            setLoadingProgress(true);
            this.m_ImageView.setOnClickListener(null);
            this.m_URLDownloader = new URLDownloader(this.m_Context, this.m_szLargeImageURL, this);
            this.m_URLDownloader.start();
        }
    }

    public void onURLDownloadEvent(int nMsg, int nProgress, int nProgressMax, float fPercent, String szURL, String szLocalFileName, String szErrorMessage) {
        if (nMsg == 1) {
            Uri FileURI = Uri.fromFile(new File(szLocalFileName));
            this.m_szLocalFilePath = szLocalFileName;
            int nScreenWidth = Utilities.getScreenWidth(this.m_Context);
            Bitmap d = new BitmapDrawable(this.m_Context.getResources(), szLocalFileName).getBitmap();
            if (d.getWidth() > nScreenWidth) {
                this.m_ImageView.setImageBitmap(Bitmap.createScaledBitmap(d, nScreenWidth, (int) (((float) d.getHeight()) * (((float) nScreenWidth) / ((float) d.getWidth()))), true));
            } else {
                this.m_ImageView.setImageBitmap(d);
            }
            setLoadingProgress(false);
            this.m_ImageView.setOnClickListener(this);
            this.m_bLargeImageLoaded = true;
            Utilities.logClick(this, new StringBuilder(String.valueOf(szURL)).append(" download success.").toString());
        } else if (nMsg == -1) {
            setLoadingProgress(false);
            this.m_ImageView.setOnClickListener(this);
            Utilities.logClick(this, new StringBuilder(String.valueOf(szURL)).append(" download fail.").toString());
        }
    }
}
