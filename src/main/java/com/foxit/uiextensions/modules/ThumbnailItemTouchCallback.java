package com.foxit.uiextensions.modules;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;

/* compiled from: ThumbnailSupport */
class ThumbnailItemTouchCallback extends Callback {
    private final ItemTouchAdapter mItemTouchAdapter;
    private OnDragListener onDragListener;

    /* compiled from: ThumbnailSupport */
    public interface ItemTouchAdapter {
        void onMove(int i, int i2);
    }

    /* compiled from: ThumbnailSupport */
    public interface OnDragListener {
        void onFinishDrag();
    }

    public ThumbnailItemTouchCallback(ItemTouchAdapter itemTouchAdapter) {
        this.mItemTouchAdapter = itemTouchAdapter;
    }

    public int getMovementFlags(RecyclerView recyclerView, ViewHolder viewHolder) {
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            return Callback.makeMovementFlags(15, 0);
        }
        return Callback.makeMovementFlags(3, 0);
    }

    public boolean onMove(RecyclerView recyclerView, ViewHolder viewHolder, ViewHolder target) {
        this.mItemTouchAdapter.onMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    public void onSwiped(ViewHolder viewHolder, int direction) {
    }

    public void clearView(RecyclerView recyclerView, ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        if (this.onDragListener != null) {
            this.onDragListener.onFinishDrag();
        }
    }

    public ThumbnailItemTouchCallback setOnDragListener(OnDragListener onDragListener) {
        this.onDragListener = onDragListener;
        return this;
    }
}
