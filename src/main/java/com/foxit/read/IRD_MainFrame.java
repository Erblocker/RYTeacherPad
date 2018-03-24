package com.foxit.read;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.widget.RelativeLayout;
import com.foxit.uiextensions.controls.panel.PanelHost;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.controls.toolbar.BaseBar;
import com.foxit.view.propertybar.IML_MultiLineBar;
import com.foxit.view.propertybar.IMT_MoreTools;

public interface IRD_MainFrame {
    BaseBar getAnnotCustomBottomBar();

    BaseBar getAnnotCustomTopBar();

    Activity getAttachedActivity();

    BaseBar getBottomToolbar();

    RelativeLayout getContentView();

    Context getContext();

    BaseBar getEditBar();

    BaseBar getEditDoneBar();

    IMT_MoreTools getMoreToolsBar();

    PanelHost getPanel();

    PropertyBar getPropertyBar();

    IML_MultiLineBar getSettingBar();

    BaseBar getToolSetBar();

    BaseBar getTopToolbar();

    void hideMaskView();

    void hidePanel();

    void hideSettingBar();

    void hideToolbars();

    boolean isEditBarShowing();

    boolean isMaskViewShowing();

    boolean isToolbarsVisible();

    void onConfigurationChanged(Configuration configuration);

    void showMaskView();

    void showPanel();

    void showPanel(int i);

    void showToolbars();
}
