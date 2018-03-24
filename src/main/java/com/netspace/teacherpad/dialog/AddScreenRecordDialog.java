package com.netspace.teacherpad.dialog;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.parser.ResourceParser;
import com.netspace.library.utilities.Utilities;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.TeacherPadApplication;
import io.vov.vitamio.MediaMetadataRetriever;

public class AddScreenRecordDialog extends Dialog {
    private EditText mEditTextName;
    private OnClickListener mOnClickListener;
    private String mszGUID;
    private String mszRecordFullFileName;
    private String mszTitle;
    private String mszXMLFullFileName;

    public AddScreenRecordDialog(Context context) {
        super(context);
    }

    public void setFileName(String szRecordFullName, String szXMLFileName) {
        this.mszRecordFullFileName = szRecordFullName;
        this.mszXMLFullFileName = szXMLFileName;
    }

    public void setOnOKClickListener(OnClickListener OnClickListener) {
        this.mOnClickListener = OnClickListener;
    }

    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(1);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_addscreenrecord);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle((CharSequence) "添加屏幕录像");
        toolbar.setTitleTextColor(-1);
        this.mEditTextName = (EditText) findViewById(R.id.editText1);
        this.mEditTextName.setText(Utilities.getNowDateChinese() + "的屏幕录像");
        findViewById(R.id.buttonCancel).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                new Builder(AddScreenRecordDialog.this.getContext()).setTitle("取消确认").setMessage("确实取消吗？取消上传会删除刚才的屏幕录像").setPositiveButton("是", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AddScreenRecordDialog.this.dismiss();
                    }
                }).setNegativeButton("否", null).show();
            }
        });
        findViewById(R.id.buttonOK).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String szTitle = AddScreenRecordDialog.this.mEditTextName.getText().toString();
                if (szTitle.isEmpty()) {
                    Utilities.showAlertMessage(AddScreenRecordDialog.this.getContext(), "请输入名称", "请输入保存名称");
                    return;
                }
                ResourceParser Parser = new ResourceParser();
                if (Parser.initialize(AddScreenRecordDialog.this.getContext(), ResourceParser.mszEmptyResourceXML)) {
                    AddScreenRecordDialog.this.mszGUID = Utilities.createGUID();
                    AddScreenRecordDialog.this.mszTitle = szTitle;
                    Parser.setGUID(AddScreenRecordDialog.this.mszGUID);
                    Parser.setTitle(szTitle);
                    Parser.setContentMainFileURL(Utilities.getFileName(AddScreenRecordDialog.this.mszRecordFullFileName));
                    Parser.setSummery(szTitle);
                    Parser.setAttribute(MediaMetadataRetriever.METADATA_KEY_DATE, Utilities.getNow());
                    Parser.setAttribute(MediaMetadataRetriever.METADATA_KEY_AUTHOR, MyiBaseApplication.getCommonVariables().UserInfo.szRealName);
                    Parser.setAttribute("mainFileExtName", "mp4");
                    Parser.setAttribute("type", "平板录屏");
                    Parser.addKnowledgePoints(TeacherPadApplication.mPersonalKPPath, TeacherPadApplication.mPersonalKPGUID);
                    Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(AddScreenRecordDialog.this.mszRecordFullFileName, 2);
                    if (bitmap != null) {
                        Parser.setThumbnail(bitmap);
                    }
                    Utilities.writeTextToFile(AddScreenRecordDialog.this.mszXMLFullFileName, Parser.getXML());
                }
                if (AddScreenRecordDialog.this.mOnClickListener != null) {
                    AddScreenRecordDialog.this.mOnClickListener.onClick(null);
                }
                AddScreenRecordDialog.this.dismiss();
            }
        });
    }

    public String getGUID() {
        return this.mszGUID;
    }

    public String getTitle() {
        return this.mszTitle;
    }
}
