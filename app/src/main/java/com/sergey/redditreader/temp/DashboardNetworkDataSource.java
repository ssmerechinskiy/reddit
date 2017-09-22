package com.sergey.redditreader.temp;

import android.net.Uri;

import com.google.gson.Gson;
import com.mantu.im.BzApp;
import com.mantu.im.Log;
import com.mantu.im.redesign.onboardregistration.response.MainParamsResponse;
import com.mantu.im.redesign.onboardregistration.response.ValidateInvitationResponse;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by Serhii.Smerechynskyi on 13.09.2016.
 */
public class DashboardNetworkDataSource extends BaseRetrofitNetworkDataSource implements IDashBoardNetworkDataSource {

    private DashboardApiService mDashboardApiService;

    public DashboardNetworkDataSource() throws Exception {
        super(BzApp.getAccount().getHostName());
    }

    @Override
    public void validateInvitation(String invitationId, final ResultListener<ValidateInvitationResponse> listener) {
        Call<ValidateInvitationResponse> call = mDashboardApiService.validateInvitation(invitationId);
        call.enqueue(new Callback<ValidateInvitationResponse>() {
            @Override
            public void onResponse(Call<ValidateInvitationResponse> call, retrofit2.Response<ValidateInvitationResponse> response) {
                processAsyncResponse(response, listener);
            }

            @Override
            public void onFailure(Call<ValidateInvitationResponse> call, Throwable t) {
                listener.onError(t, 0);
            }
        });
    }

    @Override
    public ValidateInvitationResponse validateInvitation(String invitationId) throws Exception {
        Call<ValidateInvitationResponse> call = mDashboardApiService.validateInvitation(invitationId);
        retrofit2.Response<ValidateInvitationResponse> response = call.execute();
        if(!response.isSuccessful()) throw new Exception(response.errorBody().string());
        return response.body();
    }

    public static void getMainParams(final String urlAddress, final ResultListener<MainParamsResponse> listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                WeakReference<ResultListener<MainParamsResponse>> weakListener = new WeakReference<>(listener);
                try {
                    String host = getHostName(urlAddress);
                    URL url = new URL(urlAddress);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("Host", host);
                    con.setRequestProperty("Connection", "keep-alive");
                    con.setRequestProperty("Upgrade-Insecure-Requests", "1");
                    con.setRequestProperty("User-Agent", "Google Chrome");
                    con.setRequestProperty("Accept-Language", "en-US,en;q=0.5,he;q=0.6");
                    con.setRequestProperty("Accept-Encoding", "gzip");
                    con.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                    con.setRequestProperty("Cookie", "authed=1");

                    int responseCode = con.getResponseCode();
                    Log.i("Response Code: " + responseCode);
                    if (responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
                        String locationUrl = con.getHeaderField("Location");
                        MainParamsResponse mainParamsResponse = new MainParamsResponse();
                        mainParamsResponse.location = locationUrl;
                        mainParamsResponse.ic = getUrlParam(locationUrl, "ic");
                        mainParamsResponse.icvbu = getUrlParam(locationUrl, "icvbu");
                        if(weakListener.get() != null) {
                            weakListener.get().onSuccess(mainParamsResponse);
                        }
                    } else {
                        if(weakListener.get() != null) {
                            weakListener.get().onError(new Exception(con.getResponseMessage()), responseCode);
                        }
                    }
                } catch (Exception e) {
                    if(weakListener.get() != null) {
                        weakListener.get().onError(new Exception(e.getMessage()), 0);
                    }
                }
            }
        }).start();
    }

    public static String getUrlParam(String url, String param) {
        Uri uri= Uri.parse(url);
        return uri.getQueryParameter(param);
    }

    public static String getHostName(String url) throws URISyntaxException {
        Uri uri = Uri.parse(url);
        return uri.getHost();
    }

    @Override
    protected void createAPIs() {
        mDashboardApiService = mRetrofit.create(DashboardApiService.class);
    }

    @Override
    protected Interceptor createRequestInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .header("Host", BzApp.getAccount().getHostName())
                        .header("Content-Type", "application/json")
                        .header("Accept-Charset", "utf-8")
                        .method(original.method(), original.body())
                        .build();
                Log.i("request headers:" + request.headers().toString());
                return chain.proceed(request);
            }
        };
    }

    @Override
    protected Gson createGsonObject() {
        return null;
    }

    @Override
    protected HostnameVerifier createHostNameVerifier() {
        return null;
    }


}
