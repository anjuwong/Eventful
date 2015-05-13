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

        title_text.setTextColor(Color.YELLOW);
        title_text.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        title_text.setAllCaps(true);

        time_text.setTextColor(Color.YELLOW);
        time_text.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));

        loc_text.setTextColor(Color.YELLOW);
        loc_text.setTypeface(Typeface.create(Typeface.SANS_SERIF,Typeface.BOLD));

        //TODO: add voting dialog (onLongClick)

        // TODO
        /*ListView invite_list = (ListView) parent.findViewById(R.id.invite_list);
        if(!eventInfo.getJSONArray("InviteListId").equals("")) {
            // search for inviteListId

            // before adding a group, make sure it doesn't exist
        }
        Log.v("Debugging","FRIENDS ADDED");*/

    }
}
