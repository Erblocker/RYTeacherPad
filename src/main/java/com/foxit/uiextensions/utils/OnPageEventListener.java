package com.foxit.uiextensions.utils;

import com.foxit.sdk.PDFViewCtrl.IPageEventListener;

public class OnPageEventListener implements IPageEventListener {
    public void onPageChanged(int oldPageIndex, int curPageIndex) {
    }

    public void onPageInvisible(int index) {
    }

    public void onPageVisible(int index) {
    }

    public void onPageJumped() {
    }

    public void onPagesWillRotate(int[] pageIndexes, int rotation) {
    }

    public void onPagesWillRemove(int[] pageIndexes) {
    }

    public void onPageWillMove(int index, int dstIndex) {
    }

    public void onPageMoved(boolean success, int index, int dstIndex) {
    }

    public void onPagesRemoved(boolean success, int[] pageIndexes) {
    }

    public void onPagesRotated(boolean success, int[] pageIndexes, int rotation) {
    }

    public void onPagesWillInsert(int dstIndex, int[] pageRanges) {
    }

    public void onPagesInserted(boolean success, int dstIndex, int[] range) {
    }
}
