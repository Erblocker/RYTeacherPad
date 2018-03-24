package com.foxit.uiextensions.annots.link;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.media.TransportMediator;
import android.view.MotionEvent;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.PDFViewCtrl.IPageEventListener;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.action.Action;
import com.foxit.sdk.pdf.action.Destination;
import com.foxit.sdk.pdf.action.GotoAction;
import com.foxit.sdk.pdf.action.URIAction;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Link;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AnnotContent;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.Event.Callback;
import com.foxit.uiextensions.utils.OnPageEventListener;
import java.util.ArrayList;

class LinkAnnotHandler implements AnnotHandler {
    protected boolean isDocClosed = false;
    protected Context mContext;
    protected LinkInfo mLinkInfo;
    private OnPageEventListener mPageEventListener = new OnPageEventListener() {
        public void onPageMoved(boolean success, int index, int dstIndex) {
            if (success && LinkAnnotHandler.this.mLinkInfo.pageIndex != -1 && index != dstIndex && LinkAnnotHandler.this.mLinkInfo.pageIndex >= Math.min(index, dstIndex) && LinkAnnotHandler.this.mLinkInfo.pageIndex <= Math.max(index, dstIndex)) {
                if (LinkAnnotHandler.this.mLinkInfo.pageIndex == index) {
                    LinkAnnotHandler.this.mLinkInfo.pageIndex = dstIndex;
                } else if (index > dstIndex) {
                    r0 = LinkAnnotHandler.this.mLinkInfo;
                    r0.pageIndex++;
                } else {
                    r0 = LinkAnnotHandler.this.mLinkInfo;
                    r0.pageIndex--;
                }
            }
        }

        public void onPagesRemoved(boolean success, int[] pageIndexes) {
            if (success && LinkAnnotHandler.this.mLinkInfo.pageIndex != -1) {
                for (int i : pageIndexes) {
                    if (LinkAnnotHandler.this.mLinkInfo.pageIndex == i) {
                        LinkAnnotHandler.this.mLinkInfo.pageIndex = -1;
                        break;
                    }
                }
                if (LinkAnnotHandler.this.mLinkInfo.pageIndex == -1) {
                    LinkAnnotHandler.this.mLinkInfo.links.clear();
                    return;
                }
                LinkInfo linkInfo = LinkAnnotHandler.this.mLinkInfo;
                linkInfo.pageIndex -= count;
            }
        }

        public void onPagesInserted(boolean success, int dstIndex, int[] range) {
            if (success && LinkAnnotHandler.this.mLinkInfo.pageIndex != -1 && LinkAnnotHandler.this.mLinkInfo.pageIndex > dstIndex) {
                for (int i = 0; i < range.length / 2; i++) {
                    LinkInfo linkInfo = LinkAnnotHandler.this.mLinkInfo;
                    linkInfo.pageIndex += range[(i * 2) + 1];
                }
            }
        }
    };
    private Paint mPaint;
    private PDFViewCtrl mPdfViewCtrl;
    private final int mType;

    class LinkInfo {
        ArrayList<Link> links;
        int pageIndex;

        LinkInfo() {
        }
    }

    LinkAnnotHandler(Context context, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mPaint = new Paint();
        this.mPaint.setARGB(22, 0, TransportMediator.KEYCODE_MEDIA_PAUSE, 255);
        this.mType = 2;
        this.mLinkInfo = new LinkInfo();
        this.mLinkInfo.pageIndex = -1;
        this.mLinkInfo.links = new ArrayList();
    }

    private boolean isLoadLink(int pageIndex) {
        return this.mLinkInfo.pageIndex == pageIndex;
    }

    protected IPageEventListener getPageEventListener() {
        return this.mPageEventListener;
    }

    private void loadLinks(int pageIndex) {
        try {
            if (this.mPdfViewCtrl.getDoc() != null) {
                clear();
                PDFPage page = this.mPdfViewCtrl.getDoc().getPage(pageIndex);
                int count = page.getAnnotCount();
                this.mLinkInfo.pageIndex = pageIndex;
                for (int i = 0; i < count; i++) {
                    Annot annot = page.getAnnot(i);
                    if (annot != null && annot.getType() == 2) {
                        this.mLinkInfo.links.add((Link) annot);
                    }
                }
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public int getType() {
        return this.mType;
    }

    public boolean annotCanAnswer(Annot annot) {
        try {
            if (annot.getType() == this.mType) {
                return true;
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
        return false;
    }

    public RectF getAnnotBBox(Annot annot) {
        try {
            return annot.getRect();
        } catch (PDFException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isHitAnnot(Annot annot, PointF point) {
        return getAnnotBBox(annot).contains(point.x, point.y);
    }

    public void onAnnotSelected(Annot annot, boolean reRender) {
    }

    public void removeAnnot(Annot annot, boolean addUndo, Callback result) {
    }

    public void onAnnotDeselected(Annot annot, boolean reRender) {
    }

    public void addAnnot(int pageIndex, AnnotContent content, boolean addUndo, Callback result) {
    }

    public void modifyAnnot(Annot annot, AnnotContent content, boolean addUndo, Callback result) {
    }

    public boolean onTouchEvent(int pageIndex, MotionEvent motionEvent, Annot annot) {
        return false;
    }

    private PointF getDestinationPoint(Destination destination) {
        if (destination == null) {
            return null;
        }
        PointF pt = new PointF(0.0f, 0.0f);
        try {
            switch (destination.getZoomMode()) {
                case 1:
                    pt.x = destination.getLeft();
                    pt.y = destination.getTop();
                    return pt;
                case 3:
                case 7:
                    pt.y = destination.getTop();
                    return pt;
                case 4:
                case 8:
                    pt.x = destination.getLeft();
                    return pt;
                case 5:
                    pt.x = destination.getLeft();
                    pt.y = destination.getBottom();
                    return pt;
                default:
                    return pt;
            }
        } catch (PDFException e) {
            e.printStackTrace();
            return pt;
        }
        e.printStackTrace();
        return pt;
    }

    public void onDraw(int pageIndex, Canvas canvas) {
        if (!this.isDocClosed) {
            if (!isLoadLink(pageIndex)) {
                loadLinks(pageIndex);
            }
            if (this.mLinkInfo.links.size() != 0) {
                canvas.save();
                Rect clipRect = canvas.getClipBounds();
                Rect rect = new Rect();
                try {
                    int count = this.mLinkInfo.links.size();
                    for (int i = 0; i < count; i++) {
                        RectF rectF = ((Annot) this.mLinkInfo.links.get(i)).getRect();
                        this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, rectF, pageIndex);
                        rectF.round(rect);
                        if (rect.intersect(clipRect)) {
                            canvas.drawRect(rect, this.mPaint);
                        }
                    }
                    canvas.restore();
                } catch (PDFException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean onLongPress(int pageIndex, MotionEvent motionEvent, Annot annot) {
        return false;
    }

    public boolean onSingleTapConfirmed(int pageIndex, MotionEvent motionEvent, Annot annot) {
        if (!((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).isLinksEnabled()) {
            return false;
        }
        if (pageIndex != this.mPdfViewCtrl.getCurrentPage()) {
            return false;
        }
        try {
            Action annotAction = ((Link) annot).getAction();
            if (annotAction == null) {
                return false;
            }
            switch (annotAction.getType()) {
                case 0:
                    return false;
                case 1:
                    Destination destination = ((GotoAction) annotAction).getDestination();
                    if (destination != null) {
                        PointF destPt = getDestinationPoint(destination);
                        PointF devicePt = new PointF();
                        if (!this.mPdfViewCtrl.convertPdfPtToPageViewPt(destPt, devicePt, destination.getPageIndex())) {
                            devicePt.set(0.0f, 0.0f);
                        }
                        this.mPdfViewCtrl.gotoPage(destination.getPageIndex(), devicePt.x, devicePt.y);
                        break;
                    }
                    return false;
                case 6:
                    String uri = ((URIAction) annotAction).getURI();
                    if (!uri.toLowerCase().startsWith("mailto:")) {
                        AppUtil.openUrl((Activity) this.mContext, uri);
                        break;
                    }
                    AppUtil.mailTo((Activity) this.mContext, uri);
                    break;
            }
            this.mPdfViewCtrl.getDoc().closePage(pageIndex);
            return true;
        } catch (PDFException e1) {
            if (e1.getLastError() == PDFError.OOM.getCode()) {
                this.mPdfViewCtrl.recoverForOOM();
            }
            e1.printStackTrace();
            return true;
        }
    }

    protected void clear() {
        synchronized (this.mLinkInfo) {
            try {
                if (this.mLinkInfo.pageIndex != -1) {
                    this.mPdfViewCtrl.getDoc().closePage(this.mLinkInfo.pageIndex);
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
            this.mLinkInfo.pageIndex = -1;
            this.mLinkInfo.links.clear();
        }
    }
}
