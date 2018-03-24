package com.netspace.teacherpad.controls;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.internal.view.SupportMenu;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.controls.DrawView;
import com.netspace.library.controls.DrawView.DrawViewActionInterface;
import com.netspace.library.controls.LockableScrollView;
import com.netspace.library.utilities.Utilities;
import com.netspace.teacherpad.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.protocol.HTTP;

public class CustomQuestionBlock extends LinearLayout implements DrawViewActionInterface, OnClickListener {
    private boolean mAllowHalfScore = true;
    private boolean mCaching = false;
    private boolean mCancelCache = false;
    private onScoreChangeListener mChangeListener;
    private boolean mChanged = false;
    private View mContentView;
    private Context mContext;
    private DrawView mDrawView;
    private float mFinalMinusScore = 0.0f;
    private float mFullScore = 20.0f;
    private boolean mImageChanged = false;
    private int mImageIndex = 1;
    private boolean mImageLoaded = false;
    private boolean mLastProgressVisible = false;
    private int mLastSelectIndex = -1;
    private LinearLayout mLayoutFrame;
    private LinearLayout mLayoutTools;
    private boolean mLoadImageStart = false;
    private String mPaperID = "";
    private LockableScrollView mParentScrollView;
    private int mQuestionIndex = 0;
    private boolean mScoreSet = false;
    private String mStudentID = "";
    private float mTempScore = 0.0f;
    private TextView mTextViewScore;
    private String mURL = "";
    private boolean mUploading;
    private String mUserClassGUID = "";
    private boolean mbScoreSelected = false;

    public interface onScoreChangeListener {
        void onDrawViewChanged(CustomQuestionBlock customQuestionBlock);

        void onImageLoaded(CustomQuestionBlock customQuestionBlock);

        void onScoreChanged(CustomQuestionBlock customQuestionBlock, float f);
    }

    public CustomQuestionBlock(Context context) {
        super(context);
        this.mContext = context;
        this.mContentView = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.layout_customquestionblock, this);
        this.mDrawView = (DrawView) this.mContentView.findViewById(R.id.drawView1);
        this.mDrawView.setCallback(this);
        this.mDrawView.setOnlyActivePen(true);
        this.mLayoutTools = (LinearLayout) this.mContentView.findViewById(R.id.LayoutToolButtons);
        this.mLayoutFrame = (LinearLayout) this.mContentView.findViewById(R.id.LinearLayout1);
        this.mTextViewScore = (TextView) this.mContentView.findViewById(R.id.textViewScore);
        this.mTextViewScore.setText("尚未批改");
        this.mTextViewScore.setBackgroundColor(SupportMenu.CATEGORY_MASK);
        this.mTextViewScore.setOnClickListener(this);
    }

    public void setScoreChangeListener(onScoreChangeListener onChangeListener) {
        this.mChangeListener = onChangeListener;
    }

    public void setImageInfo(String szPaperID, String szUserClassGUID, String szStudentID, int nImageIndex, int nQuestionIndex) {
        this.mPaperID = szPaperID;
        this.mUserClassGUID = szUserClassGUID;
        this.mStudentID = szStudentID;
        this.mImageIndex = nImageIndex;
        this.mQuestionIndex = nQuestionIndex;
        this.mURL = String.format(MyiBaseApplication.getProtocol() + "://%s/setpaperimage/?paperid=%s&userclassguid=%s&studentid=%s&imageindex=%d&questionindex=%d&flags=4", new Object[]{MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress, szPaperID, szUserClassGUID, szStudentID, Integer.valueOf(nImageIndex), Integer.valueOf(nQuestionIndex)});
        this.mImageLoaded = false;
        this.mLoadImageStart = false;
        Picasso.with(this.mContext).load(this.mURL).into(new Target() {
            public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from) {
                Utilities.setViewBackground(CustomQuestionBlock.this.mDrawView, new BitmapDrawable(CustomQuestionBlock.this.mContext.getResources(), bitmap));
            }

            public void onBitmapFailed(Drawable errorDrawable) {
                Log.d("CustomQuestionBlock", "FAILED");
            }

            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        });
    }

    public void setChanged(boolean bChanged) {
        this.mChanged = bChanged;
    }

    public boolean getChanged() {
        return this.mChanged;
    }

    public boolean getImageChanged() {
        return this.mImageChanged;
    }

    public void cleanImage() {
        if (this.mDrawView.getBackground() != null) {
            Drawable Drawable = this.mDrawView.getBackground();
            this.mDrawView.setBackgroundDrawable(null);
            this.mImageLoaded = false;
        }
    }

    public void loadImage() {
        if (!this.mCaching && !this.mImageLoaded) {
            Log.d("CustomQuestionBlock", "add to addLoadFromCacheTask");
        }
    }

    public boolean isCaching() {
        return this.mCaching;
    }

    public void cancelCache() {
        this.mCancelCache = true;
    }

    public boolean getScoreSet() {
        return this.mScoreSet;
    }

    public void setScore(float fFullScore, boolean bAllowHalfScore) {
        this.mFullScore = fFullScore;
        this.mAllowHalfScore = bAllowHalfScore;
    }

    public void setMinusScore(float fScore) {
        this.mTextViewScore.setText(String.format("扣 %4.1f 分", new Object[]{Float.valueOf(fScore)}));
        this.mTextViewScore.setBackgroundColor(-16711936);
        this.mFinalMinusScore = fScore;
        this.mScoreSet = true;
    }

    private void showScorePickDialog() {
        float fTempScore;
        this.mbScoreSelected = false;
        float fSep = 1.0f;
        if (this.mAllowHalfScore) {
            fSep = 0.5f;
        }
        ArrayList<String> arrScores = new ArrayList();
        for (fTempScore = 0.0f; fTempScore <= this.mFullScore; fTempScore += fSep) {
            if (fTempScore >= fSep) {
                arrScores.add("-" + String.format("%1.1f", new Object[]{Float.valueOf(fTempScore)}));
            }
        }
        arrScores.add("此题得满分/不扣分");
        int nDefaultIndex = arrScores.size() - 1;
        for (fTempScore = this.mFullScore; fTempScore > 0.0f; fTempScore -= fSep) {
            if (fTempScore < this.mFullScore) {
                arrScores.add("+" + String.format("%1.1f", new Object[]{Float.valueOf(fTempScore)}));
            }
        }
        if (this.mLastSelectIndex == -1) {
            if (this.mFinalMinusScore == 0.0f) {
                this.mLastSelectIndex = nDefaultIndex;
            } else {
                String szTargetValue = "-" + String.format("%1.1f", new Object[]{Float.valueOf(this.mFinalMinusScore)});
                for (int i = 0; i < arrScores.size(); i++) {
                    if (((String) arrScores.get(i)).equalsIgnoreCase(szTargetValue)) {
                        this.mLastSelectIndex = i;
                        break;
                    }
                }
            }
        }
        final String[] arrNames = (String[]) arrScores.toArray(new String[arrScores.size()]);
        new Builder(new ContextThemeWrapper(getContext(), 16974130)).setItems(arrNames, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (arrNames[which].equalsIgnoreCase("此题得满分/不扣分")) {
                    CustomQuestionBlock.this.mTempScore = 0.0f;
                } else {
                    float fScore = Float.valueOf(arrNames[which]).floatValue();
                    if (fScore < 0.0f) {
                        CustomQuestionBlock.this.mTempScore = -fScore;
                    } else {
                        CustomQuestionBlock.this.mTempScore = CustomQuestionBlock.this.mFullScore - fScore;
                    }
                }
                CustomQuestionBlock.this.mFinalMinusScore = CustomQuestionBlock.this.mTempScore;
                CustomQuestionBlock.this.setMinusScore(CustomQuestionBlock.this.mFinalMinusScore);
                CustomQuestionBlock.this.mbScoreSelected = true;
                CustomQuestionBlock.this.mChanged = true;
                if (CustomQuestionBlock.this.mChangeListener != null) {
                    CustomQuestionBlock.this.mChangeListener.onScoreChanged(CustomQuestionBlock.this, CustomQuestionBlock.this.mFinalMinusScore);
                }
            }
        }).setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
            }
        }).setTitle("选择分值").create().show();
    }

    public boolean checkVisibleArea() {
        Rect scrollBounds = new Rect();
        getHitRect(scrollBounds);
        if (getLocalVisibleRect(scrollBounds)) {
            return true;
        }
        return false;
    }

    public void uploadImage() {
        if (!this.mUploading) {
            this.mUploading = true;
            new AsyncTask<Void, Void, Bitmap>() {
                protected Bitmap doInBackground(Void... params) {
                    Bitmap bitmap = null;
                    Log.d("CustomQuestionBlock", "upload to server.");
                    try {
                        HttpURLConnection connection = (HttpURLConnection) new URL(CustomQuestionBlock.this.mURL).openConnection();
                        connection.setDoInput(true);
                        connection.setDoOutput(true);
                        connection.setUseCaches(false);
                        connection.setRequestMethod(HttpPost.METHOD_NAME);
                        connection.setRequestProperty(HTTP.CONTENT_TYPE, "image/jpeg");
                        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                        bitmap = CustomQuestionBlock.this.mDrawView.saveToBitmap();
                        if (bitmap != null) {
                            bitmap.compress(CompressFormat.JPEG, 100, outputStream);
                        }
                        outputStream.flush();
                        outputStream.close();
                        int serverResponseCode = connection.getResponseCode();
                        String serverResponseMessage = connection.getResponseMessage();
                        Log.d("CustomQuestionBlock", "nCode = " + serverResponseCode);
                        if (serverResponseCode == 200) {
                            CustomQuestionBlock.this.setChanged(false);
                            CustomQuestionBlock.this.mImageChanged = false;
                        }
                        connection.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (bitmap != null) {
                        bitmap.recycle();
                    }
                    return null;
                }

                protected void onPostExecute(Bitmap result) {
                    CustomQuestionBlock.this.mUploading = false;
                }
            }.execute(new Void[0]);
        }
    }

    public void onClick(View v) {
        showScorePickDialog();
    }

    public void setBrushMode(boolean bOn) {
        if (this.mParentScrollView == null) {
            for (View TempView = (View) getParent(); TempView != null; TempView = (View) TempView.getParent()) {
                if (TempView instanceof LockableScrollView) {
                    this.mParentScrollView = (LockableScrollView) TempView;
                    break;
                }
            }
        }
        this.mDrawView.setEnableCache(true);
        this.mDrawView.setBrushMode(bOn);
        this.mDrawView.setEraseMode(false);
        this.mDrawView.changeWidth(5);
        this.mDrawView.setColor(SupportMenu.CATEGORY_MASK);
    }

    public void setEraseMode(boolean bOn) {
        if (this.mParentScrollView == null) {
            for (View TempView = (View) getParent(); TempView != null; TempView = (View) TempView.getParent()) {
                if (TempView instanceof LockableScrollView) {
                    this.mParentScrollView = (LockableScrollView) TempView;
                    break;
                }
            }
        }
        this.mDrawView.setEnableCache(true);
        this.mDrawView.setBrushMode(false);
        this.mDrawView.setEraseMode(bOn);
        this.mDrawView.changeWidth(5);
        this.mDrawView.setColor(SupportMenu.CATEGORY_MASK);
    }

    public void release() {
        this.mDrawView.setEnableCache(false);
    }

    public void OnTouchDown() {
        if (this.mDrawView.getBrushMode() || this.mDrawView.getEraseMode()) {
            this.mChanged = true;
            this.mImageChanged = true;
            if (this.mChangeListener != null) {
                this.mChangeListener.onDrawViewChanged(this);
            }
        }
        if (this.mContext instanceof DrawViewActionInterface) {
            this.mContext.OnTouchDown();
        }
    }

    public void OnTouchUp() {
        if (this.mContext instanceof DrawViewActionInterface) {
            this.mContext.OnTouchUp();
        }
    }

    public void OnPenButtonDown() {
        if (this.mDrawView.getBrushMode()) {
            this.mDrawView.setBrushMode(false);
            this.mDrawView.setEraseMode(true);
        } else if (this.mDrawView.getEraseMode()) {
            this.mDrawView.setBrushMode(true);
            this.mDrawView.setEraseMode(false);
        }
    }

    public void OnPenButtonUp() {
    }

    public void OnPenAction(String szAction, float fX, float fY, int nWidth, int nHeight) {
    }

    protected void onDetachedFromWindow() {
        Utilities.unbindDrawables(this);
        super.onDetachedFromWindow();
    }

    public void OnTouchPen() {
    }

    public void OnTouchFinger() {
    }

    public void OnTouchMove() {
    }
}
