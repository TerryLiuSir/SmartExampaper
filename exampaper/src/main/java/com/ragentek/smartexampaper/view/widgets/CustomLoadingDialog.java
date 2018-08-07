package com.ragentek.smartexampaper.view.widgets;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;

import com.ragentek.smartexampaper.R;


public class CustomLoadingDialog {
    private AlertDialog dialog;
    private TextView messageText;

    public CustomLoadingDialog(Activity activity) {
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_loading, null);
        messageText = view.findViewById(R.id.message);

        dialog = new AlertDialog.Builder(activity).create();
        dialog.setView(view);

        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
    }

    public void setMessage(int resId) {
        String message = dialog.getContext().getResources().getString(resId);
        messageText.setText(message);
    }

    public void setMessage(String message) {
        messageText.setText(message);
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public void setOnCancelListener(DialogInterface.OnCancelListener listener) {
        dialog.setOnCancelListener(listener);
    }
}
