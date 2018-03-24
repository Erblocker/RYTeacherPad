package com.netspace.library.dialog;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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

public class VoiceRecordDialog extends Dialog implements OnClickListener {
    private final int MSG_ENCODE_ERROR = 2;
    private final int MSG_FILE_ERROR = 3;
    private final int MSG_RECORD_ERROR = 1;
    private final int MSG_UPDATE_AMP = 4;
    private final int MSG_UPDATE_TIMER = 5;
    private Context mContext;
    private String mFileName;
    private boolean mIsWorking = true;
    private OnRecordSendListener mListener;
    private Handler mMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    new Builder(VoiceRecordDialog.this.mContext).setTitle("录音错误").setIcon(17301543).setMessage("请检查您的设备是否能支持所需的格式，或者当前是否已经有程序正在使用麦克风了。").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            VoiceRecordDialog.this.dismiss();
                        }
                    }).show();
                    return;
                case 2:
                    new Builder(VoiceRecordDialog.this.mContext).setTitle("录音错误").setIcon(17301543).setMessage("声音编码出现问题").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            VoiceRecordDialog.this.dismiss();
                        }
                    }).show();
                    return;
                case 3:
                    new Builder(VoiceRecordDialog.this.mContext).setTitle("录音错误").setIcon(17301543).setMessage("临时文件出现问题").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            VoiceRecordDialog.this.dismiss();
                        }
                    }).show();
                    return;
                default:
                    return;
            }
        }
    };
    private MP3RecordThread mRecordThread;
    private Button mSendButton;
    private Date mStartTime;
    private TextView mTextViewTimer;
    private Runnable mUpdateTimerRunnable = new Runnable() {
        public void run() {
            long diff = new Date().getTime() - VoiceRecordDialog.this.mStartTime.getTime();
            long diffSeconds = (diff / 1000) % 60;
            long diffHours = diff / 3600000;
            String szFormat = String.format("%02d:%02d", new Object[]{Long.valueOf((diff / 60000) % 60), Long.valueOf(diffSeconds)});
            VoiceRecordDialog.this.mszTotalTime = szFormat;
            VoiceRecordDialog.this.mTextViewTimer.setText(szFormat);
            if ((diff / 60000) % 60 >= 3) {
                VoiceRecordDialog.this.mIsWorking = false;
                VoiceRecordDialog.this.mTextViewTimer.setText("达到3分钟上限，录音已停止");
                return;
            }
            VoiceRecordDialog.this.mMessageHandler.postDelayed(VoiceRecordDialog.this.mUpdateTimerRunnable, 300);
        }
    };
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
            Throwable th;
            setName("VoiceRecordDialog.MP3RecordThread");
            Process.setThreadPriority(-19);
            int minBufferSize = AudioRecord.getMinBufferSize(this.mSampleRate, this.mChannelConfig, 2);
            if (minBufferSize < 0) {
                Log.e(TAG, "Format not supported. getMinBufferSize return " + minBufferSize);
                VoiceRecordDialog.this.mMessageHandler.obtainMessage(1).sendToTarget();
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
                try {
                    File file = new File(VoiceRecordDialog.this.mFileName);
                    File file2;
                    try {
                        FileOutputStream fos2 = new FileOutputStream(file);
                        while (VoiceRecordDialog.this.mIsWorking) {
                            try {
                                int readSize = audioRecord.read(rawBuffer, 0, rawBuffer.length);
                                if (readSize < 0) {
                                    VoiceRecordDialog.this.mMessageHandler.obtainMessage(1).sendToTarget();
                                    break;
                                } else if (readSize != 0) {
                                    nEncResult = SimpleLame.encode(rawBuffer, rawBuffer, readSize, mp3Buffer);
                                    if (nEncResult < 0) {
                                        VoiceRecordDialog.this.mMessageHandler.obtainMessage(1).sendToTarget();
                                        break;
                                    } else if (nEncResult != 0) {
                                        fos2.write(mp3Buffer, 0, nEncResult);
                                    } else {
                                        continue;
                                    }
                                }
                            } catch (Exception e) {
                                fos = fos2;
                                file2 = file;
                            } catch (Throwable th2) {
                                th = th2;
                                fos = fos2;
                                file2 = file;
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
                            file2 = file;
                        } else {
                            file2 = file;
                        }
                    } catch (Exception e2) {
                        file2 = file;
                        try {
                            VoiceRecordDialog.this.mMessageHandler.obtainMessage(3).sendToTarget();
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
                        file2 = file;
                        if (fos != null) {
                        }
                        throw th;
                    }
                } catch (Exception e3) {
                    VoiceRecordDialog.this.mMessageHandler.obtainMessage(3).sendToTarget();
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
                VoiceRecordDialog.this.mMessageHandler.obtainMessage(1).sendToTarget();
            }
        }
    }

    public interface OnRecordSendListener {
        void OnRecordSend(String str, VoiceRecordDialog voiceRecordDialog, String str2);
    }

    static {
        System.loadLibrary("mp3lame");
    }

    public VoiceRecordDialog(Context context, OnRecordSendListener CallBack) {
        super(context, 16974130);
        this.mContext = context;
        setTitle("发送语音");
        this.mListener = CallBack;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_voicerecord);
        Button button = (Button) findViewById(R.id.buttonSend);
        this.mSendButton = button;
        button.setOnClickListener(this);
        findViewById(R.id.buttonCancel).setOnClickListener(this);
        this.mTextViewTimer = (TextView) findViewById(R.id.textViewTimer);
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

    public void onClick(View v) {
        if (v.getId() == R.id.buttonSend) {
            this.mIsWorking = false;
            if (this.mRecordThread != null) {
                try {
                    this.mRecordThread.join();
                    this.mRecordThread = null;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
                        VoiceRecordDialog.this.mListener.OnRecordSend(szImageKey, VoiceRecordDialog.this, VoiceRecordDialog.this.mszTotalTime);
                    }
                });
                CallItem.setFailureListener(new OnFailureListener() {
                    public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                        VoiceRecordDialog.this.mSendButton.setText("发送失败，点击重试");
                        VoiceRecordDialog.this.mSendButton.setEnabled(true);
                    }
                });
                CallItem.setParam("lpszBase64Data", szEncodedData);
                CallItem.setParam("szKey", szImageKey);
                CallItem.setAlwaysActiveCallbacks(true);
                VirtualNetworkObject.addToQueue(CallItem);
            }
        } else if (v.getId() == R.id.buttonCancel) {
            dismiss();
        }
    }

    protected void onStop() {
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
        super.onStop();
    }
}
