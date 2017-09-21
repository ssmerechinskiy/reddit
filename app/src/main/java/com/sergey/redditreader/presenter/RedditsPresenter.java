package com.sergey.redditreader.presenter;

import android.os.Handler;
import android.os.Looper;

import com.sergey.redditreader.datasource.RedditNetworkDS;
import com.sergey.redditreader.datasource.RedditNetworkDSImpl;
import com.sergey.redditreader.model.RedditChild;
import com.sergey.redditreader.model.RedditResponse;
import com.sergey.redditreader.ui.RedditsView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by user on 21.09.2017.
 */

public class RedditsPresenter extends BasePresenter {

    private String redditName = "BlackMetal";
    private final static int pageLimit = 5;
    private String redditAfterName = "";

    private List<RedditChild> reddits = new ArrayList<>();

    private RedditsView view;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private RedditNetworkDS networkDS = RedditNetworkDSImpl.INSTANCE;

    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public RedditsPresenter(RedditsView v) {
        view = v;
    }

    public void requestUpdateReddits() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                final RedditResponse response = networkDS.getRedditResponsePage(redditName, null, pageLimit);
                final List<RedditChild> newReddits = response.data.children;
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        redditAfterName = response.data.after;
                        reddits.addAll(newReddits);
                        if(view != null) view.updateReddits(reddits);
                    }
                });
            }
        });
    }

    public void requestAddRedditsPage() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                final RedditResponse response = networkDS.getRedditResponsePage(redditName, redditAfterName, pageLimit);
                final List<RedditChild> newReddits = response.data.children;
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        redditAfterName = response.data.after;
                        reddits.addAll(newReddits);
                        if(view != null) view.addReddits(newReddits);
                    }
                });
            }
        });
    }

    public List<RedditChild> getReddits() {
        return reddits;
    }

    private void releaseExecutor() {
        if(executorService == null) return;
        executorService.shutdown();
        executorService.shutdownNow();
        executorService = null;
    }



}
