package com.sergey.redditreader.datasource;

import com.sergey.redditreader.model.RedditResponse;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by user on 21.09.2017.
 */

public enum RedditNetworkDSImpl implements RedditNetworkDS {
    INSTANCE() {
        @Override
        public RedditResponse getRedditResponsePage(String redditName, String nameFrom, int pageLimit) throws Exception {
            Call<RedditResponse> call = mRedditApiService.getRedditResponse(redditName, nameFrom, pageLimit);
            Response<RedditResponse> response = call.execute();
            if(!response.isSuccessful()) throw new Exception(response.errorBody().string());
            RedditResponse r = response.body();
            return r;
        }
    };

    public final static String BASE_URL = "https://www.reddit.com";
    private static Retrofit mRetrofit;
    private static OkHttpClient mOkHttpClient;
    private static RedditApiService mRedditApiService;

    RedditNetworkDSImpl() {
        init();
    }

    private void init() {
        mOkHttpClient = createHttpClient();
        mRetrofit = createRetrofit();
        mRedditApiService = mRetrofit.create(RedditApiService.class);
    }

    private OkHttpClient createHttpClient() {
        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();

        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor();
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        okHttpBuilder.addInterceptor(logInterceptor);

        okHttpBuilder.connectTimeout(1, TimeUnit.MINUTES);
        okHttpBuilder.writeTimeout(1, TimeUnit.MINUTES);
        okHttpBuilder.readTimeout(1, TimeUnit.MINUTES);
        OkHttpClient client = okHttpBuilder.build();

        return client;
    }

    private Retrofit createRetrofit() {
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder();
        retrofitBuilder.baseUrl(BASE_URL);
        retrofitBuilder.addConverterFactory(GsonConverterFactory.create());
        retrofitBuilder.client(mOkHttpClient);
        return retrofitBuilder.build();
    }
}
