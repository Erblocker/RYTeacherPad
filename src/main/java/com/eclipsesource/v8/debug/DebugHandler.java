package com.eclipsesource.v8.debug;

import com.eclipsesource.v8.JavaVoidCallback;
import com.eclipsesource.v8.Releasable;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Function;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.V8Value;
import java.util.ArrayList;
import java.util.List;

public class DebugHandler implements Releasable {
    private static final String CHANGE_BREAK_POINT_CONDITION = "changeBreakPointCondition";
    private static final String CLEAR_BREAK_POINT = "clearBreakPoint";
    private static final String DEBUG_BREAK_HANDLER = "__j2v8_debug_handler";
    public static String DEBUG_OBJECT_NAME = "__j2v8_Debug";
    private static final String DISABLE_ALL_BREAK_POINTS = "disableAllBreakPoints";
    private static final String DISABLE_SCRIPT_BREAK_POINT = "disableScriptBreakPoint";
    private static final String ENABLE_SCRIPT_BREAK_POINT = "enableScriptBreakPoint";
    private static final String FIND_SCRIPT_BREAK_POINT = "findScriptBreakPoint";
    private static final String NUMBER = "number";
    private static final String SCRIPT_BREAK_POINTS = "scriptBreakPoints";
    private static final String SET_BREAK_POINT = "setBreakPoint";
    private static final String SET_LISTENER = "setListener";
    private static final String SET_SCRIPT_BREAK_POINT_BY_NAME = "setScriptBreakPointByName";
    private static final String V8_DEBUG_OBJECT = "Debug";
    private List<BreakHandler> breakHandlers = new ArrayList();
    private V8Object debugObject;
    private V8 runtime;

    public enum DebugEvent {
        Undefined(0),
        Break(1),
        Exception(2),
        NewFunction(3),
        BeforeCompile(4),
        AfterCompile(5),
        CompileError(6),
        PromiseError(7),
        AsyncTaskEvent(8);
        
        int index;

        private DebugEvent(int index) {
            this.index = index;
        }
    }

    private class BreakpointHandler implements JavaVoidCallback {
        private BreakpointHandler() {
        }

        public void invoke(V8Object receiver, V8Array parameters) {
            if (parameters != null && !parameters.isUndefined()) {
                int event = parameters.getInteger(0);
                for (BreakHandler handler : DebugHandler.this.breakHandlers) {
                    invokeHandler(parameters, event, handler);
                }
            }
        }

        private void invokeHandler(V8Array parameters, int event, BreakHandler handler) {
            Throwable th;
            V8Object execState = null;
            V8Object eventData = null;
            V8Object data = null;
            ExecutionState state = null;
            EventData typedEventData = null;
            try {
                execState = parameters.getObject(1);
                eventData = parameters.getObject(2);
                data = parameters.getObject(3);
                ExecutionState state2 = new ExecutionState(execState);
                try {
                    DebugEvent type = DebugEvent.values()[event];
                    typedEventData = createDebugEvent(type, eventData);
                    handler.onBreak(type, state2, typedEventData, data);
                    safeRelease(execState);
                    safeRelease(eventData);
                    safeRelease(data);
                    safeRelease(state2);
                    safeRelease(typedEventData);
                } catch (Throwable th2) {
                    th = th2;
                    state = state2;
                    safeRelease(execState);
                    safeRelease(eventData);
                    safeRelease(data);
                    safeRelease(state);
                    safeRelease(typedEventData);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                safeRelease(execState);
                safeRelease(eventData);
                safeRelease(data);
                safeRelease(state);
                safeRelease(typedEventData);
                throw th;
            }
        }

        private EventData createDebugEvent(DebugEvent type, V8Object eventData) {
            switch (type) {
                case Break:
                    return new BreakEvent(eventData);
                case BeforeCompile:
                    return new CompileEvent(eventData);
                case AfterCompile:
                    return new CompileEvent(eventData);
                case Exception:
                    return new ExceptionEvent(eventData);
                default:
                    return new EventData(eventData);
            }
        }

        private void safeRelease(Releasable object) {
            if (object != null) {
                object.release();
            }
        }
    }

    public DebugHandler(V8 runtime) {
        this.runtime = runtime;
        setupDebugObject(runtime);
        setupBreakpointHandler();
    }

    public void addBreakHandler(BreakHandler handler) {
        this.runtime.getLocker().checkThread();
        this.breakHandlers.add(handler);
    }

    public void removeBreakHandler(BreakHandler handler) {
        this.runtime.getLocker().checkThread();
        this.breakHandlers.remove(handler);
    }

    public int setBreakpoint(V8Function function) {
        V8Array parameters = new V8Array(this.runtime);
        parameters.push((V8Value) function);
        try {
            int executeIntegerFunction = this.debugObject.executeIntegerFunction(SET_BREAK_POINT, parameters);
            return executeIntegerFunction;
        } finally {
            parameters.release();
        }
    }

    public int setScriptBreakpoint(String scriptID, int lineNumber) {
        V8Array parameters = new V8Array(this.runtime);
        parameters.push(scriptID);
        parameters.push(lineNumber);
        try {
            int executeIntegerFunction = this.debugObject.executeIntegerFunction(SET_SCRIPT_BREAK_POINT_BY_NAME, parameters);
            return executeIntegerFunction;
        } finally {
            parameters.release();
        }
    }

    public void enableScriptBreakPoint(int breakpointID) {
        V8Array parameters = new V8Array(this.runtime);
        parameters.push(breakpointID);
        try {
            this.debugObject.executeVoidFunction(ENABLE_SCRIPT_BREAK_POINT, parameters);
        } finally {
            parameters.release();
        }
    }

    public void disableScriptBreakPoint(int breakpointID) {
        V8Array parameters = new V8Array(this.runtime);
        parameters.push(breakpointID);
        try {
            this.debugObject.executeVoidFunction(DISABLE_SCRIPT_BREAK_POINT, parameters);
        } finally {
            parameters.release();
        }
    }

    public void clearBreakPoint(int breakpointID) {
        V8Array parameters = new V8Array(this.runtime);
        parameters.push(breakpointID);
        try {
            this.debugObject.executeVoidFunction(CLEAR_BREAK_POINT, parameters);
        } finally {
            parameters.release();
        }
    }

    public void disableAllBreakPoints() {
        this.debugObject.executeVoidFunction(DISABLE_ALL_BREAK_POINTS, null);
    }

    public int getScriptBreakPointCount() {
        V8Array breakPoints = this.debugObject.executeArrayFunction(SCRIPT_BREAK_POINTS, null);
        try {
            int length = breakPoints.length();
            return length;
        } finally {
            breakPoints.release();
        }
    }

    public int[] getScriptBreakPointIDs() {
        V8Array breakPoints = this.debugObject.executeArrayFunction(SCRIPT_BREAK_POINTS, null);
        int[] result = new int[breakPoints.length()];
        int i = 0;
        while (i < breakPoints.length()) {
            V8Object breakPoint = breakPoints.getObject(i);
            try {
                result[i] = breakPoint.executeIntegerFunction(NUMBER, null);
                breakPoint.release();
                i++;
            } catch (Throwable th) {
                breakPoints.release();
            }
        }
        breakPoints.release();
        return result;
    }

    public ScriptBreakPoint getScriptBreakPoint(int breakPointID) {
        V8Array parameters = new V8Array(this.runtime);
        parameters.push(breakPointID);
        parameters.push(false);
        V8Object scriptBreakPoint = null;
        try {
            scriptBreakPoint = this.debugObject.executeObjectFunction(FIND_SCRIPT_BREAK_POINT, parameters);
            ScriptBreakPoint scriptBreakPoint2 = new ScriptBreakPoint(scriptBreakPoint);
            return scriptBreakPoint2;
        } finally {
            parameters.release();
            if (scriptBreakPoint != null) {
                scriptBreakPoint.release();
            }
        }
    }

    public void changeBreakPointCondition(int breakpointID, String condition) {
        V8Array parameters = new V8Array(this.runtime);
        parameters.push(breakpointID);
        parameters.push(condition);
        try {
            this.debugObject.executeVoidFunction(CHANGE_BREAK_POINT_CONDITION, parameters);
        } finally {
            parameters.release();
        }
    }

    public void release() {
        this.debugObject.release();
    }

    private void setupDebugObject(V8 runtime) {
        V8Object outerDebug = runtime.getObject(DEBUG_OBJECT_NAME);
        try {
            this.debugObject = outerDebug.getObject(V8_DEBUG_OBJECT);
        } finally {
            outerDebug.release();
        }
    }

    private void setupBreakpointHandler() {
        this.debugObject.registerJavaMethod(new BreakpointHandler(), DEBUG_BREAK_HANDLER);
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
}
