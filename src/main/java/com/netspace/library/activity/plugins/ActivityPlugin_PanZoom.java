package com.netspace.library.activity.plugins;

import android.app.Activity;
import android.graphics.PointF;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout.LayoutParams;
import com.netspace.library.controls.DrawView;

public class ActivityPlugin_PanZoom extends ActivityPluginBase {
    private DrawView mDrawView;
    private View mTargetView;
    private float mfCurrentScale = 1.0f;
    private float mfLastDistance = 0.0f;
    private float mfLastScale = 1.0f;
    private int mnLastXPos = 0;
    private int mnLastYPos = 0;
    private PointF mptTouchStart;

    public ActivityPlugin_PanZoom(Activity activity, View targetView) {
        super(activity);
        this.mTargetView = targetView;
        if (this.mTargetView instanceof DrawView) {
            this.mDrawView = (DrawView) this.mTargetView;
        }
        if (!(this.mTargetView.getLayoutParams() instanceof LayoutParams)) {
            throw new IllegalArgumentException("targetView must be in a relative layout.");
        }
    }

    private void getMidPoint(PointF point, MotionEvent event) {
        if (event.getPointerCount() >= 2) {
            point.set((event.getX(0) + event.getX(1)) / 2.0f, (event.getY(0) + event.getY(1)) / 2.0f);
        }
    }

    private float getPointSpacing(MotionEvent event) {
        if (event.getPointerCount() < 2) {
            return 0.0f;
        }
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt((double) ((x * x) + (y * y)));
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() >= 2) {
            LayoutParams Param = (LayoutParams) this.mTargetView.getLayoutParams();
            Param.topMargin--;
            this.mTargetView.setLayoutParams(Param);
        }
        switch (event.getActionMasked()) {
            case 1:
            case 6:
                this.mptTouchStart = null;
                break;
            case 2:
                if (this.mptTouchStart != null) {
                    PointF CurrentCenterPoint = new PointF();
                    getMidPoint(CurrentCenterPoint, event);
                    Display display = ((WindowManager) this.mActivity.getSystemService("window")).getDefaultDisplay();
                    float fCurrentDistance = getPointSpacing(event);
                    float fScale = (fCurrentDistance - this.mfLastDistance) / fCurrentDistance;
                    float fTotalScale = fScale + this.mfLastScale;
                    if (this.mDrawView != null) {
                        if (((double) fTotalScale) < 0.3d) {
                            this.mDrawView.setScale(0.3f);
                        } else if (fTotalScale > 3.0f) {
                            this.mDrawView.setScale(3.0f);
                        } else {
                            this.mDrawView.setScale(this.mfLastScale + fScale);
                        }
                        this.mfCurrentScale = this.mDrawView.getScale();
                    } else if (((double) fTotalScale) < 0.3d) {
                        this.mfCurrentScale = 0.3f;
                    } else if (fTotalScale > 3.0f) {
                        this.mfCurrentScale = 3.0f;
                    } else {
                        this.mfCurrentScale = this.mfLastScale + fScale;
                    }
                    int nNewLeftMargin = (int) (CurrentCenterPoint.x - (((float) this.mnLastXPos) * this.mfCurrentScale));
                    LayoutParams Param2 = (LayoutParams) this.mTargetView.getLayoutParams();
                    Param2.topMargin = (int) (CurrentCenterPoint.y - (((float) this.mnLastYPos) * this.mfCurrentScale));
                    Param2.leftMargin = nNewLeftMargin;
                    Param2.rightMargin = (int) (((((float) this.mnFullScaleWidth) * this.mfCurrentScale) - ((float) display.getWidth())) + ((float) Param2.leftMargin));
                    Param2.bottomMargin = (int) (((((float) this.mnFullScaleHeight) * this.mfCurrentScale) - ((float) display.getHeight())) + ((float) Param2.topMargin));
                    this.mTargetView.setLayoutParams(Param2);
                    break;
                }
                break;
            case 5:
                if (this.mptTouchStart == null) {
                    this.mptTouchStart = new PointF();
                }
                getMidPoint(this.mptTouchStart, event);
                Param = (LayoutParams) this.mTargetView.getLayoutParams();
                this.mfLastDistance = getPointSpacing(event);
                if (this.mDrawView != null) {
                    this.mfLastScale = this.mDrawView.getScale();
                } else {
                    this.mfLastScale = this.mfCurrentScale;
                }
                this.mnLastXPos = (int) ((this.mptTouchStart.x - ((float) Param.leftMargin)) / this.mfLastScale);
                this.mnLastYPos = (int) ((this.mptTouchStart.y - ((float) Param.topMargin)) / this.mfLastScale);
                break;
        }
        return true;
    }
}
