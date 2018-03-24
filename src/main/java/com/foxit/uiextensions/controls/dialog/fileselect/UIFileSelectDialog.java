package com.foxit.uiextensions.controls.dialog.fileselect;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.controls.dialog.UIMatchDialog;
import com.foxit.uiextensions.controls.filebrowser.FileDelegate;
import com.foxit.uiextensions.controls.filebrowser.imp.FileBrowserImpl;
import com.foxit.uiextensions.controls.filebrowser.imp.FileItem;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.AppFileUtil;
import com.foxit.uiextensions.utils.AppStorageManager;
import com.foxit.uiextensions.utils.AppUtil;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UIFileSelectDialog extends UIMatchDialog {
    private RelativeLayout mContentView;
    private Context mContext;
    private FileItem mCurrentItem;
    private FileBrowserImpl mFileBrowser;
    private FileFilter mFileFilter = new FileFilter() {
        public boolean accept(File pathname) {
            if (pathname.isHidden() || !pathname.canRead()) {
                return false;
            }
            return true;
        }
    };
    private List<FileItem> mFileItems = new ArrayList();
    private OnFileClickedListener mListener = null;
    private RelativeLayout mPathLayout;

    public interface OnFileClickedListener {
        void onFileClicked(String str);
    }

    public UIFileSelectDialog(Context context) {
        super(context, 0, true);
        this.mContext = context;
        onCreateView(true);
    }

    public void setFileClickedListener(OnFileClickedListener listener) {
        this.mListener = listener;
    }

    public UIFileSelectDialog(Context context, OnFileClickedListener listener) {
        super(context, 0, true);
        this.mContext = context;
        onCreateView(false);
        this.mFileBrowser.setEditState(false);
        this.mListener = listener;
    }

    public View onCreateView(boolean showImage) {
        this.mContentView = (RelativeLayout) View.inflate(this.mContext, R.layout.cloud_select_file, null);
        RelativeLayout fileBrowserView = (RelativeLayout) this.mContentView.findViewById(R.id.select_file_file_browser);
        this.mPathLayout = (RelativeLayout) this.mContentView.findViewById(R.id.select_file_path);
        TextView mTextView = new TextView(this.mContext);
        mTextView.setSingleLine();
        mTextView.setText(R.string.hm_back);
        mTextView.setTextColor(this.mContext.getResources().getColorStateList(R.color.hm_back_color_selector));
        mTextView.setGravity(19);
        mTextView.setPadding(AppDisplay.getInstance(this.mContext).dp2px(6.0f), 0, 0, 0);
        mTextView.setTextSize(1, 15.0f);
        ImageView imageView = new ImageView(this.mContext);
        imageView.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.pathctl_back));
        LinearLayout mLinearLayout = new LinearLayout(this.mContext);
        mLinearLayout.setOrientation(0);
        if (AppDisplay.getInstance(this.mContext).isPad()) {
            mLinearLayout.setPadding(AppDisplay.getInstance(this.mContext).dp2px(26.0f), 0, 0, 0);
        } else {
            mLinearLayout.setPadding(AppDisplay.getInstance(this.mContext).dp2px(13.0f), 0, 0, 0);
        }
        LayoutParams saParams = new LayoutParams(-2, -1);
        mLinearLayout.addView(imageView, saParams);
        mLinearLayout.addView(mTextView, saParams);
        if (AppDisplay.getInstance(this.mContext).isPad()) {
            saParams = new LayoutParams(-1, (int) this.mContext.getResources().getDimension(R.dimen.ux_list_item_height_2l_pad));
        } else {
            saParams = new LayoutParams(-1, (int) this.mContext.getResources().getDimension(R.dimen.ux_list_item_height_2l_phone));
        }
        saParams.gravity = 16;
        this.mPathLayout.addView(mLinearLayout);
        this.mPathLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String path = UIFileSelectDialog.this.mFileBrowser.getDisplayPath();
                if (path != null && path.length() != 0) {
                    if (AppStorageManager.getInstance(UIFileSelectDialog.this.mContext).getVolumePaths().contains(path)) {
                        UIFileSelectDialog.this.mFileBrowser.setPath(null);
                        return;
                    }
                    int lastIndex = path.lastIndexOf(File.separator);
                    if (lastIndex != -1) {
                        UIFileSelectDialog.this.mFileBrowser.setPath(path.substring(0, lastIndex));
                        return;
                    }
                    UIFileSelectDialog.this.mFileBrowser.setPath(null);
                }
            }
        });
        this.mFileBrowser = new FileBrowserImpl(this.mContext, new FileDelegate() {
            public List<FileItem> getDataSource() {
                return UIFileSelectDialog.this.mFileItems;
            }

            public void onPathChanged(String path) {
                int i = 0;
                FileItem item;
                if (AppUtil.isEmpty(path)) {
                    UIFileSelectDialog.this.mPathLayout.setVisibility(8);
                    UIFileSelectDialog.this.mFileItems.clear();
                    for (String p : AppStorageManager.getInstance(UIFileSelectDialog.this.mContext).getVolumePaths()) {
                        File f = new File(p);
                        item = new FileItem();
                        item.parentPath = path;
                        item.path = f.getPath();
                        item.name = f.getName();
                        item.date = AppDmUtil.getLocalDateString(AppDmUtil.javaDateToDocumentDate(f.lastModified()));
                        item.lastModifyTime = f.lastModified();
                        item.type = FileItem.TYPE_CLOUD_SELECT_FOLDER;
                        File[] fs = f.listFiles(UIFileSelectDialog.this.mFileFilter);
                        if (fs != null) {
                            item.fileCount = fs.length;
                        } else {
                            item.fileCount = 0;
                        }
                        UIFileSelectDialog.this.mFileItems.add(item);
                    }
                    return;
                }
                UIFileSelectDialog.this.mPathLayout.setVisibility(0);
                UIFileSelectDialog.this.mFileItems.clear();
                File file = new File(path);
                if (file.exists()) {
                    File[] files = file.listFiles(UIFileSelectDialog.this.mFileFilter);
                    if (files != null) {
                        int length = files.length;
                        while (i < length) {
                            f = files[i];
                            item = new FileItem();
                            item.parentPath = path;
                            item.path = f.getPath();
                            item.name = f.getName();
                            item.size = AppFileUtil.formatFileSize(f.length());
                            item.date = AppDmUtil.getLocalDateString(AppDmUtil.javaDateToDocumentDate(f.lastModified()));
                            if (f.isFile()) {
                                item.type = FileItem.TYPE_CLOUD_SELECT_FILE;
                            } else {
                                item.type = FileItem.TYPE_CLOUD_SELECT_FOLDER;
                                item.fileCount = f.listFiles(UIFileSelectDialog.this.mFileFilter).length;
                            }
                            UIFileSelectDialog.this.mFileItems.add(item);
                            i++;
                        }
                        Collections.sort(UIFileSelectDialog.this.mFileItems, UIFileSelectDialog.this.mFileBrowser.getComparator());
                    }
                }
            }

            public void onItemClicked(View view, FileItem item) {
                UIFileSelectDialog.this.mCurrentItem = item;
                if (UIFileSelectDialog.this.mListener == null || item.type != FileItem.TYPE_CLOUD_SELECT_FILE) {
                    UIFileSelectDialog.this.mFileBrowser.setPath(item.path);
                    return;
                }
                UIFileSelectDialog.this.dismiss();
                UIFileSelectDialog.this.mListener.onFileClicked(item.path);
            }

            public void onItemsCheckedChanged(boolean isAllSelected, int folderCount, int fileCount) {
                if (fileCount > 0) {
                    UIFileSelectDialog.this.setButtonEnable(true, 4);
                    UIFileSelectDialog.this.setButtonEnable(true, 128);
                    return;
                }
                UIFileSelectDialog.this.setButtonEnable(false, 4);
                UIFileSelectDialog.this.setButtonEnable(false, 128);
            }
        });
        setButtonEnable(false, 4);
        setButtonEnable(false, 128);
        this.mFileBrowser.setEditState(true);
        this.mFileBrowser.showCheckBox(showImage);
        fileBrowserView.addView(this.mFileBrowser.getContentView());
        this.mCurrentItem = new FileItem();
        this.mCurrentItem.path = null;
        setContentView(this.mContentView);
        setTitleBlueLineVisible(true);
        setBackButtonVisible(8);
        return this.mContentView;
    }

    public List<FileItem> getSelectedFiles() {
        return this.mFileBrowser.getCheckedItems();
    }

    public void init(FileFilter fileFilter, boolean isSelectOnlyOneFile) {
        if (fileFilter != null) {
            this.mFileFilter = fileFilter;
        }
        this.mFileBrowser.setPath(AppFileUtil.getSDPath());
        this.mFileBrowser.setOnlyOneSelect(isSelectOnlyOneFile);
    }
}
