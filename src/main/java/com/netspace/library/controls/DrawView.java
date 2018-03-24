package com.netspace.library.controls;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import com.netspace.library.utilities.SwipeTouchListener;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DrawView extends View implements OnTouchListener {
    private static final String TAG = "DrawView";
    private static boolean m_bIsSPenEnabled = false;
    private Bitmap mBackgroundBitmap;
    private DrawViewDrawActionInterface mDrawActionInterface;
    private int mOldColor = -16777216;
    private int mOldWidth = 0;
    private boolean mOnlyActivePen = false;
    private boolean mOnlyActivePenDraw = false;
    private Paint mPaint = new Paint();
    private boolean mPausePaint = false;
    private List<Point> mPoints = new ArrayList();
    private SwipeTouchListener mSwipeListener = null;
    private DrawViewActionInterface m_ActionInterface;
    private Bitmap m_CanvasCacheBitmap;
    private Point m_LastTouchPoint;
    private boolean m_bAllEvent = false;
    private boolean m_bBrushMode = false;
    private boolean m_bDrawFocusFrame = false;
    private boolean m_bEnableCache = false;
    private boolean m_bEraseMode = false;
    private boolean m_bEraseMode2 = false;
    private boolean m_bFocused = false;
    private boolean m_bMultiTouch = false;
    private float m_fCursorDownX = -1.0f;
    private float m_fCursorDownY = -1.0f;
    private float m_fScale = 1.0f;
    private int m_nCachePointPos = -1;
    private int m_nCurrentColor = -16777216;
    private int m_nEraseMode2Color = 0;
    private int m_nEraseWidth = 40;
    private int m_nPointEndPos = -1;
    private ArrayList<DrawViewPlugin> marrPlugins = new ArrayList();
    private boolean mbAutoScale = false;
    private boolean mbPenAction = false;
    private int mnDrawHeight = -1;
    private int mnDrawWidth = -1;
    private int mnPenWidth = 10;

    public interface DrawViewActionInterface {
        void OnPenAction(String str, float f, float f2, int i, int i2);

        void OnPenButtonDown();

        void OnPenButtonUp();

        void OnTouchDown();

        void OnTouchFinger();

        void OnTouchMove();

        void OnTouchPen();

        void OnTouchUp();
    }

    public interface DrawViewDrawActionInterface {
        void OnBeforeDraw(DrawView drawView);
    }

    public interface DrawViewPlugin {
        boolean initialize(Context context, DrawView drawView);

        void onDraw(Canvas canvas, float f);
    }

    public DrawView(Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setOnTouchListener(this);
        this.mPaint.setAntiAlias(true);
        initSPenLibrary();
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setOnTouchListener(this);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStrokeJoin(Join.ROUND);
        initSPenLibrary();
    }

    public boolean addPlugin(DrawViewPlugin plugin) {
        if (!plugin.initialize(getContext(), this)) {
            return false;
        }
        this.marrPlugins.add(plugin);
        return true;
    }

    public void setBackgroundBitmap(Bitmap bitmap) {
        this.mBackgroundBitmap = bitmap;
    }

    public Bitmap getBackgroundBitmap() {
        return this.mBackgroundBitmap;
    }

    public void setSwipeListener(SwipeTouchListener SwipeTouchListener) {
        this.mSwipeListener = SwipeTouchListener;
    }

    private void processSPenAction(String szAction, float fX, float fY) {
        fX *= this.m_fScale;
        fY *= this.m_fScale;
        int ih = getMeasuredHeight();
        int iw = getMeasuredWidth();
        int iH = this.mnDrawHeight;
        int iW = this.mnDrawWidth;
        if (getBackground() != null) {
            iH = getBackground().getIntrinsicHeight();
            iW = getBackground().getIntrinsicWidth();
        }
        if (ih != 0 && iw != 0 && iH != 0 && iW != 0) {
            if (ih / iH <= iw / iW) {
                iw = (iW * ih) / iH;
            } else {
                ih = (iH * iw) / iW;
            }
            float nXOffset = ((float) ((getWidth() - iw) / 2)) * this.m_fScale;
            float nYOffset = ((float) ((getHeight() - ih) / 2)) * this.m_fScale;
            if (fX > nXOffset && fY > nYOffset && this.m_ActionInterface != null) {
                if (szAction.equalsIgnoreCase("CursorDown")) {
                    this.m_fCursorDownX = fX;
                    this.m_fCursorDownY = fY;
                } else if (szAction.equalsIgnoreCase("CursorUp")) {
                    this.m_fCursorDownX = -1.0f;
                    this.m_fCursorDownY = -1.0f;
                } else if (szAction.equalsIgnoreCase("CursorMove") && this.m_fCursorDownX != -1.0f && this.m_fCursorDownY != -1.0f && Math.abs(this.m_fCursorDownX - fX) <= 10.0f && Math.abs(this.m_fCursorDownY - fY) <= 10.0f) {
                    return;
                }
                this.m_ActionInterface.OnPenAction(szAction, fX - nXOffset, fY - nYOffset, (int) (((float) iw) * this.m_fScale), (int) (((float) ih) * this.m_fScale));
            }
        }
    }

    private void initSPenLibrary() {
        m_bIsSPenEnabled = true;
        if (m_bIsSPenEnabled) {
            setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    boolean bTouchFinger = false;
                    boolean bTouchPen = false;
                    if (VERSION.SDK_INT < 14) {
                        if (event.getMetaState() == 0) {
                            bTouchFinger = true;
                        } else if (event.getMetaState() == 512) {
                            bTouchPen = true;
                        }
                    } else if (event.getToolType(0) == 1) {
                        bTouchFinger = true;
                    } else if (event.getToolType(0) == 2) {
                        bTouchPen = true;
                    } else if (event.getToolType(0) == 3) {
                        bTouchFinger = true;
                    }
                    if (bTouchFinger) {
                        DrawView.this.mbPenAction = false;
                        if (DrawView.this.m_ActionInterface != null) {
                            DrawView.this.m_ActionInterface.OnTouchFinger();
                        }
                        if (DrawView.m_bIsSPenEnabled && DrawView.this.mOnlyActivePen) {
                            return false;
                        }
                        if (DrawView.this.mSwipeListener == null || !DrawView.this.mSwipeListener.onTouch(v, event)) {
                            return DrawView.this.onTouch(v, event);
                        }
                        return true;
                    } else if (!bTouchPen) {
                        return false;
                    } else {
                        DrawView.this.mbPenAction = true;
                        if (DrawView.this.m_ActionInterface != null) {
                            DrawView.this.m_ActionInterface.OnTouchPen();
                        }
                        DrawView drawView = DrawView.this;
                        drawView.mnPenWidth = drawView.mnPenWidth * 2;
                        DrawView.this.onTouch(v, event);
                        drawView = DrawView.this;
                        drawView.mnPenWidth = drawView.mnPenWidth / 2;
                        if (event.getAction() == 0) {
                            DrawView.this.processSPenAction("CursorDown", event.getX(), event.getY());
                        } else if (event.getAction() == 1) {
                            DrawView.this.processSPenAction("CursorUp", event.getX(), event.getY());
                        } else if (event.getAction() == 2) {
                            DrawView.this.processSPenAction("CursorMove", event.getX(), event.getY());
                        }
                        if (DrawView.this.m_bAllEvent) {
                            return true;
                        }
                        if (DrawView.this.m_bEraseMode || DrawView.this.m_bBrushMode) {
                            return true;
                        }
                        return false;
                    }
                }
            });
        }
        setOnHoverListener(new OnHoverListener() {
            private boolean mbButtonStateChanged = false;
            private boolean mbLastSecondButtonDown = false;

            public boolean onHover(View v, MotionEvent event) {
                boolean bButtonDown;
                if (event.getButtonState() == 2) {
                    if (!this.mbLastSecondButtonDown) {
                        Log.d(DrawView.TAG, "onHoverButtonDown");
                        if (DrawView.this.m_ActionInterface != null) {
                            DrawView.this.m_ActionInterface.OnPenButtonDown();
                        }
                    }
                    bButtonDown = true;
                } else {
                    if (this.mbLastSecondButtonDown) {
                        Log.d(DrawView.TAG, "onHoverButtonUp");
                        if (DrawView.this.m_ActionInterface != null) {
                            DrawView.this.m_ActionInterface.OnPenButtonUp();
                        }
                    }
                    bButtonDown = false;
                }
                if (bButtonDown != this.mbLastSecondButtonDown) {
                    this.mbLastSecondButtonDown = bButtonDown;
                } else {
                    DrawView.this.processSPenAction("CursorMove", event.getX(), event.getY());
                }
                return false;
            }
        });
    }

    public void clear() {
        cleanCache();
        this.mPoints.clear();
        this.mPoints = new ArrayList();
    }

    public static boolean getIsSPenSupported() {
        return m_bIsSPenEnabled;
    }

    public boolean getIsPenAction() {
        return this.mbPenAction;
    }

    public void cleanCache() {
        if (this.m_CanvasCacheBitmap != null) {
            this.m_CanvasCacheBitmap.recycle();
            this.m_CanvasCacheBitmap = null;
            this.m_nCachePointPos = -1;
        }
    }

    public void setCallback(DrawViewActionInterface CallBack) {
        this.m_ActionInterface = CallBack;
    }

    public void setDrawCallback(DrawViewDrawActionInterface DrawCallBack) {
        this.mDrawActionInterface = DrawCallBack;
    }

    public void setAntialias(boolean bEnable) {
        this.mPaint.setAntiAlias(bEnable);
    }

    public void setOnlyActivePen(boolean bEnable) {
        this.mOnlyActivePen = bEnable;
    }

    public void setOnlyActivePenDraw(boolean bEnable) {
        this.mOnlyActivePenDraw = bEnable;
    }

    public void setAllEvent(boolean bAllEvent) {
        this.m_bAllEvent = bAllEvent;
    }

    public void clearPoints() {
        if (this.mPoints != null) {
            this.mPoints.clear();
            cleanCache();
            forceRedraw();
        }
    }

    public void forceRedraw() {
        invalidate();
    }

    public void setColor(int nNewColor) {
        if (this.m_bEraseMode2) {
            this.mOldColor = nNewColor;
        } else {
            this.m_nCurrentColor = nNewColor;
        }
    }

    public int getColor() {
        return this.m_nCurrentColor;
    }

    public void changeWidth(int wid_in) {
        this.mnPenWidth = wid_in;
    }

    public int getPenWidth() {
        return this.mnPenWidth;
    }

    public void setEnableCache(boolean bEnable) {
        this.m_bEnableCache = bEnable;
    }

    public boolean getEnableCache() {
        return this.m_bEnableCache;
    }

    public void setEraseMode(boolean bOn) {
        this.m_bEraseMode = bOn;
    }

    public Paint getPaint() {
        return this.mPaint;
    }

    public void setEraseMode2(boolean bOn, int nEraseColor) {
        this.m_bEraseMode2 = bOn;
        if (bOn) {
            this.mPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
            this.mOldWidth = this.mnPenWidth;
            this.mOldColor = this.m_nCurrentColor;
            this.m_nEraseMode2Color = nEraseColor;
            this.m_nCurrentColor = nEraseColor;
            this.mnPenWidth = this.m_nEraseWidth;
            this.m_bBrushMode = true;
            this.m_bEraseMode = false;
            return;
        }
        this.mPaint.setXfermode(null);
        if (this.mnPenWidth == this.m_nEraseWidth) {
            this.mnPenWidth = this.mOldWidth;
        }
        this.m_nCurrentColor = this.mOldColor;
        this.m_bBrushMode = false;
        this.m_bEraseMode = false;
    }

    public void setBrushMode(boolean bOn) {
        if (this.m_bEraseMode2) {
            setEraseMode2(false, 0);
        }
        this.m_bBrushMode = bOn;
    }

    public boolean getBrushMode() {
        return this.m_bBrushMode;
    }

    public boolean getEraseMode() {
        return this.m_bEraseMode;
    }

    public void setPausePaint(boolean bPause) {
        if (this.mPausePaint && !bPause) {
            invalidate();
        }
        this.mPausePaint = bPause;
    }

    public void setDrawFocusRect(boolean bOn) {
        this.m_bDrawFocusFrame = bOn;
    }

    public void Undo() {
        this.m_nPointEndPos--;
        if (this.m_nPointEndPos < 0) {
            this.m_nPointEndPos = 0;
        }
        if (this.m_bEraseMode2) {
            setEraseMode2(false, this.m_nEraseMode2Color);
            forceRedraw();
            setEraseMode2(true, this.m_nEraseMode2Color);
            return;
        }
        forceRedraw();
    }

    public void Redo() {
        this.m_nPointEndPos++;
        if (this.m_nPointEndPos > this.mPoints.size() - 1) {
            this.m_nPointEndPos = this.mPoints.size() - 1;
        }
        if (this.m_bEraseMode2) {
            setEraseMode2(false, this.m_nEraseMode2Color);
            forceRedraw();
            setEraseMode2(true, this.m_nEraseMode2Color);
            return;
        }
        forceRedraw();
    }

    public void onDraw(Canvas canvas) {
        boolean bCacheReady = false;
        if (this.m_bDrawFocusFrame) {
            Drawable bg = getContext().getResources().getDrawable(Utilities.getThemeCustomResID(R.attr.edittext_widget_background, getContext()));
            if (this.m_bFocused) {
                int[] iArr = new int[2];
                bg.setState(new int[]{16842908, 16842910});
            } else {
                bg.setState(new int[]{16842910});
            }
            if (bg != null) {
                bg.setBounds(0, 0, getWidth(), getHeight());
                bg.draw(canvas);
            }
        }
        if (!(this.mBackgroundBitmap == null || this.mBackgroundBitmap.isRecycled())) {
            RectF dst = new RectF();
            dst.top = 0.0f;
            dst.left = 0.0f;
            dst.right = (float) getWidth();
            dst.bottom = (float) getHeight();
            canvas.drawBitmap(this.mBackgroundBitmap, null, dst, null);
        }
        if (this.mPausePaint) {
            Log.d(TAG, "mPaint paused. ");
            return;
        }
        Iterator it = this.marrPlugins.iterator();
        while (it.hasNext()) {
            ((DrawViewPlugin) it.next()).onDraw(canvas, this.m_fScale);
        }
        if (this.mPoints.size() != 0) {
            int i;
            Point point;
            long nStartTime = System.currentTimeMillis();
            Xfermode clearMode = new PorterDuffXfermode(Mode.DST_IN);
            this.mPaint.isAntiAlias();
            if (this.m_bEnableCache) {
                Canvas CacheCanvas;
                int nWidth = getWidth();
                int nHeight = getHeight();
                if (nWidth < this.mnDrawWidth) {
                    nWidth = this.mnDrawWidth;
                }
                if (nHeight < this.mnDrawHeight) {
                    nHeight = this.mnDrawHeight;
                }
                if (nHeight > 4096) {
                    Log.e(TAG, "Height " + nHeight + " exceed max height of 4096. Using 4096 as height for DrawView Buffer.");
                    nHeight = 4096;
                }
                Runtime info = Runtime.getRuntime();
                long freeSize = info.freeMemory();
                long totalSize = info.maxMemory();
                long nFreeSize = (long) (((double) ((float) (totalSize - (totalSize - freeSize)))) * 0.8d);
                if (this.m_CanvasCacheBitmap == null && nWidth > 0 && nHeight > 0) {
                    Log.i(TAG, "Ready to create a " + nWidth + "x" + nHeight + " size buffer. OOM may happen. maxMemory=" + totalSize + ", freeMemory=" + freeSize);
                    this.m_CanvasCacheBitmap = Bitmap.createBitmap(nWidth, nHeight, Config.ARGB_8888);
                    CacheCanvas = new Canvas(this.m_CanvasCacheBitmap);
                    Paint TransparentmPaint = new Paint();
                    Rect rect = new Rect();
                    this.mPaint.setXfermode(null);
                    this.mPaint.isAntiAlias();
                    if (this.mDrawActionInterface != null) {
                        this.mDrawActionInterface.OnBeforeDraw(this);
                    }
                    rect.left = 0;
                    rect.top = 0;
                    rect.right = nWidth;
                    rect.bottom = nHeight;
                    TransparentmPaint.setStyle(Style.FILL);
                    TransparentmPaint.setARGB(0, 0, 0, 0);
                    CacheCanvas.drawRect(rect, TransparentmPaint);
                } else if (this.m_CanvasCacheBitmap == null && nWidth > 0 && nHeight > 0) {
                    Log.e(TAG, "Cache create failed due to mPaint area too large. Safe size is " + nFreeSize + " bytes. Alloced will be " + String.valueOf((nWidth * nHeight) * 4) + " bytes. Preformance will be very slow.");
                }
                if (this.m_CanvasCacheBitmap != null) {
                    CacheCanvas = new Canvas(this.m_CanvasCacheBitmap);
                    Path path2 = new Path();
                    int nStartIndex = this.m_nCachePointPos;
                    if (nStartIndex < 0) {
                        nStartIndex = 0;
                    }
                    for (i = nStartIndex; i < this.mPoints.size(); i++) {
                        if (i <= this.m_nPointEndPos) {
                            point = (Point) this.mPoints.get(i);
                            if (point.col == this.m_nEraseMode2Color) {
                                this.mPaint.setXfermode(clearMode);
                            } else {
                                this.mPaint.setXfermode(null);
                            }
                            point.draw(CacheCanvas, this.mPaint, this.m_fScale);
                        }
                    }
                    this.m_nCachePointPos = this.mPoints.size() - 1;
                    canvas.drawBitmap(this.m_CanvasCacheBitmap, 0.0f, 0.0f, null);
                    bCacheReady = true;
                }
            }
            if (!bCacheReady) {
                for (i = 0; i < this.mPoints.size(); i++) {
                    if (i <= this.m_nPointEndPos) {
                        point = (Point) this.mPoints.get(i);
                        if (point.col == this.m_nEraseMode2Color) {
                            this.mPaint.setXfermode(clearMode);
                        } else {
                            this.mPaint.setXfermode(null);
                        }
                        point.draw(canvas, this.mPaint, this.m_fScale);
                    }
                }
            }
            Log.i(TAG, "DrawTime " + String.valueOf(System.currentTimeMillis() - nStartTime) + "ms");
        }
    }

    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        this.m_bFocused = gainFocus;
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    protected void onDetachedFromWindow() {
        cleanCache();
        super.onDetachedFromWindow();
    }

    public Bitmap saveToBitmap() {
        Bitmap SourceBitmap;
        Bitmap ResultBitmap;
        Canvas canvas;
        BitmapDrawable background = (BitmapDrawable) getBackground();
        if (this.mBackgroundBitmap != null) {
            SourceBitmap = this.mBackgroundBitmap;
            ResultBitmap = Utilities.cloneBitmap(this.mBackgroundBitmap, 0, 0, this.mnDrawWidth, this.mnDrawHeight);
            canvas = new Canvas(ResultBitmap);
        } else if (background == null) {
            ResultBitmap = Bitmap.createBitmap(this.mnDrawWidth, this.mnDrawHeight, Config.ARGB_8888);
            SourceBitmap = ResultBitmap;
            canvas = new Canvas(ResultBitmap);
            Paint mPaint = new Paint();
            mPaint.setARGB(255, 255, 255, 255);
            mPaint.setStyle(Style.FILL);
            canvas.drawRect(0.0f, 0.0f, (float) this.mnDrawWidth, (float) this.mnDrawHeight, mPaint);
        } else {
            SourceBitmap = background.getBitmap();
            ResultBitmap = Utilities.cloneBitmap(SourceBitmap, 0, 0, this.mnDrawWidth, this.mnDrawHeight);
            canvas = new Canvas(ResultBitmap);
        }
        Paint Paint = new Paint();
        RectF bounds = new RectF();
        bounds.left = 0.0f;
        bounds.top = 0.0f;
        bounds.right = (float) ResultBitmap.getWidth();
        bounds.bottom = (float) ResultBitmap.getHeight();
        canvas.saveLayer(bounds, Paint, 31);
        Iterator it = this.marrPlugins.iterator();
        while (it.hasNext()) {
            ((DrawViewPlugin) it.next()).onDraw(canvas, this.m_fScale);
        }
        int nTargetWidth = ResultBitmap.getWidth();
        int nTargetHeight = ResultBitmap.getHeight();
        if (!(nTargetWidth == SourceBitmap.getWidth() && nTargetHeight == SourceBitmap.getHeight())) {
            Log.d(TAG, "Width or height different. Rescale needed.");
        }
        for (int i = 0; i < this.mPoints.size(); i++) {
            if (i <= this.m_nPointEndPos) {
                Point point = (Point) this.mPoints.get(i);
                if (point.col == this.m_nEraseMode2Color) {
                    this.mPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
                } else {
                    this.mPaint.setXfermode(null);
                }
                point.draw(canvas, this.mPaint, 1.0f);
            }
        }
        canvas.restore();
        return ResultBitmap;
    }

    private float getPointSpacing(MotionEvent event) {
        if (event.getPointerCount() < 2) {
            return 0.0f;
        }
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt((double) ((x * x) + (y * y)));
    }

    public boolean onTouch(View view, MotionEvent event) {
        requestFocusFromTouch();
        if (event.getPointerCount() > 1) {
            this.m_bMultiTouch = true;
            this.m_LastTouchPoint = null;
            return false;
        }
        int i;
        Point OnePoint;
        FriendlyPoint Point;
        Rect rect;
        if (event.getAction() == 2) {
            if (!this.m_bMultiTouch) {
                if (!this.mbPenAction && m_bIsSPenEnabled && this.mOnlyActivePenDraw && (this.m_bEraseMode || this.m_bBrushMode)) {
                    return false;
                }
                if (this.m_ActionInterface != null) {
                    this.m_ActionInterface.OnTouchMove();
                }
                if (this.m_bEraseMode) {
                    if (this.mPoints.size() > this.m_nPointEndPos && this.m_nPointEndPos != -1) {
                        i = 0;
                        while (i < this.mPoints.size()) {
                            if (i > this.m_nPointEndPos) {
                                this.mPoints.remove(i);
                                i--;
                            }
                            i++;
                        }
                    }
                    for (int j = 0; j < event.getHistorySize(); j++) {
                        boolean bLineFound = false;
                        i = 0;
                        while (i < this.mPoints.size()) {
                            OnePoint = (Point) this.mPoints.get(i);
                            if (OnePoint instanceof FriendlyPoint) {
                                Point = (FriendlyPoint) OnePoint;
                                rect = new Rect();
                                rect.left = (int) (Math.min(Point.x, Point.neighbour.x) * this.m_fScale);
                                rect.right = (int) (Math.max(Point.x, Point.neighbour.x) * this.m_fScale);
                                rect.top = (int) (Math.min(Point.y, Point.neighbour.y) * this.m_fScale);
                                rect.bottom = (int) (Math.max(Point.y, Point.neighbour.y) * this.m_fScale);
                                rect.left = (int) (((float) rect.left) - ((((float) Point.width) * this.m_fScale) / 2.0f));
                                rect.right = (int) (((float) rect.right) + ((((float) Point.width) * this.m_fScale) / 2.0f));
                                rect.top = (int) (((float) rect.top) - ((((float) Point.width) * this.m_fScale) / 2.0f));
                                rect.bottom = (int) (((float) rect.bottom) + ((((float) Point.width) * this.m_fScale) / 2.0f));
                                if (rect.contains((int) event.getHistoricalX(j), (int) event.getHistoricalY(j))) {
                                    while (i < this.mPoints.size() && (this.mPoints.get(i) instanceof FriendlyPoint)) {
                                        this.mPoints.remove(i);
                                    }
                                    while (i - 1 >= 0 && (this.mPoints.get(i - 1) instanceof FriendlyPoint)) {
                                        this.mPoints.remove(i - 1);
                                        i--;
                                    }
                                    if (i - 1 >= 0 && (this.mPoints.get(i - 1) instanceof Point)) {
                                        this.mPoints.remove(i - 1);
                                    }
                                    cleanCache();
                                    forceRedraw();
                                    bLineFound = true;
                                    if (!bLineFound) {
                                        break;
                                    }
                                }
                            }
                            i++;
                        }
                        if (!bLineFound) {
                            break;
                        }
                    }
                }
                if (this.m_bEraseMode || !this.m_bBrushMode) {
                    return false;
                }
                if (this.m_LastTouchPoint != null) {
                    this.mPoints.add(this.m_LastTouchPoint);
                    this.m_LastTouchPoint = null;
                }
                if (this.mPoints.size() > 0) {
                    Point LastPoint = (Point) this.mPoints.get(this.mPoints.size() - 1);
                    for (i = 0; i < event.getHistorySize(); i++) {
                        float fAverangedPressure2 = event.getHistoricalPressure(i);
                        if (!m_bIsSPenEnabled && (fAverangedPressure2 == 0.0f || fAverangedPressure2 == 0.005f || fAverangedPressure2 > 1.0f)) {
                            fAverangedPressure2 = 1.0f;
                        }
                        this.mPoints.add(new FriendlyPoint(event.getHistoricalX(i) / this.m_fScale, event.getHistoricalY(i) / this.m_fScale, this.m_nCurrentColor, (Point) this.mPoints.get(this.mPoints.size() - 1), (int) (((float) this.mnPenWidth) * fAverangedPressure2)));
                    }
                    float fAverangedPressure = event.getPressure();
                    if (!m_bIsSPenEnabled && (fAverangedPressure == 0.0f || fAverangedPressure == 0.005f || fAverangedPressure > 1.0f)) {
                        fAverangedPressure = 1.0f;
                    }
                    this.mPoints.add(new FriendlyPoint(event.getX() / this.m_fScale, event.getY() / this.m_fScale, this.m_nCurrentColor, (Point) this.mPoints.get(this.mPoints.size() - 1), (int) (((float) this.mnPenWidth) * fAverangedPressure)));
                    this.m_nPointEndPos = this.mPoints.size() - 1;
                }
                forceRedraw();
            }
        } else if (event.getAction() == 0) {
            if (!this.mbPenAction && m_bIsSPenEnabled && this.mOnlyActivePenDraw && (this.m_bEraseMode || this.m_bBrushMode)) {
                return false;
            }
            if (this.m_ActionInterface != null) {
                this.m_ActionInterface.OnTouchDown();
            }
            this.m_bMultiTouch = false;
            float fX = event.getX() / this.m_fScale;
            float fY = event.getY() / this.m_fScale;
            if (this.m_bEraseMode) {
                if (this.mPoints.size() > this.m_nPointEndPos && this.m_nPointEndPos != -1) {
                    i = this.m_nPointEndPos;
                    while (i < this.mPoints.size()) {
                        if (i > this.m_nPointEndPos) {
                            this.mPoints.remove(i);
                            i--;
                        }
                        i++;
                    }
                }
                i = 0;
                while (i < this.mPoints.size()) {
                    OnePoint = (Point) this.mPoints.get(i);
                    if (OnePoint instanceof FriendlyPoint) {
                        Point = (FriendlyPoint) OnePoint;
                        rect = new Rect();
                        rect.left = (int) (Math.min(Point.x, Point.neighbour.x) * this.m_fScale);
                        rect.right = (int) (Math.max(Point.x, Point.neighbour.x) * this.m_fScale);
                        rect.top = (int) (Math.min(Point.y, Point.neighbour.y) * this.m_fScale);
                        rect.bottom = (int) (Math.max(Point.y, Point.neighbour.y) * this.m_fScale);
                        rect.left = (int) (((float) rect.left) - ((((float) Point.width) * this.m_fScale) / 2.0f));
                        rect.right = (int) (((float) rect.right) + ((((float) Point.width) * this.m_fScale) / 2.0f));
                        rect.top = (int) (((float) rect.top) - ((((float) Point.width) * this.m_fScale) / 2.0f));
                        rect.bottom = (int) (((float) rect.bottom) + ((((float) Point.width) * this.m_fScale) / 2.0f));
                        if (rect.contains((int) event.getX(), (int) event.getY())) {
                            while (i < this.mPoints.size() && (this.mPoints.get(i) instanceof FriendlyPoint)) {
                                this.mPoints.remove(i);
                            }
                            while (i - 1 >= 0 && (this.mPoints.get(i - 1) instanceof FriendlyPoint)) {
                                this.mPoints.remove(i - 1);
                                i--;
                            }
                            if (i - 1 >= 0 && (this.mPoints.get(i - 1) instanceof Point)) {
                                this.mPoints.remove(i - 1);
                            }
                            cleanCache();
                            forceRedraw();
                        }
                    }
                    i++;
                }
            } else if (!this.m_bBrushMode) {
                return false;
            } else {
                float fPressure = event.getPressure();
                if (fPressure == 0.0f || fPressure == 0.005f || fPressure > 1.0f) {
                    fPressure = 1.0f;
                }
                float fWidth = ((float) this.mnPenWidth) * fPressure;
                this.m_LastTouchPoint = new Point(fX, fY, this.m_nCurrentColor, (int) fWidth);
                if (this.mPoints.size() > this.m_nPointEndPos && this.m_nPointEndPos != -1) {
                    i = this.m_nPointEndPos;
                    while (i < this.mPoints.size()) {
                        if (i > this.m_nPointEndPos) {
                            this.mPoints.remove(i);
                            i--;
                        }
                        i++;
                    }
                }
            }
        } else if (event.getAction() != 1) {
            return false;
        } else {
            if (this.m_ActionInterface != null) {
                this.m_ActionInterface.OnTouchUp();
            }
            return false;
        }
        return true;
    }

    public void setSize(int nWidth, int nHeight) {
        this.mnDrawWidth = nWidth;
        this.mnDrawHeight = nHeight;
    }

    public void addPoint(Point point) {
        this.mPoints.add(point);
        this.m_nPointEndPos = this.mPoints.size() - 1;
    }

    public void setScale(float fScale) {
        if (fScale != this.m_fScale) {
            this.m_fScale = fScale;
            cleanCache();
        }
    }

    public void setAutoScale(boolean bEnable) {
        this.mbAutoScale = bEnable;
    }

    public int getDataPointsCount() {
        return this.mPoints.size();
    }

    public String getDataAsString() {
        return getDataAsString(0);
    }

    public String getDataAsString(int nStartIndex) {
        int i;
        Point OnePoint;
        StringBuilder StringBuilder = new StringBuilder();
        boolean bSetScreenInfo = false;
        if (nStartIndex > this.mPoints.size() - 1) {
            nStartIndex = 0;
        }
        if (this.mPoints.size() > 0) {
            for (i = nStartIndex; i >= 0; i--) {
                OnePoint = (Point) this.mPoints.get(i);
                if (!(OnePoint instanceof FriendlyPoint) && !(OnePoint instanceof CirclePoint) && !(OnePoint instanceof OvalPoint)) {
                    nStartIndex = i;
                    break;
                }
            }
        }
        for (i = nStartIndex; i < this.mPoints.size(); i++) {
            OnePoint = (Point) this.mPoints.get(i);
            boolean bCirclePoint = false;
            boolean bOvalPoint = false;
            if (OnePoint instanceof FriendlyPoint) {
                StringBuilder.append("F:");
            } else if (OnePoint instanceof CirclePoint) {
                StringBuilder.append("C:");
                bCirclePoint = true;
            } else if (OnePoint instanceof OvalPoint) {
                StringBuilder.append("O:");
                bOvalPoint = true;
            } else if (OnePoint instanceof Point) {
                StringBuilder.append("P:");
            }
            StringBuilder.append("x=");
            StringBuilder.append(String.valueOf(OnePoint.x));
            StringBuilder.append(",y=");
            StringBuilder.append(String.valueOf(OnePoint.y));
            StringBuilder.append(",width=");
            StringBuilder.append(String.valueOf(OnePoint.width));
            StringBuilder.append(",color=");
            StringBuilder.append(String.valueOf(OnePoint.col));
            if (bCirclePoint) {
                CirclePoint CirclePoint = (CirclePoint) OnePoint;
                StringBuilder.append(",linewidth=");
                StringBuilder.append(String.valueOf(CirclePoint.getStockWidth()));
            }
            if (bOvalPoint) {
                OvalPoint OvalPoint = (OvalPoint) OnePoint;
                StringBuilder.append(",x2=");
                StringBuilder.append(String.valueOf(OvalPoint.mx2));
                StringBuilder.append(",y2=");
                StringBuilder.append(String.valueOf(OvalPoint.my2));
            }
            if (!bSetScreenInfo) {
                Display display = ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay();
                DisplayMetrics metrics = new DisplayMetrics();
                display.getMetrics(metrics);
                StringBuilder.append(",screenwidth=");
                StringBuilder.append(String.valueOf(metrics.widthPixels));
                StringBuilder.append(",screenheight=");
                StringBuilder.append(String.valueOf(metrics.heightPixels));
                StringBuilder.append(",objectwidth=");
                StringBuilder.append(String.valueOf(this.mnDrawWidth));
                StringBuilder.append(",objectheight=");
                StringBuilder.append(String.valueOf(this.mnDrawHeight));
                StringBuilder.append(",xdpi=");
                StringBuilder.append(String.valueOf(metrics.xdpi));
                StringBuilder.append(",ydpi=");
                StringBuilder.append(String.valueOf(metrics.ydpi));
                StringBuilder.append(",density=");
                StringBuilder.append(String.valueOf(metrics.density));
                bSetScreenInfo = true;
            }
            StringBuilder.append(";");
        }
        return StringBuilder.toString();
    }

    public String getInfoAsString() {
        StringBuilder StringBuilder = new StringBuilder();
        StringBuilder.append("I:");
        StringBuilder.append("color=");
        StringBuilder.append(String.valueOf(getColor()));
        StringBuilder.append(",width=");
        StringBuilder.append(String.valueOf(getPenWidth()));
        Display display = ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        StringBuilder.append(",screenwidth=");
        StringBuilder.append(String.valueOf(metrics.widthPixels));
        StringBuilder.append(",screenheight=");
        StringBuilder.append(String.valueOf(metrics.heightPixels));
        StringBuilder.append(",objectwidth=");
        StringBuilder.append(String.valueOf(this.mnDrawWidth));
        StringBuilder.append(",objectheight=");
        StringBuilder.append(String.valueOf(this.mnDrawHeight));
        StringBuilder.append(",xdpi=");
        StringBuilder.append(String.valueOf(metrics.xdpi));
        StringBuilder.append(",ydpi=");
        StringBuilder.append(String.valueOf(metrics.ydpi));
        StringBuilder.append(",density=");
        StringBuilder.append(String.valueOf(metrics.density));
        StringBuilder.append(";");
        return StringBuilder.toString();
    }

    public boolean fromString(String szValue) {
        if (szValue == null || szValue.isEmpty()) {
            return false;
        }
        String szOneData;
        String[] arrParams;
        long nStartTime = System.currentTimeMillis();
        String[] arrData = szValue.split(";");
        boolean bResult = false;
        Point point = null;
        float x = 0.0f;
        float y = 0.0f;
        float x2 = 0.0f;
        float y2 = 0.0f;
        int width = 0;
        int col = 0;
        int nOldPointsCount = this.mPoints.size();
        float fXScale = 1.0f;
        float fYScale = 1.0f;
        float fStockWidth = 1.0f;
        Display display = ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        float nCurrentScreenWidth = (float) metrics.widthPixels;
        float nScreenWidth = nCurrentScreenWidth;
        float nCurrentScreenHeight = (float) metrics.heightPixels;
        float nScreenHeight = nCurrentScreenHeight;
        float nXdpi = metrics.xdpi;
        float nYdpi = metrics.ydpi;
        float nDensity = metrics.density;
        if (this.mbAutoScale) {
            for (String szOneData2 : arrData) {
                if (szOneData2.startsWith("F:")) {
                    szOneData2 = szOneData2.substring(2);
                } else {
                    if (szOneData2.startsWith("P:")) {
                        szOneData2 = szOneData2.substring(2);
                    } else {
                    }
                }
                arrParams = szOneData2.split(",");
                for (String szOneParam : arrParams) {
                    if (szOneParam.startsWith("screenwidth=")) {
                        nScreenWidth = Float.valueOf(szOneParam.substring(12)).floatValue();
                    } else {
                        if (szOneParam.startsWith("screenheight=")) {
                            nScreenHeight = Float.valueOf(szOneParam.substring(13)).floatValue();
                        }
                    }
                    if (nScreenWidth != 0.0f && nScreenHeight != 0.0f) {
                        fXScale = nCurrentScreenWidth / nScreenWidth;
                        fYScale = nCurrentScreenHeight / nScreenHeight;
                        break;
                    }
                }
            }
        }
        int i = 0;
        FriendlyPoint friendlyPoint = null;
        while (i < arrData.length) {
            FriendlyPoint friendlyPoint2;
            szOneData2 = arrData[i];
            boolean bCirclePoint = false;
            boolean bFriendlyPoint = false;
            boolean bOvalPoint = false;
            if (szOneData2.startsWith("F:")) {
                szOneData2 = szOneData2.substring(2);
                bFriendlyPoint = true;
            } else {
                if (szOneData2.startsWith("C:")) {
                    szOneData2 = szOneData2.substring(2);
                    bCirclePoint = true;
                } else {
                    if (szOneData2.startsWith("O:")) {
                        szOneData2 = szOneData2.substring(2);
                        bOvalPoint = true;
                    } else {
                        if (szOneData2.startsWith("P:")) {
                            szOneData2 = szOneData2.substring(2);
                            bFriendlyPoint = false;
                        }
                    }
                }
            }
            arrParams = szOneData2.split(",");
            for (String szOneParam2 : arrParams) {
                if (szOneParam2.startsWith("x=")) {
                    x = Float.valueOf(szOneParam2.substring(2)).floatValue();
                } else {
                    if (szOneParam2.startsWith("y=")) {
                        y = Float.valueOf(szOneParam2.substring(2)).floatValue();
                    } else {
                        if (szOneParam2.startsWith("width=")) {
                            width = Integer.valueOf(szOneParam2.substring(6)).intValue();
                        } else {
                            if (szOneParam2.startsWith("color=")) {
                                col = Integer.valueOf(szOneParam2.substring(6)).intValue();
                            } else {
                                if (szOneParam2.startsWith("linewidth=")) {
                                    fStockWidth = Float.valueOf(szOneParam2.substring(10)).floatValue();
                                }
                            }
                        }
                    }
                }
                if (bOvalPoint) {
                    if (szOneParam2.startsWith("x2=")) {
                        x2 = Float.valueOf(szOneParam2.substring(3)).floatValue();
                    } else {
                        if (szOneParam2.startsWith("y2=")) {
                            y2 = Float.valueOf(szOneParam2.substring(3)).floatValue();
                        }
                    }
                }
            }
            if (bCirclePoint) {
                CirclePoint CirclePoint;
                if (this.mbAutoScale) {
                    CirclePoint = new CirclePoint(x, y, col, width, fStockWidth);
                } else {
                    CirclePoint = new CirclePoint(x * fXScale, y * fYScale, col, width, fStockWidth);
                }
                this.mPoints.add(CirclePoint);
                point = null;
                friendlyPoint2 = friendlyPoint;
            } else if (bOvalPoint) {
                OvalPoint OvalPoint;
                if (this.mbAutoScale) {
                    OvalPoint = new OvalPoint(x, y, x2, y2, col, (int) fStockWidth);
                } else {
                    OvalPoint = new OvalPoint(x * fXScale, y * fYScale, x2 * fXScale, y2 * fYScale, col, (int) fStockWidth);
                }
                this.mPoints.add(OvalPoint);
                point = null;
                friendlyPoint2 = friendlyPoint;
            } else if (bFriendlyPoint) {
                if (this.mbAutoScale) {
                    friendlyPoint2 = new FriendlyPoint(x, y, col, point, width);
                } else {
                    friendlyPoint2 = new FriendlyPoint(x * fXScale, y * fYScale, col, point, width);
                }
                this.mPoints.add(friendlyPoint);
                point = friendlyPoint;
            } else {
                if (this.mbAutoScale) {
                    point = new Point(x, y, col, width);
                } else {
                    point = new Point(x * fXScale, y * fYScale, col, width);
                }
                this.mPoints.add(point);
                friendlyPoint2 = friendlyPoint;
            }
            i++;
            friendlyPoint = friendlyPoint2;
        }
        if (nOldPointsCount != this.mPoints.size()) {
            bResult = true;
        }
        this.m_nPointEndPos = this.mPoints.size() - 1;
        Log.i(TAG, "LoadTime " + String.valueOf(System.currentTimeMillis() - nStartTime) + "ms. arrData.length=" + arrData.length + ", mPoints count=" + this.mPoints.size());
        arrData = null;
        forceRedraw();
        return bResult;
    }

    public float getScale() {
        return this.m_fScale;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mnDrawWidth == -1 || this.mnDrawHeight == -1) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            setMeasuredDimension((int) (((float) this.mnDrawWidth) * this.m_fScale), (int) (((float) this.mnDrawHeight) * this.m_fScale));
        }
    }

    public void saveCacheToBitmap(String szCacheFileName) {
        if (this.m_CanvasCacheBitmap != null) {
            Log.d(TAG, "Save cache to file " + szCacheFileName);
            Utilities.saveBitmapToPng(szCacheFileName, this.m_CanvasCacheBitmap);
            this.m_CanvasCacheBitmap.recycle();
            this.m_CanvasCacheBitmap = null;
        }
    }

    public void loadCacheFromDisk(String szCacheFileName) {
        if (new File(szCacheFileName).exists()) {
            Log.d(TAG, "Load cache from file " + szCacheFileName);
            Bitmap oldBitmap = Utilities.loadBitmapFromFile(szCacheFileName);
            this.m_CanvasCacheBitmap = oldBitmap.copy(Config.ARGB_8888, true);
            oldBitmap.recycle();
        }
    }
}
