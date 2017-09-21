package com.sergey.redditreader.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by sober on 21.09.2017.
 */

public class RedditResponse {
//    public enum Type {Listing}

    @SerializedName("kind")
    public String kind;

    @SerializedName("data")
    public RedditData data;

    public static class RedditData {
        @SerializedName("modhash")
        public String modhash;

        @SerializedName("children")
        public List<RedditChild> children;

        @SerializedName("after")
        public String after;

        @SerializedName("before")
        public String before;
    }
}
