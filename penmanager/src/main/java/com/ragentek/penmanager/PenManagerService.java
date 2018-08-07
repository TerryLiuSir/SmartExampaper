package com.ragentek.penmanager;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


import com.ragentek.penmanager.model.PenDot;
import com.tqltech.tqlpencomm.BLEException;
import com.tqltech.tqlpencomm.BLEScanner;
import com.tqltech.tqlpencomm.Dot;
import com.tqltech.tqlpencomm.PenCommAgent;
import com.tqltech.tqlpencomm.listener.TQLPenSignal;

import java.util.ArrayList;


/**
 * Created by xuanyang.feng on 2018/5/22.
 */

public class PenManagerService extends Service {
    private static final String TAG = "PenManagerService";
    private PenManagerService.PenListener mPenListener;
    private Binder mPenManager;
    private PenCommAgent penAgent;
    private String mCurrentPenAddress;
    private static final int PAPER_A4_A5 = 1;
    private static final int PAPER_A3 = 2;
    public static final boolean DEBUG = true;
    public static final String DEVICE_NAME = "Smartpen";


    //    private PaperManager mPaperManager;

    @Override
    public void onCreate() {
        Log.w(TAG, "onCreate");
        if (mPenManager == null) {
            mPenManager = new PenManager();
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mPenManager;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        return super.onUnbind(intent);
    }

    public void close() {
        if (penAgent == null) {
            return;
        }
        Log.w(TAG, "mBluetoothGatt closed");
        penAgent.disconnect(mCurrentPenAddress);
        penAgent = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class PenManager extends Binder {

        private PenManager() {
            penAgent = PenCommAgent.GetInstance(getApplication());
            penAgent.init();
            penAgent.setXYDataFormat(PAPER_A4_A5);
            penAgent.setTQLPenSignalListener(mPenSignalCallback);
        }

        public void startScan() {
            penAgent.FindAllDevices(new ScanListener());
        }

        public void setPenListener(PenManagerService.PenListener listener) {
            mPenListener = listener;
        }

        public void stopScan() {
            penAgent.stopFindAllDevices();

        }

        public void connect(String address) {
            if (penAgent.isConnect() && address.equals(mCurrentPenAddress)) {
                return;
            }
            penAgent.disconnect(address);
            penAgent.connect(address);
            mCurrentPenAddress = address;
        }

        public String getConnectedAddress() {
            return mCurrentPenAddress;
        }

        public boolean isConnected() {
            return penAgent.isConnect();
        }

        public void disconntct() {
            penAgent.disconnect(mCurrentPenAddress);
        }
    }

    protected TQLPenSignal mPenSignalCallback = new TQLPenSignal() {
        private PenDot mPenDot;

        @Override
        public void onConnected(int forceMax, String fwVersion) {
            printLog("onConnected, forceMax=" + forceMax + ",fwVersion" + fwVersion);
            mPenDot = new PenDot();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    int count = 10;
                    while (!penAgent.isConnect()) {
                        try {
                            Thread.sleep(1000);
                            count--;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (count == 0) {
                            return;
                        }
                    }
                    penAgent.ReqAdjustRTC();
                    penAgent.ReqSetupPenAutoPowerOn(true);
                    penAgent.ReqSetupPenAutoShutdownTime((short) 5);
                    penAgent.ReqSetupPenBeep(true);
                    penAgent.ReqSetupPenSensitivity((short) 0);
                    penAgent.ReqSetupPenLED(0x02);
                    penAgent.ReqPenStatus();
                    printLog(" mPenListener.onConnected");

                    mPenListener.onConnected(mCurrentPenAddress);
                }
            }).start();
        }


        @Override
        public void onDisconnected() {
            mPenListener.onDisConnected(mCurrentPenAddress);
        }

        @Override
        public void onOfflineDataList(int num) {
            printLog("onOfflineDataList, num=" + num);
        }

        @Override
        public void onStartOfflineDownload(boolean isSuccess) {
            printLog("onStartOfflineDownload, isSuccess=" + isSuccess);
            if (!isSuccess) {
                return;
            }
        }

        @Override
        public void onFinishedOfflineDownload(boolean isSuccess) {
            printLog("onFinishedOfflineDownload, isSuccess=" + isSuccess);
            if (!isSuccess) {
                return;
            }
        }

        @Override
        public void onReceiveOfflineStrokes(Dot dot) {
            printLog("onReceiveOfflineStrokes, dot=" + dot);
        }

        @Override
        public void onPenAuthenticated() {
            printLog("onPenAuthenticated");
        }

        @Override
        public void onReceivePenStatus(long timetick, int forcemax, int battery, int usedmem,
                                       boolean autopowerMode, boolean beep, short autoshutdownTime, short penSensitivity) {

            printLog("PenStatus {timetick:" + timetick
                    + ", forcemax:" + forcemax
                    + ", battery:" + battery
                    + ", usedmem:" + usedmem
                    + ", autopowerMode:" + autopowerMode
                    + ", beep:" + beep
                    + ", autoshutdownTime:" + autoshutdownTime
                    + ", penSensitivity:" + penSensitivity
                    + "}");
            //TODO
            mPenListener.onReceivePenStatus(battery, usedmem);


        }

        @Override
        public void onPenAutoPowerOnSetUpResponse(boolean isSuccess) {
            printLog("onPenAutoPowerOnSetUpResponse, isSuccess=" + isSuccess);
        }

        @Override
        public void onPenAutoShutdownSetUpResponse(boolean isSuccess) {
            printLog("onPenAutoShutdownSetUpResponse, isSuccess=" + isSuccess);
        }

        @Override
        public void onPenBeepSetUpResponse(boolean isSuccess) {
            printLog("onPenBeepSetUpResponse, isSuccess=" + isSuccess);
        }

        @Override
        public void onPenSensitivitySetUpResponse(boolean isSuccess) {
            printLog("onPenSensitivitySetUpResponse, isSuccess=" + isSuccess);
        }


        @Override
        public void onReceiveDot(Dot pointObject) {
            printLog("receive dot=" + pointObject.toString());
            double x = ((double) pointObject.fx) / 100.0 + pointObject.x;
            double y = ((double) pointObject.fy) / 100.0 + pointObject.y;
            mPenDot.setX((float) x);
            mPenDot.setY((float) y);
            mPenDot.setCounter(pointObject.Counter);
            mPenDot.setSectionID(pointObject.SectionID);
            mPenDot.setOwnerID(pointObject.OwnerID);
            mPenDot.setBookID(pointObject.BookID);
            mPenDot.setPageID(pointObject.PageID);
            mPenDot.setTimelong(pointObject.timelong);
            mPenDot.setForce(pointObject.force);
            mPenDot.setAngle(pointObject.angle);

            switch (pointObject.type) {
                case PEN_DOWN:
                    mPenDot.setType(PenDot.TYPE_DOWN);
                    break;
                case PEN_MOVE:
                    mPenDot.setType(PenDot.TYPE_MOVE);
                    break;
                case PEN_UP:
                    mPenDot.setType(PenDot.TYPE_UP);
                    break;
            }
            mPenListener.onReceiveDot(mPenDot);
        }

        @Override
        public void onReceiveOIDFormat(int penOIDSize) {
            printLog("onReceiveOIDFormat, penOIDSize=" + penOIDSize);
        }

        @Override
        public void onUpDown(boolean isUp) {
            printLog("onUpDown, isUp=" + isUp);
        }

        @Override
        public void onPenTimetickSetupResponse(boolean isSuccess) {
            printLog("onPenTimetickSetupResponse, isSuccess=" + isSuccess);
        }

        @Override
        public void onPenNameSetupResponse(boolean isSuccess) {
            printLog("onPenNameSetupResponse, isSuccess=" + isSuccess);
        }
    };


    private class ScanListener implements BLEScanner.OnBLEScanListener {


        @Override
        public void onScanResult(BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {
            printLog("scan result=" + bluetoothDevice.getName() + ", " + bluetoothDevice.getAddress());
            if (bluetoothDevice.getName() != null
                    && bluetoothDevice.getName().contains(DEVICE_NAME)) {
                mPenListener.onDeviceFind(bluetoothDevice);
            }
        }


        @Override
        public void onScanFailed(BLEException e) {
            printLog("onScanFailed, e=" + e.getMessage());
            mPenListener.onError(e.getMessage());
        }
    }

    public interface PenListener {
        void onReceivePenStatus(int battery, int usedmem);

        void onDeviceFind(BluetoothDevice results);

        void onDisConnected(String penAddress);

        void onError(String msg);

        void onConnected(String penAddress);

        void onReceiveDot(PenDot dot);


    }

    private void printLog(String message) {
        if (DEBUG) {
            Log.d(TAG, message);
        }
    }
}



