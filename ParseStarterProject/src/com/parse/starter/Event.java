package com.parse.starter;

import com.parse.ParseObject;

/**
 * Created by derek on 5/9/15.
 */
public abstract class Event {
    protected ParseObject event;

    final public ParseObject getParseObject() {
        return event;
    }

    public abstract boolean isValid();
}