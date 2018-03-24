package com.netspace.teacherpad.util;

import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import com.netspace.teacherpad.TeacherPadApplication;

public class DragEventListener implements OnDragListener {
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            case 1:
                if (event.getClipDescription().hasMimeType("text/plain")) {
                    return true;
                }
                return false;
            case 2:
                return true;
            case 3:
                String szDragData = (String) event.getClipData().getItemAt(0).getText();
                TeacherPadApplication MyApp = (TeacherPadApplication) v.getContext().getApplicationContext();
                if (szDragData != null) {
                    TeacherPadApplication.IMThread.SendMessage(szDragData);
                }
                v.setBackgroundDrawable(null);
                return true;
            case 4:
                v.setBackgroundDrawable(null);
                event.getResult();
                return true;
            case 5:
                v.setBackgroundColor(-16711936);
                return true;
            case 6:
                v.setBackgroundDrawable(null);
                return true;
            default:
                Log.e("DragDrop Example", "Unknown action type received by OnDragListener.");
                return true;
        }
    }
}
