package com.netspace.library.controls;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import com.netspace.library.multicontent.MultiContentInterface;
import com.netspace.library.multicontent.MultiContentWrapper;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class CustomCameraView extends CustomViewBase implements OnClickListener {
    private final String TAG = "CustomCameraView";
    private String mClientID = null;
    private String mImageKey = null;
    private boolean mImageLoadStarted = false;
    private ProgressBar mLoadingProgressBar;
    private boolean mStoreScaledBitmap = false;
    private Activity m_Activity;
    private Context m_Context;
    private ImageView m_ImageView;
    private LayoutInflater m_Inflater;
    private CustomCameraView m_This;
    private boolean m_bHasImage = false;
    private boolean m_bNoResize = false;
    private String m_szImageFileName;

    private static class Size {
        public int nHeight = 0;
        public int nWidth = 0;

        public Size(int nWidth, int nHeight) {
            this.nWidth = nWidth;
            this.nHeight = nHeight;
        }
    }

    public CustomCameraView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomCameraView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        this.m_Context = context;
        if (context instanceof Activity) {
            this.m_Activity = (Activity) context;
        }
        this.m_Inflater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.m_Inflater.inflate(R.layout.layout_customcameraview, this);
        setDefaultButtonIcons();
        this.m_ImageView = (ImageView) findViewById(R.id.ImageView);
        this.m_ImageView.setOnFocusChangeListener(this);
        this.m_ImageView.setOnClickListener(this);
        ((ImageButton) findViewById(R.id.ButtonTakePicture)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.ButtonDelete)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.ButtonMoveUp)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            }
        });
        ((ImageButton) findViewById(R.id.ButtonMoveDown)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            }
        });
        this.mLoadingProgressBar = (ProgressBar) findViewById(R.id.ProgressBarLoading);
        showProgress(false);
        this.m_This = this;
        onFocusChange(null, false);
    }

    public void showProgress(boolean bShow) {
        if (this.mLoadingProgressBar == null) {
            return;
        }
        if (bShow) {
            this.mLoadingProgressBar.setVisibility(0);
        } else {
            this.mLoadingProgressBar.setVisibility(4);
        }
    }

    public void setNoResize(boolean bNoResize) {
        this.m_bNoResize = bNoResize;
    }

    public void onVisible() {
        super.onVisible();
        if (!this.m_bHasImage) {
        }
    }

    public void onInVisible() {
        super.onInVisible();
        if (!this.m_bHasImage) {
        }
    }

    public void startTakepicture() {
        if (this.m_Activity instanceof MultiContentInterface) {
            this.m_Activity.StartTakePicture(this);
        }
    }

    public void startDefaultAction() {
        startTakepicture();
        super.startDefaultAction();
    }

    public void onClick(View v) {
        if (this.m_Activity != null && !this.m_bLocked) {
            if (v.getId() == this.m_ImageView.getId() && (this.m_szImageFileName != null || this.m_bHasImage)) {
                return;
            }
            if (v.getId() == this.m_ImageView.getId() || v.getId() == R.id.ButtonTakePicture) {
                if (this.m_Activity instanceof MultiContentInterface) {
                    this.m_Activity.StartTakePicture(this);
                }
            } else if (v.getId() == R.id.ButtonDelete && (this.m_Activity instanceof MultiContentInterface)) {
                ((MultiContentInterface) this.m_Activity).DeleteComponent(this.m_This);
            }
        }
    }

    public String getImageFileName(boolean bNoSave) {
        if (this.m_bHasImage && (this.m_szImageFileName == null || this.m_szImageFileName.indexOf("/") == -1)) {
            String CreateCameraFileName = MultiContentWrapper.CreateCameraFileName(this.m_Context, this.m_szImageFileName);
            if (bNoSave) {
                return CreateCameraFileName;
            }
            try {
                OutputStream FileOut = new FileOutputStream(CreateCameraFileName);
                this.m_ImageView.setDrawingCacheEnabled(true);
                Bitmap bitmap = this.m_ImageView.getDrawingCache();
                bitmap.compress(CompressFormat.JPEG, 100, FileOut);
                this.m_ImageView.setDrawingCacheEnabled(false);
                bitmap.recycle();
                this.m_szImageFileName = CreateCameraFileName;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return this.m_szImageFileName;
    }

    public void SetImageBitmap(Bitmap Bitmap) {
        float fWidth = (float) Bitmap.getWidth();
        float fHeight = (float) Bitmap.getHeight();
        this.m_ImageView.setImageBitmap(Bitmap);
        this.m_bHasImage = true;
        LayoutParams LayoutParam = (LayoutParams) this.m_This.getLayoutParams();
        int nScreenWidth = Utilities.getScreenWidth(this.m_Activity.getWindow());
        float fAspect = fWidth / fHeight;
        if (fWidth > ((float) (nScreenWidth - 140))) {
            fWidth = (float) (nScreenWidth - 140);
        }
        LayoutParam.height = (int) (fWidth / fAspect);
        this.m_This.setLayoutParams(LayoutParam);
    }

    public void setImageKey(String szKey, String szClientID) {
        this.mImageKey = szKey;
        this.mClientID = szClientID;
        this.m_bHasImage = true;
        cleanImage();
    }

    public String getImageKey() {
        return this.mImageKey;
    }

    public void cleanImage() {
        this.m_ImageView.post(new Runnable() {
            public void run() {
                CustomCameraView.this.m_ImageView.setImageDrawable(null);
            }
        });
    }

    public void setImageSize(int nWidth, int nHeight) {
        if (!this.m_bNoResize) {
            float fWidth = (float) nWidth;
            float fHeight = (float) nHeight;
            LayoutParams LayoutParam = (LayoutParams) this.m_This.getLayoutParams();
            int nScreenWidth = Utilities.getScreenWidth(this.m_Activity.getWindow());
            float fAspect = fWidth / fHeight;
            if (fWidth > ((float) (nScreenWidth - 140))) {
                fWidth = (float) (nScreenWidth - 140);
            }
            LayoutParam.height = (int) (fWidth / fAspect);
            this.m_This.setLayoutParams(LayoutParam);
        }
    }

    public void setUseDownScaleBitmap(boolean bEnable) {
        this.mStoreScaledBitmap = bEnable;
    }

    private Size calcImageSize(Integer nWidth, Integer nHeight) {
        float fWidth = (float) nWidth.intValue();
        float fHeight = (float) nHeight.intValue();
        LayoutParams LayoutParam = (LayoutParams) this.m_This.getLayoutParams();
        int nScreenWidth = Utilities.getScreenWidth(this.m_Activity.getWindow());
        float fAspect = fWidth / fHeight;
        if (fWidth > ((float) (nScreenWidth - 140))) {
            fWidth = (float) (nScreenWidth - 140);
        }
        fHeight = fWidth / fAspect;
        return new Size(Integer.valueOf((int) fWidth).intValue(), Integer.valueOf((int) fHeight).intValue());
    }

    private Bitmap rescaleBitmap(Bitmap Bitmap) {
        Size result = calcImageSize(Integer.valueOf(Bitmap.getWidth()), Integer.valueOf(Bitmap.getHeight()));
        return Bitmap.createScaledBitmap(Bitmap, result.nWidth, result.nHeight, true);
    }

    public void SetImageBitmap(Bitmap Bitmap, String szFileName) {
        float fWidth = (float) Bitmap.getWidth();
        float fHeight = (float) Bitmap.getHeight();
        this.m_ImageView.setImageBitmap(Bitmap);
        this.m_bHasImage = true;
        LayoutParams LayoutParam = (LayoutParams) this.m_This.getLayoutParams();
        int nScreenWidth = Utilities.getScreenWidth(this.m_Activity.getWindow());
        float fAspect = fWidth / fHeight;
        if (fWidth > ((float) (nScreenWidth - 140))) {
            fWidth = (float) (nScreenWidth - 140);
        }
        LayoutParam.height = (int) (fWidth / fAspect);
        this.m_This.setLayoutParams(LayoutParam);
        this.m_szImageFileName = szFileName;
    }

    public void setImageFileName(String szCameraFileName) {
        if (!(this.m_szImageFileName == null || this.m_szImageFileName.equalsIgnoreCase(szCameraFileName))) {
            new File(this.m_szImageFileName).delete();
        }
        this.m_szImageFileName = szCameraFileName;
        setChanged();
        this.m_bHasImage = true;
        Bitmap Bitmap = BitmapFactory.decodeFile(this.m_szImageFileName);
        float fWidth = (float) Bitmap.getWidth();
        float fHeight = (float) Bitmap.getHeight();
        this.m_ImageView.setImageBitmap(Bitmap);
        LayoutParams LayoutParam = (LayoutParams) this.m_This.getLayoutParams();
        int nScreenWidth = Utilities.getScreenWidth(this.m_Activity.getWindow());
        float fAspect = fWidth / fHeight;
        if (fWidth > ((float) (nScreenWidth - 140))) {
            fWidth = (float) (nScreenWidth - 140);
        }
        LayoutParam.height = (int) (fWidth / fAspect);
        this.m_This.setLayoutParams(LayoutParam);
    }
}
