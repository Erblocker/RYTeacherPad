package com.netspace.library.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.activity.plugins.ActivityPlugin_AnswerSheetOtherQuestions;
import com.netspace.library.activity.plugins.ActivityPlugin_AnswerSheetOtherQuestions.AnswerSheetCorrectImageChange;
import com.netspace.library.adapter.AnswerSheetV3OtherQuestionAdapter;
import com.netspace.library.components.LessonPrepareCorrectAnswerSheetV2Component;
import com.netspace.library.database.AnswerSheetResult;
import com.netspace.library.database.AnswerSheetResultDao;
import com.netspace.library.database.AnswerSheetStudentAnswer;
import com.netspace.library.database.AnswerSheetStudentAnswerDao.Properties;
import com.netspace.library.database.DaoSession;
import com.netspace.library.struct.RESTSynchronizeComplete;
import com.netspace.library.struct.RESTSynchronizeError;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.RESTEngine;
import com.netspace.pad.library.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

public class AnswerSheetV3OtherQuestionCorrectActivity extends BaseActivity {
    private static final String TAG = "AnswerSheetV3OtherQuestionCorrectActivity";
    private static JSONObject mAnswerSheetJsonData;
    private AnswerSheetV3OtherQuestionAdapter mAdapter;
    private Context mContext;
    private JSONObject mCurrentQuestion = null;
    private DaoSession mDaoSession;
    private RecyclerView mRecycleView;
    private ArrayList<AnswerSheetV3OtherQuestion> marrData = new ArrayList();
    private int mnID = 0;
    private int mnType = 0;
    private String mszLimitClientID;
    private String mszQuestionGUID = "";
    private String mszScheduleGUID;

    public static class AnswerSheetV3OtherQuestion {
        public AnswerSheetResult answerResult;
        public AnswerSheetStudentAnswer studentAnswer;
    }

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_answersheetv2otherquestioncorrect);
        this.mContext = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_bars).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.mRecycleView = (RecyclerView) findViewById(R.id.studentAnswerView);
        this.mRecycleView.setLayoutManager(new GridLayoutManager(this, 3));
        this.mRecycleView.setItemAnimator(new DefaultItemAnimator());
        this.mDaoSession = RESTEngine.getDefault().getDaoSession();
        this.mAdapter = new AnswerSheetV3OtherQuestionAdapter(this, this.marrData, this.mDaoSession);
        this.mRecycleView.setAdapter(this.mAdapter);
        EventBus.getDefault().register(this);
        if (getIntent() != null) {
            if (getIntent().hasExtra("id")) {
                this.mnID = getIntent().getIntExtra("id", -1);
            }
            if (getIntent().hasExtra("scheduleguid")) {
                this.mszScheduleGUID = getIntent().getStringExtra("scheduleguid");
            }
            if (getIntent().hasExtra("limitclientid")) {
                this.mszLimitClientID = getIntent().getStringExtra("limitclientid");
            }
            if (this.mnID >= 0) {
                this.mCurrentQuestion = LessonPrepareCorrectAnswerSheetV2Component.findUserQuestionByIndex(mAnswerSheetJsonData, this.mnID);
                try {
                    this.mszQuestionGUID = this.mCurrentQuestion.getString("guid");
                    setTitle("第" + this.mCurrentQuestion.getString("index") + "题");
                    this.mnType = this.mCurrentQuestion.getInt("type");
                    this.mAdapter.setFullScore((float) this.mCurrentQuestion.getDouble("score"));
                    this.mAdapter.setTitle("第" + this.mCurrentQuestion.getString("index") + "题");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                refresh();
                return;
            }
        }
        finish();
    }

    public static void setAnswerData(JSONObject answerSheetJsonData) {
        mAnswerSheetJsonData = answerSheetJsonData;
    }

    protected void onResume() {
        this.mAdapter.notifyDataSetChanged();
        super.onResume();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_answersheetv2correct, menu);
        menu.findItem(R.id.action_save).setIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_save).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        menu.findItem(R.id.action_refresh).setIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_refresh).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        menu.findItem(R.id.action_prev).setIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_arrow_left).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        menu.findItem(R.id.action_next).setIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_arrow_right).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        menu.findItem(R.id.action_save).setVisible(false);
        MenuItem menuAnswers = menu.findItem(R.id.action_correctanswer);
        if (menuAnswers != null) {
            menuAnswers.setVisible(false);
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        Utilities.logMenuClick(menuItem);
        if (menuItem.getItemId() == 16908332) {
            finish();
        } else if (menuItem.getItemId() == R.id.action_save) {
            RESTEngine.getDefault().getAnswerSheetHelper().doSychronize();
        } else if (menuItem.getItemId() == R.id.action_prev) {
            if (this.mnID > 0) {
                gotoQuestion(this.mnID - 1, menuItem.getItemId());
            }
        } else if (menuItem.getItemId() == R.id.action_next) {
            gotoQuestion(this.mnID + 1, menuItem.getItemId());
        } else if (menuItem.getItemId() == R.id.action_refresh) {
            RESTEngine.getDefault().getAnswerSheetStudentAnswerHelper().doSychronize();
        }
        return true;
    }

    private void gotoQuestion(int nID, int nMenuID) {
        JSONObject question = LessonPrepareCorrectAnswerSheetV2Component.findUserQuestionByIndex(mAnswerSheetJsonData, nID);
        if (question != null) {
            try {
                Intent intent;
                int nType = question.getInt("type");
                if (nType == 0 || nType == 1 || nType == 2) {
                    intent = new Intent(this, AnswerSheetV3SelectQuestionCorrectActivity.class);
                } else {
                    intent = new Intent(this, AnswerSheetV3OtherQuestionCorrectActivity.class);
                }
                intent.putExtra("id", nID);
                intent.putExtra("scheduleguid", this.mszScheduleGUID);
                if (this.mszLimitClientID != null) {
                    intent.putExtra("limitclientid", this.mszLimitClientID);
                }
                intent.setFlags(67108864);
                startActivity(intent);
                if (nMenuID == R.id.action_prev) {
                    overridePendingTransition(R.anim.anim_enter_left, R.anim.anim_leave_right);
                } else {
                    overridePendingTransition(R.anim.anim_enter_right, R.anim.anim_leave_left);
                }
                finish();
                return;
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
        }
        Toast.makeText(this.mContext, "没有题目了", 0).show();
    }

    private boolean refresh() {
        if (mAnswerSheetJsonData == null) {
            throw new NullPointerException("Must call setAnswerData first.");
        }
        PicassoTools.clearCache(Picasso.with(this));
        this.marrData.clear();
        List<AnswerSheetStudentAnswer> answers = this.mDaoSession.queryBuilder(AnswerSheetStudentAnswer.class).where(Properties.Scheduleguid.eq(this.mszScheduleGUID), Properties.Questionguid.eq(this.mszQuestionGUID)).list();
        for (int k = 0; k < answers.size(); k++) {
            AnswerSheetStudentAnswer oneAnswer = (AnswerSheetStudentAnswer) answers.get(k);
            if (this.mszLimitClientID == null || oneAnswer.getClientid().equalsIgnoreCase(this.mszLimitClientID)) {
                AnswerSheetResult answerResult = (AnswerSheetResult) this.mDaoSession.queryBuilder(AnswerSheetResult.class).where(AnswerSheetResultDao.Properties.Scheduleguid.eq(this.mszScheduleGUID), AnswerSheetResultDao.Properties.Questionguid.eq(this.mszQuestionGUID), AnswerSheetResultDao.Properties.Clientid.eq(oneAnswer.getClientid())).limit(1).unique();
                if (answerResult == null) {
                    answerResult = new AnswerSheetResult();
                    answerResult.setGuid(Utilities.createGUID());
                    answerResult.setScheduleguid(this.mszScheduleGUID);
                    answerResult.setQuestionguid(this.mszQuestionGUID);
                    answerResult.setClientid(oneAnswer.getClientid());
                    answerResult.setStudentname(oneAnswer.getStudentname());
                    answerResult.setUsername(oneAnswer.getUsername());
                    answerResult.setAnswersheetresourceguid(oneAnswer.getAnswersheetresourceguid());
                    answerResult.setAnswerscore(Float.valueOf(0.0f));
                    answerResult.setAnswerresult(Integer.valueOf(0));
                    answerResult.setSyn_isdelete(Integer.valueOf(0));
                    answerResult.setSyn_timestamp(new Date());
                }
                AnswerSheetV3OtherQuestion question = new AnswerSheetV3OtherQuestion();
                question.studentAnswer = oneAnswer;
                question.answerResult = answerResult;
                this.marrData.add(question);
            }
        }
        ActivityPlugin_AnswerSheetOtherQuestions.setAnswerData(this.marrData);
        this.mAdapter.notifyDataSetChanged();
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRESTSychronizeComplete(RESTSynchronizeComplete data) {
        if (RESTEngine.getDefault().getAnswerSheetStudentAnswerHelper().isSynchronizeComplete()) {
            refresh();
        }
        if (RESTEngine.getDefault().getAnswerSheetHelper().isSynchronizeComplete()) {
            refresh();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRESTSychronizeError(RESTSynchronizeError data) {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAnswerSheetCorrectImageChange(AnswerSheetCorrectImageChange data) {
        Log.d(TAG, "onAnswerSheetCorrectImageChange");
        PicassoTools.clearCache(Picasso.with(this));
        this.mAdapter.notifyDataSetChanged();
    }

    protected void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }
}
