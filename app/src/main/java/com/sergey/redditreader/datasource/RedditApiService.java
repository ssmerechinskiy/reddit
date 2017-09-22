package com.sergey.redditreader.datasource;

import com.sergey.redditreader.model.RedditResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by sober on 22.09.2017.
 */

public interface RedditApiService {
    @GET("r/{redditName}.json")
    Call<RedditResponse> getRedditResponse(@Path("redditName") String redditName, @Query("after") String nameAfter, @Query("limit") int limit);
}
