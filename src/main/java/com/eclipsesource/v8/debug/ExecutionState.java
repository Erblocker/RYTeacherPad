package com.eclipsesource.v8.debug;

import com.eclipsesource.v8.Releasable;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.debug.mirror.Frame;

public class ExecutionState implements Releasable {
    private static final String FRAME = "frame";
    private static final String FRAME_COUNT = "frameCount";
    private static final String PREPARE_STEP = "prepareStep";
    private V8Object v8Object;

    ExecutionState(V8Object v8Object) {
        this.v8Object = v8Object.twin();
    }

    public int getFrameCount() {
        return this.v8Object.executeIntegerFunction(FRAME_COUNT, null);
    }

    public void prepareStep(StepAction action) {
        V8Array parameters = new V8Array(this.v8Object.getRuntime());
        parameters.push(action.index);
        try {
            this.v8Object.executeVoidFunction(PREPARE_STEP, parameters);
        } finally {
            parameters.release();
        }
    }

    public Frame getFrame(int index) {
        V8Array parameters = new V8Array(this.v8Object.getRuntime());
        parameters.push(index);
        V8Object frame = null;
        try {
            frame = this.v8Object.executeObjectFunction(FRAME, parameters);
            Frame frame2 = new Frame(frame);
            return frame2;
        } finally {
            parameters.release();
            if (frame != null) {
                frame.release();
            }
        }
    }

    public void release() {
        if (this.v8Object != null && !this.v8Object.isReleased()) {
            this.v8Object.release();
            this.v8Object = null;
        }
    }
}
