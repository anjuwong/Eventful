package com.parse.starter;


import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
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

        //title_text.setTextColor(Color.GRAY);
        title_text.setClickable(false);

        time_text.setTextColor(Color.GRAY);
        time_text.setTextAppearance(parent.getApplicationContext(), Typeface.ITALIC);
        time_text.setClickable(false);


        loc_text.setTextColor(Color.GRAY);
        loc_text.setTextAppearance(parent.getApplicationContext(), Typeface.ITALIC);
        loc_text.setClickable(false);


        TextView invite_button = (TextView) parent.findViewById(R.id.invite_button);
        invite_button.setClickable(false);
        invite_button.setTextColor(Color.BLUE);
        invite_button.setHeight(0);
    }
}
