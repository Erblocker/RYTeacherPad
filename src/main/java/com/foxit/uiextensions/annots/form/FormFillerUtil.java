package com.foxit.uiextensions.annots.form;

import android.graphics.PointF;
import android.graphics.RectF;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.form.Form;
import com.foxit.sdk.pdf.form.FormControl;
import com.foxit.sdk.pdf.form.FormField;
import com.foxit.uiextensions.utils.AppAnnotUtil;

public class FormFillerUtil {
    protected static int getAnnotFieldType(Form form, Annot annot) {
        int type = 0;
        try {
            RectF rect = annot.getRect();
            FormControl control = AppAnnotUtil.getControlAtPos(annot.getPage(), new PointF(rect.left + Math.abs(rect.width() / 2.0f), rect.bottom + Math.abs(rect.height() / 2.0f)), 0.0f);
            if (control != null) {
                type = control.getField().getType();
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
        return type;
    }

    protected static boolean isReadOnly(Annot annot) {
        try {
            boolean bRet;
            if ((64 & annot.getFlags()) != 0) {
                bRet = true;
            } else {
                bRet = false;
            }
            FormField field = ((FormControl) annot).getField();
            int fieldType = field.getType();
            int fieldFlag = field.getFlags();
            switch (fieldType) {
                case 0:
                case 1:
                    return false;
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                    if ((fieldFlag & 1) != 0) {
                        bRet = true;
                    } else {
                        bRet = false;
                    }
                    return bRet;
                case 7:
                    return true;
                default:
                    return bRet;
            }
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    protected static boolean isVisible(Annot annot) {
        long flags = 0;
        try {
            flags = annot.getFlags();
        } catch (PDFException e) {
            e.printStackTrace();
        }
        return (1 & flags) == 0 && (2 & flags) == 0 && (32 & flags) == 0;
    }

    public static boolean isEmojiCharacter(int codePoint) {
        return codePoint == 0 || codePoint == 9 || codePoint == 169 || codePoint == 174 || codePoint == 12349 || codePoint == 12336 || codePoint == 11093 || codePoint == 11036 || codePoint == 11035 || codePoint == 11088 || ((codePoint >= 127183 && codePoint <= 128696) || codePoint == 13 || codePoint == 56845 || ((codePoint >= 8448 && codePoint <= 10239) || ((codePoint >= 11013 && codePoint <= 11015) || ((codePoint >= 10548 && codePoint <= 10549) || ((codePoint >= 8252 && codePoint <= 8265) || ((codePoint >= 12951 && codePoint <= 12953) || ((codePoint >= 128512 && codePoint <= 128591) || (codePoint >= 56320 && codePoint <= 59000))))))));
    }
}
