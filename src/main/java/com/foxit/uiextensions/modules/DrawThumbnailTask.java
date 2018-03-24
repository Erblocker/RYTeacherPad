package com.foxit.uiextensions.modules;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import com.foxit.sdk.Task;
import com.foxit.sdk.Task.CallBack;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.Renderer;

/* compiled from: ThumbnailSupport */
class DrawThumbnailTask extends Task {
    private Bitmap mBmp;
    private final Rect mBmpArea;
    private DrawThumbnailCallback mCallback;
    private final PDFPage mPDFPage;
    private ThumbnailItem mThumbnailItem;
    private final Point mViewSize;

    /* compiled from: ThumbnailSupport */
    /* renamed from: com.foxit.uiextensions.modules.DrawThumbnailTask$1 */
    class AnonymousClass1 implements CallBack {
        private final /* synthetic */ ThumbnailItem val$item;

        AnonymousClass1(ThumbnailItem thumbnailItem) {
            this.val$item = thumbnailItem;
        }

        public void result(Task task) {
            this.val$item.resetRending(false);
            DrawThumbnailTask task1 = (DrawThumbnailTask) task;
            if (task1.mStatus == 3 && task1.mCallback != null) {
                task1.mCallback.result(this.val$item, task1, ((DrawThumbnailTask) task).mBmp);
            }
        }
    }

    public DrawThumbnailTask(ThumbnailItem item, DrawThumbnailCallback callback) {
        super(new AnonymousClass1(item));
        this.mCallback = callback;
        this.mPDFPage = item.getPage();
        this.mBmpArea = new Rect(0, 0, item.getSize().x, item.getSize().y);
        this.mViewSize = item.getSize();
        this.mPriority = 3;
        this.mThumbnailItem = item;
        this.mThumbnailItem.resetRending(true);
    }

    public String toString() {
        return null;
    }

    protected void prepare() {
        if (this.mBmp == null) {
            this.mBmp = Bitmap.createBitmap(this.mBmpArea.width(), this.mBmpArea.height(), Config.ARGB_8888);
        }
    }

    protected void execute() {
        if (this.mStatus == 1) {
            this.mStatus = 2;
            if (this.mBmpArea.width() == 0 || this.mBmpArea.height() == 0) {
                this.mStatus = -1;
            } else if (this.mBmp != null) {
                renderPage();
            } else {
                this.mErr = 6;
                this.mStatus = -1;
            }
        }
    }

    private void renderPage() {
        try {
            int nRet = this.mPDFPage.startParse(0, null, false);
            while (nRet == 1) {
                nRet = this.mPDFPage.continueParse();
            }
            Matrix matrix = this.mPDFPage.getDisplayMatrix(-this.mBmpArea.left, -this.mBmpArea.top, this.mViewSize.x, this.mViewSize.y, 0);
            if (this.mPDFPage.hasTransparency()) {
                this.mBmp.eraseColor(0);
            } else {
                this.mBmp.eraseColor(-1);
            }
            Renderer render = Renderer.create(this.mBmp);
            if (render == null) {
                this.mErr = 6;
                this.mStatus = -1;
                return;
            }
            render.setColorMode(0);
            render.setRenderContent(3);
            for (int progress = render.startRender(this.mPDFPage, matrix, null); progress == 1; progress = render.continueRender()) {
            }
            render.release();
            this.mErr = 0;
            this.mStatus = 3;
        } catch (PDFException e) {
            this.mErr = e.getLastError();
            this.mStatus = -1;
        }
    }

    public ThumbnailItem getThumbnailItem() {
        return this.mThumbnailItem;
    }
}
