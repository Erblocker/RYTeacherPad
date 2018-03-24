package com.foxit.uiextensions.modules.signature;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import com.foxit.sdk.Task;
import com.foxit.sdk.Task.CallBack;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.graphics.PDFImageObject;
import com.foxit.sdk.pdf.psi.PSI;
import com.foxit.uiextensions.utils.Event.Callback;

public class SignaturePSITask extends Task {
    protected static PSI mPsi;
    private SignatureEvent mEvent;

    /* renamed from: com.foxit.uiextensions.modules.signature.SignaturePSITask$1 */
    class AnonymousClass1 implements CallBack {
        private final /* synthetic */ Callback val$callBack;
        private final /* synthetic */ SignatureEvent val$event;

        AnonymousClass1(SignatureEvent signatureEvent, Callback callback) {
            this.val$event = signatureEvent;
            this.val$callBack = callback;
        }

        public void result(Task task) {
            if (this.val$event instanceof SignatureDrawEvent) {
                if (((SignatureDrawEvent) this.val$event).mCallBack != null) {
                    ((SignatureDrawEvent) this.val$event).mCallBack.result(this.val$event, true);
                }
            } else if ((this.val$event instanceof SignatureSignEvent) && ((SignatureSignEvent) this.val$event).mCallBack != null) {
                ((SignatureSignEvent) this.val$event).mCallBack.result(this.val$event, true);
            }
            if (this.val$callBack != null) {
                this.val$callBack.result(this.val$event, true);
            }
        }
    }

    public SignaturePSITask(SignatureEvent event, Callback callBack) {
        super(new AnonymousClass1(event, callBack));
        this.mEvent = event;
    }

    public Bitmap getBitmap() {
        try {
            if (mPsi != null) {
                return mPsi.getBitmap();
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void execute() {
        if (this.mEvent instanceof SignatureDrawEvent) {
            try {
                if (((SignatureDrawEvent) this.mEvent).mType == 10) {
                    mPsi = PSI.create(((SignatureDrawEvent) this.mEvent).mBitmap, false);
                    mPsi.setColor((long) ((SignatureDrawEvent) this.mEvent).mColor);
                    mPsi.setDiameter((int) ((SignatureDrawEvent) this.mEvent).mThickness);
                    mPsi.setOpacity(1.0f);
                } else if (((SignatureDrawEvent) this.mEvent).mType == 12) {
                    mPsi.setDiameter((int) ((SignatureDrawEvent) this.mEvent).mThickness);
                } else if (((SignatureDrawEvent) this.mEvent).mType == 11) {
                    mPsi.setColor((long) ((SignatureDrawEvent) this.mEvent).mColor);
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        } else if (this.mEvent instanceof SignatureSignEvent) {
            try {
                PDFImageObject imageObject = PDFImageObject.create(((SignatureSignEvent) this.mEvent).mPage.getDocument());
                imageObject.setBitmap(((SignatureSignEvent) this.mEvent).mBitmap, null);
                Matrix matrix = new Matrix();
                matrix.setScale(Math.abs(((SignatureSignEvent) this.mEvent).mRect.width()), Math.abs(((SignatureSignEvent) this.mEvent).mRect.height()));
                matrix.postTranslate(((SignatureSignEvent) this.mEvent).mRect.left, ((SignatureSignEvent) this.mEvent).mRect.bottom);
                imageObject.setMatrix(matrix);
                ((SignatureSignEvent) this.mEvent).mPage.insertGraphicsObject(0, imageObject);
                ((SignatureSignEvent) this.mEvent).mPage.generateContent();
            } catch (PDFException e2) {
                e2.printStackTrace();
            }
        }
    }
}
