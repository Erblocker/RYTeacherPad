package com.foxit.uiextensions.annots.stamp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.Library;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Stamp;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AnnotActionHandler;
import com.foxit.uiextensions.annots.AnnotContent;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.controls.toolbar.PropertyCircleItem;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;

public class StampToolHandler implements ToolHandler {
    private long itemDynamic = 17179869184L;
    private long itemSignHere = 8589934592L;
    private long itemStandard = 4294967296L;
    private AnnotActionHandler mActionHandler;
    private Context mContext;
    protected DynamicStampIconProvider mDsip;
    private GridView mGridViewForDynamic;
    private GridView mGridViewForForSignHere;
    private GridView mGridViewForStandard;
    private boolean mIsContinuousCreate;
    private int mLastPageIndex = -1;
    private RectF mLastStampRect = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
    private RectF mPageViewThickness = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
    private PDFViewCtrl mPdfViewCtrl;
    private PropertyCircleItem mProItem;
    private PropertyBar mPropertyBar;
    Integer[] mStampIds = new Integer[]{Integer.valueOf(R.drawable._feature_annot_stamp_style0), Integer.valueOf(R.drawable._feature_annot_stamp_style1), Integer.valueOf(R.drawable._feature_annot_stamp_style2), Integer.valueOf(R.drawable._feature_annot_stamp_style3), Integer.valueOf(R.drawable._feature_annot_stamp_style4), Integer.valueOf(R.drawable._feature_annot_stamp_style5), Integer.valueOf(R.drawable._feature_annot_stamp_style6), Integer.valueOf(R.drawable._feature_annot_stamp_style7), Integer.valueOf(R.drawable._feature_annot_stamp_style8), Integer.valueOf(R.drawable._feature_annot_stamp_style9), Integer.valueOf(R.drawable._feature_annot_stamp_style10), Integer.valueOf(R.drawable._feature_annot_stamp_style11), Integer.valueOf(R.drawable._feature_annot_stamp_style12), Integer.valueOf(R.drawable._feature_annot_stamp_style13), Integer.valueOf(R.drawable._feature_annot_stamp_style14), Integer.valueOf(R.drawable._feature_annot_stamp_style15), Integer.valueOf(R.drawable._feature_annot_stamp_style16), Integer.valueOf(R.drawable._feature_annot_stamp_style17), Integer.valueOf(R.drawable._feature_annot_stamp_style18), Integer.valueOf(R.drawable._feature_annot_stamp_style19), Integer.valueOf(R.drawable._feature_annot_stamp_style20), Integer.valueOf(R.drawable._feature_annot_stamp_style21)};
    private RectF mStampRect = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
    private View mStampSelectViewForDynamic;
    private View mStampSelectViewForForSignHere;
    private View mStampSelectViewForStandard;
    private int mStampType = 0;
    private boolean mTouchCaptured = false;

    class IconView extends View {
        private RectF mIconRectF;
        private Paint mPaint = new Paint();
        private int selectRect;

        public IconView(Context context) {
            super(context);
            this.mPaint.setColor(this.selectRect);
            this.mPaint.setStyle(Style.STROKE);
            this.mPaint.setStrokeWidth(5.0f);
            this.selectRect = Color.parseColor("#00000000");
            this.mIconRectF = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
        }

        public IconView(Context context, int type) {
            super(context);
            this.mPaint.setColor(this.selectRect);
            this.mPaint.setStyle(Style.STROKE);
            this.mPaint.setStrokeWidth(5.0f);
            this.selectRect = Color.parseColor("#179CD8");
            this.mIconRectF = new RectF(0.0f, 0.0f, 300.0f, 90.0f);
        }

        protected void onDraw(Canvas canvas) {
            canvas.save();
            canvas.drawRoundRect(this.mIconRectF, 6.0f, 6.0f, this.mPaint);
            canvas.restore();
        }
    }

    public StampToolHandler(Context context, PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mContext = context;
        initAnnotIconProvider();
    }

    protected void initAnnotIconProvider() {
        if (this.mDsip == null) {
            this.mDsip = new DynamicStampIconProvider();
        }
        try {
            Library.setAnnotIconProvider(this.mDsip);
            this.mActionHandler = (AnnotActionHandler) DocumentManager.getInstance(this.mPdfViewCtrl).getActionHandler();
            if (this.mActionHandler == null) {
                this.mActionHandler = new AnnotActionHandler(this.mPdfViewCtrl);
            }
            DocumentManager.getInstance(this.mPdfViewCtrl).setActionHandler(this.mActionHandler);
        } catch (PDFException e) {
        }
    }

    public void setPropertyBar(PropertyBar propertyBar) {
        this.mPropertyBar = propertyBar;
    }

    public PropertyBar getPropertyBar() {
        return this.mPropertyBar;
    }

    public void initDisplayItems(PropertyBar propertyBar, PropertyCircleItem propertyItem) {
        int gvHeight;
        this.mStampSelectViewForStandard = View.inflate(this.mContext, R.layout._future_rd_annot_stamp_gridview, null);
        this.mStampSelectViewForForSignHere = View.inflate(this.mContext, R.layout._future_rd_annot_stamp_gridview, null);
        this.mStampSelectViewForDynamic = View.inflate(this.mContext, R.layout._future_rd_annot_stamp_gridview, null);
        int t = AppDisplay.getInstance(this.mContext).dp2px(16.0f);
        this.mStampSelectViewForStandard.setPadding(0, t, 0, 0);
        this.mStampSelectViewForForSignHere.setPadding(0, t, 0, 0);
        this.mStampSelectViewForDynamic.setPadding(0, t, 0, 0);
        if (AppDisplay.getInstance(this.mContext).isPad()) {
            gvHeight = AppDisplay.getInstance(this.mContext).dp2px(300.0f);
        } else {
            gvHeight = -1;
        }
        LayoutParams gridViewParams = new LayoutParams(-1, gvHeight);
        this.mGridViewForStandard = (GridView) this.mStampSelectViewForStandard.findViewById(R.id.rd_annot_item_stamp_gridview);
        this.mGridViewForStandard.setLayoutParams(gridViewParams);
        this.mGridViewForForSignHere = (GridView) this.mStampSelectViewForForSignHere.findViewById(R.id.rd_annot_item_stamp_gridview);
        this.mGridViewForForSignHere.setLayoutParams(gridViewParams);
        this.mGridViewForDynamic = (GridView) this.mStampSelectViewForDynamic.findViewById(R.id.rd_annot_item_stamp_gridview);
        this.mGridViewForDynamic.setLayoutParams(gridViewParams);
        final BaseAdapter adapterForStandard = new BaseAdapter() {
            public int getCount() {
                return 12;
            }

            public Object getItem(int position) {
                return Integer.valueOf(position);
            }

            public long getItemId(int position) {
                return (long) position;
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                RelativeLayout relativeLayout = new RelativeLayout(StampToolHandler.this.mContext);
                relativeLayout.setLayoutParams(new AbsListView.LayoutParams(AppDisplay.getInstance(StampToolHandler.this.mContext).dp2px(150.0f), AppDisplay.getInstance(StampToolHandler.this.mContext).dp2px(50.0f)));
                relativeLayout.setGravity(17);
                IconView iconView = new IconView(StampToolHandler.this.mContext);
                RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(-1, -1);
                iconParams.setMargins(AppDisplay.getInstance(StampToolHandler.this.mContext).dp2px(7.0f), AppDisplay.getInstance(StampToolHandler.this.mContext).dp2px(7.0f), AppDisplay.getInstance(StampToolHandler.this.mContext).dp2px(7.0f), AppDisplay.getInstance(StampToolHandler.this.mContext).dp2px(7.0f));
                iconView.setLayoutParams(iconParams);
                iconView.setBackgroundResource(StampToolHandler.this.mStampIds[position].intValue());
                if (position == StampToolHandler.this.mStampType) {
                    relativeLayout.setBackgroundResource(R.drawable._feature_annot_stamp_selectrect);
                } else {
                    relativeLayout.setBackgroundResource(0);
                }
                relativeLayout.addView(iconView);
                return relativeLayout;
            }
        };
        final BaseAdapter adapterForSignHere = new BaseAdapter() {
            public int getCount() {
                return 5;
            }

            public Object getItem(int position) {
                return Integer.valueOf(position);
            }

            public long getItemId(int position) {
                return (long) position;
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                RelativeLayout relativeLayout = new RelativeLayout(StampToolHandler.this.mContext);
                relativeLayout.setLayoutParams(new AbsListView.LayoutParams(AppDisplay.getInstance(StampToolHandler.this.mContext).dp2px(150.0f), AppDisplay.getInstance(StampToolHandler.this.mContext).dp2px(50.0f)));
                relativeLayout.setGravity(17);
                IconView iconView = new IconView(StampToolHandler.this.mContext);
                RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(-1, -1);
                iconParams.setMargins(AppDisplay.getInstance(StampToolHandler.this.mContext).dp2px(7.0f), AppDisplay.getInstance(StampToolHandler.this.mContext).dp2px(7.0f), AppDisplay.getInstance(StampToolHandler.this.mContext).dp2px(7.0f), AppDisplay.getInstance(StampToolHandler.this.mContext).dp2px(7.0f));
                iconView.setLayoutParams(iconParams);
                iconView.setBackgroundResource(StampToolHandler.this.mStampIds[position + 12].intValue());
                if (position + 12 == StampToolHandler.this.mStampType) {
                    relativeLayout.setBackgroundResource(R.drawable._feature_annot_stamp_selectrect);
                } else {
                    relativeLayout.setBackgroundResource(0);
                }
                relativeLayout.addView(iconView);
                return relativeLayout;
            }
        };
        final BaseAdapter adapterForDynamic = new BaseAdapter() {
            public int getCount() {
                return 5;
            }

            public Object getItem(int position) {
                return Integer.valueOf(position);
            }

            public long getItemId(int position) {
                return (long) position;
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                RelativeLayout relativeLayout = new RelativeLayout(StampToolHandler.this.mContext);
                relativeLayout.setLayoutParams(new AbsListView.LayoutParams(AppDisplay.getInstance(StampToolHandler.this.mContext).dp2px(150.0f), AppDisplay.getInstance(StampToolHandler.this.mContext).dp2px(50.0f)));
                relativeLayout.setGravity(17);
                IconView iconView = new IconView(StampToolHandler.this.mContext);
                RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(-1, -1);
                iconParams.setMargins(AppDisplay.getInstance(StampToolHandler.this.mContext).dp2px(7.0f), AppDisplay.getInstance(StampToolHandler.this.mContext).dp2px(7.0f), AppDisplay.getInstance(StampToolHandler.this.mContext).dp2px(7.0f), AppDisplay.getInstance(StampToolHandler.this.mContext).dp2px(7.0f));
                iconView.setLayoutParams(iconParams);
                iconView.setBackgroundResource(StampToolHandler.this.mStampIds[position + 17].intValue());
                if (position + 17 == StampToolHandler.this.mStampType) {
                    relativeLayout.setBackgroundResource(R.drawable._feature_annot_stamp_selectrect);
                } else {
                    relativeLayout.setBackgroundResource(0);
                }
                relativeLayout.addView(iconView);
                return relativeLayout;
            }
        };
        this.mGridViewForStandard.setAdapter(adapterForStandard);
        final PropertyBar propertyBar2 = propertyBar;
        this.mGridViewForStandard.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                StampToolHandler.this.mStampType = position;
                adapterForStandard.notifyDataSetChanged();
                adapterForSignHere.notifyDataSetChanged();
                adapterForDynamic.notifyDataSetChanged();
                if (propertyBar2 != null) {
                    propertyBar2.dismiss();
                }
            }
        });
        this.mGridViewForForSignHere.setAdapter(adapterForSignHere);
        propertyBar2 = propertyBar;
        this.mGridViewForForSignHere.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                StampToolHandler.this.mStampType = position + 12;
                adapterForStandard.notifyDataSetChanged();
                adapterForSignHere.notifyDataSetChanged();
                adapterForDynamic.notifyDataSetChanged();
                if (propertyBar2 != null) {
                    propertyBar2.dismiss();
                }
            }
        });
        this.mGridViewForDynamic.setAdapter(adapterForDynamic);
        propertyBar2 = propertyBar;
        this.mGridViewForDynamic.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                StampToolHandler.this.mStampType = position + 17;
                adapterForStandard.notifyDataSetChanged();
                adapterForSignHere.notifyDataSetChanged();
                adapterForDynamic.notifyDataSetChanged();
                if (propertyBar2 != null) {
                    propertyBar2.dismiss();
                }
            }
        });
        resetPropertyBar(propertyBar);
        if (propertyItem != null) {
            this.mProItem = propertyItem;
            Rect rect = new Rect();
            this.mProItem.getContentView().getGlobalVisibleRect(rect);
            this.mPropertyBar.show(new RectF(rect), true);
        }
    }

    public void resetPropertyBar(PropertyBar propertyBar) {
        if (propertyBar != null) {
            this.mPropertyBar = propertyBar;
            this.mPropertyBar.setArrowVisible(true);
            this.mPropertyBar.setPhoneFullScreen(true);
            this.mPropertyBar.reset(0);
            this.mPropertyBar.setTopTitleVisible(true);
            this.mPropertyBar.addTab("Standard Stamps", R.drawable._feature_annot_stamp_standardstamps_selector, "", 0);
            this.mPropertyBar.addCustomItem(this.itemStandard, this.mStampSelectViewForStandard, 0, 0);
            this.mPropertyBar.addTab("Sign Here", R.drawable._feature_annot_stamp_signherestamps_selector, "", 1);
            this.mPropertyBar.addCustomItem(this.itemSignHere, this.mStampSelectViewForForSignHere, 1, 0);
            this.mPropertyBar.addTab("Dynamic Stamps", R.drawable._feature_annot_stamp_dynamicstamps_selector, "", 2);
            this.mPropertyBar.addCustomItem(this.itemDynamic, this.mStampSelectViewForDynamic, 2, 0);
            if (this.mStampType >= 0 && this.mStampType <= 11) {
                this.mPropertyBar.setCurrentTab(0);
            } else if (this.mStampType >= 12 && this.mStampType <= 16) {
                this.mPropertyBar.setCurrentTab(1);
            } else if (this.mStampType >= 17 && this.mStampType <= 21) {
                this.mPropertyBar.setCurrentTab(2);
            }
        }
    }

    private String getSubject(int mStampType) {
        if (mStampType == 0) {
            return Stamp.STANDARDICONNAME_APPROVED;
        }
        if (mStampType == 1) {
            return "Completed";
        }
        if (mStampType == 2) {
            return Stamp.STANDARDICONNAME_CODFIDENTIAL;
        }
        if (mStampType == 3) {
            return Stamp.STANDARDICONNAME_DRAFT;
        }
        if (mStampType == 4) {
            return "Emergency";
        }
        if (mStampType == 5) {
            return Stamp.STANDARDICONNAME_EXPIRED;
        }
        if (mStampType == 6) {
            return Stamp.STANDARDICONNAME_FINAL;
        }
        if (mStampType == 7) {
            return "Received";
        }
        if (mStampType == 8) {
            return "Reviewed";
        }
        if (mStampType == 9) {
            return "Revised";
        }
        if (mStampType == 10) {
            return "Verified";
        }
        if (mStampType == 11) {
            return "Void";
        }
        if (mStampType == 12) {
            return "Accepted";
        }
        if (mStampType == 13) {
            return "Initial";
        }
        if (mStampType == 14) {
            return "Rejected";
        }
        if (mStampType == 15) {
            return "Sign Here";
        }
        if (mStampType == 16) {
            return "Witness";
        }
        if (mStampType == 17) {
            return "DynaApproved";
        }
        if (mStampType == 18) {
            return "DynaConfidential";
        }
        if (mStampType == 19) {
            return "DynaReceived";
        }
        if (mStampType == 20) {
            return "DynaReviewed";
        }
        if (mStampType == 21) {
            return "DynaRevised";
        }
        return null;
    }

    public String getType() {
        return ToolHandler.TH_TYPE_STAMP;
    }

    public void onActivate() {
    }

    public void onDeactivate() {
    }

    private RectF getStampRectOnPageView(PointF point, int pageIndex) {
        PointF pageViewPt = new PointF(point.x, point.y);
        float offsetX = thicknessOnPageView(pageIndex, 49.5f);
        float offsetY = thicknessOnPageView(pageIndex, 15.5f);
        RectF pageViewRect = new RectF(pageViewPt.x - offsetX, pageViewPt.y - offsetY, pageViewPt.x + offsetX, pageViewPt.y + offsetY);
        if (pageViewRect.left < 0.0f) {
            pageViewRect.offset(-pageViewRect.left, 0.0f);
        }
        if (pageViewRect.right > ((float) this.mPdfViewCtrl.getPageViewWidth(pageIndex))) {
            pageViewRect.offset(((float) this.mPdfViewCtrl.getPageViewWidth(pageIndex)) - pageViewRect.right, 0.0f);
        }
        if (pageViewRect.top < 0.0f) {
            pageViewRect.offset(0.0f, -pageViewRect.top);
        }
        if (pageViewRect.bottom > ((float) this.mPdfViewCtrl.getPageViewHeight(pageIndex))) {
            pageViewRect.offset(0.0f, ((float) this.mPdfViewCtrl.getPageViewHeight(pageIndex)) - pageViewRect.bottom);
        }
        return pageViewRect;
    }

    public boolean onTouchEvent(int pageIndex, MotionEvent e) {
        PointF point = new PointF(e.getX(), e.getY());
        this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(point, point, pageIndex);
        int action = e.getAction();
        this.mStampRect = getStampRectOnPageView(point, pageIndex);
        switch (action) {
            case 0:
                if ((!this.mTouchCaptured && this.mLastPageIndex == -1) || this.mLastPageIndex == pageIndex) {
                    this.mTouchCaptured = true;
                    this.mLastStampRect = new RectF(this.mStampRect);
                    if (this.mLastPageIndex == -1) {
                        this.mLastPageIndex = pageIndex;
                        break;
                    }
                }
                break;
            case 1:
            case 3:
                if (!this.mIsContinuousCreate) {
                    ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
                }
                RectF pdfRect = new RectF();
                this.mPdfViewCtrl.convertPageViewRectToPdfRect(this.mStampRect, pdfRect, pageIndex);
                createAnnot(pdfRect, pageIndex);
                break;
            case 2:
                break;
        }
        if (this.mTouchCaptured && this.mLastPageIndex == pageIndex) {
            RectF rect = new RectF(this.mLastStampRect);
            rect.union(this.mStampRect);
            rect.inset(-10.0f, -10.0f);
            this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rect, rect, pageIndex);
            this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(rect));
            this.mLastStampRect = new RectF(this.mStampRect);
        }
        return true;
    }

    public boolean onLongPress(int pageIndex, MotionEvent motionEvent) {
        return false;
    }

    public boolean onSingleTapConfirmed(int pageIndex, MotionEvent motionEvent) {
        return false;
    }

    public void onDraw(int pageIndex, Canvas canvas) {
        if (this.mTouchCaptured && pageIndex == this.mLastPageIndex) {
            Paint paint = new Paint();
            paint.setAlpha(100);
            Bitmap bitmap = BitmapFactory.decodeResource(this.mContext.getResources(), this.mStampIds[this.mStampType].intValue());
            if (bitmap != null && this.mStampRect != null) {
                canvas.drawBitmap(bitmap, null, this.mStampRect, paint);
            }
        }
    }

    private float thicknessOnPageView(int pageIndex, float thickness) {
        this.mPageViewThickness.set(0.0f, 0.0f, thickness, thickness);
        this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mPageViewThickness, this.mPageViewThickness, pageIndex);
        return Math.abs(this.mPageViewThickness.width());
    }

    private void createAnnot(RectF rectF, int pageIndex) {
        if (this.mPdfViewCtrl.isPageVisible(pageIndex)) {
            try {
                final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(pageIndex);
                final Stamp annot = (Stamp) page.addAnnot(13, rectF);
                final StampAddUndoItem undoItem = new StampAddUndoItem(this.mPdfViewCtrl);
                undoItem.mPageIndex = pageIndex;
                undoItem.mStampType = this.mStampType;
                undoItem.mDsip = this.mDsip;
                undoItem.mNM = AppDmUtil.randomUUID(null);
                undoItem.mAuthor = AppDmUtil.getAnnotAuthor();
                undoItem.mFlags = 4;
                undoItem.mSubject = getSubject(this.mStampType);
                undoItem.mIconName = undoItem.mSubject;
                undoItem.mCreationDate = AppDmUtil.currentDateToDocumentDate();
                undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
                undoItem.mBBox = new RectF(rectF);
                if (this.mStampType <= 17) {
                    undoItem.mBitmap = BitmapFactory.decodeResource(this.mContext.getResources(), this.mStampIds[this.mStampType].intValue());
                }
                final int i = pageIndex;
                this.mPdfViewCtrl.addTask(new EditAnnotTask(new StampEvent(1, undoItem, annot, this.mPdfViewCtrl), new Callback() {
                    public void result(Event event, boolean success) {
                        if (success) {
                            DocumentManager.getInstance(StampToolHandler.this.mPdfViewCtrl).onAnnotAdded(page, annot);
                            DocumentManager.getInstance(StampToolHandler.this.mPdfViewCtrl).addUndoItem(undoItem);
                            if (StampToolHandler.this.mPdfViewCtrl.isPageVisible(i)) {
                                try {
                                    RectF viewRect = annot.getRect();
                                    StampToolHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(viewRect, viewRect, i);
                                    Rect rect = new Rect();
                                    viewRect.roundOut(rect);
                                    viewRect.union(StampToolHandler.this.mLastStampRect);
                                    rect.inset(-10, -10);
                                    StampToolHandler.this.mPdfViewCtrl.refresh(i, rect);
                                    StampToolHandler.this.mLastStampRect.setEmpty();
                                } catch (PDFException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        StampToolHandler.this.mTouchCaptured = false;
                        StampToolHandler.this.mLastPageIndex = -1;
                    }
                }));
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean getIsContinuousCreate() {
        return this.mIsContinuousCreate;
    }

    public void setIsContinuousCreate(boolean isContinuousCreate) {
        this.mIsContinuousCreate = isContinuousCreate;
    }

    public void addAnnot(int pageIndex, AnnotContent content, boolean addUndo, Callback result) {
        if (this.mPdfViewCtrl.isPageVisible(pageIndex)) {
            RectF bboxRect = content.getBBox();
            try {
                PDFPage page = this.mPdfViewCtrl.getDoc().getPage(pageIndex);
                Stamp annot = (Stamp) page.addAnnot(13, bboxRect);
                annot.setUniqueID(content.getNM());
                annot.setCreationDateTime(AppDmUtil.currentDateToDocumentDate());
                annot.setModifiedDateTime(content.getModifiedDate());
                annot.setFlags(4);
                annot.resetAppearanceStream();
                DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotAdded(page, annot);
                if (this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                    RectF viewRect = annot.getRect();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(viewRect, viewRect, pageIndex);
                    Rect rect = new Rect();
                    viewRect.roundOut(rect);
                    rect.inset(-10, -10);
                    this.mPdfViewCtrl.refresh(pageIndex, rect);
                    if (result != null) {
                        result.result(null, true);
                    }
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }
}
