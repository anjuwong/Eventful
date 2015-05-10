package com.parse.starter;

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by derek on 5/9/15.
 */
public class PastEvent extends Event {

    public PastEvent(ParseObject event) {
        this.event = event;
    }

    public boolean isValid() {
        Date currentDate = new Date();
        if (event.getDate("Time").before(currentDate)) {
            return true;
        }
        return false;
    }

    public static List<Event> getPastEvents(List<ParseObject> allEvents) {
        List<Event> pastEventsBeforeFilter = new ArrayList<>();
        for (int i = 0; i < allEvents.size(); i++) {
            ParseObject event = allEvents.get(i);
            PastEvent pastEvent = new PastEvent(event);
            pastEventsBeforeFilter.add(pastEvent);
        }
        return pastEventsBeforeFilter;
    }
}
