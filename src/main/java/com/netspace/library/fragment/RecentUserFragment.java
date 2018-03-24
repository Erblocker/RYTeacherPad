package com.netspace.library.fragment;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import com.netspace.library.adapter.RecentUserAdapter;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.struct.RecentUserItem;
import com.netspace.pad.library.R;
import java.util.ArrayList;

public class RecentUserFragment extends ListFragment implements OnItemClickListener {
    private RecentUserAdapter mAdapter;
    private ListView mListView;
    private View mRootView;
    private TextView mTextViewMessage;
    private ArrayList<RecentUserItem> marrData;
    private boolean mbNoMessageGroup = false;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.mRootView != null) {
            return this.mRootView;
        }
        this.mRootView = inflater.inflate(R.layout.fragment_recentuser, null);
        this.mListView = (ListView) this.mRootView.findViewById(16908298);
        this.mListView.setOnItemClickListener(this);
        this.mTextViewMessage = (TextView) this.mRootView.findViewById(R.id.textViewMessage);
        this.mTextViewMessage.setVisibility(4);
        this.marrData = MyiBaseApplication.getRecentUser().getData();
        if (this.marrData == null) {
            reportMessage("当前没有最近联系人");
        } else {
            if (this.mbNoMessageGroup) {
                int i = 0;
                while (i < this.marrData.size()) {
                    if (((RecentUserItem) this.marrData.get(i)).szUID.indexOf("*") != -1) {
                        this.marrData.remove(i);
                        i--;
                    }
                    i++;
                }
            }
            this.mAdapter = new RecentUserAdapter(getActivity(), this.marrData);
            this.mListView.setAdapter(this.mAdapter);
            if (this.marrData.isEmpty()) {
                reportMessage("当前没有最近联系人\r\n程序会自动记录您使用的最后20个在线答疑对象");
            } else {
                this.mTextViewMessage.setVisibility(4);
                this.mListView.setVisibility(0);
            }
        }
        return this.mRootView;
    }

    private void reportMessage(String szMessage) {
        this.mTextViewMessage.setVisibility(0);
        this.mTextViewMessage.setText(szMessage);
        this.mListView.setVisibility(4);
    }

    public ArrayList<String> getSelectedUsersJIDs() {
        ArrayList<String> arrResult = new ArrayList();
        if (this.marrData != null) {
            for (int i = 0; i < this.marrData.size(); i++) {
                if (((RecentUserItem) this.marrData.get(i)).bChecked) {
                    arrResult.add(((RecentUserItem) this.marrData.get(i)).szUID);
                }
            }
        }
        return arrResult;
    }

    public ArrayList<String> getSelectedUsersNames() {
        ArrayList<String> arrResult = new ArrayList();
        if (this.marrData != null) {
            for (int i = 0; i < this.marrData.size(); i++) {
                if (((RecentUserItem) this.marrData.get(i)).bChecked) {
                    arrResult.add(((RecentUserItem) this.marrData.get(i)).szName);
                }
            }
        }
        return arrResult;
    }

    public void select(boolean bAll) {
        for (int i = 0; i < this.marrData.size(); i++) {
            if (bAll) {
                ((RecentUserItem) this.marrData.get(i)).bChecked = true;
            } else {
                ((RecentUserItem) this.marrData.get(i)).bChecked = false;
            }
        }
        this.mAdapter.notifyDataSetChanged();
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
    }

    public void setNoMessageGroup(boolean bNoMessageGroup) {
        this.mbNoMessageGroup = bNoMessageGroup;
    }
}
