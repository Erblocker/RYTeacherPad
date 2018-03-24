package com.foxit.uiextensions.modules.signature;

import android.content.Context;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.UIExtensionsManager;

public class SignatureMixListPopup {

    /* renamed from: com.foxit.uiextensions.modules.signature.SignatureMixListPopup$2 */
    class AnonymousClass2 implements OnDismissListener {
        private final /* synthetic */ SignatureListPicker val$listPicker;
        private final /* synthetic */ PDFViewCtrl val$pdfViewCtrl;

        AnonymousClass2(SignatureListPicker signatureListPicker, PDFViewCtrl pDFViewCtrl) {
            this.val$listPicker = signatureListPicker;
            this.val$pdfViewCtrl = pDFViewCtrl;
        }

        public void onDismiss() {
            if (this.val$listPicker.getBaseItemsSize() == 0) {
                ((UIExtensionsManager) this.val$pdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
            }
            this.val$listPicker.dismiss();
        }
    }

    /* renamed from: com.foxit.uiextensions.modules.signature.SignatureMixListPopup$4 */
    class AnonymousClass4 implements OnDismissListener {
        private final /* synthetic */ SignatureListPicker val$listPicker;
        private final /* synthetic */ PDFViewCtrl val$pdfViewCtrl;

        AnonymousClass4(SignatureListPicker signatureListPicker, PDFViewCtrl pDFViewCtrl) {
            this.val$listPicker = signatureListPicker;
            this.val$pdfViewCtrl = pDFViewCtrl;
        }

        public void onDismiss() {
            if (this.val$listPicker.getBaseItemsSize() == 0) {
                ((UIExtensionsManager) this.val$pdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
                this.val$pdfViewCtrl.invalidate();
            }
            this.val$listPicker.dismiss();
            boolean z = ((UIExtensionsManager) this.val$pdfViewCtrl.getUIExtensionsManager()).getCurrentToolHandler() instanceof SignatureToolHandler;
        }
    }

    static class Popup extends PopupWindow {
        public Popup(View view) {
            super(view, -1, -1, true);
            setWindowLayoutMode(0, -1);
            setWidth(-1);
            setBackgroundDrawable(new ColorDrawable(-1));
            setAnimationStyle(R.style.View_Animation_RtoL);
            setContentView(view);
            setFocusable(true);
            setOutsideTouchable(true);
        }
    }

    /* renamed from: com.foxit.uiextensions.modules.signature.SignatureMixListPopup$1 */
    class AnonymousClass1 implements ISignListPickerDismissCallback {
        private final /* synthetic */ Popup val$popup;

        AnonymousClass1(Popup popup) {
            this.val$popup = popup;
        }

        public void onDismiss() {
            this.val$popup.dismiss();
        }
    }

    /* renamed from: com.foxit.uiextensions.modules.signature.SignatureMixListPopup$3 */
    class AnonymousClass3 implements ISignListPickerDismissCallback {
        private final /* synthetic */ Popup val$popup;

        AnonymousClass3(Popup popup) {
            this.val$popup = popup;
        }

        public void onDismiss() {
            this.val$popup.dismiss();
        }
    }

    static void show(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl, SignatureInkCallback inkCallback) {
        SignatureListPicker listPicker = new SignatureListPicker(context, parent, pdfViewCtrl, inkCallback);
        Popup popup = new Popup(listPicker.getRootView());
        listPicker.init(new AnonymousClass1(popup));
        popup.showAtLocation(parent, 5, 0, 0);
        popup.setOnDismissListener(new AnonymousClass2(listPicker, pdfViewCtrl));
    }

    static void show(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl, SignatureInkCallback inkCallback, RectF menuRect) {
        SignatureListPicker listPicker = new SignatureListPicker(context, parent, pdfViewCtrl, inkCallback);
        Popup popup = new Popup(listPicker.getRootView());
        listPicker.init(new AnonymousClass3(popup));
        popup.showAtLocation(parent, 5, 0, 0);
        popup.setOnDismissListener(new AnonymousClass4(listPicker, pdfViewCtrl));
    }
}
