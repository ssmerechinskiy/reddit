package com.sergey.redditreader.presenter;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import com.sergey.redditreader.ui.ImageViwerActivity;
import com.sergey.redditreader.ui.RedditDetailView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by user on 25.09.2017.
 */

public class RedditDetailPresenter extends BaseActivityPresenter<RedditDetailView> {
    private final static String TAG = RedditDetailPresenter.class.getSimpleName();

    private Bitmap imageBitmap;
    private String url;
    private String name;
    private int width, height;

    public RedditDetailPresenter() {}

    public void init(String url, String title) {
        this.url = url;
        name = title;
    }

    public void onImageContainerPrepared(int w, int h) {
        width = w;
        height = h;
        Picasso.with(view.getContext()).load(url).into(loadBitmapListener);
    }

    private Target loadBitmapListener = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            imageBitmap = bitmap;
            Log.i(TAG, "onBitmapLoaded: w:" + imageBitmap.getWidth() + " h:" + imageBitmap.getHeight());
            if(width == 0 || height == 0) {
                width = imageBitmap.getWidth();
                height = imageBitmap.getHeight();
            }
            Picasso.with(view.getContext()).load(url).resize(width, height).centerInside().into(view.getImageView(), new Callback() {
                @Override
                public void onSuccess() {
                    view.showSnackMessage("Tap to image for saving");
                    view.setTtile(name);
                    view.hideImageLoadingProgress();
                    view.initImageClickListener();
                }

                @Override
                public void onError() {
                    view.showSnackMessage("Error occurred");
                    view.hideImageLoadingProgress();
                }
            });
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            view.hideImageLoadingProgress();
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            view.showImageLoadingProgress();
        }
    };

    @Override
    public void onPostCreate() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {
        if(imageBitmap != null) {
            imageBitmap.recycle();
            imageBitmap = null;
        }
    }

    public void saveImage() {
        MediaStore.Images.Media.insertImage(view.getContext().getContentResolver(), imageBitmap,  name, "reddit image");
        view.showSnackMessage("Image saved successfully");
    }

}
