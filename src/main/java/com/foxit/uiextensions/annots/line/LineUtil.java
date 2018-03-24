package com.foxit.uiextensions.annots.line;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.pdf.annots.Markup;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.AppDisplay;
import java.util.ArrayList;

class LineUtil {
    protected static float ARROW_WIDTH_SCALE = 6.0f;
    protected float CTL_EXTENT = 5.0f;
    private float mCtlLineWidth = 2.0f;
    private Paint mCtlPaint = new Paint();
    private float mCtlRadius = 5.0f;
    private float mCtlTouchExt = 20.0f;
    protected LineModule mModule;
    protected PointF tv_left_pt = new PointF();
    protected PointF tv_right_pt = new PointF();

    LineUtil(Context context, LineModule module) {
        this.mModule = module;
        float d2pFactor = (float) AppDisplay.getInstance(context).dp2px(1.0f);
        this.CTL_EXTENT *= d2pFactor;
        this.mCtlLineWidth *= d2pFactor;
        this.mCtlRadius *= d2pFactor;
        this.mCtlTouchExt *= d2pFactor;
        this.mCtlPaint.setStrokeWidth(this.mCtlLineWidth);
    }

    LineToolHandler getToolHandler(String intent) {
        if (intent == null || !intent.equals(LineConstants.INTENT_LINE_ARROW)) {
            return this.mModule.mLineToolHandler;
        }
        return this.mModule.mArrowToolHandler;
    }

    String getToolName(String intent) {
        if (intent == null || !intent.equals(LineConstants.INTENT_LINE_ARROW)) {
            return ToolHandler.TH_TYPE_LINE;
        }
        return ToolHandler.TH_TYPE_ARROW;
    }

    String getToolPropertyKey(String intent) {
        if (intent == null || !intent.equals(LineConstants.INTENT_LINE_ARROW)) {
            return "LINE";
        }
        return "ARROW";
    }

    String getSubject(String intent) {
        if (intent == null || !intent.equals(LineConstants.INTENT_LINE_ARROW)) {
            return "Line";
        }
        return LineConstants.SUBJECT_ARROW;
    }

    ArrayList<String> getEndingStyles(String intent) {
        if (intent == null || !intent.equals(LineConstants.INTENT_LINE_ARROW)) {
            return null;
        }
        ArrayList<String> endingStyles = new ArrayList();
        endingStyles.add(Markup.LINEENDINGSTYLE_NONE);
        endingStyles.add(Markup.LINEENDINGSTYLE_OPENARROW);
        return endingStyles;
    }

    public long getSupportedProperties() {
        return 7;
    }

    protected Path getLinePath(String intent, PointF start, PointF stop, float thickness) {
        if (intent == null) {
            return getLinePath(start, stop);
        }
        if (intent.equals(LineConstants.INTENT_LINE_ARROW)) {
            return getArrowPath(start, stop, thickness);
        }
        return getLinePath(start, stop);
    }

    protected Path getLinePath(PointF start, PointF stop) {
        Path path = new Path();
        path.moveTo(start.x, start.y);
        path.lineTo(stop.x, stop.y);
        return path;
    }

    protected Path getArrowPath(PointF start, PointF stop, float thickness) {
        getArrowControlPt(start, stop, thickness, this.tv_left_pt, this.tv_right_pt);
        Path path = new Path();
        path.moveTo(start.x, start.y);
        path.lineTo(stop.x, stop.y);
        path.moveTo(this.tv_left_pt.x, this.tv_left_pt.y);
        path.lineTo(stop.x, stop.y);
        path.lineTo(this.tv_right_pt.x, this.tv_right_pt.y);
        return path;
    }

    protected RectF getArrowBBox(PointF startPt, PointF stopPt, float thickness) {
        RectF bboxRect = new RectF();
        getArrowControlPt(startPt, stopPt, thickness, this.tv_left_pt, this.tv_right_pt);
        bboxRect.left = Math.min(Math.min(startPt.x, stopPt.x), Math.min(this.tv_left_pt.x, this.tv_right_pt.x));
        bboxRect.top = Math.min(Math.min(startPt.y, stopPt.y), Math.min(this.tv_left_pt.y, this.tv_right_pt.y));
        bboxRect.right = Math.max(Math.max(startPt.x, stopPt.x), Math.max(this.tv_left_pt.x, this.tv_right_pt.x));
        bboxRect.bottom = Math.max(Math.max(startPt.y, stopPt.y), Math.max(this.tv_left_pt.y, this.tv_right_pt.y));
        bboxRect.inset(-thickness, -thickness);
        return bboxRect;
    }

    protected void getArrowControlPt(PointF start, PointF stop, float thickness, PointF left, PointF right) {
        PointF direction = new PointF(stop.x - start.x, stop.y - start.y);
        double dLenth = Math.sqrt((double) ((direction.x * direction.x) + (direction.y * direction.y)));
        if (dLenth < 9.999999747378752E-5d) {
            direction.x = 1.0f;
            direction.y = 0.0f;
        } else {
            direction.x = (float) (((double) direction.x) / dLenth);
            direction.y = (float) (((double) direction.y) / dLenth);
        }
        direction.x *= -thickness;
        direction.y *= -thickness;
        PointF rotatedVector = Rotate(direction, 0.5235987755982988d);
        left.x = stop.x + rotatedVector.x;
        left.y = stop.y + rotatedVector.y;
        rotatedVector = Rotate(direction, -0.5235987755982988d);
        right.x = stop.x + rotatedVector.x;
        right.y = stop.y + rotatedVector.y;
    }

    PointF Rotate(PointF direction, double angle) {
        PointF pointF = new PointF();
        double cosValue = Math.cos(angle);
        double sinValue = Math.sin(angle);
        pointF.x = (float) ((((double) direction.x) * cosValue) - (((double) direction.y) * sinValue));
        pointF.y = (float) ((((double) direction.x) * sinValue) + (((double) direction.y) * cosValue));
        return pointF;
    }

    public PointF calculateEndingPoint(PointF p1, PointF p3) {
        PointF p2 = new PointF();
        float l = (float) Math.sqrt((double) (((p3.x - p1.x) * (p3.x - p1.x)) + ((p3.y - p1.y) * (p3.y - p1.y))));
        p2.x = p3.x - ((this.CTL_EXTENT / l) * (p3.x - p1.x));
        p2.y = p3.y - ((this.CTL_EXTENT / l) * (p3.y - p3.y));
        return p2;
    }

    public float[] calculateControls(PointF p1, PointF p2) {
        PointF p0 = new PointF();
        PointF p3 = new PointF();
        float l = (float) Math.sqrt((double) (((p2.x - p1.x) * (p2.x - p1.x)) + ((p2.y - p1.y) * (p2.y - p1.y))));
        p0.x = p1.x + ((this.CTL_EXTENT / l) * (p1.x - p2.x));
        p0.y = p1.y + ((this.CTL_EXTENT / l) * (p1.y - p2.y));
        p3.x = p2.x + ((this.CTL_EXTENT / l) * (p2.x - p1.x));
        p3.y = p2.y + ((this.CTL_EXTENT / l) * (p2.y - p1.y));
        return new float[]{p0.x, p0.y, p3.x, p3.y};
    }

    public int hitControlTest(PointF p1, PointF p2, PointF point) {
        float[] ctlPts = calculateControls(p1, p2);
        RectF area = new RectF();
        for (int i = 0; i < ctlPts.length / 2; i++) {
            area.set(ctlPts[i * 2], ctlPts[(i * 2) + 1], ctlPts[i * 2], ctlPts[(i * 2) + 1]);
            area.inset(-this.mCtlTouchExt, -this.mCtlTouchExt);
            if (area.contains(point.x, point.y)) {
                return i;
            }
        }
        return -1;
    }

    public void drawControls(Canvas canvas, PointF p1, PointF p2, int color, int opacity) {
        float[] ctlPts = calculateControls(p1, p2);
        for (int i = 0; i < ctlPts.length; i += 2) {
            this.mCtlPaint.setColor(-1);
            this.mCtlPaint.setAlpha(255);
            this.mCtlPaint.setStyle(Style.FILL);
            canvas.drawCircle(ctlPts[i], ctlPts[i + 1], this.mCtlRadius, this.mCtlPaint);
            this.mCtlPaint.setColor(color);
            this.mCtlPaint.setAlpha(opacity);
            this.mCtlPaint.setStyle(Style.STROKE);
            canvas.drawCircle(ctlPts[i], ctlPts[i + 1], this.mCtlRadius, this.mCtlPaint);
        }
    }

    private float getControlExtent() {
        return this.mCtlLineWidth + this.mCtlRadius;
    }

    public void extentBoundsToContainControl(RectF bounds) {
        float[] ctlPts = calculateControls(new PointF(bounds.left, bounds.top), new PointF(bounds.right, bounds.bottom));
        bounds.union(ctlPts[0], ctlPts[1], ctlPts[2], ctlPts[3]);
        bounds.inset(-getControlExtent(), -getControlExtent());
    }

    public void correctPvPoint(PDFViewCtrl pdfViewCtrl, int pageIndex, PointF pt, float thickness) {
        float extent = (getControlExtent() + ((float) AppAnnotUtil.getAnnotBBoxSpace())) + (thickness / 2.0f);
        pt.x = Math.max(extent, pt.x);
        pt.y = Math.max(extent, pt.y);
        pt.x = Math.min(((float) pdfViewCtrl.getPageViewWidth(pageIndex)) - extent, pt.x);
        pt.y = Math.min(((float) pdfViewCtrl.getPageViewHeight(pageIndex)) - extent, pt.y);
    }
}
