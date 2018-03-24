package com.netspace.library.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import com.netspace.library.struct.RecentUserItem;
import com.netspace.pad.library.R;
import java.util.ArrayList;

public class RecentUserAdapter extends BaseAdapter {
    private ListView mListView;
    private Context m_Context;
    private LayoutInflater m_LayoutInflater = ((LayoutInflater) this.m_Context.getSystemService("layout_inflater"));
    private ArrayList<RecentUserItem> m_arrData;

    public RecentUserAdapter(Context context, ArrayList<RecentUserItem> arrData) {
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
        final RecentUserItem Data = (RecentUserItem) this.m_arrData.get(position);
        if (convertView == null) {
            convertView = this.m_LayoutInflater.inflate(R.layout.listitem_recentuser, null);
        }
        TextView textTitle = (TextView) convertView.findViewById(R.id.textViewTitle);
        TextView textContent = (TextView) convertView.findViewById(R.id.textViewContent);
        CheckBox checkbox = (CheckBox) convertView.findViewById(R.id.checkBox1);
        checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Data.bChecked = isChecked;
            }
        });
        checkbox.setChecked(Data.bChecked);
        if (Data.nType == 0) {
            textTitle.setText("学生");
        }
        textContent.setText(Data.szName);
        return convertView;
    }
}
