package com.netspace.library.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.netspace.pad.library.R;
import java.util.ArrayList;

public class FriendsListAdapter extends BaseAdapter {
    private ListView mListView;
    private Context m_Context;
    private LayoutInflater m_LayoutInflater = ((LayoutInflater) this.m_Context.getSystemService("layout_inflater"));
    private ArrayList<FriendItem> m_arrData = new ArrayList();

    public class FriendItem {
        public boolean bHasNewMessage = false;
        public String szDrawPadBackground = "";
        public String szName;
        public String szUID;
    }

    public FriendsListAdapter(Context context, ListView ListView) {
        this.m_Context = context;
        this.mListView = ListView;
    }

    public ArrayList<FriendItem> getData() {
        return this.m_arrData;
    }

    public void add(String szName, String szUID) {
        FriendItem NewItem = new FriendItem();
        NewItem.szName = szName;
        NewItem.szUID = szUID;
        this.m_arrData.add(NewItem);
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

    public boolean setNewMessage(String szUID, boolean bHasNewMessage) {
        for (int i = 0; i < this.m_arrData.size(); i++) {
            FriendItem Data = (FriendItem) this.m_arrData.get(i);
            if (Data.szUID.equalsIgnoreCase(szUID)) {
                Data.bHasNewMessage = bHasNewMessage;
                return true;
            }
        }
        return false;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        FriendItem Data = (FriendItem) this.m_arrData.get(position);
        if (convertView == null) {
            convertView = this.m_LayoutInflater.inflate(R.layout.listitem_friend, null);
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

    public void updateBackgroundKey(String szSender2, String szBoardBackgroundKey) {
        for (int i = 0; i < this.m_arrData.size(); i++) {
            if (((FriendItem) this.m_arrData.get(i)).szUID.equalsIgnoreCase(szSender2)) {
                ((FriendItem) this.m_arrData.get(i)).szDrawPadBackground = szBoardBackgroundKey;
            }
        }
    }
}
