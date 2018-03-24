package com.netspace.library.controls;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.netspace.library.activity.FingerDrawActivity;
import com.netspace.library.activity.FingerDrawActivity.FingerDrawCallbackInterface;
import com.netspace.library.im.IMCore;
import com.netspace.library.utilities.FaceImageGetter;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.pad.library.R;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import net.sqlcipher.database.SQLiteDatabase;

public class CustomChatDisplayView extends LinearLayout {
    private static ImageView mLastPlayView;
    private static MediaPlayer mPlayer;
    private boolean mAllowPictureReedit = false;
    private Context mContext;
    private FingerDrawCallbackInterface mFingerDrawCallBack;
    private String mGUID = "";
    private OnClickListener mImageClick = new OnClickListener() {
        public void onClick(View v) {
            WebServiceCallItemObject ResourceObject = new WebServiceCallItemObject("GetTemporaryStorage", null);
            ResourceObject.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    String szKey = (String) ItemObject.getParam("lpszKey");
                    Bitmap Bitmap = Utilities.getBase64Bitmap(ItemObject.readTextData());
                    if (Bitmap != null) {
                        int nImageWidth = Bitmap.getWidth();
                        int nImageHeight = Bitmap.getHeight();
                        OutputStream stream = null;
                        try {
                            stream = new FileOutputStream(new StringBuilder(String.valueOf(CustomChatDisplayView.this.mContext.getExternalCacheDir().getAbsolutePath())).append("/").append(szKey).append(".jpg").toString());
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        if (!(stream == null || Bitmap == null)) {
                            Bitmap.compress(CompressFormat.JPEG, 100, stream);
                        }
                        Intent DrawActivity = new Intent(CustomChatDisplayView.this.mContext, FingerDrawActivity.class);
                        if (CustomChatDisplayView.this.mAllowPictureReedit) {
                            FingerDrawActivity.SetCallbackInterface(CustomChatDisplayView.this.mFingerDrawCallBack);
                            DrawActivity.putExtra("imageWidth", nImageWidth);
                            DrawActivity.putExtra("imageHeight", nImageHeight);
                            DrawActivity.putExtra("imageKey", szKey);
                            DrawActivity.putExtra("allowUpload", true);
                            DrawActivity.putExtra("allowCamera", true);
                            DrawActivity.putExtra("uploadName", "DrawPad_" + Utilities.createGUID() + ".jpg");
                            DrawActivity.putExtra("enableBackButton", true);
                        } else {
                            DrawActivity.putExtra("imageKey", szKey);
                            DrawActivity.putExtra("imageWidth", nImageWidth);
                            DrawActivity.putExtra("imageHeight", nImageHeight);
                            DrawActivity.putExtra("allowUpload", false);
                            DrawActivity.putExtra("readonly", true);
                        }
                        DrawActivity.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
                        CustomChatDisplayView.this.mContext.startActivity(DrawActivity);
                        return;
                    }
                    Utilities.showAlertMessage(CustomChatDisplayView.this.mContext, "无法打开图片", "图片数据无效。");
                }
            });
            ResourceObject.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                    Utilities.showAlertMessage(CustomChatDisplayView.this.mContext, "无法打开图片", "无法打开对应的图片。");
                }
            });
            ResourceObject.setAlwaysActiveCallbacks(true);
            ResourceObject.setParam("lpszKey", (String) v.getTag());
            VirtualNetworkObject.addToQueue(ResourceObject);
        }
    };
    private String mOwnerGUID = "";
    private OnClickListener mPlayClick = new OnClickListener() {
        public void onClick(View v) {
            if (CustomChatDisplayView.mPlayer != null) {
                CustomChatDisplayView.mPlayer.stop();
                CustomChatDisplayView.mPlayer.release();
                CustomChatDisplayView.mPlayer = null;
                if (CustomChatDisplayView.mLastPlayView != null) {
                    CustomChatDisplayView.mLastPlayView.setImageResource(R.drawable.ic_playback);
                    return;
                }
                return;
            }
            CustomChatDisplayView.mLastPlayView = (ImageView) v;
            WebServiceCallItemObject ResourceObject = new WebServiceCallItemObject("GetTemporaryStorage", null);
            ResourceObject.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    ItemObject DataObject = ItemObject;
                    String szFileName = new StringBuilder(String.valueOf(CustomChatDisplayView.this.mContext.getExternalCacheDir().getAbsolutePath())).append("/").append((String) ItemObject.getParam("lpszKey")).toString();
                    if (Utilities.writeBase64ToFile(DataObject.readTextData(), szFileName)) {
                        final String szTempFile = szFileName;
                        CustomChatDisplayView.mPlayer = MediaPlayer.create(CustomChatDisplayView.this.mContext, Uri.fromFile(new File(szFileName)));
                        CustomChatDisplayView.mPlayer.setOnCompletionListener(new OnCompletionListener() {
                            public void onCompletion(MediaPlayer mp) {
                                CustomChatDisplayView.mPlayer.release();
                                CustomChatDisplayView.mLastPlayView.setImageResource(R.drawable.ic_playback);
                                new File(szTempFile).delete();
                                CustomChatDisplayView.mPlayer = null;
                                CustomChatDisplayView.mLastPlayView = null;
                            }
                        });
                        CustomChatDisplayView.mPlayer.start();
                        CustomChatDisplayView.mLastPlayView.setImageResource(R.drawable.ic_stopplayback);
                    }
                }
            });
            ResourceObject.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                    Utilities.showAlertMessage(CustomChatDisplayView.this.mContext, "无法播放", "无法获得对应的音频数据。");
                }
            });
            ResourceObject.setAlwaysActiveCallbacks(true);
            ResourceObject.setParam("lpszKey", (String) v.getTag());
            VirtualNetworkObject.addToQueue(ResourceObject);
        }
    };
    private boolean mbIsTextContent = false;
    private String mszTextContent;

    public CustomChatDisplayView(Context context) {
        super(context);
        this.mContext = context;
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.listitem_customchatdisplayview, this);
    }

    public void setAllowImageReedit(boolean bEnable, FingerDrawCallbackInterface DrawCallback) {
        this.mAllowPictureReedit = bEnable;
        this.mFingerDrawCallBack = DrawCallback;
    }

    public void setData(String szDate, String szMessage, View view) {
        if (view == null) {
            view = this;
        }
        TextView textUserName = (TextView) view.findViewById(R.id.textViewMessageFrom);
        TextView textMessage = (TextView) view.findViewById(R.id.textViewMessageText);
        TextView textDate = (TextView) view.findViewById(R.id.textViewMessageTime);
        ImageView imageViewVoice = (ImageView) view.findViewById(R.id.imageViewVoice);
        ImageView imageViewPicture = (ImageView) view.findViewById(R.id.imageViewPicture);
        LinearLayout LayoutVoice = (LinearLayout) view.findViewById(R.id.LinearLayoutVoice);
        TextView textVoiceTime = (TextView) view.findViewById(R.id.textViewTime);
        LinearLayout LayoutExternalIntent = (LinearLayout) view.findViewById(R.id.LinearLayoutExternalIntent);
        if (LayoutExternalIntent != null) {
            LayoutExternalIntent.setVisibility(8);
        }
        int nStartPos;
        if (szMessage.startsWith("VOICE=")) {
            LayoutVoice.setVisibility(0);
            textMessage.setVisibility(8);
            imageViewPicture.setVisibility(8);
            if (szMessage.indexOf(".mp3") != -1) {
                imageViewVoice.setTag(szMessage.substring(6, szMessage.indexOf(".mp3") + 4));
                imageViewVoice.setOnClickListener(this.mPlayClick);
            }
            imageViewVoice.setImageResource(R.drawable.ic_playback);
            nStartPos = szMessage.indexOf("TIME=");
            if (nStartPos == -1 || szMessage.indexOf(";", nStartPos + 6) == -1) {
                textVoiceTime.setVisibility(4);
            } else {
                nStartPos += 6;
                textVoiceTime.setText(szMessage.substring(nStartPos, szMessage.indexOf(";", nStartPos)));
                textVoiceTime.setVisibility(0);
            }
        } else if (szMessage.startsWith("PICTURE=")) {
            LayoutVoice.setVisibility(8);
            textMessage.setVisibility(8);
            imageViewPicture.setVisibility(0);
            if (szMessage.indexOf(".jpg") != -1) {
                imageViewPicture.setTag(szMessage.substring(8, szMessage.indexOf(".jpg") + 4));
                imageViewPicture.setOnClickListener(this.mImageClick);
            }
            nStartPos = szMessage.indexOf("THUMBNAIL=");
            if (nStartPos == -1 || szMessage.indexOf(";", nStartPos + 10) == -1) {
                imageViewPicture.setImageResource(R.drawable.ic_placehold_picture);
            } else {
                nStartPos += 10;
                Bitmap bitmap = Utilities.getBase64Bitmap(szMessage.substring(nStartPos, szMessage.indexOf(";", nStartPos)).replace("<br>", "\n"));
                if (bitmap != null) {
                    imageViewPicture.setImageBitmap(bitmap);
                }
            }
        } else {
            LayoutVoice.setVisibility(8);
            imageViewPicture.setVisibility(8);
            textMessage.setVisibility(0);
            this.mbIsTextContent = true;
        }
        String szMessageText = szMessage;
        String szFrom = IMCore.getChatUserName(szMessageText);
        szMessageText = IMCore.trimMessageText(szMessageText);
        this.mszTextContent = szMessageText;
        textUserName.setText(szFrom);
        textMessage.setText(Html.fromHtml(szMessageText, new FaceImageGetter(this.mContext), null));
        textDate.setText(szDate);
        textUserName.setTextColor(-16776961);
    }

    public boolean getIsText() {
        return this.mbIsTextContent;
    }

    public String getTextContent() {
        return this.mszTextContent;
    }

    public void setGUID(String szGUID) {
        this.mGUID = szGUID;
    }

    public void setOwnerGUID(String szGUID) {
        this.mOwnerGUID = szGUID;
    }

    public String getGUID() {
        return this.mGUID;
    }

    public String getOwnerGUID() {
        return this.mOwnerGUID;
    }
}
