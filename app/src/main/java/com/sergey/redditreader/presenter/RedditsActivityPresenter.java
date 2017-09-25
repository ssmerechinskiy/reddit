package com.sergey.redditreader.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.sergey.redditreader.RedditApp;
import com.sergey.redditreader.datasource.PreferencesWrapper;
import com.sergey.redditreader.datasource.RedditNetworkDS;
import com.sergey.redditreader.datasource.RedditNetworkDSImpl;
import com.sergey.redditreader.model.RedditChild;
import com.sergey.redditreader.model.RedditResponse;
import com.sergey.redditreader.task.Task;
import com.sergey.redditreader.ui.ImageViwerActivity;
import com.sergey.redditreader.ui.RedditsView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by user on 21.09.2017.
 */

public class RedditsActivityPresenter extends BaseActivityPresenter<RedditsView> {
    private final static String TAG = RedditsActivityPresenter.class.getSimpleName();

    private String redditName = "BlackMetal";
    private final static int pageLimit = 10;
    private String redditAfterName = "";

    private List<RedditChild> reddits = new ArrayList<>();

    private ExecutorService executorService;
    private RedditNetworkDS networkDS = RedditNetworkDSImpl.INSTANCE;

    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());;

    private PreferencesWrapper preferencesWrapper;

    private Task updateRedditsTask;
    private Task addRedditsPageTask;
    private Task.ResultListener updateRedditsListener;
    private Task.ResultListener addRedditsListener;

    public RedditsActivityPresenter() {
        Log.d(TAG, "created new presenter");
        init();
    }

    private void init() {
        executorService = Executors.newSingleThreadExecutor();
        preferencesWrapper = new PreferencesWrapper(RedditApp.sContext);
        updateRedditsListener  = new Task.ResultListener<RedditResponse>() {
            @Override
            public void onSuccess(RedditResponse response) {
                handleUpdateRedditsSuccess(response);
            }
            @Override
            public void onError(Throwable error) {
                handleUpdateRedditsError(error);
            }
        };
        addRedditsListener = new Task.ResultListener<RedditResponse>() {
            @Override
            public void onSuccess(RedditResponse response) {
                handleAddRedditsSuccess(response);
            }
            @Override
            public void onError(Throwable error) {
                handleAddRedditsError(error);
            }
        };
    }

    private boolean checkrecreation() {
        boolean result = false;
        if(activityRecreated) {
            init();
            if(reddits != null && reddits.size() > 0) {
                Log.d(TAG, "get reddits from cache. size:" + reddits.size());
                if(view != null) {
                    Log.d(TAG, "display cached items");
                    view.hideRefreshingProgress();
                    view.updateReddits(reddits);
                }
            }
            result = true;
        }
        return result;
    }

    @Override
    public void onPostCreate() {
        //load data from cache
        if(!checkrecreation()) {
            Log.d(TAG, "check recreation");
            String savedName = preferencesWrapper.getRedditName();
            int savedRedditCount = preferencesWrapper.getRedditCount();
            Log.d(TAG, "check recreation: ");
            if(!TextUtils.isEmpty(savedName)) {
                redditName = savedName;
            }
            if(savedRedditCount <= 0) {
                savedRedditCount = pageLimit;
            }
            requestUpdateReddits(redditName, savedRedditCount);
        }
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {
        preferencesWrapper.setKeyRedditCount(reddits.size());
        preferencesWrapper.setRedditName(redditName);
        preferencesWrapper = null;
        updateRedditsListener = null;
        addRedditsListener = null;
        releaseExecutor();
        if(mainThreadHandler != null) {
            mainThreadHandler.removeCallbacksAndMessages(null);
        }
    }

    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    public void requestUpdateReddits() {
        requestUpdateReddits(redditName, pageLimit);
    }

    private void requestUpdateReddits(final String rName, final int pLimit) {
        Log.d(TAG, "requestUpdateReddits");
        updateRedditsTask = new Task() {
            @Override
            public RedditResponse performAsync() throws Exception {
                return networkDS.getRedditResponsePage(rName, null, pLimit);
            }
        }.executor(executorService);

        if(view != null) {
            view.updateViewTitle(redditName);
            view.showRefreshingProgress();
        }

        updateRedditsTask.execute(updateRedditsListener.handler(mainThreadHandler));
    }

    private void handleUpdateRedditsSuccess(RedditResponse response) {
        Log.d(TAG, "updating new reddits:" + response.data.children.size());
        redditAfterName = response.data.after;
        reddits.clear();
        reddits.addAll(response.data.children);
        if(view != null) {
            Log.d(TAG, "view exists. updating..." + response.data.children.size());
            view.hideRefreshingProgress();
            view.updateReddits(reddits);
        }
    }

    private void handleUpdateRedditsError(Throwable error) {
        if(view != null) view.hideRefreshingProgress();
        if(view != null) view.showToastMessage(error.getMessage());
    }

    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------
    public void requestAddRedditsPage() {
        Log.d(TAG, "requestAddReddits");
        addRedditsPageTask = new Task() {
            @Override
            public RedditResponse performAsync() throws Exception {
                return networkDS.getRedditResponsePage(redditName, redditAfterName, pageLimit);
            }
        }.executor(executorService);
        if(view != null) view.showLoadMoreProgress();
        addRedditsPageTask.execute((addRedditsListener).handler(mainThreadHandler));
    }

    private void handleAddRedditsSuccess(RedditResponse response) {
        Log.d(TAG, "adding new reddits:" + response.data.children.size());
        redditAfterName = response.data.after;
        reddits.addAll(response.data.children);
        Log.d(TAG, "after adding new reddits total::" + reddits.size());
        if(view != null) view.hideLoadMoreProgress();
        if(view != null) view.addReddits(response.data.children);
    }

    private void handleAddRedditsError(Throwable error) {
        if(view != null) view.hideLoadMoreProgress();
        if(view != null) view.showToastMessage(error.getMessage());
    }

    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------
    public void onRedditClick(RedditChild reddit, Activity activity) {
        if(reddit != null && reddit.data != null && reddit.data.preview != null
                && reddit.data.preview.imageDataItems != null
                && reddit.data.preview.imageDataItems != null && reddit.data.preview.imageDataItems.size() > 0) {

            String url = reddit.data.preview.imageDataItems.get(0).source.url;
            Log.d(TAG, "open image url:" + url);
            Intent i = new Intent(activity, ImageViwerActivity.class);
            i.putExtra(ImageViwerActivity.URL, url);
            i.putExtra(ImageViwerActivity.TITLE, reddit.data.title);
            activity.startActivity(i);
        }
    }

    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------
    public void onChangeRedditName(String name) {
        if(!TextUtils.isEmpty(name) && !name.equals(redditName)) {
            redditName = name;
            requestUpdateReddits();
        }
    }

    public String getRedditName() {
        return redditName;
    }

//    public List<RedditChild> getReddits() {
//        return reddits;
//    }

    private void releaseExecutor() {
        if(executorService == null) return;
        executorService.shutdown();
        executorService.shutdownNow();
        executorService = null;
    }



}
