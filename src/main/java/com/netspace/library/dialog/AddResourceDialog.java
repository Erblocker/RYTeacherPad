package com.netspace.library.dialog;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import com.netspace.library.parser.ResourceParser;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import io.vov.vitamio.MediaMetadataRetriever;
import java.io.File;

public class AddResourceDialog extends Dialog {
    private EditText mEditSummery;
    private EditText mEditTextName;
    private OnClickListener mOnCancelListener;
    private OnClickListener mOnClickListener;
    private String mszKPPathGUID;
    private String mszKPPathText;
    private String mszRealName;
    private String mszResourceFullFileName;
    private String mszType;
    private String mszXMLFullFileName;

    public AddResourceDialog(Context context) {
        super(context);
    }

    public void setFileName(String szResourceFullName, String szXMLFileName, String szRealName, String szKPPath, String szKPGUID) {
        this.mszResourceFullFileName = szResourceFullName;
        this.mszXMLFullFileName = szXMLFileName;
        this.mszRealName = szRealName;
        this.mszKPPathGUID = szKPGUID;
        this.mszKPPathText = szKPPath;
    }

    public void setFileType(String szType) {
        this.mszType = szType;
    }

    public void setOnOKClickListener(OnClickListener OnClickListener) {
        this.mOnClickListener = OnClickListener;
    }

    public void setOnCancelClickListener(OnClickListener OnClickListener) {
        this.mOnCancelListener = OnClickListener;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("添加资源");
        setContentView(R.layout.dialog_addresource);
        if (this.mszKPPathGUID == null) {
            findViewById(R.id.textView2).setVisibility(8);
        }
        this.mEditTextName = (EditText) findViewById(R.id.editText1);
        this.mEditSummery = (EditText) findViewById(R.id.editText2);
        findViewById(R.id.buttonCancel).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                new Builder(AddResourceDialog.this.getContext()).setTitle("取消确认").setMessage("确实取消吗？").setPositiveButton("是", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        new File(AddResourceDialog.this.mszResourceFullFileName).delete();
                        AddResourceDialog.this.dismiss();
                        if (AddResourceDialog.this.mOnCancelListener != null) {
                            AddResourceDialog.this.mOnCancelListener.onClick(null);
                        }
                    }
                }).setNegativeButton("否", null).show();
            }
        });
        findViewById(R.id.buttonOK).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String szTitle = AddResourceDialog.this.mEditTextName.getText().toString();
                String szSummery = AddResourceDialog.this.mEditSummery.getText().toString();
                if (szTitle.isEmpty()) {
                    Utilities.showAlertMessage(AddResourceDialog.this.getContext(), "请输入名称", "请输入保存名称");
                    return;
                }
                ResourceParser Parser = new ResourceParser();
                String szExtName = Utilities.getFileExtName(AddResourceDialog.this.mszResourceFullFileName);
                if (Parser.initialize(AddResourceDialog.this.getContext(), ResourceParser.mszEmptyResourceXML)) {
                    Parser.setGUID(Utilities.createGUID());
                    Parser.setTitle(szTitle);
                    Parser.setContentMainFileURL(Utilities.getFileName(AddResourceDialog.this.mszResourceFullFileName));
                    Parser.setSummery(szSummery);
                    Parser.setAttribute(MediaMetadataRetriever.METADATA_KEY_DATE, Utilities.getNow());
                    Parser.setAttribute(MediaMetadataRetriever.METADATA_KEY_AUTHOR, AddResourceDialog.this.mszRealName);
                    Parser.setAttribute("mainFileExtName", szExtName);
                    if (AddResourceDialog.this.mszType != null) {
                        Parser.setAttribute("type", AddResourceDialog.this.mszType);
                    }
                    Parser.addKnowledgePoints(AddResourceDialog.this.mszKPPathText, AddResourceDialog.this.mszKPPathGUID);
                    if (szExtName.equalsIgnoreCase("mp4")) {
                        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(AddResourceDialog.this.mszResourceFullFileName, 2);
                        if (bitmap != null) {
                            Parser.setThumbnail(bitmap);
                        }
                    }
                    if (szExtName.equalsIgnoreCase("jpg") || szExtName.equalsIgnoreCase("png")) {
                        Bitmap sourceBitmap = Utilities.loadBitmapFromFile(AddResourceDialog.this.mszResourceFullFileName);
                        Parser.setThumbnail(ThumbnailUtils.extractThumbnail(sourceBitmap, 256, 256));
                        sourceBitmap.recycle();
                    }
                    Utilities.writeTextToFile(AddResourceDialog.this.mszXMLFullFileName, Parser.getXML());
                }
                if (AddResourceDialog.this.mOnClickListener != null) {
                    AddResourceDialog.this.mOnClickListener.onClick(null);
                }
                AddResourceDialog.this.dismiss();
            }
        });
    }

    public String getTitle() {
        return this.mEditTextName.getText().toString();
    }
}
