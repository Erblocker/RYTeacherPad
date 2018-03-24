package me.maxwin.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.netspace.pad.library.R;

public class XListViewFooter extends LinearLayout {
    public static final int STATE_LOADING = 2;
    public static final int STATE_NORMAL = 0;
    public static final int STATE_READY = 1;
    private View mContentView;
    private Context mContext;
    private TextView mHintView;
    private View mProgressBar;

    public XListViewFooter(Context context) {
        super(context);
        initView(context);
    }

    public XListViewFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public void setState(int state) {
        this.mHintView.setVisibility(4);
        this.mProgressBar.setVisibility(4);
        this.mHintView.setVisibility(4);
        if (state == 1) {
            this.mHintView.setVisibility(0);
            this.mHintView.setText(R.string.xlistview_footer_hint_ready);
        } else if (state == 2) {
            this.mProgressBar.setVisibility(0);
        } else {
            this.mHintView.setVisibility(0);
            this.mHintView.setText(R.string.xlistview_footer_hint_normal);
        }
    }

    public void setBottomMargin(int height) {
        if (height >= 0) {
            LayoutParams lp = (LayoutParams) this.mContentView.getLayoutParams();
            lp.bottomMargin = height;
            this.mContentView.setLayoutParams(lp);
        }
    }

    public int getBottomMargin() {
        return ((LayoutParams) this.mContentView.getLayoutParams()).bottomMargin;
    }

    public void normal() {
        this.mHintView.setVisibility(0);
        this.mProgressBar.setVisibility(8);
    }

    public void loading() {
        this.mHintView.setVisibility(8);
        this.mProgressBar.setVisibility(0);
    }

    public void hide() {
        LayoutParams lp = (LayoutParams) this.mContentView.getLayoutParams();
        lp.height = 0;
        this.mContentView.setLayoutParams(lp);
    }

    public void show() {
        LayoutParams lp = (LayoutParams) this.mContentView.getLayoutParams();
        lp.height = -2;
        this.mContentView.setLayoutParams(lp);
    }

    private void initView(Context context) {
        this.mContext = context;
        LinearLayout moreView = (LinearLayout) LayoutInflater.from(this.mContext).inflate(R.layout.xlistview_footer, null);
        addView(moreView);
        moreView.setLayoutParams(new LayoutParams(-1, -2));
        this.mContentView = moreView.findViewById(R.id.xlistview_footer_content);
        this.mProgressBar = moreView.findViewById(R.id.xlistview_footer_progressbar);
        this.mHintView = (TextView) moreView.findViewById(R.id.xlistview_footer_hint_textview);
    }
}
