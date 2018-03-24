package com.foxit.uiextensions.annots.common;

import com.foxit.sdk.Task;
import com.foxit.sdk.Task.CallBack;
import com.foxit.uiextensions.utils.Event.Callback;

public class EditAnnotTask extends Task {
    private boolean bSuccess = true;
    private EditAnnotEvent mEvent = null;

    /* renamed from: com.foxit.uiextensions.annots.common.EditAnnotTask$1 */
    class AnonymousClass1 implements CallBack {
        private final /* synthetic */ Callback val$callBack;

        AnonymousClass1(Callback callback) {
            this.val$callBack = callback;
        }

        public void result(Task task) {
            EditAnnotTask task1 = (EditAnnotTask) task;
            if (this.val$callBack != null) {
                this.val$callBack.result(task1.mEvent, task1.bSuccess);
            }
        }
    }

    public EditAnnotTask(EditAnnotEvent event, Callback callBack) {
        super(new AnonymousClass1(callBack));
        this.mEvent = event;
    }

    protected void execute() {
        if (this.mEvent != null) {
            this.bSuccess = this.mEvent.execute();
        }
    }

    protected boolean isModify() {
        return super.isModify();
    }
}
