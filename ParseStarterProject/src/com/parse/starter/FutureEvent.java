package com.parse.starter;

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by derek on 5/9/15.
 */
public class FutureEvent extends Event {

    /**
     * Construct a FutureEvent object that contains a ParseObject corresponding to an event
     *
     * @param event a ParseObject corresponding to an event
     */
    public FutureEvent(ParseObject event) {
        this.event = event;
    }

    /**
     * Function that returns whether the ParseObject wrapped by this class should not be filtered
     *
     * @return whether the ParseObject wrapped by the class should not be filtered
     */
    public boolean isValid() {
        Date currentDate = new Date();
        if (event.getDate("Time").after(currentDate)) {
            return true;
        }
        return false;
    }

    /**
     * Convert a list of events into a list of FutureEvents by wrapping each ParseObject in a
     * FutureEvent object
     *
     * @param allEvents list of all events that a user is involved in
     * @return a list of FutureEvents
     */
    public static List<Event> getFutureEvents(List<ParseObject> allEvents) {
        List<Event> futureEventsBeforeFilter = new ArrayList<>();
        for (int i = 0; i < allEvents.size(); i++) {
            ParseObject event = allEvents.get(i);
            FutureEvent futureEvent = new FutureEvent(event);
            futureEventsBeforeFilter.add(futureEvent);
        }
        return futureEventsBeforeFilter;
    }
}