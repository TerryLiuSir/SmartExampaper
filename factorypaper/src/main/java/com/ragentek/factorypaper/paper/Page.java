package com.ragentek.factorypaper.paper;

import android.util.Log;

import com.ragentek.factorypaper.paper.models.GridItem;

import java.util.ArrayList;

import static com.ragentek.factorypaper.paper.PaperManager.GRIDE_ITEM_ROW_COUNT;


/**
 * Created by xuanyang.feng on 2018/6/19.
 */

public class Page {
    private static final String TAG = "Page";
    //typological point
    public static final float DOWN_ACTION = -1.0f;
    public static final float AREA_TYPE_CHAPTER = -2.0f;
    public static final float AREA_TYPE_INDEX = -3.0f;
    public static final float AREA_TYPE_CONTENT = -4.0f;
    public static final float AREA_TYPE_UNKNOW = -5.0f;

    float offset = 0.5f;

    private float width = 110f;
    private float height = 155f;
    //Math.abs(a-b)<0.00000001

    private float grideStartX;
    private float grideStartY;

    private float grideEndX;
    private float grideEndY;
    private int grideRowCount = 0;
    private int grideColumnCount = 0;

    private int[] columes;

    private float grideRowItemWidth = 0;
    private float grideRowItemHeight = 0;



    private Page(PageBuilder builder) {
        width = builder.endX - builder.startX;
        height = builder.endY - builder.startY;
        grideStartX = builder.startX;
        grideStartY = builder.startY;
        grideEndX = builder.endX;
        grideEndY = builder.endY;

        grideRowCount = builder.rowCount;
        ArrayList<Integer> columeArray = builder.columeArray;
        columes = new int[columeArray.size()];
        for (int i = 0; i < columeArray.size(); i++) {
            int value = columeArray.get(i).intValue();
            columes[i] = value;
            grideColumnCount = grideColumnCount + value;
        }
        grideRowItemHeight = height / grideRowCount;
        grideRowItemWidth = width / grideColumnCount;

    }

    public int geColumnIndex(float x) {
        int currentTotal = 0;
        int index = 0;
        for (int i = 0; i < columes.length; i++) {
            if (x > currentTotal * grideRowItemWidth && x < (currentTotal + columes[i]) * grideRowItemWidth) {
                index = i;
            }
            currentTotal = currentTotal + columes[i];
        }
        return index;
    }

    public int geRowIndex(float y) {
        return (int) (y / grideRowItemWidth);
    }

    public int geHeightCount(float y) {
        return GRIDE_ITEM_ROW_COUNT;
    }

    public int getWidthCount(float x) {
        int currentTotal = 0;
        int widthcount = 0;

        for (int i = 0; i < columes.length; i++) {
            if (x > currentTotal * grideRowItemWidth && x < (currentTotal + columes[i]) * grideRowItemWidth) {
                widthcount = columes[i];
                break;
            }
            currentTotal = currentTotal + columes[i];
        }

        return widthcount;
    }

    public float getGridItemHeight() {
        return height / grideRowCount;
    }

    public float getGridItemWidth() {
        return width / grideColumnCount;
    }

    public float getLeft(int count) {
        Log.d(TAG, "getLeft:" + count);
        float left = 0;
        float gridItemWidth = getGridItemWidth();
        for (int i = 0; i < count; i++) {
            left = left + gridItemWidth * columes[i];
        }

        Log.d(TAG, "left:" + left);

        return left + grideStartX;
    }

    public float getTop(int count) {
        Log.d(TAG, "getTop:" + count);
        float top = 0;
        for (int i = 0; i < count; i++) {
            top = top + getGridItemHeight();
        }
        return top + grideStartY;
    }

    public GridItem getTableItem(float x, float y) {
        if (x < grideStartX || x > grideEndX || y < grideStartY || y > grideEndY) {
            return null;

        }
        GridItem item = new GridItem();
        Log.d(TAG, "getTableItem grideRowItemWidth:" + grideRowItemWidth);
        Log.d(TAG, "getTableItem:" + grideRowItemWidth);
        Log.d(TAG, "grideRowItemHeight:" + grideRowItemHeight);

        int currentTotal = 0;
        for (int i = 0; i < columes.length; i++) {
            if ((x - grideStartX) > currentTotal * grideRowItemWidth && (x - grideStartX) < (currentTotal + columes[i]) * grideRowItemWidth) {
                item.setWidthCount(columes[i]);
                item.setColumnLocation(i);
            }
            currentTotal = currentTotal + columes[i];
        }
        item.setRowLocation((int) ((y - grideStartY) / grideRowItemHeight));
        item.setHeightCount(GRIDE_ITEM_ROW_COUNT);
        Log.d(TAG, "getTableItem:" + item.toString());

        return item;

    }

    public static class PageBuilder {
        public float startX;
        public float startY;
        public int rowCount;
        public float endX;
        public float endY;
        private ArrayList<Integer> columeArray;

        public PageBuilder(int rowCount) {
            this.rowCount = rowCount;
            columeArray = new ArrayList();
        }

        public PageBuilder setStartX(float startX) {
            this.startX = startX;
            return this;
        }


        public PageBuilder setStartY(float startY) {
            this.startY = startY;
            return this;
        }

        public PageBuilder setEndX(float endX) {
            this.endX = endX;
            return this;
        }


        public PageBuilder setEndY(float endY) {
            this.endY = endY;
            return this;
        }

        public PageBuilder addColumn(int occupyColumeCount) {
            columeArray.add(occupyColumeCount);
            return this;
        }

        public Page build() {
            return new Page(this);
        }
    }

}

