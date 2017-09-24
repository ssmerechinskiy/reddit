package com.sergey.redditreader.datasource;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by user on 24.09.2017.
 */

public class PreferencesWrapper {
    private static final String PREFERENCES_NAME = "REDDIT_PREFERENCES";

    private static final String KEY_REDDIT_COUNT = "KEY_REDDIT_COUNT";
    private static final String KEY_REDDIT_NAME = "KEY_REDDIT_NAME";

    private SharedPreferences sharedPreferences;

    public PreferencesWrapper(Context context){
        sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public String getRedditName(){
        return sharedPreferences.getString(KEY_REDDIT_NAME, null);
    }

    public void setRedditName(String name){
        sharedPreferences.edit().putString(KEY_REDDIT_NAME, name).apply();
    }

    public int getRedditCount(){
        return sharedPreferences.getInt(KEY_REDDIT_COUNT, 0);
    }

    public void setKeyRedditCount(int count){
        sharedPreferences.edit().putInt(KEY_REDDIT_COUNT, count).apply();
    }
}
