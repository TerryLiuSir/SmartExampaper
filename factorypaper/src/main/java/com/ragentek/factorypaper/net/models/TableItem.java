package com.ragentek.factorypaper.net.models;

import com.google.gson.annotations.SerializedName;

public class TableItem {
    @SerializedName("cor_index")
    private int columnIndex;
    @SerializedName("row_index")
    private int rowIndex;
    private String ocr;
    private String url;

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public String getOcr() {
        return ocr;
    }

    public void setOcr(String ocr) {
        this.ocr = ocr;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(",url:" + url);
        sb.append(",ocr:" + ocr);
        sb.append(",columnIndex:" + columnIndex);
        sb.append(",rowIndex:" + rowIndex);

        return sb.toString();
    }
}
