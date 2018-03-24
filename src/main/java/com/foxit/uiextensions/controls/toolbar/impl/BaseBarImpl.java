package com.foxit.uiextensions.controls.toolbar.impl;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.controls.toolbar.BaseBar;
import com.foxit.uiextensions.controls.toolbar.BaseBar.TB_Position;
import com.foxit.uiextensions.controls.toolbar.BaseItem;
import com.foxit.uiextensions.utils.AppDisplay;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class BaseBarImpl implements BaseBar {
    protected LayoutParams mCenterItemParams_HORIZONTAL;
    protected LayoutParams mCenterItemParams_VERTICAL;
    protected LinearLayout mCenterLayout;
    protected RelativeLayout.LayoutParams mCenterLayoutParams_HORIZONTAL;
    protected RelativeLayout.LayoutParams mCenterLayoutParams_VERTICAL;
    protected ArrayList<BaseItem> mCenter_Items;
    protected View mContentView;
    protected Context mContext;
    protected int mDefaultIntervalSpace;
    protected int mDefaultLength;
    protected int mDefaultSpace;
    protected int mDefaultWide;
    protected int mDefaultWide_HORIZONTAL;
    protected int mDefaultWide_VERTICAL;
    protected LayoutParams mEndParams;
    protected boolean mInterval;
    protected int mIntervalSpace;
    protected boolean mIsPad;
    private boolean mIsRefreshLayout;
    protected ComparatorTBItemByIndex mItemComparator;
    protected LayoutParams mLTItemParams_HORIZONTAL;
    protected LayoutParams mLTItemParams_VERTICAL;
    protected LinearLayout mLTLayout;
    protected RelativeLayout.LayoutParams mLTLayoutParams_HORIZONTAL;
    protected RelativeLayout.LayoutParams mLTLayoutParams_VERTICAL;
    protected ArrayList<BaseItem> mLT_Items;
    private String mName;
    protected boolean mNeedResetItemSize;
    protected int mOrientation;
    protected LayoutParams mRBItemParams_HORIZONTAL;
    protected LayoutParams mRBItemParams_VERTICAL;
    protected LinearLayout mRBLayout;
    protected RelativeLayout.LayoutParams mRBLayoutParams_HORIZONTAL;
    protected RelativeLayout.LayoutParams mRBLayoutParams_VERTICAL;
    protected ArrayList<BaseItem> mRB_Items;
    protected BarRelativeLayoutImpl mRootLayout;
    protected RelativeLayout.LayoutParams mRootParams_HORIZONTAL;
    protected RelativeLayout.LayoutParams mRootParams_VERTICAL;

    private class ComparatorTBItemByIndex implements Comparator<Object> {
        private ComparatorTBItemByIndex() {
        }

        public int compare(Object lhs, Object rhs) {
            if (!(lhs instanceof BaseItem) || !(rhs instanceof BaseItem)) {
                return 0;
            }
            return ((BaseItem) lhs).getTag() - ((BaseItem) rhs).getTag();
        }
    }

    public BaseBarImpl(Context context) {
        this(context, 0, 0, 0, false);
    }

    protected BaseBarImpl(Context context, int orientation) {
        this(context, orientation, 0, 0, false);
    }

    protected BaseBarImpl(Context context, int orientation, boolean interval) {
        this(context, orientation, 0, 0, interval);
    }

    protected BaseBarImpl(Context context, int orientation, int length, int wide, boolean interval) {
        this.mDefaultWide_HORIZONTAL = 60;
        this.mDefaultWide_VERTICAL = 60;
        this.mDefaultLength = -1;
        this.mDefaultSpace = 18;
        this.mDefaultIntervalSpace = 16;
        this.mIntervalSpace = 16;
        this.mIsRefreshLayout = false;
        this.mInterval = false;
        this.mNeedResetItemSize = false;
        this.mIsPad = false;
        this.mContext = null;
        this.mContext = context;
        if (checkPad(context)) {
            this.mIsPad = true;
        } else {
            this.mIsPad = false;
        }
        initDimens();
        if (wide != 0) {
            this.mDefaultWide = wide;
        } else if (orientation == 0) {
            this.mDefaultWide = dip2px_fromDimens(this.mDefaultWide_HORIZONTAL);
        } else {
            this.mDefaultWide = dip2px_fromDimens(this.mDefaultWide_VERTICAL);
        }
        if (length != 0) {
            this.mDefaultLength = length;
        } else {
            this.mDefaultLength = dip2px(this.mDefaultLength);
        }
        this.mDefaultSpace = dip2px_fromDimens(this.mDefaultSpace);
        this.mDefaultIntervalSpace = dip2px_fromDimens(this.mDefaultIntervalSpace);
        this.mInterval = interval;
        this.mOrientation = orientation;
        this.mRootLayout = new BarRelativeLayoutImpl(context, this);
        this.mLTLayout = new LinearLayout(context);
        this.mCenterLayout = new LinearLayout(context);
        this.mRBLayout = new LinearLayout(context);
        this.mRootLayout.addView(this.mLTLayout);
        this.mRootLayout.addView(this.mCenterLayout);
        this.mRootLayout.addView(this.mRBLayout);
        this.mLT_Items = new ArrayList();
        this.mCenter_Items = new ArrayList();
        this.mRB_Items = new ArrayList();
        initOrientation(orientation);
        this.mItemComparator = new ComparatorTBItemByIndex();
    }

    protected boolean checkPad(Context context) {
        return AppDisplay.getInstance(context).isPad();
    }

    public void addView(BaseItem item, TB_Position position) {
        if (item != null) {
            if (this.mContentView != null) {
                this.mRootLayout.removeView(this.mContentView);
                this.mContentView = null;
            }
            if (!this.mInterval) {
                if (!this.mInterval) {
                    if (this.mOrientation == 0) {
                        this.mRootLayout.setPadding(this.mDefaultIntervalSpace, 0, this.mDefaultIntervalSpace, 0);
                    } else {
                        this.mRootLayout.setPadding(0, this.mDefaultIntervalSpace, 0, this.mDefaultIntervalSpace);
                    }
                }
                if (TB_Position.Position_LT.equals(position)) {
                    if (!this.mLT_Items.contains(item)) {
                        sortItems(item, this.mLT_Items);
                        resetItemSize(this.mLT_Items);
                        if (this.mOrientation == 0) {
                            this.mLTItemParams_HORIZONTAL.setMargins(0, 0, this.mDefaultSpace, 0);
                            resetItemsLayout(this.mLT_Items, this.mLTLayout, this.mLTItemParams_HORIZONTAL);
                            return;
                        }
                        this.mLTItemParams_VERTICAL.setMargins(0, 0, 0, this.mDefaultSpace);
                        resetItemsLayout(this.mLT_Items, this.mLTLayout, this.mLTItemParams_VERTICAL);
                    }
                } else if (TB_Position.Position_CENTER.equals(position)) {
                    if (!this.mCenter_Items.contains(item)) {
                        sortItems(item, this.mCenter_Items);
                        resetItemSize(this.mCenter_Items);
                        if (this.mOrientation == 0) {
                            this.mCenterItemParams_HORIZONTAL.setMargins(0, 0, this.mDefaultSpace, 0);
                            resetItemsLayout(this.mCenter_Items, this.mCenterLayout, this.mCenterItemParams_HORIZONTAL);
                            return;
                        }
                        this.mCenterItemParams_VERTICAL.setMargins(0, 0, 0, this.mDefaultSpace);
                        resetItemsLayout(this.mCenter_Items, this.mCenterLayout, this.mCenterItemParams_VERTICAL);
                    }
                } else if (TB_Position.Position_RB.equals(position) && !this.mRB_Items.contains(item)) {
                    sortItems(item, this.mRB_Items);
                    resetItemSize(this.mRB_Items);
                    if (this.mOrientation == 0) {
                        this.mRBItemParams_HORIZONTAL.setMargins(this.mDefaultSpace, 0, 0, 0);
                        resetItemsLayout(this.mRB_Items, this.mRBLayout, this.mRBItemParams_HORIZONTAL);
                        return;
                    }
                    this.mRBItemParams_VERTICAL.setMargins(0, this.mDefaultSpace, 0, 0);
                    resetItemsLayout(this.mRB_Items, this.mRBLayout, this.mRBItemParams_VERTICAL);
                }
            } else if (!this.mCenter_Items.contains(item)) {
                sortItems(item, this.mCenter_Items);
                resetItemSize(this.mCenter_Items);
                if (this.mOrientation == 0) {
                    if (this.mDefaultLength <= 0) {
                        this.mCenterItemParams_HORIZONTAL.setMargins(0, 0, this.mDefaultSpace, 0);
                        resetItemsLayout(this.mCenter_Items, this.mCenterLayout, this.mCenterItemParams_HORIZONTAL);
                        return;
                    }
                    this.mCenterLayout.setPadding(this.mDefaultIntervalSpace, 0, this.mDefaultIntervalSpace, 0);
                    this.mCenterItemParams_HORIZONTAL.setMargins(0, 0, marginsItemSpace(this.mOrientation, 0), 0);
                    resetIntervalItemsLayout(this.mCenter_Items, this.mCenterLayout, this.mCenterItemParams_HORIZONTAL);
                } else if (this.mDefaultLength <= 0) {
                    this.mCenterItemParams_VERTICAL.setMargins(0, 0, 0, this.mDefaultSpace);
                    resetItemsLayout(this.mCenter_Items, this.mCenterLayout, this.mCenterItemParams_VERTICAL);
                } else {
                    this.mCenterLayout.setPadding(0, this.mDefaultIntervalSpace, 0, this.mDefaultIntervalSpace);
                    this.mCenterItemParams_VERTICAL.setMargins(0, 0, 0, marginsItemSpace(this.mOrientation, 0));
                    resetIntervalItemsLayout(this.mCenter_Items, this.mCenterLayout, this.mCenterItemParams_VERTICAL);
                }
            }
        }
    }

    private void resetItemSize(ArrayList<BaseItem> items) {
        if (this.mNeedResetItemSize) {
            BaseItem item;
            int maxH = 0;
            int maxW = 0;
            Iterator it = items.iterator();
            while (it.hasNext()) {
                item = (BaseItem) it.next();
                item.getContentView().measure(0, 0);
                if (item.getContentView().getMeasuredHeight() > maxH) {
                    maxH = item.getContentView().getMeasuredHeight();
                }
                if (item.getContentView().getMeasuredWidth() > maxW) {
                    maxW = item.getContentView().getMeasuredWidth();
                }
            }
            it = items.iterator();
            while (it.hasNext()) {
                item = (BaseItem) it.next();
                item.getContentView().setMinimumHeight(maxH);
                item.getContentView().setMinimumWidth(maxW);
            }
        }
    }

    protected int marginsItemSpace(int orientation, int newLength) {
        int itemSpace;
        int itemsWidth;
        int i;
        int itemsHeight;
        if (AppDisplay.getInstance(this.mContext).isPad()) {
            if (orientation == 0) {
                itemSpace = 0;
                itemsWidth = 0;
                int lastItemWidth = 0;
                if (this.mCenter_Items.size() >= 2) {
                    for (i = 0; i < this.mCenter_Items.size(); i++) {
                        ((BaseItem) this.mCenter_Items.get(i)).getContentView().measure(0, 0);
                        itemsWidth += ((BaseItem) this.mCenter_Items.get(i)).getContentView().getMeasuredWidth();
                        if (i == this.mCenter_Items.size() - 1) {
                            lastItemWidth = ((BaseItem) this.mCenter_Items.get(i)).getContentView().getMeasuredWidth();
                        }
                    }
                    if ((((itemsWidth - lastItemWidth) * 4) + lastItemWidth) + (this.mDefaultIntervalSpace * 2) < newLength) {
                        itemSpace = (itemsWidth / this.mCenter_Items.size()) * 3;
                        this.mIntervalSpace = (newLength - (((this.mCenter_Items.size() - 1) * itemSpace) + itemsWidth)) / 2;
                    } else {
                        itemSpace = ((newLength - (this.mDefaultIntervalSpace * 2)) - itemsWidth) / (this.mCenter_Items.size() - 1);
                        this.mIntervalSpace = this.mDefaultIntervalSpace;
                    }
                }
                return itemSpace;
            }
            itemSpace = 0;
            itemsHeight = 0;
            int lastItemHeight = 0;
            if (this.mCenter_Items.size() >= 2) {
                for (i = 0; i < this.mCenter_Items.size(); i++) {
                    ((BaseItem) this.mCenter_Items.get(i)).getContentView().measure(0, 0);
                    itemsHeight += ((BaseItem) this.mCenter_Items.get(i)).getContentView().getMeasuredHeight();
                    if (i == this.mCenter_Items.size() - 1) {
                        lastItemHeight = ((BaseItem) this.mCenter_Items.get(i)).getContentView().getMeasuredHeight();
                    }
                }
                if ((((itemsHeight - lastItemHeight) * 4) + lastItemHeight) + (this.mDefaultIntervalSpace * 2) < newLength) {
                    itemSpace = (itemsHeight / this.mCenter_Items.size()) * 3;
                    this.mIntervalSpace = (newLength - (((this.mCenter_Items.size() - 1) * itemSpace) + itemsHeight)) / 2;
                } else {
                    itemSpace = ((newLength - (this.mDefaultIntervalSpace * 2)) - itemsHeight) / (this.mCenter_Items.size() - 1);
                    this.mIntervalSpace = this.mDefaultIntervalSpace;
                }
            }
            return itemSpace;
        } else if (orientation == 0) {
            itemSpace = 0;
            itemsWidth = 0;
            if (this.mCenter_Items.size() >= 2) {
                for (i = 0; i < this.mCenter_Items.size(); i++) {
                    ((BaseItem) this.mCenter_Items.get(i)).getContentView().measure(0, 0);
                    itemsWidth += ((BaseItem) this.mCenter_Items.get(i)).getContentView().getMeasuredWidth();
                }
                itemSpace = ((newLength - (this.mDefaultIntervalSpace * 2)) - itemsWidth) / (this.mCenter_Items.size() - 1);
                this.mIntervalSpace = this.mDefaultIntervalSpace;
            }
            return itemSpace;
        } else {
            itemSpace = 0;
            itemsHeight = 0;
            if (this.mCenter_Items.size() >= 2) {
                for (i = 0; i < this.mCenter_Items.size(); i++) {
                    ((BaseItem) this.mCenter_Items.get(i)).getContentView().measure(0, 0);
                    itemsHeight += ((BaseItem) this.mCenter_Items.get(i)).getContentView().getMeasuredHeight();
                }
                itemSpace = ((newLength - (this.mDefaultIntervalSpace * 2)) - itemsHeight) / (this.mCenter_Items.size() - 1);
                this.mIntervalSpace = this.mDefaultIntervalSpace;
            }
            return itemSpace;
        }
    }

    protected void sortItems(BaseItem item, ArrayList<BaseItem> items) {
        items.add(item);
        Collections.sort(items, this.mItemComparator);
    }

    protected void resetItemsLayout(ArrayList<BaseItem> items, LinearLayout layout, LayoutParams itemParams) {
        if (items != null && !items.isEmpty() && layout != null) {
            layout.removeAllViews();
            if (this.mRBLayout == layout) {
                Iterator it = items.iterator();
                while (it.hasNext()) {
                    layout.addView(((BaseItem) it.next()).getContentView(), itemParams);
                }
                return;
            }
            for (int i = 0; i < items.size(); i++) {
                if (i == items.size() - 1) {
                    layout.addView(((BaseItem) items.get(i)).getContentView(), this.mEndParams);
                } else {
                    layout.addView(((BaseItem) items.get(i)).getContentView(), itemParams);
                }
            }
        }
    }

    protected void resetIntervalItemsLayout(ArrayList<BaseItem> items, LinearLayout layout, LayoutParams itemParams) {
        if (items != null && !items.isEmpty() && layout != null) {
            layout.removeAllViews();
            int i = 0;
            while (i < items.size()) {
                if (this.mInterval && i == items.size() - 1) {
                    layout.addView(((BaseItem) items.get(i)).getContentView(), this.mEndParams);
                } else {
                    layout.addView(((BaseItem) items.get(i)).getContentView(), itemParams);
                }
                i++;
            }
        }
    }

    public boolean removeItemByTag(int tag) {
        Iterator it;
        BaseItem item;
        if (!this.mCenter_Items.isEmpty()) {
            it = this.mCenter_Items.iterator();
            while (it.hasNext()) {
                item = (BaseItem) it.next();
                if (item.getTag() == tag) {
                    this.mCenterLayout.removeView(item.getContentView());
                    return this.mCenter_Items.remove(item);
                }
            }
        }
        if (!this.mInterval) {
            if (!this.mLT_Items.isEmpty()) {
                it = this.mLT_Items.iterator();
                while (it.hasNext()) {
                    item = (BaseItem) it.next();
                    if (item.getTag() == tag) {
                        this.mLTLayout.removeView(item.getContentView());
                        return this.mLT_Items.remove(item);
                    }
                }
            }
            if (!this.mRB_Items.isEmpty()) {
                it = this.mRB_Items.iterator();
                while (it.hasNext()) {
                    item = (BaseItem) it.next();
                    if (item.getTag() == tag) {
                        this.mRBLayout.removeView(item.getContentView());
                        return this.mRB_Items.remove(item);
                    }
                }
            }
        }
        return false;
    }

    public boolean removeItemByItem(BaseItem item) {
        if (this.mCenter_Items.contains(item)) {
            this.mCenterLayout.removeView(item.getContentView());
            return this.mCenter_Items.remove(item);
        }
        if (!this.mInterval) {
            if (this.mLT_Items.contains(item)) {
                this.mLTLayout.removeView(item.getContentView());
                return this.mLT_Items.remove(item);
            } else if (this.mRB_Items.contains(item)) {
                this.mRBLayout.removeView(item.getContentView());
                return this.mRB_Items.remove(item);
            }
        }
        return false;
    }

    public void removeAllItems() {
        this.mLTLayout.removeAllViews();
        this.mCenterLayout.removeAllViews();
        this.mRBLayout.removeAllViews();
        this.mLT_Items.clear();
        this.mCenter_Items.clear();
        this.mRB_Items.clear();
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getName() {
        return this.mName;
    }

    public void setBarVisible(boolean visible) {
        if (visible) {
            this.mRootLayout.setVisibility(0);
        } else {
            this.mRootLayout.setVisibility(4);
        }
    }

    public View getContentView() {
        return this.mRootLayout;
    }

    public void setOrientation(int orientation) {
        if (orientation == 0) {
            this.mDefaultWide = dip2px_fromDimens(this.mDefaultWide_HORIZONTAL);
        } else {
            this.mDefaultWide = dip2px_fromDimens(this.mDefaultWide_VERTICAL);
        }
        initOrientation(orientation);
    }

    protected void initOrientation(int orientation, int length, int wide) {
        this.mDefaultWide = wide;
        this.mDefaultLength = length;
        initOrientation(orientation);
    }

    protected void refreshLayout() {
        this.mIsRefreshLayout = true;
    }

    protected void initOrientation(int orientation) {
        this.mOrientation = orientation;
        if (orientation == 0) {
            if (this.mRootParams_HORIZONTAL == null || this.mIsRefreshLayout) {
                this.mRootParams_HORIZONTAL = new RelativeLayout.LayoutParams(this.mDefaultLength, this.mDefaultWide);
            }
            this.mRootLayout.setLayoutParams(this.mRootParams_HORIZONTAL);
            if (this.mLTLayoutParams_HORIZONTAL == null || this.mIsRefreshLayout) {
                this.mLTLayoutParams_HORIZONTAL = new RelativeLayout.LayoutParams(-2, this.mDefaultWide);
                this.mLTLayoutParams_HORIZONTAL.addRule(9);
            }
            if (this.mCenterLayoutParams_HORIZONTAL == null || this.mIsRefreshLayout) {
                this.mCenterLayoutParams_HORIZONTAL = new RelativeLayout.LayoutParams(-2, this.mDefaultWide);
                this.mCenterLayoutParams_HORIZONTAL.addRule(13);
            }
            if (this.mRBLayoutParams_HORIZONTAL == null || this.mIsRefreshLayout) {
                this.mRBLayoutParams_HORIZONTAL = new RelativeLayout.LayoutParams(-2, this.mDefaultWide);
                this.mRBLayoutParams_HORIZONTAL.addRule(11);
            }
            this.mLTLayout.setLayoutParams(this.mLTLayoutParams_HORIZONTAL);
            this.mCenterLayout.setLayoutParams(this.mCenterLayoutParams_HORIZONTAL);
            this.mRBLayout.setLayoutParams(this.mRBLayoutParams_HORIZONTAL);
            this.mLTLayout.setOrientation(0);
            this.mCenterLayout.setOrientation(0);
            this.mRBLayout.setOrientation(0);
            this.mEndParams = new LayoutParams(-2, this.mDefaultWide);
            if (this.mLTItemParams_HORIZONTAL == null || this.mIsRefreshLayout) {
                this.mLTItemParams_HORIZONTAL = new LayoutParams(-2, this.mDefaultWide);
                this.mLTItemParams_HORIZONTAL.setMargins(0, 0, this.mDefaultSpace, 0);
            }
            if (this.mInterval) {
                resetIntervalItemsLayout(this.mLT_Items, this.mLTLayout, this.mLTItemParams_HORIZONTAL);
            } else {
                resetItemsLayout(this.mLT_Items, this.mLTLayout, this.mLTItemParams_HORIZONTAL);
            }
            if (this.mCenterItemParams_HORIZONTAL == null || this.mIsRefreshLayout) {
                this.mCenterItemParams_HORIZONTAL = new LayoutParams(-2, this.mDefaultWide);
                this.mCenterItemParams_HORIZONTAL.setMargins(0, 0, this.mDefaultSpace, 0);
            }
            if (this.mInterval) {
                resetIntervalItemsLayout(this.mCenter_Items, this.mCenterLayout, this.mCenterItemParams_HORIZONTAL);
            } else {
                resetItemsLayout(this.mCenter_Items, this.mCenterLayout, this.mCenterItemParams_HORIZONTAL);
            }
            if (this.mRBItemParams_HORIZONTAL == null || this.mIsRefreshLayout) {
                this.mRBItemParams_HORIZONTAL = new LayoutParams(-2, this.mDefaultWide);
                this.mRBItemParams_HORIZONTAL.setMargins(this.mDefaultSpace, 0, 0, 0);
            }
            if (this.mInterval) {
                resetIntervalItemsLayout(this.mRB_Items, this.mRBLayout, this.mRBItemParams_HORIZONTAL);
            } else {
                resetItemsLayout(this.mRB_Items, this.mRBLayout, this.mRBItemParams_HORIZONTAL);
            }
        } else {
            if (this.mRootParams_VERTICAL == null || this.mIsRefreshLayout) {
                this.mRootParams_VERTICAL = new RelativeLayout.LayoutParams(this.mDefaultWide, this.mDefaultLength);
            }
            this.mRootLayout.setLayoutParams(this.mRootParams_VERTICAL);
            if (this.mLTLayoutParams_VERTICAL == null || this.mIsRefreshLayout) {
                this.mLTLayoutParams_VERTICAL = new RelativeLayout.LayoutParams(this.mDefaultWide, -2);
                this.mLTLayoutParams_VERTICAL.addRule(10);
            }
            if (this.mCenterLayoutParams_VERTICAL == null || this.mIsRefreshLayout) {
                this.mCenterLayoutParams_VERTICAL = new RelativeLayout.LayoutParams(this.mDefaultWide, -2);
                this.mCenterLayoutParams_VERTICAL.addRule(13);
            }
            if (this.mRBLayoutParams_VERTICAL == null || this.mIsRefreshLayout) {
                this.mRBLayoutParams_VERTICAL = new RelativeLayout.LayoutParams(this.mDefaultWide, -2);
                this.mRBLayoutParams_VERTICAL.addRule(12);
            }
            this.mLTLayout.setLayoutParams(this.mLTLayoutParams_VERTICAL);
            this.mCenterLayout.setLayoutParams(this.mCenterLayoutParams_VERTICAL);
            this.mRBLayout.setLayoutParams(this.mRBLayoutParams_VERTICAL);
            this.mLTLayout.setOrientation(1);
            this.mCenterLayout.setOrientation(1);
            this.mRBLayout.setOrientation(1);
            this.mEndParams = new LayoutParams(this.mDefaultWide, -2);
            if (this.mLTItemParams_VERTICAL == null || this.mIsRefreshLayout) {
                this.mLTItemParams_VERTICAL = new LayoutParams(this.mDefaultWide, -2);
                this.mLTItemParams_VERTICAL.setMargins(0, 0, 0, this.mDefaultSpace);
            }
            if (this.mInterval) {
                resetIntervalItemsLayout(this.mLT_Items, this.mLTLayout, this.mLTItemParams_VERTICAL);
            } else {
                resetItemsLayout(this.mLT_Items, this.mLTLayout, this.mLTItemParams_VERTICAL);
            }
            if (this.mCenterItemParams_VERTICAL == null || this.mIsRefreshLayout) {
                this.mCenterItemParams_VERTICAL = new LayoutParams(this.mDefaultWide, -2);
                this.mCenterItemParams_VERTICAL.setMargins(0, 0, 0, this.mDefaultSpace);
            }
            if (this.mInterval || this.mIsRefreshLayout) {
                resetIntervalItemsLayout(this.mCenter_Items, this.mCenterLayout, this.mCenterItemParams_VERTICAL);
            } else {
                resetItemsLayout(this.mCenter_Items, this.mCenterLayout, this.mCenterItemParams_VERTICAL);
            }
            if (this.mRBItemParams_VERTICAL == null || this.mIsRefreshLayout) {
                this.mRBItemParams_VERTICAL = new LayoutParams(this.mDefaultWide, -2);
                this.mRBItemParams_VERTICAL.setMargins(0, this.mDefaultSpace, 0, 0);
            }
            if (this.mInterval) {
                resetIntervalItemsLayout(this.mRB_Items, this.mRBLayout, this.mRBItemParams_VERTICAL);
            } else {
                resetItemsLayout(this.mRB_Items, this.mRBLayout, this.mRBItemParams_VERTICAL);
            }
        }
        this.mIsRefreshLayout = false;
    }

    public void setBackgroundColor(int color) {
        if (this.mRootLayout != null) {
            this.mRootLayout.setBackgroundColor(color);
        }
    }

    public void setBackgroundResource(int res) {
        if (this.mRootLayout != null) {
            this.mRootLayout.setBackgroundResource(res);
        }
    }

    public void setInterval(boolean interval) {
        this.mInterval = interval;
        if (interval) {
            this.mLTLayout.setVisibility(8);
            this.mRBLayout.setVisibility(8);
            this.mRootLayout.setPadding(0, 0, 0, 0);
            if (this.mOrientation == 0) {
                this.mCenterLayout.getLayoutParams().width = -1;
                this.mCenterLayout.getLayoutParams().height = this.mDefaultWide;
                this.mEndParams = new LayoutParams(-2, this.mDefaultWide);
                return;
            }
            this.mCenterLayout.getLayoutParams().width = this.mDefaultWide;
            this.mCenterLayout.getLayoutParams().height = -1;
            this.mEndParams = new LayoutParams(this.mDefaultWide, -2);
        }
    }

    public void setItemSpace(int space) {
        this.mDefaultSpace = space;
        if (!(this.mLT_Items == null || this.mLT_Items.isEmpty())) {
            if (this.mOrientation == 0) {
                this.mLTItemParams_HORIZONTAL.setMargins(0, 0, this.mDefaultSpace, 0);
                resetItemsLayout(this.mLT_Items, this.mLTLayout, this.mLTItemParams_HORIZONTAL);
            } else {
                this.mLTItemParams_VERTICAL.setMargins(0, 0, 0, this.mDefaultSpace);
                resetItemsLayout(this.mLT_Items, this.mLTLayout, this.mLTItemParams_VERTICAL);
            }
        }
        if (!(this.mCenter_Items == null || this.mCenter_Items.isEmpty())) {
            if (this.mOrientation == 0) {
                this.mCenterItemParams_HORIZONTAL.setMargins(0, 0, this.mDefaultSpace, 0);
                resetItemsLayout(this.mCenter_Items, this.mCenterLayout, this.mCenterItemParams_HORIZONTAL);
            } else {
                this.mCenterItemParams_VERTICAL.setMargins(0, 0, 0, this.mDefaultSpace);
                resetItemsLayout(this.mCenter_Items, this.mCenterLayout, this.mCenterItemParams_VERTICAL);
            }
        }
        if (this.mRB_Items != null && !this.mRB_Items.isEmpty()) {
            if (this.mOrientation == 0) {
                this.mRBItemParams_HORIZONTAL.setMargins(this.mDefaultSpace, 0, 0, 0);
                resetItemsLayout(this.mRB_Items, this.mRBLayout, this.mRBItemParams_HORIZONTAL);
                return;
            }
            this.mRBItemParams_VERTICAL.setMargins(0, this.mDefaultSpace, 0, 0);
            resetItemsLayout(this.mRB_Items, this.mRBLayout, this.mRBItemParams_VERTICAL);
        }
    }

    public void setWidth(int width) {
        this.mRootLayout.getLayoutParams().width = width;
    }

    public void setHeight(int height) {
        this.mRootLayout.getLayoutParams().height = height;
    }

    public void setContentView(View v) {
        removeAllItems();
        this.mRootLayout.setPadding(0, 0, 0, 0);
        this.mRootLayout.addView(v);
        this.mContentView = v;
    }

    public void setInterceptTouch(boolean isInterceptTouch) {
        this.mRootLayout.setInterceptTouch(isInterceptTouch);
    }

    public void setNeedResetItemSize(boolean needResetItemSize) {
        this.mNeedResetItemSize = needResetItemSize;
    }

    public void layout(int l, int t, int r, int b) {
        if (this.mInterval) {
            int w = Math.abs(r - l);
            int h = Math.abs(b - t);
            if (this.mOrientation == 0) {
                resetLength(w);
            } else {
                resetLength(h);
            }
        }
    }

    protected void resetLength(int newLength) {
        if (this.mOrientation == 0) {
            this.mCenterLayout.setGravity(16);
            this.mCenterItemParams_HORIZONTAL.setMargins(0, 0, marginsItemSpace(this.mOrientation, newLength), 0);
            this.mCenterLayout.setPadding(this.mIntervalSpace, 0, this.mIntervalSpace, 0);
            resetIntervalItemsLayout(this.mCenter_Items, this.mCenterLayout, this.mCenterItemParams_HORIZONTAL);
            return;
        }
        this.mCenterLayout.setGravity(1);
        this.mCenterItemParams_VERTICAL.setMargins(0, 0, 0, marginsItemSpace(this.mOrientation, newLength));
        this.mCenterLayout.setPadding(0, this.mIntervalSpace, 0, this.mIntervalSpace);
        resetIntervalItemsLayout(this.mCenter_Items, this.mCenterLayout, this.mCenterItemParams_VERTICAL);
    }

    private void initDimens() {
        if (this.mIsPad) {
            this.mDefaultWide_HORIZONTAL = this.mContext.getResources().getDimensionPixelOffset(R.dimen.ux_toolbar_height_pad);
        } else {
            this.mDefaultWide_HORIZONTAL = this.mContext.getResources().getDimensionPixelOffset(R.dimen.ux_toolbar_height_phone);
        }
        this.mDefaultWide_VERTICAL = this.mDefaultWide_HORIZONTAL;
        this.mDefaultSpace = this.mContext.getResources().getDimensionPixelOffset(R.dimen.ux_toolbar_button_interval);
        if (this.mIsPad) {
            this.mDefaultIntervalSpace = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_left_margin_pad);
        } else {
            this.mDefaultIntervalSpace = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_left_margin_phone);
        }
    }

    public void measure(int widthMeasureSpec, int heightMeasureSpec) {
    }

    protected int dip2px(int dip) {
        return dip <= 0 ? dip : AppDisplay.getInstance(this.mContext).dp2px((float) dip);
    }

    protected int dip2px_fromDimens(int dip) {
        return dip;
    }
}
