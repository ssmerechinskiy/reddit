package com.sergey.redditreader.datasource;

import com.sergey.redditreader.model.RedditResponse;

/**
 * Created by sober on 21.09.2017.
 */

public interface RedditNetworkDS {
    //if (nameFrom = null) then it will be 1st page
    RedditResponse getRedditResponsePage(String redditName, String nameFrom, int pageLimit) throws Exception;
}
