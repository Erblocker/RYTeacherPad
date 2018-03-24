package com.netspace.teacherpad.adapter;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.utilities.Utilities;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.TeacherPadApplication;
import com.netspace.teacherpad.dialog.StartClassControlUnit;
import com.netspace.teacherpad.structure.PlayPos;
import com.netspace.teacherpad.util.SimpleTooltip;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class ClassResourcesAdapter extends Adapter<ViewHolder> {
    private Context mContext;
    private OnClickListener mOnClickListener;
    private OnLongClickListener mOnLongClickListener;
    private SimpleTooltip mToolTip = new SimpleTooltip();
    private ArrayList<ResourceItemData> marrData;

    public class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        public ImageButton mButtonNext;
        public ImageButton mButtonPlay;
        public ImageButton mButtonPrev;
        public ImageButton mButtonSeek;
        public ImageButton mButtonStop;
        public CardView mCardView;
        public TextView mDescription;
        public ImageView mImageViewStates;
        public ImageView mImageViewThumbnail;
        public LinearLayout mLayoutMonitors;
        public TextView mPlayLength;
        public TextView mPlayPos;
        public RelativeLayout mRelativeLayout;
        public RelativeLayout mRelativeLayoutPlayPos;
        public SeekBar mSeekBar;
        public TextView mTitle;
        public View mViewCardLine;

        public ViewHolder(ClassResourcesAdapter adapter, View itemView, Context context, OnClickListener OnClickListener, OnLongClickListener OnLongClickListener) {
            super(itemView);
            this.mCardView = (CardView) itemView.findViewById(R.id.cardViewClassResourceItem);
            this.mCardView.setOnClickListener(OnClickListener);
            this.mRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayoutClassResourceItem);
            this.mRelativeLayoutPlayPos = (RelativeLayout) itemView.findViewById(R.id.relativeLayoutPlayPos);
            this.mPlayPos = (TextView) itemView.findViewById(R.id.textViewTimePass);
            this.mPlayLength = (TextView) itemView.findViewById(R.id.textViewTimeRemain);
            this.mSeekBar = (SeekBar) itemView.findViewById(R.id.seekBarPosition);
            this.mSeekBar.getProgressDrawable().setColorFilter(Utilities.getThemeCustomColor(R.attr.cardview_actionbar_color), Mode.SRC_IN);
            this.mSeekBar.getThumb().setColorFilter(Utilities.getThemeCustomColor(R.attr.cardview_actionbar_color), Mode.SRC_IN);
            this.mImageViewThumbnail = (ImageView) itemView.findViewById(R.id.imageViewThumbnail);
            this.mTitle = (TextView) itemView.findViewById(R.id.textTitle);
            this.mDescription = (TextView) itemView.findViewById(R.id.textDescription);
            this.mViewCardLine = itemView.findViewById(R.id.viewCardLine);
            this.mLayoutMonitors = (LinearLayout) itemView.findViewById(R.id.layoutMonitors);
            this.mButtonPlay = (ImageButton) itemView.findViewById(R.id.buttonPlay);
            this.mButtonStop = (ImageButton) itemView.findViewById(R.id.buttonStop);
            this.mButtonNext = (ImageButton) itemView.findViewById(R.id.buttonNext);
            this.mButtonPrev = (ImageButton) itemView.findViewById(R.id.buttonPrev);
            this.mButtonSeek = (ImageButton) itemView.findViewById(R.id.buttonSeek);
            ClassResourcesAdapter.this.mToolTip.regiserTooltip(this.mButtonPlay, "点击这里后台播放音乐");
            ClassResourcesAdapter.this.mToolTip.regiserTooltip(this.mButtonStop, "点击这里停止播放");
            ClassResourcesAdapter.this.mToolTip.regiserTooltip(this.mButtonNext, "下一页");
            ClassResourcesAdapter.this.mToolTip.regiserTooltip(this.mButtonPrev, "上一页");
            ClassResourcesAdapter.this.mToolTip.regiserTooltip(this.mButtonSeek, "快速跳转");
        }
    }

    public ClassResourcesAdapter(Context context, ArrayList<ResourceItemData> arrData) {
        this.mContext = context;
        this.marrData = arrData;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        this.mOnLongClickListener = onLongClickListener;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public int getItemCount() {
        return this.marrData.size();
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(this, LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listitem_classresourceitem, viewGroup, false), this.mContext, this.mOnClickListener, this.mOnLongClickListener);
    }

    public void onBindViewHolder(ViewHolder ViewHolder, int arg1) {
        String szDescription;
        final ResourceItemData resourceData = (ResourceItemData) this.marrData.get(arg1);
        boolean bShowLine = false;
        ViewHolder.mCardView.setTag(resourceData);
        if (resourceData.nType == 0) {
            szDescription = "试题";
        } else {
            szDescription = "资源";
        }
        if (resourceData.szTitle.length() > 50) {
            ViewHolder.mTitle.setText(resourceData.szTitle.substring(0, 50));
        } else {
            ViewHolder.mTitle.setText(resourceData.szTitle);
        }
        ViewHolder.mDescription.setText(szDescription);
        if (resourceData.bRead) {
            ViewHolder.mTitle.setTextColor(-7829368);
        } else {
            ViewHolder.mTitle.setTextColor(-16777216);
        }
        ViewHolder.mButtonPlay.setImageDrawable(new IconDrawable(this.mContext, FontAwesomeIcons.fa_play).color(Utilities.getThemeCustomColor(R.attr.cardview_actionbar_color)).actionBarSize());
        ViewHolder.mButtonStop.setImageDrawable(new IconDrawable(this.mContext, FontAwesomeIcons.fa_stop).color(Utilities.getThemeCustomColor(R.attr.cardview_actionbar_color)).actionBarSize());
        ViewHolder.mButtonNext.setImageDrawable(new IconDrawable(this.mContext, FontAwesomeIcons.fa_step_forward).color(Utilities.getThemeCustomColor(R.attr.cardview_actionbar_color)).actionBarSize());
        ViewHolder.mButtonPrev.setImageDrawable(new IconDrawable(this.mContext, FontAwesomeIcons.fa_step_backward).color(Utilities.getThemeCustomColor(R.attr.cardview_actionbar_color)).actionBarSize());
        ViewHolder.mButtonSeek.setImageDrawable(new IconDrawable(this.mContext, FontAwesomeIcons.fa_random).color(Utilities.getThemeCustomColor(R.attr.cardview_actionbar_color)).actionBarSize());
        ViewHolder.mButtonPlay.setTag(R.id.buttonScreen, resourceData);
        ViewHolder.mButtonStop.setTag(R.id.buttonScreen, resourceData);
        ViewHolder.mButtonNext.setTag(R.id.buttonScreen, resourceData);
        ViewHolder.mButtonPrev.setTag(R.id.buttonScreen, resourceData);
        ViewHolder.mButtonSeek.setTag(R.id.buttonScreen, resourceData);
        ViewHolder.mButtonPrev.setOnClickListener(this.mOnClickListener);
        ViewHolder.mButtonNext.setOnClickListener(this.mOnClickListener);
        ViewHolder.mButtonPlay.setOnClickListener(this.mOnClickListener);
        ViewHolder.mButtonStop.setOnClickListener(this.mOnClickListener);
        ViewHolder.mButtonSeek.setOnClickListener(this.mOnClickListener);
        if (resourceData.nType == 0) {
            ViewHolder.mImageViewThumbnail.setVisibility(8);
            ViewHolder.mRelativeLayoutPlayPos.setVisibility(8);
            Picasso.with(this.mContext).cancelRequest(ViewHolder.mImageViewThumbnail);
        } else {
            ViewHolder.mImageViewThumbnail.setVisibility(0);
            ViewHolder.mRelativeLayoutPlayPos.setVisibility(0);
            Picasso.with(this.mContext).load(MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/getresourcethumbnail?guid=" + resourceData.szGUID).error((int) R.drawable.ic_placehold_small_gray).into(ViewHolder.mImageViewThumbnail);
        }
        ViewHolder.mButtonSeek.setVisibility(8);
        ViewHolder.mLayoutMonitors.removeAllViews();
        PlayPos playPos = StartClassControlUnit.getResourcePlayPos(resourceData.szGUID);
        int nFlags = StartClassControlUnit.getResourcePlayFlags(resourceData.szGUID);
        if (playPos == null || (playPos != null && playPos.nPos == 0 && playPos.nLength == 0)) {
            ViewHolder.mRelativeLayoutPlayPos.setVisibility(8);
        } else {
            ViewHolder.mRelativeLayoutPlayPos.setVisibility(0);
            ViewHolder.mSeekBar.setMax(playPos.nLength);
            ViewHolder.mSeekBar.setProgress(playPos.nPos);
            if ((StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTPLAYSTOP & nFlags) == StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTPLAYSTOP) {
                ViewHolder.mPlayPos.setText(Utilities.msToString(playPos.nPos));
                ViewHolder.mPlayLength.setText(Utilities.msToString(playPos.nLength));
            } else {
                ViewHolder.mPlayPos.setText(String.valueOf(playPos.nPos));
                ViewHolder.mPlayLength.setText(String.valueOf(playPos.nLength));
            }
            ViewHolder.mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        StartClassControlUnit.processSeekToCommandWithScreen(resourceData.szGUID, progress);
                    }
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            bShowLine = true;
        }
        if (StartClassControlUnit.isResourceOnTop(resourceData.szGUID) && (StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTSEEK & nFlags) == StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTSEEK) {
            ViewHolder.mButtonNext.setVisibility(0);
            ViewHolder.mButtonPrev.setVisibility(0);
            if (resourceData.arrThumbnailUrls == null || resourceData.arrThumbnailUrls.size() <= 0) {
                ViewHolder.mButtonSeek.setVisibility(8);
            } else {
                ViewHolder.mButtonSeek.setVisibility(0);
            }
            bShowLine = true;
        } else {
            ViewHolder.mButtonNext.setVisibility(8);
            ViewHolder.mButtonPrev.setVisibility(8);
            ViewHolder.mButtonSeek.setVisibility(8);
        }
        if (StartClassControlUnit.isResourceOnTop(resourceData.szGUID) && (StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTPLAYSTOP & nFlags) == StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTPLAYSTOP) {
            ViewHolder.mButtonPlay.setVisibility(0);
            ViewHolder.mButtonStop.setVisibility(0);
            bShowLine = true;
        } else {
            ViewHolder.mButtonPlay.setVisibility(8);
            ViewHolder.mButtonStop.setVisibility(8);
        }
        if (TeacherPadApplication.marrMonitors.size() > 1) {
            for (int i = 0; i < TeacherPadApplication.marrMonitors.size(); i++) {
                Button textView = new Button(this.mContext);
                textView.setTag(R.id.buttonScreen, resourceData);
                textView.setId(R.id.buttonScreen);
                textView.setText(String.valueOf(i + 1));
                textView.setBackgroundResource(R.drawable.background_actionbutton_click_with_circle_border);
                textView.setTextColor(Utilities.getThemeCustomColor(R.attr.cardview_actionbar_color));
                textView.setTextSize(20.0f);
                textView.setOnClickListener(this.mOnClickListener);
                ViewHolder.mLayoutMonitors.addView(textView);
                if (StartClassControlUnit.isResourceInScreen(resourceData.szGUID, i)) {
                    textView.setSelected(true);
                } else {
                    textView.setSelected(false);
                }
                textView.getLayoutParams().width = Utilities.dpToPixel(42, this.mContext);
                textView.getLayoutParams().height = Utilities.dpToPixel(42, this.mContext);
                this.mToolTip.regiserTooltip(textView, "点击这里在屏幕" + String.valueOf(i + 1) + "上播放此资源");
                bShowLine = true;
            }
        }
        if (bShowLine) {
            ViewHolder.mViewCardLine.setVisibility(0);
        } else {
            ViewHolder.mViewCardLine.setVisibility(8);
        }
    }
}
