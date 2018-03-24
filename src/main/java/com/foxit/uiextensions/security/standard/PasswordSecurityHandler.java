package com.foxit.uiextensions.security.standard;

import android.view.KeyEvent;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.SecurityHandler;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.security.ISecurityHandler;
import com.foxit.uiextensions.security.ISecurityItemHandler;

public class PasswordSecurityHandler implements ISecurityHandler, ISecurityItemHandler {
    public int[] mDecryptItems = null;
    public int[] mEncryptItems = null;
    private PDFViewCtrl mPdfViewCtrl;
    private PasswordStandardSupport mSupport;

    public PasswordSecurityHandler(PasswordStandardSupport support, PDFViewCtrl pdfViewCtrl) {
        this.mSupport = support;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mEncryptItems = new int[]{R.string.rv_doc_encrpty_standard};
        this.mDecryptItems = new int[]{R.string.rv_doc_encrpty_standard_remove};
    }

    public int getSupportedTypes() {
        return 1;
    }

    public String getName() {
        return Module.SECURITY_NAME_PASSWORD;
    }

    public boolean isOwner(int securityPermission) {
        if (this.mSupport != null) {
            return this.mSupport.getIsOwner();
        }
        return true;
    }

    public boolean canPrintHighQuality(int securityPermission) {
        if (!this.mSupport.getIsOwner() && (securityPermission & 2048) == 0) {
            return false;
        }
        return true;
    }

    public boolean canPrint(int securityPermission) {
        if (!this.mSupport.getIsOwner() && (securityPermission & 2048) == 0) {
            return false;
        }
        return true;
    }

    public boolean canCopy(int securityPermission) {
        if (!this.mSupport.getIsOwner() && (securityPermission & 16) == 0) {
            return false;
        }
        return true;
    }

    public boolean canCopyForAssess(int securityPermission) {
        if (!this.mSupport.getIsOwner() && (securityPermission & 512) == 0) {
            return false;
        }
        return true;
    }

    public boolean canAssemble(int securityPermission) {
        if (!this.mSupport.getIsOwner() && (securityPermission & 1024) == 0 && (securityPermission & 8) == 0) {
            return false;
        }
        return true;
    }

    public boolean canFillForm(int securityPermission) {
        if (!this.mSupport.getIsOwner() && (securityPermission & 256) == 0) {
            return false;
        }
        return true;
    }

    public boolean canAddAnnot(int securityPermission) {
        if (!this.mSupport.getIsOwner() && (securityPermission & 32) == 0) {
            return false;
        }
        return true;
    }

    public boolean canModifyContents(int securityPermission) {
        if (!this.mSupport.getIsOwner() && (securityPermission & 8) == 0) {
            return false;
        }
        return true;
    }

    public int[] getItemIds() {
        if (this.mPdfViewCtrl.getDoc() == null) {
            return null;
        }
        try {
            if (this.mPdfViewCtrl.getDoc().getEncryptionType() == 1) {
                return this.mDecryptItems;
            }
            return this.mEncryptItems;
        } catch (PDFException e) {
            e.printStackTrace();
            return this.mDecryptItems;
        }
    }

    public boolean isAvailable() {
        if (this.mPdfViewCtrl.getDoc() == null) {
            return false;
        }
        try {
            if (this.mPdfViewCtrl.getDoc().isXFA()) {
                return false;
            }
            if (this.mPdfViewCtrl.getDoc().isEncrypted() && this.mPdfViewCtrl.getDoc().getEncryptionType() != 1) {
                return false;
            }
            SecurityHandler securityHandler = this.mPdfViewCtrl.getDoc().getSecurityHandler();
            boolean isOwner = DocumentManager.getInstance(this.mPdfViewCtrl).isOwner();
            if (securityHandler != null) {
                isOwner = isOwner(0);
            }
            if (!isOwner && securityHandler != null && securityHandler.getSecurityType() != 1) {
                return false;
            }
            if (DocumentManager.getInstance(this.mPdfViewCtrl).canModifyFile()) {
                if (DocumentManager.getInstance(this.mPdfViewCtrl).isSign()) {
                    return false;
                }
                return true;
            } else if (DocumentManager.getInstance(this.mPdfViewCtrl).canSaveAsFile()) {
                return true;
            } else {
                return false;
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void onActive(int itemId) {
        DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
        if (this.mPdfViewCtrl.getUIExtensionsManager() != null) {
            ToolHandler selectionTool = ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getToolHandlerByType(ToolHandler.TH_TYPE_TEXTSELECT);
            if (selectionTool != null) {
                selectionTool.onDeactivate();
            }
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean canSigning(int permissions) {
        if (canAddAnnot(permissions) || canFillForm(permissions) || canModifyContents(permissions)) {
            return true;
        }
        return false;
    }
}
