package com.foxit.uiextensions.modules;

import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnItemTouchListener;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import com.foxit.uiextensions.modules.ThumbnailAdapter.ThumbViewHolder;

/* compiled from: ThumbnailSupport */
abstract class OnThumbnailItemTouchListener implements OnItemTouchListener {
    private final GestureDetectorCompat mGestureDetector;
    private final RecyclerView recyclerView;

    /* compiled from: ThumbnailSupport */
    private class ItemTouchHelperGestureListener extends SimpleOnGestureListener {
        private ItemTouchHelperGestureListener() {
        }

        public boolean onSingleTapUp(MotionEvent e) {
            View child = OnThumbnailItemTouchListener.this.recyclerView.findChildViewUnder(e.getX(), e.getY());
            if (child == null) {
                return false;
            }
            ViewHolder vh = OnThumbnailItemTouchListener.this.recyclerView.getChildViewHolder(child);
            return ((ThumbViewHolder) vh).inEditView((int) e.getRawX(), (int) e.getRawY()) || OnThumbnailItemTouchListener.this.onItemClick(vh);
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            View child1 = OnThumbnailItemTouchListener.this.recyclerView.findChildViewUnder(e1.getX(), e1.getY());
            View child2 = OnThumbnailItemTouchListener.this.recyclerView.findChildViewUnder(e2.getX(), e2.getY());
            if (child1 == null || child1 != child2) {
                return false;
            }
            ViewHolder vh = OnThumbnailItemTouchListener.this.recyclerView.getChildViewHolder(child1);
            if (e1.getX() - e2.getX() > ((float) 20) && Math.abs(velocityX) > ((float) null)) {
                return OnThumbnailItemTouchListener.this.onToLeftFling(vh);
            }
            if (e2.getX() - e1.getX() <= ((float) 20) || Math.abs(velocityX) <= ((float) null)) {
                return false;
            }
            return OnThumbnailItemTouchListener.this.onToRightFling(vh);
        }

        public void onLongPress(MotionEvent e) {
            View child = OnThumbnailItemTouchListener.this.recyclerView.findChildViewUnder(e.getX(), e.getY());
            if (child != null) {
                OnThumbnailItemTouchListener.this.onLongPress(OnThumbnailItemTouchListener.this.recyclerView.getChildViewHolder(child));
            }
        }
    }

    public abstract boolean onItemClick(ViewHolder viewHolder);

    public abstract void onLongPress(ViewHolder viewHolder);

    abstract boolean onToLeftFling(ViewHolder viewHolder);

    abstract boolean onToRightFling(ViewHolder viewHolder);

    public OnThumbnailItemTouchListener(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        this.mGestureDetector = new GestureDetectorCompat(recyclerView.getContext(), new ItemTouchHelperGestureListener());
    }

    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        this.mGestureDetector.onTouchEvent(e);
        return false;
    }

    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        this.mGestureDetector.onTouchEvent(e);
    }

    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }
}
