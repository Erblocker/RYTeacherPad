package com.foxit.uiextensions.textselect;

import android.graphics.PointF;
import android.graphics.RectF;
import com.foxit.sdk.common.DateTime;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.annots.note.NoteAnnotContent;

/* compiled from: TextSelectToolHandler */
class TextAnnotContent implements NoteAnnotContent {
    private PointF p = new PointF();
    private int pageIndex;

    public TextAnnotContent(PointF p, int pageIndex) {
        this.p.set(p.x, p.y);
        this.pageIndex = pageIndex;
    }

    public int getPageIndex() {
        return this.pageIndex;
    }

    public int getType() {
        return 1;
    }

    public String getNM() {
        return null;
    }

    public RectF getBBox() {
        return new RectF(this.p.x, this.p.y, this.p.x, this.p.y);
    }

    public int getColor() {
        return 0;
    }

    public int getOpacity() {
        return 0;
    }

    public float getLineWidth() {
        return 0.0f;
    }

    public String getSubject() {
        return null;
    }

    public DateTime getModifiedDate() {
        return null;
    }

    public String getContents() {
        return null;
    }

    public String getIntent() {
        return null;
    }

    public String getIcon() {
        return "";
    }

    public String getFromType() {
        return Module.MODULE_NAME_SELECTION;
    }

    public String getParentNM() {
        return null;
    }
}
