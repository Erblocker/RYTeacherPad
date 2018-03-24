package com.foxit.uiextensions.controls.panel;

import android.view.View;

public interface PanelHost {
    void addSpec(PanelSpec panelSpec);

    View getContentView();

    PanelSpec getCurrentSpec();

    PanelSpec getSpec(int i);

    void removeSpec(PanelSpec panelSpec);

    void setCurrentSpec(int i);
}
