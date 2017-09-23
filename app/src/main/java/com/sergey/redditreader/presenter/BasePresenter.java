package com.sergey.redditreader.presenter;

import com.sergey.redditreader.ui.BaseView;

/**
 * Created by user on 22.09.2017.
 */

public abstract class BasePresenter<T extends BaseView> {
    private final long id;
    protected T view;

    public BasePresenter() {
        id = System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

//    public T getView() {
//        return view;
//    }

    public void setView(T v) {
        view = v;
    }
}
