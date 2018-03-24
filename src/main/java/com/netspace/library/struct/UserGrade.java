package com.netspace.library.struct;

import com.google.gson.annotations.Expose;

public class UserGrade {
    @Expose
    public boolean bVirtualClass = false;
    @Expose
    public int nGradeID = -1;
    @Expose
    public String szGUID = "";
    @Expose
    public String szLocation = "";
    @Expose
    public String szName = "";

    public UserGrade(int nGrade) {
        this.nGradeID = nGrade;
    }
}
