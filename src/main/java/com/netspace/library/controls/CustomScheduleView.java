package com.netspace.library.controls;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.text.Html;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.netspace.library.utilities.SimpleImageGetter;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CustomScheduleView extends LinearLayout implements OnLongClickListener {
    private int LESSON_CELL_HEIGHT;
    private int LESSON_CELL_WIDTH;
    private int LESSON_INDEX_WIDTH;
    private int LESSON_WEEKNAME_HEIGHT;
    private OnScheduleChangedListener mScheduleChangedListener;
    private Context m_Context;
    private RelativeLayout m_RootLayout;
    private ArrayList<StaticLessonInfo> m_arrStaticLessons;
    private boolean m_bScheduleChanged;
    private int m_nClassesPerDay;
    private int m_nHighlightWeekNumber;
    private int m_nStartIndex;

    public interface OnScheduleChangedListener {
        void OnScheduleChanged();
    }

    public static class StaticLessonInfo {
        int nLessonIndex;
        int nWeekNumber;
        String szLessonName;
        TextView textView;
    }

    public CustomScheduleView(Context context) {
        this(context, null, 0);
    }

    public CustomScheduleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomScheduleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr);
    }

    public CustomScheduleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.m_nClassesPerDay = 12;
        this.LESSON_CELL_WIDTH = 100;
        this.LESSON_CELL_HEIGHT = 70;
        this.LESSON_INDEX_WIDTH = 30;
        this.LESSON_WEEKNAME_HEIGHT = 30;
        this.m_nHighlightWeekNumber = 3;
        this.m_nStartIndex = 100000;
        this.m_bScheduleChanged = false;
        this.m_arrStaticLessons = new ArrayList();
        this.m_Context = context;
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        int nWeekNumber = c.get(7) - 1;
        if (nWeekNumber == 0) {
            nWeekNumber = 7;
        }
        this.m_nHighlightWeekNumber = nWeekNumber;
        this.LESSON_CELL_WIDTH = Utilities.dpToPixel(this.LESSON_CELL_WIDTH, this.m_Context);
        this.LESSON_CELL_HEIGHT = Utilities.dpToPixel(this.LESSON_CELL_HEIGHT, this.m_Context);
        this.LESSON_INDEX_WIDTH = Utilities.dpToPixel(this.LESSON_INDEX_WIDTH, this.m_Context);
        this.LESSON_WEEKNAME_HEIGHT = Utilities.dpToPixel(this.LESSON_WEEKNAME_HEIGHT, this.m_Context);
        this.m_RootLayout = new RelativeLayout(context);
        addView(this.m_RootLayout);
        String[] arrWeekNumber = new String[]{"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
        TextView Corner = new TextView(context);
        Corner.setBackgroundResource(R.drawable.background_scheduleweekcell);
        this.m_RootLayout.addView(Corner);
        LayoutParams Param = (LayoutParams) Corner.getLayoutParams();
        Param.leftMargin = 0;
        Param.topMargin = 0;
        Param.width = this.LESSON_INDEX_WIDTH;
        Param.height = this.LESSON_WEEKNAME_HEIGHT;
        for (int i = 0; i <= 7; i++) {
            for (int j = 0; j <= this.m_nClassesPerDay; j++) {
                boolean bWeekNumberCell = false;
                boolean bIndexNumberCell = false;
                TextView OneLesson = new TextView(context);
                OneLesson.setId((this.m_nStartIndex + (i * 100)) + j);
                OneLesson.setTextSize(15.0f);
                OneLesson.setGravity(17);
                OneLesson.setLines(3);
                OneLesson.setMaxLines(3);
                OneLesson.setEllipsize(TruncateAt.END);
                OneLesson.setSingleLine(false);
                if (i == 0) {
                    if (j > 0) {
                        bIndexNumberCell = true;
                        OneLesson.setText(String.valueOf(j) + " ");
                        OneLesson.setGravity(5);
                        OneLesson.setBackgroundResource(R.drawable.background_scheduleindex);
                    }
                } else if (j == 0) {
                    bWeekNumberCell = true;
                    OneLesson.setText(arrWeekNumber[i - 1]);
                    if (this.m_nHighlightWeekNumber != i) {
                        OneLesson.setBackgroundResource(R.drawable.background_scheduleweekcell);
                    } else {
                        OneLesson.setBackgroundResource(R.drawable.background_scheduleweekcell_highlight);
                    }
                } else {
                    OneLesson.setText("");
                    if (this.m_nHighlightWeekNumber != i) {
                        OneLesson.setBackgroundResource(R.drawable.background_schedulecell2);
                    } else {
                        OneLesson.setBackgroundResource(R.drawable.background_schedulecell2_highlight);
                    }
                }
                this.m_RootLayout.addView(OneLesson);
                Param = (LayoutParams) OneLesson.getLayoutParams();
                Param.leftMargin = this.LESSON_CELL_WIDTH * i;
                Param.topMargin = this.LESSON_CELL_HEIGHT * j;
                Param.width = this.LESSON_CELL_WIDTH;
                Param.height = this.LESSON_CELL_HEIGHT;
                if (i > 0) {
                    Param.leftMargin -= this.LESSON_CELL_WIDTH - this.LESSON_INDEX_WIDTH;
                }
                if (bIndexNumberCell) {
                    Param.width = this.LESSON_INDEX_WIDTH;
                    Param.topMargin -= this.LESSON_CELL_HEIGHT - this.LESSON_WEEKNAME_HEIGHT;
                } else if (bWeekNumberCell) {
                    Param.height = this.LESSON_WEEKNAME_HEIGHT;
                } else {
                    Param.topMargin -= this.LESSON_CELL_HEIGHT - this.LESSON_WEEKNAME_HEIGHT;
                }
                OneLesson.setLayoutParams(Param);
                OneLesson.setOnLongClickListener(this);
            }
        }
    }

    public int getHighlightWeekNumber() {
        return this.m_nHighlightWeekNumber;
    }

    public boolean isScheduleChanged() {
        return this.m_bScheduleChanged;
    }

    public void setScheduleChangedListener(OnScheduleChangedListener OnChangeListener) {
        this.mScheduleChangedListener = OnChangeListener;
    }

    public void putStaticLessonData(String szLessonData) {
        String[] arrLessonNames = szLessonData.split(";");
        int nIndex = 0;
        for (int i = 1; i <= 7; i++) {
            for (int y = 1; y <= 12; y++) {
                String szLessonName = "";
                StaticLessonInfo StaticLessonInfo = new StaticLessonInfo();
                if (arrLessonNames.length > nIndex) {
                    szLessonName = arrLessonNames[nIndex];
                }
                StaticLessonInfo.nWeekNumber = i;
                StaticLessonInfo.nLessonIndex = y;
                StaticLessonInfo.szLessonName = szLessonName;
                StaticLessonInfo.textView = (TextView) findViewById((this.m_nStartIndex + (i * 100)) + y);
                this.m_arrStaticLessons.add(StaticLessonInfo);
                if (!szLessonName.isEmpty()) {
                    AddStaticLesson(i, y, szLessonName);
                }
                nIndex++;
            }
        }
    }

    public String getStaticLessonData() {
        String szResult = "";
        for (int i = 0; i < this.m_arrStaticLessons.size(); i++) {
            szResult = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(szResult)).append(((StaticLessonInfo) this.m_arrStaticLessons.get(i)).szLessonName).toString())).append(";").toString();
        }
        return szResult;
    }

    public void AddImage(int nToWeekNumber, int nToLessonIndex) {
        TextView TargetView = (TextView) findViewById((this.m_nStartIndex + (nToWeekNumber * 100)) + nToLessonIndex);
        if (TargetView != null) {
            LayoutParams layoutParams = (LayoutParams) TargetView.getLayoutParams();
        }
    }

    private void AddStaticLesson(int nWeekNumber, int nLessonIndex, String szName) {
        TextView TargetView = (TextView) findViewById((this.m_nStartIndex + (nWeekNumber * 100)) + nLessonIndex);
        if (TargetView != null) {
            TargetView.setText(szName);
        }
    }

    private String GetStaticLesson(int nWeekNumber, int nLessonIndex) {
        TextView TargetView = (TextView) findViewById((this.m_nStartIndex + (nWeekNumber * 100)) + nLessonIndex);
        if (TargetView == null || TargetView.getTag() != null) {
            return "";
        }
        return TargetView.getText().toString();
    }

    public TextView AddLesson(int nWeekNumber, int nLessonIndex, String szName, Object Key, OnClickListener OnClickCallBack) {
        TextView TargetView = (TextView) findViewById((this.m_nStartIndex + (nWeekNumber * 100)) + nLessonIndex);
        if (TargetView != null) {
            TargetView.setText(Html.fromHtml(szName, new SimpleImageGetter(this.m_Context), null));
            TargetView.setTypeface(TargetView.getTypeface(), 0);
            TargetView.setTag(Key);
            TargetView.setOnClickListener(OnClickCallBack);
            TargetView.setOnLongClickListener(null);
        }
        return TargetView;
    }

    public void SetHighlightWeek(int nWeekNumber) {
        int j;
        TextView TargetView;
        if (this.m_nHighlightWeekNumber != -1) {
            for (j = 0; j <= this.m_nClassesPerDay; j++) {
                TargetView = (TextView) findViewById((this.m_nStartIndex + (this.m_nHighlightWeekNumber * 100)) + j);
                if (j == 0) {
                    TargetView.setBackgroundResource(R.drawable.background_scheduleweekcell);
                } else {
                    TargetView.setBackgroundResource(R.drawable.background_schedulecell2);
                }
            }
        }
        this.m_nHighlightWeekNumber = nWeekNumber;
        for (j = 0; j <= this.m_nClassesPerDay; j++) {
            TargetView = (TextView) findViewById((this.m_nStartIndex + (this.m_nHighlightWeekNumber * 100)) + j);
            if (j == 0) {
                TargetView.setBackgroundResource(R.drawable.background_scheduleweekcell_highlight);
            } else {
                TargetView.setBackgroundResource(R.drawable.background_schedulecell2_highlight);
            }
        }
    }

    public boolean onLongClick(View v) {
        ArrayList<String> arrLessonNames = new ArrayList();
        ArrayList<Integer> arrLessonID = new ArrayList();
        arrLessonNames.add("<无>");
        arrLessonID.add(Integer.valueOf(-1));
        Utilities.getAllSubjectInfo(arrLessonNames, arrLessonID);
        final String[] arrNames = (String[]) arrLessonNames.toArray(new String[arrLessonNames.size()]);
        final Integer[] arrIDs = (Integer[]) arrLessonID.toArray(new Integer[arrLessonID.size()]);
        final TextView TargetView = (TextView) v;
        new Builder(new ContextThemeWrapper(this.m_Context, 16974130)).setItems(arrNames, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                int i;
                StaticLessonInfo StaticLessonInfo;
                if (which != 0) {
                    TargetView.setText(arrNames[which]);
                    TargetView.setTag(arrIDs[which]);
                    for (i = 0; i < CustomScheduleView.this.m_arrStaticLessons.size(); i++) {
                        StaticLessonInfo = (StaticLessonInfo) CustomScheduleView.this.m_arrStaticLessons.get(i);
                        if (StaticLessonInfo.textView.equals(TargetView)) {
                            StaticLessonInfo.szLessonName = arrNames[which];
                        }
                    }
                    CustomScheduleView.this.m_bScheduleChanged = true;
                } else {
                    TargetView.setText("");
                    TargetView.setTag(null);
                    for (i = 0; i < CustomScheduleView.this.m_arrStaticLessons.size(); i++) {
                        StaticLessonInfo = (StaticLessonInfo) CustomScheduleView.this.m_arrStaticLessons.get(i);
                        if (StaticLessonInfo.textView.equals(TargetView)) {
                            StaticLessonInfo.szLessonName = "";
                        }
                    }
                    CustomScheduleView.this.m_bScheduleChanged = true;
                }
                if (CustomScheduleView.this.mScheduleChangedListener != null) {
                    CustomScheduleView.this.mScheduleChangedListener.OnScheduleChanged();
                }
            }
        }).setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
            }
        }).setTitle("选择课程").create().show();
        return false;
    }
}
