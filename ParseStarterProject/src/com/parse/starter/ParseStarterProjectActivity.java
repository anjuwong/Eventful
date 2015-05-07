package com.parse.starter;

import java.util.ArrayList;
import java.util.Date;
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

import android.widget.Toast;

public class ParseStarterProjectActivity extends FragmentActivity
		implements ItemFragment.OnFragmentInteractionListener {
	private FragmentTabHost mTabHost;

	public void error(String msg) {
		Toast toast = Toast.makeText(ParseStarterProjectActivity.this, msg, Toast.LENGTH_LONG);
		toast.show();
		finish();
	}

	public List<ParseObject> allEvents(ParseUser currentUser) {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
		query.whereEqualTo("User", currentUser.getObjectId());
		List<ParseObject> events = new ArrayList<>();
		try {
			events = query.find();
		} catch (ParseException e) {
			error("Failed to get user's associated events");
		}
		return events;
	}

	public List<ParseObject> futureEvents(List<ParseObject> allEvents) {
		List<ParseObject> futureEvents = new ArrayList<>();
		Date currentDate = new Date();
		for (int i = 0; i < allEvents.size(); i++) {
			ParseObject event = allEvents.get(i);
			if (event.getDate("Time").after(currentDate)) {
				futureEvents.add(event);
			}
		}
		return futureEvents;
	}

	public List<ParseObject> pastEvents(List<ParseObject> allEvents) {
		List<ParseObject> pastEvents = new ArrayList<>();
		Date currentDate = new Date();
		for (int i = 0; i < allEvents.size(); i++) {
			ParseObject event = allEvents.get(i);
			if (event.getDate("Time").before(currentDate)) {
				pastEvents.add(event);
			}
		}
		return pastEvents;
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

	public Bundle bundle(ArrayList<String> descriptions, ArrayList<String> ids) {
		Bundle bundle = new Bundle();
		bundle.putStringArrayList("descriptions", descriptions);
		bundle.putStringArrayList("ids", ids);
		return bundle;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		try {
			ParseUser.logIn("Andrew", "password");
		} catch (ParseException e) {

		}

		ParseUser currentUser = ParseUser.getCurrentUser();
		List<ParseObject> allEvents = allEvents(currentUser);
		List<ParseObject> futureEvents = futureEvents(allEvents);
		List<ParseObject> pastEvents = pastEvents(allEvents);

		Bundle allEventsBundle = bundle(getDescriptions(allEvents), getIDs(allEvents));
		Bundle futureEventsBundle = bundle(getDescriptions(futureEvents), getIDs(futureEvents));
		Bundle pastEventsBundle = bundle(getDescriptions(pastEvents), getIDs(pastEvents));

		mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

		mTabHost.addTab(mTabHost.newTabSpec("all").setIndicator("All Events"),
				ItemFragment.class, allEventsBundle);
		mTabHost.addTab(mTabHost.newTabSpec("future").setIndicator("Future Events"),
				ItemFragment.class, futureEventsBundle);
		mTabHost.addTab(mTabHost.newTabSpec("past").setIndicator("Past Events"),
				ItemFragment.class, pastEventsBundle);

		ParseAnalytics.trackAppOpenedInBackground(getIntent());
	}

	public void onFragmentInteraction(String id) {
		/*Intent intent = new Intent(this, EventViewerActivity.class);
		intent.putExtra("EVENT_ID", id);
		startActivity(intent);*/
		Toast toast = Toast.makeText(ParseStarterProjectActivity.this, id, Toast.LENGTH_LONG);
		toast.show();
	}

	@Override
	public void onBackPressed() {}
}