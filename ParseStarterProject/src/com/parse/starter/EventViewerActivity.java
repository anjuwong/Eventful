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
     * TODO: new Parse class: inviteList
     *  */
    public ParseObject event;
    public String userId;
    public String eventId;
    public String title;
    public String loc;
    public String creator;
    public String inviteId; // use for when we implement saving lists of invited people
    public int type;
    public List<ParseObject> inviteList = new ArrayList<>();
    public final List<String> titleList = new ArrayList<>();
    public final List<String> locList = new ArrayList<>();
    public Date datetime;
    public Date emptyDate;
    public TextView title_text;
    public TextView loc_text;
    public TextView time_text;
    public EVFillerBehavior filler;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.eventview);
        Intent intent = getIntent();

        eventId = intent.getStringExtra("EVENT_ID");
        getEvent(eventId);
        fillEvent();
    }
    public boolean verify() {
        if(title.equals("") ||
           loc.equals("") ||
           datetime.equals(emptyDate)) // update when implementing invites
            return false;
        return true;
    }
    public void submit (View view) {
        /* TODO: make this part of the filler behavior
         * Guest's submit should only save votes
         */

        if(!verify()) {
            message("Please fill out activity name, location, and time!");
            return;
        }
        // TODO: make sure that title, datetime, loc are valid (not default)
        event.put("Title", title);
        event.put("Time", datetime);
        event.put("Location", loc);
        event.put("Creator", creator);
        event.put("User", creator);
        event.put("Type", type);

        event.saveInBackground();

        // TODO: save new inviteList
        if (!titleList.contains(title)) {
            ParseObject saveTitle = new ParseObject("Titles");
            saveTitle.put("User",userId);
            saveTitle.put("Title",title);
            saveTitle.put("Type",type);
            saveTitle.saveInBackground();
        }
        if (!locList.contains(loc)) {
            ParseObject saveLoc = new ParseObject("Locations");
            saveLoc.put("User",userId);
            saveLoc.put("Type", type);
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
        emptyDate = new Date();
        emptyDate.setTime(0);
        if (eventId.equals("")) {
            // Nothing passed, create new event with default parameters
            event    = new ParseObject("Event");
            creator  = userId;
            type     = -1;
            title    = "";
            loc      = "";
            datetime = emptyDate;
            inviteId = "";

            event.put("Title", "");
            event.put("Location", "");
            event.put("Time", emptyDate);
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
            // get the event information
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
            event = new ParseObject("Event");
            try {
                event = query.get(eventId);
            } catch (com.parse.ParseException e) {
                e.printStackTrace();
            }
            title    = event.getString("Title");
            loc      = event.getString("Location");
            datetime = event.getDate("Time");
            type     = event.getInt("Type");
            inviteId = event.getString("InviteList");
            creator  = event.getString("Creator");
        }
    }
    public void fillEvent() {
        title_text = (TextView) findViewById(R.id.event_title);
        loc_text   = (TextView) findViewById(R.id.event_loc);
        time_text  = (TextView) findViewById(R.id.event_time);

        if(title.equals("")) {
            title_text.setText("activity name");
        }
        else {
            title_text.setText(event.getString("Title"));
        }

        if(datetime.equals(emptyDate))
            time_text.setText("00/00/0000, 00:00");
        else
            time_text.setText(event.getDate("Time").toString());

        if(loc.equals(""))
            loc_text.setText("location");
        else
            loc_text.setText(event.getString("Location"));

        // TODO
        /*ListView invite_list = (ListView) parent.findViewById(R.id.invite_list);
        if(!eventInfo.getJSONArray("InviteListId").equals("")) {
            // search for inviteListId

            // before adding a group, make sure it doesn't exist
        }*/

        if (!datetime.equals(emptyDate) && datetime.before(new Date())) {
            // can't change time, loc, friends
            // can add photos, notes, etc.
            filler = new ExpiredEVFillerBehavior();
        } else if (userId.equals(creator)) {
            // can change time, loc, friends, etc.
            filler = new OwnerEVFillerBehavior();
        } else {
            // can't change time, loc, friends
            // can submit other times and locs
            filler = new GuestEVFillerBehavior();
        }
        filler.fillView(event, this);

    }

    private void editTextField(View view, String changePrompt, String tableName,
                                              String attribute, final List<String> list, final TextSetter setText) {
        // Creates a new dialogBuilder for setting text, queries for a given attribute, and fills UI
        // list contains previously submitted attributes for the same event type
        // setText allows onClickListener to set specific text fields

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
    public void editLoc(View view) {
        editTextField(view, "Change the location", "Locations", "Location", locList, new setLoc());
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
                time_text.setText(datetime.toString());
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });   ;
        builder.create();
        final Dialog dialog = builder.show();
    }


    public void editInvites(View view) {
        // TODO sets inviteList, creates InviteLists object, creates new Event objects for each user

    }


    // used to set text within other classes
    private class setTitle implements TextSetter {
        public void call(String text) {
            title=text;
            title_text.setText(text);
        }
    }

    private class setLoc implements TextSetter {
        public void call(String text) {
            loc=text;
            loc_text.setText(text);
        }
    }
    public void message(String msg) {
        Toast toast = Toast.makeText(EventViewerActivity.this, msg,
                Toast.LENGTH_LONG);
        toast.show();
    }
}