package com.ragentek.factorypaper.net.models;

import com.google.gson.annotations.SerializedName;

public class SubmitResponse extends ResponseBaseMsg {

    @SerializedName("account_id")
    private int accountId;
    private String date;
    @SerializedName("grade_id")
    private int gradeId;
    @SerializedName("subject_id")
    private int subjectId;
    @SerializedName("class_id")
    private int classId;
    @SerializedName("subject_name")
    private String subjectName;

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getGradeId() {
        return gradeId;
    }

    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

}
