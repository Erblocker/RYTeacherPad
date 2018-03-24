package com.netspace.library.utilities;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import com.netspace.library.controls.CustomViewPager;
import com.netspace.library.controls.LockableScrollView;
import com.netspace.pad.library.R;

public class MoveableObject implements OnTouchListener {
    private static final String TAG = "MoveableObject";
    private final Handler handler = new Handler();
    private MoveEndCallBack mCallBack;
    private Context mContext;
    private View m_MoveObject = null;
    private boolean m_bActionDown = false;
    private boolean m_bHandleMoveEvent = false;
    private int m_nStartXOffset = 0;
    private int m_nStartYOffset = 0;
    private boolean mbAllowResize = true;
    private boolean mbAtBottom = false;
    private boolean mbAtLeft = false;
    private boolean mbAtRight = false;
    private boolean mbAtTop = false;
    private boolean mbFullHandleOnTouch = false;
    private int mnOldBottom = 0;
    private int mnOldLeft = 0;
    private int mnOldRight = 0;
    private int mnOldTop = 0;
    private final Runnable runnable = new Runnable() {
        public void run() {
            if (!MoveableObject.this.m_bHandleMoveEvent) {
                MoveableObject.this.m_bHandleMoveEvent = true;
                Toast.makeText(MoveableObject.this.mContext, "可以开始移动了", 0).show();
            }
        }
    };

    public interface MoveEndCallBack {
        void onMoveEnd(View view);

        void onMoving(View view);
    }

    public MoveableObject(Context context) {
        this.mContext = context;
    }

    public void setMoveEndCallBack(MoveEndCallBack onMoveEndCallBack) {
        this.mCallBack = onMoveEndCallBack;
    }

    public void setFullHandleOnTouch(boolean bEnable) {
        this.mbFullHandleOnTouch = bEnable;
    }

    public void setAllowResize(boolean bEnable) {
        this.mbAllowResize = bEnable;
    }

    public void prepareMoveObject(View targetView) {
        if (targetView instanceof ViewGroup) {
            ViewGroup frameLayout = (ViewGroup) targetView;
            frameLayout.getChildAt(0).setVisibility(4);
            ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.layout_move, frameLayout, true);
        }
    }

    public void unprepareMoveObject(View targetView) {
        if (targetView instanceof ViewGroup) {
            ViewGroup frameLayout = (ViewGroup) targetView;
            View moveObject = frameLayout.findViewById(R.id.layoutMove);
            if (moveObject != null) {
                frameLayout.removeView(moveObject);
            }
            frameLayout.getChildAt(0).setVisibility(0);
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        boolean bResult = false;
        int eventaction = event.getAction();
        Log.d(TAG, "onTouch");
        View ParentView;
        switch (eventaction) {
            case 0:
                ParentView = (View) v.getParent();
                while (ParentView != null) {
                    if (ParentView instanceof LockableScrollView) {
                        ((LockableScrollView) ParentView).setScrollingEnabled(false);
                    }
                    if (ParentView instanceof CustomViewPager) {
                        ((CustomViewPager) ParentView).setPagingEnabled(false);
                    }
                    if (ParentView.getParent() instanceof View) {
                        ParentView = (View) ParentView.getParent();
                    } else {
                        ParentView = null;
                    }
                }
                this.m_bHandleMoveEvent = true;
                this.m_bActionDown = true;
                if (this.mbFullHandleOnTouch) {
                    bResult = true;
                }
                this.m_nStartXOffset = (int) event.getX();
                this.m_nStartYOffset = (int) event.getY();
                this.mbAtRight = false;
                this.mbAtLeft = false;
                this.mbAtTop = false;
                this.mbAtBottom = false;
                if (!(v instanceof ImageView)) {
                    if (Math.abs(event.getX()) < 25.0f) {
                        this.mbAtLeft = true;
                    }
                    if (Math.abs(event.getX() - ((float) v.getWidth())) < 25.0f) {
                        this.mbAtRight = true;
                    }
                    if (Math.abs(event.getY()) < 25.0f) {
                        this.mbAtTop = true;
                    }
                    if (Math.abs(event.getY() - ((float) v.getHeight())) < 25.0f) {
                        this.mbAtBottom = true;
                    }
                }
                this.m_MoveObject = v;
                if (!this.mbAllowResize) {
                    this.mbAtRight = false;
                    this.mbAtLeft = false;
                    this.mbAtTop = false;
                    this.mbAtBottom = false;
                }
                this.mnOldTop = this.m_MoveObject.getTop();
                this.mnOldLeft = this.m_MoveObject.getLeft();
                this.mnOldRight = this.m_MoveObject.getRight();
                this.mnOldBottom = this.m_MoveObject.getBottom();
                break;
            case 1:
                if (this.m_bActionDown) {
                    this.m_bActionDown = false;
                    if (this.m_bHandleMoveEvent) {
                        this.m_bHandleMoveEvent = false;
                        this.m_MoveObject.invalidate();
                        bResult = true;
                        if (this.mCallBack != null) {
                            this.mCallBack.onMoveEnd(this.m_MoveObject);
                        }
                        ParentView = (View) this.m_MoveObject.getParent();
                        while (ParentView != null) {
                            if (ParentView instanceof LockableScrollView) {
                                ((LockableScrollView) ParentView).setScrollingEnabled(true);
                            }
                            if (ParentView instanceof CustomViewPager) {
                                ((CustomViewPager) ParentView).setPagingEnabled(true);
                            }
                            if (ParentView.getParent() instanceof View) {
                                ParentView = (View) ParentView.getParent();
                            } else {
                                ParentView = null;
                            }
                        }
                    }
                    this.m_MoveObject = null;
                    this.handler.removeCallbacks(this.runnable);
                    break;
                }
                break;
            case 2:
                if (this.m_MoveObject != null && this.m_bHandleMoveEvent) {
                    LayoutParams ViewGroupParams = this.m_MoveObject.getLayoutParams();
                    Rect r = new Rect();
                    this.m_MoveObject.getGlobalVisibleRect(r);
                    int nLeft = (((int) event.getX()) + r.left) - this.m_nStartXOffset;
                    int nTop = (((int) event.getY()) + r.top) - this.m_nStartYOffset;
                    if (this.mbFullHandleOnTouch) {
                        bResult = true;
                    }
                    ParentView = (View) this.m_MoveObject.getParent();
                    while (ParentView != null) {
                        if (ParentView instanceof ScrollView) {
                            ScrollView ScrollView = (ScrollView) ParentView;
                            nLeft += ScrollView.getScrollX();
                            nTop += ScrollView.getScrollY();
                        }
                        if (ParentView instanceof NestedScrollView) {
                            NestedScrollView ScrollView2 = (NestedScrollView) ParentView;
                            nLeft += ScrollView2.getScrollX();
                            nTop += ScrollView2.getScrollY();
                        }
                        if (ParentView instanceof ViewGroup) {
                            ViewGroup ViewGroup = (ViewGroup) ParentView;
                            nLeft -= ViewGroup.getLeft();
                            nTop -= ViewGroup.getTop();
                        }
                        if (ParentView.getParent() instanceof View) {
                            ParentView = (View) ParentView.getParent();
                        } else {
                            ParentView = null;
                        }
                    }
                    if (nLeft < 0) {
                        nLeft = 0;
                    }
                    if (nTop < 0) {
                        nTop = 0;
                    }
                    int nRight = nLeft + this.m_MoveObject.getWidth();
                    int nBottom = nTop + this.m_MoveObject.getHeight();
                    boolean bChangeSize = false;
                    boolean bLockX = false;
                    boolean bLockY = false;
                    if (!(this.m_MoveObject instanceof FrameLayout)) {
                        Log.d(TAG, "wrong layout");
                    }
                    if (this.mbAtRight) {
                        nLeft = this.m_MoveObject.getLeft();
                        nRight = this.mnOldRight + (((int) event.getX()) - this.m_nStartXOffset);
                        bChangeSize = true;
                        bLockY = true;
                    }
                    if (this.mbAtBottom) {
                        nTop = this.m_MoveObject.getTop();
                        nBottom = this.mnOldBottom + (((int) event.getY()) - this.m_nStartYOffset);
                        bChangeSize = true;
                        bLockX = true;
                    }
                    if (ViewGroupParams instanceof FrameLayout.LayoutParams) {
                        this.m_MoveObject = (View) this.m_MoveObject.getParent();
                        ViewGroupParams = this.m_MoveObject.getLayoutParams();
                    }
                    if (ViewGroupParams instanceof RelativeLayout.LayoutParams) {
                        RelativeLayout.LayoutParams LayoutParams = (RelativeLayout.LayoutParams) this.m_MoveObject.getLayoutParams();
                        if (!bLockX) {
                            LayoutParams.leftMargin = nLeft;
                        }
                        if (!bLockY) {
                            LayoutParams.topMargin = nTop;
                        }
                        if (bChangeSize && this.mbAllowResize) {
                            if (nRight - nLeft != this.m_MoveObject.getWidth()) {
                                LayoutParams.width = nRight - nLeft;
                            }
                            if (nBottom - nTop != this.m_MoveObject.getHeight()) {
                                LayoutParams.height = nBottom - nTop;
                            }
                        }
                        this.m_MoveObject.setLayoutParams(LayoutParams);
                        if (this.mCallBack != null) {
                            this.mCallBack.onMoving(this.m_MoveObject);
                            break;
                        }
                    }
                }
                break;
        }
        return bResult;
    }
}
