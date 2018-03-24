package com.netspace.teacherpad.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import java.util.ArrayList;

public class SimplePagesAdapter extends PagerAdapter implements OnPageChangeListener {
    private ViewPagerReadyInterface m_CallBack;
    private ViewPager m_ViewPager;
    private ArrayList<TextView> m_arrPageLabels;
    private ArrayList<String> m_arrPageTitles;
    private ArrayList<Integer> m_arrPages;

    public interface ViewPagerReadyInterface {
        void OnPageInstantiated(int i);
    }

    public SimplePagesAdapter(ArrayList<Integer> arrPages, ViewPagerReadyInterface CallBack) {
        this.m_arrPages = arrPages;
        this.m_CallBack = CallBack;
    }

    public SimplePagesAdapter(ArrayList<Integer> arrPages, ArrayList<TextView> arrPageLabels, ViewPager ViewPager, ViewPagerReadyInterface CallBack) {
        this.m_arrPages = arrPages;
        this.m_arrPageLabels = arrPageLabels;
        this.m_arrPageTitles = new ArrayList();
        for (int i = 0; i < this.m_arrPageLabels.size(); i++) {
            TextView OneLabel = (TextView) this.m_arrPageLabels.get(i);
            this.m_arrPageTitles.add(OneLabel.getText().toString());
            OneLabel.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    for (int i = 0; i < SimplePagesAdapter.this.m_arrPageLabels.size(); i++) {
                        if (v.getId() == ((TextView) SimplePagesAdapter.this.m_arrPageLabels.get(i)).getId()) {
                            SimplePagesAdapter.this.m_ViewPager.setCurrentItem(i);
                        }
                    }
                }
            });
        }
        this.m_ViewPager = ViewPager;
        this.m_CallBack = CallBack;
    }

    public int getCount() {
        return this.m_arrPages.size();
    }

    public Object instantiateItem(View collection, int position) {
        View view = ((LayoutInflater) collection.getContext().getSystemService("layout_inflater")).inflate(((Integer) this.m_arrPages.get(position)).intValue(), null);
        ((ViewPager) collection).addView(view, 0);
        if (this.m_CallBack != null) {
            this.m_CallBack.OnPageInstantiated(position);
        }
        return view;
    }

    public void destroyItem(View arg0, int arg1, Object arg2) {
    }

    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == ((View) arg1);
    }

    public void onPageScrollStateChanged(int arg0) {
    }

    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    public void onPageSelected(int arg0) {
        if (this.m_arrPageLabels != null) {
            for (int i = 0; i < this.m_arrPageLabels.size(); i++) {
                String szTitle = (String) this.m_arrPageTitles.get(i);
                TextView Label = (TextView) this.m_arrPageLabels.get(i);
                if (i == arg0) {
                    SpannableString spanString = new SpannableString(szTitle);
                    spanString.setSpan(new StyleSpan(1), 0, spanString.length(), 0);
                    Label.setText(spanString);
                    Label.setTextColor(-12936515);
                } else {
                    Label.setText(szTitle);
                    Label.setTextColor(-16777216);
                }
            }
        }
    }
}
