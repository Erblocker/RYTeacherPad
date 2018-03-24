package com.netspace.library.servers;

import android.media.AudioTrack;
import android.os.Process;
import android.util.Log;
import com.netspace.library.restful.provider.device.DeviceOperationRESTServiceProvider;
import java.io.IOException;
import java.net.URI;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

public class MP3PlayThread extends Thread {
    private static final String TAG = "MP3PlayThread";
    private AudioTrack mAudioTrack;
    private boolean mAutoReconnect = true;
    private int mBufferIndex = 0;
    private MP3PlayThreadCallBackInterface mCallBack;
    private Decoder mDecoder;
    private short[] mOutputBuffer1;
    private int mOutputBuffer1Index = 0;
    private short[] mOutputBuffer2;
    private int mOutputBuffer2Index = 0;
    private boolean mPause = false;
    private int mPlayBuffer1Index = 0;
    private int mPlayBuffer2Index = 0;
    private int mPlayBufferIndex = 0;
    private boolean mPlaying = false;
    private WriteAudioThread mWriteAudioThread;
    private HttpClient m_HttpClient;
    private volatile boolean m_bWorking = true;
    private String m_szTargetIP = "";

    public interface MP3PlayThreadCallBackInterface {
        boolean onEndOfStream();

        void onNewMP3PlayThreadInstance(MP3PlayThread mP3PlayThread);

        void onPlayError();

        void onPlayStart();
    }

    private class WriteAudioThread extends Thread {
        private boolean mStartAudioPlay;

        private WriteAudioThread() {
            this.mStartAudioPlay = false;
        }

        public void run() {
            setName("WriteAudioThread");
            while (MP3PlayThread.this.mPlaying) {
                if (MP3PlayThread.this.mPlayBufferIndex == 0) {
                    if (MP3PlayThread.this.mPlayBuffer1Index < MP3PlayThread.this.mOutputBuffer1Index) {
                        int nStartPos = MP3PlayThread.this.mPlayBuffer1Index;
                        int nEndPos = MP3PlayThread.this.mOutputBuffer1Index;
                        MP3PlayThread.this.mAudioTrack.write(MP3PlayThread.this.mOutputBuffer1, nStartPos, nEndPos);
                        MP3PlayThread.this.mPlayBuffer1Index = nEndPos;
                    } else {
                        MP3PlayThread.this.mPlayBufferIndex = 1;
                        MP3PlayThread.this.mPlayBuffer2Index = 0;
                        Log.d(MP3PlayThread.TAG, "Switch play buffer to buffer2");
                    }
                }
                if (MP3PlayThread.this.mPlayBufferIndex == 1) {
                    if (MP3PlayThread.this.mPlayBuffer2Index < MP3PlayThread.this.mOutputBuffer2Index) {
                        nStartPos = MP3PlayThread.this.mPlayBuffer2Index;
                        nEndPos = MP3PlayThread.this.mOutputBuffer2Index;
                        MP3PlayThread.this.mAudioTrack.write(MP3PlayThread.this.mOutputBuffer2, nStartPos, nEndPos);
                        MP3PlayThread.this.mPlayBuffer2Index = nEndPos;
                    } else {
                        MP3PlayThread.this.mPlayBufferIndex = 0;
                        MP3PlayThread.this.mPlayBuffer1Index = 0;
                        Log.d(MP3PlayThread.TAG, "Switch play buffer to buffer1");
                    }
                }
                if (!this.mStartAudioPlay) {
                    MP3PlayThread.this.mAudioTrack.play();
                    this.mStartAudioPlay = true;
                }
            }
            super.run();
        }
    }

    public MP3PlayThread(String szTargetIP, MP3PlayThreadCallBackInterface CallBack) {
        this.m_szTargetIP = szTargetIP;
        this.m_bWorking = true;
        this.mCallBack = CallBack;
    }

    public void pause(boolean bPause) {
        this.mPause = bPause;
    }

    public void stopPlay() {
        this.m_bWorking = false;
        if (!(this.m_HttpClient == null || this.m_HttpClient.getConnectionManager() == null)) {
            this.m_HttpClient.getConnectionManager().shutdown();
        }
        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setAutoReconnect(boolean bEnable) {
        this.mAutoReconnect = bEnable;
    }

    private void reconnect() {
        if (this.mAutoReconnect && this.m_bWorking) {
            Log.d(TAG, "Reconnecting to " + this.m_szTargetIP + ". Using new MP3PlayThread instance.");
            MP3PlayThread NewThread = new MP3PlayThread(this.m_szTargetIP, this.mCallBack);
            NewThread.start();
            if (this.mCallBack != null) {
                this.mCallBack.onNewMP3PlayThreadInstance(NewThread);
            }
        }
    }

    public void run() {
        setName(TAG);
        Process.setThreadPriority(-19);
        BasicHttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, DeviceOperationRESTServiceProvider.TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParams, DeviceOperationRESTServiceProvider.TIMEOUT);
        this.m_HttpClient = new DefaultHttpClient(httpParams);
        this.mDecoder = new Decoder();
        Log.d(TAG, "Connect to MP3Server. URL=" + this.m_szTargetIP);
        try {
            HttpResponse res = this.m_HttpClient.execute(new HttpGet(URI.create(this.m_szTargetIP)));
            Log.d(TAG, "Connected, server report status = " + res.getStatusLine().getStatusCode());
            if (res.getStatusLine().getStatusCode() == 200) {
                int intSize = AudioTrack.getMinBufferSize(44100, 4, 2);
                this.mAudioTrack = new AudioTrack(3, 44100, 4, 2, intSize, 1);
                this.mOutputBuffer1 = new short[(intSize * 10)];
                this.mOutputBuffer2 = new short[(intSize * 10)];
                Bitstream bitstream = new Bitstream(res.getEntity().getContent());
                boolean bAudioStarted = false;
                if (this.mCallBack != null) {
                    this.mCallBack.onPlayStart();
                }
                while (this.m_bWorking) {
                    Header frameHeader = bitstream.readFrame();
                    if (frameHeader == null) {
                        if (this.mCallBack != null) {
                            Log.e(TAG, "readFrame return null. Maybe end of stream or no data received.");
                            this.mCallBack.onEndOfStream();
                        }
                        if (this.m_HttpClient.getConnectionManager() != null) {
                            this.m_HttpClient.getConnectionManager().shutdown();
                        }
                        if (this.m_bWorking) {
                            reconnect();
                        }
                        if (this.mWriteAudioThread != null) {
                            this.mPlaying = false;
                            try {
                                this.mWriteAudioThread.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        if (this.mAudioTrack != null) {
                            this.mAudioTrack.stop();
                            this.mAudioTrack = null;
                        }
                        if (this.m_HttpClient.getConnectionManager() != null) {
                            this.m_HttpClient.getConnectionManager().shutdown();
                        }
                        super.run();
                    }
                    SampleBuffer output = (SampleBuffer) this.mDecoder.decodeFrame(frameHeader, bitstream);
                    if (!this.mPause) {
                        if (this.mBufferIndex == 0) {
                            if (this.mOutputBuffer1Index + output.getBufferLength() >= this.mOutputBuffer1.length || this.mPlayBufferIndex == this.mBufferIndex) {
                                this.mBufferIndex = 1;
                                this.mOutputBuffer2Index = 0;
                                Log.d(TAG, "Switch decode buffer to buffer2");
                            }
                        } else if (this.mOutputBuffer2Index + output.getBufferLength() >= this.mOutputBuffer2.length || this.mPlayBufferIndex == this.mBufferIndex) {
                            this.mBufferIndex = 0;
                            this.mOutputBuffer1Index = 0;
                            Log.d(TAG, "Switch decode buffer to buffer1");
                        }
                        short[] outputData = output.getBuffer();
                        for (int i = 0; i < output.getBufferLength(); i++) {
                            if (this.mBufferIndex == 0) {
                                this.mOutputBuffer1[this.mOutputBuffer1Index] = outputData[i];
                                this.mOutputBuffer1Index++;
                            } else {
                                this.mOutputBuffer2[this.mOutputBuffer2Index] = outputData[i];
                                this.mOutputBuffer2Index++;
                            }
                        }
                        if (!bAudioStarted) {
                            this.mPlaying = true;
                            MP3PlayThread mP3PlayThread = this;
                            this.mWriteAudioThread = new WriteAudioThread();
                            this.mWriteAudioThread.start();
                            bAudioStarted = true;
                        }
                    }
                    bitstream.closeFrame();
                }
                if (this.m_HttpClient.getConnectionManager() != null) {
                    this.m_HttpClient.getConnectionManager().shutdown();
                }
                if (this.m_bWorking) {
                    reconnect();
                }
                if (this.mWriteAudioThread != null) {
                    this.mPlaying = false;
                    this.mWriteAudioThread.join();
                }
                if (this.mAudioTrack != null) {
                    this.mAudioTrack.stop();
                    this.mAudioTrack = null;
                }
                if (this.m_HttpClient.getConnectionManager() != null) {
                    this.m_HttpClient.getConnectionManager().shutdown();
                }
                super.run();
            } else if (this.mCallBack != null) {
                this.mCallBack.onPlayError();
            }
        } catch (ClientProtocolException e2) {
            e2.printStackTrace();
            Log.d(TAG, "Request failed-ClientProtocolException", e2);
            if (this.mCallBack != null) {
                this.mCallBack.onPlayError();
            }
            reconnect();
        } catch (IOException e3) {
            e3.printStackTrace();
            Log.d(TAG, "Request failed-IOException", e3);
            if (this.mCallBack != null) {
                this.mCallBack.onPlayError();
            }
            reconnect();
        } catch (IllegalStateException e4) {
            e4.printStackTrace();
            if (this.mCallBack != null) {
                this.mCallBack.onPlayError();
            }
        } catch (DecoderException e5) {
            e5.printStackTrace();
            if (this.mCallBack != null) {
                this.mCallBack.onPlayError();
            }
            reconnect();
        } catch (BitstreamException e6) {
            e6.printStackTrace();
            if (this.mCallBack != null) {
                this.mCallBack.onPlayError();
            }
            reconnect();
        } catch (Exception e7) {
            if (this.mCallBack != null) {
                this.mCallBack.onPlayError();
            }
            reconnect();
        }
    }
}
