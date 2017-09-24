package com.sergey.redditreader.presenter;

import com.sergey.redditreader.ui.BaseView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by user on 21.09.2017.
 */

public enum PresenterManager {
    INSTANCE;

    private Map<Long, BaseActivityPresenter> activitiesPresenters = new HashMap<>();
    private Set<Long> savedActivities = new HashSet<>();

    public <P extends BaseActivityPresenter, V extends BaseView> P initPresenter(V view) {
        P presenter = (P) activitiesPresenters.get(view.getId());
        if(presenter == null) {
            presenter = (P) view.createPresenter();
            presenter.setView(view);
            activitiesPresenters.put(view.getId(), presenter);
        } else {
            presenter.setView(view);
            presenter.setActivityRecreated();
            savedActivities.remove(view.getId());
        }
        return presenter;
    }

    public void releaseRedditsPresenterForView(long viewId) {
        if(savedActivities.contains(viewId)) {
            savedActivities.remove(viewId);
            return;
        }
        RedditsActivityPresenter presenter = (RedditsActivityPresenter) activitiesPresenters.get(viewId);
        if(presenter != null) {
            activitiesPresenters.remove(viewId);
        }
    }

    public void markViewToSaveInstance(long viewId) {
        savedActivities.add(viewId);
    }
}
