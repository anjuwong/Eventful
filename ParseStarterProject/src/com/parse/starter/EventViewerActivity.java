package com.parse.starter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by andrew on 5/5/15.
 */
public class EventViewerActivity extends Activity {

    private boolean clone;
    private ParseObject event;
    private String userId;
    private String eventId;
    private String title;
    private String loc;
    private String locId;
    private String creator;
    private String inviteId; // use for when we implement saving lists of invited people
    private int type;
    private List<ParseObject> inviteList = new ArrayList<>();
    private final List<String> titleList = new ArrayList<>();
    private final List<String> locList = new ArrayList<>();
    private Date datetime;
    private Date emptyDate;
    private TextView title_text;
    private TextView loc_text;
    private TextView time_text;
    private EVFillerBehavior filler;

    /* Request code passed to the PlacePicker intent to identify its result when it returns.
     */
    private static final int REQUEST_PLACE_PICKER = 1;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.eventview);
        Intent intent = getIntent();

        // EVENT_ID should be alpha-numeric; to get extra features, add non-[A-Z0-9] character
        Bundle extras = intent.getExtras();
        eventId = extras.getString("EVENT_ID");
        clone = extras.getBoolean("CLONE");
        getEvent(eventId);
        fillEvent();
    }

    /* Verifies that the inputs are not default */
    public boolean verify() {
        if(title.equals("") ||
           locId.equals("") ||
           datetime.equals(emptyDate)) // update when implementing invites
            return false;
        return true;
    }

    /* If verified, puts parameters into the event object and pushes
     * If title or location or (TODO: invite list) are new, push as well
     */
    public void submit (View view) {
        /* TODO: make this part of the filler behavior
         * Guest's submit should only save votes
         */

        if(!verify()) {
            message("Please fill out activity name, location, and time!");
            return;
        }
        event.put("Title", title);
        event.put("Time", datetime);
        event.put("Location", loc);
        event.put("LocationId", locId);
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
            saveLoc.put("LocationId", locId);
            saveLoc.saveInBackground();
        }
        message("Saved!");
        finish();
    }

    public void cancel (View view) {
        finish();
    }

    /* Takes the input eventId and queries for that event
     * If empty-string as input, create a new parse object and create dialog to get the type
     * Otherwise, get and record all of the event's attributes
     * If the event is a cloned event, create a new parse object instead of using the old  object
     */
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
            locId    = "";
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
                            fillHeader();
                            getPreviousEntries("Titles", "Title", titleList);
                            getPreviousEntries("Locations", "Location", locList);
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
            locId    = event.getString("LocationId");
            datetime = event.getDate("Time");
            type     = event.getInt("Type");
            inviteId = event.getString("InviteList");
            creator  = event.getString("Creator");
            getPreviousEntries("Titles", "Title", titleList);
            getPreviousEntries("Locations", "Location", locList);
            if(clone) {
                event = new ParseObject("Event");
                datetime = emptyDate;
            }
        }

    }
    /* Changes the icon of the eventView and makes the header color match the image
     */
    public void fillHeader() {
        RelativeLayout header = (RelativeLayout)findViewById(R.id.event_header);
        ImageView icon = (ImageView)findViewById(R.id.event_icon);
        switch(type) {
            case -1:
                icon.setImageResource(R.drawable.ic_blank);
                header.setBackgroundColor(Color.parseColor("#D9D9D9"));
                break;
            case 0:// food
                icon.setImageResource(R.drawable.ic_food);
                header.setBackgroundColor(Color.parseColor("#6AA84F"));
                break;
            case 1://workout
                icon.setImageResource(R.drawable.ic_workout);
                header.setBackgroundColor(Color.parseColor("#E69138"));
                break;
            case 2://chill
                icon.setImageResource(R.drawable.ic_chill);
                header.setBackgroundColor(Color.parseColor("#6D9EEB"));
                break;
            case 3://game
                icon.setImageResource(R.drawable.ic_game);
                header.setBackgroundColor(Color.parseColor("#E06666"));
                break;
        }
    }

    /* Fills the view's text fields, handling default values accordingly
     * Uses EVFillerBehaviors to define different clickable levels
     */
    public void fillEvent() {
        title_text = (TextView) findViewById(R.id.event_title);
        loc_text   = (TextView) findViewById(R.id.event_loc);
        time_text  = (TextView) findViewById(R.id.event_time);

        if(title.equals("")) {
            title_text.setText("activity name");
        }
        else {
            title_text.setText(title);
        }

        if(datetime.equals(emptyDate))
            time_text.setText("00/00/0000, 00:00");
        else
            time_text.setText(datetime.toString());

        if(loc.equals(""))
            loc_text.setText("location");
        else
            loc_text.setText(loc);
        // TODO
        /*ListView invite_list = (ListView) parent.findViewById(R.id.invite_list);
        if(!eventInfo.getJSONArray("InviteListId").equals("")) {
            // search for inviteListId

            // before adding a group, make sure it doesn't exist
        }*/
        fillHeader();
        if (!datetime.equals(emptyDate) && datetime.before(new Date())) {
            filler = new ExpiredEVFillerBehavior();
        } else if (userId.equals(creator)) {
            filler = new OwnerEVFillerBehavior();
        } else {
            filler = new GuestEVFillerBehavior();
        }
        filler.fillView(event, this);
    }

    /* Queries <tableName> Parse table for <attribute> that match the userId and type
     * Fills list with entries (clears first)
     */
    private void getPreviousEntries(String tableName, String attribute, final List<String> list) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(tableName);

        query.whereEqualTo("User", userId);
        query.whereEqualTo("Type", type);
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
    }

    /* Creates a new dialogBuilder for setting fields and allows reuse of old entries
     * list contains previously submitted entries for the same event type
     * setText allows onClickListener to set specific text fields
     */
    private void editTextField(String changePrompt, final List<String> list, final TextSetter setText) {

        AlertDialog.Builder builder = new AlertDialog.Builder(EventViewerActivity.this);
        LayoutInflater inflater = (LayoutInflater) EventViewerActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View layout = inflater.inflate(R.layout.title_editor, null);
        ((TextView)(layout.findViewById(R.id.changeprompt))).setText(changePrompt);
        ListAdapter listAdapter = new ArrayAdapter<String>(EventViewerActivity.this, R.layout.row, list);
        ListView lv = (ListView) layout.findViewById(R.id.edit_list);

        if(false) {
            // do stuff for locations only
        }

        /* If click on old entry, set text to that entry */
        lv.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((TextView) layout.findViewById(R.id.edittext)).setText(list.get(i));
            }

        });
        lv.setAdapter(listAdapter);
        /* When finished, set the text of the EventViewer to the new text (done using setText.call()) */
        builder.setView(layout).setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (!((EditText) layout.findViewById(R.id.edittext)).getText().toString().equals("")) {
                    setText.call(((EditText) layout.findViewById(R.id.edittext)).getText().toString());
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
        editTextField("Change the activity name", titleList, new setTitle());
    }

    public void editLoc(View view) {
        //editTextField("Change the location", locList, new setLoc());
        // Construct an intent for the place picker
        try {
            PlacePicker.IntentBuilder intentBuilder =
                    new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(this);
            // Start the intent by requesting a result,
            // identified by a request code.
            startActivityForResult(intent, REQUEST_PLACE_PICKER);

        } catch (GooglePlayServicesRepairableException e) {
            GooglePlayServicesUtil
                    .getErrorDialog(e.getConnectionStatusCode(), EventViewerActivity.this, 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(EventViewerActivity.this, "Google Play Services is not available.",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    public void editAddress(View view) {
        // Construct an intent for the place picker
        try {
            PlacePicker.IntentBuilder intentBuilder =
                    new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(this);
            // Start the intent by requesting a result,
            // identified by a request code.
            startActivityForResult(intent, REQUEST_PLACE_PICKER);

        } catch (GooglePlayServicesRepairableException e) {
            GooglePlayServicesUtil
                    .getErrorDialog(e.getConnectionStatusCode(), EventViewerActivity.this, 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(EventViewerActivity.this, "Google Play Services is not available.",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {

        if (requestCode == REQUEST_PLACE_PICKER
                && resultCode == Activity.RESULT_OK) {

            // The user has selected a place. Extract the name and address.
            final Place place = PlacePicker.getPlace(data, this);

            // TODO: change to address
            locId = place.getId();
            loc = getLocationDescriptor(place);
            loc_text.setText(loc);

            String a = place.getName() + " (" + place.getAddress();

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private String getLocationDescriptor(Place place) {
        final String name = place.getName().toString();
        final String address = place.getAddress().toString();
        String description = "";
        if (address.toLowerCase().contains(name.toLowerCase()) || address.equals("")) {
            description = address;
        } else {
            description = name + "\nAddress: " + address;
        }
        return description;
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
        InviteHelper inviteHelper = new InviteHelper(EventViewerActivity.this);
        inviteHelper.openInviteDialog();

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
            // TODO:
            // ((TextView)findViewById(R.id.event_address)).setText("");
            // address = "";
        }
    }

    public void message(String msg) {
        Toast toast = Toast.makeText(EventViewerActivity.this, msg,
                Toast.LENGTH_LONG);
        toast.show();
    }
}