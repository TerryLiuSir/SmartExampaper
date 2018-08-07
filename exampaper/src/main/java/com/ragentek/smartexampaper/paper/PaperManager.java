package com.ragentek.smartexampaper.paper;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.util.Size;

import com.ragentek.smartexampaper.BaseApplication;
import com.ragentek.smartexampaper.paper.models.AnswerArea;
import com.ragentek.smartexampaper.paper.models.SimpleDot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;

/**
 * Created by xuanyang.feng on 2018/5/25.
 */

public class PaperManager {
    private static final String TAG = "PaperManager";

    private static PaperManager mPaperManager;
    public static final int SQUARE_RATIO = 50;

    private PaperManager() {
    }

    public static PaperManager getInstance() {
        if (mPaperManager == null) {
            synchronized (PaperManager.class) {
                if (mPaperManager == null) {
                    mPaperManager = new PaperManager();
                }
            }
        }
        return mPaperManager;
    }


    /**
     * @param simpleDots
     * @param questionIndex question index , also is the name of   dot data file
     */
    public void writeDots2File(ArrayList<SimpleDot> simpleDots, int questionIndex) {
        printLog("writeDots2File.........start:" + simpleDots.size() + ",questionIndex:" + questionIndex);
        SimpleDot simpleDot = null;
        File file = new File(BaseApplication.DOTS_SAVE_PATH, questionIndex + "");
        FileOutputStream fs = null;
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            fs = new FileOutputStream(file, true);
            FileChannel outChannel = fs.getChannel();
            //one float 4 bytes
            ByteBuffer buf = ByteBuffer.allocate(4 * simpleDots.size() * 2);
            buf.clear();
            for (int i = 0; i < simpleDots.size(); i++) {
                simpleDot = simpleDots.get(i);
                buf.putFloat(simpleDot.x);
                buf.putFloat(simpleDot.y);
//                printLog(" writeDots2File:x =" + simpleDot.x + "y=" + simpleDot.y);

            }
            buf.rewind();
            /**
             * Writes a sequence of bytes to this channel from the given buffer.
             *
             * <p> Bytes are written starting at this channel's current file position
             * unless the channel is in append mode, in which case the position is
             * first advanced to the end of the file.  The file is grown, if necessary,
             * to accommodate the written bytes, and then the file position is updated
             * with the number of bytes actually written.  Otherwise this method
             * behaves exactly as specified by the {@link WritableByteChannel}
             * interface. </p>
             */
            outChannel.write(buf);
            outChannel.close();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        } finally {
            try {
                if (fs != null) {
                    fs.flush();
                    fs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        printLog("writeDots2File.........end");

    }

    public ArrayList<SimpleDot> readFloatFromData(int index) {
        ArrayList<SimpleDot> simpleDots = new ArrayList<>();
        printLog(".......readFloatFromData....start.....index:" + index);
        RandomAccessFile rFile = null;
        try {
            rFile = new RandomAccessFile(BaseApplication.DOTS_SAVE_PATH + "/" + index, "rw");
            FileChannel inChannel = rFile.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(4 * 2);
            while (inChannel.read(buffer) != -1) {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    float x = buffer.getFloat();
                    float y = buffer.getFloat();
                    simpleDots.add(new SimpleDot(x, y, (int) AnswerArea.QUESTION_AREAS[index][0]));
//                    printLog(" readFloatFromData:x =" + x + "y=" + y);
                }

                buffer.clear();
            }
            inChannel.close();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        } finally {
            try {
                if (rFile != null) {
                    rFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        printLog("readFloatFromData ,end：" + simpleDots.size());
        return simpleDots;

    }

    public synchronized void clear(String dotsFile) {
        File file = new File(dotsFile);
        File[] files = file.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    File dotFile = new File(files[i].getPath());
                    dotFile.delete();
                }
            }
        }

    }

    public static Size getAnswerAreaSize(int index, int screenWidth, int screenHeight) {
        float questionWidth = AnswerArea.QUESTION_AREAS[index][3];
        float questionHeight = AnswerArea.QUESTION_AREAS[index][4];
        float ratio = Math.min(screenWidth / questionWidth, screenHeight / questionHeight);
        int width = (int) (ratio * questionWidth);
        int height = (int) (ratio * questionHeight);
        return new Size(width, height);

    }

    public static SimpleDot mapDot2ScreenArea(SimpleDot dot, int index, int areaWidth, int areaHeight) {
        SimpleDot mSimpleDot;
        float questionX = AnswerArea.QUESTION_AREAS[index][1];
        float questionY = AnswerArea.QUESTION_AREAS[index][2];
        float questionWidth = AnswerArea.QUESTION_AREAS[index][3];
        float questionHeight = AnswerArea.QUESTION_AREAS[index][4];
        float ratio = Math.min(areaWidth / questionWidth, areaHeight / questionHeight);
        Log.d(TAG, "all origin dot x:" + dot.x + ",dot.y:" + dot.y);
        //        Log.d("DotUtils", "questionX:" + questionX);
//        Log.d("DotUtils", "questionY:" + questionY);
        if (dot.x < 0) {
            float x = dot.x;
            float y = dot.y;
            mSimpleDot = new SimpleDot(x, y, dot.pageId);
        } else {
            float x = ratio * (dot.x - questionX);
            float y = ratio * (dot.y - questionY);
            mSimpleDot = new SimpleDot(x, y, dot.pageId);
        }
        Log.d(TAG, "all origin dot mapDot2ScreenArea ,end：" + mSimpleDot.toString());

        return mSimpleDot;
    }

    public static int getQuestionIdexFromDot(SimpleDot dot) {
        Log.d(TAG, "getQuestionIdexFromDot --index:" + dot.toString());
        int index = -1;
        for (int i = 0; i < AnswerArea.QUESTION_AREAS.length; i++) {
            if (AnswerArea.QUESTION_AREAS[i][0] == dot.pageId) {
                if (dot.x >= AnswerArea.QUESTION_AREAS[i][1] && dot.x <= (AnswerArea.QUESTION_AREAS[i][1] + AnswerArea.QUESTION_AREAS[i][3])) {
                    if (dot.y >= AnswerArea.QUESTION_AREAS[i][2] && dot.y <= (AnswerArea.QUESTION_AREAS[i][2] + AnswerArea.QUESTION_AREAS[i][4])) {
                        index = i;
                    }
                }
            }
        }
        Log.d(TAG, " result --index:" + index);
        return index;

    }

    public static void generateQuesionImage(final ArrayList<SimpleDot> dos, final int width, final int height, final File filePath, boolean isAI) {
        Log.d(TAG, " generateImage:" + filePath.getPath());

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.BLACK);
        Canvas canvas = new Canvas(bitmap);
        Path path = new Path();
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(isAI ? BaseApplication.DOT_STROKE_IMAGE_WIDTH : BaseApplication.DOT_STROKE_SHOW_WIDTH);
        paint.setAntiAlias(true);
        for (int i = 0; i < dos.size(); i++) {
//            SimpleDot movePoint = dos.get(i);
//            path.addCircle(movePoint.x, movePoint.y, 4, Path.Direction.CCW);
            SimpleDot point = dos.get(i);
            // SimpleDot x ,SimpleDot y <0  is the  down point
            if (point.x < 0) {
                i++;
                path.moveTo(dos.get(i).x, dos.get(i).y);
            } else {
//                mPath.addCircle(movePoint.x, movePoint.y, 4, Path.Direction.CCW);
                path.lineTo(point.x, point.y);
            }
        }
        canvas.drawPath(path, paint);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        if (filePath.exists()) {
            filePath.delete();
        }
        try {

            if (!filePath.getParentFile().exists()) {
                filePath.getParentFile().mkdirs();
            }
            filePath.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, filePath.getPath());
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean generateAllQuesionImage(int width, int height) {
        Log.d(TAG, " generateAllQuesionImage:");
        File dotsFile = new File(BaseApplication.DOTS_SAVE_PATH);
        if (!dotsFile.exists() || dotsFile.list().length < 1) {
            return false;

        }
        File[] files = dotsFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                File dotFile = new File(files[i].getPath());
                Log.d(TAG, " dotFile:" + dotFile.getPath());
                //read dots
                int questionIndex = Integer.parseInt(dotFile.getName());
                ArrayList<SimpleDot> dots = readFloatFromData(questionIndex);

                //map dots to screen
                Log.d(TAG, " dotFile:" + dots.size());
                ArrayList<SimpleDot> resizedSimpleDot = new ArrayList<>();
                for (SimpleDot dot : dots) {
                    SimpleDot mewDot = PaperManager.mapDot2ScreenArea(dot, questionIndex, width, height);
                    resizedSimpleDot.add(mewDot);
                }
                //generate image
                Size size = PaperManager.getAnswerAreaSize(questionIndex, width, height);
                File saveFile = new File(BaseApplication.IMAGE_SAVE_PATH + File.separator + questionIndex + BaseApplication.IMAGE_SAVE_FORMAT_PNG);
                PaperManager.generateQuesionImage(resizedSimpleDot, size.getWidth(), size.getHeight(), saveFile, false);
            }
        }
        return true;
    }


    public boolean generateAllSquareImage() {
        Log.d(TAG, " generateAllQuesionImage:");
        File dotsFile = new File(BaseApplication.DOTS_SAVE_PATH);
        if (!dotsFile.exists() || dotsFile.list().length < 1) {
            return false;

        }
        File[] files = dotsFile.listFiles();


        //TODO
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                File dotFile = new File(files[i].getPath());
                Log.d(TAG, " dotFile:" + dotFile.getPath());
                //read dots
                int questionIndex = Integer.parseInt(dotFile.getName());
                ArrayList<SimpleDot> dots = readFloatFromData(questionIndex);

                //map dots to screen
                Log.d(TAG, " dotFile:" + dots.size());
                float left = 0;
                float right = 0;
                float bottom = 0;
                float top = 0;
                for (int j = 0; j < dots.size(); j++) {
                    SimpleDot dot = dots.get(j);
                    if (dot.x > 0) {
                        left = dot.x;
                        right = dot.x;
                        top = dot.y;
                        bottom = dot.y;
                    }
                }
                for (int j = 0; j < dots.size(); j++) {
                    SimpleDot dot = dots.get(j);

                    if (dot.x > 0) {
                        if (dot.x < left) {
                            left = dot.x;
                        } else if (dot.x > right) {
                            right = dot.x;
                        }
                        if (dot.y < top) {
                            top = dot.y;
                        } else if (dot.y > bottom) {
                            bottom = dot.y;
                        }
                    }
                }

                float dotWidth = (right - left);
                float dotHeight = (bottom - top);
                int squareWidth = (int) (Math.max(dotWidth, dotHeight) * 1.4);
                float offsetX = left - (squareWidth - dotWidth) / 2;
                float offsetY = top - (squareWidth - dotHeight) / 2;
                ArrayList<SimpleDot> resizedSimpleDot = new ArrayList<>();
                for (SimpleDot dot : dots) {
                    if (dot.x < 0) {
                         resizedSimpleDot.add(dot);
                        continue;
                    }
                    SimpleDot mewDot = new SimpleDot((dot.x - offsetX) * SQUARE_RATIO, (dot.y - offsetY) * SQUARE_RATIO, dot.pageId);
                    resizedSimpleDot.add(mewDot);
                }
                Log.d(TAG, " squareWidth:" + squareWidth + ",offsetX:" + offsetX + ",offsetY" + offsetY);
                File saveFile = new File(BaseApplication.IMAGE_AI_SAVE_PATH + File.separator + questionIndex + BaseApplication.IMAGE_SAVE_FORMAT_JPG);
//test generate image
//                File saveFile = new File(BaseApplication.IMAGE_AI_SAVE_PATH + File.separator + System.currentTimeMillis() + "_" + questionIndex + BaseApplication.IMAGE_SAVE_FORMAT_JPG);
                PaperManager.generateQuesionImage(resizedSimpleDot, squareWidth * SQUARE_RATIO, squareWidth * SQUARE_RATIO, saveFile, true);
                //test generate image
//                dotFile.delete();
            }

        }

        return true;
    }

    private void printLog(String message) {
        Log.d(TAG, message);
    }

}
