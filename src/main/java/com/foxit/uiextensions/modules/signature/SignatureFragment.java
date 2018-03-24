package com.foxit.uiextensions.modules.signature;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.uiextensions.utils.AppDisplay;

public class SignatureFragment extends Fragment {
    private boolean mAttach;
    private SignatureInkCallback mCallback;
    private boolean mCheckCreateView;
    private Context mContext;
    private AppDisplay mDisplay;
    private SignatureInkItem mInkItem;
    private int mOrientation;
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private SignatureViewController mSupport;

    interface SignatureInkCallback {
        void onBackPressed();

        void onSuccess(boolean z, Bitmap bitmap, Rect rect, int i, String str);
    }

    boolean isAttached() {
        return this.mAttach;
    }

    void setInkCallback(SignatureInkCallback callback) {
        this.mCallback = callback;
    }

    void setInkCallback(SignatureInkCallback callback, SignatureInkItem item) {
        this.mCallback = callback;
        this.mInkItem = item;
    }

    private boolean checkInit() {
        return this.mCallback != null;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void init(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mParent = parent;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mDisplay = AppDisplay.getInstance(this.mContext);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (checkInit()) {
            if (this.mSupport == null) {
                this.mSupport = new SignatureViewController(this.mContext, this.mParent, this.mPdfViewCtrl, this.mCallback);
            }
            this.mOrientation = activity.getRequestedOrientation();
            if (VERSION.SDK_INT <= 8) {
                activity.setRequestedOrientation(0);
            } else {
                activity.setRequestedOrientation(6);
            }
            this.mAttach = true;
            return;
        }
        getActivity().getSupportFragmentManager().popBackStack();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.mSupport == null) {
            getActivity().getSupportFragmentManager().popBackStack();
            return super.onCreateView(inflater, container, savedInstanceState);
        }
        ViewGroup view = (ViewGroup) this.mSupport.getView().getParent();
        if (view != null) {
            view.removeView(this.mSupport.getView());
        }
        this.mSupport.resetLanguage();
        if (this.mDisplay.getScreenWidth() > this.mDisplay.getScreenHeight()) {
            if (this.mInkItem == null) {
                this.mSupport.init(this.mDisplay.getScreenWidth(), this.mDisplay.getScreenHeight());
            } else {
                this.mSupport.init(this.mDisplay.getScreenWidth(), this.mDisplay.getScreenHeight(), this.mInkItem.key, this.mInkItem.bitmap, this.mInkItem.rect, this.mInkItem.color, this.mInkItem.diameter, this.mInkItem.dsgPath);
            }
            this.mCheckCreateView = true;
        } else {
            this.mCheckCreateView = false;
        }
        return this.mSupport.getView();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!this.mCheckCreateView && this.mDisplay.getScreenWidth() > this.mDisplay.getScreenHeight()) {
            this.mCheckCreateView = true;
            if (this.mInkItem == null) {
                this.mSupport.init(this.mDisplay.getScreenWidth(), this.mDisplay.getScreenHeight());
            } else {
                this.mSupport.init(this.mDisplay.getScreenWidth(), this.mDisplay.getScreenHeight(), this.mInkItem.key, this.mInkItem.bitmap, this.mInkItem.rect, this.mInkItem.color, this.mInkItem.diameter, this.mInkItem.dsgPath);
            }
        }
    }

    public void onDetach() {
        super.onDetach();
        getActivity().setRequestedOrientation(this.mOrientation);
        if (this.mSupport != null) {
            this.mSupport.unInit();
        }
        this.mAttach = false;
    }
}
