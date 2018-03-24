package com.netspace.teacherpad.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.ui.UI;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.ScreenDisplayActivity;
import com.netspace.teacherpad.TeacherPadApplication;
import com.netspace.teacherpad.adapter.ResourcesAdapter;

public class ResourcesListDialog extends Dialog implements OnItemClickListener {
    private static int mSelectionPos = 0;
    private ListView mListView;
    private ResourcesAdapter mResourcesAdapter;

    public ResourcesListDialog(Context context) {
        super(context);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("资源列表");
        setContentView(R.layout.list);
        this.mListView = (ListView) findViewById(16908298);
        refreshDataSet();
        this.mListView.setSelection(mSelectionPos);
    }

    public void refreshDataSet() {
        this.mResourcesAdapter = new ResourcesAdapter(getContext(), TeacherPadApplication.arrResourceData);
        this.mListView.setAdapter(this.mResourcesAdapter);
        this.mListView.setOnItemClickListener(this);
    }

    public void notifyDataSetChanged() {
        this.mResourcesAdapter.notifyDataSetChanged();
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        mSelectionPos = position;
        ResourceItemData Item = (ResourceItemData) TeacherPadApplication.arrResourceData.get(position);
        if (TeacherPadApplication.szCurrentPlayingGUID == null || !Item.szGUID.equalsIgnoreCase(TeacherPadApplication.szCurrentPlayingGUID)) {
            TeacherPadApplication.szCorrectAnswer = "";
            TeacherPadApplication.szCurrentQuestionIMMessage = "";
            if (TeacherPadApplication.mapResourcePlayPos.containsKey(Item.szGUID)) {
                TeacherPadApplication.IMThread.SendMessage("SwitchToResource " + Item.szGUID + " " + String.valueOf(((Integer) TeacherPadApplication.mapResourcePlayPos.get(Item.szGUID)).intValue()));
            } else {
                TeacherPadApplication.IMThread.SendMessage("SwitchToResource " + Item.szGUID);
            }
            Item.bRead = true;
            this.mResourcesAdapter.notifyDataSetChanged();
            Activity currentActivity = UI.getCurrentActivity();
            if (currentActivity != null && (currentActivity instanceof ScreenDisplayActivity)) {
                ScreenDisplayActivity currentActivity2 = (ScreenDisplayActivity) currentActivity;
            }
            dismiss();
        }
        this.mListView.postDelayed(new Runnable() {
            public void run() {
                ResourcesListDialog.this.notifyDataSetChanged();
            }
        }, 1000);
    }
}
