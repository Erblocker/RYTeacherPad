package com.netspace.library.controls;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.v4.view.ScaleGestureDetectorCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.widget.ImageView.ScaleType;
import com.netspace.library.controls.AutoResetMode.Parser;
import com.netspace.pad.library.R;

public class ZoomageView extends AppCompatImageView implements OnScaleGestureListener {
    private final float MAX_SCALE;
    private final float MIN_SCALE;
    private final int RESET_DURATION;
    private boolean animateOnReset;
    private boolean autoCenter;
    private int autoResetMode;
    private final RectF bounds;
    private float calculatedMaxScale;
    private float calculatedMinScale;
    private PointF last;
    private GestureDetector mGestureDetector;
    private Matrix matrix;
    private float[] matrixValues;
    private float maxScale;
    private float mfDownX;
    private float mfDownY;
    private float minScale;
    private int previousPointerCount;
    private boolean restrictBounds;
    private float scaleBy;
    private ScaleGestureDetector scaleDetector;
    private Matrix startMatrix;
    private float startScale;
    private ScaleType startScaleType;
    private float[] startValues;
    private boolean translatable;
    private boolean zoomable;

    public ZoomageView(Context context) {
        this(context, null);
    }

    public ZoomageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.MIN_SCALE = 0.6f;
        this.MAX_SCALE = 8.0f;
        this.RESET_DURATION = 200;
        this.matrix = new Matrix();
        this.startMatrix = new Matrix();
        this.matrixValues = new float[9];
        this.startValues = null;
        this.minScale = 0.6f;
        this.maxScale = 8.0f;
        this.calculatedMinScale = 0.6f;
        this.calculatedMaxScale = 8.0f;
        this.bounds = new RectF();
        this.last = new PointF(0.0f, 0.0f);
        this.mfDownX = 0.0f;
        this.mfDownY = 0.0f;
        this.startScale = 1.0f;
        this.scaleBy = 1.0f;
        this.previousPointerCount = 1;
        this.scaleDetector = new ScaleGestureDetector(context, this);
        ScaleGestureDetectorCompat.setQuickScaleEnabled(this.scaleDetector, false);
        this.startScaleType = getScaleType();
        TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.ZoomageView);
        this.zoomable = values.getBoolean(R.styleable.ZoomageView_zoomage_zoomable, true);
        this.translatable = values.getBoolean(R.styleable.ZoomageView_zoomage_translatable, true);
        this.animateOnReset = values.getBoolean(R.styleable.ZoomageView_zoomage_animateOnReset, true);
        this.autoCenter = values.getBoolean(R.styleable.ZoomageView_zoomage_autoCenter, true);
        this.restrictBounds = values.getBoolean(R.styleable.ZoomageView_zoomage_restrictBounds, false);
        this.minScale = values.getFloat(R.styleable.ZoomageView_zoomage_minScale, 0.6f);
        this.maxScale = values.getFloat(R.styleable.ZoomageView_zoomage_maxScale, 8.0f);
        this.autoResetMode = Parser.fromInt(values.getInt(R.styleable.ZoomageView_zoomage_autoResetMode, 0));
        this.mGestureDetector = new GestureDetector(getContext(), new SimpleOnGestureListener() {
            public boolean onSingleTapConfirmed(MotionEvent e) {
                Log.d("ZoomageView", "onSingleTapConfirmed");
                View parentView = (View) ZoomageView.this.getParent();
                while (parentView != null && !parentView.performClick()) {
                    parentView = (View) parentView.getParent();
                }
                return true;
            }
        });
        verifyScaleRange();
        values.recycle();
    }

    private void verifyScaleRange() {
        if (this.minScale >= this.maxScale) {
            throw new IllegalStateException("minScale must be less than maxScale");
        } else if (this.minScale < 0.0f) {
            throw new IllegalStateException("minScale must be greater than 0");
        } else if (this.maxScale < 0.0f) {
            throw new IllegalStateException("maxScale must be greater than 0");
        }
    }

    public void setScaleRange(float minScale, float maxScale) {
        this.minScale = minScale;
        this.maxScale = maxScale;
        this.startValues = null;
        verifyScaleRange();
    }

    public boolean isTranslatable() {
        return this.translatable;
    }

    public void setTranslatable(boolean translatable) {
        this.translatable = translatable;
    }

    public boolean isZoomable() {
        return this.zoomable;
    }

    public void setZoomable(boolean zoomable) {
        this.zoomable = zoomable;
    }

    public boolean getRestrictBounds() {
        return this.restrictBounds;
    }

    public void setRestrictBounds(boolean restrictBounds) {
        this.restrictBounds = restrictBounds;
    }

    public boolean getAnimateOnReset() {
        return this.animateOnReset;
    }

    public void setAnimateOnReset(boolean animateOnReset) {
        this.animateOnReset = animateOnReset;
    }

    public int getAutoResetMode() {
        return this.autoResetMode;
    }

    public void setAutoResetMode(int autoReset) {
        this.autoResetMode = autoReset;
    }

    public boolean getAutoCenter() {
        return this.autoCenter;
    }

    public void setAutoCenter(boolean autoCenter) {
        this.autoCenter = autoCenter;
    }

    public void setScaleType(ScaleType scaleType) {
        super.setScaleType(scaleType);
        this.startScaleType = scaleType;
        this.startValues = null;
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            setScaleType(this.startScaleType);
        }
    }

    private void updateBounds(float[] values) {
        if (getDrawable() != null) {
            this.bounds.set(values[2], values[5], (((float) getDrawable().getIntrinsicWidth()) * values[0]) + values[2], (((float) getDrawable().getIntrinsicHeight()) * values[4]) + values[5]);
        }
    }

    private float getCurrentDisplayedWidth() {
        if (getDrawable() != null) {
            return ((float) getDrawable().getIntrinsicWidth()) * this.matrixValues[0];
        }
        return 0.0f;
    }

    private float getCurrentDisplayedHeight() {
        if (getDrawable() != null) {
            return ((float) getDrawable().getIntrinsicHeight()) * this.matrixValues[4];
        }
        return 0.0f;
    }

    private void setStartValues() {
        this.startValues = new float[9];
        this.startMatrix = new Matrix(getImageMatrix());
        this.startMatrix.getValues(this.startValues);
        this.calculatedMinScale = this.minScale * this.startValues[0];
        this.calculatedMaxScale = this.maxScale * this.startValues[0];
    }

    public boolean onTouchEvent(MotionEvent event) {
        this.mGestureDetector.onTouchEvent(event);
        if (!isEnabled() || (!this.zoomable && !this.translatable)) {
            return super.onTouchEvent(event);
        }
        if (getScaleType() != ScaleType.MATRIX) {
            super.setScaleType(ScaleType.MATRIX);
        }
        if (this.startValues == null) {
            setStartValues();
        }
        this.matrix.set(getImageMatrix());
        this.matrix.getValues(this.matrixValues);
        updateBounds(this.matrixValues);
        this.scaleDetector.onTouchEvent(event);
        if (event.getActionMasked() == 0) {
            this.mfDownX = event.getX();
            this.mfDownY = event.getY();
        }
        if (event.getActionMasked() == 0 || event.getPointerCount() != this.previousPointerCount) {
            this.last.set(this.scaleDetector.getFocusX(), this.scaleDetector.getFocusY());
        } else if (event.getActionMasked() == 2) {
            float focusx = this.scaleDetector.getFocusX();
            float focusy = this.scaleDetector.getFocusY();
            if (Math.abs(this.mfDownX - event.getX()) >= 10.0f || Math.abs(this.mfDownY - event.getY()) >= 10.0f) {
                this.mfDownX = -1000.0f;
                this.mfDownY = -1000.0f;
                if (this.translatable) {
                    this.matrix.postTranslate(getXDistance(focusx, this.last.x), getYDistance(focusy, this.last.y));
                }
                if (this.zoomable) {
                    this.matrix.postScale(this.scaleBy, this.scaleBy, focusx, focusy);
                }
                setImageMatrix(this.matrix);
                this.last.set(focusx, focusy);
            }
        }
        if (event.getActionMasked() == 1) {
            this.scaleBy = 1.0f;
            resetImage();
        }
        this.previousPointerCount = event.getPointerCount();
        return true;
    }

    private void resetImage() {
        switch (this.autoResetMode) {
            case 0:
                if (this.matrixValues[0] <= this.startValues[0]) {
                    reset();
                    return;
                } else {
                    center();
                    return;
                }
            case 1:
                if (this.matrixValues[0] >= this.startValues[0]) {
                    reset();
                    return;
                } else {
                    center();
                    return;
                }
            case 2:
                reset();
                return;
            case 3:
                center();
                return;
            default:
                return;
        }
    }

    private void center() {
        if (this.autoCenter) {
            animateTranslationX();
            animateTranslationY();
        }
    }

    public void reset() {
        reset(this.animateOnReset);
    }

    public void reset(boolean animate) {
        if (animate) {
            animateToStartMatrix();
        } else {
            setImageMatrix(this.startMatrix);
        }
    }

    private void animateToStartMatrix() {
        final Matrix beginMatrix = new Matrix(getImageMatrix());
        beginMatrix.getValues(this.matrixValues);
        final float xsdiff = this.startValues[0] - this.matrixValues[0];
        final float ysdiff = this.startValues[4] - this.matrixValues[4];
        final float xtdiff = this.startValues[2] - this.matrixValues[2];
        final float ytdiff = this.startValues[5] - this.matrixValues[5];
        ValueAnimator anim = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        anim.addUpdateListener(new AnimatorUpdateListener() {
            final Matrix activeMatrix;
            final float[] values = new float[9];

            public void onAnimationUpdate(ValueAnimator animation) {
                float val = ((Float) animation.getAnimatedValue()).floatValue();
                this.activeMatrix.set(beginMatrix);
                this.activeMatrix.getValues(this.values);
                this.values[2] = this.values[2] + (xtdiff * val);
                this.values[5] = this.values[5] + (ytdiff * val);
                this.values[0] = this.values[0] + (xsdiff * val);
                this.values[4] = this.values[4] + (ysdiff * val);
                this.activeMatrix.setValues(this.values);
                ZoomageView.this.setImageMatrix(this.activeMatrix);
            }
        });
        anim.setDuration(200);
        anim.start();
    }

    private void animateTranslationX() {
        if (getCurrentDisplayedWidth() > ((float) getWidth())) {
            if (this.bounds.left > 0.0f) {
                animateMatrixIndex(2, 0.0f);
            } else if (this.bounds.right < ((float) getWidth())) {
                animateMatrixIndex(2, (this.bounds.left + ((float) getWidth())) - this.bounds.right);
            }
        } else if (this.bounds.left < 0.0f) {
            animateMatrixIndex(2, 0.0f);
        } else if (this.bounds.right > ((float) getWidth())) {
            animateMatrixIndex(2, (this.bounds.left + ((float) getWidth())) - this.bounds.right);
        }
    }

    private void animateTranslationY() {
        if (getCurrentDisplayedHeight() > ((float) getHeight())) {
            if (this.bounds.top > 0.0f) {
                animateMatrixIndex(5, 0.0f);
            } else if (this.bounds.bottom < ((float) getHeight())) {
                animateMatrixIndex(5, (this.bounds.top + ((float) getHeight())) - this.bounds.bottom);
            }
        } else if (this.bounds.top < 0.0f) {
            animateMatrixIndex(5, 0.0f);
        } else if (this.bounds.bottom > ((float) getHeight())) {
            animateMatrixIndex(5, (this.bounds.top + ((float) getHeight())) - this.bounds.bottom);
        }
    }

    private void animateMatrixIndex(final int index, float to) {
        ValueAnimator animator = ValueAnimator.ofFloat(new float[]{this.matrixValues[index], to});
        animator.addUpdateListener(new AnimatorUpdateListener() {
            Matrix current = new Matrix();
            final float[] values = new float[9];

            public void onAnimationUpdate(ValueAnimator animation) {
                this.current.set(ZoomageView.this.getImageMatrix());
                this.current.getValues(this.values);
                this.values[index] = ((Float) animation.getAnimatedValue()).floatValue();
                this.current.setValues(this.values);
                ZoomageView.this.setImageMatrix(this.current);
            }
        });
        animator.setDuration(200);
        animator.start();
    }

    private float getXDistance(float toX, float fromX) {
        float xdistance = toX - fromX;
        if (this.restrictBounds) {
            xdistance = getRestrictedXDistance(xdistance);
        }
        if (this.bounds.right + xdistance < 0.0f) {
            return -this.bounds.right;
        }
        if (this.bounds.left + xdistance > ((float) getWidth())) {
            return ((float) getWidth()) - this.bounds.left;
        }
        return xdistance;
    }

    private float getRestrictedXDistance(float xdistance) {
        float restrictedXDistance = xdistance;
        if (getCurrentDisplayedWidth() >= ((float) getWidth())) {
            if (this.bounds.left <= 0.0f && this.bounds.left + xdistance > 0.0f && !this.scaleDetector.isInProgress()) {
                return -this.bounds.left;
            }
            if (this.bounds.right < ((float) getWidth()) || this.bounds.right + xdistance >= ((float) getWidth()) || this.scaleDetector.isInProgress()) {
                return restrictedXDistance;
            }
            return ((float) getWidth()) - this.bounds.right;
        } else if (this.scaleDetector.isInProgress()) {
            return restrictedXDistance;
        } else {
            if (this.bounds.left >= 0.0f && this.bounds.left + xdistance < 0.0f) {
                return -this.bounds.left;
            }
            if (this.bounds.right > ((float) getWidth()) || this.bounds.right + xdistance <= ((float) getWidth())) {
                return restrictedXDistance;
            }
            return ((float) getWidth()) - this.bounds.right;
        }
    }

    private float getYDistance(float toY, float fromY) {
        float ydistance = toY - fromY;
        if (this.restrictBounds) {
            ydistance = getRestrictedYDistance(ydistance);
        }
        if (this.bounds.bottom + ydistance < 0.0f) {
            return -this.bounds.bottom;
        }
        if (this.bounds.top + ydistance > ((float) getHeight())) {
            return ((float) getHeight()) - this.bounds.top;
        }
        return ydistance;
    }

    private float getRestrictedYDistance(float ydistance) {
        float restrictedYDistance = ydistance;
        if (getCurrentDisplayedHeight() >= ((float) getHeight())) {
            if (this.bounds.top <= 0.0f && this.bounds.top + ydistance > 0.0f && !this.scaleDetector.isInProgress()) {
                return -this.bounds.top;
            }
            if (this.bounds.bottom < ((float) getHeight()) || this.bounds.bottom + ydistance >= ((float) getHeight()) || this.scaleDetector.isInProgress()) {
                return restrictedYDistance;
            }
            return ((float) getHeight()) - this.bounds.bottom;
        } else if (this.scaleDetector.isInProgress()) {
            return restrictedYDistance;
        } else {
            if (this.bounds.top >= 0.0f && this.bounds.top + ydistance < 0.0f) {
                return -this.bounds.top;
            }
            if (this.bounds.bottom > ((float) getHeight()) || this.bounds.bottom + ydistance <= ((float) getHeight())) {
                return restrictedYDistance;
            }
            return ((float) getHeight()) - this.bounds.bottom;
        }
    }

    public boolean onScale(ScaleGestureDetector detector) {
        this.scaleBy = (this.startScale * detector.getScaleFactor()) / this.matrixValues[0];
        float projectedScale = this.scaleBy * this.matrixValues[0];
        if (projectedScale < this.calculatedMinScale) {
            this.scaleBy = this.calculatedMinScale / this.matrixValues[0];
        } else if (projectedScale > this.calculatedMaxScale) {
            this.scaleBy = this.calculatedMaxScale / this.matrixValues[0];
        }
        return false;
    }

    public boolean onScaleBegin(ScaleGestureDetector detector) {
        this.startScale = this.matrixValues[0];
        return true;
    }

    public void onScaleEnd(ScaleGestureDetector detector) {
        this.scaleBy = 1.0f;
    }
}
