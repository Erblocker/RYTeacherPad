package com.netspace.library.virtualnetworkobject;

import android.app.Activity;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import org.achartengine.chart.TimeChart;

public class QuestionItemObject extends ItemObject {
    private final String ENGINENAME;

    public QuestionItemObject() {
        this(null, null, null);
    }

    public QuestionItemObject(String szObjectGUID) {
        this(szObjectGUID, null, null);
    }

    public QuestionItemObject(String szObjectGUID, Activity Activity) {
        this(szObjectGUID, Activity, null);
    }

    public QuestionItemObject(String szObjectGUID, Activity Activity, OnSuccessListener SuccessListener) {
        super(szObjectGUID, Activity, SuccessListener);
        this.ENGINENAME = "QuestionEngine";
        this.mAllowCache = false;
        this.mExpireTimeInMS = TimeChart.DAY;
    }

    public String getRequiredEngineName() {
        return "QuestionEngine";
    }

    public void setQuestionGUID(String szGUID) {
        this.mObjectURI = szGUID;
    }

    public String getQuestionGUID() {
        return this.mObjectURI;
    }
}
