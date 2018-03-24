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

public class UIFolderSelectDialog extends UIMatchDialog {
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
    private RelativeLayout mPathLayout;

    public UIFolderSelectDialog(Context context) {
        super(context, 0, true);
        this.mContext = context;
        onCreateView();
    }

    public View onCreateView() {
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
                String path = UIFolderSelectDialog.this.mFileBrowser.getDisplayPath();
                if (path != null && path.length() != 0) {
                    if (AppStorageManager.getInstance(UIFolderSelectDialog.this.mContext).getVolumePaths().contains(path)) {
                        UIFolderSelectDialog.this.mFileBrowser.setPath(null);
                        return;
                    }
                    int lastIndex = path.lastIndexOf(File.separator);
                    if (lastIndex != -1) {
                        UIFolderSelectDialog.this.mFileBrowser.setPath(path.substring(0, lastIndex));
                        return;
                    }
                    UIFolderSelectDialog.this.mFileBrowser.setPath(null);
                }
            }
        });
        this.mFileBrowser = new FileBrowserImpl(this.mContext, new FileDelegate() {
            public List<FileItem> getDataSource() {
                return UIFolderSelectDialog.this.mFileItems;
            }

            public void onPathChanged(String path) {
                int i = 0;
                File f;
                FileItem item;
                if (AppUtil.isEmpty(path)) {
                    UIFolderSelectDialog.this.setButtonEnable(false, 4);
                    UIFolderSelectDialog.this.mPathLayout.setVisibility(8);
                    UIFolderSelectDialog.this.mFileItems.clear();
                    for (String p : AppStorageManager.getInstance(UIFolderSelectDialog.this.mContext).getVolumePaths()) {
                        f = new File(p);
                        item = new FileItem();
                        item.parentPath = path;
                        item.path = f.getPath();
                        item.name = f.getName();
                        item.date = AppDmUtil.getLocalDateString(AppDmUtil.javaDateToDocumentDate(f.lastModified()));
                        item.lastModifyTime = f.lastModified();
                        item.type = FileItem.TYPE_TARGET_FOLDER;
                        File[] fs = f.listFiles(UIFolderSelectDialog.this.mFileFilter);
                        if (fs != null) {
                            item.fileCount = fs.length;
                        } else {
                            item.fileCount = 0;
                        }
                        if (AppStorageManager.getInstance(UIFolderSelectDialog.this.mContext).checkStorageCanWrite(f.getPath())) {
                            UIFolderSelectDialog.this.mFileItems.add(item);
                        }
                    }
                    return;
                }
                UIFolderSelectDialog.this.setButtonEnable(true, 4);
                UIFolderSelectDialog.this.mPathLayout.setVisibility(0);
                UIFolderSelectDialog.this.mFileItems.clear();
                File file = new File(path);
                if (file.exists()) {
                    File[] files = file.listFiles(UIFolderSelectDialog.this.mFileFilter);
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
                                item.type = FileItem.TYPE_TARGET_FILE;
                            } else {
                                item.type = FileItem.TYPE_TARGET_FOLDER;
                                item.fileCount = f.listFiles(UIFolderSelectDialog.this.mFileFilter).length;
                            }
                            item.length = f.length();
                            UIFolderSelectDialog.this.mFileItems.add(item);
                            i++;
                        }
                        Collections.sort(UIFolderSelectDialog.this.mFileItems, UIFolderSelectDialog.this.mFileBrowser.getComparator());
                    }
                }
            }

            public void onItemClicked(View view, FileItem item) {
                if ((item.type & 1) <= 0) {
                    UIFolderSelectDialog.this.mCurrentItem = item;
                    UIFolderSelectDialog.this.mFileBrowser.setPath(item.path);
                }
            }

            public void onItemsCheckedChanged(boolean isAllSelected, int folderCount, int fileCount) {
            }
        });
        fileBrowserView.addView(this.mFileBrowser.getContentView());
        this.mCurrentItem = new FileItem();
        this.mCurrentItem.path = AppFileUtil.getSDPath();
        setContentView(this.mContentView);
        setTitleBlueLineVisible(true);
        setBackButtonVisible(8);
        return this.mContentView;
    }

    public String getCurrentPath() {
        return this.mCurrentItem.path;
    }

    public void setFileFilter(FileFilter fileFilter) {
        if (fileFilter != null) {
            this.mFileFilter = fileFilter;
        }
        this.mFileBrowser.setPath(AppFileUtil.getSDPath());
    }
}
