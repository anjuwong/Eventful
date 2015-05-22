package com.parse.starter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by abby on 5/21/15.
 */
public class InviteHelper {

    public void openInviteDialog(Context context) {
        String[] friendNames = getFriendNames();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // TODO(abby): Change to MultipleClickableItems
        builder.setTitle("Invite a friend")
                .setItems(friendNames, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //todo
                    }
                });
        builder.create();
        builder.show();
    }

    /*
     * Utility method to parse the list of JSON Objects and get an array of friend names.
     */
    private String[] getFriendNames() {
        List<JSONObject> friendJSONObjects =
                (List<JSONObject>) ParseUser.getCurrentUser().get("friendJSONObjects");

        List<String> friendNameList = new ArrayList<String>();
        for (JSONObject obj : friendJSONObjects) {
            try {
                friendNameList.add(obj.get("name").toString());
            } catch (JSONException e) {
                Log.e("JSON_EXCEPTION", "Couldn't get name from friend object");
            }
        }

        String[] friendNameArray = new String[friendNameList.size()];
        friendNameArray = friendNameList.toArray(friendNameArray);

        return friendNameArray;

    }
}
