package com.netspace.library.controls;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.netspace.library.utilities.Utilities;

public class MoveableObject implements OnTouchListener {
    private final Handler handler = new Handler();
    private MoveObjectCallBack m_CallBack;
    private Context m_Context;
    private View m_MoveObject = null;
    private boolean m_bActionDown = false;
    private boolean m_bHandleMoveEvent = false;
    private int m_nStartXOffset = 0;
    private int m_nStartYOffset = 0;
    private boolean mbEnableHeightSize = true;
    private boolean mbEnableWidthSize = true;
    private boolean mbUseDelay = true;
    private int mnScreenHeight = 0;
    private int mnScreenWidth = 0;
    private final Runnable runnable = new Runnable() {
        public void run() {
            if (!MoveableObject.this.m_bHandleMoveEvent) {
                MoveableObject.this.m_bHandleMoveEvent = true;
                Toast.makeText(MoveableObject.this.m_Context, "已进入移动模式，再次触摸可以移动了", 0).show();
                if (MoveableObject.this.m_CallBack != null) {
                    MoveableObject.this.m_CallBack.OnMoveStart(MoveableObject.this.m_MoveObject);
                }
            }
        }
    };

    public interface MoveObjectCallBack {
        void OnMoveEnd(View view);

        void OnMoveStart(View view);
    }

    public MoveableObject(Context Context, MoveObjectCallBack CallBack) {
        this.m_Context = Context;
        this.m_CallBack = CallBack;
        this.mnScreenWidth = Utilities.getScreenWidth();
        this.mnScreenHeight = Utilities.getScreenHeight();
    }

    public MoveableObject(Context Context, MoveObjectCallBack CallBack, boolean bEnableLeftRight, boolean bEnableTopBottom) {
        this.m_Context = Context;
        this.m_CallBack = CallBack;
        this.mbEnableWidthSize = bEnableLeftRight;
        this.mbEnableHeightSize = bEnableTopBottom;
        this.mnScreenWidth = Utilities.getScreenWidth();
        this.mnScreenHeight = Utilities.getScreenHeight();
    }

    public void setUseMoveDelay(boolean bEnable) {
        this.mbUseDelay = bEnable;
    }

    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case 0:
                if (this.mbUseDelay) {
                    this.handler.postDelayed(this.runnable, 2000);
                } else {
                    this.m_bHandleMoveEvent = true;
                }
                this.m_bActionDown = true;
                this.m_nStartXOffset = (int) event.getX();
                this.m_nStartYOffset = (int) event.getY();
                this.m_MoveObject = v;
                break;
            case 1:
                if (this.m_bActionDown) {
                    this.m_bActionDown = false;
                    if (this.m_bHandleMoveEvent) {
                        if (this.m_MoveObject instanceof FrameLayout) {
                            int nTopOffset = this.m_MoveObject.getTop();
                            int nLeftOffset = this.m_MoveObject.getLeft();
                            this.m_MoveObject = ((FrameLayout) this.m_MoveObject).getChildAt(0);
                        }
                        this.m_bHandleMoveEvent = false;
                        this.m_MoveObject.setPressed(false);
                        this.m_MoveObject.invalidate();
                        if (this.m_CallBack != null) {
                            this.m_CallBack.OnMoveEnd(this.m_MoveObject);
                            Toast.makeText(this.m_Context, "新的位置已保存", 0).show();
                        }
                    }
                    this.m_MoveObject = null;
                }
                this.handler.removeCallbacks(this.runnable);
                break;
            case 2:
                if (this.m_MoveObject != null && this.m_bHandleMoveEvent) {
                    LayoutParams ViewGroupParams = this.m_MoveObject.getLayoutParams();
                    Rect r = new Rect();
                    this.m_MoveObject.getGlobalVisibleRect(r);
                    int nLeft = (((int) event.getX()) + r.left) - this.m_nStartXOffset;
                    int nTop = (((int) event.getY()) + r.top) - this.m_nStartYOffset;
                    int nRight = nLeft + this.m_MoveObject.getWidth();
                    int nBottom = nTop + this.m_MoveObject.getHeight();
                    if (ViewGroupParams instanceof FrameLayout.LayoutParams) {
                        this.m_MoveObject = (View) this.m_MoveObject.getParent();
                        ViewGroupParams = this.m_MoveObject.getLayoutParams();
                    }
                    if (ViewGroupParams instanceof RelativeLayout.LayoutParams) {
                        RelativeLayout.LayoutParams LayoutParams = (RelativeLayout.LayoutParams) this.m_MoveObject.getLayoutParams();
                        LayoutParams.removeRule(12);
                        LayoutParams.removeRule(10);
                        LayoutParams.removeRule(9);
                        LayoutParams.removeRule(11);
                        if (nTop < 0) {
                            nTop = 0;
                        }
                        if (this.m_MoveObject.getHeight() + nTop > this.mnScreenHeight) {
                            nTop = this.mnScreenHeight - this.m_MoveObject.getHeight();
                        }
                        if (!this.mbEnableWidthSize) {
                            nLeft = LayoutParams.leftMargin;
                        }
                        if (!this.mbEnableHeightSize) {
                            nTop = LayoutParams.topMargin;
                        }
                        LayoutParams.setMargins(nLeft, nTop, 0, 0);
                        this.m_MoveObject.setLayoutParams(LayoutParams);
                        break;
                    }
                }
                break;
        }
        if (this.m_bHandleMoveEvent) {
            return true;
        }
        return false;
    }
}
