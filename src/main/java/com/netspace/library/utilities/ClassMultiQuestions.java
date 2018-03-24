package com.netspace.library.utilities;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.widget.Toast;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.service.StudentAnswerImageService;
import com.netspace.library.struct.StudentAnswer;
import com.netspace.library.struct.StudentClassQuestion;
import com.netspace.library.virtualnetworkobject.HttpItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.PutTemporaryStorageItemObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.xsj.crasheye.CrasheyeFileFilter;
import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ClassMultiQuestions {
    private static ClassMultiQuestions mInstance = null;
    private String TAG = "ClassMultiQuestions";
    private MultiQuestionCallBack mCallBack;
    private int mNewQuestionGUIDCallCount = 0;
    private ArrayList<StudentClassQuestion> marrClassQuestions = new ArrayList();
    private int mnCurrentIndex = 0;
    private String mszLocalPCAddress;
    private String mszNewQuestionGUID;

    public interface MultiQuestionCallBack {
        void onNoMoreQuestionNeedToAnswer();

        void onSuccessUploadStudentAnswerJson(String str, String str2);

        void onSwitchToQuestion(String str, ArrayList<StudentAnswer> arrayList);
    }

    public interface OnLoadListCallBack {
        void onListLoadFailed(String str);

        void onListLoadSuccess(String str);
    }

    public ClassMultiQuestions() {
        mInstance = this;
    }

    public static ClassMultiQuestions getInstance() {
        return mInstance;
    }

    public boolean addClassQuestion(String szGUID, int nType, String szCorrectAnswer) {
        synchronized (this.marrClassQuestions) {
            Iterator it = this.marrClassQuestions.iterator();
            while (it.hasNext()) {
                if (((StudentClassQuestion) it.next()).szGUID.equalsIgnoreCase(szGUID)) {
                    return false;
                }
            }
            StudentClassQuestion newQuestion = new StudentClassQuestion();
            newQuestion.szGUID = szGUID;
            newQuestion.nType = nType;
            newQuestion.nQuestionStartTime = System.currentTimeMillis();
            newQuestion.szCorrectAnswer = szCorrectAnswer;
            this.marrClassQuestions.add(newQuestion);
            return true;
        }
    }

    public void setNewQuestionGUID(String szGUID) {
        this.mszNewQuestionGUID = szGUID;
        this.mNewQuestionGUIDCallCount++;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean addClassQuestion(String szGUID, int nType, String szCorrectAnswer, String szIMMessage, String szFrom, String szOptions) {
        boolean bPreviousQuestionFinished = !hasUnfinishQuestions();
        if (szGUID == null) {
            if (this.mszNewQuestionGUID == null) {
                throw new NullPointerException("mszNewQuestionGUID should be set before call addClassQuestion");
            }
            szGUID = this.mszNewQuestionGUID;
            this.mszNewQuestionGUID = null;
        }
        synchronized (this.marrClassQuestions) {
            Iterator it = this.marrClassQuestions.iterator();
            while (it.hasNext()) {
                if (((StudentClassQuestion) it.next()).szGUID.equalsIgnoreCase(szGUID)) {
                    return false;
                }
            }
            StudentClassQuestion newQuestion = new StudentClassQuestion();
            newQuestion.szGUID = szGUID;
            newQuestion.nType = nType;
            newQuestion.nQuestionStartTime = System.currentTimeMillis();
            newQuestion.szCorrectAnswer = szCorrectAnswer;
            newQuestion.szIMMessage = szIMMessage;
            newQuestion.szFrom = szFrom;
            newQuestion.szOptions = szOptions;
            this.marrClassQuestions.add(newQuestion);
        }
    }

    public boolean rateStudentAnswer(String szQuestionGroupGUID, String szAnswerKey, String szFrom, boolean bRate) {
        for (int i = 0; i < this.marrClassQuestions.size(); i++) {
            StudentClassQuestion oneQuestion = (StudentClassQuestion) this.marrClassQuestions.get(i);
            if (oneQuestion.szGUID.equalsIgnoreCase(szQuestionGroupGUID)) {
                Iterator it = oneQuestion.arrStudentAnswers.iterator();
                while (it.hasNext()) {
                    StudentAnswer oneAnswer = (StudentAnswer) it.next();
                    if (oneAnswer.szAnswerOrPictureKey.equalsIgnoreCase(szAnswerKey)) {
                        if (!bRate) {
                            for (int k = 0; k < oneAnswer.arrVoteUsers.size(); k++) {
                                if (((String) oneAnswer.arrVoteUsers.get(k)).equalsIgnoreCase(szFrom)) {
                                    oneAnswer.arrVoteUsers.remove(k);
                                    return true;
                                }
                            }
                            continue;
                        } else if (!Utilities.isInArray(oneAnswer.arrVoteUsers, szFrom)) {
                            oneAnswer.arrVoteUsers.add(szFrom);
                            return true;
                        }
                    }
                }
                continue;
            }
        }
        return false;
    }

    public void fireUnfinishedQuestion() {
        int i = 0;
        while (i < this.marrClassQuestions.size()) {
            if (((StudentClassQuestion) this.marrClassQuestions.get(i)).bUserFinish) {
                i++;
            } else {
                this.mnCurrentIndex = i;
                if (this.mCallBack != null) {
                    this.mCallBack.onSwitchToQuestion(getCurrentQuestionGroupGUID(), null);
                }
                updateStudentAnswerImageService();
                return;
            }
        }
    }

    public boolean setCurrentIndex(int nIndex, boolean bFireSwitchEvent) {
        if (nIndex < 0 || nIndex >= this.marrClassQuestions.size()) {
            return false;
        }
        this.mnCurrentIndex = nIndex;
        if (bFireSwitchEvent && this.mCallBack != null) {
            this.mCallBack.onSwitchToQuestion(getCurrentQuestionGroupGUID(), null);
        }
        return true;
    }

    public StudentClassQuestion getQuestion(int nIndex) {
        if (nIndex < 0 || nIndex >= this.marrClassQuestions.size()) {
            return null;
        }
        return (StudentClassQuestion) this.marrClassQuestions.get(nIndex);
    }

    private void updateStudentAnswerImageService() {
        if (StudentAnswerImageService.getInstance() != null) {
            StudentAnswerImageService.getInstance().setStudentAnswerData(((StudentClassQuestion) this.marrClassQuestions.get(this.mnCurrentIndex)).arrStudentAnswers);
        }
    }

    public boolean isLastQuestion(String szQuestionGroupGUID) {
        if (this.marrClassQuestions.size() == 0) {
            return true;
        }
        if (((StudentClassQuestion) this.marrClassQuestions.get(this.marrClassQuestions.size() - 1)).szGUID.equalsIgnoreCase(szQuestionGroupGUID)) {
            return true;
        }
        return false;
    }

    public boolean hasUnfinishQuestions() {
        for (int i = 0; i < this.marrClassQuestions.size(); i++) {
            if (!((StudentClassQuestion) this.marrClassQuestions.get(i)).bUserFinish) {
                return true;
            }
        }
        return false;
    }

    public void finishCurrentQuestion(String szQuestionGroupGUID, String szAnswer) {
        Iterator it = this.marrClassQuestions.iterator();
        while (it.hasNext()) {
            StudentClassQuestion oneQuestion = (StudentClassQuestion) it.next();
            if (oneQuestion.szGUID.equalsIgnoreCase(szQuestionGroupGUID)) {
                oneQuestion.bUserFinish = true;
                oneQuestion.szCurrentAnswer = szAnswer;
                break;
            }
        }
        boolean bNoMoreQuestions = true;
        int i = 0;
        while (i < this.marrClassQuestions.size()) {
            if (((StudentClassQuestion) this.marrClassQuestions.get(i)).bUserFinish) {
                i++;
            } else {
                this.mnCurrentIndex = i;
                bNoMoreQuestions = false;
                if (this.mCallBack != null) {
                    this.mCallBack.onSwitchToQuestion(getCurrentQuestionGroupGUID(), null);
                }
                updateStudentAnswerImageService();
                if (bNoMoreQuestions && this.mCallBack != null) {
                    this.mCallBack.onNoMoreQuestionNeedToAnswer();
                    return;
                }
            }
        }
        if (bNoMoreQuestions) {
        }
    }

    public String getCurrentQuestionGroupGUID() {
        if (this.mnCurrentIndex < 0 || this.mnCurrentIndex >= this.marrClassQuestions.size()) {
            return "";
        }
        return ((StudentClassQuestion) this.marrClassQuestions.get(this.mnCurrentIndex)).szGUID;
    }

    public String getQuestionGroupIMMessage(String szGUID) {
        Iterator it = this.marrClassQuestions.iterator();
        while (it.hasNext()) {
            StudentClassQuestion oneQuestion = (StudentClassQuestion) it.next();
            if (oneQuestion.szGUID.equalsIgnoreCase(szGUID)) {
                return oneQuestion.szIMMessage;
            }
        }
        return null;
    }

    public String getQuestionGroupFrom(String szGUID) {
        Iterator it = this.marrClassQuestions.iterator();
        while (it.hasNext()) {
            StudentClassQuestion oneQuestion = (StudentClassQuestion) it.next();
            if (oneQuestion.szGUID.equalsIgnoreCase(szGUID)) {
                return oneQuestion.szFrom;
            }
        }
        return null;
    }

    public boolean addStudentAnswer(String szFrom, String szStudentAnswer, boolean bAutoSubmit, String szRealName) {
        int nPos = szStudentAnswer.indexOf(":");
        if (nPos == -1) {
            return false;
        }
        String szGroupGUID = szStudentAnswer.substring(0, nPos);
        szStudentAnswer = szStudentAnswer.substring(nPos + 1);
        Iterator it = this.marrClassQuestions.iterator();
        while (it.hasNext()) {
            StudentClassQuestion oneQuestion = (StudentClassQuestion) it.next();
            if (oneQuestion.szGUID.equalsIgnoreCase(szGroupGUID)) {
                boolean bFound = false;
                StudentAnswer newAnswer = new StudentAnswer();
                Iterator it2 = oneQuestion.arrStudentAnswers.iterator();
                while (it2.hasNext()) {
                    StudentAnswer oneAnswer = (StudentAnswer) it2.next();
                    if (oneAnswer.szStudentID.equalsIgnoreCase(szFrom)) {
                        if (szStudentAnswer.equalsIgnoreCase(oneAnswer.szAnswerOrPictureKey)) {
                            oneAnswer.szAnswerOrPictureKey = szStudentAnswer;
                            oneAnswer.nTimeInMS = (int) (System.currentTimeMillis() - oneQuestion.nQuestionStartTime);
                            if (!oneQuestion.szCorrectAnswer.isEmpty()) {
                                if (oneQuestion.szCorrectAnswer.equalsIgnoreCase(newAnswer.szAnswerOrPictureKey)) {
                                    newAnswer.bCorrect = true;
                                } else {
                                    newAnswer.bWrong = true;
                                }
                            }
                        }
                        bFound = true;
                        sendUserAnswersToPC(szGroupGUID);
                        if (!bFound) {
                            newAnswer.szAnswerOrPictureKey = szStudentAnswer;
                            newAnswer.nTimeInMS = (int) (System.currentTimeMillis() - oneQuestion.nQuestionStartTime);
                            newAnswer.bAutoSubmit = bAutoSubmit;
                            if (newAnswer.szAnswerOrPictureKey.length() < Utilities.createGUID().length()) {
                                if (newAnswer.szAnswerOrPictureKey.toUpperCase().contentEquals(newAnswer.szAnswerOrPictureKey)) {
                                    newAnswer.bIsHandWrite = true;
                                } else {
                                    newAnswer.bIsHandWrite = false;
                                }
                            } else if (!oneQuestion.szCorrectAnswer.isEmpty()) {
                                if (oneQuestion.szCorrectAnswer.equalsIgnoreCase(newAnswer.szAnswerOrPictureKey)) {
                                    newAnswer.bWrong = true;
                                } else {
                                    newAnswer.bCorrect = true;
                                }
                            }
                            newAnswer.szStudentID = szFrom;
                            newAnswer.szStudentJID = "myipad_" + szFrom;
                            newAnswer.szStudentName = szRealName;
                            generateStudentAnswerImageURL(newAnswer);
                            oneQuestion.arrStudentAnswers.add(newAnswer);
                            sendUserAnswersToPC(szGroupGUID);
                        }
                    }
                }
                if (!bFound) {
                    newAnswer.szAnswerOrPictureKey = szStudentAnswer;
                    newAnswer.nTimeInMS = (int) (System.currentTimeMillis() - oneQuestion.nQuestionStartTime);
                    newAnswer.bAutoSubmit = bAutoSubmit;
                    if (newAnswer.szAnswerOrPictureKey.length() < Utilities.createGUID().length()) {
                        if (oneQuestion.szCorrectAnswer.isEmpty()) {
                            if (oneQuestion.szCorrectAnswer.equalsIgnoreCase(newAnswer.szAnswerOrPictureKey)) {
                                newAnswer.bWrong = true;
                            } else {
                                newAnswer.bCorrect = true;
                            }
                        }
                    } else if (newAnswer.szAnswerOrPictureKey.toUpperCase().contentEquals(newAnswer.szAnswerOrPictureKey)) {
                        newAnswer.bIsHandWrite = true;
                    } else {
                        newAnswer.bIsHandWrite = false;
                    }
                    newAnswer.szStudentID = szFrom;
                    newAnswer.szStudentJID = "myipad_" + szFrom;
                    newAnswer.szStudentName = szRealName;
                    generateStudentAnswerImageURL(newAnswer);
                    oneQuestion.arrStudentAnswers.add(newAnswer);
                    sendUserAnswersToPC(szGroupGUID);
                }
            }
        }
        return true;
    }

    public int getSize() {
        return this.marrClassQuestions.size();
    }

    public void updateSideMenu(Menu subMenu) {
        subMenu.clear();
        for (int i = 0; i < this.marrClassQuestions.size(); i++) {
            subMenu.add(0, i, 0, "第" + String.valueOf(i + 1) + "题");
        }
    }

    public void refreshCurrentQuestion() {
        if (this.mnCurrentIndex >= 0 && this.mnCurrentIndex < this.marrClassQuestions.size()) {
            StudentClassQuestion classQuestion = (StudentClassQuestion) this.marrClassQuestions.get(this.mnCurrentIndex);
            if (this.mCallBack != null) {
                this.mCallBack.onSwitchToQuestion(classQuestion.szGUID, classQuestion.arrStudentAnswers);
            }
            updateStudentAnswerImageService();
        }
    }

    public void refreshCurrentQuestionByReloadingURL() {
        if (this.mnCurrentIndex >= 0 && this.mnCurrentIndex < this.marrClassQuestions.size()) {
            StudentClassQuestion classQuestion = (StudentClassQuestion) this.marrClassQuestions.get(this.mnCurrentIndex);
            if (classQuestion.szUserAnswerURL != null) {
                loadStudentAnswerFromURL(classQuestion.szUserAnswerURL);
            }
        }
    }

    public void processMenuClick(int nMenuID) {
        if (nMenuID >= 0 && nMenuID < this.marrClassQuestions.size()) {
            this.mnCurrentIndex = nMenuID;
            StudentClassQuestion classQuestion = (StudentClassQuestion) this.marrClassQuestions.get(this.mnCurrentIndex);
            if (this.mCallBack != null) {
                this.mCallBack.onSwitchToQuestion(classQuestion.szGUID, classQuestion.arrStudentAnswers);
            }
            updateStudentAnswerImageService();
        }
    }

    public void switchToLastQuestion() {
        if (this.marrClassQuestions.size() > 0) {
            this.mnCurrentIndex = this.marrClassQuestions.size() - 1;
            StudentClassQuestion classQuestion = (StudentClassQuestion) this.marrClassQuestions.get(this.mnCurrentIndex);
            if (this.mCallBack != null) {
                this.mCallBack.onSwitchToQuestion(classQuestion.szGUID, classQuestion.arrStudentAnswers);
            }
            updateStudentAnswerImageService();
        }
    }

    public int getCurrentQuestionIndex() {
        return this.mnCurrentIndex;
    }

    private void generateStudentAnswerImageURL(final StudentAnswer StudentAnswer) {
        final Context context = MyiBaseApplication.getBaseAppContext();
        if (StudentAnswer.bIsHandWrite) {
            StudentAnswer.szAnswerImageURLRemote = MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/GetTemporaryStorage?filename=" + StudentAnswer.szAnswerOrPictureKey;
            StudentAnswer.szAnswerImageURLLocal = "http://" + this.mszLocalPCAddress + ":50007/" + StudentAnswer.szAnswerOrPictureKey;
            Picasso.with(context).load(StudentAnswer.szAnswerImageURLRemote).tag(StudentAnswer.szAnswerImageURLRemote).fetch(new Callback() {
                public void onError() {
                }

                public void onSuccess() {
                    if (!StudentAnswer.szAnswerImageURLLocal.isEmpty()) {
                        Log.d(ClassMultiQuestions.this.TAG, "Load image from server success. Cancel secondary url.");
                        Picasso.with(context).cancelTag(StudentAnswer.szAnswerImageURLLocal);
                    }
                    StudentAnswer.szFinalImageURL = StudentAnswer.szAnswerImageURLRemote;
                }
            });
            if (!StudentAnswer.szAnswerImageURLLocal.isEmpty()) {
                Picasso.with(context).load(StudentAnswer.szAnswerImageURLLocal).tag(StudentAnswer.szAnswerImageURLLocal).fetch(new Callback() {
                    public void onError() {
                    }

                    public void onSuccess() {
                        Log.d(ClassMultiQuestions.this.TAG, "Load image from Myidesktop success. Cancel main url.");
                        Picasso.with(context).cancelTag(StudentAnswer.szAnswerImageURLRemote);
                        StudentAnswer.szFinalImageURL = StudentAnswer.szAnswerImageURLLocal;
                    }
                });
            }
        }
    }

    public void clear() {
        this.marrClassQuestions.clear();
    }

    public void setLocalPCAddress(String szAddress) {
        this.mszLocalPCAddress = szAddress;
    }

    public void setCallBack(MultiQuestionCallBack callBack) {
        this.mCallBack = callBack;
    }

    public void loadStudentAnswerFromURL(final String szURL) {
        int nPos = szURL.indexOf("StudentAnswer_");
        String szGroupGUID = "";
        if (nPos != -1) {
            szGroupGUID = szURL.substring(nPos + 14, szURL.lastIndexOf("."));
            Log.d(this.TAG, "Found question group is " + szGroupGUID);
            for (int i = 0; i < this.marrClassQuestions.size(); i++) {
                StudentClassQuestion oneQuestion = (StudentClassQuestion) this.marrClassQuestions.get(i);
                if (oneQuestion.szGUID.equalsIgnoreCase(szGroupGUID)) {
                    oneQuestion.szUserAnswerURL = szURL;
                    break;
                }
            }
        }
        if (!szGroupGUID.isEmpty()) {
            HttpItemObject httpItem = new HttpItemObject(szURL, null);
            final String szQuestionGroupGUID = szGroupGUID;
            httpItem.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    Log.d(ClassMultiQuestions.this.TAG, "DataLoaded. Data is " + ItemObject.readTextData());
                    ClassMultiQuestions.this.parserAnswerJson(ItemObject.readTextData(), szQuestionGroupGUID, szURL);
                }
            });
            Log.d(this.TAG, "prepare to load from " + szURL);
            httpItem.setRetryCount(3);
            httpItem.setTimeout(3000);
            httpItem.setSaveToFile(false);
            httpItem.setReadOperation(true);
            httpItem.setAlwaysActiveCallbacks(true);
            VirtualNetworkObject.addToQueue(httpItem);
        }
    }

    public void loadStudentAnswerFromURL(final String szURL, final OnLoadListCallBack callBack) {
        int nPos = szURL.indexOf("StudentAnswer_");
        String szGroupGUID = "";
        if (nPos != -1) {
            szGroupGUID = szURL.substring(nPos + 14, szURL.lastIndexOf("."));
            Log.d(this.TAG, "Found question group is " + szGroupGUID);
        }
        if (!szGroupGUID.isEmpty()) {
            HttpItemObject httpItem = new HttpItemObject(szURL, null);
            final String szQuestionGroupGUID = szGroupGUID;
            httpItem.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    Log.d(ClassMultiQuestions.this.TAG, "DataLoaded. Data is " + ItemObject.readTextData());
                    ClassMultiQuestions.this.parserAnswerJson(ItemObject.readTextData(), szQuestionGroupGUID, szURL);
                    if (callBack != null) {
                        callBack.onListLoadSuccess(szQuestionGroupGUID);
                    }
                }
            });
            httpItem.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                    if (callBack != null) {
                        callBack.onListLoadSuccess(szQuestionGroupGUID);
                    }
                }
            });
            Log.d(this.TAG, "prepare to load from " + szURL);
            httpItem.setRetryCount(3);
            httpItem.setTimeout(3000);
            httpItem.setSaveToFile(false);
            httpItem.setReadOperation(true);
            httpItem.setAlwaysActiveCallbacks(true);
            VirtualNetworkObject.addToQueue(httpItem);
        }
    }

    public boolean parserAnswerJson(String szJsonData, String szQuestionGroupGUID, String szURL) {
        int i;
        boolean bFound = false;
        StudentClassQuestion matchQuestion = null;
        int nFoundIndex = -1;
        for (i = 0; i < this.marrClassQuestions.size(); i++) {
            StudentClassQuestion oneQuestion = (StudentClassQuestion) this.marrClassQuestions.get(i);
            if (oneQuestion.szGUID.equalsIgnoreCase(szQuestionGroupGUID)) {
                oneQuestion.szUserAnswerURL = szURL;
                bFound = true;
                nFoundIndex = i;
                matchQuestion = oneQuestion;
                break;
            }
        }
        if (!bFound) {
            return false;
        }
        try {
            JSONArray jsonArray = new JSONArray(szJsonData);
            matchQuestion.arrStudentAnswers.clear();
            for (i = 0; i < jsonArray.length(); i++) {
                StudentAnswer oneAnswer = new StudentAnswer();
                JSONObject oneObject = jsonArray.getJSONObject(i);
                oneAnswer.szStudentName = oneObject.getString("studentName");
                oneAnswer.szStudentID = oneObject.getString("studentID");
                oneAnswer.szStudentJID = oneObject.getString("studentJID");
                oneAnswer.szAnswerOrPictureKey = oneObject.getString("answer");
                oneAnswer.bAutoSubmit = oneObject.getBoolean("isAutosubmit");
                oneAnswer.nTimeInMS = oneObject.getInt("timeInMS");
                oneAnswer.bCorrect = oneObject.getBoolean("isCorrect");
                oneAnswer.bIsHandWrite = oneObject.getBoolean("isHandWrite");
                matchQuestion.arrStudentAnswers.add(oneAnswer);
            }
            if (this.mnCurrentIndex == nFoundIndex) {
                updateStudentAnswerImageService();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    public String getQuestionGUID(int nIndex) {
        return ((StudentClassQuestion) this.marrClassQuestions.get(nIndex)).szGUID;
    }

    public void sendUserAnswersToPC(final String szQuestionGroupGUID) {
        final Context context = MyiBaseApplication.getBaseAppContext();
        String szHostPCMjpegServer = this.mszLocalPCAddress;
        if (szHostPCMjpegServer != null && !szHostPCMjpegServer.isEmpty()) {
            JSONArray jsonArray = new JSONArray();
            boolean bFound = false;
            try {
                StudentClassQuestion oneQuestion;
                Iterator it = this.marrClassQuestions.iterator();
                do {
                    if (!it.hasNext()) {
                        break;
                    }
                    oneQuestion = (StudentClassQuestion) it.next();
                } while (!oneQuestion.szGUID.equalsIgnoreCase(szQuestionGroupGUID));
                bFound = true;
                for (int i = 0; i < oneQuestion.arrStudentAnswers.size(); i++) {
                    StudentAnswer oneAnswer = (StudentAnswer) oneQuestion.arrStudentAnswers.get(i);
                    JSONObject oneObject = new JSONObject();
                    oneObject.put("studentName", oneAnswer.szStudentName);
                    oneObject.put("studentID", oneAnswer.szStudentID);
                    oneObject.put("studentJID", oneAnswer.szStudentJID);
                    oneObject.put("answer", oneAnswer.szAnswerOrPictureKey);
                    oneObject.put("isAutosubmit", oneAnswer.bAutoSubmit);
                    oneObject.put("timeInMS", oneAnswer.nTimeInMS);
                    oneObject.put("isCorrect", oneAnswer.bCorrect);
                    oneObject.put("isHandWrite", oneAnswer.bIsHandWrite);
                    jsonArray.put(oneObject);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (bFound) {
                final String szURL = "http://" + szHostPCMjpegServer + ":50007/StudentAnswer_" + szQuestionGroupGUID + CrasheyeFileFilter.POSTFIX;
                PutTemporaryStorageItemObject CallItem = new PutTemporaryStorageItemObject(szURL, null);
                CallItem.setSuccessListener(new OnSuccessListener() {
                    public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                        if (ClassMultiQuestions.this.mCallBack != null) {
                            ClassMultiQuestions.this.mCallBack.onSuccessUploadStudentAnswerJson(szQuestionGroupGUID, szURL);
                        }
                    }
                });
                CallItem.setFailureListener(new OnFailureListener() {
                    public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                        Context context = context;
                        final Context context2 = context;
                        Utilities.runOnUIThread(context, new Runnable() {
                            public void run() {
                                Toast.makeText(context2, "无法发送学生作答列表数据到睿易通所在的PC上", 0).show();
                            }
                        });
                    }
                });
                CallItem.writeTextData(jsonArray.toString());
                CallItem.setAlwaysActiveCallbacks(true);
                CallItem.setRetryCount(5);
                CallItem.setTimeout(4000);
                VirtualNetworkObject.executeNow(CallItem);
            }
        }
    }

    public void finishAll() {
        Iterator it = this.marrClassQuestions.iterator();
        while (it.hasNext()) {
            ((StudentClassQuestion) it.next()).bUserFinish = true;
        }
        if (this.mCallBack != null) {
            this.mCallBack.onNoMoreQuestionNeedToAnswer();
        }
    }

    public boolean isQuestionAnswered(String szQuestionGroupGUID) {
        Iterator it = this.marrClassQuestions.iterator();
        while (it.hasNext()) {
            StudentClassQuestion oneQuestion = (StudentClassQuestion) it.next();
            if (oneQuestion.szGUID.equalsIgnoreCase(szQuestionGroupGUID)) {
                return oneQuestion.bUserFinish;
            }
        }
        return false;
    }

    public String getQuestionCorrectAnswer(String szQuestionGroupGUID) {
        Iterator it = this.marrClassQuestions.iterator();
        while (it.hasNext()) {
            StudentClassQuestion oneQuestion = (StudentClassQuestion) it.next();
            if (oneQuestion.szGUID.equalsIgnoreCase(szQuestionGroupGUID)) {
                return oneQuestion.szCorrectAnswer;
            }
        }
        return "";
    }

    public String getQuestionAnswer(String szQuestionGroupGUID) {
        Iterator it = this.marrClassQuestions.iterator();
        while (it.hasNext()) {
            StudentClassQuestion oneQuestion = (StudentClassQuestion) it.next();
            if (oneQuestion.szGUID.equalsIgnoreCase(szQuestionGroupGUID)) {
                Iterator it2 = oneQuestion.arrStudentAnswers.iterator();
                while (it2.hasNext()) {
                    StudentAnswer oneAnswer = (StudentAnswer) it2.next();
                    if (oneAnswer.szStudentID.equalsIgnoreCase(MyiBaseApplication.getCommonVariables().UserInfo.szUserName)) {
                        return oneAnswer.szAnswerOrPictureKey;
                    }
                }
                continue;
            }
        }
        return "";
    }

    public String getQuestionIndex(String szGUID) {
        if (szGUID.indexOf("=") != -1) {
            return "第" + szGUID.substring(szGUID.indexOf("=") + 1) + "题";
        }
        return "";
    }

    public void showQuestionSelector(Context context) {
        ArrayList<String> arrTitles = new ArrayList();
        if (StudentAnswerImageService.getInstance() != null) {
            StudentAnswerImageService.getInstance().hide();
        }
        for (int i = 0; i < this.marrClassQuestions.size(); i++) {
            String szTitle = "第" + String.valueOf(i + 1) + "题";
            String szGUID = ((StudentClassQuestion) this.marrClassQuestions.get(i)).szGUID;
            if (szGUID.indexOf("=") != -1) {
                szTitle = "第" + szGUID.substring(szGUID.indexOf("=") + 1) + "题";
            }
            StudentClassQuestion oneQuestion = (StudentClassQuestion) this.marrClassQuestions.get(i);
            arrTitles.add(szTitle);
        }
        new Builder(new ContextThemeWrapper(context, 16974130)).setSingleChoiceItems((String[]) arrTitles.toArray(new String[0]), this.mnCurrentIndex, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ClassMultiQuestions.this.mnCurrentIndex = which;
                if (ClassMultiQuestions.this.mCallBack != null) {
                    StudentClassQuestion oneQuestion = (StudentClassQuestion) ClassMultiQuestions.this.marrClassQuestions.get(which);
                    if (oneQuestion.szUserAnswerURL != null) {
                        ClassMultiQuestions.this.loadStudentAnswerFromURL(oneQuestion.szUserAnswerURL, new OnLoadListCallBack() {
                            public void onListLoadSuccess(String szQuestionGroupGUID) {
                                ClassMultiQuestions.this.mCallBack.onSwitchToQuestion(szQuestionGroupGUID, null);
                            }

                            public void onListLoadFailed(String szQuestionGroupGUID) {
                                ClassMultiQuestions.this.mCallBack.onSwitchToQuestion(szQuestionGroupGUID, null);
                            }
                        });
                    } else {
                        ClassMultiQuestions.this.mCallBack.onSwitchToQuestion(ClassMultiQuestions.this.getCurrentQuestionGroupGUID(), null);
                    }
                }
                ClassMultiQuestions.this.updateStudentAnswerImageService();
                if (StudentAnswerImageService.getInstance() != null) {
                    StudentAnswerImageService.getInstance().show();
                }
            }
        }).setTitle("选择试题").create().show();
    }
}
