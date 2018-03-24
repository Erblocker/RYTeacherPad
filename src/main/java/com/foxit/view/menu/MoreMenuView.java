package com.foxit.view.menu;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewCompat;
import android.text.Selection;
import android.text.Spannable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import com.foxit.app.App;
import com.foxit.home.R;
import com.foxit.read.RD_Read;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.form.FormFillerModule;
import com.foxit.uiextensions.controls.dialog.MatchDialog.DialogListener;
import com.foxit.uiextensions.controls.dialog.UITextEditDialog;
import com.foxit.uiextensions.controls.dialog.fileselect.UIFileSelectDialog;
import com.foxit.uiextensions.controls.filebrowser.imp.FileItem;
import com.foxit.uiextensions.modules.DocInfoModule;
import com.foxit.uiextensions.modules.DocInfoView;
import com.foxit.uiextensions.security.digitalsignature.DigitalSignatureModule;
import com.foxit.uiextensions.security.digitalsignature.DigitalSignatureModule.DocPathChangeListener;
import com.foxit.uiextensions.security.standard.PasswordModule;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppFileUtil;
import com.foxit.uiextensions.utils.AppResource;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.UIToast;
import com.foxit.view.menu.MenuViewImpl.MenuCallback;
import java.io.File;
import java.io.FileFilter;
import java.util.List;

public class MoreMenuView {
    private MenuItemImpl deItem;
    private DocInfoModule docInfoModule = null;
    private MenuItemImpl enItem;
    private Context mContext = null;
    private String mExportFilePath = null;
    private MenuItemImpl mExportMenuItem = null;
    private MenuItemImpl mImportMenuItem = null;
    private PopupWindow mMenuPopupWindow = null;
    private MenuViewImpl mMoreMenu = null;
    private ViewGroup mParent = null;
    private String mPath = null;
    private PDFViewCtrl mPdfViewCtrl = null;
    private RD_Read mRead = null;
    private MenuItemImpl mResetMenuItem = null;
    private ViewGroup mRootView = null;
    private UITextEditDialog mSwitchDialog;

    public MoreMenuView(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl, RD_Read read, String path) {
        this.mContext = context;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mParent = parent;
        this.mRead = read;
        this.mPath = path;
    }

    public void show() {
        showMoreMenu();
    }

    public void hide() {
        hideMoreMenu();
    }

    public void initView() {
        if (this.mMoreMenu == null) {
            this.mMoreMenu = new MenuViewImpl(this.mContext, new MenuCallback() {
                public void onClosed() {
                    MoreMenuView.this.hideMoreMenu();
                }
            });
        }
        setMoreMenuView(this.mMoreMenu.getContentView());
    }

    public void addDocInfoItem() {
        if (this.mMoreMenu.getMenuGroup(0) == null) {
            this.mMoreMenu.addMenuGroup(new MenuGroupImpl(this.mContext, 0, AppResource.getString(this.mContext, R.string.rd_menu_file)));
        }
        this.mMoreMenu.addMenuItem(0, new MenuItemImpl(this.mContext, 0, AppResource.getString(this.mContext, R.string.rv_doc_info), 0, new MenuViewCallback() {
            public void onClick(MenuItemImpl item) {
                if (MoreMenuView.this.docInfoModule == null) {
                    MoreMenuView.this.docInfoModule = (DocInfoModule) ((UIExtensionsManager) MoreMenuView.this.mPdfViewCtrl.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_DOCINFO);
                }
                DocInfoView docInfoView = MoreMenuView.this.docInfoModule.getView();
                if (docInfoView != null) {
                    docInfoView.show();
                }
                MoreMenuView.this.hideMoreMenu();
            }
        }));
        Module module = ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_DIGITALSIGNATURE);
        if (module != null) {
            ((DigitalSignatureModule) module).setDocPathChangeListener(new DocPathChangeListener() {
                public void onDocPathChange(String newPath) {
                    MoreMenuView.this.mPath = newPath;
                    if (MoreMenuView.this.docInfoModule == null) {
                        MoreMenuView.this.docInfoModule = (DocInfoModule) ((UIExtensionsManager) MoreMenuView.this.mPdfViewCtrl.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_DOCINFO);
                    }
                    MoreMenuView.this.docInfoModule.setFilePath(MoreMenuView.this.mPath);
                }
            });
        }
    }

    public void addFormItem(final FormFillerModule module) {
        MenuGroupImpl group = this.mMoreMenu.getMenuGroup(2);
        if (group == null) {
            group = new MenuGroupImpl(this.mContext, 2, "Form");
        }
        this.mMoreMenu.addMenuGroup(group);
        if (this.mImportMenuItem == null) {
            this.mImportMenuItem = new MenuItemImpl(this.mContext, 1, AppResource.getString(this.mContext, R.string.menu_more_item_import), 0, new MenuViewCallback() {
                public void onClick(MenuItemImpl item) {
                    MoreMenuView.this.importFormFromXML(module);
                }
            });
        }
        if (this.mExportMenuItem == null) {
            this.mExportMenuItem = new MenuItemImpl(this.mContext, 2, AppResource.getString(this.mContext, R.string.menu_more_item_export), 0, new MenuViewCallback() {
                public void onClick(MenuItemImpl item) {
                    MoreMenuView.this.exportFormToXML(module);
                }
            });
        }
        if (this.mResetMenuItem == null) {
            this.mResetMenuItem = new MenuItemImpl(this.mContext, 0, AppResource.getString(this.mContext, R.string.menu_more_item_reset), 0, new MenuViewCallback() {
                public void onClick(MenuItemImpl item) {
                    MoreMenuView.this.resetForm(module);
                }
            });
        }
        this.mMoreMenu.addMenuItem(2, this.mImportMenuItem);
        this.mMoreMenu.addMenuItem(2, this.mExportMenuItem);
        this.mMoreMenu.addMenuItem(2, this.mResetMenuItem);
    }

    protected void reloadFormItems() {
        if (this.mImportMenuItem != null) {
            this.mImportMenuItem.setEnable(false);
        }
        if (this.mExportMenuItem != null) {
            this.mExportMenuItem.setEnable(false);
        }
        if (this.mResetMenuItem != null) {
            this.mResetMenuItem.setEnable(false);
        }
        PDFDoc doc = this.mRead.getDocViewer().getDoc();
        if (doc != null) {
            try {
                if (doc.hasForm()) {
                    if ((doc.getUserPermissions() & 256) == 256) {
                        this.mImportMenuItem.setEnable(true);
                        this.mResetMenuItem.setEnable(true);
                    }
                    this.mExportMenuItem.setEnable(true);
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    private void setMoreMenuView(View view) {
        if (this.mMenuPopupWindow == null) {
            this.mMenuPopupWindow = new PopupWindow(view, -1, -1, true);
        }
        this.mMenuPopupWindow.setBackgroundDrawable(new ColorDrawable(ViewCompat.MEASURED_SIZE_MASK));
        this.mMenuPopupWindow.setAnimationStyle(R.style.View_Animation_RtoL);
        this.mMenuPopupWindow.setOnDismissListener(new OnDismissListener() {
            public void onDismiss() {
            }
        });
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (this.mMenuPopupWindow != null && this.mMenuPopupWindow.isShowing()) {
            updateMoreMenu();
        }
    }

    private void showMoreMenu() {
        this.mRootView = (ViewGroup) this.mParent.getChildAt(0);
        int width = AppDisplay.getInstance(this.mContext).getScreenWidth();
        int height = AppDisplay.getInstance(this.mContext).getScreenHeight();
        if (AppDisplay.getInstance(this.mContext).isPad()) {
            float scale = 0.535f;
            if (width > height) {
                scale = 0.338f;
            }
            width = (int) (((float) AppDisplay.getInstance(this.mContext).getScreenWidth()) * scale);
        }
        this.mMenuPopupWindow.setWidth(width);
        this.mMenuPopupWindow.setHeight(height);
        this.mMenuPopupWindow.setSoftInputMode(1);
        this.mMenuPopupWindow.setSoftInputMode(48);
        this.mMenuPopupWindow.showAtLocation(this.mRootView, 53, 0, 0);
    }

    void updateMoreMenu() {
        int width = App.instance().getDisplay().getScreenWidth();
        int height = App.instance().getDisplay().getScreenHeight();
        if (AppDisplay.getInstance(this.mContext).isPad()) {
            float scale = 0.535f;
            if (width > height) {
                scale = 0.338f;
            }
            width = (int) (((float) App.instance().getDisplay().getScreenWidth()) * scale);
        }
        this.mMenuPopupWindow.update(width, height);
    }

    private void hideMoreMenu() {
        if (this.mMenuPopupWindow.isShowing()) {
            this.mMenuPopupWindow.dismiss();
        }
    }

    private void importFormFromXML(final FormFillerModule module) {
        final UIFileSelectDialog dialog = new UIFileSelectDialog(this.mRead.getMainFrame().getAttachedActivity());
        dialog.init(new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.isHidden() || !pathname.canRead()) {
                    return false;
                }
                if (!pathname.isFile() || pathname.getName().toLowerCase().endsWith(".xml")) {
                    return true;
                }
                return false;
            }
        }, true);
        dialog.showDialog();
        dialog.setTitle(App.instance().getApplicationContext().getResources().getString(R.string.formfiller_import_title));
        dialog.setButton(5);
        dialog.setButtonEnable(false, 4);
        dialog.setListener(new DialogListener() {
            public void onResult(long btType) {
                if (btType == 4) {
                    List<FileItem> files = dialog.getSelectedFiles();
                    dialog.dismiss();
                    MoreMenuView.this.hideMoreMenu();
                    module.importFormFromXML(((FileItem) files.get(0)).path);
                } else if (btType == 1) {
                    dialog.dismiss();
                }
            }

            public void onBackClick() {
            }
        });
    }

    public void exportFormToXML(final FormFillerModule module) {
        hideMoreMenu();
        final UITextEditDialog dialog = new UITextEditDialog(this.mRead.getMainFrame().getAttachedActivity());
        dialog.setTitle(this.mContext.getResources().getString(R.string.formfiller_export_title));
        dialog.getInputEditText().setVisibility(0);
        String fileNameWithoutExt = AppFileUtil.getFileNameWithoutExt(this.mPath);
        dialog.getInputEditText().setText(new StringBuilder(String.valueOf(fileNameWithoutExt)).append(".xml").toString());
        CharSequence text = dialog.getInputEditText().getText();
        if (text instanceof Spannable) {
            Selection.setSelection((Spannable) text, 0, fileNameWithoutExt.length());
        }
        AppUtil.showSoftInput(dialog.getInputEditText());
        dialog.getCancelButton().setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.getOKButton().setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                String name = dialog.getInputEditText().getText().toString();
                if (name.toLowerCase().endsWith(".xml")) {
                    MoreMenuView.this.mExportFilePath = new StringBuilder(String.valueOf(AppFileUtil.getFileFolder(MoreMenuView.this.mPath))).append("/").append(name).toString();
                } else {
                    MoreMenuView.this.mExportFilePath = new StringBuilder(String.valueOf(AppFileUtil.getFileFolder(MoreMenuView.this.mPath))).append("/").append(name).append(".xml").toString();
                }
                if (new File(MoreMenuView.this.mExportFilePath).exists()) {
                    final UITextEditDialog rmDialog = new UITextEditDialog(MoreMenuView.this.mRead.getMainFrame().getAttachedActivity());
                    rmDialog.setTitle(R.string.fm_file_exist);
                    rmDialog.getPromptTextView().setText(R.string.fx_string_filereplace_warning);
                    rmDialog.getInputEditText().setVisibility(8);
                    rmDialog.show();
                    Button oKButton = rmDialog.getOKButton();
                    final FormFillerModule formFillerModule = module;
                    oKButton.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            rmDialog.dismiss();
                            formFillerModule.exportFormToXML(MoreMenuView.this.mExportFilePath);
                        }
                    });
                    oKButton = rmDialog.getCancelButton();
                    formFillerModule = module;
                    oKButton.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            rmDialog.dismiss();
                            MoreMenuView.this.exportFormToXML(formFillerModule);
                        }
                    });
                } else if (!module.exportFormToXML(MoreMenuView.this.mExportFilePath)) {
                    UIToast.getInstance(MoreMenuView.this.mContext).show(MoreMenuView.this.mContext.getResources().getString(R.string.formfiller_export_error));
                }
            }
        });
        dialog.show();
    }

    public void resetForm(FormFillerModule module) {
        module.resetForm();
        hideMoreMenu();
    }

    public void addPasswordItems(final PasswordModule module) {
        this.enItem = new MenuItemImpl(this.mContext, 0, AppResource.getString(this.mContext, R.string.rv_doc_encrpty_standard), 0, new MenuViewCallback() {
            public void onClick(MenuItemImpl item) {
                try {
                    if (MoreMenuView.this.mPdfViewCtrl.getDoc().getEncryptionType() != 0) {
                        MoreMenuView.this.mSwitchDialog = new UITextEditDialog(MoreMenuView.this.mRead.getMainFrame().getAttachedActivity());
                        MoreMenuView.this.mSwitchDialog.getInputEditText().setVisibility(8);
                        MoreMenuView.this.mSwitchDialog.setTitle(MoreMenuView.this.mRead.getMainFrame().getAttachedActivity().getString(R.string.rv_doc_encrypt_standard_switch_title));
                        MoreMenuView.this.mSwitchDialog.getPromptTextView().setText(MoreMenuView.this.mRead.getMainFrame().getAttachedActivity().getString(R.string.rv_doc_encrypt_standard_switch_content));
                        MoreMenuView.this.mSwitchDialog.getCancelButton().setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                MoreMenuView.this.mSwitchDialog.dismiss();
                            }
                        });
                        Button oKButton = MoreMenuView.this.mSwitchDialog.getOKButton();
                        final PasswordModule passwordModule = module;
                        oKButton.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                MoreMenuView.this.mSwitchDialog.dismiss();
                                if (passwordModule.getPasswordSupport() != null) {
                                    if (passwordModule.getPasswordSupport().getFilePath() == null) {
                                        passwordModule.getPasswordSupport().setFilePath(MoreMenuView.this.mPath);
                                    }
                                    passwordModule.getPasswordSupport().passwordManager(11);
                                }
                            }
                        });
                        MoreMenuView.this.mSwitchDialog.show();
                    } else if (module.getPasswordSupport() != null) {
                        if (module.getPasswordSupport().getFilePath() == null) {
                            module.getPasswordSupport().setFilePath(MoreMenuView.this.mPath);
                        }
                        module.getPasswordSupport().passwordManager(11);
                    }
                } catch (PDFException e) {
                    e.printStackTrace();
                }
            }
        });
        this.deItem = new MenuItemImpl(App.instance().getApplicationContext(), 4, AppResource.getString(this.mContext, R.string.menu_more_item_remove_encrytion), 0, new MenuViewCallback() {
            public void onClick(MenuItemImpl item) {
                if (module.getPasswordSupport() != null) {
                    if (module.getPasswordSupport().getFilePath() == null) {
                        module.getPasswordSupport().setFilePath(MoreMenuView.this.mPath);
                    }
                    module.getPasswordSupport().passwordManager(13);
                }
            }
        });
    }

    public void reloadPasswordItem(PasswordModule module) {
        if (this.mMoreMenu.getMenuGroup(1) == null) {
            this.mMoreMenu.addMenuGroup(new MenuGroupImpl(this.mContext, 1, AppResource.getString(this.mContext, R.string.menu_more_group_protect)));
        }
        if (this.mPdfViewCtrl.getDoc() != null) {
            try {
                int encryptType = this.mPdfViewCtrl.getDoc().getEncryptionType();
                if (encryptType == 1) {
                    this.mMoreMenu.removeMenuItem(1, 0);
                    this.mMoreMenu.addMenuItem(1, this.deItem);
                } else if (encryptType != 0) {
                    this.mMoreMenu.removeMenuItem(1, 4);
                    this.mMoreMenu.addMenuItem(1, this.enItem);
                } else {
                    this.mMoreMenu.removeMenuItem(1, 4);
                    this.mMoreMenu.addMenuItem(1, this.enItem);
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
            if (module.getSecurityHandler().isAvailable()) {
                this.enItem.setEnable(true);
                this.deItem.setEnable(true);
                return;
            }
            this.enItem.setEnable(false);
            this.deItem.setEnable(false);
        }
    }
}
