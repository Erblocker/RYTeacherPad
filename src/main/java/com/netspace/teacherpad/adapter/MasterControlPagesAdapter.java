package com.netspace.teacherpad.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.netspace.library.utilities.Utilities;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.TeacherPadApplication;
import com.netspace.teacherpad.controls.StudentNodeView;
import java.util.ArrayList;
import java.util.HashMap;

public class MasterControlPagesAdapter extends PagerAdapter {
    private Context m_Context;
    private ArrayList<StudentInfo> m_arrAllStudentsInfo;
    private ArrayList<Integer> m_arrPages;
    private int m_nColCount = 0;
    private int m_nPosition = 0;
    private int m_nRowCount = 0;

    public class StudentInfo {
        int nCol;
        int nRow;
        Rect rect;
        String szStudentID;
        String szStudentName;
        StudentNodeView viewObject1;
        StudentNodeView viewObject2;
    }

    public StudentInfo NewStudentInfo(int nCol, int nRow, String szStudentID, String szStudentName, Rect rect) {
        StudentInfo StudentInfo = new StudentInfo();
        StudentInfo.nCol = nCol;
        StudentInfo.nRow = nRow;
        StudentInfo.szStudentID = szStudentID;
        StudentInfo.szStudentName = szStudentName;
        StudentInfo.rect = rect;
        return StudentInfo;
    }

    public void Init(Context Context, ArrayList<Integer> arrPages, ArrayList<StudentInfo> arrStudentInfo) {
        for (int i = 0; i < arrStudentInfo.size(); i++) {
            StudentInfo OneInfo = (StudentInfo) arrStudentInfo.get(i);
            if (OneInfo.nCol > this.m_nColCount - 1) {
                this.m_nColCount = OneInfo.nCol;
            }
            if (OneInfo.nRow > this.m_nRowCount - 1) {
                this.m_nRowCount = OneInfo.nRow;
            }
        }
        this.m_arrAllStudentsInfo = arrStudentInfo;
        this.m_arrPages = arrPages;
        this.m_Context = Context;
    }

    public int getCurrentPosition() {
        return this.m_nPosition;
    }

    public int getCount() {
        if (this.m_arrPages != null) {
            return this.m_arrPages.size();
        }
        return 1;
    }

    public void UpdatePadAnswer(HashMap<String, String> mapAnswer) {
        if (this.m_arrAllStudentsInfo != null) {
            TeacherPadApplication MyApp = (TeacherPadApplication) this.m_Context.getApplicationContext();
            for (int i = 0; i < this.m_arrAllStudentsInfo.size(); i++) {
                for (int p = 0; p < 2; p++) {
                    StudentNodeView OneStudent;
                    if (p == 0) {
                        OneStudent = ((StudentInfo) this.m_arrAllStudentsInfo.get(i)).viewObject1;
                    } else {
                        OneStudent = ((StudentInfo) this.m_arrAllStudentsInfo.get(i)).viewObject2;
                    }
                    if (OneStudent != null) {
                        String szAnswer = (String) mapAnswer.get(OneStudent.getStudentID());
                        if (szAnswer == null) {
                            OneStudent.setStudentAnswer("未作答", -1);
                        } else if (szAnswer.length() > 10) {
                            OneStudent.setStudentAnswer("图片作答", 0);
                        } else if (TeacherPadApplication.szCorrectAnswer.isEmpty()) {
                            OneStudent.setStudentAnswer(szAnswer, 0);
                        } else if (TeacherPadApplication.szCorrectAnswer.equalsIgnoreCase(szAnswer)) {
                            OneStudent.setStudentAnswer(szAnswer, 1);
                        } else {
                            OneStudent.setStudentAnswer(szAnswer, -1);
                        }
                    }
                }
            }
        }
    }

    public void CleanPadAnswer() {
        if (this.m_arrAllStudentsInfo != null) {
            for (int i = 0; i < this.m_arrAllStudentsInfo.size(); i++) {
                for (int p = 0; p < 2; p++) {
                    StudentNodeView OneStudent;
                    if (p == 0) {
                        OneStudent = ((StudentInfo) this.m_arrAllStudentsInfo.get(i)).viewObject1;
                    } else {
                        OneStudent = ((StudentInfo) this.m_arrAllStudentsInfo.get(i)).viewObject2;
                    }
                    if (OneStudent != null) {
                        OneStudent.setStudentAnswer("", 0);
                    }
                }
            }
        }
    }

    public void UpdatePadSatus(HashMap<String, String> mapStatus) {
        if (this.m_arrAllStudentsInfo != null) {
            TeacherPadApplication.nOnlineStudentPadCount = 0;
            TeacherPadApplication.arrCurrentOnlineStudentIDs.clear();
            for (int i = 0; i < this.m_arrAllStudentsInfo.size(); i++) {
                for (int p = 0; p < 2; p++) {
                    StudentNodeView OneStudent;
                    if (p == 0) {
                        OneStudent = ((StudentInfo) this.m_arrAllStudentsInfo.get(i)).viewObject1;
                    } else {
                        OneStudent = ((StudentInfo) this.m_arrAllStudentsInfo.get(i)).viewObject2;
                    }
                    if (!(OneStudent == null || OneStudent.getStudentID().isEmpty())) {
                        String szStatus = (String) mapStatus.get(OneStudent.getStudentID());
                        if (szStatus != null) {
                            UpdatePadStatus(szStatus, OneStudent);
                            if (!szStatus.isEmpty()) {
                                TeacherPadApplication.nOnlineStudentPadCount++;
                                TeacherPadApplication.mapStudentIDExists.put(OneStudent.getStudentID(), Boolean.TRUE);
                                TeacherPadApplication.arrCurrentOnlineStudentIDs.add(OneStudent.getStudentID());
                            }
                        } else {
                            OneStudent.setPadStatus(5);
                        }
                    }
                }
            }
        }
    }

    public void UpdatePadStatus(String szStatus, String szStudentID) {
        if (this.m_arrAllStudentsInfo != null) {
            for (int i = 0; i < this.m_arrAllStudentsInfo.size(); i++) {
                for (int p = 0; p < 2; p++) {
                    StudentNodeView OneStudent;
                    if (p == 0) {
                        OneStudent = ((StudentInfo) this.m_arrAllStudentsInfo.get(i)).viewObject1;
                    } else {
                        OneStudent = ((StudentInfo) this.m_arrAllStudentsInfo.get(i)).viewObject2;
                    }
                    if (OneStudent != null && OneStudent.getStudentID().equalsIgnoreCase(szStudentID)) {
                        UpdatePadStatus(szStatus, OneStudent);
                    }
                }
            }
        }
    }

    private void UpdatePadStatus(String szStatus, StudentNodeView OneStudent) {
        szStatus = szStatus.trim();
        if (szStatus.isEmpty()) {
            OneStudent.setPadStatus(5);
            return;
        }
        String[] szData = szStatus.split(" ");
        if (szData.length < 3) {
            OneStudent.setPadStatus(5);
            return;
        }
        String szBatteryLevel = szData[1];
        String szScreenMode = szData[2];
        if (szBatteryLevel.indexOf("+") != -1) {
            szBatteryLevel = szBatteryLevel.substring(0, szBatteryLevel.indexOf("+"));
        }
        if (Utilities.toInt(szBatteryLevel.replaceAll("%", "")) < 20) {
            OneStudent.setPadStatus(4);
        } else if (szScreenMode.equalsIgnoreCase("screenoff")) {
            OneStudent.setPadStatus(0);
        } else if (szScreenMode.equalsIgnoreCase("screenlockon")) {
            OneStudent.setPadStatus(1);
        } else if (!szScreenMode.equalsIgnoreCase("screenon")) {
        } else {
            if (szStatus.indexOf("lock") != -1) {
                OneStudent.setPadStatus(3);
            } else if (szStatus.indexOf("vote") != -1) {
                OneStudent.setPadStatus(2);
            } else if (szStatus.indexOf("normal") != -1) {
                OneStudent.setPadStatus(2);
            }
        }
    }

    private StudentInfo getLocationStudentInfo(int nCol, int nRow) {
        if (this.m_arrAllStudentsInfo == null) {
            return null;
        }
        for (int i = 0; i < this.m_arrAllStudentsInfo.size(); i++) {
            StudentInfo OneInfo = (StudentInfo) this.m_arrAllStudentsInfo.get(i);
            if (OneInfo.nCol == nCol && OneInfo.nRow == nRow) {
                return OneInfo;
            }
        }
        return null;
    }

    public Object instantiateItem(View collection, int position) {
        LayoutInflater inflater = (LayoutInflater) collection.getContext().getSystemService("layout_inflater");
        this.m_nPosition = position;
        View view = inflater.inflate(((Integer) this.m_arrPages.get(this.m_nPosition)).intValue(), null);
        ((ViewPager) collection).addView(view, 0);
        RelativeLayout TableView = (RelativeLayout) view.findViewById(R.id.RelativeLayout1);
        int nCount = 0;
        for (int i = 0; i < this.m_arrAllStudentsInfo.size(); i++) {
            StudentNodeView OneStudent = new StudentNodeView(this.m_Context);
            StudentInfo OneInfo = (StudentInfo) this.m_arrAllStudentsInfo.get(i);
            if (OneInfo != null) {
                if (OneInfo.viewObject1 == null) {
                    OneInfo.viewObject1 = OneStudent;
                } else {
                    OneInfo.viewObject2 = OneStudent;
                }
                OneStudent.setStudentName(OneInfo.szStudentName);
                OneStudent.setStudentID(OneInfo.szStudentID);
                OneStudent.setStudentInfo("状态正常");
            } else {
                OneStudent.setStudentName("");
                OneStudent.setStudentInfo("");
            }
            OneStudent.setPadStatus(0);
            TableView.addView(OneStudent);
            if (OneInfo.rect != null) {
                LayoutParams Params = (LayoutParams) OneStudent.getLayoutParams();
                Params.topMargin = OneInfo.rect.top;
                Params.leftMargin = OneInfo.rect.left;
                OneStudent.setLayoutParams(Params);
            }
            nCount++;
            if (position != 0) {
                OneStudent.setGroup(i);
            }
        }
        return view;
    }

    public void destroyItem(View arg0, int arg1, Object arg2) {
        ((ViewPager) arg0).removeView((View) arg2);
    }

    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == ((View) arg1);
    }
}
