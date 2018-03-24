package com.netspace.library.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import com.netspace.library.adapter.IMMessageListAdapter;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.struct.ScheduleItemData;
import com.netspace.library.ui.StatusBarDisplayer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public interface IWmExamDBOpenHelper {
    public static final int GETITEMRESULT_TYPE_ANSWERRESULT = 3;
    public static final int GETITEMRESULT_TYPE_FAVORITE = 2;
    public static final int GETITEMRESULT_TYPE_VOTE = 1;

    boolean CheckIfHasSelfLearnLessons(int i);

    String CheckItemExsit(String str, String str2);

    boolean CheckItemRead(String str, String str2);

    boolean DeleteDataChange(String str);

    ArrayList<String> GetAllScheduleResourceGUIDs();

    boolean GetAllSelfLearnSubjects(ArrayList<String> arrayList, ArrayList<Integer> arrayList2);

    int GetAllUnFinishedSelfLearnCount(int i, HashMap<String, Integer> hashMap);

    String GetDataXML(String str, String str2);

    boolean GetDesktopObjectPos(View view, int i, int i2);

    String GetExsitRecordGUID(String str);

    int GetItemResult(String str, String str2, int i);

    int GetItemViewResult(String str, String str2);

    String GetLatestTableSyncTime(String str);

    Cursor GetLessonDetail(String str, String str2);

    int GetQuestionAnswerResult(String str, String str2);

    float GetQuestionAnswerScore(String str, String str2);

    boolean GetScheduleInRange(String str, String str2, ArrayList<ScheduleItemData> arrayList);

    int GetScheduleState(String str);

    ArrayList GetScheduleWeekPeriod();

    int GetSelfLearnFinishCount(String str);

    boolean GetSelfLearnLessonsGUID(int i, ArrayList<String> arrayList, ArrayList<String> arrayList2);

    String GetSelfLearnProgressText(String str);

    ArrayList<String> GetUserAnswer(String str, String str2);

    boolean RegisterDataChange(String str, String str2);

    boolean SaveDesktopObjectPos(View view, int i, int i2);

    Cursor SearchSQL(String str);

    void SetDataChangeListener(DataChangeInterface dataChangeInterface);

    boolean SetDataXML(String str, StatusBarDisplayer statusBarDisplayer);

    boolean SetItemRead(String str, String str2, String str3, String str4, String str5, int i, int i2);

    void SetItemResult(String str, String str2, int i, int i2);

    boolean addIMMessage(String str, String str2, String str3, String str4, String str5);

    boolean addResourceToQuestionBook(ResourceItemData resourceItemData, String str, int i, String str2, String str3, String str4);

    boolean addResourceToQuestionBook(String str, String str2, int i, String str3, String str4, String str5);

    boolean deleteQuestionBookItemByObjectGUID(String str);

    boolean deleteQuestionBookItemFlags(String str);

    SQLiteDatabase getDataBase();

    boolean getIMMessages(String str, int i, int i2, IMMessageListAdapter iMMessageListAdapter);

    boolean getIMRelatedMessage(String str, String str2, IMMessageListAdapter iMMessageListAdapter);

    String getScheduleUserClassGUID(String str);

    String getScheduleUserClassName(String str);

    boolean isDayHasSchedule(Date date);

    void onCreate(SQLiteDatabase sQLiteDatabase);

    void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2);

    boolean searchIMMessage(String str, String str2, IMMessageListAdapter iMMessageListAdapter);
}
