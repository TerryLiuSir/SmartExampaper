package com.ragentek.factorypaper;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.ArrayMap;
import android.util.Log;


import com.ragentek.factorypaper.view.widgets.CustomAlertDialog;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseLauncherActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 0x0001;

    ArrayMap<String, Integer> permisionMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (checkPermissions()) {
            initModules();
            initView();
        }
        super.onCreate(savedInstanceState);

    }

    private boolean checkPermissions() {
        permisionMap = permissionsRequest();
        if (permisionMap == null && permisionMap.size() == 0) {
            return true;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        List<String> mPermissionList = new ArrayList<>();
        for (int i = 0; i < permisionMap.size(); i++) {
            if (ContextCompat.checkSelfPermission(BaseLauncherActivity.this, permisionMap.keyAt(i)) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permisionMap.keyAt(i));
            } else {
                permisionMap.removeAt(i);
                i--;
            }
        }
        if (!mPermissionList.isEmpty()) {
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);
            printLog("requestPermissions " + permissions.length);
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        printLog("onRequestPermissionsResult requestCode： " + requestCode + "，length：" + grantResults.length);

        boolean isPermisonGranted = true;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            isPermisonGranted = false;
                        }
                    }
                    if (isPermisonGranted) {
                        initModules();
                        initView();
                    } else {
                        StringBuilder sb = new StringBuilder(getResources().getString(R.string.permission_tips)).append('\n');
                        for (int i = 0; i < grantResults.length; i++) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                sb.append(getResources().getString(permisionMap.valueAt(i))).append('\n');
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
                                });
                        builder.create().show();
                    }
                }
                break;

            default:
                break;

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    /**
     * @return the ArrayMap<String, String> as <permission ,tips>
     */
    abstract ArrayMap<String, Integer> permissionsRequest();

    abstract void initModules();

    abstract void initView();

    private void printLog(String message) {
        Log.d(this.getClass().getName(), message);
    }

}



