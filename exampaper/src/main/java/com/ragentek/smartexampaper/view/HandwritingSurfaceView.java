package com.ragentek.smartexampaper.view;

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

import com.ragentek.smartexampaper.BaseApplication;
import com.ragentek.smartexampaper.R;
import com.ragentek.smartexampaper.paper.models.SimpleDot;

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
    private Path mPath;
    private Paint mPaint;
    private boolean mIsDrawing;
    private ExecutorService singleThreadPool;


    public HandwritingSurfaceView(Context context) {
        super(context);
    }

    public HandwritingSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
        singleThreadPool = Executors.newSingleThreadExecutor();
    }

    public void clear() {
        printLog("clear start");
        mIsDrawing = false;
        Canvas canvas = getHolder().lockCanvas();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mPath.reset();
        getHolder().unlockCanvasAndPost(canvas);
        printLog("clear end");
        mIsDrawing = true;
        singleThreadPool.execute(new DrawThead());
    }


    private void initView() {
        mHolder = getHolder();
        mHolder.addCallback(this);
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(BaseApplication.DOT_STROKE_SHOW_WIDTH);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsDrawing = true;
        singleThreadPool.execute(new DrawThead());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDrawing = false;

    }


    public synchronized void loadAnwserArea(final ArrayList<SimpleDot> dos, final boolean clear) {

        if (clear) {
            clear();
        }
        printLog("loadAnwserArea dos size=" + dos.size());
        for (int i = 0; i < dos.size(); i++) {
//            printLog("x=" + dos.get(i).x);
//            printLog("y=" + dos.get(i).y);
            SimpleDot point = dos.get(i);
            // SimpleDot x ,SimpleDot y <0  is the  down point
            if (point.x < 0) {
                i++;
                mPath.moveTo(dos.get(i).x, dos.get(i).y);
            } else {
//                mPath.addCircle(movePoint.x, movePoint.y, 4, Path.Direction.CCW);
                mPath.lineTo(point.x, point.y);
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    class DrawThead implements Runnable {
        @Override
        public void run() {
            while (mIsDrawing) {
                try {
                    mCanvas = mHolder.lockCanvas();
                    mCanvas.drawColor(getResources().getColor(R.color.boardColor));
                    mCanvas.drawPath(mPath, mPaint);
                } catch (Exception e) {
                } finally {
                    if (mCanvas != null)
                        mHolder.unlockCanvasAndPost(mCanvas);
                }
            }

        }

    }

    private void printLog(String message) {
        Log.d(TAG, message);
    }


}
