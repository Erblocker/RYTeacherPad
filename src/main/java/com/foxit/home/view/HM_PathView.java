package com.foxit.home.view;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import com.foxit.app.App;
import com.foxit.home.R;
import com.foxit.uiextensions.utils.AppStorageManager;
import java.io.File;

public class HM_PathView implements IPathCtl {
    private Context mContext;
    private PathItem mDestItem;
    private String mDestText;
    private boolean mIsRoot;
    private PathItem mParentItem;
    private String mParentPath;
    private String mParentText;
    private String mPath;
    private pathChangedListener mPathChangedListener;
    private LinearLayout mRootLayout;

    public interface pathChangedListener {
        void onPathChanged(String str);
    }

    public HM_PathView(Context context) {
        this.mContext = context;
        this.mRootLayout = new LinearLayout(context);
        this.mParentItem = new PathItem(context, "pathctl_back", R.drawable.pathctl_back);
        this.mDestItem = new PathItem(context, "pathctl_dest", R.drawable.pathctl_dest);
        this.mParentItem.setTextColorResource(R.color.hm_pathclt_parent_selector);
        this.mRootLayout.setOrientation(0);
        this.mRootLayout.addView(this.mParentItem.getContentView());
        this.mRootLayout.addView(this.mDestItem.getContentView());
        if (App.instance().getDisplay().isPad()) {
            this.mRootLayout.setPadding(App.instance().getDisplay().dp2px(24.0f), 0, 0, 0);
        } else {
            this.mRootLayout.setPadding(App.instance().getDisplay().dp2px(16.0f), 0, App.instance().getDisplay().dp2px(6.0f), 0);
        }
    }

    public void setPath(String path) {
        if (path == null) {
            this.mRootLayout.setVisibility(4);
            return;
        }
        this.mRootLayout.setVisibility(0);
        this.mPath = path;
        if (checkRoot(path)) {
            this.mDestText = "";
            this.mParentPath = path;
            getRootText(path);
        } else {
            analysisPath(path);
        }
        if (this.mDestText != null) {
            this.mDestItem.setText(this.mDestText);
        }
        if (this.mParentText != null) {
            this.mParentItem.setText(this.mParentText);
        }
        if (this.mDestItem.getText() == null || "".equals(this.mDestItem.getText())) {
            this.mDestItem.getContentView().setVisibility(4);
        } else {
            this.mDestItem.getContentView().setVisibility(0);
        }
        if (this.mParentItem.getText() == null || "".equals(this.mParentItem.getText())) {
            this.mParentItem.getContentView().setVisibility(4);
        } else {
            this.mParentItem.getContentView().setVisibility(0);
        }
        resetTextMaxWidth();
    }

    public void setPathChangedListener(pathChangedListener listener) {
        if (listener != null && this.mParentItem != null) {
            this.mPathChangedListener = listener;
            this.mParentItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (HM_PathView.this.mIsRoot) {
                        HM_PathView.this.mPathChangedListener.onPathChanged(null);
                        HM_PathView.this.mRootLayout.setVisibility(4);
                        return;
                    }
                    HM_PathView.this.mPathChangedListener.onPathChanged(HM_PathView.this.mParentPath);
                }
            });
        }
    }

    public View getContentView() {
        return this.mRootLayout;
    }

    public String getCurPath() {
        return this.mPath;
    }

    private boolean checkRoot(String Path) {
        if (AppStorageManager.getInstance(this.mContext).getVolumePaths().contains(Path)) {
            this.mIsRoot = true;
            return true;
        }
        this.mIsRoot = false;
        return false;
    }

    private void analysisPath(String path) {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        String[] hierarchies = path.split(File.separator);
        if (hierarchies != null && hierarchies.length > 2) {
            this.mParentText = hierarchies[hierarchies.length - 2];
            this.mDestText = hierarchies[hierarchies.length - 1];
        } else if (hierarchies == null || hierarchies.length != 2) {
            this.mParentText = "";
            this.mDestText = "";
        } else {
            this.mParentText = hierarchies[hierarchies.length - 1];
            this.mDestText = "";
        }
        this.mParentPath = path.substring(0, path.lastIndexOf(File.separator));
    }

    private void getRootText(String path) {
        String[] hierarchies = path.split(File.separator);
        if (hierarchies.length > 0) {
            this.mParentText = hierarchies[hierarchies.length - 1];
        }
    }

    private void resetTextMaxWidth() {
        if (App.instance().getDisplay().isPad()) {
            this.mParentItem.getTextView().setMaxWidth(App.instance().getDisplay().getRawScreenWidth() / 6);
        } else {
            this.mParentItem.getTextView().setMaxWidth(App.instance().getDisplay().getRawScreenWidth() / 3);
        }
    }
}
