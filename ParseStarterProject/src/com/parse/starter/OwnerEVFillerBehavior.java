package com.parse.starter;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by andrew on 5/6/15.
 */
public class OwnerEVFillerBehavior implements EVFillerBehavior {

    public void fillView(ParseObject eventInfo, Activity parent) {

        TextView title_text = (TextView) parent.findViewById(R.id.event_title);
        TextView time_text = (TextView) parent.findViewById(R.id.event_time);
        TextView loc_text = (TextView) parent.findViewById(R.id.event_loc);
        ((TextView) parent.findViewById(R.id.event_host)).setText("you");


        time_text.setTextColor(Color.BLUE);
        time_text.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));

        loc_text.setTextColor(Color.BLUE);
        loc_text.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));

        TextView invite_button = (TextView) parent.findViewById(R.id.invite_button);
        invite_button.setTextColor(Color.BLUE);
        invite_button.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));

        // Do not display the RSVP option
        TextView rsvp_button = (TextView) parent.findViewById(R.id.rsvp_button);
        rsvp_button.setClickable(false);
        rsvp_button.setHeight(0);
        rsvp_button = (TextView) parent.findViewById(R.id.rsvp_text);
        rsvp_button.setHeight(0);



    }
}
