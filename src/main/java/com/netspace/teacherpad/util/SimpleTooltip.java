package com.netspace.teacherpad.util;

import android.graphics.Rect;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.widget.TextView;
import android.widget.Toast;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.teacherpad.R;

public class SimpleTooltip implements OnHoverListener {
    private String TAG = "SimpleTooltip";
    private View mActiveTooltipView;
    private Handler mHandler = new Handler();
    private int mLastHoverX = 0;
    private int mLastHoverY = 0;
    private Toast mLastToast;
    private View mLastTooltipView;
    private Runnable mShowTipRunnable = new Runnable() {
        public void run() {
            if (SimpleTooltip.this.mActiveTooltipView != SimpleTooltip.this.mLastTooltipView) {
                if (SimpleTooltip.this.mLastToast != null) {
                    SimpleTooltip.this.mLastToast.cancel();
                }
                SimpleTooltip.this.mLastTooltipView = null;
                SimpleTooltip.this.mLastToast = null;
                int[] location = new int[2];
                String szText = "";
                if (SimpleTooltip.this.mActiveTooltipView.getGlobalVisibleRect(new Rect())) {
                    SimpleTooltip.this.mActiveTooltipView.getLocationOnScreen(location);
                    if (SimpleTooltip.this.mLastHoverX <= location[0] || SimpleTooltip.this.mLastHoverX >= location[0] + SimpleTooltip.this.mActiveTooltipView.getWidth() || SimpleTooltip.this.mLastHoverY <= location[1] || SimpleTooltip.this.mLastHoverY >= location[1] + SimpleTooltip.this.mActiveTooltipView.getHeight()) {
                        SimpleTooltip.this.mActiveTooltipView = null;
                        return;
                    }
                    location[1] = location[1] + 20;
                    if (SimpleTooltip.this.mActiveTooltipView.getTag() != null) {
                        szText = (String) SimpleTooltip.this.mActiveTooltipView.getTag();
                    }
                    if (!szText.isEmpty()) {
                        SimpleTooltip.this.mLastTooltipView = SimpleTooltip.this.mActiveTooltipView;
                        View v = LayoutInflater.from(MyiBaseApplication.getBaseAppContext()).inflate(R.layout.layout_tooltip, null, false);
                        ((TextView) v.findViewById(R.id.text)).setText(szText);
                        Toast toast = new Toast(MyiBaseApplication.getBaseAppContext());
                        toast.setGravity(51, location[0], location[1]);
                        toast.setDuration(0);
                        toast.setView(v);
                        toast.show();
                        SimpleTooltip.this.mLastToast = toast;
                        return;
                    }
                    return;
                }
                SimpleTooltip.this.mActiveTooltipView = null;
            }
        }
    };

    public boolean onHover(View v, MotionEvent event) {
        this.mLastHoverX = (int) event.getX();
        this.mLastHoverY = (int) event.getY();
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        this.mLastHoverX += location[0];
        this.mLastHoverY += location[1];
        if (this.mActiveTooltipView != v) {
            this.mActiveTooltipView = v;
            this.mHandler.removeCallbacks(this.mShowTipRunnable);
            this.mHandler.postDelayed(this.mShowTipRunnable, 800);
            if (this.mLastToast != null) {
                this.mLastToast.cancel();
                this.mLastToast = null;
            }
        }
        return false;
    }

    public void regiserTooltip(View v, String szTipText) {
        v.setTag(szTipText);
        v.setOnHoverListener(this);
    }
}
