package com.ragentek.exercisespaper.paper;

import android.util.Log;


/**
 * Created by xuanyang.feng on 2018/6/19.
 */

public class PageUtil {
    private static final String TAG = "PageUtil";
    //typological point
    public static final float DOWN_ACTION = -1.0f;
    public static final float AREA_TYPE_CHAPTER = -2.0f;
    public static final float AREA_TYPE_INDEX = -3.0f;
    public static final float AREA_TYPE_CONTENT = -4.0f;
    public static final float AREA_TYPE_UNKNOW = -5.0f;

    private float[] chapterArea = {1f, 5f, 50f, 16f};
    private int questionIndexHeight = 6;
    private float[] questionIndexArea = {1f, 16f, 30f, 146f};
    private float[] questionContextArea = {30f, 16f, 105f, 146f};
    private int rowConut = 22;
    float offset = 0.5f;

    private float width = 110f;
    private float height = 155f;
    //Math.abs(a-b)<0.00000001
    private PaperManager.PaperSize paperSize;

    public PageUtil(PaperManager.PaperSize size) {
        paperSize = size;
        switch (paperSize) {
            case A5:

            case A4:
                break;
            case A3:
                break;
            default:
                break;
        }
    }

    public float getRowHeight() {
        return (questionContextArea[3] - questionContextArea[1]) / rowConut;
    }

    public PaperManager.PaperSize getPaperSize() {
        return paperSize;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float[] getChapterArea() {
        return chapterArea;
    }

    public int getQuestionIndexHeight() {
        return questionIndexHeight;
    }

    public float[] getQuestionIndexArea() {
        return questionIndexArea;
    }

    public float[] getQuestionContextArea() {
        return questionContextArea;
    }

    public int getRowConut() {
        return rowConut;
    }


    public int getRow(float y) {
        Log.d(TAG, "getRow:" + y);
        y = y - offset;
        if (y < questionContextArea[1]) {
            return 0;
        } else if (y > questionContextArea[3]) {
            return -1;
        }
        float rowHeight = (questionContextArea[3] - questionContextArea[1]) / 22f;
        int row = (int) ((y - questionContextArea[1]) / rowHeight) + 1;
        return row;
    }

    public float getAreaType(float x, float y) {
        if (x > questionContextArea[0] && y > questionContextArea[1]) {
            return AREA_TYPE_CONTENT;
        }
        if (x > questionIndexArea[0] && x < questionIndexArea[2]
                && y > questionIndexArea[1] && y < questionIndexArea[3]) {
            return AREA_TYPE_INDEX;
        }
        if (x > chapterArea[0] && x < chapterArea[2]
                && y > chapterArea[1] && y < chapterArea[3]) {
            return AREA_TYPE_CHAPTER;
        }
        return AREA_TYPE_UNKNOW;
    }

}

