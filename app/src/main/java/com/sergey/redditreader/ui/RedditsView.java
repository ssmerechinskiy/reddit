package com.sergey.redditreader.ui;

import com.sergey.redditreader.model.RedditChild;
import com.sergey.redditreader.task.Task;

import java.util.List;

/**
 * Created by user on 21.09.2017.
 */

public interface RedditsView extends BaseView {
    void updateReddits(List<RedditChild> reddits);
    void addReddits(List<RedditChild> reddits);
    void showToastMessage(String message);
    void showRefreshingProgress();
    void hideRefreshingProgress();
    void showLoadMoreProgress();
    void hideLoadMoreProgress();
}
