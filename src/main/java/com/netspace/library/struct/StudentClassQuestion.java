package com.netspace.library.struct;

import java.util.ArrayList;

public class StudentClassQuestion {
    public ArrayList<StudentAnswer> arrStudentAnswers = new ArrayList();
    public boolean bUserFinish;
    public long nQuestionStartTime = 0;
    public int nType = 0;
    public String szCorrectAnswer = "";
    public String szCurrentAnswer = "";
    public String szFrom;
    public String szGUID = "";
    public String szIMMessage = "";
    public String szOptions = "";
    public String szUserAnswerURL;
}
