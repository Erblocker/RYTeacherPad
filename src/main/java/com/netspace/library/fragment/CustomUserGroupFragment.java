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
import com.netspace.library.struct.RecentUserItem;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.pad.library.R;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CustomUserGroupFragment extends Fragment implements OnItemClickListener {
    private AllUserListAdapter mAdapter;
    private ExpandableListView mListView;
    private View mRootView;
    private TextView mTextViewMessage;
    private ArrayList<RecentUserItem> marrData;

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
        WebServiceCallItemObject CallItem = new WebServiceCallItemObject("GetPrivateData2", getActivity());
        CallItem.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                if (ItemObject.readTextData() != null) {
                    try {
                        JSONArray jsonObject = new JSONArray(ItemObject.readTextData());
                        for (int i = 0; i < jsonObject.length(); i++) {
                            JSONObject oneClass = jsonObject.getJSONObject(i);
                            if (!oneClass.has("szClassGUID")) {
                                String szGroupName = oneClass.getString("szName");
                                JSONArray arrStudents = oneClass.getJSONArray("students");
                                for (int j = 0; j < arrStudents.length(); j++) {
                                    JSONObject oneStudent = arrStudents.getJSONObject(j);
                                    String szJID = "myipad_" + oneStudent.getString(UserHonourFragment.USERNAME);
                                    CustomUserGroupFragment.this.mAdapter.add(oneStudent.getString("realname"), szJID, szGroupName);
                                }
                            }
                        }
                        CustomUserGroupFragment.this.mListView.setAdapter(CustomUserGroupFragment.this.mAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (CustomUserGroupFragment.this.mAdapter.getGroupCount() == 0) {
                    CustomUserGroupFragment.this.reportMessage("当前没有定义任何自定义学生分组");
                }
            }
        });
        CallItem.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                CustomUserGroupFragment.this.reportMessage("当前没有定义任何自定义学生分组");
            }
        });
        CallItem.setParam("lpszKey", "RecentClasses_" + MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
        VirtualNetworkObject.addToQueue(CallItem);
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
}
