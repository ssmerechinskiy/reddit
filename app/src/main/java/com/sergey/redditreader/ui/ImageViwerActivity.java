package com.sergey.redditreader.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import com.sergey.redditreader.R;
import com.sergey.redditreader.presenter.BaseActivityPresenter;
import com.sergey.redditreader.presenter.RedditDetailPresenter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class ImageViwerActivity extends BaseActivity<RedditDetailPresenter, RedditsView> implements RedditDetailView{
    private final static String TAG = ImageViwerActivity.class.getSimpleName();

    public static final int PERMISSION_REQUEST_WRITE_IMAGE = 1;

    public final static String TITLE = "title";
    public final static String URL = "image_url";
    public final static String WIDTH = "width";
    public final static String HEIGHT = "height";

    private String url;
    private String title;
    private ImageView image;
    private View progressBar;

    private int width, height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viwer);
        url = getIntent().getStringExtra(URL);
        title = getIntent().getStringExtra(TITLE);
        image = (ImageView) findViewById(R.id.image);
        progressBar = findViewById(R.id.progress);

        image.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d(TAG, "onGlobalLayout");
                image.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                width = image.getMeasuredWidth();
                height = image.getMeasuredHeight();
                Log.d(TAG, "onGlobalLayout: w:" + width + " h:" + height + " url:" + url);
                presenter.onImageContainerPrepared(image, width, height, url, title);
            }
        });
    }

    @Override
    public BaseActivityPresenter createPresenter() {
        return new RedditDetailPresenter();
    }


    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void showImageLoadingProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideImageLoadingProgress() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showSnackMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }


    @Override
    public void setTtile(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public PermissionHelper getPermissionHelper() {
        return permissionHelper;
    }
}
