package com.netspace.teacherpad.modules.homework;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import com.netspace.library.activity.HomeworkCorrectActivity;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.components.CommentComponent;
import com.netspace.library.controls.CustomSelfLearnItem;
import com.netspace.library.fragment.UserHonourFragment;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.adapter.ClassesAdapter;
import com.netspace.teacherpad.adapter.ScheduleClassesListAdapter;
import com.netspace.teacherpad.modules.TeacherModuleBase;
import com.netspace.teacherpad.structure.ClassInfo;
import com.netspace.teacherpad.structure.ScheduleInfo;
import io.vov.vitamio.MediaMetadataRetriever;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;

public class HomeworkCorrectModule extends TeacherModuleBase {
    protected View mContentView;
    private ClassesAdapter m_Adapter;
    private ArrayList<ClassInfo> m_arrClasses;

    public HomeworkCorrectModule(Activity Activity, LinearLayout RootLayout) {
        super(Activity, RootLayout);
        this.m_arrClasses = new ArrayList();
        this.mModuleName = "批改作业";
        this.mCategoryName = "作业";
        this.mIconID = R.drawable.ic_correcting_light;
    }

    public void startModule() {
        super.startModule();
        if (this.mContentView == null) {
            this.mContentView = this.mLayoutInflater.inflate(R.layout.layout_homeworkcorrectmodule, null);
            this.mRootLayout.addView(this.mContentView);
            LayoutParams LayoutParam = (LayoutParams) this.mContentView.getLayoutParams();
            LayoutParam.height = -1;
            this.mContentView.setLayoutParams(LayoutParam);
            WebServiceCallItemObject ItemObject = new WebServiceCallItemObject("ProcessJSFunction", (Activity) this.mActivity.get());
            ItemObject.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    try {
                        int i;
                        CustomSelfLearnItem UIItem;
                        ArrayList<ScheduleInfo> arrItemTitles;
                        JSONArray jArray = new JSONArray((String) ItemObject.getParam("2"));
                        HashMap<String, CustomSelfLearnItem> mapData = new HashMap();
                        if (jArray.length() == 0) {
                            ((BaseActivity) HomeworkCorrectModule.this.mActivity.get()).reportMessage("没有可以批改的内容", "在您的账户下没有发现有备课可供批改。");
                        }
                        ArrayList<CustomSelfLearnItem> arrUIItems = new ArrayList();
                        for (i = 0; i < jArray.length(); i++) {
                            JSONObject OneObj = (JSONObject) jArray.get(i);
                            String szClassGUID = OneObj.getString(UserHonourFragment.USERCLASSGUID);
                            String szUserClassName = OneObj.getString("userclassname");
                            if (szClassGUID.isEmpty()) {
                                UIItem = (CustomSelfLearnItem) mapData.get(szUserClassName);
                            } else {
                                UIItem = (CustomSelfLearnItem) mapData.get(szClassGUID);
                            }
                            ArrayList<String> arrItemScheduleGUID;
                            ArrayList<String> arrItemResourceGUID;
                            ScheduleInfo OneInfo;
                            if (UIItem == null) {
                                final CustomSelfLearnItem SelfLearnItem = new CustomSelfLearnItem((Context) HomeworkCorrectModule.this.mActivity.get());
                                String szUserClassGUID = OneObj.getString(UserHonourFragment.USERCLASSGUID);
                                SelfLearnItem.setVisibility(0);
                                SelfLearnItem.setTitle(OneObj.getString("userclassname"));
                                SelfLearnItem.setScheduleGUID(OneObj.getString("guid"));
                                SelfLearnItem.setResourceGUID(OneObj.getString(CommentComponent.RESOURCEGUID));
                                final String str = szUserClassGUID;
                                SelfLearnItem.setListItemOnItemClickListener(new OnItemClickListener() {
                                    public void onItemClick(AdapterView<?> adapterView, View arg1, int arg2, long arg3) {
                                        String szScheduleGUID = (String) ((ArrayList) SelfLearnItem.getTag(R.id.textView2)).get(arg2);
                                        String szResourceGUID = (String) ((ArrayList) SelfLearnItem.getTag(R.id.textView3)).get(arg2);
                                        Intent Intent = new Intent((Context) HomeworkCorrectModule.this.mActivity.get(), HomeworkCorrectActivity.class);
                                        Intent.putExtra(CommentComponent.RESOURCEGUID, szResourceGUID);
                                        Intent.putExtra("scheduleguid", szScheduleGUID);
                                        Intent.putExtra(UserHonourFragment.USERCLASSGUID, str);
                                        Intent.putExtra("serveraddress", MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress);
                                        ((Activity) HomeworkCorrectModule.this.mActivity.get()).startActivity(Intent);
                                    }
                                });
                                ((ViewGroup) HomeworkCorrectModule.this.mContentView).addView(SelfLearnItem);
                                arrUIItems.add(SelfLearnItem);
                                LayoutParams LayoutParam = (LayoutParams) SelfLearnItem.getLayoutParams();
                                LayoutParam.leftMargin = 10;
                                LayoutParam.rightMargin = 10;
                                LayoutParam.topMargin = 10;
                                LayoutParam.height = -2;
                                LayoutParam.width = -1;
                                arrItemTitles = new ArrayList();
                                arrItemScheduleGUID = new ArrayList();
                                arrItemResourceGUID = new ArrayList();
                                SelfLearnItem.setTag(R.id.textView1, arrItemTitles);
                                SelfLearnItem.setTag(R.id.textView2, arrItemScheduleGUID);
                                SelfLearnItem.setTag(R.id.textView3, arrItemResourceGUID);
                                OneInfo = new ScheduleInfo();
                                OneInfo.szTitle = OneObj.getString("title") + "(" + OneObj.getString("lessonindex") + " " + OneObj.getString(MediaMetadataRetriever.METADATA_KEY_DATE) + ")";
                                OneInfo.bHasUnfinishedTask = OneObj.getBoolean("haswork");
                                arrItemTitles.add(OneInfo);
                                arrItemScheduleGUID.add(OneObj.getString("guid"));
                                arrItemResourceGUID.add(OneObj.getString(CommentComponent.RESOURCEGUID));
                                SelfLearnItem.setListItems((ListAdapter) new ScheduleClassesListAdapter((Context) HomeworkCorrectModule.this.mActivity.get(), arrItemTitles));
                                SelfLearnItem.setSummery("共有" + String.valueOf(arrItemTitles.size()) + "个备课。");
                                if (szClassGUID.isEmpty()) {
                                    mapData.put(OneObj.getString("userclassname"), SelfLearnItem);
                                } else {
                                    mapData.put(OneObj.getString(UserHonourFragment.USERCLASSGUID), SelfLearnItem);
                                }
                            } else {
                                arrItemTitles = (ArrayList) UIItem.getTag(R.id.textView1);
                                arrItemScheduleGUID = (ArrayList) UIItem.getTag(R.id.textView2);
                                arrItemResourceGUID = (ArrayList) UIItem.getTag(R.id.textView3);
                                OneInfo = new ScheduleInfo();
                                OneInfo.szTitle = OneObj.getString("title") + "(" + OneObj.getString("lessonindex") + " " + OneObj.getString(MediaMetadataRetriever.METADATA_KEY_DATE) + ")";
                                OneInfo.bHasUnfinishedTask = OneObj.getBoolean("haswork");
                                arrItemTitles.add(OneInfo);
                                arrItemScheduleGUID.add(OneObj.getString("guid"));
                                arrItemResourceGUID.add(OneObj.getString(CommentComponent.RESOURCEGUID));
                            }
                        }
                        for (i = 0; i < arrUIItems.size(); i++) {
                            UIItem = (CustomSelfLearnItem) arrUIItems.get(i);
                            arrItemTitles = (ArrayList) UIItem.getTag(R.id.textView1);
                            UIItem.setListItems((ListAdapter) new ScheduleClassesListAdapter((Context) HomeworkCorrectModule.this.mActivity.get(), arrItemTitles));
                            UIItem.setSummery("共有" + String.valueOf(arrItemTitles.size()) + "个备课。");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ((BaseActivity) HomeworkCorrectModule.this.mActivity.get()).reportError("数据解析错误", "解析数据时出现错误，" + e.getMessage());
                    }
                }
            });
            String szJsonData = "<%" + Utilities.readTextFileFromAssertPackage((Context) this.mActivity.get(), "json2.js") + "%>\r\n";
            ItemObject.setParam("lpszJSFileContent", new StringBuilder(String.valueOf(szJsonData)).append(Utilities.readTextFileFromAssertPackage((Context) this.mActivity.get(), "getScheduleData.js")).toString());
            ArrayList<String> arrParam = new ArrayList();
            ArrayList<String> arrValue = new ArrayList();
            arrParam.add("userguid");
            arrValue.add(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
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
        this.m_arrClasses.clear();
        this.m_Adapter = null;
    }
}
