package com.sergey.redditreader.ui;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.sergey.redditreader.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class ImageViwerActivity extends AppCompatActivity {
    private final static String TAG = ImageViwerActivity.class.getSimpleName();

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
                progressBar.setVisibility(View.VISIBLE);
                Picasso.with(ImageViwerActivity.this).load(url).into(target);
            }
        });
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveToGallery();
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
        progressBar.setVisibility(View.VISIBLE);
        MediaStore.Images.Media.insertImage(getContentResolver(), imageBitmap, title , "reddit image");
        progressBar.setVisibility(View.GONE);
        Snackbar.make(findViewById(android.R.id.content), "Image saved successfully", Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(imageBitmap != null) {
            imageBitmap.recycle();
            imageBitmap = null;
        }
    }

}
