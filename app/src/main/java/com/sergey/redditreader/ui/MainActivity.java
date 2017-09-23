package com.sergey.redditreader.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.sergey.redditreader.R;
import com.sergey.redditreader.model.RedditChild;
import com.sergey.redditreader.presenter.BasePresenter;
import com.sergey.redditreader.presenter.RedditsPresenter;

import java.util.List;

public class MainActivity extends BaseActivity<RedditsPresenter, RedditsView> implements RedditsView {

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
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//
//            }
//        });

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

        presenter.requestUpdateReddits();

    }

    @Override
    public BasePresenter createPresenter() {
        return new RedditsPresenter();
    }

    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    @Override
    public void updateReddits(List<RedditChild> reddits) {
        Log.d(TAG, "updateReddits:" + reddits.size());
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


    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------
    private void initAdapter(List<RedditChild> reddits) {
        adapter = new RedditsAdapter(this, reddits, new RedditsAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                Log.d(TAG, "onLoadMore");
                presenter.requestAddRedditsPage();
            }
        }, redditListView);
        redditListView.setAdapter(adapter);
    }

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}
