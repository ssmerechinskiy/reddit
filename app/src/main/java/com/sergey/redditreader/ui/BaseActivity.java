package com.sergey.redditreader.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.sergey.redditreader.presenter.BaseActivityPresenter;
import com.sergey.redditreader.presenter.PresenterManager;

/**
 * Created by sober on 22.09.2017.
 */

public abstract class BaseActivity<P extends BaseActivityPresenter, V extends BaseView> extends AppCompatActivity
        implements BaseView {

    private final static String TAG = BaseActivity.class.getSimpleName();

    private final static String VIEW_ID = "VIEW_ID";

    private long id;
    protected P presenter;
    protected V view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null && savedInstanceState.containsKey(VIEW_ID)) {
            Log.i(TAG, "onCreate: restore state");
            id = savedInstanceState.getLong(VIEW_ID);
        } else {
            Log.i(TAG, "onCreate: create new");
            id = System.currentTimeMillis();
        }
        view = (V) this;
        presenter = PresenterManager.INSTANCE.initPresenter(view);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        PresenterManager.INSTANCE.markViewToSaveInstance(id);
        outState.putLong(VIEW_ID, id);
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        presenter.onPostCreate();
    }

    protected void onStart() {
        super.onStart();
        presenter.onStart();
    }

    protected void onStop() {
        super.onStop();
        presenter.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PresenterManager.INSTANCE.releaseRedditsPresenterForView(id);
        presenter.onDestroy();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public abstract BaseActivityPresenter createPresenter();

}
