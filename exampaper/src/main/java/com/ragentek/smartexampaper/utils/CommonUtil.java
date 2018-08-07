package com.ragentek.smartexampaper.utils;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by xuanyang.feng on 2018/5/3.
 */

public class CommonUtil {
    private static final String TAG = "CommonUtil";

    public static String getSdPath() {
        File path = Environment.getExternalStorageDirectory();
        return path.getAbsolutePath();

    }

    public static String stampToDate(long timeMillis) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(timeMillis);
        return simpleDateFormat.format(date);
    }


}
