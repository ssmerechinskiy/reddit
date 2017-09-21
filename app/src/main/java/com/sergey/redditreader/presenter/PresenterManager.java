package com.sergey.redditreader.presenter;

import com.sergey.redditreader.ui.RedditsView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 21.09.2017.
 */

public class PresenterManager {

    private Map<Long, BasePresenter> activitiesPresenters = new HashMap<>();

    public PresenterManager() {
    }

    public RedditsPresenter initPresenter(RedditsView redditsView) {
        BasePresenter presenter = new RedditsPresenter(redditsView);
        activitiesPresenters.put(redditsView.getId(), presenter);
        return presenter;
    }



    public static class EntriesPair {
        public long presenterId;
        private long viewId;
    }
}
