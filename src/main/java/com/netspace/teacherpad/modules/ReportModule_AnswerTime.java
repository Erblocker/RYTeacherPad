package com.netspace.teacherpad.modules;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.netspace.library.struct.StudentAnswer;
import com.netspace.library.utilities.Utilities;
import com.netspace.teacherpad.TeacherPadApplication;
import com.netspace.teacherpad.fragments.ReportUserAnswerStatisticFragment.ReportModuleInterface;
import com.netspace.teacherpad.modules.startclass.ReportActivity2;
import java.util.ArrayList;
import java.util.List;

public class ReportModule_AnswerTime implements ReportModuleInterface {
    private DataSet mDataSet;
    private LineChart mLineChart;
    private LineData mLineData;
    private TableLayout mTableLayout;

    public class MyYAxisValueFormatter implements YAxisValueFormatter {
        public String getFormattedValue(float value, YAxis yAxis) {
            return Utilities.getTimeOffsetInHourMinutesSeconds(((long) value) * 1000);
        }
    }

    public boolean initModule(TextView textViewTitle, LinearLayout layoutContent, View chartView, TableLayout table) {
        this.mLineChart = (LineChart) chartView;
        this.mTableLayout = table;
        this.mTableLayout.setVisibility(8);
        this.mLineChart.setTouchEnabled(true);
        this.mLineChart.setDragDecelerationFrictionCoef(0.9f);
        this.mLineChart.setDescription("");
        this.mLineChart.setNoDataText("");
        this.mLineChart.setDragEnabled(true);
        this.mLineChart.setScaleEnabled(true);
        this.mLineChart.setDrawGridBackground(false);
        this.mLineChart.setHighlightPerDragEnabled(true);
        this.mLineChart.setPinchZoom(true);
        YAxisValueFormatter custom = new MyYAxisValueFormatter();
        YAxis leftAxis = this.mLineChart.getAxisLeft();
        leftAxis.setValueFormatter(custom);
        leftAxis.setPosition(YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15.0f);
        leftAxis.setDrawGridLines(true);
        this.mLineChart.getAxisRight().setEnabled(false);
        this.mLineChart.getXAxis().setPosition(XAxisPosition.TOP);
        this.mLineChart.getXAxis().setTextSize(15.0f);
        this.mLineChart.getLegend().setEnabled(false);
        return true;
    }

    public boolean refresh() {
        List xVals = new ArrayList();
        ArrayList<Entry> yVals1 = new ArrayList();
        synchronized (TeacherPadApplication.marrStudentAnswers) {
            for (int i = 0; i < TeacherPadApplication.marrStudentAnswers.size(); i++) {
                StudentAnswer oneAnswer = (StudentAnswer) TeacherPadApplication.marrStudentAnswers.get(i);
                xVals.add(oneAnswer.szStudentName);
                yVals1.add(new Entry((float) (oneAnswer.nTimeInMS / 1000), i));
            }
        }
        LineDataSet set1 = new LineDataSet(yVals1, "");
        set1.setAxisDependency(AxisDependency.LEFT);
        set1.setColor(ReportActivity2.REPORT_COLORS_BLUE[0]);
        set1.setCircleColor(ReportActivity2.REPORT_COLORS_BLUE[0]);
        set1.setLineWidth(2.0f);
        set1.setCircleSize(3.0f);
        set1.setFillAlpha(100);
        set1.setHighLightColor(ColorTemplate.getHoloBlue());
        set1.setValueTextColor(-16777216);
        set1.setDrawCircleHole(false);
        set1.setDrawValues(false);
        List dataSets = new ArrayList();
        dataSets.add(set1);
        this.mLineData = new LineData(xVals, dataSets);
        this.mLineData.setValueTextColor(-1);
        this.mLineData.setValueTextSize(9.0f);
        this.mLineChart.setData(this.mLineData);
        this.mLineChart.invalidate();
        return true;
    }

    public void loadData(String szJsonData) {
    }

    public void setContext(Context context) {
    }
}
