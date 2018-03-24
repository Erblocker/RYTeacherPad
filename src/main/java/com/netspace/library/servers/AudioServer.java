package com.netspace.library.servers;

import android.media.AudioRecord;
import android.util.Log;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class AudioServer {
    private static int[] m_SampleRates = new int[]{44100, 22050, 11025, 8000};
    protected String TAG = "AudioServer";
    protected AudioBuffer[] m_Buffers = new AudioBuffer[10];
    protected InetAddress m_Destination;
    protected int m_Port = 50005;
    protected AudioRecord m_Recorder;
    protected DatagramSocket m_Socket;
    protected boolean m_bRecordAndSend = false;
    protected int m_nMinBufSize = -1;

    private class AudioBuffer {
        public byte[] AudioBuffers;
        public int nBufferSize;

        private AudioBuffer() {
        }
    }

    private class AudioSendThread extends Thread {
        private AudioSendThread() {
        }

        public void run() {
            DatagramPacket packet = null;
            while (AudioServer.this.m_bRecordAndSend) {
                for (int i = 0; i < AudioServer.this.m_Buffers.length; i++) {
                    if (AudioServer.this.m_Buffers[i].nBufferSize > 0) {
                        packet = new DatagramPacket(AudioServer.this.m_Buffers[i].AudioBuffers, AudioServer.this.m_Buffers[i].nBufferSize, AudioServer.this.m_Destination, AudioServer.this.m_Port);
                        AudioServer.this.m_Buffers[i].nBufferSize = 0;
                        break;
                    }
                }
                if (packet != null) {
                    try {
                        AudioServer.this.m_Socket.send(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                Log.d(AudioServer.this.TAG, "No data to sent.");
            }
        }
    }

    private class MainThread extends Thread {
        private MainThread() {
        }

        public void run() {
            AudioServer.this.m_Recorder.startRecording();
            int i = 0;
            new AudioSendThread().start();
            while (AudioServer.this.m_bRecordAndSend) {
                if (AudioServer.this.m_Buffers[i].nBufferSize != 0) {
                    Log.d(AudioServer.this.TAG, "AudioBuffer underrun. Overwrite unsent data.");
                }
                AudioServer.this.m_Buffers[i].nBufferSize = AudioServer.this.m_Recorder.read(AudioServer.this.m_Buffers[i].AudioBuffers, 0, AudioServer.this.m_Buffers[i].AudioBuffers.length);
                i++;
                if (i >= AudioServer.this.m_Buffers.length) {
                    i = 0;
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

    public boolean InitServer(String szTargetHost, int nTargetPort) {
        try {
            this.m_Socket = new DatagramSocket();
            this.m_Destination = InetAddress.getByName(szTargetHost);
            this.m_Port = nTargetPort;
            this.m_Recorder = findAudioRecord();
            if (this.m_Recorder == null) {
                return false;
            }
            this.m_nMinBufSize += 9728;
            for (int i = 0; i < this.m_Buffers.length; i++) {
                this.m_Buffers[i] = new AudioBuffer();
                this.m_Buffers[i].AudioBuffers = new byte[this.m_nMinBufSize];
            }
            this.m_bRecordAndSend = true;
            new MainThread().start();
            return true;
        } catch (SocketException e) {
            e.printStackTrace();
            return false;
        } catch (UnknownHostException e2) {
            e2.printStackTrace();
            return false;
        }
    }

    public boolean StopServer() {
        if (this.m_Recorder == null) {
            return false;
        }
        this.m_bRecordAndSend = false;
        this.m_Recorder.release();
        return true;
    }
}
