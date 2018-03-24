package net.majorkernelpanic.streaming.rtsp;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import org.apache.http.client.methods.HttpOptions;

public class RtspServer extends Service {
    public static final int DEFAULT_RTSP_PORT = 8086;
    public static final int ERROR_BIND_FAILED = 0;
    public static final int ERROR_START_FAILED = 1;
    public static final String KEY_ENABLED = "rtsp_enabled";
    public static final String KEY_PORT = "rtsp_port";
    public static final int MESSAGE_STREAMING_STARTED = 0;
    public static final int MESSAGE_STREAMING_STOPPED = 1;
    public static String SERVER_NAME = "MajorKernelPanic RTSP Server";
    public static final String TAG = "RtspServer";
    private final IBinder mBinder = new LocalBinder();
    protected boolean mEnabled = true;
    private RequestListener mListenerThread;
    private final LinkedList<CallbackListener> mListeners = new LinkedList();
    private OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(RtspServer.KEY_PORT)) {
                int port = Integer.parseInt(sharedPreferences.getString(RtspServer.KEY_PORT, String.valueOf(RtspServer.this.mPort)));
                if (port != RtspServer.this.mPort) {
                    RtspServer.this.mPort = port;
                    RtspServer.this.mRestart = true;
                    RtspServer.this.start();
                }
            } else if (key.equals(RtspServer.KEY_ENABLED)) {
                RtspServer.this.mEnabled = sharedPreferences.getBoolean(RtspServer.KEY_ENABLED, RtspServer.this.mEnabled);
                RtspServer.this.start();
            }
        }
    };
    protected int mPort = DEFAULT_RTSP_PORT;
    private boolean mRestart = false;
    protected SessionBuilder mSessionBuilder;
    protected WeakHashMap<Session, Object> mSessions = new WeakHashMap(2);
    protected SharedPreferences mSharedPreferences;

    public interface CallbackListener {
        void onError(RtspServer rtspServer, Exception exception, int i);

        void onMessage(RtspServer rtspServer, int i);
    }

    public class LocalBinder extends Binder {
        public RtspServer getService() {
            return RtspServer.this;
        }
    }

    static class Request {
        public static final Pattern regexMethod = Pattern.compile("(\\w+) (\\S+) RTSP", 2);
        public static final Pattern rexegHeader = Pattern.compile("(\\S+):(.+)", 2);
        public HashMap<String, String> headers = new HashMap();
        public String method;
        public String uri;

        Request() {
        }

        public static Request parseRequest(BufferedReader input) throws IOException, IllegalStateException, SocketException {
            Request request = new Request();
            String line = input.readLine();
            if (line == null) {
                throw new SocketException("Client disconnected");
            }
            Matcher matcher = regexMethod.matcher(line);
            matcher.find();
            request.method = matcher.group(1);
            request.uri = matcher.group(2);
            while (true) {
                line = input.readLine();
                if (line != null && line.length() > 3) {
                    matcher = rexegHeader.matcher(line);
                    matcher.find();
                    request.headers.put(matcher.group(1).toLowerCase(Locale.US), matcher.group(2));
                } else if (line != null) {
                    throw new SocketException("Client disconnected");
                } else {
                    Log.e(RtspServer.TAG, request.method + " " + request.uri);
                    return request;
                }
            }
            if (line != null) {
                Log.e(RtspServer.TAG, request.method + " " + request.uri);
                return request;
            }
            throw new SocketException("Client disconnected");
        }
    }

    class RequestListener extends Thread implements Runnable {
        private final ServerSocket mServer;

        public RequestListener() throws IOException {
            try {
                this.mServer = new ServerSocket(RtspServer.this.mPort);
                start();
            } catch (BindException e) {
                Log.e(RtspServer.TAG, "Port already in use !");
                RtspServer.this.postError(e, 0);
                throw e;
            }
        }

        public void run() {
            Log.i(RtspServer.TAG, "RTSP server listening on port " + this.mServer.getLocalPort());
            while (!Thread.interrupted()) {
                try {
                    new WorkerThread(this.mServer.accept()).start();
                } catch (SocketException e) {
                } catch (IOException e2) {
                    Log.e(RtspServer.TAG, e2.getMessage());
                }
            }
            Log.i(RtspServer.TAG, "RTSP server stopped !");
        }

        public void kill() {
            try {
                this.mServer.close();
            } catch (IOException e) {
            }
            try {
                join();
            } catch (InterruptedException e2) {
            }
        }
    }

    static class Response {
        public static final String STATUS_BAD_REQUEST = "400 Bad Request";
        public static final String STATUS_INTERNAL_SERVER_ERROR = "500 Internal Server Error";
        public static final String STATUS_NOT_FOUND = "404 Not Found";
        public static final String STATUS_OK = "200 OK";
        public String attributes;
        public String content;
        private final Request mRequest;
        public String status;

        public Response(Request request) {
            this.status = "500 Internal Server Error";
            this.content = "";
            this.attributes = "";
            this.mRequest = request;
        }

        public Response() {
            this.status = "500 Internal Server Error";
            this.content = "";
            this.attributes = "";
            this.mRequest = null;
        }

        public void send(OutputStream output) throws IOException {
            try {
                int seqid = Integer.parseInt(((String) this.mRequest.headers.get("cseq")).replace(" ", ""));
                String response = "RTSP/1.0 " + this.status + "\r\n" + "Server: " + RtspServer.SERVER_NAME + "\r\n" + (seqid >= 0 ? "Cseq: " + seqid + "\r\n" : "") + "Content-Length: " + this.content.length() + "\r\n" + this.attributes + "\r\n" + this.content;
                Log.d(RtspServer.TAG, response.replace("\r", ""));
                output.write(response.getBytes());
            } catch (Exception e) {
                Log.e(RtspServer.TAG, "Error parsing CSeq: " + (e.getMessage() != null ? e.getMessage() : ""));
            }
        }
    }

    class WorkerThread extends Thread implements Runnable {
        private final Socket mClient;
        private final BufferedReader mInput;
        private final OutputStream mOutput;
        private Session mSession = new Session();

        public WorkerThread(Socket client) throws IOException {
            this.mInput = new BufferedReader(new InputStreamReader(client.getInputStream()));
            this.mOutput = client.getOutputStream();
            this.mClient = client;
        }

        public void run() {
            Log.i(RtspServer.TAG, "Connection from " + this.mClient.getInetAddress().getHostAddress());
            while (!Thread.interrupted()) {
                Request request = null;
                Response response = null;
                try {
                    request = Request.parseRequest(this.mInput);
                } catch (SocketException e) {
                } catch (Exception e2) {
                    response = new Response();
                    response.status = "400 Bad Request";
                }
                if (request != null) {
                    try {
                        response = processRequest(request);
                    } catch (Exception e3) {
                        RtspServer.this.postError(e3, 1);
                        Log.e(RtspServer.TAG, e3.getMessage() != null ? e3.getMessage() : "An error occurred");
                        e3.printStackTrace();
                        response = new Response(request);
                    }
                }
                try {
                    response.send(this.mOutput);
                } catch (IOException e4) {
                    Log.e(RtspServer.TAG, "Response was not sent properly");
                }
            }
            boolean streaming = RtspServer.this.isStreaming();
            this.mSession.syncStop();
            if (streaming && !RtspServer.this.isStreaming()) {
                RtspServer.this.postMessage(1);
            }
            this.mSession.release();
            try {
                this.mClient.close();
            } catch (IOException e5) {
            }
            Log.i(RtspServer.TAG, "Client disconnected");
        }

        public Response processRequest(Request request) throws IllegalStateException, IOException {
            Response response = new Response(request);
            if (request.method.equalsIgnoreCase("DESCRIBE")) {
                this.mSession = RtspServer.this.handleRequest(request.uri, this.mClient);
                RtspServer.this.mSessions.put(this.mSession, null);
                this.mSession.syncConfigure();
                String requestContent = this.mSession.getSessionDescription();
                response.attributes = "Content-Base: " + this.mClient.getLocalAddress().getHostAddress() + ":" + this.mClient.getLocalPort() + "/\r\n" + "Content-Type: application/sdp\r\n";
                response.content = requestContent;
                response.status = "200 OK";
            } else if (request.method.equalsIgnoreCase(HttpOptions.METHOD_NAME)) {
                response.status = "200 OK";
                response.attributes = "Public: DESCRIBE,SETUP,TEARDOWN,PLAY,PAUSE\r\n";
                response.status = "200 OK";
            } else if (request.method.equalsIgnoreCase("SETUP")) {
                Matcher m = Pattern.compile("trackID=(\\w+)", 2).matcher(request.uri);
                if (m.find()) {
                    int trackId = Integer.parseInt(m.group(1));
                    if (this.mSession.trackExists(trackId)) {
                        int p1;
                        int p2;
                        m = Pattern.compile("client_port=(\\d+)-(\\d+)", 2).matcher((CharSequence) request.headers.get("transport"));
                        if (m.find()) {
                            p1 = Integer.parseInt(m.group(1));
                            p2 = Integer.parseInt(m.group(2));
                        } else {
                            int[] ports = this.mSession.getTrack(trackId).getDestinationPorts();
                            p1 = ports[0];
                            p2 = ports[1];
                        }
                        int ssrc = this.mSession.getTrack(trackId).getSSRC();
                        int[] src = this.mSession.getTrack(trackId).getLocalPorts();
                        String destination = this.mSession.getDestination();
                        this.mSession.getTrack(trackId).setDestinationPorts(p1, p2);
                        boolean streaming = RtspServer.this.isStreaming();
                        this.mSession.syncStart(trackId);
                        if (!streaming && RtspServer.this.isStreaming()) {
                            RtspServer.this.postMessage(0);
                        }
                        response.attributes = "Transport: RTP/AVP/UDP;" + (InetAddress.getByName(destination).isMulticastAddress() ? "multicast" : "unicast") + ";destination=" + this.mSession.getDestination() + ";client_port=" + p1 + "-" + p2 + ";server_port=" + src[0] + "-" + src[1] + ";ssrc=" + Integer.toHexString(ssrc) + ";mode=play\r\n" + "Session: " + "1185d20035702ca" + "\r\n" + "Cache-Control: no-cache\r\n";
                        response.status = "200 OK";
                        response.status = "200 OK";
                    } else {
                        response.status = "404 Not Found";
                    }
                } else {
                    response.status = "400 Bad Request";
                }
            } else if (request.method.equalsIgnoreCase("PLAY")) {
                String requestAttributes = "RTP-Info: ";
                if (this.mSession.trackExists(0)) {
                    requestAttributes = new StringBuilder(String.valueOf(requestAttributes)).append("url=rtsp://").append(this.mClient.getLocalAddress().getHostAddress()).append(":").append(this.mClient.getLocalPort()).append("/trackID=").append(0).append(";seq=0,").toString();
                }
                if (this.mSession.trackExists(1)) {
                    requestAttributes = new StringBuilder(String.valueOf(requestAttributes)).append("url=rtsp://").append(this.mClient.getLocalAddress().getHostAddress()).append(":").append(this.mClient.getLocalPort()).append("/trackID=").append(1).append(";seq=0,").toString();
                }
                response.attributes = requestAttributes.substring(0, requestAttributes.length() - 1) + "\r\nSession: 1185d20035702ca\r\n";
                response.status = "200 OK";
            } else if (request.method.equalsIgnoreCase("PAUSE")) {
                response.status = "200 OK";
            } else if (request.method.equalsIgnoreCase("TEARDOWN")) {
                response.status = "200 OK";
            } else {
                Log.e(RtspServer.TAG, "Command unknown: " + request);
                response.status = "400 Bad Request";
            }
            return response;
        }
    }

    public void addCallbackListener(CallbackListener listener) {
        synchronized (this.mListeners) {
            if (this.mListeners.size() > 0) {
                Iterator it = this.mListeners.iterator();
                while (it.hasNext()) {
                    if (((CallbackListener) it.next()) == listener) {
                        return;
                    }
                }
            }
            this.mListeners.add(listener);
        }
    }

    public void removeCallbackListener(CallbackListener listener) {
        synchronized (this.mListeners) {
            this.mListeners.remove(listener);
        }
    }

    public int getPort() {
        return this.mPort;
    }

    public void setPort(int port) {
        Editor editor = this.mSharedPreferences.edit();
        editor.putString(KEY_PORT, String.valueOf(port));
        editor.commit();
    }

    public void start() {
        if (!this.mEnabled || this.mRestart) {
            stop();
        }
        if (this.mEnabled && this.mListenerThread == null) {
            try {
                this.mListenerThread = new RequestListener();
            } catch (Exception e) {
                this.mListenerThread = null;
            }
        }
        this.mRestart = false;
    }

    public void stop() {
        if (this.mListenerThread != null) {
            try {
                this.mListenerThread.kill();
                for (Session session : this.mSessions.keySet()) {
                    if (session != null && session.isStreaming()) {
                        session.stop();
                    }
                }
            } catch (Exception e) {
            } finally {
                this.mListenerThread = null;
            }
        }
    }

    public boolean isStreaming() {
        for (Session session : this.mSessions.keySet()) {
            if (session != null && session.isStreaming()) {
                return true;
            }
        }
        return false;
    }

    public boolean isEnabled() {
        return this.mEnabled;
    }

    public long getBitrate() {
        long bitrate = 0;
        for (Session session : this.mSessions.keySet()) {
            if (session != null && session.isStreaming()) {
                bitrate += session.getBitrate();
            }
        }
        return bitrate;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return 1;
    }

    public void onCreate() {
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.mPort = Integer.parseInt(this.mSharedPreferences.getString(KEY_PORT, String.valueOf(this.mPort)));
        this.mEnabled = this.mSharedPreferences.getBoolean(KEY_ENABLED, this.mEnabled);
        this.mSharedPreferences.registerOnSharedPreferenceChangeListener(this.mOnSharedPreferenceChangeListener);
        start();
    }

    public void onDestroy() {
        stop();
        this.mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this.mOnSharedPreferenceChangeListener);
    }

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    protected void postMessage(int id) {
        synchronized (this.mListeners) {
            if (this.mListeners.size() > 0) {
                Iterator it = this.mListeners.iterator();
                while (it.hasNext()) {
                    ((CallbackListener) it.next()).onMessage(this, id);
                }
            }
        }
    }

    protected void postError(Exception exception, int id) {
        synchronized (this.mListeners) {
            if (this.mListeners.size() > 0) {
                Iterator it = this.mListeners.iterator();
                while (it.hasNext()) {
                    ((CallbackListener) it.next()).onError(this, exception, id);
                }
            }
        }
    }

    protected Session handleRequest(String uri, Socket client) throws IllegalStateException, IOException {
        Session session = UriParser.parse(uri);
        session.setOrigin(client.getLocalAddress().getHostAddress());
        if (session.getDestination() == null) {
            session.setDestination(client.getInetAddress().getHostAddress());
        }
        return session;
    }
}
