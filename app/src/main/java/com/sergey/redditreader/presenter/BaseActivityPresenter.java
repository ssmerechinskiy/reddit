package com.sergey.redditreader.presenter;

import com.sergey.redditreader.ui.BaseView;

/**
 * Created by user on 22.09.2017.
 */

public abstract class BaseActivityPresenter<T extends BaseView> {
    private final long id;
    protected T view;
    protected boolean activityRecreated;

    public BaseActivityPresenter() {
        id = System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }


    public void setView(T v) {
        view = v;
    }

    public void setActivityRecreated() {
        activityRecreated = true;
    }

    public abstract void onPostCreate();
    public abstract void onStart();
    public abstract void onStop();
    public abstract void onDestroy();

}
