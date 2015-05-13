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
    public void fillView(ParseObject eventInfo, Activity parent) {
        TextView title_text = (TextView) parent.findViewById(R.id.event_title);
        TextView time_text = (TextView) parent.findViewById(R.id.event_time);
        TextView loc_text = (TextView) parent.findViewById(R.id.event_loc);

        title_text.setTextColor(Color.GRAY);
        title_text.setClickable(false);
        time_text.setTextColor(Color.GRAY);
        time_text.setClickable(false);
        loc_text.setTextColor(Color.GRAY);
        loc_text.setClickable(false);
        // TODO: add notes, pictures, etc.
    }
}
