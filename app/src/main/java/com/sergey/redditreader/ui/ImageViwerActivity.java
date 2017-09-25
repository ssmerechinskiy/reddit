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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class ImageViwerActivity extends AppCompatActivity {
    private final static String TAG = ImageViwerActivity.class.getSimpleName();

    private static final int PERMISSION_REQUEST_WRITE_IMAGE = 1;

    public final static String TITLE = "title";
    public final static String URL = "image_url";
    public final static String WIDTH = "width";
    public final static String HEIGHT = "height";

    private String url;
    private String title;
    private ImageView image;
    private View progressBar;

    private Bitmap imageBitmap;
    private int width, height;
    private PermissionHelper permissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viwer);
        url = getIntent().getStringExtra(URL);
        title = getIntent().getStringExtra(TITLE);
        image = (ImageView) findViewById(R.id.image);
        permissionHelper = new PermissionHelper(this);
        progressBar = findViewById(R.id.progress);
        image.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d(TAG, "onGlobalLayout");
                image.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                width = image.getMeasuredWidth();
                height = image.getMeasuredHeight();
                Log.d(TAG, "onGlobalLayout: w:" + width + " h:" + height + " url:" + url);
                progressBar.setVisibility(View.VISIBLE);
                Picasso.with(ImageViwerActivity.this).load(url).into(target);
            }
        });
    }

    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            imageBitmap = bitmap;

            Picasso.with(ImageViwerActivity.this).load(url).resize(width, height).centerInside().into(image, new Callback() {
                @Override
                public void onSuccess() {
                    Snackbar.make(findViewById(android.R.id.content), "Tap to image for saving", Snackbar.LENGTH_LONG).show();
                    image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            saveToGallery();
                        }
                    });
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError() {
                    Snackbar.make(findViewById(android.R.id.content), "Error occurred", Snackbar.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            });
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            progressBar.setVisibility(View.VISIBLE);
        }
    };

    private void saveToGallery() {
        Log.d(TAG, "saveToGallery");
        permissionHelper.requestPermission(PERMISSION_REQUEST_WRITE_IMAGE, requestPermissionCallback, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(imageBitmap != null) {
            imageBitmap.recycle();
            imageBitmap = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

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
                        MediaStore.Images.Media.insertImage(getContentResolver(), imageBitmap, title , "reddit image");
                        Snackbar.make(findViewById(android.R.id.content), "Image saved successfully", Snackbar.LENGTH_LONG).show();
                        break;
                }
            } else {
                Toast.makeText(ImageViwerActivity.this, "Permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    };

}
