package com.foxit.uiextensions.controls.filebrowser.imp;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.foxit.uiextensions.R;
import java.util.List;

abstract class FileAdapter extends BaseAdapter {
    protected IFB_FileAdapterDelegate mDelegate;

    protected static abstract class ClickListener implements OnClickListener {
        private int position;

        ClickListener(View view, int position) {
            view.setOnClickListener(this);
            this.position = position;
        }

        void update(int position) {
            this.position = position;
        }

        int getPosition() {
            return this.position;
        }
    }

    protected interface IFB_FileAdapterDelegate {
        Context getContext();

        List<FileItem> getDataSource();

        boolean isEditState();

        boolean onItemChecked(boolean z, int i, FileItem fileItem);

        void updateItem(String str);
    }

    protected static final class ViewHolder {
        public CheckBox checkBox;
        public View commonLayout;
        public TextView dateTextView;
        public TextView fileCount;
        public ImageView iconImageView;
        public TextView nameTextView;
        public View searchFolderLayout;
        public TextView searchFolderPathTextView;
        public TextView sizeTextView;

        protected ViewHolder() {
        }
    }

    public FileAdapter(IFB_FileAdapterDelegate delegate) {
        this.mDelegate = delegate;
    }

    public static int getDrawableByFileName(String name) {
        if (name == null || name.length() == 0) {
            return R.drawable.fb_file_other;
        }
        String extension = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
        if (extension == null || extension.length() == 0) {
            return R.drawable.fb_file_other;
        }
        if (extension.equals("doc") || extension.equals("docx")) {
            return R.drawable.fb_file_doc;
        }
        if (extension.equals("xls") || extension.equals("xlsx")) {
            return R.drawable.fb_file_xls;
        }
        if (extension.equals("jpg")) {
            return R.drawable.fb_file_jpg;
        }
        if (extension.equals("png")) {
            return R.drawable.fb_file_png;
        }
        if (extension.equals("txt")) {
            return R.drawable.fb_file_txt;
        }
        if (extension.equals("xml")) {
            return R.drawable.fb_file_xml;
        }
        if (extension.equals("pdf")) {
            return -1;
        }
        if (extension.equals("ppdf")) {
            return R.drawable.fb_file_ppdf;
        }
        return R.drawable.fb_file_other;
    }
}
