package com.netspace.teacherpad.popup;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout.LayoutParams;
import com.netspace.library.utilities.Utilities;
import com.netspace.teacherpad.R;

public class BasicPopupWindow extends PopupWindow {
    private static PopupWindow mActivePopupWindow;
    private int mAnchorHeight;
    private int[] mAnchorPosition;
    private int mAnchorWidth;
    protected FrameLayout mContentLayout;
    protected Context mContext;
    private ImageView mImageBottom;
    private ImageView mImageLeft;
    private ImageView mImageRight;
    private ImageView mImageTop;
    protected LayoutInflater mLayoutInflater;
    private Runnable mRepositionAnchorRunnable;
    private View mRootView;

    public BasicPopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mAnchorPosition = new int[2];
        this.mAnchorWidth = 0;
        this.mAnchorHeight = 0;
        this.mRepositionAnchorRunnable = new Runnable() {
            public void run() {
                if (BasicPopupWindow.this.isShowing()) {
                    int[] location = new int[2];
                    BasicPopupWindow.this.getContentView().getLocationOnScreen(location);
                    Log.d("PopopWindow", "xSource=" + BasicPopupWindow.this.mAnchorPosition[0]);
                    Log.d("PopopWindow", "ySource=" + BasicPopupWindow.this.mAnchorPosition[1]);
                    Log.d("PopopWindow", "x=" + location[0]);
                    Log.d("PopopWindow", "y=" + location[1]);
                    int nXOffset = (BasicPopupWindow.this.mAnchorPosition[0] - location[0]) + (BasicPopupWindow.this.mAnchorWidth / 2);
                    int nYOffset = (BasicPopupWindow.this.mAnchorPosition[1] - location[1]) + (BasicPopupWindow.this.mAnchorWidth / 2);
                    if (BasicPopupWindow.this.mImageTop.getVisibility() == 0) {
                        nXOffset -= BasicPopupWindow.this.mImageTop.getWidth() / 2;
                    } else if (BasicPopupWindow.this.mImageBottom.getVisibility() == 0) {
                        nXOffset -= BasicPopupWindow.this.mImageBottom.getWidth() / 2;
                    } else if (BasicPopupWindow.this.mImageLeft.getVisibility() == 0) {
                        nYOffset -= BasicPopupWindow.this.mImageLeft.getHeight() / 2;
                    } else if (BasicPopupWindow.this.mImageRight.getVisibility() == 0) {
                        nYOffset -= BasicPopupWindow.this.mImageRight.getHeight() / 2;
                    }
                    if (BasicPopupWindow.this.mImageTop.getVisibility() == 0) {
                        ((LayoutParams) BasicPopupWindow.this.mImageTop.getLayoutParams()).leftMargin = nXOffset;
                    }
                    if (BasicPopupWindow.this.mImageBottom.getVisibility() == 0) {
                        ((LayoutParams) BasicPopupWindow.this.mImageBottom.getLayoutParams()).leftMargin = nXOffset;
                    }
                    if (BasicPopupWindow.this.mImageLeft.getVisibility() == 0) {
                        ((LayoutParams) BasicPopupWindow.this.mImageLeft.getLayoutParams()).topMargin = nYOffset;
                    }
                    if (BasicPopupWindow.this.mImageRight.getVisibility() == 0) {
                        ((LayoutParams) BasicPopupWindow.this.mImageRight.getLayoutParams()).topMargin = nYOffset;
                        return;
                    }
                    return;
                }
                Utilities.runOnUIThread(BasicPopupWindow.this.mContext, BasicPopupWindow.this.mRepositionAnchorRunnable);
            }
        };
        this.mContext = context;
        initView();
    }

    public BasicPopupWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mAnchorPosition = new int[2];
        this.mAnchorWidth = 0;
        this.mAnchorHeight = 0;
        this.mRepositionAnchorRunnable = /* anonymous class already generated */;
        this.mContext = context;
        initView();
    }

    public BasicPopupWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mAnchorPosition = new int[2];
        this.mAnchorWidth = 0;
        this.mAnchorHeight = 0;
        this.mRepositionAnchorRunnable = /* anonymous class already generated */;
        this.mContext = context;
        initView();
    }

    public BasicPopupWindow(Context context) {
        super(context);
        this.mAnchorPosition = new int[2];
        this.mAnchorWidth = 0;
        this.mAnchorHeight = 0;
        this.mRepositionAnchorRunnable = /* anonymous class already generated */;
        this.mContext = context;
        initView();
    }

    public void initView() {
        this.mLayoutInflater = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).cloneInContext(new ContextThemeWrapper(this.mContext, R.style.ComponentTheme));
        this.mRootView = this.mLayoutInflater.inflate(R.layout.layout_popuptoolwindow, null);
        this.mContentLayout = (FrameLayout) this.mRootView.findViewById(R.id.framePopupContent);
        this.mImageTop = (ImageView) this.mRootView.findViewById(R.id.imageViewTop);
        this.mImageBottom = (ImageView) this.mRootView.findViewById(R.id.imageViewBottom);
        this.mImageLeft = (ImageView) this.mRootView.findViewById(R.id.imageViewLeft);
        this.mImageRight = (ImageView) this.mRootView.findViewById(R.id.imageViewRight);
        setContentView(this.mRootView);
        setBackgroundDrawable(new ColorDrawable(0));
        setTouchable(true);
        setOutsideTouchable(true);
    }

    public void show(View v) {
        if (mActivePopupWindow == null || !mActivePopupWindow.isShowing()) {
            mActivePopupWindow = this;
            v.getLocationOnScreen(this.mAnchorPosition);
            int nScreenWidth = Utilities.getScreenWidth(this.mContext);
            int nWidth = getWidth();
            if (((float) this.mAnchorPosition[0]) < Utilities.dpToPixel(100)) {
                this.mImageLeft.setVisibility(0);
                this.mImageTop.setVisibility(8);
                this.mImageBottom.setVisibility(8);
                this.mImageRight.setVisibility(8);
                showAtLocation(v, 21, -getWidth(), 0);
            } else if (((float) (nScreenWidth - this.mAnchorPosition[0])) < Utilities.dpToPixel(100)) {
                this.mImageTop.setVisibility(8);
                this.mImageBottom.setVisibility(8);
                this.mImageLeft.setVisibility(8);
                this.mImageRight.setVisibility(0);
                showAtLocation(v, 19, -getWidth(), 0);
            } else {
                this.mImageLeft.setVisibility(8);
                this.mImageRight.setVisibility(8);
                showAsDropDown(v, 0, 0, 80);
            }
            if (isAboveAnchor()) {
                this.mImageTop.setVisibility(8);
            } else {
                this.mImageBottom.setVisibility(8);
            }
            this.mAnchorWidth = v.getWidth();
            Utilities.runOnUIThread(this.mContext, this.mRepositionAnchorRunnable);
            return;
        }
        mActivePopupWindow.dismiss();
        mActivePopupWindow = null;
    }
}
