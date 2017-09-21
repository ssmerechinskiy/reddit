package com.sergey.redditreader.presenter;

/**
 * Created by user on 22.09.2017.
 */

public class BasePresenter {
    private final long id;

    public BasePresenter() {
        id = System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }
}
