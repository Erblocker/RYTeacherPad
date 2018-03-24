package com.farproc.wifi.connecter;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import com.netspace.pad.library.R;

public class Floating extends Activity {
    private static final int[] BUTTONS = new int[]{R.id.button1, R.id.button2, R.id.button3};
    private Content mContent;
    private ViewGroup mContentViewContainer;
    private View mView;

    public interface Content {
        int getButtonCount();

        OnClickListener getButtonOnClickListener(int i);

        CharSequence getButtonText(int i);

        CharSequence getTitle();

        View getView();

        boolean onContextItemSelected(MenuItem menuItem);

        void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenuInfo contextMenuInfo);
    }

    public void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(1);
        super.onCreate(savedInstanceState);
        this.mView = View.inflate(this, R.layout.dialog_wifi, null);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        this.mView.setMinimumWidth(Math.min(dm.widthPixels, dm.heightPixels) - 20);
        setContentView(this.mView);
        this.mContentViewContainer = (ViewGroup) this.mView.findViewById(R.id.content);
    }

    private void setDialogContentView(View contentView) {
        this.mContentViewContainer.removeAllViews();
        this.mContentViewContainer.addView(contentView, new LayoutParams(-1, -2));
    }

    public void setContent(Content content) {
        this.mContent = content;
        refreshContent();
    }

    public void refreshContent() {
        setDialogContentView(this.mContent.getView());
        ((TextView) findViewById(R.id.title)).setText(this.mContent.getTitle());
        int btnCount = this.mContent.getButtonCount();
        if (btnCount > BUTTONS.length) {
            throw new RuntimeException(String.format("%d exceeds maximum button count: %d!", new Object[]{Integer.valueOf(btnCount), Integer.valueOf(BUTTONS.length)}));
        }
        int i;
        View findViewById = findViewById(R.id.buttons_view);
        if (btnCount > 0) {
            i = 0;
        } else {
            i = 8;
        }
        findViewById.setVisibility(i);
        for (int buttonId : BUTTONS) {
            Button btn = (Button) findViewById(buttonId);
            btn.setOnClickListener(null);
            btn.setVisibility(8);
        }
        for (int btnIndex = 0; btnIndex < btnCount; btnIndex++) {
            btn = (Button) findViewById(BUTTONS[btnIndex]);
            btn.setText(this.mContent.getButtonText(btnIndex));
            btn.setVisibility(0);
            btn.setOnClickListener(this.mContent.getButtonOnClickListener(btnIndex));
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if (this.mContent != null) {
            this.mContent.onCreateContextMenu(menu, v, menuInfo);
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        if (this.mContent != null) {
            return this.mContent.onContextItemSelected(item);
        }
        return false;
    }
}
