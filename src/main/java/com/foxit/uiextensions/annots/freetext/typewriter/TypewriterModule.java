package com.foxit.uiextensions.annots.freetext.typewriter;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.PDFViewCtrl.IDrawEventListener;
import com.foxit.sdk.PDFViewCtrl.IPageEventListener;
import com.foxit.sdk.PDFViewCtrl.IRecoveryEventListener;
import com.foxit.sdk.common.PDFException;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.controls.propertybar.PropertyBar.PropertyChangeListener;
import com.foxit.uiextensions.controls.propertybar.imp.AnnotMenuImpl;
import com.foxit.uiextensions.controls.propertybar.imp.PropertyBarImpl;
import com.foxit.uiextensions.controls.toolbar.BaseItem;
import com.foxit.uiextensions.controls.toolbar.ToolbarItemConfig;
import com.foxit.uiextensions.controls.toolbar.impl.CircleItemImpl;
import com.foxit.uiextensions.utils.OnPageEventListener;
import com.foxit.uiextensions.utils.ToolUtil;

public class TypewriterModule implements Module, PropertyChangeListener {
    private TypewriterAnnotHandler mAnnotHandler;
    private ColorChangeListener mColorChangeListener = null;
    private Context mContext;
    private int mCurrentColor;
    private String mCurrentFontName;
    private float mCurrentFontSize;
    private int mCurrentOpacity;
    private IDrawEventListener mDrawEventListener = new IDrawEventListener() {
        public void onDraw(int pageIndex, Canvas canvas) {
            TypewriterModule.this.mAnnotHandler.onDrawForControls(canvas);
        }
    };
    private IPageEventListener mPageEventListener = new OnPageEventListener() {
        public void onPageChanged(int oldPageIndex, int curPageIndex) {
            UIExtensionsManager uiExtensionsManager = (UIExtensionsManager) TypewriterModule.this.mPdfViewCtrl.getUIExtensionsManager();
            AnnotHandler currentAnnotHandler = ToolUtil.getCurrentAnnotHandler(uiExtensionsManager);
            if (!(uiExtensionsManager.getCurrentToolHandler() == null || uiExtensionsManager.getCurrentToolHandler() != TypewriterModule.this.mToolHandler || TypewriterModule.this.mToolHandler.mLastPageIndex == -1 || TypewriterModule.this.mToolHandler.mLastPageIndex == curPageIndex)) {
                uiExtensionsManager.setCurrentToolHandler(null);
            }
            if (DocumentManager.getInstance(TypewriterModule.this.mPdfViewCtrl).getCurrentAnnot() != null && currentAnnotHandler == TypewriterModule.this.mAnnotHandler) {
                try {
                    if (DocumentManager.getInstance(TypewriterModule.this.mPdfViewCtrl).getCurrentAnnot().getPage().getIndex() != curPageIndex) {
                        DocumentManager.getInstance(TypewriterModule.this.mPdfViewCtrl).setCurrentAnnot(null);
                    }
                } catch (PDFException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private BaseItem mToolBtn;
    private TypewriterToolHandler mToolHandler;
    IRecoveryEventListener memoryEventListener = new IRecoveryEventListener() {
        public void onWillRecover() {
            if (TypewriterModule.this.mAnnotHandler.getAnnotMenu() != null && TypewriterModule.this.mAnnotHandler.getAnnotMenu().isShowing()) {
                TypewriterModule.this.mAnnotHandler.getAnnotMenu().dismiss();
            }
            if (TypewriterModule.this.mAnnotHandler.getPropertyBar() != null && TypewriterModule.this.mAnnotHandler.getPropertyBar().isShowing()) {
                TypewriterModule.this.mAnnotHandler.getPropertyBar().dismiss();
            }
        }

        public void onRecovered() {
        }
    };

    public interface ColorChangeListener {
        void onColorChange(int i);
    }

    public TypewriterModule(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mParent = parent;
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public ToolHandler getToolHandler() {
        return this.mToolHandler;
    }

    public AnnotHandler getAnnotHandler() {
        return this.mAnnotHandler;
    }

    public String getName() {
        return Module.MODULE_NAME_TYPEWRITER;
    }

    public boolean loadModule() {
        this.mToolHandler = new TypewriterToolHandler(this.mContext, this.mPdfViewCtrl, this.mParent);
        this.mAnnotHandler = new TypewriterAnnotHandler(this.mContext, this.mPdfViewCtrl, this.mParent);
        this.mAnnotHandler.setPropertyChangeListener(this);
        this.mAnnotHandler.setAnnotMenu(new AnnotMenuImpl(this.mContext, this.mParent));
        this.mAnnotHandler.setPropertyBar(new PropertyBarImpl(this.mContext, this.mPdfViewCtrl, this.mParent));
        this.mPdfViewCtrl.registerRecoveryEventListener(this.memoryEventListener);
        this.mPdfViewCtrl.registerDrawEventListener(this.mDrawEventListener);
        this.mPdfViewCtrl.registerPageEventListener(this.mPageEventListener);
        initUI();
        return true;
    }

    public boolean unloadModule() {
        this.mAnnotHandler.removePropertyBarListener();
        this.mPdfViewCtrl.unregisterRecoveryEventListener(this.memoryEventListener);
        this.mPdfViewCtrl.unregisterDrawEventListener(this.mDrawEventListener);
        this.mPdfViewCtrl.unregisterPageEventListener(this.mPageEventListener);
        return true;
    }

    private void initUI() {
        initCurrentValue();
        initToolBtn();
    }

    private void initCurrentValue() {
        if (this.mCurrentColor == 0) {
            this.mCurrentColor = PropertyBar.PB_COLORS_TYPEWRITER[0];
        }
        if (this.mCurrentOpacity == 0) {
            this.mCurrentOpacity = 100;
        }
        if (this.mCurrentFontName == null) {
            this.mCurrentFontName = "Courier";
        }
        if (this.mCurrentFontSize == 0.0f) {
            this.mCurrentFontSize = 24.0f;
        }
        this.mToolHandler.onColorValueChanged(this.mCurrentColor);
        this.mToolHandler.onOpacityValueChanged(this.mCurrentOpacity);
        this.mToolHandler.onFontValueChanged(this.mCurrentFontName);
        this.mToolHandler.onFontSizeValueChanged(this.mCurrentFontSize);
    }

    private void initToolBtn() {
        this.mToolBtn = new CircleItemImpl(this.mContext);
        this.mToolBtn.setImageResource(R.drawable.annot_typewriter_selector);
        this.mToolBtn.setTag(ToolbarItemConfig.ANNOTS_BAR_ITEM_TYPEWRITE);
        if (!(this.mPdfViewCtrl == null || DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot())) {
            this.mToolBtn.setEnable(false);
        }
        this.mToolBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!DocumentManager.getInstance(TypewriterModule.this.mPdfViewCtrl).canAddAnnot()) {
                    return;
                }
                if (((UIExtensionsManager) TypewriterModule.this.mPdfViewCtrl.getUIExtensionsManager()).getCurrentToolHandler() != TypewriterModule.this.mToolHandler) {
                    ((UIExtensionsManager) TypewriterModule.this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(TypewriterModule.this.mToolHandler);
                    TypewriterModule.this.mToolBtn.setSelected(true);
                    return;
                }
                ((UIExtensionsManager) TypewriterModule.this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
                TypewriterModule.this.mToolBtn.setSelected(false);
            }
        });
        this.mToolBtn.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                if (DocumentManager.getInstance(TypewriterModule.this.mPdfViewCtrl).canAddAnnot() && ((UIExtensionsManager) TypewriterModule.this.mPdfViewCtrl.getUIExtensionsManager()).getCurrentToolHandler() != TypewriterModule.this.mToolHandler) {
                    ((UIExtensionsManager) TypewriterModule.this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(TypewriterModule.this.mToolHandler);
                    TypewriterModule.this.mToolBtn.setSelected(true);
                }
                return true;
            }
        });
    }

    public void setColorChangeListener(ColorChangeListener listener) {
        this.mColorChangeListener = listener;
    }

    public void onValueChanged(long property, int value) {
        UIExtensionsManager uiExtensionsManager = (UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager();
        AnnotHandler currentAnnotHandler = ToolUtil.getCurrentAnnotHandler(uiExtensionsManager);
        if (property == 1 || property == 128) {
            if (uiExtensionsManager.getCurrentToolHandler() == this.mToolHandler) {
                this.mCurrentColor = value;
                this.mToolHandler.onColorValueChanged(value);
            }
            if (currentAnnotHandler == this.mAnnotHandler) {
                this.mAnnotHandler.onColorValueChanged(value);
            }
            if (this.mColorChangeListener != null) {
                this.mColorChangeListener.onColorChange(value);
            }
        } else if (property != 2) {
        } else {
            if (uiExtensionsManager.getCurrentToolHandler() == this.mToolHandler) {
                this.mCurrentOpacity = value;
                this.mToolHandler.onOpacityValueChanged(value);
            } else if (currentAnnotHandler == this.mAnnotHandler) {
                this.mAnnotHandler.onOpacityValueChanged(value);
            }
        }
    }

    public void onValueChanged(long property, float value) {
        UIExtensionsManager uiExtensionsManager = (UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager();
        AnnotHandler currentAnnotHandler = ToolUtil.getCurrentAnnotHandler(uiExtensionsManager);
        if (property != 16) {
            return;
        }
        if (uiExtensionsManager.getCurrentToolHandler() == this.mToolHandler) {
            this.mCurrentFontSize = value;
            this.mToolHandler.onFontSizeValueChanged(value);
        } else if (currentAnnotHandler == this.mAnnotHandler) {
            this.mAnnotHandler.onFontSizeValueChanged(value);
        }
    }

    public void onValueChanged(long property, String value) {
        UIExtensionsManager uiExtensionsManager = (UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager();
        AnnotHandler currentAnnotHandler = ToolUtil.getCurrentAnnotHandler(uiExtensionsManager);
        if (property != 8) {
            return;
        }
        if (uiExtensionsManager.getCurrentToolHandler() == this.mToolHandler) {
            this.mCurrentFontName = value;
            this.mToolHandler.onFontValueChanged(value);
        } else if (currentAnnotHandler == this.mAnnotHandler) {
            this.mAnnotHandler.onFontValueChanged(value);
        }
    }
}
