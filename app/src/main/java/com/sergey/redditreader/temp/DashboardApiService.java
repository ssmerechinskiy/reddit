package com.sergey.redditreader.temp;

import com.mantu.im.redesign.onboardregistration.response.ValidateInvitationResponse;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by Serhii.Smerechynskyi on 13.09.2016.
 */
public interface DashboardApiService {
    @POST("dashboard-server/api/registration/validate_invitation")
    /*
    @Headers({"Content-Type : application/json",
            "Accept-Charset : utf-8"})*/
    Call<ValidateInvitationResponse> validateInvitation(@Header("Authorization") String invitationId);


}
