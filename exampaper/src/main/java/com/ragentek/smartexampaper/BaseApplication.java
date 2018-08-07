package com.ragentek.smartexampaper;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.ragentek.smartexampaper.utils.CommonUtil;

/**
 * Created by xuanyang.feng on 2018/5/29.
 */

public class BaseApplication extends Application {

    static public BaseApplication sApplication;
    public static final int DOT_STROKE_IMAGE_WIDTH = 15;
    public static final int DOT_STROKE_SHOW_WIDTH = 6;
    public static final String IMAGE_SAVE_PATH = CommonUtil.getSdPath() + "/a_examPaper";
    public static final String IMAGE_AI_SAVE_PATH = CommonUtil.getSdPath() + "/a_examPaper/ai";
    public static final String DOTS_SAVE_PATH = IMAGE_SAVE_PATH + "/data";
    public static final String IMAGE_SAVE_FORMAT_JPG = ".jpg";
    public static final String IMAGE_SAVE_FORMAT_PNG = ".png";

    public static final String DEVICE_NAME = "Smartpen";


    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
    }

    public static int getScreenWidth() {
        int width = -1;
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) sApplication.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(dm);
            width = dm.widthPixels;
        }
        return width;
    }

    public static int getScreenHeight() {
        int height = -1;
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) sApplication.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(dm);
            height = dm.heightPixels;
        }
        return height;
    }
}
