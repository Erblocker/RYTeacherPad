package com.netspace.library.controls;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.netspace.library.activity.FingerDrawActivity;
import com.netspace.library.activity.FingerDrawActivity.FingerDrawCallbackInterface;
import com.netspace.library.dialog.FaceSelectDialog;
import com.netspace.library.dialog.FaceSelectDialog.OnFaceSelectListener;
import com.netspace.library.dialog.VoiceRecordDialog;
import com.netspace.library.dialog.VoiceRecordDialog.OnRecordSendListener;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.FaceImageGetter;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.pad.library.R;

public class CustomChatInputView extends LinearLayout implements OnClickListener {
    private static String mszAppendText = "";
    private CustomChatCallBack mCallBack;
    private Context mContext;
    private FingerDrawCallbackInterface mDrawCallBackInterface = new FingerDrawCallbackInterface() {
        public void OnFingerDrawCreate(Activity Activity) {
        }

        public boolean HasMJpegClients() {
            return false;
        }

        public void OnUpdateMJpegImage(Bitmap bitmap, Activity Activity) {
        }

        public void OnProject(Activity Activity) {
        }

        public void OnDestroy(Activity Activity) {
        }

        public void OnBroadcast(Activity Activity) {
        }

        public void OnPenAction(String szAction, float fX, float fY, int nWidth, int nHeight, Activity Activity) {
        }

        public void OnOK(Bitmap bitmap, final String szUploadName, final Activity Activity) {
            Bitmap bitmapSmall = ThumbnailUtils.extractThumbnail(bitmap, 48, 48);
            final String szBitmap = Utilities.saveBitmapToBase64String(bitmapSmall);
            String szEncodedData = Utilities.saveBitmapToBase64String(bitmap);
            bitmapSmall.recycle();
            WebServiceCallItemObject CallItem = new WebServiceCallItemObject("PutTemporaryStorage", UI.getCurrentActivity());
            CallItem.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    CustomChatInputView.this.mbTrimBr = true;
                    CustomChatInputView.this.sendText("PICTURE=" + szUploadName + ",THUMBNAIL=" + szBitmap + ";");
                    Activity.finish();
                }
            });
            CallItem.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                    Utilities.showAlertMessage(Activity, "保存图片失败", "图片保存出现问题。");
                }
            });
            CallItem.setParam("lpszBase64Data", szEncodedData);
            CallItem.setParam("szKey", szUploadName);
            VirtualNetworkObject.addToQueue(CallItem);
        }
    };
    private ImageButton mDrawPadButton;
    private String mDrawPadKey;
    private ImageButton mFaceButton;
    private ImageButton mSendButton;
    private EditText mTextToSend;
    private ImageButton mVoiceButton;
    private boolean mbTrimBr = false;

    public interface CustomChatCallBack {
        void OnSendMessage(String str, String str2);
    }

    public CustomChatInputView(Context context) {
        super(context);
        int nThemeID = getResources().getIdentifier(PreferenceManager.getDefaultSharedPreferences(context).getString("MainTheme", "AppTheme"), "style", context.getPackageName());
        if (nThemeID != 0) {
            this.mContext = new ContextThemeWrapper(context, nThemeID);
        }
        ((LayoutInflater) context.getSystemService("layout_inflater")).cloneInContext(this.mContext).inflate(R.layout.layout_customchatview, this);
        this.mTextToSend = (EditText) findViewById(R.id.editText1);
        ImageButton imageButton = (ImageButton) findViewById(R.id.imageButtonSend);
        this.mSendButton = imageButton;
        imageButton.setOnClickListener(this);
        this.mFaceButton = (ImageButton) findViewById(R.id.imageButtonFace);
        this.mFaceButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v, "CustomChatInputView");
                new FaceSelectDialog(CustomChatInputView.this.mContext, new OnFaceSelectListener() {
                    public void OnFaceSelected(int nIndex, int nResID) {
                        CustomChatInputView.this.mTextToSend.append(Html.fromHtml("<img src='" + String.valueOf(nIndex) + "'/>", new FaceImageGetter(CustomChatInputView.this.mContext), null));
                    }
                }).show();
            }
        });
        this.mVoiceButton = (ImageButton) findViewById(R.id.imageButtonVoice);
        this.mVoiceButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v, "CustomChatInputView");
                VoiceRecordDialog dialog = new VoiceRecordDialog(CustomChatInputView.this.mContext, new OnRecordSendListener() {
                    public void OnRecordSend(String szLocalFileName, VoiceRecordDialog Dialog, String szTime) {
                        CustomChatInputView.this.sendText("VOICE=" + szLocalFileName + ",TIME=" + szTime + ";");
                        Dialog.dismiss();
                    }
                });
                dialog.setCancelable(false);
                dialog.show();
            }
        });
        this.mDrawPadButton = (ImageButton) findViewById(R.id.imageButtonDrawPad);
        this.mDrawPadButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v, "CustomChatInputView");
                int nImageWidth = Utilities.getScreenWidth(CustomChatInputView.this.mContext);
                int nImageHeight = Utilities.getScreenHeight(CustomChatInputView.this.mContext);
                CustomChatInputView.this.mDrawPadKey = "DrawPad_" + Utilities.createGUID() + ".jpg";
                Intent DrawActivity = new Intent(CustomChatInputView.this.mContext, FingerDrawActivity.class);
                FingerDrawActivity.SetCallbackInterface(CustomChatInputView.this.mDrawCallBackInterface);
                DrawActivity.putExtra("imageWidth", nImageWidth);
                DrawActivity.putExtra("imageHeight", nImageHeight);
                DrawActivity.putExtra("allowUpload", true);
                DrawActivity.putExtra("allowCamera", true);
                DrawActivity.putExtra("uploadName", CustomChatInputView.this.mDrawPadKey);
                DrawActivity.putExtra("enableBackButton", true);
                DrawActivity.setFlags(335544320);
                CustomChatInputView.this.mContext.startActivity(DrawActivity);
            }
        });
    }

    public FingerDrawCallbackInterface getFingerCallBack() {
        return this.mDrawCallBackInterface;
    }

    public void setCallBack(CustomChatCallBack CallBack) {
        this.mCallBack = CallBack;
    }

    public static void setAppendString(String szText) {
        mszAppendText = szText;
    }

    public void setHintText(String szText) {
        this.mTextToSend.setHint(szText);
    }

    public void clearText() {
        this.mTextToSend.setText("");
    }

    public void sendText(String szMessageText) {
        if (this.mbTrimBr) {
            szMessageText = szMessageText.replace("<br>", "\n");
            this.mbTrimBr = false;
        }
        this.mCallBack.OnSendMessage(this.mTextToSend.getText().toString(), new StringBuilder(String.valueOf(szMessageText)).append(mszAppendText).toString());
    }

    public void onClick(View v) {
        Utilities.logClick(v, "CustomChatInputView");
        if (this.mCallBack != null && !this.mTextToSend.getText().toString().isEmpty()) {
            sendText(Utilities.trimToHtml(Html.toHtml(this.mTextToSend.getText())));
            this.mTextToSend.setText("");
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.mFaceButton.setEnabled(enabled);
        this.mVoiceButton.setEnabled(enabled);
        this.mDrawPadButton.setEnabled(enabled);
        this.mTextToSend.setEnabled(enabled);
        this.mSendButton.setEnabled(enabled);
        if (enabled) {
            this.mFaceButton.setAlpha(1.0f);
            this.mVoiceButton.setAlpha(1.0f);
            this.mDrawPadButton.setAlpha(1.0f);
            this.mTextToSend.setAlpha(1.0f);
            this.mSendButton.setAlpha(1.0f);
            return;
        }
        this.mFaceButton.setAlpha(0.6f);
        this.mVoiceButton.setAlpha(0.6f);
        this.mDrawPadButton.setAlpha(0.6f);
        this.mTextToSend.setAlpha(0.6f);
        this.mSendButton.setAlpha(0.6f);
    }
}
