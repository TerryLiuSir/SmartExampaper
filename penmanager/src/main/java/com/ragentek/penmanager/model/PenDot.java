package com.ragentek.penmanager.model;

/**
 * Created by xuanyang.feng on 2018/5/23.
 */

public class PenDot {
    public static final int TYPE_DOWN = 1;
    public static final int TYPE_MOVE = 2;
    public static final int TYPE_UP = 3;
    private int type;
    private int Counter;
    private int SectionID;
    private int OwnerID;
    private int BookID;
    private int PageID;
    private long timelong;
    private float x;
    private float y;
    private int angle;
    private int force;

    public void setType(int type) {
        this.type = type;
    }

    public void setCounter(int counter) {
        Counter = counter;
    }

    public void setSectionID(int sectionID) {
        SectionID = sectionID;
    }

    public void setOwnerID(int ownerID) {
        OwnerID = ownerID;
    }

    public void setBookID(int bookID) {
        BookID = bookID;
    }

    public void setPageID(int pageID) {
        PageID = pageID;
    }

    public void setTimelong(long timelong) {
        this.timelong = timelong;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }


    public void setForce(int force) {
        this.force = force;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public PenDot() {
    }

    public PenDot(int type, int counter, int sectionID, int ownerID, int bookID, int pageID, long timelong, int x, int y, int fx, int fy, int force, int angle, float ab_x, float ab_y) {
        this.type = type;
        Counter = counter;
        SectionID = sectionID;
        OwnerID = ownerID;
        BookID = bookID;
        PageID = pageID;
        this.timelong = timelong;
        this.x = x;
        this.y = y;
        this.force = force;
        this.angle = angle;
    }

    public int getType() {
        return type;
    }

    public int getCounter() {
        return Counter;
    }

    public int getSectionID() {
        return SectionID;
    }

    public int getOwnerID() {
        return OwnerID;
    }

    public int getBookID() {
        return BookID;
    }

    public int getPageID() {
        return PageID;
    }

    public long getTimelong() {
        return timelong;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getForce() {
        return force;
    }

    public int getAngle() {
        return angle;
    }


}