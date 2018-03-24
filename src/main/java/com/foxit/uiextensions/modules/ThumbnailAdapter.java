package com.foxit.uiextensions.modules;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.Task;
import com.foxit.sdk.Task.CallBack;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.controls.dialog.MatchDialog.DialogListener;
import com.foxit.uiextensions.controls.dialog.UITextEditDialog;
import com.foxit.uiextensions.controls.dialog.fileselect.UIFileSelectDialog;
import com.foxit.uiextensions.controls.dialog.fileselect.UIFileSelectDialog.OnFileClickedListener;
import com.foxit.uiextensions.controls.dialog.fileselect.UIFolderSelectDialog;
import com.foxit.uiextensions.modules.ThumbnailItemTouchCallback.ItemTouchAdapter;
import com.foxit.uiextensions.utils.AppFileUtil;
import com.foxit.uiextensions.utils.AppResource;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.UIToast;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/* compiled from: ThumbnailSupport */
class ThumbnailAdapter extends Adapter<ThumbViewHolder> implements ItemTouchAdapter {
    private final int EXPORT_PAGE = 4;
    private final int INSERT_FROM_DOCUMENT = 3;
    private final int REMOVE_PAGE = 0;
    private final int ROTATE_PAGE_ACW = 2;
    private final int ROTATE_PAGE_CW = 1;
    private boolean isEditing = false;
    private int mBitmapsMax;
    final ArrayList<ThumbnailItem> mCacheList;
    private int mCurrentPage;
    private final PDFViewCtrl mPDFViewCtrl;
    final ArrayList<ThumbnailItem> mSelectedList;
    private final ThumbnailSupport mSupport;
    private final ArrayList<DrawThumbnailTask> mTaskList;
    private int mTasksMax;
    final ArrayList<ThumbnailItem> mThumbnailList;

    /* compiled from: ThumbnailSupport */
    interface EditThumbnailCallback {
        void result(boolean z);
    }

    /* compiled from: ThumbnailSupport */
    class EditThumbnailTask extends Task {
        private String mExtractPath;
        private int[] mImportRanges;
        private int mInsertPosition;
        private PDFDoc mPDFDoc;
        private boolean mSuccess;
        private final int mType;
        private final ArrayList<ThumbnailItem> tmpItemLists;

        /* compiled from: ThumbnailSupport */
        /* renamed from: com.foxit.uiextensions.modules.ThumbnailAdapter$EditThumbnailTask$1 */
        class AnonymousClass1 implements CallBack {
            private final /* synthetic */ EditThumbnailCallback val$callback;
            private final /* synthetic */ ThumbnailAdapter val$this$0;

            AnonymousClass1(ThumbnailAdapter thumbnailAdapter, EditThumbnailCallback editThumbnailCallback) {
                this.val$this$0 = thumbnailAdapter;
                this.val$callback = editThumbnailCallback;
            }

            public void result(Task task) {
                if (this.val$callback != null) {
                    this.val$callback.result(((EditThumbnailTask) task).mSuccess);
                }
            }
        }

        /* compiled from: ThumbnailSupport */
        /* renamed from: com.foxit.uiextensions.modules.ThumbnailAdapter$EditThumbnailTask$2 */
        class AnonymousClass2 implements CallBack {
            private final /* synthetic */ EditThumbnailCallback val$callback;
            private final /* synthetic */ ThumbnailAdapter val$this$0;

            AnonymousClass2(ThumbnailAdapter thumbnailAdapter, EditThumbnailCallback editThumbnailCallback) {
                this.val$this$0 = thumbnailAdapter;
                this.val$callback = editThumbnailCallback;
            }

            public void result(Task task) {
                if (this.val$callback != null) {
                    this.val$callback.result(((EditThumbnailTask) task).mSuccess);
                }
            }
        }

        /* compiled from: ThumbnailSupport */
        /* renamed from: com.foxit.uiextensions.modules.ThumbnailAdapter$EditThumbnailTask$3 */
        class AnonymousClass3 implements CallBack {
            private final /* synthetic */ EditThumbnailCallback val$callback;
            private final /* synthetic */ ThumbnailAdapter val$this$0;

            AnonymousClass3(ThumbnailAdapter thumbnailAdapter, EditThumbnailCallback editThumbnailCallback) {
                this.val$this$0 = thumbnailAdapter;
                this.val$callback = editThumbnailCallback;
            }

            public void result(Task task) {
                if (this.val$callback != null) {
                    this.val$callback.result(((EditThumbnailTask) task).mSuccess);
                }
            }
        }

        EditThumbnailTask(int type, ArrayList<ThumbnailItem> itemLists, EditThumbnailCallback callback) {
            super(new AnonymousClass1(ThumbnailAdapter.this, callback));
            this.mSuccess = false;
            this.mType = type;
            this.tmpItemLists = itemLists;
        }

        EditThumbnailTask(int position, int[] ranges, PDFDoc doc, EditThumbnailCallback callback) {
            super(new AnonymousClass2(ThumbnailAdapter.this, callback));
            this.mSuccess = false;
            this.mType = 3;
            this.tmpItemLists = null;
            this.mInsertPosition = position;
            this.mImportRanges = ranges;
            this.mPDFDoc = doc;
        }

        EditThumbnailTask(ArrayList<ThumbnailItem> itemLists, String path, EditThumbnailCallback callback) {
            super(new AnonymousClass3(ThumbnailAdapter.this, callback));
            this.mSuccess = false;
            this.mType = 4;
            this.tmpItemLists = itemLists;
            this.mExtractPath = path;
        }

        protected void execute() {
            ThumbnailAdapter.this.isEditing = true;
            switch (this.mType) {
                case 0:
                case 1:
                case 2:
                    editSelectedPages();
                    break;
                case 3:
                    insertPages();
                    break;
                case 4:
                    extractPages();
                    break;
            }
            ThumbnailAdapter.this.isEditing = false;
        }

        private void editSelectedPages() {
            int[] pageIndexes = new int[this.tmpItemLists.size()];
            for (int i = 0; i < this.tmpItemLists.size(); i++) {
                pageIndexes[i] = ((ThumbnailItem) this.tmpItemLists.get(i)).getIndex();
            }
            if (this.mType == 0) {
                this.mSuccess = ThumbnailAdapter.this.mPDFViewCtrl.removePages(pageIndexes);
            }
            Iterator it = this.tmpItemLists.iterator();
            while (it.hasNext()) {
                ThumbnailItem item = (ThumbnailItem) it.next();
                int rotation;
                switch (this.mType) {
                    case 1:
                        rotation = item.getRotation();
                        this.mSuccess = item.setRotation(rotation < 3 ? rotation + 1 : 3 - rotation);
                        break;
                    case 2:
                        rotation = item.getRotation();
                        this.mSuccess = item.setRotation(rotation > 0 ? rotation - 1 : rotation + 3);
                        break;
                    default:
                        break;
                }
            }
            ThumbnailAdapter.this.mCurrentPage = Math.min(ThumbnailAdapter.this.mCurrentPage, ThumbnailAdapter.this.getItemCount() - 1);
        }

        private void insertPages() {
            this.mSuccess = ThumbnailAdapter.this.mPDFViewCtrl.insertPages(this.mInsertPosition, 0, null, this.mPDFDoc, this.mImportRanges);
        }

        private void extractPages() {
            try {
                PDFDoc doc = PDFDoc.create();
                if (doc == null) {
                    this.mSuccess = false;
                    return;
                }
                int i;
                Collections.sort(this.tmpItemLists);
                ArrayList<Boolean> extractFlagList = new ArrayList();
                for (i = 0; i < ThumbnailAdapter.this.mPDFViewCtrl.getPageCount(); i++) {
                    extractFlagList.add(Boolean.valueOf(false));
                }
                Iterator it = this.tmpItemLists.iterator();
                while (it.hasNext()) {
                    extractFlagList.set(((ThumbnailItem) it.next()).getIndex(), Boolean.valueOf(true));
                }
                ArrayList<Integer> rangeList = new ArrayList();
                int lastIndex = -1;
                int count = 0;
                for (i = 0; i < extractFlagList.size(); i++) {
                    if (((Boolean) extractFlagList.get(i)).booleanValue()) {
                        if (lastIndex == -1) {
                            lastIndex = i;
                        }
                        count++;
                    } else if (lastIndex == -1) {
                        count = 0;
                    } else {
                        rangeList.add(Integer.valueOf(lastIndex));
                        rangeList.add(Integer.valueOf(count));
                        lastIndex = -1;
                        count = 0;
                    }
                }
                if (lastIndex != -1) {
                    rangeList.add(Integer.valueOf(lastIndex));
                    rangeList.add(Integer.valueOf(count));
                }
                int[] ranges = new int[rangeList.size()];
                for (i = 0; i < rangeList.size(); i++) {
                    ranges[i] = ((Integer) rangeList.get(i)).intValue();
                }
                doc.startImportPages(doc.getPageCount(), 2, null, ThumbnailAdapter.this.mPDFViewCtrl.getDoc(), ranges, null);
                doc.saveAs(this.mExtractPath, 1);
                doc.release();
                this.mSuccess = true;
            } catch (PDFException e) {
                this.mSuccess = false;
            }
        }
    }

    /* compiled from: ThumbnailSupport */
    public class ThumbViewHolder extends ViewHolder {
        private final ImageView mImageView;
        private final TextView mIndexView;
        private final ImageView mInsertLeftView;
        private final ImageView mInsertRightView;
        private final LinearLayout mLeftEditViewLayout;
        private final ImageView mRemoveView;
        private final LinearLayout mRightEditViewLayout;
        private final ImageView mRotateAcwView;
        private final ImageView mRotateCwView;
        private final ImageView mSelectView;
        protected Bitmap mThumbnailBitmap;

        public ThumbViewHolder(View itemView) {
            super(itemView);
            this.mIndexView = (TextView) itemView.findViewById(R.id.item_text);
            this.mImageView = (ImageView) itemView.findViewById(R.id.item_image);
            this.mSelectView = (ImageView) itemView.findViewById(R.id.thumbnail_select_view);
            this.mRemoveView = (ImageView) itemView.findViewById(R.id.thumbnail_delete_self);
            this.mRotateAcwView = (ImageView) itemView.findViewById(R.id.thumbnail_rotate_acw);
            this.mRotateCwView = (ImageView) itemView.findViewById(R.id.thumbnail_rotate_cw);
            this.mInsertLeftView = (ImageView) itemView.findViewById(R.id.thumbnail_insert_left);
            this.mInsertRightView = (ImageView) itemView.findViewById(R.id.thumbnail_insert_right);
            this.mLeftEditViewLayout = (LinearLayout) itemView.findViewById(R.id.thumbnail_edit_left_layout);
            this.mRightEditViewLayout = (LinearLayout) itemView.findViewById(R.id.thumbnail_edit_right_layout);
        }

        public void updateImageView() {
            this.mImageView.setImageBitmap(getThumbnailBitmap());
            this.mImageView.invalidate();
        }

        public Bitmap getThumbnailBitmap() {
            if (this.mThumbnailBitmap == null) {
                this.mThumbnailBitmap = Bitmap.createBitmap(ThumbnailAdapter.this.mSupport.getThumbnailBackgroundSize().x, ThumbnailAdapter.this.mSupport.getThumbnailBackgroundSize().y, Config.ARGB_8888);
            }
            return this.mThumbnailBitmap;
        }

        public boolean inEditView(int x, int y) {
            int[] location = new int[2];
            if (this.mLeftEditViewLayout.getVisibility() == 0) {
                this.mLeftEditViewLayout.getLocationOnScreen(location);
                if (new Rect(location[0], location[1], location[0] + this.mLeftEditViewLayout.getWidth(), location[1] + this.mLeftEditViewLayout.getHeight()).contains(x, y)) {
                    return true;
                }
            }
            if (this.mRightEditViewLayout.getVisibility() == 0) {
                this.mRightEditViewLayout.getLocationOnScreen(location);
                if (new Rect(location[0], location[1], location[0] + this.mRightEditViewLayout.getWidth(), location[1] + this.mRightEditViewLayout.getHeight()).contains(x, y)) {
                    return true;
                }
            }
            return false;
        }

        protected void blank(ThumbnailItem item) {
            Bitmap bitmap = getThumbnailBitmap();
            bitmap.eraseColor(ThumbnailAdapter.this.mSupport.getContext().getResources().getColor(R.color.ux_color_thumbnail_textview_background));
            this.mImageView.setImageBitmap(bitmap);
            this.mImageView.invalidate();
        }

        public void changeLeftEditView(final int position, boolean withAnimation) {
            final ThumbnailItem item = (ThumbnailItem) ThumbnailAdapter.this.mThumbnailList.get(position);
            if (ThumbnailAdapter.this.mSupport.isEditMode() && item.editViewFlag == 1) {
                if (this.mLeftEditViewLayout.getVisibility() != 0) {
                    LayoutParams layoutParams = this.mRotateCwView.getLayoutParams();
                    layoutParams.width = ThumbnailAdapter.this.mSupport.getThumbnailBackgroundSize().x / 3;
                    layoutParams.height = layoutParams.width;
                    this.mRotateCwView.setLayoutParams(layoutParams);
                    this.mRotateAcwView.setLayoutParams(layoutParams);
                    this.mRemoveView.setLayoutParams(layoutParams);
                    this.mRotateCwView.setPadding(5, 0, 5, 0);
                    this.mRotateAcwView.setPadding(5, 0, 5, 0);
                    this.mRemoveView.setPadding(5, 0, 5, 0);
                    this.mRotateCwView.requestLayout();
                    this.mRotateAcwView.requestLayout();
                    this.mRemoveView.requestLayout();
                    this.mRotateAcwView.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            ThumbnailAdapter.this.rotatePage(position, false);
                        }
                    });
                    this.mRotateCwView.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            ThumbnailAdapter.this.rotatePage(position, true);
                        }
                    });
                    this.mRemoveView.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            item.editViewFlag = 0;
                            ThumbnailAdapter.this.removePage(position);
                        }
                    });
                    if (withAnimation) {
                        TranslateAnimation showAnimation = new TranslateAnimation(1, -1.0f, 1, 0.0f, 1, 0.0f, 1, 0.0f);
                        showAnimation.setDuration(500);
                        this.mLeftEditViewLayout.startAnimation(showAnimation);
                    }
                    this.mLeftEditViewLayout.setVisibility(0);
                }
            } else if (this.mLeftEditViewLayout.getVisibility() != 8) {
                if (withAnimation) {
                    TranslateAnimation goneAnimation = new TranslateAnimation(1, 0.0f, 1, -1.0f, 1, 0.0f, 1, 0.0f);
                    goneAnimation.setDuration(500);
                    this.mLeftEditViewLayout.startAnimation(goneAnimation);
                }
                this.mLeftEditViewLayout.setVisibility(8);
            }
        }

        public void changeRightEditView(final int position, boolean withAnimation) {
            ThumbnailItem item = (ThumbnailItem) ThumbnailAdapter.this.mThumbnailList.get(position);
            TranslateAnimation showAnimation;
            if (ThumbnailAdapter.this.mSupport.isEditMode() && item.editViewFlag == 2) {
                if (this.mRightEditViewLayout.getVisibility() != 0) {
                    LayoutParams layoutParams = this.mInsertLeftView.getLayoutParams();
                    layoutParams.width = ThumbnailAdapter.this.mSupport.getThumbnailBackgroundSize().x / 3;
                    layoutParams.height = layoutParams.width;
                    this.mInsertLeftView.setLayoutParams(layoutParams);
                    this.mInsertRightView.setLayoutParams(layoutParams);
                    this.mInsertRightView.setPadding(5, 0, 5, 0);
                    this.mInsertLeftView.setPadding(5, 0, 5, 0);
                    this.mInsertLeftView.requestLayout();
                    this.mInsertRightView.requestLayout();
                    this.mInsertLeftView.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            ThumbnailAdapter.this.importPages(position);
                        }
                    });
                    this.mInsertRightView.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            ThumbnailAdapter.this.importPages(position + 1);
                        }
                    });
                    if (withAnimation) {
                        showAnimation = new TranslateAnimation(1, 1.0f, 1, 0.0f, 1, 0.0f, 1, 0.0f);
                        showAnimation.setDuration(500);
                        this.mRightEditViewLayout.startAnimation(showAnimation);
                    }
                    this.mRightEditViewLayout.setVisibility(0);
                }
            } else if (this.mRightEditViewLayout.getVisibility() != 8) {
                if (withAnimation) {
                    showAnimation = new TranslateAnimation(1, 0.0f, 1, 1.0f, 1, 0.0f, 1, 0.0f);
                    showAnimation.setDuration(500);
                    this.mRightEditViewLayout.startAnimation(showAnimation);
                }
                this.mRightEditViewLayout.setVisibility(8);
            }
        }

        public void changeSelectView(boolean show) {
            if (ThumbnailAdapter.this.mSupport.isEditMode()) {
                LayoutParams layoutParams = this.mSelectView.getLayoutParams();
                layoutParams.height = ThumbnailAdapter.this.mSupport.getThumbnailBackgroundSize().x / 5;
                layoutParams.width = layoutParams.height;
                this.mSelectView.setLayoutParams(layoutParams);
                this.mSelectView.requestLayout();
                this.mSelectView.setVisibility(0);
                if (show) {
                    this.mSelectView.setImageDrawable(ThumbnailAdapter.this.mSupport.getContext().getResources().getDrawable(R.drawable.thumbnail_select_true));
                    return;
                } else {
                    this.mSelectView.setImageDrawable(ThumbnailAdapter.this.mSupport.getContext().getResources().getDrawable(R.drawable.thumbnail_select_normal));
                    return;
                }
            }
            this.mSelectView.setVisibility(8);
        }

        public void drawThumbnail(ThumbnailItem item, int position) {
            int index = ThumbnailAdapter.this.isEditing ? position : item.getIndex();
            changeLeftEditView(index, false);
            changeRightEditView(index, false);
            changeSelectView(item.isSelected());
            if (ThumbnailAdapter.this.mCurrentPage == index) {
                this.mIndexView.setBackgroundDrawable(ThumbnailAdapter.this.mSupport.getContext().getResources().getDrawable(R.drawable.thumbnail_textview_background_current));
            } else {
                this.mIndexView.setBackgroundDrawable(ThumbnailAdapter.this.mSupport.getContext().getResources().getDrawable(R.drawable.thumbnail_textview_background_normal));
            }
            this.mIndexView.setText(String.format("%d", new Object[]{Integer.valueOf(index + 1)}));
            if (item.getBitmap() == null || item.needRecompute()) {
                blank(item);
                if ((!item.isRendering() || item.needRecompute()) && !ThumbnailAdapter.this.isEditing) {
                    ThumbnailAdapter.this.addTask(new DrawThumbnailTask(item, new DrawThumbnailCallback() {
                        public void result(ThumbnailItem item, DrawThumbnailTask task, Bitmap bitmap) {
                            ThumbViewHolder viewHolder = ThumbnailAdapter.this.mSupport.getViewHolderByItem(item);
                            if (bitmap != null && viewHolder != null) {
                                Bitmap vhBitmap = viewHolder.getThumbnailBitmap();
                                new Canvas(vhBitmap).drawBitmap(bitmap, null, item.getRect(), new Paint());
                                item.setBitmap(Bitmap.createBitmap(vhBitmap));
                                viewHolder.updateImageView();
                                ThumbnailAdapter.this.updateCacheListInfo(item, true);
                                ThumbnailAdapter.this.removeTask(task);
                            }
                        }
                    }));
                    return;
                }
                return;
            }
            this.mImageView.setImageBitmap(item.getBitmap());
            this.mImageView.invalidate();
        }
    }

    public ThumbnailAdapter(ThumbnailSupport support) {
        this.mSupport = support;
        this.mPDFViewCtrl = support.getPDFView();
        this.mCurrentPage = this.mPDFViewCtrl.getCurrentPage();
        this.mThumbnailList = new ArrayList();
        this.mSelectedList = new ArrayList();
        this.mCacheList = new ArrayList();
        this.mTaskList = new ArrayList();
        this.mCacheList.clear();
        this.mSelectedList.clear();
        this.mThumbnailList.clear();
        for (int i = 0; i < this.mPDFViewCtrl.getPageCount(); i++) {
            this.mThumbnailList.add(i, new ThumbnailItem(i, this.mSupport.getThumbnailBackgroundSize(), this.mPDFViewCtrl));
        }
    }

    public int getEditPosition() {
        if (this.mSelectedList == null || this.mSelectedList.size() <= 0) {
            return this.mThumbnailList.size();
        }
        int index = -1;
        Iterator it = this.mSelectedList.iterator();
        while (it.hasNext()) {
            ThumbnailItem item = (ThumbnailItem) it.next();
            if (item.getIndex() > index) {
                index = item.getIndex();
            }
        }
        return index + 1;
    }

    public void setCacheSize(int tasksMax, int bitmapsMax) {
        this.mTasksMax = tasksMax;
        this.mBitmapsMax = bitmapsMax;
    }

    public boolean isSelectedAll() {
        return this.mThumbnailList.size() == this.mSelectedList.size() && this.mSelectedList.size() != 0;
    }

    public void selectAll(boolean isSelect) {
        this.mSelectedList.clear();
        for (int i = 0; i < this.mThumbnailList.size(); i++) {
            updateSelectListInfo((ThumbnailItem) this.mThumbnailList.get(i), isSelect);
        }
        notifyDataSetChanged();
    }

    private void addTask(DrawThumbnailTask task) {
        synchronized (this.mTaskList) {
            if (this.mTaskList.size() >= this.mTasksMax) {
                DrawThumbnailTask oldTask = null;
                int position = task.getThumbnailItem().getIndex();
                Iterator it = this.mTaskList.iterator();
                while (it.hasNext()) {
                    DrawThumbnailTask thumbnailTask = (DrawThumbnailTask) it.next();
                    if (!this.mSupport.isThumbnailItemVisible(thumbnailTask.getThumbnailItem())) {
                        if (oldTask == null) {
                            oldTask = thumbnailTask;
                        } else if (Math.abs(oldTask.getThumbnailItem().getIndex() - position) < Math.abs(thumbnailTask.getThumbnailItem().getIndex() - position)) {
                            oldTask = thumbnailTask;
                        }
                        if (oldTask == null) {
                            oldTask = (DrawThumbnailTask) this.mTaskList.get(0);
                        }
                        this.mPDFViewCtrl.removeTask(oldTask);
                        this.mTaskList.remove(oldTask);
                        oldTask.getThumbnailItem().resetRending(false);
                    }
                }
                if (oldTask == null) {
                    oldTask = (DrawThumbnailTask) this.mTaskList.get(0);
                }
                this.mPDFViewCtrl.removeTask(oldTask);
                this.mTaskList.remove(oldTask);
                oldTask.getThumbnailItem().resetRending(false);
            }
            this.mTaskList.add(task);
            this.mPDFViewCtrl.addTask(task);
        }
    }

    private void removeTask(DrawThumbnailTask task) {
        synchronized (this.mTaskList) {
            this.mTaskList.remove(task);
        }
    }

    public void updateCacheListInfo(ThumbnailItem value, boolean add) {
        if (add) {
            if (!this.mCacheList.contains(value)) {
                if (this.mCacheList.size() >= this.mBitmapsMax) {
                    ((ThumbnailItem) this.mCacheList.get(0)).setBitmap(null);
                    this.mCacheList.remove(0);
                }
                this.mCacheList.add(value);
            }
        } else if (this.mCacheList.contains(value)) {
            this.mCacheList.remove(value);
            value.setBitmap(null);
        }
    }

    public int getSelectedItemCount() {
        return this.mSelectedList.size();
    }

    public void updateSelectListInfo(ThumbnailItem item, boolean select) {
        if (select) {
            if (!this.mSelectedList.contains(item)) {
                this.mSelectedList.add(item);
            }
            item.setSelected(true);
            return;
        }
        if (this.mSelectedList.contains(item)) {
            this.mSelectedList.remove(item);
        }
        item.setSelected(false);
    }

    public ThumbViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ThumbViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.thumbnail_view, parent, false));
    }

    public void onBindViewHolder(ThumbViewHolder holder, int position) {
        holder.drawThumbnail((ThumbnailItem) this.mThumbnailList.get(position), position);
    }

    public int getItemCount() {
        return this.mThumbnailList.size();
    }

    public ThumbnailItem getThumbnailItem(int position) {
        if (position < 0 || position > getItemCount()) {
            return null;
        }
        return (ThumbnailItem) this.mThumbnailList.get(position);
    }

    private void swap(int dst, int src) {
        Collections.swap(this.mThumbnailList, dst, src);
    }

    public void onMove(int fromPosition, int toPosition) {
        int i;
        if (fromPosition < toPosition) {
            for (i = fromPosition; i < toPosition; i++) {
                swap(i, i + 1);
            }
        } else {
            for (i = fromPosition; i > toPosition; i--) {
                swap(i, i - 1);
            }
        }
        movePage(fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void clear() {
        Iterator it;
        if (this.mSelectedList != null) {
            it = this.mSelectedList.iterator();
            while (it.hasNext()) {
                ((ThumbnailItem) it.next()).setSelected(false);
            }
            this.mSelectedList.clear();
        }
        if (this.mCacheList != null) {
            it = this.mCacheList.iterator();
            while (it.hasNext()) {
                ((ThumbnailItem) it.next()).setBitmap(null);
            }
            this.mCacheList.clear();
        }
        it = this.mThumbnailList.iterator();
        while (it.hasNext()) {
            ((ThumbnailItem) it.next()).closePage();
        }
        if (this.mThumbnailList != null) {
            this.mThumbnailList.clear();
        }
    }

    public void removeSelectedPages() {
        if (this.mSelectedList.size() != 0) {
            this.mSupport.getProgressDialog().setMessage(AppResource.getString(this.mSupport.getContext(), R.string.rv_page_delete));
            this.mSupport.getProgressDialog().setCancelable(false);
            this.mSupport.getProgressDialog().show();
            prepare();
            final ArrayList<ThumbnailItem> tmpSelectList = new ArrayList();
            Iterator it = this.mSelectedList.iterator();
            while (it.hasNext()) {
                tmpSelectList.add((ThumbnailItem) it.next());
            }
            doRemovePages(tmpSelectList, new EditThumbnailCallback() {
                public void result(boolean success) {
                    tmpSelectList.clear();
                    ThumbnailAdapter.this.mSupport.getProgressDialog().dismiss();
                    ThumbnailAdapter.this.notifyDataSetChanged();
                }
            });
        }
    }

    private void prepare() {
        Iterator it = this.mThumbnailList.iterator();
        while (it.hasNext()) {
            ((ThumbnailItem) it.next()).getPage();
        }
    }

    private void doRemovePages(ArrayList<ThumbnailItem> itemList, EditThumbnailCallback callback) {
        this.mPDFViewCtrl.addTask(new EditThumbnailTask(0, (ArrayList) itemList, callback));
    }

    private void doRotatePages(ArrayList<ThumbnailItem> itemList, boolean isClockWise, EditThumbnailCallback callback) {
        this.mPDFViewCtrl.addTask(new EditThumbnailTask(isClockWise ? 1 : 2, (ArrayList) itemList, callback));
    }

    private void importDocument(final int dstIndex, final String filepath, String password) {
        try {
            final PDFDoc doc = PDFDoc.createFromFilePath(filepath);
            doc.load(password == null ? null : password.getBytes());
            final ProgressDialog progressDialog = this.mSupport.getProgressDialog();
            progressDialog.setMessage(AppResource.getString(this.mSupport.getContext(), R.string.rv_page_import));
            int[] ranges = new int[2];
            ranges[1] = doc.getPageCount();
            doImportPages(dstIndex, ranges, doc, new EditThumbnailCallback() {
                public void result(boolean success) {
                    try {
                        doc.release();
                    } catch (PDFException e) {
                        e.printStackTrace();
                    }
                    progressDialog.dismiss();
                    ThumbnailAdapter.this.notifyDataSetChanged();
                }
            });
            progressDialog.show();
        } catch (PDFException e) {
            if (e.getLastError() == 3) {
                String tips;
                if (password == null || password.trim().length() <= 0) {
                    tips = AppResource.getString(this.mSupport.getContext(), R.string.rv_tips_password);
                } else {
                    tips = AppResource.getString(this.mSupport.getContext(), R.string.rv_tips_password_error);
                }
                final UITextEditDialog uiTextEditDialog = new UITextEditDialog(this.mSupport.getContext());
                uiTextEditDialog.getDialog().setCanceledOnTouchOutside(false);
                uiTextEditDialog.getInputEditText().setInputType(129);
                uiTextEditDialog.setTitle(AppResource.getString(this.mSupport.getContext(), R.string.rv_password_dialog_title));
                uiTextEditDialog.getPromptTextView().setText(tips);
                uiTextEditDialog.show();
                uiTextEditDialog.getOKButton().setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        uiTextEditDialog.dismiss();
                        ((InputMethodManager) v.getContext().getSystemService("input_method")).hideSoftInputFromWindow(v.getWindowToken(), 0);
                        ThumbnailAdapter.this.importDocument(dstIndex, filepath, uiTextEditDialog.getInputEditText().getText().toString());
                    }
                });
                uiTextEditDialog.getCancelButton().setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        uiTextEditDialog.dismiss();
                        ((InputMethodManager) v.getContext().getSystemService("input_method")).hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                });
                uiTextEditDialog.getDialog().setOnKeyListener(new OnKeyListener() {
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode != 4) {
                            return false;
                        }
                        uiTextEditDialog.getDialog().cancel();
                        return true;
                    }
                });
                uiTextEditDialog.show();
                return;
            }
            UIToast.getInstance(this.mSupport.getContext()).show(e.getMessage());
        }
    }

    private void doImportPages(int dstIndex, int[] pageRanges, PDFDoc srcDoc, EditThumbnailCallback callback) {
        this.mPDFViewCtrl.addTask(new EditThumbnailTask(dstIndex, pageRanges, srcDoc, callback));
    }

    private void doExtractPages(ArrayList<ThumbnailItem> itemList, String path) {
        final ProgressDialog progressDialog = this.mSupport.getProgressDialog();
        progressDialog.setCancelable(false);
        progressDialog.setMessage(AppResource.getString(this.mSupport.getContext(), R.string.rv_page_extract));
        progressDialog.show();
        this.mPDFViewCtrl.addTask(new EditThumbnailTask((ArrayList) itemList, path, new EditThumbnailCallback() {
            public void result(boolean success) {
                progressDialog.dismiss();
                if (!success) {
                    UIToast.getInstance(ThumbnailAdapter.this.mSupport.getContext()).show(AppResource.getString(ThumbnailAdapter.this.mSupport.getContext(), R.string.rv_page_extract_error));
                }
                ThumbnailAdapter.this.notifyDataSetChanged();
            }
        }));
    }

    private void showInputFileNameDialog(final String fileFolder) {
        String fileName = AppFileUtil.getFileNameWithoutExt(AppFileUtil.getFileDuplicateName(""));
        final UITextEditDialog rmDialog = new UITextEditDialog(this.mSupport.getContext());
        rmDialog.setPattern("[/\\:*?<>|\"\n\t]");
        rmDialog.setTitle(AppResource.getString(this.mSupport.getContext(), R.string.fx_string_extract));
        rmDialog.getPromptTextView().setVisibility(8);
        rmDialog.getInputEditText().setText(fileName);
        rmDialog.getInputEditText().selectAll();
        rmDialog.show();
        AppUtil.showSoftInput(rmDialog.getInputEditText());
        rmDialog.getOKButton().setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                rmDialog.dismiss();
                String newPath = new StringBuilder(String.valueOf(fileFolder + "/" + rmDialog.getInputEditText().getText().toString())).append(".pdf").toString();
                if (new File(newPath).exists()) {
                    ThumbnailAdapter.this.showAskReplaceDialog(fileFolder, newPath);
                } else {
                    ThumbnailAdapter.this.doExtractPages(ThumbnailAdapter.this.mSelectedList, newPath);
                }
            }
        });
    }

    private void showAskReplaceDialog(final String fileFolder, final String newPath) {
        final UITextEditDialog rmDialog = new UITextEditDialog(this.mSupport.getContext());
        rmDialog.setTitle(R.string.fx_string_extract);
        rmDialog.getPromptTextView().setText(R.string.fx_string_filereplace_warning);
        rmDialog.getInputEditText().setVisibility(8);
        rmDialog.show();
        rmDialog.getOKButton().setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                rmDialog.dismiss();
                File file = new File(newPath);
                if (file.delete()) {
                    ThumbnailAdapter.this.doExtractPages(ThumbnailAdapter.this.mSelectedList, newPath);
                } else {
                    UIToast.getInstance(ThumbnailAdapter.this.mSupport.getContext()).show(file.getPath() + " Can not replace.");
                }
            }
        });
        rmDialog.getCancelButton().setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                rmDialog.dismiss();
                ThumbnailAdapter.this.showInputFileNameDialog(fileFolder);
            }
        });
    }

    void rotateSelectedPages() {
        if (this.mSelectedList.size() != 0) {
            this.mSupport.getProgressDialog().setMessage(AppResource.getString(this.mSupport.getContext(), R.string.rv_page_rotate_cw));
            this.mSupport.getProgressDialog().show();
            doRotatePages(this.mSelectedList, true, new EditThumbnailCallback() {
                public void result(boolean success) {
                    ThumbnailAdapter.this.mSupport.getProgressDialog().dismiss();
                    ThumbnailAdapter.this.notifyDataSetChanged();
                }
            });
        }
    }

    private void removePage(int position) {
        if (this.mThumbnailList.size() <= 1) {
            this.mSupport.showTipsDlg(0);
            return;
        }
        ThumbnailItem item = (ThumbnailItem) this.mThumbnailList.get(position);
        final ArrayList<ThumbnailItem> itemArrayList = new ArrayList();
        itemArrayList.add(item);
        doRemovePages(itemArrayList, new EditThumbnailCallback() {
            public void result(boolean success) {
                itemArrayList.clear();
                ThumbnailAdapter.this.notifyDataSetChanged();
            }
        });
    }

    private void rotatePage(int position, boolean isClockWise) {
        final ArrayList<ThumbnailItem> itemArrayList = new ArrayList();
        itemArrayList.add((ThumbnailItem) this.mThumbnailList.get(position));
        doRotatePages(itemArrayList, isClockWise, new EditThumbnailCallback() {
            public void result(boolean success) {
                itemArrayList.clear();
                ThumbnailAdapter.this.notifyDataSetChanged();
            }
        });
    }

    private void movePage(int fromPosition, int toPosition) {
        try {
            this.mPDFViewCtrl.movePage(this.mPDFViewCtrl.getDoc().getPage(fromPosition).getIndex(), toPosition);
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    void importPages(final int dstIndex) {
        UIFileSelectDialog dialog = this.mSupport.getFileSelectDialog();
        dialog.setFileClickedListener(new OnFileClickedListener() {
            public void onFileClicked(String filepath) {
                ThumbnailAdapter.this.importDocument(dstIndex, filepath, null);
            }
        });
        dialog.setHeight(dialog.getDialogHeight());
        dialog.showDialog();
    }

    void extractPages() {
        final UIFolderSelectDialog dialog = this.mSupport.getFolderSelectDialog();
        dialog.setListener(new DialogListener() {
            public void onResult(long btType) {
                if (btType == 4) {
                    ThumbnailAdapter.this.showInputFileNameDialog(dialog.getCurrentPath());
                }
                dialog.dismiss();
            }

            public void onBackClick() {
            }
        });
        dialog.setHeight(dialog.getDialogHeight());
        dialog.showDialog();
    }
}
