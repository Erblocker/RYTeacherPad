package com.netspace.library.struct;

import com.google.gson.annotations.Expose;
import java.util.ArrayList;

public class UserClassInfo {
    @Expose
    public UserGrade Grade;
    @Expose
    public ArrayList<UserInfo> arrStudents = new ArrayList();
    @Expose
    public ArrayList<TeacherInfo> arrTeachers = new ArrayList();
    @Expose
    public int nSubject = -1;
    @Expose
    public int nVirtual = 0;
    @Expose
    public String szClassGUID = "";
    @Expose
    public String szClassName = "";
}
