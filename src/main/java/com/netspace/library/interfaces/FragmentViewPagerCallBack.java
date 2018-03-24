package com.netspace.library.interfaces;

import android.support.v4.app.Fragment;

public interface FragmentViewPagerCallBack {
    void onDestroy();

    Fragment onNewInstance();
}
