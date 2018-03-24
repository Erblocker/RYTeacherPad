package com.netspace.teacherpad.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.TeacherPadApplication;
import com.netspace.teacherpad.structure.ClassInfo;
import java.util.ArrayList;

public class ClassesAdapter extends BaseAdapter {
    private Context m_Context;
    private LayoutInflater m_LayoutInflater;
    private ArrayList<ClassInfo> m_arrData;

    public ClassesAdapter(Context context, ArrayList<ClassInfo> arrData) {
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
        ClassInfo OneItem = (ClassInfo) this.m_arrData.get(position);
        String szDescription = "";
        if (convertView == null) {
            convertView = this.m_LayoutInflater.inflate(R.layout.listitem_classes, null);
        }
        TextView textTitle = (TextView) convertView.findViewById(R.id.textTitle);
        TextView textDescription = (TextView) convertView.findViewById(R.id.textDescription);
        if (TeacherPadApplication.szPCScheduleGUID == null || TeacherPadApplication.szPCScheduleGUID.isEmpty() || !TeacherPadApplication.szPCScheduleGUID.equalsIgnoreCase(OneItem.szGUID)) {
            textTitle.setText(OneItem.szClassName);
        } else {
            textTitle.setText(OneItem.szClassName + "(进行中)");
        }
        textDescription.setText(OneItem.szLessonIndex);
        return convertView;
    }
}
