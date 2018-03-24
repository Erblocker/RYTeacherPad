package com.netspace.library.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.dialog.UserInfoDialog;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.pad.library.R;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

public class BasicUserInfoFragment extends Fragment {
    private LinearLayout mLayout;
    private View mRootView;
    private TextView mTextViewMessage;
    private UserInfoDialog mUserInfoDialog;
    private String mszUserGUID;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.mRootView != null) {
            return this.mRootView;
        }
        this.mRootView = inflater.inflate(R.layout.fragment_basicuserinfo, null);
        this.mLayout = (LinearLayout) this.mRootView.findViewById(R.id.layoutContent);
        this.mLayout.setVisibility(4);
        this.mTextViewMessage = (TextView) this.mRootView.findViewById(R.id.textViewMessage);
        this.mTextViewMessage.setVisibility(4);
        if (this.mszUserGUID != null) {
            loadData();
        }
        return this.mRootView;
    }

    private void reportMessage(String szMessage) {
        this.mTextViewMessage.setVisibility(0);
        this.mTextViewMessage.setText(szMessage);
        this.mLayout.setVisibility(4);
    }

    public void setParent(UserInfoDialog UserInfoDialog) {
        this.mUserInfoDialog = UserInfoDialog;
    }

    public void setData(String szUserGUID) {
        this.mszUserGUID = szUserGUID;
        if (this.mRootView != null) {
            loadData();
        }
    }

    private void loadData() {
        reportMessage("请稍候...");
        WebServiceCallItemObject ItemObject = new WebServiceCallItemObject("ProcessJSFunction", null);
        ItemObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                try {
                    int i;
                    JSONObject jsonObject = new JSONObject((String) ItemObject.getParam("2"));
                    String[] arrFields = new String[]{UserHonourFragment.USERNAME, "studentid"};
                    int[] nID = new int[]{R.id.textViewUserName, R.id.textViewUserID};
                    for (i = 0; i < arrFields.length; i++) {
                        String szFieldName = arrFields[i];
                        if (jsonObject.has(szFieldName)) {
                            ((TextView) BasicUserInfoFragment.this.mRootView.findViewById(nID[i])).setText(jsonObject.getString(szFieldName));
                        }
                    }
                    if (BasicUserInfoFragment.this.mUserInfoDialog != null) {
                        BasicUserInfoFragment.this.mUserInfoDialog.setUserNameAndType(jsonObject.getString("realname"), jsonObject.getString("usertype"));
                    }
                    JSONArray classArray = jsonObject.getJSONArray("classes");
                    String szClasses = "";
                    for (i = 0; i < classArray.length(); i++) {
                        szClasses = new StringBuilder(String.valueOf(szClasses)).append(classArray.getJSONObject(i).getString(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX)).append("\r\n").toString();
                    }
                    ((TextView) BasicUserInfoFragment.this.mRootView.findViewById(R.id.textViewClasses)).setText(szClasses);
                    String szCreateDate = jsonObject.getString("createdate");
                    String szLoginDate = null;
                    String szLoginIP = null;
                    if (jsonObject.has("lastlogindate")) {
                        szLoginDate = jsonObject.getString("lastlogindate");
                    }
                    if (jsonObject.has("lastloginip")) {
                        szLoginIP = jsonObject.getString("lastloginip");
                    }
                    String szBrief = "创建于" + szCreateDate;
                    if (szLoginDate != null) {
                        szBrief = new StringBuilder(String.valueOf(szBrief)).append("，最后一次于 ").append(szLoginDate).append(" 从 ").toString();
                    }
                    if (szLoginIP != null) {
                        szBrief = new StringBuilder(String.valueOf(szBrief)).append(szLoginIP).append(" 登录。").toString();
                    }
                    ((TextView) BasicUserInfoFragment.this.mRootView.findViewById(R.id.textViewTimeInfo)).setText(szBrief);
                    BasicUserInfoFragment.this.mTextViewMessage.setVisibility(4);
                    BasicUserInfoFragment.this.mLayout.setVisibility(0);
                } catch (Exception e) {
                    e.printStackTrace();
                    BasicUserInfoFragment.this.reportMessage("数据解析出现错误");
                }
            }
        });
        ItemObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                BasicUserInfoFragment.this.reportMessage("读取数据出现错误，错误信息：" + Utilities.getErrorMessage(nReturnCode));
            }
        });
        String szJsonData = "<%" + Utilities.readTextFileFromAssertPackage(MyiBaseApplication.getBaseAppContext(), "json2.js") + "%>\r\n";
        ItemObject.setParam("lpszJSFileContent", new StringBuilder(String.valueOf(szJsonData)).append(Utilities.readTextFileFromAssertPackage(MyiBaseApplication.getBaseAppContext(), "getUserInfo.js")).toString());
        ArrayList<String> arrParam = new ArrayList();
        ArrayList<String> arrValue = new ArrayList();
        arrParam.add("userguid");
        arrValue.add(this.mszUserGUID);
        ItemObject.setParam("arrInputParamName", arrParam);
        ItemObject.setParam("arrInputParamValue", arrValue);
        ItemObject.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(ItemObject);
    }
}
