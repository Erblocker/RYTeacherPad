package net.majorkernelpanic.streaming.rtsp;

import android.net.http.Headers;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import com.netspace.library.restful.provider.device.DeviceOperationRESTServiceProvider;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.Stream;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HTTP;

public class RtspClient {
    public static final int ERROR_CONNECTION_FAILED = 1;
    public static final int ERROR_CONNECTION_LOST = 4;
    public static final int ERROR_WRONG_CREDENTIALS = 3;
    public static final int MESSAGE_CONNECTION_RECOVERED = 5;
    private static final int STATE_STARTED = 0;
    private static final int STATE_STARTING = 1;
    private static final int STATE_STOPPED = 3;
    private static final int STATE_STOPPING = 2;
    public static final String TAG = "RtspClient";
    public static final int TRANSPORT_TCP = 1;
    public static final int TRANSPORT_UDP = 0;
    protected static final char[] hexArray = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private String mAuthorization;
    private BufferedReader mBufferedReader;
    private int mCSeq;
    private Callback mCallback;
    private Runnable mConnectionMonitor;
    private Handler mHandler;
    private Handler mMainHandler;
    private OutputStream mOutputStream;
    private Parameters mParameters;
    private Runnable mRetryConnection;
    private String mSessionID;
    private Socket mSocket;
    private int mState;
    private Parameters mTmpParameters;

    public interface Callback {
        void onRtspUpdate(int i, Exception exception);
    }

    private class Parameters {
        public String host;
        public String password;
        public String path;
        public int port;
        public Session session;
        public int transport;
        public String username;

        private Parameters() {
        }

        public Parameters clone() {
            Parameters params = new Parameters();
            params.host = this.host;
            params.username = this.username;
            params.password = this.password;
            params.path = this.path;
            params.session = this.session;
            params.port = this.port;
            params.transport = this.transport;
            return params;
        }
    }

    static class Response {
        public static final Pattern regexStatus = Pattern.compile("RTSP/\\d.\\d (\\d+) (\\w+)", 2);
        public static final Pattern rexegAuthenticate = Pattern.compile("realm=\"(.+)\",\\s+nonce=\"(\\w+)\"", 2);
        public static final Pattern rexegHeader = Pattern.compile("(\\S+):(.+)", 2);
        public static final Pattern rexegSession = Pattern.compile("(\\d+)", 2);
        public static final Pattern rexegTransport = Pattern.compile("client_port=(\\d+)-(\\d+).+server_port=(\\d+)-(\\d+)", 2);
        public HashMap<String, String> headers = new HashMap();
        public int status;

        Response() {
        }

        public static Response parseResponse(BufferedReader input) throws IOException, IllegalStateException, SocketException {
            Response response = new Response();
            String line = input.readLine();
            if (line == null) {
                throw new SocketException("Connection lost");
            }
            Matcher matcher = regexStatus.matcher(line);
            matcher.find();
            response.status = Integer.parseInt(matcher.group(1));
            while (true) {
                line = input.readLine();
                if (line != null && line.length() > 3) {
                    matcher = rexegHeader.matcher(line);
                    matcher.find();
                    response.headers.put(matcher.group(1).toLowerCase(Locale.US), matcher.group(2));
                }
            }
            if (line == null) {
                throw new SocketException("Connection lost");
            }
            Log.d(RtspClient.TAG, "Response from server: " + response.status);
            return response;
        }
    }

    public RtspClient() {
        this.mState = 0;
        this.mConnectionMonitor = new Runnable() {
            public void run() {
                if (RtspClient.this.mState == 0) {
                    try {
                        RtspClient.this.sendRequestOption();
                        RtspClient.this.mHandler.postDelayed(RtspClient.this.mConnectionMonitor, 6000);
                    } catch (IOException e) {
                        RtspClient.this.postMessage(4);
                        Log.e(RtspClient.TAG, "Connection lost with the server...");
                        RtspClient.this.mParameters.session.stop();
                        RtspClient.this.mHandler.post(RtspClient.this.mRetryConnection);
                    }
                }
            }
        };
        this.mRetryConnection = new Runnable() {
            public void run() {
                if (RtspClient.this.mState == 0) {
                    try {
                        Log.e(RtspClient.TAG, "Trying to reconnect...");
                        RtspClient.this.tryConnection();
                        try {
                            RtspClient.this.mParameters.session.start();
                            RtspClient.this.mHandler.post(RtspClient.this.mConnectionMonitor);
                            RtspClient.this.postMessage(5);
                        } catch (Exception e) {
                            RtspClient.this.abort();
                        }
                    } catch (IOException e2) {
                        RtspClient.this.mHandler.postDelayed(RtspClient.this.mRetryConnection, 1000);
                    }
                }
            }
        };
        this.mCSeq = 0;
        this.mTmpParameters = new Parameters();
        this.mTmpParameters.port = 1935;
        this.mTmpParameters.path = "/";
        this.mTmpParameters.transport = 0;
        this.mAuthorization = null;
        this.mCallback = null;
        this.mMainHandler = new Handler(Looper.getMainLooper());
        this.mState = 3;
        final Semaphore signal = new Semaphore(0);
        new HandlerThread("net.majorkernelpanic.streaming.RtspClient") {
            protected void onLooperPrepared() {
                RtspClient.this.mHandler = new Handler();
                signal.release();
            }
        }.start();
        signal.acquireUninterruptibly();
    }

    public void setCallback(Callback cb) {
        this.mCallback = cb;
    }

    public void setSession(Session session) {
        this.mTmpParameters.session = session;
    }

    public Session getSession() {
        return this.mTmpParameters.session;
    }

    public void setServerAddress(String host, int port) {
        this.mTmpParameters.port = port;
        this.mTmpParameters.host = host;
    }

    public void setCredentials(String username, String password) {
        this.mTmpParameters.username = username;
        this.mTmpParameters.password = password;
    }

    public void setStreamPath(String path) {
        this.mTmpParameters.path = path;
    }

    public void setTransportMode(int mode) {
        this.mTmpParameters.transport = mode;
    }

    public boolean isStreaming() {
        int i = 1;
        int i2 = this.mState == 0 ? 1 : 0;
        if (this.mState != 1) {
            i = 0;
        }
        return i2 | i;
    }

    public void startStream() {
        if (this.mTmpParameters.host == null) {
            throw new IllegalStateException("setServerAddress(String,int) has not been called !");
        } else if (this.mTmpParameters.session == null) {
            throw new IllegalStateException("setSession() has not been called !");
        } else {
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (RtspClient.this.mState == 3) {
                        RtspClient.this.mState = 1;
                        Log.d(RtspClient.TAG, "Connecting to RTSP server...");
                        RtspClient.this.mParameters = RtspClient.this.mTmpParameters.clone();
                        RtspClient.this.mParameters.session.setDestination(RtspClient.this.mTmpParameters.host);
                        try {
                            RtspClient.this.mParameters.session.syncConfigure();
                            try {
                                RtspClient.this.tryConnection();
                                try {
                                    RtspClient.this.mParameters.session.syncStart();
                                    RtspClient.this.mState = 0;
                                    if (RtspClient.this.mParameters.transport == 0) {
                                        RtspClient.this.mHandler.post(RtspClient.this.mConnectionMonitor);
                                    }
                                } catch (Exception e) {
                                    RtspClient.this.abort();
                                }
                            } catch (Exception e2) {
                                RtspClient.this.postError(1, e2);
                                RtspClient.this.abort();
                            }
                        } catch (Exception e3) {
                            RtspClient.this.mParameters.session = null;
                            RtspClient.this.mState = 3;
                        }
                    }
                }
            });
        }
    }

    public void stopStream() {
        this.mHandler.post(new Runnable() {
            public void run() {
                if (!(RtspClient.this.mParameters == null || RtspClient.this.mParameters.session == null)) {
                    RtspClient.this.mParameters.session.stop();
                }
                if (RtspClient.this.mState != 3) {
                    RtspClient.this.mState = 2;
                    RtspClient.this.abort();
                }
            }
        });
    }

    public void release() {
        stopStream();
        this.mHandler.getLooper().quit();
    }

    private void abort() {
        try {
            sendRequestTeardown();
        } catch (Exception e) {
        }
        try {
            this.mSocket.close();
        } catch (Exception e2) {
        }
        this.mHandler.removeCallbacks(this.mConnectionMonitor);
        this.mHandler.removeCallbacks(this.mRetryConnection);
        this.mState = 3;
    }

    private void tryConnection() throws IOException {
        this.mCSeq = 0;
        this.mSocket = new Socket(this.mParameters.host, this.mParameters.port);
        this.mBufferedReader = new BufferedReader(new InputStreamReader(this.mSocket.getInputStream()));
        this.mOutputStream = new BufferedOutputStream(this.mSocket.getOutputStream());
        sendRequestAnnounce();
        sendRequestSetup();
        sendRequestRecord();
    }

    private void sendRequestAnnounce() throws IllegalStateException, SocketException, IOException {
        String body = this.mParameters.session.getSessionDescription();
        StringBuilder append = new StringBuilder("ANNOUNCE rtsp://").append(this.mParameters.host).append(":").append(this.mParameters.port).append(this.mParameters.path).append(" RTSP/1.0\r\n").append("CSeq: ");
        int i = this.mCSeq + 1;
        this.mCSeq = i;
        String request = append.append(i).append("\r\n").append("Content-Length: ").append(body.length()).append("\r\n").append("Content-Type: application/sdp \r\n\r\n").append(body).toString();
        Log.i(TAG, request.substring(0, request.indexOf("\r\n")));
        this.mOutputStream.write(request.getBytes(HTTP.UTF_8));
        this.mOutputStream.flush();
        Response response = Response.parseResponse(this.mBufferedReader);
        if (response.headers.containsKey("server")) {
            Log.v(TAG, "RTSP server name:" + ((String) response.headers.get("server")));
        } else {
            Log.v(TAG, "RTSP server name unknown");
        }
        try {
            Matcher m = Response.rexegSession.matcher((CharSequence) response.headers.get("session"));
            m.find();
            this.mSessionID = m.group(1);
            if (response.status == HttpStatus.SC_UNAUTHORIZED) {
                if (this.mParameters.username == null || this.mParameters.password == null) {
                    throw new IllegalStateException("Authentication is enabled and setCredentials(String,String) was not called !");
                }
                try {
                    m = Response.rexegAuthenticate.matcher((CharSequence) response.headers.get(Headers.WWW_AUTHENTICATE));
                    m.find();
                    String nonce = m.group(2);
                    String realm = m.group(1);
                    String uri = "rtsp://" + this.mParameters.host + ":" + this.mParameters.port + this.mParameters.path;
                    String hash1 = computeMd5Hash(new StringBuilder(String.valueOf(this.mParameters.username)).append(":").append(m.group(1)).append(":").append(this.mParameters.password).toString());
                    this.mAuthorization = "Digest username=\"" + this.mParameters.username + "\",realm=\"" + realm + "\",nonce=\"" + nonce + "\",uri=\"" + uri + "\",response=\"" + computeMd5Hash(new StringBuilder(String.valueOf(hash1)).append(":").append(m.group(2)).append(":").append(computeMd5Hash("ANNOUNCE:" + uri)).toString()) + "\"";
                    append = new StringBuilder("ANNOUNCE rtsp://").append(this.mParameters.host).append(":").append(this.mParameters.port).append(this.mParameters.path).append(" RTSP/1.0\r\n").append("CSeq: ");
                    i = this.mCSeq + 1;
                    this.mCSeq = i;
                    request = append.append(i).append("\r\n").append("Content-Length: ").append(body.length()).append("\r\n").append("Authorization: ").append(this.mAuthorization).append("\r\n").append("Session: ").append(this.mSessionID).append("\r\n").append("Content-Type: application/sdp \r\n\r\n").append(body).toString();
                    Log.i(TAG, request.substring(0, request.indexOf("\r\n")));
                    this.mOutputStream.write(request.getBytes(HTTP.UTF_8));
                    this.mOutputStream.flush();
                    if (Response.parseResponse(this.mBufferedReader).status == HttpStatus.SC_UNAUTHORIZED) {
                        throw new RuntimeException("Bad credentials !");
                    }
                } catch (Exception e) {
                    throw new IOException("Invalid response from server");
                }
            } else if (response.status == HttpStatus.SC_FORBIDDEN) {
                throw new RuntimeException("Access forbidden !");
            }
        } catch (Exception e2) {
            throw new IOException("Invalid response from server. Session id: " + this.mSessionID);
        }
    }

    private void sendRequestSetup() throws IllegalStateException, SocketException, IOException {
        for (int i = 0; i < 2; i++) {
            Stream stream = this.mParameters.session.getTrack(i);
            if (stream != null) {
                String params;
                if (this.mParameters.transport == 1) {
                    params = "TCP;interleaved=" + (i * 2) + "-" + ((i * 2) + 1);
                } else {
                    params = "UDP;unicast;client_port=" + ((i * 2) + DeviceOperationRESTServiceProvider.TIMEOUT) + "-" + (((i * 2) + DeviceOperationRESTServiceProvider.TIMEOUT) + 1) + ";mode=receive";
                }
                String request = "SETUP rtsp://" + this.mParameters.host + ":" + this.mParameters.port + this.mParameters.path + "/trackID=" + i + " RTSP/1.0\r\n" + "Transport: RTP/AVP/" + params + "\r\n" + addHeaders();
                Log.i(TAG, request.substring(0, request.indexOf("\r\n")));
                this.mOutputStream.write(request.getBytes(HTTP.UTF_8));
                this.mOutputStream.flush();
                Response response = Response.parseResponse(this.mBufferedReader);
                if (this.mParameters.transport == 0) {
                    try {
                        Matcher m = Response.rexegTransport.matcher((CharSequence) response.headers.get("transport"));
                        m.find();
                        stream.setDestinationPorts(Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4)));
                        Log.d(TAG, "Setting destination ports: " + Integer.parseInt(m.group(3)) + ", " + Integer.parseInt(m.group(4)));
                    } catch (Exception e) {
                        e.printStackTrace();
                        int[] ports = stream.getDestinationPorts();
                        Log.d(TAG, "Server did not specify ports, using default ports: " + ports[0] + "-" + ports[1]);
                    }
                } else {
                    stream.setOutputStream(this.mOutputStream, (byte) (i * 2));
                }
            }
        }
    }

    private void sendRequestRecord() throws IllegalStateException, SocketException, IOException {
        String request = "RECORD rtsp://" + this.mParameters.host + ":" + this.mParameters.port + this.mParameters.path + " RTSP/1.0\r\n" + "Range: npt=0.000-\r\n" + addHeaders();
        Log.i(TAG, request.substring(0, request.indexOf("\r\n")));
        this.mOutputStream.write(request.getBytes(HTTP.UTF_8));
        this.mOutputStream.flush();
        Response.parseResponse(this.mBufferedReader);
    }

    private void sendRequestTeardown() throws IOException {
        String request = "TEARDOWN rtsp://" + this.mParameters.host + ":" + this.mParameters.port + this.mParameters.path + " RTSP/1.0\r\n" + addHeaders();
        Log.i(TAG, request.substring(0, request.indexOf("\r\n")));
        this.mOutputStream.write(request.getBytes(HTTP.UTF_8));
        this.mOutputStream.flush();
    }

    private void sendRequestOption() throws IOException {
        String request = "OPTIONS rtsp://" + this.mParameters.host + ":" + this.mParameters.port + this.mParameters.path + " RTSP/1.0\r\n" + addHeaders();
        Log.i(TAG, request.substring(0, request.indexOf("\r\n")));
        this.mOutputStream.write(request.getBytes(HTTP.UTF_8));
        this.mOutputStream.flush();
        Response.parseResponse(this.mBufferedReader);
    }

    private String addHeaders() {
        StringBuilder stringBuilder = new StringBuilder("CSeq: ");
        int i = this.mCSeq + 1;
        this.mCSeq = i;
        return stringBuilder.append(i).append("\r\n").append("Content-Length: 0\r\n").append("Session: ").append(this.mSessionID).append("\r\n").append(this.mAuthorization != null ? "Authorization: " + this.mAuthorization + "\r\n" : "").append("\r\n").toString();
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[(bytes.length * 2)];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 255;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[(j * 2) + 1] = hexArray[v & 15];
        }
        return new String(hexChars);
    }

    private String computeMd5Hash(String buffer) {
        try {
            return bytesToHex(MessageDigest.getInstance("MD5").digest(buffer.getBytes(HTTP.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            return "";
        } catch (UnsupportedEncodingException e2) {
            return "";
        }
    }

    private void postMessage(final int message) {
        this.mMainHandler.post(new Runnable() {
            public void run() {
                if (RtspClient.this.mCallback != null) {
                    RtspClient.this.mCallback.onRtspUpdate(message, null);
                }
            }
        });
    }

    private void postError(final int message, final Exception e) {
        this.mMainHandler.post(new Runnable() {
            public void run() {
                if (RtspClient.this.mCallback != null) {
                    RtspClient.this.mCallback.onRtspUpdate(message, e);
                }
            }
        });
    }
}
