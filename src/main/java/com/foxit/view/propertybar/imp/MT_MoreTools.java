package com.foxit.view.propertybar.imp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import com.foxit.app.App;
import com.foxit.home.R;
import com.foxit.read.RD_Read;
import com.foxit.uiextensions.controls.toolbar.BaseBar.TB_Position;
import com.foxit.uiextensions.controls.toolbar.BaseItem;
import com.foxit.uiextensions.controls.toolbar.ToolbarItemConfig;
import com.foxit.uiextensions.controls.toolbar.impl.CircleItemImpl;
import com.foxit.view.propertybar.IMT_MoreTools;
import com.foxit.view.propertybar.IMT_MoreTools.IMT_DismissListener;
import com.foxit.view.propertybar.IMT_MoreTools.IMT_MoreClickListener;
import java.util.HashMap;
import java.util.Map;

public class MT_MoreTools implements IMT_MoreTools {
    private static RD_Read mRead;
    private Context mContext;
    private IMT_DismissListener mDismissListener;
    private ImageView mIv_arrow_bottom;
    private Map<Integer, IMT_MoreClickListener> mListeners;
    private LinearLayout mLl_arrow;
    private LinearLayout mLl_content;
    private View mLl_root;
    private BaseItem mMoreItem;
    private ImageView mMt_iv_arrow;
    private ImageView mMt_iv_circle;
    private ImageView mMt_iv_eraser;
    private ImageView mMt_iv_fileattach;
    private ImageView mMt_iv_highlight;
    private ImageView mMt_iv_inserttext;
    private ImageView mMt_iv_line;
    private ImageView mMt_iv_note;
    private ImageView mMt_iv_pencil;
    private ImageView mMt_iv_replace;
    private ImageView mMt_iv_square;
    private ImageView mMt_iv_squiggly;
    private ImageView mMt_iv_stamp;
    private ImageView mMt_iv_strikeout;
    private ImageView mMt_iv_typewriter;
    private ImageView mMt_iv_underline;
    private LinearLayout mMt_ll_fileattach;
    private LinearLayout mMt_ll_stamp;
    private TextView mMt_tv_fileattach;
    private TextView mMt_tv_note;
    private TextView mMt_tv_stamp;
    private TextView mMt_tv_typewriter;
    private int mPadWidth;
    private PopupWindow mPopupWindow;
    private boolean mShowMask = false;

    public MT_MoreTools(RD_Read read) {
        mRead = read;
        this.mContext = App.instance().getApplicationContext();
        this.mListeners = new HashMap();
        this.mPadWidth = App.instance().getDisplay().dp2px(315.0f);
        init();
    }

    @SuppressLint({"NewApi"})
    private void init() {
        this.mMoreItem = new CircleItemImpl(this.mContext) {
            public void onItemLayout(int l, int t, int r, int b) {
                super.onItemLayout(l, t, r, b);
                if (MT_MoreTools.mRead.getMainFrame().getMoreToolsBar().isShowing() && MT_MoreTools.mRead.getState() == 4) {
                    Rect rect = new Rect();
                    MT_MoreTools.this.mMoreItem.getContentView().getGlobalVisibleRect(rect);
                    MT_MoreTools.mRead.getMainFrame().getMoreToolsBar().update(new RectF(rect));
                }
            }
        };
        this.mMoreItem.setTag(ToolbarItemConfig.ITEM_ANNOTSBAR_MORE_TAG);
        this.mMoreItem.setImageResource(R.drawable.mt_more_selector);
        this.mMoreItem.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (MT_MoreTools.mRead.getMainFrame().isEditBarShowing()) {
                    App.instance().getEventManager().onTriggerDismissMenu();
                    Rect rect = new Rect();
                    MT_MoreTools.this.mMoreItem.getContentView().getGlobalVisibleRect(rect);
                    MT_MoreTools.mRead.getMainFrame().getMoreToolsBar().show(new RectF(rect), true);
                }
            }
        });
        mRead.getMainFrame().getEditBar().addView(this.mMoreItem, TB_Position.Position_CENTER);
        this.mLl_root = LayoutInflater.from(this.mContext).inflate(R.layout.mt_moretools, null, false);
        this.mMt_iv_highlight = (ImageView) this.mLl_root.findViewById(R.id.mt_iv_highlight);
        this.mMt_iv_underline = (ImageView) this.mLl_root.findViewById(R.id.mt_iv_underline);
        this.mMt_iv_squiggly = (ImageView) this.mLl_root.findViewById(R.id.mt_iv_squiggly);
        this.mMt_iv_strikeout = (ImageView) this.mLl_root.findViewById(R.id.mt_iv_strikeout);
        this.mMt_iv_inserttext = (ImageView) this.mLl_root.findViewById(R.id.mt_iv_insert);
        this.mMt_iv_replace = (ImageView) this.mLl_root.findViewById(R.id.mt_iv_replace);
        this.mMt_iv_circle = (ImageView) this.mLl_root.findViewById(R.id.mt_iv_circle);
        this.mMt_iv_square = (ImageView) this.mLl_root.findViewById(R.id.mt_iv_square);
        this.mMt_iv_pencil = (ImageView) this.mLl_root.findViewById(R.id.mt_iv_pencil);
        this.mMt_iv_eraser = (ImageView) this.mLl_root.findViewById(R.id.mt_iv_eraser);
        this.mMt_iv_line = (ImageView) this.mLl_root.findViewById(R.id.mt_iv_line);
        this.mMt_iv_arrow = (ImageView) this.mLl_root.findViewById(R.id.mt_iv_arrow);
        this.mMt_iv_typewriter = (ImageView) this.mLl_root.findViewById(R.id.mt_iv_typewriter);
        this.mMt_tv_typewriter = (TextView) this.mLl_root.findViewById(R.id.mt_tv_typewriter);
        this.mMt_iv_note = (ImageView) this.mLl_root.findViewById(R.id.mt_iv_note);
        this.mMt_tv_note = (TextView) this.mLl_root.findViewById(R.id.mt_tv_note);
        this.mMt_ll_stamp = (LinearLayout) this.mLl_root.findViewById(R.id.mt_ll_stamp);
        this.mMt_iv_stamp = (ImageView) this.mLl_root.findViewById(R.id.mt_iv_stamp);
        this.mMt_tv_stamp = (TextView) this.mLl_root.findViewById(R.id.mt_tv_stamp);
        this.mMt_ll_fileattach = (LinearLayout) this.mLl_root.findViewById(R.id.mt_ll_fileattach);
        this.mMt_iv_fileattach = (ImageView) this.mLl_root.findViewById(R.id.mt_iv_fileattach);
        this.mMt_tv_fileattach = (TextView) this.mLl_root.findViewById(R.id.mt_tv_fileattach);
        this.mLl_content = (LinearLayout) this.mLl_root.findViewById(R.id.mt_ll_content);
        this.mLl_arrow = (LinearLayout) this.mLl_root.findViewById(R.id.mt_ll_arrow);
        this.mIv_arrow_bottom = (ImageView) this.mLl_root.findViewById(R.id.mt_iv_arrow_bottom);
        if (this.mPopupWindow == null) {
            if (App.instance().getDisplay().isPad()) {
                this.mPopupWindow = new PopupWindow(this.mLl_root, this.mPadWidth, -2);
                this.mLl_content.setBackgroundResource(R.drawable.dlg_title_bg_4circle_corner_white);
            } else {
                this.mPopupWindow = new PopupWindow(this.mLl_root, -1, -2);
                this.mLl_content.setBackgroundResource(R.color.ux_color_white);
                this.mLl_arrow.setVisibility(8);
            }
            this.mLl_content.setPadding(0, App.instance().getDisplay().dp2px(10.0f), 0, App.instance().getDisplay().dp2px(10.0f));
            this.mPopupWindow.setTouchable(true);
            this.mPopupWindow.setOutsideTouchable(true);
            this.mPopupWindow.setBackgroundDrawable(new ColorDrawable(0));
            this.mPopupWindow.setSoftInputMode(1);
            this.mPopupWindow.setSoftInputMode(48);
            this.mPopupWindow.setOnDismissListener(new OnDismissListener() {
                public void onDismiss() {
                    if (MT_MoreTools.this.mDismissListener != null) {
                        MT_MoreTools.this.mDismissListener.onMTDismiss();
                    }
                    MT_MoreTools.mRead.getMainFrame().hideMaskView();
                }
            });
            if (!App.instance().getDisplay().isPad()) {
                this.mPopupWindow.setAnimationStyle(R.style.View_Animation_BtoT);
            }
        } else {
            this.mPopupWindow.setContentView(this.mLl_root);
        }
        OnClickListener listener = new OnClickListener() {
            public void onClick(View v) {
                if (v.getId() == R.id.mt_iv_highlight) {
                    if (MT_MoreTools.this.mListeners.get(Integer.valueOf(1)) != null) {
                        ((IMT_MoreClickListener) MT_MoreTools.this.mListeners.get(Integer.valueOf(1))).onMTClick(1);
                    }
                } else if (v.getId() == R.id.mt_iv_underline) {
                    if (MT_MoreTools.this.mListeners.get(Integer.valueOf(5)) != null) {
                        ((IMT_MoreClickListener) MT_MoreTools.this.mListeners.get(Integer.valueOf(5))).onMTClick(5);
                    }
                } else if (v.getId() == R.id.mt_iv_squiggly) {
                    if (MT_MoreTools.this.mListeners.get(Integer.valueOf(4)) != null) {
                        ((IMT_MoreClickListener) MT_MoreTools.this.mListeners.get(Integer.valueOf(4))).onMTClick(4);
                    }
                } else if (v.getId() == R.id.mt_iv_strikeout) {
                    if (MT_MoreTools.this.mListeners.get(Integer.valueOf(3)) != null) {
                        ((IMT_MoreClickListener) MT_MoreTools.this.mListeners.get(Integer.valueOf(3))).onMTClick(3);
                    }
                } else if (v.getId() == R.id.mt_iv_circle) {
                    if (MT_MoreTools.this.mListeners.get(Integer.valueOf(6)) != null) {
                        ((IMT_MoreClickListener) MT_MoreTools.this.mListeners.get(Integer.valueOf(6))).onMTClick(6);
                    }
                } else if (v.getId() == R.id.mt_iv_square) {
                    if (MT_MoreTools.this.mListeners.get(Integer.valueOf(7)) != null) {
                        ((IMT_MoreClickListener) MT_MoreTools.this.mListeners.get(Integer.valueOf(7))).onMTClick(7);
                    }
                } else if (v.getId() == R.id.mt_iv_note) {
                    if (MT_MoreTools.this.mListeners.get(Integer.valueOf(2)) != null) {
                        ((IMT_MoreClickListener) MT_MoreTools.this.mListeners.get(Integer.valueOf(2))).onMTClick(2);
                    }
                } else if (v.getId() == R.id.mt_iv_typewriter) {
                    if (MT_MoreTools.this.mListeners.get(Integer.valueOf(8)) != null) {
                        ((IMT_MoreClickListener) MT_MoreTools.this.mListeners.get(Integer.valueOf(8))).onMTClick(8);
                    }
                } else if (v.getId() == R.id.mt_iv_stamp) {
                    if (MT_MoreTools.this.mListeners.get(Integer.valueOf(9)) != null) {
                        ((IMT_MoreClickListener) MT_MoreTools.this.mListeners.get(Integer.valueOf(9))).onMTClick(9);
                    }
                } else if (v.getId() == R.id.mt_iv_insert) {
                    if (MT_MoreTools.this.mListeners.get(Integer.valueOf(10)) != null) {
                        ((IMT_MoreClickListener) MT_MoreTools.this.mListeners.get(Integer.valueOf(10))).onMTClick(10);
                    }
                } else if (v.getId() == R.id.mt_iv_replace) {
                    if (MT_MoreTools.this.mListeners.get(Integer.valueOf(11)) != null) {
                        ((IMT_MoreClickListener) MT_MoreTools.this.mListeners.get(Integer.valueOf(11))).onMTClick(11);
                    }
                } else if (v.getId() == R.id.mt_iv_pencil) {
                    if (MT_MoreTools.this.mListeners.get(Integer.valueOf(13)) != null) {
                        ((IMT_MoreClickListener) MT_MoreTools.this.mListeners.get(Integer.valueOf(13))).onMTClick(13);
                    }
                } else if (v.getId() == R.id.mt_iv_eraser) {
                    if (MT_MoreTools.this.mListeners.get(Integer.valueOf(12)) != null) {
                        ((IMT_MoreClickListener) MT_MoreTools.this.mListeners.get(Integer.valueOf(12))).onMTClick(12);
                    }
                } else if (v.getId() == R.id.mt_iv_arrow) {
                    if (MT_MoreTools.this.mListeners.get(Integer.valueOf(15)) != null) {
                        ((IMT_MoreClickListener) MT_MoreTools.this.mListeners.get(Integer.valueOf(15))).onMTClick(15);
                    }
                } else if (v.getId() == R.id.mt_iv_line) {
                    if (MT_MoreTools.this.mListeners.get(Integer.valueOf(14)) != null) {
                        ((IMT_MoreClickListener) MT_MoreTools.this.mListeners.get(Integer.valueOf(14))).onMTClick(14);
                    }
                } else if (v.getId() == R.id.mt_iv_fileattach && MT_MoreTools.this.mListeners.get(Integer.valueOf(16)) != null) {
                    ((IMT_MoreClickListener) MT_MoreTools.this.mListeners.get(Integer.valueOf(16))).onMTClick(16);
                }
                MT_MoreTools.this.dismiss();
            }
        };
        this.mMt_iv_highlight.setOnClickListener(listener);
        this.mMt_iv_underline.setOnClickListener(listener);
        this.mMt_iv_strikeout.setOnClickListener(listener);
        this.mMt_iv_squiggly.setOnClickListener(listener);
        this.mMt_iv_inserttext.setOnClickListener(listener);
        this.mMt_iv_replace.setOnClickListener(listener);
        this.mMt_iv_circle.setOnClickListener(listener);
        this.mMt_iv_square.setOnClickListener(listener);
        this.mMt_iv_pencil.setOnClickListener(listener);
        this.mMt_iv_eraser.setOnClickListener(listener);
        this.mMt_iv_line.setOnClickListener(listener);
        this.mMt_iv_arrow.setOnClickListener(listener);
        this.mMt_iv_typewriter.setOnClickListener(listener);
        this.mMt_iv_note.setOnClickListener(listener);
        this.mMt_iv_stamp.setOnClickListener(listener);
        this.mMt_iv_fileattach.setOnClickListener(listener);
    }

    public void registerListener(IMT_MoreClickListener listener) {
        if (!this.mListeners.containsKey(Integer.valueOf(listener.getType()))) {
            this.mListeners.put(Integer.valueOf(listener.getType()), listener);
        }
    }

    public void unRegisterListener(IMT_MoreClickListener listener) {
        if (this.mListeners.containsKey(Integer.valueOf(listener.getType()))) {
            this.mListeners.remove(Integer.valueOf(listener.getType()));
        }
    }

    public void setMTDismissListener(IMT_DismissListener dismissListener) {
        this.mDismissListener = dismissListener;
    }

    public View getContentView() {
        return this.mLl_root;
    }

    public void setButtonEnable(int buttonType, boolean enable) {
        if (buttonType == 1) {
            this.mMt_iv_highlight.setEnabled(enable);
        } else if (buttonType == 5) {
            this.mMt_iv_underline.setEnabled(enable);
        } else if (buttonType == 4) {
            this.mMt_iv_squiggly.setEnabled(enable);
        } else if (buttonType == 3) {
            this.mMt_iv_strikeout.setEnabled(enable);
        } else if (buttonType == 6) {
            this.mMt_iv_circle.setEnabled(enable);
        } else if (buttonType == 7) {
            this.mMt_iv_square.setEnabled(enable);
        } else if (buttonType == 10) {
            this.mMt_iv_inserttext.setEnabled(enable);
        } else if (buttonType == 11) {
            this.mMt_iv_replace.setEnabled(enable);
        } else if (buttonType == 13) {
            this.mMt_iv_pencil.setEnabled(enable);
        } else if (buttonType == 12) {
            this.mMt_iv_eraser.setEnabled(enable);
        } else if (buttonType == 14) {
            this.mMt_iv_line.setEnabled(enable);
        } else if (buttonType == 15) {
            this.mMt_iv_arrow.setEnabled(enable);
        } else if (buttonType == 2) {
            this.mMt_tv_note.setTextColor(this.mContext.getResources().getColor(R.color.ux_text_color_body2_gray));
            this.mMt_iv_note.setEnabled(enable);
        } else if (buttonType == 8) {
            if (enable) {
                this.mMt_tv_typewriter.setTextColor(this.mContext.getResources().getColor(R.color.ux_text_color_body2_dark));
                this.mMt_iv_typewriter.setEnabled(enable);
                return;
            }
            this.mMt_tv_typewriter.setTextColor(this.mContext.getResources().getColor(R.color.ux_text_color_body2_gray));
            this.mMt_iv_typewriter.setEnabled(enable);
        } else if (buttonType == 9) {
            if (enable) {
                this.mMt_tv_stamp.setTextColor(this.mContext.getResources().getColor(R.color.ux_text_color_body2_dark));
            } else {
                this.mMt_tv_stamp.setTextColor(this.mContext.getResources().getColor(R.color.ux_text_color_body2_gray));
            }
            this.mMt_iv_stamp.setEnabled(enable);
        } else if (buttonType != 16) {
        } else {
            if (enable) {
                this.mMt_tv_fileattach.setTextColor(this.mContext.getResources().getColor(R.color.ux_text_color_body2_dark));
                this.mMt_iv_fileattach.setEnabled(enable);
                return;
            }
            this.mMt_tv_fileattach.setTextColor(this.mContext.getResources().getColor(R.color.ux_text_color_body2_gray));
            this.mMt_iv_fileattach.setEnabled(enable);
        }
    }

    public void show(RectF rectF, boolean showMask) {
        if (this.mPopupWindow != null && !isShowing()) {
            this.mPopupWindow.setFocusable(true);
            int height = mRead.getMainFrame().getContentView().getHeight();
            int width = mRead.getMainFrame().getContentView().getWidth();
            if (App.instance().getDisplay().isPad()) {
                int toRight;
                this.mIv_arrow_bottom.measure(0, 0);
                if (((float) (this.mPadWidth / 2)) > (((float) width) - rectF.right) + ((rectF.right - rectF.left) / 2.0f)) {
                    toRight = 0;
                    this.mLl_arrow.setPadding((int) ((((((float) this.mPadWidth) / 2.0f) - (((float) this.mIv_arrow_bottom.getMeasuredWidth()) / 2.0f)) + ((float) (this.mPadWidth / 2))) - ((((float) width) - rectF.right) + ((rectF.right - rectF.left) / 2.0f))), 0, 0, 0);
                } else if (rectF.right - ((rectF.right - rectF.left) / 2.0f) > ((float) this.mPadWidth) / 2.0f) {
                    toRight = (int) (((((float) width) - rectF.right) + ((rectF.right - rectF.left) / 2.0f)) - ((float) (this.mPadWidth / 2)));
                    this.mLl_arrow.setPadding((int) ((((float) this.mPadWidth) / 2.0f) - (((float) this.mIv_arrow_bottom.getMeasuredWidth()) / 2.0f)), 0, 0, 0);
                } else {
                    toRight = width - this.mPadWidth;
                    if (rectF.right - ((rectF.right - rectF.left) / 2.0f) > ((float) this.mIv_arrow_bottom.getMeasuredWidth()) / 2.0f) {
                        this.mLl_arrow.setPadding((int) ((rectF.right - ((rectF.right - rectF.left) / 2.0f)) - (((float) this.mIv_arrow_bottom.getMeasuredWidth()) / 2.0f)), 0, 0, 0);
                    } else {
                        this.mLl_arrow.setPadding(0, 0, 0, 0);
                    }
                }
                this.mPopupWindow.showAtLocation(mRead.getMainFrame().getContentView(), 85, toRight, (int) (((float) height) - rectF.top));
            } else {
                this.mPopupWindow.setWidth(width);
                this.mPopupWindow.showAtLocation(mRead.getMainFrame().getContentView(), 83, 0, 0);
            }
            this.mShowMask = showMask;
            if (this.mShowMask) {
                mRead.getMainFrame().showMaskView();
            }
        }
    }

    public boolean isShowing() {
        if (this.mPopupWindow != null) {
            return this.mPopupWindow.isShowing();
        }
        return false;
    }

    public void dismiss() {
        if (this.mPopupWindow != null && isShowing()) {
            this.mPopupWindow.setFocusable(false);
            this.mPopupWindow.dismiss();
        }
    }

    public void update(RectF rectF) {
        int height = mRead.getMainFrame().getContentView().getHeight();
        int width = mRead.getMainFrame().getContentView().getWidth();
        if (App.instance().getDisplay().isPad()) {
            int toRight;
            this.mIv_arrow_bottom.measure(0, 0);
            if (((float) (this.mPadWidth / 2)) > (((float) width) - rectF.right) + ((rectF.right - rectF.left) / 2.0f)) {
                toRight = 0;
                this.mLl_arrow.setPadding((int) ((((((float) this.mPadWidth) / 2.0f) - (((float) this.mIv_arrow_bottom.getMeasuredWidth()) / 2.0f)) + ((float) (this.mPadWidth / 2))) - ((((float) width) - rectF.right) + ((rectF.right - rectF.left) / 2.0f))), 0, 0, 0);
            } else if (rectF.right - ((rectF.right - rectF.left) / 2.0f) > ((float) this.mPadWidth) / 2.0f) {
                toRight = (int) (((((float) width) - rectF.right) + ((rectF.right - rectF.left) / 2.0f)) - ((float) (this.mPadWidth / 2)));
                this.mLl_arrow.setPadding((int) ((((float) this.mPadWidth) / 2.0f) - (((float) this.mIv_arrow_bottom.getMeasuredWidth()) / 2.0f)), 0, 0, 0);
            } else {
                toRight = width - this.mPadWidth;
                if (rectF.right - ((rectF.right - rectF.left) / 2.0f) > ((float) this.mIv_arrow_bottom.getMeasuredWidth()) / 2.0f) {
                    this.mLl_arrow.setPadding((int) ((rectF.right - ((rectF.right - rectF.left) / 2.0f)) - (((float) this.mIv_arrow_bottom.getMeasuredWidth()) / 2.0f)), 0, 0, 0);
                } else {
                    this.mLl_arrow.setPadding(0, 0, 0, 0);
                }
            }
            this.mPopupWindow.update(toRight, (int) (((float) height) - rectF.top), -1, -1);
            return;
        }
        this.mPopupWindow.update(0, 0, width, -1);
    }
}
