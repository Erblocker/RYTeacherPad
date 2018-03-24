package com.netspace.library.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import com.netspace.pad.library.R;

public class ColorPickerView extends View {
    private static final float BORDER_WIDTH_PX = 1.0f;
    private static final int PANEL_ALPHA = 2;
    private static final int PANEL_HUE = 1;
    private static final int PANEL_SAT_VAL = 0;
    private static float mDensity = BORDER_WIDTH_PX;
    private float ALPHA_PANEL_HEIGHT;
    private float HUE_PANEL_WIDTH;
    private float PALETTE_CIRCLE_TRACKER_RADIUS;
    private float PANEL_SPACING;
    private float RECTANGLE_TRACKER_OFFSET;
    private int mAlpha;
    private Paint mAlphaPaint;
    private AlphaPatternDrawable mAlphaPattern;
    private RectF mAlphaRect;
    private Shader mAlphaShader;
    private String mAlphaSliderText;
    private Paint mAlphaTextPaint;
    private int mBorderColor;
    private Paint mBorderPaint;
    private int mDrawingOffset;
    private RectF mDrawingRect;
    private float mHue;
    private Paint mHueAlphaTrackerPaint;
    private Paint mHuePaint;
    private RectF mHueRect;
    private Shader mHueShader;
    private int mLastTouchedPanel;
    private OnColorChangedListener mListener;
    private float mSat;
    private Shader mSatShader;
    private BitmapCache mSatValBackgroundCache;
    private Paint mSatValPaint;
    private RectF mSatValRect;
    private Paint mSatValTrackerPaint;
    private boolean mShowAlphaPanel;
    private int mSliderTrackerColor;
    private Point mStartTouchPoint;
    private float mVal;
    private Shader mValShader;

    private class BitmapCache {
        public Bitmap bitmap;
        public Canvas canvas;
        public float value;

        private BitmapCache() {
        }
    }

    public interface OnColorChangedListener {
        void onColorChanged(int i);
    }

    public ColorPickerView(Context context) {
        this(context, null);
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.HUE_PANEL_WIDTH = 30.0f;
        this.ALPHA_PANEL_HEIGHT = 20.0f;
        this.PANEL_SPACING = 10.0f;
        this.PALETTE_CIRCLE_TRACKER_RADIUS = 5.0f;
        this.RECTANGLE_TRACKER_OFFSET = 2.0f;
        this.mAlpha = 255;
        this.mHue = 360.0f;
        this.mSat = 0.0f;
        this.mVal = 0.0f;
        this.mAlphaSliderText = null;
        this.mSliderTrackerColor = -4342339;
        this.mBorderColor = -9539986;
        this.mShowAlphaPanel = false;
        this.mLastTouchedPanel = 0;
        this.mStartTouchPoint = null;
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ColorPickerView);
        this.mShowAlphaPanel = a.getBoolean(R.styleable.ColorPickerView_alphaChannelVisible, false);
        this.mAlphaSliderText = a.getString(R.styleable.ColorPickerView_alphaChannelText);
        this.mSliderTrackerColor = a.getColor(R.styleable.ColorPickerView_colorPickerSliderColor, -4342339);
        this.mBorderColor = a.getColor(R.styleable.ColorPickerView_colorPickerBorderColor, -9539986);
        a.recycle();
        mDensity = getContext().getResources().getDisplayMetrics().density;
        this.PALETTE_CIRCLE_TRACKER_RADIUS *= mDensity;
        this.RECTANGLE_TRACKER_OFFSET *= mDensity;
        this.HUE_PANEL_WIDTH *= mDensity;
        this.ALPHA_PANEL_HEIGHT *= mDensity;
        this.PANEL_SPACING *= mDensity;
        this.mDrawingOffset = calculateRequiredOffset();
        initPaintTools();
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    private void initPaintTools() {
        this.mSatValPaint = new Paint();
        this.mSatValTrackerPaint = new Paint();
        this.mHuePaint = new Paint();
        this.mHueAlphaTrackerPaint = new Paint();
        this.mAlphaPaint = new Paint();
        this.mAlphaTextPaint = new Paint();
        this.mBorderPaint = new Paint();
        this.mSatValTrackerPaint.setStyle(Style.STROKE);
        this.mSatValTrackerPaint.setStrokeWidth(mDensity * 2.0f);
        this.mSatValTrackerPaint.setAntiAlias(true);
        this.mHueAlphaTrackerPaint.setColor(this.mSliderTrackerColor);
        this.mHueAlphaTrackerPaint.setStyle(Style.STROKE);
        this.mHueAlphaTrackerPaint.setStrokeWidth(mDensity * 2.0f);
        this.mHueAlphaTrackerPaint.setAntiAlias(true);
        this.mAlphaTextPaint.setColor(-14935012);
        this.mAlphaTextPaint.setTextSize(14.0f * mDensity);
        this.mAlphaTextPaint.setAntiAlias(true);
        this.mAlphaTextPaint.setTextAlign(Align.CENTER);
        this.mAlphaTextPaint.setFakeBoldText(true);
    }

    private int calculateRequiredOffset() {
        return (int) (1.5f * Math.max(Math.max(this.PALETTE_CIRCLE_TRACKER_RADIUS, this.RECTANGLE_TRACKER_OFFSET), BORDER_WIDTH_PX * mDensity));
    }

    private int[] buildHueColorArray() {
        int[] hue = new int[361];
        int count = 0;
        int i = hue.length - 1;
        while (i >= 0) {
            hue[count] = Color.HSVToColor(new float[]{(float) i, BORDER_WIDTH_PX, BORDER_WIDTH_PX});
            i--;
            count++;
        }
        return hue;
    }

    protected void onDraw(Canvas canvas) {
        if (this.mDrawingRect.width() > 0.0f && this.mDrawingRect.height() > 0.0f) {
            drawSatValPanel(canvas);
            drawHuePanel(canvas);
            drawAlphaPanel(canvas);
        }
    }

    private void drawSatValPanel(Canvas canvas) {
        RectF rect = this.mSatValRect;
        this.mBorderPaint.setColor(this.mBorderColor);
        canvas.drawRect(this.mDrawingRect.left, this.mDrawingRect.top, BORDER_WIDTH_PX + rect.right, BORDER_WIDTH_PX + rect.bottom, this.mBorderPaint);
        if (this.mValShader == null) {
            this.mValShader = new LinearGradient(rect.left, rect.top, rect.left, rect.bottom, -1, -16777216, TileMode.CLAMP);
        }
        if (this.mSatValBackgroundCache == null || this.mSatValBackgroundCache.value != this.mHue) {
            if (this.mSatValBackgroundCache == null) {
                this.mSatValBackgroundCache = new BitmapCache();
            }
            if (this.mSatValBackgroundCache.bitmap == null) {
                this.mSatValBackgroundCache.bitmap = Bitmap.createBitmap((int) rect.width(), (int) rect.height(), Config.ARGB_8888);
            }
            if (this.mSatValBackgroundCache.canvas == null) {
                this.mSatValBackgroundCache.canvas = new Canvas(this.mSatValBackgroundCache.bitmap);
            }
            this.mSatShader = new LinearGradient(rect.left, rect.top, rect.right, rect.top, -1, Color.HSVToColor(new float[]{this.mHue, BORDER_WIDTH_PX, BORDER_WIDTH_PX}), TileMode.CLAMP);
            this.mSatValPaint.setShader(new ComposeShader(this.mValShader, this.mSatShader, Mode.MULTIPLY));
            this.mSatValBackgroundCache.canvas.drawRect(0.0f, 0.0f, (float) this.mSatValBackgroundCache.bitmap.getWidth(), (float) this.mSatValBackgroundCache.bitmap.getHeight(), this.mSatValPaint);
            this.mSatValBackgroundCache.value = this.mHue;
        }
        canvas.drawBitmap(this.mSatValBackgroundCache.bitmap, null, rect, null);
        Point p = satValToPoint(this.mSat, this.mVal);
        this.mSatValTrackerPaint.setColor(-16777216);
        canvas.drawCircle((float) p.x, (float) p.y, this.PALETTE_CIRCLE_TRACKER_RADIUS - (BORDER_WIDTH_PX * mDensity), this.mSatValTrackerPaint);
        this.mSatValTrackerPaint.setColor(-2236963);
        canvas.drawCircle((float) p.x, (float) p.y, this.PALETTE_CIRCLE_TRACKER_RADIUS, this.mSatValTrackerPaint);
    }

    private void drawHuePanel(Canvas canvas) {
        RectF rect = this.mHueRect;
        this.mBorderPaint.setColor(this.mBorderColor);
        canvas.drawRect(rect.left - BORDER_WIDTH_PX, rect.top - BORDER_WIDTH_PX, rect.right + BORDER_WIDTH_PX, BORDER_WIDTH_PX + rect.bottom, this.mBorderPaint);
        if (this.mHueShader == null) {
            this.mHueShader = new LinearGradient(0.0f, 0.0f, 0.0f, rect.height(), buildHueColorArray(), null, TileMode.CLAMP);
            this.mHuePaint.setShader(this.mHueShader);
        }
        canvas.drawRect(rect, this.mHuePaint);
        float rectHeight = (4.0f * mDensity) / 2.0f;
        Point p = hueToPoint(this.mHue);
        RectF r = new RectF();
        r.left = rect.left - this.RECTANGLE_TRACKER_OFFSET;
        r.right = rect.right + this.RECTANGLE_TRACKER_OFFSET;
        r.top = ((float) p.y) - rectHeight;
        r.bottom = ((float) p.y) + rectHeight;
        canvas.drawRoundRect(r, 2.0f, 2.0f, this.mHueAlphaTrackerPaint);
    }

    private void drawAlphaPanel(Canvas canvas) {
        if (this.mShowAlphaPanel && this.mAlphaRect != null && this.mAlphaPattern != null) {
            RectF rect = this.mAlphaRect;
            this.mBorderPaint.setColor(this.mBorderColor);
            canvas.drawRect(rect.left - BORDER_WIDTH_PX, rect.top - BORDER_WIDTH_PX, BORDER_WIDTH_PX + rect.right, BORDER_WIDTH_PX + rect.bottom, this.mBorderPaint);
            this.mAlphaPattern.draw(canvas);
            float[] hsv = new float[]{this.mHue, this.mSat, this.mVal};
            this.mAlphaShader = new LinearGradient(rect.left, rect.top, rect.right, rect.top, Color.HSVToColor(hsv), Color.HSVToColor(0, hsv), TileMode.CLAMP);
            this.mAlphaPaint.setShader(this.mAlphaShader);
            canvas.drawRect(rect, this.mAlphaPaint);
            if (!(this.mAlphaSliderText == null || this.mAlphaSliderText.equals(""))) {
                canvas.drawText(this.mAlphaSliderText, rect.centerX(), rect.centerY() + (4.0f * mDensity), this.mAlphaTextPaint);
            }
            float rectWidth = (4.0f * mDensity) / 2.0f;
            Point p = alphaToPoint(this.mAlpha);
            RectF r = new RectF();
            r.left = ((float) p.x) - rectWidth;
            r.right = ((float) p.x) + rectWidth;
            r.top = rect.top - this.RECTANGLE_TRACKER_OFFSET;
            r.bottom = rect.bottom + this.RECTANGLE_TRACKER_OFFSET;
            canvas.drawRoundRect(r, 2.0f, 2.0f, this.mHueAlphaTrackerPaint);
        }
    }

    private Point hueToPoint(float hue) {
        RectF rect = this.mHueRect;
        float height = rect.height();
        Point p = new Point();
        p.y = (int) ((height - ((hue * height) / 360.0f)) + rect.top);
        p.x = (int) rect.left;
        return p;
    }

    private Point satValToPoint(float sat, float val) {
        RectF rect = this.mSatValRect;
        float height = rect.height();
        float width = rect.width();
        Point p = new Point();
        p.x = (int) ((sat * width) + rect.left);
        p.y = (int) (((BORDER_WIDTH_PX - val) * height) + rect.top);
        return p;
    }

    private Point alphaToPoint(int alpha) {
        RectF rect = this.mAlphaRect;
        float width = rect.width();
        Point p = new Point();
        p.x = (int) ((width - ((((float) alpha) * width) / 255.0f)) + rect.left);
        p.y = (int) rect.top;
        return p;
    }

    private float[] pointToSatVal(float x, float y) {
        RectF rect = this.mSatValRect;
        float[] result = new float[2];
        float width = rect.width();
        float height = rect.height();
        if (x < rect.left) {
            x = 0.0f;
        } else if (x > rect.right) {
            x = width;
        } else {
            x -= rect.left;
        }
        if (y < rect.top) {
            y = 0.0f;
        } else if (y > rect.bottom) {
            y = height;
        } else {
            y -= rect.top;
        }
        result[0] = (BORDER_WIDTH_PX / width) * x;
        result[1] = BORDER_WIDTH_PX - ((BORDER_WIDTH_PX / height) * y);
        return result;
    }

    private float pointToHue(float y) {
        RectF rect = this.mHueRect;
        float height = rect.height();
        if (y < rect.top) {
            y = 0.0f;
        } else if (y > rect.bottom) {
            y = height;
        } else {
            y -= rect.top;
        }
        return 360.0f - ((y * 360.0f) / height);
    }

    private int pointToAlpha(int x) {
        RectF rect = this.mAlphaRect;
        int width = (int) rect.width();
        if (((float) x) < rect.left) {
            x = 0;
        } else if (((float) x) > rect.right) {
            x = width;
        } else {
            x -= (int) rect.left;
        }
        return 255 - ((x * 255) / width);
    }

    public boolean onTrackballEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        boolean update = false;
        if (event.getAction() == 2) {
            switch (this.mLastTouchedPanel) {
                case 0:
                    float sat = this.mSat + (x / 50.0f);
                    float val = this.mVal - (y / 50.0f);
                    if (sat < 0.0f) {
                        sat = 0.0f;
                    } else if (sat > BORDER_WIDTH_PX) {
                        sat = BORDER_WIDTH_PX;
                    }
                    if (val < 0.0f) {
                        val = 0.0f;
                    } else if (val > BORDER_WIDTH_PX) {
                        val = BORDER_WIDTH_PX;
                    }
                    this.mSat = sat;
                    this.mVal = val;
                    update = true;
                    break;
                case 1:
                    float hue = this.mHue - (y * 10.0f);
                    if (hue < 0.0f) {
                        hue = 0.0f;
                    } else if (hue > 360.0f) {
                        hue = 360.0f;
                    }
                    this.mHue = hue;
                    update = true;
                    break;
                case 2:
                    if (this.mShowAlphaPanel && this.mAlphaRect != null) {
                        int alpha = (int) (((float) this.mAlpha) - (x * 10.0f));
                        if (alpha < 0) {
                            alpha = 0;
                        } else if (alpha > 255) {
                            alpha = 255;
                        }
                        this.mAlpha = alpha;
                        update = true;
                        break;
                    }
                    update = false;
                    break;
                    break;
            }
        }
        if (!update) {
            return super.onTrackballEvent(event);
        }
        if (this.mListener != null) {
            this.mListener.onColorChanged(Color.HSVToColor(this.mAlpha, new float[]{this.mHue, this.mSat, this.mVal}));
        }
        invalidate();
        return true;
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean update = false;
        switch (event.getAction()) {
            case 0:
                this.mStartTouchPoint = new Point((int) event.getX(), (int) event.getY());
                update = moveTrackersIfNeeded(event);
                break;
            case 1:
                this.mStartTouchPoint = null;
                update = moveTrackersIfNeeded(event);
                break;
            case 2:
                update = moveTrackersIfNeeded(event);
                break;
        }
        if (!update) {
            return super.onTouchEvent(event);
        }
        if (this.mListener != null) {
            this.mListener.onColorChanged(Color.HSVToColor(this.mAlpha, new float[]{this.mHue, this.mSat, this.mVal}));
        }
        invalidate();
        return true;
    }

    private boolean moveTrackersIfNeeded(MotionEvent event) {
        if (this.mStartTouchPoint == null) {
            return false;
        }
        int startX = this.mStartTouchPoint.x;
        int startY = this.mStartTouchPoint.y;
        if (this.mHueRect.contains((float) startX, (float) startY)) {
            this.mLastTouchedPanel = 1;
            this.mHue = pointToHue(event.getY());
            return true;
        } else if (this.mSatValRect.contains((float) startX, (float) startY)) {
            this.mLastTouchedPanel = 0;
            float[] result = pointToSatVal(event.getX(), event.getY());
            this.mSat = result[0];
            this.mVal = result[1];
            return true;
        } else if (this.mAlphaRect == null || !this.mAlphaRect.contains((float) startX, (float) startY)) {
            return false;
        } else {
            this.mLastTouchedPanel = 2;
            this.mAlpha = pointToAlpha((int) event.getX());
            return true;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int finalWidth = 0;
        int finalHeight = 0;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthAllowed = MeasureSpec.getSize(widthMeasureSpec);
        int heightAllowed = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode != 1073741824 && heightMode != 1073741824) {
            int widthNeeded = (int) ((((float) heightAllowed) + this.PANEL_SPACING) + this.HUE_PANEL_WIDTH);
            int heightNeeded = (int) ((((float) widthAllowed) - this.PANEL_SPACING) - this.HUE_PANEL_WIDTH);
            if (this.mShowAlphaPanel) {
                widthNeeded = (int) (((float) widthNeeded) - (this.PANEL_SPACING + this.ALPHA_PANEL_HEIGHT));
                heightNeeded = (int) (((float) heightNeeded) + (this.PANEL_SPACING + this.ALPHA_PANEL_HEIGHT));
            }
            if (widthNeeded <= widthAllowed) {
                finalWidth = widthNeeded;
                finalHeight = heightAllowed;
            } else if (heightNeeded <= heightAllowed) {
                finalHeight = heightNeeded;
                finalWidth = widthAllowed;
            }
        } else if (widthMode == 1073741824 && heightMode != 1073741824) {
            int h = (int) ((((float) widthAllowed) - this.PANEL_SPACING) - this.HUE_PANEL_WIDTH);
            if (this.mShowAlphaPanel) {
                h = (int) (((float) h) + (this.PANEL_SPACING + this.ALPHA_PANEL_HEIGHT));
            }
            if (h > heightAllowed) {
                finalHeight = heightAllowed;
            } else {
                finalHeight = h;
            }
            finalWidth = widthAllowed;
        } else if (heightMode != 1073741824 || widthMode == 1073741824) {
            finalWidth = widthAllowed;
            finalHeight = heightAllowed;
        } else {
            int w = (int) ((((float) heightAllowed) + this.PANEL_SPACING) + this.HUE_PANEL_WIDTH);
            if (this.mShowAlphaPanel) {
                w = (int) (((float) w) - (this.PANEL_SPACING - this.ALPHA_PANEL_HEIGHT));
            }
            if (w > widthAllowed) {
                finalWidth = widthAllowed;
            } else {
                finalWidth = w;
            }
            finalHeight = heightAllowed;
        }
        setMeasuredDimension(finalWidth, finalHeight);
    }

    private String modeToString(int mode) {
        switch (mode) {
            case Integer.MIN_VALUE:
                return "AT MOST";
            case 0:
                return "UNSPECIFIED";
            case 1073741824:
                return "EXACTLY";
            default:
                return "ERROR";
        }
    }

    private int getPreferredWidth() {
        return (int) ((((float) ((int) (200.0f * mDensity))) + this.HUE_PANEL_WIDTH) + this.PANEL_SPACING);
    }

    private int getPreferredHeight() {
        int height = (int) (200.0f * mDensity);
        if (this.mShowAlphaPanel) {
            return (int) (((float) height) + (this.PANEL_SPACING + this.ALPHA_PANEL_HEIGHT));
        }
        return height;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mDrawingRect = new RectF();
        this.mDrawingRect.left = (float) (this.mDrawingOffset + getPaddingLeft());
        this.mDrawingRect.right = (float) ((w - this.mDrawingOffset) - getPaddingRight());
        this.mDrawingRect.top = (float) (this.mDrawingOffset + getPaddingTop());
        this.mDrawingRect.bottom = (float) ((h - this.mDrawingOffset) - getPaddingBottom());
        this.mValShader = null;
        this.mSatShader = null;
        this.mHueShader = null;
        this.mAlphaShader = null;
        setUpSatValRect();
        setUpHueRect();
        setUpAlphaRect();
    }

    private void setUpSatValRect() {
        RectF dRect = this.mDrawingRect;
        float left = dRect.left + BORDER_WIDTH_PX;
        float top = dRect.top + BORDER_WIDTH_PX;
        float bottom = dRect.bottom - BORDER_WIDTH_PX;
        float right = ((dRect.right - BORDER_WIDTH_PX) - this.PANEL_SPACING) - this.HUE_PANEL_WIDTH;
        if (this.mShowAlphaPanel) {
            bottom -= this.ALPHA_PANEL_HEIGHT + this.PANEL_SPACING;
        }
        this.mSatValRect = new RectF(left, top, right, bottom);
    }

    private void setUpHueRect() {
        RectF dRect = this.mDrawingRect;
        this.mHueRect = new RectF((dRect.right - this.HUE_PANEL_WIDTH) + BORDER_WIDTH_PX, dRect.top + BORDER_WIDTH_PX, dRect.right - BORDER_WIDTH_PX, (dRect.bottom - BORDER_WIDTH_PX) - (this.mShowAlphaPanel ? this.PANEL_SPACING + this.ALPHA_PANEL_HEIGHT : 0.0f));
    }

    private void setUpAlphaRect() {
        if (this.mShowAlphaPanel) {
            RectF dRect = this.mDrawingRect;
            this.mAlphaRect = new RectF(dRect.left + BORDER_WIDTH_PX, (dRect.bottom - this.ALPHA_PANEL_HEIGHT) + BORDER_WIDTH_PX, dRect.right - BORDER_WIDTH_PX, dRect.bottom - BORDER_WIDTH_PX);
            this.mAlphaPattern = new AlphaPatternDrawable((int) (5.0f * mDensity));
            this.mAlphaPattern.setBounds(Math.round(this.mAlphaRect.left), Math.round(this.mAlphaRect.top), Math.round(this.mAlphaRect.right), Math.round(this.mAlphaRect.bottom));
        }
    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        this.mListener = listener;
    }

    public int getColor() {
        return Color.HSVToColor(this.mAlpha, new float[]{this.mHue, this.mSat, this.mVal});
    }

    public void setColor(int color) {
        setColor(color, false);
    }

    public void setColor(int color, boolean callback) {
        int alpha = Color.alpha(color);
        float[] hsv = new float[3];
        Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsv);
        this.mAlpha = alpha;
        this.mHue = hsv[0];
        this.mSat = hsv[1];
        this.mVal = hsv[2];
        if (callback && this.mListener != null) {
            this.mListener.onColorChanged(Color.HSVToColor(this.mAlpha, new float[]{this.mHue, this.mSat, this.mVal}));
        }
        invalidate();
    }

    public float getDrawingOffset() {
        return (float) this.mDrawingOffset;
    }

    public void setAlphaSliderVisible(boolean visible) {
        if (this.mShowAlphaPanel != visible) {
            this.mShowAlphaPanel = visible;
            this.mValShader = null;
            this.mSatShader = null;
            this.mHueShader = null;
            this.mAlphaShader = null;
            requestLayout();
        }
    }

    public void setSliderTrackerColor(int color) {
        this.mSliderTrackerColor = color;
        this.mHueAlphaTrackerPaint.setColor(this.mSliderTrackerColor);
        invalidate();
    }

    public int getSliderTrackerColor() {
        return this.mSliderTrackerColor;
    }

    public void setBorderColor(int color) {
        this.mBorderColor = color;
        invalidate();
    }

    public int getBorderColor() {
        return this.mBorderColor;
    }

    public void setAlphaSliderText(int res) {
        setAlphaSliderText(getContext().getString(res));
    }

    public void setAlphaSliderText(String text) {
        this.mAlphaSliderText = text;
        invalidate();
    }

    public String getAlphaSliderText() {
        return this.mAlphaSliderText;
    }
}
