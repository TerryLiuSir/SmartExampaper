package com.ragentek.exercisespaper.net.api;

import com.ragentek.exercisespaper.net.models.Homework;
import com.ragentek.exercisespaper.net.models.ResponseBaseMsg;
import com.ragentek.exercisespaper.net.models.SubmitResponse;
import com.ragentek.exercisespaper.net.models.UplodeResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by xuanyang.feng on 2018/6/29.
 */

public interface SubmitAPI {
    @Multipart
    @POST("paper/file/upload")
    Call<UplodeResponse> uploadFile(@Part MultipartBody.Part imgs);

    //http://ip:8080/paper/homework/upload?book_id=xxxx&account_id=xxxx
    @POST("paper/homework/upload")
    Call<SubmitResponse> submitResult(@Body Homework homework);
}
