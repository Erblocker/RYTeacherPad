package com.netspace.library.struct;

import java.util.HashMap;

public class StudentClassAnswer {
    public boolean bAnswered = false;
    public HashMap<String, Integer> mapVoteCount = new HashMap();
    public int nAnswerResult = 0;
    public int nScore = 0;
    public int nSubmitIndex = 0;
    public int nType = 0;
    public int nVoteToThisQuestion = 0;
    public String szAnswer = "";
    public String szAnswerTime = "";
    public String szClientID = "";
    public String szCorrectAnswer = "";
    public String szGUID = "";
    public String szPicturePackageID = "";
    public String szScheduleGUID = "";
    public String szTime = "";
}
