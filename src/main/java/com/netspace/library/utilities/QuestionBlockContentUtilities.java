package com.netspace.library.utilities;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.netspace.library.controls.CustomCameraView;
import com.netspace.library.controls.CustomDrawView;
import com.netspace.library.controls.CustomTextView;

public class QuestionBlockContentUtilities {
    protected static final int TOPMARGIN = 20;
    protected QuestionUtilitiesChangeCallBack mCallBack;
    protected Context mContext;
    protected LinearLayout mRootLayout;

    public interface QuestionUtilitiesChangeCallBack {
        void onChanged();
    }

    public QuestionBlockContentUtilities(Context context, LinearLayout linearLayout, QuestionUtilitiesChangeCallBack QuestionUtilitiesChangeCallBack) {
        this.mContext = context;
        this.mRootLayout = linearLayout;
        this.mCallBack = QuestionUtilitiesChangeCallBack;
    }

    public void add(int nType) {
        int which = nType;
        LayoutParams LayoutParam;
        if (which == 0) {
            CustomCameraView CameraView = new CustomCameraView(this.mContext);
            this.mRootLayout.addView(CameraView);
            LayoutParam = (LayoutParams) CameraView.getLayoutParams();
            LayoutParam.topMargin = 20;
            LayoutParam.height = Utilities.dpToPixel(165, this.mContext);
            CameraView.setLayoutParams(LayoutParam);
            CameraView.startDefaultAction();
            setChanged(true);
        } else if (which == 2) {
            CustomTextView TextView = new CustomTextView(this.mContext);
            this.mRootLayout.addView(TextView);
            LayoutParam = (LayoutParams) TextView.getLayoutParams();
            LayoutParam.topMargin = 20;
            LayoutParam.height = Utilities.dpToPixel(250, this.mContext);
            TextView.setLayoutParams(LayoutParam);
            TextView.startDefaultAction();
            setChanged(true);
        } else if (which == 1) {
            CustomDrawView DrawView = new CustomDrawView(this.mContext);
            this.mRootLayout.addView(DrawView);
            LayoutParam = (LayoutParams) DrawView.getLayoutParams();
            LayoutParam.topMargin = 20;
            LayoutParam.height = Utilities.dpToPixel(500, this.mContext);
            DrawView.setLayoutParams(LayoutParam);
            DrawView.startDefaultAction();
            setChanged(true);
        }
    }

    protected void setChanged(boolean bChanged) {
        if (this.mCallBack != null && bChanged) {
            this.mCallBack.onChanged();
        }
    }
}
