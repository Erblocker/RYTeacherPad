package com.netspace.library.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.TextView;
import com.esotericsoftware.wildcard.Paths;
import com.netspace.library.activity.ResourceDetailActivity;
import com.netspace.library.activity.VideoPlayerActivity2;
import com.netspace.library.adapter.LessonClassAdapter;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.components.CommentComponent;
import com.netspace.library.consts.Features;
import com.netspace.library.struct.LessonClassData;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.pad.library.R;
import io.vov.vitamio.MediaMetadataRetriever;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;

public class LessonClassViewFragment extends Fragment implements OnClickListener {
    private static final String TAG = "LessonClassViewFragment";
    private LessonClassAdapter mAdapter;
    private LessonClassData mLastActiveClassData;
    private TextView mNoDataView;
    private OnItemSelectedListener mOnSpinnerSelectedListener = new OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            if (LessonClassViewFragment.this.mSpinnerSubjects.getSelectedItemPosition() != -1 && LessonClassViewFragment.this.mSpinnerTimer.getSelectedItemPosition() != -1 && LessonClassViewFragment.this.mSpinnerTeacher.getSelectedItemPosition() != -1 && LessonClassViewFragment.this.mPageActived) {
                LessonClassViewFragment.this.initData();
            }
        }

        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    };
    private boolean mPageActived = false;
    private RecyclerView mRecyclerView;
    private View mRootView;
    private String mSearchKeywords = "";
    private Spinner mSpinnerSubjects;
    private Spinner mSpinnerTeacher;
    private Spinner mSpinnerTimer;
    private ArrayList<LessonClassData> marrData = new ArrayList();
    private ArrayList<String> marrDate = new ArrayList();
    private ArrayList<Integer> marrDateValue = new ArrayList();
    private ArrayList<Integer> marrSubjectIDs = new ArrayList();
    private ArrayList<String> marrTeacherGUID = new ArrayList();
    private ArrayList<String> marrTeacherName = new ArrayList();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.mRootView != null) {
            return this.mRootView;
        }
        this.mRootView = inflater.inflate(R.layout.fragment_lessonclassview, null);
        this.mRecyclerView = (RecyclerView) this.mRootView.findViewById(R.id.recentView);
        this.mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 4));
        this.mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        this.mNoDataView = (TextView) this.mRootView.findViewById(R.id.textViewNoData);
        this.mAdapter = new LessonClassAdapter(getActivity(), this.marrData);
        this.mAdapter.setOnClickListener(this);
        this.mRecyclerView.setAdapter(this.mAdapter);
        initSelection();
        return this.mRootView;
    }

    public void activePage() {
        if (!this.mPageActived) {
            this.mPageActived = true;
            initData();
        }
    }

    public void setSearchKey(String szKey) {
        this.mSearchKeywords = szKey;
        this.mSearchKeywords = this.mSearchKeywords.replace("%", "");
        this.mSearchKeywords = this.mSearchKeywords.replace("'", "");
        this.mSearchKeywords = this.mSearchKeywords.replace("\"", "");
        this.mSearchKeywords = this.mSearchKeywords.replace("*", "");
        if (this.mPageActived) {
            initData();
        }
    }

    protected void scanItemForLocalImage(LessonClassData classData) {
        Iterator it = new Paths(getActivity().getExternalCacheDir().getAbsolutePath(), classData.szGUID + "*.mp4").getFiles().iterator();
        if (it.hasNext()) {
            File file = (File) it.next();
            classData.bDownloaded = true;
            classData.szLocalFileName = file.getAbsolutePath();
        }
    }

    private void initData() {
        this.marrData.clear();
        this.mAdapter.notifyDataSetChanged();
        WebServiceCallItemObject CallItem = new WebServiceCallItemObject("BroadcastGetLessons", getActivity());
        CallItem.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                ArrayList<String> arrTitles = (ArrayList) ItemObject.getParam("0");
                ArrayList<String> arrGUIDs = (ArrayList) ItemObject.getParam("1");
                for (int i = 0; i < arrTitles.size(); i++) {
                    LessonClassData Data = new LessonClassData();
                    Data.szGUID = (String) arrGUIDs.get(i);
                    Data.szTitle = (String) arrTitles.get(i);
                    Data.szTeacherName = "";
                    Data.szDateTime = "正在直播";
                    Data.szUserClassName = "";
                    Data.szScheduleGUID = "";
                    Data.szScheduleResourceGUID = "";
                    LessonClassViewFragment.this.marrData.add(Data);
                }
                LessonClassViewFragment.this.loadStaticLessonData();
            }
        });
        VirtualNetworkObject.addToQueue(CallItem);
    }

    public void loadStaticLessonData() {
        if (VirtualNetworkObject.getOfflineMode()) {
            this.mNoDataView.setVisibility(0);
            this.mNoDataView.setText("当前处于离线模式，无法使用");
            return;
        }
        int nSubjectID = ((Integer) this.marrSubjectIDs.get(this.mSpinnerSubjects.getSelectedItemPosition())).intValue();
        int nTimeOffset = ((Integer) this.marrDateValue.get(this.mSpinnerTimer.getSelectedItemPosition())).intValue();
        String szTeacherGUID = "";
        if (this.mSpinnerTeacher.getSelectedItemPosition() != -1) {
            szTeacherGUID = (String) this.marrTeacherGUID.get(this.mSpinnerTeacher.getSelectedItemPosition());
        }
        String szResourceTableSQL = "";
        String szScheduleTableSQL = "";
        String szClassRecordTableSQL = "";
        String szTitleSearchSQL = "";
        if (!this.mSearchKeywords.isEmpty()) {
            szTitleSearchSQL = " and title like '%" + this.mSearchKeywords + "%' ";
        }
        if (!szTeacherGUID.isEmpty()) {
            szResourceTableSQL = " and resources.acl_ownerguid='" + szTeacherGUID + "' ";
        }
        if (nSubjectID != -1) {
            szScheduleTableSQL = " and lessonsschedule.Subject='" + nSubjectID + "' ";
        }
        if (nTimeOffset != -1) {
            szClassRecordTableSQL = " and lessonsclass.startTime > SUBDATE(Now(), " + nTimeOffset + ") ";
        }
        Log.d(TAG, "sql=" + new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf("select lessonsclass.userclassname, lessonsclass.userclassguid, lessonsclass.starttime, lessonsschedule.guid as scheduleguid, lessonsschedule.resourceguid as scheduleresourceguid,  resources.title, resources.author, lessonsclass.guid from lessonsclass, wmexam.resources, wmexam.lessonsschedule where resources.guid = lessonsschedule.ResourceGUID and lessonsclass.LessonsScheduleGUID = lessonsschedule.guid and resources.type='1000' and lessonsschedule.SYN_IsDelete = '0' " + szTitleSearchSQL)).append(szResourceTableSQL).toString())).append(szScheduleTableSQL).toString())).append(szClassRecordTableSQL).toString())).append(" order by lessonsclass.StartTime desc ").toString());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("operation", "read");
            jsonObject.put("tableName", "resources");
            jsonObject.put("searchKeywords", this.mSearchKeywords);
            jsonObject.put("teacherGUID", szTeacherGUID);
            jsonObject.put("subjectID", nSubjectID);
            jsonObject.put("timeOffset", nTimeOffset);
            jsonObject.put("sql", "%SQL_LessonClassView_Search%");
            WebServiceCallItemObject CallItem = new WebServiceCallItemObject("JsonTableAccess", getActivity());
            CallItem.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    String szResult = ItemObject.readTextData();
                    if (szResult != null) {
                        try {
                            JSONArray arrData = new JSONObject(szResult).getJSONArray("data");
                            for (int i = 0; i < arrData.length(); i++) {
                                JSONObject oneData = arrData.getJSONObject(i);
                                LessonClassData Data = new LessonClassData();
                                Data.szGUID = oneData.getString("guid");
                                Data.szTitle = oneData.getString("title");
                                Data.szTeacherName = oneData.getString(MediaMetadataRetriever.METADATA_KEY_AUTHOR);
                                Data.szDateTime = oneData.getString("starttime");
                                Data.szUserClassName = oneData.getString("userclassname");
                                Data.szScheduleGUID = oneData.getString("scheduleguid");
                                Data.szScheduleResourceGUID = oneData.getString("scheduleresourceguid");
                                if (MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_LESSONCLASS_PRIVATE_ONLY)) {
                                    if (!MyiBaseApplication.getCommonVariables().UserInfo.isInClass(oneData.getString(UserHonourFragment.USERCLASSGUID))) {
                                    }
                                }
                                LessonClassViewFragment.this.scanItemForLocalImage(Data);
                                LessonClassViewFragment.this.marrData.add(Data);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    LessonClassViewFragment.this.displayEmptyLabel();
                    LessonClassViewFragment.this.mAdapter.notifyDataSetChanged();
                }
            });
            CallItem.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                    LessonClassViewFragment.this.displayEmptyLabel();
                }
            });
            CallItem.setParam("lpszJsonInputData", jsonObject.toString());
            VirtualNetworkObject.addToQueue(CallItem);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void displayEmptyLabel() {
        if (this.marrData.size() == 0) {
            this.mNoDataView.setVisibility(0);
        } else {
            this.mNoDataView.setVisibility(4);
        }
    }

    public void onResume() {
        if (this.mLastActiveClassData != null) {
            scanItemForLocalImage(this.mLastActiveClassData);
            this.mLastActiveClassData = null;
            this.mAdapter.notifyDataSetChanged();
        }
        super.onResume();
    }

    protected void initSelection() {
        this.mSpinnerSubjects = (Spinner) this.mRootView.findViewById(R.id.spinnerSubject);
        this.mSpinnerTimer = (Spinner) this.mRootView.findViewById(R.id.spinnerTime);
        this.mSpinnerTeacher = (Spinner) this.mRootView.findViewById(R.id.spinnerTeacher);
        this.mSpinnerSubjects.setOnItemSelectedListener(this.mOnSpinnerSelectedListener);
        this.mSpinnerTimer.setOnItemSelectedListener(this.mOnSpinnerSelectedListener);
        this.mSpinnerTeacher.setOnItemSelectedListener(this.mOnSpinnerSelectedListener);
        ArrayList<String> arrSubjectNames = new ArrayList();
        Utilities.getAllSubjectInfo(arrSubjectNames, this.marrSubjectIDs);
        arrSubjectNames.add(0, "全部科目");
        this.marrSubjectIDs.add(0, Integer.valueOf(-1));
        Utilities.setSpinnerData(getActivity(), this.mSpinnerSubjects, arrSubjectNames);
        this.marrDate.add("一星期内");
        this.marrDateValue.add(Integer.valueOf(7));
        this.marrDate.add("两星期内");
        this.marrDateValue.add(Integer.valueOf(14));
        this.marrDate.add("一个月内");
        this.marrDateValue.add(Integer.valueOf(30));
        this.marrDate.add("两个月内");
        this.marrDateValue.add(Integer.valueOf(60));
        this.marrDate.add("三个月内");
        this.marrDateValue.add(Integer.valueOf(90));
        this.marrDate.add("四个月内");
        this.marrDateValue.add(Integer.valueOf(SoapEnvelope.VER12));
        this.marrDate.add("全部时间");
        this.marrDateValue.add(Integer.valueOf(-1));
        Utilities.setSpinnerData(getActivity(), this.mSpinnerTimer, this.marrDate);
        this.marrTeacherName.add(0, "全部教师");
        this.marrTeacherGUID.add(0, "");
        JSONObject jsonObject = new JSONObject();
        String szSQL = "select author,acl_ownerguid from wmexam.resources where Type='1000' and author != '' group by ACL_OwnerName";
        try {
            jsonObject.put("operation", "read");
            jsonObject.put("tableName", "resources");
            jsonObject.put("sql", "%SQL_LessonClassView_All%");
            WebServiceCallItemObject CallItem = new WebServiceCallItemObject("JsonTableAccess", getActivity());
            CallItem.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    String szResult = ItemObject.readTextData();
                    if (szResult != null) {
                        try {
                            JSONArray arrData = new JSONObject(szResult).getJSONArray("data");
                            for (int i = 0; i < arrData.length(); i++) {
                                JSONObject oneData = arrData.getJSONObject(i);
                                LessonClassViewFragment.this.marrTeacherName.add(oneData.getString(MediaMetadataRetriever.METADATA_KEY_AUTHOR));
                                LessonClassViewFragment.this.marrTeacherGUID.add(oneData.getString("acl_ownerguid"));
                            }
                            Utilities.setSpinnerData(LessonClassViewFragment.this.getActivity(), LessonClassViewFragment.this.mSpinnerTeacher, LessonClassViewFragment.this.marrTeacherName);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            CallItem.setParam("lpszJsonInputData", jsonObject.toString());
            VirtualNetworkObject.addToQueue(CallItem);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onClick(View v) {
        final LessonClassData data = (LessonClassData) v.getTag();
        Intent intent = new Intent(getActivity(), ResourceDetailActivity.class);
        this.mLastActiveClassData = data;
        if (data.szScheduleGUID.isEmpty()) {
            WebServiceCallItemObject CallItem = new WebServiceCallItemObject("BroadcastGetURL", getActivity());
            CallItem.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    String szURL = (String) ItemObject.getParam("0");
                    try {
                        JSONArray json = new JSONArray((String) ItemObject.getParam("1"));
                        String szCameraIndex = "";
                        String szFinalFileName = new StringBuilder(String.valueOf(data.szGUID)).append("_All_0").toString();
                        for (int i = 1; i < json.length(); i++) {
                            JSONObject oneItem = json.getJSONObject(i);
                            if (oneItem.getInt("isvideo") == 1) {
                                szCameraIndex = new StringBuilder(String.valueOf(szCameraIndex)).append(String.valueOf(oneItem.getInt("index"))).toString();
                            }
                        }
                        if (!szCameraIndex.isEmpty()) {
                            szFinalFileName = new StringBuilder(String.valueOf(szFinalFileName)).append("_").append(szCameraIndex).toString();
                        }
                        szURL = szURL.substring(0, szURL.lastIndexOf("/")) + "/" + new StringBuilder(String.valueOf(szFinalFileName)).append("_Live.m3u8").toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Intent intent2 = new Intent(LessonClassViewFragment.this.getActivity(), VideoPlayerActivity2.class);
                    intent2.setAction("org.videolan.vlc.gui.video.PLAY_FROM_VIDEOGRID");
                    intent2.putExtra("itemLocation", szURL);
                    intent2.putExtra("itemTitle", data.szTitle);
                    LessonClassViewFragment.this.startActivity(intent2);
                }
            });
            CallItem.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                    String szURL = VirtualNetworkObject.getServerAddress();
                    szURL = "rtsp://" + szURL.substring(0, szURL.indexOf(":")) + "/" + data.szGUID + "_Mix.sdp";
                    Intent intent2 = new Intent(LessonClassViewFragment.this.getActivity(), VideoPlayerActivity2.class);
                    intent2.setAction("org.videolan.vlc.gui.video.PLAY_FROM_VIDEOGRID");
                    intent2.putExtra("itemLocation", szURL);
                    intent2.putExtra("itemTitle", data.szTitle);
                    LessonClassViewFragment.this.startActivity(intent2);
                }
            });
            CallItem.setParam("lpszScheduleGUID", data.szGUID);
            VirtualNetworkObject.addToQueue(CallItem);
            return;
        }
        intent.putExtra(CommentComponent.RESOURCEGUID, data.szGUID);
        intent.putExtra("isquestion", false);
        intent.putExtra("title", data.szTitle);
        intent.putExtra("resourcetype", 4000);
        startActivity(intent);
    }
}
