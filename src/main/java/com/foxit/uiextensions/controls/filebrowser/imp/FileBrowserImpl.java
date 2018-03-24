package com.foxit.uiextensions.controls.filebrowser.imp;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.controls.filebrowser.FileBrowser;
import com.foxit.uiextensions.controls.filebrowser.FileComparator;
import com.foxit.uiextensions.controls.filebrowser.FileDelegate;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class FileBrowserImpl implements FileBrowser {
    private boolean isEditing;
    private boolean isTouchHold;
    private final List<FileItem> mCheckedItems = new ArrayList();
    private FileComparator mComparator;
    private Context mContext;
    private String mCurrentPath;
    private FileDelegate mDelegate;
    private FileAdapter mFileAdapter;
    private IFB_FileAdapterDelegate mFileAdapterDelegate = new IFB_FileAdapterDelegate() {
        public boolean onItemChecked(boolean checked, int position, FileItem item) {
            FileBrowserImpl fileBrowserImpl;
            if (checked) {
                if (FileBrowserImpl.this.mOnlyOneSelect && FileBrowserImpl.this.mCheckedItems.size() >= 1 && !FileBrowserImpl.this.mCheckedItems.contains(item)) {
                    ((FileItem) FileBrowserImpl.this.mCheckedItems.get(0)).checked = false;
                    FileBrowserImpl.this.mCheckedItems.clear();
                    FileBrowserImpl.this.mCheckedItems.add(item);
                }
                if (!FileBrowserImpl.this.mCheckedItems.contains(item)) {
                    FileBrowserImpl.this.mCheckedItems.add(item);
                }
                if ((item.type & 1) != 0) {
                    fileBrowserImpl = FileBrowserImpl.this;
                    fileBrowserImpl.mFileCounter = fileBrowserImpl.mFileCounter + 1;
                } else {
                    fileBrowserImpl = FileBrowserImpl.this;
                    fileBrowserImpl.mFolderCounter = fileBrowserImpl.mFolderCounter + 1;
                }
            } else if (FileBrowserImpl.this.mCheckedItems.remove(item)) {
                if ((item.type & 1) != 0) {
                    fileBrowserImpl = FileBrowserImpl.this;
                    fileBrowserImpl.mFileCounter = fileBrowserImpl.mFileCounter - 1;
                } else {
                    fileBrowserImpl = FileBrowserImpl.this;
                    fileBrowserImpl.mFolderCounter = fileBrowserImpl.mFolderCounter - 1;
                }
            }
            item.checked = checked;
            FileBrowserImpl.this.updateDataSource(true);
            FileBrowserImpl.this.mDelegate.onItemsCheckedChanged(false, FileBrowserImpl.this.mFolderCounter, FileBrowserImpl.this.mFileCounter);
            return true;
        }

        public boolean isEditState() {
            return FileBrowserImpl.this.isEditing;
        }

        public Context getContext() {
            return FileBrowserImpl.this.mContext;
        }

        public List<FileItem> getDataSource() {
            return FileBrowserImpl.this.mDelegate.getDataSource();
        }

        public void updateItem(String path) {
            if (path != null && path.length() != 0) {
                int start = FileBrowserImpl.this.mListView.getFirstVisiblePosition();
                int i = start;
                int j = FileBrowserImpl.this.mListView.getLastVisiblePosition();
                while (i <= j) {
                    FileItem info = (FileItem) FileItem.class.cast(FileBrowserImpl.this.mListView.getItemAtPosition(i));
                    if (info == null || !path.equals(info.path)) {
                        i++;
                    } else {
                        FileBrowserImpl.this.mFileAdapter.getView(i, FileBrowserImpl.this.mListView.getChildAt(i - start), FileBrowserImpl.this.mListView);
                        return;
                    }
                }
            }
        }
    };
    private int mFileCounter;
    private int mFolderCounter;
    private ListView mListView;
    private boolean mOnlyOneSelect = false;
    private final Stack<Integer> mSelectionStack = new Stack();

    public FileBrowserImpl(Context context, FileDelegate delegate) {
        this.mContext = context;
        this.mDelegate = delegate;
        initView();
    }

    public void showCheckBox(boolean show) {
        if (this.mFileAdapter != null && (this.mFileAdapter instanceof FileListAdapter)) {
            ((FileListAdapter) this.mFileAdapter).showCheckBox(show);
        }
    }

    private void initView() {
        this.mListView = new ListView(this.mContext);
        this.mListView.setLayoutParams(new LayoutParams(-1, -1));
        this.mListView.setCacheColorHint(this.mContext.getResources().getColor(R.color.ux_color_translucent));
        this.mListView.setDivider(new ColorDrawable(this.mContext.getResources().getColor(R.color.ux_color_seperator_gray)));
        this.mListView.setDividerHeight(AppDisplay.getInstance(this.mContext).dp2px(1.0f));
        this.mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileAdapter adapter = (FileAdapter) parent.getAdapter();
                if (adapter != null) {
                    FileItem item = (FileItem) adapter.getItem(position);
                    if (item == null) {
                        return;
                    }
                    if (!FileBrowserImpl.this.isEditing || item.type == FileItem.TYPE_CLOUD_SELECT_FOLDER) {
                        if ((item.type & 16) != 0) {
                            FileBrowserImpl.this.mSelectionStack.push(Integer.valueOf(FileBrowserImpl.this.mListView.getFirstVisiblePosition()));
                        }
                        FileBrowserImpl.this.mDelegate.onItemClicked(view, item);
                    } else if (item.type != 256) {
                        FileBrowserImpl.this.mFileAdapterDelegate.onItemChecked(!item.checked, position, item);
                    }
                }
            }
        });
        this.mListView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (MotionEventCompat.getActionMasked(event)) {
                    case 0:
                    case 1:
                    case 3:
                        if (!FileBrowserImpl.this.isTouchHold) {
                            return false;
                        }
                        FileBrowserImpl.this.isTouchHold = false;
                        return true;
                    default:
                        return false;
                }
            }
        });
        if (this.mFileAdapter == null || !(this.mFileAdapter instanceof FileListAdapter)) {
            this.mFileAdapter = new FileListAdapter(this.mFileAdapterDelegate);
            this.mListView.setAdapter(this.mFileAdapter);
            return;
        }
        this.mListView.setAdapter(this.mFileAdapter);
    }

    public View getContentView() {
        return this.mListView;
    }

    public FileComparator getComparator() {
        if (this.mComparator == null) {
            this.mComparator = new FileComparator();
        }
        return this.mComparator;
    }

    public void setEditState(boolean editState) {
        this.isEditing = editState;
        if (!editState) {
            this.mCheckedItems.clear();
            for (FileItem item : this.mDelegate.getDataSource()) {
                item.checked = false;
            }
        }
        this.mFileCounter = 0;
        this.mFolderCounter = 0;
        this.mDelegate.onItemsCheckedChanged(false, 0, 0);
        updateDataSource(true);
    }

    public void setPath(String currentPath) {
        boolean isFolderBack;
        if (AppUtil.isEmpty(currentPath) || AppUtil.isEmpty(this.mCurrentPath)) {
            isFolderBack = false;
        } else {
            isFolderBack = !this.mCurrentPath.equals(currentPath) && this.mCurrentPath.startsWith(currentPath);
        }
        this.mCurrentPath = currentPath;
        this.mDelegate.onPathChanged(currentPath);
        updateDataSource(true);
        if (!isFolderBack || this.mSelectionStack.empty()) {
            this.mListView.setSelection(0);
        } else {
            this.mListView.setSelection(((Integer) this.mSelectionStack.pop()).intValue());
        }
        clearCheckedItems();
    }

    public String getDisplayPath() {
        return this.mCurrentPath == null ? "" : this.mCurrentPath;
    }

    public void updateDataSource(boolean isOnlyNotify) {
        if (!isOnlyNotify) {
            this.mDelegate.onPathChanged(this.mCurrentPath);
        }
        ((BaseAdapter) this.mListView.getAdapter()).notifyDataSetChanged();
    }

    public List<FileItem> getCheckedItems() {
        return this.mCheckedItems;
    }

    public void clearCheckedItems() {
        for (FileItem item : this.mCheckedItems) {
            item.checked = false;
        }
        this.mCheckedItems.clear();
        this.mFileCounter = 0;
        this.mFolderCounter = 0;
        this.mDelegate.onItemsCheckedChanged(false, this.mFolderCounter, this.mFileCounter);
    }

    public void setOnlyOneSelect(boolean isOne) {
        this.mOnlyOneSelect = isOne;
    }
}
