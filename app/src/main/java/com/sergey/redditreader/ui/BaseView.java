package com.sergey.redditreader.ui;

import com.sergey.redditreader.presenter.BasePresenter;

/**
 * Created by sober on 22.09.2017.
 */

public interface BaseView {
    long getId();
    BasePresenter createPresenter();
}
