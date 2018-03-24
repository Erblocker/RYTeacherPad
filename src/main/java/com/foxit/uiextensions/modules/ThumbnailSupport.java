package com.foxit.uiextensions.modules;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.RecyclerView.State;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.PDFViewCtrl.IPageEventListener;
import com.foxit.sdk.common.PDFException;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.controls.dialog.UITextEditDialog;
import com.foxit.uiextensions.controls.dialog.fileselect.UIFileSelectDialog;
import com.foxit.uiextensions.controls.dialog.fileselect.UIFolderSelectDialog;
import com.foxit.uiextensions.controls.toolbar.BaseBar;
import com.foxit.uiextensions.controls.toolbar.BaseBar.TB_Position;
import com.foxit.uiextensions.controls.toolbar.BaseItem;
import com.foxit.uiextensions.controls.toolbar.impl.BaseItemImpl;
import com.foxit.uiextensions.controls.toolbar.impl.TopBarImpl;
import com.foxit.uiextensions.modules.ThumbnailAdapter.ThumbViewHolder;
import com.foxit.uiextensions.modules.ThumbnailItemTouchCallback.OnDragListener;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppResource;
import com.foxit.uiextensions.utils.OnPageEventListener;
import com.foxit.uiextensions.utils.UIToast;
import java.io.File;
import java.io.FileFilter;

public class ThumbnailSupport extends DialogFragment {
    protected static final int REMOVE_ALL_PAGES_TIP = 0;
    protected static final int REMOVE_SOME_PAGES_TIP = 1;
    private ThumbnailAdapter mAdapter;
    private Context mContext;
    private ThumbnailItem mCurEditItem;
    private BaseItem mDeleteItem;
    private AppDisplay mDisplay;
    private BaseItem mExtractItem;
    private UIFileSelectDialog mFileSelectDialog = null;
    private UIFolderSelectDialog mFolderSelectDialog = null;
    private GridLayoutManager mGridLayoutManager;
    private final int mHorSpacing = 5;
    private BaseItem mInsertItem;
    private PDFViewCtrl mPDFView;
    private final IPageEventListener mPageEventListener = new OnPageEventListener() {
        public void onPagesRemoved(boolean success, int[] pageIndexes) {
            if (success) {
                ThumbnailSupport.this.mCurEditItem = null;
                ThumbnailSupport.this.mbNeedRelayout = true;
                for (int i = 0; i < pageIndexes.length; i++) {
                    ThumbnailItem item = (ThumbnailItem) ThumbnailSupport.this.mAdapter.mThumbnailList.get(pageIndexes[i] - i);
                    ThumbnailSupport.this.mAdapter.updateCacheListInfo(item, false);
                    ThumbnailSupport.this.mAdapter.updateSelectListInfo(item, false);
                    ThumbnailSupport.this.mAdapter.mThumbnailList.remove(item);
                }
                updateTopLayout();
                return;
            }
            showTips(AppResource.getString(ThumbnailSupport.this.mContext, R.string.rv_page_remove_error));
        }

        public void onPageMoved(boolean success, int index, int dstIndex) {
            if (success) {
                ThumbnailSupport.this.mbNeedRelayout = true;
            }
        }

        public void onPagesRotated(boolean success, int[] pageIndexes, int rotation) {
            if (success) {
                ThumbnailSupport.this.mbNeedRelayout = true;
                for (int i : pageIndexes) {
                    ThumbnailSupport.this.mAdapter.updateCacheListInfo((ThumbnailItem) ThumbnailSupport.this.mAdapter.mThumbnailList.get(i), false);
                }
                updateTopLayout();
                return;
            }
            showTips(AppResource.getString(ThumbnailSupport.this.mContext, R.string.rv_page_rotate_error));
        }

        public void onPagesInserted(boolean success, int dstIndex, int[] range) {
            if (success) {
                ThumbnailSupport.this.mbNeedRelayout = true;
                for (int i = 0; i < range.length / 2; i++) {
                    for (int index = range[i * 2]; index < range[(i * 2) + 1]; index++) {
                        ThumbnailSupport.this.mAdapter.mThumbnailList.add(dstIndex, new ThumbnailItem(dstIndex, ThumbnailSupport.this.getThumbnailBackgroundSize(), ThumbnailSupport.this.mPDFView));
                        dstIndex++;
                    }
                }
                updateTopLayout();
                return;
            }
            showTips(AppResource.getString(ThumbnailSupport.this.mContext, R.string.rv_page_import_error));
        }

        private void updateTopLayout() {
            ((Activity) ThumbnailSupport.this.mContext).runOnUiThread(new Runnable() {
                public void run() {
                    ThumbnailSupport.this.setSelectViewMode(ThumbnailSupport.this.mAdapter.isSelectedAll());
                }
            });
        }

        private void showTips(final String tips) {
            ((Activity) ThumbnailSupport.this.mContext).runOnUiThread(new Runnable() {
                public void run() {
                    UIToast.getInstance(ThumbnailSupport.this.mContext).show(tips, 1);
                }
            });
        }
    };
    private ProgressDialog mProgressDialog = null;
    private BaseItem mRotateItem;
    private BaseItem mSelectAllItem;
    private int mSpanCount;
    private RecyclerView mThumbnailGridView;
    private Point mThumbnailSize;
    private BaseItem mThumbnailTitle;
    private BaseBar mThumbnailTopBar;
    private int mVerSpacing;
    private boolean mbEditMode = false;
    private boolean mbNeedRelayout = false;

    private class SpacesItemDecoration extends ItemDecoration {
        private SpacesItemDecoration() {
        }

        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
            if (ThumbnailSupport.this.mSpanCount > 0) {
                int spanIndex = parent.getChildAdapterPosition(view) % ThumbnailSupport.this.mSpanCount;
                outRect.left = ThumbnailSupport.this.mVerSpacing - ((ThumbnailSupport.this.mVerSpacing * spanIndex) / ThumbnailSupport.this.mSpanCount);
                outRect.right = ((spanIndex + 1) * ThumbnailSupport.this.mVerSpacing) / ThumbnailSupport.this.mSpanCount;
                outRect.top = 5;
                outRect.bottom = 5;
                return;
            }
            outRect.setEmpty();
        }
    }

    public PDFViewCtrl getPDFView() {
        return this.mPDFView;
    }

    public boolean isEditMode() {
        return this.mbEditMode;
    }

    public Context getContext() {
        return this.mContext;
    }

    public void init(PDFViewCtrl pdfViewCtrl) {
        this.mPDFView = pdfViewCtrl;
        this.mPDFView.registerPageEventListener(this.mPageEventListener);
    }

    protected UIFileSelectDialog getFileSelectDialog() {
        if (this.mFileSelectDialog == null) {
            this.mFileSelectDialog = new UIFileSelectDialog(getContext(), null);
            this.mFileSelectDialog.init(new FileFilter() {
                public boolean accept(File pathname) {
                    return !pathname.isHidden() && pathname.canRead() && (!pathname.isFile() || pathname.getName().toLowerCase().endsWith(".pdf"));
                }
            }, true);
            this.mFileSelectDialog.setTitle(AppResource.getString(this.mContext, R.string.fx_string_import));
            this.mFileSelectDialog.setCanceledOnTouchOutside(true);
        }
        return this.mFileSelectDialog;
    }

    public ThumbViewHolder getViewHolderByItem(ThumbnailItem item) {
        return (ThumbViewHolder) this.mThumbnailGridView.findViewHolderForAdapterPosition(this.mAdapter.mThumbnailList.indexOf(item));
    }

    public boolean isThumbnailItemVisible(ThumbnailItem item) {
        int position = this.mAdapter.mThumbnailList.indexOf(item);
        return position >= this.mGridLayoutManager.findFirstVisibleItemPosition() && position <= this.mGridLayoutManager.findLastVisibleItemPosition();
    }

    protected UIFolderSelectDialog getFolderSelectDialog() {
        if (this.mFolderSelectDialog == null) {
            this.mFolderSelectDialog = new UIFolderSelectDialog(this.mContext);
            this.mFolderSelectDialog.setFileFilter(new FileFilter() {
                public boolean accept(File pathname) {
                    return !pathname.isHidden() && pathname.canRead() && (!pathname.isFile() || pathname.getName().toLowerCase().endsWith(".pdf"));
                }
            });
            this.mFolderSelectDialog.setTitle(AppResource.getString(this.mContext, R.string.fx_string_extract));
            this.mFolderSelectDialog.setButton(4);
            this.mFolderSelectDialog.setCanceledOnTouchOutside(true);
        }
        return this.mFolderSelectDialog;
    }

    ProgressDialog getProgressDialog() {
        if (this.mProgressDialog == null) {
            this.mProgressDialog = new ProgressDialog(this.mContext);
            this.mProgressDialog.setCancelable(false);
        }
        return this.mProgressDialog;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        computeSize();
        if (this.mGridLayoutManager != null) {
            this.mGridLayoutManager.setSpanCount(this.mSpanCount);
            this.mGridLayoutManager.requestLayout();
        }
        if (this.mFileSelectDialog != null && this.mFileSelectDialog.isShowing()) {
            this.mFileSelectDialog.setHeight(this.mFileSelectDialog.getDialogHeight());
            this.mFileSelectDialog.showDialog();
        }
        if (this.mFolderSelectDialog != null && this.mFolderSelectDialog.isShowing()) {
            this.mFolderSelectDialog.setHeight(this.mFolderSelectDialog.getDialogHeight());
            this.mFolderSelectDialog.showDialog();
        }
        this.mAdapter.notifyDataSetChanged();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return initView();
    }

    public void onCreate(Bundle savedInstanceState) {
        int theme;
        super.onCreate(savedInstanceState);
        this.mContext = getActivity();
        this.mDisplay = AppDisplay.getInstance(this.mContext);
        this.mAdapter = new ThumbnailAdapter(this);
        computeSize();
        if (VERSION.SDK_INT >= 21) {
            theme = 16974065;
        } else if (VERSION.SDK_INT >= 14) {
            theme = 16974125;
        } else if (VERSION.SDK_INT >= 13) {
            theme = 16974065;
        } else {
            theme = 16973838;
        }
        setStyle(1, theme);
    }

    public void onDetach() {
        this.mAdapter.clear();
        this.mPDFView.unregisterPageEventListener(this.mPageEventListener);
        super.onDetach();
    }

    private void resetCurEditThumbnailItem() {
        if (this.mCurEditItem != null) {
            this.mCurEditItem.editViewFlag = 0;
            int position = this.mAdapter.mThumbnailList.indexOf(this.mCurEditItem);
            ThumbViewHolder viewHolder = getViewHolderByItem(this.mCurEditItem);
            if (viewHolder != null) {
                viewHolder.changeLeftEditView(position, true);
                viewHolder.changeRightEditView(position, true);
            }
            this.mCurEditItem = null;
        }
    }

    private void changeCurEditThumbnailItem(int position, int flags) {
        this.mCurEditItem = (ThumbnailItem) this.mAdapter.mThumbnailList.get(position);
        this.mCurEditItem.editViewFlag = flags;
    }

    private void updateRecycleLayout() {
        if (this.mbNeedRelayout) {
            this.mPDFView.updatePagesLayout();
        }
    }

    private View initView() {
        View dialogView = View.inflate(this.mContext, R.layout.rd_thumnail_dialog, null);
        LinearLayout thumbnailLayout = (LinearLayout) dialogView.findViewById(R.id.thumbnailist);
        this.mThumbnailGridView = (RecyclerView) dialogView.findViewById(R.id.thumbnail_grid_view);
        this.mThumbnailGridView.setOnScrollListener(new OnScrollListener() {
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                for (int position = ThumbnailSupport.this.mGridLayoutManager.findFirstVisibleItemPosition(); position <= ThumbnailSupport.this.mGridLayoutManager.findLastVisibleItemPosition(); position++) {
                    ((ThumbViewHolder) ThumbnailSupport.this.mThumbnailGridView.findViewHolderForAdapterPosition(position)).drawThumbnail(ThumbnailSupport.this.mAdapter.getThumbnailItem(position), position);
                }
            }
        });
        if (this.mDisplay.isPad()) {
            ((LayoutParams) thumbnailLayout.getLayoutParams()).topMargin = (int) AppResource.getDimension(this.mContext, R.dimen.ux_toolbar_height_pad);
        } else {
            ((LayoutParams) thumbnailLayout.getLayoutParams()).topMargin = (int) AppResource.getDimension(this.mContext, R.dimen.ux_toolbar_height_phone);
        }
        this.mThumbnailTopBar = new TopBarImpl(this.mContext);
        RelativeLayout dialogTitle = (RelativeLayout) dialogView.findViewById(R.id.rd_viewmode_dialog_title);
        changeEditState(false);
        dialogTitle.removeAllViews();
        dialogTitle.addView(this.mThumbnailTopBar.getContentView());
        this.mThumbnailGridView = (RecyclerView) this.mThumbnailGridView.findViewById(R.id.thumbnail_grid_view);
        this.mThumbnailGridView.setHasFixedSize(true);
        this.mThumbnailGridView.setAdapter(this.mAdapter);
        this.mGridLayoutManager = new GridLayoutManager(this.mContext, this.mSpanCount);
        this.mThumbnailGridView.setLayoutManager(this.mGridLayoutManager);
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ThumbnailItemTouchCallback(this.mAdapter).setOnDragListener(new OnDragListener() {
            public void onFinishDrag() {
                ThumbnailSupport.this.mAdapter.notifyDataSetChanged();
            }
        }));
        this.mThumbnailGridView.addItemDecoration(new SpacesItemDecoration());
        itemTouchHelper.attachToRecyclerView(this.mThumbnailGridView);
        this.mThumbnailGridView.addOnItemTouchListener(new OnThumbnailItemTouchListener(this.mThumbnailGridView) {
            public void onLongPress(ViewHolder vh) {
                if (ThumbnailSupport.this.mbEditMode) {
                    ThumbnailSupport.this.resetCurEditThumbnailItem();
                    itemTouchHelper.startDrag(vh);
                } else {
                    ThumbnailSupport.this.changeEditState(true);
                }
                ((Vibrator) ThumbnailSupport.this.getActivity().getSystemService("vibrator")).vibrate(70);
            }

            public boolean onItemClick(ViewHolder vh) {
                ThumbViewHolder viewHolder = (ThumbViewHolder) vh;
                ThumbnailItem thumbnailItem = ThumbnailSupport.this.mAdapter.getThumbnailItem(vh.getAdapterPosition());
                if (ThumbnailSupport.this.mbEditMode) {
                    if (!thumbnailItem.equals(ThumbnailSupport.this.mCurEditItem)) {
                        boolean isSelected = !thumbnailItem.isSelected();
                        ThumbnailSupport.this.mAdapter.updateSelectListInfo(thumbnailItem, isSelected);
                        ThumbnailSupport.this.setSelectViewMode(ThumbnailSupport.this.mAdapter.isSelectedAll());
                        viewHolder.changeSelectView(isSelected);
                        ThumbnailSupport.this.mThumbnailTitle.setText(String.format("%d", new Object[]{Integer.valueOf(ThumbnailSupport.this.mAdapter.getSelectedItemCount())}));
                    }
                    ThumbnailSupport.this.resetCurEditThumbnailItem();
                } else {
                    ThumbnailSupport.this.updateRecycleLayout();
                    ThumbnailSupport.this.mPDFView.gotoPage(thumbnailItem.getIndex());
                    ((PageNavigationModule) ((UIExtensionsManager) ThumbnailSupport.this.mPDFView.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_PAGENAV)).resetJumpView();
                    if (ThumbnailSupport.this.getDialog() != null) {
                        ThumbnailSupport.this.getDialog().dismiss();
                    }
                    ThumbnailSupport.this.dismiss();
                }
                return true;
            }

            public boolean onToRightFling(ViewHolder vh) {
                if (!ThumbnailSupport.this.mbEditMode) {
                    return false;
                }
                ThumbnailSupport.this.resetCurEditThumbnailItem();
                ThumbViewHolder viewHolder = (ThumbViewHolder) vh;
                ThumbnailSupport.this.changeCurEditThumbnailItem(vh.getAdapterPosition(), 1);
                viewHolder.changeLeftEditView(vh.getAdapterPosition(), true);
                return true;
            }

            public boolean onToLeftFling(ViewHolder vh) {
                if (!ThumbnailSupport.this.mbEditMode) {
                    return false;
                }
                ThumbnailSupport.this.resetCurEditThumbnailItem();
                ThumbViewHolder viewHolder = (ThumbViewHolder) vh;
                int position = vh.getAdapterPosition();
                ThumbnailSupport.this.changeCurEditThumbnailItem(position, 2);
                viewHolder.changeRightEditView(position, true);
                return true;
            }
        });
        getDialog().setOnKeyListener(new OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() != 1) {
                    return false;
                }
                if (keyCode == 4) {
                    if (ThumbnailSupport.this.mbEditMode) {
                        ThumbnailSupport.this.changeEditState(false);
                    } else {
                        if (ThumbnailSupport.this.getDialog() != null) {
                            ThumbnailSupport.this.getDialog().dismiss();
                        }
                        ThumbnailSupport.this.dismiss();
                        ThumbnailSupport.this.updateRecycleLayout();
                        ((PageNavigationModule) ((UIExtensionsManager) ThumbnailSupport.this.mPDFView.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_PAGENAV)).resetJumpView();
                    }
                }
                return true;
            }
        });
        return dialogView;
    }

    private void setSelectViewMode(boolean selectedAll) {
        if (this.mAdapter.getSelectedItemCount() == 0) {
            this.mRotateItem.setEnable(false);
            this.mDeleteItem.setEnable(false);
            this.mExtractItem.setEnable(false);
            this.mExtractItem.setImageResource(R.drawable.thumbnail_extract_pressed);
            this.mDeleteItem.setImageResource(R.drawable.thumbnail_delete_all_pressed);
            this.mRotateItem.setImageResource(R.drawable.thumbnail_rotate_pressed);
        } else {
            this.mRotateItem.setEnable(true);
            this.mDeleteItem.setEnable(true);
            this.mDeleteItem.setImageResource(R.drawable.thumbnail_delete_selector);
            this.mRotateItem.setImageResource(R.drawable.thumbnail_rotate_selector);
            if (DocumentManager.getInstance(this.mPDFView).canCopy()) {
                this.mExtractItem.setEnable(true);
                this.mExtractItem.setImageResource(R.drawable.thumbnail_extract_selector);
            } else {
                this.mExtractItem.setEnable(false);
                this.mExtractItem.setImageResource(R.drawable.thumbnail_extract_pressed);
            }
        }
        if (selectedAll) {
            this.mSelectAllItem.setImageResource(R.drawable.thumbnail_selected_all);
        } else {
            this.mSelectAllItem.setImageResource(R.drawable.thumbnail_select_all);
        }
        this.mThumbnailTitle.setText(String.format("%d", new Object[]{Integer.valueOf(this.mAdapter.getSelectedItemCount())}));
    }

    private void changeEditState(boolean isEditMode) {
        boolean z;
        boolean canModifyContents = DocumentManager.getInstance(this.mPDFView).canModifyContents();
        if (isEditMode && canModifyContents) {
            z = true;
        } else {
            z = false;
        }
        this.mbEditMode = z;
        this.mThumbnailTopBar.removeAllItems();
        BaseItem mCloseThumbnailBtn = new BaseItemImpl(this.mContext);
        mCloseThumbnailBtn.setImageResource(R.drawable.cloud_back);
        this.mThumbnailTopBar.addView(mCloseThumbnailBtn, TB_Position.Position_LT);
        this.mThumbnailTitle = new BaseItemImpl(this.mContext);
        this.mThumbnailTitle.setTextColorResource(R.color.ux_text_color_title_light);
        this.mThumbnailTitle.setTextSize(this.mDisplay.px2dp(this.mContext.getResources().getDimension(R.dimen.ux_text_height_title)));
        this.mThumbnailTopBar.addView(this.mThumbnailTitle, TB_Position.Position_LT);
        if (this.mbEditMode) {
            this.mThumbnailTitle.setText(String.format("%d", new Object[]{Integer.valueOf(this.mAdapter.getSelectedItemCount())}));
            this.mSelectAllItem = new BaseItemImpl(this.mContext);
            this.mInsertItem = new BaseItemImpl(this.mContext);
            this.mInsertItem.setImageResource(R.drawable.thumbnail_add_page_selector);
            this.mExtractItem = new BaseItemImpl(this.mContext);
            this.mExtractItem.setImageResource(R.drawable.thumbnail_extract);
            this.mRotateItem = new BaseItemImpl(this.mContext);
            this.mRotateItem.setImageResource(R.drawable.thumbnail_rotate);
            this.mDeleteItem = new BaseItemImpl(this.mContext);
            this.mDeleteItem.setImageResource(R.drawable.thumbnail_delete_all_normal);
            this.mThumbnailTopBar.addView(this.mRotateItem, TB_Position.Position_RB);
            this.mThumbnailTopBar.addView(this.mDeleteItem, TB_Position.Position_RB);
            this.mThumbnailTopBar.addView(this.mSelectAllItem, TB_Position.Position_RB);
            this.mRotateItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ThumbnailSupport.this.mAdapter.rotateSelectedPages();
                }
            });
            this.mDeleteItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ThumbnailSupport.this.showTipsDlg(ThumbnailSupport.this.mAdapter.isSelectedAll() ? 0 : 1);
                }
            });
            this.mExtractItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ThumbnailSupport.this.mAdapter.extractPages();
                }
            });
            this.mSelectAllItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    boolean isSelectedAll = !ThumbnailSupport.this.mAdapter.isSelectedAll();
                    ThumbnailSupport.this.mAdapter.selectAll(isSelectedAll);
                    ThumbnailSupport.this.setSelectViewMode(isSelectedAll);
                }
            });
            this.mInsertItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ThumbnailSupport.this.mAdapter.importPages(ThumbnailSupport.this.mAdapter.getEditPosition());
                }
            });
            mCloseThumbnailBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ThumbnailSupport.this.changeEditState(false);
                }
            });
            setSelectViewMode(this.mAdapter.isSelectedAll());
        } else {
            if (canModifyContents) {
                BaseItem mEditThumbnailNtu = new BaseItemImpl(this.mContext);
                mEditThumbnailNtu.setText(AppResource.getString(this.mContext, R.string.rv_page_present_thumbnail_edit));
                mEditThumbnailNtu.setTextSize(this.mDisplay.px2dp(this.mContext.getResources().getDimension(R.dimen.ux_text_height_title)));
                mEditThumbnailNtu.setTextColorResource(R.color.ux_text_color_title_light);
                this.mThumbnailTopBar.addView(mEditThumbnailNtu, TB_Position.Position_RB);
                mEditThumbnailNtu.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        ThumbnailSupport.this.changeEditState(true);
                    }
                });
            }
            mCloseThumbnailBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (ThumbnailSupport.this.getDialog() != null) {
                        ThumbnailSupport.this.getDialog().dismiss();
                    }
                    ThumbnailSupport.this.dismiss();
                    ThumbnailSupport.this.updateRecycleLayout();
                    ((PageNavigationModule) ((UIExtensionsManager) ThumbnailSupport.this.mPDFView.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_PAGENAV)).resetJumpView();
                }
            });
            this.mThumbnailTopBar.addView(mCloseThumbnailBtn, TB_Position.Position_LT);
            this.mThumbnailTopBar.setBackgroundResource(R.color.ux_bg_color_toolbar_colour);
            this.mThumbnailTitle.setText(AppResource.getString(this.mContext, R.string.rv_page_present_thumbnail));
            resetCurEditThumbnailItem();
        }
        this.mAdapter.notifyDataSetChanged();
    }

    void showTipsDlg(int removeType) {
        final UITextEditDialog dialog = new UITextEditDialog(this.mContext);
        dialog.getInputEditText().setVisibility(8);
        dialog.setTitle(AppResource.getString(this.mContext, R.string.fx_string_delete));
        switch (removeType) {
            case 0:
                dialog.getCancelButton().setVisibility(8);
                dialog.getPromptTextView().setText(AppResource.getString(this.mContext, R.string.rv_page_delete_all_thumbnail));
                dialog.getOKButton().setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                break;
            case 1:
                dialog.getPromptTextView().setText(AppResource.getString(this.mContext, R.string.rv_page_delete_thumbnail));
                dialog.getCancelButton().setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.getOKButton().setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        ThumbnailSupport.this.mAdapter.removeSelectedPages();
                        dialog.dismiss();
                    }
                });
                break;
        }
        dialog.show();
    }

    public Point getThumbnailBackgroundSize() {
        if (this.mThumbnailSize == null) {
            float scale;
            float dpi = (float) this.mContext.getResources().getDisplayMetrics().densityDpi;
            if (dpi == 0.0f) {
                dpi = 240.0f;
            }
            try {
                float width = this.mPDFView.getDoc().getPage(0).getWidth();
                float height = this.mPDFView.getDoc().getPage(0).getHeight();
                scale = width > height ? height / width : width / height;
            } catch (PDFException e) {
                scale = 0.7f;
            }
            this.mThumbnailSize = new Point((int) (dpi * 0.7f), (int) ((0.7f * dpi) / scale));
        }
        return this.mThumbnailSize;
    }

    private void computeSize() {
        int displayWidth = this.mContext.getResources().getDisplayMetrics().widthPixels;
        int displayHeight = this.mContext.getResources().getDisplayMetrics().heightPixels;
        Point size = getThumbnailBackgroundSize();
        this.mSpanCount = Math.max(1, (displayWidth - 5) / ((size.x + 5) + 2));
        int tasksMax = this.mSpanCount * ((displayHeight / size.y) + 2);
        this.mAdapter.setCacheSize(tasksMax, Math.max(64, tasksMax));
        this.mVerSpacing = (displayWidth - (size.x * this.mSpanCount)) / (this.mSpanCount + 1);
    }
}
