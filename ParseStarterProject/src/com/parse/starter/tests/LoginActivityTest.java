package com.parse.starter.tests;

import android.test.ActivityInstrumentationTestCase2;

import com.parse.starter.LoginActivity;

/**
 * Created by derek on 5/28/15.
 */
public class LoginActivityTest extends ActivityInstrumentationTestCase2<LoginActivity> {

    private LoginActivity mLoginActivity;

    public LoginActivityTest() {
        super(LoginActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLoginActivity = getActivity();
    }

    public void testPreconditions() {
        assertNotNull("mLoginActivity is null", mLoginActivity);
    }
}
