package com.sergey.redditreader.ui;

import com.sergey.redditreader.model.RedditChild;

import java.util.List;

/**
 * Created by user on 21.09.2017.
 */

public interface RedditsView extends BaseView {
    void updateReddits(List<RedditChild> reddits);
    void addReddits(List<RedditChild> reddits);
}
