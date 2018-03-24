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
import android.widget.TextView;
import com.netspace.library.activity.ResourceDetailActivity;
import com.netspace.library.activity.VideoPlayerActivity2;
import com.netspace.library.adapter.StudentClassAnswerAdapter;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.components.CommentComponent;
import com.netspace.library.struct.LessonClassData;
import com.netspace.library.struct.StudentClassAnswer;
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

public class StudentClassAnswerFragment extends Fragment implements OnClickListener {
    private static final String TAG = "StudentClassAnswerFragment";
    private StudentClassAnswerAdapter mAdapter;
    private LessonClassData mLastActiveClassData;
    private TextView mNoDataView;
    private boolean mPageActived = false;
    private RecyclerView mRecyclerView;
    private View mRootView;
    private String mSearchKeywords = "";
    private ArrayList<StudentClassAnswer> marrData = new ArrayList();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.mRootView != null) {
            return this.mRootView;
        }
        this.mRootView = inflater.inflate(R.layout.fragment_studentclassanswer, container, false);
        this.mRecyclerView = (RecyclerView) this.mRootView.findViewById(R.id.recentView);
        this.mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 4));
        this.mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        this.mNoDataView = (TextView) this.mRootView.findViewById(R.id.textViewNoData);
        for (int i = 0; i < 10; i++) {
            StudentClassAnswer test = new StudentClassAnswer();
            test.szTime = "2015-1-1 12:00:00";
            test.szAnswerTime = "2015-1-1 12:00:10";
            test.nAnswerResult = 1;
            test.nSubmitIndex = i;
            test.szAnswer = "BCE";
            test.szCorrectAnswer = "ABC";
            test.mapVoteCount.put("star", Integer.valueOf(i * 3));
            test.szClientID = MyiBaseApplication.getCommonVariables().MyiApplication.getClientID();
            test.szPicturePackageID = "Camera_3ba7ea113b03419394f1609f0797dff1.jpg";
            test.nScore = i + 10;
            this.marrData.add(test);
        }
        this.mAdapter = new StudentClassAnswerAdapter(getActivity(), this.marrData);
        this.mAdapter.setOnClickListener(this);
        this.mRecyclerView.setAdapter(this.mAdapter);
        displayEmptyLabel();
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

    private void initData() {
        this.marrData.clear();
        this.mAdapter.notifyDataSetChanged();
    }

    public void loadStaticLessonData() {
        if (VirtualNetworkObject.getOfflineMode()) {
            this.mNoDataView.setVisibility(0);
            this.mNoDataView.setText("当前处于离线模式，无法使用");
            return;
        }
        String szTeacherGUID = "";
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
        if (0 != -1) {
            szScheduleTableSQL = " and lessonsschedule.Subject='" + 0 + "' ";
        }
        if (0 != -1) {
            szClassRecordTableSQL = " and lessonsclass.startTime > SUBDATE(Now(), " + 0 + ") ";
        }
        Log.d("LessonClassViewFragment", "sql=" + new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf("select lessonsclass.userclassname, lessonsclass.starttime, lessonsschedule.guid as scheduleguid, lessonsschedule.resourceguid as scheduleresourceguid,  resources.title, resources.author, lessonsclass.guid from lessonsclass, wmexam.resources, wmexam.lessonsschedule where resources.guid = lessonsschedule.ResourceGUID and lessonsclass.LessonsScheduleGUID = lessonsschedule.guid and resources.type='1000' and lessonsschedule.SYN_IsDelete = '0' " + szTitleSearchSQL)).append(szResourceTableSQL).toString())).append(szScheduleTableSQL).toString())).append(szClassRecordTableSQL).toString())).append(" order by lessonsclass.StartTime desc ").toString());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("operation", "read");
            jsonObject.put("tableName", "resources");
            jsonObject.put("teacherGUID", szTeacherGUID);
            jsonObject.put("subjectID", 0);
            jsonObject.put("timeOffset", 0);
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
                                StudentClassAnswer Data = new StudentClassAnswer();
                                Data.szGUID = oneData.getString("guid");
                                StudentClassAnswerFragment.this.marrData.add(Data);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    StudentClassAnswerFragment.this.displayEmptyLabel();
                    StudentClassAnswerFragment.this.mAdapter.notifyDataSetChanged();
                }
            });
            CallItem.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                    StudentClassAnswerFragment.this.displayEmptyLabel();
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
            this.mLastActiveClassData = null;
            this.mAdapter.notifyDataSetChanged();
        }
        super.onResume();
    }

    public void onClick(View v) {
        LessonClassData data = (LessonClassData) v.getTag();
        Intent intent = new Intent(getActivity(), ResourceDetailActivity.class);
        this.mLastActiveClassData = data;
        if (data.szScheduleGUID.isEmpty()) {
            String szURL = VirtualNetworkObject.getServerAddress();
            szURL = "rtsp://" + szURL.substring(0, szURL.indexOf(":")) + "/" + data.szGUID + "_Mix.sdp";
            Intent intent2 = new Intent(getActivity(), VideoPlayerActivity2.class);
            intent2.setAction("org.videolan.vlc.gui.video.PLAY_FROM_VIDEOGRID");
            intent2.putExtra("itemLocation", szURL);
            intent2.putExtra("itemTitle", data.szTitle);
            startActivity(intent2);
            return;
        }
        intent.putExtra(CommentComponent.RESOURCEGUID, data.szGUID);
        intent.putExtra("isquestion", false);
        intent.putExtra("title", data.szTitle);
        intent.putExtra("resourcetype", 4000);
        startActivity(intent);
    }
}
