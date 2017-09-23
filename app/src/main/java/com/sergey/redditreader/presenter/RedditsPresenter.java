package com.sergey.redditreader.presenter;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.sergey.redditreader.datasource.RedditNetworkDS;
import com.sergey.redditreader.datasource.RedditNetworkDSImpl;
import com.sergey.redditreader.model.RedditChild;
import com.sergey.redditreader.model.RedditResponse;
import com.sergey.redditreader.task.Task;
import com.sergey.redditreader.ui.MainActivity;
import com.sergey.redditreader.ui.RedditsView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by user on 21.09.2017.
 */

public class RedditsPresenter extends BasePresenter<RedditsView> {
    private final static String TAG = RedditsPresenter.class.getSimpleName();

    private String redditName = "BlackMetal";
    private final static int pageLimit = 5;
    private String redditAfterName = "";

    private List<RedditChild> reddits = new ArrayList<>();

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private RedditNetworkDS networkDS = RedditNetworkDSImpl.INSTANCE;

    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private List<Task> currentTasks = new ArrayList<>();

    public RedditsPresenter() {
    }

    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------
    public void requestUpdateReddits() {
        Log.d(TAG, "requestUpdateReddits");
        Task task = new Task() {
            @Override
            public RedditResponse performAction() throws Exception {
                return networkDS.getRedditResponsePage(redditName, null, pageLimit);
            }
        }.executor(executorService);
        if(view != null) view.showRefreshingProgress();
        task.execute((new Task.ResultListener<RedditResponse>() {
            @Override
            public void onSuccess(RedditResponse response) {
                handleUpdateRedditsSuccess(response);
            }
            @Override
            public void onError(Throwable error) {
                handleUpdateRedditsError(error);
            }
        }).handler(mainThreadHandler));
    }

    private void handleUpdateRedditsSuccess(RedditResponse response) {
        Log.d(TAG, "updating new reddits:" + response.data.children.size());
        redditAfterName = response.data.after;
        reddits.clear();
        reddits.addAll(response.data.children);
        if(view != null) view.hideRefreshingProgress();
        if(view != null) view.updateReddits(reddits);
    }

    private void handleUpdateRedditsError(Throwable error) {
        if(view != null) view.hideRefreshingProgress();
        if(view != null) view.showToastMessage(error.getMessage());
    }

    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------
    public void requestAddRedditsPage() {
        Log.d(TAG, "requestAddReddits");
        Task task = new Task() {
            @Override
            public RedditResponse performAction() throws Exception {
                return networkDS.getRedditResponsePage(redditName, redditAfterName, pageLimit);
            }
        }.executor(executorService);
        if(view != null) view.showLoadMoreProgress();
        task.execute((new Task.ResultListener<RedditResponse>() {
            @Override
            public void onSuccess(RedditResponse response) {
                handleAddRedditsSuccess(response);
            }
            @Override
            public void onError(Throwable error) {
                handleAddRedditsError(error);
            }
        }).handler(mainThreadHandler));
    }

    private void handleAddRedditsSuccess(RedditResponse response) {
        Log.d(TAG, "adding new reddits:" + response.data.children.size());
        redditAfterName = response.data.after;
        reddits.addAll(response.data.children);
        if(view != null) view.hideLoadMoreProgress();
        if(view != null) view.addReddits(response.data.children);
    }

    private void handleAddRedditsError(Throwable error) {
        if(view != null) view.hideLoadMoreProgress();
        if(view != null) view.showToastMessage(error.getMessage());
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
