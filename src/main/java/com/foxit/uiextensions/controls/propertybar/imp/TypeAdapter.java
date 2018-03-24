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
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.utils.AppDisplay;

public class TypeAdapter extends BaseAdapter {
    private AppDisplay display;
    private Context mContext;
    private int mNoteIconType = 1;
    private String[] mTypeNames;
    private int[] mTypePicIds;

    private class ViewHolder {
        private ImageView pb_iv_typePic;
        private TextView pb_tv_typeName;
        private ImageView pb_tv_type_tag;

        private ViewHolder() {
        }
    }

    public TypeAdapter(Context context, int[] typePicIds, String[] typeNames) {
        this.mContext = context;
        this.mTypePicIds = typePicIds;
        this.mTypeNames = typeNames;
        this.display = AppDisplay.getInstance(context);
    }

    public void setNoteIconType(int noteIconType) {
        this.mNoteIconType = noteIconType;
    }

    public int getCount() {
        return this.mTypePicIds.length;
    }

    public Object getItem(int position) {
        return Integer.valueOf(this.mTypePicIds[position]);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(this.mContext).inflate(R.layout.pb_type, null, false);
            if (this.display.isPad()) {
                convertView.setLayoutParams(new LayoutParams(-1, (int) this.mContext.getResources().getDimension(R.dimen.ux_list_item_height_1l_pad)));
            } else {
                convertView.setLayoutParams(new LayoutParams(-1, (int) this.mContext.getResources().getDimension(R.dimen.ux_list_item_height_1l_phone)));
            }
            holder.pb_iv_typePic = (ImageView) convertView.findViewById(R.id.pb_iv_typePic);
            holder.pb_tv_typeName = (TextView) convertView.findViewById(R.id.pb_tv_typeName);
            holder.pb_tv_type_tag = (ImageView) convertView.findViewById(R.id.pb_tv_type_tag);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.pb_iv_typePic.setImageResource(this.mTypePicIds[position]);
        holder.pb_tv_typeName.setText(this.mTypeNames[position]);
        if (this.mNoteIconType == PropertyBar.ICONTYPES[position]) {
            holder.pb_tv_type_tag.setVisibility(0);
        } else {
            holder.pb_tv_type_tag.setVisibility(4);
        }
        return convertView;
    }
}
