package com.ragentek.smartexampaper;

import android.Manifest;
import android.app.DatePickerDialog;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

;
import com.ragentek.penmanager.PenManagerService;
import com.ragentek.penmanager.model.PenDot;
import com.ragentek.smartexampaper.paper.PaperManager;
import com.ragentek.smartexampaper.paper.models.SimpleDot;
import com.ragentek.smartexampaper.resultPaperUI.Page1Activity;
import com.ragentek.smartexampaper.view.HandwritingSurfaceView;
import com.ragentek.smartexampaper.view.widgets.CustomAlertDialog;
import com.ragentek.smartexampaper.view.widgets.CustomLoadingDialog;
import com.ragentek.smartexampaper.view.widgets.ListDeviceDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    @BindView(R.id.iv_submit)
    ImageView submit;
    @BindView(R.id.tv_state)
    TextView stateTv;
    @BindView(R.id.sv_answerArea)
    HandwritingSurfaceView answerAre;
    @BindView(R.id.iv_devices)
    ImageView deviceImage;
    @BindView(R.id.iv_result)
    ImageView resultImage;
    @BindView(R.id.toolBar)
    RelativeLayout toolBar;

    private static final int PERMISSIONS_REQUEST_CODE = 0x0001;
    private int lastQuestionIdex = -1;
    private String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION};
    int[] permissionsTips = new int[]{
            R.string.sdcard,
            R.string.location};

    private PenManagerService.PenManager penHandle;
    private PaperManager mPaperManager;

    private ArrayList<BluetoothDevice> deviceList = new ArrayList();
    private volatile ArrayList<SimpleDot> tempDots = new ArrayList<>();
    private ListDeviceDialog mListDeviceDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        checkPermissions();
        initModules();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseModules();
    }

    private void releaseModules() {
        Intent serviceIntent = new Intent(this, PenManagerService.class);
        serviceIntent.setPackage(getPackageName());
        unbindService(mPenManagerConnection);
    }

    private void initModules() {
        Intent serviceIntent = new Intent(this, PenManagerService.class);
        serviceIntent.setPackage(getPackageName());
        startService(serviceIntent);
        bindService(serviceIntent, mPenManagerConnection, Context.BIND_AUTO_CREATE);
        mPaperManager = PaperManager.getInstance();
    }


    private void checkPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        List<String> mPermissionList = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        if (!mPermissionList.isEmpty()) {
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, PERMISSIONS_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    StringBuilder sb = new StringBuilder(getResources().getString(R.string.permission_tips)).append('\n');
                    ;
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            sb.append(getResources().getString(permissionsTips[i])).append('\n');
                        }
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(this);
                    builder.setMessage(sb.toString())
                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    checkPermissions();
                                }
                            })
                            .setNegativeButton(R.string.cancel, null);
                    builder.create().show();
                }
                break;

            default:
                break;

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @OnClick(R.id.iv_devices)
    void scannerDevices() {

        showDevicesList();
        penHandle.startScan();

    }

    @OnClick(R.id.iv_clear)
    void clearHistory() {
        CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(this);
        builder.setMessage(R.string.clear_tips)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mPaperManager.clear(BaseApplication.DOTS_SAVE_PATH);
                        answerAre.clear();
                    }
                })
                .setNegativeButton(R.string.cancel, new DatePickerDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create().show();
    }

    @OnClick(R.id.iv_submit)
    void submit() {
        final CustomLoadingDialog loading = new CustomLoadingDialog(this);
        loading.setMessage(R.string.loading_commit);
        loading.show();
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> success) throws Exception {
                boolean resultAI = mPaperManager.generateAllSquareImage();
                boolean resultShow;
                if (BaseApplication.getScreenWidth() > BaseApplication.getScreenHeight()) {
                    resultShow = mPaperManager.generateAllQuesionImage(BaseApplication.getScreenWidth() - toolBar.getWidth(), BaseApplication.getScreenHeight());
                } else {
                    resultShow = mPaperManager.generateAllQuesionImage(BaseApplication.getScreenWidth(), BaseApplication.getScreenHeight() - toolBar.getHeight());

                }
                success.onNext(new Boolean(resultAI && resultShow));
                success.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean simpleDots) throws Exception {
                        if (simpleDots.booleanValue()) {
                            Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(MainActivity.this, "fail", Toast.LENGTH_SHORT).show();
                        }
                        loading.dismiss();

                    }
                });
    }

    @OnClick(R.id.iv_result)
    void showReslut() {
        Intent intent = new Intent(this, Page1Activity.class);
        startActivity(intent);
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
            stateTv.setText("unready");
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //TODO
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
            printLog("onConnected");
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
                    tempDots.clear();
                    //if   down point ,get the negative of x ,y ,
//                    tempDots.add(new SimpleDot(SimpleDot.DOWN, SimpleDot.DOWN, dot.getPageID()));
                    printLog(" TYPE_DOWN:x =" + dot.getX() + "y=" + dot.getY());

                    tempDots.add(new SimpleDot(-1.0f, -1.0f, dot.getPageID()));
                    tempDots.add(new SimpleDot(dot.getX(), dot.getY(), dot.getPageID()));
                    break;
                case PenDot.TYPE_MOVE:
                    tempDots.add(new SimpleDot(dot.getX(), dot.getY(), dot.getPageID()));
                    break;
                case PenDot.TYPE_UP:
                    printLog("TYPE_UP=" + tempDots.size());
                    if (tempDots == null || tempDots.size() < 1) {
                        return;
                    }
                    final int currentQuestionArea = PaperManager.getQuestionIdexFromDot(tempDots.get(1));
                    Log.d(TAG, "currentQuestionArea  :" + currentQuestionArea);

                    if (currentQuestionArea < 0) {
                        Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Observable.create(new ObservableOnSubscribe<ArrayList<SimpleDot>>() {
                        @Override
                        public void subscribe(ObservableEmitter<ArrayList<SimpleDot>> emitter) throws Exception {
                            ArrayList<SimpleDot> currentDots = new ArrayList<>(tempDots);
                            Log.d(TAG, "writeDots2File start ");
                            mPaperManager.writeDots2File(currentDots, currentQuestionArea);
                            Log.d(TAG, "writeDots2File end ");

                            if (currentQuestionArea != lastQuestionIdex) {
                                currentDots = mPaperManager.readFloatFromData(currentQuestionArea);
                            }
                            ArrayList<SimpleDot> resizedSimpleDot = new ArrayList<>();
                            Log.d(TAG, "mapDot2ScreenArea start ");
                            boolean isLandScreen = BaseApplication.getScreenWidth() > BaseApplication.getScreenHeight();
                            for (SimpleDot dot : currentDots) {
                                if (isLandScreen) {
                                    SimpleDot mewDot = PaperManager.mapDot2ScreenArea(dot, currentQuestionArea, BaseApplication.getScreenWidth() - toolBar.getWidth(), BaseApplication.getScreenHeight());
                                    resizedSimpleDot.add(mewDot);
                                } else {
                                    SimpleDot mewDot = PaperManager.mapDot2ScreenArea(dot, currentQuestionArea, BaseApplication.getScreenWidth(), BaseApplication.getScreenHeight() - toolBar.getHeight());
                                    resizedSimpleDot.add(mewDot);
                                }

                            }
                            Log.d(TAG, "mapDot2ScreenArea end ");
                            Log.d(TAG, "loadAnwserArea start ");
                            answerAre.loadAnwserArea(resizedSimpleDot, currentQuestionArea != lastQuestionIdex);

                            //  mPaperManager.generateAllSquareImage();//test generate image
                            Log.d(TAG, "loadAnwserArea end ");
                            emitter.onNext(resizedSimpleDot);
                        }
                    })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<ArrayList<SimpleDot>>() {
                                @Override
                                public void accept(ArrayList<SimpleDot> resizedSimpleDot) throws Exception {
                                    if (resizedSimpleDot == null) {
                                        Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    printLog("  currentQuestionArea:" + currentQuestionArea);
                                    printLog("  lastQuestionIdex:" + lastQuestionIdex);
                                    lastQuestionIdex = currentQuestionArea;
                                    String question = getResources().getString(R.string.question) + ":" + (currentQuestionArea + 1);
                                    stateTv.setText(question);
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                                }
                            });
                    break;
            }
        }


//for test
//                    Observable.create(new ObservableOnSubscribe<ArrayList<SimpleDot>>() {
//                        @Override
//                        public void subscribe(ObservableEmitter<ArrayList<SimpleDot>> emitter) throws Exception {//
//                            ArrayList<SimpleDot> dots = new ArrayList<>(tempDots);
//                            //TODO O is action point
//                            int questionArea = PaperManager.getQuestionIdexFromDot(dots.get(1));
//                            if (questionArea < 0) {
//                                printLog("  questionArea not exits :" + questionArea);
//                                emitter.onNext(null);
//                                return;
//                            }
//                            //TODO for test  map pen  dot to screen dot
//                            ArrayList<SimpleDot> resizedSimpleDot = new ArrayList<>();
//                            for (SimpleDot dot : dots) {
//                                SimpleDot mewDot = PaperManager.mapDot2ScreenArea(dot, questionArea, BaseApplication.getScreenWidth() - 100, BaseApplication.getScreenHeight());
//                                resizedSimpleDot.add(mewDot);
//                            }
////                            Size size = PaperManager.getAnswerAreaSize(questionArea, BaseApplication.getScreenWidth() - 100, BaseApplication.getScreenHeight());
////                            PaperManager.generateQuesionImage(resizedSimpleDot, size.getWidth(), size.getHeight(), questionArea + ".png");
//                            emitter.onNext(resizedSimpleDot);
//
//                            mPaperManager.writeDots2File(dots, questionArea);
////                            //TODO for test  read dots
////                            printLog("read questionArea=" + questionArea);
////                            ArrayList<SimpleDot> readresult = mPaperManager.readFloatFromData(questionArea);
////                            printLog("read size=" + readresult.size());
//
//                        }
//                    })
//                            .subscribeOn(Schedulers.io())
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .subscribe(new Consumer<ArrayList<SimpleDot>>() {
//                                @Override
//                                public void accept(ArrayList<SimpleDot> simpleDots) throws Exception {
//                                    if (simpleDots == null) {
//                                        Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
//                                        return;
//                                    }
//                                    answerAre.loadAnwserArea(simpleDots, true);
//                                }
//                            }, new Consumer<Throwable>() {
//                                @Override
//                                public void accept(Throwable throwable) throws Exception {
//                                    Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                    break;
//            }
//        }
    };

    private void printLog(String message) {
        Log.d(TAG, message);
    }
}
