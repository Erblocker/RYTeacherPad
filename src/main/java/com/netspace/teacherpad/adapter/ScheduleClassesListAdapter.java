package com.netspace.teacherpad.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.structure.ScheduleInfo;
import java.util.ArrayList;

public class ScheduleClassesListAdapter extends BaseAdapter {
    private Context m_Context;
    private LayoutInflater m_LayoutInflater;
    private ArrayList<ScheduleInfo> m_arrData;

    public ScheduleClassesListAdapter(Context context, ArrayList<ScheduleInfo> arrData) {
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
        ScheduleInfo OneItem = (ScheduleInfo) this.m_arrData.get(position);
        String szDescription = "";
        if (convertView == null) {
            convertView = this.m_LayoutInflater.inflate(R.layout.listitem_scheduleclasslist, null);
        }
        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView1);
        ((TextView) convertView.findViewById(R.id.textView1)).setText(OneItem.szTitle);
        if (OneItem.bHasUnfinishedTask) {
            imageView.setImageResource(R.drawable.circle_blue);
        } else {
            imageView.setImageDrawable(null);
        }
        return convertView;
    }
}
