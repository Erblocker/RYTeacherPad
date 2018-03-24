package com.netspace.library.servers;

import android.media.AudioRecord;
import android.util.Log;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class AudioTCPServer extends Thread {
    private static int[] m_SampleRates = new int[]{44100, 22050, 11025, 8000};
    protected String TAG = "AudioServer";
    protected byte[] m_Buffer1;
    protected byte[] m_Buffer2;
    protected AudioRecord m_Recorder;
    private ServerSocket m_Server;
    protected DatagramSocket m_Socket;
    private ArrayList<OutputStream> m_arrClientOutputStream;
    private ArrayList<Socket> m_arrClientSockets;
    protected volatile boolean m_bRecordAndSend = false;
    protected int m_nMinBufSize = -1;
    protected int m_nPort = 50005;
    protected int nBuffer1Size = 0;
    protected int nBuffer2Size = 0;

    private class AudioSendThread extends Thread {
        private AudioSendThread() {
        }

        public void run() {
            while (AudioTCPServer.this.m_bRecordAndSend) {
                int i = 0;
                while (i < AudioTCPServer.this.m_arrClientOutputStream.size()) {
                    OutputStream Stream = (OutputStream) AudioTCPServer.this.m_arrClientOutputStream.get(i);
                    try {
                        if (AudioTCPServer.this.nBuffer1Size > 0) {
                            Stream.write(AudioTCPServer.this.m_Buffer1, 0, AudioTCPServer.this.nBuffer1Size);
                        } else if (AudioTCPServer.this.nBuffer2Size > 0) {
                            Stream.write(AudioTCPServer.this.m_Buffer2, 0, AudioTCPServer.this.nBuffer2Size);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        AudioTCPServer.this.m_arrClientSockets.remove(i);
                        AudioTCPServer.this.m_arrClientOutputStream.remove(i);
                        i--;
                    }
                    i++;
                }
                AudioTCPServer.this.nBuffer1Size = 0;
                AudioTCPServer.this.nBuffer2Size = 0;
            }
        }
    }

    private class RecordMainThread extends Thread {
        private RecordMainThread() {
        }

        public void run() {
            AudioTCPServer.this.m_Recorder.startRecording();
            new AudioSendThread().start();
            while (AudioTCPServer.this.m_bRecordAndSend) {
                for (int i = 0; i < 2; i++) {
                    if (i == 0) {
                        AudioTCPServer.this.nBuffer1Size = AudioTCPServer.this.m_Recorder.read(AudioTCPServer.this.m_Buffer1, 0, AudioTCPServer.this.m_Buffer1.length);
                    } else {
                        AudioTCPServer.this.nBuffer2Size = AudioTCPServer.this.m_Recorder.read(AudioTCPServer.this.m_Buffer2, 0, AudioTCPServer.this.m_Buffer2.length);
                    }
                }
            }
        }
    }

    private AudioRecord findAudioRecord() {
        for (int rate : m_SampleRates) {
            for (short audioFormat : new short[]{(short) 2, (short) 3}) {
                for (short channelConfig : new short[]{(short) 16, (short) 12}) {
                    try {
                        Log.d(this.TAG, "Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: " + channelConfig);
                        int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);
                        if (bufferSize != -2) {
                            AudioRecord recorder = new AudioRecord(1, rate, channelConfig, audioFormat, bufferSize);
                            if (recorder.getState() == 1) {
                                this.m_nMinBufSize = bufferSize;
                                return recorder;
                            }
                        } else {
                            continue;
                        }
                    } catch (Exception e) {
                        Log.e(this.TAG, new StringBuilder(String.valueOf(rate)).append("Exception, keep trying.").toString(), e);
                    }
                }
            }
        }
        return null;
    }

    public void run() {
        try {
            this.m_Server = new ServerSocket(this.m_nPort);
            while (this.m_bRecordAndSend) {
                Socket socket = this.m_Server.accept();
                Log.i(this.TAG, "New connection to :" + socket.getInetAddress());
                this.m_arrClientSockets.add(socket);
                this.m_arrClientOutputStream.add(socket.getOutputStream());
            }
            this.m_Server.close();
        } catch (IOException e) {
            Log.e(this.TAG, e.getMessage());
        }
    }

    public boolean StartServer(int nPort) {
        this.m_Recorder = findAudioRecord();
        this.m_nPort = nPort;
        if (this.m_Recorder == null) {
            return false;
        }
        this.m_arrClientSockets = new ArrayList();
        this.m_arrClientOutputStream = new ArrayList();
        this.m_nMinBufSize += 9728;
        this.m_Buffer1 = new byte[this.m_nMinBufSize];
        this.m_Buffer2 = new byte[this.m_nMinBufSize];
        this.m_bRecordAndSend = true;
        new RecordMainThread().start();
        start();
        return true;
    }

    public boolean StopServer() {
        if (!this.m_bRecordAndSend) {
            return false;
        }
        this.m_bRecordAndSend = false;
        return true;
    }
}
