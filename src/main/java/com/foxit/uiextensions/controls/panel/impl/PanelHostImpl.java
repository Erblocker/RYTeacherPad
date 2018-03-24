package com.foxit.uiextensions.controls.panel.impl;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.controls.panel.PanelContentViewAdapter;
import com.foxit.uiextensions.controls.panel.PanelHost;
import com.foxit.uiextensions.controls.panel.PanelSpec;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppResource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PanelHostImpl implements PanelHost {
    private LinearLayout mContentLayout = ((LinearLayout) this.mRootView.findViewById(R.id.panel_content_layout));
    private ViewPager mContentViewPager = ((ViewPager) this.mRootView.findViewById(R.id.panel_content_viewpager));
    private Context mContext;
    private PanelSpec mCurSpec;
    private AppDisplay mDisplay = new AppDisplay(this.mContext);
    private View mRootView = View.inflate(this.mContext, R.layout.root_panel, null);
    private ArrayList<PanelSpec> mSpecs = new ArrayList();
    private LinearLayout mTabLayout = ((LinearLayout) this.mRootView.findViewById(R.id.panel_tabbar_layout));
    private LinearLayout mTopLayout = ((LinearLayout) this.mRootView.findViewById(R.id.panel_topbar_layout));
    private PanelContentViewAdapter mViewPagerAdapter = new PanelContentViewAdapter(this.mViewPagerList);
    private List<View> mViewPagerList = new ArrayList();

    public PanelHostImpl(Context context) {
        this.mContext = context;
        this.mContentViewPager.setAdapter(this.mViewPagerAdapter);
        this.mContentViewPager.setOnPageChangeListener(new OnPageChangeListener() {
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                if (PanelHostImpl.this.mCurSpec != null && PanelHostImpl.this.mCurSpec.getTag() != ((PanelSpec) PanelHostImpl.this.mSpecs.get(position)).getTag()) {
                    PanelHostImpl.this.setCurrentSpec(((PanelSpec) PanelHostImpl.this.mSpecs.get(position)).getTag());
                }
            }

            public void onPageScrollStateChanged(int state) {
            }
        });
        this.mTopLayout.setBackgroundResource(R.color.ux_text_color_subhead_colour);
        if (this.mDisplay.isPad()) {
            LayoutParams params = (LayoutParams) this.mTopLayout.getLayoutParams();
            params.height = this.mDisplay.dp2px(64.0f);
            this.mTopLayout.setLayoutParams(params);
        }
        this.mTabLayout.setBackgroundResource(R.color.ux_text_color_subhead_colour);
        if (this.mDisplay.isPad()) {
            this.mTopLayout.getLayoutParams().height = AppResource.getDimensionPixelSize(this.mContext, R.dimen.ux_toolbar_height_pad);
        }
    }

    public View getContentView() {
        return this.mRootView;
    }

    public PanelSpec getSpec(int tag) {
        Iterator it = this.mSpecs.iterator();
        while (it.hasNext()) {
            PanelSpec spec = (PanelSpec) it.next();
            if (spec.getTag() == tag) {
                return spec;
            }
        }
        return null;
    }

    public void addSpec(PanelSpec spec) {
        if (getSpec(spec.getTag()) == null) {
            int index = -1;
            for (int i = 0; i < this.mSpecs.size(); i++) {
                if (((PanelSpec) this.mSpecs.get(i)).getTag() > spec.getTag()) {
                    index = i;
                    break;
                }
            }
            if (index == -1) {
                this.mSpecs.add(spec);
                this.mViewPagerList.add(spec.getContentView());
            } else {
                this.mSpecs.add(index, spec);
                this.mViewPagerList.add(index, spec.getContentView());
            }
            this.mViewPagerAdapter.notifyDataSetChanged();
            addTab(spec);
            if (this.mCurSpec == null) {
                setFocuses(0);
            }
        }
    }

    public void removeSpec(PanelSpec spec) {
        int index = this.mSpecs.indexOf(spec);
        if (index >= 0) {
            this.mSpecs.remove(index);
            this.mViewPagerList.remove(index);
            this.mViewPagerAdapter.notifyDataSetChanged();
            removeTab(spec);
            if (this.mSpecs.size() > index) {
                setFocuses(index);
            } else {
                setFocuses(this.mSpecs.size() - 1);
            }
        }
    }

    public void setCurrentSpec(int tag) {
        if (this.mCurSpec != null) {
            if (this.mCurSpec.getTag() == tag) {
                this.mCurSpec.onActivated();
                return;
            }
            this.mCurSpec.onDeactivated();
        }
        for (int i = 0; i < this.mSpecs.size(); i++) {
            if (((PanelSpec) this.mSpecs.get(i)).getTag() == tag) {
                setFocuses(i);
                ((PanelSpec) this.mSpecs.get(i)).onActivated();
            }
        }
    }

    public PanelSpec getCurrentSpec() {
        return this.mCurSpec;
    }

    private void addTab(final PanelSpec spec) {
        ImageView iconView = new ImageView(this.mContext);
        iconView.setId(R.id.rd_panel_tab_item);
        iconView.setImageResource(spec.getIcon());
        LayoutParams iconLayoutParams = new LayoutParams(-2, -2);
        iconLayoutParams.addRule(13, -1);
        iconLayoutParams.addRule(13, -1);
        ImageView focusView = new ImageView(this.mContext);
        focusView.setBackgroundColor(-1);
        focusView.setImageResource(R.drawable.toolbar_shadow_top);
        focusView.setVisibility(4);
        LayoutParams focusLayoutParams = new LayoutParams(-1, this.mDisplay.dp2px(4.0f));
        focusLayoutParams.addRule(12, -1);
        focusLayoutParams.setMargins(0, this.mDisplay.dp2px((float) 0), 0, 0);
        RelativeLayout tabItemView = new RelativeLayout(this.mContext);
        tabItemView.addView(iconView, iconLayoutParams);
        tabItemView.addView(focusView, focusLayoutParams);
        tabItemView.setTag(Integer.valueOf(spec.getTag()));
        tabItemView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                PanelHostImpl.this.setCurrentSpec(spec.getTag());
            }
        });
        this.mTabLayout.addView(tabItemView, new LinearLayout.LayoutParams(-1, -2, 1.0f));
    }

    void removeTab(PanelSpec spec) {
        int count = this.mTabLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            if (((Integer) this.mTabLayout.getChildAt(i).getTag()).intValue() == spec.getTag()) {
                this.mTabLayout.removeViewAt(i);
                return;
            }
        }
    }

    private void setFocuses(int index) {
        if (index < 0 || index > this.mSpecs.size() - 1) {
            index = 0;
        }
        if (this.mSpecs.size() != 0) {
            this.mCurSpec = (PanelSpec) this.mSpecs.get(index);
            this.mTopLayout.removeAllViews();
            this.mTopLayout.addView(this.mCurSpec.getTopToolbar());
            this.mContentViewPager.setCurrentItem(index);
            int iconCount = this.mSpecs.size();
            for (int i = 0; i < iconCount; i++) {
                RelativeLayout iconBox = (RelativeLayout) this.mTabLayout.getChildAt(i);
                if (i == index) {
                    ((ImageView) iconBox.getChildAt(0)).setImageState(new int[]{16842919}, true);
                    iconBox.getChildAt(1).setVisibility(0);
                } else {
                    ((ImageView) iconBox.getChildAt(0)).setImageState(new int[0], true);
                    iconBox.getChildAt(1).setVisibility(4);
                }
            }
        }
    }
}
