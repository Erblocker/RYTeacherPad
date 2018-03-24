package com.netspace.library.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.struct.UserRight;
import com.netspace.pad.library.R;
import java.util.ArrayList;

public class UserRightsAdapter extends BaseAdapter {
    private ListView mListView;
    private Context m_Context;
    private LayoutInflater m_LayoutInflater = ((LayoutInflater) this.m_Context.getSystemService("layout_inflater"));
    private ArrayList<UserRight> m_arrData;

    public UserRightsAdapter(Context context, ArrayList<UserRight> arrData) {
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
        UserRight Data = (UserRight) this.m_arrData.get(position);
        if (convertView == null) {
            convertView = this.m_LayoutInflater.inflate(R.layout.listitem_userright, null);
        }
        ImageView imageView = (ImageView) convertView.findViewById(R.id.ImageViewState);
        ((TextView) convertView.findViewById(R.id.TextTitle)).setText(Data.szName);
        if (Data.szValue.equalsIgnoreCase("on")) {
            imageView.setImageDrawable(new IconDrawable(this.m_Context, FontAwesomeIcons.fa_check).color(-16716288).actionBarSize());
        } else if (Data.szValue.equalsIgnoreCase("off")) {
            imageView.setImageDrawable(new IconDrawable(this.m_Context, FontAwesomeIcons.fa_ban).color(-1179648).actionBarSize());
        } else {
            imageView.setImageDrawable(new IconDrawable(this.m_Context, FontAwesomeIcons.fa_question).color(-16777216).actionBarSize());
        }
        return convertView;
    }
}
