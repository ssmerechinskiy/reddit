package com.sergey.redditreader.temp;

import com.mantu.im.redesign.contactsearch.model.ReinstallationResponse;
import com.mantu.im.redesign.contactsearch.model.SearchContactRequest;
import com.mantu.im.redesign.contactsearch.model.SearchContactResponse;
import com.mantu.im.redesign.mvp.network.model.BaseResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by Serhii.Smerechynskyi on 25.10.2016.
 */

public interface MantuApiService {

    @POST("reg_users/get_registered_user")
    Call<ResponseBody> getRegisteredMantuContacts(@Header("Users") String deviceContactsString);

    @POST("1.0/search?os=1&app_ver=1.0")
    Call<SearchContactResponse> searchContacts(@Body SearchContactRequest searchContactRequest);

    @GET("1.0/mod_group/group_reinstallation")
    Call<ReinstallationResponse> getGroupsReinstallation(@HeaderMap Map<String, String> headers);

    @POST("1.0/Push/ANDROID_file")
    Call<BaseResponse> sendSilentPushFile(@HeaderMap Map<String, String> headers);

    @POST("1.0/Push/ANDROID_msg")
    Call<BaseResponse> sendSilentPushMessage(@HeaderMap Map<String, String> headers);

    @POST("1.0/Push/voice")
    Call<BaseResponse> sendPushVoice(@HeaderMap Map<String, String> headers);

    @Multipart
    @POST("1.0/sendmessage")
    Call<BaseResponse> sendMessage(@HeaderMap Map<String, String> headers, @Part List<MultipartBody.Part> parts);

    @Multipart
    @POST("1.0/sendevent")
    Call<BaseResponse> sendEvent(@HeaderMap Map<String, String> headers, @Part List<MultipartBody.Part> parts);

}
