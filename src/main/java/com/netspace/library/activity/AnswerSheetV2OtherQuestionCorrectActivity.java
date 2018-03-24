package com.netspace.library.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.adapter.AnswerSheetV2OtherQuestionAdapter;
import com.netspace.library.components.LessonPrepareCorrectAnswerSheetV2Component;
import com.netspace.library.components.LessonPrepareCorrectAnswerSheetV2Component.LoadUserData;
import com.netspace.library.struct.AnswerSheetV2QuestionItem;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AnswerSheetV2OtherQuestionCorrectActivity extends BaseActivity {
    private static JSONObject mAnswerSheetJsonData;
    private static ArrayList<LoadUserData> marrLoadUserData = new ArrayList();
    private AnswerSheetV2OtherQuestionAdapter mAdapter;
    private Context mContext;
    private JSONObject mCurrentQuestion = null;
    private RecyclerView mRecycleView;
    private ArrayList<AnswerSheetV2OtherQuestion> marrData = new ArrayList();
    private int mnID = 0;
    private int mnType = 0;
    private String mszResourceGUID = "";

    public static class AnswerSheetV2OtherQuestion {
        public LoadUserData loadUserData;
        public AnswerSheetV2QuestionItem questionItem;
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
        this.mAdapter = new AnswerSheetV2OtherQuestionAdapter(this, this.marrData);
        this.mRecycleView.setAdapter(this.mAdapter);
        if (getIntent() != null) {
            if (getIntent().hasExtra("id")) {
                this.mnID = getIntent().getIntExtra("id", -1);
            }
            if (this.mnID >= 0) {
                this.mCurrentQuestion = LessonPrepareCorrectAnswerSheetV2Component.findUserQuestionByIndex(mAnswerSheetJsonData, this.mnID);
                try {
                    this.mszResourceGUID = this.mCurrentQuestion.getString("guid");
                    setTitle("第" + this.mCurrentQuestion.getString("index") + "题");
                    this.mnType = this.mCurrentQuestion.getInt("type");
                    this.mAdapter.setFullScore((float) this.mCurrentQuestion.getDouble("score"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                refresh();
                return;
            }
        }
        finish();
    }

    public static void setAnswerData(JSONObject answerSheetJsonData, ArrayList<LoadUserData> arrLoadUserData) {
        marrLoadUserData = arrLoadUserData;
        mAnswerSheetJsonData = answerSheetJsonData;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_answersheetv2correct, menu);
        menu.findItem(R.id.action_save).setIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_save).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        menu.findItem(R.id.action_refresh).setIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_refresh).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        menu.findItem(R.id.action_prev).setIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_arrow_left).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        menu.findItem(R.id.action_next).setIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_arrow_right).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
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
            LessonPrepareCorrectAnswerSheetV2Component.saveCorrectData();
        } else if (menuItem.getItemId() == R.id.action_prev) {
            if (this.mnID > 0) {
                gotoQuestion(this.mnID - 1, menuItem.getItemId());
            }
        } else if (menuItem.getItemId() == R.id.action_next) {
            gotoQuestion(this.mnID + 1, menuItem.getItemId());
        } else if (menuItem.getItemId() == R.id.action_refresh) {
            PicassoTools.clearCache(Picasso.with(this));
            LessonPrepareCorrectAnswerSheetV2Component.refreshAnsweredStudentList(new Runnable() {
                public void run() {
                    AnswerSheetV2OtherQuestionCorrectActivity.this.refresh();
                }
            }, null);
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
                    intent = new Intent(this, AnswerSheetV2SelectQuestionCorrectActivity.class);
                } else {
                    intent = new Intent(this, AnswerSheetV2OtherQuestionCorrectActivity.class);
                }
                intent.putExtra("id", nID);
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
        if (marrLoadUserData == null || mAnswerSheetJsonData == null) {
            throw new NullPointerException("Must call setAnswerData first.");
        }
        this.marrData.clear();
        for (int k = 0; k < marrLoadUserData.size(); k++) {
            LoadUserData oneData = (LoadUserData) marrLoadUserData.get(k);
            if (oneData.getFullJSON() != null) {
                boolean bFound = false;
                JSONObject jsonData = oneData.getJSON();
                if (jsonData.has("category")) {
                    try {
                        if (jsonData.getJSONArray("category").length() > 0) {
                            JSONArray jsonArray = jsonData.getJSONArray("category");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONArray arrQuestions = jsonArray.getJSONObject(i).getJSONArray("questions");
                                int j = 0;
                                while (j < arrQuestions.length()) {
                                    JSONObject oneQuestion = arrQuestions.getJSONObject(j);
                                    String szQuestionGUID = oneQuestion.getString("guid");
                                    if (szQuestionGUID.equalsIgnoreCase(this.mszResourceGUID)) {
                                        AnswerSheetV2OtherQuestion question = new AnswerSheetV2OtherQuestion();
                                        question.loadUserData = oneData;
                                        question.questionItem = new AnswerSheetV2QuestionItem();
                                        question.questionItem.szGuid = szQuestionGUID;
                                        if (oneQuestion.has("answer0")) {
                                            question.questionItem.szAnswer0 = oneQuestion.getString("answer0");
                                        }
                                        if (oneQuestion.has("answer1")) {
                                            question.questionItem.szAnswer1 = oneQuestion.getString("answer1");
                                        }
                                        if (oneQuestion.has("answer1preview")) {
                                            question.questionItem.szAnswer1Preview = oneQuestion.getString("answer1preview");
                                        }
                                        if (oneQuestion.has("answer2")) {
                                            question.questionItem.szAnswer2 = oneQuestion.getString("answer2");
                                        }
                                        this.marrData.add(question);
                                        bFound = true;
                                        if (!bFound) {
                                            break;
                                        }
                                    } else {
                                        j++;
                                    }
                                }
                                if (!bFound) {
                                    break;
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                oneData.releaseJSON();
            }
        }
        this.mAdapter.notifyDataSetChanged();
        return true;
    }
}
