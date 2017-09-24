package com.sergey.redditreader.ui;

import android.content.Context;

import com.sergey.redditreader.presenter.BaseActivityPresenter;

/**
 * Created by sober on 22.09.2017.
 */

public interface BaseView {
    long getId();
    BaseActivityPresenter createPresenter();
    Context getContext();
}
