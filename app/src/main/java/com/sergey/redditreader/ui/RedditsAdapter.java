package com.sergey.redditreader.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.TextView;

import com.sergey.redditreader.R;
import com.sergey.redditreader.Util;
import com.sergey.redditreader.model.RedditChild;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 23.09.2017.
 */

public class RedditsAdapter extends RecyclerView.Adapter<RedditsAdapter.ViewHolder> {
    private final static String TAG = RedditsAdapter.class.getSimpleName();

    private final static int VIEW_TYPE_ITEM = 0;
    private final static int VIEW_TYPE_LOADING = 1;

    private Context context;
    private List<RedditChild> reddits = new ArrayList<>();

    private OnLoadMoreListener loadMoreListener;
    private RecyclerView recyclerView;

    private boolean isLoading;
    private int lastVisibleItem, totalItemCount;
    private int visibleThreshold = 5;


    public RedditsAdapter(Context c, List<RedditChild> items, OnLoadMoreListener listener, RecyclerView view) {
        context = c;
        reddits.addAll(items);
        loadMoreListener = listener;
        recyclerView = view;
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
//                Log.d(TAG, "onScrolled");
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (loadMoreListener != null) {
                        Log.d(TAG, "onScrolled: loadmore");
                        loadMoreListener.onLoadMore();
                    }
                    isLoading = true;
                }
            }
        });
    }

    public void addItems(List<RedditChild> items) {
//        Log.i(TAG, "addItems:" + items.size() + " before:" + reddits.size());
        int insertedPositionStart = reddits.size();
        reddits.addAll(items);
//        Log.i(TAG, "addItems:from=" + insertedPositionStart + " after:" + reddits.size());
        notifyItemRangeInserted(insertedPositionStart, items.size());
    }

    public void updateItems(List<RedditChild> items) {
        reddits.clear();
        notifyDataSetChanged();
        reddits.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.reddit_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RedditChild item = reddits.get(position);

        holder.title.setText(item.data.title);
        holder.date.setText(Util.getTimeAgoString(item.data.date));
        holder.author.setText(Util.getAuthorString(item.data.author));

        holder.comments.setText(Util.getCommentsString(item.data.num_comments));

        setImage(holder.image, item.data.thumbnail, item.data.thumbnailWidth, item.data.thumbnailHeight);

//        if(position == getItemCount() - 1) {
//            if(loadMoreListener != null) loadMoreListener.onLoadMore();
//        }

    }

    private void setImage(ImageView imageView, String url, int w, int h) {
        Log.i(TAG, "setImage url:" + url);
//        imageView.setImageResource(R.drawable.default_reddit);
        if(!TextUtils.isEmpty(url) && !url.equals("self") && !url.equals("default")) {
//            imageView.setMinimumWidth(w);
//            imageView.setMinimumHeight(h);
            Log.i(TAG, "setImage picasso:" + url);
            Picasso.with(context).load(url)
                    .fit().centerCrop().into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                    Log.i(TAG, "onSuccess url");
                }

                @Override
                public void onError() {
                    Log.i(TAG, "onError url");
                }
            });
        } else {
            Log.i(TAG, "setImage default");
            Picasso.with(context).load(R.drawable.default_reddit).into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                    Log.i(TAG, "onSuccess default");
                }

                @Override
                public void onError() {
                    Log.i(TAG, "onError default");
                }
            });
//            imageView.setImageResource(R.drawable.default_reddit);
//            imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.default_reddit));
//            Drawable drawable = context.getResources().getDrawable(R.drawable.default_reddit);
//            imageView.setBackgroundDrawable(drawable);
        }
    }

    @Override
    public int getItemCount() {
        return reddits.size();
    }

    @Override public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView title;
        public TextView date;
        public TextView author;
        public TextView comments;
        public ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            date = (TextView) itemView.findViewById(R.id.date);
            author = (TextView) itemView.findViewById(R.id.author);
            comments = (TextView) itemView.findViewById(R.id.comments);
            image = (ImageView) itemView.findViewById(R.id.image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

        }
    }

    public void setLoaded() {
        isLoading = false;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }
}
