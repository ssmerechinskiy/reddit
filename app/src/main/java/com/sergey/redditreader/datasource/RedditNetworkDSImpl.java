package com.sergey.redditreader.datasource;

import com.sergey.redditreader.model.RedditResponse;

/**
 * Created by user on 21.09.2017.
 */

public enum RedditNetworkDSImpl implements RedditNetworkDS {

    INSTANCE {
        @Override
        public RedditResponse getRedditResponsePage(String redditName, String nameFrom, int pageLimit) {
            return null;
        }
    };

    RedditNetworkDSImpl() {

    }

}
