package com.netspace.library.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.netspace.library.struct.LessonClassItemData;
import com.netspace.pad.library.R;
import java.util.ArrayList;

public class LessonClassExpandableListAdapter implements ExpandableListAdapter {
    private Context mContext;
    private ArrayList<LessonClassItemData> mData;
    private int mGroupCount = 0;
    private LayoutInflater mLayoutInflater;
    private OnClickListener mOnClickListener;

    public LessonClassExpandableListAdapter(Context Context, ArrayList<LessonClassItemData> arrData, OnClickListener OnClickListener) {
        int nGroupPosition = -1;
        int nChildPosition = 0;
        this.mData = arrData;
        this.mContext = Context;
        this.mOnClickListener = OnClickListener;
        this.mLayoutInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        for (int i = 0; i < arrData.size(); i++) {
            LessonClassItemData Data = (LessonClassItemData) arrData.get(i);
            if (Data.szMethod.equalsIgnoreCase("SwitchToResource") || Data.szMethod.equalsIgnoreCase("EndClass")) {
                nGroupPosition++;
                Data.bIsGroup = true;
                Data.nGroupPosition = nGroupPosition;
                nChildPosition = 0;
                this.mGroupCount++;
            } else {
                Data.bIsGroup = false;
                Data.nGroupPosition = nGroupPosition;
                Data.nChildPosition = nChildPosition;
                nChildPosition++;
            }
        }
    }

    public int getGroupCount() {
        return this.mGroupCount;
    }

    public int getChildrenCount(int groupPosition) {
        int nResult = 0;
        int i = 0;
        while (i < this.mData.size()) {
            if (((LessonClassItemData) this.mData.get(i)).nGroupPosition == groupPosition && !((LessonClassItemData) this.mData.get(i)).bIsGroup) {
                nResult++;
            }
            i++;
        }
        return nResult;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        LessonClassItemData Data = (LessonClassItemData) getGroup(groupPosition);
        if (convertView == null) {
            convertView = this.mLayoutInflater.inflate(R.layout.listgroupitem_startclassgroup, parent, false);
        }
        TextView textTime = (TextView) convertView.findViewById(R.id.textViewTime);
        ImageView imageViewPlay = (ImageView) convertView.findViewById(R.id.imageViewPlay);
        ((TextView) convertView.findViewById(R.id.textViewName)).setText(Data.szTitle);
        textTime.setText(Data.szTime);
        imageViewPlay.setTag(Integer.valueOf(Data.nTimeOffsetInSeconds));
        imageViewPlay.setOnClickListener(this.mOnClickListener);
        return convertView;
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        LessonClassItemData Data = (LessonClassItemData) getChild(groupPosition, childPosition);
        if (Data == null) {
            return null;
        }
        if (convertView == null) {
            convertView = this.mLayoutInflater.inflate(R.layout.listgroupitem_startclassitem, parent, false);
        }
        TextView textTitle = (TextView) convertView.findViewById(R.id.textViewName);
        TextView textTime = (TextView) convertView.findViewById(R.id.textViewTime);
        ImageView imageViewPlay = (ImageView) convertView.findViewById(R.id.imageViewPlay);
        ImageView imageViewHasImage = (ImageView) convertView.findViewById(R.id.imageViewHasPicture);
        String szTitle = Data.szTitle;
        if (!Data.szResult.isEmpty()) {
            szTitle = new StringBuilder(String.valueOf(szTitle)).append(" - ").append(Data.szResult).toString();
        }
        if (Data.szAnswerResult.equalsIgnoreCase("correct")) {
            szTitle = new StringBuilder(String.valueOf(szTitle)).append("(回答正确)").toString();
        }
        if (Data.szAnswerResult.equalsIgnoreCase("wrong")) {
            szTitle = new StringBuilder(String.valueOf(szTitle)).append("(回答错误)").toString();
        }
        textTitle.setText(Data.szTitle);
        textTime.setText(Data.szTime);
        imageViewHasImage.setVisibility(4);
        if (Data.szObjectGUID.startsWith("http://")) {
            imageViewHasImage.setVisibility(0);
        }
        imageViewPlay.setTag(Integer.valueOf(Data.nTimeOffsetInSeconds));
        imageViewPlay.setOnClickListener(this.mOnClickListener);
        return convertView;
    }

    public Object getGroup(int groupPosition) {
        int i = 0;
        while (i < this.mData.size()) {
            if (((LessonClassItemData) this.mData.get(i)).nGroupPosition == groupPosition && ((LessonClassItemData) this.mData.get(i)).bIsGroup) {
                return this.mData.get(i);
            }
            i++;
        }
        return null;
    }

    public Object getChild(int groupPosition, int childPosition) {
        int i = 0;
        while (i < this.mData.size()) {
            if (((LessonClassItemData) this.mData.get(i)).nGroupPosition == groupPosition && !((LessonClassItemData) this.mData.get(i)).bIsGroup && ((LessonClassItemData) this.mData.get(i)).nChildPosition == childPosition) {
                return this.mData.get(i);
            }
            i++;
        }
        return null;
    }

    public long getGroupId(int groupPosition) {
        return (long) groupPosition;
    }

    public long getChildId(int groupPosition, int childPosition) {
        int i = 0;
        while (i < this.mData.size()) {
            if (((LessonClassItemData) this.mData.get(i)).nGroupPosition == groupPosition && !((LessonClassItemData) this.mData.get(i)).bIsGroup && ((LessonClassItemData) this.mData.get(i)).nChildPosition == childPosition) {
                return (long) i;
            }
            i++;
        }
        return -1;
    }

    public boolean hasStableIds() {
        return true;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public boolean areAllItemsEnabled() {
        return true;
    }

    public boolean isEmpty() {
        if (this.mData.size() == 0) {
            return true;
        }
        return false;
    }

    public void onGroupExpanded(int groupPosition) {
    }

    public void onGroupCollapsed(int groupPosition) {
    }

    public long getCombinedChildId(long groupId, long childId) {
        return (Long.MIN_VALUE | ((2147483647L & groupId) << 32)) | (-1 & childId);
    }

    public long getCombinedGroupId(long groupId) {
        return (2147483647L & groupId) << 32;
    }

    public void registerDataSetObserver(DataSetObserver observer) {
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
    }
}
