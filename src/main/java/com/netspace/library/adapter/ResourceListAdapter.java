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

public class ResourceListAdapter extends BaseAdapter {
    private Context m_Context;
    private LayoutInflater m_LayoutInflater = ((LayoutInflater) this.m_Context.getSystemService("layout_inflater"));
    private ArrayList<ResourceItemData> m_arrData;

    public ResourceListAdapter(Context context, ArrayList<ResourceItemData> arrData) {
        this.m_Context = context;
        this.m_arrData = arrData;
    }

    public int getCount() {
        return this.m_arrData.size();
    }

    public Object getItem(int position) {
        return this.m_arrData.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ResourceItemData Data = (ResourceItemData) this.m_arrData.get(position);
        if (convertView == null) {
            convertView = this.m_LayoutInflater.inflate(R.layout.listitem_resourcelist, null);
        }
        TextView textTitle = (TextView) convertView.findViewById(R.id.TextTitle);
        TextView textTip = (TextView) convertView.findViewById(R.id.textViewTip);
        ImageView ImageViewFav = (ImageView) convertView.findViewById(R.id.ImageViewFav);
        ImageView ImageViewFileType = (ImageView) convertView.findViewById(R.id.ImageViewFileType);
        textTitle.setText(Data.szTitle);
        if (Data.nTipNumber == 0) {
            textTip.setVisibility(8);
        } else {
            textTip.setText(String.valueOf(Data.nTipNumber));
            textTip.setVisibility(0);
        }
        boolean z = Data.bRead;
        if (Data.nType == 0) {
            ImageViewFileType.setImageResource(R.drawable.ic_question);
        } else {
            ImageViewFileType.setImageResource(R.drawable.ic_multimedia);
        }
        if (Data.nCorrectResult != 0) {
            if (Data.nCorrectResult == -1) {
                ImageViewFileType.setImageResource(R.drawable.ic_resource_wrong);
            } else if (Data.nCorrectResult == 1) {
                ImageViewFileType.setImageResource(R.drawable.ic_resource_halfcorrect);
            } else if (Data.nCorrectResult == 2) {
                ImageViewFileType.setImageResource(R.drawable.ic_resource_correct);
            }
        }
        if (Data.bAnswered) {
            textTitle.setTextColor(-7829368);
        } else {
            textTitle.setTextColor(-16777216);
        }
        if (Data.bFav) {
            ImageViewFav.setVisibility(0);
            ImageViewFav.setImageResource(R.drawable.ic_star_light);
        } else if (Data.bThumbUp) {
            ImageViewFav.setVisibility(0);
            ImageViewFav.setImageResource(R.drawable.ic_thumbup_light);
        } else {
            ImageViewFav.setVisibility(8);
            ImageViewFav.setImageResource(R.drawable.ic_star);
        }
        return convertView;
    }
}
