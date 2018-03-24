package com.netspace.library.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.TextView;
import com.netspace.library.adapter.AllUserListAdapter;
import com.netspace.library.adapter.AllUserListAdapter.UserItem;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.consts.Features;
import com.netspace.library.struct.RecentUserItem;
import com.netspace.library.struct.UserClassInfo;
import com.netspace.library.struct.UserInfo;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import java.util.ArrayList;
import java.util.Iterator;

public class AllUserFragment extends Fragment implements OnItemClickListener {
    private AllUserListAdapter mAdapter;
    private ExpandableListView mListView;
    private View mRootView;
    private TextView mTextViewMessage;
    private ArrayList<RecentUserItem> marrData;
    private boolean mbNoMessageGroup = false;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.mRootView != null) {
            return this.mRootView;
        }
        this.mRootView = inflater.inflate(R.layout.fragment_alluser, null);
        this.mListView = (ExpandableListView) this.mRootView.findViewById(16908298);
        this.mListView.setOnItemClickListener(this);
        this.mListView.setVisibility(0);
        this.mTextViewMessage = (TextView) this.mRootView.findViewById(R.id.textViewMessage);
        this.mTextViewMessage.setVisibility(4);
        this.mAdapter = new AllUserListAdapter(getActivity());
        if (MyiBaseApplication.getCommonVariables().UserInfo.arrClasses.size() > 0) {
            Iterator it = MyiBaseApplication.getCommonVariables().UserInfo.arrClasses.iterator();
            while (it.hasNext()) {
                UserClassInfo UserClassInfo = (UserClassInfo) it.next();
                String szUserClassName = UserClassInfo.szClassName;
                if (!this.mbNoMessageGroup && MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_CHAT_CLASSGROUPCHAT)) {
                    this.mAdapter.add("班级讨论区", "*_" + UserClassInfo.szClassGUID + "_*", szUserClassName);
                }
                Iterator it2;
                UserInfo UserInfo;
                if (MyiBaseApplication.getCommonVariables().UserInfo.nUserType == 0) {
                    it2 = UserClassInfo.arrTeachers.iterator();
                    while (it2.hasNext()) {
                        UserInfo = (UserInfo) it2.next();
                        this.mAdapter.add(UserInfo.szRealName, UserInfo.szUserName + "_teacherpad", szUserClassName);
                    }
                } else {
                    it2 = UserClassInfo.arrStudents.iterator();
                    while (it2.hasNext()) {
                        UserInfo = (UserInfo) it2.next();
                        this.mAdapter.add(UserInfo.szRealName, "myipad_" + UserInfo.szUserName, szUserClassName);
                    }
                }
            }
        } else {
            reportMessage("当前没有分配具体班级，无法显示学生列表");
        }
        this.mListView.setAdapter(this.mAdapter);
        return this.mRootView;
    }

    private void reportMessage(String szMessage) {
        this.mTextViewMessage.setVisibility(0);
        this.mTextViewMessage.setText(szMessage);
        this.mListView.setVisibility(4);
    }

    public ArrayList<String> getSelectedUsersJIDs() {
        ArrayList arrResult = new ArrayList();
        ArrayList<UserItem> arrData = this.mAdapter.getData();
        for (int i = 0; i < arrData.size(); i++) {
            if (((UserItem) arrData.get(i)).bSelected) {
                String szUID = ((UserItem) arrData.get(i)).szUID;
                if (!Utilities.isInArray(arrResult, szUID)) {
                    arrResult.add(szUID);
                }
            }
        }
        return arrResult;
    }

    public ArrayList<String> getSelectedUsersNames() {
        ArrayList arrResult = new ArrayList();
        ArrayList<String> arrNameResult = new ArrayList();
        ArrayList<UserItem> arrData = this.mAdapter.getData();
        for (int i = 0; i < arrData.size(); i++) {
            if (((UserItem) arrData.get(i)).bSelected) {
                String szUID = ((UserItem) arrData.get(i)).szUID;
                if (!Utilities.isInArray(arrResult, szUID)) {
                    arrResult.add(szUID);
                    arrNameResult.add(((UserItem) arrData.get(i)).szName);
                }
            }
        }
        return arrNameResult;
    }

    public void select(boolean bAll) {
        ArrayList<UserItem> arrData = this.mAdapter.getData();
        for (int i = 0; i < arrData.size(); i++) {
            if (!bAll) {
                ((UserItem) arrData.get(i)).bSelected = false;
            } else if (this.mAdapter.isGroupExpand(((UserItem) arrData.get(i)).szGroupName)) {
                ((UserItem) arrData.get(i)).bSelected = true;
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
