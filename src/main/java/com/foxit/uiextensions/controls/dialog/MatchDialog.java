package com.foxit.uiextensions.controls.dialog;

import android.view.View;
import com.foxit.uiextensions.controls.toolbar.BaseBar.TB_Position;

public interface MatchDialog {
    public static final long DIALOG_CANCEL = 1;
    public static final long DIALOG_COPY = 32;
    public static final long DIALOG_MOVE = 64;
    public static final long DIALOG_NO_BUTTON = 0;
    public static final long DIALOG_OK = 4;
    public static final long DIALOG_OPEN_ONLY = 8;
    public static final long DIALOG_REPLACE = 16;
    public static final long DIALOG_SKIP = 2;
    public static final long DIALOG_UPLOAD = 128;
    public static final int DLG_TITLE_STYLE_BG_BLUE = 1;
    public static final int DLG_TITLE_STYLE_BG_WHITE = 2;

    public interface DialogListener {
        void onBackClick();

        void onResult(long j);
    }

    public interface DismissListener {
        void onDismiss();
    }

    void dismiss();

    View getRootView();

    boolean isShowing();

    void setBackButtonVisible(int i);

    void setButton(long j);

    void setButtonEnable(boolean z, long j);

    void setContentView(View view);

    void setFullScreenWithStatusBar();

    void setHeight(int i);

    void setListener(DialogListener dialogListener);

    void setOnDLDismissListener(DismissListener dismissListener);

    void setStyle(int i);

    void setTitle(String str);

    void setTitleBlueLineVisible(boolean z);

    void setTitlePosition(TB_Position tB_Position);

    void setWidth(int i);

    void showDialog();

    void showDialog(boolean z);

    void showDialogNoManage();
}
