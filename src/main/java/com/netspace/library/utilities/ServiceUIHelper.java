package com.netspace.library.utilities;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import com.netspace.pad.library.R;

public class ServiceUIHelper {
    private static final int HIDE_WINDOW = 300;
    public static final int OPERATION_HIDE = 101;
    public static final int OPERATION_SHOW = 100;
    private static final int SHOW_WINDOW = 200;
    public static final String TAG = "ServiceUIHelper";
    private static LayoutParams mParams;
    private static WindowManager mWindowManager;
    private boolean mAdded = false;
    private boolean mAllowMoveX = true;
    private boolean mAllowMoveY = true;
    private Context mContext;
    private View mControlsView;
    private Runnable mFadeUI = new Runnable() {
        public void run() {
            if (!ServiceUIHelper.this.mbUIFade) {
                ViewGroup viewGroup = (ViewGroup) ServiceUIHelper.this.mControlsView;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    View oneChild = viewGroup.getChildAt(i);
                    oneChild.setAlpha(ServiceUIHelper.this.mfFade);
                    if (i != 0) {
                        if (oneChild.getVisibility() == 0) {
                            oneChild.setVisibility(8);
                        } else {
                            oneChild.setVisibility(0);
                        }
                    }
                }
                ServiceUIHelper.this.mControlsView.setAlpha(ServiceUIHelper.this.mfFade);
                ServiceUIHelper.this.mControlsView.getBackground().setAlpha((int) (255.0f * ServiceUIHelper.this.mfFade));
                ServiceUIHelper.this.mbUIFade = true;
            }
        }
    };
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 200:
                    if (!ServiceUIHelper.this.mAdded) {
                        ServiceUIHelper.mWindowManager.addView(ServiceUIHelper.this.mControlsView, ServiceUIHelper.mParams);
                        ServiceUIHelper.this.sliderShow(ServiceUIHelper.this.mControlsView, 1000, ServiceUIHelper.mParams.y);
                        ServiceUIHelper.this.mAdded = true;
                        return;
                    }
                    return;
                case 300:
                    if (ServiceUIHelper.this.mAdded) {
                        ServiceUIHelper.this.sliderHide(ServiceUIHelper.this.mControlsView, 1000, ServiceUIHelper.mParams.y);
                        ServiceUIHelper.mWindowManager.removeView(ServiceUIHelper.this.mControlsView);
                        ServiceUIHelper.this.mAdded = false;
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private LayoutInflater mInflater;
    private boolean mItemMove;
    private Runnable mUnFadeUI = new Runnable() {
        public void run() {
            if (ServiceUIHelper.this.mbUIFade) {
                ViewGroup viewGroup = (ViewGroup) ServiceUIHelper.this.mControlsView;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    View oneChild = viewGroup.getChildAt(i);
                    oneChild.setAlpha(1.0f);
                    if (i != 0) {
                        if (oneChild.getVisibility() == 8) {
                            oneChild.setVisibility(0);
                        } else {
                            oneChild.setVisibility(8);
                        }
                    }
                }
                ServiceUIHelper.this.mControlsView.setAlpha(1.0f);
                ServiceUIHelper.this.mControlsView.getBackground().setAlpha(255);
                ServiceUIHelper.this.mbUIFade = false;
            }
        }
    };
    private boolean mbUIFade = false;
    private float mfFade = 0.4f;

    public ServiceUIHelper(Context context, int nLayoutID, int nStartX, int nStartY) {
        this.mContext = context;
        createFloatView(nLayoutID, nStartX, nStartY);
    }

    public void showWindow() {
        this.mHandler.sendEmptyMessage(200);
    }

    public void hideWindow() {
        this.mHandler.sendEmptyMessage(300);
    }

    public void setMove(boolean bMoveX, boolean bMoveY) {
        this.mAllowMoveX = bMoveX;
        this.mAllowMoveY = bMoveY;
    }

    private void createFloatView(int nLayoutID, int nStartX, int nStartY) {
        this.mInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        this.mInflater = this.mInflater.cloneInContext(new ContextThemeWrapper(this.mContext, R.style.AppTheme));
        this.mControlsView = this.mInflater.inflate(nLayoutID, null);
        mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        mParams = new LayoutParams();
        mParams.type = 2003;
        mParams.format = 1;
        mParams.flags = 40;
        mParams.width = -2;
        mParams.height = -2;
        this.mControlsView.setOnTouchListener(new OnTouchListener() {
            int lastX;
            int lastY;
            int paramX;
            int paramY;

            public boolean onTouch(View v, MotionEvent event) {
                ServiceUIHelper.this.mHandler.removeCallbacks(ServiceUIHelper.this.mFadeUI);
                ServiceUIHelper.this.mUnFadeUI.run();
                ServiceUIHelper.this.mHandler.postDelayed(ServiceUIHelper.this.mFadeUI, 5000);
                switch (event.getAction()) {
                    case 0:
                        this.lastX = (int) event.getRawX();
                        this.lastY = (int) event.getRawY();
                        this.paramX = ServiceUIHelper.mParams.x;
                        this.paramY = ServiceUIHelper.mParams.y;
                        ServiceUIHelper.this.mItemMove = false;
                        break;
                    case 1:
                        if (ServiceUIHelper.this.mItemMove) {
                            return true;
                        }
                        break;
                    case 2:
                        int dx = ((int) event.getRawX()) - this.lastX;
                        int dy = ((int) event.getRawY()) - this.lastY;
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            if (ServiceUIHelper.this.mAllowMoveX) {
                                ServiceUIHelper.mParams.x = this.paramX + dx;
                            }
                            if (ServiceUIHelper.this.mAllowMoveY) {
                                ServiceUIHelper.mParams.y = this.paramY + dy;
                            }
                            ServiceUIHelper.this.mItemMove = true;
                            ServiceUIHelper.mWindowManager.updateViewLayout(ServiceUIHelper.this.mControlsView, ServiceUIHelper.mParams);
                            break;
                        }
                }
                return false;
            }
        });
        this.mControlsView.setOnHoverListener(new OnHoverListener() {
            public boolean onHover(View v, MotionEvent event) {
                ServiceUIHelper.this.mHandler.removeCallbacks(ServiceUIHelper.this.mFadeUI);
                ServiceUIHelper.this.mUnFadeUI.run();
                ServiceUIHelper.this.mHandler.postDelayed(ServiceUIHelper.this.mFadeUI, 5000);
                return false;
            }
        });
        mWindowManager.addView(this.mControlsView, mParams);
        this.mAdded = true;
        Display display = mWindowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        mParams.x = (-((metrics.widthPixels - mParams.width) / 2)) + nStartX;
        mParams.y = (-((metrics.heightPixels - mParams.height) / 2)) + nStartY;
        mWindowManager.updateViewLayout(this.mControlsView, mParams);
        this.mHandler.postDelayed(this.mFadeUI, 5000);
    }

    public void sliderHide(View View, int nTime, int nStartY) {
        TranslateAnimation SliderIn = new TranslateAnimation(0.0f, 0.0f, (float) nStartY, (float) (nStartY - 100));
        SliderIn.setInterpolator(new DecelerateInterpolator());
        SliderIn.setDuration((long) nTime);
        View.setVisibility(0);
        View.startAnimation(SliderIn);
        View.setVisibility(4);
    }

    public void sliderShow(View View, int nTime, int nStartY) {
        TranslateAnimation SliderIn = new TranslateAnimation(0.0f, 0.0f, (float) (nStartY - 100), (float) nStartY);
        SliderIn.setInterpolator(new DecelerateInterpolator());
        SliderIn.setDuration((long) nTime);
        View.setVisibility(4);
        View.startAnimation(SliderIn);
        View.setVisibility(0);
    }

    public View getView() {
        return this.mControlsView;
    }
}
