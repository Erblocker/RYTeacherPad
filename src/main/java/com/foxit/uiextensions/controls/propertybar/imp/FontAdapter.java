package com.foxit.uiextensions.controls.propertybar.imp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.utils.AppDisplay;

public class FontAdapter extends BaseAdapter {
    private Context mContext;
    private boolean[] mFontChecked;
    private String[] mFontNames;

    private class ViewHolder {
        private ImageView pb_iv_fontItem_selected;
        private TextView pb_tv_fontItem;

        private ViewHolder() {
        }
    }

    public FontAdapter(Context context, String[] fontNames, boolean[] fontChecked) {
        this.mContext = context;
        this.mFontNames = fontNames;
        this.mFontChecked = fontChecked;
    }

    public int getCount() {
        return this.mFontNames.length;
    }

    public Object getItem(int position) {
        return this.mFontNames[position];
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(this.mContext).inflate(R.layout.pb_fontstyle_fontitem, null, false);
            LayoutParams layoutParams = new LayoutParams(-1, -2);
            if (AppDisplay.getInstance(this.mContext).isPad()) {
                layoutParams.height = (int) this.mContext.getResources().getDimension(R.dimen.ux_list_item_height_1l_pad);
            } else {
                layoutParams.height = (int) this.mContext.getResources().getDimension(R.dimen.ux_list_item_height_1l_phone);
            }
            convertView.setLayoutParams(layoutParams);
            int padding = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_left_margin_phone);
            convertView.setPadding(padding, 0, padding, 0);
            holder.pb_tv_fontItem = (TextView) convertView.findViewById(R.id.pb_tv_fontItem);
            holder.pb_iv_fontItem_selected = (ImageView) convertView.findViewById(R.id.pb_iv_fontItem_selected);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.pb_tv_fontItem.setText(this.mFontNames[position]);
        holder.pb_iv_fontItem_selected.setImageResource(R.drawable.pb_selected);
        if (this.mFontChecked[position]) {
            holder.pb_tv_fontItem.setTextColor(this.mContext.getResources().getColor(R.color.ux_text_color_button_colour));
            holder.pb_iv_fontItem_selected.setVisibility(0);
        } else {
            holder.pb_tv_fontItem.setTextColor(this.mContext.getResources().getColor(R.color.ux_text_color_body1_gray));
            holder.pb_iv_fontItem_selected.setVisibility(8);
        }
        return convertView;
    }
}
