package com.eclipsesource.v8;

import com.eclipsesource.v8.utils.V8Executor;
import com.eclipsesource.v8.utils.V8Map;
import com.eclipsesource.v8.utils.V8Runnable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class V8 extends V8Object {
    private static boolean initialized = false;
    private static Object invalid = new Object();
    private static Object lock = new Object();
    private static boolean nativeLibraryLoaded = false;
    private static Error nativeLoadError = null;
    private static Exception nativeLoadException = null;
    private static volatile int runtimeCounter = 0;
    private static V8Value undefined = new Undefined();
    private static String v8Flags = null;
    private Map<String, Object> data;
    private V8Map<V8Executor> executors;
    private boolean forceTerminateExecutors;
    private Map<Long, MethodDescriptor> functionRegistry;
    private final V8Locker locker;
    private long objectReferences;
    private LinkedList<ReferenceHandler> referenceHandlers;
    private LinkedList<V8Runnable> releaseHandlers;
    private List<Releasable> resources;
    private long v8RuntimePtr;
    protected Map<Long, V8Value> v8WeakReferences;

    private class MethodDescriptor {
        JavaCallback callback;
        boolean includeReceiver;
        Method method;
        Object object;
        JavaVoidCallback voidCallback;

        private MethodDescriptor() {
        }
    }

    private native void _acquireLock(long j);

    private native void _add(long j, long j2, String str, double d);

    private native void _add(long j, long j2, String str, int i);

    private native void _add(long j, long j2, String str, String str2);

    private native void _add(long j, long j2, String str, boolean z);

    private native void _addArrayBooleanItem(long j, long j2, boolean z);

    private native void _addArrayDoubleItem(long j, long j2, double d);

    private native void _addArrayIntItem(long j, long j2, int i);

    private native void _addArrayNullItem(long j, long j2);

    private native void _addArrayObjectItem(long j, long j2, long j3);

    private native void _addArrayStringItem(long j, long j2, String str);

    private native void _addArrayUndefinedItem(long j, long j2);

    private native void _addNull(long j, long j2, String str);

    private native void _addObject(long j, long j2, String str, long j3);

    private native void _addUndefined(long j, long j2, String str);

    private native Object _arrayGet(long j, int i, long j2, int i2);

    private native boolean _arrayGetBoolean(long j, long j2, int i);

    private native int _arrayGetBooleans(long j, long j2, int i, int i2, boolean[] zArr);

    private native boolean[] _arrayGetBooleans(long j, long j2, int i, int i2);

    private native byte _arrayGetByte(long j, long j2, int i);

    private native int _arrayGetBytes(long j, long j2, int i, int i2, byte[] bArr);

    private native byte[] _arrayGetBytes(long j, long j2, int i, int i2);

    private native double _arrayGetDouble(long j, long j2, int i);

    private native int _arrayGetDoubles(long j, long j2, int i, int i2, double[] dArr);

    private native double[] _arrayGetDoubles(long j, long j2, int i, int i2);

    private native int _arrayGetInteger(long j, long j2, int i);

    private native int _arrayGetIntegers(long j, long j2, int i, int i2, int[] iArr);

    private native int[] _arrayGetIntegers(long j, long j2, int i, int i2);

    private native int _arrayGetSize(long j, long j2);

    private native String _arrayGetString(long j, long j2, int i);

    private native int _arrayGetStrings(long j, long j2, int i, int i2, String[] strArr);

    private native String[] _arrayGetStrings(long j, long j2, int i, int i2);

    private native boolean _contains(long j, long j2, String str);

    private native long _createIsolate(String str);

    private native void _createTwin(long j, long j2, long j3);

    private native ByteBuffer _createV8ArrayBufferBackingStore(long j, long j2, int i);

    private native boolean _equals(long j, long j2, long j3);

    private native boolean _executeBooleanFunction(long j, long j2, String str, long j3);

    private native boolean _executeBooleanScript(long j, String str, String str2, int i);

    private native double _executeDoubleFunction(long j, long j2, String str, long j3);

    private native double _executeDoubleScript(long j, String str, String str2, int i);

    private native Object _executeFunction(long j, int i, long j2, String str, long j3);

    private native Object _executeFunction(long j, long j2, long j3, long j4);

    private native int _executeIntegerFunction(long j, long j2, String str, long j3);

    private native int _executeIntegerScript(long j, String str, String str2, int i);

    private native Object _executeScript(long j, int i, String str, String str2, int i2);

    private native String _executeStringFunction(long j, long j2, String str, long j3);

    private native String _executeStringScript(long j, String str, String str2, int i);

    private native void _executeVoidFunction(long j, long j2, String str, long j3);

    private native void _executeVoidScript(long j, String str, String str2, int i);

    private native Object _get(long j, int i, long j2, String str);

    private native int _getArrayType(long j, long j2);

    private native boolean _getBoolean(long j, long j2, String str);

    private native long _getBuildID();

    private native double _getDouble(long j, long j2, String str);

    private native long _getGlobalObject(long j);

    private native int _getInteger(long j, long j2, String str);

    private native String[] _getKeys(long j, long j2);

    private native String _getString(long j, long j2, String str);

    private native int _getType(long j, long j2);

    private native int _getType(long j, long j2, int i);

    private native int _getType(long j, long j2, int i, int i2);

    private native int _getType(long j, long j2, String str);

    private static native String _getVersion();

    private native int _identityHash(long j, long j2);

    private native long _initNewV8Array(long j);

    private native long _initNewV8ArrayBuffer(long j, int i);

    private native long _initNewV8ArrayBuffer(long j, ByteBuffer byteBuffer, int i);

    private native long _initNewV8Float32Array(long j, long j2, int i, int i2);

    private native long _initNewV8Float64Array(long j, long j2, int i, int i2);

    private native long[] _initNewV8Function(long j);

    private native long _initNewV8Int16Array(long j, long j2, int i, int i2);

    private native long _initNewV8Int32Array(long j, long j2, int i, int i2);

    private native long _initNewV8Int8Array(long j, long j2, int i, int i2);

    private native long _initNewV8Object(long j);

    private native long _initNewV8UInt16Array(long j, long j2, int i, int i2);

    private native long _initNewV8UInt32Array(long j, long j2, int i, int i2);

    private native long _initNewV8UInt8Array(long j, long j2, int i, int i2);

    private native long _initNewV8UInt8ClampedArray(long j, long j2, int i, int i2);

    private static native boolean _isRunning(long j);

    private native boolean _isWeak(long j, long j2);

    private native void _lowMemoryNotification(long j);

    private static native boolean _pumpMessageLoop(long j);

    private native long _registerJavaMethod(long j, long j2, String str, boolean z);

    private native void _release(long j, long j2);

    private native void _releaseLock(long j);

    private native void _releaseMethodDescriptor(long j, long j2);

    private native void _releaseRuntime(long j);

    private native boolean _sameValue(long j, long j2, long j3);

    private static native void _setFlags(String str);

    private native void _setPrototype(long j, long j2, long j3);

    private native void _setWeak(long j, long j2);

    private static native void _startNodeJS(long j, String str);

    private native boolean _strictEquals(long j, long j2, long j3);

    private native void _terminateExecution(long j);

    private native String _toString(long j, long j2);

    private static synchronized void load(String tmpDirectory) {
        synchronized (V8.class) {
            try {
                LibraryLoader.loadLibrary(tmpDirectory);
                nativeLibraryLoaded = true;
            } catch (Error e) {
                nativeLoadError = e;
            } catch (Exception e2) {
                nativeLoadException = e2;
            }
        }
    }

    public static boolean isLoaded() {
        return nativeLibraryLoaded;
    }

    public static void setFlags(String flags) {
        v8Flags = flags;
        initialized = false;
    }

    public static V8 createV8Runtime() {
        return createV8Runtime(null, null);
    }

    public static V8 createV8Runtime(String globalAlias) {
        return createV8Runtime(globalAlias, null);
    }

    public static V8 createV8Runtime(String globalAlias, String tempDirectory) {
        if (!nativeLibraryLoaded) {
            synchronized (lock) {
                if (!nativeLibraryLoaded) {
                    load(tempDirectory);
                }
            }
        }
        checkNativeLibraryLoaded();
        if (!initialized) {
            _setFlags(v8Flags);
            initialized = true;
        }
        V8 runtime = new V8(globalAlias);
        synchronized (lock) {
            runtimeCounter++;
        }
        return runtime;
    }

    public void addReferenceHandler(ReferenceHandler handler) {
        this.referenceHandlers.add(0, handler);
    }

    public void addReleaseHandler(V8Runnable handler) {
        this.releaseHandlers.add(handler);
    }

    public void removeReferenceHandler(ReferenceHandler handler) {
        this.referenceHandlers.remove(handler);
    }

    public void removeReleaseHandler(V8Runnable handler) {
        this.releaseHandlers.remove(handler);
    }

    public synchronized void setData(String key, Object value) {
        if (this.data == null) {
            this.data = new HashMap();
        }
        this.data.put(key, value);
    }

    public Object getData(String key) {
        if (this.data == null) {
            return null;
        }
        return this.data.get(key);
    }

    private void notifyReleaseHandlers(V8 runtime) {
        Iterator it = this.releaseHandlers.iterator();
        while (it.hasNext()) {
            ((V8Runnable) it.next()).run(runtime);
        }
    }

    private void notifyReferenceCreated(V8Value object) {
        Iterator it = this.referenceHandlers.iterator();
        while (it.hasNext()) {
            ((ReferenceHandler) it.next()).v8HandleCreated(object);
        }
    }

    private void notifyReferenceDisposed(V8Value object) {
        Iterator it = this.referenceHandlers.iterator();
        while (it.hasNext()) {
            ((ReferenceHandler) it.next()).v8HandleDisposed(object);
        }
    }

    private static void checkNativeLibraryLoaded() {
        if (!nativeLibraryLoaded) {
            if (nativeLoadError != null) {
                throw new IllegalStateException("J2V8 native library not loaded", nativeLoadError);
            } else if (nativeLoadException != null) {
                throw new IllegalStateException("J2V8 native library not loaded", nativeLoadException);
            } else {
                throw new IllegalStateException("J2V8 native library not loaded");
            }
        }
    }

    protected V8() {
        this(null);
    }

    protected V8(String globalAlias) {
        super(null);
        this.v8WeakReferences = new HashMap();
        this.data = null;
        this.objectReferences = 0;
        this.v8RuntimePtr = 0;
        this.resources = null;
        this.executors = null;
        this.forceTerminateExecutors = false;
        this.functionRegistry = new HashMap();
        this.referenceHandlers = new LinkedList();
        this.releaseHandlers = new LinkedList();
        this.released = false;
        this.v8RuntimePtr = _createIsolate(globalAlias);
        this.locker = new V8Locker(this);
        checkThread();
        this.objectHandle = _getGlobalObject(this.v8RuntimePtr);
    }

    public static V8Value getUndefined() {
        return undefined;
    }

    public static int getActiveRuntimes() {
        return runtimeCounter;
    }

    public long getObjectReferenceCount() {
        return this.objectReferences - ((long) this.v8WeakReferences.size());
    }

    protected long getV8RuntimePtr() {
        return this.v8RuntimePtr;
    }

    public static String getV8Version() {
        return _getVersion();
    }

    public static String getSCMRevision() {
        return "Unknown revision ID";
    }

    public void release() {
        release(true);
    }

    public void terminateExecution() {
        this.forceTerminateExecutors = true;
        terminateExecution(this.v8RuntimePtr);
    }

    public void release(boolean reportMemoryLeaks) {
        if (!isReleased()) {
            checkThread();
            try {
                notifyReleaseHandlers(this);
                releaseResources();
                shutdownExecutors(this.forceTerminateExecutors);
                if (this.executors != null) {
                    this.executors.clear();
                }
                releaseNativeMethodDescriptors();
                synchronized (lock) {
                    runtimeCounter--;
                }
                _releaseRuntime(this.v8RuntimePtr);
                this.v8RuntimePtr = 0;
                this.released = true;
                if (reportMemoryLeaks && getObjectReferenceCount() > 0) {
                    throw new IllegalStateException(this.objectReferences + " Object(s) still exist in runtime");
                }
            } catch (Throwable th) {
                releaseResources();
                shutdownExecutors(this.forceTerminateExecutors);
                if (this.executors != null) {
                    this.executors.clear();
                }
                releaseNativeMethodDescriptors();
                synchronized (lock) {
                    runtimeCounter--;
                    _releaseRuntime(this.v8RuntimePtr);
                    this.v8RuntimePtr = 0;
                    this.released = true;
                    if (reportMemoryLeaks && getObjectReferenceCount() > 0) {
                        IllegalStateException illegalStateException = new IllegalStateException(this.objectReferences + " Object(s) still exist in runtime");
                    }
                }
            }
        }
    }

    private void releaseNativeMethodDescriptors() {
        for (Long nativeMethodDescriptor : this.functionRegistry.keySet()) {
            releaseMethodDescriptor(this.v8RuntimePtr, nativeMethodDescriptor.longValue());
        }
    }

    private void releaseResources() {
        if (this.resources != null) {
            for (Releasable releasable : this.resources) {
                releasable.release();
            }
            this.resources.clear();
            this.resources = null;
        }
    }

    public void registerV8Executor(V8Object key, V8Executor executor) {
        checkThread();
        if (this.executors == null) {
            this.executors = new V8Map();
        }
        this.executors.put((V8Value) key, (Object) executor);
    }

    public V8Executor removeExecutor(V8Object key) {
        checkThread();
        if (this.executors == null) {
            return null;
        }
        return (V8Executor) this.executors.remove(key);
    }

    public V8Executor getExecutor(V8Object key) {
        checkThread();
        if (this.executors == null) {
            return null;
        }
        return (V8Executor) this.executors.get(key);
    }

    public void shutdownExecutors(boolean forceTerminate) {
        checkThread();
        if (this.executors != null) {
            for (V8Executor executor : this.executors.values()) {
                if (forceTerminate) {
                    executor.forceTermination();
                } else {
                    executor.shutdown();
                }
            }
        }
    }

    public void registerResource(Releasable resource) {
        checkThread();
        if (this.resources == null) {
            this.resources = new ArrayList();
        }
        this.resources.add(resource);
    }

    public int executeIntegerScript(String script) {
        return executeIntegerScript(script, null, 0);
    }

    public int executeIntegerScript(String script, String scriptName, int lineNumber) {
        checkThread();
        checkScript(script);
        return executeIntegerScript(this.v8RuntimePtr, script, scriptName, lineNumber);
    }

    protected void createTwin(V8Value value, V8Value twin) {
        checkThread();
        createTwin(this.v8RuntimePtr, value.getHandle(), twin.getHandle());
    }

    public double executeDoubleScript(String script) {
        return executeDoubleScript(script, null, 0);
    }

    public double executeDoubleScript(String script, String scriptName, int lineNumber) {
        checkThread();
        checkScript(script);
        return executeDoubleScript(this.v8RuntimePtr, script, scriptName, lineNumber);
    }

    public String executeStringScript(String script) {
        return executeStringScript(script, null, 0);
    }

    public String executeStringScript(String script, String scriptName, int lineNumber) {
        checkThread();
        checkScript(script);
        return executeStringScript(this.v8RuntimePtr, script, scriptName, lineNumber);
    }

    public boolean executeBooleanScript(String script) {
        return executeBooleanScript(script, null, 0);
    }

    public boolean executeBooleanScript(String script, String scriptName, int lineNumber) {
        checkThread();
        checkScript(script);
        return executeBooleanScript(this.v8RuntimePtr, script, scriptName, lineNumber);
    }

    public V8Array executeArrayScript(String script) {
        return executeArrayScript(script, null, 0);
    }

    public V8Array executeArrayScript(String script, String scriptName, int lineNumber) {
        checkThread();
        Object result = executeScript(script, scriptName, lineNumber);
        if (result instanceof V8Array) {
            return (V8Array) result;
        }
        throw new V8ResultUndefined();
    }

    public Object executeScript(String script) {
        return executeScript(script, null, 0);
    }

    public Object executeScript(String script, String scriptName, int lineNumber) {
        checkThread();
        checkScript(script);
        return executeScript(getV8RuntimePtr(), 0, script, scriptName, lineNumber);
    }

    public V8Object executeObjectScript(String script) {
        return executeObjectScript(script, null, 0);
    }

    public V8Object executeObjectScript(String script, String scriptName, int lineNumber) {
        checkThread();
        Object result = executeScript(script, scriptName, lineNumber);
        if (result instanceof V8Object) {
            return (V8Object) result;
        }
        throw new V8ResultUndefined();
    }

    public void executeVoidScript(String script) {
        executeVoidScript(script, null, 0);
    }

    public void executeVoidScript(String script, String scriptName, int lineNumber) {
        checkThread();
        checkScript(script);
        executeVoidScript(this.v8RuntimePtr, script, scriptName, lineNumber);
    }

    public V8Locker getLocker() {
        return this.locker;
    }

    public long getBuildID() {
        return _getBuildID();
    }

    public void lowMemoryNotification() {
        checkThread();
        lowMemoryNotification(getV8RuntimePtr());
    }

    void checkRuntime(V8Value value) {
        if (value != null && !value.isUndefined()) {
            V8 runtime = value.getRuntime();
            if (runtime == null || runtime.isReleased() || runtime != this) {
                throw new Error("Invalid target runtime");
            }
        }
    }

    void checkThread() {
        this.locker.checkThread();
        if (isReleased()) {
            throw new Error("Runtime disposed error");
        }
    }

    static void checkScript(String script) {
        if (script == null) {
            throw new NullPointerException("Script is null");
        }
    }

    void registerCallback(Object object, Method method, long objectHandle, String jsFunctionName, boolean includeReceiver) {
        MethodDescriptor methodDescriptor = new MethodDescriptor();
        methodDescriptor.object = object;
        methodDescriptor.method = method;
        methodDescriptor.includeReceiver = includeReceiver;
        this.functionRegistry.put(Long.valueOf(registerJavaMethod(getV8RuntimePtr(), objectHandle, jsFunctionName, isVoidMethod(method))), methodDescriptor);
    }

    void registerVoidCallback(JavaVoidCallback callback, long objectHandle, String jsFunctionName) {
        MethodDescriptor methodDescriptor = new MethodDescriptor();
        methodDescriptor.voidCallback = callback;
        this.functionRegistry.put(Long.valueOf(registerJavaMethod(getV8RuntimePtr(), objectHandle, jsFunctionName, true)), methodDescriptor);
    }

    void registerCallback(JavaCallback callback, long objectHandle, String jsFunctionName) {
        createAndRegisterMethodDescriptor(callback, registerJavaMethod(getV8RuntimePtr(), objectHandle, jsFunctionName, false));
    }

    void createAndRegisterMethodDescriptor(JavaCallback callback, long methodID) {
        MethodDescriptor methodDescriptor = new MethodDescriptor();
        methodDescriptor.callback = callback;
        this.functionRegistry.put(Long.valueOf(methodID), methodDescriptor);
    }

    private boolean isVoidMethod(Method method) {
        if (method.getReturnType().equals(Void.TYPE)) {
            return true;
        }
        return false;
    }

    private Object getDefaultValue(Class<?> type) {
        if (type.equals(V8Object.class)) {
            return new Undefined();
        }
        if (type.equals(V8Array.class)) {
            return new Undefined();
        }
        return invalid;
    }

    protected void disposeMethodID(long methodID) {
        this.functionRegistry.remove(Long.valueOf(methodID));
    }

    protected void weakReferenceReleased(long objectID) {
        V8Value v8Value = (V8Value) this.v8WeakReferences.get(Long.valueOf(objectID));
        if (v8Value != null) {
            this.v8WeakReferences.remove(Long.valueOf(objectID));
            try {
                v8Value.release();
            } catch (Exception e) {
            }
        }
    }

    protected Object callObjectJavaMethod(long methodID, V8Object receiver, V8Array parameters) throws Throwable {
        MethodDescriptor methodDescriptor = (MethodDescriptor) this.functionRegistry.get(Long.valueOf(methodID));
        if (methodDescriptor.callback != null) {
            return checkResult(methodDescriptor.callback.invoke(receiver, parameters));
        }
        boolean hasVarArgs = methodDescriptor.method.isVarArgs();
        Object[] args = getArgs(receiver, methodDescriptor, parameters, hasVarArgs);
        checkArgs(args);
        try {
            Object checkResult = checkResult(methodDescriptor.method.invoke(methodDescriptor.object, args));
            releaseArguments(args, hasVarArgs);
            return checkResult;
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (IllegalAccessException e2) {
            throw e2;
        } catch (IllegalArgumentException e3) {
            throw e3;
        } catch (Throwable th) {
            releaseArguments(args, hasVarArgs);
        }
    }

    private Object checkResult(Object result) {
        if (result == null) {
            return result;
        }
        if (result instanceof Float) {
            return Double.valueOf(((Float) result).doubleValue());
        }
        if ((result instanceof Integer) || (result instanceof Double) || (result instanceof Boolean) || (result instanceof String)) {
            return result;
        }
        if (!(result instanceof V8Value)) {
            throw new V8RuntimeException("Unknown return type: " + result.getClass());
        } else if (!((V8Value) result).isReleased()) {
            return result;
        } else {
            throw new V8RuntimeException("V8Value already released");
        }
    }

    protected void callVoidJavaMethod(long methodID, V8Object receiver, V8Array parameters) throws Throwable {
        MethodDescriptor methodDescriptor = (MethodDescriptor) this.functionRegistry.get(Long.valueOf(methodID));
        if (methodDescriptor.voidCallback != null) {
            methodDescriptor.voidCallback.invoke(receiver, parameters);
            return;
        }
        boolean hasVarArgs = methodDescriptor.method.isVarArgs();
        Object[] args = getArgs(receiver, methodDescriptor, parameters, hasVarArgs);
        checkArgs(args);
        try {
            methodDescriptor.method.invoke(methodDescriptor.object, args);
            releaseArguments(args, hasVarArgs);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (IllegalAccessException e2) {
            throw e2;
        } catch (IllegalArgumentException e3) {
            throw e3;
        } catch (Throwable th) {
            releaseArguments(args, hasVarArgs);
        }
    }

    private void checkArgs(Object[] args) {
        for (Object argument : args) {
            if (argument == invalid) {
                throw new IllegalArgumentException("argument type mismatch");
            }
        }
    }

    private void releaseArguments(Object[] args, boolean hasVarArgs) {
        if (hasVarArgs && args.length > 0 && (args[args.length - 1] instanceof Object[])) {
            for (Object object : (Object[]) args[args.length - 1]) {
                if (object instanceof V8Value) {
                    ((V8Value) object).release();
                }
            }
        }
        for (Object arg : args) {
            if (arg instanceof V8Value) {
                ((V8Value) arg).release();
            }
        }
    }

    private Object[] getArgs(V8Object receiver, MethodDescriptor methodDescriptor, V8Array parameters, boolean hasVarArgs) {
        int varArgIndex;
        int numberOfParameters = methodDescriptor.method.getParameterTypes().length;
        if (hasVarArgs) {
            varArgIndex = numberOfParameters - 1;
        } else {
            varArgIndex = numberOfParameters;
        }
        Object[] args = setDefaultValues(new Object[numberOfParameters], methodDescriptor.method.getParameterTypes(), receiver, methodDescriptor.includeReceiver);
        List<Object> varArgs = new ArrayList();
        populateParamters(parameters, varArgIndex, args, varArgs, methodDescriptor.includeReceiver);
        if (hasVarArgs) {
            Object varArgContainer = getVarArgContainer(methodDescriptor.method.getParameterTypes(), varArgs.size());
            System.arraycopy(varArgs.toArray(), 0, varArgContainer, 0, varArgs.size());
            args[varArgIndex] = varArgContainer;
        }
        return args;
    }

    private Object getVarArgContainer(Class<?>[] parameterTypes, int size) {
        Class<?> clazz = parameterTypes[parameterTypes.length - 1];
        if (clazz.isArray()) {
            clazz = clazz.getComponentType();
        }
        return Array.newInstance(clazz, size);
    }

    private void populateParamters(V8Array parameters, int varArgIndex, Object[] args, List<Object> varArgs, boolean includeReceiver) {
        int start = 0;
        if (includeReceiver) {
            start = 1;
        }
        for (int i = start; i < parameters.length() + start; i++) {
            if (i >= varArgIndex) {
                varArgs.add(getArrayItem(parameters, i - start));
            } else {
                args[i] = getArrayItem(parameters, i - start);
            }
        }
    }

    private Object[] setDefaultValues(Object[] parameters, Class<?>[] parameterTypes, V8Object receiver, boolean includeReceiver) {
        int start = 0;
        if (includeReceiver) {
            start = 1;
            parameters[0] = receiver;
        }
        for (int i = start; i < parameters.length; i++) {
            parameters[i] = getDefaultValue(parameterTypes[i]);
        }
        return parameters;
    }

    private Object getArrayItem(V8Array array, int index) {
        try {
            switch (array.getType(index)) {
                case 1:
                    return Integer.valueOf(array.getInteger(index));
                case 2:
                    return Double.valueOf(array.getDouble(index));
                case 3:
                    return Boolean.valueOf(array.getBoolean(index));
                case 4:
                    return array.getString(index);
                case 5:
                case 8:
                    return array.getArray(index);
                case 6:
                    return array.getObject(index);
                case 7:
                    return array.getObject(index);
                case 10:
                    return array.get(index);
                case 99:
                    return getUndefined();
            }
        } catch (V8ResultUndefined e) {
        }
        return null;
    }

    void createNodeRuntime(String fileName) {
        _startNodeJS(this.v8RuntimePtr, fileName);
    }

    boolean pumpMessageLoop() {
        return _pumpMessageLoop(this.v8RuntimePtr);
    }

    boolean isRunning() {
        return _isRunning(this.v8RuntimePtr);
    }

    protected long initNewV8Object(long v8RuntimePtr) {
        return _initNewV8Object(v8RuntimePtr);
    }

    protected void acquireLock(long v8RuntimePtr) {
        _acquireLock(v8RuntimePtr);
    }

    protected void releaseLock(long v8RuntimePtr) {
        _releaseLock(v8RuntimePtr);
    }

    protected void lowMemoryNotification(long v8RuntimePtr) {
        _lowMemoryNotification(v8RuntimePtr);
    }

    protected void createTwin(long v8RuntimePtr, long objectHandle, long twinHandle) {
        _createTwin(v8RuntimePtr, objectHandle, twinHandle);
    }

    protected int executeIntegerScript(long v8RuntimePtr, String script, String scriptName, int lineNumber) {
        return _executeIntegerScript(v8RuntimePtr, script, scriptName, lineNumber);
    }

    protected double executeDoubleScript(long v8RuntimePtr, String script, String scriptName, int lineNumber) {
        return _executeDoubleScript(v8RuntimePtr, script, scriptName, lineNumber);
    }

    protected String executeStringScript(long v8RuntimePtr, String script, String scriptName, int lineNumber) {
        return _executeStringScript(v8RuntimePtr, script, scriptName, lineNumber);
    }

    protected boolean executeBooleanScript(long v8RuntimePtr, String script, String scriptName, int lineNumber) {
        return _executeBooleanScript(v8RuntimePtr, script, scriptName, lineNumber);
    }

    protected Object executeScript(long v8RuntimePtr, int expectedType, String script, String scriptName, int lineNumber) {
        return _executeScript(v8RuntimePtr, expectedType, script, scriptName, lineNumber);
    }

    protected void executeVoidScript(long v8RuntimePtr, String script, String scriptName, int lineNumber) {
        _executeVoidScript(v8RuntimePtr, script, scriptName, lineNumber);
    }

    protected void setWeak(long v8RuntimePtr, long objectHandle) {
        _setWeak(v8RuntimePtr, objectHandle);
    }

    protected boolean isWeak(long v8RuntimePtr, long objectHandle) {
        return _isWeak(v8RuntimePtr, objectHandle);
    }

    protected void release(long v8RuntimePtr, long objectHandle) {
        _release(v8RuntimePtr, objectHandle);
    }

    protected boolean contains(long v8RuntimePtr, long objectHandle, String key) {
        return _contains(v8RuntimePtr, objectHandle, key);
    }

    protected String[] getKeys(long v8RuntimePtr, long objectHandle) {
        return _getKeys(v8RuntimePtr, objectHandle);
    }

    protected int getInteger(long v8RuntimePtr, long objectHandle, String key) {
        return _getInteger(v8RuntimePtr, objectHandle, key);
    }

    protected boolean getBoolean(long v8RuntimePtr, long objectHandle, String key) {
        return _getBoolean(v8RuntimePtr, objectHandle, key);
    }

    protected double getDouble(long v8RuntimePtr, long objectHandle, String key) {
        return _getDouble(v8RuntimePtr, objectHandle, key);
    }

    protected String getString(long v8RuntimePtr, long objectHandle, String key) {
        return _getString(v8RuntimePtr, objectHandle, key);
    }

    protected Object get(long v8RuntimePtr, int expectedType, long objectHandle, String key) {
        return _get(v8RuntimePtr, expectedType, objectHandle, key);
    }

    protected int executeIntegerFunction(long v8RuntimePtr, long objectHandle, String name, long parametersHandle) {
        return _executeIntegerFunction(v8RuntimePtr, objectHandle, name, parametersHandle);
    }

    protected double executeDoubleFunction(long v8RuntimePtr, long objectHandle, String name, long parametersHandle) {
        return _executeDoubleFunction(v8RuntimePtr, objectHandle, name, parametersHandle);
    }

    protected String executeStringFunction(long v8RuntimePtr, long handle, String name, long parametersHandle) {
        return _executeStringFunction(v8RuntimePtr, handle, name, parametersHandle);
    }

    protected boolean executeBooleanFunction(long v8RuntimePtr, long handle, String name, long parametersHandle) {
        return _executeBooleanFunction(v8RuntimePtr, handle, name, parametersHandle);
    }

    protected Object executeFunction(long v8RuntimePtr, int expectedType, long objectHandle, String name, long parametersHandle) {
        return _executeFunction(v8RuntimePtr, expectedType, objectHandle, name, parametersHandle);
    }

    protected Object executeFunction(long v8RuntimePtr, long receiverHandle, long functionHandle, long parametersHandle) {
        return _executeFunction(v8RuntimePtr, receiverHandle, functionHandle, parametersHandle);
    }

    protected void executeVoidFunction(long v8RuntimePtr, long objectHandle, String name, long parametersHandle) {
        _executeVoidFunction(v8RuntimePtr, objectHandle, name, parametersHandle);
    }

    protected boolean equals(long v8RuntimePtr, long objectHandle, long that) {
        return _equals(v8RuntimePtr, objectHandle, that);
    }

    protected String toString(long v8RuntimePtr, long objectHandle) {
        return _toString(v8RuntimePtr, objectHandle);
    }

    protected boolean strictEquals(long v8RuntimePtr, long objectHandle, long that) {
        return _strictEquals(v8RuntimePtr, objectHandle, that);
    }

    protected boolean sameValue(long v8RuntimePtr, long objectHandle, long that) {
        return _sameValue(v8RuntimePtr, objectHandle, that);
    }

    protected int identityHash(long v8RuntimePtr, long objectHandle) {
        return _identityHash(v8RuntimePtr, objectHandle);
    }

    protected void add(long v8RuntimePtr, long objectHandle, String key, int value) {
        _add(v8RuntimePtr, objectHandle, key, value);
    }

    protected void addObject(long v8RuntimePtr, long objectHandle, String key, long value) {
        _addObject(v8RuntimePtr, objectHandle, key, value);
    }

    protected void add(long v8RuntimePtr, long objectHandle, String key, boolean value) {
        _add(v8RuntimePtr, objectHandle, key, value);
    }

    protected void add(long v8RuntimePtr, long objectHandle, String key, double value) {
        _add(v8RuntimePtr, objectHandle, key, value);
    }

    protected void add(long v8RuntimePtr, long objectHandle, String key, String value) {
        _add(v8RuntimePtr, objectHandle, key, value);
    }

    protected void addUndefined(long v8RuntimePtr, long objectHandle, String key) {
        _addUndefined(v8RuntimePtr, objectHandle, key);
    }

    protected void addNull(long v8RuntimePtr, long objectHandle, String key) {
        _addNull(v8RuntimePtr, objectHandle, key);
    }

    protected long registerJavaMethod(long v8RuntimePtr, long objectHandle, String functionName, boolean voidMethod) {
        return _registerJavaMethod(v8RuntimePtr, objectHandle, functionName, voidMethod);
    }

    protected long initNewV8ArrayBuffer(long v8RuntimePtr, ByteBuffer buffer, int capacity) {
        return _initNewV8ArrayBuffer(v8RuntimePtr, buffer, capacity);
    }

    protected long initNewV8ArrayBuffer(long v8RuntimePtr, int capacity) {
        return _initNewV8ArrayBuffer(v8RuntimePtr, capacity);
    }

    public long initNewV8Int32Array(long runtimePtr, long bufferHandle, int offset, int size) {
        return _initNewV8Int32Array(runtimePtr, bufferHandle, offset, size);
    }

    public long initNewV8Float32Array(long runtimePtr, long bufferHandle, int offset, int size) {
        return _initNewV8Float32Array(runtimePtr, bufferHandle, offset, size);
    }

    public long initNewV8Float64Array(long runtimePtr, long bufferHandle, int offset, int size) {
        return _initNewV8Float64Array(runtimePtr, bufferHandle, offset, size);
    }

    public long initNewV8UInt32Array(long runtimePtr, long bufferHandle, int offset, int size) {
        return _initNewV8UInt32Array(runtimePtr, bufferHandle, offset, size);
    }

    public long initNewV8UInt16Array(long runtimePtr, long bufferHandle, int offset, int size) {
        return _initNewV8UInt16Array(runtimePtr, bufferHandle, offset, size);
    }

    public long initNewV8Int16Array(long runtimePtr, long bufferHandle, int offset, int size) {
        return _initNewV8Int16Array(runtimePtr, bufferHandle, offset, size);
    }

    public long initNewV8UInt8Array(long runtimePtr, long bufferHandle, int offset, int size) {
        return _initNewV8UInt8Array(runtimePtr, bufferHandle, offset, size);
    }

    public long initNewV8Int8Array(long runtimePtr, long bufferHandle, int offset, int size) {
        return _initNewV8Int8Array(runtimePtr, bufferHandle, offset, size);
    }

    public long initNewV8UInt8ClampedArray(long runtimePtr, long bufferHandle, int offset, int size) {
        return _initNewV8UInt8ClampedArray(runtimePtr, bufferHandle, offset, size);
    }

    protected ByteBuffer createV8ArrayBufferBackingStore(long v8RuntimePtr, long objectHandle, int capacity) {
        return _createV8ArrayBufferBackingStore(v8RuntimePtr, objectHandle, capacity);
    }

    protected long initNewV8Array(long v8RuntimePtr) {
        return _initNewV8Array(v8RuntimePtr);
    }

    protected long[] initNewV8Function(long v8RuntimePtr) {
        checkThread();
        return _initNewV8Function(v8RuntimePtr);
    }

    protected int arrayGetSize(long v8RuntimePtr, long arrayHandle) {
        return _arrayGetSize(v8RuntimePtr, arrayHandle);
    }

    protected int arrayGetInteger(long v8RuntimePtr, long arrayHandle, int index) {
        return _arrayGetInteger(v8RuntimePtr, arrayHandle, index);
    }

    protected boolean arrayGetBoolean(long v8RuntimePtr, long arrayHandle, int index) {
        return _arrayGetBoolean(v8RuntimePtr, arrayHandle, index);
    }

    protected byte arrayGetByte(long v8RuntimePtr, long arrayHandle, int index) {
        return _arrayGetByte(v8RuntimePtr, arrayHandle, index);
    }

    protected double arrayGetDouble(long v8RuntimePtr, long arrayHandle, int index) {
        return _arrayGetDouble(v8RuntimePtr, arrayHandle, index);
    }

    protected String arrayGetString(long v8RuntimePtr, long arrayHandle, int index) {
        return _arrayGetString(v8RuntimePtr, arrayHandle, index);
    }

    protected Object arrayGet(long v8RuntimePtr, int expectedType, long arrayHandle, int index) {
        return _arrayGet(v8RuntimePtr, expectedType, arrayHandle, index);
    }

    protected void addArrayIntItem(long v8RuntimePtr, long arrayHandle, int value) {
        _addArrayIntItem(v8RuntimePtr, arrayHandle, value);
    }

    protected void addArrayBooleanItem(long v8RuntimePtr, long arrayHandle, boolean value) {
        _addArrayBooleanItem(v8RuntimePtr, arrayHandle, value);
    }

    protected void addArrayDoubleItem(long v8RuntimePtr, long arrayHandle, double value) {
        _addArrayDoubleItem(v8RuntimePtr, arrayHandle, value);
    }

    protected void addArrayStringItem(long v8RuntimePtr, long arrayHandle, String value) {
        _addArrayStringItem(v8RuntimePtr, arrayHandle, value);
    }

    protected void addArrayObjectItem(long v8RuntimePtr, long arrayHandle, long value) {
        _addArrayObjectItem(v8RuntimePtr, arrayHandle, value);
    }

    protected void addArrayUndefinedItem(long v8RuntimePtr, long arrayHandle) {
        _addArrayUndefinedItem(v8RuntimePtr, arrayHandle);
    }

    protected void addArrayNullItem(long v8RuntimePtr, long arrayHandle) {
        _addArrayNullItem(v8RuntimePtr, arrayHandle);
    }

    protected int getType(long v8RuntimePtr, long objectHandle) {
        return _getType(v8RuntimePtr, objectHandle);
    }

    protected int getType(long v8RuntimePtr, long objectHandle, String key) {
        return _getType(v8RuntimePtr, objectHandle, key);
    }

    protected int getType(long v8RuntimePtr, long objectHandle, int index) {
        return _getType(v8RuntimePtr, objectHandle, index);
    }

    protected int getArrayType(long v8RuntimePtr, long objectHandle) {
        return _getArrayType(v8RuntimePtr, objectHandle);
    }

    protected int getType(long v8RuntimePtr, long objectHandle, int index, int length) {
        return _getType(v8RuntimePtr, objectHandle, index, length);
    }

    protected void setPrototype(long v8RuntimePtr, long objectHandle, long prototypeHandle) {
        _setPrototype(v8RuntimePtr, objectHandle, prototypeHandle);
    }

    protected int[] arrayGetIntegers(long v8RuntimePtr, long objectHandle, int index, int length) {
        return _arrayGetIntegers(v8RuntimePtr, objectHandle, index, length);
    }

    protected double[] arrayGetDoubles(long v8RuntimePtr, long objectHandle, int index, int length) {
        return _arrayGetDoubles(v8RuntimePtr, objectHandle, index, length);
    }

    protected boolean[] arrayGetBooleans(long v8RuntimePtr, long objectHandle, int index, int length) {
        return _arrayGetBooleans(v8RuntimePtr, objectHandle, index, length);
    }

    protected byte[] arrayGetBytes(long v8RuntimePtr, long objectHandle, int index, int length) {
        return _arrayGetBytes(v8RuntimePtr, objectHandle, index, length);
    }

    protected String[] arrayGetStrings(long v8RuntimePtr, long objectHandle, int index, int length) {
        return _arrayGetStrings(v8RuntimePtr, objectHandle, index, length);
    }

    protected int arrayGetIntegers(long v8RuntimePtr, long objectHandle, int index, int length, int[] resultArray) {
        return _arrayGetIntegers(v8RuntimePtr, objectHandle, index, length, resultArray);
    }

    protected int arrayGetDoubles(long v8RuntimePtr, long objectHandle, int index, int length, double[] resultArray) {
        return _arrayGetDoubles(v8RuntimePtr, objectHandle, index, length, resultArray);
    }

    protected int arrayGetBooleans(long v8RuntimePtr, long objectHandle, int index, int length, boolean[] resultArray) {
        return _arrayGetBooleans(v8RuntimePtr, objectHandle, index, length, resultArray);
    }

    protected int arrayGetBytes(long v8RuntimePtr, long objectHandle, int index, int length, byte[] resultArray) {
        return _arrayGetBytes(v8RuntimePtr, objectHandle, index, length, resultArray);
    }

    protected int arrayGetStrings(long v8RuntimePtr, long objectHandle, int index, int length, String[] resultArray) {
        return _arrayGetStrings(v8RuntimePtr, objectHandle, index, length, resultArray);
    }

    protected void terminateExecution(long v8RuntimePtr) {
        _terminateExecution(v8RuntimePtr);
    }

    protected void releaseMethodDescriptor(long v8RuntimePtr, long methodDescriptor) {
        _releaseMethodDescriptor(v8RuntimePtr, methodDescriptor);
    }

    void addObjRef(V8Value reference) {
        this.objectReferences++;
        if (!this.referenceHandlers.isEmpty()) {
            notifyReferenceCreated(reference);
        }
    }

    void releaseObjRef(V8Value reference) {
        if (!this.referenceHandlers.isEmpty()) {
            notifyReferenceDisposed(reference);
        }
        this.objectReferences--;
    }
}
