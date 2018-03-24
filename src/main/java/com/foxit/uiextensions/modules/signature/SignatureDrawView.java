package com.foxit.uiextensions.modules.signature;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.controls.propertybar.PropertyBar.DismissListener;
import com.foxit.uiextensions.controls.propertybar.PropertyBar.PropertyChangeListener;
import com.foxit.uiextensions.controls.propertybar.imp.PropertyBarImpl;
import com.foxit.uiextensions.controls.toolbar.BaseBar;
import com.foxit.uiextensions.controls.toolbar.BaseBar.TB_Position;
import com.foxit.uiextensions.controls.toolbar.BaseItem;
import com.foxit.uiextensions.controls.toolbar.BaseItem.ItemType;
import com.foxit.uiextensions.controls.toolbar.CircleItem;
import com.foxit.uiextensions.controls.toolbar.PropertyCircleItem;
import com.foxit.uiextensions.controls.toolbar.impl.BaseItemImpl;
import com.foxit.uiextensions.controls.toolbar.impl.BottomBarImpl;
import com.foxit.uiextensions.controls.toolbar.impl.CircleItemImpl;
import com.foxit.uiextensions.controls.toolbar.impl.PropertyCircleItemImp;
import com.foxit.uiextensions.security.digitalsignature.DigitalSignatureModule;
import com.foxit.uiextensions.security.digitalsignature.DigitalSignatureUtil;
import com.foxit.uiextensions.security.digitalsignature.IDigitalSignatureCallBack;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppResource;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

class SignatureDrawView {
    private static final int MSG_CLEAR = 2;
    private static final int MSG_COLOR = 4;
    private static final int MSG_DIAMETER = 8;
    private static final int MSG_DRAW = 1;
    private static final int MSG_RELEASE = 16;
    private CircleItem mBackItem;
    private Bitmap mBitmap;
    private int mBmpHeight;
    private int mBmpWidth;
    private boolean mCanDraw = false;
    private BaseItem mCertificateItem;
    private CircleItem mClearItem;
    private Context mContext;
    private String mCurDsgPath;
    private AppDisplay mDisplay;
    private ViewGroup mDrawContainer;
    private SignatureDrawEvent mDrawEvent;
    private DrawView mDrawView;
    private DigitalSignatureUtil mDsgUtil;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (SignatureDrawView.this.mDrawView != null) {
                        SignatureDrawView.this.mCanDraw = true;
                        SignatureDrawView.this.mDrawView.invalidate();
                        return;
                    }
                    return;
                case 2:
                    SignatureDrawView.this.mCanDraw = true;
                    SignatureDrawView.this.mSaveItem.setEnable(false);
                    SignatureDrawView.this.mDrawView.invalidate();
                    return;
                case 4:
                    SignatureDrawView.this.mCanDraw = true;
                    return;
                case 8:
                    SignatureDrawView.this.mCanDraw = true;
                    return;
                case 16:
                    SignatureDrawView.this.mCanDraw = false;
                    if (SignatureDrawView.this.mBitmap != null && SignatureDrawView.this.mBitmap.isRecycled()) {
                        SignatureDrawView.this.mBitmap.recycle();
                    }
                    SignatureDrawView.this.mBitmap = null;
                    return;
                default:
                    return;
            }
        }
    };
    private String mKey;
    private OnDrawListener mListener;
    private View mMaskView;
    private OnClickListener mOnClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (!AppUtil.isFastDoubleClick()) {
                int id = v.getId();
                if (R.id.sig_create_back != id) {
                    if (R.id.sig_create_property == id) {
                        SignatureDrawView.this.preparePropertyBar();
                        SignatureDrawView.this.addMask();
                        Rect rect = new Rect();
                        SignatureDrawView.this.mProItem.getContentView().getGlobalVisibleRect(rect);
                        SignatureDrawView.this.mPropertyBar.show(new RectF(rect), false);
                    }
                    if (R.id.sig_create_delete == id) {
                        SignatureDrawView.this.clearCanvas();
                    } else if (R.id.sig_create_save == id && SignatureDrawView.this.mDrawView != null && SignatureDrawView.this.mDrawView.getBmp() != null) {
                        SignatureDrawView.this.saveSign();
                        if (!(((UIExtensionsManager) SignatureDrawView.this.mPdfViewCtrl.getUIExtensionsManager()).getCurrentToolHandler() instanceof SignatureToolHandler)) {
                            ((UIExtensionsManager) SignatureDrawView.this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(((SignatureModule) ((UIExtensionsManager) SignatureDrawView.this.mPdfViewCtrl.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_PSISIGNATURE)).getToolHandler());
                        }
                    }
                } else if (SignatureDrawView.this.mListener != null) {
                    SignatureDrawView.this.mListener.onBackPressed();
                }
            }
        }
    };
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private PropertyCircleItem mProItem;
    private PropertyBar mPropertyBar;
    private DismissListener mPropertyBarDismissListener = new DismissListener() {
        public void onDismiss() {
            if (SignatureDrawView.this.mMaskView != null) {
                SignatureDrawView.this.mMaskView.setVisibility(4);
            }
        }
    };
    private Rect mRect = new Rect();
    private AlertDialog mSaveDialog;
    private CircleItem mSaveItem;
    private BaseBar mSignCreateBottomBar;
    private RelativeLayout mSignCreateBottomBarLayout;
    private BaseBar mSignCreateTopBar;
    private RelativeLayout mSignCreateTopBarLayout;
    private BaseItem mTitleItem;
    private SignatureToolHandler mToolHandler;
    private Rect mValidRect = new Rect();
    private View mViewGroup;
    private PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
        public void onValueChanged(long property, int value) {
            if (property == 1) {
                if (value != SignatureDrawView.this.mToolHandler.getColor()) {
                    SignatureDrawView.this.mToolHandler.setColor(value);
                    SignatureDrawView.this.setInkColor(value);
                    SignatureDrawView.this.mProItem.setCentreCircleColor(value);
                }
            } else if (property == 128 && value != SignatureDrawView.this.mToolHandler.getColor()) {
                SignatureDrawView.this.mToolHandler.setColor(value);
                SignatureDrawView.this.setInkColor(value);
                SignatureDrawView.this.mProItem.setCentreCircleColor(value);
            }
        }

        public void onValueChanged(long property, float value) {
            if (property == 4 && SignatureDrawView.this.mToolHandler.getDiameter() != value) {
                SignatureDrawView.this.mToolHandler.setDiameter(value);
                SignatureDrawView.this.setInkDiameter(value);
            }
        }

        public void onValueChanged(long property, String value) {
        }
    };

    class DrawView extends View {
        private boolean mMultiPointer;
        private Paint mPaint;
        private boolean mPointInvalid;
        private PointF mPointTmp;

        public DrawView(Context context) {
            super(context);
            setLayoutParams(new LayoutParams(-1, -1));
            if (VERSION.SDK_INT >= 11) {
                setLayerType(1, null);
            }
            this.mPaint = new Paint();
            this.mPaint.setAntiAlias(true);
            this.mPaint.setFilterBitmap(true);
            this.mPointTmp = new PointF();
        }

        protected void onDraw(Canvas canvas) {
            if (SignaturePSITask.mPsi != null) {
                canvas.drawBitmap(SignatureDrawView.this.mBitmap, 0.0f, 0.0f, this.mPaint);
            }
        }

        private float getDistanceOfTwoPoint(PointF p1, PointF p2) {
            return (float) Math.sqrt((double) (((p1.x - p2.x) * (p1.x - p2.x)) + ((p1.y - p2.y) * (p1.y - p2.y))));
        }

        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
        }

        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
        }

        public boolean onTouchEvent(MotionEvent event) {
            if (!SignatureDrawView.this.mCanDraw || SignatureDrawView.this.mListener == null || !SignatureDrawView.this.mListener.canDraw()) {
                return false;
            }
            int count = event.getPointerCount();
            PointF point = new PointF(event.getX(), event.getY());
            if (count > 1) {
                if (!this.mMultiPointer) {
                    this.mPointTmp.set(point);
                    this.mMultiPointer = true;
                }
                return false;
            }
            int action = event.getAction();
            float pressure = event.getPressure();
            if (((double) pressure) < 0.1d) {
                pressure = 0.1f;
            }
            List<PointF> points;
            List<Float> pressures;
            switch (action) {
                case 0:
                    if (!SignatureDrawView.this.mValidRect.contains((int) event.getX(), (int) event.getY())) {
                        this.mPointInvalid = true;
                        break;
                    }
                    this.mPointInvalid = false;
                    points = new ArrayList();
                    pressures = new ArrayList();
                    points.add(point);
                    pressures.add(Float.valueOf(pressure));
                    SignatureDrawView.this.addPoint(points, pressures, 1);
                    this.mPointTmp.set(point);
                    break;
                case 1:
                    points = new ArrayList();
                    pressures = new ArrayList();
                    pressures.add(Float.valueOf(pressure));
                    if (SignatureDrawView.this.mValidRect.contains((int) event.getX(), (int) event.getY())) {
                        if (!this.mPointInvalid) {
                            if (!this.mMultiPointer) {
                                points.add(point);
                                SignatureDrawView.this.addPoint(points, pressures, 3);
                                break;
                            }
                            this.mMultiPointer = false;
                            points.add(this.mPointTmp);
                            SignatureDrawView.this.addPoint(points, pressures, 3);
                            break;
                        }
                        points.add(this.mPointTmp);
                        SignatureDrawView.this.addPoint(points, pressures, 3);
                        break;
                    }
                    points.add(this.mPointTmp);
                    SignatureDrawView.this.addPoint(points, pressures, 3);
                    break;
                case 2:
                    if (!this.mMultiPointer) {
                        if (SignatureDrawView.this.mValidRect.contains((int) event.getX(), (int) event.getY())) {
                            if (!this.mPointInvalid) {
                                if (getDistanceOfTwoPoint(point, this.mPointTmp) >= 2.0f) {
                                    points = new ArrayList();
                                    pressures = new ArrayList();
                                    for (int i = 0; i < event.getHistorySize(); i++) {
                                        this.mPointTmp.set(event.getHistoricalX(i), event.getHistoricalY(i));
                                        if (getDistanceOfTwoPoint(point, this.mPointTmp) >= 2.0f) {
                                            points.add(new PointF(event.getHistoricalX(i), event.getHistoricalY(i)));
                                            pressures.add(Float.valueOf(event.getHistoricalPressure(i)));
                                        }
                                    }
                                    points.add(point);
                                    pressures.add(Float.valueOf(pressure));
                                    SignatureDrawView.this.addPoint(points, pressures, 2);
                                    break;
                                }
                            }
                            this.mPointInvalid = false;
                            points = new ArrayList();
                            pressures = new ArrayList();
                            points.add(point);
                            pressures.add(Float.valueOf(pressure));
                            SignatureDrawView.this.addPoint(points, pressures, 1);
                            this.mPointTmp.set(point);
                            break;
                        }
                        this.mPointInvalid = true;
                        break;
                    }
                    break;
            }
            return true;
        }

        public Bitmap getBmp() {
            if (SignatureDrawView.this.mBitmap != null) {
                return Bitmap.createBitmap(SignatureDrawView.this.mBitmap);
            }
            return null;
        }
    }

    public interface OnDrawListener {
        boolean canDraw();

        void moveToTemplate();

        void onBackPressed();

        void result(Bitmap bitmap, Rect rect, int i, String str);
    }

    public void setOnDrawListener(OnDrawListener listener) {
        this.mListener = listener;
    }

    public SignatureDrawView(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mParent = parent;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mViewGroup = View.inflate(this.mContext, R.layout.rv_sg_create, null);
        this.mDisplay = AppDisplay.getInstance(this.mContext);
        this.mSignCreateTopBarLayout = (RelativeLayout) this.mViewGroup.findViewById(R.id.sig_create_top_bar_layout);
        this.mSignCreateBottomBarLayout = (RelativeLayout) this.mViewGroup.findViewById(R.id.sig_create_bottom_bar_layout);
        Module dsgModule = ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_DIGITALSIGNATURE);
        if (dsgModule != null) {
            this.mDsgUtil = ((DigitalSignatureModule) dsgModule).getDSG_Util();
        }
        SignatureModule sigModule = (SignatureModule) ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_PSISIGNATURE);
        if (sigModule != null) {
            this.mToolHandler = (SignatureToolHandler) sigModule.getToolHandler();
        }
        initBarLayout();
        this.mDrawContainer = (ViewGroup) this.mViewGroup.findViewById(R.id.sig_create_canvas);
        this.mDrawView = new DrawView(this.mContext);
        this.mDrawContainer.addView(this.mDrawView);
    }

    private void initBarLayout() {
        if (this.mSignCreateTopBar == null) {
            initTopBar();
            if (this.mDsgUtil != null) {
                if (this.mPdfViewCtrl.getDoc() == null) {
                    initBottomBar();
                } else {
                    initBottomBar();
                }
            }
            this.mSignCreateTopBarLayout.addView(this.mSignCreateTopBar.getContentView());
            if (this.mDsgUtil != null) {
                this.mSignCreateBottomBarLayout.addView(this.mSignCreateBottomBar.getContentView());
            }
        }
    }

    private void initTopBar() {
        this.mSignCreateTopBar = new SignatureCreateSignTitleBar(this.mContext);
        this.mSignCreateTopBar.setBackgroundColor(this.mContext.getResources().getColor(R.color.ux_bg_color_toolbar_light));
        int circleRes = R.drawable.rd_sign_circle_selector;
        this.mBackItem = new CircleItemImpl(this.mContext);
        this.mBackItem.setImageResource(R.drawable.rd_sg_back_selector);
        this.mBackItem.setCircleRes(circleRes);
        this.mBackItem.setId(R.id.sig_create_back);
        this.mBackItem.setOnClickListener(this.mOnClickListener);
        this.mClearItem = new CircleItemImpl(this.mContext);
        this.mClearItem.setImageResource(R.drawable.rd_sg_clear_selector);
        this.mClearItem.setCircleRes(circleRes);
        this.mClearItem.setId(R.id.sig_create_delete);
        this.mClearItem.setOnClickListener(this.mOnClickListener);
        this.mSaveItem = new CircleItemImpl(this.mContext);
        this.mSaveItem.setImageResource(R.drawable.rd_sg_save_selector);
        this.mSaveItem.setCircleRes(circleRes);
        this.mSaveItem.setId(R.id.sig_create_save);
        this.mSaveItem.setOnClickListener(this.mOnClickListener);
        this.mPropertyBar = new PropertyBarImpl(this.mContext, this.mPdfViewCtrl, this.mParent);
        this.mProItem = new PropertyCircleItemImp(this.mContext) {
            public void onItemLayout(int l, int t, int r, int b) {
                if (((UIExtensionsManager) SignatureDrawView.this.mPdfViewCtrl.getUIExtensionsManager()).getCurrentToolHandler() != null && ((UIExtensionsManager) SignatureDrawView.this.mPdfViewCtrl.getUIExtensionsManager()).getCurrentToolHandler().getType() == ToolHandler.TH_TYPE_SIGNATURE && SignatureDrawView.this.mPropertyBar.isShowing()) {
                    Rect mProRect = new Rect();
                    SignatureDrawView.this.mProItem.getContentView().getGlobalVisibleRect(mProRect);
                    SignatureDrawView.this.mPropertyBar.update(new RectF(mProRect));
                }
            }
        };
        this.mProItem.setCircleRes(circleRes);
        this.mProItem.setId(R.id.sig_create_property);
        this.mProItem.setOnClickListener(this.mOnClickListener);
        this.mTitleItem = new BaseItemImpl(this.mContext);
        this.mTitleItem.setTextSize(18.0f);
        this.mTitleItem.setText(AppResource.getString(this.mContext, R.string.rv_sign_create));
        if (!this.mDisplay.isPad()) {
            this.mSignCreateTopBar.setItemSpace(this.mDisplay.dp2px(16.0f));
        }
        this.mSignCreateTopBar.addView(this.mBackItem, TB_Position.Position_LT);
        this.mSignCreateTopBar.addView(this.mTitleItem, TB_Position.Position_LT);
        this.mSignCreateTopBar.addView(this.mProItem, TB_Position.Position_RB);
        this.mSignCreateTopBar.addView(this.mClearItem, TB_Position.Position_RB);
        this.mSignCreateTopBar.addView(this.mSaveItem, TB_Position.Position_RB);
    }

    private void initBottomBar() {
        this.mSignCreateBottomBar = new BottomBarImpl(this.mContext);
        this.mSignCreateBottomBar.setBackgroundColor(this.mContext.getResources().getColor(R.color.ux_bg_color_toolbar_light));
        this.mCertificateItem = new BaseItemImpl(this.mContext);
        this.mCertificateItem.setImageResource(R.drawable.sg_cert_add_selector);
        this.mCertificateItem.setText(AppResource.getString(this.mContext, R.string.sg_cert_add_text));
        this.mCertificateItem.setTextSize(18.0f);
        this.mCertificateItem.setRelation(12);
        this.mCertificateItem.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SignatureDrawView.this.mDsgUtil.addCertList(new IDigitalSignatureCallBack() {
                    public void onCertSelect(String path, String name) {
                        if (AppUtil.isEmpty(path) || AppUtil.isEmpty(name)) {
                            SignatureDrawView.this.mCertificateItem.setDisplayStyle(ItemType.Item_Text_Image);
                            SignatureDrawView.this.mCertificateItem.setText(AppResource.getString(SignatureDrawView.this.mContext, R.string.sg_cert_add_text));
                            SignatureDrawView.this.mCurDsgPath = null;
                            return;
                        }
                        SignatureDrawView.this.mCertificateItem.setDisplayStyle(ItemType.Item_Text);
                        SignatureDrawView.this.mCertificateItem.setText(new StringBuilder(String.valueOf(AppResource.getString(SignatureDrawView.this.mContext, R.string.sg_cert_current_name_title))).append(name).toString());
                        SignatureDrawView.this.mCurDsgPath = path;
                    }
                });
            }
        });
        this.mSignCreateBottomBar.addView(this.mCertificateItem, TB_Position.Position_CENTER);
    }

    private void addMask() {
        if (this.mMaskView == null) {
            this.mMaskView = this.mViewGroup.findViewById(R.id.sig_create_mask_layout);
            this.mMaskView.setBackgroundColor(this.mContext.getResources().getColor(R.color.ux_color_mask_background));
        }
        this.mPropertyBar.setDismissListener(this.mPropertyBarDismissListener);
        this.mMaskView.setVisibility(0);
    }

    private void preparePropertyBar() {
        int[] colors = new int[PropertyBar.PB_COLORS_SIGN.length];
        System.arraycopy(PropertyBar.PB_COLORS_SIGN, 0, colors, 0, colors.length);
        colors[0] = PropertyBar.PB_COLORS_SIGN[0];
        this.mPropertyBar.setColors(colors);
        this.mPropertyBar.setProperty(1, this.mToolHandler.getColor());
        this.mPropertyBar.setProperty(4, this.mToolHandler.getDiameter());
        this.mPropertyBar.setArrowVisible(true);
        this.mPropertyBar.reset(getSupportedProperties());
        this.mPropertyBar.setPropertyChangeListener(this.propertyChangeListener);
    }

    private long getSupportedProperties() {
        return 5;
    }

    public View getView() {
        return this.mViewGroup;
    }

    public void resetLanguage() {
        if (this.mViewGroup != null && this.mTitleItem != null) {
            this.mTitleItem.setText(AppResource.getString(this.mContext, R.string.rv_sign_create));
        }
    }

    public void init(int width, int height, String dsgPath) {
        this.mSaveDialog = null;
        this.mBmpWidth = width;
        if (this.mDisplay.isPad()) {
            this.mBmpHeight = height - ((int) AppResource.getDimension(this.mContext, R.dimen.ux_toolbar_height_pad));
        } else {
            this.mBmpHeight = height - ((int) AppResource.getDimension(this.mContext, R.dimen.ux_toolbar_height_phone));
        }
        this.mValidRect.set(this.mDisplay.dp2px(3.0f), this.mDisplay.dp2px(7.0f), this.mBmpWidth - this.mDisplay.dp2px(3.0f), this.mBmpHeight - this.mDisplay.dp2px(7.0f));
        this.mKey = null;
        this.mRect.setEmpty();
        this.mSaveItem.setEnable(false);
        if (this.mToolHandler.getColor() == 0) {
            this.mToolHandler.setColor(PropertyBar.PB_COLORS_SIGN[0]);
        }
        this.mProItem.setCentreCircleColor(this.mToolHandler.getColor());
        if (this.mToolHandler.getDiameter() == 0.0f) {
            this.mToolHandler.setDiameter(7.0f);
        }
        if (this.mBitmap == null) {
            try {
                this.mBitmap = Bitmap.createBitmap(this.mBmpWidth, this.mBmpHeight, Config.ARGB_8888);
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
                if (this.mListener != null) {
                    this.mListener.onBackPressed();
                    return;
                }
                return;
            }
        }
        this.mBitmap.eraseColor(-1);
        this.mCanDraw = false;
        initCanvas();
        this.mCurDsgPath = dsgPath;
        setCertificateItem(this.mCurDsgPath);
    }

    public void init(int width, int height, String key, Bitmap bitmap, Rect rect, int color, float diameter, String dsgPath) {
        this.mSaveDialog = null;
        if (bitmap == null || rect == null) {
            init(width, height, dsgPath);
            return;
        }
        this.mBmpWidth = width;
        if (this.mDisplay.isPad()) {
            this.mBmpHeight = height - ((int) AppResource.getDimension(this.mContext, R.dimen.ux_toolbar_height_pad));
        } else {
            this.mBmpHeight = height - ((int) AppResource.getDimension(this.mContext, R.dimen.ux_toolbar_height_phone));
        }
        this.mValidRect.set(this.mDisplay.dp2px(3.0f), this.mDisplay.dp2px(7.0f), this.mBmpWidth - this.mDisplay.dp2px(3.0f), this.mBmpHeight - this.mDisplay.dp2px(7.0f));
        this.mKey = key;
        this.mRect.set(rect);
        this.mSaveItem.setEnable(true);
        if (this.mBitmap != null) {
            if (!this.mBitmap.isRecycled()) {
                this.mBitmap.recycle();
            }
            this.mBitmap = null;
        }
        try {
            this.mBitmap = Bitmap.createBitmap(this.mBmpWidth, this.mBmpHeight, Config.ARGB_8888);
            int[] colors = new int[(this.mBmpWidth * this.mBmpHeight)];
            try {
                bitmap.getPixels(colors, 0, this.mBmpWidth, 0, 0, this.mBmpWidth, this.mBmpHeight);
                this.mBitmap.setPixels(colors, 0, this.mBmpWidth, 0, 0, this.mBmpWidth, this.mBmpHeight);
            } catch (Exception e) {
                int oldVerBmpHeight = height - this.mDisplay.dp2px(80.0f);
                if (oldVerBmpHeight > bitmap.getHeight()) {
                    bitmap.getPixels(colors, 0, this.mBmpWidth, 0, 0, this.mBmpWidth, bitmap.getHeight());
                    this.mBitmap.setPixels(colors, 0, this.mBmpWidth, 0, 0, this.mBmpWidth, bitmap.getHeight());
                } else {
                    bitmap.getPixels(colors, 0, this.mBmpWidth, 0, 0, this.mBmpWidth, oldVerBmpHeight);
                    this.mBitmap.setPixels(colors, 0, this.mBmpWidth, 0, 0, this.mBmpWidth, oldVerBmpHeight);
                }
            }
            bitmap.recycle();
            this.mToolHandler.setColor(color);
            this.mProItem.setCentreCircleColor(color);
            this.mToolHandler.setDiameter(diameter);
            this.mCanDraw = false;
            initCanvas();
            this.mCurDsgPath = dsgPath;
            setCertificateItem(this.mCurDsgPath);
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
            if (this.mListener != null) {
                this.mListener.onBackPressed();
            }
        }
    }

    public void unInit() {
        releaseCanvas();
        if (this.mSaveDialog != null && this.mSaveDialog.isShowing()) {
            try {
                this.mSaveDialog.dismiss();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        this.mSaveDialog = null;
        this.mPropertyBar.setDismissListener(null);
    }

    private void setCertificateItem(String dsgPath) {
        if (this.mDsgUtil != null && this.mCertificateItem != null) {
            if (AppUtil.isEmpty(dsgPath)) {
                this.mCertificateItem.setDisplayStyle(ItemType.Item_Text_Image);
                this.mCertificateItem.setText(R.string.sg_cert_add_text);
                return;
            }
            this.mCertificateItem.setDisplayStyle(ItemType.Item_Text);
            this.mCertificateItem.setText(R.string.sg_cert_current_name_title + new File(dsgPath).getName());
        }
    }

    private void saveSign() {
        Bitmap bitmap = this.mDrawView.getBmp();
        if (this.mKey == null) {
            SignatureDataUtil.insertData(this.mContext, bitmap, this.mRect, this.mToolHandler.getColor(), this.mToolHandler.getDiameter(), this.mCurDsgPath);
        } else {
            SignatureDataUtil.updateByKey(this.mContext, this.mKey, bitmap, this.mRect, this.mToolHandler.getColor(), this.mToolHandler.getDiameter(), this.mCurDsgPath);
        }
        if (this.mListener != null) {
            this.mListener.result(bitmap, this.mRect, this.mToolHandler.getColor(), this.mCurDsgPath);
        }
    }

    private void adjustCanvasRect() {
        if (this.mBitmap != null) {
            if (this.mRect.left < 0) {
                this.mRect.left = 0;
            }
            if (this.mRect.top < 0) {
                this.mRect.top = 0;
            }
            if (this.mRect.right > this.mBmpWidth) {
                this.mRect.right = this.mBmpWidth;
            }
            if (this.mRect.bottom > this.mBmpHeight) {
                this.mRect.bottom = this.mBmpHeight;
            }
        }
    }

    private void initCanvas() {
        if (this.mBitmap != null) {
            this.mDrawEvent = new SignatureDrawEvent(this.mBitmap, 10, this.mToolHandler.getColor(), this.mToolHandler.getDiameter(), null);
            this.mDrawEvent.mType = 10;
            this.mPdfViewCtrl.addTask(new SignaturePSITask(this.mDrawEvent, new Callback() {
                public void result(Event event, boolean success) {
                    SignatureDrawView.this.mHandler.sendEmptyMessage(1);
                }
            }));
        }
    }

    private void setInkColor(int color) {
        if (this.mDrawEvent != null) {
            SignatureDrawEvent drawEvent = new SignatureDrawEvent();
            drawEvent.mType = 11;
            drawEvent.mColor = color;
            this.mPdfViewCtrl.addTask(new SignaturePSITask(drawEvent, new Callback() {
                public void result(Event event, boolean success) {
                    SignatureDrawView.this.mHandler.sendEmptyMessage(4);
                }
            }));
        }
    }

    private void setInkDiameter(float diameter) {
        if (this.mDrawEvent != null) {
            SignatureDrawEvent drawEvent = new SignatureDrawEvent();
            drawEvent.mType = 12;
            drawEvent.mThickness = diameter;
            this.mPdfViewCtrl.addTask(new SignaturePSITask(drawEvent, new Callback() {
                public void result(Event event, boolean success) {
                    SignatureDrawView.this.mHandler.sendEmptyMessage(8);
                }
            }));
        }
    }

    private void clearCanvas() {
        if (this.mDrawEvent != null) {
            this.mDrawEvent.mType = 13;
            this.mBitmap.eraseColor(-1);
            this.mHandler.sendEmptyMessage(2);
        }
    }

    private void addPoint(List<PointF> points, List<Float> pressures, int flag) {
        int i = 0;
        while (i < points.size()) {
            try {
                int i2 = flag;
                SignaturePSITask.mPsi.addPoint((PointF) points.get(i), i2, ((Float) pressures.get(i)).floatValue());
                i++;
            } catch (PDFException e) {
                e.printStackTrace();
                return;
            }
        }
        RectF rect = SignaturePSITask.mPsi.getContentsRect();
        Rect contentRect = new Rect((int) rect.left, (int) rect.top, (int) (((double) rect.right) + 0.5d), (int) (((double) rect.bottom) + 0.5d));
        if (this.mRect.isEmpty()) {
            this.mRect.set(contentRect);
        } else {
            this.mRect.union(contentRect);
        }
        adjustCanvasRect();
        this.mSaveItem.setEnable(true);
        this.mDrawView.invalidate(contentRect);
    }

    private void releaseCanvas() {
        if (this.mDrawEvent != null) {
            this.mDrawEvent.mType = 14;
            this.mHandler.sendEmptyMessage(16);
            this.mDrawEvent = null;
        }
    }
}
