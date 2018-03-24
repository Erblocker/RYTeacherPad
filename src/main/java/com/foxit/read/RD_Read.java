package com.foxit.read;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import com.foxit.app.App;
import com.foxit.home.R;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.PDFViewCtrl.IDocEventListener;
import com.foxit.sdk.PDFViewCtrl.IDoubleTapEventListener;
import com.foxit.sdk.PDFViewCtrl.IRecoveryEventListener;
import com.foxit.sdk.PDFViewCtrl.ITouchEventListener;
import com.foxit.sdk.PDFViewCtrl.UIExtensionsManager;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.UIExtensionsManager.ToolHandlerChangedListener;
import com.foxit.uiextensions.annots.fileattachment.FileAttachmentModule;
import com.foxit.uiextensions.annots.form.FormFillerModule;
import com.foxit.uiextensions.controls.dialog.MatchDialog.DialogListener;
import com.foxit.uiextensions.controls.dialog.UITextEditDialog;
import com.foxit.uiextensions.controls.dialog.fileselect.UIFolderSelectDialog;
import com.foxit.uiextensions.modules.PageNavigationModule;
import com.foxit.uiextensions.modules.SearchModule;
import com.foxit.uiextensions.modules.signature.SignatureModule;
import com.foxit.uiextensions.modules.signature.SignatureToolHandler;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.AppFileUtil;
import com.foxit.uiextensions.utils.AppResource;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.UIToast;
import com.foxit.view.menu.MoreMenuModule;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Iterator;

public class RD_Read implements IRD_Read {
    protected boolean bDocClosed = false;
    private String currentFileCachePath;
    private boolean isSaveDoc = false;
    private Context mContext;
    private DocumentManager mDocMgr;
    private String mDocPath;
    private PDFViewCtrl mDocViewerCtrl;
    private String mLanguage;
    private ArrayList<ILifecycleEventListener> mLifecycleEventList = new ArrayList();
    private RD_MainFrame mMainFrame;
    private ProgressDialog mProgressDlg;
    private AlertDialog mSaveAlertDlg;
    private int mState = 1;
    private ArrayList<IRD_StateChangeListener> mStateChangeEventList = new ArrayList();
    private UIExtensionsManager mUIExtensionsManager;

    protected void setDocPath(String docPath) {
        this.mDocPath = docPath;
    }

    public RD_Read(Context context) {
        boolean z = true;
        this.mContext = context;
        this.mDocViewerCtrl = new PDFViewCtrl(this.mContext);
        this.mDocMgr = DocumentManager.getInstance(this.mDocViewerCtrl);
        Context context2 = this.mContext;
        if (App.instance().getDisplay().isPad()) {
            z = false;
        }
        this.mMainFrame = new RD_MainFrame(context2, z);
        this.mMainFrame.addDocView(this.mDocViewerCtrl);
        this.mProgressDlg = new ProgressDialog(context);
        this.mProgressDlg.setProgressStyle(0);
        this.mProgressDlg.setCancelable(false);
        this.mProgressDlg.setIndeterminate(false);
        this.mUIExtensionsManager = new com.foxit.uiextensions.UIExtensionsManager(this.mContext, this.mMainFrame.getContentView(), this.mDocViewerCtrl);
        this.mDocViewerCtrl.registerTouchEventListener(new ITouchEventListener() {
            public boolean onTouchEvent(MotionEvent motionEvent) {
                return false;
            }
        });
        this.mDocViewerCtrl.registerDoubleTapEventListener(new IDoubleTapEventListener() {
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                if (RD_Read.this.getMainFrame().isToolbarsVisible()) {
                    RD_Read.this.getMainFrame().hideToolbars();
                } else {
                    RD_Read.this.getMainFrame().showToolbars();
                }
                return true;
            }

            public boolean onDoubleTap(MotionEvent motionEvent) {
                if (RD_Read.this.getMainFrame().isToolbarsVisible()) {
                    RD_Read.this.getMainFrame().hideToolbars();
                }
                return false;
            }

            public boolean onDoubleTapEvent(MotionEvent motionEvent) {
                return false;
            }
        });
        this.mDocViewerCtrl.registerRecoveryEventListener(new IRecoveryEventListener() {
            public void onWillRecover() {
                synchronized (AppFileUtil.getInstance().isOOMHappened) {
                    AppFileUtil.getInstance().isOOMHappened = Boolean.valueOf(true);
                }
            }

            public void onRecovered() {
                AppFileUtil.getInstance().isOOMHappened = Boolean.valueOf(false);
            }
        });
        this.mDocViewerCtrl.setUIExtensionsManager(this.mUIExtensionsManager);
        ((com.foxit.uiextensions.UIExtensionsManager) this.mUIExtensionsManager).registerToolHandlerChangedListener(new ToolHandlerChangedListener() {
            public void onToolHandlerChanged(ToolHandler oldToolHandler, ToolHandler newToolHandler) {
                if (newToolHandler instanceof SignatureToolHandler) {
                    RD_Read.this.mMainFrame.resetAnnotCustomBottomBar();
                    RD_Read.this.mMainFrame.resetAnnotCustomTopBar();
                    RD_Read.this.changeState(5);
                } else if (newToolHandler != null) {
                    RD_Read.this.changeState(6);
                } else if (RD_Read.this.getState() == 6) {
                    RD_Read.this.changeState(4);
                }
                if ((oldToolHandler instanceof SignatureToolHandler) && newToolHandler == null) {
                    RD_Read.this.changeState(1);
                }
                if (oldToolHandler != null && newToolHandler != null) {
                    RD_Read.this.getMainFrame().showToolbars();
                }
            }
        });
        this.mDocViewerCtrl.registerDocEventListener(new IDocEventListener() {
            public void onDocWillOpen() {
                RD_Read.this.mProgressDlg.setMessage("opening");
                RD_Read.this.mProgressDlg.show();
            }

            public void onDocOpened(PDFDoc pdfDoc, int i) {
                RD_Read.this.mProgressDlg.dismiss();
            }

            public void onDocWillClose(PDFDoc pdfDoc) {
                RD_Read.this.mProgressDlg.setMessage("closing");
                RD_Read.this.mProgressDlg.show();
            }

            public void onDocClosed(PDFDoc pdfDoc, int i) {
                RD_Read.this.mProgressDlg.dismiss();
            }

            public void onDocWillSave(PDFDoc pdfDoc) {
                RD_Read.this.mProgressDlg.setMessage("saving");
                RD_Read.this.mProgressDlg.show();
            }

            public void onDocSaved(PDFDoc pdfDoc, int i) {
                RD_Read.this.mProgressDlg.dismiss();
            }
        });
    }

    public void release() {
        this.mLifecycleEventList.clear();
        this.mStateChangeEventList.clear();
        this.mContext = null;
        this.mDocViewerCtrl = null;
        DocumentManager.release();
        this.mDocMgr = null;
        this.mUIExtensionsManager = null;
    }

    public void init() {
        this.mMainFrame.init(this);
    }

    public boolean registerLifecycleListener(ILifecycleEventListener listener) {
        this.mLifecycleEventList.add(listener);
        return true;
    }

    public boolean unregisterLifecycleListener(ILifecycleEventListener listener) {
        this.mLifecycleEventList.remove(listener);
        return true;
    }

    public boolean registerStateChangeListener(IRD_StateChangeListener listener) {
        this.mStateChangeEventList.add(listener);
        return true;
    }

    public boolean unregisterStateChangeListener(IRD_StateChangeListener listener) {
        this.mStateChangeEventList.remove(listener);
        return true;
    }

    public IRD_MainFrame getMainFrame() {
        return this.mMainFrame;
    }

    public UIExtensionsManager getUIExtensionsManager() {
        return this.mUIExtensionsManager;
    }

    public PDFViewCtrl getDocViewer() {
        return this.mDocViewerCtrl;
    }

    public DocumentManager getDocMgr() {
        return this.mDocMgr;
    }

    public int getState() {
        return this.mState;
    }

    public void changeState(int state) {
        App.instance().getEventManager().triggerInteractTimer();
        int oldState = this.mState;
        this.mState = state;
        Iterator it = this.mStateChangeEventList.iterator();
        while (it.hasNext()) {
            ((IRD_StateChangeListener) it.next()).onStateChanged(oldState, state);
        }
        PageNavigationModule module = (PageNavigationModule) ((com.foxit.uiextensions.UIExtensionsManager) getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_PAGENAV);
        if (module != null) {
            module.changPageNumberState(getMainFrame().isToolbarsVisible());
        }
    }

    void _resetStatusAfterClose() {
        changeState(1);
    }

    void _resetStatusBeforeOpen() {
        this.mMainFrame.showToolbars();
        this.mState = 1;
    }

    public void backToPrevActivity() {
        if (((com.foxit.uiextensions.UIExtensionsManager) this.mUIExtensionsManager).getCurrentToolHandler() != null) {
            ((com.foxit.uiextensions.UIExtensionsManager) this.mUIExtensionsManager).setCurrentToolHandler(null);
        }
        if (!(DocumentManager.getInstance(this.mDocViewerCtrl) == null || DocumentManager.getInstance(this.mDocViewerCtrl).getCurrentAnnot() == null)) {
            DocumentManager.getInstance(this.mDocViewerCtrl).setCurrentAnnot(null);
        }
        if (this.mMainFrame.getAttachedActivity() == null) {
            closeAllDocuments();
            return;
        }
        try {
            if (this.mDocViewerCtrl.getDoc() == null || !this.mDocViewerCtrl.getDoc().isModified()) {
                closeAllDocuments();
                return;
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
        if (true) {
            this.mProgressDlg.setMessage("saving");
            this.mProgressDlg.show();
            this.mDocViewerCtrl.saveDoc(getCacheFile(), 1);
            this.isSaveDoc = true;
            closeAllDocuments();
            return;
        }
        boolean hideSave;
        if (DocumentManager.getInstance(this.mDocViewerCtrl).canModifyContents()) {
            hideSave = false;
        } else {
            hideSave = true;
        }
        Builder builder = new Builder(this.mMainFrame.getAttachedActivity());
        builder.setItems(hideSave ? new String[]{AppResource.getString(this.mContext, R.string.rv_back_saveas), AppResource.getString(this.mContext, R.string.rv_back_discard_modify)} : new String[]{AppResource.getString(this.mContext, R.string.rv_back_save), AppResource.getString(this.mContext, R.string.rv_back_saveas), AppResource.getString(this.mContext, R.string.rv_back_discard_modify)}, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (hideSave) {
                    which++;
                }
                switch (which) {
                    case 0:
                        RD_Read.this.mProgressDlg.setMessage("saving");
                        RD_Read.this.mProgressDlg.show();
                        RD_Read.this.mDocViewerCtrl.saveDoc(RD_Read.this.getCacheFile(), 1);
                        RD_Read.this.isSaveDoc = true;
                        RD_Read.this.closeAllDocuments();
                        break;
                    case 1:
                        onSaveAsClicked();
                        break;
                    case 2:
                        RD_Read.this.closeAllDocuments();
                        break;
                }
                dialog.dismiss();
                RD_Read.this.mSaveAlertDlg = null;
            }

            void showInputFileNameDialog(final String fileFolder) {
                String fileName = AppFileUtil.getFileNameWithoutExt(AppFileUtil.getFileDuplicateName(new StringBuilder(String.valueOf(fileFolder)).append("/").append(AppFileUtil.getFileName(RD_Read.this.mDocPath)).toString()));
                final UITextEditDialog rmDialog = new UITextEditDialog(RD_Read.this.mMainFrame.getAttachedActivity());
                rmDialog.setPattern("[/\\:*?<>|\"\n\t]");
                rmDialog.setTitle(AppResource.getString(RD_Read.this.mContext, R.string.fx_string_saveas));
                rmDialog.getPromptTextView().setVisibility(8);
                rmDialog.getInputEditText().setText(fileName);
                rmDialog.getInputEditText().selectAll();
                rmDialog.show();
                AppUtil.showSoftInput(rmDialog.getInputEditText());
                rmDialog.getOKButton().setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        rmDialog.dismiss();
                        String newPath = new StringBuilder(String.valueOf(fileFolder + "/" + rmDialog.getInputEditText().getText().toString())).append(".pdf").toString();
                        if (new File(newPath).exists()) {
                            AnonymousClass6.this.showAskReplaceDialog(fileFolder, newPath);
                            return;
                        }
                        RD_Read.this.mProgressDlg.setMessage("saving");
                        RD_Read.this.mProgressDlg.show();
                        RD_Read.this.mDocViewerCtrl.saveDoc(newPath, 1);
                        RD_Read.this.closeAllDocuments();
                    }
                });
            }

            void showAskReplaceDialog(final String fileFolder, final String newPath) {
                final UITextEditDialog rmDialog = new UITextEditDialog(RD_Read.this.mMainFrame.getAttachedActivity());
                rmDialog.setTitle(R.string.fx_string_saveas);
                rmDialog.getPromptTextView().setText(R.string.fx_string_filereplace_warning);
                rmDialog.getInputEditText().setVisibility(8);
                rmDialog.show();
                rmDialog.getOKButton().setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        rmDialog.dismiss();
                        RD_Read.this.mDocViewerCtrl.saveDoc(newPath, 1);
                        RD_Read.this.closeAllDocuments();
                    }
                });
                rmDialog.getCancelButton().setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        rmDialog.dismiss();
                        AnonymousClass6.this.showInputFileNameDialog(fileFolder);
                    }
                });
            }

            void onSaveAsClicked() {
                final UIFolderSelectDialog dialog = new UIFolderSelectDialog(RD_Read.this.mMainFrame.getAttachedActivity());
                dialog.setFileFilter(new FileFilter() {
                    public boolean accept(File pathname) {
                        return !pathname.isHidden() && pathname.canRead() && (!pathname.isFile() || pathname.getName().toLowerCase().endsWith(".pdf"));
                    }
                });
                dialog.setTitle(AppResource.getString(RD_Read.this.mContext, R.string.fx_string_saveas));
                dialog.setButton(5);
                dialog.setListener(new DialogListener() {
                    public void onResult(long btType) {
                        if (btType == 4) {
                            AnonymousClass6.this.showInputFileNameDialog(dialog.getCurrentPath());
                        }
                        dialog.dismiss();
                    }

                    public void onBackClick() {
                    }
                });
                dialog.showDialog();
            }
        });
        this.mSaveAlertDlg = builder.create();
        this.mSaveAlertDlg.setCanceledOnTouchOutside(true);
        this.mSaveAlertDlg.show();
    }

    private String getCacheFile() {
        File file = new File(this.mDocPath);
        String dir = file.getParent() + "/";
        while (file.exists()) {
            this.currentFileCachePath = new StringBuilder(String.valueOf(dir)).append(AppDmUtil.randomUUID(null)).append(".pdf").toString();
            file = new File(this.currentFileCachePath);
        }
        return this.currentFileCachePath;
    }

    public void onCreate(Activity act, Bundle bundle) {
        Iterator it;
        if (!(this.mMainFrame.getAttachedActivity() == null || this.mMainFrame.getAttachedActivity() == act)) {
            it = this.mLifecycleEventList.iterator();
            while (it.hasNext()) {
                ((ILifecycleEventListener) it.next()).onDestroy(act);
            }
        }
        if (this.mLanguage == null) {
            this.mLanguage = act.getResources().getConfiguration().locale.getLanguage();
        }
        this.mMainFrame.setAttachedActivity(act);
        it = this.mLifecycleEventList.iterator();
        while (it.hasNext()) {
            ((ILifecycleEventListener) it.next()).onCreate(act, bundle);
        }
    }

    public void onStart(Activity act) {
        if (this.mMainFrame.getAttachedActivity() == act) {
            Iterator it = this.mLifecycleEventList.iterator();
            while (it.hasNext()) {
                ((ILifecycleEventListener) it.next()).onStart(act);
            }
        }
    }

    public void onPause(Activity act) {
        if (this.mMainFrame.getAttachedActivity() == act) {
            Iterator it = this.mLifecycleEventList.iterator();
            while (it.hasNext()) {
                ((ILifecycleEventListener) it.next()).onPause(act);
            }
        }
    }

    public void onResume(Activity act) {
        if (this.mMainFrame.getAttachedActivity() == act) {
            Iterator it = this.mLifecycleEventList.iterator();
            while (it.hasNext()) {
                ((ILifecycleEventListener) it.next()).onResume(act);
            }
            String curLanguage = act.getResources().getConfiguration().locale.getLanguage();
            if (!this.mLanguage.equals(curLanguage)) {
                this.mLanguage = curLanguage;
            }
        }
    }

    protected void onStop(Activity act) {
        if (this.mMainFrame.getAttachedActivity() == act) {
            Iterator it = this.mLifecycleEventList.iterator();
            while (it.hasNext()) {
                ((ILifecycleEventListener) it.next()).onStop(act);
            }
        }
    }

    protected void onDestroy(Activity act) {
        if (this.mMainFrame.getAttachedActivity() == act) {
            Iterator it = this.mLifecycleEventList.iterator();
            while (it.hasNext()) {
                ((ILifecycleEventListener) it.next()).onDestroy(act);
            }
            this.mMainFrame.setAttachedActivity(null);
            closeAllDocuments();
            release();
        }
    }

    protected void onActivityResult(Activity act, int requestCode, int resultCode, Intent data) {
    }

    public void onConfigurationChanged(Activity act, Configuration newConfig) {
        if (this.mMainFrame.getAttachedActivity() == act) {
            this.mMainFrame.onConfigurationChanged(newConfig);
            MoreMenuModule module = (MoreMenuModule) App.instance().getModuleByName(Module.MODULE_MORE_MENU);
            if (module != null) {
                module.onConfigurationChanged(newConfig);
            }
        }
    }

    public boolean onKeyDown(Activity act, int keyCode, KeyEvent event) {
        if (this.mMainFrame.getAttachedActivity() != act || keyCode != 4) {
            return false;
        }
        com.foxit.uiextensions.UIExtensionsManager uiExtensionsManager = (com.foxit.uiextensions.UIExtensionsManager) this.mDocViewerCtrl.getUIExtensionsManager();
        SearchModule searchModule = (SearchModule) App.instance().getModuleByName(Module.MODULE_NAME_SEARCH);
        if (searchModule == null || !searchModule.onKeyBack()) {
            FormFillerModule formFillerModule = (FormFillerModule) uiExtensionsManager.getModuleByName(Module.MODULE_NAME_FORMFILLER);
            if (formFillerModule == null || !formFillerModule.onKeyBack()) {
                ToolHandler currentToolHandler = uiExtensionsManager.getCurrentToolHandler();
                SignatureModule signature_module = (SignatureModule) uiExtensionsManager.getModuleByName(Module.MODULE_NAME_PSISIGNATURE);
                if (signature_module != null && (currentToolHandler instanceof SignatureToolHandler) && signature_module.onKeyBack()) {
                    changeState(1);
                    getMainFrame().showToolbars();
                    this.mDocViewerCtrl.invalidate();
                    return true;
                }
                FileAttachmentModule fileattachmodule = (FileAttachmentModule) uiExtensionsManager.getModuleByName(Module.MODULE_NAME_FILEATTACHMENT);
                if (fileattachmodule != null && fileattachmodule.onKeyDown(keyCode, event)) {
                    getMainFrame().showToolbars();
                    return true;
                } else if (DocumentManager.getInstance(this.mDocViewerCtrl).onKeyDown(keyCode, event)) {
                    return true;
                } else {
                    if (currentToolHandler != null) {
                        uiExtensionsManager.setCurrentToolHandler(null);
                        return true;
                    } else if (getState() != 1) {
                        changeState(1);
                        return true;
                    } else if (event.getRepeatCount() != 0) {
                        return false;
                    } else {
                        backToPrevActivity();
                        return true;
                    }
                }
            }
            changeState(1);
            getMainFrame().showToolbars();
            return true;
        }
        changeState(1);
        getMainFrame().showToolbars();
        return true;
    }

    public boolean onPrepareOptionsMenu(Activity act, Menu menu) {
        return this.mMainFrame.getAttachedActivity() == act && this.mDocViewerCtrl.getDoc() != null;
    }

    public void closeDocument() {
        _closeDocument();
    }

    private void closeAllDocuments() {
        if (!this.bDocClosed) {
            _closeDocument();
        } else if (this.mMainFrame.getAttachedActivity() != null) {
            this.mMainFrame.getAttachedActivity().finish();
        }
    }

    void _closeDocument() {
        getDocViewer().closeDoc();
        this.mMainFrame.resetMaskView();
    }

    public void closeDocumentSucceed() {
        if (!(this.mMainFrame == null || this.mMainFrame.getAttachedActivity() == null || !this.mMainFrame.getCloseAttachedActivity())) {
            this.mMainFrame.getAttachedActivity().finish();
        }
        if (this.isSaveDoc) {
            File file = new File(this.currentFileCachePath);
            File docFile = new File(this.mDocPath);
            if (file.exists()) {
                docFile.delete();
                if (!file.renameTo(docFile)) {
                    UIToast.getInstance(this.mContext).show((CharSequence) "Save document failed!");
                    return;
                }
                return;
            }
            UIToast.getInstance(this.mContext).show((CharSequence) "Save document failed!");
        }
    }

    public void openDocumentFailed() {
        if (this.mMainFrame.getAttachedActivity() != null) {
            this.mMainFrame.getAttachedActivity().finish();
        }
    }
}
