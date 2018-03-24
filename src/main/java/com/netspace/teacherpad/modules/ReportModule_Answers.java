package com.netspace.teacherpad.modules;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
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
import com.netspace.library.struct.StudentAnswer;
import com.netspace.library.utilities.Utilities;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.TeacherPadApplication;
import com.netspace.teacherpad.fragments.ReportUserAnswerStatisticFragment.ReportModuleInterface;
import com.netspace.teacherpad.modules.startclass.ReportActivity2;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ReportModule_Answers implements ReportModuleInterface {
    private BarChart mBarChart;
    private BarData mBarData;
    private BarDataSet mBarDataSet;
    private Context mContext;
    private ArrayList<BarEntry> mEntries;
    private TableLayout mTableLayout;
    private ArrayList<TableRow> mTableRows = new ArrayList();
    private TextView mTextViewLabel;
    private ArrayList<String> mxVals;

    public class SimpleValueFormatter implements ValueFormatter, YAxisValueFormatter {
        private DecimalFormat mFormat = new DecimalFormat("###,###,###,##0");

        public String getFormattedValue(float value, YAxis yAxis) {
            return this.mFormat.format((double) value);
        }

        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return this.mFormat.format((double) value);
        }
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public boolean initModule(TextView textViewTitle, LinearLayout layoutContent, View chartView, TableLayout table) {
        this.mBarChart = (BarChart) chartView;
        this.mTableLayout = table;
        this.mTextViewLabel = textViewTitle;
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
                Iterator it = ReportModule_Answers.this.mTableRows.iterator();
                while (it.hasNext()) {
                    Utilities.setViewBackground((TableRow) it.next(), null);
                }
                ((TableRow) ReportModule_Answers.this.mTableRows.get(e.getXIndex())).setBackgroundResource(R.drawable.background_blueframe);
            }

            public void onNothingSelected() {
                Iterator it = ReportModule_Answers.this.mTableRows.iterator();
                while (it.hasNext()) {
                    Utilities.setViewBackground((TableRow) it.next(), null);
                }
            }
        });
        return true;
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

    public boolean refresh() {
        int i;
        HashMap<String, Integer> mapAnswerCount = new HashMap();
        HashMap<String, String> mapAnswerStudentName = new HashMap();
        int nValidCount = 0;
        ArrayList arrNames = new ArrayList();
        synchronized (TeacherPadApplication.marrStudentAnswers) {
            Iterator it = TeacherPadApplication.marrStudentAnswers.iterator();
            while (it.hasNext()) {
                StudentAnswer oneAnswer = (StudentAnswer) it.next();
                if (!oneAnswer.bIsHandWrite) {
                    nValidCount++;
                    String szAnswer = oneAnswer.szAnswerOrPictureKey.toUpperCase();
                    registerName(szAnswer, oneAnswer.szStudentName, mapAnswerCount, mapAnswerStudentName);
                    if (!Utilities.isInArray(arrNames, szAnswer)) {
                        arrNames.add(szAnswer);
                    }
                    if (szAnswer.length() > 1) {
                        for (i = 0; i < szAnswer.length(); i++) {
                            String szOneChar = "包含" + String.valueOf(szAnswer.charAt(i));
                            registerName(szOneChar, oneAnswer.szStudentName, mapAnswerCount, mapAnswerStudentName);
                            if (!Utilities.isInArray(arrNames, szOneChar)) {
                                arrNames.add(szOneChar);
                            }
                        }
                    }
                }
            }
        }
        if (nValidCount == 0) {
            return false;
        }
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
        this.mBarDataSet.setColors(ReportActivity2.REPORT_COLORS_BAR);
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
                title.setText(new StringBuilder(String.valueOf(szName)).append("(正确答案)").toString());
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
        return true;
    }

    public void loadData(String szJsonData) {
    }
}
