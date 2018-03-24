package com.netspace.library.components;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.interfaces.IComponents;
import com.netspace.library.interfaces.IComponents.ComponentCallBack;
import com.netspace.library.restful.provider.device.DeviceOperationRESTServiceProvider;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.DataSynchronizeItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.pad.library.R;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

public class CorrectFootComponent extends FrameLayout implements IComponents {
    private static ArrayList<CorrectFootComponent> marrFootComponents = new ArrayList();
    private ComponentCallBack mCallBack;
    private String mClientID;
    private ContentDisplayComponent mContentComponent;
    private String mData;
    private EditText mEditScore;
    private String mGUID;
    private ImageView mImageViewChat;
    private RadioGroup mResultGroup;
    private View mRootView;
    private boolean mbChanged = false;
    private boolean mbScoreChanged = false;
    private String mszData;

    public CorrectFootComponent(Context context) {
        super(context);
        initView();
    }

    public CorrectFootComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CorrectFootComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void initView() {
        this.mRootView = ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.component_correctfoot, this, true);
        this.mResultGroup = (RadioGroup) this.mRootView.findViewById(R.id.radioCorrectArea);
        this.mResultGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                CorrectFootComponent.this.mbScoreChanged = true;
            }
        });
        this.mResultGroup.check(-1);
        this.mImageViewChat = (ImageView) this.mRootView.findViewById(R.id.buttonChat);
        this.mEditScore = (EditText) this.mRootView.findViewById(R.id.editScore);
        this.mEditScore.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                CorrectFootComponent.this.mbScoreChanged = true;
            }

            public void afterTextChanged(Editable s) {
            }
        });
        this.mImageViewChat.setImageDrawable(new IconDrawable(getContext(), FontAwesomeIcons.fa_twitch).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
    }

    public void setAnswerResult(int nAnswerResult) {
        if (this.mResultGroup != null) {
            if (nAnswerResult == 2) {
                this.mResultGroup.check(R.id.radioCorrect);
            } else if (nAnswerResult == 1) {
                this.mResultGroup.check(R.id.radioHalfCorrect);
            } else if (nAnswerResult == -1) {
                this.mResultGroup.check(R.id.radioWrong);
            }
        }
    }

    public void setAnswerScore(int nScore) {
        this.mEditScore.setText(String.valueOf(nScore));
    }

    public int getAnswerResult() {
        if (this.mResultGroup.getCheckedRadioButtonId() != -1) {
            if (this.mResultGroup.getCheckedRadioButtonId() == R.id.radioCorrect) {
                return 2;
            }
            if (this.mResultGroup.getCheckedRadioButtonId() == R.id.radioHalfCorrect) {
                return 1;
            }
            if (this.mResultGroup.getCheckedRadioButtonId() == R.id.radioWrong) {
                return -1;
            }
        }
        return 0;
    }

    public int getScore() {
        int nScore = 0;
        try {
            nScore = Integer.valueOf(this.mEditScore.getText().toString()).intValue();
        } catch (NumberFormatException e) {
        }
        return nScore;
    }

    public boolean isChanged() {
        return this.mbChanged || this.mContentComponent.isChanged() || this.mbScoreChanged;
    }

    public boolean isScoreChanged() {
        return this.mbScoreChanged;
    }

    public void setScoreChanged(boolean bChanged) {
        this.mbScoreChanged = bChanged;
    }

    public void setChanged(boolean bChanged) {
        this.mbChanged = bChanged;
    }

    public String getGUID() {
        return this.mGUID;
    }

    public void setData(String szData) {
        this.mData = szData;
        try {
            JSONObject JSON = new JSONObject(this.mData);
            this.mClientID = JSON.getString(DeviceOperationRESTServiceProvider.CLIENTID);
            this.mGUID = JSON.getString("guid");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (this.mClientID == null) {
            throw new IllegalArgumentException("ClientID is not set in szData");
        } else if (this.mGUID == null) {
            throw new IllegalArgumentException("GUID is not set in szData");
        }
    }

    public String getData() {
        return "";
    }

    public void setDisplayComponent(ContentDisplayComponent DisplayComponent) {
        this.mContentComponent = DisplayComponent;
    }

    public void setCallBack(ComponentCallBack ComponentCallBack) {
        this.mCallBack = ComponentCallBack;
    }

    public void save() {
        if (this.mContentComponent == null) {
            throw new IllegalArgumentException("ContentComponent is not set. Can not save correction handwrite.");
        }
        if (!this.mContentComponent.getDrawView().getDataAsString().isEmpty()) {
            DataSynchronizeItemObject ResourceObject = new DataSynchronizeItemObject(this.mGUID + "_CorrectResult", null);
            ResourceObject.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    CorrectFootComponent.this.mContentComponent.setChanged(false);
                }
            });
            ResourceObject.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                }
            });
            ResourceObject.setClientID(this.mClientID);
            ResourceObject.writeTextData(this.mContentComponent.getDrawView().getDataAsString());
            ResourceObject.setReadOperation(false);
            ResourceObject.setAlwaysActiveCallbacks(true);
            ResourceObject.setIgnoreActivityFinishCheck(true);
            VirtualNetworkObject.addToQueue(ResourceObject);
        }
        ArrayList<Integer> arrAnswerResult = new ArrayList();
        ArrayList<Integer> arrAnswerScore = new ArrayList();
        ArrayList<String> arrGUIDs = new ArrayList();
        final ArrayList<CorrectFootComponent> arrComponents = new ArrayList();
        synchronized (marrFootComponents) {
            for (int i = 0; i < marrFootComponents.size(); i++) {
                CorrectFootComponent OneComponent = (CorrectFootComponent) marrFootComponents.get(i);
                if (OneComponent.isScoreChanged() && OneComponent.getAnswerResult() != 0) {
                    arrAnswerResult.add(Integer.valueOf(OneComponent.getAnswerResult()));
                    arrAnswerScore.add(Integer.valueOf(OneComponent.getScore()));
                    arrGUIDs.add(OneComponent.getGUID());
                    OneComponent.setScoreChanged(false);
                    arrComponents.add(OneComponent);
                }
            }
        }
        if (arrComponents.size() > 0) {
            WebServiceCallItemObject CallItem = new WebServiceCallItemObject("LessonsScheduleSetAnswerResult", null);
            CallItem.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                    for (int i = 0; i < arrComponents.size(); i++) {
                        ((CorrectFootComponent) arrComponents.get(i)).setChanged(true);
                    }
                }
            });
            CallItem.setParam("arrGUID", arrGUIDs);
            CallItem.setParam("arrAnswerResult", arrAnswerResult);
            CallItem.setParam("arrAnswerScore", arrAnswerScore);
            CallItem.setAlwaysActiveCallbacks(true);
            VirtualNetworkObject.addToQueue(CallItem);
        }
    }

    public void intentComplete(Intent intent) {
    }

    public void setLocked(boolean bLock) {
    }

    protected void onDetachedFromWindow() {
        synchronized (marrFootComponents) {
            for (int i = 0; i < marrFootComponents.size(); i++) {
                if (((CorrectFootComponent) marrFootComponents.get(i)).equals(this)) {
                    marrFootComponents.remove(i);
                    break;
                }
            }
        }
        super.onDetachedFromWindow();
    }

    protected void onAttachedToWindow() {
        synchronized (marrFootComponents) {
            marrFootComponents.add(this);
        }
        super.onAttachedToWindow();
    }
}
