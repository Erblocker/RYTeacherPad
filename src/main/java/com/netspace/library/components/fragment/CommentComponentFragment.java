package com.netspace.library.components.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.components.CommentComponent;
import com.netspace.library.interfaces.IComponents;
import com.netspace.library.interfaces.IComponents.ComponentCallBack;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;

public class CommentComponentFragment extends Fragment implements ComponentCallBack {
    private CommentComponent mCommentComponent;
    private String mData;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.mCommentComponent != null) {
            return this.mCommentComponent;
        }
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.mData = bundle.getString(CommentComponent.RESOURCEGUID, this.mData);
        }
        this.mCommentComponent = new CommentComponent(getActivity());
        this.mCommentComponent.setCallBack(this);
        this.mCommentComponent.setData(this.mData);
        setHasOptionsMenu(true);
        return this.mCommentComponent;
    }

    public void OnDataLoaded(String szFileName, IComponents Component) {
    }

    public void OnDataUploaded(String szData, IComponents Component) {
    }

    public void OnRequestIntent(Intent intent, IComponents Component) {
    }

    private void setData(String szData) {
        this.mData = szData;
    }

    public String getData() {
        return null;
    }

    public void setCallBack(ComponentCallBack ComponentCallBack) {
        this.mCommentComponent.setCallBack(ComponentCallBack);
    }

    public void intentComplete(Intent intent) {
        this.mCommentComponent.intentComplete(intent);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        boolean z = false;
        if (item.getItemId() == R.id.action_openclose) {
            this.mCommentComponent.setDiscussOpen(!this.mCommentComponent.getDiscussOpen());
            if (this.mCommentComponent.getDiscussOpen()) {
                item.setIcon(new IconDrawable(getActivity(), FontAwesomeIcons.fa_toggle_on).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
            } else {
                item.setIcon(new IconDrawable(getActivity(), FontAwesomeIcons.fa_toggle_off).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
            }
        }
        if (item.getItemId() == R.id.action_lockunlock) {
            CommentComponent commentComponent = this.mCommentComponent;
            if (!this.mCommentComponent.getDiscussLock()) {
                z = true;
            }
            commentComponent.setDiscussLock(z);
            if (this.mCommentComponent.getDiscussLock()) {
                item.setIcon(new IconDrawable(getActivity(), FontAwesomeIcons.fa_lock).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
            } else {
                item.setIcon(new IconDrawable(getActivity(), FontAwesomeIcons.fa_unlock).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (MyiBaseApplication.getCommonVariables().UserInfo.nUserType != 0) {
            inflater.inflate(R.menu.menu_fragment_comment, menu);
            if (this.mCommentComponent.getDiscussOpen()) {
                menu.findItem(R.id.action_openclose).setIcon(new IconDrawable(getActivity(), FontAwesomeIcons.fa_toggle_on).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
            } else {
                menu.findItem(R.id.action_openclose).setIcon(new IconDrawable(getActivity(), FontAwesomeIcons.fa_toggle_off).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
            }
            if (this.mCommentComponent.getDiscussLock()) {
                menu.findItem(R.id.action_lockunlock).setIcon(new IconDrawable(getActivity(), FontAwesomeIcons.fa_lock).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
            } else {
                menu.findItem(R.id.action_lockunlock).setIcon(new IconDrawable(getActivity(), FontAwesomeIcons.fa_unlock).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }
}
