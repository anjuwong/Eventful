package com.parse.starter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by andrew on 5/22/15.
 */
public class EventListAdapter extends ArrayAdapter<EventListItem> {

    private List<EventListItem> items;

    public EventListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        // TODO Auto-generated constructor stub
    }

    public EventListAdapter(Context context, int resource, List<EventListItem> objects) {
        super(context, resource, objects);
        this.items = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {

            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.main_row, null);

        }

        EventListItem p = items.get(position);

        if (p != null) {

            TextView title_view = (TextView) v.findViewById(R.id.main_row_title);
            TextView loc_view = (TextView) v.findViewById(R.id.main_row_location);
            TextView date_view = (TextView) v.findViewById(R.id.main_row_time);
            ImageView icon = (ImageView) v.findViewById(R.id.ic);
            if (title_view != null) {
                title_view.setText(p.getTitle());
            }
            if (loc_view != null) {

                loc_view.setText(p.getLoc());
            }
            if (date_view != null) {

                date_view.setText(p.getDate());
            }
            if (icon != null) {

                int type = p.getType();
                switch(type) {
                    case -1:
                        icon.setImageResource(R.drawable.ic_blank);
                        break;
                    case 0:// food
                        icon.setImageResource(R.drawable.ic_food);
                        break;
                    case 1://workout
                        icon.setImageResource(R.drawable.ic_workout);
                        break;
                    case 2://chill
                        icon.setImageResource(R.drawable.ic_chill);
                        break;
                    case 3://game
                        icon.setImageResource(R.drawable.ic_game);
                        break;
                }
            }
        }

        return v;

    }
}
