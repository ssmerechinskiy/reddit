package com.sergey.redditreader.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by sober on 21.09.2017.
 */

public class RedditChild {
    @SerializedName("kind")
    public String kind;

    @SerializedName("data")
    public Data data;

    public static class Data {
        @SerializedName("id")
        public String id;

        @SerializedName("subreddit_id")
        public String subredditId;

        @SerializedName("subreddit")
        public String subreddit;

        @SerializedName("title")
        public String title;

        @SerializedName("name")
        public String name;

        @SerializedName("selftext")
        public String selftext;

        @SerializedName("thumbnail")
        public String thumbnail;

        @SerializedName("thumbnail_height")
        public int thumbnailHeight;

        @SerializedName("thumbnail_width")
        public int thumbnailWidth;

        @SerializedName("preview")
        public Preview preview;

        @SerializedName("author")
        public String author;

        @SerializedName("is_video")
        public boolean isVideo;

        @SerializedName("num_comments")
        public int num_comments;

        @SerializedName("url")
        public String url;

        public static class Preview {
            @SerializedName("enabled")
            public boolean enabled;

            @SerializedName("images")
            public List<ImageDataItem> imageDataItems;

            public static class ImageDataItem {
                @SerializedName("source")
                public Source source;

                @SerializedName("resolutions")
                public List<Source> resolutionItems;

                public static class Source {
                    @SerializedName("url")
                    public String url;

                    @SerializedName("width")
                    public int width;

                    @SerializedName("height")
                    public int height;
                }
            }
        }
    }
}
