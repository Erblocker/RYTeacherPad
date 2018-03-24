package com.foxit.uiextensions.modules.signature;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.controls.toolbar.BaseBar;
import com.foxit.uiextensions.controls.toolbar.BaseBar.TB_Position;
import com.foxit.uiextensions.controls.toolbar.BaseItem;
import com.foxit.uiextensions.controls.toolbar.impl.BaseItemImpl;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppResource;
import com.foxit.uiextensions.utils.AppUtil;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SignatureListPicker {
    private SingListAdapter mAdapter;
    private Map<String, WeakReference<Bitmap>> mCacheMap = new HashMap();
    private Context mContext;
    private AppDisplay mDisplay;
    private ArrayList<SignatureInkItem> mDsgInkItems = new ArrayList();
    private ArrayList<SignatureInkItem> mHandwritingInkItems = new ArrayList();
    private ArrayList<SignatureInkItem> mInkBaseItems = new ArrayList();
    private SignatureInkCallback mInkCallback;
    private boolean mIsPad;
    private int mLeftSideInterval = 16;
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private int mRightSideInterval = 9;
    private View mRootView;
    private ISignListPickerDismissCallback mSignListPickerDismissCallback;
    private ArrayList<SignListPickerGroupItem> mSignListPickerGroupItems = new ArrayList();
    private ExpandableListView mSignListView;
    private BaseItem mSignPickerCloseItem;
    private BaseItem mSignPickerCreateItem;
    private BaseItem mSignPickerTitleItem;
    private BaseBar mSignPickerTopBar;

    static class ChildViewHolder {
        ImageView bitmap;
        LinearLayout delete;
        LinearLayout edit;
        ImageView menu;
        LinearLayout menu_layout;
        ImageView selectedImg;

        ChildViewHolder() {
        }
    }

    static class GroupViewHolder {
        View cuttingLine;
        TextView name;

        GroupViewHolder() {
        }
    }

    interface ISignListPickerDismissCallback {
        void onDismiss();
    }

    class SignListPickerGroupItem {
        ArrayList<SignatureInkItem> inkItems;
        String name;

        SignListPickerGroupItem() {
        }
    }

    class SingListAdapter extends BaseExpandableListAdapter {
        SingListAdapter() {
        }

        public int getGroupCount() {
            return SignatureListPicker.this.mSignListPickerGroupItems.size();
        }

        public int getChildrenCount(int groupPosition) {
            if (((SignListPickerGroupItem) SignatureListPicker.this.mSignListPickerGroupItems.get(groupPosition)).inkItems != null) {
                return ((SignListPickerGroupItem) SignatureListPicker.this.mSignListPickerGroupItems.get(groupPosition)).inkItems.size();
            }
            return 0;
        }

        public SignListPickerGroupItem getGroup(int groupPosition) {
            return (SignListPickerGroupItem) SignatureListPicker.this.mSignListPickerGroupItems.get(groupPosition);
        }

        public SignatureInkItem getChild(int groupPosition, int childPosition) {
            if (((SignListPickerGroupItem) SignatureListPicker.this.mSignListPickerGroupItems.get(groupPosition)).inkItems == null || ((SignListPickerGroupItem) SignatureListPicker.this.mSignListPickerGroupItems.get(groupPosition)).inkItems.size() == 0) {
                return null;
            }
            return (SignatureInkItem) ((SignListPickerGroupItem) SignatureListPicker.this.mSignListPickerGroupItems.get(groupPosition)).inkItems.get(childPosition);
        }

        public long getGroupId(int groupPosition) {
            return (long) groupPosition;
        }

        public long getChildId(int groupPosition, int childPosition) {
            return (long) childPosition;
        }

        public boolean hasStableIds() {
            return false;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupViewHolder gViewHolder;
            if (convertView == null) {
                gViewHolder = new GroupViewHolder();
                convertView = View.inflate(SignatureListPicker.this.mContext, R.layout.sign_list_group_item, null);
                gViewHolder.name = (TextView) convertView.findViewById(R.id.sign_list_group_name);
                gViewHolder.cuttingLine = convertView.findViewById(R.id.sign_list_group_item_cutting_line);
                gViewHolder.name.setPadding(SignatureListPicker.this.mLeftSideInterval, 0, SignatureListPicker.this.mRightSideInterval, 0);
                convertView.setTag(gViewHolder);
            } else {
                gViewHolder = (GroupViewHolder) convertView.getTag();
            }
            gViewHolder.name.setText(getGroup(groupPosition).name);
            if (getGroup(groupPosition).inkItems.size() == 0) {
                gViewHolder.cuttingLine.setVisibility(0);
            } else {
                gViewHolder.cuttingLine.setVisibility(4);
            }
            return convertView;
        }

        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ChildViewHolder cViewHolder;
            if (convertView == null) {
                cViewHolder = new ChildViewHolder();
                convertView = View.inflate(SignatureListPicker.this.mContext, R.layout.sign_list_listview_child_item, null);
                cViewHolder.selectedImg = (ImageView) convertView.findViewById(R.id.sign_list_item_selected);
                cViewHolder.bitmap = (ImageView) convertView.findViewById(R.id.sign_list_child_item_bitmap);
                cViewHolder.menu = (ImageView) convertView.findViewById(R.id.sign_list_child_menu_item);
                cViewHolder.menu_layout = (LinearLayout) convertView.findViewById(R.id.sign_list_child_menu_layout);
                cViewHolder.edit = (LinearLayout) convertView.findViewById(R.id.sign_list_child_edit_layout);
                cViewHolder.delete = (LinearLayout) convertView.findViewById(R.id.sign_list_child_item_delete_layout);
                cViewHolder.selectedImg.setPadding(SignatureListPicker.this.mLeftSideInterval, 0, 0, 0);
                cViewHolder.menu.setPadding(0, 0, SignatureListPicker.this.mRightSideInterval, 0);
                convertView.setTag(cViewHolder);
            } else {
                cViewHolder = (ChildViewHolder) convertView.getTag();
            }
            final SignatureInkItem inkItem = getChild(groupPosition, childPosition);
            if (inkItem == null) {
                return null;
            }
            if (inkItem.selected) {
                cViewHolder.selectedImg.setVisibility(0);
            } else {
                cViewHolder.selectedImg.setVisibility(4);
            }
            if (inkItem.isOpened) {
                cViewHolder.menu_layout.setVisibility(0);
            } else {
                cViewHolder.menu_layout.setVisibility(8);
            }
            WeakReference<Bitmap> reference = (WeakReference) SignatureListPicker.this.mCacheMap.get(inkItem.key);
            Bitmap bitmap = null;
            if (reference != null) {
                bitmap = (Bitmap) reference.get();
            }
            if (bitmap == null) {
                bitmap = SignatureListPicker.this.getBitmap(inkItem);
            }
            if (bitmap != null) {
                cViewHolder.bitmap.setImageBitmap(bitmap);
            }
            cViewHolder.menu.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!AppUtil.isFastDoubleClick()) {
                        SingListAdapter.this.updateMenuLayoutState(inkItem);
                    }
                }
            });
            cViewHolder.delete.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    SignatureDataUtil.deleteByKey(SignatureListPicker.this.mContext, SignatureConstants.getModelTableName(), inkItem.key);
                    SignatureListPicker.this.mInkBaseItems.remove(inkItem);
                    SignatureListPicker.this.mHandwritingInkItems.remove(inkItem);
                    SignatureListPicker.this.mDsgInkItems.remove(inkItem);
                    if (SignatureListPicker.this.mInkBaseItems.size() != 0) {
                        List<String> recent = SignatureDataUtil.getRecentKeys(SignatureListPicker.this.mContext);
                        String recentKey = recent == null ? null : (String) recent.get(0);
                        Iterator it = SignatureListPicker.this.mInkBaseItems.iterator();
                        while (it.hasNext()) {
                            SignatureInkItem item = (SignatureInkItem) it.next();
                            if (item.key.equals(recentKey)) {
                                item.selected = true;
                            } else {
                                item.selected = false;
                            }
                        }
                    }
                    SingListAdapter.this.notifyDataSetChanged();
                }
            });
            cViewHolder.edit.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!AppUtil.isFastDoubleClick()) {
                        SignatureListPicker.this.initItem(inkItem);
                        SignatureListPicker.this.mSignListPickerDismissCallback.onDismiss();
                        FragmentActivity act = (FragmentActivity) SignatureListPicker.this.mContext;
                        SignatureFragment fragment = (SignatureFragment) act.getSupportFragmentManager().findFragmentByTag("InkSignFragment");
                        if (fragment == null) {
                            fragment = new SignatureFragment();
                            fragment.init(SignatureListPicker.this.mContext, SignatureListPicker.this.mParent, SignatureListPicker.this.mPdfViewCtrl);
                        }
                        fragment.setInkCallback(SignatureListPicker.this.mInkCallback, inkItem);
                        if (fragment.isAdded()) {
                            act.getSupportFragmentManager().beginTransaction().attach(fragment);
                        } else {
                            act.getSupportFragmentManager().beginTransaction().add(R.id.rd_main_id, fragment, "InkSignFragment").addToBackStack(null).commitAllowingStateLoss();
                        }
                    }
                }
            });
            convertView.setMinimumHeight(SignatureListPicker.this.mDisplay.dp2px(100.0f));
            return convertView;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        private void updateMenuLayoutState(SignatureInkItem item) {
            for (int i = 0; i < SignatureListPicker.this.mInkBaseItems.size(); i++) {
                ((SignatureInkItem) SignatureListPicker.this.mInkBaseItems.get(i)).isOpened = false;
            }
            item.isOpened = true;
            notifyDataSetChanged();
        }
    }

    public SignatureListPicker(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl, SignatureInkCallback inkCallback) {
        this.mContext = context;
        this.mParent = parent;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mDisplay = AppDisplay.getInstance(this.mContext);
        this.mIsPad = this.mDisplay.isPad();
        this.mInkCallback = inkCallback;
        if (this.mIsPad) {
            this.mRootView = View.inflate(this.mContext, R.layout.sign_list_layout_pad, null);
        } else {
            this.mRootView = View.inflate(this.mContext, R.layout.sign_list_layout_phone, null);
        }
        initDimens();
        initTopBar();
        initData();
        initList();
    }

    public void init(ISignListPickerDismissCallback signListPickerDismissCallback) {
        this.mSignListPickerDismissCallback = signListPickerDismissCallback;
    }

    private void initDimens() {
        if (this.mIsPad) {
            this.mLeftSideInterval = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_left_margin_pad);
        } else {
            this.mLeftSideInterval = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_left_margin_phone);
        }
        if (this.mIsPad) {
            this.mRightSideInterval = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_right_margin_pad);
        } else {
            this.mRightSideInterval = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_right_margin_phone);
        }
    }

    private void initTopBar() {
        Context context = this.mContext;
        this.mSignPickerTopBar = new SignatureSignListBar(context);
        if (this.mIsPad) {
            this.mSignPickerTopBar.setBackgroundResource(R.drawable.dlg_title_bg_circle_corner_blue);
        } else {
            this.mSignPickerTopBar.setBackgroundResource(R.color.ux_bg_color_toolbar_colour);
        }
        this.mSignPickerTitleItem = new BaseItemImpl(context);
        this.mSignPickerTitleItem.setText(AppResource.getString(this.mContext, R.string.rv_sign_model));
        this.mSignPickerTitleItem.setTextColorResource(R.color.ux_text_color_title_light);
        this.mSignPickerTitleItem.setTextSize(this.mDisplay.px2dp((float) this.mContext.getResources().getDimensionPixelOffset(R.dimen.ux_text_height_title)));
        this.mSignPickerCreateItem = new BaseItemImpl(context);
        this.mSignPickerCreateItem.setImageResource(R.drawable.sg_list_create_selector);
        this.mSignPickerCreateItem.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SignatureListPicker.this.mSignListPickerDismissCallback.onDismiss();
                FragmentActivity act = (FragmentActivity) SignatureListPicker.this.mContext;
                SignatureFragment fragment = (SignatureFragment) act.getSupportFragmentManager().findFragmentByTag("InkSignFragment");
                if (fragment == null) {
                    fragment = new SignatureFragment();
                    fragment.init(SignatureListPicker.this.mContext, SignatureListPicker.this.mParent, SignatureListPicker.this.mPdfViewCtrl);
                }
                fragment.setInkCallback(SignatureListPicker.this.mInkCallback);
                if (fragment.isAdded()) {
                    act.getSupportFragmentManager().beginTransaction().attach(fragment);
                } else {
                    act.getSupportFragmentManager().beginTransaction().add(R.id.rd_main_id, fragment, "InkSignFragment").addToBackStack(null).commitAllowingStateLoss();
                }
            }
        });
        this.mSignPickerCloseItem = new BaseItemImpl(context);
        this.mSignPickerCloseItem.setImageResource(R.drawable.cloud_back);
        this.mSignPickerCloseItem.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SignatureListPicker.this.mSignListPickerDismissCallback.onDismiss();
            }
        });
        if (!this.mIsPad) {
            this.mSignPickerTopBar.addView(this.mSignPickerCloseItem, TB_Position.Position_LT);
        }
        this.mSignPickerTopBar.addView(this.mSignPickerTitleItem, TB_Position.Position_LT);
        this.mSignPickerTopBar.addView(this.mSignPickerCreateItem, TB_Position.Position_RB);
        ((RelativeLayout) this.mRootView.findViewById(R.id.sign_list_top_bar)).addView(this.mSignPickerTopBar.getContentView());
    }

    private void initData() {
        List<String> recent = SignatureDataUtil.getRecentKeys(this.mContext);
        String recentKey = recent == null ? null : (String) recent.get(0);
        List<String> keys = SignatureDataUtil.getModelKeys(this.mContext);
        if (keys != null) {
            for (String key : keys) {
                SignatureInkItem item = new SignatureInkItem();
                if (key.equals(recentKey)) {
                    item.selected = true;
                }
                item.key = key;
                initBaseItemInfo(item);
                this.mInkBaseItems.add(item);
            }
            initDsgAndHandwritingItems();
            initGroupItems();
        }
    }

    private void initGroupItems() {
        this.mSignListPickerGroupItems.clear();
        SignListPickerGroupItem dsgInkGroupItem = new SignListPickerGroupItem();
        SignListPickerGroupItem handwritingInkGroupItem = new SignListPickerGroupItem();
        dsgInkGroupItem.name = AppResource.getString(this.mContext, R.string.sg_signer_dsg_group_title);
        handwritingInkGroupItem.name = AppResource.getString(this.mContext, R.string.sg_signer_handwriting_group_title);
        dsgInkGroupItem.inkItems = this.mDsgInkItems;
        handwritingInkGroupItem.inkItems = this.mHandwritingInkItems;
        this.mSignListPickerGroupItems.add(handwritingInkGroupItem);
        this.mSignListPickerGroupItems.add(dsgInkGroupItem);
    }

    private void initItem(SignatureInkItem item) {
        HashMap map = SignatureDataUtil.getBitmapByKey(this.mContext, item.key);
        item.bitmap = (Bitmap) map.get("bitmap");
        item.rect = (Rect) map.get("rect");
        item.color = ((Integer) map.get("color")).intValue();
        item.diameter = ((Float) map.get("diameter")).floatValue();
        Object dsgPathObj = map.get("dsgPath");
        if (dsgPathObj == null || AppUtil.isEmpty((String) dsgPathObj)) {
            item.dsgPath = null;
        } else {
            item.dsgPath = (String) dsgPathObj;
        }
    }

    private void initBaseItemInfo(SignatureInkItem item) {
        Object dsgPathObj = SignatureDataUtil.getBitmapByKey(this.mContext, item.key).get("dsgPath");
        if (dsgPathObj == null || AppUtil.isEmpty((String) dsgPathObj)) {
            item.dsgPath = null;
        } else {
            item.dsgPath = (String) dsgPathObj;
        }
    }

    private void initDsgAndHandwritingItems() {
        this.mDsgInkItems.clear();
        this.mHandwritingInkItems.clear();
        Iterator it = this.mInkBaseItems.iterator();
        while (it.hasNext()) {
            SignatureInkItem inkItem = (SignatureInkItem) it.next();
            if (AppUtil.isEmpty(inkItem.dsgPath)) {
                this.mHandwritingInkItems.add(inkItem);
            } else {
                this.mDsgInkItems.add(inkItem);
            }
        }
    }

    private void initList() {
        this.mSignListView = (ExpandableListView) this.mRootView.findViewById(R.id.sign_list_listview);
        this.mSignListView.setGroupIndicator(null);
        this.mAdapter = new SingListAdapter();
        this.mSignListView.setAdapter(this.mAdapter);
        for (int i = 0; i < this.mAdapter.getGroupCount(); i++) {
            this.mSignListView.expandGroup(i);
        }
        addListListener();
    }

    private void addListListener() {
        this.mSignListView.setOnGroupClickListener(new OnGroupClickListener() {
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });
        this.mSignListView.setOnChildClickListener(new OnChildClickListener() {
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                boolean hasOpened = false;
                Iterator it = SignatureListPicker.this.mInkBaseItems.iterator();
                while (it.hasNext()) {
                    SignatureInkItem inkItem = (SignatureInkItem) it.next();
                    if (inkItem.isOpened) {
                        hasOpened = true;
                        inkItem.isOpened = false;
                    }
                }
                if (hasOpened) {
                    SignatureListPicker.this.mAdapter.notifyDataSetChanged();
                    return true;
                }
                SignatureInkItem item = (SignatureInkItem) ((SignListPickerGroupItem) SignatureListPicker.this.mSignListPickerGroupItems.get(groupPosition)).inkItems.get(childPosition);
                if (item == null) {
                    return false;
                }
                SignatureListPicker.this.applySign(item);
                return true;
            }
        });
    }

    private void applySign(SignatureInkItem item) {
        initItem(item);
        this.mInkCallback.onSuccess(false, item.bitmap, item.rect, item.color, item.dsgPath);
        SignatureDataUtil.insertRecent(this.mContext, item.key);
        this.mSignListPickerDismissCallback.onDismiss();
    }

    public int getBaseItemsSize() {
        if (this.mInkBaseItems != null) {
            return this.mInkBaseItems.size();
        }
        return 0;
    }

    public int getHandwritingItemsSize() {
        if (this.mHandwritingInkItems != null) {
            return this.mHandwritingInkItems.size();
        }
        return 0;
    }

    public void dismiss() {
        for (WeakReference clear : this.mCacheMap.values()) {
            clear.clear();
        }
        this.mCacheMap.clear();
        this.mInkBaseItems.clear();
        this.mHandwritingInkItems.clear();
        this.mDsgInkItems.clear();
    }

    private Bitmap getBitmap(SignatureInkItem item) {
        Bitmap bmp = null;
        try {
            bmp = SignatureDataUtil.getScaleBmpByKey(this.mContext, item.key, this.mDisplay.dp2px(120.0f), this.mDisplay.dp2px(100.0f));
            this.mCacheMap.put(item.key, new WeakReference(bmp));
            return bmp;
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
            return bmp;
        }
    }

    public View getRootView() {
        return this.mRootView;
    }
}
