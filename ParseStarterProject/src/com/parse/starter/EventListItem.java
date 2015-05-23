package com.parse.starter;

import java.util.Date;

/**
 * Created by andrew on 5/22/15.
 */
public class EventListItem {
    private int type;
    private String title;
    private String location;
    private String datetime;
    public String id;
    public String getTitle() {
        return title;
    }
    public String getLoc() {
        return location;
    }
    public String getDate() {
        return datetime;
    }
    public int getType() {
        return type;
    }
    public EventListItem(String id, String title, String loc, String datetime, int type) {
        this.id = id;
        this.title = title;
        this.location = loc;
        this.datetime = datetime;
        this.type = type;
    }
}
