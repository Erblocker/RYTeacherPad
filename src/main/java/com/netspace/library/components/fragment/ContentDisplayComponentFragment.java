package com.netspace.library.components.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.components.ContentDisplayComponent;
import com.netspace.library.interfaces.IComponents;
import com.netspace.library.interfaces.IComponents.ComponentCallBack;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;

public class ContentDisplayComponentFragment extends Fragment implements ComponentCallBack {
    private ContentDisplayComponent mContentDisplayComponent;
    private String mData;
    private ContentDisplayExtensionCallBack mExtensionCallBack;
    private int mOptions;
    private boolean mbCanAddToQuestionBook = false;
    private boolean mbCanAddToResourceLibrary = false;
    private boolean mbHasAnswerSwitchButton = false;
    private boolean mbShowAnswers = false;

    public interface ContentDisplayExtensionCallBack {
        void onAddToQuestionBook(ResourceItemData resourceItemData, int i);

        void onAddToResourceLibrary(ResourceItemData resourceItemData);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.mContentDisplayComponent != null) {
            return this.mContentDisplayComponent;
        }
        Bundle argument = getArguments();
        if (argument != null) {
            this.mOptions = argument.getInt(ContentDisplayComponent.FLAGS, this.mOptions);
            this.mData = argument.getString("data", this.mData);
            this.mbCanAddToQuestionBook = argument.getBoolean(ContentDisplayComponent.ADDTOQUESTIONBOOK, this.mbCanAddToQuestionBook);
            this.mbCanAddToResourceLibrary = argument.getBoolean(ContentDisplayComponent.ADDTORESOURCELIBRARY, this.mbCanAddToResourceLibrary);
            this.mbHasAnswerSwitchButton = argument.getBoolean(ContentDisplayComponent.ANSWERSWITCHBUTTON, this.mbHasAnswerSwitchButton);
        }
        this.mContentDisplayComponent = new ContentDisplayComponent(getActivity());
        this.mContentDisplayComponent.setDisplayOptions(this.mOptions);
        this.mContentDisplayComponent.setCallBack(this);
        this.mContentDisplayComponent.setData(this.mData);
        this.mContentDisplayComponent.initContent();
        if ((this.mOptions & 64) == 64) {
            this.mbShowAnswers = true;
        }
        setHasOptionsMenu(true);
        int nPadding = Utilities.dpToPixel(12, getActivity());
        this.mContentDisplayComponent.setPadding(nPadding, nPadding, nPadding, nPadding);
        return this.mContentDisplayComponent;
    }

    public void setHasAnswerSwitchButton(boolean bEnable) {
        this.mbHasAnswerSwitchButton = bEnable;
    }

    public void setCanAddToQuestionBook(boolean bEnable) {
        this.mbCanAddToQuestionBook = bEnable;
    }

    public void setCanAddToResourceLibrary(boolean bEnable) {
        this.mbCanAddToResourceLibrary = bEnable;
    }

    public void setExtensionCallBack(ContentDisplayExtensionCallBack CallBack) {
        this.mExtensionCallBack = CallBack;
    }

    public void OnDataLoaded(String szFileName, IComponents Component) {
    }

    public void OnDataUploaded(String szData, IComponents Component) {
    }

    public void OnRequestIntent(Intent intent, IComponents Component) {
    }

    private void setData(String szData) {
        this.mData = szData;
    }

    private void setDisplayOptions(int nOptions) {
        this.mOptions = nOptions;
    }

    public ContentDisplayComponent getComponent() {
        return this.mContentDisplayComponent;
    }

    public String getData() {
        return this.mContentDisplayComponent.getData();
    }

    public void setCallBack(ComponentCallBack ComponentCallBack) {
        this.mContentDisplayComponent.setCallBack(ComponentCallBack);
    }

    public void intentComplete(Intent intent) {
        this.mContentDisplayComponent.intentComplete(intent);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        boolean z = false;
        if (item.getItemId() == R.id.action_showanswers) {
            if (!this.mbShowAnswers) {
                z = true;
            }
            this.mbShowAnswers = z;
            if (this.mbShowAnswers) {
                this.mOptions |= 64;
            } else {
                this.mOptions &= -65;
            }
            String szUserAnswers = this.mContentDisplayComponent.getUserAnswers();
            this.mContentDisplayComponent.clear();
            this.mContentDisplayComponent.setDisplayOptions(this.mOptions);
            this.mContentDisplayComponent.setUserAnswer(szUserAnswers);
            this.mContentDisplayComponent.setData(this.mData);
            ActivityCompat.invalidateOptionsMenu(getActivity());
        } else if (item.getItemId() == R.id.action_addtoquestionbook) {
            if (this.mExtensionCallBack != null) {
                nSubject = this.mContentDisplayComponent.getResourceSubjectID();
                ResourceItemData = new ResourceItemData();
                ResourceItemData.szResourceGUID = this.mContentDisplayComponent.getResourceGUID();
                ResourceItemData.szTitle = this.mContentDisplayComponent.getTitle();
                if (this.mContentDisplayComponent.isQuestion()) {
                    ResourceItemData.nType = 0;
                } else {
                    ResourceItemData.nType = 1;
                }
                this.mExtensionCallBack.onAddToQuestionBook(ResourceItemData, nSubject);
            }
        } else if (item.getItemId() == R.id.action_addresourcelibrary && this.mExtensionCallBack != null) {
            nSubject = this.mContentDisplayComponent.getResourceSubjectID();
            ResourceItemData = new ResourceItemData();
            ResourceItemData.szResourceGUID = this.mContentDisplayComponent.getResourceGUID();
            ResourceItemData.szTitle = this.mContentDisplayComponent.getTitle();
            if (this.mContentDisplayComponent.isQuestion()) {
                ResourceItemData.nType = 0;
            } else {
                ResourceItemData.nType = 1;
            }
            this.mExtensionCallBack.onAddToResourceLibrary(ResourceItemData);
        }
        return super.onOptionsItemSelected(item);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_contentdisplay, menu);
        if (!this.mbCanAddToQuestionBook) {
            menu.findItem(R.id.action_addtoquestionbook).setVisible(false);
        }
        if (this.mbCanAddToResourceLibrary) {
            menu.findItem(R.id.action_addresourcelibrary).setIcon(new IconDrawable(getContext(), FontAwesomeIcons.fa_save).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        } else {
            menu.findItem(R.id.action_addresourcelibrary).setVisible(false);
        }
        if (!this.mbHasAnswerSwitchButton) {
            menu.findItem(R.id.action_showanswers).setVisible(false);
        } else if (menu.findItem(R.id.action_showanswers) != null) {
            menu.findItem(R.id.action_showanswers).setCheckable(true);
            menu.findItem(R.id.action_showanswers).setChecked(this.mbShowAnswers);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }
}
