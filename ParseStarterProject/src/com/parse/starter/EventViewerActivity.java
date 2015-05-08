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

        // TODO: make sure that title, datetime, loc are valid (not default)
        event.put("Title", title);
        event.put("Time", datetime);
        event.put("Location", loc);
        event.put("Creator", creator);
        event.put("Type", type);

        event.saveInBackground();

        // TODO: save new title
        // TODO: save new inviteList
        message("Saved!");
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

        String userId = ParseUser.getCurrentUser().getObjectId();

        filler = new OwnerEVFillerBehavior();

        // TODO: alter clickability and maybe color (later) of stuff
        if (event.getDate("Time").before(new Date())) {
            // can't change time, loc, friends
            // can add photos, notes, etc.

        } else if (userId.equals(creator)) {
            // can change time, loc, friends, etc.
        } else {
            // can't change time, loc, friends
            // can submit other times and locs
            ((TextView) findViewById(R.id.event_title)).setClickable(false);

        }
        filler.fillView(event, this);

    }

    // TODO: there is a design pattern (Strategy?) in these dialogs
    public void editTitle(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(EventViewerActivity.this);
        LayoutInflater inflater = (LayoutInflater) EventViewerActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.title_editor, null);
        ((TextView)(layout.findViewById(R.id.changeprompt))).setText("Change the activity name");

        // Populate old titles
        // TODO: set onclick check for duplicate
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Titles");
        query.whereEqualTo("User", userId);
        ArrayList<ParseObject> objectList = new ArrayList<ParseObject>();
        try {
            objectList = (ArrayList) query.find();
        } catch (com.parse.ParseException e) {
            message("Error retrieving records");
        }
        Log.v("Debugging", String.valueOf(objectList.size()));
        for (ParseObject o: objectList) {
            titleList.add(o.getString("Title"));
            Log.v("Debugging",o.getString("Title"));
        }
        ListAdapter listAdapter = new ArrayAdapter<String>(EventViewerActivity.this, R.layout.row, titleList);
        ListView lv = (ListView) layout.findViewById(R.id.edit_list);
        lv.setOnItemClickListener(new ListView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.v("Debugging", titleList.get(i));
                ((TextView) layout.findViewById(R.id.edittext)).setText(titleList.get(i));

            }

        });
        lv.setAdapter(listAdapter);

        builder.setView(layout).setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (!((EditText) layout.findViewById(R.id.edittext)).getText().toString().equals("")) {
                    title = ((EditText) layout.findViewById(R.id.edittext)).getText().toString();
                    ((TextView) findViewById(R.id.event_title)).setText(title);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(EventViewerActivity.this);
        LayoutInflater inflater = (LayoutInflater) EventViewerActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.title_editor, null);
        ((TextView)(layout.findViewById(R.id.changeprompt))).setText("Change the location");
        builder.setView(layout).setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (!((EditText) layout.findViewById(R.id.edittext)).getText().toString().equals("")) {
                    loc = ((EditText) layout.findViewById(R.id.edittext)).getText().toString();
                    ((TextView) findViewById(R.id.event_loc)).setText(loc);
                }
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Locations");
        query.whereEqualTo("User", userId);
        ArrayList<ParseObject> objectList = new ArrayList<ParseObject>();
        try {
            objectList = (ArrayList) query.find();
        } catch (com.parse.ParseException e) {
            message("Error retrieving records");
        }
        Log.v("Debugging", String.valueOf(objectList.size()));
        for (ParseObject o: objectList) {
            locList.add(o.getString("Location"));
            Log.v("Debugging",o.getString("Location"));
        }
        ListAdapter listAdapter = new ArrayAdapter<String>(EventViewerActivity.this, R.layout.row, locList);
        ListView lv = (ListView) layout.findViewById(R.id.edit_list);
        lv.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.v("Debugging", locList.get(i));
                ((TextView) layout.findViewById(R.id.edittext)).setText(locList.get(i));

            }

        });
        lv.setAdapter(listAdapter);

        builder.create();
        final Dialog dialog = builder.show();
    }

    public void editInvites(View view) {
        // TODO sets inviteList, creates InviteLists object, creates new Event objects for each user

    }

    public void message(String msg) {
        Toast toast = Toast.makeText(EventViewerActivity.this, msg,
                Toast.LENGTH_LONG);
        toast.show();
    }
}