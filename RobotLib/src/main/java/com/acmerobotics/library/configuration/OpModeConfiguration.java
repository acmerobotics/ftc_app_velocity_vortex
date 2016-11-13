package com.acmerobotics.library.configuration;

import android.content.Context;
import android.content.SharedPreferences;

public class OpModeConfiguration {

    private static final String PREFS_NAME = "opmode";
    private static final String PREF_ALLIANCE_COLOR = "alliance_color";
    private static final String PREF_DELAY = "delay";

    public enum AllianceColor {
        RED(0),
        BLUE(1);
        private int index;
        AllianceColor(int i) {
            index = i;
        }
        public int getIndex() {
            return index;
        }
        public static AllianceColor fromIndex(int i) {
            for (AllianceColor color : AllianceColor.values()) {
                if (color.getIndex() == i) {
                    return color;
                }
            }
            return null;
        }
    }

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public OpModeConfiguration(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public AllianceColor getAllianceColor() {
        return AllianceColor.fromIndex(preferences.getInt(PREF_ALLIANCE_COLOR, 0));
    }

    public void setAllianceColor(AllianceColor color) {
        editor.putInt(PREF_ALLIANCE_COLOR, color.getIndex());
    }

    public int getDelay() {
        return preferences.getInt(PREF_DELAY, 0);
    }

    public void setDelay(int delay) {
        if (delay >= 0 && delay <= 30) {
            editor.putInt(PREF_DELAY, delay);
        }
    }

    public boolean commit() {
        return editor.commit();
    }

}
