package com.parse.starter.tests;

import android.test.ActivityInstrumentationTestCase2;

import com.parse.ParseObject;
import com.parse.starter.Event;
import com.parse.starter.ParseStarterProjectActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by derek on 5/28/15.
 */
public class ParseStarterProjectActivityTest
        extends ActivityInstrumentationTestCase2<ParseStarterProjectActivity> {

    private ParseStarterProjectActivity mParseStarterProjectActivity;
    private List<Event> mEventList;
    private List<ParseObject> mParseObjectList;

    public ParseStarterProjectActivityTest() {
        super(ParseStarterProjectActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mParseStarterProjectActivity = getActivity();
        mEventList = new ArrayList<>();
        mParseObjectList = new ArrayList<>();
    }

    public void testPreconditions() {
        assertNotNull("mParseStarterProjectActivity is null", mParseStarterProjectActivity);
    }

    public void testFilterEvents() {
        assertNotNull(mParseStarterProjectActivity.filterEvents(mEventList));
    }

    public void testGetLocs() {
        assertNotNull(mParseStarterProjectActivity.getLocs(mParseObjectList));
    }

    public void testGetDates() {
        assertNotNull(mParseStarterProjectActivity.getDates(mParseObjectList));
    }

    public void testGetTitles() {
        assertNotNull(mParseStarterProjectActivity.getTitles(mParseObjectList));
    }

    public void testGetTypes() {
        assertNotNull(mParseStarterProjectActivity.getTypes(mParseObjectList));
    }

    public void testGetIDs() {
        assertNotNull(mParseStarterProjectActivity.getIDs(mParseObjectList));
    }
}
