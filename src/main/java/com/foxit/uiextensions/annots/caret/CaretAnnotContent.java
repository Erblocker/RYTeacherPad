package com.foxit.uiextensions.annots.caret;

import com.foxit.sdk.common.DateTime;
import com.foxit.uiextensions.annots.AnnotContent;
import com.foxit.uiextensions.annots.textmarkup.TextSelector;
import com.foxit.uiextensions.utils.AppDmUtil;

public abstract class CaretAnnotContent implements AnnotContent {
    private boolean mIsInsert = false;

    public abstract DateTime getCreatedDate();

    public abstract int getRotate();

    public abstract TextSelector getTextSelector();

    public CaretAnnotContent(boolean isTnsert) {
        this.mIsInsert = isTnsert;
    }

    public int getPageIndex() {
        return 0;
    }

    public int getType() {
        return 0;
    }

    public float getLineWidth() {
        return 0.0f;
    }

    public String getAuthor() {
        return AppDmUtil.getAnnotAuthor();
    }

    public String getNM() {
        return AppDmUtil.randomUUID(null);
    }

    public String getIntent() {
        return this.mIsInsert ? "Insert Text" : "Replace";
    }

    public String getSubject() {
        return this.mIsInsert ? "Insert Text" : "Replace";
    }
}
