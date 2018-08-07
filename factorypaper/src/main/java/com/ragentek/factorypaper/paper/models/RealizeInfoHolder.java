package com.ragentek.factorypaper.paper.models;

/**
 * Created by xuanyang.feng on 2018/6/26.
 */

public class RealizeInfoHolder {
    public static final int TYPE_AREA_CHAPTER = 1;
    public static final int TYPE_AREA_INDEX = TYPE_AREA_CHAPTER + 1;
    public static final int TYPE_AREA_RESULT = TYPE_AREA_INDEX + 1;

    public static final int TYPE_ANSWER_OBJECT_CHOICE = 10;
    public static final int TYPE_ANSWER_OBJECT_FILLING = TYPE_ANSWER_OBJECT_CHOICE + 1;
    public static final int TYPE_ANSWER_OBJECT_SHORT_ANSWER = TYPE_ANSWER_OBJECT_FILLING + 1;
    private int bookNum;
    private int pageNum;
    private int rowNum;
    private int type;

    public RealizeInfoHolder(int bookNum, int pageNum, int rowNum, int type) {
        this.bookNum = bookNum;
        this.pageNum = pageNum;
        this.rowNum = rowNum;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public int getBookNum() {
        return bookNum;
    }

    public int getPageNum() {
        return pageNum;
    }

    public int getRowNum() {
        return rowNum;
    }
}
