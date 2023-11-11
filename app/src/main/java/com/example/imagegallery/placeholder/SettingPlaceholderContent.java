package com.example.imagegallery.placeholder;

import static android.provider.Settings.System.getString;

import com.example.imagegallery.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class SettingPlaceholderContent {

    /**
     * Setting placeholder item. Contains the id, property, and value of each setting property.
     */

    public static final List<SettingPlaceholderItem> ITEMS = new ArrayList<SettingPlaceholderItem>();

    public static final Map<String, SettingPlaceholderItem> ITEM_MAP = new HashMap<String, SettingPlaceholderItem>();

    public static void addItem(SettingPlaceholderItem item) {
        // check if the setting id already exists by checking the map
        if(ITEM_MAP.containsKey(item.id)) {
            // if it does,  do nothing
        } else {
            ITEMS.add(item);
            ITEM_MAP.put(item.id, item);
        }

    }

    public SettingPlaceholderContent() {
    }

    public static class SettingPlaceholderItem {
        public String id;
        public String property;
        public String value;

        public SettingPlaceholderItem(String id, String property, String value) {
            this.id = id;
            this.property = property;
            this.value = value;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }
}