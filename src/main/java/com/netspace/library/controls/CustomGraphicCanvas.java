package com.netspace.library.controls;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;
import com.netspace.library.components.DrawComponent.DrawComponentGraphic;
import com.netspace.library.utilities.MoveableObject;
import com.netspace.library.utilities.MoveableObject.MoveEndCallBack;
import com.netspace.library.utilities.Utilities;

public class CustomGraphicCanvas extends View {
    private int mColor;
    private int mDataIndex;
    private DrawView mDrawView;
    private DrawComponentGraphic mGraphic;
    private MoveableObject mMoveableObject;
    private final int mPadding;
    private int mWidth;
    private float[] mX;
    private float[] mY;

    public CustomGraphicCanvas(Context context) {
        super(context);
        this.mX = new float[1000];
        this.mY = new float[1000];
        this.mDataIndex = 0;
        this.mColor = -16777216;
        this.mWidth = 3;
        this.mPadding = 10;
    }

    public CustomGraphicCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mX = new float[1000];
        this.mY = new float[1000];
        this.mDataIndex = 0;
        this.mColor = -16777216;
        this.mWidth = 3;
        this.mPadding = 10;
    }

    public CustomGraphicCanvas(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mX = new float[1000];
        this.mY = new float[1000];
        this.mDataIndex = 0;
        this.mColor = -16777216;
        this.mWidth = 3;
        this.mPadding = 10;
    }

    public void setDrawView(DrawView DrawView) {
        this.mDrawView = DrawView;
        this.mColor = DrawView.getColor();
    }

    public void setGraphic(DrawComponentGraphic Graphic) {
        this.mGraphic = Graphic;
    }

    public float[] getXPoints() {
        return this.mX;
    }

    public float[] getYPoints() {
        return this.mY;
    }

    public int getPadding() {
        return 10;
    }

    public int getDataCount() {
        return this.mDataIndex;
    }

    public void addPoint(float fX, float fY) {
        if (this.mDataIndex < this.mX.length - 1) {
            this.mX[this.mDataIndex] = fX;
            this.mY[this.mDataIndex] = fY;
            this.mDataIndex++;
        }
    }

    public int getColor() {
        return this.mColor;
    }

    public int getLineWidth() {
        return this.mWidth;
    }

    public DrawView getDrawView() {
        return this.mDrawView;
    }

    @SuppressLint({"WrongCall"})
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (this.mGraphic != null) {
            this.mGraphic.onDrawPreviewContent(this, canvas);
        }
    }

    private void resizeSelf() {
        int i;
        int nTop = getHeight();
        int nLeft = getWidth();
        int nRight = 0;
        int nBottom = 0;
        for (i = 0; i < this.mDataIndex; i++) {
            if (((float) nTop) > this.mY[i]) {
                nTop = (int) this.mY[i];
            }
            if (((float) nBottom) < this.mY[i]) {
                nBottom = (int) this.mY[i];
            }
            if (((float) nLeft) > this.mX[i]) {
                nLeft = (int) this.mX[i];
            }
            if (((float) nRight) < this.mX[i]) {
                nRight = (int) this.mX[i];
            }
        }
        nTop -= 10;
        nLeft -= 10;
        nRight += 10;
        nBottom += 10;
        for (i = 0; i < this.mDataIndex; i++) {
            float[] fArr = this.mY;
            fArr[i] = fArr[i] - ((float) nTop);
            fArr = this.mX;
            fArr[i] = fArr[i] - ((float) nLeft);
        }
        LayoutParams LayoutParams = (LayoutParams) getLayoutParams();
        LayoutParams.topMargin += nTop;
        LayoutParams.leftMargin += nLeft;
        LayoutParams.width = nRight - nLeft;
        LayoutParams.height = nBottom - nTop;
        setLayoutParams(LayoutParams);
        Utilities.runOnUIThread(getContext(), new Runnable() {
            public void run() {
                if (CustomGraphicCanvas.this.mGraphic != null) {
                    CustomGraphicCanvas.this.mGraphic.onMoveObjectResize(CustomGraphicCanvas.this);
                }
            }
        });
    }

    public void drawTipText(Canvas canvas, String szText) {
        Paint paint = new Paint();
        paint.setColor(-16777216);
        paint.setTextSize(22.0f);
        paint.setTextAlign(Align.LEFT);
        paint.setAntiAlias(true);
        canvas.drawText(szText, 20.0f, 40.0f, paint);
    }

    private void prepareMove() {
        this.mMoveableObject = new MoveableObject(getContext());
        this.mMoveableObject.setMoveEndCallBack(new MoveEndCallBack() {
            public void onMoveEnd(View v) {
                Utilities.runOnUIThread(CustomGraphicCanvas.this.getContext(), new Runnable() {
                    public void run() {
                        if (CustomGraphicCanvas.this.mGraphic != null) {
                            CustomGraphicCanvas.this.mGraphic.onMoveObjectResize(CustomGraphicCanvas.this);
                        }
                    }
                });
            }

            public void onMoving(View v) {
                Utilities.runOnUIThread(CustomGraphicCanvas.this.getContext(), new Runnable() {
                    public void run() {
                        if (CustomGraphicCanvas.this.mGraphic != null) {
                            CustomGraphicCanvas.this.mGraphic.onMoveObjectResize(CustomGraphicCanvas.this);
                        }
                    }
                });
            }
        });
        this.mMoveableObject.prepareMoveObject(this);
        this.mMoveableObject.setFullHandleOnTouch(true);
        this.mMoveableObject.setAllowResize(false);
        setOnTouchListener(this.mMoveableObject);
    }

    public boolean isReadyToMove() {
        return this.mMoveableObject != null;
    }

    public boolean onTouchEvent(MotionEvent event) {
        int eventaction = event.getAction();
        if (this.mGraphic == null) {
            return super.onTouchEvent(event);
        }
        if (eventaction == 0) {
            if (this.mDataIndex < this.mX.length - 1) {
                this.mX[this.mDataIndex] = event.getX();
                this.mY[this.mDataIndex] = event.getY();
                this.mDataIndex++;
                invalidate();
                if (!this.mGraphic.addPoint(this, event.getX(), event.getY())) {
                    prepareMove();
                    this.mGraphic.onPrepareMoveObject(this, this.mMoveableObject);
                    resizeSelf();
                    invalidate();
                }
            } else {
                Toast.makeText(getContext(), "超过最大允许点数", 0).show();
            }
        }
        return true;
    }

    public void measureDataToDrawView() {
        float nXOffset = ((float) getLeft()) - ((float) this.mDrawView.getLeft());
        float nYOffset = ((float) getTop()) - ((float) this.mDrawView.getTop());
        if (this.mGraphic != null) {
            this.mGraphic.measureToDrawView(this, this.mDrawView, nXOffset, nYOffset);
            this.mDrawView.invalidate();
        }
    }
}
