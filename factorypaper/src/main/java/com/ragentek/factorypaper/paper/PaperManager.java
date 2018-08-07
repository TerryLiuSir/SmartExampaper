package com.ragentek.factorypaper.paper;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;


import com.ragentek.factorypaper.BaseApplication;
import com.ragentek.factorypaper.paper.models.SimpleDot;
import com.ragentek.factorypaper.paper.models.GridItem;
import com.ragentek.factorypaper.utils.CommonUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;


public class PaperManager {
    private static final String TAG = "PaperManager";

    private static PaperManager mPaperManager;
    public static final String COORDINATE_SEPARATOR = ",";

    private static final int SQUARE_RATIO = 4;
    private Page mPage;
    private static final String FILE_NAME_WHOLE_DATA = "data";


    //for factory table
    //    测试日期 ，产品名称 ，提供数量 ，测试数量 ，次品描述 ，试验结果 ，测试人 ，报告编号 ，审批确认人
//    private int[] PAGE_COLUMNS = {2, 2, 1, 1, 3, 1, 2, 2, 2};
//    private int PAGE_ROW_COUNT = 20;
//    private float START_X = 3f;
//    private float START_Y = 3f;
//    private float END_X = 215f;
//    private float END_Y = 146f;
    //for node book
    //    测试日期 ，产品名称 ，提供数量 ，测试数量 ，次品描述 ，试验结果
    private int[] PAGE_COLUMNS = {1, 1, 1, 1, 1, 1};
    private int[] COLUMN_TYPES = {GridItem.TYPE_DATE, GridItem.TYPE_CHINNESE, GridItem.TYPE_NUMBER, GridItem.TYPE_NUMBER, GridItem.TYPE_CHINNESE, GridItem.TYPE_CHINNESE};

    private int PAGE_ROW_COUNT = 22;
    private float START_X = 12f;
    private float START_Y = 16f;
    private float END_X = 104f;
    private float END_Y = 146f;
    public static final int GRIDE_ITEM_ROW_COUNT = 1;

    private PaperManager() {
        Page.PageBuilder builder = new Page.PageBuilder(PAGE_ROW_COUNT);
        for (int column : PAGE_COLUMNS) {
            builder.addColumn(column);
        }
        mPage = builder
                .setStartX(START_X)
                .setStartY(START_Y)
                .setEndX(END_X)
                .setEndY(END_Y)
                .build();
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

    public int getRowCount() {
        return PAGE_ROW_COUNT;
    }

    public int getColumnCount() {
        return PAGE_COLUMNS.length;
    }

    public GridItem getTableItem(float x, float y, int pageid, int bookid) {
        printLog("getTableItem: " + x + ",y:" + y);

        GridItem item = mPage.getTableItem(x, y);
        if (item == null) {
            return null;
        }
        item.setPage(pageid);
        item.setBook(bookid);
        return item;
    }

    public void saveDots2File(ArrayList<SimpleDot> simpleDots, int book, int page, boolean isClassify) {
        printLog("saveDots2File: " + simpleDots.size() + ",book:" + book + ",page:" + page);

        String fileName;
        SimpleDot simpleDot = null;
        GridItem mTableItem = null;
        StringBuilder fileBuilder = new StringBuilder(BaseApplication.EXERCISE_SAVE_PATH);
        fileBuilder.append(File.separator + book)
                .append(File.separator + page);
        if (!isClassify) {
            fileName = FILE_NAME_WHOLE_DATA;
        } else {
            for (SimpleDot dot : simpleDots) {
                if (dot.x > 0 && dot.y > 0) {
                    mTableItem = mPage.getTableItem(dot.x, dot.y);
                    break;
                }
            }
            fileBuilder.append(File.separator + mTableItem.getRowLocation())
                    .append(File.separator + mTableItem.getColumnLocation());
            fileName = FILE_NAME_WHOLE_DATA;
        }


        Log.d(TAG, fileBuilder.toString());
        File file = new File(fileBuilder.toString(), fileName);
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
            Log.e(TAG, "saveDots2File ERROR:" + ex.getMessage());
            System.err.println();
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


    public ArrayList<SimpleDot> readTableItemData(int book, int page, int rowLocation, int columnLocation) {
        printLog("readTableItemData ,book：" + book + ",page:" + page + ",rowLocation:" + rowLocation + ",columnLocation:" + columnLocation);

        ArrayList<SimpleDot> simpleDots = new ArrayList<>();
        StringBuilder fileBuilder = new StringBuilder(BaseApplication.EXERCISE_SAVE_PATH);
        fileBuilder.append(File.separator + book)
                .append(File.separator + page)
                .append(File.separator + rowLocation)
                .append(File.separator + columnLocation);

        String filePath = fileBuilder.toString();
        String fileName = FILE_NAME_WHOLE_DATA;
        File file = new File(filePath, fileName);
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
                    simpleDots.add(new SimpleDot(x, y, page, book));
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
        printLog("readTableItemData ,end：" + simpleDots.size());
        return simpleDots;

    }


    /**
     * @param book real book id
     * @param page real page id
     * @return dots of page
     */
    public ArrayList<SimpleDot> readPageData(int book, int page) {
        ArrayList<SimpleDot> simpleDots = new ArrayList<>();

        StringBuilder fileBuilder = new StringBuilder(BaseApplication.EXERCISE_SAVE_PATH);
        fileBuilder.append(File.separator + book)
                .append(File.separator + page);
        String filePath = fileBuilder.toString();
        String fileName = FILE_NAME_WHOLE_DATA;
        File file = new File(filePath, fileName);
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
                    simpleDots.add(new SimpleDot(x, y, page, book));
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
        return Math.abs(maxX - minX) > 3 && Math.abs(maxY - minY) < 1;


    }

    public SimpleDot mapExercises2ScreenArea(SimpleDot dot, int areaWidth, int areaHeight, GridItem tableItem) {

        SimpleDot mSimpleDot;

        float questionWidth = mPage.getGridItemWidth() * tableItem.getWidthCount();
        float questionHeight = mPage.getGridItemHeight() * tableItem.getHeightCount();


        float ratio = Math.min(areaWidth / questionWidth, areaHeight / questionHeight);
        if (dot.x < 0) {
            float x = dot.x;
            float y = dot.y;
            mSimpleDot = new SimpleDot(x, y, dot.pageId, dot.bookid);
        } else {
            float x = ratio * (dot.x - mPage.getLeft(tableItem.getColumnLocation()));
            float y = ratio * (dot.y - mPage.getTop(tableItem.getRowLocation()));
            mSimpleDot = new SimpleDot(x, y, dot.pageId, dot.bookid);
        }
        return mSimpleDot;
    }


    public synchronized void clear(int book, int page, int row, int column) {
        printLog("clear：" + book + "" + ",page" + page + ",row" + row + ",column" + column);
        StringBuilder fileBuilder = new StringBuilder(BaseApplication.EXERCISE_SAVE_PATH);
        fileBuilder.append(File.separator + book)
                .append(File.separator + page)
                .append(File.separator + row)
                .append(File.separator + column);
        File file = new File(fileBuilder.toString(), FILE_NAME_WHOLE_DATA);
        CommonUtil.deleteFile(file);
    }

    public synchronized void clear(int book, int page) {
        printLog("clear：" + book + "" + ",page" + page);
        StringBuilder fileBuilder = new StringBuilder(BaseApplication.EXERCISE_SAVE_PATH);
        fileBuilder.append(File.separator + book)
                .append(File.separator + page);
        File file = new File(fileBuilder.toString());
        CommonUtil.deleteFile(file);
    }

    public ArrayList<GridItem> getTableItems(int bookid, int pageid) {
        ArrayList<GridItem> list = new ArrayList<>();
        StringBuilder fileBuilder = new StringBuilder(BaseApplication.EXERCISE_SAVE_PATH);
        fileBuilder.append(File.separator + bookid)
                .append(File.separator + pageid);
        File file = new File(fileBuilder.toString());
        int row = -1;
        int column = -1;
        if (file.isDirectory()) {
            File[] rowfiles = file.listFiles();
            for (int i = 0; i < rowfiles.length; i++) {
                File rowf = rowfiles[i];
                row = Integer.parseInt(rowf.getName());

                File[] columnfiles = rowf.listFiles();
                for (int j = 0; j < columnfiles.length; j++) {
                    File columnf = columnfiles[j];
                    column = Integer.parseInt(columnf.getName());

                    File[] datafiles = columnf.listFiles();

                    for (int n = 0; n < datafiles.length; n++) {
                        File dataf = datafiles[n];
                        if (FILE_NAME_WHOLE_DATA.equals(dataf.getName())) {
                            printLog("getTableItems：" + dataf.getName());
                            GridItem item = new GridItem();
                            item.setBook(bookid);
                            item.setPage(pageid);
                            item.setColumnLocation(column);
                            item.setWidthCount(PAGE_COLUMNS[column]);
                            item.setRowLocation(row);
                            item.setHeightCount(GRIDE_ITEM_ROW_COUNT);
                            item.setType(COLUMN_TYPES[column]);
                            item.setDotsPath(dataf.getPath());
                            printLog("getTableItems：" + item.toString());
                            list.add(item);
                        }
                    }
                }
            }
        }
        return list;

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

    private void printLog(String message) {
        Log.d(TAG, message);
    }
}

