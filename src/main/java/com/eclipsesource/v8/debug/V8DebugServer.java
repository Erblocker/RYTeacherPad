package com.eclipsesource.v8.debug;

import com.eclipsesource.v8.JavaVoidCallback;
import com.eclipsesource.v8.Releasable;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Function;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.V8Value;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import org.apache.http.protocol.HTTP;

public class V8DebugServer {
    private static final String DEBUG_BREAK_HANDLER = "__j2v8_debug_handler";
    public static String DEBUG_OBJECT_NAME = "__j2v8_Debug";
    private static final String HEADER_EMBEDDING_HOST = "Embedding-Host: ";
    private static final String HEADER_PROTOCOL_VERSION = "Protocol-Version: ";
    private static final String HEADER_TYPE = "Type: ";
    private static final String HEADER_V8_VERSION = "V8-Version: ";
    private static final String J2V8_VERSION = "4.0.0";
    private static final String MAKE_BREAK_EVENT = "__j2v8_MakeBreakEvent";
    private static final String MAKE_COMPILE_EVENT = "__j2v8_MakeCompileEvent";
    private static final int PROTOCOL_BUFFER_SIZE = 4096;
    private static final Charset PROTOCOL_CHARSET = Charset.forName(HTTP.UTF_8);
    private static final byte[] PROTOCOL_CONTENT_LENGTH_BYTES = PROTOCOL_CONTENT_LENGTH_HEADER.getBytes(PROTOCOL_CHARSET);
    private static final String PROTOCOL_CONTENT_LENGTH_HEADER = "Content-Length:";
    private static final String PROTOCOL_EOL = "\r\n";
    private static final byte[] PROTOCOL_EOL_BYTES = PROTOCOL_EOL.getBytes(PROTOCOL_CHARSET);
    private static final String PROTOCOL_VERSION = "1";
    private static final String SET_LISTENER = "setListener";
    private static final String V8_DEBUG_OBJECT = "Debug";
    private static final String V8_VERSION = "4.10.253";
    private Socket client;
    private Object clientLock = new Object();
    private V8Object debugObject;
    private List<String> requests = new LinkedList();
    private V8Object runningStateDcp;
    private V8 runtime;
    private ServerSocket server;
    private V8Object stoppedStateDcp;
    private boolean traceCommunication = false;
    private boolean waitForConnection;

    private class ClientLoop implements Runnable {
        private int from;

        private ClientLoop() {
        }

        public void run() {
            while (true) {
                try {
                    Socket socket = V8DebugServer.this.server.accept();
                    socket.setTcpNoDelay(true);
                    synchronized (V8DebugServer.this.clientLock) {
                        V8DebugServer.this.client = socket;
                        V8DebugServer.this.waitForConnection = false;
                        V8DebugServer.this.clientLock.notifyAll();
                    }
                    startHandshake();
                    processClientRequests();
                } catch (Exception e) {
                    synchronized (V8DebugServer.this.clientLock) {
                        if (V8DebugServer.this.client != null) {
                            try {
                                V8DebugServer.this.client.close();
                            } catch (IOException e2) {
                            }
                            V8DebugServer.this.client = null;
                        }
                        V8DebugServer.this.logError(e);
                    }
                }
            }
        }

        private void startHandshake() throws IOException {
            StringBuilder sb = new StringBuilder();
            sb.append(V8DebugServer.HEADER_V8_VERSION);
            sb.append(V8DebugServer.V8_VERSION);
            sb.append(V8DebugServer.PROTOCOL_EOL);
            sb.append(V8DebugServer.HEADER_PROTOCOL_VERSION);
            sb.append(V8DebugServer.PROTOCOL_VERSION);
            sb.append(V8DebugServer.PROTOCOL_EOL);
            sb.append(V8DebugServer.HEADER_EMBEDDING_HOST);
            sb.append("j2v8 ");
            sb.append(V8DebugServer.J2V8_VERSION);
            sb.append(V8DebugServer.PROTOCOL_EOL);
            sb.append(V8DebugServer.HEADER_TYPE);
            sb.append("connect");
            sb.append(V8DebugServer.PROTOCOL_EOL);
            V8DebugServer.this.sendMessage(sb.toString(), "");
        }

        private void processClientRequests() throws IOException {
            byte[] EMPTY_ARR = new byte[0];
            byte[] buf = new byte[4096];
            int offset = 0;
            boolean toolInfoSkipped = false;
            byte[] messageBytes = EMPTY_ARR;
            int contentLength = -1;
            synchronized (V8DebugServer.this.clientLock) {
                InputStream cIn = V8DebugServer.this.client.getInputStream();
            }
            while (true) {
                int bytesRead = cIn.read(buf, offset, 4096 - offset);
                if (bytesRead > 0) {
                    bytesRead += offset;
                    this.from = 0;
                    do {
                        if (contentLength < 0) {
                            contentLength = readContentLength(buf, bytesRead);
                            if (contentLength < 0) {
                                break;
                            }
                        }
                        if (!toolInfoSkipped) {
                            toolInfoSkipped = skipToolInfo(buf, bytesRead);
                            if (!toolInfoSkipped) {
                                break;
                            }
                        }
                        int length = Math.min(contentLength - messageBytes.length, bytesRead - this.from);
                        messageBytes = join(messageBytes, buf, this.from, length);
                        this.from += length;
                        if (messageBytes.length == contentLength) {
                            String message = new String(messageBytes, V8DebugServer.PROTOCOL_CHARSET);
                            synchronized (V8DebugServer.this.requests) {
                                V8DebugServer.this.requests.add(message);
                            }
                            contentLength = -1;
                            toolInfoSkipped = false;
                            messageBytes = EMPTY_ARR;
                        }
                    } while (this.from < bytesRead);
                    if (this.from < bytesRead) {
                        System.arraycopy(buf, this.from, buf, 0, bytesRead - this.from);
                        offset = bytesRead - this.from;
                    } else {
                        offset = 0;
                    }
                } else {
                    return;
                }
            }
        }

        private int readContentLength(byte[] bytes, int to) throws IOException {
            int i = -1;
            int pos = indexOf(V8DebugServer.PROTOCOL_CONTENT_LENGTH_BYTES, bytes, this.from, to);
            if (pos >= 0) {
                pos += V8DebugServer.PROTOCOL_CONTENT_LENGTH_BYTES.length;
                int end = indexOf(V8DebugServer.PROTOCOL_EOL_BYTES, bytes, pos, to);
                if (end >= 0) {
                    String str = new String(bytes, pos, end - pos, V8DebugServer.PROTOCOL_CHARSET);
                    try {
                        i = Integer.parseInt(str.trim());
                        this.from = V8DebugServer.PROTOCOL_EOL_BYTES.length + end;
                    } catch (Exception e) {
                        throw new IOException("Invalid content length header: '" + str + "' in message" + new String(bytes, V8DebugServer.PROTOCOL_CHARSET));
                    }
                }
            }
            return i;
        }

        private boolean skipToolInfo(byte[] bytes, int n) {
            int end = indexOf(V8DebugServer.PROTOCOL_EOL_BYTES, bytes, this.from, n);
            if (end < 0) {
                return false;
            }
            this.from = V8DebugServer.PROTOCOL_EOL_BYTES.length + end;
            return true;
        }

        private int indexOf(byte[] pattern, byte[] array, int start, int end) {
            int len = pattern.length;
            int i = start;
            while (i < end) {
                int j = 0;
                while (j <= len) {
                    if (j != len) {
                        if (i + j >= end || array[i + j] != pattern[j]) {
                            break;
                        }
                        j++;
                    } else {
                        return i;
                    }
                }
                i++;
            }
            return -1;
        }

        private byte[] join(byte[] arr1, byte[] arr2, int startPos, int length) {
            byte[] res = new byte[(arr1.length + length)];
            System.arraycopy(arr1, 0, res, 0, arr1.length);
            System.arraycopy(arr2, startPos, res, arr1.length, length);
            return res;
        }
    }

    private class EventHandler implements JavaVoidCallback {
        private EventHandler() {
        }

        public void invoke(V8Object receiver, V8Array parameters) {
            if (parameters != null && !parameters.isUndefined()) {
                V8Object execState = null;
                V8Object eventData = null;
                try {
                    int event = parameters.getInteger(0);
                    execState = parameters.getObject(1);
                    eventData = parameters.getObject(2);
                    if (V8DebugServer.this.traceCommunication) {
                        String type = "unknown";
                        switch (event) {
                            case 1:
                                type = "Break";
                                break;
                            case 2:
                                type = "Exception";
                                break;
                            case 3:
                                type = "NewFunction";
                                break;
                            case 4:
                                type = "BeforeCompile";
                                break;
                            case 5:
                                type = "AfterCompile";
                                break;
                            case 6:
                                type = "CompileError";
                                break;
                            case 7:
                                type = "PromiseEvent";
                                break;
                            case 8:
                                type = "AsyncTaskEvent";
                                break;
                        }
                        System.out.println("V8 has emmitted an event of type " + type);
                    }
                    if (V8DebugServer.this.isConnected()) {
                        switch (event) {
                            case 1:
                                V8DebugServer.this.enterBreakLoop(execState, eventData);
                                break;
                            case 5:
                            case 6:
                                V8DebugServer.this.sendCompileEvent(eventData);
                                break;
                        }
                        safeRelease(execState);
                        safeRelease(eventData);
                    }
                } catch (Exception e) {
                    V8DebugServer.this.logError(e);
                } finally {
                    safeRelease(execState);
                    safeRelease(eventData);
                }
            }
        }

        private void safeRelease(Releasable object) {
            if (object != null) {
                object.release();
            }
        }
    }

    private void sendCompileEvent(com.eclipsesource.v8.V8Object r11) throws java.io.IOException {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0006 in list [B:12:0x0071]
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:323)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:226)
*/
        /*
        r10 = this;
        r5 = r10.isConnected();
        if (r5 != 0) goto L_0x0007;
    L_0x0006:
        return;
    L_0x0007:
        r5 = "type_";
        r4 = r11.getInteger(r5);
        r5 = "script_";
        r3 = r11.getObject(r5);
        r0 = 0;
        r2 = new com.eclipsesource.v8.V8Array;
        r5 = r10.runtime;
        r2.<init>(r5);
        r2.push(r3);	 Catch:{ all -> 0x0075 }
        r2.push(r4);	 Catch:{ all -> 0x0075 }
        r5 = r10.debugObject;	 Catch:{ all -> 0x0075 }
        r6 = "__j2v8_MakeCompileEvent";	 Catch:{ all -> 0x0075 }
        r0 = r5.executeObjectFunction(r6, r2);	 Catch:{ all -> 0x0075 }
        r5 = "toJSONProtocol";	 Catch:{ all -> 0x0075 }
        r6 = 0;	 Catch:{ all -> 0x0075 }
        r1 = r0.executeStringFunction(r5, r6);	 Catch:{ all -> 0x0075 }
        r5 = r10.traceCommunication;	 Catch:{ all -> 0x0075 }
        if (r5 == 0) goto L_0x0060;	 Catch:{ all -> 0x0075 }
    L_0x0038:
        r5 = java.lang.System.out;	 Catch:{ all -> 0x0075 }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0075 }
        r6.<init>();	 Catch:{ all -> 0x0075 }
        r7 = "Sending event (CompileEvent):\n";	 Catch:{ all -> 0x0075 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0075 }
        r7 = 0;	 Catch:{ all -> 0x0075 }
        r8 = r1.length();	 Catch:{ all -> 0x0075 }
        r9 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;	 Catch:{ all -> 0x0075 }
        r8 = java.lang.Math.min(r8, r9);	 Catch:{ all -> 0x0075 }
        r7 = r1.substring(r7, r8);	 Catch:{ all -> 0x0075 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0075 }
        r6 = r6.toString();	 Catch:{ all -> 0x0075 }
        r5.println(r6);	 Catch:{ all -> 0x0075 }
    L_0x0060:
        r5 = r1.length();	 Catch:{ all -> 0x0075 }
        if (r5 <= 0) goto L_0x0069;	 Catch:{ all -> 0x0075 }
    L_0x0066:
        r10.sendJson(r1);	 Catch:{ all -> 0x0075 }
    L_0x0069:
        r2.release();
        r3.release();
        if (r0 == 0) goto L_0x0006;
    L_0x0071:
        r0.release();
        goto L_0x0006;
    L_0x0075:
        r5 = move-exception;
        r2.release();
        r3.release();
        if (r0 == 0) goto L_0x0081;
    L_0x007e:
        r0.release();
    L_0x0081:
        throw r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.eclipsesource.v8.debug.V8DebugServer.sendCompileEvent(com.eclipsesource.v8.V8Object):void");
    }

    public static void configureV8ForDebugging() {
        try {
            V8.setFlags("-expose-debug-as=" + DEBUG_OBJECT_NAME);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public V8DebugServer(V8 runtime, int port, boolean waitForConnection) {
        this.runtime = runtime;
        this.waitForConnection = waitForConnection;
        V8Object debugScope = runtime.getObject(DEBUG_OBJECT_NAME);
        if (debugScope == null) {
            System.err.println("Cannot initialize debugger server - global debug object not found.");
            return;
        }
        try {
            this.debugObject = debugScope.getObject(V8_DEBUG_OBJECT);
            runtime.executeVoidScript("(function() {\n " + DEBUG_OBJECT_NAME + ".Debug. " + MAKE_BREAK_EVENT + " = function (break_id,breakpoints_hit) {\n" + "  return new " + DEBUG_OBJECT_NAME + ".BreakEvent(break_id,breakpoints_hit);\n" + " }\n" + " " + DEBUG_OBJECT_NAME + ".Debug. " + MAKE_COMPILE_EVENT + " = function(script,type) {\n" + "  var scripts = " + DEBUG_OBJECT_NAME + ".Debug.scripts()\n" + "  for (var i in scripts) {\n" + "   if (scripts[i].id == script.id()) {\n" + "     return new " + DEBUG_OBJECT_NAME + ".CompileEvent(scripts[i], type);\n" + "   }\n" + "  }\n" + "  return {toJSONProtocol: function() {return ''}}\n" + " }\n" + "})()");
            try {
                this.server = new ServerSocket(port);
            } catch (Exception e) {
                logError(e);
            }
        } finally {
            debugScope.release();
        }
    }

    public int getPort() {
        return (this.server == null || !this.server.isBound()) ? -1 : this.server.getLocalPort();
    }

    public void setTraceCommunication(boolean value) {
        this.traceCommunication = value;
    }

    public void start() {
        if (this.server != null) {
            boolean waitForConnection = this.waitForConnection;
            Thread clientThread = new Thread(new ClientLoop(), "J2V8 Debugger Server");
            clientThread.setDaemon(true);
            clientThread.start();
            setupEventHandler();
            this.runningStateDcp = this.runtime.executeObjectScript("(function() {return new " + DEBUG_OBJECT_NAME + ".DebugCommandProcessor(null, true)})()");
            if (waitForConnection) {
                synchronized (this.clientLock) {
                    while (this.waitForConnection) {
                        try {
                            this.clientLock.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
                try {
                    processRequests(100);
                } catch (InterruptedException e2) {
                }
            }
        }
    }

    public void stop() {
        try {
            this.server.close();
            synchronized (this.clientLock) {
                if (this.client != null) {
                    this.client.close();
                    this.client = null;
                }
            }
        } catch (IOException e) {
            logError(e);
        }
        if (this.runningStateDcp != null) {
            this.runningStateDcp.release();
            this.runningStateDcp = null;
        }
        if (this.debugObject != null) {
            this.debugObject.release();
            this.debugObject = null;
        }
        if (this.stoppedStateDcp != null) {
            this.stoppedStateDcp.release();
            this.stoppedStateDcp = null;
        }
    }

    private void sendJson(String json) throws IOException {
        sendMessage("", json.replace("\\/", "/"));
    }

    protected void logError(Throwable t) {
        t.printStackTrace();
    }

    private void sendMessage(String header, String contents) throws IOException {
        synchronized (this.clientLock) {
            if (isConnected()) {
                byte[] contentBytes = contents.getBytes(PROTOCOL_CHARSET);
                StringBuilder sb = new StringBuilder();
                sb.append(header);
                sb.append(PROTOCOL_CONTENT_LENGTH_HEADER);
                sb.append(Integer.toString(contentBytes.length));
                sb.append(PROTOCOL_EOL);
                sb.append(PROTOCOL_EOL);
                this.client.getOutputStream().write(sb.toString().getBytes(PROTOCOL_CHARSET));
                if (contentBytes.length > 0) {
                    this.client.getOutputStream().write(contentBytes);
                }
            } else {
                throw new IOException("There is no connected client.");
            }
        }
    }

    private boolean isConnected() {
        boolean z;
        synchronized (this.clientLock) {
            z = (this.server == null || this.client == null || !this.client.isConnected()) ? false : true;
        }
        return z;
    }

    public void processRequests(long timeout) throws InterruptedException {
        if (this.server != null) {
            long start = System.currentTimeMillis();
            while (true) {
                String[] reqs;
                synchronized (this.requests) {
                    reqs = (String[]) this.requests.toArray(new String[this.requests.size()]);
                    this.requests.clear();
                }
                for (String req : reqs) {
                    try {
                        processRequest(req);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
                if (reqs.length <= 0) {
                    if (timeout > 0) {
                        Thread.sleep(10);
                    }
                    if (timeout <= 0) {
                        return;
                    }
                    if (start + timeout <= System.currentTimeMillis()) {
                        return;
                    }
                }
            }
        }
    }

    private void processRequest(String message) throws IOException {
        if (this.traceCommunication) {
            System.out.println("Got message: \n" + message.substring(0, Math.min(message.length(), 1000)));
        }
        V8Array params = new V8Array(this.runtime);
        params.push(message);
        V8Object dcp = this.stoppedStateDcp != null ? this.stoppedStateDcp : this.runningStateDcp;
        String json = dcp.executeFunction("processDebugJSONRequest", params).toString();
        if (this.stoppedStateDcp == null && json.contains("\"running\":false")) {
            json = json.replace("\"running\":false", "\"running\":true").replace("\"success\":true", "\"success\":false").replace("{\"", "{\"message\":\"Client requested suspension is not supported on J2V8.\",\"");
            dcp.add("running_", true);
        }
        if (this.traceCommunication) {
            System.out.println("Returning response: \n" + json.substring(0, Math.min(json.length(), 1000)));
        }
        sendJson(json);
    }

    private void setupEventHandler() {
        this.debugObject.registerJavaMethod(new EventHandler(), DEBUG_BREAK_HANDLER);
        V8Function debugHandler = null;
        V8Array parameters = null;
        try {
            debugHandler = (V8Function) this.debugObject.getObject(DEBUG_BREAK_HANDLER);
            parameters = new V8Array(this.runtime).push((V8Value) debugHandler);
            this.debugObject.executeFunction(SET_LISTENER, parameters);
        } finally {
            if (!(debugHandler == null || debugHandler.isReleased())) {
                debugHandler.release();
            }
            if (!(parameters == null || parameters.isReleased())) {
                parameters.release();
            }
        }
    }

    private void enterBreakLoop(V8Object execState, V8Object eventData) throws IOException {
        V8Object event;
        V8Array params;
        V8Value breakpointsHit;
        try {
            params = new V8Array(this.runtime);
            params.push(false);
            this.stoppedStateDcp = execState.executeObjectFunction("debugCommandProcessor", params);
            params.release();
            int breakId = execState.getInteger("break_id");
            breakpointsHit = eventData.getArray("break_points_hit_");
            event = null;
            params = new V8Array(this.runtime);
            params.push(breakId);
            params.push(breakpointsHit);
            event = this.debugObject.executeObjectFunction(MAKE_BREAK_EVENT, params);
            String json = event.executeStringFunction("toJSONProtocol", null);
            if (this.traceCommunication) {
                System.out.println("Sending event (Break):\n" + json);
            }
            sendJson(json);
            params.release();
            breakpointsHit.release();
            if (event != null) {
                event.release();
            }
            while (isConnected() && !this.stoppedStateDcp.executeBooleanFunction("isRunning", null)) {
                try {
                    processRequests(10);
                } catch (InterruptedException e) {
                }
            }
            this.stoppedStateDcp.release();
            this.stoppedStateDcp = null;
        } catch (Throwable th) {
            this.stoppedStateDcp.release();
            this.stoppedStateDcp = null;
        }
    }
}
