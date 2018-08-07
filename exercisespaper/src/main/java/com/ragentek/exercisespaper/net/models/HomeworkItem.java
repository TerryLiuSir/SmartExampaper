package com.ragentek.exercisespaper.net.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by xuanyang.feng on 2018/6/29.
 */

public class HomeworkItem {
    @SerializedName("lesson")
    private String chapter;
    private String index1;
    private String index2;
    private String ocr;

    private String url;

    public String getChapter() {
        return chapter;
    }

    public void setChapter(String chapter) {
        this.chapter = chapter;
    }

    public String getIndex1() {
        return index1;
    }

    public void setIndex1(String index1) {
        this.index1 = index1;
    }

    public String getIndex2() {
        return index2;
    }

    public void setIndex2(String index2) {
        this.index2 = index2;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOcr() {
        return ocr;
    }

    public void setOcr(String ocr) {
        this.ocr = ocr;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("chapter:" + chapter);
        sb.append(",index1:" + index1);
        sb.append(",index2:" + index2);
        sb.append(",url:" + url);
        sb.append(",ocr:" + ocr);

        return sb.toString();
    }
}
