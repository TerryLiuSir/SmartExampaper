package com.ragentek.factorypaper.paper.models;

import java.util.ArrayList;

/**
 * Created by xuanyang.feng on 2018/6/29.
 */

public class GridItem {


    public static final int TYPE_ENG = 1;
    public static final int TYPE_NUMBER = TYPE_ENG + 1;
    public static final int TYPE_CHINNESE = TYPE_NUMBER + 1;
    public static final int TYPE_DATE = TYPE_CHINNESE + 1;

    private int book;
    private int page;
    private int rowLocation;
    private int columnLocation;
    private int widthCount;
    private int heightCount;

    private String dotsPath;
    private String imagePath;
    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    private ArrayList<SimpleDot> dots;
    private String content;

    private int type = TYPE_NUMBER;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ArrayList<SimpleDot> getDots() {
        return dots;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setDots(ArrayList<SimpleDot> dots) {
        this.dots = dots;
    }

    public String getDotsPath() {
        return dotsPath;
    }

    public void setDotsPath(String dotsPath) {
        this.dotsPath = dotsPath;
    }

    public int getRowLocation() {
        return rowLocation;
    }

    public void setRowLocation(int rowLocation) {
        this.rowLocation = rowLocation;
    }

    public int getColumnLocation() {
        return columnLocation;
    }

    public void setColumnLocation(int columnLocation) {
        this.columnLocation = columnLocation;
    }

    public int getWidthCount() {
        return widthCount;

    }

    public void setWidthCount(int widthCount) {
        this.widthCount = widthCount;
    }

    public int getHeightCount() {
        return heightCount;
    }

    public void setHeightCount(int heightCount) {
        this.heightCount = heightCount;
    }

    public int getBook() {
        return book;

    }

    public void setBook(int book) {
        this.book = book;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(",book:" + book);
        sb.append(",page:" + page);
        sb.append(",rowLocation:" + rowLocation);
        sb.append(",columnLocation:" + columnLocation);
        sb.append(",widthCount:" + widthCount);
        sb.append(",heightCount:" + heightCount);
        sb.append(",dotsPath:" + dotsPath);
        sb.append(",imagePath:" + imagePath);
        sb.append(",content:" + content);
        sb.append(",imageUrl:" + imageUrl);

        sb.append(",type:" + type);

        return sb.toString();
    }
}
