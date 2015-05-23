package com.parse.starter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by abby on 5/21/15.
 */
public class InviteHelper {

    private final Context context;

    /* Array of all the names of the user's friends. */
    private final String[] friendNames;

    /* Holds the indices in the friendNames array of invited friends. */
    private ArrayList<Integer> invitedFriendIndices;

    public InviteHelper (Context context) {
        this.context = context;
        this.friendNames = getFriendNames();
        this.invitedFriendIndices = new ArrayList<Integer>();
    }

    /*
     * Builds and opens up the invite dialog popup.
     */
    public void openInviteDialog() {
        boolean[] isCheckedArr = new boolean[friendNames.length];

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // TODO(abby): Put this title in the strings.xml file
        builder.setTitle("Invite your friends")
                .setMultiChoiceItems(friendNames, isCheckedArr, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position, boolean isChecked) {
                        if (isChecked) {
                            // If the checkbox is checked, add the position to the list of indices.
                            invitedFriendIndices.add(position);
                        } else if (invitedFriendIndices.contains(position)) {
                            // If the position is already in the list, remove it.
                            invitedFriendIndices.remove(Integer.valueOf(position));
                        }
                    }
                })
                .setPositiveButton("OK", testCheckboxSelection());
        builder.create();
        builder.show();
    }

    /*
     * Utility method to parse the list of JSON Objects and get an array of friend names.
     */
    private String[] getFriendNames() {
        List<HashMap<String, String>> friendJSONObjects =
                (List<HashMap<String, String>>) ParseUser.getCurrentUser().get("friendJSONObjects");

        List<String> friendNameList = new ArrayList<String>();

        for (int i = 0; i < friendJSONObjects.size(); i++) {
            try {
                HashMap<String, String> friendInfo = friendJSONObjects.get(i);
                String name = friendInfo.get("name");
                friendNameList.add(name);
            } catch (Exception e) {
                Log.e("EXCEPTION", "Couldn't get name from friend object");
            }
        }

        String[] friendNameArray = new String[friendNameList.size()];
        friendNameArray = friendNameList.toArray(friendNameArray);

        return friendNameArray;

    }

    /*
     * Utility method to show Toast popup - used for debugging OK button.
     */
     private DialogInterface.OnClickListener testCheckboxSelection() {
         DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 String msg = "";
                 for (int i = 0; i < invitedFriendIndices.size(); i++) {
                     msg = msg + "\n" + (i + 1) + " : " + friendNames[invitedFriendIndices.get(i)];
                 }
                 Toast.makeText(context,
                         "Total " + invitedFriendIndices.size() + " Items Selected.\n" + msg, Toast.LENGTH_LONG)
                         .show();
             }
         };

         return listener;
     }
}
