package com.parse.starter;
import android.app.Activity;
import android.util.Log;
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


        if(eventInfo.getString("Title").equals(""))
            title_text.setText("activity name");
        else
            title_text.setText(eventInfo.getString("Title"));


        TextView time_text = (TextView) parent.findViewById(R.id.event_time);
        Date emptyDate = new Date();
        emptyDate.setTime(0);
        if(eventInfo.getDate("Time").equals(emptyDate))
            time_text.setText("00/00/0000, 00:00");
        else
            time_text.setText(eventInfo.getDate("Time").toString());


        TextView loc_text = (TextView) parent.findViewById(R.id.event_loc);
        if(eventInfo.getString("Location").equals(""))
            loc_text.setText("location");
        else
            loc_text.setText(eventInfo.getString("Location"));


        // TODO
        /*ListView invite_list = (ListView) parent.findViewById(R.id.invite_list);
        if(!eventInfo.getJSONArray("InviteListId").equals("")) {
            // search for inviteListId

            // before adding a group, make sure it doesn't exist
        }
        Log.v("Debugging","FRIENDS ADDED");*/

    }



}
