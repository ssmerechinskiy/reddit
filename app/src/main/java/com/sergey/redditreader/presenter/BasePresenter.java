package com.sergey.redditreader.presenter;

import com.sergey.redditreader.ui.BaseView;

/**
 * Created by user on 22.09.2017.
 */

public abstract class BasePresenter {
    private final long id;
    private BaseView view;

    public BasePresenter() {
        id = System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

    public BaseView getView() {
        return view;
    }

    public void setView(BaseView v) {
        view = v;
    }
}
