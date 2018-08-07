package com.ragentek.factorypaper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.os.Bundle;
import android.os.IBinder;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lastark.smartboard.control.HwRealizeManager;
import com.ragentek.factorypaper.net.HttpRetrofitManager;
import com.ragentek.factorypaper.net.api.SubmitAPI;
import com.ragentek.factorypaper.net.models.ResponseBaseMsg;
import com.ragentek.factorypaper.net.models.SubmitResponse;
import com.ragentek.factorypaper.net.models.Table;
import com.ragentek.factorypaper.net.models.TableItem;
import com.ragentek.factorypaper.net.models.UplodeResponse;
import com.ragentek.factorypaper.paper.Page;
import com.ragentek.factorypaper.paper.PaperManager;
import com.ragentek.factorypaper.paper.models.SimpleDot;
import com.ragentek.factorypaper.paper.models.GridItem;
import com.ragentek.factorypaper.utils.CommonUtil;
import com.ragentek.factorypaper.view.HandwritingSurfaceView;
import com.ragentek.factorypaper.view.widgets.CustomAlertDialog;
import com.ragentek.factorypaper.view.widgets.CustomLoadingDialog;
import com.ragentek.factorypaper.view.widgets.ListDeviceDialog;
import com.ragentek.penmanager.PenManagerService;
import com.ragentek.penmanager.model.PenDot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
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
    private static final String TAG = "MainActivity";

    @BindView(R.id.iv_submit)
    ImageView submit;
    @BindView(R.id.tv_state)
    TextView stateTv;
    @BindView(R.id.writing_area)
    HandwritingSurfaceView writingAre;
    @BindView(R.id.iv_devices)
    ImageView deviceImage;
    @BindView(R.id.iv_result)
    ImageView resultImage;
    @BindView(R.id.toolBar)
    RelativeLayout toolBar;

    @BindView(R.id.tv_book)
    TextView bookTv;
    @BindView(R.id.tv_page)
    TextView pageTv;
    @BindView(R.id.tv_row)
    TextView rowTv;
    @BindView(R.id.tv_column)
    TextView columnTv;

    private ListDeviceDialog mListDeviceDialog;
    private ArrayMap<String, Integer> permisions;
    private static final int STATE_INITIALIZE = 0x000;
    private static final int STATE_LOGIN = 0x001;
    private static final int STATE_CONNECTED = 0x010;
    private static final int STATE_BOOK_SELLECT = 0x100;
    private static final int STATE_WELL = STATE_LOGIN | STATE_CONNECTED | STATE_BOOK_SELLECT;
    private int currentState = STATE_INITIALIZE;

    private ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
    private volatile ArrayList<SimpleDot> tempDots = new ArrayList<>();
    private GridItem currentTableItem;
    private GridItem lastTableItem;

    private PenManagerService.PenManager penHandle;
    private PaperManager mPaperManager;
    private SubmitAPI mSubmitAPI;
    private SubmitAPI mUploadAPI;

    //TODO
    private int clearIndex = 0;
    private int CLEAR_LINE_NUM = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
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

    @Override
    void initModules() {
        mPaperManager = PaperManager.getInstance();
        mSubmitAPI = HttpRetrofitManager.getInstance().setBaseUrl(BaseApplication.SUBMUIT_BASEURL).creatHttpApi(SubmitAPI.class);
        mUploadAPI = HttpRetrofitManager.getInstance().setBaseUrl(BaseApplication.FILE_UPLOAD_BASEURL).creatHttpApi(SubmitAPI.class);
        Intent serviceIntent = new Intent(this, PenManagerService.class);
        serviceIntent.setPackage(getPackageName());
        startService(serviceIntent);
        bindService(serviceIntent, mPenManagerConnection, Context.BIND_AUTO_CREATE);
        mPaperManager = PaperManager.getInstance();
        addState(STATE_LOGIN);
    }

    private void updateConnectState(boolean isConn) {
        if (isConn) {
            deviceImage.setImageResource(R.drawable.device_on_icon);
            stateTv.setText(R.string.connected);
            addState(STATE_CONNECTED);
        } else {
            deviceImage.setImageResource(R.drawable.device_off_icon);
            stateTv.setText(R.string.unconnected);
            removeState(STATE_CONNECTED);
        }
    }

    private ServiceConnection mPenManagerConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            stateTv.setText("ready");
            penHandle = (PenManagerService.PenManager) service;
            penHandle.setPenListener(penListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            stateTv.setText(R.string.app_unready);
        }
    };
    PenManagerService.PenListener penListener = new PenManagerService.PenListener() {
        @Override
        public void onReceivePenStatus(int battery, int usedmem) {

        }

        @Override
        public void onDeviceFind(final BluetoothDevice results) {
            printLog("onDeviceFind:" + results.getName());
            if (deviceList.contains(results)) {
                return;
            }

            deviceList.add(results);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mListDeviceDialog.addData(results.getName() + ":" + results.getAddress());
                    mListDeviceDialog.stopRefresh();
                }
            });
        }

        @Override
        public void onDisConnected(String penAddress) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateConnectState(false);
                }

            });

        }

        @Override
        public void onError(String msg) {
            if (deviceList.size() < 1) {
                mListDeviceDialog.showMessage(getResources().getString(R.string.no_device));
            } else {
                mListDeviceDialog.stopRefresh();
            }
        }

        @Override
        public void onConnected(String penAddress) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateConnectState(true);
                }
            });
        }

        boolean isOutOfTable = false;

        @Override
        public void onReceiveDot(final PenDot dot) {
            switch (dot.getType()) {
                case PenDot.TYPE_DOWN:
                    tempDots.clear();
                    int currentBookNum = dot.getBookID();
                    int currentPageID = dot.getPageID();
                    printLog(" TYPE_DOWN  currentPageID" + currentPageID);

                    addState(STATE_BOOK_SELLECT);
                    GridItem tableitem = mPaperManager.getTableItem(dot.getX(), dot.getY(), currentPageID, currentBookNum);

                    isOutOfTable = tableitem == null;

                    if (isOutOfTable) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(MainActivity.this);
                                builder.setMessage(R.string.out_of_table_tips)
                                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                            }
                                        });
                                builder.create().show();
                            }
                        });
                    } else {
                        currentTableItem = tableitem;
                        printLog(" TYPE_DOWN  item" + currentTableItem.toString());
                        tempDots.add(new SimpleDot(Page.DOWN_ACTION, Page.DOWN_ACTION, currentPageID, currentBookNum));
                        tempDots.add(new SimpleDot(dot.getX(), dot.getY(), currentPageID, currentBookNum));

                        boolean isNewArea = false;
                        printLog(" TYPE_DOWN  isNewArea " + isNewArea);

                        if (lastTableItem == null
                                || lastTableItem.getBook() != currentTableItem.getBook()
                                || lastTableItem.getPage() != currentTableItem.getPage()
                                || lastTableItem.getColumnLocation() != currentTableItem.getColumnLocation()
                                || lastTableItem.getRowLocation() != currentTableItem.getRowLocation()) {

                            isNewArea = true;


                            if (isNewArea) {
                                lastTableItem = currentTableItem;
                                ArrayList<SimpleDot> savedDots = mPaperManager.readTableItemData(currentTableItem.getBook(),
                                        currentTableItem.getPage(),
                                        currentTableItem.getRowLocation(),
                                        currentTableItem.getColumnLocation());
                                final ArrayList<Point> resizedSimpleDot = new ArrayList<>();
                                if (savedDots != null && savedDots.size() > 0) {
                                    for (SimpleDot item : savedDots) {
                                        SimpleDot newDot = mPaperManager.mapExercises2ScreenArea(item, BaseApplication.getScreenWidth() - toolBar.getWidth(), BaseApplication.getScreenHeight(), currentTableItem);
                                        resizedSimpleDot.add(new Point((int) newDot.x, (int) newDot.y));
                                    }
                                }
                                writingAre.loadAnwserArea(resizedSimpleDot, true);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        pageTv.setText(String.format(getResources().getString(R.string.page), currentTableItem.getPage() + 1));
                                        bookTv.setText(String.format(getResources().getString(R.string.book), currentTableItem.getBook() + 1));
                                        rowTv.setText(String.format(getResources().getString(R.string.row), currentTableItem.getRowLocation() + 1));
                                        columnTv.setText(String.format(getResources().getString(R.string.column), currentTableItem.getColumnLocation() + 1));
                                    }
                                });
                            }
                        }
                        SimpleDot currentDot = new SimpleDot(dot.getX(), dot.getY(), currentTableItem.getPage(), currentTableItem.getBook());

                        SimpleDot screenAreaDot = mPaperManager.mapExercises2ScreenArea(currentDot, BaseApplication.getScreenWidth() - toolBar.getWidth(), BaseApplication.getScreenHeight(), currentTableItem);

                        writingAre.drawPoint(new Point((int) screenAreaDot.x, (int) screenAreaDot.y), false, true);
                    }


                    break;
                case PenDot.TYPE_MOVE:

                    if (isOutOfTable) break;
                    SimpleDot currentDot = new SimpleDot(dot.getX(), dot.getY(), currentTableItem.getPage(), currentTableItem.getBook());

                    SimpleDot screenAreaDot = mPaperManager.mapExercises2ScreenArea(currentDot, BaseApplication.getScreenWidth() - toolBar.getWidth(), BaseApplication.getScreenHeight(), currentTableItem);

                    writingAre.drawPoint(new Point((int) screenAreaDot.x, (int) screenAreaDot.y), false, false);
                    tempDots.add(currentDot);
                    break;
                case PenDot.TYPE_UP:
                    if (isOutOfTable) break;

                    if (tempDots == null || tempDots.size() < 1) {
                        return;
                    }
                    tempDots.add(new SimpleDot(dot.getX(), dot.getY(), currentTableItem.getPage(), currentTableItem.getBook()));
//                    final ArrayList<Point> resizedSimpleDot = new ArrayList<>();
//                    printLog("TYPE_UP :" + tempDots.size());
//                    for (SimpleDot item : tempDots) {
//                        SimpleDot newDot = mPaperManager.mapExercises2ScreenArea(item, BaseApplication.getScreenWidth() - toolBar.getWidth(), BaseApplication.getScreenHeight(), currentTableItem);
//                        resizedSimpleDot.add(new Point((int) newDot.x, (int) newDot.y));
//                    }
//                    writingAre.loadAnwserArea(resizedSimpleDot, false);
                    mPaperManager.saveDots2File(tempDots, currentTableItem.getBook(), currentTableItem.getPage(), true);

                    break;
            }
        }
    };


    @Override
    void initView() {
        stateTv.setText(String.format(getResources().getString(R.string.state), -1));
        pageTv.setText(String.format(getResources().getString(R.string.page), -1));
        bookTv.setText(String.format(getResources().getString(R.string.book), -1));
        rowTv.setText(String.format(getResources().getString(R.string.row), -1));
        columnTv.setText(String.format(getResources().getString(R.string.column), -1));

    }


    @OnClick(R.id.iv_clear)
    void clearHistory() {
        if (currentTableItem != null) {
            CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(this);
            builder.setMessage(R.string.clear_tips)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            realizeMap.clear();
                            writingAre.clear();
                            mPaperManager.clear(currentTableItem.getBook(), currentTableItem.getPage());
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

    }

    @OnClick(R.id.iv_devices)
    void scannerDevices() {
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
        penHandle.startScan();
    }

    ArrayMap<String, GridItem> realizeMap = new ArrayMap<>();
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();


    @SuppressLint("CheckResult")
    @OnClick(R.id.iv_submit)
    void doSubmit() {
        printLog("doSubmit start:");
        final CustomLoadingDialog loading = new CustomLoadingDialog(this);
        loading.setMessage(R.string.loading_commit);
        loading.show();
        final ArrayList<GridItem> gridItems = mPaperManager.getTableItems(currentTableItem.getBook(), currentTableItem.getPage());
        if (gridItems != null && gridItems.size() > 0) {
            Collections.sort(gridItems, new Comparator<GridItem>() {
                @Override
                public int compare(GridItem o1, GridItem o2) {
                    return o1.getRowLocation() - o2.getRowLocation();
                }
            });
            printLog("doSubmit fileUpload tableItems:" + gridItems.size());
            for (int i = 0; i < gridItems.size(); i++) {
                final GridItem gridItem = gridItems.get(i);
                ArrayList<SimpleDot> rawdatas = mPaperManager.readTableItemData(
                        gridItem.getBook(),
                        gridItem.getPage(),
                        gridItem.getRowLocation(),
                        gridItem.getColumnLocation());
                printLog(" doSubmit fileUpload rawdatas:" + rawdatas.size());
                ArrayList<SimpleDot> resizeddatas = new ArrayList<>();
                for (SimpleDot dot : rawdatas) {
                    if (dot.x > 0) {
                        SimpleDot newDot = mPaperManager.mapExercises2ScreenArea(dot,
                                ((BaseApplication.getScreenWidth() - toolBar.getWidth()) / mPaperManager.getColumnCount()) * gridItem.getWidthCount(),
                                (BaseApplication.getScreenHeight() / mPaperManager.getRowCount()) * gridItem.getHeightCount(),
                                gridItem);
                        resizeddatas.add(newDot);
                    } else {
                        resizeddatas.add(dot);
                    }
                }
                gridItem.setDots(resizeddatas);
            }

            Observable<GridItem> imgGenerate = Observable
                    .create(new ObservableOnSubscribe<GridItem>() {
                        @Override
                        public void subscribe(ObservableEmitter<GridItem> emitter) throws Exception {
                            printLog("doSubmit fileUpload tableItems:" + gridItems.size());
                            for (int i = 0; i < gridItems.size(); i++) {
                                final GridItem gridItem = gridItems.get(i);

                                String filePath = gridItem.getDotsPath() + BaseApplication.IMAGE_SAVE_FORMAT_JPG;
                                boolean imgGenerate = mPaperManager.generateQuesionImage(
                                        gridItem.getDots(),
                                        ((BaseApplication.getScreenWidth() - toolBar.getWidth()) / mPaperManager.getColumnCount()) * gridItem.getWidthCount(),
                                        (BaseApplication.getScreenHeight() / mPaperManager.getRowCount()) * gridItem.getHeightCount(),
                                        filePath,
                                        false);
                                printLog("doSubmit  imgGenerate setImagePath:" + gridItem.getDotsPath() + BaseApplication.IMAGE_SAVE_FORMAT_JPG);
                                if (imgGenerate) {
                                    gridItem.setImagePath(filePath);
                                    File file = new File(filePath);
                                    RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                                    MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
                                    Call<UplodeResponse> model = mUploadAPI.uploadFile(part);

                                    Response<UplodeResponse> respons = model.execute();
                                    String url = null;
                                    if (respons.isSuccessful()) {
                                        if (respons.body() != null) {
                                            url = respons.body().getUrl();
                                        }
                                    }
                                    if (url != null) {
                                        gridItem.setImageUrl(url);

                                    } else {
                                        emitter.onError(new Exception("upload image fail  :"));
                                    }
                                } else {
                                    emitter.onError(new Exception("generate image fail path:"));
                                    return;
                                }
                                emitter.onNext(gridItem);

                            }
                            emitter.onComplete();
                        }
                    })
                    .subscribeOn(Schedulers.io());


            Observable realizeObservable = Observable
                    .create(new ObservableOnSubscribe<GridItem>() {
                        @Override
                        public void subscribe(final ObservableEmitter<GridItem> emitter) throws Exception {
                            printLog("doSubmit realize tableItems:" + gridItems.size());
                            HwRealizeManager mHwRealizeManager = new HwRealizeManager(MainActivity.this, new HwRealizeManager.HwRealizeListener() {

                                @Override
                                public void onRealizeFormula(String data, String token) {

                                }

                                @Override
                                public void onRealizeSingle(String[] data, String token) {

                                }

                                @Override
                                public void onRealizeLine(String[] data, String token) {
                                    Log.d(TAG, "doSubmit onRealizeLine");
                                    GridItem griditem = realizeMap.get(token);
                                    if (data != null && data.length > 0) {
                                        switch (griditem.getType()) {
                                            case GridItem.TYPE_NUMBER:
                                                for (String str : data) {
                                                    printLog("Realize TYPE_NUMBER   str: " + str);
                                                    if (CommonUtil.isNumber(str)) {
                                                        griditem.setContent(str);
                                                        break;
                                                    }
                                                }
                                                break;
                                            case GridItem.TYPE_CHINNESE:
                                                for (String str : data) {
                                                    printLog("Realize TYPE_CHINNESE   str: " + str);
                                                }
                                                griditem.setContent(data[0]);
                                                break;
                                            case GridItem.TYPE_ENG:
                                                for (String str : data) {
                                                    printLog("Realize TYPE_ENG   str: " + str);

                                                }

                                                griditem.setContent(data[0]);
                                                break;
                                            case GridItem.TYPE_DATE:
                                                for (String str : data) {
                                                    printLog("Realize TYPE_DATE   str: " + str);
                                                    if (CommonUtil.isDate(str)) {
                                                        griditem.setContent(str);
                                                        break;
                                                    }
                                                }

                                                break;

                                            default:
                                                Log.e(TAG, "ANSWER Realize  wrong type " + griditem.getType());
                                        }
                                    }
                                    emitter.onNext(griditem);
                                }
                            });


                            for (int i = 0; i < gridItems.size(); i++) {
                                final GridItem item = gridItems.get(i);
                                StringBuilder sb = new StringBuilder();
                                for (SimpleDot dot : item.getDots()) {
                                    if (dot.x > 0) {
                                        sb.append(dot.x);
                                        sb.append(PaperManager.COORDINATE_SEPARATOR);
                                        sb.append(dot.y);
                                        sb.append(PaperManager.COORDINATE_SEPARATOR);
                                    } else if (CommonUtil.isFloatEquals(Page.DOWN_ACTION, dot.x)) {
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
                                String token = mHwRealizeManager.realizeLine(HwRealizeManager.LANG_CHNS, sb.toString());
                                realizeMap.put(token, item);
                            }
                        }
                    })
                    .subscribeOn(Schedulers.io());


            Observable
                    .zip(realizeObservable, imgGenerate, new BiFunction<GridItem, GridItem, GridItem>() {
                        @Override
                        public GridItem apply(GridItem tableItem1, GridItem tableItem2) throws Exception {
                            return tableItem1;
                        }
                    })
                    .toList()
                    .map(new Function<List<GridItem>, Response<ResponseBaseMsg>>() {

                        @Override
                        public Response<ResponseBaseMsg> apply(List<GridItem> tems) throws Exception {
                            Table tab = new Table();

                            tab.setPageId(currentTableItem.getPage());
                            tab.setBookId(currentTableItem.getBook());
                            List<TableItem> items = new ArrayList<>();
                            tab.setItems(items);
//                            for (int i = 0; i < mPaperManager.getRowCount(); i++) {
//                                for (int j = 0; j < mPaperManager.getColumnCount(); j++) {
//                                    TableItem item = new TableItem();
//                                    item.setRowIndex(i);
//                                    item.setColumnIndex(j);
//                                    for (int n = 0; n < gridItems.size(); n++) {
//                                        GridItem grid = gridItems.get(n);
//                                        if (grid.getColumnLocation() == j && grid.getRowLocation() == i) {
//                                            item.setOcr(grid.getContent());
//                                            item.setUrl(grid.getImageUrl());
//                                        }
//                                    }
//                                    items.add(item);
//                                }
//                            }

                            for (int n = 0; n < gridItems.size(); n++) {
                                GridItem grid = gridItems.get(n);
                                TableItem item = new TableItem();
                                item.setRowIndex(grid.getRowLocation());
                                item.setColumnIndex(grid.getColumnLocation());
                                item.setOcr(grid.getContent());
                                item.setUrl(grid.getImageUrl());
                                items.add(item);
                            }
                            Call<ResponseBaseMsg> call = mSubmitAPI.submitResult(tab);
                            try {
                                return call.execute();
                            } catch (IOException e) {
                                return null;
                            }
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Response<ResponseBaseMsg>>() {
                        @Override
                        public void accept(Response<ResponseBaseMsg> response) throws Exception {
                            if (response != null && response.isSuccessful()) {
                                ResponseBaseMsg mSubmitResponse = response.body();
                                Log.d(TAG, "submit success msg:" + mSubmitResponse.toString());
                                clearHistory();
                            } else {
                                Log.e(TAG, "submit fail  ");
                            }
                            loading.dismiss();

                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Log.e(TAG, "submit fail  ");
                            Toast.makeText(MainActivity.this, R.string.submit_fail, Toast.LENGTH_SHORT).show();
                            loading.dismiss();

                        }
                    });

        } else {
            loading.dismiss();
        }

    }


    private void addState(int newState) {
        currentState = currentState | newState;
    }

    private void removeState(int newState) {
        currentState = currentState ^ newState;
    }

    /**
     * @return true ,if all state id well
     */
    private boolean checkState() {
        Log.d(TAG, "checkState currentState:" + currentState);
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

    private boolean deubg = true;

    private void printLog(String message) {
        if (deubg) {
            Log.d(this.getClass().getName(), message);
        }
    }

}
