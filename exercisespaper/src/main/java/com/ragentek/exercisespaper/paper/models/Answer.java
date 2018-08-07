package com.ragentek.exercisespaper.paper.models;

/**
 * Created by xuanyang.feng on 2018/6/29.
 */

public class Answer {


    private int book;
    private String chapter;
    private int page;
    private int startRow;
    private int endRow;
    private String index1;
    private int index2;
    private String imagePath;
    private int objectType;

    public int getObjectType() {
        return objectType;
    }

    public void setObjectType(int objectType) {
        this.objectType = objectType;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public int getEndRow() {
        return endRow;
    }

    public void setEndRow(int endRow) {
        this.endRow = endRow;
    }


    public String getChapter() {
        return chapter;
    }

    public void setChapter(String chapter) {
        this.chapter = chapter;
    }


    public int getBook() {
        return book;
    }

    public void setBook(int book) {
        this.book = book;
    }

    public String getIndex1() {
        return index1;
    }

    public void setIndex1(String index1) {
        this.index1 = index1;
    }

    public int getIndex2() {
        return index2;
    }

    public void setIndex2(int index2) {
        this.index2 = index2;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(",book:" + book);
        sb.append(",chapter:" + chapter);
        sb.append(",page:" + page);
        sb.append(",startRow:" + startRow);
        sb.append(",endRow:" + endRow);
        sb.append(",index1:" + index1);
        sb.append(",index2:" + index2);
        sb.append(",imagePath:" + imagePath);
        return sb.toString();
    }
}
