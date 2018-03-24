package com.foxit.uiextensions.controls.propertybar.imp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextUtils.TruncateAt;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.controls.propertybar.PropertyBar.DismissListener;
import com.foxit.uiextensions.controls.propertybar.PropertyBar.PropertyChangeListener;
import com.foxit.uiextensions.controls.propertybar.PropertyBar.UpdateViewListener;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.ToolUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyBarImpl extends PopupWindow implements PropertyBar {
    private AppDisplay display;
    private boolean mArrowVisible;
    private int mColor;
    private int[] mColorDotPics;
    private ViewPager mColorViewPager;
    private int[] mColors;
    private LinearLayout mColorsPickerRoot;
    private Context mContext;
    private int mCurrentColorIndex;
    private int mCurrentRotation;
    private int mCurrentTab;
    private int mCurrentWidth;
    private List<Map<String, Object>> mCustomItemList;
    private long mCustomProperty;
    private List<Map<String, Object>> mCustomTabList;
    private DismissListener mDismissListener;
    private FontAdapter mFontAdapter;
    private boolean[] mFontChecked;
    private String[] mFontNames;
    private FontSizeAdapter mFontSizeAdapter;
    private boolean[] mFontSizeChecked;
    private float[] mFontSizes;
    private String mFontname;
    private float mFontsize;
    private ImageView mIvArrowBottom;
    private ImageView mIvArrowLeft;
    private ImageView mIvArrowRight;
    private ImageView mIvArrowTop;
    private ImageView mIv_title_shadow;
    private int mLinestyle;
    private int[] mLinestyles;
    private float mLinewith;
    private LinearLayout mLlArrowBottom;
    private LinearLayout mLlArrowLeft;
    private LinearLayout mLlArrowRight;
    private LinearLayout mLlArrowTop;
    private LinearLayout mLlColorDots;
    private LinearLayout mLl_PropertyBar;
    private LinearLayout mLl_root;
    private LinearLayout mLl_tabContents;
    private LinearLayout mLl_title_checked;
    private LinearLayout mLl_titles;
    private LinearLayout mLl_topTabs;
    private int mNoteIconType;
    private int mOpacity;
    private int[] mOpacityIds;
    private int[] mOpacityIdsChecked;
    private int[] mOpacitys;
    private boolean mOrientationed;
    private LinearLayout mPBLlColors;
    private int mPadWidth;
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private LinearLayout mPopupView;
    private PropertyChangeListener mPropertyChangeListener;
    private RectF mRectF;
    private EditText mScaleEdt;
    private int mScalePercent;
    private int mScaleSwitch;
    private final int mShowFont;
    private final int mShowFontSize;
    private boolean mShowMask;
    private long mSupportProperty;
    private String[] mSupportTabNames;
    private List<String> mTabs;
    private LinearLayout mTopShadow;
    private TextView mTopTitle;
    private LinearLayout mTopTitleLayout;
    private TypeAdapter mTypeAdapter;
    private String[] mTypeNames;
    private int[] mTypePicIds;
    private float offset;

    public PropertyBarImpl(Context context, PDFViewCtrl pdfViewer, ViewGroup parent) {
        this(context, null, pdfViewer, parent);
    }

    public PropertyBarImpl(Context context, AttributeSet attrs, PDFViewCtrl pdfViewer, ViewGroup parent) {
        this(context, attrs, 0, pdfViewer, parent);
    }

    public PropertyBarImpl(Context context, AttributeSet attrs, int defStyleAttr, PDFViewCtrl pdfViewCtrl, ViewGroup parent) {
        super(context, attrs, defStyleAttr);
        this.mArrowVisible = false;
        this.mSupportProperty = 0;
        this.mCustomProperty = 0;
        this.mCurrentTab = 0;
        this.mOpacitys = PB_OPACITYS;
        this.mOpacity = this.mOpacitys[this.mOpacitys.length - 1];
        this.mFontname = "Courier";
        this.mFontsize = 24.0f;
        this.mLinewith = 6.0f;
        this.mLinestyles = new int[]{1, 2, 3, 4, 5};
        this.mLinestyle = this.mLinestyles[0];
        this.mOpacityIds = new int[]{R.drawable.pb_opacity25, R.drawable.pb_opacity50, R.drawable.pb_opacity75, R.drawable.pb_opacity100};
        this.mOpacityIdsChecked = new int[]{R.drawable.pb_opacity25_pressed, R.drawable.pb_opacity50_pressed, R.drawable.pb_opacity75_pressed, R.drawable.pb_opacity100_pressed};
        this.mTypePicIds = new int[]{R.drawable.pb_note_type_comment, R.drawable.pb_note_type_key, R.drawable.pb_note_type_note, R.drawable.pb_note_type_help, R.drawable.pb_note_type_new_paragraph, R.drawable.pb_note_type_paragraph, R.drawable.pb_note_type_insert};
        this.mNoteIconType = 1;
        this.mColorDotPics = new int[]{R.drawable.pb_ll_colors_dot_selected, R.drawable.pb_ll_colors_dot};
        this.mCurrentColorIndex = 0;
        this.mFontNames = PB_FONTNAMES;
        this.mFontSizes = PB_FONTSIZES;
        this.mShowFontSize = 2;
        this.mShowFont = 1;
        this.mScalePercent = 20;
        this.mScaleSwitch = 0;
        this.mCurrentWidth = 0;
        this.mShowMask = false;
        this.offset = 0.0f;
        this.mOrientationed = false;
        this.mPdfViewCtrl = null;
        this.mContext = context;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mParent = parent;
        this.display = AppDisplay.getInstance(context);
        this.mCurrentRotation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
        initVariable();
        initView();
        if (this.display.isPad()) {
            setWidth(this.mPadWidth);
        } else {
            setWidth(-1);
        }
        setHeight(-2);
        setContentView(this.mPopupView);
        setTouchable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(0));
        setSoftInputMode(1);
        setSoftInputMode(48);
        if (!this.display.isPad()) {
            setAnimationStyle(R.style.PB_PopupAnimation);
        }
        setOnDismissListener(new OnDismissListener() {
            public void onDismiss() {
                if (PropertyBarImpl.this.mDismissListener != null) {
                    PropertyBarImpl.this.mDismissListener.onDismiss();
                }
                if (PropertyBarImpl.this.mShowMask) {
                    PropertyBarImpl.this.mShowMask = false;
                }
                if (!PropertyBarImpl.this.display.isPad()) {
                    PropertyBarImpl.this.setPhoneFullScreen(false);
                }
                UIExtensionsManager uiExtensionsManager = (UIExtensionsManager) PropertyBarImpl.this.mPdfViewCtrl.getUIExtensionsManager();
                AnnotHandler currentAnnotHandler = ToolUtil.getCurrentAnnotHandler(uiExtensionsManager);
                if (!PropertyBarImpl.this.display.isPad() && currentAnnotHandler != null && uiExtensionsManager.getCurrentToolHandler() == null && PropertyBarImpl.this.offset > 0.0f) {
                    PropertyBarImpl.this.mPdfViewCtrl.layout(0, 0, PropertyBarImpl.this.mPdfViewCtrl.getWidth(), PropertyBarImpl.this.mPdfViewCtrl.getHeight());
                    PropertyBarImpl.this.offset = 0.0f;
                }
                if (DocumentManager.getInstance(PropertyBarImpl.this.mPdfViewCtrl).getCurrentAnnot() != null) {
                    DocumentManager.getInstance(PropertyBarImpl.this.mPdfViewCtrl).setCurrentAnnot(null);
                }
            }
        });
    }

    private void initVariable() {
        int i;
        this.mPadWidth = this.display.dp2px(320.0f);
        int[] colors = new int[PropertyBar.PB_COLORS_TEXT.length];
        System.arraycopy(PropertyBar.PB_COLORS_TEXT, 0, colors, 0, colors.length);
        colors[0] = PropertyBar.PB_COLORS_TEXT[0];
        this.mColors = colors;
        this.mColor = this.mColors[0];
        this.mRectF = new RectF();
        this.mSupportTabNames = new String[]{this.mContext.getResources().getString(R.string.pb_type_tab), this.mContext.getResources().getString(R.string.pb_fill_tab), this.mContext.getResources().getString(R.string.fx_string_border), this.mContext.getResources().getString(R.string.fx_string_fontname), "Watermark"};
        this.mTabs = new ArrayList();
        this.mCustomTabList = new ArrayList();
        this.mCustomItemList = new ArrayList();
        this.mFontChecked = new boolean[this.mFontNames.length];
        for (i = 0; i < this.mFontNames.length; i++) {
            if (i == 0) {
                this.mFontChecked[i] = true;
            } else {
                this.mFontChecked[i] = false;
            }
        }
        this.mFontSizeChecked = new boolean[this.mFontSizes.length];
        for (i = 0; i < this.mFontSizes.length; i++) {
            if (i == 0) {
                this.mFontSizeChecked[i] = true;
            } else {
                this.mFontSizeChecked[i] = false;
            }
        }
        this.mTypeNames = new String[]{this.mContext.getResources().getString(R.string.annot_text_comment), this.mContext.getResources().getString(R.string.annot_text_key), this.mContext.getResources().getString(R.string.annot_text_note), this.mContext.getResources().getString(R.string.annot_text_help), this.mContext.getResources().getString(R.string.annot_text_newparagraph), this.mContext.getResources().getString(R.string.annot_text_paragraph), this.mContext.getResources().getString(R.string.annot_text_insert)};
    }

    private void initView() {
        this.mPopupView = new LinearLayout(this.mContext);
        this.mPopupView.setOrientation(1);
        this.mLl_root = new LinearLayout(this.mContext);
        this.mLl_root.setLayoutParams(new LayoutParams(-1, -2));
        this.mLl_root.setOrientation(1);
        this.mPopupView.addView(this.mLl_root);
        if (!this.display.isPad()) {
            this.mTopShadow = new LinearLayout(this.mContext);
            this.mTopShadow.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
            this.mTopShadow.setOrientation(1);
            this.mLl_root.addView(this.mTopShadow);
            ImageView shadow = new ImageView(this.mContext);
            shadow.setLayoutParams(new ViewGroup.LayoutParams(-1, (int) this.mContext.getResources().getDimension(R.dimen.ux_shadow_height)));
            shadow.setImageResource(R.drawable.search_shadow_bg270);
            this.mTopShadow.addView(shadow);
            ImageView shadowLine = new ImageView(this.mContext);
            shadowLine.setLayoutParams(new ViewGroup.LayoutParams(-1, 1));
            shadowLine.setImageResource(R.color.ux_color_shadow_solid_line);
            this.mTopShadow.addView(shadowLine);
        }
        this.mLlArrowTop = new LinearLayout(this.mContext);
        this.mLlArrowTop.setLayoutParams(new LayoutParams(-1, -2));
        this.mLlArrowTop.setOrientation(1);
        this.mLl_root.addView(this.mLlArrowTop);
        this.mIvArrowTop = new ImageView(this.mContext);
        this.mIvArrowTop.setLayoutParams(new LayoutParams(-2, -2));
        this.mIvArrowTop.setImageResource(R.drawable.pb_arrow_top);
        this.mLlArrowTop.addView(this.mIvArrowTop);
        this.mLlArrowTop.setVisibility(8);
        LinearLayout mLlArrowCenter = new LinearLayout(this.mContext);
        mLlArrowCenter.setLayoutParams(new LayoutParams(-1, -2, 1.0f));
        mLlArrowCenter.setOrientation(0);
        this.mLl_root.addView(mLlArrowCenter);
        this.mLlArrowLeft = new LinearLayout(this.mContext);
        this.mLlArrowLeft.setLayoutParams(new LayoutParams(-2, -1));
        this.mLlArrowLeft.setOrientation(1);
        mLlArrowCenter.addView(this.mLlArrowLeft);
        this.mIvArrowLeft = new ImageView(this.mContext);
        this.mIvArrowLeft.setLayoutParams(new LayoutParams(-2, -2));
        this.mIvArrowLeft.setImageResource(R.drawable.pb_arrow_left);
        this.mLlArrowLeft.addView(this.mIvArrowLeft);
        this.mLlArrowLeft.setVisibility(8);
        this.mLl_PropertyBar = new LinearLayout(this.mContext);
        this.mLl_PropertyBar.setLayoutParams(new LayoutParams(0, -1, 1.0f));
        this.mLl_PropertyBar.setOrientation(1);
        if (this.display.isPad()) {
            this.mLl_PropertyBar.setBackgroundResource(R.drawable.pb_popup_bg_shadow);
            this.mLl_PropertyBar.setPadding(this.display.dp2px(4.0f), this.display.dp2px(4.0f), this.display.dp2px(4.0f), this.display.dp2px(4.0f));
        } else {
            this.mLl_PropertyBar.setBackgroundColor(this.mContext.getResources().getColor(R.color.ux_text_color_title_light));
        }
        mLlArrowCenter.addView(this.mLl_PropertyBar);
        this.mLlArrowRight = new LinearLayout(this.mContext);
        this.mLlArrowRight.setLayoutParams(new LayoutParams(-2, -1));
        this.mLlArrowRight.setOrientation(1);
        mLlArrowCenter.addView(this.mLlArrowRight);
        this.mIvArrowRight = new ImageView(this.mContext);
        this.mIvArrowRight.setLayoutParams(new LayoutParams(-2, -2));
        this.mIvArrowRight.setImageResource(R.drawable.pb_arrow_right);
        this.mLlArrowRight.addView(this.mIvArrowRight);
        this.mLlArrowRight.setVisibility(8);
        this.mLlArrowBottom = new LinearLayout(this.mContext);
        this.mLlArrowBottom.setLayoutParams(new LayoutParams(-1, -2));
        this.mLlArrowBottom.setOrientation(1);
        this.mLl_root.addView(this.mLlArrowBottom);
        this.mIvArrowBottom = new ImageView(this.mContext);
        this.mIvArrowBottom.setLayoutParams(new LayoutParams(-2, -2));
        this.mIvArrowBottom.setImageResource(R.drawable.pb_arrow_bottom);
        this.mLlArrowBottom.addView(this.mIvArrowBottom);
        this.mLlArrowBottom.setVisibility(8);
        addPbAll();
    }

    private void addPbAll() {
        View view = LayoutInflater.from(this.mContext).inflate(R.layout.pb_rl_propertybar, null, false);
        this.mLl_topTabs = (LinearLayout) view.findViewById(R.id.pb_ll_top);
        if (this.display.isPad()) {
            this.mLl_topTabs.setBackgroundResource(R.drawable.pb_tabs_bg);
        } else {
            this.mLl_topTabs.setBackgroundResource(R.color.ux_text_color_subhead_colour);
        }
        this.mTopTitleLayout = (LinearLayout) view.findViewById(R.id.pb_topTitle_ll);
        this.mTopTitleLayout.setVisibility(8);
        this.mTopTitleLayout.setTag(Integer.valueOf(0));
        if (this.display.isPad()) {
            this.mTopTitle = new TextView(this.mContext);
            this.mTopTitle.setLayoutParams(new LayoutParams(-1, -2));
            this.mTopTitle.setTextSize(0, this.mContext.getResources().getDimension(R.dimen.ux_text_height_title));
            this.mTopTitle.setTextColor(this.mContext.getResources().getColor(R.color.ux_text_color_title_light));
            this.mTopTitle.setTypeface(Typeface.DEFAULT);
            this.mTopTitle.setGravity(17);
            this.mTopTitle.setSingleLine(true);
            this.mTopTitle.setEllipsize(TruncateAt.END);
            this.mTopTitleLayout.addView(this.mTopTitle);
        } else {
            RelativeLayout relativeLayout = new RelativeLayout(this.mContext);
            relativeLayout.setLayoutParams(new LayoutParams(-1, -2));
            relativeLayout.setGravity(16);
            this.mTopTitleLayout.addView(relativeLayout);
            this.mTopTitle = new TextView(this.mContext);
            RelativeLayout.LayoutParams titleLayoutParams = new RelativeLayout.LayoutParams(-1, -2);
            titleLayoutParams.addRule(15);
            titleLayoutParams.addRule(9);
            titleLayoutParams.leftMargin = this.display.dp2px(70.0f);
            this.mTopTitle.setLayoutParams(titleLayoutParams);
            this.mTopTitle.setSingleLine(true);
            this.mTopTitle.setEllipsize(TruncateAt.END);
            this.mTopTitle.setTextSize(0, this.mContext.getResources().getDimension(R.dimen.ux_text_height_title));
            this.mTopTitle.setTextColor(this.mContext.getResources().getColor(R.color.ux_text_color_title_light));
            this.mTopTitle.setTypeface(Typeface.DEFAULT);
            this.mTopTitle.setGravity(16);
            this.mTopTitle.setSingleLine(true);
            this.mTopTitle.setEllipsize(TruncateAt.END);
            relativeLayout.addView(this.mTopTitle);
            ImageView img = new ImageView(this.mContext);
            RelativeLayout.LayoutParams imgLayoutParams = new RelativeLayout.LayoutParams(-2, -2);
            imgLayoutParams.addRule(15);
            imgLayoutParams.addRule(9);
            imgLayoutParams.leftMargin = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_left_margin_phone);
            img.setLayoutParams(imgLayoutParams);
            img.setImageResource(R.drawable.panel_topbar_close_selector);
            img.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    PropertyBarImpl.this.dismiss();
                }
            });
            relativeLayout.addView(img);
        }
        this.mLl_titles = (LinearLayout) view.findViewById(R.id.pb_ll_titles);
        this.mLl_title_checked = (LinearLayout) view.findViewById(R.id.pb_ll_title_checks);
        this.mIv_title_shadow = (ImageView) view.findViewById(R.id.pb_iv_title_shadow);
        this.mLl_tabContents = (LinearLayout) view.findViewById(R.id.pb_ll_tabContents);
        this.mLl_PropertyBar.addView(view);
    }

    public void setPhoneFullScreen(boolean fullScreen) {
        if (!this.display.isPad()) {
            LinearLayout tabLayout = (LinearLayout) this.mLl_tabContents.getParent();
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) tabLayout.getLayoutParams();
            LayoutParams tabContentsLayoutParams = (LayoutParams) this.mLl_tabContents.getLayoutParams();
            if (fullScreen) {
                setHeight(-1);
                layoutParams.height = -1;
                tabContentsLayoutParams.height = -1;
            } else {
                setHeight(-2);
                layoutParams.height = -2;
                tabContentsLayoutParams.height = -2;
            }
            tabLayout.setLayoutParams(layoutParams);
            this.mLl_tabContents.setLayoutParams(tabContentsLayoutParams);
        }
    }

    private View getScaleView() {
        View scaleItem = LayoutInflater.from(this.mContext).inflate(R.layout.pb_scale, null, false);
        this.mScaleEdt = (EditText) scaleItem.findViewById(R.id.pb_scale_percent);
        LinearLayout switchLayout = (LinearLayout) scaleItem.findViewById(R.id.pb_scale_switch_ll);
        ImageView switchImg = (ImageView) scaleItem.findViewById(R.id.pb_scale_switch);
        this.mScaleEdt.setText(String.valueOf(this.mScalePercent));
        this.mScaleEdt.setSelection(String.valueOf(this.mScalePercent).length());
        this.mScaleEdt.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (PropertyBarImpl.this.mScaleSwitch == 1 && s.toString().length() != 0) {
                    int percent = Integer.valueOf(s.toString()).intValue();
                    if (percent < 1) {
                        PropertyBarImpl.this.mScaleEdt.setText(String.valueOf(PropertyBarImpl.this.mScalePercent));
                        PropertyBarImpl.this.mScaleEdt.selectAll();
                    } else if (percent > 100) {
                        PropertyBarImpl.this.mScaleEdt.setText(s.toString().substring(0, s.toString().length() - 1));
                        PropertyBarImpl.this.mScaleEdt.selectAll();
                    } else {
                        PropertyBarImpl.this.mScalePercent = percent;
                        if (PropertyBarImpl.this.mPropertyChangeListener != null) {
                            PropertyBarImpl.this.mPropertyChangeListener.onValueChanged(256, PropertyBarImpl.this.mScalePercent);
                        }
                    }
                }
            }

            public void afterTextChanged(Editable s) {
            }
        });
        if (this.mScaleSwitch == 1) {
            switchImg.setImageResource(R.drawable.setting_on);
            this.mScaleEdt.setEnabled(true);
        } else {
            switchImg.setImageResource(R.drawable.setting_off);
            this.mScaleEdt.setEnabled(false);
        }
        switchLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ImageView switchImage = (ImageView) ((LinearLayout) v).getChildAt(0);
                EditText scaleEdit = (EditText) ((LinearLayout) v.getParent()).getChildAt(0);
                if (PropertyBarImpl.this.mScaleSwitch == 1) {
                    PropertyBarImpl.this.mScaleSwitch = 0;
                    switchImage.setImageResource(R.drawable.setting_off);
                    scaleEdit.setEnabled(false);
                } else {
                    PropertyBarImpl.this.mScaleSwitch = 1;
                    switchImage.setImageResource(R.drawable.setting_on);
                    scaleEdit.setEnabled(true);
                }
                if (PropertyBarImpl.this.mPropertyChangeListener != null) {
                    PropertyBarImpl.this.mPropertyChangeListener.onValueChanged(512, PropertyBarImpl.this.mScaleSwitch);
                }
            }
        });
        return scaleItem;
    }

    private View getIconTypeView() {
        LinearLayout typeItem = new LinearLayout(this.mContext);
        typeItem.setLayoutParams(new LayoutParams(-1, -1));
        typeItem.setGravity(17);
        typeItem.setOrientation(0);
        ListView lv_type = new ListView(this.mContext);
        lv_type.setLayoutParams(new LayoutParams(-1, -1));
        lv_type.setCacheColorHint(this.mContext.getResources().getColor(R.color.ux_color_translucent));
        lv_type.setDivider(new ColorDrawable(this.mContext.getResources().getColor(R.color.ux_color_seperator_gray)));
        lv_type.setDividerHeight(1);
        typeItem.addView(lv_type);
        this.mTypeAdapter = new TypeAdapter(this.mContext, this.mTypePicIds, this.mTypeNames);
        this.mTypeAdapter.setNoteIconType(this.mNoteIconType);
        lv_type.setAdapter(this.mTypeAdapter);
        lv_type.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                PropertyBarImpl.this.mNoteIconType = PropertyBarImpl.ICONTYPES[position];
                PropertyBarImpl.this.mTypeAdapter.setNoteIconType(PropertyBarImpl.this.mNoteIconType);
                PropertyBarImpl.this.mTypeAdapter.notifyDataSetChanged();
                if (PropertyBarImpl.this.mPropertyChangeListener != null) {
                    PropertyBarImpl.this.mPropertyChangeListener.onValueChanged(64, PropertyBarImpl.this.mNoteIconType);
                }
            }
        });
        return typeItem;
    }

    private View getLineWidthView() {
        View lineWidthItem = LayoutInflater.from(this.mContext).inflate(R.layout.pb_linewidth, null, false);
        ThicknessImage thicknessImage = (ThicknessImage) lineWidthItem.findViewById(R.id.pb_img_lineWidth_mypic);
        ((TextView) lineWidthItem.findViewById(R.id.pb_tv_lineWidth_size)).setText(new StringBuilder(String.valueOf((int) (this.mLinewith + 0.5f))).append("px").toString());
        SeekBar sb_lineWidth = (SeekBar) lineWidthItem.findViewById(R.id.sb_lineWidth);
        sb_lineWidth.setProgress((int) ((this.mLinewith - 1.0f) + 0.5f));
        sb_lineWidth.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                LinearLayout linearLayout = (LinearLayout) seekBar.getParent();
                ThicknessImage thicknessImage = (ThicknessImage) linearLayout.getChildAt(0);
                TextView tv_width = (TextView) linearLayout.getChildAt(1);
                if (progress >= 0 && progress < 12) {
                    PropertyBarImpl.this.mLinewith = (float) (progress + 1);
                    thicknessImage.setBorderThickness((float) (progress + 1));
                    tv_width.setText((progress + 1) + "px");
                    if (PropertyBarImpl.this.mPropertyChangeListener != null) {
                        PropertyBarImpl.this.mPropertyChangeListener.onValueChanged(4, (float) (progress + 1));
                    }
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        thicknessImage.setBorderThickness(this.mLinewith);
        thicknessImage.setColor(this.mColor);
        return lineWidthItem;
    }

    private View getLineStyleView() {
        int i;
        View lineStyleItem = LayoutInflater.from(this.mContext).inflate(R.layout.pb_linestyle, null, false);
        LinearLayout pb_ll_borderStyle = (LinearLayout) lineStyleItem.findViewById(R.id.pb_ll_borderStyle);
        for (i = 0; i < this.mLinestyles.length; i++) {
            if (i + 1 == this.mLinestyle) {
                pb_ll_borderStyle.getChildAt(i).setBackgroundResource(R.drawable.pb_border_style_checked);
            } else {
                pb_ll_borderStyle.getChildAt(i).setBackgroundResource(0);
            }
        }
        for (i = 0; i < this.mLinestyles.length; i++) {
            ImageView imageView = (ImageView) pb_ll_borderStyle.getChildAt(i);
            imageView.setTag(Integer.valueOf(i));
            imageView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    LinearLayout linearLayout = (LinearLayout) v.getParent();
                    int tag = Integer.valueOf(v.getTag().toString()).intValue();
                    for (int i = 0; i < PropertyBarImpl.this.mLinestyles.length; i++) {
                        if (i == tag) {
                            linearLayout.getChildAt(i).setBackgroundResource(R.drawable.pb_border_style_checked);
                        } else {
                            linearLayout.getChildAt(i).setBackgroundResource(0);
                        }
                    }
                    if (PropertyBarImpl.this.mPropertyChangeListener != null) {
                        PropertyBarImpl.this.mPropertyChangeListener.onValueChanged(32, PropertyBarImpl.this.mLinestyles[tag]);
                    }
                }
            });
        }
        return lineStyleItem;
    }

    private View getFontView() {
        View fontStyleItem = LayoutInflater.from(this.mContext).inflate(R.layout.pb_fontstyle, null, false);
        TextView pb_tv_font = (TextView) fontStyleItem.findViewById(R.id.pb_tv_font);
        pb_tv_font.setText(this.mFontname);
        pb_tv_font.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                PropertyBarImpl.this.mLl_root.setLayoutParams(new LayoutParams(-1, PropertyBarImpl.this.mLl_root.getMeasuredHeight()));
                PropertyBarImpl.this.mLl_topTabs.setVisibility(8);
                PropertyBarImpl.this.mIv_title_shadow.setVisibility(8);
                PropertyBarImpl.this.mLl_tabContents.removeAllViews();
                PropertyBarImpl.this.mLl_tabContents.addView(PropertyBarImpl.this.getFontSelectedView(1));
            }
        });
        TextView pb_tv_fontSize = (TextView) fontStyleItem.findViewById(R.id.pb_tv_fontSize);
        pb_tv_fontSize.setText(new StringBuilder(String.valueOf((int) this.mFontsize)).append("px").toString());
        pb_tv_fontSize.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                PropertyBarImpl.this.mLl_root.setLayoutParams(new LayoutParams(-1, PropertyBarImpl.this.mLl_root.getMeasuredHeight()));
                PropertyBarImpl.this.mLl_topTabs.setVisibility(8);
                PropertyBarImpl.this.mIv_title_shadow.setVisibility(8);
                PropertyBarImpl.this.mLl_tabContents.removeAllViews();
                PropertyBarImpl.this.mLl_tabContents.addView(PropertyBarImpl.this.getFontSelectedView(2));
            }
        });
        return fontStyleItem;
    }

    private View getFontSelectedView(final int type) {
        View view = LayoutInflater.from(this.mContext).inflate(R.layout.pb_fontstyle_set, null, false);
        ((ImageView) view.findViewById(R.id.pb_iv_fontstyle_back)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                PropertyBarImpl.this.reset(PropertyBarImpl.this.mSupportProperty);
            }
        });
        TextView pb_font_select_title = (TextView) view.findViewById(R.id.pb_font_select_title);
        ListView pb_lv_font = (ListView) view.findViewById(R.id.pb_lv_font);
        int i;
        if (type == 1) {
            pb_font_select_title.setText(this.mContext.getResources().getString(R.string.fx_string_font));
            for (i = 0; i < this.mFontNames.length; i++) {
                if (this.mFontNames[i].equals(this.mFontname)) {
                    this.mFontChecked[i] = true;
                } else {
                    this.mFontChecked[i] = false;
                }
            }
            this.mFontAdapter = new FontAdapter(this.mContext, this.mFontNames, this.mFontChecked);
            pb_lv_font.setAdapter(this.mFontAdapter);
        } else if (type == 2) {
            pb_font_select_title.setText(this.mContext.getResources().getString(R.string.fx_string_fontsize));
            for (i = 0; i < this.mFontSizes.length; i++) {
                if (this.mFontSizes[i] == this.mFontsize) {
                    this.mFontSizeChecked[i] = true;
                } else {
                    this.mFontSizeChecked[i] = false;
                }
            }
            this.mFontSizeAdapter = new FontSizeAdapter(this.mContext, this.mFontSizes, this.mFontSizeChecked);
            pb_lv_font.setAdapter(this.mFontSizeAdapter);
        }
        pb_lv_font.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                int i;
                if (type == 1) {
                    for (i = 0; i < PropertyBarImpl.this.mFontChecked.length; i++) {
                        if (i == position) {
                            PropertyBarImpl.this.mFontChecked[i] = true;
                        } else {
                            PropertyBarImpl.this.mFontChecked[i] = false;
                        }
                    }
                    PropertyBarImpl.this.mFontAdapter.notifyDataSetChanged();
                    PropertyBarImpl.this.mFontname = PropertyBarImpl.this.mFontNames[position];
                    if (PropertyBarImpl.this.mPropertyChangeListener != null) {
                        PropertyBarImpl.this.mPropertyChangeListener.onValueChanged(8, PropertyBarImpl.this.mFontNames[position]);
                    }
                } else if (type == 2) {
                    for (i = 0; i < PropertyBarImpl.this.mFontSizeChecked.length; i++) {
                        if (i == position) {
                            PropertyBarImpl.this.mFontSizeChecked[i] = true;
                        } else {
                            PropertyBarImpl.this.mFontSizeChecked[i] = false;
                        }
                    }
                    PropertyBarImpl.this.mFontSizeAdapter.notifyDataSetChanged();
                    PropertyBarImpl.this.mFontsize = PropertyBarImpl.this.mFontSizes[position];
                    if (PropertyBarImpl.this.mPropertyChangeListener != null) {
                        PropertyBarImpl.this.mPropertyChangeListener.onValueChanged(16, PropertyBarImpl.this.mFontSizes[position]);
                    }
                }
            }
        });
        return view;
    }

    private View getOpacityView() {
        View opacityItem = LayoutInflater.from(this.mContext).inflate(R.layout.pb_opacity, null, false);
        final LinearLayout pb_ll_opacity = (LinearLayout) opacityItem.findViewById(R.id.pb_ll_opacity);
        for (int i = 0; i < pb_ll_opacity.getChildCount(); i++) {
            if (i % 2 == 0) {
                ImageView iv_opacity_item = (ImageView) ((LinearLayout) pb_ll_opacity.getChildAt(i)).getChildAt(0);
                TextView tv_opacity_item = (TextView) ((LinearLayout) pb_ll_opacity.getChildAt(i)).getChildAt(1);
                iv_opacity_item.setTag(Integer.valueOf(i));
                iv_opacity_item.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        int tag = Integer.valueOf(v.getTag().toString()).intValue();
                        for (int j = 0; j < pb_ll_opacity.getChildCount(); j++) {
                            if (j % 2 == 0) {
                                ImageView iv_opacity = (ImageView) ((LinearLayout) ((LinearLayout) ((LinearLayout) ((ImageView) v).getParent()).getParent()).getChildAt(j)).getChildAt(0);
                                TextView tv_opacity = (TextView) ((LinearLayout) ((LinearLayout) ((LinearLayout) ((ImageView) v).getParent()).getParent()).getChildAt(j)).getChildAt(1);
                                if (tag == j) {
                                    ((ImageView) v).setImageResource(PropertyBarImpl.this.mOpacityIdsChecked[j / 2]);
                                    tv_opacity.setTextColor(PropertyBarImpl.this.mContext.getResources().getColor(R.color.ux_text_color_button_colour));
                                } else {
                                    iv_opacity.setImageResource(PropertyBarImpl.this.mOpacityIds[j / 2]);
                                    tv_opacity.setTextColor(PropertyBarImpl.this.mContext.getResources().getColor(R.color.ux_color_dark));
                                }
                            }
                        }
                        if (PropertyBarImpl.this.mPropertyChangeListener != null) {
                            PropertyBarImpl.this.mPropertyChangeListener.onValueChanged(2, PropertyBarImpl.this.mOpacitys[tag / 2]);
                            PropertyBarImpl.this.mOpacity = PropertyBarImpl.this.mOpacitys[tag / 2];
                        }
                    }
                });
                if (this.mOpacity == this.mOpacitys[i / 2]) {
                    iv_opacity_item.setImageResource(this.mOpacityIdsChecked[i / 2]);
                    tv_opacity_item.setTextColor(this.mContext.getResources().getColor(R.color.ux_text_color_button_colour));
                } else {
                    iv_opacity_item.setImageResource(this.mOpacityIds[i / 2]);
                    tv_opacity_item.setTextColor(this.mContext.getResources().getColor(R.color.ux_color_dark));
                }
            }
        }
        return opacityItem;
    }

    private View getColorView() {
        View colorItemView = LayoutInflater.from(this.mContext).inflate(R.layout.pb_color, null, false);
        ((TextView) colorItemView.findViewById(R.id.pb_tv_colorTitle)).setText(this.mContext.getResources().getString(R.string.fx_string_color));
        this.mColorViewPager = (ViewPager) colorItemView.findViewById(R.id.pb_ll_colors_viewpager);
        this.mLlColorDots = (LinearLayout) colorItemView.findViewById(R.id.pb_ll_colors_dots);
        LayoutParams vpParams = (LayoutParams) this.mColorViewPager.getLayoutParams();
        if (this.display.isPad()) {
            vpParams.height = this.display.dp2px(90.0f);
        } else if (this.mContext.getResources().getConfiguration().orientation == 2) {
            vpParams.height = this.display.dp2px(42.0f);
        } else {
            vpParams.height = this.display.dp2px(90.0f);
        }
        this.mColorViewPager.setLayoutParams(vpParams);
        this.mPBLlColors = new LinearLayout(this.mContext);
        this.mPBLlColors.setLayoutParams(new LayoutParams(-1, -2));
        this.mPBLlColors.setOrientation(0);
        initColorOne(this.mPBLlColors);
        this.mColorsPickerRoot = new LinearLayout(this.mContext);
        this.mColorsPickerRoot.setLayoutParams(new LayoutParams(-1, -2));
        this.mColorsPickerRoot.setOrientation(0);
        this.mColorsPickerRoot.setGravity(17);
        ColorPicker colorPicker = new ColorPicker(this.mContext, this.mParent);
        this.mColorsPickerRoot.addView(colorPicker);
        ImageView selfColor = new ImageView(this.mContext);
        LayoutParams selfColorParams = new LayoutParams(this.display.dp2px(30.0f), this.display.dp2px(90.0f));
        if (this.display.isPad()) {
            selfColorParams.height = this.display.dp2px(90.0f);
        } else if (this.mContext.getResources().getConfiguration().orientation == 2) {
            selfColorParams.height = this.display.dp2px(42.0f);
        } else {
            selfColorParams.height = this.display.dp2px(90.0f);
        }
        selfColorParams.leftMargin = this.display.dp2px(10.0f);
        selfColor.setLayoutParams(selfColorParams);
        selfColor.setImageDrawable(new ColorDrawable(this.mColors[0]));
        this.mColorsPickerRoot.addView(selfColor);
        colorPicker.setOnUpdateViewListener(new UpdateViewListener() {
            public void onUpdate(long property, int value) {
                PropertyBarImpl.this.mColors[0] = value;
                PropertyBarImpl.this.mColor = value;
                ((ImageView) PropertyBarImpl.this.mColorsPickerRoot.getChildAt(1)).setImageDrawable(new ColorDrawable(value));
                PropertyBarImpl.this.initColorOne(PropertyBarImpl.this.mPBLlColors);
                if (PropertyBarImpl.this.mTabs.contains(PropertyBarImpl.this.mSupportTabNames[2])) {
                    ((ThicknessImage) ((LinearLayout) ((LinearLayout) ((LinearLayout) PropertyBarImpl.this.mLl_tabContents.getChildAt(PropertyBarImpl.this.mTabs.indexOf(PropertyBarImpl.this.mSupportTabNames[2]))).getChildAt(1)).getChildAt(1)).getChildAt(0)).setColor(PropertyBarImpl.this.mColor);
                }
                if (PropertyBarImpl.this.mPropertyChangeListener != null) {
                    PropertyBarImpl.this.mPropertyChangeListener.onValueChanged(128, value);
                }
            }
        });
        List<View> colorViewList = new ArrayList();
        colorViewList.add(this.mPBLlColors);
        colorViewList.add(this.mColorsPickerRoot);
        this.mColorViewPager.setAdapter(new ColorVPAdapter(colorViewList));
        this.mColorViewPager.setOnPageChangeListener(new OnPageChangeListener() {
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                PropertyBarImpl.this.mCurrentColorIndex = position;
                for (int i = 0; i < PropertyBarImpl.this.mLlColorDots.getChildCount(); i++) {
                    ImageView imageView = (ImageView) PropertyBarImpl.this.mLlColorDots.getChildAt(i);
                    if (i == position) {
                        imageView.setImageResource(PropertyBarImpl.this.mColorDotPics[0]);
                    } else {
                        imageView.setImageResource(PropertyBarImpl.this.mColorDotPics[1]);
                    }
                }
            }

            public void onPageScrollStateChanged(int state) {
            }
        });
        for (int i = 0; i < this.mLlColorDots.getChildCount(); i++) {
            ImageView imageView = (ImageView) this.mLlColorDots.getChildAt(i);
            imageView.setTag(Integer.valueOf(i));
            imageView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    int index = ((Integer) v.getTag()).intValue();
                    if (PropertyBarImpl.this.mCurrentColorIndex != index) {
                        for (int j = 0; j < PropertyBarImpl.this.mLlColorDots.getChildCount(); j++) {
                            ImageView iv = (ImageView) PropertyBarImpl.this.mLlColorDots.getChildAt(j);
                            if (j == index) {
                                iv.setImageResource(PropertyBarImpl.this.mColorDotPics[0]);
                            } else {
                                iv.setImageResource(PropertyBarImpl.this.mColorDotPics[1]);
                            }
                        }
                        PropertyBarImpl.this.mColorViewPager.setCurrentItem(index);
                    }
                }
            });
            if (i == 0) {
                imageView.setImageResource(this.mColorDotPics[0]);
            } else {
                imageView.setImageResource(this.mColorDotPics[1]);
            }
        }
        this.mColorViewPager.setCurrentItem(this.mCurrentColorIndex);
        return colorItemView;
    }

    private void initColorOne(LinearLayout pb_ll_colors) {
        int length;
        pb_ll_colors.removeAllViews();
        int colorWidth = this.display.dp2px(30.0f);
        int padding = this.display.dp2px(6.0f);
        int space = this.display.dp2px(5.0f);
        int tempWidth = this.mParent.getWidth();
        int tempHeight = this.mParent.getHeight();
        if (this.display.isPad()) {
            this.mCurrentWidth = this.mPadWidth;
        } else if (this.mContext.getResources().getConfiguration().orientation == 2) {
            if (tempHeight <= tempWidth) {
                tempHeight = tempWidth;
            }
            this.mCurrentWidth = tempHeight;
        } else {
            if (tempWidth >= tempHeight) {
                tempWidth = tempHeight;
            }
            this.mCurrentWidth = tempWidth;
        }
        if (!this.display.isPad()) {
            length = this.mCurrentWidth - (this.display.dp2px(16.0f) * 2);
        } else if (this.mLlArrowLeft.getVisibility() == 0) {
            this.mLlArrowLeft.measure(0, 0);
            length = (this.mCurrentWidth - (this.display.dp2px(16.0f) * 2)) - this.mLlArrowLeft.getMeasuredWidth();
        } else if (this.mLlArrowRight.getVisibility() == 0) {
            this.mLlArrowRight.measure(0, 0);
            length = (this.mCurrentWidth - (this.display.dp2px(16.0f) * 2)) - this.mLlArrowRight.getMeasuredWidth();
        } else if (this.mLlArrowLeft.getVisibility() == 8 && this.mLlArrowRight.getVisibility() == 8 && this.mLlArrowTop.getVisibility() == 8 && this.mLlArrowBottom.getVisibility() == 8) {
            length = this.mCurrentWidth - (this.display.dp2px(20.0f) * 2);
        } else {
            length = this.mCurrentWidth - (this.display.dp2px(20.0f) * 2);
        }
        int i;
        LinearLayout linearLayout;
        LayoutParams linearLayoutParams;
        if (((padding * 2) + colorWidth) * this.mColors.length > length) {
            if (this.mColors.length <= 1) {
                space = 0;
            } else if (this.mColors.length % 2 == 0) {
                spaces = length - (((padding * 2) + colorWidth) * (this.mColors.length / 2));
                if (spaces > 0) {
                    space = spaces / ((this.mColors.length / 2) - 1);
                } else {
                    space = 0;
                }
            } else {
                spaces = length - (((padding * 2) + colorWidth) * ((this.mColors.length / 2) + 1));
                if (spaces > 0) {
                    space = spaces / (this.mColors.length / 2);
                } else {
                    space = 0;
                }
            }
            pb_ll_colors.setOrientation(1);
            pb_ll_colors.setGravity(17);
            LinearLayout ll_ColorRow1 = new LinearLayout(this.mContext);
            ll_ColorRow1.setLayoutParams(new LayoutParams(-1, -2));
            ll_ColorRow1.setOrientation(0);
            ll_ColorRow1.setGravity(17);
            pb_ll_colors.addView(ll_ColorRow1);
            LinearLayout ll_ColorRow2 = new LinearLayout(this.mContext);
            ll_ColorRow2.setLayoutParams(new LayoutParams(-1, -2));
            ll_ColorRow2.setOrientation(0);
            ll_ColorRow2.setPadding(0, this.display.dp2px(5.0f), 0, 0);
            ll_ColorRow2.setGravity(17);
            pb_ll_colors.addView(ll_ColorRow2);
            i = 0;
            while (i < this.mColors.length) {
                linearLayout = new LinearLayout(this.mContext);
                linearLayoutParams = new LayoutParams((padding * 2) + colorWidth, (padding * 2) + colorWidth);
                linearLayout.setOrientation(0);
                linearLayout.setGravity(17);
                if (this.mColors.length % 2 == 0) {
                    if ((i > 0 && i < this.mColors.length / 2) || i > this.mColors.length / 2) {
                        linearLayoutParams.leftMargin = space;
                    }
                } else if ((i > 0 && i < (this.mColors.length / 2) + 1) || i > (this.mColors.length / 2) + 1) {
                    linearLayoutParams.leftMargin = space;
                }
                linearLayout.setLayoutParams(linearLayoutParams);
                linearLayout.setTag(Integer.valueOf(i));
                LinearLayout oneColor = new LinearLayout(this.mContext);
                LayoutParams oneColorParams = new LayoutParams(colorWidth, colorWidth);
                oneColor.setOrientation(0);
                oneColor.setGravity(17);
                oneColor.setLayoutParams(oneColorParams);
                oneColor.setBackgroundResource(R.drawable.pb_color_bg_border);
                linearLayout.addView(oneColor);
                ImageView color = new ImageView(this.mContext);
                color.setLayoutParams(new LayoutParams(colorWidth - 2, colorWidth - 2));
                color.setImageDrawable(new ColorDrawable(this.mColors[i]));
                oneColor.addView(color);
                if (this.mColors.length % 2 == 0) {
                    if (i < this.mColors.length / 2) {
                        ll_ColorRow1.addView(linearLayout);
                    } else {
                        ll_ColorRow2.addView(linearLayout);
                    }
                } else if (i < (this.mColors.length / 2) + 1) {
                    ll_ColorRow1.addView(linearLayout);
                } else {
                    ll_ColorRow2.addView(linearLayout);
                }
                if (this.mColor == this.mColors[i]) {
                    linearLayout.setBackgroundResource(R.drawable.pb_color_bg);
                } else {
                    linearLayout.setBackgroundColor(0);
                }
                linearLayout.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (v instanceof LinearLayout) {
                            int tag = ((Integer) v.getTag()).intValue();
                            PropertyBarImpl.this.mColor = PropertyBarImpl.this.mColors[tag];
                            for (int j = 0; j < PropertyBarImpl.this.mColors.length; j++) {
                                if (j == tag) {
                                    v.setBackgroundResource(R.drawable.pb_color_bg);
                                } else {
                                    LinearLayout otherColor;
                                    if (PropertyBarImpl.this.mColors.length % 2 == 0) {
                                        if (j < PropertyBarImpl.this.mColors.length / 2) {
                                            otherColor = (LinearLayout) ((LinearLayout) ((LinearLayout) ((LinearLayout) ((LinearLayout) v).getParent()).getParent()).getChildAt(0)).getChildAt(j);
                                        } else {
                                            otherColor = (LinearLayout) ((LinearLayout) ((LinearLayout) ((LinearLayout) ((LinearLayout) v).getParent()).getParent()).getChildAt(1)).getChildAt(j - (PropertyBarImpl.this.mColors.length / 2));
                                        }
                                    } else if (j < (PropertyBarImpl.this.mColors.length / 2) + 1) {
                                        otherColor = (LinearLayout) ((LinearLayout) ((LinearLayout) ((LinearLayout) ((LinearLayout) v).getParent()).getParent()).getChildAt(0)).getChildAt(j);
                                    } else {
                                        otherColor = (LinearLayout) ((LinearLayout) ((LinearLayout) ((LinearLayout) ((LinearLayout) v).getParent()).getParent()).getChildAt(1)).getChildAt(j - ((PropertyBarImpl.this.mColors.length / 2) + 1));
                                    }
                                    otherColor.setBackgroundColor(0);
                                }
                            }
                            if (PropertyBarImpl.this.mPropertyChangeListener != null) {
                                PropertyBarImpl.this.mPropertyChangeListener.onValueChanged(1, PropertyBarImpl.this.mColor);
                            }
                            if (PropertyBarImpl.this.mTabs.contains(PropertyBarImpl.this.mSupportTabNames[2])) {
                                ((ThicknessImage) ((LinearLayout) ((LinearLayout) ((LinearLayout) PropertyBarImpl.this.mLl_tabContents.getChildAt(PropertyBarImpl.this.mTabs.indexOf(PropertyBarImpl.this.mSupportTabNames[2]))).getChildAt(1)).getChildAt(1)).getChildAt(0)).setColor(PropertyBarImpl.this.mColor);
                            }
                        }
                    }
                });
                i++;
            }
            return;
        }
        if (this.mColors.length > 1) {
            space = ((length - (this.mColors.length * colorWidth)) - ((padding * 2) * this.mColors.length)) / (this.mColors.length - 1);
        } else {
            space = 0;
        }
        pb_ll_colors.setOrientation(0);
        pb_ll_colors.setGravity(19);
        for (i = 0; i < this.mColors.length; i++) {
            linearLayout = new LinearLayout(this.mContext);
            linearLayoutParams = new LayoutParams((padding * 2) + colorWidth, (padding * 2) + colorWidth);
            linearLayout.setOrientation(0);
            linearLayout.setGravity(17);
            if (i > 0) {
                linearLayoutParams.leftMargin = space;
            } else if (i == 0) {
                linearLayoutParams.leftMargin = 0;
            }
            linearLayout.setLayoutParams(linearLayoutParams);
            linearLayout.setTag(Integer.valueOf(i));
            oneColor = new LinearLayout(this.mContext);
            oneColorParams = new LayoutParams(colorWidth, colorWidth);
            oneColor.setOrientation(0);
            oneColor.setGravity(17);
            oneColor.setLayoutParams(oneColorParams);
            oneColor.setBackgroundResource(R.drawable.pb_color_bg_border);
            linearLayout.addView(oneColor);
            color = new ImageView(this.mContext);
            color.setLayoutParams(new LayoutParams(colorWidth - 2, colorWidth - 2));
            color.setImageDrawable(new ColorDrawable(this.mColors[i]));
            oneColor.addView(color);
            pb_ll_colors.addView(linearLayout);
            if (this.mColor == this.mColors[i]) {
                linearLayout.setBackgroundResource(R.drawable.pb_color_bg);
            } else {
                linearLayout.setBackgroundColor(0);
            }
            linearLayout.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (v instanceof LinearLayout) {
                        int tag = ((Integer) v.getTag()).intValue();
                        PropertyBarImpl.this.mColor = PropertyBarImpl.this.mColors[tag];
                        for (int j = 0; j < PropertyBarImpl.this.mColors.length; j++) {
                            if (j == tag) {
                                v.setBackgroundResource(R.drawable.pb_color_bg);
                            } else {
                                ((LinearLayout) ((LinearLayout) ((LinearLayout) v).getParent()).getChildAt(j)).setBackgroundColor(0);
                            }
                        }
                        if (PropertyBarImpl.this.mPropertyChangeListener != null) {
                            PropertyBarImpl.this.mPropertyChangeListener.onValueChanged(1, PropertyBarImpl.this.mColor);
                        }
                        if (PropertyBarImpl.this.mTabs.contains(PropertyBarImpl.this.mSupportTabNames[2])) {
                            ((ThicknessImage) ((LinearLayout) ((LinearLayout) ((LinearLayout) PropertyBarImpl.this.mLl_tabContents.getChildAt(PropertyBarImpl.this.mTabs.indexOf(PropertyBarImpl.this.mSupportTabNames[2]))).getChildAt(1)).getChildAt(1)).getChildAt(0)).setColor(PropertyBarImpl.this.mColor);
                        }
                    }
                }
            });
        }
    }

    public void onConfigurationChanged(RectF rectF) {
        int currentRotation = ((Activity) this.mContext).getWindowManager().getDefaultDisplay().getRotation();
        if (this.mCurrentRotation != currentRotation || rectF == null) {
            this.mCurrentRotation = currentRotation;
            if ((this.mSupportProperty != 0 || this.mCustomProperty != 0) && this.mPdfViewCtrl.getDoc() != null) {
                this.mOrientationed = true;
                reset(this.mSupportProperty);
                return;
            }
            return;
        }
        update(rectF);
    }

    public void reset(long items) {
        this.mSupportProperty = items;
        if (!this.mOrientationed) {
            this.mCustomProperty = 0;
            this.mCurrentTab = 0;
            this.mCustomTabList.clear();
            this.mCustomItemList.clear();
        }
        this.mTabs.clear();
        this.mLl_titles.removeAllViews();
        this.mLl_title_checked.removeAllViews();
        for (int i = 0; i < this.mLl_tabContents.getChildCount(); i++) {
            ((LinearLayout) this.mLl_tabContents.getChildAt(i)).removeAllViews();
        }
        this.mLl_tabContents.removeAllViews();
        this.mLl_root.setLayoutParams(new LayoutParams(-1, -2));
        if (items == 0) {
            this.mLl_topTabs.setVisibility(8);
            this.mIv_title_shadow.setVisibility(8);
        } else {
            this.mLl_topTabs.setVisibility(0);
            this.mIv_title_shadow.setVisibility(0);
            resetSupportedView();
        }
        if (this.mOrientationed) {
            if (this.mCustomProperty != 0) {
                resetCustomView();
            }
            this.mOrientationed = false;
        }
    }

    private void resetSupportedView() {
        if ((this.mSupportProperty & 64) == 64) {
            int iconTabIndex;
            String iconTabTitle = this.mSupportTabNames[0];
            if (this.mTabs.size() <= 0) {
                iconTabIndex = 0;
            } else if (this.mTabs.contains(iconTabTitle)) {
                iconTabIndex = this.mTabs.indexOf(iconTabTitle);
                if (iconTabIndex < 0) {
                    iconTabIndex = 0;
                }
            } else {
                iconTabIndex = 0;
            }
            this.mTopTitleLayout.setVisibility(8);
            addTab(iconTabTitle, iconTabIndex);
            addCustomItem(0, getIconTypeView(), iconTabIndex, -1);
        }
        if ((this.mSupportProperty & 8) == 8 || (this.mSupportProperty & 16) == 16 || (this.mSupportProperty & 4) == 4 || (this.mSupportProperty & 1) == 1 || (this.mSupportProperty & 2) == 2 || (this.mSupportProperty & 256) == 256 || (this.mSupportProperty & 512) == 512) {
            String propertyTabTitle = "";
            if ((this.mSupportProperty & 8) == 8 || (this.mSupportProperty & 16) == 16) {
                propertyTabTitle = this.mSupportTabNames[3];
            } else if ((this.mSupportProperty & 4) == 4 || (this.mSupportProperty & 32) == 32) {
                propertyTabTitle = this.mSupportTabNames[2];
            } else if ((this.mSupportProperty & 256) == 256 || (this.mSupportProperty & 512) == 512) {
                propertyTabTitle = this.mSupportTabNames[4];
            } else {
                propertyTabTitle = this.mSupportTabNames[1];
            }
            int propertyTabIndex = this.mTabs.size();
            this.mTopTitleLayout.setVisibility(8);
            addTab(propertyTabTitle, propertyTabIndex);
            if ((this.mSupportProperty & 256) == 256 || (this.mSupportProperty & 512) == 512) {
                addCustomItem(0, getScaleView(), propertyTabIndex, -1);
            }
            if ((this.mSupportProperty & 8) == 8 || (this.mSupportProperty & 16) == 16) {
                addCustomItem(0, getFontView(), propertyTabIndex, -1);
            }
            if ((this.mSupportProperty & 1) == 1) {
                addCustomItem(0, getColorView(), propertyTabIndex, -1);
            }
            if ((this.mSupportProperty & 4) == 4) {
                addCustomItem(0, getLineWidthView(), propertyTabIndex, -1);
            }
            if ((this.mSupportProperty & 32) == 32) {
                addCustomItem(0, getLineStyleView(), propertyTabIndex, -1);
            }
            if ((this.mSupportProperty & 2) == 2) {
                addCustomItem(0, getOpacityView(), propertyTabIndex, -1);
            }
        }
    }

    private void resetCustomView() {
        int i;
        for (i = 0; i < this.mCustomTabList.size(); i++) {
            addTab(((Map) this.mCustomTabList.get(i)).get("topTitle").toString(), ((Integer) ((Map) this.mCustomTabList.get(i)).get("resid_img")).intValue(), ((Map) this.mCustomTabList.get(i)).get("title").toString(), ((Integer) ((Map) this.mCustomTabList.get(i)).get("tabIndex")).intValue());
        }
        for (i = 0; i < this.mCustomItemList.size(); i++) {
            long item = ((Long) ((Map) this.mCustomItemList.get(i)).get("item")).longValue();
            if ((this.mCustomProperty & item) == item) {
                addCustomItem(item, (View) ((Map) this.mCustomItemList.get(i)).get("itemView"), ((Integer) ((Map) this.mCustomItemList.get(i)).get("tabIndex")).intValue(), ((Integer) ((Map) this.mCustomItemList.get(i)).get("index")).intValue());
            }
        }
    }

    private void doAfterAddContentItem() {
        int i;
        for (i = 0; i < this.mLl_tabContents.getChildCount(); i++) {
            LinearLayout tabContentTemp = (LinearLayout) this.mLl_tabContents.getChildAt(i);
            if (tabContentTemp != null && tabContentTemp.getChildCount() > 0) {
                for (int j = 0; j < tabContentTemp.getChildCount(); j++) {
                    View viewItem = tabContentTemp.getChildAt(j);
                    if (viewItem != null) {
                        View separator = viewItem.findViewById(R.id.pb_separator_iv);
                        if (separator != null) {
                            if (j == tabContentTemp.getChildCount() - 1) {
                                separator.setVisibility(8);
                            } else {
                                separator.setVisibility(0);
                            }
                        }
                    }
                }
            }
        }
        resetContentHeight();
        for (i = 0; i < this.mLl_tabContents.getChildCount(); i++) {
            if (i == this.mCurrentTab) {
                this.mLl_tabContents.getChildAt(i).setVisibility(0);
            } else {
                this.mLl_tabContents.getChildAt(i).setVisibility(8);
            }
        }
    }

    private void resetContentHeight() {
        int maxTabContentHeight = 0;
        int w = MeasureSpec.makeMeasureSpec(0, 0);
        int h = MeasureSpec.makeMeasureSpec(0, 0);
        int iconTabIndex = -1;
        if (this.mTabs.contains(this.mSupportTabNames[0])) {
            iconTabIndex = this.mTabs.indexOf(this.mSupportTabNames[0]);
        }
        for (int i = 0; i < this.mLl_tabContents.getChildCount(); i++) {
            LinearLayout child = (LinearLayout) this.mLl_tabContents.getChildAt(i);
            child.measure(w, h);
            int childHeight = child.getMeasuredHeight();
            if (i == iconTabIndex) {
                childHeight = 0;
            }
            if (childHeight > maxTabContentHeight) {
                maxTabContentHeight = childHeight;
            }
        }
        LayoutParams layoutParams = (LayoutParams) this.mLl_tabContents.getLayoutParams();
        if (this.display.isPad() || layoutParams.height != -1) {
            layoutParams.height = maxTabContentHeight;
            this.mLl_tabContents.setLayoutParams(layoutParams);
        }
    }

    private void checkContained() {
        boolean colorContained = false;
        for (int i : this.mColors) {
            if (this.mColor == i) {
                colorContained = true;
                break;
            }
        }
        if (!colorContained) {
            this.mColor = this.mColors[0];
        }
        boolean colorOpacity = false;
        for (int i2 : this.mOpacitys) {
            if (this.mOpacity == i2) {
                colorOpacity = true;
                break;
            }
        }
        if (!colorOpacity) {
            this.mOpacity = this.mOpacitys[this.mOpacitys.length - 1];
        }
    }

    public void addTab(String title, int tabIndex) {
        if (tabIndex <= this.mTabs.size() && tabIndex >= 0) {
            if (title.length() == 0) {
                this.mTabs.add(tabIndex, "");
            } else {
                this.mTabs.add(tabIndex, title);
            }
            TextView tv_title = new TextView(this.mContext);
            tv_title.setLayoutParams(new LayoutParams(0, -2, 1.0f));
            tv_title.setText(title);
            tv_title.setTextSize(16.0f);
            tv_title.setTextColor(this.mContext.getResources().getColor(R.color.ux_text_color_title_light));
            tv_title.setTypeface(Typeface.DEFAULT);
            tv_title.setGravity(17);
            tv_title.setSingleLine(true);
            tv_title.setEllipsize(TruncateAt.END);
            tv_title.setPadding(0, this.display.dp2px(5.0f), 0, this.display.dp2px(10.0f));
            tv_title.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    int clickTagIndex = 0;
                    for (int i = 0; i < PropertyBarImpl.this.mLl_titles.getChildCount(); i++) {
                        if (v == PropertyBarImpl.this.mLl_titles.getChildAt(i)) {
                            clickTagIndex = i;
                        }
                    }
                    if (PropertyBarImpl.this.mCurrentTab != clickTagIndex) {
                        PropertyBarImpl.this.mLl_root.setLayoutParams(new LayoutParams(-1, PropertyBarImpl.this.mLl_root.getMeasuredHeight()));
                        PropertyBarImpl.this.mCurrentTab = clickTagIndex;
                        PropertyBarImpl.this.setCurrentTab(PropertyBarImpl.this.mCurrentTab);
                    }
                }
            });
            this.mLl_titles.addView(tv_title, tabIndex);
            ImageView iv_title_checked = new ImageView(this.mContext);
            iv_title_checked.setLayoutParams(new LayoutParams(0, (int) this.mContext.getResources().getDimension(R.dimen.ux_tab_selection_height), 1.0f));
            this.mLl_title_checked.addView(iv_title_checked);
            LinearLayout ll_content = new LinearLayout(this.mContext);
            ll_content.setLayoutParams(new LayoutParams(-1, -2));
            ll_content.setOrientation(1);
            this.mLl_tabContents.addView(ll_content, tabIndex);
            if (this.mTabs.size() + this.mCustomTabList.size() <= 0) {
                return;
            }
            if (this.mTabs.size() + this.mCustomTabList.size() == 1) {
                this.mLl_topTabs.setVisibility(8);
                this.mIv_title_shadow.setVisibility(8);
                return;
            }
            this.mLl_topTabs.setVisibility(0);
            this.mIv_title_shadow.setVisibility(0);
            setCurrentTab(this.mCurrentTab);
        }
    }

    public void setTopTitleVisible(boolean visible) {
        if (visible) {
            this.mTopTitleLayout.setVisibility(0);
            this.mTopTitleLayout.setTag(Integer.valueOf(1));
            return;
        }
        this.mTopTitleLayout.setVisibility(8);
        this.mTopTitleLayout.setTag(Integer.valueOf(0));
    }

    public void addTab(String topTitle, int resid_img, String title, int tabIndex) {
        if (tabIndex <= this.mTabs.size() + this.mCustomTabList.size() && tabIndex >= 0) {
            if (!this.mOrientationed) {
                Map<String, Object> map = new HashMap();
                if (title.length() == 0) {
                    map.put("title", "");
                } else {
                    map.put("title", title);
                }
                if (topTitle.length() == 0) {
                    map.put("topTitle", "");
                } else {
                    map.put("topTitle", topTitle);
                }
                map.put("resid_img", Integer.valueOf(resid_img));
                map.put("tabIndex", Integer.valueOf(tabIndex));
                this.mCustomTabList.add(map);
            }
            LinearLayout titleLayout = new LinearLayout(this.mContext);
            titleLayout.setLayoutParams(new LayoutParams(0, -2, 1.0f));
            titleLayout.setGravity(17);
            titleLayout.setOrientation(0);
            titleLayout.setPadding(0, this.display.dp2px(5.0f), 0, this.display.dp2px(10.0f));
            titleLayout.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    int clickTagIndex = 0;
                    for (int i = 0; i < PropertyBarImpl.this.mLl_titles.getChildCount(); i++) {
                        if (v == PropertyBarImpl.this.mLl_titles.getChildAt(i)) {
                            clickTagIndex = i;
                        }
                    }
                    if (PropertyBarImpl.this.mCurrentTab != clickTagIndex) {
                        PropertyBarImpl.this.mLl_root.setLayoutParams(new LayoutParams(-1, PropertyBarImpl.this.mLl_root.getMeasuredHeight()));
                        PropertyBarImpl.this.mCurrentTab = clickTagIndex;
                        PropertyBarImpl.this.setCurrentTab(PropertyBarImpl.this.mCurrentTab);
                    }
                }
            });
            if (resid_img != 0 && resid_img > 0) {
                ImageView img = new ImageView(this.mContext);
                img.setLayoutParams(new LayoutParams(-2, -2));
                img.setImageResource(resid_img);
                titleLayout.addView(img);
            }
            if (!(title == null || "".equals(title))) {
                TextView tv_title = new TextView(this.mContext);
                tv_title.setLayoutParams(new LayoutParams(-2, -2));
                tv_title.setText(title);
                tv_title.setTextSize(16.0f);
                tv_title.setTextColor(this.mContext.getResources().getColor(R.color.ux_text_color_title_light));
                tv_title.setGravity(17);
                tv_title.setSingleLine(true);
                tv_title.setEllipsize(TruncateAt.END);
                titleLayout.addView(tv_title);
            }
            this.mLl_titles.addView(titleLayout, tabIndex);
            ImageView iv_title_checked = new ImageView(this.mContext);
            iv_title_checked.setLayoutParams(new LayoutParams(0, (int) this.mContext.getResources().getDimension(R.dimen.ux_tab_selection_height), 1.0f));
            this.mLl_title_checked.addView(iv_title_checked);
            LinearLayout ll_content = new LinearLayout(this.mContext);
            ll_content.setLayoutParams(new LayoutParams(-1, -2));
            ll_content.setOrientation(1);
            this.mLl_tabContents.addView(ll_content, tabIndex);
            if (this.mTabs.size() + this.mCustomTabList.size() <= 0) {
                return;
            }
            if (this.mTabs.size() + this.mCustomTabList.size() == 1) {
                this.mLl_topTabs.setVisibility(8);
                this.mIv_title_shadow.setVisibility(8);
                return;
            }
            this.mLl_topTabs.setVisibility(0);
            this.mIv_title_shadow.setVisibility(0);
            setCurrentTab(this.mCurrentTab);
        }
    }

    public int getCurrentTabIndex() {
        return this.mCurrentTab;
    }

    public void setCurrentTab(int currentTab) {
        this.mCurrentTab = currentTab;
        for (int i = 0; i < this.mLl_titles.getChildCount(); i++) {
            View viewTab;
            View view;
            if (i == currentTab) {
                viewTab = this.mLl_titles.getChildAt(i);
                if (viewTab instanceof TextView) {
                    this.mTopTitle.setText("");
                    this.mTopTitleLayout.setVisibility(8);
                } else if (viewTab instanceof LinearLayout) {
                    if (((Integer) this.mTopTitleLayout.getTag()).intValue() == 1) {
                        this.mTopTitleLayout.setVisibility(0);
                        if (this.mTopTitleLayout.getVisibility() == 0) {
                            String topTitle = "";
                            for (int j = 0; j < this.mCustomTabList.size(); j++) {
                                if (currentTab == ((Integer) ((Map) this.mCustomTabList.get(j)).get("tabIndex")).intValue()) {
                                    topTitle = ((Map) this.mCustomTabList.get(j)).get("topTitle").toString();
                                    break;
                                }
                            }
                            this.mTopTitle.setText(topTitle);
                        }
                    }
                    view = ((LinearLayout) viewTab).getChildAt(0);
                    if (view != null && (view instanceof ImageView)) {
                        ((ImageView) view).setImageState(new int[]{16842913}, true);
                        ((ImageView) view).setSelected(true);
                    }
                }
                ((ImageView) this.mLl_title_checked.getChildAt(i)).setImageDrawable(new ColorDrawable(-1));
                this.mLl_tabContents.getChildAt(i).setVisibility(0);
            } else {
                viewTab = this.mLl_titles.getChildAt(i);
                if (viewTab instanceof LinearLayout) {
                    view = ((LinearLayout) this.mLl_titles.getChildAt(i)).getChildAt(0);
                    if (view != null && (view instanceof ImageView)) {
                        ((ImageView) view).setImageState(new int[0], true);
                        ((ImageView) view).setSelected(false);
                    }
                } else {
                    boolean z = viewTab instanceof TextView;
                }
                ((ImageView) this.mLl_title_checked.getChildAt(i)).setImageDrawable(new ColorDrawable(0));
                this.mLl_tabContents.getChildAt(i).setVisibility(8);
            }
        }
    }

    public int getItemIndex(long item) {
        int indexItemInTab = -1;
        if ((this.mSupportProperty & item) == item) {
            if (item == 64) {
                return this.mTabs.indexOf(this.mSupportTabNames[0]);
            }
            if (this.mTabs.contains(this.mSupportTabNames[1])) {
                indexItemInTab = this.mTabs.indexOf(this.mSupportTabNames[1]);
            }
            if (this.mTabs.contains(this.mSupportTabNames[2])) {
                indexItemInTab = this.mTabs.indexOf(this.mSupportTabNames[2]);
            }
            if (this.mTabs.contains(this.mSupportTabNames[3])) {
                indexItemInTab = this.mTabs.indexOf(this.mSupportTabNames[3]);
            }
            if (this.mTabs.contains(this.mSupportTabNames[4])) {
                return this.mTabs.indexOf(this.mSupportTabNames[4]);
            }
            return indexItemInTab;
        } else if ((this.mCustomProperty & item) != item) {
            return -1;
        } else {
            for (int i = 0; i < this.mCustomItemList.size(); i++) {
                if (item == ((Long) ((Map) this.mCustomItemList.get(i)).get("item")).longValue()) {
                    return ((Integer) ((Map) this.mCustomItemList.get(i)).get("tabIndex")).intValue();
                }
            }
            return -1;
        }
    }

    public void addCustomItem(long item, View itemView, int tabIndex, int index) {
        if (itemView != null && tabIndex >= 0 && tabIndex <= this.mLl_tabContents.getChildCount() - 1) {
            View view = this.mLl_tabContents.getChildAt(tabIndex);
            if (view != null) {
                LinearLayout ll_content = (LinearLayout) view;
                if (index == -1 || (index >= 0 && index <= ll_content.getChildCount())) {
                    if (item > 0 && !this.mOrientationed) {
                        this.mCustomProperty |= item;
                        Map<String, Object> map = new HashMap();
                        map.put("item", Long.valueOf(item));
                        map.put("itemView", itemView);
                        map.put("tabIndex", Integer.valueOf(tabIndex));
                        map.put("index", Integer.valueOf(index));
                        this.mCustomItemList.add(map);
                    }
                    if (index == -1) {
                        ll_content.addView(itemView);
                    } else if (index >= 0 && index <= ll_content.getChildCount()) {
                        ll_content.addView(itemView, index);
                    } else {
                        return;
                    }
                }
                return;
            }
            doAfterAddContentItem();
        }
    }

    public void addContentView(View contentView) {
        this.mLl_tabContents.addView(contentView);
    }

    public View getContentView() {
        return super.getContentView();
    }

    public void update(RectF rectF) {
        this.mRectF.set(rectF);
        int height = this.mParent.getHeight();
        int width = this.mParent.getWidth();
        if (this.display.isPad()) {
            int arrowPosition;
            int w1 = MeasureSpec.makeMeasureSpec(this.mPadWidth, 1073741824);
            int h1 = MeasureSpec.makeMeasureSpec(0, 0);
            this.mLl_root.measure(w1, h1);
            if (rectF.top >= ((float) this.mLl_root.getMeasuredHeight())) {
                arrowPosition = 4;
                this.mLlArrowLeft.setVisibility(8);
                this.mLlArrowTop.setVisibility(8);
                this.mLlArrowRight.setVisibility(8);
                this.mLlArrowBottom.setVisibility(0);
            } else if (((float) height) - rectF.bottom >= ((float) this.mLl_root.getMeasuredHeight())) {
                arrowPosition = 2;
                this.mLlArrowLeft.setVisibility(8);
                this.mLlArrowTop.setVisibility(0);
                this.mLlArrowRight.setVisibility(8);
                this.mLlArrowBottom.setVisibility(8);
            } else if (((float) width) - rectF.right >= ((float) this.mPadWidth)) {
                arrowPosition = 1;
                this.mLlArrowLeft.setVisibility(0);
                this.mLlArrowTop.setVisibility(8);
                this.mLlArrowRight.setVisibility(8);
                this.mLlArrowBottom.setVisibility(8);
            } else if (rectF.left >= ((float) this.mPadWidth)) {
                arrowPosition = 3;
                this.mLlArrowLeft.setVisibility(8);
                this.mLlArrowTop.setVisibility(8);
                this.mLlArrowRight.setVisibility(0);
                this.mLlArrowBottom.setVisibility(8);
            } else {
                arrowPosition = 5;
                this.mLlArrowLeft.setVisibility(8);
                this.mLlArrowTop.setVisibility(8);
                this.mLlArrowRight.setVisibility(8);
                this.mLlArrowBottom.setVisibility(8);
            }
            if (this.mArrowVisible) {
                this.mLl_PropertyBar.setBackgroundResource(R.drawable.pb_popup_bg);
                this.mLl_PropertyBar.setPadding(0, 0, 0, this.display.dp2px(5.0f));
            } else {
                this.mLlArrowLeft.setVisibility(8);
                this.mLlArrowTop.setVisibility(8);
                this.mLlArrowRight.setVisibility(8);
                this.mLlArrowBottom.setVisibility(8);
                this.mLl_PropertyBar.setBackgroundResource(R.drawable.pb_popup_bg_shadow);
                this.mLl_PropertyBar.setPadding(this.display.dp2px(4.0f), this.display.dp2px(4.0f), this.display.dp2px(4.0f), this.display.dp2px(4.0f));
            }
            this.mLl_root.measure(w1, h1);
            int toLeft;
            if (arrowPosition == 4) {
                this.mIvArrowBottom.measure(0, 0);
                if (rectF.left + ((rectF.right - rectF.left) / 2.0f) <= ((float) this.mPadWidth) / 2.0f) {
                    toLeft = 0;
                    if (this.mArrowVisible) {
                        if (rectF.left + ((rectF.right - rectF.left) / 2.0f) > ((float) this.mIvArrowBottom.getMeasuredWidth()) / 2.0f) {
                            this.mLlArrowBottom.setPadding((int) ((rectF.left + ((rectF.right - rectF.left) / 2.0f)) - (((float) this.mIvArrowBottom.getMeasuredWidth()) / 2.0f)), 0, 0, 0);
                        } else {
                            this.mLlArrowBottom.setPadding(0, 0, 0, 0);
                        }
                    }
                } else if ((((float) width) - rectF.left) - ((rectF.right - rectF.left) / 2.0f) > ((float) this.mPadWidth) / 2.0f) {
                    toLeft = (int) ((rectF.left + ((rectF.right - rectF.left) / 2.0f)) - (((float) this.mPadWidth) / 2.0f));
                    if (this.mArrowVisible) {
                        this.mLlArrowBottom.setPadding((int) ((((float) this.mPadWidth) / 2.0f) - (((float) this.mIvArrowBottom.getMeasuredWidth()) / 2.0f)), 0, 0, 0);
                    }
                } else {
                    toLeft = width - this.mPadWidth;
                    if (this.mArrowVisible) {
                        if ((((float) width) - rectF.left) - ((rectF.right - rectF.left) / 2.0f) > ((float) this.mIvArrowBottom.getMeasuredWidth()) / 2.0f) {
                            this.mLlArrowBottom.setPadding(0, 0, (int) (((((float) width) - rectF.left) - ((rectF.right - rectF.left) / 2.0f)) - (((float) this.mIvArrowBottom.getMeasuredWidth()) / 2.0f)), 0);
                        } else {
                            this.mLlArrowBottom.setPadding(this.mPadWidth - this.mIvArrowBottom.getMeasuredWidth(), 0, 0, 0);
                        }
                    }
                }
                update(toLeft, (int) (rectF.top - ((float) this.mLl_root.getMeasuredHeight())), -1, -1);
                return;
            } else if (arrowPosition == 2) {
                this.mIvArrowTop.measure(0, 0);
                if (rectF.left + ((rectF.right - rectF.left) / 2.0f) <= ((float) this.mPadWidth) / 2.0f) {
                    toLeft = 0;
                    if (this.mArrowVisible) {
                        if (rectF.left + ((rectF.right - rectF.left) / 2.0f) > ((float) this.mIvArrowTop.getMeasuredWidth()) / 2.0f) {
                            this.mLlArrowTop.setPadding((int) ((rectF.left + ((rectF.right - rectF.left) / 2.0f)) - (((float) this.mIvArrowTop.getMeasuredWidth()) / 2.0f)), 0, 0, 0);
                        } else {
                            this.mLlArrowTop.setPadding(0, 0, 0, 0);
                        }
                    }
                } else if ((((float) width) - rectF.left) - ((rectF.right - rectF.left) / 2.0f) > ((float) this.mPadWidth) / 2.0f) {
                    toLeft = (int) ((rectF.left + ((rectF.right - rectF.left) / 2.0f)) - (((float) this.mPadWidth) / 2.0f));
                    if (this.mArrowVisible) {
                        this.mLlArrowTop.setPadding((int) ((((float) this.mPadWidth) / 2.0f) - (((float) this.mIvArrowTop.getMeasuredWidth()) / 2.0f)), 0, 0, 0);
                    }
                } else {
                    toLeft = width - this.mPadWidth;
                    if (this.mArrowVisible) {
                        if ((((float) width) - rectF.left) - ((rectF.right - rectF.left) / 2.0f) > ((float) this.mIvArrowTop.getMeasuredWidth()) / 2.0f) {
                            this.mLlArrowTop.setPadding(0, 0, (int) (((((float) width) - rectF.left) - ((rectF.right - rectF.left) / 2.0f)) - (((float) this.mIvArrowTop.getMeasuredWidth()) / 2.0f)), 0);
                        } else {
                            this.mLlArrowTop.setPadding(this.mPadWidth - this.mIvArrowTop.getMeasuredWidth(), 0, 0, 0);
                        }
                    }
                }
                update(toLeft, (int) rectF.bottom, -1, -1);
                return;
            } else if (arrowPosition == 1) {
                this.mIvArrowLeft.measure(0, 0);
                if (rectF.top + ((rectF.bottom - rectF.top) / 2.0f) <= ((float) this.mLl_root.getMeasuredHeight()) / 2.0f) {
                    toTop = 0;
                    if (this.mArrowVisible) {
                        if (rectF.top + ((rectF.bottom - rectF.top) / 2.0f) > ((float) this.mIvArrowLeft.getMeasuredHeight()) / 2.0f) {
                            this.mLlArrowLeft.setPadding(0, (int) ((rectF.top + ((rectF.bottom - rectF.top) / 2.0f)) - (((float) this.mIvArrowLeft.getMeasuredHeight()) / 2.0f)), 0, 0);
                        } else {
                            this.mLlArrowLeft.setPadding(0, 0, 0, 0);
                        }
                    }
                } else if ((((float) height) - rectF.top) - ((rectF.bottom - rectF.top) / 2.0f) > ((float) this.mLl_root.getMeasuredHeight()) / 2.0f) {
                    toTop = (int) ((rectF.top + ((rectF.bottom - rectF.top) / 2.0f)) - (((float) this.mLl_root.getMeasuredHeight()) / 2.0f));
                    if (this.mArrowVisible) {
                        this.mLlArrowLeft.setPadding(0, (int) ((((float) this.mLl_root.getMeasuredHeight()) / 2.0f) - (((float) this.mIvArrowLeft.getMeasuredHeight()) / 2.0f)), 0, 0);
                    }
                } else {
                    toTop = height - this.mLl_root.getMeasuredHeight();
                    if (this.mArrowVisible) {
                        if ((((float) height) - rectF.top) - ((rectF.bottom - rectF.top) / 2.0f) > ((float) this.mIvArrowLeft.getMeasuredHeight()) / 2.0f) {
                            this.mLlArrowLeft.setPadding(0, 0, 0, (int) (((((float) height) - rectF.top) - ((rectF.bottom - rectF.top) / 2.0f)) - (((float) this.mIvArrowLeft.getMeasuredHeight()) / 2.0f)));
                        } else {
                            this.mLlArrowLeft.setPadding(0, this.mLl_root.getMeasuredHeight() - this.mIvArrowLeft.getMeasuredHeight(), 0, 0);
                        }
                    }
                }
                update((int) rectF.right, toTop, -1, -1);
                return;
            } else if (arrowPosition == 3) {
                this.mIvArrowRight.measure(0, 0);
                if (rectF.top + ((rectF.bottom - rectF.top) / 2.0f) <= ((float) this.mLl_root.getMeasuredHeight()) / 2.0f) {
                    toTop = 0;
                    if (this.mArrowVisible) {
                        if (rectF.top + ((rectF.bottom - rectF.top) / 2.0f) > ((float) this.mIvArrowRight.getMeasuredHeight()) / 2.0f) {
                            this.mLlArrowRight.setPadding(0, (int) ((rectF.top + ((rectF.bottom - rectF.top) / 2.0f)) - (((float) this.mIvArrowRight.getMeasuredHeight()) / 2.0f)), 0, 0);
                        } else {
                            this.mLlArrowRight.setPadding(0, 0, 0, 0);
                        }
                    }
                } else if ((((float) height) - rectF.top) - ((rectF.bottom - rectF.top) / 2.0f) > ((float) this.mLl_root.getMeasuredHeight()) / 2.0f) {
                    toTop = (int) ((rectF.top + ((rectF.bottom - rectF.top) / 2.0f)) - (((float) this.mLl_root.getMeasuredHeight()) / 2.0f));
                    if (this.mArrowVisible) {
                        this.mLlArrowRight.setPadding(0, (int) ((((float) this.mLl_root.getMeasuredHeight()) / 2.0f) - (((float) this.mIvArrowRight.getMeasuredHeight()) / 2.0f)), 0, 0);
                    }
                } else {
                    toTop = height - this.mLl_root.getMeasuredHeight();
                    if (this.mArrowVisible) {
                        if ((((float) height) - rectF.top) - ((rectF.bottom - rectF.top) / 2.0f) > ((float) this.mIvArrowRight.getMeasuredHeight()) / 2.0f) {
                            this.mLlArrowRight.setPadding(0, 0, 0, (int) (((((float) height) - rectF.top) - ((rectF.bottom - rectF.top) / 2.0f)) - (((float) this.mIvArrowRight.getMeasuredHeight()) / 2.0f)));
                        } else {
                            this.mLlArrowRight.setPadding(0, this.mLl_root.getMeasuredHeight() - this.mIvArrowRight.getMeasuredHeight(), 0, 0);
                        }
                    }
                }
                update((int) (rectF.left - ((float) this.mPadWidth)), toTop, -1, -1);
                return;
            } else if (arrowPosition == 5) {
                update((int) (rectF.left + ((rectF.right - rectF.left) / 4.0f)), (int) (rectF.top + ((rectF.bottom - rectF.top) / 4.0f)), -1, -1);
                return;
            } else {
                return;
            }
        }
        this.mArrowVisible = false;
        this.mLlArrowLeft.setVisibility(8);
        this.mLlArrowTop.setVisibility(8);
        this.mLlArrowRight.setVisibility(8);
        this.mLlArrowBottom.setVisibility(8);
        this.mLl_PropertyBar.setBackgroundColor(this.mContext.getResources().getColor(R.color.ux_text_color_title_light));
        update(0, 0, -1, -1);
    }

    public boolean isShowing() {
        if (this != null) {
            return super.isShowing();
        }
        return false;
    }

    public void show(RectF rectF, boolean showMask) {
        this.mRectF.set(rectF);
        if (this != null && !isShowing()) {
            int w1;
            setFocusable(true);
            int height = this.mParent.getHeight();
            int width = this.mParent.getWidth();
            if (this.display.isPad()) {
                w1 = MeasureSpec.makeMeasureSpec(this.mPadWidth, 1073741824);
            } else {
                w1 = MeasureSpec.makeMeasureSpec(0, 0);
            }
            int h1 = MeasureSpec.makeMeasureSpec(0, 0);
            this.mLl_root.measure(w1, h1);
            if (this.display.isPad()) {
                int arrowPosition;
                if (rectF.top >= ((float) this.mLl_root.getMeasuredHeight())) {
                    arrowPosition = 4;
                    this.mLlArrowLeft.setVisibility(8);
                    this.mLlArrowTop.setVisibility(8);
                    this.mLlArrowRight.setVisibility(8);
                    this.mLlArrowBottom.setVisibility(0);
                } else if (((float) height) - rectF.bottom >= ((float) this.mLl_root.getMeasuredHeight())) {
                    arrowPosition = 2;
                    this.mLlArrowLeft.setVisibility(8);
                    this.mLlArrowTop.setVisibility(0);
                    this.mLlArrowRight.setVisibility(8);
                    this.mLlArrowBottom.setVisibility(8);
                } else if (((float) width) - rectF.right >= ((float) this.mPadWidth)) {
                    arrowPosition = 1;
                    this.mLlArrowLeft.setVisibility(0);
                    this.mLlArrowTop.setVisibility(8);
                    this.mLlArrowRight.setVisibility(8);
                    this.mLlArrowBottom.setVisibility(8);
                } else if (rectF.left >= ((float) this.mPadWidth)) {
                    arrowPosition = 3;
                    this.mLlArrowLeft.setVisibility(8);
                    this.mLlArrowTop.setVisibility(8);
                    this.mLlArrowRight.setVisibility(0);
                    this.mLlArrowBottom.setVisibility(8);
                } else {
                    arrowPosition = 5;
                    this.mLlArrowLeft.setVisibility(8);
                    this.mLlArrowTop.setVisibility(8);
                    this.mLlArrowRight.setVisibility(8);
                    this.mLlArrowBottom.setVisibility(8);
                }
                if (this.mArrowVisible) {
                    this.mLl_PropertyBar.setBackgroundResource(R.drawable.pb_popup_bg);
                    this.mLl_PropertyBar.setPadding(0, 0, 0, this.display.dp2px(5.0f));
                } else {
                    this.mLlArrowLeft.setVisibility(8);
                    this.mLlArrowTop.setVisibility(8);
                    this.mLlArrowRight.setVisibility(8);
                    this.mLlArrowBottom.setVisibility(8);
                    this.mLl_PropertyBar.setBackgroundResource(R.drawable.pb_popup_bg_shadow);
                    this.mLl_PropertyBar.setPadding(this.display.dp2px(4.0f), this.display.dp2px(4.0f), this.display.dp2px(4.0f), this.display.dp2px(4.0f));
                }
                this.mLl_root.measure(MeasureSpec.makeMeasureSpec(this.mPadWidth, 1073741824), MeasureSpec.makeMeasureSpec(0, 0));
                int toLeft;
                if (arrowPosition == 4) {
                    this.mIvArrowBottom.measure(0, 0);
                    if (rectF.left + ((rectF.right - rectF.left) / 2.0f) <= ((float) this.mPadWidth) / 2.0f) {
                        toLeft = 0;
                        if (this.mArrowVisible) {
                            if (rectF.left + ((rectF.right - rectF.left) / 2.0f) > ((float) this.mIvArrowBottom.getMeasuredWidth()) / 2.0f) {
                                this.mLlArrowBottom.setPadding((int) ((rectF.left + ((rectF.right - rectF.left) / 2.0f)) - (((float) this.mIvArrowBottom.getMeasuredWidth()) / 2.0f)), 0, 0, 0);
                            } else {
                                this.mLlArrowBottom.setPadding(0, 0, 0, 0);
                            }
                        }
                    } else if ((((float) width) - rectF.left) - ((rectF.right - rectF.left) / 2.0f) > ((float) this.mPadWidth) / 2.0f) {
                        toLeft = (int) ((rectF.left + ((rectF.right - rectF.left) / 2.0f)) - (((float) this.mPadWidth) / 2.0f));
                        if (this.mArrowVisible) {
                            this.mLlArrowBottom.setPadding((int) ((((float) this.mPadWidth) / 2.0f) - (((float) this.mIvArrowBottom.getMeasuredWidth()) / 2.0f)), 0, 0, 0);
                        }
                    } else {
                        toLeft = width - this.mPadWidth;
                        if (this.mArrowVisible) {
                            if ((((float) width) - rectF.left) - ((rectF.right - rectF.left) / 2.0f) > ((float) this.mIvArrowBottom.getMeasuredWidth()) / 2.0f) {
                                this.mLlArrowBottom.setPadding(0, 0, (int) (((((float) width) - rectF.left) - ((rectF.right - rectF.left) / 2.0f)) - (((float) this.mIvArrowBottom.getMeasuredWidth()) / 2.0f)), 0);
                            } else {
                                this.mLlArrowBottom.setPadding(this.mPadWidth - this.mIvArrowBottom.getMeasuredWidth(), 0, 0, 0);
                            }
                        }
                    }
                    showAtLocation(this.mParent, 51, toLeft, (int) (rectF.top - ((float) this.mLl_root.getMeasuredHeight())));
                } else if (arrowPosition == 2) {
                    this.mIvArrowTop.measure(0, 0);
                    if (rectF.left + ((rectF.right - rectF.left) / 2.0f) <= ((float) this.mPadWidth) / 2.0f) {
                        toLeft = 0;
                        if (this.mArrowVisible) {
                            if (rectF.left + ((rectF.right - rectF.left) / 2.0f) > ((float) this.mIvArrowTop.getMeasuredWidth()) / 2.0f) {
                                this.mLlArrowTop.setPadding((int) ((rectF.left + ((rectF.right - rectF.left) / 2.0f)) - (((float) this.mIvArrowTop.getMeasuredWidth()) / 2.0f)), 0, 0, 0);
                            } else {
                                this.mLlArrowTop.setPadding(0, 0, 0, 0);
                            }
                        }
                    } else if ((((float) width) - rectF.left) - ((rectF.right - rectF.left) / 2.0f) > ((float) this.mPadWidth) / 2.0f) {
                        toLeft = (int) ((rectF.left + ((rectF.right - rectF.left) / 2.0f)) - (((float) this.mPadWidth) / 2.0f));
                        if (this.mArrowVisible) {
                            this.mLlArrowTop.setPadding((int) ((((float) this.mPadWidth) / 2.0f) - (((float) this.mIvArrowTop.getMeasuredWidth()) / 2.0f)), 0, 0, 0);
                        }
                    } else {
                        toLeft = width - this.mPadWidth;
                        if (this.mArrowVisible) {
                            if ((((float) width) - rectF.left) - ((rectF.right - rectF.left) / 2.0f) > ((float) this.mIvArrowTop.getMeasuredWidth()) / 2.0f) {
                                this.mLlArrowTop.setPadding(0, 0, (int) (((((float) width) - rectF.left) - ((rectF.right - rectF.left) / 2.0f)) - (((float) this.mIvArrowTop.getMeasuredWidth()) / 2.0f)), 0);
                            } else {
                                this.mLlArrowTop.setPadding(this.mPadWidth - this.mIvArrowTop.getMeasuredWidth(), 0, 0, 0);
                            }
                        }
                    }
                    showAtLocation(this.mParent, 51, toLeft, (int) rectF.bottom);
                } else if (arrowPosition == 1) {
                    this.mIvArrowLeft.measure(0, 0);
                    if (rectF.top + ((rectF.bottom - rectF.top) / 2.0f) <= ((float) this.mLl_root.getMeasuredHeight()) / 2.0f) {
                        toTop = 0;
                        if (this.mArrowVisible) {
                            if (rectF.top + ((rectF.bottom - rectF.top) / 2.0f) > ((float) this.mIvArrowLeft.getMeasuredHeight()) / 2.0f) {
                                this.mLlArrowLeft.setPadding(0, (int) ((rectF.top + ((rectF.bottom - rectF.top) / 2.0f)) - (((float) this.mIvArrowLeft.getMeasuredHeight()) / 2.0f)), 0, 0);
                            } else {
                                this.mLlArrowLeft.setPadding(0, 0, 0, 0);
                            }
                        }
                    } else if ((((float) height) - rectF.top) - ((rectF.bottom - rectF.top) / 2.0f) > ((float) this.mLl_root.getMeasuredHeight()) / 2.0f) {
                        toTop = (int) ((rectF.top + ((rectF.bottom - rectF.top) / 2.0f)) - (((float) this.mLl_root.getMeasuredHeight()) / 2.0f));
                        if (this.mArrowVisible) {
                            this.mLlArrowLeft.setPadding(0, (int) ((((float) this.mLl_root.getMeasuredHeight()) / 2.0f) - (((float) this.mIvArrowLeft.getMeasuredHeight()) / 2.0f)), 0, 0);
                        }
                    } else {
                        toTop = height - this.mLl_root.getMeasuredHeight();
                        if (this.mArrowVisible) {
                            if ((((float) height) - rectF.top) - ((rectF.bottom - rectF.top) / 2.0f) > ((float) this.mIvArrowLeft.getMeasuredHeight()) / 2.0f) {
                                this.mLlArrowLeft.setPadding(0, 0, 0, (int) (((((float) height) - rectF.top) - ((rectF.bottom - rectF.top) / 2.0f)) - (((float) this.mIvArrowLeft.getMeasuredHeight()) / 2.0f)));
                            } else {
                                this.mLlArrowLeft.setPadding(0, this.mLl_root.getMeasuredHeight() - this.mIvArrowLeft.getMeasuredHeight(), 0, 0);
                            }
                        }
                    }
                    showAtLocation(this.mParent, 51, (int) rectF.right, toTop);
                } else if (arrowPosition == 3) {
                    this.mIvArrowRight.measure(0, 0);
                    if (rectF.top + ((rectF.bottom - rectF.top) / 2.0f) <= ((float) this.mLl_root.getMeasuredHeight()) / 2.0f) {
                        toTop = 0;
                        if (this.mArrowVisible) {
                            if (rectF.top + ((rectF.bottom - rectF.top) / 2.0f) > ((float) this.mIvArrowRight.getMeasuredHeight()) / 2.0f) {
                                this.mLlArrowRight.setPadding(0, (int) ((rectF.top + ((rectF.bottom - rectF.top) / 2.0f)) - (((float) this.mIvArrowRight.getMeasuredHeight()) / 2.0f)), 0, 0);
                            } else {
                                this.mLlArrowRight.setPadding(0, 0, 0, 0);
                            }
                        }
                    } else if ((((float) height) - rectF.top) - ((rectF.bottom - rectF.top) / 2.0f) > ((float) this.mLl_root.getMeasuredHeight()) / 2.0f) {
                        toTop = (int) ((rectF.top + ((rectF.bottom - rectF.top) / 2.0f)) - (((float) this.mLl_root.getMeasuredHeight()) / 2.0f));
                        if (this.mArrowVisible) {
                            this.mLlArrowRight.setPadding(0, (int) ((((float) this.mLl_root.getMeasuredHeight()) / 2.0f) - (((float) this.mIvArrowRight.getMeasuredHeight()) / 2.0f)), 0, 0);
                        }
                    } else {
                        toTop = height - this.mLl_root.getMeasuredHeight();
                        if (this.mArrowVisible) {
                            if ((((float) height) - rectF.top) - ((rectF.bottom - rectF.top) / 2.0f) > ((float) this.mIvArrowRight.getMeasuredHeight()) / 2.0f) {
                                this.mLlArrowRight.setPadding(0, 0, 0, (int) (((((float) height) - rectF.top) - ((rectF.bottom - rectF.top) / 2.0f)) - (((float) this.mIvArrowRight.getMeasuredHeight()) / 2.0f)));
                            } else {
                                this.mLlArrowRight.setPadding(0, this.mLl_root.getMeasuredHeight() - this.mIvArrowRight.getMeasuredHeight(), 0, 0);
                            }
                        }
                    }
                    showAtLocation(this.mParent, 51, (int) (rectF.left - ((float) this.mPadWidth)), toTop);
                } else if (arrowPosition == 5) {
                    showAtLocation(this.mParent, 51, (int) (rectF.left + ((rectF.right - rectF.left) / 4.0f)), (int) (rectF.top + ((rectF.bottom - rectF.top) / 4.0f)));
                }
            } else {
                if (showMask) {
                    this.mTopShadow.setVisibility(8);
                } else {
                    this.mTopShadow.setVisibility(0);
                }
                this.mArrowVisible = false;
                this.mLlArrowLeft.setVisibility(8);
                this.mLlArrowTop.setVisibility(8);
                this.mLlArrowRight.setVisibility(8);
                this.mLlArrowBottom.setVisibility(8);
                this.mLl_PropertyBar.setBackgroundColor(this.mContext.getResources().getColor(R.color.ux_text_color_title_light));
                showAtLocation(this.mParent, 80, 0, 0);
                UIExtensionsManager uiExtensionsManager = (UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager();
                if (ToolUtil.getCurrentAnnotHandler(uiExtensionsManager) != null && uiExtensionsManager.getCurrentToolHandler() == null) {
                    this.mLl_root.measure(w1, h1);
                    if (rectF.bottom <= 0.0f || rectF.bottom > ((float) height)) {
                        if (rectF.top >= 0.0f && rectF.top <= ((float) height) && rectF.bottom > ((float) height) && rectF.top > ((float) (height - this.mLl_root.getMeasuredHeight()))) {
                            this.offset = (((float) this.mLl_root.getMeasuredHeight()) - (((float) height) - rectF.top)) + 10.0f;
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    PropertyBarImpl.this.mPdfViewCtrl.layout(0, 0 - ((int) PropertyBarImpl.this.offset), PropertyBarImpl.this.mPdfViewCtrl.getWidth(), PropertyBarImpl.this.mPdfViewCtrl.getHeight() - ((int) PropertyBarImpl.this.offset));
                                }
                            }, 300);
                        }
                    } else if (rectF.bottom > ((float) (height - this.mLl_root.getMeasuredHeight()))) {
                        this.offset = ((float) this.mLl_root.getMeasuredHeight()) - (((float) height) - rectF.bottom);
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                PropertyBarImpl.this.mPdfViewCtrl.layout(0, 0 - ((int) PropertyBarImpl.this.offset), PropertyBarImpl.this.mPdfViewCtrl.getWidth(), PropertyBarImpl.this.mPdfViewCtrl.getHeight() - ((int) PropertyBarImpl.this.offset));
                            }
                        }, 300);
                    }
                }
            }
            this.mShowMask = showMask;
        }
    }

    public void dismiss() {
        if (this != null && isShowing()) {
            setFocusable(false);
            super.dismiss();
        }
    }

    public void setArrowVisible(boolean visible) {
        this.mArrowVisible = visible;
    }

    public void setColors(int[] colors) {
        this.mColors = colors;
    }

    public void setProperty(long property, int value) {
        if (property == 1) {
            this.mColor = value;
            int r = Color.red(this.mColor);
            int g = Color.green(this.mColor);
            int b = Color.blue(this.mColor);
            int i = 0;
            while (i < this.mColors.length) {
                int r2 = Color.red(this.mColors[i]);
                int g2 = Color.green(this.mColors[i]);
                int b2 = Color.blue(this.mColors[i]);
                if (Math.abs(r2 - r) > 3 || Math.abs(g2 - g) > 3 || Math.abs(b2 - b) > 3) {
                    i++;
                } else {
                    this.mColor = this.mColors[i];
                    return;
                }
            }
        } else if (property == 2) {
            this.mOpacity = value;
        } else if (property == 64) {
            this.mNoteIconType = value;
        } else if (property == 256) {
            this.mScalePercent = value;
        } else if (property == 512) {
            this.mScaleSwitch = value;
        }
    }

    public void setProperty(long property, float value) {
        if (property == 4) {
            this.mLinewith = value;
        } else if (property == 16) {
            this.mFontsize = value;
        }
    }

    public void setProperty(long property, String value) {
        if (property == 8) {
            this.mFontname = value;
        } else if (property == 64) {
            int i = 0;
            while (i < ICONNAMES.length) {
                if (ICONNAMES[i].compareTo(value) != 0) {
                    i++;
                } else {
                    this.mNoteIconType = ICONTYPES[i];
                    return;
                }
            }
        }
    }

    public PropertyChangeListener getPropertyChangeListener() {
        return this.mPropertyChangeListener;
    }

    public void setPropertyChangeListener(PropertyChangeListener listener) {
        this.mPropertyChangeListener = listener;
    }

    public void setDismissListener(DismissListener dismissListener) {
        this.mDismissListener = dismissListener;
    }
}
