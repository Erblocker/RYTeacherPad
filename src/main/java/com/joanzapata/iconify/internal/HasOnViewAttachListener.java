package com.joanzapata.iconify.internal;

import android.widget.TextView;

public interface HasOnViewAttachListener {

    public static class HasOnViewAttachListenerDelegate {
        private OnViewAttachListener listener;
        private final TextView view;

        public HasOnViewAttachListenerDelegate(TextView view) {
            this.view = view;
        }

        public void setOnViewAttachListener(OnViewAttachListener listener) {
            if (this.listener != null) {
                this.listener.onDetach();
            }
            this.listener = listener;
            if (this.view.isAttachedToWindow() && listener != null) {
                listener.onAttach();
            }
        }

        public void onAttachedToWindow() {
            if (this.listener != null) {
                this.listener.onAttach();
            }
        }

        public void onDetachedFromWindow() {
            if (this.listener != null) {
                this.listener.onDetach();
            }
        }
    }

    public interface OnViewAttachListener {
        void onAttach();

        void onDetach();
    }

    void setOnViewAttachListener(OnViewAttachListener onViewAttachListener);
}
