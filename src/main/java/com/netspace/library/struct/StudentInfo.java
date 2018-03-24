package com.netspace.library.struct;

import com.google.gson.annotations.Expose;
import java.util.ArrayList;

public class StudentInfo extends UserInfo {
    @Expose
    public ArrayList<UserClassInfo> arrClassInfo = new ArrayList();
    @Expose
    public int nBaseGrade = -1;
}
