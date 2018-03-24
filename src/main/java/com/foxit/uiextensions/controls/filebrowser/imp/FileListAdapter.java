package com.foxit.uiextensions.controls.filebrowser.imp;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.controls.filebrowser.imp.FileThumbnail.ThumbnailCallback;
import com.foxit.uiextensions.utils.AppDisplay;

class FileListAdapter extends FileAdapter {
    private Context mContext;
    private boolean mShowCheckBox = true;
    private ThumbnailCallback mThumbnailCallback = new ThumbnailCallback() {
        public void result(boolean succeed, final String filePath) {
            if (succeed) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        FileListAdapter.this.mDelegate.updateItem(filePath);
                    }
                });
            }
        }
    };

    protected FileListAdapter(IFB_FileAdapterDelegate delegate) {
        super(delegate);
        this.mContext = delegate.getContext();
    }

    public void showCheckBox(boolean show) {
        this.mShowCheckBox = show;
    }

    public int getCount() {
        if (this.mDelegate.getDataSource() == null) {
            return 0;
        }
        return this.mDelegate.getDataSource().size();
    }

    public FileItem getItem(int position) {
        try {
            return (FileItem) this.mDelegate.getDataSource().get(position);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        int i = 8;
        FileItem item = (FileItem) this.mDelegate.getDataSource().get(position);
        if (item == null) {
            throw new NullPointerException("item == null");
        }
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            if (AppDisplay.getInstance(this.mContext).isPad()) {
                convertView = View.inflate(this.mContext, R.layout.fb_file_item_pad, null);
            } else {
                convertView = View.inflate(this.mContext, R.layout.fb_file_item_phone, null);
            }
            holder.searchFolderLayout = convertView.findViewById(R.id.fb_item_search_layout);
            holder.searchFolderPathTextView = (TextView) convertView.findViewById(R.id.fb_item_search_path);
            holder.commonLayout = convertView.findViewById(R.id.fb_item_common_layout);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.fb_item_checkbox);
            holder.iconImageView = (ImageView) convertView.findViewById(R.id.fb_item_icon);
            holder.nameTextView = (TextView) convertView.findViewById(R.id.fb_item_name);
            holder.dateTextView = (TextView) convertView.findViewById(R.id.fb_item_date);
            holder.sizeTextView = (TextView) convertView.findViewById(R.id.fb_item_size);
            holder.fileCount = (TextView) convertView.findViewById(R.id.fb_item_filecount);
            holder.checkBox.setTag(new ClickListener(holder.checkBox, position) {
                public void onClick(View v) {
                    FileItem item = FileListAdapter.this.getItem(getPosition());
                    if (((CompoundButton) v).isChecked()) {
                        if (FileListAdapter.this.mDelegate.onItemChecked(true, getPosition(), item)) {
                            ((CompoundButton) v).setChecked(true);
                        } else {
                            ((CompoundButton) v).setChecked(item.checked);
                        }
                    } else if (FileListAdapter.this.mDelegate.onItemChecked(false, getPosition(), item)) {
                        ((CompoundButton) v).setChecked(false);
                    } else {
                        ((CompoundButton) v).setChecked(item.checked);
                    }
                }
            });
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            ((ClickListener) holder.checkBox.getTag()).update(position);
        }
        if (!this.mDelegate.isEditState()) {
            holder.checkBox.setVisibility(8);
        } else if (item.type != 256) {
            holder.checkBox.setVisibility(0);
            holder.checkBox.setChecked(item.checked);
        } else {
            holder.checkBox.setVisibility(8);
        }
        holder.iconImageView.setBackgroundDrawable(null);
        switch (item.type) {
            case 0:
                setVisibility(holder.searchFolderLayout, false);
                setVisibility(holder.commonLayout, true);
                setVisibility(holder.sizeTextView, false);
                setVisibility(holder.fileCount, true);
                holder.fileCount.setText(new StringBuilder(String.valueOf(item.fileCount)).toString());
                holder.iconImageView.setImageResource(R.drawable.fb_file_dir);
                break;
            case 16:
                setVisibility(holder.searchFolderLayout, false);
                setVisibility(holder.commonLayout, true);
                setVisibility(holder.sizeTextView, false);
                setVisibility(holder.fileCount, true);
                holder.fileCount.setText(new StringBuilder(String.valueOf(item.fileCount)).toString());
                holder.iconImageView.setImageResource(R.drawable.fb_file_dir);
                break;
            case 256:
                setVisibility(holder.searchFolderLayout, true);
                setVisibility(holder.commonLayout, false);
                holder.searchFolderPathTextView.setText(item.path == null ? "" : item.path);
                setVisibility(holder.fileCount, false);
                break;
            case 257:
                setVisibility(holder.searchFolderLayout, false);
                setVisibility(holder.commonLayout, true);
                setVisibility(holder.sizeTextView, true);
                setIcon(holder.iconImageView, item.path);
                setVisibility(holder.fileCount, false);
                break;
            case FileItem.TYPE_CLOUD_SELECT_FILE /*65537*/:
                int i2;
                setVisibility(holder.searchFolderLayout, false);
                setVisibility(holder.commonLayout, true);
                setVisibility(holder.sizeTextView, true);
                setVisibility(holder.fileCount, false);
                CheckBox checkBox = holder.checkBox;
                if (this.mShowCheckBox) {
                    i2 = 0;
                } else {
                    i2 = 8;
                }
                checkBox.setVisibility(i2);
                setIcon(holder.iconImageView, item.path);
                break;
            case FileItem.TYPE_CLOUD_SELECT_FOLDER /*65552*/:
                setVisibility(holder.searchFolderLayout, false);
                setVisibility(holder.commonLayout, true);
                setVisibility(holder.sizeTextView, false);
                setVisibility(holder.fileCount, true);
                holder.fileCount.setText(new StringBuilder(String.valueOf(item.fileCount)).toString());
                CheckBox checkBox2 = holder.checkBox;
                if (this.mShowCheckBox) {
                    i = 4;
                }
                checkBox2.setVisibility(i);
                holder.iconImageView.setImageResource(R.drawable.fb_file_dir);
                break;
            case FileItem.TYPE_TARGET_FILE /*1048577*/:
                setVisibility(holder.searchFolderLayout, false);
                setVisibility(holder.commonLayout, true);
                setVisibility(holder.sizeTextView, true);
                setVisibility(holder.fileCount, false);
                setIcon(holder.iconImageView, item.path);
                break;
            case FileItem.TYPE_TARGET_FOLDER /*1048592*/:
                setVisibility(holder.searchFolderLayout, false);
                setVisibility(holder.commonLayout, true);
                setVisibility(holder.sizeTextView, false);
                setVisibility(holder.fileCount, true);
                holder.fileCount.setText(new StringBuilder(String.valueOf(item.fileCount)).toString());
                holder.iconImageView.setImageResource(R.drawable.fb_file_dir);
                break;
            default:
                setVisibility(holder.searchFolderLayout, false);
                setVisibility(holder.commonLayout, true);
                setVisibility(holder.sizeTextView, true);
                setVisibility(holder.fileCount, false);
                setIcon(holder.iconImageView, item.path);
                break;
        }
        holder.nameTextView.setText(item.name == null ? "" : item.name);
        holder.sizeTextView.setText(item.size == null ? "" : item.size);
        holder.dateTextView.setText(item.date == null ? "" : item.date);
        return convertView;
    }

    public static boolean isSupportThumbnail(String name) {
        String extension = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
        if (extension == null || extension.length() == 0 || !extension.equals("ofd")) {
            return false;
        }
        return true;
    }

    private void setIcon(ImageView iconImageView, String path) {
        Bitmap bitmap;
        if (isSupportThumbnail(path)) {
            bitmap = FileThumbnail.getInstance(this.mContext).getThumbnail(path, this.mThumbnailCallback);
            if (bitmap != null) {
                iconImageView.setBackgroundDrawable(this.mContext.getResources().getDrawable(R.drawable.fb_file_pdf_bg));
                iconImageView.setImageBitmap(bitmap);
                return;
            }
            return;
        }
        int drawableId = FileAdapter.getDrawableByFileName(path);
        if (drawableId == -1) {
            bitmap = FileThumbnail.getInstance(this.mContext).getThumbnail(path, this.mThumbnailCallback);
            if (bitmap == null) {
                iconImageView.setImageResource(R.drawable.fb_file_pdf);
                return;
            }
            iconImageView.setBackgroundDrawable(this.mContext.getResources().getDrawable(R.drawable.fb_file_pdf_bg));
            iconImageView.setImageBitmap(bitmap);
            return;
        }
        iconImageView.setImageResource(drawableId);
    }

    private void setVisibility(View view, boolean visible) {
        view.setVisibility(visible ? 0 : 8);
    }
}
