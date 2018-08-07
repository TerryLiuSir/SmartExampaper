package com.ragentek.exercisespaper.net;

import android.util.Log;

import com.google.gson.Gson;
import com.ragentek.exercisespaper.BaseApplication;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by xuanyang.feng on 2018/6/29.
 */

public class HttpRetrofitManager {
    private static HttpRetrofitManager mAudioCenterHttpManager;
    private static boolean DEBUG = true;
    private Retrofit retrofitAudio;
    private String BASE_URL;
    private static final String TAG = "HttpRetrofitManager";
    private String currentIp;


    private HttpRetrofitManager() {
    }

    public static final HttpRetrofitManager getInstance() {
        if (mAudioCenterHttpManager == null) {
            synchronized (HttpRetrofitManager.class) {
                if (mAudioCenterHttpManager == null) {
                    mAudioCenterHttpManager = new HttpRetrofitManager();
                }
            }
        }
        return mAudioCenterHttpManager;
    }


    public HttpRetrofitManager setBaseUrl(String url) {
        BASE_URL = url;
        return mAudioCenterHttpManager;
    }

    public <T> T creatHttpApi(Class<T> httpApiClass) {
        OkHttpClient.Builder mBuilder = new OkHttpClient.Builder()
                .addInterceptor(getLoggingInterceptor());
        OkHttpClient okHttpClient = mBuilder.build();
        Gson gson = new Gson();

        retrofitAudio = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .build();
        return retrofitAudio.create(httpApiClass);
    }

    private static HttpLoggingInterceptor getLoggingInterceptor() {
        if (DEBUG) {
            return new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        }
        return new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.NONE);
    }

    private void printLog(String message) {
        Log.d(TAG, message);
    }

}
