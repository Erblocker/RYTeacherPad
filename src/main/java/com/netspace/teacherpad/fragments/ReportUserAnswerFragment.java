package com.netspace.teacherpad.fragments;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import com.netspace.library.activity.FingerDrawActivity;
import com.netspace.library.activity.FingerDrawActivity.FingerDrawCallbackInterface;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.service.StudentAnswerImageService;
import com.netspace.library.struct.StudentAnswer;
import com.netspace.library.struct.UserClassInfo;
import com.netspace.library.struct.UserInfo;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.PutTemporaryStorageItemObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.ScreenDisplayActivity;
import com.netspace.teacherpad.TeacherPadApplication;
import com.netspace.teacherpad.adapter.StudentAnswerAdapter;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class ReportUserAnswerFragment extends Fragment implements FingerDrawCallbackInterface {
    private final int INTENT_ID_SENDBACK = 1;
    private StudentAnswerAdapter mAdapter;
    private StudentAnswer mCurrentStudentAnswer;
    private Target mImageClickTarget = new Target() {
        public void onBitmapFailed(Drawable arg0) {
            Utilities.showAlertMessage(ReportUserAnswerFragment.this.getActivity(), "获取用户作答图片失败", "获取图片数据失败。");
        }

        public void onBitmapLoaded(Bitmap arg0, LoadedFrom arg1) {
            Utilities.saveBitmapToJpeg(new StringBuilder(String.valueOf(ReportUserAnswerFragment.this.getContext().getExternalCacheDir().getAbsolutePath())).append("/").append(ReportUserAnswerFragment.this.mCurrentStudentAnswer.szAnswerOrPictureKey).append(".jpg").toString(), arg0);
            Intent DrawActivity = new Intent(ReportUserAnswerFragment.this.getActivity(), FingerDrawActivity.class);
            FingerDrawActivity.SetCallbackInterface(ReportUserAnswerFragment.this);
            DrawActivity.putExtra("imageKey", ReportUserAnswerFragment.this.mCurrentStudentAnswer.szAnswerOrPictureKey);
            DrawActivity.putExtra("imageWidth", -1);
            DrawActivity.putExtra("imageHeight", -1);
            DrawActivity.putExtra("allowProject", true);
            DrawActivity.putExtra("allowBroadcast", true);
            DrawActivity.putExtra("allowUpload", true);
            DrawActivity.putExtra("uploadName", ReportUserAnswerFragment.this.mCurrentStudentAnswer.szAnswerOrPictureKey);
            DrawActivity.putExtra("enableBackButton", true);
            DrawActivity.putExtra("displayText", ReportUserAnswerFragment.this.mCurrentStudentAnswer.szStudentName);
            ReportUserAnswerFragment.this.startActivity(DrawActivity);
            if (!TeacherPadApplication.szLastStudentAnswerJSON.isEmpty()) {
                Intent show = new Intent(ReportUserAnswerFragment.this.getContext(), StudentAnswerImageService.class);
                show.putExtra("operation", 100);
                show.putExtra(StudentAnswerImageService.LISTURL, TeacherPadApplication.szLastStudentAnswerJSON);
                ReportUserAnswerFragment.this.getContext().startService(show);
            }
        }

        public void onPrepareLoad(Drawable arg0) {
        }
    };
    private OnClickListener mOnImageViewClickListener = new OnClickListener() {
        private int mnSelectedIndex = 0;

        public void onClick(View v) {
            StudentAnswer StudentAnswer = (StudentAnswer) v.getTag();
            ReportUserAnswerFragment.this.mCurrentStudentAnswer = StudentAnswer;
            if (v.getId() == R.id.buttonSendBack) {
                if (TeacherPadApplication.IMThread.SendProjectRequest(StudentAnswer.szStudentID, StudentAnswer.szAnswerOrPictureKey) && TeacherPadApplication.marrMonitors.size() < 2) {
                    Intent intent = new Intent(ReportUserAnswerFragment.this.getActivity(), ScreenDisplayActivity.class);
                    intent.putExtra("MJpegServer", TeacherPadApplication.getMJpegURL());
                    ReportUserAnswerFragment.this.startActivityForResult(intent, 1);
                }
            } else if (v.getId() == R.id.buttonVoteCount) {
                if (StudentAnswer.arrVoteUsers.size() > 0) {
                    String[] arrStudentNames2 = new String[StudentAnswer.arrVoteUsers.size()];
                    for (int i = 0; i < StudentAnswer.arrVoteUsers.size(); i++) {
                        arrStudentNames2[i] = (String) TeacherPadApplication.mapStudentName.get(StudentAnswer.arrVoteUsers.get(i));
                    }
                    this.mnSelectedIndex = 0;
                    new Builder(ReportUserAnswerFragment.this.getActivity()).setSingleChoiceItems(arrStudentNames2, 0, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            AnonymousClass3.this.mnSelectedIndex = which;
                        }
                    }).setPositiveButton("发还给这个学生", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (TeacherPadApplication.IMThread.SendProjectRequest((String) ReportUserAnswerFragment.this.mCurrentStudentAnswer.arrVoteUsers.get(AnonymousClass3.this.mnSelectedIndex), ReportUserAnswerFragment.this.mCurrentStudentAnswer.szAnswerOrPictureKey) && TeacherPadApplication.marrMonitors.size() < 2) {
                                Intent intent = new Intent(ReportUserAnswerFragment.this.getActivity(), ScreenDisplayActivity.class);
                                intent.putExtra("MJpegServer", TeacherPadApplication.getMJpegURL());
                                ReportUserAnswerFragment.this.startActivityForResult(intent, 1);
                            }
                        }
                    }).setNeutralButton("打开这个学生的作答", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String szSelectdUserName = (String) ReportUserAnswerFragment.this.mCurrentStudentAnswer.arrVoteUsers.get(AnonymousClass3.this.mnSelectedIndex);
                            Iterator it = TeacherPadApplication.marrStudentAnswers.iterator();
                            while (it.hasNext()) {
                                StudentAnswer oneAnswer = (StudentAnswer) it.next();
                                if (oneAnswer.szStudentID.equalsIgnoreCase(szSelectdUserName) && oneAnswer.bIsHandWrite) {
                                    Picasso.with(ReportUserAnswerFragment.this.getActivity()).cancelRequest(ReportUserAnswerFragment.this.mImageClickTarget);
                                    Picasso.with(ReportUserAnswerFragment.this.getActivity()).load(MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/GetTemporaryStorage?filename=" + oneAnswer.szAnswerOrPictureKey).into(ReportUserAnswerFragment.this.mImageClickTarget);
                                }
                            }
                        }
                    }).setNegativeButton("取消", null).setTitle("点赞的学生").create().show();
                    return;
                }
                Utilities.showAlertMessage(ReportUserAnswerFragment.this.getActivity(), "点赞学生清单", "当前作答没有学生点赞");
            } else if (StudentAnswer.bIsHandWrite) {
                Picasso.with(ReportUserAnswerFragment.this.getActivity()).cancelRequest(ReportUserAnswerFragment.this.mImageClickTarget);
                Picasso.with(ReportUserAnswerFragment.this.getActivity()).load(MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/GetTemporaryStorage?filename=" + StudentAnswer.szAnswerOrPictureKey).into(ReportUserAnswerFragment.this.mImageClickTarget);
            }
        }
    };
    private OnLongClickListener mOnImageViewLongClickListener = new OnLongClickListener() {
        private int mnSelectedIndex = 0;

        public boolean onLongClick(View v) {
            final StudentAnswer StudentAnswer = (StudentAnswer) v.getTag();
            ReportUserAnswerFragment.this.mCurrentStudentAnswer = StudentAnswer;
            if (v.getId() != R.id.buttonSendBack) {
                return false;
            }
            ArrayList<String> arrStudentNames = new ArrayList();
            final ArrayList<String> arrStudentIDs = new ArrayList();
            this.mnSelectedIndex = 0;
            Iterator it = MyiBaseApplication.getCommonVariables().UserInfo.arrClasses.iterator();
            while (it.hasNext()) {
                UserClassInfo classInfo = (UserClassInfo) it.next();
                if (classInfo.szClassGUID.equalsIgnoreCase(TeacherPadApplication.szCurrentClassGUID)) {
                    it = classInfo.arrStudents.iterator();
                    while (it.hasNext()) {
                        UserInfo oneStudent = (UserInfo) it.next();
                        if (oneStudent.szUserName.equalsIgnoreCase(StudentAnswer.szStudentID)) {
                            arrStudentNames.add(0, oneStudent.szRealName + "(原作者)");
                            arrStudentIDs.add(0, oneStudent.szUserName);
                        } else {
                            if (Utilities.isInArray(StudentAnswer.arrVoteUsers, oneStudent.szUserName)) {
                                arrStudentNames.add(oneStudent.szRealName + "(已点赞)");
                            } else {
                                arrStudentNames.add(oneStudent.szRealName);
                            }
                            arrStudentIDs.add(oneStudent.szUserName);
                        }
                    }
                    new Builder(ReportUserAnswerFragment.this.getActivity()).setSingleChoiceItems((String[]) arrStudentNames.toArray(new String[arrStudentNames.size()]), 0, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            AnonymousClass2.this.mnSelectedIndex = which;
                        }
                    }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (TeacherPadApplication.IMThread.SendProjectRequest((String) arrStudentIDs.get(AnonymousClass2.this.mnSelectedIndex), StudentAnswer.szAnswerOrPictureKey)) {
                                Intent intent = new Intent(ReportUserAnswerFragment.this.getActivity(), ScreenDisplayActivity.class);
                                intent.putExtra("MJpegServer", TeacherPadApplication.getMJpegURL());
                                ReportUserAnswerFragment.this.startActivityForResult(intent, 1);
                            }
                        }
                    }).setNegativeButton("取消", null).setTitle("发还给哪个学生").create().show();
                    return true;
                }
            }
            new Builder(ReportUserAnswerFragment.this.getActivity()).setSingleChoiceItems((String[]) arrStudentNames.toArray(new String[arrStudentNames.size()]), 0, /* anonymous class already generated */).setPositiveButton("确定", /* anonymous class already generated */).setNegativeButton("取消", null).setTitle("发还给哪个学生").create().show();
            return true;
        }
    };
    private RecyclerView mRecycleView;
    private View mRootView;
    private TextView mTextViewMessage;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mRootView = inflater.inflate(R.layout.fragment_useranswer, null);
        this.mRecycleView = (RecyclerView) this.mRootView.findViewById(R.id.studentAnswerView);
        this.mRecycleView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        this.mRecycleView.setItemAnimator(new DefaultItemAnimator());
        this.mAdapter = new StudentAnswerAdapter(getActivity(), TeacherPadApplication.marrStudentAnswers);
        this.mAdapter.setOnClickListener(this.mOnImageViewClickListener);
        this.mAdapter.setOnLongClickListener(this.mOnImageViewLongClickListener);
        this.mRecycleView.setAdapter(this.mAdapter);
        this.mTextViewMessage = (TextView) this.mRootView.findViewById(R.id.textViewNoData);
        this.mTextViewMessage.setVisibility(4);
        if (this.mAdapter.getItemCount() == 0) {
            reportMessage("当前没有数据");
        }
        return this.mRootView;
    }

    private void reportMessage(String szMessage) {
        if (this.mTextViewMessage != null) {
            this.mTextViewMessage.setVisibility(0);
            this.mTextViewMessage.setText(szMessage);
        }
    }

    public void refresh() {
        if (this.mAdapter != null) {
            this.mAdapter.notifyDataSetChanged();
            if (this.mAdapter.getItemCount() == 0) {
                reportMessage("当前没有数据");
                return;
            } else {
                this.mTextViewMessage.setVisibility(8);
                return;
            }
        }
        reportMessage("当前没有数据");
    }

    public boolean HasMJpegClients() {
        boolean bHasClient = UI.ScreenJpegServer.HasClients();
        if (!bHasClient) {
            UI.ScreenJpegServer.CleanImageData();
        }
        return bHasClient;
    }

    public void OnUpdateMJpegImage(Bitmap bitmap, Activity Activity) {
        UI.ScreenJpegServer.PostNewImageData(bitmap);
    }

    public void OnOK(Bitmap bitmap, String szUploadName, Activity Activity) {
        final ProgressDialog Progress = new ProgressDialog((FingerDrawActivity) Activity);
        final boolean[] bFinishState = new boolean[2];
        Progress.setMessage("正在提交，请稍候...");
        Progress.setCancelable(false);
        Progress.setIndeterminate(true);
        Progress.setProgressStyle(0);
        Progress.show();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.JPEG, 75, baos);
            String szURL = "http://" + TeacherPadApplication.szPCIP + ":50007/" + szUploadName;
            final String szFileName = File.createTempFile("ClassUpload", null, getContext().getExternalCacheDir()).getAbsolutePath();
            Utilities.saveBitmapToJpeg(szFileName, bitmap);
            PutTemporaryStorageItemObject CallItem = new PutTemporaryStorageItemObject(szURL, null);
            CallItem.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    new File(szFileName).delete();
                    Utilities.showToastMessage(ReportUserAnswerFragment.this.getActivity(), "批改痕迹已成功保存到PC端。");
                    bFinishState[0] = true;
                    if (bFinishState[0] && bFinishState[1] && Progress.isShowing()) {
                        Progress.dismiss();
                    }
                }
            });
            CallItem.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                    bFinishState[0] = true;
                    if (bFinishState[0] && bFinishState[1] && Progress.isShowing()) {
                        Progress.dismiss();
                    }
                    new File(szFileName).delete();
                    Utilities.showAlertMessage(null, "批改保存失败", "保存批改痕迹到睿易通所在PC出现错误。");
                }
            });
            CallItem.setSourceFileName(szFileName);
            CallItem.setAlwaysActiveCallbacks(true);
            CallItem.setTimeout(2000);
            VirtualNetworkObject.executeNow(CallItem);
            String encodedImage = Base64.encodeToString(baos.toByteArray(), 0);
            byte[] byteArrayImage = null;
            WebServiceCallItemObject CallItem2 = new WebServiceCallItemObject("PutTemporaryStorage", null);
            CallItem2.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    Utilities.showToastMessage(ReportUserAnswerFragment.this.getActivity(), "批改痕迹已成功保存到服务器端。");
                    bFinishState[1] = true;
                    if (bFinishState[0] && bFinishState[1] && Progress.isShowing()) {
                        Progress.dismiss();
                    }
                }
            });
            CallItem2.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                    bFinishState[1] = true;
                    if (bFinishState[0] && bFinishState[1] && Progress.isShowing()) {
                        Progress.dismiss();
                    }
                    Utilities.showAlertMessage(null, "批改保存失败", "保存批改痕迹到服务器出现错误。");
                }
            });
            CallItem2.setParam("lpszBase64Data", encodedImage);
            CallItem2.setParam("szKey", szUploadName);
            CallItem2.setAlwaysActiveCallbacks(true);
            VirtualNetworkObject.addToQueue(CallItem2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void OnProject(Activity Activity) {
        TeacherPadApplication.projectToMonitor();
    }

    public void OnFingerDrawCreate(Activity Activity) {
    }

    public void OnBroadcast(Activity Activity) {
        if (Activity instanceof FingerDrawActivity) {
            try {
                FingerDrawActivity TempActivity = (FingerDrawActivity) Activity;
                Bitmap bitmap = TempActivity.getDrawView().saveToBitmap();
                final String szImageKey = Utilities.createGUID();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(CompressFormat.JPEG, 75, baos);
                String encodedImage = Base64.encodeToString(baos.toByteArray(), 0);
                bitmap.recycle();
                baos.close();
                byte[] byteArrayImage = null;
                WebServiceCallItemObject CallItem = new WebServiceCallItemObject("PutTemporaryStorage", TempActivity);
                CallItem.setSuccessListener(new OnSuccessListener() {
                    public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                        TeacherPadApplication.IMThread.SendToEveryone("HandWriteNoUpload " + szImageKey);
                        TeacherPadApplication.IMThread.SendMessage("ClearWhiteBoard", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
                    }
                });
                CallItem.setParam("lpszBase64Data", encodedImage);
                CallItem.setParam("szKey", szImageKey);
                VirtualNetworkObject.addToQueue(CallItem);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void OnDestroy(Activity Activity) {
        if (!TeacherPadApplication.szLastStudentAnswerJSON.isEmpty()) {
            MyiBaseApplication.getBaseAppContext().stopService(new Intent(MyiBaseApplication.getBaseAppContext(), StudentAnswerImageService.class));
        }
    }

    public void OnPenAction(String szAction, float fX, float fY, int nWidth, int nHeight, Activity Activity) {
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
