package com.netspace.library.fragment;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.netspace.library.adapter.UserRightsAdapter;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.consts.Features;
import com.netspace.library.struct.UserRight;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.pad.library.R;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

public class UserRightsFragment extends Fragment implements OnItemClickListener {
    private UserRightsAdapter mAdapter;
    private JSONObject mJsonObject;
    private LinearLayout mLayout;
    private ListView mListView;
    private View mRootView;
    private TextView mTextViewInfo;
    private TextView mTextViewMessage;
    private ArrayList<UserRight> marrData = new ArrayList();
    private boolean mbChanged = false;
    private String mszUserGUID;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.mRootView != null) {
            return this.mRootView;
        }
        this.mRootView = inflater.inflate(R.layout.fragment_userrights, null);
        this.mLayout = (LinearLayout) this.mRootView.findViewById(R.id.layoutContent);
        this.mLayout.setVisibility(4);
        this.mTextViewMessage = (TextView) this.mRootView.findViewById(R.id.textViewMessage);
        this.mTextViewMessage.setVisibility(4);
        this.mTextViewInfo = (TextView) this.mRootView.findViewById(R.id.textViewInfo);
        this.mListView = (ListView) this.mRootView.findViewById(R.id.listView1);
        this.mListView.setOnItemClickListener(this);
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

    public void setData(String szUserGUID) {
        this.mszUserGUID = szUserGUID;
        if (this.mRootView != null) {
            loadData();
        }
    }

    private void loadData() {
        reportMessage("稍候...");
        WebServiceCallItemObject ItemObject = new WebServiceCallItemObject("ProcessJSFunction", null);
        ItemObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                String szData = ((String) ItemObject.getParam("2")).trim();
                try {
                    JSONObject jsonObject;
                    String key = "";
                    if (szData.isEmpty()) {
                        jsonObject = new JSONObject();
                    } else {
                        jsonObject = new JSONObject(szData);
                    }
                    String[] szFeatures = new String[]{Features.PERMISSION_DISCUSS_READ, Features.PERMISSION_DISCUSS_WRITE, Features.PERMISSION_MYILIBRARY_ADDPICTURE, Features.PERMISSION_MYILIBRARY_ADDVIDEO, Features.PERMISSION_MYILIBRARY_ADDFOLDER, Features.PERMISSION_MYILIBRARY_SHARETOIM, Features.PERMISSION_MYILIBRARY_SHARETOSTUDENTS, Features.PERMISSION_CHAT_CLASSGROUPCHAT, Features.PERMISSION_LESSONCLASS};
                    for (String key2 : szFeatures) {
                        UserRight temp = new UserRight();
                        temp.szKey = key2;
                        temp.szName = Features.getName(key2);
                        if (jsonObject == null || !jsonObject.has(key2)) {
                            temp.szValue = "auto";
                        } else {
                            temp.szValue = jsonObject.getString(key2);
                        }
                        UserRightsFragment.this.marrData.add(temp);
                    }
                    UserRightsFragment.this.mJsonObject = jsonObject;
                    UserRightsFragment.this.mAdapter = new UserRightsAdapter(UserRightsFragment.this.getActivity(), UserRightsFragment.this.marrData);
                    UserRightsFragment.this.mListView.setAdapter(UserRightsFragment.this.mAdapter);
                    UserRightsFragment.this.mTextViewMessage.setVisibility(4);
                    UserRightsFragment.this.mLayout.setVisibility(0);
                } catch (Exception e) {
                    e.printStackTrace();
                    UserRightsFragment.this.reportMessage("数据解析出现错误");
                }
            }
        });
        ItemObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                UserRightsFragment.this.reportMessage("读取数据出现错误，错误信息：" + Utilities.getErrorMessage(nReturnCode));
            }
        });
        String szJsonData = "<%" + Utilities.readTextFileFromAssertPackage(MyiBaseApplication.getBaseAppContext(), "json2.js") + "%>\r\n";
        ItemObject.setParam("lpszJSFileContent", new StringBuilder(String.valueOf(szJsonData)).append(Utilities.readTextFileFromAssertPackage(MyiBaseApplication.getBaseAppContext(), "getUserRights.js")).toString());
        ArrayList<String> arrParam = new ArrayList();
        ArrayList<String> arrValue = new ArrayList();
        arrParam.add("userguid");
        arrValue.add(this.mszUserGUID);
        ItemObject.setParam("arrInputParamName", arrParam);
        ItemObject.setParam("arrInputParamValue", arrValue);
        ItemObject.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(ItemObject);
    }

    public void save() {
        if (!this.mbChanged) {
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id) {
        String[] arrNames = new String[]{"始终启用", "始终禁用", "使用默认值"};
        Builder dialogBuilder = new Builder(getActivity());
        dialogBuilder.setItems(arrNames, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                final UserRight data = (UserRight) UserRightsFragment.this.marrData.get(position);
                String szNewValue = null;
                if (which == 0) {
                    szNewValue = "on";
                } else if (which == 1) {
                    szNewValue = "off";
                } else if (which == 2) {
                    szNewValue = "auto";
                }
                WebServiceCallItemObject CallItem = new WebServiceCallItemObject("UsersSetACL", null);
                CallItem.setSuccessListener(new OnSuccessListener() {
                    public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                        data.szValue = (String) ItemObject.getParam("lpszFieldValue");
                        UserRightsFragment.this.mbChanged = true;
                        try {
                            UserRightsFragment.this.mJsonObject.put(data.szName, data.szValue);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        UserRightsFragment.this.mAdapter.notifyDataSetChanged();
                    }
                });
                CallItem.setFailureListener(new OnFailureListener() {
                    public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                        Utilities.showAlertMessage(UserRightsFragment.this.getActivity(), "设置权限出现错误", "设置权限时出现错误，错误信息：" + Utilities.getErrorMessage(nReturnCode));
                    }
                });
                CallItem.setParam("lpszUserNameOrGUID", UserRightsFragment.this.mszUserGUID);
                CallItem.setParam("lpszFieldName", data.szKey);
                CallItem.setParam("lpszFieldValue", szNewValue);
                CallItem.setAlwaysActiveCallbacks(true);
                VirtualNetworkObject.addToQueue(CallItem);
            }
        });
        dialogBuilder.setTitle("设置权限");
        dialogBuilder.setCancelable(true);
        dialogBuilder.show();
    }
}
