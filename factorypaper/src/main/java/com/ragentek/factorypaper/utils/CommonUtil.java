package com.ragentek.factorypaper.utils;

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

    //        Chinese :[0x4e00,0x9fa5](or [19968,40869])
//            Digit:[0x30,0x39](or  [48, 57])
//            capital :[0x61,0x7a](or  [97, 122])
//           lowercase:[0x41,0x5a](or [65, 90])
    public static String getSdPath() {
        File path = Environment.getExternalStorageDirectory();
        return path.getAbsolutePath();

    }

    public static String stampToDate(long timeMillis) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(timeMillis);
        return simpleDateFormat.format(date);
    }

    public static boolean isFloatEquals(float x, float y) {
        return Math.abs(x - y) < 0.0001;
    }


    public static boolean isDigit(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNumber(String numStr) {
        String regex = "^[1-9]+[0-9]*([.][1-9]+)?$";
        return numStr.matches(regex);
    }

    public static boolean isDate(String numStr) {
        String regex = "(^[1-9])[0-9]{0,3}.*$";
        return numStr.matches(regex);
    }

    public static boolean isChoiceAnswer(String str) {
        String regex = "^[A-D]+$";
        return str.matches(regex);
    }

    public static void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                deleteFile(f);
            }
            file.delete();
        } else if (file.exists()) {
            file.delete();
        }
    }

}
