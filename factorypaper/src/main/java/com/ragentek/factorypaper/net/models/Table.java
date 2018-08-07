package com.ragentek.factorypaper.net.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Table {
    @SerializedName("book_id")
    private int bookId;
    @SerializedName("page_id")
    private int pageId;

    @SerializedName("form_data")
    private List<TableItem> items;


    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getBookId() {
        return bookId;
    }

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public List<TableItem> getItems() {
        return items;
    }

    public void setItems(List<TableItem> items) {
        this.items = items;
    }
}
