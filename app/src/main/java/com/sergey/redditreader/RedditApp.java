package com.sergey.redditreader;

import android.app.Application;
import android.content.Context;

/**
 * Created by user on 24.09.2017.
 */

public class RedditApp extends Application {
    public static Context sContext = null;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
    }

}
