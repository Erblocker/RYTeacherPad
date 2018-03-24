package com.netspace.library.components;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import com.netspace.library.interfaces.IComponents;
import com.netspace.library.interfaces.IComponents.ComponentCallBack;
import com.netspace.library.struct.UserAnswerSheetImage;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.pad.library.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.http.HttpStatus;

public class UserAnswersComponent extends FrameLayout implements IComponents {
    private String mCorrectAnswer = "";
    private View mRootView;
    private String mScheduleGUID;
    private TableLayout mTableLayout;
    private ArrayList<String> marrAllAnswerGUIDs = new ArrayList();
    private ArrayList<String> marrAllAnswers = new ArrayList();
    private ArrayList<String> marrAnswers = new ArrayList();
    private ArrayList<UserAnswerSheetImage> marrUserImages = new ArrayList();
    private boolean mbMulti = false;
    private HashMap<String, ArrayList<String>> mmapAnswerStudents = new HashMap();
    private float mnFullScore = 0.0f;
    private int mnStudentsCount = 0;

    public UserAnswersComponent(Context context) {
        super(context);
        initView();
    }

    public UserAnswersComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public UserAnswersComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void initView() {
        this.mRootView = ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.component_useranswers, this, true);
        this.mTableLayout = (TableLayout) this.mRootView.findViewById(R.id.table);
    }

    public void setFullScore(float fFullScore) {
        this.mnFullScore = fFullScore;
    }

    public void setScheduleGUID(String szGUID) {
        this.mScheduleGUID = szGUID;
    }

    public void addStudentAnswerImage(String szScheduleResultGUID, String szStudentName, String szClientID, String szQuestionGUID, String szAnswer0, String szAnswer1, String szAnswer2, String szAnswer1Preview, float fAnswerScore) {
        UserAnswerSheetImage newData = new UserAnswerSheetImage();
        newData.szRealName = szStudentName;
        newData.szClientID = szClientID;
        newData.szQuestionGUID = szQuestionGUID;
        newData.szAnswer0 = szAnswer0;
        newData.szAnswer1 = szAnswer1;
        newData.szAnswer2 = szAnswer2;
        newData.szAnswer1Preview = szAnswer1Preview;
        newData.nFullScore = this.mnFullScore;
        newData.nAnswerScore = fAnswerScore;
        newData.szGUID = szScheduleResultGUID;
        this.marrUserImages.add(newData);
    }

    public void setCorrectAnswer(String szCorrectAnswer) {
        this.mCorrectAnswer = szCorrectAnswer;
    }

    public void addStudentAnswer(String szScheduleResultGUID, String szStudentName, String szAnswer, boolean bCorrectAnswer) {
        int i;
        szAnswer = szAnswer.toUpperCase();
        this.marrAllAnswerGUIDs.add(szScheduleResultGUID);
        this.marrAllAnswers.add(szAnswer);
        this.mnStudentsCount++;
        if (szAnswer.length() > 1) {
            this.mbMulti = true;
        }
        if (bCorrectAnswer) {
            this.mCorrectAnswer = szAnswer;
        }
        ArrayList<String> arrAllAnswers = new ArrayList();
        arrAllAnswers.add(szAnswer);
        if (szAnswer.length() > 1) {
            for (i = 0; i < szAnswer.length(); i++) {
                arrAllAnswers.add("包含" + szAnswer.charAt(i));
            }
        }
        for (i = 0; i < arrAllAnswers.size(); i++) {
            szAnswer = (String) arrAllAnswers.get(i);
            if (!(szAnswer.isEmpty() || Utilities.isInArray(this.marrAnswers, szAnswer))) {
                this.marrAnswers.add(szAnswer);
            }
            if (this.mmapAnswerStudents.containsKey(szAnswer)) {
                ((ArrayList) this.mmapAnswerStudents.get(szAnswer)).add(szStudentName);
            } else {
                ArrayList<String> arrStudentNames = new ArrayList();
                arrStudentNames.add(szStudentName);
                this.mmapAnswerStudents.put(szAnswer, arrStudentNames);
            }
        }
    }

    public void buildUI() {
        TableRow newRow;
        if (this.marrUserImages.size() > 0) {
            this.mTableLayout.removeAllViews();
            this.mTableLayout.setDividerDrawable(null);
            int nCount = 0;
            newRow = new TableRow(getContext());
            this.mTableLayout.addView(newRow);
            Iterator it = this.marrUserImages.iterator();
            while (it.hasNext()) {
                UserAnswerSheetImage oneImage = (UserAnswerSheetImage) it.next();
                if (nCount == 3) {
                    newRow = new TableRow(getContext());
                    this.mTableLayout.addView(newRow);
                    nCount = 0;
                }
                AnswerSheetOneAnswerComponent oneComponent = new AnswerSheetOneAnswerComponent(getContext());
                oneComponent.setData(oneImage);
                oneComponent.setScheduleGUID(this.mScheduleGUID);
                newRow.addView(oneComponent, Utilities.dpToPixel((int) HttpStatus.SC_MULTIPLE_CHOICES, getContext()), Utilities.dpToPixel((int) HttpStatus.SC_MULTIPLE_CHOICES, getContext()));
                nCount++;
            }
            return;
        }
        Collections.sort(this.marrAnswers, String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < this.marrAnswers.size(); i++) {
            String szOneAnswer = (String) this.marrAnswers.get(i);
            ArrayList<String> arrUserNames = (ArrayList) this.mmapAnswerStudents.get(szOneAnswer);
            newRow = new TableRow(getContext());
            this.mTableLayout.addView(newRow);
            newRow.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (v.getTag() == null) {
                        return;
                    }
                    if (v.isSelected()) {
                        v.setBackgroundColor(60928);
                        UserAnswersComponent.this.mCorrectAnswer = "";
                        v.setSelected(false);
                        return;
                    }
                    v.setBackgroundColor(-16716288);
                    UserAnswersComponent.this.mCorrectAnswer = (String) v.getTag();
                    v.setSelected(true);
                }
            });
            TextView textView = new TextView(getContext());
            textView.setText(szOneAnswer);
            newRow.addView(textView);
            if (szOneAnswer.indexOf("包含") == -1) {
                newRow.setTag(szOneAnswer);
                if (szOneAnswer.equalsIgnoreCase(this.mCorrectAnswer)) {
                    newRow.setBackgroundColor(-16716288);
                    newRow.setSelected(true);
                }
            }
            LayoutParams params = (LayoutParams) textView.getLayoutParams();
            int dpToPixel = Utilities.dpToPixel(10, getContext());
            params.bottomMargin = dpToPixel;
            params.topMargin = dpToPixel;
            params.rightMargin = dpToPixel;
            params.leftMargin = dpToPixel;
            textView = new TextView(getContext());
            textView.setText(new StringBuilder(String.valueOf(String.valueOf(arrUserNames.size()))).append("人").toString());
            newRow.addView(textView);
            params = (LayoutParams) textView.getLayoutParams();
            dpToPixel = Utilities.dpToPixel(10, getContext());
            params.bottomMargin = dpToPixel;
            params.topMargin = dpToPixel;
            params.rightMargin = dpToPixel;
            params.leftMargin = dpToPixel;
            textView = new TextView(getContext());
            textView.setText(new StringBuilder(String.valueOf(String.valueOf((int) ((((float) arrUserNames.size()) / ((float) this.mnStudentsCount)) * 100.0f)))).append("%").toString());
            newRow.addView(textView);
            params = (LayoutParams) textView.getLayoutParams();
            dpToPixel = Utilities.dpToPixel(10, getContext());
            params.bottomMargin = dpToPixel;
            params.topMargin = dpToPixel;
            params.rightMargin = dpToPixel;
            params.leftMargin = dpToPixel;
            textView = new TextView(getContext());
            String szNames = "";
            for (int j = 0; j < arrUserNames.size(); j++) {
                if (szNames.length() > 0) {
                    szNames = new StringBuilder(String.valueOf(szNames)).append("、").toString();
                }
                szNames = new StringBuilder(String.valueOf(szNames)).append((String) arrUserNames.get(j)).toString();
            }
            textView.setText(szNames);
            newRow.addView(textView);
            params = (LayoutParams) textView.getLayoutParams();
            dpToPixel = Utilities.dpToPixel(10, getContext());
            params.bottomMargin = dpToPixel;
            params.topMargin = dpToPixel;
            params.rightMargin = dpToPixel;
            params.leftMargin = dpToPixel;
        }
    }

    public void save() {
        WebServiceCallItemObject CallItem = new WebServiceCallItemObject("LessonsScheduleSetAnswerResult", null);
        CallItem.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
            }
        });
        ArrayList<Integer> arrAnswerResult;
        ArrayList<Integer> arrAnswerScore;
        int i;
        if (this.marrAllAnswerGUIDs.size() <= 0) {
            ArrayList<String> arrAnswerGUIDs = new ArrayList();
            arrAnswerResult = new ArrayList();
            arrAnswerScore = new ArrayList();
            for (i = 0; i < this.marrUserImages.size(); i++) {
                UserAnswerSheetImage oneImage = (UserAnswerSheetImage) this.marrUserImages.get(i);
                if (oneImage.nAnswerResult != 0) {
                    arrAnswerGUIDs.add(oneImage.szGUID);
                    arrAnswerResult.add(Integer.valueOf(oneImage.nAnswerResult));
                    arrAnswerScore.add(Integer.valueOf((int) oneImage.nAnswerScore));
                }
            }
            CallItem.setParam("arrGUID", arrAnswerGUIDs);
            CallItem.setParam("arrAnswerResult", arrAnswerResult);
            CallItem.setParam("arrAnswerScore", arrAnswerScore);
        } else if (!this.mCorrectAnswer.isEmpty()) {
            arrAnswerResult = new ArrayList();
            arrAnswerScore = new ArrayList();
            for (i = 0; i < this.marrAllAnswers.size(); i++) {
                if (((String) this.marrAllAnswers.get(i)).equalsIgnoreCase(this.mCorrectAnswer)) {
                    arrAnswerResult.add(Integer.valueOf(2));
                    arrAnswerScore.add(Integer.valueOf((int) this.mnFullScore));
                } else {
                    arrAnswerResult.add(Integer.valueOf(-1));
                    arrAnswerScore.add(Integer.valueOf(0));
                }
            }
            CallItem.setParam("arrGUID", this.marrAllAnswerGUIDs);
            CallItem.setParam("arrAnswerResult", arrAnswerResult);
            CallItem.setParam("arrAnswerScore", arrAnswerScore);
        } else {
            return;
        }
        CallItem.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(CallItem);
    }

    public void intentComplete(Intent intent) {
    }

    public void setLocked(boolean bLock) {
    }

    public void setData(String szData) {
    }

    public String getData() {
        return null;
    }

    public void setCallBack(ComponentCallBack ComponentCallBack) {
    }
}
