package com.parse.starter;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;

import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.view.View;
import android.widget.Toast;

public class ParseStarterProjectActivity extends FragmentActivity
		implements ItemFragment.OnFragmentInteractionListener {
	private FragmentTabHost mTabHost;
	private int mReloadCount = 1;

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
		String description;
		for (int i = 0; i < events.size(); i++) {
			ParseObject event = events.get(i);
			description = event.getString("Title") +
					"\nLocation: " + event.getString("Location") +
					"\nTime: " + event.getDate("Time").toString();
			descriptions.add(description);
		}
		return descriptions;
	}

	public ArrayList<String> getIDs(List<ParseObject> events) {
		ArrayList<String> ids = new ArrayList<>();
		for (int i = 0; i < events.size(); i++) {
			ids.add(events.get(i).getObjectId());
		}
		return ids;
	}

	public Bundle genBundle(ArrayList<String> descriptions, ArrayList<String> ids) {
		Bundle bundle = new Bundle();
		bundle.putStringArrayList("descriptions", descriptions);
		bundle.putStringArrayList("ids", ids);
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
		List<Event> futureEventsBeforeFilter = FutureEvent.getFutureEvents(allEvents);
		List<ParseObject> futureEvents = filterEvents(futureEventsBeforeFilter);
		List<Event> pastEventsBeforeFilter = PastEvent.getPastEvents(allEvents);
		List<ParseObject> pastEvents = filterEvents(pastEventsBeforeFilter);

		Bundle allEventsBundle = genBundle(getDescriptions(allEvents), getIDs(allEvents));
		Bundle futureEventsBundle = genBundle(getDescriptions(futureEvents), getIDs(futureEvents));
		Bundle pastEventsBundle = genBundle(getDescriptions(pastEvents), getIDs(pastEvents));

		mTabHost.addTab(mTabHost.newTabSpec("a" + mReloadCount).setIndicator("All Events"),
				ItemFragment.class, allEventsBundle);
		mTabHost.addTab(mTabHost.newTabSpec("f" + mReloadCount).setIndicator("Future Events"),
				ItemFragment.class, futureEventsBundle);
		mTabHost.addTab(mTabHost.newTabSpec("p" + mReloadCount).setIndicator("Past Events"),
				ItemFragment.class, pastEventsBundle);

		mReloadCount++;
	}

	public void onFragmentInteraction(String id) {
		Intent intent = new Intent(this, EventViewerActivity.class);
		intent.putExtra("EVENT_ID", id);
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