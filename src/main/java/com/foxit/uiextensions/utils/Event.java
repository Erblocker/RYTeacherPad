package com.foxit.uiextensions.utils;

public class Event {
    public int mType;

    public interface Callback {
        void result(Event event, boolean z);
    }

    public Event(int type) {
        this.mType = type;
    }
}
