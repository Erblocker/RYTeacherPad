package com.foxit.uiextensions.annots;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.internal.view.SupportMenu;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.controls.propertybar.PropertyBar.PropertyChangeListener;
import com.foxit.uiextensions.controls.toolbar.CircleItem;
import com.foxit.uiextensions.controls.toolbar.PropertyCircleItem;
import com.foxit.uiextensions.utils.AppDisplay;
import java.util.ArrayList;

public abstract class AbstractToolHandler implements ToolHandler, PropertyChangeListener {
    protected int mBtnTag;
    protected int mColor;
    private ColorChangeListener mColorChangeListener = null;
    protected Context mContext;
    protected CircleItem mContinuousBtn;
    protected int mCustomColor;
    protected ArrayList<Integer> mDisplayIcons;
    protected String mDisplayName;
    protected boolean mIsContinuousCreate;
    protected CircleItem mMoreToolsBtn;
    protected CircleItem mOkBtn;
    protected int mOpacity;
    protected ViewGroup mParent;
    protected PDFViewCtrl mPdfViewCtrl;
    protected PropertyBar mPropertyBar;
    protected PropertyCircleItem mPropertyBtn;
    protected String mPropertyKey;
    protected float mThickness;
    protected String mToolName;

    public interface ColorChangeListener {
        void onColorChange(int i);
    }

    public abstract long getSupportedProperties();

    protected abstract void setPaintProperty(PDFViewCtrl pDFViewCtrl, int i, Paint paint);

    public AbstractToolHandler(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl, String name, String propKey) {
        this.mContext = context;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mParent = parent;
        this.mToolName = name;
        this.mPropertyKey = propKey;
        this.mColor = SupportMenu.CATEGORY_MASK;
        this.mCustomColor = SupportMenu.CATEGORY_MASK;
        this.mOpacity = 100;
        this.mThickness = 5.0f;
        this.mDisplayIcons = new ArrayList();
    }

    public void setPropertyBar(PropertyBar propertyBar) {
        this.mPropertyBar = propertyBar;
    }

    public PropertyBar getPropertyBar() {
        return this.mPropertyBar;
    }

    protected void addToolButton(int tag) {
        this.mBtnTag = tag;
    }

    protected void removeToolButton() {
    }

    public void updateToolButtonStatus() {
    }

    public int getCustomColor() {
        return this.mCustomColor;
    }

    public void setCustomColor(int color) {
        this.mCustomColor = color;
    }

    public int getColor() {
        return this.mColor;
    }

    public void setColor(int color) {
        if (this.mColor != color) {
            this.mColor = color;
        }
    }

    public int getOpacity() {
        return this.mOpacity;
    }

    public void setOpacity(int opacity) {
        if (this.mOpacity != opacity) {
            this.mOpacity = opacity;
        }
    }

    public float getThickness() {
        return this.mThickness;
    }

    public void setThickness(float thickness) {
        if (this.mThickness != thickness) {
            this.mThickness = thickness;
        }
    }

    public String getFontName() {
        return null;
    }

    public void setFontName(String name) {
    }

    public float getFontSize() {
        return 0.0f;
    }

    public void setFontSize(float size) {
    }

    public void setColorChangeListener(ColorChangeListener listener) {
        this.mColorChangeListener = listener;
    }

    public void onValueChanged(long property, int value) {
        if (property == 1) {
            setColor(value);
            if (this.mColorChangeListener != null) {
                this.mColorChangeListener.onColorChange(value);
            }
        } else if (property == 128) {
            setCustomColor(value);
            setColor(value);
            if (this.mColorChangeListener != null) {
                this.mColorChangeListener.onColorChange(value);
            }
        } else if (property == 2) {
            setOpacity(value);
        }
    }

    public void onValueChanged(long property, float value) {
        if (property == 4) {
            setThickness(value);
        }
    }

    public void onValueChanged(long property, String value) {
    }

    public String getType() {
        return this.mToolName;
    }

    public boolean onTouchEvent(int pageIndex, MotionEvent motionEvent) {
        return false;
    }

    public boolean onPrepareOptionsMenu() {
        if (((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getCurrentToolHandler() == this) {
            return false;
        }
        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getCurrentToolHandler() != this || keyCode != 4) {
            return false;
        }
        ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
        return true;
    }

    public void onConfigurationChanged(Configuration newConfig) {
    }

    public void onStatusChanged(int oldState, int newState) {
        updateToolButtonStatus();
    }

    protected void showPropertyBar(long curProperty) {
        setPropertyBarProperties(this.mPropertyBar);
        this.mPropertyBar.setPropertyChangeListener(this);
        this.mPropertyBar.reset(getSupportedProperties());
        Rect rect = new Rect();
        this.mPropertyBtn.getContentView().getGlobalVisibleRect(rect);
        if (AppDisplay.getInstance(this.mContext).isPad()) {
            this.mPropertyBar.show(new RectF(rect), true);
        } else {
            this.mPropertyBar.show(new RectF(rect), true);
        }
    }

    protected void hidePropertyBar() {
        this.mPropertyBar.setPropertyChangeListener(null);
        if (this.mPropertyBar.isShowing()) {
            this.mPropertyBar.dismiss();
        }
    }

    protected void setPropertyBarProperties(PropertyBar propertyBar) {
        propertyBar.setProperty(1, getColor());
        propertyBar.setProperty(2, getOpacity());
        propertyBar.setProperty(4, getThickness());
        if (AppDisplay.getInstance(this.mContext).isPad()) {
            propertyBar.setArrowVisible(true);
        } else {
            propertyBar.setArrowVisible(false);
        }
    }

    public boolean getIsContinuousCreate() {
        return this.mIsContinuousCreate;
    }

    public void setIsContinuousCreate(boolean isContinuousCreate) {
        this.mIsContinuousCreate = isContinuousCreate;
    }
}
