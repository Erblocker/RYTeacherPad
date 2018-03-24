package com.foxit.uiextensions.utils;

import android.content.Context;
import android.graphics.DashPathEffect;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Caret;
import com.foxit.sdk.pdf.annots.FreeText;
import com.foxit.sdk.pdf.annots.Line;
import com.foxit.sdk.pdf.annots.Markup;
import com.foxit.sdk.pdf.annots.Note;
import com.foxit.sdk.pdf.form.FormControl;
import com.foxit.sdk.pdf.form.FormField;
import com.foxit.sdk.pdf.signature.Signature;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.annots.line.LineConstants;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AppAnnotUtil {
    public static float ANNOT_SELECT_TOLERANCE = 10.0f;
    private static final List<Integer> IDS = Collections.unmodifiableList(Arrays.asList(new Integer[]{Integer.valueOf(R.drawable.rv_panel_annot_highlight_type), Integer.valueOf(R.drawable.rv_panel_annot_text_type), Integer.valueOf(R.drawable.rv_panel_annot_strikeout_type), Integer.valueOf(R.drawable.rv_panel_annot_underline_type), Integer.valueOf(R.drawable.rv_panel_annot_squiggly_type), Integer.valueOf(R.drawable.rv_panel_annot_circle_type), Integer.valueOf(R.drawable.rv_panel_annot_square_type), Integer.valueOf(R.drawable.rv_panel_annot_typewriter_type), Integer.valueOf(R.drawable.rv_panel_annot_stamp_type), Integer.valueOf(R.drawable.rv_panel_annot_caret_type), Integer.valueOf(R.drawable.rv_panel_annot_replace_type), Integer.valueOf(R.drawable.rv_panel_annot_ink_type), Integer.valueOf(R.drawable.rv_panel_annot_line_type), Integer.valueOf(R.drawable.rv_panel_annot_arrow_type), Integer.valueOf(R.drawable.rv_panel_annot_accthment_type)}));
    private static final List<String> TYPES = Collections.unmodifiableList(Arrays.asList(new String[]{"Highlight", "Text", "StrikeOut", "Underline", "Squiggly", Markup.LINEENDINGSTYLE_CIRCLE, Markup.LINEENDINGSTYLE_SQUARE, "FreeTextTypewriter", "Stamp", "Caret", "Replace", "Ink", "Line", LineConstants.INTENT_LINE_ARROW, "FileAttachment"}));
    private static AppAnnotUtil mAppAnnotUtil = null;
    private static Context mContext;
    private static PathEffect mPathEffect;
    private Toast mAnnotToast;
    private AppDisplay mDisplay;

    public static AppAnnotUtil getInstance(Context context) {
        if (mAppAnnotUtil == null) {
            mAppAnnotUtil = new AppAnnotUtil(context);
            mContext = context;
        }
        return mAppAnnotUtil;
    }

    public AppAnnotUtil(Context context) {
        this.mDisplay = AppDisplay.getInstance(context);
    }

    public static PathEffect getAnnotBBoxPathEffect() {
        return new DashPathEffect(new float[]{6.0f, 2.0f}, 0.0f);
    }

    public static boolean isValid(PDFPage page) {
        if (page == null) {
            return false;
        }
        Class[] argsClass = new Class[]{page.getClass()};
        try {
            Method method = argsClass[0].getDeclaredMethod("getCPtr", argsClass);
            method.setAccessible(true);
            Object obj = method.invoke(null, new Object[]{page});
            method.setAccessible(false);
            if (obj == null || ((Long) obj).longValue() == 0) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValid(Annot annot) {
        if (annot == null) {
            return false;
        }
        Class[] argsClass = new Class[]{annot.getClass()};
        try {
            Method method = argsClass[0].getDeclaredMethod("getCPtr", argsClass);
            method.setAccessible(true);
            Object obj = method.invoke(null, new Object[]{annot});
            method.setAccessible(false);
            if (obj == null || ((Long) obj).longValue() == 0) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static int getAnnotBBoxSpace() {
        return 5;
    }

    public float getAnnotBBoxStrokeWidth() {
        return (float) this.mDisplay.dp2px(1.0f);
    }

    public static PathEffect getBBoxPathEffect2() {
        if (mPathEffect == null) {
            mPathEffect = new DashPathEffect(new float[]{6.0f, 6.0f}, 0.0f);
        }
        return mPathEffect;
    }

    public static void toastAnnotCopy(Context context) {
        UIToast.getInstance(context).show(R.string.fm_annot_copy);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isSupportReply(Annot annot) {
        if (annot == null) {
            return false;
        }
        try {
            switch (annot.getType()) {
                case 1:
                    if (((Note) annot).isStateAnnot()) {
                        return false;
                    }
                    break;
                case 3:
                    if (!"FreeTextTypewriter".equals(((Markup) annot).getIntent())) {
                        return false;
                    }
                    break;
                case 4:
                case 13:
                case 14:
                case 15:
                    break;
                case 5:
                case 6:
                case 9:
                case 10:
                case 11:
                case 12:
                    break;
                default:
                    return false;
            }
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getTypeString(Annot annot) {
        try {
            switch (annot.getType()) {
                case 1:
                    return "Text";
                case 2:
                    return "Link";
                case 3:
                    String intent = ((FreeText) annot).getIntent();
                    if (intent == null) {
                        intent = "TextBox";
                    }
                    return intent;
                case 4:
                    if (LineConstants.INTENT_LINE_ARROW.equals(((Line) annot).getIntent())) {
                        return LineConstants.INTENT_LINE_ARROW;
                    }
                    return "Line";
                case 5:
                    return Markup.LINEENDINGSTYLE_SQUARE;
                case 6:
                    return Markup.LINEENDINGSTYLE_CIRCLE;
                case 7:
                    return "Polygon";
                case 8:
                    return "PolyLine";
                case 9:
                    return "Highlight";
                case 10:
                    return "Underline";
                case 11:
                    return "Squiggly";
                case 12:
                    return "StrikeOut";
                case 13:
                    return "Stamp";
                case 14:
                    return isReplaceCaret(annot) ? "Replace" : "Caret";
                case 15:
                    return "Ink";
                case 16:
                    return "PSInk";
                case 17:
                    return "FileAttachment";
                case 18:
                    return "Sound";
                case 19:
                    return "Movie";
                case 20:
                    return "Widget";
                case 21:
                    return "Screen";
                case 22:
                    return "PrinterMark";
                case 23:
                    return "TrapNet";
                case 24:
                    return "Watermark";
                case 25:
                    return "3D";
                default:
                    return "Unknown";
            }
        } catch (PDFException e) {
            e.printStackTrace();
            return "Unknown";
        }
        e.printStackTrace();
        return "Unknown";
    }

    public static boolean contentsModifiable(String type) {
        return "Text".equals(type) || "Line".equals(type) || LineConstants.INTENT_LINE_ARROW.equals(type) || Markup.LINEENDINGSTYLE_SQUARE.equals(type) || Markup.LINEENDINGSTYLE_CIRCLE.equals(type) || "Highlight".equals(type) || "Underline".equals(type) || "Squiggly".equals(type) || "StrikeOut".equals(type) || "Stamp".equals(type) || "Caret".equals(type) || "Replace".equals(type) || "Ink".equals(type);
    }

    public static Annot getAnnot(PDFPage page, String UID) {
        Annot annot = null;
        if (page == null) {
            return null;
        }
        try {
            long nCount = (long) page.getAnnotCount();
            int i = 0;
            while (((long) i) < nCount) {
                try {
                    if (page.getAnnot(i).getUniqueID() != null && page.getAnnot(i).getUniqueID().compareTo(UID) == 0) {
                        annot = page.getAnnot(i);
                        break;
                    }
                } catch (PDFException e) {
                }
                i++;
            }
        } catch (PDFException e2) {
            e2.printStackTrace();
        }
        return annot;
    }

    public static FormControl getControlAtPos(PDFPage page, PointF point, float tolerance) throws PDFException {
        Annot annot = page.getAnnotAtPos(point, tolerance);
        if (annot == null || annot.getType() != 20) {
            return null;
        }
        return (FormControl) annot;
    }

    public static Signature getSignatureAtPos(PDFPage page, PointF point, float tolerance) throws PDFException {
        Annot annot = page.getAnnotAtPos(point, tolerance);
        if (annot != null && annot.getType() == 20) {
            FormControl control = (FormControl) annot;
            FormField field = control.getField();
            if (field != null && field.getType() == 7) {
                return (Signature) control;
            }
        }
        return null;
    }

    public static boolean isSameAnnot(Annot annot, Annot comparedAnnot) {
        long objNumA = 0;
        if (annot != null) {
            try {
                objNumA = annot.getDict().getObjNum();
            } catch (PDFException e) {
                e.printStackTrace();
                return false;
            }
        }
        long objNumB = 0;
        if (comparedAnnot != null) {
            objNumB = comparedAnnot.getDict().getObjNum();
        }
        if (objNumA == objNumB) {
            return true;
        }
        return false;
    }

    public static boolean isSupportGroupElement(Annot annot) {
        if (!isSupportGroup(annot)) {
            return false;
        }
        try {
            return !isSameAnnot(annot, ((Markup) annot).getGroupHeader());
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isSupportGroup(Annot annot) {
        if (annot == null) {
            return false;
        }
        try {
            if (!annot.isMarkup() || !((Markup) annot).isGrouped()) {
                return false;
            }
            Markup head = ((Markup) annot).getGroupHeader();
            switch (head.getType()) {
                case 14:
                    return isReplaceCaret(head);
                default:
                    return false;
            }
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isReplaceCaret(Annot annot) {
        if (annot != null) {
            try {
                if (annot.getType() == 14 && ((Markup) annot).isGrouped()) {
                    Caret caret = (Caret) annot;
                    Markup head = caret.getGroupHeader();
                    if (head.getType() != 14 || head.getGroupElementCount() != 2 || !isSameAnnot(head, caret)) {
                        return false;
                    }
                    for (int i = 0; i < 2; i++) {
                        if (caret.getGroupElement(i).getType() == 12) {
                            return true;
                        }
                    }
                    return false;
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static Annot getReplyToAnnot(Annot annot) {
        Annot annot2 = null;
        if (annot != null) {
            try {
                if (annot.getType() == 1) {
                    annot2 = ((Note) annot).getReplyTo();
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
        return annot2;
    }

    public static PointF getPageViewPoint(PDFViewCtrl pdfViewCtrl, int pageIndex, MotionEvent motionEvent) {
        PointF devPt = new PointF(motionEvent.getX(), motionEvent.getY());
        PointF point = new PointF();
        pdfViewCtrl.convertDisplayViewPtToPageViewPt(devPt, point, pageIndex);
        return point;
    }

    public static PointF getPdfPoint(PDFViewCtrl pdfViewCtrl, int pageIndex, MotionEvent motionEvent) {
        PointF devPt = new PointF(motionEvent.getX(), motionEvent.getY());
        PointF pageViewPt = new PointF();
        pdfViewCtrl.convertDisplayViewPtToPageViewPt(devPt, pageViewPt, pageIndex);
        PointF point = new PointF();
        pdfViewCtrl.convertPageViewPtToPdfPt(pageViewPt, point, pageIndex);
        return point;
    }

    public static int getIconId(String type) {
        int index = TYPES.indexOf(type);
        if (index != -1) {
            return ((Integer) IDS.get(index)).intValue();
        }
        return R.drawable.rv_panel_annot_not_edit_type;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isSupportEditAnnot(Annot annot) {
        if (annot == null) {
            return false;
        }
        try {
            switch (annot.getType()) {
                case 1:
                    if (((Note) annot).isStateAnnot()) {
                        return false;
                    }
                    break;
                case 3:
                    if (!"FreeTextTypewriter".equals(((Markup) annot).getIntent())) {
                        return false;
                    }
                    break;
                case 4:
                case 13:
                case 14:
                case 15:
                case 17:
                    break;
                case 5:
                case 6:
                case 9:
                case 10:
                case 11:
                case 12:
                    break;
                default:
                    return false;
            }
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void showAnnotContinueCreateToast(boolean isContinuousCreate) {
        if (this.mAnnotToast == null) {
            initAnnotToast();
        }
        if (this.mAnnotToast != null) {
            String str;
            int yOffset;
            if (isContinuousCreate) {
                str = AppResource.getString(mContext, R.string.annot_continue_create);
            } else {
                str = AppResource.getString(mContext, R.string.annot_single_create);
            }
            TextView tv = (TextView) this.mAnnotToast.getView().findViewById(R.id.annot_continue_create_toast_tv);
            if (this.mDisplay.isPad()) {
                yOffset = AppResource.getDimensionPixelSize(mContext, R.dimen.ux_toolbar_height_pad) + (this.mDisplay.dp2px(16.0f) * 3);
            } else {
                yOffset = AppResource.getDimensionPixelSize(mContext, R.dimen.ux_toolbar_height_phone) + (this.mDisplay.dp2px(16.0f) * 3);
            }
            this.mAnnotToast.setGravity(80, 0, yOffset);
            tv.setText(str);
            this.mAnnotToast.show();
        }
    }

    private void initAnnotToast() {
        try {
            int yOffset;
            this.mAnnotToast = new Toast(mContext);
            View toastlayout = ((LayoutInflater) mContext.getSystemService("layout_inflater")).inflate(R.layout.annot_continue_create_tips, null);
            TextView tv = (TextView) toastlayout.findViewById(R.id.annot_continue_create_toast_tv);
            this.mAnnotToast.setView(toastlayout);
            this.mAnnotToast.setDuration(0);
            if (this.mDisplay.isPad()) {
                yOffset = AppResource.getDimensionPixelSize(mContext, R.dimen.ux_toolbar_height_pad) + (this.mDisplay.dp2px(16.0f) * 3);
            } else {
                yOffset = AppResource.getDimensionPixelSize(mContext, R.dimen.ux_toolbar_height_phone) + (this.mDisplay.dp2px(16.0f) * 3);
            }
            this.mAnnotToast.setGravity(80, 0, yOffset);
        } catch (Exception e) {
            this.mAnnotToast = null;
        }
    }
}
