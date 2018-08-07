package com.ragentek.smartexampaper;

import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ragentek.smartexampaper.view.widgets.ListDeviceDialog;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class TestActivity extends AppCompatActivity {

    public static final String TAG = "TestActivity";

    ListDeviceDialog mListDeviceDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        ButterKnife.bind(this);

    }


    @OnClick(R.id.btn_test)
    void test() {

        if (mListDeviceDialog == null) {
            mListDeviceDialog = new ListDeviceDialog.Builder()
                    .setNegativeButton(getResources().getString(R.string.cancel))
                    .setPositiveButton(getResources().getString(R.string.scan))
                    .setItemClickListener(new ListDeviceDialog.OnClickListener() {
                        @Override
                        public void onClick(int position) {
                            switch (position) {
                                case ListDeviceDialog.Builder.ID_NEGATIVE_BUTTON:
                                    mListDeviceDialog.stopRefresh();
                                    break;
                                case ListDeviceDialog.Builder.ID_POSITIVE_BUTTON:
                                    mListDeviceDialog.refresh(false);
                                    break;
                                default:
                                    Log.d(TAG, "position:" + position);

                            }
                        }
                    })
                    .create(this);
        }

        mListDeviceDialog.show();

    }

    private void printLog(String message) {
        Log.d(TAG, message);
    }


}
