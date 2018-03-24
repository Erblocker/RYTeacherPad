package com.foxit.uiextensions.utils;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import com.foxit.uiextensions.R;

public class AppKeyboardUtil {
    private static Handler mMainThreadHandler;

    /* renamed from: com.foxit.uiextensions.utils.AppKeyboardUtil$1 */
    class AnonymousClass1 implements OnGlobalLayoutListener {
        private final /* synthetic */ IKeyboardListener val$listener;
        private final /* synthetic */ int[] val$mKeyboardHeight;
        private final /* synthetic */ int[] val$mOldKeyboardHeight;
        private final /* synthetic */ ViewGroup val$parent;

        AnonymousClass1(ViewGroup viewGroup, int[] iArr, int[] iArr2, IKeyboardListener iKeyboardListener) {
            this.val$parent = viewGroup;
            this.val$mKeyboardHeight = iArr;
            this.val$mOldKeyboardHeight = iArr2;
            this.val$listener = iKeyboardListener;
        }

        public void onGlobalLayout() {
            Rect r = new Rect();
            this.val$parent.getWindowVisibleDisplayFrame(r);
            this.val$mKeyboardHeight[0] = this.val$parent.getRootView().getHeight() - (r.bottom - r.top);
            if (this.val$mOldKeyboardHeight[0] != this.val$mKeyboardHeight[0]) {
                if (((double) this.val$mOldKeyboardHeight[0]) > ((double) this.val$parent.getRootView().getHeight()) / 5.0d && this.val$mKeyboardHeight[0] == 0) {
                    this.val$listener.onKeyboardClosed();
                }
                this.val$mOldKeyboardHeight[0] = this.val$mKeyboardHeight[0];
                if (((double) this.val$mKeyboardHeight[0]) > ((double) this.val$parent.getRootView().getHeight()) / 5.0d) {
                    Handler access$0 = AppKeyboardUtil.getMainThreadHandler();
                    final ViewGroup viewGroup = this.val$parent;
                    final IKeyboardListener iKeyboardListener = this.val$listener;
                    access$0.postDelayed(new Runnable() {
                        public void run() {
                            Rect r = new Rect();
                            viewGroup.getWindowVisibleDisplayFrame(r);
                            int h = viewGroup.getRootView().getHeight() - (r.bottom - r.top);
                            if (((double) h) > ((double) viewGroup.getRootView().getHeight()) / 5.0d) {
                                iKeyboardListener.onKeyboardOpened(h);
                            }
                        }
                    }, 300);
                }
            }
        }
    }

    /* renamed from: com.foxit.uiextensions.utils.AppKeyboardUtil$2 */
    class AnonymousClass2 extends Handler {
        AnonymousClass2(Looper $anonymous0) {
            super($anonymous0);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Runnable runnable = msg.obj;
            if (runnable != null) {
                runnable.run();
            }
        }
    }

    public interface IKeyboardListener {
        void onKeyboardClosed();

        void onKeyboardOpened(int i);
    }

    @TargetApi(16)
    public static void removeKeyboardListener(View showKeyboardView) {
        if (showKeyboardView.getTag(R.id.keyboard_util) != null && (showKeyboardView.getTag(R.id.keyboard_util) instanceof OnGlobalLayoutListener) && VERSION.SDK_INT >= 16) {
            showKeyboardView.getViewTreeObserver().removeOnGlobalLayoutListener((OnGlobalLayoutListener) showKeyboardView.getTag(R.id.keyboard_util));
            showKeyboardView.setTag(R.id.keyboard_util, null);
        }
    }

    public static void setKeyboardListener(ViewGroup parent, View showKeyboardView, IKeyboardListener listener) {
        int[] mKeyboardHeight = new int[]{0};
        OnGlobalLayoutListener globalLayoutListener = new AnonymousClass1(parent, mKeyboardHeight, new int[1], listener);
        if (VERSION.SDK_INT >= 16) {
            showKeyboardView.setTag(R.id.keyboard_util, globalLayoutListener);
        }
        showKeyboardView.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
    }

    private static Handler getMainThreadHandler() {
        if (mMainThreadHandler == null) {
            mMainThreadHandler = new AnonymousClass2(Looper.getMainLooper());
        }
        return mMainThreadHandler;
    }
}
