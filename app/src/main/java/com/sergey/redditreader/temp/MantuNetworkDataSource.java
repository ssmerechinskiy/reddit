package com.sergey.redditreader.temp;

import com.google.gson.Gson;
import com.mantu.im.BzApp;
import com.mantu.im.BzGrpHeaders;
import com.mantu.im.Log;
import com.mantu.im.asmackmanager.XmppController;
import com.mantu.im.redesign.contactsearch.model.ReinstallationResponse;
import com.mantu.im.redesign.contactsearch.model.SearchContactRequest;
import com.mantu.im.redesign.contactsearch.model.SearchContactResponse;
import com.mantu.im.redesign.data.format.JsonMessage;
import com.mantu.im.redesign.data.format.Payload;
import com.mantu.im.redesign.data.format.ServerMessage;
import com.mantu.im.redesign.data.format.Target;
import com.mantu.im.redesign.data.model.Chat;
import com.mantu.im.redesign.data.model.Message;
import com.mantu.im.redesign.data.util.ContactUtils;
import com.mantu.im.redesign.data.util.GroupUtil;
import com.mantu.im.redesign.mvp.network.model.BaseResponse;
import com.mantu.im.redesign.onboardregistration.model.ErrorModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.net.ssl.HostnameVerifier;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class MantuNetworkDataSource extends BaseRetrofitNetworkDataSource implements IMantuNetworkDataSource{

    private static MantuNetworkDataSource sInstance = null;

    private MantuApiService mMantuApiService;
    private XmppController xmppController;


    public static MantuNetworkDataSource getInstance() {
        if(sInstance == null) {
            try {
                sInstance = new MantuNetworkDataSource();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sInstance;
    }

    public void purge(){
        if(mOkHttpClient != null){
            mOkHttpClient.dispatcher().cancelAll();
        }
        sInstance = null;
    }

    private MantuNetworkDataSource() throws Exception {
        super(BzApp.getAccount().getHostName() + ':' + BzApp.getAccount().getHttpPort());
        xmppController = XmppController.getInstance();
    }

    @Override
    public List<String> getRegisteredMantuContacts(String deviceContacts) throws Exception {
        Call<ResponseBody> call = mMantuApiService.getRegisteredMantuContacts(deviceContacts);
        Response<ResponseBody> response = call.execute();
        if(!response.isSuccessful()) throw new Exception(response.errorBody().string());
        String[] responseString = response.body().string().split(",");
        List<String> users = new ArrayList<>(Arrays.asList(responseString));
        return users;
    }

    @Override
    public void getGroupsReinstallation() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Map_jid", ContactUtils.getMeJabberId());
        headers.put("X-API-KEY", BzApp.getEncryptionUtil().getXmppAppKeyHeader());

        Call<ReinstallationResponse> call = mMantuApiService.getGroupsReinstallation(headers);
        Response<ReinstallationResponse> response = call.execute();
        if(!response.isSuccessful()) throw new Exception(response.message());
        ReinstallationResponse groupsReInstallationResponse = response.body();
        ErrorModel errorModel = groupsReInstallationResponse.getError();
        if(isError(errorModel)) {
            processIfCriticalError(errorModel.code);
            throw new Exception(errorModel.desc);
        } else {
            GroupUtil.handleReinstallation(groupsReInstallationResponse.getPayload().getGroups());
        }
    }

    @Override
    public SearchContactResponse searchContacts(SearchContactRequest request) throws Exception {
        Call<SearchContactResponse> call = mMantuApiService.searchContacts(request);
        Response<SearchContactResponse> response = call.execute();
        if(!response.isSuccessful()) throw new Exception(response.errorBody().string());
        SearchContactResponse searchContactResponse = response.body();
        SearchContactResponse.Error error = searchContactResponse.error;
        //processIfCriticalError()
        if(searchContactResponse.error != null) {
            if(!processIfCriticalError(searchContactResponse.error.code)) {
                //-41010 - not found error code. Means that model list is empty
                if(searchContactResponse.error.code != -41010 && searchContactResponse.error.code != 0) {
                    throw new Exception("code:" + error.code + " desc:" + error.description);
                }
            }
        }
        return searchContactResponse;
    }

    @Override
    public void sendSilentPush(String jabberId, boolean file, boolean privacyMode) throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(BzGrpHeaders.SENDER, ContactUtils.getMeJabberId());
        headers.put("RECEIVER", jabberId);
        headers.put("PRIVATE", "" + (privacyMode ? 1 : 0));

        Call<BaseResponse> call = null;
        if (file) {
            call = mMantuApiService.sendSilentPushFile(headers);
        } else {
            call = mMantuApiService.sendSilentPushMessage(headers);
        }

        Response<BaseResponse> response = call.execute();
        if(!response.isSuccessful()) throw new Exception(response.message());
        BaseResponse leaveGroupResponse = response.body();
        ErrorModel errorModel = leaveGroupResponse.errors;
        if(isError(errorModel)) {
            processIfCriticalError(errorModel.code);
            throw new Exception(errorModel.desc);
        }
    }

    @Override
    public void sendVoicePush(String jabberId, boolean privacyMode) throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(BzGrpHeaders.SENDER, ContactUtils.getMeJabberId());
        headers.put("RECEIVER", jabberId);
        headers.put("PRIVATE", "" + (privacyMode ? 1 : 0));

        Call<BaseResponse> call = mMantuApiService.sendPushVoice(headers);
        Response<BaseResponse> response = call.execute();
        if(!response.isSuccessful()) throw new Exception(response.message());
        BaseResponse leaveGroupResponse = response.body();
        ErrorModel errorModel = leaveGroupResponse.errors;
        if(isError(errorModel)) {
            processIfCriticalError(errorModel.code);
            throw new Exception(errorModel.desc);
        }
    }

    @Override
    protected void createAPIs() {
        mMantuApiService = mRetrofit.create(MantuApiService.class);
    }

    @Override
    protected Interceptor createRequestInterceptor() {
        return new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .header("Host", getFormattedBaseUrl())
                        .header("Content-Type", "application/json")
                        .header("Accept-Charset", "utf-8")
                        .header("Authorization", "Token " + BzApp.getAccount().getAuthToken())
                        //.header("Accept-Encoding", "gzip")
                        .method(original.method(), original.body())
                        .build();
                Log.d("request headers:" + request.headers().toString());
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

    @Override
    public void sendGroupEvent(Message message, Chat chat) throws Exception {
        List<MultipartBody.Part> parts = new ArrayList<>();

        ServerMessage serverMessage = new ServerMessage();

        Target target = new Target();
        target.setTo(chat.getId());
        target.setChatType(Target.CHAT_TYPE_GROUP);
        serverMessage.setTarget(target);

        Payload payload = new Payload();
        payload.setMessage(new JsonMessage(message, chat));
        serverMessage.setPayload(payload);

        Cipher encryptor = BzApp.getEncryptionUtil().getXmppAesEncryptor();
        byte[] serverMessageBytes = new Gson().toJson(serverMessage).getBytes();
        byte[] encryptedServerMessageBytes = encryptor.doFinal(serverMessageBytes);
        parts.add(MultipartBody.Part.createFormData("message", null, RequestBody.create(MediaType.parse("application/octet-stream"),
                encryptedServerMessageBytes)));

        if(message.getEvent() == Message.Event.NEW_GROUP || message.getEvent() == Message.Event.AVATAR_CHANGED){
            if(chat.getGroupData().getAvatarBytes() != null){
                Headers headers = Headers.of("Content-Disposition", "form-data; name=\"part\"; filename=\""
                        + chat.getGroupData().getAvatarId() + "\"");
                byte[] encryptedAvatar = encryptor.doFinal(chat.getGroupData().getAvatarBytes());
                if(encryptedAvatar != null){
                    RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), encryptedAvatar);
                    parts.add(MultipartBody.Part.create(headers, requestBody));
                }
            }
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("X-API-KEY", BzApp.getEncryptionUtil().getXmppAppKeyHeader());

        Call<BaseResponse> call = mMantuApiService.sendEvent(headers, parts);
        Response<BaseResponse> response = call.execute();
        if(!response.isSuccessful()) throw new Exception(response.message());
        BaseResponse responseBody = response.body();
        ErrorModel errorModel = responseBody.errors;
        if(isError(errorModel)) {
            processIfCriticalError(errorModel.code);
            throw new Exception(errorModel.desc);
        }
    }

    @Override
    public void sendMessage(Message message, Chat chat) throws Exception {
        List<MultipartBody.Part> parts = new ArrayList<>();

        ServerMessage serverMessage = new ServerMessage();
        Target target = new Target();
        if(chat.isGroup()){
            target.setTo(chat.getId());
            target.setChatType(Target.CHAT_TYPE_GROUP);
        } else {
            target.setTo(chat.getContact().getJabberId());
            target.setChatType(Target.CHAT_TYPE_P2P);
        }
        serverMessage.setTarget(target);
        Payload payload = new Payload();
        payload.setMessage(new JsonMessage(message, chat));
        serverMessage.setPayload(payload);

        Cipher encryptor = BzApp.getEncryptionUtil().getXmppAesEncryptor();
        byte[] serverMessageBytes = new Gson().toJson(serverMessage).getBytes();
        byte[] encryptedServerMessageBytes = encryptor.doFinal(serverMessageBytes);
        parts.add(MultipartBody.Part.createFormData("message", null, RequestBody.create(MediaType.parse("application/octet-stream"),
                encryptedServerMessageBytes)));

        // thumbnail part
        switch (message.getType()){
            case IMAGE:
            case VIDEO:
                Headers headers = Headers.of("Content-Disposition", "form-data; name=\"part\"; filename=\""
                        + message.getThumbnailId() + "\"");
                byte[] encryptedThumbnailBytes = encryptor.doFinal(message.getThumbnailBytes());
                RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), encryptedThumbnailBytes);
                parts.add(MultipartBody.Part.create(headers, requestBody));
                break;
        }

        // media part
        switch (message.getType()){
            case IMAGE:
            case VIDEO:
            case AUDIO:
            case FILE:
                Headers headers = Headers.of("Content-Disposition", "form-data; name=\"part\"; filename=\""
                        + message.getMediaId() + "\"");
                parts.add(MultipartBody.Part.create(headers, buildMediaRequestBody(message, encryptor)));
                break;
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("X-API-KEY", BzApp.getEncryptionUtil().getXmppAppKeyHeader());

        Call<BaseResponse> call = mMantuApiService.sendMessage(headers, parts);
        Response<BaseResponse> response = call.execute();
        if(!response.isSuccessful()) throw new Exception(response.message());
        BaseResponse responseBody = response.body();
        ErrorModel errorModel = responseBody.errors;
        if(isError(errorModel)) {
            processIfCriticalError(errorModel.code);
            throw new Exception(errorModel.desc);
        }
    }

}
