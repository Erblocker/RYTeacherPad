package com.netspace.teacherpad.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.netspace.teacherpad.R;
import com.squareup.picasso.Picasso;

public class PDFThumbnailAdapter extends BaseAdapter {
    private Context m_Context;
    private LayoutInflater m_LayoutInflater;
    private String m_szBaseURL;
    private int mnPageCount = 0;

    public PDFThumbnailAdapter(Context context, String szBaseURL, int nPageCount) {
        this.m_Context = context;
        this.m_LayoutInflater = LayoutInflater.from(context);
        this.m_szBaseURL = szBaseURL;
        this.mnPageCount = nPageCount;
    }

    public int getCount() {
        return this.mnPageCount;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        String szURL = this.m_szBaseURL + "-" + String.valueOf(position) + ".jpg";
        if (convertView == null) {
            convertView = this.m_LayoutInflater.inflate(R.layout.listitem_pdfthumbnail, null);
        }
        Picasso.with(this.m_Context).load(szURL).resize(0, 200).into((ImageView) convertView.findViewById(R.id.imageView1));
        return convertView;
    }
}
