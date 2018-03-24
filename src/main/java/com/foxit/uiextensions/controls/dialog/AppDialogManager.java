package com.foxit.uiextensions.controls.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.PopupWindow;
import java.util.Stack;

public class AppDialogManager {
    private static AppDialogManager mInstance = null;
    private final Stack<E> mStack = new Stack();

    public interface CancelListener {
        void cancel();
    }

    class E {
        CancelListener listener;
        Object obj;

        E(Object obj, CancelListener listener) {
            this.obj = obj;
            this.listener = listener;
        }
    }

    public static AppDialogManager getInstance() {
        if (mInstance == null) {
            mInstance = new AppDialogManager();
        }
        return mInstance;
    }

    protected AppDialogManager() {
    }

    public void showAllowManager(DialogFragment fragment, FragmentManager manager, String tag, CancelListener listener) {
        showInner(true, fragment, manager, tag, listener);
    }

    public void show(DialogFragment fragment, FragmentManager manager, String tag, CancelListener listener) {
        showInner(false, fragment, manager, tag, listener);
    }

    private void showInner(boolean allowManager, DialogFragment fragment, FragmentManager manager, String tag, CancelListener listener) {
        if (fragment != null) {
            try {
                FragmentTransaction transaction = manager.beginTransaction();
                Fragment targetFragment = manager.findFragmentByTag(tag);
                if (targetFragment != null) {
                    transaction.remove(targetFragment);
                }
                transaction.add((Fragment) fragment, tag);
                transaction.commitAllowingStateLoss();
                if (allowManager && !this.mStack.contains(fragment)) {
                    this.mStack.push(new E(fragment, listener));
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.cancel();
                }
                e.printStackTrace();
            }
        }
    }

    public void dismiss(DialogFragment fragment) {
        if (fragment != null) {
            if (this.mStack.contains(fragment)) {
                this.mStack.remove(fragment);
            }
            if (!fragment.isDetached()) {
                try {
                    fragment.dismissAllowingStateLoss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void showAllowManager(Dialog dialog, CancelListener listener) {
        showInner(true, dialog, listener);
    }

    public void show(Dialog dialog, CancelListener listener) {
        showInner(false, dialog, listener);
    }

    private void showInner(boolean allowManager, Dialog dialog, CancelListener listener) {
        if (dialog != null) {
            try {
                dialog.show();
                if (allowManager && !this.mStack.contains(dialog)) {
                    this.mStack.push(new E(dialog, listener));
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.cancel();
                }
                e.printStackTrace();
            }
        }
    }

    public void dismiss(Dialog dialog) {
        if (dialog != null) {
            if (this.mStack.contains(dialog)) {
                this.mStack.remove(dialog);
            }
            if (dialog.isShowing()) {
                try {
                    dialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void show(AlertDialog dialog, CancelListener listener) {
        showInner(false, dialog, listener);
    }

    private void showInner(boolean allowManager, AlertDialog dialog, CancelListener listener) {
        if (dialog != null) {
            try {
                dialog.show();
                if (allowManager && !this.mStack.contains(dialog)) {
                    this.mStack.push(new E(dialog, listener));
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.cancel();
                }
                e.printStackTrace();
            }
        }
    }

    public void dismiss(AlertDialog dialog) {
        if (dialog != null) {
            if (this.mStack.contains(dialog)) {
                this.mStack.remove(dialog);
            }
            if (dialog.isShowing()) {
                try {
                    dialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void dismiss(PopupWindow popup) {
        if (popup != null) {
            if (this.mStack.contains(popup)) {
                this.mStack.remove(popup);
            }
            if (popup.isShowing()) {
                try {
                    popup.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void closeAllDialog() {
        while (!this.mStack.isEmpty()) {
            E e = (E) this.mStack.pop();
            if (!(e == null || e.obj == null)) {
                if (e.obj instanceof DialogFragment) {
                    dismiss((DialogFragment) e.obj);
                } else if (e.obj instanceof Dialog) {
                    dismiss((Dialog) e.obj);
                } else if (e.obj instanceof AlertDialog) {
                    dismiss((AlertDialog) e.obj);
                } else if (e.obj instanceof PopupWindow) {
                    dismiss((PopupWindow) e.obj);
                }
                if (e.listener != null) {
                    e.listener.cancel();
                }
            }
        }
    }
}
