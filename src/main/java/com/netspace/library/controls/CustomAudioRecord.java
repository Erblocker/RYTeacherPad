package com.netspace.library.controls;

import android.content.Context;
import android.media.AudioRecord;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.pad.library.R;
import com.uraroji.garage.android.lame.SimpleLame;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class CustomAudioRecord extends LinearLayout implements OnClickListener {
    private final int MSG_ENCODE_ERROR = 2;
    private final int MSG_FILE_ERROR = 3;
    private final int MSG_RECORD_ERROR = 1;
    private final int MSG_UPDATE_AMP = 4;
    private final int MSG_UPDATE_TIMER = 5;
    private final String TAG = "CustomAudioRecord";
    private Button mCancelButton;
    private Context mContext;
    private String mFileName;
    private boolean mIsWorking = true;
    private OnRecordSendListener mListener;
    private Handler mMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    CustomAudioRecord.this.mTextViewMessage.setText("录音错误，无法打开麦克风。");
                    return;
                case 2:
                    CustomAudioRecord.this.mTextViewMessage.setText("录音错误，声音编码出现问题。");
                    return;
                case 3:
                    CustomAudioRecord.this.mTextViewMessage.setText("录音错误，临时文件保存出现问题。");
                    return;
                default:
                    return;
            }
        }
    };
    private MP3RecordThread mRecordThread;
    private View mRootView;
    private Button mSendButton;
    private Date mStartTime;
    private TextView mTextViewMessage;
    private TextView mTextViewTimer;
    private Runnable mUpdateTimerRunnable = new Runnable() {
        public void run() {
            long diff = new Date().getTime() - CustomAudioRecord.this.mStartTime.getTime();
            long diffSeconds = (diff / 1000) % 60;
            long diffHours = diff / 3600000;
            String szFormat = String.format("%02d:%02d", new Object[]{Long.valueOf((diff / 60000) % 60), Long.valueOf(diffSeconds)});
            CustomAudioRecord.this.mszTotalTime = szFormat;
            CustomAudioRecord.this.mTextViewTimer.setText(szFormat);
            if ((diff / 60000) % 60 >= 10) {
                CustomAudioRecord.this.mIsWorking = false;
                CustomAudioRecord.this.mTextViewTimer.setText("达到10分钟上限，录音已停止");
                return;
            }
            CustomAudioRecord.this.mMessageHandler.postDelayed(CustomAudioRecord.this.mUpdateTimerRunnable, 300);
        }
    };
    private boolean mbVerbose = false;
    private String mszTotalTime = "00:00:00";

    private class MP3RecordThread extends Thread {
        private static final String TAG = "MP3RecordThread";
        private int mChannelConfig;
        private int mSampleRate;

        private MP3RecordThread() {
            this.mSampleRate = 44100;
            this.mChannelConfig = 16;
        }

        public void run() {
            File file;
            Throwable th;
            setName("VoiceRecordDialog.MP3RecordThread");
            Process.setThreadPriority(-19);
            int minBufferSize = AudioRecord.getMinBufferSize(this.mSampleRate, this.mChannelConfig, 2);
            if (minBufferSize < 0) {
                Log.e(TAG, "Format not supported. getMinBufferSize return " + minBufferSize);
                CustomAudioRecord.this.mMessageHandler.obtainMessage(1).sendToTarget();
                return;
            }
            AudioRecord audioRecord = new AudioRecord(1, this.mSampleRate, this.mChannelConfig, 2, minBufferSize * 5);
            short[] rawBuffer = new short[(((this.mSampleRate * 2) * 1) * 1)];
            byte[] mp3Buffer = new byte[((int) (7200.0d + (((double) (rawBuffer.length * 2)) * 1.25d)))];
            try {
                audioRecord.startRecording();
                SimpleLame.init(this.mSampleRate, 1, this.mSampleRate, 32);
                int nEncResult = 0;
                FileOutputStream fos = null;
                long nEncodeTick = 0;
                long nWriteTick = 0;
                try {
                    File file2 = new File(CustomAudioRecord.this.mFileName);
                    try {
                        FileOutputStream fos2 = new FileOutputStream(file2);
                        while (CustomAudioRecord.this.mIsWorking) {
                            try {
                                long nStartTick = System.currentTimeMillis();
                                int readSize = audioRecord.read(rawBuffer, 0, rawBuffer.length);
                                long nAudioReadTick = System.currentTimeMillis() - nStartTick;
                                if (readSize < 0) {
                                    CustomAudioRecord.this.mMessageHandler.obtainMessage(1).sendToTarget();
                                    break;
                                }
                                if (readSize != 0) {
                                    nStartTick = System.currentTimeMillis();
                                    nEncResult = SimpleLame.encode(rawBuffer, rawBuffer, readSize, mp3Buffer);
                                    nEncodeTick = System.currentTimeMillis() - nStartTick;
                                    if (nEncResult < 0) {
                                        CustomAudioRecord.this.mMessageHandler.obtainMessage(1).sendToTarget();
                                        break;
                                    } else if (nEncResult != 0) {
                                        nStartTick = System.currentTimeMillis();
                                        fos2.write(mp3Buffer, 0, nEncResult);
                                        nWriteTick = System.currentTimeMillis() - nStartTick;
                                    }
                                }
                                if (CustomAudioRecord.this.mbVerbose) {
                                    Log.i(TAG, "ReadTick=" + nAudioReadTick + ",EncodeTick=" + nEncodeTick + ",WriteTick=" + nWriteTick);
                                }
                            } catch (Exception e) {
                                fos = fos2;
                                file = file2;
                            } catch (Throwable th2) {
                                th = th2;
                                fos = fos2;
                                file = file2;
                            }
                        }
                        int flushResult = SimpleLame.flush(mp3Buffer);
                        if (flushResult >= 0 && flushResult != 0) {
                            fos2.write(mp3Buffer, 0, nEncResult);
                        }
                        SimpleLame.close();
                        fos2.flush();
                        fos2.close();
                        if (fos2 != null) {
                            file = file2;
                        } else {
                            file = file2;
                        }
                    } catch (Exception e2) {
                        file = file2;
                        try {
                            CustomAudioRecord.this.mMessageHandler.obtainMessage(3).sendToTarget();
                            if (fos != null) {
                            }
                            audioRecord.stop();
                            audioRecord.release();
                            super.run();
                        } catch (Throwable th3) {
                            th = th3;
                            if (fos != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        file = file2;
                        if (fos != null) {
                        }
                        throw th;
                    }
                } catch (Exception e3) {
                    CustomAudioRecord.this.mMessageHandler.obtainMessage(3).sendToTarget();
                    if (fos != null) {
                    }
                    audioRecord.stop();
                    audioRecord.release();
                    super.run();
                }
                audioRecord.stop();
                audioRecord.release();
                super.run();
            } catch (IllegalStateException e4) {
                e4.printStackTrace();
                Log.e(TAG, "startRecording failed.");
                CustomAudioRecord.this.mMessageHandler.obtainMessage(1).sendToTarget();
            }
        }
    }

    public interface OnRecordSendListener {
        void OnRecordCancel();

        void OnRecordSend(String str, String str2);
    }

    static {
        System.loadLibrary("mp3lame");
    }

    public CustomAudioRecord(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public CustomAudioRecord(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CustomAudioRecord(Context context) {
        super(context);
        initView();
    }

    public void initView() {
        this.mContext = getContext();
        this.mRootView = ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.layout_customaudiorecord, this);
        Button button = (Button) this.mRootView.findViewById(R.id.buttonSend);
        this.mSendButton = button;
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.buttonCancel);
        this.mCancelButton = button;
        button.setOnClickListener(this);
        this.mTextViewTimer = (TextView) this.mRootView.findViewById(R.id.textViewTimer);
        this.mTextViewMessage = (TextView) this.mRootView.findViewById(R.id.textViewMessage);
    }

    public void start() {
        if (this.mListener == null) {
            this.mSendButton.setEnabled(false);
        }
        try {
            this.mFileName = File.createTempFile("audio_", ".mp3", this.mContext.getExternalCacheDir()).getAbsolutePath();
            this.mStartTime = new Date();
            this.mRecordThread = new MP3RecordThread();
            this.mRecordThread.start();
            this.mMessageHandler.postDelayed(this.mUpdateTimerRunnable, 300);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSendCallBack(OnRecordSendListener CallBack) {
        this.mListener = CallBack;
    }

    public void onClick(View v) {
        if (v.getId() == R.id.buttonSend) {
            this.mIsWorking = false;
            if (this.mbVerbose) {
                Log.d("CustomAudioRecord", "mIsWorking set to false.");
            }
            if (this.mRecordThread != null) {
                try {
                    this.mRecordThread.join();
                    this.mRecordThread = null;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (this.mbVerbose) {
                Log.d("CustomAudioRecord", "mRecordThread stopped.");
            }
            if (this.mListener != null) {
                this.mSendButton.setText("正在发送");
                this.mSendButton.setEnabled(false);
                this.mMessageHandler.removeCallbacks(this.mUpdateTimerRunnable);
                final String szImageKey = "Audio_" + Utilities.createGUID() + ".mp3";
                String szEncodedData = Utilities.getBase64FileContent(this.mFileName);
                WebServiceCallItemObject CallItem = new WebServiceCallItemObject("PutTemporaryStorage", null);
                CallItem.setSuccessListener(new OnSuccessListener() {
                    public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                        CustomAudioRecord.this.mListener.OnRecordSend(szImageKey, CustomAudioRecord.this.mszTotalTime);
                    }
                });
                CallItem.setFailureListener(new OnFailureListener() {
                    public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                        CustomAudioRecord.this.mSendButton.setText("发送失败，点击重试");
                        CustomAudioRecord.this.mSendButton.setEnabled(true);
                    }
                });
                CallItem.setParam("lpszBase64Data", szEncodedData);
                CallItem.setParam("szKey", szImageKey);
                CallItem.setAlwaysActiveCallbacks(true);
                VirtualNetworkObject.addToQueue(CallItem);
            }
        } else if (v.getId() == R.id.buttonCancel) {
            this.mCancelButton.setEnabled(false);
            this.mCancelButton.setText("正在取消");
            this.mIsWorking = false;
            if (this.mRecordThread != null) {
                try {
                    this.mRecordThread.join();
                    this.mRecordThread = null;
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
            }
            if (this.mListener != null) {
                this.mListener.OnRecordCancel();
            }
        }
    }

    protected void onDetachedFromWindow() {
        this.mMessageHandler.removeCallbacks(this.mUpdateTimerRunnable);
        this.mIsWorking = false;
        if (this.mRecordThread != null) {
            try {
                this.mRecordThread.join();
                this.mRecordThread = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        new File(this.mFileName).delete();
        super.onDetachedFromWindow();
    }
}
