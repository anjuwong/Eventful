package com.parse.starter;

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by derek on 5/9/15.
 */
public class FutureEvent extends Event {

    public FutureEvent(ParseObject event) {
        this.event = event;
    }

    public boolean isValid() {
        Date currentDate = new Date();
        if (event.getDate("Time").after(currentDate)) {
            return true;
        }
        return false;
    }

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