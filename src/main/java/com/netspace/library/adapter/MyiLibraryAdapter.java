package com.netspace.library.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.pad.library.R;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class MyiLibraryAdapter extends Adapter<ViewHolder> {
    private Context mContext;
    private OnClickListener mOnClickListener;
    private ArrayList<ResourceItemData> marrData;
    private boolean mbCanShare = true;
    private boolean mbReadOnly = false;

    public static class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        public LinearLayout mCardView;
        public TextView mDate;
        public TextView mDescription;
        public TextView mFolderName;
        public ImageView mImageDeleteIcon;
        public ImageView mImageRenameIcon;
        public ImageView mImageShareIcon;
        public ImageView mThumbnail;
        public TextView mTitle;

        public ViewHolder(MyiLibraryAdapter adapter, View itemView, Context context, OnClickListener OnClickListener) {
            super(itemView);
            this.mTitle = (TextView) itemView.findViewById(R.id.textViewTitle);
            this.mDate = (TextView) itemView.findViewById(R.id.textViewTime);
            this.mDescription = (TextView) itemView.findViewById(R.id.textViewContent);
            this.mFolderName = (TextView) itemView.findViewById(R.id.textViewFolderName);
            this.mThumbnail = (ImageView) itemView.findViewById(R.id.imageViewThumbnail);
            this.mImageShareIcon = (ImageView) itemView.findViewById(R.id.imageViewShare);
            this.mImageDeleteIcon = (ImageView) itemView.findViewById(R.id.imageViewDelete);
            this.mImageRenameIcon = (ImageView) itemView.findViewById(R.id.imageViewRename);
            this.mImageShareIcon.setImageDrawable(new IconDrawable(context, FontAwesomeIcons.fa_share_alt).colorRes(17170432).actionBarSize());
            this.mImageDeleteIcon.setImageDrawable(new IconDrawable(context, FontAwesomeIcons.fa_trash_o).colorRes(17170432).actionBarSize());
            this.mImageRenameIcon.setImageDrawable(new IconDrawable(context, FontAwesomeIcons.fa_pencil).colorRes(17170432).actionBarSize());
            this.mImageShareIcon.setOnClickListener(OnClickListener);
            this.mImageDeleteIcon.setOnClickListener(OnClickListener);
            this.mImageRenameIcon.setOnClickListener(OnClickListener);
            this.mCardView = (LinearLayout) itemView.findViewById(R.id.cardViewResource);
            this.mCardView.setOnClickListener(OnClickListener);
        }
    }

    public MyiLibraryAdapter(Context context, ArrayList<ResourceItemData> arrData) {
        this.mContext = context;
        this.marrData = arrData;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

    public void setReadOnly(boolean bReadOnly) {
        this.mbReadOnly = bReadOnly;
    }

    public void setCanShare(boolean bCanShare) {
        this.mbCanShare = bCanShare;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public int getItemCount() {
        return this.marrData.size();
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(this, LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_resourceitem, viewGroup, false), this.mContext, this.mOnClickListener);
    }

    public static int getResourceIconByType(int nType) {
        if (nType == 0) {
            return R.drawable.ic_question;
        }
        if (nType >= 1000 && nType <= 1999) {
            return R.drawable.ic_lesson;
        }
        if (nType == 11) {
            return R.drawable.ic_multimedia;
        }
        if (nType == 21) {
            return R.drawable.ic_audio;
        }
        if (nType == 41) {
            return R.drawable.ic_picture;
        }
        if (nType < 4000 || nType > 4999) {
            return R.drawable.ic_multimedia;
        }
        return R.drawable.ic_class;
    }

    public void onBindViewHolder(ViewHolder ViewHolder, int arg1) {
        ResourceItemData data = (ResourceItemData) this.marrData.get(arg1);
        Picasso.with(this.mContext).cancelRequest(ViewHolder.mThumbnail);
        if (data.bFolder) {
            ViewHolder.mFolderName.setText(data.szTitle);
            ViewHolder.mFolderName.setVisibility(0);
            ViewHolder.mTitle.setVisibility(4);
            ViewHolder.mDate.setVisibility(4);
            ViewHolder.mDescription.setVisibility(8);
            ViewHolder.mImageShareIcon.setVisibility(8);
            ViewHolder.mImageRenameIcon.setVisibility(0);
            ViewHolder.mImageDeleteIcon.setVisibility(0);
            if (data.szGUID.equalsIgnoreCase("back")) {
                ViewHolder.mThumbnail.setImageResource(R.drawable.folder_back);
                ViewHolder.mImageShareIcon.setVisibility(8);
                ViewHolder.mImageRenameIcon.setVisibility(8);
                ViewHolder.mImageDeleteIcon.setVisibility(8);
            } else {
                ViewHolder.mThumbnail.setImageResource(R.drawable.folder);
                if (arg1 == 0 && data.szTitle.endsWith("分享区")) {
                    ViewHolder.mThumbnail.setImageResource(R.drawable.folder_user);
                    ViewHolder.mImageShareIcon.setVisibility(8);
                    ViewHolder.mImageRenameIcon.setVisibility(8);
                    ViewHolder.mImageDeleteIcon.setVisibility(8);
                }
                if (data.bLocked) {
                    ViewHolder.mImageShareIcon.setVisibility(8);
                    ViewHolder.mImageRenameIcon.setVisibility(8);
                    ViewHolder.mImageDeleteIcon.setVisibility(8);
                }
            }
        } else {
            ViewHolder.mFolderName.setVisibility(4);
            ViewHolder.mImageShareIcon.setVisibility(0);
            ViewHolder.mImageRenameIcon.setVisibility(8);
            ViewHolder.mImageDeleteIcon.setVisibility(0);
            ViewHolder.mTitle.setVisibility(0);
            ViewHolder.mDate.setVisibility(0);
            ViewHolder.mDescription.setVisibility(0);
            if (data.nType == 0) {
                ViewHolder.mTitle.setVisibility(8);
                ViewHolder.mDate.setVisibility(8);
                ViewHolder.mDescription.setText(data.szTitle);
                ViewHolder.mThumbnail.setImageResource(R.drawable.ic_question);
            } else {
                ViewHolder.mTitle.setText("资源");
                Picasso.with(this.mContext).load(MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/getresourcethumbnail?guid=" + data.szGUID).error(R.drawable.ic_placehold_small_gray).into(ViewHolder.mThumbnail);
                Bitmap bitmap = data.bmThumbnail;
                if (data.nType >= 1000 && data.nType <= 1999) {
                    ViewHolder.mTitle.setText("备课资源");
                }
                if (data.nType == 11) {
                    ViewHolder.mTitle.setText("视频资源");
                }
                if (data.nType == 21) {
                    ViewHolder.mTitle.setText("音频资源");
                }
                if (data.nType == 41) {
                    ViewHolder.mTitle.setText("图片资源");
                }
                if (data.nType >= 4000 && data.nType <= 4999) {
                    ViewHolder.mTitle.setText("课堂实录");
                }
                ViewHolder.mDescription.setText(data.szTitle);
            }
            if (data.szAuthor == null || data.szAuthor.isEmpty()) {
                ViewHolder.mDate.setText(data.szDateTime);
            } else {
                ViewHolder.mDate.setText(data.szAuthor + "，" + data.szDateTime);
            }
            ViewHolder.mImageShareIcon.setVisibility(0);
            ViewHolder.mImageRenameIcon.setVisibility(8);
        }
        if (this.mbReadOnly) {
            ViewHolder.mImageShareIcon.setVisibility(8);
            ViewHolder.mImageRenameIcon.setVisibility(8);
            ViewHolder.mImageDeleteIcon.setVisibility(8);
        }
        if (!this.mbCanShare) {
            ViewHolder.mImageShareIcon.setVisibility(8);
        }
        ViewHolder.mImageDeleteIcon.setTag(data);
        ViewHolder.mImageRenameIcon.setTag(data);
        ViewHolder.mImageShareIcon.setTag(data);
        ViewHolder.mCardView.setTag(data);
    }

    public boolean getReadOnly() {
        return this.mbReadOnly;
    }
}
