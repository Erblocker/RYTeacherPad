package com.netspace.teacherpad.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.netspace.library.controls.DrawView;
import com.netspace.teacherpad.R;

public class DrawViewFragment extends Fragment {
    private DrawView mDrawView;
    private View mRootView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_drawview, null);
        this.mDrawView = (DrawView) v.findViewById(R.id.drawPad);
        this.mRootView = v;
        this.mDrawView.setEnableCache(true);
        this.mDrawView.setBackgroundResource(R.drawable.background_drawpad);
        return v;
    }
}
