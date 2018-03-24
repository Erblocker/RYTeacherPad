package com.netspace.library.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import java.util.ArrayList;
import java.util.HashMap;

public class AllUserListAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private HashMap<String, Boolean> mGroupExpandState = new HashMap();
    private LayoutInflater mLayoutInflater;
    private ArrayList<UserItem> marrData;
    private ArrayList<String> marrGroups = new ArrayList();

    public class UserItem {
        public boolean bSelected = false;
        public String szGroupName;
        public String szName;
        public String szUID;
    }

    public AllUserListAdapter(Context context) {
        this.mContext = context;
        this.mLayoutInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        this.marrData = new ArrayList();
    }

    public void add(String szName, String szUID, String szGroupName) {
        UserItem NewItem = new UserItem();
        NewItem.szName = szName;
        NewItem.szUID = szUID;
        NewItem.szGroupName = szGroupName;
        if (!Utilities.isInArray(this.marrGroups, szGroupName)) {
            this.marrGroups.add(szGroupName);
        }
        this.marrData.add(NewItem);
    }

    public int getGroupID(String szGroupName) {
        for (int i = 0; i < this.marrGroups.size(); i++) {
            if (((String) this.marrGroups.get(i)).equalsIgnoreCase(szGroupName)) {
                return i;
            }
        }
        return -1;
    }

    public int getChildID(String szUID, String szGroupName) {
        int nCount = 0;
        for (int i = 0; i < this.marrData.size(); i++) {
            if (((UserItem) this.marrData.get(i)).szGroupName.equalsIgnoreCase(szGroupName)) {
                if (((UserItem) this.marrData.get(i)).szUID.equalsIgnoreCase(szUID)) {
                    break;
                }
                nCount++;
            }
        }
        return nCount;
    }

    public int getGroupCount() {
        return this.marrGroups.size();
    }

    public int getChildrenCount(int groupPosition) {
        String szGroupName = (String) this.marrGroups.get(groupPosition);
        int nCount = 0;
        for (int i = 0; i < this.marrData.size(); i++) {
            if (((UserItem) this.marrData.get(i)).szGroupName.equalsIgnoreCase(szGroupName)) {
                nCount++;
            }
        }
        return nCount;
    }

    public Object getGroup(int groupPosition) {
        return this.marrGroups.get(groupPosition);
    }

    public Object getChild(int groupPosition, int childPosition) {
        String szGroupName = (String) this.marrGroups.get(groupPosition);
        for (int i = 0; i < this.marrData.size(); i++) {
            if (((UserItem) this.marrData.get(i)).szGroupName.equalsIgnoreCase(szGroupName)) {
                if (childPosition <= 0) {
                    return this.marrData.get(i);
                }
                childPosition--;
            }
        }
        return null;
    }

    public long getGroupId(int groupPosition) {
        return (long) groupPosition;
    }

    public long getCombinedChildId(long groupId, long childId) {
        return (Long.MIN_VALUE | ((2147483647L & groupId) << 32)) | (-1 & childId);
    }

    public long getCombinedGroupId(long groupId) {
        return (2147483647L & groupId) << 32;
    }

    public long getChildId(int groupPosition, int childPosition) {
        String szGroupName = (String) this.marrGroups.get(groupPosition);
        for (int i = 0; i < this.marrData.size(); i++) {
            if (((UserItem) this.marrData.get(i)).szGroupName.equalsIgnoreCase(szGroupName)) {
                if (childPosition <= 0) {
                    return (long) i;
                }
                childPosition--;
            }
        }
        return -1;
    }

    public boolean hasStableIds() {
        return true;
    }

    public boolean isGroupExpand(String szGroupName) {
        if (this.mGroupExpandState.containsKey(szGroupName)) {
            return ((Boolean) this.mGroupExpandState.get(szGroupName)).booleanValue();
        }
        return false;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String szGroupName = (String) getGroup(groupPosition);
        this.mGroupExpandState.put(szGroupName, Boolean.valueOf(isExpanded));
        if (convertView == null) {
            convertView = this.mLayoutInflater.inflate(R.layout.listitem_allusergroup, null);
        }
        TextView textTitle = (TextView) convertView.findViewById(R.id.textView1);
        ((ImageView) convertView.findViewById(R.id.imageUnread)).setVisibility(8);
        textTitle.setText(szGroupName);
        return convertView;
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final UserItem Data = (UserItem) getChild(groupPosition, childPosition);
        if (convertView == null) {
            convertView = this.mLayoutInflater.inflate(R.layout.listitem_recentuser, null);
        }
        TextView textTitle = (TextView) convertView.findViewById(R.id.textViewTitle);
        TextView textContent = (TextView) convertView.findViewById(R.id.textViewContent);
        CheckBox checkbox = (CheckBox) convertView.findViewById(R.id.checkBox1);
        checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Data.bSelected = isChecked;
            }
        });
        checkbox.setChecked(Data.bSelected);
        textTitle.setText("学生");
        textContent.setText(Data.szName);
        return convertView;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public boolean areAllItemsEnabled() {
        return true;
    }

    public boolean isEmpty() {
        if (this.marrData.size() == 0) {
            return true;
        }
        return false;
    }

    public void onGroupExpanded(int groupPosition) {
    }

    public void onGroupCollapsed(int groupPosition) {
    }

    public ArrayList<UserItem> getData() {
        return this.marrData;
    }
}
