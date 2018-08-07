package com.lastark.smartboard.control;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.hanvon.HWCloudManager;

import org.json.JSONObject;

import java.util.UUID;

public class HwRealizeManager {
    private static final String TAG = "HwRealizeManager";

    public static final String LANG_EN = "en"; //英文
    public static final String LANG_JPN = "jpn"; //日文
    public static final String LANG_CHNS = "chns"; //简体中文
    public static final String LANG_CHNT = "chnt"; //繁体中文

    public interface HwRealizeListener {
        void onRealizeFormula(String data, String token);

        void onRealizeSingle(String[] data, String token);

        void onRealizeLine(String[] data, String token);
    }

    private HwRealizeListener mHwRealizeListener;

    private HWCloudManager mHWCloudManager;

    private HandlerThread mHandlerThread;
    private Handler mHandler;

    public HwRealizeManager(Context context, HwRealizeListener listener) {
        mHwRealizeListener = listener;

        mHandlerThread = new HandlerThread("HwRealizeManager");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());



        mHWCloudManager = new HWCloudManager(context, "90f1ce5c-6b5e-4770-bfa2-33c40827e9f4");
    }

    public String realizeFormula(final String data) {
        final String token = getGUID();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String response = mHWCloudManager.formulaLanguage(data);
                if (mHwRealizeListener != null) {
                    mHwRealizeListener.onRealizeFormula(parseFormulaResponse(response),token);
                }
            }
        });
        return token;

    }

    public String realizeSingle(final String lang, final String data) {
        final String token = getGUID();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String response = mHWCloudManager.handSingleLanguage("1", lang, data);
                if (mHwRealizeListener != null) {
                    mHwRealizeListener.onRealizeSingle(parseSingleResponse(response),token);
                }
            }
        });
        return token;
    }

    public String realizeLine(final String lang, final String data) {
        final String token = getGUID();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String response = mHWCloudManager.handLineLanguage(lang, data);
                if (mHwRealizeListener != null) {
                    mHwRealizeListener.onRealizeLine(parseLineResponse(response), token);
                }
            }
        });
        return token;

    }

    public String getGUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String parseFormulaResponse(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            if ("0".equals(obj.getString("code"))) {
                return obj.getString("formulas");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private String[] parseSingleResponse(String response) {

        try {
            JSONObject obj = new JSONObject(response);
            if ("0".equals(obj.getString("code"))) {
                String result = obj.getString("result");
                String[] words = null;
                if (result != null) {
                    String[] wordsChar = result.split(",");
                    words = new String[wordsChar.length];
                    int i = 0;
                    for (String word : wordsChar) {
                        words[i] = String.valueOf((char) Integer.parseInt(word));
                        i++;
                    }
                }
                return words;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private String[] parseLineResponse(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            if ("0".equals(obj.getString("code"))) {
                String result = obj.getString("result");
                String[] words = null;
                if (result != null) {
                    String[] wordsChar = result.split(",0,");
                    words = new String[wordsChar.length];
                    int i = 0;
                    for (String word : wordsChar) {
                        words[i] = "";
                        String[] chars = word.split(",");
                        for (String aChar : chars) {
                            words[i] += String.valueOf((char) Integer.parseInt(aChar));
                        }
                        i++;
                    }
                }
                return words;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void destroy() {
        if (mHandlerThread != null && mHandlerThread.isAlive()) {
            mHandlerThread.quitSafely();
            mHandlerThread = null;
        }
    }
}
