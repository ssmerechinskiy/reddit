package com.sergey.redditreader.presenter;

import com.sergey.redditreader.ui.BaseView;
import com.sergey.redditreader.ui.RedditsView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by user on 21.09.2017.
 */

public enum PresenterManager {
    INSTANCE;

    private Map<Long, BasePresenter> activitiesPresenters = new HashMap<>();
    private Set<Long> savedActivities = new HashSet<>();

    public <P extends BasePresenter, V extends BaseView> P initPresenter(V view) {
        P presenter = (P) activitiesPresenters.get(view.getId());
        if(presenter == null) {
            presenter = (P) view.createPresenter();
            presenter.setView(view);
            activitiesPresenters.put(view.getId(), presenter);
        } else {
            savedActivities.remove(view.getId());
        }
        return presenter;
    }

    public void releaseRedditsPresenter(long viewId) {
        if(savedActivities.contains(viewId)) {
            savedActivities.remove(viewId);
            return;
        }
        RedditsPresenter presenter = (RedditsPresenter) activitiesPresenters.get(viewId);
        if(presenter != null) {
            activitiesPresenters.remove(viewId);
        }
    }

    public void markViewToSaveInstance(long viewId) {
        savedActivities.add(viewId);
    }
}
