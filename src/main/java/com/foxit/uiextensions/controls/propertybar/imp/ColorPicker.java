package com.foxit.uiextensions.controls.propertybar.imp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.support.v4.internal.view.SupportMenu;
import android.support.v4.view.InputDeviceCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.controls.propertybar.PropertyBar.UpdateViewListener;
import com.foxit.uiextensions.utils.AppDisplay;
import io.vov.vitamio.ThumbnailUtils;

public class ColorPicker extends View implements OnGestureListener {
    private GestureDetector detector;
    private AppDisplay display;
    private Bitmap mBitmapNormal;
    private Paint mBitmapPaint;
    private Bitmap mBitmapPress;
    private float mBitmapRadius;
    private int[] mBmpColor;
    private Context mContext;
    private int mCurrentColor;
    private Bitmap mGradualChangedBitmap;
    private int mHeight;
    private ViewGroup mParent;
    private PointF mSelectPoint;
    private int mSelfColorHeight;
    private int mSelfColorWidth;
    private boolean mShow;
    private UpdateViewListener mUpdateViewListener;
    private int mWidth;

    public interface ColorChangedListener {
        void onColorChanged(int i);
    }

    public ColorPicker(Context context, ViewGroup parent) {
        this(context, null, parent);
    }

    public ColorPicker(Context context, AttributeSet attrs, ViewGroup parent) {
        super(context, attrs);
        this.mBitmapRadius = 30.0f;
        this.mContext = context;
        this.mParent = parent;
        this.display = AppDisplay.getInstance(context);
        init();
    }

    private void init() {
        if (this.display.isPad()) {
            this.mSelfColorWidth = dp2px(ThumbnailUtils.TARGET_SIZE_MINI_THUMBNAIL_HEIGHT) - dp2px(80);
            this.mSelfColorHeight = this.display.dp2px(90.0f);
        } else {
            int tempWidth = this.mParent.getWidth();
            int tempHeight = this.mParent.getHeight();
            if (this.mContext.getResources().getConfiguration().orientation == 2) {
                if (tempHeight > tempWidth) {
                    tempWidth = tempHeight;
                }
                this.mSelfColorHeight = this.display.dp2px(42.0f);
            } else {
                if (tempWidth >= tempHeight) {
                    tempWidth = tempHeight;
                }
                this.mSelfColorHeight = this.display.dp2px(90.0f);
            }
            this.mSelfColorWidth = tempWidth - dp2px(72);
        }
        this.mBitmapPaint = new Paint();
        this.mBitmapNormal = BitmapFactory.decodeResource(this.mContext.getResources(), R.drawable.pb_colorpicker_point_selected);
        this.mBitmapRadius = (float) (this.mBitmapNormal.getWidth() / 2);
        this.mSelectPoint = new PointF((float) dp2px(100), (float) dp2px(30));
        this.detector = new GestureDetector(this);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.mWidth = this.mSelfColorWidth;
        this.mHeight = this.mSelfColorHeight;
        setMeasuredDimension(this.mWidth, this.mHeight);
    }

    @SuppressLint({"DrawAllocation"})
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(getGradual(), null, new Rect(0, 0, this.mWidth, this.mHeight), this.mBitmapPaint);
        if (this.mShow) {
            canvas.drawBitmap(this.mBitmapNormal, this.mSelectPoint.x - this.mBitmapRadius, this.mSelectPoint.y - this.mBitmapRadius, this.mBitmapPaint);
        }
        super.onDraw(canvas);
    }

    public boolean onSingleTapUp(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        this.mShow = true;
        proofDisk(x, y);
        this.mCurrentColor = getSelectColor(this.mSelectPoint.x, this.mSelectPoint.y);
        invalidate();
        if (this.mUpdateViewListener != null) {
            this.mUpdateViewListener.onUpdate(128, this.mCurrentColor);
        }
        return true;
    }

    protected void onDetachedFromWindow() {
        if (this.mGradualChangedBitmap != null && this.mGradualChangedBitmap.isRecycled()) {
            this.mGradualChangedBitmap.recycle();
        }
        if (this.mBitmapNormal != null && this.mBitmapNormal.isRecycled()) {
            this.mBitmapNormal.recycle();
        }
        if (this.mBitmapPress != null && this.mBitmapNormal.isRecycled()) {
            this.mBitmapPress.recycle();
        }
        super.onDetachedFromWindow();
    }

    public boolean onTouchEvent(MotionEvent event) {
        return this.detector.onTouchEvent(event);
    }

    private void proofDisk(float x, float y) {
        if (x < 0.0f) {
            this.mSelectPoint.x = 0.0f;
        } else if (x > ((float) this.mWidth)) {
            this.mSelectPoint.x = (float) this.mWidth;
        } else {
            this.mSelectPoint.x = x;
        }
        if (y < 0.0f) {
            this.mSelectPoint.y = 0.0f;
        } else if (y > ((float) this.mHeight)) {
            this.mSelectPoint.y = (float) this.mHeight;
        } else {
            this.mSelectPoint.y = y;
        }
    }

    private int getSelectColor(float x, float y) {
        Bitmap temp = getGradual();
        int intX = (int) x;
        int intY = (int) y;
        if (intX >= temp.getWidth()) {
            intX = temp.getWidth() - 1;
        }
        if (intY >= temp.getHeight()) {
            intY = temp.getHeight() - 1;
        }
        return temp.getPixel(intX, intY);
    }

    private Bitmap getGradual() {
        if (this.mGradualChangedBitmap == null) {
            Paint paint = new Paint();
            paint.setStrokeWidth(1.0f);
            this.mGradualChangedBitmap = Bitmap.createBitmap(this.mWidth, this.mHeight, Config.ARGB_8888);
            Canvas canvas = new Canvas(this.mGradualChangedBitmap);
            int bitmapWidth = this.mGradualChangedBitmap.getWidth();
            this.mWidth = bitmapWidth;
            int bitmapHeight = this.mGradualChangedBitmap.getHeight();
            float f = 0.0f;
            paint.setShader(new LinearGradient(0.0f, f, (float) bitmapWidth, 0.0f, new int[]{SupportMenu.CATEGORY_MASK, -65281, -16776961, -16711681, -16711936, InputDeviceCompat.SOURCE_ANY, SupportMenu.CATEGORY_MASK}, null, TileMode.MIRROR));
            canvas.drawRect(0.0f, 0.0f, (float) bitmapWidth, (float) bitmapHeight, paint);
        }
        return this.mGradualChangedBitmap;
    }

    public void setOnUpdateViewListener(UpdateViewListener listener) {
        this.mUpdateViewListener = listener;
    }

    public void setColor(int color) {
        this.mShow = false;
        this.mCurrentColor = color;
        invalidate();
    }

    public boolean onDown(MotionEvent e) {
        return true;
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    public void onLongPress(MotionEvent e) {
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    private int dp2px(int dip) {
        return this.display.dp2px((float) dip);
    }
}
