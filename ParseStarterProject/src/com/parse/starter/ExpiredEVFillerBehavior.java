package com.parse.starter;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.TextView;

import com.parse.ParseObject;

/**
 * Created by andrew on 5/12/15.
 */
public class ExpiredEVFillerBehavior implements EVFillerBehavior {
    /**
     * Fills the view for an expired event
     *
     * @param eventInfo ParseObject that contains the event
     * @param parent the activity that calls the filler
     */
    public void fillView(ParseObject eventInfo, Activity parent) {
        TextView title_text = (TextView) parent.findViewById(R.id.event_title);
        TextView time_text = (TextView) parent.findViewById(R.id.event_time);
        TextView loc_text = (TextView) parent.findViewById(R.id.event_loc);

        title_text.setClickable(false);
        time_text.setTextColor(Color.GRAY);
        time_text.setClickable(false);
        loc_text.setTextColor(Color.GRAY);
        loc_text.setClickable(false);

        TextView invite_button = (TextView) parent.findViewById(R.id.invite_button);
        invite_button.setClickable(false);
        invite_button.setHeight(0);


        // Do not display the RSVP option
        TextView rsvp_button = (TextView) parent.findViewById(R.id.rsvp_button);
        rsvp_button.setClickable(false);
        rsvp_button.setHeight(0);
        rsvp_button = (TextView) parent.findViewById(R.id.rsvp_text);
        rsvp_button.setHeight(0);
    }
}
