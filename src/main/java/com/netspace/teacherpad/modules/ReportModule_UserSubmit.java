package com.netspace.teacherpad.modules;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
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
import java.util.List;

public class ReportModule_UserSubmit implements ReportModuleInterface {
    private Context mContext;
    private ArrayList<Entry> mEntries;
    private PieChart mPieChart;
    private PieData mPieData;
    private PieDataSet mPieDataSet;
    private TableLayout mTableLayout;
    private ArrayList<TableRow> mTableRows = new ArrayList();
    private TextView mTextViewLabel;
    private ArrayList<String> mxVals;

    public void setContext(Context context) {
        this.mContext = context;
    }

    public boolean initModule(TextView textViewTitle, LinearLayout layoutContent, View chartView, TableLayout table) {
        this.mPieChart = (PieChart) chartView;
        this.mTableLayout = table;
        this.mTextViewLabel = textViewTitle;
        this.mPieChart.setHoleRadius(45.0f);
        this.mPieChart.setTransparentCircleRadius(50.0f);
        this.mPieChart.setDescription("");
        this.mPieChart.getLegend().setPosition(LegendPosition.RIGHT_OF_CHART);
        this.mPieChart.setUsePercentValues(true);
        this.mPieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                Iterator it = ReportModule_UserSubmit.this.mTableRows.iterator();
                while (it.hasNext()) {
                    Utilities.setViewBackground((TableRow) it.next(), null);
                }
                ((TableRow) ReportModule_UserSubmit.this.mTableRows.get(e.getXIndex())).setBackgroundResource(R.drawable.background_blueframe);
            }

            public void onNothingSelected() {
                Iterator it = ReportModule_UserSubmit.this.mTableRows.iterator();
                while (it.hasNext()) {
                    Utilities.setViewBackground((TableRow) it.next(), null);
                }
            }
        });
        return true;
    }

    public boolean refresh() {
        int i;
        ArrayList<String> arrNames = new ArrayList();
        String szAnsweredStudentNames = "";
        String szUnAnsweredStudentNames = "";
        int nAnswerCount = 0;
        int nUnAnswerCount = 0;
        ArrayList arrAnsweredJIDs = new ArrayList();
        ArrayList<String> arrStudentNames = new ArrayList();
        int[] arrData = new int[2];
        boolean bResult = false;
        arrNames.add("已回答");
        arrNames.add("未回答");
        synchronized (TeacherPadApplication.marrStudentAnswers) {
            Iterator it = TeacherPadApplication.marrStudentAnswers.iterator();
            while (it.hasNext()) {
                StudentAnswer oneAnswer = (StudentAnswer) it.next();
                if (!szAnsweredStudentNames.isEmpty()) {
                    szAnsweredStudentNames = new StringBuilder(String.valueOf(szAnsweredStudentNames)).append("、").toString();
                }
                szAnsweredStudentNames = new StringBuilder(String.valueOf(szAnsweredStudentNames)).append(oneAnswer.szStudentName).toString();
                nAnswerCount++;
                arrAnsweredJIDs.add(oneAnswer.szStudentJID);
                bResult = true;
            }
        }
        synchronized (TeacherPadApplication.marrRequiredStudentAnswer) {
            it = TeacherPadApplication.marrRequiredStudentAnswer.iterator();
            while (it.hasNext()) {
                oneAnswer = (StudentAnswer) it.next();
                if (!Utilities.isInArray(arrAnsweredJIDs, oneAnswer.szStudentJID)) {
                    if (!szUnAnsweredStudentNames.isEmpty()) {
                        szUnAnsweredStudentNames = new StringBuilder(String.valueOf(szUnAnsweredStudentNames)).append("、").toString();
                    }
                    nUnAnswerCount++;
                    szUnAnsweredStudentNames = new StringBuilder(String.valueOf(szUnAnsweredStudentNames)).append(oneAnswer.szStudentName).toString();
                    bResult = true;
                }
            }
        }
        arrData[0] = nAnswerCount;
        arrData[1] = nUnAnswerCount;
        arrStudentNames.add(szAnsweredStudentNames);
        arrStudentNames.add(szUnAnsweredStudentNames);
        if (this.mPieData != null) {
            this.mPieData.removeDataSet(this.mPieDataSet);
            this.mPieDataSet = null;
        }
        this.mEntries = new ArrayList();
        this.mxVals = new ArrayList();
        for (i = 0; i < arrNames.size(); i++) {
            this.mxVals.add((String) arrNames.get(i));
        }
        for (i = 0; i < arrNames.size(); i++) {
            this.mxVals.add("entry" + (i + 1));
            this.mEntries.add(new Entry((float) arrData[i], i));
        }
        this.mPieDataSet = new PieDataSet(this.mEntries, "");
        this.mPieDataSet.setColors(ReportActivity2.REPORT_COLORS);
        this.mPieDataSet.setSliceSpace(2.0f);
        this.mPieDataSet.setValueFormatter(new PercentFormatter());
        this.mPieDataSet.setValueTextColor(-16777216);
        this.mPieDataSet.setValueTextSize(12.0f);
        if (this.mPieData == null) {
            this.mPieData = new PieData((List) this.mxVals, this.mPieDataSet);
            this.mPieChart.setData(this.mPieData);
        } else {
            this.mPieData.addDataSet(this.mPieDataSet);
        }
        this.mPieChart.notifyDataSetChanged();
        this.mPieChart.invalidate();
        this.mTableRows.clear();
        this.mTableLayout.removeAllViews();
        for (i = 0; i < arrNames.size(); i++) {
            String szName = (String) arrNames.get(i);
            String szStudentNames = (String) arrStudentNames.get(i);
            TableRow row = new TableRow(TeacherPadApplication.mBaseContext);
            View title = new TextView(TeacherPadApplication.mBaseContext);
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
            if (szName.equalsIgnoreCase(TeacherPadApplication.szCorrectAnswer)) {
                title.setBackgroundColor(-4128884);
            }
            LayoutParams params = (LayoutParams) title.getLayoutParams();
            int dpToPixel = Utilities.dpToPixel(8, TeacherPadApplication.mBaseContext);
            params.bottomMargin = dpToPixel;
            params.topMargin = dpToPixel;
            params.rightMargin = dpToPixel;
            params.leftMargin = dpToPixel;
            title.setLayoutParams(params);
            title = new TextView(TeacherPadApplication.mBaseContext);
            title.setText(szStudentNames);
            title.setTextColor(textColor);
            title.setTextSize((float) textSize);
            title.setGravity(19);
            row.addView(title);
            params = (LayoutParams) title.getLayoutParams();
            dpToPixel = Utilities.dpToPixel(8, TeacherPadApplication.mBaseContext);
            params.bottomMargin = dpToPixel;
            params.topMargin = dpToPixel;
            params.rightMargin = dpToPixel;
            params.leftMargin = dpToPixel;
            params.weight = 0.8f;
            title.setLayoutParams(params);
            this.mTableRows.add(row);
            this.mTableLayout.addView(row);
        }
        return bResult;
    }

    public void loadData(String szJsonData) {
    }
}
