package com.foxit.read;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.foxit.app.App;
import com.foxit.app.utils.AppTheme;
import com.foxit.home.R;
import com.foxit.read.common.RD_Brightness;
import com.foxit.read.common.RD_Reflow;
import com.foxit.read.common.RD_ScreenLock;
import com.foxit.sdk.PDFViewCtrl.IDocEventListener;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.uiextensions.AbstractUndo.IUndoEventListener;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.DocumentManager.AnnotEventListener;
import com.foxit.uiextensions.IUndoItem;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AbstractToolHandler;
import com.foxit.uiextensions.annots.caret.CaretModule;
import com.foxit.uiextensions.annots.caret.CaretToolHandler;
import com.foxit.uiextensions.annots.circle.CircleModule;
import com.foxit.uiextensions.annots.circle.CircleToolHandler;
import com.foxit.uiextensions.annots.fileattachment.FileAttachmentModule;
import com.foxit.uiextensions.annots.fileattachment.FileAttachmentModule.IAttachmentDocEvent;
import com.foxit.uiextensions.annots.fileattachment.FileAttachmentToolHandler;
import com.foxit.uiextensions.annots.freetext.typewriter.TypewriterModule;
import com.foxit.uiextensions.annots.freetext.typewriter.TypewriterToolHandler;
import com.foxit.uiextensions.annots.ink.EraserModule;
import com.foxit.uiextensions.annots.ink.InkModule;
import com.foxit.uiextensions.annots.line.LineModule;
import com.foxit.uiextensions.annots.note.NoteModule;
import com.foxit.uiextensions.annots.note.NoteToolHandler;
import com.foxit.uiextensions.annots.square.SquareModule;
import com.foxit.uiextensions.annots.square.SquareToolHandler;
import com.foxit.uiextensions.annots.stamp.StampModule;
import com.foxit.uiextensions.annots.stamp.StampToolHandler;
import com.foxit.uiextensions.annots.textmarkup.highlight.HighlightModule;
import com.foxit.uiextensions.annots.textmarkup.highlight.HighlightModule.ColorChangeListener;
import com.foxit.uiextensions.annots.textmarkup.highlight.HighlightToolHandler;
import com.foxit.uiextensions.annots.textmarkup.squiggly.SquigglyModule;
import com.foxit.uiextensions.annots.textmarkup.squiggly.SquigglyToolHandler;
import com.foxit.uiextensions.annots.textmarkup.strikeout.StrikeoutModule;
import com.foxit.uiextensions.annots.textmarkup.strikeout.StrikeoutToolHandler;
import com.foxit.uiextensions.annots.textmarkup.underline.UnderlineModule;
import com.foxit.uiextensions.annots.textmarkup.underline.UnderlineToolHandler;
import com.foxit.uiextensions.controls.dialog.UITextEditDialog;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.controls.propertybar.imp.PropertyBarImpl;
import com.foxit.uiextensions.controls.toolbar.BaseBar.TB_Position;
import com.foxit.uiextensions.controls.toolbar.BaseItem;
import com.foxit.uiextensions.controls.toolbar.CircleItem;
import com.foxit.uiextensions.controls.toolbar.PropertyCircleItem;
import com.foxit.uiextensions.controls.toolbar.ToolbarItemConfig;
import com.foxit.uiextensions.controls.toolbar.impl.BaseItemImpl;
import com.foxit.uiextensions.controls.toolbar.impl.CircleItemImpl;
import com.foxit.uiextensions.controls.toolbar.impl.PropertyCircleItemImp;
import com.foxit.uiextensions.modules.AnnotPanelModule;
import com.foxit.uiextensions.modules.DocInfoModule;
import com.foxit.uiextensions.modules.OutlineModule;
import com.foxit.uiextensions.modules.PageNavigationModule;
import com.foxit.uiextensions.modules.ReadingBookmarkModule;
import com.foxit.uiextensions.modules.SearchModule;
import com.foxit.uiextensions.modules.SearchView;
import com.foxit.uiextensions.modules.SearchView.SearchCancelListener;
import com.foxit.uiextensions.modules.ThumbnailModule;
import com.foxit.uiextensions.modules.UndoModule;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.AppFileUtil;
import com.foxit.uiextensions.utils.AppResource;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.UIToast;
import com.foxit.view.propertybar.IMT_MoreTools.IMT_MoreClickListener;
import io.vov.vitamio.MediaMetadataRetriever;
import io.vov.vitamio.provider.MediaStore.MediaColumns;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class RD_ReadActivity extends FragmentActivity {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$foxit$sdk$common$PDFError = null;
    private static final String TAG = "ReadActivity";
    private static RelativeLayout mActivityLayout;
    final IDocEventListener docEventListener = new IDocEventListener() {
        private static /* synthetic */ int[] $SWITCH_TABLE$com$foxit$sdk$common$PDFError;

        static /* synthetic */ int[] $SWITCH_TABLE$com$foxit$sdk$common$PDFError() {
            int[] iArr = $SWITCH_TABLE$com$foxit$sdk$common$PDFError;
            if (iArr == null) {
                iArr = new int[PDFError.values().length];
                try {
                    iArr[PDFError.CERTIFICATE_ERROR.ordinal()] = 6;
                } catch (NoSuchFieldError e) {
                }
                try {
                    iArr[PDFError.DATA_CONFLICT.ordinal()] = 16;
                } catch (NoSuchFieldError e2) {
                }
                try {
                    iArr[PDFError.DATA_NOT_FOUND.ordinal()] = 14;
                } catch (NoSuchFieldError e3) {
                }
                try {
                    iArr[PDFError.FILE_ERROR.ordinal()] = 2;
                } catch (NoSuchFieldError e4) {
                }
                try {
                    iArr[PDFError.FORMAT_ERROR.ordinal()] = 3;
                } catch (NoSuchFieldError e5) {
                }
                try {
                    iArr[PDFError.HANDLER_ERROR.ordinal()] = 5;
                } catch (NoSuchFieldError e6) {
                }
                try {
                    iArr[PDFError.INVALID_OBJECT_TYPE.ordinal()] = 15;
                } catch (NoSuchFieldError e7) {
                }
                try {
                    iArr[PDFError.LICENSE_INVALID.ordinal()] = 8;
                } catch (NoSuchFieldError e8) {
                }
                try {
                    iArr[PDFError.NOT_PARSED_ERROR.ordinal()] = 13;
                } catch (NoSuchFieldError e9) {
                }
                try {
                    iArr[PDFError.NO_ERROR.ordinal()] = 1;
                } catch (NoSuchFieldError e10) {
                }
                try {
                    iArr[PDFError.OOM.ordinal()] = 11;
                } catch (NoSuchFieldError e11) {
                }
                try {
                    iArr[PDFError.PARAM_INVALID.ordinal()] = 9;
                } catch (NoSuchFieldError e12) {
                }
                try {
                    iArr[PDFError.PASSWORD_INVALID.ordinal()] = 4;
                } catch (NoSuchFieldError e13) {
                }
                try {
                    iArr[PDFError.SECURITY_HANDLE_ERROR.ordinal()] = 12;
                } catch (NoSuchFieldError e14) {
                }
                try {
                    iArr[PDFError.UNKNOWN_ERROR.ordinal()] = 7;
                } catch (NoSuchFieldError e15) {
                }
                try {
                    iArr[PDFError.UNSUPPORTED.ordinal()] = 10;
                } catch (NoSuchFieldError e16) {
                }
                $SWITCH_TABLE$com$foxit$sdk$common$PDFError = iArr;
            }
            return iArr;
        }

        public void onDocWillOpen() {
        }

        public void onDocOpened(PDFDoc pdfDoc, int errCode) {
            switch (AnonymousClass1.$SWITCH_TABLE$com$foxit$sdk$common$PDFError()[PDFError.valueOf(Integer.valueOf(errCode)).ordinal()]) {
                case 1:
                    RD_ReadActivity.this.mRead.bDocClosed = false;
                    RD_ReadActivity.this.mPasswordError = false;
                    RD_ReadActivity.this.mRead.changeState(1);
                    return;
                case 4:
                    String tips;
                    if (RD_ReadActivity.this.mPasswordError) {
                        tips = AppResource.getString(RD_ReadActivity.this, R.string.rv_tips_password_error);
                    } else {
                        tips = AppResource.getString(RD_ReadActivity.this, R.string.rv_tips_password);
                    }
                    final UITextEditDialog uiTextEditDialog = new UITextEditDialog(RD_ReadActivity.this);
                    uiTextEditDialog.getDialog().setCanceledOnTouchOutside(false);
                    uiTextEditDialog.getInputEditText().setInputType(129);
                    uiTextEditDialog.setTitle(R.string.fx_string_passwordDialog_title);
                    uiTextEditDialog.getPromptTextView().setText(tips);
                    uiTextEditDialog.show();
                    uiTextEditDialog.getOKButton().setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            uiTextEditDialog.dismiss();
                            ((InputMethodManager) v.getContext().getSystemService("input_method")).hideSoftInputFromWindow(v.getWindowToken(), 0);
                            RD_ReadActivity.this.mRead.getDocViewer().openDoc(RD_ReadActivity.this.mFilePath, uiTextEditDialog.getInputEditText().getText().toString().getBytes());
                        }
                    });
                    uiTextEditDialog.getCancelButton().setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            uiTextEditDialog.dismiss();
                            ((InputMethodManager) v.getContext().getSystemService("input_method")).hideSoftInputFromWindow(v.getWindowToken(), 0);
                            RD_ReadActivity.this.mPasswordError = false;
                            RD_ReadActivity.this.mRead.bDocClosed = true;
                            RD_ReadActivity.this.mRead.openDocumentFailed();
                        }
                    });
                    uiTextEditDialog.getDialog().setOnKeyListener(new OnKeyListener() {
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode != 4) {
                                return false;
                            }
                            uiTextEditDialog.getDialog().cancel();
                            RD_ReadActivity.this.mPasswordError = false;
                            RD_ReadActivity.this.mRead.bDocClosed = true;
                            RD_ReadActivity.this.mRead.openDocumentFailed();
                            return true;
                        }
                    });
                    if (!RD_ReadActivity.this.mPasswordError) {
                        RD_ReadActivity.this.mPasswordError = true;
                        return;
                    }
                    return;
                default:
                    RD_ReadActivity.this.mRead.bDocClosed = true;
                    UIToast.getInstance(RD_ReadActivity.this.getApplicationContext()).show("ReadActivity-onDocOpened:" + PDFException.getErrorMessage(errCode));
                    RD_ReadActivity.this.mRead.openDocumentFailed();
                    return;
            }
        }

        public void onDocWillClose(PDFDoc pdfDoc) {
            RD_ReadActivity.this.mRead._resetStatusAfterClose();
        }

        public void onDocClosed(PDFDoc pdfDoc, int i) {
            RD_ReadActivity.this.mRead.bDocClosed = true;
            RD_ReadActivity.this.mRead.closeDocumentSucceed();
        }

        public void onDocWillSave(PDFDoc pdfDoc) {
        }

        public void onDocSaved(PDFDoc pdfDoc, int i) {
        }
    };
    BaseItem mBookmarkAddButton;
    final Intent mBroadcastIntent = new Intent();
    private BaseItem mCircleItem;
    private BaseItem mContinuousCreateItem;
    private BaseItem mFileAttachmentItem;
    private String mFilePath = null;
    private BaseItem mHighlightItem;
    private int mHistoryState;
    BaseItem mInkItem;
    private BaseItem mInsertTextItem;
    private boolean mIsContinuousCreate = false;
    BaseItem mLineItem;
    private BaseItem mMoreItem;
    private BaseItem mNoteItem;
    private BaseItem mOKItem;
    private boolean mPasswordError = false;
    private PropertyBar mPropertyBar;
    private PropertyCircleItem mPropertyItem;
    private RD_Read mRead;
    private CircleItem mRedoButton;
    private BaseItem mReplaceItem;
    BaseItem mSearchButtonItem;
    private BaseItem mStrikeoutItem;
    private BaseItem mTypewriterItem;
    private CircleItem mUndoButton;

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    static /* synthetic */ int[] $SWITCH_TABLE$com$foxit$sdk$common$PDFError() {
        int[] iArr = $SWITCH_TABLE$com$foxit$sdk$common$PDFError;
        if (iArr == null) {
            iArr = new int[PDFError.values().length];
            try {
                iArr[PDFError.CERTIFICATE_ERROR.ordinal()] = 6;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[PDFError.DATA_CONFLICT.ordinal()] = 16;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[PDFError.DATA_NOT_FOUND.ordinal()] = 14;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[PDFError.FILE_ERROR.ordinal()] = 2;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[PDFError.FORMAT_ERROR.ordinal()] = 3;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[PDFError.HANDLER_ERROR.ordinal()] = 5;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[PDFError.INVALID_OBJECT_TYPE.ordinal()] = 15;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[PDFError.LICENSE_INVALID.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[PDFError.NOT_PARSED_ERROR.ordinal()] = 13;
            } catch (NoSuchFieldError e9) {
            }
            try {
                iArr[PDFError.NO_ERROR.ordinal()] = 1;
            } catch (NoSuchFieldError e10) {
            }
            try {
                iArr[PDFError.OOM.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                iArr[PDFError.PARAM_INVALID.ordinal()] = 9;
            } catch (NoSuchFieldError e12) {
            }
            try {
                iArr[PDFError.PASSWORD_INVALID.ordinal()] = 4;
            } catch (NoSuchFieldError e13) {
            }
            try {
                iArr[PDFError.SECURITY_HANDLE_ERROR.ordinal()] = 12;
            } catch (NoSuchFieldError e14) {
            }
            try {
                iArr[PDFError.UNKNOWN_ERROR.ordinal()] = 7;
            } catch (NoSuchFieldError e15) {
            }
            try {
                iArr[PDFError.UNSUPPORTED.ordinal()] = 10;
            } catch (NoSuchFieldError e16) {
            }
            $SWITCH_TABLE$com$foxit$sdk$common$PDFError = iArr;
        }
        return iArr;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkLicense()) {
            AppTheme.setThemeFullScreen(this);
            AppTheme.setThemeNeedMenuKey(this);
            this.mRead = new RD_Read(this);
            this.mRead.init();
            if (mActivityLayout == null) {
                mActivityLayout = new RelativeLayout(this);
            } else {
                mActivityLayout.removeAllViews();
                mActivityLayout = new RelativeLayout(this);
            }
            mActivityLayout.setId(R.id.rd_main_id);
            mActivityLayout.addView(this.mRead.getMainFrame().getContentView(), new LayoutParams(-1, -1));
            setContentView(mActivityLayout);
            setDocumentPath(getIntent());
            addToolListener();
            registerOtherModule();
            App.instance().loadModules();
            this.mRead.onCreate(this, savedInstanceState);
            openDocument(null);
        }
    }

    private boolean checkLicense() {
        switch ($SWITCH_TABLE$com$foxit$sdk$common$PDFError()[PDFError.valueOf(Integer.valueOf(App.getLicenseErrCode())).ordinal()]) {
            case 1:
                return true;
            case 8:
                UIToast.getInstance(this).show((CharSequence) "The License is invalid!");
                return false;
            default:
                UIToast.getInstance(this).show((CharSequence) "Failed to initialize the library!");
                return false;
        }
    }

    void addToolListener() {
        addSquigglyListener((SquigglyModule) ((UIExtensionsManager) this.mRead.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_SQUIGGLY));
        addStrikeoutListener((StrikeoutModule) ((UIExtensionsManager) this.mRead.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_STRIKEOUT));
        addUnderlineListener((UnderlineModule) ((UIExtensionsManager) this.mRead.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_UNDERLINE));
        addHighlightListener((HighlightModule) ((UIExtensionsManager) this.mRead.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_HIGHLIGHT));
        addNoteListener((NoteModule) ((UIExtensionsManager) this.mRead.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_NOTE));
        addCircleListener((CircleModule) ((UIExtensionsManager) this.mRead.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_CIRCLE));
        addSquareListener((SquareModule) ((UIExtensionsManager) this.mRead.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_SQUARE));
        addTypewriterListener((TypewriterModule) ((UIExtensionsManager) this.mRead.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_TYPEWRITER));
        addStampListener((StampModule) ((UIExtensionsManager) this.mRead.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_STAMP));
        Module module = ((UIExtensionsManager) this.mRead.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_CARET);
        addInsertTextListener((CaretModule) module);
        addReplaceListener((CaretModule) module);
        addInkListener((InkModule) ((UIExtensionsManager) this.mRead.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_INK));
        addEraserListener((EraserModule) ((UIExtensionsManager) this.mRead.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_ERASER));
        module = ((UIExtensionsManager) this.mRead.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_LINE);
        addLineListener((LineModule) module, 14);
        addLineListener((LineModule) module, 15);
        module = ((UIExtensionsManager) this.mRead.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_FILEATTACHMENT);
        if (module != null) {
            addFileAttachmentListener((FileAttachmentModule) module);
            ((FileAttachmentModule) module).registerAttachmentDocEventListener(new IAttachmentDocEvent() {
                public void onAttachmentDocWillOpen() {
                    RD_ReadActivity.this.mRead.getMainFrame().hidePanel();
                }

                public void onAttachmentDocOpened(PDFDoc document, int errCode) {
                    if (errCode == PDFError.NO_ERROR.getCode()) {
                        RD_ReadActivity.this.mHistoryState = RD_ReadActivity.this.mRead.getState();
                        RD_ReadActivity.this.mRead.changeState(1);
                        PageNavigationModule module = (PageNavigationModule) ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_PAGENAV);
                        if (module != null) {
                            module.changPageNumberState(false);
                        }
                    }
                }

                public void onAttachmentDocWillClose() {
                    RD_ReadActivity.this.mRead.changeState(RD_ReadActivity.this.mHistoryState);
                }

                public void onAttachmentDocClosed() {
                }
            });
        }
        addUndo((UndoModule) ((UIExtensionsManager) this.mRead.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_UNDO));
        DocumentManager.getInstance(this.mRead.getDocViewer()).registerAnnotEventListener(new AnnotEventListener() {
            public void onAnnotAdded(PDFPage page, Annot annot) {
                if (RD_ReadActivity.this.mRead.getState() == 6 && !RD_ReadActivity.this.mIsContinuousCreate) {
                    RD_ReadActivity.this.mRead.changeState(4);
                    RD_ReadActivity.this.mRead.getMainFrame().showToolbars();
                }
            }

            public void onAnnotDeleted(PDFPage page, Annot annot) {
            }

            public void onAnnotModified(PDFPage page, Annot annot) {
            }

            public void onAnnotChanged(Annot lastAnnot, Annot currentAnnot) {
            }
        });
    }

    void registerOtherModule() {
        ReadingBookmarkModule readingBookmarkModule = new ReadingBookmarkModule(this, this.mRead.getMainFrame().getContentView(), this.mRead.getDocViewer());
        readingBookmarkModule.setPanelHost(this.mRead.getMainFrame().getPanel());
        readingBookmarkModule.setPopupWindow(((RD_MainFrame) this.mRead.getMainFrame()).getPanelWindow());
        App.instance().registerModule(readingBookmarkModule);
        addBookmarkListener(readingBookmarkModule);
        OutlineModule outlineModule = new OutlineModule(this, this.mRead.getMainFrame().getContentView(), this.mRead.getDocViewer());
        outlineModule.setPanelHost(this.mRead.getMainFrame().getPanel());
        outlineModule.setPopupWindow(((RD_MainFrame) this.mRead.getMainFrame()).getPanelWindow());
        App.instance().registerModule(outlineModule);
        AnnotPanelModule annotPanelModule = new AnnotPanelModule(this, this.mRead.getMainFrame().getContentView(), this.mRead.getDocViewer());
        annotPanelModule.setPanelHost(this.mRead.getMainFrame().getPanel());
        annotPanelModule.setPopupWindow(((RD_MainFrame) this.mRead.getMainFrame()).getPanelWindow());
        App.instance().registerModule(annotPanelModule);
        ((UIExtensionsManager) this.mRead.getUIExtensionsManager()).registerModule(annotPanelModule);
        App.instance().registerModule(new SearchModule(this, this.mRead.getMainFrame().getContentView(), this.mRead.getDocViewer()));
        addSearchListener();
        DocInfoModule docInfoModule = new DocInfoModule(this, this.mRead.getMainFrame().getContentView(), this.mRead.getDocViewer(), this.mFilePath);
        docInfoModule.loadModule();
        ((UIExtensionsManager) this.mRead.getUIExtensionsManager()).registerModule(docInfoModule);
        App.instance().registerModule(new ThumbnailModule(this, this.mRead.getDocViewer()));
        App.instance().registerModule(new RD_Brightness(this, this.mRead));
        App.instance().registerModule(new RD_ScreenLock(this));
        App.instance().registerModule(new RD_Reflow(this, this.mRead));
    }

    private void addSearchListener() {
        this.mSearchButtonItem = new BaseItemImpl(this);
        this.mSearchButtonItem.setTag(ToolbarItemConfig.ITEM_SEARCH_TAG);
        this.mSearchButtonItem.setImageResource(R.drawable.rd_search_selector);
        this.mSearchButtonItem.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!AppUtil.isFastDoubleClick()) {
                    App.instance().getEventManager().onTriggerDismissMenu();
                    RD_ReadActivity.this.mRead.getMainFrame().hideToolbars();
                    RD_ReadActivity.this.mRead.changeState(3);
                    SearchView searchView = ((SearchModule) App.instance().getModuleByName(Module.MODULE_NAME_SEARCH)).getSearchView();
                    searchView.setSearchCancelListener(new SearchCancelListener() {
                        public void onSearchCancel() {
                            RD_ReadActivity.this.mRead.changeState(1);
                            RD_ReadActivity.this.mRead.getMainFrame().showToolbars();
                        }
                    });
                    searchView.launchSearchView();
                    searchView.show();
                }
            }
        });
        this.mRead.getMainFrame().getTopToolbar().addView(this.mSearchButtonItem, TB_Position.Position_RB);
    }

    private void addBookmarkListener(ReadingBookmarkModule module) {
        if (module != null) {
            this.mBookmarkAddButton = new BaseItemImpl(this);
            this.mBookmarkAddButton.setTag(ToolbarItemConfig.ITEM_READINGMARK_TAG);
            this.mBookmarkAddButton.setImageResource(R.drawable.rd_readingmark_add_selector);
            module.addMarkedItem(this.mBookmarkAddButton);
            this.mRead.getMainFrame().getTopToolbar().addView(this.mBookmarkAddButton, TB_Position.Position_RB);
        }
    }

    private void resetAnnotBarToTextMarkup(final Module module) {
        if (module != null) {
            this.mRead.getMainFrame().getToolSetBar().removeAllItems();
            this.mPropertyBar = this.mRead.getMainFrame().getPropertyBar();
            int color = 0;
            ToolHandler toolHandler;
            int[] mPBColors;
            if (module instanceof HighlightModule) {
                toolHandler = ((HighlightModule) module).getToolHandler();
                ((HighlightToolHandler) toolHandler).setIsContinuousCreate(false);
                mPBColors = new int[PropertyBar.PB_COLORS_HIGHLIGHT.length];
                System.arraycopy(PropertyBar.PB_COLORS_HIGHLIGHT, 0, mPBColors, 0, mPBColors.length);
                mPBColors[0] = PropertyBar.PB_COLORS_HIGHLIGHT[0];
                this.mPropertyBar.setColors(mPBColors);
                this.mPropertyBar.setProperty(1, ((HighlightToolHandler) toolHandler).getColor());
                this.mPropertyBar.setProperty(2, AppDmUtil.opacity255To100(((HighlightToolHandler) toolHandler).getOpacity()));
                this.mPropertyBar.reset(3);
                this.mPropertyBar.setPropertyChangeListener((HighlightModule) module);
                color = ((HighlightToolHandler) toolHandler).getColor();
                ((HighlightModule) module).setColorChangeListener(new ColorChangeListener() {
                    public void onColorChange(int color) {
                        RD_ReadActivity.this.mPropertyItem.setCentreCircleColor(color);
                    }
                });
            } else if (module instanceof UnderlineModule) {
                toolHandler = ((UnderlineModule) module).getToolHandler();
                ((UnderlineToolHandler) toolHandler).setIsContinuousCreate(false);
                mPBColors = new int[PropertyBar.PB_COLORS_UNDERLINE.length];
                System.arraycopy(PropertyBar.PB_COLORS_UNDERLINE, 0, mPBColors, 0, mPBColors.length);
                mPBColors[0] = PropertyBar.PB_COLORS_UNDERLINE[0];
                this.mPropertyBar.setColors(mPBColors);
                this.mPropertyBar.setProperty(1, ((UnderlineToolHandler) toolHandler).getColor());
                this.mPropertyBar.setProperty(2, AppDmUtil.opacity255To100(((UnderlineToolHandler) toolHandler).getOpacity()));
                this.mPropertyBar.reset(3);
                this.mPropertyBar.setPropertyChangeListener((UnderlineModule) module);
                color = ((UnderlineToolHandler) toolHandler).getColor();
                ((UnderlineModule) module).setColorChangeListener(new UnderlineModule.ColorChangeListener() {
                    public void onColorChange(int color) {
                        RD_ReadActivity.this.mPropertyItem.setCentreCircleColor(color);
                    }
                });
            } else if (module instanceof StrikeoutModule) {
                toolHandler = ((StrikeoutModule) module).getToolHandler();
                ((StrikeoutToolHandler) toolHandler).setIsContinuousCreate(false);
                mPBColors = new int[PropertyBar.PB_COLORS_STRIKEOUT.length];
                System.arraycopy(PropertyBar.PB_COLORS_STRIKEOUT, 0, mPBColors, 0, mPBColors.length);
                mPBColors[0] = PropertyBar.PB_COLORS_STRIKEOUT[0];
                this.mPropertyBar.setColors(mPBColors);
                this.mPropertyBar.setProperty(1, ((StrikeoutToolHandler) toolHandler).getColor());
                this.mPropertyBar.setProperty(2, AppDmUtil.opacity255To100(((StrikeoutToolHandler) toolHandler).getOpacity()));
                this.mPropertyBar.reset(3);
                this.mPropertyBar.setPropertyChangeListener((StrikeoutModule) module);
                color = ((StrikeoutToolHandler) toolHandler).getColor();
                ((StrikeoutModule) module).setColorChangeListener(new StrikeoutModule.ColorChangeListener() {
                    public void onColorChange(int color) {
                        RD_ReadActivity.this.mPropertyItem.setCentreCircleColor(color);
                    }
                });
            } else if (module instanceof SquigglyModule) {
                toolHandler = ((SquigglyModule) module).getToolHandler();
                ((SquigglyToolHandler) toolHandler).setIsContinuousCreate(false);
                mPBColors = new int[PropertyBar.PB_COLORS_SQUIGGLY.length];
                System.arraycopy(PropertyBar.PB_COLORS_SQUIGGLY, 0, mPBColors, 0, mPBColors.length);
                mPBColors[0] = PropertyBar.PB_COLORS_SQUIGGLY[0];
                this.mPropertyBar.setColors(mPBColors);
                this.mPropertyBar.setProperty(1, ((SquigglyToolHandler) toolHandler).getColor());
                this.mPropertyBar.setProperty(2, AppDmUtil.opacity255To100(((SquigglyToolHandler) toolHandler).getOpacity()));
                this.mPropertyBar.reset(3);
                this.mPropertyBar.setPropertyChangeListener((SquigglyModule) module);
                color = ((SquigglyToolHandler) toolHandler).getColor();
                ((SquigglyModule) module).setColorChangeListener(new SquigglyModule.ColorChangeListener() {
                    public void onColorChange(int color) {
                        RD_ReadActivity.this.mPropertyItem.setCentreCircleColor(color);
                    }
                });
            }
            this.mMoreItem = new CircleItemImpl(App.instance().getApplicationContext()) {
                public void onItemLayout(int l, int t, int r, int b) {
                    ToolHandler toolHandler = null;
                    if (module instanceof HighlightModule) {
                        toolHandler = ((HighlightModule) module).getToolHandler();
                    } else if (module instanceof UnderlineModule) {
                        toolHandler = ((UnderlineModule) module).getToolHandler();
                    } else if (module instanceof StrikeoutModule) {
                        toolHandler = ((StrikeoutModule) module).getToolHandler();
                    } else if (module instanceof SquigglyModule) {
                        toolHandler = ((SquigglyModule) module).getToolHandler();
                    }
                    if (((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).getCurrentToolHandler() == toolHandler) {
                        Rect rect = new Rect();
                        RD_ReadActivity.this.mMoreItem.getContentView().getGlobalVisibleRect(rect);
                        RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().update(new RectF(rect));
                    }
                }
            };
            this.mMoreItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_MORE);
            this.mMoreItem.setImageResource(R.drawable.mt_more_selector);
            this.mMoreItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Rect rect = new Rect();
                    RD_ReadActivity.this.mMoreItem.getContentView().getGlobalVisibleRect(rect);
                    RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().show(new RectF(rect), true);
                }
            });
            this.mOKItem = new CircleItemImpl(App.instance().getApplicationContext());
            this.mOKItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_OK);
            this.mOKItem.setImageResource(R.drawable.rd_annot_create_ok_selector);
            this.mOKItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    RD_ReadActivity.this.mRead.changeState(4);
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(null);
                }
            });
            this.mPropertyItem = new PropertyCircleItemImp(App.instance().getApplicationContext()) {
                public void onItemLayout(int l, int t, int r, int b) {
                    ToolHandler toolHandler = null;
                    if (module instanceof HighlightModule) {
                        toolHandler = ((HighlightModule) module).getToolHandler();
                    } else if (module instanceof UnderlineModule) {
                        toolHandler = ((UnderlineModule) module).getToolHandler();
                    } else if (module instanceof StrikeoutModule) {
                        toolHandler = ((StrikeoutModule) module).getToolHandler();
                    } else if (module instanceof SquigglyModule) {
                        toolHandler = ((SquigglyModule) module).getToolHandler();
                    }
                    if (((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).getCurrentToolHandler() == toolHandler && RD_ReadActivity.this.mPropertyBar.isShowing()) {
                        Rect rect = new Rect();
                        RD_ReadActivity.this.mPropertyItem.getContentView().getGlobalVisibleRect(rect);
                        RD_ReadActivity.this.mPropertyBar.update(new RectF(rect));
                    }
                }
            };
            this.mPropertyItem.setTag(ToolbarItemConfig.ITEM_PROPERTY_TAG);
            this.mPropertyItem.setCentreCircleColor(color);
            final Rect rect = new Rect();
            this.mPropertyItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    RD_ReadActivity.this.mRead.getMainFrame().getPropertyBar().setArrowVisible(true);
                    RD_ReadActivity.this.mPropertyItem.getContentView().getGlobalVisibleRect(rect);
                    RD_ReadActivity.this.mRead.getMainFrame().getPropertyBar().show(new RectF(rect), true);
                }
            });
            this.mContinuousCreateItem = new CircleItemImpl(App.instance().getApplicationContext());
            this.mContinuousCreateItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_CONTINUE);
            this.mIsContinuousCreate = false;
            this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_false_selector);
            this.mContinuousCreateItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!AppUtil.isFastDoubleClick()) {
                        boolean z;
                        if (module instanceof HighlightModule) {
                            HighlightToolHandler hltToolHandler = (HighlightToolHandler) ((HighlightModule) module).getToolHandler();
                            if (hltToolHandler.getIsContinuousCreate()) {
                                hltToolHandler.setIsContinuousCreate(false);
                                RD_ReadActivity.this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_false_selector);
                            } else {
                                hltToolHandler.setIsContinuousCreate(true);
                                RD_ReadActivity.this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_true_selector);
                            }
                        } else if (module instanceof UnderlineModule) {
                            UnderlineToolHandler unlToolHandler = (UnderlineToolHandler) ((UnderlineModule) module).getToolHandler();
                            if (unlToolHandler.getIsContinuousCreate()) {
                                unlToolHandler.setIsContinuousCreate(false);
                                RD_ReadActivity.this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_false_selector);
                            } else {
                                unlToolHandler.setIsContinuousCreate(true);
                                RD_ReadActivity.this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_true_selector);
                            }
                        } else if (module instanceof StrikeoutModule) {
                            StrikeoutToolHandler stoToolHandler = (StrikeoutToolHandler) ((StrikeoutModule) module).getToolHandler();
                            if (stoToolHandler.getIsContinuousCreate()) {
                                stoToolHandler.setIsContinuousCreate(false);
                                RD_ReadActivity.this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_false_selector);
                            } else {
                                stoToolHandler.setIsContinuousCreate(true);
                                RD_ReadActivity.this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_true_selector);
                            }
                        } else if (module instanceof SquigglyModule) {
                            SquigglyToolHandler sqgToolHandler = (SquigglyToolHandler) ((SquigglyModule) module).getToolHandler();
                            if (sqgToolHandler.getIsContinuousCreate()) {
                                sqgToolHandler.setIsContinuousCreate(false);
                                RD_ReadActivity.this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_false_selector);
                            } else {
                                sqgToolHandler.setIsContinuousCreate(true);
                                RD_ReadActivity.this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_true_selector);
                            }
                        }
                        RD_ReadActivity rD_ReadActivity = RD_ReadActivity.this;
                        if (RD_ReadActivity.this.mIsContinuousCreate) {
                            z = false;
                        } else {
                            z = true;
                        }
                        rD_ReadActivity.mIsContinuousCreate = z;
                        AppAnnotUtil.getInstance(RD_ReadActivity.this.getApplicationContext()).showAnnotContinueCreateToast(RD_ReadActivity.this.mIsContinuousCreate);
                    }
                }
            });
            this.mRead.getMainFrame().getToolSetBar().addView(this.mMoreItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getToolSetBar().addView(this.mPropertyItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getToolSetBar().addView(this.mOKItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getToolSetBar().addView(this.mContinuousCreateItem, TB_Position.Position_CENTER);
        }
    }

    private void addHighlightListener(final HighlightModule module) {
        if (module != null) {
            this.mHighlightItem = new CircleItemImpl(App.instance().getApplicationContext());
            this.mHighlightItem.setImageResource(R.drawable.annot_highlight_selector);
            this.mHighlightItem.setTag(ToolbarItemConfig.ITEM_HIGHLIGHT_TAG);
            this.mRead.getMainFrame().getEditBar().addView(this.mHighlightItem, TB_Position.Position_CENTER);
            this.mHighlightItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    HighlightToolHandler toolHandler = (HighlightToolHandler) module.getToolHandler();
                    toolHandler.getTextSelector().clear();
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(toolHandler);
                    RD_ReadActivity.this.mRead.changeState(6);
                    RD_ReadActivity.this.resetAnnotBarToTextMarkup(module);
                }
            });
            this.mHighlightItem.setEnable(true);
            this.mRead.getMainFrame().getMoreToolsBar().registerListener(new IMT_MoreClickListener() {
                public void onMTClick(int type) {
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(module.getToolHandler());
                    RD_ReadActivity.this.mRead.changeState(6);
                    RD_ReadActivity.this.resetAnnotBarToTextMarkup(module);
                }

                public int getType() {
                    return 1;
                }
            });
        }
    }

    private void addSquigglyListener(final SquigglyModule module) {
        if (module != null) {
            this.mRead.getMainFrame().getMoreToolsBar().registerListener(new IMT_MoreClickListener() {
                public void onMTClick(int type) {
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(module.getToolHandler());
                    RD_ReadActivity.this.mRead.changeState(6);
                    RD_ReadActivity.this.resetAnnotBarToTextMarkup(module);
                }

                public int getType() {
                    return 4;
                }
            });
        }
    }

    private void addUnderlineListener(final UnderlineModule module) {
        if (module != null) {
            this.mRead.getMainFrame().getMoreToolsBar().registerListener(new IMT_MoreClickListener() {
                public void onMTClick(int type) {
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(module.getToolHandler());
                    RD_ReadActivity.this.mRead.changeState(6);
                    RD_ReadActivity.this.resetAnnotBarToTextMarkup(module);
                }

                public int getType() {
                    return 5;
                }
            });
        }
    }

    private void addStrikeoutListener(final StrikeoutModule module) {
        if (module != null) {
            this.mStrikeoutItem = new CircleItemImpl(App.instance().getApplicationContext());
            this.mStrikeoutItem.setImageResource(R.drawable.annot_sto_selector);
            this.mStrikeoutItem.setTag(ToolbarItemConfig.ANNOTS_BAR_ITEM_STO_TAG);
            this.mRead.getMainFrame().getEditBar().addView(this.mStrikeoutItem, TB_Position.Position_CENTER);
            this.mStrikeoutItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    StrikeoutToolHandler toolHandler = (StrikeoutToolHandler) module.getToolHandler();
                    toolHandler.resetLineData();
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(toolHandler);
                    RD_ReadActivity.this.mRead.changeState(6);
                    RD_ReadActivity.this.resetAnnotBarToTextMarkup(module);
                }
            });
            this.mRead.getMainFrame().getMoreToolsBar().registerListener(new IMT_MoreClickListener() {
                public void onMTClick(int type) {
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(module.getToolHandler());
                    RD_ReadActivity.this.mRead.changeState(6);
                    RD_ReadActivity.this.resetAnnotBarToTextMarkup(module);
                }

                public int getType() {
                    return 3;
                }
            });
        }
    }

    public void resetAnnotBarToNote(final NoteModule module) {
        if (module != null) {
            this.mRead.getMainFrame().getToolSetBar().removeAllItems();
            this.mPropertyBar = this.mRead.getMainFrame().getPropertyBar();
            this.mPropertyBar.setPropertyChangeListener(module);
            final NoteToolHandler toolHandler = (NoteToolHandler) module.getToolHandler();
            this.mPropertyItem = new PropertyCircleItemImp(this) {
                public void onItemLayout(int l, int t, int r, int b) {
                    super.onItemLayout(l, t, r, b);
                    if (((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).getCurrentToolHandler() == toolHandler && RD_ReadActivity.this.mPropertyBar.isShowing()) {
                        Rect rect = new Rect();
                        RD_ReadActivity.this.mPropertyItem.getContentView().getGlobalVisibleRect(rect);
                        RD_ReadActivity.this.mPropertyBar.update(new RectF(rect));
                    }
                }
            };
            int[] colors = new int[PropertyBar.PB_COLORS_TEXT.length];
            System.arraycopy(PropertyBar.PB_COLORS_TEXT, 0, colors, 0, colors.length);
            colors[0] = PropertyBar.PB_COLORS_TEXT[0];
            this.mPropertyBar.setColors(colors);
            this.mPropertyBar.setProperty(1, toolHandler.getColor());
            this.mPropertyBar.setProperty(2, toolHandler.getOpacity());
            this.mPropertyBar.setProperty(64, toolHandler.getIconType());
            this.mPropertyBar.setPropertyChangeListener(module);
            this.mPropertyItem.setTag(ToolbarItemConfig.ITEM_PROPERTY_TAG);
            this.mPropertyItem.setCentreCircleColor(toolHandler.getColor());
            this.mPropertyItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    RD_ReadActivity.this.mPropertyBar.reset(67);
                    RD_ReadActivity.this.mPropertyBar.setArrowVisible(true);
                    Rect rect = new Rect();
                    RD_ReadActivity.this.mPropertyItem.getContentView().getGlobalVisibleRect(rect);
                    RD_ReadActivity.this.mPropertyBar.show(new RectF(rect), true);
                }
            });
            module.setColorChangeListener(new NoteModule.ColorChangeListener() {
                public void onColorChange(int color) {
                    RD_ReadActivity.this.mPropertyItem.setCentreCircleColor(color);
                }
            });
            this.mMoreItem = new CircleItemImpl(this) {
                public void onItemLayout(int l, int t, int r, int b) {
                    super.onItemLayout(l, t, r, b);
                    if (((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).getCurrentToolHandler() == module.getToolHandler() && RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().isShowing()) {
                        Rect rect = new Rect();
                        RD_ReadActivity.this.mMoreItem.getContentView().getGlobalVisibleRect(rect);
                        RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().update(new RectF(rect));
                    }
                }
            };
            this.mMoreItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_MORE);
            this.mMoreItem.setImageResource(R.drawable.mt_more_selector);
            this.mMoreItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Rect rect = new Rect();
                    RD_ReadActivity.this.mMoreItem.getContentView().getGlobalVisibleRect(rect);
                    RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().show(new RectF(rect), true);
                }
            });
            this.mOKItem = new CircleItemImpl(this);
            this.mOKItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_OK);
            this.mOKItem.setImageResource(R.drawable.rd_annot_create_ok_selector);
            this.mOKItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(null);
                    RD_ReadActivity.this.mRead.changeState(4);
                }
            });
            this.mContinuousCreateItem = new CircleItemImpl(this);
            this.mContinuousCreateItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_CONTINUE);
            this.mIsContinuousCreate = false;
            ((NoteToolHandler) module.getToolHandler()).setIsContinuousCreate(false);
            this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_false_selector);
            this.mContinuousCreateItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!AppUtil.isFastDoubleClick()) {
                        if (((NoteToolHandler) module.getToolHandler()).getIsContinuousCreate()) {
                            RD_ReadActivity.this.mIsContinuousCreate = false;
                            ((NoteToolHandler) module.getToolHandler()).setIsContinuousCreate(false);
                            RD_ReadActivity.this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_false_selector);
                        } else {
                            RD_ReadActivity.this.mIsContinuousCreate = true;
                            ((NoteToolHandler) module.getToolHandler()).setIsContinuousCreate(true);
                            RD_ReadActivity.this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_true_selector);
                        }
                        AppAnnotUtil.getInstance(RD_ReadActivity.this.getApplicationContext()).showAnnotContinueCreateToast(RD_ReadActivity.this.mIsContinuousCreate);
                    }
                }
            });
            this.mRead.getMainFrame().getToolSetBar().addView(this.mPropertyItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getToolSetBar().addView(this.mMoreItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getToolSetBar().addView(this.mOKItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getToolSetBar().addView(this.mContinuousCreateItem, TB_Position.Position_CENTER);
        }
    }

    private void addNoteListener(final NoteModule module) {
        if (module != null) {
            this.mNoteItem = new CircleItemImpl(this);
            this.mNoteItem.setTag(ToolbarItemConfig.ITEM_NOTE_TAG);
            this.mNoteItem.setImageResource(R.drawable.mt_iv_note_selector);
            this.mNoteItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(module.getToolHandler());
                    RD_ReadActivity.this.mRead.changeState(6);
                    RD_ReadActivity.this.resetAnnotBarToNote(module);
                }
            });
            this.mRead.getMainFrame().getEditBar().addView(this.mNoteItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getMoreToolsBar().registerListener(new IMT_MoreClickListener() {
                public void onMTClick(int type) {
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(module.getToolHandler());
                    RD_ReadActivity.this.mRead.changeState(6);
                    RD_ReadActivity.this.resetAnnotBarToNote(module);
                }

                public int getType() {
                    return 2;
                }
            });
            this.mNoteItem.setEnable(true);
        }
    }

    private void addCircleListener(final CircleModule module) {
        if (module != null) {
            this.mCircleItem = new CircleItemImpl(this);
            this.mCircleItem.setTag(ToolbarItemConfig.ANNOTS_BAR_ITEM_CIR_TAG);
            this.mCircleItem.setImageResource(R.drawable.annot_circle_selector);
            this.mRead.getMainFrame().getMoreToolsBar().registerListener(new IMT_MoreClickListener() {
                public void onMTClick(int type) {
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(module.getToolHandler());
                    RD_ReadActivity.this.mRead.changeState(6);
                    RD_ReadActivity.this.resetAnnotBarToCircle(module);
                }

                public int getType() {
                    return 6;
                }
            });
            this.mCircleItem.setEnable(true);
        }
    }

    private void addSquareListener(final SquareModule module) {
        if (module != null) {
            this.mCircleItem = new CircleItemImpl(this);
            this.mCircleItem.setTag(ToolbarItemConfig.ANNOTS_BAR_ITEM_SQU_TAG);
            this.mCircleItem.setImageResource(R.drawable.annot_square_selector);
            this.mRead.getMainFrame().getMoreToolsBar().registerListener(new IMT_MoreClickListener() {
                public void onMTClick(int type) {
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(module.getToolHandler());
                    RD_ReadActivity.this.mRead.changeState(6);
                    RD_ReadActivity.this.resetAnnotBarToSquare(module);
                }

                public int getType() {
                    return 7;
                }
            });
            this.mCircleItem.setEnable(true);
        }
    }

    private void resetAnnotBarToCircle(final CircleModule module) {
        if (module != null) {
            this.mRead.getMainFrame().getToolSetBar().removeAllItems();
            this.mMoreItem = new CircleItemImpl(this) {
                public void onItemLayout(int l, int t, int r, int b) {
                    if (module.getToolHandler() == ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).getCurrentToolHandler() && RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().isShowing()) {
                        Rect rect = new Rect();
                        RD_ReadActivity.this.mMoreItem.getContentView().getGlobalVisibleRect(rect);
                        RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().update(new RectF(rect));
                    }
                }
            };
            this.mMoreItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_MORE);
            this.mMoreItem.setImageResource(R.drawable.mt_more_selector);
            this.mMoreItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Rect rect = new Rect();
                    RD_ReadActivity.this.mMoreItem.getContentView().getGlobalVisibleRect(rect);
                    RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().show(new RectF(rect), true);
                }
            });
            this.mPropertyBar = this.mRead.getMainFrame().getPropertyBar();
            this.mPropertyBar.setPropertyChangeListener(module);
            final CircleToolHandler toolHandler = (CircleToolHandler) module.getToolHandler();
            this.mPropertyItem = new PropertyCircleItemImp(this) {
                public void onItemLayout(int l, int t, int r, int b) {
                    if (((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).getCurrentToolHandler() == toolHandler && RD_ReadActivity.this.mPropertyBar.isShowing()) {
                        Rect mProRect = new Rect();
                        RD_ReadActivity.this.mPropertyItem.getContentView().getGlobalVisibleRect(mProRect);
                        RD_ReadActivity.this.mPropertyBar.update(new RectF(mProRect));
                    }
                }
            };
            int[] colors = new int[PropertyBar.PB_COLORS_CIRCLE.length];
            System.arraycopy(PropertyBar.PB_COLORS_CIRCLE, 0, colors, 0, colors.length);
            colors[0] = PropertyBar.PB_COLORS_CIRCLE[0];
            this.mPropertyBar.setColors(colors);
            this.mPropertyBar.setProperty(1, toolHandler.getColor());
            this.mPropertyBar.setProperty(2, toolHandler.getOpacity());
            this.mPropertyBar.setProperty(4, toolHandler.getLineWidth());
            this.mPropertyBar.setPropertyChangeListener(module);
            this.mPropertyItem.setTag(ToolbarItemConfig.ITEM_PROPERTY_TAG);
            this.mPropertyItem.setCentreCircleColor(toolHandler.getColor());
            this.mPropertyItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    RD_ReadActivity.this.mPropertyBar.setArrowVisible(true);
                    RD_ReadActivity.this.mPropertyBar.reset(7);
                    Rect mProRect = new Rect();
                    RD_ReadActivity.this.mPropertyItem.getContentView().getGlobalVisibleRect(mProRect);
                    RD_ReadActivity.this.mRead.getMainFrame().getPropertyBar().show(new RectF(mProRect), true);
                }
            });
            this.mOKItem = new CircleItemImpl(this);
            this.mOKItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_OK);
            this.mOKItem.setImageResource(R.drawable.rd_annot_create_ok_selector);
            this.mOKItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(null);
                    RD_ReadActivity.this.mRead.changeState(4);
                }
            });
            module.setColorChangeListener(new CircleModule.ColorChangeListener() {
                public void onColorChange(int color) {
                    RD_ReadActivity.this.mPropertyItem.setCentreCircleColor(color);
                }
            });
            this.mContinuousCreateItem = new CircleItemImpl(this);
            this.mContinuousCreateItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_CONTINUE);
            this.mIsContinuousCreate = false;
            ((CircleToolHandler) module.getToolHandler()).setIsContinuousCreate(false);
            if (this.mIsContinuousCreate) {
                this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_true_selector);
            } else {
                this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_false_selector);
            }
            this.mContinuousCreateItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!AppUtil.isFastDoubleClick()) {
                        if (((CircleToolHandler) module.getToolHandler()).getIsContinuousCreate()) {
                            RD_ReadActivity.this.mIsContinuousCreate = false;
                            ((CircleToolHandler) module.getToolHandler()).setIsContinuousCreate(RD_ReadActivity.this.mIsContinuousCreate);
                            RD_ReadActivity.this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_false_selector);
                        } else {
                            RD_ReadActivity.this.mIsContinuousCreate = true;
                            ((CircleToolHandler) module.getToolHandler()).setIsContinuousCreate(RD_ReadActivity.this.mIsContinuousCreate);
                            RD_ReadActivity.this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_true_selector);
                        }
                        AppAnnotUtil.getInstance(RD_ReadActivity.this.getApplicationContext()).showAnnotContinueCreateToast(RD_ReadActivity.this.mIsContinuousCreate);
                    }
                }
            });
            this.mRead.getMainFrame().getToolSetBar().addView(this.mMoreItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getToolSetBar().addView(this.mPropertyItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getToolSetBar().addView(this.mOKItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getToolSetBar().addView(this.mContinuousCreateItem, TB_Position.Position_CENTER);
        }
    }

    private void resetAnnotBarToSquare(final SquareModule module) {
        if (module != null) {
            this.mRead.getMainFrame().getToolSetBar().removeAllItems();
            this.mMoreItem = new CircleItemImpl(this) {
                public void onItemLayout(int l, int t, int r, int b) {
                    if (module.getToolHandler() == ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).getCurrentToolHandler() && RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().isShowing()) {
                        Rect rect = new Rect();
                        RD_ReadActivity.this.mMoreItem.getContentView().getGlobalVisibleRect(rect);
                        RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().update(new RectF(rect));
                    }
                }
            };
            this.mMoreItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_MORE);
            this.mMoreItem.setImageResource(R.drawable.mt_more_selector);
            this.mMoreItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Rect rect = new Rect();
                    RD_ReadActivity.this.mMoreItem.getContentView().getGlobalVisibleRect(rect);
                    RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().show(new RectF(rect), true);
                }
            });
            this.mPropertyBar = this.mRead.getMainFrame().getPropertyBar();
            this.mPropertyBar.setPropertyChangeListener(module);
            final SquareToolHandler toolHandler = (SquareToolHandler) module.getToolHandler();
            this.mPropertyItem = new PropertyCircleItemImp(this) {
                public void onItemLayout(int l, int t, int r, int b) {
                    if (((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).getCurrentToolHandler() == toolHandler && RD_ReadActivity.this.mPropertyBar.isShowing()) {
                        Rect mProRect = new Rect();
                        RD_ReadActivity.this.mPropertyItem.getContentView().getGlobalVisibleRect(mProRect);
                        RD_ReadActivity.this.mPropertyBar.update(new RectF(mProRect));
                    }
                }
            };
            int[] colors = new int[PropertyBar.PB_COLORS_SQUARE.length];
            System.arraycopy(PropertyBar.PB_COLORS_SQUARE, 0, colors, 0, colors.length);
            colors[0] = PropertyBar.PB_COLORS_SQUARE[0];
            this.mPropertyBar.setColors(colors);
            this.mPropertyBar.setProperty(1, toolHandler.getColor());
            this.mPropertyBar.setProperty(2, toolHandler.getOpacity());
            this.mPropertyBar.setProperty(4, toolHandler.getLineWidth());
            this.mPropertyBar.setPropertyChangeListener(module);
            this.mPropertyItem.setTag(ToolbarItemConfig.ITEM_PROPERTY_TAG);
            this.mPropertyItem.setCentreCircleColor(toolHandler.getColor());
            this.mPropertyItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    RD_ReadActivity.this.mPropertyBar.setArrowVisible(true);
                    RD_ReadActivity.this.mPropertyBar.reset(7);
                    Rect mProRect = new Rect();
                    RD_ReadActivity.this.mPropertyItem.getContentView().getGlobalVisibleRect(mProRect);
                    RD_ReadActivity.this.mRead.getMainFrame().getPropertyBar().show(new RectF(mProRect), true);
                }
            });
            this.mOKItem = new CircleItemImpl(this);
            this.mOKItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_OK);
            this.mOKItem.setImageResource(R.drawable.rd_annot_create_ok_selector);
            this.mOKItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(null);
                    RD_ReadActivity.this.mRead.changeState(4);
                }
            });
            module.setColorChangeListener(new SquareModule.ColorChangeListener() {
                public void onColorChange(int color) {
                    RD_ReadActivity.this.mPropertyItem.setCentreCircleColor(color);
                }
            });
            this.mContinuousCreateItem = new CircleItemImpl(this);
            this.mContinuousCreateItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_CONTINUE);
            this.mIsContinuousCreate = false;
            ((SquareToolHandler) module.getToolHandler()).setIsContinuousCreate(false);
            if (this.mIsContinuousCreate) {
                this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_true_selector);
            } else {
                this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_false_selector);
            }
            this.mContinuousCreateItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!AppUtil.isFastDoubleClick()) {
                        if (((SquareToolHandler) module.getToolHandler()).getIsContinuousCreate()) {
                            RD_ReadActivity.this.mIsContinuousCreate = false;
                            ((SquareToolHandler) module.getToolHandler()).setIsContinuousCreate(RD_ReadActivity.this.mIsContinuousCreate);
                            RD_ReadActivity.this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_false_selector);
                        } else {
                            RD_ReadActivity.this.mIsContinuousCreate = true;
                            ((SquareToolHandler) module.getToolHandler()).setIsContinuousCreate(RD_ReadActivity.this.mIsContinuousCreate);
                            RD_ReadActivity.this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_true_selector);
                        }
                        AppAnnotUtil.getInstance(RD_ReadActivity.this.getApplicationContext()).showAnnotContinueCreateToast(RD_ReadActivity.this.mIsContinuousCreate);
                    }
                }
            });
            this.mRead.getMainFrame().getToolSetBar().addView(this.mMoreItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getToolSetBar().addView(this.mPropertyItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getToolSetBar().addView(this.mOKItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getToolSetBar().addView(this.mContinuousCreateItem, TB_Position.Position_CENTER);
        }
    }

    private void addTypewriterListener(final TypewriterModule module) {
        if (module != null) {
            this.mTypewriterItem = new CircleItemImpl(this);
            this.mTypewriterItem.setTag(ToolbarItemConfig.ANNOTS_BAR_ITEM_TYPEWRITE);
            this.mTypewriterItem.setImageResource(R.drawable.annot_typewriter_selector);
            this.mTypewriterItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(module.getToolHandler());
                    RD_ReadActivity.this.mRead.changeState(6);
                    RD_ReadActivity.this.resetAnnotBarToTypewriter(module);
                }
            });
            this.mRead.getMainFrame().getEditBar().addView(this.mTypewriterItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getMoreToolsBar().registerListener(new IMT_MoreClickListener() {
                public void onMTClick(int type) {
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(module.getToolHandler());
                    RD_ReadActivity.this.mRead.changeState(6);
                    RD_ReadActivity.this.resetAnnotBarToTypewriter(module);
                }

                public int getType() {
                    return 8;
                }
            });
            this.mTypewriterItem.setEnable(true);
        }
    }

    private void resetAnnotBarToTypewriter(final TypewriterModule module) {
        if (module != null) {
            this.mRead.getMainFrame().getToolSetBar().removeAllItems();
            this.mMoreItem = new CircleItemImpl(this) {
                public void onItemLayout(int l, int t, int r, int b) {
                    if (module.getToolHandler() == ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).getCurrentToolHandler() && RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().isShowing()) {
                        Rect rect = new Rect();
                        RD_ReadActivity.this.mMoreItem.getContentView().getGlobalVisibleRect(rect);
                        RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().update(new RectF(rect));
                    }
                }
            };
            this.mMoreItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_MORE);
            this.mMoreItem.setImageResource(R.drawable.mt_more_selector);
            this.mMoreItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Rect rect = new Rect();
                    RD_ReadActivity.this.mMoreItem.getContentView().getGlobalVisibleRect(rect);
                    RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().show(new RectF(rect), true);
                }
            });
            this.mPropertyBar = this.mRead.getMainFrame().getPropertyBar();
            this.mPropertyBar.setPropertyChangeListener(module);
            final TypewriterToolHandler toolHandler = (TypewriterToolHandler) module.getToolHandler();
            this.mPropertyItem = new PropertyCircleItemImp(this) {
                public void onItemLayout(int l, int t, int r, int b) {
                    if (toolHandler == ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).getCurrentToolHandler() && RD_ReadActivity.this.mPropertyBar.isShowing()) {
                        Rect mProRect = new Rect();
                        RD_ReadActivity.this.mPropertyItem.getContentView().getGlobalVisibleRect(mProRect);
                        RD_ReadActivity.this.mPropertyBar.update(new RectF(mProRect));
                    }
                }
            };
            int[] colors = new int[PropertyBar.PB_COLORS_TYPEWRITER.length];
            System.arraycopy(PropertyBar.PB_COLORS_TYPEWRITER, 0, colors, 0, colors.length);
            colors[0] = PropertyBar.PB_COLORS_TYPEWRITER[0];
            this.mPropertyBar.setColors(colors);
            this.mPropertyBar.setProperty(1, toolHandler.getColor());
            this.mPropertyBar.setProperty(2, toolHandler.getOpacity());
            this.mPropertyBar.setProperty(8, toolHandler.getFontName());
            this.mPropertyBar.setProperty(16, toolHandler.getFontSize());
            this.mPropertyBar.setPropertyChangeListener(module);
            this.mPropertyItem.setTag(ToolbarItemConfig.ITEM_PROPERTY_TAG);
            this.mPropertyItem.setCentreCircleColor(toolHandler.getColor());
            this.mPropertyItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    RD_ReadActivity.this.mPropertyBar.setArrowVisible(true);
                    RD_ReadActivity.this.mPropertyBar.reset(27);
                    Rect rect = new Rect();
                    RD_ReadActivity.this.mPropertyItem.getContentView().getGlobalVisibleRect(rect);
                    RD_ReadActivity.this.mRead.getMainFrame().getPropertyBar().show(new RectF(rect), true);
                }
            });
            module.setColorChangeListener(new TypewriterModule.ColorChangeListener() {
                public void onColorChange(int color) {
                    RD_ReadActivity.this.mPropertyItem.setCentreCircleColor(color);
                }
            });
            this.mContinuousCreateItem = new CircleItemImpl(this);
            this.mContinuousCreateItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_CONTINUE);
            this.mIsContinuousCreate = false;
            ((TypewriterToolHandler) module.getToolHandler()).setIsContinuousCreate(false);
            if (this.mIsContinuousCreate) {
                this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_true_selector);
            } else {
                this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_false_selector);
            }
            this.mContinuousCreateItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!AppUtil.isFastDoubleClick()) {
                        if (((TypewriterToolHandler) module.getToolHandler()).getIsContinuousCreate()) {
                            RD_ReadActivity.this.mIsContinuousCreate = false;
                            ((TypewriterToolHandler) module.getToolHandler()).setIsContinuousCreate(RD_ReadActivity.this.mIsContinuousCreate);
                            RD_ReadActivity.this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_false_selector);
                        } else {
                            RD_ReadActivity.this.mIsContinuousCreate = true;
                            ((TypewriterToolHandler) module.getToolHandler()).setIsContinuousCreate(RD_ReadActivity.this.mIsContinuousCreate);
                            RD_ReadActivity.this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_true_selector);
                        }
                        AppAnnotUtil.getInstance(RD_ReadActivity.this.getApplicationContext()).showAnnotContinueCreateToast(RD_ReadActivity.this.mIsContinuousCreate);
                    }
                }
            });
            this.mOKItem = new CircleItemImpl(this);
            this.mOKItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_OK);
            this.mOKItem.setImageResource(R.drawable.rd_annot_create_ok_selector);
            this.mOKItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(null);
                    RD_ReadActivity.this.mRead.changeState(4);
                }
            });
            this.mRead.getMainFrame().getToolSetBar().addView(this.mMoreItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getToolSetBar().addView(this.mPropertyItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getToolSetBar().addView(this.mOKItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getToolSetBar().addView(this.mContinuousCreateItem, TB_Position.Position_CENTER);
        }
    }

    private void addStampListener(final StampModule module) {
        if (module != null) {
            this.mRead.getMainFrame().getMoreToolsBar().registerListener(new IMT_MoreClickListener() {
                public void onMTClick(int type) {
                    if (type == 9) {
                        ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(module.getToolHandler());
                        RD_ReadActivity.this.mRead.changeState(6);
                        RD_ReadActivity.this.resetAnnotBarToStamp(module);
                    }
                }

                public int getType() {
                    return 9;
                }
            });
        }
    }

    private void resetAnnotBarToStamp(final StampModule module) {
        if (module != null) {
            this.mRead.getMainFrame().getToolSetBar().removeAllItems();
            this.mOKItem = new CircleItemImpl(this);
            this.mOKItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_OK);
            this.mOKItem.setImageResource(R.drawable.rd_annot_create_ok_selector);
            this.mOKItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(null);
                    RD_ReadActivity.this.mRead.changeState(4);
                }
            });
            this.mPropertyBar = this.mRead.getMainFrame().getPropertyBar();
            this.mPropertyBar.setPropertyChangeListener(module);
            final StampToolHandler toolHandler = (StampToolHandler) module.getToolHandler();
            this.mPropertyItem = new PropertyCircleItemImp(this) {
                public void onItemLayout(int l, int t, int r, int b) {
                    super.onItemLayout(l, t, r, b);
                    if (((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).getCurrentToolHandler() == toolHandler && RD_ReadActivity.this.mPropertyBar.isShowing()) {
                        toolHandler.resetPropertyBar(RD_ReadActivity.this.mPropertyBar);
                        Rect rect = new Rect();
                        RD_ReadActivity.this.mPropertyItem.getContentView().getGlobalVisibleRect(rect);
                        RD_ReadActivity.this.mPropertyBar.update(new RectF(rect));
                    }
                }
            };
            toolHandler.initDisplayItems(this.mPropertyBar, this.mPropertyItem);
            this.mPropertyItem.setTag(ToolbarItemConfig.ITEM_PROPERTY_TAG);
            this.mPropertyItem.setCentreCircleColor(Color.parseColor("#179CD8"));
            this.mPropertyItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Rect rect = new Rect();
                    RD_ReadActivity.this.mPropertyItem.getContentView().getGlobalVisibleRect(rect);
                    RD_ReadActivity.this.mPropertyBar.show(new RectF(rect), true);
                }
            });
            this.mContinuousCreateItem = new CircleItemImpl(this);
            this.mContinuousCreateItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_CONTINUE);
            this.mIsContinuousCreate = false;
            toolHandler.setIsContinuousCreate(this.mIsContinuousCreate);
            if (this.mIsContinuousCreate) {
                this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_true_selector);
            } else {
                this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_false_selector);
            }
            this.mContinuousCreateItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!AppUtil.isFastDoubleClick()) {
                        if (((StampToolHandler) module.getToolHandler()).getIsContinuousCreate()) {
                            RD_ReadActivity.this.mIsContinuousCreate = false;
                            ((StampToolHandler) module.getToolHandler()).setIsContinuousCreate(RD_ReadActivity.this.mIsContinuousCreate);
                            RD_ReadActivity.this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_false_selector);
                        } else {
                            RD_ReadActivity.this.mIsContinuousCreate = true;
                            ((StampToolHandler) module.getToolHandler()).setIsContinuousCreate(RD_ReadActivity.this.mIsContinuousCreate);
                            RD_ReadActivity.this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_true_selector);
                        }
                        AppAnnotUtil.getInstance(RD_ReadActivity.this.getApplicationContext()).showAnnotContinueCreateToast(RD_ReadActivity.this.mIsContinuousCreate);
                    }
                }
            });
            this.mMoreItem = new CircleItemImpl(this) {
                public void onItemLayout(int l, int t, int r, int b) {
                    if (((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).getCurrentToolHandler() == toolHandler && RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().isShowing()) {
                        Rect rect = new Rect();
                        RD_ReadActivity.this.mMoreItem.getContentView().getGlobalVisibleRect(rect);
                        RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().update(new RectF(rect));
                    }
                }
            };
            this.mMoreItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_MORE);
            this.mMoreItem.setImageResource(R.drawable.mt_more_selector);
            this.mMoreItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Rect rect = new Rect();
                    RD_ReadActivity.this.mMoreItem.getContentView().getGlobalVisibleRect(rect);
                    RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().show(new RectF(rect), true);
                }
            });
            this.mRead.getMainFrame().getToolSetBar().addView(this.mPropertyItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getToolSetBar().addView(this.mOKItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getToolSetBar().addView(this.mContinuousCreateItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getToolSetBar().addView(this.mMoreItem, TB_Position.Position_CENTER);
        }
    }

    private void addInsertTextListener(final CaretModule module) {
        if (module != null) {
            this.mInsertTextItem = new CircleItemImpl(this);
            this.mInsertTextItem.setImageResource(R.drawable.annot_insert_selector);
            this.mInsertTextItem.setTag(ToolbarItemConfig.ANNOTS_BAR_ITEM_CARET);
            this.mRead.getMainFrame().getMoreToolsBar().registerListener(new IMT_MoreClickListener() {
                public void onMTClick(int type) {
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(module.getISToolHandler());
                    RD_ReadActivity.this.mRead.changeState(6);
                    RD_ReadActivity.this.resetAnnotBarToInsertText(module);
                }

                public int getType() {
                    return 10;
                }
            });
            this.mInsertTextItem.setEnable(true);
        }
    }

    private void resetAnnotBarToInsertText(final CaretModule module) {
        if (module != null) {
            this.mRead.getMainFrame().getToolSetBar().removeAllItems();
            this.mMoreItem = new CircleItemImpl(this) {
                public void onItemLayout(int l, int t, int r, int b) {
                    if (module.getISToolHandler() == ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).getCurrentToolHandler() && RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().isShowing()) {
                        Rect rect = new Rect();
                        RD_ReadActivity.this.mMoreItem.getContentView().getGlobalVisibleRect(rect);
                        RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().update(new RectF(rect));
                    }
                }
            };
            this.mMoreItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_MORE);
            this.mMoreItem.setImageResource(R.drawable.mt_more_selector);
            this.mMoreItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Rect rect = new Rect();
                    RD_ReadActivity.this.mMoreItem.getContentView().getGlobalVisibleRect(rect);
                    RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().show(new RectF(rect), true);
                }
            });
            this.mPropertyBar = this.mRead.getMainFrame().getPropertyBar();
            this.mPropertyBar.setPropertyChangeListener(module);
            final CaretToolHandler toolHandler = (CaretToolHandler) module.getISToolHandler();
            this.mPropertyItem = new PropertyCircleItemImp(this) {
                public void onItemLayout(int l, int t, int r, int b) {
                    if (toolHandler == ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).getCurrentToolHandler() && RD_ReadActivity.this.mPropertyBar.isShowing()) {
                        Rect mProRect = new Rect();
                        RD_ReadActivity.this.mPropertyItem.getContentView().getGlobalVisibleRect(mProRect);
                        RD_ReadActivity.this.mPropertyBar.update(new RectF(mProRect));
                    }
                }
            };
            int[] colors = new int[PropertyBar.PB_COLORS_CARET.length];
            System.arraycopy(PropertyBar.PB_COLORS_CARET, 0, colors, 0, colors.length);
            colors[0] = PropertyBar.PB_COLORS_CARET[0];
            this.mPropertyBar.setColors(colors);
            this.mPropertyBar.setProperty(1, toolHandler.getColor());
            this.mPropertyBar.setProperty(2, toolHandler.getOpacity());
            this.mPropertyBar.setPropertyChangeListener(module);
            this.mPropertyItem.setTag(ToolbarItemConfig.ITEM_PROPERTY_TAG);
            this.mPropertyItem.setCentreCircleColor(toolHandler.getColor());
            this.mPropertyItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    RD_ReadActivity.this.mPropertyBar.setArrowVisible(true);
                    RD_ReadActivity.this.mPropertyBar.reset(3);
                    Rect rect = new Rect();
                    RD_ReadActivity.this.mPropertyItem.getContentView().getGlobalVisibleRect(rect);
                    RD_ReadActivity.this.mRead.getMainFrame().getPropertyBar().show(new RectF(rect), true);
                }
            });
            module.setColorChangeListener(new CaretModule.ColorChangeListener() {
                public void onColorChange(int color) {
                    RD_ReadActivity.this.mPropertyItem.setCentreCircleColor(color);
                }
            });
            this.mContinuousCreateItem = new CircleItemImpl(this);
            this.mContinuousCreateItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_CONTINUE);
            this.mIsContinuousCreate = false;
            ((CaretToolHandler) module.getISToolHandler()).setIsContinuousCreate(false);
            if (this.mIsContinuousCreate) {
                this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_true_selector);
            } else {
                this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_false_selector);
            }
            this.mContinuousCreateItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!AppUtil.isFastDoubleClick()) {
                        if (((CaretToolHandler) module.getISToolHandler()).getIsContinuousCreate()) {
                            RD_ReadActivity.this.mIsContinuousCreate = false;
                            ((CaretToolHandler) module.getISToolHandler()).setIsContinuousCreate(RD_ReadActivity.this.mIsContinuousCreate);
                            RD_ReadActivity.this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_false_selector);
                        } else {
                            RD_ReadActivity.this.mIsContinuousCreate = true;
                            ((CaretToolHandler) module.getISToolHandler()).setIsContinuousCreate(RD_ReadActivity.this.mIsContinuousCreate);
                            RD_ReadActivity.this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_true_selector);
                        }
                        AppAnnotUtil.getInstance(RD_ReadActivity.this.getApplicationContext()).showAnnotContinueCreateToast(RD_ReadActivity.this.mIsContinuousCreate);
                    }
                }
            });
            this.mOKItem = new CircleItemImpl(this);
            this.mOKItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_OK);
            this.mOKItem.setImageResource(R.drawable.rd_annot_create_ok_selector);
            this.mOKItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(null);
                    RD_ReadActivity.this.mRead.changeState(4);
                }
            });
            this.mRead.getMainFrame().getToolSetBar().addView(this.mMoreItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getToolSetBar().addView(this.mPropertyItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getToolSetBar().addView(this.mOKItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getToolSetBar().addView(this.mContinuousCreateItem, TB_Position.Position_CENTER);
        }
    }

    private void addReplaceListener(final CaretModule module) {
        if (module != null) {
            this.mReplaceItem = new CircleItemImpl(this);
            this.mReplaceItem.setImageResource(R.drawable.annot_replace_selector);
            this.mReplaceItem.setTag(ToolbarItemConfig.ANNOTS_BAR_ITEM_REPLACE);
            this.mRead.getMainFrame().getMoreToolsBar().registerListener(new IMT_MoreClickListener() {
                public void onMTClick(int type) {
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(module.getRPToolHandler());
                    RD_ReadActivity.this.mRead.changeState(6);
                    RD_ReadActivity.this.resetAnnotBarToReplace(module);
                }

                public int getType() {
                    return 11;
                }
            });
            this.mInsertTextItem.setEnable(true);
        }
    }

    private void resetAnnotBarToReplace(final CaretModule module) {
        if (module != null) {
            this.mRead.getMainFrame().getToolSetBar().removeAllItems();
            this.mMoreItem = new CircleItemImpl(this) {
                public void onItemLayout(int l, int t, int r, int b) {
                    if (module.getRPToolHandler() == ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).getCurrentToolHandler() && RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().isShowing()) {
                        Rect rect = new Rect();
                        RD_ReadActivity.this.mMoreItem.getContentView().getGlobalVisibleRect(rect);
                        RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().update(new RectF(rect));
                    }
                }
            };
            this.mMoreItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_MORE);
            this.mMoreItem.setImageResource(R.drawable.mt_more_selector);
            this.mMoreItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Rect rect = new Rect();
                    RD_ReadActivity.this.mMoreItem.getContentView().getGlobalVisibleRect(rect);
                    RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().show(new RectF(rect), true);
                }
            });
            this.mPropertyBar = this.mRead.getMainFrame().getPropertyBar();
            this.mPropertyBar.setPropertyChangeListener(module);
            final CaretToolHandler toolHandler = (CaretToolHandler) module.getRPToolHandler();
            this.mPropertyItem = new PropertyCircleItemImp(this) {
                public void onItemLayout(int l, int t, int r, int b) {
                    if (toolHandler == ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).getCurrentToolHandler() && RD_ReadActivity.this.mPropertyBar.isShowing()) {
                        Rect mProRect = new Rect();
                        RD_ReadActivity.this.mPropertyItem.getContentView().getGlobalVisibleRect(mProRect);
                        RD_ReadActivity.this.mPropertyBar.update(new RectF(mProRect));
                    }
                }
            };
            int[] colors = new int[PropertyBar.PB_COLORS_CARET.length];
            System.arraycopy(PropertyBar.PB_COLORS_CARET, 0, colors, 0, colors.length);
            colors[0] = PropertyBar.PB_COLORS_CARET[0];
            this.mPropertyBar.setColors(colors);
            this.mPropertyBar.setProperty(1, toolHandler.getColor());
            this.mPropertyBar.setProperty(2, toolHandler.getOpacity());
            this.mPropertyBar.setPropertyChangeListener(module);
            this.mPropertyItem.setTag(ToolbarItemConfig.ITEM_PROPERTY_TAG);
            this.mPropertyItem.setCentreCircleColor(toolHandler.getColor());
            this.mPropertyItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    RD_ReadActivity.this.mPropertyBar.setArrowVisible(true);
                    RD_ReadActivity.this.mPropertyBar.reset(3);
                    Rect rect = new Rect();
                    RD_ReadActivity.this.mPropertyItem.getContentView().getGlobalVisibleRect(rect);
                    RD_ReadActivity.this.mRead.getMainFrame().getPropertyBar().show(new RectF(rect), true);
                }
            });
            module.setColorChangeListener(new CaretModule.ColorChangeListener() {
                public void onColorChange(int color) {
                    RD_ReadActivity.this.mPropertyItem.setCentreCircleColor(color);
                }
            });
            this.mContinuousCreateItem = new CircleItemImpl(this);
            this.mContinuousCreateItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_CONTINUE);
            this.mIsContinuousCreate = false;
            ((CaretToolHandler) module.getRPToolHandler()).setIsContinuousCreate(false);
            if (this.mIsContinuousCreate) {
                this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_true_selector);
            } else {
                this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_false_selector);
            }
            this.mContinuousCreateItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!AppUtil.isFastDoubleClick()) {
                        if (((CaretToolHandler) module.getRPToolHandler()).getIsContinuousCreate()) {
                            RD_ReadActivity.this.mIsContinuousCreate = false;
                            ((CaretToolHandler) module.getRPToolHandler()).setIsContinuousCreate(RD_ReadActivity.this.mIsContinuousCreate);
                            RD_ReadActivity.this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_false_selector);
                        } else {
                            RD_ReadActivity.this.mIsContinuousCreate = true;
                            ((CaretToolHandler) module.getRPToolHandler()).setIsContinuousCreate(RD_ReadActivity.this.mIsContinuousCreate);
                            RD_ReadActivity.this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_true_selector);
                        }
                        AppAnnotUtil.getInstance(RD_ReadActivity.this.getApplicationContext()).showAnnotContinueCreateToast(RD_ReadActivity.this.mIsContinuousCreate);
                    }
                }
            });
            this.mOKItem = new CircleItemImpl(this);
            this.mOKItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_OK);
            this.mOKItem.setImageResource(R.drawable.rd_annot_create_ok_selector);
            this.mOKItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(null);
                    RD_ReadActivity.this.mRead.changeState(4);
                }
            });
            this.mRead.getMainFrame().getToolSetBar().addView(this.mMoreItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getToolSetBar().addView(this.mPropertyItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getToolSetBar().addView(this.mOKItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getToolSetBar().addView(this.mContinuousCreateItem, TB_Position.Position_CENTER);
        }
    }

    private void addInkListener(final InkModule module) {
        if (module != null) {
            this.mInkItem = new CircleItemImpl(this);
            this.mInkItem.setTag(ToolbarItemConfig.ANNOTS_BAR_ITEM_PEN);
            this.mInkItem.setImageResource(R.drawable.annot_pencil_selector);
            this.mInkItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(module.getToolHandler());
                    RD_ReadActivity.this.mRead.changeState(6);
                    RD_ReadActivity.this.resetAnnotBar(module, 13);
                }
            });
            this.mRead.getMainFrame().getEditBar().addView(this.mInkItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getMoreToolsBar().registerListener(new IMT_MoreClickListener() {
                public void onMTClick(int type) {
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(module.getToolHandler());
                    RD_ReadActivity.this.mRead.changeState(6);
                    RD_ReadActivity.this.resetAnnotBar(module, 13);
                }

                public int getType() {
                    return 13;
                }
            });
            this.mInkItem.setEnable(true);
        }
    }

    private AbstractToolHandler getToolHandler(Module module) {
        return getToolHandler(module, -1);
    }

    private AbstractToolHandler getToolHandler(Module module, int tag) {
        if (module == null) {
            return null;
        }
        String name = module.getName();
        switch (name.hashCode()) {
            case -976244096:
                if (name.equals(Module.MODULE_NAME_ERASER)) {
                    return (AbstractToolHandler) ((EraserModule) module).getToolHandler();
                }
                return null;
            case -269570010:
                if (name.equals(Module.MODULE_NAME_INK)) {
                    return (AbstractToolHandler) ((InkModule) module).getToolHandler();
                }
                return null;
            case 1035655192:
                if (!name.equals(Module.MODULE_NAME_LINE)) {
                    return null;
                }
                if (tag == 15) {
                    return (AbstractToolHandler) ((LineModule) module).getArrowToolHandler();
                }
                if (tag == 14) {
                    return (AbstractToolHandler) ((LineModule) module).getLineToolHandler();
                }
                return null;
            default:
                return null;
        }
    }

    private void resetAnnotBar(final Module module, final int tag) {
        if (module != null) {
            this.mRead.getMainFrame().getToolSetBar().removeAllItems();
            this.mMoreItem = new CircleItemImpl(this) {
                public void onItemLayout(int l, int t, int r, int b) {
                    if (RD_ReadActivity.this.getToolHandler(module, tag) == ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).getCurrentToolHandler() && RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().isShowing()) {
                        Rect rect = new Rect();
                        RD_ReadActivity.this.mMoreItem.getContentView().getGlobalVisibleRect(rect);
                        RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().update(new RectF(rect));
                    }
                }
            };
            this.mMoreItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_MORE);
            this.mMoreItem.setImageResource(R.drawable.mt_more_selector);
            this.mMoreItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Rect rect = new Rect();
                    RD_ReadActivity.this.mMoreItem.getContentView().getGlobalVisibleRect(rect);
                    RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().show(new RectF(rect), true);
                }
            });
            this.mPropertyBar = this.mRead.getMainFrame().getPropertyBar();
            final PropertyCircleItem mPropertyBtn = new PropertyCircleItemImp(this) {
                public void onItemLayout(int l, int t, int r, int b) {
                    if (RD_ReadActivity.this.getToolHandler(module, tag) == ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).getCurrentToolHandler() && RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().isShowing()) {
                        Rect rect = new Rect();
                        RD_ReadActivity.this.mMoreItem.getContentView().getGlobalVisibleRect(rect);
                        RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().update(new RectF(rect));
                    }
                }
            };
            mPropertyBtn.setTag(ToolbarItemConfig.ITEM_PROPERTY_TAG);
            final AbstractToolHandler toolHandler = getToolHandler(module, tag);
            int color = toolHandler.getColor();
            int opacity = toolHandler.getOpacity();
            float thickness = toolHandler.getThickness();
            this.mPropertyBar.setProperty(1, color);
            this.mPropertyBar.setProperty(2, opacity);
            this.mPropertyBar.setProperty(4, thickness);
            this.mPropertyBar.setPropertyChangeListener(toolHandler);
            final long properties = toolHandler.getSupportedProperties();
            mPropertyBtn.setCentreCircleColor(color);
            mPropertyBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (AppDisplay.getInstance(RD_ReadActivity.this).isPad()) {
                        RD_ReadActivity.this.mPropertyBar.setArrowVisible(true);
                    } else {
                        RD_ReadActivity.this.mPropertyBar.setArrowVisible(false);
                    }
                    RD_ReadActivity.this.mPropertyBar.reset(properties);
                    Rect rect = new Rect();
                    mPropertyBtn.getContentView().getGlobalVisibleRect(rect);
                    if (AppDisplay.getInstance(RD_ReadActivity.this).isPad()) {
                        RD_ReadActivity.this.mPropertyBar.show(new RectF(rect), true);
                    } else {
                        RD_ReadActivity.this.mPropertyBar.show(new RectF(rect), true);
                    }
                }
            });
            toolHandler.setColorChangeListener(new AbstractToolHandler.ColorChangeListener() {
                public void onColorChange(int color) {
                    mPropertyBtn.setCentreCircleColor(color);
                }
            });
            this.mContinuousCreateItem = new CircleItemImpl(this);
            this.mContinuousCreateItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_CONTINUE);
            boolean z = tag == 13 || tag == 12;
            this.mIsContinuousCreate = z;
            toolHandler.setIsContinuousCreate(this.mIsContinuousCreate);
            if (this.mIsContinuousCreate) {
                this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_true_selector);
            } else {
                this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_false_selector);
            }
            this.mContinuousCreateItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!AppUtil.isFastDoubleClick()) {
                        if (toolHandler.getIsContinuousCreate()) {
                            RD_ReadActivity.this.mIsContinuousCreate = false;
                            RD_ReadActivity.this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_false_selector);
                        } else {
                            RD_ReadActivity.this.mIsContinuousCreate = true;
                            RD_ReadActivity.this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_true_selector);
                        }
                        toolHandler.setIsContinuousCreate(RD_ReadActivity.this.mIsContinuousCreate);
                        AppAnnotUtil.getInstance(RD_ReadActivity.this.getApplicationContext()).showAnnotContinueCreateToast(RD_ReadActivity.this.mIsContinuousCreate);
                    }
                }
            });
            this.mOKItem = new CircleItemImpl(this);
            this.mOKItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_OK);
            this.mOKItem.setImageResource(R.drawable.rd_annot_create_ok_selector);
            this.mOKItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    RD_ReadActivity.this.mRead.changeState(4);
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(null);
                }
            });
            this.mRead.getMainFrame().getToolSetBar().addView(this.mMoreItem, TB_Position.Position_CENTER);
            if (!(module.getName().equalsIgnoreCase(Module.MODULE_NAME_INK) || module.getName().equalsIgnoreCase(Module.MODULE_NAME_ERASER))) {
                this.mRead.getMainFrame().getToolSetBar().addView(this.mContinuousCreateItem, TB_Position.Position_CENTER);
            }
            this.mRead.getMainFrame().getToolSetBar().addView(mPropertyBtn, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getToolSetBar().addView(this.mOKItem, TB_Position.Position_CENTER);
        }
    }

    private void addEraserListener(final EraserModule module) {
        if (module != null) {
            this.mRead.getMainFrame().getMoreToolsBar().registerListener(new IMT_MoreClickListener() {
                public void onMTClick(int type) {
                    if (DocumentManager.getInstance(RD_ReadActivity.this.mRead.getDocViewer()).canAddAnnot()) {
                        if (((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).getCurrentToolHandler() != module.getToolHandler()) {
                            ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(module.getToolHandler());
                        } else {
                            ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(null);
                        }
                        if (((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).getCurrentToolHandler() != null) {
                            RD_ReadActivity.this.mRead.changeState(6);
                        } else if (RD_ReadActivity.this.mRead.getState() == 6) {
                            RD_ReadActivity.this.mRead.changeState(4);
                        }
                        RD_ReadActivity.this.resetAnnotBar(module, 12);
                    }
                }

                public int getType() {
                    return 12;
                }
            });
        }
    }

    private void addLineListener(final LineModule module, final int tag) {
        if (module != null) {
            this.mLineItem = new CircleItemImpl(this);
            this.mLineItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(RD_ReadActivity.this.getToolHandler(module, tag));
                    RD_ReadActivity.this.mRead.changeState(6);
                    RD_ReadActivity.this.resetAnnotBar(module, tag);
                }
            });
            this.mRead.getMainFrame().getEditBar().addView(this.mInkItem, TB_Position.Position_CENTER);
            this.mRead.getMainFrame().getMoreToolsBar().registerListener(new IMT_MoreClickListener() {
                public void onMTClick(int type) {
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(RD_ReadActivity.this.getToolHandler(module, tag));
                    RD_ReadActivity.this.mRead.changeState(6);
                    RD_ReadActivity.this.resetAnnotBar(module, tag);
                }

                public int getType() {
                    return tag;
                }
            });
            this.mInkItem.setEnable(true);
        }
    }

    private void addUndo(final UndoModule module) {
        if (module != null) {
            this.mUndoButton = new CircleItemImpl(this);
            this.mUndoButton.setTag(ToolbarItemConfig.DONE_BAR_ITEM_UNDO);
            this.mUndoButton.setImageResource(R.drawable.annot_undo_pressed);
            this.mUndoButton.setEnable(false);
            this.mRead.getMainFrame().getEditDoneBar().addView(this.mUndoButton, TB_Position.Position_LT);
            this.mRedoButton = new CircleItemImpl(this);
            this.mRedoButton.setTag(ToolbarItemConfig.DONE_BAR_ITEM_REDO);
            this.mRedoButton.setImageResource(R.drawable.annot_redo_pressed);
            this.mRedoButton.setEnable(false);
            this.mRead.getMainFrame().getEditDoneBar().addView(this.mRedoButton, TB_Position.Position_LT);
            this.mUndoButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!AppUtil.isFastDoubleClick()) {
                        module.undo();
                    }
                }
            });
            this.mRedoButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!AppUtil.isFastDoubleClick()) {
                        module.redo();
                    }
                }
            });
            DocumentManager.getInstance(this.mRead.getDocViewer()).registerUndoEventListener(new IUndoEventListener() {
                public void itemWillAdd(DocumentManager dm, IUndoItem item) {
                }

                public void itemAdded(DocumentManager dm, IUndoItem item) {
                    RD_ReadActivity.this.changeButtonStatus();
                }

                public void willUndo(DocumentManager dm, IUndoItem item) {
                }

                public void undoFinished(DocumentManager dm, IUndoItem item) {
                    RD_ReadActivity.this.changeButtonStatus();
                }

                public void willRedo(DocumentManager dm, IUndoItem item) {
                }

                public void redoFinished(DocumentManager dm, IUndoItem item) {
                    RD_ReadActivity.this.changeButtonStatus();
                }

                public void willClearUndo(DocumentManager dm) {
                }

                public void clearUndoFinished(DocumentManager dm) {
                    RD_ReadActivity.this.changeButtonStatus();
                }
            });
            this.mRead.registerStateChangeListener(new IRD_StateChangeListener() {
                public void onStateChanged(int oldState, int newState) {
                    RD_ReadActivity.this.changeButtonStatus();
                }
            });
        }
    }

    private void changeButtonStatus() {
        DocumentManager dm = DocumentManager.getInstance(this.mRead.getDocViewer());
        if (dm.canUndo()) {
            this.mUndoButton.setImageResource(R.drawable.annot_undo_enabled);
            this.mUndoButton.setEnable(true);
        } else {
            this.mUndoButton.setImageResource(R.drawable.annot_undo_pressed);
            this.mUndoButton.setEnable(false);
        }
        if (dm.canRedo()) {
            this.mRedoButton.setImageResource(R.drawable.annot_redo_enabled);
            this.mRedoButton.setEnable(true);
        } else {
            this.mRedoButton.setImageResource(R.drawable.annot_redo_pressed);
            this.mRedoButton.setEnable(false);
        }
        if (((UIExtensionsManager) this.mRead.getDocViewer().getUIExtensionsManager()).getCurrentToolHandler() == null || !((UIExtensionsManager) this.mRead.getDocViewer().getUIExtensionsManager()).getCurrentToolHandler().getType().equals(ToolHandler.TH_TYPE_INK)) {
            this.mUndoButton.getContentView().setVisibility(0);
            this.mRedoButton.getContentView().setVisibility(0);
            return;
        }
        this.mUndoButton.getContentView().setVisibility(4);
        this.mRedoButton.getContentView().setVisibility(4);
    }

    private void addFileAttachmentListener(final FileAttachmentModule module) {
        this.mRead.getMainFrame().getMoreToolsBar().registerListener(new IMT_MoreClickListener() {
            public void onMTClick(int type) {
                if (type == 16) {
                    ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(module.getToolHandler());
                    RD_ReadActivity.this.mRead.changeState(6);
                    RD_ReadActivity.this.resetAnnotBarToFileAttachment(module);
                    module.resetPropertyBar();
                }
            }

            public int getType() {
                return 16;
            }
        });
    }

    private void resetAnnotBarToFileAttachment(final FileAttachmentModule module) {
        this.mPropertyBar = module.getPropertyBar();
        this.mRead.getMainFrame().getToolSetBar().removeAllItems();
        this.mMoreItem = new CircleItemImpl(App.instance().getApplicationContext()) {
            public void onItemLayout(int l, int t, int r, int b) {
                if (((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).getCurrentToolHandler() == module.getToolHandler()) {
                    Rect rect = new Rect();
                    RD_ReadActivity.this.mMoreItem.getContentView().getGlobalVisibleRect(rect);
                    RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().update(new RectF(rect));
                }
            }
        };
        this.mMoreItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_MORE);
        this.mMoreItem.setImageResource(R.drawable.mt_more_selector);
        this.mMoreItem.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Rect rect = new Rect();
                RD_ReadActivity.this.mMoreItem.getContentView().getGlobalVisibleRect(rect);
                RD_ReadActivity.this.mRead.getMainFrame().getMoreToolsBar().show(new RectF(rect), true);
            }
        });
        this.mOKItem = new CircleItemImpl(this);
        this.mOKItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_OK);
        this.mOKItem.setImageResource(R.drawable.rd_annot_create_ok_selector);
        this.mOKItem.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                RD_ReadActivity.this.mRead.changeState(4);
                ((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(null);
            }
        });
        this.mPropertyItem = new PropertyCircleItemImp(App.instance().getApplicationContext()) {
            public void onItemLayout(int l, int t, int r, int b) {
                if (((UIExtensionsManager) RD_ReadActivity.this.mRead.getUIExtensionsManager()).getCurrentToolHandler() == module.getToolHandler() && RD_ReadActivity.this.mPropertyBar.isShowing()) {
                    Rect rect = new Rect();
                    RD_ReadActivity.this.mPropertyItem.getContentView().getGlobalVisibleRect(rect);
                    RD_ReadActivity.this.mPropertyBar.update(new RectF(rect));
                }
            }
        };
        this.mPropertyItem.setTag(ToolbarItemConfig.ITEM_PROPERTY_TAG);
        this.mPropertyItem.setCentreCircleColor(((FileAttachmentToolHandler) module.getToolHandler()).getColor());
        final Rect rect = new Rect();
        this.mPropertyItem.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                RD_ReadActivity.this.mPropertyBar.setArrowVisible(true);
                RD_ReadActivity.this.mPropertyItem.getContentView().getGlobalVisibleRect(rect);
                RD_ReadActivity.this.mPropertyBar.show(new RectF(rect), true);
            }
        });
        module.setColorChangeListener(new FileAttachmentModule.ColorChangeListener() {
            public void onColorChange(int color) {
                RD_ReadActivity.this.mPropertyItem.setCentreCircleColor(color);
            }
        });
        this.mContinuousCreateItem = new CircleItemImpl(App.instance().getApplicationContext());
        this.mContinuousCreateItem.setTag(ToolbarItemConfig.ANNOT_BAR_ITEM_CONTINUE);
        this.mIsContinuousCreate = false;
        ((FileAttachmentToolHandler) module.getToolHandler()).setIsContinuousCreate(false);
        if (this.mIsContinuousCreate) {
            this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_true_selector);
        } else {
            this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_false_selector);
        }
        this.mContinuousCreateItem.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!AppUtil.isFastDoubleClick()) {
                    if (RD_ReadActivity.this.mIsContinuousCreate) {
                        RD_ReadActivity.this.mIsContinuousCreate = true;
                        ((FileAttachmentToolHandler) module.getToolHandler()).setIsContinuousCreate(RD_ReadActivity.this.mIsContinuousCreate);
                        RD_ReadActivity.this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_false_selector);
                    } else {
                        RD_ReadActivity.this.mIsContinuousCreate = true;
                        ((FileAttachmentToolHandler) module.getToolHandler()).setIsContinuousCreate(RD_ReadActivity.this.mIsContinuousCreate);
                        RD_ReadActivity.this.mContinuousCreateItem.setImageResource(R.drawable.rd_annot_create_continuously_true_selector);
                    }
                    AppAnnotUtil.getInstance(RD_ReadActivity.this.getApplicationContext()).showAnnotContinueCreateToast(RD_ReadActivity.this.mIsContinuousCreate);
                }
            }
        });
        this.mRead.getMainFrame().getToolSetBar().addView(this.mMoreItem, TB_Position.Position_CENTER);
        this.mRead.getMainFrame().getToolSetBar().addView(this.mPropertyItem, TB_Position.Position_CENTER);
        this.mRead.getMainFrame().getToolSetBar().addView(this.mOKItem, TB_Position.Position_CENTER);
        this.mRead.getMainFrame().getToolSetBar().addView(this.mContinuousCreateItem, TB_Position.Position_CENTER);
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (this.mRead.getMainFrame().getAttachedActivity() == this) {
            setDocumentPath(intent);
            addToolListener();
            registerOtherModule();
            App.instance().loadModules();
            openDocument(null);
        }
    }

    void setDocumentPath(Intent intent) {
        this.mFilePath = getFilePath(this, intent);
        this.mRead.setDocPath(this.mFilePath);
    }

    void openDocument(byte[] password) {
        this.mRead.getDocViewer().registerDocEventListener(this.docEventListener);
        this.mRead.getDocViewer().openDoc(this.mFilePath, password);
    }

    protected static String getFilePath(Context context, Intent intent) {
        String filePath = null;
        if ("android.intent.action.VIEW".equals(intent.getAction())) {
            Uri uri = intent.getData();
            if (uri.getScheme().equals("file")) {
                filePath = uri.getPath();
            } else {
                try {
                    FileDescriptor fileDescriptor;
                    ContentResolver resolver = context.getContentResolver();
                    projections = new String[2][];
                    projections[0] = new String[]{MediaColumns.DISPLAY_NAME};
                    projections[1] = new String[]{MediaMetadataRetriever.METADATA_KEY_FILENAME};
                    String filename = null;
                    for (String[] projection : projections) {
                        try {
                            Cursor cursor = resolver.query(uri, projection, null, null, null);
                            if (cursor == null) {
                                continue;
                            } else {
                                cursor.moveToFirst();
                                int column_index = cursor.getColumnIndex(projection[0]);
                                if (column_index >= 0) {
                                    filename = cursor.getString(column_index);
                                    cursor.close();
                                    break;
                                }
                                cursor.close();
                            }
                        } catch (Exception e) {
                        }
                    }
                    if (filename == null) {
                        filename = AppDmUtil.randomUUID(null) + ".pdf";
                    }
                    ParcelFileDescriptor parcelFileDescriptor = resolver.openFileDescriptor(uri, "r");
                    if (parcelFileDescriptor != null) {
                        fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                    } else {
                        fileDescriptor = null;
                    }
                    filePath = cacheContentPdfFile(context, fileDescriptor, filename);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        if (filePath != null) {
            return filePath;
        }
        return intent.getStringExtra("filePath");
    }

    private static String cacheContentPdfFile(Context context, FileDescriptor fileDesp, String filename) {
        File contentFile = null;
        String cacheDir = new StringBuilder(String.valueOf(context.getCacheDir().getPath())).append("/contentfile").toString();
        File contentDirFile = new File(cacheDir);
        if (contentDirFile.exists()) {
            AppFileUtil.deleteFolder(contentDirFile, false);
        }
        contentDirFile.mkdirs();
        if (contentDirFile.exists() && contentDirFile.isDirectory()) {
            contentFile = new File(new StringBuilder(String.valueOf(cacheDir)).append("/").append(filename).toString());
        }
        if (contentFile == null) {
            return null;
        }
        try {
            if (contentFile.exists()) {
                contentFile.delete();
            }
            contentFile.createNewFile();
            FileInputStream fis = new FileInputStream(fileDesp);
            FileOutputStream fos = new FileOutputStream(contentFile);
            byte[] read = new byte[8192];
            while (true) {
                int byteCount = fis.read(read);
                if (byteCount <= 0) {
                    fis.close();
                    fos.flush();
                    fos.close();
                    return contentFile.getAbsolutePath();
                }
                fos.write(read, 0, byteCount);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected void onStart() {
        super.onStart();
        this.mRead.onStart(this);
    }

    protected void onPause() {
        super.onPause();
        this.mBroadcastIntent.setAction("com.nvidia.intent.action.ENABLE_STYLUS");
        this.mBroadcastIntent.putExtra("package", "");
        sendBroadcast(this.mBroadcastIntent);
        this.mRead.onPause(this);
    }

    protected void onResume() {
        super.onResume();
        this.mBroadcastIntent.setAction("com.nvidia.intent.action.ENABLE_STYLUS");
        this.mBroadcastIntent.putExtra("package", getPackageName());
        sendBroadcast(this.mBroadcastIntent);
        this.mRead.onResume(this);
    }

    protected void onStop() {
        super.onStop();
        this.mRead.onStop(this);
    }

    protected void onDestroy() {
        getWindow().clearFlags(128);
        App.instance().unloadModules();
        this.mRead.onDestroy(this);
        App.instance().getDialogManager().closeAllDialog();
        super.onDestroy();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.mRead.onActivityResult(this, requestCode, resultCode, data);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ((RD_MainFrame) this.mRead.getMainFrame()).updateSettingBar();
        if (this.mPropertyBar != null) {
            ((PropertyBarImpl) this.mPropertyBar).onConfigurationChanged(null);
        }
        this.mRead.onConfigurationChanged(this, newConfig);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this.mRead.onKeyDown(this, keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        if (this.mRead.onPrepareOptionsMenu(this, menu)) {
            return super.onPrepareOptionsMenu(menu);
        }
        return false;
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        App.instance().getEventManager().triggerInteractTimer();
        return super.dispatchTouchEvent(ev);
    }

    public void onUserInteraction() {
        super.onUserInteraction();
        App.instance().getEventManager().triggerInteractTimer();
    }
}
