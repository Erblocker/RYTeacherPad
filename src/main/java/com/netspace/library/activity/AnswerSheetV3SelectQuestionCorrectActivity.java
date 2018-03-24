package com.netspace.library.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.components.LessonPrepareCorrectAnswerSheetV2Component;
import com.netspace.library.components.LessonPrepareCorrectAnswerSheetV3Component;
import com.netspace.library.database.AnswerSheetStudentAnswer;
import com.netspace.library.database.AnswerSheetStudentAnswerDao.Properties;
import com.netspace.library.database.DaoSession;
import com.netspace.library.struct.RESTSynchronizeComplete;
import com.netspace.library.struct.RESTSynchronizeError;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.RESTEngine;
import com.netspace.pad.library.R;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.kxml2.wap.Wbxml;

public class AnswerSheetV3SelectQuestionCorrectActivity extends BaseActivity {
    private static final int[] REPORT_COLORS_BAR = new int[]{Color.rgb(0, Wbxml.EXT_0, 0), Color.rgb(255, 102, 0), -8604180, -12369080, -548004, -8354327, -959360, -1780908, -13922161, -763045, -7214879};
    private static JSONObject mAnswerSheetJsonData;
    private BarChart mBarChart;
    private BarData mBarData;
    private BarDataSet mBarDataSet;
    private LinearLayout mContentLayout;
    private Context mContext;
    private JSONObject mCurrentQuestion = null;
    private DaoSession mDaoSession;
    private ArrayList<BarEntry> mEntries;
    private TableLayout mTableLayout;
    private ArrayList<TableRow> mTableRows = new ArrayList();
    private int mnID = 0;
    private int mnMenuStartID = (R.id.action_a + 1000);
    private int mnType = 0;
    private String mszCorrectAnswer = "";
    private String mszOptions = "";
    private String mszQuestionGUID = "";
    private String mszScheduleGUID = "";
    private ArrayList<String> mxVals;

    public static class AnswerSheetCorrectAnswerChanged {
    }

    public class SimpleValueFormatter implements ValueFormatter, YAxisValueFormatter {
        private DecimalFormat mFormat = new DecimalFormat("###,###,###,##0");

        public String getFormattedValue(float value, YAxis yAxis) {
            return this.mFormat.format((double) value);
        }

        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return this.mFormat.format((double) value);
        }
    }

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_answersheetv2selectquestioncorrect);
        this.mContext = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_bars).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.mContentLayout = (LinearLayout) findViewById(R.id.layoutContent);
        this.mBarChart = (BarChart) findViewById(R.id.chartAnswers);
        this.mTableLayout = (TableLayout) findViewById(R.id.chartAnswersTable);
        this.mDaoSession = RESTEngine.getDefault().getDaoSession();
        initTable();
        if (getIntent() != null) {
            if (getIntent().hasExtra("id")) {
                this.mnID = getIntent().getIntExtra("id", -1);
            }
            if (getIntent().hasExtra("scheduleguid")) {
                this.mszScheduleGUID = getIntent().getStringExtra("scheduleguid");
            }
            if (this.mnID >= 0) {
                this.mCurrentQuestion = LessonPrepareCorrectAnswerSheetV2Component.findUserQuestionByIndex(mAnswerSheetJsonData, this.mnID);
                try {
                    this.mszQuestionGUID = this.mCurrentQuestion.getString("guid");
                    this.mszCorrectAnswer = this.mCurrentQuestion.getString("correctanswer");
                    setTitle("第" + this.mCurrentQuestion.getString("index") + "题");
                    this.mnType = this.mCurrentQuestion.getInt("type");
                    if (this.mnType == 0) {
                        this.mszOptions = "对错";
                    } else if (this.mnType == 1 || this.mnType == 2) {
                        this.mszOptions = this.mCurrentQuestion.getString("options");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                refresh();
                EventBus.getDefault().register(this);
                return;
            }
        }
        finish();
    }

    public static void setAnswerData(JSONObject answerSheetJsonData) {
        mAnswerSheetJsonData = answerSheetJsonData;
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
            SubMenu menuSubmenu = menuAnswers.getSubMenu();
            menuSubmenu.clear();
            for (int i = 0; i < this.mszOptions.length(); i++) {
                MenuItem oneOption = menuSubmenu.add(1, this.mnMenuStartID + i, 0, String.valueOf(this.mszOptions.charAt(i)).toUpperCase());
                if (this.mszCorrectAnswer.contains(String.valueOf(this.mszOptions.charAt(i)))) {
                    oneOption.setChecked(true);
                }
            }
            if (this.mnType == 2) {
                menuSubmenu.setGroupCheckable(1, true, false);
            } else {
                menuSubmenu.setGroupCheckable(1, true, true);
            }
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
                overridePendingTransition(R.anim.anim_enter_left, R.anim.anim_leave_right);
                gotoQuestion(this.mnID - 1, menuItem.getItemId());
            }
        } else if (menuItem.getItemId() == R.id.action_next) {
            overridePendingTransition(R.anim.anim_enter_right, R.anim.anim_leave_left);
            gotoQuestion(this.mnID + 1, menuItem.getItemId());
        } else if (menuItem.getItemId() == R.id.action_refresh) {
            RESTEngine.getDefault().getAnswerSheetStudentAnswerHelper().doSychronize();
        } else if (menuItem.getItemId() >= this.mnMenuStartID) {
            String szAnswer = menuItem.getTitle().toString();
            if (this.mnType != 2) {
                this.mszCorrectAnswer = menuItem.getTitle().toString();
            } else if (this.mszCorrectAnswer.indexOf(szAnswer) != -1) {
                this.mszCorrectAnswer = this.mszCorrectAnswer.replace(szAnswer, "");
            } else {
                this.mszCorrectAnswer += szAnswer;
                char[] charArray = this.mszCorrectAnswer.toCharArray();
                Arrays.sort(charArray);
                this.mszCorrectAnswer = new String(charArray);
            }
            try {
                this.mCurrentQuestion.put("correctanswer", this.mszCorrectAnswer);
                LessonPrepareCorrectAnswerSheetV3Component.findUserQuestionByIndexAndSetCorrectAnswer(mAnswerSheetJsonData, this.mnID, this.mszCorrectAnswer);
                EventBus.getDefault().post(new AnswerSheetCorrectAnswerChanged());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            invalidateOptionsMenu();
            refresh();
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

    private void initTable() {
        this.mBarChart.setDrawBarShadow(false);
        this.mBarChart.setDrawValueAboveBar(true);
        this.mBarChart.setDescription("");
        this.mBarChart.setNoDataText("");
        this.mBarChart.getLegend().setPosition(LegendPosition.RIGHT_OF_CHART);
        this.mBarChart.setPinchZoom(false);
        this.mBarChart.setDrawGridBackground(false);
        SimpleValueFormatter customFormatter = new SimpleValueFormatter();
        XAxis xAxis = this.mBarChart.getXAxis();
        xAxis.setPosition(XAxisPosition.BOTTOM);
        xAxis.setTextSize(15.0f);
        xAxis.setDrawGridLines(false);
        xAxis.setSpaceBetweenLabels(2);
        YAxis leftAxis = this.mBarChart.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        leftAxis.setPosition(YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15.0f);
        YAxis rightAxis = this.mBarChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setLabelCount(8, false);
        rightAxis.setSpaceTop(15.0f);
        rightAxis.setEnabled(false);
        Legend l = this.mBarChart.getLegend();
        l.setPosition(LegendPosition.BELOW_CHART_RIGHT);
        l.setForm(LegendForm.SQUARE);
        l.setFormSize(9.0f);
        l.setTextSize(11.0f);
        l.setXEntrySpace(4.0f);
        l.setEnabled(false);
        this.mBarChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                Iterator it = AnswerSheetV3SelectQuestionCorrectActivity.this.mTableRows.iterator();
                while (it.hasNext()) {
                    Utilities.setViewBackground((TableRow) it.next(), null);
                }
                ((TableRow) AnswerSheetV3SelectQuestionCorrectActivity.this.mTableRows.get(e.getXIndex())).setBackgroundResource(R.drawable.background_blueframe);
            }

            public void onNothingSelected() {
                Iterator it = AnswerSheetV3SelectQuestionCorrectActivity.this.mTableRows.iterator();
                while (it.hasNext()) {
                    Utilities.setViewBackground((TableRow) it.next(), null);
                }
            }
        });
    }

    private void registerName(String szOneChar, String szStudentName, HashMap<String, Integer> mapAnswerCount, HashMap<String, String> mapAnswerStudentName) {
        String szName = "";
        if (mapAnswerCount.containsKey(szOneChar)) {
            mapAnswerCount.put(szOneChar, Integer.valueOf(((Integer) mapAnswerCount.get(szOneChar)).intValue() + 1));
        } else {
            mapAnswerCount.put(szOneChar, Integer.valueOf(1));
        }
        if (mapAnswerStudentName.containsKey(szOneChar)) {
            szName = (String) mapAnswerStudentName.get(szOneChar);
            if (!szName.isEmpty()) {
                szName = new StringBuilder(String.valueOf(szName)).append("、").toString();
            }
            mapAnswerStudentName.put(szOneChar, new StringBuilder(String.valueOf(szName)).append(szStudentName).toString());
            return;
        }
        mapAnswerStudentName.put(szOneChar, szStudentName);
    }

    private boolean refresh() {
        if (mAnswerSheetJsonData == null) {
            throw new NullPointerException("Must call setAnswerData first.");
        }
        HashMap<String, Integer> mapAnswerCount = new HashMap();
        HashMap<String, String> mapAnswerStudentName = new HashMap();
        int nValidCount = 0;
        ArrayList arrNames = new ArrayList();
        String szStudentName = "";
        String szAnswer = "";
        List<AnswerSheetStudentAnswer> answers = this.mDaoSession.queryBuilder(AnswerSheetStudentAnswer.class).where(Properties.Scheduleguid.eq(this.mszScheduleGUID), Properties.Questionguid.eq(this.mszQuestionGUID)).list();
        for (int k = 0; k < answers.size(); k++) {
            int i;
            AnswerSheetStudentAnswer oneAnswer = (AnswerSheetStudentAnswer) answers.get(k);
            szStudentName = oneAnswer.getStudentname();
            szAnswer = oneAnswer.getAnswerchoice().toUpperCase();
            registerName(szAnswer, szStudentName, mapAnswerCount, mapAnswerStudentName);
            if (!Utilities.isInArray(arrNames, szAnswer)) {
                arrNames.add(szAnswer);
            }
            if (szAnswer.length() > 1) {
                for (i = 0; i < szAnswer.length(); i++) {
                    String szOneChar = "包含" + String.valueOf(szAnswer.charAt(i));
                    registerName(szOneChar, szStudentName, mapAnswerCount, mapAnswerStudentName);
                    if (!Utilities.isInArray(arrNames, szOneChar)) {
                        arrNames.add(szOneChar);
                    }
                }
            }
            nValidCount++;
        }
        if (nValidCount == 0) {
            this.mTableLayout.setVisibility(8);
            return false;
        }
        this.mTableLayout.setVisibility(0);
        if (this.mBarData != null) {
            this.mBarData.removeDataSet(this.mBarDataSet);
            this.mBarDataSet = null;
        }
        this.mEntries = new ArrayList();
        this.mxVals = new ArrayList();
        Collections.sort(arrNames, String.CASE_INSENSITIVE_ORDER);
        for (i = 0; i < arrNames.size(); i++) {
            this.mxVals.add((String) arrNames.get(i));
        }
        for (i = 0; i < arrNames.size(); i++) {
            this.mEntries.add(new BarEntry((float) ((Integer) mapAnswerCount.get(arrNames.get(i))).intValue(), i));
        }
        this.mBarDataSet = new BarDataSet(this.mEntries, "回答情况");
        this.mBarDataSet.setColors(REPORT_COLORS_BAR);
        this.mBarDataSet.setBarSpacePercent(35.0f);
        this.mBarDataSet.setValueFormatter(new SimpleValueFormatter());
        this.mBarDataSet.setValueTextColor(-16777216);
        this.mBarDataSet.setValueTextSize(12.0f);
        this.mBarData = new BarData((List) this.mxVals, this.mBarDataSet);
        this.mBarChart.setData(this.mBarData);
        this.mBarChart.notifyDataSetChanged();
        this.mBarChart.invalidate();
        this.mTableRows.clear();
        this.mTableLayout.removeAllViews();
        for (i = 0; i < arrNames.size(); i++) {
            String szName = (String) arrNames.get(i);
            String szStudentNames = (String) mapAnswerStudentName.get(szName);
            TableRow row = new TableRow(this.mContext);
            View title = new TextView(this.mContext);
            int[] iArr = new int[2];
            TypedArray a = this.mContext.obtainStyledAttributes(16842818, new int[]{16842901, 16842904});
            int textSize = a.getDimensionPixelSize(0, -1);
            int textColor = a.getColor(1, -9211021);
            a.recycle();
            title.setText(szName);
            title.setTextColor(-16777216);
            title.setTextSize((float) textSize);
            title.setGravity(17);
            row.addView(title);
            if (szName.equalsIgnoreCase(this.mszCorrectAnswer)) {
                title.setBackgroundColor(-4128884);
                title.setText(new StringBuilder(String.valueOf(szName)).append("(正确答案)").toString());
            }
            LayoutParams params = (LayoutParams) title.getLayoutParams();
            int dpToPixel = Utilities.dpToPixel(8, this.mContext);
            params.bottomMargin = dpToPixel;
            params.topMargin = dpToPixel;
            params.rightMargin = dpToPixel;
            params.leftMargin = dpToPixel;
            title.setLayoutParams(params);
            title = new TextView(this.mContext);
            title.setText(szStudentNames);
            title.setTextColor(textColor);
            title.setTextSize((float) textSize);
            title.setGravity(19);
            row.addView(title);
            params = (LayoutParams) title.getLayoutParams();
            dpToPixel = Utilities.dpToPixel(8, this.mContext);
            params.bottomMargin = dpToPixel;
            params.topMargin = dpToPixel;
            params.rightMargin = dpToPixel;
            params.leftMargin = dpToPixel;
            params.weight = 0.8f;
            title.setLayoutParams(params);
            this.mTableRows.add(row);
            this.mTableLayout.addView(row);
        }
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRESTSychronizeComplete(RESTSynchronizeComplete data) {
        if (RESTEngine.getDefault().getAnswerSheetStudentAnswerHelper().isSynchronizeComplete()) {
            refresh();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRESTSychronizeError(RESTSynchronizeError data) {
    }

    protected void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }
}
