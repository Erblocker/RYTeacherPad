package com.eclipsesource.v8;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.http.protocol.HTTP;

public class NodeJS {
    private static final String GLOBAL = "global";
    private static final String NEXT_TICK = "nextTick";
    private static final String NODE = "node";
    private static final String PROCESS = "process";
    private static final String STARTUP_CALLBACK = "__run";
    private static final String STARTUP_SCRIPT = "global.__run(require, exports, module, __filename, __dirname);";
    private static final String STARTUP_SCRIPT_NAME = "startup";
    private static final String TMP_JS_EXT = ".js.tmp";
    private static final String VERSIONS = "versions";
    private String nodeVersion = null;
    private V8Function require;
    private V8 v8;

    public static NodeJS createNodeJS() {
        return createNodeJS(null);
    }

    public String getNodeVersion() {
        if (this.nodeVersion != null) {
            return this.nodeVersion;
        }
        V8Object process = null;
        V8Object versions = null;
        try {
            process = this.v8.getObject(PROCESS);
            versions = process.getObject(VERSIONS);
            this.nodeVersion = versions.getString(NODE);
            return this.nodeVersion;
        } finally {
            safeRelease(process);
            safeRelease(versions);
        }
    }

    public static NodeJS createNodeJS(File file) {
        V8 v8 = V8.createV8Runtime(GLOBAL);
        final NodeJS node = new NodeJS(v8);
        v8.registerJavaMethod(new JavaVoidCallback() {
            public void invoke(V8Object receiver, V8Array parameters) {
                V8Function require = (V8Function) parameters.get(0);
                try {
                    node.init(require.twin());
                } finally {
                    require.release();
                }
            }
        }, STARTUP_CALLBACK);
        File startupScript;
        try {
            startupScript = createTemporaryScriptFile(STARTUP_SCRIPT, STARTUP_SCRIPT_NAME);
            v8.createNodeRuntime(startupScript.getAbsolutePath());
            startupScript.delete();
            if (file != null) {
                node.exec(file);
            }
            return node;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Throwable th) {
            startupScript.delete();
        }
    }

    public V8 getRuntime() {
        return this.v8;
    }

    public boolean handleMessage() {
        this.v8.checkThread();
        return this.v8.pumpMessageLoop();
    }

    public void release() {
        this.v8.checkThread();
        if (!this.require.isReleased()) {
            this.require.release();
        }
        if (!this.v8.isReleased()) {
            this.v8.release();
        }
    }

    public boolean isRunning() {
        this.v8.checkThread();
        return this.v8.isRunning();
    }

    public V8Object require(File file) {
        this.v8.checkThread();
        V8Array requireParams = new V8Array(this.v8);
        try {
            requireParams.push(file.getAbsolutePath());
            V8Object v8Object = (V8Object) this.require.call(null, requireParams);
            return v8Object;
        } finally {
            requireParams.release();
        }
    }

    public void exec(File file) {
        Throwable th;
        V8Value scriptExecution = createScriptExecutionCallback(file);
        V8Object process = null;
        V8Array parameters = null;
        try {
            process = this.v8.getObject(PROCESS);
            V8Array parameters2 = new V8Array(this.v8);
            try {
                parameters2.push(scriptExecution);
                process.executeObjectFunction(NEXT_TICK, parameters2);
                safeRelease(process);
                safeRelease(parameters2);
                safeRelease(scriptExecution);
            } catch (Throwable th2) {
                th = th2;
                parameters = parameters2;
                safeRelease(process);
                safeRelease(parameters);
                safeRelease(scriptExecution);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            safeRelease(process);
            safeRelease(parameters);
            safeRelease(scriptExecution);
            throw th;
        }
    }

    private V8Function createScriptExecutionCallback(final File file) {
        return new V8Function(this.v8, new JavaCallback() {
            public Object invoke(V8Object receiver, V8Array parameters) {
                V8Array requireParams = new V8Array(NodeJS.this.v8);
                try {
                    requireParams.push(file.getAbsolutePath());
                    Object call = NodeJS.this.require.call(null, requireParams);
                    return call;
                } finally {
                    requireParams.release();
                }
            }
        });
    }

    private void safeRelease(Releasable releasable) {
        if (releasable != null) {
            releasable.release();
        }
    }

    private NodeJS(V8 v8) {
        this.v8 = v8;
    }

    private void init(V8Function require) {
        this.require = require;
    }

    private static File createTemporaryScriptFile(String script, String name) throws IOException {
        File tempFile = File.createTempFile(name, TMP_JS_EXT);
        PrintWriter writer = new PrintWriter(tempFile, HTTP.UTF_8);
        try {
            writer.print(script);
            return tempFile;
        } finally {
            writer.close();
        }
    }
}
