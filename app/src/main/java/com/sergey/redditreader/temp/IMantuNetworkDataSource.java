package com.sergey.redditreader.temp;

import com.mantu.im.redesign.contactsearch.model.SearchContactRequest;
import com.mantu.im.redesign.contactsearch.model.SearchContactResponse;
import com.mantu.im.redesign.data.model.Chat;
import com.mantu.im.redesign.data.model.Message;

import java.util.List;

/**
 * Created by Serhii.Smerechynskyi on 25.10.2016.
 */

public interface IMantuNetworkDataSource {
    List<String> getRegisteredMantuContacts(String deviceContacts) throws Exception;
    void sendGroupEvent(Message message, Chat chat) throws Exception;
    void sendMessage(Message message, Chat chat) throws Exception;
    void getGroupsReinstallation() throws Exception;
    SearchContactResponse searchContacts(SearchContactRequest request) throws Exception;
    void sendSilentPush(String jabberId, boolean file, boolean privacyMode) throws Exception;
    void sendVoicePush(String jabberId, boolean privacyMode) throws Exception;
    //void registerFCMPushToken(RegisterPushTokenRequest request, final BaseRetrofitNetworkDataSource.ResultListener<RegisterPushTokenResponse> listener);
}
