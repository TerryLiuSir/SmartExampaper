package com.ragentek.factorypaper.paper.models;

/**
 * Created by xuanyang.feng on 2018/5/27.
 */

public class SimpleDot {
    public static final float DOWN = -1.0f;
    public float x;
    public float y;
    public int pageId;
    public int bookid;

    public SimpleDot(float x, float y, int pageId, int bookid) {
        this.x = x;
        this.y = y;
        this.pageId = pageId;
        this.bookid = bookid;

    }

    @Override
    public String toString() {
        return "pageId:" + pageId + ",x:" + x + ",y:" + y + ",pageId:" + pageId + ",bookid:" + bookid;
    }
}
