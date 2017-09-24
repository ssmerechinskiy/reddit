package com.sergey.redditreader.ui;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sergey.redditreader.R;
import com.sergey.redditreader.Util;
import com.sergey.redditreader.model.RedditChild;
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

    private Callback listener;
    private RecyclerView recyclerView;

    private boolean isLoading;
    private int lastVisibleItem, totalItemCount;
    private int visibleThreshold = 1;


    public RedditsAdapter(Context c, List<RedditChild> items, Callback listener, RecyclerView view) {
        context = c;
        reddits.addAll(items);
        this.listener = listener;
        recyclerView = view;
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (RedditsAdapter.this.listener != null) {
                        Log.d(TAG, "onScrolled: loadmore. last visible:" + lastVisibleItem + " total:" + totalItemCount);
                        RedditsAdapter.this.listener.onLoadMore();
                    }
                    isLoading = true;
                }
            }
        });
    }

    public void addItems(List<RedditChild> items) {
        int insertedPositionStart = reddits.size();
        reddits.addAll(items);
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
        holder.date.setText(Util.getTimeAgoString(item.data.date * 1000));
        holder.author.setText(Util.getAuthorString(item.data.author));

        holder.comments.setText(Util.getCommentsString(item.data.num_comments));

        setImage(holder.image, item.data.thumbnail, item.data.thumbnailWidth, item.data.thumbnailHeight);
    }

    private void setImage(ImageView imageView, final String url, int w, int h) {
//        Log.i(TAG, "setImage url:" + url);
        if(!TextUtils.isEmpty(url) && !url.equals("self") && !url.equals("default")) {
            Picasso.with(context).load(url)
                    .fit().centerCrop().into(imageView, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    Log.i(TAG, "onSuccess url:" + url);
                }

                @Override
                public void onError() {
                    Log.i(TAG, "onError url" + url);
                }
            });
        } else {
//            Log.i(TAG, "setImage default");
            Picasso.with(context).load(R.drawable.call_message).into(imageView);
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
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                if(listener != null) listener.onItemClick(reddits.get(position));
            }
        }
    }

    public void setLoaded() {
        isLoading = false;
    }

    public interface Callback {
        void onLoadMore();
        void onItemClick(RedditChild redditChild);
    }
}
