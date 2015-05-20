package com.parse.starter;

import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * Helper class to get a facebook user's name, profile picture, and friend list.
 * Created by abby on 5/19/15.
 */
public class FacebookHelper {

    private static FacebookHelper facebookHelper = new FacebookHelper();

    /* Make the constructor private so no other class can instantiate. */
    private FacebookHelper() {}

    public static FacebookHelper getInstance() {
        return facebookHelper;
    }

    /*
     * Query the Facebook Graph API for the name and ID of the user.
     */
    protected static void getNameAndId() {
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject jsonObject, GraphResponse response) {
                        Log.d("GRAPH_REQUEST", "onCompleted jsonObject: " + jsonObject);
                        Log.d("GRAPH_REQUEST", "onCompleted response: " + response);
                    }
                });

        // Select only the id and name.
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name");
        request.setParameters(parameters);

        // Execute the request in the background.
        request.executeAsync();
        return;
    }

    /*
     * Query the Facebook Graph API for the user's friends that also use Eventful.
     */
    protected static void getFriendList() {
        GraphRequest request = GraphRequest.newMyFriendsRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONArrayCallback() {
                    @Override
                    public void onCompleted(JSONArray jsonArray, GraphResponse response) {
                        Log.d("GRAPH_REQUEST", "onCompleted jsonArray: " + jsonArray);
                        Log.d("GRAPH_REQUEST", "onCompleted response: " + response);
                    }
                });

        request.executeAsync();
        return;

    }

}
