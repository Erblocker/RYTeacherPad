package com.netspace.library.adapter;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.media.ThumbnailUtils;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.esotericsoftware.wildcard.Paths;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.struct.LessonClassData;
import com.netspace.pad.library.R;
import java.io.File;
import java.util.ArrayList;

public class LessonClassAdapter extends Adapter<ViewHolder> {
    private Context mContext;
    private OnClickListener mOnClickListener;
    private ArrayList<LessonClassData> marrData;

    public static class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        public CardView mCardView;
        public ImageView mDownloadedImage;
        public ImageView mImagePlayIcon;
        public TextView mLastPlayTime;
        public TextView mLessonTitle;
        public TextView mStartTime;
        public TextView mTeacherName;
        public ImageView mThumbnail;

        public ViewHolder(final LessonClassAdapter adapter, View itemView, final Context context, OnClickListener OnClickListener) {
            super(itemView);
            this.mLessonTitle = (TextView) itemView.findViewById(R.id.textViewTitle);
            this.mStartTime = (TextView) itemView.findViewById(R.id.textViewTime);
            this.mLastPlayTime = (TextView) itemView.findViewById(R.id.textViewLastPlayTime);
            this.mTeacherName = (TextView) itemView.findViewById(R.id.textViewTeacherName);
            this.mDownloadedImage = (ImageView) itemView.findViewById(R.id.imageViewDownload);
            this.mImagePlayIcon = (ImageView) itemView.findViewById(R.id.imageViewPlayIcon);
            this.mThumbnail = (ImageView) itemView.findViewById(R.id.imageViewThumbnail);
            this.mDownloadedImage.setImageDrawable(new IconDrawable(context, FontAwesomeIcons.fa_download).colorRes(17170443).actionBarSize());
            this.mCardView = (CardView) itemView.findViewById(R.id.cardViewLessonClass);
            this.mCardView.setOnClickListener(OnClickListener);
            this.mCardView.setOnLongClickListener(new OnLongClickListener() {
                public boolean onLongClick(View v) {
                    if (v.getTag() != null) {
                        final LessonClassData classData = (LessonClassData) v.getTag();
                        if (classData.bDownloaded) {
                            CharSequence[] colors = new CharSequence[]{"删除离线视频"};
                            Builder builder = new Builder(context);
                            builder.setTitle("选择动作");
                            final Context context = context;
                            final LessonClassAdapter lessonClassAdapter = adapter;
                            builder.setItems(colors, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 0) {
                                        for (File file : new Paths(context.getExternalCacheDir().getAbsolutePath(), new StringBuilder(String.valueOf(classData.szGUID)).append("*.mp4").toString()).getFiles()) {
                                            file.delete();
                                        }
                                        classData.bDownloaded = false;
                                        if (classData.bmThumbnail != null) {
                                            classData.bmThumbnail.recycle();
                                        }
                                        classData.bmThumbnail = null;
                                        lessonClassAdapter.notifyDataSetChanged();
                                    }
                                }
                            });
                            builder.show();
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
    }

    public LessonClassAdapter(Context context, ArrayList<LessonClassData> arrData) {
        this.mContext = context;
        this.marrData = arrData;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public int getItemCount() {
        return this.marrData.size();
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(this, LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_lessonclass, viewGroup, false), this.mContext, this.mOnClickListener);
    }

    public void onBindViewHolder(ViewHolder ViewHolder, int arg1) {
        LessonClassData classData = (LessonClassData) this.marrData.get(arg1);
        ViewHolder.mLessonTitle.setText(classData.szTitle);
        if (classData.szUserClassName.isEmpty()) {
            ViewHolder.mStartTime.setText(classData.szDateTime);
        } else {
            ViewHolder.mStartTime.setText(classData.szDateTime + " 在 " + classData.szUserClassName + " 上课");
        }
        ViewHolder.mTeacherName.setText(classData.szTeacherName);
        ViewHolder.mLessonTitle.setText(classData.szTitle);
        if (classData.szTeacherName.isEmpty()) {
            ViewHolder.mTeacherName.setVisibility(4);
        } else {
            ViewHolder.mTeacherName.setVisibility(0);
        }
        if (classData.bDownloaded) {
            ViewHolder.mDownloadedImage.setVisibility(0);
        } else {
            ViewHolder.mDownloadedImage.setVisibility(4);
        }
        if (!classData.szLocalFileName.isEmpty() && classData.bmThumbnail == null) {
            classData.bmThumbnail = ThumbnailUtils.createVideoThumbnail(classData.szLocalFileName, 2);
        }
        if (classData.bmThumbnail != null) {
            ViewHolder.mThumbnail.setImageBitmap(classData.bmThumbnail);
        } else {
            ViewHolder.mThumbnail.setImageResource(R.drawable.ic_cardview_placehold_1);
        }
        if (classData.szLastPlayPos == null || classData.szLastPlayPos.isEmpty()) {
            ViewHolder.mLastPlayTime.setVisibility(4);
        } else {
            ViewHolder.mLastPlayTime.setText("上次播放到" + classData.szLastPlayPos);
            ViewHolder.mLastPlayTime.setVisibility(0);
        }
        ViewHolder.mCardView.setTag(classData);
    }
}
