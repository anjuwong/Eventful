package com.parse.starter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.usage.UsageEvents;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.app.Activity;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

/**
 * Created by andrew on 5/5/15.
 */
public class EventViewerActivity extends Activity {

    /*
     * Get event via eventId
     * Change layout to eventview
     * Make elements clickable (popups for now)
     * TODO: check user preferences before opening/event type to have different behavior?
     * TODO: store multiple locations, times, etc. for different users (later)
     * TODO: new Parse class: inviteList
     *  */
    public String userId;
    public ParseObject event;
    public String eventId;
    public int eventType;
    public String title;
    public Date datetime;
    public String loc;
    public String creator;
    public int type;
    public List<ParseObject> inviteList;
    public String inviteId;
    public final List<String> titleList = new ArrayList<String>();
    public final List<String> locList = new ArrayList<String>();
    public void onCreate(Bundle savedInstanceState) {
        Log.v("Debugging", "1.5");

        super.onCreate(savedInstanceState);
        Log.v("Debugging", "2");
        setContentView(R.layout.eventview);
        Log.v("Debugging","3");
        Intent intent = getIntent();

        eventId = intent.getStringExtra("EVENT_ID");
        Log.v("Debugging","4");
        getEvent(eventId);
    }
    EVFillerBehavior filler;
    public void submit (View view) {
        /* TODO: make this part of the filler behavior
         * Guest's submit should only save votes
         */

        // TODO: make sure that title, datetime, loc are valid (not default)
        event.put("Title", title);
        event.put("Time", datetime);
        event.put("Location", loc);
        event.put("Creator", creator);
        event.put("User", creator);
        event.put("Type", type);

        event.saveInBackground();

        // TODO: save new title
        // TODO: save new inviteList
        boolean exists = false;
        for (String t:titleList) {
            if (t.equals(title)) {
                exists = true;
            }
        }
        if (!exists) {
            ParseObject saveTitle = new ParseObject("Titles");
            saveTitle.put("User",userId);
            saveTitle.put("Title",title);
            saveTitle.put("Type",type);
            saveTitle.saveInBackground();
        }
        exists = false;
        for (String l:locList) {
            if (l.equals(loc)) {
                exists = true;
            }
        }
        if (!exists) {
            ParseObject saveLoc = new ParseObject("Locations");
            saveLoc.put("User",userId);
            saveLoc.put("Location",loc);
            saveLoc.saveInBackground();
        }
        message("Saved!");
        finish();
    }

    public void cancel (View view) {
        finish();
    }
    public void getEvent(String eventId) {
        userId = ParseUser.getCurrentUser().getObjectId();
        if (eventId.equals("")) {
            // Nothing passed, create new event with default parameters
            event = new ParseObject("Event");

            Date emptyDate = new Date();
            emptyDate.setTime(0);
            creator = userId;
            type = 0;
            title = "";
            loc = "";
            datetime = emptyDate;
            inviteId = "";

            event.put("Title","");
            event.put("Location", "");

            event.put("Time",emptyDate);

            String[] actList = {"Food", "Workout", "Chill", "Game"};
            AlertDialog.Builder builder = new AlertDialog.Builder(EventViewerActivity.this);
            builder.setTitle("Choose an Activity")
                    .setItems(actList, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            type = which;
                        }
                    });
            builder.create();
            builder.show();
        } else {

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
            event = new ParseObject("Event");
            try {
                event = query.get(eventId);
            } catch (com.parse.ParseException e) {
                e.printStackTrace();
            }
            title = event.getString("Title");
            loc = event.getString("Location");
            datetime = event.getDate("Time");
            type = event.getInt("Type");
            inviteId = event.getString("InviteList");
            creator = event.getString("Creator");
        }

        TextView title_text = (TextView) findViewById(R.id.event_title);

        if(event.getString("Title").equals(""))
            title_text.setText("activity name");
        else
            title_text.setText(event.getString("Title"));


        TextView time_text = (TextView) findViewById(R.id.event_time);
        Date emptyDate = new Date();
        emptyDate.setTime(0);
        if(event.getDate("Time").equals(emptyDate))
            time_text.setText("00/00/0000, 00:00");
        else
            time_text.setText(event.getDate("Time").toString());


        TextView loc_text = (TextView) findViewById(R.id.event_loc);
        if(event.getString("Location").equals(""))
            loc_text.setText("location");
        else
            loc_text.setText(event.getString("Location"));
        // TODO
        /*ListView invite_list = (ListView) parent.findViewById(R.id.invite_list);
        if(!eventInfo.getJSONArray("InviteListId").equals("")) {
            // search for inviteListId

            // before adding a group, make sure it doesn't exist
        }*/


        // TODO: alter clickability and maybe color (later) of stuff
        if (!event.getDate("Time").equals(emptyDate) && event.getDate("Time").before(new Date())) {
            // can't change time, loc, friends
            // can add photos, notes, etc.
            Log.v("Debugging","that's expired");
            filler = new ExpiredEVFillerBehavior();
        } else if (userId.equals(creator)) {
            // can change time, loc, friends, etc.
            Log.v("Debugging","that's a creator");
            filler = new OwnerEVFillerBehavior();
        } else {
            // can't change time, loc, friends
            // can submit other times and locs
            Log.v("Debugging","that's a guest");
            filler = new GuestEVFillerBehavior();

        }
        filler.fillView(event, this);

    }

    private void editTextField(View view, String changePrompt, String tableName,
                                              String attribute, final List<String> list, final TextSetter setText) {
        // Creates a new dialogBuilder for setting text, queries for a given attribute, and fills UI

        AlertDialog.Builder builder = new AlertDialog.Builder(EventViewerActivity.this);
        LayoutInflater inflater = (LayoutInflater) EventViewerActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        final View layout = inflater.inflate(R.layout.title_editor, null);
        ((TextView)(layout.findViewById(R.id.changeprompt))).setText(changePrompt);
        ParseQuery<ParseObject> query = ParseQuery.getQuery(tableName);

        query.whereEqualTo("User", userId);
        query.whereEqualTo("Type",type);
        ArrayList<ParseObject> objectList = new ArrayList<ParseObject>();
        try {
            objectList = (ArrayList) query.find();
        } catch (com.parse.ParseException e) {
            message("Error retrieving records");
        }

        list.clear();
        for (ParseObject o: objectList) {

            list.add(o.getString(attribute));

        }
        ListAdapter listAdapter = new ArrayAdapter<String>(EventViewerActivity.this, R.layout.row, list);
        ListView lv = (ListView) layout.findViewById(R.id.edit_list);
        lv.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((TextView) layout.findViewById(R.id.edittext)).setText(list.get(i));

            }

        });
        lv.setAdapter(listAdapter);
        builder.setView(layout).setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (!((EditText) layout.findViewById(R.id.edittext)).getText().toString().equals("")) {
                    setText.call(((EditText) layout.findViewById(R.id.edittext)).getText().toString());
                    //((TextView) findViewById(R.id.event_title)).setText(title);
                }

            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });   ;
        builder.create();
        final Dialog dialog = builder.show();
    }
    public void editTitle(View view) {

        editTextField(view, "Change the activity name", "Titles", "Title", titleList, new setTitle());
    }
    public void editTime(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EventViewerActivity.this);
        LayoutInflater inflater = (LayoutInflater) EventViewerActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.datetime_editor, null);
        builder.setView(layout).setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                TimePicker tp = (TimePicker)layout.findViewById(R.id.edit_time);
                DatePicker dp = (DatePicker)layout.findViewById(R.id.edit_date);
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, dp.getYear());
                cal.set(Calendar.MONTH, dp.getMonth());
                cal.set(Calendar.DATE, dp.getDayOfMonth());
                cal.set(Calendar.HOUR_OF_DAY, tp.getCurrentHour());
                cal.set(Calendar.MINUTE, tp.getCurrentMinute());
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                datetime = cal.getTime();
                ((TextView) findViewById(R.id.event_time)).setText(datetime.toString());
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });   ;
        builder.create();
        final Dialog dialog = builder.show();
    }
    public void editLoc(View view) {
        editTextField(view, "Change the location", "Locations", "Location", locList, new setLoc());
    }

    public void editInvites(View view) {
        // TODO sets inviteList, creates InviteLists object, creates new Event objects for each user

    }


    // used to set text within other classes
    private class setTitle implements TextSetter {
        public void call(String text) {
            title=text;
            ((TextView) findViewById(R.id.event_title)).setText(text);
        }
    }
    private class setLoc implements TextSetter {
        public void call(String text) {
            loc=text;
            ((TextView) findViewById(R.id.event_loc)).setText(text);
        }
    }
    public void message(String msg) {
        Toast toast = Toast.makeText(EventViewerActivity.this, msg,
                Toast.LENGTH_LONG);
        toast.show();
    }
}