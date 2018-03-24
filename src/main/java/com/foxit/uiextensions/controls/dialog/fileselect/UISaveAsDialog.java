package com.foxit.uiextensions.controls.dialog.fileselect;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.controls.dialog.MatchDialog.DialogListener;
import com.foxit.uiextensions.controls.dialog.UITextEditDialog;
import com.foxit.uiextensions.utils.AppFileUtil;
import com.foxit.uiextensions.utils.AppResource;
import com.foxit.uiextensions.utils.AppUtil;
import java.io.File;
import java.io.FileFilter;

public class UISaveAsDialog {
    private ISaveAsOnOKClickCallBack mCallback;
    private Context mContext;
    private String mCurrentFilePath;
    private UIFolderSelectDialog mFolderSelectDialog;
    private boolean mOnlySelectFolder = false;
    private String mOrigName = "";
    private String mSaveExpandedName;

    public interface ISaveAsOnOKClickCallBack {
        void onCancelClick();

        void onOkClick(String str);
    }

    public UISaveAsDialog(Context context, String currentFilePath, String saveExpandedName, ISaveAsOnOKClickCallBack callback) {
        this.mContext = context;
        this.mCurrentFilePath = currentFilePath;
        this.mSaveExpandedName = saveExpandedName;
        this.mCallback = callback;
        if (AppFileUtil.getFileFolder(currentFilePath).length() > 0) {
            this.mFolderSelectDialog = new UIFolderSelectDialog(context);
        } else {
            this.mFolderSelectDialog = new UIFolderSelectDialog(context);
        }
        this.mFolderSelectDialog.setFileFilter(new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.isHidden() || !pathname.canRead()) {
                    return false;
                }
                if (!pathname.isFile() || pathname.getName().toLowerCase().endsWith("pdf")) {
                    return true;
                }
                return false;
            }
        });
        this.mFolderSelectDialog.setTitle(AppResource.getString(this.mContext, R.string.fx_string_saveas));
        this.mFolderSelectDialog.setButton(5);
        this.mFolderSelectDialog.setListener(new DialogListener() {
            public void onResult(long btType) {
                if (btType == 4) {
                    String fileFolder = UISaveAsDialog.this.mFolderSelectDialog.getCurrentPath();
                    if (UISaveAsDialog.this.mOnlySelectFolder) {
                        String newPath = new StringBuilder(String.valueOf(fileFolder)).append("/").append(UISaveAsDialog.this.mOrigName).append(".").append(UISaveAsDialog.this.mSaveExpandedName).toString();
                        if (new File(newPath).exists()) {
                            UISaveAsDialog.this.showAskReplaceDialog(newPath, fileFolder);
                        } else {
                            UISaveAsDialog.this.mCallback.onOkClick(newPath);
                        }
                    } else {
                        UISaveAsDialog.this.showInputFileNameDialog(fileFolder);
                    }
                } else if (btType == 1) {
                    UISaveAsDialog.this.mCallback.onCancelClick();
                }
                UISaveAsDialog.this.mFolderSelectDialog.dismiss();
            }

            public void onBackClick() {
            }
        });
        this.mFolderSelectDialog.showDialog();
    }

    public void showDialog() {
        this.mOnlySelectFolder = false;
        this.mFolderSelectDialog.showDialog();
    }

    public void showDialog(boolean onlySelectFolder, String origName) {
        this.mOnlySelectFolder = onlySelectFolder;
        this.mOrigName = origName;
        this.mFolderSelectDialog.showDialog();
    }

    private void showInputFileNameDialog(final String fileFolder) {
        String fileName = AppFileUtil.getFileNameWithoutExt(AppFileUtil.getFileDuplicateName(this.mCurrentFilePath));
        final UITextEditDialog rmDialog = new UITextEditDialog(this.mContext);
        rmDialog.setPattern("[/\\:*?<>|\"\n\t]");
        rmDialog.setTitle(AppResource.getString(this.mContext, R.string.fx_string_saveas));
        rmDialog.getPromptTextView().setVisibility(8);
        rmDialog.getInputEditText().setText(fileName);
        rmDialog.getInputEditText().selectAll();
        rmDialog.show();
        AppUtil.showSoftInput(rmDialog.getInputEditText());
        rmDialog.getOKButton().setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                rmDialog.dismiss();
                String newPath = fileFolder + "/" + rmDialog.getInputEditText().getText().toString() + "." + UISaveAsDialog.this.mSaveExpandedName;
                if (new File(newPath).exists()) {
                    UISaveAsDialog.this.showAskReplaceDialog(newPath, fileFolder);
                } else {
                    UISaveAsDialog.this.mCallback.onOkClick(newPath);
                }
            }
        });
        rmDialog.getCancelButton().setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                rmDialog.dismiss();
                UISaveAsDialog.this.mCallback.onCancelClick();
            }
        });
    }

    private void showAskReplaceDialog(final String filePath, final String fileFolder) {
        final UITextEditDialog rmDialog = new UITextEditDialog(this.mContext);
        rmDialog.setTitle(R.string.fx_string_saveas);
        rmDialog.getPromptTextView().setText(R.string.fx_string_filereplace_warning);
        rmDialog.getInputEditText().setVisibility(8);
        rmDialog.show();
        rmDialog.getOKButton().setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                rmDialog.dismiss();
                UISaveAsDialog.this.mCallback.onOkClick(filePath);
            }
        });
        rmDialog.getCancelButton().setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                rmDialog.dismiss();
                UISaveAsDialog.this.showInputFileNameDialog(fileFolder);
            }
        });
    }
}
