package com.parse.starter;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

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

        if (isLoginNeeded()) {
            initLogin();
        } else {
            // Query and store the list of friends
            getFriendsList();
            openMainPage();
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
                    // Query and store the user's Facebook ID, name, and friends list
                    getNameAndFacebookID();
                    getFriendsList();
                    openMainPage();
                }

            }
        });
    }

    /*
     * Check if the user needs to log in to Facebook.
     */
    private boolean isLoginNeeded() {
        ParseUser currentUser = ParseUser.getCurrentUser();
         return !((currentUser != null) &&
                    currentUser.has("Name") &&
                        currentUser.has("FacebookID") &&
                            ParseFacebookUtils.isLinked(currentUser));
    }

    /*
     * Open up the main view.
     */
    private void openMainPage() {
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

    /*
     * Get and store the user's friend list. This is done every time the user starts the app.
     */
    private void getFriendsList() {
        FacebookHelper facebookHelper = FacebookHelper.getInstance();
        facebookHelper.getFriendList();
    }

    private void createNotification(String title, String msg) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle(title)
                        .setAutoCancel(true)
                        .setContentText(msg);

        Intent notifyIntent =
                new Intent(this, ParseStarterProjectActivity.class);
        // Sets the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Creates the PendingIntent
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(notifyPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, mBuilder.build());
    }
}
