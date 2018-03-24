package com.netspace.teacherpad;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.OnTabSelectedListener;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.utilities.Utilities;
import com.netspace.teacherpad.controls.CustomViewPager;
import com.netspace.teacherpad.fragments.DrawViewFragment;
import java.util.ArrayList;

public class StudentAnswerViewActivity extends AppCompatActivity implements OnTabSelectedListener {
    private StudentAnswerPagesAdapter mAdapter;
    private LinearLayout mLayoutNav;
    private boolean mNavHide = false;
    private ArrayList<Fragment> mPages = new ArrayList();
    private CustomViewPager mViewPager;

    public class StudentAnswerPagesAdapter extends FragmentPagerAdapter {
        public StudentAnswerPagesAdapter(FragmentManager fm) {
            super(fm);
        }

        public Fragment getItem(int position) {
            return (Fragment) StudentAnswerViewActivity.this.mPages.get(position);
        }

        public int getCount() {
            return StudentAnswerViewActivity.this.mPages.size();
        }

        public CharSequence getPageTitle(int position) {
            if (getItem(position) instanceof DrawViewFragment) {
                return "绘画板";
            }
            return null;
        }
    }

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView((int) R.layout.activity_studentanswerview);
        setTitle("批改学生作业");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                StudentAnswerViewActivity.this.finish();
            }
        });
        this.mLayoutNav = (LinearLayout) findViewById(R.id.layoutNav);
        this.mViewPager = (CustomViewPager) findViewById(R.id.pager);
        this.mAdapter = new StudentAnswerPagesAdapter(getSupportFragmentManager());
        this.mViewPager.setAdapter(this.mAdapter);
        this.mViewPager.setOffscreenPageLimit(3);
        for (int i = 0; i < 10; i++) {
            this.mPages.add(new DrawViewFragment());
        }
        this.mAdapter.notifyDataSetChanged();
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(this.mViewPager);
        tabLayout.setOnTabSelectedListener(this);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_studentanswerview, menu);
        menu.findItem(R.id.action_correct).setIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_pencil_square_o).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        menu.findItem(R.id.action_favorite).setIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_star_o).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        menu.findItem(R.id.action_share).setIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_share_alt).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        menu.findItem(R.id.action_tag).setIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_tag).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        menu.findItem(R.id.action_contact).setIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_weixin).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        return true;
    }

    public void showHideNav(boolean bShow) {
        View view = this.mLayoutNav;
        if (bShow) {
            view.setVisibility(0);
            view.setAlpha(0.0f);
            view.animate().translationY(0.0f).alpha(1.0f).setListener(new AnimatorListener() {
                public void onAnimationEnd(Animator animation) {
                    StudentAnswerViewActivity.this.mLayoutNav.setVisibility(0);
                }

                public void onAnimationCancel(Animator animation) {
                }

                public void onAnimationRepeat(Animator animation) {
                }

                public void onAnimationStart(Animator animation) {
                }
            });
            return;
        }
        view.setVisibility(0);
        view.setAlpha(1.0f);
        view.animate().translationY((float) (-view.getHeight())).alpha(0.0f).setListener(new AnimatorListener() {
            public void onAnimationEnd(Animator animation) {
                StudentAnswerViewActivity.this.mLayoutNav.setVisibility(8);
            }

            public void onAnimationCancel(Animator animation) {
            }

            public void onAnimationRepeat(Animator animation) {
            }

            public void onAnimationStart(Animator animation) {
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == 82) {
            showHideNav(this.mNavHide);
            this.mNavHide = !this.mNavHide;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 82) {
            return super.onKeyDown(keyCode, event);
        }
        event.startTracking();
        return true;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode != 82) {
            return super.onKeyUp(keyCode, event);
        }
        event.startTracking();
        return true;
    }

    public void onTabReselected(Tab arg0) {
    }

    public void onTabSelected(Tab arg0) {
        this.mViewPager.setCurrentItem(arg0.getPosition());
    }

    public void onTabUnselected(Tab arg0) {
    }
}
