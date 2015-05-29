package com.parse.starter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by abby on 5/21/15.
 */
public class InviteHelper {

    private final Context context;

    /* Array of all the names of the user's friends who have NOT been invited. */
    private List<String> friendNames;

    /* Maps a parse user's id to their name. */
    private HashMap<String, String> parseIdToNameMap;

    /* Maps a parse user's name to their id */
    private HashMap<String, String> nameToParseIdMap;

    /* Holds the indices in the friendNames array of invited friends. */
    private ArrayList<Integer> invitedFriendIndices;

    /* Holds the parse ids of the invited friends. */
    private ArrayList<String> invitedParseIds;

    /* Resets the parent's display of the list */
    private Function resetParentDisplay;

    private List<String> fullInviteList;

    /* True if the dialog builder is to display reuseable lists instead of single users */
    /*private boolean listBool;
    public void toggleList(View view) {
        listBool = !listBool;
    }*/
    public InviteHelper (Context context, List<String> alreadyInvitedParseIds, Function resetParentDisplay) {
        this.context = context;
        this.parseIdToNameMap = new HashMap<String, String>();
        this.nameToParseIdMap = new HashMap<String, String>();
        this.friendNames = getFriendNames();
        this.invitedFriendIndices = new ArrayList<Integer>();
        this.invitedParseIds = new ArrayList<String>();
        this.resetParentDisplay = resetParentDisplay;
        this.fullInviteList = alreadyInvitedParseIds;


        // hacky
        resetInviteHelper(alreadyInvitedParseIds);
    }


    /**
     * Builds and opens up the invite dialog popup.
     */
    public void openInviteDialog() {
        boolean[] isCheckedArr = new boolean[friendNames.size()];
        String[] friendNamesArr = new String[friendNames.size()];

        AlertDialog.Builder builder = new AlertDialog.Builder(context);


        // TODO(abby): Put this title in the strings.xml file
        builder.setTitle("Invite your friends")
                .setMultiChoiceItems(friendNames.toArray(friendNamesArr), isCheckedArr, new DialogInterface.OnMultiChoiceClickListener() {
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
                .setPositiveButton("OK", saveCheckboxSelection());

        builder.create();
        builder.show();
    }

    /**
        Resets the invite helper once the event is saved.
        Clears the list of invited parse user ids and the list of indices.
        Filters the array of friend names to contain only the ones that haven't been invited yet

        @param savedInvitedParseIds list of the parse ids of users that have already been invited.
     */
    public void resetInviteHelper(List<String> savedInvitedParseIds) {
        invitedParseIds.clear();
        invitedFriendIndices.clear();

        List<String> invitedFriendNames = new ArrayList<String>();
        for (String id : savedInvitedParseIds) {
            invitedFriendNames.add(parseIdToNameMap.get(id));
        }

        friendNames.removeAll(invitedFriendNames);
    }
    /**
     * Utility method to parse the list of JSON Objects and get an array of friend names.
     *
     * @return list of friend names
     */
    private List<String> getFriendNames() {
        List<HashMap<String, String>> friendJSONObjects =
                (List<HashMap<String, String>>) ParseUser.getCurrentUser().get("friendJSONObjects");

        List<String> friendNameList = new ArrayList<String>();

        for (int i = 0; i < friendJSONObjects.size(); i++) {
            try {
                HashMap<String, String> friendInfo = friendJSONObjects.get(i);
                String name = friendInfo.get("name");
                friendNameList.add(name);

                // Update the map of parse ids to name
                String id = friendInfo.get("id");
                parseIdToNameMap.put(id, name);
                nameToParseIdMap.put(name, id);

            } catch (Exception e) {
                Log.e("EXCEPTION", "Couldn't get name from friend object");
            }
        }

        return friendNameList;

    }

    /**
      Returns the parse ids of the invited users. This includes users who have already clicked
      attending or not attending.

      @return list of parse ids
     */
    public List<String> getInvitedParseIds() {

        return invitedParseIds;
    }


    /**
        Creates a DialogInterface listener to save the checked users.

        @return DialogInterface.OnClickListener
     */
    private DialogInterface.OnClickListener saveCheckboxSelection() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i = 0; i < invitedFriendIndices.size(); i++) {
                    invitedParseIds.add(nameToParseIdMap.get(friendNames.get(i)));
                }

                /* After choosing, update the list in the EventViewerActivity
                 * Update the ListView to reflect the selection
                 * Reset so user can't select a user twice (would lead to crash) */
                fullInviteList.addAll(invitedParseIds);
                resetParentDisplay.call();
                resetInviteHelper(fullInviteList);
            }
        };

        return listener;

    }

    /*
     * Utility method to show Toast popup - used for debugging OK button.
     */
     /*private DialogInterface.OnClickListener testCheckboxSelection() {
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
     }*/
}
