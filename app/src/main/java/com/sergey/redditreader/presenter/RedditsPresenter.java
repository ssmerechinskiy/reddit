package com.sergey.redditreader.presenter;

import android.os.Handler;
import android.os.Looper;

import com.sergey.redditreader.datasource.RedditNetworkDS;
import com.sergey.redditreader.datasource.RedditNetworkDSImpl;
import com.sergey.redditreader.model.RedditChild;
import com.sergey.redditreader.model.RedditResponse;
import com.sergey.redditreader.task.Task;
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

    private RedditsView redditsView;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private RedditNetworkDS networkDS = RedditNetworkDSImpl.INSTANCE;

    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private List<Task> currentTasks = new ArrayList<>();

    public RedditsPresenter() {
    }

    public void requestUpdateReddits() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                final RedditResponse response;
                try {
                    response = networkDS.getRedditResponsePage(redditName, null, pageLimit);
                    final List<RedditChild> newReddits = response.data.children;
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            redditAfterName = response.data.after;
                            reddits.addAll(newReddits);
                            if(redditsView != null) redditsView.updateReddits(reddits);
                        }
                    });
                } catch (final Exception e) {
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(redditsView != null) redditsView.showMessage(e.getMessage());
                        }
                    });
                }
            }
        });
    }

    public void requestUpdateReddits2() {
        Task task = new Task() {
            @Override
            public RedditResponse performAction() throws Exception {
                return networkDS.getRedditResponsePage(redditName, null, pageLimit);
            }
        }.executor(executorService);
        task.execute(new Task.ResultListener<RedditResponse>() {
            @Override
            public void onSuccess(RedditResponse response) {
                redditAfterName = response.data.after;
                reddits.addAll(response.data.children);
                if(redditsView != null) redditsView.updateReddits(reddits);
            }
            @Override
            public void onError(Throwable error) {
                if(redditsView != null) redditsView.showMessage(error.getMessage());
            }
        });
    }

    public void requestAddRedditsPage() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                final RedditResponse response;
                try {
                    response = networkDS.getRedditResponsePage(redditName, redditAfterName, pageLimit);
                    final List<RedditChild> newReddits = response.data.children;
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            redditAfterName = response.data.after;
                            reddits.addAll(newReddits);
                            if(redditsView != null) redditsView.addReddits(newReddits);
                        }
                    });
                } catch (final Exception e) {
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(redditsView != null) redditsView.showMessage(e.getMessage());
                        }
                    });
                }
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
