package com.netspace.teacherpad;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.parser.QuestionParser;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.utilities.ResourceUploadProcess;
import com.netspace.library.utilities.ResourceUploadProcess.ResourceUploadInterface;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.library.window.ChatWindow;
import com.netspace.teacherpad.dialog.QuestionCaptureGetTypeDialog;
import eu.janmuller.android.simplecropimage.CropImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import net.sourceforge.opencamera.MainActivity;
import wei.mark.standout.StandOutWindow;

public class QuestionCaptureActivity extends BaseActivity {
    private static final int CROP_BIG_PICTURE = 2;
    private static final int TAKE_BIG_PICTURE = 1;
    private static boolean mbCurrentActivity = false;
    private Context mContext;
    private int mHeight = 0;
    private Uri mImageUri;
    private String mPictureFileName;
    private QuestionCaptureGetTypeDialog mQuestionTypeDialog;
    private int mWidth = 0;
    private String mszOldPictureFileName;

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;
        this.mQuestionTypeDialog = new QuestionCaptureGetTypeDialog(this);
        this.mQuestionTypeDialog.setCancelable(false);
        Intent it = new Intent(this.mContext, MainActivity.class);
        it.setAction("android.media.action.IMAGE_CAPTURE");
        mbCurrentActivity = true;
        try {
            this.mPictureFileName = File.createTempFile("tmpcamera", ".png", this.mContext.getExternalCacheDir()).getAbsolutePath();
            this.mszOldPictureFileName = File.createTempFile("tmpcamera", ".png", this.mContext.getExternalCacheDir()).getAbsolutePath();
            this.mImageUri = Uri.fromFile(new File(this.mPictureFileName));
            it.putExtra("output", this.mImageUri);
            startActivityForResult(it, 1);
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != -1) {
            new File(this.mPictureFileName).delete();
            this.mPictureFileName = "";
            finish();
        } else if (requestCode == 1) {
            Utilities.copyFile(new File(this.mPictureFileName), new File(this.mszOldPictureFileName));
            Log.d("TakePicture", "m_ImageUri:" + this.mImageUri.toString());
            cropImageUri(this.mImageUri, 640, 480, 2);
        } else if (requestCode == 2) {
            Bitmap SourceBitmap = BitmapFactory.decodeFile(this.mPictureFileName);
            if (SourceBitmap != null) {
                int nWidth = SourceBitmap.getWidth();
                int nHeight = SourceBitmap.getHeight();
                float fScale = 1.0f;
                if (nWidth > nHeight) {
                    if (nWidth > 1280) {
                        fScale = 1280.0f / ((float) nWidth);
                        nWidth = 1280;
                        nHeight = (int) (((float) nHeight) * fScale);
                    }
                } else if (nHeight > 1280) {
                    fScale = 1280.0f / ((float) nHeight);
                    nHeight = 1280;
                    nWidth = (int) (((float) nWidth) * fScale);
                }
                if (fScale != 1.0f) {
                    Bitmap TargetBitmap = Bitmap.createScaledBitmap(SourceBitmap, nWidth, nHeight, true);
                    if (TargetBitmap != null) {
                        try {
                            TargetBitmap.compress(CompressFormat.PNG, 100, new FileOutputStream(this.mPictureFileName));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        TargetBitmap.recycle();
                    }
                }
                this.mWidth = nWidth;
                this.mHeight = nHeight;
                SourceBitmap.recycle();
            }
            this.mQuestionTypeDialog.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    if (!QuestionCaptureActivity.this.isFinishing()) {
                        final String szQuestionGUID = Utilities.createGUID();
                        if (QuestionCaptureActivity.this.mQuestionTypeDialog.getQuestionType().isEmpty()) {
                            final ResourceUploadProcess UploadProcess = new ResourceUploadProcess(QuestionCaptureActivity.this, QuestionCaptureActivity.this.mContext.getExternalCacheDir().getAbsolutePath(), MyiBaseApplication.getCommonVariables().UserInfo.szRealName, null, null);
                            UploadProcess.setType("拍照");
                            UploadProcess.setCallBack(new ResourceUploadInterface() {
                                public void onBeginUpload() {
                                }

                                public void onUploadComplete() {
                                    TeacherPadApplication.IMThread.SendMessage("CameraQuestionImage RES:" + UploadProcess.getResourceGUID(), MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
                                    Utilities.showAlertMessage(QuestionCaptureActivity.this.mContext, "数据提交成功", "题目提交成功，点击确定继续拍摄下一页。", new OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent it = new Intent(QuestionCaptureActivity.this.mContext, MainActivity.class);
                                            it.setAction("android.media.action.IMAGE_CAPTURE");
                                            it.putExtra("output", QuestionCaptureActivity.this.mImageUri);
                                            QuestionCaptureActivity.this.startActivityForResult(it, 1);
                                        }
                                    });
                                }

                                public void onCancel() {
                                    QuestionCaptureActivity.this.finish();
                                }
                            });
                            UploadProcess.startUploadProcess(QuestionCaptureActivity.this.mPictureFileName);
                            return;
                        }
                        QuestionParser question = new QuestionParser(QuestionParser.EMPTY_QUESTION_XML);
                        question.setGUID(szQuestionGUID);
                        question.setQuestionType(QuestionCaptureActivity.this.mQuestionTypeDialog.getQuestionType());
                        if (!QuestionCaptureActivity.this.mQuestionTypeDialog.getCorrectAnswer().isEmpty()) {
                            question.setQuestionAnswer(QuestionCaptureActivity.this.mQuestionTypeDialog.getCorrectAnswer());
                        }
                        question.setMainNodeAttribute("pdfQuestion", "true");
                        question.addBase64File("QuestionContent.png", "image/png", Utilities.getBase64FileContent(QuestionCaptureActivity.this.mPictureFileName));
                        question.setQuestionContent("睿易派拍照的题目");
                        question.setTitle("睿易派拍照的题目");
                        String szQuestionXML = question.getXML();
                        WebServiceCallItemObject CallItem = new WebServiceCallItemObject("AddQuestionByXML", QuestionCaptureActivity.this);
                        CallItem.setSuccessListener(new OnSuccessListener() {
                            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                                TeacherPadApplication.IMThread.SendMessage("CameraQuestionImage " + szQuestionGUID, MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
                                Utilities.showAlertMessage(QuestionCaptureActivity.this.mContext, "数据提交成功", "题目提交成功，点击确定继续截取下一道题目。", new OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Utilities.copyFile(new File(QuestionCaptureActivity.this.mszOldPictureFileName), new File(QuestionCaptureActivity.this.mPictureFileName));
                                        QuestionCaptureActivity.this.cropImageUri(QuestionCaptureActivity.this.mImageUri, 640, 480, 2);
                                    }
                                });
                            }
                        });
                        CallItem.setFailureListener(new OnFailureListener() {
                            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                                Utilities.showAlertMessage(QuestionCaptureActivity.this.mContext, "数据提交出现错误", "数据提交出现错误，错误信息：" + Utilities.getErrorMessage(nReturnCode), new OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        QuestionCaptureActivity.this.finish();
                                    }
                                });
                            }
                        });
                        CallItem.setParam("lpszQuestionXML", szQuestionXML);
                        CallItem.setParam("nFlags", Integer.valueOf(0));
                        CallItem.setUserGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
                        VirtualNetworkObject.addToQueue(CallItem);
                    }
                }
            });
            this.mQuestionTypeDialog.setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    QuestionCaptureActivity.this.finish();
                }
            });
            this.mQuestionTypeDialog.show();
        }
    }

    private void cropImageUri(Uri uri, int outputX, int outputY, int requestCode) {
        Intent intent = new Intent(this.mContext, CropImage.class);
        intent.putExtra(CropImage.IMAGE_PATH, this.mPictureFileName);
        intent.putExtra(CropImage.SCALE, true);
        intent.putExtra(CropImage.ASPECT_X, 0);
        intent.putExtra(CropImage.ASPECT_Y, 0);
        startActivityForResult(intent, requestCode);
    }

    protected void onDestroy() {
        StandOutWindow.restoreAll(this.mContext, ChatWindow.class);
        mbCurrentActivity = false;
        super.onDestroy();
    }

    protected void onResume() {
        StandOutWindow.hideAll(this.mContext, ChatWindow.class);
        super.onResume();
    }

    public static boolean getIsActivity() {
        return mbCurrentActivity;
    }
}
