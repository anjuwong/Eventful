package com.parse.starter;


import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import com.parse.ParseObject;

/**
 * Created by andrew on 5/6/15.
 */
public class GuestEVFillerBehavior implements EVFillerBehavior {
    // can add time and location
    public void fillView(ParseObject eventInfo, Activity parent) {
        TextView title_text = (TextView) parent.findViewById(R.id.event_title);
        TextView time_text = (TextView) parent.findViewById(R.id.event_time);
        TextView loc_text = (TextView) parent.findViewById(R.id.event_loc);

        title_text.setTextColor(Color.GRAY);
        title_text.setClickable(false);

        time_text.setTextColor(Color.MAGENTA);
        time_text.setTextAppearance(parent.getApplicationContext(), Typeface.ITALIC);

        loc_text.setTextColor(Color.MAGENTA);
        loc_text.setTextAppearance(parent.getApplicationContext(), Typeface.ITALIC);

        // TODO: add voting dialog (onLongClick)
        // TODO: make dialogs push to voting table instead of event table
        // event object (per user) could have voting stuff while eventInfo has official info
        // query on Events for Event == eventId
        // count up locs and times
        // only allow one vote at a time
    }
}
