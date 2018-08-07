package com.ragentek.exercisespaper.paper;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;


import com.ragentek.exercisespaper.BaseApplication;
import com.ragentek.exercisespaper.paper.models.SimpleDot;
import com.ragentek.exercisespaper.utils.CommonUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import static com.ragentek.exercisespaper.paper.PaperManager.PaperSize.A5;


public class PaperManager {
    private static final String TAG = "PaperManager";

    private static PaperManager mPaperManager;
    private static final int SQUARE_RATIO = 4;
    private PaperSize currentPaperSize = A5;
    private PageUtil mPageUtils;
    public static final String COORDINATE_SEPARATOR = ",";
    private static final String FILE_NAME_CHAPTER = "chapter";
    private static final String FILE_NAME_INDEX = "index";
    private static final String FILE_NAME_WHOLE_DATA = "data";
    private static final String FILE_NAME_WHOLE_CONTENT = "content";

    public enum PaperSize {
        A5, A4, A3
    }

    private PaperManager() {
        mPageUtils = new PageUtil(currentPaperSize);
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


    public void setPaperSize(PaperSize size) {
        currentPaperSize = size;
        mPageUtils = new PageUtil(currentPaperSize);
    }


    public void savePage2File(ArrayList<SimpleDot> simpleDots, int book, int page, boolean isClassify) {
        String fileName;
        SimpleDot simpleDot = null;
        String filePath = BaseApplication.EXERCISE_SAVE_PATH + File.separator + book + File.separator + page;

        if (!isClassify) {
            fileName = FILE_NAME_WHOLE_DATA;
        } else {
            float type = simpleDots.get(1).x;
            printLog("type:" + type);
            if (Math.abs(type - PageUtil.AREA_TYPE_CHAPTER) < 0.00001) {
                fileName = FILE_NAME_CHAPTER;
            } else if (Math.abs(type - PageUtil.AREA_TYPE_INDEX) < 0.00001) {
                fileName = FILE_NAME_INDEX;
            } else {
                fileName = FILE_NAME_WHOLE_CONTENT;
            }
        }

        File file = new File(filePath, fileName);
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

//               Writes a sequence of bytes to this channel from the given buffer.
//
//              <p> Bytes are written starting at this channel's current file position
//              unless the channel is in append mode, in which case the position is
//               first advanced to the end of the file.  The file is grown, if necessary,
//               to accommodate the written bytes, and then the file position is updated
//              with the number of bytes actually written.  Otherwise this method
//               behaves exactly as specified by the {@link WritableByteChannel}
//               interface. </p>

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


    public ArrayList<SimpleDot> readIndexData(int book, int page, int row) {
        printLog("readIndexData row:" + row);

        ArrayList<SimpleDot> simpleDots = new ArrayList<>();
        String filePath = BaseApplication.EXERCISE_SAVE_PATH + File.separator + book + File.separator + page;
        File file = new File(filePath, FILE_NAME_INDEX);
        RandomAccessFile rFile = null;
        try {
            boolean isCurrentRow = false;

            rFile = new RandomAccessFile(file, "rw");
            FileChannel inChannel = rFile.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(4 * 2);
            while (inChannel.read(buffer) != -1) {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    float x = buffer.getFloat();
                    float y = buffer.getFloat();
//                    printLog("readIndexData x：" + x + ",y:" + y);
//                    printLog("readIndexData AREA_TYPE_INDEX：" + (Math.abs(x - PageUtil.AREA_TYPE_INDEX) < 0.00001));

                    if (Math.abs(x - PageUtil.AREA_TYPE_INDEX) < 0.00001) {
                        isCurrentRow = false;
//                        printLog("readIndexData  y - row：" + (Math.abs(y - row) < 0.00001));
                        if (Math.abs(y - row) < 0.00001) {
                            isCurrentRow = true;
                        }
                    }
                    if (isCurrentRow) {
                        simpleDots.add(new SimpleDot(x, y, page));
                    }
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
        printLog("readIndexData ,end：" + simpleDots.size());
        return simpleDots;

    }

    /**
     * @param book real bookid
     * @param page real pageid
     * @return dot list
     */
    public ArrayList<SimpleDot> readChapter(int book, int page) {
        printLog("readChapter ,book：" + book + ",page:" + page);

        ArrayList<SimpleDot> simpleDots = new ArrayList<>();
        String filePath = BaseApplication.EXERCISE_SAVE_PATH + File.separator + book + File.separator + page;
        File file = new File(filePath, FILE_NAME_CHAPTER);
        RandomAccessFile rFile = null;
        try {
            rFile = new RandomAccessFile(file, "rw");
            FileChannel inChannel = rFile.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(4 * 2);
            while (inChannel.read(buffer) != -1) {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    float x = buffer.getFloat();
                    float y = buffer.getFloat();
                    simpleDots.add(new SimpleDot(x, y, page));
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
        printLog("readChapter ,end：" + simpleDots.size());
        return simpleDots;

    }

    public boolean cleaChapterrData(int book, int page) {
        printLog("cleaChapterrData book:" + book + ",page:" + page);
        ArrayList<SimpleDot> simpleDots = new ArrayList<>();
        String filePath = BaseApplication.EXERCISE_SAVE_PATH + File.separator + book + File.separator + page;
        File file = new File(filePath, FILE_NAME_CHAPTER);
        return file.delete();

    }

    public ArrayList<SimpleDot> readAnswerData(int book, int page, int startRow, int endRow) {
        printLog("readChapter ,book：" + book + ",page" + page + ",startRow" + startRow + ",endRow" + endRow);

        ArrayList<SimpleDot> simpleDots = new ArrayList<>();
        String filePath = BaseApplication.EXERCISE_SAVE_PATH + File.separator + book + File.separator + page;
        File file = new File(filePath, FILE_NAME_WHOLE_CONTENT);
        RandomAccessFile rFile = null;
        try {
            rFile = new RandomAccessFile(file, "rw");
            FileChannel inChannel = rFile.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(4 * 2);
            boolean isNewStroke = false;
            boolean isContain = false;

            while (inChannel.read(buffer) != -1) {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    float x = buffer.getFloat();
                    float y = buffer.getFloat();
                    if (CommonUtil.isFloatEquals(PageUtil.DOWN_ACTION, x)) {
                        isNewStroke = true;
                        continue;
                    }
//                    printLog("readRowData isNewStroke.... " + isNewStroke);
                    if (isNewStroke) {
                        isNewStroke = false;
                        int rowNum = (int) y;
//                        printLog("readRowData rowNum.... " + rowNum);
                        if (rowNum >= startRow && rowNum <= endRow) {
                            isContain = true;
                        } else {
                            isContain = false;
                        }
                    }
//                    printLog("readRowData isContain.... " + isContain);
                    if (isContain) {
                        simpleDots.add(new SimpleDot(x, y, page));
                    }
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
        printLog("readPageData ,end：" + simpleDots.size());
        return simpleDots;
    }

    /**
     * @param book real book id
     * @param page real page id
     * @return dots of page
     */
    public ArrayList<SimpleDot> readPageData(int book, int page) {
        ArrayList<SimpleDot> simpleDots = new ArrayList<>();
        String filePath = BaseApplication.EXERCISE_SAVE_PATH + File.separator + book + File.separator + page;
        File file = new File(filePath, FILE_NAME_WHOLE_DATA);
        RandomAccessFile rFile = null;
        try {
            rFile = new RandomAccessFile(file, "rw");
            FileChannel inChannel = rFile.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(4 * 2);
            while (inChannel.read(buffer) != -1) {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    float x = buffer.getFloat();
                    float y = buffer.getFloat();
                    simpleDots.add(new SimpleDot(x, y, page));
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
        printLog("readPageData ,end：" + simpleDots.size());
        return simpleDots;

    }

    /**
     * @param book     real bok id
     * @param page     real page id
     * @param startRow real number of the row, for start
     * @param endRow   real number of   the row ,for end
     * @return dots of rows
     */
    public ArrayList<SimpleDot> readRowData(int book, int page, int startRow, int endRow) {
        printLog("readRowData.... ..book:" + book + ",page" + page + ",startRow" + startRow + ",endRow" + endRow);

        ArrayList<SimpleDot> pageDots = readPageData(book, page);
        ArrayList<SimpleDot> rowDots = new ArrayList<>();

        boolean isNewStroke = false;
        boolean isContain = false;

        for (int i = 0; i < pageDots.size(); i++) {
            SimpleDot point = pageDots.get(i);
            if (CommonUtil.isFloatEquals(PageUtil.DOWN_ACTION, point.x)) {
                isNewStroke = true;
                continue;
            }
//            printLog("readRowData isNewStroke.... " + isNewStroke);
            if (isNewStroke) {
                isNewStroke = false;
//                printLog("readRowData.... ..y:" + point.y + ", point.x" + point.x);

                int rowNum = (int) point.y;
//                printLog("readRowData rowNum.... " + rowNum);
                if (rowNum >= startRow && rowNum <= endRow) {
                    isContain = true;
                } else {
                    isContain = false;
                }

            }
//            printLog("readRowData isContain.... " + isContain);

            if (isContain) {
                rowDots.add(point);
            }

        }
        return rowDots;

    }


    private void deleteFile(File file) {
        printLog("deleteFile：" + file.getAbsolutePath());
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

    public synchronized void clearBook(int book) {
        File file = new File(BaseApplication.EXERCISE_SAVE_PATH + File.separator + book);
        printLog("clearBook：" + file.getPath());
        deleteFile(file);
    }

    public synchronized void clearPage(int book, int page) {
        File file = new File(BaseApplication.EXERCISE_SAVE_PATH + File.separator + book + File.separator + page);
        printLog("clearPage：" + file.getPath());
        deleteFile(file);
    }


    public SimpleDot mapExercises2ScreenArea(SimpleDot dot, int areaWidth, int areaHeight, float offsetx, float offsety) {
        SimpleDot mSimpleDot;
        float questionWidth = mPageUtils.getWidth();
        float questionHeight = mPageUtils.getHeight();
        float ratio = Math.min(areaWidth / questionWidth, areaHeight / questionHeight);
//        Log.d(TAG, "all origin dot.x < 0  :" + (dot.x < 0));
        if (dot.x < 0) {
            float x = dot.x;
            float y = dot.y;
            mSimpleDot = new SimpleDot(x, y, dot.pageId);
        } else {
            float x = ratio * (dot.x - offsetx);
            float y = ratio * (dot.y - offsety);
//            Log.d(TAG, "x" + x + ",y:" + y);
//            Log.d(TAG, "ratio：" + ratio);
            mSimpleDot = new SimpleDot(x, y, dot.pageId);
        }
        return mSimpleDot;
    }

    public SimpleDot mapExercises2ScreenArea(SimpleDot dot, int areaWidth, int areaHeight) {
        SimpleDot mSimpleDot;
        float questionWidth = mPageUtils.getWidth();
        float questionHeight = mPageUtils.getHeight();
        float ratio = Math.min(areaWidth / questionWidth, areaHeight / questionHeight);
//        Log.d(TAG, "all origin dot.x < 0  :" + (dot.x < 0));
        if (dot.x < 0) {
            float x = dot.x;
            float y = dot.y;
            mSimpleDot = new SimpleDot(x, y, dot.pageId);
        } else {
            float x = ratio * dot.x;
            float y = ratio * dot.y;
//            Log.d(TAG, "x" + x + ",y:" + y);
//            Log.d(TAG, "ratio：" + ratio);
            mSimpleDot = new SimpleDot(x, y, dot.pageId);
        }
        return mSimpleDot;
    }

    public boolean generateQuesionImage(final ArrayList<SimpleDot> dos, final int width, final int height, final String filePath, boolean isAI) {
        Log.d(TAG, " generateImage:" + filePath);
        boolean isSuccess = true;
        File file = new File(filePath);

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
        boolean isNewStroke = false;
        for (int i = 0; i < dos.size(); i++) {
            SimpleDot point = dos.get(i);
            if (point.x < 0) {
                isNewStroke = true;
                continue;
            }
            // SimpleDot x ,SimpleDot y <0  is the  down point
            if (isNewStroke) {
                path.moveTo(dos.get(i).x, dos.get(i).y);
                isNewStroke = false;
            } else {
                path.lineTo(point.x, point.y);
            }

        }
        canvas.drawPath(path, paint);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        if (file.exists()) {
            isSuccess = file.delete();
        }
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            isSuccess = file.createNewFile();
            Log.i(TAG, file.getPath());
            FileOutputStream fos = null;
            fos = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "generateQuesionImage error");
            isSuccess = false;
        }
        return isSuccess;
    }


    public boolean generateSquareImage(final ArrayList<SimpleDot> dots, final String filePath) {
        Log.d(TAG, " generateSquareImage:" + filePath);
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
                break;
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
        Log.d(TAG, " squareWidth:" + squareWidth);

        ArrayList<SimpleDot> resizedSimpleDot = new ArrayList<>();
        for (SimpleDot dot : dots) {
            if (dot.x < 0) {
                resizedSimpleDot.add(dot);
                continue;
            }
            SimpleDot mewDot = new SimpleDot((dot.x - offsetX) * SQUARE_RATIO, (dot.y - offsetY) * SQUARE_RATIO, dot.pageId);
            resizedSimpleDot.add(mewDot);
        }
        return generateQuesionImage(resizedSimpleDot, squareWidth * SQUARE_RATIO, squareWidth * SQUARE_RATIO, filePath, true);

    }

    //
//    public boolean generateAllSquareImage() {
//        Log.d(TAG, " generateAllQuesionImage:");
//        File dotsFile = new File(BaseApplication.DOTS_SAVE_PATH);
//        if (!dotsFile.exists() || dotsFile.list().length < 1) {
//            return false;
//
//        }
//        File[] files = dotsFile.listFiles();
//
//
//        //TODO
//        for (int i = 0; i < files.length; i++) {
//            if (files[i].isFile()) {
//                File dotFile = new File(files[i].getPath());
//                Log.d(TAG, " dotFile:" + dotFile.getPath());
//                //read dots
//                int questionIndex = Integer.parseInt(dotFile.getName());
//                ArrayList<SimpleDot> dots = readFloatFromData(questionIndex);
//
//                //map dots to screen
//                Log.d(TAG, " dotFile:" + dots.size());
//                float left = 0;
//                float right = 0;
//                float bottom = 0;
//                float top = 0;
//                for (int j = 0; j < dots.size(); j++) {
//                    SimpleDot dot = dots.get(j);
//                    if (dot.x > 0) {
//                        left = dot.x;
//                        right = dot.x;
//                        top = dot.y;
//                        bottom = dot.y;
//                    }
//                }
//                for (int j = 0; j < dots.size(); j++) {
//                    SimpleDot dot = dots.get(j);
//
//                    if (dot.x > 0) {
//                        if (dot.x < left) {
//                            left = dot.x;
//                        } else if (dot.x > right) {
//                            right = dot.x;
//                        }
//                        if (dot.y < top) {
//                            top = dot.y;
//                        } else if (dot.y > bottom) {
//                            bottom = dot.y;
//                        }
//                    }
//                }
//
//                float dotWidth = (right - left);
//                float dotHeight = (bottom - top);
//                int squareWidth = (int) (Math.max(dotWidth, dotHeight) * 1.4);
//                float offsetX = left - (squareWidth - dotWidth) / 2;
//                float offsetY = top - (squareWidth - dotHeight) / 2;
//                ArrayList<SimpleDot> resizedSimpleDot = new ArrayList<>();
//                for (SimpleDot dot : dots) {
//                    if (dot.x < 0) {
//                        resizedSimpleDot.add(dot);
//                        continue;
//                    }
//                    SimpleDot mewDot = new SimpleDot((dot.x - offsetX) * SQUARE_RATIO, (dot.y - offsetY) * SQUARE_RATIO, dot.pageId);
//                    resizedSimpleDot.add(mewDot);
//                }
//                Log.d(TAG, " squareWidth:" + squareWidth + ",offsetX:" + offsetX + ",offsetY" + offsetY);
//                File saveFile = new File(BaseApplication.IMAGE_AI_SAVE_PATH + File.separator + questionIndex + BaseApplication.IMAGE_SAVE_FORMAT_JPG);
////test generate image
////                File saveFile = new File(BaseApplication.IMAGE_AI_SAVE_PATH + File.separator + System.currentTimeMillis() + "_" + questionIndex + BaseApplication.IMAGE_SAVE_FORMAT_JPG);
//                PaperManager.generateQuesionImage(resizedSimpleDot, squareWidth * SQUARE_RATIO, squareWidth * SQUARE_RATIO, saveFile, true);
//                //test generate image
////                dotFile.delete();
//            }
//
//        }
//
//        return true;
//    }


    /***
     *discrepancy x:1
     * discrepancy y:3
     * @param dots  line dots
     * @return
     */
    public boolean isStraight(ArrayList<SimpleDot> dots) {
        float maxX = -1;
        float minX = -1;
        float maxY = -1;
        float minY = -1;

        for (SimpleDot dot : dots) {
            if (dot.x > 0) {
                minX = dot.x;
                maxX = dot.x;
                minY = dot.y;
                maxY = dot.y;
                break;
            }

        }
        for (int j = 0; j < dots.size(); j++) {
            SimpleDot dot = dots.get(j);

            if (dot.x > 0) {
                if (dot.x < maxX) {
                    minX = dot.x;
                } else if (dot.x > minX) {
                    minX = dot.x;
                }
                if (dot.y < minY) {
                    minY = dot.y;
                } else if (dot.y > maxY) {
                    maxY = dot.y;
                }
            }
        }
        printLog("isClearAction minX:" + minX + ",maxX:" + maxX + ",minY:" + minY + ",maxY:" + maxY);
        printLog("isClearAction > 3:" + (Math.abs(maxX - minX) > 3));
        printLog("isClearAction < 1:" + (Math.abs(maxY - minY) < 1));
        return Math.abs(maxX - minX) > 3 && Math.abs(maxY - minY) < 1;


    }

    private void printLog(String message) {
        Log.d(TAG, message);
    }
}

