package com.foxit.uiextensions.annots.fileattachment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.PDFViewCtrl.IDocEventListener;
import com.foxit.sdk.PDFViewCtrl.IDrawEventListener;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.controls.propertybar.PropertyBar.PropertyChangeListener;
import com.foxit.uiextensions.controls.propertybar.imp.PropertyBarImpl;
import com.foxit.uiextensions.utils.ToolUtil;

public class FileAttachmentModule implements Module, PropertyChangeListener {
    private FileAttachmentPBAdapter mAdapter;
    private FileAttachmentAnnotHandler mAnnotHandler;
    private ColorChangeListener mColorChangeListener = null;
    private Context mContext;
    private int mCurrentColor;
    private int mCurrentOpacity;
    private IDocEventListener mDocEventListener = new IDocEventListener() {
        public void onDocWillOpen() {
        }

        public void onDocOpened(PDFDoc doc, int err) {
            if (!FileAttachmentModule.this.isAttachmentOpening()) {
                FileAttachmentModule.this.mAnnotHandler.deleteTMP_PATH();
            }
        }

        public void onDocWillClose(PDFDoc doc) {
        }

        public void onDocClosed(PDFDoc doc, int err) {
        }

        public void onDocWillSave(PDFDoc document) {
        }

        public void onDocSaved(PDFDoc document, int errCode) {
        }
    };
    private IDrawEventListener mDrawEventListener = new IDrawEventListener() {
        public void onDraw(int pageIndex, Canvas canvas) {
            FileAttachmentModule.this.mAnnotHandler.onDrawForControls(canvas);
        }
    };
    private int mFlagType;
    private LinearLayout mIconItem_ly;
    private int[] mPBColors = new int[PropertyBar.PB_COLORS_FILEATTACHMENT.length];
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private PropertyBar mPropertyBar;
    private FileAttachmentToolHandler mToolHandler;
    private String[] mTypeNames;
    private int[] mTypePicIds = new int[]{R.drawable.pb_fat_type_graph, R.drawable.pb_fat_type_paperclip, R.drawable.pb_fat_type_pushpin, R.drawable.pb_fat_type_tag};
    private UIExtensionsManager mUIExtensionsManager;

    public interface ColorChangeListener {
        void onColorChange(int i);
    }

    public interface IAttachmentDocEvent {
        void onAttachmentDocClosed();

        void onAttachmentDocOpened(PDFDoc pDFDoc, int i);

        void onAttachmentDocWillClose();

        void onAttachmentDocWillOpen();
    }

    public String getName() {
        return Module.MODULE_NAME_FILEATTACHMENT;
    }

    public FileAttachmentModule(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
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

    public boolean loadModule() {
        this.mToolHandler = new FileAttachmentToolHandler(this.mContext, this.mPdfViewCtrl);
        this.mCurrentColor = PropertyBar.PB_COLORS_FILEATTACHMENT[0];
        this.mCurrentOpacity = 100;
        this.mFlagType = 2;
        this.mToolHandler.setPaint(this.mCurrentColor, this.mCurrentOpacity, this.mFlagType);
        initView();
        this.mAnnotHandler = new FileAttachmentAnnotHandler(this.mContext, this.mParent, this.mPdfViewCtrl, this);
        this.mAnnotHandler.setToolHandler(this.mToolHandler);
        this.mAnnotHandler.setPropertyBarIconLayout(this.mIconItem_ly);
        this.mAnnotHandler.setPropertyListViewAdapter(this.mAdapter);
        this.mPdfViewCtrl.registerDocEventListener(this.mDocEventListener);
        this.mPdfViewCtrl.registerDrawEventListener(this.mDrawEventListener);
        return true;
    }

    public boolean unloadModule() {
        this.mPdfViewCtrl.unregisterDocEventListener(this.mDocEventListener);
        this.mPdfViewCtrl.unregisterDrawEventListener(this.mDrawEventListener);
        return true;
    }

    public void setColorChangeListener(ColorChangeListener listener) {
        this.mColorChangeListener = listener;
    }

    public void onValueChanged(long property, int value) {
        this.mUIExtensionsManager = (UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager();
        AnnotHandler currentAnnotHandler = ToolUtil.getCurrentAnnotHandler(this.mUIExtensionsManager);
        ToolHandler currentToolHandler = this.mUIExtensionsManager.getCurrentToolHandler();
        if (property == 1 || property == 128) {
            if (currentToolHandler == this.mToolHandler) {
                this.mCurrentColor = value;
                this.mToolHandler.setPaint(this.mCurrentColor, this.mCurrentOpacity, this.mFlagType);
            } else if (currentAnnotHandler == this.mAnnotHandler) {
                this.mAnnotHandler.modifyAnnotColor(value);
                this.mToolHandler.setColor(value);
            }
            if (this.mColorChangeListener != null) {
                this.mColorChangeListener.onColorChange(this.mCurrentColor);
            }
        } else if (property != 2) {
        } else {
            if (currentToolHandler == this.mToolHandler) {
                this.mCurrentOpacity = value;
                this.mToolHandler.setPaint(this.mCurrentColor, this.mCurrentOpacity, this.mFlagType);
            } else if (currentAnnotHandler == this.mAnnotHandler) {
                this.mAnnotHandler.modifyAnnotOpacity(value);
            }
        }
    }

    public void onValueChanged(long property, float value) {
    }

    public void onValueChanged(long property, String value) {
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return this.mToolHandler.onKeyDown(keyCode, event) || this.mAnnotHandler.onKeyDown(keyCode, event);
    }

    public void resetPropertyBar() {
        FileAttachmentToolHandler toolHandler = (FileAttachmentToolHandler) getToolHandler();
        System.arraycopy(PropertyBar.PB_COLORS_FILEATTACHMENT, 0, this.mPBColors, 0, this.mPBColors.length);
        this.mPBColors[0] = PropertyBar.PB_COLORS_FILEATTACHMENT[0];
        this.mPropertyBar.setColors(this.mPBColors);
        this.mPropertyBar.setProperty(1, toolHandler.getColor());
        this.mPropertyBar.setProperty(2, toolHandler.getOpacity());
        this.mPropertyBar.reset(3);
        this.mPropertyBar.setPropertyChangeListener(this);
        this.mPropertyBar.addTab("", 0, this.mContext.getString(R.string.pb_type_tab), 0);
        this.mPropertyBar.addCustomItem(PropertyBar.PROPERTY_FILEATTACHMENT, this.mIconItem_ly, 0, 0);
    }

    private void initView() {
        this.mPropertyBar = new PropertyBarImpl(this.mContext, this.mPdfViewCtrl, this.mParent);
        this.mIconItem_ly = new LinearLayout(this.mContext);
        this.mIconItem_ly.setLayoutParams(new LayoutParams(-1, -1));
        this.mIconItem_ly.setGravity(17);
        this.mIconItem_ly.setOrientation(0);
        ListView listView = new ListView(this.mContext);
        listView.setLayoutParams(new LayoutParams(-1, -1));
        listView.setCacheColorHint(this.mContext.getResources().getColor(R.color.ux_color_translucent));
        listView.setDivider(new ColorDrawable(this.mContext.getResources().getColor(R.color.ux_color_seperator_gray)));
        listView.setDividerHeight(1);
        this.mIconItem_ly.addView(listView);
        this.mTypeNames = FileAttachmentUtil.getIconNames();
        this.mAdapter = new FileAttachmentPBAdapter(this.mContext, this.mTypePicIds, this.mTypeNames);
        this.mAdapter.setNoteIconType(this.mFlagType);
        listView.setAdapter(this.mAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                FileAttachmentModule.this.mFlagType = position;
                FileAttachmentModule.this.mAdapter.setNoteIconType(FileAttachmentModule.this.mFlagType);
                FileAttachmentModule.this.mAdapter.notifyDataSetChanged();
                FileAttachmentModule.this.mAnnotHandler.modifyIconType(FileAttachmentModule.this.mFlagType);
                FileAttachmentModule.this.mToolHandler.setIconName(FileAttachmentUtil.getIconNames()[FileAttachmentModule.this.mFlagType]);
            }
        });
        resetPropertyBar();
    }

    public PropertyBar getPropertyBar() {
        return this.mPropertyBar;
    }

    public boolean isAttachmentOpening() {
        return this.mAnnotHandler.isAttachmentOpening();
    }

    public void registerAttachmentDocEventListener(IAttachmentDocEvent listener) {
        this.mAnnotHandler.registerAttachmentDocEventListener(listener);
    }
}
