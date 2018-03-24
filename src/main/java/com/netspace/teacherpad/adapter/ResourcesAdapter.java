package com.netspace.teacherpad.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.TeacherPadApplication;
import java.util.ArrayList;

public class ResourcesAdapter extends BaseAdapter {
    private Context m_Context;
    private LayoutInflater m_LayoutInflater;
    private ArrayList<ResourceItemData> m_arrData;

    public ResourcesAdapter(Context context, ArrayList<ResourceItemData> arrData) {
        this.m_Context = context;
        this.m_arrData = arrData;
        this.m_LayoutInflater = LayoutInflater.from(context);
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
        ResourceItemData OneItem = (ResourceItemData) this.m_arrData.get(position);
        String szDescription = "";
        if (convertView == null) {
            convertView = this.m_LayoutInflater.inflate(R.layout.listitem_resource, null);
        }
        TextView textTitle = (TextView) convertView.findViewById(R.id.textTitle);
        TextView textDescription = (TextView) convertView.findViewById(R.id.textDescription);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageViewPlaying);
        if (OneItem.nType == 0) {
            szDescription = "试题";
        } else {
            szDescription = "资源";
        }
        if (OneItem.szTitle.length() > 50) {
            textTitle.setText(OneItem.szTitle.substring(0, 50));
        } else {
            textTitle.setText(OneItem.szTitle);
        }
        textDescription.setText(szDescription);
        if (OneItem.bRead) {
            textTitle.setTextColor(-7829368);
        } else {
            textTitle.setTextColor(-16777216);
        }
        if (TeacherPadApplication.szCurrentPlayingGUID == null || !TeacherPadApplication.szCurrentPlayingGUID.equalsIgnoreCase(OneItem.szGUID)) {
            imageView.setImageDrawable(null);
        } else {
            imageView.setImageResource(R.drawable.ic_playing);
        }
        return convertView;
    }
}
