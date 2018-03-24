package com.netspace.library.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.pad.library.R;
import java.util.ArrayList;

public class SearchResultListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mLayoutInflater = ((LayoutInflater) this.mContext.getSystemService("layout_inflater"));
    private ArrayList<ResourceItemData> marrData;

    public SearchResultListAdapter(Context context, ArrayList<ResourceItemData> arrData) {
        this.mContext = context;
        this.marrData = arrData;
    }

    public int getCount() {
        return this.marrData.size();
    }

    public Object getItem(int position) {
        return this.marrData.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ResourceItemData Data = (ResourceItemData) this.marrData.get(position);
        if (convertView == null) {
            convertView = this.mLayoutInflater.inflate(R.layout.listitem_searchresultitem, null);
        }
        TextView textSubTitle = (TextView) convertView.findViewById(R.id.TextViewSubTitle);
        ImageView ImageViewThumbnail = (ImageView) convertView.findViewById(R.id.imageViewThumbnail);
        ((TextView) convertView.findViewById(R.id.TextViewTitle)).setText(Data.szTitle);
        ImageViewThumbnail.setVisibility(8);
        if (Data.nType == 1) {
            textSubTitle.setText("试题");
        } else if (Data.nType == 4) {
            textSubTitle.setText("资源");
        }
        return convertView;
    }
}
