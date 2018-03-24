package com.netspace.teacherpad.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.netspace.library.utilities.Utilities;
import com.netspace.teacherpad.R;
import java.util.ArrayList;

public class FriendsListAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ListView mListView;
    private ArrayList<FriendItem> marrData;
    private ArrayList<String> marrGroups = new ArrayList();

    public class FriendItem {
        public boolean bHasNewMessage = false;
        public String szGroupName;
        public String szName;
        public String szUID;
    }

    public FriendsListAdapter(Context context, ListView ListView) {
        this.mContext = context;
        this.mLayoutInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        this.marrData = new ArrayList();
        this.mListView = ListView;
    }

    public void add(String szName, String szUID, String szGroupName, boolean bHasNewMessage) {
        FriendItem NewItem = new FriendItem();
        NewItem.szName = szName;
        NewItem.szUID = szUID;
        NewItem.szGroupName = szGroupName;
        NewItem.bHasNewMessage = bHasNewMessage;
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
            if (((FriendItem) this.marrData.get(i)).szGroupName.equalsIgnoreCase(szGroupName)) {
                if (((FriendItem) this.marrData.get(i)).szUID.equalsIgnoreCase(szUID)) {
                    break;
                }
                nCount++;
            }
        }
        return nCount;
    }

    public boolean setNewMessage(String szUID, boolean bHasNewMessage) {
        for (int i = 0; i < this.marrData.size(); i++) {
            FriendItem Data = (FriendItem) this.marrData.get(i);
            if (Data.szUID.equalsIgnoreCase(szUID)) {
                Data.bHasNewMessage = bHasNewMessage;
                return true;
            }
        }
        return false;
    }

    public void registerDataSetObserver(DataSetObserver observer) {
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
    }

    public int getGroupCount() {
        return this.marrGroups.size();
    }

    public int getChildrenCount(int groupPosition) {
        String szGroupName = (String) this.marrGroups.get(groupPosition);
        int nCount = 0;
        for (int i = 0; i < this.marrData.size(); i++) {
            if (((FriendItem) this.marrData.get(i)).szGroupName.equalsIgnoreCase(szGroupName)) {
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
            if (((FriendItem) this.marrData.get(i)).szGroupName.equalsIgnoreCase(szGroupName)) {
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
            if (((FriendItem) this.marrData.get(i)).szGroupName.equalsIgnoreCase(szGroupName)) {
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

    private boolean checkGroupHasNewMessage(String szGroupName) {
        int i = 0;
        while (i < this.marrData.size()) {
            if (((FriendItem) this.marrData.get(i)).szGroupName.equalsIgnoreCase(szGroupName) && ((FriendItem) this.marrData.get(i)).bHasNewMessage) {
                return true;
            }
            i++;
        }
        return false;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String szGroupName = (String) getGroup(groupPosition);
        if (convertView == null) {
            convertView = this.mLayoutInflater.inflate(R.layout.listitem_friendgroup, null);
        }
        TextView textTitle = (TextView) convertView.findViewById(R.id.textView1);
        ImageView newImageIcon = (ImageView) convertView.findViewById(R.id.imageUnread);
        if (checkGroupHasNewMessage(szGroupName)) {
            newImageIcon.setVisibility(0);
        } else {
            newImageIcon.setVisibility(8);
        }
        textTitle.setText(szGroupName);
        return convertView;
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        FriendItem Data = (FriendItem) getChild(groupPosition, childPosition);
        if (convertView == null) {
            convertView = this.mLayoutInflater.inflate(R.layout.listitem_friend, null);
        }
        TextView textTitle = (TextView) convertView.findViewById(R.id.textView1);
        ImageView newImageIcon = (ImageView) convertView.findViewById(R.id.imageUnread);
        if (Data.bHasNewMessage) {
            newImageIcon.setVisibility(0);
        } else {
            newImageIcon.setVisibility(8);
        }
        textTitle.setText(Data.szName);
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
}
