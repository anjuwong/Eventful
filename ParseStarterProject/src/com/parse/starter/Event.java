package com.parse.starter;

import com.parse.ParseObject;

/**
 * Created by derek on 5/9/15.
 */
public abstract class Event {
    protected ParseObject event;

    /**
     * Function that returns the ParseObject wrapped by the class
     *
     * @return the ParseObject wrapped by this class
     */
    final public ParseObject getParseObject() {
        return event;
    }

    /**
     * Function that returns whether the ParseObject wrapped by this class should not be filtered
     *
     * @return whether the ParseObject wrapped by the class should not be filtered
     */
    public abstract boolean isValid();
}