package com.ragentek.factorypaper;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.ragentek.factorypaper.utils.CommonUtil;


/**
 * Created by xuanyang.feng on 2018/5/29.
 */

public class BaseApplication extends Application {

    static public BaseApplication sApplication;
    public static final int DOT_STROKE_IMAGE_WIDTH = 15;
    public static final int DOT_STROKE_SHOW_WIDTH = 5;
    public static final String IMAGE_SAVE_PATH = CommonUtil.getSdPath() + "/a_exercisesPaper";
    public static final String IMAGE_AI_SAVE_PATH = CommonUtil.getSdPath() + "/a_examPaper/ai";
    public static final String EXERCISE_SAVE_PATH = CommonUtil.getSdPath() + "/a_factory";

    public static final String IMAGE_SAVE_FORMAT_JPG = ".jpg";
    public static final String IMAGE_SAVE_FORMAT_PNG = ".png";

    public static final String DEVICE_NAME = "Smartpen";

//    public static final String FILE_UPLOAD_BASEURL = "http://121.196.194.80";

    public static final String FILE_UPLOAD_BASEURL = "http://121.196.194.80";
    public static final String SUBMUIT_BASEURL = "http://121.196.194.80";

    public static final int DEFAULT_BOOKID = 23;

//    public static final String SUBMUIT_BASEURL = "http://192.168.12.30:8080";
//    public static final String SUBMUIT_RESULT_BASEURL = "http://121.168.12.80:8080";
//public static final String SUBMUIT_BASEURL = "http://192.168.12.30:8080";

//    public static final String SUBMUIT_RESULT_BASEURL = "http://121.196.194.80";

    public static final boolean ENCRYPTED = true;

    public static final long ACCOUNT_ID = 3L;
    public static final int STUDENT_IDENTITY = 2;
    public static final int STUDENT_GRADEID = 1;
    public static final int STUDENT_CLASSID = 2;

    public static final String DB_NAME = "exercises-db";


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
