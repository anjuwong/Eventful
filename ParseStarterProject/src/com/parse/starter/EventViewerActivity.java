package com.parse.starter;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.parse.Parse;
import com.parse.ParseException;

import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by andrew on 5/5/15.
 */
public class EventViewerActivity extends Activity {


    private InviteHelper inviteHelper;
    /*
     * TODO: new Parse class: inviteList
     *  */
    private boolean clone;
    private ParseObject event;
    private String userId;
    private String eventId;
    private String title;
    private String loc;
    private String locId;
    private String creator;
    private String inviteId =""; // use for when we implement saving lists of invited people
    private String globalId = "";
    private int type;
    private int status;

    /** Invitation stuff */
    private List<String> oldInvitedParseIds = new ArrayList<>();
    private List<String> newInvitedParseIds = new ArrayList<>();
    private List<String> attendingParseIds = new ArrayList<>();
    private List<String> notAttendingParseIds = new ArrayList<>();
    private List<String> fullInvitedParseIds = new ArrayList<>();

    private final List<String> titleList = new ArrayList<>();
    private final List<String> locList = new ArrayList<>();
    private List<ParseObject> inviteListList = new ArrayList<>();
    private Date datetime;

    private List<Date> suggestedTimesList = new ArrayList<>();
    private List<String> chat = new ArrayList<>();

    private Date emptyDate;
    private TextView title_text;
    private TextView loc_text;
    private TextView time_text;
    private EVFillerBehavior filler;

    /* Request code passed to the PlacePicker intent to identify its result when it returns. */
    private static final int REQUEST_PLACE_PICKER = 1;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.eventview);
        Intent intent = getIntent();

        Bundle extras = intent.getExtras();
        eventId = extras.getString("EVENT_ID");
        clone = extras.getBoolean("CLONE");

        getEvent(eventId);
        fillEvent();

        // haxxy fix to get InviteHelper to display stuff in this activity
        inviteHelper = new InviteHelper(EventViewerActivity.this, fullInvitedParseIds, new fillInviteListFunction());

        /* Changed to use InviteLists table
        List<String> invitedParseIds = (List<String>) event.get(ParseConstants.InvitedParseIds);
        if (invitedParseIds != null) {
            savedInvitedParseIds.addAll(invitedParseIds);
        } else {
            invitedParseIds = new ArrayList<String>();
        }
        inviteHelper = new InviteHelper(EventViewerActivity.this, invitedParseIds);*/
    }

    /* Takes the input eventId and queries for that event
     * If empty-string as input, create a new parse object and create dialog to get the type
     * Otherwise, get and record all of the event's attributes
     * If the event is a cloned event, create a new parse object instead of using the old  object */
    public void getEvent(String eventId) {
        userId = ParseUser.getCurrentUser().getObjectId();
        emptyDate = new Date();
        emptyDate.setTime(0);

        //TODO (andrew) reuse inviteList? eh too much work
        /* Get the list of inviteLists (for reuse purposes) */
        ParseQuery<ParseObject> inviteListQuery = ParseQuery.getQuery("InviteLists");

        inviteListQuery.whereEqualTo("User", userId);
        inviteListList.clear();
        try {
            inviteListList = (ArrayList) inviteListQuery.find();
        } catch (com.parse.ParseException e) {
            message("Error retrieving records");
        }
        
        if (eventId.equals("")) {
            /* Nothing passed, create new event with default parameters */
            event    = new ParseObject("Event");
            creator  = userId;
            type     = -1;
            title    = "";
            loc      = "";
            locId    = "";
            datetime = emptyDate;
            inviteId = "";
            status = 1;
            globalId = "";

            event.put("Title", "");
            event.put("Location", "");
            event.put("Time", emptyDate);

            /* Create a dialog so users can choose a type */
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
            builder.setCancelable(false);
            builder.show();
        } else {
            /* get the event information */
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
            suggestedTimesList = event.getList("TimeVote");
            chat = event.getList("Chat");
            status   = event.getInt("Status");
            globalId = event.getString("globalId");


            // HAXXORZ
            /* Get the list of users that were already invited */
            ArrayList<String> invitedParseIds = new ArrayList<>();
            ParseQuery<ParseObject> inviteQuery = ParseQuery.getQuery("InviteLists");
            inviteQuery.whereEqualTo("objectId", inviteId);
            try {
                if (!inviteId.equals("")) {
                    ParseObject invites = inviteQuery.getFirst();
                    invitedParseIds = (ArrayList<String>) invites.get("InviteList");
                }
            } catch (com.parse.ParseException e) {
                e.printStackTrace();
            }
            oldInvitedParseIds.addAll(invitedParseIds);
            fullInvitedParseIds.addAll(oldInvitedParseIds);


            /* Get the list of titles and locations (for reuse purposes) */
            getPreviousEntries("Titles", "Title", titleList);
            getPreviousEntries("Locations", "Location", locList);

            /* Cloned events should have similar behavior to new events */
            if(clone) {
                event = new ParseObject("Event");
                status = 1;
                datetime = emptyDate;
                globalId = "";
            }
        }

    }

    /* Fills in the text fields for the event page
     * Has different behaviors based on whether event is expired, or whether user is the creator */
    public void fillEvent() {
        title_text = (TextView) findViewById(R.id.event_title);
        loc_text   = (TextView) findViewById(R.id.event_loc);
        time_text  = (TextView) findViewById(R.id.event_time);
        TextView rsvp_text = (TextView) findViewById(R.id.rsvp_button);
        switch(status) {
            case 0: rsvp_text.setText("Undecided"); break;
            case 1: rsvp_text.setText("Going"); break;
            case 2: rsvp_text.setText("Not going"); break;
            default: break;
        }
        ((TextView) findViewById(R.id.event_host)).setText(getName(creator));

        if(title.equals("")) {
            title_text.setText("activity name");
        }
        else {
            title_text.setText(title);
        }

        if(datetime.equals(emptyDate))
            time_text.setText("time and date not set");
        else
            time_text.setText(datetime.toString());

        if(loc.equals(""))
            loc_text.setText("location");
        else
            loc_text.setText(loc);

        fillInviteList();
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

    /* Changes the icon of the eventView and makes the header color match the image */
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

    /* If verified, puts parameters into the event object and pushes
     * Creators can update their guests' objects as well */
    public void submit (View view) {

        /* Creator: changes get pushed to all users' objects
         * Guests: cannot change fields, can only vote or RSVP
         *      If vote, push to all users' objects? (depending on voting implementation)
         *      If RSVP, only push own object */

        if(!verify()) {
            message("Please fill out activity name, location, and time!");
            return;
        }

        /* If this is a brand new object (no globalId), create a new EventId object and use its
         * objectId as the globalId */
        if(globalId.equals("")) {
            final ParseObject globalEvent = new ParseObject("EventIds");
            globalId = "";
            try {
                globalEvent.save();
                globalId = globalEvent.getObjectId();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        /* Get difference of fullInvitedParseIds and oldInvitedParseIds to get newly invited users */
        ArrayList<String> uniqueNewInvites = new ArrayList<>();
        for (String id : fullInvitedParseIds) {
            if (!oldInvitedParseIds.contains(id)) {
                uniqueNewInvites.add(id);
            }
        }
        newInvitedParseIds.clear();
        newInvitedParseIds.addAll(uniqueNewInvites);

        /* Save the title and location if new
         * TODO: should these only be done by creator? may not want to use other person's title/location */
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

        /* If the inviteList already exists in the DB, get its objectId and set inviteId to it */
        String matchingInviteId = listInListList(inviteListList, fullInvitedParseIds);
        if (matchingInviteId.equals("")) {
            for(String id:fullInvitedParseIds) {
                Log.v("Debug", id);
            }
            final ParseObject saveInvList = new ParseObject("InviteLists");
            saveInvList.put("User", userId);
            saveInvList.put("Type", type);
            saveInvList.put("InviteList", fullInvitedParseIds);
            try {
                saveInvList.save();
                inviteId = saveInvList.getObjectId();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            inviteId = matchingInviteId;
        }

        saveOwnEvent();

        /* Only the creator of an event can affect other users*/

        /* only do if not new object (eventId == "" || clone == true)
           query for old objects and update fields */
        saveNewInviteEvents();
        //if (!(eventId == "" || clone))
            saveOldInviteEvents();

        inviteHelper.resetInviteHelper(fullInvitedParseIds);
        message("Saved!");


        // TODO: Send Invite Notifications to all invited guests
        String notifyMsg = getName(creator) + " has invited you to a new Event!";
        for (String fbID: fullInvitedParseIds) {
            ParseQuery pQuery = ParseInstallation.getQuery(); // Installation query
            pQuery.whereEqualTo("facebookId", fbID);
            ParsePush.sendMessageInBackground(notifyMsg, pQuery);
        }

        // Local Notification - Reminder 30 minutes before event - for some reason doesn't work with Andrew's device
        notifyMsg = "Event " + "\'" + title + "\'" + " scheduled for " + datetime;
        Notification eventNotif = EventfulNotification.createNotification(
                this.getApplicationContext(), "Upcoming Event!", notifyMsg);

        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, eventId.hashCode());
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, eventNotif);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, eventId.hashCode(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        Log.d("curTime", "" + System.currentTimeMillis() );
        Log.d("datetime", "" + datetime.getTime() );
        Log.d("elapsedTime", "" + SystemClock.elapsedRealtime());
        // Notification pops 30 minutes before Event occurs
        long futureInMillis = Math.max((long)0.0,
                (SystemClock.elapsedRealtime() + datetime.getTime() - System.currentTimeMillis() - 60*30*1000));

        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
        // End Alarm notification - recall chunk if event time changes

        finish();
    }
    /* Verifies that the inputs are not default */
    public boolean verify() {
        if(title.equals("") ||
                //locId.equals("") ||
                datetime.equals(emptyDate)) // update when implementing invites
            return false;
        return true;
    }

    /* Saves all attributes for the user's event */
    public void saveOwnEvent() {
        event.put("Title", title);
        event.put("Time", datetime);
        event.put("Location", loc);
        event.put("LocationId", locId);
        event.put("Creator", creator);
        event.put("User", userId);
        event.put("Type", type);
        event.put("Status", status);
        event.put("InviteList", inviteId);
        event.put("globalId", globalId);
        event.put("TimeVote", suggestedTimesList);
        event.put("Chat", chat);
        event.saveInBackground();
    }

    /* Creates and saves new objects for the newly invited users */
    public void saveNewInviteEvents() {
        // create new event objects for new invitees
        for (String id : newInvitedParseIds) {
            ParseObject newInvitee = new ParseObject("Event");
            newInvitee.put("Title", title);
            newInvitee.put("Time", datetime);
            newInvitee.put("Location", loc);
            newInvitee.put("LocationId", locId);
            newInvitee.put("Creator", creator);
            newInvitee.put("User", getParseId(id));
            newInvitee.put("Type", type);
            newInvitee.put("Status", 0);
            newInvitee.put("InviteList", inviteId);
            newInvitee.put("TimeVote", suggestedTimesList);
            newInvitee.put("Chat", chat);
            newInvitee.put("globalId", globalId);
            newInvitee.saveInBackground();
        }
    }

    /* Looks for existing events and updates them
     * Only updates things that can be changed by other users (e.g. RSVP is unchanged)
     * Should only be called by the creator, since only creator can update anything */
    public void saveOldInviteEvents() {
        // TODO: check if event changed at all first
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
        query.whereEqualTo("globalId", globalId);
        List<ParseObject> invitees = new ArrayList<>();
        try {
            invitees = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        for (ParseObject invitee:invitees) {
            invitee.put("Title", title);
            invitee.put("Time", datetime);
            invitee.put("Location", loc);
            invitee.put("LocationId", locId);
            invitee.put("InviteList", inviteId);
            invitee.put("TimeVote", suggestedTimesList);
            invitee.put("Chat", chat);
            invitee.saveInBackground();
        }
    }
    /* Check whether a list is contained in a list of lists
     * For use to check if inviteList is already saved */
    public String listInListList(List<ParseObject> ll, List<String> l) {
        if (l == null || ll == null)
            return "";

        List<String> invites = new ArrayList<>();
        for (ParseObject o: ll) {
            invites = (ArrayList<String>) o.get("InviteList");
            if (invites.containsAll(l) && l.containsAll(invites)) {
                return o.getObjectId();
            }
        }
        return "";
    }




    /* Gets the ParseId for a given FacebookID */
    public String getParseId(String facebookId) {
        ParseQuery<ParseUser> pq = ParseUser.getQuery();
        pq.whereEqualTo("FacebookID", facebookId);
        try {
            ParseUser o = pq.getFirst();
            return o.getObjectId();
        } catch (ParseException e) {
            Log.v("DEBUGGING", facebookId + " doesn't exist");
            e.printStackTrace();
        }
        return "";
    }

    /* Gets the name for a given FacebookID
     * If it fails, this will try for a given ParseId */
    public String getName(String facebookId) {
        ParseQuery<ParseUser> pq = ParseUser.getQuery();
        pq.whereEqualTo("FacebookID", facebookId);
        try {
            ParseUser o = pq.getFirst();
            return o.getString("Name");
        } catch (ParseException e) {
            Log.v("DEBUGGING", facebookId + " doesn't exist as a FacebookID");
            ParseQuery<ParseUser> parse_pq = ParseUser.getQuery();
            parse_pq.whereEqualTo("objectId", facebookId);
            try {
                ParseUser o = parse_pq.getFirst();
                return o.getString("Name");
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return "";
    }



    public void voteTime(View view) {
        //...
        if(creator.equals(userId)) // creator should set as datetime instead of timeVote
            return;
    }

    public void voteLoc(View view) {
        //...
        if(creator.equals(userId)) // creator should set as loc instead of locVote
            return;
    }

    /* Queries <tableName> Parse table for <attribute> that match the userId and type
     * Fills list with entries (clears first) */
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
     * setText allows onClickListener to set specific text fields */
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

    /* RSVP by setting status field, 0 Undecided, 1 Going, 2 Not going */
    public void setRSVP (View view) {
        String[] statusList = {"Undecided","Going", "Not going"}; //
        AlertDialog.Builder builder = new AlertDialog.Builder(EventViewerActivity.this);
        builder.setTitle("RSVP")
                .setItems(statusList, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        status = which;
                        TextView rsvp_text = (TextView) findViewById(R.id.rsvp_button);
                        switch(status) {
                            case 0: rsvp_text.setText("Undecided"); break;
                            case 1: rsvp_text.setText("Going"); break;
                            case 2: rsvp_text.setText("Not going"); break;
                            default: break;
                        }
                    }
                });
        builder.create();
        builder.show();
    }
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {

        if (requestCode == REQUEST_PLACE_PICKER
                && resultCode == Activity.RESULT_OK) {

            // The user has selected a place. Extract the name and address.
            final Place place = PlacePicker.getPlace(data, this);

            locId = place.getId();
            loc = getLocationDescriptor(place);
            loc_text.setText(loc);

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

    public void suggestTime(View view) {
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

                if(!suggestedTimesList.contains(cal.getTime()))
                    suggestedTimesList.add(cal.getTime());

            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });   ;
        builder.create();
        final Dialog dialog = builder.show();
    }

    public void viewSuggetedTimes(View view) {
        String[] dateList = new String[suggestedTimesList.size()];
        for(int i = 0; i < suggestedTimesList.size(); i++) {
            dateList[i] = suggestedTimesList.get(i).toString();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(EventViewerActivity.this);
        builder.setTitle("Choose a time")
                .setItems(dateList, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        datetime = suggestedTimesList.get(which);
                        time_text.setText(datetime.toString());
                    }
                });
        builder.create();
        builder.show();
    }

    public void chat(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Leave a Message");
        builder.setMessage("Message");

        // Set an EditText view to get user input
        final EditText userInput = new EditText(this);
        builder.setView(userInput);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                chat.add(getName(userId) + ": " + userInput.getText().toString());
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        builder.show();
    }

    public void viewChat(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EventViewerActivity.this);

        // set title
        builder.setTitle("Messages");

        String messages = "";

        for(String s : chat) {
            messages += s + "\n";
        }

        // set dialog message
        builder
                .setMessage(messages)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        builder.create();
        builder.show();
    }

    public void editInvites(View view) {
        inviteHelper.openInviteDialog();

    }
    /* Helper to allow InviteHelper access to event_invite_list */
    public void fillInviteList() {
        ListView invited = (ListView) findViewById(R.id.event_invite_list);
        List<String> nameList = new ArrayList<>();
        for (String facebookId:fullInvitedParseIds) {
            nameList.add(getName(facebookId));
        }
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(EventViewerActivity.this,
                R.layout.row,
                nameList);
        invited.setAdapter(adapter);
        setListViewHeightBasedOnChildren(invited);
    }

    /* Object to pass to InviteHelper so it can access event_invite_list */
    public class fillInviteListFunction implements Function {
        public void call() {
            fillInviteList();
        }
    }

    // used to set text within other classes
    private class setTitle implements TextSetter {
        public void call(String text) {
            title=text;
            title_text.setText(text);
        }
    }

    /* Displays a message */
    public void message(String msg) {
        Toast toast = Toast.makeText(EventViewerActivity.this, msg,
                Toast.LENGTH_LONG);
        toast.show();
    }

    /* Exit */
    public void cancel (View view) {
        finish();
    }

    public void navigate(View view) {
        if (locId.equals(""))
            return;
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .build();
        googleApiClient.connect();
        Places.GeoDataApi.getPlaceById(googleApiClient, locId)
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        if (places.getStatus().isSuccess()) {
                            LatLng location = places.get(0).getLatLng();
                            String searchString =
                                    "google.navigation:q="
                                            + location.latitude + ","
                                            + location.longitude;
                            Uri gmmIntentUri = Uri.parse(searchString);
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");
                            EventViewerActivity.this.startActivity(mapIntent);
                        }
                        places.release();
                    }
                });
    }

    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            //pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight()*1.05;
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}