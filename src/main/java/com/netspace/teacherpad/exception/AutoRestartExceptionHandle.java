package com.netspace.teacherpad.exception;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.im.IMService;
import com.netspace.library.service.CountDownService;
import com.netspace.library.service.ScreenRecorderService;
import com.netspace.library.service.ScreenRecorderService2;
import com.netspace.library.service.ScreenRecorderService3;
import com.netspace.library.window.AnswerSheetWindow;
import com.netspace.library.window.CameraWindow;
import com.netspace.library.window.ChatWindow;
import com.netspace.library.window.DrawWindow;
import com.netspace.library.window.TextWindow;
import com.netspace.library.window.VideoChatWindow;
import com.netspace.library.window.VideoWindow;
import com.netspace.library.window.VoiceWindow;
import com.netspace.teacherpad.MainActivity;
import com.netspace.teacherpad.TeacherPadApplication;
import java.lang.Thread.UncaughtExceptionHandler;

public class AutoRestartExceptionHandle implements UncaughtExceptionHandler {
    private UncaughtExceptionHandler mOldExceptionHandle;
    private Thread mRestartThread = new Thread() {
        public void run() {
            Context context = MyiBaseApplication.getBaseAppContext();
            if (context != null) {
                context.stopService(new Intent(context, IMService.class));
                context.stopService(new Intent(context, ChatWindow.class));
                context.stopService(new Intent(context, DrawWindow.class));
                context.stopService(new Intent(context, TextWindow.class));
                context.stopService(new Intent(context, VoiceWindow.class));
                context.stopService(new Intent(context, VideoWindow.class));
                context.stopService(new Intent(context, VideoChatWindow.class));
                context.stopService(new Intent(context, CameraWindow.class));
                context.stopService(new Intent(context, AnswerSheetWindow.class));
                context.stopService(new Intent(context, CountDownService.class));
            }
            super.run();
        }
    };

    public AutoRestartExceptionHandle(UncaughtExceptionHandler oldExceptionHandle) {
        this.mOldExceptionHandle = oldExceptionHandle;
    }

    public static void restartApp(int nTimeInMS) {
        try {
            Intent intent = new Intent(MyiBaseApplication.getBaseAppContext(), MainActivity.class);
            intent.addFlags(335577088);
            ((AlarmManager) MyiBaseApplication.getBaseAppContext().getSystemService("alarm")).set(1, System.currentTimeMillis() + ((long) nTimeInMS), PendingIntent.getActivity(MyiBaseApplication.getBaseAppContext(), 0, intent, intent.getFlags()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void uncaughtException(Thread thread, Throwable ex) {
        Context context = MyiBaseApplication.getBaseAppContext();
        if (context != null) {
            IMService.hideChatNotifyBar();
            if (TeacherPadApplication.mRecordStatusBarDisplayer != null) {
                TeacherPadApplication.mRecordStatusBarDisplayer.hideMessage();
                TeacherPadApplication.mRecordStatusBarDisplayer.shutDown();
                TeacherPadApplication.mRecordStatusBarDisplayer = null;
            }
            context.stopService(new Intent(context, ScreenRecorderService.class));
            context.stopService(new Intent(context, ScreenRecorderService2.class));
            context.stopService(new Intent(context, ScreenRecorderService3.class));
            context.stopService(new Intent(context, IMService.class));
            context.stopService(new Intent(context, ChatWindow.class));
            context.stopService(new Intent(context, DrawWindow.class));
            context.stopService(new Intent(context, TextWindow.class));
            context.stopService(new Intent(context, VoiceWindow.class));
            context.stopService(new Intent(context, VideoWindow.class));
            context.stopService(new Intent(context, VideoChatWindow.class));
            context.stopService(new Intent(context, CameraWindow.class));
            context.stopService(new Intent(context, AnswerSheetWindow.class));
            context.stopService(new Intent(context, CountDownService.class));
        }
        if (this.mOldExceptionHandle != null) {
            this.mOldExceptionHandle.uncaughtException(thread, ex);
        }
    }
}
