package com.netspace.library.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.netspace.library.restful.provider.device.DeviceOperationRESTServiceProvider;
import com.netspace.pad.library.R;
import java.util.ArrayList;

public class SubjectLearnAdapter extends BaseAdapter {
    private Context m_Context;
    private LayoutInflater m_LayoutInflater = ((LayoutInflater) this.m_Context.getSystemService("layout_inflater"));
    private ArrayList<SubjectItem> m_arrData;

    public static class SubjectItem {
        public boolean bIsQuestion;
        public boolean bIsResource;
        public int nItemType = 0;
        public String szItemExt = "";
        public String szItemGUID = "";
        public String szItemName = "";
        public String szParentGUID = "";
    }

    public SubjectLearnAdapter(Context context, ArrayList<SubjectItem> arrData) {
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
        SubjectItem Data = (SubjectItem) this.m_arrData.get(position);
        if (convertView == null) {
            convertView = this.m_LayoutInflater.inflate(R.layout.listitem_subjectlearn, null);
        }
        ((TextView) convertView.findViewById(R.id.textTitle)).setText(Data.szItemName);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageViewVoice);
        imageView.setImageResource(R.drawable.ic_lesson);
        if (Data.bIsResource) {
            if (Data.nItemType >= 1 && Data.nItemType < 1000) {
                imageView.setImageResource(R.drawable.ic_multimedia);
            } else if (Data.nItemType >= 1000 && Data.nItemType < 2000) {
                imageView.setImageResource(R.drawable.ic_lesson);
            } else if (Data.nItemType >= 2000 && Data.nItemType < 3000) {
                imageView.setImageResource(R.drawable.ic_lesson);
            } else if (Data.nItemType >= 4000 && Data.nItemType < DeviceOperationRESTServiceProvider.TIMEOUT) {
                imageView.setImageResource(R.drawable.ic_camera);
            }
        } else if (Data.bIsQuestion) {
            imageView.setImageResource(R.drawable.ic_question);
        } else {
            imageView.setImageResource(R.drawable.ic_folder2);
        }
        return convertView;
    }
}
