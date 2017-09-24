package com.sergey.redditreader.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sergey.redditreader.R;
import com.sergey.redditreader.model.RedditChild;
import com.sergey.redditreader.presenter.BaseActivityPresenter;
import com.sergey.redditreader.presenter.RedditsActivityPresenter;

import java.util.List;

public class MainActivity extends BaseActivity<RedditsActivityPresenter, RedditsView> implements RedditsView {

    private final static String TAG = MainActivity.class.getSimpleName();

    private SwipeRefreshLayout swipeContainer;
    private RecyclerView redditListView;
    private RedditsAdapter adapter;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        snackbar = Snackbar.make(findViewById(android.R.id.content), "Loading next page...", Snackbar.LENGTH_LONG);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "onRefresh");
                presenter.requestUpdateReddits();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        redditListView = (RecyclerView) findViewById(R.id.reddit_list);
        redditListView.setLayoutManager(new LinearLayoutManager(this));

//        presenter.requestUpdateReddits();

    }

//    protected void onPostCreate(Bundle savedInstanceState) {
//        super.onPostCreate(savedInstanceState);
//        presenter.onPostCreate();
//    }
//
//    protected void onStart() {
//        super.onStart();
//        presenter.onStart();
//    }
//
//    protected void onStop() {
//        super.onStop();
//        presenter.onStop();
//    }
//
//    protected void onDestroy() {
//        super.onDestroy();
//        presenter.onDestroy();
//    }

    @Override
    public BaseActivityPresenter createPresenter() {
        return new RedditsActivityPresenter();
    }

    @Override
    public Context getContext() {
        return this;
    }

    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------
    @Override
    public void updateReddits(List<RedditChild> reddits) {
        Log.d(TAG, "updateReddits:" + reddits.size());
//        initAdapter(reddits);
        if(adapter == null) {
            initAdapter(reddits);
        } else {
            adapter.updateItems(reddits);
        }

    }

    @Override
    public void addReddits(List<RedditChild> reddits) {
        Log.d(TAG, "addReddits:" + reddits.size());
        adapter.addItems(reddits);
        adapter.setLoaded();
    }

    @Override
    public void showToastMessage(String message) {

    }

    @Override
    public void showRefreshingProgress() {
        swipeContainer.setRefreshing(true);
    }

    @Override
    public void hideRefreshingProgress() {
        swipeContainer.setRefreshing(false);
    }

    @Override
    public void showLoadMoreProgress() {
        Log.d(TAG, "showLoadMoreProgress");
//        if(snackbar.isShown()) snackbar.dismiss();
        snackbar.show();
    }

    @Override
    public void hideLoadMoreProgress() {
        if(snackbar.isShown()) snackbar.dismiss();
    }

    @Override
    public void updateViewTitle(String title) {
        getSupportActionBar().setTitle(title);
    }


    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------
    private void initAdapter(List<RedditChild> reddits) {
        adapter = new RedditsAdapter(this, reddits, new RedditsAdapter.Callback() {
            @Override
            public void onLoadMore() {
                Log.d(TAG, "onLoadMore");
                presenter.requestAddRedditsPage();
            }

            @Override
            public void onItemClick(RedditChild redditChild) {
                presenter.onRedditClick(redditChild, MainActivity.this);
            }
        }, redditListView);
        redditListView.setAdapter(adapter);
    }

    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            showDialog(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void showDialog(Activity activity){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.enter_name_layout);


        final EditText name = (EditText) dialog.findViewById(R.id.name);
        name.setText(presenter.getRedditName());

        Button dialogButton = (Button) dialog.findViewById(R.id.ok);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onChangeRedditName(name.getText().toString());
                dialog.dismiss();
            }
        });

        dialog.show();

    }


}
