package com.netspace.teacherpad.modules;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.netspace.library.struct.StudentAnswer;
import com.netspace.library.utilities.Utilities;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.TeacherPadApplication;
import com.netspace.teacherpad.fragments.ReportUserAnswerStatisticFragment.ReportModuleInterface;
import com.netspace.teacherpad.modules.startclass.ReportActivity2;
import java.util.ArrayList;
import java.util.Iterator;

public class ReportModule_CorrectWrong implements ReportModuleInterface {
    private PieChart mCorrectRateChart;
    private TableRow mCorrectRow;
    private TextView mCorrectTextView;
    private ArrayList<Entry> mEntries;
    private PieData mPieData;
    private PieDataSet mPieDataSet;
    private TableLayout mTableLayout;
    private TextView mTextViewTitle;
    private TableRow mWrongRow;
    private TextView mWrongTextView;
    private ArrayList<String> mxVals;

    public boolean initModule(TextView textViewTitle, LinearLayout layoutContent, View chartView, TableLayout table) {
        this.mTextViewTitle = textViewTitle;
        this.mCorrectRateChart = (PieChart) chartView;
        this.mTableLayout = table;
        this.mCorrectRow = (TableRow) this.mTableLayout.getChildAt(0);
        this.mCorrectTextView = (TextView) this.mCorrectRow.getChildAt(1);
        this.mWrongRow = (TableRow) this.mTableLayout.getChildAt(1);
        this.mWrongTextView = (TextView) this.mWrongRow.getChildAt(1);
        this.mCorrectTextView.setText("");
        this.mWrongTextView.setText("");
        this.mCorrectRateChart.setHoleRadius(45.0f);
        this.mCorrectRateChart.setTransparentCircleRadius(50.0f);
        this.mCorrectRateChart.setDescription("");
        this.mCorrectRateChart.getLegend().setPosition(LegendPosition.RIGHT_OF_CHART);
        this.mEntries = new ArrayList();
        this.mxVals = new ArrayList();
        this.mxVals.add("正确");
        this.mxVals.add("错误");
        for (int i = 0; i < 2; i++) {
            this.mxVals.add("entry" + (i + 1));
            this.mEntries.add(new Entry(0.0f, i));
        }
        this.mPieDataSet = new PieDataSet(this.mEntries, "");
        this.mPieDataSet.setColors(ReportActivity2.REPORT_COLORS);
        this.mPieDataSet.setSliceSpace(2.0f);
        this.mPieDataSet.setValueFormatter(new PercentFormatter());
        this.mPieDataSet.setValueTextColor(-16777216);
        this.mPieDataSet.setValueTextSize(12.0f);
        this.mPieData = new PieData(this.mxVals, this.mPieDataSet);
        this.mCorrectRateChart.setData(this.mPieData);
        this.mCorrectRateChart.setUsePercentValues(true);
        this.mCorrectRateChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                Utilities.setViewBackground(ReportModule_CorrectWrong.this.mCorrectRow, null);
                Utilities.setViewBackground(ReportModule_CorrectWrong.this.mWrongRow, null);
                if (e.getXIndex() == 0) {
                    ReportModule_CorrectWrong.this.mCorrectRow.setBackgroundResource(R.drawable.background_blueframe);
                } else if (e.getXIndex() == 1) {
                    ReportModule_CorrectWrong.this.mWrongRow.setBackgroundResource(R.drawable.background_blueframe);
                }
            }

            public void onNothingSelected() {
                Utilities.setViewBackground(ReportModule_CorrectWrong.this.mCorrectRow, null);
                Utilities.setViewBackground(ReportModule_CorrectWrong.this.mWrongRow, null);
            }
        });
        return true;
    }

    public boolean refresh() {
        String szCorrectStudentName = "";
        String szWrongStudentName = "";
        int nCorrectCount = 0;
        int nWrongCount = 0;
        synchronized (TeacherPadApplication.marrStudentAnswers) {
            Iterator it = TeacherPadApplication.marrStudentAnswers.iterator();
            while (it.hasNext()) {
                StudentAnswer oneAnswer = (StudentAnswer) it.next();
                if (oneAnswer.bCorrect) {
                    if (!szCorrectStudentName.isEmpty()) {
                        szCorrectStudentName = new StringBuilder(String.valueOf(szCorrectStudentName)).append("、").toString();
                    }
                    szCorrectStudentName = new StringBuilder(String.valueOf(szCorrectStudentName)).append(oneAnswer.szStudentName).toString();
                    nCorrectCount++;
                }
                if (oneAnswer.bWrong) {
                    if (!szWrongStudentName.isEmpty()) {
                        szWrongStudentName = new StringBuilder(String.valueOf(szWrongStudentName)).append("、").toString();
                    }
                    szWrongStudentName = new StringBuilder(String.valueOf(szWrongStudentName)).append(oneAnswer.szStudentName).toString();
                    nWrongCount++;
                }
            }
        }
        this.mPieDataSet.removeEntry(0);
        this.mPieDataSet.removeEntry(1);
        this.mPieDataSet.addEntry(new Entry((float) nCorrectCount, 0));
        this.mPieDataSet.addEntry(new Entry((float) nWrongCount, 1));
        if (szCorrectStudentName.isEmpty()) {
            szCorrectStudentName = "无";
        }
        if (szWrongStudentName.isEmpty()) {
            szWrongStudentName = "无";
        }
        this.mCorrectTextView.setText(szCorrectStudentName);
        this.mWrongTextView.setText(szWrongStudentName);
        this.mPieData.notifyDataChanged();
        this.mCorrectRateChart.notifyDataSetChanged();
        this.mCorrectRateChart.invalidate();
        if (nCorrectCount == 0 && nWrongCount == 0) {
            this.mCorrectRateChart.setVisibility(8);
            this.mTableLayout.setVisibility(8);
            this.mTextViewTitle.setVisibility(8);
            return false;
        }
        this.mCorrectRateChart.setVisibility(0);
        this.mTableLayout.setVisibility(0);
        this.mTextViewTitle.setVisibility(0);
        return true;
    }

    public void loadData(String szJsonData) {
    }

    public void setContext(Context context) {
    }
}
