package com.netspace.teacherpad.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.netspace.teacherpad.controls.CustomQuestionBlock;
import com.netspace.teacherpad.structure.ClassInfo;
import java.util.ArrayList;

public class QuestionsBlockAdapter extends BaseAdapter {
    private Context m_Context;
    private LayoutInflater m_LayoutInflater;
    private ArrayList<ClassInfo> m_arrData;

    public QuestionsBlockAdapter(Context context, ArrayList<ClassInfo> arrData) {
        this.m_Context = context;
        this.m_arrData = arrData;
        this.m_LayoutInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return 200;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        String szDescription = "";
        if (convertView == null) {
            View TargetObject = new CustomQuestionBlock(this.m_Context);
            TargetObject.setVisibility(0);
            convertView = TargetObject;
        }
        CustomQuestionBlock OneBlock = (CustomQuestionBlock) convertView;
        OneBlock.setImageInfo("1303191646170000", "3820", "11091029", 1, 1);
        OneBlock.setScore(20.0f, true);
        return convertView;
    }
}
