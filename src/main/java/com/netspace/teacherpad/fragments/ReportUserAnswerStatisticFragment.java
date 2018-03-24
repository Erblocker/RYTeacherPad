package com.netspace.teacherpad.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.modules.ReportModule_Answers;
import com.netspace.teacherpad.modules.ReportModule_CorrectWrong;
import java.util.ArrayList;
import java.util.Iterator;

public class ReportUserAnswerStatisticFragment extends Fragment {
    private Handler mHandler = new Handler();
    private View mLayoutContent;
    private View mRootView;
    private TextView mTextViewMessage;
    private ArrayList<ReportModuleInterface> marrReportModules;

    public interface ReportModuleInterface {
        boolean initModule(TextView textView, LinearLayout linearLayout, View view, TableLayout tableLayout);

        void loadData(String str);

        boolean refresh();

        void setContext(Context context);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mRootView = inflater.inflate(R.layout.fragment_usercorrectrate, null);
        this.mTextViewMessage = (TextView) this.mRootView.findViewById(R.id.textViewMessage);
        this.mTextViewMessage.setVisibility(4);
        this.marrReportModules = new ArrayList();
        ReportModuleInterface oneModule = new ReportModule_CorrectWrong();
        TextView textView = (TextView) this.mRootView.findViewById(R.id.textViewCorrectRate);
        LinearLayout layout = (LinearLayout) this.mRootView.findViewById(R.id.layoutCorrectRate);
        View chart = this.mRootView.findViewById(R.id.chartCorrectRate);
        TableLayout table = (TableLayout) this.mRootView.findViewById(R.id.chartCorrectRateTable);
        this.mLayoutContent = this.mRootView.findViewById(R.id.linearLayoutContent);
        oneModule.setContext(getActivity());
        if (oneModule.initModule(textView, layout, chart, table)) {
            this.marrReportModules.add(oneModule);
        } else {
            textView.setVisibility(8);
            layout.setVisibility(8);
        }
        oneModule = new ReportModule_Answers();
        oneModule.setContext(getActivity());
        textView = (TextView) this.mRootView.findViewById(R.id.textViewAnswers);
        layout = (LinearLayout) this.mRootView.findViewById(R.id.layoutAnswers);
        if (oneModule.initModule(textView, layout, this.mRootView.findViewById(R.id.chartAnswers), (TableLayout) this.mRootView.findViewById(R.id.chartAnswersTable))) {
            this.marrReportModules.add(oneModule);
        } else {
            textView.setVisibility(8);
            layout.setVisibility(8);
        }
        return this.mRootView;
    }

    private void reportMessage(String szMessage) {
        this.mTextViewMessage.setVisibility(0);
        this.mTextViewMessage.setText(szMessage);
    }

    public void onDetach() {
        super.onDetach();
    }

    public void refresh() {
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                boolean bResult = false;
                Iterator it = ReportUserAnswerStatisticFragment.this.marrReportModules.iterator();
                while (it.hasNext()) {
                    bResult |= ((ReportModuleInterface) it.next()).refresh();
                }
                if (bResult) {
                    ReportUserAnswerStatisticFragment.this.mTextViewMessage.setVisibility(8);
                    ReportUserAnswerStatisticFragment.this.mLayoutContent.setVisibility(0);
                    return;
                }
                ReportUserAnswerStatisticFragment.this.reportMessage("当前没有客观题分析数据可供显示。");
                ReportUserAnswerStatisticFragment.this.mLayoutContent.setVisibility(8);
            }
        }, 100);
    }
}
