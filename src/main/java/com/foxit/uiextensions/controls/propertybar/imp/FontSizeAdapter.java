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

public class FontSizeAdapter extends BaseAdapter {
    private Context mContext;
    private boolean[] mFontSizeChecked;
    private float[] mFontSizes;

    private class ViewHolder {
        private ImageView pb_iv_fontSizeItem_selected;
        private TextView pb_tv_fontSizeItem;

        private ViewHolder() {
        }
    }

    public FontSizeAdapter(Context context, float[] fontSizes, boolean[] fontSizeChecked) {
        this.mContext = context;
        this.mFontSizes = fontSizes;
        this.mFontSizeChecked = fontSizeChecked;
    }

    public int getCount() {
        return this.mFontSizes.length;
    }

    public Object getItem(int position) {
        return Float.valueOf(this.mFontSizes[position]);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(this.mContext).inflate(R.layout.pb_fontstyle_fontsizeitem, null, false);
            LayoutParams layoutParams = new LayoutParams(-1, -2);
            if (AppDisplay.getInstance(this.mContext).isPad()) {
                layoutParams.height = (int) this.mContext.getResources().getDimension(R.dimen.ux_list_item_height_1l_pad);
            } else {
                layoutParams.height = (int) this.mContext.getResources().getDimension(R.dimen.ux_list_item_height_1l_phone);
            }
            convertView.setLayoutParams(layoutParams);
            int padding = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_left_margin_phone);
            convertView.setPadding(padding, 0, padding, 0);
            holder.pb_tv_fontSizeItem = (TextView) convertView.findViewById(R.id.pb_tv_fontSizeItem);
            holder.pb_iv_fontSizeItem_selected = (ImageView) convertView.findViewById(R.id.pb_iv_fontSizeItem_selected);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.pb_tv_fontSizeItem.setText(new StringBuilder(String.valueOf((int) this.mFontSizes[position])).append("px").toString());
        holder.pb_iv_fontSizeItem_selected.setImageResource(R.drawable.pb_selected);
        if (this.mFontSizeChecked[position]) {
            holder.pb_tv_fontSizeItem.setTextColor(this.mContext.getResources().getColor(R.color.ux_text_color_button_colour));
            holder.pb_iv_fontSizeItem_selected.setVisibility(0);
        } else {
            holder.pb_tv_fontSizeItem.setTextColor(this.mContext.getResources().getColor(R.color.ux_text_color_body1_gray));
            holder.pb_iv_fontSizeItem_selected.setVisibility(8);
        }
        return convertView;
    }
}
