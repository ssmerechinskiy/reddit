package com.sergey.redditreader.temp;

import android.util.Base64;

import com.google.gson.Gson;
import com.mantu.im.AppResources;
import com.mantu.im.BzApp;
import com.mantu.im.redesign.data.model.Message;
import com.mantu.im.redesign.onboardregistration.BaseModel;
import com.mantu.im.redesign.onboardregistration.CriticalErrorHandler;
import com.mantu.im.redesign.onboardregistration.model.ErrorModel;
import com.mantu.im.redesign.utils.StorageUtil;
import com.mantu.im.redesign.xmpp.XmppConnectionManager;
import com.mantu.im.utils.NetworkUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Serhii.Smerechynskyi on 13.09.2016.
 */
public abstract class BaseRetrofitNetworkDataSource {

    protected final String baseUrl;
    protected Retrofit mRetrofit = null;
    protected OkHttpClient mOkHttpClient = null;

    protected BaseRetrofitNetworkDataSource(String baseUrl) throws Exception {
        if(!baseUrl.contains("https://")) {
            this.baseUrl = "https://" + baseUrl;
        } else {
            this.baseUrl = baseUrl;
        }
        init();
    }

    private void init() throws Exception {
        mOkHttpClient = createHttpClient();
        mRetrofit = createRetrofit();
        createAPIs();

    }

    private OkHttpClient createHttpClient() throws Exception {
        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .cipherSuites(
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA256,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA
                )
                .build();

        List<ConnectionSpec> specList = Collections.singletonList(spec);
        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
        okHttpBuilder.connectionSpecs(specList);

        SSLContext sslContext = NetworkUtil.getSSLConfig(BzApp.sContext);
        okHttpBuilder.sslSocketFactory(sslContext.getSocketFactory());

        Interceptor requestInterceptor = createRequestInterceptor();
        if (requestInterceptor != null) {
            okHttpBuilder.addInterceptor(requestInterceptor);
        }

        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor();
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        okHttpBuilder.addInterceptor(logInterceptor);

        HostnameVerifier hostnameVerifier = createHostNameVerifier();
        if(hostnameVerifier != null) {
            okHttpBuilder.hostnameVerifier(hostnameVerifier);
        } else {
            okHttpBuilder.hostnameVerifier(NetworkUtil.getHostnameVerifier());
        }
        okHttpBuilder.connectTimeout(1, TimeUnit.MINUTES);
        okHttpBuilder.writeTimeout(1, TimeUnit.MINUTES);
        okHttpBuilder.readTimeout(1, TimeUnit.MINUTES);
        OkHttpClient client = okHttpBuilder.build();
        return client;
    }

    private Retrofit createRetrofit() {
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder();
        retrofitBuilder.baseUrl(baseUrl);
        Gson gson = createGsonObject();
        if (gson != null) {
            retrofitBuilder.addConverterFactory(GsonConverterFactory.create(gson));
        } else {
            retrofitBuilder.addConverterFactory(GsonConverterFactory.create());
        }
        retrofitBuilder.client(mOkHttpClient);
        return retrofitBuilder.build();
    }

    protected SSLContext createSSLContext(int crtResId) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream caInput = AppResources.openRawResource(crtResId);
        Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);
        } finally {
            caInput.close();
        }
        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);
        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        SSLContext context = SSLContext.getInstance("TLS");
        //context.init(null, tmf.getTrustManagers(), new SecureRandom());
        context.init(null, tmf.getTrustManagers(), null);

        return context;
    }

    protected <T> void processAsyncResponse(retrofit2.Response<T> response, DashboardNetworkDataSource.ResultListener<T> listener) {
        if (response.isSuccessful()) {
            BaseModel baseModel = (BaseModel) response.body();
            if(baseModel.getError() != null) {
                listener.onError(new Exception(baseModel.getError().description), baseModel.getError().code);
            } else {
                listener.onSuccess(response.body());
            }
        } else {
            String error = "";
            if (response.errorBody() != null) {
                try {
                    error = response.errorBody().string();
                } catch (IOException e) {
                    error = "Unknown error";
                }
            }
            listener.onError(new Exception(error), response.code());
        }
    }

    protected boolean isError(ErrorModel model) {
        if(model != null && model.code != 0) {
            return true;
        }
        return false;
    }

    public static boolean processIfCriticalError(int code) {
        boolean needToRepeatResponse = false;
        switch (code) {
            case -40000 :
                //Impossible to continue. Don’t try to re-new a token. The error can be returned if a token is invalid and it is mandatory to pass a new authentication
                if(XmppConnectionManager.getInstance().getConnectionState() == XmppConnectionManager.ConnectionState.connecting) return false;
                XmppConnectionManager.getInstance().releaseConnection();
                CriticalErrorHandler.getInstance().startPhoneRegistrationFlow();
                break;
            case -40001 :
                //Token expired no Re-new a token
                CriticalErrorHandler.getInstance().startReissueTokenFlow();
                break;
            case -40002 :
                //Invalid protocol version. Impossible to continue. Update requires
                //CriticalErrorHandler.getInstance().startNewRegistrationFlow();
                break;
        }
        return needToRepeatResponse;
    }

    public static String getBasicAuthorizationString(String companyId, String appKey) {
        String authValue = companyId + ":" + appKey;
        byte[] data;
        try {
            data = authValue.getBytes("UTF-8");
            String base64 = Base64.encodeToString(data, Base64.NO_WRAP);
            authValue = "Basic" + " " + base64;
        } catch (UnsupportedEncodingException e) {
            authValue = null;
        }
        return authValue;
    }

    public static String getHostName() {
        String host = BzApp.getAccount().getHostName();
        if(host.contains("https://")) {
            String[] parts = host.split("https://");
            host = parts[1];
        }
        return host;
    }

    public String getFormattedBaseUrl() {
        String host = baseUrl;
        if(baseUrl.contains("https://")) {
            String[] parts = baseUrl.split("https://");
            host = parts[1];
        }
        return host;
    }

    public static RequestBody buildMediaRequestBody(Message message, Cipher encryptor) throws Exception {
        RequestBody requestBody = null;
        switch (message.getType()){
            case IMAGE:
                byte[] decodedImage = StorageUtil.decryptToBytes(message.getMediaPath(),
                        message.getEncryptionIVBytes());
                byte[] encryptedImage = encryptor.doFinal(decodedImage);
                if(encryptedImage != null){
                    requestBody = RequestBody.create(MediaType.parse("image/jpeg"), encryptedImage);
                }
                break;
            case VIDEO:
                byte[] decodedVideo = StorageUtil.decryptToBytes(message.getMediaPath(),
                        message.getEncryptionIVBytes());
                byte[] encryptedVideo = encryptor.doFinal(decodedVideo);
                if(encryptedVideo != null){
                    requestBody = RequestBody.create(MediaType.parse("video/mp4"), encryptedVideo);
                }
                break;
            case AUDIO:
            case FILE:
                byte[] decodedFile = StorageUtil.decryptToBytes(message.getMediaPath(), message.getEncryptionIVBytes());
                byte[] encryptedFile = encryptor.doFinal(decodedFile);
                if(encryptedFile != null){
                    requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), encryptedFile);
                }
                break;
        }
        return requestBody;
    }

    protected abstract void createAPIs();

    protected abstract Interceptor createRequestInterceptor();

    protected abstract Gson createGsonObject();

    protected abstract HostnameVerifier createHostNameVerifier();

    public interface ResultListener<T> {
        void onSuccess(T response);
        void onError(Throwable e, int code);
    }

}
