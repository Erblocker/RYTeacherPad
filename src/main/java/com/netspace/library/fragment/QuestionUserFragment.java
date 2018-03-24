package com.netspace.library.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.netspace.library.adapter.AllUserListAdapter;
import com.netspace.library.adapter.AllUserListAdapter.UserItem;
import com.netspace.library.adapter.RandomUserAdapter;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.struct.UserClassInfo;
import com.netspace.library.struct.UserInfo;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.pad.library.R;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QuestionUserFragment extends Fragment implements OnClickListener {
    private AllUserListAdapter mAdapter;
    private ArrayList<String> mAllStudentUserNames = new ArrayList();
    private ExpandableListView mListViewGroup;
    private ListView mListViewRandom;
    private RadioButton mRadioButtonAll;
    private RadioButton mRadioButtonGroup;
    private RadioButton mRadioButtonRandom;
    private Button mRandomButton;
    private RandomUserAdapter mRandomUserAdapter;
    private View mRootView;
    private TextView mTextViewEmpty;
    private TextView mTextViewMessage;
    private String mUserClassGUID;
    private ArrayList<StudentInfo> marrNeedAnswerStudents = new ArrayList();
    private String mszClassName;

    public static class StudentInfo {
        public String szJID;
        public String szRealName;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.mRootView != null) {
            return this.mRootView;
        }
        this.mRootView = inflater.inflate(R.layout.fragment_questionuser, null);
        this.mListViewGroup = (ExpandableListView) this.mRootView.findViewById(R.id.listGroup);
        this.mListViewRandom = (ListView) this.mRootView.findViewById(R.id.listRandom);
        this.mTextViewEmpty = (TextView) this.mRootView.findViewById(R.id.textViewEmpty);
        this.mTextViewMessage = (TextView) this.mRootView.findViewById(R.id.textViewMessage);
        this.mTextViewMessage.setVisibility(4);
        this.mRadioButtonAll = (RadioButton) this.mRootView.findViewById(R.id.radioButtonAll);
        this.mRadioButtonAll.setOnClickListener(this);
        this.mRadioButtonGroup = (RadioButton) this.mRootView.findViewById(R.id.radioButtonGroup);
        this.mRadioButtonGroup.setOnClickListener(this);
        this.mRadioButtonRandom = (RadioButton) this.mRootView.findViewById(R.id.radioButtonRandom);
        this.mRadioButtonRandom.setOnClickListener(this);
        this.mAdapter = new AllUserListAdapter(getActivity());
        this.mRandomUserAdapter = new RandomUserAdapter(getActivity());
        this.mRandomButton = (Button) this.mRootView.findViewById(R.id.buttonRandom);
        this.mRandomButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                QuestionUserFragment.this.mRandomUserAdapter.addRandomUser(QuestionUserFragment.this.mUserClassGUID);
            }
        });
        this.mListViewRandom.setAdapter(this.mRandomUserAdapter);
        this.mRadioButtonAll.setChecked(true);
        this.mListViewRandom.setVisibility(8);
        this.mListViewGroup.setVisibility(8);
        this.mTextViewEmpty.setVisibility(8);
        this.mRandomButton.setVisibility(8);
        if (MyiBaseApplication.getCommonVariables().UserInfo.arrClasses.size() > 0) {
            Iterator it = MyiBaseApplication.getCommonVariables().UserInfo.arrClasses.iterator();
            while (it.hasNext()) {
                UserClassInfo UserClassInfo = (UserClassInfo) it.next();
                if (UserClassInfo.szClassGUID.equalsIgnoreCase(this.mUserClassGUID)) {
                    String szUserClassName = UserClassInfo.szClassName;
                    this.mszClassName = szUserClassName;
                    Iterator it2 = UserClassInfo.arrStudents.iterator();
                    while (it2.hasNext()) {
                        UserInfo UserInfo = (UserInfo) it2.next();
                        this.mAdapter.add(UserInfo.szRealName, "myipad_" + UserInfo.szUserName, szUserClassName);
                        this.mAllStudentUserNames.add(UserInfo.szUserName);
                    }
                }
            }
        }
        this.mListViewGroup.setAdapter(this.mAdapter);
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
                                    String szUserName = oneStudent.getString(UserHonourFragment.USERNAME);
                                    String szJID = "myipad_" + oneStudent.getString(UserHonourFragment.USERNAME);
                                    String szRealName = oneStudent.getString("realname");
                                    if (Utilities.isInArray(QuestionUserFragment.this.mAllStudentUserNames, szUserName)) {
                                        QuestionUserFragment.this.mAdapter.add(szRealName, szJID, szGroupName);
                                    }
                                }
                            }
                        }
                        QuestionUserFragment.this.mListViewGroup.setAdapter(QuestionUserFragment.this.mAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        CallItem.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
            }
        });
        CallItem.setParam("lpszKey", "RecentClasses_" + MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
        VirtualNetworkObject.addToQueue(CallItem);
        return this.mRootView;
    }

    public ArrayList<StudentInfo> getNeedAnswerStudents() {
        return this.marrNeedAnswerStudents;
    }

    public void setUserClassGUID(String szGUID) {
        this.mUserClassGUID = szGUID;
    }

    public void select(boolean bAll) {
        ArrayList<UserItem> arrData = this.mAdapter.getData();
        if (bAll && this.mAdapter.isGroupExpand(this.mszClassName)) {
            Utilities.showAlertMessage(getContext(), "无法全部选中", "全选仅用于分组模式，如需选择全部学生请点击界面中“全班学生”。");
        }
        for (int i = 0; i < arrData.size(); i++) {
            if (!((UserItem) arrData.get(i)).szGroupName.equalsIgnoreCase(this.mszClassName)) {
                if (!bAll) {
                    ((UserItem) arrData.get(i)).bSelected = false;
                } else if (this.mAdapter.isGroupExpand(((UserItem) arrData.get(i)).szGroupName)) {
                    ((UserItem) arrData.get(i)).bSelected = true;
                }
            }
        }
        this.mAdapter.notifyDataSetChanged();
    }

    private void addUserByJID(String szUserJID) {
        if (MyiBaseApplication.getCommonVariables().UserInfo.arrClasses.size() > 0) {
            Iterator it = MyiBaseApplication.getCommonVariables().UserInfo.arrClasses.iterator();
            while (it.hasNext()) {
                UserClassInfo UserClassInfo = (UserClassInfo) it.next();
                if (UserClassInfo.szClassGUID.equalsIgnoreCase(this.mUserClassGUID)) {
                    String szUserClassName = UserClassInfo.szClassName;
                    Iterator it2 = UserClassInfo.arrStudents.iterator();
                    while (it2.hasNext()) {
                        UserInfo UserInfo = (UserInfo) it2.next();
                        String szStudentName = UserInfo.szRealName;
                        String szJID = "myipad_" + UserInfo.szUserName;
                        if (szJID.equalsIgnoreCase(szUserJID) || szUserJID.isEmpty()) {
                            StudentInfo oneStudent = new StudentInfo();
                            oneStudent.szJID = szJID;
                            oneStudent.szRealName = szStudentName;
                            this.marrNeedAnswerStudents.add(oneStudent);
                        }
                    }
                }
            }
        }
    }

    public String getSelectedUserJIDs(String szSepChar) {
        this.marrNeedAnswerStudents.clear();
        String szResult = "";
        if (this.mRadioButtonAll.isChecked()) {
            addUserByJID("");
            return "";
        }
        Iterator it;
        if (this.mRadioButtonGroup.isChecked()) {
            it = this.mAdapter.getData().iterator();
            while (it.hasNext()) {
                UserItem UserItem = (UserItem) it.next();
                if (UserItem.bSelected) {
                    if (!szResult.isEmpty()) {
                        szResult = new StringBuilder(String.valueOf(szResult)).append(szSepChar).toString();
                    }
                    szResult = new StringBuilder(String.valueOf(szResult)).append(UserItem.szUID).toString();
                    addUserByJID(UserItem.szUID);
                }
            }
            if (szResult.isEmpty()) {
                addUserByJID("");
            }
        } else if (this.mRadioButtonRandom.isChecked()) {
            it = this.mRandomUserAdapter.getData().iterator();
            while (it.hasNext()) {
                RandomUserAdapter.UserItem UserItem2 = (RandomUserAdapter.UserItem) it.next();
                if (UserItem2.bChecked) {
                    if (!szResult.isEmpty()) {
                        szResult = new StringBuilder(String.valueOf(szResult)).append(szSepChar).toString();
                    }
                    szResult = new StringBuilder(String.valueOf(szResult)).append(UserItem2.szUID).toString();
                    addUserByJID(UserItem2.szUID);
                }
            }
            if (szResult.isEmpty()) {
                addUserByJID("");
            }
        }
        return szResult;
    }

    public void onClick(View v) {
        if (v.getId() == R.id.radioButtonAll) {
            this.mListViewRandom.setVisibility(8);
            this.mListViewGroup.setVisibility(8);
            this.mTextViewEmpty.setVisibility(8);
            this.mRandomButton.setVisibility(8);
            this.mRadioButtonGroup.setChecked(false);
            this.mRadioButtonRandom.setChecked(false);
        } else if (v.getId() == R.id.radioButtonGroup) {
            this.mListViewRandom.setVisibility(8);
            this.mRandomButton.setVisibility(8);
            Utilities.fadeOutView(this.mListViewGroup, HttpStatus.SC_MULTIPLE_CHOICES);
            if (this.mAdapter.getChildTypeCount() == 0) {
                this.mTextViewEmpty.setVisibility(0);
            }
            layoutParams = (LayoutParams) this.mListViewGroup.getLayoutParams();
            layoutParams.height = Utilities.dpToPixel(280, getActivity());
            this.mListViewGroup.setLayoutParams(layoutParams);
            this.mRadioButtonAll.setChecked(false);
            this.mRadioButtonRandom.setChecked(false);
        } else if (v.getId() == R.id.radioButtonRandom) {
            this.mListViewGroup.setVisibility(8);
            Utilities.fadeOutView(this.mListViewRandom, HttpStatus.SC_MULTIPLE_CHOICES);
            this.mRandomButton.setVisibility(0);
            layoutParams = (LayoutParams) this.mListViewRandom.getLayoutParams();
            layoutParams.height = Utilities.dpToPixel(280, getActivity());
            this.mListViewRandom.setLayoutParams(layoutParams);
            this.mRadioButtonAll.setChecked(false);
            this.mRadioButtonGroup.setChecked(false);
        }
    }
}
