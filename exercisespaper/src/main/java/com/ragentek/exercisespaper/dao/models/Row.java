package com.ragentek.exercisespaper.dao.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by xuanyang.feng on 2018/6/25.
 */
@Entity
public class Row {
    @Id
    private Long id;
    @NotNull
    private Long pageId;
    @NotNull
    private int row;
    private int index;
    @Generated(hash = 540879402)
    public Row(Long id, @NotNull Long pageId, int row, int index) {
        this.id = id;
        this.pageId = pageId;
        this.row = row;
        this.index = index;
    }
    @Generated(hash = 1680760861)
    public Row() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getPageId() {
        return this.pageId;
    }
    public void setPageId(Long pageId) {
        this.pageId = pageId;
    }
    public int getRow() {
        return this.row;
    }
    public void setRow(int row) {
        this.row = row;
    }
    public int getIndex() {
        return this.index;
    }
    public void setIndex(int index) {
        this.index = index;
    }
 

}
