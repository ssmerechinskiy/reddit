package com.sergey.redditreader.temp;

import com.mantu.im.redesign.onboardregistration.response.ValidateInvitationResponse;

/**
 * Created by Serhii.Smerechynskyi on 14.09.2016.
 */
public interface IDashBoardNetworkDataSource {
    void validateInvitation(String invitationId, final DashboardNetworkDataSource.ResultListener<ValidateInvitationResponse> listener);
    ValidateInvitationResponse validateInvitation(String invitationId) throws Exception;

    //void getMainParams(String url, final DashboardNetworkDataSource.ResultListener<MainParamsResponse> listener);
}
