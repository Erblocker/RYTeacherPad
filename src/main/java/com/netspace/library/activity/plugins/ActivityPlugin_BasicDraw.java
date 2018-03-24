package com.netspace.library.activity.plugins;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.NovaIcons;
import com.netspace.library.bluetooth.BlueToothPen;
import com.netspace.library.bluetooth.BlueToothPen.PenActionInterface;
import com.netspace.library.components.DrawComponent;
import com.netspace.library.components.DrawComponent.DrawComponentGraphic;
import com.netspace.library.controls.CustomGraphicCanvas;
import com.netspace.library.controls.DrawView;
import com.netspace.library.controls.FriendlyPoint;
import com.netspace.library.controls.MoveableObject;
import com.netspace.library.controls.Point;
import com.netspace.library.dialog.ColorPickerDialog;
import com.netspace.library.dialog.ColorPickerDialog.OnColorChangedListener;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import java.util.ArrayList;

public class ActivityPlugin_BasicDraw extends ActivityPluginBase implements OnClickListener, OnLongClickListener, PenActionInterface {
    private ImageView mBestFitButton;
    private BlueToothPen mBlueToothPen;
    private ImageView mBrushButton;
    private ImageView mColorButton;
    private DrawView mDrawView;
    private ImageView mEraseButton;
    private CustomGraphicCanvas mGraphicCanvas;
    private ImageView mGraphicsButton;
    private ImageView mGraphicsCancelButton;
    private ImageView mGraphicsOKButton;
    private Runnable mHidePointRunnable = new Runnable() {
        public void run() {
            ActivityPlugin_BasicDraw.this.mPointer.setVisibility(4);
        }
    };
    private Point mLastPoint;
    private ImageView mPencialButton;
    private ImageView mPointer;
    private RelativeLayout mRelativeLayout;
    private ImageView mRotateButton;
    private ImageView mTextButton;
    private TextView mTextView;
    private ViewGroup mTools;

    public ActivityPlugin_BasicDraw(Activity activity, RelativeLayout relativeLayout, TextView textView, DrawView targetView, ViewGroup tools) {
        super(activity);
        this.mDrawView = targetView;
        this.mTools = tools;
        this.mRelativeLayout = relativeLayout;
        this.mTextView = textView;
        addButtonWithTooltip(R.id.buttonBrush, "粗笔输入", tools);
        addButtonWithTooltip(R.id.buttonPencial, "细笔输入", tools);
        if (this.mTextView != null) {
            addButtonWithTooltip(R.id.buttonText, "文字输入", tools);
        }
        addButtonWithTooltip(R.id.buttonEraser, "橡皮擦，长按清除全部内容", tools);
        addButtonWithTooltip(R.id.buttonColorize, "选择调色", tools);
        addButtonWithTooltip(R.id.buttonGraphics, "几何画板", tools);
        addButtonWithTooltip(R.id.buttonGraphicOK, "保存几何画板内容", tools);
        addButtonWithTooltip(R.id.buttonGraphicCancel, "取消画板内容", tools);
        addButtonWithTooltip(R.id.buttonRotate, "旋转画布", tools);
        addButtonWithTooltip(R.id.buttonBestFit, "最适合大小", tools);
        if (this.mTextView != null) {
            this.mTextButton = (ImageView) tools.findViewById(R.id.buttonText);
            this.mTextButton.setImageDrawable(new IconDrawable((Context) activity, NovaIcons.nova_icon_text_input_1).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
            this.mTextButton.setOnClickListener(this);
            this.mTextView.setOnTouchListener(new MoveableObject(activity, null));
        }
        this.mPencialButton = (ImageView) tools.findViewById(R.id.buttonPencial);
        this.mPencialButton.setImageDrawable(new IconDrawable((Context) activity, NovaIcons.nova_icon_pencil_3).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        this.mPencialButton.setOnClickListener(this);
        this.mEraseButton = (ImageView) tools.findViewById(R.id.buttonEraser);
        this.mEraseButton.setImageDrawable(new IconDrawable((Context) activity, NovaIcons.nova_icon_eraser).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        this.mEraseButton.setOnClickListener(this);
        this.mEraseButton.setOnLongClickListener(this);
        this.mBrushButton = (ImageView) tools.findViewById(R.id.buttonBrush);
        this.mBrushButton.setImageDrawable(new IconDrawable((Context) activity, NovaIcons.nova_icon_paint_brush_1).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        this.mBrushButton.setOnClickListener(this);
        this.mColorButton = (ImageView) tools.findViewById(R.id.buttonColorize);
        this.mColorButton.setImageDrawable(new IconDrawable((Context) activity, NovaIcons.nova_icon_paint_palette).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        this.mColorButton.setOnClickListener(this);
        this.mRotateButton = (ImageView) tools.findViewById(R.id.buttonRotate);
        this.mRotateButton.setImageDrawable(new IconDrawable((Context) activity, NovaIcons.nova_icon_mobile_phone_rotate_1).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        this.mRotateButton.setOnClickListener(this);
        this.mGraphicsButton = (ImageView) tools.findViewById(R.id.buttonGraphics);
        this.mGraphicsButton.setImageDrawable(new IconDrawable((Context) activity, NovaIcons.nova_icon_ruler_2).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        this.mGraphicsButton.setOnClickListener(this);
        this.mGraphicsOKButton = (ImageView) tools.findViewById(R.id.buttonGraphicOK);
        this.mGraphicsOKButton.setImageDrawable(new IconDrawable((Context) activity, NovaIcons.nova_icon_check_circle_2).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        this.mGraphicsOKButton.setOnClickListener(this);
        this.mGraphicsCancelButton = (ImageView) tools.findViewById(R.id.buttonGraphicCancel);
        this.mGraphicsCancelButton.setImageDrawable(new IconDrawable((Context) activity, NovaIcons.nova_icon_close_circle).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        this.mGraphicsCancelButton.setOnClickListener(this);
        this.mBestFitButton = (ImageView) tools.findViewById(R.id.buttonBestFit);
        this.mBestFitButton.setImageDrawable(new IconDrawable((Context) activity, NovaIcons.nova_icon_focus_2).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        this.mBestFitButton.setOnClickListener(this);
        showHideView(this.mGraphicsOKButton, 8);
        showHideView(this.mGraphicsCancelButton, 8);
        this.mBrushButton.performClick();
    }

    private void enableGraphic(boolean bEnable) {
        if (bEnable) {
            this.mGraphicsButton.setEnabled(true);
            this.mGraphicsButton.setAlpha(1.0f);
            return;
        }
        this.mGraphicsButton.setEnabled(false);
        this.mGraphicsButton.setAlpha(0.5f);
    }

    public void setPenPointer(ImageView pointer) {
        this.mPointer = pointer;
        if (this.mBlueToothPen == null && this.mDrawView != null) {
            this.mBlueToothPen = new BlueToothPen();
            this.mBlueToothPen.setCallBack(this);
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.buttonBestFit) {
            doBestFit();
        } else if (v.getId() == R.id.buttonBrush) {
            if (v.isSelected()) {
                this.mPencialButton.setSelected(false);
                this.mEraseButton.setSelected(false);
                this.mBrushButton.setSelected(false);
                this.mDrawView.setBrushMode(false);
                this.mDrawView.setEraseMode(false);
                return;
            }
            this.mBrushButton.setSelected(true);
            this.mEraseButton.setSelected(false);
            this.mPencialButton.setSelected(false);
            this.mDrawView.setEraseMode(false);
            this.mDrawView.setBrushMode(true);
            this.mDrawView.changeWidth(10);
            enableGraphic(true);
        } else if (v.getId() == R.id.buttonEraser) {
            if (v.isSelected()) {
                this.mPencialButton.setSelected(false);
                this.mEraseButton.setSelected(false);
                this.mBrushButton.setSelected(false);
                this.mDrawView.setEraseMode2(false, 0);
                enableGraphic(true);
                return;
            }
            this.mEraseButton.setSelected(true);
            this.mBrushButton.setSelected(false);
            this.mPencialButton.setSelected(false);
            this.mDrawView.setEraseMode2(true, 0);
            enableGraphic(false);
        } else if (v.getId() == R.id.buttonColorize) {
            new ColorPickerDialog(this.mActivity, this.mDrawView.getColor(), "选择颜色", new OnColorChangedListener() {
                public void colorChanged(int color) {
                    ActivityPlugin_BasicDraw.this.mDrawView.setColor(color);
                }
            }).show();
        } else if (v.getId() == R.id.buttonPencial) {
            if (this.mPencialButton.isSelected()) {
                this.mPencialButton.setSelected(false);
                this.mEraseButton.setSelected(false);
                this.mBrushButton.setSelected(false);
                this.mDrawView.setBrushMode(false);
                return;
            }
            this.mPencialButton.setSelected(true);
            this.mEraseButton.setSelected(false);
            this.mBrushButton.setSelected(false);
            this.mDrawView.setEraseMode(false);
            this.mDrawView.setBrushMode(true);
            this.mDrawView.changeWidth(3);
            enableGraphic(true);
        } else if (v.getId() == R.id.buttonText) {
            if (v.isSelected()) {
                this.mTextButton.setSelected(false);
                this.mTextView.setVisibility(4);
                return;
            }
            this.mTextButton.setSelected(true);
            this.mTextView.setVisibility(0);
            this.mTextView.requestFocus();
            enableGraphic(true);
        } else if (v.getId() == R.id.buttonRotate) {
            if (this.mDrawView.getBackground() instanceof BitmapDrawable) {
                boolean bUseInternalBitmap = false;
                Bitmap SourceBitmap = this.mDrawView.getBackgroundBitmap();
                if (SourceBitmap == null) {
                    BitmapDrawable BitmapBackground = (BitmapDrawable) this.mDrawView.getBackground();
                    if (BitmapBackground != null) {
                        SourceBitmap = BitmapBackground.getBitmap();
                    }
                } else {
                    bUseInternalBitmap = true;
                }
                if (SourceBitmap != null) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90.0f);
                    Bitmap rotated = Bitmap.createBitmap(SourceBitmap, 0, 0, SourceBitmap.getWidth(), SourceBitmap.getHeight(), matrix, true);
                    if (bUseInternalBitmap) {
                        this.mDrawView.setBackgroundBitmap(rotated);
                        this.mDrawView.invalidate();
                    } else {
                        this.mDrawView.setBackgroundDrawable(new BitmapDrawable(this.mActivity.getResources(), rotated));
                    }
                    int nTemp = this.mnFullScaleWidth;
                    this.mnFullScaleWidth = this.mnFullScaleHeight;
                    this.mnFullScaleHeight = nTemp;
                    this.mDrawView.setSize(this.mnFullScaleWidth, this.mnFullScaleHeight);
                    SourceBitmap.recycle();
                    doBestFit();
                }
            }
        } else if (v.getId() == R.id.buttonGraphics) {
            PopupMenu popup = new PopupMenu(this.mActivity, v);
            for (int i = 0; i < DrawComponent.getGraphics().size(); i++) {
                popup.getMenu().add(0, i, i, ((DrawComponentGraphic) DrawComponent.getGraphics().get(i)).getName());
            }
            popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    DrawComponentGraphic DrawComponentGraphic = (DrawComponentGraphic) DrawComponent.getGraphics().get(item.getItemId());
                    if (ActivityPlugin_BasicDraw.this.mGraphicCanvas != null) {
                        ActivityPlugin_BasicDraw.this.mRelativeLayout.removeView(ActivityPlugin_BasicDraw.this.mGraphicCanvas);
                        ActivityPlugin_BasicDraw.this.mGraphicCanvas = null;
                    }
                    ActivityPlugin_BasicDraw.this.mGraphicCanvas = new CustomGraphicCanvas(ActivityPlugin_BasicDraw.this.mActivity);
                    ActivityPlugin_BasicDraw.this.mGraphicCanvas.setGraphic(DrawComponentGraphic);
                    ActivityPlugin_BasicDraw.this.mRelativeLayout.addView(ActivityPlugin_BasicDraw.this.mGraphicCanvas, 200, 100);
                    ActivityPlugin_BasicDraw.this.mGraphicCanvas.setBackgroundResource(R.drawable.background_grahpiccanvas);
                    ActivityPlugin_BasicDraw.this.mGraphicCanvas.setDrawView(ActivityPlugin_BasicDraw.this.mDrawView);
                    DrawComponentGraphic.init(ActivityPlugin_BasicDraw.this.mGraphicCanvas);
                    LayoutParams Params = (LayoutParams) ActivityPlugin_BasicDraw.this.mGraphicCanvas.getLayoutParams();
                    Params.topMargin = Math.max(ActivityPlugin_BasicDraw.this.mDrawView.getTop(), 0);
                    Params.leftMargin = Math.max(ActivityPlugin_BasicDraw.this.mDrawView.getLeft(), 0);
                    Params.width = Math.min(ActivityPlugin_BasicDraw.this.mDrawView.getRight() - Params.leftMargin, Utilities.getScreenWidth(ActivityPlugin_BasicDraw.this.mActivity) - ActivityPlugin_BasicDraw.this.mTools.getWidth());
                    Params.height = ActivityPlugin_BasicDraw.this.mDrawView.getBottom() - Params.topMargin;
                    ActivityPlugin_BasicDraw.this.showHideView(ActivityPlugin_BasicDraw.this.mGraphicsOKButton, 0);
                    ActivityPlugin_BasicDraw.this.showHideView(ActivityPlugin_BasicDraw.this.mGraphicsCancelButton, 0);
                    ActivityPlugin_BasicDraw.this.enableOtherButtons(false);
                    return false;
                }
            });
            popup.show();
        } else if (v.getId() == R.id.buttonGraphicOK) {
            if (this.mGraphicCanvas != null) {
                this.mGraphicCanvas.measureDataToDrawView();
                this.mGraphicCanvas.setVisibility(8);
                this.mRelativeLayout.removeView(this.mGraphicCanvas);
                this.mGraphicCanvas = null;
                showHideView(this.mGraphicsOKButton, 8);
                showHideView(this.mGraphicsCancelButton, 8);
            }
            enableOtherButtons(true);
        } else if (v.getId() == R.id.buttonGraphicCancel) {
            if (this.mGraphicCanvas != null) {
                this.mGraphicCanvas.setVisibility(8);
                this.mRelativeLayout.removeView(this.mGraphicCanvas);
                this.mGraphicCanvas = null;
            }
            showHideView(this.mGraphicsOKButton, 8);
            showHideView(this.mGraphicsCancelButton, 8);
            enableOtherButtons(true);
        }
    }

    private void showHideView(ImageView imageView, int nVisibility) {
        ((View) imageView.getParent()).setVisibility(nVisibility);
    }

    public boolean onLongClick(View v) {
        if (v.getId() == R.id.buttonEraser) {
            new Builder(this.mActivity).setTitle("清除确认").setMessage("是否清除全部内容？").setPositiveButton("是", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ActivityPlugin_BasicDraw.this.mDrawView.clearPoints();
                }
            }).setNegativeButton("否", null).show();
        }
        return false;
    }

    public void doBestFit() {
        Display display = ((WindowManager) this.mActivity.getSystemService("window")).getDefaultDisplay();
        int nDisplayWidth = display.getWidth();
        int nDisplayHeight = display.getHeight() - Utilities.getStatusBarHeight(this.mActivity);
        float fScale = 1.0f / Math.max(((float) this.mnFullScaleWidth) / ((float) nDisplayWidth), ((float) this.mnFullScaleHeight) / ((float) nDisplayHeight));
        this.mDrawView.setScale(fScale);
        LayoutParams Param = (LayoutParams) this.mDrawView.getLayoutParams();
        Param.topMargin = (int) ((((float) nDisplayHeight) - (((float) this.mnFullScaleHeight) * fScale)) / 2.0f);
        Param.leftMargin = (int) ((((float) nDisplayWidth) - (((float) this.mnFullScaleWidth) * fScale)) / 2.0f);
        this.mDrawView.setLayoutParams(Param);
    }

    private void enableOtherButtons(boolean bEnable) {
        ArrayList<ImageView> arrButtons = new ArrayList();
        arrButtons.add(this.mBrushButton);
        arrButtons.add(this.mPencialButton);
        arrButtons.add(this.mColorButton);
        arrButtons.add(this.mGraphicsButton);
        arrButtons.add(this.mEraseButton);
        arrButtons.add(this.mTextButton);
        arrButtons.add(this.mRotateButton);
        arrButtons.add(this.mBestFitButton);
        for (int i = 0; i < arrButtons.size(); i++) {
            ImageView oneButton = (ImageView) arrButtons.get(i);
            if (oneButton != null && oneButton.getVisibility() == 0) {
                if (bEnable) {
                    oneButton.setEnabled(true);
                    oneButton.setAlpha(1.0f);
                } else {
                    oneButton.setEnabled(false);
                    oneButton.setAlpha(0.5f);
                }
            }
        }
    }

    public void onPenConnected() {
    }

    public void onPenAction(String szAction, int nX, int nY, float fPressure) {
        boolean bShowPointer = false;
        int nOldX = nX;
        int nOldY = nY;
        if (szAction.equalsIgnoreCase("write")) {
            if (this.mLastPoint != null) {
                final FriendlyPoint friendlyPoint = new FriendlyPoint((float) nX, (float) nY, this.mDrawView.getColor(), this.mLastPoint, (int) (3.0f * fPressure));
                this.mDrawView.post(new Runnable() {
                    public void run() {
                        ActivityPlugin_BasicDraw.this.mDrawView.setEraseMode(false);
                        ActivityPlugin_BasicDraw.this.mDrawView.setBrushMode(true);
                        ActivityPlugin_BasicDraw.this.mDrawView.changeWidth(3);
                        ActivityPlugin_BasicDraw.this.mDrawView.addPoint(friendlyPoint);
                        ActivityPlugin_BasicDraw.this.mDrawView.invalidate();
                    }
                });
                this.mLastPoint = friendlyPoint;
            } else {
                final Point Point = new Point((float) nX, (float) nY, this.mDrawView.getColor(), (int) (3.0f * fPressure));
                this.mDrawView.post(new Runnable() {
                    public void run() {
                        ActivityPlugin_BasicDraw.this.mDrawView.setEraseMode(false);
                        ActivityPlugin_BasicDraw.this.mDrawView.setBrushMode(true);
                        ActivityPlugin_BasicDraw.this.mDrawView.changeWidth(3);
                        ActivityPlugin_BasicDraw.this.mDrawView.addPoint(Point);
                        ActivityPlugin_BasicDraw.this.mDrawView.invalidate();
                    }
                });
                this.mLastPoint = Point;
            }
            bShowPointer = true;
        } else if (szAction.equalsIgnoreCase("move")) {
            bShowPointer = true;
            this.mLastPoint = null;
        } else if (szAction.equalsIgnoreCase("reset")) {
            bShowPointer = false;
            this.mLastPoint = null;
        }
        if (bShowPointer) {
            final int nPointerX = nOldX;
            final int nPointerY = nOldY;
            this.mPointer.post(new Runnable() {
                public void run() {
                    ActivityPlugin_BasicDraw.this.mPointer.setVisibility(0);
                    int nWidth = ActivityPlugin_BasicDraw.this.mPointer.getWidth();
                    int nHeight = ActivityPlugin_BasicDraw.this.mPointer.getHeight();
                    LayoutParams param = (LayoutParams) ActivityPlugin_BasicDraw.this.mPointer.getLayoutParams();
                    param.leftMargin = (ActivityPlugin_BasicDraw.this.mDrawView.getLeft() + ((int) (((float) nPointerX) * ActivityPlugin_BasicDraw.this.mDrawView.getScale()))) - (nWidth / 2);
                    param.topMargin = (ActivityPlugin_BasicDraw.this.mDrawView.getTop() + ((int) (((float) nPointerY) * ActivityPlugin_BasicDraw.this.mDrawView.getScale()))) - (nHeight / 2);
                    ActivityPlugin_BasicDraw.this.mPointer.setLayoutParams(param);
                }
            });
            this.mPointer.removeCallbacks(this.mHidePointRunnable);
            this.mPointer.postDelayed(this.mHidePointRunnable, 5000);
            return;
        }
        this.mPointer.post(new Runnable() {
            public void run() {
                ActivityPlugin_BasicDraw.this.mPointer.setVisibility(4);
            }
        });
    }

    public void onPenDisconnected() {
    }

    public void onPause() {
        if (this.mBlueToothPen != null) {
            this.mBlueToothPen.stop();
        }
    }

    public void onResume() {
        if (this.mBlueToothPen != null) {
            this.mBlueToothPen.start();
        }
    }

    public void onDestroy() {
        if (this.mBlueToothPen != null) {
            this.mBlueToothPen.stop();
            this.mBlueToothPen = null;
        }
    }
}
