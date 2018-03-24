package com.foxit.uiextensions.security;

import android.view.KeyEvent;

public interface ISecurityItemHandler {
    int[] getItemIds();

    boolean isAvailable();

    void onActive(int i);

    boolean onKeyDown(int i, KeyEvent keyEvent);
}
