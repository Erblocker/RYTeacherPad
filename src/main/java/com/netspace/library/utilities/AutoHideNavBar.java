package com.netspace.library.utilities;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.view.View;
import android.widget.LinearLayout;
import com.netspace.library.controls.LockableScrollView;
import com.netspace.library.controls.LockableScrollView.ScrollViewListener;

public class AutoHideNavBar {
    private static final boolean VERBOSE = false;
    private Runnable mCheckScrollRunnable = new Runnable() {
        public void run() {
            if (Math.abs(AutoHideNavBar.this.mOldScrollY - AutoHideNavBar.this.mScrollY) <= 5) {
                if (AutoHideNavBar.this.mScrollY < AutoHideNavBar.this.mLimitY) {
                    if (!AutoHideNavBar.this.mbHitBottom) {
                        AutoHideNavBar.this.mScrollView.removeCallbacks(AutoHideNavBar.this.mHideNavRunnable);
                        AutoHideNavBar.this.mScrollView.postDelayed(AutoHideNavBar.this.mShowNavRunnable, 100);
                    }
                } else if (AutoHideNavBar.this.mOldScrollY < AutoHideNavBar.this.mScrollY) {
                    AutoHideNavBar.this.mScrollView.removeCallbacks(AutoHideNavBar.this.mShowNavRunnable);
                    AutoHideNavBar.this.mScrollView.postDelayed(AutoHideNavBar.this.mHideNavRunnable, 100);
                } else if (AutoHideNavBar.this.mOldScrollY > AutoHideNavBar.this.mScrollY) {
                    AutoHideNavBar.this.mScrollView.removeCallbacks(AutoHideNavBar.this.mHideNavRunnable);
                    AutoHideNavBar.this.mScrollView.postDelayed(AutoHideNavBar.this.mShowNavRunnable, 100);
                }
                AutoHideNavBar.this.mbHitBottom = false;
            }
        }
    };
    private Runnable mHideNavRunnable = new Runnable() {
        public void run() {
            if (!AutoHideNavBar.this.mNavHide) {
                AutoHideNavBar.this.showHideNav(false);
            }
        }
    };
    private LinearLayout mLayoutNav;
    private int mLimitY = 1024;
    private boolean mNavHide = false;
    private int mOldScrollY = 0;
    private LockableScrollView mScrollView;
    private int mScrollY = 0;
    private Runnable mShowNavRunnable = new Runnable() {
        public void run() {
            if (AutoHideNavBar.this.mNavHide) {
                AutoHideNavBar.this.showHideNav(true);
            }
        }
    };
    private boolean mSkipScrollCheck = false;
    private boolean mbHitBottom = false;

    public void init(LockableScrollView ScrollView, LinearLayout LayoutNav) {
        this.mScrollView = ScrollView;
        this.mLayoutNav = LayoutNav;
        this.mScrollView.setScrollViewListener(new ScrollViewListener() {
            public void onScrollChanged(LockableScrollView scrollView, int x, int y, int oldx, int oldy) {
                if (!AutoHideNavBar.this.mSkipScrollCheck) {
                    AutoHideNavBar.this.mScrollY = y;
                    AutoHideNavBar.this.mOldScrollY = oldy;
                    AutoHideNavBar.this.mScrollView.removeCallbacks(AutoHideNavBar.this.mCheckScrollRunnable);
                    AutoHideNavBar.this.mScrollView.postDelayed(AutoHideNavBar.this.mCheckScrollRunnable, 300);
                }
            }
        });
    }

    public void showHideNav(boolean bShow) {
        View view = this.mLayoutNav;
        final int nLayoutHeight = this.mLayoutNav.getHeight();
        if (bShow) {
            this.mSkipScrollCheck = true;
            view.setVisibility(0);
            view.setAlpha(0.0f);
            this.mScrollView.scrollBy(0, nLayoutHeight);
            view.animate().translationY(0.0f).alpha(1.0f).setListener(new AnimatorListener() {
                public void onAnimationEnd(Animator animation) {
                    AutoHideNavBar.this.mLayoutNav.setVisibility(0);
                    AutoHideNavBar.this.mSkipScrollCheck = false;
                    AutoHideNavBar.this.mNavHide = false;
                }

                public void onAnimationCancel(Animator animation) {
                }

                public void onAnimationRepeat(Animator animation) {
                }

                public void onAnimationStart(Animator animation) {
                }
            });
        } else if (this.mScrollY >= this.mLimitY) {
            this.mSkipScrollCheck = true;
            view.setVisibility(0);
            view.setAlpha(1.0f);
            view.animate().translationY((float) (-view.getHeight())).alpha(0.0f).setListener(new AnimatorListener() {
                public void onAnimationEnd(Animator animation) {
                    AutoHideNavBar.this.mLayoutNav.setVisibility(8);
                    AutoHideNavBar.this.mScrollView.scrollBy(0, -nLayoutHeight);
                    AutoHideNavBar.this.mNavHide = true;
                    AutoHideNavBar.this.mSkipScrollCheck = false;
                }

                public void onAnimationCancel(Animator animation) {
                }

                public void onAnimationRepeat(Animator animation) {
                }

                public void onAnimationStart(Animator animation) {
                }
            });
        }
    }
}
