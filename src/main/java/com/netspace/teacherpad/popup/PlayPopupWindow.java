package com.netspace.teacherpad.popup;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.adapter.SimpleListAdapterWrapper;
import com.netspace.library.adapter.SimpleListAdapterWrapper.ListAdapterWrapperCallBack;
import com.netspace.library.controls.HorizontalListView;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.utilities.Utilities;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.TeacherPadApplication;
import com.netspace.teacherpad.dialog.StartClassControlUnit;
import com.netspace.teacherpad.structure.PlayPos;
import com.squareup.picasso.Picasso;
import org.apache.http.HttpStatus;

public class PlayPopupWindow extends BasicPopupWindow implements OnClickListener, ListAdapterWrapperCallBack, OnItemClickListener, OnDismissListener {
    private ImageButton mButtonNext;
    private ImageButton mButtonPlay;
    private ImageButton mButtonPrev;
    private ImageButton mButtonStop;
    private ImageButton mButtonZoomin;
    private ImageButton mButtonZoomout;
    private OnChangeCallBack mCallBack;
    private View mContentView;
    private Context mContext;
    private Handler mHandler;
    private LinearLayoutManager mLayoutManager;
    private HorizontalListView mListView;
    private SimpleListAdapterWrapper mPDFThumbnailsAdapter;
    private TextView mPlayLength;
    private TextView mPlayPos;
    private RelativeLayout mRelativeLayoutPlayPos;
    private ResourceItemData mResourceData;
    private SeekBar mSeekBar;
    private TextView mTextViewWidth;
    private Runnable mUpdateRunnable = new Runnable() {
        public void run() {
            PlayPos playPos = StartClassControlUnit.getResourcePlayPos(PlayPopupWindow.this.mResourceData.szGUID);
            int nFlags = StartClassControlUnit.getResourcePlayFlags(PlayPopupWindow.this.mResourceData.szGUID);
            if (playPos == null || (playPos != null && playPos.nPos == 0 && playPos.nLength == 0)) {
                PlayPopupWindow.this.mRelativeLayoutPlayPos.setVisibility(8);
            } else {
                PlayPopupWindow.this.mRelativeLayoutPlayPos.setVisibility(0);
                PlayPopupWindow.this.mSeekBar.setMax(playPos.nLength);
                PlayPopupWindow.this.mSeekBar.setProgress(playPos.nPos);
                if ((StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTPLAYSTOP & nFlags) == StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTPLAYSTOP) {
                    PlayPopupWindow.this.mPlayPos.setText(Utilities.msToString(playPos.nPos));
                    PlayPopupWindow.this.mPlayLength.setText(Utilities.msToString(playPos.nLength));
                } else {
                    PlayPopupWindow.this.mPlayPos.setText(String.valueOf(playPos.nPos));
                    PlayPopupWindow.this.mPlayLength.setText(String.valueOf(playPos.nLength));
                }
                if (PlayPopupWindow.this.mbFirstUpdate && PlayPopupWindow.this.mPDFThumbnailsAdapter != null) {
                    PlayPopupWindow.this.mbFirstUpdate = false;
                    if (PlayPopupWindow.this.mListView.getSelection() != playPos.nPos - 1) {
                        PlayPopupWindow.this.mListView.setSelection(playPos.nPos - 1);
                        PlayPopupWindow.this.mPDFThumbnailsAdapter.notifyDataSetChanged();
                        View oneChild = PlayPopupWindow.this.mListView.getChildAt(0);
                        if (oneChild != null) {
                            int nX = ((playPos.nPos - 1) * oneChild.getWidth()) - (oneChild.getWidth() / 2);
                            if (nX < 0) {
                                nX = 0;
                            }
                            PlayPopupWindow.this.mListView.scrollTo(nX);
                        } else {
                            PlayPopupWindow.this.mbFirstUpdate = true;
                        }
                    }
                }
            }
            PlayPopupWindow.this.mHandler.postDelayed(PlayPopupWindow.this.mUpdateRunnable, 1000);
        }
    };
    private boolean mbFirstUpdate = true;

    public interface OnChangeCallBack {
        void onDataChanged(int i, int i2);
    }

    public PlayPopupWindow(Context context, ResourceItemData resourceitem) {
        super(context);
        this.mContext = context;
        this.mResourceData = resourceitem;
        initView();
    }

    public void initView() {
        if (this.mResourceData != null) {
            super.initView();
            if (this.mResourceData.arrThumbnailUrls == null || this.mResourceData.arrThumbnailUrls.size() <= 0) {
                setWidth(Utilities.dpToPixel((int) HttpStatus.SC_BAD_REQUEST, this.mContext));
                setHeight(Utilities.dpToPixel(150, this.mContext));
            } else {
                setWidth(Utilities.dpToPixel(600, this.mContext));
                setHeight(Utilities.dpToPixel(450, this.mContext));
            }
            this.mContentView = this.mLayoutInflater.inflate(R.layout.popup_play, null);
            this.mRelativeLayoutPlayPos = (RelativeLayout) this.mContentView.findViewById(R.id.relativeLayoutPlayPos);
            this.mListView = (HorizontalListView) this.mContentView.findViewById(R.id.studentAnswerView);
            this.mButtonPlay = (ImageButton) this.mContentView.findViewById(R.id.buttonPlay);
            this.mButtonStop = (ImageButton) this.mContentView.findViewById(R.id.buttonStop);
            this.mButtonNext = (ImageButton) this.mContentView.findViewById(R.id.buttonNext);
            this.mButtonPrev = (ImageButton) this.mContentView.findViewById(R.id.buttonPrev);
            this.mButtonZoomout = (ImageButton) this.mContentView.findViewById(R.id.buttonZoomout);
            this.mButtonZoomin = (ImageButton) this.mContentView.findViewById(R.id.buttonZoomin);
            this.mButtonPlay.setOnClickListener(this);
            this.mButtonStop.setOnClickListener(this);
            this.mButtonNext.setOnClickListener(this);
            this.mButtonPrev.setOnClickListener(this);
            this.mButtonZoomout.setOnClickListener(this);
            this.mButtonZoomin.setOnClickListener(this);
            this.mPlayPos = (TextView) this.mContentView.findViewById(R.id.textViewTimePass);
            this.mPlayLength = (TextView) this.mContentView.findViewById(R.id.textViewTimeRemain);
            this.mButtonPlay.setImageDrawable(new IconDrawable(this.mContext, FontAwesomeIcons.fa_play).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
            this.mButtonStop.setImageDrawable(new IconDrawable(this.mContext, FontAwesomeIcons.fa_stop).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
            this.mButtonNext.setImageDrawable(new IconDrawable(this.mContext, FontAwesomeIcons.fa_step_forward).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
            this.mButtonPrev.setImageDrawable(new IconDrawable(this.mContext, FontAwesomeIcons.fa_step_backward).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
            this.mButtonZoomout.setImageDrawable(new IconDrawable(this.mContext, FontAwesomeIcons.fa_search_minus).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
            this.mButtonZoomin.setImageDrawable(new IconDrawable(this.mContext, FontAwesomeIcons.fa_search_plus).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
            this.mTextViewWidth = (TextView) this.mContentView.findViewById(R.id.textViewWidth);
            this.mSeekBar = (SeekBar) this.mContentView.findViewById(R.id.seekBarPosition);
            this.mSeekBar.getProgressDrawable().setColorFilter(Utilities.getThemeCustomColor(R.attr.float_button_border_color), Mode.SRC_IN);
            this.mSeekBar.getThumb().setColorFilter(Utilities.getThemeCustomColor(R.attr.float_button_border_color), Mode.SRC_IN);
            this.mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        StartClassControlUnit.processSeekToCommandWithScreen(PlayPopupWindow.this.mResourceData.szGUID, progress + 1);
                        if (PlayPopupWindow.this.mPDFThumbnailsAdapter != null) {
                            PlayPopupWindow.this.mListView.setSelection(progress);
                        }
                    }
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            int nFlags = StartClassControlUnit.getResourcePlayFlags(this.mResourceData.szGUID);
            PlayPos playPos = StartClassControlUnit.getResourcePlayPos(this.mResourceData.szGUID);
            if (StartClassControlUnit.isResourceOnTop(this.mResourceData.szGUID) && (StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTSEEK & nFlags) == StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTSEEK) {
                this.mButtonNext.setVisibility(0);
                this.mButtonPrev.setVisibility(0);
                if (this.mResourceData.arrThumbnailUrls == null || this.mResourceData.arrThumbnailUrls.size() <= 0) {
                    this.mListView.setVisibility(8);
                } else {
                    this.mPDFThumbnailsAdapter = new SimpleListAdapterWrapper(this.mContext, this, R.layout.listitem_sidebar_seekthumbnail);
                    this.mListView.setAdapter(this.mPDFThumbnailsAdapter);
                    this.mListView.setVisibility(0);
                    this.mListView.setOnItemClickListener(this);
                }
            } else {
                this.mButtonNext.setVisibility(8);
                this.mButtonPrev.setVisibility(8);
                this.mListView.setVisibility(8);
            }
            if (StartClassControlUnit.isResourceOnTop(this.mResourceData.szGUID) && (StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTPLAYSTOP & nFlags) == StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTPLAYSTOP) {
                this.mButtonPlay.setVisibility(0);
                this.mButtonStop.setVisibility(0);
            } else {
                this.mButtonPlay.setVisibility(8);
                this.mButtonStop.setVisibility(8);
            }
            if (StartClassControlUnit.isResourceOnTop(this.mResourceData.szGUID) && (StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTZOOM & nFlags) == StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTZOOM) {
                this.mButtonZoomin.setVisibility(0);
                this.mButtonZoomout.setVisibility(0);
            } else {
                this.mButtonZoomin.setVisibility(8);
                this.mButtonZoomout.setVisibility(8);
            }
            this.mHandler = new Handler();
            this.mHandler.post(this.mUpdateRunnable);
            this.mContentLayout.addView(this.mContentView, -1, -1);
            setOnDismissListener(this);
        }
    }

    public void setCallBack(OnChangeCallBack CallBack) {
        this.mCallBack = CallBack;
    }

    public void onClick(View v) {
        for (int i = 0; i < TeacherPadApplication.marrMonitors.size(); i++) {
            if (StartClassControlUnit.isResourceInScreen(this.mResourceData.szGUID, i)) {
                StartClassControlUnit.processCommandWithScreen(v, i);
            }
        }
    }

    public int getCount() {
        return this.mResourceData.arrThumbnailUrls.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public void getView(int position, View convertView) {
        TextView textView = (TextView) convertView.findViewById(R.id.textViewIndex);
        Picasso.with(this.mContext).load((String) this.mResourceData.arrThumbnailUrls.get(position)).error((int) R.drawable.ic_placehold_small_gray).into((ImageView) convertView.findViewById(R.id.imageThumbnail));
        textView.setText("第" + String.valueOf(position + 1) + "页");
        if (position != this.mListView.getSelection()) {
            convertView.setBackgroundColor(-1);
        } else {
            convertView.setBackgroundColor(-16750849);
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        this.mListView.setSelection(position);
        StartClassControlUnit.processSeekToCommandWithScreen(this.mResourceData.szGUID, position + 1);
        view.setBackgroundColor(-16750849);
        this.mPDFThumbnailsAdapter.notifyDataSetChanged();
    }

    public void onDismiss() {
        this.mHandler.removeCallbacks(this.mUpdateRunnable);
    }
}
