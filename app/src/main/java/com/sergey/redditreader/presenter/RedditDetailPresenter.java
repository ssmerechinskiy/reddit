package com.sergey.redditreader.presenter;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.sergey.redditreader.ui.ImageViwerActivity;
import com.sergey.redditreader.ui.PermissionHelper;
import com.sergey.redditreader.ui.RedditDetailView;
import com.sergey.redditreader.ui.RedditsView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import static com.sergey.redditreader.ui.ImageViwerActivity.PERMISSION_REQUEST_WRITE_IMAGE;

/**
 * Created by user on 25.09.2017.
 */

public class RedditDetailPresenter extends BaseActivityPresenter<RedditDetailView> {

    private ImageView imageView;
    private Bitmap imageBitmap;
    private String url;
    private String name;

    private int imageContainerW;
    private int imageContainerH;

    public RedditDetailPresenter() {
    }

    public void onImageContainerPrepared(ImageView v, int w, int h, String url, String title) {
        imageView = v;
        imageContainerW = w;
        imageContainerH = h;
        this.url = url;
        name = title;
        Picasso.with(view.getContext()).load(url).into(loadBitmapListener);
    }

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

    private Target loadBitmapListener = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            imageBitmap = bitmap;

            Picasso.with(view.getContext()).load(url).resize(imageContainerW, imageContainerH).centerInside().into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                    view.showSnackMessage("Tap to image for saving");
                    view.setTtile(name);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            view.getPermissionHelper().requestPermission(PERMISSION_REQUEST_WRITE_IMAGE, requestPermissionCallback, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        }
                    });
                    view.hideImageLoadingProgress();
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

    private PermissionHelper.RequestPermissionCallback requestPermissionCallback = new PermissionHelper.RequestPermissionCallback() {
        @Override
        public void onPermissionGranted(String permission, int requestCode) {
        }

        @Override
        public void onPermissionDenied(String permission, int requestCode) {
        }

        @Override
        public void onRequestPermissionComplete(int requestCode, int grantedCount, int requestedCount) {
            if(grantedCount == requestedCount){
                switch (requestCode){
                    case PERMISSION_REQUEST_WRITE_IMAGE:
                        MediaStore.Images.Media.insertImage(view.getContext().getContentResolver(), imageBitmap, name , "reddit image");
                        view.showSnackMessage("Image saved successfully");
                        break;
                }
            } else {
                view.showSnackMessage("Permission not granted");
            }
        }
    };
}
