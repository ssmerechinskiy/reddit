package com.sergey.redditreader.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sergey.redditreader.presenter.BasePresenter;
import com.sergey.redditreader.presenter.PresenterManager;

/**
 * Created by sober on 22.09.2017.
 */

public abstract class BaseActivity<P extends BasePresenter, V extends BaseView> extends AppCompatActivity
        implements BaseView {

    private long id;
    protected P presenter;
    protected V view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id = System.currentTimeMillis();
        view = (V) this;
        presenter = PresenterManager.INSTANCE.initPresenter(view);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        PresenterManager.INSTANCE.markViewToSaveInstance(id);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // TODO: 22.09.2017 restore activiti id and get presenter for it
//        id = System.currentTimeMillis();
//        presenter = PresenterManager.INSTANCE.initPresenter(view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PresenterManager.INSTANCE.releaseRedditsPresenter(id);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public abstract BasePresenter createPresenter();


    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------


}
