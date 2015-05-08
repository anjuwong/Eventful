package com.parse.starter;

import android.app.Activity;

import com.parse.ParseObject;

public interface EVFillerBehavior {
    public void fillView(ParseObject eventInfo, Activity parent);
}
