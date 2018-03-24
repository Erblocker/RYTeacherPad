package com.netspace.teacherpad.controls;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.servers.MJpegDisplayThread;
import com.netspace.library.servers.MJpegDisplayThread.MJpegCallInterface;
import com.netspace.library.servers.MJpegDisplayThread.MJpegFrameData;
import com.netspace.library.utilities.Utilities;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.TeacherPadApplication;
import com.netspace.teacherpad.adapter.SimplePagesAdapter;
import com.netspace.teacherpad.adapter.SimplePagesAdapter.ViewPagerReadyInterface;
import com.squareup.picasso.Picasso;
import io.vov.vitamio.ThumbnailUtils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class StudentNodeView extends LinearLayout implements OnClickListener, ViewPagerReadyInterface, MJpegCallInterface {
    public static final int PAD_STATUS_LOCKED = 3;
    public static final int PAD_STATUS_LOW_BATTERY = 4;
    public static final int PAD_STATUS_NO_RESPONSE = 5;
    public static final int PAD_STATUS_OFF = 0;
    public static final int PAD_STATUS_ON = 1;
    public static final int PAD_STATUS_RUNNING = 2;
    private SimplePagesAdapter m_Adapter;
    private Context m_Context;
    private StudentNodeViewMessageHandler m_Handler = new StudentNodeViewMessageHandler(this);
    private MJpegDisplayThread m_MJpegDisplayThread = null;
    private ImageView m_PadImage;
    private View m_PopupRootView;
    private TextView m_StudentInfo;
    private TextView m_StudentName;
    private boolean m_bDialogShow = false;
    private int m_nPadStatus = 0;
    private String m_szStudentAnswer = "";
    private String m_szStudentID = "";

    private static class StudentNodeViewMessageHandler extends Handler {
        private final WeakReference<StudentNodeView> m_ParentView;

        public StudentNodeViewMessageHandler(StudentNodeView View) {
            this.m_ParentView = new WeakReference(View);
        }

        public void handleMessage(Message msg) {
            StudentNodeView ParentView = (StudentNodeView) this.m_ParentView.get();
            if (msg.obj != null && (msg.obj instanceof MJpegFrameData)) {
                MJpegFrameData FrameData = msg.obj;
                Bitmap bm = FrameData.bm;
                if (ParentView.m_PopupRootView != null) {
                    ImageView MJpegImage = (ImageView) ParentView.m_PopupRootView.findViewById(R.id.imageView1);
                    if (MJpegImage != null) {
                        MJpegImage.setImageBitmap(Bitmap.createScaledBitmap(bm, ThumbnailUtils.TARGET_SIZE_MINI_THUMBNAIL_HEIGHT, 240, false));
                        Log.d("StudentNodeView", "Assigned new screen copy image.");
                    }
                }
                if (bm != null) {
                    bm.recycle();
                }
                FrameData.DisplayObject.setFrameHandled();
            }
        }
    }

    public StudentNodeView(Context context) {
        super(context);
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.studentnodeview, this);
        this.m_Context = context;
        InitView();
    }

    public StudentNodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.studentnodeview, this);
        this.m_Context = context;
        InitView();
    }

    protected void InitView() {
        this.m_StudentName = (TextView) findViewById(R.id.textViewStudentName);
        this.m_StudentInfo = (TextView) findViewById(R.id.textViewStudentInfo);
        this.m_PadImage = (ImageView) findViewById(R.id.imageViewPad);
        if (this.m_StudentName != null) {
            this.m_StudentName.setText("姓名");
        }
        if (this.m_StudentInfo != null) {
            this.m_StudentInfo.setText("描述");
        }
        if (!isInEditMode()) {
            this.m_PadImage.setOnClickListener(this);
        }
    }

    public String getStudentID() {
        return this.m_szStudentID;
    }

    public void setStudentID(String szStudentID) {
        this.m_szStudentID = szStudentID;
    }

    public void setStudentAnswer(String szAnswer, int nCorrect) {
        this.m_szStudentAnswer = szAnswer;
        if (this.m_szStudentAnswer.isEmpty()) {
            setPadStatus(this.m_nPadStatus);
            this.m_StudentInfo.setTextColor(-11119018);
            return;
        }
        this.m_StudentInfo.setText(this.m_szStudentAnswer);
        if (nCorrect > 0) {
            this.m_StudentInfo.setTextColor(-16728064);
        } else if (nCorrect < 0) {
            this.m_StudentInfo.setTextColor(-4194304);
        } else {
            this.m_StudentInfo.setTextColor(-11119018);
        }
    }

    public String getStudentAnswer() {
        return this.m_szStudentAnswer;
    }

    public void setStudentName(String szName) {
        this.m_StudentName.setText(szName);
    }

    public void setStudentInfo(String szInfo) {
        if (this.m_szStudentAnswer == null || this.m_szStudentAnswer.isEmpty()) {
            this.m_StudentInfo.setText(szInfo);
        }
    }

    public void setGroup(int nGroupID) {
        setBackgroundColor(this.m_Context.getResources().getColor(R.color.group1 + (nGroupID % 9)));
    }

    public void setPadStatus(int nStatus) {
        String szStatusText = "";
        if (nStatus != this.m_nPadStatus || TeacherPadApplication.szActiveAudioStudentID.equalsIgnoreCase(this.m_szStudentID)) {
            this.m_nPadStatus = nStatus;
            if (this.m_nPadStatus == 0) {
                this.m_PadImage.setImageResource(R.drawable.ic_pad);
                szStatusText = "关屏";
            } else if (this.m_nPadStatus == 1) {
                this.m_PadImage.setImageResource(R.drawable.ic_pad_on);
                szStatusText = "开机";
            } else if (this.m_nPadStatus == 2) {
                this.m_PadImage.setImageResource(R.drawable.ic_pad_on2);
                szStatusText = "正常运行";
            } else if (this.m_nPadStatus == 3) {
                this.m_PadImage.setImageResource(R.drawable.ic_pad_lock);
                szStatusText = "已锁屏";
            } else if (this.m_nPadStatus == 4) {
                this.m_PadImage.setImageResource(R.drawable.ic_pad_low_battery);
                szStatusText = "将要没电";
            } else if (this.m_nPadStatus == 5) {
                this.m_PadImage.setImageResource(R.drawable.ic_pad_offline);
                szStatusText = "无应答";
            }
            if (this.m_szStudentAnswer == null || this.m_szStudentAnswer.isEmpty()) {
                this.m_StudentInfo.setText(szStatusText);
            }
            if (TeacherPadApplication.szActiveAudioStudentID.equalsIgnoreCase(this.m_szStudentID)) {
                this.m_PadImage.setImageResource(R.drawable.ic_pad_voice);
                this.m_nPadStatus = -1;
            }
        }
    }

    public void onClick(View v) {
        if (v.getId() == this.m_PadImage.getId() && !this.m_bDialogShow) {
            View RootView = ((LayoutInflater) this.m_Context.getSystemService("layout_inflater")).inflate(R.layout.popup_studentnode, null, false);
            boolean bHasAnswerPage = false;
            this.m_PopupRootView = RootView;
            if (TeacherPadApplication.mapStudentsQuestionAnswers.containsKey(this.m_szStudentID)) {
                bHasAnswerPage = true;
            }
            ArrayList<Integer> arrPages = new ArrayList();
            arrPages.add(Integer.valueOf(R.layout.page_monitor));
            if (bHasAnswerPage) {
                arrPages.add(Integer.valueOf(R.layout.page_monitor2));
            }
            ArrayList<TextView> arrPageLabels = new ArrayList();
            arrPageLabels.add((TextView) this.m_PopupRootView.findViewById(R.id.textViewComputerScreen));
            if (bHasAnswerPage) {
                arrPageLabels.add((TextView) this.m_PopupRootView.findViewById(R.id.textViewQuestionAnswer));
            } else {
                ((TextView) this.m_PopupRootView.findViewById(R.id.textViewQuestionAnswer)).setTextColor(-2368549);
            }
            ViewPager m_myPager = (ViewPager) this.m_PopupRootView.findViewById(R.id.pageSelector);
            this.m_Adapter = new SimplePagesAdapter(arrPages, arrPageLabels, m_myPager, this);
            m_myPager.setAdapter(this.m_Adapter);
            m_myPager.setOnPageChangeListener(this.m_Adapter);
            if (bHasAnswerPage) {
                m_myPager.setCurrentItem(1);
            } else {
                m_myPager.setCurrentItem(0);
            }
            this.m_Adapter.onPageSelected(m_myPager.getCurrentItem());
            TextView textviewIP = (TextView) this.m_PopupRootView.findViewById(R.id.textViewIP);
            if (textviewIP != null) {
                String szLanIP = (String) TeacherPadApplication.mapStudentIP.get(this.m_szStudentID);
                boolean bNoDisplay = true;
                if (szLanIP == null) {
                    szLanIP = "没有获得内网IP";
                } else {
                    bNoDisplay = false;
                }
                if (bNoDisplay) {
                    textviewIP.setVisibility(8);
                } else {
                    textviewIP.setText("IP：" + szLanIP);
                }
            }
            Button AskQuestionButton = (Button) this.m_PopupRootView.findViewById(R.id.buttonAskQuestion);
            if (AskQuestionButton != null) {
                AskQuestionButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        TeacherPadApplication.IMThread.SendAskQuestionRequest(StudentNodeView.this.m_szStudentID);
                        new Builder(v.getContext()).setTitle("等待提问结束").setCancelable(false).setMessage("请在学生回答结束后，点击下面的确定按钮。").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                TeacherPadApplication.IMThread.SendAskQuestionRequest(null);
                            }
                        }).show();
                    }
                });
            }
            Button LockScreenButton = (Button) this.m_PopupRootView.findViewById(R.id.buttonLockScreen);
            if (LockScreenButton != null) {
                if (null == null) {
                    LockScreenButton.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            TeacherPadApplication.IMThread.SendLockRequest(StudentNodeView.this.m_szStudentID);
                            TeacherPadApplication.IMThread.SendStatusRequest(StudentNodeView.this.m_szStudentID);
                        }
                    });
                } else {
                    LockScreenButton.setText("打开语音");
                    LockScreenButton.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            TeacherPadApplication.IMThread.SendMessage("ActiveVoice", StudentNodeView.this.m_szStudentID);
                            TeacherPadApplication.switchToStudentAudio(StudentNodeView.this.m_szStudentID);
                        }
                    });
                }
            }
            Button UnLockScreenButton = (Button) this.m_PopupRootView.findViewById(R.id.buttonUnLock);
            if (UnLockScreenButton != null) {
                if (null == null) {
                    UnLockScreenButton.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            TeacherPadApplication.IMThread.SendUnLockRequest(StudentNodeView.this.m_szStudentID);
                            TeacherPadApplication.IMThread.SendStatusRequest(StudentNodeView.this.m_szStudentID);
                        }
                    });
                } else {
                    UnLockScreenButton.setText("关闭语音");
                    UnLockScreenButton.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            TeacherPadApplication.IMThread.SendMessage("StopVoice", StudentNodeView.this.m_szStudentID);
                            TeacherPadApplication.stopStudentAudio();
                        }
                    });
                }
            }
            Button ProjectToScreenButton = (Button) this.m_PopupRootView.findViewById(R.id.buttonProjectToScreen);
            if (ProjectToScreenButton != null) {
                ProjectToScreenButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        TeacherPadApplication.IMThread.SendProjectRequest(StudentNodeView.this.m_szStudentID, StudentNodeView.this.m_szStudentAnswer);
                    }
                });
            }
            Button AnswerReSendButton = (Button) this.m_PopupRootView.findViewById(R.id.buttonReAnswer);
            if (AnswerReSendButton != null) {
                if (bHasAnswerPage) {
                    AnswerReSendButton.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            String szAnswer = (String) TeacherPadApplication.mapStudentsQuestionAnswers.get(StudentNodeView.this.m_szStudentID);
                            if (szAnswer.length() > 10) {
                                TeacherPadApplication.IMThread.SendProjectRequest(StudentNodeView.this.m_szStudentID, szAnswer);
                            }
                        }
                    });
                } else {
                    AnswerReSendButton.setEnabled(false);
                }
            }
            PopupWindow popupWindow = new PopupWindow(RootView, (int) (490.0f * Utilities.getDisplayScale(this.m_Context)), (int) (300.0f * Utilities.getDisplayScale(this.m_Context)), true);
            this.m_bDialogShow = true;
            popupWindow.setBackgroundDrawable(this.m_Context.getResources().getDrawable(R.drawable.background_tip));
            popupWindow.setOutsideTouchable(true);
            popupWindow.showAsDropDown(v);
            popupWindow.setOnDismissListener(new OnDismissListener() {
                public void onDismiss() {
                    StudentNodeView.this.m_PopupRootView = null;
                    StudentNodeView.this.m_bDialogShow = false;
                    if (StudentNodeView.this.m_MJpegDisplayThread != null) {
                        StudentNodeView.this.m_MJpegDisplayThread.stopDisplay();
                        Log.d("StudentNodeView", "Stop MJpeg thread.");
                        StudentNodeView.this.m_MJpegDisplayThread = null;
                    }
                    TeacherPadApplication.IMThread.SendMessage("StopMJpegServer", StudentNodeView.this.m_szStudentID);
                }
            });
        }
    }

    public void OnPageInstantiated(int nPosition) {
        if (nPosition == 0) {
            if (TeacherPadApplication.mapStudentIP.containsKey(this.m_szStudentID)) {
                String szURL = "";
                synchronized (TeacherPadApplication.mapStudentIP) {
                    szURL = (String) TeacherPadApplication.mapStudentIP.get(this.m_szStudentID);
                }
                szURL = "http://" + szURL + ":" + String.valueOf(8081) + "/";
                if (szURL != null) {
                    this.m_MJpegDisplayThread = new MJpegDisplayThread(this.m_Context, this.m_Handler, 0, szURL, this);
                    this.m_MJpegDisplayThread.SetShortTimeWait(false);
                    this.m_MJpegDisplayThread.start();
                }
            }
        } else if (nPosition == 1) {
            String szKey = "";
            if (TeacherPadApplication.mapStudentsQuestionAnswers.containsKey(this.m_szStudentID)) {
                String szAnswer = (String) TeacherPadApplication.mapStudentsQuestionAnswers.get(this.m_szStudentID);
                if (szAnswer.length() > 10) {
                    szKey = szAnswer;
                }
            }
            if (!szKey.isEmpty() && this.m_PopupRootView != null) {
                ImageView PopupPadImage = (ImageView) this.m_PopupRootView.findViewById(R.id.imageView2);
                if (PopupPadImage != null && PopupPadImage.getVisibility() == 0) {
                    Picasso.with(this.m_Context).load(MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/GetTemporaryStorage?filename=" + szKey).into(PopupPadImage);
                }
            }
        }
    }

    public void OnNewMJpegInstance(MJpegDisplayThread NewThread) {
        this.m_MJpegDisplayThread = NewThread;
    }

    public void OnMJpegError(String szMessage) {
    }

    public void OnMJpegMessage(String szMessage) {
    }
}
