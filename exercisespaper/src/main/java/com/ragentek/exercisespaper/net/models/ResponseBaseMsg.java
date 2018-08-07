package com.ragentek.exercisespaper.net.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by xuanyang.feng on 2018/6/29.
 */

public class ResponseBaseMsg {
    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    @SerializedName("res_code")
    private int resultCode;

    @SerializedName("res_msg")
    private String resultMsg;


//    {
//        "account_id": 3,
//            "class_id": 2,
//            "date": "2018-07-04",
//            "grade_id": 1,
//            "res_code": 0,
//            "res_msg": "success",
//            "subject_id": 2
//    }


    @Override
    public String toString() {
        return "res_code:"+resultCode+",res_msg:"+resultMsg;
    }
}
