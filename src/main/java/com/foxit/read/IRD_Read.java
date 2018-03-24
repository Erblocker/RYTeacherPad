package com.foxit.read;

import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.PDFViewCtrl.UIExtensionsManager;
import com.foxit.uiextensions.DocumentManager;

public interface IRD_Read {
    void backToPrevActivity();

    void changeState(int i);

    void closeDocument();

    DocumentManager getDocMgr();

    PDFViewCtrl getDocViewer();

    IRD_MainFrame getMainFrame();

    int getState();

    UIExtensionsManager getUIExtensionsManager();

    void init();

    boolean registerLifecycleListener(ILifecycleEventListener iLifecycleEventListener);

    boolean registerStateChangeListener(IRD_StateChangeListener iRD_StateChangeListener);

    boolean unregisterLifecycleListener(ILifecycleEventListener iLifecycleEventListener);

    boolean unregisterStateChangeListener(IRD_StateChangeListener iRD_StateChangeListener);
}
