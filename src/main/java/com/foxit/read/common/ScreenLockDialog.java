package com.foxit.read.common;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.foxit.app.App;
import com.foxit.app.utils.AppTheme;
import com.foxit.home.R;
import com.foxit.uiextensions.controls.dialog.UIDialog;
import com.foxit.uiextensions.utils.AppResource;
import java.util.ArrayList;

/* compiled from: RD_ScreenLock */
class ScreenLockDialog extends UIDialog {
    private final Context mContext;
    private int mCurOption = -1;
    private ArrayList<String> mOptionList;
    private ListView mScreenLockList;
    private BaseAdapter screenLockAdapter = new BaseAdapter() {
        public int getCount() {
            return ScreenLockDialog.this.mOptionList.size();
        }

        public String getItem(int position) {
            return (String) ScreenLockDialog.this.mOptionList.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(App.instance().getApplicationContext(), R.layout.screen_lock_item, null);
                holder.optionName = (TextView) convertView.findViewById(R.id.rd_screen_lock_textview);
                holder.checkedCircle = (ImageView) convertView.findViewById(R.id.rd_screen_lock_imageview);
                if (App.instance().getDisplay().isPad()) {
                    ((LayoutParams) holder.optionName.getLayoutParams()).leftMargin = (int) AppResource.getDimension(ScreenLockDialog.this.mContext, R.dimen.ux_horz_left_margin_pad);
                    ((LayoutParams) holder.checkedCircle.getLayoutParams()).rightMargin = (int) AppResource.getDimension(ScreenLockDialog.this.mContext, R.dimen.ux_horz_right_margin_pad);
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.optionName.setText((CharSequence) ScreenLockDialog.this.mOptionList.get(position));
            if (position == ScreenLockDialog.this.mCurOption) {
                holder.checkedCircle.setImageResource(R.drawable.rd_circle_checked);
            } else {
                holder.checkedCircle.setImageResource(R.drawable.rd_circle_normal);
            }
            if (position == ScreenLockDialog.this.mOptionList.size() - 1) {
                convertView.setBackgroundResource(R.drawable.dialog_button_background_selector);
            } else {
                convertView.setBackgroundResource(R.drawable.rd_menu_item_selector);
            }
            return convertView;
        }
    };

    /* compiled from: RD_ScreenLock */
    static class ViewHolder {
        public ImageView checkedCircle;
        public TextView optionName;

        ViewHolder() {
        }
    }

    public ScreenLockDialog(Context context) {
        super(context, R.layout.screen_lock_dialog, AppTheme.getDialogTheme(), App.instance().getDisplay().getUITextEditDialogWidth());
        this.mContext = context;
        this.mScreenLockList = (ListView) this.mContentView.findViewById(R.id.rd_screen_lock_listview);
        if (App.instance().getDisplay().isPad()) {
            usePadDimes();
        }
        setTitle(AppResource.getString(context, R.string.rv_screen_rotation_pad));
        this.mOptionList = new ArrayList();
        this.mOptionList.add(AppResource.getString(this.mContext, R.string.rv_screen_rotation_pad_landscape));
        this.mOptionList.add(AppResource.getString(this.mContext, R.string.rv_screen_rotation_pad_portrait));
        this.mOptionList.add(AppResource.getString(this.mContext, R.string.rv_screen_rotation_pad_auto));
        this.mScreenLockList.setAdapter(this.screenLockAdapter);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mScreenLockList.setOnItemClickListener(listener);
    }

    public void setCurOption(int position) {
        this.mCurOption = position;
        this.screenLockAdapter.notifyDataSetChanged();
    }

    private void usePadDimes() {
        try {
            ((LinearLayout.LayoutParams) this.mTitleView.getLayoutParams()).leftMargin = App.instance().getDisplay().dp2px(24.0f);
            ((LinearLayout.LayoutParams) this.mTitleView.getLayoutParams()).rightMargin = App.instance().getDisplay().dp2px(24.0f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
