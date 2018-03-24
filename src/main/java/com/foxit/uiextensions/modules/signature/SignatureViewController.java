package com.foxit.uiextensions.modules.signature;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.uiextensions.modules.signature.SignatureDrawView.OnDrawListener;
import com.foxit.uiextensions.modules.signature.SignatureViewGroup.IMoveCallBack;

class SignatureViewController {
    private Context mContext;
    OnDrawListener mDrawListener = new OnDrawListener() {
        public void result(Bitmap bitmap, Rect rect, int color, String dsgPath) {
            if (SignatureViewController.this.mPSICallback != null) {
                SignatureViewController.this.mPSICallback.onSuccess(true, bitmap, rect, color, dsgPath);
            }
        }

        public void onBackPressed() {
            if (SignatureViewController.this.mPSICallback != null) {
                SignatureViewController.this.mPSICallback.onBackPressed();
            }
        }

        public void moveToTemplate() {
            SignatureViewController.this.mOnMoving = true;
            SignatureViewController.this.mViewGroup.moveToTop(new IMoveCallBack() {
                public void onStop() {
                    SignatureViewController.this.mOnMoving = false;
                }

                public void onStart() {
                }
            });
        }

        public boolean canDraw() {
            return !SignatureViewController.this.mOnMoving;
        }
    };
    private SignatureDrawView mDrawView;
    private int mHeight;
    private boolean mOnMoving;
    private SignatureInkCallback mPSICallback;
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private SignatureViewGroup mViewGroup;
    private int mWidth;

    public SignatureViewController(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl, SignatureInkCallback callback) {
        this.mPSICallback = callback;
        this.mContext = context;
        this.mParent = parent;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mViewGroup = new SignatureViewGroup(this.mContext);
        this.mDrawView = new SignatureDrawView(this.mContext, parent, pdfViewCtrl);
        this.mDrawView.setOnDrawListener(this.mDrawListener);
    }

    SignatureDrawView drawView() {
        if (this.mDrawView == null) {
            this.mDrawView = new SignatureDrawView(this.mContext, this.mParent, this.mPdfViewCtrl);
            this.mDrawView.setOnDrawListener(this.mDrawListener);
        }
        return this.mDrawView;
    }

    public void init(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
        this.mViewGroup.init(this.mWidth, this.mHeight);
        drawView().init(width, height, null);
        this.mViewGroup.addView(this.mDrawView.getView());
    }

    public void init(int width, int height, String key, Bitmap bmp, Rect rect, int color, float diameter, String dsgPath) {
        this.mWidth = width;
        this.mHeight = height;
        this.mViewGroup.init(this.mWidth, this.mHeight);
        drawView().init(width, height, key, bmp, rect, color, diameter, dsgPath);
        this.mViewGroup.addView(this.mDrawView.getView());
    }

    public void unInit() {
        if (this.mViewGroup.getChildCount() > 0) {
            this.mViewGroup.removeAllViews();
        }
        this.mDrawView.unInit();
        this.mDrawView = null;
    }

    public View getView() {
        return this.mViewGroup;
    }

    public void resetLanguage() {
        if (this.mDrawView != null) {
            this.mDrawView.resetLanguage();
        }
    }
}
