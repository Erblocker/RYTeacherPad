package com.foxit.uiextensions.security.digitalsignature;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.http.Headers;
import android.os.Build.VERSION;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.Task;
import com.foxit.sdk.Task.CallBack;
import com.foxit.sdk.common.DateTime;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.signature.Signature;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.modules.DocInfoModule;
import com.foxit.uiextensions.security.certificate.CertificateFileInfo;
import com.foxit.uiextensions.security.certificate.CertificateSupport;
import com.foxit.uiextensions.security.certificate.CertificateViewSupport;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.Event.Callback;
import com.foxit.uiextensions.utils.UIToast;
import java.io.File;
import java.util.Calendar;

public class DigitalSignatureSecurityHandler {
    private Callback mCallback;
    private Context mContext;
    private boolean mIsFileChanged = false;
    private PDFViewCtrl mPdfViewCtrl;
    private ProgressDialog mProgressDialog;
    private boolean mSuccess;
    private long mVerifyResult = 0;
    private CertificateViewSupport mViewSupport;

    class AddSignatureTask extends Task {
        private Bitmap mBitmap;
        private String mDocPath;
        private CertificateFileInfo mInfo;
        private int mPageIndex;
        private RectF mRect;

        /* renamed from: com.foxit.uiextensions.security.digitalsignature.DigitalSignatureSecurityHandler$AddSignatureTask$1 */
        class AnonymousClass1 implements CallBack {
            private final /* synthetic */ DigitalSignatureSecurityHandler val$this$0;

            AnonymousClass1(DigitalSignatureSecurityHandler digitalSignatureSecurityHandler) {
                this.val$this$0 = digitalSignatureSecurityHandler;
            }

            public void result(Task task) {
                if (this.val$this$0.mProgressDialog != null) {
                    if (this.val$this$0.mProgressDialog.isShowing()) {
                        this.val$this$0.mProgressDialog.dismiss();
                    }
                    this.val$this$0.mProgressDialog = null;
                }
                if (this.val$this$0.mCallback != null) {
                    this.val$this$0.mCallback.result(null, this.val$this$0.mSuccess);
                }
            }
        }

        public AddSignatureTask(String docPath, CertificateFileInfo info, int pageIndex, Bitmap bitmap, RectF rect) {
            super(new AnonymousClass1(DigitalSignatureSecurityHandler.this));
            this.mDocPath = docPath;
            this.mInfo = info;
            this.mPageIndex = pageIndex;
            this.mBitmap = bitmap;
            this.mRect = rect;
        }

        protected void execute() {
            try {
                String location = Headers.LOCATION;
                String text = TestHandler.TEXT;
                DateTime dateTime = new DateTime();
                Calendar c = Calendar.getInstance();
                int offset = c.getTimeZone().getRawOffset();
                int tzMinute = (offset / 1000) % 3600;
                dateTime.set(c.get(1), c.get(2) + 1, c.get(5), c.get(10), c.get(12), c.get(13), 0, (short) (offset / 3600000), tzMinute);
                Signature signature = DigitalSignatureSecurityHandler.this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex).addSignature(this.mRect);
                signature.setKeyValue(6, "Adobe.PPKLite");
                signature.setKeyValue(7, "adbe.pkcs7.detached");
                signature.setKeyValue(4, "dn");
                signature.setKeyValue(1, location);
                signature.setKeyValue(2, "reason");
                signature.setKeyValue(3, "contactInfo");
                signature.setKeyValue(0, "signer");
                signature.setKeyValue(5, text);
                signature.setSigningTime(dateTime);
                signature.setBitmap(this.mBitmap);
                signature.setAppearanceFlags(128);
                for (int progress = signature.startSign(this.mDocPath, this.mInfo.filePath, this.mInfo.password.getBytes(), 0, null, null); progress == 1; progress = signature.continueSign()) {
                }
                if (signature.getState() == 2 && signature.isSigned()) {
                    DigitalSignatureSecurityHandler.this.mPdfViewCtrl.getDoc().closePage(this.mPageIndex);
                    DigitalSignatureSecurityHandler.this.mSuccess = true;
                }
            } catch (PDFException e) {
                DigitalSignatureSecurityHandler.this.mSuccess = false;
            }
        }
    }

    class VerifySignatureTask extends Task {
        private Annot mAnnot;

        /* renamed from: com.foxit.uiextensions.security.digitalsignature.DigitalSignatureSecurityHandler$VerifySignatureTask$1 */
        class AnonymousClass1 implements CallBack {
            private final /* synthetic */ Annot val$annot;
            private final /* synthetic */ DigitalSignatureSecurityHandler val$this$0;

            AnonymousClass1(DigitalSignatureSecurityHandler digitalSignatureSecurityHandler, Annot annot) {
                this.val$this$0 = digitalSignatureSecurityHandler;
                this.val$annot = annot;
            }

            public void result(Task task) {
                int theme;
                if (this.val$this$0.mProgressDialog != null) {
                    if (this.val$this$0.mProgressDialog.isShowing()) {
                        this.val$this$0.mProgressDialog.dismiss();
                    }
                    this.val$this$0.mProgressDialog = null;
                }
                Signature signature = this.val$annot;
                if (VERSION.SDK_INT >= 21) {
                    theme = 16974396;
                } else if (VERSION.SDK_INT >= 14) {
                    theme = 16974132;
                } else if (VERSION.SDK_INT >= 11) {
                    theme = 16973941;
                } else {
                    theme = R.style.rv_dialog_style;
                }
                final Dialog dialog = new Dialog(this.val$this$0.mContext, theme);
                View view = View.inflate(this.val$this$0.mContext, R.layout.rv_security_dsg_verify, null);
                dialog.setContentView(view, new LayoutParams(AppDisplay.getInstance(this.val$this$0.mContext).getDialogWidth(), -2));
                TextView tv = (TextView) view.findViewById(R.id.rv_security_dsg_verify_result);
                String resultText = "";
                switch ((int) this.val$this$0.mVerifyResult) {
                    case 4:
                        if (!this.val$this$0.mIsFileChanged) {
                            resultText = new StringBuilder(String.valueOf(resultText)).append(this.val$this$0.mContext.getString(R.string.rv_security_dsg_verify_valid)).append("\n").toString();
                            break;
                        } else {
                            resultText = new StringBuilder(String.valueOf(resultText)).append(this.val$this$0.mContext.getString(R.string.rv_security_dsg_verify_perm)).append("\n").toString();
                            break;
                        }
                    case 8:
                        resultText = new StringBuilder(String.valueOf(resultText)).append(this.val$this$0.mContext.getString(R.string.rv_security_dsg_verify_invalid)).append("\n").toString();
                        break;
                    case 64:
                        resultText = new StringBuilder(String.valueOf(resultText)).append(this.val$this$0.mContext.getString(R.string.rv_security_dsg_verify_errorByteRange)).append("\n").toString();
                        break;
                    default:
                        resultText = new StringBuilder(String.valueOf(resultText)).append(this.val$this$0.mContext.getString(R.string.rv_security_dsg_verify_otherState)).append("\n").toString();
                        break;
                }
                try {
                    resultText = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(resultText)).append(this.val$this$0.mContext.getString(R.string.rv_security_dsg_cert_publisher)).append(AppUtil.getEntryName(signature.getCertificateInfo("Issuer"), "CN=")).append("\n").toString())).append(this.val$this$0.mContext.getString(R.string.rv_security_dsg_cert_serialNumber)).append(signature.getCertificateInfo("SerialNumber")).append("\n").toString())).append(this.val$this$0.mContext.getString(R.string.rv_security_dsg_cert_emailAddress)).append(AppUtil.getEntryName(signature.getCertificateInfo("Subject"), "E=")).append("\n").toString())).append(this.val$this$0.mContext.getString(R.string.rv_security_dsg_cert_validityStarts)).append(signature.getCertificateInfo("ValidPeriodFrom")).append("\n").toString())).append(this.val$this$0.mContext.getString(R.string.rv_security_dsg_cert_validityEnds)).append(signature.getCertificateInfo("ValidPeriodTo")).append("\n").toString();
                    resultText = new StringBuilder(String.valueOf(resultText)).append(new StringBuilder(String.valueOf(this.val$this$0.mContext.getString(R.string.rv_security_dsg_cert_signedTime))).append(AppDmUtil.getLocalDateString(signature.getSigningTime())).toString()).append("\n").toString();
                } catch (PDFException e) {
                    e.printStackTrace();
                }
                tv.setText(resultText);
                dialog.setCanceledOnTouchOutside(true);
                ((Activity) this.val$this$0.mContext).runOnUiThread(new Runnable() {
                    public void run() {
                        dialog.show();
                    }
                });
            }
        }

        public VerifySignatureTask(Annot annot) {
            super(new AnonymousClass1(DigitalSignatureSecurityHandler.this, annot));
            this.mAnnot = annot;
        }

        protected void execute() {
            if (this.mAnnot != null && (this.mAnnot instanceof Signature)) {
                Signature signature = this.mAnnot;
                try {
                    for (int progress = signature.startVerify(null, null); progress == 1; progress = signature.continueVerify()) {
                    }
                    DigitalSignatureSecurityHandler.this.mVerifyResult = signature.getState();
                    int[] byteRanges = signature.getByteRanges();
                    DocInfoModule module = (DocInfoModule) ((UIExtensionsManager) DigitalSignatureSecurityHandler.this.mPdfViewCtrl.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_DOCINFO);
                    if (module != null) {
                        File file = new File(module.getFilePath());
                        if (byteRanges == null || file.length() == ((long) (byteRanges[2] + byteRanges[3]))) {
                            DigitalSignatureSecurityHandler.this.mIsFileChanged = false;
                        } else {
                            DigitalSignatureSecurityHandler.this.mIsFileChanged = true;
                        }
                    }
                } catch (PDFException e) {
                    e.printStackTrace();
                    UIToast.getInstance(DigitalSignatureSecurityHandler.this.mContext).show(DigitalSignatureSecurityHandler.this.mContext.getResources().getString(R.string.rv_security_dsg_verify_error));
                }
            }
        }
    }

    public DigitalSignatureSecurityHandler(Context dmContext, PDFViewCtrl pdfViewCtrl, CertificateSupport support) {
        this.mContext = dmContext;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mViewSupport = new CertificateViewSupport(this.mContext, support);
    }

    public void addSignature(String docPath, CertificateFileInfo info, Bitmap bitmap, int pageIndex, RectF rect, Callback callback) {
        this.mProgressDialog = new ProgressDialog(this.mContext);
        this.mProgressDialog.setMessage(this.mContext.getResources().getString(R.string.rv_sign_waiting));
        this.mProgressDialog.setProgressStyle(0);
        this.mProgressDialog.setCancelable(false);
        this.mProgressDialog.setIndeterminate(true);
        this.mProgressDialog.show();
        this.mCallback = callback;
        this.mPdfViewCtrl.addTask(new AddSignatureTask(docPath, info, pageIndex, bitmap, rect));
    }

    public void verifySignature(Annot annot) throws PDFException {
        this.mProgressDialog = new ProgressDialog(this.mContext);
        this.mProgressDialog.setMessage(this.mContext.getResources().getString(R.string.rv_sign_waiting));
        this.mProgressDialog.setProgressStyle(0);
        this.mProgressDialog.setCancelable(false);
        this.mProgressDialog.setIndeterminate(true);
        this.mProgressDialog.show();
        this.mPdfViewCtrl.addTask(new VerifySignatureTask(annot));
    }
}
