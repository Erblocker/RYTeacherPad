package com.netspace.teacherpad.modules.paper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.controls.CustomSelfLearnItem;
import com.netspace.library.fragment.UserHonourFragment;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.modules.TeacherModuleBase;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;

public class PaperModule extends TeacherModuleBase {
    protected View mContentView;

    public PaperModule(Activity Activity, LinearLayout RootLayout) {
        super(Activity, RootLayout);
        this.mModuleName = "网上阅卷";
        this.mCategoryName = "试卷";
        this.mIconID = R.drawable.ic_correcting_light;
    }

    public void startModule() {
        super.startModule();
        if (this.mContentView == null) {
            this.mContentView = this.mLayoutInflater.inflate(R.layout.layout_papermodule, null);
            this.mRootLayout.addView(this.mContentView);
            LayoutParams LayoutParam = (LayoutParams) this.mContentView.getLayoutParams();
            LayoutParam.height = -1;
            this.mContentView.setLayoutParams(LayoutParam);
            WebServiceCallItemObject ItemObject = new WebServiceCallItemObject("ProcessJSFunction", (Activity) this.mActivity.get());
            ItemObject.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    try {
                        JSONArray jArray = new JSONArray((String) ItemObject.getParam("2"));
                        HashMap<String, CustomSelfLearnItem> mapData = new HashMap();
                        if (jArray.length() == 0) {
                            ((BaseActivity) PaperModule.this.mActivity.get()).reportMessage("没有可以批改的内容", "在您的账户下没有发现任何需要阅卷的内容。");
                        }
                        for (int i = 0; i < jArray.length(); i++) {
                            JSONObject OneObj = (JSONObject) jArray.get(i);
                            CustomSelfLearnItem SelfLearnItem = new CustomSelfLearnItem((Context) PaperModule.this.mActivity.get());
                            String szPaperID = OneObj.getString("paperid");
                            String szSummery = "共有" + OneObj.getString("totalcount") + "道题目需要批改，目前剩余" + OneObj.getString("unfinishcount") + "道尚未批改。";
                            SelfLearnItem.setVisibility(0);
                            SelfLearnItem.setTitle(OneObj.getString("title"));
                            SelfLearnItem.setScheduleGUID(OneObj.getString("paperid"));
                            SelfLearnItem.setSummery(szSummery);
                            ((ViewGroup) PaperModule.this.mContentView).addView(SelfLearnItem);
                            SelfLearnItem.setOnClickListener(new OnClickListener() {
                                public void onClick(View v) {
                                    CustomSelfLearnItem Item = (CustomSelfLearnItem) v;
                                    PaperViewActivity.clearData();
                                    Intent Intent = new Intent((Context) PaperModule.this.mActivity.get(), PaperViewActivity.class);
                                    Intent.putExtra("paperid", Item.getScheduleGUID());
                                    ((Activity) PaperModule.this.mActivity.get()).startActivity(Intent);
                                }
                            });
                            LayoutParams LayoutParam = (LayoutParams) SelfLearnItem.getLayoutParams();
                            LayoutParam.leftMargin = 10;
                            LayoutParam.rightMargin = 10;
                            LayoutParam.topMargin = 10;
                            LayoutParam.height = -2;
                            LayoutParam.width = -1;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ((BaseActivity) PaperModule.this.mActivity.get()).reportError("数据解析错误", "解析数据时出现错误，" + e.getMessage());
                    }
                }
            });
            String szJsonData = "<%" + Utilities.readTextFileFromAssertPackage((Context) this.mActivity.get(), "json2.js") + "%>\r\n";
            ItemObject.setParam("lpszJSFileContent", new StringBuilder(String.valueOf(szJsonData)).append(Utilities.readTextFileFromAssertPackage((Context) this.mActivity.get(), "getPaperData.js")).toString());
            ArrayList<String> arrParam = new ArrayList();
            ArrayList<String> arrValue = new ArrayList();
            arrParam.add("userguid");
            arrValue.add(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
            arrParam.add(UserHonourFragment.USERNAME);
            arrValue.add(MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
            ItemObject.setParam("arrInputParamName", arrParam);
            ItemObject.setParam("arrInputParamValue", arrValue);
            VirtualNetworkObject.addToQueue(ItemObject);
        }
    }

    public void stopModule() {
        super.stopModule();
        if (this.mContentView != null) {
            this.mRootLayout.removeView(this.mContentView);
            this.mContentView = null;
        }
    }
}
