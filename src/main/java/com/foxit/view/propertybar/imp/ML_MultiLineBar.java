package com.foxit.view.propertybar.imp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.foxit.app.App;
import com.foxit.home.R;
import com.foxit.read.RD_Read;
import com.foxit.view.propertybar.IML_MultiLineBar;
import com.foxit.view.propertybar.IML_MultiLineBar.IML_ValueChangeListener;
import java.util.HashMap;
import java.util.Map;

public class ML_MultiLineBar extends ViewGroup implements IML_MultiLineBar {
    private static RD_Read mRead;
    private Context mContext;
    private boolean mDay;
    private ImageView mIv_daynight;
    private ImageView mIv_light_big;
    private ImageView mIv_light_small;
    private ImageView mIv_setlockscreen;
    private ImageView mIv_setreflow;
    private ImageView mIv_syslight;
    private int mLight;
    private Map<Integer, IML_ValueChangeListener> mListeners;
    private View mLl_root;
    private boolean mLockScreen;
    private PopupWindow mPopupWindow;
    private SeekBar mSb_light;
    private boolean mSinglePage;
    private boolean mSysLight;
    private ImageView mTablet_iv_conpage;
    private ImageView mTablet_iv_daynight;
    private ImageView mTablet_iv_light_big;
    private ImageView mTablet_iv_light_small;
    private ImageView mTablet_iv_lockscreen;
    private ImageView mTablet_iv_reflow;
    private ImageView mTablet_iv_singlepage;
    private ImageView mTablet_iv_syslight;
    private ImageView mTablet_iv_thumbs;
    private SeekBar mTablet_sb_light;
    private TextView mTv_conpage;
    private TextView mTv_singlepage;
    private TextView mTv_thumbs;

    public ML_MultiLineBar(Context context) {
        this(context, null);
        this.mContext = context;
        this.mListeners = new HashMap();
        initView();
    }

    public void init(RD_Read read) {
        mRead = read;
    }

    public ML_MultiLineBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ML_MultiLineBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mLight = 50;
        this.mDay = true;
        this.mSysLight = true;
        this.mSinglePage = true;
        this.mLockScreen = false;
    }

    public void setProperty(int property, Object value) {
        Rect bounds;
        if (App.instance().getDisplay().isPad()) {
            if (property == 1) {
                this.mLight = ((Integer) value).intValue();
                this.mTablet_sb_light.setProgress(this.mLight);
            } else if (property == 2) {
                this.mDay = ((Boolean) value).booleanValue();
                if (this.mDay) {
                    this.mTablet_iv_daynight.setImageResource(R.drawable.ml_daynight_day_selector);
                } else {
                    this.mTablet_iv_daynight.setImageResource(R.drawable.ml_daynight_night_selector);
                }
            } else if (property == 3) {
                this.mSysLight = ((Boolean) value).booleanValue();
                if (this.mSysLight) {
                    this.mTablet_iv_syslight.setImageResource(R.drawable.setting_on);
                    bounds = this.mTablet_sb_light.getProgressDrawable().getBounds();
                    this.mTablet_sb_light.setProgressDrawable(this.mContext.getResources().getDrawable(R.drawable.ml_seekbar_unenable_bg));
                    this.mTablet_sb_light.getProgressDrawable().setBounds(bounds);
                    this.mTablet_sb_light.setEnabled(false);
                    this.mTablet_iv_light_small.setImageResource(R.drawable.ml_light_small_pressed);
                    this.mTablet_iv_light_big.setImageResource(R.drawable.ml_light_big_pressed);
                    return;
                }
                this.mTablet_iv_syslight.setImageResource(R.drawable.setting_off);
                bounds = this.mTablet_sb_light.getProgressDrawable().getBounds();
                this.mTablet_sb_light.setProgressDrawable(this.mContext.getResources().getDrawable(R.drawable.ml_seekbar_bg));
                this.mTablet_sb_light.getProgressDrawable().setBounds(bounds);
                this.mTablet_sb_light.setEnabled(true);
                if (this.mTablet_sb_light.getProgress() >= 1) {
                    this.mTablet_sb_light.setProgress(this.mTablet_sb_light.getProgress() - 1);
                    this.mTablet_sb_light.setProgress(this.mTablet_sb_light.getProgress() + 1);
                }
                this.mTablet_iv_light_small.setImageResource(R.drawable.ml_light_small);
                this.mTablet_iv_light_big.setImageResource(R.drawable.ml_light_big);
            } else if (property == 4) {
                this.mSinglePage = ((Boolean) value).booleanValue();
                if (this.mSinglePage) {
                    this.mTablet_iv_singlepage.setImageResource(R.drawable.ml_iv_singlepage_pad_checked_selector);
                    this.mTablet_iv_singlepage.setBackgroundResource(R.drawable.ml_iv_circle_bg_checked_selector);
                    this.mTablet_iv_conpage.setImageResource(R.drawable.ml_iv_conpage_pad_selector);
                    this.mTablet_iv_conpage.setBackgroundResource(R.drawable.ml_iv_circle_bg_selector);
                    return;
                }
                this.mTablet_iv_singlepage.setImageResource(R.drawable.ml_iv_singlepage_pad_selector);
                this.mTablet_iv_singlepage.setBackgroundResource(R.drawable.ml_iv_circle_bg_selector);
                this.mTablet_iv_conpage.setImageResource(R.drawable.ml_iv_conpage_pad_checked_selector);
                this.mTablet_iv_conpage.setBackgroundResource(R.drawable.ml_iv_circle_bg_checked_selector);
            } else if (property == 6) {
                this.mLockScreen = ((Boolean) value).booleanValue();
                if (this.mLockScreen) {
                    this.mTablet_iv_lockscreen.setImageResource(R.drawable.ml_iv_lockscreen_checked_selector);
                    this.mTablet_iv_lockscreen.setBackgroundResource(R.drawable.ml_iv_circle_bg_checked_selector);
                    return;
                }
                this.mTablet_iv_lockscreen.setImageResource(R.drawable.ml_iv_lockscreen_selector);
                this.mTablet_iv_lockscreen.setBackgroundResource(R.drawable.ml_iv_circle_bg_selector);
            }
        } else if (property == 1) {
            this.mLight = ((Integer) value).intValue();
            this.mSb_light.setProgress(this.mLight);
        } else if (property == 2) {
            this.mDay = ((Boolean) value).booleanValue();
            if (this.mDay) {
                this.mIv_daynight.setImageResource(R.drawable.ml_daynight_day_selector);
            } else {
                this.mIv_daynight.setImageResource(R.drawable.ml_daynight_night_selector);
            }
        } else if (property == 3) {
            this.mSysLight = ((Boolean) value).booleanValue();
            if (this.mSysLight) {
                this.mIv_syslight.setImageResource(R.drawable.setting_on);
                bounds = this.mSb_light.getProgressDrawable().getBounds();
                this.mSb_light.setProgressDrawable(this.mContext.getResources().getDrawable(R.drawable.ml_seekbar_unenable_bg));
                this.mSb_light.getProgressDrawable().setBounds(bounds);
                this.mSb_light.setEnabled(false);
                this.mIv_light_small.setImageResource(R.drawable.ml_light_small_pressed);
                this.mIv_light_big.setImageResource(R.drawable.ml_light_big_pressed);
                return;
            }
            this.mIv_syslight.setImageResource(R.drawable.setting_off);
            bounds = this.mSb_light.getProgressDrawable().getBounds();
            this.mSb_light.setProgressDrawable(this.mContext.getResources().getDrawable(R.drawable.ml_seekbar_bg));
            this.mSb_light.getProgressDrawable().setBounds(bounds);
            this.mSb_light.setEnabled(true);
            if (this.mSb_light.getProgress() >= 1) {
                this.mSb_light.setProgress(this.mSb_light.getProgress() - 1);
                this.mSb_light.setProgress(this.mSb_light.getProgress() + 1);
            }
            this.mIv_light_small.setImageResource(R.drawable.ml_light_small);
            this.mIv_light_big.setImageResource(R.drawable.ml_light_big);
        } else if (property == 4) {
            this.mSinglePage = ((Boolean) value).booleanValue();
            if (this.mSinglePage) {
                ((LinearLayout) this.mTv_singlepage.getParent()).setBackgroundResource(R.drawable.ml_top_tv_bg_checked);
                this.mTv_singlepage.setTextColor(-1);
                ((LinearLayout) this.mTv_conpage.getParent()).setBackgroundResource(R.drawable.ml_top_tv_bg);
                this.mTv_conpage.setTextColor(this.mContext.getResources().getColor(R.color.ux_text_color_body2_dark));
                return;
            }
            ((LinearLayout) this.mTv_singlepage.getParent()).setBackgroundResource(R.drawable.ml_top_tv_bg);
            this.mTv_singlepage.setTextColor(this.mContext.getResources().getColor(R.color.ux_text_color_body2_dark));
            ((LinearLayout) this.mTv_conpage.getParent()).setBackgroundResource(R.drawable.ml_top_tv_bg_checked);
            this.mTv_conpage.setTextColor(-1);
        } else if (property == 6) {
            this.mLockScreen = ((Boolean) value).booleanValue();
            if (this.mLockScreen) {
                this.mIv_setlockscreen.setImageResource(R.drawable.ml_iv_lockscreen_checked_selector);
                this.mIv_setlockscreen.setBackgroundResource(R.drawable.ml_iv_circle_bg_checked_selector);
                return;
            }
            this.mIv_setlockscreen.setImageResource(R.drawable.ml_iv_lockscreen_selector);
            this.mIv_setlockscreen.setBackgroundResource(R.drawable.ml_iv_circle_bg_selector);
        }
    }

    public View getContentView() {
        return this.mLl_root;
    }

    public void registerListener(IML_ValueChangeListener listener) {
        int type = listener.getType();
        if (!this.mListeners.containsKey(Integer.valueOf(type))) {
            this.mListeners.put(Integer.valueOf(type), listener);
        }
    }

    public void unRegisterListener(IML_ValueChangeListener listener) {
        if (this.mListeners.containsKey(Integer.valueOf(listener.getType()))) {
            this.mListeners.remove(Integer.valueOf(listener.getType()));
        }
    }

    public boolean isShowing() {
        if (this.mPopupWindow != null) {
            return this.mPopupWindow.isShowing();
        }
        return false;
    }

    public void show() {
        if (this.mPopupWindow != null && !isShowing()) {
            this.mPopupWindow.setFocusable(true);
            this.mPopupWindow.showAtLocation(mRead.getMainFrame().getContentView(), 80, 0, 0);
        }
    }

    public void dismiss() {
        if (this.mPopupWindow != null && isShowing()) {
            this.mPopupWindow.setFocusable(false);
            this.mPopupWindow.dismiss();
        }
    }

    @SuppressLint({"NewApi"})
    private void initView() {
        setLayoutParams(new LayoutParams(-1, -2));
        setBackgroundColor(-1);
        Rect bounds;
        if (App.instance().getDisplay().isPad()) {
            this.mLl_root = LayoutInflater.from(this.mContext).inflate(R.layout.ml_setbar_tablet, null, false);
            this.mLl_root.setLayoutParams(new LayoutParams(-1, -2));
            addView(this.mLl_root);
            ((HorizontalScrollView) this.mLl_root.findViewById(R.id.ml_tablet_hsv_all)).setHorizontalScrollBarEnabled(false);
            this.mTablet_iv_singlepage = (ImageView) this.mLl_root.findViewById(R.id.ml_tablet_iv_singlepage);
            this.mTablet_iv_conpage = (ImageView) this.mLl_root.findViewById(R.id.ml_tablet_iv_conpage);
            this.mTablet_iv_thumbs = (ImageView) this.mLl_root.findViewById(R.id.ml_tablet_iv_thumbs);
            this.mTablet_iv_reflow = (ImageView) this.mLl_root.findViewById(R.id.ml_tablet_iv_reflow);
            this.mTablet_iv_lockscreen = (ImageView) this.mLl_root.findViewById(R.id.ml_tablet_iv_lockscreen);
            this.mTablet_iv_daynight = (ImageView) this.mLl_root.findViewById(R.id.ml_tablet_iv_daynight);
            this.mTablet_iv_syslight = (ImageView) this.mLl_root.findViewById(R.id.ml_tablet_iv_syslight);
            this.mTablet_sb_light = (SeekBar) this.mLl_root.findViewById(R.id.ml_tablet_sb_light);
            this.mTablet_iv_light_small = (ImageView) this.mLl_root.findViewById(R.id.ml_tablet_iv_light_small);
            this.mTablet_iv_light_big = (ImageView) this.mLl_root.findViewById(R.id.ml_tablet_iv_light_big);
            OnClickListener tb_clickListener = new OnClickListener() {
                public void onClick(View v) {
                    if (v.getId() == R.id.ml_tablet_iv_singlepage) {
                        if (ML_MultiLineBar.this.mListeners.get(Integer.valueOf(4)) != null && !ML_MultiLineBar.this.mSinglePage) {
                            ML_MultiLineBar.this.mSinglePage = true;
                            ((IML_ValueChangeListener) ML_MultiLineBar.this.mListeners.get(Integer.valueOf(4))).onValueChanged(4, Boolean.valueOf(ML_MultiLineBar.this.mSinglePage));
                            if (ML_MultiLineBar.this.mSinglePage) {
                                ML_MultiLineBar.this.mTablet_iv_singlepage.setImageResource(R.drawable.ml_iv_singlepage_pad_checked_selector);
                                ML_MultiLineBar.this.mTablet_iv_singlepage.setBackgroundResource(R.drawable.ml_iv_circle_bg_checked_selector);
                                ML_MultiLineBar.this.mTablet_iv_conpage.setImageResource(R.drawable.ml_iv_conpage_pad_selector);
                                ML_MultiLineBar.this.mTablet_iv_conpage.setBackgroundResource(R.drawable.ml_iv_circle_bg_selector);
                                return;
                            }
                            ML_MultiLineBar.this.mTablet_iv_singlepage.setImageResource(R.drawable.ml_iv_singlepage_pad_selector);
                            ML_MultiLineBar.this.mTablet_iv_singlepage.setBackgroundResource(R.drawable.ml_iv_circle_bg_selector);
                            ML_MultiLineBar.this.mTablet_iv_conpage.setImageResource(R.drawable.ml_iv_conpage_pad_checked_selector);
                            ML_MultiLineBar.this.mTablet_iv_conpage.setBackgroundResource(R.drawable.ml_iv_circle_bg_checked_selector);
                        }
                    } else if (v.getId() == R.id.ml_tablet_iv_conpage) {
                        if (ML_MultiLineBar.this.mListeners.get(Integer.valueOf(4)) != null && ML_MultiLineBar.this.mSinglePage) {
                            ML_MultiLineBar.this.mSinglePage = false;
                            ((IML_ValueChangeListener) ML_MultiLineBar.this.mListeners.get(Integer.valueOf(4))).onValueChanged(4, Boolean.valueOf(ML_MultiLineBar.this.mSinglePage));
                            if (ML_MultiLineBar.this.mSinglePage) {
                                ML_MultiLineBar.this.mTablet_iv_singlepage.setImageResource(R.drawable.ml_iv_singlepage_pad_checked_selector);
                                ML_MultiLineBar.this.mTablet_iv_singlepage.setBackgroundResource(R.drawable.ml_iv_circle_bg_checked_selector);
                                ML_MultiLineBar.this.mTablet_iv_conpage.setImageResource(R.drawable.ml_iv_conpage_pad_selector);
                                ML_MultiLineBar.this.mTablet_iv_conpage.setBackgroundResource(R.drawable.ml_iv_circle_bg_selector);
                                return;
                            }
                            ML_MultiLineBar.this.mTablet_iv_singlepage.setImageResource(R.drawable.ml_iv_singlepage_pad_selector);
                            ML_MultiLineBar.this.mTablet_iv_singlepage.setBackgroundResource(R.drawable.ml_iv_circle_bg_selector);
                            ML_MultiLineBar.this.mTablet_iv_conpage.setImageResource(R.drawable.ml_iv_conpage_pad_checked_selector);
                            ML_MultiLineBar.this.mTablet_iv_conpage.setBackgroundResource(R.drawable.ml_iv_circle_bg_checked_selector);
                        }
                    } else if (v.getId() == R.id.ml_tablet_iv_thumbs) {
                        if (ML_MultiLineBar.this.mListeners.get(Integer.valueOf(5)) != null) {
                            ((IML_ValueChangeListener) ML_MultiLineBar.this.mListeners.get(Integer.valueOf(5))).onValueChanged(5, Integer.valueOf(0));
                        }
                    } else if (v.getId() == R.id.ml_tablet_iv_lockscreen) {
                        ML_MultiLineBar.this.mLockScreen = false;
                        if (ML_MultiLineBar.this.mListeners.get(Integer.valueOf(6)) != null) {
                            ((IML_ValueChangeListener) ML_MultiLineBar.this.mListeners.get(Integer.valueOf(6))).onValueChanged(6, Boolean.valueOf(ML_MultiLineBar.this.mLockScreen));
                            if (ML_MultiLineBar.this.mLockScreen) {
                                ML_MultiLineBar.this.mTablet_iv_lockscreen.setImageResource(R.drawable.ml_iv_lockscreen_checked_selector);
                                ML_MultiLineBar.this.mTablet_iv_lockscreen.setBackgroundResource(R.drawable.ml_iv_circle_bg_checked_selector);
                                return;
                            }
                            ML_MultiLineBar.this.mTablet_iv_lockscreen.setImageResource(R.drawable.ml_iv_lockscreen_selector);
                            ML_MultiLineBar.this.mTablet_iv_lockscreen.setBackgroundResource(R.drawable.ml_iv_circle_bg_selector);
                        }
                    } else if (v.getId() == R.id.ml_tablet_iv_daynight) {
                        if (ML_MultiLineBar.this.mListeners.get(Integer.valueOf(2)) != null) {
                            ImageView imageView = (ImageView) v;
                            r5 = ML_MultiLineBar.this;
                            if (ML_MultiLineBar.this.mDay) {
                                r2 = false;
                            } else {
                                r2 = true;
                            }
                            r5.mDay = r2;
                            ((IML_ValueChangeListener) ML_MultiLineBar.this.mListeners.get(Integer.valueOf(2))).onValueChanged(2, Boolean.valueOf(ML_MultiLineBar.this.mDay));
                            if (ML_MultiLineBar.this.mDay) {
                                imageView.setImageResource(R.drawable.ml_daynight_day_selector);
                            } else {
                                imageView.setImageResource(R.drawable.ml_daynight_night_selector);
                            }
                        }
                    } else if (v.getId() == R.id.ml_tablet_iv_syslight) {
                        if (ML_MultiLineBar.this.mListeners.get(Integer.valueOf(3)) != null) {
                            r5 = ML_MultiLineBar.this;
                            if (ML_MultiLineBar.this.mSysLight) {
                                r2 = false;
                            } else {
                                r2 = true;
                            }
                            r5.mSysLight = r2;
                            ((IML_ValueChangeListener) ML_MultiLineBar.this.mListeners.get(Integer.valueOf(3))).onValueChanged(3, Boolean.valueOf(ML_MultiLineBar.this.mSysLight));
                            Rect bounds;
                            if (ML_MultiLineBar.this.mSysLight) {
                                ((ImageView) v).setImageResource(R.drawable.setting_on);
                                bounds = ML_MultiLineBar.this.mTablet_sb_light.getProgressDrawable().getBounds();
                                ML_MultiLineBar.this.mTablet_sb_light.setProgressDrawable(ML_MultiLineBar.this.mContext.getResources().getDrawable(R.drawable.ml_seekbar_unenable_bg));
                                ML_MultiLineBar.this.mTablet_sb_light.getProgressDrawable().setBounds(bounds);
                                ML_MultiLineBar.this.mTablet_sb_light.setEnabled(false);
                                ML_MultiLineBar.this.mTablet_iv_light_small.setImageResource(R.drawable.ml_light_small_pressed);
                                ML_MultiLineBar.this.mTablet_iv_light_big.setImageResource(R.drawable.ml_light_big_pressed);
                                return;
                            }
                            ((ImageView) v).setImageResource(R.drawable.setting_off);
                            bounds = ML_MultiLineBar.this.mTablet_sb_light.getProgressDrawable().getBounds();
                            ML_MultiLineBar.this.mTablet_sb_light.setProgressDrawable(ML_MultiLineBar.this.mContext.getResources().getDrawable(R.drawable.ml_seekbar_bg));
                            ML_MultiLineBar.this.mTablet_sb_light.getProgressDrawable().setBounds(bounds);
                            ML_MultiLineBar.this.mTablet_sb_light.setEnabled(true);
                            if (ML_MultiLineBar.this.mTablet_sb_light.getProgress() >= 1) {
                                ML_MultiLineBar.this.mTablet_sb_light.setProgress(ML_MultiLineBar.this.mTablet_sb_light.getProgress() - 1);
                                ML_MultiLineBar.this.mTablet_sb_light.setProgress(ML_MultiLineBar.this.mTablet_sb_light.getProgress() + 1);
                            }
                            ML_MultiLineBar.this.mTablet_iv_light_small.setImageResource(R.drawable.ml_light_small);
                            ML_MultiLineBar.this.mTablet_iv_light_big.setImageResource(R.drawable.ml_light_big);
                        }
                    } else if (v.getId() == R.id.ml_tablet_iv_reflow && ML_MultiLineBar.this.mListeners.get(Integer.valueOf(7)) != null) {
                        ((IML_ValueChangeListener) ML_MultiLineBar.this.mListeners.get(Integer.valueOf(7))).onValueChanged(7, Boolean.valueOf(true));
                        ML_MultiLineBar.this.mTablet_iv_reflow.setImageResource(R.drawable.ml_iv_reflow_selector);
                        ML_MultiLineBar.this.mTablet_iv_reflow.setBackgroundResource(R.drawable.ml_iv_circle_bg_selector);
                    }
                }
            };
            this.mTablet_iv_singlepage.setOnClickListener(tb_clickListener);
            this.mTablet_iv_conpage.setOnClickListener(tb_clickListener);
            this.mTablet_iv_thumbs.setOnClickListener(tb_clickListener);
            this.mTablet_iv_lockscreen.setOnClickListener(tb_clickListener);
            this.mTablet_iv_reflow.setOnClickListener(tb_clickListener);
            this.mTablet_iv_daynight.setOnClickListener(tb_clickListener);
            this.mTablet_iv_syslight.setOnClickListener(tb_clickListener);
            if (this.mDay) {
                this.mTablet_iv_daynight.setImageResource(R.drawable.ml_daynight_day_selector);
            } else {
                this.mTablet_iv_daynight.setImageResource(R.drawable.ml_daynight_night_selector);
            }
            if (this.mSysLight) {
                this.mTablet_iv_syslight.setImageResource(R.drawable.setting_on);
                bounds = this.mTablet_sb_light.getProgressDrawable().getBounds();
                this.mTablet_sb_light.setProgressDrawable(this.mContext.getResources().getDrawable(R.drawable.ml_seekbar_unenable_bg));
                this.mTablet_sb_light.getProgressDrawable().setBounds(bounds);
                this.mTablet_sb_light.setEnabled(false);
                this.mTablet_iv_light_small.setImageResource(R.drawable.ml_light_small_pressed);
                this.mTablet_iv_light_big.setImageResource(R.drawable.ml_light_big_pressed);
            } else {
                this.mTablet_iv_syslight.setImageResource(R.drawable.setting_off);
                bounds = this.mTablet_sb_light.getProgressDrawable().getBounds();
                this.mTablet_sb_light.setProgressDrawable(this.mContext.getResources().getDrawable(R.drawable.ml_seekbar_bg));
                this.mTablet_sb_light.getProgressDrawable().setBounds(bounds);
                this.mTablet_sb_light.setEnabled(true);
                if (this.mTablet_sb_light.getProgress() >= 1) {
                    this.mTablet_sb_light.setProgress(this.mTablet_sb_light.getProgress() - 1);
                    this.mTablet_sb_light.setProgress(this.mTablet_sb_light.getProgress() + 1);
                }
                this.mTablet_iv_light_small.setImageResource(R.drawable.ml_light_small);
                this.mTablet_iv_light_big.setImageResource(R.drawable.ml_light_big);
            }
            this.mTablet_sb_light.setProgress(this.mLight);
            this.mTablet_sb_light.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    ML_MultiLineBar.this.mLight = progress;
                    if (ML_MultiLineBar.this.mListeners.get(Integer.valueOf(1)) != null) {
                        ((IML_ValueChangeListener) ML_MultiLineBar.this.mListeners.get(Integer.valueOf(1))).onValueChanged(1, Integer.valueOf(progress));
                    }
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            this.mTablet_sb_light.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    return event.getAction() == 0;
                }
            });
        } else {
            this.mLl_root = LayoutInflater.from(this.mContext).inflate(R.layout.ml_setbar, null, false);
            this.mLl_root.setLayoutParams(new LayoutParams(-1, -2));
            addView(this.mLl_root);
            this.mTv_singlepage = (TextView) this.mLl_root.findViewById(R.id.ml_tv_singlepage);
            this.mTv_conpage = (TextView) this.mLl_root.findViewById(R.id.ml_tv_conpage);
            this.mTv_thumbs = (TextView) this.mLl_root.findViewById(R.id.ml_tv_thumbs);
            this.mIv_setreflow = (ImageView) this.mLl_root.findViewById(R.id.ml_iv_setreflow);
            this.mTablet_iv_reflow = (ImageView) this.mLl_root.findViewById(R.id.ml_tablet_iv_reflow);
            this.mIv_setlockscreen = (ImageView) this.mLl_root.findViewById(R.id.ml_iv_setlockscreen);
            this.mIv_daynight = (ImageView) this.mLl_root.findViewById(R.id.ml_iv_daynight);
            this.mIv_syslight = (ImageView) this.mLl_root.findViewById(R.id.ml_iv_syslight);
            this.mSb_light = (SeekBar) this.mLl_root.findViewById(R.id.ml_sb_light);
            this.mIv_light_small = (ImageView) this.mLl_root.findViewById(R.id.ml_iv_light_small);
            this.mIv_light_big = (ImageView) this.mLl_root.findViewById(R.id.ml_iv_light_big);
            OnClickListener clickListener = new OnClickListener() {
                public void onClick(View v) {
                    if (v.getId() == R.id.ml_tv_singlepage) {
                        if (ML_MultiLineBar.this.mListeners.get(Integer.valueOf(4)) != null && !ML_MultiLineBar.this.mSinglePage) {
                            ML_MultiLineBar.this.mSinglePage = true;
                            ((IML_ValueChangeListener) ML_MultiLineBar.this.mListeners.get(Integer.valueOf(4))).onValueChanged(4, Boolean.valueOf(ML_MultiLineBar.this.mSinglePage));
                            ML_MultiLineBar.this.mTv_singlepage.setTextColor(-1);
                            ((LinearLayout) ML_MultiLineBar.this.mTv_singlepage.getParent()).setBackgroundResource(R.drawable.ml_top_tv_bg_checked);
                            ML_MultiLineBar.this.mTv_conpage.setTextColor(ML_MultiLineBar.this.mContext.getResources().getColor(R.color.ux_text_color_body2_dark));
                            ((LinearLayout) ML_MultiLineBar.this.mTv_conpage.getParent()).setBackgroundResource(R.drawable.ml_top_tv_bg);
                        }
                    } else if (v.getId() == R.id.ml_tv_conpage) {
                        if (ML_MultiLineBar.this.mListeners.get(Integer.valueOf(4)) != null && ML_MultiLineBar.this.mSinglePage) {
                            ML_MultiLineBar.this.mSinglePage = false;
                            ((IML_ValueChangeListener) ML_MultiLineBar.this.mListeners.get(Integer.valueOf(4))).onValueChanged(4, Boolean.valueOf(ML_MultiLineBar.this.mSinglePage));
                            ML_MultiLineBar.this.mTv_singlepage.setTextColor(ML_MultiLineBar.this.mContext.getResources().getColor(R.color.ux_text_color_body2_dark));
                            ((LinearLayout) ML_MultiLineBar.this.mTv_singlepage.getParent()).setBackgroundResource(R.drawable.ml_top_tv_bg);
                            ML_MultiLineBar.this.mTv_conpage.setTextColor(-1);
                            ((LinearLayout) ML_MultiLineBar.this.mTv_conpage.getParent()).setBackgroundResource(R.drawable.ml_top_tv_bg_checked);
                        }
                    } else if (v.getId() == R.id.ml_tv_thumbs) {
                        if (ML_MultiLineBar.this.mListeners.get(Integer.valueOf(5)) != null) {
                            ((IML_ValueChangeListener) ML_MultiLineBar.this.mListeners.get(Integer.valueOf(5))).onValueChanged(5, Integer.valueOf(0));
                        }
                    } else if (v.getId() == R.id.ml_iv_setlockscreen) {
                        ML_MultiLineBar.this.mLockScreen = false;
                        if (ML_MultiLineBar.this.mListeners.get(Integer.valueOf(6)) != null) {
                            ((IML_ValueChangeListener) ML_MultiLineBar.this.mListeners.get(Integer.valueOf(6))).onValueChanged(6, Boolean.valueOf(ML_MultiLineBar.this.mLockScreen));
                            if (ML_MultiLineBar.this.mLockScreen) {
                                ML_MultiLineBar.this.mIv_setlockscreen.setImageResource(R.drawable.ml_iv_lockscreen_checked_selector);
                                ML_MultiLineBar.this.mIv_setlockscreen.setBackgroundResource(R.drawable.ml_iv_circle_bg_checked_selector);
                                return;
                            }
                            ML_MultiLineBar.this.mIv_setlockscreen.setImageResource(R.drawable.ml_iv_lockscreen_selector);
                            ML_MultiLineBar.this.mIv_setlockscreen.setBackgroundResource(R.drawable.ml_iv_circle_bg_selector);
                        }
                    } else if (v.getId() == R.id.ml_iv_daynight) {
                        if (ML_MultiLineBar.this.mListeners.get(Integer.valueOf(2)) != null) {
                            r5 = ML_MultiLineBar.this;
                            if (ML_MultiLineBar.this.mDay) {
                                r2 = false;
                            } else {
                                r2 = true;
                            }
                            r5.mDay = r2;
                            ((IML_ValueChangeListener) ML_MultiLineBar.this.mListeners.get(Integer.valueOf(2))).onValueChanged(2, Boolean.valueOf(ML_MultiLineBar.this.mDay));
                            ImageView imageView = (ImageView) v;
                            if (ML_MultiLineBar.this.mDay) {
                                imageView.setImageResource(R.drawable.ml_daynight_day_selector);
                            } else {
                                imageView.setImageResource(R.drawable.ml_daynight_night_selector);
                            }
                        }
                    } else if (v.getId() == R.id.ml_iv_syslight) {
                        if (ML_MultiLineBar.this.mListeners.get(Integer.valueOf(3)) != null) {
                            r5 = ML_MultiLineBar.this;
                            if (ML_MultiLineBar.this.mSysLight) {
                                r2 = false;
                            } else {
                                r2 = true;
                            }
                            r5.mSysLight = r2;
                            ((IML_ValueChangeListener) ML_MultiLineBar.this.mListeners.get(Integer.valueOf(3))).onValueChanged(3, Boolean.valueOf(ML_MultiLineBar.this.mSysLight));
                            Rect bounds;
                            if (ML_MultiLineBar.this.mSysLight) {
                                ((ImageView) v).setImageResource(R.drawable.setting_on);
                                bounds = ML_MultiLineBar.this.mSb_light.getProgressDrawable().getBounds();
                                ML_MultiLineBar.this.mSb_light.setProgressDrawable(ML_MultiLineBar.this.mContext.getResources().getDrawable(R.drawable.ml_seekbar_unenable_bg));
                                ML_MultiLineBar.this.mSb_light.getProgressDrawable().setBounds(bounds);
                                ML_MultiLineBar.this.mSb_light.setEnabled(false);
                                ML_MultiLineBar.this.mIv_light_small.setImageResource(R.drawable.ml_light_small_pressed);
                                ML_MultiLineBar.this.mIv_light_big.setImageResource(R.drawable.ml_light_big_pressed);
                                return;
                            }
                            ((ImageView) v).setImageResource(R.drawable.setting_off);
                            bounds = ML_MultiLineBar.this.mSb_light.getProgressDrawable().getBounds();
                            ML_MultiLineBar.this.mSb_light.setProgressDrawable(ML_MultiLineBar.this.mContext.getResources().getDrawable(R.drawable.ml_seekbar_bg));
                            ML_MultiLineBar.this.mSb_light.getProgressDrawable().setBounds(bounds);
                            ML_MultiLineBar.this.mSb_light.setEnabled(true);
                            if (ML_MultiLineBar.this.mSb_light.getProgress() >= 1) {
                                ML_MultiLineBar.this.mSb_light.setProgress(ML_MultiLineBar.this.mSb_light.getProgress() - 1);
                                ML_MultiLineBar.this.mSb_light.setProgress(ML_MultiLineBar.this.mSb_light.getProgress() + 1);
                            }
                            ML_MultiLineBar.this.mIv_light_small.setImageResource(R.drawable.ml_light_small);
                            ML_MultiLineBar.this.mIv_light_big.setImageResource(R.drawable.ml_light_big);
                        }
                    } else if (v.getId() == R.id.ml_iv_setreflow && ML_MultiLineBar.this.mListeners.get(Integer.valueOf(7)) != null) {
                        ((IML_ValueChangeListener) ML_MultiLineBar.this.mListeners.get(Integer.valueOf(7))).onValueChanged(7, Boolean.valueOf(true));
                        ML_MultiLineBar.this.mIv_setreflow.setImageResource(R.drawable.ml_iv_reflow_selector);
                        ML_MultiLineBar.this.mIv_setreflow.setBackgroundResource(R.drawable.ml_iv_circle_bg_selector);
                    }
                }
            };
            this.mTv_singlepage.setOnClickListener(clickListener);
            this.mTv_conpage.setOnClickListener(clickListener);
            this.mTv_thumbs.setOnClickListener(clickListener);
            this.mIv_setreflow.setOnClickListener(clickListener);
            this.mIv_setlockscreen.setOnClickListener(clickListener);
            this.mIv_daynight.setOnClickListener(clickListener);
            this.mIv_syslight.setOnClickListener(clickListener);
            if (this.mDay) {
                this.mIv_daynight.setImageResource(R.drawable.ml_daynight_day_selector);
            } else {
                this.mIv_daynight.setImageResource(R.drawable.ml_daynight_night_selector);
            }
            if (this.mSysLight) {
                this.mIv_syslight.setImageResource(R.drawable.setting_on);
                bounds = this.mSb_light.getProgressDrawable().getBounds();
                this.mSb_light.setProgressDrawable(this.mContext.getResources().getDrawable(R.drawable.ml_seekbar_unenable_bg));
                this.mSb_light.getProgressDrawable().setBounds(bounds);
                this.mSb_light.setEnabled(false);
                this.mIv_light_small.setImageResource(R.drawable.ml_light_small_pressed);
                this.mIv_light_big.setImageResource(R.drawable.ml_light_big_pressed);
            } else {
                this.mIv_syslight.setImageResource(R.drawable.setting_off);
                bounds = this.mSb_light.getProgressDrawable().getBounds();
                this.mSb_light.setProgressDrawable(this.mContext.getResources().getDrawable(R.drawable.ml_seekbar_bg));
                this.mSb_light.getProgressDrawable().setBounds(bounds);
                this.mSb_light.setEnabled(true);
                if (this.mSb_light.getProgress() >= 1) {
                    this.mSb_light.setProgress(this.mSb_light.getProgress() - 1);
                    this.mSb_light.setProgress(this.mSb_light.getProgress() + 1);
                }
                this.mIv_light_small.setImageResource(R.drawable.ml_light_small);
                this.mIv_light_big.setImageResource(R.drawable.ml_light_big);
            }
            this.mSb_light.setProgress(this.mLight);
            this.mSb_light.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    ML_MultiLineBar.this.mLight = progress;
                    if (ML_MultiLineBar.this.mListeners.get(Integer.valueOf(1)) != null) {
                        ((IML_ValueChangeListener) ML_MultiLineBar.this.mListeners.get(Integer.valueOf(1))).onValueChanged(1, Integer.valueOf(progress));
                    }
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }
        if (this.mPopupWindow == null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getMetrics(displayMetrics);
            int heightPixels = displayMetrics.heightPixels;
            if (heightPixels < 480) {
                this.mPopupWindow = new PopupWindow(this, -1, heightPixels);
            } else {
                this.mPopupWindow = new PopupWindow(this, -1, -2);
            }
            this.mPopupWindow.setTouchable(true);
            this.mPopupWindow.setOutsideTouchable(true);
            this.mPopupWindow.setBackgroundDrawable(new ColorDrawable(0));
            this.mPopupWindow.setOnDismissListener(new OnDismissListener() {
                public void onDismiss() {
                    ((IML_ValueChangeListener) ML_MultiLineBar.this.mListeners.get(Integer.valueOf(1))).onDismiss();
                }
            });
            return;
        }
        this.mPopupWindow.setContentView(this);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        int measureWidth = 0;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        switch (widthMode) {
            case Integer.MIN_VALUE:
            case 1073741824:
                measureWidth = widthSize;
                break;
        }
        int measureHeight = 0;
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int totalHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            totalHeight += getChildAt(i).getMeasuredHeight();
        }
        switch (heightMode) {
            case Integer.MIN_VALUE:
                measureHeight = totalHeight;
                break;
            case 1073741824:
                measureHeight = heightSize;
                break;
        }
        setMeasuredDimension(measureWidth, measureHeight);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int mTotalHeight = 0;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            int measureHeight = childView.getMeasuredHeight();
            childView.layout(l, mTotalHeight, l + childView.getMeasuredWidth(), mTotalHeight + measureHeight);
            mTotalHeight += measureHeight;
        }
    }
}
