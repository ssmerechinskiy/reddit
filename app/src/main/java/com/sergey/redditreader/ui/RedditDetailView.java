package com.sergey.redditreader.ui;

import android.widget.ImageView;

/**
 * Created by user on 25.09.2017.
 */

public interface RedditDetailView extends BaseView {
    void showImageLoadingProgress();
    void hideImageLoadingProgress();
    void showSnackMessage(String message);
    void setTtile(String title);
    PermissionHelper getPermissionHelper();
    ImageView getImageView();
    void initImageClickListener();
}
