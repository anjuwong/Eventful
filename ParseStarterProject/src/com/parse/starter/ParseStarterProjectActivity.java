package com.parse.starter;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;

import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TabHost;
import android.widget.Toast;

public class ParseStarterProjectActivity extends FragmentActivity
		implements ItemFragment.OnLongFragmentInteractionListener, ItemFragment.OnFragmentInteractionListener {
	private FragmentTabHost mTabHost;
	private int mReloadCount = 1;
    private int currentTypeFilter = 4;

	public void error(String msg) {
		Toast toast = Toast.makeText(ParseStarterProjectActivity.this, msg, Toast.LENGTH_LONG);
		toast.show();
		finish();
	}

	public List<ParseObject> getAllEvents(ParseUser currentUser) {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
		query.whereEqualTo("User", currentUser.getObjectId());
		query.orderByDescending("Time");
		List<ParseObject> events = new ArrayList<>();
		try {
			events = query.find();
		} catch (ParseException e) {
			error("Failed to get user's associated events. Check your internet connection.");
		}
		return events;
	}

    public List<ParseObject> getAllEventsFilteredByType(ParseUser currentUser, int type) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
        query.whereEqualTo("User", currentUser.getObjectId());
        query.orderByDescending("Time");
        List<ParseObject> events = new ArrayList<>();
        try {
            events = query.find();
        } catch (ParseException e) {
            error("Failed to get user's associated events. Check your internet connection.");
        }

        List<ParseObject> filteredEvents = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            ParseObject event = events.get(i);
            if (event.getInt("Type") == type) {
                filteredEvents.add(event);
            }
        }
        return filteredEvents;
    }

	public List<ParseObject> filterEvents(List<Event> events) {
		List<ParseObject> filteredEvents = new ArrayList<>();
		for (int i = 0; i < events.size(); i++) {
			Event event = events.get(i);
			if (event.isValid()) {
				filteredEvents.add(event.getParseObject());
			}
		}
		return filteredEvents;
	}

	public ArrayList<String> getDescriptions(List<ParseObject> events) {
		ArrayList<String> descriptions = new ArrayList<>();
		ArrayList<EventListItem> eventAttribs = new ArrayList<>();
		String description;
		for (int i = 0; i < events.size(); i++) {
			ParseObject event = events.get(i);
			description = event.getString("Title") +
					"\nLocation: " + event.getString("Location") +
					"\nTime: " + event.getDate("Time").toString();
			//eventAttribs.add(new EventListItem("",event.getString("Title"),event.getString("Location"), event.getDate("Time"), event.getInt("Type")));
			descriptions.add(description);
		}
		return descriptions;
	}
	public ArrayList<String> getLocs(List<ParseObject> events) {
		ArrayList<String> locs = new ArrayList<>();
		for (int i = 0; i < events.size(); i++) {
			ParseObject event = events.get(i);
			locs.add(event.getString("Location"));
		}
		return locs;
	}
	public ArrayList<String> getDates(List<ParseObject> events) {
		ArrayList<String> dates = new ArrayList<>();
		for (int i = 0; i < events.size(); i++) {
			ParseObject event = events.get(i);
			dates.add(event.getDate("Time").toString());
		}
		return dates;
	}
	public ArrayList<String> getTitles(List<ParseObject> events) {
		ArrayList<String> titles = new ArrayList<>();
		for (int i = 0; i < events.size(); i++) {
			ParseObject event = events.get(i);
			titles.add(event.getString("Title"));
		}
		return titles;
	}
	public ArrayList<Integer> getTypes(List<ParseObject> events) {
		ArrayList<Integer> types = new ArrayList<>();
		for (int i = 0; i < events.size(); i++) {
			types.add(events.get(i).getInt("Type"));
		}
		return types;
	}

	public ArrayList<String> getIDs(List<ParseObject> events) {
		ArrayList<String> ids = new ArrayList<>();
		for (int i = 0; i < events.size(); i++) {
			ids.add(events.get(i).getObjectId());
		}
		return ids;
	}

	public Bundle genBundle(ArrayList<String> ids, ArrayList<String> titles, ArrayList<String> locs, ArrayList<String> dates, ArrayList<Integer> types) {
		Bundle bundle = new Bundle();
		//bundle.putStringArrayList("descriptions", descriptions);
		bundle.putStringArrayList("dates", dates);
		bundle.putStringArrayList("locs", locs);
		bundle.putStringArrayList("titles", titles);
		bundle.putStringArrayList("ids", ids);
		bundle.putIntegerArrayList("types", types);
		return bundle;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

		ParseAnalytics.trackAppOpenedInBackground(getIntent());
	}

	public void reloadEvents() {
		mTabHost.clearAllTabs();
        ParseUser currentUser = ParseUser.getCurrentUser();
        List<ParseObject> allEvents = getAllEvents(currentUser);

        if(currentTypeFilter == 4) {
            allEvents = getAllEvents(currentUser);
        } else {
            allEvents = getAllEventsFilteredByType(currentUser, currentTypeFilter);
        }

        List<Event> futureEventsBeforeFilter = FutureEvent.getFutureEvents(allEvents);
        List<ParseObject> futureEvents = filterEvents(futureEventsBeforeFilter);
        List<Event> pastEventsBeforeFilter = PastEvent.getPastEvents(allEvents);
        List<ParseObject> pastEvents = filterEvents(pastEventsBeforeFilter);

        Bundle allEventsBundle = genBundle(getIDs(allEvents), getTitles(allEvents), getLocs(allEvents), getDates(allEvents), getTypes(allEvents));
        Bundle futureEventsBundle = genBundle(getIDs(futureEvents), getTitles(futureEvents), getLocs(futureEvents), getDates(futureEvents), getTypes(futureEvents));
        Bundle pastEventsBundle = genBundle(getIDs(pastEvents), getTitles(pastEvents), getLocs(pastEvents), getDates(pastEvents), getTypes(pastEvents));

        mTabHost.addTab(mTabHost.newTabSpec("a" + mReloadCount).setIndicator("All Events"),
				ItemFragment.class, allEventsBundle);
        mTabHost.addTab(mTabHost.newTabSpec("f" + mReloadCount).setIndicator("Future Events"),
                ItemFragment.class, futureEventsBundle);
        mTabHost.addTab(mTabHost.newTabSpec("p" + mReloadCount).setIndicator("Past Events"),
				ItemFragment.class, pastEventsBundle);

        mReloadCount++;


	}

    public void filterEventsByType(View view) {
        String[] actList = {"Food", "Workout", "Chill", "Game", "All"};
        AlertDialog.Builder builder = new AlertDialog.Builder(ParseStarterProjectActivity.this);
        builder.setTitle("Choose an Activity")
                .setItems(actList, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						currentTypeFilter = which;
						reloadEvents();
					}
				});
        builder.create();
        builder.show();
    }

	public void onFragmentInteraction(String id) {
		Intent intent = new Intent(this, EventViewerActivity.class);
		Bundle extras = new Bundle();
		extras.putString("EVENT_ID", id);
		extras.putBoolean("CLONE", false);
		//intent.putExtra("EVENT_ID", id);
		intent.putExtras(extras);
		startActivity(intent);
	}

	public void onLongFragmentInteraction(String id) {
		Intent intent = new Intent(this, EventViewerActivity.class);
		Bundle extras = new Bundle();
		extras.putString("EVENT_ID", id);
		extras.putBoolean("CLONE", true);
		//intent.putExtra("EVENT_ID", id);
		intent.putExtras(extras);
		startActivity(intent);
	}

	public void newEvent(View view) {
		Intent intent = new Intent(this, EventViewerActivity.class);
		intent.putExtra("EVENT_ID", "");
		startActivity(intent);
	}

	@Override
	public void onResume() {
		super.onResume();
		reloadEvents();
	}

	@Override
	public void onBackPressed() {}


}