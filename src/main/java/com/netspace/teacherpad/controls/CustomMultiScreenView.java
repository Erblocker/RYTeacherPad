package com.netspace.teacherpad.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.utilities.Utilities;
import com.netspace.teacherpad.TeacherPadApplication;
import com.netspace.teacherpad.structure.MultiScreen;
import java.util.ArrayList;

public class CustomMultiScreenView extends View {
    private MultiScreenCallBack mCallBack;
    private Runnable mCheckLongPressRunnable = new Runnable() {
        public void run() {
            CustomMultiScreenView.this.mHandler.removeCallbacks(CustomMultiScreenView.this.mCheckLongPressRunnable);
        }
    };
    private Handler mHandler = new Handler();
    private int mMainScreenHeight = 0;
    private int mMainScreenWidth = 0;
    private boolean mbDownInActionArea = false;
    private float mfScale = 1.0f;

    public interface MultiScreenCallBack {
        void onActiveScreen(int i);

        void onClickInCorner(int i, Rect rect);

        void onClickSomeWhere();

        void onRepositionTools(Rect rect);
    }

    public CustomMultiScreenView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomMultiScreenView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomMultiScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomMultiScreenView(Context context) {
        super(context);
    }

    public void setCallBack(MultiScreenCallBack CallBack) {
        this.mCallBack = CallBack;
    }

    public int getActiveScreenID() {
        return TeacherPadApplication.mActiveScreenID;
    }

    public void setScale(float fScale) {
        this.mfScale = fScale;
    }

    public void addMonitor(int nIndex, Rect rect) {
        MultiScreen emptyRect = new MultiScreen();
        while (TeacherPadApplication.marrMonitors.size() - 1 < nIndex) {
            TeacherPadApplication.marrMonitors.add(emptyRect);
        }
        MultiScreen currentMonitor = new MultiScreen();
        currentMonitor.rectScreen = rect;
        TeacherPadApplication.marrMonitors.set(nIndex, currentMonitor);
        for (int i = 0; i < TeacherPadApplication.marrMonitors.size(); i++) {
            rect = ((MultiScreen) TeacherPadApplication.marrMonitors.get(i)).rectScreen;
            if (rect.right > this.mMainScreenWidth) {
                this.mMainScreenWidth = rect.right;
            }
            if (rect.bottom > this.mMainScreenHeight) {
                this.mMainScreenHeight = rect.bottom;
            }
        }
        invalidate();
    }

    private void recalcMaxSize() {
        for (int i = 0; i < TeacherPadApplication.marrMonitors.size(); i++) {
            Rect rect = ((MultiScreen) TeacherPadApplication.marrMonitors.get(i)).rectScreen;
            if (rect.right > this.mMainScreenWidth) {
                this.mMainScreenWidth = rect.right;
            }
            if (rect.bottom > this.mMainScreenHeight) {
                this.mMainScreenHeight = rect.bottom;
            }
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            if (event.getAction() == 0) {
                int nCurrentActiveScreenID = TeacherPadApplication.mActiveScreenID;
                Point pt = new Point((int) event.getX(), (int) event.getY());
                checkAndSetActiveScreen(pt);
                if (nCurrentActiveScreenID == TeacherPadApplication.mActiveScreenID || this.mCallBack == null) {
                    this.mHandler.removeCallbacks(this.mCheckLongPressRunnable);
                    this.mHandler.postDelayed(this.mCheckLongPressRunnable, 1000);
                } else {
                    this.mCallBack.onActiveScreen(TeacherPadApplication.mActiveScreenID);
                }
                if (nCurrentActiveScreenID != TeacherPadApplication.mActiveScreenID) {
                    this.mbDownInActionArea = false;
                } else if (checkInCorner(pt) != -1) {
                    this.mbDownInActionArea = true;
                    super.onTouchEvent(event);
                    return true;
                } else {
                    this.mbDownInActionArea = false;
                }
            } else if (event.getAction() == 1) {
                int nActiveScreenButton = checkInCorner(new Point((int) event.getX(), (int) event.getY()));
                if (nActiveScreenButton != -1 && this.mbDownInActionArea) {
                    if (this.mCallBack != null) {
                        this.mCallBack.onClickInCorner(nActiveScreenButton, ((MultiScreen) TeacherPadApplication.marrMonitors.get(nActiveScreenButton)).rectCornerButton);
                    }
                    this.mbDownInActionArea = false;
                }
                this.mHandler.removeCallbacks(this.mCheckLongPressRunnable);
            }
        }
        return super.onTouchEvent(event);
    }

    public void splitScreen(int[] nColumns, String szTag) {
        if (this.mMainScreenWidth == 0 || this.mMainScreenHeight == 0) {
            recalcMaxSize();
        }
        if (this.mMainScreenWidth != 0 && this.mMainScreenHeight != 0) {
            int i;
            Rect rectArea = new Rect(0, 0, this.mMainScreenWidth, this.mMainScreenHeight);
            int nXStep = rectArea.width() / nColumns.length;
            ArrayList<Rect> arrRects = new ArrayList();
            for (i = 0; i < nColumns.length; i++) {
                int nYStep = rectArea.height() / nColumns[i];
                for (int j = 0; j < nColumns[i]; j++) {
                    arrRects.add(new Rect(rectArea.left + (nXStep * i), rectArea.top + (nYStep * j), rectArea.left + ((i + 1) * nXStep), rectArea.top + ((j + 1) * nYStep)));
                }
            }
            if (arrRects.size() == 0) {
                arrRects.add(rectArea);
            }
            String szData = "";
            String szSendData = "";
            for (i = 0; i < arrRects.size(); i++) {
                Rect oneRect = (Rect) arrRects.get(i);
                szSendData = new StringBuilder(String.valueOf(szSendData)).append(String.format("%d,%d,%d,%d;", new Object[]{Integer.valueOf(oneRect.left), Integer.valueOf(oneRect.top), Integer.valueOf(oneRect.right), Integer.valueOf(oneRect.bottom)})).toString();
            }
            TeacherPadApplication.IMThread.SendMessage("SetScreenLayout " + szSendData + " " + szTag, MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
        }
    }

    public void checkAndSetActiveScreen(Point pt) {
        Point testPoint = new Point(pt);
        testPoint.x = (int) (((float) testPoint.x) / this.mfScale);
        testPoint.y = (int) (((float) testPoint.y) / this.mfScale);
        TeacherPadApplication.mActiveScreenID = -1;
        for (int i = 0; i < TeacherPadApplication.marrMonitors.size(); i++) {
            if (new Rect(((MultiScreen) TeacherPadApplication.marrMonitors.get(i)).rectScreen).contains(testPoint.x, testPoint.y)) {
                TeacherPadApplication.mActiveScreenID = i;
                if (((MultiScreen) TeacherPadApplication.marrMonitors.get(i)).bMaximized) {
                    break;
                }
            }
        }
        invalidate();
    }

    public int checkInCorner(Point pt) {
        Point testPoint = new Point(pt);
        int nResult = -1;
        for (int i = 0; i < TeacherPadApplication.marrMonitors.size(); i++) {
            if (new Rect(((MultiScreen) TeacherPadApplication.marrMonitors.get(i)).rectCornerButton).contains(testPoint.x, testPoint.y)) {
                nResult = i;
                if (((MultiScreen) TeacherPadApplication.marrMonitors.get(i)).bMaximized) {
                    break;
                }
            }
        }
        return nResult;
    }

    public void setMonitorActiveButton(int nButtonID, int nActiveID, Drawable button) {
        if (nActiveID >= 0 && TeacherPadApplication.marrMonitors.size() > nActiveID) {
            ((MultiScreen) TeacherPadApplication.marrMonitors.get(nActiveID)).nCurrentFunctionButtonID = nButtonID;
            ((MultiScreen) TeacherPadApplication.marrMonitors.get(nActiveID)).drawableActiveButton = button;
            invalidate();
        }
    }

    protected void onDraw(Canvas canvas) {
        int i;
        Log.d("CustomMultiScreenView", "onDraw");
        Paint focusPaint = new Paint();
        Paint unfocusPaint = new Paint();
        focusPaint.setStrokeWidth((float) Utilities.dpToPixel(2, getContext()));
        focusPaint.setColor(-300587584);
        focusPaint.setStyle(Style.STROKE);
        focusPaint.setTextSize(24.0f);
        focusPaint.setTextAlign(Align.RIGHT);
        focusPaint.setShadowLayer(3.0f, 1.0f, 1.0f, -12303292);
        unfocusPaint.setStrokeWidth((float) Utilities.dpToPixel(2, getContext()));
        unfocusPaint.setColor(-291594594);
        unfocusPaint.setStyle(Style.STROKE);
        unfocusPaint.setTextSize(24.0f);
        unfocusPaint.setTextAlign(Align.RIGHT);
        boolean bSkipRest = false;
        for (i = 0; i < TeacherPadApplication.marrMonitors.size(); i++) {
            Rect rect = new Rect(((MultiScreen) TeacherPadApplication.marrMonitors.get(i)).rectScreen);
            if (((MultiScreen) TeacherPadApplication.marrMonitors.get(i)).bMaximized) {
                rect.left = (int) (this.mfScale * ((float) rect.left));
                rect.right = (int) (this.mfScale * ((float) rect.right));
                rect.top = (int) (this.mfScale * ((float) rect.top));
                rect.bottom = (int) (this.mfScale * ((float) rect.bottom));
                String szLabel = String.valueOf(i + 1);
                canvas.drawRect(rect, unfocusPaint);
                canvas.drawText(szLabel, (float) (rect.right - 4), (float) (rect.bottom - 4), unfocusPaint);
                bSkipRest = true;
            }
        }
        if (!bSkipRest) {
            for (i = 0; i < TeacherPadApplication.marrMonitors.size(); i++) {
                rect = new Rect(((MultiScreen) TeacherPadApplication.marrMonitors.get(i)).rectScreen);
                rect.left = (int) (this.mfScale * ((float) rect.left));
                rect.right = (int) (this.mfScale * ((float) rect.right));
                rect.top = (int) (this.mfScale * ((float) rect.top));
                rect.bottom = (int) (this.mfScale * ((float) rect.bottom));
                szLabel = String.valueOf(i + 1);
                if (TeacherPadApplication.mActiveScreenID != i) {
                    canvas.drawRect(rect, unfocusPaint);
                    canvas.drawText(szLabel, (float) (rect.right - 4), (float) (rect.bottom - 4), unfocusPaint);
                }
            }
        }
        if (TeacherPadApplication.mActiveScreenID != -1 && TeacherPadApplication.mActiveScreenID < TeacherPadApplication.marrMonitors.size()) {
            rect = new Rect(((MultiScreen) TeacherPadApplication.marrMonitors.get(TeacherPadApplication.mActiveScreenID)).rectScreen);
            rect.left = (int) (this.mfScale * ((float) rect.left));
            rect.right = (int) (this.mfScale * ((float) rect.right));
            rect.top = (int) (this.mfScale * ((float) rect.top));
            rect.bottom = (int) (this.mfScale * ((float) rect.bottom));
            szLabel = String.valueOf(TeacherPadApplication.mActiveScreenID + 1);
            canvas.drawRect(rect, focusPaint);
            canvas.drawText(szLabel, (float) (rect.right - 4), (float) (rect.bottom - 4), focusPaint);
            int nButtonWidth = Utilities.dpToPixel(35, getContext());
            int nButtonIconWidth = Utilities.dpToPixel(28, getContext());
            int nButtonPadding = Utilities.dpToPixel(3, getContext());
            MultiScreen activeScreen = (MultiScreen) TeacherPadApplication.marrMonitors.get(TeacherPadApplication.mActiveScreenID);
            focusPaint.setStyle(Style.FILL_AND_STROKE);
            rect = new Rect(((MultiScreen) TeacherPadApplication.marrMonitors.get(TeacherPadApplication.mActiveScreenID)).rectScreen);
            rect.left = (int) (this.mfScale * ((float) rect.left));
            rect.right = (int) (this.mfScale * ((float) rect.right));
            rect.top = (int) (this.mfScale * ((float) rect.top));
            rect.bottom = (int) (this.mfScale * ((float) rect.bottom));
            RectF rectButton = new RectF();
            rectButton.left = (float) (rect.left - nButtonWidth);
            rectButton.top = (float) (rect.bottom - nButtonWidth);
            rectButton.right = (float) (rect.left + nButtonWidth);
            rectButton.bottom = (float) (rect.bottom + nButtonWidth);
            rect = new Rect(rect.left, ((int) rectButton.top) - nButtonPadding, ((int) rectButton.right) + nButtonPadding, rect.bottom);
            activeScreen.rectCornerButton = rect;
            if (this.mCallBack != null) {
                this.mCallBack.onRepositionTools(rect);
            }
            activeScreen.rectOldCornerButton = rect;
            canvas.drawArc(rectButton, 0.0f, -90.0f, true, focusPaint);
            Rect rectImageButton = new Rect();
            Drawable button = ((MultiScreen) TeacherPadApplication.marrMonitors.get(TeacherPadApplication.mActiveScreenID)).drawableActiveButton;
            if (button == null) {
                button = new IconDrawable(getContext(), FontAwesomeIcons.fa_ellipsis_h).color(-1).actionBarSize();
            }
            rectImageButton.left = rect.left + nButtonPadding;
            rectImageButton.right = (rect.left + nButtonIconWidth) - nButtonPadding;
            rectImageButton.top = (rect.bottom - nButtonIconWidth) + nButtonPadding;
            rectImageButton.bottom = rect.bottom - nButtonPadding;
            button.setBounds(rectImageButton);
            button.draw(canvas);
        }
        super.onDraw(canvas);
    }
}
