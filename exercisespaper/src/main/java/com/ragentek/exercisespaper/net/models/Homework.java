package com.ragentek.exercisespaper.net.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuanyang.feng on 2018/6/29.
 */

public class Homework {

    @SerializedName("homework")
    private List<HomeworkItem> homework;
    @SerializedName("account_id")
    private int accountId;

    @SerializedName("book_id")
    private int bookId;

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public List<HomeworkItem> getHomework() {
        return homework;
    }

    public void setHomework(List<HomeworkItem> homework) {
        this.homework = homework;
    }


}
