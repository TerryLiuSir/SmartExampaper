package com.ragentek.exercisespaper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lastark.smartboard.control.HwRealizeManager;
import com.ragentek.exercisespaper.dao.DaoSession;
import com.ragentek.exercisespaper.dao.models.Book;
import com.ragentek.exercisespaper.dao.models.Row;
import com.ragentek.exercisespaper.dao.models.User;
import com.ragentek.exercisespaper.net.HttpRequestConstant;
import com.ragentek.exercisespaper.net.HttpRetrofitManager;
import com.ragentek.exercisespaper.net.api.SubmitAPI;
import com.ragentek.exercisespaper.net.models.Homework;
import com.ragentek.exercisespaper.net.models.HomeworkItem;
import com.ragentek.exercisespaper.net.models.SubmitResponse;
import com.ragentek.exercisespaper.net.models.UplodeResponse;
import com.ragentek.exercisespaper.paper.PageUtil;
import com.ragentek.exercisespaper.paper.PaperManager;
import com.ragentek.exercisespaper.paper.models.Answer;
import com.ragentek.exercisespaper.paper.models.RealizeInfoHolder;
import com.ragentek.exercisespaper.paper.models.SimpleDot;
import com.ragentek.exercisespaper.utils.CommonUtil;
import com.ragentek.exercisespaper.view.HandwritingSurfaceView;
import com.ragentek.exercisespaper.view.widgets.CustomAlertDialog;
import com.ragentek.exercisespaper.view.widgets.CustomLoadingDialog;
import com.ragentek.exercisespaper.view.widgets.ListDeviceDialog;
import com.ragentek.penmanager.PenManagerService;
import com.ragentek.penmanager.model.PenDot;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends BaseLauncherActivity {
    public static final String TAG = "MainActivity";

    @BindView(R.id.iv_submit)
    ImageView submit;
    @BindView(R.id.tv_state)
    TextView stateTv;
    @BindView(R.id.tv_book)
    TextView bookTv;
    @BindView(R.id.tv_chapter)
    TextView chapterTv;
    @BindView(R.id.tv_page)
    TextView pageTv;
    @BindView(R.id.tv_index)
    TextView indexTv;
    @BindView(R.id.tv_row)
    TextView rowTv;

    @BindView(R.id.sv_answerArea)
    HandwritingSurfaceView answerAre;
    @BindView(R.id.iv_devices)
    ImageView deviceImage;
    @BindView(R.id.iv_result)
    ImageView resultImage;
    @BindView(R.id.iv_clear)
    ImageView clearImage;
    @BindView(R.id.toolBar)
    RelativeLayout toolBar;

    private static final int PERMISSIONS_REQUEST_CODE = 0x0001;

    private static final int STATE_INITIALIZE = 0x000;
    private static final int STATE_LOGIN = 0x001;
    private static final int STATE_CONNECTED = 0x010;
    private static final int STATE_BOOK_SELLECT = 0x100;
    private static final int STATE_WELL = STATE_LOGIN | STATE_CONNECTED | STATE_BOOK_SELLECT;
    private int currentState = STATE_INITIALIZE;


    private ArrayMap<String, Integer> permisions;
    private Book currentBook;
    private Book lastBook;
    private com.ragentek.exercisespaper.dao.models.Page currentPage;
    private com.ragentek.exercisespaper.dao.models.Page lastPage;
    private Row lastRow;
    private Row currentRow;
    private float lastAreaType;

    private float currentAreaType;
    private boolean newPage;
    private boolean newRow;
    private boolean newArea;

    private PenManagerService.PenManager penHandle;
    private PaperManager mPaperManager;
    private PageUtil mPageUtils;
    private HwRealizeManager mHwRealizeManager;

    private ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
    private volatile ArrayList<SimpleDot> tempDots = new ArrayList<>();
    ArrayMap<String, RealizeInfoHolder> realizeMap = new ArrayMap<>();
    private ListDeviceDialog mListDeviceDialog;
    private DaoSession daoSession;
    private User currentUser;
    private SubmitAPI sumitAPI;
    private SubmitResponse mSubmitResponse;


    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    void initView() {
        stateTv.setText(String.format(getResources().getString(R.string.state), -1));
        pageTv.setText(String.format(getResources().getString(R.string.page), -1));
        bookTv.setText(String.format(getResources().getString(R.string.book), -1));
        rowTv.setText(String.format(getResources().getString(R.string.row), -1));
        chapterTv.setText(String.format(getResources().getString(R.string.chapter), "none"));
        indexTv.setText(String.format(getResources().getString(R.string.question), -1));

        float questionWidth = mPageUtils.getWidth();
        float questionHeight = mPageUtils.getHeight();
        float mapScreenRatio = Math.min(BaseApplication.getScreenWidth() / questionWidth, (BaseApplication.getScreenHeight() - toolBar.getHeight()) / questionHeight);
        answerAre.drawLine(mapScreenRatio * mPageUtils.getChapterArea()[0], mapScreenRatio * mPageUtils.getChapterArea()[1], mapScreenRatio * mPageUtils.getChapterArea()[0], mapScreenRatio * mPageUtils.getChapterArea()[3]);
        answerAre.drawLine(mapScreenRatio * mPageUtils.getChapterArea()[0], mapScreenRatio * mPageUtils.getChapterArea()[1], mapScreenRatio * mPageUtils.getChapterArea()[2], mapScreenRatio * mPageUtils.getChapterArea()[1]);
        answerAre.drawLine(mapScreenRatio * mPageUtils.getChapterArea()[2], mapScreenRatio * mPageUtils.getChapterArea()[1], mapScreenRatio * mPageUtils.getChapterArea()[2], mapScreenRatio * mPageUtils.getChapterArea()[3]);
        answerAre.drawLine(mapScreenRatio * mPageUtils.getChapterArea()[0], mapScreenRatio * mPageUtils.getChapterArea()[3], mapScreenRatio * mPageUtils.getChapterArea()[2], mapScreenRatio * mPageUtils.getChapterArea()[3]);
        answerAre.drawLine(mapScreenRatio * mPageUtils.getQuestionIndexArea()[2], mapScreenRatio * mPageUtils.getQuestionIndexArea()[1], mapScreenRatio * mPageUtils.getQuestionIndexArea()[2], mapScreenRatio * mPageUtils.getQuestionIndexArea()[3]);
        answerAre.drawLine(0, mapScreenRatio * mPageUtils.getQuestionContextArea()[1], mapScreenRatio * mPageUtils.getQuestionContextArea()[2], mapScreenRatio * mPageUtils.getQuestionContextArea()[1]);
        answerAre.drawLine(0, mapScreenRatio * mPageUtils.getQuestionContextArea()[3], mapScreenRatio * mPageUtils.getQuestionContextArea()[2], mapScreenRatio * mPageUtils.getQuestionContextArea()[3]);

    }


    @Override
    ArrayMap<String, Integer> permissionsRequest() {
        if (permisions == null) {
            permisions = new ArrayMap<>();
            permisions.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.sdcard);
            permisions.put(Manifest.permission.ACCESS_FINE_LOCATION, R.string.location);
            permisions.put(Manifest.permission.READ_PHONE_STATE, R.string.phone_state);
        }
        return permisions;
    }

    @OnClick(R.id.iv_devices)
    void scannerDevices() {
        showDevicesList();
        penHandle.startScan();
    }

    @OnClick(R.id.iv_clear)
    void clearHistory() {
        if (currentBook != null && currentPage != null) {
            CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(this);
            builder.setMessage(R.string.clear_tips)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mPaperManager.clearPage(currentBook.getBookNum(), currentPage.getPageNumber());
                            daoSession.getPageDao().deleteByKey(currentPage.getId());
                            for (Row row : currentPage.getRows()) {
//                                daoSession.getRowDao().load(row.getId()).
                                daoSession.getRowDao().deleteByKey(row.getId());

                            }
                            currentPage.resetRows();
                            lastPage = null;
                            lastRow = null;
                            realizeMap.clear();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DatePickerDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            builder.create().show();

        } else {
            Toast.makeText(this, R.string.no_writing, Toast.LENGTH_SHORT).show();
        }
        answerAre.clear();

    }


    @OnClick(R.id.iv_submit)
    void submit() {
        if (checkState()) {
            doSubmit();
        }
    }


    @OnClick(R.id.iv_result)
    void showResult() {
        printLog("showResult：" + mSubmitResponse);
        Intent intent = new Intent(this, SubmitDisplayActivity.class);
        if (mSubmitResponse != null) {
            Uri.Builder builder = Uri.parse(HttpRequestConstant.ANSWER_URL).buildUpon();
            builder.appendQueryParameter(HttpRequestConstant.KEY_IDENTITY, BaseApplication.STUDENT_IDENTITY + "");
            builder.appendQueryParameter(HttpRequestConstant.KEY_DATE, mSubmitResponse.getDate());
            builder.appendQueryParameter(HttpRequestConstant.KEY_GRADEID, mSubmitResponse.getGradeId() + "");
            builder.appendQueryParameter(HttpRequestConstant.KEY_CLASSID, mSubmitResponse.getClassId() + "");
            builder.appendQueryParameter(HttpRequestConstant.KEY_ACCOUNTID, mSubmitResponse.getAccountId() + "");
            builder.appendQueryParameter(HttpRequestConstant.KEY_SUBJECTID, mSubmitResponse.getSubjectId() + "");
            builder.appendQueryParameter(HttpRequestConstant.KEY_SUBJECT_NAME, mSubmitResponse.getSubjectName());
            intent.putExtra(SubmitDisplayActivity.KEY_URL, builder.toString());
        } else {
            Uri.Builder builder = Uri.parse(HttpRequestConstant.CHECK_URL).buildUpon();
            builder.appendQueryParameter(HttpRequestConstant.KEY_IDENTITY, currentUser.getIdentity() + "");
            builder.appendQueryParameter(HttpRequestConstant.KEY_GRADEID, currentUser.getGradeId() + "");
            builder.appendQueryParameter(HttpRequestConstant.KEY_CLASSID, currentUser.getClassId() + "");
            builder.appendQueryParameter(HttpRequestConstant.KEY_ACCOUNTID, currentUser.getAccountId() + "");
            intent.putExtra(SubmitDisplayActivity.KEY_URL, builder.toString());
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseModules();
    }

    @Override
    void initModules() {
        sumitAPI = HttpRetrofitManager.getInstance().setBaseUrl(BaseApplication.FILE_UPLOAD_BASEURL).creatHttpApi(SubmitAPI.class);
        Intent serviceIntent = new Intent(this, PenManagerService.class);
        serviceIntent.setPackage(getPackageName());
        startService(serviceIntent);
        bindService(serviceIntent, mPenManagerConnection, Context.BIND_AUTO_CREATE);
        mPaperManager = PaperManager.getInstance();
        mPageUtils = new PageUtil(PaperManager.PaperSize.A5);
        mHwRealizeManager = new HwRealizeManager(getApplicationContext(), new HwRealizeManager.HwRealizeListener() {
            @Override
            public void onRealizeFormula(String data, String token) {
                printLog("onRealizeFormula");
                if (data != null) {
                    printLog("onRealizeFormula：" + data);
                }
            }

            @Override
            public void onRealizeSingle(String[] data, String token) {
                for (int i = 0; i < data.length; i++) {
                    printLog("onRealizeSingle" + data[i]);
                }
            }

            @Override
            public void onRealizeLine(String[] data, String token) {
                printLog("onRealizeLine data：" + data);
                if (data != null) {
                    String realizeResult = "none";
                    RealizeInfoHolder info = realizeMap.get(token);
                    if (info == null) {
                        return;
                    }
                    switch (info.getType()) {
                        case RealizeInfoHolder.TYPE_AREA_CHAPTER:
                            for (String str : data) {
                                printLog(" AREA_CHAPTER Realize   :" + str + " isNumber:" + CommonUtil.isNumber(str));
                                if (CommonUtil.isNumber(str)) {
                                    if (CommonUtil.isDigit(str)) {
                                        str = str.substring(0, 1) + "." + str.substring(1);
                                        printLog(" AREA_CHAPTER str   :" + str);
                                    }
                                    realizeResult = str;
                                    break;
                                }
                            }
                            updateChapter(info.getBookNum(), info.getPageNum(), realizeResult);
                            break;
                        case RealizeInfoHolder.TYPE_AREA_INDEX:
                            int index = -1;
                            for (String result : data) {
                                printLog("AREA_INDEX Realize ：" + result + ",isDigit:" + CommonUtil.isDigit(result));
                                if (CommonUtil.isDigit(result)) {
                                    index = Integer.parseInt(result);
                                    break;
                                }
                            }
                            printLog(" AREA Realize result：" + index);
                            updateIndex(info.getBookNum(), info.getPageNum(), info.getRowNum(), index);

                            break;
                        case RealizeInfoHolder.TYPE_AREA_RESULT:
                            break;
                        default:
                            Log.e(TAG, "AREA Realize wrong type " + info.getType());
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, R.string.realize_error, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
        daoSession = BaseApplication.getDaoSession();
        currentUser = daoSession.getUserDao().load(BaseApplication.ACCOUNT_ID);
        if (currentUser == null) {
            currentUser = new User(BaseApplication.ACCOUNT_ID, BaseApplication.STUDENT_IDENTITY, BaseApplication.STUDENT_GRADEID, BaseApplication.STUDENT_CLASSID);
            daoSession.getUserDao().insert(currentUser);
        }
        printLog("currentUser:" + currentUser.getAccountId() + "," + currentUser.getGradeId() + "," + currentUser.getClassId() + "," + currentUser.getIdentity());
        addState(STATE_LOGIN);
    }

    private void releaseModules() {
        Intent serviceIntent = new Intent(this, PenManagerService.class);
        serviceIntent.setPackage(getPackageName());
        unbindService(mPenManagerConnection);
        mHwRealizeManager.destroy();
    }

    private void addState(int newState) {
        printLog("addState" + newState);
        currentState = currentState | newState;
    }

    private void removeState(int newState) {
        currentState = currentState ^ newState;
    }

    /**
     * @return true ,if all state id well
     */
    private boolean checkState() {
        printLog("checkState currentState:" + currentState);
        String stateMsg = null;
        if ((STATE_LOGIN & currentState) != STATE_LOGIN) {
            stateMsg = getResources().getString(R.string.login_tips);
        } else if ((STATE_CONNECTED & currentState) != STATE_CONNECTED) {
            stateMsg = getResources().getString(R.string.sellect_device_tips);
        } else if ((STATE_BOOK_SELLECT & currentState) != STATE_BOOK_SELLECT) {
            stateMsg = getResources().getString(R.string.sellect_book_tips);
        }

        if (stateMsg != null) {
            CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(this);
            builder.setMessage(stateMsg)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
            builder.create().show();
            return false;
        }
        return true;
    }

    private void updateIndex(int book, int page, int row, int index) {
        List<Book> books = currentUser.getBooks();
        for (Book bookitem : books) {
            if (bookitem.getBookNum() == book) {
                for (com.ragentek.exercisespaper.dao.models.Page pageitem : bookitem.getPages()) {
                    if (pageitem.getPageNumber() == page) {
                        for (Row rowitem : pageitem.getRows()) {
                            if (rowitem.getRow() == row) {
                                rowitem.setIndex(index);
                                daoSession.getRowDao().update(rowitem);
                                return;
                            }
                        }
                    }

                }
            }
        }
    }

    private void updateChapter(int book, int page, String chapter) {
        printLog("updateChapter book：" + book + ",page：" + page + ",chapter：" + chapter);
        List<Book> books = currentUser.getBooks();
        for (Book bookitem : books) {
            if (bookitem.getBookNum() == book) {
                for (com.ragentek.exercisespaper.dao.models.Page pageitem : bookitem.getPages()) {
                    if (pageitem.getPageNumber() == page) {
                        printLog("updateChapter page：" + page);
                        pageitem.setChapter(chapter);
                        daoSession.getPageDao().update(pageitem);
                    }

                }
            }
        }
    }


    @SuppressLint("CheckResult")
    private void doSubmit() {
        final ArrayList<Answer> answers = getAnswers();
        if (answers.size() > 0) {
            final CustomLoadingDialog loading = new CustomLoadingDialog(this);
            loading.setMessage(R.string.loading_commit);
            loading.show();
            Observable<HomeworkItem> fileUpload = Observable
                    .create(new ObservableOnSubscribe<HomeworkItem>() {
                        @Override
                        public void subscribe(ObservableEmitter<HomeworkItem> emitter) throws Exception {
                            for (int i = 0; i < answers.size(); i++) {
                                final Answer ans = answers.get(i);
                                boolean imgGenerate = generateAnswerImage(ans.getStartRow(), ans.getEndRow(), ans.getImagePath());
                                if (imgGenerate) {
                                    File file = new File(ans.getImagePath());
                                    Log.d(TAG, "subscribe：" + file.toString());
                                    RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                                    MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
                                    Call<UplodeResponse> model = sumitAPI.uploadFile(part);

                                    Response<UplodeResponse> respons = model.execute();
                                    if (respons.isSuccessful()) {
                                        HomeworkItem item = new HomeworkItem();
                                        item.setChapter(ans.getChapter());
                                        item.setIndex1("一");
                                        item.setIndex2(ans.getIndex2() + "");
                                        if (respons.body() != null) {
                                            item.setUrl(respons.body().getUrl());
                                        }
                                        Log.d(TAG, "fileUpload  onNext: " + item.toString());
                                        emitter.onNext(item);
                                    }

                                } else {
                                    emitter.onError(new Exception("generate image fail path:" + ans.getImagePath()));
                                }
                            }
                            emitter.onComplete();

                        }
                    })
                    .subscribeOn(Schedulers.io());


            Observable<String> answerRecognition = Observable
                    .create(new ObservableOnSubscribe<String>() {
                        @Override
                        public void subscribe(final ObservableEmitter<String> emitter) throws Exception {
                            HwRealizeManager mHwRealizeManager = new HwRealizeManager(MainActivity.this, new HwRealizeManager.HwRealizeListener() {
                                @Override
                                public void onRealizeFormula(String data, String token) {

                                }

                                @Override
                                public void onRealizeSingle(String[] data, String token) {

                                }

                                @Override
                                public void onRealizeLine(String[] data, String token) {

                                    if (data != null && data.length > 0) {
                                        RealizeInfoHolder info = realizeMap.get(token);
                                        switch (info.getType()) {
                                            case RealizeInfoHolder.TYPE_ANSWER_OBJECT_CHOICE:
                                                String result = "unknown";
                                                for (String item : data) {
                                                    printLog("ANSWER Realize   item: " + item);

                                                    if (CommonUtil.isChoiceAnswer(item)) {
                                                        result = item;
                                                        break;
                                                    }
                                                }
                                                printLog("ANSWER Realize   result: " + result);
                                                emitter.onNext(result);
                                                break;
                                            case RealizeInfoHolder.TYPE_ANSWER_OBJECT_FILLING:
                                                emitter.onNext(data[0]);

                                                break;
                                            case RealizeInfoHolder.TYPE_ANSWER_OBJECT_SHORT_ANSWER:
                                                emitter.onNext(data[0]);

                                                break;
                                            default:
                                                Log.e(TAG, "ANSWER Realize  wrong type " + info.getType());

                                        }

                                    } else {
                                        emitter.onNext("none");
                                    }

                                }
                            });
                            for (final Answer answer : answers) {
                                ArrayList<SimpleDot> chapterDots = mPaperManager.readAnswerData(answer.getBook(), answer.getPage(), answer.getStartRow(), answer.getEndRow());
                                StringBuilder sb = new StringBuilder();
                                for (SimpleDot dot : chapterDots) {
                                    SimpleDot mewDot = mPaperManager.mapExercises2ScreenArea(dot, BaseApplication.getScreenWidth() - toolBar.getWidth(), BaseApplication.getScreenHeight());
                                    if (dot.x > 0) {
                                        sb.append(mewDot.x);
                                        sb.append(PaperManager.COORDINATE_SEPARATOR);
                                        sb.append(mewDot.y);
                                        sb.append(PaperManager.COORDINATE_SEPARATOR);

                                    } else if (CommonUtil.isFloatEquals(PageUtil.DOWN_ACTION, dot.x)) {
                                        sb.append("-1");
                                        sb.append(PaperManager.COORDINATE_SEPARATOR);
                                        sb.append("0");
                                        sb.append(PaperManager.COORDINATE_SEPARATOR);
                                    }
                                }
                                sb.append("-1");
                                sb.append(PaperManager.COORDINATE_SEPARATOR);
                                sb.append("0");
                                sb.append(PaperManager.COORDINATE_SEPARATOR);
                                Log.d(TAG, "result  : " + sb.toString());

                                String token = mHwRealizeManager.realizeLine(HwRealizeManager.LANG_EN, sb.toString());
                                RealizeInfoHolder info = new RealizeInfoHolder(answer.getBook(), answer.getPage(), answer.getIndex2(), RealizeInfoHolder.TYPE_ANSWER_OBJECT_CHOICE);
                                realizeMap.put(token, info);
                            }
                        }
                    })
                    .subscribeOn(Schedulers.io());

            Observable
                    .zip(fileUpload, answerRecognition, new BiFunction<HomeworkItem, String, HomeworkItem>() {

                        @Override
                        public HomeworkItem apply(HomeworkItem homeworkItem, String s) throws Exception {
                            Log.d(TAG, "zip apply  : " + homeworkItem.toString());
                            Log.d(TAG, "zip ocr  : " + s);
                            homeworkItem.setOcr(s);
                            return homeworkItem;
                        }
                    })
                    .toList()
                    .map(new Function<List<HomeworkItem>, Response<SubmitResponse>>() {
                        @Override
                        public Response<SubmitResponse> apply(List<HomeworkItem> homeworkItems) throws Exception {
                            final Homework hw = new Homework();
                            hw.setHomework(homeworkItems);
                            hw.setBookId(currentBook.getBookNum());
                            hw.setAccountId((int) BaseApplication.ACCOUNT_ID);
                            Log.d(TAG, "submitResult size：" + homeworkItems.size());
                            Call<SubmitResponse> call = sumitAPI.submitResult(hw);
                            return call.execute();
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Response<SubmitResponse>>() {
                        @Override
                        public void accept(Response<SubmitResponse> response) throws Exception {
                            if (response.isSuccessful()) {
                                mSubmitResponse = response.body();
                                Log.d(TAG, "submit success msg:" + response.body().toString());
                            } else {
                                Log.e(TAG, "submit fail  ");
                            }
                            loading.dismiss();
                            showResult();
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Log.e(TAG, "accept Exception  : " + throwable.toString());
                            loading.dismiss();
                        }
                    });
        } else {
            Log.e(TAG, " Toast.makeText");

            Toast.makeText(this, R.string.no_writing, Toast.LENGTH_SHORT).show();
        }

    }


    private ArrayList<Answer> getAnswers() {
        ArrayList<Answer> currentAnswers = new ArrayList<>();
        printLog("generateAnswers getPageNumber:" + currentPage.getPageNumber());
        List<Row> rows = currentPage.getRows();
        printLog("generateAnswers  all rows  size:" + rows.size());
        Collections.sort(rows, new Comparator<Row>() {
            @Override
            public int compare(Row o1, Row o2) {
                return o1.getRow() - o2.getRow();
            }
        });
        int questionIndex = -1;
        int lastQuestionIndex = -1;
        int startRow = -1;
        int endRow = -1;
        for (Row row : rows) {
            printLog("startRow:" + startRow + ",endRow:" + endRow);
            if (row.getIndex() > 0) {
                questionIndex = row.getIndex();
            }
            printLog("row:" + row.getRow() + ",id:" + row.getId() + ",lastQuestionIndex:" + lastQuestionIndex + ",questionIndex:" + questionIndex);
            if (lastQuestionIndex != questionIndex) {
                if (startRow != -1 && endRow >= startRow) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(BaseApplication.EXERCISE_SAVE_PATH)
                            .append(File.separator)
                            .append(currentBook.getBookNum())
                            .append(File.separator)
                            .append(currentPage.getPageNumber())
                            .append(File.separator)
                            .append(lastQuestionIndex)
                            .append(BaseApplication.IMAGE_SAVE_FORMAT_JPG);

                    Answer anser = new Answer();
                    anser.setStartRow(startRow);
                    anser.setPage(currentPage.getPageNumber());
                    anser.setEndRow(endRow);
                    anser.setBook(currentBook.getBookNum());
                    anser.setImagePath(sb.toString());
                    anser.setChapter(currentPage.getChapter());
                    anser.setIndex1("一");
                    anser.setIndex2(lastQuestionIndex);
                    printLog("add Answers :" + anser.toString());
                    currentAnswers.add(anser);
                }
                startRow = row.getRow();
                endRow = row.getRow();
                lastQuestionIndex = questionIndex;
            } else {
                endRow = row.getRow();
            }
        }
        printLog("startRow:" + startRow + ",endRow:" + endRow);
        if (rows.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(BaseApplication.EXERCISE_SAVE_PATH)
                    .append(File.separator)
                    .append(currentBook.getBookNum())
                    .append(File.separator)
                    .append(currentPage.getPageNumber())
                    .append(File.separator)
                    .append(questionIndex)
                    .append(BaseApplication.IMAGE_SAVE_FORMAT_JPG);

            Answer anser = new Answer();
            anser.setBook(currentBook.getBookNum());
            anser.setPage(currentPage.getPageNumber());
            anser.setStartRow(startRow);
            anser.setEndRow(endRow);
            anser.setImagePath(sb.toString());
            anser.setChapter(currentPage.getChapter());
            anser.setIndex2(questionIndex);
            currentAnswers.add(anser);
            printLog("add Answers :" + anser.toString());
        }
        Log.d(TAG, "getAnswers result  size: " + currentAnswers.size());

        return currentAnswers;
    }

    private ServiceConnection mPenManagerConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            stateTv.setText("ready");
            penHandle = (PenManagerService.PenManager) service;
            penHandle.setPenListener(penListener);
            updateConnectUI(penHandle.isConnected());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            stateTv.setText(R.string.app_unready);
        }
    };


    private void updateConnectUI(boolean isConn) {
        deviceImage.setImageResource(isConn ? R.drawable.device_on_icon : R.drawable.device_off_icon);
        stateTv.setText(isConn ? R.string.connected : R.string.unconnected);
    }


    private void showDevicesList() {
        if (mListDeviceDialog == null) {
            mListDeviceDialog = new ListDeviceDialog.Builder()
                    .setNegativeButton(getResources().getString(R.string.cancel))
                    .setPositiveButton(getResources().getString(R.string.scan))
                    .setItemClickListener(new ListDeviceDialog.OnClickListener() {
                        @Override
                        public void onClick(int position) {
                            switch (position) {
                                case ListDeviceDialog.Builder.ID_NEGATIVE_BUTTON:
                                    penHandle.stopScan();
                                    mListDeviceDialog.dismiss();
                                    break;
                                case ListDeviceDialog.Builder.ID_POSITIVE_BUTTON:
                                    mListDeviceDialog.refresh(false);
                                    penHandle.startScan();
                                    break;
                                default:
                                    penHandle.connect(deviceList.get(position).getAddress());
                                    Log.d(TAG, "position:" + position);
                                    mListDeviceDialog.setSellected(deviceList.get(position).getName());
                                    mListDeviceDialog.dismiss();
                            }
                        }
                    })
                    .create(this);

        } else {
            mListDeviceDialog.refresh(false);
        }
        mListDeviceDialog.show();

    }


    PenManagerService.PenListener penListener = new PenManagerService.PenListener() {
        @Override
        public void onReceivePenStatus(int battery, int usedmem) {
            printLog("onReceivePenStatus");
        }

        @Override
        public void onDeviceFind(final BluetoothDevice results) {
            printLog("onDeviceFind");
            if (deviceList.contains(results)) {
                return;
            }
            deviceList.add(results);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mListDeviceDialog.addData(results.getName());
                    mListDeviceDialog.stopRefresh();
                }
            });
        }

        @Override
        public void onDisConnected(final String penAddress) {
            printLog("onDisConnected");
            removeState(STATE_CONNECTED);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateConnectUI(false);
                }

            });
        }

        @Override
        public void onError(String msg) {
            printLog("onError" + deviceList.size());
            if (deviceList.size() < 1) {
                mListDeviceDialog.showMessage(getResources().getString(R.string.no_device));
            } else {
                mListDeviceDialog.stopRefresh();
            }
        }

        @Override
        public void onConnected(String penAddress) {
            addState(STATE_CONNECTED);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateConnectUI(true);
                }
            });
        }


        @Override
        public void onReceiveDot(PenDot dot) {
            switch (dot.getType()) {
                case PenDot.TYPE_DOWN:
                    printLog(" TYPE_DOWN ");
                    tempDots.clear();
                    int bookNum = dot.getBookID();
                    addState(STATE_BOOK_SELLECT);
                    currentAreaType = mPageUtils.getAreaType(dot.getX(), dot.getY());

                    newArea = !CommonUtil.isFloatEquals(currentAreaType, lastAreaType);

                    if (lastBook == null || bookNum != lastBook.getBookNum()) {
                        currentBook = bookSaved(currentUser, bookNum);
                        if (currentBook == null) {
                            currentBook = new Book();
                            currentBook.setBookNum(bookNum);
                            currentBook.setUserId(currentUser.getAccountId());
                            currentBook.setName("fxy");
                            daoSession.getBookDao().insert(currentBook);
                            currentUser.resetBooks();
                        }
                    }
                    int pageNumber = dot.getPageID();
                    if (lastPage == null || pageNumber != lastPage.getPageNumber()) {
                        currentPage = pageSaved(currentBook, pageNumber);
                        printLog(" pageNumber=" + pageNumber);
                        if (currentPage == null) {
                            currentPage = new com.ragentek.exercisespaper.dao.models.Page();
                            currentPage.setBookId(currentBook.getId());
                            currentPage.setPageNumber(pageNumber);
                            currentPage.setChapter("none");
                            daoSession.getPageDao().insert(currentPage);
                            currentBook.resetPages();
                        }
                        newPage = true;
                    } else {
                        newPage = false;
                    }
                    if (newArea && CommonUtil.isFloatEquals(PageUtil.AREA_TYPE_CHAPTER, lastAreaType)) {
                        doChapterRealize(currentBook.getBookNum(), currentPage.getPageNumber());
                    }
                    int currentRowNum = mPageUtils.getRow(dot.getY());

//                       PageUtil.java
//                       typological point  ,
//                     public static final float DOWN_ACTION = -1.0f;
//                     public static final float AREA_TYPE_CHAPTER = -2.0f;
//                     public static final float AREA_TYPE_INDEX = -3.0f;
//                     public static final float AREA_TYPE_CONTENT = -4.0f;
//                     public static final float AREA_TYPE_UNKNOW = -5.0f;

                    tempDots.add(new SimpleDot(PageUtil.DOWN_ACTION, PageUtil.DOWN_ACTION, pageNumber));
                    tempDots.add(new SimpleDot(currentAreaType, currentRowNum, pageNumber));
                    tempDots.add(new SimpleDot(dot.getX(), dot.getY(), pageNumber));
                    if (lastRow == null || currentRowNum != lastRow.getRow()) {
                        currentRow = hasSavedRow(currentPage, currentRowNum);
                        if (currentRow == null) {
                            currentRow = new Row();
                            currentRow.setPageId(currentPage.getId());
                            currentRow.setRow(currentRowNum);
                            daoSession.getRowDao().insert(currentRow);
                            currentPage.resetRows();
                        }
                        newRow = true;
                    } else {
                        newRow = false;
                    }

                    if (lastRow != null) {
                        if ((newRow || newArea) && CommonUtil.isFloatEquals(PageUtil.AREA_TYPE_INDEX, lastAreaType)) {
                            doIndexRealize(currentBook.getBookNum(), currentPage.getPageNumber(), lastRow.getRow());
                        }
                    }
                    if (newRow) {
                        lastRow = currentRow;
                        clearIndex = 0;

                    }
                    if (newArea) {
                        clearIndex = 0;

                    }
                    if (newPage) {
                        lastPage = currentPage;
                    }

                    lastBook = currentBook;
                    printLog(" newPage:" + newPage + ".newPage:" + newArea + ",newRow:" + newRow);

                    break;
                case PenDot.TYPE_MOVE:
                    tempDots.add(new SimpleDot(dot.getX(), dot.getY(), currentPage.getPageNumber()));
                    break;
                case PenDot.TYPE_UP:
                    if (tempDots == null || tempDots.size() < 1) {
                        return;
                    }
                    final int pageId = tempDots.get(0).pageId;
                    if (pageId < 0 || pageId > 255) {
                        Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Observable.create(new ObservableOnSubscribe<ArrayList<SimpleDot>>() {
                        @Override
                        public void subscribe(ObservableEmitter<ArrayList<SimpleDot>> emitter) throws Exception {
                            ArrayList<SimpleDot> currentDots = new ArrayList<>(tempDots);

                            if (isClearAction(currentDots)) {
                                Log.d(TAG, "cleaChapterrData");
                                clearIndex = 0;
                                if (CommonUtil.isFloatEquals(PageUtil.AREA_TYPE_CHAPTER, currentAreaType)) {
                                    mPaperManager.cleaChapterrData(currentBook.getBookNum(), pageId);
                                }
//                                mPaperManager.readAnswerData()
                            } else {
                                mPaperManager.savePage2File(currentDots, currentBook.getBookNum(), pageId, true);

                            }
                            mPaperManager.savePage2File(currentDots, currentBook.getBookNum(), pageId, false);

                            Log.d(TAG, "writeDots2File end " + newPage);
                            if (newPage) {
                                currentDots = mPaperManager.readPageData(currentBook.getBookNum(), pageId);
                            }
                            ArrayList<SimpleDot> resizedSimpleDot = new ArrayList<>();
                            boolean isLandScreen = BaseApplication.getScreenWidth() > BaseApplication.getScreenHeight();
                            for (SimpleDot dot : currentDots) {
                                //
                                if (isLandScreen) {
                                    SimpleDot mewDot = mPaperManager.mapExercises2ScreenArea(dot, BaseApplication.getScreenWidth() - toolBar.getWidth(), BaseApplication.getScreenHeight());
                                    resizedSimpleDot.add(mewDot);
                                } else {
                                    SimpleDot mewDot = mPaperManager.mapExercises2ScreenArea(dot, BaseApplication.getScreenWidth(), BaseApplication.getScreenHeight() - toolBar.getHeight());
                                    resizedSimpleDot.add(mewDot);
                                }

                            }
                            answerAre.loadAnwserArea(resizedSimpleDot, newPage);
                            //  mPaperManager.generateAllSquareImage();//test generate image
                            emitter.onNext(resizedSimpleDot);
                        }
                    })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<ArrayList<SimpleDot>>() {
                                @Override
                                public void accept(ArrayList<SimpleDot> resizedSimpleDot) throws Exception {
                                    if (resizedSimpleDot == null) {
                                        Toast.makeText(MainActivity.this, "null error", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    printLog("  pageId:" + pageId);
                                    //                                    currentPage = pageId;
                                    lastAreaType = currentAreaType;
                                    pageTv.setText(String.format(getResources().getString(R.string.page), pageId + 1));
                                    bookTv.setText(String.format(getResources().getString(R.string.book), currentBook.getBookNum() + 1));
                                    rowTv.setText(String.format(getResources().getString(R.string.row), currentRow.getRow() + 1));
                                    indexTv.setText(String.format(getResources().getString(R.string.question), currentRow.getIndex()));
                                    chapterTv.setText(String.format(getResources().getString(R.string.chapter), currentPage.getChapter()));
                                    clearImage.setClickable(true);
                                    resultImage.setClickable(true);

                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                                    printLog("  error:" + throwable.getMessage());

                                }
                            });
                    break;
            }
        }
    };


    private Book bookSaved(User user, int bookNum) {

        if (user.getBooks() != null && user.getBooks().size() > 0) {
            for (Book book : user.getBooks()) {
                if (book.getBookNum() == bookNum) {
                    return book;
                }
            }
        }
        return null;
    }

    private com.ragentek.exercisespaper.dao.models.Page pageSaved(Book book, int pageNum) {
        if (book.getPages() != null && book.getPages().size() > 0) {
            for (com.ragentek.exercisespaper.dao.models.Page page : book.getPages()) {
                if (page.getPageNumber() == pageNum) {
                    return page;
                }
            }
        }
        return null;
    }

    private Row hasSavedRow(com.ragentek.exercisespaper.dao.models.Page page, int rowNum) {
        if (page.getRows() != null && page.getRows().size() > 0) {
            for (Row row : page.getRows()) {
                if (row.getRow() == rowNum) {
                    return row;
                }
            }
        }
        return null;
    }

    private void doChapterRealize(int bookid, int pageid) {
        Log.d(TAG, "doChapterRealize bookid:" + bookid + ",pageid:" + pageid);
        ArrayList<SimpleDot> chapterDots = mPaperManager.readChapter(bookid, pageid);
        StringBuilder sb = new StringBuilder();
        for (SimpleDot dot : chapterDots) {
            SimpleDot mewDot = mPaperManager.mapExercises2ScreenArea(dot, BaseApplication.getScreenWidth(), BaseApplication.getScreenHeight() - toolBar.getHeight());

            if (dot.x > 0) {
                sb.append(mewDot.x);
                sb.append(PaperManager.COORDINATE_SEPARATOR);
                sb.append(mewDot.y);
                sb.append(PaperManager.COORDINATE_SEPARATOR);

            } else if (CommonUtil.isFloatEquals(PageUtil.DOWN_ACTION, dot.x)) {
                sb.append("-1");
                sb.append(PaperManager.COORDINATE_SEPARATOR);
                sb.append("0");
                sb.append(PaperManager.COORDINATE_SEPARATOR);
            }
        }
        Log.d(TAG, "result  : " + sb.toString());
        sb.append("-1");
        sb.append(PaperManager.COORDINATE_SEPARATOR);
        sb.append("0");
        sb.append(PaperManager.COORDINATE_SEPARATOR);
//        String token = mHwRealizeManager.realizeFormula(sb.toString());
//        String token = mHwRealizeManager.realizeSingle(HwRealizeManager.LANG_CHNS, sb.toString());
        String token = mHwRealizeManager.realizeLine(HwRealizeManager.LANG_CHNS, sb.toString());
//        String token = mHwRealizeManager.realizeFormula(sb.toString());


        RealizeInfoHolder info = new RealizeInfoHolder(bookid, pageid, -1, RealizeInfoHolder.TYPE_AREA_CHAPTER);
        realizeMap.put(token, info);

    }

    private void doIndexRealize(int bookid, int pageid, int row) {
        Log.d(TAG, "doIndexRealize bookid:" + bookid + ",pageid:" + pageid + ",row:" + row);
        ArrayList<SimpleDot> indexDots = mPaperManager.readIndexData(bookid, pageid, row);
        StringBuilder sb = new StringBuilder();
        for (SimpleDot dot : indexDots) {
            SimpleDot mewDot = mPaperManager.mapExercises2ScreenArea(dot, BaseApplication.getScreenWidth(), BaseApplication.getScreenHeight() - toolBar.getHeight());

            if (dot.x > 0) {
                sb.append(mewDot.x);
                sb.append(PaperManager.COORDINATE_SEPARATOR);
                sb.append(mewDot.y);
                sb.append(PaperManager.COORDINATE_SEPARATOR);

            } else if (CommonUtil.isFloatEquals(PageUtil.DOWN_ACTION, dot.x)) {
                sb.append("-1");
                sb.append(PaperManager.COORDINATE_SEPARATOR);
                sb.append("0");
                sb.append(PaperManager.COORDINATE_SEPARATOR);
            }
        }
        Log.d(TAG, "result  : " + sb.toString());
        sb.append("-1");
        sb.append(PaperManager.COORDINATE_SEPARATOR);
        sb.append("0");
        sb.append(PaperManager.COORDINATE_SEPARATOR);
//        String token = mHwRealizeManager.realizeFormula(sb.toString());
//        String token = mHwRealizeManager.realizeSingle(HwRealizeManager.LANG_CHNS, sb.toString());
        String token = mHwRealizeManager.realizeLine(HwRealizeManager.LANG_CHNS, sb.toString());
        RealizeInfoHolder info = new RealizeInfoHolder(bookid, pageid, row, RealizeInfoHolder.TYPE_AREA_INDEX);
        realizeMap.put(token, info);
    }


    private boolean generateAnswerImage(int startRow, int endRow, String imgFile) {
        printLog("generateAnswerImage  startRow:" + startRow + ",endRow:" + endRow + ",File:" + imgFile);
        ArrayList<SimpleDot> datas = mPaperManager.readAnswerData(currentBook.getBookNum(), currentPage.getPageNumber(), startRow, endRow);
        printLog("generateAnswerImage :" + datas.size());

        float xOffset = 0f;
        float yOffset = mPageUtils.getRowHeight() * (startRow - 1) + mPageUtils.getQuestionContextArea()[1];
        printLog("generateAnswerImage  xOffset:" + xOffset + ",yOffset:" + yOffset);
        ArrayList<SimpleDot> resizeddatas = new ArrayList<>();
        for (SimpleDot dot : datas) {
            if (dot.x > 0) {
                resizeddatas.add(
                        mPaperManager.mapExercises2ScreenArea(dot, BaseApplication.getScreenWidth() - toolBar.getWidth(), BaseApplication.getScreenHeight(), xOffset, yOffset)
                );
            } else {
                resizeddatas.add(dot);
            }
        }
        return mPaperManager.generateSquareImage(resizeddatas, imgFile);

//        return mPaperManager.generateQuesionImage(resizeddatas, BaseApplication.getScreenWidth(), (BaseApplication.getScreenHeight() - toolBar.getHeight()) / mPageUtils.getRowConut() * (endRow - startRow + 1), imgFile, false);
    }


    private void printLog(String message) {
        Log.d(TAG, message);
    }


    //TODO
    private int clearIndex = 0;
    private int CLEAR_LINE_NUM = 2;

    private boolean isClearAction(ArrayList<SimpleDot> dots) {
        if (mPaperManager.isStraight(dots)) {
            printLog("clearIndex++  ");
            clearIndex++;
        } else {
            printLog(" clearIndex = 0");
            clearIndex = 0;
        }
        printLog("isClearAction clearIndex:" + clearIndex);
        return clearIndex >= CLEAR_LINE_NUM;
    }
}
