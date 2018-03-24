package com.netspace.library.components;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.controls.CustomFrameLayout;
import com.netspace.library.controls.LinedEditText;
import com.netspace.library.dialog.AnswerSheetOneAnswerDialog;
import com.netspace.library.dialog.AnswerSheetOneAnswerDialog.OnCorrectScoreSelectedListener;
import com.netspace.library.interfaces.IComponents;
import com.netspace.library.interfaces.IComponents.ComponentCallBack;
import com.netspace.library.struct.UserAnswerSheetImage;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObjectManager;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.pad.library.R;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class AnswerSheetOneAnswerComponent extends CustomFrameLayout implements IComponents, OnScrollChangedListener, OnClickListener {
    private ImageView mAnswerImage;
    private ComponentCallBack mCallBack;
    private OnClickListener mClickListener = new OnClickListener() {
        public void onClick(View v) {
            AnswerSheetOneAnswerComponent.this.mCorrectButton.setBackgroundColor(0);
            AnswerSheetOneAnswerComponent.this.mHalfcorrectButton.setBackgroundColor(0);
            AnswerSheetOneAnswerComponent.this.mWrongButton.setBackgroundColor(0);
            v.setBackgroundColor(-16776978);
            if (v.getId() == R.id.imageViewCorrect) {
                AnswerSheetOneAnswerComponent.this.mData.nAnswerResult = 2;
                AnswerSheetOneAnswerComponent.this.mSpinner.setSelection(AnswerSheetOneAnswerComponent.this.marrScores.size() - 1);
            } else if (v.getId() == R.id.imageViewHalfCorrect) {
                AnswerSheetOneAnswerComponent.this.mData.nAnswerResult = 1;
            } else if (v.getId() == R.id.imageViewWrong) {
                AnswerSheetOneAnswerComponent.this.mData.nAnswerResult = -1;
                AnswerSheetOneAnswerComponent.this.mSpinner.setSelection(0);
            }
        }
    };
    private ContextThemeWrapper mContextThemeWrapper;
    private ImageView mCorrectButton;
    private UserAnswerSheetImage mData;
    private ImageView mHalfcorrectButton;
    private View mRootView;
    private String mScheduleGUID;
    private Spinner mSpinner;
    private String mUrlToLoadOnVisible;
    private VirtualNetworkObjectManager mVirtualNetworkObjectManager = new VirtualNetworkObjectManager();
    private ImageView mWrongButton;
    private ArrayList<String> marrScores = new ArrayList();
    private boolean mbDataLoadStarted = false;

    public AnswerSheetOneAnswerComponent(Context context) {
        super(context);
        initView();
    }

    public AnswerSheetOneAnswerComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public AnswerSheetOneAnswerComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        this.mContextThemeWrapper = new ContextThemeWrapper(getContext(), R.style.ComponentTheme);
        this.mRootView = inflater.cloneInContext(this.mContextThemeWrapper).inflate(R.layout.component_answersheetoneanswer, this, true);
        this.mRootView.findViewById(R.id.linearLayoutContent).setOnClickListener(this);
        this.mCorrectButton = (ImageView) this.mRootView.findViewById(R.id.imageViewCorrect);
        this.mHalfcorrectButton = (ImageView) this.mRootView.findViewById(R.id.imageViewHalfCorrect);
        this.mWrongButton = (ImageView) this.mRootView.findViewById(R.id.imageViewWrong);
        this.mAnswerImage = (ImageView) this.mRootView.findViewById(R.id.imageViewThumbnil);
        this.mSpinner = (Spinner) this.mRootView.findViewById(R.id.spinnerScores);
        this.mCorrectButton.setOnClickListener(this.mClickListener);
        this.mHalfcorrectButton.setOnClickListener(this.mClickListener);
        this.mWrongButton.setOnClickListener(this.mClickListener);
    }

    public void setScheduleGUID(String szScheduleGUID) {
        this.mScheduleGUID = szScheduleGUID;
    }

    public void setData(UserAnswerSheetImage data) {
        this.mData = data;
        TextView textView = (TextView) this.mRootView.findViewById(R.id.textViewUserName);
        LinedEditText editText = (LinedEditText) this.mRootView.findViewById(R.id.editText1);
        String szURL = MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/DataSynchronizeGetSingleData?clientid=" + this.mData.szClientID + "&packageid=";
        this.mAnswerImage.setVisibility(4);
        editText.setVisibility(4);
        textView.setText(this.mData.szRealName);
        editText.setFocusable(false);
        int nSelectedIndex = 0;
        for (float fScore = 0.0f; fScore <= this.mData.nFullScore; fScore += 1.0f) {
            this.marrScores.add("得" + String.valueOf((int) fScore) + "分");
            if (fScore == this.mData.nAnswerScore) {
                nSelectedIndex = this.marrScores.size() - 1;
            }
        }
        String[] arrStringObjects = new String[this.marrScores.size()];
        this.marrScores.toArray(arrStringObjects);
        this.mSpinner.setAdapter(new ArrayAdapter(getContext(), R.layout.simple_list_item_smallpad, 16908308, arrStringObjects));
        this.mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String szScoreText = (String) AnswerSheetOneAnswerComponent.this.marrScores.get(position);
                szScoreText = szScoreText.substring(1, szScoreText.length() - 1);
                AnswerSheetOneAnswerComponent.this.mData.nAnswerScore = Float.valueOf(szScoreText).floatValue();
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        this.mSpinner.setSelection(nSelectedIndex);
        if (!this.mData.szAnswer0.isEmpty()) {
            editText.setVisibility(0);
            editText.setText(this.mData.szAnswer0);
            editText.setTextSize(9.0f);
        } else if (!this.mData.szAnswer1Preview.isEmpty()) {
            szURL = new StringBuilder(String.valueOf(szURL)).append(this.mData.szAnswer1Preview).toString();
            this.mAnswerImage.setVisibility(0);
            this.mUrlToLoadOnVisible = szURL;
        } else if (!this.mData.szAnswer2.isEmpty()) {
            szURL = new StringBuilder(String.valueOf(szURL)).append(this.mData.szAnswer2).toString();
            this.mAnswerImage.setVisibility(0);
            this.mUrlToLoadOnVisible = szURL;
        }
    }

    public boolean isDataLoaded() {
        return this.mbDataLoadStarted;
    }

    private void loadData() {
        if (!this.mbDataLoadStarted) {
            this.mbDataLoadStarted = true;
            if (this.mUrlToLoadOnVisible != null) {
                Picasso.with(getContext()).load(this.mUrlToLoadOnVisible).resize(0, 500).into(this.mAnswerImage);
            }
            WebServiceCallItemObject ItemObject = new WebServiceCallItemObject("LessonsScheduleGetQuestionAnswer", null);
            ItemObject.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    String szAnswerResult = (String) ItemObject.getParam("0");
                    String szAnswerScore = (String) ItemObject.getParam("2");
                    AnswerSheetOneAnswerComponent.this.mData.nAnswerScore = (float) Utilities.toInt(szAnswerScore);
                    AnswerSheetOneAnswerComponent.this.mData.nAnswerResult = Utilities.toInt(szAnswerResult);
                    AnswerSheetOneAnswerComponent.this.updateDisplay();
                }
            });
            ItemObject.setParam("lpszLessonsScheduleGUID", this.mScheduleGUID);
            ItemObject.setParam("lpszStudentID", this.mData.szClientID.replace("myipad_", ""));
            ItemObject.setParam("lpszObjectGUID", this.mData.szQuestionGUID);
            ItemObject.setAlwaysActiveCallbacks(true);
            VirtualNetworkObject.addToQueue(ItemObject);
            this.mVirtualNetworkObjectManager.add(ItemObject);
        }
    }

    private void updateDisplay() {
        int nScore = (int) this.mData.nAnswerScore;
        int nAnswerResult = this.mData.nAnswerResult;
        this.mCorrectButton.setBackgroundColor(0);
        this.mHalfcorrectButton.setBackgroundColor(0);
        this.mWrongButton.setBackgroundColor(0);
        if (nAnswerResult == 2) {
            this.mCorrectButton.setBackgroundColor(-16776978);
        } else if (nAnswerResult == 1) {
            this.mHalfcorrectButton.setBackgroundColor(-16776978);
        } else if (nAnswerResult == -1) {
            this.mWrongButton.setBackgroundColor(-16776978);
        }
        for (int i = 0; i < this.marrScores.size(); i++) {
            String szScoreText = (String) this.marrScores.get(i);
            if (Utilities.toInt(szScoreText.substring(1, szScoreText.length() - 1)) == nScore) {
                this.mSpinner.setSelection(i);
                return;
            }
        }
    }

    public void setData(String szData) {
    }

    public String getData() {
        return null;
    }

    public void setCallBack(ComponentCallBack ComponentCallBack) {
        this.mCallBack = ComponentCallBack;
    }

    public void intentComplete(Intent intent) {
    }

    public void setLocked(boolean bLock) {
    }

    protected void onAttachedToWindow() {
        boolean bisInViewport = getLocalVisibleRect(new Rect());
        ViewTreeObserver vto = getViewTreeObserver();
        if (vto != null) {
            vto.addOnScrollChangedListener(this);
        }
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        this.mVirtualNetworkObjectManager.cancelAll();
        ViewTreeObserver vto = getViewTreeObserver();
        if (vto != null) {
            vto.removeOnScrollChangedListener(this);
        }
        super.onDetachedFromWindow();
    }

    public void onScrollChanged() {
        if (getLocalVisibleRect(new Rect()) && !this.mbDataLoadStarted) {
            loadData();
        }
    }

    public void onClick(View v) {
        AnswerSheetOneAnswerDialog dialog = new AnswerSheetOneAnswerDialog();
        Activity activity = UI.getCurrentActivity();
        if (activity instanceof BaseActivity) {
            FragmentTransaction ft = ((BaseActivity) activity).getSupportFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            dialog.setCancelable(true);
            dialog.setData(this.mData);
            dialog.setCallBack(new OnCorrectScoreSelectedListener() {
                public void onScoreSelected() {
                    AnswerSheetOneAnswerComponent.this.updateDisplay();
                }

                public void onImageChanged() {
                    if (AnswerSheetOneAnswerComponent.this.mUrlToLoadOnVisible != null) {
                        Picasso.with(AnswerSheetOneAnswerComponent.this.getContext()).load(AnswerSheetOneAnswerComponent.this.mUrlToLoadOnVisible).networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).resize(0, 500).into(AnswerSheetOneAnswerComponent.this.mAnswerImage);
                    }
                }
            });
            dialog.show(ft, "DetailDialog");
        }
    }
}
