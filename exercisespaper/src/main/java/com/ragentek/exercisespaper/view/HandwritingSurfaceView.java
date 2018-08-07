package com.ragentek.exercisespaper.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


import com.ragentek.exercisespaper.BaseApplication;
import com.ragentek.exercisespaper.R;
import com.ragentek.exercisespaper.paper.models.SimpleDot;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by xuanyang.feng on 2018/4/28.
 */

public class HandwritingSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    public static final String TAG = "HandwritingSurfaceView";
    private Canvas mCanvas;
    private Path mWritingPath;
    private Paint mTextPaint;
    private Paint mTablePaint;
    private Path mTablePath;

    private volatile boolean mIsDrawing = true;
    private ExecutorService singleThreadPool;
    private volatile boolean mSurfaceDestory;


    public HandwritingSurfaceView(Context context) {
        super(context);
        initView();
        singleThreadPool = Executors.newSingleThreadExecutor();
    }

    public HandwritingSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
        singleThreadPool = Executors.newSingleThreadExecutor();
    }

    public void clear() {
        mIsDrawing = false;
        Canvas canvas = getHolder().lockCanvas();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mWritingPath.reset();
        getHolder().unlockCanvasAndPost(canvas);
        mIsDrawing = true;
        singleThreadPool.execute(new DrawThead());
    }


    private void initView() {

        mHolder = getHolder();
        mHolder.addCallback(this);
        mWritingPath = new Path();

        mTextPaint = new Paint();
        mTextPaint.setStyle(Paint.Style.STROKE);
        mTextPaint.setStrokeWidth(BaseApplication.DOT_STROKE_SHOW_WIDTH);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(Color.WHITE);

        mTablePath = new Path();
        mTablePaint = new Paint();
        mTablePaint.setStyle(Paint.Style.STROKE);
        mTablePaint.setStrokeWidth(BaseApplication.DOT_STROKE_SHOW_WIDTH - 2);
        mTablePaint.setAntiAlias(true);
        mTablePaint.setColor(getContext().getResources().getColor(R.color.colorAccent));

        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceDestory = false;
        singleThreadPool.execute(new DrawThead());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceDestory = true;
    }


    public void drawLine(float startX, float startY, float stopX, float stopY) {
        mTablePath.moveTo(startX, startY);
        mTablePath.lineTo(stopX, stopY);

    }

    public synchronized void loadAnwserArea(final ArrayList<SimpleDot> dos, final boolean clear) {
        if (clear) {
            clear();
        }
        boolean isNewStroke = false;
        for (int i = 0; i < dos.size(); i++) {
            SimpleDot point = dos.get(i);

            /**
             * PageUtil.java
             *  typological point  ,
             public static final float DOWN_ACTION = -1.0f;
             public static final float AREA_TYPE_CHAPTER = -2.0f;
             public static final float AREA_TYPE_INDEX = -3.0f;
             public static final float AREA_TYPE_CONTENT = -4.0f;
             public static final float AREA_TYPE_UNKNOW = -5.0f;
             */
            if (point.x < 0) {
                isNewStroke = true;
                continue;
            }

            if (isNewStroke) {
                mWritingPath.moveTo(dos.get(i).x, dos.get(i).y);
                isNewStroke = false;
            } else {
                mWritingPath.lineTo(point.x, point.y);

            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mWritingPath.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                mWritingPath.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    class DrawThead implements Runnable {

        private void drawBackground() {
            mCanvas.drawPath(mTablePath, mTablePaint);
        }


        private void drawWriting() {
            mCanvas.drawColor(getResources().getColor(R.color.boardColor));
            mCanvas.drawPath(mWritingPath, mTextPaint);
        }

        @Override
        public void run() {
            while (mIsDrawing) {
                try {
                    mCanvas = mHolder.lockCanvas();
                    drawWriting();
                    drawBackground();
                    Thread.sleep(10);
                } catch (Exception e) {
                } finally {
                    if (!mSurfaceDestory) {
                        mHolder.unlockCanvasAndPost(mCanvas);
                    }
                }
            }
        }
    }


    private void printLog(String message) {
        Log.d(TAG, message);
    }


}
