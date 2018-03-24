package com.eclipsesource.v8.debug;

import com.eclipsesource.v8.Releasable;
import com.eclipsesource.v8.V8Object;

public class EventData implements Releasable {
    protected V8Object v8Object;

    EventData(V8Object eventData) {
        this.v8Object = eventData.twin();
    }

    public void release() {
        if (!this.v8Object.isReleased()) {
            this.v8Object.release();
        }
    }
}
