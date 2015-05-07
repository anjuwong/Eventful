package com.parse.starter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by derek on 5/7/15.
 */

public class ListContent {

    /**
     * An array of list items.
     */
    public List<ListItem> ITEMS = new ArrayList<ListItem>();

    /**
     * A map of list items, by ID.
     */
    public Map<String, ListItem> ITEM_MAP = new HashMap<String, ListItem>();

    public ListContent() {}

    public void addItem(ListItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A list item representing a piece of content.
     */
    public static class ListItem {
        public String id;
        public String description;

        public ListItem(String id, String description) {
            this.id = id;
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}