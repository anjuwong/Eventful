package com.parse.starter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import java.util.Arrays;
import java.util.List;

/**
 * Created by abby on 5/7/15.
 */
public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //If current user exists and is already linked to a FB account
        ParseUser currentUser = ParseUser.getCurrentUser();
        if ((currentUser != null) &&
                currentUser.has("Name") &&
                currentUser.has("FacebookID") &&
                ParseFacebookUtils.isLinked(currentUser)) {
            openMainPage();
        } else {
            initLogin();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    /*
     * Login to Facebook and store the user ID.
     */
    public void initLogin() {
        List<String> permissions = Arrays.asList("public_profile", "email", "user_friends");

        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, permissions, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                if (user != null) {
                    // Query and store the user's Facebook ID and Name
                    getNameAndFacebookID();
                    openMainPage();
                }

            }
        });
    }

    public void openMainPage() {
        Intent intent = new Intent(this, ParseStarterProjectActivity.class);
        startActivity(intent);
    }

    /*
     * Check if the Facebook ID is stored in Parse. If not, get it.
     */
    private void getNameAndFacebookID() {
        FacebookHelper facebookHelper = FacebookHelper.getInstance();
        facebookHelper.getNameAndId(ParseUser.getCurrentUser());
    }
}
