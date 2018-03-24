package com.foxit.uiextensions.modules.signature;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.controls.dialog.UITextEditDialog;
import com.foxit.uiextensions.controls.dialog.fileselect.UISaveAsDialog;
import com.foxit.uiextensions.controls.dialog.fileselect.UISaveAsDialog.ISaveAsOnOKClickCallBack;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu.ClickListener;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.controls.propertybar.PropertyBar.DismissListener;
import com.foxit.uiextensions.controls.propertybar.imp.AnnotMenuImpl;
import com.foxit.uiextensions.controls.propertybar.imp.PropertyBarImpl;
import com.foxit.uiextensions.security.digitalsignature.DigitalSignatureModule;
import com.foxit.uiextensions.security.digitalsignature.DigitalSignatureUtil;
import com.foxit.uiextensions.security.digitalsignature.IDigitalSignatureCreateCallBack;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SignatureToolHandler implements ToolHandler {
    private static final int CTR_LEFT_BOTTOM = 6;
    private static final int CTR_LEFT_MID = 7;
    private static final int CTR_LEFT_TOP = 0;
    private static final int CTR_MID_BOTTOM = 5;
    private static final int CTR_MID_TOP = 1;
    private static final int CTR_NONE = -1;
    private static final int CTR_RIGHT_BOTTOM = 4;
    private static final int CTR_RIGHT_MID = 3;
    private static final int CTR_RIGHT_TOP = 2;
    private static final int OPER_DEFAULT = -1;
    private static final int OPER_SCALE = 1;
    private static final int OPER_TRANSLATE = 0;
    private AnnotMenu mAnnotMenu;
    private RectF mAreaTmpRect = new RectF();
    private RectF mBBoxTmp;
    private RectF mBbox;
    private Bitmap mBitmap;
    private Paint mBitmapPaint;
    private Rect mBitmapRect;
    private int mColor;
    private Context mContext;
    private float mCtlPtLineWidth = 4.0f;
    private Paint mCtlPtPaint;
    private float mCtlPtRadius = 5.0f;
    private float mCtlPtTouchExt = 20.0f;
    private int mCurrentCtr = -1;
    private boolean mDefaultAdd = false;
    private RectF mDesRect;
    private float mDiameter;
    private AppDisplay mDisplay;
    private PointF mDownPoint;
    private String mDsgPath;
    private SignatureFragment mFragment;
    private RectF mFrameRectF;
    private RectF mFrameTmpRect = new RectF();
    private float mFrmLineWidth = 1.0f;
    private Paint mFrmPaint;
    private SignatureInkCallback mInkCallback = new SignatureInkCallback() {
        public void onBackPressed() {
            boolean backToNormal = false;
            FragmentActivity act = (FragmentActivity) SignatureToolHandler.this.mContext;
            if (act == null) {
                backToNormal = true;
            } else if (((SignatureFragment) act.getSupportFragmentManager().findFragmentByTag("InkSignFragment")) == null) {
                backToNormal = true;
            }
            try {
                ((FragmentActivity) SignatureToolHandler.this.mContext).getSupportFragmentManager().popBackStack();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (backToNormal) {
                ((UIExtensionsManager) SignatureToolHandler.this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
                return;
            }
            List<String> list = SignatureDataUtil.getRecentKeys(SignatureToolHandler.this.mContext);
            if (list == null || list.size() <= 0) {
                ((UIExtensionsManager) SignatureToolHandler.this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
            } else if (SignatureToolHandler.this.mBitmap != null && SignatureToolHandler.this.mBBoxTmp != null) {
                SignatureToolHandler.this.mAnnotMenu.show(SignatureToolHandler.this.mBBoxTmp);
            }
        }

        public void onSuccess(boolean isFromFragment, Bitmap bitmap, Rect rect, int color, String dsgPath) {
            if (isFromFragment) {
                try {
                    ((FragmentActivity) SignatureToolHandler.this.mContext).getSupportFragmentManager().popBackStack();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (isFromFragment) {
                final Bitmap bitmap2 = bitmap;
                final Rect rect2 = rect;
                final int i = color;
                final String str = dsgPath;
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    public void run() {
                        AnonymousClass1.this.showSignOnPage(bitmap2, rect2, i, str);
                    }
                }, 300);
                return;
            }
            showSignOnPage(bitmap, rect, color, dsgPath);
        }

        private void showSignOnPage(Bitmap bitmap, Rect rect, int color, String dsgPath) {
            if (bitmap == null || rect.isEmpty()) {
                SignatureToolHandler.this.mIsSignEditing = false;
            } else if (bitmap.getWidth() < rect.width() || bitmap.getHeight() < rect.height()) {
                SignatureToolHandler.this.mIsSignEditing = false;
            } else {
                float scale = ((float) rect.height()) / ((float) rect.width());
                if (SignatureToolHandler.this.mPageIndex >= 0) {
                    int rh;
                    int rw;
                    int cx;
                    int cy;
                    int offsetX;
                    int offsetY;
                    int vw = SignatureToolHandler.this.mPdfViewCtrl.getPageViewWidth(SignatureToolHandler.this.mPageIndex);
                    int vh = SignatureToolHandler.this.mPdfViewCtrl.getPageViewHeight(SignatureToolHandler.this.mPageIndex);
                    if (scale >= 1.0f) {
                        rh = SignatureToolHandler.this.dp2px(150);
                        rw = (int) (((float) rh) / scale);
                        if (rw > vw / 2) {
                            rw = vw / 2;
                        }
                        SignatureToolHandler.this.mPdfViewCtrl.convertPdfPtToPageViewPt(SignatureToolHandler.this.mDownPoint, SignatureToolHandler.this.mDownPoint, SignatureToolHandler.this.mPageIndex);
                    } else {
                        rw = SignatureToolHandler.this.dp2px(150);
                        rh = (int) (((float) rw) * scale);
                        if (((double) scale) > 1.0d && rh > vh / 2) {
                            rh = vh / 2;
                        }
                        SignatureToolHandler.this.mPdfViewCtrl.convertPdfPtToPageViewPt(SignatureToolHandler.this.mDownPoint, SignatureToolHandler.this.mDownPoint, SignatureToolHandler.this.mPageIndex);
                    }
                    if (SignatureToolHandler.this.mBitmap != null) {
                        SignatureToolHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(SignatureToolHandler.this.mBbox, SignatureToolHandler.this.mBbox, SignatureToolHandler.this.mPageIndex);
                        cx = ((int) SignatureToolHandler.this.mBbox.centerX()) - (rw / 2);
                        cy = ((int) SignatureToolHandler.this.mBbox.centerY()) - (rh / 2);
                        SignatureToolHandler.this.mBitmap.recycle();
                        SignatureToolHandler.this.mBitmap = null;
                    } else {
                        cx = ((int) SignatureToolHandler.this.mDownPoint.x) - (rw / 2);
                        cy = ((int) SignatureToolHandler.this.mDownPoint.y) - (rh / 2);
                    }
                    if (cx <= 0) {
                        cx = 0;
                    }
                    if (cy <= 0) {
                        cy = 0;
                    }
                    if (cx + rw > vw) {
                        offsetX = (vw - rw) - 5;
                    } else {
                        offsetX = cx;
                    }
                    if (cy + rh > vh) {
                        offsetY = (vh - rh) - 5;
                    } else {
                        offsetY = cy;
                    }
                    SignatureToolHandler.this.mBbox.set((float) offsetX, (float) offsetY, (float) (rw + offsetX), (float) (rh + offsetY));
                    SignatureToolHandler.this.mBitmapRect.set(0, 0, rect.width(), rect.height());
                    int t = rect.top;
                    int b = rect.bottom;
                    int l = rect.left;
                    int r = rect.right;
                    int[] pixels = new int[(rect.width() * rect.height())];
                    bitmap.getPixels(pixels, 0, r - l, l, t, r - l, b - t);
                    for (int i = 0; i < pixels.length; i++) {
                        if (-1 == pixels[i]) {
                            pixels[i] = 0;
                        }
                    }
                    SignatureToolHandler.this.mBitmap = Bitmap.createBitmap(pixels, rect.width(), rect.height(), Config.ARGB_8888);
                    bitmap.recycle();
                    SignatureToolHandler.this.mDsgPath = dsgPath;
                    SignatureToolHandler.this.mIsSignEditing = true;
                    SignatureToolHandler.this.mAnnotMenu.setMenuItems(SignatureToolHandler.this.mMenuItems);
                    SignatureToolHandler.this.mAnnotMenu.setListener(SignatureToolHandler.this.mMenuListener);
                    SignatureToolHandler.this.mAnnotMenu.setShowAlways(true);
                    SignatureToolHandler.this.mPdfViewCtrl.convertPageViewRectToPdfRect(SignatureToolHandler.this.mBbox, SignatureToolHandler.this.mBbox, SignatureToolHandler.this.mPageIndex);
                    SignatureToolHandler.this.mBBoxTmp.set(SignatureToolHandler.this.mBbox);
                    SignatureToolHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(SignatureToolHandler.this.mBBoxTmp, SignatureToolHandler.this.mBBoxTmp, SignatureToolHandler.this.mPageIndex);
                    SignatureToolHandler.this.mFrameRectF.set(SignatureToolHandler.this.mBBoxTmp);
                    SignatureToolHandler.this.mFrameRectF.inset(-2.5f, -2.5f);
                    SignatureToolHandler.this.mPdfViewCtrl.invalidate();
                    SignatureToolHandler.this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(SignatureToolHandler.this.mBBoxTmp, SignatureToolHandler.this.mBBoxTmp, SignatureToolHandler.this.mPageIndex);
                    SignatureToolHandler.this.mAnnotMenu.show(SignatureToolHandler.this.mBBoxTmp);
                }
            }
        }
    };
    private boolean mIsSignEditing;
    private boolean mIsSigning;
    private int mLastOper = -1;
    private PointF mLastPoint;
    private Matrix mMatrix;
    private ArrayList<Integer> mMenuItems;
    private ClickListener mMenuListener = new ClickListener() {
        public void onAMClick(int id) {
            if (!AppUtil.isFastDoubleClick()) {
                SignatureToolHandler.this.mIsSigning = false;
                if (id == 12) {
                    SignatureToolHandler.this.doSign();
                } else if (15 == id) {
                    ((UIExtensionsManager) SignatureToolHandler.this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
                } else if (2 == id) {
                    int pageIndex = SignatureToolHandler.this.mPageIndex;
                    SignatureToolHandler.this.clearData();
                    if (SignatureToolHandler.this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                        SignatureToolHandler.this.mPdfViewCtrl.invalidate();
                    }
                }
            }
        }
    };
    private int mPageIndex = -1;
    private PaintFlagsDrawFilter mPaintFilter;
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private PropertyBar mPropertyBar;
    private boolean mTouchCaptured;
    private RectF mTouchTmpRectF = new RectF();
    private UITextEditDialog mWillSignDialog;

    public SignatureToolHandler(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mParent = parent;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mDisplay = AppDisplay.getInstance(this.mContext);
        init();
        this.mPropertyBar = new PropertyBarImpl(this.mContext, this.mPdfViewCtrl, this.mParent);
    }

    private void init() {
        this.mLastPoint = new PointF();
        this.mFrmPaint = new Paint();
        this.mFrmPaint.setPathEffect(AppAnnotUtil.getAnnotBBoxPathEffect());
        this.mFrmPaint.setStyle(Style.STROKE);
        this.mFrmPaint.setAntiAlias(true);
        this.mPaintFilter = new PaintFlagsDrawFilter(0, 3);
        this.mMatrix = new Matrix();
        this.mFrameRectF = new RectF();
        this.mDesRect = new RectF();
        this.mBbox = new RectF();
        this.mBBoxTmp = new RectF();
        this.mBitmapRect = new Rect();
        this.mCtlPtPaint = new Paint();
        this.mBitmapPaint = new Paint();
        this.mBitmapPaint.setAntiAlias(true);
        this.mBitmapPaint.setFilterBitmap(true);
        this.mAnnotMenu = new AnnotMenuImpl(this.mContext, this.mParent);
        this.mMenuItems = new ArrayList();
        this.mMenuItems.add(0, Integer.valueOf(12));
        this.mMenuItems.add(1, Integer.valueOf(2));
        this.mCtlPtRadius = (float) dp2px(5);
    }

    public String getType() {
        return ToolHandler.TH_TYPE_SIGNATURE;
    }

    public void onActivate() {
        this.mIsSignEditing = false;
        this.mIsSigning = false;
        this.mPageIndex = -1;
        showSignDialog(false);
    }

    public void onDeactivate() {
        this.mIsSignEditing = false;
        this.mIsSigning = false;
        if (this.mFragment != null && this.mFragment.isAttached()) {
            try {
                ((FragmentActivity) this.mContext).getSupportFragmentManager().popBackStack();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.mAnnotMenu.dismiss();
        this.mBbox.setEmpty();
        if (!(this.mBitmap == null || this.mBitmap.isRecycled())) {
            this.mBitmap.recycle();
        }
        this.mBitmap = null;
        this.mDsgPath = null;
    }

    public void addSignature(int pageIndex, PointF downPoint, boolean isFromTs) {
        if (!AppUtil.isFastDoubleClick()) {
            this.mPageIndex = pageIndex;
            this.mDownPoint = new PointF(downPoint.x, downPoint.y);
            this.mPdfViewCtrl.convertPageViewPtToPdfPt(this.mDownPoint, this.mDownPoint, this.mPageIndex);
            showSignDialog(isFromTs);
        }
    }

    public boolean onTouchEvent(int pageIndex, MotionEvent motionEvent) {
        if (this.mIsSigning) {
            return true;
        }
        int action = motionEvent.getActionMasked();
        PointF devPt = new PointF(motionEvent.getX(), motionEvent.getY());
        PointF point = new PointF();
        this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(devPt, point, pageIndex);
        float evX = point.x;
        float evY = point.y;
        switch (action) {
            case 0:
                if (this.mIsSignEditing && this.mPageIndex == pageIndex) {
                    this.mPdfViewCtrl.convertPageViewPtToDisplayViewPt(point, point, pageIndex);
                    this.mBBoxTmp.set(this.mBbox);
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mBBoxTmp, this.mBBoxTmp, this.mPageIndex);
                    this.mCurrentCtr = isTouchControlPoint(this.mBBoxTmp, evX, evY, 5.0f);
                    if (this.mCurrentCtr != -1) {
                        this.mLastOper = 1;
                        this.mTouchCaptured = true;
                        this.mLastPoint.set(evX, evY);
                        this.mAnnotMenu.dismiss();
                        return true;
                    } else if (this.mBBoxTmp.contains(evX, evY)) {
                        this.mLastOper = 0;
                        this.mTouchCaptured = true;
                        this.mLastPoint.set(evX, evY);
                        this.mAnnotMenu.dismiss();
                        return true;
                    }
                }
                return false;
            case 1:
            case 3:
                if (this.mTouchCaptured) {
                    this.mBBoxTmp.set(this.mBbox);
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mBBoxTmp, this.mBBoxTmp, this.mPageIndex);
                    this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mBBoxTmp, this.mBBoxTmp, this.mPageIndex);
                    this.mAnnotMenu.show(this.mBBoxTmp);
                }
                this.mTouchCaptured = false;
                this.mLastPoint.set(0.0f, 0.0f);
                this.mLastOper = -1;
                this.mCurrentCtr = -1;
                break;
            case 2:
                if (!this.mTouchCaptured) {
                    return false;
                }
                if (!(evX == this.mLastPoint.x || evY == this.mLastPoint.y)) {
                    this.mBBoxTmp.set(this.mBbox);
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mBBoxTmp, this.mBBoxTmp, this.mPageIndex);
                    float dx;
                    float dy;
                    float deltaXY;
                    switch (this.mLastOper) {
                        case 0:
                            dx = evX - this.mLastPoint.x;
                            dy = evY - this.mLastPoint.y;
                            this.mBBoxTmp.offset(dx, dy);
                            float adjustx = 0.0f;
                            float adjusty = 0.0f;
                            deltaXY = ((5.0f / 2.0f) + this.mCtlPtRadius) + 3.0f;
                            if (this.mBBoxTmp.left < deltaXY) {
                                adjustx = (-this.mBBoxTmp.left) + deltaXY;
                            }
                            if (this.mBBoxTmp.top < deltaXY) {
                                adjusty = (-this.mBBoxTmp.top) + deltaXY;
                            }
                            if (this.mBBoxTmp.right > ((float) this.mPdfViewCtrl.getPageViewWidth(pageIndex)) - deltaXY) {
                                adjustx = (((float) this.mPdfViewCtrl.getPageViewWidth(pageIndex)) - this.mBBoxTmp.right) - deltaXY;
                            }
                            if (this.mBBoxTmp.bottom > ((float) this.mPdfViewCtrl.getPageViewHeight(pageIndex)) - deltaXY) {
                                adjusty = (((float) this.mPdfViewCtrl.getPageViewHeight(pageIndex)) - this.mBBoxTmp.bottom) - deltaXY;
                            }
                            this.mBBoxTmp.offset(adjustx, adjusty);
                            this.mBbox.set(this.mBBoxTmp);
                            this.mBBoxTmp.offset(-(adjustx + dx), -(adjusty + dy));
                            this.mBBoxTmp.union(this.mBbox);
                            this.mBBoxTmp.inset(-deltaXY, -deltaXY);
                            this.mPdfViewCtrl.convertPageViewRectToPdfRect(this.mBbox, this.mBbox, this.mPageIndex);
                            this.mPdfViewCtrl.invalidate();
                            this.mLastPoint.offset(dx + adjustx, dy + adjusty);
                            break;
                        case 1:
                            boolean isBreak;
                            dx = evX - this.mLastPoint.x;
                            dy = evY - this.mLastPoint.y;
                            if (this.mBBoxTmp.width() - Math.abs(dx) < 30.0f) {
                                isBreak = false;
                                switch (this.mCurrentCtr) {
                                    case 0:
                                    case 6:
                                    case 7:
                                        if (dx > 0.0f) {
                                            isBreak = true;
                                            break;
                                        }
                                        break;
                                    case 2:
                                    case 3:
                                    case 4:
                                        if (dx < 0.0f) {
                                            isBreak = true;
                                            break;
                                        }
                                        break;
                                }
                                if (isBreak) {
                                    dx = 0.0f;
                                }
                            }
                            if (this.mBBoxTmp.height() - Math.abs(dy) < 30.0f) {
                                isBreak = false;
                                switch (this.mCurrentCtr) {
                                    case 0:
                                    case 1:
                                    case 2:
                                        if (dy > 0.0f) {
                                            isBreak = true;
                                            break;
                                        }
                                        break;
                                    case 4:
                                    case 5:
                                    case 6:
                                        if (dy < 0.0f) {
                                            isBreak = true;
                                            break;
                                        }
                                        break;
                                }
                                if (isBreak) {
                                    dy = 0.0f;
                                }
                            }
                            this.mTouchTmpRectF.set(this.mBBoxTmp);
                            calculateScaleMatrix(this.mMatrix, this.mCurrentCtr, this.mBBoxTmp, dx, dy);
                            this.mMatrix.mapRect(this.mBBoxTmp);
                            deltaXY = ((5.0f / 2.0f) + this.mCtlPtRadius) + 3.0f;
                            PointF adjustXY = adjustScalePointF(pageIndex, this.mBBoxTmp, deltaXY);
                            this.mLastPoint.offset(adjustXY.x + dx, adjustXY.y + dy);
                            this.mBbox.set(this.mBBoxTmp);
                            this.mPdfViewCtrl.convertPageViewRectToPdfRect(this.mBbox, this.mBbox, this.mPageIndex);
                            this.mTouchTmpRectF.union(this.mBBoxTmp);
                            this.mTouchTmpRectF.inset(-deltaXY, -deltaXY);
                            this.mPdfViewCtrl.invalidate();
                            break;
                        default:
                            return false;
                    }
                }
                return true;
        }
        return false;
    }

    public boolean onLongPress(int pageIndex, MotionEvent motionEvent) {
        if (this.mIsSignEditing) {
            return false;
        }
        PointF devPt = new PointF(motionEvent.getX(), motionEvent.getY());
        PointF point = new PointF();
        this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(devPt, point, pageIndex);
        addSignature(pageIndex, point, false);
        return true;
    }

    public boolean onSingleTapConfirmed(int pageIndex, MotionEvent motionEvent) {
        if (this.mIsSignEditing) {
            return false;
        }
        PointF devPt = new PointF(motionEvent.getX(), motionEvent.getY());
        PointF point = new PointF();
        this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(devPt, point, pageIndex);
        addSignature(pageIndex, point, false);
        return true;
    }

    public void onDraw(int pageIndex, Canvas canvas) {
        if (this.mPageIndex == pageIndex && this.mBitmap != null) {
            this.mDesRect.set(this.mBbox);
            this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mDesRect, this.mDesRect, this.mPageIndex);
            if (!this.mDesRect.isEmpty()) {
                canvas.setDrawFilter(this.mPaintFilter);
                this.mFrameRectF.set(this.mDesRect);
                this.mFrameRectF.inset((-1084227584) * 0.5f, (-1084227584) * 0.5f);
                canvas.save();
                drawFrame(canvas, this.mFrameRectF);
                this.mBitmapPaint.setColor(-16777216);
                canvas.drawBitmap(this.mBitmap, this.mBitmapRect, this.mDesRect, this.mBitmapPaint);
                drawFrame(canvas, this.mFrameRectF, -12740612, 5.0f);
                drawControlPoints(canvas, this.mFrameRectF, -12740612, 5.0f);
                canvas.restore();
            }
        }
    }

    private void drawFrame(Canvas canvas, RectF rectF, int color, float thickness) {
        this.mFrmPaint.setColor(color);
        this.mFrmPaint.setStrokeWidth(this.mFrmLineWidth);
        canvas.drawRect(rectF, this.mFrmPaint);
    }

    private void drawFrame(Canvas canvas, RectF rectF) {
        this.mBitmapPaint.setColor(1379647997);
        canvas.drawRect(rectF, this.mBitmapPaint);
    }

    private void drawControlPoints(Canvas canvas, RectF rectF, int color, float thickness) {
        float[] ctlPts = calculateControlPoints(rectF);
        this.mCtlPtPaint.setStrokeWidth(this.mCtlPtLineWidth);
        for (int i = 0; i < ctlPts.length; i += 2) {
            this.mCtlPtPaint.setColor(-1);
            this.mCtlPtPaint.setStyle(Style.FILL);
            canvas.drawCircle(ctlPts[i], ctlPts[i + 1], this.mCtlPtRadius, this.mCtlPtPaint);
            this.mCtlPtPaint.setColor(color);
            this.mCtlPtPaint.setStyle(Style.STROKE);
            canvas.drawCircle(ctlPts[i], ctlPts[i + 1], this.mCtlPtRadius, this.mCtlPtPaint);
        }
    }

    public void onDrawForControls(Canvas canvas) {
        if (this.mIsSignEditing) {
            this.mBBoxTmp.set(this.mBbox);
            if (this.mPdfViewCtrl.isPageVisible(this.mPageIndex)) {
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mBBoxTmp, this.mBBoxTmp, this.mPageIndex);
                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mBBoxTmp, this.mBBoxTmp, this.mPageIndex);
                this.mAnnotMenu.update(this.mBBoxTmp);
            }
        }
    }

    private void showSignDialog(boolean isFromTs) {
        HashMap<String, Object> map = SignatureDataUtil.getRecentData(this.mContext);
        if (map != null && map.get("rect") != null && map.get("bitmap") != null) {
            Object dsgPathObj = map.get("dsgPath");
            if (dsgPathObj == null || AppUtil.isEmpty((String) dsgPathObj)) {
                this.mInkCallback.onSuccess(false, (Bitmap) map.get("bitmap"), (Rect) map.get("rect"), ((Integer) map.get("color")).intValue(), null);
            } else {
                this.mInkCallback.onSuccess(false, (Bitmap) map.get("bitmap"), (Rect) map.get("rect"), ((Integer) map.get("color")).intValue(), (String) dsgPathObj);
            }
        } else if (!isFromTs) {
            showDrawViewFragment();
        }
    }

    private void showDrawViewFragment() {
        FragmentActivity act = this.mContext;
        if (this.mFragment == null) {
            this.mFragment = (SignatureFragment) act.getSupportFragmentManager().findFragmentByTag("InkSignFragment");
        }
        if (this.mFragment == null) {
            this.mFragment = new SignatureFragment();
            this.mFragment.init(this.mContext, this.mParent, this.mPdfViewCtrl);
        }
        this.mFragment.setInkCallback(this.mInkCallback);
        if (this.mFragment.isAdded()) {
            act.getSupportFragmentManager().beginTransaction().attach(this.mFragment);
        } else {
            act.getSupportFragmentManager().beginTransaction().add(R.id.rd_main_id, this.mFragment, "InkSignFragment").addToBackStack(null).commitAllowingStateLoss();
        }
    }

    private void applyRecentNormalSignData(boolean isFromTs) {
        HashMap<String, Object> map = SignatureDataUtil.getRecentNormalSignData(this.mContext);
        if (map != null && map.get("rect") != null && map.get("bitmap") != null) {
            this.mInkCallback.onSuccess(false, (Bitmap) map.get("bitmap"), (Rect) map.get("rect"), ((Integer) map.get("color")).intValue(), null);
        } else if (!isFromTs) {
            showDrawViewFragment();
        }
    }

    private PointF adjustScalePointF(int pageIndex, RectF rectF, float dxy) {
        float adjustx = 0.0f;
        float adjusty = 0.0f;
        int pageHeight = this.mPdfViewCtrl.getPageViewHeight(pageIndex);
        int pageWidth = this.mPdfViewCtrl.getPageViewWidth(pageIndex);
        switch (this.mCurrentCtr) {
            case 0:
                if (rectF.left < dxy) {
                    adjustx = (-rectF.left) + dxy;
                    rectF.left = dxy;
                }
                if (rectF.top < dxy) {
                    adjusty = (-rectF.top) + dxy;
                    rectF.top = dxy;
                    break;
                }
                break;
            case 1:
                if (rectF.top < dxy) {
                    adjusty = (-rectF.top) + dxy;
                    rectF.top = dxy;
                    break;
                }
                break;
            case 2:
                if (rectF.top < dxy) {
                    adjusty = (-rectF.top) + dxy;
                    rectF.top = dxy;
                }
                if (rectF.right > ((float) pageWidth) - dxy) {
                    adjustx = (((float) pageWidth) - rectF.right) - dxy;
                    rectF.right = ((float) pageWidth) - dxy;
                    break;
                }
                break;
            case 3:
                if (rectF.right > ((float) pageWidth) - dxy) {
                    adjustx = (((float) pageWidth) - rectF.right) - dxy;
                    rectF.right = ((float) pageWidth) - dxy;
                    break;
                }
                break;
            case 4:
                if (rectF.right > ((float) pageWidth) - dxy) {
                    adjustx = (((float) pageWidth) - rectF.right) - dxy;
                    rectF.right = ((float) pageWidth) - dxy;
                }
                if (rectF.bottom > ((float) pageHeight) - dxy) {
                    adjusty = (((float) pageHeight) - rectF.bottom) - dxy;
                    rectF.bottom = ((float) pageHeight) - dxy;
                    break;
                }
                break;
            case 5:
                if (rectF.bottom > ((float) pageHeight) - dxy) {
                    adjusty = (((float) pageHeight) - rectF.bottom) - dxy;
                    rectF.bottom = ((float) pageHeight) - dxy;
                    break;
                }
                break;
            case 6:
                if (rectF.left < dxy) {
                    adjustx = (-rectF.left) + dxy;
                    rectF.left = dxy;
                }
                if (rectF.bottom > ((float) pageHeight) - dxy) {
                    adjusty = (((float) pageHeight) - rectF.bottom) - dxy;
                    rectF.bottom = ((float) pageHeight) - dxy;
                    break;
                }
                break;
            case 7:
                if (rectF.left < dxy) {
                    adjustx = (-rectF.left) + dxy;
                    rectF.left = dxy;
                    break;
                }
                break;
        }
        return new PointF(adjustx, adjusty);
    }

    private int isTouchControlPoint(RectF rectF, float x, float y, float thickness) {
        this.mFrameTmpRect.set(rectF);
        this.mFrameTmpRect.inset((-thickness) * 0.5f, (-thickness) * 0.5f);
        float[] ctlPts = calculateControlPoints(this.mFrameTmpRect);
        for (int i = 0; i < ctlPts.length / 2; i++) {
            this.mAreaTmpRect.set(ctlPts[i * 2], ctlPts[(i * 2) + 1], ctlPts[i * 2], ctlPts[(i * 2) + 1]);
            this.mAreaTmpRect.inset(-this.mCtlPtTouchExt, -this.mCtlPtTouchExt);
            if (this.mAreaTmpRect.contains(x, y)) {
                return i;
            }
        }
        return -1;
    }

    private float[] calculateControlPoints(RectF rectF) {
        float l = rectF.left;
        float t = rectF.top;
        float r = rectF.right;
        float b = rectF.bottom;
        return new float[]{l, t, (l + r) / 2.0f, t, r, t, r, (t + b) / 2.0f, r, b, (l + r) / 2.0f, b, l, b, l, (t + b) / 2.0f};
    }

    private void calculateScaleMatrix(Matrix matrix, int ctl, RectF rectF, float dx, float dy) {
        matrix.reset();
        float[] ctlPts = calculateControlPoints(rectF);
        float px = ctlPts[ctl * 2];
        float py = ctlPts[(ctl * 2) + 1];
        float oppositeX = 0.0f;
        float oppositeY = 0.0f;
        if (ctl < 4 && ctl >= 0) {
            oppositeX = ctlPts[(ctl * 2) + 8];
            oppositeY = ctlPts[(ctl * 2) + 9];
        } else if (ctl >= 4) {
            oppositeX = ctlPts[(ctl * 2) - 8];
            oppositeY = ctlPts[(ctl * 2) - 7];
        }
        float scaleH = ((px + dx) - oppositeX) / (px - oppositeX);
        float scaleV = ((py + dy) - oppositeY) / (py - oppositeY);
        switch (ctl) {
            case 0:
            case 2:
            case 4:
            case 6:
                matrix.postScale(scaleH, scaleV, oppositeX, oppositeY);
                return;
            case 1:
            case 5:
                matrix.postScale(1.0f, scaleV, oppositeX, oppositeY);
                return;
            case 3:
            case 7:
                matrix.postScale(scaleH, 1.0f, oppositeX, oppositeY);
                return;
            default:
                return;
        }
    }

    private int dp2px(int dp) {
        return this.mDisplay.dp2px((float) dp);
    }

    public boolean onKeyBack() {
        this.mInkCallback.onBackPressed();
        return true;
    }

    public void reset() {
        this.mIsSignEditing = false;
        this.mIsSigning = false;
        if (this.mFragment != null && this.mFragment.isAttached()) {
            this.mInkCallback.onBackPressed();
        }
        if (this.mWillSignDialog != null && this.mWillSignDialog.getDialog().isShowing()) {
            try {
                this.mWillSignDialog.dismiss();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        this.mWillSignDialog = null;
    }

    public boolean doSign() {
        if (this.mBitmap == null) {
            return false;
        }
        this.mAnnotMenu.dismiss();
        if (this.mDefaultAdd) {
            sign2Doc();
            return true;
        }
        if (this.mWillSignDialog == null || this.mWillSignDialog.getDialog().getOwnerActivity() == null) {
            this.mWillSignDialog = new UITextEditDialog(this.mContext);
            this.mWillSignDialog.getCancelButton().setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!AppUtil.isFastDoubleClick()) {
                        SignatureToolHandler.this.mWillSignDialog.dismiss();
                        int pageIndex = SignatureToolHandler.this.mPageIndex;
                        SignatureToolHandler.this.clearData();
                        if (SignatureToolHandler.this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                            SignatureToolHandler.this.mPdfViewCtrl.invalidate();
                        }
                    }
                }
            });
            this.mWillSignDialog.setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    int pageIndex = SignatureToolHandler.this.mPageIndex;
                    SignatureToolHandler.this.clearData();
                    if (SignatureToolHandler.this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                        SignatureToolHandler.this.mPdfViewCtrl.invalidate();
                    }
                }
            });
            this.mWillSignDialog.getPromptTextView().setText(R.string.rv_sign_dialog_description);
            this.mWillSignDialog.setTitle(R.string.rv_sign_dialog_title);
            this.mWillSignDialog.getInputEditText().setVisibility(8);
        }
        this.mWillSignDialog.getOKButton().setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!AppUtil.isFastDoubleClick()) {
                    SignatureToolHandler.this.mWillSignDialog.dismiss();
                    SignatureToolHandler.this.sign2Doc();
                    SignatureToolHandler.this.mDefaultAdd = true;
                }
            }
        });
        this.mWillSignDialog.show();
        return true;
    }

    private void sign2Doc() {
        if (!AppUtil.isEmpty(this.mDsgPath)) {
            final Module dsgModule = ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_DIGITALSIGNATURE);
            if (dsgModule != null) {
                new UISaveAsDialog(this.mContext, "sign.pdf", "pdf", new ISaveAsOnOKClickCallBack() {
                    public void onOkClick(final String newFilePath) {
                        String tmpPath = newFilePath;
                        if (new File(newFilePath).exists()) {
                            tmpPath = new StringBuilder(String.valueOf(newFilePath)).append("_tmp.pdf").toString();
                        }
                        DigitalSignatureUtil dsgUtil = ((DigitalSignatureModule) dsgModule).getDSG_Util();
                        final String finalTmpPath = tmpPath;
                        String access$21 = SignatureToolHandler.this.mDsgPath;
                        Bitmap access$2 = SignatureToolHandler.this.mBitmap;
                        RectF access$9 = SignatureToolHandler.this.mBbox;
                        int access$6 = SignatureToolHandler.this.mPageIndex;
                        final Module module = dsgModule;
                        dsgUtil.addCertSignature(tmpPath, access$21, access$2, access$9, access$6, new IDigitalSignatureCreateCallBack() {
                            public void onCreateFinish(boolean success) {
                                if (success) {
                                    new File(finalTmpPath).renameTo(new File(newFilePath));
                                }
                                if (SignatureToolHandler.this.mPdfViewCtrl.isPageVisible(SignatureToolHandler.this.mPageIndex)) {
                                    RectF rect = new RectF(SignatureToolHandler.this.mBbox);
                                    SignatureToolHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(rect, rect, SignatureToolHandler.this.mPageIndex);
                                    SignatureToolHandler.this.clearData();
                                    SignatureToolHandler.this.mPdfViewCtrl.refresh(SignatureToolHandler.this.mPageIndex, AppDmUtil.rectFToRect(rect));
                                    SignatureToolHandler.this.mIsSigning = false;
                                    SignatureToolHandler.this.clearData();
                                    DocumentManager.getInstance(SignatureToolHandler.this.mPdfViewCtrl).clearUndoRedo();
                                    SignatureToolHandler.this.mPdfViewCtrl.openDoc(newFilePath, null);
                                    if (((DigitalSignatureModule) module).getDocPathChangeListener() != null) {
                                        ((DigitalSignatureModule) module).getDocPathChangeListener().onDocPathChange(newFilePath);
                                    }
                                    ((UIExtensionsManager) SignatureToolHandler.this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
                                } else if (success) {
                                    SignatureToolHandler.this.clearData();
                                } else {
                                    SignatureToolHandler.this.mIsSigning = false;
                                    ((UIExtensionsManager) SignatureToolHandler.this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
                                }
                            }
                        });
                    }

                    public void onCancelClick() {
                    }
                }).showDialog();
                return;
            }
        }
        PDFPage page = null;
        try {
            page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
        } catch (PDFException e) {
            e.printStackTrace();
        }
        this.mPdfViewCtrl.addTask(new SignaturePSITask(new SignatureSignEvent(page, this.mBitmap, this.mBbox, 15, null), new Callback() {
            public void result(Event event, boolean success) {
                if (SignatureToolHandler.this.mPdfViewCtrl.isPageVisible(SignatureToolHandler.this.mPageIndex)) {
                    RectF rect = new RectF(SignatureToolHandler.this.mBbox);
                    SignatureToolHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(rect, rect, SignatureToolHandler.this.mPageIndex);
                    SignatureToolHandler.this.mPdfViewCtrl.refresh(SignatureToolHandler.this.mPageIndex, AppDmUtil.rectFToRect(rect));
                    SignatureToolHandler.this.clearData();
                    return;
                }
                SignatureToolHandler.this.clearData();
            }
        }));
    }

    private void clearData() {
        this.mIsSignEditing = false;
        this.mIsSigning = false;
        if (this.mFragment != null && this.mFragment.isAttached()) {
            try {
                ((FragmentActivity) this.mContext).getSupportFragmentManager().popBackStack();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.mAnnotMenu.dismiss();
        this.mBbox.setEmpty();
        if (!(this.mBitmap == null || this.mBitmap.isRecycled())) {
            this.mBitmap.recycle();
        }
        this.mBitmap = null;
        this.mDsgPath = null;
        this.mPageIndex = -1;
    }

    public void showSignList(RectF rectF) {
        this.mAnnotMenu.dismiss();
        if (this.mDisplay.isPad()) {
            showMixListPopupPad(rectF);
        } else {
            showMixListPopupPhone();
        }
    }

    private void showMixListPopupPhone() {
        if (this.mBitmap == null) {
            SignatureMixListPopup.show(this.mContext, this.mParent, this.mPdfViewCtrl, this.mInkCallback);
        } else {
            SignatureMixListPopup.show(this.mContext, this.mParent, this.mPdfViewCtrl, this.mInkCallback, this.mBBoxTmp);
        }
    }

    private void showMixListPopupPad(RectF rectF) {
        final SignatureListPicker listPicker = new SignatureListPicker(this.mContext, this.mParent, this.mPdfViewCtrl, this.mInkCallback);
        listPicker.init(new ISignListPickerDismissCallback() {
            public void onDismiss() {
                SignatureToolHandler.this.mPropertyBar.dismiss();
            }
        });
        this.mPropertyBar.setArrowVisible(true);
        this.mPropertyBar.reset(0);
        this.mPropertyBar.addContentView(listPicker.getRootView());
        listPicker.getRootView().getLayoutParams().height = this.mDisplay.dp2px(460.0f);
        this.mPropertyBar.setDismissListener(new DismissListener() {
            public void onDismiss() {
                if (listPicker.getBaseItemsSize() == 0) {
                    ((UIExtensionsManager) SignatureToolHandler.this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
                }
                listPicker.dismiss();
                if (SignatureToolHandler.this.mBitmap != null) {
                    SignatureToolHandler.this.mAnnotMenu.show(SignatureToolHandler.this.mBBoxTmp);
                }
            }
        });
        this.mPropertyBar.show(rectF, true);
    }

    public void setColor(int color) {
        this.mColor = color;
    }

    public int getColor() {
        return this.mColor;
    }

    public float getDiameter() {
        return this.mDiameter;
    }

    public void setDiameter(float diameter) {
        this.mDiameter = diameter;
    }
}
