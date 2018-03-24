package com.foxit.uiextensions.controls.propertybar.imp;

import android.content.Context;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu.ClickListener;
import com.foxit.uiextensions.utils.AppDisplay;
import java.util.ArrayList;

public class AnnotMenuImpl implements AnnotMenu {
    private AppDisplay display;
    private Context mContext;
    private ClickListener mListener;
    private int mMaxWidth;
    private ArrayList<Integer> mMenuItems;
    private int mMinWidth;
    private ViewGroup mParent;
    private LinearLayout mPopView;
    private PopupWindow mPopupWindow;
    private boolean mShowing = false;

    public AnnotMenuImpl(Context context, ViewGroup parent) {
        this.mContext = context;
        this.mParent = parent;
        this.display = AppDisplay.getInstance(context);
        this.mMaxWidth = this.display.dp2px(5000.0f);
        this.mMinWidth = this.display.dp2px(80.0f);
    }

    public void setMenuItems(ArrayList<Integer> menuItems) {
        this.mMenuItems = menuItems;
        initView();
    }

    private void initView() {
        if (this.mPopView == null) {
            this.mPopView = new LinearLayout(this.mContext);
            this.mPopView.setLayoutParams(new LayoutParams(-2, -2));
            this.mPopView.setOrientation(1);
            this.mPopView.setBackgroundResource(R.drawable.am_popup_bg);
        } else {
            this.mPopView.removeAllViews();
        }
        for (int i = 0; i < this.mMenuItems.size(); i++) {
            if (i > 0) {
                ImageView separate = new ImageView(this.mContext);
                separate.setLayoutParams(new LinearLayout.LayoutParams(-2, this.display.dp2px(1.0f)));
                separate.setImageResource(R.color.ux_color_seperator_gray);
                this.mPopView.addView(separate);
            }
            if (((Integer) this.mMenuItems.get(i)).intValue() == 1) {
                addSubMenu(((Integer) this.mMenuItems.get(i)).intValue(), this.mContext.getResources().getString(R.string.rd_am_item_copy_text));
            } else if (((Integer) this.mMenuItems.get(i)).intValue() == 7) {
                addSubMenu(((Integer) this.mMenuItems.get(i)).intValue(), this.mContext.getResources().getString(R.string.fx_string_highlight));
            } else if (((Integer) this.mMenuItems.get(i)).intValue() == 8) {
                addSubMenu(((Integer) this.mMenuItems.get(i)).intValue(), this.mContext.getResources().getString(R.string.fx_string_underline));
            } else if (((Integer) this.mMenuItems.get(i)).intValue() == 9) {
                addSubMenu(((Integer) this.mMenuItems.get(i)).intValue(), this.mContext.getResources().getString(R.string.fx_string_strikeout));
            } else if (((Integer) this.mMenuItems.get(i)).intValue() == 10) {
                addSubMenu(((Integer) this.mMenuItems.get(i)).intValue(), this.mContext.getResources().getString(R.string.fx_string_squiggly));
            } else if (((Integer) this.mMenuItems.get(i)).intValue() == 5) {
                addSubMenu(((Integer) this.mMenuItems.get(i)).intValue(), this.mContext.getResources().getString(R.string.fx_string_edit));
            } else if (((Integer) this.mMenuItems.get(i)).intValue() == 6) {
                addSubMenu(((Integer) this.mMenuItems.get(i)).intValue(), this.mContext.getResources().getString(R.string.fx_am_style));
            } else if (((Integer) this.mMenuItems.get(i)).intValue() == 3) {
                addSubMenu(((Integer) this.mMenuItems.get(i)).intValue(), this.mContext.getResources().getString(R.string.fx_string_open));
            } else if (((Integer) this.mMenuItems.get(i)).intValue() == 4) {
                addSubMenu(((Integer) this.mMenuItems.get(i)).intValue(), this.mContext.getResources().getString(R.string.fx_string_reply));
            } else if (((Integer) this.mMenuItems.get(i)).intValue() == 2) {
                addSubMenu(((Integer) this.mMenuItems.get(i)).intValue(), this.mContext.getResources().getString(R.string.fx_string_delete));
            } else if (((Integer) this.mMenuItems.get(i)).intValue() == 11) {
                addSubMenu(((Integer) this.mMenuItems.get(i)).intValue(), this.mContext.getResources().getString(R.string.fx_string_note));
            } else if (((Integer) this.mMenuItems.get(i)).intValue() == 12) {
                addSubMenu(((Integer) this.mMenuItems.get(i)).intValue(), this.mContext.getResources().getString(R.string.rd_am_item_add_sign));
            } else if (((Integer) this.mMenuItems.get(i)).intValue() == 14) {
                addSubMenu(((Integer) this.mMenuItems.get(i)).intValue(), this.mContext.getResources().getString(R.string.fx_string_signature));
            } else if (((Integer) this.mMenuItems.get(i)).intValue() == 15) {
                addSubMenu(((Integer) this.mMenuItems.get(i)).intValue(), this.mContext.getResources().getString(R.string.fx_string_cancel));
            } else if (((Integer) this.mMenuItems.get(i)).intValue() == 16) {
                addSubMenu(((Integer) this.mMenuItems.get(i)).intValue(), this.mContext.getResources().getString(R.string.rv_security_dsg_verify));
            }
        }
        setMenuWidth();
        if (this.mPopupWindow == null) {
            this.mPopupWindow = new PopupWindow(this.mPopView, -2, -2);
            this.mPopupWindow.setTouchable(true);
            this.mPopupWindow.setBackgroundDrawable(new ColorDrawable(0));
        }
        setShowAlways(false);
    }

    public void setShowAlways(boolean showAlways) {
        if (showAlways) {
            this.mPopupWindow.setFocusable(false);
            this.mPopupWindow.setOutsideTouchable(false);
            return;
        }
        this.mPopupWindow.setFocusable(false);
        this.mPopupWindow.setOutsideTouchable(false);
    }

    private void addSubMenu(int menuTag, String text) {
        TextView textView = new TextView(this.mContext);
        textView.setLayoutParams(new LinearLayout.LayoutParams(-2, this.display.dp2px(56.0f)));
        textView.setText(text);
        textView.setTypeface(Typeface.DEFAULT);
        textView.setTextSize(14.0f);
        textView.setTextColor(this.mContext.getResources().getColor(R.color.ux_color_dark));
        textView.setGravity(19);
        textView.setPadding(this.display.dp2px(8.0f), this.display.dp2px(5.0f), this.display.dp2px(8.0f), this.display.dp2px(5.0f));
        textView.setBackgroundResource(R.drawable.am_tv_bg_selector);
        textView.setSingleLine(true);
        textView.setEllipsize(TruncateAt.END);
        textView.setTag(Integer.valueOf(menuTag));
        textView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                int tag = ((Integer) v.getTag()).intValue();
                if (AnnotMenuImpl.this.mListener != null) {
                    AnnotMenuImpl.this.mListener.onAMClick(tag);
                }
            }
        });
        this.mPopView.addView(textView);
    }

    private void setMenuWidth() {
        int width = getMenuWidth();
        for (int i = 0; i < this.mMenuItems.size(); i++) {
            if (i > 0) {
                ImageView separate = (ImageView) this.mPopView.getChildAt((i * 2) - 1);
                LinearLayout.LayoutParams separateParams = (LinearLayout.LayoutParams) separate.getLayoutParams();
                if (!(separateParams == null || separate == null)) {
                    separateParams.width = width;
                    separate.setLayoutParams(separateParams);
                }
            }
            TextView textView = (TextView) this.mPopView.getChildAt(i * 2);
            if (textView != null) {
                textView.setWidth(width);
                textView.setMaxWidth(this.mMaxWidth);
            }
        }
    }

    private int getMenuWidth() {
        int realShowWidth = 0;
        for (int i = 0; i < this.mMenuItems.size(); i++) {
            TextView textView = (TextView) this.mPopView.getChildAt(i * 2);
            if (textView != null) {
                textView.measure(0, 0);
                if (textView.getMeasuredWidth() >= this.mMaxWidth) {
                    realShowWidth = this.mMaxWidth;
                    break;
                } else if (textView.getMeasuredWidth() > realShowWidth) {
                    realShowWidth = textView.getMeasuredWidth();
                }
            }
        }
        if (realShowWidth == 0 || realShowWidth < this.mMinWidth) {
            return this.mMinWidth;
        }
        return realShowWidth;
    }

    public PopupWindow getPopupWindow() {
        return this.mPopupWindow;
    }

    public void show(RectF rectF) {
        if (this.mMenuItems != null && this.mMenuItems.size() > 0) {
            int space = this.display.dp2px(10.0f);
            RectF expandRectF = new RectF(rectF.left - ((float) space), rectF.top - ((float) space), rectF.right + ((float) space), rectF.bottom + ((float) space));
            RelativeLayout view = this.mParent;
            int height = view.getHeight();
            int width = view.getWidth();
            if (this.mPopupWindow != null && !this.mPopupWindow.isShowing()) {
                int right = this.mParent.getWidth();
                int bottom = this.mParent.getHeight();
                if (RectF.intersects(rectF, new RectF((float) 0, (float) 0, (float) right, (float) this.mParent.getHeight()))) {
                    this.mPopupWindow.getContentView().measure(0, 0);
                    if (expandRectF.top >= ((float) this.mPopupWindow.getContentView().getMeasuredHeight())) {
                        this.mPopupWindow.showAtLocation(view, 51, (int) ((expandRectF.right - ((expandRectF.right - expandRectF.left) / 2.0f)) - ((float) (this.mPopupWindow.getContentView().getMeasuredWidth() / 2))), (int) (expandRectF.top - ((float) this.mPopupWindow.getContentView().getMeasuredHeight())));
                    } else if (((float) height) - expandRectF.bottom >= ((float) this.mPopupWindow.getContentView().getMeasuredHeight())) {
                        this.mPopupWindow.showAtLocation(view, 51, (int) ((expandRectF.right - ((expandRectF.right - expandRectF.left) / 2.0f)) - ((float) (this.mPopupWindow.getContentView().getMeasuredWidth() / 2))), (int) expandRectF.bottom);
                    } else if (((float) width) - expandRectF.right >= ((float) this.mPopupWindow.getContentView().getMeasuredWidth())) {
                        this.mPopupWindow.showAtLocation(view, 51, (int) expandRectF.right, (int) ((expandRectF.bottom - ((float) (this.mPopupWindow.getContentView().getMeasuredHeight() / 2))) - ((expandRectF.bottom - expandRectF.top) / 2.0f)));
                    } else if (expandRectF.left >= ((float) this.mPopupWindow.getContentView().getMeasuredWidth())) {
                        this.mPopupWindow.showAtLocation(view, 51, (int) (expandRectF.left - ((float) this.mPopupWindow.getContentView().getMeasuredWidth())), (int) ((expandRectF.bottom - ((float) (this.mPopupWindow.getContentView().getMeasuredHeight() / 2))) - ((expandRectF.bottom - expandRectF.top) / 2.0f)));
                    } else {
                        this.mPopupWindow.showAtLocation(view, 51, (int) ((expandRectF.right - ((expandRectF.right - expandRectF.left) / 2.0f)) - ((float) (this.mPopupWindow.getContentView().getMeasuredWidth() / 2))), (int) ((expandRectF.bottom - ((float) (this.mPopupWindow.getContentView().getMeasuredHeight() / 2))) - ((expandRectF.bottom - expandRectF.top) / 2.0f)));
                    }
                }
                this.mShowing = true;
            }
        }
    }

    public void show(RectF rectF, int pageWidth, int pageHeight, boolean autoDismiss) {
        if (this.mMenuItems != null && this.mMenuItems.size() > 0) {
            int space = this.display.dp2px(10.0f);
            RectF expandRectF = new RectF(rectF.left - ((float) space), rectF.top - ((float) space), rectF.right + ((float) space), rectF.bottom + ((float) space));
            RelativeLayout view = this.mParent;
            int height = pageHeight;
            int width = pageWidth;
            if (this.mPopupWindow != null && !this.mPopupWindow.isShowing()) {
                if (RectF.intersects(rectF, new RectF((float) 0, (float) 0, (float) pageWidth, (float) pageHeight)) || !autoDismiss) {
                    this.mPopupWindow.getContentView().measure(0, 0);
                    if (expandRectF.top >= ((float) this.mPopupWindow.getContentView().getMeasuredHeight())) {
                        this.mPopupWindow.showAtLocation(view, 51, (int) ((expandRectF.right - ((expandRectF.right - expandRectF.left) / 2.0f)) - ((float) (this.mPopupWindow.getContentView().getMeasuredWidth() / 2))), (int) (expandRectF.top - ((float) this.mPopupWindow.getContentView().getMeasuredHeight())));
                    } else if (((float) height) - expandRectF.bottom >= ((float) this.mPopupWindow.getContentView().getMeasuredHeight())) {
                        this.mPopupWindow.showAtLocation(view, 51, (int) ((expandRectF.right - ((expandRectF.right - expandRectF.left) / 2.0f)) - ((float) (this.mPopupWindow.getContentView().getMeasuredWidth() / 2))), (int) expandRectF.bottom);
                    } else if (((float) width) - expandRectF.right >= ((float) this.mPopupWindow.getContentView().getMeasuredWidth())) {
                        this.mPopupWindow.showAtLocation(view, 51, (int) expandRectF.right, (int) ((expandRectF.bottom - ((float) (this.mPopupWindow.getContentView().getMeasuredHeight() / 2))) - ((expandRectF.bottom - expandRectF.top) / 2.0f)));
                    } else if (expandRectF.left >= ((float) this.mPopupWindow.getContentView().getMeasuredWidth())) {
                        this.mPopupWindow.showAtLocation(view, 51, (int) (expandRectF.left - ((float) this.mPopupWindow.getContentView().getMeasuredWidth())), (int) ((expandRectF.bottom - ((float) (this.mPopupWindow.getContentView().getMeasuredHeight() / 2))) - ((expandRectF.bottom - expandRectF.top) / 2.0f)));
                    } else {
                        this.mPopupWindow.showAtLocation(view, 51, (int) ((expandRectF.right - ((expandRectF.right - expandRectF.left) / 2.0f)) - ((float) (this.mPopupWindow.getContentView().getMeasuredWidth() / 2))), (int) ((expandRectF.bottom - ((float) (this.mPopupWindow.getContentView().getMeasuredHeight() / 2))) - ((expandRectF.bottom - expandRectF.top) / 2.0f)));
                    }
                }
                this.mShowing = true;
            }
        }
    }

    public void show(RectF rectF, View view) {
        if (this.mMenuItems != null && this.mMenuItems.size() > 0) {
            int space = this.display.dp2px(10.0f);
            RectF expandRectF = new RectF(rectF.left - ((float) space), rectF.top - ((float) space), rectF.right + ((float) space), rectF.bottom + ((float) space));
            int height = this.mParent.getHeight();
            int width = this.mParent.getWidth();
            if (this.mPopupWindow != null && !this.mPopupWindow.isShowing()) {
                this.mPopupWindow.getContentView().measure(0, 0);
                if (expandRectF.top >= ((float) this.mPopupWindow.getContentView().getMeasuredHeight())) {
                    this.mPopupWindow.showAtLocation(view, 51, (int) ((expandRectF.right - ((expandRectF.right - expandRectF.left) / 2.0f)) - ((float) (this.mPopupWindow.getContentView().getMeasuredWidth() / 2))), (int) (expandRectF.top - ((float) this.mPopupWindow.getContentView().getMeasuredHeight())));
                } else if (((float) height) - expandRectF.bottom >= ((float) this.mPopupWindow.getContentView().getMeasuredHeight())) {
                    this.mPopupWindow.showAtLocation(view, 51, (int) ((expandRectF.right - ((expandRectF.right - expandRectF.left) / 2.0f)) - ((float) (this.mPopupWindow.getContentView().getMeasuredWidth() / 2))), (int) expandRectF.bottom);
                } else if (((float) width) - expandRectF.right >= ((float) this.mPopupWindow.getContentView().getMeasuredWidth())) {
                    this.mPopupWindow.showAtLocation(view, 51, (int) expandRectF.right, (int) ((expandRectF.bottom - ((float) (this.mPopupWindow.getContentView().getMeasuredHeight() / 2))) - ((expandRectF.bottom - expandRectF.top) / 2.0f)));
                } else if (expandRectF.left >= ((float) this.mPopupWindow.getContentView().getMeasuredWidth())) {
                    this.mPopupWindow.showAtLocation(view, 51, (int) (expandRectF.left - ((float) this.mPopupWindow.getContentView().getMeasuredWidth())), (int) ((expandRectF.bottom - ((float) (this.mPopupWindow.getContentView().getMeasuredHeight() / 2))) - ((expandRectF.bottom - expandRectF.top) / 2.0f)));
                } else {
                    this.mPopupWindow.showAtLocation(view, 51, (int) ((expandRectF.right - ((expandRectF.right - expandRectF.left) / 2.0f)) - ((float) (this.mPopupWindow.getContentView().getMeasuredWidth() / 2))), (int) ((expandRectF.bottom - ((float) (this.mPopupWindow.getContentView().getMeasuredHeight() / 2))) - ((expandRectF.bottom - expandRectF.top) / 2.0f)));
                }
                this.mShowing = true;
            }
        }
    }

    public void update(RectF rectF) {
        if (this.mMenuItems != null && this.mMenuItems.size() > 0) {
            int space = this.display.dp2px(10.0f);
            RectF expandRectF = new RectF(rectF.left - ((float) space), rectF.top - ((float) space), rectF.right + ((float) space), rectF.bottom + ((float) space));
            int height = this.mParent.getHeight();
            int width = this.mParent.getWidth();
            if (expandRectF.top >= ((float) this.mPopupWindow.getContentView().getMeasuredHeight())) {
                this.mPopupWindow.update((int) ((expandRectF.right - ((expandRectF.right - expandRectF.left) / 2.0f)) - ((float) (this.mPopupWindow.getContentView().getMeasuredWidth() / 2))), (int) (expandRectF.top - ((float) this.mPopupWindow.getContentView().getMeasuredHeight())), -1, -1);
            } else if (((float) height) - expandRectF.bottom >= ((float) this.mPopupWindow.getContentView().getMeasuredHeight())) {
                this.mPopupWindow.update((int) ((expandRectF.right - ((expandRectF.right - expandRectF.left) / 2.0f)) - ((float) (this.mPopupWindow.getContentView().getMeasuredWidth() / 2))), (int) expandRectF.bottom, -1, -1);
            } else if (((float) width) - expandRectF.right >= ((float) this.mPopupWindow.getContentView().getMeasuredWidth())) {
                this.mPopupWindow.update((int) expandRectF.right, (int) ((expandRectF.bottom - ((float) (this.mPopupWindow.getContentView().getMeasuredHeight() / 2))) - ((expandRectF.bottom - expandRectF.top) / 2.0f)), -1, -1);
            } else if (expandRectF.left >= ((float) this.mPopupWindow.getContentView().getMeasuredWidth())) {
                this.mPopupWindow.update((int) (expandRectF.left - ((float) this.mPopupWindow.getContentView().getMeasuredWidth())), (int) ((expandRectF.bottom - ((float) (this.mPopupWindow.getContentView().getMeasuredHeight() / 2))) - ((expandRectF.bottom - expandRectF.top) / 2.0f)), -1, -1);
            } else {
                this.mPopupWindow.update((int) ((expandRectF.right - ((expandRectF.right - expandRectF.left) / 2.0f)) - ((float) (this.mPopupWindow.getContentView().getMeasuredWidth() / 2))), (int) ((expandRectF.bottom - ((float) (this.mPopupWindow.getContentView().getMeasuredHeight() / 2))) - ((expandRectF.bottom - expandRectF.top) / 2.0f)), -1, -1);
            }
            if (this.mShowing) {
                int right = this.mParent.getWidth();
                int bottom = this.mParent.getHeight();
                bottom = this.mParent.getHeight();
                if (isShowing()) {
                    if (rectF.bottom <= ((float) null) || rectF.right <= ((float) null) || rectF.left >= ((float) right) || rectF.top >= ((float) bottom)) {
                        this.mPopupWindow.dismiss();
                    }
                } else if (rectF.top >= ((float) null) && rectF.left >= ((float) null) && rectF.right <= ((float) right) && rectF.bottom <= ((float) bottom)) {
                    boolean showing = this.mShowing;
                    show(expandRectF);
                    this.mShowing = showing;
                }
            }
        }
    }

    public void update(RectF rectF, int pageWidth, int pageHeight, boolean autoDismiss) {
        if (this.mMenuItems != null && this.mMenuItems.size() > 0) {
            int space = this.display.dp2px(10.0f);
            RectF expandRectF = new RectF(rectF.left - ((float) space), rectF.top - ((float) space), rectF.right + ((float) space), rectF.bottom + ((float) space));
            int height = pageHeight;
            int width = pageWidth;
            if (expandRectF.top >= ((float) this.mPopupWindow.getContentView().getMeasuredHeight())) {
                this.mPopupWindow.update((int) ((expandRectF.right - ((expandRectF.right - expandRectF.left) / 2.0f)) - ((float) (this.mPopupWindow.getContentView().getMeasuredWidth() / 2))), (int) (expandRectF.top - ((float) this.mPopupWindow.getContentView().getMeasuredHeight())), -1, -1);
            } else if (((float) height) - expandRectF.bottom >= ((float) this.mPopupWindow.getContentView().getMeasuredHeight())) {
                this.mPopupWindow.update((int) ((expandRectF.right - ((expandRectF.right - expandRectF.left) / 2.0f)) - ((float) (this.mPopupWindow.getContentView().getMeasuredWidth() / 2))), (int) expandRectF.bottom, -1, -1);
            } else if (((float) width) - expandRectF.right >= ((float) this.mPopupWindow.getContentView().getMeasuredWidth())) {
                this.mPopupWindow.update((int) expandRectF.right, (int) ((expandRectF.bottom - ((float) (this.mPopupWindow.getContentView().getMeasuredHeight() / 2))) - ((expandRectF.bottom - expandRectF.top) / 2.0f)), -1, -1);
            } else if (expandRectF.left >= ((float) this.mPopupWindow.getContentView().getMeasuredWidth())) {
                this.mPopupWindow.update((int) (expandRectF.left - ((float) this.mPopupWindow.getContentView().getMeasuredWidth())), (int) ((expandRectF.bottom - ((float) (this.mPopupWindow.getContentView().getMeasuredHeight() / 2))) - ((expandRectF.bottom - expandRectF.top) / 2.0f)), -1, -1);
            } else {
                this.mPopupWindow.update((int) ((expandRectF.right - ((expandRectF.right - expandRectF.left) / 2.0f)) - ((float) (this.mPopupWindow.getContentView().getMeasuredWidth() / 2))), (int) ((expandRectF.bottom - ((float) (this.mPopupWindow.getContentView().getMeasuredHeight() / 2))) - ((expandRectF.bottom - expandRectF.top) / 2.0f)), -1, -1);
            }
            if (autoDismiss && this.mShowing) {
                int right = pageHeight;
                int bottom = pageWidth;
                if (isShowing()) {
                    if (rectF.bottom <= ((float) null) || rectF.right <= ((float) null) || rectF.left >= ((float) right) || rectF.top >= ((float) bottom)) {
                        this.mPopupWindow.dismiss();
                    }
                } else if (rectF.top >= ((float) null) && rectF.left >= ((float) null) && rectF.right <= ((float) right) && rectF.bottom <= ((float) bottom)) {
                    boolean showing = this.mShowing;
                    show(expandRectF, pageWidth, pageHeight, autoDismiss);
                    this.mShowing = showing;
                }
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
        if (this.mPopupWindow != null && this.mPopupWindow.isShowing()) {
            this.mPopupWindow.dismiss();
            this.mShowing = false;
        }
    }

    public void setListener(ClickListener listener) {
        this.mListener = listener;
    }
}
