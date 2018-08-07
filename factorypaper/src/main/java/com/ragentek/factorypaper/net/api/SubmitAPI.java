package com.ragentek.factorypaper.net.api;


import com.ragentek.factorypaper.net.models.ResponseBaseMsg;
import com.ragentek.factorypaper.net.models.SubmitResponse;
import com.ragentek.factorypaper.net.models.Table;
import com.ragentek.factorypaper.net.models.UplodeResponse;

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

    @POST("upload/file/form")
    Call<UplodeResponse> uploadFile(@Part MultipartBody.Part imgs);

    //http://ip:8080/paper/homework/upload?book_id=xxxx&account_id=xxxx
    @POST("form/data")
    Call<ResponseBaseMsg> submitResult(@Body Table table);
}
